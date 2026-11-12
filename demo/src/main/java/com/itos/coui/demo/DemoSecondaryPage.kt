package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.floatingactionbutton.CouiFabColor
import androidx.compose.ui.Alignment
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
@Composable
internal fun DemoSecondaryPage(page: String, dark: Boolean, preferences: DemoPreferences,
    back: () -> Unit) {
    val toggleTheme = { preferences.dark(!dark) }
    var fabPreset by rememberSaveable { mutableStateOf(CouiFabColor.Notes) }
    var roundedFab by rememberSaveable { mutableStateOf(false) }
    val catalogPage = if (DemoWheelPickerEntries.any { it.page == page }) "wheel" else page
    val entry = (DemoComponentGroups + DemoEffectGroups).flatten().firstOrNull { it.page == catalogPage }
    val title = entry?.title ?: R.string.settings_about
    val floatingAction: (@Composable BoxScope.() -> Unit)? = if (page == "fab") {
        {
            FabSampleButton(Modifier.align(Alignment.BottomEnd).padding(24.dp),
                dark = dark, preset = fabPreset, rounded = roundedFab)
        }
    } else null
    CompositionLocalProvider(LocalCouiContourGlowEnabled provides preferences.contour) {
        DemoSettingsPage(title = stringResource(title), backDescription = stringResource(R.string.settings_back),
            onBack = back, dark = dark, colors = CouiPageDefaults.themeColors(dark), floatingAction = floatingAction) {
            when (page) {
                "sliders", "switch", "checkbox", "dialogs", "menu" -> ComponentDetail(page, dark, toggleTheme)
                "chip" -> ChipTabSample(dark)
                "segment" -> SegmentedControlSample(dark)
                "effect_backdrop" -> BlurTutorial(dark)
                "effect_shadow" -> Group(title = stringResource(R.string.shadow_title)) {
                    if (Build.VERSION.SDK_INT >= 34) ShadowSamples(dark)
                    else EffectText(stringResource(R.string.requires_api_34))
                }
                "effect_fusion" -> RecordSwipeSamples(dark)
                "effect_material" -> Group(title = stringResource(R.string.material_title)) {
                    if (Build.VERSION.SDK_INT >= 33) MaterialSample(dark)
                    else EffectText(stringResource(R.string.requires_api_33))
                }
                "wheel", "timepicker", "countdownpicker", "datepicker", "calendar" -> PickerSamples(page, dark)
                "appbar", "search", "navigation" -> ChromeDetail(page, dark)
                "fab" -> FabSample(preset = fabPreset, onPresetChange = { fabPreset = it },
                    rounded = roundedFab, onRoundedChange = { roundedFab = it })
                "floatingtoolbar" -> FloatingToolbarSample(dark)
                "about" -> {
                    DemoAboutHero()
                    Group {
                        DemoPreference(title = stringResource(R.string.app_name), value = "1.0")
                        Divider()
                        DemoPreference(title = stringResource(R.string.settings_baseline), value = "ColorOS 17")
                    }
                }
            }
        }
    }
}
