package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.Group
import com.itos.coui.layout.Divider
import com.itos.coui.chip.ChipTabButton
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.chip.ChipTabRow
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.menu.Menu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor

@Composable
internal fun ChipTabSample(dark: Boolean) {
    if (Build.VERSION.SDK_INT >= 34) ChipTabMaterialSample(dark)
    else Text(stringResource(R.string.requires_api_34), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@RequiresApi(34)
@Composable
private fun ChipTabMaterialSample(dark: Boolean) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var showIconButton by rememberSaveable { mutableStateOf(false) }
    var solidBackground by rememberSaveable { mutableStateOf(true) }
    var categoriesOpen by rememberSaveable { mutableStateOf(false) }
    val labels = listOf(
        R.string.chip_category_recent, R.string.chip_category_favorites,
        R.string.chip_category_media, R.string.chip_category_notes,
        R.string.chip_category_archive, R.string.chip_category_shared,
        R.string.chip_category_all,
    ).map { stringResource(it) }
    val rendering = remember(labels.size) { runCatching { List(labels.size) { CouiFloatingRenderer.standalone() } } }
    val buttonRendering = remember { lazy(LazyThreadSafetyMode.NONE) { runCatching { CouiFloatingRenderer.standalone() } } }
    val buttonResult = if (showIconButton || buttonRendering.isInitialized()) buttonRendering.value else null
    val renderers = rendering.getOrNull()
    val buttonRenderer = buttonResult?.getOrNull()
    val backdrop = rememberCouiBackdrop()
    val anchor = rememberCouiMenuAnchor()
    val backgroundColor = if (solidBackground) {
        if (dark) Color.Black else Color.White
    } else MaterialTheme.colorScheme.surfaceBright
    Group {
        DemoSwitchPreference(title = stringResource(R.string.chip_enable), checked = enabled, onCheckedChange = { enabled = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.chip_show_icon_button), checked = showIconButton, onCheckedChange = { showIconButton = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.chip_solid_background), checked = solidBackground, onCheckedChange = { solidBackground = it })
    }
    if (renderers == null || (showIconButton && buttonRenderer == null)) {
        val failure = checkNotNull(rendering.exceptionOrNull() ?: buttonResult?.exceptionOrNull())
        Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
            color = MaterialTheme.colorScheme.error)
        return
    }
    // 卡片只在完整示意区域裁剪；不能用 48dp 行高把分类项的投影上下截断。
    Group { Box(Modifier.fillMaxWidth().height(144.dp)) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(backgroundColor))
        ChipTabRow(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            items = labels.mapIndexed { index, label ->
                CouiChipTabItem(index, label)
            },
            selectedKey = selected, onSelectedKeyChange = { selected = it as Int }, backdrop = backdrop, dark = dark,
            enabled = enabled, itemSpacing = 8.dp, renderers = renderers,
            leadingActionVisible = showIconButton,
            leadingAction = if (buttonRenderer != null) {
                {
                    Box {
                        ChipTabButton(modifier = Modifier.couiMenuAnchor(anchor),
                            onClick = { anchor.open { categoriesOpen = true } },
                            contentDescription = stringResource(R.string.chip_categories), backdrop = backdrop,
                            dark = dark, enabled = enabled && showIconButton, renderer = buttonRenderer) {
                            Icon(DemoIcons.Tune, null, tint = LocalCouiContentColor.current)
                        }
                        Menu(expanded = categoriesOpen, onDismissRequest = { categoriesOpen = false },
                            anchor = anchor, style = demoMenuStyle(dark), headerText = stringResource(R.string.chip_categories),
                            items = labels.mapIndexed { index, label -> CouiMenuItem(index, label, selected = selected == index) },
                            onItemClick = { item -> selected = item.value; categoriesOpen = false })
                    }
                }
            } else null,
        )
    }
    Group(title = stringResource(R.string.chip_selection)) {
        DemoPreference(title = labels[selected], summary = stringResource(R.string.chip_content_summary))
    }
}
}
