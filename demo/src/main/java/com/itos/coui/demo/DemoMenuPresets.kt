package com.itos.coui.demo

import com.itos.coui.settings.*
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.itos.coui.theme.LocalCouiContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.menu.CouiMenu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.shape.CouiShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults

/** CouiMenu 的顶栏装配预设；放入 SettingsPage.actions，复用同一个菜单及完整按钮锚点。 */
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
    val settings = LocalDemoSettingsStyle.current
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    val anchor = rememberCouiMenuAnchor(useTouchPosition = false)
    val show = { anchor.open { onExpandedChange(true) } }
    Box(modifier.size(48.dp).couiMenuAnchor(anchor), contentAlignment = Alignment.Center) {
        if (Build.VERSION.SDK_INT >= 34) CouiTopAppBarButton(onClick = show, enabled = enabled,
            style = CouiToolbarStyle(settings.dark, circleShape = CouiShape(20.dp, 2f), capsuleShape = CouiCapsuleShape,
                contentColor = settings.colors.title, textStyle = rememberCouiTextStyle(CouiTextKind.Toolbar),
                cursorColor = settings.colors.accent, contourGlowEnabled = contour,
                material = { CouiTopAppBarButtonDefaults.systemMaterial(settings.dark, it) },
                shadow = { CouiTopAppBarButtonDefaults.shadow(settings.dark, it, density, contour) })) {
            Icon(DemoIcons.More, contentDescription, Modifier.size(24.dp),
                tint = LocalCouiContentColor.current)
        } else Box(Modifier.size(40.dp)
            .clickable(remember { MutableInteractionSource() }, null, enabled, role = Role.Button, onClick = show),
            contentAlignment = Alignment.Center) {
            Icon(DemoIcons.More, contentDescription, tint = settings.colors.title)
        }
        CouiMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }, anchor = anchor,
            items = items, style = style, onItemClick = { onItemClick(it); onExpandedChange(false) })
    }
}
