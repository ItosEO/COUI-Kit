package com.itos.coui.effect

import android.animation.ArgbEvaluator
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

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
        drawRoundRect(maskColor(hover.value, if (dark) 0x26FFFFFF else 0x14000000), Offset(-inset, 0f), maskSize, CornerRadius(radius))
        drawRoundRect(maskColor(focus.value, if (dark) 0x331A8CFF else 0x1F0080FF), Offset(-inset, 0f), maskSize, CornerRadius(radius))
    }
}

/** COUIFloatingButton 启用材质模糊时关闭 press mask, 保留 hover; 按压形变独立处理。 */
@Composable
fun Modifier.couiFabFeedback(dark: Boolean, interactionSource: MutableInteractionSource): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val hover = rememberMaskProgress(hovered)
    return hoverable(interactionSource).drawBehind {
        val radius = CornerRadius(size.minDimension * 0.5f)
        drawRoundRect(maskColor(hover.value, if (dark) 0x26FFFFFF else 0x14000000), cornerRadius = radius)
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
        drawRoundRect(maskColor(hover.value, if (dark) 0x26FFFFFF else 0x14000000), cornerRadius = radius)
        drawRoundRect(maskColor(focus.value, if (dark) 0x331A8CFF else 0x1F0080FF), cornerRadius = radius)
    }
}

@Composable
private fun rememberMaskProgress(active: Boolean): State<Float> {
    val effect = remember { CouiStateProgress() }
    SideEffect { effect.setActive(active) }
    DisposableEffect(effect) { onDispose { effect.dispose() } }
    return effect.progress
}

/** COUISwitch 的 hover/press 蒙层；触摸会话与点击识别取消分别收尾。 */
@Composable
internal fun Modifier.couiSwitchFeedback(
    dark: Boolean,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    touchPressed: Boolean,
): Modifier {
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val hover = rememberMaskProgress(enabled && hovered)
    val press = rememberMaskProgress(enabled && (pressed || touchPressed))
    return hoverable(interactionSource, enabled).drawBehind {
        if (enabled) {
            drawRect(maskColor(hover.value, if (dark) 0x26FFFFFF else 0x14000000))
            drawRect(maskColor(press.value, if (dark) 0x33FFFFFF else 0x1F000000))
        }
    }
}

/** COUIStrokeDrawable 的 TYPE_OUTER：沿项目形状画双倍笔宽，再裁掉轨道内部。 */
@Composable
internal fun Modifier.couiSwitchFocusOutline(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    color: Color,
    shape: Shape,
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val focus = rememberMaskProgress(enabled && focused)
    return drawWithCache {
        val path = (shape.createOutline(size, layoutDirection, this) as Outline.Generic).path
        // getDimensionPixelOffset(default_focus_stroke_radius) 先取整；外半笔宽为 3dp。
        val stroke = Stroke(3.dp.toPx().toInt() * 2f)
        onDrawBehind {
            if (enabled) clipPath(path, ClipOp.Difference) {
                drawPath(path, maskColor(focus.value, color.toArgb()), style = stroke)
            }
        }
    }
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
        drawRect(maskColor(hover.value, if (dark) 0x26FFFFFF else 0x14000000))
        drawRect(maskColor(focus.value, if (dark) 0x331A8CFF else 0x1F0080FF))
        drawRect(maskColor(press.value, if (dark) 0x33FFFFFF else 0x1F000000))
    }
}

private val MaskEvaluator = ArgbEvaluator()

private fun maskColor(progress: Float, target: Int): Color =
    Color(MaskEvaluator.evaluate(progress.coerceIn(0f, 1f), 0, target) as Int)
