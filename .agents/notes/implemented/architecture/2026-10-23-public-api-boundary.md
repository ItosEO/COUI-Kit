# Agent Note: 公开 API 收口与消费边界

Status: implemented

## Problem

菜单滚动条坐标、拖动状态、运动路径和 Popup 定位随实现直接公开，宿主可绕过组件生命周期修改它们。开发基线与最低消费要求也未区分，容易把生产端工具链或单条版本请求当作已验证的消费下限。

## Decision

菜单滚动条、项遮罩、运动工厂/帧/路径/定位、搜索运动实现、底栏附件控制器及其内部几何和提交辅助函数收为 internal。已知消费者检索覆盖本库、Demo 与原宿主的业务模块，未发现这些符号的外部调用。普通投影、融合、背景源、材质、光效、形变和固定预设继续公开，不按“底层”一律隐藏。

CouiMenuBlur 保留公开 draw 和 remember 工厂，构造器收为 internal，避免宿主组装固定数量的中间图层。顶栏按钮的 enabled/text/renderer 增加默认值，搜索状态工厂可自建单主体 renderer。显式 renderer 参数保留，默认创建不吞异常、不跨节点共享；需要创建错误控制时仍显式装配。Demo 保留此高级调用方式。

库 AAR metadata 表达当前 Compose 依赖要求的编译 SDK 和 AGP 下限，数值由构建文件持有。[兼容文档](../../../../docs/guide/compatibility.md) 区分必要元数据下限与待验证组合；DynamicAnimation 严格约束保持。不猜测 Kotlin 最低消费版本，也不把 Foundation 请求值当作 BOM 最终解析版本。

[接入指南](../../../../docs/guide/getting-started.md) 提供模块依赖与第一个组件；阴影、背景与各组件装配由普通使用页分别持有。当前不配置 Maven、不检查发行 AAR 或独立消费，不声明稳定二进制 ABI。

## Alternatives considered

- **只写边界说明，保留所有 public。** 没有源码兼容成本，但宿主仍可修改实现状态，边界不受编译可见性约束。
- **只留高层组件，全部底层接口 internal。** 暴露面最小，却破坏已约定的自定义阴影、算法融合和公共绘制能力。
- **把开发版本全部设为消费下限。** 容易记录，却会无依据地提高要求；先用依赖 AAR 元数据表达必要条件，其余明确待验证。

## Consequences

组件内部可独立演进；依赖被收口状态的未知调用者需要改用组件或公开算法。当前尚未发行，已知调用保留；后续公开变更仍需在 PR 说明。便捷入口复用同一后端，不证明创建一定成功。

静态核验覆盖可见性、公开声明的内部类型、Demo 导入、默认参数、Shader/算法主体及文档引用。Android 构建、独立消费、真机视觉和性能均未执行，源码检查不证明这些能力。

本轮 77 个 Kotlin/KTS 文件通过 PSI 语法解析，Demo 的 61 个 COUI 导入与 59 处可定位调用通过可见性/参数检查，未发现公开显式类型引用已收口类型。与 HEAD 对照确认 Kotlin 修改仅包含可见性、默认参数和错误定位文案，其他库源码保持一致。检查不解析 Android 类型，也不构成 ABI 或运行时保证。

## Existing-note audit

[原生主题与默认值](2026-10-30-native-theme-and-component-defaults.md)在原入口增加默认参数，并移除核心 M3 装配依赖；本篇的资源所有权与内部状态收口继续有效。

[普通弹簧持久通道](2026-10-29-persistent-motion-channels.md)进一步将 Touch/Press 轴改为只读 State，保留构造和读取；其迁移与二进制边界由该篇持有。

- [独立工程](2026-10-23-standalone-coui-demo.md)：部分重叠；保留初次原样迁移和工具链决定，本篇记录后续收口，互链。
- [模块分层](2026-10-21-coui-module-boundary.md)：部分重叠；职责与通用算法不变，本篇调整内部暴露面，互链。
- [Demo 展示](../feature/2026-10-23-demo-material-and-gallery.md)：部分重叠；保留消费者定位和高级示例，互链。
- 迁入的算法笔记保留各自依据，没有同范围 proposed/rejected 需要删除或归档。流程由 [开发指南与 PR 决定](../process/2026-10-23-developer-guide-and-pr-notes.md) 持有。
