# Agent Note: 底部面板的运动与窗口采样归属

Status: implemented

## Problem

底部编辑面板需要受控退场、下拖关闭、嵌套滚动和独立窗口背景。拉高普通警告弹窗不能表达这些交互；将时钟编辑业务放进库又会混淆通用组件与宿主职责。窗口、纯材质背景及清晰内容若共用同一个采样源，还会产生自采样或重复叠加。

## Decision

[ModalBottomSheet](../../../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt) 只提供展开与隐藏位置、遮罩、拖动、安全区及通用内容槽。visible 和退出决定归宿主，调用方持续装配直到退场完成；确认及草稿留在 Demo。SheetToolbar 是独立可选入口，按钮继续组合 TopAppBarButton。

普通底部窗口只执行位移与遮罩，不套用居中或锚点分支的缩放。位移响应由可见面板高度与可用高度确定，遮罩使用同一响应。Compose 记录的是可见表面高度，不能重复扣掉原厂测量时先扩展、再在位移距离中减去的底部 padding。正常反向保留速度，手势接管清除旧目标和速度；取消及未达到隐藏条件的释放回到展开位置，只有宿主接受退出才隐藏。

首轮测量在组合阶段订阅，测得高度后先提交隐藏帧，再启动入场弹簧。固定外框持有未动画的采样锚点，位移只在内层 graphicsLayer 和背景绘制阶段读取；背景逆位移与显示使用同一帧。纯背景有独立 Auto 绘制节点，避免采样失效牵连清晰滚轮和内容的显示列表。实时背景、模糊网格与轮数不冻结或降级，净性能收益仍需设备数据。

面板表面消费进入 Dialog 前取得的外层窗口源。Dialog 内部提供以外层源为 parent 的完整窗口源，供后续独立菜单和弹窗使用；工具栏和分类项则消费不含自身的纯面板背景源。已经提供窗口源的同窗口内容不再重复包装 provider。默认保留内容隔离；isolateContent=false 只用于已自行隔离目标混合的内容，无消费者时允许 Auto，实际浮层消费者仍恢复原分辨率离屏。

菜单可见边界通过稳定读取函数传递，在消费端计算，不按每帧矩形重组整棵内容树。面板内菜单按可见区域限制位置与高度；未提供该 Local 的普通菜单行为保持。内容消费面板已处理的顶部和底部 inset，IME 只在窗口外层处理，避免重复安全留白。

外形复用 CouiShape，平滑上圆角与下半部矩形用 PathOperation.Union 做真正并集，不能以追加反向轮廓代替。Panel 的 SHADOW_STYLE_2 在清晰内容之后绘制，使用公共 Contour 的独立预设；底部轮廓延伸到窗口之外。材质仍复用公共 Fast Kawase 和混色后端，不复制 Shader。

Panel 工具栏按其窗口内 Drawable 分支选择按钮底色和 CAPSULE_8 / CIRCLE_4，不使用普通顶栏的两层混色。标题与菜单文字持有各自原厂角色和字体留白，不能借用警告弹窗按钮字号。两侧操作先测量，居中标题对称避让较宽一侧；长标题可增加高度。

WheelPicker 的 visualWidth / visualAlignment 只控制数字绘制，不缩小输入列；TimeLimitPicker 与 MiniTimePicker 的选择线保留选择器自身的水平内缩，独立于外部卡片留白。闹钟类型组合 ChipTabRow，其可选 selectedColor 是宿主指定的主题色覆盖，默认材质不变，不将该组合声称为新增的原厂 Choice 控件。

## 原厂依据与适配

基准为 Clock 17.6.94，APK SHA-256 为 231b22b01cee3783259bdf896399308c0b9cd7e279b0ba226d6735f0075a778d。实际消费者是 AddAlarmPanelFragment 与 TimerPanelFragment，经 COUIPanelFragment / COUIBottomSheetDialog 装配；普通手机使用最大可用高度、隐藏拖动条，并设置 Panel 工具栏。Demo 只展示本地闹钟和倒计时草稿，不访问系统提醒或复制铃声、振动等完整子页。

doParentViewTranslationShowingAnim / isFadeInCenter 区分普通位移与居中缩放；COUIBottomSheetBehavior.onMeasureChild、updatePaddingBottom、getTranslationDistance / getTranslationResponse 共同决定高度语义。PanelPercentFrameLayout.draw 与 applyShadowEdgeParams 决定前景轮廓和 STYLE_2。ToolbarMaterialEffectDelegate.applyPanelBackground、Title.Panel / HeadlineS、COUIMenuItemStyle 决定材质和文字。相关 Java 分支交叉核对对应 smali。

TimeLimitPicker.onMeasure 与 NumberPicker.onDraw 说明宽点击列内部的数字位置；TimeLimitPicker.dispatchDraw 和 MiniTimePickerView.onDraw 说明选择线内缩。设备原厂控件树曾与相同 APK 的资源核对，但不能据此宣布当前移植的视觉和性能已经通过。

公开 Android/Compose 窗口与应用内背景源承担移植，不等价于 OEM 系统合成端。当前不实现居中、锚点跟手、多窗、小折叠屏、多级 Fragment 合并、collapsed 状态或拖动条折线形变；这些边界由使用页明确说明。

## Existing-note audit

- [警告弹窗](2026-10-24-coui-alert-dialog.md)：部分重叠，复用其公共效果与文字度量，保留独立的窗口和交互职责。
- [持久运动通道](../architecture/2026-10-29-persistent-motion-channels.md)：部分重叠，沿用重定向、硬重置与取消语义，不新增物理引擎。
- [宿主页面职责](../architecture/2026-11-03-host-owned-page-layout.md)：部分重叠，草稿、页面内容和完整窗口源归宿主，组件不引入业务依赖。
- [ChipTab](2026-10-24-chip-tab.md)、[选择器](2026-11-01-coui-pickers.md)、[菜单输入](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)：保留既有交互，本篇只持有面板消费者的可选样式和可见范围装配。
- 尚未投稿的本机 Sheet 调试记录只用于整理依据，不作为多篇历史记录进入此次新增组件 PR；无已发布同范围 Note 需要归档。

## Alternatives considered

M3 ModalBottomSheet 可快速提供窗口和拖动，但动画分支、背景采样与状态协议不对应目标原厂。将 DialogCard 拉高能复用布局，却会把警告操作与面板滚动混在一起，因此只复用效果，独立实现窗口和运动。

把平滑轮廓与矩形改为相同路径方向可消除当前孔洞，但依赖输入轮廓方向；真正并集更能保持组合语义。每帧通过 CompositionLocal 发布矩形便于读取，却会重组全部内容；稳定读取函数把动画状态留在实际消费者。冻结背景或减小模糊能减少工作，但改变材质，不采用。

## Consequences

库可独立承载底部编辑内容，并保持草稿、提交和业务导航的宿主所有权。代价是独立窗口与运动生命周期，以及 Panel 对 Contour、Picker、Chip 和菜单的最小可选支持。没有选择这些选项的现有消费者保持原行为。

静态与构建检查不能证明帧率或原厂逐像素等价。首次及重复开关、反向、拖动中断、IME、RTL、字体缩放、按钮和顶边中间帧、TalkBack 与嵌套浮层仍需同设备验收；实际执行结果写在 PR。
