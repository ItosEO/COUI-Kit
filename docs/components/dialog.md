# 💬弹窗

`Dialog` 用于显示确认、提示等弹窗，可以放在屏幕底部或居中显示。需要单行输入时，可以使用 `InputDialog`。任务执行期间可以使用 `LoadingDialog` 显示加载动画和提示。这些组件位于 `com.itos.coui.dialog`。

[确认弹窗](#显示一个简单确认弹窗) · [输入弹窗](#输入型弹窗) · [加载弹窗](#显示加载弹窗) · [背景材质](#使用背景材质)

## 显示一个简单确认弹窗

### 最小调用

在页面的 Composable 中加入下面的代码。示例进入组合时显示弹窗；`title`、`confirmLabel`、`cancelLabel` 和 `onConfirm` 由页面提供：

~~~kotlin
var visible by remember { mutableStateOf(true) }

Dialog(
    visible = visible,
    title = title,
    onDismissRequest = { visible = false },
    confirmAction = CouiDialogAction(confirmLabel, onClick = {
        onConfirm()
        visible = false
    }),
    dismissAction = CouiDialogAction(cancelLabel, onClick = { visible = false }),
)
~~~

确认时示例先调用 `onConfirm`，再将 `visible` 设为 `false`；取消时直接设为 `false`。返回键和点击外部会调用 `onDismissRequest`。组件不会自动更新页面的显隐状态。要从按钮打开弹窗，可以将 `visible` 的初值设为 `false`，再在按钮回调中设为 `true`。标题和正文可以传 `null`，对应区域不会显示。两个短按钮横排；单个按钮、三个按钮或文案放不下时自动竖排。

### 控制显隐

传入 `visible` 时，将调用持续保留在组合中。`true` 开始入场，`false` 开始退场；库在退场结束后释放窗口。需要背景材质时，移动和背景采样使用同一帧的位移、缩放，动画中途可以反向。`Dialog` 和两个 `InputDialog` 重载均支持此参数。不要再用 `if (visible)` 包裹这类调用，否则退场内容会立即移除。

也可以省略 `visible`，由外层条件创建窗口。此时使用系统窗口动画，不提供移动过程中的实时采样。下面的 `visible`、`onDismiss`、`confirmLabel` 和 `onConfirm` 由页面提供；需要关闭时，由相应回调更新页面状态：

~~~kotlin
if (visible) {
    Dialog(
        onDismissRequest = onDismiss,
        confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    )
}
~~~

## 添加第三个操作

通过 `neutralAction` 添加第三个操作。例如，在确认和取消之外增加重置按钮：

~~~kotlin
Dialog(
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

三个按钮竖排时，从上到下依次是 `neutralAction`、`confirmAction` 和 `dismissAction`。红字是 `neutralAction` 的默认文字色，取自弹窗的 `colors.destructive`。三个按钮都可以各自传入 `CouiDialogAction.colors`，分别指定底色、文字色和禁用态配色。选择 `Primary` 样式时需显式传入按钮配色。`enabled = false` 只禁用对应按钮。

如果三个按钮都使用普通配色，让它们沿用默认的 `Secondary` 样式，并把 `destructive` 设为普通操作的文字色 `action`。下面的文案和操作回调由宿主提供。

~~~kotlin
val colors = CouiDialogDefaults.themeColors(dark)

Dialog(
    onDismissRequest = onDismiss,
    dark = dark,
    title = title,
    colors = colors.copy(destructive = colors.action),
    confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    dismissAction = CouiDialogAction(cancelLabel, onDismiss),
    neutralAction = CouiDialogAction(alternativeLabel, onAlternative),
)
~~~

## 使用自定义正文

需要在正文中放列表或选择器时，可以使用 `content` 槽位：

~~~kotlin
Dialog(
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

默认会在窗口获焦后请求输入焦点和键盘。需要手动点击输入区时，设置 `autoFocus = false`。

传入 `visible` 时，卡片随键盘展开、收起而移动，背景模糊采样同步跟随卡片位置。关闭弹窗会立即释放输入焦点并收起键盘，卡片从当前位置退场。省略 `visible` 时，使用系统窗口的 `ADJUST_RESIZE` 避让键盘。两种方式均不需要额外添加 `imePadding`；自定义 `properties` 时，保留 `decorFitsSystemWindows = true` 和 `usePlatformDefaultWidth = false`。

### 宿主页的键盘避让

宿主页如果使用 `imePadding()`，只在自己的窗口拥有焦点时启用。输入弹窗打开后，宿主页仍可能收到键盘尺寸；继续应用间距会提前空出整块区域，在键盘升起前露出窗口底色。

在宿主 Activity 的 Composable 中创建以下 Modifier，替换页面原来的无条件 `imePadding()`，并传给现有的 Scaffold 或页面容器。焦点判断放在宿主页，不放在 `InputDialog` 内部：

~~~kotlin
val imePadding = if (LocalWindowInfo.current.isWindowFocused) {
    Modifier.imePadding()
} else {
    Modifier
}
val pageModifier = Modifier.fillMaxSize().then(imePadding)
~~~

## 显示加载弹窗

`LoadingDialog` 在屏幕中央显示循环转动的弧线和一行提示。把调用留在组合中，由宿主在任务开始和结束时更新 `visible`。

~~~kotlin
LoadingDialog(
    visible = isWorking,
    message = loadingMessage,
)
~~~

`isWorking` 由宿主的任务状态提供，`loadingMessage` 是宿主的提示文案。提示过长时以省略号收尾。默认屏蔽外点关闭。传入 `onDismissRequest` 后，返回键可请求宿主关闭。需要让点击外部也请求关闭时，同时设置 `dismissOnClickOutside = true`。宿主处理任务和关闭请求，通过 `visible` 控制窗口显隐。

## 使用背景材质

要让背景模糊随弹窗一起移动，在 API 33 及以上提供窗口背景源，并把显隐状态传给 `visible`。下面的 `page` 是原有宿主页，可以包含 COUI `Scaffold`。把背景修饰符挂到页面容器上，弹窗留在该容器之外。

~~~kotlin
@Composable
fun BlurredDialogPage(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    page: @Composable () -> Unit,
) {
    val supported = CouiDialogDefaults.isMaterialSupported()
    ProvideCouiMenuBackdrop { windowSource ->
        Box(modifier.fillMaxSize().then(if (supported) windowSource else Modifier)) {
            page()
        }
        Dialog(
            visible = visible,
            title = title,
            onDismissRequest = onDismiss,
            confirmAction = CouiDialogAction(confirmLabel, onDismiss),
            material = if (supported) CouiDialogDefaults.material(CouiTheme.isDark) else null,
        )
    }
}
~~~

宿主打开弹窗时将 `visible` 设为 `true`，在 `onDismiss` 中改为 `false`。保留弹窗调用与窗口背景源，让库在退场结束后释放窗口。`InputDialog` 和 `LoadingDialog` 使用相同的材质接入方式。普通弹窗与输入弹窗也可以省略 `visible`，由外层条件创建窗口，此时系统移动整个窗口，COUI 会采样弹窗到位时的位置。

`material` 非空时须通过 `backdrop` 或 `ProvideCouiMenuBackdrop` 提供有效窗口源。COUI `Scaffold` 自动提供的是栏位使用的正文源。窗口源的作用域见[宿主页面接入](../guide/secondary-pages.md#正文源与窗口源)。Android 12 上传入 `material = null`，使用不透明表面。

启用材质时，`colors.dimAmount` 同时控制窗口遮罩与面板采样的压暗强度。面板先压暗采样，再做模糊和混色。

## 位置和外观

默认从底部显示。需要居中时，设置 `placement = CouiDialogPlacement.Center`：

~~~kotlin
Dialog(
    onDismissRequest = onDismiss,
    confirmAction = CouiDialogAction(confirmLabel, onConfirm),
    dark = dark,
    placement = CouiDialogPlacement.Center,
)
~~~

`dark` 和 `colors` 默认读取局部 `CouiTheme` 或初始化策略；未初始化时使用原生预设，模式来自 Local 或系统。M3 模式使用对应文字角色的字体族，字号、字重、行距和 fontScale 由组件控制。自定义正文使用宿主样式。通用规则见[外观与预设](../guide/appearance.md)。

默认 `colors` 使用 `CouiDialogDefaults.themeColors(dark)`。显式自定义时可从该入口复制调整；主操作配色可以使用 `CouiDialogDefaults.primaryActionColors(accent)`。

修改圆角时，传入 `shape = CouiShape(radius, cornerWeight)`。裁剪与材质轮廓从 Shape 读取半径和权重。`cornerRadius` 默认 `Dp.Unspecified`，表示从 Shape 解析；显式传入时必须与 CouiShape 的半径一致。

使用其他自定义 `Shape` 并开启材质轮廓时，须显式提供有限非负的 `cornerRadius`；轮廓权重使用 Dialog 预设。无材质轮廓时只使用 Shape 裁剪。

## 窗口和限制

默认 `DialogProperties` 关闭平台默认宽度，使用 COUI 自己的外框策略。自定义 `properties` 时如果还要保留 COUI 宽度，显式设置 `usePlatformDefaultWidth = false`。

尺寸极小的弹窗和推荐/AI 支付按钮暂不支持。

## Demo 中的样式与示例

Demo 的“组件 → 弹窗”分组包含底部、居中、长内容、多个操作、输入和加载弹窗示例。加载示例会在模拟任务结束后自动关闭。

支持的设备可以通过“弹窗背景材质”开关比较模糊材质和不透明表面。

Demo 源码见 [DemoDialogSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt) 与[输入型示例](../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)。公开参数见 [Dialog / CouiDialogAction](../../coui/src/main/java/com/itos/coui/dialog/Dialog.kt)、[CouiDialogActionStyle / CouiDialogActionColors](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogButton.kt)、[InputDialog](../../coui/src/main/java/com/itos/coui/dialog/InputDialog.kt)、[LoadingDialog](../../coui/src/main/java/com/itos/coui/dialog/LoadingDialog.kt) 与 [CouiDialogDefaults](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogDefaults.kt)。
