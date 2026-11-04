package com.itos.coui.demo

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Footer
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.picker.*
import com.itos.coui.layout.Group
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun PickerSamples(page: String, dark: Boolean) {
    var seconds by rememberSaveable { mutableIntStateOf(7 * 3600 + 30 * 60) }
    var date by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var draftSeconds by rememberSaveable { mutableIntStateOf(seconds) }
    var draftDate by rememberSaveable { mutableLongStateOf(date) }
    var opened by rememberSaveable { mutableStateOf(false) }
    var is24Hour by rememberSaveable { mutableStateOf(true) }
    var restricted by rememberSaveable { mutableStateOf(false) }
    val title = stringResource(DemoComponentGroups.flatten().first { it.page == page }.title)
    val backdrop = rememberCouiWindowBackdrop()
    val locale = LocalConfiguration.current.locales[0]
    val value = if (page == "datepicker" || page == "calendar") LocalDate.ofEpochDay(date).format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)) else if (page == "timepicker")
        LocalTime.ofSecondOfDay(seconds.toLong()).format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a", locale))
        else String.format(locale, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
    val thisMonth = LocalDate.now().withDayOfMonth(1)
    val minDate = if (restricted) thisMonth else LocalDate.of(1900, 1, 1)
    val maxDate = if (restricted) thisMonth.plusMonths(1).minusDays(1) else LocalDate.of(2100, 12, 31)
    if (page == "datepicker" || page == "calendar") {
        Group {
            DemoSwitchPreference(title = stringResource(R.string.picker_date_restricted),
                checked = restricted, onCheckedChange = { active ->
                    restricted = active
                    if (active) {
                        date = LocalDate.ofEpochDay(date).coerceIn(thisMonth, maxDate).toEpochDay()
                        draftDate = date
                    }
                })
        }
        if (restricted) Footer(text = stringResource(R.string.picker_date_restriction_note))
    }
    Group(title = stringResource(R.string.picker_inline)) {
        PickerBody(page, seconds, { seconds = it }, date, { date = it }, dark, is24Hour, minDate, maxDate)
    }
    Footer(text = stringResource(R.string.picker_selected, value))
    if (page == "timepicker") Group {
        DemoSwitchPreference(title = stringResource(R.string.settings_time_24), checked = is24Hour, onCheckedChange = { is24Hour = it })
    }
    Group(title = stringResource(R.string.picker_dialog)) {
        DemoPreference(title = title, value = value, onClick = {
            draftSeconds = seconds; draftDate = date; opened = true
        })
    }
    if (opened) DemoPickerDialog(onDismissRequest = { opened = false }, onConfirm = {
        seconds = draftSeconds; date = draftDate; opened = false
    }, title = title, confirmText = stringResource(R.string.picker_confirm), cancelText = stringResource(R.string.picker_cancel),
        backdrop = backdrop) {
        PickerBody(page, draftSeconds, { draftSeconds = it }, draftDate, { draftDate = it }, dark, is24Hour, minDate, maxDate)
    }
}

@Composable
private fun PickerBody(page: String, seconds: Int, setSeconds: (Int) -> Unit, date: Long, setDate: (Long) -> Unit,
    dark: Boolean, is24Hour: Boolean, minDate: LocalDate, maxDate: LocalDate) {
    val hour = stringResource(R.string.picker_hour)
    val minute = stringResource(R.string.picker_minute)
    val year = stringResource(R.string.picker_year)
    val month = stringResource(R.string.picker_month)
    val day = stringResource(R.string.picker_day)
    when (page) {
        "timepicker" -> DemoTimePicker(value = DemoTime(seconds / 3600, seconds / 60 % 60),
            onValueChange = { setSeconds(it.hour * 3600 + it.minute * 60) },
            hourLabel = hour, minuteLabel = minute, is24Hour = is24Hour)
        "countdownpicker" -> DemoCountdownPicker(value = DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60),
            onValueChange = { setSeconds(it.totalSeconds) }, hourLabel = hour,
            minuteLabel = minute, secondLabel = stringResource(R.string.picker_second))
        "datepicker" -> DemoDatePicker(value = LocalDate.ofEpochDay(date), onValueChange = { setDate(it.toEpochDay()) },
            yearLabel = year, monthLabel = month, dayLabel = day, minDate = minDate, maxDate = maxDate)
        "calendar" -> CalendarPicker(value = LocalDate.ofEpochDay(date), onValueChange = { setDate(it.toEpochDay()) },
            dark = dark, minDate = minDate, maxDate = maxDate)
    }
}

/** 直接展示公开的受控 WheelPicker，而不是时间或日期的业务组合。 */
@Composable
internal fun WheelSample() {
    var value by rememberSaveable { mutableIntStateOf(0) }
    var wrap by rememberSaveable { mutableStateOf(true) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    Group {
        DemoSwitchPreference(title = stringResource(R.string.picker_wheel_wrap), checked = wrap,
            onCheckedChange = { wrap = it })
        com.itos.coui.layout.Divider()
        DemoSwitchPreference(title = stringResource(R.string.picker_wheel_enabled), checked = enabled,
            onCheckedChange = { enabled = it })
    }
    Group(title = stringResource(R.string.catalog_wheel)) {
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            WheelPicker(Modifier.width(120.dp), value = value, onValueChange = { value = it },
                valueRange = 0..12, label = stringResource(R.string.picker_wheel_label),
                wrap = wrap, enabled = enabled, formatter = { it.toString().padStart(2, '0') })
        }
        com.itos.coui.layout.Divider()
        DemoPreference(title = stringResource(R.string.picker_wheel_value, value))
    }
    Footer(text = stringResource(R.string.picker_wheel_note))
}
