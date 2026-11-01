package com.itos.coui.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.picker.*
import com.itos.coui.settings.CouiSettingsGroup
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun PickerSamples(page: String, dark: Boolean) {
    var seconds by rememberSaveable { mutableIntStateOf(7 * 3600 + 30 * 60) }
    var date by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var draftSeconds by rememberSaveable { mutableIntStateOf(seconds) }
    var draftDate by rememberSaveable { mutableLongStateOf(date) }
    var opened by rememberSaveable { mutableStateOf(false) }
    var is24Hour by rememberSaveable { mutableStateOf(true) }
    val title = stringResource(DemoComponentGroups.flatten().first { it.page == page }.title)
    val backdrop = rememberCouiWindowBackdrop()
    val value = if (page == "datepicker" || page == "calendar") LocalDate.ofEpochDay(date).format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) else if (page == "timepicker")
        LocalTime.ofSecondOfDay(seconds.toLong()).format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a"))
        else String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60)
    CouiSettingsGroup(title = stringResource(R.string.picker_inline)) {
        PickerBody(page, seconds, { seconds = it }, date, { date = it }, dark, is24Hour)
    }
    if (page == "timepicker") CouiSettingsGroup {
        DemoSwitchPreference(title = stringResource(R.string.settings_time_24), checked = is24Hour, onCheckedChange = { is24Hour = it })
    }
    CouiSettingsGroup(title = stringResource(R.string.picker_dialog)) {
        DemoPreference(title = title, value = value, onClick = {
            draftSeconds = seconds; draftDate = date; opened = true
        })
    }
    if (opened) DemoPickerDialog(onDismissRequest = { opened = false }, onConfirm = {
        seconds = draftSeconds; date = draftDate; opened = false
    }, title = title, confirmText = stringResource(R.string.picker_confirm), cancelText = stringResource(R.string.picker_cancel),
        dark = dark, accent = com.itos.coui.theme.CouiTheme.colorScheme.accent, backdrop = backdrop) {
        PickerBody(page, draftSeconds, { draftSeconds = it }, draftDate, { draftDate = it }, dark, is24Hour)
    }
}

@Composable
private fun PickerBody(page: String, seconds: Int, setSeconds: (Int) -> Unit, date: Long, setDate: (Long) -> Unit,
    dark: Boolean, is24Hour: Boolean) {
    val hour = stringResource(R.string.picker_hour)
    val minute = stringResource(R.string.picker_minute)
    val year = stringResource(R.string.picker_year)
    val month = stringResource(R.string.picker_month)
    val day = stringResource(R.string.picker_day)
    when (page) {
        "timepicker" -> DemoTimePicker(value = DemoTime(seconds / 3600, seconds / 60 % 60),
            onValueChange = { setSeconds(it.hour * 3600 + it.minute * 60) }, dark = dark,
            hourLabel = hour, minuteLabel = minute, is24Hour = is24Hour)
        "countdownpicker" -> DemoCountdownPicker(value = DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60),
            onValueChange = { setSeconds(it.totalSeconds) }, dark = dark, hourLabel = hour,
            minuteLabel = minute, secondLabel = stringResource(R.string.picker_second))
        "datepicker" -> DemoDatePicker(value = LocalDate.ofEpochDay(date), onValueChange = { setDate(it.toEpochDay()) },
            dark = dark, yearLabel = year, monthLabel = month, dayLabel = day)
        "calendar" -> CouiCalendarPicker(value = LocalDate.ofEpochDay(date), onValueChange = { setDate(it.toEpochDay()) },
            dark = dark, accent = com.itos.coui.theme.CouiTheme.colorScheme.accent, previousDescription = stringResource(R.string.picker_previous),
            nextDescription = stringResource(R.string.picker_next), changeMonthDescription = stringResource(R.string.picker_change_month),
            yearLabel = year, monthLabel = month, dayLabel = day)
    }
}
