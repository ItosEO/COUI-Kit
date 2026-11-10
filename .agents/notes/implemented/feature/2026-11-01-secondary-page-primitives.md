# Agent Note: 设置式组件、常驻搜索与折叠顶栏

Status: implemented

## Problem

设置式目录需要分组、行布局、折叠标题、常驻搜索和边界回弹。这些能力应能由宿主独立组合，并复用已有主题、背景采样与绘制后端。

## Decision

库提供局部布局、语义文字、折叠顶栏、常驻搜索与可选回弹。查询、展开目标、导航、偏好、业务回调与滚动状态由宿主持有。布局族当前入口为 `com.itos.coui.layout` 下的 Group、Item、Divider、Footer；行内容槽合并和图示移除由[通用 Item](../simplification/2026-11-03-layout-item-api.md)持有。

折叠顶栏用可空搜索槽区分两种装配：传入槽位时保留设置首页的标题退场与搜索停靠；省略或传 null 时，大标题缩为固定小标题，不降低透明度，短页面也允许保留收起状态。空 lambda 仍属于搜索式布局，不能据其测量高度猜宿主意图。折叠状态按当前槽位配置与密度更新行程，并按比例迁移已接受位置、正文补偿和搜索恢复位置；不让状态工厂反复恢复旧行程。无搜索时忽略搜索状态，避免调用方保留的展开目标锁住普通滚动。

独立标题的依据为便签 17.0.24 的 `collapsing_floating_appbar_layout` / todo 版、`COUICollapsingToolbarLayoutStyle`、`COUICollapsedToolbarStyle` 以及 `COUICollapsingTextHelper` 的非 fade 分支。布局未装配常驻搜索，默认同色标题仅插值位置和字号；有持久副标题时使用另一组折叠字号，当前组件不装配该分支。紧凑布局的起始边距、展开顶部位置与标题字号来自同包资源，插值使用 `COUIEaseInterpolator`。公共实现使用公开 Typeface 与 Compose 安全区，暂不接入原厂响应式大屏边距；字体文件和动画像素仍需设备验收。

LargeTopAppBar 的起始边距与搜索式首页对齐，折叠时过渡回紧凑位置。展开字号参考设置 17.0.0 的首页：`homepage_toolbar` 通过 `SettingsCOUICollapsingToolbarLayoutStyle` 使用 `SettingsCollapsingTitleExpanded`，其字号覆盖便签的通用预设。带导航按钮时，默认收起字号与普通顶栏一致。折叠全程缩放同一标题，默认保持同一字体和字重，避免在端点出现笔画突变。两个端点的像素字号分别解析后再计算缩放比例，保留系统非线性字体缩放。CollapsingTopAppBar 的无搜索分支继续保留便签排版。

普通大标题的背景也使用便签分支，不能混入搜索首页的滚动淡入。便签 `note_list_fragment`、`fragment_todo_list` 与 `todo_list_layout` 的 AppBarBlurOverlayView 未启用 scrollDrivenAlphaEnabled，构造默认 alpha 为 1、固定延伸 8dp。AppBarBlurOverlayBehavior.updateOverlayBounds 读取 AppBar 的 top 与 bottom，COUIViewOffsetHelper 通过 offsetTopAndBottom 平移整个 AppBar；背景保持完整高度随之移动，而非按剩余栏高缩放。页面入口经 NoteAppBarBlurUtil.resetToPageThemeDefaults 清除参数覆盖，采用 AppBarBlurParams 默认的 30px→0、speed=3。Java 的浮点值与构造分支已用 Droid ASC 和 JADX 交叉核对。

移植在同一测量阶段读取标题实际高度与顶部延伸，固定下沿延伸，避免收起端点切换背景尺寸。这样滚动只改变背景位置，不会因剩余栏高变化而重建渐进效果的尺寸快照。

搜索式首页同样保持完整背景高度，延伸取已有搜索预设，不在收起端点或搜索活动切换时重设尺寸。淡入长度和搜索活动时的显示规则保留，但滚动淡入读取正文位移，而非滞后的标题弹簧位移：正文 Padding 最终由 contentOffsetPx 决定，背景必须在正文进入顶栏时同步出现。此处属于针对 Compose 双位移装配的连续性适配，不将该透明度输入规则表述为已确认的 OEM 行为；标题、搜索与投影继续使用各自的动画进度。

常驻搜索扩展现有浮动后端的真实双主体装配和材质后、内容前回调。形状、材质、轮廓、投影、跟手光及融合复用公共效果，不复制 Shader 或以第三个假节点凑数量。项目选用带背景模糊的 TYPE_FRAMEWORK_TOP_BAR_BLUR，区别于原厂默认的零半径材质分支。

双主体低层入口继续公开，与单主体和三主体装配保持一致，供宿主自定义主体几何与内容；普通搜索用户使用 SearchBar。只因当前仓库主要由搜索消费就改为 internal，会撤回已选定的低层自定义能力，因此不收窄该接口。

宽度、取消、页面与结果分别使用 CouiSpring 通道。取消释放搜索自身的焦点与 IME 后请求关闭；清空只更新查询，IME Search 先隐藏键盘再调用业务。输入、绘制与配置变化的交接由[共享效果记录](../bug-fix/2026-11-02-secondary-page-review-fixes.md)持有；页面和结果的快速反向尚缺 OEM setter 完整证据。

搜索 enabled 控制输入、操作和触摸反馈，复用 Foundation 的禁用语义；禁用释放自身焦点，不改宿主持有的查询与展开目标。重新启用仍展开的搜索会请求焦点。结果列表与返回处理仍由宿主决定。取消文字使用同步系统 Typeface，宽度缓存覆盖文案、样式、密度、布局方向与 TextMeasurer；这只减少重复测量调用，不作为帧率收益结论。

列表反馈由唯一点击所有者驱动。父滚动接管、离界和 CANCEL 释放会话；窗口失焦保留最低高亮与弹簧退场，禁用或销毁才复位。短页占满视口，使用与长页相同的输入装配，详见[输入连续性](../bug-fix/2026-11-03-page-input-continuity.md)。

分类栏与纵向回弹共享 CouiScrollPhysics：分类栏使用 3.2 张力倍数和 scrollView 临时位置分支，纵向使用 RecyclerView 的 2.15 及非 scrollView 分支。两者共享 RK4 与惯性计算，按实例配置参数。

主题在消费处解析，显式参数优先。菜单的按需背景隔离、组合期引用所有权和透明光晕短路沿用主线。渐进背景的 renderer 与图层归各节点；`gradientSpeed` 沿现有参数更新与缓存失效路径传递，保留两遍卷积和 density 更新。低层浮动接口仍要求 renderer 与内容层归单个节点。

## 页面容器的决定关系

早期 Scaffold 把栏位测量、安全区、正文/窗口采样和页面配色集中装配，目的是减少整页示例的重复代码。[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)解除这些绑定；[可选轻量页面容器](../architecture/2026-11-03-optional-coui-scaffold.md)提供独立的通用测量与 Insets，不恢复页面配色作用域。[Scaffold 正文源](../architecture/2026-11-04-scaffold-body-backdrop.md)管理 COUI 容器独立槽位的默认源，第三方容器仍显式接入。折叠补偿只改变正文顶部留白，底部视口保持固定。

## Evidence

初始材料为 Settings 17.0.0 / versionCode 17000000，APK SHA-256：`d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5`。

- HomepageImpl、SettingsCollapsingToolbarLayout.OffsetUpdateListener、SettingsSearchBar → COUISearchBar 的 instant-search 分支决定标题、搜索和结果动画。
- COUIPreferenceFragment.onCreateRecyclerView → COUIRecyclerView 确认列表入口；COUIPhysicalAnimationUtil 与 SpringOverScroller 提供越界映射和 RK4 收尾。
- ListSelectedItemLayout 与 COUIMaskEffectDrawable 提供最低高亮和取消规则。

可复核同版本候选的哈希、Java/smali 证据与限制见[共享效果记录](../bug-fix/2026-11-02-secondary-page-review-fixes.md)。候选不能替代目标原包的完整验收。研究材料不进入运行依赖，公开 Compose 承担输入、焦点和窗口承载。

既有约束分别见[主题策略](../architecture/2026-10-31-optional-material3-style.md)、[顶栏搜索](../bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)、[模块边界](../architecture/2026-10-21-coui-module-boundary.md)及[节点资源](../architecture/2026-11-02-modifier-contracts-and-picker-layout.md)。

## Alternatives considered

- 平铺 Demo 能直接操作全部样例，但不能展示目录和完整二级页面，采用宿主导航与局部组件组合。
- 平台 stretch 或 Compose spring 能减少代码，但与目标 RK4、整数映射和时间规则不同，保留已验证内核。
- 独立搜索 Shader 能单独控制绘制，却重复同源算法，扩展现有浮动后端的主体数量与内容回调。

## Consequences

宿主可独立使用局部组件、关闭回弹或覆盖颜色；代价是正确组合 Insets、采样、滚动和输入。普通用法见[宿主页面接入](../../../../docs/guide/secondary-pages.md)。

公开承载不包含 OEM 特权窗口、专用字体、分屏嵌入及完整 RecyclerView 协议。动画中段、IME、反向/多指、RTL、键盘、TalkBack 和 GPU 行为仍需目标设备对照。
