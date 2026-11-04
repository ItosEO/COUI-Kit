package com.itos.coui.demo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Footer
import com.itos.coui.layout.Group
import com.itos.coui.search.SearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.*

/** 顶栏示例只增加首页没有单独展示的公开按钮状态。折叠标题由真实首页负责。 */
@Composable
internal fun ToolbarSample() {
    var showIcon by rememberSaveable { mutableStateOf(true) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var actions by rememberSaveable { mutableIntStateOf(0) }

    Group {
        DemoSwitchPreference(title = stringResource(R.string.toolbar_show_action), checked = showIcon,
            onCheckedChange = { showIcon = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.toolbar_enable_action), checked = enabled,
            onCheckedChange = { enabled = it })
    }
    Group(title = stringResource(R.string.catalog_appbar)) {
        ChromePreview(Modifier.height(176.dp)) { backdrop ->
            TopAppBar(Modifier.align(Alignment.TopCenter).fillMaxWidth(), backdrop) {
                Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.toolbar_preview_title), Modifier.weight(1f),
                        style = rememberCouiTextStyle(CouiTextKind.Toolbar),
                        color = MaterialTheme.colorScheme.onSurface)
                    if (Build.VERSION.SDK_INT >= 34) TopAppBarButtonGroup {
                        TopAppBarActionVisibility(visible = showIcon) {
                            TopAppBarButton(onClick = { actions++ }) {
                                Icon(CouiIcons.Check, stringResource(R.string.toolbar_icon_action))
                            }
                        }
                        TopAppBarButton(onClick = { actions++ }, text = true, enabled = enabled) {
                            Text(stringResource(R.string.toolbar_text_action))
                        }
                    }
                }
            }
        }
    }
    Footer(text = stringResource(R.string.toolbar_action_count, actions))
    if (Build.VERSION.SDK_INT < 34) Footer(text = stringResource(R.string.requires_api_34))
    Footer(text = stringResource(R.string.toolbar_collapsing_hint))
}

/** 常驻搜索与顶栏展开式搜索分开持有状态，避免把两套公开 API 混成一套。 */
@Composable
internal fun SearchSamples(dark: Boolean, open: (String) -> Unit) {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var persistentExpanded by rememberSaveable { mutableStateOf(false) }
    var persistentQuery by rememberSaveable { mutableStateOf("") }
    val persistent = rememberCouiSearchBarState(persistentExpanded)
    var toolbarExpanded by rememberSaveable { mutableStateOf(false) }
    var toolbarQuery by rememberSaveable { mutableStateOf("") }
    var focusPending by remember { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    val closeToolbar = {
        toolbarExpanded = false
        focusPending = false
        toolbarQuery = ""
        keyboard?.hide()
        Unit
    }
    BackHandler(persistentExpanded || toolbarExpanded) {
        if (toolbarExpanded) closeToolbar()
        else { persistentExpanded = false; persistentQuery = "" }
    }
    Group {
        DemoSwitchPreference(title = stringResource(R.string.search_enabled),
            checked = enabled, onCheckedChange = { enabled = it })
    }
    Group(title = stringResource(R.string.search_persistent)) {
        ChromePreview(Modifier.height(140.dp)) { backdrop ->
            SearchBar(Modifier.align(Alignment.Center),
                state = persistent, query = persistentQuery, onQueryChange = { persistentQuery = it },
                onExpandedChange = {
                    persistentExpanded = it
                    if (it) { toolbarExpanded = false; toolbarQuery = "" }
                    if (!it) persistentQuery = ""
                },
                placeholder = stringResource(R.string.catalog_search_hint),
                cancelText = stringResource(R.string.catalog_search_cancel),
                clearDescription = stringResource(R.string.catalog_search_clear),
                dark = dark, backdrop = backdrop, enabled = enabled)
        }
        if (persistent.active) SearchResults(persistentQuery, enabled && persistentExpanded, open)
    }
    Group(title = stringResource(R.string.search_toolbar)) {
        if (Build.VERSION.SDK_INT >= 34) {
            val state = rememberCouiTopAppBarSearchState(toolbarExpanded)
            ChromePreview(Modifier.height(140.dp)) { backdrop ->
                TopAppBar(Modifier.align(Alignment.TopCenter).fillMaxWidth(), backdrop) {
                    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        TopAppBarSearch(modifier = Modifier.weight(1f), state = state,
                            query = toolbarQuery, onQueryChange = { toolbarQuery = it },
                            onSearch = { keyboard?.hide() },
                            placeholder = stringResource(R.string.catalog_search_hint),
                            focusPending = focusPending, onFocused = { focusPending = false },
                            enabled = enabled, trailingIcon = {
                                IconButton(onClick = closeToolbar) {
                                    Icon(CouiIcons.Close, stringResource(R.string.search_close))
                                }
                            }) {
                            Text(stringResource(R.string.search_toolbar_title))
                        }
                        TopAppBarButtonGroup {
                            TopAppBarSearchButton(state = state,
                                onClick = {
                                    persistentExpanded = false
                                    persistentQuery = ""
                                    toolbarExpanded = true
                                    focusPending = true
                                },
                                contentDescription = stringResource(R.string.search_toolbar),
                                enabled = enabled)
                        }
                    }
                }
            }
            if (toolbarExpanded) SearchResults(toolbarQuery, enabled, open)
        } else {
            DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
        }
    }
}

@Composable
private fun SearchResults(query: String, enabled: Boolean, open: (String) -> Unit) {
    val entries = DemoComponentGroups.flatten().filter {
        stringResource(it.title).contains(query.trim(), ignoreCase = true) ||
            it.page.contains(query.trim(), ignoreCase = true)
    }.take(4)
    Group(title = stringResource(R.string.search_results)) {
        if (entries.isEmpty()) {
            DemoPreference(title = stringResource(R.string.search_no_results), enabled = false)
        } else {
            entries.forEachIndexed { index, entry ->
                DemoPreference(title = stringResource(entry.title), enabled = enabled,
                    onClick = { open(entry.page) })
                if (index < entries.lastIndex) Divider()
            }
        }
    }
}
