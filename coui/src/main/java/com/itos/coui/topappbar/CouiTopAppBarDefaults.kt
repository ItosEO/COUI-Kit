package com.itos.coui.topappbar

import androidx.compose.ui.unit.dp

object CouiTopAppBarDefaults {
    val fadeHeight = 96.dp
    @Deprecated("旧版按 density 放大的调参值；默认采用 referenceBlurRadiusPx 的原始引擎值")
    val referenceBlurRadius = 12.dp
    /** Settings 17 AppBarBlurParams 的原始引擎值；默认不再按 density 放大。 */
    const val referenceBlurRadiusPx = 30f
    val bottomExtension = 12.dp
}
