# ☑️复选框

`CouiCheckbox` 显示勾选状态。它只负责绘制，需要由外层添加点击和无障碍语义。

## 显示一个整行复选框

给整行添加 `toggleable`，点击文字或复选框都可以切换：

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

在 `onCheckedChange` 中更新页面的 `checked`，并将 `checked` 和 `enabled` 同时传给行和复选框。不要再给复选框添加单独的点击处理，以免与整行争用点击。

`indication = null` 关掉整行涟漪，但保留 `toggleable` 的点击和 Checkbox 语义。示例中的 `Text` 来自 Material3，项目需要声明该依赖。

## 只放一个复选框

没有整行文案时，可以用 48dp 的 `Box` 包住复选框，并提供无障碍描述。下面的片段使用上一节的参数和导入：

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

## 颜色和形状

`CouiCheckboxDefaults.colors(checked = accent, dark = dark)` 根据强调色和深浅色生成配色。`shape` 控制内部填充块，`borderShape` 控制外框。

切换中反向时，动画会按新方向重新播放。禁用时直接显示目标状态；重新启用后不会补播切换动画。

## 尺寸和语义

复选框以 24dp 为基准等比缩放，外层尺寸和留白由页面决定。

示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)，公开参数见 [CouiCheckbox](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckbox.kt) 和 [Defaults](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckboxDefaults.kt)。
