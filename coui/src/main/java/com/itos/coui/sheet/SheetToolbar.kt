package com.itos.coui.sheet

import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.effect.CouiContourStyle
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiDarkTheme
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults
import com.itos.coui.topappbar.LocalCouiToolbarBackdrop

internal val LocalCouiSheetSurfaceBackdrop = staticCompositionLocalOf<CouiBackdrop?> { null }

/** 当前面板已绘制的纯背景源，供同窗口浮动控件使用；独立弹窗仍消费窗口内容源。 */
@Composable
fun rememberCouiSheetSurfaceBackdrop(): CouiBackdrop =
    checkNotNull(LocalCouiSheetSurfaceBackdrop.current) { "面板背景源必须在 ModalBottomSheet 内容中读取" }

/**
 * Panel 工具栏：按钮采样面板已经绘制的背景，不采样含按钮自身的窗口内容源。
 * style 显式提供按钮外观；标题保留原厂角色并合并 titleStyle，titleColor 优先于样式中的颜色。
 */
@RequiresApi(34)
@Composable
fun SheetToolbar(
    modifier: Modifier = Modifier,
    title: String,
    dark: Boolean = CouiTheme.isDark,
    style: CouiToolbarStyle? = null,
    titleStyle: TextStyle = TextStyle.Default,
    titleColor: Color = Color.Unspecified,
    navigation: @Composable (CouiToolbarStyle) -> Unit = {},
    actions: @Composable (CouiToolbarStyle) -> Unit = {},
) {
    val backdrop = rememberCouiSheetSurfaceBackdrop()
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    val buttonTextStyle = rememberSheetTextStyle(SheetTextKind.Button)
    val colors = CouiDialogDefaults.themeColors(dark)
    val resolvedStyle = style ?: remember(dark, density, contour, buttonTextStyle, colors.action) {
        CouiToolbarStyle(dark, CouiTopAppBarButtonDefaults.circleShape, CouiCapsuleShape,
            material = { CouiSheetDefaults.toolbarMaterial(dark, it) },
            shadow = { CouiTopAppBarButtonDefaults.shadow(dark, it, density, contour, alphaScale = 1f) },
            contentColor = colors.action, textStyle = buttonTextStyle,
            cursorColor = colors.action, contourGlowEnabled = contour,
            contourStyle = { capsule -> if (capsule) CouiContourStyle.Button else CouiContourStyle.CircleButton })
    }
    val resolvedTitleStyle = rememberSheetTextStyle(SheetTextKind.Title).copy(textAlign = TextAlign.Center).merge(titleStyle)
    val resolvedTitleColor = titleColor.takeOrElse {
        titleStyle.color.takeOrElse { (style?.contentColor ?: Color.Unspecified).takeOrElse { colors.title } }
    }
    CompositionLocalProvider(LocalCouiToolbarBackdrop provides backdrop, LocalCouiDarkTheme provides resolvedStyle.dark) {
        Layout(modifier = modifier.fillMaxWidth().padding(top = CouiSheetDefaults.toolbarTopSpacing)
            .padding(horizontal = CouiSheetDefaults.toolbarHorizontalPadding), content = {
            Box { navigation(resolvedStyle) }
            Box { actions(resolvedStyle) }
            Box(Modifier.padding(vertical = CouiSheetDefaults.toolbarTitleVerticalPadding)
                .heightIn(min = CouiSheetDefaults.toolbarTitleMinHeight), contentAlignment = Alignment.Center) {
                BasicText(title, Modifier.semantics { heading() },
                    style = resolvedTitleStyle.copy(color = resolvedTitleColor),
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }) { measurables, constraints ->
            val width = constraints.maxWidth
            val gap = CouiSheetDefaults.toolbarTitleGap.roundToPx()
            val buttonConstraints = Constraints(maxWidth = (width / 2 - gap).coerceAtLeast(0),
                maxHeight = constraints.maxHeight)
            val navigationPlaceable = measurables[0].measure(buttonConstraints)
            val actionPlaceable = measurables[1].measure(buttonConstraints)
            // 两边按较宽操作区等量避让，标题保持窗口居中；长标题可自然增加高度。
            val inset = maxOf(navigationPlaceable.width, actionPlaceable.width) + gap
            val titlePlaceable = measurables[2].measure(Constraints(
                maxWidth = (width - 2 * inset).coerceAtLeast(0), maxHeight = constraints.maxHeight))
            val height = constraints.constrainHeight(maxOf(CouiSheetDefaults.toolbarHeight.roundToPx(),
                navigationPlaceable.height, actionPlaceable.height, titlePlaceable.height))
            layout(width, height) {
                navigationPlaceable.placeRelative(0, (height - navigationPlaceable.height) / 2)
                actionPlaceable.placeRelative(width - actionPlaceable.width, (height - actionPlaceable.height) / 2)
                titlePlaceable.placeRelative((width - titlePlaceable.width) / 2, (height - titlePlaceable.height) / 2)
            }
        }
    }
}
