package com.itos.coui.demo

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.dialog.InputDialog
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.collect

/** 重命名设备只更新 Demo 中的示例名称，不读取或修改系统设置。 */
@Composable
internal fun RenameDeviceDialogSample(
    dark: Boolean,
    initialName: String,
    material: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
    backdrop: CouiBackdrop? = null,
) {
    val draft = rememberTextFieldState(initialText = initialName, initialSelection = TextRange(initialName.length))
    var edited by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(draft) { snapshotFlow { draft.text.toString() }.drop(1).collect { edited = true } }
    val text = draft.text.toString()
    val name = text.trim { it <= ' ' }
    val canSave = edited && name.isNotEmpty() && name != "null"
    val count = deviceNameByteCount(text)
    val context = LocalContext.current
    val unsupported = stringResource(R.string.dialog_input_unsupported)
    var lastWarning by remember { mutableLongStateOf(-2201L) }
    val transformation = remember(context, unsupported) {
        InputTransformation {
            val blocked = filterDeviceNameEdit(this)
            if (asCharSequence().toString() != originalText.toString()) edited = true
            if (blocked) {
                val now = SystemClock.elapsedRealtime()
                if (now - lastWarning > 2200L) {
                    Toast.makeText(context, unsupported, Toast.LENGTH_SHORT).show()
                    lastWarning = now
                }
            }
        }
    }
    InputDialog(state = draft, inputTransformation = transformation,
        onDismissRequest = onDismissRequest, dark = dark, accent = com.itos.coui.theme.CouiTheme.colorScheme.accent,
        title = stringResource(R.string.dialog_device_name_title),
        message = stringResource(R.string.dialog_device_name_message),
        clearContentDescription = stringResource(R.string.dialog_input_clear),
        counter = stringResource(R.string.dialog_input_counter, count, DEVICE_NAME_MAX_BYTES),
        counterIsError = count > DEVICE_NAME_MAX_BYTES,
        confirmAction = CouiDialogAction(stringResource(R.string.dialog_input_save), { if (canSave) onSave(name) }, enabled = canSave),
        dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), onDismissRequest),
        material = if (material) CouiDialogDefaults.material(dark) else null, backdrop = backdrop)
}

private const val DEVICE_NAME_MAX_BYTES = 32

// Settings 的名称策略按 UTF-16 单元计 1/2/3，而非 String.length；emoji 范围以 smali 常量交叉核对。
private val deviceNameEmoji = Regex(
    "\u2b55|\u2b50|[\uD83C\uDC00-\uD83C\uDFFF]|[\uD83D\uDC00-\uD83D\uDFFF]|[\u2600-\u27ff]|" +
        "[\uD83E\uDD00-\uD83E\uDDFF]|[\u2300-\u23ff]|[\u2500-\u25ff]|[\u2b00-\u2bff]|[\u2d06]|" +
        "[\u3030]|[\u2049]|[\u203c]|[\u2934\ufe0f]|[\u2935]|[\u3299]|[\u303d]|[\u2150-\u21ff]|" +
        "[#*0-9]\ufe0f?\u20e3|[\uD83E\uDE00-\uD83E\uDEFF]", RegexOption.IGNORE_CASE)

private fun deviceNameByteCount(text: String): Int = text.sumOf { if (it.code < 128) 1 else if (it.code < 2048) 2 else 3 }

@OptIn(ExperimentalFoundationApi::class)
private fun filterDeviceNameEdit(buffer: TextFieldBuffer): Boolean {
    val text = buffer.asCharSequence().toString()
    // ChangeList 记录真实替换区间，包括与旧文相同的粘贴内容；先快照，避免改写时追踪表变化。
    val replacements = List(buffer.changes.changeCount) { buffer.changes.getRange(it) }
    var remaining = DEVICE_NAME_MAX_BYTES - deviceNameByteCount(text) +
        replacements.sumOf { deviceNameByteCount(text.substring(it.min, it.max)) }
    var blocked = false
    val retained = replacements.map { range ->
        val inserted = text.substring(range.min, range.max)
        var length = 0
        if (deviceNameEmoji.containsMatchIn(inserted)) blocked = true
        else while (length < inserted.length) {
            val units = Character.charCount(inserted.codePointAt(length))
            val cost = deviceNameByteCount(inserted.substring(length, length + units))
            if (cost > remaining) break
            remaining -= cost
            length += units
        }
        inserted.take(length)
    }
    for (index in replacements.indices.reversed()) {
        val range = replacements[index]
        if (retained[index].length != range.length) buffer.replace(range.min, range.max, retained[index])
    }
    return blocked
}
