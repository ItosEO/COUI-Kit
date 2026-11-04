package com.itos.coui.demo

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Footer
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item

/** 只展示 Group、Item、Divider、Footer 和选择语义；其他组件在各自页面中验证。 */
@Composable
internal fun ListSamples(open: (String) -> Unit) {
    var headerActions by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var enabled by rememberSaveable { mutableStateOf(true) }

    Group(title = stringResource(R.string.settings_group_example),
        footer = stringResource(R.string.settings_header_count, headerActions),
        headerActions = {
            TextButton(onClick = { headerActions++ }) {
                Text(stringResource(R.string.settings_header_action))
            }
        }) {
        DemoPreference(title = stringResource(R.string.app_name),
            summary = stringResource(R.string.settings_long_summary), value = "ColorOS 17",
            icon = { Icon(CouiIcons.Info, null) })
        Divider(startIndent = CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
        DemoPreference(title = stringResource(R.string.list_item_action),
            summary = stringResource(R.string.settings_font),
            value = stringResource(R.string.settings_size_default),
            onClick = { open("font") })
        Divider()
        DemoPreference(title = stringResource(R.string.settings_disabled),
            summary = stringResource(R.string.settings_disabled_summary),
            enabled = false, onClick = {})
    }
    Group(Modifier.selectableGroup(), title = stringResource(R.string.list_item_single)) {
        listOf(R.string.settings_size_small, R.string.settings_size_large).forEachIndexed { index, label ->
            DemoSelectPreference(title = stringResource(label), selected = selected == index,
                onSelect = { selected = index })
            if (index == 0) Divider()
        }
    }
    Group(title = stringResource(R.string.settings_notifications)) {
        DemoSwitchPreference(title = stringResource(R.string.settings_allow_notifications), checked = enabled,
            onCheckedChange = { enabled = it })
        Divider()
        Item(enabled = enabled, onClick = { open("appsettings") },
            summary = { Text(stringResource(R.string.settings_dependency)) }) {
            Text(stringResource(R.string.settings_automatic))
        }
    }
    Group(title = stringResource(R.string.settings_preview)) {
        Text(stringResource(R.string.list_item_text_preview), Modifier.padding(16.dp),
            style = TextStyle(fontSize = if (selected == 0) 14.sp else 20.sp))
    }
    Footer(text = stringResource(R.string.settings_demo_only))
}
