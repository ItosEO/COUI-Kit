package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogMaterial
import com.itos.coui.effect.couiButtonFeedback
import com.itos.coui.icons.CouiIcons
import com.itos.coui.menu.CouiMenuAnchor
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.Menu
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.segmentbutton.SegmentedControl
import com.itos.coui.sheet.BottomSheet
import com.itos.coui.sheet.CouiSheetButton
import com.itos.coui.sheet.CouiSheetDefaults
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text as CouiText
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiContentColor
import java.time.LocalTime
import java.util.Locale

@Composable
internal fun SheetSamples(dark: Boolean) {
    var blurEnabled by rememberSaveable { mutableStateOf(true) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val supportsBlur = CouiDialogDefaults.isMaterialSupported()
    val blurAvailable = supportsBlur && windowBackdrop != null
    val blurSummary = stringResource(when {
        !supportsBlur -> R.string.requires_api_33
        windowBackdrop == null -> R.string.sheet_blur_no_source
        else -> R.string.sheet_blur_summary
    })
    val blurControl: @Composable () -> Unit = {
        DemoSwitchPreference(
            title = stringResource(R.string.sheet_blur_title),
            summary = blurSummary,
            enabled = blurAvailable,
            checked = blurAvailable && blurEnabled,
            onCheckedChange = { blurEnabled = it },
        )
    }
    val material = if (blurAvailable && blurEnabled) CouiSheetDefaults.material(dark) else null

    Group { blurControl() }
    AlarmSheetSample(dark, material)
    HalfSheetSample(
        dark = dark,
        material = material,
        blurAvailable = blurAvailable,
        blurEnabled = blurAvailable && blurEnabled,
        onToggleBlur = { blurEnabled = !blurEnabled },
        blurControl = blurControl,
    )

    // 色块由宿主页面录制，供面板对照模糊效果。
    Group {
        val swatches = if (dark) listOf(Color(0xFF315C99), Color(0xFF91445F), Color(0xFF287461))
            else listOf(Color(0xFF7EB1F4), Color(0xFFF19AB5), Color(0xFF76C6AD))
        Row(Modifier.fillMaxWidth().height(120.dp)) {
            swatches.forEach { color -> Box(Modifier.weight(1f).fillMaxHeight().background(color)) }
        }
    }
}

@Composable
private fun AlarmSheetSample(dark: Boolean, material: CouiDialogMaterial?) {
    val visible = rememberSaveable { mutableStateOf(false) }
    var draftTime by rememberSaveable { mutableIntStateOf(0) }
    var alarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var draftAlarmOptions by rememberSaveable(stateSaver = DemoAlarmOptions.Saver) { mutableStateOf(DemoAlarmOptions()) }
    var savedTime by rememberSaveable { mutableStateOf("") }

    Group(footer = stringResource(R.string.sheet_usage)) {
        SheetEntry(
            title = stringResource(R.string.sheet_alarm),
            dark = dark,
            icon = { Icon(CouiIcons.Filled.Add, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface) },
            isOpen = { visible.value },
            onClick = {
                val now = LocalTime.now()
                draftTime = now.hour * 60 + now.minute
                draftAlarmOptions = alarmOptions
                visible.value = true
            },
        )
    }
    if (savedTime.isNotEmpty()) Group {
        DemoPreference(title = stringResource(R.string.sheet_saved), value = savedTime, showChevron = false)
    }

    BottomSheet(
        visible = visible,
        sheetHeight = LocalConfiguration.current.screenHeightDp.dp,
        colors = CouiDialogDefaults.themeColors(dark).copy(surface = if (dark) Color(0xFF1E1E1E) else Color(0xFFF0F1F2)),
        material = material,
        leadingButton = CouiSheetButton(
            label = stringResource(R.string.dialog_cancel),
            onClick = { visible.value = false },
            enabled = visible.value,
            modifier = Modifier.widthIn(max = 104.dp),
        ),
        titleContent = { CouiText(text = stringResource(R.string.sheet_alarm_editor), kind = CouiTextKind.SheetTitle) },
        trailingButton = CouiSheetButton(
            label = stringResource(R.string.sheet_done),
            onClick = {
                alarmOptions = draftAlarmOptions
                savedTime = String.format(Locale.getDefault(), "%02d:%02d", draftTime / 60, draftTime % 60)
                visible.value = false
            },
            enabled = visible.value,
            modifier = Modifier.widthIn(max = 104.dp),
        ),
    ) {
        DemoClockAlarmContent(dark, draftTime, { draftTime = it }, draftAlarmOptions, { draftAlarmOptions = it })
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun HalfSheetSample(
    dark: Boolean,
    material: CouiDialogMaterial?,
    blurAvailable: Boolean,
    blurEnabled: Boolean,
    onToggleBlur: () -> Unit,
    blurControl: @Composable () -> Unit,
) {
    val visible = rememberSaveable { mutableStateOf(false) }
    val title = stringResource(R.string.sheet_half)
    val menuAnchor = rememberCouiMenuAnchor()
    var menuExpanded by remember { mutableStateOf(false) }

    Group {
        SheetEntry(
            title = title,
            dark = dark,
            icon = null,
            isOpen = { visible.value },
            onClick = {
                menuExpanded = false
                visible.value = true
            },
        )
    }

    BottomSheet(
        visible = visible,
        sheetHeight = LocalConfiguration.current.screenHeightDp.dp / 2,
        showDragHandle = true,
        material = material,
        leadingButton = CouiSheetButton(
            label = stringResource(R.string.sheet_close),
            onClick = { visible.value = false },
            enabled = visible.value,
        ),
        titleContent = { CouiText(text = title, kind = CouiTextKind.SheetTitle) },
        trailingButton = CouiSheetButton(
            onClick = { menuAnchor.open { menuExpanded = true } },
            icon = {
                Icon(CouiIcons.Outlined.More, stringResource(R.string.sheet_more_actions),
                    Modifier.size(24.dp), tint = LocalCouiContentColor.current)
            },
            enabled = visible.value,
            modifier = Modifier.couiMenuAnchor(menuAnchor),
        ),
    ) {
        HalfSheetMoreMenu(
            anchor = menuAnchor,
            dark = dark,
            expanded = menuExpanded && visible.value,
            onDismissRequest = { menuExpanded = false },
            blurAvailable = blurAvailable,
            blurEnabled = blurEnabled,
            onToggleBlur = onToggleBlur,
            onClose = { visible.value = false },
        )
        Group(footer = stringResource(R.string.sheet_half_usage)) {
            blurControl()
            Divider()
            if (Build.VERSION.SDK_INT >= 34) {
                var selected by rememberSaveable { mutableIntStateOf(0) }
                SegmentedControl(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    labels = listOf(
                        stringResource(R.string.segment_all),
                        stringResource(R.string.segment_current_week),
                        stringResource(R.string.segment_not_scheduled),
                    ),
                    selectedIndex = selected,
                    onSelected = { selected = it },
                )
            } else {
                Text(stringResource(R.string.requires_api_34), Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HalfSheetMoreMenu(
    anchor: CouiMenuAnchor,
    dark: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    blurAvailable: Boolean,
    blurEnabled: Boolean,
    onToggleBlur: () -> Unit,
    onClose: () -> Unit,
) {
    var selectedItem by remember { mutableIntStateOf(1) }
    Menu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        anchor = anchor,
        style = demoMenuStyle(dark),
        items = buildList {
            add(CouiMenuItem(onToggleBlur, stringResource(R.string.sheet_blur_title),
                checked = blurEnabled, enabled = blurAvailable))
            for (index in 1..3) {
                add(CouiMenuItem({ selectedItem = index }, stringResource(R.string.menu_item_number, index),
                    selected = selectedItem == index))
            }
            add(CouiMenuItem(onClose, stringResource(R.string.sheet_close), dividerBefore = true))
        },
        onItemClick = {
            onDismissRequest()
            it.value()
        },
    )
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
