# 🧭底栏

CouiBottomBar 是页面底部的导航栏，中间是受控的页面选项，左右各有一个附件。它内部用三主体浮动表面，背景材质从页面源采样。

## 显示一个底栏

使用以下代码显示一个带左右附件的底栏：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.navigation.CouiBottomBar
import com.itos.coui.navigation.CouiNavigationItem
import com.itos.coui.shape.CouiCapsuleShape

@RequiresApi(34)
@Composable
fun PageWithBottomBar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    background: Color,
    surface: Color,
    selectedIndex: Int,
    leftVisible: Boolean,
    rightVisible: Boolean,
    onSelection: (Int) -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    pageContent: @Composable ColumnScope.() -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer() }
    val items = listOf(
        CouiNavigationItem("首页", Icons.Filled.Home, Icons.Filled.Home),
        CouiNavigationItem("组件", Icons.Filled.List, Icons.Filled.List),
        CouiNavigationItem("设置", Icons.Filled.Settings, Icons.Filled.Settings),
    )
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            pageContent()
        }
        CouiBottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            backdrop = backdrop,
            renderer = renderer,
            items = items,
            selectedIndex = selectedIndex,
            leftIcon = Icons.Filled.Notifications,
            leftDescription = "通知",
            rightIcon = Icons.Filled.Add,
            rightDescription = "新建",
            leftVisible = leftVisible,
            rightVisible = rightVisible,
            onLeftClick = onLeftClick,
            onRightClick = onRightClick,
            rightContainerColor = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark),
            dark = dark,
            lightColor = Color.White,
            themeTint = surface,
            circleShape = CircleShape,
            capsuleShape = CouiCapsuleShape,
            onSelection = onSelection,
        )
    }
}
~~~

`items` 不能为空，`selectedIndex` 要在列表范围内。每个 `CouiNavigationItem` 的 `title`、`icon` 和 `selectedIcon` 都由页面提供；示例为了短，选中和未选中用了同一个图标，实际项目要换成对应状态。

底栏自己处理底部系统安全区，但不会给滚动内容留白。页面内容的底部间距按底栏实测高度加，不要再加一次系统 bottom inset。

## 左右附件

左右附件可以独立显隐，收放期间会短暂限制形变中的交互。附件的点击回调由页面处理。

`rightContainerColor` 是右附件的最终底色。如果页面同时有独立 FAB，两边应该用同一个颜色，否则材质和高光会对不上。

## 配色和形状

`themeTint` 用页面表面的颜色，让材质混色贴近当前页面；`lightColor` 是高光的基础色，通常传白色。`circleShape` 和 `capsuleShape` 分别对应附件和中间导航胶囊。

底栏要求用三主体 renderer，也就是 `CouiFloatingRenderer()`，不要换成 `standalone()`，也不要和别的节点共用。

## 版本和采样

底栏和相关附件要求 API 34。低于 34 时用 `NavigationBar` 或项目自己的导航组件。

`backdrop` 要覆盖真正的不透明页面内容，底栏放在源子树外。页面背景如果是透明的，采样结果会缺底。

`onSelection` 只回传索引，页面自己跳转。源码见 [CouiBottomBar / CouiNavigationItem](../../coui/src/main/java/com/itos/coui/navigation/CouiBottomBar.kt)，高度测量、左右附件和受控导航见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
