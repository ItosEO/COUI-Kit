# Agent Note: 原生主题、组件默认值与 Material 染色边界

Status: implemented

## Problem

组件依赖宿主逐项传入配色和样式，核心还使用 Material 3 的文字、插槽上下文、菜单定位和交互尺寸。省略参数时必须有独立的默认来源，且深浅色、材质和投影不能分别取色。

## Decision

在原组件上增加默认参数，建立可选 CouiTheme、原生色板、文字和 Shape 预设。显式参数优先；未指定颜色时按最终深浅色读取匹配主题，否则使用对应原生预设。Effect、Backdrop 与 Renderer 不以主题为前提，不改变资源和失败契约。

原生主题保留灰白表面、黑白文字和 Theme.COUI 的蓝色强调色。菜单与底栏的额外页面色相染色仅在宿主适配明确提供 tint 时启用；有色 FAB 预设是另一条原厂能力，保留显式颜色和材质/投影的一致性。FAB 默认材质使用主题强调色，默认投影使用 material.firstColor，不自动叠加菜单式染色。Material 映射由 Demo 与使用示例承载，核心不引用 M3 类型。

当前入口为 [CouiTheme](../../../../coui/src/main/java/com/itos/coui/theme/CouiTheme.kt)、[菜单 Defaults](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)、[顶栏 Defaults](../../../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonDefaults.kt)、[默认 FAB](../../../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)与[底栏](../../../../coui/src/main/java/com/itos/coui/navigation/CouiBottomBar.kt)。[外观指南](../../../../docs/guide/appearance.md)持有颜色优先级、插槽迁移、宿主桥接和染色契约。

## Evidence

原厂基准为已有 PJZ110 录音 APK 的 Theme.COUI、values/values-night 中的 coui_color_primary_blue、coui_color_surface、coui_color_surface_top、coui_color_primary_neutral、coui_color_secondary_neutral 和 coui_color_divider。Theme.COUI 的 Android colorAccent 是另一条白色资源链，不能代替 COUI 控件的 couiColorPrimary；Switch 的 switch_custom_track_on 和 SeekBar 的 coui_seekbar_progress_selector 证明控件读取 COUI 蓝色角色。录音 AppTheme 继承 Red 变体，不能用应用红色覆盖基础蓝色默认。

菜单 COUIPopupListWindow.createContentView 读取 coui_popup_window_background 的 couiColorBackgroundTop，DefaultAdapter.setTitle 使用 couiTextBodyL，后者与旧 couiTextAppearanceBodyL 的字号不同。菜单使用顶层中性表面；当前菜单字号与排版按用户指定的 Metric / M3 菜单调整，见[可选 M3 策略](2026-10-31-optional-material3-style.md)。菜单组合轮廓、当前几何及顶栏增强参数保留已有项目特化，不将其称为全部原厂默认。Dialog 保留原先独立排版。

原始材料沿[复刻指南](../../../../docs/development/replication-guide.md)恢复，不加入工程依赖。去 M3 对照 Compose BOM 缓存对应的 Material3 1.5.0-alpha27 MenuAnchorPosition.Below / DropdownMenuPositionProvider / minimumInteractiveComponentSize / Text / Icon。菜单自有定位器保持起终点、窗口边候选、安全区、offset 与 RTL；全窗口 Popup 原点和触点纵向避让仍归 CouiMenuMotionPositionProvider。顶栏保留最小布局占位与居中，不再读取 M3 Local 或提供 M3 对齐线。

搜索图标保留 AndroidX Outlined.Search 的路径并转为 vector drawable；版权和 Apache 许可进入 NOTICE 与发行归档。直接使用的 AndroidX Core 能力由核心显式声明，避免去除 M3 后依赖偶然的传递关系。

## Existing-note audit

- [模块边界](2026-10-21-coui-module-boundary.md)：部分重叠；本篇取代“所有主题均由宿主装配”的部分，应用偏好、动态取色、业务状态和 HCT 仍归宿主。
- [API 收口](2026-10-23-public-api-boundary.md)：部分重叠；保留底层接口与资源所有权，不新增高低两套组件。
- [菜单合成](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)：部分重叠；保留已批准组合光效及显式染色，原生默认不启用额外染色。
- 其余组件笔记保留各自输入、算法与已批准特化；当前没有同范围的活跃 proposed/rejected 需要删除或归档。

## Alternatives considered

- 默认读取 MaterialTheme 可直接复用宿主颜色，但核心无法独立使用；改为宿主显式映射。
- 增加便捷组件与高级组件两层可保留旧签名，但重复入口不符合用户要求；在现有参数上提供默认值。
- 对所有参数使用 null 可统一延迟解析，但扩大空值契约；只有后置参数参与默认计算等实际需要时使用。

## Consequences

[可选 M3 策略](2026-10-31-optional-material3-style.md)部分替代本篇的宿主手工映射、逐插槽接线与 M3 顶栏固定占位决定。当前 M3 宿主通过可选模块初始化，在消费处解析并桥接内容；native 默认、显式覆盖、原厂依据、资源及定位边界继续有效。本篇保留原决定的理由与当时验证范围。

底栏右附件图标固定白色是用户确认的设计，包括自动使用主题强调色作底色的路径；不应将其改为 onAccent。这与独立 FAB 的内容 Local 是不同契约，宿主选择附件底色时需与白色搭配。

菜单标题的原生字体由 CouiTypography.label 提供，绘制直接使用最终 headerTextStyle，避免覆盖宿主字体。保留菜单自己的标题颜色、对齐、行数和溢出规则；Material 映射及显式样式可改变标题尺寸，因此实际布局仍需设备验收。

调用方可省略样式，仍可显式覆盖配置并独立使用 Effect。Slider 只对需要后置 dark/enabled 的 colors 使用 null；BottomBar 保留非空 Color.Unspecified，避免 nullable Color 改变 JVM 装箱描述符。可选附件缺少必需配置时显式失败，旧的完整附件参数仍参与原退场流程。

代价是 M3 插槽不再自动继承其颜色和文字样式，使用页与 Demo 显式接入 COUI 内容 Local。默认 Theme 与组件签名尚未发行，alpha2 不能直接使用新示例；消费者须重新编译核验，不声明二进制稳定。

隔离 JVM 自检直接编译生产 CouiMenuPositionProvider，与手算锚点、LTR/RTL、边缘、越界、大菜单、奇数居中和 offset 的 8 个预期位置比较通过。主题解析自检直接使用生产 CouiTheme 与真实 Compose 值类型，替换系统模式和 CompositionLocal 提供机制，覆盖无主题、显式主题、异模式原生回退、嵌套恢复及透明/显式染色；它不执行 Compose 重组或 Android。临时自检不进入工程或 CI。

Kotlin PSI、XML、引用及 Note 检查只说明各自静态范围。Android 构建、发行依赖图/POM/module、消费者/R8、菜单 RTL/IME、触摸占位、字体与 GPU 实绘仍由用户或后续已授权 CI 验证。本地不执行 Gradle、Preview、安装或设备操作。
