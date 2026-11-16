# Group

`Group` 将相邻设置条目放入同一张平滑圆角卡片，并支持可选分组标题、标题栏右侧操作与底部说明。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：平滑圆角卡片（`CouiPageDefaults.shape`）、自动水平边距与组间距、局部配色覆盖；不依赖背景采样源。

## 基本用法

在宿主 Composable 中组合 `Group` 与 [Item](item.md)，分组标题、条目文案与底部说明均由宿主传入：

~~~kotlin
@Composable
fun SettingsGroupExample(
    modifier: Modifier = Modifier,
    groupTitle: String,
    rowTitle: String,
    footerText: String,
    onRowClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    Group(
        modifier = modifier,
        title = groupTitle,
        footer = footerText,
        colors = colors,
    ) {
        Item(onClick = onRowClick) {
            Text(
                text = rowTitle,
                kind = CouiTextKind.ItemTitle,
                color = colors.title,
            )
        }
    }
}
~~~

组内多条设置项之间使用 [Divider](divider.md) 分隔；需要独立放在卡片外的辅助说明请使用 [Footer](footer.md)。

## 标题与操作（`headerActions`）

当 `title` 非空时，`Group` 会在卡片上方渲染标题行，并将 `headerActions` 放在标题行末端（`RowScope`）：

- **条件渲染**：`title` 为 `null` 或空字符串时，整行标题（含 `headerActions`）都不会渲染。
- **紧凑图标尺寸**：`headerActions` 参与标题行高度测量。放置图标按钮时建议使用 `20.dp` 容器配合 `18.dp` 图标，避免默认 `48.dp` 最小点击尺寸撑高标题行；同时可按需隐藏点击涟漪并保留按钮语义与无障碍描述。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符；若包含 `padding`，会在组件内置留白基础上累加。 |
| `title` | `null` | 分组标题。非空时显示标题行并使用 `CouiTextKind.GroupTitle` 样式与 `heading()` 语义。 |
| `footer` | `null` | 分组底部说明。非空时自动在卡片下方装配 `Footer` 并使用 `colors.summary`。 |
| `headerActions` | `{}` | 标题行末端操作槽位（`RowScope`），仅在 `title` 非空时渲染。 |
| `colors` | `CouiPageDefaults.themeColors()` | 控制本组卡片底色、组标题与底部说明颜色，**不会改变** `content` 槽位内子组件的主题。 |
| `content` | 必填 | 卡片内容槽位（`ColumnScope`），通常放入 `Item` 与 `Divider`。 |

## 装配注意事项

- **条件隐藏子条目**：按条件省略卡片内的某个 `Item` 时，组件不会为其保留空白或额外圆角端点。
- **留白累加规则**：`Group` 会根据 `title` 是否为空自动选择顶部间距（有标题取 `CouiPageDefaults.categoryTopSpacing`，无标题取 `CouiPageDefaults.groupSpacing`），并为卡片添加水平边距（`CouiPageDefaults.horizontalMargin`）。需要完全自定义组间距或卡片结构时，请直接使用普通布局配合 `CouiPageDefaults`。
- **内容槽显式着色**：`colors` 不向下覆盖子组件主题；使用自定义 `Text` 或 `Divider` 时，请显式传入对应的 `colors.title`、`colors.summary` 或 `colors.divider`（配色规则详见[外观与预设](../guide/appearance.md)）。
- **页面职责边界**：`Group` 不安装滚动、不消费系统栏 Insets，也不创建背景采样源；整页装配请见[宿主页面接入](../guide/secondary-pages.md)。

## 源码与完整示例

- **公开布局与预设**：[CouiPageComponents.kt](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
- **Demo 完整示例**：[DemoSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
