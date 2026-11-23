# Slider 滑条

拖动时用 `onValueChange` 更新屏幕上的值，结束时用 `onValueChangeFinished` 持久化，避免连续拖动触发多次保存。

`steps` 只计算两个端点之间的档位。比如 `0f..4f` 要五个可选值，应传 `steps = 3`。只想隐藏刻度点时用 `showTickMarks = false`，保留 `steps` 才能保留量化档位。[Slider 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/slider.md)

五档设置先更新页面的 `level`，松手或键盘调节结束后再保存。[Demo 设置滑条](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceControls.kt#L103)

```kotlin
Slider(
    modifier = Modifier.semantics { contentDescription = title },
    value = level,
    onValueChange = { level = it },
    valueRange = 0f..4f,
    steps = 3,
    showTickMarks = true,
    onValueChangeFinished = { onSave(level) },
)
```
