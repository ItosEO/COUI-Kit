package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingtoolbar.CouiFloatingToolbarDefaults
import com.itos.coui.floatingtoolbar.FloatingToolbar
import com.itos.coui.floatingtoolbar.FloatingToolbarItem
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group

/** 操作栏只关心「一排同级操作」；数量、宽度和图标配色都由调用方决定。 */
private class ToolbarAction(val icon: ImageVector, val label: String, val tint: Color? = null)

@Composable
internal fun FloatingToolbarSample(dark: Boolean) {
    if (Build.VERSION.SDK_INT >= 34) FloatingToolbarContent(dark)
    else Text(stringResource(R.string.requires_api_34), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@RequiresApi(34)
@Composable
private fun FloatingToolbarContent(dark: Boolean) {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var lastAction by rememberSaveable { mutableStateOf("") }
    val actions = listOf(
        // 多色图标按图标库通用约定自带颜色，其余图标使用操作栏的内容色。
        ToolbarAction(CouiIcons.Filled.AI, stringResource(R.string.floating_toolbar_edit), Color.Unspecified),
        ToolbarAction(CouiIcons.Filled.ShieldCheck, stringResource(R.string.floating_toolbar_check)),
        ToolbarAction(CouiIcons.Filled.Microphone, stringResource(R.string.floating_toolbar_voice)),
        ToolbarAction(CouiIcons.Filled.Add, stringResource(R.string.floating_toolbar_add)),
    )
    Group(title = stringResource(R.string.floating_toolbar_four_actions)) {
        ToolbarPreview(actions, dark, enabled) { lastAction = actions[it].label }
    }
    // 同一组件承载不同数量的操作，栏宽随内容变化。
    val fewActions = listOf(actions[0], actions[3])
    Group(title = stringResource(R.string.floating_toolbar_two_actions)) {
        ToolbarPreview(fewActions, dark, enabled) { lastAction = fewActions[it].label }
    }
    Group {
        DemoPreference(
            title = stringResource(R.string.floating_toolbar_last_action),
            summary = stringResource(R.string.floating_toolbar_hint),
            value = if (lastAction.isEmpty()) stringResource(R.string.floating_toolbar_none) else lastAction,
        )
        Divider()
        DemoSwitchPreference(
            title = stringResource(R.string.floating_toolbar_enabled),
            checked = enabled,
            onCheckedChange = { enabled = it },
        )
    }
}

@RequiresApi(34)
@Composable
private fun ToolbarPreview(
    actions: List<ToolbarAction>,
    dark: Boolean,
    enabled: Boolean,
    onAction: (Int) -> Unit,
) {
    val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val renderer = rendering.getOrNull()
    ChromePreview(
        modifier = Modifier.height(120.dp),
        background = {
            ChromePreviewGradient(dark)
            // 栏后保留一段正文，才能看出胶囊的背景模糊：栏内文字被模糊，栏外保持清晰。
            Text(
                stringResource(R.string.floating_toolbar_preview_text),
                Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        },
    ) { backdrop ->
        if (renderer != null) {
            FloatingToolbar(
                modifier = Modifier.align(Alignment.Center),
                backdrop = backdrop,
                dark = dark,
                renderer = renderer,
            ) {
                actions.forEachIndexed { index, action ->
                    FloatingToolbarItem(onClick = { onAction(index) }, enabled = enabled) {
                        Icon(action.icon, action.label, Modifier.size(CouiFloatingToolbarDefaults.iconSize),
                            tint = action.tint ?: LocalContentColor.current)
                    }
                }
            }
        } else {
            val failure = checkNotNull(rendering.exceptionOrNull())
            Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
        }
    }
}
