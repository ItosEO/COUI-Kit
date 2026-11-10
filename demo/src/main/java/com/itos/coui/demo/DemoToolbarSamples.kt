package com.itos.coui.demo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.scroll.couiVerticalScroll
import com.itos.coui.search.SearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.topappbar.*

private enum class SearchSampleMode { Persistent, Toolbar }

/** 顶栏固定在十条可搜索的模拟内容上方，列表不执行导航或持久化。 */
@Composable
internal fun ToolbarSample() {
    var showIcon by remember { mutableStateOf(true) }
    var enabled by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var focusPending by remember { mutableStateOf(false) }
    val scroll = remember { ScrollState(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    val closeSearch = {
        expanded = false
        query = ""
        focusPending = false
        keyboard?.hide()
        Unit
    }
    BackHandler(expanded, onBack = closeSearch)
    LaunchedEffect(query) { scroll.scrollTo(0) }

    Group {
        DemoSwitchPreference(title = stringResource(R.string.toolbar_show_action), checked = showIcon,
            onCheckedChange = { showIcon = it; if (!it) closeSearch() })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.toolbar_enable_action), checked = enabled,
            onCheckedChange = { enabled = it })
    }
    Group(title = stringResource(R.string.catalog_appbar)) {
        ChromePreview(Modifier.height(360.dp), background = {
            Column(Modifier.fillMaxSize().couiVerticalScroll(scroll)
                .padding(top = CouiTopAppBarDefaults.height, bottom = 16.dp)) {
                ToolbarPreviewList(query)
            }
        }) { backdrop ->
            val state = if (Build.VERSION.SDK_INT >= 34) rememberCouiTopAppBarSearchState(expanded) else null
            val style = CouiTopAppBarButtonDefaults.style(search = true).withDemoThemeTint()
            val title: @Composable () -> Unit = { Text(stringResource(R.string.toolbar_preview_title), maxLines = 1) }
            TopAppBar(
                modifier = Modifier.align(Alignment.TopCenter),
                backdrop = backdrop,
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = CouiTopAppBarDefaults.colors(backgroundColor = LocalDemoTopBarGradient.current,
                    titleColor = MaterialTheme.colorScheme.onSurface),
                title = {
                    if (Build.VERSION.SDK_INT >= 34 && state != null) {
                        TopAppBarSearch(state = state, style = style,
                            query = query, onQueryChange = { query = it },
                            onSearch = { keyboard?.hide() },
                            placeholder = stringResource(R.string.toolbar_search_hint),
                            focusPending = focusPending, onFocused = { focusPending = false },
                            action = { Text(stringResource(R.string.toolbar_text_action)) },
                            onActionClick = if (enabled) closeSearch else null,
                            title = title)
                    } else title()
                },
                actions = {
                    if (Build.VERSION.SDK_INT >= 34 && state != null) {
                        TopAppBarButtonGroup {
                            TopAppBarActionVisibility(visible = showIcon) {
                                TopAppBarSearchButton(state = state, style = style,
                                    onClick = { expanded = true; focusPending = true },
                                    contentDescription = stringResource(R.string.search_toolbar))
                            }
                        }
                    }
                },
            )
        }
        if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
    }
}

/** 两种搜索共用查询与模拟列表，当前搜索类型保证展开互斥。 */
@Composable
internal fun SearchSamples(dark: Boolean) {
    val titleStyle = CouiTopAppBarDefaults.titleTextStyle()
    var enabled by remember { mutableStateOf(true) }
    var active by remember { mutableStateOf<SearchSampleMode?>(null) }
    var query by remember { mutableStateOf("") }
    val persistentExpanded = active == SearchSampleMode.Persistent
    val toolbarExpanded = active == SearchSampleMode.Toolbar
    val persistentEnabled = enabled && active != SearchSampleMode.Toolbar
    val toolbarEnabled = enabled && active != SearchSampleMode.Persistent
    val persistent = rememberCouiSearchBarState(persistentExpanded)
    var focusPending by remember { mutableStateOf(false) }
    val scroll = remember { ScrollState(0) }
    val keyboard = LocalSoftwareKeyboardController.current
    val closeSearch = {
        active = null
        focusPending = false
        query = ""
        keyboard?.hide()
        Unit
    }
    BackHandler(active != null, onBack = closeSearch)
    LaunchedEffect(query) { scroll.scrollTo(0) }
    Group {
        DemoSwitchPreference(title = stringResource(R.string.search_enabled),
            checked = enabled, onCheckedChange = { enabled = it })
    }
    Group {
        ChromePreview(Modifier.height(480.dp), background = {
            Column(Modifier.fillMaxSize().couiVerticalScroll(scroll)
                .padding(top = 128.dp, bottom = 16.dp)) {
                ToolbarPreviewList(query)
            }
        }) { backdrop ->
            // 搜索控件自行处理跟手变形，纵向拖动留在预览区，避免带动详情页滚动。
            val dragBoundary = rememberDraggableState { }
            Column(Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .draggable(dragBoundary, Orientation.Vertical)) {
                SearchBar(Modifier.padding(vertical = 6.dp),
                    state = persistent, query = query, onQueryChange = { query = it },
                    onExpandedChange = {
                        if (!it) closeSearch()
                        else if (active == null) active = SearchSampleMode.Persistent
                    },
                    onSearch = { keyboard?.hide() },
                    placeholder = stringResource(R.string.toolbar_search_hint),
                    cancelText = stringResource(R.string.catalog_search_cancel),
                    clearDescription = stringResource(R.string.catalog_search_clear),
                    dark = dark, backdrop = backdrop, enabled = persistentEnabled)
                if (Build.VERSION.SDK_INT >= 34) {
                    val state = rememberCouiTopAppBarSearchState(toolbarExpanded)
                    TopAppBarSurface(Modifier.fillMaxWidth(), backdrop, gradientColor = LocalDemoTopBarGradient.current) {
                        val style = CouiTopAppBarButtonDefaults.style(search = true).withDemoThemeTint()
                        Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            TopAppBarSearch(modifier = Modifier.weight(1f), state = state, style = style,
                                query = query, onQueryChange = { query = it },
                                onSearch = { keyboard?.hide() },
                                placeholder = stringResource(R.string.toolbar_search_hint),
                                focusPending = focusPending, onFocused = { focusPending = false },
                                enabled = toolbarEnabled,
                                action = { Text(stringResource(R.string.catalog_search_cancel)) },
                                onActionClick = closeSearch) {
                                Text(stringResource(R.string.toolbar_preview_title),
                                    style = titleStyle,
                                    color = MaterialTheme.colorScheme.onSurface)
                            }
                            TopAppBarButtonGroup {
                                TopAppBarSearchButton(state = state, style = style,
                                    onClick = {
                                        if (active == null) {
                                            active = SearchSampleMode.Toolbar
                                            focusPending = true
                                        }
                                    },
                                    contentDescription = stringResource(R.string.search_toolbar),
                                    enabled = toolbarEnabled)
                            }
                        }
                    }
                } else {
                    DemoPreference(Modifier.height(64.dp), title = stringResource(R.string.requires_api_34), enabled = false)
                }
            }
        }
    }
}
