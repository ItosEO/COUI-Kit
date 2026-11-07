package com.itos.coui.demo

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import com.itos.coui.layout.CouiPageDefaults

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

internal data class DemoTimerOptions(val name: String = "", val silent: Boolean = false, val preset: Boolean = false) {
    companion object {
        val Saver = listSaver<DemoTimerOptions, Any>(save = { listOf(it.name, it.silent, it.preset) },
            restore = { DemoTimerOptions(it[0] as String, it[1] as Boolean, it[2] as Boolean) })
    }
}

/** 倒计时编辑页使用现有数值滚轮和设置行；不复用倒计时主页的显示组合。 */
@Composable
internal fun DemoClockTimerContent(
    dark: Boolean,
    seconds: Int,
    onSecondsChange: (Int) -> Unit,
    options: DemoTimerOptions,
    onOptionsChange: (DemoTimerOptions) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)
            .padding(horizontal = CouiPageDefaults.horizontalMargin)
            .clip(CouiPageDefaults.shape).background(CouiPageDefaults.themeColors(dark).card)) {
        DemoTimerEditorPicker(Modifier.padding(top = 16.dp, bottom = 20.dp),
            DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60), { onSecondsChange(it.totalSeconds) },
            listOf(stringResource(R.string.picker_hour), stringResource(R.string.picker_minute), stringResource(R.string.picker_second)))
    }
    Column(Modifier.fillMaxWidth().padding(top = 14.dp)
            .padding(horizontal = CouiPageDefaults.horizontalMargin)
            .clip(CouiPageDefaults.shape).background(CouiPageDefaults.themeColors(dark).card)) {
        val textStyle = rememberCouiTextStyle(CouiTextKind.PreferenceTitle)
        Box(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart) {
            BasicTextField(options.name, { onOptionsChange(options.copy(name = it)) }, Modifier.fillMaxWidth(),
                singleLine = true, textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), decorationBox = { field ->
                    if (options.name.isEmpty()) BasicText(stringResource(R.string.sheet_timer_name),
                        style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    field()
                })
        }
        Divider()
        DemoPreference(title = stringResource(R.string.sheet_sound),
            summary = stringResource(if (options.silent) R.string.sheet_silent else R.string.sheet_default_sound),
            onClick = { onOptionsChange(options.copy(silent = !options.silent)) })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.sheet_timer_preset),
            checked = options.preset, onCheckedChange = { onOptionsChange(options.copy(preset = it)) })
    }
}
