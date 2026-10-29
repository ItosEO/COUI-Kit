# ➕FAB

CouiFab 是独立浮动操作按钮，使用单主体材质和投影，不依赖底栏。适合新建、编辑这类页面级动作。

## 显示一个 FAB

使用以下代码显示一个固定配色的 FAB：

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

固定配色有 `Notes`、`Phone`、`InfraredRemote`、`Recorder` 四种，深浅色下的具体颜色由 `CouiFabDefaults.containerColor` 决定。

FAB 不处理页面位置和安全区。放到 Box 的右下角，再按页面需要加边距；父级要为投影留出绘制空间，不要沿 FAB 边界裁剪。

## 使用自定义底色

想用页面主题色或业务色，自己算一个最终颜色，然后同时传给材质和阴影：

~~~kotlin
import androidx.compose.ui.graphics.Color

val color = Color(0xFF0080FF)
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

不要在材质和阴影里分别重新取色，两边颜色不一致时投影、高光和内容会看起来不是同一个按钮。

## 图标和轮廓光

图标不会重新着色；深色底上要自己传 `tint`。`CouiShape` 的半径会按最短边收敛，用很大的半径就能得到圆形；也可以换成自己的 Shape。

轮廓光通过 `LocalCouiContourGlowEnabled` 控制。关闭时 `CouiFabDefaults.shadow` 会改用普通有色投影。

## 版本和绘制空间

FAB 要求 API 34。每个 FAB 单独持有一个 `CouiFloatingRenderer.standalone()`，不要和顶栏按钮、底栏或其他 FAB 共用。

`backdrop` 必须覆盖不透明页面内容，FAB 放在源子树外。透明页面或只采样局部背景时，材质会缺底。

源码见 [CouiFab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)、[配色](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)，完整页面见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
