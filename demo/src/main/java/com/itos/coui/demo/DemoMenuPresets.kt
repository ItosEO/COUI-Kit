package com.itos.coui.demo

import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.*
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.CouiTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.menu.Menu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.TopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults

/** Menu 的顶栏装配预设；放入 SettingsPage.actions，复用同一个菜单及完整按钮锚点。 */
@Composable
internal fun <T> DemoSettingsOverflowMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<CouiMenuItem<T>>,
    onItemClick: (CouiMenuItem<T>) -> Unit,
    style: CouiMenuStyle,
    contentDescription: String,
    enabled: Boolean = true,
) {
    val dark = CouiTheme.isDark
    val colors = CouiPageDefaults.themeColors(dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    val show = { onExpandedChange(true) }
    Box(modifier.size(48.dp), contentAlignment = Alignment.Center) {
        if (Build.VERSION.SDK_INT >= 34) TopAppBarButton(onClick = show, enabled = enabled,
            style = CouiToolbarStyle(dark, circleShape = CouiShape(20.dp, 2f), capsuleShape = CouiCapsuleShape,
                contentColor = colors.title, textStyle = rememberCouiTextStyle(CouiTextKind.Toolbar),
                cursorColor = colors.accent, contourGlowEnabled = contour,
                material = { CouiTopAppBarButtonDefaults.systemMaterial(dark, it) },
                shadow = { CouiTopAppBarButtonDefaults.shadow(dark, it, density, contour) })) {
            Icon(CouiIcons.More, contentDescription, Modifier.size(24.dp),
                tint = LocalCouiContentColor.current)
        } else Box(Modifier.size(40.dp)
            .clickable(remember { MutableInteractionSource() }, null, enabled, role = Role.Button, onClick = show),
            contentAlignment = Alignment.Center) {
            Icon(CouiIcons.More, contentDescription, tint = colors.title)
        }
        Menu(expanded = expanded, onDismissRequest = { onExpandedChange(false) },
            items = items, style = style, onItemClick = { onItemClick(it); onExpandedChange(false) })
    }
}
