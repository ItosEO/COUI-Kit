package com.itos.coui.demo

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.layout.CouiGroup

@Composable
internal fun PopupRowSample(dark: Boolean) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val labels = listOf(stringResource(R.string.menu_row_option_one), stringResource(R.string.menu_row_option_two))
    CouiGroup(title = stringResource(R.string.menu_row_group)) {
        DemoPopupPreference(title = stringResource(R.string.menu_row_title), value = labels[selected],
            expanded = expanded, onExpandedChange = { expanded = it },
            items = labels.mapIndexed { index, title -> CouiMenuItem(index, title, icon = if (index == 0) DemoIcons.SimOne else DemoIcons.SimTwo,
                iconTint = Color(if (index == 0) 0xFF27B73D else 0xFF1A8CFF), selected = index == selected) },
            onItemClick = { selected = it.value }, menuStyle = demoMenuStyle(dark))
    }
}
