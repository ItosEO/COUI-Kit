package com.itos.coui.floatingtoolbar

import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.LocalCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.CouiFloatingSurface
import com.itos.coui.effect.SpotlightPreset
import com.itos.coui.effect.couiFloatingSurface
import com.itos.coui.effect.couiToolbarFeedback
import com.itos.coui.effect.rememberCouiSpotlight
import com.itos.coui.motion.couiPressMotion
import com.itos.coui.motion.couiTouchMotion
import com.itos.coui.motion.rememberCouiPressMotion
import com.itos.coui.motion.rememberCouiTouchMotion
import com.itos.coui.theme.Coui
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.ProvideCouiContent

/** 禁用态内容淡化；与顶栏按钮的禁用内容色一致。 */
private const val DisabledContentAlpha = 0.38f

/**
 * 胶囊形悬浮操作栏，功能对齐 M3/Miuix 的 FloatingToolbar：一排同级操作共用一个玻璃表面。
 * [content] 是完整的 Row 插槽，可放任意数量、任意宽度的同级内容；[FloatingToolbarItem] 只是常用的
 * 可点击图标项，不参与材质绘制。
 *
 * 跟手光、位移回弹和形变由整条胶囊持有：按下时高光出现在触点处并随手指移动，胶囊随拖动一起位移、
 * 拉伸，因此同一栏内各操作共享一个指示区域，不各自叠加高光；装配方式与底栏中间胶囊一致。
 * 省略 [renderer] 时创建独立的单主体绘制器，宿主可先创建并处理失败，再显式传入。
 */
@RequiresApi(34)
@Composable
fun FloatingToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop = checkNotNull(LocalCouiBackdrop.current) {
        "悬浮操作栏需要显式 backdrop 或 COUI Scaffold 栏位提供的背景源"
    },
    dark: Boolean = CouiTheme.isDark,
    style: CouiFloatingToolbarStyle = CouiFloatingToolbarDefaults.style(dark),
    renderer: CouiFloatingRenderer = remember { CouiFloatingRenderer.standalone() },
    content: @Composable RowScope.() -> Unit,
) {
    var boundsSize by remember { mutableStateOf(IntSize.Zero) }
    val contentLayer = rememberGraphicsLayer()
    val motion = rememberCouiTouchMotion(true)
    val press = rememberCouiPressMotion()
    val (lightInput, drawLight) = rememberCouiSpotlight(
        interactive = true,
        preset = SpotlightPreset.OpaqueSmall2,
        dark = style.dark,
        color = style.lightColor,
    )
    Box(
        modifier
            .height(CouiFloatingToolbarDefaults.height)
            .onSizeChanged { boundsSize = it }
            .couiFloatingSurface(
                backdrop = backdrop,
                renderer = renderer,
                material = style.material,
                shadow = style.shadow,
                shape = style.shape,
                contentLayer = contentLayer,
                contourGlowEnabled = style.contourGlowEnabled,
            ) {
                CouiFloatingSurface(
                    bounds = Rect(Offset.Zero, Size(boundsSize.width.toFloat(), boundsSize.height.toFloat()))
                        .translate(Offset(motion.translationX.value, motion.translationY.value)),
                    scaleX = motion.scaleX.value + press.scale.value - 1f,
                    scaleY = motion.scaleY.value + press.scale.value - 1f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalCouiFloatingToolbarStyle provides style) {
            Row(
                Modifier.fillMaxHeight()
                    // 内容与材质读同一组弹簧值，避免缩放时图标和玻璃错位。
                    .graphicsLayer {
                        translationX = motion.translationX.value
                        translationY = motion.translationY.value
                        scaleX = motion.scaleX.value + press.scale.value - 1f
                        scaleY = motion.scaleY.value + press.scale.value - 1f
                    }
                    .then(
                        Modifier
                            // 拖动由操作栏接管，纵向滚动不会带着胶囊一起走。
                            .couiTouchMotion(motion, true, cancelOnConsumedMove = false, consumeDrag = true)
                            .couiPressMotion(press, true)
                            .then(lightInput),
                    )
                    .drawWithContent { contentLayer.record { this@drawWithContent.drawContent() } }
                    .clip(style.shape)
                    // 高光在操作内容之下、整条胶囊之内，图标按自身配色保持可见。
                    .drawBehind { drawLight?.invoke(this) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/**
 * 操作栏中的单个可点击项。点击、悬停、焦点语义与键盘焦点由本节点持有。
 * 最小宽度为 [CouiFloatingToolbarDefaults.itemSize]，宿主可用 `Modifier.width` 加宽。
 * 本节点不绘制表面也不显示按压高光：跟手光由 [FloatingToolbar] 整条提供，操作项只区分可用状态。
 * 内容色来自操作栏样式（默认 `onSurface` 的 80%），图标自带 `tint` 时以图标为准。
 */
@Composable
fun FloatingToolbarItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val style = LocalCouiFloatingToolbarStyle.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier
            .then(Coui.defaultStyle.minimumInteractiveComponentSize())
            .fillMaxHeight()
            .widthIn(min = CouiFloatingToolbarDefaults.itemSize)
            .couiToolbarFeedback(style.dark, interactionSource, enabled)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 禁用状态只淡化内容，不改变宿主或图标自带的颜色。
        Box(
            Modifier.then(if (enabled) Modifier else Modifier.alpha(DisabledContentAlpha)),
            contentAlignment = Alignment.Center,
        ) {
            if (style.contentColor.isSpecified) {
                ProvideCouiContent(color = style.contentColor) { content() }
            } else {
                content()
            }
        }
    }
}
