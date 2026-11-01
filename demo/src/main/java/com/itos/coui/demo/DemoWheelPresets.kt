package com.itos.coui.demo

import com.itos.coui.picker.*
import android.text.format.DateFormat
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    dark: Boolean,
    hourLabel: String,
    minuteLabel: String,
    is24Hour: Boolean = true,
    enabled: Boolean = true,
    fillColumns: Boolean = false,
    twoDigitHours: Boolean = false,
) {
    val locale = LocalConfiguration.current.locales[0]
    val periods = DateFormatSymbols(locale).amPmStrings
    val periodFirst = DateFormat.getBestDateTimePattern(locale, "hm").startsWith("a")
    val period: @Composable () -> Unit = {
        CouiWheelPicker(Modifier.width(CouiPickerDefaults.columnWidth), value.hour / 12,
            { onValueChange(value.copy(hour = value.hour % 12 + it * 12)) }, 0..1, periods[value.hour / 12], dark,
            enabled, wrap = false, textSize = 18.dp, formatter = { periods[it] })
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().pickerSelectionLines(dark, if (fillColumns) 0.dp else 16.dp).padding(vertical = CouiPickerDefaults.padding),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (!is24Hour && periodFirst) period()
            CouiWheelPicker(if (fillColumns) Modifier.weight(1f) else Modifier.width(CouiPickerDefaults.columnWidth),
                if (is24Hour) value.hour else ((value.hour + 11) % 12 + 1),
                { onValueChange(value.copy(hour = if (is24Hour) it else it % 12 + value.hour / 12 * 12)) },
                if (is24Hour) 0..23 else 1..12, hourLabel, dark, enabled,
                formatter = { String.format(locale, if (twoDigitHours) "%02d" else "%d", it) })
            CouiWheelPicker(if (fillColumns) Modifier.weight(1f) else Modifier.width(CouiPickerDefaults.columnWidth), value.minute,
                { onValueChange(value.copy(minute = it)) }, 0..59, minuteLabel, dark, enabled,
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
    dark: Boolean,
    hourLabel: String,
    minuteLabel: String,
    secondLabel: String,
    enabled: Boolean = true,
) {
    val locale = LocalConfiguration.current.locales[0]
    val foreground = Color(if (dark) 0xE6FFFFFF else 0xE6000000)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().padding(horizontal = CouiPickerDefaults.countdownMargin),
            verticalAlignment = Alignment.CenterVertically) {
            val values = listOf(value.hour, value.minute, value.second)
            val labels = listOf(hourLabel, minuteLabel, secondLabel)
            values.forEachIndexed { index, number ->
                CouiWheelPicker(Modifier.weight(1f), number, { next -> onValueChange(when (index) {
                    0 -> value.copy(hour = next); 1 -> value.copy(minute = next); else -> value.copy(second = next)
                }) }, if (index == 0) 0..23 else 0..59, labels[index], dark, enabled,
                    height = CouiPickerDefaults.countdownHeight, textSize = 38.dp, focusTextSize = 38.dp,
                    diffusion = 4.dp, verticalFading = true, clockTypeface = true,
                    repeatIntervalMillis = if (index == 0) 300 else 100,
                    formatter = { String.format(locale, "%02d", it) })
                if (index < 2) BasicText(":", Modifier.width(CouiPickerDefaults.separatorWidth).padding(bottom = 4.dp),
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
    dark: Boolean,
    labels: List<String>,
) {
    val locale = LocalConfiguration.current.locales[0]
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(modifier.fillMaxWidth().pickerSelectionLines(dark, 0.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            listOf(value.hour, value.minute, value.second).forEachIndexed { index, number ->
                CouiWheelPicker(Modifier.width(58.dp), number, { next -> onValueChange(when (index) {
                    0 -> value.copy(hour = next); 1 -> value.copy(minute = next); else -> value.copy(second = next)
                }) }, if (index == 0) 0..23 else 0..59, labels[index], dark,
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
    dark: Boolean,
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
    val pattern = DateFormat.getBestDateTimePattern(locale, "yyyyMMMMdd")
    val order = pattern.filter { it in "yMd" }.toList().distinct().filter { showDay || it != 'd' }
    val months = DateFormatSymbols(locale).shortMonths
    val chinese = locale.language == "zh"
    val minMonth = if (value.year == minDate.year) minDate.monthValue else 1
    val maxMonth = if (value.year == maxDate.year) maxDate.monthValue else 12
    val minDay = if (value.year == minDate.year && value.monthValue == minDate.monthValue) minDate.dayOfMonth else 1
    val maxDay = if (value.year == maxDate.year && value.monthValue == maxDate.monthValue) maxDate.dayOfMonth else value.lengthOfMonth()
    fun submit(date: LocalDate) = onValueChange(date.coerceIn(minDate, maxDate))
    Row(modifier.fillMaxWidth().pickerSelectionLines(dark).padding(vertical = CouiPickerDefaults.padding),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        order.forEach { kind -> when (kind) {
            'y' -> CouiWheelPicker(Modifier.width(CouiPickerDefaults.dateYearWidth), value.year,
                { submit(value.withYear(it)) }, minDate.year..maxDate.year, yearLabel, dark, enabled,
                formatter = { if (chinese) "$it$yearLabel" else String.format(locale, "%d", it) })
            'M' -> CouiWheelPicker(Modifier.width(CouiPickerDefaults.dateMonthWidth), value.monthValue,
                { submit(value.withMonth(it)) }, minMonth..maxMonth, monthLabel, dark, enabled,
                formatter = { if (chinese) "$it$monthLabel" else months[it - 1] })
            'd' -> CouiWheelPicker(Modifier.width(CouiPickerDefaults.columnWidth), value.dayOfMonth,
                { submit(value.withDayOfMonth(it)) }, minDay..maxDay, dayLabel, dark, enabled,
                formatter = { if (chinese) "$it$dayLabel" else String.format(locale, "%02d", it) })
        } }
    }
}


/** COUITimeLimitPicker / COUIDatePicker.dispatchDraw，先画全宽分割线再画列。 */
private fun Modifier.pickerSelectionLines(dark: Boolean, horizontalInset: androidx.compose.ui.unit.Dp = 16.dp): Modifier = drawBehind {
    val inset = horizontalInset.toPx().toInt().toFloat()
    val half = 24.dp.toPx().toInt().toFloat()
    val thickness = .33000004.dp.toPx().toInt().coerceAtLeast(1).toFloat()
    val color = Color(if (dark) 0x33FFFFFF else 0x1F000000)
    for (y in listOf(size.height / 2 - half, size.height / 2 + half)) {
        drawRect(color, Offset(inset, y.toInt().toFloat()), Size((size.width - inset * 2).coerceAtLeast(0f), thickness))
    }
}
