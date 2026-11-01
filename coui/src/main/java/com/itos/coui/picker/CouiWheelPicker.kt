package com.itos.coui.picker

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itos.coui.theme.CouiTheme
import com.itos.coui.input.isCouiTouchCancelled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** 滚轮普通项和选中项配色；禁用透明度由组件只应用一次。 */
@androidx.compose.runtime.Immutable
data class CouiWheelColors(val normal: Color, val focus: Color)

object CouiPickerDefaults {
    @Composable
    fun wheelColors(dark: Boolean = CouiTheme.isDark): CouiWheelColors {
        val theme = CouiTheme.colorSchemeFor(dark)
        return CouiWheelColors(theme.onSurface.copy(alpha = if (dark) .30f else .26f), theme.onSurface)
    }

    val height = 135.dp
    val columnWidth = 62.dp
    val padding = 12.dp
    val countdownHeight = 228.dp
    val countdownMargin = 30.dp
    val separatorWidth = 30.dp
    val dateYearWidth = 76.dp
    val dateMonthWidth = 96.dp
}

/**
 * 可装配数值滚轮；受控 value，每次跨项提交，CANCEL 只吸附当前项、不产生额外点击。
 * textSize 为 dp；与原厂 NumberPicker 一样不重复应用系统 SP 放大。
 */
@Composable
fun CouiWheelPicker(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    label: String,
    dark: Boolean = CouiTheme.isDark,
    enabled: Boolean = true,
    wrap: Boolean = true,
    height: Dp = CouiPickerDefaults.height,
    textSize: Dp = 16.dp,
    focusTextSize: Dp = 20.dp,
    diffusion: Dp = 0.dp,
    verticalFading: Boolean = false,
    clockTypeface: Boolean = false,
    repeatIntervalMillis: Long = 300,
    formatter: (Int) -> String = Int::toString,
    colors: CouiWheelColors = CouiPickerDefaults.wheelColors(dark),
) {
    require(!valueRange.isEmpty() && value in valueRange)
    require(textSize.value > 0 && focusTextSize.value > 0 && height.value > 0)
    require(repeatIntervalMillis > 0)
    require(diffusion.value >= 0f)
    val density = LocalDensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val callback by rememberUpdatedState(onValueChange)
    val normal = with(density) { textSize.roundToPx().coerceAtLeast(1) }
    val focus = with(density) { focusTextSize.roundToPx().coerceAtLeast(1) }
    val h = with(density) { height.roundToPx().coerceAtLeast(1) }
    val spread = with(density) { diffusion.toPx().toInt().toFloat() }
    val row = normal + (h - 3 * normal).coerceAtLeast(0) / 3
    val motion = remember(context, valueRange, wrap, row, spread) {
        CouiWheelMotion(context, scope, value, valueRange, wrap && valueRange.count() > 3, row.toFloat(), spread)
    }
    SideEffect {
        motion.onChange = { next ->
            callback(next)
            if (!view.performHapticFeedback(308) && !view.performHapticFeedback(302))
                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
        }
        motion.sync(value)
        if (!enabled) motion.reset(value)
    }
    LaunchedEffect(value, motion.value, motion.busy) { withFrameNanos { }; motion.reconcile(value) }
    DisposableEffect(motion) { onDispose { motion.dispose() } }
    val paint = remember(normal, clockTypeface) { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.textSize = normal.toFloat(); textAlign = Paint.Align.CENTER
        typeface = if (clockTypeface) Typeface.create(Typeface.create("sys-sans-en", Typeface.NORMAL), 500, false)
            else Typeface.create("sans-serif-medium", Typeface.NORMAL)
    } }
    val normalColor = colors.normal
    val focusColor = colors.focus
    val baselineMetrics = remember(normal) { Paint().apply { this.textSize = normal.toFloat() }.fontMetrics }
    val mask = if (verticalFading) Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val edge = (((size.height.toInt() - normal) / 2) * .9f / size.height).coerceIn(0f, .5f)
            drawRect(Brush.verticalGradient(0f to Color.Transparent, edge to Color.White,
                (1f - edge) to Color.White, 1f to Color.Transparent), blendMode = BlendMode.DstIn)
        } else Modifier
    Canvas(modifier.height(height).clipToBounds().then(mask)
        .progressSemantics(motion.value.toFloat(), valueRange.first.toFloat()..valueRange.last.toFloat(), (valueRange.count() - 2).coerceAtLeast(0))
        .semantics {
            contentDescription = label; stateDescription = formatter(motion.value)
            if (!enabled) disabled()
            setProgress { next ->
                if (!enabled || !next.isFinite()) false else {
                    val target = next.roundToInt().coerceIn(valueRange)
                    motion.stop(); callback(target); true
                }
            }
        }.onKeyEvent { event ->
            if (!enabled || event.type != KeyEventType.KeyDown) false
            else when (event.key) {
                Key.DirectionUp -> { motion.tap(-1); true }
                Key.DirectionDown -> { motion.tap(1); true }
                else -> false
            }
        }.focusable(enabled).pointerInput(motion, enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume(); motion.begin()
                val tracker = VelocityTracker().apply { addPosition(down.uptimeMillis, down.position) }
                var dragging = false
                var finished = false
                var longPressed = false
                val direction = if (down.position.y < size.height / 3f) -1 else if (down.position.y > size.height * 2 / 3f) 1 else 0
                val repeat = scope.launch {
                    if (direction != 0) {
                        delay(viewConfiguration.longPressTimeoutMillis)
                        longPressed = true
                        while (true) { motion.tap(direction); delay(repeatIntervalMillis) }
                    }
                }
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (event.isCouiTouchCancelled() || (pointer.isConsumed && pointer.pressed)) break
                        tracker.addPosition(pointer.uptimeMillis, pointer.position)
                        if (!dragging && abs(pointer.position.y - down.position.y) > viewConfiguration.touchSlop) {
                            dragging = true; repeat.cancel(); motion.begin()
                        } else if (dragging && pointer.pressed) motion.drag(pointer.position.y - pointer.previousPosition.y)
                        pointer.consume()
                        if (!pointer.pressed) {
                            repeat.cancel()
                            if (dragging) motion.end(tracker.calculateVelocity().y, false)
                            else if (!longPressed && direction != 0) motion.tap(direction) else motion.settle()
                            finished = true; break
                        }
                    }
                } finally { repeat.cancel(); if (!finished) motion.end(0f, true) }
            }
        }) {
        drawIntoCanvas { output ->
            val canvas = output.nativeCanvas
            val center = size.height / 2
            val band = 24.dp.toPx() + spread
            paint.textSize = normal.toFloat()
            val baseline = (3 * row - baselineMetrics.top - baselineMetrics.bottom) / 2
            for (relative in -2..2) {
                val number = motion.display(relative) ?: continue
                val text = formatter(number)
                val y = (baseline + motion.offset + relative * (row + spread)).toInt().toFloat()
                val alpha = if (enabled) 1f else .5f
                canvas.save(); canvas.clipOutRect(0f, center - band, size.width, center + band)
                paint.textSize = normal.toFloat(); paint.color = normalColor.copy(alpha = normalColor.alpha * alpha).toArgb()
                canvas.drawText(text, size.width / 2, y, paint); canvas.restore()
                canvas.save(); canvas.clipRect(0f, center - band, size.width, center + band)
                paint.textSize = focus.toFloat(); paint.color = focusColor.copy(alpha = focusColor.alpha * alpha).toArgb()
                canvas.drawText(text, size.width / 2, y, paint); canvas.restore()
            }
        }
    }
}
