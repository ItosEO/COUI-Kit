# 常驻搜索

`SearchBar` 显示常驻搜索胶囊，展开后缩窄输入区并显示取消按钮。适用于设置页和列表搜索；从顶栏按钮展开的搜索见[顶栏搜索](search.md)。

## 基本用法

以下片段位于宿主 Composable 中。宿主持有 `query`、`expanded` 及更新回调，文案来自应用资源。示例显式传入不包含搜索自身的页面内容源。放入 COUI Scaffold 的独立消费者槽位时可省略 `backdrop`，显式参数优先；正文内部的搜索仍需独立源。装配方法见[背景采样](../effects/backdrop.md)。

```kotlin
val search = rememberCouiSearchBarState(expanded)
BackHandler(enabled = expanded) { updateExpanded(false) }
SearchBar(
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

设置 `enabled = false` 可禁用展开、输入、清除、取消和交互反馈，并释放搜索自身的焦点和键盘。查询与展开目标保持不变；重新启用且仍展开时会请求焦点。

结果列表需要单独处理禁用状态，可用 `enabled && expanded` 控制交互；返回处理由页面决定。

## 与页面内容联动

同一个 state 可同时传给 [CollapsingTopAppBar](collapsing-app-bar.md) 和搜索组件。`contentAlpha` 控制原页面透明度，`resultsAlpha` 控制结果透明度；`active` 包含展开及退场过程。

使用独立结果层时，保留被覆盖的正文以保存滚动位置，同时禁用其滚动、点击和焦点，并将其移出无障碍遍历。上层结果以 `search.expanded` 控制输入，以 `active` 保留退场绘制。完整装配见[折叠顶栏](collapsing-app-bar.md)。如果直接筛选原列表，列表可以保持可见和可操作，不需要使用页面及结果透明度；参见[首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。

动画随 state 的组合释放。只移除搜索框时，可以保留 state，让标题和结果继续读取动画进度。

## 外观与平台

`shadowProgress` 可接收折叠顶栏的 `collapsedFraction`。默认值不添加滚动投影；展开时使用搜索自身的投影动画。

`CouiSearchBarDefaults.colors(dark)` 读取当前主题。`foreground`、`secondary`、`clear` 分别控制文字、图标等次要内容和清除操作；通过 `copy` 覆盖颜色。`fallbackBackground` 仅作用于 API 31–33 的普通背景。

| 平台 | 绘制 |
| --- | --- |
| API 31–32 | 普通填色、输入和宽度/取消动画 |
| API 33 | 普通填色及支持的跟手光 |
| API 34+ | 背景模糊材质、双主体融合、轮廓和投影 |

API 34+ 在组件内部创建模糊材质；创建失败时抛出异常。

在顶栏效果作用域（例如 `TopAppBarSurface`）中，搜索优先读取顶栏处理后的渐进背景；独立使用时读取显式 `backdrop` 或当前默认源。COUI Scaffold 只在顶栏、底栏和 FAB 槽位提供默认源，不向正文提供自身源。源不能包含搜索自身。双主体绘制复用[浮动表面](../effects/floating-surfaces.md)，每个搜索实例分别持有 renderer 和内容层。

[组件](../../coui/src/main/java/com/itos/coui/search/SearchBar.kt) · [状态](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarState.kt) · [Defaults](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarDefaults.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
