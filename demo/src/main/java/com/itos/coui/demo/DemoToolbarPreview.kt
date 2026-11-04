package com.itos.coui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group

@Composable
internal fun ToolbarPreviewList(query: String) {
    val titles = stringArrayResource(R.array.toolbar_preview_titles)
    val summaries = stringArrayResource(R.array.toolbar_preview_summaries)
    val values = stringArrayResource(R.array.toolbar_preview_values)
    val icons = listOf(CouiIcons.Network, CouiIcons.Sound, CouiIcons.Notifications, CouiIcons.Brightness,
        CouiIcons.Calendar, CouiIcons.Sound, CouiIcons.Personalise, CouiIcons.Components, CouiIcons.Info, CouiIcons.Display)
    val colors = listOf(0xFF1A8CFF, 0xFF6366F1, 0xFFFF9B35, 0xFF34B978, 0xFFF0667B,
        0xFFA884E7, 0xFF45B6AE, 0xFF1A8CFF, 0xFF34B978, 0xFFFF9B35)
    val needle = query.trim()
    val matches = titles.indices.filter { index ->
        titles[index].contains(needle, ignoreCase = true) ||
            summaries[index].contains(needle, ignoreCase = true) ||
            values[index].contains(needle, ignoreCase = true)
    }
    Group {
        if (matches.isEmpty()) {
            DemoPreference(title = stringResource(R.string.toolbar_search_empty))
        } else matches.forEachIndexed { position, index -> key(index) {
            val color = Color(colors[index])
            DemoPreference(Modifier.heightIn(min = if (summaries[index].isEmpty()) 56.dp else 72.dp),
                title = titles[index], summary = summaries[index].takeIf { it.isNotEmpty() },
                value = values[index].takeIf { it.isNotEmpty() },
                icon = {
                    Box(Modifier.size(CouiPageDefaults.iconSize)
                        .background(color.copy(alpha = .16f), if (index % 2 == 0) DemoShapes.capsule else DemoShapes.checkbox),
                        contentAlignment = Alignment.Center) {
                        Icon(icons[index], null, Modifier.size(18.dp), tint = color)
                    }
                })
            if (position < matches.lastIndex) Divider(startIndent =
                CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing)
        } }
    }
}
