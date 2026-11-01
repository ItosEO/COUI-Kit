package com.itos.coui.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itos.coui.checkbox.CouiCheckbox
import com.itos.coui.checkbox.CouiCheckboxDefaults
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.switch.CouiSwitch
import com.itos.coui.switch.CouiSwitchDefaults
import com.itos.coui.text.CouiText
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import androidx.compose.ui.platform.LocalView
import com.itos.coui.input.CouiHapticFeedback
import com.itos.coui.input.performCouiHapticFeedback

import com.itos.coui.settings.*

/** 标题、摘要、可选图标、右侧状态与跳转箭头。无 onClick 时保持信息项语义。 */
@Composable
internal fun DemoPreference(
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    showChevron: Boolean = onClick != null,
) {
    val style = LocalDemoSettingsStyle.current
    val source = remember { MutableInteractionSource() }
    DemoPreferenceRow(modifier.then(if (onClick != null) Modifier.couiButtonFeedback(style.dark, source, enabled, list = true)
        .clickable(source, null, enabled, role = Role.Button, onClick = onClick) else Modifier),
        title, summary, enabled, icon) {
        if (!value.isNullOrEmpty()) BasicText(value, Modifier.widthIn(max = 162.dp).padding(start = 16.dp),
            style = rememberCouiTextStyle(CouiTextKind.PreferenceValue).copy(
                color = if (enabled) style.colors.summary else style.colors.disabled, textAlign = TextAlign.End),
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (showChevron) DemoSettingsArrow(Modifier.padding(start = 4.dp).size(width = 12.dp, height = 24.dp),
            back = false, color = Color(if (style.dark) 0x4DFFFFFF else 0x42000000))
    }
}

/** 整行提交一次；子开关只绘制并共享反馈源，避免两个点击/无障碍节点。 */
@Composable
internal fun DemoSwitchPreference(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    loading: Boolean = false,
    loadingDescription: String? = null,
    hapticFeedbackEnabled: Boolean = true,
    loadingStyle: Boolean = false,
) {
    val style = LocalDemoSettingsStyle.current
    val view = LocalView.current
    val source = remember { MutableInteractionSource() }
    DemoPreferenceRow(modifier.couiButtonFeedback(style.dark, source, enabled && !loading, list = true)
        .toggleable(checked, source, null, enabled && !loading, Role.Switch) { target ->
            view.performCouiHapticFeedback(CouiHapticFeedback.Switch, hapticFeedbackEnabled)
            onCheckedChange(target)
        }, title, summary, enabled, icon) {
        CouiSwitch(Modifier.padding(start = 16.dp), checked, null, style.dark,
            CouiSwitchDefaults.colors(style.colors.accent, style.dark), enabled, source,
            // COUISwitch.canDrag 对 loadingStyle 始终返回 false，等待开始前也只接受点击。
            onDragCheckedChange = if (loadingStyle) null else onCheckedChange,
            loading = loading, loadingDescription = loadingDescription,
            hapticFeedbackEnabled = hapticFeedbackEnabled)
    }
}

/** 整行承担复选语义；子框只绘制，受控值由宿主维护。 */
@Composable
internal fun DemoCheckboxPreference(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(
        horizontal = CouiSettingsDefaults.rowPadding, vertical = CouiSettingsDefaults.contentPadding),
) {
    val style = LocalDemoSettingsStyle.current
    val source = remember { MutableInteractionSource() }
    DemoPreferenceRow(modifier.couiButtonFeedback(style.dark, source, enabled, list = true)
        .toggleable(checked, source, null, enabled, Role.Checkbox, onCheckedChange), title, summary, enabled, null, contentPadding) {
        CouiCheckbox(modifier = Modifier.padding(start = 16.dp).size(24.dp),
            checked = checked, enabled = enabled,
            colors = CouiCheckboxDefaults.colors(style.colors.accent, style.dark),
            shape = CouiCheckboxDefaults.shape, borderShape = CouiCheckboxDefaults.borderShape)
    }
}

/** 整行单选；选择组和选中业务值由宿主维护。 */
@Composable
internal fun DemoSelectPreference(
    modifier: Modifier = Modifier,
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val style = LocalDemoSettingsStyle.current
    val source = remember { MutableInteractionSource() }
    DemoPreferenceRow(modifier.couiButtonFeedback(style.dark, source, enabled, list = true)
        .selectable(selected, source, null, enabled, Role.RadioButton, onSelect), title, summary, enabled, icon) {
        Box(Modifier.padding(start = 16.dp).size(24.dp)) {
            if (selected) Icon(DemoIcons.Check, null, Modifier.fillMaxSize(),
                tint = if (enabled) style.colors.accent else style.colors.disabled)
        }
    }
}

@Composable
internal fun DemoPreferenceRow(modifier: Modifier, title: String, summary: String?, enabled: Boolean,
    icon: (@Composable () -> Unit)?,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(
        horizontal = CouiSettingsDefaults.rowPadding, vertical = CouiSettingsDefaults.contentPadding),
    trailing: @Composable () -> Unit) {
    val colors = LocalDemoSettingsStyle.current.colors
    CouiPreferenceLayout(modifier, leading = icon, contentPadding = contentPadding, headline = {
        CouiText(text = title, kind = CouiTextKind.PreferenceTitle, color = if (enabled) colors.title else colors.disabled,
            textAlign = TextAlign.Start)
    }, supporting = summary?.takeIf { it.isNotEmpty() }?.let { text -> @Composable {
        CouiText(text = text, kind = CouiTextKind.PreferenceSummary, color = if (enabled) colors.summary else colors.disabled,
            textAlign = TextAlign.Start)
    } }, trailing = trailing)
}

/** 原始 VectorDrawable 保留 RTL 镜像与原生 viewport。 */
@Composable
internal fun DemoSettingsArrow(modifier: Modifier, back: Boolean, color: Color) {
    Icon(if (back) DemoIcons.Back else DemoIcons.Next, null, modifier, tint = color)
}
