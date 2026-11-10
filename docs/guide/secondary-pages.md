# 在宿主页面中组合 COUI

COUI 组件可以放入现有 Material、Miuix 或自有页面。纯 COUI 应用也可选择 [Scaffold](../components/scaffold.md) 处理栏位测量和安全区。

| 页面需要 | 装配方式 |
| --- | --- |
| 普通分组与控件 | 直接放入已有布局，见[Group](../components/group.md) |
| COUI 惯性与回弹 | 在实际滚动容器使用 [couiVerticalScroll](../effects/overscroll.md) |
| 模糊顶栏、底栏或 FAB | COUI Scaffold 自动提供正文源；第三方容器显式接入 Backdrop |
| 材质菜单或弹窗 | 按需提供窗口背景源 |

配色来自组件参数或当前 `CouiTheme`。M3 适配是可选依赖，接入方法见[外观与预设](appearance.md)。

## 使用已有 Scaffold

下面的应用层示例使用 Material 3 Scaffold。宿主需自行依赖 Material 3 并提供 MaterialTheme；文案和导航回调由宿主传入。

```kotlin
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
                    backgroundColor = background, titleColor = titleColor),
                title = { BasicText(title, style = LocalCouiTextStyle.current) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)
                .consumeWindowInsets(padding),
            contentPadding = padding,
        ) {
            item {
                Group(colors = colors) {
                    Item(onClick = onOpen) {
                        Text(text = rowTitle, kind = CouiTextKind.ItemTitle,
                            color = colors.title)
                    }
                }
            }
        }
    }
}
```

仅使用 Group、Item 或普通控件时，不需要示例中的 Backdrop。使用其他页面容器时，保留相同的源与消费者关系。

## Padding、Insets 与滚动

正文应用并消费宿主给出的 Padding。LazyColumn 使用 `contentPadding`；Column 使用 `verticalScroll` 或 `couiVerticalScroll` 时，将 Padding 放在滚动 Modifier 后，使内容能滚入浮动栏位下方。不要叠加两层纵向滚动。

顶栏负责顶部及自身横向安全区，底栏负责底部安全区；宿主容器返回的占位包含这些实测高度。已消费的部分不再添加第二次。Material 3 的分工见 [Android 官方说明](https://developer.android.com/develop/ui/compose/system/material-insets)，Scaffold 的分工见[页面容器](../components/scaffold.md#padding-与系统-insets)。

含输入框的页面在滚动容器外使用 `imePadding()`，使键盘打开时实际视口缩小，Foundation 才能处理焦点避让。只增加滚动内容尾部空白不会缩小视口。父级已处理 IME 时沿用其分工，详见 [Insets 消费规则](https://developer.android.com/develop/ui/compose/system/insets-ui)。

页面还会打开输入弹窗时，按[宿主窗口焦点启用键盘避让](../components/dialog.md#宿主页的键盘避让)，避免背景页面提前空出键盘区域。

大标题页面使用 `LargeTopAppBar` 与 `CouiTopAppBarDefaults.exitUntilCollapsedScrollBehavior`，正文通过 `scrollBehavior.state.contentPadding` 补偿顶部，并用宿主原始 Padding 消费 Insets。常驻搜索页面使用 `CollapsingTopAppBar` 的 `state.contentPadding`。完整装配见[折叠顶栏](../components/collapsing-app-bar.md)。该状态接收普通 PaddingValues，不依赖特定 Scaffold。

## 正文源与窗口源

COUI `Scaffold` 自动录制完整正文视口与 `containerColor`，仅向 `topBar`、`bottomBar` 和 `floatingActionButton` 提供默认源。放入这些槽位的 COUI 顶栏、底栏、FAB 和搜索可省略 `backdrop`；显式参数优先。正文 `content` 不获得自身源，内部效果需配置独立源。完整用法见[页面容器](../components/scaffold.md)。

第三方容器的正文源需覆盖完整视口和不透明底色，供顶栏、底栏等浮动组件采样。将源挂在应用滚动 Padding 之前的位置，避免栏位下方出现未录制区域。消费者不能被录入自身采样的源。

使用第三方容器时，在现有正文容器的 Modifier 上添加 `.couiBackdrop(backdrop)`，并将同一个源显式传给顶栏即可，不必额外包装容器或安装 Provider。图层由库管理，无需全局初始化，见[背景采样](../effects/backdrop.md#顶栏接入)。

Scaffold 不安装窗口源。菜单或材质弹窗需要包含顶栏的完整窗口背景时，可在宿主 Composable 中使用以下结构：

```kotlin
ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        // 原有页面；正文 Backdrop 保持独立。
        // 菜单与弹窗按各自接口使用窗口源，在独立浮层中绘制。
    }
}
```

`rememberCouiWindowBackdrop()` 读取最近提供的窗口源，未提供时返回 null。只在有相应消费者时安装窗口源；每个源挂到一个实际节点，每个绘制节点持有独立可变 renderer。资源和失败处理见[背景采样](../effects/backdrop.md)，具体消费见[菜单](../components/menu.md)与[弹窗](../components/dialog.md)。

## 状态与导航

查询、结果、返回栈、页面身份和受控设置值由宿主持有。单 Activity 应用可沿用 Compose 导航；多 Activity 应用可使用标准 Intent 与 `finish()`。普通、不透明 Activity 在未指定自定义动画时使用 ROM 提供的默认转场，不保证系统设置的特权动画或双栏协议。

搜索展开或有未保存变更时，由宿主安装局部返回处理器，状态关闭后释放拦截。切页时，把持续存在的底栏、renderer 和反馈节点留在稳定组合位置，只替换页面内容。

Demo 使用普通 Activity 导航。完整装配见 [MainActivity](../../demo/src/main/java/com/itos/coui/demo/MainActivity.kt)、[DemoSecondaryActivity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt)和 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)。这些路由和行包装属于 Demo，库使用者直接消费公开组件。
