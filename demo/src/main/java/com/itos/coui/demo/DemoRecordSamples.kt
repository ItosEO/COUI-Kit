package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.effect.CouiMetaballRenderer
import kotlinx.coroutines.launch

private data class DemoRecord(
    val id: Int,
    val startTimeText: String,
    val averageFpsText: String,
    val averagePowerText: String,
    val durationText: String,
    @param:StringRes val remark: Int? = null,
    val hasSegmentFilter: Boolean = false,
)

private val SampleRecords = listOf(
    DemoRecord(1, "2026-11-03 14:32", "59.8", "3.42W", "10m24s", R.string.record_remark, true),
    DemoRecord(2, "2026-11-03 13:10", "119.5", "5.16W", "8m30s"),
    DemoRecord(3, "2026-11-03 11:45", "89.7", "4.28W", "12m45s"),
)

@Composable
internal fun RecordSwipeSamples(dark: Boolean) {
    var records by rememberSaveable { mutableStateOf(arrayListOf(1, 2, 3)) }
    var openId by remember { mutableIntStateOf(0) }
    var lastViewedId by rememberSaveable { mutableIntStateOf(0) }
    var viewedId by rememberSaveable { mutableIntStateOf(0) }
    if (Build.VERSION.SDK_INT < 33) {
        Text(stringResource(R.string.requires_api_33), Modifier.padding(horizontal = 16.dp))
        return
    }
    val context = LocalContext.current
    val iconSizePx = with(LocalDensity.current) { 44.dp.roundToPx() }
    val icon = remember(context, iconSizePx) {
        context.applicationInfo.loadIcon(context.packageManager).toBitmap(iconSizePx, iconSizePx).asImageBitmap()
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
        records.forEachIndexed { index, id ->
            key(id) {
                RecordSample(
                    record = SampleRecords.first { it.id == id },
                    icon = icon,
                    isLastViewed = lastViewedId == id,
                    isOpen = openId == id,
                    bottomSpacing = if (index == records.lastIndex) 0.dp else 10.dp,
                    onOpenChange = { open ->
                        if (open) openId = id else if (openId == id) openId = 0
                    },
                    onClick = { lastViewedId = id; viewedId = id },
                    onDelete = { records = records.filterTo(arrayListOf()) { it != id } },
                )
            }
        }
        if (records.isEmpty()) {
            DemoPreference(title = stringResource(R.string.record_restore), onClick = {
                records = arrayListOf(1, 2, 3); openId = 0; lastViewedId = 0; viewedId = 0
            })
        }
    }
    SampleRecords.firstOrNull { it.id == viewedId }?.let { record ->
        CouiAlertDialog(onDismissRequest = { viewedId = 0 }, dark = dark,
            title = stringResource(R.string.record_preview),
            confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), { viewedId = 0 })) {
            RecordHistoryContent(record = record)
        }
    }
}

@RequiresApi(33)
@Composable
private fun RecordSample(record: DemoRecord, icon: ImageBitmap, isLastViewed: Boolean, isOpen: Boolean, bottomSpacing: Dp,
    onOpenChange: (Boolean) -> Unit, onClick: () -> Unit, onDelete: () -> Unit) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val collapseHeight = remember(record.id) { Animatable(1f) }
    val iconAlpha = remember(record.id) { Animatable(1f) }
    var rowHeightPx by remember(record.id, record.remark) { mutableFloatStateOf(0f) }
    val collapsing = collapseHeight.targetValue == 0f
    val rowModifier = if (collapsing) {
        Modifier.height(with(density) { (rowHeightPx * collapseHeight.value).toDp() })
    } else Modifier
    // 列表项使用稳定 key，背景与遮罩各自持有可变 Shader。
    val rendering = remember { runCatching { CouiMetaballRenderer() to CouiMetaballRenderer() } }
    val renderers = rendering.getOrNull()
    if (renderers == null) {
        val failure = rendering.exceptionOrNull()
        Text(stringResource(R.string.renderer_error, failure?.message ?: failure?.javaClass?.simpleName.orEmpty()),
            color = MaterialTheme.colorScheme.error)
        return
    }
    Box(Modifier.fillMaxWidth().then(rowModifier).clipToBounds().onSizeChanged {
        if (!collapsing) rowHeightPx = it.height.toFloat()
    }) {
        Row(Modifier.fillMaxWidth().padding(bottom = bottomSpacing),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(bitmap = icon, contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(44.dp).alpha(iconAlpha.value).clip(DemoShapes.item))
            Box(Modifier.weight(1f)) {
                DemoRecordSwipeRow(
                    backgroundRenderer = renderers.first,
                    maskRenderer = renderers.second,
                    isOpen = isOpen,
                    onOpenChange = onOpenChange,
                    onClick = onClick,
                    onCommitDelete = {
                        onOpenChange(false)
                        scope.launch {
                            collapseHeight.animateTo(0f, tween(240, easing = FastOutSlowInEasing))
                            onDelete()
                        }
                    },
                    onDeleteAnimationStart = {
                        scope.launch { iconAlpha.animateTo(0f, tween(220, easing = FastOutSlowInEasing)) }
                    },
                    endActionContent = { committed, confirmation, requestDelete ->
                        val label = stringResource(if (confirmation) R.string.record_confirm_delete else R.string.record_delete)
                        Box(Modifier.fillMaxSize().semantics { contentDescription = label }.clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null,
                            role = Role.Button, onClickLabel = label, onClick = requestDelete,
                        ), contentAlignment = Alignment.Center) {
                            DemoRecordDeleteIcon(modifier = Modifier.size(20.dp * (38f / 44f)),
                                confirmation = confirmation, committed = committed, color = MaterialTheme.colorScheme.error)
                        }
                    },
                ) {
                    RecordHistoryContent(record = record)
                }
                if (isLastViewed) DemoRecordLastViewedIndicator(
                    Modifier.align(Alignment.CenterStart).offset(x = (-5).dp))
            }
        }
    }
}

@Composable
private fun RecordHistoryContent(modifier: Modifier = Modifier, record: DemoRecord) {
    val colors = MaterialTheme.colorScheme
    val detailStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp)
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            if (record.hasSegmentFilter) Icon(Icons.Outlined.FilterAlt,
                contentDescription = stringResource(R.string.record_filtered), tint = colors.primary,
                modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.app_name), Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp, lineHeight = 14.sp),
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = colors.onSurface)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(record.startTimeText, Modifier.width(100.dp).align(Alignment.Bottom),
                style = detailStyle, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.weight(1f).padding(start = 9.dp).align(Alignment.Bottom),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.record_fps, record.averageFpsText), style = detailStyle,
                    color = colors.onSurfaceVariant, maxLines = 1)
                Text(stringResource(R.string.record_separator), style = detailStyle, color = colors.outline, maxLines = 1)
                Text(record.averagePowerText, style = detailStyle, color = colors.onSurfaceVariant, maxLines = 1)
            }
            Text(record.durationText, Modifier.width(58.dp).align(Alignment.Bottom), style = detailStyle,
                color = colors.onSurfaceVariant, maxLines = 1, textAlign = TextAlign.End)
        }
        record.remark?.let {
            Text(stringResource(it), style = detailStyle, color = colors.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
