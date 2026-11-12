package com.itos.coui.floatingtoolbar

import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingMaterial
import com.itos.coui.effect.CouiFloatingShadow
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults

/**
 * 悬浮操作栏及其操作项的样式；材质与阴影需要 API 29 构造，装配仍要求 API 34。
 * [contentColor] 默认取 [dark] 对应配色 `onSurface` 的 80%；传入 `Color.Unspecified` 时不提供，图标退回环境内容色。
 */
data class CouiFloatingToolbarStyle(
    val dark: Boolean,
    val shape: Shape,
    val material: CouiFloatingMaterial,
    val shadow: CouiFloatingShadow,
    val contentColor: Color,
    val lightColor: Color,
    val contourGlowEnabled: Boolean,
)

internal val LocalCouiFloatingToolbarStyle = staticCompositionLocalOf<CouiFloatingToolbarStyle> {
    error("悬浮操作栏的按钮项必须位于 FloatingToolbar 内")
}

/**
 * 栏高与单个操作项的最小宽度一致，操作项按图标按钮处理：只承担点击与悬停，不各自绘制玻璃表面。
 * 组件不按位置或颜色区分操作项，多色图标与单色图标反馈完全一致。
 */
object CouiFloatingToolbarDefaults {
    val height = 48.dp
    val itemSize = 48.dp

    /**
     * 系统胶囊的操作项间距为 48dp、图标为 24dp，两侧各留 12dp；图标按 24dp 取尺寸，
     * 与系统同间距下才会得到同一处内外边距，缩到 20dp 会让操作项显得空。
     */
    val iconSize = 24.dp

    /** 玻璃上的图标相对正文色的透明度；正文色本身是 90%，直接使用偏重。 */
    private const val FloatingToolbarIconAlpha = 0.8f

    /**
     * 背景模糊半径，单位是设备像素，与 [CouiFloatingMaterial.blurRadius] 一致。
     * CAPSULE_1 在顶栏里不带模糊，因为顶栏背景本身已渐进模糊；操作栏单独浮在正文上，
     * 沿用库内搜索胶囊同一档 20px 模糊，避免正文透过玻璃后仍可辨认。
     */
    private const val FloatingToolbarBlurRadius = 20f

    /** 与底栏中间胶囊、搜索胶囊共用的胶囊轮廓。 */
    val capsuleShape: Shape = CouiCapsuleShape

    @RequiresApi(29)
    @Composable
    fun style(
        dark: Boolean = CouiTheme.isDark,
        themeTint: Color = CouiTheme.materialTintFor(dark),
    ): CouiFloatingToolbarStyle {
        val density = LocalDensity.current.density
        val contour = LocalCouiContourGlowEnabled.current
        val colors = CouiTheme.colorSchemeFor(dark)
        return CouiFloatingToolbarStyle(
            dark = dark,
            shape = capsuleShape,
            // CAPSULE_1 系统胶囊材质与阴影；不叠加顶栏按钮的加深特化，只补上浮在正文上所需的背景模糊。
            material = CouiTopAppBarButtonDefaults.systemMaterial(dark, capsule = true)
                .copy(themeTint = themeTint, blurRadius = FloatingToolbarBlurRadius),
            shadow = CouiTopAppBarButtonDefaults.shadow(
                dark = dark, capsule = true, density = density, contour = contour, search = true,
            ),
            // 兜底内容色：取正文色但压到 80%，避免纯白/纯黑压在透明材质上过重；显式 tint 仍然优先。
            contentColor = colors.onSurface.copy(alpha = FloatingToolbarIconAlpha),
            lightColor = Color.White,
            contourGlowEnabled = contour,
        )
    }
}
