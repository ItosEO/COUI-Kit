# 🏷️ChipTab 分类栏

`ChipTabRow` 用于横向排列单选分类。分类较多时可以左右滚动，也可以在分类栏前放一个独立按钮。

## 最小调用

分类项、选中 key、选择回调和背景源由宿主提供：

~~~kotlin
import com.itos.coui.chip.ChipTabRow

ChipTabRow(
    items = items,
    selectedKey = selectedKey,
    onSelectedKeyChange = onSelectedKeyChange,
    backdrop = backdrop,
)
~~~

`dark` 默认跟随 `LocalCouiDarkTheme` 或系统；局部 `CouiTheme` 会提供自身模式。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个分类栏

下面创建三个分类，并在点击时更新选中项。分类栏需要 API 34，以及一个覆盖页面内容的背景源。

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.chip.CouiChipTabItem
import com.itos.coui.chip.ChipTabRow

@RequiresApi(34)
@Composable
fun Categories(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
) {
    val items = listOf(
        CouiChipTabItem("all", "全部"),
        CouiChipTabItem("photos", "照片"),
        CouiChipTabItem("files", "文件"),
    )
    var selectedKey by remember { mutableStateOf<Any?>("all") }

    ChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = { selectedKey = it },
        backdrop = backdrop,
    )
}
~~~

`backdrop` 由页面提供。示例在组件内保存 `selectedKey`；需要用分类筛选内容时，可以将状态提到页面中。每个分类的 `key` 必须唯一，并在重排时保持稳定。选中项更换位置后，分类栏会滚动使其完整露出。非空列表的 `selectedKey` 必须属于列表；空列表传 `null`。重复点击不会取消选择。

背景源应覆盖不透明的页面内容，分类栏放在源节点之外。创建方法见[快速开始](../guide/getting-started.md#浮动组件的背景源)。

## 添加独立动作

使用 `leadingAction` 可以在分类组前添加按钮。例如，将上一节的 `ChipTabRow` 替换为下面的代码，点击按钮打开分类管理：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import com.itos.coui.chip.ChipTabButton

ChipTabRow(
    modifier = modifier.fillMaxWidth(),
    items = items,
    selectedKey = selectedKey,
    onSelectedKeyChange = { selectedKey = it },
    backdrop = backdrop,
    leadingAction = {
        ChipTabButton(
            onClick = onOpenCategories,
            contentDescription = categoriesLabel,
            backdrop = backdrop,
        ) {
            Icon(Icons.Default.Menu, contentDescription = null)
        }
    },
    leadingActionVisible = categoriesVisible,
)
~~~

`onOpenCategories`、`categoriesLabel` 和 `categoriesVisible` 由页面提供。需要退出动画时，保留 `leadingAction` 的内容，只切换 `leadingActionVisible`。示例假定已接入可选 M3 适配，ChipSurface 自动同步最终内容色，M3 `Icon` 可直接继承；leadingAction 本身只转发内容。应用需要声明示例使用的 Material3 和图标依赖。

## 文字、间距和动画

可以通过 `textStyle` 和 `contentColor` 修改文字样式与颜色。`itemSpacing` 控制分类间距；`animationLevel` 从 1 到 4，1 的动画最明显，4 最弱。

M3 自动模式仅从 labelLarge 接入字体族，保留 Chip 的原字号、字重和排版。显式 textStyle 仍完整生效，图标显式 tint 优先。

每个分类项和独立图标按钮各自持有单主体 renderer。显式传 `renderers` 时，数量要和 `items` 一致，实例不能重复。

## 尺寸和 renderer

文字超宽时会省略，分类项会在点击、获得焦点或选中后滚动到可见区域。

需要自定义错误界面时，可以在调用前创建 renderer 并处理创建失败。

源码见 [ChipTabRow / CouiChipTabItem](../../coui/src/main/java/com/itos/coui/chip/ChipTabRow.kt)、[ChipTab / ChipTabButton](../../coui/src/main/java/com/itos/coui/chip/ChipTab.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabDefaults.kt)，分类菜单、图标动作和纯色背景见 [DemoChipSamples](../../demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt)。
