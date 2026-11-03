# Agent Note: 跳过不可见绘制并复用固定绘制指令

Status: implemented

## Problem

渐进模糊在透明中心仍运行最终输出为零的卷积。顶栏与菜单每次绘制重录只引用前驱图层的中间指令。未选中 Chip 的前景光晕输出透明，仍创建背景快照、采样和合成。复选框每帧创建不随动画变化的轮廓、勾线与 PathMeasure。菜单预选框在组合阶段读取位移动画。

这些是源码可确认的重复工作。它们是否支配整帧成本，仍需设备 trace，不能从代码数量或图层数推算应用整体收益。

## Decision

保留公开入口、原厂算法与项目特化，使用公开 Android / Compose 承载减少重复工作，不增加系统 OSDK、私有符号或厂商 SO 依赖：

1. [CouiGradientBlur](../../../../coui/src/main/java/com/itos/coui/effect/CouiGradientBlur.kt) 保留两遍、原分辨率、padding、整数半径、权重、中心余重、噪声及覆盖率处理。先保留小半径的原直通分支，再仅对中心 alpha 精确等于零的卷积分支返回透明；不截断低但非零的覆盖率。第一遍逐次录制动态背景，第二遍仅在尺寸或 LayoutDirection 改变时录制固定的前驱层引用；Density 改变由 remember 重建 renderer。
2. [CouiMenuBlur](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt) 保留原 Fast Kawase 的九轮滤镜和独立像素网格。只在采样网格、density、fontScale 或 LayoutDirection 改变时录制九层固定指令；输入层逐次录制。每个闭包捕获固定的前驱层，保存独立 Density 值，不长期持有易变 DrawScope 代理。
3. [CouiFloatingSurfaces](../../../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt) 仅在 foregroundComposition 分支跳过原 Shader 已定义为透明的 caustic。有效 alpha 使用与 uniform 相同的乘积和原阈值；[CouiCausticShader](../../../../coui/src/main/java/com/itos/coui/effect/CouiCausticShader.kt) 及完整 destination 回填分支保持。
4. [Checkbox](../../../../coui/src/main/java/com/itos/coui/checkbox/Checkbox.kt) 用 drawWithCache 缓存轮廓、Stroke、勾线、PathMeasure 和 segment。每次提取 segment 前 reset；颜色、状态与时间线仍在绘制阶段读取。Spacer 配合 onDrawBehind 保留原 Canvas 的布局与绘制位置，不改变方向动画、受控值或外层交互语义。
5. [Menu](../../../../coui/src/main/java/com/itos/coui/menu/Menu.kt) 的私有预选框使用 lambda offset，在 placement 阶段读取动画值；像素取整、弹簧和淡出逻辑保持。官方实现通过 placement layer 移动预选框，不要求每个位置帧重组。

这些缓存只复用对象或固定绘制指令，不缓存旧背景像素；滤镜执行遍数和节点各自持有 renderer 的要求保持。

## 官方与源码依据

对照基线为 e40c1c413b68fa43924ac6e48620ad3e9d703a08。原厂算法依据沿用[顶栏渐进模糊](../bug-fix/2026-10-18-coui-topbar-gradient-blur.md)和[菜单输入与合成层序](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)：PJZ110 顶栏经 GradientBlur 进入 OplusGradientGaussianBlurEffect，菜单使用 Fast Kawase。此次没有重新执行原始 OEM 库，已有研究记录不能算作新增原厂运行对照。

AndroidX 的 [GraphicsLayer 实现](https://android.googlesource.com/platform/frameworks/support/+/88e1988b6bf1969184b60bb87c92237a13fdf0b5/compose/ui/ui-graphics/src/androidMain/kotlin/androidx/compose/ui/graphics/layer/AndroidGraphicsLayer.android.kt)保存绘制闭包及环境，并在 display list 丢失后重新录制；[AOSP RenderNode](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/libs/hwui/RenderNode.cpp)遍历子节点、传播 damage 并更新 filter。另核对实际解析的 ui-graphics-android 1.13.0-alpha02 AAR 字节码：record 保存 Density、LayoutDirection 和 drawBlock，draw 调用 recreateDisplayListIfNeeded。固定 drawLayer 引用继续引用实时子层，不能据此省略输入层录制。

[官方绘制缓存说明](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers#drawwithcache)适用于复选框的尺寸相关几何；[官方延后状态读取说明](https://developer.android.com/develop/ui/compose/performance/bestpractices#defer-reads-as-long-as-possible)明确使用 lambda offset 将位置更新推迟到布局阶段。这两处复用现成机制，不建立新的缓存或动画框架。

## 已有决定审计

- [顶栏渐进模糊](../bug-fix/2026-10-18-coui-topbar-gradient-blur.md)、[顶栏前景与搜索](../bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)：部分重叠，互链。各自保留原算法、层序和已批准适配，本篇持有透明工作省略与固定指令复用的依据。
- [菜单输入与合成层序](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)、[ChipTab 装配](../feature/2026-10-24-chip-tab.md)、[持久动画通道](../architecture/2026-10-29-persistent-motion-channels.md)：部分重叠，互链。只涉及所消费绘制路径的缓存或状态读取阶段，不替代输入会话、滚动物理、方向动画和取消规则。
- 阴影移植、模块与 API 边界、主题与 Material3、弹窗与输入预设、Demo、开发流程和签名记录：无关决定，关键词命中不构成替代；它们持有的已有约束继续适用。此前阴影优化不计入本次成果。
- 没有完全吸收项或过时提案需要删除、拒绝或归档；本篇从 proposed 迁入 implemented，首次提出日期保持。

## Verification

Shader 对照直接提取基线与生产 AGSL，使用 Skia 144.1 的 EGL / Ganesh 执行；后端为 llvmpipe LLVM 20.1.2、Mesa 25.2.8 软件 OpenGL。CPU raster 路径不能可靠执行该版本 Shader，不计入有效结果。测试使用独立横纵中间图并等待提交完成，不能替代 Android HWUI、Compose 或手机 GPU 帧时。

| 检查 | 结果与范围 |
| --- | --- |
| 渐进模糊 RGBA8 / RGBA16F | 360×108 可见区域，整数核半径 1、2、12、22、32、43、80，八种图案；两种格式各 56/56、合计 112 组对照零差异，包含透明边缘、低 alpha 和 HDR 颜色输入。 |
| 顶栏尺寸 RGBA8 | 1080×276 可见区域、核半径 32；最终重复试验的八种图案全部零差异。 |
| caustic | 576 个案例严格透明，576 个跳过前后合成逐像素相等；36 个阈值上方阳性对照均产生非零效果。覆盖两种尺寸、RGBA8/RGBA16F、六种背景、两种 outlineType，以及分数位置与不等比缩放。 |
| 编译与 Lint | 核心库与 Material3 适配模块的 compileDebugKotlin 通过；核心 lintDebug 为 0 errors、18 warnings、3 hints。未将这些结果扩大为 Demo 构建或设备验证。 |
| API / ABI | 核心库基线与优化产物均生成 317 个 class，逐类 javap -public -s 输出一致，其中 191 个为 JVM public 类。五个修改文件的公开源码签名与默认值不变。Material3 未单独执行 ABI 对照。 |

顶栏场景覆盖 720×184 / 核半径 22 / fade 192，以及 1080×276 / 核半径 32 / fade 288；完整中间图分别保留每边 45、65 像素 padding。screen 输入使屏幕顶边及两侧外沿透明，下方页面继续有内容。预热 8 轮，计时顺序交替，使用相同四个输入轮换；表中为两遍 Shader 任务的中位数，下降比例按 1 - 优化中位数 / 基线中位数计算：

| 输入与样本数 | 基线 | 优化 | 结果 |
| --- | --- | --- | --- |
| 720×184 screen，48 个计时样本 | 14.114952 ms | 9.646394 ms | 下降 31.7% |
| 1080×276 screen，48 个计时样本 | 40.6516815 ms | 26.173254 ms | 下降 35.6% |
| 1080×276 screen 独立重复，64 个计时样本 | 38.73602 ms | 23.800349 ms | 下降 38.6% |
| 1080×276 完全不透明控制，48 个计时样本 | 39.36155 ms | 36.6768175 ms | 未检出明显回退，不认定稳定收益 |

结果支持透明外沿场景下省略无输出卷积的收益，也显示它不是所有输入都有相同比例的优化。完全不透明控制的配对改善中位数仅 1.14%，与两组总体中位数之比不是同一个统计量；不将其写成稳定性能提升。固定层录制、复选框分配和重组变化没有纳入上述 Shader 计时，不能累加为整帧收益。验证附件由性能报告保存，包括 benchmark-summary.json、gradient-gl-720-final.json、gradient-gl-final-1080.json、gradient-gl-final-repeat.json、gradient-gl-opaque-final.json、gradient-f16-correctness.json、caustic-correctness.json 与 optimized-verification.json。

## Alternatives considered

- **复用平台 createBlurEffect 或 AndroidX 渐进模糊。** 接口成熟且维护量小，但位置半径场、采样网格、sigma、边界、中心权重及 alpha 语义不能直接等同于现有路径，不能以替换算法兑现指标。
- **裁去固定外沿 crop。** 可直接减少处理面积，离线样本的可见区域可以相同；但还要维护横纵依赖、双线性保护带和变换坐标，外沿也未必透明。与透明中心短路叠加的宿主实验没有额外收益，因此保留原完整范围，使用不依赖几何假设的精确零 alpha 条件。
- **Gaussian 权重递推 recurrence。** 可减少指数计算，但真实 Skia 对照出现 RGBA8 单通道 1/255 差异；大自定义半径还需额外精度证明。保留原权重计算，不用少量像素变化交换性能。
- **降采样或合并滤镜遍数。** 可减少像素和中间图成本，但改变已确认的原分辨率卷积、逐轮采样网格或量化，不能保持当前效果。
- **无跟手光时删除菜单内容 Offscreen。** 能省一层隔离，但公开 content 可使用 Clear、DstIn、DstOut 等混合操作；删除图层会改变它们的 destination。正常 Demo 像素一致不足以证明 API 语义不变，保留隔离。
- **为浮动材质增加全局依赖缓存。** 可能进一步减少 RenderEffect 创建，但融合依赖跨主体，失效条件与持有关系复杂；当前局部省略不需要新增缓存框架。只有设备 trace 确认这一环节支配成本后才重访。

## Consequences

窗口源的显式按需离屏策略见[菜单背景使用期](../architecture/2026-11-01-menu-backdrop-isolation-lifetime.md)。该策略保留源录制，并在材质消费期间恢复原尺寸离屏；本篇的卷积、指令复用与菜单内容隔离决定保持，收益分别验证。

在已量测的 720 与 1080 宽透明外沿宿主 Shader 场景，开销下降 31.7%～38.6%，进入 30%～50% 目标区间。图层复用同时减少重复录制，复选框使用官方缓存机制；代价是需要维护几何/环境失效键和少量常驻绘制对象。滤镜本身的执行遍数、全尺寸纹理成本和融合材质依赖仍存在，不能宣称项目整体已降低同等比例。

没有 Android 真机、硬件 GPU 或设备帧时数据。后台恢复、trim memory、窗口变换、Density/字体/RTL 切换和真实重组次数仍需运行验证；官方 GraphicsLayer 恢复机制与实际 AAR 核对提供实现依据，不代替九层链的生命周期测试。完整不透明场景收益较小，后续优先用设备 trace 确认剩余主线程录制、RenderThread、GPU 或背景源成本，再决定是否扩展缓存。

公开参数、回调、平台门槛、背景源装配、坐标和 renderer 生命周期均保持；[模糊与材质](../../../../docs/effects/blur-material.md)、[背景采样](../../../../docs/effects/backdrop.md)、[复选框](../../../../docs/components/checkbox.md)、[菜单](../../../../docs/components/menu.md)及相关组件使用页无需迁移或修改。性能验证记录由本篇与报告持有，不将实验数据写成普通使用文档的新保证。
