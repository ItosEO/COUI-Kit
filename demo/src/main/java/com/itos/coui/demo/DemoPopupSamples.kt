package com.itos.coui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.Menu
import com.itos.coui.menu.ProvideCouiMenuBackdrop
import com.itos.coui.menu.rememberCouiWindowBackdrop

@Composable
internal fun MenuSamples(dark: Boolean) {
    var blurEnabled by rememberSaveable { mutableStateOf(true) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val style = demoMenuStyle(dark)
    val modes = listOf(stringResource(R.string.menu_first), stringResource(R.string.menu_second))

    Group {
        DemoSwitchPreference(
            title = stringResource(R.string.menu_blur),
            summary = stringResource(R.string.menu_blur_summary),
            checked = blurEnabled,
            onCheckedChange = { blurEnabled = it },
        )
    }
    ProvideCouiMenuBackdrop(backdrop = if (blurEnabled) windowBackdrop else null) {
        Group(title = stringResource(R.string.menu_anchors)) {
            ButtonMenuSample(modes)
            Divider()
            PopupRowSample(style)
        }
        Group(title = stringResource(R.string.menu_actions)) {
            CheckableMenuSample(style)
            Divider()
            SubMenuSample(modes, style)
        }
        Group(title = stringResource(R.string.menu_titled)) {
            val header = stringResource(R.string.menu_header)
            ChoiceMenuSample(title = stringResource(R.string.menu_header_center),
                options = modes, style = style, header = header)
            Divider()
            ChoiceMenuSample(title = stringResource(R.string.menu_header_start),
                options = modes, style = style, header = header, headerAlign = TextAlign.Start)
        }
        Group {
            ChoiceMenuSample(menuModifier = Modifier.heightIn(max = 240.dp),
                title = stringResource(R.string.menu_scrollable), style = style,
                options = (1..16).map { stringResource(R.string.menu_item_number, it) })
        }
        Group {
            val tint = MaterialTheme.colorScheme.primary
            val swatches = if (dark) listOf(Color(0xFF315C99), Color(0xFF91445F), Color(0xFF287461))
                else listOf(Color(0xFF7EB1F4), Color(0xFFF19AB5), Color(0xFF76C6AD))
            Box(Modifier.fillMaxWidth()) {
                Row(Modifier.matchParentSize()) {
                    swatches.forEach { color -> Box(Modifier.weight(1f).fillMaxHeight().background(color)) }
                }
                Box(Modifier.padding(vertical = 32.dp)) {
                    ChoiceMenuSample(title = stringResource(R.string.menu_custom_style),
                        options = modes, style = style.copy(
                            shape = RoundedCornerShape(16.dp),
                            radius = 16.dp,
                            itemShape = RoundedCornerShape(8.dp),
                            themeTint = tint,
                        ))
                }
            }
        }
    }
}

@Composable
private fun ButtonMenuSample(options: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.menu_button_anchor), modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { expanded = true }) { Text(options[selected]) }
            Menu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                items = options.mapIndexed { index, label -> CouiMenuItem(index, label, selected = selected == index) },
                onItemClick = { item ->
                    selected = item.value
                    expanded = false
                },
                offset = DpOffset(0.dp, 4.dp),
            )
        }
    }
}

@Composable
private fun PopupRowSample(style: CouiMenuStyle) {
    var expanded by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val options = listOf(stringResource(R.string.menu_row_option_one), stringResource(R.string.menu_row_option_two))
    DemoPopupPreference(
        title = stringResource(R.string.menu_row_title),
        value = options[selected],
        expanded = expanded,
        onExpandedChange = { expanded = it },
        items = options.mapIndexed { index, label ->
            CouiMenuItem(index, label, icon = if (index == 0) DemoIcons.SimOne else DemoIcons.SimTwo,
                iconTint = Color(if (index == 0) 0xFF27B73D else 0xFF1A8CFF), selected = index == selected)
        },
        onItemClick = { selected = it.value },
        menuStyle = style,
        summary = stringResource(R.string.menu_touch_anchor),
    )
}

private enum class MenuCheckOption { Pinned, Details, Disabled, Done }

@Composable
private fun CheckableMenuSample(style: CouiMenuStyle) {
    var pinned by rememberSaveable { mutableStateOf(true) }
    var showDetails by rememberSaveable { mutableStateOf(false) }
    MenuSampleRow(
        title = stringResource(R.string.menu_checkable),
        items = listOf(
            CouiMenuItem(MenuCheckOption.Pinned, stringResource(R.string.menu_pinned), checked = pinned),
            CouiMenuItem(MenuCheckOption.Details, stringResource(R.string.menu_show_details), checked = showDetails),
            CouiMenuItem(MenuCheckOption.Disabled, stringResource(R.string.menu_disabled), checked = false, enabled = false),
            CouiMenuItem(MenuCheckOption.Done, stringResource(R.string.menu_done), dividerBefore = true),
        ),
        style = style,
        onItemClick = { item ->
            when (item.value) {
                MenuCheckOption.Pinned -> pinned = !pinned
                MenuCheckOption.Details -> showDetails = !showDetails
                MenuCheckOption.Disabled, MenuCheckOption.Done -> Unit
            }
        },
    )
}

@Composable
private fun SubMenuSample(options: List<String>, style: CouiMenuStyle) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    MenuSampleRow(
        title = stringResource(R.string.menu_submenu),
        value = options[selected],
        items = listOf(
            CouiMenuItem(0, options[0], icon = CouiIcons.Filled.SettingDetail, selected = selected == 0),
            CouiMenuItem(2, stringResource(R.string.menu_more), children = listOf(
                CouiMenuItem(1, options[1], selected = selected == 1),
                CouiMenuItem(3, stringResource(R.string.menu_disabled), enabled = false),
            )),
        ),
        style = style,
        onItemClick = { selected = it.value },
    )
}

@Composable
private fun ChoiceMenuSample(
    menuModifier: Modifier = Modifier,
    title: String,
    options: List<String>,
    style: CouiMenuStyle,
    header: String? = null,
    headerAlign: TextAlign = TextAlign.Center,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    MenuSampleRow(
        menuModifier = menuModifier,
        title = title,
        value = options[selected],
        items = options.mapIndexed { index, label -> CouiMenuItem(index, label, selected = selected == index) },
        style = style,
        header = header,
        headerAlign = headerAlign,
        onItemClick = { selected = it.value },
    )
}

@Composable
private fun <T> MenuSampleRow(
    menuModifier: Modifier = Modifier,
    title: String,
    items: List<CouiMenuItem<T>>,
    style: CouiMenuStyle,
    value: String? = null,
    header: String? = null,
    headerAlign: TextAlign = TextAlign.Center,
    onItemClick: (CouiMenuItem<T>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    DemoPopupPreference(
        title = title,
        value = value,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        items = items,
        onItemClick = onItemClick,
        menuStyle = style,
        menuModifier = menuModifier,
        headerText = header,
        headerTextAlign = headerAlign,
        closeOnItemClick = { it.checked == null },
    )
}
