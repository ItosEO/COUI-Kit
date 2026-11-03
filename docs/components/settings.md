# 分组与行布局

`Group`、`Item`、`Divider` 和 `Footer` 用于组合设置式列表，均位于 `com.itos.coui.layout`。可以将它们放入现有 Column、LazyColumn 或其他页面容器。

| 组件 | 用途 |
| --- | --- |
| `Group` | 圆角卡片及可选的标题、标题操作和说明 |
| `Item` | 图标、主文字、辅助文字、尾部内容及可选整行点击 |
| `Divider` | 同组条目之间的分隔线 |
| `Footer` | 分组下方的辅助说明 |

## 基本用法

下面的函数只依赖 Foundation 和 COUI。文案与导航回调由宿主传入。

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.text.Text
import com.itos.coui.text.CouiTextKind

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
                Text(text = summary, kind = CouiTextKind.PreferenceSummary, color = colors.summary)
            },
            onClick = onClick,
        ) {
            Text(text = title, kind = CouiTextKind.PreferenceTitle, color = colors.title)
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

`Group` 根据是否有标题选择顶部留白，并为卡片提供水平留白和平滑圆角。外部 `Modifier.padding` 会与这些留白累加。需要完全自定组间距或卡片结构时，使用普通布局与 `CouiPageDefaults` 组合。

`headerActions` 位于标题行末端，仅在 `title` 非空时显示。

`Item.contentPadding` 控制行内留白，`iconSize` 控制图标槽。外部尺寸使用标准 `Modifier.height` / `heightIn`；默认最小行高服从父级约束。

`Divider.startIndent` 和 `endIndent` 用于对齐同组文字。分隔线默认至少占一个物理像素，实际高度服从父级约束。独立卡片之间使用间距。

使用默认行布局时，带图标的分隔线起始缩进为 `CouiPageDefaults.rowPadding + CouiPageDefaults.iconSize + CouiPageDefaults.iconSpacing`。

自定义行内留白或图标尺寸时，将同一组值用于 Item 和分隔线。在宿主 Composable 中计算：

```kotlin
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

val startIndent = contentPadding.calculateStartPadding(LocalLayoutDirection.current) +
    if (icon != null) iconSize + CouiPageDefaults.iconSpacing else 0.dp
```

这里的 `contentPadding`、`iconSize` 和 `icon` 是传给 Item 的实际参数。将结果传给 `Divider(startIndent = startIndent)`；分隔线不会自动读取相邻行的尺寸。

## 配色与文字

| 入口 | 作用范围 |
| --- | --- |
| `Group.colors` | 本组卡片、标题和说明 |
| `Item.dark` | 本行点击反馈 |
| `Divider.color` | 当前分隔线 |
| `Footer.color` | 当前说明文字 |

默认配色由组件读取 `CouiTheme`。`CouiPageDefaults.themeColors(dark)` 解析当前主题；`colors(accent, dark)` 从原生浅色或深色预设生成独立配色值。可用 `copy` 覆盖所需角色。`Group.colors` 不改变内容槽的主题，自定义文字和 Divider 需要显式接收对应颜色。

`Text.kind` 选择固定的 COUI 排版角色；`rememberCouiTextStyle(kind)` 将同一排版用于 BasicText 或编辑框。这些角色使用系统字体与对应度量，不读取 `CouiTypography` 的普通文字样式。PreferenceTitle、PreferenceSummary、Body 和 Description 默认按逻辑起始边对齐，其他角色默认居中，`textAlign` 可覆盖；`onLineCount` 返回实际布局的行数。

需要限制行数或显示省略号时，设置 `maxLines = 1` 和 `overflow = TextOverflow.Ellipsis`；`TextOverflow` 来自 `androidx.compose.ui.text.style`。默认不限制行数，溢出策略为 `Clip`，`maxLines` 必须大于零。

`Text.color` 省略或为 `Color.Unspecified` 时，先使用已提供的 `LocalCouiContentColor`，再使用当前 `CouiTheme.colorScheme.onSurface`。没有 Theme 或 Provider 也能跟随系统深浅色；显式颜色始终优先。需要区分主文字与辅助文字时，可像示例一样传入各自角色。主题和可选 M3 适配的用法见[外观与预设](../guide/appearance.md)。

## 页面装配

这些组件不安装滚动、不消费系统栏 Insets，也不创建背景采样源。页面负责布局、导航和受控值。需要 COUI 惯性与回弹时使用 [couiVerticalScroll](../effects/overscroll.md)；需要模糊顶栏或浮层时按[宿主页面接入](../guide/secondary-pages.md)装配采样。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt) · [文字 API](../../coui/src/main/java/com/itos/coui/text/Text.kt) · [设置列表示例](../../demo/src/main/java/com/itos/coui/demo/DemoListSamples.kt)
