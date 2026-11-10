# 折叠顶栏

`LargeTopAppBar` 将大标题缩成固定的小标题。首页需要常驻搜索时，使用 `CollapsingTopAppBar`。带常驻搜索的标题沿用 `LargeTopAppBar` 的默认展开样式，随滚动缩放和淡出，搜索栏停靠顶部。

## 普通大标题

下面片段放在页面 Composable 中。宿主提供 `title`、不透明 `background` 和正文 `content`；示例使用 COUI Scaffold，自动向顶栏提供正文背景源。纯 Compose 标题从顶栏槽位读取字体和颜色：

```kotlin
val density = LocalDensity.current
val scroll = rememberScrollState()
val behavior = CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior(
    canScroll = { scroll.maxValue > 0 },
)
Scaffold(
    modifier = Modifier.nestedScroll(behavior.nestedScrollConnection),
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
    Column(Modifier.fillMaxSize()
        .verticalScroll(scroll).padding(contentPadding).consumeWindowInsets(padding)) {
        content()
    }
}
```

`scrollBehavior` 持有折叠 state 和嵌套滚动连接。连接只挂到实际滚动正文的一个祖先；`canScroll` 读取实际滚动范围，LazyColumn 可使用 `canScrollForward || canScrollBackward`。正文必须保留 `state.contentPadding(padding, density)` 补偿，Insets 仍消费 Scaffold 返回的原始 Padding。

省略 `scrollBehavior` 时保持展开。`title`、`navigationIcon` 和 `actions` 槽位与 `TopAppBar` 相同，字体由组件提供；接入 M3 适配后，M3 `Text` 可继承该字体。`expandedHeight` 与 `collapsedHeight` 不包含安全区，必须是有限正数，且展开高度大于收起高度。

默认展开字号为 40sp，基础字重为 700。收起时，起始侧导航槽占用空间则使用 18sp，导航槽为空则使用 24sp。标题槽位须继承组件提供的样式。

`expandedTitleStyle` 和 `collapsedTitleStyle` 接受 Compose `TextStyle`，可设置字体族、字重、字号、颜色等属性。收起样式中省略的属性继承展开样式，字号使用收起预设。折叠过程中缩放展开标题，完全收起后应用收起样式。标题内容始终由同一个 `title` 槽提供。

在上面的调用中传入两套样式，即可单独指定展开和收起的字号与字重：

```kotlin
expandedTitleStyle = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold),
collapsedTitleStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
```

在两套样式中通过 `fontSize` 设置字号。字号须为有限正数 `sp` 或 `TextUnit.Unspecified`，并跟随系统字体大小。

`LargeTopAppBar` 使用项目定制预设；有导航按钮时，展开标题位于按钮下方，折叠时沿连续弧线向逻辑末端和上方移动到按钮旁。按钮与操作区保持在标题上层，标题宽度保留右侧操作区。二级页装配与轨迹说明见[定制差异](../guide/customizations.md#二级页大标题与弧线收起)。

完整顶栏默认处理顶部和横向系统安全区。可通过 `windowInsets` 调整，不要重复添加同一安全区。默认配色来自 `CouiTopAppBarDefaults.colors()`；局部指定深浅模式时，向 `colors(dark = dark)` 和顶栏传入同一个 `dark`。

## 只显示标题

使用 `CollapsingTopAppBar` 的页面也可省略 `searchBar` 和 `searchState`，保留固定小标题：

```kotlin
CollapsingTopAppBar(
    state = header,
    title = title,
    backdrop = backdrop,
)
```

`header` 和 `backdrop` 由页面按下节方式创建；正文使用同一个 `header.nestedScrollConnection` 和 `contentPadding`。标题保持可见，字号和位置随折叠变化，最终保留固定栏位。预设见 `CouiCollapsingAppBarDefaults`；无搜索模式的字号使用 dp，不额外按系统字体倍率放大。普通大标题页面优先使用上面的 `LargeTopAppBar` 槽位接口。

省略 `searchBar` 或传入 `null` 时使用纯标题布局；传入空 lambda 也会使用搜索式布局。

## 基本装配

下面在 Material 3 Scaffold 中组合折叠标题、搜索框和结果层。片段放在页面 Composable 中，页面提供模式、文案、查询状态和回调；`openDetail` 打开详情，`results(query, enabled)` 显示结果并控制其滚动与交互。

```kotlin
val density = LocalDensity.current
val backdrop = rememberCouiBackdrop()
val scroll = rememberScrollState()
val header = rememberCouiCollapsingAppBarState(canScroll = { scroll.maxValue > 0 })
val search = rememberCouiSearchBarState(expanded)
val colors = CouiPageDefaults.themeColors(dark)
BackHandler(enabled = expanded) { updateExpanded(false) }
androidx.compose.material3.Scaffold(
    modifier = Modifier.nestedScroll(header.nestedScrollConnection),
    containerColor = colors.background,
    topBar = {
        CollapsingTopAppBar(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            state = header, searchState = search, title = title,
            color = colors.title, backdrop = backdrop,
            dark = dark,
        ) {
            SearchBar(state = search, query = query, onQueryChange = updateQuery,
                onExpandedChange = updateExpanded, placeholder = searchLabel,
                cancelText = cancelLabel, clearDescription = clearLabel,
                dark = dark, backdrop = backdrop, shadowProgress = header.collapsedFraction)
        }
    },
) { padding ->
    val contentPadding = remember(header, padding, density) {
        header.contentPadding(padding, density)
    }
    Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(colors.background)) {
        Column(Modifier.fillMaxSize()
            .verticalScroll(scroll, enabled = !search.active)
            .padding(contentPadding).consumeWindowInsets(padding)
            .graphicsLayer { alpha = search.contentAlpha }
            .then(if (search.active) Modifier.clearAndSetSemantics {} else Modifier)) {
            Group(colors = colors) {
                Item(onClick = openDetail, enabled = !search.active, dark = dark) {
                    BasicText(rowTitle, style = rowTextStyle)
                }
            }
        }
        if (search.active) {
            Box(Modifier.matchParentSize()
                .padding(contentPadding).consumeWindowInsets(padding)
                .then(if (search.expanded) Modifier else Modifier.clearAndSetSemantics {})
                .graphicsLayer {
                    alpha = search.resultsAlpha
                    translationY = with(density) {
                        CouiCollapsingAppBarDefaults.resultsStartOffset.toPx()
                    } * (1f - search.resultsAlpha)
                }) {
                results(query, search.expanded)
            }
        }
    }
}
```

这里使用宿主的普通惯性。需要 COUI 惯性与同帧触边交接时，用 [couiVerticalScroll](../effects/overscroll.md) 替换 `verticalScroll`。使用 [Scaffold](scaffold.md) 时，横向安全区由容器处理，可省略顶栏的横向 `windowInsetsPadding`。

## 状态与正文布局

`state.nestedScrollConnection` 连接到实际列表的祖先：向上滚动先折叠标题，反向滚动在列表到顶后展开；停止时执行吸附。`canScroll` 读取实际列表是否存在滚动范围，带搜索槽的短内容在滚动结束后恢复展开；只有标题时，短内容也可以保留折叠的小标题。LazyColumn 可读取 `canScrollForward || canScrollBackward`。

顶栏回传实际测量占位。`state.contentPadding(padding, density)` 只修正正文顶部，保留两侧和底部 Padding；将返回对象记住，供布局阶段读取。不要用 `contentTranslationY` 平移整个视口，否则背景和底部边界也会移动；该像素值只适用于宿主明确控制的局部布局。

LazyColumn 使用补偿后的 `contentPadding`，但 `consumeWindowInsets` 仍接收宿主原始 Padding。顶栏内部消费剩余状态栏 Insets，宿主处理横向安全区和底栏，具体分工见[宿主页面接入](../guide/secondary-pages.md)。

## 搜索与输入

顶栏和 `SearchBar` 使用同一个搜索 state。搜索展开前记录恢复位置：完全折叠的顶栏恢复折叠，中间位置恢复展开。搜索活动期间锁住普通顶栏滚动。

宿主用 `contentAlpha` 和 `resultsAlpha` 绘制正文与结果，并在 `search.active` 期间禁用正文交互、移除其无障碍子树。示例同时处理滚动和行点击；增加其他控件时，也要禁用其输入与焦点。结果内容以 `search.expanded` 控制交互，关闭时禁用输入并保留退场绘制。透明度不能代替这些处理。输入与返回规则见[常驻搜索](search-bar.md)。

`searchBar` 放置搜索内容；需要排列多个组件时，在槽内使用 Row 或 Column。无搜索槽时，顶栏不读取搜索状态的展开目标与动画进度，也不会锁住列表滚动。

## 外观与采样

`color` 默认按实际 `dark` 解析，显式颜色优先，其 alpha 与标题动画相乘。`dark` 只控制顶栏自身；需要搜索采用同一模式时，显式传入相同 `dark`，或将二者放入同一个局部 `CouiTheme`。

`backgroundColor` 省略或传入 `Color.Unspecified` 时，背景遮罩在浅色模式使用白色、深色模式使用黑色，并向下渐隐。显式颜色覆盖默认遮罩；`Color.Transparent` 关闭遮罩并保留模糊。模糊背景使用固定的下沿延伸，并随折叠向上平移；完整背景高度不随当前栏高缩放。普通大标题保持不透明；带常驻搜索时，背景淡入跟随正文实际位置，搜索活动期间保持显示。这些绘制范围不改变布局或命中高度。

COUI Scaffold 自动录制正文与结果，不包含顶栏或其浮动消费者；`LargeTopAppBar` 和 `CollapsingTopAppBar` 放在 `topBar` 时可省略 `backdrop`，显式参数优先。上面的 Material 3 示例仍需创建源，在现有正文容器添加 `.couiBackdrop(backdrop)`，并将源显式传给顶栏。图层由库管理，无需全局初始化，见[背景采样](../effects/backdrop.md#顶栏接入)。自定义背景容器 `TopAppBarSurface` 的参数见[顶栏与按钮](top-app-bar.md#背景参数)。

[LargeTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBar.kt) · [滚动行为与 Defaults](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarDefaults.kt) · [CollapsingTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/CollapsingTopAppBar.kt) · [状态](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingAppBarState.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
