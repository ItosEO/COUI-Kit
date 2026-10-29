# 复选框

CouiCheckbox 绘制勾选、取消和禁用状态。宿主负责点击及无障碍语义，可让整行成为一个复选框。

## 整行切换

在 Compose 页面调用下面的函数，传入宿主文案、checked 状态和回调：

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
            colors = CouiCheckboxDefaults.colors(accent, dark),
        )
    }
}
~~~

示例的尺寸和 Shape 为宿主选择；实际 App 将 label 放入字符串资源。indication = null 关闭整行涟漪，保留 toggleable 的点击和 Checkbox 语义。

## 状态与装配

checked 与 enabled 同时传给行和绘制组件。子控件不再添加点击节点，以免一次操作触发两次更新。CouiCheckbox 不创建自己的业务状态或持久化值。

外框、底色与勾线按原厂分组绘制；勾线的选中回落曲线独立于外框和底色。切换中再次反向时，从新方向的原厂时间线起点播放，不从当前帧连续倒放。禁用时停止切换并立即绘制目标状态，重新启用不补播动画。Shape 继续由宿主提供。

## 入口与示例

[CouiCheckbox](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckbox.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/checkbox/CouiCheckboxDefaults.kt)；完整禁用态见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。
