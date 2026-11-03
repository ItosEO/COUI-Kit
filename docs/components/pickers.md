# 📅选择器

`WheelPicker` 用于选择整数，`CalendarPicker` 用于选择单个公历日期。两者都由页面提供当前值和更新回调，可直接嵌入页面，也可以放进现有的 `AlertDialog`。

库公开的是这两个受控组件。时间、倒计时、年月日滚轮组合和选择弹窗属于 Demo 示例，不是额外的公开 Picker API。

滚轮是一列上下滚动的数字，中间为较大的当前值，上下显示相邻数字；单列本身没有时间冒号和整组选择线。月历包含月份标题、左右翻月按钮、七列日期网格和选中圆圈。点击标题切换到内置的年/月/日三列滚轮，不需要单独创建三个 `WheelPicker`。

## 最小调用

滚轮需要当前整数、范围、回调和无障碍标签：

~~~kotlin
import com.itos.coui.picker.WheelPicker

WheelPicker(
    value = minute,
    onValueChange = { minute = it },
    valueRange = 0..59,
    label = "分钟",
)
~~~

月历只需要当前日期和回调；年月日文案及按钮的无障碍说明由库按系统语言提供：

~~~kotlin
import com.itos.coui.picker.CalendarPicker

CalendarPicker(
    value = selectedDate,
    onValueChange = onDateChange,
)
~~~

上述变量由页面提供。`value` 是已接受的值；页面必须在 `onValueChange` 中更新它。两个组件的 `dark` 和 `colors` 默认从 `CouiTheme` 解析，不需要 Material3。主题规则见[外观与预设](../guide/appearance.md)。

## 组合小时和分钟滚轮

下面使用两个 `WheelPicker` 选择 24 小时制时间。每列独立保存数值：

~~~kotlin
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.picker.WheelPicker

@Composable
fun TimeWheelExample() {
    var hour by rememberSaveable { mutableIntStateOf(9) }
    var minute by rememberSaveable { mutableIntStateOf(30) }

    Row {
        WheelPicker(
            modifier = Modifier.width(62.dp),
            value = hour,
            onValueChange = { hour = it },
            valueRange = 0..23,
            label = "小时",
            formatter = { "%02d".format(it) },
        )
        WheelPicker(
            modifier = Modifier.width(62.dp),
            value = minute,
            onValueChange = { minute = it },
            valueRange = 0..59,
            label = "分钟",
            repeatIntervalMillis = 100,
            formatter = { "%02d".format(it) },
        )
    }
}
~~~

`formatter` 只改变显示文字，不改变整数值。`label` 是页面必须提供的无障碍说明，不会显示在数字旁。`repeatIntervalMillis` 控制长按连续调整的间隔。

默认 `wrap = true`，但范围只有三个或更少数值时不会循环；需要固定边界时显式设置 `wrap = false`。例如 `0..59` 循环时，59 后面是 0，0 前面是 59；关闭循环后在两端停止。`wrap` 不会改变 `valueRange` 本身，也不会自动联动其他列。时间滚轮需要的整组选择线、冒号和 AM/PM 排列由页面组合，样式可参考 Demo。

## 显示月历和日期编辑器

月历支持翻月、点击日期，以及点击顶部日期标题切换到年/月/日滚轮：

~~~kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itos.coui.picker.CalendarPicker
import java.time.LocalDate

@Composable
fun CalendarExample(modifier: Modifier = Modifier) {
    var epochDay by rememberSaveable {
        mutableLongStateOf(LocalDate.now().toEpochDay())
    }
    val selectedDate = LocalDate.ofEpochDay(epochDay)

    CalendarPicker(
        modifier = modifier.fillMaxWidth(),
        value = selectedDate,
        onValueChange = { epochDay = it.toEpochDay() },
    )
}
~~~

`LocalDate` 不直接放入 `rememberSaveable`；此例通过 epoch day 保存日期。库在 `coui` 模块内提供英文和简体中文默认文案，并随系统语言选择；如需要特殊措辞，可覆盖 `previousDescription` 等可选参数。无需把 Demo 的字符串资源复制到业务项目。

左右翻月箭头只改变浏览月份，不提交日期。点击具体日期或滚动日期编辑器才调用 `onValueChange`。顶部标题右侧的图标随模式切换：月历态向下，滚轮态向上。关闭编辑器后回到宿主已接受的日期。

## 设置日期范围

通过 `minDate` 和 `maxDate` 限制可选日期：

~~~kotlin
CalendarPicker(
    value = selectedDate,
    onValueChange = onDateChange,
    minDate = LocalDate.of(2026, 11, 1),
    maxDate = LocalDate.of(2026, 11, 30),
)
~~~

这里的 `selectedDate` 必须在 2026 年 11 月内。`minDate` 和 `maxDate` 都是 **`LocalDate`（年月日）**，不接受时分秒；它们可以省略，默认覆盖 1900-01-01 到 2100-12-31。`minDate` 不得晚于 `maxDate`，`value` 也必须在范围内，否则组件会抛出异常。若动态收紧范围，宿主应先将已选日期夹紧到新范围，再传给组件。编辑滚轮会按实际月份天数和日期范围限制可选值。

`today` 仅决定“今天”的显示样式，不会自动更改 `value`；省略时取设备当前日期。需要允许选择未来 7 天时，可以写 `minDate = LocalDate.now()`、`maxDate = LocalDate.now().plusDays(7)`，并确保传入的 `value` 也在这个范围内。

## 在弹窗内选择日期

直接使用已有的 `AlertDialog` 和 `content` 槽位。弹窗持有草稿，用户确认后才保存：

~~~kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.itos.coui.dialog.AlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.picker.CalendarPicker
import java.time.LocalDate

@Composable
fun DatePickerDialog(
    visible: Boolean,
    value: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    var draftDay by rememberSaveable(value) {
        mutableLongStateOf(value.toEpochDay())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = "选择日期",
        confirmAction = CouiDialogAction("确定", {
            onConfirm(LocalDate.ofEpochDay(draftDay))
            onDismiss()
        }),
        dismissAction = CouiDialogAction("取消", onDismiss),
    ) {
        CalendarPicker(
            value = LocalDate.ofEpochDay(draftDay),
            onValueChange = { draftDay = it.toEpochDay() },
        )
    }
}
~~~

点击取消、外部区域或返回键时，示例不会提交草稿。需要主操作强调色、居中窗口或背景材质时，直接使用[弹窗文档](dialog.md)中的 `CouiDialogAction`、`placement` 和 `material`；Picker 本身不创建窗口或背景采样源。

## 自定义滚轮外观

多列布局可使用 `CouiPickerDefaults` 的 `columnWidth`、`padding`、`dateYearWidth` 和 `dateMonthWidth` 预设。

高度通过 `Modifier.height()` 设置；省略时使用 `CouiPickerDefaults.height`。父级约束和 Modifier 顺序决定实际视口，绘制、命中和运动步长使用实测高度。`textSize`、`focusTextSize` 和 `diffusion` 使用 `Dp`，字号不再次按系统 `sp` 放大。下面沿用前面的 `minute` 状态：

~~~kotlin
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.picker.CouiPickerDefaults

WheelPicker(
    modifier = Modifier.height(160.dp),
    value = minute,
    onValueChange = { minute = it },
    valueRange = 0..59,
    label = "分钟",
    textSize = 16.dp,
    focusTextSize = 22.dp,
    diffusion = 4.dp,
    verticalFading = true,
    colors = CouiPickerDefaults.wheelColors().copy(
        focus = accent,
    ),
)
~~~

这里的 `accent` 由宿主提供。`verticalFading` 开启上下渐隐，额外使用离屏合成；无须渐隐时保留默认 `false`。`clockTypeface` 使用时钟专用字体配置，显示效果可能受设备字体支持情况影响。

`diffusion` 参与滚轮的运动步长和选中带，不能用外部 padding 替代。

## 整组选择线

时间或日期列需要贯穿整组的选择线时，在父级 Row 使用共享绘制：

~~~kotlin
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.itos.coui.picker.CouiPickerDefaults
import com.itos.coui.picker.couiPickerSelectionLines

Row(
    Modifier.fillMaxWidth()
        .couiPickerSelectionLines(dividerColor)
        .padding(vertical = CouiPickerDefaults.padding),
) {
    // 放入前述小时、分钟滚轮。
}
~~~

此片段位于 Compose 内容中，`dividerColor` 由宿主主题提供。选择线绘制在内容后方，不改变高度、裁剪和输入。对称的上下留白可放内侧，非对称留白放在选择线外侧。倒计时的大字与 diffusion 配置不适用这组标准选择线。`horizontalInset` 控制横向内缩，须为有限非负 Dp。

## 颜色与主题

省略 `dark` 时使用 `CouiTheme.isDark`；省略 `colors` 时，滚轮通过 `CouiPickerDefaults.wheelColors(dark)`、月历通过 `CouiCalendarDefaults.colors(dark)` 创建颜色。显式传入的颜色优先。

`CouiWheelColors` 包含 `normal`（普通项）和 `focus`（选中项）；`CouiCalendarColors` 包含 `foreground`、`secondary`、`weekday`、`hint`、`highlight`、`divider` 和 `onAccent`。月历另有独立的 `accent` 参数，用于选中圆及强调色元素。自定义 `accent` 时，应确保 `onAccent` 文字与选中圆有足够对比度。

M3 主题适配的接入方式见[外观与预设](../guide/appearance.md)。

## 交互与限制

滚轮支持点击邻项、拖动、惯性滑动、长按、键盘方向键和无障碍调整。`valueRange` 必须非空且包含 `value`。`enabled = false` 会停止滚轮交互。`repeatIntervalMillis` 和两个字号必须大于零，`diffusion` 不能为负值。

月历会按范围约束日期；顶部月份箭头只浏览，不提交日期。月历不提供整体 `enabled` 参数。滚轮没有 `onValueChangeFinished`，如需“确认后才保存”，请由页面或弹窗持有草稿。

选择器本身不需要背景源；只有宿主额外使用材质弹窗时，才按[弹窗背景材质](dialog.md#使用背景材质)要求配置。系统与构建门槛见[兼容要求](../guide/compatibility.md)。

## Demo 与源码

Demo 为时间、倒计时、年月日滚轮和月历提供独立详情页，每页包含页面控件和弹窗草稿。

| 组合                    | 示例                                                                                  |
|-----------------------|-------------------------------------------------------------------------------------|
| 时间、AM/PM、倒计时、日期顺序与范围  | [DemoWheelPresets](../../demo/src/main/java/com/itos/coui/demo/DemoWheelPresets.kt) |
| 标题、项目材质、确认/取消与任意选择器内容 | [DemoPickerDialog](../../demo/src/main/java/com/itos/coui/demo/DemoPickerDialog.kt) |
| 页面与弹窗共用内容、草稿及确认提交     | [DemoPickerSamples](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt) |

公开 API 见 [WheelPicker / CouiWheelColors / CouiPickerDefaults](../../coui/src/main/java/com/itos/coui/picker/WheelPicker.kt)、[CalendarPicker](../../coui/src/main/java/com/itos/coui/picker/CalendarPicker.kt)及 [CouiCalendarColors / CouiCalendarDefaults](../../coui/src/main/java/com/itos/coui/picker/CouiCalendarDefaults.kt)。
