package com.itos.coui.sheet

import android.graphics.Paint
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.effect.CouiContour
import com.itos.coui.effect.CouiContourStyle
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.dialog.CouiDialogColors
import com.itos.coui.dialog.CouiDialogMaterial
import com.itos.coui.effect.rememberCouiMaterialEffect
import com.itos.coui.menu.rememberCouiKawaseBlur
import com.itos.coui.shape.CouiShape

@Composable
internal fun CouiSheetSurface(
    modifier: Modifier,
    material: CouiDialogMaterial?,
    backdrop: CouiBackdrop?,
    colors: CouiDialogColors,
    dark: Boolean,
    surfaceBackdrop: CouiBackdrop,
    sampleCoordinates: () -> LayoutCoordinates?,
    sampleTranslation: () -> Offset,
    content: @Composable BoxScope.() -> Unit,
) {
    // 表面在组合期间保留外层窗口源，退场结束后与面板一并释放。
    if (material != null) checkNotNull(backdrop).RetainWhileComposed()
    val blur = if (material != null) rememberCouiKawaseBlur(checkNotNull(backdrop), colors.surface, material.blurRadius) else null
    val materialEffect = if (material != null) rememberCouiMaterialEffect(material.mix, material.blend, material.blendMode) else null
    val surfaceEffect = remember(materialEffect) { materialEffect?.asComposeRenderEffect() }
    val layer = rememberGraphicsLayer()
    val recordingDensity = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val contour = LocalCouiContourGlowEnabled.current
    val edge = remember(material != null, contour) {
        if (material != null && contour && Build.VERSION.SDK_INT >= 33) CouiContour(style = CouiContourStyle.Panel) else null
    }
    val edgePaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    Layout(modifier = modifier.drawWithCache {
        // 原厂先把底部 padding 纳入测量，再让这部分伸出窗口；材质与轮廓使用同一完整尺寸。
        val fullSize = size.copy(height = size.height + CouiSheetDefaults.bottomPadding.roundToPx())
        edgePaint.shader = if (Build.VERSION.SDK_INT >= 33) {
            edge?.shader(fullSize, density, CouiSheetDefaults.cornerRadius.toPx(), CouiSheetDefaults.edgeWeight,
                Color.Transparent, CouiSheetDefaults.innerGlowAlpha(dark), CouiSheetDefaults.lineAlpha(dark), 1f,
                CouiSheetDefaults.lineWidth(dark).toPx(), bottomRadiusPx = 0f)
        } else null
        onDrawWithContent {
            drawContent()
            // NEW_G2 分支先恢复内容的路径裁剪，再由 Drawable 自身处理轮廓覆盖率。
            if (edge != null) drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRect(0f, 0f, fullSize.width, fullSize.height, edgePaint)
            }
        }
    }.clip(SheetShape), content = {
        // 采样位移每帧使背景失效；独立绘制节点保留清晰内容和轮廓的显示列表。
        Box(Modifier.graphicsLayer().couiBackdrop(surfaceBackdrop).drawBehind {
            val target = sampleCoordinates()
            if (Build.VERSION.SDK_INT >= 33 && blur != null && materialEffect != null &&
                target != null && target.isAttached && size.width > 0f && size.height > 0f) {
                layer.renderEffect = surfaceEffect
                layer.record(recordingDensity, layoutDirection,
                    IntSize(size.width.toInt().coerceAtLeast(1), size.height.toInt().coerceAtLeast(1))) {
                    blur.draw(this, target, 1f, sampleTranslation())
                }
                drawLayer(layer)
            } else drawRect(colors.surface)
        })
        Box(content = content)
    }) { measurables, constraints ->
        val contentPlaceable = measurables[1].measure(constraints)
        val fullHeight = contentPlaceable.height + CouiSheetDefaults.bottomPadding.roundToPx()
        val backgroundPlaceable = measurables[0].measure(Constraints.fixed(contentPlaceable.width, fullHeight))
        // 对父级与运动只报告可见高度，不能把原厂的额外测量 padding 再计进入退场距离。
        layout(contentPlaceable.width, contentPlaceable.height) {
            backgroundPlaceable.place(0, 0)
            contentPlaceable.place(0, 0)
        }
    }
}

private val SheetShape = object : Shape {
    private val rounded = CouiShape(CouiSheetDefaults.cornerRadius, CouiSheetDefaults.edgeWeight)
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val fullSize = size.copy(height = size.height + with(density) { CouiSheetDefaults.bottomPadding.roundToPx() })
        val roundedPath = Path().apply { addOutline(rounded.createOutline(fullSize, layoutDirection, density)) }
        val bottom = Path().apply { addRect(Rect(0f, fullSize.height / 2f, fullSize.width, fullSize.height)) }
        // 下半部使用几何并集，保持两个下角为直角。
        return Outline.Generic(Path.combine(PathOperation.Union, roundedPath, bottom))
    }
}
