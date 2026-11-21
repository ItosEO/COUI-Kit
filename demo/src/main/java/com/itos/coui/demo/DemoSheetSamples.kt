package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.icons.CouiIcons
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.scroll.couiVerticalScroll
import com.itos.coui.sheet.ModalBottomSheet
import com.itos.coui.sheet.CouiSheetDefaults
import com.itos.coui.sheet.SheetToolbar
import com.itos.coui.theme.CouiTheme
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.TopAppBarButton
import java.time.LocalTime
import java.util.Locale

private const val AlarmSheet = 0
private const val TimerSheet = 1
private const val HalfSheet = 2

@Composable
internal fun SheetSamples(dark: Boolean) {
    var panel by rememberSaveable { mutableIntStateOf(-1) }
    var retainedPanel by rememberSaveable { mutableIntStateOf(AlarmSheet) }
    var duration by rememberSaveable { mutableIntStateOf(5 * 60) }
    var draftTime by rememberSaveable { mutableIntStateOf(0) }
    var alarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var draftAlarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var timerOptions by rememberSaveable(stateSaver = DemoTimerOptions.Saver) { mutableStateOf(DemoTimerOptions()) }
    var draftTimerOptions by rememberSaveable(stateSaver = DemoTimerOptions.Saver) { mutableStateOf(DemoTimerOptions()) }
    var saved by rememberSaveable { mutableStateOf("") }
    var blurEnabled by rememberSaveable { mutableStateOf(true) }
    var halfSheetMenuExpanded by remember { mutableStateOf(false) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val blurAvailable = Build.VERSION.SDK_INT >= 33 && windowBackdrop != null
    val blurTitle = stringResource(R.string.sheet_blur_title)
    val blurSummary = stringResource(when {
        Build.VERSION.SDK_INT < 33 -> R.string.requires_api_33
        windowBackdrop == null -> R.string.sheet_blur_no_source
        else -> R.string.sheet_blur_summary
    })
    val blurControl: @Composable () -> Unit = {
        DemoSwitchPreference(title = blurTitle, summary = blurSummary, enabled = blurAvailable,
            checked = blurAvailable && blurEnabled, onCheckedChange = { blurEnabled = it })
    }
    val labels = listOf(
        stringResource(R.string.sheet_alarm),
        stringResource(R.string.sheet_timer),
        stringResource(R.string.sheet_half),
    )
    val open: (Int) -> Unit = { index ->
        when (index) {
            AlarmSheet -> {
                val now = LocalTime.now()
                draftTime = now.hour * 60 + now.minute
                draftAlarmOptions = alarmOptions
            }
            TimerSheet -> {
                draftTime = duration
                draftTimerOptions = timerOptions
            }
        }
        retainedPanel = index
        panel = index
    }
    Group { blurControl() }
    val addIcon: @Composable () -> Unit = {
        Icon(CouiIcons.Filled.Add, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
    }
    Group(footer = stringResource(R.string.sheet_usage)) {
        labels.forEachIndexed { index, label ->
            SheetEntry(
                title = label,
                dark = dark,
                icon = if (index == HalfSheet) null else addIcon,
                isOpen = { panel == index },
                onClick = { open(index) },
            )
            if (index < labels.lastIndex) Divider()
        }
    }
    // 色块属于宿主页面；现有窗口源直接录入，面板内容不参与自己的采样。
    Group {
        val swatches = if (dark) listOf(Color(0xFF315C99), Color(0xFF91445F), Color(0xFF287461))
            else listOf(Color(0xFF7EB1F4), Color(0xFFF19AB5), Color(0xFF76C6AD))
        Row(Modifier.fillMaxWidth().height(120.dp)) {
            swatches.forEach { color -> Box(Modifier.weight(1f).fillMaxHeight().background(color))
            }
        }
    }
    if (saved.isNotEmpty()) Group {
        DemoPreference(title = stringResource(R.string.sheet_saved), value = saved, showChevron = false)
    }
    val halfScreen = retainedPanel == HalfSheet
    val sheetColors = CouiDialogDefaults.themeColors(dark)
    // 两个 Clock 编辑面板以 elevatedWithCard 作为不透明回退底色。
    val colors = if (halfScreen) sheetColors else sheetColors.copy(
        surface = if (dark) Color(0xFF1E1E1E) else Color(0xFFF0F1F2),
    )
    val title = if (retainedPanel == AlarmSheet) stringResource(R.string.sheet_alarm_editor)
        else labels.getOrNull(retainedPanel).orEmpty()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val opened = panel in labels.indices
    val dismiss: () -> Unit = {
        halfSheetMenuExpanded = false
        panel = -1
    }
    val saveDraft: () -> Unit = {
        if (retainedPanel == AlarmSheet) {
            alarmOptions = draftAlarmOptions
            saved = String.format(Locale.getDefault(), "%02d:%02d", draftTime / 60, draftTime % 60)
        } else {
            duration = draftTime
            timerOptions = draftTimerOptions
            saved = String.format(Locale.getDefault(), "%02d:%02d:%02d", duration / 3600, duration / 60 % 60, duration % 60)
        }
        dismiss()
    }
    ModalBottomSheet(visible = opened,
        onDismissRequest = dismiss, title = title,
        colors = colors,
        sheetHeight = if (halfScreen) screenHeight / 2 else screenHeight,
        showDragHandle = halfScreen,
        nestedScrollEnabled = false,
        material = if (blurAvailable && blurEnabled) CouiSheetDefaults.material(dark) else null) {
        SheetToolbar(title = title, navigation = { style ->
            SheetEditorAction(stringResource(if (halfScreen) R.string.sheet_close else R.string.dialog_cancel),
                onClick = dismiss, enabled = opened, style = style)
        }, actions = { style ->
            if (halfScreen) {
                HalfSheetMoreMenu(
                    style = style,
                    enabled = opened,
                    expanded = halfSheetMenuExpanded,
                    onExpandedChange = { halfSheetMenuExpanded = it },
                    blurAvailable = blurAvailable,
                    blurEnabled = blurAvailable && blurEnabled,
                    onToggleBlur = { blurEnabled = !blurEnabled },
                    onClose = dismiss,
                )
            } else {
                SheetEditorAction(stringResource(R.string.sheet_done), onClick = saveDraft,
                    enabled = opened && (retainedPanel != TimerSheet || draftTime > 0), style = style)
            }
        })
        if (halfScreen) {
            Group(modifier = Modifier.couiVerticalScroll(rememberScrollState()),
                footer = stringResource(R.string.sheet_half_usage)) { blurControl() }
        } else {
            // 长表单的滚动范围由正文决定，视口单独负责裁剪上拉回弹。
            Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                Column(Modifier.fillMaxWidth().couiVerticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                    if (retainedPanel == AlarmSheet) {
                        DemoClockAlarmContent(dark, draftTime, { draftTime = it },
                            draftAlarmOptions, { draftAlarmOptions = it })
                    } else {
                        DemoClockTimerContent(dark, draftTime, { draftTime = it },
                            draftTimerOptions, { draftTimerOptions = it })
                    }
                }
            }
        }
    }
}

@Composable
private fun HalfSheetMoreMenu(
    style: CouiToolbarStyle,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    blurAvailable: Boolean,
    blurEnabled: Boolean,
    onToggleBlur: () -> Unit,
    onClose: () -> Unit,
) {
    val anchor = rememberCouiMenuAnchor()
    var selectedItem by remember { mutableIntStateOf(1) }
    val anchorModifier = Modifier.couiMenuAnchor(anchor)
    val openMenu = { anchor.open { onExpandedChange(true) } }
    val moreIcon: @Composable () -> Unit = {
        Icon(CouiIcons.Outlined.More, stringResource(R.string.sheet_more_actions),
            Modifier.size(24.dp), tint = style.contentColor)
    }
    Box {
        if (Build.VERSION.SDK_INT >= 34) {
            TopAppBarButton(anchorModifier, onClick = openMenu, enabled = enabled, style = style) { moreIcon() }
        } else {
            IconButton(onClick = openMenu, modifier = anchorModifier, enabled = enabled) { moreIcon() }
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            anchor = anchor,
            style = demoMenuStyle(style.dark),
            items = buildList {
                add(CouiMenuItem(onToggleBlur, stringResource(R.string.sheet_blur_title),
                    checked = blurEnabled, enabled = blurAvailable))
                for (index in 1..3) {
                    add(CouiMenuItem({ selectedItem = index }, stringResource(R.string.menu_item_number, index),
                        selected = selectedItem == index))
                }
                add(CouiMenuItem(onClose, stringResource(R.string.sheet_close), dividerBefore = true))
            },
            onItemClick = { item ->
                onExpandedChange(false)
                item.value()
            },
        )
    }
}

@Composable
private fun SheetEntry(
    title: String,
    dark: Boolean,
    icon: (@Composable () -> Unit)?,
    isOpen: () -> Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val opened by remember(isOpen) { derivedStateOf(structuralEqualityPolicy(), isOpen) }
    // 确认打开后结束入口行的按压蒙层，避免退场弹簧持续驱动宿主绘制。
    DemoPreference(
        modifier = Modifier
            .couiButtonFeedback(dark, interaction, pressFeedback = !opened, list = true)
            .clickable(interaction, indication = null, role = Role.Button, onClick = onClick),
        title = title,
        icon = icon,
        showChevron = true,
    )
}

@Composable
private fun SheetEditorAction(text: String, onClick: () -> Unit, enabled: Boolean, style: CouiToolbarStyle) {
    if (Build.VERSION.SDK_INT >= 34) {
        TopAppBarButton(Modifier.widthIn(max = 104.dp), onClick = onClick, enabled = enabled, text = true, style = style) {
            Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    } else {
        TextButton(onClick = onClick, enabled = enabled) { Text(text) }
    }
}

/** 编辑页共用卡片外形，顶部间距由各内容区的 Modifier 提供。 */
@Composable
internal fun DemoSheetCard(
    modifier: Modifier = Modifier,
    dark: Boolean = CouiTheme.isDark,
    shape: Shape = CouiPageDefaults.shape,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin)
        .clip(shape).background(CouiPageDefaults.themeColors(dark).card), content = content)
}
