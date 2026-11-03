# Agent Note: 目录式 Demo 与系统图标子集

Status: implemented

## Problem

组件和效果需要在完整页面中展示。目录、详情、受控状态与系统图标应各有明确归属，避免把 Demo 业务变成库接口，也避免只按 APK 中的资源名称选图标。

## Decision

Demo 保留组件、效果、预设三页与底栏，组件和效果由目录进入普通 Activity。标准 Intent 和 finish 使用当前 ROM 的应用转场；首页与搜索详情的查询、展开目标和来源行反馈在转场时保留，底栏和 renderer 位于稳定组合位置。点击搜索结果只执行导航，取消搜索或在展开时按返回键才关闭并清空查询。导航、草稿、偏好和业务回调只存在于 Demo。

详情按稳定 page 标识直接装配示例。目录搜索只筛选可导航条目，不把本地化标题转换成 query 再筛回详情。首页页签继续使用 SaveableStateHolder，详情保留各示例自身的 rememberSaveable；不为固定 Activity 再保存一层筛选分组。设置行直接组合公开 COUI 控件，删除无消费者的旧 M3 开关和纯参数转发。整行开关的点击与拖动边界见[整行开关输入](../bug-fix/2026-11-03-switch-row-drag.md)。

DemoApp 与 DemoSettingsPage 使用[可选 CouiScaffold](../architecture/2026-11-03-optional-coui-scaffold.md)。正文源录制完整视口，顶栏与底栏不录入自身消费的源；DemoHostActivity 提供菜单和弹窗的窗口源。容器负责横向安全区，栏位负责对应纵向 Insets，IME 在容器外消费。独立组件与主题、采样的关系沿用[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)。

Application 初始化 CouiMaterial3Style；局部 CouiTheme 切换原生外观，movableContentOf 保留同一内容树。Activity 共享偏好与实际模式，行控件直接读取 CouiTheme 和 CouiPageDefaults，不另存一份 Demo 主题 Local。DemoTheme 为全部 Activity 提供 M3 LocalContentColor=onBackground；MaterialTheme 本身不提供该内容色，仅在首页设置会使独立详情中的普通 Text/Icon 回退黑色。显式样式取最终 COUI 角色，菜单使用明确模式。依据见[可选主题策略](../architecture/2026-10-31-optional-material3-style.md)。

组件预览只在实际分支持有状态与资源：查询和搜索状态归搜索页，浮动 renderer 归底栏/FAB，创建错误也在相应分支显示。共用预览布局负责底图、裁剪和采样源，不持有控件状态。模糊对比图仅为需要采样的一侧安装源。顶栏菜单与按钮共用一个 Box，直接使用 Popup 父布局定位；设置行的触点定位继续使用公开锚点。

图标表登记 27 个 Settings、Tips 和 Clock 资源入口及实际调用链。SearchField 与 Search 来自不同资源；Tips 的分类和精选保留普通/选中态。Demo 的预设使用个性化图标，由底栏表达选择。颜色、点击热区和无障碍说明归消费者，几何保存在不可变 ImageVector 中。

转换范围包括路径、分组、裁剪、描边、alpha 和所记录的 RTL 选择。导航 alpha 由底栏统一控制。原始 XML/APK 哈希及转换范围由 [ICON_SOURCES](../../../../coui/ICON_SOURCES.md)维护，Demo 用途见[图标分配](../../../../demo/ICON_SOURCES.md)；来源 App 的调用证据不代表 Demo 是同一官方页面。

源码不包含原 APK、全量提取素材、预览图、本机路径或密钥。第三方权利不改标为 MPL。来源表是研究记录，目标材料的复核缺口见[输入连续性记录](../bug-fix/2026-11-03-page-input-continuity.md#consequences)。

## Alternatives considered

- 整体迁入开发树最省拆分工作，但会混入未纳入范围的 Sheet、普通按钮、反射对照及历史变动，按组件与 Demo 的实际范围整理。
- 只按资源名称选图标更快，但 APK 可能含未使用资源和通用依赖，采用有调用记录的子集并保留证据限制。
- 公开各类设置行和三点菜单能缩短示例，但这些主要是参数转发与业务组合，保留为 Demo 内部实现。
- 沿用 gallery 的标题筛选可以保留旧分发代码，但独立 Activity 已有稳定 page，翻译字符串和筛选专用状态层不再承担页面选择；直接在详情入口分发。

## Consequences

目录、详情和库的职责可分别维护。代价是处理 Activity 状态、资源生命周期和图标来源。Sheet、闹钟/倒计时编辑面板、独立普通按钮及原版反射对照不属于当前示例；记录侧滑等业务演示继续留在 Demo。

本篇部分取代 [M3 gallery 装配](2026-10-23-demo-material-and-gallery.md)中的私有 M3 配置开关、同页标题筛选与分组保存层；保留其 M3 语义、业务 owner 和采样分层理由。旧私有开关没有消费者，删除不收窄公开库能力。

本篇沿用[独立工程](../architecture/2026-10-23-standalone-coui-demo.md)，消费[二级页组件](2026-11-01-secondary-page-primitives.md)与[选择器](2026-11-01-coui-pickers.md)的公开能力。构建及静态检查不证明像素、动画中段、IME、RTL、TalkBack、预测返回或性能等价，实际结果按提交记录在 PR。
