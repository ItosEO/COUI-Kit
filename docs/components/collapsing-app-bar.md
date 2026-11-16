# 折叠顶栏

`LargeTopAppBar` 用于二级页的大标题滚动折叠（展开大标题沿弧线收起为固定小标题）；`CollapsingTopAppBar` 用于首页等需要大标题与常驻搜索框联动的场景（带常驻搜索的标题沿用 `LargeTopAppBar` 的默认展开样式，随滚动缩放并淡出，搜索栏停靠顶部）。

- **最低系统版本**：顶栏容器本身支持库最低版本（**API 31+**）；API 33+ 使用 `RuntimeShader` 渐进模糊，API 31–32 使用分层模糊。
- **内部子控件门槛**：内部放入 `TopAppBarButton` 需 **API 34+**；搭配 `SearchBar` 时，API 31–33 渲染普通填色胶囊，API 34+ 启用浮动模糊融合材质。
- **前置依赖**：放入 COUI `Scaffold` 的 `topBar` 时自动获取正文 `backdrop`；放在 Material 3 `Scaffold` 等第三方容器时，需手动创建并挂载 `.couiBackdrop(backdrop)`。

## 普通大标题

在二级页中使用 COUI `Scaffold` 与 `LargeTopAppBar` 时，按以下三步完成接线：

1. 创建滚动状态 `scroll` 与 `CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior(canScroll = ...)`。
2. 将 `Modifier.nestedScroll(behavior.nestedScrollConnection)` 挂到实际滚动容器的单个祖先节点（通常是 `Scaffold`）。
3. 正文使用 `behavior.state.contentPadding(padding, density)` 作为顶部补偿留白，而 `consumeWindowInsets(padding)` 仍传入 `Scaffold` 提供的原始 `padding`。

~~~kotlin
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
~~~

接入要点：

- **`canScroll` 判定**：需反映正文是否具备滚动范围。普通 `ScrollState` 用 `{ scroll.maxValue > 0 }`；`LazyListState` 可使用 `{ listState.canScrollForward || listState.canScrollBackward }`。
- **保持展开与高度限制**：省略 `scrollBehavior` 时顶栏保持展开。`expandedHeight` 与 `collapsedHeight` 不包含安全区，必须为有限正数且 `expandedHeight > collapsedHeight`。
- **标题样式与字号（`expandedTitleStyle` / `collapsedTitleStyle`）**：默认展开字号为 `40.sp`、基础字重为 `700`；收起时若存在导航按钮为 `18.sp`，导航槽为空则为 `24.sp`。可通过 `expandedTitleStyle` 与 `collapsedTitleStyle` 传入 `TextStyle`（如 `TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)`）覆盖字体族、字重、字号或颜色；`collapsedTitleStyle` 省略的属性继承展开样式，`fontSize` 须为有限正数 `sp` 或 `TextUnit.Unspecified` 并跟随系统字体大小。折叠过程连续缩放展开标题，完全收起后应用收起样式。
- **弧线标题轨迹**：存在 `navigationIcon` 时，展开标题位于导航按钮下方，折叠时沿连续弧线移动至按钮旁；按钮与操作区始终位于标题上层，标题宽度预留右侧操作区，细节见[定制差异](../guide/customizations.md#二级页大标题与弧线收起)。
- **槽位与深浅模式**：`title`、`navigationIcon`、`actions` 与 `TopAppBar` 一致，标题槽位须继承组件提供的样式（M3 适配下 `Text` 可直接继承）。局部覆盖深浅模式时，请向 `CouiTopAppBarDefaults.colors(dark = dark)` 与 `LargeTopAppBar` 传入同一个 `dark`。

## 联动常驻搜索（`CollapsingTopAppBar`）

首页需要“大标题随滚动折叠淡出 + 搜索框停靠顶部 + 点击搜索展开独立结果层”时，使用 `CollapsingTopAppBar` 搭配 `SearchBar`。以下示例演示如何在 Material 3 `Scaffold` 中完整装配：

1. 创建背景源 `backdrop`、滚动状态 `scroll`、折叠状态 `header`、搜索状态 `search`，并在 `expanded` 时拦截系统返回键。
2. 将 `header.nestedScrollConnection` 挂到宿主 `Scaffold`，在 `topBar` 中放入 `CollapsingTopAppBar` 与 `SearchBar`，将 `header.collapsedFraction` 传给搜索框的 `shadowProgress`。
3. 在正文根容器挂载 `.couiBackdrop(backdrop)` 与不透明背景色，并用 `header.contentPadding(padding, density)` 排版滚动正文。
4. 在 `search.active` 期间同步淡出原正文并禁用其滚动、点击与无障碍语义，同时按 `search.resultsAlpha` 与偏移量显示上层结果容器。

~~~kotlin
@Composable
fun CollapsingSearchPageExample(
    modifier: Modifier = Modifier,
    dark: Boolean,
    title: String,
    rowTitle: String,
    searchLabel: String,
    cancelLabel: String,
    clearLabel: String,
    query: String,
    expanded: Boolean,
    rowTextStyle: TextStyle,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onOpenDetail: () -> Unit,
    results: @Composable (query: String, interactive: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    val backdrop = rememberCouiBackdrop()
    val scroll = rememberScrollState()
    val header = rememberCouiCollapsingAppBarState(canScroll = { scroll.maxValue > 0 })
    val search = rememberCouiSearchBarState(expanded)
    val colors = CouiPageDefaults.themeColors(dark)

    BackHandler(enabled = expanded) { onExpandedChange(false) }

    androidx.compose.material3.Scaffold(
        modifier = modifier.nestedScroll(header.nestedScrollConnection),
        containerColor = colors.background,
        topBar = {
            CollapsingTopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                ),
                state = header,
                searchState = search,
                title = title,
                color = colors.title,
                backdrop = backdrop,
                dark = dark,
            ) {
                SearchBar(
                    state = search,
                    query = query,
                    onQueryChange = onQueryChange,
                    onExpandedChange = onExpandedChange,
                    placeholder = searchLabel,
                    cancelText = cancelLabel,
                    clearDescription = clearLabel,
                    dark = dark,
                    backdrop = backdrop,
                    shadowProgress = header.collapsedFraction,
                )
            }
        },
    ) { padding ->
        val contentPadding = remember(header, padding, density) {
            header.contentPadding(padding, density)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .couiBackdrop(backdrop)
                .background(colors.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll, enabled = !search.active)
                    .padding(contentPadding)
                    .consumeWindowInsets(padding)
                    .graphicsLayer { alpha = search.contentAlpha }
                    .then(if (search.active) Modifier.clearAndSetSemantics {} else Modifier),
            ) {
                Group(colors = colors) {
                    Item(onClick = onOpenDetail, enabled = !search.active, dark = dark) {
                        BasicText(rowTitle, style = rowTextStyle)
                    }
                }
            }
            if (search.active) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(contentPadding)
                        .consumeWindowInsets(padding)
                        .then(if (search.expanded) Modifier else Modifier.clearAndSetSemantics {})
                        .graphicsLayer {
                            alpha = search.resultsAlpha
                            translationY = with(density) {
                                CouiCollapsingAppBarDefaults.resultsStartOffset.toPx()
                            } * (1f - search.resultsAlpha)
                        },
                ) {
                    results(query, search.expanded)
                }
            }
        }
    }
}
~~~

### 关键机制拆解

- **滚动连接与 `contentPadding`**：
  - 向上滚动时 `nestedScrollConnection` 先折叠顶栏，到达折叠态后再滚动列表；向下滚动在列表到顶后重新展开顶栏，停止时自动执行半程吸附。
  - `header.contentPadding(padding, density)` 仅修正顶部留白，保留左右与底部值；请用 `remember` 缓存并在布局阶段读取，而 `consumeWindowInsets(padding)` 必须继续传入宿主原始的 `padding`。
  - 如需 COUI 弹性过界回弹与同帧触边交接，可将 `verticalScroll` 替换为 [couiVerticalScroll](../effects/overscroll.md)。
  - 若宿主换用 COUI [Scaffold](scaffold.md)，横向安全区与默认 `backdrop` 均由容器自动处理，可省略顶栏上的横向 `windowInsetsPadding` 和手动 `.couiBackdrop(backdrop)`。
- **搜索遮罩与无障碍控制**：
  - `CollapsingTopAppBar` 与 `SearchBar` 共享同一个 `search` 状态。展开搜索前会记录恢复位置（完全折叠时恢复折叠，其余恢复展开），且搜索激活期间锁定顶栏普通滚动。
  - 仅靠 `alpha = 0f` **无法**阻止触摸或焦点穿透。`search.active` 为 `true` 时，必须同步禁用底层正文的滚动、点击和焦点，并附加 `Modifier.clearAndSetSemantics {}` 移除无障碍子树。
  - 上层结果容器在 `search.active` 期间保持组合以播放退场动画，交互权限应绑定到 `search.expanded`（退场期间不可点击且清除语义）。更多输入与返回规则见[常驻搜索](search-bar.md)。

### 仅显示标题（无搜索槽）

使用 `CollapsingTopAppBar` 时若省略 `searchBar`（或传 `null`）与 `searchState`，标题在折叠后会缩小并停留在固定顶栏位置而不淡出（普通大标题页面优先使用前文的 `LargeTopAppBar`）：

~~~kotlin
@Composable
fun CollapsingTitleOnlyExample(
    modifier: Modifier = Modifier,
    header: CouiCollapsingAppBarState,
    title: String,
    backdrop: CouiBackdrop,
) {
    CollapsingTopAppBar(
        modifier = modifier,
        state = header,
        title = title,
        backdrop = backdrop,
    )
}
~~~

注意：**省略 `searchBar` 或传入 `null` 才会进入纯标题布局**；传入空 lambda `{}` 仍会按搜索式布局测量并淡出标题。无搜索模式的字号固定使用 `dp`，不随系统字体倍率额外放大。

## 参数与行为速查

| 组件 / 状态 | 参数 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `LargeTopAppBar` | `scrollBehavior` | `null` | 持有折叠状态与滚动连接；`null` 时顶栏保持展开 |
| `LargeTopAppBar` | `expandedHeight` / `collapsedHeight` | `110.dp` / `52.dp` | 不含安全区的展开与收起高度，必须为有限正数且前者大于后者 |
| `LargeTopAppBar` | `expandedTitleStyle` / `collapsedTitleStyle` | `TextStyle.Default` | 分别控制展开与收起态的标题 `TextStyle`；收起样式未指定的属性继承展开样式 |
| `CollapsingTopAppBar` | `searchState` | `rememberCouiSearchBarState(false)` | 与 `SearchBar` 共享同一个状态以联动标题动画与滚动锁定 |
| `CollapsingTopAppBar` | `color` / `backgroundColor` | `Color.Unspecified` | `color` 为标题色（alpha 与动画相乘）；`backgroundColor` 同 `TopAppBarSurface` 的 `gradientColor` |
| `CollapsingTopAppBar` | `dark` | `CouiTheme.isDark` | 仅控制顶栏自身配色；内部 `SearchBar` 需同步传入相同 `dark` 或共处同一局部 `CouiTheme` |
| `CollapsingTopAppBar` | `searchBar` | `null` | 传入 `null` 显示固定小标题；需要排列多个搜索区控件时可在槽内组合 `Row` 或 `Column` |
| `CouiCollapsingAppBarState` | `collapsedFraction` / `contentPadding(...)` | — | `collapsedFraction` 范围为 `0f..1f`，可传给 `SearchBar.shadowProgress` |

## 装配注意事项

- **不要用 `contentTranslationY` 平移整个视口**：整体平移会导致背景与底部边界一并移动，产生黑边或安全区错位；正文补偿请统一使用 `state.contentPadding(padding, density)`，`contentTranslationY` 仅适合宿主明确控制的局部布局。
- **独立背景采样**：在 COUI `Scaffold` 中无需手动传 `backdrop`；在 Material 3 等第三方容器中，需将 `.couiBackdrop(backdrop)` 挂在正文外层并传给顶栏，不可把顶栏包进源节点内，详见[背景采样](../effects/backdrop.md#顶栏接入)。自定义 `TopAppBarSurface` 参数见[顶栏与按钮](top-app-bar.md#背景参数)。
- **短内容行为**：带搜索槽的短内容（`canScroll() == false`）在滚动松手后会自动恢复展开；无搜索槽的纯标题模式允许短内容保留折叠态。

## 源码与完整示例

- **大标题与滚动行为**：[TopAppBar.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBar.kt)、[CouiTopAppBarDefaults.kt](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarDefaults.kt)
- **折叠搜索顶栏与状态**：[CollapsingTopAppBar.kt](../../coui/src/main/java/com/itos/coui/topappbar/CollapsingTopAppBar.kt)、[CouiCollapsingAppBarState.kt](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingAppBarState.kt)
- **首页完整示例**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
