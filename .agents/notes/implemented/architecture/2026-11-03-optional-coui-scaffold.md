# Agent Note: 可选的 Foundation 页面容器

Status: implemented

本篇的显式采样与无浮动操作槽选择由[Scaffold 正文源](2026-11-04-scaffold-body-backdrop.md)部分取代；栏位测量、安全区及可选容器的决定继续有效。

## Problem

纯 COUI 应用需要顶栏、底栏与正文布局，但不应因此依赖 Material 3。完全交给宿主实现会重复栏位测量和 Insets 消费，Demo 首页与详情页也有同一需求。

## Decision

核心 layout 包提供可选 `Scaffold`，只处理栏位测量、正文 Padding、安全区和背景色。它使用 Compose UI / Foundation，背景默认读取 CouiTheme；不创建主题 Provider、采样源、页面状态或导航。

容器先应用并消费横向安全区，再测量栏位。正文顶部和底部 Padding 使用对应栏位的实测高度；空槽或零高度槽使用父级消费后剩余的纵向 Insets。栏位负责自身纵向安全区。正文保留完整视口，先于顶栏、底栏绘制，使内容可进入模糊栏位下方。

内部稳定 PaddingValues 在正文子组合和测量前更新；正文 lambda 以实际 content 为 key 保持身份，使折叠补偿能读取当前测量值。容器要求有界宽高，不推断无界页面尺寸。

默认 Insets 包含系统栏和屏幕缺口。IME 由宿主在容器外消费，以缩小真实滚动视口。首页和详情页使用同一容器，采样、搜索门控和业务状态仍由 Demo 装配。

这是已采用的通用 Compose 布局能力，不对应 OEM 页面管理或特权窗口协议。用法由[页面容器](../../../../docs/components/scaffold.md)持有。

## 与既有决定的关系

本篇部分取代[宿主页面所有权](2026-11-03-host-owned-page-layout.md)中“不提供 Scaffold”的选择，保留其主题、导航与显式采样分工。它也接管[二级页组件](../feature/2026-11-01-secondary-page-primitives.md)中的通用容器职责；[输入修复记录](../bug-fix/2026-11-02-secondary-page-review-fixes.md)中的早期 Miuix 派生实现保持历史含义。

[通用 Item](../simplification/2026-11-03-layout-item-api.md)、[设置式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)及[输入连续性](../bug-fix/2026-11-03-page-input-continuity.md)分别持有行交互、路由与 IME 分工，本容器不改变这些决定。

## Alternatives considered

- 每个宿主组合 Foundation 布局能保持最小库接口，但需重复测量与安全区处理，采用有两处实际消费者的可选容器。
- M3 Scaffold 提供成熟测量与更多槽位，但会增加纯 COUI 应用的设计体系依赖。
- 恢复早期完整容器可一并减少采样和配色代码，却重新绑定页面主题与窗口源，因此只恢复布局职责。
- Column 顺序排列栏位更短，但缩小正文视口，不能保留栏位下方的内容采样，采用分层测量与 Padding。

## Consequences

纯 COUI 页面有无 M3 依赖的布局入口，混合应用仍可保留原容器。库承担栏位测量与 Insets 维护成本；宿主继续负责纵向栏位安全区、正文 Padding、IME 和采样。不增加专用设置页、Snackbar、FAB、Drawer 或导航槽，也不保留开发分支旧容器的兼容包装。

实现时的静态检查覆盖 Kotlin PSI、接口、调用点和文档。临时 JVM 布局替身检查过空槽、单栏、双栏、零高度、动态高度、剩余 Insets、密度及无界约束等十种情况；替身只验证提取的测量 lambda，不执行真实 Compose Insets 传播或 GPU，检查器未作为仓库工具保留。

实际 CI 结果按提交记录在 PR。键盘动画、横屏缺口、三键/手势导航、RTL、折叠滚动与模糊合成仍需设备验收。
