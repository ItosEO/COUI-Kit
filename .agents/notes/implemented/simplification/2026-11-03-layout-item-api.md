# Agent Note: 通用 Item 内容槽与布局公开入口

Status: implemented

## Problem

普通设置行需要嵌套点击组件与另一个公开排版组件，信息行和可点击行因此走不同入口。独立图示组件只封装标准布局约束，也增加了公开维护面。

## Decision

布局族使用 Group、Item、Divider、Footer 四个名称，CouiPageColors 与 CouiPageDefaults 保持原名。Item 统一 leading、headline、supporting、trailing、内边距和图标尺寸，并保留现有默认几何。Group 提供标题、卡片、说明与自由内容，不增加 item DSL。

`Item.onClick` 非空时安装一份点击、按钮语义和 COUI 反馈，共用交互源；为空时只布局内容。`enabled` 不跨内容槽禁用子控件或统一降低透明度。整行 toggleable/selectable 及其反馈由宿主提供，独立尾部操作由控件提供。

Demo 的普通跳转使用 Item.onClick；开关、复选与单选行使用外层语义，尾部不另提交点击或拖动。Popup 行要在展开时保持反馈，因此使用同一个外部点击与反馈源。滑条、输入、步进和进度保持各自事件。图示直接组合 Box、Row 与 Modifier。

页面配色从 CouiPresets 派生，CouiText 的默认颜色读取 LocalCouiContentColor。菜单排版由 typography.menuItem 持有。couiButtonFeedback 为模式和 enabled 提供默认值，交互源仍由调用方传入。

顶栏显式颜色的 alpha 与渐变、折叠和搜索动画相乘；搜索槽的模式由宿主传入或共同主题提供。这些调整保留原反馈、模糊与融合算法。

## 与既有决定的关系

本篇部分替代[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)和[二级页组件](../feature/2026-11-01-secondary-page-primitives.md)中独立公开行布局与图示的选择。通用页面容器由[可选 CouiScaffold](../architecture/2026-11-03-optional-coui-scaffold.md)单独定义；主题消费规则沿用[可选 M3 策略](../architecture/2026-10-31-optional-material3-style.md)，实际行装配由[设置式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)消费。

## Alternatives considered

- 分开点击与排版最灵活，但普通行需嵌套两个库入口；采用可选 onClick，同时保留外部交互 Modifier。
- 为每种设置行公开专用组件能缩短页面代码，但会把宿主组合扩大为库接口；只保留 Demo 内部组合。
- Illustration 能统一示意图尺寸，但标准 Modifier 已能表达这些约束，图示回到宿主。
- 全库去前缀能统一命名，但此决定只调整四个布局入口，其他公开名称保持。

## Consequences

信息行和点击行使用同一入口，宿主仍可组合独立控件。开发分支消费者需迁移名称与内容槽；这些接口尚未发布，不保留临时别名。源码与用法见[布局组件](../../../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)及[分组与行](../../../../docs/components/settings.md)。

语法、引用和静态检查不能证明触摸取消、TalkBack、字体换行或 GPU 结果；这些行为仍需设备验证。实际检查结果由 PR 记录。
