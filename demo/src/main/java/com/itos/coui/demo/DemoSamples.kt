package com.itos.coui.demo

import android.os.Build
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.Dialog
import com.itos.coui.menu.rememberCouiWindowBackdrop
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import com.itos.coui.checkbox.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import kotlin.math.roundToInt

@Composable
internal fun ComponentDetail(page: String, dark: Boolean, onToggleTheme: () -> Unit) {
    when (page) {
        "switch" -> SwitchSample(dark)
        "sliders" -> SliderSample()
        "menu" -> MenuSamples(dark)
        "dialogs" -> DialogSample(dark, onToggleTheme)
    }
    if (page == "checkbox") {
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var checked by rememberSaveable { mutableStateOf(false) }
        var standalone by rememberSaveable { mutableStateOf(false) }
        Group(Modifier.selectableGroup(), title = stringResource(R.string.radio_group_title)) {
            DemoSelectPreference(title = stringResource(R.string.radio_first), selected = selected == 0, onSelect = { selected = 0 })
            Divider()
            DemoSelectPreference(title = stringResource(R.string.radio_second), selected = selected == 1, onSelect = { selected = 1 })
        }
        Group(Modifier.selectableGroup()) {
            DemoSelectPreference(title = stringResource(R.string.checkbox_disabled), selected = true, onSelect = {}, enabled = false)
            Divider()
            DemoSelectPreference(title = stringResource(R.string.checkbox_disabled_unchecked), selected = false, onSelect = {}, enabled = false)
        }
        Group(title = stringResource(R.string.checkbox_standalone)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.checkbox_standalone))
                Box(Modifier.size(48.dp).toggleable(value = standalone,
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                    role = Role.Checkbox, onValueChange = { standalone = it }),
                    contentAlignment = Alignment.Center) {
                    Checkbox(modifier = Modifier.size(24.dp), checked = standalone)
                }
            }
        }
        Group {
            DemoCheckboxPreference(title = stringResource(R.string.checkbox_toggle), checked = checked, onCheckedChange = { checked = it })
            Divider()
            DemoCheckboxPreference(title = stringResource(R.string.checkbox_disabled), checked = true, onCheckedChange = {}, enabled = false)
            Divider()
            DemoCheckboxPreference(title = stringResource(R.string.checkbox_disabled_unchecked), checked = false, onCheckedChange = {}, enabled = false)
        }
    }
}

@Composable
private fun SliderSample() {
    var value by rememberSaveable { mutableFloatStateOf(.4f) }
    var section by rememberSaveable { mutableFloatStateOf(2f) }
    var implicitStep by rememberSaveable { mutableFloatStateOf(5f) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    Group {
        DemoSwitchPreference(title = stringResource(R.string.enable_controls), checked = enabled, onCheckedChange = { enabled = it })
    }
    Group {
        DemoSliderPreference(title = stringResource(R.string.continuous_slider),
            value = value, onValueChange = { value = it }, valueText = "${(value * 100).roundToInt()}%",
            enabled = enabled)
        Divider()
        DemoSliderPreference(title = stringResource(R.string.section_slider),
            value = section, onValueChange = { section = it }, valueText = section.roundToInt().toString(),
            valueRange = 0f..4f, steps = 3, showTickMarks = true, enabled = enabled)
        Divider()
        DemoSliderPreference(title = stringResource(R.string.implicit_slider),
            value = implicitStep, onValueChange = { implicitStep = it }, valueText = implicitStep.roundToInt().toString(),
            valueRange = 0f..10f, steps = 9, showTickMarks = false, enabled = enabled)
    }
}

@Composable
internal fun SettingsDemo(dark: Boolean, preferences: DemoPreferences,
    leftAccessory: Boolean, onLeftAccessoryChange: (Boolean) -> Unit,
    rightAccessory: Boolean, onRightAccessoryChange: (Boolean) -> Unit,
    onAbout: () -> Unit, onReset: () -> Unit) {
    var reset by rememberSaveable { mutableStateOf(false) }
    Group(title = stringResource(R.string.demo_theme_settings)) {
        DemoSwitchPreference(title = stringResource(R.string.material_theme_toggle), checked = preferences.materialTheme,
            onCheckedChange = preferences::materialTheme)
    }
    Group(title = stringResource(R.string.demo_navigation_settings)) {
        DemoSwitchPreference(title = stringResource(R.string.contour_toggle), checked = preferences.contour,
            onCheckedChange = preferences::contour)
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.left_accessory_toggle), checked = leftAccessory,
            onCheckedChange = onLeftAccessoryChange)
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.right_accessory_toggle), checked = rightAccessory,
            onCheckedChange = onRightAccessoryChange)
    }
    val progressiveBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    Group(title = stringResource(R.string.progressive_blur_comparison)) {
        DemoSwitchPreference(
            title = stringResource(R.string.compose_progressive_blur),
            summary = stringResource(if (progressiveBlurSupported) R.string.compose_progressive_blur_summary else R.string.requires_api_33),
            checked = progressiveBlurSupported && preferences.composeProgressiveBlur,
            onCheckedChange = preferences::composeProgressiveBlur,
            enabled = progressiveBlurSupported,
        )
    }
    Group {
        DemoPreference(title = stringResource(R.string.settings_about), onClick = onAbout)
        Divider()
        DemoPreference(title = stringResource(R.string.settings_reset), onClick = { reset = true })
    }
    val backdrop = rememberCouiWindowBackdrop()
    Dialog(visible = reset, onDismissRequest = { reset = false }, dark = dark,
        title = stringResource(R.string.settings_reset), message = stringResource(R.string.settings_reset_message),
        confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm),
            { onReset(); reset = false }, style = CouiDialogActionStyle.Primary),
        dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), { reset = false }),
        material = demoDialogMaterial(backdrop),
        backdrop = backdrop)
}
