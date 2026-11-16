# Sheet 面板

`ModalBottomSheet` 提供独立窗口的底部面板，支持受控入退场、遮罩、下拉关闭、嵌套滚动接管和键盘避让；`SheetToolbar` 为面板内部提供居中标题与两侧操作栏。这些组件及预设位于 `com.itos.coui.sheet`。

[基本用法](#基本用法) · [面板工具栏](#面板工具栏) · [状态、拖动与退出](#状态拖动与退出) · [背景材质与平台](#背景材质与平台)

## 基本用法

在页面的 Composable 中持续保留组件调用，用 `visible` 控制展开与退场，让窗口在退场动画结束后释放，不要用 `if (visible)` 包裹。下面的 `buttonLabel`、`sheetTitle`、`draft`、`saveLabel` 和 `onSave` 由页面提供：

```kotlin
var visible by rememberSaveable { mutableStateOf(false) }

Button(onClick = { visible = true }) {
    Text(buttonLabel)
}

ModalBottomSheet(
    visible = visible,
    onDismissRequest = { visible = false },
    title = sheetTitle,
    sheetHeight = 520.dp,
) {
    Text(sheetTitle)
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(draft)
    }
    TextButton(onClick = {
        onSave()
        visible = false
    }) {
        Text(saveLabel)
    }
}
```

示例中的 `Button`、`Text` 和 `TextButton` 来自 Material 3，项目需要声明该依赖；也可以使用宿主自有按钮或 Foundation 的 `BasicText`。`ModalBottomSheet` 本身不依赖 Material 3。

`title` 用于系统窗口标题与无障碍 `paneTitle`，不会自动绘制可见标题栏；需要标题栏时可在内容中放置 `SheetToolbar` 或页面自己的顶栏。

`Modifier.weight(...)` 由内容的 `ColumnScope` 直接提供。`sheetHeight` 表示窗口内的可见高度（示例中的 `520.dp` 仅作演示高度，不是官方默认），包含拖动条、内容与底部安全区；省略或传入 `null` 时按内容高度测量，均受父容器可用高度与平台上限限制。长内容由宿主放入单个纵向滚动容器，避免多层纵向滚动冲突。`modifier` 作用于面板容器本身（居中贴底并受 `CouiSheetDefaults.maxWidth` 约束），不作用于全屏遮罩。

## 面板工具栏

`SheetToolbar` 在 `ModalBottomSheet` 内容中提供居中标题和 `navigation` / `actions` 两侧操作槽。槽位参数会收到 `CouiToolbarStyle`，在 API 34 及以上可传给 [TopAppBarButton](top-app-bar.md)，使按钮使用面板内的纯色底及圆形/胶囊轮廓预设；在较低 API 上可放置宿主自己的普通按钮。

```kotlin
SheetToolbar(
    title = sheetTitle,
    navigation = { style ->
        if (Build.VERSION.SDK_INT >= 34) {
            TopAppBarButton(onClick = onCancel, text = true, style = style) {
                Text(cancelLabel)
            }
        } else {
            TextButton(onClick = onCancel) {
                Text(cancelLabel, style = style.textStyle, color = style.contentColor)
            }
        }
    },
    actions = { style ->
        if (Build.VERSION.SDK_INT >= 34) {
            TopAppBarButton(onClick = onDone, text = true, style = style) {
                Text(doneLabel)
            }
        } else {
            TextButton(onClick = onDone) {
                Text(doneLabel, style = style.textStyle, color = style.contentColor)
            }
        }
    },
)
```

示例中的 `TextButton` 与 `Text` 来自 Material 3，应用需声明相应依赖。`SheetToolbar` 必须在 `ModalBottomSheet` 内容中调用，并自动继承面板的 `dark` 设置。

标题默认使用 `18sp`、字重 `550` 与面板字体留白，最多两行，并按两侧较宽操作区对称避让；操作文字默认使用 `14dp`、字重 `500`。传给 `TopAppBarButton` 时，按钮会自动向内容插槽提供 `style.textStyle` 与内容色；若放置其他自定义按钮，需从收到的 `style` 读取 `style.textStyle` 与 `style.contentColor`。

`style` 可覆盖工具栏按钮外观，`titleStyle` 可覆盖标题排版与颜色。标题颜色依次取 `titleStyle.color`、显式 `style` 的内容色和主题标题色；`Color.Unspecified` 会继续取下一项。可选主题通过 `CouiTheme.typography.dialogTitleFontFamily` 与 `dialogButtonFontFamily` 提供字体族，默认字号、字重与行距由面板控制。

工具栏按钮会自动采样面板单独录制的纯表面源（不包含按钮自身与同层控件）。面板内的其他浮动控件（例如 `ChipTabRow`）可调用 `rememberCouiSheetSurfaceBackdrop()` 获取同一个纯背景源；在 `ModalBottomSheet` 内容之外调用该函数或 `SheetToolbar` 会抛出异常。

## 状态、拖动与退出

`visible` 是唯一的展开目标，面板内部只维护局部运动状态。返回键、遮罩点击、`dismiss` 无障碍操作以及满足关闭条件的下拉释放都会回调 `onDismissRequest`，由宿主决定何时将 `visible` 设为 `false`。遮罩默认提供本地化的关闭文案，可用 `dismissDescription` 覆盖。`dismissOnClickOutside = false` 时仅关闭遮罩点击退出，返回键与无障碍 `dismiss` 仍会请求退出。

`showDragHandle` 控制顶部拖动条显示（默认 `true`；搭配 `SheetToolbar` 等页面布局时可设为 `false`）。顶部拖动条支持直接下拉，按下时显示 `200ms` 缓动按压阴影；当 `showDragHandle = false` 时，面板内容中除 `SheetToolbar` 两侧操作槽以外的区域也可直接拖动。向下快速甩动或拖过面板中线后松手会请求关闭；未达到关闭条件、手势取消或向上释放会回到展开位置。`draggable = false` 可关闭拖动。

内容滚动到顶后，未消费的下拉输入会接管为面板拖动；上拖会先把面板送回展开位置再交给内容滚动。从 `SheetToolbar` 两侧操作槽起始的触摸保留给槽内控件处理，不触发面板拖动或嵌套接管。入退场途中改变 `visible` 会保留当前速度平滑反向，拖动接管会中断待处理的动画目标，节点移除会停止动画。

在半屏面板内容中打开 [Menu](menu.md) 时，主菜单与子菜单会自动约束在面板当前可见区域内并按可用高度滚动；从菜单或面板内另行打开的独立 `Dialog` 按其自身窗口定位。

## 背景材质与平台

默认使用不透明表面。要在 API 33 及以上启用半透明模糊材质，使用 `ProvideCouiMenuBackdrop` 为宿主页面提供不含面板与遮罩的窗口背景源，并传入 `CouiSheetDefaults.material(dark)`：

```kotlin
@Composable
fun BlurredSheetPage(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    dark: Boolean = CouiTheme.isDark,
    onDismiss: () -> Unit,
    page: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val supported = CouiDialogDefaults.isMaterialSupported()
    ProvideCouiMenuBackdrop { windowSource ->
        Box(modifier.fillMaxSize().then(if (supported) windowSource else Modifier)) {
            page()
        }
        ModalBottomSheet(
            visible = visible,
            onDismissRequest = onDismiss,
            title = title,
            dark = dark,
            material = if (supported) CouiSheetDefaults.material(dark) else null,
            content = sheetContent,
        )
    }
}
```

`material` 非空时，面板优先使用显式 `backdrop`，否则读取 `LocalCouiMenuBackdrop`；要求 **API 33 及以上**和**可用的宿主窗口源**，缺少任一条件会抛出异常。保持同一个 `ModalBottomSheet` 调用并在 `material` 与 `null` 之间切换时，可保留窗口与草稿状态，并在切为 `null` 后释放模糊图层与源消费引用。宿主源的创建与约束见[背景采样](../effects/backdrop.md)。

`dark` 默认读取 `CouiTheme.isDark`，并通过 `LocalCouiDarkTheme` 向内容提供一致模式。`colors` 默认复用 `CouiDialogDefaults.themeColors(dark)`；需要固定原生面板配色时传入 `CouiSheetDefaults.colors(dark)`。`colors.dimAmount` 控制窗口遮罩的最大透明度。

面板窗口内部会自动为后续 `Menu` 或材质 `Dialog` 提供包含外层页面、遮罩与当前面板的新窗口源，内容中通过 `rememberCouiWindowBackdrop()` 读取即可，无需再包一层 `ProvideCouiMenuBackdrop`，也不能把该源反传给当前面板自身的 `backdrop`。

`isolateContent` 默认为 `true`，保持面板内容的离屏隔离。仅当内容中的 `Clear`、`DstIn`、`DstOut` 等目标混合已自行隔离时可设为 `false`，在没有后续材质菜单或弹窗消费当前窗口源期间允许使用 `CompositingStrategy.Auto`；有消费者时仍会恢复离屏隔离并在退场后释放。具体约束见[背景源的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

### 安全区与平台范围

面板窗口内部已处理顶部边距、底部导航栏安全区与键盘避让（IME），内容中不要再次追加 `navigationBarsPadding()` 或 `imePadding()`。如果宿主页面容器本身使用了 `imePadding()`，建议按[宿主窗口焦点启用键盘避让](dialog.md#宿主页的键盘避让)处理，避免在面板内输入时让底层宿主页提前空出键盘高度。

组件基于公开 Android 窗口能力实现，目前提供展开与隐藏两个位置（对应原厂 `skipCollapsed`），暂未覆盖半折叠停靠、OEM 小折叠屏多窗协议或锚点跟手缩放弹窗。

无材质面板与 `SheetToolbar` 布局支持库最低系统版本；面板材质要求 API 33，工具栏操作槽中的 `TopAppBarButton` 要求 API 34，完整对照见[兼容要求](../guide/compatibility.md#效果门槛)。

## Demo 示例与公开 API

Demo 的“组件 → Sheet 面板”页演示闹钟编辑、倒计时编辑与半屏面板三个场景，包含背景色块与模糊开关；闹钟和倒计时中的「完成」仅在 Demo 内保存草稿并显示结果，不接入系统闹钟或计时器。

公开参数与预设见 [ModalBottomSheet / CouiSheetDefaults](../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt) 与 [SheetToolbar / rememberCouiSheetSurfaceBackdrop](../../coui/src/main/java/com/itos/coui/sheet/SheetToolbar.kt)；完整示例见 [DemoSheetSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoClockAlarmContent](../../demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt) 与 [DemoClockTimerContent](../../demo/src/main/java/com/itos/coui/demo/DemoClockTimerContent.kt)。
