package com.itos.coui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 组件实际使用的色彩角色；页面染色由主题单独提供，不混入原生材质。 */
@Immutable
data class CouiColorScheme(
    val accent: Color,
    val onAccent: Color,
    val surface: Color,
    val container: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineVariant: Color,
    val accentOnSurface: Color = accent,
)

/** Theme.COUI 的蓝色强调色与中性表面；不读取壁纸动态色。 */
object CouiPresets {
    val Light = CouiColorScheme(Color(0xFF0080FF), Color.White, Color.White, Color.White,
        Color(0xE6000000), Color(0x8A000000), Color(0x1F000000))
    val Dark = CouiColorScheme(Color(0xFF1A8CFF), Color.White, Color(0xFF1E1E1E), Color(0xFF333333),
        Color(0xE6FFFFFF), Color(0x8AFFFFFF), Color(0x33FFFFFF), Color(0xFF5C9DFF))
}

/** 普通内容的字体预设；Dialog 保留自己的系统字体与行距策略。 */
@Immutable
data class CouiTypography(
    val body: TextStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = true)),
    val label: TextStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 0.sp,
        platformStyle = PlatformTextStyle(includeFontPadding = true)),
    val menuItem: TextStyle = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, fontWeight = FontWeight.Normal,
        platformStyle = PlatformTextStyle(includeFontPadding = true)),
)

private data class CouiThemeValues(val dark: Boolean, val colors: CouiColorScheme,
    val typography: CouiTypography, val materialTint: Color)

private val LocalCouiTheme = staticCompositionLocalOf<CouiThemeValues?> { null }
val LocalCouiContentColor = staticCompositionLocalOf { Color(0xE6000000) }
val LocalCouiTextStyle = staticCompositionLocalOf { CouiTypography().body }

object CouiTheme {
    val isDark: Boolean
        @Composable get() = LocalCouiTheme.current?.dark ?: isSystemInDarkTheme()
    val colorScheme: CouiColorScheme
        @Composable get() = colorSchemeFor(isDark)
    val typography: CouiTypography
        @Composable get() = LocalCouiTheme.current?.typography ?: CouiTypography()

    /** 组件覆盖深浅色时，不复用另一模式的宿主色板。 */
    @Composable
    fun colorSchemeFor(dark: Boolean): CouiColorScheme =
        LocalCouiTheme.current?.takeIf { it.dark == dark }?.colors ?: if (dark) CouiPresets.Dark else CouiPresets.Light

    /** 透明表示不做额外页面色相染色；显式材质配置始终由调用方负责。 */
    @Composable
    fun materialTintFor(dark: Boolean): Color =
        LocalCouiTheme.current?.takeIf { it.dark == dark }?.materialTint ?: Color.Transparent
}

/**
 * 可选外观上下文。materialTint 默认为透明；Material 宿主可显式传入页面表面色启用项目染色。
 * 不提供主题时，组件按系统深浅色使用原生预设。业务状态及资源生命周期仍由原接口管理。
 */
@Composable
fun CouiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: CouiColorScheme = if (darkTheme) CouiPresets.Dark else CouiPresets.Light,
    typography: CouiTypography = CouiTypography(),
    materialTint: Color = Color.Transparent,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalCouiTheme provides CouiThemeValues(darkTheme, colorScheme, typography, materialTint),
        LocalCouiContentColor provides colorScheme.onSurface,
        LocalCouiTextStyle provides typography.body,
        content = content,
    )
}

@Composable
internal fun ProvideCouiTextStyle(style: TextStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCouiTextStyle provides LocalCouiTextStyle.current.merge(style), content = content)
}
