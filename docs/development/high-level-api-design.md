# COUI 独立主题与组件默认参数

本设计已在当前分支落地，尚未发行。使用方式与颜色解析规则由[外观与预设](../guide/appearance.md)维护，公开接口规则见[API 设计](api-design.md)，迁移要求见[兼容说明](../guide/compatibility.md#原生主题与默认参数迁移)。

## 已实施范围

- 保留现有组件名称和参数顺序，通过默认参数简化调用；显式配置优先，不建立简单版与高级版组件。
- 核心提供可选 CouiTheme、原生色板和字体预设；没有主题时按系统深浅色使用原生预设。Shape 仍由组件 Defaults 或显式参数提供。
- 核心移除 Material 3 与 Material Icons 依赖声明，文字、图标、菜单定位和内容上下文使用 Compose 基础能力。
- Material 映射保留在 Demo 与可复制的宿主示例中，未新增独立桥接模块。
- 原生默认关闭菜单与底栏的额外页面染色；FAB 有色预设保留，材质与投影共用最终底色。底栏右附件图标按设计固定白色。
- Backdrop、Renderer、Effect 与 Motion 保持独立入口、资源所有权和失败契约。

实现依据及方案取舍见[原生主题决定记录](../../.agents/notes/implemented/architecture/2026-10-30-native-theme-and-component-defaults.md)。

## 验证与后续

已完成针对性静态检查、菜单定位和主题解析的隔离自检。Android 构建、发行依赖图、消费者 R8、菜单 RTL/IME、触摸区域和设备绘制仍待验证；不能将源码中无 M3 引用视为发行产物依赖图已验证。

[自动化测试评估](testing-plan.md)保留为后续建议，本次未新增测试框架、设备矩阵或 CI 阶段。后续验证应覆盖省略参数与显式原生配置的一致性，以及旧自定义入口和独立 Effect 的使用。
