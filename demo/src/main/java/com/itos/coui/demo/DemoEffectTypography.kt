package com.itos.coui.demo

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

/** 效果页的说明采用系统支持文本角色；调节项由设置行持有标题和数值。 */
@Composable
internal fun EffectText(text: String, modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = rememberCouiTextStyle(CouiTextKind.ItemSummary)) {
    BasicText(text, modifier, style = style.copy(color = color))
}
