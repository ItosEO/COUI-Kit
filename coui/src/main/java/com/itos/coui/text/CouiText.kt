package com.itos.coui.text

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

/** COUI 语义排版预设；宿主可通过 rememberCouiTextStyle 用于 Compose 文本与编辑框。 */
enum class CouiTextKind(internal val size: Float, internal val family: String, internal val weight: Int) {
    Title(18f, "sans-serif-semibold", 550),
    Message(13f, "sans-serif", 400),
    Button(16f, "sans-serif-medium", 500),
    PreferenceTitle(16f, "sans-serif-medium", 500),
    PreferenceSummary(13f, "sans-serif", 400),
    Category(12f, "sans-serif", 400),
    Toolbar(18f, "sans-serif-medium", 550),
    PageTitle(24f, "sans-serif-semibold", 600),
    SectionTitle(18f, "sans-serif-medium", 600),
    Body(15f, "sans-serif", 400),
    HomeTitle(40f, "sans-serif-semibold", 700),
    Search(15f, "sans-serif", 400),
    SearchCancel(14f, "sans-serif-medium", 500),
    PreferenceValue(14f, "sans-serif", 400),
    GroupTitle(12f, "sans-serif-medium", 500),
    Stepper(18f, "sans-serif-medium", 500),
    Progress(14f, "sans-serif-medium", 500),
    Description(12f, "sans-serif", 400),
    Menu(15f, "sans-serif-regular", 400),
    Input(15f, "sans-serif", 400),
    Counter(10f, "sans-serif", 400),
}

/** 系统资源决定排版，组件与语义由 Compose 承载，不继承宿主 M3 Typography。 */
@Composable
fun CouiText(modifier: Modifier = Modifier, text: String, kind: CouiTextKind, color: Color,
    onLineCount: (Int) -> Unit = {}, textAlign: TextAlign? = null) {
    var lines by remember(text) { mutableIntStateOf(1) }
    val style = rememberCouiTextStyle(kind)
    BasicText(text, modifier.then(if (kind == CouiTextKind.Title) Modifier.semantics { heading() } else Modifier),
        style = style.copy(color = color,
            textAlign = textAlign ?: if (kind == CouiTextKind.Message && lines > 1) TextAlign.Start else TextAlign.Center),
        maxLines = if (kind == CouiTextKind.Button) 2 else Int.MAX_VALUE,
        overflow = if (kind == CouiTextKind.Button) TextOverflow.Ellipsis else TextOverflow.Clip,
        onTextLayout = {
            if (lines != it.lineCount) lines = it.lineCount
            onLineCount(it.lineCount)
        })
}

@Composable
fun rememberCouiTextStyle(kind: CouiTextKind): TextStyle {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    return remember(kind, configuration, density) {
        val typeface = Typeface.create(Typeface.create(kind.family, Typeface.NORMAL), kind.weight, false)
        val fixedDp = kind == CouiTextKind.Toolbar || kind == CouiTextKind.PageTitle || kind == CouiTextKind.PreferenceValue
        val pixels = if (fixedDp) kind.size * density.density else couiTextSize(kind, configuration) { with(density) { it.sp.toPx() } }
        val metrics = Paint().apply { this.typeface = typeface; textSize = pixels }.fontMetricsInt
        val base = metrics.descent - metrics.ascent
        // TextView lineSpacingMultiplier 作用于 ascent/descent；额外间距在行下方，末行不加。
        val height = base + (base * (couiLineSpacing(kind, configuration) - 1f)).roundToInt()
        TextStyle(fontFamily = FontFamily(typeface), fontWeight = FontWeight(kind.weight), fontSynthesis = FontSynthesis.None,
            fontSize = with(density) { pixels.toSp() }, lineHeight = with(density) { height.toFloat().toSp() },
            platformStyle = PlatformTextStyle(includeFontPadding = kind !in listOf(
                CouiTextKind.Toolbar, CouiTextKind.PageTitle, CouiTextKind.HomeTitle)),
            lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Top,
                trim = LineHeightStyle.Trim.LastLineBottom, mode = LineHeightStyle.Mode.Minimum))
    }
}

private fun couiTextSize(kind: CouiTextKind, configuration: Configuration, spToPx: (Float) -> Float): Float {
    val pixels = spToPx(kind.size).roundToInt().toFloat()
    if (kind == CouiTextKind.SearchCancel) {
        val base = (pixels / configuration.fontScale).roundToInt()
        return base * if (configuration.fontScale < 1.15f) 1f else 1.15f
    }
    if (kind != CouiTextKind.Button && kind != CouiTextKind.Menu) return pixels
    // COUIButton -> COUIChangeTextUtil.adaptFontSize(G4)。保留 SP 转换后的取整顺序。
    val scale = if (configuration.densityDpi == 300 || configuration.densityDpi == 296 ||
        configuration.smallestScreenWidthDp <= 210) 1f else configuration.fontScale
    return (pixels / scale).roundToInt() * scale.coerceAtMost(if (kind == CouiTextKind.Menu) 1.6f else 1.35f)
}

private fun couiLineSpacing(kind: CouiTextKind, configuration: Configuration): Float = when (kind) {
    CouiTextKind.Title -> if (configuration.locales[0].language == "my" && configuration.locales[0].country == "MM")
        1.2f else if (Build.VERSION.SDK_INT >= 35) 1.1429f else 1.15f
    CouiTextKind.Category -> 1f
    CouiTextKind.GroupTitle -> if (Build.VERSION.SDK_INT >= 35) 1.143f else 1f
    CouiTextKind.Description -> if (Build.VERSION.SDK_INT >= 35) 1.143f else 1f
    CouiTextKind.Stepper -> if (Build.VERSION.SDK_INT >= 35) 1.1429f else 1.15f
    CouiTextKind.Progress -> if (Build.VERSION.SDK_INT >= 35) 1.2245f else 1.1f
    CouiTextKind.PreferenceTitle -> if (Build.VERSION.SDK_INT >= 35) 1.158f else 1.05f
    CouiTextKind.Toolbar -> if (Build.VERSION.SDK_INT >= 35) 1.1429f else 1.15f
    CouiTextKind.PreferenceSummary, CouiTextKind.Message -> if (Build.VERSION.SDK_INT >= 35) 1.1739f else 1.1f
    CouiTextKind.Button -> if (Build.VERSION.SDK_INT >= 35) 1.1579f else 1.2f
    CouiTextKind.PageTitle -> 1f
    CouiTextKind.SectionTitle -> if (Build.VERSION.SDK_INT >= 35) 1.1429f else 1.15f
    CouiTextKind.Body -> 1.1321f
    CouiTextKind.HomeTitle -> 1f
    CouiTextKind.Search -> if (Build.VERSION.SDK_INT >= 35) 1.1321f else 1.1f
    CouiTextKind.SearchCancel -> if (Build.VERSION.SDK_INT >= 35) 1.2245f else 1.1f
    CouiTextKind.PreferenceValue -> if (Build.VERSION.SDK_INT >= 35) 1.2245f else 1.1f
    CouiTextKind.Menu -> if (Build.VERSION.SDK_INT >= 35) 1.1321f else 1.1f
    CouiTextKind.Input -> if (Build.VERSION.SDK_INT >= 35) 1.1321f else 1.1f
    CouiTextKind.Counter -> 1f
}
