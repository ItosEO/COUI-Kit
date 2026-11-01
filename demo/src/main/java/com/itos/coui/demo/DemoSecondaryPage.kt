package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.settings.CouiSettingsDefaults
import com.itos.coui.settings.CouiSettingsDivider
import com.itos.coui.settings.CouiSettingsFooter
import com.itos.coui.settings.CouiSettingsGroup
@Composable
internal fun DemoSecondaryPage(page: String, dark: Boolean, preferences: DemoPreferences,
    back: () -> Unit, open: (String) -> Unit) {
    val toggleTheme = { preferences.dark(!dark) }
    var reset by rememberSaveable { mutableStateOf(false) }
    val accent = com.itos.coui.theme.CouiTheme.colorScheme.accent
    val entry = (DemoComponentGroups + DemoEffectGroups).flatten().firstOrNull { it.page == page }
    val title = entry?.title ?: when (page) {
        "font" -> R.string.settings_font
        "color" -> R.string.settings_color
        "privacy" -> R.string.settings_privacy
        "about" -> R.string.settings_about
        else -> R.string.home_app_settings
    }
    CompositionLocalProvider(LocalCouiContourGlowEnabled provides preferences.contour) {
        DemoSettingsPage(title = stringResource(title), backDescription = stringResource(R.string.settings_back),
            onBack = back, dark = dark, colors = CouiSettingsDefaults.themeColors(dark),
            actions = { if (page == "menu") MenuToolbarAction(dark, preferences, open) }) {
            if (entry != null && page != "effect_backdrop") BasicText(stringResource(catalogDescription(page)),
                Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 12.dp),
                style = com.itos.coui.text.rememberCouiTextStyle(com.itos.coui.text.CouiTextKind.PreferenceSummary)
                    .copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            when (page) {
                "sliders", "switch", "checkbox", "dialogs", "menu" -> ComponentDetail(page, dark, toggleTheme)
                "settings" -> {
                    ListSamples(dark, open)
                    CouiSettingsGroup { DemoPreference(title = stringResource(R.string.home_app_settings), onClick = { open("appsettings") }) }
                }
                "chip" -> ChipTabSample(dark)
                "effect_backdrop" -> BlurTutorial(dark)
                "timepicker", "countdownpicker", "datepicker", "calendar" -> PickerSamples(page, dark)
                "appbar", "search", "navigation", "fab" -> ChromeDetail(page, dark, open)
                "color" -> CouiSettingsGroup(Modifier.selectableGroup(), footer = stringResource(R.string.settings_demo_only)) {
                    listOf(R.string.settings_color_natural, R.string.settings_color_vivid).forEachIndexed { index, label ->
                        DemoSelectPreference(title = stringResource(label), selected = preferences.colorMode == index,
                            onSelect = { preferences.color(index) })
                        if (index == 0) CouiSettingsDivider()
                    }
                }
                "font" -> {
                    CouiSettingsGroup(Modifier.selectableGroup()) {
                        listOf(R.string.settings_size_small, R.string.settings_size_default, R.string.settings_size_large).forEachIndexed { index, label ->
                            DemoSelectPreference(title = stringResource(label), selected = preferences.fontSize == index,
                                onSelect = { preferences.font(index) })
                            if (index < 2) CouiSettingsDivider()
                        }
                    }
                    CouiSettingsGroup(footer = stringResource(R.string.settings_demo_only)) {
                        DemoPreference(title = stringResource(R.string.settings_preview))
                        BasicText(stringResource(R.string.settings_preview_summary), Modifier.padding(16.dp),
                            style = TextStyle(fontSize = listOf(14.sp, 16.sp, 20.sp)[preferences.fontSize.coerceIn(0, 2)],
                                color = MaterialTheme.colorScheme.onSurface))
                    }
                }
                "privacy" -> CouiSettingsGroup(footer = stringResource(R.string.settings_demo_only)) {
                    DemoPreference(title = stringResource(R.string.settings_local), summary = stringResource(R.string.settings_privacy_summary))
                    CouiSettingsDivider()
                    DemoPreference(title = stringResource(R.string.settings_disabled), enabled = false,
                        summary = stringResource(R.string.settings_disabled_summary), onClick = {})
                }
                "about" -> {
                    CouiSettingsGroup {
                        DemoPreference(title = stringResource(R.string.app_name), value = "1.0")
                        CouiSettingsDivider()
                        DemoPreference(title = stringResource(R.string.settings_baseline), value = "ColorOS 17")
                        CouiSettingsDivider()
                        DemoPreference(title = stringResource(R.string.home_release), value = "Release / R8")
                    }
                    CouiSettingsGroup {
                        DemoPreference(title = stringResource(R.string.settings_reset), onClick = { reset = true })
                    }
                }
                else -> if (page.startsWith("effect_") && entry != null) {
                    Column(Modifier.fillMaxWidth()) {
                        EffectsDemo(dark, stringResource(entry.title), shadowAvailable = Build.VERSION.SDK_INT >= 34)
                    }
                } else {
                    CouiSettingsGroup(title = stringResource(R.string.settings_appearance)) {
                        DemoSwitchPreference(title = stringResource(R.string.settings_dark), checked = dark, onCheckedChange = preferences::dark)
                        CouiSettingsDivider()
                        DemoPreference(title = stringResource(R.string.settings_color),
                            value = stringResource(if (preferences.colorMode == 0) R.string.settings_color_natural else R.string.settings_color_vivid),
                            onClick = { open("color") })
                        CouiSettingsDivider()
                        DemoPreference(title = stringResource(R.string.settings_font),
                            value = stringResource(R.string.settings_size_value, preferences.fontSize + 1), onClick = { open("font") })
                    }
                    CouiSettingsGroup(title = stringResource(R.string.settings_notifications), footer = stringResource(R.string.settings_dependency)) {
                        DemoSwitchPreference(title = stringResource(R.string.settings_allow_notifications), checked = preferences.notifications,
                            onCheckedChange = preferences::notifications)
                        CouiSettingsDivider()
                        DemoSwitchPreference(title = stringResource(R.string.settings_sound), checked = preferences.sound,
                            onCheckedChange = preferences::sound, enabled = preferences.notifications)
                        CouiSettingsDivider()
                        DemoSwitchPreference(title = stringResource(R.string.settings_automatic), checked = preferences.automatic,
                            onCheckedChange = preferences::automatic, enabled = preferences.notifications)
                    }
                    CouiSettingsGroup {
                        DemoPreference(title = stringResource(R.string.settings_privacy), onClick = { open("privacy") })
                        CouiSettingsDivider()
                        DemoPreference(title = stringResource(R.string.settings_about), onClick = { open("about") })
                    }
                    CouiSettingsFooter(text = stringResource(R.string.settings_demo_only))
                }
            }
            val backdrop = rememberCouiWindowBackdrop()
            if (reset) CouiAlertDialog(onDismissRequest = { reset = false }, dark = dark,
                title = stringResource(R.string.settings_reset), message = stringResource(R.string.settings_reset_message),
                confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), { preferences.reset(); reset = false },
                    style = CouiDialogActionStyle.Primary, colors = CouiDialogDefaults.primaryActionColors(accent)),
                dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), { reset = false }),
                material = if (Build.VERSION.SDK_INT >= 33 && backdrop != null) CouiDialogDefaults.material(dark) else null,
                backdrop = backdrop)
        }
    }
}
