package com.itos.coui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.menu.CouiMenu
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
            Group(title = stringResource(R.string.menu_overflow_preset)) {
                DemoPreference(title = stringResource(R.string.menu_toolbar_more),
                    summary = stringResource(R.string.menu_overflow_preset_summary))
            }
            PopupRowSample(dark)
            Group(title = stringResource(R.string.menu_actions)) { MenuSample(dark) }
        }
        "dialogs" -> DialogSample(dark, onToggleTheme)
    }
    if (page == "checkbox") {
        var checked by rememberSaveable { mutableStateOf(false) }
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
            value = value, onValueChange = { value = it }, valueText = "${(value * 100).roundToInt()}%", enabled = enabled)
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
        CouiMenu(expanded = expanded, onDismissRequest = { expanded = false }, anchor = anchor,
            modifier = Modifier.heightIn(max = 320.dp),
            style = demoMenuStyle(dark), headerText = stringResource(R.string.menu_header),
            items = if (longMenu) longLabels.mapIndexed { index, label ->
                CouiMenuItem(index, label, icon = DemoIcons.Settings, selected = selected == index,
                    dividerBefore = index > 0 && index % 3 == 0)
            } else listOf(
                CouiMenuItem(0, first, icon = DemoIcons.Settings, selected = selected == 0),
                CouiMenuItem(-1, stringResource(R.string.menu_pinned), icon = DemoIcons.Check, checked = pinned),
                CouiMenuItem(-2, stringResource(R.string.menu_more), dividerBefore = true,
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
internal fun PresetsDemo(dark: Boolean, contour: Boolean, onContourChange: (Boolean) -> Unit,
    leftAccessory: Boolean, onLeftAccessoryChange: (Boolean) -> Unit,
    rightAccessory: Boolean, onRightAccessoryChange: (Boolean) -> Unit,
    preset: CouiFabColor, onPresetChange: (CouiFabColor) -> Unit,
    materialTheme: Boolean, onMaterialThemeChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Group(title = stringResource(R.string.presets_title)) {
        DemoSwitchPreference(title = stringResource(R.string.material_theme_toggle), checked = materialTheme,
            onCheckedChange = onMaterialThemeChange, enabled = enabled)
        Divider()
        Column(Modifier.selectableGroup()) {
            CouiFabColor.entries.forEach { option ->
                val label = stringResource(when (option) {
                    CouiFabColor.Notes -> R.string.preset_notes
                    CouiFabColor.Phone -> R.string.preset_phone
                    CouiFabColor.InfraredRemote -> R.string.preset_remote
                    CouiFabColor.Recorder -> R.string.preset_recorder
                })
                DemoSelectPreference(title = label, selected = preset == option, onSelect = { onPresetChange(option) }, enabled = enabled, icon = {
                    Box(Modifier.size(24.dp).background(CouiFabDefaults.containerColor(option, dark), DemoShapes.capsule))
                })
            }
        }
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.contour_toggle), checked = contour, onCheckedChange = onContourChange, enabled = enabled)
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.left_accessory_toggle), checked = leftAccessory,
            onCheckedChange = onLeftAccessoryChange, enabled = enabled)
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.right_accessory_toggle), checked = rightAccessory,
            onCheckedChange = onRightAccessoryChange, enabled = enabled)
    }
}
