package com.itos.coui.demo

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.picker.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun PickerSamples(page: String, dark: Boolean) {
    var mode by rememberSaveable { mutableStateOf(page) }
    var number by rememberSaveable { mutableIntStateOf(0) }
    var timeSeconds by rememberSaveable { mutableIntStateOf(7 * 3600 + 30 * 60) }
    var durationSeconds by rememberSaveable { mutableIntStateOf(7 * 3600 + 30 * 60) }
    var date by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var wrap by rememberSaveable { mutableStateOf(true) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var is24Hour by rememberSaveable { mutableStateOf(true) }
    var restricted by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    var dialogMode by rememberSaveable { mutableStateOf<String?>(null) }
    var draft by rememberSaveable { mutableLongStateOf(0) }
    val activeMode = mode
    val calendar = page == "calendar"
    val backdrop = rememberCouiWindowBackdrop()
    val locale = LocalConfiguration.current.locales[0]
    val thisMonth = LocalDate.now().withDayOfMonth(1)
    val monthEnd = thisMonth.plusMonths(1).minusDays(1)
    val minDate = if (restricted) thisMonth else LocalDate.of(1900, 1, 1)
    val maxDate = if (restricted) monthEnd else LocalDate.of(2100, 12, 31)
    fun currentValue(kind: String): Long = when (kind) {
        "wheel" -> number.toLong()
        "timepicker" -> timeSeconds.toLong()
        "countdownpicker" -> durationSeconds.toLong()
        else -> date
    }
    fun updateValue(kind: String, next: Long) {
        when (kind) {
            "wheel" -> number = next.toInt()
            "timepicker" -> timeSeconds = next.toInt()
            "countdownpicker" -> durationSeconds = next.toInt()
            else -> date = next
        }
    }
    val title = stringResource(pickerTitle(activeMode))
    val value = when (activeMode) {
        "wheel" -> String.format(locale, "%02d", number)
        "timepicker" -> LocalTime.ofSecondOfDay(timeSeconds.toLong()).format(
            DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a", locale))
        "countdownpicker" -> String.format(locale, "%02d:%02d:%02d",
            durationSeconds / 3600, durationSeconds / 60 % 60, durationSeconds % 60)
        else -> LocalDate.ofEpochDay(date).format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    }
    if (!calendar) Group {
        DemoPopupPreference(title = stringResource(R.string.picker_type), value = title,
            expanded = menuExpanded, onExpandedChange = { menuExpanded = it },
            items = DemoWheelPickerEntries.map {
                CouiMenuItem(it.page, stringResource(it.title), selected = it.page == activeMode)
            }, onItemClick = { mode = it.value }, menuStyle = demoMenuStyle(dark))
    }
    Group(title = stringResource(R.string.picker_inline)) {
        // 类型变化时销毁旧滚轮，结束其长按、手势和惯性会话。
        key(activeMode) {
            Box(if (calendar) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().height(252.dp),
                contentAlignment = Alignment.Center) {
                PickerBody(activeMode, currentValue(activeMode), { updateValue(activeMode, it) },
                    dark, is24Hour, minDate, maxDate, wrap, enabled)
            }
        }
    }
    when (activeMode) {
        "wheel" -> Group {
            DemoSwitchPreference(title = stringResource(R.string.picker_wheel_wrap), checked = wrap,
                onCheckedChange = { wrap = it })
            Divider()
            DemoSwitchPreference(title = stringResource(R.string.picker_wheel_enabled), checked = enabled,
                onCheckedChange = { enabled = it })
        }
        "timepicker" -> Group {
            DemoSwitchPreference(title = stringResource(R.string.settings_time_24), checked = is24Hour,
                onCheckedChange = { is24Hour = it })
        }
        "datepicker", "calendar" -> Group {
            DemoSwitchPreference(title = stringResource(R.string.picker_date_restricted), checked = restricted,
                onCheckedChange = { active ->
                    if (active) date = LocalDate.ofEpochDay(date).coerceIn(thisMonth, monthEnd).toEpochDay()
                    restricted = active
                })
        }
    }
    Group(title = stringResource(R.string.picker_dialog)) {
        DemoPreference(title = title, value = value, enabled = activeMode != "wheel" || enabled, onClick = {
            draft = currentValue(activeMode)
            dialogMode = activeMode
        })
    }
    val editingMode = dialogMode
    if (editingMode != null) DemoPickerDialog(onDismissRequest = { dialogMode = null }, onConfirm = {
        updateValue(editingMode, draft)
        dialogMode = null
    }, title = stringResource(pickerTitle(editingMode)),
        confirmText = stringResource(R.string.picker_confirm), cancelText = stringResource(R.string.picker_cancel),
        backdrop = backdrop) {
        PickerBody(editingMode, draft, { draft = it }, dark, is24Hour, minDate, maxDate, wrap, enabled)
    }
}

private fun pickerTitle(page: String): Int = when (page) {
    "calendar" -> R.string.picker_calendar_title
    else -> DemoWheelPickerEntries.first { it.page == page }.title
}

@Composable
private fun PickerBody(page: String, value: Long, onValueChange: (Long) -> Unit,
    dark: Boolean, is24Hour: Boolean, minDate: LocalDate, maxDate: LocalDate, wrap: Boolean, enabled: Boolean) {
    val hour = stringResource(R.string.picker_hour)
    val minute = stringResource(R.string.picker_minute)
    val locale = LocalConfiguration.current.locales[0]
    val seconds = value.toInt()
    when (page) {
        "wheel" -> Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            WheelPicker(Modifier.width(120.dp), value = value.toInt(), onValueChange = { onValueChange(it.toLong()) },
                valueRange = 0..12, label = stringResource(R.string.picker_wheel_label),
                wrap = wrap, enabled = enabled, formatter = { String.format(locale, "%02d", it) })
        }
        "timepicker" -> DemoTimePicker(value = DemoTime(seconds / 3600, seconds / 60 % 60),
            onValueChange = { onValueChange((it.hour * 3600 + it.minute * 60).toLong()) },
            hourLabel = hour, minuteLabel = minute, is24Hour = is24Hour)
        "countdownpicker" -> DemoCountdownPicker(value = DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60),
            onValueChange = { onValueChange(it.totalSeconds.toLong()) }, hourLabel = hour,
            minuteLabel = minute, secondLabel = stringResource(R.string.picker_second))
        "datepicker" -> DemoDatePicker(value = LocalDate.ofEpochDay(value), onValueChange = { onValueChange(it.toEpochDay()) },
            yearLabel = stringResource(R.string.picker_year), monthLabel = stringResource(R.string.picker_month),
            dayLabel = stringResource(R.string.picker_day), minDate = minDate, maxDate = maxDate)
        "calendar" -> CalendarPicker(value = LocalDate.ofEpochDay(value), onValueChange = { onValueChange(it.toEpochDay()) },
            dark = dark, minDate = minDate, maxDate = maxDate)
    }
}
