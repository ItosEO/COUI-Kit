package com.itos.coui.scroll

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalView
import kotlin.math.abs

/** RecyclerView 的正常惯性与越界共用 SpringOverScroller 轴，不串接平台 spline decay。 */
@Composable
internal fun rememberCouiScrollFlingBehavior(): FlingBehavior {
    val refresh = checkNotNull(LocalView.current.display).refreshRate
    require(refresh.isFinite() && refresh > 0f)
    return remember(refresh) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                val physics = CouiScrollPhysics(1, springBackTensionMultiple = 2.15f, scrollView = false)
                physics.fling(0, initialVelocity.toInt(), 1f / refresh)
                var previous = withFrameNanos { it }
                val began = previous
                var last = 0
                try {
                    while (physics.running) {
                        val frame = withFrameNanos { it }
                        physics.update((frame - previous) / 1_000_000_000f, (frame - began) / 1_000_000f)
                        previous = frame
                        val position = physics.current
                        val delta = (position - last).toFloat()
                        last = position
                        if (abs(delta - scrollBy(delta)) > .5f) return physics.currentVelocity
                    }
                    return 0f
                } finally { physics.stop() }
            }
        }
    }
}
