# Agent Note: 设置式 Demo 与可追溯系统图标子集

Status: implemented

## Problem

完整开发树混有尚未投稿的 Sheet、普通按钮和历史反射对照，不能直接覆盖最新主线。Demo 要保留组件/效果/预设三页及原 dock，并将具体组件组织成真实二级页面；图标需要确认实际消费者，不能仅凭提取索引的名称选用。

## Decision

Demo 消费公开的选择器与独立布局组件，组件/效果首页只登记目录，详情由普通 Activity 装配。标准 Intent 启动和 finish 交由当前 ROM 决定转场；不注册厂商白名单或复制 OEM 窗口协议。来源页查询、导航行和反馈在转场时保留，返回恢复原状态；dock 及 renderer 位于稳定作用域。

页面沿用[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)：DemoApp 与 DemoSettingsPage 使用应用层 Material 3 Scaffold，正文源显式录制完整视口，顶栏和底栏不录入其消费的正文源。DemoHostActivity 提供菜单/弹窗窗口源。横向安全区由页面消费，顶部由顶栏处理，底部由底栏或 Scaffold 处理；滚动内容应用并消费测量所得 padding。分组使用 Group 的默认留白，不恢复已移除的 topSpacing 参数或库内页面容器。

保留主线 DemoApplication 的 Coui.initialize(CouiMaterial3Style()) 与可选模块依赖，不恢复根部手工色板映射。预设页控制局部原生/M3 外观，movableContentOf 保留同一页面状态树；各 Activity 共享偏好和实际深浅模式。组件默认配色在消费处解析，显式样式取最终 COUI 角色，菜单填写明确模式，不猜表面亮度。

CouiIcons 只收录 Settings、Tips 和 Clock 中已沿资源/代码调用者追溯的 27 个原始矢量。SearchField 与 Search 是不同原始资源；Tips 分类和精选分别保留真实普通/选中路径，预设复用 Settings 个性化图标并由 dock 表达选择。滑条、搜索、菜单、返回、日期、时间与倒计时按用途分配；不再用 SystemUI 的“水”设备开关表示效果页。几何转换保留分组、裁剪、描边和 alpha，RTL 从原选择器继承；功能 tint 由消费者提供，导航 alpha 统一归底栏。

完整来源、原始 XML/APK 哈希及调用链见 [ICON_SOURCES](../../../../coui/ICON_SOURCES.md)，Demo 分配见[图标说明](../../../../demo/ICON_SOURCES.md)。源码不包含原 APK、全量提取图标、PNG 预览、本机材料路径、密钥或映射文件；第三方权利不改标为 MPL。

Sheet、闹钟/倒计时编辑面板、独立普通按钮及临时原版反射对照不进入本批。记录侧滑等既有业务示例仍在 Demo，不加入库。

## Existing-note audit

- [独立 Demo](../architecture/2026-10-23-standalone-coui-demo.md)：保留独立宿主与公开 API 边界，本篇更新示例组织。
- [可选主题策略](../architecture/2026-10-31-optional-material3-style.md)：直接沿用初始化、局部覆盖与内容桥接，不复制适配模块实现。
- [二级页面](2026-11-01-secondary-page-primitives.md)与[选择器](2026-11-01-coui-pickers.md)：消费其公开能力；完整路由、草稿、字符串和偏好始终归 Demo。
- [宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)：沿用页面由宿主管理的决定，示例迁移到通用布局名称和显式背景源。
- 活跃 proposed/rejected 无同范围提案；旧本地研究保留在开发树，不将其路径或全部历史 Notes 复制进投稿。

## Alternatives considered

- 直接上传完整开发树最省拆分工作，但会回退最新主线并带入未授权的 Sheet 和其他开发项，按三批能力边界组织。
- 只凭图标资源名称筛选更快，但 APK 可能包含未使用资源和通用依赖；选用能提供具体调用者的子集，并明确源码证据不能证明所有设备的运行分支。
- 把每种设置行和三点菜单公开为新组件可减少示例代码，但只增加转发层，保留为 internal 宿主组合。

## Consequences

目录、详情与公共库职责分离，可独立审阅；图标只有一份不可变 ImageVector，减少重复资源。代价是维护真实 Activity 的状态与生命周期，以及受限图标子集的来源表。构建、签名和静态核对不能证明像素、动画中段、IME、RTL、TalkBack、预测返回或性能等价，设备交互仍需针对性验收。
