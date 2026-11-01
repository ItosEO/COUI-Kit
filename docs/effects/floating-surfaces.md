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

将 surfaceModifier 挂到负责绘制的节点。宿主负责内容层的录制、当前尺寸与变换，完整单主体装配见 [CouiFab](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)。不要将空的 GraphicsLayer 当作已录制内容。

## 单主体与三主体

- CouiFloatingRenderer.standalone() 配合 couiFloatingSurface，仅创建单主体链路。
- CouiFloatingRenderer() 配合 couiFloatingSurfaces，固定三个主体；各列表数量与主体顺序一致。material 的 index 分别对应中间菜单与两侧附件，规则见公开声明。
- 不跨节点共享 renderer，也不把一个单主体塞入三主体入口来代替独立按钮。

CouiFloatingSurface 的 bounds 使用当前绘制节点中的未缩放像素坐标，scaleX / scaleY 单独传入，alpha 与 visible 表达当前主体状态。宿主负责把窗口或布局坐标映射到该节点，不把缩放预先写入 bounds。

CouiFloatingShadow 的长度是像素；material 字段的单位按声明区分，带 Dp 后缀的参数不能与像素混用。预设优先从组件 Defaults 获取。

## 合成与生命周期

foregroundComposition 只在单主体入口提供，控制材质和光晕的前景表达。beforeContent 在材质之后、内容之前执行，接收只读使用的缓存裁剪 Path；坐标仍为节点本地坐标，变换与裁剪归调用方。

内容录制、背景采样与效果节点分层。主题、轮廓状态、几何和位置变化应进入当前绘制，资源随节点管理；错误与平台检查见[背景采样](backdrop.md#资源与失败处理)。

## 公开入口

[renderer、配置与 Modifier](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt)、[CouiFloatingSurface](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingGeometry.kt)。横向同色双主体融合是[另一条公开算法](metaball.md)，不替代本页的多层材质。

## 双主体与绘制阶段

搜索与取消使用 CouiFloatingRenderer.pair() 及 Modifier.couiFloatingPair。materials、shadows、shapes、contentLayers 均为两项，材质 index 按 1、2 排列；drawOrder 是这两个下标的排列，仅改变同高绘制顺序。surfaces 返回同一帧的两个节点，几何与内容必须一致。beforeContent 在各材质之后、对应内容之前执行，接收节点下标与缓存裁剪 Path；不得修改 Path。foregroundComposition 只输出前景，以 SrcOver 叠到已绘制页面，要求不透明 SDR 来源；默认仍保留完整 destination 合成。

这条入口复用现有融合和光效，不增加假节点。每个独立绘制组件持有自己的 renderer 和内容层；不得跨搜索实例共享可变资源。
