# ChipTab / ChipTabRow 分类栏

一排可滚动的单选分类用 `ChipTabRow`。若前面还有打开筛选菜单的图标，把它放在 `leadingAction` 里，用 `ChipTabButton` 触发菜单；这样打开筛选不会改动当前分类。

API 34+ 的浮动分类栏需要背景源。在 `BottomSheet` 内，`ChipTabRow` 可读取面板底图；显式提供页面背景源时，将分类栏放到录制节点之外。[ChipTab 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/chip-tab.md)

把不透明底色录在背景节点上，分类栏与它并列，让每项的轮廓和投影在父容器内有绘制空间。`items` 使用稳定且唯一的 key，`selectedKey` 对应当前分类；前置菜单按钮单独处理点击。[Demo 分类栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt#L81)

```kotlin
Box {
    Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(pageColor))
    ChipTabRow(
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = onSelectCategory,
        backdrop = backdrop,
        leadingAction = {
            ChipTabButton(
                onClick = onOpenCategories,
                contentDescription = categoriesDescription,
                backdrop = backdrop,
                icon = categoriesIcon,
            )
        },
    )
}
```
