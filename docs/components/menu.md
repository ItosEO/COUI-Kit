# 菜单

`Menu` 提供悬浮弹出的选择菜单，支持普通菜单项、独立勾选状态、分组分隔线、顶部提示文本和一级子菜单。组件位于 `com.itos.coui.menu`，菜单的展开状态、选中结果与勾选数据均由宿主持有。

- **基础菜单与兼容模糊**：支持库的最低系统版本（API 31+）；在 API 33 以下提供背景源时使用普通 Gaussian 模糊分支（与 Fast Kawase 效果不等价）。
- **Fast Kawase 模糊材质、轮廓光与跟手光**：要求 Android 13（API 33）及以上，并需要外层通过 `ProvideCouiMenuBackdrop` 提供宿主窗口背景源。

## 基本用法

最小调用只需提供展开状态、关闭请求回调、菜单项列表和点击回调：

~~~kotlin
Menu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    items = items,
    onItemClick = onItemClick,
)
~~~

`style` 默认使用 `CouiMenuDefaults.style()`，从当前 `CouiTheme`（或 M3 适配策略）解析配色与文字样式，详见[外观与预设](../guide/appearance.md)。**点击菜单项不会自动收起菜单**，需要在 `onItemClick` 中由宿主决定是否将 `expanded` 设为 `false`；点击外部或按系统返回键会触发 `onDismissRequest`。

### 触点锚点与同 `Box` 定位

`Menu` 支持两种常见的锚点定位方式：

- **方式一：按触点弹出（`rememberCouiMenuAnchor()`）**：适合列表项、设置行或大面积按钮，菜单会根据手指按下的位置与窗口可见范围自适应弹出。
- **方式二：顶栏按钮同 `Box` 定位（省略 `anchor`）**：适合顶栏更多按钮等紧凑图标，只需用 `Box` 包住触发按钮与 `Menu`，菜单会按父级 `Box` 的位置和 `offset` 定位。

下面展示带触点锚点的菜单按钮，选择菜单项后回调 `onSelect` 并收起菜单：

~~~kotlin
@Composable
fun TouchAnchorMenuButton(
    modifier: Modifier = Modifier,
    style: CouiMenuStyle = CouiMenuDefaults.style(),
    label: String,
    items: List<CouiMenuItem<Int>>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anchor = rememberCouiMenuAnchor()

    Box(modifier) {
        Button(
            modifier = Modifier.couiMenuAnchor(anchor),
            onClick = { anchor.open { expanded = true } },
        ) {
            Text(label)
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            onItemClick = { item ->
                onSelect(item.value)
                expanded = false
            },
            style = style,
            anchor = anchor,
        )
    }
}
~~~

若使用顶栏按钮同 `Box` 定位，直接省略 `anchor` 即可：

~~~kotlin
@Composable
fun ToolbarMenuButton(
    modifier: Modifier = Modifier,
    label: String,
    items: List<CouiMenuItem<Int>>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        Button(onClick = { expanded = true }) {
            Text(label)
        }
        Menu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            onItemClick = { item ->
                onSelect(item.value)
                expanded = false
            },
        )
    }
}
~~~

定位时注意以下两点：

1. **只包裹触发按钮**：省略 `anchor` 时，菜单按 `Popup` 父布局的范围定位，因此外层 `Box` 应只包住触发按钮本身，不要包住整行或整个顶栏。
2. **触点观察与无障碍回退**：`Modifier.couiMenuAnchor(anchor)` 只观察按下坐标，不会消费点击事件；通过键盘或无障碍服务触发 `anchor.open { ... }` 时没有触点坐标，菜单自动回退到默认锚点定位。示例中的 `Button` 和 `Text` 来自 Material 3，宿主需自行声明该依赖（最小调用不依赖 Material 3）。

### 菜单选择行的图标

设置行显示当前值且点击后弹出菜单时，在行尾使用 COUI 上下双箭头（菜单打开和关闭时保持同一个图标，避免误读为折叠正文入口）。宿主绘制设置行时使用 `CouiIcons.Outlined.Popup` 或 `CouiIcons.Filled.Popup` 并按页面主题设置颜色（详见[图标用法](icons.md#指定颜色与特殊图标)）；弹出菜单中带 `children` 的父项由 `Menu` 自动绘制同样的上下双箭头。

## 菜单项：选中、勾选与子菜单

每个菜单项由 `CouiMenuItem<T>` 描述：

| 字段 | 默认值 | 说明 |
| --- | --- | --- |
| `value` | 必填 | 提交给业务层的数据值。 |
| `text` | 必填 | 菜单项显示文本。 |
| `icon` / `iconTint` | `null` | 左侧图标与可选图标染色；`iconTint = null` 时跟随当前项文字色。 |
| `selected` | `false` | 单选高亮样式（高亮底色与 `primary` 文字色），**与 `checked` 互不冲突、相互独立**。 |
| `checked` | `null` | 独立开关勾选态。非 `null` 时在左侧显示 `Checkbox`；`null` 表示普通菜单项。 |
| `enabled` | `true` | 是否响应点击或滑动选择提交。 |
| `dividerBefore` | `false` | 是否在该项上方插入一条分组分隔线。 |
| `children` | `emptyList()` | 一级子菜单项列表。非空时点击父项只负责展开/切换子菜单，**不会触发 `onItemClick`**。 |

下面的示例组合了单选高亮、独立勾选、分组线与一级子菜单：

~~~kotlin
val items = listOf(
    CouiMenuItem(0, "按名称排序", icon = Icons.Default.SortByAlpha, selected = sortMode == 0),
    CouiMenuItem(1, "显示隐藏文件", checked = showHidden),
    CouiMenuItem(
        value = 2,
        text = "更多操作",
        dividerBefore = true,
        children = listOf(
            CouiMenuItem(3, "分享"),
            CouiMenuItem(4, "删除", enabled = false),
        ),
    ),
)
~~~

- **两级菜单上限**：子菜单仅支持一级（即主菜单 + 一级子菜单，总计两级）。子项中的 `children` 必须为空，否则会抛出异常。
- **勾选状态由宿主持有**：点击带 `checked` 的菜单项后，组件不会自动翻转勾选状态；如需连续勾选多个项目，可在 `onItemClick` 中更新状态而不关闭菜单。

## 使用背景材质

不提供背景源时，菜单使用 `CouiMenuStyle.colors.container` 作为不透明底色。需要采样页面背景实现毛玻璃材质时，在页面外层包一层 `ProvideCouiMenuBackdrop`，并将回调提供的 `Modifier` 挂到页面的背景内容容器上：

~~~kotlin
@Composable
fun HostPageWithMenuExample(
    modifier: Modifier = Modifier,
    background: Color,
    style: CouiMenuStyle,
    buttonLabel: String,
    items: List<CouiMenuItem<Int>>,
    onSelect: (Int) -> Unit,
    pageContent: @Composable () -> Unit,
) {
    ProvideCouiMenuBackdrop { windowSource ->
        Box(modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .then(windowSource)
                    .background(background),
            ) {
                pageContent()
            }
            TouchAnchorMenuButton(
                style = style,
                label = buttonLabel,
                items = items,
                onSelect = onSelect,
            )
        }
    }
}
~~~

- **菜单放在背景源之外**：`Menu` 自身运行在 `Popup` 中且触发容器位于 `windowSource` 之外，弹窗和页面遮罩也不要放入 `windowSource` 节点树内。
- **页面色相叠加（`themeTint`）**：`CouiMenuStyle.themeTint` 会在采样材质上叠加页面色相（原生默认透明，M3 适配默认取 host surface）；将 `themeTint` 显式设为 `Color.Transparent` 可关闭色相叠加。

### 空闲时按需离屏

`ProvideCouiMenuBackdrop` 默认对背景内容保持离屏隔离（`isolateContent = true`）。对于已确认内容内部没有跨节点目标混合（如 `Clear`、`DstIn`）的**不透明静态页面根**，可传入 `isolateContent = false`，让窗口在空闲期间采用 `CompositingStrategy.Auto`：

~~~kotlin
ProvideCouiMenuBackdrop(isolateContent = false) { windowSource ->
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .then(windowSource)
                .background(background),
        ) {
            pageContent()
        }
        TouchAnchorMenuButton(
            style = style,
            label = buttonLabel,
            items = items,
            onSelect = onSelect,
        )
    }
}
~~~

- **自动管理消费生命周期**：菜单入场、展开、退场以及材质弹窗存活期间，库会自动恢复背景源及其父窗口源的离屏隔离；最后一个消费者释放后自动恢复空闲策略。**宿主不要按 `expanded` 手动切换策略**。
- **适用边界**：透明窗口、缩放、移动中或分数位移容器仍须使用默认 `isolateContent = true`；内容中的特殊混合模式在 `Auto` 下需要自行隔离，详见[菜单窗口的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)。

## 菜单样式

可以通过 `CouiMenuDefaults.style().copy(...)` 微调默认样式，或构造完整的 `CouiMenuStyle` 来统一控制配色、圆角、复选框外观与文字样式：

~~~kotlin
@Composable
fun rememberMenuStyle(
    dark: Boolean,
    accent: Color,
    surface: Color,
    contentColor: Color,
): CouiMenuStyle = remember(dark, accent, surface, contentColor) {
    CouiMenuStyle(
        colors = CouiMenuColors(
            surface = surface,
            container = surface,
            primary = accent,
            onSurface = contentColor,
            onSurfaceVariant = contentColor.copy(alpha = 0.6f),
            outlineVariant = contentColor.copy(alpha = 0.12f),
        ),
        shape = CouiShape(16.dp, cornerWeight = 2f),
        itemShape = CouiShape(10.dp, cornerWeight = 2f),
        checkboxShape = CouiShape(4.dp),
        checkboxBorderShape = CouiShape(3.2.dp),
        checkboxColors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
        itemTextStyle = TextStyle(fontSize = 15.sp, color = contentColor),
        headerTextStyle = TextStyle(fontSize = 12.sp, color = contentColor.copy(alpha = 0.6f)),
        themeTint = surface,
        dark = dark,
    )
}
~~~

- **显式传入 `dark`**：手写 `CouiMenuStyle` 时建议传入 `dark`，主菜单与子菜单的文字、材质、阴影与跟手反馈共用该模式；省略时按 `colors.surface` 亮度推断。
- **圆角与轮廓（`shape` 与 `radius`）**：默认 `shape` 为 `CouiShape(28.dp)`，`radius` 为 `Dp.Unspecified`（自动从 `CouiShape` 读取半径与权重）。更换为非 `CouiShape` 的自定义 `Shape` 时，须显式提供有限非负的 `radius`，轮廓权重使用菜单预设；`itemShape`、`checkboxShape`、`checkboxBorderShape` 分别控制菜单项与内置复选框圆角。
- **顶部提示文本（`headerText`）**：通过 `Menu(..., headerText = ...)` 显示顶部不可选提示文案（最多两行）。修改 `CouiTheme.typography.label` 或 `CouiMenuStyle.headerTextStyle` 可调整字体、字号、字重与行距，文字颜色由菜单按深浅色控制。
- **菜单限高与滚动**：`Menu` 默认不设固定最大高度；长菜单可在 `Menu` 的 `modifier` 中传入 `Modifier.heightIn(max = 320.dp)`，使其在限定高度内纵向滚动（子菜单会自动受可用窗口高度约束）。
- **在 [ModalBottomSheet](bottom-sheet.md) 内定位**：主菜单与子菜单共用面板的可见区域，长列表可在区域内滚动；子菜单展开时，主菜单的缩小与平移也受该区域约束。窄面板内的子菜单上下展开；独立 Dialog 内的菜单按自己的窗口定位。

## 装配注意事项

- **系统版本差异**：API 33+ 使用 Fast Kawase 模糊、RuntimeShader 轮廓光与跟手光；API 31–32 使用普通 Gaussian 模糊分支且不提供跟手光，详见[兼容要求](../guide/compatibility.md#效果门槛)。
- **宽度范围**：菜单面板宽度限制在 `128.dp` 至 `280.dp` 之间，内容按最大固有宽度测量。
- **诊断回调**：`onSpotlightDiagnostic` 仅用于调试跟手光状态，常规业务接入无需传入。

## 源码与完整示例

- **公开入口与预设**：[Menu.kt](../../coui/src/main/java/com/itos/coui/menu/Menu.kt)、[CouiMenuStyle.kt](../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)、[CouiMenuAnchor.kt](../../coui/src/main/java/com/itos/coui/menu/CouiMenuAnchor.kt)、[CouiMenuBackdrop.kt](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBackdrop.kt)
- **Demo 完整示例**：[DemoSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)、[DemoPopupSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoPopupSamples.kt)
