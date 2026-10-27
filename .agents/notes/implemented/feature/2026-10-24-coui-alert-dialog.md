# Agent Note: COUI 弹窗组件与原厂承载边界

Status: implemented

## Problem

上游没有普通 COUI 弹窗。直接使用宿主 M3 AlertDialog 无法表达目标的标题正文排版、操作布局、底边距和材质；把整套本地 Demo 重构一起引入，又使弹窗无法独立评审。初次拆分还混入普通按钮、排版与列表反馈接口，并把原厂含留白的宽度当作可见卡片宽度。

## Decision

新增唯一的弹窗组件 CouiAlertDialog 及其操作数据、配色和 Defaults。窗口、操作按钮和字体度量均在 dialog 包内部，Demo 只消费公开弹窗 API，不新增普通按钮或 Typography 组件。组件负责局部交互，宿主持有可见性、文案、主题和业务提交。

以主线 3a13b9f7b6e866dec421244357d14ce1de8b1a54 为本批起点，保留原 Demo 的三页、顶栏、dock、图标及其他示例。接入沿用上游 PR #2：DemoSamples 增加一个 DemoSection、DemoApp 的分组搜索标题增加一项，独立 DemoDialogSamples 持有显示与确认结果。一个分组中提供底部、居中、长文、三个操作、禁用确认及主题色确认六种参数装配，并允许切换材质；不带二级页、开关、选择器和目录重构。

### 原厂依据与执行路径

材料为 Settings 17.0.0、versionCode 17000000，APK SHA-256 为 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。未取得设备构建指纹，反编译材料不作为源码依赖。

COUIAlertDialogBuilder 经 alertDialogStyle 选择 BottomWarning / Center 与 coui_center_alert_dialog_layout；COUIAlertDialogMaxLinearLayout 读取宽高上限，背景 InsetDrawable 持有左右和底部留白。上限作用于外框，卡片在左右留白内排布，避免宽窗口额外增加两侧留白的宽度。底部导航适配沿 updateDialogBottomMargin / getEffectiveBottomMargin 的有效值分支，用公开 inset 承载导航类型识别。

COUIAlertDialogMessageView 按行数切换单行居中、多行起始对齐；COUIButtonBarLayout.getHorizontalButtonContentWidth / needSetButVertical 决定两项横排或竖排。标题、正文和操作使用系统 Typeface、Paint 字体度量及原厂缩放/行距规则转换为 Compose 样式。正文滚动与操作独立；按钮区正常高度不建立滚动裁剪，实际超高才滚动。

COUIButtonBarLayout.setBlurBackgroundWindow 将材质门控传到 COUIButton；changeButtonAlphaColor 在普通中性底色与半透明白色间选择，applyBlurEffect 选择 pressed 蒙层。移植保持同一门控，轮廓、形变及跟手光分别复用公共后端；主题色由操作显式传入，配色只有实际消费的字段。窗口属性转交 Compose，窗口位置、动画、DIM 和平台模糊则由组件统一管理。轮廓的深浅分支使用宿主传入的 dark，不从自定义底色亮度反推主题。触摸探索启用时不允许外点关闭，返回仍按宿主设置处理。

底部使用原厂 250ms 位移动画，居中使用 250ms 缩放与淡入、150ms 淡出；资源保留进入/退出各自的曲线和坐标语义。确认与取消按钮各只有一个 clickable，外点或返回仅调用 onDismissRequest，宿主移除后释放图层、监听与协程。按钮的按压、拖动和光效复用现有输入观察器，UP、CANCEL 与父容器分发仍须运行核验。

操作区滚动接管后，按钮的按压、拖拽形变和点触光分别按 CANCEL 收尾，不等待真实 UP。公共形变入口保留原有公开签名，通过内部重载启用父消费取消；其他消费者仍使用原会话规则。点触光沿用已有取消选项，任一手指移动被消费后结束会话，后续移动或加指均不重新点亮，所有手指抬起或收到合成 CANCEL 后允许下一次按下。没有添加极矮窗口专用布局或新的公开配置。

取消链的共同实现依据来自录音 APK，SHA-256 为 968c3c17c6cdcc09f9a64e6fd939bfa4382104d7afda6dffb7c2f14ccb587588。`coui_center_alert_dialog_button_panel` 使用 `COUIMaxHeightNestedScrollView`，可滚动时进入 `COUINestedScrollView.onInterceptTouchEvent`；父容器接管经 `ViewGroup` 分发 CANCEL。`COUIButton.onTouchEvent` 退出状态蒙层并转发点触光熄灭，`dispatchTouchEvent` 将取消送入 `COUITouchMotionAnimator`，Press / Deform / Rubber 单元以零输入速度走抬手收尾。这条链证明原厂有对应处理，不表示日常默认布局容易触发，也不替代指定 Settings APK 的运行对照。

### 公共效果复用

窗口复用既有 CouiBackdrop 与菜单 Fast Kawase 后端，只将半径参数化为弹窗所需的引擎等级；菜单工厂仍使用原等级，其他菜单行为不变。不使用平台 Gaussian 替代 Kawase，也不把引擎等级解释为像素半径。

材质复用 rememberCouiMaterialEffect，扩展默认保持 Overlay 的可选 ColorDodge；operateBlur → COUIBackgroundBlurBuilder → OplusBlurParam → MaterialFilter 的目标分支为浅色 ColorDodge、深色 Overlay。只过滤背景，不过滤文字与按钮。系统 DIM 不录入页面源。图层录制传入独立 Density，避免 DrawScope 成为自己的密度代理。RuntimeShader 材质创建、模糊绘制及轮廓调用点均显式检查 API 33；材质参数的入口校验继续拒绝旧平台，不增加另一种静默替代算法。

ColorDodge 保留原厂的 `d / (1 - s)`，不增加 epsilon 或白色通道特判。PJZ110 的 `librenderengineextimpl.so`（SHA-256：1a232042b2252c4d061296ac347f6995824003b8d2c1553a51672105ddac7850）中，Shader 位于 0x29ca0，`MaterialFilter::appendColorDodge` 位于 0xa264c；参数读取与 uniform 上传也没有白色通道限幅。自定义输入出现除零不是这条原厂分支的移植差异，不将 SystemUI 另一个同名混合函数的保护移入此处；默认弹窗预设不使用白色 blend。

轮廓复用 CouiContour / CouiEdge 公共距离场、内晕和周长分布，增加 Dialog 与内部 Button 使用的 Drawable 参数映射及 NEW_G2 覆盖率。RootLayout.onDraw 使用 EDGE_STYLE_2 / SHADOW_STYLE_0，输出预乘白光 SrcOver；不能用菜单的后景感知提亮替代。保留既有菜单和 floating 分支；不新增组件私有 Shader。状态蒙层只增加内部按钮入口，不提前引入列表会话。

## Alternatives considered

- **独立公开按钮、排版和窗口容器。** 宿主能单独装配更多能力，但本次用户只提交弹窗，尚没有这些入口的独立交付需求；操作配色作为弹窗数据，内部按钮、字体和窗口不公开。
- **直接把本地 Demo 整体覆盖上游。** 可以保留已完成的设置式目录和更多演示，但会混入后续三批功能，无法独立评审；本批只在原示例中增量接入。
- **直接使用 M3 弹窗或平台窗口模糊。** 接线少，却改变目标的布局、字体及模糊链；保留 Compose 窗口承载，内容与项目效果自行装配。
- **给弹窗复制轮廓或 Fast Kawase Shader。** 能独立调试，却重复已有后端，且容易让菜单与弹窗公式分叉；只扩展所需的参数和绘制分支。
- **统一让所有形变消费者在父消费后取消。** 可少一个内部重载，但会改变菜单等既有会话；保留公开签名与默认行为，仅弹窗启用。按压与点触光复用已有选项，不新建会话框架。
- **为 ColorDodge 白色输入增加保护。** 可消除自定义极值输入的除零，但当前目标 native 分支没有该处理；按用户要求保留原算法，不将这一边界列为必须修复的原厂差异。

## Consequences

本批可从干净主线独立检查和构建，Demo 的其他组件仍保持上游行为。共享效果的扩展使第一批包含 effect 与菜单模糊文件，评审须确认默认菜单、FAB 与顶栏分支未改变；后续工作必须基于本批最终接口重新拆分，不能直接覆盖回原本地版本。

公开 Compose 裁剪与字体度量不等于 OEM 私有渲染器、字体和窗口调度。Tiny、推荐/AI Payment 按钮、列表 Controller、完整极端高度动态拆分/合并尚未移植；导航类型识别、居中承载、输入法、字体缩放、RTL、触摸探索、动画中段和 GPU 性能仍需设备验收。静态检查或构建通过不能声称 1:1 验收或性能提升。

## Verification

本批通过 GitHub 插件读取上游 PR #2 的说明、完整文件名及 Defaults、DemoApp、DemoSamples、DemoChipSamples 的实际补丁，并核对原厂 Java、资源与现有后端。

在本批独立目录执行 `:demo:assembleRelease --offline --console=plain --no-daemon` 成功，包含依赖编译、R8、资源优化和 Release vital lint；最后一次构建 85 项任务，20 项执行、65 项复用。首次沙箱阻断 Gradle 与本机 daemon 的连接，获本次本机构建执行权限后完成，未改变构建或依赖配置。

差异格式、21 篇 Note 的树/格式/归档检查、72 篇 Markdown 的 471 个本地文件链接、16 组双语资源及新增 XML 解析通过。新增公开 Composable 仅 CouiAlertDialog；库内窗口、按钮及字体入口保持 internal/private，Demo 不导入内部接口。窗口宽度含留白的约束、普通/材质按钮配色和窗口属性转交做了定向源码核对。

最终 APK 的 v2 测试签名有效，源码时间早于产物。这里不沿用完整重构 Demo 的旧构建结论；本地没有运行 Debug、独立 Lint、库发行、设备或原库运行对照。动画、GPU、输入、字体和性能仍未完成设备验收。

滚动取消修复另以当前生产输入控制流做临时 JVM 替身检查，13 组输入分别覆盖按压、形变和点触光：正常点击/拖动、被消费的按下或静止、父接管、接管后移动/加指、全部 UP、合成 CANCEL 后再次按下、非光源指被消费、外部目标映射、节点移除及禁用。公开入口的尾随 lambda 调用保持可编译，取消使用零输入速度且速度跟踪器均回收；3 个修改的 Kotlin 文件通过 PSI 语法检查。替身不执行 Android ViewGroup、Compose 事件分发或真实光效动画，不等于设备验收。本次未运行 Android 构建或设备操作，未修改 ColorDodge 材质代码。

## Existing-note audit

[公开 API 收口](../architecture/2026-10-23-public-api-boundary.md)、[模块边界](../architecture/2026-10-21-coui-module-boundary.md)为本批沿用的约束，不改变既有决定。[菜单输入与合成层序](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)继续持有菜单特化与源契约；[顶栏渐进模糊](../bug-fix/2026-10-18-coui-topbar-gradient-blur.md)的独立 Density 约束在窗口录制中复用。其余 ChipTab、发布、签名和 Demo 记录与新增弹窗没有决定取代关系；没有同范围 proposed/rejected 需删除或归档。
