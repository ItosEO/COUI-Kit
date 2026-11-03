package com.itos.coui.demo

import androidx.compose.runtime.Composable

@Composable
internal fun DemoSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    DemoSwitchPreference(title = label, checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
}

@Composable
internal fun DemoChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit,
    enabled: Boolean = true, leading: (@Composable () -> Unit)? = null) {
    DemoSelectPreference(title = label, selected = selected, onSelect = onSelect, enabled = enabled, icon = leading)
}
