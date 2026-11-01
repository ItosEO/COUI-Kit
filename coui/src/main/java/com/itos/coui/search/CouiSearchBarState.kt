package com.itos.coui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.PI
import kotlin.math.pow

/** 查询和展开目标由宿主控制；宽度、取消胶囊和页面进入各自保持原厂弹簧。 */
@Stable
class CouiSearchBarState internal constructor(expanded: Boolean) {
    var expanded by mutableStateOf(expanded)
        private set
    internal var widthProgress by mutableFloatStateOf(if (expanded) 1f else 0f)
    internal var cancelProgress by mutableFloatStateOf(if (expanded) 1f else 0f)
    internal var pageProgress by mutableFloatStateOf(if (expanded) 1f else 0f)
    internal var resultProgress by mutableFloatStateOf(if (expanded) 1f else 0f)
    internal var running by mutableStateOf(false)
    val active: Boolean get() = expanded || running || widthProgress > 0f
    /** 原首页在进入时使用立方淡出，退出使用线性淡入。 */
    val contentAlpha: Float get() = (1f - pageProgress).coerceIn(0f, 1f).let { if (expanded) it.pow(3) else it }
    val resultsAlpha: Float get() = resultProgress.coerceIn(0f, 1f)
    private val width = animation(.5f, .25f, { widthProgress = it })
    private val cancel = animation(.5f, .25f, { cancelProgress = it })
    private val page = animation(.35f, 0f, { pageProgress = it })
    private val results = animation(.35f, 0f, { resultProgress = it })

    internal fun updateTarget(target: Boolean) {
        if (expanded == target) return
        expanded = target
        running = true
        val response = if (target) .35f else .1f
        results.spring.stiffness = ((2 * PI / response).let { it * it }).toFloat()
        val end = if (target) 1000f else 0f
        // 与原厂运行中反向一致，只修改目标，保留当前位置和速度。
        for ((spring, value) in listOf(width to widthProgress, cancel to cancelProgress, page to pageProgress, results to resultProgress)) {
            if (!spring.isRunning) spring.setStartValue(value * 1000f)
            spring.animateToFinalPosition(end)
        }
    }

    internal fun dispose() { width.cancel(); cancel.cancel(); page.cancel(); results.cancel() }

    private fun animation(response: Float, bounce: Float, update: (Float) -> Unit): SpringAnimation =
        SpringAnimation(FloatValueHolder()).apply {
            spring = SpringForce().apply {
                dampingRatio = 1f - bounce
                stiffness = ((2 * PI / response) * (2 * PI / response)).toFloat()
            }
            setMinimumVisibleChange(.001f)
            addUpdateListener { _, value, _ -> update(value / 1000f) }
            addEndListener { _, cancelled, _, _ ->
                if (!cancelled) running = width.isRunning || cancel.isRunning || page.isRunning || results.isRunning
            }
        }
}

@Composable
fun rememberCouiSearchBarState(expanded: Boolean): CouiSearchBarState {
    val state = remember { CouiSearchBarState(expanded) }
    SideEffect { state.updateTarget(expanded) }
    DisposableEffect(state) { onDispose { state.dispose() } }
    return state
}
