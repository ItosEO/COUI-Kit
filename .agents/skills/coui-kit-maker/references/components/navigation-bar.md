# NavigationBar 底栏

页面用当前路由决定选中项，点击时交给既有导航处理。未选中与选中图标配对使用同名的 `Outlined` 和 `Filled`。中间导航项自带状态着色，图标资源再预乘状态透明度会使其过淡。

在 API 34+ 将底栏放进 COUI `Scaffold.bottomBar` 即可取得正文背景源。底栏已处理底部安全区，再叠一层 `navigationBarsPadding()` 会把胶囊抬高。[NavigationBar 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/navigation-bar.md)

在 `bottomBar` 槽中将页面的 `List<CouiNavigationItem>`、当前页下标和导航回调直接传入，保留默认胶囊材质和融合动画。[Demo 首页底栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L107)

```kotlin
NavigationBar(
    items = destinations,
    selectedIndex = currentPage,
    onSelection = onNavigate,
)
```

页面支持 API 31–33 时，把已有的普通底栏放在同一个 `bottomBar` 槽，复用相同的目的地、当前选中项和导航回调。API 34+ 若浮动绘制器创建失败，也走这个可导航的分支，并把失败交给宿主报告。Demo 的 `PlainDemoBottomBar` 是这种接法的示例。[Demo 普通底栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L121)
