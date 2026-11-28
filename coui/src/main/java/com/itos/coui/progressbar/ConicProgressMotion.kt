package com.itos.coui.progressbar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.itos.coui.motion.CouiSpring
import kotlin.math.abs
import kotlin.math.min

internal class ConicProgressMotion {
    private val shape = CouiSpring(1f, minimumChange = .0001f)
    private val opacity = CouiSpring(1f, minimumChange = .0001f)
    private val highlight = CouiSpring(1f, minimumChange = .0001f)
    private val press = CouiSpring(0f, minimumChange = .0001f)
    private val seek = CouiSpring(0f, minimumChange = 1f / CouiConicProgressDefaults.SeekUnits)
    private val tap = CouiSpring(0f)
    private var from = Mode.Off.profile
    private var mode = Mode.Off
    private var highlightVisible = false
    private var highlightFrom = 0f
    private var highlightTarget = 0f
    private var pressed = false
    private var tapping by mutableStateOf(false)

    val expansion: Float get() = press.value.coerceIn(0f, 1f)
    val dragProgress: Float get() = seek.value.coerceIn(0f, 1f)
    val tapProgress: Float? get() = if (tapping) tap.value.coerceIn(0f, 1f) else null
    val highlightAlpha: Float get() = lerp(highlightFrom, highlightTarget, highlight.value)
    val drawsCone: Boolean get() = mode != Mode.Off || shape.isRunning || opacity.isRunning
    val height: Float get() = lerp(from.height, mode.profile.height, shape.value)
    val falloff: Float get() = lerp(from.falloff, mode.profile.falloff, shape.value)
    val slope: Float get() = lerp(from.slope, mode.profile.slope, shape.value)
    val baseWidth: Float get() = lerp(from.baseWidth, mode.profile.baseWidth, shape.value)
    val alpha1: Float get() = lerp(from.alpha1, mode.profile.alpha1, opacity.value)
    val alpha2: Float get() = lerp(from.alpha2, mode.profile.alpha2, opacity.value)

    fun update(playing: Boolean, dragging: Boolean, progress: Float, animate: Boolean) {
        val next = when {
            playing && dragging -> Mode.Dragging
            playing -> Mode.Playing
            else -> Mode.Off
        }
        if (next != mode) {
            from = Profile(height, falloff, slope, baseWidth, alpha1, alpha2)
            mode = next
            shape.restart(if (next == Mode.Playing) .9f else .4f, animate)
            opacity.restart(if (next == Mode.Playing) .3f else .4f, animate)
        }
        val showHighlight = progress > 0f && (playing || dragging)
        if (progress > 0f && showHighlight != highlightVisible) {
            highlightFrom = highlightAlpha
            highlightTarget = if (showHighlight) 1f else 0f
            highlightVisible = showHighlight
            highlight.restart(.5f, animate)
        }
        val pressing = dragging || tapping
        if (pressed != pressing) {
            pressed = pressing
            val target = if (pressing) 1f else 0f
            if (animate) press.animateTo(target, response = .3f) else press.snapTo(target)
        }
        if (!animate) {
            if (shape.isRunning) shape.snapTo(1f)
            if (opacity.isRunning) opacity.snapTo(1f)
            if (highlight.isRunning) highlight.snapTo(1f)
            if (press.isRunning) press.snapTo(if (pressing) 1f else 0f)
        }
    }

    fun dispose() {
        stopDrag()
        stopTap()
        shape.cancel()
        opacity.cancel()
        highlight.cancel()
        press.cancel()
    }

    fun moveDrag(start: Float?, target: Float, animate: Boolean) {
        if (start != null) seek.snapTo(start)
        val catchUp = start != null && abs(target - start) > 1f / CouiConicProgressDefaults.SeekUnits
        if (animate && (catchUp || seek.isRunning)) seek.animateTo(target, response = .15f)
        else seek.snapTo(target)
    }

    fun stopDrag() = seek.cancelComplete()

    fun moveTap(current: Float, target: Float, width: Int, animate: Boolean, onEnd: (Float) -> Unit) {
        val start = tapProgress ?: current
        if (!animate || start == target) {
            stopTap()
            onEnd(target)
            return
        }
        tap.snapTo(start)
        tapping = true
        // 原厂以像素求解位置弹簧，最小可见变化为 1px。
        tap.animateTo(target, response = .3f, minimumChange = 1f / width) {
            tapping = false
            onEnd(target)
        }
    }

    fun stopTap() {
        if (!tapping) return
        tap.cancelComplete()
        tapping = false
    }

    private fun CouiSpring.restart(response: Float, animate: Boolean) {
        snapTo(if (animate) 0f else 1f)
        if (animate) animateTo(1f, response)
    }

    private enum class Mode(val profile: Profile) {
        Off(Profile(.1f, 0f, 0f, 0f, 0f, 0f)),
        Playing(Profile(.6f, .6f, .2f, .008f, 1f, .6f)),
        Dragging(Profile(.2f, 2f, 0f, .014f, .3f, .3f)),
    }

    private data class Profile(
        val height: Float,
        val falloff: Float,
        val slope: Float,
        val baseWidth: Float,
        val alpha1: Float,
        val alpha2: Float,
    )
}

internal fun conicHighlightFraction(progress: Float, playing: Boolean, dragging: Boolean): Float =
    when {
        playing -> min(progress, CouiConicProgressDefaults.PlayingHighlight)
        dragging -> min(progress, CouiConicProgressDefaults.DraggingHighlight)
        else -> 0f
    }

private fun lerp(start: Float, end: Float, fraction: Float): Float =
    start + (end - start) * fraction.coerceIn(0f, 1f)
