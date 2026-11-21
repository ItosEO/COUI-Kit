# Sheet 面板

`BottomSheet` 提供独立窗口的底部面板，处理正文滚动、回弹、入退场、下拉关闭与键盘避让。顶部可放两个操作按钮和居中的内容。面板内容、草稿状态与确认保存逻辑由宿主持有。

- 基础面板与顶部按钮支持 API 31+。顶部按钮在 API 34+ 使用浮动材质，此前版本使用普通按钮外观。
- 半透明模糊材质要求 Android 13（API 33）及以上，并需要宿主窗口背景源。

## 基本用法

把下面的代码放进现有页面的 Composable 中。推荐在同一个 `BottomSheet` 调用里配置标题和两侧操作按钮，正文直接放入 `Group`、表单或其他控件。

~~~kotlin
val visible = rememberSaveable { mutableStateOf(false) }

Item(onClick = { visible.value = true }) {
    Text(text = "打开面板", kind = CouiTextKind.ItemTitle)
}

BottomSheet(
    visible = visible,
    leadingButton = CouiSheetButton(
        label = "取消",
        onClick = { visible.value = false },
    ),
    titleContent = {
        Text(text = "面板标题", kind = CouiTextKind.SheetTitle)
    },
    trailingButton = CouiSheetButton(
        label = "完成",
        onClick = { visible.value = false },
    ),
) {
    Text(text = "面板正文", kind = CouiTextKind.ItemSummary)
}
~~~

系统返回、点击遮罩和下拉默认关闭面板。保存表单或确认丢弃草稿后，设置 `visible.value = false`。需要拦截默认关闭操作时，传入 `onDismissRequest`。把 Sheet 放在 `if (visible.value)` 里会跳过退场动画。

`modifier` 作用于贴底居中的面板，宽度受 `CouiSheetDefaults.maxWidth` 约束。默认按内容测量高度，也可设置 `sheetHeight`，该高度包含拖动条、正文和底部安全区。横屏和键盘弹出时，面板受窗口可用高度限制。

### 滚动与回弹

- **默认行为**。直接放正文控件即可。正文上下越界时回弹，顶部操作区保持固定。短正文下方的留白可下拉关闭。需要让正文滚到顶部后也能下拉关闭时，设置 `nestedScrollEnabled = true`。
- **整页回弹**。设置 `fullPageOverscroll = true` 后，留白也可发起回弹，顶部操作区只在越界回弹时与正文一起移动。可用 `showDragHandle = true` 保留顶部下拉关闭的入口。
- **自带纵向滚动的正文**。`content` 使用整块 `LazyColumn`、`verticalScroll` 或其他纵向滚动容器时，传入 `scrollableContent = false`，把滚动交给该容器。此时容器负责自己的边缘效果。

## 顶部操作按钮与标题

文字按钮传 `label`，图标按钮传 `icon` 并设置无障碍描述。通过 `enabled` 控制按钮是否可用。`titleContent` 可放任意 Composable，使用 `CouiTextKind.SheetTitle` 时默认居中、单行省略。三个位置可按场景省略。

如果取消按钮也需要关闭确认，将同一个确认函数传给 `onDismissRequest` 和取消按钮的 `onClick`。按钮需要打开菜单时，将 `couiMenuAnchor` 挂在按钮的 `modifier` 上，菜单放在同一个 Sheet 的内容里。

## 背景模糊材质

日常接入省略 `material` 和 `backdrop`。Android 13（API 33）及以上且有宿主窗口背景源时，面板默认开启背景模糊。较低版本或缺少窗口源时使用 `colors.surface`，也可传入 `material = null` 直接使用该底色。

宿主已有窗口源时直接复用。首次接入时，在宿主窗口使用 `ProvideCouiMenuBackdrop`，把它给出的 `windowSource` 挂到一个实际绘制页面的根节点，并在同一作用域调用 Sheet。COUI `Scaffold` 自动给栏位提供的正文源独立于这个窗口源。

~~~kotlin
ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        // 放入宿主页内容和打开按钮。
    }

    BottomSheet(
        visible = visible,
        // 沿用基本用法中的标题与两侧按钮。
    ) {
        // 放入面板正文。
    }
}
~~~

### 背景源与嵌套浮层规则

- **面板材质**优先使用显式 `backdrop`，否则读取宿主提供的窗口源。这个源须排除当前面板和遮罩。在 API 33 以下或缺少窗口源时，显式传入非空 `material` 会抛出异常。详见[背景采样](../effects/backdrop.md)。
- **面板内的 [Menu](menu.md) 和材质 `Dialog`** 自动采样 Sheet 提供的窗口源，其中包含面板、遮罩和宿主背景。需要显式读取时，在 `content` 里调用 `rememberCouiWindowBackdrop()`。
- **顶部操作按钮、面板内的 `Fab` 和 `ChipTabRow`** 默认使用排除正文与按钮的面板纯背景。自定义控件可在 `content` 中调用 `rememberCouiSheetSurfaceBackdrop()` 获取这张底图。
- **面板内的 `Group` 和 `SegmentedControl`** 可直接使用默认外观。`Group` 使用主题卡片色，`SegmentedControl` 默认填色，显式传入 `backdrop` 才会使用透明模糊。
- 面板内使用 `Clear`、`DstIn` 等混合模式的自定义控件应隔离自己的绘制层。详见[菜单窗口的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

## 参数与交互行为速查

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `visible` | 必填 | 调用方持有的 `MutableState<Boolean>`。将 `value` 设为 `true` 展开，设为 `false` 退场。 |
| `onDismissRequest` | `null` | 接管返回键、点击遮罩、下拉和无障碍关闭。省略时，这些操作默认将 `visible.value` 设为 `false`。 |
| `dismissDescription` | `stringResource(R.string.coui_sheet_dismiss)` | 遮罩点击与无障碍关闭操作的默认描述文案，可按业务覆盖。 |
| `dark` | `CouiTheme.isDark` | 控制面板深浅色，并通过 `LocalCouiDarkTheme` 向面板内容提供一致模式。 |
| `sheetHeight` | `null` | `null` 时按内容测量；指定有限正数 `Dp` 时固定面板高度（含拖动条、内容与底部安全留白）。 |
| `showDragHandle` | `false` | 设为 `true` 时显示顶部拖动条。手势下拉由 `draggable` 控制。 |
| `draggable` | `true` | 允许下拉关闭面板。 |
| `nestedScrollEnabled` | `false` | 设为 `true` 后，正文到顶后的向下拖动可以关闭面板。该交接在 `draggable = true` 且 `fullPageOverscroll = false` 时生效。 |
| `fullPageOverscroll` | `false` | 设为 `true` 后，留白也可发起回弹，顶部操作区和正文一起回弹。 |
| `scrollableContent` | `true` | Sheet 负责正文滚动。使用自带纵向滚动的正文时设为 `false`。 |
| `leadingButton`、`trailingButton` | `null` | 两侧各一个 `CouiSheetButton`，随布局方向镜像。 |
| `titleContent` | `null` | 顶部居中的 Compose 内容。 |
| `dismissOnClickOutside` | `true` | 控制点击遮罩能否请求关闭。 |
| `colors` | `CouiDialogDefaults.themeColors(dark)` | 控制面板不透明底色与遮罩最大透明度（`dimAmount`）。需要固定原生面板配色时传入 `CouiSheetDefaults.colors(dark)`。 |
| `material` | API 33+ 且有窗口源时使用 `CouiSheetDefaults.material(dark)`，其余情况为 `null` | 控制面板的模糊与轮廓光。传 `null` 使用不透明表面。 |

## 装配注意事项

- **请勿重复添加系统 Insets**：面板窗口内部已处理顶部边距、底部导航栏安全区与 `IME` 键盘避让。面板内容与内嵌的 `Scaffold` **不要**再次叠加 `navigationBarsPadding()` 或 `imePadding()`。如果宿主页容器使用了 `imePadding()`，建议按[宿主页的键盘避让](dialog.md#宿主页的键盘避让)仅在宿主窗口获焦时启用。
- **面板内菜单高度自适应**：在面板内容中弹出的 [Menu](menu.md) 与子菜单会自动将面板当前可见区域作为弹出边界，并在可用高度内滚动；从面板内另行打开的独立 `Dialog` 按自身窗口定位。
- **手势与状态收尾**：开启下拉关闭时，向下快速甩动或拖过面板中线释放会请求关闭。未过关闭阈值、系统取消或节点移除会停止或回到展开位置。
- **窗口形态边界**：组件基于公开平台 `Dialog` 窗口实现，仅提供“展开”与“隐藏”两态，暂不支持半折叠停靠、OEM 小折叠屏多窗协议或锚点跟手弹出。
- **兼容性对照**：完整平台门槛对照见[兼容要求](../guide/compatibility.md#效果门槛)。

## 源码与完整示例

- **公开入口与预设**：[BottomSheet.kt](../../coui/src/main/java/com/itos/coui/sheet/BottomSheet.kt)、[CouiSheetButton](../../coui/src/main/java/com/itos/coui/sheet/SheetToolbar.kt)
- **运动实现**：[CouiSheetMotion.kt](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetMotion.kt)
- **Demo 完整示例（添加闹钟与半屏面板）**：[DemoSheetSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoClockAlarmContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt)
