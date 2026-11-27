# 分组、列表行与交互控件

## Group 与 Item

相邻设置项放同一 `Group`，卡片内用 `Divider`，底注用 `Group.footer` 或 `Footer`。

```kotlin
val colors = CouiPageDefaults.themeColors()
Group(title = groupTitle, footer = note, colors = colors) {
    Item(enabled = enabled, onClick = onOpen,
        title = { Text(text = title, kind = CouiTextKind.ItemTitle, color = if (enabled) colors.title else colors.disabled) },
        summary = { Text(text = summary, kind = CouiTextKind.ItemSummary, color = if (enabled) colors.summary else colors.disabled) })
    Divider()
}
```

### 避坑

- **边距叠加**：`Group`、`Footer` 已带 `16.dp` 水平边距与组间距，父容器勿加同向 padding。
- **禁用态文字未变灰**：`Item.enabled` 只控点击反馈，不改文字色；须手动按 `enabled` 切 `colors.title`/`colors.disabled`。
- **双重反馈**：自定义 `toggleable`/`selectable`/`clickable` 行，省略 `Item.onClick`。带前导图标时给 `Divider` 设对齐标题起始边的 `startIndent`。

## TextField

页面内输入使用 `com.itos.coui.textfield.TextField(value = value, onValueChange = onChange, label = fieldName)`。编辑事务或 `OutputTransformation` 交给 `TextFieldState` 重载。占位提示按格式化后的可见文本判断空态，独立多行提示受 `maxLines` 限制。密码示例的输入值和明文开关使用 `remember`。

## Switch

独立即时生效：`Switch(checked = checked, onCheckedChange = onChange)`（包 `com.itos.coui.switch`）。
异步等待直接用内置 `loading`：`Switch(checked = checked, onCheckedChange = onRequest, enabled = canChange, loading = saving, loadingDescription = savingDesc)`。

整行开关由 `Item` 管点击与语义，尾部滑块保留拖动并共用 `interactionSource`：

```kotlin
val view = LocalView.current; val source = remember { MutableInteractionSource() }; val interactive = enabled && !loading
Item(modifier = Modifier.couiButtonFeedback(CouiTheme.isDark, source, interactive, list = true)
    .toggleable(checked, source, null, interactive, Role.Switch) { target ->
        view.performCouiHapticFeedback(CouiHapticFeedback.Switch); onCheckedChange(target)
    }, title = title, trailing = { Switch(checked = checked, onCheckedChange = null,
    onDragCheckedChange = onCheckedChange, interactionSource = source, enabled = enabled, loading = loading, loadingDescription = loadingDesc) })
```

### 避坑

- 勿外挂 `CircularProgressIndicator`，勿用 `enabled = false` 冒充加载。
- 加载中点行仍提交：检查 `Item.toggleable` 是否漏用 `enabled && !loading`。
- 只要点击不要拖动：省略 `onDragCheckedChange`，保持 `onCheckedChange = null`。

## Checkbox 与 RadioButton

两者**纯绘制，无点击和无障碍语义**。整行共享 `MutableInteractionSource`；单选组外加 `Modifier.selectableGroup()`；独立放置也须包隐藏涟漪的点击热区与语义：

```kotlin
val source = remember { MutableInteractionSource() }
Item(Modifier.couiButtonFeedback(CouiTheme.isDark, source, enabled, list = true)
    .toggleable(checked, source, null, enabled, Role.Checkbox, onCheckedChange),
    trailing = { Checkbox(checked = checked, enabled = enabled) }, title = checkTitle)
// RadioButton: 改用 selectable(selected, source, null, enabled, Role.RadioButton, onSelect)
```

## Slider

拖动更新状态用 `onValueChange`，松手/键盘调节后持久化用 `onValueChangeFinished`：`Slider(Modifier.semantics { contentDescription = title }, value = level, onValueChange = { level = it }, valueRange = 0f..4f, steps = 3, showTickMarks = true, onValueChangeFinished = { onSave(level) })`。

- **避坑**：`steps` 只计端点**之间**点数（`0f..4f` 要 5 档传 `steps = 3`）；隐藏刻度设 `showTickMarks = false` 并保留 `steps`，删 `steps` 会失量化步进。

## ChipTabRow

**API 34+**。放在背景源之外并留足投影空间；前置筛选菜单按钮放 `leadingAction` 避免切走分类：

```kotlin
Box {
    Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(pageColor))
    ChipTabRow(items = items, selectedKey = selectedKey, onSelectedKeyChange = onSelect, backdrop = backdrop,
        leadingAction = { ChipTabButton(onClick = onOpenFilter, contentDescription = filterDesc, backdrop = backdrop, icon = filterIcon) })
}
```

- **避坑**：`items` 的 `key` 须唯一，非空时 `selectedKey` 须在列表中；`leadingAction` 要退场动画时保留内容只切 `leadingActionVisible`。

## SegmentedControl

**API 34+**。`Group` 或 `BottomSheet` 卡片内省略 `backdrop` 用填色分段；API 31–33 降级为单选 `Item + RadioButton`：

```kotlin
if (Build.VERSION.SDK_INT >= 34) {
    SegmentedControl(Modifier.fillMaxWidth().padding(20.dp, 12.dp), labels = options.map { it.label },
        selectedIndex = options.indexOfFirst { it.id == draftId }, onSelected = { draftId = options[it].id })
} else { /* 渲染单选 Item + RadioButton */ }
```

仅直接贴在图片、渐变或面板底图上且需透出变化时，才传兄弟 `backdrop` + 独立 `CouiFloatingRenderer.standalone()`（失败回退填色）。
**避坑**：卡片内勿传 `rememberCouiSheetSurfaceBackdrop()`（透出卡片背后色）；`labels` 非空且 `selectedIndex` 须有效（删项前先修正）。

## WheelPicker 与 CalendarPicker

单列 `WheelPicker(value, onValueChange, valueRange, label)`；日历 `CalendarPicker(value, onValueChange, minDate, maxDate)`。
多列（如 24h 时分）锁定 LTR 并用 `couiPickerSelectionLines` 画共享线，回调读同一 `MutableIntState` 最新值防快速滑动丢值：

```kotlin
CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Row(modifier.fillMaxWidth().couiPickerSelectionLines(CouiTheme.colorScheme.outlineVariant)
        .padding(vertical = CouiPickerDefaults.padding), verticalAlignment = Alignment.CenterVertically) {
        WheelPicker(Modifier.weight(1f), mins.intValue / 60, { mins.intValue = it * 60 + mins.intValue % 60 },
            0..23, hLabel, formatter = twoDigits, visualWidth = 76.dp, visualAlignment = Alignment.End)
        WheelPicker(Modifier.weight(1f), mins.intValue % 60, { mins.intValue = (mins.intValue / 60) * 60 + it },
            0..59, mLabel, formatter = twoDigits, repeatIntervalMillis = 100, visualWidth = 76.dp, visualAlignment = Alignment.Start)
    }
}
```

### 避坑

- **`WheelPicker`**：`valueRange` 非空且 `value in valueRange`；`textSize`/`focusTextSize` 为 `Dp`（不叠 SP 缩放）；对称垂直 padding 放 `couiPickerSelectionLines` 内侧。
- **`CalendarPicker`**：左右箭头仅翻月，等 `onValueChange` 再更新选中日；收紧范围前先钳制 `value`；点月份标题切内置滚轮。嵌入弹窗/面板时打开复制草稿，确认写回。
