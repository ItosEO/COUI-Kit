# 弹窗

`Dialog` 提供底部或居中显示的确认与警告弹窗，`InputDialog` 提供内置单行输入区的输入型弹窗，`LoadingDialog` 在任务执行期间显示居中的加载圆弧与提示文案。这些组件位于 `com.itos.coui.dialog`，显隐状态、文案、输入校验与业务提交均由宿主持有。

- **基础不透明弹窗（`Dialog`、`InputDialog`、`LoadingDialog`）**：支持库的最低系统版本（API 31+）。
- **半透明模糊材质与轮廓光**：要求 Android 13（API 33）及以上，并需要宿主提供不包含本弹窗的窗口背景源（`backdrop`）。

## 基本用法

`Dialog` 支持两种显隐控制方式，接入时按是否需要**移动过程中实时采样背景**与**自定义退场动画**二选一：

1. **常驻组合传入 `visible`（推荐用于材质弹窗）**：将 `Dialog` 持续保留在组合中，通过 `visible` 切换展开与关闭。`visible = true` 开始入场，`false` 开始退场并在动画结束后释放窗口；背景模糊采样与卡片位移、缩放共用同一帧变换，支持动画中途反向。**不要在外层再用 `if (visible)` 包裹**，否则关闭时会直接销毁窗口、跳过退场动画。
2. **外层 `if (visible)` 控制（省略 `visible`）**：`visible` 默认为 `null`。由外层条件决定是否进入组合，使用系统窗口动画；此方式不提供卡片移动过程中的实时背景采样。

下面展示常驻组合的确认弹窗用法，文案、深浅色模式与回调均由宿主传入：

~~~kotlin
@Composable
fun ConfirmDialogExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    title: String?,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        dark = dark,
        title = title,
        message = message,
        confirmAction = CouiDialogAction(confirmLabel, onConfirm),
        dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
    )
}
~~~

若不需要实时背景采样，也可省略 `visible`，改由外层控制组合：

~~~kotlin
if (visible) {
    Dialog(
        onDismissRequest = onDismissRequest,
        title = title,
        message = message,
        confirmAction = CouiDialogAction(confirmLabel, onConfirm),
        dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
    )
}
~~~

接入时注意以下要点：

- **显式关闭由宿主控制**：点击按钮会触发 `CouiDialogAction.onClick`，**组件不会自动关闭弹窗**；需要在回调中由宿主将 `visible` 设为 `false`。系统返回键与点击遮罩区域会触发 `onDismissRequest`。
- **可选标题与正文**：`title` 和 `message` 可传 `null` 或空字符串，对应区域会自动隐藏。
- **主题默认解析**：省略 `dark` 与 `colors` 时自动读取局部 `CouiTheme` 或全局初始化策略，详见[外观与预设](../guide/appearance.md)。

## 多操作、主操作配色与自定义正文

通过 `neutralAction` 可添加第三个操作，通过 `CouiDialogActionStyle.Primary` 可突出主操作按钮；需要在正文中放置列表或[选择器](pickers.md)时，可使用尾随 `content` 槽位：

~~~kotlin
@Composable
fun MultiActionDialogExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    accent: Color,
    title: String,
    confirmLabel: String,
    cancelLabel: String,
    resetLabel: String,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    onReset: () -> Unit,
    onDismissRequest: () -> Unit,
    bodyContent: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        dark = dark,
        title = title,
        confirmAction = CouiDialogAction(
            text = confirmLabel,
            onClick = onConfirm,
            enabled = canConfirm,
            style = CouiDialogActionStyle.Primary,
            colors = CouiDialogDefaults.primaryActionColors(accent),
        ),
        dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
        neutralAction = CouiDialogAction(resetLabel, onReset),
        content = bodyContent,
    )
}
~~~

- **按钮排布规则**：两个短按钮自动横排（`dismissAction` 在左、`confirmAction` 在右）；单个按钮、三个按钮，或任意按钮文案在均分宽度下放不下时，自动转为竖排（自上而下依次为 `neutralAction`、`confirmAction`、`dismissAction`）。
- **主操作与破坏性配色**：设置 `CouiDialogActionStyle.Primary` 时**必须**传入 `colors`（可通过 `CouiDialogDefaults.primaryActionColors(accent)` 创建），否则会抛出异常。`neutralAction` 默认使用破坏性文字色（`colors.destructive`），`enabled = false` 仅禁用对应按钮。
- **正文滚动隔离**：`content` 位于正文可滚动区域内，操作按钮区独立固定在底部；正文较长时按钮仍保留在窗口内，不会滚出视口。若正文使用 Material 3 的 `Text` 等组件，宿主需自行声明 Material 3 依赖。

## 输入型弹窗

需要单行输入时使用 `InputDialog`。组件提供两种状态重载：`TextFieldState` 重载（支持通过 `inputTransformation` 过滤粘贴和输入法组合编辑范围）与 `TextFieldValue` 回调重载。

下面以 `TextFieldState` 重载为例，仅在输入非空时启用保存按钮：

~~~kotlin
@Composable
fun RenameDialogExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    accent: Color,
    title: String,
    message: String?,
    placeholder: String?,
    saveLabel: String,
    cancelLabel: String,
    clearLabel: String,
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
) {
    val editor = rememberTextFieldState()
    InputDialog(
        modifier = modifier,
        visible = visible,
        state = editor,
        onDismissRequest = onDismissRequest,
        dark = dark,
        accent = accent,
        title = title,
        message = message,
        placeholder = placeholder,
        clearContentDescription = clearLabel,
        confirmAction = CouiDialogAction(
            text = saveLabel,
            onClick = { onSave(editor.text.toString()) },
            enabled = editor.text.isNotBlank(),
        ),
        dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
    )
}
~~~

接入 `InputDialog` 时注意以下规则：

- **草稿生命周期与提交**：编辑不会自动保存或关闭弹窗。若将 `rememberTextFieldState()` 放在常驻组合的弹窗外层或内部，关闭后再次打开会保留上次输入；需要每次打开清空草稿时，可在打开时重置 `state`，或改用外层 `if (visible)` 包裹。
- **校验、计数与清除按钮**：长度限制、错误态（`counterIsError`）与 `counter` 文案均由宿主持有并传入。`clearContentDescription` 为必填无障碍描述；清除按钮仅在获焦且内容非空时出现，点击仅清空文本并保持焦点，不触发提交。使用 `TextFieldState` 重载时，程序性调用 `state.edit` 与清除操作不经过 `inputTransformation`。
- **焦点与键盘策略**：默认 `autoFocus = true`，窗口获焦后自动请求输入焦点并弹出软键盘；设为 `false` 时需用户手动点击输入区。传入 `visible` 时，卡片会随键盘展开和收起平滑移动并同步背景采样，关闭（`visible = false`）时立即释放焦点并收起键盘；省略 `visible` 时使用系统 `ADJUST_RESIZE` 避让键盘。两种方式均**不要**在弹窗内部叠加 `imePadding()`。

### 宿主页的键盘避让

宿主页如果使用 `imePadding()`，必须只在宿主自身窗口拥有焦点时启用。输入弹窗打开后，宿主页仍可能收到键盘尺寸变化；继续无条件应用 `imePadding()` 会导致页面在键盘升起前提前空出整块区域，露出窗口底色。

请在宿主页的根容器（如传给 `Scaffold` 或页面容器的 `modifier`）中使用 `LocalWindowInfo.current.isWindowFocused` 替换无条件的 `imePadding()`，该判断应放在宿主页而非 `InputDialog` 内部：

~~~kotlin
val imePadding = if (LocalWindowInfo.current.isWindowFocused) {
    Modifier.imePadding()
} else {
    Modifier
}
val pageModifier = Modifier.fillMaxSize().then(imePadding)
~~~

## 显示加载弹窗

`LoadingDialog` 在屏幕中央显示循环转动的弧线和一行提示。把调用常驻留在组合中，由宿主在任务开始和结束时更新 `visible`：

~~~kotlin
@Composable
fun TaskLoadingDialogExample(
    modifier: Modifier = Modifier,
    isWorking: Boolean,
    loadingMessage: String,
    onDismissRequest: (() -> Unit)? = null,
) {
    LoadingDialog(
        modifier = modifier,
        visible = isWorking,
        message = loadingMessage,
        onDismissRequest = onDismissRequest,
    )
}
~~~

- **文案与交互**：提示过长时以省略号收尾。默认屏蔽点击遮罩关闭；传入 `onDismissRequest` 后，系统返回键可请求宿主关闭，若需点击外部也请求关闭请同时设置 `dismissOnClickOutside = true`。
- **状态归属**：宿主负责实际任务生命周期与关闭请求，通过 `visible` 控制窗口显隐。

## 使用背景材质

弹窗默认使用不透明表面。在 Android 13（API 33）及以上，可在宿主页外层使用 `ProvideCouiMenuBackdrop` 录制窗口背景，并向 `Dialog` 或 `InputDialog` 传入 `material` 与 `backdrop`：

~~~kotlin
@Composable
fun HostPageWithDialogExample(
    modifier: Modifier = Modifier,
    dialogVisible: Boolean,
    dark: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    pageContent: @Composable () -> Unit,
) {
    ProvideCouiMenuBackdrop { windowSource ->
        Box(Modifier.fillMaxSize().then(windowSource)) {
            pageContent()
        }

        val backdrop = rememberCouiWindowBackdrop()
        val material = if (CouiDialogDefaults.isMaterialSupported() && backdrop != null) {
            CouiDialogDefaults.material(dark)
        } else {
            null
        }

        Dialog(
            modifier = modifier,
            visible = dialogVisible,
            onDismissRequest = onDismissRequest,
            dark = dark,
            title = title,
            message = message,
            confirmAction = CouiDialogAction(confirmLabel, onConfirm),
            dismissAction = CouiDialogAction(cancelLabel, onDismissRequest),
            material = material,
            backdrop = backdrop,
        )
    }
}
~~~

- **版本与背景源校验**：当 `material != null` 时，系统低于 API 33 或未通过 `backdrop` / `ProvideCouiMenuBackdrop` 提供有效背景源会直接抛出异常。不支持材质时传入 `material = null`，使用不透明表面。`InputDialog` 与 `LoadingDialog` 使用相同的材质接入方式。
- **背景源节点边界**：`Dialog` 必须放在 `windowSource` 修饰的页面内容节点之外，不能让弹窗自身进入背景源，也不要只在弹窗内部新建背景源来代替宿主背景。
- 启用材质时，`colors.dimAmount` 同时控制窗口遮罩与面板采样的压暗强度。面板先压暗采样，再做模糊。底图恢复到面板尺寸后，先按 `mix` 的 alpha 逐通道混合 RGB，再执行 `blendMode`。模糊等级为 `0` 时直接混色。窗口外遮罩由系统独立淡入淡出。传入 `visible` 时，入场在窗口完成布局后开始，背景模糊采样与卡片当前帧位置、缩放实时同步。

## 位置、圆角与外观

- **显示位置（`placement`）**：默认 `CouiDialogPlacement.Bottom`（底部显示）；需要居中显示时设为 `CouiDialogPlacement.Center`。
- **配色（`colors`）**：默认使用 `CouiDialogDefaults.themeColors(dark)`，自定义时可基于 `themeColors(dark)` 或 `CouiDialogDefaults.colors(dark)` 复制调整；`dimAmount` 必须在 `0f..1f` 范围内。
- **圆角与轮廓（`shape`、`cornerRadius`）**：默认 `shape = CouiDialogDefaults.shape`（`CouiShape(45.dp, 3f)`），`cornerRadius = Dp.Unspecified` 表示自动从 `CouiShape` 解析圆角半径与权重。更换为其他 `CouiShape` 时保持 `cornerRadius = Dp.Unspecified`（或传入一致的半径）；若改用非 `CouiShape` 的自定义 `Shape` 且开启材质轮廓，必须显式传入有限非负的 `cornerRadius`（轮廓权重使用 `Dialog` 预设），未开启材质时仅用于裁剪。

## 参数与行为速查

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `visible` | `null` | `Boolean?` 可选参数。传 `Boolean` 时常驻组合执行入退场动画并实时同步背景采样；省略（`null`）时由外层 `if (visible)` 控制，使用系统窗口动画。 |
| `onDismissRequest` | 必填 | 系统返回键或点击弹窗外遮罩时触发；不会自动关闭弹窗，需由宿主更新状态。 |
| `confirmAction` | 必填 | 确认按钮配置；`text` 不能为空白，使用 `Primary` 样式时必须提供 `colors`。 |
| `dismissAction` / `neutralAction` | `null` | 可选取消与第三操作；`neutralAction` 默认使用破坏性文字色。 |
| `placement` | `CouiDialogPlacement.Bottom` | 弹窗位置：底部（`Bottom`）或居中（`Center`）。 |
| `material` / `backdrop` | `null` | API 33+ 背景材质配置与窗口背景源；`material` 非空时必须同时具备 API 33+ 与有效背景源。 |
| `properties` | `usePlatformDefaultWidth = false` | `DialogProperties` 配置；`InputDialog` 额外要求保留 `decorFitsSystemWindows = true`。 |
| `autoFocus` | `true` | 仅 `InputDialog` 可用；窗口获焦后自动请求输入焦点并弹出键盘。 |

## 装配注意事项

- **自定义 `properties` 的保留项**：自定义 `DialogProperties` 时如需维持 COUI 弹窗宽度与外边距策略，请保留 `usePlatformDefaultWidth = false`；`InputDialog` 还**必须**保留 `decorFitsSystemWindows = true`，否则会抛出异常。
- **模糊半径语义**：`CouiDialogMaterial.blurRadius` 为 Fast Kawase 引擎等级（`0` 或不小于 `10`），并非高斯像素或 `sigma` 值。
- **无障碍触摸浏览**：开启系统 TalkBack 触摸浏览时，弹窗会自动禁用点击遮罩关闭（`dismissOnClickOutside`），用户可通过系统返回或无障碍操作关闭。
- **能力边界**：极小尺寸弹窗及推荐/AI 支付专用按钮暂不支持。

## 源码与完整示例

- **公开入口与预设**：[Dialog.kt](../../coui/src/main/java/com/itos/coui/dialog/Dialog.kt)、[InputDialog.kt](../../coui/src/main/java/com/itos/coui/dialog/InputDialog.kt)、[LoadingDialog.kt](../../coui/src/main/java/com/itos/coui/dialog/LoadingDialog.kt)、[CouiDialogButton.kt](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogButton.kt)、[CouiDialogDefaults.kt](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogDefaults.kt)
- **Demo 完整示例**：[DemoDialogSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)、[DemoDialogInputSample.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)
