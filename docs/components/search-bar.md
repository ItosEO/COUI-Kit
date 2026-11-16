# 常驻搜索

`SearchBar` 提供常驻搜索胶囊，展开后缩窄输入区并在右侧分裂出独立取消按钮，适用于设置页及首页搜索。由顶栏按钮展开的搜索请使用[顶栏搜索](search.md)。

- **最低系统版本与渲染分级**：支持库最低版本（**API 31+**），按系统能力分级呈现（API 34+ 启用浮动模糊与双主体融合材质，若创建 `renderer` 失败会抛出异常）。
- **前置依赖**：需要不包含搜索自身的 `backdrop`（放入 `TopAppBarSurface` 或 COUI `Scaffold` 独立消费者槽位时可省略）；示例中的 `BackHandler` 来自 `androidx.activity:activity-compose`。

## 基本用法

宿主持有 `query`、`expanded` 及对应回调，通过 `rememberCouiSearchBarState(expanded)` 驱动展开与收起动画：

~~~kotlin
@Composable
fun PersistentSearchBarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    query: String,
    expanded: Boolean,
    searchLabel: String,
    cancelLabel: String,
    clearLabel: String,
    onQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: () -> Unit,
) {
    val search = rememberCouiSearchBarState(expanded)
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    SearchBar(
        modifier = modifier,
        state = search,
        query = query,
        onQueryChange = onQueryChange,
        onExpandedChange = onExpandedChange,
        placeholder = searchLabel,
        cancelText = cancelLabel,
        clearDescription = clearLabel,
        backdrop = backdrop,
        onSearch = onSearch,
    )
}
~~~

接入要点：

1. **保持 `state` 组合位置稳定**：切勿以 `expanded` 作为 `key` 重建 `rememberCouiSearchBarState(expanded)`。需要配置重建后恢复时，由宿主用 `rememberSaveable` 保存 `query` 和 `expanded`，动画中间值无需持久化。
2. **移动中可点击取消**：取消按钮显露后即可立即点击，无需等待弹簧完全停稳；点击后弹簧从当前位置与速度平滑反向收起。

## 状态与事件

| 用户操作 | 组件行为 | 宿主职责 |
| --- | --- | --- |
| 点击收起态搜索胶囊 | 触发 `onExpandedChange(true)` | 更新 `expanded = true` 并回传给 `state` |
| 编辑输入文字 | 触发 `onQueryChange(newQuery)` | 保存最新 `query` 并筛选结果 |
| 点击清除图标 | 触发 `onQueryChange("")`，保持展开 | 清空查询内容 |
| 点击取消按钮 | 释放搜索自身焦点与软键盘，再触发 `onExpandedChange(false)` | 决定是否收起搜索及清空查询 |
| 按下键盘搜索键 | 隐藏软键盘后触发 `onSearch()` | 执行搜索提交或其他业务 |
| 按下系统返回键 | 由宿主的 `BackHandler` 拦截处理 | 展开时关闭搜索，关闭后释放拦截 |

焦点与禁用契约：

- **焦点边界**：展开时成功获取焦点后自动弹出键盘；初始收起或已失焦时不会误清页面内其他输入框的焦点。若宿主在 `onExpandedChange(false)` 中拒绝关闭，搜索保持展开且不会因失焦立即抢回输入，用户可再次点击编辑。
- **禁用状态（`enabled = false`）**：停止展开、输入、清除、取消与交互反馈，并释放搜索自身焦点与键盘；`query` 与 `expanded` 目标值保持不变，重新启用且仍处于展开状态时会重新请求焦点。结果列表的交互禁用需由宿主自行用 `enabled && expanded` 控制。

## 与页面内容联动

同一个 `CouiSearchBarState` 可同时传给 [CollapsingTopAppBar](collapsing-app-bar.md) 与 `SearchBar`，读取以下状态属性驱动页面过渡：

| 状态属性 | 类型 / 范围 | 联动用途 |
| --- | --- | --- |
| `expanded` | `Boolean` | 当前是否处于展开目标态；用来控制上层结果层是否可点击与可聚焦 |
| `active` | `Boolean` | 包含展开态与正在进行的退场动画；用于在退场期间保持结果层挂载，同时禁用底层正文交互 |
| `contentAlpha` | `0f..1f` | 驱动被覆盖原正文的透明度（进入立方淡出、退出线性淡入） |
| `resultsAlpha` | `0f..1f` | 驱动独立结果层的透明度 |

- **独立结果层模式**：保留底层正文以维持原有滚动位置，在 `search.active` 期间禁用底层滚动、点击与焦点，并附加 `Modifier.clearAndSetSemantics {}`；上层结果层以 `search.active` 保留退场绘制、以 `search.expanded` 控制可交互性，完整代码见[折叠顶栏](collapsing-app-bar.md)。
- **直接筛选原列表**：若直接过滤原列表而无需独立结果遮罩，原列表可保持可见与可交互，无需绑定 `contentAlpha` 与 `resultsAlpha`，参见[首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
- **动画生命周期**：动画通道随创建 `state` 的组合释放；若需要临时移除 `SearchBar` 本身，只要将 `state` 保留在外层组合，顶栏与结果层仍可继续读取动画进度。

## 参数与平台分层

### 平台渲染分层

| 系统版本 | 渲染能力 |
| --- | --- |
| **API 31–32** | 普通填色背景（`colors.fallbackBackground`）、文本输入与宽度/取消按钮展开动画 |
| **API 33** | 普通填色背景及支持的跟手聚光效 |
| **API 34+** | 背景模糊材质、搜索与取消胶囊双主体融合、轮廓光及投影（内部创建双主体 `renderer`） |

### 常用外观参数

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `shadowProgress` | `0f` | 可直接传入折叠顶栏的 `collapsedFraction`；默认不带滚动投影，展开后使用搜索自身的投影动画 |
| `colors` | `CouiSearchBarDefaults.colors(dark)` | 通过 `copy(...)` 覆盖 `foreground`（输入/取消文字）、`secondary`（占位文字与搜索图标）、`clear`（清除图标圆底）及 `fallbackBackground`（仅 API 31–33 生效） |
| `dark` | `CouiTheme.isDark` | 控制默认配色、高光与深浅材质分支；API 34+ 材质读取当前主题 `materialTint`（M3 取 `surface` 染色，原生默认为透明，可在局部 `CouiTheme` 调整或关闭） |
| `backdrop` | `LocalCouiToolbarBackdrop.current ?: LocalCouiBackdrop.current` | 背景采样源，不可包含 `SearchBar` 自身 |

## 装配注意事项

- **背景源位置**：放在 `TopAppBarSurface`（例如 `CollapsingTopAppBar`）内部时，优先读取顶栏渐进处理后的背景；独立使用时读取显式 `backdrop` 或当前默认源。**注意**：COUI `Scaffold` 只向 `topBar`、`bottomBar` 与 `floatingActionButton` 提供默认源，若把 `SearchBar` 放在正文 `content` 内部，必须手动为其提供不包含自身的正文背景源，详见[背景采样](../effects/backdrop.md)。
- **独立实例资源**：每个 `SearchBar` 实例内部各自持有双主体 `renderer` 与内容图层；在 API 34+ 若设备图形后端创建 `renderer` 失败会抛出异常。

## 源码与完整示例

- **组件实现**：[SearchBar.kt](../../coui/src/main/java/com/itos/coui/search/SearchBar.kt)
- **状态与预设**：[CouiSearchBarState.kt](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarState.kt)、[CouiSearchBarDefaults.kt](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarDefaults.kt)
- **首页完整示例**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
