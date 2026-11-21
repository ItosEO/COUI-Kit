# 顶栏与按钮

`TopAppBar` 提供标题、导航与操作槽位并自带渐进模糊背景；`TopAppBarSurface` 提供可自由排版的背景容器；`TopAppBarButton` 提供带光效与物理按压反馈的浮动操作按钮。大标题折叠请使用[折叠顶栏](collapsing-app-bar.md)。

- **顶栏容器（`TopAppBar`、`TopAppBarSurface`）**：支持库最低版本（**API 31+**）；API 33+ 使用 `RuntimeShader` 渐进模糊，API 31–32 使用分层覆盖率模糊。
- **顶栏按钮与搜索（`TopAppBarButton`、`TopAppBarSearch`）**：要求 **API 34+**。
- **背景源要求**：在 COUI `Scaffold` 的 `topBar` 槽位中可省略 `backdrop`；在第三方容器中使用时需显式传入不包含顶栏自身的 `backdrop`。

## 显示基础顶栏

下面的示例展示带标题和右侧文字按钮的 `TopAppBar`。当顶栏放在 COUI `Scaffold` 的 `topBar` 中时可省略 `backdrop`；在 Material 3 等第三方容器中需通过参数传入正文背景源（参见[快速开始](../guide/getting-started.md#浮动组件的背景源)）：

~~~kotlin
@RequiresApi(34)
@Composable
fun PageToolbarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(),
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

接入说明：

- **槽位与排版职责**：`navigationIcon` 放置返回等导航按钮，`actions` 位于 `RowScope` 中。`TopAppBar` 会向 `title` 提供 `18.sp`、基础字重 `700` 与 `sans-serif-semibold` 排版（与带导航按钮的 `LargeTopAppBar` 收起时一致，均跟随系统字体大小与粗体设置）；纯 Compose 文本请写为 `BasicText(title, style = LocalCouiTextStyle.current)`，自定义 `TopAppBarSurface` 可调用 `CouiTopAppBarDefaults.titleTextStyle()` 复用这套排版。
- **按钮默认值**：`TopAppBarButton` 默认使用 `CouiTopAppBarButtonDefaults.style()`，搜索使用 `style(search = true)`。默认材质取顶栏背景并执行两层混色，附加模糊半径为零。显式传入 `style` 可覆盖默认样式。
- **背景遮罩配色**：`CouiTopAppBarDefaults.colors(dark = dark)` 控制顶部渐变遮罩、标题、导航与操作颜色。默认在浅色模式使用白色、深色模式使用黑色向下渐隐；传入 `backgroundColor = Color.Transparent` 可关闭遮罩并仅保留模糊。
- API 33+ 的完整顶栏按设备像素应用全高渐进模糊。直接使用 `TopAppBarSurface` 时，也可以指定参考半径、渐隐长度和曲线。
- **大标题顶栏**：普通大标题页面的滚动折叠装配见[折叠顶栏](collapsing-app-bar.md#普通大标题)。

## 自定义表面与最小按钮（`TopAppBarSurface`）

需要完全自定义内部排版或装配顶栏搜索时，使用 `TopAppBarSurface` 配合 `TopAppBarButtonGroup` 和 `TopAppBarButton`（按钮要求 **API 34+**）：

~~~kotlin
@RequiresApi(34)
@Composable
fun CustomToolbarActionExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onAction: () -> Unit,
    actionContent: @Composable () -> Unit,
) {
    TopAppBarSurface(modifier = modifier, backdrop = backdrop) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            TopAppBarButtonGroup {
                TopAppBarButton(onClick = onAction) {
                    actionContent()
                }
            }
        }
    }
}
~~~

注意：`TopAppBarSurface` 只负责绘制背景和提供顶栏效果作用域，**不会**自动消费安全区 `WindowInsets`，需在内部自行调用 `statusBarsPadding()` 等一次安全区留白。

## 配置顶栏样式

`TopAppBarButton` 与顶栏搜索使用 `CouiToolbarStyle` 统一控制材质、阴影、形状、文字样式和光标颜色。通常只需使用 `CouiTopAppBarButtonDefaults.style()`；需要自定义颜色或字体时按下面方式创建：

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

- `circleShape` 用于圆形图标按钮，`capsuleShape` 用于文字按钮与搜索胶囊；搜索框材质配置见[顶栏搜索](search.md#搜索框样式)。
- `contourGlowEnabled` 与 `shadow` 工厂中的 `contour` 应保持同一个状态，避免轮廓光与阴影策略不一致。
- **`contourStyle`（独立 Drawable 轮廓）**：为 `TopAppBarButton` 按按钮形状（`capsule: Boolean`）指定 `CouiContourStyle`（仅接受非 `Menu` 的独立预设，详见[轮廓光](../effects/lighting.md#独立-drawable-轮廓)）；为 `null` 时沿用浮动材质自带轮廓。非空时轮廓在材质之后、内容之前绘制，同样受 `contourGlowEnabled` 控制（例如 [Sheet 面板](bottom-sheet.md)的顶部按钮按胶囊与圆形分别选用 `Button` 与 `CircleButton` 预设）。

## 添加操作与显隐动画

将操作放进 `TopAppBarButtonGroup`；需要切换操作按钮的显隐并保留外围投影时，用 `TopAppBarActionVisibility` 包裹按钮（按钮要求 **API 34+**）：

~~~kotlin
@RequiresApi(34)
@Composable
fun ToolbarActionsGroupExample(
    modifier: Modifier = Modifier,
    style: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(),
    actionsVisible: Boolean,
    canReset: Boolean,
    settingsLabel: String,
    resetLabel: String,
    onSettings: () -> Unit,
    onReset: () -> Unit,
) {
    TopAppBarButtonGroup(modifier = modifier) {
        TopAppBarActionVisibility(visible = actionsVisible) {
            TopAppBarButton(onClick = onSettings, style = style) {
                Icon(Icons.Default.Settings, contentDescription = settingsLabel)
            }
        }
        TopAppBarButton(onClick = onReset, text = true, enabled = canReset, style = style) {
            Text(resetLabel)
        }
    }
}
~~~

- **图标与文字模式**：`text = false`（默认）渲染 `40.dp` 圆形按钮；`text = true` 渲染最小宽度 `60.dp`、高度 `40.dp` 的胶囊文字按钮。
- **禁用行为**：`enabled = false` 会禁用点击并将内容透明度降至 `0.38f`，不会隐藏按钮。
- **原位淡化 vs 横向收放**：`TopAppBarActionVisibility` 默认 `animateSize = true`（水平展开/收起配合淡入淡出）；设为 `false` 时保留占位并原位淡化。
- **M3 插槽兼容**：在 M3 适配下，槽位会自动提供 Material 3 `LocalContentColor` / `LocalTextStyle`；纯 Compose 核心不附带 Material 3 桥接或图标依赖，详见[外观指南](../guide/appearance.md#内容插槽与最小交互尺寸)。

## 顶栏搜索

需要在顶栏中将搜索按钮展开为输入框时，将 `TopAppBarSearch`、`TopAppBarSearchButton` 与 `TopAppBarSearchAction` 放入同一个顶栏并共享一份 `CouiTopAppBarSearchState`，详见[顶栏搜索](search.md)。

## 背景参数

以下参数属于 `TopAppBarSurface`，用于控制渐进模糊与顶部遮罩效果：

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `backdrop` | `LocalCouiBackdrop.current` | 必填或由 COUI `Scaffold` 自动提供，不能包含顶栏自身 |
| `fadeHeight` | `CouiTopAppBarDefaults.fadeHeight` | 背景向下渐隐的参考高度（默认 `96.dp`） |
| `referenceBlurRadius` | `CouiTopAppBarDefaults.referenceBlurRadius` | 按 `dp` 解释的参考模糊半径（默认 `10.8.dp`）；若需固定引擎像素半径，请按当前 `density` 换算后传入 |
| `bottomExtension` | `CouiTopAppBarDefaults.bottomExtension` | 背景向下额外延伸的距离（默认 `12.dp`），不改变布局高度与触摸范围 |
| `topExtension` | `{ 0.dp }` | 在测量阶段读取的向上延伸距离；会将背景增加对应高度并向上平移，不改变内容占位与命中区域，常用于折叠顶栏保持渐隐曲线稳定 |
| `backgroundAlpha` | `1f` | 同时控制模糊背景与遮罩的整体透明度（`0f..1f`），不影响标题和按钮 |
| `gradientColor` | `Color.Unspecified` | 未指定时浅色用白色、深色用黑色向下渐隐；传入显式颜色会覆盖默认遮罩（其 `alpha` 与渐变曲线及 `backgroundAlpha` 相乘）；传入 `Color.Transparent` 关闭遮罩并保留模糊 |
| `gradientSpeed` | `null` | API 33+ 有效：`null` 使用固定长度渐隐，正值启用全高幂曲线；API 31–32 使用分层平台模糊固定参数 |
| `dark` | `CouiTheme.isDark` | 决定背景渐变的深浅色分支 |

## 参数与行为速查

| 组件 | 关键参数 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `TopAppBar` | `colors` | `CouiTopAppBarDefaults.colors(dark = dark)` | 配置背景遮罩、标题、导航及操作区颜色 |
| `TopAppBar` | `windowInsets` | `CouiTopAppBarDefaults.windowInsets` | 默认消费顶部系统栏与屏幕缺口及横向安全区 |
| `TopAppBarButton` | `renderer` | `remember { CouiFloatingRenderer.standalone() }` | 每个按钮需独立 `renderer`；如需捕获创建失败，可自行创建后传入 |
| `TopAppBarButton` | `text` | `false` | `false` 为 `40.dp` 圆形图标按钮，`true` 为自适应宽度胶囊文字按钮 |
| `TopAppBarActionVisibility` | `durationMillis` / `animateSize` | `280` / `true` | 控制淡化时长及是否伴随横向宽度收放动画 |

## 装配注意事项

- **不要重复添加顶部安全区**：`TopAppBar` 与 `LargeTopAppBar` 已消费 `windowInsets` 指定的顶部与横向安全区，父级已消费部分会自动扣除；只有 `TopAppBarSurface` 需自行在内容上处理一次安全区。
- **触摸拦截与延伸绘制边界**：顶栏的布局区域（包括空白处）会拦截发往下层正文的触摸事件；背景的 `bottomExtension` / `topExtension` 只扩展绘制区域，不会增大顶栏布局高度，也不会拦截布局之外正文的点击事件。
- **背景源不能包含顶栏自身**：放在 COUI `Scaffold` 的 `topBar` 中时可省略 `backdrop`；在第三方容器中请用 `rememberCouiBackdrop()` 并在正文容器挂载 `.couiBackdrop(backdrop)`，不要把顶栏放在源节点内部，详见[背景采样](../effects/backdrop.md#顶栏接入)。
- **ButtonGroup 必须位于顶栏作用域内**：`TopAppBarButton` 与 `TopAppBarButtonGroup` 会读取顶栏提供的内部作用域，不能脱离 `TopAppBar`、`LargeTopAppBar` 或 `TopAppBarSurface` 单独放置。

## 源码与完整示例

- **公开组件入口**：[TopAppBar.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBar.kt)、[TopAppBarSurface.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarSurface.kt)
- **按钮、分组与预设**：[TopAppBarButton.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButton.kt)、[TopAppBarButtonGroup.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButtonGroup.kt)、[CouiTopAppBarButtonDefaults.kt](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)
- **Demo 完整页面**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
