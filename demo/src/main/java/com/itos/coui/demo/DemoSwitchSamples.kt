package com.itos.coui.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.itos.coui.switch.CouiSwitch
import kotlinx.coroutines.delay

@Composable
internal fun SwitchSample() {
    var checked by rememberSaveable { mutableStateOf(false) }
    var described by rememberSaveable { mutableStateOf(true) }
    var loadingChecked by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    // 仅模拟业务等待；加载仍是同一组件的状态，完成后才提交业务值。
    LaunchedEffect(loading) {
        if (loading) {
            delay(2200)
            loadingChecked = pending
            loading = false
        }
    }
    val loadingDescription = stringResource(R.string.switch_sample_loading_description)
    SwitchExampleRow(title = stringResource(R.string.switch_sample_toggle), summary = stringResource(R.string.switch_sample_toggle_summary)) { label ->
        CouiSwitch(Modifier.semantics { contentDescription = label }, checked, { checked = it })
    }
    SwitchExampleRow(title = stringResource(R.string.switch_sample_description), summary = stringResource(R.string.switch_sample_description_summary)) { label ->
        CouiSwitch(Modifier.semantics { contentDescription = label }, described, { described = it })
    }
    SwitchExampleRow(title = stringResource(R.string.switch_sample_loading), summary = stringResource(R.string.switch_sample_loading_summary)) { label ->
        CouiSwitch(Modifier.semantics { contentDescription = label }, loadingChecked,
            onCheckedChange = { pending = it; loading = true },
            onDragCheckedChange = null, loading = loading, loadingDescription = loadingDescription)
    }
    SwitchExampleRow(title = stringResource(R.string.switch_sample_disabled_off)) { label ->
        CouiSwitch(Modifier.semantics { contentDescription = label }, false, {}, enabled = false)
    }
    SwitchExampleRow(title = stringResource(R.string.switch_sample_disabled_on)) { label ->
        CouiSwitch(Modifier.semantics { contentDescription = label }, true, {}, enabled = false)
    }
}

@Composable
private fun SwitchExampleRow(modifier: Modifier = Modifier, title: String, summary: String? = null,
    content: @Composable (String) -> Unit) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) Text(summary, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        content(title)
    }
}
