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
import com.itos.coui.text.CouiText
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
            supporting = {
                CouiText(text = summary, kind = CouiTextKind.PreferenceSummary, color = colors.summary)
            },
            onClick = onClick,
        ) {
            CouiText(text = title, kind = CouiTextKind.PreferenceTitle, color = colors.title)
        }
    }
}
```

`headline` 是必填内容槽，可用尾随 lambda 提供。`leading` 放图标，`supporting` 放辅助文字，`trailing` 放值或控件。内容槽可以使用 COUI、Material、Miuix 或宿主自己的组件。

## 点击与选择

`Item.onClick` 非空时，整行及其内边距参与点击，并使用 COUI 列表反馈。省略 `onClick` 时，`Item` 只排列内容，不安装点击、按钮语义或反馈。

整行开关与单选使用宿主的 `toggleable` / `selectable`，并省略 `Item.onClick`。需要 COUI 反馈时，将同一个 `MutableInteractionSource` 传给交互 Modifier 和 `couiButtonFeedback(list = true)`。整行承担点击时，尾部控件不再安装第二份点击回调；独立拖动等操作按该控件的公开 API 装配。

`Item.enabled` 只控制本行自身的点击和反馈。尾部开关、输入框等独立控件仍需接收自己的 `enabled`。`pressFeedback = false` 可关闭反馈 Modifier 的按压部分，hover 和焦点反馈继续生效。

## 尺寸与留白

`Group` 根据是否有标题选择顶部留白，并为卡片提供水平留白和平滑圆角。外部 `Modifier.padding` 会与这些留白累加。需要完全自定组间距或卡片结构时，使用普通布局与 `CouiPageDefaults` 组合。

`Item.contentPadding` 控制行内留白，`leadingSize` 控制图标槽。外部尺寸使用标准 `Modifier.height` / `heightIn`；默认最小行高服从父级约束。

`Divider.startIndent` 和 `endIndent` 用于对齐同组文字。默认厚度按密度取整，至少占一个物理像素；显式尺寸和父级约束仍决定实际高度。独立卡片之间使用间距，不需要再加分隔线。图示使用普通 Box、Row 和 Modifier 组合。

## 配色与文字

| 入口 | 作用范围 |
| --- | --- |
| `Group.colors` | 本组卡片、标题和说明 |
| `Item.dark` | 本行点击反馈 |
| `Divider.color` | 当前分隔线 |
| `Footer.color` | 当前说明文字 |

默认配色由组件读取 `CouiTheme`。`CouiPageDefaults.themeColors(dark)` 解析当前主题；`colors(accent, dark)` 从原生浅色或深色预设生成独立配色值。可用 `copy` 覆盖所需角色。`Group.colors` 不改变内容槽的主题，自定义文字和 Divider 需要显式接收对应颜色。

`CouiText.kind` 选择固定的 COUI 排版角色；`rememberCouiTextStyle(kind)` 将同一排版用于 BasicText 或编辑框。这些角色使用系统字体与对应度量，不读取 `CouiTypography` 的普通文字样式。PreferenceTitle 和 PreferenceSummary 默认按逻辑起始边对齐，`textAlign` 可覆盖；`onLineCount` 返回实际行数。

`CouiText.color` 默认读取 `LocalCouiContentColor`。在没有提供该 Local 的独立布局中，应像示例一样显式传入主题颜色。主题和可选 M3 适配的用法见[外观与预设](../guide/appearance.md)。

## 页面装配

这些组件不安装滚动、不消费系统栏 Insets，也不创建背景采样源。页面负责布局、导航和受控值。需要 COUI 惯性与回弹时使用 [couiVerticalScroll](../effects/overscroll.md)；需要模糊顶栏或浮层时按[宿主页面接入](../guide/secondary-pages.md)装配采样。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt) · [文字 API](../../coui/src/main/java/com/itos/coui/text/CouiText.kt) · [设置列表示例](../../demo/src/main/java/com/itos/coui/demo/DemoListSamples.kt)
