# 🧭顶栏与按钮

`TopAppBar` 提供标题、导航和操作槽位，并显示渐进模糊背景。需要大标题时使用 `LargeTopAppBar`；需要自行组合布局或顶栏搜索时使用 `TopAppBarSurface`。

## 最小调用

顶栏按钮需要 API 34 和页面背景源。宿主提供按钮内容与操作：

~~~kotlin
@RequiresApi(34)
@Composable
fun ToolbarAction(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onAction: () -> Unit,
    actionContent: @Composable () -> Unit,
) {
    TopAppBarSurface(modifier = modifier, backdrop = backdrop) {
        TopAppBarButtonGroup {
            TopAppBarButton(onClick = onAction) {
                actionContent()
            }
        }
    }
}
~~~

`TopAppBarButton` 默认使用 `CouiTopAppBarButtonDefaults.style()`；`TopAppBarSearchButton` 和 `TopAppBarSearch` 默认使用 `CouiTopAppBarButtonDefaults.style(search = true)`。显式传入 `style` 时覆盖默认值。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个简单顶栏

下面的顶栏显示标题和一个文字按钮。按钮需要 API 34；放入 COUI Scaffold 的 `topBar` 时可省略 `backdrop`。本例通过参数接收源，适用于第三方容器，接入方式见[快速开始](../guide/getting-started.md#浮动组件的背景源)。

~~~kotlin
@RequiresApi(34)
@Composable
fun PageToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle,
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        backdrop = backdrop,
        title = { Text(title) },
        actions = {
            TopAppBarButtonGroup {
                TopAppBarButton(onClick = onAction, text = true, style = style) {
                    Text(actionLabel)
                }
            }
        },
    )
}
~~~

把顶栏放入 Scaffold 的 `topBar`，并将正文应用 Scaffold 返回的 Padding。直接叠放时，由页面预留顶栏的实际高度。此例显式使用按钮 `style`；也可以省略它以使用默认样式。示例假定宿主已接入可选 M3 适配，Material3 `Text` 自动继承组件内容色与文字样式，项目仍需声明 Material3 依赖。

`navigationIcon` 放置返回等导航操作，`actions` 是 RowScope 槽位。`CouiTopAppBarDefaults.colors()` 分别配置背景、标题、导航和操作颜色；默认读取当前主题。标题字体由顶栏提供。纯 Compose 标题使用 `BasicText(title, style = LocalCouiTextStyle.current)`，其中 `BasicText` 来自 `androidx.compose.foundation.text`，`LocalCouiTextStyle` 来自 `com.itos.coui.theme`。

普通大标题页面的滚动装配见[折叠顶栏](collapsing-app-bar.md#普通大标题)。

## 配置顶栏样式

顶栏按钮使用 `CouiToolbarStyle` 设置材质、文字和光标。下面根据页面的颜色创建一份样式：

~~~kotlin
@Composable
fun rememberToolbarStyle(
    dark: Boolean,
    accent: Color,
    contentColor: Color,
    textStyle: TextStyle,
): CouiToolbarStyle {
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    return remember(dark, accent, contentColor, textStyle, density, contour) {
        CouiToolbarStyle(
            dark = dark,
            circleShape = CircleShape,
            capsuleShape = CouiCapsuleShape,
            material = { capsule -> CouiTopAppBarButtonDefaults.material(dark, capsule) },
            shadow = { capsule ->
                CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour)
            },
            contentColor = contentColor,
            textStyle = textStyle,
            cursorColor = accent,
            contourGlowEnabled = contour,
        )
    }
}
~~~

`circleShape` 用于图标按钮，`capsuleShape` 用于文字按钮和搜索框。搜索框的材质配置见[顶栏搜索](search.md#搜索框样式)。

`contourGlowEnabled` 与阴影工厂应使用同一个轮廓状态。

## 添加操作按钮

默认按钮适合放图标；设置 `text = true` 可以显示文字按钮：

~~~kotlin
TopAppBarButton(onClick = onSettings, style = style) {
    Icon(Icons.Default.Settings, contentDescription = settingsLabel)
}

TopAppBarButton(onClick = onReset, text = true, enabled = canReset, style = style) {
    Text(resetLabel)
}
~~~

`enabled = false` 会禁用按钮，但不会隐藏它。可选 M3 适配为内容插槽提供 M3 Local；子内容显式颜色或内层 Local 优先。未接入适配的纯 native 核心需自行桥接 M3 插槽，见[外观指南](../guide/appearance.md#内容插槽与最小交互尺寸)。Material 图标、`Icon` 和 `Text` 需要由应用声明相应依赖。

## 显示和隐藏操作

将操作放进 `TopAppBarButtonGroup`。需要显隐动画时，再用 `TopAppBarActionVisibility` 包住按钮：

~~~kotlin
TopAppBarButtonGroup {
    TopAppBarActionVisibility(visible = actionsVisible) {
        TopAppBarButton(onClick = onSettings, style = style) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel)
        }
    }
}
~~~

## 搜索

搜索按钮和输入框必须放在同一个顶栏里，并共享一个 `CouiTopAppBarSearchState`。默认搜索样式由 `CouiTopAppBarButtonDefaults.style(search = true)` 提供；需要覆盖时显式传入 `style`。展开、关闭、返回键和常驻动作见[顶栏搜索](search.md)。

## 版本和 inset

顶栏容器支持库的最低系统版本，顶栏按钮和搜索需要 API 34。按钮组放在完整顶栏的导航或操作槽位中，或放在 `TopAppBarSurface` 的自定义内容中。组合搜索使用 `TopAppBarSurface`。

每个按钮默认创建独立的 renderer。需要处理创建失败时，可以先创建 `CouiFloatingRenderer.standalone()`，再通过 `renderer` 传入。

M3 模式的 `TopAppBarButton` 使用 `minimumInteractiveComponentSize()`，遵守宿主最小交互尺寸设置。SearchButton 使用固定容器。完整顶栏提供标题字体；自定义 Surface 中的 title 与 buttonGroup 沿用内容自身的文字样式。

`TopAppBar` 和 `LargeTopAppBar` 默认处理顶部及横向系统安全区，可通过 `windowInsets` 调整。父级已消费的 inset 不重复添加。`TopAppBarSurface` 只提供背景，不消费 inset；自定义内容需自行处理一次安全区。渐进模糊背景会向下延伸，但不会增加布局高度或触摸范围。

COUI Scaffold 的 `topBar` 自动提供正文背景源，`TopAppBar`、`LargeTopAppBar` 和 `TopAppBarSurface` 可省略 `backdrop`，显式参数优先。第三方容器使用 `rememberCouiBackdrop()` 创建源，在现有正文容器上添加 `.couiBackdrop(backdrop)`，再将源显式传给顶栏。不需要额外的背景容器或全局初始化；源节点不能包含顶栏。完整装配与高级作用域见[背景采样](../effects/backdrop.md#顶栏接入)。

源码见 [TopAppBar / LargeTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBar.kt)、[TopAppBarSurface](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarSurface.kt)、[按钮](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButton.kt)、[按钮组与显隐](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButtonGroup.kt)、[按钮预设](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。

## 背景参数

以下参数属于 `TopAppBarSurface`。`backgroundAlpha` 只控制背景透明度。`gradientColor` 的 alpha 与渐变曲线及背景图层透明度相乘；`dark` 决定背景渐变的深浅色分支，默认读取 `CouiTheme.isDark`。

`topExtension` 返回背景向上延伸的 dp 距离，在测量阶段读取；它增加背景高度并将背景向上平移，不改变内容占位或命中区域。折叠顶栏用它保持完整背景随顶栏移动，使渐进曲线与采样尺寸稳定。`bottomExtension` 只增加向下延伸的范围。

API 33 起，`gradientSpeed = null` 使用固定长度渐隐，正值选择全高幂曲线。`referenceBlurRadius` 按 dp 解释；需要固定引擎像素半径时，在消费处按当前 density 换算。API 31–32 的平台分层路径仍使用自身固定模糊参数。

需要大标题与常驻搜索联动时，使用[折叠顶栏](collapsing-app-bar.md)。
