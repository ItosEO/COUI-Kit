# Agent Note: COUI 实现与宿主组件分层

Status: implemented

## Problem

按文件名或 import 判断组件归属，会把应用主题、偏好和业务装配带入公共库，也会让通用 COUI 算法继续依附宿主。

## Decision

通用控件、效果、模糊、动效、输入、几何和预设归 `coui`；主题、HCT、应用 Shape、样式选择、导航和业务回调归宿主。依赖方向为宿主 → COUI，库不反向读取应用设置，不依赖 LoggerX、第三方 Backdrop 或厂商 SDK。固定参数集中在组件族 Defaults，不在宿主复制。

菜单通过 CouiMenuStyle 接收外观，顶栏通过 CouiToolbarStyle 接收最终材质与字体。搜索的几何、renderer 和运动状态成对持有并随节点释放；查询、焦点、IME、返回以及创建失败后的替代 UI 仍由宿主处理。

横向双主体融合由 CouiMetaballRenderer 与 rememberCouiMetaballFollowingTranslation 提供。中心、尺寸和半径直接使用本地像素；背景与 DstIn 遮罩使用同一算法，各自持有 renderer。侧滑手势、RTL 装配、删除确认和提交不进入库。三主体浮动材质是另一条算法分支，不能用它替代双主体融合。

普通阴影保留便捷 Modifier 与低层 Renderer，两者共享后端。需要窗口坐标、轴向缩放、节点透明度或多主体时由低层入口装配，不用便捷入口替换已有组件的完整变换链。

## Alternatives considered

- **移动所有引用 COUI 的文件。** 容易批量执行，但会连同主题、设置和业务状态一起迁入，破坏单向依赖。
- **只调整目录，继续依赖宿主实现。** 调用修改少，但公共算法仍不能独立维护和消费。

## Consequences

[原生主题与默认值](2026-10-30-native-theme-and-component-defaults.md)部分取代“全部主题由宿主装配”的决定；宿主应用偏好、动态取色、HCT 和业务职责继续保持原边界。

[分类栏](../feature/2026-10-24-chip-tab.md) 沿用同一边界：材质、输入和滚动归库，内容、选择状态及动作由宿主提供。

库的可复用性来自职责边界，不来自清除所有宿主 COUI import。宿主可以直接使用背景源和底层算法，不增加无职责的代理层。代价是宿主必须明确装配主题、布局与错误处理。

本篇从原宿主的同名架构 Note 与公共效果契约整理迁入，保留首次决定日期；路径改为独立工程。原宿主仍使用自己的副本，不因迁移自动切换依赖。独立配置及并存代价见 [迁出决定](2026-10-23-standalone-coui-demo.md)，公开边界见 [API 收口](2026-10-23-public-api-boundary.md)。

原工程的预设 JVM 对照与融合 Shader 对照是历史依据。本轮只检查迁入文档和当前公开边界，不据此新增 Android 编译、输入或 GPU 验收结论。
