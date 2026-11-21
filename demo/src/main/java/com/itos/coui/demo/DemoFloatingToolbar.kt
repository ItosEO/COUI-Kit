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
import androidx.compose.ui.graphics.isSpecified
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
import com.itos.coui.menu.CouiMenuItem

/** 操作栏只关心「一排同级操作」；数量、宽度和图标配色都由调用方决定。 */
private class ToolbarAction(val icon: ImageVector, val label: String, val tint: Color? = null)

/** 菜单里只有显式画得出颜色的图标沿用自身配色，其余跟随菜单项文字色。 */
private fun ToolbarAction.menuIconTint(): Color? = tint?.takeIf { it.isSpecified }

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
        ToolbarAction(CouiIcons.Filled.Photo, stringResource(R.string.floating_toolbar_photo)),
        ToolbarAction(CouiIcons.Filled.Setting, stringResource(R.string.floating_toolbar_settings)),
    )
    // 多选分组：先勾选若干操作，再用一个开关整组显隐，用来观察多个图标同时进出场。
    var selectedFlags by rememberSaveable { mutableStateOf(List(actions.size) { it == 1 || it == 3 }) }
    var selectionVisible by rememberSaveable { mutableStateOf(true) }
    var groupMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val selected = List(actions.size) { selectedFlags.getOrElse(it) { false } }
    // 最终显隐 = 未勾选多选，或整组开关打开。
    val flags = List(actions.size) { selectionVisible || !selected[it] }
    val menuStyle = demoMenuStyle(dark)
    Group(title = stringResource(R.string.floating_toolbar_actions_count, flags.count { it })) {
        ToolbarPreview(actions, flags, dark, enabled) { lastAction = actions[it].label }
    }
    // 长列表收进行内菜单：菜单固定在入口行下方展开，预览始终留在视野上方，
    // 勾选时能直接看到胶囊收放；菜单不随提交关闭，可以连续切换。
    Group(footer = stringResource(R.string.floating_toolbar_controls_hint)) {
        DemoPopupPreference(
            title = stringResource(R.string.floating_toolbar_multi_select),
            value = stringResource(R.string.floating_toolbar_group_count, selected.count { it }),
            expanded = groupMenuExpanded,
            onExpandedChange = { groupMenuExpanded = it },
            items = actions.mapIndexed { index, action ->
                CouiMenuItem(index, action.label, icon = action.icon, iconTint = action.menuIconTint(), checked = selected[index])
            },
            onItemClick = { item ->
                selectedFlags = selected.toMutableList().also { it[item.value] = !it[item.value] }
            },
            menuStyle = menuStyle,
            closeOnItemClick = false,
        )
        Divider()
        DemoSwitchPreference(
            title = stringResource(R.string.floating_toolbar_multi_visible),
            checked = selectionVisible,
            enabled = selected.any { it },
            onCheckedChange = { selectionVisible = it },
        )
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
    visibleFlags: List<Boolean>,
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
                    // 操作项始终保留，靠 visible 做进出场，胶囊宽度随槽位一起收放。
                    FloatingToolbarItem(
                        onClick = { onAction(index) },
                        enabled = enabled,
                        visible = visibleFlags.getOrElse(index) { true },
                    ) {
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
