# 分组与行布局

`Group`、`Item`、`Divider` 和 `Footer` 可以独立放入 Material、Miuix 或宿主自己的页面。不需要库内 Scaffold、专用页面主题或导航容器。它们仍位于 `com.itos.coui.layout`；`CouiPageColors` 和 `CouiPageDefaults` 保留原名。

| 库内入口 | 职责 |
| --- | --- |
| `Group` | 平滑圆角卡片、可选标题、说明和 headerActions；内容槽自由组合 |
| `Item` | leading、headline、supporting、trailing 与行内留白；可选整行点击 |
| `Divider` / `Footer` | 组内分隔与辅助说明 |

## 直接放入已有布局

以下函数只使用 Foundation 与 COUI；可以放入已有 Column、LazyColumn 的 item 或其他页面容器。文案、控件状态和业务回调由宿主持有。

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.text.CouiText
import com.itos.coui.text.CouiTextKind

@Composable
fun AboutRow(
    modifier: Modifier = Modifier,
    groupTitle: String,
    title: String,
    summary: String,
    dark: Boolean,
    onClick: () -> Unit,
) {
    val colors = CouiPageDefaults.themeColors(dark)
    Group(modifier = modifier, title = groupTitle, colors = colors) {
        Item(
            supporting = {
                CouiText(text = summary, kind = CouiTextKind.PreferenceSummary, color = colors.summary)
            },
            onClick = onClick,
            dark = dark,
        ) {
            CouiText(text = title, kind = CouiTextKind.PreferenceTitle, color = colors.title)
        }
    }
}
```

`headline` 必填，可用尾随 lambda 提供，如上例。图标、摘要与尾部控件通过其他内容槽组合。

`onClick` 默认是 `null`。没有回调时，`Item` 不创建点击行为、按钮语义或 COUI 反馈；它仍提供行布局。普通跳转把回调传给 `Item.onClick`，不要再嵌套另一层行布局。`enabled` 默认是 `true`，`dark` 默认读取 `CouiTheme.isDark`。`enabled` 只控制 `Item` 自己的点击和反馈，不会自动禁用 `trailing` 中的开关、复选框或其他独立控件；这些控件的状态和 `enabled` 由宿主管理。

选择行或开关行需要由外层 `toggleable` / `selectable` 持有语义和业务状态。此时不传 `onClick`，并由宿主在外层配置 `couiButtonFeedback`。将同一个 `MutableInteractionSource` 传给反馈 Modifier 和唯一的交互 Modifier；`interactionSource` 必填，`dark` 默认读取 `CouiTheme.isDark`，`enabled` 默认是 `true`。不要叠加第二个 clickable。

`CouiText.kind` 仍为必填项；省略 `color` 时读取 `LocalCouiContentColor.current`，显式颜色优先。`Group.colors` 只控制该组卡片、标题和说明，不会改变任意内容槽的主题。

`CouiText` 的 PreferenceTitle/PreferenceSummary 默认按逻辑起始边对齐，包括多行与 RTL；显式 `textAlign` 优先。`rememberCouiTextStyle` 提供相同语义角色的样式。需要根据实际行数调整布局时使用 `CouiText.onLineCount`。弹窗排版继续由 [Dialog](dialog.md) 持有。

## 主题与显式配色

默认值在每个组件的消费处读取现有 `CouiTheme`，遵循原生预设或已选择的可选 M3 策略。未初始化适配器时仍可使用原生默认值。可以通过既有局部 `CouiTheme` 调整一段 COUI 内容，或只给单个组件传入参数；不需要页面级 Provider。

| 组件 | 覆盖入口 | 作用范围 |
| --- | --- | --- |
| `Group` | `colors: CouiPageColors` | 仅本组卡片、标题和说明；不更改任意内容槽的主题 |
| `Item` | `dark` | 本行 COUI 点击反馈的深浅色分支 |
| `Divider` | `color` | 当前分隔线 |
| `Footer` | `color` | 当前说明文字 |

`CouiPageDefaults.themeColors(dark)` 读取当前原生或 M3 策略；`CouiPageDefaults.colors(accent, dark)` 从对应原生浅色或深色预设生成颜色，再使用传入的强调色。两者都返回普通不可变配色值，`copy` 可覆盖单个角色；它们不创建主题作用域。需要同组 Divider 或自定义文字使用指定颜色时，显式传入对应颜色。完整颜色、字体与可选适配规则见[外观与预设](../guide/appearance.md)。

## 几何与控件组合

`Group` 根据是否有标题选择默认顶部留白；`modifier.padding` 在默认留白之外累加，不会抵消内部值。完全自定组间距时可直接组合 Column、标题、卡片与内容。`Item` 集成 leading/headline/supporting/trailing 的行内布局；`contentPadding` 与 `leadingSize` 控制行内留白和图标槽，外部高度使用标准 `Modifier.height` / `heightIn` 及父级约束。

开关、复选、滑条和菜单可选用已有 [CouiSwitch](switch.md)、[CouiCheckbox](checkbox.md)、[CouiSlider](slider.md)、[CouiMenu](menu.md)，也可混用其他设计的控件。尾部控件独立操作时不要再包整行点击；选择整行交互时使用相应 toggleable/selectable 语义和唯一业务 owner。开关拖动仍经 onDragCheckedChange 提交到同一受控值。程序赋值不额外触发触觉。

`Divider` 将 Defaults 中的厚度按当前密度换算并取整，默认至少一个物理像素，分隔线本体的占位与填色高度一致。宿主的 Modifier.height / heightIn 与父级约束仍决定实际高度。分隔线只用于同组内容，带图标时调整 startIndent 对齐标题；不要在独立卡片之间统一加线。需要图示时，由宿主使用普通布局和 Modifier 组合，不依赖独立图示组件。

## 反馈、滚动与背景采样

`Item` 设置 `onClick` 后复用 `couiButtonFeedback` 的 list 分支：短 UP/CANCEL 达到最低高亮后退场，父滚动接管、离界、禁用和移除分别释放会话。窗口失焦保留弹簧退场，禁用/销毁才停止局部动画。自定义行可直接使用该 Effect；`pressFeedback = false` 仅关闭按压，hover/focus 继续按原契约处理。不要额外叠加 ripple 或另一套缩放。

这些普通布局组件不创建 Backdrop、不替宿主安装滚动、不消费系统栏安全区。宿主选择已有 Scaffold、Column 或 LazyColumn 并处理 padding。需要 COUI 惯性与回弹时显式使用 [couiVerticalScroll](../effects/overscroll.md)；短页先占满视口，且不要叠加两层滚动。

只有使用模糊材质时才装配[背景采样](../effects/backdrop.md)。顶栏、底栏、FAB 采样宿主明确录制的正文源；独立 Popup/Dialog 可使用已有 ProvideCouiMenuBackdrop 和 rememberCouiWindowBackdrop。两种源不互相替代，消费者不能采样包含自身的源。组合步骤见[宿主页面接入](../guide/secondary-pages.md)。

## 迁移与边界

这些布局入口位于 `com.itos.coui.layout`，不会为业务类型提供开关行、菜单行或其他行包装。COUI 不提供页面级 Scaffold 或设置页包装；宿主继续使用自己的页面容器和导航。Demo 的行包装是 Demo 内部实现，不是库 API。

完整路由、搜索结果与业务行由宿主实现。实际组合见[二级页宿主](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)、[设置列表示例](../../demo/src/main/java/com/itos/coui/demo/DemoListSamples.kt)和 [Demo 指南](../guide/demo.md)。布局和反馈依据目标 Settings 17 的实际消费者，保留项目批准的平滑圆角与公共效果。跨设备字体、IME、RTL、TalkBack 和动画中段仍需实际验收。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)。
