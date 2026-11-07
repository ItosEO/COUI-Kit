package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.segmentbutton.SegmentedControl

@Composable
internal fun SegmentedControlSample(dark: Boolean) {
    if (Build.VERSION.SDK_INT >= 34) SegmentedControlContent(dark)
    else Text(stringResource(R.string.requires_api_34), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@RequiresApi(34)
@Composable
private fun SegmentedControlContent(dark: Boolean) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var unequal by rememberSaveable { mutableIntStateOf(0) }
    var small by rememberSaveable { mutableStateOf(false) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var onRelease by rememberSaveable { mutableStateOf(false) }
    val backdrop = rememberCouiBackdrop()
    val labels = listOf(stringResource(R.string.segment_day), stringResource(R.string.segment_week), stringResource(R.string.segment_month))
    val unequalLabels = listOf(stringResource(R.string.segment_all), stringResource(R.string.segment_current_week), stringResource(R.string.segment_not_scheduled))
    Group(footer = stringResource(R.string.segment_usage)) {
        Box(Modifier.fillMaxWidth().height(176.dp)) {
            Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(Brush.linearGradient(
                if (dark) listOf(Color(0xFF152D40), Color(0xFF49313D), Color(0xFF343B2B))
                else listOf(Color(0xFFD7EDF9), Color(0xFFF9DFDC), Color(0xFFEAF1D2)))))
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SegmentedControl(Modifier.fillMaxWidth(), labels, selected, { selected = it }, backdrop,
                    dark, enabled, small, onRelease)
                SegmentedControl(Modifier.fillMaxWidth(), unequalLabels, unequal, { unequal = it }, backdrop,
                    dark, enabled, small, onRelease)
            }
        }
    }
    Group {
        DemoSwitchPreference(title = stringResource(R.string.segment_enabled), checked = enabled, onCheckedChange = { enabled = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.segment_small), checked = small, onCheckedChange = { small = it })
        Divider()
        DemoSwitchPreference(title = stringResource(R.string.segment_commit_on_release), checked = onRelease, onCheckedChange = { onRelease = it })
    }
}
