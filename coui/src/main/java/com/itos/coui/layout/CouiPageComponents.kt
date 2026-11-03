package com.itos.coui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.shape.CouiShape
import com.itos.coui.theme.CouiTheme
import com.itos.coui.text.CouiText
import com.itos.coui.text.CouiTextKind

/** 独立配色值；不创建页面主题作用域，也不改变内容槽内其他组件的主题。 */
@Immutable
data class CouiPageColors(val background: Color, val card: Color, val title: Color, val summary: Color,
    val disabled: Color, val accent: Color, val divider: Color = title.copy(alpha = .12f))

object CouiPageDefaults {
    val horizontalMargin = 16.dp
    val rowPadding = 16.dp
    val rowMinHeight = 48.dp
    val homeRowMinHeight = 52.dp
    val iconSize = 24.dp
    val contentPadding = 10.dp
    val summarySpacing = 2.dp
    val groupPadding = 0.dp
    val groupSpacing = 16.dp
    val categoryTopSpacing = 12.dp
    val shape = CouiShape(24.dp, 3f)
    val toolbarHeight = 52.dp
    val dividerHeight = 0.33.dp
    /** 消费当前原生/可选宿主策略；显式 colors 仍优先。 */
    @Composable
    fun themeColors(dark: Boolean = CouiTheme.isDark): CouiPageColors {
        val theme = CouiTheme.colorSchemeFor(dark)
        return CouiPageColors(theme.surface, theme.container, theme.onSurface,
            theme.onSurfaceVariant, theme.onSurface.copy(alpha = .3f), theme.accent, theme.outlineVariant)
    }
    fun colors(accent: Color, dark: Boolean) = CouiPageColors(
        Color(if (dark) 0xFF000000 else 0xFFF0F1F2), Color(if (dark) 0x1AFFFFFF else 0xFFFFFFFF),
        Color(if (dark) 0xE6FFFFFF else 0xE6000000), Color(if (dark) 0x8AFFFFFF else 0x8A000000),
        Color(if (dark) 0x4DFFFFFF else 0x4D000000), accent)
}

/**
 * 一组相邻设置项共享项目平滑圆角；条件隐藏的条目不保留空白或分组端点。
 * 内部顶部留白按标题选择 Defaults；modifier.padding 在默认留白之外累加。
 * colors 只控制本组卡片、标题和说明；任意内容槽继续使用各组件自己的主题或显式参数。
 */
@Composable
fun CouiGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    colors: CouiPageColors = CouiPageDefaults.themeColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val topSpacing = if (title.isNullOrEmpty()) CouiPageDefaults.groupSpacing else CouiPageDefaults.categoryTopSpacing
    Column(modifier.fillMaxWidth().padding(top = topSpacing)) {
        if (!title.isNullOrEmpty()) Row(Modifier.padding(horizontal = 32.dp, vertical = 8.dp).heightIn(min = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            CouiText(text = title, kind = CouiTextKind.GroupTitle, color = colors.summary, modifier =
                Modifier.weight(1f).semantics { heading() }, textAlign = TextAlign.Start)
            headerActions()
        }
        Column(Modifier.padding(horizontal = CouiPageDefaults.horizontalMargin).fillMaxWidth()
            .clip(CouiPageDefaults.shape).background(colors.card)
            .padding(vertical = CouiPageDefaults.groupPadding), content = content)
        if (!footer.isNullOrEmpty()) CouiFooter(text = footer, color = colors.summary)
    }
}

/** 自定义可点击设置项。只拥有自身点击与反馈，不改变内容槽的主题或业务状态。 */
@Composable
fun CouiItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    dark: Boolean = CouiTheme.isDark,
    content: @Composable RowScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Row(modifier.fillMaxWidth().heightIn(min = CouiPageDefaults.rowMinHeight)
        .couiButtonFeedback(dark, source, enabled, list = true)
        .clickable(source, null, enabled, role = Role.Button, onClick = onClick)
        .semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically, content = content)
}

/** 卡片内部的细分隔。默认至少一个物理像素，实际高度服从宿主约束。 */
@Composable
fun CouiDivider(modifier: Modifier = Modifier,
    startIndent: androidx.compose.ui.unit.Dp = CouiPageDefaults.rowPadding,
    endIndent: androidx.compose.ui.unit.Dp = CouiPageDefaults.rowPadding,
    color: Color = CouiTheme.colorScheme.outlineVariant) {
    val thickness = with(LocalDensity.current) {
        CouiPageDefaults.dividerHeight.roundToPx().coerceAtLeast(1).toDp()
    }
    Box(modifier.fillMaxWidth().padding(start = startIndent, end = endIndent)
        .height(thickness)
        .background(color))
}

@Composable
fun CouiFooter(modifier: Modifier = Modifier, text: String,
    color: Color = CouiTheme.colorScheme.onSurfaceVariant) {
    CouiText(text = text, kind = CouiTextKind.Description, color = color, modifier =
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp), textAlign = TextAlign.Start)
}

/** 可装配的行布局；宿主替换 leading/headline/supporting/trailing，交互由外层 Modifier 拥有。 */
@Composable
fun CouiPreferenceLayout(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    supporting: (@Composable () -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(
        horizontal = CouiPageDefaults.rowPadding, vertical = CouiPageDefaults.contentPadding),
    leadingSize: androidx.compose.ui.unit.Dp = CouiPageDefaults.iconSize,
    headline: @Composable () -> Unit,
) {
    Row(modifier.fillMaxWidth().defaultMinSize(minHeight = CouiPageDefaults.rowMinHeight).semantics(mergeDescendants = true) {}
        .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically) {
        if (leading != null) Box(Modifier.padding(end = 16.dp).size(leadingSize), contentAlignment = Alignment.Center) { leading() }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CouiPageDefaults.summarySpacing)) {
            headline()
            supporting?.invoke()
        }
        trailing()
    }
}
