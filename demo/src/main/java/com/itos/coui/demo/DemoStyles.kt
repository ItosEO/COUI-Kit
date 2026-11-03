package com.itos.coui.demo

import androidx.compose.runtime.Composable
import com.itos.coui.menu.CouiMenuDefaults
import com.itos.coui.menu.CouiMenuStyle

@Composable
internal fun demoMenuStyle(dark: Boolean): CouiMenuStyle {
    return CouiMenuDefaults.style(dark).copy(
        shape = DemoShapes.menu,
        itemShape = DemoShapes.menuItem,
        checkboxShape = DemoShapes.checkbox,
        checkboxBorderShape = DemoShapes.checkboxBorder,
    )
}
