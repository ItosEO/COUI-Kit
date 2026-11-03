package com.itos.coui.demo

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.asin

@Composable
internal fun DemoRecordDeleteIcon(modifier: Modifier = Modifier, confirmation: Boolean, committed: Boolean, color: Color) {
    Box(modifier) {
        if (confirmation) Icon(Icons.Default.Check, contentDescription = null,
            modifier = Modifier.fillMaxSize(), tint = color)
        else RecordTrashCanIcon(Modifier.fillMaxSize(), open = committed, color = color)
    }
}

@Composable
private fun RecordTrashCanIcon(modifier: Modifier = Modifier, open: Boolean, color: Color) {
    val lidRotation by animateFloatAsState(
        targetValue = if (open) -34f else 0f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "record_trash_lid_rotation",
    )
    Canvas(modifier) {
        val strokeWidth = size.minDimension * 0.095f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val bodyLeft = size.width * 0.26f
        val bodyTop = size.height * 0.38f
        val bodyWidth = size.width * 0.48f
        val bodyHeight = size.height * 0.42f
        val lidY = size.height * 0.28f
        val lidStart = Offset(size.width * 0.22f, lidY)
        val lidWidth = size.width * 0.56f
        drawRoundRect(color = color, topLeft = Offset(bodyLeft, bodyTop), size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(size.minDimension * 0.06f), style = stroke)
        drawLine(color = color, start = Offset(size.width * 0.41f, bodyTop + size.height * 0.08f),
            end = Offset(size.width * 0.41f, bodyTop + bodyHeight - size.height * 0.08f),
            strokeWidth = strokeWidth, cap = StrokeCap.Round)
        drawLine(color = color, start = Offset(size.width * 0.59f, bodyTop + size.height * 0.08f),
            end = Offset(size.width * 0.59f, bodyTop + bodyHeight - size.height * 0.08f),
            strokeWidth = strokeWidth, cap = StrokeCap.Round)
        rotate(degrees = lidRotation, pivot = lidStart) {
            drawLine(color = color, start = lidStart, end = Offset(lidStart.x + lidWidth, lidStart.y),
                strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = color, start = Offset(size.width * 0.43f, lidY - size.height * 0.08f),
                end = Offset(size.width * 0.57f, lidY - size.height * 0.08f),
                strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
    }
}

@Composable
internal fun DemoRecordLastViewedIndicator(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.record_last_viewed)
    val color = MaterialTheme.colorScheme.primary
    Box(modifier.width(4.dp).height(14.dp).drawWithCache {
        val strokeWidth = 3.dp.toPx()
        val halfSpan = ((size.height - strokeWidth) * 0.5f).coerceAtLeast(0f)
        // 短弧按记录卡片圆角生成，方向朝向卡片。
        val radius = (DemoShapes.record.topStart.toPx(size, this) * 1.7f).coerceAtLeast(halfSpan)
        val halfAngle = asin((halfSpan / radius).coerceIn(0f, 1f)) * 180f / Math.PI.toFloat()
        onDrawBehind {
            scale(scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f, scaleY = 1f) {
                drawArc(color = color, startAngle = 180f - halfAngle, sweepAngle = halfAngle * 2f,
                    useCenter = false, topLeft = Offset(strokeWidth * 0.5f, size.height * 0.5f - radius),
                    size = Size(radius * 2f, radius * 2f), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
        }
    }.semantics { contentDescription = description })
}
