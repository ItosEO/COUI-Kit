package com.itos.coui.demo

import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.floatingactionbutton.CouiFab
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.menu.ProvideCouiMenuBackdrop
import com.itos.coui.navigation.CouiBottomBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonGroup
import com.itos.coui.topappbar.CouiTopAppBarSearch
import com.itos.coui.topappbar.CouiTopAppBarSearchAction
import com.itos.coui.topappbar.CouiTopAppBarSearchButton
import com.itos.coui.topappbar.rememberCouiTopAppBarSearchState

// 每个独立绘制节点持有自己的 renderer, 搜索按钮与搜索框共用同一状态。
@RequiresApi(34)
private class DemoRenderers {
    val theme = CouiFloatingRenderer.standalone()
    val text = CouiFloatingRenderer.standalone()
    val search = CouiFloatingRenderer.standalone()
    val fab = CouiFloatingRenderer.standalone()
    val bottom = CouiFloatingRenderer()
}

@Composable
internal fun DemoApp(dark: Boolean, onToggleTheme: () -> Unit, materialTheme: Boolean,
    onMaterialThemeChange: (Boolean) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var contour by rememberSaveable { mutableStateOf(true) }
    var leftAccessory by rememberSaveable { mutableStateOf(true) }
    var rightAccessory by rememberSaveable { mutableStateOf(true) }
    var toolbarEnabled by rememberSaveable { mutableStateOf(true) }
    var preset by rememberSaveable { mutableStateOf(CouiFabColor.Notes) }
    var query by rememberSaveable { mutableStateOf("") }
    val rendering = remember {
        if (Build.VERSION.SDK_INT >= 34) runCatching { DemoRenderers() } else null
    }
    val renderers = rendering?.getOrNull()
    val backdrop = rememberCouiBackdrop()
    val density = LocalDensity.current
    val pageState = rememberSaveableStateHolder()
    var topHeight by remember { mutableIntStateOf(0) }
    var bottomHeight by remember { mutableIntStateOf(0) }
    var railWidth by remember { mutableIntStateOf(0) }
    val titles = listOf(stringResource(R.string.controls), stringResource(R.string.effects), stringResource(R.string.presets))
    val navigation = listOf(Icons.Default.Tune, Icons.Default.WaterDrop, Icons.Default.Palette)
    val selectPage: (Int) -> Unit = { page = it; query = "" }
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val sections = when (page) {
        0 -> listOf(R.string.picker_title, R.string.dialog_title, R.string.slider_title, R.string.checkbox_title, R.string.switch_title, R.string.menu_title, R.string.chip_tab_title)
        1 -> listOf(R.string.shadow_title, R.string.fusion_title, R.string.backdrop_title, R.string.material_title)
        else -> listOf(R.string.presets_title)
    }.map { stringResource(it) }
    CompositionLocalProvider(LocalCouiContourGlowEnabled provides contour,
        LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        ProvideCouiMenuBackdrop { menuSource ->
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)).imePadding()) {
                val useRail = renderers == null && landscape
                val contentStart = if (useRail) with(density) { railWidth.toDp() } else 0.dp
                val contentBottom = if (useRail) 0.dp else with(density) { bottomHeight.toDp() }
                // 只录制不透明页面。顶栏、底栏和 FAB 均在采样子树外。
                pageState.SaveableStateProvider(page) {
                    val scroll = rememberScrollState()
                    Column(
                        Modifier.fillMaxSize().then(menuSource).couiBackdrop(backdrop)
                            .background(MaterialTheme.colorScheme.background).padding(start = contentStart).verticalScroll(scroll)
                            .padding(horizontal = 24.dp)
                            .padding(top = with(density) { topHeight.toDp() } + 16.dp,
                                bottom = contentBottom + if (renderers != null) 104.dp else 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (Build.VERSION.SDK_INT < 34) {
                            Text(stringResource(R.string.requires_api_34), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        rendering?.exceptionOrNull()?.let { failure ->
                            Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                                color = MaterialTheme.colorScheme.error)
                        }
                        if (query.isNotBlank() && sections.none { it.contains(query.trim(), ignoreCase = true) }) {
                            Text(stringResource(R.string.no_matches))
                        }
                        when (page) {
                            0 -> ControlsDemo(dark = dark, query = query)
                            1 -> EffectsDemo(dark = dark, query = query, shadowAvailable = renderers != null)
                            2 -> PresetsDemo(dark = dark, query = query, contour = contour, onContourChange = { contour = it },
                                leftAccessory = leftAccessory, onLeftAccessoryChange = { leftAccessory = it },
                                rightAccessory = rightAccessory, onRightAccessoryChange = { rightAccessory = it },
                                toolbarEnabled = toolbarEnabled, onToolbarEnabledChange = { toolbarEnabled = it },
                                preset = preset, onPresetChange = { preset = it }, materialTheme = materialTheme,
                                onMaterialThemeChange = onMaterialThemeChange)
                        }
                    }
                }
                CouiTopAppBar(
                    modifier = Modifier.padding(start = contentStart).fillMaxWidth().onSizeChanged { topHeight = it.height },
                    backdrop = backdrop,
                ) {
                    if (Build.VERSION.SDK_INT >= 34 && renderers != null) {
                        DemoToolbar(renderers = renderers, dark = dark, contour = contour, query = query,
                            actionsEnabled = toolbarEnabled, onQueryChange = { query = it },
                            onToggleTheme = onToggleTheme, onReset = { preset = CouiFabColor.Notes })
                    } else {
                        MaterialDemoToolbar(query = query, onQueryChange = { query = it },
                            actionsEnabled = toolbarEnabled, onToggleTheme = onToggleTheme)
                    }
                }
                if (Build.VERSION.SDK_INT >= 34 && renderers != null) {
                    val color = CouiFabDefaults.containerColor(preset, dark)
                    CouiFab(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp,
                            bottom = with(density) { bottomHeight.toDp() } + 16.dp),
                        backdrop = backdrop, renderer = renderers.fab, onClick = { selectPage(2) }, dark = dark,
                        shape = DemoShapes.couiCircle, material = CouiFabDefaults.material(color, dark),
                        shadow = CouiFabDefaults.shadow(color, density.density, contour),
                    ) { Icon(Icons.Default.Palette, stringResource(R.string.open_presets), tint = Color.White) }
                    CouiBottomBar(
                        modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { bottomHeight = it.height },
                        backdrop = backdrop, renderer = renderers.bottom,
                        items = titles.mapIndexed { index, title -> CouiNavigationItem(title, navigation[index], navigation[index]) },
                        selectedIndex = page, leftIcon = Icons.Default.DarkMode, leftDescription = stringResource(R.string.toggle_theme),
                        rightIcon = Icons.Default.Palette, rightDescription = stringResource(R.string.open_presets),
                        leftVisible = leftAccessory, rightVisible = rightAccessory,
                        onLeftClick = onToggleTheme, onRightClick = { selectPage(2) }, rightContainerColor = color,
                        dark = dark, lightColor = Color.White,
                        circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule, onSelection = selectPage,
                    )
                } else {
                    if (useRail) NavigationRail(
                        modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight().onSizeChanged { railWidth = it.width },
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Spacer(Modifier.weight(1f))
                        titles.forEachIndexed { index, title ->
                            NavigationRailItem(selected = index == page, onClick = { selectPage(index) },
                                icon = { Icon(navigation[index], null) }, label = { Text(title) })
                        }
                        Spacer(Modifier.weight(1f))
                    } else NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { bottomHeight = it.height },
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        titles.forEachIndexed { index, title ->
                            NavigationBarItem(selected = index == page, onClick = { selectPage(index) },
                                icon = { Icon(navigation[index], null) }, label = { Text(title) })
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(34)
@Composable
private fun DemoToolbar(renderers: DemoRenderers, dark: Boolean, contour: Boolean, query: String, actionsEnabled: Boolean,
    onQueryChange: (String) -> Unit, onToggleTheme: () -> Unit, onReset: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var focusPending by remember { mutableStateOf(false) }
    val state = rememberCouiTopAppBarSearchState(expanded, renderers.search)
    val style = demoToolbarStyle(dark, contour)
    val searchStyle = demoToolbarStyle(dark, contour, search = true)
    val keyboard = LocalSoftwareKeyboardController.current
    val closeSearch = { expanded = false; focusPending = false; onQueryChange(""); keyboard?.hide(); Unit }
    BackHandler(enabled = expanded, onBack = closeSearch)
    Row(Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        CouiTopAppBarSearch(
            modifier = Modifier.weight(1f), state = state, style = searchStyle,
            query = query, onQueryChange = onQueryChange, onSearch = { keyboard?.hide() },
            placeholder = stringResource(R.string.search_hint), focusPending = focusPending, onFocused = { focusPending = false },
            trailingIcon = {
                IconButton(onClick = closeSearch, shape = DemoShapes.capsule) {
                    Icon(Icons.Default.Close, stringResource(R.string.close_search))
                }
            },
        ) { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) }
        CouiTopAppBarButtonGroup {
            CouiTopAppBarSearchAction(state = state) {
                CouiTopAppBarButton(onClick = onReset, enabled = actionsEnabled, text = true, renderer = renderers.text, style = style) {
                    Text(stringResource(R.string.reset))
                }
            }
            CouiTopAppBarSearchAction(state = state, visibleWhileSearching = true) {
                CouiTopAppBarButton(onClick = onToggleTheme, enabled = actionsEnabled, text = false, renderer = renderers.theme) {
                    Icon(Icons.Default.DarkMode, stringResource(R.string.toggle_theme))
                }
            }
            CouiTopAppBarSearchButton(state = state, style = searchStyle,
                onClick = { expanded = true; focusPending = true }, contentDescription = stringResource(R.string.search_hint))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialDemoToolbar(query: String, onQueryChange: (String) -> Unit, actionsEnabled: Boolean, onToggleTheme: () -> Unit) {
    var searching by rememberSaveable { mutableStateOf(false) }
    val requester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val closeSearch = { searching = false; onQueryChange(""); keyboard?.hide(); Unit }
    BackHandler(enabled = searching, onBack = closeSearch)
    LaunchedEffect(searching) {
        if (searching) { requester.requestFocus(); keyboard?.show() }
    }
    Row(Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (searching) SearchBarDefaults.InputField(
            modifier = Modifier.weight(1f).clip(DemoShapes.capsule).focusRequester(requester),
            query = query, onQueryChange = onQueryChange,
            onSearch = { keyboard?.hide() }, expanded = true, onExpandedChange = {},
            placeholder = { Text(stringResource(R.string.search_hint)) },
            trailingIcon = { IconButton(onClick = closeSearch, shape = DemoShapes.capsule) {
                Icon(Icons.Default.Close, stringResource(R.string.close_search))
            } },
        ) else {
            Text(stringResource(R.string.app_name), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = onToggleTheme, enabled = actionsEnabled, shape = DemoShapes.capsule) {
                Icon(Icons.Default.DarkMode, stringResource(R.string.toggle_theme))
            }
            IconButton(onClick = { searching = true }, shape = DemoShapes.capsule) {
                Icon(Icons.Default.Search, stringResource(R.string.search_hint))
            }
        }
    }
}
