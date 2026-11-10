# 🧭底栏

`NavigationBar` 用于在页面底部切换导航项，两侧还可以放独立的操作按钮。

## 最小调用

底栏需要 API 34、背景源、导航项、选中索引和选择回调。放入 COUI Scaffold 的 `bottomBar` 时可省略 `backdrop`，容器自动提供正文源；显式参数优先。下面示例显式接收背景源，适用于第三方容器。`items`、`selectedIndex` 与 `onSelection` 由宿主提供：

~~~kotlin
@RequiresApi(34)
@Composable
fun NavigationBarExample(
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

通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个底栏

底栏需要 API 34，以及一个覆盖页面内容的背景源。下面显示三个导航项和两个操作按钮；背景源的创建方式见[快速开始](../guide/getting-started.md#浮动组件的背景源)。

~~~kotlin
@RequiresApi(34)
@Composable
fun PageNavigationBar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    surface: Color,
    onSelection: (Int) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        CouiNavigationItem(
            title = "相册",
            icon = CouiIcons.Outlined.Album,
            selectedIcon = CouiIcons.Filled.Album,
        ),
        CouiNavigationItem(
            title = "云端数据",
            icon = CouiIcons.Outlined.CloudData,
            selectedIcon = CouiIcons.Filled.CloudData,
        ),
        CouiNavigationItem(
            title = "创作",
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
        leftDescription = "切换主题",
        rightIcon = CouiIcons.Filled.Add,
        rightDescription = "新建",
        leftVisible = true,
        rightVisible = true,
        onLeftClick = onLeftClick,
        onRightClick = onRightClick,
        rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
        dark = dark,
        lightColor = Color.White,
        themeTint = surface,
        circleShape = CircleShape,
        capsuleShape = CouiCapsuleShape,
        onSelection = { index ->
            selectedIndex = index
            onSelection(index)
        },
    )
}
~~~

示例保存选中的索引，并通过 `onSelection` 通知页面切换。项目已有导航状态时，可以将索引作为参数传入 `PageNavigationBar`，再传给 `NavigationBar`。`items` 不能为空，选中索引必须属于列表。

`icon` 是未选中的图形，`selectedIcon` 是选中和拖动预选时的图形；省略 `selectedIcon` 时，两种状态共用 `icon`。底栏内部选择图形、绘制 24dp 图标并应用状态色，同时处理点击、焦点、选择语义和文字标签。页面状态通过 `onSelection` 提交。

示例使用可选的 `coui-icons` 图标包，原生主题和 M3 适配均可直接使用。主题接入见[外观与预设](../guide/appearance.md)。示例文案在实际应用中应使用字符串资源。

使用 COUI Scaffold 时，将底栏放在 `bottomBar`。第三方容器将底栏放在背景源节点之外，例如放在页面 `Box` 的底部。

底栏会处理底部系统安全区，但不会自行给正文留白。COUI Scaffold 测量底栏并通过正文 Padding 提供占位，正文应用并消费它。第三方容器按其契约处理底栏的实际高度，不要重复叠加系统底部 inset。

## 左右附件

左右附件默认隐藏。将 `leftVisible` 或 `rightVisible` 设为 `true` 时，必须提供该侧的图标和无障碍描述。点击回调可以省略；没有回调时，附件保留展示与拖拽反馈，但不注册点击或 Button 语义。需要保留附件退场动画内容时，隐藏期间仍传入对应附件参数，只切换可见状态。

底栏主体及附件的拖拽超过触摸阈值后会接管移动事件，避免带动父级滚动。轻触仍由导航项或附件已有的点击回调处理。

`rightContainerColor` 是右侧按钮的底色；默认的 `Color.Unspecified` 会解析为 COUI 主题强调色。页面同时使用独立 FAB 时，可以显式传入相同颜色，让两个按钮保持一致。

右侧附件图标按设计固定为白色，包括底色自动取主题强调色的情况；它不读取 `onAccent`。选择底色时应与白色图标搭配。

## 配色和形状

导航项的图标与文字默认共用状态色：日间为黑色、夜间为白色；选中态不透明度为 `230 / 255`，未选中态为 `184 / 255`。组件根据拖动预选或当前选择自动切换，无需调用方计算透明度。

可在 `NavigationBar` 中传入 `selectedColor` 和 `unselectedColor` 覆盖对应状态的图标与文字颜色。显式颜色按原值使用，不额外叠加状态透明度，例如 `selectedColor = CouiColor.Day.BLUE`。这些参数只影响中间导航项，左右附件使用各自的配色规则。图标应使用不含状态透明度的资源；图形自身的半透明层仍会与 tint 相乘。

例如，在现有 `NavigationBar` 调用中覆盖选中和未选中的颜色：

~~~kotlin
NavigationBar(
    backdrop = backdrop,
    items = items,
    selectedIndex = selectedIndex,
    selectedColor = CouiColor.Day.BLUE,
    unselectedColor = CouiColor.Day.GRAY,
    onSelection = onSelection,
)
~~~

`themeTint` 默认读取匹配模式的主题策略：原生为透明，M3 适配为宿主 surface，仅进入已有页面色相层。显式透明关闭染色；异模式自动回退也为透明。显式传入其他颜色时按该颜色染色。`lightColor` 用于高光；`circleShape` 和 `capsuleShape` 分别控制两侧按钮和中间导航栏的形状。

M3 适配下，自有文字继承宿主 ambient 字体属性，同时保留底栏自己的字号、行高与字重。附件图标仍遵守各自配色契约。

## 版本和采样

低于 API 34 时，可以显示项目自己的导航栏。

每个底栏默认创建独立的三主体绘制器。宿主需要处理 Shader 初始化失败时，可以先用 `CouiFloatingRenderer()` 创建绘制器，成功后通过 `renderer` 传入底栏。每个底栏各用一个实例。

完整页面示例见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)，公开参数见 [NavigationBar / CouiNavigationItem](../../coui/src/main/java/com/itos/coui/navigation/NavigationBar.kt)。
