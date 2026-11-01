package com.itos.coui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.theme.CouiTheme
import com.itos.coui.menu.ProvideCouiMenuBackdrop

/**
 * 页面容器。先测顶栏、底栏再传内容留白，首帧就拥有正确尺寸。
 * topBar 拥有状态栏 inset；content 必须在滚动内容内应用并消费 PaddingValues。
 * 水平安全区由容器消费；有 bottomBar 时由该 slot 处理底部 inset，其测量高度进入内容留白。
 * 无底栏时使用底部安全区。FAB 放在底栏上方、逻辑末端，slot 自己设置间距。
 * 三个浮层 slot 不录入其消费的正文源；窗口源录制完整页面，供独立菜单/弹窗使用。
 */
@Composable
fun CouiSettingsScaffold(
    modifier: Modifier = Modifier,
    dark: Boolean = CouiTheme.isDark,
    colors: CouiSettingsColors = CouiSettingsDefaults.themeColors(dark),
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    bottomBar: @Composable (CouiBackdrop) -> Unit = {},
    floatingActionButton: @Composable (CouiBackdrop) -> Unit = {},
    topBar: @Composable (CouiBackdrop) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    var consumed by remember { mutableStateOf(WindowInsets(0, 0, 0, 0)) }
    val insets = contentWindowInsets.exclude(consumed)
    CompositionLocalProvider(LocalSettingsStyle provides SettingsStyle(dark, colors)) {
        ProvideCouiMenuBackdrop(isolateContent = false) { source ->
            SubcomposeLayout(modifier.fillMaxSize().then(source).background(colors.background)
                .onConsumedWindowInsetsChanged { consumed = it }
                .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal))) { constraints ->
                val loose = constraints.copy(minWidth = 0, minHeight = 0)
                val bars = subcompose(SettingsSlot.TopBar) { topBar(backdrop) }.map { it.measure(loose) }
                val top = bars.maxOfOrNull { it.height } ?: 0
                val bottomBars = subcompose(SettingsSlot.BottomBar) { bottomBar(backdrop) }.map { it.measure(loose) }
                val bottom = bottomBars.maxOfOrNull { it.height } ?: insets.getBottom(this)
                val floating = subcompose(SettingsSlot.Floating) { floatingActionButton(backdrop) }
                    .map { it.measure(loose) }
                val padding = PaddingValues(top = top.toDp(), bottom = bottom.toDp())
                val body = subcompose(SettingsSlot.Content) {
                    // 顶栏等消费正文源；独立 Popup/Dialog 消费外层完整窗口源，不把自身录进去。
                    Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(colors.background)) {
                        content(padding)
                    }
                }.map { it.measure(constraints) }
                layout(constraints.maxWidth, constraints.maxHeight) {
                    body.forEach { it.placeRelative(0, 0) }
                    bars.forEach { it.placeRelative(0, 0) }
                    floating.forEach { it.placeRelative(constraints.maxWidth - it.width,
                        (constraints.maxHeight - bottom - it.height).coerceAtLeast(0)) }
                    bottomBars.forEach { it.placeRelative(0, constraints.maxHeight - it.height) }
                }
            }
        }
    }
}

private enum class SettingsSlot { TopBar, BottomBar, Floating, Content }
