# Agent Note: 通用 Item 内容槽与布局公开入口收敛

Status: implemented

## Problem

普通设置行需要组合点击行与另一个公开布局组件，信息行和可点击行因而使用不同入口。独立图示组件只封装标准布局约束，也增加了公开维护面。用户要求统一行内容槽、仅去掉四个布局组件的前缀，并让宿主继续拥有选择、菜单和导航。

## Decision

布局包只保留 Group、Item、Divider、Footer 四个组件名称，CouiPageColors 与 CouiPageDefaults 保持原名。Item 合并 leading、headline、supporting、trailing、内边距和图标尺寸；沿用现有默认几何，不提供旧公开名称的兼容包装。Group 保留标题、圆角背景、说明与自由内容，不引入 item DSL。

Item 仅在 onClick 非空时安装点击、按钮语义和 COUI 反馈，并共用一个交互源。onClick 为空时只布局内容，enabled 不跨内容槽禁用子控件或统一降低透明度。整行 toggleable/selectable 和对应反馈由宿主负责，尾部独立操作由子控件负责；不新增 SwitchItem、CheckboxItem 或 MenuItem。

Demo 普通跳转通过 Item.onClick；开关、复选与单选保留外层语义，尾部开关不提供独立点击或拖动提交。带展开锁定反馈的 Popup 行保留 Demo 的同一个外部点击与反馈源，Item 不再安装点击。滑条、输入、步进和进度操作继续保留独立事件链。Illustration 从库移除，原 Demo 图示直接组合 Box、Row 与 Modifier。

页面原生配色统一从 CouiPresets 派生。CouiText 的颜色缺省读取 LocalCouiContentColor，菜单排版只由已有 typography.menuItem 提供。couiButtonFeedback 的模式与启用状态提供常用默认值，交互源仍由调用方明确传入。顶栏显式颜色的 alpha 与渐变、折叠和搜索动画相乘；搜索槽的主题仍由宿主控制。

这些是用户批准的公开装配与颜色覆盖调整，不改变原有反馈物理、模糊或融合算法。低层浮动接口继续按绘制节点管理 renderer 和图层。

## Existing-note audit

- [宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)：部分取代独立公开行布局与图示的选择；页面、安全区、主题与采样职责保留，双方互链。
- [二级页面组件](../feature/2026-11-01-secondary-page-primitives.md)：部分取代点击行和排版分开的公开入口；原厂输入、滚动与搜索依据保留，双方互链。
- [可选 Material 3 策略](../architecture/2026-10-31-optional-material3-style.md)：沿用消费处解析与显式覆盖，补充内容色与原生配色的唯一来源。
- [设置式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)：同一职责，就地同步公开名称，不改变导航与示例状态所有权。
- 其余 Note 的组件算法与本次收敛无关；没有需要删除或归档的同范围活跃提案。

## Alternatives considered

- 保留点击行与独立布局最灵活，但普通调用需要嵌套两个库入口。使用可选 onClick 的 Item，同时保留外部交互 Modifier。
- 为每种设置行公开专用组件可以缩短页面代码，但会把业务组合扩展成库 API。只保留 Demo 内部文字和交互包装。
- Illustration 可以统一示意图尺寸，但这些约束可由标准 Modifier 完成。删除公开入口，在 Demo 内保持原有几何。
- 全库统一去前缀可获得一致命名，但超出本轮授权；仅迁移四个布局组件。

## Consequences

普通信息行与点击行共用一个公开入口，宿主仍可组合独立尾部控件。原开发分支消费者须迁移名称与内容槽；这些接口在仓库中标记为未发布，不保留临时别名。源码与用法分别见[布局组件](../../../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)及[分组与行](../../../../docs/components/settings.md)。

差异、引用、Kotlin PSI 语法与接口静态断言、普通文档链接及 Note 校验通过；本地未运行 Android 构建或设备操作。真实触摸取消、TalkBack、字体换行与 GPU 合成仍需设备验证，静态结果不代替这些行为验收。
