# Scaffold 页面容器

在 COUI `Scaffold` 中，将顶栏、底栏和悬浮按钮放到所属槽位，它们可以直接采样正文。列表接收容器回传的 `padding`，用 `contentPadding = padding` 排版，并用 `consumeWindowInsets(padding)` 告知子树这些区域已处理。

```kotlin
Scaffold(
    topBar = { TopAppBar(title = title) },
    containerColor = CouiPageDefaults.themeColors().background,
) { padding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
        contentPadding = padding,
        content = rows,
    )
}
```

标题和列表项由页面提供。Demo 同样在槽位放置栏位，使用普通 `Column` 时把正文留白放在滚动修饰符之后。[Demo 页面布局](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L88)

如果上下栏外侧多出一截空白，检查是否在 COUI 栏位外又加了一次系统栏留白。宿主已有 M3 `Scaffold` 时也能直接加入 COUI 顶栏，接线见 [TopAppBar](../components/top-app-bar.md)。[Scaffold 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/scaffold.md)
