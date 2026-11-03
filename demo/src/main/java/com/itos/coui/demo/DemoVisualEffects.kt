package com.itos.coui.demo

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.drawCouiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.rememberCouiMaterialEffect
import com.itos.coui.layout.Divider
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults
import kotlin.math.ceil

@Composable
private fun BackdropStage(phase: Float, content: @Composable BoxScope.(CouiBackdrop) -> Unit) {
    val backdrop = rememberCouiBackdrop()
    val colors = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth().height(200.dp).clip(DemoShapes.item)) {
        // 独立采样源只包含图案, 其上的效果面板不能录入该源。
        Canvas(Modifier.fillMaxSize().couiBackdrop(backdrop)) {
            drawRect(colors.surfaceBright)
            val palette = listOf(colors.primaryContainer, colors.secondaryContainer, colors.tertiaryContainer)
            repeat(9) { index ->
                drawRect(palette[index % palette.size], Offset((index - 1 + phase) * size.width / 7f, 0f),
                    Size(size.width / 14f, size.height))
            }
            drawCircle(colors.primary, radius = size.height * 0.22f,
                center = Offset(size.width * (0.2f + phase * 0.5f), size.height * 0.45f))
        }
        content(backdrop)
    }
}

@RequiresApi(33)
@Composable
internal fun MaterialSample(dark: Boolean) {
    var radius by rememberSaveable { mutableFloatStateOf(16f) }
    var phase by rememberSaveable { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val material = CouiTopAppBarButtonDefaults.material(dark, capsule = true)
    val optical = rememberCouiMaterialEffect(material.firstColor, material.secondColor)
    DemoSliderPreference(title = stringResource(R.string.blur_radius),
        value = radius, onValueChange = { radius = it }, valueRange = 0f..32f, valueText = radius.toInt().toString())
    Divider()
    DemoSliderPreference(title = stringResource(R.string.backdrop_position),
        value = phase, onValueChange = { phase = it }, valueText = "${(phase * 100).toInt()}%")
    BackdropStage(phase) { backdrop ->
        Box(Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth().height(112.dp)
            .drawCouiBackdrop(backdrop, shape = { DemoShapes.effect }, effects = {
                val blurPx = with(density) { radius.dp.toPx() }
                padding = ceil(blurPx)
                renderEffect = if (blurPx == 0f) optical.asComposeRenderEffect() else RenderEffect.createChainEffect(optical,
                    RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)).asComposeRenderEffect()
            }))
    }
}
