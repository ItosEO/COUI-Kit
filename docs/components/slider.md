# Slider 滑动选择条

`Slider` 用于调节音量、亮度等连续数值，也支持固定档位的分段选择。组件位于 `com.itos.coui.slider`，逻辑值与保存时机由宿主持有。

- **系统版本要求**：支持库最低系统版本（API 31+）。
- **核心特性**：支持连续调节与分段吸附（`steps` / `showTickMarks`）、区分实时回调（`onValueChange`）与结束提交回调（`onValueChangeFinished`）、支持横向惯性与键盘方向键；不依赖背景采样源。

## 基本用法（连续调节）

在宿主 Composable 中传入受控 `value` 与 `onValueChange`，拖动或点击轨道时实时更新页面状态：

~~~kotlin
@Composable
fun VolumeSliderExample(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
        shape = CouiCapsuleShape,
    )
}
~~~

省略 `colors` 时（例如最小调用 `Slider(value = value, onValueChange = onValueChange)`），滑条按当前 `dark`、`enabled` 与 `CouiTheme` 自动生成配色；`shape` 默认使用胶囊形 `CouiCapsuleShape`（外观规则详见[外观与预设](../guide/appearance.md)）。

请在 `onValueChange` 中立即更新传给滑条的内存值：若设置来自 DataStore 等异步来源，用已保存的值初始化内存状态；拖动期间若较早的保存结果回传给 `value`，滑条会将其视为外部修改并结束当前手势。需要在调整结束后保存时，通过 `onValueChangeFinished` 提交最新内存值。

## 分段选择与结束提交

通过 `valueRange`、`steps` 和 `showTickMarks` 可配置固定档位，并通过 `onValueChangeFinished` 在一次调整结束时执行保存或提交：

~~~kotlin
@Composable
fun SectionSliderExample(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    onSaveValue: () -> Unit,
    dark: Boolean,
    accent: Color,
) {
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onSaveValue,
        valueRange = 0f..4f,
        steps = 3,
        showTickMarks = true,
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
    )
}
~~~

- **档位与刻度点独立控制**：`steps` 表示两个端点之间的**等分点数**（如 `0f..4f` 搭配 `steps = 3` 产生 `0`、`1`、`2`、`3`、`4` 共 5 个可选值）。`showTickMarks` 独立决定是否显示刻度点位与启用分段吸附手感；设为 `false` 会隐藏刻度点，但仍保留 `steps` 对应的 5 个量化档位。
- **实时回调 vs 结束提交**：
  - `onValueChange`：拖动、惯性滑行、松手动画或键盘操作改变值时持续触发，用于更新界面显示。
  - `onValueChangeFinished`：一次手势释放、键盘微调或无障碍 `setProgress` 结束后触发，适合持久化保存或发往服务端。

## 在回弹页面中使用滑条

按住滑条时若需让外层回弹页面停在当前位置，可在滑条的 Modifier 上观察按下与结束事件：通过 `stopScroll(MutatePriority.UserInput)` 停止正在进行的惯性滚动，并通过 `couiVerticalScroll` 的 `enabled` 暂停新的纵向手势：

~~~kotlin
@Composable
fun ScrollableSliderPageExample(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    var sliderPressed by remember { mutableStateOf(false) }
    LaunchedEffect(sliderPressed) {
        if (sliderPressed) scroll.stopScroll(MutatePriority.UserInput)
    }
    Column(modifier.fillMaxSize().couiVerticalScroll(scroll, enabled = !sliderPressed)) {
        Slider(
            modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    sliderPressed = true
                    try {
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Final)
                        } while (event.changes.any { it.pressed })
                    } finally {
                        sliderPressed = false
                    }
                }
            },
            value = value,
            onValueChange = onValueChange,
        )
        content()
    }
}
~~~

观察代码不消费触摸事件，滑条照常处理点击和拖动；松手、手势取消或滑条移除时通过 `finally` 恢复页面滚动。`onValueChangeFinished` 用于提交数值，按下后未调节数值时也依赖该手势观察恢复滚动。滚动容器参数见[边界回弹](../effects/overscroll.md#状态与拖动观察)。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符（内置最小尺寸 `144.dp × 48.dp`）。 |
| `value` | 必填 | 受控当前数值，必须为有限浮点值。 |
| `onValueChange` | 必填 | 数值变化时的实时回调。 |
| `colors` | `null` | 轨道、拇指与刻度配色；为 `null` 时按 `dark`、`enabled` 与主题自动生成。 |
| `dark` | `CouiTheme.isDark` | 控制深浅色与按压光效模式。 |
| `shape` | `CouiCapsuleShape` | 轨道裁剪轮廓，可按需覆盖。 |
| `valueRange` | `0f..1f` | 有效取值范围，端点必须有限且 `endInclusive >= start`。 |
| `steps` | `0` | 端点之间的等分点数，须满足 `0 <= steps < Int.MAX_VALUE`；`0` 为连续调节。 |
| `showTickMarks` | `false` | 是否绘制刻度点并启用分段吸附手感。 |
| `enabled` | `true` | 是否允许交互；`false` 时关闭输入并切换为禁用外观。 |
| `onValueChangeFinished` | `{}` | 一次调节结束时的提交回调。 |

## 交互职责与装配注意事项

- **严格参数校验**：`value` 与 `valueRange` 起止值必须为有限值，`valueRange` 不能反向且跨度必须有限，`steps` 不能为负数或 `Int.MAX_VALUE`，否则在组合阶段直接抛出 `IllegalArgumentException`。当 `valueRange` 起止相等时，滑条自动处于不可交互状态。
- **自定义 `colors` 的禁用态同步**：省略 `colors` 时会自动生成禁用态配色；若显式传入 `CouiSliderDefaults.colors(primary = accent, dark = dark, enabled = enabled)`，请确保将当前的 `enabled` 同步传入。
- **键盘与无障碍**：支持焦点聚焦、方向键（`Left` / `Right` / `Up` / `Down`）、`Home` / `End` 跳至端点，以及无障碍 `setProgress` 调节；`RTL` 布局自动镜像进度方向。

## 源码与完整示例

- **公开入口与运动实现**：[Slider.kt](../../coui/src/main/java/com/itos/coui/slider/Slider.kt)、[CouiSliderDefaults.kt](../../coui/src/main/java/com/itos/coui/slider/CouiSliderDefaults.kt)
- **Demo 完整示例**：[DemoSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)
