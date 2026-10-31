# ➕FAB

`CouiFab` 适合放置新建、编辑等页面操作，可以单独使用，也可以与底栏一起使用。

## 最小调用

FAB 需要 API 34、页面背景源和单独的 standalone renderer；页面提供点击回调与内容：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.floatingactionbutton.CouiFab

@RequiresApi(34)
@Composable
fun PageFab(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer.standalone() }
    CouiFab(modifier = modifier, backdrop = backdrop, renderer = renderer, onClick = onClick, content = content)
}
~~~

省略 material 时，底色取 COUI 强调色，内容色取 `onAccent`；省略 shadow 时，阴影颜色从最终 `material.firstColor` 取得。显式 material 保留宿主 ambient 内容色，子内容的显式颜色继续优先。默认强调色本身可以有颜色，但这不表示启用了 Material 页面染色；FAB 不叠加菜单式 `themeTint`。通用主题规则见[外观与预设](../guide/appearance.md)；强调色生成策略（包括 HCT）由宿主负责。

## 使用显式配色预设

FAB 需要 API 34 和页面背景源。下面使用内置配色创建一个按钮：

~~~kotlin
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.floatingactionbutton.CouiFab
import com.itos.coui.floatingactionbutton.CouiFabColor
import com.itos.coui.floatingactionbutton.CouiFabDefaults
import com.itos.coui.shape.CouiShape

@RequiresApi(34)
@Composable
fun PresetFab(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    preset: CouiFabColor,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val renderer = remember { CouiFloatingRenderer.standalone() }
    val color = CouiFabDefaults.containerColor(preset, dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current
    CouiFab(
        modifier = modifier,
        backdrop = backdrop,
        renderer = renderer,
        onClick = onClick,
        dark = dark,
        shape = CouiShape(999.dp, cornerWeight = 2f),
        material = CouiFabDefaults.material(color, dark),
        shadow = CouiFabDefaults.shadow(color, density, contour),
        content = content,
    )
}
~~~

`preset` 可以选择 `Notes`、`Phone`、`InfraredRemote` 或 `Recorder`。按钮内容通过 `content` 传入，点击时调用 `onClick`。

位置和安全区由页面控制。例如，可以将 FAB 放在 `Box` 右下角并添加边距。父容器需要为投影留出空间。

## 使用自定义底色

需要使用自己的颜色时，将同一个颜色传给材质和阴影：

~~~kotlin
import androidx.compose.ui.graphics.Color

val color = Color(0xFF0080FF)
val renderer = remember { CouiFloatingRenderer.standalone() }
val density = LocalDensity.current.density
val contour = LocalCouiContourGlowEnabled.current

CouiFab(
    backdrop = backdrop,
    renderer = renderer,
    onClick = onClick,
    dark = dark,
    shape = CouiShape(999.dp, cornerWeight = 2f),
    material = CouiFabDefaults.material(color, dark),
    shadow = CouiFabDefaults.shadow(color, density, contour),
    content = content,
)
~~~

这段代码使用上一节的导入；`backdrop`、`onClick` 和 `content` 由页面提供。

## 图标和轮廓光

启用 M3 适配后，省略材质的 FAB 会向 M3 图标提供主题 `onAccent`。显式材质保持 ambient 内容色；预设或自定义底色可显式设置 `tint`。示例使用 `CouiShape` 创建圆形，也可以传入自己的 Shape。

`CouiFabColor` 和 `CouiFabDefaults` 保留显式的有色预设。使用自定义颜色时，请让材质与阴影共用同一最终颜色。

轮廓光由 `LocalCouiContourGlowEnabled` 控制；关闭后，`CouiFabDefaults.shadow` 使用普通有色投影。

## 版本和绘制空间

每个 FAB 使用独立的 `CouiFloatingRenderer.standalone()`，不要与其他绘制节点共用。

`backdrop` 应覆盖不透明的页面内容，FAB 放在源节点之外。背景源的创建方式见[背景采样](../effects/backdrop.md)。

源码见 [CouiFab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)、[配色](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
