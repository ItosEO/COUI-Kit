package com.itos.coui.material3

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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

/** 在组件消费处读取宿主 MaterialTheme，支持动态色、动画和嵌套主题。 */
class CouiMaterial3Style : CouiStyle {
    @Composable
    override fun themeDefaults(dark: Boolean): CouiThemeDefaults {
        val colors = MaterialTheme.colorScheme
        val typography = MaterialTheme.typography
        val checkboxAccent = if (dark) colors.inversePrimary else colors.primary
        return CouiThemeDefaults(
            CouiColorScheme(colors.primary, colors.onPrimary, colors.surface, colors.surfaceBright,
                colors.onSurface, colors.onSurfaceVariant, colors.outlineVariant,
                accentOnSurface = colors.primary, menuSurface = colors.surface,
                checkboxAccent = checkboxAccent, menuCheckboxAccent = checkboxAccent,
                background = colors.background),
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
