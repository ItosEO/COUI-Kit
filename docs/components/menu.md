# 📋菜单

`Menu` 可以显示普通菜单项、勾选项和一级子菜单。菜单的展开状态和选择结果由页面保存。

## 最小调用

页面提供展开状态、菜单项和回调：

~~~kotlin
Menu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    items = items,
    onItemClick = onItemClick,
)
~~~

需要采样页面背景时，把菜单调用放在[背景材质 Provider](#使用背景材质)的 `ProvideCouiMenuBackdrop` 内容中；默认样式不会创建背景源。样式默认使用 `CouiMenuDefaults.style()`，显式传入的样式仍优先。原生默认 `themeTint` 透明；M3 适配取 host surface，显式透明可关闭。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个菜单

下面的按钮会打开菜单。选择项目后调用 `onSelect`，然后关闭菜单：

~~~kotlin
@Composable
fun MenuButton(
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

`anchor` 记录按钮上的触点，菜单根据触点和窗口可见范围定位。通过键盘或无障碍服务打开时，没有触点，菜单使用默认锚点。示例中的 Material3 `Button` 和 `Text` 需要由应用声明 Material3 依赖；最小调用不需要该依赖。自定义 `style` 的创建方式见[菜单样式](#菜单样式)。

顶栏按钮与菜单放在同一个 Box 时，可省略 `anchor`，直接用按钮回调更新 `expanded`。菜单按 Popup 父布局的范围定位，因此 Box 应只包住对应按钮。需要按触点定位时再使用 `rememberCouiMenuAnchor()`；触点锚点只观察输入，点击仍由按钮或列表项处理。

`expanded` 控制菜单显隐。点击菜单项时调用 `onItemClick`；点击外部或按返回键时调用 `onDismissRequest`。需要在选中后关闭菜单时，在 `onItemClick` 中将 `expanded` 设为 `false`，如上例所示。

## 添加勾选和子菜单

每个 `CouiMenuItem` 对应一个选项：

| 字段 | 作用 |
| --- | --- |
| `value` | 提交给业务层的值 |
| `text` | 显示文本 |
| `icon` / `iconTint` | 图标和自定义颜色 |
| `selected` | 当前选中样式，和勾选状态分开 |
| `checked` | 独立复选框状态；`null` 表示普通菜单项 |
| `enabled` | 是否接受点击 |
| `dividerBefore` | 在该项前加分组线 |
| `children` | 一级子菜单；父项只展开，不提交值 |

~~~kotlin
val items = listOf(
    CouiMenuItem(0, "重命名", icon = Icons.Default.Edit, selected = selected == 0),
    CouiMenuItem(1, "固定到桌面", icon = Icons.Default.Check, checked = pinned),
    CouiMenuItem(2, "更多", dividerBefore = true, children = listOf(
        CouiMenuItem(3, "分享"),
        CouiMenuItem(4, "删除", enabled = false),
    )),
)
~~~

示例中的 `selected` 和 `pinned` 是页面状态。点击勾选项后，需要在 `onItemClick` 中更新 `pinned`。有 `children` 的父项只负责展开，子项才会提交值；子菜单只支持一级。

## 使用背景材质

不提供背景源时，菜单使用 `CouiMenuStyle.colors.container` 作为不透明底色。需要采样页面背景时，用 `ProvideCouiMenuBackdrop` 包住页面，并将返回的 Modifier 加到背景内容上：

~~~kotlin
ProvideCouiMenuBackdrop { source ->
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().then(source).background(background)) {
            pageContent()
        }
        MenuButton(style = style, label = label, items = items, onSelect = onSelect)
    }
}
~~~

这里的 `background` 和 `pageContent` 由页面提供，`MenuButton` 沿用前面的示例。菜单放在背景源之外；弹窗和页面遮罩也不要录入背景源。`style.themeTint` 可以传页面表面色。

### 空闲时按需离屏

背景内容默认使用离屏隔离。不透明的静态页面可设置 `isolateContent = false`，让没有菜单或材质弹窗消费背景时的源使用 `CompositingStrategy.Auto`。下面沿用前面的参数和 `MenuButton`：

~~~kotlin
ProvideCouiMenuBackdrop(isolateContent = false) { source ->
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().then(source).background(background)) {
            pageContent()
        }
        MenuButton(style = style, label = label, items = items, onSelect = onSelect)
    }
}
~~~

库在菜单进入、显示和退出期间保持源及其父窗口源的离屏隔离。材质弹窗按实际存活期持有背景；最后一个消费者释放后，源恢复所选的空闲策略。普通菜单、子菜单和嵌套窗口共用这套管理，宿主不要按 `expanded` 手动切换背景策略。

`Auto` 仍可能使用离屏缓冲，内容的目标混合需要自行隔离。透明窗口、移动的 BottomSheet、缩放或分数位移容器应使用默认隔离模式。完整要求见[背景源的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)，公开声明见 [ProvideCouiMenuBackdrop](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBackdrop.kt)。

## 菜单样式

`CouiMenuStyle` 设置菜单的颜色、形状和文字。下面根据页面颜色创建一份样式：

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
    )
}
~~~

`shape` 为 `CouiShape` 时，材质轮廓和普通投影从它读取半径，轮廓同时使用它的权重。`radius` 默认 `Dp.Unspecified`，表示由 Shape 解析；显式传入时必须与 Shape 的半径一致。可通过 `CouiMenuDefaults.style().copy(shape = newShape)` 修改形状。`itemShape` 和 Checkbox 的形状分别控制内部区域。

使用其他自定义 Shape 时，须显式提供有限非负的 `radius`，轮廓权重使用菜单预设。普通投影使用圆角矩形轮廓。

菜单没有默认高度上限；长菜单可以在 `Menu` 的 `modifier` 中传入 `Modifier.heightIn(max = 320.dp)`，让它在限定高度内滚动。子菜单会按窗口高度限制。

在 [ModalBottomSheet](bottom-sheet.md) 内容中，主菜单与子菜单共同使用面板的可见区域限制尺寸和位置，长列表在区域内滚动。子菜单展开时，主菜单的缩小与平移也遵守这个区域；窄面板使用上下展开的布局。该边界只属于当前面板窗口，内容中新建的独立 Dialog 按自己的窗口定位菜单。

自定义样式应传入 `dark`，省略时按 surface 亮度判断。默认工厂使用当前主题模式，主菜单与子菜单的文字、材质、阴影和反馈共同使用它。M3 模糊补底取 surface，无 Backdrop 的容器取 surfaceBright；selected 取 primary，checkbox 深色取 inversePrimary。

## 兼容性和滚动

API 33 以下的菜单模糊效果与较新系统不同，见[兼容要求](../guide/compatibility.md)。

`headerText` 用于显示提示文案，不参与选择。`themeTint` 透明时关闭采样材质上的页面色相叠加；显式样式可以提供非透明色。

标题字体默认来自 `CouiTheme.typography.label`。修改主题的 `label` 或显式传入 `CouiMenuStyle.headerTextStyle` 可以调整字体、字号、字重和行距。标题颜色、对齐和最多两行由菜单控制。

源码见 [Menu / CouiMenuItem](../../coui/src/main/java/com/itos/coui/menu/Menu.kt)、[样式](../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)、[触点锚点](../../coui/src/main/java/com/itos/coui/menu/CouiMenuAnchor.kt)，选中、勾选、子菜单和限高示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。
