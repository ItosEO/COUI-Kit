package com.itos.coui.menu

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itos.coui.checkbox.CouiCheckboxColors
import com.itos.coui.checkbox.CouiCheckboxDefaults
import com.itos.coui.effect.SpotlightDiagnostic
import com.itos.coui.shape.CouiShape
import com.itos.coui.theme.CouiTheme

data class CouiMenuColors(
    val surface: Color,
    val container: Color,
    val primary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineVariant: Color,
)

object CouiMenuDefaults {
    const val spotlightDiffuse: Float = 0.5f
    val radius = 28.dp
    val shape = CouiShape(radius)
    val itemShape = CouiShape(radius)

    @Composable
    fun style(dark: Boolean = CouiTheme.isDark): CouiMenuStyle {
        val colors = CouiTheme.colorSchemeFor(dark)
        return CouiMenuStyle(
            colors = CouiMenuColors(colors.container, colors.container, colors.accentOnSurface,
                colors.onSurface, colors.onSurfaceVariant, colors.outlineVariant),
            shape = shape, itemShape = itemShape,
            checkboxShape = CouiCheckboxDefaults.shape, checkboxBorderShape = CouiCheckboxDefaults.borderShape,
            checkboxColors = CouiCheckboxDefaults.colors(colors.accentOnSurface, dark), radius = radius,
            itemTextStyle = CouiTheme.typography.menuItem, headerTextStyle = CouiTheme.typography.label,
            themeTint = CouiTheme.materialTintFor(dark),
        )
    }
}

/** 仅描述宿主外观; COUI 不读取应用主题、偏好或日志。 */
data class CouiMenuStyle(
    val colors: CouiMenuColors,
    val shape: Shape,
    val itemShape: Shape,
    val checkboxShape: Shape,
    val checkboxBorderShape: Shape,
    val checkboxColors: CouiCheckboxColors,
    val radius: Dp,
    val itemTextStyle: TextStyle,
    val headerTextStyle: TextStyle,
    val themeTint: Color = Color.Transparent,
    val spotlightDiffuse: Float? = null,
)

internal val LocalCouiMenuStyle = staticCompositionLocalOf<CouiMenuStyle> {
    error("菜单样式必须由 CouiMenu 提供")
}
internal val LocalCouiMenuDiagnostic = staticCompositionLocalOf<((SpotlightDiagnostic) -> Unit)?> { null }
