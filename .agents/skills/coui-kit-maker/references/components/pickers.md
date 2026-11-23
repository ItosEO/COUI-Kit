# WheelPicker / CalendarPicker

整数或时间档位用 `WheelPicker`。时、分等多列由宿主组合，整组共用选择线时用 `couiPickerSelectionLines`。日期用 `CalendarPicker`，点击月份标题即可切换内置年月日滚轮。

月历左右箭头只切换正在浏览的月份。等 `onValueChange` 再更新选中日期；动态收紧日期范围时，先将选中值调整到新范围之内。[WheelPicker 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/pickers.md)

单列数值用页面给出的范围和单位。日历直接传 `LocalDate`。把选择器嵌入确认弹窗时，打开时将已保存的值复制到草稿，滚动或选日期只改草稿，确认才写回；弹窗沿用 [Dialog 推荐写法](dialog.md)。[Demo 数值与日历](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt#L137) · [Demo 选择弹窗](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPickerDialog.kt#L32)

```kotlin
WheelPicker(value = count, onValueChange = { count = it }, valueRange = 0..12, label = unitLabel)

CalendarPicker(
    value = date,
    onValueChange = { date = it },
    minDate = firstDate,
    maxDate = lastDate,
)
```

## 时分双列滚轮

要对齐 Demo 的 24 小时闹钟编辑器，让时、分各占半行，数字向中间靠，两列共用一对选择线。`label` 是无障碍名称，显示「09」「05」由 `formatter` 控制。[编辑面板](../containers/bottom-sheet.md)传入当天分钟数的草稿（0 至 1439）。两个回调都读取同一份最新草稿，快速交替滚动时能保留另一列刚选的值。[Demo 时分布局](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoWheelPresets.kt#L75)

```kotlin
@Composable
private fun ReminderTimeFields(
    modifier: Modifier = Modifier,
    minutes: MutableIntState,
    hourLabel: String,
    minuteLabel: String,
) {
    val locale = LocalConfiguration.current.locales[0]
    val twoDigits: (Int) -> String = { String.format(locale, "%02d", it) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier.fillMaxWidth().couiPickerSelectionLines(CouiTheme.colorScheme.outlineVariant)
                .padding(vertical = CouiPickerDefaults.padding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelPicker(
                modifier = Modifier.weight(1f), value = minutes.intValue / 60,
                onValueChange = { hour -> minutes.intValue = hour * 60 + minutes.intValue % 60 },
                valueRange = 0..23, label = hourLabel, formatter = twoDigits,
                visualWidth = 76.dp, visualAlignment = Alignment.End,
            )
            WheelPicker(
                modifier = Modifier.weight(1f), value = minutes.intValue % 60,
                onValueChange = { minute -> minutes.intValue = (minutes.intValue / 60) * 60 + minute },
                valueRange = 0..59, label = minuteLabel, formatter = twoDigits,
                repeatIntervalMillis = 100, visualWidth = 76.dp, visualAlignment = Alignment.Start,
            )
        }
    }
}
```
