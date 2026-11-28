package com.itos.coui.input

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/** COUIHapticFeedbackConstants；由当前 ROM 消费，不合成另一套马达波形。 */
enum class CouiHapticFeedback(val code: Int) {
    Switch(302),
    ScrollEdge(307),
}

/** 保留 View 和系统触觉设置的门控；不支持该反馈码的 ROM 返回 false。 */
fun View.performCouiHapticFeedback(feedback: CouiHapticFeedback, enabled: Boolean = true): Boolean =
    enabled && isAttachedToWindow && performHapticFeedback(feedback.code)

internal const val CouiSeekBarDragTick = 305
internal const val CouiSeekBarEdgeTick = 306

private val usesCouiSeekBarHaptics = when (Build.MANUFACTURER.lowercase()) {
    "oplus", "oppo", "oneplus", "realme" -> true
    else -> false
}

internal fun View.performCouiSeekBarHaptic(code: Int, section: Boolean = false) {
    // AOSP 的异步分发可能在系统忽略私有反馈码时仍返回 true。
    if (usesCouiSeekBarHaptics) {
        if (performHapticFeedback(code)) return
        if (section && code == 308 && performHapticFeedback(302)) return
    }
    val standard = when {
        code == CouiSeekBarEdgeTick && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            HapticFeedbackConstants.GESTURE_END
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
            if (section) HapticFeedbackConstants.SEGMENT_TICK else HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        else -> HapticFeedbackConstants.CLOCK_TICK
    }
    performHapticFeedback(standard)
}
