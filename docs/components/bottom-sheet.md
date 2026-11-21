# Sheet 面板

`ModalBottomSheet` 提供独立窗口的底部面板，内置顶部拖动条、入退场动画、遮罩渐变、下拉关闭联动与键盘避让；`SheetToolbar` 为面板内部提供居中标题与两侧操作栏。面板内容、草稿状态与确认保存逻辑由宿主持有。

- **基础不透明面板与 `SheetToolbar` 布局**：支持库的最低系统版本（API 31+）。
- **半透明模糊材质**：要求 Android 13（API 33）及以上，并需要宿主窗口背景源。
- **`SheetToolbar` 操作槽内的 `TopAppBarButton`**：要求 Android 14（API 34）及以上（较低版本可放置读取 `style.textStyle` 与 `style.contentColor` 的宿主普通按钮）。

## 基本用法

在宿主中常驻调用 `ModalBottomSheet`，通过 `visible` 控制展收。`content` 按 `ColumnScope` 排版，可直接放入标题、正文或其他组件。

~~~kotlin
@Composable
fun BasicSheetExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    title: String,
    bodyText: String,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
    ) {
        Text(text = title, kind = CouiTextKind.SheetTitle)
        BasicText(bodyText)
    }
}
~~~

收到 `onDismissRequest` 后，由宿主将 `visible` 设为 `false`。保持 Sheet 在组合中，窗口会在退场动画结束后释放。示例的标题使用 `com.itos.coui.text.Text`。

`modifier` 作用于贴底居中的面板，宽度受 `CouiSheetDefaults.maxWidth` 约束。默认按内容测量高度，也可设置 `sheetHeight`，该高度包含拖动条、正文和底部安全区。横屏和键盘弹出时，面板受窗口可用高度限制。

### 滚动与回弹

长正文放进一个纵向滚动容器，例如 `LazyColumn` 或 [couiVerticalScroll](../effects/overscroll.md)。可给容器加 `Modifier.weight(1f)`，把固定标题和操作区放在容器外。

- 默认从空白处下拉可以关闭，滚动组件各自处理回弹。将 `nestedScrollEnabled` 设为 `false` 可让正文到顶后的下拉留在滚动组件内。
- `fullPageOverscroll = true` 时，从正文空白处拖动也能触发弹簧回弹。此时正文的嵌套拖动交给内容处理。若显示了拖动条，仍可从拖动条下拉关闭。

## 搭配面板工具栏（`SheetToolbar`）

需要居中标题和“取消 / 完成”等操作时，在面板顶部放入 `SheetToolbar`。API 34 及以上可将槽位收到的样式传给 [TopAppBarButton](top-app-bar.md)，较低版本可用读取槽位样式的普通按钮。以下示例的普通按钮使用 Material 3 的 `TextButton` 和 `Text`。

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

- `navigation` 和 `actions` 接收 `CouiToolbarStyle`。`TopAppBarButton` 会向自己的内容提供 `LocalCouiTextStyle` 和 `LocalCouiContentColor`，普通按钮可直接读取 `style.textStyle` 和 `style.contentColor`。
- `SheetToolbar` 继承面板的深浅色设置，并自动采样面板纯背景。用 `style` 覆盖按钮外观，用 `titleStyle` 覆盖标题排版。标题居中、单行省略，颜色依次取 `titleStyle.color`、显式传入的 `style.contentColor` 和主题标题色。标题与操作文字分别使用 `CouiTheme.typography` 的 `dialogTitleFontFamily` 和 `dialogButtonFontFamily`。

## 背景模糊材质

Android 13（API 33）及以上且有宿主窗口背景源时，面板默认开启背景模糊。宿主可用 `ProvideCouiMenuBackdrop` 提供窗口源。系统版本较低或没有窗口源时，面板使用不透明表面。传入 `material = null` 可关闭模糊。

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
    ProvideCouiMenuBackdrop { windowSource ->
        Box(Modifier.fillMaxSize().then(windowSource)) {
            pageContent()
        }

        ModalBottomSheet(
            modifier = modifier,
            visible = sheetVisible,
            onDismissRequest = onDismissRequest,
            title = title,
            dismissDescription = cancelLabel,
            dark = dark,
            content = sheetContent,
        )
    }
}
~~~

### 背景源与嵌套浮层规则

- **面板材质**优先使用显式 `backdrop`，否则读取宿主提供的窗口源。这个源须排除当前面板和遮罩。在 API 33 以下或缺少窗口源时，显式传入非空 `material` 会抛出异常。详见[背景采样](../effects/backdrop.md)。
- **面板内的 [Menu](menu.md) 和材质 `Dialog`** 自动采样 Sheet 提供的窗口源，其中包含面板、遮罩和宿主背景。需要显式读取时，在 `content` 里调用 `rememberCouiWindowBackdrop()`。
- **面板内的 `ChipTabRow` 等浮动控件**可通过 `rememberCouiSheetSurfaceBackdrop()` 取得排除正文与按钮的面板纯背景。该函数和 `SheetToolbar` 须在 `ModalBottomSheet` 的 `content` 中调用。
- **窗口源在浮层采样期间离屏绘制**，最后一个采样方退场后恢复到 `CompositingStrategy.Auto`。使用 `Clear`、`DstIn` 等混合模式的控件应隔离自己的绘制层。详见[菜单窗口的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

## 参数与交互行为速查

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `visible` | 必填 | 唯一展开目标。`true` 入场展开，`false` 退场隐藏；动画途中切换目标会保留当前运动速度平滑衔接。 |
| `onDismissRequest` | 必填 | 系统返回键、点击遮罩、手势下拉释放或无障碍关闭操作时触发。**组件不会自动修改 `visible` 或保存草稿**，由宿主在回调中决定关闭或弹窗确认。 |
| `title` | `""` | 系统窗口标题。提供非空白标题时也设置无障碍 `paneTitle`。可见标题可通过 `SheetToolbar` 添加。 |
| `dismissDescription` | `stringResource(R.string.coui_sheet_dismiss)` | 遮罩点击与无障碍关闭操作的默认描述文案，可按业务覆盖。 |
| `dark` | `CouiTheme.isDark` | 控制面板深浅色，并通过 `LocalCouiDarkTheme` 向面板内容提供一致模式。 |
| `sheetHeight` | `null` | `null` 时按内容测量；指定有限正数 `Dp` 时固定面板高度（含拖动条、内容与底部安全留白）。 |
| `showDragHandle` | `false` | 设为 `true` 时显示顶部拖动条。手势下拉由 `draggable` 控制。 |
| `draggable` | `true` | 允许通过下拉手势关闭面板。 |
| `nestedScrollEnabled` | `true` | 是否允许可滚动子内容把到顶后的向下拖动交给面板。该交接在 `draggable = true` 且 `fullPageOverscroll = false` 时生效。 |
| `fullPageOverscroll` | `false` | 开启后，正文空白处的纵向拖动也会触发整块内容回弹。 |
| `dismissOnClickOutside` | `true` | 是否允许点击暗色遮罩触发 `onDismissRequest`。设为 `false` 仅屏蔽遮罩点击，系统返回键与无障碍关闭仍有效。 |
| `colors` | `CouiDialogDefaults.themeColors(dark)` | 控制面板不透明底色与遮罩最大透明度（`dimAmount`）。需要固定原生面板配色时传入 `CouiSheetDefaults.colors(dark)`。 |
| `material` | API 33+ 且有窗口源时使用 `CouiSheetDefaults.material(dark)`，其余情况为 `null` | 控制面板的模糊与轮廓光。传 `null` 使用不透明表面。 |

## 装配注意事项

- **请勿重复添加系统 Insets**：面板窗口内部已处理顶部边距、底部导航栏安全区与 `IME` 键盘避让。面板内容与内嵌的 `Scaffold` **不要**再次叠加 `navigationBarsPadding()` 或 `imePadding()`。如果宿主页容器使用了 `imePadding()`，建议按[宿主页的键盘避让](dialog.md#宿主页的键盘避让)仅在宿主窗口获焦时启用。
- **面板内菜单高度自适应**：在面板内容中弹出的 [Menu](menu.md) 与子菜单会自动将面板当前可见区域作为弹出边界，并在可用高度内滚动；从面板内另行打开的独立 `Dialog` 按自身窗口定位。
- **手势与状态收尾**：向下快速甩动或拖过面板中线释放会触发 `onDismissRequest`；未过关闭阈值、系统取消或节点移除会停止或回到展开位置，不会自行修改 `visible`。
- **窗口形态边界**：组件基于公开平台 `Dialog` 窗口实现，仅提供“展开”与“隐藏”两态，暂不支持半折叠停靠、OEM 小折叠屏多窗协议或锚点跟手弹出。
- **兼容性对照**：完整平台门槛对照见[兼容要求](../guide/compatibility.md#效果门槛)。

## 源码与完整示例

- **公开入口与预设**：[ModalBottomSheet.kt](../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt)、[SheetToolbar.kt](../../coui/src/main/java/com/itos/coui/sheet/SheetToolbar.kt)
- **运动实现**：[CouiSheetMotion.kt](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetMotion.kt)
- **Demo 完整示例（闹钟、倒计时与半屏面板）**：[DemoSheetSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoClockAlarmContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt)、[DemoClockTimerContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockTimerContent.kt)
