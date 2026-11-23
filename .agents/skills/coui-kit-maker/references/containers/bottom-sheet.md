# BottomSheet 底部面板

默认使用带材质的面板。先安装[窗口背景源](../effects/backdrop.md#窗口背景源)，将保存的 `MutableState<Boolean>` 本身传给 `BottomSheet`。API 33+ 且有源时，省略 `material` 即自动选用 `CouiSheetDefaults.material(dark)`。API 31–32 使用实色面板，取消和保存仍可用。[Demo 闹钟面板](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt#L112)

例如，设置页允许编辑每天的提醒时间。`savedMinutes` 是已保存的当天分钟数，`onSave` 接收确认后的新值。每次打开从已保存值开始，点取消、遮罩、系统返回或下拉关闭都丢弃本次编辑。

```kotlin
val visible = rememberSaveable { mutableStateOf(false) }
val draftMinutes = rememberSaveable { mutableIntStateOf(0) }
Item(
    title = timeSettingTitle,
    onClick = {
        draftMinutes.intValue = savedMinutes
        visible.value = true
    },
)
BottomSheet(
    visible = visible,
    titleContent = { Text(text = title, kind = CouiTextKind.SheetTitle) },
    leadingButton = CouiSheetButton(label = cancelText, enabled = visible.value, onClick = { visible.value = false }),
    trailingButton = CouiSheetButton(label = saveText, enabled = visible.value, onClick = {
        onSave(draftMinutes.intValue)
        visible.value = false
    }),
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin)
        .clip(CouiPageDefaults.shape).background(CouiPageDefaults.themeColors().card)) {
        ReminderTimeFields(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            minutes = draftMinutes, hourLabel = hourLabel, minuteLabel = minuteLabel,
        )
    }
}
```

上例将[时分双列滚轮 ReminderTimeFields](../components/pickers.md#时分双列滚轮)放进独立圆角卡片。Demo 在时间卡片下方留出 `8.dp` 再放类别选择。若表单使用[卡片内分段](../components/segmented-control.md#设置卡片和面板表单)，打开时也复制当前模式，确认时一次提交时间和模式。[Demo 闹钟表单](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt#L53)

保持面板调用常驻，退场由 `visible.value = false` 触发。若产品要求关闭前确认放弃修改，让 `onDismissRequest` 和取消按钮共用确认流程，确认后再改 `visible.value`。

面板自带正文滚动和底部导航栏、键盘避让。普通表单可直接放正文；整块正文用自己的 `LazyColumn` 时传 `scrollableContent = false`，让列表负责滚动。正文重复加 `imePadding()` 或 `navigationBarsPadding()` 会使面板与键盘之间多出空白。

顶部按钮要弹菜单时，把 `Menu` 放入 `CouiSheetButton.popupContent`，它随完整按钮定位。面板内再开材质菜单时，可复用面板提供的窗口源。[BottomSheet 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/bottom-sheet.md)
