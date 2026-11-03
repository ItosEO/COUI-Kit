# Agent Note: 菜单窗口背景的按需离屏隔离

Status: implemented

## Problem

ProvideCouiMenuBackdrop 的源覆盖所属窗口内容。若它始终使用 Offscreen，即使菜单和材质弹窗均未显示，连续页面更新仍经过这层离屏隔离。宿主还可能为顶栏和底栏保留另一层页面源，因此菜单关闭不能消除外层窗口源的合成工作。源码可以确认这条额外路径，但不能据图层数量推算整帧 GPU 成本。

直接删除窗口源会影响首次打开的录制时序、布局坐标和跨窗口更新；将所有源改为 Auto 则会改变任意内容的混合隔离，以及变换下的栅格化方式。

## Decision

[ProvideCouiMenuBackdrop](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuBackdrop.kt) 保留单参数入口和默认强制隔离，增加必须显式传入 isolateContent 的重载。false 仅允许源在没有材质消费者时使用 Auto，普通 rememberCouiBackdrop 的策略保持不变。现有调用无需迁移，新选项只用于宿主已核对的场景。

[CouiBackdrop](../../../../coui/src/main/java/com/itos/coui/backdrop/CouiBackdrop.kt) 持有空闲策略和消费者计数，沿已有 parent 链成对保留及释放。internal 组合入口 RetainWhileComposed 集中持有 DisposableEffect，以实际源身份为 key；retain、release 为 private，消费者不能拆开调用引用配对。策略更新统一考虑空闲隔离与活跃消费者；Provider 的 SideEffect 不能覆盖已经取得的引用。计数属于图层使用期，不作为 Compose 业务状态，也不建立额外租约对象或全局登记表。

[菜单材质](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuMaterial.kt) 在原材质组合位置调用 RetainWhileComposed。普通菜单和主、子菜单的材质在 expanded 或 motion.visible 为真时存在，退出动画结束后才释放，快速反向保持同一使用期。[材质弹窗](../../../../coui/src/main/java/com/itos/coui/dialog/CouiDialogWindow.kt) 在需要背景材质时，经平台及非空检查后调用同一入口持有实际窗口源；无材质弹窗不申请引用。显式传入的普通背景源仍保持原默认隔离。

源节点、clip、recorded、布局坐标、PreDraw 和 drawSource 录制继续存在。此决定只控制空闲源是否强制离屏，不删除采样节点、不冻结背景、不缩小输入，也不改变 Gaussian、Kawase 或菜单内容层的隔离。新菜单消费现有源，避免按需添加 source 带来的首帧重建。节点 detach 不清零消费者计数；即使图层已释放，消费者仍须偿还父链引用。诊断重建按 backdrop identity 迁移 Effect。

接入优先限制在不透明、静态页面根。Clear、DstIn 等目标混合仍须由所属组件隔离；透明窗口、移动的 BottomSheet、缩放或分数位移容器保留默认策略，另行验证后才能选择 false。公开使用要求由[菜单](../../../../docs/components/menu.md#空闲时按需离屏)和[背景采样](../../../../docs/effects/backdrop.md#菜单窗口的离屏策略)维护。

## Evidence

AndroidX 的 [GraphicsLayer](https://github.com/androidx/androidx/blob/918659e0a6ddfad64eb556fa2b6adf75844cba27/compose/ui/ui-graphics/src/androidMain/kotlin/androidx/compose/ui/graphics/layer/AndroidGraphicsLayer.android.kt) 将 compositingStrategy 交给平台实现；[GraphicsLayerV29](https://github.com/androidx/androidx/blob/918659e0a6ddfad64eb556fa2b6adf75844cba27/compose/ui/ui-graphics/src/androidMain/kotlin/androidx/compose/ui/graphics/layer/GraphicsLayerV29.android.kt) 通过 RenderNode 的合成层开关表达 Offscreen 与 Auto，并保留其他效果要求离屏的条件。因此 Auto 只取消强制隔离，不是绝对无离屏保证。

菜单 Popup 与材质使用相同的可见条件，见 [Menu](../../../../coui/src/main/java/com/itos/coui/menu/Menu.kt) 和 [CouiMenuSubMenu](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuSubMenu.kt)。[CouiMenuMotion](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuMotion.kt) 在关闭动画结束后清理 visible。AndroidX [Dialog](https://github.com/androidx/androidx/blob/918659e0a6ddfad64eb556fa2b6adf75844cba27/compose/ui/ui/src/androidMain/kotlin/androidx/compose/ui/window/AndroidDialog.android.kt) 在退出时 dismiss 并释放内容 Composition，不为系统窗口退出动画额外增加固定延迟。

本决定属于公开 Android / Compose 的资源管理，不是新移植的 OEM 算法，也不引用厂商私有 SDK。此前透明卷积和固定绘制指令优化的收益仍归原决定，不与本次源隔离调整混算。

## 已有决定审计

按 CouiBackdrop、ProvideCouiMenuBackdrop、离屏、背景源、采样和 render-work-elision 检索活跃 Note，排除 archived，命中项处理如下：

- [绘制工作省略](../simplification/2026-11-01-render-work-elision.md)：部分重叠，互链。它持有透明卷积和固定指令复用；本篇只调整显式选择按需策略的窗口源，不推翻其保留菜单内容隔离的理由。
- [菜单输入会话与合成层序](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)：部分重叠，互链。消费期间仍先按原尺寸栅格化源，菜单层序、输入会话、退出和独立 Density 规则保持。
- [弹窗窗口与输入](../feature/2026-10-24-coui-alert-dialog.md)、[顶栏渐进模糊](../bug-fix/2026-10-18-coui-topbar-gradient-blur.md)、[顶栏搜索与前景](../bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)、[原厂阴影](../bug-fix/2026-10-18-coui-native-shadow.md)：无关决定。它们持有各自算法、窗口或组件行为；新增源引用管理不替代这些决定。
- [ChipTab](../feature/2026-10-24-chip-tab.md)、[滑条](../feature/2026-10-21-coui-slider.md)、[开关](../feature/2026-10-27-coui-switch.md)、[持久动画通道](2026-10-29-persistent-motion-channels.md)：无关决定。命中涉及局部材质、其他采样或已有性能 Note 引用，组件算法和时钟不变。
- [Demo 侧滑](../feature/2026-10-24-demo-record-swipe.md)、[M3 展示装配](../feature/2026-10-23-demo-material-and-gallery.md)、[Demo 展示范围](../feature/2026-10-24-demo-focused-showcase.md)、[独立工程](2026-10-23-standalone-coui-demo.md)：无关决定。Demo 内容、局部遮罩和采样装配保持默认行为。
- [API 收口](2026-10-23-public-api-boundary.md)、[模块职责](2026-10-21-coui-module-boundary.md)、[文档受众](../process/2026-10-25-documentation-audiences.md)：无关决定。源使用期的组合入口保持 internal，引用配对实现为 private；新增公开策略有真实宿主场景及普通使用文档，不改变这些职责划分。

没有完全吸收项或过时提案需要删除、拒绝或归档。

## Verification

组合期配对收回成员后，两个调用点的条件、位置和 Effect key 保持；离线执行 core 与 Material3 模块的 compileDebugKotlin、core 的 lintDebug 均通过，Lint 为 0 errors、18 warnings、3 hints。构建日志保留 Gradle 转换缓存工作区内容变更的警告，未据此扩展构建或清理缓存。该结果不证明 Android 绘制、窗口时序或设备收益。独立源码审查覆盖父链引用、多个消费者、普通和两级菜单退出、材质弹窗、Effect identity 迁移及诊断重建，没有发现阻断问题。

本次重新对实际 Debug 编译产物执行 javap 对照：相对任务前 main，core 没有缺失的原 JVM public 类、方法 descriptor 或变化的类头；原 Provider 入口保留，新增 Boolean 重载。coui-material3 的类与方法逐签名一致。RetainWhileComposed 和 setIsolateWhenIdle 为 internal，retain、release 为 private；internal 方法与编译器生成的访问器仍可能出现在 JVM public 列表中，不等于新增公开源码契约。相对首版策略实现，内部方法和匿名类有所调整。本对照不覆盖 Kotlin metadata、Release 产物或 Android 运行，不宣称整个 ABI 完全相同。

Skia / EGL 软件 GL 对照保留相同全屏页面源与顶部栏，只改变最外层全屏离屏目标。固定尺寸、主题、位置序列及有无两遍渐进模糊的矩阵重复两次，128 个静态根、内部页面移动的像素对照均为零差异。含模糊模型的各组中位耗时下降约 6%～12%，无模糊对照约 15%～28%；透明内容、未隔离目标混合和根级变换另有像素差异反例，支持收窄接入范围。软件 GL 不包含 Compose、Android HWUI 或硬件 GPU，位置序列也不等于实际导航时钟；这些比例不是设备收益保证，原始记录与复现脚本由联合性能报告附件保存。

没有硬件 GPU、真机帧时或设备视觉验收结果。待验证项包含菜单首开及快速重开、退出时同时切页、Dialog 内菜单的父链释放、源重建与后台恢复，以及深浅主题和宽色域下的颜色。诊断中的 activeConsumers、isolateWhenIdle 和 compositingStrategy 用于核对状态，不当作 GPU 耗时测量。

## Alternatives considered

- **在没有菜单时移除 source Modifier。** 可同时省去录制和节点维护，但首次消费需要重新挂载、定位和录制；跨窗口首帧和诊断操作会增加新的时序。本实现保留源，只切换合成策略。
- **所有 CouiBackdrop 默认使用 Auto。** 接入最省事，但会改变既有任意内容的目标混合隔离，并可能让多个消费者重复回放复杂页面。默认行为保持，只有显式窗口源选择按需策略。
- **只按 expanded 开关隔离。** 状态简单，但关闭动画仍然绘制材质，也不能覆盖多菜单、子菜单和材质弹窗的共同背景。按实际材质存活期计数。
- **由 Provider 另建 CompositionLocal 与源映射。** 能集中管理菜单需求，但重复已有 parent 关系，并增加重建后的身份迁移。计数直接随 CouiBackdrop 存活，公开入口保持受控范围。
- **在各消费者分别配对 retain/release，或仅提取顶层扩展。** 两处直接配对容易阅读，但调用方仍可漏掉释放或选错 Effect key；顶层扩展也无法访问类的 private 配对方法。成员入口集中组合期协议并收窄可见性，不新增资源对象，不改变消费者的显隐判断。
- **同时让所有 Dialog 和 BottomSheet 选择 false。** 可以扩展省层范围，但透明内容和动态变换会扩大像素验收要求。先限于静态页面根，独立验收其他容器后再扩展。

## Consequences

显式选择 false 的窗口源在没有材质消费者时不强制整窗离屏；消费期间恢复原尺寸栅格化，父链和多消费者继续共享源。代价是新增公开策略与组合期使用要求，未来增加新的隐式材质消费者时必须在实际消费期间调用 RetainWhileComposed，引用配对由源统一持有。

空闲源仍录制绘制指令，顶栏模糊、浮动材质和子内容的渲染成本仍在。默认入口不获得这项变化，实际收益取决于宿主原有图层、操作序列和设备。只有设备证据确认其余源或录制成本支配帧时，才扩展策略或重访节点按需创建；不将该调整描述为 GPU 开销已降低固定比例。
