package com.itos.coui.demo

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import com.itos.coui.scroll.couiVerticalScroll
import com.itos.coui.scroll.CouiOverscrollEffect
import com.itos.coui.scroll.rememberCouiOverscrollEffect
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.theme.LocalCouiContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import androidx.compose.ui.platform.LocalDensity
import android.os.Build
import com.itos.coui.shape.CouiCapsuleShape

import com.itos.coui.layout.*

/**
 * 普通设置二级页：52dp 顶栏、独立采样/滚动内容和系统安全区。业务导航和返回拦截由宿主处理。
 * 标题依据实际 preference_list_with_appbar 的 SecondTitle，未启用另一条大标题折叠分支。
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    val backdrop = rememberCouiBackdrop()
    Scaffold(modifier = modifier.fillMaxSize()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom), topBar = {
                CouiTopAppBar(Modifier.fillMaxWidth(), backdrop) {
                    Row(Modifier.fillMaxWidth().statusBarsPadding().height(CouiPageDefaults.toolbarHeight)
                        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (Build.VERSION.SDK_INT >= 34) {
                            CouiTopAppBarButton(onClick = onBack, style = CouiToolbarStyle(dark = dark,
                                contourGlowEnabled = contour, circleShape = CouiShape(20.dp, 2f),
                                capsuleShape = CouiCapsuleShape, contentColor = colors.title,
                                textStyle = rememberCouiTextStyle(CouiTextKind.Toolbar), cursorColor = colors.accent,
                                material = { CouiTopAppBarButtonDefaults.systemMaterial(dark, it) },
                                shadow = { CouiTopAppBarButtonDefaults.shadow(dark, it, density, contour, alphaScale = 1f) })) {
                                Icon(DemoIcons.Back, backDescription, Modifier.size(24.dp))
                            }
                        } else Box(Modifier.size(48.dp).clip(CouiShape(24.dp, 2f))
                            .couiButtonFeedback(dark, source, true)
                            .clickable(source, indication = null, role = Role.Button, onClick = onBack)
                            .semantics { contentDescription = backDescription }, contentAlignment = Alignment.Center) {
                            DemoSettingsArrow(Modifier.size(24.dp), back = true, color = colors.title)
                        }
                        BasicText(title, Modifier.weight(1f).padding(start = 8.dp, end = 8.dp).semantics { heading() },
                            style = rememberCouiTextStyle(CouiTextKind.Toolbar).copy(color = colors.title),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        actions()
                    }
                }
    }) { innerPadding ->
        Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(colors.background)) {
            Column(Modifier.fillMaxSize().couiVerticalScroll(scrollState, overscrollEffect = overscrollEffect)
                .padding(innerPadding).consumeWindowInsets(innerPadding)
                .padding(bottom = CouiPageDefaults.groupSpacing), content = content)
        }
    }
}
