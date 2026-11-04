# 🧭底栏

`NavigationBar` 用于在页面底部切换导航项，两侧还可以放独立的操作按钮。

## 最小调用

底栏需要 API 34、背景源、导航项、选中索引和选择回调。`items`、`selectedIndex` 与 `onSelection` 由宿主提供：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.navigation.CouiNavigationItem

@RequiresApi(34)
@Composable
fun NavigationBarExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    items: List<CouiNavigationItem>,
    selectedIndex: Int,
    onSelection: (Int) -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer() }
    NavigationBar(
        modifier = modifier,
        backdrop = backdrop,
        renderer = renderer,
        items = items,
        selectedIndex = selectedIndex,
        onSelection = onSelection,
    )
}
~~~

通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个底栏

底栏需要 API 34，以及一个覆盖页面内容的背景源。下面显示三个导航项和两个操作按钮；背景源的创建方式见[快速开始](../guide/getting-started.md#浮动组件的背景源)。

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.navigation.NavigationBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.shape.CouiCapsuleShape

@RequiresApi(34)
@Composable
fun PageNavigationBar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    surface: Color,
    onSelection: (Int) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer() }
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        CouiNavigationItem("首页", Icons.Filled.Home, Icons.Filled.Home),
        CouiNavigationItem("组件", Icons.Filled.List, Icons.Filled.List),
        CouiNavigationItem("设置", Icons.Filled.Settings, Icons.Filled.Settings),
    )
    NavigationBar(
        modifier = modifier,
        backdrop = backdrop,
        renderer = renderer,
        items = items,
        selectedIndex = selectedIndex,
        leftIcon = Icons.Filled.Notifications,
        leftDescription = "通知",
        rightIcon = Icons.Filled.Add,
        rightDescription = "新建",
        leftVisible = true,
        rightVisible = true,
        onLeftClick = onLeftClick,
        onRightClick = onRightClick,
        rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
        dark = dark,
        lightColor = Color.White,
        themeTint = surface,
        circleShape = CircleShape,
        capsuleShape = CouiCapsuleShape,
        onSelection = { index ->
            selectedIndex = index
            onSelection(index)
        },
    )
}
~~~

示例保存选中的索引，并通过 `onSelection` 通知页面切换。项目已有导航状态时，可以将索引作为参数传入 `PageNavigationBar`，再传给 `NavigationBar`。`items` 不能为空，选中索引必须属于列表。每项可以分别设置普通图标和选中图标；示例使用相同图标。示例中的 Material 图标需要由应用声明图标依赖。

将底栏放在背景源节点之外，例如放在页面 `Box` 的底部。

底栏会处理底部系统安全区，但不会给页面内容留白。请按底栏的实际高度为内容设置底部间距，不要重复叠加系统底部 inset。

## 左右附件

左右附件默认隐藏。将 `leftVisible` 或 `rightVisible` 设为 `true` 时，必须提供该侧的图标和无障碍描述。点击回调可以省略；没有回调时，附件保留展示与拖拽反馈，但不注册点击或 Button 语义。需要保留附件退场动画内容时，隐藏期间仍传入对应附件参数，只切换可见状态。

底栏主体及附件的拖拽超过触摸阈值后会接管移动事件，避免带动父级滚动。轻触仍由导航项或附件已有的点击回调处理。

`rightContainerColor` 是右侧按钮的底色；默认的 `Color.Unspecified` 会解析为 COUI 主题强调色。页面同时使用独立 FAB 时，可以显式传入相同颜色，让两个按钮保持一致。

右侧附件图标按设计固定为白色，包括底色自动取主题强调色的情况；它不读取 `onAccent`。选择底色时应与白色图标搭配。

## 配色和形状

`themeTint` 默认读取匹配模式的主题策略：原生为透明，M3 适配为宿主 surface，仅进入已有页面色相层。显式透明关闭染色；异模式自动回退也为透明。显式传入其他颜色时按该颜色染色。`lightColor` 用于高光；`circleShape` 和 `capsuleShape` 分别控制两侧按钮和中间导航栏的形状。

M3 适配下，自有文字继承宿主 ambient 字体属性，同时保留底栏自己的字号、行高与字重。附件图标仍遵守各自配色契约。

底栏使用三主体 renderer：`CouiFloatingRenderer()`。每个底栏需要独立实例，不能使用 `standalone()`。

## 版本和采样

低于 API 34 时，可以显示项目自己的导航栏。

完整页面示例见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)，公开参数见 [NavigationBar / CouiNavigationItem](../../coui/src/main/java/com/itos/coui/navigation/NavigationBar.kt)。
