# TopAppBar 顶栏

## TopAppBar

在 COUI `Scaffold.topBar` 里直接放 `TopAppBar(title = title)`，容器会提供正文背景源。默认保留顶栏的渐进模糊、遮罩和按钮反馈。[Demo 顶栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoToolbarSamples.kt#L65)

使用 Material 3 `Scaffold` 时保留原容器，把同一个背景源挂到正文并传给顶栏。下面的标题槽和列表内容由页面提供，`TopAppBar` 来自 `com.itos.coui.topappbar`。

```kotlin
@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    rows: LazyListScope.() -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    val background = MaterialTheme.colorScheme.background
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = title, backdrop = backdrop) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .couiBackdrop(backdrop)
                .background(background)
                .consumeWindowInsets(padding),
            contentPadding = padding,
            content = rows,
        )
    }
}
```

## TopAppBarButton

把操作按钮放进顶栏的 `actions` 或 `navigationIcon` 槽。COUI 浮动按钮需要 API 34+ 和顶栏提供的效果作用域。按钮需要弹出菜单时，将它和菜单并列放进同一个紧凑容器。[Menu 的按钮菜单](menu.md#按钮菜单)

## TopAppBarSurface

需要自定义顶栏排版时让整个固定区域共用一个表面。下面的操作按钮位于 API 34+，`backdrop` 由页面正文录制；`headerContent` 是宿主的第二行内容。[Demo 自定义顶栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoToolbarSamples.kt#L139) · [Demo 顶栏按钮](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt#L82)

```kotlin
TopAppBarSurface(backdrop = backdrop) {
    Column(Modifier.windowInsetsPadding(CouiTopAppBarDefaults.windowInsets)) {
        TopAppBarButton(onClick = onBack) {
            Icon(CouiIcons.Filled.Back, contentDescription = backDescription)
        }
        headerContent()
    }
}
```

标题行和固定表头共用渐进模糊时，用一个 `TopAppBarSurface` 包住两行，让渐隐边界落在表头下方。只要模糊时传 `gradientColor = Color.Transparent`。自定义表面由内容处理一次顶部安全区，完整的 `TopAppBar` 已经处理。[顶栏参数与槽位](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/top-app-bar.md)

## 折叠顶栏

设置页上滑时让大标题收起，列表回到顶部后下滑再展开。已有 M3 `Scaffold` 时，把滚动连接挂到容器一次，把同一个正文背景源传给顶栏。API 31–32 自动使用分层模糊，API 33+ 默认使用 Compose 渐进模糊。`title` 是页面的单行标题槽，使用 [M3 适配](../theme/material3.md#couimaterial3style)时，槽里的 `Text` 继承顶栏字样式；`rows` 是列表内容。

```kotlin
val list = rememberLazyListState()
val behavior = CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior {
    list.canScrollForward || list.canScrollBackward
}
val backdrop = rememberCouiBackdrop()
val density = LocalDensity.current
val background = MaterialTheme.colorScheme.background
androidx.compose.material3.Scaffold(
    modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
    topBar = { LargeTopAppBar(title = title, scrollBehavior = behavior, backdrop = backdrop) },
) { insets ->
    val padding = remember(behavior.state, insets, density) {
        behavior.state.contentPadding(insets, density)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().couiBackdrop(backdrop)
            .background(background).consumeWindowInsets(insets),
        state = list,
        contentPadding = padding,
        content = rows,
    )
}
```

列表按折叠状态计算顶部留白，消费 Insets 时仍使用 Scaffold 给的原值。若标题已收起但首行上方仍有大块空白，检查列表的 `contentPadding`。使用 COUI `Scaffold` 的顶栏槽时，容器已提供正文源，可省去手动录制和传源。[Demo 折叠顶栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L88)

需要常驻搜索框时，只替换上例的顶栏。页面持有展开状态，`search` 与[搜索框](search.md#searchbar)共用，`searchContent` 是该搜索框的内容槽。

```kotlin
CollapsingTopAppBar(
    state = behavior.state,
    searchState = search,
    backdrop = backdrop,
    title = titleText,
    color = MaterialTheme.colorScheme.onSurface,
    searchBar = searchContent,
)
```
