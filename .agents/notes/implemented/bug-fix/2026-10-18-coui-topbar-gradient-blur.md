# Agent Note: 顶栏渐进 Gaussian 与采样边界

Status: implemented

## Problem

顶栏曾使用多级 Kawase atlas 放大混合，出现粗网格。菜单使用 Fast Kawase 不能证明顶栏也使用该算法；没有采样器证据时，也不能把问题归因于 nearest filtering。

## Decision

CouiTopAppBar 在相应平台通过 CouiGradientBlur 移植位置可变 Gaussian。横、纵两遍分别记录到原尺寸 GraphicsLayer，前一遍栅格化后再进入后一遍。保留整数核、对称权重、中心剩余权重和末端返回，不改成均匀模糊与原图混合。不进行 Bitmap 回读，也不在低分辨率图层重绘矢量源。

独立 Density 用于采样录制，不能让 DrawScope 成为自己的密度代理。背景源、坐标和裁剪继续复用 CouiBackdrop；渐进输出排除标题与按钮，按钮采样该输出并通过 parent 补足源区域外底图。

以下是已有授权适配：固定逻辑渐隐长度并锚定下沿，折叠只平移；位置使用 smoothstep；最大强度按 density 装配；保留页面颜色，不叠加原厂独立主题渐变；两遍使用固定噪声种子；透明边界恢复有效样本颜色但不扩张中心 alpha。旧平台的 Gaussian/遮罩分支继续存在，不称为原厂路径等价。

## Evidence

来源为原宿主的同名 Note 与顶部栏渐进模糊契约。原厂基准是 PJZ110 Contacts、OppoNote2、Calendar 的 AppBarBlurHelper.applyGradientBlurRenderEffect，经 framework 的 GradientBlur 进入 libhwui 的 OplusGradientGaussianBlurEffect。对应起止强度不同分支先横后纵，两遍共用竖向半径场，并绕过普通 Gaussian 的降采样分支。原厂独立背景 Drawable 的 positions/alphas 不是卷积核。

此证据仅限定该材料与分支，不证明任意画布矩阵或厂商缓存始终为屏幕原分辨率。原材料不作为本仓库运行依赖。当前算法与参数位置见 [CouiGradientBlur](../../../../coui/src/main/java/com/itos/coui/effect/CouiGradientBlur.kt)。

## Alternatives considered

- **提高 atlas 分辨率或补滤波。** 改动少，但保留没有原厂顶栏依据的多级近似。
- **平台均匀 Gaussian 后渐变混合。** 实现简单，原厂也有备用分支，但不是已确认的默认位置可变核路径。
- **反射厂商 RenderEffect。** 可以复用其执行器，但不满足跨品牌要求。

## Consequences

透明中心的工作省略与固定第二遍指令复用见[绘制工作省略](../simplification/2026-11-01-render-work-elision.md)；本篇的核、完整采样范围和既有视觉适配保持。

移除粗网格放大来源，保留原尺寸卷积的采样成本。高 density 的强度适配会扩大卷积核和外沿，不能以图层减少宣称更快。

原工程曾用生产与原片段离线 Skia 对照核、边界、顺序和锚点，并检查曲线与方差；这是历史证据，不是本仓库的 Android GPU 验收。本次迁入不改 Shader。当前公开承载的分配、精度、噪声时间行为及既有强度适配仍不能表述为原厂逐像素等价。
