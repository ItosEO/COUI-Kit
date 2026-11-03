# 分组与行布局

`CouiGroup`、`CouiItem`、`CouiPreferenceLayout` 和图示等组件可以独立放入 Material、Miuix 或宿主自己的页面。不需要库内 Scaffold、专用页面主题或导航容器。布局组件位于 `com.itos.coui.layout`；库不另设开关行、菜单行、滑条行等业务包装。

| 库内入口 | 职责 |
| --- | --- |
| CouiGroup | 平滑圆角卡片、可选标题、说明和 headerActions |
| CouiPreferenceLayout | leading、headline、supporting、trailing、行内留白与图标槽 |
| CouiItem | 唯一的整行点击与 COUI 列表反馈；内容自行排版 |
| CouiDivider / CouiFooter | 组内分隔与辅助说明 |
| CouiIllustration | 独立图示区域，内容由宿主提供 |

## 直接放入已有布局

以下函数只使用 Foundation 与 COUI；可以放入任意已有 Column、LazyColumn 的 item 或其他页面容器。文案和点击来自宿主，实际应用从资源读取文案。

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.layout.CouiGroup
import com.itos.coui.layout.CouiItem
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.CouiPreferenceLayout
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
    CouiGroup(modifier = modifier, title = groupTitle, colors = colors) {
        CouiItem(onClick = onClick) {
            CouiPreferenceLayout(
                supporting = {
                    CouiText(text = summary, kind = CouiTextKind.PreferenceSummary,
                        color = colors.summary)
                },
            ) {
                CouiText(text = title, kind = CouiTextKind.PreferenceTitle,
                    color = colors.title)
            }
        }
    }
}
```

`CouiPreferenceLayout` 不创建点击。纯信息行直接使用它；整行操作由 `CouiItem` 或宿主唯一的交互 Modifier 拥有，不能叠加两个 clickable。标题、摘要和右值可使用 `CouiTextKind.PreferenceTitle`、`PreferenceSummary`、`PreferenceValue`；也可直接放入宿主的 Text、图标和控件。内容槽不强制切换宿主主题。

`CouiText` 的 PreferenceTitle/PreferenceSummary 默认按逻辑起始边对齐，包括多行与 RTL；显式 textAlign 优先。`rememberCouiTextStyle` 提供相同语义角色的样式。需要根据实际行数调整布局时使用 `CouiText.onLineCount`，不重新实现文字测量。弹窗排版继续由 [Dialog](dialog.md) 持有。

## 主题与显式配色

默认值在每个组件的消费处读取现有 `CouiTheme`，遵循原生预设或已选择的可选 M3 策略。未初始化适配器时仍可使用原生默认值。可以通过既有局部 `CouiTheme` 调整一段 COUI 内容，或只给单个组件传入参数；不需要页面级 Provider。

| 组件 | 覆盖入口 | 作用范围 |
| --- | --- | --- |
| CouiGroup | `colors: CouiPageColors` | 仅本组卡片、标题和 footer；不更改任意内容槽的主题 |
| CouiItem | `dark` | 本行 COUI 交互反馈的深浅色分支 |
| CouiDivider | `color` | 当前分隔线 |
| CouiFooter | `color` | 当前说明文字 |
| CouiPreferenceLayout | 内容槽及 `contentPadding` / `leadingSize` | 仅行内排版，不持有主题或点击 |

`CouiPageDefaults.themeColors(dark)` 返回普通不可变配色值，`copy` 可覆盖单个角色；`colors(accent, dark)` 保留显式原生预设。配色对象不是主题作用域。给 Group 传入颜色后，需要同组 Divider 或自定义文字使用相同颜色时，显式传 `color = colors.divider` 或对应文字色。不要期待 Group 自动改变 Material Switch、Text 或其他库组件。颜色、字体与可选适配的完整规则见[外观与预设](../guide/appearance.md)。

## 几何与控件组合

Group 根据是否有标题选用 Defaults 中的默认顶部留白；`modifier.padding` 增加默认留白以外的间距，不会抵消内部值。完全自定组间距时可直接组合 Column、标题、卡片和内容。PreferenceLayout 的外部高度使用标准 `Modifier.height` / `heightIn`，内部 `defaultMinSize` 服从宿主约束。`contentPadding` 与 `leadingSize` 仍负责行内留白和图标槽。

开关、复选、滑条和菜单可选用已有 [CouiSwitch](switch.md)、[CouiCheckbox](checkbox.md)、[CouiSlider](slider.md)、[CouiMenu](menu.md)，也可混用其他设计的控件。尾部控件独立操作时不要再包整行点击；选择整行交互时使用相应 toggleable/selectable 语义和唯一业务 owner，尾部控件不重复提交。开关拖动仍经 onDragCheckedChange 提交到同一受控值。程序赋值不额外触发触觉。

CouiDivider 将 Defaults 中的厚度按当前密度换算并取整，默认至少一个物理像素，分隔线本体的占位与填色高度一致。宿主的 Modifier.height / heightIn 与父级约束仍决定实际高度。分隔线只用于同组内容，带图标时调整 startIndent 对齐标题；不要在独立卡片之间统一加线。图示使用 CouiIllustration，不塞入普通点击行。

## 反馈、滚动与背景采样

CouiItem 复用 `couiButtonFeedback` 的 list 分支：短 UP/CANCEL 达到最低高亮后退场，父滚动接管、离界、禁用和移除分别释放会话。窗口失焦保留弹簧退场，禁用/销毁才停止局部动画。自定义行可直接使用该 Effect；pressFeedback=false 仅关闭按压，hover/focus 继续按原契约处理。不要额外叠加 ripple 或另一套缩放。

这些普通布局组件不创建 Backdrop、不替宿主安装滚动、不消费系统栏安全区。宿主选择已有 Scaffold、Column 或 LazyColumn 并处理 padding。需要 COUI 惯性与回弹时显式使用 [couiVerticalScroll](../effects/overscroll.md)；短页先占满视口，且不要叠加两层滚动。

只有使用模糊材质时才装配[背景采样](../effects/backdrop.md)。顶栏、底栏、FAB 采样宿主明确录制的正文源；独立 Popup/Dialog 可使用已有 ProvideCouiMenuBackdrop 和 rememberCouiWindowBackdrop。两种源不互相替代，消费者不能采样包含自身的源。组合步骤见[宿主页面接入](../guide/secondary-pages.md)。

## 迁移与边界

PR 开发阶段的 CouiScaffold 已移除，不保留重命名包装或兼容别名。保留自己的页面容器，将配色覆盖转为上述组件参数或既有 CouiTheme；需要背景材质时显式装配现有 Backdrop 接口。此前临时的 CouiSettingsPage、各类 Preference 业务包装也不是库 API。

完整路由、搜索结果与业务行由宿主实现；当前 PR 不提供完整二级页 Demo，不依赖尚未提供的 Demo 文件。布局和反馈依据目标 Settings 17 的实际消费者，保留项目批准的平滑圆角与公共效果。跨设备字体、IME、RTL、TalkBack 和动画中段仍需实际验收。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)、[图示](../../coui/src/main/java/com/itos/coui/layout/CouiIllustration.kt)。
