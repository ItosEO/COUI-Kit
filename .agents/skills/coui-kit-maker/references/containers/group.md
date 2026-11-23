# Group / Item / Divider / Footer

## Group

将相邻设置项放进同一个 `Group`，组内使用 `Item` 和 `Divider`，组标题和底部说明分别传给 `title` 和 `footer`。组件自带水平边距和组间距，再给父容器加同方向的边距会叠加留白。

`Group.colors` 控制卡片、组标题和底部说明。自定义整组配色时，将需要覆盖的文字色和分隔线色传给对应子组件。希望跟随 M3 动态色时从 `CouiPageDefaults.themeColors()` 取色。[Group 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/group.md)

页面提供两行的标题槽及点击回调。使用 `footer` 后，`Group` 会在卡片下方绘制说明；需要放在卡片外的独立说明时用 `Footer(text = note)`。[Demo 分组](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSamples.kt#L54) · [Demo 条目](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceRows.kt#L140)

```kotlin
Group(title = groupTitle, footer = note) {
    Item(onClick = onOpenFirst, title = firstTitle)
    Divider()
    Item(onClick = onOpenSecond, title = secondTitle)
}
```

## Item

普通跳转行用 `Item.onClick`。整行开关由行的 `toggleable` 处理，见 [Switch](../components/switch.md)；整行弹菜单用同一个触点锚点，见 [Menu](../components/menu.md#设置行菜单)。[Item 槽位](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/item.md)

设置项禁用时，标题、摘要和控件应一起显示禁用色。`Item` 把文字绘制交给槽位，`enabled` 控制它自己的点击反馈。下面的 `Text` 来自 `com.itos.coui.text`，文案、启用状态和点击回调由页面提供。[Demo 设置行](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceRows.kt#L139)

```kotlin
val colors = CouiPageDefaults.themeColors()
Item(
    enabled = enabled,
    onClick = onOpen,
    title = {
        Text(text = title, kind = CouiTextKind.ItemTitle,
            color = if (enabled) colors.title else colors.disabled)
    },
    summary = {
        Text(text = summary, kind = CouiTextKind.ItemSummary,
            color = if (enabled) colors.summary else colors.disabled)
    },
)
```

## Divider 与 Footer

同一卡片内的条目之间用 `Divider`，有前导图标时让 `startIndent` 对齐标题起始边。组下说明可直接传给 `Group.footer`；独立放置时用 `Footer` 并沿用它的内置留白。[Divider 缩进](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/divider.md) · [Footer 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/footer.md)
