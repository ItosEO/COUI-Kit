package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import com.itos.coui.scroll.couiVerticalScroll
import com.itos.coui.scroll.CouiOverscrollEffect
import com.itos.coui.scroll.rememberCouiOverscrollEffect
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.LargeTopAppBar
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults
import com.itos.coui.topappbar.CouiTopAppBarDefaults
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import android.os.Build

import com.itos.coui.layout.*

/**
 * Demo 二级页统一使用大标题折叠顶栏。业务导航和返回拦截由宿主处理。
 * floatingAction 位于正文采样源之外，按页面安全区固定放置，不参与正文滚动。
 */
@Composable
internal fun DemoSettingsPage(
    modifier: Modifier = Modifier,
    title: String,
    backDescription: String,
    onBack: () -> Unit,
    dark: Boolean,
    colors: CouiPageColors,
    scrollState: ScrollState = rememberScrollState(),
    actions: @Composable () -> Unit = {},
    overscrollEffect: CouiOverscrollEffect? = rememberCouiOverscrollEffect(),
    floatingAction: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    val behavior = CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        canScroll = { scrollState.maxValue > 0 })
    val localDensity = LocalDensity.current
    // 弹窗持有输入时，后面的页面不为其键盘提前腾空背景。
    val imePadding = if (LocalWindowInfo.current.isWindowFocused) Modifier.imePadding() else Modifier
    Scaffold(modifier = modifier.fillMaxSize().then(imePadding)
        .nestedScroll(behavior.nestedScrollConnection),
        containerColor = colors.background,
        floatingActionButton = floatingAction,
        topBar = {
            val navigation: @Composable () -> Unit = {
                    if (Build.VERSION.SDK_INT >= 34) {
                        TopAppBarButton(onClick = onBack, style = CouiTopAppBarButtonDefaults.style(dark).copy(
                            contentColor = colors.title,
                            textStyle = rememberCouiTextStyle(CouiTextKind.Toolbar), cursorColor = colors.accent,
                            material = { CouiTopAppBarButtonDefaults.systemMaterial(dark, it) },
                            shadow = { CouiTopAppBarButtonDefaults.shadow(dark, it, density, contour, alphaScale = 1f) })) {
                            Icon(CouiIcons.Filled.Back, backDescription, Modifier.size(24.dp))
                        }
                    } else Box(Modifier.size(48.dp).clip(CouiShape(24.dp, 2f))
                        .couiButtonFeedback(dark, source, true)
                        .clickable(source, indication = null, role = Role.Button, onClick = onBack)
                        .semantics { contentDescription = backDescription }, contentAlignment = Alignment.Center) {
                        DemoSettingsArrow(Modifier.size(24.dp), back = true, color = colors.title)
                    }
            }
            val barColors = CouiTopAppBarDefaults.colors(backgroundColor = LocalDemoTopBarGradient.current,
                titleColor = colors.title, dark = dark)
            LargeTopAppBar(title = {
                androidx.compose.material3.Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }, navigationIcon = navigation, actions = { actions() },
                scrollBehavior = behavior, dark = dark, colors = barColors)
    }) { innerPadding ->
        val contentPadding = remember(behavior, innerPadding, localDensity) {
            behavior.state.contentPadding(innerPadding, localDensity)
        }
        Column(Modifier.fillMaxSize().couiVerticalScroll(scrollState, overscrollEffect = overscrollEffect)
            .padding(contentPadding).consumeWindowInsets(innerPadding)
            .padding(bottom = CouiPageDefaults.groupSpacing), content = content)
    }
}
