# 底栏

`NavigationBar` 在页面底部渲染胶囊式导航栏，并支持在左右两侧以融合动画展开或收起独立圆形操作按钮。选中索引与页面路由由宿主持有。

- **最低系统版本**：要求 **API 34+**。低于 API 34 时需由宿主提供自有的替代导航栏。
- **前置依赖**：需要页面背景源 `backdrop`（放入 COUI `Scaffold` 的 `bottomBar` 时可省略）；每个底栏默认各自创建独立的三主体 `CouiFloatingRenderer()`，如需在外部捕获 Shader 初始化失败也可显式传入 `renderer`（**不能**使用 `standalone()`）。

## 最小调用

传入导航项列表 `items`（不可为空）、当前选中索引 `selectedIndex` 与切换回调 `onSelection`；在 COUI `Scaffold` 的 `bottomBar` 外使用时需显式传入 `backdrop`：

~~~kotlin
@RequiresApi(34)
@Composable
fun BasicNavigationBarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    items: List<CouiNavigationItem>,
    selectedIndex: Int,
    onSelection: (Int) -> Unit,
) {
    NavigationBar(
        modifier = modifier,
        backdrop = backdrop,
        items = items,
        selectedIndex = selectedIndex,
        onSelection = onSelection,
    )
}
~~~

接入说明：

- **`items` 与索引约束**：`items` 不能为空，且 `selectedIndex` 必须处于 `items.indices` 范围内，否则组件会抛出异常。
- **`CouiNavigationItem` 图标**：`icon` 为未选中图形，`selectedIcon` 为选中与拖动预选图形；省略 `selectedIcon` 时两种状态共用 `icon`。底栏内部负责绘制 `24.dp` 图标并自动应用状态透明度，无需调用方计算 alpha。

## 搭配左右附件与自定义配色

下面展示包含 3 个导航项、左右两侧融合操作按钮及自定义右侧按钮底色的完整装配（示例使用可选的 `coui-icons` 图标包，原生主题与 M3 适配均可直接使用）：

~~~kotlin
@RequiresApi(34)
@Composable
fun NavigationBarWithAccessoriesExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    surfaceTint: Color,
    leftVisible: Boolean,
    rightVisible: Boolean,
    leftDescription: String,
    rightDescription: String,
    albumTitle: String,
    cloudTitle: String,
    createTitle: String,
    selectedIndex: Int,
    onSelection: (Int) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    val items = listOf(
        CouiNavigationItem(
            title = albumTitle,
            icon = CouiIcons.Outlined.Album,
            selectedIcon = CouiIcons.Filled.Album,
        ),
        CouiNavigationItem(
            title = cloudTitle,
            icon = CouiIcons.Outlined.CloudData,
            selectedIcon = CouiIcons.Filled.CloudData,
        ),
        CouiNavigationItem(
            title = createTitle,
            icon = CouiIcons.Outlined.WandSpark,
            selectedIcon = CouiIcons.Filled.WandSpark,
        ),
    )

    NavigationBar(
        modifier = modifier,
        backdrop = backdrop,
        items = items,
        selectedIndex = selectedIndex,
        leftIcon = CouiIcons.Filled.DarkLight,
        leftDescription = leftDescription,
        rightIcon = CouiIcons.Filled.Add,
        rightDescription = rightDescription,
        leftVisible = leftVisible,
        rightVisible = rightVisible,
        onLeftClick = onLeftClick,
        onRightClick = onRightClick,
        rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
        dark = dark,
        lightColor = Color.White,
        themeTint = surfaceTint,
        circleShape = CircleShape,
        capsuleShape = CouiCapsuleShape,
        onSelection = onSelection,
    )
}
~~~

## 左右附件

| 参数 | 默认值 | 行为说明 |
| --- | --- | --- |
| `leftVisible` / `rightVisible` | `false` / `false` | 控制左右两侧独立圆形按钮的融合展开与收起 |
| `leftIcon` / `leftDescription` | `null` / `null` | `leftVisible = true` 时**必须同时提供**图标与无障碍描述，否则校验失败 |
| `rightIcon` / `rightDescription` | `null` / `null` | `rightVisible = true` 时**必须同时提供**图标与无障碍描述，否则校验失败 |
| `onLeftClick` / `onRightClick` | `null` / `null` | 省略回调（`null`）时仍保留图标展示与拖拽形变反馈，但不注册点击或 `Role.Button` 语义 |
| `rightContainerColor` | `Color.Unspecified` | 右侧附件底色；未指定时自动解析为 COUI 主题强调色 `accent` |

- **保留收起动画内容**：从可见切换为隐藏（`leftVisible = false` 或 `rightVisible = false`）时，请**继续传入**对应侧的图标参数，只切换布尔值，以便退场缩放融合期间图标内容完整保留。
- **右侧图标固定白色**：右侧附件图标按设计固定为白色（`Color.White`），即使底色自动取主题强调色也不读取 `onAccent`；自定义 `rightContainerColor` 时应确保与白色图标保持足够对比度。当页面同时包含独立 `Fab` 时，可传入同一个颜色保持视觉一致。
- **拖拽与轻触手势**：底栏主体及两侧附件的拖拽超过系统触摸阈值后会接管移动事件，避免误触带动父级页面滚动；轻触与横向滑选松手仍由导航项或点击回调处理。

## 配色、形状与 Insets 速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `renderer` | `remember { CouiFloatingRenderer() }` | 默认由组件各自创建三主体绘制器；需处理 Shader 初始化失败时可自行创建后传入，不可使用 `standalone()` 或跨节点共享 |
| `backdrop` | `LocalCouiBackdrop.current` | COUI `Scaffold` 的 `bottomBar` 自动提供；第三方容器需显式传入覆盖正文且不含底栏的源 |
| `dark` | `CouiTheme.isDark` | 控制底栏深浅色材质与前景颜色 |
| `selectedColor` / `unselectedColor` | `Color.Unspecified` | 仅覆盖中间导航项的选中/未选中图标与文字颜色；默认日间黑色、夜间白色，选中态不透明度 `230 / 255`、未选中态 `184 / 255`。显式传入颜色时按原值应用（如 `CouiColor.Day.BLUE` / `CouiColor.Day.GRAY`），不再额外叠加状态透明度；图标应使用不含状态透明度的资源 |
| `themeTint` | `CouiTheme.materialTintFor(dark)` | 原生模式默认透明；M3 适配默认读取宿主 `surface` 染色；传入 `Color.Transparent` 关闭染色，传入其他颜色按该颜色染色（详见[外观与预设](../guide/appearance.md)） |
| `lightColor` | `Color.White` | 控制表面高光与焦散光颜色 |
| `circleShape` / `capsuleShape` | `CouiTopAppBarButtonDefaults.circleShape` / `CouiCapsuleShape` | 分别控制两侧附件与中间胶囊导航栏的形状 |

- **系统 Insets 与正文占位**：`NavigationBar` 内部会自动处理水平与底部可点击区安全区（`WindowInsets.tappableElement`）以及悬浮底边距，**不会**自行给页面正文留白。放入 COUI `Scaffold` 的 `bottomBar` 时，容器会测量底栏高度并通过正文 `padding.bottom` 回传占位；在第三方容器中需按底栏实测高度给正文留白，不要再重复叠加底部系统 `Insets`。
- **M3 字体继承**：启用 M3 适配时，导航项文字会继承宿主的 ambient 字体属性，同时保留底栏自身的字号、行高与字重；左右附件图标继续遵守各自配色契约。

## 装配注意事项

- **保持稳定组合位置**：切换页面或 Tab 内容时，请把 `NavigationBar` 与其 `renderer` 留在外层稳定组合树中，只替换上方正文内容，避免切换页面时重建 `renderer` 或中断动画。
- **放置于背景源之外**：使用第三方容器时，将 `NavigationBar` 放在页面叠加层的底部（正文 `.couiBackdrop(backdrop)` 节点之外），切勿让底栏被录入自身采样的背景源中。
- **处理 API 版本门槛**：由于依赖 API 34+ 的多主体浮动渲染器，在支持更低版本系统的应用中请在外层判断 `Build.VERSION.SDK_INT >= 34`，低版本提供常规导航栏回退。

## 源码与完整示例

- **公开实现**：[NavigationBar.kt](../../coui/src/main/java/com/itos/coui/navigation/NavigationBar.kt)
- **首页完整示例**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
