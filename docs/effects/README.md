# 效果目录

只需要背景采样、模糊、材质、投影、光效或运动等底层绘制与交互能力时，可以直接接入这里的公开入口。需要完整控件时，先看[组件目录](../components/README.md)。

## 能力总览

| 效果 | 公开入口（便捷入口 / 底层接口） | 平台门槛 | 用途与边界 |
| --- | --- | --- | --- |
| [背景采样](backdrop.md) | `rememberCouiBackdrop` / `couiBackdrop` / `drawCouiBackdrop` / `ProvideCouiBackdrop` / `ProvideCouiMenuBackdrop` | **API 31+**（配合 Shader 效果时按对应门槛） | 录制所属窗口或正文内容，供兄弟节点与独立浮层窗口采样；严禁自采样 |
| [普通阴影](shadow.md) | `Modifier.couiShadow` / `CouiShadowRenderer` / `CouiShadowDefaults` | **API 34+**（API 35+ 使用公开 `Canvas.clipShader`） | 不依赖背景源的普通 Mesh 投影；不包含材质、模糊或 caustic 外光晕 |
| [模糊与材质混色](blur-material.md) | `CouiBackdropEffectScope.blur` / `drawCouiGradientBackdrop` / `rememberCouiMenuBlur` / `CouiCoverageBlurEffect` / `rememberCouiMaterialEffect` | 均匀模糊 **API 31+**；渐进模糊、材质混色、覆盖率归一化与菜单 Fast Kawase **API 33+** | 均匀背景模糊、顶栏渐进高斯模糊、菜单快速模糊与两层材质混色；不同算法不可互换 |
| [浮动表面](floating-surfaces.md) | `couiFloatingSurface` / `couiFloatingPair` / `couiFloatingSurfaces` / `CouiFloatingRenderer` | **API 34+** | 单通道组合背景材质、普通投影、caustic 外光晕、轮廓内晕与前景内容（支持 1/2/3 主体） |
| [轮廓、跟手光与状态反馈](lighting.md) | `CouiContour` / `CouiEdge` / `rememberCouiSpotlight` / `couiNavigationFeedback` / `couiFabFeedback` / `couiToolbarFeedback` / `couiButtonFeedback` | `CouiContour`、`CouiEdge`、`rememberCouiSpotlight` **API 33+**；状态反馈 Modifier **API 31+** | 独立白光轮廓、背景感知描边、触摸跟手光与 `hover`/`focus`/`press` 蒙层 |
| [横向双主体融合](metaball.md) | `CouiMetaballRenderer` / `CouiMetaballBody` | **API 33+** | 同色横向双主体 CONIC 融合背景及 `BlendMode.DstIn` 内容遮罩；不采样背景 |
| [运动与跟随回弹](motion.md) | `rememberCouiMetaballFollowingTranslation` / `rememberCouiTouchMotion` / `couiTouchMotion` / `rememberCouiPressMotion` / `couiPressMotion` | **API 31+** | 双主体跟随回弹、触摸形变拖拽与按压缩放；只输出只读状态，不接管业务点击 |
| [边界回弹](overscroll.md) | `couiVerticalScroll` / `rememberCouiOverscrollEffect` / `CouiOverscrollEffect` | **API 31+** | COUI 纵向惯性及长、短、空内容的边缘阻尼回弹与触觉反馈 |

## 接入前核对

- **是否需要背景源**：`drawCouiBackdrop`、`blur-material.md` 与 `floating-surfaces.md` 需要由兄弟节点或外层窗口提供的 `CouiBackdrop`；`couiShadow`、`CouiContour`、`rememberCouiSpotlight` 与 `CouiMetaballRenderer` 不采样背景。
- **逐节点持有可变资源**：`CouiFloatingRenderer`、`CouiShadowRenderer`、`CouiContour`、`CouiEdge`、`CouiMetaballRenderer`、`CouiMenuBlur` 与 `CouiOverscrollEffect` 均需按绘制节点独立创建与持有，禁止跨节点共享。
- **延伸参考**：运行版本与依赖对齐见[兼容要求](../guide/compatibility.md)，`CouiShape` 与组件 `Defaults` 见[外观与预设](../guide/appearance.md)。
