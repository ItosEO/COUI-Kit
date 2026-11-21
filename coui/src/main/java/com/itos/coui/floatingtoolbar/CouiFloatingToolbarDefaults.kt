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
 * 尺寸取自系统便签 17.0.30 的 `coui_floating_toolbar_*` 资源：
 * 栏高 44dp、项宽 44dp、项高 40dp、项间距 4dp、图标 24dp、两项以上时两侧各留 6dp。
 * 操作项按图标按钮处理：只承担点击与悬停，不各自绘制玻璃表面。
 * 组件不按位置或颜色区分操作项，多色图标与单色图标反馈完全一致。
 */
object CouiFloatingToolbarDefaults {
    /** `coui_floating_toolbar_menu_view_height`。 */
    val height = 44.dp

    /** `coui_floating_toolbar_item_width`；宿主可用 `Modifier.width` 加宽。 */
    val itemSize = 44.dp

    /** `coui_floating_toolbar_item_height`：项比栏矮 4dp，上下各留 2dp。 */
    val itemHeight = 40.dp

    /** `coui_floating_toolbar_item_view_space`：删除时先扩到 1.75 倍，插入时先压到 0.25 倍再收拢。 */
    val itemSpacing = 4.dp

    /**
     * `coui_floating_toolbar_menu_view_item_bg_inset_horizontal`：胶囊边缘到首/末操作项的距离。
     * 项只在相邻一侧带半个间距，两端直接补这个值；单项时原厂取 0。
     */
    val edgeInset = 6.dp

    /** `coui_floating_toolbar_item_single_mask_inset_horizontal`：单项时蒙版左右各内缩 2dp。 */
    val singleItemFeedbackInset = 2.dp

    /** `coui_floating_toolbar_item_icon_width`：项内左右各留 10dp。 */
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
