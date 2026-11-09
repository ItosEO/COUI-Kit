package com.itos.coui.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.itos.coui.icons.CouiIcons

/** 各演示场景复用已核对调用来源的库图标。 */
internal object DemoIcons {
    val Effects: ImageVector
        @Composable get() = CouiIcons.Outlined.WandSpark
    val EffectsSelected: ImageVector
        @Composable get() = CouiIcons.Filled.WandSpark
    val Settings: ImageVector
        @Composable get() = CouiIcons.Filled.SettingDetail
    val SettingsSelected: ImageVector
        @Composable get() = CouiIcons.Filled.SettingDetail
    val Tune: ImageVector
        @Composable get() = CouiIcons.Filled.Sun
    val Switch: ImageVector
        @Composable get() = CouiIcons.Filled.Bluetooth
    val Checkbox: ImageVector
        @Composable get() = CouiIcons.Filled.Grid
    val Dialogs: ImageVector
        @Composable get() = CouiIcons.Filled.Bell
    val Title: ImageVector
        @Composable get() = CouiIcons.Filled.Foldable
    val Date: ImageVector
        @Composable get() = CouiIcons.Filled.Health
    val SimOne: ImageVector
        @Composable get() = CouiIcons.Filled.Transfer
    val SimTwo: ImageVector
        @Composable get() = CouiIcons.Filled.Transfer
}
