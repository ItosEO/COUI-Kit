package com.itos.coui.demo

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.Dialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogPlacement

/**
 * 标准选择弹窗：项目材质、原厂标题/操作区及主题色确认。content 放置任意选择器。
 * 宿主持有草稿，只有 onConfirm 保存业务值；取消、返回与外部点击走 onDismissRequest。
 */
@Composable
internal fun DemoPickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    confirmText: String,
    cancelText: String,
    visible: Boolean,
    backdrop: CouiBackdrop? = null,
    confirmEnabled: Boolean = true,
    placement: CouiDialogPlacement = CouiDialogPlacement.Bottom,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(modifier, onDismissRequest, visible = visible,
        confirmAction = CouiDialogAction(confirmText, onConfirm, confirmEnabled,
            style = CouiDialogActionStyle.Primary),
        dismissAction = CouiDialogAction(cancelText, onDismissRequest),
        title = title, placement = placement,
        material = demoDialogMaterial(backdrop),
        backdrop = backdrop, content = content)
}
