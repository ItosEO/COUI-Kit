package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.floatingactionbutton.Fab
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Footer
import com.itos.coui.layout.Group
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

@Composable
internal fun ChromeDetail(page: String, dark: Boolean, open: (String) -> Unit) {
    when (page) {
        "appbar" -> ToolbarSample()
        "search" -> SearchSamples(dark, open)
        "navigation" -> NavigationSample(dark, open)
        "fab" -> FabSample(dark)
    }
}

/** 主页面展示真实导航；此页专门比较两侧附件的独立显隐和操作。 */
@Composable
private fun NavigationSample(dark: Boolean, open: (String) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var left by rememberSaveable { mutableStateOf(false) }
    var right by rememberSaveable { mutableStateOf(true) }
    var created by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf(stringResource(R.string.controls), stringResource(R.string.effects),
        stringResource(R.string.presets))
    val rendering = if (Build.VERSION.SDK_INT >= 34) remember { runCatching { CouiFloatingRenderer() } } else null
    val renderer = rendering?.getOrNull()

    Group {
        DemoSwitchPreference(title = stringResource(R.string.nav_left_visible), checked = left,
            onCheckedChange = { left = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.nav_right_visible), checked = right,
            onCheckedChange = { right = it })
    }
    Group(title = stringResource(R.string.catalog_navigation)) {
        ChromePreview(Modifier.height(224.dp)) { backdrop ->
            if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                NavigationBar(Modifier.align(Alignment.BottomCenter), backdrop, renderer,
                    items = listOf(
                        CouiNavigationItem(titles[0], CouiIcons.Components, CouiIcons.ComponentsSelected),
                        CouiNavigationItem(titles[1], DemoIcons.Effects, DemoIcons.EffectsSelected),
                        CouiNavigationItem(titles[2], DemoIcons.Presets, DemoIcons.PresetsSelected)),
                    selectedIndex = selected, onSelection = { selected = it },
                    leftIcon = CouiIcons.Settings, leftDescription = stringResource(R.string.nav_left_description),
                    rightIcon = CouiIcons.Add, rightDescription = stringResource(R.string.sample_click),
                    leftVisible = left, rightVisible = right,
                    onLeftClick = { open("appsettings") }, onRightClick = { created++ },
                    rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
                    dark = dark, lightColor = Color.White,
                    circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule)
            }
        }
        if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
        rendering?.exceptionOrNull()?.let { Text(stringResource(R.string.renderer_error, it.message ?: it.javaClass.simpleName),
            color = MaterialTheme.colorScheme.error) }
    }
    Footer(text = stringResource(R.string.nav_current, titles[selected]))
    Footer(text = stringResource(R.string.nav_created, created))
}

/** 同一 FAB 动态切换配色，而不是复制四个相同的按钮。 */
@Composable
private fun FabSample(dark: Boolean) {
    var preset by rememberSaveable { mutableStateOf(CouiFabColor.Notes) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var created by rememberSaveable { mutableIntStateOf(0) }
    val rendering = if (Build.VERSION.SDK_INT >= 34) remember { runCatching { CouiFloatingRenderer.standalone() } } else null
    val renderer = rendering?.getOrNull()
    Group(title = stringResource(R.string.presets_title)) {
        Column(Modifier.selectableGroup()) {
            CouiFabColor.entries.forEachIndexed { index, option ->
                val label = stringResource(when (option) {
                    CouiFabColor.Notes -> R.string.preset_notes
                    CouiFabColor.Phone -> R.string.preset_phone
                    CouiFabColor.InfraredRemote -> R.string.preset_remote
                    CouiFabColor.Recorder -> R.string.preset_recorder
                })
                DemoSelectPreference(title = label, selected = preset == option, onSelect = { preset = option })
                if (index < CouiFabColor.entries.lastIndex) Divider()
            }
        }
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.fab_use_custom), checked = custom,
            onCheckedChange = { custom = it })
    }
    Group(title = stringResource(R.string.fab_preview_title)) {
        ChromePreview(Modifier.height(224.dp)) { backdrop ->
            if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                val color = if (custom) Color(0xFF2060D6) else CouiFabDefaults.containerColor(preset, dark)
                Fab(Modifier.align(Alignment.BottomEnd).padding(24.dp), backdrop, renderer,
                    onClick = { created++ }, dark = dark, shape = DemoShapes.couiCircle,
                    material = CouiFabDefaults.material(color, dark),
                    shadow = CouiFabDefaults.shadow(color, LocalDensity.current.density,
                        LocalCouiContourGlowEnabled.current)) {
                    Icon(CouiIcons.Add, stringResource(R.string.sample_click), Modifier.size(24.dp), tint = Color.White)
                }
            }
        }
        if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
        rendering?.exceptionOrNull()?.let { Text(stringResource(R.string.renderer_error, it.message ?: it.javaClass.simpleName),
            color = MaterialTheme.colorScheme.error) }
    }
    Footer(text = stringResource(R.string.fab_action_result, created))
}

/** 在采样源上放置少量真实内容；组件不被录入自己的 Backdrop。 */
@Composable
internal fun ChromePreview(modifier: Modifier = Modifier, content: @Composable BoxScope.(CouiBackdrop) -> Unit) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().clip(CouiPageDefaults.shape)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(MaterialTheme.colorScheme.background)) {
            Text(stringResource(R.string.settings_preview), Modifier.padding(18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary))
        }
        content(backdrop)
    }
}
