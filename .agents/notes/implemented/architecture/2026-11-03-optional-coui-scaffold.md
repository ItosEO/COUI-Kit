# Agent Note: 可选的 Foundation 页面容器

Status: implemented

## Problem

纯 COUI 应用不应为页面布局引入 Material 3。完全移除页面容器后，宿主需要自行维护顶栏与底栏测量、正文 Padding 和 Insets 消费；Demo 的两种页面也重复这些装配。用户要求恢复通用轻量容器，同时保留组件独立取用的能力。

## Decision

`CouiScaffold` 位于核心 layout 包，使用 Foundation 和 Compose UI，仅提供 topBar、bottomBar、content、containerColor 与 windowInsets。背景默认读取现有 CouiTheme；不增加主题 Provider、采样源或 M3 依赖，不提供 CouiSettingsScaffold、Snackbar/FAB/Drawer 槽位或页面状态管理。

容器先应用并消费横向系统安全区，再测量栏位。纵向 Insets 由对应栏位处理，正文 Padding 使用栏位实测高度；空槽或零高度槽使用父级消费后剩余的纵向 Insets。正文保留完整视口，先绘制正文，再绘制顶栏和底栏，供显式装配的 Backdrop 采样。容器不裁剪顶栏的延伸效果。

PaddingValues 使用稳定的内部可观察对象，在正文子组合和测量前更新；正文 lambda 以真实 content 为 key 保持身份。这样折叠顶栏的 contentPadding 包装能读取本帧高度，且不因容器每次测量重新创建入口。宽高要求有界，容器不伪造无界约束下的页面尺寸。

默认系统 Insets 包含系统栏与屏幕缺口，IME 由宿主在滚动容器外通过 imePadding 缩小视口。Demo 首页和二级页采用新容器并删除重复的横向安全区装配，保留原有 IME、折叠补偿、搜索门控、窗口与正文采样分工。

这是用户批准的通用 Compose 布局能力。它不声称复刻 OEM 页面管理或窗口协议，也不改变已有 COUI 视觉与输入算法。普通用法由[页面容器文档](../../../../docs/components/scaffold.md)持有。

## Existing-note audit

- [宿主页面所有权](2026-11-03-host-owned-page-layout.md)：部分取代“不提供 Scaffold”的决定；独立组件、主题、导航与显式采样边界保留，双方互链。
- [二级页组件](../feature/2026-11-01-secondary-page-primitives.md)：部分重叠，保留原厂组件依据和早期容器历史；新的容器只提供布局，双方互链。
- [二级页修复复核](../bug-fix/2026-11-02-secondary-page-review-fixes.md)：保留旧实现的研究记录。新容器采用稳定 PaddingValues 与子组合的一般模式，不恢复已移除的页面主题或 Miuix 派生文件，双方互链。
- [通用 Item](../simplification/2026-11-03-layout-item-api.md)：只更新布局包的组件范围描述，原有行交互决定保持。
- [设置式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)：同步页面容器的实际调用，导航、状态和图标决定保持。
- [页面输入连续性](../bug-fix/2026-11-03-page-input-continuity.md)：沿用 IME 在视口外消费的规则，未改变输入节点与滚动算法。
- 其余组件和效果 Note 无关；活跃 proposed/rejected 中没有同范围待清理的提案。

## Alternatives considered

- 宿主直接组合 Foundation 布局可保持最小库接口，但每个纯 COUI 应用都要重复测量和安全区契约。保留可选容器能覆盖两处实际 Demo 调用，其他宿主无需迁移。
- 沿用 M3 Scaffold 已有成熟测量和丰富槽位，但要求纯 COUI 应用引入额外设计体系，不能满足用户的依赖边界。
- 恢复旧完整容器可以同时减少采样和配色代码，但会重新绑定独立组件、页面主题与窗口源。仅恢复布局职责。
- 用 Column 按顺序摆放栏位更短，但会缩小正文视口，无法保持顶栏和底栏下方的滚动内容采样。采用分层布局与实测 Padding。

## Consequences

纯 COUI 页面拥有无 M3 依赖的公共入口；混合应用仍可保留现有 Scaffold。代价是库需要维护栏位测量与 Insets 契约。自定义顶底栏继续负责对应纵向安全区；正文应用并消费返回 Padding，采样和 IME 仍按宿主布局组合。现有开发分支旧容器的参数不提供兼容包装。

Kotlin PSI 语法、接口与消费者静态断言、文档链接、差异及 Note 校验通过。提取实际测量 lambda，在临时 JVM 布局替身中通过空槽、单栏、双栏、零高度、动态高度、剩余 Insets、密度和无界约束共十种检查，确认 Padding 在正文组合前更新、正文保持完整测量范围且先于栏位放置。替身不执行真实 Compose Insets 传播或 GPU 绘制；临时检查材料在完成后移除。

本地未运行 Android 构建或设备操作；键盘动画、横屏缺口、三键/手势导航、RTL、折叠滚动与模糊合成仍需设备验收。
