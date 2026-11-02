# 设置页布局

库提供可重用的 Scaffold、分组、图示、行布局和输入反馈；完整二级页与具体设置行由宿主装配。库不再分别公开开关行、菜单行、滑条行、输入行等包装入口。

| 库内入口 | 职责 |
| --- | --- |
| CouiSettingsScaffold | 测量顶栏、底栏和 FAB，提供内容 padding、安全区及独立采样源 |
| CouiSettingsGroup | 平滑圆角卡片、可选标题、说明和 headerActions |
| CouiPreferenceLayout | leading、headline、supporting、trailing、内部留白与图标槽，默认行高服从宿主约束 |
| CouiSettingsItem | 自定义单一点击与列表反馈；内容自行排版 |
| CouiSettingsDivider / CouiSettingsFooter | 组内分割与独立说明 |
| CouiSettingsIllustration | 图示和教程区域，内容由宿主提供 |

以下片段位于宿主 Composable 中；文字、样式、图标和颜色是宿主参数。行布局不创建点击，交互由唯一的 Modifier 或尾部控件拥有。

```kotlin
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import com.itos.coui.settings.CouiPreferenceLayout

CouiPreferenceLayout(
    leading = { Icon(icon, null, tint = iconColor) },
    supporting = { BasicText(summary, style = summaryStyle) },
    trailing = { BasicText(value, style = valueStyle) },
) { BasicText(title, style = titleStyle) }
```

标题、摘要和右值样式分别使用 CouiTextKind.PreferenceTitle、PreferenceSummary、PreferenceValue。CouiText 的 PreferenceTitle/PreferenceSummary 默认按逻辑起始边对齐，包括多行与 RTL；显式 textAlign 优先。图标与文字沿同一垂直中线；同组分割由宿主选择插入，带图标时让 startIndent 对齐标题，不在独立卡片之间统一加线。

CouiText 与 rememberCouiTextStyle 提供设置页、标题、搜索等角色的原生排版；需要根据实际行数调整宿主布局时，使用 CouiText 的 onLineCount 回调。弹窗的标题、正文、按钮、输入和计数排版由 [Dialog](dialog.md) 内部持有，继续在消费处解析主题字体。

CouiSettingsGroup 根据是否有标题选用 CouiSettingsDefaults 的默认顶部留白；`modifier.padding` 增加默认留白以外的间距，不会替换或抵消默认值。需要完全自行控制组间距时，由宿主组合 Column、标题、卡片和内容。CouiPreferenceLayout 的外部高度使用标准 `Modifier.height` / `heightIn`；只有调用方没有给出最小高度约束时，`defaultMinSize` 才补充默认行高，内容仍按真实尺寸测量。`contentPadding` 与 `leadingSize` 分别调整行内留白和图标槽，仍通过组件参数设置。

开关直接使用 [CouiSwitch](switch.md)，复选直接使用 [CouiCheckbox](checkbox.md)，滑条直接使用 [CouiSlider](slider.md)，菜单直接使用 [CouiMenu](menu.md)。整行交互时，尾部控件不再独立提交点击；开关拖动继续通过 onDragCheckedChange 提交给同一个业务 owner。整行触觉可使用公开的 CouiHapticFeedback / performCouiHapticFeedback，程序赋值不触发触觉。

以下完整 Demo 装配由后续批次提供，当前 PR 尚不包含这些示例文件；本批可按本文及[折叠顶栏](collapsing-app-bar.md)的片段接入：

| 装配 | 示例源码 |
| --- | --- |
| 信息、跳转、开关、复选、单选行 | DemoPreferenceRows（完整 Demo 在后续重构批次提供） |
| Popup、滑条、输入、步进 | DemoPreferenceControls（完整 Demo 在后续重构批次提供） |
| 进度操作行 | DemoProgressPreference（完整 Demo 在后续重构批次提供） |
| 紧凑顶栏、返回、滚动与安全区 | DemoSettingsPage（完整 Demo 在后续重构批次提供） |
| 右上角三点菜单 | DemoMenuPresets（完整 Demo 在后续重构批次提供） |

后续示例仅供宿主参考和按业务调整，不应从 Demo 导入私有函数，也不依赖 Demo 的主题作用域。选择范围、加载任务、导航和确认提交均属于宿主。库内公共固定参数继续由各组件 Defaults 持有。

列表反馈使用 couiButtonFeedback 的 list 分支：短 UP/CANCEL 达到最低高亮后退场，父滚动接管、离界、禁用和移除释放会话。Popup 展开时通过 hoverLocked 保留行悬停，pressed 独立退场。窗口失焦只释放会话并继续弹簧退场，Activity 转场期间不清零；禁用或节点销毁才立即释放动画。显式设置 pressFeedback=false 会释放现有按压动画，并移除按压观察器；hover/focus 继续生效。导航宿主应保留来源行到转场完成，不能先拆掉搜索结果。不要额外叠加 Compose ripple 或普通按钮缩放。

Scaffold 的 content 必须消费它返回的 PaddingValues；将同一对象直接传入 `Modifier.padding` 或滚动容器的 `contentPadding`，使顶栏、底栏和 inset 的高度变化在测量时生效。若在组合阶段调用 calculateTopPadding 等方法拆出值，读值所在的组合仍会随其更新。内容、顶栏、底栏、Popup 与 Dialog 分层采样。短页和空白区要占满视口，再装配 [couiVerticalScroll](../effects/overscroll.md)，不能因没有滚动范围而移除回弹。完整接入与 ROM 默认 Activity 转场见[二级页接入](../guide/secondary-pages.md)。

topBar、bottomBar 和 floatingActionButton 收到正文源，不包含它们自身；rememberCouiWindowBackdrop 返回的窗口源录制完整页面，包含已经绘制的浮层，供独立 Popup/Dialog 采样。两者不能互换，具体规则见[背景采样](../effects/backdrop.md)。

布局依据 Settings 17 的实际 AppBar、Preference 与 cardlist 消费者，使用项目平滑圆角及公共效果。OEM 特权窗口协议、专用字体及设备动画中段仍需验收。

迁移：此前临时公开的 CouiSettingsPage、各类 Preference 业务包装不再属于库 API。宿主改为组合 Scaffold、PreferenceLayout 和实际控件；没有保留重复公共别名，也不依赖尚未提供的 Demo 文件。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/settings/CouiSettings.kt)、[Scaffold](../../coui/src/main/java/com/itos/coui/settings/CouiSettingsScaffold.kt)。

## 主题与显式配色

默认值在消费处读取 `CouiTheme`，跟随原生或可选 M3 策略；显式颜色优先。设置容器使用 `CouiSettingsDefaults.themeColors`，可通过返回值的 `copy` 覆盖各颜色角色。常驻搜索的颜色角色及 `fallbackBackground` 生效范围见[搜索栏配色契约](search-bar.md#主题与显式配色)。
