package com.itos.coui.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Item
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

// Clock 的 background_add_alarm_item 使用 coui_round_corner_xl。
private val TimerCardShape = RoundedCornerShape(20.dp)

internal data class DemoTimerOptions(val name: String = "", val silent: Boolean = false, val preset: Boolean = false) {
    companion object {
        val Saver = listSaver<DemoTimerOptions, Any>(save = { listOf(it.name, it.silent, it.preset) },
            restore = { DemoTimerOptions(it[0] as String, it[1] as Boolean, it[2] as Boolean) })
    }
}

/** Clock 的 oplus_timer_settings，滚轮与名称、铃声及常用开关各占一张卡片。 */
@Composable
internal fun DemoClockTimerContent(
    dark: Boolean,
    seconds: Int,
    onSecondsChange: (Int) -> Unit,
    options: DemoTimerOptions,
    onOptionsChange: (DemoTimerOptions) -> Unit,
) {
    DemoSheetCard(Modifier.padding(top = 12.dp), dark, shape = TimerCardShape) {
        DemoTimerEditorPicker(Modifier.padding(top = 16.dp, bottom = 20.dp),
            DemoDuration(seconds / 3600, seconds / 60 % 60, seconds % 60), { onSecondsChange(it.totalSeconds) },
            listOf(stringResource(R.string.picker_hour), stringResource(R.string.picker_minute), stringResource(R.string.picker_second)))
    }
    DemoSheetCard(Modifier.padding(top = 14.dp), dark, shape = TimerCardShape) {
        val colors = CouiPageDefaults.themeColors(dark)
        val titleStyle = rememberCouiTextStyle(CouiTextKind.ItemTitle)
        val nameStyle = titleStyle.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
        val summaryStyle = rememberCouiTextStyle(CouiTextKind.ItemSummary)
            .copy(fontSize = 14.sp, lineHeight = TextUnit.Unspecified, color = colors.summary)
        BasicTextField(
            value = options.name,
            onValueChange = { onOptionsChange(options.copy(name = it)) },
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .heightIn(min = 22.dp).padding(bottom = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            textStyle = nameStyle.copy(color = colors.title),
            cursorBrush = SolidColor(colors.accent),
            decorationBox = { field ->
                Box {
                    if (options.name.isEmpty()) BasicText(stringResource(R.string.sheet_timer_name),
                        style = nameStyle.copy(color = colors.summary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    field()
                }
            },
        )
        Item(
            onClick = { onOptionsChange(options.copy(silent = !options.silent)) },
            dark = dark,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            trailing = {
                DemoSettingsArrow(Modifier.padding(start = 8.dp).size(width = 12.dp, height = 24.dp),
                    back = false, color = Color(if (dark) 0x4DFFFFFF else 0x42000000))
            },
        ) {
            Column {
                BasicText(stringResource(R.string.sheet_sound), style = titleStyle.copy(color = colors.title))
                BasicText(stringResource(if (options.silent) R.string.sheet_silent else R.string.sheet_default_sound),
                    style = summaryStyle, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Divider(color = colors.divider)
        DemoSwitchPreference(title = stringResource(R.string.sheet_timer_preset),
            checked = options.preset, onCheckedChange = { onOptionsChange(options.copy(preset = it)) })
    }
}
