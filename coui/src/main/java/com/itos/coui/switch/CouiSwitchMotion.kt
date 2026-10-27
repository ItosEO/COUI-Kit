package com.itos.coui.switch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.PI
import kotlin.math.abs

/** COUISwitch 的切换、弹簧跟随与二锚点吸附；宿主持有 checked。 */
internal class CouiSwitchMotion(initial: Boolean, val travel: Float, private val rtl: Boolean) {
    private var observed = initial
    private var phase = Phase.Idle
    private var animation: SpringAnimation? = null
    private var attachYAnimation: SpringAnimation? = null
    private var completedAttachment: Boolean? = null
    private var start = 0f
    private var down = 0f
    private var downY = 0f
    private var deltaSatisfied = false
    private var velocity = 0f
    private var physical = endpoint(initial)
    var translation by mutableFloatStateOf(physical.toInt().toFloat())
        private set
    var stretch by mutableFloatStateOf(1f)
        private set
    var fraction by mutableFloatStateOf(if (initial) 1f else 0f)
        private set
    var settled by mutableIntStateOf(0)
        private set
    var onCommit: (Boolean) -> Unit = {}

    private fun endpoint(checked: Boolean) = if (checked != rtl) travel else 0f
    private fun logical(value: Float) = if (rtl) 1f - value / travel else value / travel

    fun update(checked: Boolean, enabled: Boolean) {
        if (checked != observed) {
            observed = checked
            if (completedAttachment == checked) {
                // 原厂吸附提交 setChecked(target, false)，不再启动普通切换弹簧。
                completedAttachment = null
                snap(checked)
            } else animate(endpoint(checked), Phase.Toggle)
        } else if (!enabled && (phase == Phase.Drag || phase == Phase.Attach)) {
            // 禁用输入只终止手势；startLoading / setEnabled 不取消原厂正在执行的 Toggle。
            cancel()
        }
    }

    fun begin(x: Float, y: Float): Boolean {
        if (phase != Phase.Idle) return false
        completedAttachment = null
        start = translation
        down = x
        downY = y
        deltaSatisfied = false
        phase = Phase.Drag
        physical = start
        stretch = 1f
        return true
    }

    fun drag(x: Float, y: Float) {
        if (phase != Phase.Drag) return
        deltaSatisfied = deltaSatisfied || abs(x - down) >= CouiSwitchDefaults.transformDistancePx ||
            abs(y - downY) >= CouiSwitchDefaults.transformDistancePx
        // 原厂 overDistance 为零，限制目标而不截断弹簧当前位置。
        val target = (start + x - down).coerceIn(0f, travel)
        // COUISpringDragHelper.dragTo 的两级启动阈值：.01px / 1px。
        val distance = abs(target - physical)
        if (distance >= .01f && (animation != null || distance >= 1f)) animate(target, Phase.Drag)
    }

    fun attach(inputVelocity: Float, inputVelocityY: Float) {
        if (phase != Phase.Drag) return
        val v = inputVelocity.toInt().toFloat()
        val vy = inputVelocityY.toInt().toFloat()
        // getAttachPosition 先找最近锚点；严格小于比较使等距时保留第一个锚点。
        var target = if (abs(physical) <= abs(travel - physical)) 0f else travel
        if (abs(v) >= CouiSwitchDefaults.transformVelocityPx && deltaSatisfied) target = if (v > 0f) travel else 0f
        else if (abs(vy) >= CouiSwitchDefaults.transformVelocityPx && deltaSatisfied) target = if (vy > 0f) travel else 0f
        dispose()
        phase = Phase.Attach
        if (abs(target - physical) < 1f) {
            finishAttachment()
            return
        }
        // 选锚点使用整数速度；吸附的初速度仍使用原始浮点速度。
        animate(target, Phase.Attach, inputVelocity.coerceIn(-CouiSwitchDefaults.transformVelocityPx, CouiSwitchDefaults.transformVelocityPx))
        attachYAnimation = SpringAnimation(FloatValueHolder()).apply {
            spring = switchSpring(0f, CouiSwitchDefaults.attachResponse, CouiSwitchDefaults.attachBounce)
            setMinimumVisibleChange(1f)
            setStartValue(0f)
            setStartVelocity(inputVelocityY.coerceIn(-CouiSwitchDefaults.transformVelocityPx, CouiSwitchDefaults.transformVelocityPx))
            addEndListener { ended, canceled, _, _ ->
                if (attachYAnimation === ended && !canceled) {
                    attachYAnimation = null
                    finishAttachment()
                }
            }
            animateToFinalPosition(0f)
        }
    }

    fun reconcile(checked: Boolean) {
        if (completedAttachment == checked) { completedAttachment = null; return }
        completedAttachment = null
        if (phase == Phase.Idle && abs(translation - endpoint(checked)) >= .1f) animate(endpoint(checked), Phase.Toggle)
    }

    fun cancel() {
        dispose()
        completedAttachment = null
        snap(observed)
    }

    private fun snap(checked: Boolean) {
        phase = Phase.Idle
        physical = endpoint(checked)
        translation = physical.toInt().toFloat()
        fraction = if (checked) 1f else 0f
        stretch = 1f
    }

    fun dispose() {
        val old = animation
        animation = null
        old?.cancel()
        val oldY = attachYAnimation
        attachYAnimation = null
        oldY?.cancel()
        velocity = 0f
    }

    private fun finishAttachment() {
        if (phase != Phase.Attach || animation != null || attachYAnimation != null) return
        phase = Phase.Idle
        stretch = 1f
        val checked = (translation >= travel.toInt() / 2) != rtl
        completedAttachment = checked
        onCommit(checked)
        settled++
    }

    private fun animate(target: Float, next: Phase, initialVelocity: Float? = null) {
        val samePhase = phase == next && animation != null
        if (!samePhase) {
            dispose()
            if (next == Phase.Toggle) physical = translation
            phase = next
        }
        val response = when (next) {
            Phase.Attach -> CouiSwitchDefaults.attachResponse
            Phase.Toggle -> CouiSwitchDefaults.toggleResponse
            else -> CouiSwitchDefaults.dragResponse
        }
        val bounce = when (next) {
            Phase.Toggle -> CouiSwitchDefaults.toggleBounce
            Phase.Attach -> CouiSwitchDefaults.attachBounce
            else -> CouiSwitchDefaults.dragBounce
        }
        val active = animation ?: SpringAnimation(FloatValueHolder()).apply {
            spring = switchSpring(target, response, bounce)
            setMinimumVisibleChange(if (next == Phase.Toggle) .1f else 1f)
            addUpdateListener { _, value, speed ->
                physical = value
                velocity = speed
                translation = value.toInt().toFloat()
                val p = logical(if (next == Phase.Toggle) value else translation)
                fraction = p.coerceIn(0f, 1f)
                stretch = if (next == Phase.Toggle) {
                    if (p < .34725848f) 1f + p / .34725848f * .29999995f
                    else 1.3f - (p - .34725848f) / .65274155f * .29999995f
                } else 1f
            }
            addEndListener { ended, canceled, _, _ ->
                if (animation === ended && !canceled) {
                    animation = null
                    if (next == Phase.Attach) finishAttachment() else {
                        phase = if (next == Phase.Drag) Phase.Drag else Phase.Idle
                        stretch = 1f
                        if (next != Phase.Drag) settled++
                    }
                }
            }
        }.also { animation = it }
        if (!active.isRunning) active.setStartValue(physical).setStartVelocity(initialVelocity ?: velocity)
        active.animateToFinalPosition(target)
    }

    private enum class Phase { Idle, Toggle, Drag, Attach }
}

private fun switchSpring(target: Float, response: Float, bounce: Float) = SpringForce(target).apply {
    stiffness = ((2.0 * PI / response) * (2.0 * PI / response)).toFloat()
    dampingRatio = 1f - bounce
}
