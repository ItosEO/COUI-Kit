# Group

`Group` 将相邻条目放入同一张平滑圆角卡片，可提供标题、标题右侧操作和底部说明。组件位于 `com.itos.coui.layout`。

```kotlin
Group(title = groupTitle, footer = description) {
    Item(title = { Text(text = title, kind = CouiTextKind.PreferenceTitle) })
}
```

`groupTitle`、`description` 和 `title` 由宿主提供。条目装配见 [Item](item.md)，组内分隔见 [Divider](divider.md)。

## 标题与操作

`headerActions` 位于标题行末端，仅在 `title` 非空时显示。槽位参与标题行高度测量；紧凑图标操作可使用 20dp 容器与 18dp 图标，避免按钮默认最小尺寸撑高标题行。图标操作可使用强调色并隐藏点击涟漪，同时保留按钮语义和操作描述。

`footer` 非空时在卡片下方显示说明，独立说明使用 [Footer](footer.md)。

## 尺寸与配色

`Group` 根据是否有标题选择顶部留白，并为卡片提供水平留白和平滑圆角。外部 `Modifier.padding` 会与这些留白累加。需要完全自定组间距或卡片结构时，使用普通布局与 `CouiPageDefaults` 组合。

`colors` 控制本组卡片、标题和说明，不改变内容槽的主题。自定义文字和 Divider 需要显式接收对应颜色。默认使用 `CouiPageDefaults.themeColors()`；配色规则见[外观与预设](../guide/appearance.md)。

组件不安装滚动、不消费系统栏 Insets，也不创建背景采样源。页面装配见[宿主页面接入](../guide/secondary-pages.md)。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt) · [Demo 示例](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
