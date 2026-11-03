# 公开 API 设计

默认使用 `internal` 或 `private`。公开入口应能解释使用场景、状态 owner、平台门槛、单位、失败行为和资源生命周期。组件通过受控值与回调接入，不接管宿主业务。`Modifier` 为第一个参数并默认 `Modifier`。

公开 UI 组件使用无 Coui 前缀的名称，如 `Text`、`Switch`、`Menu` 和 `Scaffold`，通过包名区分来源。类型、Defaults、Style、状态、`CouiTheme`、`rememberCoui…` 工厂及 `coui…` 效果入口保留前缀。同文件混用其他设计体系的同名组件时，用 import 别名区分。

保留高自定义程度的底层算法接口；常用入口复用同一后端，不再造效果。普通阴影保留 `couiShadow` 与 `CouiShadowRenderer`，双主体融合只公开算法和跟随回弹，不加入侧滑容器。预设集中在所属组件族 Defaults，宿主不复制固定参数。

菜单滚动条坐标、拖动状态、Popup 定位、菜单动效帧、搜索运动实现和底栏附件控制器属于内部装配。不要为了测试或 Demo 暴露它们。公开预设弹簧仍可直接引用。菜单模糊从 `rememberCouiMenuBlur` 获取，不让宿主组装固定数量的 GraphicsLayer。

修改公开声明前检索库、Demo 和已知消费者。改变可见性、参数、单位或生命周期时，同步调用与接入文档，并在 PR 写明迁移方式。

## 参数归属与 Modifier 顺序

添加配置时，先确认它修改哪个节点、是否需要组件内部信息，再选择入口：

| 职责 | 入口 | 例子 |
| --- | --- | --- |
| 当前节点的外部布局与独立装饰 | 标准 Modifier；COUI 特有绘制复用已有效果 | Wheel 高度、外部留白、整组选择线 |
| 内部多个区域共同使用的状态与样式 | 组件参数或已有 Defaults | enabled、日期范围、字号、状态颜色、面板形状 |
| 多个子节点的排列与业务联动 | 组件组合或内容槽位 | 小时与分钟、确认草稿、日期列联动 |

`enabled` 和 `onClick` 属于交互组件的核心职责，不能只因多个组件都有它们就改成 Modifier。Wheel 的字号、diffusion 和 wrap 影响内部测量或运动，仍由参数协调。不要增加 `couiWidth` 等标准 API 包装，也不要用 Modifier 隐藏组件配置。内部去重不要求新增公开类型；只有实际跨模块消费者需要时，才公开最小入口。

调用者的 Modifier 在根布局应用一次，内部默认尺寸随后追加，并服从父级约束。布局、绘制和命中使用同一实际尺寸。修改已有内区留白前，先追踪其测量和动画用途，不能机械替换为根 padding。

顺序是调用契约，不是“后写覆盖”或“全部累加”：

~~~kotlin
Modifier.background(color).padding(16.dp) // 背景包含留白。
Modifier.padding(16.dp).background(color) // 留白位于背景外。
Modifier.clickable(onClick = onClick).padding(16.dp) // 留白可点击。
Modifier.padding(16.dp).clickable(onClick = onClick) // 留白不参与点击。
~~~

`background(shape)` 只画背景，`clip(shape)` 裁剪其内侧绘制；尺寸 Modifier 依次收紧约束，不相加。效果入口须说明背景/内容范围、测量影响、坐标、裁剪和 padding 顺序。阴影通常放在主体裁剪外，背景滤镜只处理采样层，不能顺带模糊文字。

配置可以复用，可变 renderer、采样坐标和图层归具体节点。通用效果内部需要这些资源时使用节点生命周期；简单无状态装饰直接组合已有 Modifier。显式接收 renderer、内容层或锚点的低层接口保留其单节点 owner，不能复制返回的 Modifier 来隐式复制资源。主题在组件消费位置解析；显式传入的颜色不因节点移动而变成另一套主题。

同一几何应提供裁剪与效果所需的参数。Dialog/Menu 从 `CouiShape` 读取半径和权重；任意 Shape 无法反推这些数值，保留明确的高级配置。不同几何（例如 Checkbox 填充与边框）分别保留。

依据：[Compose Modifier 顺序](https://developer.android.com/develop/ui/compose/modifiers)、[自定义 Modifier 与节点](https://developer.android.com/develop/ui/compose/custom-modifiers)。

## 常规入口与底层自定义

[组件目录](../components/README.md)面向直接装配，[效果目录](../effects/README.md)面向独立绘制。在原组件参数上提供合理默认值，不新增简单版与高级版两套组件。默认值使用初始化策略、局部主题与所属 Defaults，未初始化时使用原生预设；显式配置继续优先。深浅色、内容插槽及染色规则统一见[外观与预设](../guide/appearance.md)。高自定义接口继续支持真实的坐标、几何和合成需求。

保留公开参数顺序。需要后置参数参与自动解析时，仅对相关配置使用可空默认值或原类型的 Unspecified；不批量改成 null。Backdrop、Renderer 的默认策略以实际窗口和单/多主体所有权为准，不随颜色参数一起隐藏。

公开参数应说明单位、本地或窗口坐标、有效范围、输入错误、版本分支，以及状态和资源由谁持有。构造失败要向调用方暴露；需要错误 UI 的宿主在工厂创建阶段捕获结果，再决定是否进入组件。

诊断入口 CouiRenderDiagnostics / renderDiagnosticProbe 只在主线程按需启停，不作为业务状态。单主体和固定三主体浮动装配能力不同；说明见[浮动表面](../effects/floating-surfaces.md)。

## 文档与迁移

公开签名和 KDoc 是参数声明的来源；使用页解释当前语义和正确装配，不复制另一份默认数值。变更公开 API 时同步调用方与对应页面；迁移方式在 PR 中说明，发行时记入 CHANGELOG。

公开边界的历史决定见[API 收口](../../.agents/notes/implemented/architecture/2026-10-23-public-api-boundary.md)。
