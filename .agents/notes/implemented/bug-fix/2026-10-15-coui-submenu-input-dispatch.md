# Agent Note: 菜单输入会话与合成层序

Status: implemented

## Problem

对子菜单统一增加移动门槛不能表达原厂的取消、补按下和退出接续。Compose 在 DOWN 时固定命中路径，新子面板不会自动收到原手势。轮廓与跟手光若放错离屏层，也会改变混色结果。

## Decision

两级菜单共用一个 Popup，普通触摸提交由共同根节点持有；行保留键盘、悬停和无障碍。停留展开先取消父会话，子进入完成后在后续移动中补建按下。移出子面板取消选择与滚动，再进入重新按下；没有子按下的 UP 不提交。开始收起取消子会话，退出移除后才将下一条事件替换为一级按下；原事件若为 UP，只结束，不点击底下的主项。

复制父行的微动保护独立于普通子项。退出期间列表 MOVE 不更新预选或滚动，但根区域停留、标题反向和一级点击不被整体禁用。同父项从当前进度反向，换父项结束旧转场后重新测量。触摸探索不接管跨行滑选，真实 CANCEL 不提交。

预选、跟手光、形变有各自的结束条件。直接 DOWN 的形变与光效会话可延续范围外坐标，转发到子面板的光效按补 CANCEL 规则出界结束。换指保持连续，不重设拖动原点。父、子滚动各用自身 ScrollState；滚动条优先消费，不提交底下行。先停止用户滚动再归零，避免取消转场协程。整菜单重开回顶是已有用户契约；子退出保留位置，移除后清理，不称为所有原厂列表的通用行为。

Fast Kawase 使用变换后的设备像素建立各轮独立网格，原源先按自身尺寸栅格化，不在降采样层重绘矢量。仅背景执行模糊、材质、内晕、描边；透明前景按跟手光 → 内容绘制。光效只观察输入，资源随退场释放。菜单 SDR、组合描边和可选主题染色是已接受适配，不宣称原菜单完整 1:1。

菜单背景通过 `GraphicsLayer.record` 成员入口显式传入 `LocalDensity`。这段录制只画背景，不调用 `drawContent`，无需重定向外层绘制作用域。`DrawScope` 的同名扩展会将外层作用域写回自身的密度来源；嵌套模糊读取 `density` 或 `fontScale` 时会无限递归。不能在已经重定向的回调中读取这些值来补建 Density。单层、一级和子菜单共用 [couiMenuMaterial](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuMaterial.kt) 修复此边界，模糊算法、前景离屏隔离和调用接口保持原样。

## Evidence

本篇从原宿主的同名 Note 与菜单契约整理迁入。原厂基准为 PJZ110 相册/录音的 Root、RootListener、RegionBasedLongPressDetector、COUITouchRecyclerView 和 RoundFrameLayout。原厂子 View 可独立收 CANCEL/DOWN，Compose 需要还原会话语义，不能向整个 ComposeView 重投事件。直接按下和父转发会话的范围外光源行为曾对照官方相册取样，该结果不是修改后组件验收。

当前入口见 [CouiMenu](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenu.kt)、[子菜单](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuSubMenu.kt) 和 [模糊](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)。公开宿主只接数据、外观与回调，不读写滚动条、运动帧或坐标状态。

密度自引用核对 `ui-graphics-android:1.13.0-beta02` 的 `DrawScope.record` 与 `CanvasDrawScope` 源码。在 JVM 上直接使用该 AAR 的实际类，将作用域写入自身 `DrawContext.density` 后，读取 `density` 和 `fontScale` 均复现 `StackOverflowError`；改用独立 Density 后两项读取均通过。此检查验证密度代理行为，未执行 Android 图层录制、菜单实绘或设备验收。

## Alternatives considered

- **保留统一移动阈值。** 能防部分误点，但该阈值只属于刚展开的返回标题，不能替代普通子项会话。
- **向 ComposeView 重投 MotionEvent。** 可产生真实取消，却会取消整个宿主命中路径并产生重入，边界不同于原厂独立子 View。
- **保留行触摸提交，根只做滑选。** 接线少，但两个 owner 会竞争同次 UP，也不能让旧滚动节点自动接收子面板手势。
- **统一所有观察器的出界释放。** 分支少，却会同时破坏拖动连续性或遗漏转发子会话的取消。

## Consequences

固定模糊指令复用与预选位移延后读取见[绘制工作省略](../simplification/2026-11-01-render-work-elision.md)；九轮网格、内容离屏隔离和本篇输入会话保持。

[原生主题](../architecture/2026-10-30-native-theme-and-component-defaults.md)使默认 Style 的额外页面染色按宿主显式适配启用；本篇的组合光效、输入会话与层序不变。

保留原厂会话和层序约束，代价是根节点必须显式处理面板归属与退出时序。内部状态收为 internal，公共绘制算法与固定预设仍可复用。

原工程用生产输入体与 JVM 替身检查过提交、取消、退出、标题、滚动及边缘起手；替身不包含 Compose 真实命中或物理时钟。本轮只迁入约束，不修改菜单算法，不重做设备验证。公开 Path 覆盖率、旧平台 Gaussian、组合描边与实际惯性仍有已知承载/适配边界。
