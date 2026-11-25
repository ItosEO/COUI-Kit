package com.itos.coui.material3

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.itos.coui.theme.CouiColorScheme
import com.itos.coui.theme.CouiStyle
import com.itos.coui.theme.CouiThemeDefaults
import com.itos.coui.theme.CouiTypography
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle

/**
 * 在组件消费处读取宿主 MaterialTheme，支持动态色、动画和嵌套主题。
 *
 * @param useHctColors 默认使用 HCT 生成控件强调色。传入 false 时直接使用 M3 色彩角色。
 */
class CouiMaterial3Style(
    private val useHctColors: Boolean = true,
) : CouiStyle {
    @Composable
    override fun themeDefaults(dark: Boolean): CouiThemeDefaults {
        val colors = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        val primary = colors.primary
        val accent = if (useHctColors) remember(primary) { enhanceHctColor(primary) } else primary
        val controlAccentOverride = if (useHctColors) {
            // 实色控件共用白色文字或标记，深色模式略提亮填色以维持层次。
            remember(primary, dark) { enhanceHctColor(primary, tone = if (dark) 49.0 else 45.0) }
        } else Color.Unspecified
        val checkboxAccent = when {
            useHctColors -> controlAccentOverride
            dark -> colors.inversePrimary
            else -> primary
        }
        return CouiThemeDefaults(
            CouiColorScheme(accent, colors.onPrimary, colors.surface, colors.surfaceBright,
                colors.onSurface, colors.onSurfaceVariant, colors.outlineVariant,
                menuSurface = colors.surface, checkboxAccent = checkboxAccent, menuCheckboxAccent = checkboxAccent,
                background = colors.background, controlAccentOverride = controlAccentOverride),
            CouiTypography(typography.bodyLarge, typography.bodySmall, typography.labelLarge,
                chipLabelFontFamily = typography.labelLarge.fontFamily ?: FontFamily.Default,
                dialogTitleFontFamily = typography.titleLarge.fontFamily ?: FontFamily.Default,
                dialogMessageFontFamily = typography.bodyMedium.fontFamily ?: FontFamily.Default,
                dialogButtonFontFamily = typography.labelLarge.fontFamily ?: FontFamily.Default,
                dialogInputFontFamily = typography.bodyLarge.fontFamily ?: FontFamily.Default,
                dialogCounterFontFamily = typography.labelSmall.fontFamily ?: FontFamily.Default),
            materialTint = colors.surface,
        )
    }

    @Composable
    override fun ProvideContent(color: Color, textStyle: TextStyle?, mergeTextStyle: Boolean, content: @Composable () -> Unit) {
        val resolvedColor = if (color.isSpecified) color else LocalContentColor.current
        // 与 M3 ProvideTextStyle 使用相同合并顺序；主题基线直接替换，组件局部样式才合并。
        val resolvedStyle = if (mergeTextStyle) LocalTextStyle.current.merge(textStyle) else textStyle ?: TextStyle.Default
        CompositionLocalProvider(
            LocalContentColor provides resolvedColor, LocalCouiContentColor provides resolvedColor,
            LocalTextStyle provides resolvedStyle, LocalCouiTextStyle provides resolvedStyle,
            content = content,
        )
    }

    @Composable
    override fun minimumInteractiveComponentSize(): Modifier = Modifier.minimumInteractiveComponentSize()
}
