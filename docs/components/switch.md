# 🔘开关

`Switch` 用于开启或关闭一个选项，支持点击、拖动和加载状态。

## 最小调用

页面持有当前值并在回调中更新：

~~~kotlin
Switch(
    checked = checked,
    onCheckedChange = onCheckedChange,
)
~~~

省略外观参数时，深浅色和强调色由可选的 `CouiTheme` 提供；未设置时跟随系统深浅色和 COUI 预设。通用规则见[外观与预设](../guide/appearance.md)。

## 显示一个开关

在 Compose 页面中创建一个开关，点击后更新 `checked`：

~~~kotlin
var checked by rememberSaveable { mutableStateOf(false) }

Switch(
    modifier = Modifier.semantics { contentDescription = "消息通知" },
    checked = checked,
    onCheckedChange = { checked = it },
    dark = dark,
    colors = CouiSwitchDefaults.colors(accent, dark),
)
~~~

此例显式传入页面的深浅色和强调色。示例用文字说明开关的用途，实际应用可以使用字符串资源。开关处理点击、键盘和 Switch 语义；传入回调时，触摸区域会扩到 48dp。

禁用只停止手势和点击，已经开始的切换仍会走完。

## 整行点击

需要点击整行切换时，给行添加 `toggleable`，并将开关的 `onCheckedChange` 设为 `null`：

~~~kotlin
@Composable
fun SwitchRow(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    val source = remember { MutableInteractionSource() }
    Row(
        modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
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

行和开关共用 `MutableInteractionSource`，使按压和焦点反馈保持一致。示例将同一个回调传给 `onDragCheckedChange`，因此也可以拖动滑块；传 `null` 则只保留整行点击。示例中的 `Text` 来自 Material3，项目需要声明该依赖。

## 显示加载状态

需要等待异步操作时，可以在开关上显示加载状态。请求开始时设置 `loading = true`，并保留原来的 `checked`；成功后更新 `checked` 并结束加载，失败或取消时结束加载即可。下面的 `requestChange`、`label` 和 `loadingLabel` 由页面提供。

~~~kotlin
Switch(
    modifier = Modifier.semantics { contentDescription = label },
    checked = checked,
    onCheckedChange = requestChange,
    onDragCheckedChange = null,
    loading = loading,
    loadingDescription = loadingLabel,
    dark = dark,
    colors = CouiSwitchDefaults.colors(accent, dark),
)
~~~

加载期间不能重复操作。`loadingDescription` 用于向无障碍服务说明当前状态。示例通过 `onDragCheckedChange = null` 关闭拖动；如果也需要拖动，可以将请求回调传给它。

## 拖动和取消

松手后，开关会根据位置和速度吸附，并在状态改变时回调一次。手势被取消、父容器接管或组件禁用时，不提交新值，开关回到当前的 `checked` 状态。RTL 布局会反转位置，不改变 `checked` 的含义。

## 配色、焦点和触觉

配色可以从 `CouiSwitchDefaults.colors(accent, dark)` 获取。

键盘焦点描边的颜色是 `CouiSwitchColors.focusOutline`，可以通过 `copy` 修改。

触觉反馈遵循系统设置；传入 `hapticFeedbackEnabled = false` 可以关闭。

## Demo 示例

普通、加载和禁用示例见 [DemoSettingsSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsSamples.kt)，库入口和配色见 [Switch / CouiSwitchDefaults](../../coui/src/main/java/com/itos/coui/switch/Switch.kt)。
