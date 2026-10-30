# ➕FAB

`CouiFab` 适合放置新建、编辑等页面操作，可以单独使用，也可以与底栏一起使用。

## 显示一个 FAB

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

FAB 不会修改图标颜色，使用图标时请根据底色设置 `tint`。示例使用 `CouiShape` 创建圆形，也可以传入自己的 Shape。

轮廓光由 `LocalCouiContourGlowEnabled` 控制；关闭后，`CouiFabDefaults.shadow` 使用普通有色投影。

## 版本和绘制空间

每个 FAB 使用独立的 `CouiFloatingRenderer.standalone()`，不要与其他绘制节点共用。

`backdrop` 应覆盖不透明的页面内容，FAB 放在源节点之外。背景源的创建方式见[背景采样](../effects/backdrop.md)。

源码见 [CouiFab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)、[配色](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
