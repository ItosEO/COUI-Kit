package com.itos.coui.demo

import com.itos.coui.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.menu.CouiMenu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.slider.CouiSlider
import com.itos.coui.slider.CouiSliderDefaults
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.motion.rememberCouiPressMotion
import com.itos.coui.motion.couiPressMotion
import com.itos.coui.theme.CouiTheme
import kotlinx.coroutines.delay

/** CouiMenu 的设置行装配预设；展开期间锁住行蒙层，菜单选择和退场仍由同一个 CouiMenu 持有。 */
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
        CouiMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, items = items,
            onItemClick = { item -> onItemClick(item); onExpandedChange(false) }, style = menuStyle, anchor = anchor)
    }
}

@Composable
private fun PopupArrow(modifier: Modifier, color: Color) {
    androidx.compose.material3.Icon(DemoIcons.Popup, null, modifier, tint = color)
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
            CouiSlider(Modifier.weight(1f).semantics { contentDescription = title }, value, onValueChange,
                dark = dark, enabled = enabled,
                colors = CouiSliderDefaults.colors(colors.accent, dark, enabled), shape = CouiCapsuleShape,
                valueRange = valueRange, steps = steps, showTickMarks = showTickMarks)
            trailing?.invoke()
        }
    }
}

/** 页面内输入项；标题、支持文本与编辑内容可组合，输入值与校验归宿主。 */
@Composable
internal fun DemoInputPreference(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    supportingText: String? = null,
) {
    val colors = CouiPageDefaults.themeColors()
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BasicText(title, style = rememberCouiTextStyle(CouiTextKind.PreferenceTitle).copy(color = colors.title))
        BasicTextField(value, onValueChange, Modifier.fillMaxWidth().semantics { contentDescription = title },
            singleLine = true, enabled = enabled,
            textStyle = rememberCouiTextStyle(CouiTextKind.Body).copy(color = if (enabled) colors.title else colors.disabled),
            cursorBrush = SolidColor(colors.accent), decorationBox = { field ->
                Box { if (value.isEmpty()) BasicText(placeholder,
                    style = rememberCouiTextStyle(CouiTextKind.Body).copy(color = colors.summary)); field() }
            })
        if (!supportingText.isNullOrEmpty()) BasicText(supportingText,
            style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary).copy(color = colors.summary))
    }
}

/** 步进设置项；32dp 圆形控制、独立触摸反馈，标题不承担第二次点击。 */
@Composable
internal fun DemoStepperPreference(
    modifier: Modifier = Modifier,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    valueRange: IntRange = 0..100,
    summary: String? = null,
    enabled: Boolean = true,
) {
    require(value in valueRange)
    val colors = CouiPageDefaults.themeColors()
    DemoPreferenceRow(modifier, title, summary, enabled, null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperControl(false, decreaseDescription, enabled && value > valueRange.first) { onValueChange(value - 1) }
            BasicText(value.toString(), Modifier.widthIn(min = 44.dp).padding(horizontal = 12.dp),
                style = rememberCouiTextStyle(CouiTextKind.Stepper).copy(color = colors.title, textAlign = TextAlign.Center))
            StepperControl(true, increaseDescription, enabled && value < valueRange.last) { onValueChange(value + 1) }
        }
    }
}

@Composable
private fun StepperControl(plus: Boolean, description: String, enabled: Boolean, click: () -> Unit) {
    val dark = CouiTheme.isDark
    val colors = CouiPageDefaults.themeColors(dark)
    val source = remember { MutableInteractionSource() }
    val press = rememberCouiPressMotion()
    val pressed by source.collectIsPressedAsState()
    val currentClick by rememberUpdatedState(click)
    var repeating by remember { mutableStateOf(false) }
    LaunchedEffect(pressed, repeating, enabled) {
        if (!pressed || !enabled) repeating = false
        else if (repeating) while (true) { delay(100); currentClick() }
    }
    Canvas(Modifier.size(32.dp).couiPressMotion(press, enabled).clip(CouiCapsuleShape)
        .background(Color(if (dark) 0x1AFFFFFF else 0x14000000))
        .couiButtonFeedback(dark, source, enabled)
        .semantics { contentDescription = description }.combinedClickable(source, null, enabled, role = Role.Button,
            onLongClick = { currentClick(); repeating = true }, onClick = click)) {
        val color = if (enabled) colors.title else colors.disabled
        val center = this.center
        val half = 7.6.dp.toPx()
        drawLine(color, center.copy(x = center.x - half), center.copy(x = center.x + half), 1.6.dp.toPx(), StrokeCap.Round)
        if (plus) drawLine(color, center.copy(y = center.y - half), center.copy(y = center.y + half), 1.6.dp.toPx(), StrokeCap.Round)
    }
}
