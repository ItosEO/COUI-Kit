package com.itos.coui.demo

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogPlacement

@Composable
internal fun DialogSample(dark: Boolean) {
    var opened by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    var material by rememberSaveable { mutableStateOf(true) }
    val materialSupported = CouiDialogDefaults.isMaterialSupported()
    if (materialSupported) DemoSwitchRow(stringResource(R.string.dialog_material), material, { material = it })
    else Text(stringResource(R.string.dialog_material_unavailable), color = MaterialTheme.colorScheme.onSurfaceVariant)
    val examples = listOf(
        "bottom" to R.string.dialog_bottom, "center" to R.string.dialog_center,
        "long" to R.string.dialog_long, "three" to R.string.dialog_three,
        "disabled" to R.string.dialog_disabled, "primary" to R.string.dialog_primary,
    )
    examples.forEach { (key, label) ->
        FilledTonalButton(onClick = { opened = key }, shape = DemoShapes.capsule) {
            Text(stringResource(label))
        }
    }
    Text(stringResource(if (confirmed) R.string.dialog_applied else R.string.dialog_not_applied))
    val example = opened
    if (example != null) {
        val close = { opened = null }
        val title = stringResource(R.string.dialog_sample_title)
        val message = stringResource(R.string.dialog_sample_message)
        CouiAlertDialog(onDismissRequest = close, dark = dark, title = title,
            message = if (example == "long") List(12) { message }.joinToString("\n\n") else message,
            confirmAction = CouiDialogAction(stringResource(R.string.dialog_confirm), { confirmed = true; close() },
                enabled = example != "disabled",
                style = if (example == "primary") CouiDialogActionStyle.Primary else CouiDialogActionStyle.Secondary,
                colors = if (example == "primary") CouiDialogDefaults.primaryActionColors(MaterialTheme.colorScheme.primary) else null),
            dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), close),
            neutralAction = if (example == "three")
                CouiDialogAction(stringResource(R.string.dialog_reset), { confirmed = false; close() }) else null,
            placement = if (example == "center") CouiDialogPlacement.Center else CouiDialogPlacement.Bottom,
            properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
            material = if (material && materialSupported) CouiDialogDefaults.material(dark) else null)
    }
}
