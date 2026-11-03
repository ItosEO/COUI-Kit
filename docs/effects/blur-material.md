# 模糊与材质混色

需要柔化后景时使用模糊，需要调整采样颜色时叠加材质效果。两者的输入仍来自[背景采样源](backdrop.md)。

## 为已有背景源添加材质

下面片段位于 API 33 及以上的 Compose 内容中。backdrop 为已有源，shape 是宿主 Shape，firstColor 和 secondColor 为材质的两次混色输入。

~~~kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.drawCouiBackdrop
import com.itos.coui.effect.rememberCouiMaterialEffect

val material = rememberCouiMaterialEffect(firstColor, secondColor)
Box(
    Modifier.fillMaxWidth().height(112.dp).drawCouiBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = { renderEffect = material.asComposeRenderEffect() },
    ),
)
~~~

均匀模糊可直接使用 CouiBackdropEffectScope.blur，示例见[背景采样](backdrop.md)。需要同时模糊和混色时，先处理采样模糊，再处理材质，保留模糊采样外沿 padding；完整 RenderEffect 链见 [DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)。

## 选择正确入口

| 目标       | 入口与行为                                              |
|----------|----------------------------------------------------|
| 均匀背景模糊   | drawCouiBackdrop 的 effects / blur，半径使用本地像素         |
| 顶栏渐进模糊   | TopAppBar；低层 drawCouiGradientBackdrop 使用位置可变卷积 |
| 菜单快速模糊   | rememberCouiMenuBlur / CouiMenuBlur.draw，由工厂管理中间图层 |
| 已有覆盖率模糊链 | CouiCoverageBlurEffect                             |
| 材质混色     | rememberCouiMaterialEffect，输入为采样颜色合成所需的两种颜色        |

顶栏的渐进 Gaussian、菜单 Fast Kawase 和均匀模糊是不同算法。菜单工厂在旧平台返回 null，由菜单自己的兼容路径处理；不能将它当作同等 Fast Kawase 结果。

## 坐标、资源与平台

低层渐进接口的长度使用像素，位置与源坐标保持一致。`rememberCouiMaterialEffect` 的 `blendMode` 默认为 `Overlay`，也可选择 `ColorDodge`；弹窗 Defaults 按主题选择模式。两种模式均先执行 luminosity，再以 blend alpha 混合第二层。材质工厂随颜色和模式输入更新；RenderEffect 是参数快照。

`drawCouiGradientBackdrop` 可复用到不同尺寸的节点。每个节点拥有独立的横向、纵向图层和 Shader，参数或 Density 改变后更新本节点的 renderer，保留先横后纵的卷积。渐进 Shader 在节点首次绘制时创建；异常直接暴露，不提供替代模糊。

RuntimeShader 的相关效果要求 API 33，具体分支见[兼容要求](../guide/compatibility.md)。Shader 初始化失败按[资源规则](backdrop.md#资源与失败处理)报告。

## 公开入口

[材质](../../coui/src/main/java/com/itos/coui/effect/CouiMaterial.kt)、[渐进模糊](../../coui/src/main/java/com/itos/coui/effect/CouiGradientBlur.kt)、[覆盖率模糊](../../coui/src/main/java/com/itos/coui/effect/CouiCoverageBlurEffect.kt)、[菜单模糊工厂](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)。需要完整控件时使用[顶栏](../components/top-app-bar.md)或[菜单](../components/menu.md)。
