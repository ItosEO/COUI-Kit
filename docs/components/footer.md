# Footer

`Footer` 用于在分组下方展示辅助说明文字。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：使用 `CouiTextKind.GroupFooter` 排版角色并按逻辑起始边对齐，内置水平与垂直留白；不依赖背景采样源。

## 基本用法

在需要独立放置说明文字的位置调用 `Footer`，说明文案由宿主传入：

~~~kotlin
@Composable
fun SectionFooterExample(
    modifier: Modifier = Modifier,
    description: String,
) {
    Footer(
        modifier = modifier,
        text = description,
    )
}
~~~

若说明文字直接跟随某张分组卡片，更推荐在 [Group](group.md) 中传入 `footer` 参数——`Group` 会自动在卡片下方装配 `Footer` 并复用该组的 `colors.summary`。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符；外部 `padding` 会与组件自带留白（水平 `32.dp`、垂直 `8.dp`）累加。 |
| `text` | 必填 | 辅助说明文案，采用 `CouiTextKind.GroupFooter` 角色并按 `TextAlign.Start` 对齐。 |
| `color` | `CouiTheme.colorScheme.onSurfaceVariant` | 说明文字颜色。 |

## 源码与完整示例

- **公开布局与预设**：[CouiPageComponents.kt](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
