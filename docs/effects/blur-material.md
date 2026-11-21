# 模糊与材质混色

模糊用于柔化后景，材质混色用于对采样后的背景执行两层颜色合成。两者都以[背景采样源](backdrop.md)为输入。

- **平台门槛**：
  - **均匀模糊**（`CouiBackdropEffectScope.blur`）：支持库最低系统（**API 31+**）。
  - **材质混色、顶栏渐进模糊、覆盖率归一化与菜单 Fast Kawase**（`rememberCouiMaterialEffect`、`drawCouiGradientBackdrop`、`CouiCoverageBlurEffect` 覆盖率恢复分支、`rememberCouiMenuBlur`）：要求 Android 13（**API 33**）及以上。
- **算法不可互换**：顶栏的**渐进 Gaussian**、菜单的 **Fast Kawase** 与普通**均匀模糊**是三种不同算法，视觉结果与参数语义均不等价。

## 为已有背景源添加材质

下面示例在 **API 33+** 的 Compose 内容中为已有 `backdrop` 添加两层材质混色；`firstColor`、`secondColor` 与 `shape` 由宿主传入，完整函数将 `modifier: Modifier = Modifier` 放在首位：

~~~kotlin
@RequiresApi(33)
@Composable
fun MaterialPanelExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    shape: Shape,
    firstColor: Color,
    secondColor: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    val material = rememberCouiMaterialEffect(firstColor, secondColor)
    Box(
        modifier
            .fillMaxWidth()
            .height(112.dp)
            .drawCouiBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = { renderEffect = material.asComposeRenderEffect() },
            ),
        content = content,
    )
}
~~~

- 仅需均匀模糊时，直接在 `effects` 中调用 `blur(radiusPx)`（示例见[背景采样](backdrop.md#一个源与一个面板)）。
- 需要**同时模糊和材质混色**时，须**先模糊背景、再叠加材质**（`RenderEffect.createChainEffect(material, blurEffect)`），并按模糊半径设置 `padding = ceil(blurPx)` 以保留模糊采样外沿；完整 `RenderEffect` 链见 [DemoVisualEffects.kt](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)。

## 选择正确入口

| 目标 | 公开入口 | 平台门槛 | 输入单位与坐标 | 算法与平台说明 |
| --- | --- | --- | --- | --- |
| 均匀背景模糊 | `Modifier.drawCouiBackdrop` + `CouiBackdropEffectScope.blur` | **API 31+** | `radius` 使用**本地 px**（可用 `dp.toPx()`） | 基于 Compose `BlurEffect`；调用 `blur` 会自动累加 `padding` |
| 顶栏渐进模糊 | 完整顶栏用 `TopAppBarSurface`；底层用 `Modifier.drawCouiGradientBackdrop` | **API 33+** | `fadeHeightPx`、`referenceBlurRadiusPx` 使用 **px**，位置与源坐标对齐 | 原厂渐进 Gaussian（先横后纵两遍原尺寸卷积）；不提供旧平台替代模糊 |
| 菜单快速模糊 | `rememberCouiMenuBlur(backdrop, background)` + `CouiMenuBlur.draw(...)` | **API 33+**（低于 33 返回 `null`） | `background` 为补底 `Color`。内部按设备像素的 16px 网格分块 | 使用 Fast Kawase，只输出模糊底图。菜单在底图恢复到面板尺寸后合成材质 |
| 已有覆盖率模糊链 | `CouiCoverageBlurEffect(radiusPx)` | **API 31+**（API 33+ 启用覆盖率恢复） | `radiusPx` 使用 **px**；提供 `padding` 与 `effect` | 平台模糊后在 API 33+ 恢复边缘样本颜色与覆盖度，避免屏幕边缘混入透明外沿导致顶边透出正文 |
| 两层材质混色 | `rememberCouiMaterialEffect(mix, blend, blendMode)` | **API 33+** | `mix`、`blend` 为 `Color`；`blendMode` 为 `CouiMaterialBlendMode` | 先执行 `luminosity(mix)`，再按 `Overlay`（默认）或 `ColorDodge` 混合 `blend`；弹窗 `Defaults` 按主题选模式 |

## 坐标、资源与生命周期

- **`drawCouiGradientBackdrop` 参数与节点资源**：
  - `fadeHeightPx` 与 `referenceBlurRadiusPx` 均使用 **px**；可选参数 `gradientSpeed`（须为有限正数，如顶栏传入 `3f`）控制是否使用原厂指数衰减曲线，省略时使用 `smoothstep`。
  - 返回的 `Modifier` 可复用到不同尺寸的节点，库会为每个节点独立持有横向、纵向 2 个中间 `GraphicsLayer` 与 2 个 `RuntimeShader`。参数或 `Density` 变更时自动更新本节点的 `renderer`，保留先横后纵卷积。
  - 渐进 `Shader` 在**节点首次绘制时创建**，异常直接暴露，不提供降级模糊。
- **`rememberCouiMenuBlur` 资源与调用链**：
  - 内部为调用节点创建并记住降采样与 Kawase 滤波所需的离屏 `GraphicsLayer` 与 `RuntimeShader`。降采样使用原厂网格和四点盒式滤波，1/8 档先执行半尺寸双线性采样。
  - 调用 `CouiMenuBlur.draw(scope, coordinates)` 时，`coordinates` 为消费者节点坐标，内部会通过 `localToScreen` 计算设备像素尺寸，并将 `background` 补底一并写入采样链（不能在材质混色之后再补底）。
- **`rememberCouiMaterialEffect` 参数快照**：
  - 随 `mix`、`blend` 与 `blendMode` 输入变化更新；由于 `RenderEffect` 持有的是 uniform 参数快照，在底层自定义绘制链中修改参数时必须重建相应 `RenderEffect`。
- **平台门槛与失败处理**：`RuntimeShader` 相关效果要求 **API 33+**，具体分支见[兼容要求](../guide/compatibility.md)；Shader 初始化与创建异常按[资源与失败处理](backdrop.md#资源与失败处理)处理。

## 公开入口

- **效果实现**：[CouiMaterial.kt](../../coui/src/main/java/com/itos/coui/effect/CouiMaterial.kt)、[CouiGradientBlur.kt](../../coui/src/main/java/com/itos/coui/effect/CouiGradientBlur.kt)、[CouiCoverageBlurEffect.kt](../../coui/src/main/java/com/itos/coui/effect/CouiCoverageBlurEffect.kt)、[CouiMenuBlur.kt](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)
- **完整控件接入**：[顶栏](../components/top-app-bar.md)、[菜单](../components/menu.md)
