package com.itos.coui.demo

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
import androidx.compose.ui.graphics.vector.ImageVector
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.icons.CouiIcons

internal data class DemoCatalogEntry(val page: String, val title: Int, val icon: @Composable () -> ImageVector)

private val CatalogRowMinHeight = 52.dp

internal val DemoWheelPickerEntries = listOf(
    DemoCatalogEntry("wheel", R.string.picker_number_title) { DemoIcons.Tune },
    DemoCatalogEntry("timepicker", R.string.picker_time_title) { CouiIcons.Filled.Health },
    DemoCatalogEntry("countdownpicker", R.string.picker_countdown_title) { CouiIcons.Filled.Battery },
    DemoCatalogEntry("datepicker", R.string.picker_date_title) { DemoIcons.Date },
)

internal val DemoComponentGroups = listOf(
    listOf(DemoCatalogEntry("switch", R.string.switch_title) { DemoIcons.Switch },
        DemoCatalogEntry("segment", R.string.segment_title) { DemoIcons.Switch },
        DemoCatalogEntry("sliders", R.string.slider_title) { DemoIcons.Tune },
        DemoCatalogEntry("checkbox", R.string.checkbox_title) { DemoIcons.Checkbox }),
    listOf(DemoCatalogEntry("dialogs", R.string.dialog_title) { DemoIcons.Dialogs },
        DemoCatalogEntry("menu", R.string.menu_title) { CouiIcons.Filled.Grid }),
    listOf(DemoCatalogEntry("appbar", R.string.catalog_appbar) { DemoIcons.Title },
        DemoCatalogEntry("search", R.string.catalog_search) { CouiIcons.Outlined.Search },
        DemoCatalogEntry("navigation", R.string.catalog_navigation) { CouiIcons.Filled.Lab },
        DemoCatalogEntry("chip", R.string.chip_tab_title) { CouiIcons.Filled.Grid },
        DemoCatalogEntry("fab", R.string.catalog_fab) { CouiIcons.Filled.Add }),
    listOf(DemoCatalogEntry("wheel", R.string.catalog_wheel) { DemoIcons.Tune },
        DemoCatalogEntry("calendar", R.string.picker_calendar_title) { CouiIcons.Filled.Health }),
)

internal val DemoEffectGroups = listOf(
    listOf(DemoCatalogEntry("effect_shadow", R.string.shadow_title) { CouiIcons.Filled.Rings },
        DemoCatalogEntry("effect_fusion", R.string.fusion_title) { CouiIcons.Filled.Add },
        DemoCatalogEntry("effect_backdrop", R.string.backdrop_title) { CouiIcons.Filled.Radar },
        DemoCatalogEntry("effect_material", R.string.material_title) { DemoIcons.Switch }),
)

@Composable
internal fun DemoCatalog(groups: List<List<DemoCatalogEntry>>, open: (String) -> Unit, enabled: Boolean = true,
    query: String = "") {
    val needle = query.trim()
    var hasMatches = false
    groups.forEach { entries ->
        val matches = entries.mapNotNull { entry ->
            if (catalogSearchMatches(entry, needle)) entry to entry.page
            else if (entry.page == "wheel") {
                DemoWheelPickerEntries.firstOrNull { catalogSearchMatches(it, needle) }?.let { entry to it.page }
            } else null
        }
        if (matches.isNotEmpty()) {
            hasMatches = true
            key(entries.first().page) {
                Group {
                    matches.forEachIndexed { index, (entry, route) -> key(entry.page) {
                        DemoPreference(Modifier.heightIn(min = CatalogRowMinHeight),
                            title = stringResource(entry.title), enabled = enabled, onClick = { open(route) }, icon = {
                                Icon(entry.icon(), null, Modifier.size(CouiPageDefaults.iconSize), tint = Color.Unspecified)
                            })
                        if (index != matches.lastIndex) Divider(startIndent =
                            CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
                    } }
                }
            }
        }
    }
    if (!hasMatches) Text(stringResource(R.string.no_matches), Modifier.padding(32.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun catalogSearchMatches(entry: DemoCatalogEntry, needle: String): Boolean =
    stringResource(entry.title).contains(needle, ignoreCase = true) ||
        entry.page.contains(needle, ignoreCase = true) ||
        (entry.page == "checkbox" && "radio".contains(needle, ignoreCase = true)) ||
        stringResource(if (entry.page == "wheel") R.string.intro_numberpicker else catalogDescription(entry.page))
            .contains(needle, ignoreCase = true) ||
        (entry.page == "wheel" && stringResource(R.string.picker_number_title).contains(needle, ignoreCase = true))

internal fun catalogDescription(page: String): Int = when (page) {
    "chip" -> R.string.intro_chip
    "switch" -> R.string.intro_switch
    "sliders" -> R.string.intro_sliders
    "segment" -> R.string.intro_segment
    "checkbox" -> R.string.intro_checkbox
    "dialogs" -> R.string.intro_dialogs
    "menu" -> R.string.intro_menu
    "appbar" -> R.string.intro_appbar
    "search" -> R.string.intro_search
    "navigation" -> R.string.intro_navigation
    "fab" -> R.string.intro_fab
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
