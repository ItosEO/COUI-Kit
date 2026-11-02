# Agent Note: Modifier 职责与 Picker 面板几何

Status: implemented

## Problem

Wheel 同时接收高度参数与根 Modifier，父容器已有布局入口。Calendar 与 Demo 的多列选择线各自复制像素计算。Dialog/Menu 的裁剪形状与效果半径需要调用者手动同步；默认 Style 复制后只改形状容易留下旧半径。部分通用效果的可变 renderer、图层和坐标归组合调用点，复用同一个 Modifier 时会被多个节点共享。

## Decision

参数与 Modifier 的归属规则由 [API 设计](../../../../docs/development/api-design.md)持有。外部布局使用标准 Modifier，内部状态、字形、运动和分区样式继续用参数；不新增 Style 总容器或组件配置 Modifier。

Wheel 移除 height 参数，根节点在调用者链后追加 Defaults 高度，继续接受外部约束。实际高度参与原有整数步长，零高度也作为真实测量值，渐隐不对零高度做除法；双 Scroller、取整、选中带及输入规则保持原样。

Picker 选择线共用 `couiPickerSelectionLines`，颜色由消费处传入，横向内缩保留各场景值。Calendar 与 Demo 分属不同模块，而 Demo 只消费公开 API，因此公开这个独立绘制入口；不公开多列状态、布局容器或内部测量数据。原厂依据仍为 [Picker 记录](../feature/2026-11-01-coui-pickers.md)中的 Clock/Calendar，保留原有中心、线厚、整数截断和先线后列顺序。

Dialog/Menu 的半径缺省为 Dp.Unspecified，在消费处从 CouiShape 解析。已知 Shape 同时提供轮廓权重；显式重复半径必须相同。任意 Shape 保留显式半径路径与原组件轮廓权重，不猜路径几何。无材质轮廓的 Dialog 不需要额外数值。普通投影保留 RoundRect caster，内容 G2 与投影的不同几何没有被强制合并。默认外观不变，修改已知 Shape 的半径或权重时，轮廓随其更新。

普通阴影使用 ShadowNode 持有一份 renderer，参数、尺寸变化不重建 Shader。背景采样使用 DrawCouiBackdropNode，图层在 attach/detach 成对取得和释放；记录使用独立 Density。渐进背景复用同一采样与内容层序，节点另外持有两遍图层和 renderer，参数或 Density 变化更新，移除后清空，重新挂载重新创建。卷积核、先横后纵顺序、采样留白及缓存判定保留。

浮动表面显式接收 renderer 和内容层，继续是单节点低层装配；本轮核对 FAB、底栏、顶栏、Search 与 Chip 的独立 owner。菜单主子面板分别创建材质与阴影，背景源和 anchor 保留单源/单触发节点契约。状态反馈的动画由调用者提供的输入状态驱动，不复制成额外状态机。

## Alternatives considered

- 只删 height 并留下重复选择线，改动更少，但没有完成本次跨模块去重；共享入口仅保留实际使用的颜色和内缩。
- 把所有配置改成 Modifier 或统一 Style，调用表面更短，但会把内部测量与业务状态拆成第二条配置通道；保持参数与组合边界。
- 所有 Shape 都反推半径，可消除显式配置，但任意路径无法可靠反推，且普通投影本来使用独立 caster；只解析已有 CouiShape。
- 对效果统一使用 drawWithCache，代码较短，但阴影在尺寸变化时重建 Shader，采样坐标也仍需节点 owner；只将确认持有内部可变资源的通用入口改为 Node。显式资源低层入口不进行整套重构。

## Existing-note audit

- [Picker](../feature/2026-11-01-coui-pickers.md)、[弹窗](../feature/2026-10-24-coui-alert-dialog.md)：部分重叠，原厂行为与特化继续有效，本记录持有布局与几何迁移。
- [API 收口](2026-10-23-public-api-boundary.md)、[模块边界](2026-10-21-coui-module-boundary.md)：沿用，新增绘制入口具有库与 Demo 两个消费者。
- [菜单背景生命周期](2026-11-01-menu-backdrop-isolation-lifetime.md)：沿用，源的组合期保留和空闲隔离策略不变，本次改变消费者内部资源 owner。
- [渲染工作裁减](../simplification/2026-11-01-render-work-elision.md)：沿用渐进两遍和 uniform 快照缓存；没有改变算法或宣称性能收益。
- 活跃 proposed/rejected 没有同范围决定需取代，其余记录无关。

## Consequences

Wheel 调用需要迁移并重新编译。默认半径字段现在是自动解析标记，使用方不能直接将 Dp.Unspecified 转为像素；旧显式且匹配的配置继续可用。任意 Shape 的轮廓仍由宿主负责匹配。渐进 Shader 创建移到节点首次绘制，异常保持可见；普通阴影在节点创建时校验后端。通用 Modifier 的节点隔离不改变显式传入的外部资源 owner。

静态检查与独立评审覆盖调用迁移、几何来源、选择线像素公式以及节点创建、更新、移除、再次挂载。Android 验证交由现有 PR CI；未执行本地 Android 构建、设备、像素、动画中段或性能验收，不能据此声称完整原厂等价。

## Verification

临时 JVM 断言直接编译当前 CouiShape 与 CouiPickerSelectionLines，使用缓存中的 Compose 类。半径检查覆盖自动解析、显式相同/冲突半径、copy 改形状、任意 Shape、缺省和非法数值；选择线通过 ContentDrawScope 替身记录生产绘制命令，覆盖三种 density 与窄宽度、两条线坐标/线厚和一次内容绘制。全部通过。该检查不执行 Android Canvas、布局或 GPU，不替代设备验收；临时入口不进入仓库。

两轮独立源码核验分别检查效果节点与完整改动范围。已修复新增逐帧 Density 分配与一处说明链接；核验未发现其他明确回归。Compose 生命周期依据来自当前缓存的 1.13.0-alpha03 源码，实际依赖与 Android 编译仍以 PR CI 为准。
