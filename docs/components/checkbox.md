# ☑️复选框

CouiCheckbox 只画勾选、取消和禁用状态，不创建点击和无障碍语义。点击交给整行或外层容器。

## 显示一个整行复选框

使用以下代码让整行都能切换：

~~~kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.checkbox.CouiCheckbox
import com.itos.coui.checkbox.CouiCheckboxDefaults
import com.itos.coui.shape.CouiShape

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
        CouiCheckbox(
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

`checked` 和 `enabled` 要同时传给行和绘制组件。子控件不要再加 `clickable`，否则点复选框时整行回调不触发。

`indication = null` 关掉整行涟漪，但保留 `toggleable` 的点击和 Checkbox 语义。

## 只放一个复选框

没有整行文案时，用 48dp 的 Box 包住复选框，给它一个点击节点和可访问说明：

~~~kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

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
    CouiCheckbox(
        checked = checked,
        enabled = enabled,
        shape = CouiShape(4.dp),
        borderShape = CouiShape(3.2.dp),
        colors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
    )
}
~~~

`CouiCheckbox` 不持有业务状态，也不会自己取反。`checked` 从页面来，`onValueChange` 只把请求传回去。

## 颜色和形状

`CouiCheckboxDefaults.colors(checked = accent, dark = dark)` 提供选中、未选中、禁用和勾线颜色，`accent` 通常传当前主题色。`shape` 是内部填充块的轮廓，`borderShape` 是外框，两者可以不一样。

切换中再次反向时，勾线会从新方向的时间线重新播放，不会从当前帧倒放。禁用时直接画目标状态，重新启用不补播动画。

## 尺寸和语义

复选框以 24dp 为基准等比缩放，外层尺寸和留白由页面布局决定。给它传自己的 Shape 时，半径会按实际尺寸收敛。

整行可点击时只保留一个点击 owner，避免无障碍读出一组重复操作。源码见 [CouiCheckbox](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckbox.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckboxDefaults.kt)，完整禁用态见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。
