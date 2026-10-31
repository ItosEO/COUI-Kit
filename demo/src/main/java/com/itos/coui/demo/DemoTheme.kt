package com.itos.coui.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.shape.CouiShape
import com.kyant.capsule.ContinuousRoundedRectangle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiDarkTheme

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
    val currentContent by rememberUpdatedState(content)
    // 切换局部外观时移动同一内容树，保留页面状态与 renderer 的生命周期。
    val scopedContent = remember { movableContentOf { currentContent() } }
    // 页面使用 surfaceContainer，示例卡片独立使用 surfaceBright。
    MaterialTheme(colorScheme = colors.copy(surface = colors.surfaceContainer, background = colors.surfaceContainer)) {
        CompositionLocalProvider(LocalCouiDarkTheme provides dark) {
            if (materialTheme) scopedContent()
            else CouiTheme(darkTheme = dark, content = scopedContent)
        }
    }
}
