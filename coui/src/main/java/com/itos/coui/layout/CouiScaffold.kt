package com.itos.coui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.LayoutDirection
import com.itos.coui.theme.CouiTheme

/**
 * 可选页面容器：为正文保留完整视口，顶栏和底栏绘制在正文之上。
 *
 * [windowInsets] 的横向安全区由容器应用并消费；纵向安全区由对应栏位处理。
 * 没有栏位或栏位高度为零时，将剩余纵向安全区交给 [content]。
 * 正文应应用返回的 PaddingValues 并消费它；滚动内容可将 padding 放在滚动节点内侧。
 * 容器需要有界宽高。IME 避让由宿主用 Modifier.imePadding() 缩小视口。
 * 背景采样、滚动和导航由宿主装配，组件不要求放在此容器中。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouiScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    containerColor: Color = CouiTheme.colorScheme.background,
    windowInsets: WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
    content: @Composable (PaddingValues) -> Unit,
) {
    val remainingInsets = remember(windowInsets) { MutableWindowInsets(windowInsets) }
    val contentPadding = remember { ScaffoldPadding() }
    val body: @Composable () -> Unit = remember(content) { { content(contentPadding) } }
    SubcomposeLayout(
        modifier = modifier.background(containerColor)
            .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
            .onConsumedWindowInsetsChanged { remainingInsets.insets = windowInsets.exclude(it) },
    ) { constraints ->
        require(constraints.hasBoundedWidth && constraints.hasBoundedHeight) {
            "CouiScaffold 需要有界的宽度和高度"
        }
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val top = subcompose(ScaffoldSlot.TopBar, topBar).map { it.measure(looseConstraints) }
        val bottom = subcompose(ScaffoldSlot.BottomBar, bottomBar).map { it.measure(looseConstraints) }
        val topHeight = top.maxOfOrNull { it.height } ?: 0
        val bottomHeight = bottom.maxOfOrNull { it.height } ?: 0
        // 在正文子组合与测量前更新同一个对象，折叠栏可持续读取本帧的实际占位。
        contentPadding.value = PaddingValues(
            top = (if (topHeight > 0) topHeight else remainingInsets.getTop(this)).toDp(),
            bottom = (if (bottomHeight > 0) bottomHeight else remainingInsets.getBottom(this)).toDp(),
        )
        val bodyPlaceables = subcompose(ScaffoldSlot.Content, body).map { it.measure(looseConstraints) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            bodyPlaceables.forEach { it.placeRelative(0, 0) }
            top.forEach { it.placeRelative(0, 0) }
            bottom.forEach { it.placeRelative(0, constraints.maxHeight - bottomHeight) }
        }
    }
}

private enum class ScaffoldSlot { TopBar, BottomBar, Content }

@Stable
private class ScaffoldPadding : PaddingValues {
    var value by mutableStateOf<PaddingValues>(PaddingValues())

    override fun calculateLeftPadding(layoutDirection: LayoutDirection) = value.calculateLeftPadding(layoutDirection)
    override fun calculateTopPadding() = value.calculateTopPadding()
    override fun calculateRightPadding(layoutDirection: LayoutDirection) = value.calculateRightPadding(layoutDirection)
    override fun calculateBottomPadding() = value.calculateBottomPadding()
}
