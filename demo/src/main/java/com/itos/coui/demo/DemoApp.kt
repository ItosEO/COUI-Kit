package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.effect.couiNavigationFeedback
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.search.SearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Scaffold
import com.itos.coui.topappbar.CollapsingTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarDefaults
import com.itos.coui.topappbar.LargeTopAppBar

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
    val navigation = listOf(CouiIcons.Components, DemoIcons.Effects, DemoIcons.Presets)
    val selectedNavigation = listOf(CouiIcons.ComponentsSelected, DemoIcons.EffectsSelected, DemoIcons.PresetsSelected)
    val selectPage: (Int) -> Unit = { target ->
        if (target != page) { page = target; query = ""; searching = false }
    }
    val pageState = rememberSaveableStateHolder()
    val scrolls = List(3) { rememberScrollState() }
    val componentSearchScroll = rememberScrollState()
    LaunchedEffect(query) { componentSearchScroll.scrollTo(0) }
    val headers = List(3) { index -> CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        canScroll = { scrolls[index].maxValue > 0 }) }
    val colors = MaterialTheme.colorScheme
    val background = CouiPageDefaults.themeColors(dark).background
    val color = CouiFabDefaults.containerColor(preset, dark)
    // 源 Activity 在窗口转场中仍可见，导航不拆掉结果行及其按压退场；返回时保留查询。
    val open: (String) -> Unit = openPage
    BackHandler(page == 0 && searching) { searching = false; query = "" }
    CompositionLocalProvider(LocalCouiContourGlowEnabled provides contour) {
        val contentScroll = if (page == 0 && query.isNotBlank()) componentSearchScroll else scrolls[page]
        val behavior = headers[page]
        val header = behavior.state
        val search = key(page) { rememberCouiSearchBarState(page == 0 && searching) }
        val density = LocalDensity.current
        Scaffold(modifier = Modifier.fillMaxSize().imePadding()
            .nestedScroll(behavior.nestedScrollConnection), containerColor = background,
            topBar = {
                if (page == 0) {
                    CollapsingTopAppBar(state = header, searchState = search, title = titles[page],
                        color = colors.onSurface, backgroundColor = background, dark = dark) {
                            SearchBar(state = search, query = query, onQueryChange = { query = it },
                                onExpandedChange = { searching = it; if (!it) query = "" },
                                placeholder = stringResource(R.string.catalog_search_hint),
                                cancelText = stringResource(R.string.catalog_search_cancel),
                                clearDescription = stringResource(R.string.catalog_search_clear),
                                dark = dark, shadowProgress = header.collapsedFraction)
                    }
                } else LargeTopAppBar(title = { Text(titles[page], maxLines = 1) },
                    scrollBehavior = behavior, dark = dark,
                    colors = CouiTopAppBarDefaults.colors(backgroundColor = background,
                        titleColor = colors.onSurface, dark = dark))
            }, bottomBar = {
                if (Build.VERSION.SDK_INT >= 34 && renderers != null) {
                    Box(Modifier.fillMaxWidth()) {
                        NavigationBar(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            renderer = renderers.bottom,
                            items = titles.mapIndexed { index, title -> CouiNavigationItem(title, navigation[index], selectedNavigation[index]) },
                            selectedIndex = page, leftIcon = CouiIcons.Theme, leftDescription = stringResource(R.string.toggle_theme),
                            rightIcon = CouiIcons.Add, rightDescription = stringResource(R.string.catalog_fab),
                            leftVisible = leftAccessory, rightVisible = rightAccessory,
                            onLeftClick = onToggleTheme, onRightClick = { openPage("fab") }, rightContainerColor = color,
                            dark = dark, lightColor = Color.White,
                            circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule, onSelection = selectPage,
                        )
                    }
                } else PlainDemoBottomBar(titles, navigation, selectedNavigation, page, selectPage, dark)
            } ) { innerPadding ->
            pageState.SaveableStateProvider(page) {
                val contentPadding = remember(header, innerPadding, density) { header.contentPadding(innerPadding, density) }
                Column(Modifier.fillMaxSize().couiVerticalScroll(contentScroll)
                    .padding(contentPadding).consumeWindowInsets(innerPadding)
                    .padding(bottom = 16.dp)) {
                    rendering?.exceptionOrNull()?.let { failure ->
                        Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                            Modifier.padding(32.dp), color = colors.error)
                    }
                    when (page) {
                        0 -> DemoCatalog(DemoComponentGroups, open, query = query)
                        1 -> DemoCatalog(DemoEffectGroups, open)
                        else -> PresetsDemo(dark, contour, preferences::contour, leftAccessory, { leftAccessory = it },
                            rightAccessory, { rightAccessory = it },
                            preset, { preset = it }, preferences.materialTheme, preferences::materialTheme)
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
