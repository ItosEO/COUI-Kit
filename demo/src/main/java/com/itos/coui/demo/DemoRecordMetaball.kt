package com.itos.coui.demo

import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.itos.coui.effect.CouiMetaballBody
import com.itos.coui.effect.CouiMetaballRenderer
import com.itos.coui.swipereveal.CouiSwipeRevealDefaults

/** 背景与遮罩共用几何, 但各自持有可变 Shader。 */
@RequiresApi(33)
@Composable
internal fun DemoRecordMetaballBackground(
    modifier: Modifier = Modifier,
    renderer: CouiMetaballRenderer,
    visibleCardWidth: Dp,
    deleteActionWidth: Dp,
    deleteActionSizePx: Float,
    alignCardEnd: Boolean,
    menuTranslation: Float,
    fusionActive: Boolean,
    deleteActionAlpha: Float,
) {
    val cardColor = MaterialTheme.colorScheme.surfaceBright
    Canvas(modifier = modifier) {
        drawRecordMetaball(
            renderer, size, visibleCardWidth, deleteActionWidth,
            deleteActionSizePx, alignCardEnd, menuTranslation, fusionActive,
            deleteActionAlpha, cardColor, BlendMode.SrcOver,
        )
    }
}

/** 与背景使用同一距离场和退晕带, 避免移动中的笔画露出背景可见边缘。 */
@RequiresApi(33)
@Composable
internal fun demoRecordMetaballContentMask(
    renderer: CouiMetaballRenderer,
    fullWidthPx: Float,
    minHeightPx: Float,
    visibleCardWidth: Dp,
    deleteActionWidth: Dp,
    deleteActionSizePx: Float,
    alignCardEnd: Boolean,
    menuTranslation: Float,
    fusionActive: Boolean,
    deleteActionAlpha: Float,
): Modifier {
    return Modifier
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val fullSize = Size(fullWidthPx, maxOf(minHeightPx, size.height))
            // 内容层宽度已收缩; 将背景坐标转换到内容层, 同时保留 RTL 与右拉对齐。
            val cardAtPhysicalEnd = alignCardEnd != (layoutDirection == LayoutDirection.Rtl)
            val contentLeft = if (cardAtPhysicalEnd) fullWidthPx - size.width else 0f
            translate(left = -contentLeft) {
                drawRecordMetaball(
                    renderer, fullSize, visibleCardWidth, deleteActionWidth,
                    deleteActionSizePx, alignCardEnd, menuTranslation, fusionActive,
                    deleteActionAlpha, Color.White, BlendMode.DstIn,
                )
            }
        }
}

@RequiresApi(33)
private fun DrawScope.drawRecordMetaball(
    renderer: CouiMetaballRenderer,
    regionSize: Size,
    visibleCardWidth: Dp,
    deleteActionWidth: Dp,
    deleteActionSizePx: Float,
    alignCardEnd: Boolean,
    menuTranslation: Float,
    fusionActive: Boolean,
    deleteActionAlpha: Float,
    color: Color,
    blendMode: BlendMode,
) {
    val actionWidth = minOf(deleteActionWidth.toPx(), regionSize.width)
    val actionHeight = minOf(deleteActionSizePx, regionSize.height)
    val cardWidth = minOf(visibleCardWidth.toPx(), regionSize.width)
    val cardLeft = if (alignCardEnd) regionSize.width - cardWidth else 0f
    val actionLeft = regionSize.width - actionWidth + menuTranslation
    val radius = DemoShapes.record.topEnd.toPx(regionSize, this)
    scale(
        scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f,
        scaleY = 1f,
        pivot = Offset(regionSize.width * 0.5f, regionSize.height * 0.5f),
    ) {
        renderer.draw(
            scope = this,
            first = CouiMetaballBody(
                center = Offset(cardLeft + cardWidth * 0.5f, regionSize.height * 0.5f),
                size = Size(cardWidth, regionSize.height),
                cornerRadius = radius,
            ),
            second = CouiMetaballBody(
                center = Offset(actionLeft + actionWidth * 0.5f, regionSize.height * 0.5f),
                size = Size(actionWidth, actionHeight),
                cornerRadius = radius,
            ),
            color = color,
            blendRangePx = CouiSwipeRevealDefaults.metaballBlendRange.roundToPx().toFloat(),
            fusionEnabled = fusionActive,
            secondAlpha = deleteActionAlpha,
            blendMode = blendMode,
            size = regionSize,
        )
    }
}
