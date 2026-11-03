package com.itos.coui.demo

import com.itos.coui.picker.*
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itos.coui.theme.CouiTheme
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.util.Locale

private val CountdownHeight = 228.dp
private val CountdownMargin = 30.dp
private val CountdownSeparatorWidth = 30.dp

@Immutable
internal data class DemoTime(val hour: Int, val minute: Int) {
    init { require(hour in 0..23 && minute in 0..59) }
}

@Immutable
internal data class DemoDuration(val hour: Int, val minute: Int, val second: Int) {
    init { require(hour in 0..23 && minute in 0..59 && second in 0..59) }
    val totalSeconds: Int get() = hour * 3600 + minute * 60 + second
}

/** 通用时间滚轮；hourLabel/minuteLabel 只用于语义，不在数字旁额外占位。 */
@Composable
internal fun DemoTimePicker(
    modifier: Modifier = Modifier,
    value: DemoTime,
    onValueChange: (DemoTime) -> Unit,
    hourLabel: String,
    minuteLabel: String,
    is24Hour: Boolean = true,
    enabled: Boolean = true,
    fillColumns: Boolean = false,
    twoDigitHours: Boolean = false,
) {
    val locale = LocalConfiguration.current.locales[0]
    var latest by remember { mutableStateOf(value) }
    SideEffect { if (latest != value) latest = value }
    fun submit(next: DemoTime) { latest = next; onValueChange(next) }
    val periods = DateFormatSymbols(locale).amPmStrings
    val periodFirst = DateFormat.getBestDateTimePattern(locale, "hm").startsWith("a")
    val period: @Composable () -> Unit = {
        WheelPicker(Modifier.width(CouiPickerDefaults.columnWidth), value.hour / 12,
            { submit(latest.copy(hour = latest.hour % 12 + it * 12)) }, 0..1, periods[value.hour / 12],
            enabled = enabled, wrap = false, textSize = 18.dp, formatter = { periods[it] })
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().couiPickerSelectionLines(CouiTheme.colorScheme.outlineVariant,
            if (fillColumns) 0.dp else 16.dp).padding(vertical = CouiPickerDefaults.padding),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (!is24Hour && periodFirst) period()
            WheelPicker(if (fillColumns) Modifier.weight(1f) else Modifier.width(CouiPickerDefaults.columnWidth),
                if (is24Hour) value.hour else ((value.hour + 11) % 12 + 1),
                { submit(latest.copy(hour = if (is24Hour) it else it % 12 + latest.hour / 12 * 12)) },
                if (is24Hour) 0..23 else 1..12, hourLabel, enabled = enabled,
                formatter = { String.format(locale, if (twoDigitHours) "%02d" else "%d", it) })
            WheelPicker(if (fillColumns) Modifier.weight(1f) else Modifier.width(CouiPickerDefaults.columnWidth), value.minute,
                { submit(latest.copy(minute = it)) }, 0..59, minuteLabel, enabled = enabled,
                repeatIntervalMillis = 100, formatter = { String.format(locale, "%02d", it) })
            if (!is24Hour && !periodFirst) period()
        }
    }
}

/** 时钟标准倒计时：三行、三列、38dp 数字、30dp 独立冒号及渐隐。 */
@Composable
internal fun DemoCountdownPicker(
    modifier: Modifier = Modifier,
    value: DemoDuration,
    onValueChange: (DemoDuration) -> Unit,
    hourLabel: String,
    minuteLabel: String,
    secondLabel: String,
    enabled: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales[0]
    var latest by remember { mutableStateOf(value) }
    SideEffect { if (latest != value) latest = value }
    fun submit(next: DemoDuration) { latest = next; onValueChange(next) }
    val foreground = CouiTheme.colorScheme.onSurface.copy(alpha = .9f)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().padding(horizontal = CountdownMargin),
            verticalAlignment = Alignment.CenterVertically) {
            val values = listOf(value.hour, value.minute, value.second)
            val labels = listOf(hourLabel, minuteLabel, secondLabel)
            values.forEachIndexed { index, number ->
                WheelPicker(Modifier.weight(1f).height(CountdownHeight), number, { next -> submit(when (index) {
                    0 -> latest.copy(hour = next); 1 -> latest.copy(minute = next); else -> latest.copy(second = next)
                }) }, if (index == 0) 0..23 else 0..59, labels[index], enabled = enabled,
                    textSize = 38.dp, focusTextSize = 38.dp,
                    diffusion = 4.dp, verticalFading = true, clockTypeface = true,
                    repeatIntervalMillis = if (index == 0) 300 else 100,
                    formatter = { String.format(locale, "%02d", it) })
                if (index < 2) BasicText(":", Modifier.width(CountdownSeparatorWidth).padding(bottom = 4.dp),
                    style = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold,
                        color = foreground.copy(alpha = foreground.alpha * if (enabled) 1f else .5f), textAlign = TextAlign.Center))
            }
        }
    }
}

/** 新建倒计时的 MiniTimePickerView：135dp 三列，不使用主页的大号数字与冒号。 */
@Composable
internal fun DemoTimerEditorPicker(
    modifier: Modifier = Modifier,
    value: DemoDuration,
    onValueChange: (DemoDuration) -> Unit,
    labels: List<String>,
) {
    val locale = LocalConfiguration.current.locales[0]
    var latest by remember { mutableStateOf(value) }
    SideEffect { if (latest != value) latest = value }
    fun submit(next: DemoDuration) { latest = next; onValueChange(next) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().couiPickerSelectionLines(CouiTheme.colorScheme.outlineVariant, 0.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            listOf(value.hour, value.minute, value.second).forEachIndexed { index, number ->
                WheelPicker(Modifier.width(58.dp), number, { next -> submit(when (index) {
                    0 -> latest.copy(hour = next); 1 -> latest.copy(minute = next); else -> latest.copy(second = next)
                }) }, if (index == 0) 0..23 else 0..59, labels[index],
                    repeatIntervalMillis = if (index == 0) 300 else 100,
                    formatter = { String.format(locale, "%02d", it) })
            }
        }
    }
}

/** 公历日期滚轮；日期范围是宿主约束，年月改变时按实际月份夹紧 day。 */
@Composable
internal fun DemoDatePicker(
    modifier: Modifier = Modifier,
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    yearLabel: String,
    monthLabel: String,
    dayLabel: String,
    minDate: LocalDate = LocalDate.of(1900, 1, 1),
    maxDate: LocalDate = LocalDate.of(2100, 12, 31),
    enabled: Boolean = true,
    showDay: Boolean = true,
) {
    require(minDate <= maxDate && value in minDate..maxDate)
    val locale = LocalConfiguration.current.locales[0]
    var latest by remember { mutableStateOf(value) }
    SideEffect { if (latest != value) latest = value }
    val pattern = DateFormat.getBestDateTimePattern(locale, "yyyyMMMMdd")
    val order = pattern.filter { it in "yMd" }.toList().distinct().filter { showDay || it != 'd' }
    val months = DateFormatSymbols(locale).shortMonths
    val chinese = locale.language == "zh"
    val minMonth = if (value.year == minDate.year) minDate.monthValue else 1
    val maxMonth = if (value.year == maxDate.year) maxDate.monthValue else 12
    val minDay = if (value.year == minDate.year && value.monthValue == minDate.monthValue) minDate.dayOfMonth else 1
    val maxDay = if (value.year == maxDate.year && value.monthValue == maxDate.monthValue) maxDate.dayOfMonth else value.lengthOfMonth()
    fun submit(date: LocalDate) {
        val next = date.coerceIn(minDate, maxDate)
        latest = next
        onValueChange(next)
    }
    Row(modifier.fillMaxWidth().couiPickerSelectionLines(CouiTheme.colorScheme.outlineVariant)
        .padding(vertical = CouiPickerDefaults.padding),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        order.forEach { kind -> when (kind) {
            'y' -> WheelPicker(Modifier.width(CouiPickerDefaults.dateYearWidth), value.year,
                { submit(latest.withYear(it)) }, minDate.year..maxDate.year, yearLabel, enabled = enabled,
                formatter = { if (chinese) "$it$yearLabel" else String.format(locale, "%d", it) })
            'M' -> WheelPicker(Modifier.width(CouiPickerDefaults.dateMonthWidth), value.monthValue,
                { submit(latest.withMonth(it)) }, minMonth..maxMonth, monthLabel, enabled = enabled,
                wrap = minMonth == 1 && maxMonth == 12,
                formatter = { if (chinese) "$it$monthLabel" else months[it - 1] })
            'd' -> WheelPicker(Modifier.width(CouiPickerDefaults.columnWidth), value.dayOfMonth,
                { submit(latest.withDayOfMonth(it.coerceAtMost(latest.lengthOfMonth()))) }, minDay..maxDay, dayLabel, enabled = enabled,
                wrap = minDay == 1 && maxDay == value.lengthOfMonth(),
                formatter = { if (chinese) "$it$dayLabel" else String.format(locale, "%02d", it) })
        } }
    }
}
