# 🏷️ChipTab 分类栏

`CouiChipTabRow` 用于横向排列单选分类。分类较多时可以左右滚动，也可以在分类栏前放一个独立按钮。

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
import com.itos.coui.chip.CouiChipTabRow

@RequiresApi(34)
@Composable
fun Categories(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
) {
    val items = listOf(
        CouiChipTabItem("all", "全部"),
        CouiChipTabItem("photos", "照片"),
        CouiChipTabItem("files", "文件"),
    )
    var selectedKey by remember { mutableStateOf<Any?>("all") }

    CouiChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = { selectedKey = it },
        backdrop = backdrop,
        dark = dark,
    )
}
~~~

`backdrop` 和 `dark` 由页面提供。示例在组件内保存 `selectedKey`；需要用分类筛选内容时，可以将状态提到页面中。每个分类的 `key` 必须唯一，并在重排时保持稳定。非空列表的 `selectedKey` 必须属于列表；空列表传 `null`。重复点击不会取消选择。

背景源应覆盖不透明的页面内容，分类栏放在源节点之外。创建方法见[快速开始](../guide/getting-started.md#浮动组件的背景源)。

## 添加独立动作

使用 `leadingAction` 可以在分类组前添加按钮。例如，将上一节的 `CouiChipTabRow` 替换为下面的代码，点击按钮打开分类管理：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import com.itos.coui.chip.CouiChipTabButton

CouiChipTabRow(
    modifier = modifier.fillMaxWidth(),
    items = items,
    selectedKey = selectedKey,
    onSelectedKeyChange = { selectedKey = it },
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

`onOpenCategories`、`categoriesLabel` 和 `categoriesVisible` 由页面提供。需要退出动画时，保留 `leadingAction` 的内容，只切换 `leadingActionVisible`。Material `Icon` 默认跟随按钮提供的内容色；显式设置 `tint` 时使用传入的颜色。应用需要声明示例使用的 Material3 和图标依赖。

## 文字、间距和动画

可以通过 `textStyle` 和 `contentColor` 修改文字样式与颜色。`itemSpacing` 控制分类间距；`animationLevel` 从 1 到 4，1 的动画最明显，4 最弱。

每个分类项和独立图标按钮各自持有单主体 renderer。显式传 `renderers` 时，数量要和 `items` 一致，实例不能重复。

## 尺寸和 renderer

文字超宽时会省略，分类项会在点击、获得焦点或选中后滚动到可见区域。

需要自定义错误界面时，可以在调用前创建 renderer 并处理创建失败。

源码见 [CouiChipTabRow / CouiChipTabItem](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabRow.kt)、[CouiChipTab / CouiChipTabButton](../../coui/src/main/java/com/itos/coui/chip/CouiChipTab.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabDefaults.kt)，分类菜单、图标动作和纯色背景见 [DemoChipSamples](../../demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt)。
