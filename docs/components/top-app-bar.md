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
                    Text(actionLabel)
                }
            }
        }
    }
}
~~~

顶栏叠在页面内容上。请按实际顶栏高度设置内容的顶部间距，避免内容被遮挡。此例显式使用 `style`；按钮和搜索组件也可以省略它以使用默认样式。示例假定宿主已接入可选 M3 适配，Material3 `Text` 自动继承组件内容色与文字样式，项目仍需声明 Material3 依赖。

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

CouiTopAppBarButton(onClick = onSettings, style = style) {
    Icon(Icons.Default.Settings, contentDescription = settingsLabel)
}

CouiTopAppBarButton(onClick = onReset, text = true, enabled = canReset, style = style) {
    Text(resetLabel)
}
~~~

`enabled = false` 会禁用按钮，但不会隐藏它。可选 M3 适配会在最终内容作用域同步 Local，禁用 alpha 只计算一次；子内容显式颜色或内层 Local 继续优先。未接入适配的纯 native 核心需自行桥接 M3 插槽，见[外观指南](../guide/appearance.md#内容插槽与最小交互尺寸)。Material 图标、`Icon` 和 `Text` 需要由应用声明相应依赖。

## 显示和隐藏操作

将操作放进 `CouiTopAppBarButtonGroup`。需要显隐动画时，再用 `CouiTopAppBarActionVisibility` 包住按钮：

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

搜索按钮和输入框必须放在同一个顶栏里，并共享一个 `CouiTopAppBarSearchState`。默认搜索样式由 `CouiTopAppBarButtonDefaults.style(search = true)` 提供；需要覆盖时显式传入 `style`。展开、关闭、返回键和常驻动作见[顶栏搜索](search.md)。

## 版本和 inset

顶栏容器支持库的最低系统版本，顶栏按钮和搜索需要 API 34。按钮组和搜索组件放在 `CouiTopAppBar` 内使用。

每个按钮默认创建独立的 renderer。需要处理创建失败时，可以先创建 `CouiFloatingRenderer.standalone()`，再通过 `renderer` 传入。

`CouiTopAppBarButton` 的 M3 策略在原 Modifier 位置使用真正的 `minimumInteractiveComponentSize()`，遵守宿主尺寸 Local 和 alignment lines，不再叠加固定占位。原生策略保留原规则；SearchButton 的固定容器不变。仅转发的 title 与 buttonGroup 不建立新的文字样式作用域。

顶栏不消费状态栏 inset，可在顶栏内容或页面根节点添加一次 `statusBarsPadding()`。渐进模糊背景会向下延伸，但不会增加布局高度或触摸范围。

源码见 [CouiTopAppBar](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBar.kt)、[按钮](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButton.kt)、[按钮组与显隐](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonGroup.kt)、[按钮预设](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。

## 设置式折叠背景

CouiTopAppBar 的 backgroundAlpha 只控制背景，不改变标题和触摸布局。gradientSpeed 留空时保留原固定渐隐，正值选择全高幂曲线；gradientColor 非透明时按 dark 选择对应页面背景渐变。默认模糊半径由原始像素预设换算成 dp，显式 referenceBlurRadius 仍按 dp 解释。新首页可直接组合 [CouiCollapsingTopAppBar](collapsing-app-bar.md)，普通二级页继续使用原顶栏。
