# 顶栏搜索

`TopAppBarSearch` 用于在顶栏内部将搜索按钮以融合动画展开为胶囊输入框。查询内容、展开目标与返回拦截均由宿主持有。

- **最低系统版本**：要求 **API 34+**。
- **前置依赖**：必须放置在 `TopAppBarSurface`、`TopAppBar` 或 `LargeTopAppBar` 内部（读取顶栏效果与共享转场作用域），并需要 `backdrop`（在 COUI `Scaffold` 的 `topBar` 中可省略）；示例中的 `BackHandler` 与 `IconButton` 分别来自 `androidx.activity:activity-compose` 与 Material 3。

## 基本用法

在同一个 `TopAppBarSurface` 内放置 `TopAppBarSearch`（占据标题槽）和 `TopAppBarSearchButton`（放在 `TopAppBarButtonGroup` 中），两者必须共享同一个 `CouiTopAppBarSearchState`：

~~~kotlin
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
~~~

接入要点：

1. **状态所有权**：`query`、`onQueryChange` 与 `expanded` 由页面管理；关闭时是否同时清空 `query` 由页面决定。
2. **焦点与键盘时序**：点击搜索按钮时同步置 `expanded = true` 与 `focusPending = true`，组件取得焦点后触发 `onFocused` 将请求清除；关闭时同步清除 `focusPending` 并调用 `keyboard?.hide()`。
3. **返回键拦截**：`BackHandler` 仅在 `expanded = true` 时启用，收起后自动释放返回拦截。

## 控制右侧常驻动作

需要控制顶栏右侧其他操作按钮在搜索展开时的去留时，用 `TopAppBarSearchAction` 包裹对应按钮：

~~~kotlin
@RequiresApi(34)
@Composable
fun SearchToolbarActionsExample(
    modifier: Modifier = Modifier,
    state: CouiTopAppBarSearchState,
    style: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(),
    resetLabel: String,
    settingsLabel: String,
    searchDescription: String,
    onReset: () -> Unit,
    onSettings: () -> Unit,
    onOpenSearch: () -> Unit,
) {
    TopAppBarButtonGroup(modifier = modifier) {
        TopAppBarSearchAction(state = state) {
            TopAppBarButton(onClick = onReset, style = style) {
                Icon(Icons.Default.Refresh, contentDescription = resetLabel)
            }
        }
        TopAppBarSearchAction(state = state, visibleWhileSearching = true) {
            TopAppBarButton(onClick = onSettings, style = style) {
                Icon(Icons.Default.Settings, contentDescription = settingsLabel)
            }
        }
        TopAppBarSearchButton(
            state = state,
            style = style,
            onClick = onOpenSearch,
            contentDescription = searchDescription,
        )
    }
}
~~~

- `visibleWhileSearching = false`（默认）：搜索展开时该动作在原位淡出并移除无障碍语义。
- `visibleWhileSearching = true`：搜索展开时该动作平滑移动至新布局位置并保持可点击。

## 从搜索胶囊分离自定义按钮

希望关闭按钮或扩展按钮在展开时从搜索胶囊右侧融合分离（类似“取消”胶囊），可传入 `action` 与 `onActionClick`（不再使用 `trailingIcon`）：

~~~kotlin
@RequiresApi(34)
@Composable
fun DetachedActionSearchExample(
    modifier: Modifier = Modifier,
    state: CouiTopAppBarSearchState,
    searchStyle: CouiToolbarStyle = CouiTopAppBarButtonDefaults.style(search = true),
    title: String,
    placeholder: String,
    actionLabel: String,
    query: String,
    focusPending: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onFocused: () -> Unit,
    onClose: () -> Unit,
) {
    TopAppBarSearch(
        modifier = modifier.fillMaxWidth(),
        state = state,
        style = searchStyle,
        query = query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        placeholder = placeholder,
        focusPending = focusPending,
        onFocused = onFocused,
        action = { Text(actionLabel) },
        onActionClick = onClose,
    ) {
        Text(title)
    }
}
~~~

- **切勿再套按钮或点击修饰符**：`action` 槽位仅放置文字、图标或非交互排版；外围胶囊材质、点击响应与 `Role.Button` 语义均由组件自身处理，不要在内部再套 `TopAppBarButton` 或 `Modifier.clickable`。
- **交互与宽度限制**：`onActionClick = null` 会单独禁用动作按钮；`enabled = false` 同时禁用输入与动作。关闭目标生效时动作立即停止输入与语义遍历。动作按钮在展开途中即可点击，宽度最多占可用搜索宽度的 40%（且受组件最大宽度限制），输入区使用剩余宽度；RTL 下动作位于逻辑末端。
- **`trailingIcon` 相互独立**：`trailingIcon` 位于输入框内部右侧，可与外侧融合动作按需要分别使用。

## 搜索框样式

搜索框默认使用 `CouiTopAppBarButtonDefaults.style(search = true)`。如需在[配置顶栏样式](top-app-bar.md#配置顶栏样式)的基础上自定义样式，请将 `material` 与 `shadow` 的 `search` 参数设为 `true`：

~~~kotlin
@Composable
fun rememberSearchToolbarStyle(
    dark: Boolean,
    accent: Color,
    contentColor: Color,
    textStyle: TextStyle,
): CouiToolbarStyle {
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    return remember(dark, accent, contentColor, textStyle, density, contour) {
        CouiToolbarStyle(
            dark = dark,
            circleShape = CircleShape,
            capsuleShape = CouiCapsuleShape,
            material = { capsule ->
                CouiTopAppBarButtonDefaults.material(dark, capsule, search = true)
            },
            shadow = { capsule ->
                CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour, search = true)
            },
            contentColor = contentColor,
            textStyle = textStyle,
            cursorColor = accent,
            contourGlowEnabled = contour,
        )
    }
}
~~~

注意：`TopAppBarSearch` 的 `visible` 参数仅控制整组搜索区域与标题的淡入淡出，不会修改 `expanded` 或清空 `query`；隐藏期间搜索框停止接受输入。

## 参数与行为速查

| 组件 / 状态 | 关键参数 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `rememberCouiTopAppBarSearchState` | `expanded` / `renderer` | 必填 / `standalone()` | 控制搜索展开目标与转场动画；由 `TopAppBarSearch` 与按钮共用 |
| `TopAppBarSearch` | `endPadding` | `6.dp` | 展开后搜索胶囊右侧预留间距 |
| `TopAppBarSearch` | `enabled` / `visible` | `true` / `true` | `enabled` 控制输入与动作交互；`visible` 控制整组淡化显隐 |
| `TopAppBarSearch` | `action` / `onActionClick` | `null` / `null` | 配置从胶囊融合分离的右侧独立动作 |
| `TopAppBarSearchAction` | `visibleWhileSearching` | `false` | `false` 原位淡出，`true` 随搜索展开平移保留 |

## 装配注意事项

- **占满标题剩余宽度**：`TopAppBarSearch` 会占满可用宽度，放入 `Row` 时请使用 `Modifier.weight(1f)`，再通过 `endPadding` 微调与右侧常驻按钮的间距。
- **处理顶栏安全区**：`TopAppBarSurface` 不自动消费状态栏安全区，内部 `Row` 需自行添加 `statusBarsPadding()`。
- **固定容器尺寸**：`TopAppBarSearchButton` 使用固定尺寸容器，不受 Material 3 最小交互尺寸 `Local` 影响；收起态的 `title` 保持宿主自身的文字样式，M3 插槽适配见[外观与预设](../guide/appearance.md)。
- **键盘自动弹出兼容性**：部分设备在请求焦点后可能不会自动弹出软键盘，用户可手动点击输入框拉起。

## 源码与完整示例

- **组件与动作**：[TopAppBarSearch.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarSearch.kt)、[TopAppBarSearchPair.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarSearchPair.kt)
- **搜索状态**：[CouiTopAppBarSearchState.kt](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearchState.kt)
- **完整页面与顶栏示例**：[DemoToolbarSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoToolbarSamples.kt)、[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
