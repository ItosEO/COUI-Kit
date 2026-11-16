# 选择器

`WheelPicker` 提供受控的整数滚轮，`CalendarPicker` 提供受控的公历月历（支持点击标题切换内置年/月/日三列滚轮）。两者均由宿主持有当前值并在回调中更新，可直接放入页面或嵌入[弹窗](dialog.md)。

- **平台版本门槛**：支持库的最低系统版本（API 31+），选择器本身无需宿主窗口背景源（`backdrop`）。
- **公开范围**：库公开 `WheelPicker`、`CalendarPicker` 与整组选择线修饰符 `couiPickerSelectionLines`；时间、倒计时、AM/PM 多列组合与选择弹窗由宿主按需装配，不作为额外内置 Picker API 提供。

## 整数滚轮（`WheelPicker`）

最小调用只需提供当前值、更新回调、整数范围和无障碍标签：

~~~kotlin
WheelPicker(
    value = minute,
    onValueChange = { minute = it },
    valueRange = 0..59,
    label = minuteLabel,
)
~~~

### 组合多列滚轮与整组选择线

下面使用两个 `WheelPicker` 组合 24 小时制时间选择，并通过 `couiPickerSelectionLines` 在父级 `Row` 绘制贯穿整组的选中带分割线：

~~~kotlin
@Composable
fun TimeWheelExample(
    modifier: Modifier = Modifier,
    hourLabel: String,
    minuteLabel: String,
    dividerColor: Color,
) {
    var hour by rememberSaveable { mutableIntStateOf(9) }
    var minute by rememberSaveable { mutableIntStateOf(30) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .couiPickerSelectionLines(dividerColor)
            .padding(vertical = CouiPickerDefaults.padding),
        horizontalArrangement = Arrangement.Center,
    ) {
        WheelPicker(
            modifier = Modifier.width(CouiPickerDefaults.columnWidth),
            value = hour,
            onValueChange = { hour = it },
            valueRange = 0..23,
            label = hourLabel,
            formatter = { "%02d".format(it) },
        )
        WheelPicker(
            modifier = Modifier.width(CouiPickerDefaults.columnWidth),
            value = minute,
            onValueChange = { minute = it },
            valueRange = 0..59,
            label = minuteLabel,
            repeatIntervalMillis = 100,
            formatter = { "%02d".format(it) },
        )
    }
}
~~~

接入 `WheelPicker` 与 `couiPickerSelectionLines` 时注意以下要点：

- **受控提交时机**：每次滚动跨越刻度时即触发 `onValueChange`，组件不提供 `onValueChangeFinished`；如果需要“点击确定后才生效”，请由页面或弹窗持有草稿状态。
- **循环与边界（`wrap`、`repeatIntervalMillis`）**：默认 `wrap = true`，当 `valueRange` 只有 3 个或更少数值时不会循环；需要到达两端停止时设置 `wrap = false`。`repeatIntervalMillis` 控制长按上下区域连续步进的间隔（默认 `300` 毫秒）。
- **无障碍标签与格式化（`label`、`formatter`）**：`label` 为必填无障碍说明，不会绘制在数字旁；`formatter` 仅影响显示文本与状态播报，不改变真实整数值。
- **尺寸、字号与视觉区域**：
  - 高度通过 `Modifier.height(...)` 设置（省略时默认为 `CouiPickerDefaults.height`，即 `135.dp`）；多列预设宽度可使用 `CouiPickerDefaults.columnWidth`、`dateYearWidth`、`dateMonthWidth`。
  - `textSize`、`focusTextSize` 和 `diffusion` 单位均为 `Dp`，不随系统 `sp` 放大；`diffusion` 参与滚轮步长和中间选中带宽度，不能用外部 `padding` 替代。
  - `visualWidth` 与 `visualAlignment` 可收窄宽列内数字的视觉绘制区域（例如让双列数字向中线靠拢），同时保持整列触摸命中范围不变。
  - `verticalFading = true` 开启上下边缘渐隐（会额外使用离屏合成）；`clockTypeface = true` 使用时钟数字字体（实际字形受设备字体支持情况影响）。
- **选择线留白顺序**：`couiPickerSelectionLines(color, horizontalInset)` 绘制在内容后方，不改变高度、裁剪与输入。对称的垂直留白应放在 `couiPickerSelectionLines` 内侧，非对称留白放在外侧；大字号与较大 `diffusion` 的倒计时组合不适用这组标准选择线。

## 公历月历（`CalendarPicker`）

最小调用只需传入当前日期 `LocalDate` 与更新回调：

~~~kotlin
CalendarPicker(
    value = selectedDate,
    onValueChange = onDateChange,
)
~~~

下面展示带持久化状态与日期范围限制的月历调用（`LocalDate` 不能直接放入 `rememberSaveable`，通过 `epochDay` 保存）：

~~~kotlin
@Composable
fun CalendarExample(
    modifier: Modifier = Modifier,
    minDate: LocalDate = LocalDate.of(2026, 11, 1),
    maxDate: LocalDate = LocalDate.of(2026, 11, 30),
) {
    var epochDay by rememberSaveable {
        mutableLongStateOf(minDate.toEpochDay())
    }
    val selectedDate = LocalDate.ofEpochDay(epochDay).coerceIn(minDate, maxDate)

    CalendarPicker(
        modifier = modifier.fillMaxWidth(),
        value = selectedDate,
        onValueChange = { epochDay = it.toEpochDay() },
        minDate = minDate,
        maxDate = maxDate,
    )
}
~~~

- **内置年月滚轮切换**：点击顶部月份标题即可在月历网格与内置年/月/日三列滚轮之间切换，无需宿主自行组合三个 `WheelPicker`。左右翻月箭头仅改变浏览月份，**不会调用 `onValueChange`**；点击具体日期或滚动内置日期滚轮才会提交新日期。
- **日期范围与 `today`**：`minDate` 和 `maxDate` 默认覆盖 `1900-01-01` 至 `2100-12-31`，必须满足 `minDate <= maxDate` 且 `value in minDate..maxDate`（否则会抛出异常）。若页面动态收紧范围，宿主必须先将 `value` 夹紧到新范围内再传入组件。`today` 仅控制“今天”的圆环与高亮指示色，不会自动修改 `value`。
- **内置多语言文案**：库已在 `coui` 模块内置英文与简体中文默认无障碍文案并随系统语言切换，无需复制 Demo 字符串资源；如有特殊措辞需求，可覆盖 `previousDescription`、`nextDescription`、`changeMonthDescription`、`yearLabel`、`monthLabel`、`dayLabel`。

## 在弹窗中装配选择器

将 `CalendarPicker` 或 `WheelPicker` 直接放入 `Dialog` 的 `content` 槽位即可实现弹窗选日期/时间，由弹窗持有草稿并在点击确认后提交：

~~~kotlin
@Composable
fun DatePickerDialogExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    value: LocalDate,
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: (LocalDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return

    var draftDay by rememberSaveable(value) {
        mutableLongStateOf(value.toEpochDay())
    }
    Dialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = title,
        confirmAction = CouiDialogAction(confirmLabel, {
            onConfirm(LocalDate.ofEpochDay(draftDay))
            onDismissRequest()
        }),
        dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
    ) {
        CalendarPicker(
            value = LocalDate.ofEpochDay(draftDay),
            onValueChange = { draftDay = it.toEpochDay() },
        )
    }
}
~~~

若弹窗需要主操作强调色、居中显示或 API 33+ 背景模糊材质，直接按[弹窗文档](dialog.md#使用背景材质)配置 `Dialog` 的 `visible`、`placement`、`material` 与 `backdrop`。

## 颜色与主题覆盖

省略 `dark` 时默认读取 `CouiTheme.isDark`；省略 `colors` 时，`WheelPicker` 使用 `CouiPickerDefaults.wheelColors(dark)`，`CalendarPicker` 使用 `CouiCalendarDefaults.colors(dark)`，通用主题接入见[外观与预设](../guide/appearance.md)。

可通过 `copy(...)` 覆盖局部色彩角色，其中月历的 `accent` 为独立参数：

~~~kotlin
CalendarPicker(
    value = selectedDate,
    onValueChange = onDateChange,
    dark = dark,
    accent = accent,
    colors = CouiCalendarDefaults.colors(dark).copy(
        onAccent = onAccentColor,
    ),
)
~~~

| 类型 | 字段 | 用途说明 |
| --- | --- | --- |
| `CouiWheelColors` | `normal` | 上下相邻非选中项文字颜色 |
| `CouiWheelColors` | `focus` | 中央选中项文字颜色 |
| `CouiCalendarColors` | `foreground` | 本月常规日期与翻月箭头颜色 |
| `CouiCalendarColors` | `secondary` | 顶部标题展开/收起图标颜色 |
| `CouiCalendarColors` | `weekday` | 星期栏（周日–周六）文字颜色 |
| `CouiCalendarColors` | `hint` | 非本月日期及内置滚轮非选中项颜色 |
| `CouiCalendarColors` | `highlight` | 日期单元格按下态高亮底色 |
| `CouiCalendarColors` | `divider` | 内置日期滚轮的上下选择线颜色 |
| `CouiCalendarColors` | `onAccent` | 已选日期圆形内的文字颜色（自定义 `accent` 时需保证对比度） |

## 装配注意事项

- **`WheelPicker` 参数约束**：`valueRange` 必须非空且包含 `value`；`textSize`、`focusTextSize`、`repeatIntervalMillis` 必须大于 `0`；`diffusion` 不能为负值；`visualWidth` 为 `Dp.Unspecified` 或有限正数。`enabled = false` 会禁用交互并将文字半透明显示。
- **`CalendarPicker` 约束**：不提供整体 `enabled` 参数；日期约束完全通过 `minDate` 与 `maxDate` 控制。
- **背景源边界**：选择器本身不消费背景源；只有外层搭配材质 `Dialog` 时才需要按[弹窗背景材质](dialog.md#使用背景材质)提供 `backdrop`，系统与构建约束见[兼容要求](../guide/compatibility.md)。

## 源码与完整示例

- **公开入口与预设**：[WheelPicker.kt](../../coui/src/main/java/com/itos/coui/picker/WheelPicker.kt)、[CalendarPicker.kt](../../coui/src/main/java/com/itos/coui/picker/CalendarPicker.kt)、[CouiCalendarDefaults.kt](../../coui/src/main/java/com/itos/coui/picker/CouiCalendarDefaults.kt)、[CouiPickerSelectionLines.kt](../../coui/src/main/java/com/itos/coui/picker/CouiPickerSelectionLines.kt)
- **Demo 完整示例**：[DemoWheelPresets.kt](../../demo/src/main/java/com/itos/coui/demo/DemoWheelPresets.kt)、[DemoPickerDialog.kt](../../demo/src/main/java/com/itos/coui/demo/DemoPickerDialog.kt)、[DemoPickerSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt)
