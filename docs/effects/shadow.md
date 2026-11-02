# 普通阴影

给卡片或胶囊添加普通投影时，从 Modifier.couiShadow 进入；它不依赖背景源，也不包含材质与 caustic 外光晕。

## 最小普通阴影

下面使用便捷入口，不需要背景采样。示例没有应用文案，可直接放入支持 API 34 的 Compose 内容。宿主在进入此函数前检查版本；父级需要为阴影保留绘制空间，不能沿主体边界裁剪。

```kotlin
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiShadowDefaults
import com.itos.coui.effect.couiShadow
import com.itos.coui.shape.CouiShape

@RequiresApi(34)
@Composable
fun ShadowSample(modifier: Modifier = Modifier, dark: Boolean) {
    val radius = 24.dp
    Box(
        modifier.padding(32.dp).size(160.dp, 72.dp)
            .couiShadow(style = CouiShadowDefaults.menu(dark), radius = radius)
            .background(
                color = if (dark) Color.DarkGray else Color.White,
                shape = CouiShape(radius, cornerWeight = 2f),
            ),
    )
}
```

便捷阴影不改变布局，长度使用 dp；窗口坐标、节点 alpha、轴向缩放和多主体请直接使用 `CouiShadowRenderer`。普通 RoundRect 不支持 inverse clipping，不能开启对应参数。完整低层示例见 [DemoShadowSamples](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)。

## 自定义绘制

CouiShadowStyle 的长度使用 dp。便捷入口完成本地坐标和像素换算，不改变布局尺寸；父级变换作用于主体和投影整体。radius 省略时使用短边的一半。

同一个 `couiShadow` Modifier 可用于多个节点，每个节点各持有 renderer 和缓存槽。尺寸变化更新本节点的网格，不重建 Shader。阴影画在内容后方；将主体的 clip 放在阴影内侧，外层 padding 用来给阴影留出空间。

CouiShadowOutline 提供公开轮廓选项；普通 RoundRect 不支持 inverse clipping。使用 SmoothCapsule 时，内容形状和投影几何需要按同一主体装配，示例见 DemoShadowSamples。

需要窗口坐标、节点 alpha、轴向缩放或多主体槽位时，直接持有 CouiShadowRenderer，并调用其 Canvas 绘制接口；不要把已经缩放的网格再次整体缩放。完整参数见[公开声明](../../coui/src/main/java/com/itos/coui/effect/CouiShadowRenderer.kt)。

## 平台与入口

普通 Mesh 阴影要求 API 34；fast 胶囊裁剪的承载分支见[兼容要求](../guide/compatibility.md)。构造失败交给宿主处理，父级裁剪与离屏透明度边界仍会影响可见阴影。

[便捷入口与 Defaults](../../coui/src/main/java/com/itos/coui/effect/CouiShadow.kt)、[低层 renderer / outline](../../coui/src/main/java/com/itos/coui/effect/CouiShadowRenderer.kt)。原厂网格与移植方法供开发者阅读[复刻指南](../development/replication-guide.md#32-底栏与-fab先分主体再分效果)。
