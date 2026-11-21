# Sheet 面板

`BottomSheet` 提供独立窗口的底部面板，处理正文滚动、越界回弹、入退场动画、下拉关闭与键盘避让，并在顶部提供两侧操作按钮与居中标题槽位。组件位于 `com.itos.coui.sheet`，显隐状态、面板内容、草稿与确认保存逻辑均由宿主持有。

- **基础面板与顶部按钮**：支持库最低系统版本（**API 31+**）；顶部按钮在 **API 34+** 使用半透明浮动模糊材质，在 API 31–33 使用普通按钮外观。
- **半透明模糊材质与轮廓光**：要求 Android 13（**API 33**）及以上，并需要宿主提供不包含本面板的窗口背景源（`backdrop`）。

## 基本用法

将 `BottomSheet` 放入页面的 Composable 中，通过宿主持有的 `visible` 控制展开与关闭；建议在同一次调用中配置顶部两侧操作按钮与标题，正文直接放入 `Group`、表单或其他控件：

~~~kotlin
@Composable
fun BasicBottomSheetExample(
    modifier: Modifier = Modifier,
    openLabel: String,
    title: String,
    cancelLabel: String,
    doneLabel: String,
    onDone: () -> Unit,
    bodyContent: @Composable ColumnScope.() -> Unit,
) {
    val visible = rememberSaveable { mutableStateOf(false) }

    Item(modifier = modifier, onClick = { visible.value = true }) {
        Text(text = openLabel, kind = CouiTextKind.ItemTitle)
    }

    BottomSheet(
        visible = visible,
        leadingButton = CouiSheetButton(
            label = cancelLabel,
            onClick = { visible.value = false },
        ),
        titleContent = {
            Text(text = title, kind = CouiTextKind.SheetTitle)
        },
        trailingButton = CouiSheetButton(
            label = doneLabel,
            onClick = {
                onDone()
                visible.value = false
            },
        ),
        content = bodyContent,
    )
}
~~~

接入时注意以下要点：

- **常驻组合控制显隐**：`BottomSheet` 必须保留在组合中，通过修改 `visible.value` 切换展开与退场。**不要在外层再用 `if (visible.value)` 包裹**，否则关闭时会直接销毁窗口、跳过退场动画。
- **默认关闭与拦截确认**：系统返回键、点击遮罩与下拉默认将 `visible.value` 设为 `false`。需要拦截关闭（如弹出“是否放弃草稿”二次确认）时，传入 `onDismissRequest`；此时组件不再自动修改 `visible.value`，需由宿主在确认后手动设为 `false`。
- **尺寸与可用高度**：传给 `BottomSheet` 的 `modifier` 作用于贴底居中的面板（宽度受 `CouiSheetDefaults.maxWidth` 即 `540.dp` 约束）。`sheetHeight` 默认按内容测量高度；指定有限正数 `Dp` 时固定面板高度（包含顶部拖动条、标题栏、正文与底部安全区）。横屏及软键盘弹出时受窗口可用高度限制。

## 滚动、回弹与手势关闭

- **默认模式（固定顶栏 + 正文回弹）**：直接向 `content` 放入正文控件即可。面板内部负责正文纵向滚动与 COUI 越界回弹，顶部栏保持固定；短正文下方的留白区域可下拉关闭面板。需要让长正文滚动到顶部后继续下拉也能关闭面板时，设置 `nestedScrollEnabled = true`（在 `draggable = true` 且 `fullPageOverscroll = false` 时生效）。
- **整页回弹模式（`fullPageOverscroll = true`）**：由外层统一接管越界回弹，短正文下方的留白也可发起整页回弹，顶部操作区只在越界回弹期间随正文一起移动（内置拖动条保持在回弹区域之外）。此时可搭配 `showDragHandle = true` 保留顶部下拉关闭的直观入口；该模式下 `nestedScrollEnabled` 不生效。
- **自带纵向滚动的正文（`scrollableContent = false`）**：当 `content` 整块使用 `LazyColumn`、`verticalScroll` 或其他纵向滚动容器时，传入 `scrollableContent = false`，将滚动与边缘效果交由该容器自行处理，避免双层纵向滚动冲突。

## 顶部操作按钮与标题

- **两侧按钮（`CouiSheetButton`）**：`leadingButton` 与 `trailingButton` 随布局方向（LTR / RTL）自动镜像。`label`（文字按钮，最多两行省略）与 `icon`（圆形图标按钮）必须**二选一**；使用 `icon` 时请在内部图标上设置无障碍 `contentDescription`。可通过 `enabled` 控制按钮是否可点击。
- **居中标题（`titleContent`）**：可放置任意 Composable；使用 `Text(..., kind = CouiTextKind.SheetTitle)` 时默认居中、单行省略，并自动具备无障碍标题语义。两侧按钮按较宽一侧等量避让，使标题保持视觉居中。三个顶部槽位均可省略；全部省略时不占用顶部栏高度。
- **确认与菜单**：若取消按钮也需要关闭确认，可将同一个确认回调传给 `onDismissRequest` 和取消按钮的 `onClick`。把 [Menu](menu.md) 放入 `CouiSheetButton.popupContent`，菜单会以整个按钮为锚点固定弹出。

## 使用背景材质

日常接入省略 `material` 和 `backdrop` 即可：在 **API 33+** 且宿主外层已安装 `ProvideCouiMenuBackdrop` 时，面板**默认自动开启**背景模糊材质与轮廓光；在 API 33 以下或缺少窗口背景源时自动使用不透明 `colors.surface`。显式传入 `material = null` 可强制使用不透明表面。

宿主已有窗口源时直接复用；首次接入时，在宿主窗口根部安装 `ProvideCouiMenuBackdrop`，将回调给出的 `windowSource` 挂到实际绘制页面内容的根节点，并在同一作用域（源节点之外）调用 `BottomSheet`（COUI `Scaffold` 为栏位提供的正文源独立于该窗口源）：

~~~kotlin
@Composable
fun HostPageWithSheetExample(
    modifier: Modifier = Modifier,
    title: String,
    cancelLabel: String,
    doneLabel: String,
    onDone: () -> Unit,
    pageContent: @Composable (onOpenSheet: () -> Unit) -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val visible = rememberSaveable { mutableStateOf(false) }

    ProvideCouiMenuBackdrop { windowSource ->
        Box(modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().then(windowSource)) {
                pageContent { visible.value = true }
            }

            BottomSheet(
                visible = visible,
                leadingButton = CouiSheetButton(
                    label = cancelLabel,
                    onClick = { visible.value = false },
                ),
                titleContent = {
                    Text(text = title, kind = CouiTextKind.SheetTitle)
                },
                trailingButton = CouiSheetButton(
                    label = doneLabel,
                    onClick = {
                        onDone()
                        visible.value = false
                    },
                ),
                content = sheetContent,
            )
        }
    }
}
~~~

### 背景源与嵌套浮层规则

- **面板自身背景源与显式 `material` 校验**：面板材质优先使用显式 `backdrop`，否则读取宿主提供的窗口源（该源须排除当前面板和遮罩）。在 API 33 以下或缺少窗口源时，**显式传入非空 `material` 会抛出异常**；如需手动开关材质，请保持默认或参考[弹窗背景材质](dialog.md#使用背景材质)先核对支持条件。详见[背景采样](../effects/backdrop.md)。
- **面板内的 `Menu` 与材质 `Dialog`**：在面板内容中弹出的 [Menu](menu.md) 与材质 [Dialog](dialog.md) 会自动采样 `BottomSheet` 提供的窗口源（其中包含面板、遮罩和宿主背景）。需要显式读取时，在 `content` 中调用 `rememberCouiWindowBackdrop()`。
- **面板内的浮动控件**：面板内部向正文提供排除正文与按钮的面板纯背景，因此顶部操作按钮、面板内的 [FAB](fab.md) 与 [ChipTabRow](chip-tab.md) 可省略 `backdrop` 直接使用面板底图；自定义控件可在 `content` 中调用 `rememberCouiSheetSurfaceBackdrop()` 获取该底图。
- **面板内的 `Group` 与 `SegmentedControl`**：[Group](group.md) 与 [分段选择器](segmented-control.md) 可直接使用默认外观（`Group` 使用主题卡片色，`SegmentedControl` 默认普通填色）；仅当需要让 `SegmentedControl` 在面板内呈现透明模糊时，显式传入 `backdrop = rememberCouiSheetSurfaceBackdrop()`。
- **混合模式与离屏隔离**：面板内使用 `Clear`、`DstIn` 等目标混合模式的自定义控件应在自身内部隔离绘制层，详见[菜单窗口的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

## 参数与交互行为速查

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 作用于贴底居中的面板外层（最大宽度 `CouiSheetDefaults.maxWidth`）。 |
| `visible` | 必填 | 调用方持有的 `MutableState<Boolean>`。将 `value` 设为 `true` 展开，设为 `false` 退场。 |
| `onDismissRequest` | `null` | 接管返回键、点击遮罩、下拉和无障碍关闭。省略时，这些操作默认将 `visible.value` 设为 `false`。 |
| `dismissDescription` | `stringResource(R.string.coui_sheet_dismiss)` | 遮罩点击与无障碍关闭操作的默认描述文案，可按业务覆盖。 |
| `dark` | `CouiTheme.isDark` | 控制面板深浅色，并通过 `LocalCouiDarkTheme` 向面板内容提供一致模式。 |
| `sheetHeight` | `null` | `null` 时按内容测量；指定有限正数 `Dp` 时固定面板高度（含拖动条、内容与底部安全留白）。 |
| `showDragHandle` | `false` | 设为 `true` 时显示顶部拖动条；手势下拉开关由 `draggable` 控制。 |
| `draggable` | `true` | 允许下拉关闭面板。 |
| `nestedScrollEnabled` | `false` | 设为 `true` 后，正文滚动到顶后的继续向下拖动可关闭面板（仅在 `draggable = true` 且 `fullPageOverscroll = false` 时生效）。 |
| `fullPageOverscroll` | `false` | 设为 `true` 后，留白也可发起回弹，顶部操作区在越界时与正文一起回弹。 |
| `scrollableContent` | `true` | 由面板负责正文滚动与回弹；使用自带纵向滚动容器的正文时设为 `false`。 |
| `leadingButton`、`trailingButton` | `null` | 顶部两侧各一个 `CouiSheetButton`，随布局方向自动镜像。 |
| `titleContent` | `null` | 顶部居中的 Compose 内容。 |
| `dismissOnClickOutside` | `true` | 控制点击遮罩能否请求关闭。 |
| `colors` | `CouiDialogDefaults.themeColors(dark)` | 控制面板不透明底色与遮罩最大透明度（`dimAmount`）。需要固定原生面板配色时传入 `CouiSheetDefaults.colors(dark)`。 |
| `backdrop` | `null` | 显式传入面板采样窗口源；为 `null` 时读取外层 `ProvideCouiMenuBackdrop` 提供的窗口源。 |
| `material` | API 33+ 且有窗口源时使用 `CouiSheetDefaults.material(dark)`，其余为 `null` | 控制面板的模糊与轮廓光；传入 `null` 强制使用不透明表面。 |

## 装配注意事项

- **请勿重复添加系统 Insets**：面板窗口内部已处理顶部边距、底部导航栏安全区与 `IME` 键盘避让。面板内容与内嵌的 `Scaffold` **不要**再次叠加 `navigationBarsPadding()` 或 `imePadding()`。若宿主页容器使用了 `imePadding()`，建议按[宿主页的键盘避让](dialog.md#宿主页的键盘避让)仅在宿主窗口获焦时启用。
- **面板内菜单高度自适应**：在面板内容中弹出的 [Menu](menu.md) 与子菜单会自动将面板当前可见区域作为弹出边界，并在可用高度内滚动；从面板内另行打开的独立 `Dialog` 按自身窗口定位。
- **手势与状态收尾**：开启下拉关闭时，向下快速甩动或拖过面板中线释放会请求关闭。未过关闭阈值、系统 `CANCEL` 或节点移除会取消拖动或回到展开位置。
- **窗口形态边界**：组件基于公开平台 `Dialog` 窗口实现，仅提供“展开”与“隐藏”两态，暂不支持半折叠停靠、OEM 小折叠屏多窗协议或锚点跟手弹出。
- **兼容性对照**：完整平台门槛对照见[兼容要求](../guide/compatibility.md#效果门槛)。

## 源码与完整示例

- **公开入口与预设**：[BottomSheet.kt](../../coui/src/main/java/com/itos/coui/sheet/BottomSheet.kt)、[SheetToolbar.kt](../../coui/src/main/java/com/itos/coui/sheet/SheetToolbar.kt)
- **运动与材质实现**：[CouiSheetMotion.kt](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetMotion.kt)、[CouiSheetSurface.kt](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetSurface.kt)
- **Demo 完整示例（添加闹钟与半屏面板）**：[DemoSheetSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoClockAlarmContent.kt](../../demo/src/main/java/com/itos/coui/demo/DemoClockAlarmContent.kt)
