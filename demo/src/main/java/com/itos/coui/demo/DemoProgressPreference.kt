package com.itos.coui.demo

import com.itos.coui.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle

/** 带小型进度操作的列表项；进度、下载/暂停文案与任务生命周期归宿主。 */
@Composable
internal fun DemoProgressPreference(
    modifier: Modifier = Modifier,
    title: String,
    progress: Float,
    actionText: String,
    onAction: () -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    require(progress.isFinite() && progress in 0f..1f)
    val style = LocalDemoSettingsStyle.current
    val source = remember { MutableInteractionSource() }
    val textStyle = rememberCouiTextStyle(CouiTextKind.Progress).copy(textAlign = TextAlign.Center)
    DemoPreferenceRow(modifier, title, summary, enabled, icon) {
        Box(Modifier.padding(start = 16.dp).widthIn(min = 52.dp).height(28.dp).clip(CouiCapsuleShape)
            .background(style.colors.accent.copy(alpha = if (style.dark) .3f else .12f))
            .progressSemantics(progress).couiButtonFeedback(style.dark, source, enabled)
            .clickable(source, null, enabled, role = Role.Button, onClick = onAction), contentAlignment = Alignment.Center) {
            Box(Modifier.matchParentSize().drawWithContent {
                val width = size.width * progress
                drawRect(if (enabled) style.colors.accent else style.colors.disabled,
                    size = androidx.compose.ui.geometry.Size(width, size.height))
            })
            BasicText(actionText, Modifier.padding(horizontal = 6.dp),
                style = textStyle.copy(color = style.colors.accent), maxLines = 1)
            // 原厂文字在已完成区域和未完成区域使用不同颜色；两份文字共享测量。
            Box(Modifier.matchParentSize().drawWithContent {
                drawContext.canvas.save()
                drawContext.canvas.clipRect(0f, 0f, size.width * progress, size.height)
                drawContent(); drawContext.canvas.restore()
            }, contentAlignment = Alignment.Center) {
                BasicText(actionText, Modifier.padding(horizontal = 6.dp), style = textStyle.copy(color = Color.White), maxLines = 1)
            }
        }
    }
}
