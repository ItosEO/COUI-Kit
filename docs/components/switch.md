# 🔘开关

CouiSwitch 是受控的二值开关，带弹簧拖动和吸附，也能在同一组件里显示加载状态。

## 显示一个开关

使用以下代码显示一个独立开关：

~~~kotlin
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.itos.coui.switch.CouiSwitch
import com.itos.coui.switch.CouiSwitchDefaults

CouiSwitch(
    modifier = Modifier.semantics { contentDescription = label },
    checked = checked,
    onCheckedChange = onCheckedChange,
    dark = dark,
    colors = CouiSwitchDefaults.colors(accent, dark),
)
~~~

点击只请求一个新的 `checked`，真正改不改由页面决定。开关自己处理点击、键盘和 Switch 语义；视觉尺寸固定为 44×24dp，传入 `onCheckedChange` 时触摸区域会扩到 48dp。

禁用只停止手势和点击，已经开始的切换仍会走完。

## 整行点击

需要整行切换时，让外层持有唯一的点击 owner，子开关只负责绘制：

~~~kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.itos.coui.switch.CouiSwitch
import com.itos.coui.switch.CouiSwitchDefaults

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
        CouiSwitch(
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

外层和子开关共用同一个 `MutableInteractionSource`，按压和焦点反馈才会同步。如果还想在滑块区域拖动，把同一个业务回调传给 `onDragCheckedChange`；传 `null` 就只保留整行点击。

## 显示加载状态

加载是同一个 CouiSwitch 的状态，不是另一个控件。请求开始时把 `loading` 设为 `true`，先保留原 `checked`；成功时提交新值并清除加载，失败或取消时只清除加载。

~~~kotlin
CouiSwitch(
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

等待期间组件会禁止重复输入，`loadingDescription` 通过状态说明和 live region 读出来。示例同时关掉拖动，避免等待中又被拖走。

滑块缩小、加载图放大并旋转；停止时恢复滑块并立即关闭加载图。加载图是随库打包的 PNG。

## 拖动和取消

按住后开始拖动，按压反馈会保留到松手或真正取消；点击识别不会提前结束它。

超过拖动阈值后，开关拥有本次拖动，抬手按最近锚点、速度与位移门控吸附，结束时只提交一次业务变化。RTL 会反转几何位置，`checked` 的逻辑含义不变；动画中反向会保留已有位置和速度。

CANCEL、父容器接管、禁用和移除都会终止手势，不提交新值，并回到 `checked` 的状态。

## 配色、焦点和触觉

配色从 `CouiSwitchDefaults.colors(accent, dark)` 取。轨道和拉伸后的滑块使用项目 `CouiCapsuleShape`，这是保留的平滑几何。

键盘焦点沿轨道外侧显示独立描边，颜色是 `CouiSwitchColors.focusOutline`，可以 `copy` 覆盖。这个描边复用现有状态弹簧，不属于材质轮廓光。

触觉走系统设置，`hapticFeedbackEnabled = false` 可以关掉。程序赋值、取消和禁用不会额外振动。

## 版本和验收

开关沿库最低平台运行。快速反向、RTL、字体缩放和 TalkBack 仍要真机确认。

## Demo 示例

进入 Demo 的“组件”页，找到“开关”分组，或直接搜索分组标题。分组里有普通点击/拖动、加载状态、禁用未选中和禁用已选中；后两项只是状态示例，不是独立控件。

Demo 示例见 [DemoSwitchSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSwitchSamples.kt)，库入口和配色见 [CouiSwitch / CouiSwitchDefaults](../../coui/src/main/java/com/itos/coui/switch/CouiSwitch.kt)。
