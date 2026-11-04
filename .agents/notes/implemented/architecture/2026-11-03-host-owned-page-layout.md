# Agent Note: 宿主拥有页面结构，COUI 组件按需混用

Status: implemented

本篇“不提供 Scaffold”的选择由[可选轻量页面容器](2026-11-03-optional-coui-scaffold.md)部分取代。组件独立使用、主题与采样分离、导航和业务由宿主持有的决定继续有效。

## Problem

初始 Scaffold 同时承担页面测量、安全区、背景采样和局部配色，容易使调用方把 COUI 组件视为整套页面框架的一部分。已有 Material、Miuix 或自有布局的应用应能只取用一个 COUI 控件。

## Decision

独立组件通过现有 CouiTheme 与显式参数解析外观。Group 的配色只作用于自身卡片、标题和说明，不隐式改变内容槽中的其他设计组件。导航、业务状态、返回分发和页面容器选择归宿主。

背景采样使用现有 rememberCouiBackdrop、couiBackdrop 和 ProvideCouiMenuBackdrop。普通分组、行和文字不创建源；模糊材质显式接入正文源，菜单和弹窗按需使用窗口源。消费者不能录入自身采样源，正文源也不能代替需要完整窗口的来源。

### 已被部分取代的页面选择

本篇移除初始 Scaffold 及 PageContentPadding、PageSlot、PageStyle、LocalPageStyle 和 pageStyle，选择由宿主完成通用页面布局，不另提供同职能容器。理由是页面测量、配色作用域和采样被绑定，而当时已有基础接口能表达 COUI 特有能力。

[可选轻量页面容器](2026-11-03-optional-coui-scaffold.md)恢复通用栏位测量与 Insets 处理，接管“不提供容器”这一部分。旧页面配色作用域不恢复；COUI Scaffold 的局部正文采样由[Scaffold 正文源](2026-11-04-scaffold-body-backdrop.md)持有，第三方容器继续显式装配。

独立行布局与图示曾用于减少宿主重复排版；[通用 Item](../simplification/2026-11-03-layout-item-api.md)将行内容槽并入 Item，图示由宿主组合。Group、Item、Divider、Footer 仍可独立使用。

COUI 物理、搜索、折叠标题、材质和光效的算法及资源规则沿用各自实现。应用层示例可以使用 M3 Scaffold，核心不因此增加 Material 3 或 Miuix 依赖。

## Alternatives considered

- 保留初始可选 Scaffold 能缩短整页装配，但继续绑定通用测量与页面配色，选择移除该版本。
- 只将 Scaffold 改名为 Page 或 Layout 能保留调用形态，但职责不变，不能解除页面依赖。
- 新建页面主题 Provider 能单独提供颜色，但与 CouiTheme 重复，采用现有主题和组件参数。
- 删除全部局部布局可进一步缩小接口，却使宿主重复编写分组、排版和反馈，保留有视觉与交互职责的组件。

## Consequences

宿主可在已有布局中组合 COUI 控件；代价是显式处理滚动、Padding 和采样的关系。接入步骤由[宿主页面指南](../../../../docs/guide/secondary-pages.md)持有。

本篇部分替代[二级页组件](../feature/2026-11-01-secondary-page-primitives.md)的早期容器选择和[输入修复记录](../bug-fix/2026-11-02-secondary-page-review-fixes.md)中的旧容器优化，沿用[模块边界](2026-10-21-coui-module-boundary.md)与[可选 M3 策略](2026-10-31-optional-material3-style.md)。这些关系不表示各宿主和设备的动态兼容已经验收。
