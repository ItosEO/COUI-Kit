package com.itos.coui.dialog

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/** 普通弹窗内部排版角色，不建立独立的公共 Typography 组件。 */
internal enum class DialogTextKind(internal val size: Float, internal val family: String, internal val weight: Int) {
    Title(18f, "sans-serif-semibold", 550),
    Message(13f, "sans-serif", 400),
    Button(16f, "sans-serif-medium", 500),
    Input(15f, "sans-serif", 400),
    Counter(10f, "sans-serif", 400),
}

/** 系统资源决定排版，组件与语义由 Compose 承载，不继承宿主 M3 Typography。 */
@Composable
internal fun DialogText(modifier: Modifier = Modifier, text: String, kind: DialogTextKind, color: Color,
    onLineCount: (Int) -> Unit = {}, textAlign: TextAlign? = null) {
    var lines by remember(text) { mutableIntStateOf(1) }
    val style = rememberDialogTextStyle(kind)
    BasicText(text, modifier.then(if (kind == DialogTextKind.Title) Modifier.semantics { heading() } else Modifier),
        style = style.copy(color = color,
            textAlign = textAlign ?: if (kind == DialogTextKind.Message && lines > 1) TextAlign.Start else TextAlign.Center),
        maxLines = if (kind == DialogTextKind.Button) 2 else Int.MAX_VALUE,
        overflow = if (kind == DialogTextKind.Button) TextOverflow.Ellipsis else TextOverflow.Clip,
        onTextLayout = {
            if (lines != it.lineCount) lines = it.lineCount
            onLineCount(it.lineCount)
        })
}

@Composable
internal fun rememberDialogTextStyle(kind: DialogTextKind): TextStyle {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(kind, configuration, density) {
        val typeface = Typeface.create(Typeface.create(kind.family, Typeface.NORMAL), kind.weight, false)
        val pixels = couiTextSize(kind, configuration) { with(density) { it.sp.toPx() } }
        val metrics = Paint().apply { this.typeface = typeface; textSize = pixels }.fontMetricsInt
        val base = metrics.descent - metrics.ascent
        // TextView lineSpacingMultiplier 作用于 ascent/descent；额外间距在行下方，末行不加。
        val height = base + (base * (couiLineSpacing(kind, configuration) - 1f)).roundToInt()
        TextStyle(fontFamily = FontFamily(typeface), fontWeight = FontWeight(kind.weight), fontSynthesis = FontSynthesis.None,
            fontSize = with(density) { pixels.toSp() }, lineHeight = with(density) { height.toFloat().toSp() },
            platformStyle = PlatformTextStyle(includeFontPadding = true),
            lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Top,
                trim = LineHeightStyle.Trim.LastLineBottom, mode = LineHeightStyle.Mode.Minimum))
    }
}

private fun couiTextSize(kind: DialogTextKind, configuration: Configuration, spToPx: (Float) -> Float): Float {
    val pixels = spToPx(kind.size).roundToInt().toFloat()
    if (kind != DialogTextKind.Button) return pixels
    // COUIButton -> COUIChangeTextUtil.adaptFontSize(G4)。保留 SP 转换后的取整顺序。
    val scale = if (configuration.densityDpi == 300 || configuration.densityDpi == 296 ||
        configuration.smallestScreenWidthDp <= 210) 1f else configuration.fontScale
    return (pixels / scale).roundToInt() * scale.coerceAtMost(1.35f)
}

private fun couiLineSpacing(kind: DialogTextKind, configuration: Configuration): Float = when (kind) {
    DialogTextKind.Title -> if (configuration.locales[0].language == "my" && configuration.locales[0].country == "MM")
        1.2f else if (Build.VERSION.SDK_INT >= 35) 1.1429f else 1.15f
    DialogTextKind.Message -> if (Build.VERSION.SDK_INT >= 35) 1.1739f else 1.1f
    DialogTextKind.Button -> if (Build.VERSION.SDK_INT >= 35) 1.1579f else 1.2f
    DialogTextKind.Input -> if (Build.VERSION.SDK_INT >= 35) 1.1321f else 1.1f
    DialogTextKind.Counter -> 1f
}
