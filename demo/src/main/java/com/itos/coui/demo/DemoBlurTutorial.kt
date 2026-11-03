package com.itos.coui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.drawCouiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.*
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import kotlin.math.roundToInt

/** 按设置预览页的“图示—说明—调节”顺序组织，右侧采样自己的原图内容。 */
@Composable
internal fun BlurTutorial(dark: Boolean) {
    var radius by rememberSaveable { mutableFloatStateOf(30f) }
    Box(Modifier.fillMaxWidth().padding(top = 16.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        Row(Modifier.widthIn(max = 412.dp).heightIn(max = 300.dp).fillMaxWidth().height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (blurred in listOf(false, true)) Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val backdrop = if (blurred) rememberCouiBackdrop() else null
                Box(Modifier.weight(1f).fillMaxWidth().clip(DemoShapes.card)) {
                    Column(Modifier.fillMaxSize()
                        .then(if (backdrop != null) Modifier.couiBackdrop(backdrop) else Modifier)
                        .background(MaterialTheme.colorScheme.surfaceBright)
                        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.controls), style = rememberCouiTextStyle(CouiTextKind.PreferenceTitle))
                        listOf(DemoIcons.Switch, DemoIcons.Tune, DemoIcons.Date, DemoIcons.Dialogs).forEachIndexed { index, icon ->
                            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHighest, DemoShapes.item)
                                .padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, Modifier.size(16.dp), tint = Color(listOf(0xFF34B978, 0xFF1A8CFF, 0xFFA884E7, 0xFFFF9B35)[index]))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(listOf(R.string.switch_title, R.string.slider_title, R.string.picker_date_title, R.string.dialog_title)[index]),
                                    style = rememberCouiTextStyle(CouiTextKind.Category), maxLines = 1)
                            }
                        }
                    }
                    if (backdrop != null) Box(Modifier.matchParentSize().drawCouiBackdrop(backdrop,
                        shape = { DemoShapes.card }, effects = { blur(radius) }))
                }
                Text(stringResource(if (blurred) R.string.illustration_blurred else R.string.illustration_original),
                    Modifier.padding(top = 8.dp), style = rememberCouiTextStyle(CouiTextKind.GroupTitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    Text(stringResource(R.string.intro_effect_backdrop), Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
        style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary), color = MaterialTheme.colorScheme.onSurfaceVariant)
    Group {
        DemoSliderPreference(title = stringResource(R.string.blur_radius), value = radius, onValueChange = { radius = it },
            valueText = radius.roundToInt().toString(), valueRange = 0f..80f)
    }
}
