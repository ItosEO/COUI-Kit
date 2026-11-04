package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group

internal data class DemoCatalogEntry(val page: String, val title: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val CatalogRowMinHeight = 52.dp

internal val DemoWheelPickerEntries = listOf(
    DemoCatalogEntry("wheel", R.string.picker_number_title, DemoIcons.Tune),
    DemoCatalogEntry("timepicker", R.string.picker_time_title, CouiIcons.Time),
    DemoCatalogEntry("countdownpicker", R.string.picker_countdown_title, CouiIcons.Countdown),
    DemoCatalogEntry("datepicker", R.string.picker_date_title, DemoIcons.Date),
)

internal val DemoComponentGroups = listOf(
    listOf(DemoCatalogEntry("switch", R.string.switch_title, DemoIcons.Switch),
        DemoCatalogEntry("sliders", R.string.slider_title, DemoIcons.Tune),
        DemoCatalogEntry("checkbox", R.string.checkbox_title, DemoIcons.Checkbox)),
    listOf(DemoCatalogEntry("dialogs", R.string.dialog_title, DemoIcons.Dialogs),
        DemoCatalogEntry("menu", R.string.menu_title, CouiIcons.More)),
    listOf(DemoCatalogEntry("appbar", R.string.catalog_appbar, DemoIcons.Title),
        DemoCatalogEntry("search", R.string.catalog_search, CouiIcons.Search),
        DemoCatalogEntry("navigation", R.string.catalog_navigation, CouiIcons.Featured),
        DemoCatalogEntry("fab", R.string.catalog_fab, CouiIcons.Add)),
    listOf(DemoCatalogEntry("wheel", R.string.catalog_wheel, DemoIcons.Tune),
        DemoCatalogEntry("calendar", R.string.picker_calendar_title, CouiIcons.Calendar)),
    listOf(DemoCatalogEntry("settings", R.string.settings_title, DemoIcons.Tune),
        DemoCatalogEntry("chip", R.string.chip_tab_title, CouiIcons.Components)),
)

internal val DemoEffectGroups = listOf(
    listOf(DemoCatalogEntry("effect_shadow", R.string.shadow_title, CouiIcons.Featured),
        DemoCatalogEntry("effect_fusion", R.string.fusion_title, CouiIcons.Add),
        DemoCatalogEntry("effect_backdrop", R.string.backdrop_title, CouiIcons.Components),
        DemoCatalogEntry("effect_material", R.string.material_title, DemoIcons.Switch)),
)

@Composable
internal fun DemoCatalog(groups: List<List<DemoCatalogEntry>>, open: (String) -> Unit, enabled: Boolean = true) {
    groups.forEach { entries -> key(entries.first().page) {
        Group {
            entries.forEachIndexed { index, entry -> key(entry.page) {
                DemoPreference(Modifier.heightIn(min = CatalogRowMinHeight),
                    title = stringResource(entry.title), enabled = enabled, onClick = { open(entry.page) }, icon = {
                        Icon(entry.icon, null, Modifier.size(CouiPageDefaults.iconSize), tint = catalogIconColor(entry.page))
                    })
                if (index != entries.lastIndex) Divider(startIndent =
                    CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
            } }
        }
    } }
}

@Composable
internal fun DemoCatalogSearch(query: String, enabled: Boolean = true, open: (String) -> Unit) {
    val entries = (DemoComponentGroups + DemoEffectGroups).flatten() + DemoWheelPickerEntries.drop(1)
    val filtered = entries.filter {
        val needle = query.trim()
        stringResource(it.title).contains(needle, ignoreCase = true) ||
            it.page.contains(needle, ignoreCase = true) ||
            stringResource(if (it.page == "wheel") R.string.intro_numberpicker else catalogDescription(it.page))
                .contains(needle, ignoreCase = true) ||
            (it.page == "wheel" && stringResource(R.string.picker_number_title).contains(needle, ignoreCase = true))
    }
    if (filtered.isEmpty()) Text(stringResource(R.string.no_matches), Modifier.padding(32.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant)
    else DemoCatalog(listOf(filtered), open, enabled = enabled)
}

/** 目录图标由 Demo 配色，几何使用随 Demo 打包的 ColorOS 原始矢量。 */
private fun catalogIconColor(page: String): Color = Color(when (page) {
    "timepicker", "effect_backdrop", "effect_shadow" -> 0xFF1A8CFF
    "switch", "checkbox" -> 0xFF34B978
    "sliders", "countdownpicker" -> 0xFFFF9B35
    "dialogs", "calendar", "effect_material" -> 0xFFA884E7
    "menu", "datepicker", "effect_fusion" -> 0xFF45B6AE
    "appbar", "navigation" -> 0xFFE97697
    else -> 0xFF7397C8
})

internal fun catalogDescription(page: String): Int = when (page) {
    "chip" -> R.string.intro_chip
    "switch" -> R.string.intro_switch
    "sliders" -> R.string.intro_sliders
    "checkbox" -> R.string.intro_checkbox
    "dialogs" -> R.string.intro_dialogs
    "menu" -> R.string.intro_menu
    "appbar" -> R.string.intro_appbar
    "search" -> R.string.intro_search
    "navigation" -> R.string.intro_navigation
    "fab" -> R.string.intro_fab
    "settings" -> R.string.intro_settings
    "wheel" -> R.string.intro_wheel
    "timepicker" -> R.string.intro_timepicker
    "countdownpicker" -> R.string.intro_countdownpicker
    "datepicker" -> R.string.intro_datepicker
    "calendar" -> R.string.intro_calendar
    "effect_backdrop" -> R.string.intro_effect_backdrop
    "effect_material" -> R.string.intro_effect_material
    "effect_shadow" -> R.string.intro_effect_shadow
    "effect_fusion" -> R.string.intro_effect_fusion
    else -> R.string.intro_settings
}
