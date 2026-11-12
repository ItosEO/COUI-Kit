package com.itos.coui.demo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.layout.CouiPageDefaults
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
    var gradientSelected by rememberSaveable { mutableIntStateOf(0) }
    var groupSelected by rememberSaveable { mutableIntStateOf(0) }
    var pageSelected by rememberSaveable { mutableIntStateOf(0) }
    var small by rememberSaveable { mutableStateOf(false) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var onRelease by rememberSaveable { mutableStateOf(false) }
    val labels = listOf(stringResource(R.string.segment_all), stringResource(R.string.segment_current_week),
        stringResource(R.string.segment_not_scheduled))
    val colors = CouiPageDefaults.themeColors(dark)
    val gradientBackdrop = rememberCouiBackdrop()
    val pageBackdrop = rememberCouiBackdrop()
    val gradientRendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val pageRendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val gradientRenderer = gradientRendering.getOrNull()
    val pageRenderer = pageRendering.getOrNull()
    Group {
        Box(Modifier.fillMaxWidth().height(128.dp)) {
            ChromePreviewGradient(dark, gradientBackdrop)
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (gradientRenderer != null) {
                    SegmentedControl(
                        modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth(),
                        labels = labels,
                        selectedIndex = gradientSelected,
                        onSelected = { gradientSelected = it },
                        backdrop = gradientBackdrop,
                        dark = dark,
                        enabled = enabled,
                        small = small,
                        commitOnRelease = onRelease,
                        renderer = gradientRenderer,
                    )
                    Text(stringResource(R.string.segment_selected, labels[gradientSelected]),
                        color = colors.summary, style = MaterialTheme.typography.bodySmall)
                } else {
                    val failure = checkNotNull(gradientRendering.exceptionOrNull())
                    Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                        color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
    Group {
        Box(Modifier.fillMaxWidth().height(128.dp), contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                SegmentedControl(
                    modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth(),
                    labels = labels,
                    selectedIndex = groupSelected,
                    onSelected = { groupSelected = it },
                    dark = dark,
                    enabled = enabled,
                    small = small,
                    commitOnRelease = onRelease,
                )
                Text(stringResource(R.string.segment_selected, labels[groupSelected]),
                    color = colors.summary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    Group(colors = colors.copy(card = colors.background)) {
        Box(Modifier.fillMaxWidth().height(128.dp)) {
            Box(Modifier.matchParentSize().couiBackdrop(pageBackdrop).background(colors.background))
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (pageRenderer != null) {
                    SegmentedControl(
                        modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth(),
                        labels = labels,
                        selectedIndex = pageSelected,
                        onSelected = { pageSelected = it },
                        backdrop = pageBackdrop,
                        dark = dark,
                        enabled = enabled,
                        small = small,
                        commitOnRelease = onRelease,
                        renderer = pageRenderer,
                    )
                    Text(stringResource(R.string.segment_selected, labels[pageSelected]),
                        color = colors.summary, style = MaterialTheme.typography.bodySmall)
                } else {
                    val failure = checkNotNull(pageRendering.exceptionOrNull())
                    Text(stringResource(R.string.renderer_error, failure.message ?: failure.javaClass.simpleName),
                        color = MaterialTheme.colorScheme.error)
                }
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
