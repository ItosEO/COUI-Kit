# Agent Note: 顶栏、底栏、FAB 与搜索的材质分支

Status: implemented

## Problem

同用背景源的控件有不同的原厂滤镜。顶栏将项目调校的半径或渐变用于默认预设，设置式搜索额外加了一层模糊，默认 FAB 的不透明色会遮住之前取得的模糊底图。两个搜索胶囊重叠时，后绘制的材质还需要读取同一节点中已绘制的下层胶囊。

## Decision

完整顶栏使用 AppBarBlurHelper 的固定像素半径和全高曲线。背景遮罩沿用[独立的平滑渐变](2026-10-18-coui-topbar-gradient-blur.md#decision)。低层 TopAppBarSurface 继续通过公开参数配置固定长度或全高模糊。浮动顶栏按钮和顶栏搜索的默认 Style 使用 ToolbarMaterialEffectResolver 的 TOP_BAR 材质，采样已完成渐进处理的背景。宿主显式使用的旧混色预设仍由 CouiTopAppBarButtonDefaults.material 提供。

底栏主体与无色附件使用 FRAMEWORK_BOTTOM_BAR。深浅色各用自己的两层滤镜。独立 FAB 和项目的右侧有色附件使用 FRAMEWORK_FAB。COUIFloatingButton 将默认主题色的 alpha 与显式 ColorStateList 分开处理，库同样只在默认主题色入口应用主题 alpha。项目右侧保留有色附件的装配及白色图标。

设置式搜索使用零半径 TOP_BAR 材质。与零半径顶栏按钮一样，背景采样和混色仍有可见输出。显式 backdrop 覆盖作用域默认源。搜索的转场前景模糊继续由前景内容层承载。共享绘制节点的双胶囊按现有顺序读取前面已绘制的图层，输出继续使用前景 SrcOver。这样可读取下层胶囊，又可避免输出矩形覆盖兄弟节点。

## Evidence

PJZ110 的 Contacts APK 中，AppBarBlurParamsExtKt 提供 30px 到 0 的渐进参数，GradientBackgroundDrawable 从底到顶生成线性节点。ToolbarMaterialEffectResolver、BaseBottomFloatingMenuView、COUIBottomFloatingAccessoryView、COUIFloatingButton 与 COUIMaterialBlurEffect 共同确定各材质分支。MaterialBlurEffect 调用平台 MIRROR Gaussian，然后顺序执行两次 BlendModeColorFilter。ThemeBlurEffectParams.getColorLayer1 仅对主题属性覆盖 alpha。COUISearchBar 的默认 mMaterialType 为 0，额外模糊和按进度切换材质各有单独的配置门控。便签和设置页面的原厂装配证据见[折叠顶栏与常驻搜索](../feature/2026-11-01-secondary-page-primitives.md)。顶栏的两遍渐进核依据见[渐进模糊](2026-10-18-coui-topbar-gradient-blur.md)。

## Alternatives considered

沿用项目调校可维持旧版本观感和浅色底栏的降亮效果。默认组件需要遵循各自的原厂预设，因此由组件使用原厂参数，显式样式继续由宿主选择。双胶囊若使用完整背景回填可读取下层颜色，但会改变透明祖先及背景接缝的合成。复用前景合成已有的采样前缀可以读取同节点的下层图层。

## Consequences

浮动表面仍以所属页面的公开背景源承载系统 Canvas 采样，双主体的采样前缀仅覆盖同一绘制节点中的既有录制图层。顶栏渐进核继续保留透明边缘覆盖率及固定抖动。原厂动态噪声、跨独立兄弟节点的整个 Canvas 和设备 GPU 输出需要在各自条件下对照。
