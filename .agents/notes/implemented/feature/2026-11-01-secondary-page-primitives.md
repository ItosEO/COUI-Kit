# Agent Note: 设置式页面、常驻搜索与二级页装配

Status: implemented

## Problem

完整 Demo 需要设置式目录、折叠首页、常驻搜索和普通二级页面。库需要提供可组合的布局、滚动和反馈，同时继续保留最新主线的主题策略、背景隔离与透明绘制优化。

## Decision

库在 `com.itos.coui.layout` 包公开 `Group`、`Item`、`Divider`、`Footer`，另提供语义文字、折叠顶栏、常驻搜索和可选回弹。独立点击行与行布局的原选择用于拆开交互和排版；该公开拆分由[通用 Item 收敛](../simplification/2026-11-03-layout-item-api.md)替代，图示由宿主组合。布局族不为未合并的旧名称建立兼容别名。宿主持有查询、展开目标、导航、偏好、业务回调与滚动状态；不公开整页业务包装。

原先的页面容器与配色作用域决定由[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)替代。页面结构与采样装配由宿主选择，组件可独立与其他设计混用。折叠补偿仍只改变顶部留白，底部视口保持固定。

常驻搜索复用现有浮动后端，增加真实双主体装配与材质后内容前回调；没有第三个虚构节点或第二套 Shader。宽度、取消、标题和结果分别持有弹簧通道，复用现有 CouiSpring。宽度与取消的运行中目标重定向有原厂依据；页面与结果保留当前适配，候选 OEM setStartValue 的运行中语义尚缺实现材料，不能声称快速反向完全等价。取消只释放搜索自身的焦点/IME 并请求关闭；清空只更新查询，IME Search 先隐藏键盘再执行搜索。输入与展开分别受控，移除不清理宿主全局焦点。

列表反馈保持唯一点击 owner。父滚动接管、离界和 CANCEL 释放会话，窗口失焦继续最低进度及弹簧退场，禁用/销毁才硬重置。短页仍占满内容视口，通过同一个回弹入口接收剩余位移。分类栏和纵向回弹共享 CouiScrollPhysics 内核：分类栏保留原 3.2 张力倍数和 scrollView 临时位置分支，纵向按 RecyclerView 选择 2.15 和非 scrollView 分支；不复制两份 RK4 与惯性计算。普通二级页的 Intent、返回与系统默认 Activity 转场由宿主负责。

默认颜色在消费处读取 CouiTheme 与已初始化的可选 M3 策略；显式 colors 优先，原生 colors 工厂仍可单独使用。沿用最新主线的菜单按需背景隔离、组合期引用所有权和前景透明光晕短路，不整文件回退主题或性能实现。Sheet 与完整 Demo 属于其他范围。

渐进背景沿用具体节点持有 renderer 与横纵图层的实现，图层随 attach/detach 成对取得和释放。gradientSpeed 进入节点参数及 renderer 更新判定，变化时重建对应 Shader 配置；默认分支、两遍卷积、Density 更新和采样缓存规则保持。浮动表面的显式 renderer 与内容层继续遵守单节点所有权。

## 原页面容器决定（已取代）

初始 PR 用 CouiScaffold 统一测量顶栏和底栏、分离正文源与窗口源、消费横向安全区，并向正文回传 padding 与页面配色。该选择旨在减少整页示例的重复装配；稳定 PaddingValues 的实现参考 Miuix。后续明确“可单独取用并兼容其他设计”的核心边界后，这些通用页面职责回到宿主。实现与相关派生代码已移除，不把旧理由改写成当时从未采用过该方案。

## Evidence

Settings 17.0.0 / versionCode 17000000，APK SHA-256 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。HomepageImpl、SettingsCollapsingToolbarLayout.OffsetUpdateListener、SettingsSearchBar → COUISearchBar 的 instant-search 分支分别决定标题、搜索和结果动画。COUIPreferenceFragment.onCreateRecyclerView → COUIRecyclerView 决定列表入口；COUIPhysicalAnimationUtil 与 SpringOverScroller 确定越界映射和 RK4 收尾；ListSelectedItemLayout 与 COUIMaskEffectDrawable 确定最低高亮及取消规则。

上述哈希是初始实现记录的原始输入。本次复核可用的同版本候选哈希、Java/smali 证据、任务取消、触边交接及摩擦隔离由[二级页修复复核](../bug-fix/2026-11-02-secondary-page-review-fixes.md)持有；不能用候选材料替代原包的完整一致性验收。

研究材料只用于核对，不进入源码依赖。公开 Compose 承载输入、焦点和窗口，不承诺 OEM 特权窗口、专用字体、分屏嵌入及 RecyclerView 完整分发协议等价。

## Existing-note audit

- [主题策略](../architecture/2026-10-31-optional-material3-style.md)：沿用消费处解析与显式覆盖；新布局不重新维护根色板。
- [顶栏与搜索边界](../bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)：不同搜索场景；复用已有固定输入重定位边界，保留便签式顶栏搜索。
- [模块边界](../architecture/2026-10-21-coui-module-boundary.md)：局部布局、效果归库，页面结构、导航、示例和业务归宿主。
- [Modifier 与节点资源](../architecture/2026-11-02-modifier-contracts-and-picker-layout.md)：复用渐进背景节点生命周期，新增参数沿现有更新路径传递；Picker、面板几何及其 Demo 保留已合入的实现。
- [宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)：部分替代本篇页面容器与配色作用域决定，原厂组件及效果依据继续保留。
- 本地设置研究与上述决定部分重叠，本篇记录投稿范围；活跃 proposed/rejected 无同范围需要清理的记录。

## Alternatives considered

- 继续平铺 Demo 能立即操作所有样例，但无法表达完整二级页面和目录导航，按用户确认改为可组合页面能力。
- 用平台 stretch 或 Compose spring 可减少物理代码，但算法与目标 RK4、整数映射及时间规则不同，保留原厂内核。
- 复制浮动 Shader 可单独控制搜索绘制，却增加同源算法；以双主体参数和内容回调扩展已有后端。

## Consequences

宿主可独立组合设置页面与常驻搜索，也可关闭回弹或传入明确颜色。代价是需要正确消费安全区、保持稳定 renderer 和单一输入 owner；当前不包含具体路由或业务页面。静态检查和构建仅验证其覆盖范围；动画中段、字体、搜索 IME、反向/多指、RTL、键盘、TalkBack、GPU 和性能仍需设备对照。
