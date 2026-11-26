# 页面容器

`Scaffold` 负责测量顶栏、底栏与悬浮按钮占位并向正文回传 `PaddingValues`，同时自动录制正文视口作为浮动栏位的默认背景源。组件位于 `com.itos.coui.layout`，页面业务状态、导航与滚动均由宿主持有。

- **最低系统版本**：支持库最低版本（**API 31+**）；内部各槽位控件按各自组件要求运行（例如 `Fab`、`NavigationBar` 需 API 34+）。
- **前置依赖**：仅使用 Compose UI / Foundation，纯 COUI 页面只需依赖核心 `coui`，无需 Material 3；使用自带默认背景源时无需手动创建 `CouiBackdrop`。

## 基本用法

在页面 Composable 中组合 `CouiTheme` 与 `Scaffold`，将顶栏放入 `topBar`，并将回传的 `padding` 传给列表的 `contentPadding` 与 `consumeWindowInsets`：

~~~kotlin
@Composable
fun BasicScaffoldPageExample(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<String>,
) {
    CouiTheme {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = { BasicText(title, style = LocalCouiTextStyle.current) },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                items(entries) { entry ->
                    Group {
                        Item {
                            Text(text = entry, kind = CouiTextKind.ItemTitle)
                        }
                    }
                }
            }
        }
    }
}
~~~

接入时注意以下三点：

1. **绘制与叠加顺序**：正文 `content` 先绘制，`topBar`、`bottomBar` 与 `floatingActionButton` 覆盖在正文之上，使正文可以滚动进入浮动栏位下方。
2. **自动背景源分发**：`Scaffold` 会自动录制完整正文视口与 `containerColor`，并向 `topBar`、`bottomBar` 和 `floatingActionButton` 槽位提供默认 `backdrop`，消费者不会被录入正文源中；显式传给组件的 `backdrop` 参数优先级更高。
3. **正文内部不共享自身源**：默认源不会向 `content` 子树提供，避免正文里的效果采样自身。正文内若放置 `SearchBar`、`ChipTab` 或模糊演示，需按各自组件文档单独配置隔离源，详见[背景采样](../effects/backdrop.md)。

## Padding 与系统 Insets

### 槽位与参数速查

| 参数 / 槽位 | 默认值 | 作用说明 |
| --- | --- | --- |
| `topBar` | `{}` | 顶部栏位，自动获得默认正文 `backdrop` |
| `bottomBar` | `{}` | 底部栏位，自动获得默认正文 `backdrop` |
| `containerColor` | `CouiTheme.colorScheme.background` | 正文底层背景色，录入正文 `backdrop`。不透明时也作为 [顶栏遮罩](top-app-bar.md#背景参数) 的默认颜色 |
| `windowInsets` | `WindowInsets.systemBars.union(WindowInsets.displayCutout)` | 系统安全区与屏幕缺口；传入 `WindowInsets(0, 0, 0, 0)` 可关闭默认安全区处理 |
| `floatingActionButton` | `null` | 位于 `BoxScope` 的浮动操作槽位，自动获得默认正文 `backdrop` 与安全留白 |

### 纵向安全区与正文留白分工

容器会先在自身应用并消费 `windowInsets` 的**横向安全区**（扣除父级已消费部分），保证栏位与正文共用同一安全宽度；随后按各栏位实测高度生成正文 `padding`：

| 区域状态 | 正文 `padding` 规则 | 栏位自身职责 |
| --- | --- | --- |
| 存在非零高度的顶栏 | `padding.top` 等于顶栏实测高度 | 顶栏内部负责顶部安全区 |
| 存在非零高度的底栏 | `padding.bottom` 等于底栏实测高度 | 底栏内部负责底部安全区 |
| 栏位为空或高度为 `0` | 对应边直接使用剩余系统 `Insets` | 无 |

- **内置栏位已处理纵向安全区**：`TopAppBar`、`LargeTopAppBar` 和 `CollapsingTopAppBar` 内部已处理顶部安全区；`NavigationBar` 与 `FloatingToolbar` 内部已处理底部安全区（`WindowInsets.tappableElement`）与浮动边距。不要再给内置栏位内容叠加 `statusBarsPadding()` 或 `navigationBarsPadding()`，也不要把同一纵向 Insets 再次加到正文 `padding` 上。
- **自定义栏位需自行处理**：`TopAppBarSurface` 只提供背景，内部自定义内容需自行处理一次顶部安全区；自定义底栏应按自身布局使用 `navigationBarsPadding()` 等 Modifier。
- **不同正文容器的写法**：
  - `LazyColumn`：传 `contentPadding = padding` 并配合 `Modifier.consumeWindowInsets(padding)`。
  - 普通非滚动正文：使用 `Modifier.padding(padding).consumeWindowInsets(padding)`。
  - 可滚动 `Column`：将 `.padding(padding)` 放在 `.verticalScroll(...)` **之后**，保留栏位下方的绘制采样区域。
  - 折叠顶栏页面：使用 `state.contentPadding(padding, density)` 补偿顶部，同时 `consumeWindowInsets(padding)` 仍传入 `Scaffold` 给出的原始 `padding`，详见[折叠顶栏](collapsing-app-bar.md)。
- **IME 键盘避让**：默认 `windowInsets` 不包含 `IME`。导航栏需要固定在屏幕底部时，在正文视口消费 `padding` 后应用 `imePadding()`，可滚动正文应在滚动修饰符之前处理。底部输入栏需要紧贴键盘时，在整个 `Scaffold` 外侧应用 `imePadding()`。父级已避让时沿用父级分工。输入型弹窗的处理见[宿主窗口焦点与键盘避让](dialog.md#宿主页的键盘避让)。

## 放置 FAB

`floatingActionButton` 槽位运行在 `BoxScope` 中。`Scaffold` 已为该槽位应用正文占位 `PaddingValues` 并消费对应 Insets，但不会添加额外外边距，需由调用方通过 `align` 和 `padding` 定位；`Fab` 要求 **API 34+** 并默认各自创建独立绘制器：

~~~kotlin
@RequiresApi(34)
@Composable
fun ScaffoldFabExample(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    addContent: @Composable () -> Unit,
    bodyContent: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Fab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                onClick = onAdd,
                content = addContent,
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            bodyContent()
        }
    }
}
~~~

注意：`floatingActionButton` 不改变正文返回的 `padding`。如果需要避免按钮遮挡列表最底部内容，请在正文列表尾部自行增加留白。

### 放置多个 FAB

同一个 `floatingActionButton` 槽位内可以组合多个 `Fab`。相邻按钮需设置 `foregroundComposition = true`（确保拖拽形变时不会互相覆盖背景回填），两个按钮共享容器提供的正文源并各自创建独立绘制器：

~~~kotlin
@RequiresApi(34)
@Composable
fun ScaffoldMultiFabExample(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    addContent: @Composable () -> Unit,
    editContent: @Composable () -> Unit,
    bodyContent: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Fab(
                    onClick = onEdit,
                    foregroundComposition = true,
                    content = editContent,
                )
                Fab(
                    onClick = onAdd,
                    foregroundComposition = true,
                    content = addContent,
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            bodyContent()
        }
    }
}
~~~

横向排列可将 `Column` 替换为 `Row`，重叠排布可用 `Box` 配合 `Modifier.zIndex` 控制层级；整个组合位于正文源之外，槽位安全区留白只应用一次。

## 装配注意事项

- **必须提供有界宽高**：`Scaffold` 要求父级提供有界约束（`hasBoundedWidth` 与 `hasBoundedHeight`），不能直接放入纵向无界滚动容器内。
- **混搭第三方容器**：若使用 Material 3 `Scaffold`、Miuix 或自定义容器，需手动创建 `rememberCouiBackdrop()` 并在正文容器挂载 `.couiBackdrop(backdrop)`，再显式传给浮动组件，详见[宿主页面接入](../guide/secondary-pages.md)。
- **窗口级浮层背景源**：`Scaffold` 仅录制正文源，不会创建包含顶栏/底栏的窗口背景源；若页面内需弹出带模糊材质的 `Menu` 或 `Dialog`，请在宿主根部按各自文档配置 `ProvideCouiMenuBackdrop`。

## 源码与完整示例

- **公开实现**：[Scaffold.kt](../../coui/src/main/java/com/itos/coui/layout/Scaffold.kt)
- **首页完整示例**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
- **二级页完整示例**：[DemoSettingsPage.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)
