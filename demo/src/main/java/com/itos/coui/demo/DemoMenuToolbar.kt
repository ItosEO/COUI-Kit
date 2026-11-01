package com.itos.coui.demo

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.itos.coui.menu.CouiMenuItem
@Composable
internal fun MenuToolbarAction(dark: Boolean, preferences: DemoPreferences, open: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    DemoSettingsOverflowMenu(expanded = expanded, onExpandedChange = { expanded = it },
        contentDescription = stringResource(R.string.menu_toolbar_more), style = demoMenuStyle(dark),
        items = listOf(CouiMenuItem(0, stringResource(R.string.menu_light), selected = !dark),
            CouiMenuItem(1, stringResource(R.string.menu_dark), selected = dark),
            CouiMenuItem(2, stringResource(R.string.settings_about), dividerBefore = true)),
        onItemClick = { if (it.value == 2) open("about") else preferences.dark(it.value == 1) })
}
