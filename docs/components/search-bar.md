# 常驻搜索

`CouiSearchBar` 显示常驻搜索胶囊，展开后缩窄输入区并显示独立取消按钮。它适用于 Settings 式搜索；从顶栏按钮展开的搜索见[顶栏搜索](search.md)。

## 基本用法

以下片段位于宿主 Composable 中。宿主持有 `query`、`expanded` 及更新回调，文案来自应用资源。`backdrop` 是不包含搜索自身的页面内容源，装配方法见[背景采样](../effects/backdrop.md)。

```kotlin
import androidx.activity.compose.BackHandler
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState

val search = rememberCouiSearchBarState(expanded)
BackHandler(enabled = expanded) { updateExpanded(false) }
CouiSearchBar(
    state = search,
    query = query,
    onQueryChange = updateQuery,
    onExpandedChange = updateExpanded,
    placeholder = searchLabel,
    cancelText = cancelLabel,
    clearDescription = clearLabel,
    backdrop = backdrop,
    onSearch = submitSearch,
)
```

示例中的 BackHandler 来自 `androidx.activity:activity-compose`，宿主需声明相应依赖；已有返回分发器也可承担同一职责。

保持 `state` 的组合位置稳定，不要用 `expanded` 作为重建它的 key。需要配置恢复时，宿主用 `rememberSaveable` 保存查询和展开目标，动画中间值无需保存。

## 状态与事件

| 操作 | 组件行为 | 宿主职责 |
| --- | --- | --- |
| 点击收起的搜索 | 请求 `onExpandedChange(true)` | 接受展开目标后回传给 state |
| 编辑输入 | 调用 `onQueryChange` | 更新查询、筛选结果 |
| 清空 | 请求空查询，保持展开 | 更新查询 |
| 取消 | 释放搜索自身的焦点与 IME，再请求关闭 | 决定是否关闭及清空查询 |
| IME 搜索 | 隐藏键盘后调用 `onSearch` | 执行查询或其他业务 |
| 系统返回 | 由宿主的返回处理器响应 | 展开时关闭搜索，关闭后释放拦截 |

展开时，组件在成功取得焦点后显示键盘。初始收起或已经失焦时，不清理其他输入框的焦点。宿主拒绝关闭请求时，查询与展开状态保持；搜索不会因失焦立即抢回输入，用户可再次点击编辑。

查询、搜索结果和导航由宿主持有。组件不提供麦克风功能或占位，也不在输入变化时启动导航。

## 与页面内容联动

同一个 state 可同时传给 [CouiCollapsingTopAppBar](collapsing-app-bar.md) 和搜索组件。`contentAlpha` 用于原页面内容，`resultsAlpha` 用于结果；两者使用各自的动画通道。`active` 包含展开及退场过程。

搜索活动期间，宿主应禁用原正文的滚动和交互，并将其移出无障碍遍历。透明度本身不会禁用点击、键盘或焦点。保留正文组合可以保留滚动与示例状态。结果放在上层，并以 `search.expanded` 控制输入：关闭请求被接受后，立即禁用结果的滚动、点击和焦点，退出无障碍遍历，同时按 `active` 保留退场绘制。完整装配见[折叠顶栏](collapsing-app-bar.md)。

动画随创建 state 的组合释放。只移除搜索节点而保留 state 时，动画仍可供标题和结果读取；输入、内容图层和触摸效果随搜索节点释放。固定搜索输入限制祖先重定位，字段内部的光标滚动仍由 Foundation 处理。

## 外观与平台

`shadowProgress` 可接收折叠顶栏的 `collapsedFraction`。默认值不添加滚动投影；展开时使用搜索自身的投影动画。

`CouiSearchBarDefaults.colors(dark)` 读取当前主题。`foreground`、`secondary`、`clear` 分别控制文字、图标等次要内容和清除操作；通过 `copy` 覆盖颜色。`fallbackBackground` 仅作用于 API 31–33 的普通背景。

| 平台 | 绘制 |
| --- | --- |
| API 31–32 | 普通填色、输入和宽度/取消动画 |
| API 33 | 普通填色及支持的跟手光 |
| API 34+ | 背景模糊材质、双主体融合、轮廓和投影 |

API 34+ 使用项目选定的 `TYPE_FRAMEWORK_TOP_BAR_BLUR` 材质，区别于原厂零半径的 `TYPE_FRAMEWORK_TOP_BAR` 分支。低平台填色不提供相同的融合效果。Shader 在组件内部创建；创建失败会使当前组合失败，不自动回退到普通填色。

在 `CouiTopAppBar` 中，搜索读取顶栏提供的渐进背景；独立使用时读取显式 `backdrop`。源不能包含搜索自身。双主体绘制复用[浮动表面](../effects/floating-surfaces.md)，每个搜索实例分别持有 renderer 和内容层。

## 实现依据

组件对应 Settings 17 的 instant-search 分支。宽度、取消、页面和结果分别使用动画通道；页面与结果的 OEM 运行中 `setStartValue` 语义及默认阈值仍缺同源实现材料，因此快速反向尚不能认定为原厂等价。原始材料哈希、平台适配和待验证行为见[输入与共享效果记录](../../.agents/notes/implemented/bug-fix/2026-11-02-secondary-page-review-fixes.md)。

[组件](../../coui/src/main/java/com/itos/coui/search/CouiSearchBar.kt) · [状态](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarState.kt) · [Defaults](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarDefaults.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
