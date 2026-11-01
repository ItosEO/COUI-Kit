package com.itos.coui.demo

import com.itos.coui.picker.*
import android.os.Build
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults
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
    dark: Boolean,
    accent: Color,
    backdrop: CouiBackdrop?,
    confirmEnabled: Boolean = true,
    placement: CouiDialogPlacement = CouiDialogPlacement.Bottom,
    content: @Composable ColumnScope.() -> Unit,
) {
    CouiAlertDialog(modifier, onDismissRequest,
        confirmAction = CouiDialogAction(confirmText, onConfirm, confirmEnabled,
            style = CouiDialogActionStyle.Primary, colors = CouiDialogDefaults.primaryActionColors(accent)),
        dismissAction = CouiDialogAction(cancelText, onDismissRequest),
        title = title, dark = dark, placement = placement,
        material = if (Build.VERSION.SDK_INT >= 33 && backdrop != null) CouiDialogDefaults.material(dark) else null,
        backdrop = backdrop, content = content)
}
