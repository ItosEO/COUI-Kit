# 🏷️ChipTab 分类栏

CouiChipTabRow 是横向滚动的受控单选分类栏，也可以在分类组外放一个独立动作。分类数据和选中值由页面传入，选中露出、惯性和回弹由组件处理。

## 显示一个分类栏

使用以下代码显示一个受控分类栏：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.chip.CouiChipTabRow

@RequiresApi(34)
@Composable
fun Categories(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    items: List<CouiChipTabItem>,
    selectedKey: Any?,
    onSelectedKeyChange: (Any) -> Unit,
) {
    CouiChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = onSelectedKeyChange,
        backdrop = backdrop,
        dark = dark,
    )
}
~~~

`CouiChipTabItem` 的 `key` 在同一栏里必须唯一，重排和重组时也要保持稳定。非空列表的 `selectedKey` 必须属于列表；空列表传 `null`。重复点击不会取消选择，点击、键盘焦点和选中值变化都会把对应项目完整露出来。

分类栏要求 API 34，`backdrop` 要是真正的不透明兄弟节点，不能采样分类栏自己。

## 添加独立动作

`leadingAction` 放在单选组外，`leadingActionVisible` 控制它的动画显隐。需要在分类栏外放动作时：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import com.itos.coui.chip.CouiChipTabButton

CouiChipTabRow(
    items = items,
    selectedKey = selectedKey,
    onSelectedKeyChange = onSelectedKeyChange,
    backdrop = backdrop,
    dark = dark,
    leadingAction = {
        CouiChipTabButton(
            onClick = onOpenCategories,
            contentDescription = categoriesLabel,
            backdrop = backdrop,
            dark = dark,
        ) {
            Icon(Icons.Default.Menu, contentDescription = null)
        }
    },
    leadingActionVisible = categoriesVisible,
)
~~~

动作有退出过渡时，保留 `leadingAction` 的内容，只切换 `leadingActionVisible`，这样宽度、透明度和留白会一起收放。`CouiChipTabButton` 在同一个包里。图标不会被重新着色，颜色由页面传的 icon 决定。

## 文字、间距和动画

默认文字样式和内容色从 `CouiChipTabDefaults` 取，也可以自己传 `textStyle` 和 `contentColor`。`itemSpacing` 默认是 0，按页面需要加间距；`animationLevel` 的 1 最明显，4 最弱。

每个分类项和独立图标按钮各自持有单主体 renderer。显式传 `renderers` 时，数量要和 `items` 一致，实例不能重复。

## 尺寸和 renderer

文字超宽会省略。横向裁剪同时限制点击范围，纵向会保留阴影空间。

renderer 创建可能失败，需要自己兜底。要自定义错误 UI 时，在调用前创建并检查结果。

源码见 [CouiChipTabRow / CouiChipTabItem](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabRow.kt)、[CouiChipTab / CouiChipTabButton](../../coui/src/main/java/com/itos/coui/chip/CouiChipTab.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabDefaults.kt)，分类菜单、图标动作和纯色背景见 [DemoChipSamples](../../demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt)。
