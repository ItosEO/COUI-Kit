# 背景采样

`CouiBackdrop` 用于录制指定布局节点的内容作为可采样背景源，供兄弟面板、顶栏、底栏或独立窗口浮层在其上方执行模糊、材质混色或浮动表面绘制。

- **平台门槛**：基础背景录制与均匀模糊（`rememberCouiBackdrop`、`couiBackdrop`、`drawCouiBackdrop`、`CouiBackdropEffectScope.blur`）支持库最低系统版本（**API 31+**）；叠加 `RuntimeShader` 材质、渐进模糊或浮动表面时受对应效果的 **API 33+** / **API 34+** 门槛约束。
- **公开入口**：
  - **录制端**：`rememberCouiBackdrop`、`Modifier.couiBackdrop`（挂在被录制的内容节点上）。
  - **消费端（便捷 Modifier）**：`Modifier.drawCouiBackdrop`（在 `CouiBackdropEffectScope` 中配置滤镜）。
  - **消费端（底层绘制）**：`CouiBackdrop.draw(scope, target)`（在自定义 `DrawScope` 中直接采样）。
  - **作用域与窗口源**：`ProvideCouiBackdrop`、`ProvideCouiMenuBackdrop`、`rememberCouiWindowBackdrop`。

## 一个源与一个面板

下面示例把 `background` 内容作为不透明采样源，`foreground` 作为模糊面板上的清晰内容；`backgroundColor`、`background` 与 `foreground` 均由宿主提供，示例尺寸与模糊半径仅用于演示装配：

~~~kotlin
@Composable
fun BlurStage(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    background: @Composable BoxScope.() -> Unit,
    foreground: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(
        modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .couiBackdrop(backdrop)
                .background(backgroundColor),
            content = background,
        )
        Box(
            Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
                .height(112.dp)
                .drawCouiBackdrop(
                    backdrop = backdrop,
                    shape = { CouiShape(24.dp) },
                    effects = { blur(16.dp.toPx()) },
                ),
            content = foreground,
        )
    }
}
~~~

接入时注意以下四点：

1. **源必须提供真实不透明底色**：`backgroundColor` 需保持不透明，`background` 绘制实际页面或图案内容；透明窗口不会变成屏幕截图源。
2. **严禁自采样**：源节点（`couiBackdrop`）与消费者（`drawCouiBackdrop`、顶栏、底栏或浮层）必须是**兄弟节点或外层分离关系**，切勿把消费者放进它所采样的源子树内部。
3. **单位与裁剪分工**：`CouiBackdropEffectScope.blur` 接收**像素（px）**，`CouiBackdropEffectScope` 实现了 `Density`，可在 `effects` 内直接调用 `16.dp.toPx()`。`shape` 同时裁剪采样背景、`onDrawSurface` 与子内容，但 `effects` 滤镜只作用于采样背景层，前景内容保持清晰。
4. **`padding` 的位置差异**：在 `drawCouiBackdrop` **外侧**添加 `padding` 会缩小采样和裁剪区域；写在**内侧**时，留白属于面板内部。

## 顶栏接入

| 宿主容器 | 背景源装配方式 | 注意事项 |
| --- | --- | --- |
| COUI `Scaffold` | 容器自动录制完整正文视口与 `containerColor`，`topBar`、`bottomBar` 与 `floatingActionButton` 槽位自动获得默认源，无需创建或传参 | 正文子树不会获得自身源，避免自采样；正文内部效果需独立装配 |
| Material 3 等第三方容器 | 调用 `rememberCouiBackdrop()` 创建源，把 `.couiBackdrop(backdrop)` 挂到现有正文容器，再显式传给 `TopAppBar(backdrop = backdrop, ...)` | 源只挂到一个正文节点，顶栏必须位于源节点之外 |

在 Material 3 `Scaffold` 中接入顶栏时（`backgroundColor`、`title` 与正文 `content` 由宿主提供）：

~~~kotlin
@Composable
fun MaterialScaffoldWithCouiTopBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                backdrop = backdrop,
                title = { BasicText(title, style = LocalCouiTextStyle.current) },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .couiBackdrop(backdrop)
                .background(backgroundColor)
                .padding(padding)
                .consumeWindowInsets(padding),
            content = content,
        )
    }
}
~~~

无需额外的背景容器或全局初始化；图层的创建、更新与释放由库自动处理。其他第三方容器或自定义页面结构的接入方式见[宿主页面接入](../guide/secondary-pages.md)。

## 源、作用域与跨窗口规则

| 入口 | 作用域与行为 | 使用约束 |
| --- | --- | --- |
| `rememberCouiBackdrop(parent, diagnosticName, drawSource)` | 创建并记住一个背景录制源；`parent` 用于叠加所属父窗口的源，`drawSource` 默认为 `{ drawContent() }` | 每个 `CouiBackdrop` 只挂到一个内容节点；自定义 `drawSource` 时必须绘制真实内容，不能只录制补底 |
| `ProvideCouiBackdrop(backdrop)` | 向子树提供已有 `CouiBackdrop`；`TopAppBar`、`LargeTopAppBar`、`TopAppBarSurface`、`CollapsingTopAppBar`、`NavigationBar`、`Fab`、`SearchBar`、`ChipTabRow` 会读取最近作用域 | 只提供引用，不创建或挂载节点；显式 `backdrop` 参数优先。未传参且无作用域时会抛异常；不要向源自己的内容子树提供它自身 |
| `ProvideCouiMenuBackdrop(isolateContent, content)` / `rememberCouiWindowBackdrop()` | 录制当前窗口内容供[菜单](../components/menu.md)（`Popup`）、材质[弹窗](../components/dialog.md)或 [Sheet 面板](../components/bottom-sheet.md)采样 | `Popup` 与独立窗口自身不进入该采样树；页面源不会从窗口源隐式获取，COUI `Scaffold` 也不会自动安装窗口源 |

坐标系与窗口嵌套契约：

- **坐标系转换责任**：同窗口内通过 `LayoutCoordinates` 进行二维仿射映射；跨窗口（`Dialog`、`Popup`）由消费者在调用 `backdrop.draw(scope, target)` 时通过 `localToScreen` / `screenToLocal` 自动换算到目标节点本地坐标（当前支持二维仿射，不支持三维翻转）。
- **材质弹窗采样边界**：材质弹窗必须采样宿主窗口里**不包含弹窗自身**的背景源，不能在 `Dialog` 内部新建源来代替宿主背景（装配见[弹窗](../components/dialog.md#使用背景材质)）。若弹窗内容上还要再弹菜单或其他浮层，再为该弹窗内容另挂窗口源。

## 菜单窗口的离屏策略

普通 `rememberCouiBackdrop` 与单参数 `ProvideCouiMenuBackdrop(content)` 始终保持内容离屏隔离。只有显式传入 `ProvideCouiMenuBackdrop(isolateContent = false, content = ...)` 时，窗口源才会在没有材质消费者时采用 `CompositingStrategy.Auto`（接入片段见[菜单](../components/menu.md#空闲时按需离屏)）。

| 阶段 | `isolateContent = true`（默认） | `isolateContent = false` |
| --- | --- | --- |
| 空闲（无材质消费者） | `CompositingStrategy.Offscreen` | `CompositingStrategy.Auto` |
| 菜单 / 材质弹窗 / 材质 Sheet 消费期间（含退场动画） | `CompositingStrategy.Offscreen`，保留该源及全部 `parent` 源 | 自动恢复 `CompositingStrategy.Offscreen`，保留该源及全部 `parent` 源 |
| 最后一个消费者结束后 | 保持 `Offscreen` | 恢复 `CompositingStrategy.Auto` |

> [!NOTE]
> `isolateContent = false` 期间，源节点、布局坐标、裁剪、绘制指令录制与跨窗口更新均保持运行；它**不是**暂停采样或缓存旧帧的开关。

选择 `false` 前，宿主须确认以下三项条件：

1. **目标混合自隔离**：内容中的 `Clear`、`DstIn`、`DstOut` 等目标混合必须在所属组件内部完成隔离，不能依赖外层窗口源提供离屏隔离。
2. **优先用于静态不透明根**：优先用于不透明、静态的页面根节点。取消强制离屏后，文字与矢量可能在最终变换下直接栅格化，在分数位移或缩放时可能与离屏纹理产生不同的抗锯齿结果。
3. **特殊容器须实测**：透明窗口、移动中的面板以及存在额外变换的容器，需单独核对颜色、覆盖率与动画中段；未核对时保持默认 `true`。`Auto` 由 Android / Compose 决定实际合成方式，其他效果仍可能触发离屏，且该选项不改变模糊算法、菜单自身隔离或平台门槛。

## 资源与失败处理

- **内部图层与可变 `renderer` 隔离**：`drawCouiBackdrop` 逐节点创建、更新和释放内部 `GraphicsLayer`、效果 scope 与目标坐标，返回的 `Modifier` 可复用到多个面板；但自定义回调捕获的外部可变 `renderer` 仍必须由调用方逐节点独立创建与管理，**禁止借复用 `Modifier` 共享可变绘制资源**。
- **渲染器选型**：多主体浮动表面选型见[选择主体数量](floating-surfaces.md#选择主体数量)；同色融合背景与遮罩见[内容遮罩](metaball.md#内容遮罩)。
- **平台门槛与异常捕获**：宿主在创建底层 `Shader` / `Renderer` 前须先检查系统 API 版本。需要展示降级或错误 UI 时，在 `remember { runCatching { ... } }` 工厂中捕获构造结果并按结果分支渲染，**不要把 Compose `@Composable` 内容放入普通 `try/catch`**；捕获时保留原因，协程取消（`CancellationException`）必须继续传播。
- **状态更新与快照失效**：主题、几何与位置变化后应让当前状态流入绘制，不永久缓存旧背景；已有 `RenderEffect` 保存的是 uniform 快照，低层自定义装配在参数变更时需重建相应快照。

## 公开入口

- **背景录制与提供**：[CouiBackdrop.kt](../../coui/src/main/java/com/itos/coui/backdrop/CouiBackdrop.kt)、[ProvideCouiBackdrop.kt](../../coui/src/main/java/com/itos/coui/backdrop/ProvideCouiBackdrop.kt)
- **便捷采样绘制**：[DrawCouiBackdrop.kt](../../coui/src/main/java/com/itos/coui/backdrop/DrawCouiBackdrop.kt)
- **窗口级菜单背景源**：[CouiMenuBackdrop.kt](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBackdrop.kt)
- **完整采样舞台示例**：[DemoVisualEffects.kt](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)
