# Agent Note: 底部面板的运动与窗口采样归属

Status: implemented

## Problem

底部编辑面板需要受控退场、下拖关闭、嵌套滚动和独立窗口背景。拉高普通警告弹窗不能表达这些交互；将时钟编辑业务放进库又会混淆通用组件与宿主职责。窗口、纯材质背景及清晰内容若共用同一个采样源，还会产生自采样或重复叠加。

## Decision

[ModalBottomSheet](../../../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt) 只提供展开与隐藏位置、遮罩、拖动、安全区及通用内容槽。visible 和退出决定归宿主，调用方持续装配直到退场完成；确认及草稿留在 Demo。SheetToolbar 是独立可选入口，按钮继续组合 TopAppBarButton。

普通底部窗口只执行位移与遮罩，不套用居中或锚点分支的缩放。程序开关的位移响应取可见面板高度、实际父容器高度与导航 peek，遮罩使用同一响应。手势回弹使用 Behavior 的响应，每帧先按移动前的位置和父容器高度计算遮罩，再放置面板；手势隐藏的位移按剩余可见高度取响应，Dialog 的独立遮罩动画承接当前透明度。不能把程序开关、手势回弹与手势隐藏合成一个动画分支。

窗口根高度、Coordinator 高度、面板完整测量高度与可见高度各有消费者。父容器遵守顶部边距及导航区域，不能直接用限制面板内容的工具值替代它。原厂测量先扩展底部 padding，计算可见高度时再减去；这部分属于屏外外形，不是内容安全留白。Compose 对父级报告可见高度，纯背景的裁剪和轮廓使用完整高度。正常反向保留速度，手势接管清除旧目标和速度；取消及未达到隐藏条件的释放回到展开位置，只有宿主接受退出才隐藏。

直接拖动沿 `onViewReleased` 检查双轴速度和越过中线的条件，速度样本复用公共平台跟踪器。ViewDragHelper 的直接 MOVE 逐次截断像素增量，再执行快速拖动调整，不能累积小数后才截断终点。嵌套滚动沿 `onStopNestedScroll` 检查剩余位移和面板最后消费的方向，子滚动容器剩余的甩动速度不用于决定方向。到达边界后的零消费 MOVE 仍刷新位置遮罩，不重新接管弹簧；嵌套回到展开位后也保留这一补帧路径。只有实际指针会话能启动嵌套拖动；Compose 的 UserInput 也用于键盘、鼠标滚轮和无障碍滚动，不能据此等待一个并不存在的释放回调。面板在子滚动的释放回调前处理系统取消，UP 后保留已有嵌套会话的收尾。Clock 两个 Fragment 的退出拦截属于编辑页业务，通用面板通过受控退出把决定留给宿主。

面板在 Initial 阶段记录首指，工具栏操作槽只标记自己的首指命中，不消费事件。面板的直接拖动和嵌套接管在本次触摸中尊重该归属，第二指加入也不改写它。按钮可继续处理点触光、按压、位移和点击。`nestedScrollEnabled`（默认 `true`）独立门控嵌套滚动接管，对应原厂 `COUIBottomSheetBehavior.setIsNestedScrollingCheckEnabled`：设为 `false` 时，内容区可搭配公共 [couiVerticalScroll](../../../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt) 保留上下双向越界回弹（`springBackTensionMultiple = 2.15f`）而不拉动面板，同时拖动条与工具栏非按钮区仍受 `draggable` 控制；滚动容器置于 `SheetToolbar` 下方时在 `couiVerticalScroll` 前加 `clipToBounds()`，避免上拉越界绘制覆盖工具栏。动态关闭 `nestedScrollEnabled` 时只取消进行中的嵌套拖动，不打断直接拖动。已经拖回展开点时，释放直接复位位移、遮罩及旧弹簧状态。离开展开点时，上甩仍按 `panelHeightVerticalMoving` 衰减越位。留白属于展开布局，裁剪应放在顶部留白之外的实际视口，避免截掉回弹时的圆角。

窗口与 Coordinator 使用普通布局。面板高度按父约束和窗口上限测量，父约束可能超过上限时读取系统可见窗口。首次 pre-draw 确认窗口已完成布局，运动协程从同一份快照读取各层实测高度和 IME 状态，再驱动普通底部入场。同一窗口的高度变化和反向继续使用现有运动通道。

[Demo 入口行](../../../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt) 的点击与列表反馈共用交互源。宿主接受对应面板的打开目标后，通过 `pressFeedback` 结束该行的按压蒙层，避免宿主列表的退场弹簧与独立窗口入场同时请求绘制。按压收尾由 Demo 的打开状态控制。

窗口根提供固定的采样坐标，宿主背景或窗口几何更新时录制材质。面板位移时，用同帧逆位移从窗口坐标的材质图取出可见部分。背景与清晰内容各自绘制，纯背景源保留面板内的材质结果。滤镜与录制层随材质组合建立和释放，固定尺寸下复用各轮绘制指令。

面板表面消费进入 Dialog 前取得的外层窗口源。Dialog 内部提供以外层源为 parent 的完整窗口源，供后续独立菜单和弹窗使用；工具栏和分类项则消费不含自身的纯面板背景源。已经提供窗口源的同窗口内容不再重复包装 provider。默认保留内容隔离；isolateContent=false 只用于已自行隔离目标混合的内容，无消费者时允许 Auto，实际浮层消费者仍恢复原分辨率离屏。

菜单可见边界携带所属窗口与稳定读取函数。主菜单在测量时取可见区域的宽高，在放置时取定位边界。完整面板四角先随动画转换到窗口，再与实际父视口求交；不能先裁静态可见高度再平移，也不能由面板底边反推父视口，以免上回弹或宿主 padding/offset/scale 破坏边界。无交集时保留视口内的零尺寸区域。同窗口的主菜单、子菜单和主菜单缩移使用这一可见区域；换算到 Popup 局部坐标后再定位。独立 Dialog 虽可继承 CompositionLocal，也不能使用外层窗口的矩形；未提供该 Local 的普通菜单行为保持。内容消费面板已处理的顶部和底部 inset，IME 只在窗口外层处理，避免重复安全留白。

外形复用 CouiShape，平滑上圆角与下半部矩形用 PathOperation.Union 合并。内容路径保留浮点半径，模糊区域与轮廓光采用 PixelOffset 取整，三个绘制阶段使用完整测量边界和两个直角下角。清晰内容绘制完成后，由公共 Contour 绘制 SHADOW_STYLE_2 并处理边缘覆盖率，线宽采用 PixelSize 取整。

Panel 工具栏按其窗口内 Drawable 分支选择按钮底色和 CAPSULE_8 / CIRCLE_4，胶囊与圆形的描边、内晕分别使用对应 Drawable 预设。TopAppBarButton 在公共浮动表面的材质之后、清晰内容之前装配公共 Contour，保留按压、跟手光与阴影；普通顶栏未选择该配置时保持自己的浮动轮廓。标题与菜单文字持有各自原厂角色和字体留白，不能借用警告弹窗按钮字号。主题只提供默认颜色与字体族，显式 Style 和标题配置优先。两侧操作先测量，居中标题对称避让较宽一侧；长标题可增加高度。

WheelPicker 的 visualWidth / visualAlignment 只控制数字绘制，不缩小输入列；TimeLimitPicker 与 MiniTimePicker 的选择线保留选择器自身的水平内缩，独立于外部卡片留白。闹钟类型组合 ChipTabRow，宿主分别提供 selectedColor 与 selectedContentColor，不由背景参数强制白色前景；默认中性材质不变，不将该组合声称为新增的原厂 Choice 控件。

## 原厂依据与适配

基准为 Clock 17.6.94，APK SHA-256 为 231b22b01cee3783259bdf896399308c0b9cd7e279b0ba226d6735f0075a778d。实际消费者是 AddAlarmPanelFragment 与 TimerPanelFragment，经 COUIPanelFragment / COUIBottomSheetDialog 装配；普通手机使用最大可用高度、隐藏拖动条，并设置 Panel 工具栏。Demo 只展示本地闹钟和倒计时草稿，不访问系统提醒或复制铃声、振动等完整子页。

doParentViewTranslationShowingAnim / isFadeInCenter 区分普通位移与居中缩放。initCoordinateInsets、IgnoreWindowInsetsFrameLayout、COUIBottomSheetBehavior.onMeasureChild、updatePaddingBottom、getTranslationDistance / getTranslationResponse 共同决定高度语义。手势沿 startSettleRunnable、calculatePanelOutsideAlpha 追到 PullUpToDismissPanelListener 与 dismissWithAlphaAnim。`COUIViewDragHelper.forceSettleCapturedViewAt` 在位移为零时直接完成，`panelHeightVerticalMoving` 对从展开点下方起步的高速上甩保留 40% 越位。实际 `design_bottom_sheet_dialog` 的容器和 Coordinator 都声明 `clipChildren=false`。`ToolbarMaterialEffectDelegate.handleTouch` 在工具栏触摸动画分支的 DOWN 调用 `requestDisallowInterceptTouchEvent`。关键公式及分支交叉核对对应字节码。

`COUIBottomSheetDialog.initBlurParams` 读取 `coui_bottomdialog_*`，`onAttachedToWindow` 将 type 2 背景模糊 Drawable 附到面板 View，首次 pre-draw 以实测高度启动普通入场。普通 `doParentViewTranslationShowingAnim` 将面板 alpha 置 1，只驱动位移与遮罩。AddAlarm 和 Timer 显式取 Clock 的 `elevatedWithCard` 作无模糊底色。该固件的 `appendOverlay` 和 `appendColorDodge` 均先调用 `appendMixShader`，按 mix alpha 逐通道插值 RGB，再执行对应的第二层混合；亮度混色位于 `appendGlowMixShader` 分支。[公共材质工厂](../../../../coui/src/main/java/com/itos/coui/effect/CouiMaterial.kt)由 Sheet 内部选择 RGB 路径。ToolbarMaterialEffectDelegate 的 Panel Drawable 分支、shadowedge 预设及映射器决定按钮几何，Title.Panel / HeadlineS 与 COUIMenuItemStyle 决定文字。

同一固件的 RenderEngine `BlurGenerator::generate` 将 type 2 分派到 `fastKawaseGenerator`。超过 250 的等级进入 `oneSixteenthKawaseBlur`。400 先减 26，以 `ceil(extent / 16) * 4`、`ceil(extent / 16)` 执行两次四点下采样，再做九轮五点滤波。公共 [Fast Kawase](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt) 用首轮的两倍网格承载四个取样点，以应用内背景源和公开 RenderEffect 表达该链。原厂 SurfaceFlinger 先将窗口矩形映射到显示区求交，将该范围传给 BlurRegion 的图像生成，随后按独立的区域参数绘制。滤波结果在两个小尺寸 Surface 间交替写入。固件提供 RenderEngine shader 预编译入口与 SurfaceFlinger 缓存，具体 Clock 命中情况仍需原厂运行时序。

同一固件的 Oplus API 值为 40。`COUIPanelPercentFrameLayout.initRadiusAndWeight` 的 NEW_G2 高版本分支读取 `coui_round_corner_xxxxl_radius_large_16_1` 与 `weight_16_1_large`，对应 45dp 与 3.0。`COUIBottomSheetDialog` 在该版本也从同一 radius 资源取值，普通手机 ratio=1 的 `refresBlurCorner`、`refreshShadowEdgeDrawableCorner` 将下角置零。`32dp` 来自旧分支的 `couiRoundCornerXXXXL`，更早的 smooth Outline 则解析主题的 25dp 和 1.1。`PanelPercentFrameLayout.draw` 和 `applyShadowEdgeParams` 确定轮廓及内晕的层序和预设。设备上参与 `v2.a.e()` 的动画等级及默认权重属性仍需读取，不能由固件版本推断其运行时门控。

TimeLimitPicker.onMeasure 与 NumberPicker.onDraw 说明宽点击列内部的数字位置。TimeLimitPicker.dispatchDraw 和 MiniTimePickerView.onDraw 说明选择线内缩。倒计时由 TimerSetFragment 实际加载 oplus_timer_settings，三列滚轮由 MiniTimePickerView 装配。普通手机两张卡片使用 20dp 圆角。第二张卡片在铃声与常用开关之间绘制分隔线，名称输入的起始位置由独立的顶部、侧边和底部留白决定。Demo 用主题色承载这些控件和本地草稿。同设备（PLK110，构建 `PLK110_17.0.1.201(CN01)`）核对 Clock 17.6.100（SHA-256 `8988224525c329a3de5ed6171e72cef2e08e871a8f02ff2a1bc64e9eab36af2f`）与 `com.oplus.linker` 17.20.11（SHA-256 `a39e574c00d8131d298c4d13abeb384b548de3c1a19c10e3c25662c0be38d954`）：Clock 的 `TimerSetFragment` 与 `AddAlarmFragment` 在面板内使用自定义 `ReboundScrollView`（`ScrollView` 子类，短内容不触发嵌套滚动，`dispatchTouchEvent` 按 `0.15f` 偏移并 200ms 线性回弹，外层 `COUIBottomSheetDialog` 在 `onShowAnimationEnd` 启用 `setDraggable(true)`）；设备互联的“我的远程设备”面板（`UnifyPanelActivity` → `OPPOPanelImpl` → `OPPOExpansionFragment`）调用 `setDraggable(false)` 与 `setCanPullUp(false)`，并以 `ScrollOnlyNestedScrollView`（继承 `COUINestedScrollView`，屏蔽纵向 `startNestedScroll`）提供标准 `SpringOverScroller` 回弹（`springBackTensionMultiple = 2.15f`，`overscrollDistance = heightPixels`）。Demo 的 Sheet 示例因此复用公共 `couiVerticalScroll`（同一 `2.15f` 参数），并设 `nestedScrollEnabled = false` 保留工具栏/手柄拖动与内容区双向弹性回弹。设备原厂控件树曾与相同 APK 的资源核对，当前移植的视觉和帧时仍需实机核验。

公开 Android/Compose 窗口与应用内背景源承担移植，设备仍需对照 OEM 窗口采样与裁剪的覆盖率。顶边回弹、全高度和半屏的浅深色中间帧都需要设备核验。当前支持普通底部的展开与隐藏位置，其余窗口和 Fragment 分支见使用页。

## Existing-note audit

- [警告弹窗](2026-10-24-coui-alert-dialog.md)：部分重叠，复用其公共效果与文字度量，保留独立的窗口和交互职责。
- [持久运动通道](../architecture/2026-10-29-persistent-motion-channels.md)：部分重叠，沿用重定向、硬重置与取消语义，不新增物理引擎。
- [宿主页面职责](../architecture/2026-11-03-host-owned-page-layout.md)：部分重叠，草稿、页面内容和完整窗口源归宿主，组件不引入业务依赖。
- [ChipTab](2026-10-24-chip-tab.md)、[选择器](2026-11-01-coui-pickers.md)、[菜单输入](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)：保留既有交互，本篇只持有面板消费者的可选样式和可见范围装配。
- 尚未投稿的本机 Sheet 调试记录只用于整理依据，不作为多篇历史记录进入此次新增组件 PR；无已发布同范围 Note 需要归档。

## Alternatives considered

M3 ModalBottomSheet 可快速提供窗口和拖动，但动画分支、背景采样与状态协议不对应目标原厂。将 DialogCard 拉高能复用布局，却会把警告操作与面板滚动混在一起，因此只复用效果，独立实现窗口和运动。

把平滑轮廓与矩形改为相同路径方向可消除孔洞，但依赖输入轮廓方向；真正并集更能保持组合语义。通过延长圆角矩形遮住下角能减少形状分支，却改变实际测量边界及内晕计算，不能替代完整边界与下直角。每帧通过 CompositionLocal 发布矩形便于读取，却会重组全部内容；携带窗口归属的稳定读取函数把动画状态留在实际消费者。冻结背景或减小模糊能减少工作，但改变材质，不采用。

## Consequences

库可独立承载底部编辑内容，并保持草稿、提交和业务导航的宿主所有权。代价是独立窗口与运动生命周期，以及 Panel 对 Contour、Picker、Chip 和菜单的最小可选支持。没有选择这些选项的现有消费者保持原行为。

静态与构建检查不能证明帧率或原厂逐像素等价。首次及重复开关、反向、拖动中断、IME、RTL、字体缩放、按钮和顶边中间帧、TalkBack 与嵌套浮层仍需同设备验收；实际执行结果写在 PR。
