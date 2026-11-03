package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import com.itos.coui.layout.CouiDivider
import com.itos.coui.layout.CouiGroup
import kotlin.math.roundToInt

@Composable
internal fun DemoSection(title: Int, query: String, padded: Boolean = true, content: @Composable ColumnScope.() -> Unit) {
    val headingText = stringResource(title)
    val state = rememberSaveableStateHolder()
    if (query.isNotBlank() && !headingText.contains(query.trim(), ignoreCase = true)) return
    state.SaveableStateProvider(title) {
        CouiGroup(title = headingText) {
            if (padded) Column(Modifier.fillMaxWidth(), content = content)
            else content()
        }
    }
}

@Composable
private fun DemoCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    dark: Boolean, enabled: Boolean = true) {
    DemoCheckboxPreference(title = label, checked = checked,
        onCheckedChange = onCheckedChange, enabled = enabled)
}

@Composable
internal fun ComponentDetail(page: String, dark: Boolean, onToggleTheme: () -> Unit) {
    when (page) {
        "switch" -> SwitchSample(dark)
        "sliders" -> SliderSample(dark)
        "menu" -> {
            CouiGroup(title = stringResource(R.string.menu_overflow_preset)) {
                DemoPreference(title = stringResource(R.string.menu_toolbar_more),
                    summary = stringResource(R.string.menu_overflow_preset_summary))
            }
            PopupRowSample(dark)
            CouiGroup(title = stringResource(R.string.menu_actions)) { MenuSample(dark) }
        }
        "dialogs" -> DialogSample(dark, onToggleTheme)
    }
    if (page == "checkbox") {
        var checked by rememberSaveable { mutableStateOf(false) }
        CouiGroup {
            DemoCheckboxRow(stringResource(R.string.checkbox_toggle), checked, { checked = it }, dark)
            CouiDivider()
            DemoCheckboxRow(stringResource(R.string.checkbox_disabled), true, {}, dark, enabled = false)
            CouiDivider()
            DemoCheckboxRow(stringResource(R.string.checkbox_disabled_unchecked), false, {}, dark, enabled = false)
        }
    }
}

@Composable
private fun SliderSample(dark: Boolean) {
    var value by rememberSaveable { mutableFloatStateOf(.4f) }
    var section by rememberSaveable { mutableFloatStateOf(2f) }
    var implicitStep by rememberSaveable { mutableFloatStateOf(5f) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    CouiGroup {
        DemoSwitchRow(stringResource(R.string.enable_controls), enabled, { enabled = it })
    }
    CouiGroup {
        DemoSliderPreference(title = stringResource(R.string.continuous_slider),
            value = value, onValueChange = { value = it }, valueText = "${(value * 100).roundToInt()}%", enabled = enabled)
        CouiDivider()
        DemoSliderPreference(title = stringResource(R.string.section_slider),
            value = section, onValueChange = { section = it }, valueText = section.roundToInt().toString(),
            valueRange = 0f..4f, steps = 3, showTickMarks = true, enabled = enabled)
        CouiDivider()
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
    CouiDivider()
    DemoSwitchRow(stringResource(R.string.scroll_menu), longMenu, { longMenu = it; selected = 0 })
    CouiDivider()
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
internal fun EffectsDemo(dark: Boolean, query: String, shadowAvailable: Boolean) {
    DemoSection(R.string.shadow_title, query) {
        EffectText(stringResource(R.string.shadow_description))
        if (Build.VERSION.SDK_INT >= 34 && shadowAvailable) {
            ShadowSamples(dark)
        } else if (Build.VERSION.SDK_INT < 34) {
            EffectText(stringResource(R.string.requires_api_34))
        } else {
            EffectText(stringResource(R.string.renderer_unavailable), color = MaterialTheme.colorScheme.error)
        }
    }
    DemoSection(R.string.fusion_title, query) {
        if (Build.VERSION.SDK_INT >= 33) RecordSwipeSamples(dark) else EffectText(stringResource(R.string.requires_api_33))
    }
    FoundationEffectsDemo(dark, query)
}

@Composable
internal fun PresetsDemo(dark: Boolean, query: String, contour: Boolean, onContourChange: (Boolean) -> Unit,
    leftAccessory: Boolean, onLeftAccessoryChange: (Boolean) -> Unit,
    rightAccessory: Boolean, onRightAccessoryChange: (Boolean) -> Unit,
    preset: CouiFabColor, onPresetChange: (CouiFabColor) -> Unit,
    materialTheme: Boolean, onMaterialThemeChange: (Boolean) -> Unit, enabled: Boolean = true) {
    DemoSection(R.string.presets_title, query, padded = false) {
        DemoSwitchRow(stringResource(R.string.material_theme_toggle), materialTheme, onMaterialThemeChange, enabled = enabled)
        CouiDivider()
        Column(Modifier.selectableGroup()) {
            CouiFabColor.entries.forEach { option ->
                val label = stringResource(when (option) {
                    CouiFabColor.Notes -> R.string.preset_notes
                    CouiFabColor.Phone -> R.string.preset_phone
                    CouiFabColor.InfraredRemote -> R.string.preset_remote
                    CouiFabColor.Recorder -> R.string.preset_recorder
                })
                DemoChoiceRow(label, preset == option, { onPresetChange(option) }, enabled = enabled) {
                    Box(Modifier.size(24.dp).background(CouiFabDefaults.containerColor(option, dark), DemoShapes.capsule))
                }
            }
        }
        CouiDivider()
        DemoSwitchRow(stringResource(R.string.contour_toggle), contour, onContourChange, enabled = enabled)
        CouiDivider()
        DemoSwitchRow(stringResource(R.string.left_accessory_toggle), leftAccessory, onLeftAccessoryChange, enabled = enabled)
        CouiDivider()
        DemoSwitchRow(stringResource(R.string.right_accessory_toggle), rightAccessory, onRightAccessoryChange, enabled = enabled)
    }
}
