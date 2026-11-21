package com.itos.coui.effect

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/** 材质的第二层混合模式。 */
enum class CouiMaterialBlendMode { Overlay, ColorDodge }

/** 两层材质混色, 只作用于背景输入。组件负责选择颜色和模式预设。 */
@RequiresApi(33)
@Composable
fun rememberCouiMaterialEffect(mix: Color, blend: Color,
    blendMode: CouiMaterialBlendMode = CouiMaterialBlendMode.Overlay): RenderEffect =
    rememberCouiMaterialEffect(mix, blend, blendMode, rgbMix = false)

@RequiresApi(33)
@Composable
internal fun rememberCouiMaterialEffect(
    mix: Color,
    blend: Color,
    blendMode: CouiMaterialBlendMode,
    rgbMix: Boolean,
    slightDither: Boolean = false,
): RenderEffect =
    remember(mix, blend, blendMode, rgbMix, slightDither) {
        val shader = RuntimeShader(
            "const bool RGB_MIX = $rgbMix;\nconst bool SLIGHT_DITHER = $slightDither;\n" + CouiMaterialShader,
        )
        shader.setFloatUniform("uMixColor", mix.red, mix.green, mix.blue, mix.alpha)
        shader.setFloatUniform("uBlendColor", blend.red, blend.green, blend.blue, blend.alpha)
        shader.setFloatUniform("uColorDodge", if (blendMode == CouiMaterialBlendMode.ColorDodge) 1f else 0f)
        RenderEffect.createRuntimeShaderEffect(shader, "content")
    }

internal const val CouiMaterialShaderFunctions = """
    float lum(float3 color) {
        return dot(color, float3(0.3, 0.59, 0.11));
    }

    float3 luminosity(float3 source, float3 destination) {
        float destinationLum = lum(destination);
        float sourceLum = lum(source);
        float3 color = destination + (sourceLum - destinationLum);
        float minColor = min(min(color.x, color.y), color.z);
        float maxColor = max(max(color.x, color.y), color.z);
        if (minColor < 0.0) {
            return sourceLum + ((color - sourceLum) * sourceLum) / (sourceLum - minColor);
        } else if (maxColor > 1.0) {
            return sourceLum + ((color - sourceLum) * (1.0 - sourceLum)) / (maxColor - sourceLum);
        }
        return color;
    }

    float overlay(float source, float destination) {
        return destination < 0.5
            ? 2.0 * source * destination
            : 1.0 - 2.0 * (1.0 - source) * (1.0 - destination);
    }

    float3 overlay(float3 source, float3 destination) {
        return float3(
            overlay(source.x, destination.x),
            overlay(source.y, destination.y),
            overlay(source.z, destination.z)
        );
    }

    half4 couiMaterial(half4 inColor, float4 mixColor, float4 blendColor) {
        float3 luminosityColor = luminosity(mixColor.rgb, inColor.rgb);
        inColor.rgb = mix(inColor.rgb, luminosityColor, mixColor.a);
        float3 overlayColor = overlay(blendColor.rgb, inColor.rgb);
        inColor.rgb = mix(inColor.rgb, overlayColor, blendColor.a);
        return inColor;
    }
"""

private const val CouiMaterialShader = CouiMaterialShaderFunctions + """
    uniform shader content;
    uniform float4 uMixColor;
    uniform float4 uBlendColor;
    uniform float uColorDodge;

    half rgbOverlay(half source, half destination) {
        return destination < 0.5
            ? 2.0 * source * destination
            : 1.0 - 2.0 * (1.0 - source) * (1.0 - destination);
    }

    half4 main(float2 coordinate) {
        half4 color = content.eval(coordinate);
        if (SLIGHT_DITHER) {
            // 面板绘制包含屏外余量；材质处理后仍须保留放大采样的覆盖度。
            if (color.a == 0.0) return half4(0.0);
            color.rgb /= color.a;
        }
        if (RGB_MIX) {
            // 原厂的 RGB 混色与第二层混合均使用 half；ColorDodge 保留未限幅结果。
            half4 mixColor = half4(uMixColor);
            half4 blendColor = half4(uBlendColor);
            color.rgb = mix(color.rgb, mixColor.rgb, mixColor.a);
            half3 blend = uColorDodge < 0.5
                ? half3(rgbOverlay(blendColor.r, color.r), rgbOverlay(blendColor.g, color.g),
                    rgbOverlay(blendColor.b, color.b))
                : color.rgb / (half3(1.0) - blendColor.rgb);
            color.rgb = mix(color.rgb, blend, blendColor.a);
        } else {
            float3 tint = luminosity(uMixColor.rgb, color.rgb);
            color.rgb = mix(color.rgb, tint, uMixColor.a);
            float3 blend = uColorDodge < 0.5
                ? overlay(uBlendColor.rgb, color.rgb)
                : color.rgb / (1.0 - uBlendColor.rgb);
            color.rgb = mix(color.rgb, blend, uBlendColor.a);
        }
        if (SLIGHT_DITHER) {
            // SkiaDitherEffect::createSlightDitherShader：输出像素上共享 RGB 扰动，避免引入彩色噪点。
            float noise = fract(sin(dot(coordinate, float2(12.9898, 78.233))) * 43758.5453);
            color.rgb = (color.rgb + mix(-0.005, 0.005, noise)) * color.a;
        }
        return color;
    }
"""
