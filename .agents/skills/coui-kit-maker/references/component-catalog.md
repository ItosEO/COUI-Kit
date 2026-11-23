# 组件全目录与真实示例（docs/components 全部条目）

本分册逐一列出 COUI-Kit 当前全部公开组件、源码入口、平台门槛与仓库文档中的真实用法示例。示例均摘自对应 `docs/components/*.md`，未改写 API。制作新组件时：先在此目录确认是否已有可复用实现，再按 `references/coui-core.md` 的流程扩展。

## 0. 索引

| 组件 / 入口 | 包 / 源码 | 平台 | 文档 |
| --- | --- | --- | --- |
| `Scaffold` | `layout/Scaffold.kt` | API 31+ | `docs/components/scaffold.md` |
| `TopAppBar`、`TopAppBarSurface`、`TopAppBarButton`、`TopAppBarButtonGroup`、`TopAppBarActionVisibility`、`ProvideCouiTopAppBarBlur` | `topappbar/TopAppBar.kt`、`TopAppBarSurface.kt`、`TopAppBarButton.kt`、`TopAppBarButtonGroup.kt`、`CouiTopAppBarButtonDefaults.kt` | 容器 31+；按钮 34+ | `docs/components/top-app-bar.md` |
| `LargeTopAppBar`、`CollapsingTopAppBar`、`rememberCouiCollapsingAppBarState` | `topappbar/TopAppBar.kt`、`CollapsingTopAppBar.kt`、`CouiCollapsingAppBarState.kt`、`CouiTopAppBarDefaults.kt` | 容器 31+ | `docs/components/collapsing-app-bar.md` |
| `TopAppBarSearch`、`TopAppBarSearchButton`、`TopAppBarSearchAction`、`rememberCouiTopAppBarSearchState` | `topappbar/TopAppBarSearch.kt`、`TopAppBarSearchPair.kt`、`CouiTopAppBarSearchState.kt` | API 34+ | `docs/components/search.md` |
| `SearchBar`、`rememberCouiSearchBarState` | `search/SearchBar.kt`、`CouiSearchBarState.kt`、`CouiSearchBarDefaults.kt` | API 31+（34+ 浮动材质） | `docs/components/search-bar.md` |
| `NavigationBar`、`CouiNavigationItem` | `navigation/NavigationBar.kt` | API 34+ | `docs/components/navigation-bar.md` |
| `Fab`、`CouiFabDefaults`、`CouiFabColor` | `floatingactionbutton/Fab.kt`、`CouiFabDefaults.kt`、`CouiFabColor.kt` | API 34+ | `docs/components/fab.md` |
| `Group`、`Item`、`Divider`、`Footer`、`CouiPageDefaults`、`com.itos.coui.text.Text`、`CouiTextKind` | `layout/CouiPageComponents.kt`、`text/Text.kt` | API 31+ | `docs/components/group.md`、`item.md`、`divider.md`、`footer.md` |
| `Switch`、`CouiSwitchDefaults` | `switch/Switch.kt` | API 31+ | `docs/components/switch.md` |
| `Checkbox`、`CouiCheckboxDefaults`、`RadioButton`、`CouiRadioButtonDefaults` | `checkbox/Checkbox.kt`、`radiobutton/RadioButton.kt` | API 31+ | `docs/components/checkbox.md` |
| `Slider`、`CouiSliderDefaults` | `slider/Slider.kt`、`CouiSliderDefaults.kt` | API 31+ | `docs/components/slider.md` |
| `ChipTabRow`、`ChipTab`、`ChipTabButton`、`CouiChipTabItem`、`CouiChipTabDefaults` | `chip/ChipTabRow.kt`、`ChipTab.kt`、`CouiChipTabDefaults.kt` | API 34+ | `docs/components/chip-tab.md` |
| `SegmentedControl`、`CouiSegmentedControlDefaults` | `segmentbutton/SegmentedControl.kt`、`CouiSegmentedControlDefaults.kt` | API 34+ | `docs/components/segmented-control.md` |
| `MessageInputBar`、`CouiMessageInputAction`、`CouiMessageInputBarDefaults`、`rememberCouiImeInsets` | `messageinput/MessageInputBar.kt`、`CouiMessageInputAction.kt`、`layout/CouiImeInsets.kt` | API 34+ | `docs/components/message-input-bar.md` |
| `Menu`、`CouiMenuItem<T>`、`CouiMenuStyle`、`rememberCouiMenuAnchor` | `menu/Menu.kt`、`CouiMenuStyle.kt`、`CouiMenuAnchor.kt`、`CouiMenuBackdrop.kt` | 基础 31+；Fast Kawase/轮廓光 33+ | `docs/components/menu.md` |
| `Dialog`、`CouiDialogAction`、`CouiDialogDefaults`、`InputDialog`、`LoadingDialog` | `dialog/Dialog.kt`、`InputDialog.kt`、`LoadingDialog.kt`、`CouiDialogDefaults.kt` | 基础 31+；材质 33+ | `docs/components/dialog.md` |
| `BottomSheet`、`CouiSheetButton`、`CouiSheetDefaults` | `sheet/BottomSheet.kt`、`SheetToolbar.kt` | 基础/按钮 31+；材质 33+；按钮浮动材质 34+ | `docs/components/bottom-sheet.md` |
| `WheelPicker`、`CalendarPicker`、`CouiPickerDefaults`、`CouiCalendarDefaults`、`couiPickerSelectionLines` | `picker/WheelPicker.kt`、`CalendarPicker.kt`、`CouiPickerSelectionLines.kt` | API 31+ | `docs/components/pickers.md` |
| `CouiIcons`（Filled/Outlined/Bitmap） | `coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt` | API 31+ | `docs/components/icons.md` |

## 1. Scaffold（页面容器）

要点（`scaffold.md`）：API 31+；仅依赖 Compose UI/Foundation；自动录制正文视口 + `containerColor` 作为 `topBar`/`bottomBar`/`floatingActionButton` 默认背景源，正文子树不获得自身源；要求父级有界约束；`windowInsets` 默认 `systemBars ∪ displayCutout`；`floatingActionButton` 槽位不改变正文 `padding`。

```kotlin
@Composable
fun BasicScaffoldPageExample(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<String>,
) {
    CouiTheme {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { BasicText(title, style = LocalCouiTextStyle.current) },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                items(entries) { entry ->
                    Group {
                        Item {
                            Text(text = entry, kind = CouiTextKind.ItemTitle)
                        }
                    }
                }
            }
        }
    }
}
```

放置 FAB（API 34+，`padding(24.dp)` + `align(BottomEnd)`；多个 FAB 时每个传 `foregroundComposition = true`）见 `scaffold.md#放置-fab`。

## 2. TopAppBar / TopAppBarSurface / TopAppBarButton

要点（`top-app-bar.md`）：容器 31+，按钮/顶栏搜索 34+；`Modifier` 不重复加顶部安全区；`TopAppBarButton` 默认 `CouiTopAppBarButtonDefaults.style()`，文本按钮 `text = true` 为最小宽 60dp/高 40dp 胶囊，禁用时透明度 0.38f；`TopAppBarButton` 每个按钮独立 `renderer`（默认 `remember { CouiFloatingRenderer.standalone() }`）；`TopAppBarActionVisibility` 默认 `animateSize = true`、`durationMillis = 280`。

模糊后端（同页「接入说明」）：Android 13+ 默认走 Compose 实现；传 `useCouiBlur = true` 切到 COUI 后端，`ProvideCouiTopAppBarBlur(useComposeBlur = false)` 可为多个顶栏设同一默认值（显式传参优先）；两种后端共用参考半径、清晰端与渐隐位置。

```kotlin
@RequiresApi(34)
@Composable
fun PageToolbarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(),
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        backdrop = backdrop,
        title = { Text(title) },
        actions = {
            TopAppBarButtonGroup {
                TopAppBarButton(onClick = onAction, text = true, style = style) {
                    Text(actionLabel)
                }
            }
        },
    )
}
```

自定义表面 `TopAppBarSurface` 不消费安全区，内部需自行 `statusBarsPadding()`：

```kotlin
@RequiresApi(34)
@Composable
fun CustomToolbarActionExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onAction: () -> Unit,
    actionContent: @Composable () -> Unit,
) {
    TopAppBarSurface(modifier = modifier, backdrop = backdrop) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            TopAppBarButtonGroup {
                TopAppBarButton(onClick = onAction) {
                    actionContent()
                }
            }
        }
    }
}
```

背景参数（`TopAppBarSurface`）：`fadeHeight` 默认 96.dp、`referenceBlurRadius` 默认 10.8.dp、`bottomExtension` 默认 12.dp、`topExtension` 默认 `{ 0.dp }`、`backgroundAlpha` 默认 1f、`gradientColor` 未指定时用页面底色、`gradientSpeed` 默认 null（API 33+ 有效）。（`top-app-bar.md#背景参数`）

## 3. LargeTopAppBar / CollapsingTopAppBar

要点（`collapsing-app-bar.md`）：三步接线——`exitUntilCollapsedScrollBehavior(canScroll)`、`nestedScroll` 挂到滚动容器单个祖先、正文用 `behavior.state.contentPadding(padding, density)` 补偿顶部且 `consumeWindowInsets(padding)` 传原始 padding；默认展开 40.sp、收起 18.sp（有导航按钮）或 24.sp；高度默认 110.dp / 52.dp（不含安全区）。

```kotlin
@Composable
fun LargeTopAppBarPageExample(
    modifier: Modifier = Modifier,
    title: String,
    background: Color,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scroll = rememberScrollState()
    val behavior = CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        canScroll = { scroll.maxValue > 0 },
    )
    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        containerColor = background,
        topBar = {
            LargeTopAppBar(
                title = { BasicText(title, style = LocalCouiTextStyle.current) },
                scrollBehavior = behavior,
            )
        },
    ) { padding ->
        val contentPadding = remember(behavior.state, padding, density) {
            behavior.state.contentPadding(padding, density)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(contentPadding)
                .consumeWindowInsets(padding),
        ) {
            content()
        }
    }
}
```

`CollapsingTopAppBar` 与 `SearchBar` 联动时共用一个 `rememberCouiSearchBarState(expanded)`，把 `header.collapsedFraction` 传给 `SearchBar.shadowProgress`，并按 `search.active` / `contentAlpha` / `resultsAlpha` 控制正文与结果层；完整示例见 `collapsing-app-bar.md#联动常驻搜索collapsingtopappbar`。纯标题模式必须省略 `searchBar` 或传 `null`（传 `{}` 仍按搜索布局测量）。

## 4. TopAppBarSearch（顶栏搜索）

要点（`search.md`）：API 34+；必须位于 `TopAppBarSurface`/`TopAppBar`/`LargeTopAppBar` 内；`TopAppBarSearch` 与 `TopAppBarSearchButton` 共享同一个 `CouiTopAppBarSearchState`；`TopAppBarSearch` 占满可用宽度，需 `Modifier.weight(1f)`；`action`/`onActionClick` 用于从胶囊分离的右侧动作，槽内不要再套按钮或 `clickable`。

```kotlin
@RequiresApi(34)
@Composable
fun SearchToolbarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(),
    searchStyle: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(search = true),
    title: String,
    placeholder: String,
    searchDescription: String,
    closeDescription: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var focusPending by remember { mutableStateOf(false) }
    val state = rememberCouiTopAppBarSearchState(expanded)
    val keyboard = LocalSoftwareKeyboardController.current
    val close = {
        expanded = false
        focusPending = false
        keyboard?.hide()
        Unit
    }
    BackHandler(enabled = expanded, onBack = close)

    TopAppBarSurface(modifier = modifier, backdrop = backdrop) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopAppBarSearch(
                modifier = Modifier.weight(1f),
                state = state,
                style = searchStyle,
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                placeholder = placeholder,
                focusPending = focusPending,
                onFocused = { focusPending = false },
                trailingIcon = {
                    IconButton(onClick = close) {
                        Icon(Icons.Default.Close, contentDescription = closeDescription)
                    }
                },
            ) {
                Text(title)
            }
            TopAppBarButtonGroup {
                TopAppBarSearchButton(
                    state = state,
                    style = style,
                    onClick = {
                        expanded = true
                        focusPending = true
                    },
                    contentDescription = searchDescription,
                )
            }
        }
    }
}
```

## 5. SearchBar（常驻搜索）

要点（`search-bar.md`）：API 31+，31–32 普通填色、33 填色 + 跟手光、34+ 模糊材质与双主体融合；`rememberCouiSearchBarState(expanded)` 必须保持稳定组合位置（不要以 `expanded` 作 key）；取消按钮移动中即可点击；API 34+ 创建 renderer 失败会向上传播，当前无公开 `renderer` 参数与内置降级。

```kotlin
@Composable
fun PersistentSearchBarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    query: String,
    expanded: Boolean,
    searchLabel: String,
    cancelLabel: String,
    clearLabel: String,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: () -> Unit,
) {
    val search = rememberCouiSearchBarState(expanded)
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    SearchBar(
        modifier = modifier,
        state = search,
        query = query,
        onQueryChange = onQueryChange,
        onExpandedChange = onExpandedChange,
        placeholder = searchLabel,
        cancelText = cancelLabel,
        clearDescription = clearLabel,
        backdrop = backdrop,
        onSearch = onSearch,
    )
}
```

状态属性：`expanded`、`active`、`contentAlpha`(0..1)、`resultsAlpha`(0..1)（`search-bar.md#与页面内容联动`）。

## 6. NavigationBar（底栏）

要点（`navigation-bar.md`）：API 34+；`items` 非空且 `selectedIndex` 在 `items.indices` 内，否则抛异常；默认各自创建三主体 `CouiFloatingRenderer()`，**不能**用 `standalone()` 或跨节点共享；左右附件 `leftVisible`/`rightVisible` 为 true 时必须同时给图标与无障碍描述；右侧图标固定白色。

```kotlin
@RequiresApi(34)
@Composable
fun BasicNavigationBarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    items: List<CouiNavigationItem>,
    selectedIndex: Int,
    onSelection: (Int) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        backdrop = backdrop,
        items = items,
        selectedIndex = selectedIndex,
        onSelection = onSelection,
    )
}
```

带左右附件的完整装配（`CouiIcons.Outlined/Filled`、`CouiFabDefaults.containerColor(CouiFabColor.Notes, dark)`、`themeTint`、`circleShape`/`capsuleShape`）见 `navigation-bar.md#搭配左右附件与自定义配色`。

## 7. Fab

要点（`fab.md`）：API 34+；`onClick` 必传；默认 `CouiFloatingRenderer.standalone()`，每实例独立；`foregroundComposition = true` 用于相邻多 FAB；内容色默认日夜不透明白色；预设 `Notes`/`Phone`/`InfraredRemote`/`Recorder`。

```kotlin
@RequiresApi(34)
@Composable
fun BasicFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        content = content,
    )
}
```

配色预设（同一 `color` 同时给 material 与 shadow）：

```kotlin
@RequiresApi(34)
@Composable
fun PresetFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    preset: CouiFabColor,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = CouiFabDefaults.containerColor(preset, dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current

    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        dark = dark,
        shape = CouiFabDefaults.circleShape,
        material = CouiFabDefaults.material(color, dark),
        shadow = CouiFabDefaults.shadow(color, density, contour),
        content = content,
    )
}
```

## 8. Group / Item / Divider / Footer

要点：`Group` 不安装滚动、不消费 Insets、不创建背景源；`Group.colors` 不向下覆盖子组件主题，`Text`/`Divider` 需显式传 `colors.title`/`summary`/`divider`；`Item` 传 `onClick` 时整行可点并带 `Role.Button`，省略时由宿主组合 `toggleable`/`selectable`；`Item.enabled` 不影响 `trailing` 内子控件；`Divider` 默认 `startIndent = endIndent = CouiPageDefaults.rowPadding`，图标行对齐需自行计算；`Footer` 水平 32.dp、垂直 8.dp 内置留白。（`group.md`/`item.md`/`divider.md`/`footer.md`）

```kotlin
@Composable
fun SettingsGroupExample(
    modifier: Modifier = Modifier,
    groupTitle: String,
    rowTitle: String,
    footerText: String,
    onRowClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    Group(
        modifier = modifier,
        title = groupTitle,
        footer = footerText,
        colors = colors,
    ) {
        Item(onClick = onRowClick) {
            Text(
                text = rowTitle,
                kind = CouiTextKind.ItemTitle,
                color = colors.title,
            )
        }
    }
}
```

```kotlin
@Composable
fun AboutRowExample(
    modifier: Modifier = Modifier,
    groupTitle: String,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    Group(
        modifier = modifier,
        title = groupTitle,
        colors = colors,
    ) {
        Item(
            summary = {
                Text(
                    text = summary,
                    kind = CouiTextKind.ItemSummary,
                    color = colors.summary,
                )
            },
            onClick = onClick,
        ) {
            Text(
                text = title,
                kind = CouiTextKind.ItemTitle,
                color = colors.title,
            )
        }
    }
}
```

```kotlin
@Composable
fun GroupedDividerExample(
    modifier: Modifier = Modifier,
    firstTitle: String,
    secondTitle: String,
) {
    Group(modifier = modifier) {
        Item {
            Text(text = firstTitle, kind = CouiTextKind.ItemTitle)
        }
        Divider()
        Item {
            Text(text = secondTitle, kind = CouiTextKind.ItemTitle)
        }
    }
}
```

```kotlin
@Composable
fun SectionFooterExample(
    modifier: Modifier = Modifier,
    description: String,
) {
    Footer(
        modifier = modifier,
        text = description,
    )
}
```

## 9. Switch

要点（`switch.md`）：API 31+；外部传 `onCheckedChange` 时自动扩到 ≥48dp 并加 `Role.Switch`；整行模式把 `onCheckedChange = null` 并共享 `MutableInteractionSource`；`loading = true` 屏蔽点击/拖动并通过 `loadingDescription`（`LiveRegionMode.Polite`）播报；CANCEL/父容器接管不提交，滑块回到宿主 `checked`。

```kotlin
@Composable
fun NotificationSwitchExample(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    dark: Boolean,
    accent: Color,
) {
    Switch(
        modifier = modifier.semantics { contentDescription = description },
        checked = checked,
        onCheckedChange = onCheckedChange,
        dark = dark,
        colors = CouiSwitchDefaults.colors(accent, dark),
    )
}
```

整行点击（`toggleable` + `Role.Switch`，`onDragCheckedChange` 决定是否保留拖动）：见 `switch.md#整行点击`。

## 10. Checkbox / RadioButton

要点（`checkbox.md`）：纯绘制组件，24dp 基准等比缩放，热区与语义由外层提供；单选外层 `selectableGroup()` + `selectable` + `Role.RadioButton`；复选整行 `toggleable` + `Role.Checkbox`，`indication = null`；独立复选框用 ≥48dp `Box` 包裹并 `semantics`；`Checkbox` 的 `shape` 默认 `CouiShape(4.dp)`、`borderShape` 默认 `CouiShape(3.2.dp)`。

```kotlin
@Composable
fun RadioButtonGroupExample(
    modifier: Modifier = Modifier,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true,
) {
    Column(modifier.selectableGroup()) {
        labels.forEachIndexed { index, label ->
            val selected = selectedIndex == index
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, Modifier.weight(1f))
                RadioButton(selected = selected, enabled = enabled)
            }
        }
    }
}
```

```kotlin
@Composable
fun CheckboxRowExample(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
    enabled: Boolean = true,
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Checkbox(
            modifier = Modifier.size(24.dp),
            checked = checked,
            enabled = enabled,
            shape = CouiShape(4.dp),
            borderShape = CouiShape(3.2.dp),
            colors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
        )
    }
}
```

## 11. Slider

要点（`slider.md`）：API 31+；内置最小尺寸 144.dp × 48.dp；`value`/`valueRange` 必须有限，`steps` 为端点间等分点数，`showTickMarks` 独立；`onValueChange` 实时、`onValueChangeFinished` 结束时提交；参数非法在组合阶段抛 `IllegalArgumentException`。

```kotlin
@Composable
fun VolumeSliderExample(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
        shape = CouiCapsuleShape,
    )
}
```

分段 + 结束提交（`valueRange = 0f..4f`、`steps = 3` → 5 个可选值）：

```kotlin
@Composable
fun SectionSliderExample(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onSaveValue: () -> Unit,
    dark: Boolean,
    accent: Color,
) {
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onSaveValue,
        valueRange = 0f..4f,
        steps = 3,
        showTickMarks = true,
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
    )
}
```

## 12. ChipTabRow

要点（`chip-tab.md`）：API 34+；`items` 的 `key` 唯一，非空时 `selectedKey` 必须在 items 内、空列表必须 null；`ChipTabRow` 要求有限宽度；`ChipTabRow` 在 Sheet/`ProvideCouiBackdrop` 内可省略 `backdrop`，`ChipTab`/`ChipTabButton` 必须显式传；每项独立单主体 renderer，不可共享；胶囊高 32.dp、宽 52–300.dp（文字最大 200.dp）。

```kotlin
@RequiresApi(34)
@Composable
fun CategoryChipTabExample(
    modifier: Modifier = Modifier,
    items: List<CouiChipTabItem>,
    selectedKey: Any?,
    onSelectedKeyChange: (Any) -> Unit,
    backdrop: CouiBackdrop,
) {
    ChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = onSelectedKeyChange,
        backdrop = backdrop,
    )
}
```

前置动作 `leadingAction` + `ChipTabButton`、`leadingActionVisible` 的显隐规则见 `chip-tab.md#添加前置独立动作leadingaction`。

## 13. SegmentedControl

要点（`segmented-control.md`）：API 34+；`backdrop = null` 为普通填色（Group 内推荐），非空为透明模糊且必须覆盖不透明 SDR 背景、排除自身；`labels` 非空且 `selectedIndex` 合法，否则抛 `IllegalArgumentException`；默认高 40.dp，`small = true` 为 32.dp；`commitOnRelease = true` 仅抬手提交，取消/父容器接管恢复宿主 `selectedIndex`。

```kotlin
@RequiresApi(34)
@Composable
fun RangeInGroupExample(
    modifier: Modifier = Modifier,
    labels: List<String>,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    if (labels.isEmpty()) return

    val safeSelected = selected.coerceIn(0, labels.lastIndex)
    LaunchedEffect(labels.size) {
        if (selected != safeSelected) selected = safeSelected
    }
    Group(modifier = modifier) {
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            labels = labels,
            selectedIndex = safeSelected,
            onSelected = { selected = it },
        )
    }
}
```

透明模糊 + renderer 失败兜底（`runCatching { CouiFloatingRenderer.standalone() }`）见 `segmented-control.md#页面背景与透明模糊`。

## 14. MessageInputBar

要点（`message-input-bar.md`）：API 34+；需要不含自身的正文背景源；三个操作位 `leadingAction`/`trailingAction`/`primaryAction`（`CouiMessageInputAction`），传 null 隐藏；`placeholder` 仅空草稿显示；编辑区默认最多三行；宿主用 `rememberCouiImeInsets()` 处理 IME；每实例独立单主体 renderer。

```kotlin
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
```

## 15. Menu

要点（`menu.md`）：点击菜单项不自动收起；`CouiMenuItem<T>` 字段 `value/text/icon/iconTint/selected/checked/enabled/dividerBefore/children`；`selected` 与 `checked` 相互独立；子菜单仅一级，子项的 `children` 必须为空；普通菜单宽 160–256dp，含子菜单最小 196dp；`style` 默认 `CouiMenuDefaults.style()`。

最小调用：

```kotlin
Menu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    items = items,
    onItemClick = onItemClick,
)
```

触点锚点：

```kotlin
@Composable
fun TouchAnchorMenuButton(
    modifier: Modifier = Modifier,
    style: CouiMenuStyle = CouiMenuDefaults.style(),
    label: String,
    items: List<CouiMenuItem<Int>>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anchor = rememberCouiMenuAnchor()

    Box(modifier) {
        Button(
            modifier = Modifier.couiMenuAnchor(anchor),
            onClick = { anchor.open { expanded = true } },
        ) {
            Text(label)
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            onItemClick = { item ->
                onSelect(item.value)
                expanded = false
            },
            style = style,
            anchor = anchor,
        )
    }
}
```

菜单项组合（单选高亮 + 独立勾选 + 分组线 + 一级子菜单）：

```kotlin
val items = listOf(
    CouiMenuItem(0, "按名称排序", icon = Icons.Default.SortByAlpha, selected = sortMode == 0),
    CouiMenuItem(1, "显示隐藏文件", checked = showHidden),
    CouiMenuItem(
        value = 2,
        text = "更多操作",
        dividerBefore = true,
        children = listOf(
            CouiMenuItem(3, "分享"),
            CouiMenuItem(4, "删除", enabled = false),
        ),
    ),
)
```

菜单材质需要 `ProvideCouiMenuBackdrop` 窗口源；`themeTint = Color.Transparent` 关闭色相；`isolateContent = false` 仅用于确认无不透明混合的静态根。完整示例见 `menu.md#使用背景材质`。

## 16. Dialog / InputDialog / LoadingDialog

要点（`dialog.md`）：基础 31+，材质 33+ 且需要不含自身的窗口背景源；推荐常驻组合传 `visible`（不要外层再 `if`），省略 `visible` 时由外层控制、无实时采样；按钮回调不自动关闭；`Primary` 样式必须给 `colors`（`CouiDialogDefaults.primaryActionColors(accent)`）；`InputDialog` 的 `usePlatformDefaultWidth = false` 与 `decorFitsSystemWindows = true` 必须保留；`blurRadius` 是 Fast Kawase 等级（0 或 ≥10）。

自定义操作行（同页「自定义底部操作行」）：`actionRows` 重载按行声明 1–4 行、每行至少一个操作，越界抛 `IllegalArgumentException`；窄屏按声明顺序拆成竖排，操作区仍与正文滚动分离；它与 `confirmAction`/`dismissAction`/`neutralAction` 的自动排布重载互斥。

```kotlin
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
```

自定义操作行（上行通栏主操作、下行两个次操作）：

```kotlin
@Composable
fun CustomActionRowsDialogExample(
    modifier: Modifier = Modifier,
    visible: Boolean,
    dark: Boolean,
    accent: Color,
    title: String?,
    message: String?,
    confirmLabel: String,
    cancelLabel: String,
    laterLabel: String,
    onConfirm: () -> Unit,
    onLater: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        modifier = modifier,
        visible = visible,
        onDismissRequest = onDismissRequest,
        dark = dark,
        title = title,
        message = message,
        actionRows = listOf(
            listOf(
                CouiDialogAction(
                    text = confirmLabel,
                    onClick = onConfirm,
                    style = CouiDialogActionStyle.Primary,
                    colors = CouiDialogDefaults.primaryActionColors(accent),
                ),
            ),
            listOf(
                CouiDialogAction(cancelLabel, onDismissRequest),
                CouiDialogAction(laterLabel, onLater),
            ),
        ),
    )
}
```

输入型：

```kotlin
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
```

加载型：

```kotlin
LoadingDialog(
    modifier = modifier,
    visible = isWorking,
    message = loadingMessage,
    onDismissRequest = onDismissRequest,
)
```

材质接入（`ProvideCouiMenuBackdrop` + `rememberCouiWindowBackdrop()` + `CouiDialogDefaults.isMaterialSupported()`）见 `dialog.md#使用背景材质`。

## 17. BottomSheet

要点（`bottom-sheet.md`）：必须常驻组合，用 `MutableState<Boolean>` 控制显隐；系统返回/遮罩/下拉默认设 false，传 `onDismissRequest` 后由宿主接管；宽度受 `CouiSheetDefaults.maxWidth`（540.dp）约束；`scrollableContent = false` 用于正文自带滚动容器；API 33+ 且有窗口源时默认开启模糊材质，`material = null` 强制不透明。

```kotlin
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
```

## 18. WheelPicker / CalendarPicker

要点（`pickers.md`）：API 31+，不消费背景源；`WheelPicker` 每次跨刻度即回调、无 `onValueChangeFinished`；`wrap` 默认 true（≤3 个值不循环）；`label` 必填且不绘制；`CalendarPicker` 左右翻月不回调、点日期或内置滚轮才提交；`minDate <= maxDate` 且 `value` 在范围内，否则抛异常。

```kotlin
WheelPicker(
    value = minute,
    onValueChange = { minute = it },
    valueRange = 0..59,
    label = minuteLabel,
)
```

```kotlin
@Composable
fun TimeWheelExample(
    modifier: Modifier = Modifier,
    hourLabel: String,
    minuteLabel: String,
    dividerColor: Color,
) {
    var hour by rememberSaveable { mutableIntStateOf(9) }
    var minute by rememberSaveable { mutableIntStateOf(30) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .couiPickerSelectionLines(dividerColor)
            .padding(vertical = CouiPickerDefaults.padding),
        horizontalArrangement = Arrangement.Center,
    ) {
        WheelPicker(
            modifier = Modifier.width(CouiPickerDefaults.columnWidth),
            value = hour,
            onValueChange = { hour = it },
            valueRange = 0..23,
            label = hourLabel,
            formatter = { "%02d".format(it) },
        )
        WheelPicker(
            modifier = Modifier.width(CouiPickerDefaults.columnWidth),
            value = minute,
            onValueChange = { minute = it },
            valueRange = 0..59,
            label = minuteLabel,
            repeatIntervalMillis = 100,
            formatter = { "%02d".format(it) },
        )
    }
}
```

```kotlin
CalendarPicker(
    value = selectedDate,
    onValueChange = onDateChange,
)
```

## 19. Icons

`CouiIcons` 用法与完整名称清单见 `references/icon-catalog.md`；组件页 `docs/components/icons.md` 提供显示、位图、指定颜色与特殊图标示例。

## 20. 文本组件

- 公开文本入口 `com.itos.coui.text.Text`：`kind` 必填（`CouiTextKind.ItemTitle`/`ItemSummary`/`GroupFooter`/`SheetTitle` 等），颜色优先级「显式 color → `LocalCouiContentColor` → `CouiTheme.colorScheme.onSurface`」；`ItemTitle`/`ItemSummary`/`GroupFooter` 默认起始边对齐，其余居中；`SheetTitle` 默认单行省略；`rememberCouiTextStyle(kind)` 供 `BasicText` 复用。（`docs/components/item.md#coui-文字排版text-与-remembercouitextstyle`）
- `com.itos.coui.theme` 内的 `CouiText`、`CouiIcon`、`ProvideCouiTextStyle` 是内部实现，不作为公开接入入口。（`appearance.md`）
