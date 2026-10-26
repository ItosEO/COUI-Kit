package com.itos.coui.effect

import android.animation.ArgbEvaluator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** 导航项过滤 state_pressed, COUIMaskRippleDrawable 实际只绘 hover -> focus。 */
@Composable
fun Modifier.couiNavigationFeedback(
    dark: Boolean,
    interactionSource: MutableInteractionSource,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val hover = rememberMaskProgress(hovered)
    val focus = rememberMaskProgress(focused)
    val inset = with(LocalDensity.current) { 8.dp.toPx() }
    return hoverable(interactionSource).drawBehind {
        val radius = size.height * 0.5f
        val maskSize = Size(size.width + inset * 2f, size.height)
        drawRoundRect(maskColor(hover, if (dark) 0x26FFFFFF else 0x14000000), Offset(-inset, 0f), maskSize, CornerRadius(radius))
        drawRoundRect(maskColor(focus, if (dark) 0x331A8CFF else 0x1F0080FF), Offset(-inset, 0f), maskSize, CornerRadius(radius))
    }
}

/** COUIFloatingButton 启用材质模糊时关闭 press mask, 保留 hover; 按压形变独立处理。 */
@Composable
fun Modifier.couiFabFeedback(dark: Boolean, interactionSource: MutableInteractionSource): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val hover = rememberMaskProgress(hovered)
    return hoverable(interactionSource).drawBehind {
        val radius = CornerRadius(size.minDimension * 0.5f)
        drawRoundRect(maskColor(hover, if (dark) 0x26FFFFFF else 0x14000000), cornerRadius = radius)
    }
}

/** 原厂顶栏材质过滤 pressed 通知, 仅保留 hover/focus 蒙层; 按压形变独立处理。 */
@Composable
fun Modifier.couiToolbarFeedback(
    dark: Boolean,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val hover = rememberMaskProgress(enabled && hovered)
    val focus = rememberMaskProgress(enabled && focused)
    return hoverable(interactionSource, enabled).drawBehind {
        val radius = CornerRadius(size.minDimension * 0.5f)
        drawRoundRect(maskColor(hover, if (dark) 0x26FFFFFF else 0x14000000), cornerRadius = radius)
        drawRoundRect(maskColor(focus, if (dark) 0x331A8CFF else 0x1F0080FF), cornerRadius = radius)
    }
}

@Composable
private fun rememberMaskProgress(active: Boolean): Float {
    val scope = rememberCoroutineScope()
    val effect = remember(scope) { CouiMaskProgress(scope) }
    SideEffect { effect.setActive(active) }
    return effect.progress.value
}

/** 普通按钮的状态蒙层；点击由组件唯一的 clickable 提交，材质分支关闭 pressed 蒙层。 */
@Composable
internal fun Modifier.couiButtonFeedback(
    dark: Boolean,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    pressFeedback: Boolean,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val hover = rememberMaskProgress(enabled && hovered)
    val focus = rememberMaskProgress(enabled && focused)
    val press = rememberMaskProgress(enabled && pressed && pressFeedback)
    return hoverable(interactionSource, enabled).drawBehind {
        drawRect(maskColor(hover, if (dark) 0x26FFFFFF else 0x14000000))
        drawRect(maskColor(focus, if (dark) 0x331A8CFF else 0x1F0080FF))
        drawRect(maskColor(press, if (dark) 0x33FFFFFF else 0x1F000000))
    }
}

/** 各状态沿用 StateEffectAnimator 的弹簧。 */
private class CouiMaskProgress(private val scope: CoroutineScope) {
    val progress = Animatable(0f, visibilityThreshold = 0.001f)
    private var active = false

    fun setActive(value: Boolean) {
        if (active == value) return
        active = value
        animate(if (value) 1f else 0f)
    }

    private fun animate(target: Float) {
        scope.launch {
            // response=.3, bounce=0; 与厂商 StateEffectAnimator 的物理参数一致。
            progress.animateTo(target, spring(1f, 438.64908f, 0.001f))
        }
    }
}

private val MaskEvaluator = ArgbEvaluator()

private fun maskColor(progress: Float, target: Int): Color =
    Color(MaskEvaluator.evaluate(progress.coerceIn(0f, 1f), 0, target) as Int)
