package com.itos.coui.demo

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.itos.coui.settings.*
import kotlinx.coroutines.delay

@Composable
internal fun SwitchSample(dark: Boolean) {
    var first by rememberSaveable { mutableStateOf(false) }
    var second by rememberSaveable { mutableStateOf(true) }
    var row by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(loading) { if (loading) { delay(2200); row = pending; loading = false } }
    CouiSettingsGroup(title = stringResource(R.string.sample_enabled)) {
        DemoSwitchPreference(title = stringResource(R.string.settings_switch_drag), checked = first, onCheckedChange = { first = it })
        CouiSettingsDivider()
        DemoSwitchPreference(title = stringResource(R.string.catalog_switch_row), checked = second, onCheckedChange = { second = it },
            summary = stringResource(R.string.settings_sound_summary))
    }
    CouiSettingsGroup(title = stringResource(R.string.loading_switch)) {
        DemoSwitchPreference(title = stringResource(R.string.loading_switch), checked = row,
            onCheckedChange = { pending = it; loading = true }, summary = stringResource(R.string.loading_switch_summary),
            loading = loading, loadingDescription = stringResource(R.string.loading_switch_description), loadingStyle = true)
    }
    CouiSettingsGroup(title = stringResource(R.string.sample_disabled)) {
        DemoSwitchPreference(title = stringResource(R.string.checkbox_disabled_unchecked), checked = false, onCheckedChange = {}, enabled = false)
        CouiSettingsDivider()
        DemoSwitchPreference(title = stringResource(R.string.checkbox_disabled), checked = true, onCheckedChange = {}, enabled = false)
    }
}
