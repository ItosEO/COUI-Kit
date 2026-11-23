# SegmentedControl 分段选择

## 设置卡片和面板表单

在 `Group` 内切换选项时，直接用填色分段，底部面板里的卡片也一样。`rememberCouiSheetSurfaceBackdrop()` 只提供面板底图，传给卡片内的控件会透出卡片背后的颜色。整个 `SegmentedControl` 都要求 API 34+。API 31–33 若也要编辑该设置，让单选行修改同一份草稿。[Demo 面板分组](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt#L197)

下面是已有 `Group` 的内容。`options` 是页面提供的非空选项，每项有稳定的 `id` 和本地化 `label`。面板保存时才把 `draftId` 写回业务值。若当前选项被删除，宿主先选定有效的业务值，再绘制分段。

```kotlin
if (Build.VERSION.SDK_INT >= 34) {
    SegmentedControl(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        labels = options.map { it.label },
        selectedIndex = options.indexOfFirst { it.id == draftId },
        onSelected = { draftId = options[it].id },
    )
} else {
    Column(Modifier.selectableGroup()) {
        options.forEach { option ->
            key(option.id) {
                val selected = draftId == option.id
                val source = remember { MutableInteractionSource() }
                Item(
                    modifier = Modifier.couiButtonFeedback(CouiTheme.isDark, source, true, list = true)
                        .selectable(selected, source, null, true, Role.RadioButton) { draftId = option.id },
                    title = { Text(text = option.label, kind = CouiTextKind.ItemTitle) },
                    trailing = { RadioButton(selected = selected) },
                )
            }
        }
    }
}
```

## 图片或渐变上的透明分段

需要透出颜色变化时，在背景节点上使用 `.couiBackdrop(backdrop)`，分段与它并列。每个透明分段单独创建 `CouiFloatingRenderer.standalone()`，将它和同一个 `backdrop` 一起传入。若绘制器创建失败，宿主报告失败并沿用填色分段。控件直接放在面板底图上且需要采样该底图时，才使用 `rememberCouiSheetSurfaceBackdrop()`。[Demo 渐变与透明分段](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSegmentedControl.kt#L54)
