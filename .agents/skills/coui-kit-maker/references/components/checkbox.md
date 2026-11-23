# Checkbox / RadioButton

整行复选时让父行持有 `toggleable` 和勾选状态；互斥单选让父行持有 `selectable`，组容器提供 `selectableGroup`。COUI 的 `Checkbox` 和 `RadioButton` 负责绘制，在行尾传入同一个选中值和启用状态。

只放一个复选框时也要提供外层点击热区和无障碍语义。已有整行点击时，图形沿用行的交互；标题与摘要按同一个 `enabled` 设置[文字样式](../containers/group.md#item)。[Checkbox 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/checkbox.md)

下面是已有设置行中的两种接法。每一行共享自己的 `MutableInteractionSource` 给 COUI 反馈与手势；单选行所在的组使用 `Modifier.selectableGroup()`。[Demo 勾选行](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceRows.kt#L104) · [Demo 单选行](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceRows.kt#L126)

```kotlin
val checkSource = remember { MutableInteractionSource() }
Item(
    modifier = Modifier
        .couiButtonFeedback(CouiTheme.isDark, checkSource, enabled, list = true)
        .toggleable(checked, checkSource, null, enabled, Role.Checkbox, onCheckedChange),
    trailing = { Checkbox(checked = checked, enabled = enabled) },
    title = checkTitle,
)

val radioSource = remember { MutableInteractionSource() }
Item(
    modifier = Modifier
        .couiButtonFeedback(CouiTheme.isDark, radioSource, enabled, list = true)
        .selectable(selected, radioSource, null, enabled, Role.RadioButton, onSelect),
    trailing = { RadioButton(selected = selected, enabled = enabled) },
    title = radioTitle,
)
```
