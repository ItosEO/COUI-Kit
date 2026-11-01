package com.itos.coui.scroll

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.withoutVisualEffect
import android.view.View
import android.view.ViewConfiguration
import android.os.SystemClock
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import com.itos.coui.input.isCouiTouchCancelled
import com.itos.coui.input.CouiHapticFeedback
import com.itos.coui.input.performCouiHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Settings COUIRecyclerView 的纵向越界阻尼和回弹；不绘制平台 glow / Compose stretch。
 * 每个滚动容器持有独立实例，传给 verticalScroll 的 overscrollEffect，不能多节点共用。
 * 只持有边界运动，常规滚动、列表数据与 fling owner 仍由宿主滚动容器提供。
 */
class CouiOverscrollEffect internal constructor(private val scope: CoroutineScope, private val screenHeight: Int,
    private val nominalFrameSeconds: Float, private val view: View,
    private val hapticFeedbackEnabled: Boolean) : OverscrollEffect {
    private var translation by mutableFloatStateOf(0f)
    private val physics = CouiScrollPhysics(1, springBackTensionMultiple = 2.15f, scrollView = false)
    private var animation: Job? = null
    private var touched = false
    private var flinging = false
    private var disposed = false
    private var overflowFling = false
    private var edgeStarted = false
    override val isInProgress: Boolean get() = translation != 0f || physics.running
    override val node: DelegatableNode = EdgeNode()

    override fun applyToScroll(delta: Offset, source: NestedScrollSource, performScroll: (Offset) -> Offset): Offset {
        if (disposed) return performScroll(delta)
        if (source != NestedScrollSource.UserInput) return performScroll(delta)
        stop()
        var remaining = delta.y
        var pre = 0f
        // 越界后先恢复边界，再把剩余位移交回内容；不会跨过 0 后吞掉正常滚动。
        if (translation * remaining < 0f) {
            val factor = (1f - (abs(translation) / screenHeight).coerceAtMost(1f)) * .4f
            val toEdge = -translation / factor.coerceAtLeast(.0001f)
            pre = if (abs(remaining) >= abs(toEdge)) toEdge else remaining
            translation = if (pre == toEdge) 0f else translation + damp(pre)
            remaining -= pre
        }
        val consumed = performScroll(Offset(delta.x, remaining))
        val available = remaining - consumed.y
        if (available != 0f) translation = (translation + damp(available)).coerceIn(-screenHeight.toFloat(), screenHeight.toFloat())
        return Offset(consumed.x, consumed.y + pre + available)
    }

    private fun damp(delta: Float): Float = ((delta.toInt() *
        (1f - (abs(translation) / screenHeight).coerceAtMost(1f))) / 5f * 2f).toInt().toFloat()

    override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) {
        stop()
        flinging = true
        try {
            var remainingVelocity = velocity
            if (translation != 0f) {
                // 原厂直接释放保留位置，通常以零速度 springBack；高速反向交还内容。
                val reverse = translation * velocity.y < 0 && abs(velocity.y) > 6000f
                if (!reverse) { rebound(); return }
                val y = reverseToBoundary(velocity.y)
                if (y == 0f) return
                remainingVelocity = Velocity(velocity.x, y)
            }
            val consumed = performFling(remainingVelocity)
            val remaining = remainingVelocity.y - consumed.y
            if (remaining != 0f) rebound(remaining)
            else if (translation != 0f) rebound()
        } finally {
            flinging = false
            if (!touched && translation != 0f && animation == null && !disposed) rebound()
        }
    }

    private suspend fun reverseToBoundary(fingerVelocity: Float): Float {
        val start = -translation
        physics.fling(start.toInt(), -fingerVelocity.toInt(), nominalFrameSeconds)
        var previous = withFrameNanos { it }
        val began = previous
        while (physics.running) {
            val frame = withFrameNanos { it }
            physics.update((frame - previous) / 1_000_000_000f, (frame - began) / 1_000_000f)
            previous = frame
            val position = physics.current
            if (position * start <= 0f) {
                val velocity = -physics.currentVelocity
                stop(); translation = 0f
                return velocity
            }
            translation = -position.toFloat()
        }
        if (!touched) rebound()
        return 0f
    }

    private fun rebound(fingerVelocity: Float = 0f) {
        if (disposed) return
        stop()
        overflowFling = fingerVelocity != 0f
        edgeStarted = false
        // ViewFlinger 首次用未消费速度进入越界分支时反馈一次，不在回弹帧循环内触发。
        if (overflowFling) view.performCouiHapticFeedback(CouiHapticFeedback.ScrollEdge, hapticFeedbackEnabled)
        if (overflowFling) physics.fling((-translation).toInt(), -fingerVelocity.toInt(), nominalFrameSeconds)
        else physics.returnToBounds((-translation).toInt(), 0)
        if (!physics.running) { translation = 0f; return }
        animation = scope.launch {
            var previous = withFrameNanos { it }
            val started = previous
            do {
                val now = withFrameNanos { it }
                physics.update((now - previous) / 1_000_000_000f, (now - started) / 1_000_000f)
                previous = now
                val raw = physics.current
                if (overflowFling) {
                    // onOverScrolled(scrollType=3) 与原厂物理坐标分开；不把阻尼后的位置回写积分器。
                    val limit = screenHeight * .3731f
                    translation = -(raw * limit / sqrt(raw.toDouble() * raw + limit * limit)).toInt().toFloat()
                    if (raw != 0 && !edgeStarted) { physics.returnToBounds(raw, 0, preserveVelocity = true); edgeStarted = true }
                } else translation = -raw.toFloat()
            } while (physics.running)
            translation = 0f
            animation = null
        }
    }

    private fun stop() { animation?.cancel(); animation = null; physics.stop() }
    internal fun release() { if (!flinging && translation != 0f) rebound() }
    internal fun dispose() { disposed = true; stop(); translation = 0f }

    private inner class EdgeNode : Modifier.Node(), LayoutModifierNode, PointerInputModifierNode {
        private var downPosition = Offset.Zero
        private var lastPosition = Offset.Zero
        private var downTime = 0L

        private fun finishTouch() {
            if (!touched) return
            val delta = lastPosition - downPosition
            val dx = delta.x.toInt()
            val dy = delta.y.toInt()
            val click = SystemClock.uptimeMillis() - downTime < ViewConfiguration.getLongPressTimeout() &&
                sqrt((dx.toDouble() * dx + dy.toDouble() * dy)).toInt() < 10
            touched = false
            // COUIRecyclerView.dispatchTouchEvent：越界的 UP/CANCEL 排除短点按，随后 springBack。
            if (translation != 0f && !click)
                view.performCouiHapticFeedback(CouiHapticFeedback.ScrollEdge, hapticFeedbackEnabled)
            if (!flinging && translation != 0f) rebound()
        }
        override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
            val child = measurable.measure(constraints)
            return layout(child.width, child.height) { child.placeWithLayer(0, translation.toInt()) }
        }
        override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
            if (pass != PointerEventPass.Initial) return
            val down = pointerEvent.changes.firstOrNull { it.pressed && !it.previousPressed }
            if (down != null && !touched) {
                touched = true; stop()
                downPosition = down.position; lastPosition = down.position; downTime = down.uptimeMillis
            }
            pointerEvent.changes.firstOrNull()?.let { lastPosition = it.position }
            if (pointerEvent.changes.none { it.pressed }) {
                finishTouch()
            }
        }
        override fun onCancelPointerInput() { finishTouch() }
        override fun onDetach() { touched = false; stop(); translation = 0f }
    }
}

/** 短内容、空内容同样响应越界拖动；关闭时向 verticalScroll 显式传 null。 */
@Composable
fun rememberCouiOverscrollEffect(hapticFeedbackEnabled: Boolean = true): CouiOverscrollEffect {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val height = view.resources.displayMetrics.heightPixels.coerceAtLeast(1)
    val refresh = checkNotNull(view.display).refreshRate
    require(refresh.isFinite() && refresh > 0f)
    val effect = remember(scope, height, refresh, view, hapticFeedbackEnabled) {
        CouiOverscrollEffect(scope, height, 1f / refresh, view, hapticFeedbackEnabled)
    }
    DisposableEffect(effect) { onDispose { effect.dispose() } }
    return effect
}


/** 二级页纵向滚动入口；空页和短页同样有 COUI 回弹，不安装平台 OverscrollEffect。 */
@Composable
fun Modifier.couiVerticalScroll(
    state: ScrollState,
    enabled: Boolean = true,
    overscrollEffect: CouiOverscrollEffect? = rememberCouiOverscrollEffect(),
): Modifier {
    val short = state.maxValue == 0
    val input = if (enabled && short && overscrollEffect != null) Modifier.pointerInput(state, overscrollEffect) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            try {
                val drag = awaitVerticalTouchSlopOrCancellation(down.id) { change, over ->
                    overscrollEffect.applyToScroll(Offset(0f, over), NestedScrollSource.UserInput) { Offset.Zero }
                    change.consume()
                }
                if (drag != null) while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == drag.id } ?: break
                    if (event.isCouiTouchCancelled() || !change.pressed) break
                    if (change.isConsumed) break
                    overscrollEffect.applyToScroll(Offset(0f, change.position.y - change.previousPosition.y),
                        NestedScrollSource.UserInput) { Offset.Zero }
                    change.consume()
                }
            } finally { overscrollEffect.release() }
        }
    } else Modifier
    // 首次测量前 maxValue 尚未确定。绘制节点始终由同一个外层位置持有，不能在长/短页
    // 切换时从 Foundation 迁到手动节点；新节点若遇到旧 owner 尚未 detach 会放弃挂载。
    val visual = if (enabled && overscrollEffect != null) Modifier.overscroll(overscrollEffect) else Modifier
    val scrollEvents = remember(overscrollEffect) { overscrollEffect?.withoutVisualEffect() }
    // 短页只保留 Foundation 的测量/ScrollState，避免零范围时的分发门控；子控件优先消费。
    return then(visual).then(input).verticalScroll(state,
        flingBehavior = rememberCouiScrollFlingBehavior(),
        overscrollEffect = if (enabled && !short) scrollEvents else null, enabled = enabled && !short)
}
