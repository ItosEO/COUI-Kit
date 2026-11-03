package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogPlacement
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.switch.CouiSwitch
import com.itos.coui.switch.CouiSwitchDefaults
import com.itos.coui.menu.rememberCouiWindowBackdrop
import com.itos.coui.layout.*

@Composable
internal fun DialogSample(dark: Boolean, onToggleTheme: (() -> Unit)? = null) {
    var opened by rememberSaveable { mutableStateOf<String?>(null) }
    var result by rememberSaveable { mutableStateOf(false) }
    var material by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= 33) }
    val initialDeviceName = stringResource(R.string.dialog_device_name_initial)
    var deviceName by rememberSaveable { mutableStateOf(initialDeviceName) }
    val windowBackdrop = rememberCouiWindowBackdrop()
    val supported = Build.VERSION.SDK_INT >= 33 && windowBackdrop != null
    val variants = listOf("primary" to R.string.dialog_primary, "bottom" to R.string.dialog_bottom, "center" to R.string.dialog_center,
        "long" to R.string.dialog_long, "three" to R.string.dialog_three, "disabled" to R.string.dialog_disabled,
        "input" to R.string.dialog_input) +
        if (onToggleTheme != null) listOf("theme" to R.string.dialog_theme_live) else emptyList()
    if (supported) Group {
        DemoSwitchPreference(title = stringResource(R.string.dialog_material), checked = material, onCheckedChange = { material = it })
    } else Footer(text = stringResource(R.string.requires_api_33))
    Group {
        variants.forEachIndexed { index, (key, label) ->
            DemoPreference(title = stringResource(label), onClick = { opened = key })
            if (index < variants.lastIndex) Divider()
        }
    }
    Footer(text = stringResource(if (result) R.string.dialog_applied else R.string.dialog_not_applied))
    Footer(text = stringResource(R.string.dialog_device_name_value, deviceName))
    if (opened != null) {
        val variant = opened
        val dismiss = { opened = null }
        val regularMessage = stringResource(R.string.dialog_sample_message)
        val title = stringResource(R.string.dialog_sample_title)
        if (variant == "input") RenameDeviceDialogSample(dark = dark, initialName = deviceName,
            material = material && supported, backdrop = windowBackdrop, onDismissRequest = dismiss,
            onSave = { deviceName = it; result = true; opened = null })
        else CouiAlertDialog(onDismissRequest = dismiss, dark = dark,
            title = title,
            message = if (variant == "long") List(12) { regularMessage }.joinToString("\n\n") else regularMessage,
            confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), { result = true; opened = null }, variant != "disabled",
                style = if (variant == "primary") CouiDialogActionStyle.Primary else CouiDialogActionStyle.Secondary,
                colors = if (variant == "primary") CouiDialogDefaults.primaryActionColors(com.itos.coui.theme.CouiTheme.colorScheme.accent) else null),
            dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), dismiss),
            neutralAction = when {
                variant == "theme" && onToggleTheme != null -> CouiDialogAction(stringResource(R.string.toggle_theme), onToggleTheme)
                variant == "three" -> CouiDialogAction(stringResource(R.string.dialog_reset), { result = false; opened = null })
                else -> null
            },
            placement = if (variant == "center") CouiDialogPlacement.Center else CouiDialogPlacement.Bottom,
            properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
            material = if (material && supported) CouiDialogDefaults.material(dark) else null,
            backdrop = windowBackdrop)
    }
}
