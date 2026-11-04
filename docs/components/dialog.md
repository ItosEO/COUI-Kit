# 💬弹窗

`AlertDialog` 用于显示确认、提示等弹窗，可以放在屏幕底部或居中显示。需要单行输入时，可以使用 `InputDialog`。

## 最小调用

页面提供显隐状态、操作文案和回调；`confirmAction` 是宿主用文案和业务回调构造的 `CouiDialogAction`：

~~~kotlin
if (visible) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmAction = confirmAction,
    )
}
~~~

`dark` 和 `colors` 默认读取局部 `CouiTheme` 或初始化策略；未初始化时使用原生预设，模式来自 Local 或系统。M3 模式使用对应文字角色的字体族，字号、字重、行距和 fontScale 由组件控制。自定义正文使用宿主样式。通用规则见[外观与预设](../guide/appearance.md)。

## 显示一个简单确认弹窗

下面的弹窗包含标题、正文、确认和取消按钮：

~~~kotlin
@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    title: String?,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            dark = dark,
            title = title,
            message = message,
            confirmAction = CouiDialogAction(confirmLabel, onConfirm),
            dismissAction = CouiDialogAction(cancelLabel, onDismiss),
        )
    }
}
~~~

`visible` 为 `true` 时显示弹窗。点击按钮会调用对应的回调；需要关闭弹窗时，在回调中将页面的 `visible` 设为 `false`。返回键和点击外部通过 `onDismissRequest` 请求关闭。此例显式提供 `dark`；省略时使用默认值。

标题和正文可以传 `null`，对应区域不会显示。两个短按钮横排；单个按钮、三个按钮或文案放不下时自动竖排。

## 添加第三个操作

通过 `neutralAction` 添加第三个操作。例如，在确认和取消之外增加重置按钮：

~~~kotlin
AlertDialog(
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

三个按钮竖排时，`neutralAction` 在最上方，默认使用破坏性文字色。设置 `CouiDialogActionStyle.Primary` 时，需要像示例一样提供 `colors`。`enabled = false` 只禁用对应按钮。

## 使用自定义正文

需要在正文中放列表或选择器时，可以使用 `content` 槽位：

~~~kotlin
AlertDialog(
    onDismissRequest = onDismiss,
    dark = dark,
    title = title,
    confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    dismissAction = CouiDialogAction(cancelLabel, onDismiss),
) {
    Text("这里放页面自己的内容")
}
~~~

`content` 位于正文滚动区。操作区独立排列；正文较长时，按钮仍保留在窗口内。示例中的 `Text` 来自 Material3，项目需要声明该依赖。

## 输入型弹窗

需要单行输入时，可以使用 `InputDialog`。下面用 `TextFieldState` 保存草稿，仅在输入非空时启用保存按钮：

~~~kotlin
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
    InputDialog(
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

编辑不会自动提交或关闭弹窗。示例在 `onSave` 中传出输入值，由页面保存并关闭弹窗；关闭后重新打开会创建新的输入状态。需要保留草稿时，可以将 `TextFieldState` 提到弹窗外层。

页面负责设置长度、内容校验和确认条件。`InputDialog` 支持 `TextFieldState` 和 `TextFieldValue`。需要按替换范围过滤粘贴或输入法组合输入时，使用 `state` 版本的 `inputTransformation`；程序性的 `state.edit` 和清除不会经过该转换。

默认会在窗口获焦后请求输入焦点和键盘。需要手动点击输入区时，设置 `autoFocus = false`。输入弹窗使用 `ADJUST_RESIZE`，内容不需要再添加 `imePadding`。自定义 `properties` 时，请保留 `decorFitsSystemWindows = true` 和 `usePlatformDefaultWidth = false`。

## 使用背景材质

默认弹窗使用不透明表面。API 33 及以上可以传入 `material`，采样页面背景。先按[快速开始](../guide/getting-started.md#浮动组件的背景源)创建 `backdrop`，再将弹窗放在背景源节点之外：

~~~kotlin
if (visible) {
    AlertDialog(
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
~~~

`visible`、文案和回调沿用前面的确认弹窗示例。`material` 非空时要求 API 33 及以上，并须通过 `backdrop` 或 `ProvideCouiMenuBackdrop` 提供有效背景源；缺少系统支持或背景源时会报错。不支持材质时传入 `material = null`，使用不透明表面。

背景模糊采样自页面，窗口遮罩由系统绘制。

## 位置和外观

默认从底部显示。需要居中时，设置 `placement = CouiDialogPlacement.Center`：

~~~kotlin
AlertDialog(
    onDismissRequest = onDismiss,
    confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    dark = dark,
    placement = CouiDialogPlacement.Center,
)
~~~

默认 `colors` 使用 `CouiDialogDefaults.themeColors(dark)`。显式自定义时可从该入口复制调整；主操作配色可以使用 `CouiDialogDefaults.primaryActionColors(accent)`。

修改圆角时，传入 `shape = CouiShape(radius, cornerWeight)`。裁剪与材质轮廓从 Shape 读取半径和权重。`cornerRadius` 默认 `Dp.Unspecified`，表示从 Shape 解析；显式传入时必须与 CouiShape 的半径一致。

使用其他自定义 `Shape` 并开启材质轮廓时，须显式提供有限非负的 `cornerRadius`；轮廓权重使用 Dialog 预设。无材质轮廓时只使用 Shape 裁剪。

## 窗口和限制

默认 `DialogProperties` 关闭平台默认宽度，使用 COUI 自己的外框策略。自定义 `properties` 时如果还要保留 COUI 宽度，显式设置 `usePlatformDefaultWidth = false`。

尺寸极小的弹窗和推荐/AI 支付按钮暂不支持。

## Demo 中的样式与示例

Demo 的“组件 → 弹窗”分组包含底部、居中、长内容、多个操作和输入弹窗示例。

支持的设备可以通过“弹窗背景材质”开关比较模糊材质和不透明表面。

Demo 源码见 [DemoDialogSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt) 与[输入型示例](../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)。公开参数见 [AlertDialog / CouiDialogAction](../../coui/src/main/java/com/itos/coui/dialog/AlertDialog.kt)、[CouiDialogActionStyle / CouiDialogActionColors](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogButton.kt)、[InputDialog](../../coui/src/main/java/com/itos/coui/dialog/InputDialog.kt) 与 [CouiDialogDefaults](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogDefaults.kt)。
