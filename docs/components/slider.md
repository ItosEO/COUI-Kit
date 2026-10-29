# 🎚️滑动选择条

CouiSlider 用来调节音量、亮度、强度这类连续值，也支持可见分段和隐藏点位的逻辑步进。

## 显示一个滑条

使用以下代码显示一个连续滑条：

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

值是受控的，拖动时组件只回调 `onValueChange`，保存和上报由页面自己做。`Modifier.fillMaxWidth()` 让轨道占满可用宽度，组件本身有最小尺寸，不会自动撑满父级。

## 分段选择

`steps` 是内部等分点数，两个端点也算可选值。`showTickMarks` 只决定要不要画点位和按点位吸附，关掉它不会取消逻辑步进。

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

上面会有 0、1、2、3、4 五个可选值。想要隐藏点位但仍按 5 档步进，把 `showTickMarks` 设为 `false` 即可。

## 结束回调与禁用

`onValueChangeFinished` 在一次调整结束时触发，适合保存或提交；拖动过程中只走 `onValueChange`。键盘方向键、Home 和 End 也会改值，并走同一组回调。

`enabled = false` 会关闭触摸、焦点和语义操作。配色不会自动变灰，需要把 `enabled = false` 也传给 `CouiSliderDefaults.colors`，让轨道、滑块和点位使用禁用配色。

## 轨道配色

配色从 `CouiSliderDefaults.colors(primary = accent, dark = dark)` 取，轨道 Shape 由页面选择。分段和轨道外观由页面自己适配，不是库强制主题。

## 参数校验

`value`、范围端点和步进必须是有限值，范围不能反向，`steps` 不能是负数或 `Int.MAX_VALUE`。传错会直接抛异常。

滑条不采样背景，不需要 `CouiBackdrop`。如果页面用了浮动材质，滑条作为普通内容放在源子树里即可。源码见 [CouiSlider](../../coui/src/main/java/com/itos/coui/slider/CouiSlider.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/slider/CouiSliderDefaults.kt)，连续、分段和禁用示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。
