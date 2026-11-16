# Sheet 面板

`ModalBottomSheet` 提供独立窗口的底部面板，内置顶部拖动条、入退场动画、遮罩渐变、下拉关闭联动与键盘避让；`SheetToolbar` 为面板内部提供居中标题与两侧操作栏。面板内容、草稿状态与确认保存逻辑由宿主持有。

- **基础不透明面板与 `SheetToolbar` 布局**：支持库的最低系统版本（API 31+）。
- **半透明模糊材质**：要求 Android 13（API 33）及以上，并需要宿主窗口背景源。
- **`SheetToolbar` 操作槽内的 `TopAppBarButton`**：要求 Android 14（API 34）及以上（较低版本可放置读取 `style.textStyle` 与 `style.contentColor` 的宿主普通按钮）。

## 基本用法

在宿主 Composable 中**常驻调用** `ModalBottomSheet`，通过 `visible` 控制展开与收起。下面展示一个固定高度的底部编辑面板，文案、深浅色模式与状态回调均由宿主传入：

~~~kotlin
@Composable
fun DraftSheetExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    cancelDescription: String,
    draftText: String,
    saveLabel: String,
    dark: Boolean = CouiTheme.isDark,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        dismissDescription = cancelDescription,
        dark = dark,
        sheetHeight = 520.dp,
    ) {
        Text(title)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(draftText)
        }
        TextButton(onClick = onSave) {
            Text(saveLabel)
        }
    }
}
~~~

接入时注意以下三点：

1. **不要用 `if (visible)` 包裹组件**：`visible = false` 时组件会先执行退场动画，动画结束后自动释放底层 `Dialog` 窗口。如果在外层写 `if (visible) { ModalBottomSheet(...) }`，关闭时会直接销毁窗口、跳过退场动画。
2. **高度与容器约束**：`title` 仅用于系统窗口标题与无障碍 `paneTitle`，不会自动绘制可见标题栏；`modifier` 作用于面板容器本身（居中贴底并受 `CouiSheetDefaults.maxWidth` 约束），不作用于全屏遮罩。`sheetHeight` 指定具体 `Dp` 时包含顶部拖动条、正文与底部安全区；传入 `null`（默认）时按内容高度自适应，且始终受父容器可用高度与顶部留白限制（横屏及键盘弹出时同样生效）。
3. **单层纵向滚动**：`content` 槽位运行在 `ColumnScope` 中（可直接使用 `Modifier.weight(1f)`）。长内容请放入单个滚动容器（如 `verticalScroll` 或 `LazyColumn`），固定标题或底部操作区放在滚动容器外，避免嵌套多层纵向滚动。示例中的 `Text` 和 `TextButton` 来自 Material 3，可按宿主设计体系替换或使用 `BasicText`（`ModalBottomSheet` 本身不依赖 Material 3）。

## 搭配面板工具栏（`SheetToolbar`）

当面板需要居中标题与两侧操作按钮（如“取消 / 完成”）时，可将 `showDragHandle` 设为 `false`，并在面板顶部放入 `SheetToolbar`。在 API 34 及以上可将槽位收到的 `CouiToolbarStyle` 传给 [TopAppBarButton](top-app-bar.md)，较低版本放置读取 `style.textStyle` 与 `style.contentColor` 的普通按钮：

~~~kotlin
@Composable
fun SheetWithToolbarExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    cancelLabel: String,
    doneLabel: String,
    canSave: Boolean,
    dark: Boolean = CouiTheme.isDark,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    sheetBody: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
        dismissDescription = cancelLabel,
        dark = dark,
        showDragHandle = false,
    ) {
        SheetToolbar(
            title = title,
            dark = dark,
            navigation = { style ->
                if (Build.VERSION.SDK_INT >= 34) {
                    TopAppBarButton(onClick = onDismissRequest, text = true, style = style) {
                        BasicText(
                            text = cancelLabel,
                            style = LocalCouiTextStyle.current.copy(color = LocalCouiContentColor.current),
                        )
                    }
                } else {
                    TextButton(onClick = onDismissRequest) {
                        Text(cancelLabel, style = style.textStyle, color = style.contentColor)
                    }
                }
            },
            actions = { style ->
                if (Build.VERSION.SDK_INT >= 34) {
                    TopAppBarButton(onClick = onSave, enabled = canSave, text = true, style = style) {
                        BasicText(
                            text = doneLabel,
                            style = LocalCouiTextStyle.current.copy(color = LocalCouiContentColor.current),
                        )
                    }
                } else {
                    TextButton(onClick = onSave, enabled = canSave) {
                        Text(doneLabel, style = style.textStyle, color = style.contentColor)
                    }
                }
            },
        )
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            content = sheetBody,
        )
    }
}
~~~

- **槽位样式传递**：`navigation` 和 `actions` 槽位会收到适配面板内部对比度的 `CouiToolbarStyle`。API 34+ 直接传给 `TopAppBarButton` 时，按钮会向其内容插槽提供 `LocalCouiTextStyle.current` 与 `LocalCouiContentColor.current`；较低版本或放置其他自定义按钮时，请从收到的 `style` 读取 `style.textStyle` 与 `style.contentColor`。
- **标题与样式覆盖**：`SheetToolbar` 默认继承面板的 `dark` 设置，可通过 `style` 覆盖按钮外观、通过 `titleStyle` 覆盖标题排版与颜色（标题颜色依次取 `titleStyle.color`、`style?.contentColor` 与主题标题色，字体族可由 `CouiTheme.typography.dialogTitleFontFamily` 与 `dialogButtonFontFamily` 提供）。默认标题为 `18sp`、字重 `550`、最多两行，并按两侧较宽操作区对称避让以保持视觉居中；标题换行时会自然撑高工具栏。操作文字默认 `14dp`、字重 `500`。
- **使用范围**：`SheetToolbar` 必须在 `ModalBottomSheet` 内容中调用，它会自动读取面板已绘制的纯背景源（不包含按钮自身与同层控件）。若面板内容已自带其他顶栏，无需同时装配 `SheetToolbar`。

## 开启背景模糊材质（API 33+）

面板默认使用不透明表面。需要半透明模糊材质时，在宿主页面外层使用 `ProvideCouiMenuBackdrop` 录制窗口背景，并在支持平台传入 `CouiSheetDefaults.material(dark)`（保留同一个 `ModalBottomSheet` 调用并在 `material` 与 `null` 之间切换时，可保留窗口与草稿状态）：

~~~kotlin
@Composable
fun HostPageWithSheet(
    modifier: Modifier = Modifier,
    sheetVisible: Boolean,
    title: String,
    cancelLabel: String,
    dark: Boolean = CouiTheme.isDark,
    onDismissRequest: () -> Unit,
    pageContent: @Composable () -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val supported = CouiDialogDefaults.isMaterialSupported()
    ProvideCouiMenuBackdrop { windowSource ->
        Box(
            Modifier
                .fillMaxSize()
                .then(if (supported) windowSource else Modifier),
        ) {
            pageContent()
        }

        val backdrop = rememberCouiWindowBackdrop()
        val material = if (supported && backdrop != null) {
            CouiSheetDefaults.material(dark)
        } else {
            null
        }

        ModalBottomSheet(
            modifier = modifier,
            visible = sheetVisible,
            onDismissRequest = onDismissRequest,
            title = title,
            dismissDescription = cancelLabel,
            dark = dark,
            material = material,
            backdrop = backdrop,
            content = sheetContent,
        )
    }
}
~~~

### 背景源与嵌套浮层规则

- **宿主背景源边界**：当 `material != null` 时，面板优先使用显式 `backdrop`，否则读取 `LocalCouiMenuBackdrop`；该源必须是不包含本面板及遮罩的宿主窗口源。若系统低于 API 33 或未提供有效背景源，组件会直接抛出异常。详见[背景采样](../effects/backdrop.md)。
- **面板内再弹菜单或弹窗**：`ModalBottomSheet` 会自动为内部子内容建立新的窗口背景源（包含当前面板、遮罩并叠加外层宿主页）。因此在面板内容里打开 [Menu](menu.md) 或材质 `Dialog` 时，直接调用 `rememberCouiWindowBackdrop()` 即可，**无需在面板内再包一层 `ProvideCouiMenuBackdrop`，也切勿将该内部源反传给当前面板自身**。
- **面板内其他同窗口浮动控件**：若在面板内容中放置其他需要采样“面板纯背景（不含面板内文字和按钮）”的浮动控件（如 `ChipTabRow`），可在面板内容作用域内调用 `rememberCouiSheetSurfaceBackdrop()`；在 `ModalBottomSheet` 内容之外调用该函数或 `SheetToolbar` 会抛出异常。
- **离屏隔离（`isolateContent`）**：默认为 `true`。当面板内容为不透明常规布局时，可设为 `false`，在没有下级材质菜单或弹窗消费面板窗口源期间采用 `CompositingStrategy.Auto`；一旦内部菜单或弹窗打开会自动恢复离屏隔离并在退场后释放。开启前请确认内容中的 `Clear`、`DstIn`、`DstOut` 等混合已自行隔离，详见[菜单窗口的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

## 参数与交互行为速查

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `visible` | 必填 | 唯一展开目标。`true` 入场展开，`false` 退场隐藏；动画途中切换目标会保留当前运动速度平滑衔接。 |
| `onDismissRequest` | 必填 | 系统返回键、点击遮罩、手势下拉释放或无障碍关闭操作时触发。**组件不会自动修改 `visible` 或保存草稿**，由宿主在回调中决定关闭或弹窗确认。 |
| `title` | 必填 | 系统窗口标题与无障碍 `paneTitle`，不会自动绘制可见标题栏。 |
| `dismissDescription` | `stringResource(R.string.coui_sheet_dismiss)` | 遮罩点击与无障碍关闭操作的默认描述文案，可按业务覆盖。 |
| `dark` | `CouiTheme.isDark` | 控制面板深浅色，并通过 `LocalCouiDarkTheme` 向面板内容提供一致模式。 |
| `sheetHeight` | `null` | `null` 时按内容测量；指定有限正数 `Dp` 时固定面板高度（含拖动条、内容与底部安全留白）。 |
| `showDragHandle` | `true` | 是否在顶部显示拖动条（按下显示 `200ms` 按压阴影）。设为 `false` 时，内容区除 `SheetToolbar` 两侧操作槽外的区域也可直接拖动。 |
| `draggable` | `true` | 是否允许手势下拉关闭。开启时拖动条可直接拖动；内容滚到顶部后继续下拉会交接给面板，上推则优先推回展开位再滚动内容。 |
| `dismissOnClickOutside` | `true` | 是否允许点击暗色遮罩触发 `onDismissRequest`。设为 `false` 仅屏蔽遮罩点击，系统返回键与无障碍关闭仍有效。 |
| `colors` | `CouiDialogDefaults.themeColors(dark)` | 控制面板不透明底色与遮罩最大透明度（`dimAmount`）。需要固定原生面板配色时传入 `CouiSheetDefaults.colors(dark)`。 |
| `material` | `null` | 传 `CouiSheetDefaults.material(dark)` 启用 API 33+ 半透明模糊与轮廓光；切回 `null` 可释放模糊图层与源引用。 |

## 装配注意事项

- **请勿重复添加系统 Insets**：面板窗口内部已处理顶部边距、底部导航栏安全区与 `IME` 键盘避让。面板内容与内嵌的 `Scaffold` **不要**再次叠加 `navigationBarsPadding()` 或 `imePadding()`。如果宿主页容器使用了 `imePadding()`，建议按[宿主页的键盘避让](dialog.md#宿主页的键盘避让)仅在宿主窗口获焦时启用。
- **面板内菜单高度自适应**：在面板内容中弹出的 [Menu](menu.md) 与子菜单会自动将面板当前可见区域作为弹出边界，并在可用高度内滚动；从面板内另行打开的独立 `Dialog` 按自身窗口定位。
- **手势与状态收尾**：向下快速甩动或拖过面板中线释放会触发 `onDismissRequest`；未过关闭阈值、系统取消或节点移除会停止或回到展开位置，不会自行修改 `visible`。
- **窗口形态边界**：组件基于公开平台 `Dialog` 窗口实现，仅提供“展开”与“隐藏”两态（对应 `skipCollapsed`），暂未覆盖半折叠停靠、OEM 小折叠屏多窗协议或锚点跟手弹出。
- **兼容性对照**：完整平台门槛对照见[兼容要求](../guide/compatibility.md#效果门槛)。

## 源码与完整示例

- **公开入口与预设**：[ModalBottomSheet.kt](../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt)、[SheetToolbar.kt](../../coui/src/main/java/com/itos/coui/sheet/SheetToolbar.kt)
- **运动实现**：[CouiSheetMotion.kt](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetMotion.kt)
- **Demo 完整示例（闹钟、倒计时与半屏面板）**：[DemoSheetSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoClockAlarmContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt)、[DemoClockTimerContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockTimerContent.kt)
