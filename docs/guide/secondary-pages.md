# 在宿主页面中组合 COUI

COUI 组件可以直接放入现有的 Material 3、Miuix 或自有页面布局；纯 COUI 应用也可选择 [Scaffold](../components/scaffold.md) 统一处理栏位测量与安全区。

| 页面需要 | 装配方式 |
| --- | --- |
| 普通分组与控件 | 直接放入已有布局，见 [Group](../components/group.md) |
| COUI 惯性与边界回弹 | 在实际滚动容器上使用 [couiVerticalScroll](../effects/overscroll.md) |
| 模糊顶栏、底栏或 FAB | COUI `Scaffold` 自动提供正文源；第三方容器需显式接入 `Backdrop` |
| 材质菜单或弹窗 | 按需在宿主根部提供窗口背景源 |

配色来自组件显式参数或当前 `CouiTheme`；Material 3 适配为可选模块，配置见[外观与预设](appearance.md)。

## 使用已有 Scaffold

下面展示在 Material 3 `Scaffold` 中组合 COUI `TopAppBar`、`Group` 与 `Item`（宿主需自行依赖 Material 3 并提供 `MaterialTheme`，文案与导航回调由宿主传入）：

~~~kotlin
@Composable
fun MixedPage(
    modifier: Modifier = Modifier,
    title: String,
    rowTitle: String,
    onOpen: () -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    val background = MaterialTheme.colorScheme.background
    val titleColor = MaterialTheme.colorScheme.onBackground
    val colors = CouiPageDefaults.themeColors()
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        containerColor = background,
        topBar = {
            TopAppBar(
                backdrop = backdrop,
                colors = CouiTopAppBarDefaults.colors(
                    backgroundColor = background,
                    titleColor = titleColor,
                ),
                title = { BasicText(title, style = LocalCouiTextStyle.current) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .couiBackdrop(backdrop)
                .background(background)
                .consumeWindowInsets(padding),
            contentPadding = padding,
        ) {
            item {
                Group(colors = colors) {
                    Item(onClick = onOpen) {
                        Text(
                            text = rowTitle,
                            kind = CouiTextKind.ItemTitle,
                            color = colors.title,
                        )
                    }
                }
            }
        }
    }
}
~~~

仅使用 `Group`、`Item` 或普通控件时，不需要示例中的 `Backdrop`。使用 Miuix 或自有容器时，保持相同的“正文源 → 消费者”关系即可。

## Padding、Insets 与滚动

### 1. 安全区与栏位职责分工

| 角色 | 负责的 Insets 与留白 | 消费者注意点 |
| --- | --- | --- |
| COUI 顶栏 | 顶部状态栏及自身横向安全区 | 勿对顶栏外层重复加顶部/横向安全区 |
| COUI 底栏 | 底部导航栏安全区 | 勿对底栏外层重复加底部安全区 |
| 宿主 `Scaffold` | 计算栏位占位与剩余安全区，返回 `padding` | 正文必须应用并 `consumeWindowInsets(padding)`，已消费部分勿重复叠加 |

Material 3 的分工详见 [Android 官方 Insets 说明](https://developer.android.com/develop/ui/compose/system/material-insets)，COUI `Scaffold` 的分工见[页面容器](../components/scaffold.md#padding-与系统-insets)。

### 2. 滚动容器与折叠顶栏接线

- **`LazyColumn`**：传入 `contentPadding = padding`，并在滚动节点外层调用 `.consumeWindowInsets(padding)`。
- **`Column` + `verticalScroll` / `couiVerticalScroll`**：将 `.padding(padding)` 放在滚动 Modifier **之后**，让内容可以滚入浮动栏位下方；不要叠加两层纵向滚动。
- **大标题折叠顶栏**：使用 `LargeTopAppBar` + `CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior`，正文通过 `scrollBehavior.state.contentPadding` 补偿顶部，并继续用宿主原始 `padding` 消费 Insets。
- **常驻搜索折叠顶栏**：使用 `CollapsingTopAppBar` 的 `state.contentPadding`，完整装配见[折叠顶栏](../components/collapsing-app-bar.md)（该状态接收普通 `PaddingValues`，不依赖特定 `Scaffold`）。

### 3. 键盘避让（`IME`）

- **页面内含输入框**：在滚动容器**外侧**添加 `imePadding()`，确保键盘弹出时实际视口缩小、Foundation 能自动将焦点滚入可见区（仅在列表尾部加空白无法缩小视口）；若父级已消费 IME 则沿用父级，详见 [Insets 消费规则](https://developer.android.com/develop/ui/compose/system/insets-ui)。
- **页面会弹出输入弹窗**：按[宿主窗口焦点启用键盘避让](../components/dialog.md#宿主页的键盘避让)配置，避免背景页面在弹窗打开时提前空出键盘区域。

## 正文源与窗口源

COUI 背景采样区分**正文源**（给栏位和局部浮动组件采样）与**窗口源**（给独立窗口浮层采样）：

| 背景源类型 | 谁来创建与挂载 | 谁来消费 | 核心约束 |
| --- | --- | --- | --- |
| 正文源（COUI `Scaffold`） | 容器自动创建并录制正文视口与 `containerColor` | `topBar`、`bottomBar`、`floatingActionButton` 槽位内的组件（可省略 `backdrop`，显式优先） | 正文 `content` 不获得自身源；正文内的模糊组件须另建独立源 |
| 正文源（第三方容器） | 宿主调用 `rememberCouiBackdrop()`，并在应用滚动 Padding **之前**挂 `.couiBackdrop(backdrop)` | 显式传参给 `TopAppBar`、`NavigationBar`、`Fab` 等消费者 | 必须覆盖完整视口与不透明底色，避免栏位下方出现未录制区域；消费者绝不能被录入自身采样的源 |
| 窗口源（`Menu` / `Dialog`） | `Scaffold` **不安装**窗口源；宿主在根部使用 `ProvideCouiMenuBackdrop` | 子树中的菜单或材质弹窗通过 `rememberCouiWindowBackdrop()` 读取（未提供时返回 `null`） | 仅在存在消费者时安装；每个源只挂到一个实际节点，每个绘制节点持有独立可变 `renderer` |

在第三方容器中挂载正文源时，无需额外包装容器或安装 Provider，直接把 `.couiBackdrop(backdrop)` 挂到现有正文节点即可（详见[背景采样](../effects/backdrop.md#顶栏接入)）。窗口源典型装配如下：

~~~kotlin
// 在宿主根部 @Composable 作用域中安装窗口背景源
ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        // 原有页面；正文 Backdrop 保持独立。
        // 菜单与弹窗按各自接口使用窗口源，在独立浮层中绘制。
    }
}
~~~

资源管理与失败处理详见[背景采样](../effects/backdrop.md)，具体消费见[菜单](../components/menu.md)与[弹窗](../components/dialog.md)。

## 状态与导航

- **状态归属**：搜索查询、结果、返回栈、页面身份和受控设置值均由宿主持有。
- **页面导航**：单 Activity 应用可沿用 Compose Navigation；多 Activity 应用使用标准 `Intent` 与 `finish()`。普通不透明 Activity 在未指定自定义转场时使用 ROM 默认动画，不保证系统设置的特权动画或双栏协议。
- **局部返回与节点稳定性**：搜索展开或存在未保存编辑草稿时，由宿主安装局部返回拦截器并在关闭后释放；切页时将持久存在的底栏、`renderer` 与反馈节点留在稳定的组合位置，仅替换页面内容区域。

Demo 完整导航与页面装配可参考 [MainActivity](../../demo/src/main/java/com/itos/coui/demo/MainActivity.kt)、[DemoSecondaryActivity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt) 与 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)（其中的路由和行包装属于 Demo，应用直接消费公开组件即可）。
