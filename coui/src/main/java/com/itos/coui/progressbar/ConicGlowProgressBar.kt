package com.itos.coui.progressbar

import android.animation.ValueAnimator
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.itos.coui.input.CouiSeekBarDragTick
import com.itos.coui.input.CouiSeekBarEdgeTick
import com.itos.coui.input.isCouiTouchCancelled
import com.itos.coui.input.performCouiSeekBarHaptic
import com.itos.coui.motion.CouiTouchMotion
import com.itos.coui.motion.CouiVelocityTracker
import com.itos.coui.theme.CouiTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 流体云歌曲进度样式。宿主持有 [progress]（0 到 1）和 [playing]。
 *
 * [onSeek] 为空时只展示进度。点击跳转在动画到达目标后提交归一化进度，
 * 横拖在松手时提交，键盘和无障碍调节即时提交。拖动期间提供预览和系统触觉反馈，
 * 取消后恢复宿主值。
 * 锥光会画到轨道外，外层容器可按需要裁剪。
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ConicGlowProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    playing: Boolean,
    color: Color = CouiTheme.colorScheme.controlAccent,
    trackColor: Color = Color(0x26FFFFFF),
    onSeek: ((Float) -> Unit)? = null,
) {
    require(progress.isFinite()) { "progress must be finite" }
    val value = progress.coerceIn(0f, 1f)
    val interactive = onSeek != null
    val commit by rememberUpdatedState(onSeek)
    val latestProgress by rememberUpdatedState(value)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current.density
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val motion = remember { ConicProgressMotion() }
    val touch = remember(scope, density) { CouiTouchMotion(scope, density, deformationEnabled = false) }
    val renderer = remember { ConicProgressRenderer() }
    var dragging by remember(interactive, rtl) { mutableStateOf(false) }
    val visibleProgress = when {
        dragging -> motion.dragProgress
        interactive -> motion.tapProgress ?: value
        else -> value
    }
    SideEffect {
        if (!interactive) motion.stopTap()
        motion.update(playing, dragging, visibleProgress, ValueAnimator.areAnimatorsEnabled())
    }
    DisposableEffect(motion) {
        onDispose { motion.dispose() }
    }
    DisposableEffect(touch) {
        onDispose { touch.dispose() }
    }

    val seekInput = if (interactive) {
        Modifier
            .semantics {
                setProgress { next ->
                    if (!next.isFinite()) false else {
                        motion.stopTap()
                        commit?.invoke(next.coerceIn(0f, 1f))
                        true
                    }
                }
            }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val step = CouiConicProgressDefaults.KeyIncrement
                val current = motion.tapProgress ?: value
                val next = when (event.key) {
                    Key.DirectionLeft -> current + if (rtl) step else -step
                    Key.DirectionRight -> current + if (rtl) -step else step
                    Key.DirectionUp -> current + step
                    Key.DirectionDown -> current - step
                    Key.MoveHome -> 0f
                    Key.MoveEnd -> 1f
                    else -> return@onKeyEvent false
                }
                motion.stopTap()
                commit?.invoke(next.coerceIn(0f, 1f))
                true
            }
            .focusable()
            .onGloballyPositioned { touch.coordinates = it }
            .pointerInput(rtl, touch, view) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    if (size.width <= 0) return@awaitEachGesture
                    val coordinates = touch.coordinates?.takeIf { it.isAttached }
                    val tracker = coordinates?.let { CouiVelocityTracker(down.uptimeMillis) }
                    var releaseVelocity = Offset.Zero
                    var lastDragTarget = 0f
                    fun railX(x: Float): Float = x - touch.translationX.value
                    fun fraction(x: Float): Float {
                        val width = size.width
                        val physical = (x / width).coerceIn(0f, 1f)
                        val logical = if (rtl) 1f - physical else physical
                        return quantizeSeek(logical)
                    }
                    try {
                        if (coordinates != null) tracker?.add(down, coordinates)
                        val start = Offset(railX(down.position.x), down.position.y)
                        if (ValueAnimator.areAnimatorsEnabled()) touch.begin(start, size) else touch.reset()
                        while (size.width > 0) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (event.isCouiTouchCancelled() || change.isConsumed) break
                            if (coordinates?.isAttached == true) tracker?.add(change, coordinates)
                            val position = change.position
                            // 原厂 View 接收扣除 translationX 后的本地坐标。
                            val localX = railX(position.x)
                            if (!change.pressed) {
                                releaseVelocity = Offset(tracker?.releaseVelocity()?.x ?: 0f, 0f)
                                if (dragging ||
                                    (localX in 0f..size.width.toFloat() && position.y in 0f..size.height.toFloat())
                                ) {
                                    change.consume()
                                    val target = fraction(localX)
                                    if (dragging) {
                                        commit?.invoke(target)
                                    } else {
                                        motion.moveTap(latestProgress, target, size.width, ValueAnimator.areAnimatorsEnabled()) {
                                            commit?.invoke(it)
                                        }
                                    }
                                }
                                break
                            }
                            val animate = ValueAnimator.areAnimatorsEnabled()
                            if (animate) touch.move(Offset(localX, start.y)) else touch.reset()
                            val dragStart = if (dragging) null else {
                                val delta = Offset(localX, position.y) - start
                                val horizontal = abs(delta.x)
                                val vertical = abs(delta.y)
                                if (maxOf(horizontal, vertical) <= viewConfiguration.touchSlop) {
                                    // 主事件阶段之后，父级仍可能消费这次移动。
                                    if (awaitPointerEvent(PointerEventPass.Final).changes.any {
                                            it.id == down.id && it.isConsumed
                                        }
                                    ) break
                                    continue
                                }
                                if (horizontal <= vertical) break
                                val current = motion.tapProgress ?: latestProgress
                                motion.stopTap()
                                lastDragTarget = quantizeSeek(current)
                                current
                            }
                            val target = fraction(localX)
                            motion.moveDrag(dragStart, target, animate)
                            dragging = true
                            if (target != lastDragTarget) {
                                lastDragTarget = target
                                view.performCouiSeekBarHaptic(
                                    if (target == 0f || target == 1f) CouiSeekBarEdgeTick else CouiSeekBarDragTick,
                                )
                            }
                            change.consume()
                        }
                    } finally {
                        motion.stopDrag()
                        dragging = false
                        if (ValueAnimator.areAnimatorsEnabled()) touch.release(releaseVelocity) else touch.reset()
                        tracker?.recycle()
                    }
                }
            }
    } else Modifier

    Box(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minWidth = 144.dp, minHeight = CouiConicProgressDefaults.ViewHeight)
            .progressSemantics(visibleProgress)
            .then(seekInput),
    ) {
        // Canvas 底层使用 Spacer，仅有最小高度时可能以零高度绘制。
        Canvas(Modifier.matchParentSize()) {
            translate(left = touch.translationX.value) {
                renderer.draw(
                    canvas = drawContext.canvas.nativeCanvas,
                    width = size.width,
                    centerY = (size.height.toInt() / 2).toFloat(),
                    height = minOf(size.height, CouiConicProgressDefaults.TrackHeight.toPx() +
                        CouiConicProgressDefaults.TrackExpansion.toPx() * motion.expansion),
                    progress = visibleProgress,
                    playing = playing,
                    dragging = dragging,
                    rtl = rtl,
                    color = color,
                    trackColor = trackColor,
                    motion = motion,
                )
            }
        }
    }
}

private fun quantizeSeek(progress: Float): Float {
    val steps = CouiConicProgressDefaults.SeekUnits
    return (progress * steps).roundToInt() / steps.toFloat()
}

internal object CouiConicProgressDefaults {
    val ViewHeight = 14.dp
    val TrackHeight = 4.dp
    val TrackExpansion = 2.dp
    const val SeekUnits = 1000
    const val KeyIncrement = .05f
    const val PlayingHighlight = .2f
    const val DraggingHighlight = .1f
    const val LightHeight = 450f
    const val LightPadding = 300f
    const val SpotRadius = 500f
}
