# 分段选择器

`SegmentedControl` 在一个胶囊容器内选择项目，支持点按、拖动和指示器回弹。它适合日/周/月、内容范围等少量互斥项目，要求 Android 14（API 34）。

## 选择外观

| 放置位置 | 推荐用法 |
| --- | --- |
| Group 内 | 省略 `backdrop`，使用普通填色，不采样背景，也不创建模糊材质 |
| 直接放在页面背景上，或位于多色图案之上 | 传入 `backdrop`，使用透明模糊材质 |

普通填色常称为“不透明模式”，指它不使用背景采样；填充颜色仍按预设保留透明度。`containerColor` 控制普通模式的容器填色，`indicatorColor` 控制两种模式的指示器填色，默认值见 `CouiSegmentedControlDefaults`。

## Group 内使用

`labels` 来自宿主字符串资源。选择值由宿主持有，收到 `onSelected` 后更新：

```kotlin
@RequiresApi(34)
@Composable
fun RangeInGroup(modifier: Modifier = Modifier, labels: List<String>) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Group(modifier = modifier) {
        SegmentedControl(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            labels = labels,
            selectedIndex = selected,
            onSelected = { selected = it },
        )
    }
}
```

## 页面背景与透明模糊

背景源挂在独立的背景兄弟节点上，不能包含选择器或其文字。透明模糊需要覆盖完整采样范围的不透明 SDR 背景；透明的是控件材质，采样源不能留空。

下面的 `backgroundColor` 是宿主页面背景色，`dark` 来自宿主主题，`errorContent` 由宿主提供。多色图案也可以绘制在同一个背景兄弟节点中。

```kotlin
@RequiresApi(34)
@Composable
fun RangeOnBackground(
    modifier: Modifier = Modifier,
    labels: List<String>,
    backgroundColor: Color,
    dark: Boolean,
    errorContent: @Composable (Throwable) -> Unit,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val renderer = rendering.getOrNull()
    if (renderer == null) {
        errorContent(checkNotNull(rendering.exceptionOrNull()))
        return
    }
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().height(96.dp)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop)
            .background(backgroundColor))
        SegmentedControl(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).align(Alignment.Center),
            labels = labels,
            selectedIndex = selected,
            onSelected = { selected = it },
            backdrop = backdrop,
            dark = dark,
            renderer = renderer,
        )
    }
}
```

透明模糊模式可以省略 `renderer`，由组件创建。需要错误 UI 时，按示例在 `remember` 工厂中捕获创建结果，再决定是否进入组件。每个绘制节点使用独立的 renderer，不能把同一个实例交给多个选择器。背景的完整要求见[背景采样](../effects/backdrop.md)。

## 标签与尺寸

`labels` 不能为空，`selectedIndex` 必须在标签范围内，否则抛出异常。项目增删后，宿主同步合法选择。项目宽度根据可用空间和文字自然宽度分配；空间充分时可以均分，空间不足时显示省略号。`fillMaxWidth()` 使用完整可用宽度，`widthIn()` 可限制展示宽度。

默认高度由 `CouiSegmentedControlDefaults.height` 提供，`small = true` 选择紧凑高度与内缩。标准尺寸 Modifier 可覆盖高度，绘制和命中使用实际约束。文字默认使用原厂 ButtonM 文字角色和主题前景色，`textStyle` 可覆盖字体和颜色。固定高度过小时，宿主需自行保证文字可读。

## 选择与取消

普通点按在抬手时请求选择，命中覆盖整个项目。按下当前指示器后可横向拖动，越过其他项目边缘的切换距离后即时调用 `onSelected`。

横向滑选确认后，组件持有本次拖动，页面不随手指的纵向偏移滚动。确认滑选前的纵向手势仍交给父容器。系统取消、父容器提前接管、失去主指、禁用或节点移除都会结束手势。

`commitOnRelease = true` 仅预览拖动位置，在项目的有效命中区抬手才提交一次。拖动中的有效命中区扣除项目两侧的切换距离。取消不会新增提交：即时模式保留已经交给宿主的选择，延后提交模式中的草稿恢复到宿主值。宿主拒绝更新选择时，组件在收尾后回到宿主值。

`enabled = false` 关闭触摸、键盘和无障碍选择。标签提供单选组及选中语义；Enter、方向键中心键和空格可激活焦点项。RTL 反转布局与命中，索引顺序保持传入列表的顺序。

## Demo 与实现

Demo 依次展示多色背景上的透明模糊、默认 Group 内的普通填色，以及页面底色上的透明模糊。每组显示宿主已接收的选择值，可用于观察即时提交和抬手提交的差别。紧凑尺寸、禁用态和提交方式由同一组开关控制。

[公开实现](../../coui/src/main/java/com/itos/coui/segmentbutton/SegmentedControl.kt)、[预设](../../coui/src/main/java/com/itos/coui/segmentbutton/CouiSegmentedControlDefaults.kt)、[Demo](../../demo/src/main/java/com/itos/coui/demo/DemoSegmentedControl.kt)。
