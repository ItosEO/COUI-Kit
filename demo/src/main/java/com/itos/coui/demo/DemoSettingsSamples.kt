package com.itos.coui.demo

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.switch.Switch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.itos.coui.layout.*
import kotlinx.coroutines.delay

@Composable
internal fun SwitchSample(dark: Boolean) {
    var first by rememberSaveable { mutableStateOf(false) }
    var row by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf(false) }
    var standalone by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(loading) { if (loading) { delay(2200); row = pending; loading = false } }
    Group(title = stringResource(R.string.sample_enabled)) {
        DemoSwitchPreference(title = stringResource(R.string.settings_switch_drag), checked = first, onCheckedChange = { first = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.loading_switch), checked = row,
            onCheckedChange = { pending = it; loading = true }, summary = stringResource(R.string.loading_switch_summary),
            loading = loading, loadingDescription = stringResource(R.string.loading_switch_description))
    }
    Group(title = stringResource(R.string.switch_standalone)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.switch_standalone))
            Switch(checked = standalone, onCheckedChange = { standalone = it })
        }
    }
    Group(title = stringResource(R.string.sample_disabled)) {
        DemoSwitchPreference(title = stringResource(R.string.checkbox_disabled_unchecked), checked = false, onCheckedChange = {}, enabled = false)
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.checkbox_disabled), checked = true, onCheckedChange = {}, enabled = false)
    }
}
