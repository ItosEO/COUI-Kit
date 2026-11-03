# Agent Note: 普通弹簧持久通道与方向动画边界

Status: implemented

## Problem

动画参数相同不能保证输入中断后的状态相同。Touch/Press 每次 MOVE 或按压启动 Animatable 协程，不能直接保留原厂持续运行的 pending 目标与半帧积分。Slider 的普通 cancel 未清除 pending 和速度，旧目标可能进入下一次触摸。状态反馈归一化后使用另一结束精度；复选框则把独立图形组与不可逆方向资源合成了一条可连续反向的缩放。

## Decision

仅将普通标量弹簧抽为内部 [CouiSpring](../../../../coui/src/main/java/com/itos/coui/motion/CouiSpring.kt)，复用已锁定的 AndroidX DynamicAnimation。Touch、Press、Slider、Switch 的普通弹簧和状态反馈共用它，不复制厂商调度框架。

运行中重定向复用原 SpringAnimation，保留速度及 pending/半帧积分。普通 cancel 只停止；cancelComplete 清回调并在使用过的通道上重建实例，清除原厂 reset 对应的历史。无弹簧运行介入的连续 snap 不反复重建。Slider 停止位置动画、重新按下及硬定位使用完整重置，Section ValueAnimator、惯性与既有边界适配保持。

[TouchMotion](../../../../coui/src/main/java/com/itos/coui/motion/CouiTouchMotion.kt) 与 [PressMotion](../../../../coui/src/main/java/com/itos/coui/motion/CouiPressMotion.kt) 保留构造入口，通过原 scope 的取消释放动画；工厂也在节点移除时释放。入口及作用域均在主线程使用。四轴/按压轴公开类型收为 State<Float>，保留 .value 读取；子菜单退出改调 Touch.reset。库内 Press 调用统一使用 rememberCouiPressMotion。同一触摸组合的缩放增量相加，底栏附件外层收放仍按独立变换相乘；背景、内容、光效和投影使用一致的几何。

[CouiStateProgress](../../../../coui/src/main/java/com/itos/coui/effect/CouiStateProgress.kt) 保留原始进度尺度，仅在读取时归一化。短按等待首个实际超过最低进度的更新帧再重定向；掉帧直接到终点时，在旧动画结束后退场。菜单共享预选框交接直接从传入进度退场，不补亮。各消费者的颜色、短按门控、ARGB 插值和单一点击 owner 保持。

[CouiSwitchMotion](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitchMotion.kt) 持有受控值同步、普通切换、拉伸、颜色进度和提交；内部 [CouiSwitchDrag](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitchDrag.kt) 持有拖动阈值、双锚点选择及吸附速度。Toggle 与拖动 X/Y 分别使用公共弹簧通道，吸附等待两轴均停止后通知一次完成。保留整数可见位置、浮点切换进度、速度量化、目标限制、近锚点直接完成，以及 CANCEL 不提交、宿主拒绝候选后恢复受控值。公开开关 API、输入装配及加载动画不变。

[Checkbox](../../../../coui/src/main/java/com/itos/coui/checkbox/Checkbox.kt) 用一个线性时钟驱动方向资源的图形组，不为每项属性新增动画对象。外框与底色同曲线但分别绘制，勾线的选中回落独立；填充、trim 和方向笔宽来自对应资源。反向按不可逆 animated-selector 从新方向起点播放。初次显示、禁用与重新启用使用静态目标，保留宿主 Shape 和受控接口。

## 原厂与平台依据

- 录音 com.coloros.soundrecorder 17.8.8 / 17008008，SHA-256 为 968c3c17c6cdcc09f9a64e6fd939bfa4382104d7afda6dffb7c2f14ccb587588。COUITouchMotionAnimator.TouchMotionCollector.put 对 scale 累加相对单位值的增量；COUIPressAnimatorUnit 按下显式设置输入速度，抬手保留运行速度。COUIDeformAnimatorUnit、COUIRubberAnimatorUnit 和 COUIITouchMotionUnit 分别持有普通取消与完整 reset。
- StateEffectAnimator.animateToProgressUntil / setProgress 保留运行中的上行目标，跨阈值后重定向；直接达到终点时通过结束监听退场。animateToProgressFrom 清除短按阈值。此规则不推广到未启用最低进度的反馈消费者。
- CellBroadcastReceiver 的 com.android.cellbroadcastreceiver.module 17.10.10 / 1700010010，SHA-256 为 e57ed508605794386949f90c0c288fbaacea327ba618d7bae91d4d03ac421d10。coui_btn_check 双向 transition 没有 reversible；转入 check 的缩放回落有独立插值，转出 fill 延迟退场。Android SDK 的 AnimatedStateListDrawable.selectTransition 在不可逆中断时以旧目标作为下一条转场来源，不连续倒放旧动画。
- 锁定的 AndroidX SpringAnimation.animateToFinalPosition 在运行时设置 pending；DynamicAnimation.cancel 不清 pending 或速度。原厂菜单的独立双阈值弹簧不是此普通分支，本轮不改菜单展开后端。
- Switch 的目标 Settings 参数与既有适配见[开关组件](../feature/2026-10-27-coui-switch.md)。本次回读录音包的 COUISpringDragHelper.dragTo、getAttachPosition、cancelDrag 及双轴状态收尾，核对提取边界；没有重新提取 Settings APK，不将共享 helper 的核对扩大为目标包全链路复验。

## Existing-note audit

- [绘制工作省略](../simplification/2026-11-01-render-work-elision.md)：部分重叠，复选框缓存静态几何、菜单预选位移在 placement 读取；本篇的时钟、方向重启、弹簧与取消语义保持。
- [公开 API 收口](2026-10-23-public-api-boundary.md)、[滑条](../feature/2026-10-21-coui-slider.md)、[开关反馈](../bug-fix/2026-10-28-switch-review-feedback.md)：部分重叠，互链。本篇补轴写入口收窄、完整重置与进度承载，不取代公共绘制、惯性边界、G2、触摸 pressed owner 和焦点外描边。
- [ChipTab 中断](../bug-fix/2026-10-24-chip-scroll-interruption.md)与[弹窗](../feature/2026-10-24-coui-alert-dialog.md)：沿用各自父级接管与业务提交规则，仅迁移共享 Press 工厂和缩放合成。
- Demo 展示、分类栏装配与其他命中记录仍持有各自布局、绘制和物理决定；没有同范围 proposed/rejected 或完全吸收项需要删除、归档。

## Alternatives considered

- **继续使用每次调用的 Animatable 协程。** Compose 接线最少，但中断与重定向规则不同于本轮确认的普通弹簧分支，且直接暴露轴写入口。
- **搬入 COUIComposeMotionAnimator 与全部物理框架。** 更接近原类结构，但带入本轮没有消费者的调度、配置和单位系统；所需行为可由已有公开依赖承载。
- **所有 cancel 都完整重建。** 容易消除旧状态，却破坏 Touch DOWN 的普通取消语义；普通停止与硬重置必须分开。
- **复选框每组分别持有动画。** 易于单项观察，但这些资源共享时长；单时钟加独立插值即可表达时间关系，也便于统一取消和方向重启。
- **保留公开 Animatable 并同步两份值。** 减小源码迁移，却建立可写镜像和两个动画 owner；选择只读 State，承担明确 API/ABI 迁移。
- **Switch 继续混合管理，或搬入完整通用拖动 helper。** 前者文件最少，但拖动、吸附与受控提交仍耦合；后者贴近原类结构，却增加没有消费者的多锚点与二维拖动接口。只提取开关使用的双锚点内部 helper，不公开扩展框架。

## Consequences

普通弹簧有明确的重定向、停止、重置与释放路径，复选框不再把共享时钟误当作共享曲线。代价是 Touch/Press 每实例持有与 scope 绑定的生命周期 Job，硬重置在需要时替换 SpringAnimation。State 返回类型导致二进制不兼容，调用模块须重新编译；只读 .value 源码保持，直接轴控制按[运动控制说明](../../../../docs/effects/motion.md#主动控制运动)改为组件方法。已知外部宿主只读取轴值，没有切换其依赖或修改其独立 LiquidGlass 合成。

本轮不处理子菜单动态可见矩形、完整 Slider 边界物理或统一菜单、Section、Loading、Chip 物理后端。保留 Compose 绘制、G2、染色、搜索避让和 Switch 受控 CANCEL 等已批准特化。没有设备数据，不宣称性能收益或全链路原厂等价。

Switch 增加一个职责明确的内部实现，三个标量通道随开关实例持有，代替原有按阶段临时创建的弹簧；硬重置仍按需替换底层实例。代价是静止实例也持有通道对象，收益是物理通道与业务提交分离。此次内部拆分不改变开关使用契约，调用方与普通使用文档无需迁移。

## Verification

临时 JVM 检查直接编译生产 CouiSpring、CouiTouchMotion、CouiPressMotion、CouiStateProgress，并运行锁定版本 AndroidX 实际二进制。覆盖持续重定向保速、硬重置与直接参考弹簧逐帧一致、重复硬定位不重建、取消不回调、AndroidX 原始 pending 残留反例、短按最低进度、重按、掉帧直达终点、交接退场、Touch reset/release 及 scope 取消后不重启。时钟、Looper、Compose State/几何和 PathInterpolator 使用临时替身，不执行真实 Choreographer、Snapshot、Compose 事件或 GPU 绘制，不能证明 Android 运行等价。

Android 构建、Sync、安装、设备视觉与性能未执行；需要用户验证按压、拖动、反向、短按、父容器接管与节点移除。不新增常驻测试框架。

复选框检查提取生产帧表达式，使用缓存的 Compose Easing 执行，覆盖分组峰值、独立回落、填充延迟、方向重启和禁用/启用端点；不执行真实 Compose 时间调度。修改的 Kotlin 文件通过 PSI 语法检查，使用兼容的缓存编译器解析；开发版本的独立 PSI 环境初始化失败，不能记为源码错误。普通文档相对链接/锚点、UTF-8 无 BOM、Note 树/格式/归档与 git diff --check 通过。语法解析不包含 Android/Compose 全模块类型检查。

Switch 的临时 JVM 检查直接编译生产 CouiSpring、CouiSwitchMotion、CouiSwitchDrag，与拆分前实现运行同一锁定版本 AndroidX 二进制。62 组输入逐帧比较位置、颜色进度、拉伸、完成计数及提交内容与时机，覆盖 RTL/初始值、运行中反向、两级拖动启动、停稳后重启、pending 取消再进入、禁用、程序值接管、宿主接受/拒绝、速度门槛、Y 轴收尾、分数行程、节点移除和近锚点重复 UP；另检查 helper 等距锚点及取消不回调。全部通过。这是拆分前后的回归对照，不是原厂整库执行对照。Looper、时钟与 Compose State 使用临时替身，未执行真实事件分发、Snapshot、Choreographer 或设备绘制；临时编译器首次缺少 kotlin-reflect，补齐缓存 classpath 后通过，没有修改工程依赖。
