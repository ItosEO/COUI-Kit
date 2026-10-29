# 💬弹窗

CouiAlertDialog 是 COUI 的普通弹窗，支持底部或居中显示、标题、正文和一到三个操作。CouiInputDialog 复用同一个窗口，增加单行输入。

## 显示一个简单确认弹窗

使用以下代码显示一个底部确认弹窗：

~~~kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction

@Composable
fun ConfirmDialogHost(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    pageContent: @Composable () -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        pageContent()
        if (visible) {
            CouiAlertDialog(
                onDismissRequest = onDismiss,
                dark = dark,
                title = title,
                message = message,
                confirmAction = CouiDialogAction(confirmLabel, onConfirm),
                dismissAction = CouiDialogAction(cancelLabel, onDismiss),
            )
        }
    }
}
~~~

`visible` 为 `true` 时才组合弹窗。确认和取消分别执行各自的 `onClick`，返回键和点击外部只回调 `onDismissRequest`，不提交确认。

标题和正文可以为 `null`，对应的文本区就不显示。操作默认是次按钮；两个短操作横排，单个、三个或文案放不下时自动竖排。

## 添加第三个操作

`neutralAction` 是第三个操作。三个操作会竖排，neutral 在最上，默认用破坏性文字色。加第三个操作时：

~~~kotlin
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogDefaults

CouiAlertDialog(
    onDismissRequest = onDismiss,
    dark = dark,
    title = title,
    message = message,
    confirmAction = CouiDialogAction(
        text = confirmLabel,
        onClick = onConfirm,
        enabled = canConfirm,
        style = CouiDialogActionStyle.Primary,
        colors = CouiDialogDefaults.primaryActionColors(accent),
    ),
    dismissAction = CouiDialogAction(cancelLabel, onDismiss),
    neutralAction = CouiDialogAction("重置选择", onReset),
)
~~~

主操作必须显式提供配色，库不读 MaterialTheme。`enabled = false` 只禁用这一个操作，其他操作仍然可用。`accent` 是当前主题色，`canConfirm` 是页面校验结果。

## 使用自定义正文

需要列表、说明或选择器时，把内容放进 `content` 槽位：

~~~kotlin
import androidx.compose.material3.Text

CouiAlertDialog(
    onDismissRequest = onDismiss,
    dark = dark,
    title = title,
    confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    dismissAction = CouiDialogAction(cancelLabel, onDismiss),
) {
    Text("这里放页面自己的内容")
    // 草稿和校验由页面维护。
}
~~~

`content` 位于正文滚动区，适合普通内容。不要再套一个全屏纵向滚动，也不要把操作按钮放进正文视口；操作区在超高时独立滚动，长正文不会把按钮顶出窗口。

## 输入型弹窗

需要标准单行输入时直接使用 CouiInputDialog。下面用 `TextFieldState` 保存草稿，确认条件也由页面判断：

~~~kotlin
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiInputDialog

@Composable
fun RenameDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    accent: Color,
    title: String,
    message: String,
    saveLabel: String,
    cancelLabel: String,
    clearLabel: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    if (!visible) return
    val editor = rememberTextFieldState()
    CouiInputDialog(
        modifier = modifier,
        state = editor,
        onDismissRequest = onDismiss,
        dark = dark,
        accent = accent,
        title = title,
        message = message,
        clearContentDescription = clearLabel,
        confirmAction = CouiDialogAction(
            text = saveLabel,
            onClick = { onSave(editor.text.toString()) },
            enabled = editor.text.isNotBlank(),
        ),
        dismissAction = CouiDialogAction(cancelLabel, onDismiss),
    )
}
~~~

输入值、选区和输入法组合区都在页面状态里，编辑不会自动提交业务或关闭弹窗。长度和内容校验由调用方做；计数、清除和确认条件也由调用方给。

需要保存 `TextFieldValue` 的旧调用仍然可用。要按真实替换范围过滤粘贴或组合输入时，用 `state` 版本加 `inputTransformation`；程序性的 `state.edit` 和清除不会经过转换。

默认 `autoFocus` 会等窗口获焦后请求输入焦点和键盘。需要用户自己点输入区时传 `false`。输入弹窗显式使用 `ADJUST_RESIZE`，内容不要再叠 `imePadding`；`decorFitsSystemWindows = true` 必须保留，覆盖 `properties` 时也要保留 `usePlatformDefaultWidth = false`。

## 使用背景材质

不传 `material` 时弹窗是不透明表面，可以直接用。API 33 起可以打开项目材质：

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogDefaults

@Composable
fun MaterialDialogHost(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    background: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    pageContent: @Composable () -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            pageContent()
        }
        if (visible) {
            CouiAlertDialog(
                onDismissRequest = onDismiss,
                dark = dark,
                title = title,
                message = message,
                confirmAction = CouiDialogAction(confirmLabel, onConfirm),
                dismissAction = CouiDialogAction(cancelLabel, onDismiss),
                material = if (CouiDialogDefaults.isMaterialSupported()) {
                    CouiDialogDefaults.material(dark)
                } else {
                    null
                },
                backdrop = backdrop,
            )
        }
    }
}
~~~

弹窗在源子树外，通过屏幕坐标采样页面。缺少平台支持或窗口源时会明确报错，不会悄悄退回另一种模糊。已有 `ProvideCouiMenuBackdrop` 的页面可以省略 `backdrop`，沿用当前 Provider 的源。

背景是采样页面做的模糊，窗口遮罩仍由系统画。

## 位置和外观

`placement` 用 `CouiDialogPlacement.Bottom` 或 `CouiDialogPlacement.Center`。`colors` 从 `CouiDialogDefaults.colors(dark)` 取，可以按页面调表面、标题、正文和操作色；主操作配色由 `CouiDialogDefaults.primaryActionColors(accent)` 生成。

`shape` 和 `cornerRadius` 要一起改。面板、操作区和外层裁剪都依赖同一组圆角，只换 Shape 不换半径会看起来错位。宽度、高度和按钮留白由 `CouiDialogDefaults` 持有，不要在页面里复制一套数值。

## 窗口和限制

默认 `DialogProperties` 关闭平台默认宽度，使用 COUI 自己的外框策略。自定义 `properties` 时如果还要保留 COUI 宽度，显式设置 `usePlatformDefaultWidth = false`。

操作区可滚动，拖动时不会误触按钮。

尺寸极小的弹窗和推荐/AI 支付按钮暂不支持；极端高度下的系统弹窗行为也没有完整还原。`content` 槽位不代表这些能力已经实现。

部分 ROM 的字体和键盘行为要真机确认。

## Demo 中的样式与示例

Demo 的“组件 → 弹窗”分组有六个 CouiAlertDialog 示例：底部确认、居中确认、长内容滚动、三个操作、禁用确认和主题色确认。输入示例使用 CouiInputDialog，演示重命名设备时的计数、清除、过滤和保存条件。

支持平台可以通过分组里的“弹窗背景材质”开关，在项目模糊材质和不透明表面之间比较。确定会更新示例结果，取消、外点和返回只关闭窗口。

Demo 源码见 [DemoDialogSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt) 与[输入型示例](../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)。公开参数见 [CouiAlertDialog / CouiDialogAction](../../coui/src/main/java/com/itos/coui/dialog/CouiAlertDialog.kt)、[CouiDialogActionStyle / CouiDialogActionColors](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogButton.kt)、[CouiInputDialog](../../coui/src/main/java/com/itos/coui/dialog/CouiInputDialog.kt) 与 [CouiDialogDefaults](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogDefaults.kt)。
