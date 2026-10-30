package com.itos.coui.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.shape.CouiShape
import com.kyant.capsule.ContinuousRoundedRectangle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.CouiColorScheme
import com.itos.coui.theme.CouiTypography

internal object DemoShapes {
    val card = ContinuousRoundedRectangle(24.dp)
    val item = ContinuousRoundedRectangle(12.dp)
    val capsule = ContinuousRoundedRectangle(999.dp)
    val checkbox = ContinuousRoundedRectangle(4.dp)
    val checkboxBorder = ContinuousRoundedRectangle(3.2.dp)
    val menu = CouiShape(28.dp)
    val menuItem = ContinuousRoundedRectangle(28.dp)
    val couiCircle = CouiShape(999.dp, cornerWeight = 2f)
    val couiCapsule = CouiCapsuleShape
    val effect = CouiShape(24.dp)
}

@Composable
internal fun DemoTheme(dark: Boolean, materialTheme: Boolean = true, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    // 沿用 Metric 的页面表面层级, 示例卡片独立使用 surfaceBright。
    MaterialTheme(colorScheme = colors.copy(surface = colors.surfaceContainer, background = colors.surfaceContainer)) {
        if (materialTheme) CouiMaterial3Theme(dark, content)
        else CouiTheme(darkTheme = dark, content = content)
    }
}

/** Material 适配在宿主侧；核心既不读取 M3，也不自行创建动态色。 */
@Composable
internal fun CouiMaterial3Theme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    CouiTheme(darkTheme = dark,
        colorScheme = CouiColorScheme(colors.primary, colors.onPrimary, colors.surface, colors.surfaceBright,
            colors.onSurface, colors.onSurfaceVariant, colors.outlineVariant),
        typography = CouiTypography(MaterialTheme.typography.bodyLarge, MaterialTheme.typography.labelLarge,
            MaterialTheme.typography.bodyLarge),
        materialTint = colors.surface, content = content)
}
