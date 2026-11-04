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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.TopAppBar
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults
import com.itos.coui.topappbar.CouiTopAppBarDefaults
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import androidx.compose.ui.platform.LocalDensity
import android.os.Build

import com.itos.coui.layout.*

/**
 * 普通设置二级页：52dp 顶栏、独立采样/滚动内容和系统安全区。业务导航和返回拦截由宿主处理。
 * 标题依据实际 preference_list_with_appbar 的 SecondTitle，未启用另一条大标题折叠分支。
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
    Scaffold(modifier = modifier.fillMaxSize().imePadding(),
        containerColor = colors.background,
        floatingActionButton = floatingAction,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = title, kind = CouiTextKind.Toolbar, color = colors.title,
                        textAlign = TextAlign.Start, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                dark = dark,
                colors = CouiTopAppBarDefaults.colors(backgroundColor = Color.Transparent,
                    titleColor = colors.title, dark = dark),
                navigationIcon = {
                    if (Build.VERSION.SDK_INT >= 34) {
                        TopAppBarButton(onClick = onBack, style = CouiTopAppBarButtonDefaults.style(dark).copy(
                            contentColor = colors.title,
                            textStyle = rememberCouiTextStyle(CouiTextKind.Toolbar), cursorColor = colors.accent,
                            material = { CouiTopAppBarButtonDefaults.systemMaterial(dark, it) },
                            shadow = { CouiTopAppBarButtonDefaults.shadow(dark, it, density, contour, alphaScale = 1f) })) {
                            Icon(CouiIcons.Back, backDescription, Modifier.size(24.dp))
                        }
                    } else Box(Modifier.size(48.dp).clip(CouiShape(24.dp, 2f))
                        .couiButtonFeedback(dark, source, true)
                        .clickable(source, indication = null, role = Role.Button, onClick = onBack)
                        .semantics { contentDescription = backDescription }, contentAlignment = Alignment.Center) {
                        DemoSettingsArrow(Modifier.size(24.dp), back = true, color = colors.title)
                    }
                },
                actions = { actions() },
            )
    }) { innerPadding ->
        Column(Modifier.fillMaxSize().couiVerticalScroll(scrollState, overscrollEffect = overscrollEffect)
            .padding(innerPadding).consumeWindowInsets(innerPadding)
            .padding(bottom = CouiPageDefaults.groupSpacing), content = content)
    }
}
