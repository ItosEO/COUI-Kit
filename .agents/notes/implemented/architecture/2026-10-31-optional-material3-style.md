# Agent Note: 可选 Material 3 策略与消费处解析

Status: implemented

## Problem

核心独立后，M3 宿主必须在根部手工映射颜色，并为每个内容插槽传 COUI Local。根部映射不能自动跟随组件内部的嵌套主题；逐插槽接线容易遗漏禁用颜色、部分文字样式和字体。宿主需要保留现有业务调用与自定义外观，同时减少重复装配。

## Decision

新增可选 coui-material3 → coui + M3。核心继续无 M3/Material Icons 依赖。Application 在 onCreate 调用 Coui.initialize 固定默认 CouiStyle；未初始化为 native，重复初始化明确失败。不保存 Context、色板、字体或业务状态，不提供运行时全局切换。CouiStyle 只持有主题默认值解析、内容作用域适配和最小交互尺寸三项契约，类型限于核心与基础 Compose。

CouiMaterial3Style 在每个组件消费处读取 MaterialTheme，随宿主动态色、动画与嵌套主题更新。LocalCouiDarkTheme 接收宿主模式，缺省使用系统；显式组件 dark/style.dark 优先。任意 M3 色板不提供可靠的模式标记，因此库不猜深浅。组件请求另一模式时默认颜色回退对应 native，tint 透明。局部 CouiTheme 提供自身模式与 native/custom 默认值；局部异模式直接回退 native，不再尝试全局策略；退出恢复外层。它仍使用已选适配器桥接 M3 插槽。

CouiTopAppBar 的 dark 默认同样读取 CouiTheme.isDark，防止只传 gradientColor 的调用在深色主题下使用浅色渐变透明度；显式 dark 继续优先。

统一内容作用域放在 TopAppBarButton、SearchSurface、ChipSurface 和 FAB 的最终样式位置。M3 对部分组件文字样式采用 LocalTextStyle.current.merge 的主线顺序，保留 ambient 属性；完整 CouiTheme 基线直接替换。禁用 alpha 只算一次，内层 Local 与显式子颜色/样式保持优先。只转发的 title、buttonGroup、leadingAction 和 Dialog 自定义正文不建立新样式。BottomBar 仅桥接其内部文字，保留自己的字号、行高与字重。

TopAppBarButton 在原 Modifier 位置调用真正的 M3 minimumInteractiveComponentSize，恢复尺寸 Local 与 alignment lines；native 策略保留原规则。SearchButton 主线本就使用固定容器，因此保持固定尺寸，不受该 Local 控制。

颜色映射由[适配器源码](../../../../coui-material3/src/main/java/com/itos/coui/material3/CouiMaterial3Style.kt)持有：菜单 selected 与 checkbox 角色分开，菜单模糊补底与无 Backdrop 容器分开。页面 background 与组件 surface 分开，分组卡片取 container，避免原生浅色页面与白色卡片混合，也避免为页面对比修改弹窗表面。菜单与 BottomBar 仅把 host surface 送入既有页面色相层，透明显式关闭。默认 FAB 用 accent 材质与 onAccent 内容色，shadow 取最终 material.firstColor；显式 material 保留 ambient 内容色，不叠加页面 tint。BottomBar 右附件仍固定白色。

菜单项与头部标签分别采用 M3 labelLarge 与 bodySmall，与用户指定的 Metric 菜单角色一致；普通内容继续采用 bodyLarge。原生菜单排版按同一基准调整，具体参数只由 CouiTypography 持有。

Chip 与 Dialog 仅接入相应 M3 字体族，不改 COUI 字号、字重、行距和 fontScale。Dialog 订阅 FontFamily.Resolver 返回的 State，resolved Typeface 同时驱动行距计算、操作区测量和绘制，避免锁住首次 fallback 字体。native 保留原系统字体。

CouiMenuStyle 仅在末尾增加可选 dark。默认工厂填写实际模式，main/submenu 的文字、材质、阴影与反馈统一使用它。旧手写完整 Style 缺省该字段时保留原 surface 亮度判定。headerTextStyle 的显式字体与字号继续完整生效。

Demo 在 Application 初始化，以局部 CouiTheme 切换 native。关闭 Material 配色时，DemoTheme 同时给 M3 内容提供原生颜色映射；否则显式 MaterialTheme 取色及 Backdrop 会继续带入壁纸动态色。首页和底栏示例使用默认 tint，不用显式 M3 颜色覆盖开关。movableContentOf 保留切换中的同一业务状态树与 renderer；不更改全局策略。保留完整 Style、Shape、材质和有色预设示例，并展示省略 style 的调用。

两个 artifact 同版本，适配模块显式设置独立 artifactId。PR 与手动预览发布共用真实 Maven 产物断言；两个模块均执行既有 Lint/本地发行检查，预览发布均上传并确认远端。不增加渠道或远端凭据流转范围。

## Existing-note audit

- [原生主题与默认值](2026-10-30-native-theme-and-component-defaults.md)：部分重叠。本篇替代宿主手工 M3 映射、逐插槽接线与 M3 顶栏固定占位的决定；原生预设、显式参数、原厂依据、定位与资源边界保留。
- [模块边界](2026-10-21-coui-module-boundary.md)：部分重叠。核心仍独立，增加可选适配模块；偏好、动态色创建、HCT 和业务状态仍归宿主。
- [API 收口](2026-10-23-public-api-boundary.md)：部分重叠。只公开跨模块策略所需契约，内部 Text/Icon 与组件状态继续收口。
- [预览发布](../process/2026-10-25-github-packages-preview.md)：同一发行决定扩展到两个 artifact，就地同步文件与检查入口，不改变渠道和凭据规则。
- 其余组件 Note 的几何、运动、输入与已批准特化无关，保留；当前无同范围活跃 proposed/rejected 需要删除或归档。

## Alternatives considered

- 宿主根部映射与逐插槽 Local 接线无需新模块，且可完全自定义；但难以跟随嵌套主题，重复装配易漏。改为可选适配器，显式参数继续保留。
- 把 M3 放回核心可直接复用内容 Local；但 native 宿主被迫引入无关依赖。保留核心无 M3，以小型核心契约跨模块适配。
- 初始化时缓存色板最简单，但无法响应动态色、动画和嵌套主题。初始化只存策略，默认值在消费处解析。
- 通过 surface 亮度猜模式无需宿主 Local，但自定义色板不保证该关系。新自动路径使用显式模式来源；只为旧完整 Menu Style 保留原判定。

## Consequences

M3 宿主只需依赖与启动初始化，可继续使用普通 Text/Icon 和原业务调用。代价是两个同版本 artifact 的发行与依赖需要共同核验；自定义 App 模式和嵌套模式须明确提供 Local。公开参数相对顺序保留，不承诺 ABI，所有消费者须重新编译。

本轮只执行差异、Kotlin PSI、引用、脚本语法和 Note 静态检查。真实初始化/Compose 解析断言、重组、最小尺寸测量、异步字体排版、GPU、消费者/R8、Maven 产物和远端发行均未执行；本地 Android 构建与设备操作不在授权内。真实产物断言由已有 CI/用户执行，不以替身或镜像实现冒充运行结果。
