# ➕FAB

`Fab` 适合放置新建、编辑等页面操作，可以单独使用，也可以与底栏一起使用。

## 最小调用

FAB 需要 API 34、页面背景源和单独的 standalone renderer。放入 COUI Scaffold 的 `floatingActionButton` 时可省略 `backdrop`，容器自动提供正文源；显式参数优先。下面示例显式接收背景源，适用于第三方容器。页面提供内容，需要业务操作时再传入点击回调：

~~~kotlin
@RequiresApi(34)
@Composable
fun PageFab(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer.standalone() }
    Fab(modifier = modifier, backdrop = backdrop, renderer = renderer, onClick = onClick, content = content)
}
~~~

省略 material 时，底色取 COUI 强调色，内容色取 `onAccent`；省略 shadow 时，阴影颜色从最终 `material.firstColor` 取得。显式 material 使用宿主 ambient 内容色，子内容的显式颜色优先。FAB 不使用页面 `themeTint`。通用主题规则见[外观与预设](../guide/appearance.md)；强调色生成策略（包括 HCT）由宿主负责。

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
    val renderer = remember { CouiFloatingRenderer.standalone() }
    val color = CouiFabDefaults.containerColor(preset, dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    Fab(
        modifier = modifier,
        backdrop = backdrop,
        renderer = renderer,
        onClick = onClick,
        dark = dark,
        shape = CouiShape(999.dp, cornerWeight = 2f),
        material = CouiFabDefaults.material(color, dark),
        shadow = CouiFabDefaults.shadow(color, density, contour),
        content = content,
    )
}
~~~

`preset` 可以选择 `Notes`、`Phone`、`InfraredRemote` 或 `Recorder`。按钮内容通过 `content` 传入，点击时调用 `onClick`。省略回调时保留展示和拖拽反馈，不添加点击或 Button 语义。

拖拽超过系统触摸阈值后，FAB 接管移动事件，避免带动父级滚动；轻触仍交给已提供的点击回调。

COUI Scaffold 的 `floatingActionButton` 已应用正文占位 Padding 并消费 Insets。槽位不添加默认外边距，调用方使用 `Modifier.align(Alignment.BottomEnd).padding(24.dp)` 定位；也可组合多个 FAB，每个按钮使用独立 renderer，见[页面容器](scaffold.md#放置-fab)。第三方容器的位置和安全区由页面控制。父容器需要为投影留出空间。

## 使用自定义底色

需要使用自己的颜色时，将同一个颜色传给材质和阴影：

~~~kotlin
val color = Color(0xFF0080FF)
val renderer = remember { CouiFloatingRenderer.standalone() }
val density = LocalDensity.current.density
val contour = LocalCouiContourGlowEnabled.current

Fab(
    backdrop = backdrop,
    renderer = renderer,
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

启用 M3 适配后，省略材质的 FAB 会向 M3 图标提供主题 `onAccent`。显式材质保持 ambient 内容色；预设或自定义底色可显式设置 `tint`。示例使用 `CouiShape` 创建圆形，也可以传入自己的 Shape。

有色预设见 `CouiFabColor` 和 `CouiFabDefaults`。

轮廓光由 `LocalCouiContourGlowEnabled` 控制；关闭后，`CouiFabDefaults.shadow` 使用普通有色投影。

## 版本和绘制空间

每个 FAB 使用独立的 `CouiFloatingRenderer.standalone()`，不要与其他绘制节点共用。

COUI Scaffold 自动将 FAB 放在正文源之外。第三方容器的 `backdrop` 应覆盖不透明的页面内容，FAB 放在源节点之外。背景源的创建方式见[背景采样](../effects/backdrop.md)。

源码见 [Fab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)、[配色](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)。Demo 的固定位置装配见 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt) 和 [DemoSecondaryPage](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)。
