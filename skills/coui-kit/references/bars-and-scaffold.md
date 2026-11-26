# Scaffold、顶栏、底栏、FAB 与输入栏

## Scaffold

将顶栏、底栏、FAB 放入 COUI `Scaffold` 槽位即可自动采样正文；容器须有界宽高。

```kotlin
Scaffold(topBar = { TopAppBar(title = title) }, containerColor = CouiPageDefaults.themeColors().background) { padding ->
    LazyColumn(Modifier.fillMaxSize().consumeWindowInsets(padding), contentPadding = padding, content = rows)
}
```

### 避坑

- 普通 `Column` 配 `couiVerticalScroll` 时，`.padding(padding)` 与 `.consumeWindowInsets(padding)` **放在滚动之后**，否则内容无法滚入栏位下方。
- 勿在 COUI 顶栏/底栏外重复加 `statusBarsPadding()` / `navigationBarsPadding()`。
- 含输入框页面将 `imePadding()` 放在滚动容器**外侧**缩小视口。

## TopAppBar 与 TopAppBarSurface

COUI `Scaffold` 直接用 `TopAppBar(title = title)`；M3 `Scaffold` 须显式挂载正文源：

```kotlin
val backdrop = rememberCouiBackdrop()
androidx.compose.material3.Scaffold(topBar = { TopAppBar(title = title, backdrop = backdrop) }) { padding ->
    LazyColumn(Modifier.fillMaxSize().couiBackdrop(backdrop).background(MaterialTheme.colorScheme.background)
        .consumeWindowInsets(padding), contentPadding = padding, content = rows)
}
```

- 自定义多行固定顶栏用同一 `TopAppBarSurface(backdrop = backdrop)` 包裹，内部处理一次 `Modifier.windowInsetsPadding(CouiTopAppBarDefaults.windowInsets)`（`TopAppBar` 已内置）。
- **只要模糊不要渐变遮罩**：`TopAppBar` 传 `colors = CouiTopAppBarDefaults.colors(backgroundColor = Color.Transparent)`；`TopAppBarSurface` 传 `gradientColor = Color.Transparent`。
- 渐进模糊后端（API 33+ 默认 Compose）：用 `ProvideCouiTopAppBarBlur(useComposeBlur)` 或 `useCouiBlur` 切换。

## 折叠顶栏

`LargeTopAppBar` 折叠大标题（`expandedHeight > collapsedHeight > 0.dp`）；大标题带常驻搜索改用 `CollapsingTopAppBar`。

```kotlin
val list = rememberLazyListState()
val behavior = CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior { list.canScrollForward || list.canScrollBackward }
val density = LocalDensity.current // backdrop / background 接线同上
androidx.compose.material3.Scaffold(Modifier.nestedScroll(behavior.nestedScrollConnection),
    topBar = { LargeTopAppBar(title = title, scrollBehavior = behavior, backdrop = backdrop) }) { insets ->
    val padding = remember(behavior.state, insets, density) { behavior.state.contentPadding(insets, density) }
    LazyColumn(Modifier.fillMaxSize().couiBackdrop(backdrop).background(bg).consumeWindowInsets(insets),
        state = list, contentPadding = padding, content = rows)
}
```

带常驻搜索替换 `topBar`：

```kotlin
CollapsingTopAppBar(state = behavior.state, searchState = search, backdrop = backdrop,
    title = titleText, color = MaterialTheme.colorScheme.onSurface) {
    SearchBar(state = search, query = query, onQueryChange = onQueryChange, onExpandedChange = onExpandedChange,
        placeholder = hint, cancelText = cancelText, clearDescription = clearDesc, shadowProgress = behavior.state.collapsedFraction)
}
```

### 避坑

- **收起后顶部留白**：须用 `behavior.state.contentPadding(insets, density)`；`.consumeWindowInsets(insets)` 仍传原始 `insets`。
- `LargeTopAppBar` 的 `title` 槽须继承槽位样式；COUI `Scaffold` 可省略手动 `backdrop`。

## TopAppBarButton

**API 34+**。读取 `LocalCouiToolbarBackdrop`，**必须在 `TopAppBar` / `LargeTopAppBar` / `TopAppBarSurface` 内部调用**，外部调用直接 `checkNotNull` 崩溃。弹菜单见[按钮菜单](overlays.md#按钮菜单)。

## NavigationBar

**API 34+**。放入 COUI `Scaffold.bottomBar`，勿叠 `navigationBarsPadding()`：

```kotlin
NavigationBar(
    items = items, selectedIndex = index, onSelection = onNav,
    leftAction = CouiNavigationBarAction(themeIcon, themeDesc, visible = showLeft, onClick = onToggleTheme),
    rightAction = CouiNavigationBarAction(addIcon, addDesc, visible = showRight, onClick = onAdd,
        style = CouiNavigationBarDefaults.coloredActionStyle()),
)
```

- 省略 `style` 默认使用透明附件；`coloredActionStyle()` 省略 `containerColor` 即跟随主题强调色；`onClick` 可为 `null`（只保留拖拽与按压光效）。
- 中间胶囊固定 `CouiCapsuleShape`，由顶层 `material` / `themeTint` / `lightColor` 控制；左右附件各自通过 `CouiNavigationBarAction.style` 控制。

### 避坑

- **顶层 `material` / `themeTint` 不作用于透明附件**：它们只影响中间胶囊；透明附件需同步材质或关闭染色时，传 `style = CouiNavigationBarDefaults.transparentActionStyle(material = customMaterial)`。
- 整栏显隐使用 `NavigationBar(visible = …)`，布局保留原高度。仅收起附件时保留 `CouiNavigationBarAction` 实例并将其 `visible` 设为 `false`。传 `null` 会立即移除该附件。
- **自定义附件样式**：`CouiNavigationBarActionStyle.shape` 须使用 `CouiShape`，`shadow` 须同步读取 `LocalCouiContourGlowEnabled.current`。
- **门槛与稳定位置**：API 31–33 或 `CouiFloatingRenderer()`（**不可用 `standalone()`**）创建失败时同槽位回退宿主普通底栏；切页保持底栏与 `renderer` 处于稳定组合位置。
- `items` 非空且 `selectedIndex` 有效；勿对图标预乘状态透明度。

## FloatingToolbar

**API 34+**。编辑页底部的悬浮操作放入 COUI `Scaffold.bottomBar`，勿外叠 `navigationBarsPadding()`（组件内置 `WindowInsets.tappableElement` 横向/底部安全区及悬浮边距，收起整栏时保留占位；IME 避让由宿主页处理）。调用 `FloatingToolbar(items = actions, onItemClick = onAction, visible = showBar)`，各项使用稳定的 `CouiFloatingToolbarItem.value`，可通过顶层 `dark`、`style` 与 `themeTint`（同步作用于“更多”菜单）定制外观。[悬浮操作栏](../../../docs/components/floating-toolbar.md)

## Fab

**API 34+**。放 `Scaffold.floatingActionButton`；默认强调色省略 `material`/`shadow`。指定颜色时须用同一 `color` 配对传入：

```kotlin
val dark = CouiTheme.isDark; val c = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark)
Fab(onClick = onAdd, material = CouiFabDefaults.material(c, dark),
    shadow = CouiFabDefaults.shadow(c, LocalDensity.current.density, LocalCouiContourGlowEnabled.current)) {
    Icon(CouiIcons.Filled.Add, contentDescription = addDesc)
}
```

### 避坑

- **相邻多 `Fab` 互擦投影**：必传 `foregroundComposition = true` 并用不含按钮的不透明 SDR 正文源。
- 默认内容色为白色，去掉子 M3 `Icon` 的显式 `tint`；禁止多按钮共享 `renderer`。

## TopAppBarSearch 与 SearchBar

- `TopAppBarSearch`（**API 34+**）：放 `TopAppBar` `title` 槽 + `actions` 槽放 `TopAppBarSearchButton`，共用 `rememberCouiTopAppBarSearchState(expanded)`；`BackHandler(expanded)` 与取消按钮共用关闭逻辑（清查询、收键盘）。低版本改用 `SearchBar` 或宿主入口。
- **顶栏操作间距**由宿主设置，`TopAppBarButtonGroup` 将按钮向末端排列。需要等距时，以按钮的可见表面为准。搜索按钮占宽 `48.dp`，圆形表面宽 `40.dp`。用 `TopAppBarSearchAction(visibleWhileSearching = true)` 保留分类按钮时，可用 `Modifier.padding(start = if (expanded) 0.dp else 4.dp)` 补齐收起态中搜索与分类的间距，展开搜索时收回这段留白。`4.dp` 为宿主示例值。
- `SearchBar`（API 31–33 填色，API 34+ 浮动材质）：结果层盖原列表时保留底层滚动并停用底层交互与无障碍焦点。
- **避坑**：两者内置搜索图标，勿再放放大镜。

## MessageInputBar

**API 34+**。放 COUI `Scaffold.bottomBar`（第三方容器传不含自身的正文源），`onSubmit` 与 `primaryAction.onClick` 共用提交函数。
**避坑**：组件已补底部导航栏占位，宿主只处理 IME 避让，勿叠底部留白；`placeholderMaxLines > 0`。
