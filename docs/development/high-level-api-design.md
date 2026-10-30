# COUI-Kit 高层 API 分层设计（提案，未实现）

本文是 2026-10-30 的设计评估，不表示下述接口已经进入发行版。具体签名必须经原型编译、现有调用点扫描与设备验收后确定。

## 目标与不变量

- 常见控件的首次使用减少样式样板代码；优先让开关、滑条和弹窗只需要受控状态与回调。
- **保留所有现有公开入口**：高级颜色、Shape、Shader、Renderer、运动与采样能力不删除、不降级。
- **底层 Effect 继续可独立使用**：不强制通过统一组件、主题、页面容器或内部单例调用。
- 不读取厂商私有 API，不把 MaterialTheme 强制引入库；宿主决定业务状态、导航、错误展示和样式。
- 不为减少参数而隐藏跨窗口采样、Shader 平台门槛、renderer 生命周期或失败条件。

当前代码分别位于 [组件目录](../components/README.md)、[效果目录](../effects/README.md)、[架构](architecture.md)和 [API 约定](api-design.md)。

## 外部参考与取舍

- [Compose MIUIX](https://github.com/compose-miuix-ui/miuix)：`MiuixTheme` 提供颜色、字体和 CompositionLocal；基础 `Switch` 使用少量必填参数、可覆盖的 Defaults。可借鉴**主题提供与组件默认值**，不要求迁移到 KMP，也不照搬完整主题控制器。
- [DialogX](https://github.com/kongzue/DialogX)：按对话框用途提供容易调用的入口，样式可扩展。可借鉴**常用任务的便捷入口**；其 View/全局对话框调用模式不适合直接套到 Compose 的受控状态与生命周期。
- COUI-Kit：重点能力在背景采样、材质混合、光影、G2 Shape 和复杂动效；底层能力和原有独立调用路径应当成为架构的一部分，而非由便捷包装替代。

## 三层公共 API

| 层级 | 面向场景 | 典型入口 | 必须保留的边界 |
| --- | --- | --- | --- |
| L1 便捷组件 | 日常表单、设置与页面 | 简化重载的 `CouiSwitch`、`CouiSlider`、`CouiAlertDialog` 等 | 受控状态与语义默认正确；可局部覆写外观 |
| L2 高级组件 | 复杂布局、定制外观与交互 | 现有 `CouiSwitch(... dark, colors ...)`、`CouiBottomBar(... renderer, backdrop ...)` 等 | 原参数、预设、组件语义及回调契约不变 |
| L3 Effect / Engine | 自定义 Canvas、Shader、采样和动画 | `CouiFloatingRenderer`、`CouiBackdrop`、`couiShadow`、`rememberCouiSpotlight` 等 | 不要求调用任何 L1/L2 或全局主题 |

L1 必须组合已有 L2 和 L3 后端，而非复制绘制与物理实现；L2/L3 不能反向依赖 L1。现阶段无需拆成多个 Gradle 模块，先保证**逻辑层次**清楚。

## 可选外观上下文

建议提供 `CouiAppearance`（深浅色、颜色令牌、Shape、Typography）以及 `CouiTheme(appearance, content)`；通过 CompositionLocal 将常用 Defaults 传给便捷入口。

- 调用者可以显式传 `appearance` 覆盖当前上下文；优先级为**组件显式参数 ＞ CouiTheme 上下文 ＞ 库内明确标注的默认预设**。
- 不要求宿主把应用主题改成 COUI，也不自动读取 `MaterialTheme`；宿主可以自行写一层 M3 / 动态色适配器。
- 未放置 `CouiTheme` 仍允许使用现有 L2/L3。L1 的无主题默认色需单独定义、核对并记录，不假称原厂完整主题；不应因为缺失 Provider 崩溃。
- 动画强度、光影和使用者偏好与基本颜色区分；不添加会悄悄改变 Shader 语义的全局开关。高层可暴露可选的外观策略，但不改变底层工厂默认值。

以下是**拟议调用方式，不是当前可编译 API**：

~~~kotlin
CouiTheme(appearance = hostAppearance) {
    CouiSwitch(
        checked = checked,
        onCheckedChange = { checked = it },
    )
    CouiSlider(
        value = progress,
        onValueChange = { progress = it },
    )
}

// 需要逐项控制时，仍使用原有高级入口：
CouiSwitch(
    modifier = Modifier,
    checked = checked,
    onCheckedChange = onCheckedChange,
    dark = dark,
    colors = customColors,
    onDragCheckedChange = onDragCheckedChange,
)
~~~

## 组件优先级与建议签名

| 顺序 | 组件 | 便捷层改进 | 当前高级能力 |
| --- | --- | --- | --- |
| 1 | Switch | `checked`、`onCheckedChange` 为必填；`enabled`、`loading` 选填；外观从可选主题读取 | 保留拖动回调、InteractionSource、触觉和颜色 |
| 2 | Slider | `value`、`onValueChange` 为必填；范围、步进、完成回调直接沿用 | 保留精确 Shape、颜色、深浅色、点位控制 |
| 3 | Checkbox | 新增带点击、语义和建议触摸区的便捷重载 | **保留纯绘制的现有 CouiCheckbox**，整行交互继续由宿主拥有 |
| 4 | AlertDialog/InputDialog | 常用标题、正文、确认/取消预设；不接管可见性及输入草稿 | 保留任意内容、三按钮、Material、窗口属性和 TextFieldState |
| 5 | Menu / TopAppBarButton | Style 从上下文取默认；减少重复颜色、形状和文本样式 | 保留泛型 item、子菜单、Popup 属性和 Renderer |
| 6 | FAB / BottomBar / ChipTab | 提供可选的 renderer 装配、外观预设和附件模型 | 保留 Backdrop/Renderer 显式控制，不隐藏 GPU 失败 |

**Checkbox 的语义陷阱**：纯绘制版本用于外层整行 `toggleable` 的场景；带交互版本仅用于自身拥有点击的场景。不得在同一层级重复增加 Toggleable 语义。

**BottomBar 的前置条件**：现有三个主体、左右附件及 renderer 路径有明确装配约束。先做可选附件的数据模型和生命周期测试，不能简单用虚构图标填补现有必填参数，也不能把独立 FAB 当作已有绑定协议。

## Backdrop 与 Renderer 的便捷装配

可以研究 `CouiBackdropHost`（名称暂定），将**背景源节点**与**浮动消费者节点**放进不同插槽。源节点仍只挂一个 `couiBackdrop(backdrop)`，覆盖不透明内容；消费者使用同一个 `backdrop`，但必须在该源节点之外。

- 不允许源节点采样自身或将已绘制浮动层再次作为源。
- Dialog 跨 Window 时继续显式处理采样坐标及已有窗口策略。
- renderer 工厂在 API 34 及以上才调用；Shader 编译失败要暴露给宿主，宿主决定降级 UI。不能假报已成功创建 renderer。
- 显式传入 `CouiBackdrop`、`CouiFloatingRenderer` 的所有旧入口保持可用，L3 效果仍能单独挂载。

## 源码与二进制兼容策略

1. **优先增加重载与 Defaults，不修改现有高级函数的参数顺序、类型和默认值。** 尤其不能直接将现有 CouiCheckbox 改成自动管理点击。
2. 检查 Kotlin 命名参数、Java/JVM 签名、Compose 编译后签名与重载选择。避免 `null`、隐式默认参数造成重载歧义；必要时采用独立便捷名称，而不是强求同名。
3. 新增的外观数据类优先使用 `@Immutable`、稳定值对象或受控 `State`；避免把 renderer、GraphicsLayer、Motion 生命周期塞到颜色模型中。
4. 不从外部 Compose 依赖或主项目偏好中读取隐含设置。单个参数不应同时表示像素和 dp，也不能悄悄更改底层失败行为。
5. 新入口的文档、示例、KDoc、迁移说明与测试同一个 PR 交付。原入口必须有编译与运行回归检查。

## 交付顺序与验收门槛

- **A：外观上下文 + Switch/Slider 便捷重载**。无材质或复杂窗口生命周期，最适合验证 API 模式。
- **B：Checkbox 交互版 + Dialog/Menu 默认 Style**。重点检查语义、键盘、对话框状态 owner 和重载歧义。
- **C：BackdropHost + FAB/BottomBar/ChipTab 简化层**。先完成真实 API 34 设备与 renderer 失败测试，再决定公开签名。

每阶段验收：旧高级调用源码编译、独立 Effect 接口无变化、独立 Maven 消费者构建、对应状态/无障碍测试和至少一轮真机交互。仅凭 Demo 截图或 Lint 通过不能判定完成。

**暂不实施**：模块拆分、KMP、统一重写组件、移植完整厂商主题控制器、统一隐藏渲染失败。
