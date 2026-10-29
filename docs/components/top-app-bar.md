# 🧭顶栏与按钮

CouiTopAppBar 负责页面顶部的渐进模糊、标题和操作区。按钮、按钮组和搜索都从顶栏拿同一份材质与转场上下文，所以它们必须放在顶栏内部。

## 显示一个简单顶栏

使用以下代码显示一个带标题和文字按钮的顶栏：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonGroup

@RequiresApi(34)
@Composable
fun PageWithToolbar(
    modifier: Modifier = Modifier,
    style: CouiToolbarStyle,
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    background: Color,
    pageContent: @Composable ColumnScope.() -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            pageContent()
        }
        CouiTopAppBar(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
            backdrop = backdrop,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
                    .heightIn(min = 64.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, Modifier.weight(1f), style = style.textStyle, color = style.contentColor)
                CouiTopAppBarButtonGroup {
                    CouiTopAppBarButton(onClick = onAction, text = true, style = style) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}
~~~

请注意，顶栏是叠在内容上的，不会自动给内容让位。`pageContent` 的顶部间距要按顶栏实际高度自己加，否则第一项会被遮住。`style` 可以用下一节的 `rememberToolbarStyle` 生成。

## 配置顶栏样式

`CouiToolbarStyle` 描述按钮材质、轮廓、文字和光标。库不读 MaterialTheme，下面用页面当前的深浅色和轮廓开关生成一份：

~~~kotlin
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBarButtonDefaults

@Composable
fun rememberToolbarStyle(
    dark: Boolean,
    accent: Color,
    contentColor: Color,
    textStyle: TextStyle,
): CouiToolbarStyle {
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    return remember(dark, accent, contentColor, textStyle, density, contour) {
        CouiToolbarStyle(
            dark = dark,
            circleShape = CircleShape,
            capsuleShape = CouiCapsuleShape,
            material = { capsule -> CouiTopAppBarButtonDefaults.material(dark, capsule) },
            shadow = { capsule ->
                CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour)
            },
            contentColor = contentColor,
            textStyle = textStyle,
            cursorColor = accent,
            contourGlowEnabled = contour,
        )
    }
}
~~~

`material` 和 `shadow` 要使用同一个轮廓状态，否则按钮的投影和描边会对不上。`circleShape` 用于方形按钮，`capsuleShape` 用于文字按钮和搜索框。搜索外观可以把两个默认工厂的 `search` 参数设为 `true`。

## 添加操作按钮

`text = true` 显示文字胶囊，默认是 40dp 方形图标按钮。例如：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon

CouiTopAppBarButton(onClick = onSettings, style = style) {
    Icon(Icons.Default.Settings, contentDescription = settingsLabel)
}

CouiTopAppBarButton(onClick = onReset, text = true, enabled = canReset, style = style) {
    Text(resetLabel)
}
~~~

按钮自己处理按压形变、点触光和键盘语义，外面不用再包一层 `clickable`。`enabled = false` 只改变交互和内容透明度，不会让按钮消失。图标由你传，组件不会重新着色；深色底上要自己给 `tint`。Material 图标依赖需要自己声明。

## 显示和隐藏操作

按钮组包住整个操作槽位。需要显隐过渡时，把按钮包进 `CouiTopAppBarActionVisibility`：

~~~kotlin
import com.itos.coui.topappbar.CouiTopAppBarActionVisibility

CouiTopAppBarButtonGroup {
    CouiTopAppBarActionVisibility(visible = actionsVisible) {
        CouiTopAppBarButton(onClick = onSettings, style = style) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel)
        }
    }
}
~~~

## 搜索

搜索按钮和输入框必须放在同一个顶栏里，并共享一个 `CouiTopAppBarSearchState`。展开、关闭、返回键和常驻动作见[顶栏搜索](search.md)。

## 版本和 inset

按钮和搜索要求 API 34；按钮组本身没有版本注解，但只能在顶栏里用。顶栏容器不要求 API 34。

每个按钮默认在调用点创建一个独立的 `CouiFloatingRenderer.standalone()`。需要捕获创建失败时，先创建好再显式传 `renderer`。

顶栏背景会向下延伸，但不会增加布局高度或触摸范围。顶栏不自己消费状态栏 inset；在顶栏内容或页面根节点加一次 `statusBarsPadding()` 就够了。

源码见 [CouiTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBar.kt)、[按钮](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButton.kt)、[按钮组与显隐](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonGroup.kt)、[按钮预设](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
