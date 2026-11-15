package com.itos.coui.menu

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.itos.coui.backdrop.CouiBackdrop
import kotlin.math.ceil

@Composable
fun rememberCouiMenuBlur(backdrop: CouiBackdrop, background: Color): CouiMenuBlur? =
    rememberCouiKawaseBlur(backdrop, background, CouiMenuBlurRadius)

/** 菜单固定 radius160; 弹窗使用原厂 integer/coui_dialog_blur_radius = 200。 */
private const val CouiMenuBlurRadius = 160
private const val FastKawaseEighthMaxRadius = 250
private const val KawasePassCount = 9

/** 原厂 BackgroundBlurDrawable 的 Fast Kawase 载体, radius 为引擎等级而不是像素。 */
@Composable
internal fun rememberCouiKawaseBlur(
    backdrop: CouiBackdrop,
    background: Color,
    radius: Int,
): CouiMenuBlur? {
    if (Build.VERSION.SDK_INT < 33) return null
    val downsampleCount = if (radius > FastKawaseEighthMaxRadius) 2 else 1
    val layers = List(downsampleCount + KawasePassCount) { rememberGraphicsLayer() }
    return remember(backdrop, background, radius, layers) {
        CouiMenuBlur(backdrop, background, radius, layers)
    }
}

/** 移植 type 2 Fast Kawase，radius 使用原厂引擎等级，每轮保留独立像素网格。 */
@RequiresApi(33)
class CouiMenuBlur internal constructor(
    private val backdrop: CouiBackdrop,
    private val background: Color,
    private val radius: Int,
    private val layers: List<GraphicsLayer>,
) {
    private val sixteenth = radius > FastKawaseEighthMaxRadius
    private val downsample = RuntimeShader(KawaseDownsampleShader)
    private val kawase = RuntimeShader(KawasePassShader)
    private var effectInputSize = IntSize.Zero
    private var recordedBlurSize = IntSize.Zero
    private var recordedDensity: Density? = null
    private var recordedLayoutDirection: LayoutDirection? = null

    init {
        layers.forEach {
            it.compositingStrategy = CompositingStrategy.Offscreen
            it.clip = true
        }
    }

    fun draw(scope: DrawScope, coordinates: LayoutCoordinates) = draw(scope, coordinates, 1f, Offset.Zero)

    /** coordinates 为未动画的锚点；变换与消费者的实际绘制使用同一帧数据。 */
    internal fun draw(
        scope: DrawScope,
        coordinates: LayoutCoordinates,
        sampleScale: Float,
        translation: Offset,
        dimAmount: Float = 0f,
    ) {
        fun drawSource(target: DrawScope) {
            target.withTransform({
                scale(1f / sampleScale, 1f / sampleScale, Offset.Zero)
                translate(-translation.x, -translation.y)
            }) { backdrop.draw(this, coordinates) }
        }
        fun drawInputDim(target: DrawScope) {
            // 宿主录制缺少系统遮罩，在采样层补上它以对齐窗口合成顺序。
            if (dimAmount > 0f) target.drawRect(Color.Black, alpha = dimAmount)
        }
        val menuSize = scope.size
        if (!coordinates.isAttached || menuSize.width <= 0f || menuSize.height <= 0f) return
        if (radius == 0) {
            scope.drawRect(background)
            drawSource(scope)
            drawInputDim(scope)
            return
        }
        val origin = coordinates.localToScreen(Offset.Zero)
        val right = coordinates.localToScreen(Offset(menuSize.width, 0f))
        val bottom = coordinates.localToScreen(Offset(0f, menuSize.height))
        val deviceWidth = (right - origin).getDistance() * sampleScale
        val deviceHeight = (bottom - origin).getDistance() * sampleScale
        if (!deviceWidth.isFinite() || !deviceHeight.isFinite() || deviceWidth <= 0f || deviceHeight <= 0f) {
            scope.drawRect(background)
            drawInputDim(scope)
            return
        }

        // RenderEngine 以设备像素计算 ceil(extent / 16)，再扩展各轮的对齐尺寸。
        val blocksX = ceil(deviceWidth / 16f).toInt().coerceAtLeast(1)
        val blocksY = ceil(deviceHeight / 16f).toInt().coerceAtLeast(1)
        val captureSize = IntSize(blocksX * 8, blocksY * 8)
        val firstSize = if (sixteenth) IntSize(blocksX * 4, blocksY * 4) else captureSize
        val blurSize = IntSize(firstSize.width / 4, firstSize.height / 4)
        updateEffects(captureSize, firstSize, blurSize)

        layers[0].record(scope, scope.layoutDirection, captureSize) {
            // 补底也进入采样链, 不能在材质混色之后再补不透明背景。
            drawRect(background)
            withTransform({
                scale(captureSize.width / menuSize.width, captureSize.height / menuSize.height, Offset.Zero)
            }) {
                drawSource(this)
            }
            drawInputDim(this)
        }
        recordBlurPasses(scope, firstSize, blurSize)
        scope.withTransform({
            // 原厂用输入区域与最终网格尺寸的实际比值恢复尺寸。
            scale(menuSize.width / blurSize.width, menuSize.height / blurSize.height, Offset.Zero)
        }) {
            drawLayer(layers.last())
        }
    }

    private fun recordBlurPasses(scope: DrawScope, firstSize: IntSize, blurSize: IntSize) {
        val density = recordedDensity
        if (recordedBlurSize == blurSize && density != null && density.density == scope.density &&
            density.fontScale == scope.fontScale && recordedLayoutDirection == scope.layoutDirection
        ) return
        // 固定指令只引用前驱层; RenderNode 继续跟踪动态内容, GraphicsLayer 负责恢复被回收的指令。
        // 保存 Density 值, 不让恢复回调持有不断重定向的 DrawScope。
        val recordingDensity = Density(scope.density, scope.fontScale)
        for (index in 1..layers.lastIndex) {
            val previous = layers[index - 1]
            val size = if (sixteenth && index == 1) firstSize else blurSize
            layers[index].record(recordingDensity, scope.layoutDirection, size) {
                drawLayer(previous)
            }
        }
        recordedBlurSize = blurSize
        recordedDensity = recordingDensity
        recordedLayoutDirection = scope.layoutDirection
    }

    private fun updateEffects(captureSize: IntSize, firstSize: IntSize, blurSize: IntSize) {
        if (effectInputSize == captureSize) return
        if (sixteenth) {
            // 1/16 分支先将 ±0.25 的四个取样点栅格化在首轮输出的两倍网格上，再做平均。
            setDownsampleEffect(layers[0], captureSize, 2f)
        }
        val downsampleIndex = if (sixteenth) 1 else 0
        setDownsampleEffect(layers[downsampleIndex], firstSize, 4f)
        // fastKawaseGenerator 的 1/16 分支先减 26，再用 (radius / 9) / 10 计算步长。
        val step = if (sixteenth) (radius - 26f) / 9f / 10f / 16f else (radius - 10f) / (9f * 10f) / 8f
        kawase.setFloatUniform("inputSize", blurSize.width.toFloat(), blurSize.height.toFloat())
        for (pass in 1..KawasePassCount) {
            kawase.setFloatUniform("in_blurOffset", pass * step)
            layers[downsampleIndex + pass].renderEffect =
                RenderEffect.createRuntimeShaderEffect(kawase, "child").asComposeRenderEffect()
        }
        effectInputSize = captureSize
    }

    private fun setDownsampleEffect(layer: GraphicsLayer, inputSize: IntSize, scale: Float) {
        downsample.setFloatUniform("inputSize", inputSize.width.toFloat(), inputSize.height.toFloat())
        downsample.setFloatUniform("inputScale", scale)
        layer.renderEffect = RenderEffect.createRuntimeShaderEffect(downsample, "child").asComposeRenderEffect()
    }
}

// RenderEngine 的四点下采样，矩阵与图像边界由采样网格给定。
private const val KawaseDownsampleShader = """
    uniform shader child;
    uniform float2 inputSize;
    uniform float inputScale;
    half offset = 0.25;

    half4 sampleChild(float2 xy) {
        return child.eval(clamp(xy * inputScale, float2(0.5), inputSize - 0.5));
    }

    half4 main(float2 xy) {
        half4 c = sampleChild(xy + float2(+offset, +offset));
             c += sampleChild(xy + float2(+offset, -offset));
             c += sampleChild(xy + float2(-offset, -offset));
             c += sampleChild(xy + float2(-offset, +offset));
        return half4(c.rgb * 0.25, 1.0);
    }
"""

// RenderEngine 的五点 Kawase 滤波，Clamp 限定在各轮实际图像范围内。
private const val KawasePassShader = """
    uniform shader child;
    uniform float2 inputSize;
    uniform float in_blurOffset;

    half4 sampleChild(float2 xy) {
        return child.eval(clamp(xy, float2(0.5), inputSize - 0.5));
    }

    half4 main(float2 xy) {
        half4 c = sampleChild(xy);
        c += sampleChild(xy + float2(+in_blurOffset, +in_blurOffset));
        c += sampleChild(xy + float2(+in_blurOffset, -in_blurOffset));
        c += sampleChild(xy + float2(-in_blurOffset, -in_blurOffset));
        c += sampleChild(xy + float2(-in_blurOffset, +in_blurOffset));
        return half4(c.rgb * 0.2, 1.0);
    }
"""
