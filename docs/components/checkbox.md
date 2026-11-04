# ☑️复选框

`Checkbox` 显示勾选状态。它只负责绘制，需要由外层添加点击和无障碍语义。

## 最小调用

页面提供当前勾选状态：

~~~kotlin
Checkbox(checked = checked)
~~~

组件没有点击回调或 `dark` 参数。默认配色使用 COUI checkbox 角色；原生主题使用强调色，M3 适配在浅色使用 primary、深色使用 inversePrimary。交互由外层提供。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个整行复选框

给整行添加 `toggleable`，点击文字或复选框都可以切换：

~~~kotlin
@Composable
fun CheckboxRow(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
    enabled: Boolean = true,
) {
    Row(
        modifier.fillMaxWidth().toggleable(
            value = checked,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Checkbox(
            modifier = Modifier.size(24.dp),
            checked = checked,
            enabled = enabled,
            shape = CouiShape(4.dp),
            borderShape = CouiShape(3.2.dp),
            colors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
        )
    }
}
~~~

在 `onCheckedChange` 中更新页面的 `checked`，并将 `checked` 和 `enabled` 同时传给行和复选框。不要再给复选框添加单独的点击处理，以免与整行争用点击。

`indication = null` 关掉整行涟漪，但保留 `toggleable` 的点击和 Checkbox 语义。示例中的 `Text` 来自 Material3，项目需要声明该依赖。

## 只放一个复选框

没有整行文案时，可以用 48dp 的 `Box` 包住复选框，并提供无障碍描述。外层 `toggleable` 必须设置 `indication = null`，隐藏点击涟漪，保留点击和复选语义。下面的片段使用上一节的参数和导入：

~~~kotlin
Box(
    Modifier
        .size(48.dp)
        .semantics { contentDescription = label }
        .toggleable(
            value = checked,
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = enabled,
            role = Role.Checkbox,
            onValueChange = onCheckedChange,
        ),
    contentAlignment = Alignment.Center,
) {
    Checkbox(
        checked = checked,
        enabled = enabled,
        shape = CouiShape(4.dp),
        borderShape = CouiShape(3.2.dp),
        colors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
    )
}
~~~

## 颜色和形状

`CouiCheckboxDefaults.colors(checked = accent, dark = dark)` 根据强调色和深浅色生成配色。`shape` 控制内部填充块，`borderShape` 控制外框。

切换中反向时，动画会按新方向重新播放。禁用时直接显示目标状态；重新启用后不会补播切换动画。

## 尺寸和语义

复选框以 24dp 为基准等比缩放，外层尺寸和留白由页面决定。

示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)，公开参数见 [Checkbox](../../coui/src/main/java/com/itos/coui/checkbox/Checkbox.kt) 和 [Defaults](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckboxDefaults.kt)。
