package com.itos.coui.picker

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.pager.PagerState
import kotlin.math.abs
import kotlin.math.floor

/** Calendar 原厂 COUICalendarViewPagerScroller：300ms，COUIMoveEaseInterpolator。 */
internal val CouiCalendarPageEasing = CubicBezierEasing(.3f, 0f, .1f, 1f)

/** Calendar 原厂 ViewPager 的翻页目标判定与 300ms 收尾。 */
internal class CouiCalendarFlingBehavior(
    private val pager: PagerState,
    private val density: Float,
    private val startPage: () -> Int,
) : TargetedFlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float,
        onRemainingDistanceUpdated: (Float) -> Unit): Float {
        val width = pager.layoutInfo.pageSize
        if (width <= 0) return 0f
        val position = pager.currentPage + pager.currentPageOffsetFraction
        val base = floor(position).toInt()
        val offset = position - base
        val moved = abs(position - startPage()) * width
        val fling = moved > 25f * density && abs(initialVelocity) > 400f * density
        val target = (if (fling) {
            if (initialVelocity > 0f) startPage() + 1 else startPage() - 1
        } else {
            base + if (offset > .5f) 1 else 0
        }).coerceIn(0, pager.pageCount - 1)
        val distance = (target - position) * width
        var previous = 0f
        onRemainingDistanceUpdated(distance)
        AnimationState(0f, initialVelocity).animateTo(distance, tween(300, easing = CouiCalendarPageEasing)) {
            val delta = value - previous
            previous = value
            val consumed = scrollBy(delta)
            onRemainingDistanceUpdated(distance - value)
            if (abs(delta - consumed) > .5f) cancelAnimation()
        }
        return 0f
    }
}
