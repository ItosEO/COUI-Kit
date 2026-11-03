# 浮动表面

需要组合背景材质、普通投影、caustic、轮廓与内容时，使用浮动表面接口。现成装配可从[顶栏按钮](../components/top-app-bar.md)、[底栏](../components/bottom-bar.md)和 [FAB](../components/fab.md)进入。

## 单主体装配

下面片段位于 API 34 及以上的 Compose 内容中。backdrop 来自不透明 SDR 页面；material、shadow、shape 为宿主外观；contentLayer 是节点持有并录制实际前景内容的 GraphicsLayer；surface 返回该节点当前几何。

~~~kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.couiFloatingSurface

val renderer = remember { CouiFloatingRenderer.standalone() }
val surfaceModifier = Modifier.couiFloatingSurface(
    backdrop = backdrop,
    renderer = renderer,
    material = material,
    shadow = shadow,
    shape = shape,
    contentLayer = contentLayer,
    contourGlowEnabled = contourGlowEnabled,
    surface = surface,
)
~~~

将 surfaceModifier 挂到负责绘制的节点。宿主负责内容层的录制、当前尺寸与变换，完整单主体装配见 [Fab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)。不要将空的 GraphicsLayer 当作已录制内容。

此 Modifier 绑定传入的 renderer、内容层和当前坐标，只能挂到该节点。第二个节点须单独装配；可复用 material、shadow 等不可变配置。它不改变测量，内容路径、投影溢出和材质 patch 各按原有层序绘制，不能再用外层 clip 一并裁掉投影。

## 选择主体数量

| renderer 工厂 | Modifier | 用途 |
| --- | --- | --- |
| `standalone()` | `couiFloatingSurface` | 独立按钮或 FAB |
| `pair()` | `couiFloatingPair` | 两个相互融合的表面，例如搜索与取消 |
| `CouiFloatingRenderer()` | `couiFloatingSurfaces` | 固定三个主体，例如底栏与两侧附件 |

不要用虚构的空主体凑数量。各配置列表与 `surfaces` 返回值须具有相同数量和顺序。双主体的 `material.index` 依次为 1、2；三主体的菜单为 1，两侧附件为 2。

双主体的 `drawOrder` 使用列表下标 0、1，只改变同一 Z 分组内的顺序。例如 `listOf(1, 0)` 先画第二项、后画第一项；它与 `material.index` 的编号不同。

## 几何与绘制

`CouiFloatingSurface.bounds` 使用当前绘制节点中的未缩放像素坐标。缩放单独通过 `scaleX` / `scaleY` 传入，`alpha` 与 `visible` 表达主体状态。宿主负责窗口或布局坐标到节点坐标的转换，并让内容录制与几何对应同一帧。

`CouiFloatingShadow` 的长度使用像素；material 字段的单位以公开声明为准，带 Dp 后缀的参数使用 dp。固定预设从所属组件 Defaults 获取。

单主体和双主体支持 `foregroundComposition`。启用时，材质与光晕只输出前景，以 SrcOver 叠到已绘制页面；Backdrop 必须覆盖不透明 SDR 页面。三主体保留完整 destination 合成。

`beforeContent` 在对应材质之后、内容之前绘制。双主体回调另接收列表下标。回调坐标属于当前绘制节点，缓存 Path 描述未缩放主体的局部轮廓；调用方负责位移、缩放和裁剪，不得修改该 Path。完整双主体装配见 [SearchBar](../../coui/src/main/java/com/itos/coui/search/SearchBar.kt)。

## 资源与错误

每个独立绘制节点持有自己的 renderer、内容层和采样坐标，不跨节点共享可变资源。material、shadow 等不可变配置可以复用。主题、轮廓、几何和位置变化应更新当前节点，录制层随节点释放。

平台门槛及创建失败的处理见[背景采样](backdrop.md#资源与失败处理)。

## 公开入口

[renderer、配置与 Modifier](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt) · [几何](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingGeometry.kt)

只需要同色背景及内容遮罩时，参见[横向双主体融合](metaball.md)。
