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
import androidx.compose.ui.graphics.Brush
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
import com.itos.coui.layout.Group
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.theme.CouiTheme

@Composable
internal fun ChromeDetail(page: String, dark: Boolean) {
    when (page) {
        "appbar" -> ToolbarSample()
        "search" -> SearchSamples(dark)
        "navigation" -> NavigationSample(dark)
    }
}

/** 主页面展示真实导航；此页专门比较两侧附件的独立显隐和操作。 */
@Composable
private fun NavigationSample(dark: Boolean) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var left by rememberSaveable { mutableStateOf(false) }
    var right by rememberSaveable { mutableStateOf(true) }
    val titles = listOf(stringResource(R.string.controls), stringResource(R.string.effects),
        stringResource(R.string.demo_settings))
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
                        CouiNavigationItem(title = titles[0], icon = CouiIcons.Filled.Grid),
                        CouiNavigationItem(title = titles[1], icon = DemoIcons.Effects, selectedIcon = DemoIcons.EffectsSelected),
                        CouiNavigationItem(title = titles[2], icon = DemoIcons.Settings, selectedIcon = DemoIcons.SettingsSelected)),
                    selectedIndex = selected, onSelection = { selected = it },
                    leftIcon = CouiIcons.Filled.SettingDetail, leftDescription = stringResource(R.string.nav_left_description),
                    rightIcon = CouiIcons.Filled.Add, rightDescription = stringResource(R.string.nav_right_description),
                    leftVisible = left, rightVisible = right,
                    rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
                    dark = dark, lightColor = Color.White,
                    circleShape = DemoShapes.couiCircle, capsuleShape = DemoShapes.couiCapsule)
            }
        }
        if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
        rendering?.exceptionOrNull()?.let { Text(stringResource(R.string.renderer_error, it.message ?: it.javaClass.simpleName),
            color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
internal fun FabSample(preset: CouiFabColor, onPresetChange: (CouiFabColor) -> Unit,
    rounded: Boolean, onRoundedChange: (Boolean) -> Unit) {
    Group(title = stringResource(R.string.fab_shape)) {
        Column(Modifier.selectableGroup()) {
            DemoSelectPreference(title = stringResource(R.string.fab_circle), selected = !rounded,
                onSelect = { onRoundedChange(false) })
            Divider()
            DemoSelectPreference(title = stringResource(R.string.fab_rounded_square), selected = rounded,
                onSelect = { onRoundedChange(true) })
        }
    }
    Group(title = stringResource(R.string.presets_title)) {
        Column(Modifier.selectableGroup()) {
            CouiFabColor.entries.forEachIndexed { index, option ->
                val label = stringResource(when (option) {
                    CouiFabColor.Notes -> R.string.preset_notes
                    CouiFabColor.Phone -> R.string.preset_phone
                    CouiFabColor.InfraredRemote -> R.string.preset_remote
                    CouiFabColor.Recorder -> R.string.preset_recorder
                })
                DemoSelectPreference(title = label, selected = preset == option, onSelect = { onPresetChange(option) })
                if (index < CouiFabColor.entries.lastIndex) Divider()
            }
        }
    }
    if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.requires_api_34), enabled = false)
    // 固定按钮上方保留滚动余量，窄屏横向布局下末项仍能完整露出。
    if (Build.VERSION.SDK_INT >= 34) Spacer(Modifier.height(80.dp))
}

@Composable
internal fun FabSampleButton(modifier: Modifier = Modifier, dark: Boolean, preset: CouiFabColor, rounded: Boolean) {
    if (Build.VERSION.SDK_INT >= 34) {
        val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
        val renderer = rendering.getOrNull()
        if (renderer != null) {
            val color = CouiFabDefaults.containerColor(preset, dark)
            Fab(modifier, renderer = renderer, onClick = {}, dark = dark,
                shape = if (rounded) CouiFabDefaults.roundedSquareShape else CouiFabDefaults.circleShape,
                material = CouiFabDefaults.material(color, dark),
                shadow = CouiFabDefaults.shadow(color, LocalDensity.current.density,
                    LocalCouiContourGlowEnabled.current)) {
                Icon(CouiIcons.Filled.Add, stringResource(R.string.catalog_fab), Modifier.size(24.dp))
            }
        }
        rendering.exceptionOrNull()?.let { error ->
            Text(stringResource(R.string.renderer_error, error.message ?: error.javaClass.simpleName),
                modifier, color = MaterialTheme.colorScheme.error)
        }
    }
}

/** 浮动组件的多彩预览背景, 让玻璃材质有可采样的内容；传入 [backdrop] 时同时录入采样源。 */
@Composable
internal fun BoxScope.ChromePreviewGradient(
    dark: Boolean = CouiTheme.isDark,
    backdrop: CouiBackdrop? = null,
) {
    Box(
        Modifier.matchParentSize()
            .then(if (backdrop != null) Modifier.couiBackdrop(backdrop) else Modifier)
            .background(Brush.linearGradient(
                if (dark) listOf(Color(0xFF152D40), Color(0xFF49313D), Color(0xFF343B2B))
                else listOf(Color(0xFFD7EDF9), Color(0xFFF9DFDC), Color(0xFFEAF1D2)),
            )),
    )
}

/** 预览背景独立录入采样源；浮动组件不被录入自己的 Backdrop。 */
@Composable
internal fun ChromePreview(
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit = {
        Text(stringResource(R.string.settings_preview), Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = rememberCouiTextStyle(CouiTextKind.ItemSummary))
    },
    content: @Composable BoxScope.(CouiBackdrop) -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().clip(CouiPageDefaults.shape)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(MaterialTheme.colorScheme.background),
            content = background)
        content(backdrop)
    }
}
