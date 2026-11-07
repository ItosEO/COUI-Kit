# 分段选择器

`SegmentedControl` 在一个胶囊容器内选择项目。选中指示器具有独立的位置、宽度和高度回弹，并支持从选中项开始拖动。它适合日/周/月、课程范围等少量互斥项目，要求 Android 14（API 34）。

## 基本装配

下面的 `labels` 来自宿主字符串资源，`dark` 来自宿主主题。背景源只挂在背景兄弟节点上，不能包含选择器自身：

```kotlin
@Composable
fun ScheduleRange(modifier: Modifier = Modifier, labels: List<String>, dark: Boolean) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().height(96.dp)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop)
            .background(if (dark) Color(0xFF292929) else Color(0xFFEBEBEB)))
        SegmentedControl(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).align(Alignment.Center),
            labels = labels,
            selectedIndex = selected,
            onSelected = { selected = it },
            backdrop = backdrop,
            dark = dark,
        )
    }
}
```

`labels` 不能为空，`selectedIndex` 必须在标签范围内，否则抛出异常。项目增删后，宿主先同步合法选择。标签按自然文字宽度分配空间；通过 `fillMaxWidth()` 使用完整可用宽度，空间不足时显示省略号。

默认高度由 `CouiSegmentedControlDefaults.height` 提供；`small = true` 选择紧凑高度与内缩。标准尺寸 Modifier 可覆盖高度，绘制和命中使用实际约束。`textStyle` 可覆盖文字外观，默认使用原厂 ButtonM 文字角色。固定高度过小时，宿主需自行保证文字可读。

## 选择与取消

普通点按在抬手时请求选择。按下当前指示器后可横向拖动，进入其他项目时即时调用 `onSelected`。选择由宿主持有，回调不能代替更新 `selectedIndex`。

`commitOnRelease = true` 仅预览拖动位置，在有效项目内抬手才提交一次。纵向手势、父容器接管、系统取消、失去主指、禁用或节点移除均收尾，取消不会新增提交。即时模式中，先前已交给宿主的选择不会回滚；延后提交模式中的未提交草稿会恢复到宿主值。

`enabled = false` 关闭触摸、键盘和无障碍选择。标签提供单选组及选中语义；Enter、方向键中心键和空格可激活焦点项。RTL 反转布局与命中，索引顺序保持传入列表的顺序。

## 材质与来源

容器复用公共单主体浮动材质、均匀模糊、轮廓和跟手光，指示器保持清晰。每个组件独立持有 renderer 与图层，移除时释放动画；创建失败向上传播。背景要求见[背景采样](../effects/backdrop.md)。

[公开实现](../../coui/src/main/java/com/itos/coui/segmentbutton/SegmentedControl.kt)、[预设](../../coui/src/main/java/com/itos/coui/segmentbutton/CouiSegmentedControlDefaults.kt)、[Demo](../../demo/src/main/java/com/itos/coui/demo/DemoSegmentedControl.kt)。
