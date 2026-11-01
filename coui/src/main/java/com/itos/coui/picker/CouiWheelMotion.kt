package com.itos.coui.picker

import android.content.Context
import android.view.ViewConfiguration
import android.view.animation.PathInterpolator
import android.widget.Scroller
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/** Clock 17 COUINumberPicker 的两路 Scroller；不使用 LazyList 的另一套 fling/snap。 */
internal class CouiWheelMotion(private val context: Context, private val scope: CoroutineScope,
    initial: Int, val range: IntRange, val wrap: Boolean, val step: Float, private val diffusion: Float) {
    var value by mutableIntStateOf(initial)
        private set
    var offset by mutableFloatStateOf(0f)
        private set
    var onChange: (Int) -> Unit = {}
    private val fling = Scroller(context, PathInterpolator(0f, .23f, .1f, 1f))
    private val adjust = Scroller(context, PathInterpolator(0f, 0f, .4f, 1f))
    private var job: Job? = null
    private var controlled = initial
    private var disposed = false
    var busy by mutableStateOf(false)
        private set
    private var lastDelta = 0
    private val pitch get() = step + diffusion
    fun sync(next: Int) {
        if (next == controlled) return
        controlled = next
        if (next != value) { stop(); value = next; offset = 0f }
    }
    fun display(relative: Int): Int? {
        val next = value + relative
        return if (wrap) range.first + Math.floorMod(next - range.first, range.count())
            else next.takeIf { it in range }
    }
    fun begin() { if (disposed) return; stop(); busy = true; lastDelta = 0 }
    fun stop() { job?.cancel(); job = null; fling.forceFinished(true); adjust.forceFinished(true); busy = false }
    fun reconcile(next: Int) { if (!busy && value != next) { value = next; offset = 0f } }
    fun reset(next: Int) { stop(); value = next; offset = 0f }
    fun dispose() { disposed = true; stop() }
    fun drag(delta: Float) { if (disposed) return; lastDelta = delta.toInt(); scroll(lastDelta) }
    private fun scroll(delta: Int) {
        if (!wrap && ((value == range.first && delta > 0 && offset + delta >= 0) ||
                (value == range.last && delta < 0 && offset + delta <= 0))) { offset = 0f; return }
        offset += delta
        while (offset > step * .95f + diffusion) {
            val next = display(-1) ?: run { offset = 0f; return }
            offset = (offset - pitch).toInt().toFloat(); change(next)
        }
        while (offset < -step * .95f - diffusion) {
            val next = display(1) ?: run { offset = 0f; return }
            offset = (offset + pitch).toInt().toFloat(); change(next)
        }
    }
    private fun change(next: Int) { if (value != next) { value = next; onChange(next) } }
    fun end(velocity: Float, cancelled: Boolean) {
        if (disposed) return
        val raw = velocity.coerceIn(-5000f, 5000f).toInt()
        if (!cancelled && abs(raw) > 750) {
            val speed = (raw * 1.6f).toInt()
            val decelerationRate = ln(.78) / ln(.9)
            val physical = context.resources.displayMetrics.density * 160f * 386.0878f * .84f
            val friction = ViewConfiguration.getScrollFriction()
            val logarithm = ln(abs(speed) * .35 / (friction * physical))
            val distance = friction * physical * exp(decelerationRate / (decelerationRate - 1) * logarithm)
            val rounded = if (distance > pitch) distance - distance % pitch else distance % pitch
            val travel = if (speed < 0) -(rounded + lastDelta + (offset - lastDelta) % pitch)
                else rounded + lastDelta - (offset + lastDelta) % pitch
            val duration = (exp(logarithm / (decelerationRate - 1)) * 1000).toInt()
            animate(fling, travel.toInt(), (duration * 1.5f).toInt())
        } else settle()
    }
    fun tap(direction: Int) {
        if (disposed) return
        if (display(direction) == null) { settle(); return }
        stop()
        // 连续点按先对齐当前 offset，再按原厂 300ms 曲线跨一项。
        val nearest = if (abs(offset) > pitch / 2) if (offset > 0) -1 else 1 else 0
        if (nearest != 0) display(nearest)?.let(::change)
        offset = 0f
        animate(fling, (-direction * pitch).toInt(), 300)
    }
    fun settle() {
        if (disposed) return
        var target = -offset.toInt()
        if (abs(target) > pitch / 2) target += (if (target > 0) -pitch else pitch).toInt()
        if (target == 0) { offset = 0f; busy = false; return }
        animate(adjust, target, 300)
    }
    private fun animate(scroller: Scroller, distance: Int, duration: Int) {
        if (disposed) return
        job?.cancel()
        scroller.startScroll(0, 0, 0, distance, duration.coerceAtLeast(1))
        busy = true
        job = scope.launch {
            var previous = 0
            do {
                withFrameNanos { }
                scroller.computeScrollOffset()
                val current = scroller.currY
                scroll(current - previous); previous = current
            } while (!scroller.isFinished)
            job = null
            if (scroller === fling) settle() else { offset = 0f; busy = false }
        }
    }
}
