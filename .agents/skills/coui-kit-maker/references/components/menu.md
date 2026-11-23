# Menu 菜单

默认在已安装[窗口背景源](../effects/backdrop.md#窗口背景源)的作用域中调用 `Menu`，沿用其背景模糊与光效。Demo 单独提供模糊对照开关并默认开启。[Demo 菜单材质](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPopupSamples.kt#L40)

选位置先看触发区域。设置行很宽，用户通常希望菜单在手指附近出现。顶栏的“更多”按钮很小，用户希望无论点中按钮哪一侧，菜单都从同一处展开。

## 设置行菜单

在行和菜单共同的 Composable 中记住一个 `anchor`。把观察触点的修饰符放到整行，在行原有的点击回调里调用 `anchor.open`，最后把同一个 `anchor` 传给 `Menu`。下面的 `title` 是行标题槽，`items` 和 `onSelect` 来自页面。

```kotlin
val anchor = rememberCouiMenuAnchor()
var expanded by remember { mutableStateOf(false) }

Box {
    Item(
        modifier = Modifier.couiMenuAnchor(anchor),
        onClick = { anchor.open { expanded = true } },
        title = title,
    )
    Menu(
        anchor = anchor,
        expanded = expanded,
        onDismissRequest = { expanded = false },
        items = items,
        onItemClick = { item ->
            expanded = false
            onSelect(item.value)
        },
    )
}
```

在行的左右两端分别点一次，菜单应随按下位置横向移动，并避开窗口边缘。键盘或无障碍激活时没有触点，菜单按触发容器定位。`.couiMenuAnchor` 只观察触点；再给子图标加一套点击处理会拆开行的交互。需要在行尾提示“打开选择菜单”时用 `CouiIcons.Outlined.Popup`，展开后仍保持上下双箭头。[Demo 设置菜单行](https://github.com/ItosEO/COUI-Kit/blob/main/demo/src/main/java/com/itos/coui/demo/DemoPreferenceControls.kt)

设计要求菜单打开期间高亮触发行时，在行的 `couiButtonFeedback` 上传 `list = true, hoverLocked = expanded`，让它和行原有的 `clickable` 共用一个 `MutableInteractionSource`。这条路径由行的 Modifier 负责点击，使用 `Item` 时省略其 `onClick`，避免叠加两层按压反馈。

## 按钮菜单

在顶栏的 `actions` 槽内，为触发按钮单独放一个随按钮大小测量的 `Box`。按钮和菜单是它的两个子项，省略 `anchor`。API 34+ 使用 COUI 浮动按钮，API 31–33 可以沿用宿主的普通按钮，两者打开同一个菜单。`moreIcon` 由宿主提供，并带有“更多操作”的无障碍描述。

```kotlin
var expanded by remember { mutableStateOf(false) }

Box {
    if (Build.VERSION.SDK_INT >= 34) {
        TopAppBarButton(onClick = { expanded = true }) { moreIcon() }
    } else {
        androidx.compose.material3.IconButton(onClick = { expanded = true }) { moreIcon() }
    }
    Menu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        items = items,
        onItemClick = { item ->
            expanded = false
            onSelect(item.value)
        },
        offset = DpOffset(0.dp, 4.dp),
    )
}
```

这样锚定的是完整按钮的布局范围。点按钮左右两侧，菜单位置应保持一致；下方空间充足时，示例会留出 `4.dp` 间距。如果菜单偏到一组按钮中间，检查 `Box` 是否包住了整排操作。如果菜单贴着小图标，检查 `Menu` 是否被放进了按钮的图标槽。先修正容器，再调整间距。[Demo 固定按钮菜单](https://github.com/ItosEO/COUI-Kit/blob/main/demo/src/main/java/com/itos/coui/demo/DemoPopupSamples.kt)

两种定位都由页面持有 `expanded`。单选通常在提交后关闭；连续勾选可只更新 `checked` 并保持展开。单选高亮用 `selected`，勾选标记用 `checked`。若定位正确但菜单显示为纯色，检查[窗口背景源](../effects/backdrop.md#窗口背景源)。[菜单用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/menu.md)
