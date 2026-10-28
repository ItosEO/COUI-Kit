package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.checkbox.CouiCheckbox
import com.itos.coui.checkbox.CouiCheckboxDefaults
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.menu.CouiMenu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.slider.CouiSlider
import com.itos.coui.slider.CouiSliderDefaults
import kotlin.math.roundToInt

@Composable
internal fun DemoSection(title: Int, query: String, content: @Composable ColumnScope.() -> Unit) {
    val heading = stringResource(title)
    val state = rememberSaveableStateHolder()
    if (query.isNotBlank() && !heading.contains(query.trim(), ignoreCase = true)) return
    state.SaveableStateProvider(title) {
        Column(Modifier.widthIn(max = 840.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(heading, Modifier.padding(horizontal = 4.dp), style = MaterialTheme.typography.titleMedium)
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceBright, DemoShapes.card).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}

@Composable
private fun DemoCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    dark: Boolean, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().clip(DemoShapes.item).heightIn(min = 48.dp).toggleable(checked,
        interactionSource = remember { MutableInteractionSource() }, indication = null, enabled = enabled, role = Role.Checkbox,
        onValueChange = onCheckedChange).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant)
        CouiCheckbox(modifier = Modifier.size(24.dp), checked = checked, enabled = enabled,
            shape = DemoShapes.checkbox, borderShape = DemoShapes.checkboxBorder,
            colors = CouiCheckboxDefaults.colors(if (dark) MaterialTheme.colorScheme.inversePrimary
                else MaterialTheme.colorScheme.primary, dark))
    }
}

@Composable
internal fun ControlsDemo(dark: Boolean, query: String) {
    DemoSection(R.string.dialog_title, query) { DialogSample(dark) }
    var value by rememberSaveable { mutableFloatStateOf(0.4f) }
    var section by rememberSaveable { mutableFloatStateOf(2f) }
    var implicitStep by rememberSaveable { mutableFloatStateOf(5f) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var checked by rememberSaveable { mutableStateOf(false) }
    DemoSection(R.string.slider_title, query) {
        DemoSwitchRow(stringResource(R.string.enable_controls), enabled, { enabled = it })
        Text(stringResource(R.string.continuous_value, (value * 100).roundToInt()))
        CouiSlider(modifier = Modifier.fillMaxWidth().demoLabel(R.string.continuous_slider), value = value,
            onValueChange = { value = it }, enabled = enabled, dark = dark,
            colors = CouiSliderDefaults.colors(MaterialTheme.colorScheme.primary, dark, enabled), shape = DemoShapes.capsule)
        Text(stringResource(R.string.section_value, section.roundToInt()))
        CouiSlider(modifier = Modifier.fillMaxWidth().demoLabel(R.string.section_slider), value = section,
            onValueChange = { section = it }, enabled = enabled, dark = dark,
            colors = CouiSliderDefaults.colors(MaterialTheme.colorScheme.primary, dark, enabled), shape = DemoShapes.capsule,
            valueRange = 0f..4f, steps = 3, showTickMarks = true)
        Text(stringResource(R.string.implicit_step_value, implicitStep.roundToInt()))
        CouiSlider(modifier = Modifier.fillMaxWidth().demoLabel(R.string.implicit_slider), value = implicitStep,
            onValueChange = { implicitStep = it }, enabled = enabled, dark = dark,
            colors = CouiSliderDefaults.colors(MaterialTheme.colorScheme.primary, dark, enabled), shape = DemoShapes.capsule,
            valueRange = 0f..10f, steps = 9, showTickMarks = false)
    }
    DemoSection(R.string.checkbox_title, query) {
        DemoCheckboxRow(stringResource(R.string.checkbox_toggle), checked, { checked = it }, dark)
        DemoCheckboxRow(stringResource(R.string.checkbox_disabled), true, {}, dark, enabled = false)
        DemoCheckboxRow(stringResource(R.string.checkbox_disabled_unchecked), false, {}, dark, enabled = false)
    }
    DemoSection(R.string.switch_title, query) { SwitchSample(dark) }
    DemoSection(R.string.menu_title, query) { MenuSample(dark) }
    DemoSection(R.string.chip_tab_title, query) { ChipTabSample(dark) }
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
    Text(stringResource(R.string.menu_selection, if (longMenu) longLabels[selected] else if (selected == 0) first else second))
    DemoSwitchRow(stringResource(R.string.scroll_menu), longMenu, { longMenu = it; selected = 0 })
    Box {
        FilledTonalButton(modifier = Modifier.couiMenuAnchor(anchor), shape = DemoShapes.capsule,
            onClick = { anchor.open { expanded = true } }) {
            Text(stringResource(R.string.open_menu))
        }
        CouiMenu(modifier = if (longMenu) Modifier.heightIn(max = 320.dp) else Modifier,
            expanded = expanded, onDismissRequest = { expanded = false }, anchor = anchor,
            style = demoMenuStyle(dark), headerText = stringResource(R.string.menu_header),
            items = if (longMenu) longLabels.mapIndexed { index, label ->
                CouiMenuItem(index, label, icon = Icons.Default.Tune, selected = selected == index,
                    dividerBefore = index > 0 && index % 3 == 0)
            } else listOf(
                CouiMenuItem(0, first, icon = Icons.Default.Tune, selected = selected == 0),
                CouiMenuItem(-1, stringResource(R.string.menu_pinned), icon = Icons.Default.Check, checked = pinned),
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
        Text(stringResource(R.string.shadow_description))
        if (Build.VERSION.SDK_INT >= 34 && shadowAvailable) {
            ShadowSamples(dark)
        } else if (Build.VERSION.SDK_INT < 34) {
            Text(stringResource(R.string.requires_api_34))
        } else {
            Text(stringResource(R.string.renderer_unavailable), color = MaterialTheme.colorScheme.error)
        }
    }
    DemoSection(R.string.fusion_title, query) {
        RecordSwipeSamples()
    }
    FoundationEffectsDemo(dark, query)
}

@Composable
internal fun PresetsDemo(dark: Boolean, query: String, contour: Boolean, onContourChange: (Boolean) -> Unit,
    leftAccessory: Boolean, onLeftAccessoryChange: (Boolean) -> Unit,
    rightAccessory: Boolean, onRightAccessoryChange: (Boolean) -> Unit,
    toolbarEnabled: Boolean, onToolbarEnabledChange: (Boolean) -> Unit, preset: CouiFabColor,
    onPresetChange: (CouiFabColor) -> Unit) {
    DemoSection(R.string.presets_title, query) {
        Column(Modifier.selectableGroup()) { CouiFabColor.entries.forEach { option ->
            val label = stringResource(when (option) {
                CouiFabColor.Notes -> R.string.preset_notes
                CouiFabColor.Phone -> R.string.preset_phone
                CouiFabColor.InfraredRemote -> R.string.preset_remote
                CouiFabColor.Recorder -> R.string.preset_recorder
            })
            DemoChoiceRow(label, preset == option, { onPresetChange(option) }) {
                Box(Modifier.size(24.dp).background(CouiFabDefaults.containerColor(option, dark), DemoShapes.capsule))
            }
        } }
        DemoSwitchRow(stringResource(R.string.contour_toggle), contour, onContourChange)
        DemoSwitchRow(stringResource(R.string.left_accessory_toggle), leftAccessory, onLeftAccessoryChange)
        DemoSwitchRow(stringResource(R.string.right_accessory_toggle), rightAccessory, onRightAccessoryChange)
        DemoSwitchRow(stringResource(R.string.toolbar_enabled), toolbarEnabled, onToolbarEnabledChange)
    }
}
