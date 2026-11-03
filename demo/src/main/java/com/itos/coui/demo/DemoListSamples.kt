package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.*
import com.itos.coui.dialog.*
import com.itos.coui.menu.rememberCouiWindowBackdrop
import android.os.Build
import kotlinx.coroutines.delay

/** 设置列表展示基础形态与组合，不在这里重复菜单页的 Popup 样例。 */
@Composable
internal fun ListSamples(dark: Boolean, open: (String) -> Unit) {
    var choice by rememberSaveable { mutableIntStateOf(0) }
    var first by rememberSaveable { mutableStateOf(true) }
    var second by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var draft by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable { mutableStateOf(false) }
    var count by rememberSaveable { mutableIntStateOf(3) }
    var progress by rememberSaveable { mutableFloatStateOf(.35f) }
    var running by rememberSaveable { mutableStateOf(false) }
    val backdrop = rememberCouiWindowBackdrop()
    LaunchedEffect(running) {
        while (running && progress < 1f) { delay(250); progress = (progress + .02f).coerceAtMost(1f) }
        if (progress >= 1f) running = false
    }
    Group(title = stringResource(R.string.list_item_info)) {
        DemoPreference(title = stringResource(R.string.app_name), value = "ColorOS 17", icon = { Icon(CouiIcons.Info, null) })
        Divider(startIndent = CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
        DemoPreference(title = stringResource(R.string.settings_local), summary = stringResource(R.string.settings_privacy_summary))
    }
    Group {
        DemoPreference(title = stringResource(R.string.list_item_action), summary = stringResource(R.string.settings_font),
            value = stringResource(R.string.settings_size_default), onClick = { open("font") })
        Divider()
        DemoPreference(title = stringResource(R.string.list_item_input), value = name,
            onClick = { draft = name; editing = true })
    }
    Group(Modifier.selectableGroup(), title = stringResource(R.string.list_item_single)) {
        listOf(R.string.settings_color_natural, R.string.settings_color_vivid).forEachIndexed { index, title ->
            DemoSelectPreference(title = stringResource(title), selected = choice == index, onSelect = { choice = index })
            if (index == 0) Divider()
        }
    }
    Group(title = stringResource(R.string.list_item_multi)) {
        DemoCheckboxPreference(title = stringResource(R.string.settings_sound), checked = first, onCheckedChange = { first = it })
        Divider()
        DemoCheckboxPreference(title = stringResource(R.string.settings_automatic), checked = second, onCheckedChange = { second = it })
    }
    Group(title = stringResource(R.string.list_item_input)) {
        DemoInputPreference(title = stringResource(R.string.list_item_input), value = name, onValueChange = { name = it },
            placeholder = stringResource(R.string.list_item_hint))
    }
    Group {
        DemoStepperPreference(title = stringResource(R.string.list_item_stepper), value = count, onValueChange = { count = it },
            decreaseDescription = stringResource(R.string.stepper_decrease), increaseDescription = stringResource(R.string.stepper_increase))
        Divider()
        DemoProgressPreference(title = stringResource(R.string.list_item_progress), progress = progress,
            actionText = stringResource(if (running) R.string.progress_pause else R.string.progress_resume),
            onAction = { if (progress >= 1f) progress = 0f; running = !running })
    }
    Group(title = stringResource(R.string.list_item_link)) {
        DemoPreference(title = stringResource(R.string.menu_title), onClick = { open("menu") })
        Divider()
        DemoPreference(title = stringResource(R.string.picker_time_title), onClick = { open("timepicker") })
    }
    Group {
        DemoPreference(title = stringResource(R.string.dialog_title), onClick = { open("dialogs") })
    }
    if (editing) AlertDialog(onDismissRequest = { editing = false }, dark = dark,
        title = stringResource(R.string.list_item_input),
        confirmAction = CouiDialogAction(stringResource(R.string.picker_confirm), { name = draft; editing = false }),
        dismissAction = CouiDialogAction(stringResource(R.string.picker_cancel), { editing = false }),
        material = if (Build.VERSION.SDK_INT >= 33 && backdrop != null) CouiDialogDefaults.material(dark) else null,
        backdrop = backdrop) {
        DemoInputPreference(title = stringResource(R.string.list_item_input), value = draft, onValueChange = { draft = it },
            placeholder = stringResource(R.string.list_item_hint))
    }
}
