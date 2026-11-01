package com.itos.coui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Settings IllustrationPreference 的独立图示区域；不将图示塞入普通点击行。 */
@Composable
fun CouiSettingsIllustration(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.widthIn(max = 412.dp).heightIn(max = 300.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center, content = content)
    }
}
