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
import com.itos.coui.layout.Group
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

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
                        CouiNavigationItem(titles[0], CouiIcons.Components, CouiIcons.ComponentsSelected),
                        CouiNavigationItem(titles[1], DemoIcons.Effects, DemoIcons.EffectsSelected),
                        CouiNavigationItem(titles[2], DemoIcons.Settings, DemoIcons.SettingsSelected)),
                    selectedIndex = selected, onSelection = { selected = it },
                    leftIcon = CouiIcons.Settings, leftDescription = stringResource(R.string.nav_left_description),
                    rightIcon = CouiIcons.Add, rightDescription = stringResource(R.string.nav_right_description),
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
internal fun FabSample(preset: CouiFabColor, onPresetChange: (CouiFabColor) -> Unit) {
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
internal fun FabSampleButton(modifier: Modifier = Modifier, dark: Boolean, preset: CouiFabColor) {
    if (Build.VERSION.SDK_INT >= 34) {
        val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
        val renderer = rendering.getOrNull()
        if (renderer != null) {
            val color = CouiFabDefaults.containerColor(preset, dark)
            Fab(modifier, renderer = renderer, dark = dark, shape = DemoShapes.couiCircle,
                material = CouiFabDefaults.material(color, dark),
                shadow = CouiFabDefaults.shadow(color, LocalDensity.current.density,
                    LocalCouiContourGlowEnabled.current)) {
                Icon(CouiIcons.Add, stringResource(R.string.catalog_fab), Modifier.size(24.dp), tint = Color.White)
            }
        }
        rendering.exceptionOrNull()?.let { error ->
            Text(stringResource(R.string.renderer_error, error.message ?: error.javaClass.simpleName),
                modifier, color = MaterialTheme.colorScheme.error)
        }
    }
}

/** 预览背景独立录入采样源；浮动组件不被录入自己的 Backdrop。 */
@Composable
internal fun ChromePreview(
    modifier: Modifier = Modifier,
    background: @Composable BoxScope.() -> Unit = {
        Text(stringResource(R.string.settings_preview), Modifier.padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary))
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
