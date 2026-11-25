package com.itos.coui.demo

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.itos.coui.dialog.Dialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogPlacement
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiSingleChoiceItem
import com.itos.coui.dialog.LoadingDialog
import com.itos.coui.dialog.SingleChoiceDialog
import com.itos.coui.icons.CouiIcons
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.*
import kotlinx.coroutines.delay

@Composable
internal fun DialogSample(dark: Boolean, onToggleTheme: (() -> Unit)? = null) {
    var opened by rememberSaveable { mutableStateOf<String?>(null) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var session by rememberSaveable { mutableStateOf(0) }
    var materialEnabled by rememberSaveable { mutableStateOf(true) }
    val initialDeviceName = stringResource(R.string.dialog_device_name_initial)
    var deviceName by rememberSaveable { mutableStateOf(initialDeviceName) }
    var usbMode by rememberSaveable { mutableIntStateOf(R.string.dialog_usb_charge) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val material = demoDialogMaterial(windowBackdrop)
    val variants = listOf("primary" to R.string.dialog_primary, "bottom" to R.string.dialog_bottom, "center" to R.string.dialog_center,
        "long" to R.string.dialog_long, "three" to R.string.dialog_three, "actionRows" to R.string.dialog_action_rows,
        "disabled" to R.string.dialog_disabled,
        "input" to R.string.dialog_input, "singleChoice" to R.string.dialog_single_choice,
        "loading" to R.string.dialog_loading) +
        if (onToggleTheme != null) listOf("theme" to R.string.dialog_theme_live) else emptyList()
    if (material != null) Group {
        DemoSwitchPreference(title = stringResource(R.string.dialog_material), checked = materialEnabled, onCheckedChange = { materialEnabled = it })
    }
    Group {
        variants.forEachIndexed { index, (key, label) ->
            DemoPreference(
                title = stringResource(label),
                summary = if (key == "singleChoice") stringResource(usbMode) else null,
                onClick = { opened = key; session++; visible = true },
            )
            if (index < variants.lastIndex) Divider()
        }
    }
    if (opened != null) {
        val variant = opened
        val dismiss = { visible = false }
        val regularMessage = stringResource(R.string.dialog_sample_message)
        val title = stringResource(R.string.dialog_sample_title)
        val dialogMaterial = if (materialEnabled) material else null
        if (variant == "loading") {
            LaunchedEffect(visible, session) {
                if (visible) {
                    delay(2500)
                    visible = false
                }
            }
            LoadingDialog(
                visible = visible,
                message = stringResource(R.string.dialog_loading_message),
                onDismissRequest = dismiss,
                dark = dark,
                material = dialogMaterial,
                backdrop = windowBackdrop,
            )
        } else if (variant == "input") androidx.compose.runtime.key(session) {
            RenameDeviceDialogSample(dark = dark, initialName = deviceName, visible = visible,
                material = dialogMaterial, backdrop = windowBackdrop, onDismissRequest = dismiss,
                onSave = { deviceName = it; visible = false })
        } else if (variant == "singleChoice") {
            SingleChoiceDialog(
                visible = visible,
                title = stringResource(R.string.dialog_usb_title),
                items = listOf(
                    CouiSingleChoiceItem(R.string.dialog_usb_charge, stringResource(R.string.dialog_usb_charge),
                        icon = { Icon(CouiIcons.Filled.Battery, contentDescription = null) }),
                    CouiSingleChoiceItem(R.string.dialog_usb_files, stringResource(R.string.dialog_usb_files),
                        icon = { Icon(CouiIcons.Filled.Transfer, contentDescription = null) }),
                    CouiSingleChoiceItem(R.string.dialog_usb_photos, stringResource(R.string.dialog_usb_photos),
                        icon = { Icon(CouiIcons.Outlined.Photo, contentDescription = null) }),
                    CouiSingleChoiceItem(R.string.dialog_usb_tethering, stringResource(R.string.dialog_usb_tethering),
                        icon = { Icon(CouiIcons.Filled.Wifi, contentDescription = null) }),
                    CouiSingleChoiceItem(R.string.dialog_usb_midi, stringResource(R.string.dialog_usb_midi),
                        icon = { Icon(CouiIcons.Filled.Volume, contentDescription = null) }),
                ),
                selectedValue = usbMode,
                onItemSelected = { mode ->
                    usbMode = mode
                    visible = false
                },
                onDismissRequest = dismiss,
                actionRows = listOf(listOf(CouiDialogAction(stringResource(R.string.dialog_cancel), dismiss))),
                dark = dark,
                material = dialogMaterial,
                backdrop = windowBackdrop,
            )
        } else if (variant == "actionRows") {
            Dialog(
                onDismissRequest = dismiss,
                actionRows = listOf(
                    listOf(CouiDialogAction(
                        text = stringResource(R.string.dialog_confirm),
                        onClick = dismiss,
                        style = CouiDialogActionStyle.Primary,
                    )),
                    listOf(
                        CouiDialogAction(stringResource(R.string.dialog_cancel), dismiss),
                        CouiDialogAction(stringResource(R.string.dialog_later), dismiss),
                    ),
                ),
                dark = dark,
                title = title,
                message = regularMessage,
                properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
                visible = visible,
                material = dialogMaterial,
                backdrop = windowBackdrop,
            )
        } else Dialog(onDismissRequest = dismiss, dark = dark, visible = visible,
            title = title,
            message = if (variant == "long") List(12) { regularMessage }.joinToString("\n\n") else regularMessage,
            confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), dismiss, variant != "disabled",
                style = if (variant == "primary") CouiDialogActionStyle.Primary else CouiDialogActionStyle.Secondary),
            dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), dismiss),
            neutralAction = when {
                variant == "theme" && onToggleTheme != null -> CouiDialogAction(stringResource(R.string.toggle_theme), onToggleTheme)
                variant == "three" -> CouiDialogAction(stringResource(R.string.dialog_later), dismiss)
                else -> null
            },
            placement = if (variant == "center") CouiDialogPlacement.Center else CouiDialogPlacement.Bottom,
            properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
            material = dialogMaterial,
            backdrop = windowBackdrop)
    }
}
