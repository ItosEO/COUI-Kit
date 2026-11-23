# CouiIcons 图标

底栏用同名 `Outlined` / `Filled` 表示未选中与选中，设置行打开选择菜单用 `Popup` 上下双箭头。`SearchBar` 和 `TopAppBarSearch` 都内置搜索图标，额外再放一个会重复。

多色图标要保留资源内置配色时，用 `Image`，或在 M3 `Icon` 上传 `tint = Color.Unspecified`。普通单色操作图标按组件内容色绘制。[CouiIcons 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/icons.md)

导航目的地同时提供两套图形，用文字作为可访问名称；独立的“添加”按钮沿用按钮槽给出的内容色。[Demo 导航图标](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoApp.kt#L107) · [Demo 添加按钮](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoChromeDetail.kt#L124)

```kotlin
val destination = CouiNavigationItem(title = title, icon = outlinedIcon, selectedIcon = filledIcon)
Icon(CouiIcons.Filled.Add, contentDescription = addDescription)
```
