package com.itos.coui.demo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import com.itos.coui.scroll.couiVerticalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.effect.couiNavigationFeedback
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.navigation.CouiBottomBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.topappbar.CouiCollapsingTopAppBar
import com.itos.coui.topappbar.CouiCollapsingAppBarDefaults
import com.itos.coui.topappbar.rememberCouiCollapsingAppBarState

@RequiresApi(34)
private class DemoRenderers {
    val bottom = CouiFloatingRenderer()
}

@Composable
internal fun DemoApp(dark: Boolean, preferences: DemoPreferences, onToggleTheme: () -> Unit, openPage: (String) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val contour = preferences.contour
    var leftAccessory by rememberSaveable { mutableStateOf(true) }
    var rightAccessory by rememberSaveable { mutableStateOf(true) }
    var preset by rememberSaveable { mutableStateOf(CouiFabColor.Notes) }
    var query by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    val rendering = remember { if (Build.VERSION.SDK_INT >= 34) runCatching { DemoRenderers() } else null }
    val renderers = rendering?.getOrNull()
    val titles = listOf(stringResource(R.string.controls), stringResource(R.string.effects), stringResource(R.string.presets))
    val navigation = listOf(DemoIcons.Components, DemoIcons.Effects, DemoIcons.Presets)
    val selectedNavigation = listOf(DemoIcons.ComponentsSelected, DemoIcons.EffectsSelected, DemoIcons.PresetsSelected)
    val selectPage: (Int) -> Unit = { target ->
        if (target != page) { page = target; query = ""; searching = false }
    }
    val pageState = rememberSaveableStateHolder()
    val scrolls = List(3) { rememberScrollState() }
    val headers = List(3) { index -> rememberCouiCollapsingAppBarState(canScroll = { scrolls[index].maxValue > 0 }) }
    val colors = MaterialTheme.colorScheme
    val background = CouiPageDefaults.themeColors(dark).background
    val backdrop = rememberCouiBackdrop()
    val color = CouiFabDefaults.containerColor(preset, dark)
    // 源 Activity 在窗口转场中仍可见，导航不拆掉结果行及其按压退场；返回时保留查询。
    val open: (String) -> Unit = openPage
    BackHandler(searching) { searching = false; query = "" }
    CompositionLocalProvider(LocalCouiContourGlowEnabled provides contour, LocalContentColor provides colors.onBackground) {
        run {
            val contentScroll = scrolls[page]
            val header = headers[page]
            val search = rememberCouiSearchBarState(searching)
            val density = LocalDensity.current
            val resultOffset = with(LocalDensity.current) { CouiCollapsingAppBarDefaults.resultsStartOffset.toPx() }
            Scaffold(modifier = Modifier.fillMaxSize().imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .nestedScroll(header.nestedScrollConnection), containerColor = background,
                contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom), topBar = {
                    CouiCollapsingTopAppBar(state = header, searchState = search, title = titles[page],
                        color = colors.onSurface, backdrop = backdrop, backgroundColor = colors.background, dark = dark) {
                        CouiSearchBar(state = search, query = query, onQueryChange = { query = it },
                            onExpandedChange = { searching = it; if (!it) query = "" },
                            placeholder = stringResource(R.string.catalog_search_hint),
                            cancelText = stringResource(R.string.catalog_search_cancel),
                            clearDescription = stringResource(R.string.catalog_search_clear),
                            dark = dark, backdrop = backdrop, shadowProgress = header.collapsedFraction)
                    }
                }, bottomBar = {
                    if (Build.VERSION.SDK_INT >= 34 && renderers != null) {
                        Box(Modifier.fillMaxWidth()) {
                            CouiBottomBar(
                                modifier = Modifier.align(Alignment.BottomCenter),
                                backdrop = backdrop, renderer = renderers.bottom,
                                items = titles.mapIndexed { index, title -> CouiNavigationItem(title, navigation[index], selectedNavigation[index]) },
                                selectedIndex = page, leftIcon = DemoIcons.Theme, leftDescription = stringResource(R.string.toggle_theme),
                                rightIcon = DemoIcons.Add, rightDescription = stringResource(R.string.catalog_fab),
                                leftVisible = leftAccessory, rightVisible = rightAccessory,
                                onLeftClick = onToggleTheme, onRightClick = { openPage("fab") }, rightContainerColor = color,
                                dark = dark, lightColor = Color.White, themeTint = MaterialTheme.colorScheme.surface,
                                circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule, onSelection = selectPage,
                            )
                        }
                    } else PlainDemoBottomBar(titles, navigation, selectedNavigation, page, selectPage, dark)
                } ) { innerPadding ->
                Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
                pageState.SaveableStateProvider(page) {
                val contentPadding = remember(header, innerPadding, density) { header.contentPadding(innerPadding, density) }
                val contentEnabled = !search.active
                    Column(Modifier.fillMaxSize().graphicsLayer { alpha = search.contentAlpha }
                        .then(if (contentEnabled) Modifier else Modifier.clearAndSetSemantics {})
                        .couiVerticalScroll(contentScroll, enabled = contentEnabled)
                        .padding(contentPadding).consumeWindowInsets(innerPadding)
                        .padding(bottom = 16.dp)) {
                        rendering?.exceptionOrNull()?.let { failure ->
                            Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                                Modifier.padding(32.dp), color = colors.error)
                        }
                        when (page) {
                            0 -> DemoCatalog(DemoComponentGroups, open, enabled = contentEnabled)
                            1 -> DemoCatalog(DemoEffectGroups, open, enabled = contentEnabled)
                            else -> PresetsDemo(dark, "", contour, preferences::contour, leftAccessory, { leftAccessory = it },
                                rightAccessory, { rightAccessory = it },
                                preset, { preset = it }, preferences.materialTheme, preferences::materialTheme,
                                enabled = contentEnabled)
                        }
                    }
                    if (search.active) {
                        Column(Modifier.fillMaxSize().graphicsLayer {
                            alpha = search.resultsAlpha
                            translationY = resultOffset * (1f - search.resultsAlpha)
                        }
                            .background(colors.background).couiVerticalScroll(rememberScrollState())
                            .padding(contentPadding).consumeWindowInsets(innerPadding).padding(bottom = 16.dp)) {
                            DemoCatalogSearch(query, open)
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun PlainDemoBottomBar(titles: List<String>, icons: List<ImageVector>, selectedIcons: List<ImageVector>, page: Int,
    select: (Int) -> Unit, dark: Boolean) {
    Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)).height(64.dp).selectableGroup(),
        verticalAlignment = Alignment.CenterVertically) {
        titles.forEachIndexed { index, title ->
            val source = remember { MutableInteractionSource() }
            Column(Modifier.weight(1f).clip(DemoShapes.couiCapsule).couiNavigationFeedback(dark, source)
                .selectable(index == page, source, null, true, Role.Tab, onClick = { select(index) }).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                val color = if (index == page) com.itos.coui.theme.CouiTheme.colorScheme.accent else MaterialTheme.colorScheme.onSurfaceVariant
                Icon(if (index == page) selectedIcons[index] else icons[index], null, Modifier.size(24.dp), tint = color)
                Text(title, style = MaterialTheme.typography.labelMedium, color = color)
            }
        }
    }
}
