# Item

`Item` 组合图标、主文字、辅助文字、尾部内容与可选整行点击，可直接放入 `Column`、`LazyColumn` 或 [Group](group.md)。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：内置最小行高（`48.dp`）与标准内边距、可选整行点击及 COUI 列表反馈；不依赖背景采样源。

| 组件 | 用途 |
| --- | --- |
| [Group](group.md) | 平滑圆角卡片及可选的标题、标题操作和底部说明 |
| `Item` | 图标、主文字、辅助文字、尾部内容及可选整行点击 |
| [Divider](divider.md) | 同组条目之间的细分隔线 |
| [Footer](footer.md) | 分组下方的辅助说明 |

## 基本用法

以下示例仅依赖 Foundation 与 COUI，分组标题、主副文案与点击回调均由宿主传入：

~~~kotlin
@Composable
fun AboutRowExample(
    modifier: Modifier = Modifier,
    groupTitle: String,
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors()
    Group(
        modifier = modifier,
        title = groupTitle,
        colors = colors,
    ) {
        Item(
            summary = {
                Text(
                    text = summary,
                    kind = CouiTextKind.ItemSummary,
                    color = colors.summary,
                )
            },
            onClick = onClick,
        ) {
            Text(
                text = title,
                kind = CouiTextKind.ItemTitle,
                color = colors.title,
            )
        }
    }
}
~~~

`title` 为必填槽位（可用尾随 lambda 提供）；`icon` 放左侧图标，`summary` 放辅助文字，`trailing` 放当前值、指示箭头或交互控件。各内容槽位均可使用 COUI、Material 3 或宿主自定义组件。

## 点击与选择

`Item` 根据是否传入 `onClick` 区分两种交互模式：

- **普通点击行（传入 `onClick`）**：整行及 `contentPadding` 区域均可点击，自动安装 `Role.Button` 语义与 COUI 列表按压反馈。整行承担点击时，尾部控件不要再安装第二份点击回调。
- **整行开关或单选（省略 `onClick`）**：不添加点击、按钮语义或语义合并，内部子控件保留各自交互。由宿主在 `Item` 上组合 `toggleable` 或 `selectable`（配合父级 `selectableGroup`）；需要 COUI 列表反馈时，将同一个 `MutableInteractionSource` 同时传给交互 Modifier 与 `couiButtonFeedback(dark, source, enabled, list = true)`。
- **`enabled` 作用边界**：`Item.enabled` 仅控制 `Item` 自身的点击与列表反馈；放在 `trailing` 中的 `Switch`、输入框等独立控件必须单独传入 `enabled`（整行开关示例见[开关整行点击](switch.md#整行点击)）。

## 尺寸与留白

- **`contentPadding`**：控制行内留白，默认水平 `CouiPageDefaults.rowPadding`（`16.dp`）、垂直 `CouiPageDefaults.contentPadding`（`10.dp`）。
- **`iconSize`**：控制左侧图标槽尺寸，默认 `CouiPageDefaults.iconSize`（`24.dp`），与文字间距为 `CouiPageDefaults.iconSpacing`（`16.dp`）。
- **外部高度约束**：默认最小行高为 `CouiPageDefaults.rowMinHeight`（`48.dp`），外部可通过 `Modifier.height` 或 `Modifier.heightIn` 调整（与图标行对齐的分隔线计算见 [Divider](divider.md#与图标行对齐)）。

## 配色与文字

| 入口 | 作用范围 |
| --- | --- |
| `Group.colors` | 本组卡片、标题与底部说明 |
| `Item.dark` | 本行点击反馈的明暗模式 |
| `Divider.color` | 当前分隔线 |
| `Footer.color` | 当前说明文字 |

### 页面配色解析

组件默认从当前 `CouiTheme` 解析配色：

- `CouiPageDefaults.themeColors(dark)` 根据当前主题与深浅模式返回 `CouiPageColors`。
- `CouiPageDefaults.colors(accent, dark)` 从原生浅色或深色预设生成独立 `CouiPageColors`，可用 `.copy(...)` 覆盖指定角色。
- `Group.colors` **不会改变**内容槽内子组件的主题；使用 `Text` 或 `Divider` 时需按需显式传入对应颜色（通用规则见[外观与预设](../guide/appearance.md)）。

### COUI 文字排版（`Text` 与 `rememberCouiTextStyle`）

- **排版角色（`CouiTextKind`）**：选择固定的 COUI 系统字体与度量预设（不读取 `CouiTypography` 的普通文本样式）。`ItemTitle`、`ItemSummary` 与 `GroupFooter` 默认按逻辑起始边对齐（`TextAlign.Start`），其余角色默认居中，均可传入 `textAlign` 覆盖。从旧版升级时，将 `CouiTextKind.PreferenceTitle` 改为 `CouiTextKind.ItemTitle`。
- **颜色优先级**：省略 `Text.color` 或传入 `Color.Unspecified` 时，依次使用已提供的 `LocalCouiContentColor.current` 与当前 `CouiTheme.colorScheme.onSurface`；显式传入 `color` 始终优先。未包 `CouiTheme` 时也能跟随系统深浅色。
- **行数与溢出**：默认 `maxLines = Int.MAX_VALUE`、`overflow = TextOverflow.Clip`；需要单行省略时设 `maxLines = 1` 与 `overflow = TextOverflow.Ellipsis`（`maxLines` 必须大于 `0`）。`onLineCount` 回调返回实际排版行数。
- **自定义文本复用**：在 `BasicText` 或输入框中使用同一排版时，调用 `rememberCouiTextStyle(kind)` 获取 `TextStyle`。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符。 |
| `icon` | `null` | 左侧图标槽位；非空时在行首占据 `iconSize` 区域并与文字间隔 `CouiPageDefaults.iconSpacing`。 |
| `iconSize` | `CouiPageDefaults.iconSize` | 左侧图标槽宽高（默认 `24.dp`）。 |
| `summary` | `null` | 主文字下方的辅助说明槽位。 |
| `trailing` | `{}` | 行尾内容槽位（如值文字、箭头、开关）。 |
| `onClick` | `null` | 非空时启用整行点击、`Role.Button` 语义与 COUI 列表反馈；为空时仅作布局排列。 |
| `enabled` | `true` | 仅控制 `Item` 自身的点击与反馈，不影响 `trailing` 内子控件的启用态。 |
| `dark` | `CouiTheme.isDark` | 控制本行列表反馈的深浅色模式。 |
| `contentPadding` | `PaddingValues(horizontal = 16.dp, vertical = 10.dp)` | 行内留白，也参与整行点击热区。 |
| `title` | 必填 | 主文字内容槽位（支持尾随 lambda）。 |

## 页面装配

`Item` 与分组组件不安装滚动、不消费系统栏 Insets，也不创建背景采样源。页面负责列表容器、导航与状态管理：

- 需要 COUI 惯性与边缘回弹时，使用 [couiVerticalScroll](../effects/overscroll.md)。
- 需要模糊顶栏或浮层背景采样时，按[宿主页面接入](../guide/secondary-pages.md)配置背景源。

## 源码与完整示例

- **公开布局与预设**：[CouiPageComponents.kt](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
- **文字 API**：[Text.kt](../../coui/src/main/java/com/itos/coui/text/Text.kt)
- **Demo 完整示例**：[DemoSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
