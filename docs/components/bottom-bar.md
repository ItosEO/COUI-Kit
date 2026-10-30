# 🧭底栏

`CouiBottomBar` 用于在页面底部切换导航项，两侧还可以放独立的操作按钮。

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
import com.itos.coui.navigation.CouiBottomBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.shape.CouiCapsuleShape

@RequiresApi(34)
@Composable
fun PageBottomBar(
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
    CouiBottomBar(
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

示例保存选中的索引，并通过 `onSelection` 通知页面切换。项目已有导航状态时，可以将索引作为参数传入 `PageBottomBar`，再传给 `CouiBottomBar`。`items` 不能为空，选中索引必须属于列表。每项可以分别设置普通图标和选中图标；示例使用相同图标。Material 图标需要由应用声明相应依赖。

将底栏放在背景源节点之外，例如放在页面 `Box` 的底部。

底栏会处理底部系统安全区，但不会给页面内容留白。请按底栏的实际高度为内容设置底部间距，不要重复叠加系统底部 inset。

## 左右附件

`leftVisible` 和 `rightVisible` 分别控制两侧按钮的显示，点击后调用对应的 `onLeftClick` 或 `onRightClick`。

`rightContainerColor` 是右侧按钮的底色。页面同时使用独立 FAB 时，可以传入相同颜色，让两个按钮保持一致。

## 配色和形状

`themeTint` 可以传页面表面色，`lightColor` 用于高光。`circleShape` 和 `capsuleShape` 分别控制两侧按钮和中间导航栏的形状。

底栏使用三主体 renderer：`CouiFloatingRenderer()`。每个底栏需要独立实例，不能使用 `standalone()`。

## 版本和采样

低于 API 34 时，可以显示项目自己的导航栏。

`backdrop` 应覆盖不透明的页面内容，底栏放在源节点之外。背景源的创建方式见[背景采样](../effects/backdrop.md)。

完整页面示例见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)，公开参数见 [CouiBottomBar / CouiNavigationItem](../../coui/src/main/java/com/itos/coui/navigation/CouiBottomBar.kt)。
