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
- 操作项最小宽度 48dp、高度填满栏高，宿主可用 `Modifier.width` 加宽；默认样式 `contentColor` 取当前模式的 `onSurface` 压到 80% 作为兜底，图标显式 `tint` 优先，需要整栏着色时由宿主覆盖。
- 兜底内容色是必须的：操作栏常浮在任意背景（含彩色卡片）之上，完全依赖环境内容色会把没有自带颜色的图标交给上游决定，出现与页面正文不一致的颜色。
- 兜底色压到 80% 是因为正文色本身是 90% 白/黑，直接压在透明材质上偏重；多色图标与自带配色的图标仍由自身 `tint` 决定，不受该透明度影响。
- 禁用态只对内容应用 0.38 透明度，不改写颜色；这样即使宿主没有指定内容色，禁用状态仍然可见。
- 栏高 48dp 与单项最小宽相同，四项即 192dp 宽且无横向内边距，对应参考图约 4:1 的胶囊比例；真机反馈 56dp 偏大（52dp 仍偏大），收到 48dp。图标取 24dp：系统胶囊的图标中心间距同为 48dp、图标约 24dp，操作项内两侧各 12dp；早先从 48dp 栏高降到 20dp 图标会让同一间距下显得空。
- `themeTint` 跟随 `CouiTheme.materialTintFor(dark)`，与底栏、菜单、搜索栏的 M3 染色契约一致。

## Alternatives considered

- 组合四个 `TopAppBarButton` 或 `Fab`：会得到四个彼此分离的圆形表面与四个 renderer，与参考图中一体化胶囊不符，也违反基础效果复用规则。
- 把 `systemMaterial` 的参数复制进新 Defaults：值等价，但后续 CAPSULE_1 调整会分叉，属于跨组件复制混色。
- 为操作项补充独立材质或选中态底色：参考图没有该状态，且会把多彩图标重新变成醒目按钮。
- 每个操作项各挂一份 `rememberCouiSpotlight`：真机对比显示相邻项会各自出现光斑，与参考图「整条栏一处跟手光」不符，故改为栏级一套。
- 用隐藏的 `clickable` 加自定义拖动重定向（底栏的 release-target 模型）：操作栏是一次性提交而非选中，拖动重定向会让误滑触发别的操作，保持轻触提交、拖动只形变更安全。

## Consequences

- 组件不新增 Shader、模糊、轮廓或投影实现，只新增布局与装配；渲染成本为一个单主体。
- 每个操作栏持有 1 个 `CouiTouchMotion`、1 个 `CouiPressMotion` 与 1 套跟手光状态，与底栏中间胶囊同量级，不随项目数增长。
- 新 Defaults 跨组件族引用顶栏胶囊预设。若将来提取共享「系统胶囊」入口，应原地改引用，不复制数值。
- 胶囊外观以库内 CAPSULE_1 与参考截图为准，没有取得对应 OEM 构建的证据；真机颜色、阴影与深浅色仍需设备验收。

## Existing-note audit

- [胶囊分段指示器](2026-11-07-coui-segmented-control.md)：部分重叠，同样复用单主体浮动后端；本篇只新增容器装配与操作项反馈，不翻转其决定。
- [顶栏展开式搜索](2026-11-04-topbar-search-fused-action.md)：不重叠，双主体转场与本组件无关。
