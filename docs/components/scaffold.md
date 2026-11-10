# 页面容器

`Scaffold` 测量顶栏、底栏并向正文提供 Padding，同时为浮动栏位提供正文背景源。它使用 Compose UI / Foundation，纯 COUI 页面只需依赖核心 `coui`。

## 显示带顶栏的列表

下面用顶栏槽显示标题，将正文 Padding 传给列表。标题和条目由页面提供，Activity 负责启用 edge-to-edge。

```kotlin
@Composable
fun NativePage(
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
                modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                items(entries) { entry ->
                    Group {
                        Item { Text(text = entry, kind = CouiTextKind.PreferenceTitle) }
                    }
                }
            }
        }
    }
}
```

正文先绘制，顶栏、底栏和 FAB 覆盖在正文上。容器自动录制完整正文视口与 `containerColor`，并向 `topBar`、`bottomBar` 和 `floatingActionButton` 提供默认背景源；这些消费者不会被录入正文源。使用 COUI Scaffold 时，无需创建、挂载或传递 Backdrop。

默认源不向 `content` 子树提供，避免正文中的效果采样自身。正文内的 ChipTab、模糊演示等效果需按各自用法配置独立源。显式传入组件的 `backdrop` 优先，采样契约见[背景采样](../effects/backdrop.md)。

## Padding 与系统 Insets

`topBar`、`bottomBar`、`floatingActionButton` 均可省略。`containerColor` 默认读取 `CouiTheme.colorScheme.background`，也会进入正文采样，不修改内容槽的主题。

`windowInsets` 默认包含系统栏与屏幕缺口。容器先应用并消费横向安全区，使栏位和正文使用同一安全宽度；返回的正文 Padding 不再包含这部分。父级已消费的 Insets 会被扣除。

| 区域 | 处理方式 |
| --- | --- |
| 有顶栏 | 正文顶部 Padding 等于顶栏实测高度；顶栏内容负责顶部安全区 |
| 有底栏 | 正文底部 Padding 等于底栏实测高度；底栏负责底部安全区 |
| 栏位为空或高度为零 | 对应正文 Padding 使用剩余系统 Insets |

`TopAppBar`、`LargeTopAppBar` 和 `CollapsingTopAppBar` 已处理顶部安全区，不再给内容叠加 `statusBarsPadding()`。`TopAppBarSurface` 只提供背景，自定义内容须自行处理安全区。`NavigationBar` 自带底部可点击区域的避让和浮动间距。自定义底栏应按其布局使用 `navigationBarsPadding()` 等 Foundation Modifier。不要把同一纵向 Insets 再加到返回的正文 Padding 上。

普通正文使用 `Modifier.padding(padding).consumeWindowInsets(padding)`。`LazyColumn` 使用 `contentPadding = padding` 并消费它；`Column` 加滚动时，将 Padding 放在滚动 Modifier 后，保留栏位下方的采样区域。折叠标题需使用状态的顶部补偿，见[折叠顶栏](collapsing-app-bar.md)。

含输入框的页面在容器外侧使用 `Modifier.imePadding()`，使键盘打开时缩小实际视口；默认 `windowInsets` 不包含 IME。只增加正文尾部空白不能代替焦点避让。父级已处理 IME 时沿用父级分工。消费规则见 [Android 官方 Insets 文档](https://developer.android.com/develop/ui/compose/system/insets-ui)。

页面还会打开输入弹窗时，按[宿主窗口焦点启用键盘避让](dialog.md#宿主页的键盘避让)，避免背景页面提前空出键盘区域。

容器占满父级提供的有界宽高，不放入纵向无界滚动内容。关闭系统安全区处理可传入 `windowInsets = WindowInsets(0, 0, 0, 0)`，随后由宿主负责避让。

## 放置 FAB

`floatingActionButton` 是独立的 `BoxScope` 槽位。容器已为该槽应用正文占位 Padding 并消费 Insets，不添加默认外边距。调用方通过 `align` 与 `padding` 定位。下面片段位于 API 34 及以上的页面 Composable，`onAdd`、按钮内容 `addContent` 和正文 `content` 由页面提供。

```kotlin
Scaffold(
    floatingActionButton = {
        Fab(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            onClick = onAdd,
            content = addContent,
        )
    },
) { padding ->
    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
        content()
    }
}
```

正文仍须应用并消费返回的 Padding。FAB 不改变正文占位；需要避免遮挡正文尾项时，由页面增加相应留白。

### 放置多个 FAB

同一个槽位可组合任意多个 FAB。下面在页面右下角竖向叠放两个按钮；`onAdd`、`onEdit`、按钮内容 `addContent`、`editContent` 和正文 `content` 由页面提供，页面运行于 API 34 及以上：

```kotlin
Scaffold(
    floatingActionButton = {
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Fab(onClick = onEdit, foregroundComposition = true, content = editContent)
            Fab(onClick = onAdd, foregroundComposition = true, content = addContent)
        }
    },
) { padding ->
    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
        content()
    }
}
```

两个按钮共享容器提供的正文源，并各自创建独立绘制器。横排可用 Row，重叠可用 Box 并通过 `Modifier.zIndex` 控制层级。整个组合位于正文源之外，槽位 Padding 只应用一次。

## 混合设计应用

使用 Material 3 `Scaffold`、Miuix 或自有布局时，创建源、在正文容器挂载源，再显式传给浮动组件，见[宿主页面接入](../guide/secondary-pages.md)。导航、返回、业务状态、滚动和 Snackbar 由宿主组合；设置页直接组合 `Group` 与 `Item`。菜单和材质弹窗的窗口背景源需另行配置。

[公开实现](../../coui/src/main/java/com/itos/coui/layout/Scaffold.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt) · [二级页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)
