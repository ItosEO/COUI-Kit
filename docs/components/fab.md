# ➕FAB

`Fab` 适合放置新建、编辑等页面操作，可以单独使用，也可以与底栏一起使用。

## 最小调用

FAB 需要 API 34 和页面背景源。放入 COUI Scaffold 的 `floatingActionButton` 时可省略 `backdrop`，容器自动提供正文源。显式参数优先。下面示例适用于第三方容器，页面提供背景源、按钮内容和点击回调。

~~~kotlin
@RequiresApi(34)
@Composable
fun PageFab(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Fab(modifier = modifier, backdrop = backdrop, onClick = onClick, content = content)
}
~~~

省略 material 时，底色取 COUI 强调色；省略 shadow 时，阴影颜色从最终 `material.firstColor` 取得。内容色在日间和夜间默认均为不透明白色，也适用于显式 material。可用 `contentColor` 覆盖，子内容的显式颜色优先。FAB 不使用页面 `themeTint`。通用主题规则见[外观与预设](../guide/appearance.md)；强调色生成策略（包括 HCT）由宿主负责。

## 使用显式配色预设

FAB 需要 API 34 和页面背景源。下面使用内置配色创建一个按钮：

~~~kotlin
@RequiresApi(34)
@Composable
fun PresetFab(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    preset: CouiFabColor,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = CouiFabDefaults.containerColor(preset, dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        dark = dark,
        shape = CouiShape(999.dp, cornerWeight = 2f),
        material = CouiFabDefaults.material(color, dark),
        shadow = CouiFabDefaults.shadow(color, density, contour),
        content = content,
    )
}
~~~

`preset` 可以选择 `Notes`、`Phone`、`InfraredRemote` 或 `Recorder`。按钮内容通过 `content` 传入，点击时调用必传的 `onClick`。仅展示交互效果时可传入 `onClick = {}`，按钮仍保留点击、焦点和无障碍语义。

拖拽超过系统触摸阈值后，FAB 接管移动事件，避免带动父级滚动；轻触仍交给已提供的点击回调。

COUI Scaffold 的 `floatingActionButton` 已应用正文占位 Padding 并消费 Insets。槽位不添加默认外边距，调用方使用 `Modifier.align(Alignment.BottomEnd).padding(24.dp)` 定位；也可组合多个 FAB，每个按钮使用独立 renderer，见[页面容器](scaffold.md#放置-fab)。第三方容器的位置和安全区由页面控制。父容器需要为投影留出空间。

## 使用自定义底色

需要使用自己的颜色时，将同一个颜色传给材质和阴影：

~~~kotlin
val color = Color(0xFF0080FF)
val density = LocalDensity.current.density
val contour = LocalCouiContourGlowEnabled.current

Fab(
    backdrop = backdrop,
    onClick = onClick,
    dark = dark,
    shape = CouiShape(999.dp, cornerWeight = 2f),
    material = CouiFabDefaults.material(color, dark),
    shadow = CouiFabDefaults.shadow(color, density, contour),
    content = content,
)
~~~

这段代码位于宿主 Composable 中；`backdrop`、`onClick` 和 `content` 由页面提供。

## 图标和轮廓光

启用 M3 适配后，FAB 会向 M3 `Icon` 提供内容色，图标可省略 `tint`。要覆盖整个内容槽，可传入 `contentColor = Color.Black` 等颜色；`contentColor = Color.Unspecified` 表示继承外部内容色。子图标显式指定 `tint` 时优先使用该值，`Image` 不会自动读取内容色。

轮廓光由 `LocalCouiContourGlowEnabled` 控制；关闭后，`CouiFabDefaults.shadow` 使用普通有色投影。

## 圆角方形

`Fab` 默认使用 `CouiFabDefaults.circleShape`。需要圆角方形时，在原调用中传入形状预设：

~~~kotlin
Fab(
    backdrop = backdrop,
    onClick = onClick,
    shape = CouiFabDefaults.roundedSquareShape,
    content = content,
)
~~~

这段代码位于 API 34 及以上的宿主 Composable 中，各参数沿用最小调用的来源。用 `CouiShape(20.dp)` 可自定义圆角；用 `Modifier.size(width, height)` 可改为圆角矩形。

`CouiShape` 的半径和权重同时用于内容裁剪、材质与轮廓光。半径按实际短边的一半限制，普通投影使用同半径的 RoundRect caster。该投影沿用不透明主体的绘制方式，不裁掉主体内部的阴影；自定义半透明材质会透出内部阴影。其他 Shape 只控制内容裁剪，材质仍使用圆形或胶囊几何。

## 版本和绘制空间

每个 FAB 默认创建独立的单主体绘制器。宿主需要处理 Shader 初始化失败时，可以先用 `CouiFloatingRenderer.standalone()` 创建绘制器，成功后通过 `renderer` 传入 FAB。每个 FAB 各用一个实例。

组合相邻 FAB 时，传入 `foregroundComposition = true`。材质和外光晕将以透明前景叠加，拖拽形变时不会用矩形背景回填覆盖相邻按钮。此模式要求背景源覆盖不透明 SDR 页面，并排除所有 FAB；各按钮仍保留自己的 renderer、输入和动画。重叠区域的光效不包含兄弟前景采样，合成边界见[浮动表面](../effects/floating-surfaces.md#几何与绘制)。

COUI Scaffold 自动将 FAB 放在正文源之外。第三方容器的 `backdrop` 应覆盖不透明的页面内容，FAB 放在源节点之外。背景源的创建方式见[背景采样](../effects/backdrop.md)。

源码见 [Fab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)、[配色](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)。Demo 的固定位置装配见 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt) 和 [DemoSecondaryPage](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)。
