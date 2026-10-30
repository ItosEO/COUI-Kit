# 🧭顶栏与按钮

`CouiTopAppBar` 在页面顶部显示渐进模糊背景。你可以在其中放置标题、操作按钮和搜索框。

## 最小调用

顶栏按钮需要 API 34 和页面背景源。宿主提供按钮内容与操作：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonGroup

@RequiresApi(34)
@Composable
fun ToolbarAction(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onAction: () -> Unit,
    actionContent: @Composable () -> Unit,
) {
    CouiTopAppBar(modifier = modifier, backdrop = backdrop) {
        CouiTopAppBarButtonGroup {
            CouiTopAppBarButton(onClick = onAction) {
                actionContent()
            }
        }
    }
}
~~~

`CouiTopAppBarButton` 默认使用 `CouiTopAppBarButtonDefaults.style()`；`CouiTopAppBarSearchButton` 和 `CouiTopAppBarSearch` 默认使用 `CouiTopAppBarButtonDefaults.style(search = true)`。显式传入 `style` 时覆盖默认值。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个简单顶栏

下面的顶栏显示标题和一个文字按钮。按钮需要 API 34；请先按[快速开始](../guide/getting-started.md#浮动组件的背景源)创建背景源，再把顶栏放在源节点之外。

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarButtonGroup

@RequiresApi(34)
@Composable
fun PageToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle,
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    CouiTopAppBar(modifier = modifier.fillMaxWidth(), backdrop = backdrop) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .heightIn(min = 64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, Modifier.weight(1f), style = style.textStyle, color = style.contentColor)
            CouiTopAppBarButtonGroup {
                CouiTopAppBarButton(onClick = onAction, text = true, style = style) {
                    Text(actionLabel, style = LocalCouiTextStyle.current,
                        color = LocalCouiContentColor.current)
                }
            }
        }
    }
}
~~~

顶栏叠在页面内容上。请按实际顶栏高度设置内容的顶部间距，避免内容被遮挡。此例显式使用 `style`；按钮和搜索组件也可以省略它以使用默认样式。示例中的 Material3 `Text` 显式读取 COUI 的内容色和文字样式，项目需要声明 Material3 依赖。

## 配置顶栏样式

顶栏按钮使用 `CouiToolbarStyle` 设置材质、文字和光标。下面根据页面的颜色创建一份样式：

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

`circleShape` 用于图标按钮，`capsuleShape` 用于文字按钮和搜索框。搜索框的材质配置见[顶栏搜索](search.md#搜索框样式)。

示例让 `contourGlowEnabled` 与阴影工厂使用同一个轮廓状态，修改时请保持一致。

## 添加操作按钮

默认按钮适合放图标；设置 `text = true` 可以显示文字按钮：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle

CouiTopAppBarButton(onClick = onSettings, style = style) {
    Icon(Icons.Default.Settings, contentDescription = settingsLabel,
        tint = LocalCouiContentColor.current)
}

CouiTopAppBarButton(onClick = onReset, text = true, enabled = canReset, style = style) {
    Text(resetLabel, style = LocalCouiTextStyle.current,
        color = LocalCouiContentColor.current)
}
~~~

`enabled = false` 会禁用按钮，但不会隐藏它。Material3 `Icon` 和 `Text` 不读取 COUI 的 CompositionLocal，因此示例显式传入 COUI 内容色和文字样式。Material 图标、`Icon` 和 `Text` 需要由应用声明相应依赖。

## 显示和隐藏操作

将操作放进 `CouiTopAppBarButtonGroup`。需要显隐动画时，再用 `CouiTopAppBarActionVisibility` 包住按钮：

~~~kotlin
import com.itos.coui.topappbar.CouiTopAppBarActionVisibility

CouiTopAppBarButtonGroup {
    CouiTopAppBarActionVisibility(visible = actionsVisible) {
        CouiTopAppBarButton(onClick = onSettings, style = style) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel,
                tint = LocalCouiContentColor.current)
        }
    }
}
~~~

## 搜索

搜索按钮和输入框必须放在同一个顶栏里，并共享一个 `CouiTopAppBarSearchState`。默认搜索样式由 `CouiTopAppBarButtonDefaults.style(search = true)` 提供；需要覆盖时显式传入 `style`。展开、关闭、返回键和常驻动作见[顶栏搜索](search.md)。

## 版本和 inset

顶栏容器支持库的最低系统版本，顶栏按钮和搜索需要 API 34。按钮组和搜索组件放在 `CouiTopAppBar` 内使用。

每个按钮默认创建独立的 renderer。需要处理创建失败时，可以先创建 `CouiFloatingRenderer.standalone()`，再通过 `renderer` 传入。

顶栏不消费状态栏 inset，可在顶栏内容或页面根节点添加一次 `statusBarsPadding()`。渐进模糊背景会向下延伸，但不会增加布局高度或触摸范围。

源码见 [CouiTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBar.kt)、[按钮](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButton.kt)、[按钮组与显隐](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonGroup.kt)、[按钮预设](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
