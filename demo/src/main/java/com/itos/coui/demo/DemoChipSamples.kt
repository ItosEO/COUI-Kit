package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
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
import com.itos.coui.chip.CouiChipTabButton
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.chip.CouiChipTabRow
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.menu.CouiMenu
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
    val labels = (1..7).map { stringResource(R.string.chip_category_number, it) }
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
    DemoSwitchRow(stringResource(R.string.chip_enable), enabled, { enabled = it })
    DemoSwitchRow(stringResource(R.string.chip_show_icon_button), showIconButton, { showIconButton = it })
    DemoSwitchRow(stringResource(R.string.chip_solid_background), solidBackground, { solidBackground = it })
    if (renderers == null || (showIconButton && buttonRenderer == null)) {
        val failure = checkNotNull(rendering.exceptionOrNull() ?: buttonResult?.exceptionOrNull())
        Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
            color = MaterialTheme.colorScheme.error)
        return
    }
    Box(Modifier.fillMaxWidth()) {
        Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(backgroundColor))
        CouiChipTabRow(
            modifier = Modifier.fillMaxWidth(),
            items = labels.mapIndexed { index, label ->
                CouiChipTabItem(index, label, icon = { Icon(Icons.Default.Tune, null) })
            },
            selectedKey = selected, onSelectedKeyChange = { selected = it as Int }, backdrop = backdrop, dark = dark,
            enabled = enabled, itemSpacing = 8.dp, renderers = renderers,
            leadingActionVisible = showIconButton,
            leadingAction = if (buttonRenderer != null) {
                {
                    Box {
                        CouiChipTabButton(modifier = Modifier.couiMenuAnchor(anchor),
                            onClick = { anchor.open { categoriesOpen = true } },
                            contentDescription = stringResource(R.string.chip_categories), backdrop = backdrop,
                            dark = dark, enabled = enabled && showIconButton, renderer = buttonRenderer) {
                            Icon(Icons.Default.Tune, null)
                        }
                        CouiMenu(expanded = categoriesOpen, onDismissRequest = { categoriesOpen = false },
                            anchor = anchor, style = demoMenuStyle(dark), headerText = stringResource(R.string.chip_categories),
                            items = labels.mapIndexed { index, label -> CouiMenuItem(index, label, selected = selected == index) },
                            onItemClick = { item -> selected = item.value; categoriesOpen = false })
                    }
                }
            } else null,
        )
    }
}
