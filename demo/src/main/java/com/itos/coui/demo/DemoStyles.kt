package com.itos.coui.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import com.itos.coui.menu.CouiMenuDefaults
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults

@Composable
internal fun demoToolbarStyle(dark: Boolean, contour: Boolean, search: Boolean = false): CouiToolbarStyle {
    val density = LocalDensity.current.density
    val colors = CouiTheme.colorSchemeFor(dark)
    return CouiToolbarStyle(
        dark = dark,
        circleShape = DemoShapes.couiCircle,
        capsuleShape = DemoShapes.couiCapsule,
        material = { capsule -> CouiTopAppBarButtonDefaults.material(dark, capsule, search) },
        shadow = { capsule -> CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour, search) },
        contentColor = colors.onSurface,
        textStyle = rememberCouiTextStyle(CouiTextKind.Button),
        cursorColor = colors.accent,
        contourGlowEnabled = contour,
    )
}

@Composable
internal fun demoMenuStyle(dark: Boolean): CouiMenuStyle {
    return CouiMenuDefaults.style(dark).copy(
        shape = DemoShapes.menu,
        itemShape = DemoShapes.menuItem,
        checkboxShape = DemoShapes.checkbox,
        checkboxBorderShape = DemoShapes.checkboxBorder,
    )
}
