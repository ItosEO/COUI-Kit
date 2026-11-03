package com.itos.coui.demo

import androidx.annotation.RequiresApi

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.width

import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableFloatStateOf

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.drawBehind

import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

import androidx.compose.ui.graphics.nativeCanvas

import androidx.compose.ui.graphics.toArgb

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.unit.dp

import com.itos.coui.effect.CouiShadowDefaults

import com.itos.coui.effect.CouiShadowOutline

import com.itos.coui.effect.CouiShadowRenderer

import com.itos.coui.effect.couiShadow

@RequiresApi(34)
@Composable
internal fun ShadowSamples(dark: Boolean) {
    var elevation by rememberSaveable { mutableFloatStateOf(CouiShadowDefaults.menu(dark).elevation.value) }
    val outline = CouiShadowOutline.SmoothCapsule
    var lowLevel by rememberSaveable { mutableStateOf(false) }
    val rendering = remember { runCatching { CouiShadowRenderer() } }
    val renderer = rendering.getOrNull()
    if (renderer == null) {
        val failure = checkNotNull(rendering.exceptionOrNull())
        EffectText(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName), color = MaterialTheme.colorScheme.error)
        return
    }
    DemoSliderPreference(title = stringResource(R.string.shadow_elevation),
        value = elevation, onValueChange = { elevation = it }, valueRange = 0f..200f, valueText = elevation.toInt().toString())
    DemoSwitchPreference(title = stringResource(R.string.low_level_shadow), checked = lowLevel, onCheckedChange = { lowLevel = it })
    val style = CouiShadowDefaults.menu(dark).copy(elevation = elevation.dp)
    val shape = DemoShapes.couiCapsule
    val shadow = if (lowLevel) Modifier.drawBehind {
        drawIntoCanvas { canvas ->
            renderer.draw(canvas.nativeCanvas, slot = 0, width = size.width, height = size.height,
                sx = 1f, sy = 1f, windowX = 0f, windowY = 0f, alpha = 1f, color = style.color.toArgb(),
                elevation = style.elevation.roundToPx().toFloat(), lightY = style.lightY.roundToPx().toFloat(),
                lightZ = style.lightZ.roundToPx().toFloat(), lightRadius = style.lightRadius.roundToPx().toFloat(),
                outline = outline, radius = size.minDimension * .5f, clipping = true)
        }
    } else Modifier.couiShadow(style, outline, radius = 28.dp)
    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.width(160.dp).height(56.dp).then(shadow).background(if (dark) androidx.compose.ui.graphics.Color(0xFF353535) else androidx.compose.ui.graphics.Color(0xFFE4E6E8), shape),
            contentAlignment = Alignment.Center) {
            EffectText(stringResource(R.string.shadow_subject), color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
