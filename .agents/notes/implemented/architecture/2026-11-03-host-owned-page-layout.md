# Agent Note: 宿主拥有页面结构，COUI 组件按需混用

Status: implemented

## Problem

组件库应兼容宿主已有设计体系，调用方可以只使用单个 COUI 组件。PR #15 的 CouiScaffold 将页面测量、安全区、背景采样和局部配色集中到一个库内页面容器，使组件装配被误认为必须采用整套页面框架。

## Decision

移除 CouiScaffold 及其专用的 PageContentPadding、PageSlot、PageStyle、LocalPageStyle 和 pageStyle。库不提供替代的 Scaffold、页面包装器或新的全局 Provider。页面布局、导航、安全区分工、返回分发和业务状态归宿主。

CouiGroup、CouiItem、CouiDivider、CouiFooter、CouiPreferenceLayout 和 CouiIllustration 保持独立。默认外观在消费处读取现有 CouiTheme；需要覆盖时使用所属组件的显式颜色或 dark 参数。分组的配色只作用于自身标题、卡片和说明，不隐式改变任意内容槽内的 Material、Miuix 或宿主组件。

背景采样继续使用现有 rememberCouiBackdrop、Modifier.couiBackdrop 和 ProvideCouiMenuBackdrop。纯分组、设置行、文字和图示不创建采样源。使用模糊材质的宿主显式指定正文源；菜单/弹窗的窗口源按需单独安装，不能用正文源替代完整窗口，也不能将采样消费者录入它自己的源。

保留既有 COUI 物理、搜索、折叠标题、材质、光效和节点资源所有权；不因删除页面容器而修改其算法或增加新的兼容状态机。Material 3 Scaffold 仅作为应用层用法示例，核心库不增加 Material 3 或 Miuix 依赖。

## Existing-note audit

- [二级页面组件](../feature/2026-11-01-secondary-page-primitives.md)：部分取代。原页面容器与页面配色作用域决定由本篇替代，其余组件与原厂依据保留。
- [二级页修复复核](../bug-fix/2026-11-02-secondary-page-review-fixes.md)：部分取代。Scaffold 稳定 padding 的优化随容器删除，其余已修复输入、绘制与物理问题保留。
- [模块边界](2026-10-21-coui-module-boundary.md)与[可选 Material 3 适配](2026-10-31-optional-material3-style.md)：沿用组件按需使用、业务由宿主持有及消费处解析主题的规则。

## Alternatives considered

- 保留可选 CouiScaffold 可以减少整页示例的装配代码，但仍维护一套通用页面测量与配色作用域。现有基础接口已覆盖 COUI 特有职责，不保留该入口。
- 将 Scaffold 改名为 Page 或 Layout 并保留实现，仍然接管宿主页面，不能解决职责问题。
- 新建页面主题 Provider 可解除颜色与 Scaffold 的绑定，但会与已有 CouiTheme 重复；采用现有主题和组件参数。
- 删除全部局部布局组件会使调用方重复编写 COUI 分组、行排版和反馈，超出本次目标；保留有明确视觉与交互职责的独立组件。

## Consequences

宿主可以将 COUI 组件放进 Material、Miuix 或自有布局，不需要替换页面骨架。代价是调用方负责页面 padding、滚动容器与采样源的正确组合；这些契约由普通使用文档说明。

此决定仅改变宿主装配与配色入口，不代表已经完成各设备、各设计体系的动态兼容验收。CI、源码复核、采样与状态所有权检查的实际结果写入 PR；不以编译通过替代 IME、RTL、TalkBack、模糊和动画中段的设备检查。
