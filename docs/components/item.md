# Item

`Item` 组合图标、主文字、辅助文字、尾部内容和可选整行点击，位于 `com.itos.coui.layout`。可以放入现有 Column、LazyColumn 或 [Group](group.md)。

| 组件        | 用途                      |
|-----------|-------------------------|
| [Group](group.md) | 圆角卡片及可选的标题、标题操作和说明 |
| `Item`    | 图标、主文字、辅助文字、尾部内容及可选整行点击 |
| [Divider](divider.md) | 同组条目之间的分隔线 |
| [Footer](footer.md) | 分组下方的辅助说明 |

## 基本用法

下面的函数只依赖 Foundation 和 COUI。文案与导航回调由宿主传入。

```kotlin
@Composable
fun AboutRow(
    modifier: Modifier = Modifier,
    groupTitle: String,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    Group(modifier = modifier, title = groupTitle) {
        Item(
            summary = {
                Text(text = summary, kind = CouiTextKind.ItemSummary, color = colors.summary)
            },
            onClick = onClick,
        ) {
            Text(text = title, kind = CouiTextKind.ItemTitle, color = colors.title)
        }
    }
}
```

`title` 是必填内容槽，可用尾随 lambda 提供。`icon` 放图标，`summary` 放辅助文字，`trailing` 放值或控件。内容槽可以使用 COUI、Material、Miuix 或宿主自己的组件。

## 点击与选择

提供 `Item.onClick` 时，整行及其内边距均可点击，并使用 COUI 列表反馈。省略时只排列内容，子控件保留各自的交互和语义；整行语义合并由 `clickable`、`toggleable` 或 `selectable` 负责。

整行开关与单选使用宿主的 `toggleable` / `selectable`，并省略 `Item.onClick`。需要 COUI 反馈时，将同一个 `MutableInteractionSource` 传给交互 Modifier 和 `couiButtonFeedback(list = true)`。整行承担点击时，尾部控件不再安装第二份点击回调；独立拖动等操作按该控件的公开 API 装配。

`Item.enabled` 只控制本行自身的点击和反馈。尾部开关、输入框等独立控件需要单独传入 `enabled`。

## 尺寸与留白

`Item.contentPadding` 控制行内留白，`iconSize` 控制图标槽。外部尺寸使用标准 `Modifier.height` / `heightIn`；默认最小行高服从父级约束。

与图标和文字对齐的分隔线配置见 [Divider](divider.md)。

## 配色与文字

| 入口              | 作用范围       |
|-----------------|------------|
| `Group.colors`  | 本组卡片、标题和说明 |
| `Item.dark`     | 本行点击反馈     |
| `Divider.color` | 当前分隔线      |
| `Footer.color`  | 当前说明文字     |

默认配色由组件读取 `CouiTheme`。`CouiPageDefaults.themeColors(dark)` 解析当前主题；`colors(accent, dark)` 从原生浅色或深色预设生成独立配色值。可用 `copy` 覆盖所需角色。`Group.colors` 不改变内容槽的主题，自定义文字和 Divider 需要显式接收对应颜色。

`Text.kind` 选择固定的 COUI 排版角色。`rememberCouiTextStyle(kind)` 将同一排版用于 BasicText 或编辑框。这些角色使用系统字体与对应度量，不读取 `CouiTypography` 的普通文字样式。`ItemTitle`、`ItemSummary` 和 `GroupFooter` 默认按逻辑起始边对齐，其他角色默认居中。可用 `textAlign` 覆盖对齐方式。`onLineCount` 返回实际布局的行数。

从旧版升级时，将 `CouiTextKind.PreferenceTitle` 改为 `CouiTextKind.ItemTitle`。

需要限制行数或显示省略号时，设置 `maxLines = 1` 和 `overflow = TextOverflow.Ellipsis`；`TextOverflow` 来自 `androidx.compose.ui.text.style`。默认不限制行数，溢出策略为 `Clip`，`maxLines` 必须大于零。

`Text.color` 省略或为 `Color.Unspecified` 时，先使用已提供的 `LocalCouiContentColor`，再使用当前 `CouiTheme.colorScheme.onSurface`。没有 Theme 或 Provider 也能跟随系统深浅色；显式颜色始终优先。需要区分主文字与辅助文字时，可像示例一样传入各自角色。主题和可选 M3 适配的用法见[外观与预设](../guide/appearance.md)。

## 页面装配

这些组件不安装滚动、不消费系统栏 Insets，也不创建背景采样源。页面负责布局、导航和受控值。需要 COUI 惯性与回弹时使用 [couiVerticalScroll](../effects/overscroll.md)；需要模糊顶栏或浮层时按[宿主页面接入](../guide/secondary-pages.md)装配采样。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt) · [文字 API](../../coui/src/main/java/com/itos/coui/text/Text.kt) · [Demo 示例](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
