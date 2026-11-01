# Agent Note: COUI 开关组件与状态示例

Status: implemented

审查发现的拖动期间按压提前取消与键盘焦点描边修正见[开关反馈复核](../bug-fix/2026-10-28-switch-review-feedback.md)，以目标 Settings 实际消费者复核，保留本篇的物理、资源和受控取消决定。

## Problem

主线缺少普通 COUI 开关；Demo 自己的 M3 配置开关不能表达原厂的轨道、滑块拉伸、拖动吸附与加载动画。用户完成真实系统控件的同页视觉审核后，要求单独准备开关 PR，沿原 Demo 增加示例，不带入完整页面重构或系统反射对照。

## Decision

[CouiSwitch](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitch.kt)是唯一基础组件，宿主持有 checked、loading、主题与业务回调。库公开 CouiSwitchColors 和 CouiSwitchDefaults；运动和加载绘制保持 internal，触觉辅助为 private，不另公开按钮、设置行或通用触觉枚举。

加载和禁用都是同一个 CouiSwitch 的状态。[DemoSwitchSamples](../../../../demo/src/main/java/com/itos/coui/demo/DemoSettingsSamples.kt)直接传入 loading 或 enabled 展示，仅模拟业务等待，不提供额外预设或包装组件。Demo 沿原 ControlsDemo 增加一个分组和搜索标题，保留原页面、dock、配置开关和其他示例。

### 原厂依据与移植

基准为 com.android.settings 17.0.0 / versionCode 17000000，APK SHA-256 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。主要链路为 COUISwitchStyle → COUISwitch，以及 COUISwitchParamsExtKt → SpringData → COUISpringForce/Animation、COUISpringDragHelper。构建指纹和用户设备 APK 哈希未读取；版本名一致不当作包字节一致。

SpringData 的构造顺序为 bounce、response、velocity，已经由 smali 字段写入确认。普通切换根据命名 getter 设置 Force，不能将构造调用的前两项读反，也不误用 SpringData.getDampingRatio 的另一条换算。拖动和吸附来自 COUISwitchParams 的独立字段。复用已有严格版本约束的 AndroidX DynamicAnimation，保持原厂闭式积分、阈值、整数可见位置与浮点拉伸/颜色。普通反向只改变目标并保留速度；吸附开始使用原始浮点速度，选锚点先量化速度，并等 X/Y 两路收尾。

[CouiSwitchMotion](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitchMotion.kt)负责 checked 同步、普通切换、拉伸、颜色进度与单一提交；[CouiSwitchDrag](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitchDrag.kt)仅持有拖动阈值、双锚点和双轴吸附完成，不接触业务值。两者复用内部 CouiSpring，不另造物理后端或公开通用拖动框架。通道生命周期、拆分代价及逐帧回归边界见[普通弹簧持久通道](../architecture/2026-10-29-persistent-motion-channels.md)，公开 API 与加载实现保持。

触摸位移来自原始坐标差；速度采样沿 convertToTranslationCoordinates，把局部 X 映射并夹紧到行程、固定 Y=0，再交给平台 VelocityTracker。原厂限制弹簧目标而不截断物理当前位置。切换完成、快速反向、未过阈值 UP、取消、父容器接管、禁用和移除分别收尾；宿主拒绝拖动候选时恢复实际 checked。CANCEL 不提交是用户批准的受控适配，原厂 CompoundButton 的 CANCEL 可能吸附并改变值。

轨道和滑块使用已有 CouiCapsuleShape；在拉伸尺寸下重建滑块轮廓，而非退回普通圆角。这是用户批准的项目平滑几何，原厂滑块自身使用 drawRoundRect。当前原厂关闭 spotlight，不叠材质玻璃或虚假轮廓光。轨道颜色和滑块在绘制阶段同步读取。

### 公共效果与资源

原厂 drawBar 将 StateEffectAnimator 的 hover/press 合成到轨道。新增内部 couiSwitchFeedback，复用 [CouiStateFeedback](../../../../coui/src/main/java/com/itos/coui/effect/CouiStateFeedback.kt)现有蒙层弹簧和混色；不复制后端、不扩展已有公开反馈函数签名，不改变其他消费者。

当前 View 通过公开 performHapticFeedback 调用原厂开关码，并保留系统设置及附着门控。不支持的 ROM 不合成替代波形；触觉码一致不代表跨硬件马达一致。程序赋值和取消不振动，声音未移植。

非换肤加载沿 initStartLoadingAnimator、initStopLoadingAnimator、drawLoading 和 startLoading/stopLoading：保留滑块收缩、图像显现、线性旋转、立即关图与快速重开的两组 AnimatorSet。原厂 COUILinearInterpolator 没有额外旋转曲线。加载开始和禁用输入不截断普通切换；等待期间不重复接受输入，成功、失败与业务提交归宿主。

加载图直接使用用户要求的原始 PNG，来源与字节哈希见[图像来源表](../../../../coui/RESOURCE_SOURCES.md)，第三方权利登记在 [NOTICE](../../../../NOTICE)。每个开关独立 mutate Drawable，保持整数四边和浮点中心的缩放、旋转顺序，配置变化重新加载。不在运行时依赖系统 APK、私有 SDK/SO 或反射。纯 Compose 组件可以通过公开 Canvas 绘制静态 PNG，不把无损位图误认为必须另造的矢量图。

## Existing-note audit

- [Demo 材质与语义](2026-10-23-demo-material-and-gallery.md)：部分重叠；原 M3 配置开关继续由 App 装配，本篇新增库内 COUI 控件及独立分组，不全局替换 Demo。
- [模块边界](../architecture/2026-10-21-coui-module-boundary.md)、[API 收口](../architecture/2026-10-23-public-api-boundary.md)：保持主题/业务归宿主、可变绘制节点独立、内部状态不公开，新增实际组件包。
- [ChipTab](2026-10-24-chip-tab.md)：保留原有输入与示例，没有共享修改其物理算法或页面。
- 主线没有同范围 CouiSwitch 决定，也没有需要归档或删除的同范围 proposed/rejected。

## Alternatives considered

继续装饰 Demo 的 M3 Switch 无法得到 COUISwitch 的物理参数、加载图及拖动状态机；另写独立加载组件会复制同一个开关后端。采用唯一组件，加载和禁用只提供状态示例。

手绘圆弧无法保留原始图像的离散边缘和透明尾迹，用户同屏审核已暴露差异；直接使用原始 PNG 并维护来源。打包整套厂商库或调用系统开关只适合临时审核，不能作为跨设备库依赖。

搬入完整二级页/触觉工具/列表反馈会扩张本 PR。复用当前效果后端并增加内部消费入口即可，其他功能继续独立评审。

## Consequences

宿主获得可单独接入的 COUI 开关，加载与禁用无需复制组件；Demo 和文档可展示完整装配。代价是维护受控取消与原厂自持有状态的差异，以及原图来源和每节点 Drawable。公开 Compose 事件分发、G2 几何和平台触觉仍有明确边界；不包含换肤、原厂声效或全 ROM 帧调度。

## Verification

用户已在完整 Demo 对照原厂实际控件并确认原图修复版本可用，临时对照随后按用户要求移除。主实现者回读 Java、smali、资源表及绘制入口后再收窄到当前主线。只改变反馈后端的内部接入名称及私有触觉封装，三份运动/加载实现、项目几何与 PNG 都与已审核版本一致。

独立原版 Demo 执行 :demo:assembleRelease --offline --console=plain --no-daemon 成功，包含 R8、资源裁剪和 Release vital lint。21 篇 Note 的树/格式/归档、59 篇 Markdown 的 476 条本地文件链接、10 组双语资源、UTF-8、差异与公开导入核验通过。主线原有 SearchBarDefaults.InputField 有一条废弃警告，未修改无关调用。

实际 APK 的 v2 测试签名有效，原始 PNG 保留，无厂商类定义、原版反射入口或额外 APK/AAR。清理后的完整 Demo 也单独构建并按用户授权安装成功，保留应用数据；独立原版 Demo 的构建产物仅用于本地提交前审查，未覆盖用户日常的完整 Demo。未运行测试任务、Debug、独立 Lint、库发行、Sync 或 Preview，没有 GitHub 写入。

不把完整 Demo 的视觉接受扩大为新增装配、全部无障碍或所有设备已验收。RTL、TalkBack、极端配置、动画中段和性能仍有设备验收边界；构建与源图一致不是这些路径的运行证明。
