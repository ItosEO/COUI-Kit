package com.itos.coui.demo

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.itos.coui.dialog.Dialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogPlacement
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.LoadingDialog
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.*
import kotlinx.coroutines.delay

@Composable
internal fun DialogSample(dark: Boolean, onToggleTheme: (() -> Unit)? = null) {
    var opened by rememberSaveable { mutableStateOf<String?>(null) }
    var visible by rememberSaveable { mutableStateOf(false) }
    var session by rememberSaveable { mutableStateOf(0) }
    var material by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= 33) }
    val initialDeviceName = stringResource(R.string.dialog_device_name_initial)
    var deviceName by rememberSaveable { mutableStateOf(initialDeviceName) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val supported = Build.VERSION.SDK_INT >= 33 && windowBackdrop != null
    val variants = listOf("primary" to R.string.dialog_primary, "bottom" to R.string.dialog_bottom, "center" to R.string.dialog_center,
        "long" to R.string.dialog_long, "three" to R.string.dialog_three, "disabled" to R.string.dialog_disabled,
        "input" to R.string.dialog_input, "loading" to R.string.dialog_loading) +
        if (onToggleTheme != null) listOf("theme" to R.string.dialog_theme_live) else emptyList()
    if (supported) Group {
        DemoSwitchPreference(title = stringResource(R.string.dialog_material), checked = material, onCheckedChange = { material = it })
    }
    Group {
        variants.forEachIndexed { index, (key, label) ->
            DemoPreference(title = stringResource(label), onClick = { opened = key; session++; visible = true })
            if (index < variants.lastIndex) Divider()
        }
    }
    if (opened != null) {
        val variant = opened
        val dismiss = { visible = false }
        val regularMessage = stringResource(R.string.dialog_sample_message)
        val title = stringResource(R.string.dialog_sample_title)
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
                material = if (material && supported) CouiDialogDefaults.material(dark) else null,
                backdrop = windowBackdrop,
            )
        } else if (variant == "input") androidx.compose.runtime.key(session) {
            RenameDeviceDialogSample(dark = dark, initialName = deviceName, visible = visible,
            material = material && supported, backdrop = windowBackdrop, onDismissRequest = dismiss,
            onSave = { deviceName = it; visible = false })
        } else Dialog(onDismissRequest = dismiss, dark = dark, visible = visible,
            title = title,
            message = if (variant == "long") List(12) { regularMessage }.joinToString("\n\n") else regularMessage,
            confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), dismiss, variant != "disabled",
                style = if (variant == "primary") CouiDialogActionStyle.Primary else CouiDialogActionStyle.Secondary,
                colors = if (variant == "primary") CouiDialogDefaults.primaryActionColors(com.itos.coui.theme.CouiTheme.colorScheme.accent) else null),
            dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), dismiss),
            neutralAction = when {
                variant == "theme" && onToggleTheme != null -> CouiDialogAction(stringResource(R.string.toggle_theme), onToggleTheme)
                variant == "three" -> CouiDialogAction(stringResource(R.string.dialog_later), dismiss)
                else -> null
            },
            placement = if (variant == "center") CouiDialogPlacement.Center else CouiDialogPlacement.Bottom,
            properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
            material = if (material && supported) CouiDialogDefaults.material(dark) else null,
            backdrop = windowBackdrop)
    }
}
