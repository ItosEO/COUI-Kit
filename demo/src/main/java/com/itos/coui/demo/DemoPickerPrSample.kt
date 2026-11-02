package com.itos.coui.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.itos.coui.picker.CouiCalendarPicker
import java.time.LocalDate

@Composable
internal fun PickerPrSample() {
    var seconds by rememberSaveable { mutableIntStateOf(27000) }
    var day by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var draft by rememberSaveable { mutableLongStateOf(day) }
    var opened by rememberSaveable { mutableStateOf(false) }
    val hour = stringResource(R.string.picker_hour)
    val minute = stringResource(R.string.picker_minute)
    val year = stringResource(R.string.picker_year)
    val month = stringResource(R.string.picker_month)
    val date = stringResource(R.string.picker_day)
    val calendar: @Composable (Long, (Long) -> Unit) -> Unit = { value, change ->
        CouiCalendarPicker(value = LocalDate.ofEpochDay(value), onValueChange = { change(it.toEpochDay()) })
    }
    Column {
        DemoTimePicker(value = DemoTime(seconds / 3600, seconds / 60 % 60),
            onValueChange = { seconds = it.hour * 3600 + it.minute * 60 }, hourLabel = hour, minuteLabel = minute)
        DemoCountdownPicker(value = DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60),
            onValueChange = { seconds = it.totalSeconds }, hourLabel = hour, minuteLabel = minute,
            secondLabel = stringResource(R.string.picker_second))
        DemoDatePicker(value = LocalDate.ofEpochDay(day), onValueChange = { day = it.toEpochDay() },
            yearLabel = year, monthLabel = month, dayLabel = date)
        calendar(day, { day = it })
        FilledTonalButton(onClick = { draft = day; opened = true }) { Text(stringResource(R.string.picker_dialog)) }
    }
    if (opened) DemoPickerDialog(onDismissRequest = { opened = false }, onConfirm = { day = draft; opened = false },
        title = stringResource(R.string.picker_calendar_title), confirmText = stringResource(R.string.picker_confirm),
        cancelText = stringResource(R.string.picker_cancel),
        ) { calendar(draft, { draft = it }) }
}
