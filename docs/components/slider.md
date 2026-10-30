# 🎚️滑动选择条

使用 `CouiSlider` 调节音量、亮度等数值。滑条支持连续调节，也可以设置固定的档位。

## 最小调用

页面提供当前值和更新回调：

~~~kotlin
import com.itos.coui.slider.CouiSlider

CouiSlider(
    value = value,
    onValueChange = onValueChange,
)
~~~

省略 `colors` 时，滑条按最终的 `dark`、`enabled` 和 COUI 强调色生成配色；`shape` 默认使用胶囊形。外观主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个滑条

下面的滑条接收页面的当前值，并在拖动时通过 `onValueChange` 更新：

~~~kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.slider.CouiSlider
import com.itos.coui.slider.CouiSliderDefaults

@Composable
fun VolumeSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    dark: Boolean,
    accent: Color,
) {
    CouiSlider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
        shape = CouiCapsuleShape,
    )
}
~~~

页面需要在 `onValueChange` 中更新 `value`。此例显式传入强调色、深浅色和形状；省略 `colors` 时会按最终的 `dark` 与 `enabled` 生成配色。需要保存到设置或发送给服务时，可以在调整结束后处理，见下文的 `onValueChangeFinished`。

## 分段选择

设置 `valueRange` 和 `steps` 可以限制可选档位。例如，在前面的页面中提供一个无参数的 `saveValue` 回调，并在 0 到 4 之间选择整数：

~~~kotlin
CouiSlider(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    onValueChangeFinished = saveValue,
    valueRange = 0f..4f,
    steps = 3,
    showTickMarks = true,
    colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
    dark = dark,
    shape = CouiCapsuleShape,
)
~~~

`steps` 表示两个端点之间的等分点数。这里的可选值是 0、1、2、3、4；把 `showTickMarks` 设为 `false` 可以隐藏点位，同时保留五个档位。

## 结束回调与禁用

`onValueChangeFinished` 在一次调整结束时触发，适合保存或提交。拖动过程中通过 `onValueChange` 更新显示；键盘方向键、Home 和 End 也支持调整。

禁用滑条时传入 `enabled = false` 即可关闭交互；省略 `colors` 时，组件会生成禁用配色。若要传入自定义 `colors`，可用相同的 `enabled` 值调用 `CouiSliderDefaults.colors`。

## 轨道配色

显式配色可用 `CouiSliderDefaults.colors(primary = accent, dark = dark, enabled = enabled)` 创建。轨道形状可按页面需要覆盖默认胶囊形。

## 参数校验

`value` 和范围端点必须是有限值，范围不能反向；`steps` 不能是负数或 `Int.MAX_VALUE`。参数不符合要求时会抛出异常。

滑条不需要背景源。更多示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)，公开参数见 [CouiSlider](../../coui/src/main/java/com/itos/coui/slider/CouiSlider.kt) 和 [Defaults](../../coui/src/main/java/com/itos/coui/slider/CouiSliderDefaults.kt)。
