package com.itos.coui.demo

import androidx.compose.runtime.Composable
import com.itos.coui.menu.CouiMenuDefaults
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.CouiToolbarStyle

@Composable
internal fun demoMenuStyle(dark: Boolean): CouiMenuStyle {
    return CouiMenuDefaults.style(dark).copy(
        shape = DemoShapes.menu,
        itemShape = DemoShapes.menuItem,
        checkboxShape = DemoShapes.checkbox,
        checkboxBorderShape = DemoShapes.checkboxBorder,
    )
}

@Composable
internal fun CouiToolbarStyle.withDemoThemeTint(): CouiToolbarStyle {
    val tint = CouiTheme.materialTintFor(dark)
    return copy(material = { capsule -> material(capsule).copy(themeTint = tint) })
}
