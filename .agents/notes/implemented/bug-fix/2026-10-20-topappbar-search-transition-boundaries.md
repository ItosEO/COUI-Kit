# Agent Note: 顶栏 overlay、搜索转场与焦点边界

Status: implemented

## Problem

Material3 顶栏的祖先裁剪会截断悬浮按钮。逐按钮提升会丢失祖先淡化，完整背景 patch 又会擦除兄弟阴影。搜索对整表面模糊、异步坐标更新及越界输入可见请求会造成失焦、错位或 Pager 位移。

## Decision

TopAppBar 建立局部 SharedTransitionScope。完整槽位通过 TopAppBarButtonGroup 提升，捕获边界包含内部进出动画；测量、输入、语义和 Popup 锚点仍在原位。不要只设置按钮 clip=false，也不要复制 Material3 布局。overlay 仍受外层页面和窗口变换/裁剪影响，不是全局独立窗口。

按钮材质与 caustic 使用已绘制的渐进背景，排除标题和按钮自身。顶栏单主体启用 foregroundComposition，将结果编码为合法预乘透明前景后 SrcOver；无光区域输出透明，避免背景回填擦除兄弟投影。保留原距离场、衰减和自身普通投影前缀。真正重叠的有光区域仍没有完整兄弟 framebuffer 参考，不承诺原厂顺序等价。底栏与 FAB 不跟随改为该模式。

搜索按钮与输入共用状态；目标值直接供组合/测量，SideEffect 只启动动画，入口与展开表面互斥。点击中心和展开端点统一到所属顶栏实际根坐标，在 PlacementScope 中映射；转场期间冻结端点，宽度、位移和模糊共读一帧进度。收起保留展开排版宽度，不让恢复操作区重定向路径。

搜索转场滤镜只处理透明 contentLayer，材质、轮廓、光晕和普通投影保持清晰。退场操作让出宽度但原位淡化；常驻操作通过同一搜索状态连续移动。纯淡化保持实际与预布局尺寸，完成后移除，不让零尺寸目标驱动内部位置动画。可见性与展收目标分开，隐藏 UI 不自动丢失查询或启动收起；焦点、IME、返回和业务加载仍归宿主。

固定搜索容器将向祖先的 BringIntoView 请求映射到稳定可见范围，输入内部光标滚动保留。不通过延迟焦点、禁用所有 Pager 或强制归位掩盖问题。状态随节点清理，不在宿主保存布局坐标或另建运动 owner。

## Evidence

合并整理原宿主的同名搜索 Note、2026-10-20-coui-toolbar-clipping-backdrop、topappbar-sibling-shadow-compositing 及顶栏/搜索契约。对应 AndroidX AppBarLayout 的祖先 clipToBounds、SharedTransitionScope 的捕获/绘制、PlacementScope 的真实坐标与 Pager BringIntoView 行为是承载依据。原厂转场来源为便签 COUISearchBar.showInToolBar/hideInToolBar，不等比缩放整条输入。

当前实现见 [搜索组件](../../../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarSearch.kt) 和 [按钮组](../../../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButtonGroup.kt)。本项目为常驻操作避让而加快搜索收起、调整部分光效强度，属于已有特化，不称为原厂全部曲线逐项一致。

## Alternatives considered

- **逐按钮 overlay 或只扩大局部绘制范围。** 改动少，但前者丢父级 alpha，后者不能撤销祖先裁剪或消除背景擦除。
- **PLUS 直接叠加光贡献。** 对不透明目标简单，但按钮可能在透明祖先离屏层内，PLUS 会改变淡化背景项。
- **整表面模糊或延迟焦点。** 可以暂时掩盖转场问题，却改变原效果层序或输入响应，不能修复可见范围请求。
- **整组共享 renderer 与采样前缀。** 可进一步对齐兄弟光贡献，但需要同时承接真实绘制顺序和祖先淡化，超出当前局部修复。

## Consequences

前景 caustic 在原阈值内省略透明绘制，依据见[绘制工作省略](../simplification/2026-11-01-render-work-elision.md)；本篇的前景编码、兄弟合成与搜索输入边界保持。

保留原位输入与布局，同时承担局部录制和 LookaheadScope 成本。透明 SDR 前景编码不能扩展为 HDR/透明窗口等价保证，减少 patch 覆盖也不能证明 GPU 更快。

原工程曾执行坐标反例、合成数值及 PSI 检查，未以它们证明真实 Android 动画、焦点或 GPU 效果。本轮只整理核心决定与当前路径；设备对照仍独立记录，不把历史模型作为当前真机验收。
