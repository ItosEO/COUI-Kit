# Agent Note: 通用 Item 内容槽与布局公开入口

Status: implemented

下文“尾部不提交拖动”的 Demo 开关装配由[整行开关输入](../bug-fix/2026-11-03-switch-row-drag.md)部分取代。Item 的内容槽、可选点击与宿主选择语义保持不变。

## Problem

普通设置行需要嵌套点击组件与另一个公开排版组件，信息行和可点击行因此走不同入口。独立图示组件只封装标准布局约束，也增加了公开维护面。

## Decision

布局族使用 Group、Item、Divider、Footer 四个名称，CouiPageColors 与 CouiPageDefaults 保持原名。Item 统一 icon、title、summary、trailing、内边距和 iconSize 图标尺寸，并保留现有默认几何。Group 提供标题、卡片、说明与自由内容，不增加 item DSL。

`Item.onClick` 非空时安装一份点击、按钮语义和 COUI 反馈，共用交互源；为空时只布局内容。`enabled` 不跨内容槽禁用子控件或统一降低透明度。整行 toggleable/selectable 及其反馈由宿主提供，独立尾部操作由控件提供。

Item 不额外安装 mergeDescendants：有整行交互时由 clickable/toggleable/selectable 提供合并规则，无交互时保留子控件语义。Group 的标题操作仅随非空标题渲染。零值 groupPadding 不作为公开配置；目录专用行高归 Demo。

Item 与分隔线共用行内留白、图标尺寸及 CouiPageDefaults.iconSpacing。Demo 默认行从这些默认值计算缩进；自定义布局使用实际参数计算，不建立相邻行测量协议或自动分组状态。

Demo 的普通跳转使用 Item.onClick；开关、复选与单选行使用外层语义，尾部不另提交点击或拖动。Popup 行要在展开时保持反馈，因此使用同一个外部点击与反馈源。滑条、输入、步进和进度保持各自事件。图示直接组合 Box、Row 与 Modifier。

页面配色从 CouiPresets 派生，Text 的默认颜色读取 LocalCouiContentColor；未提供内容色时在消费处读取当前主题前景色。菜单排版由 typography.menuItem 持有。couiButtonFeedback 为模式和 enabled 提供默认值，交互源仍由调用方传入。

Text 直接向 BasicText 传递可选 maxLines 和 overflow，复用平台的限制与非法行数校验。Body、Description 与设置标题/摘要默认按逻辑起始边对齐；显式对齐优先。Demo 尾部值和工具栏标题直接使用该公开组件，保留各自的对齐和省略规则。

顶栏显式颜色的 alpha 与渐变、折叠和搜索动画相乘；搜索槽的模式由宿主传入或共同主题提供。这些调整保留原反馈、模糊与融合算法。

## 与既有决定的关系

本篇部分替代[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)和[二级页组件](../feature/2026-11-01-secondary-page-primitives.md)中独立公开行布局与图示的选择。通用页面容器由[可选 Scaffold](../architecture/2026-11-03-optional-coui-scaffold.md)单独定义；主题消费规则沿用[可选 M3 策略](../architecture/2026-10-31-optional-material3-style.md)，实际行装配由[设置式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)消费。

## Alternatives considered

- 分开点击与排版最灵活，但普通行需嵌套两个库入口；采用可选 onClick，同时保留外部交互 Modifier。
- 为每种设置行公开专用组件能缩短页面代码，但会把宿主组合扩大为库接口；只保留 Demo 内部组合。
- Illustration 能统一示意图尺寸，但标准 Modifier 已能表达这些约束，图示回到宿主。
- 当时仅收敛四个布局入口，避免将其他组件改名混入行布局调整；当前组件统一命名见 [API 设计](../../../../docs/development/api-design.md)，类型和工厂仍保留前缀。

## Consequences

信息行和点击行使用同一入口，宿主仍可组合独立控件。开发分支消费者需迁移名称与内容槽；这些接口尚未发布，不保留临时别名。源码与用法见[布局组件](../../../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)及[Item](../../../../docs/components/item.md)。

语法、引用和静态检查不能证明触摸取消、TalkBack、字体换行或 GPU 结果；这些行为仍需设备验证。实际检查结果由 PR 记录。
