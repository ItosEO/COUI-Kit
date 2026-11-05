# Divider

`Divider` 在同组条目之间绘制细分隔线，位于 `com.itos.coui.layout`。独立卡片之间使用间距。

```kotlin
Divider()
```

`startIndent` 和 `endIndent` 用于对齐同组文字。分隔线默认至少占一个物理像素，实际高度服从宿主约束。`color` 控制当前分隔线，默认读取 `CouiTheme.colorScheme.outlineVariant`。

## 与图标行对齐

使用 [Item](item.md) 默认布局时，带图标的分隔线起始缩进为 `CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing`。

自定义行内留白或图标尺寸时，将同一组值用于 Item 和分隔线。在宿主 Composable 中计算：

```kotlin
val startIndent = contentPadding.calculateStartPadding(LocalLayoutDirection.current) +
    if (icon != null) iconSize + CouiPageDefaults.iconSpacing else 0.dp
Divider(startIndent = startIndent)
```

这里的 `contentPadding`、`iconSize` 和 `icon` 是传给 Item 的实际参数。分隔线不会自动读取相邻行的尺寸。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
