package com.itos.coui.topappbar

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.itos.coui.theme.CouiTheme
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.search.CouiSearchBarState
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import kotlin.math.roundToInt

/** 设置首页标题与常驻搜索槽；scrollState 的连接须挂在实际内容滚动容器的祖先。 */
@Composable
fun CouiCollapsingTopAppBar(
    modifier: Modifier = Modifier,
    state: CouiCollapsingAppBarState,
    searchState: CouiSearchBarState,
    title: String,
    color: Color = CouiTheme.colorScheme.onSurface,
    backdrop: CouiBackdrop,
    backgroundColor: Color = Color.Transparent,
    dark: Boolean = CouiTheme.isDark,
    searchBar: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val topInset = WindowInsets.statusBars.getTop(density)
    val titleStyle = rememberCouiTextStyle(CouiTextKind.HomeTitle)
    val descent = remember(titleStyle, density) { Paint().apply {
        typeface = Typeface.create(Typeface.create("sans-serif-semibold", 0), 700, false)
        textSize = with(density) { titleStyle.fontSize.toPx() }
    }.fontMetrics.descent }
    val titleHeight = with(density) { CouiCollapsingAppBarDefaults.titleHeight.roundToPx().toFloat() }
    val collapsedHeight = with(density) { CouiCollapsingAppBarDefaults.collapsedTitleHeight.roundToPx().toFloat() }
    val titleBottom = with(density) { CouiCollapsingAppBarDefaults.titleBottom.toPx().toInt().toFloat() }
    val start = with(density) { CouiCollapsingAppBarDefaults.titleStart.toPx().toInt().toFloat() }
    val f = (state.offsetPx / (titleHeight - collapsedHeight)).coerceIn(0f, 1f)
    val p = searchState.pageProgress.coerceIn(0f, 1f)
    SideEffect { state.updateSearch(searchState.expanded, searchState.active) }
    CouiTopAppBar(modifier.fillMaxWidth(), backdrop,
        // HomepageImpl.updateBlurOverlayExtraHeight：展开为零，折叠或搜索编辑时使用页面资源。
        bottomExtension = if (searchState.active || state.collapsedFraction >= 1f)
            CouiCollapsingAppBarDefaults.searchBlurExtension else 0.dp,
        gradientSpeed = 3f, gradientColor = backgroundColor, dark = dark,
        backgroundAlpha = if (searchState.active) 1f else (state.offsetPx / (30 * density.density)).coerceIn(0f, 1f)) {
        Layout(content = {
            BasicText(title, Modifier.semantics { heading() }.graphicsLayer {
                alpha = (1f - f) * searchState.contentAlpha
                scaleX = (1f - .3f * f) * (.8f + .2f * searchState.contentAlpha)
                scaleY = scaleX
                translationY = -200f * p
                transformOrigin = TransformOrigin(0f, 0f)
            }, style = titleStyle.copy(color = color.copy(alpha = 1f)),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            searchBar()
        }) { children, constraints ->
            val titleNode = children[0].measure(constraints.copy(minWidth = 0, minHeight = 0,
                maxWidth = (constraints.maxWidth - 2 * start.roundToInt()).coerceAtLeast(0)))
            val search = children[1].measure(constraints.copy(minWidth = constraints.maxWidth, minHeight = 0))
            val remainingTitle = (titleHeight - state.offsetPx).coerceAtLeast(0f)
            val searchY = topInset + remainingTitle
            layout(constraints.maxWidth, (searchY + search.height).roundToInt()) {
                val expandedY = titleHeight - titleBottom - titleNode.height + descent
                val collapsedY = titleHeight - collapsedHeight + (collapsedHeight - titleNode.height) / 2
                val titleY = expandedY + (collapsedY - expandedY) * f - state.offsetPx
                titleNode.placeRelative((start - (8 * density.density) * f).roundToInt(), topInset + titleY.roundToInt())
                search.placeRelative(0, searchY.roundToInt())
            }
        }
    }
}
