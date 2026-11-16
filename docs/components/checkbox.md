# 单/复选框

`RadioButton` 用于互斥单选，`Checkbox` 用于独立复选勾选。两者均为纯绘制组件，点击热区、交互反馈与无障碍语义由外层容器提供；组件分别位于 `com.itos.coui.radiobutton` 和 `com.itos.coui.checkbox`。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：24dp 基准矢量几何与双向切换动画、禁用态静态展示；不依赖背景采样源。

## 单选框（`RadioButton`）

单选组由宿主持有一个当前选中的索引或选项，外层容器使用 `selectableGroup()`，每行使用 `selectable` 与 `Role.RadioButton`：

~~~kotlin
@Composable
fun RadioButtonGroupExample(
    modifier: Modifier = Modifier,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true,
) {
    Column(modifier.selectableGroup()) {
        labels.forEachIndexed { index, label ->
            val selected = selectedIndex == index
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelect(index) },
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, Modifier.weight(1f))
                RadioButton(selected = selected, enabled = enabled)
            }
        }
    }
}
~~~

- **单选语义与状态同步**：点击已选中项不取消选择。行容器与 `RadioButton` 必须传入相同的 `selected` 与 `enabled`，不要再给 `RadioButton` 叠加第二个点击入口。
- **最小调用与自定义配色**：仅绘制时可调用 `RadioButton(selected = selected)`；需要覆盖强调色与深浅模式时传入 `colors = CouiRadioButtonDefaults.colors(selected = accent, dark = dark)`。

## 复选框整行装配（`Checkbox`）

给整行添加 `toggleable` 与 `Role.Checkbox`，点击整行文字或复选框均可切换：

~~~kotlin
@Composable
fun CheckboxRowExample(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
    enabled: Boolean = true,
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            )
            .padding(12.dp),
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

- **默认主题解析**：省略 `colors` 时，`Checkbox(checked = checked)` 读取 `CouiTheme.colorScheme.checkboxAccent`（原生主题为强调色；M3 适配在浅色取 `primary`、深色取 `inversePrimary`，详见[外观与预设](../guide/appearance.md)）。
- **自定义形状**：`shape` 控制选中时的内部填充块形状（默认 `CouiCheckboxDefaults.shape`，即 `CouiShape(4.dp)`），`borderShape` 控制未选中时的外框形状（默认 `CouiCheckboxDefaults.borderShape`，即 `CouiShape(3.2.dp)`）。

## 只放一个复选框

没有整行文案时，使用至少 `48.dp` 的 `Box` 包裹 `Checkbox`，通过 `semantics` 提供无障碍描述，并在外层 `toggleable` 设置 `indication = null` 隐藏点击涟漪：

~~~kotlin
@Composable
fun StandaloneCheckboxExample(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
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
            colors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
        )
    }
}
~~~

## 参数速查

### `RadioButton`

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符（基准绘制尺寸为 `24.dp`）。 |
| `selected` | 必填 | 当前是否处于选中状态。 |
| `enabled` | `true` | 控制启用态或禁用态外观。 |
| `colors` | `CouiRadioButtonDefaults.colors(CouiTheme.colorScheme.accent, CouiTheme.isDark)` | 选中、未选中、中心圆点及禁用态配色。 |

### `Checkbox`

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符（基准绘制尺寸为 `24.dp`）。 |
| `checked` | 必填 | 当前是否处于勾选状态。 |
| `enabled` | `true` | 控制启用态或禁用态外观。 |
| `shape` | `CouiCheckboxDefaults.shape` | 勾选内部填充块形状。 |
| `borderShape` | `CouiCheckboxDefaults.borderShape` | 复选框外框描边形状。 |
| `colors` | `CouiCheckboxDefaults.colors(CouiTheme.colorScheme.checkboxAccent, CouiTheme.isDark)` | 勾选块、边框、对勾及禁用态配色。 |

## 交互职责与装配注意事项

- **尺寸等比缩放**：`RadioButton` 与 `Checkbox` 默认均以 `24.dp` 为基准等比缩放内部路径与线宽；至少 `48.dp` 的触控热区及行内留白由外层容器保证。
- **切换动画与禁用行为**：切换中途反向时，动画会从新方向的起点重新播放；`enabled = false` 时直接显示静态目标状态，重新启用后不会补播历史切换动画。
- **示例依赖说明**：上述行示例中的 `Text` 来自 Material 3，若只使用 COUI 与 Foundation，可替换为 `com.itos.coui.text.Text` 或 `BasicText`。

## 源码与完整示例

- **单选框源码与预设**：[RadioButton.kt](../../coui/src/main/java/com/itos/coui/radiobutton/RadioButton.kt)、[CouiRadioButtonDefaults.kt](../../coui/src/main/java/com/itos/coui/radiobutton/CouiRadioButtonDefaults.kt)
- **复选框源码与预设**：[Checkbox.kt](../../coui/src/main/java/com/itos/coui/checkbox/Checkbox.kt)、[CouiCheckboxDefaults.kt](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckboxDefaults.kt)
- **Demo 完整示例**：[DemoSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
