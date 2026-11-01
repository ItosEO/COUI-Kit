package com.itos.coui.demo

import androidx.compose.runtime.Composable

@Composable
internal fun DemoSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    DemoSwitchPreference(title = label, checked = checked, onCheckedChange = onCheckedChange)
}

@Composable
internal fun DemoChoiceRow(label: String, selected: Boolean, onSelect: () -> Unit,
    leading: (@Composable () -> Unit)? = null) {
    DemoSelectPreference(title = label, selected = selected, onSelect = onSelect, icon = leading)
}
