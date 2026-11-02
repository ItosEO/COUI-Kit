package com.itos.coui.scroll

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.withoutVisualEffect
import android.view.View
import android.view.ViewConfiguration
import android.os.SystemClock
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import com.itos.coui.input.CouiHapticFeedback
import com.itos.coui.input.isCouiTouchCancelled
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign
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
    private var fling: Job? = null
    private var touched = false
    private var disposed = false
    private var overflowFling = false
    override val isInProgress: Boolean get() = translation != 0f || physics.running
    override val node: DelegatableNode = EdgeNode()

    override fun applyToScroll(delta: Offset, source: NestedScrollSource, performScroll: (Offset) -> Offset): Offset {
        if (disposed) return performScroll(delta)
        if (source != NestedScrollSource.UserInput) {
            val consumed = performScroll(delta)
            val available = delta.y - consumed.y
            val edge = -available.toInt()
            if (fling != null && edge != 0) {
                // ViewFlinger 先绘制本帧剩余位移，再把速度交给 notifyVerticalEdgeReached。
                physics.returnToBounds(edge, 0, preserveVelocity = true, initialVelocity = 0f)
                translation = overFlingTranslation(physics.current)
                if (!overflowFling) view.performCouiHapticFeedback(CouiHapticFeedback.ScrollEdge, hapticFeedbackEnabled)
                overflowFling = true
            }
            // 保留未消费量，使内容 fling 停在边界，并由父链决定剩余速度。
            return consumed
        }
        stop()
        overflowFling = false
        var remaining = delta.y
        var pre = 0f
        // 越界后先恢复边界，再把剩余位移交回内容；不会跨过 0 后吞掉正常滚动。
        if (translation * remaining < 0f) {
            // RecyclerView 的纵向反向分支用 scrollX；本入口只滚动纵轴，scrollX 为 0。
            // 先按原厂整数阻尼求到边界所需输入，再交还跨边界的剩余位移。
            val toEdge = -sign(translation) * ceil(abs(translation) * 5f / 2f)
            pre = if (abs(remaining) >= abs(toEdge)) toEdge else remaining
            translation = if (pre == toEdge) 0f else translation + damp(pre, 0f)
            remaining -= pre
        }
        val consumed = performScroll(Offset(delta.x, remaining))
        val available = remaining - consumed.y
        if (available != 0f) translation += damp(available)
        return Offset(consumed.x, consumed.y + pre + available)
    }

    private fun damp(delta: Float, displacement: Float = translation): Float = ((delta.toInt() *
        (1f - (abs(displacement) / screenHeight).coerceAtMost(1f))) / 5f * 2f).toInt().toFloat()

    override suspend fun applyToFling(velocity: Velocity, performFling: suspend (Velocity) -> Velocity) = coroutineScope {
        stop()
        val job = coroutineContext.job
        fling = job
        if (translation == 0f) overflowFling = false
        try {
            var remainingVelocity = velocity
            if (translation != 0f) {
                // 原厂只给高速释放保留速度：同向 springBack，反向恢复边界后交还内容。
                val fast = abs(velocity.y) > 6000f
                val reverse = translation * velocity.y < 0 && fast
                val y = if (reverse) reverseToBoundary(velocity.y) else 0f
                if (y == 0f) rebound(if (fast && !reverse) velocity.y else 0f, overFling = overflowFling)
                remainingVelocity = Velocity(velocity.x, y)
            }
            val consumed = performFling(remainingVelocity)
            val remaining = remainingVelocity.y - consumed.y
            if (translation != 0f && animation == null) {
                if (overflowFling) rebound(remaining, from = physics.current, overFling = true) else rebound()
            }
        } finally {
            // stop() 先撤销 owner；旧协程的 finally 不能重新启动新手势已接管的回弹。
            if (fling === job) {
                fling = null
                if (!touched && translation != 0f && animation == null && !disposed) rebound(overFling = overflowFling)
            }
        }
    }

    private suspend fun reverseToBoundary(fingerVelocity: Float): Float {
        val start = -translation
        physics.fling(start.toInt(), -fingerVelocity.toInt(), nominalFrameSeconds)
        var previous = System.nanoTime()
        val began = previous
        while (physics.running) {
            val frame = withFrameNanos { it }
            physics.update((frame - previous) / 1_000_000_000f, (frame - began) / 1_000_000f)
            previous = frame
            val position = physics.current
            if (position * start <= 0f) {
                val velocity = -physics.currentVelocity
                physics.stop(); translation = 0f
                return velocity
            }
            translation = -position.toFloat()
        }
        return 0f
    }

    internal fun absorbFling(fingerVelocity: Float): Boolean {
        if (fling == null || !overflowFling) return false
        rebound(fingerVelocity, from = physics.current, overFling = true)
        return true
    }

    private fun rebound(fingerVelocity: Float = 0f, from: Int = (-translation).toInt(), overFling: Boolean = false) {
        if (disposed) return
        animation?.cancel()
        animation = null
        physics.stop()
        overflowFling = overFling
        physics.returnToBounds(from, 0, initialVelocity = -fingerVelocity)
        if (!physics.running) { translation = 0f; return }
        animation = scope.launch {
            var previous = System.nanoTime()
            val started = previous
            do {
                val now = withFrameNanos { it }
                physics.update((now - previous) / 1_000_000_000f, (now - started) / 1_000_000f)
                previous = now
                // onOverScrolled(scrollType=3) 不把显示映射后的坐标回写积分器。
                translation = if (overflowFling) overFlingTranslation(physics.current) else -physics.current.toFloat()
            } while (physics.running)
            translation = 0f
            animation = null
        }
    }

    private fun overFlingTranslation(position: Int): Float {
        val limit = screenHeight * .3731f
        return -((position * limit).toDouble() / sqrt((position * position + limit * limit).toDouble())).toInt().toFloat()
    }

    private fun stop() {
        val runningFling = fling
        fling = null
        runningFling?.cancel()
        animation?.cancel()
        animation = null
        physics.stop()
    }
    internal fun dispose() { disposed = true; stop(); translation = 0f }

    private inner class EdgeNode : Modifier.Node(), LayoutModifierNode, PointerInputModifierNode {
        private var downPosition = Offset.Zero
        private var lastPosition = Offset.Zero
        private var downTime = 0L

        private fun finishTouch(cancelled: Boolean = false) {
            if (!touched) return
            val delta = lastPosition - downPosition
            val dx = delta.x.toInt()
            val dy = delta.y.toInt()
            val click = SystemClock.uptimeMillis() - downTime < ViewConfiguration.getLongPressTimeout() &&
                sqrt((dx.toDouble() * dx + dy.toDouble() * dy)).toInt() < 10
            touched = false
            // dispatchTouchEvent 只在真实 UP 把 scrollType=3 改为 2；CANCEL 保留映射，
            // 但 springBack 的起点仍取当前可见位置，不沿用上次积分器的 raw position。
            if (!cancelled) overflowFling = false
            // COUIRecyclerView.dispatchTouchEvent：越界的 UP/CANCEL 排除短点按，随后 springBack。
            if (translation != 0f && !click)
                view.performCouiHapticFeedback(CouiHapticFeedback.ScrollEdge, hapticFeedbackEnabled)
            if (fling == null && translation != 0f) rebound(overFling = overflowFling)
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
                finishTouch(cancelled = pointerEvent.isCouiTouchCancelled())
            }
        }
        override fun onCancelPointerInput() { finishTouch(cancelled = true) }
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
    // 首次测量前 maxValue 尚未确定。绘制节点始终由同一个外层位置持有，不能在长/短页
    // 切换时从 Foundation 迁到手动节点；新节点若遇到旧 owner 尚未 detach 会放弃挂载。
    val visual = if (enabled && overscrollEffect != null) Modifier.overscroll(overscrollEffect) else Modifier
    val scrollEvents = remember(overscrollEffect) { overscrollEffect?.withoutVisualEffect() }
    val flingBehavior = rememberCouiScrollFlingBehavior(overscrollEffect)
    // 零范围 ScrollState 会关闭 overscroll 分发。无内容位移的 Foundation state 只负责
    // 短页手势、速度、多指和完整 nested scroll；不伪造 ScrollState 的可滚动范围。
    val shortContent = rememberScrollableState { 0f }
    val input = if (short) Modifier.scrollable(shortContent, Orientation.Vertical,
        overscrollEffect = scrollEvents, enabled = enabled, reverseDirection = true,
        flingBehavior = flingBehavior) else Modifier
    return then(visual).then(input).verticalScroll(state,
        flingBehavior = flingBehavior,
        overscrollEffect = if (enabled && !short) scrollEvents else null, enabled = enabled && !short)
}
