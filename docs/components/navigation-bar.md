# 底栏

`NavigationBar`（包名 `com.itos.coui.navigation`）在页面底部渲染胶囊导航栏，并支持在左右两侧以融合动画展开或收起独立圆形操作按钮。选中索引与页面路由由宿主持有。

- **最低版本**：**API 34+**（标记 `@RequiresApi(34)`）；低于 API 34 时由宿主提供普通底部导航替代。
- **前置依赖**：需要不包含底栏自身的正文 `backdrop`（放入 COUI `Scaffold.bottomBar` 时自动获取）；默认各自创建三主体 `CouiFloatingRenderer()`，如需在外部捕获 Shader 初始化失败，可在 `remember` 中预创建后显式传入（**不能**使用 `standalone()`）。

## 最小可用用法

在 COUI `Scaffold` 的 `bottomBar` 槽位中直接调用，省略外观参数即自动跟随当前 `CouiTheme` 或 Material 3 主题（在第三方容器中使用时显式传入 `backdrop`）：

~~~kotlin
NavigationBar(
    items = destinations,
    selectedIndex = selectedIndex,
    onSelection = onSelectPage,
)
~~~

- `CouiNavigationItem` 的 `icon` 与 `selectedIcon` 推荐配对同名 `Outlined` 和 `Filled` 图标（见[图标选用与接入](icons.md)）；省略 `selectedIcon` 时两种状态共用 `icon`。
- 组件内部按选中状态绘制 `24.dp` 图标与透明度和文字颜色，图标资源勿预乘透明度；`selectedColor` / `unselectedColor` 可按需显式覆盖。
- 传入 `visible` 可让胶囊和两侧附件一起原地缩放显隐。收起时立即停用交互，底栏始终保留布局高度。搜索时需要把底栏固定在窗口底部，可让正文处理 `imePadding()`，由宿主按键盘的目标状态驱动 `visible`。

## 左右附件

通过 `leftAction` 和 `rightAction` 分别传入 `CouiNavigationBarAction`，任一侧均可使用默认透明样式、主题强调色有色样式或自定义外观：

~~~kotlin
NavigationBar(
    items = destinations,
    selectedIndex = selectedIndex,
    leftAction = CouiNavigationBarAction(
        icon = themeIcon,
        contentDescription = themeDescription,
        visible = showLeftAction,
        onClick = onToggleTheme,
    ),
    rightAction = CouiNavigationBarAction(
        icon = addIcon,
        contentDescription = addDescription,
        visible = showRightAction,
        onClick = onAdd,
        style = CouiNavigationBarDefaults.coloredActionStyle(),
    ),
    onSelection = onSelectPage,
)
~~~

- **显隐控制**：`CouiNavigationBarAction.visible` 默认为 `true`。需要播放融合收起动画时，保留该侧配置对象并将其 `visible` 设为 `false`；将 `leftAction` 或 `rightAction` 设为 `null` 会立即移除按钮并销毁动画状态。
- **内置样式与有色按钮**：省略 `style` 时使用 `CouiNavigationBarDefaults.transparentActionStyle()`；`CouiNavigationBarDefaults.coloredActionStyle()` 省略 `containerColor` 时取当前主题强调色，图标默认不透明白色。
- **可选点击与拖拽**：省略 `onClick`（默认 `null`）时不创建点击与 `Button` 语义，但保留拖拽、按压与光斑反馈；主体与附件横向拖动超过阈值后自动接管移动，避免带动父级滚动。

## 自定义胶囊与附件材质

`NavigationBar` 顶层的 `material`、`themeTint` 与 `lightColor` **只作用于中间胶囊**。需要调整胶囊混色、高光或关闭染色，并让透明附件同步保持一致时，将自定义 `material` 一并传给 `transparentActionStyle(material = ...)`：

~~~kotlin
val customMaterial = CouiNavigationBarDefaults.material(
    themeTint = Color.Transparent, // 仅关闭额外染色；也可改用 copy 覆盖混色
)
NavigationBar(
    items = destinations,
    selectedIndex = selectedIndex,
    material = customMaterial,
    leftAction = CouiNavigationBarAction(
        icon = themeIcon,
        contentDescription = themeDescription,
        style = CouiNavigationBarDefaults.transparentActionStyle(material = customMaterial),
    ),
    onSelection = onSelectPage,
)
~~~

## 约束与避坑

- **入参校验**：`items` 不能为空且 `selectedIndex` 必须在 `items.indices` 内，否则触发 `require` 异常。
- **材质与染色隔离**：不要假设传给 `NavigationBar` 的 `material`、`themeTint` 或 `lightColor` 会自动影响左右附件；透明附件与有色附件分别从各自的 `CouiNavigationBarAction.style` 读取材质、投影与染色（完整染色规则见[外观与预设](../guide/appearance.md#背景材质染色)）。
- **自定义附件样式**：中间胶囊固定使用 `CouiCapsuleShape`；手动构造 `CouiNavigationBarActionStyle` 时，`shape` 应使用 `CouiShape` 以保证内容裁剪、投影和融合轮廓一致，且 `shadow` 须读取 `LocalCouiContourGlowEnabled.current`。
- **Insets 与正文留白**：底栏已处理横向与底部安全区（`WindowInsets.tappableElement`）及悬浮边距，不要在外部再叠 `navigationBarsPadding()`；放入 COUI `Scaffold` 会自动通过 `PaddingValues.bottom` 为正文留白，第三方容器须按底栏实测高度给正文留白。
- **稳定组合位置与源分层**：切页时将 `NavigationBar` 及 `renderer` 留在外层稳定组合树中，避免重建导致动画中断；在第三方容器中使用时必须将其放在 `.couiBackdrop(backdrop)` 录制节点之外。
- **版本与渲染失败降级**：接入时判断 `Build.VERSION.SDK_INT >= 34`，同时捕获手动创建的 `CouiFloatingRenderer()` 构造异常；不满足条件时在同一底栏槽位改用宿主普通导航栏。
- **M3 字体继承**：启用 `CouiMaterial3Style` 时，导航项文字继承宿主字体族，同时保留组件自身的字号、行高与字重。

## 源码与完整示例

- **公开实现**：[NavigationBar.kt](../../coui/src/main/java/com/itos/coui/navigation/NavigationBar.kt)、[CouiNavigationBarAction.kt](../../coui/src/main/java/com/itos/coui/navigation/CouiNavigationBarAction.kt)、[CouiNavigationBarDefaults.kt](../../coui/src/main/java/com/itos/coui/navigation/CouiNavigationBarDefaults.kt)
- **首页与控件示例**：[DemoApp.kt](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)、[DemoChromeDetail.kt](../../demo/src/main/java/com/itos/coui/demo/DemoChromeDetail.kt)
