# 消息输入栏

`com.itos.coui.messageinput.MessageInputBar` 提供小布助手式胶囊输入栏，包含文本编辑、前置操作、尾部操作和主操作。操作的图标、说明、事件及显隐规则由宿主提供。

## 基本用法

下面的 Composable 放在 API 34 及以上页面的底栏或浮层中。`backdrop` 来自宿主正文，前置与尾部操作由宿主配置，主操作的图标和本地化说明也由宿主传入。

~~~kotlin
@Composable
fun ChatComposer(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    leadingAction: CouiMessageInputAction,
    trailingAction: CouiMessageInputAction,
    primaryIcon: Painter,
    primaryDescription: String,
    submitMessage: (String) -> Unit,
    placeholder: String = "",
) {
    val draft = rememberTextFieldState()
    val submit: () -> Unit = {
        val text = draft.text.toString().trim()
        if (text.isNotEmpty()) {
            submitMessage(text)
            draft.edit { replace(0, length, "") }
        }
    }
    MessageInputBar(
        modifier = modifier,
        state = draft,
        onSubmit = submit,
        placeholder = placeholder,
        leadingAction = leadingAction,
        trailingAction = trailingAction,
        primaryAction = CouiMessageInputAction(
            icon = primaryIcon,
            contentDescription = primaryDescription,
            onClick = submit,
            enabled = draft.text.isNotBlank(),
        ),
        showLeadingAction = draft.text.isEmpty(),
        showPrimaryAction = draft.text.isNotEmpty(),
        backdrop = backdrop,
    )
}
~~~

## 状态与事件

宿主持有 `TextFieldState`。输入区使用同一个编辑节点，最多显示三行。输入转换通过 `inputTransformation` 接收 Foundation 的实际编辑事务，选区、粘贴和输入法组合区保留在同一输入状态中。程序性修改草稿使用 `state.edit`。

`onSubmit` 接收 IME 提交事件，按钮触发各自的 `onClick`。示例将主操作与 IME 事件连接到同一个宿主回调，统一处理空白校验、业务提交和草稿清空。退出编辑由宿主执行，禁用输入栏时释放编辑焦点。

`CouiMessageInputAction` 提供 `Painter` 图标、本地化 `contentDescription`、点击事件和独立的 `enabled` 状态。组件负责图标尺寸、染色、按压状态及按钮语义。输入栏整体禁用时同时禁用全部操作。

## 按钮显隐

`leadingAction`、`trailingAction` 和 `primaryAction` 分别配置前置操作、尾部操作和主操作，默认均为空。宿主通过 `showLeadingAction`、`showTrailingAction`、`showPrimaryAction` 独立控制已配置操作的显隐。显隐只控制对应按钮，IME 提交事件由 `onSubmit` 处理。

图标显隐采用淡化和缩放，尾部操作随主操作进出平移，输入区两侧留白同步变化。退场按钮立即停止响应点击并移除操作语义。移除操作配置时保留退场图标，动画结束后释放。按钮状态变化保持编辑节点与输入状态，快速反向切换从当前动画位置继续。

Demo 使用 `CouiIcons.Outlined.Camera`、`CouiIcons.Outlined.AddCircle` 和 `CouiIcons.Outlined.Send` 演示三个操作。空草稿时显示相机和加号，有草稿时显示加号和发送。三个开关保存按钮启用设置，关闭的按钮在输入、清空草稿、重新进入页面和重启应用后持续隐藏。重新打开开关后，按钮按草稿状态显示。启用设置由宿主持久保存，并与草稿显隐条件合并后传入组件。

## 外观与装配

材质与阴影预设由 `CouiMessageInputBarDefaults` 提供，组件复用公共浮动表面后端。`colors`、`textStyle`、`material` 和 `shadow` 可按宿主需求覆盖。`placeholder` 默认空字符串，宿主按需传入本地化提示。默认配色读取 `CouiTheme`。`shape` 与 `surfaceCornerRadius` 应采用相同半径和权重为 2 的平滑圆角矩形。

`backdrop` 必须覆盖不透明 SDR 正文，采样范围包含输入栏下方及模糊外沿。使用 COUI `Scaffold.bottomBar` 时可继承正文源，其他容器显式传入。每个输入栏独立持有 renderer 与内容层。创建失败的处理见[背景采样](../effects/backdrop.md#资源与失败处理)。

组件的默认高度服从父级约束，文字增多时向上增长。

### 底部放置与键盘抬升

输入栏放在宿主已有的底栏槽位，系统栏和键盘避让按宿主页的布局策略处理。宿主 Activity 使用边到边布局。`MessageInputBar` 与 `rememberCouiImeInsets` 自动将所属窗口的调整模式设为 `adjustResize`，同窗最后一个使用者退出时恢复原调整模式，窗口的其他 `softInputMode` 位随宿主当前值保留。页面通过 `windowInsetsPadding(rememberCouiImeInsets())` 缩小视口，底栏、底部对齐内容与浮动操作随可用底边抬升。窗口配置使用公开的 [Window.setSoftInputMode](https://developer.android.com/reference/android/view/Window#setSoftInputMode(int))。

下面的片段放在宿主 Composable 中。`bottomContent`、`floatingContent` 与 `pageContent` 是宿主已有的底部、浮动和正文内容。消息输入栏可以位于 `bottomContent` 中，其他内容保留原有位置。

~~~kotlin
Scaffold(
    modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(rememberCouiImeInsets()),
    bottomBar = bottomContent,
    floatingActionButton = floatingContent,
) { padding ->
    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
        pageContent()
    }
}
~~~

`rememberCouiImeInsets` 在测量时读取系统 IME 动画高度，窗口因截图浮层或输入弹窗失焦时保留最近一次占位，恢复焦点后读取当前高度。Foundation 负责 Insets 消费，底部内容各自的 `navigationBarsPadding` 处理剩余导航栏区域。占位应用于所属页面或底部容器一次，父级已消费的区域由 Foundation 扣除。输入栏底部外边距采用原厂布局的 16dp。页面级装配见[宿主页面接入](../guide/secondary-pages.md)，输入弹窗场景见[宿主页的键盘避让](dialog.md#宿主页的键盘避让)。

自动窗口配置适用于 Activity 与 Compose Dialog 宿主。窗口由当前 Compose View 定位，无法取得窗口时抛出 `IllegalStateException`。

库同时通过 `View.setScrollContainer` 声明窗口根容器可随 IME 缩小，使 Android 的首次自动模式选择使用 resize。最后一个使用者退出时恢复原容器标记。该标记的用途见 [Android 官方说明](https://developer.android.com/reference/android/view/View#setScrollContainer(boolean))。

Demo 在底栏编辑期间用同一个 `ScrollState` 保持正文末端可见。正文原本已在末端时，底部卡片随视口变化继续显示；用户滚到其他位置后，由宿主持有新的滚动位置。

## 公开入口与示例

[组件](../../coui/src/main/java/com/itos/coui/messageinput/MessageInputBar.kt) · [操作配置](../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputAction.kt) · [预设](../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputBarDefaults.kt) · [IME 占位](../../coui/src/main/java/com/itos/coui/layout/CouiImeInsets.kt) · [Demo](../../demo/src/main/java/com/itos/coui/demo/DemoMessageInputSample.kt)

文本编辑采用 Foundation 的状态式输入接口。参数与编辑事务说明见 [Android 官方文档](https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable)。
