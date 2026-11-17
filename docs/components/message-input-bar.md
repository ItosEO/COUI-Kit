# 消息输入栏

`MessageInputBar` 提供小布助手式底部输入栏，自带胶囊编辑区、渐变遮罩和底部留白。宿主持有草稿、提交逻辑及操作按钮配置。

- **最低系统版本**：要求 **API 34+**；支持更低版本的应用需在外层判断版本并提供替代输入 UI。
- **前置依赖**：需要不包含输入栏自身的正文背景源 `backdrop`。放入 COUI `Scaffold.bottomBar` 时可省略，其他容器需显式提供。

## 基本用法

下面的示例将输入法“发送”与发送按钮连接到同一个回调，统一判空、提交和清空草稿。将它放入页面底栏；图标、提示文案和本地化操作说明由宿主提供。

~~~kotlin
@RequiresApi(34)
@Composable
fun ChatComposer(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
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
        backdrop = backdrop,
        placeholder = placeholder,
        primaryAction = if (draft.text.isNotEmpty()) CouiMessageInputAction(
            icon = primaryIcon,
            contentDescription = primaryDescription,
            onClick = submit,
            enabled = draft.text.isNotBlank(),
        ) else null,
    )
}
~~~

## 状态与事件

通过 `rememberTextFieldState()` 创建草稿，程序性修改使用 `state.edit`。`inputTransformation` 用于过滤用户输入或限制字数，不处理程序性修改。

| 用户操作 | 组件行为 | 宿主职责 |
| --- | --- | --- |
| 编辑文字 | 更新传入的 `TextFieldState`，保留选区和输入法组合区 | 按需读取草稿或配置输入转换 |
| 点击输入法“发送” | 触发 `onSubmit()` | 校验、提交，并决定是否清空草稿或退出编辑 |
| 点击操作按钮 | 触发该按钮的 `onClick()` | 处理对应操作；发送按钮可与 `onSubmit` 共用回调 |
| 设置 `enabled = false` | 停止编辑和全部按钮交互，释放自身焦点并收起键盘 | 草稿由宿主保留，不会自动清空 |

### 提示切换与焦点

需要在激活前后显示不同提示时，传入记住的 `MutableInteractionSource`，通过 `collectIsFocusedAsState()` 观察文本框焦点，再将对应的本地化字符串传给 `placeholder`。`interactionSource` 只关联编辑区，不包含操作按钮；省略时由 Foundation 在内部管理交互。

Demo 在草稿为空、文本框未获焦且键盘隐藏时显示待输入文案，其余状态使用编辑文案。键盘隐藏或草稿清空后，若窗口仍有焦点且草稿为空、键盘已隐藏，会清除编辑焦点；有草稿时收起键盘保留焦点。完整处理见下方 Demo 链接。

点击输入栏外收起键盘也由宿主页处理。Demo 在页面的 `PointerEventPass.Initial` 观察首次按下，排除整个底栏区域后清除焦点并隐藏键盘，保留草稿。观察器不消费事件，正文仍可点击和滚动；有附件面板时，应将面板一并排除。

## 按钮显隐

每个操作通过 `CouiMessageInputAction` 提供 `Painter` 图标、本地化 `contentDescription`、`onClick` 和独立的 `enabled` 状态。组件统一处理图标尺寸、染色、按压反馈及按钮语义。

| 参数 | 位置 | Demo 用途 |
| --- | --- | --- |
| `leadingAction` | 编辑区前方 | 相机 |
| `trailingAction` | 编辑区尾部 | 加号 |
| `primaryAction` | 主操作位置 | 发送 |

传入配置时显示，传 `null` 时隐藏，组件不会自行隐藏已配置的按钮。图标与文字留白随动画过渡；退场时立即停止点击和无障碍操作，保留图标直到动画结束。切换按钮不会重建编辑区，快速反向切换从当前动画位置继续。按钮显隐不影响 IME 的 `onSubmit`。

Demo 在空草稿时显示相机和加号，有草稿时显示加号和发送。三个开关的偏好由 Demo 持久保存，再与草稿条件合并决定是否传入操作配置。

## 提示文字与排版

普通接入只需传入 `placeholder` 字符串。提示仅在草稿为空时显示，默认单行并在末尾显示省略号；空字符串不显示提示。输入正文默认最多显示三行高度，超出后滚动，保留完整内容。

| 参数 | 用途 |
| --- | --- |
| `textStyle` | 合并覆盖编辑文字的字体、字号、字重和行高 |
| `placeholderStyle` | 合并覆盖提示文字的排版，与编辑文字独立 |
| `placeholderMaxLines` | 提示最大行数，必须大于 0，否则抛出 `IllegalArgumentException` |
| `placeholderOverflow` | 提示超出可见行数后的省略或裁剪方式 |
| `lineLimits` | 使用 `TextFieldLineLimits` 选择编辑区的单行模式或多行可见高度，不限制草稿总字数 |

样式只覆盖显式指定的属性。例如只传入 `fontFamily` 时，字号、字重和行高继续使用组件预设。文字颜色统一由 `colors.content` 和 `colors.hint` 控制，两个 `TextStyle` 中的颜色不参与覆盖；编辑文字还会应用禁用态透明度。

需要提示换成自定义字体并最多显示两行时，在 `MessageInputBar` 调用中传入以下参数。`hint` 来自宿主字符串资源，`customFont` 是宿主的 `FontFamily`：

~~~kotlin
placeholder = hint,
placeholderStyle = TextStyle(fontFamily = customFont),
placeholderMaxLines = 2,
placeholderOverflow = TextOverflow.Ellipsis,
~~~

多行提示会参与输入栏高度测量，开始输入后提示消失，高度由编辑内容决定。提示行数与编辑区的 `lineLimits` 独立。

默认编辑字号不随系统字体缩放，提示字号随系统字体缩放；编辑区上下留白依据字体缩放与编辑文字的实际行数调整。覆盖一侧排版不会修改另一侧。

## 外观与装配

- **主题与材质**：默认读取 `CouiTheme`，通常无需传入 `dark`。`colors` 和 `material` 可覆盖预设；阴影按当前主题和有效光晕状态自动选择。
- **形状**：`shape` 接收 `CouiShape`，只控制胶囊的裁剪与材质几何，两者共用半径和权重。
- **布局范围**：`modifier` 作用于包含遮罩、留白和剩余导航栏占位的整个底部区域。`onGloballyPositioned` 取得完整区域的边界，外部 `padding` 则作用于该区域之外。胶囊周围的预设留白由组件内部提供，高度随文字增长并服从父级约束。

模糊与描边跟随系统材质设置。关闭模糊时，胶囊使用对应深浅色背景填充；省电模式同时关闭模糊、描边和外光晕。`contourGlowEnabled = false` 可额外关闭描边与光晕。没有 Oplus 材质设置的系统默认开启这些效果。

### 底部渐变遮罩

遮罩覆盖胶囊周围的留白，并延伸到导航栏底部，与胶囊共用主题和系统材质状态。开启模糊时使用原厂深浅色渐变；关闭模糊后，浅色保留对应渐变，深色使用当前主题背景色填充。

### 背景源与绘制器

- **采样范围**：`backdrop` 需覆盖输入栏下方及模糊外沿的不透明 SDR 内容，并排除输入栏自身。下方若有独立浮层，也应纳入背景源，装配方法见[背景采样](../effects/backdrop.md)。
- **遮罩合成**：组件将单独录制的遮罩叠加到正文源，再交给胶囊采样。输入文字、按钮和阴影不进入遮罩源。
- **实例与失败处理**：每个输入栏默认创建独立的单主体 `renderer`。需要显示创建失败的错误 UI 时，宿主可提前创建 `CouiFloatingRenderer.standalone()`、捕获失败后再决定是否显示输入栏。绘制器不可跨节点共享，详见[资源与失败处理](../effects/backdrop.md#资源与失败处理)。

## 底部放置与键盘抬升

将输入栏放入页面底栏。**组件处理剩余底部导航栏占位，宿主页处理 IME 避让和其余安全区**；导航栏区域使用渐变末端颜色填充。

宿主 Activity 使用边到边布局，并在页面容器应用一次 IME 占位。下方片段中的 `bottomContent` 包含输入栏，`floatingContent` 与 `pageContent` 是宿主已有的浮动和正文内容：

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

页面视口随键盘动画缩小，底栏、底部对齐内容与浮动操作随可用底边抬升。Foundation 扣除父级已消费的 Insets，输入栏只补足剩余导航栏占位。窗口因小窗等浮层失焦后，IME 占位仍随键盘收起而减小至零，不会被其他窗口的键盘重新抬高。

`MessageInputBar` 与 `rememberCouiImeInsets` 自动配置所属窗口为 `adjustResize`，同窗最后一个使用者退出时恢复原调整模式，其他 `softInputMode` 位仍由宿主管理。支持 Activity 和 Compose Dialog；无法取得所属窗口时抛出 `IllegalStateException`。

正文滚动跟随由宿主决定。Demo 在正文原本已处于末端时保持末端可见，用户滚到其他位置后保留其滚动位置。页面装配见[宿主页面接入](../guide/secondary-pages.md)，输入弹窗场景见[宿主页的键盘避让](dialog.md#宿主页的键盘避让)。

## 源码与完整示例

- **组件实现**：[MessageInputBar.kt](../../coui/src/main/java/com/itos/coui/messageinput/MessageInputBar.kt)
- **操作与外观预设**：[CouiMessageInputAction.kt](../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputAction.kt)、[CouiMessageInputBarDefaults.kt](../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputBarDefaults.kt)
- **IME 占位**：[CouiImeInsets.kt](../../coui/src/main/java/com/itos/coui/layout/CouiImeInsets.kt)
- **完整 Demo**：[DemoMessageInputSample.kt](../../demo/src/main/java/com/itos/coui/demo/DemoMessageInputSample.kt)

文本编辑采用 Foundation 的状态式输入接口。参数与编辑事务说明见 [Android 官方文档](https://developer.android.com/reference/kotlin/androidx/compose/foundation/text/BasicTextField.composable)。
