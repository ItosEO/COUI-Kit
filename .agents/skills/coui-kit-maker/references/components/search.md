# TopAppBarSearch / SearchBar

## TopAppBarSearch

顶栏按钮点击后展开成输入胶囊时，用 `TopAppBarSearch`，放在提供效果作用域的 COUI 顶栏内。这个入口要求 API 34+；展开状态、查询词和返回键行为由页面持有。[顶栏搜索示例](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/search.md)

在 API 34+，标题槽和操作槽共享一个搜索状态。返回键和取消按钮共用关闭操作，恢复标题并清除本次查询。以下代码位于 COUI 顶栏槽；已有 M3 容器时，按[普通顶栏](top-app-bar.md#topappbar)显式传入正文背景源。低版本也要提供搜索时可用下方的常驻搜索框。[Demo 顶栏搜索](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoToolbarSamples.kt#L38)

```kotlin
var expanded by rememberSaveable { mutableStateOf(false) }
var focusPending by remember { mutableStateOf(false) }
var query by rememberSaveable { mutableStateOf("") }
val keyboard = LocalSoftwareKeyboardController.current
val closeSearch: () -> Unit = {
    expanded = false
    focusPending = false
    query = ""
    keyboard?.hide()
}
BackHandler(enabled = expanded, onBack = closeSearch)
val state = rememberCouiTopAppBarSearchState(expanded)
TopAppBar(
    title = {
        TopAppBarSearch(
            state = state, query = query, onQueryChange = { query = it },
            onSearch = onSearch, placeholder = hint, title = title,
            focusPending = focusPending, onFocused = { focusPending = false },
            action = { Text(cancelText) }, onActionClick = closeSearch,
        )
    },
    actions = {
        TopAppBarSearchButton(state = state, contentDescription = searchDescription, onClick = {
            expanded = true
            focusPending = true
        })
    },
)
```

## SearchBar

希望搜索框常驻页面时用 `SearchBar`。API 31–33 显示填色胶囊，API 34+ 支持浮动模糊材质。若结果列表盖在原列表上，保留底层滚动位置，并在搜索活动期间停用底层交互和无障碍焦点。[常驻搜索示例](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/search-bar.md)

将 `SearchBar` 放入 `CollapsingTopAppBar` 的搜索槽时，共用顶栏的搜索状态，把折叠进度传给阴影。[Demo 常驻搜索](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L92)

```kotlin
SearchBar(
    state = search, query = query, onQueryChange = onQueryChange,
    onExpandedChange = onExpandedChange, placeholder = hint,
    cancelText = cancelText, clearDescription = clearDescription,
    shadowProgress = header.collapsedFraction,
)
```
