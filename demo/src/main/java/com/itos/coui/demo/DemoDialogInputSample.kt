package com.itos.coui.demo

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiInputDialog

/** 重命名设备只更新 Demo 中的示例名称，不读取或修改系统设置。 */
@Composable
internal fun RenameDeviceDialogSample(
    dark: Boolean,
    initialName: String,
    material: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
) {
    var draft by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialName, TextRange(initialName.length)))
    }
    var edited by rememberSaveable { mutableStateOf(false) }
    val name = draft.text.trim { it <= ' ' }
    val canSave = edited && name.isNotEmpty() && name != "null"
    val count = deviceNameByteCount(draft.text)
    val context = LocalContext.current
    val unsupported = stringResource(R.string.dialog_input_unsupported)
    var lastWarning by remember { mutableLongStateOf(-2201L) }
    CouiInputDialog(value = draft, onValueChange = { proposed ->
        val filtered = filterDeviceNameEdit(draft, proposed)
        if (proposed.text != draft.text && deviceNameEmoji.containsMatchIn(proposed.text)) {
            val now = SystemClock.elapsedRealtime()
            if (now - lastWarning > 2200L) {
                Toast.makeText(context, unsupported, Toast.LENGTH_SHORT).show()
                lastWarning = now
            }
        }
        if (filtered.text != draft.text) edited = true
        draft = filtered
    }, onDismissRequest = onDismissRequest, dark = dark, accent = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.dialog_device_name_title),
        message = stringResource(R.string.dialog_device_name_message),
        clearContentDescription = stringResource(R.string.dialog_input_clear),
        counter = stringResource(R.string.dialog_input_counter, count, DEVICE_NAME_MAX_BYTES),
        counterIsError = count >= DEVICE_NAME_MAX_BYTES,
        confirmAction = CouiDialogAction(stringResource(R.string.dialog_input_save), { if (canSave) onSave(name) }, enabled = canSave),
        dismissAction = CouiDialogAction(stringResource(R.string.dialog_cancel), onDismissRequest),
        material = if (material) CouiDialogDefaults.material(dark) else null)
}

private const val DEVICE_NAME_MAX_BYTES = 32

// Settings 的名称策略按 UTF-16 单元计 1/2/3，而非 String.length；emoji 范围以 smali 常量交叉核对。
private val deviceNameEmoji = Regex(
    "\u2b55|\u2b50|[\uD83C\uDC00-\uD83C\uDFFF]|[\uD83D\uDC00-\uD83D\uDFFF]|[\u2600-\u27ff]|" +
        "[\uD83E\uDD00-\uD83E\uDDFF]|[\u2300-\u23ff]|[\u2500-\u25ff]|[\u2b00-\u2bff]|[\u2d06]|" +
        "[\u3030]|[\u2049]|[\u203c]|[\u2934\ufe0f]|[\u2935]|[\u3299]|[\u303d]|[\u2150-\u21ff]|" +
        "[#*0-9]\ufe0f?\u20e3|[\uD83E\uDE00-\uD83E\uDEFF]", RegexOption.IGNORE_CASE)

private fun deviceNameByteCount(text: String): Int = text.sumOf { if (it.code < 128) 1 else if (it.code < 2048) 2 else 3 }

private fun filterDeviceNameEdit(previous: TextFieldValue, proposed: TextFieldValue): TextFieldValue {
    if (previous.text == proposed.text) return proposed
    val before = previous.text
    val after = proposed.text
    var start = 0
    while (start < minOf(before.length, after.length) && before[start] == after[start]) start++
    if (start > 0 && start < after.length && Character.isSurrogatePair(after[start - 1], after[start])) start--
    var suffix = 0
    while (suffix < minOf(before.length, after.length) - start &&
        before[before.lastIndex - suffix] == after[after.lastIndex - suffix]) suffix++
    if (suffix > 0 && suffix < after.length - start &&
        Character.isSurrogatePair(after[after.length - suffix - 1], after[after.length - suffix])) suffix--
    val inserted = after.substring(start, after.length - suffix)
    val blocked = deviceNameEmoji.containsMatchIn(inserted)
    var remaining = DEVICE_NAME_MAX_BYTES - deviceNameByteCount(before.take(start) + before.takeLast(suffix))
    var retained = 0
    while (!blocked && retained < inserted.length) {
        val size = Character.charCount(inserted.codePointAt(retained))
        val cost = deviceNameByteCount(inserted.substring(retained, retained + size))
        if (cost > remaining) break
        remaining -= cost
        retained += size
    }
    if (retained == inserted.length) return proposed
    val removed = inserted.length - retained
    val end = start + inserted.length
    fun mapOffset(index: Int) = when {
        index <= start + retained -> index
        index < end -> start + retained
        else -> index - removed
    }
    fun mapRange(range: TextRange) = TextRange(mapOffset(range.start), mapOffset(range.end))
    return TextFieldValue(after.take(start) + inserted.take(retained) + after.takeLast(suffix),
        selection = mapRange(proposed.selection), composition = proposed.composition?.let(::mapRange)?.takeUnless { it.collapsed })
}
