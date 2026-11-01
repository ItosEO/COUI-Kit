package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.rememberCouiTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiMetaballRenderer

@Composable
internal fun RecordSwipeSamples(dark: Boolean) {
    var records by rememberSaveable { mutableStateOf(arrayListOf(1, 2, 3)) }
    var openId by rememberSaveable { mutableIntStateOf(0) }
    if (Build.VERSION.SDK_INT < 33) {
        Text(stringResource(R.string.requires_api_33))
        return
    }
    records.forEach { id ->
        key(id) {
            RecordSample(
                id = id,
                isOpen = openId == id,
                onOpenChange = { open ->
                    if (open) openId = id else if (openId == id) openId = 0
                },
                onDelete = { records = records.filterTo(arrayListOf()) { it != id } },
            )
        }
    }
    if (records.isEmpty()) {
        DemoPreference(title = stringResource(R.string.record_restore),
            onClick = { records = arrayListOf(1, 2, 3); openId = 0 })
    }
}

@RequiresApi(33)
@Composable
private fun RecordSample(id: Int, isOpen: Boolean, onOpenChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    // 列表项使用稳定 key, 删除其他项时不能复用其动画或可变 Shader。
    val rendering = remember { runCatching { CouiMetaballRenderer() to CouiMetaballRenderer() } }
    val renderers = rendering.getOrNull()
    if (renderers == null) {
        val failure = rendering.exceptionOrNull()
        Text(stringResource(R.string.renderer_error, failure?.message ?: failure?.javaClass?.simpleName.orEmpty()),
            color = MaterialTheme.colorScheme.error)
        return
    }
    DemoRecordSwipeRow(
        backgroundRenderer = renderers.first,
        maskRenderer = renderers.second,
        isOpen = isOpen,
        onOpenChange = onOpenChange,
        onClick = { onOpenChange(true) },
        onCommitDelete = onDelete,
        endActionContent = { committed, confirmation, requestDelete ->
            val label = stringResource(if (confirmation || committed) R.string.record_confirm_delete else R.string.record_delete)
            Box(Modifier.fillMaxSize().clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = label,
                onClick = requestDelete,
            ), contentAlignment = Alignment.Center) {
                Icon(if (confirmation || committed) DemoIcons.Check else DemoIcons.Delete,
                    contentDescription = label, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        },
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(DemoIcons.Record, contentDescription = null, modifier = Modifier.size(28.dp),
                tint = com.itos.coui.theme.CouiTheme.colorScheme.accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(R.string.record_title, id), style = rememberCouiTextStyle(CouiTextKind.PreferenceTitle),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.record_detail, id, id * 12), style = rememberCouiTextStyle(CouiTextKind.PreferenceSummary),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
