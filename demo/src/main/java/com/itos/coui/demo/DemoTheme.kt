package com.itos.coui.demo

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.shape.CouiShape
import com.kyant.capsule.ContinuousRoundedRectangle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.CouiPresets
import com.itos.coui.theme.LocalCouiDarkTheme

internal object DemoShapes {
    val contentCard = CouiShape(20.dp)
    val card = ContinuousRoundedRectangle(24.dp)
    val item = ContinuousRoundedRectangle(12.dp)
    val record = ContinuousRoundedRectangle(16.dp)
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
    val colors = if (materialTheme) {
        val dynamic = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // 页面与卡片分别使用 Material 的容器色和亮表面。
        dynamic.copy(surface = dynamic.surfaceContainer, background = dynamic.surfaceContainer)
    } else {
        val native = if (dark) CouiPresets.Dark else CouiPresets.Light
        val inverse = if (dark) CouiPresets.Light else CouiPresets.Dark
        // Demo 中的 M3 内容与采样背景也使用原生配色，避免从外层带入动态色。
        (if (dark) darkColorScheme() else lightColorScheme()).copy(
            primary = native.accent, onPrimary = native.onAccent,
            primaryContainer = native.container, onPrimaryContainer = native.onSurface,
            inversePrimary = inverse.accent,
            secondary = native.onSurfaceVariant, onSecondary = native.surface,
            secondaryContainer = native.container, onSecondaryContainer = native.onSurface,
            tertiary = native.accentOnSurface, onTertiary = native.onAccent,
            tertiaryContainer = native.surface, onTertiaryContainer = native.onSurface,
            background = native.background, onBackground = native.onSurface,
            surface = native.surface, onSurface = native.onSurface,
            surfaceVariant = native.container, onSurfaceVariant = native.onSurfaceVariant,
            surfaceTint = Color.Transparent,
            inverseSurface = inverse.surface, inverseOnSurface = inverse.onSurface,
            outline = native.onSurfaceVariant, outlineVariant = native.outlineVariant,
            surfaceBright = native.container, surfaceDim = native.background,
            surfaceContainer = native.background, surfaceContainerLow = native.surface,
            surfaceContainerLowest = native.background, surfaceContainerHigh = native.container,
            surfaceContainerHighest = native.container,
        )
    }
    val currentContent by rememberUpdatedState(content)
    // 切换局部外观时移动同一内容树，保留页面状态与 renderer 的生命周期。
    val scopedContent = remember { movableContentOf { currentContent() } }
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalCouiDarkTheme provides dark, LocalContentColor provides colors.onBackground) {
            if (materialTheme) scopedContent()
            else CouiTheme(darkTheme = dark, content = scopedContent)
        }
    }
}
