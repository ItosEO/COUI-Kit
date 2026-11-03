package com.itos.coui.demo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.floatingactionbutton.CouiFab
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Footer
import com.itos.coui.layout.Group
import com.itos.coui.navigation.CouiBottomBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.CouiTopAppBar

/** 各类导航组件的独立 Activity 内容，宿主标题栏仍由 DemoSettingsPage 提供。 */
@Composable
internal fun ChromeDetail(page: String, dark: Boolean, open: (String) -> Unit) {
    when (page) {
        "search" -> {
            var expanded by rememberSaveable { mutableStateOf(false) }
            var query by rememberSaveable { mutableStateOf("") }
            val state = rememberCouiSearchBarState(expanded)
            BackHandler(expanded) { expanded = false; query = "" }
            Group {
                ChromePreview(Modifier.height(152.dp)) { backdrop ->
                    CouiSearchBar(Modifier.align(Alignment.Center), state, query, { query = it },
                        { expanded = it; if (!it) query = "" }, stringResource(R.string.catalog_search_hint),
                        stringResource(R.string.catalog_search_cancel), stringResource(R.string.catalog_search_clear), dark, backdrop)
                }
            }
            if (state.active) Column(if (expanded) Modifier else Modifier.clearAndSetSemantics {}) {
                DemoCatalogSearch(query, enabled = expanded) { query = ""; expanded = false; open(it) }
            }
        }
        "appbar" -> {
            Group {
                ChromePreview(Modifier.height(260.dp)) { backdrop ->
                    CouiTopAppBar(Modifier.align(Alignment.TopCenter), backdrop) {
                        Text(stringResource(R.string.catalog_appbar), Modifier.fillMaxWidth().padding(16.dp),
                            style = rememberCouiTextStyle(CouiTextKind.Toolbar), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Group {
                DemoPreference(title = stringResource(R.string.catalog_appbar_behavior), summary = stringResource(R.string.catalog_appbar_behavior_summary))
            }
        }
        "navigation", "fab" -> {
            var clicks by rememberSaveable { mutableIntStateOf(0) }
            val rendering = if (Build.VERSION.SDK_INT >= 34) remember(page) {
                runCatching { if (page == "fab") CouiFloatingRenderer.standalone() else CouiFloatingRenderer() }
            } else null
            val renderer = rendering?.getOrNull()
            Group {
                ChromePreview(Modifier.height(260.dp)) { backdrop ->
                    if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                        if (page == "navigation") {
                            var selected by rememberSaveable { mutableIntStateOf(0) }
                            CouiBottomBar(Modifier.align(Alignment.BottomCenter), backdrop, renderer,
                                items = listOf(CouiNavigationItem(stringResource(R.string.controls), DemoIcons.Components, DemoIcons.ComponentsSelected),
                                    CouiNavigationItem(stringResource(R.string.effects), DemoIcons.Effects, DemoIcons.EffectsSelected),
                                    CouiNavigationItem(stringResource(R.string.presets), DemoIcons.Presets, DemoIcons.PresetsSelected)),
                                selectedIndex = selected, leftIcon = DemoIcons.Theme, rightIcon = DemoIcons.Add,
                                leftVisible = true, rightVisible = true,
                                leftDescription = stringResource(R.string.toggle_theme), rightDescription = stringResource(R.string.sample_click),
                                onLeftClick = { clicks++ }, onRightClick = { clicks++ }, rightContainerColor = com.itos.coui.theme.CouiTheme.colorScheme.accent,
                                dark = dark, lightColor = Color.White,
                                circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule, onSelection = { selected = it })
                        } else {
                            val color = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark)
                            CouiFab(Modifier.align(Alignment.BottomEnd).padding(16.dp), backdrop, renderer, { clicks++ }, dark,
                                shape = DemoShapes.couiCircle, material = CouiFabDefaults.material(color, dark),
                                shadow = CouiFabDefaults.shadow(color, LocalDensity.current.density, LocalCouiContourGlowEnabled.current)) {
                                Icon(DemoIcons.Add, stringResource(R.string.sample_click), Modifier.size(24.dp), tint = Color.White)
                            }
                        }
                    }
                }
                rendering?.exceptionOrNull()?.let {
                    Text(stringResource(R.string.renderer_error, it.message ?: it.javaClass.simpleName), color = MaterialTheme.colorScheme.error)
                }
                if (Build.VERSION.SDK_INT < 34) Text(stringResource(R.string.requires_api_34))
            }
            Footer(text = stringResource(R.string.sample_press_count, clicks))
        }
    }
}

@Composable
private fun ChromePreview(modifier: Modifier = Modifier, content: @Composable BoxScope.(CouiBackdrop) -> Unit) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().clip(CouiPageDefaults.shape)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(MaterialTheme.colorScheme.background)) {
            Text(stringResource(R.string.settings_preview_summary), Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurface, style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary))
        }
        content(backdrop)
    }
}
