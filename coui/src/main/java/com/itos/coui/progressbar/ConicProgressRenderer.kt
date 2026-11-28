package com.itos.coui.progressbar

import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class ConicProgressRenderer {
    private val trackPath = Path()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlight = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightMatrix = Matrix()
    private var gradientWidth = 0f
    private var lastHighlightWidth = 0f
    private val cone = RuntimeShader(ConicProgressShader)
    private val light = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = cone
        blendMode = BlendMode.SCREEN
    }

    fun draw(
        canvas: Canvas,
        width: Float,
        centerY: Float,
        height: Float,
        progress: Float,
        playing: Boolean,
        dragging: Boolean,
        rtl: Boolean,
        color: Color,
        trackColor: Color,
        motion: ConicProgressMotion,
    ) {
        if (width <= 0f || height <= 0f) return
        val radius = height / 2f
        val top = centerY - radius
        val bottom = centerY + radius
        fill.color = trackColor.toArgb()
        canvas.drawRoundRect(0f, top, width, bottom, radius, radius, fill)

        val currentHighlightWidth = width * conicHighlightFraction(progress, playing, dragging)
        if (currentHighlightWidth > 0f) lastHighlightWidth = currentHighlightWidth
        val head = width * progress
        val saved = canvas.save()
        try {
            if (rtl) {
                canvas.translate(width, 0f)
                canvas.scale(-1f, 1f)
            }
            if (head > 0f) {
                trackPath.rewind()
                trackPath.addRoundRect(0f, top, width, bottom, radius, radius, Path.Direction.CW)
                val clip = canvas.save()
                canvas.clipPath(trackPath)
                val argb = color.toArgb()
                fill.color = argb
                canvas.drawRoundRect(0f, top, head, bottom, radius, radius, fill)
                val alpha = (motion.highlightAlpha * 255f).toInt().coerceIn(0, 255)
                if (alpha > 0 && lastHighlightWidth > 0f) {
                    if (gradientWidth != lastHighlightWidth) {
                        gradientWidth = lastHighlightWidth
                        highlight.shader = LinearGradient(
                            0f, 0f, gradientWidth, 0f, 0x00FFFFFF, -1, Shader.TileMode.CLAMP,
                        )
                    }
                    val left = head - gradientWidth
                    highlightMatrix.setTranslate(left, 0f)
                    highlight.shader?.setLocalMatrix(highlightMatrix)
                    highlight.alpha = alpha
                    canvas.drawRoundRect(left, top, head, bottom, radius, radius, highlight)
                }
                canvas.restoreToCount(clip)
            }
            if (motion.drawsCone && motion.falloff > 0f) {
                drawCone(canvas, width, centerY, progress, rtl, color.toArgb(), motion)
            }
        } finally {
            canvas.restoreToCount(saved)
        }
    }

    private fun drawCone(
        canvas: Canvas,
        width: Float,
        centerY: Float,
        progress: Float,
        rtl: Boolean,
        argb: Int,
        motion: ConicProgressMotion,
    ) {
        val lightHeight = CouiConicProgressDefaults.LightHeight
        val pad = CouiConicProgressDefaults.LightPadding
        // 原厂以物理横坐标调制高度，再在片元中按 RTL 翻转光尾。
        val physicalProgress = if (rtl) 1f - progress else progress
        val threshold = CouiConicProgressDefaults.PlayingHighlight
        val modulation = if (physicalProgress > threshold) 1f else physicalProgress / (threshold + .01f)
        cone.setFloatUniform("origin", width * progress + pad, lightHeight / 2f)
        cone.setFloatUniform(
            "tint", ((argb shr 16) and 255) * .003921f,
            ((argb shr 8) and 255) * .003921f, (argb and 255) * .003921f,
        )
        cone.setFloatUniform(
            "shape", motion.height * modulation, motion.falloff * modulation, motion.slope, motion.baseWidth,
        )
        cone.setFloatUniform("strength", motion.alpha1, motion.alpha2)
        cone.setFloatUniform("spotRadius", CouiConicProgressDefaults.SpotRadius)
        canvas.translate(-pad, centerY - lightHeight / 2f)
        canvas.drawRect(0f, 0f, width + pad * 2f, lightHeight, light)
    }
}

// 两束同色锥光共用空间衰减。保留两路 alpha 合成及 RGB 主导时的预乘约束。
private const val ConicProgressShader = """
    uniform float2 origin;
    uniform float3 tint;
    uniform float4 shape;
    uniform float2 strength;
    uniform float spotRadius;

    half4 main(float2 pixel) {
        float2 offset = pixel - origin;
        if (offset.x >= 0.0 || shape.y <= 0.0) return half4(0.0);
        float trailDistance = -offset.x;
        float vertical = abs(offset.y);
        float coneWidth = shape.w * spotRadius + shape.z * trailDistance;
        float span = spotRadius * shape.x;
        float radial = vertical / (coneWidth + 0.0001);
        float falloff = 1.0 - pow(clamp(trailDistance / (span + 0.0001), 0.0, 1.0), shape.y);
        falloff *= exp(-3.0 * (trailDistance - span) / spotRadius * step(span, trailDistance));
        float intensity = (1.0 - smoothstep(0.0, 2.0 * spotRadius, vertical - coneWidth))
            * exp(-radial * radial) * falloff * exp(-2.0 * trailDistance / spotRadius)
            * smoothstep(0.0, 0.03 * spotRadius, trailDistance);
        float2 alphas = intensity * strength;
        float alpha = alphas.x * (1.0 - alphas.y) + alphas.y;
        float3 single = tint * intensity;
        float dominance = abs(alphas.x - alphas.y) / max(alpha, 0.000001);
        float3 rgb = clamp(mix(min(float3(1.0), single + single), single, dominance), 0.0, 1.0);
        float brightest = max(rgb.r, max(rgb.g, rgb.b));
        return half4(rgb, clamp(max(alpha, brightest), 0.0, 1.0));
    }
"""
