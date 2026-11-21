# Agent Note: 悬浮操作栏只绘制一个胶囊表面

Status: implemented

## Problem

宿主需要一个容纳多个同级操作的悬浮胶囊，行为对齐 M3、Miuix 的 FloatingToolbar。参考图里有一个多彩图标，容易被理解成独立的醒目按钮；若按逐项独立表面实现，每个操作都会创建自己的材质、投影与 renderer，既增加绘制成本，也与参考图中统一的整条胶囊不符。操作数量、宽度和图标配色也不能由组件预设。

## Decision

[FloatingToolbar](../../../../coui/src/main/java/com/itos/coui/floatingtoolbar/FloatingToolbar.kt) 与 `FloatingToolbarItem` 分工：容器绘制一次单主体浮动表面，并持有整条栏的跟手光、位移与形变；操作项只负责点击、悬停与焦点。

- 容器使用 `CouiFloatingRenderer.standalone()` + `Modifier.couiFloatingSurface`，整个 Row 录制到同一个内容层。没有第二个主体，也没有逐项表面。
- 跟手光、`couiTouchMotion`、`couiPressMotion` 都挂在整条胶囊上，装配方式与底栏中间胶囊（`NavigationBar` 的 `FloatingNode`）一致：光画在内容之下、随触点在栏内移动；表面几何与内容层读同一组弹簧值，材质和图标不会错位。操作项不各自叠光或形变，否则相邻项会各自亮起、缩放也会与整条玻璃分叉。
- 拖动由操作栏接管（`consumeDrag = true`），与底栏一致：拖动时胶囊位移、拉伸而不带动页面滚动；形成拖动后子项 `clickable` 因移动被消费而取消，只有静止点按才提交。
- 材质与投影直接复用 `CouiTopAppBarButtonDefaults.systemMaterial(dark, capsule = true)` 与 `shadow(..., capsule = true, search = true)`，即 CAPSULE_1 系统胶囊。没有使用项目对顶栏按钮的加深特化 `material()`：那条特化以 OVERLAY/COLOR_DODGE 为主，在不透明深色页面上会接近透明，与参考图中可见的深灰胶囊不符。
- 系统胶囊在顶栏里的 `blurRadius` 为 0，因为顶栏按钮所在背景本身已经模糊；操作栏浮在正文上，默认在材质上补 20px 背景模糊（与库内搜索胶囊同一档），否则正文透过后仍可辨认，玻璃读不出层次。模糊仍由 `CouiFloatingMaterial.blurRadius` 的既有 RenderEffect 链执行，不新增模糊实现；宿主可改材质半径，`0f` 关闭。
- `content` 是完整的 Row 插槽，`FloatingToolbarItem` 只是常用的可点击项；组件不为项目数预留槽位，也没有主次或强调变体，颜色不表达层级。
- 操作项宽 44dp、高 40dp，宿主可用 `Modifier.width` 加宽；默认样式 `contentColor` 取当前模式的 `onSurface` 压到 80% 作为兜底，图标显式 `tint` 优先，需要整栏着色时由宿主覆盖。
- 兜底内容色是必须的：操作栏常浮在任意背景（含彩色卡片）之上，完全依赖环境内容色会把没有自带颜色的图标交给上游决定，出现与页面正文不一致的颜色。
- 兜底色压到 80% 是因为正文色本身是 90% 白/黑，直接压在透明材质上偏重；多色图标与自带配色的图标仍由自身 `tint` 决定，不受该透明度影响。
- 禁用态只对内容应用 0.38 透明度，不改写颜色；这样即使宿主没有指定内容色，禁用状态仍然可见。
- 栏高 44dp、项 44×40dp、项间 4dp，两项以上两端各 6dp：四项 200dp、两项 104dp，与原厂 `coui_floating_toolbar_*` 一致。早期按真机观感把栏高调到 48dp、取消项间距，只能近似比例；反编译原厂资源后改回精确尺寸，图标到边缘 16dp。图标取 24dp，与系统胶囊的图标中心间距一致。
- `themeTint` 跟随 `CouiTheme.materialTintFor(dark)`，与底栏、菜单、搜索栏的 M3 染色契约一致。
- 进出场按反编译的 `ItemViewAnimatorImpl` 建模：0–200ms 用线性 ValueAnimator 把 `mGapScale` 推到 0.75（删除时有效间距 1.75×、插入时 0.25×），随后 item spring（response 0.5 / bounce 0.4）与 gap 回程并行。没有独立的宽度弹簧：原厂 `onMeasure`/`onLayout` 在收到新项列表后立刻按最终可见项定宽，动画期间 `updateAllAnimatingViews` 只重排每个子 View 的 `setX()` 与 `setScaleX()`，可见胶囊 `mBackgroundBounds` 由首尾 View 的 x/scale 反算。
- Compose 侧把这套「首尾反算」等价为逐帧槽位求和：每项槽位 = `scale·width` + 两侧各 `scale·有效间距/2`，首尾项不留外侧半间距；胶囊宽度 = 槽位之和 + 两端内边距。内容与边界读同一组 `gap`/`progress`/角色，因此图标与玻璃不会分叉，也不需记录 `positionInRoot`。
- 弹簧进度归一化到 0..1 时，`CouiSpring.minimumChange` 必须按原厂单位换算：原厂 `mProgress` 是 0..10000、`COUISpringAnimation` 默认最小可见变化 1，对应 0..1 的 `0.0001f`。早先直接写 `0.5f`，弹簧约 90ms 就被判定收敛，`onEnd` 立刻把状态拨回静止态，图标和胶囊瞬间跳到终态、gap 补间却仍在跑——「动画整个乱掉」正来自这里。
- 项角色（进入/退出/保留）必须在 `visible` 变化的同一次 composition 中登记，不能留到下一帧的 `LaunchedEffect`：否则切换当帧 `itemScale` 已按新 `visible` 返回 0/1，会先闪一下。同一帧多项一起切换时按一批处理，共用一条进度。
- `phase` 回到静止态要等 item spring 与 gap 回程两条都收敛；只等 spring 会让间距在收尾突然归零，多出一次宽度跳变。
- 弹簧超调（progress > 1）时退出项也必须有回弹：`handleItemState` 返回的是未钳制的 `1−progress`（可为负），`updateAllAnimatingViews` 据此把退出项左右半间距改成 `5·(1−progress)·eff`（`GAP_ANIMATION_MULTIPLIER`），形成一份负的水平推进量。Compose 侧把这份负推进量摊到整排有效间距上；只把退出项的视觉 scale `coerceAtLeast(0f)` 会让收缩收到终点就停住，用户实测反馈「收缩缺少弹性」正来自这里。进入项本身可以越过 1，扩展方向的弹性不需要这一项。
- 内容不能把 `(width + space)` 整体乘 `scale` 当槽位：`scale<1` 时槽位比 44dp 的内容还窄，内容被挤出槽位并跳变。内容始终按原始尺寸测量，缩放交给内层 `graphicsLayer`，`place` 时抵消 `(1−scale)·width/2`，可见左边缘才落在算好的位置。
- 不采用材质横向 `scaleX` 冒充宽度变化，也不采用 `Modifier.animateContentSize`：前者只放大一层模糊壳，后者让内容坐标停在旧尺寸上。

## 原厂实现复核

系统便签 17.0.30 自带这套栏（`com.coui.appcompat.bottomfloatingtoolbar` + `animation.touchanimation` + `spotlight`），反编译后逐项核对，触摸层与灯效层的数值与本组件一致，无需改动：

- 橡皮筋 `D=28dp`、`c=0.05`，形变 `150dp`、曲线 0.55、最大缩放 1.2（按面积与宽高比衰减到 1.03），死区 5px，拖动跟随 0.15/0.15。
- 抬手回弹 response 0.42（小面积）→0.45（大面积），bounce `max(0.65, |v|/1000)`，初速 `v/28dp` 限 ±1000；`TouchMotionDebugFlags.useNormalizedHandUpVelocity` 默认为 true，走的正是这条归一化分支，与 `CouiTouchMotion.release` 相同。
- 按压 response 0.25 / bounce 0、释放 0.5 / 0.6、缩放 0.85–0.97，按按压面积与宽高比取，与 `CouiPressMotion` 相同。
- 跟手光走 `COUISpotLightEffectDrawable` 的 `TYPE_BOTTOM_NAVIGATION`，映射到 `TYPE_OPAQUE_MEDIUM_2`（数值与 `TYPE_OPAQUE_SMALL_2` 完全相同）：白色、半径 100→500px、跟手 0.35/0.15、点亮 0.15/0、熄灭 0.4；`coui_floating_toolbar_menu_touch_effect_radius` 只被已关闭的自绘 shader 兜底路径使用。`SpotlightPreset.OpaqueSmall2` 与之一致。
- 两处修正：操作项补回按压蒙层（原厂每项一次 bounded ripple，此前被整条栏的形变取代）；项弹簧回弹由 0.35 改为原厂的 0.4。
- 布局按原厂资源重排：高 44dp、项 44×40dp、项间 4dp、图标 24dp、两项以上两端各 6dp；操作项不再叠 `minimumInteractiveComponentSize`（否则每项被撑到 48dp，胶囊比参考宽 4dp），需要更大点击区由宿主加尺寸。
- 精读 `ItemViewAnimatorImpl.updateAllAnimatingViews` 与 `COUIBottomFloatingToolbarMenuView.updateAndDrawBackgroundRect` 后确认双轨模型：`mGapScale` 不改 measured/layout 宽度，只重排子 View 的 x；`mBackgroundBounds` 由首尾 View 的 `getX()`/`getScaleX()` 反算。据此删除独立宽度弹簧，改为逐帧槽位求和。
- 进出场补上原厂两段节奏：删除时 200ms 把间距撑到 1.75 倍、插入时压到 0.25 倍，随后 item spring 与 gap 回程并行。隐藏项缩放为 0 时槽位连同两侧半间距一起归零，不会留下空档。
- 两端内边距固定取 `edgeInset`（6dp），不再用 `edgeInset − itemSpacing/2` 补偿：新槽位只有相邻侧带半个间距，两端直接补 6dp 才是原厂 `mMenuViewItemBgInsetHorizontal`。
- 反馈蒙版按 `COUIBottomFloatingToolbarItemView.applyBackgroundMaskRect` 还原：多项时 `mFloatingToolbarMaskHorizontalOutset` = `coui_floating_toolbar_item_view_space` = 4dp，蒙版左右各外扩 4dp；单项时 `coui_floating_toolbar_item_single_mask_inset_horizontal` = 2dp，改为内缩 2dp；圆角取项高一半。hover / focus / press 共用这一个矩形，早先误把 6dp 的两端内边距当成 ripple 内缩。`couiToolbarFeedback` 用可负的 `maskInset` 统一表达。
- 真机（PLK110，1272×2772 @560dpi）抽帧逐帧核对：6→4 项为 997→1051px（间距扩张，5×14px×0.75≈52.5px）→660→645px（压过头）→663px 静止；4→6 项为 663→631px（间距压缩）→1013px（涨过头）→996px 静止。两个方向都有回弹，图标质心全程与边界同步，没有突变帧。
- 材质仍保留差异（不在动画范围）：原厂兼容路径 30px 模糊 + 两层混色、系统路径 25px，本组件沿用 CAPSULE_1 加 20px。

## Alternatives considered

- 组合四个 `TopAppBarButton` 或 `Fab`：会得到四个彼此分离的圆形表面与四个 renderer，与参考图中一体化胶囊不符，也违反基础效果复用规则。
- 把 `systemMaterial` 的参数复制进新 Defaults：值等价，但后续 CAPSULE_1 调整会分叉，属于跨组件复制混色。
- 为操作项补充独立材质或选中态底色：参考图没有该状态，且会把多彩图标重新变成醒目按钮。
- 每个操作项各挂一份 `rememberCouiSpotlight`：真机对比显示相邻项会各自出现光斑，与参考图「整条栏一处跟手光」不符，故改为栏级一套。
- 用隐藏的 `clickable` 加自定义拖动重定向（底栏的 release-target 模型）：操作栏是一次性提交而非选中，拖动重定向会让误滑触发别的操作，保持轻触提交、拖动只形变更安全。

## Consequences

- 组件不新增 Shader、模糊、轮廓或投影实现，只新增布局与装配，退场弹簧复用 `CouiSpring`；渲染成本为一个单主体。
- 每个操作栏持有 1 个 `CouiTouchMotion`、1 个 `CouiPressMotion` 与 1 套跟手光状态，与底栏中间胶囊同量级，不随项目数增长。
- 新 Defaults 跨组件族引用顶栏胶囊预设。若将来提取共享「系统胶囊」入口，应原地改引用，不复制数值。
- 胶囊外观以库内 CAPSULE_1 与参考截图为准，没有取得对应 OEM 构建的证据；真机颜色、阴影与深浅色仍需设备验收。

## Existing-note audit

- [胶囊分段指示器](2026-11-07-coui-segmented-control.md)：部分重叠，同样复用单主体浮动后端；本篇只新增容器装配与操作项反馈，不翻转其决定。
- [顶栏展开式搜索](2026-11-04-topbar-search-fused-action.md)：不重叠，双主体转场与本组件无关。
