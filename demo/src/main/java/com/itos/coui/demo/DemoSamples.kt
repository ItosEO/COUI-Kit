package com.itos.coui.demo

import android.os.Build
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.AlertDialog
import com.itos.coui.theme.CouiTheme
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.icons.CouiIcons
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
import androidx.compose.foundation.layout.heightIn
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
import com.itos.coui.menu.Menu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import kotlin.math.roundToInt

@Composable
internal fun ComponentDetail(page: String, dark: Boolean, onToggleTheme: () -> Unit) {
    when (page) {
        "switch" -> SwitchSample(dark)
        "sliders" -> SliderSample()
        "menu" -> {
            PopupRowSample(dark)
            Group(title = stringResource(R.string.menu_actions)) { MenuSample(dark) }
        }
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
private fun MenuSample(dark: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    var selected by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(0) }
    var pinned by rememberSaveable { mutableStateOf(false) }
    var longMenu by rememberSaveable { mutableStateOf(false) }
    val anchor = rememberCouiMenuAnchor()
    val first = stringResource(R.string.menu_first)
    val second = stringResource(R.string.menu_second)
    val longLabels = (1..12).map { stringResource(R.string.menu_item_number, it) }
    DemoPreference(title = stringResource(R.string.menu_selection_label),
        value = if (longMenu) longLabels[selected] else if (selected == 0) first else second)
    Divider()
    DemoSwitchPreference(title = stringResource(R.string.scroll_menu), checked = longMenu, onCheckedChange = { longMenu = it; selected = 0 })
    Divider()
    Box {
        DemoPreference(modifier = Modifier.couiMenuAnchor(anchor),
            title = stringResource(R.string.open_menu), summary = stringResource(R.string.menu_open_summary),
            onClick = { anchor.open { expanded = true } })
        Menu(expanded = expanded, onDismissRequest = { expanded = false }, anchor = anchor,
            modifier = Modifier.heightIn(max = 320.dp),
            style = demoMenuStyle(dark), headerText = stringResource(R.string.menu_header),
            items = if (longMenu) longLabels.mapIndexed { index, label ->
                CouiMenuItem(index, label, icon = CouiIcons.Settings, selected = selected == index,
                    dividerBefore = index > 0 && index % 3 == 0)
            } else listOf(
                CouiMenuItem(-1, stringResource(R.string.menu_pinned), checked = pinned),
                CouiMenuItem(0, first, icon = CouiIcons.Settings, selected = selected == 0, dividerBefore = true),
                CouiMenuItem(-2, stringResource(R.string.menu_more),
                    children = listOf(CouiMenuItem(1, second, selected = selected == 1),
                        CouiMenuItem(-3, stringResource(R.string.menu_disabled), enabled = false))),
            ),
            onItemClick = { item ->
                if (item.value == -1) pinned = !pinned else selected = item.value
                expanded = false
            })
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
    Group {
        DemoPreference(title = stringResource(R.string.settings_about), onClick = onAbout)
        Divider()
        DemoPreference(title = stringResource(R.string.settings_reset), onClick = { reset = true })
    }
    val backdrop = rememberCouiWindowBackdrop()
    val accent = CouiTheme.colorScheme.accent
    if (reset) AlertDialog(onDismissRequest = { reset = false }, dark = dark,
        title = stringResource(R.string.settings_reset), message = stringResource(R.string.settings_reset_message),
        confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm),
            { onReset(); reset = false }, style = CouiDialogActionStyle.Primary,
            colors = CouiDialogDefaults.primaryActionColors(accent)),
        dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), { reset = false }),
        material = if (Build.VERSION.SDK_INT >= 33 && backdrop != null)
            CouiDialogDefaults.material(dark) else null,
        backdrop = backdrop)
}
