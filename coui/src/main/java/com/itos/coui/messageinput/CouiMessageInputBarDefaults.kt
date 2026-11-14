package com.itos.coui.messageinput

import android.graphics.BlendMode
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingMaterial
import com.itos.coui.effect.CouiFloatingShadow
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.couiTextStyle
import com.itos.coui.theme.CouiTheme

@Immutable
data class CouiMessageInputBarColors(
    val content: Color,
    val hint: Color,
    val cursor: Color,
    val pressedIcon: Color,
)

object CouiMessageInputBarDefaults {
    val cornerRadius = 24.dp
    val shape = CouiShape(cornerRadius, cornerWeight = 2f)
    val minHeight = 50.dp
    internal val leadingActionSize = 40.dp
    internal val actionSize = 48.dp
    internal val iconSize = 24.dp
    internal val textStartWithLeadingAction = 46.dp
    internal val textStart = 16.dp
    internal val textEnd = 16.dp
    internal val textActionSpacing = 8.dp
    internal val leadingActionStart = 6.dp
    internal val leadingActionBottom = 5.dp
    internal val actionsEnd = 2.dp
    internal val actionsBottom = 1.dp
    internal val actionSpacing = (-10).dp
    internal val textPadding = 12.dp
    internal const val actionDurationMillis = 280
    internal const val hiddenIconScale = .8f

    @Composable
    fun colors(dark: Boolean = CouiTheme.isDark): CouiMessageInputBarColors {
        val theme = CouiTheme.colorSchemeFor(dark)
        return CouiMessageInputBarColors(theme.onSurface,
            theme.onSurface.copy(alpha = if (dark) .3f else .26f), theme.accent,
            theme.onSurface.copy(alpha = 138f / 255f))
    }

    @Composable
    fun textStyle(): TextStyle {
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        val family = CouiTheme.typography.body.fontFamily
        return remember(configuration, density, family) {
            val typeface = Typeface.create(Typeface.create("sans-serif-medium", Typeface.NORMAL), 500, false)
            val pixels = 16f * density.density * configuration.fontScale.coerceAtMost(1.15f)
            val style = couiTextStyle(typeface, 500, pixels, 1f, includeFontPadding = true, density = density)
            style.copy(fontFamily = family ?: style.fontFamily,
                lineHeight = with(density) { (style.lineHeight.toPx() + 3.dp.toPx()).toSp() })
        }
    }

    fun material(dark: Boolean): CouiFloatingMaterial = CouiFloatingMaterial(
        firstColor = Color(if (dark) 0x80666666 else 0xB3666666),
        firstBlendMode = if (dark) BlendMode.OVERLAY else BlendMode.COLOR_DODGE,
        secondColor = Color(if (dark) 0xB3262626 else 0x99F5F5F5),
        secondBlendMode = BlendMode.LUMINOSITY,
        blurRadius = 25f,
        edgeAlpha = if (dark) .8f else .25f,
        edgeWidthDp = 2.5f,
        innerGlowAlpha = if (dark) .03f else .1f,
    )

    fun shadow(dark: Boolean, density: Float, contourGlowEnabled: Boolean): CouiFloatingShadow {
        fun pixelSize(dp: Float): Float {
            val px = dp * density
            return (if (px >= 0f) px + .5f else px - .5f).toInt().toFloat()
        }
        return CouiFloatingShadow(
            elevation = pixelSize(if (contourGlowEnabled) 24f else 350f),
            color = Color.Black.copy(alpha =
                (if (contourGlowEnabled) 45f else if (dark) 120f else 40f) / 255f),
            lightY = pixelSize(if (contourGlowEnabled) -320f else -160f),
            lightZ = if (contourGlowEnabled) 0f else pixelSize(10000f),
            lightRadius = pixelSize(if (contourGlowEnabled) 1600f else 2000f),
            causticColor = Color(0x42FFFFFF),
            // 原厂光晕布局使用 getDimensionPixelOffset，保留截断语义。
            layout = Rect(0f, 0f, 0f, (4.9f * density).toInt().toFloat()),
            sdf = 10.4f * density,
            smoothCorner = true,
        )
    }
}
