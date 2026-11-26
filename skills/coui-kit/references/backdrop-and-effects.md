# 背景采样、材质效果与交互

## 背景采样

| 源类型 | 创建与挂载 | 消费者 | 核心约束 |
| --- | --- | --- | --- |
| **正文源** | COUI `Scaffold` 自动提供；第三方容器挂 `rememberCouiBackdrop()` | 顶栏、底栏、`Fab`、搜索、输入栏 | 挂在铺满视口、含**不透明底色**且在滚动 padding 之前的正文节点（见[顶栏接线](bars-and-scaffold.md#topappbar-与-topappbarsurface)） |
| **窗口源** | 根部 `ProvideCouiMenuBackdrop`（`Scaffold` 不提供） | `Menu`、`Dialog`、`BottomSheet`（`rememberCouiWindowBackdrop()`） | 根部挂一次；浮层在独立窗口绘制 |

### 窗口源

```kotlin
ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) { pageContent() }
}
```

- 仅静态、全屏、不透明且自身不做缩放/透明度转场的主根，可用 `ProvideCouiMenuBackdrop(isolateContent = false)` 降低空闲离屏开销（子树 `Clear`/`DstIn` 需自行隔离）。弹窗与面板保持默认 `isolateContent = true`。

### 避坑

- **禁止自采样**：消费者不可在自身源子树内。正文内放 `ChipTabRow`、透明 `SegmentedControl` 或自绘模糊，须另建**兄弟节点**和独立源。
- **单节点专源**：一个 `CouiBackdrop` 只挂一节点，禁止跨页/跨窗复用（勿把主 Activity 源传给 Dialog）。
- **`ProvideCouiBackdrop` 不录制**：仅传已有源。缺不透明底色会导致模糊发黑或断层。

## 模糊与材质

优先用组件预设；渐进模糊用 `TopAppBar`/`TopAppBarSurface`。自绘面板将录制与显示节点设为兄弟关系，两层混色（API 33+）用 `rememberCouiMaterialEffect`：

```kotlin
val backdrop = rememberCouiBackdrop()
Box(Modifier.fillMaxSize()) {
    Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(pageColor)) { backgroundContent() }
    Box(Modifier.drawCouiBackdrop(backdrop, shape = { panelShape }, effects = { blur(radiusPx) })) { panelContent() }
}
```

## 阴影与浮动表面

- **普通阴影（API 34+）**：`.couiShadow(...)` 放 `.background()` 和 `.clip()` **之前**；父级留出绘制余量。`CouiShadowOutline.RoundRect` 禁传 `clipping = true`（直接抛 `IllegalArgumentException`；仅 `Oval`/`SmoothCapsule` 支持）：

```kotlin
Box(Modifier.size(160.dp, 56.dp)
    .couiShadow(CouiShadowDefaults.menu(CouiTheme.isDark), CouiShadowOutline.SmoothCapsule, radius = 28.dp)
    .background(surfaceColor, CouiCapsuleShape))
```

- **浮动表面（API 34+）**：优先用 `Fab`、`NavigationBar`、`SearchBar`。改色须同步更新 `material`、`shadow` 与 `LocalCouiContourGlowEnabled.current`。自绘单主体用 `CouiFloatingRenderer.standalone()` + `couiFloatingSurface`，双/三主体用 `CouiFloatingRenderer()` + `couiFloatingPair`/`couiFloatingSurfaces`；两类不可混用，逐节点独立创建。

## 点击反馈、回弹与融合

- **自定义行点击**：`.couiButtonFeedback` 与 `.clickable(source, indication = null, ...)` 共用同一 `MutableInteractionSource`；菜单展开持续高亮行时传 `hoverLocked = expanded`：

```kotlin
val source = remember { MutableInteractionSource() }
Modifier.couiButtonFeedback(CouiTheme.isDark, source, enabled, list = true, hoverLocked = expanded)
    .clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onOpen)
```

- **缩放显隐**：用 `rememberCouiScaleVisibility(visible)`，在 `isPresent` 期间保留内容并挂 `Modifier.couiScaleVisibility(motion)`。自绘链读取 `scale.value` 和 `alpha.value`，并将 `foregroundBlurEffect` 赋给前景图层。宿主按 `visible` 控制交互。
- **纵向回弹**：普通 `Column` 用 `.couiVerticalScroll(rememberScrollState())` 且 `.padding(padding)` 放滚动**之后**；`LazyColumn` 传独立 `overscrollEffect = rememberCouiOverscrollEffect()`。禁止跨列表共享、双层纵向滚动，或用于横向分页/侧滑。
- **横向融合（API 33+）**：同坐标系准备两个 `CouiMetaballBody`，背景与 `BlendMode.DstIn` 内容遮罩各自持有独立 `CouiMetaballRenderer`。跟随位移用 `rememberCouiMetaballFollowingTranslation`（像素），业务触发在松手完成时决定。
