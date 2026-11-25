package com.itos.coui.material3

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.hct.Hct
import kotlin.math.exp

/**
 * 以 [seed] 的色相和 [tone] 为目标生成增强色。
 *
 * 彩度使用平滑收敛曲线，低彩度增强，高彩度逐步接近上限。种子色的 RGB 三分量相同时保持中性灰。
 * 实际输出的色相、彩度和色调受 sRGB 色域及 8 位量化限制。
 *
 * @param seed 已指定的种子色，转为 sRGB 后参与 HCT 计算，忽略原透明度。
 * @param tone 目标 HCT 色调，取值 0.0 至 100.0，分别对应黑色至白色。
 * @return 不透明的 sRGB 颜色。
 * @throws IllegalArgumentException 种子色未指定，或 [tone] 超出范围。
 */
fun enhanceHctColor(seed: Color, tone: Double): Color = enhancedHctColor(seed, tone)

internal fun enhanceHctColor(seed: Color): Color = enhancedHctColor(seed, tone = null)

private fun enhancedHctColor(seed: Color, tone: Double?): Color {
    require(seed.isSpecified) { "种子色必须已指定" }
    require(tone == null || tone in 0.0..100.0) { "HCT 色调必须在 0 到 100 之间" }
    val hct = Hct.fromInt(seed.toArgb())
    val grayscale = seed.red == seed.green && seed.green == seed.blue
    val chroma = if (grayscale) 0.0 else 80.0 * (1.0 - exp(-3.0 * hct.chroma / 80.0))
    return Color(Hct.from(hct.hue, chroma, tone ?: hct.tone).toInt())
}
