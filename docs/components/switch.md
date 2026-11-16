# Switch 开关

`Switch` 用于二值开关切换，支持点击、横向拖动吸附、键盘激活、`loading` 加载状态与触觉反馈。组件位于 `com.itos.coui.switch`，开关状态与业务请求由宿主持有。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：受控二值状态、平滑胶囊几何、拖动与点击同一归属提交、支持 `loading` 等待态；不依赖背景采样源。

## 基本用法

在宿主 Composable 中保存或接收 `checked`，并在回调中更新状态：

~~~kotlin
@Composable
fun NotificationSwitchExample(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String,
    dark: Boolean,
    accent: Color,
) {
    Switch(
        modifier = modifier.semantics { contentDescription = description },
        checked = checked,
        onCheckedChange = onCheckedChange,
        dark = dark,
        colors = CouiSwitchDefaults.colors(accent, dark),
    )
}
~~~

- **省略外观参数**：直接写 `Switch(checked = checked, onCheckedChange = onCheckedChange)` 时，会读取当前 `CouiTheme` 的深浅色与强调色；未设置 `CouiTheme` 时自动跟随系统深浅色与 COUI 预设（详见[外观与预设](../guide/appearance.md)）。
- **热区与语义**：传入 `onCheckedChange` 时，开关会自动将触摸热区扩展至至少 `48.dp × 48.dp` 并附加 `Role.Switch` 语义。

## 整行点击

需要点击整行文字区域切换开关时，由外层行承担 `toggleable` 与 `Role.Switch` 语义，并将 `Switch` 的 `onCheckedChange` 设为 `null`：

~~~kotlin
@Composable
fun SwitchRowExample(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                interactionSource = source,
                indication = null,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = null,
            interactionSource = source,
            onDragCheckedChange = onCheckedChange,
            dark = dark,
            colors = CouiSwitchDefaults.colors(accent, dark),
        )
    }
}
~~~

- **共用交互源**：将同一个 `MutableInteractionSource` 传给整行 `toggleable` 与 `Switch`，可同步按压与焦点反馈。
- **保留或关闭滑块拖动**：示例将 `onCheckedChange` 传给 `onDragCheckedChange`，整行点击与滑块区域拖动共享同一业务提交归属；若希望仅保留整行点击，可将 `onDragCheckedChange` 设为 `null`。
- **示例依赖说明**：示例中的 `Text` 来自 Material 3，接入 COUI 行布局时也可搭配 [Item](item.md) 与 `com.itos.coui.text.Text`。

## 加载状态（`loading`）

需要等待异步请求确认结果时，使用 `loading` 显示加载动画并阻止重复操作：

~~~kotlin
@Composable
fun AsyncSwitchExample(
    modifier: Modifier = Modifier,
    checked: Boolean,
    loading: Boolean,
    description: String,
    loadingDescription: String,
    onRequestChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    Switch(
        modifier = modifier.semantics { contentDescription = description },
        checked = checked,
        onCheckedChange = onRequestChange,
        onDragCheckedChange = null,
        loading = loading,
        loadingDescription = loadingDescription,
        dark = dark,
        colors = CouiSwitchDefaults.colors(accent, dark),
    )
}
~~~

1. 发起异步请求时保持原 `checked` 不变，先将 `loading` 设为 `true`。
2. 请求成功后再更新 `checked` 并将 `loading` 设为 `false`；失败或取消时直接将 `loading` 设为 `false`。
3. `loading = true` 期间组件自动屏蔽点击与拖动，并通过 `loadingDescription`（`LiveRegionMode.Polite`）向无障碍服务播报加载状态。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符。独立使用时建议通过 `semantics { contentDescription = ... }` 提供标签。 |
| `checked` | 必填 | 受控当前开关状态，由宿主持有。 |
| `onCheckedChange` | 必填（可传 `null`） | 点击、键盘或无障碍切换回调；为 `null` 时由外层整行承担点击与语义。 |
| `dark` | `CouiTheme.isDark` | 控制深浅色与反馈外观。 |
| `colors` | `CouiSwitchDefaults.colors(...)` | 轨道、滑块、禁用态与焦点描边配色。 |
| `enabled` | `true` | 是否允许交互；`false` 时禁用点击与拖动。 |
| `interactionSource` | `null` | 共享按压与焦点交互源；整行装配时由外层传入。 |
| `onDragCheckedChange` | `onCheckedChange` | 滑块拖动吸附提交回调；整行模式下可显式传入以保留拖动，或传 `null` 关闭拖动。 |
| `loading` | `false` | 是否显示加载状态；为 `true` 时不可交互。 |
| `loadingDescription` | `null` | `loading = true` 时播报的无障碍状态说明。 |
| `hapticFeedbackEnabled` | `true` | 是否启用开关触觉反馈（仍受系统触觉总开关控制）。 |

## 交互职责与装配注意事项

- **拖动吸附与取消**：手指松开后，滑块根据当前位置与速度吸附到目标端，并在目标状态与当前 `checked` 不同时回调一次；若触摸被取消、父容器接管手势或过程中变为禁用，不会提交新值，滑块回到宿主当前的 `checked` 位置。
- **禁用行为**：`enabled = false`（或 `loading = true`）立即屏蔽新输入并取消进行中的手势，但已经开始的切换动画仍会平稳走完。
- **键盘焦点描边**：`CouiSwitchColors.focusOutline` 控制键盘聚焦时的外框颜色，可通过 `.copy(focusOutline = ...)` 修改。
- **RTL 布局**：RTL 下自动反转滑块的左右视觉方向，不改变 `checked` 的布尔含义。

## 源码与完整示例

- **公开入口与预设**：[Switch.kt](../../coui/src/main/java/com/itos/coui/switch/Switch.kt)
- **Demo 完整示例**：[DemoSettingsSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsSamples.kt)
