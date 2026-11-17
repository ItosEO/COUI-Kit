# Divider

`Divider` 用于在同一 [Group](group.md) 卡片内的相邻条目之间绘制细分隔线；独立卡片之间请使用间距而非分隔线。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：默认厚度至少为 1 个物理像素（基于 `CouiPageDefaults.dividerHeight` 换算并取整），支持左右缩进与颜色覆盖；不依赖背景采样源。

## 基本用法

在同组相邻的 [Item](item.md) 之间直接调用 `Divider()`：

~~~kotlin
@Composable
fun GroupedDividerExample(
    modifier: Modifier = Modifier,
    firstTitle: String,
    secondTitle: String,
) {
    Group(modifier = modifier) {
        Item {
            Text(text = firstTitle, kind = CouiTextKind.ItemTitle)
        }
        Divider()
        Item {
            Text(text = secondTitle, kind = CouiTextKind.ItemTitle)
        }
    }
}
~~~

## 与图标行对齐

`Divider` 不会自动读取相邻 `Item` 的留白或图标尺寸。当上方条目包含图标时，需通过 `startIndent` 将分隔线起点与文字起始边对齐：

- **使用 `Item` 默认留白与图标尺寸时**：起始缩进为 `CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing`。
- **自定义行内留白或图标尺寸时**：将传给 `Item` 的同一组 `contentPadding`、`iconSize` 与图标有无判断在宿主中计算后传入 `startIndent`：

~~~kotlin
@Composable
fun IconAlignedDivider(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    iconSize: Dp,
    hasIcon: Boolean,
) {
    val startIndent = contentPadding.calculateStartPadding(LocalLayoutDirection.current) +
        if (hasIcon) iconSize + CouiPageDefaults.iconSpacing else 0.dp
    Divider(
        modifier = modifier,
        startIndent = startIndent,
    )
}
~~~

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符。 |
| `startIndent` | `CouiPageDefaults.rowPadding` | 逻辑起始边缩进，默认与无图标 `Item` 文字左对齐。 |
| `endIndent` | `CouiPageDefaults.rowPadding` | 逻辑结束边缩进。 |
| `color` | `CouiTheme.colorScheme.outlineVariant` | 分隔线颜色；在 `Group` 内覆盖了 `colors` 时可传入 `colors.divider`。 |

## 源码与完整示例

- **公开布局与预设**：[CouiPageComponents.kt](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
