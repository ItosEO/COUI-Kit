package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TextButton
import com.itos.coui.icons.CouiIcons
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.sheet.ModalBottomSheet
import com.itos.coui.sheet.CouiSheetDefaults
import com.itos.coui.sheet.SheetToolbar
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle
import com.itos.coui.topappbar.TopAppBarButton
import java.time.LocalTime
import java.util.Locale

/** 同一 Sheet 外壳承载两个编辑示例；内容 slot 保持通用，不调用系统提醒。 */
@Composable
internal fun SheetSamples(dark: Boolean) {
    var panel by rememberSaveable { mutableIntStateOf(-1) }
    var retainedPanel by rememberSaveable { mutableIntStateOf(0) }
    var duration by rememberSaveable { mutableIntStateOf(5 * 60) }
    var draftTime by rememberSaveable { mutableIntStateOf(0) }
    var alarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var draftAlarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var timerOptions by rememberSaveable(stateSaver = DemoTimerOptions.Saver) { mutableStateOf(DemoTimerOptions()) }
    var draftTimerOptions by rememberSaveable(stateSaver = DemoTimerOptions.Saver) { mutableStateOf(DemoTimerOptions()) }
    var saved by rememberSaveable { mutableStateOf("") }
    val labels = listOf(stringResource(R.string.sheet_alarm), stringResource(R.string.sheet_timer))
    val open: (Int) -> Unit = { index ->
        retainedPanel = index
        val now = LocalTime.now()
        draftTime = if (index == 1) duration else now.hour * 60 + now.minute
        draftAlarmOptions = alarmOptions; draftTimerOptions = timerOptions
        panel = index
    }
    Group(footer = stringResource(R.string.sheet_usage)) {
        labels.forEachIndexed { index, label ->
            DemoPreference(title = label, onClick = { open(index) }, icon = {
                androidx.compose.material3.Icon(CouiIcons.Monochrome.Add, null, Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface)
            })
            if (index < labels.lastIndex) Divider()
        }
    }
    if (saved.isNotEmpty()) Group {
        DemoPreference(title = stringResource(R.string.sheet_saved), value = saved, showChevron = false)
    }
    val backdrop = rememberCouiWindowBackdrop()
    val title = if (retainedPanel == 1) labels[1] else stringResource(R.string.sheet_alarm_editor)
    val panelHeight = LocalConfiguration.current.screenHeightDp.dp
    val materialEnabled = Build.VERSION.SDK_INT >= 33 && backdrop != null
    val sheetColors = CouiSheetDefaults.colors(dark).copy(surface = Color(if (dark) 0xFF1E1E1E else 0xFFF0F1F2))
    val dismiss: () -> Unit = { panel = -1 }
    val saveDraft: () -> Unit = {
        if (retainedPanel == 0) {
            alarmOptions = draftAlarmOptions
            saved = String.format(Locale.getDefault(), "%02d:%02d", draftTime / 60, draftTime % 60)
        } else {
            duration = draftTime; timerOptions = draftTimerOptions
            saved = String.format(Locale.getDefault(), "%02d:%02d:%02d", duration / 3600, duration / 60 % 60, duration % 60)
        }
        panel = -1
    }
    // 持续装配，让退场完成后再移除窗口；内容页的导航由宿主决定。
    ModalBottomSheet(visible = panel in labels.indices && retainedPanel in labels.indices,
        onDismissRequest = dismiss, title = title,
        dismissDescription = stringResource(R.string.dialog_cancel), dark = dark,
        sheetHeight = panelHeight, showDragHandle = false, colors = sheetColors, isolateContent = false,
        material = if (materialEnabled) CouiSheetDefaults.material(dark) else null, backdrop = backdrop) {
        val canSave = panel in labels.indices && (retainedPanel != 1 || draftTime > 0)
        if (Build.VERSION.SDK_INT >= 34) {
            SheetToolbar(title = title, dark = dark, navigation = { style ->
                TopAppBarButton(Modifier.widthIn(max = 104.dp), onClick = { panel = -1 }, enabled = panel >= 0, text = true, style = style) {
                    Text(stringResource(R.string.dialog_cancel), color = LocalCouiContentColor.current,
                        style = LocalCouiTextStyle.current, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }, actions = { style ->
                TopAppBarButton(Modifier.widthIn(max = 104.dp), onClick = saveDraft, enabled = canSave, text = true, style = style) {
                    Text(stringResource(R.string.sheet_done), color = LocalCouiContentColor.current,
                        style = LocalCouiTextStyle.current, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            })
        } else Box(Modifier.fillMaxWidth()) {
            Text(title, Modifier.align(Alignment.Center).padding(horizontal = 88.dp),
                style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { panel = -1 }, enabled = panel >= 0) {
                    Text(stringResource(R.string.dialog_cancel))
                }
                TextButton(onClick = saveDraft, enabled = canSave) {
                    Text(stringResource(R.string.sheet_done))
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
            if (retainedPanel == 0) DemoClockAlarmContent(dark, draftTime, { draftTime = it },
                draftAlarmOptions, { draftAlarmOptions = it })
            else DemoClockTimerContent(dark, draftTime, { draftTime = it },
                draftTimerOptions, { draftTimerOptions = it })
        }
    }
}
