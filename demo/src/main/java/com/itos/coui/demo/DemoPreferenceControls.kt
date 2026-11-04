package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.menu.Menu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.slider.Slider
import com.itos.coui.slider.CouiSliderDefaults
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.theme.CouiTheme

/** Menu 的设置行装配预设；展开期间锁住行蒙层，菜单选择和退场仍由同一个 Menu 持有。 */
@Composable
internal fun <T> DemoPopupPreference(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<CouiMenuItem<T>>,
    onItemClick: (CouiMenuItem<T>) -> Unit,
    menuStyle: CouiMenuStyle,
    summary: String? = null,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val dark = CouiTheme.isDark
    val colors = CouiPageDefaults.themeColors(dark)
    val source = remember { MutableInteractionSource() }
    val anchor = rememberCouiMenuAnchor()
    Box(modifier) {
        DemoPreferenceRow(Modifier.couiMenuAnchor(anchor)
            .couiButtonFeedback(dark, source, enabled, list = true, hoverLocked = expanded)
            .clickable(source, null, enabled, role = Role.Button) { anchor.open { onExpandedChange(true) } },
            title, summary, enabled, icon) {
            BasicText(value, Modifier.padding(start = 16.dp), maxLines = 2,
                style = rememberCouiTextStyle(CouiTextKind.PreferenceValue).copy(color = colors.summary, textAlign = TextAlign.End))
            PopupArrow(Modifier.padding(start = 4.dp).size(14.dp, 24.dp),
                Color(if (dark) 0x4DFFFFFF else 0x42000000))
        }
        Menu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, items = items,
            onItemClick = { item -> onItemClick(item); onExpandedChange(false) }, style = menuStyle, anchor = anchor)
    }
}

@Composable
private fun PopupArrow(modifier: Modifier, color: Color) {
    androidx.compose.material3.Icon(CouiIcons.Popup, null, modifier, tint = color)
}

/** 设置滑条行：标题/右值独立于滑条，48dp 触摸槽始终以自身中线绘制。 */
@Composable
internal fun DemoSliderPreference(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueText: String? = null,
    summary: String? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    showTickMarks: Boolean = steps > 0,
    enabled: Boolean = true,
    onValueChangeFinished: () -> Unit = {},
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val dark = CouiTheme.isDark
    val colors = CouiPageDefaults.themeColors(dark)
    Column(modifier.fillMaxWidth()) {
        DemoPreference(title = title, summary = summary, value = valueText, enabled = enabled)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            leading?.invoke()
            Slider(Modifier.weight(1f).semantics { contentDescription = title }, value, onValueChange,
                dark = dark, enabled = enabled,
                colors = CouiSliderDefaults.colors(colors.accent, dark, enabled), shape = CouiCapsuleShape,
                valueRange = valueRange, steps = steps, showTickMarks = showTickMarks,
                onValueChangeFinished = onValueChangeFinished)
            trailing?.invoke()
        }
    }
}
