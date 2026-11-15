# 🎚️滑动选择条

使用 `Slider` 调节音量、亮度等数值。滑条支持连续调节，也可以设置固定的档位。组件位于 `com.itos.coui.slider`。

## 显示一个滑条

### 最小调用

在页面的 Composable 中加入滑条，拖动时更新页面的 `value`：

~~~kotlin
var value by rememberSaveable { mutableFloatStateOf(0.4f) }

Slider(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = { value = it },
)
~~~

默认范围为 `0f..1f`。省略 `colors` 时，滑条按最终的 `dark`、`enabled` 和 COUI 强调色生成配色；`shape` 默认使用胶囊形。外观主题规则见[外观与预设](../guide/appearance.md)。

在 `onValueChange` 中立即更新传给滑条的内存值。若设置来自 DataStore 等异步来源，用已保存的值初始化内存状态。拖动期间，较早的保存结果回传给 `value` 时，滑条会将其视为外部修改并结束当前手势。需要在调整结束后保存时，用 `onValueChangeFinished` 提交最新内存值。

## 分段选择

设置 `valueRange` 和 `steps` 可以限制可选档位。下面在 0 到 4 之间选择整数，并在调整结束时调用页面提供的 `saveValue: (Float) -> Unit`：

~~~kotlin
var level by rememberSaveable { mutableFloatStateOf(0f) }

Slider(
    modifier = Modifier.fillMaxWidth(),
    value = level,
    onValueChange = { level = it },
    onValueChangeFinished = { saveValue(level) },
    valueRange = 0f..4f,
    steps = 3,
    showTickMarks = true,
)
~~~

`steps` 表示两个端点之间的等分点数。这里的可选值是 0、1、2、3、4；把 `showTickMarks` 设为 `false` 可以隐藏点位，同时保留五个档位。

## 结束回调与禁用

`onValueChangeFinished` 在一次调整结束时触发，适合保存或提交。拖动过程中通过 `onValueChange` 更新显示；键盘方向键、Home 和 End 也支持调整。

禁用滑条时传入 `enabled = false` 即可关闭交互；省略 `colors` 时，组件会生成禁用配色。若要传入自定义 `colors`，可用相同的 `enabled` 值调用 `CouiSliderDefaults.colors`。

## 在回弹页面中使用滑条

按住滑条时，如果要让页面停在当前位置，在滑条的 Modifier 上观察按下和结束事件。用 `stopScroll` 停止页面正在进行的惯性滚动，并通过 `couiVerticalScroll` 的 `enabled` 暂停新的纵向手势。宿主传入滑条的当前内存值、同步更新该值的回调和其余页面内容。

~~~kotlin
@Composable
fun ScrollableSliderPage(
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

观察代码不消费触摸事件，滑条照常处理点击和拖动。松手、手势取消或滑条移除时，通过 `finally` 恢复页面滚动。`onValueChangeFinished` 用于提交数值，按下后没有调整时也要由手势观察恢复滚动。滚动容器的其他参数见[边界回弹](../effects/overscroll.md#状态与拖动观察)。

## 轨道配色

需要自定义配色时，在页面的 Composable 中用宿主提供的 `accent`、`dark` 和 `enabled` 创建颜色，再传给滑条：

~~~kotlin
val colors = CouiSliderDefaults.colors(primary = accent, dark = dark, enabled = enabled)

Slider(
    value = value,
    onValueChange = onValueChange,
    dark = dark,
    enabled = enabled,
    colors = colors,
)
~~~

`value` 和 `onValueChange` 由页面状态提供。轨道形状可通过 `shape` 覆盖默认胶囊形。

## 参数校验

`value` 和范围端点必须是有限值，范围不能反向；`steps` 不能是负数或 `Int.MAX_VALUE`。参数不符合要求时会抛出异常。

滑条不需要背景源。更多示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)，公开参数见 [Slider](../../coui/src/main/java/com/itos/coui/slider/Slider.kt) 和 [Defaults](../../coui/src/main/java/com/itos/coui/slider/CouiSliderDefaults.kt)。
