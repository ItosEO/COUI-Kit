package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.chip.ChipTabRow
import com.itos.coui.layout.Divider
import com.itos.coui.sheet.rememberCouiSheetSurfaceBackdrop
import com.itos.coui.text.Text as CouiText
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.theme.CouiTheme
import java.time.LocalTime
import kotlinx.coroutines.delay

internal data class DemoAlarmOptions(val name: String = "", val type: Int = 0,
    val silent: Boolean = false, val vibrate: Boolean = true, val snooze: Boolean = true) {
    companion object {
        val Saver = listSaver<DemoAlarmOptions, Any>(
            save = { listOf(it.name, it.type, it.silent, it.vibrate, it.snooze) },
            restore = { DemoAlarmOptions(it[0] as String, it[1] as Int, it[2] as Boolean,
                it[3] as Boolean, it[4] as Boolean) })
    }
}

/** Clock 的新建页面装配；选择仅修改 Demo 草稿，不访问系统闹钟或铃声。 */
@Composable
internal fun DemoClockAlarmContent(
    dark: Boolean,
    time: Int,
    onTimeChange: (Int) -> Unit,
    options: DemoAlarmOptions,
    onOptionsChange: (DemoAlarmOptions) -> Unit,
) {
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(60_000); now = LocalTime.now() } }
    val remaining = (time - now.hour * 60 - now.minute).let { if (it <= 0) it + 24 * 60 else it }
    val summary = CouiTheme.colorSchemeFor(dark).onSurface.copy(alpha = .6f)
    CouiText(Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 12.dp),
        stringResource(R.string.sheet_alarm_until, remaining / 60, remaining % 60),
        CouiTextKind.GroupFooter, summary, textAlign = TextAlign.Center)
    DemoSheetCard(dark = dark) {
        DemoTimePicker(Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            DemoTime(time / 60, time % 60), { onTimeChange(it.hour * 60 + it.minute) },
            stringResource(R.string.picker_hour), stringResource(R.string.picker_minute),
            fillColumns = true, twoDigitHours = true)
    }
    Spacer(Modifier.height(8.dp))
    AlarmTypeChoices(options.type, { onOptionsChange(options.copy(type = it)) }, dark)
    DemoSheetCard(dark = dark) {
        val textStyle = rememberCouiTextStyle(CouiTextKind.ItemTitle)
        Box(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart) {
            BasicTextField(options.name, { onOptionsChange(options.copy(name = it)) }, Modifier.fillMaxWidth(),
                singleLine = true, textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), decorationBox = { field ->
                    if (options.name.isEmpty()) BasicText(stringResource(R.string.sheet_alarm_name),
                        style = textStyle.copy(color = summary))
                    field()
                })
        }
        Divider()
        DemoPreference(Modifier.heightIn(min = 60.dp), title = stringResource(R.string.sheet_sound),
            summary = stringResource(if (options.silent) R.string.sheet_silent else R.string.sheet_default_sound),
            onClick = { onOptionsChange(options.copy(silent = !options.silent)) })
        Divider()
        DemoPreference(Modifier.heightIn(min = 60.dp), title = stringResource(R.string.sheet_vibrate),
            summary = stringResource(if (options.vibrate) R.string.sheet_default_vibration else R.string.sheet_no_vibration),
            onClick = { onOptionsChange(options.copy(vibrate = !options.vibrate)) })
        Divider()
        DemoPreference(Modifier.heightIn(min = 64.dp), title = stringResource(R.string.sheet_snooze),
            summary = stringResource(if (options.snooze) R.string.sheet_snooze_default else R.string.sheet_snooze_off),
            onClick = { onOptionsChange(options.copy(snooze = !options.snooze)) })
    }
}

@Composable
private fun AlarmTypeChoices(selected: Int, onSelected: (Int) -> Unit, dark: Boolean) {
    val labels = listOf(stringResource(R.string.sheet_ring_once), stringResource(R.string.sheet_weekdays),
        stringResource(R.string.sheet_quick_alarm), stringResource(R.string.sheet_custom))
    if (Build.VERSION.SDK_INT < 34) {
        Text(stringResource(R.string.requires_api_34), Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    // 分类栏内部已经有 16dp 水平与 8dp 垂直留白，不在外层重复叠加。
    ChipTabRow(Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp),
        items = labels.mapIndexed { index, label -> CouiChipTabItem(index, label) },
        selectedKey = selected, onSelectedKeyChange = { onSelected(it as Int) },
        backdrop = rememberCouiSheetSurfaceBackdrop(), dark = dark, itemSpacing = 8.dp,
        selectedColor = MaterialTheme.colorScheme.primary,
        selectedContentColor = MaterialTheme.colorScheme.onPrimary)
}
