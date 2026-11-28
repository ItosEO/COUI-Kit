# 更新记录（CHANGELOG）

COUI-Kit 处于早期预览阶段。本文记录公开 API、行为、接入和兼容要求的变化。日期以提交合并时间为准；版本归档以真实 Git Tag 和发行产物为准。

## [0.0.1-alpha22] - 2026-11-28

### 新增与改进

- 优化 `BottomSheet` 打开性能与组件着色器资源复用。`CouiShadowRenderer` 改为复用无状态网格规范与静态 LUT 表，浮动表面焦散、无状态目标滤镜与跟手光（`Spotlight`）着色器改为按需惰性创建；面板纯背景采用按需离屏以削减重复全屏离屏缓冲，上圆角裁剪改为复用 `CouiShape` 同源单路径替代 `PathOperation.Union` 布尔运算，并收敛入场结束重组作用域与退场期顶栏点击防重。[Sheet 面板](docs/components/bottom-sheet.md)
- 修复 `BottomSheet` 收起动画期间阻挡背景内容点击与焦点的问题。退场开始即释放窗口触摸与焦点，退场期间下层页面可正常点击、滚动及响应返回键，中途反向重开时立即恢复。

### 升级说明

- Alpha22 仅通过 JitPack 发布，核心库坐标为 `com.github.itoseo.coui-kit:coui:0.0.1-alpha22`。`coui-material3` 和 `coui-icons` 请同步升级到同一版本。[JitPack 接入](docs/guide/getting-started.md#jitpack)

## [0.0.1-alpha21] - 2026-11-26

### 新增与改进

- 修复 `Dialog` 和 `InputDialog` 的长正文滚动布局。标题和底部操作区保持固定，说明文字和自定义内容共用滚动区。[弹窗](docs/components/dialog.md)
- 为缩放显隐动效增加随系统材质设置启用的前景模糊，并接入 `FloatingToolbar`。自绘组件可通过 `CouiScaleVisibilityState.foregroundBlurEffect` 将效果应用到前景图层。[缩放显隐](docs/effects/motion.md#缩放显隐)
- 为 `NavigationBar` 增加整栏 `visible` 控制。胶囊和两侧附件在原位一起缩放显隐，收起时立即停用交互并保留底栏占位。[底栏](docs/components/navigation-bar.md)

### 升级说明

- Alpha21 通过 JitPack 发布，核心库坐标为 `com.github.itoseo.coui-kit:coui:0.0.1-alpha21`。`coui-material3` 和 `coui-icons` 请同步升级到同一版本。[JitPack 接入](docs/guide/getting-started.md#jitpack)

## [0.0.1-alpha20] - 2026-11-26

### 新增与改进

- 新增悬浮操作栏 `FloatingToolbar`（Android 14 / API 34+），支持 `CouiFloatingToolbarItem` 列表动态增删回弹、宽度不足时自动收进“更多”溢出菜单、整栏 `visible` 缩放显隐、跟手形变与单项反馈。内置 `WindowInsets.tappableElement` 安全区及悬浮留白，支持通过 `style` 与 `themeTint` 配置玻璃材质或纯色外观，且 `themeTint` 同步作用于溢出菜单。[悬浮操作栏](docs/components/floating-toolbar.md)
- 新增缩放显隐动效入口 `rememberCouiScaleVisibility`、`Modifier.couiScaleVisibility` 与 `CouiScaleVisibilityState`，供同一绘制主体在显示与退场期间同步驱动双轴缩放和透明度。[运动与输入](docs/effects/motion.md)
- 优化 Material 3 强调色映射：`CouiMaterial3Style` 新增 `useHctColors`（默认 `true`），默认基于宿主 `primary` 生成 HCT 强调色——`accent`（菜单选中文字、光标等）增强彩度并保留原色调，搭配白色前景的实色控件与复选框使用按浅/深模式固定色调的 `controlAccent`。传入 `useHctColors = false` 可直接沿用 Material 3 原始强调色。[外观与预设](docs/guide/appearance.md#接入-material-3)
- 优化 `Dialog` 按钮配色：`CouiDialogActionStyle.Primary` 省略 `colors` 时默认采用主题强调填色（`controlAccent`），不再强制传入 `colors`；普通按钮在纯色面板和已染色模糊面板上透出底色（浅色压暗、深色提亮），关闭模糊染色时保留原生半透明白底。[弹窗](docs/components/dialog.md#多操作主操作配色与自定义正文)
- `CouiColorScheme` 新增 `controlAccent` 角色（缺省回退至 `accent`），`Dialog` 主操作、默认 `Fab`、`NavigationBar` 有色附件、`Switch`、`Slider`、`RadioButton` 与 `SingleChoiceDialog` 单选框统一使用该角色。
- `Modifier.couiToolbarFeedback` 新增可选的 `pressFeedback` 与 `maskInset` 参数，支持按压波纹与蒙版外扩；同步更新相关组件文档、外观指南与 `coui-kit` 外部接入 Skill。

### 升级说明

- `CouiMaterial3Style()` 默认启用 HCT 强调色增强；如需继续直接沿用 Material 3 原始 `primary`（深色复选框取 `inversePrimary`），在初始化时改为 `Coui.initialize(defaultStyle = CouiMaterial3Style(useHctColors = false))`。
- `CouiDialogActionStyle.Primary` 已支持省略 `colors` 跟随主题，现有显式传入 `CouiDialogDefaults.primaryActionColors(accent)` 的调用仍可沿用并保持优先。
- Alpha20 通过 Maven Central 与 JitPack 发布，坐标分别为 `io.github.itoseo:coui:0.0.1-alpha20` 与 `com.github.itoseo.coui-kit:coui:0.0.1-alpha20`；使用 `coui-material3` 或 `coui-icons` 时请同步升级至同渠道 `0.0.1-alpha20`。

## [0.0.1-alpha19] - 2026-11-24

### 新增与改进

- 重构 `NavigationBar` 左右附件配置，新增 `CouiNavigationBarAction`、`CouiNavigationBarActionStyle` 与 `CouiNavigationBarDefaults`（`material`、`transparentActionStyle`、`coloredActionStyle`）。两侧附件均支持独立选用默认透明、主题强调色或自定义外观（材质、投影、图标颜色、形状、光斑与悬停反馈），`visible` 控制融合收起动画，省略 `onClick` 时仅保留拖拽与光效。[底栏用法](docs/components/navigation-bar.md#左右附件)
- `NavigationBar` 新增 `material` 参数用于定制中间胶囊材质（与 `lightColor`、`themeTint` 共同仅作用于中间胶囊）；同步更新底栏组件文档、外观染色说明与 `coui-kit` 外部接入 Skill。

### 升级说明

- 升级时将 `NavigationBar` 原有的 `leftIcon`、`rightIcon`、`leftDescription`、`rightDescription`、`leftVisible`、`rightVisible`、`onLeftClick`、`onRightClick` 迁入 `leftAction` / `rightAction`（`CouiNavigationBarAction`）；将 `rightContainerColor` 传给 `CouiNavigationBarDefaults.coloredActionStyle(containerColor = …)`；将 `circleShape` 移入各侧的 `style.shape`。中间胶囊统一使用 `CouiCapsuleShape`，升级时删除 `capsuleShape` 实参。保留附件配置并将其 `visible` 设为 `false` 可播放收起动画，传 `null` 会直接移除附件。
- Alpha19 通过 JitPack 发布，坐标为 `com.github.itoseo.coui-kit:coui:0.0.1-alpha19`；使用 `coui-material3` 或 `coui-icons` 时请同步升级至 `0.0.1-alpha19`。

## [0.0.1-alpha18] - 2026-11-24

### 新增与改进

- 为单选弹窗 `SingleChoiceDialog` 新增可选的 `message` 参数。说明文字显示在标题下方，与选项一起滚动，标题和底部操作区保持固定。[弹窗](docs/components/dialog.md#单选弹窗)
- 修复 Nav3 预测性返回的窗口圆角读取兼容性。窗口圆角 API 不可用时，沿用系统圆角资源。

### 升级说明

- Alpha18 通过 JitPack 发布。升级时，在 `settings.gradle.kts` 中添加 JitPack 仓库，将已使用的 COUI 模块统一切换到 `com.github.itoseo.coui-kit`，版本设为 `0.0.1-alpha18`。[快速开始](docs/guide/getting-started.md#jitpack)
- 核心库坐标为 `com.github.itoseo.coui-kit:coui:0.0.1-alpha18`。如使用 `coui-material3` 或 `coui-icons`，请同步切换到同一渠道和版本。

## [0.0.1-alpha17] - 2026-11-24

### 新增与改进

- 新增单选弹窗 `SingleChoiceDialog`，保持标题与底部操作区固定，中间列表独立滚动。支持通过 `CouiSingleChoiceItem` 配置行首图标与禁用态，并支持 0 至 4 行底部操作按钮。[弹窗](docs/components/dialog.md#单选弹窗)
- 新增 Nav3 预测性返回转场 `PredictiveBackNavDisplay` 与 `CouiBackPreset` 预设，支持 `AOSP` 跟手圆角卡片缩放和 `CHATGPT` 水平共享轴转场，兼容多标签与分栏场景。[Nav3 页面转场](docs/components/predictive-back.md)
- 统一全库中性模糊材质的主题染色管道，所有组件共用保亮度染色算法与库内固定强度。为 `Dialog` 系列、`Menu`、`SearchBar`、`MessageInputBar`、`SegmentedControl`、`ChipTab` 系列、顶栏组件及 `rememberCouiMaterialEffect` 统一补全或规范 `themeTint` 参数；菜单染色收敛至 AGSL 内部执行。[外观与预设](docs/guide/appearance.md#背景材质染色)
- 修复 `CouiGradientBlur` 的渐变模糊边界问题，避免屏幕外透明外沿拉低边界 alpha，防止顶边透出未模糊正文或产生外溢不透明边，并优化全高渐隐曲线的边界过渡。
- 新增 `coui-kit` 外部接入 Agent Skill，并在 README 补充安装指令与推荐工具，方便外部项目通过 AI 辅助接入组件。[SKILL.md](skills/coui-kit/SKILL.md)

### 升级说明

- 如果在调用括号内按位置传入 `Dialog` 的 `content`、`ChipTabButton` 的 `icon`、`TopAppBarButton` 的 `content`、`TopAppBarSearch` 的 `title` 或 `Menu` 的 `onSpotlightDiagnostic`，请改用命名参数或尾随 lambda。
- 开启 Material 3 适配后，弹窗、顶栏按钮、搜索栏、消息输入栏、分段选择器、分类栏、菜单、底栏和底部面板默认使用宿主 `surface` 作为材质染色基准色。传入 `themeTint = Color.Transparent` 或使用 `CouiTheme(materialTint = Color.Transparent)` 可关闭染色；自定义 `themeTint` 时，库会忽略颜色自身的 alpha 并应用统一强度。
- 核心库 `coui` 新增 `androidx.navigation3:navigation3-ui:1.2.1` 的 API 依赖。`coui`、`coui-material3` 和 `coui-icons` 继续使用同一版本，升级后重新编译宿主模块。

## [0.0.1-alpha16] - 2026-11-23

### 新增与改进

- 新增 `Dialog` 的 `actionRows` 重载，支持自定义 1 至 4 行底部按钮，例如上方一个通栏按钮、下方两个并排按钮。宽度不足时自动拆成竖排，操作区独立于正文滚动。[弹窗](docs/components/dialog.md#自定义底部操作行)
- 优化顶栏渐进模糊，带来更好的效果，传入 `useCouiBlur = true` 可切回旧实现。[顶栏](docs/components/top-app-bar.md)
- 新增 HCT 颜色增强函数 `enhanceHctColor(seed, tone)`，可从种子色生成指定色调的增强色，适合用于 FAB 等需要强调的控件。中性灰保持中性。[外观与预设](docs/guide/appearance.md)
- 新增删除、收藏、信息、编辑和分享图标，均提供 Filled 与 Outlined 两种样式。补充实心 AddCircle、实心 Send、文档扫描和翻译图标，调整描边 Send 的圆环与箭头样式。[图标](docs/components/icons.md)

### 升级说明

- 现有 `Dialog` 调用可直接沿用。需要自定义按钮排布时，改用 `actionRows` 重载。
- 如果在调用括号内按位置传入 `TopAppBarSurface` 的 `content` 或 `CollapsingTopAppBar` 的 `searchBar`，请改用命名参数或尾随 lambda。
- 如果希望保留 Alpha 15 的实心发送图标，请把 `CouiIcons.Outlined.Send` 改为 `CouiIcons.Filled.Send`。

## [0.0.1-alpha15] - 2026-11-22

### 新增与改进

- 优化顶栏渐变模糊，减轻浅色模式下的中段亮带和色阶分层。遮罩默认跟随页面底色。
- 修复顶栏搜索收起时的锚点错位。操作区布局变化后，搜索框会回到入口的当前位置。
- 优化触点菜单定位。菜单横向居中对准按下位置，靠近窗口边缘时向内移动。普通菜单最小宽度从 `168dp` 调整为 `160dp`。
- 新增局部菜单背景采样控制，可让指定范围内的菜单在模糊材质与纯色背景之间切换。
- 为 `BottomSheet` 新增 `themeTint` 参数。启用 Material 3 适配后，面板的模糊底图默认采用宿主的 `surface` 色染色。

### 升级说明

- 如果在 `BottomSheet(...)` 括号内按位置传入内容 lambda，改用 `content = { … }` 或移到括号外。原本用这两种方式的调用可直接升级。
- 如果启用了 Material 3 适配，且希望底部面板保持原来的未染色外观，传入 `themeTint = Color.Transparent`。
- 如果顶栏依赖原先的浅色白、深色黑遮罩，请在 `CouiTopAppBarDefaults.colors(backgroundColor = …)` 中显式指定颜色。直接使用 `TopAppBarSurface` 时，设置 `gradientColor`。

### 编译要求

宿主最低 `compileSdk` 提升至 `37.1`，运行最低版本仍为 API 31。

## [0.0.1-alpha14] - 2026-11-22

### 新增与改进

- 优化顶栏、底栏、FAB、搜索栏、菜单和弹窗的背景模糊与材质效果，修复部分场景下的马赛克和色阶分层。
- 调整 Popup 菜单的默认宽度。普通菜单为 `168–256dp`，含子菜单项时最小宽度为 `196dp`。 [PR #33](https://github.com/ItosEO/COUI-Kit/pull/33)

### 升级说明

- `coui`、`coui-material3` 和 `coui-icons` 使用同一版本。升级后重新编译宿主模块。

## [0.0.1-alpha13] - 2026-11-21

### 新增与改进

- 新增底部面板 `BottomSheet`，通过 `MutableState<Boolean>` 控制显隐，支持居中标题、两侧操作按钮、正文滚动回弹及下拉关闭。面板支持 API 31+，有宿主窗口背景源时可在 API 33+ 启用模糊材质，顶部按钮的浮动材质要求 API 34+。[底部面板](docs/components/bottom-sheet.md)
- 对齐底部面板 Fast Kawase 的降采样网格与放大后的混色顺序，减轻底图的色阶块。面板内的菜单和子菜单按面板可见范围定位，顶部按钮可用 `CouiSheetButton.popupContent` 挂载固定位置的菜单。[菜单](docs/components/menu.md)
- `ChipTabRow` 可继承容器提供的背景源，并支持分别设置选中底色与前景色。新增默认单行省略的 `CouiTextKind.SheetTitle`。[分类栏](docs/components/chip-tab.md)
- `WheelPicker` 支持单独设置滚轮文字的可见宽度和对齐方式，整列仍可触摸拖动。[选择器](docs/components/pickers.md)

### 升级说明

- `coui`、`coui-material3` 和 `coui-icons` 使用同一版本。升级后重新编译宿主模块。

## [0.0.1-alpha12] - 2026-11-18

### 新增与改进

- 新增消息输入栏 `MessageInputBar`（Android 14 / API 34+），自带随深浅主题切换的底部渐变遮罩和留白。宿主可按焦点切换提示文案，也可按草稿内容切换操作按钮。切换按钮时保留光标位置和选区。[消息输入栏](docs/components/message-input-bar.md)
- 新增 `rememberCouiImeInsets()`，供宿主页随软键盘调整布局。编辑时切到小窗，键盘收起后输入栏随之回落。[底部放置与键盘抬升](docs/components/message-input-bar.md#底部放置与键盘抬升)
- 新增 `CouiIcons.Outlined` 下的 `Camera`、`AddCircle`、`Send` 和 `Keyboard`，保留原厂日夜配色与透明度。新增菜单选择行的上下双箭头 `Filled.Popup` 和 `Outlined.Popup`，调整 `Outlined.Setting` 的描边。[图标](docs/components/icons.md)

### 升级说明

- `coui`、`coui-material3` 和 `coui-icons` 使用同一版本。alpha11 的公开调用可沿用，升级后重新编译宿主模块。
- 接入消息输入栏时，将 `MessageInputBar` 放入页面底栏，由宿主页统一处理键盘避让。组件会补足底部导航栏留白。

## [0.0.1-alpha11] - 2026-11-12

### 新增与改进

- 新增 `LoadingDialog`，居中显示加载动画和单行提示。宿主通过 `visible` 控制显隐。默认屏蔽外点关闭，可通过 `dismissOnClickOutside` 启用。[加载弹窗](docs/components/dialog.md#显示加载弹窗)
- 修正弹窗材质的合成顺序。先按窗口遮罩强度压暗采样底图，再做模糊和混色。[背景材质](docs/components/dialog.md#使用背景材质)
- 修复顶栏空白处的触摸穿透。顶栏布局之外的渐隐区域仍可与正文交互。[顶栏](docs/components/top-app-bar.md)
- 新增支持 RTL 自动镜像的 `CouiIcons.Filled.Next` 和 `CouiIcons.Outlined.Next`。[图标](docs/components/icons.md)

### 升级说明

- `coui`、`coui-material3` 和 `coui-icons` 使用同一版本。alpha10 的公开调用可沿用，升级后重新编译宿主模块。

## [0.0.1-alpha10] - 2026-11-10

以下为相对 `0.0.1-alpha9` 的变更。

### 新增与改进

- 新增胶囊分段选择器 `SegmentedControl`（Android 14 / API 34+）。支持点按、横向拖动、弹簧指示器、即时选择或抬手提交，以及普通填色和透明模糊两种外观。[分段选择器](docs/components/segmented-control.md)
- 统一普通顶栏与大标题顶栏的默认标题排版。普通 `TopAppBar` 为 `18sp`。`LargeTopAppBar` 展开为 `40sp`，有起始侧导航按钮时收起为 `18sp`，没有时为 `24sp`。默认折叠过程保持基础字重 `700`。普通顶栏和大标题顶栏的默认样式跟随系统字体大小与字重补偿。
- 常驻 `SearchBar` 的输入胶囊支持在展开、收起动画期间跟手移动和变形，松手后回弹。
- `Fab` 和 `NavigationBar` 的 `renderer` 改为可选。省略时组件各自创建绘制器。宿主也可自行处理初始化失败，并在创建成功后显式传入。

### 升级说明

- `LargeTopAppBar` 移除了 `expandedTitleSize` 和 `collapsedTitleSize`。把原有字号分别传给 `expandedTitleStyle = TextStyle(fontSize = …)` 和 `collapsedTitleStyle = TextStyle(fontSize = …)`。两套样式也可各自指定字体族、字重和颜色。[大标题顶栏](docs/components/collapsing-app-bar.md#普通大标题)
- 更新 `CouiTextKind` 枚举引用。将 `PreferenceTitle`、`PreferenceSummary`、`PreferenceValue` 分别改为 `ItemTitle`、`ItemSummary`、`ItemValue`。将 `Toolbar` 改为 `TopBar`，其字号从固定 `18dp` 改为 `18sp`。将 `Description` 改为 `GroupFooter`。
- 移除了 `Category`、`SearchCancel`、`PageTitle`、`SectionTitle`、`Body`、`Stepper` 和 `Progress`。直接使用这些预设的调用方可改用 Compose `TextStyle`。常驻搜索的取消文字样式由 `SearchBar` 内部提供。
- 预览版有源码不兼容变更。更新上述调用后，重新编译使用库的模块。

## [0.0.1-alpha9] - 2026-11-09

`coui`、`coui-material3` 与 `coui-icons` 同版本发行。源码见 [v0.0.1-alpha9](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha9)，Demo 下载见 [Release](https://github.com/ItosEO/COUI-Kit/releases/tag/v0.0.1-alpha9)。

- 新增 7 个账号页位图，通过 `CouiIcons.Bitmap` 获取 `Painter`。`DevicePair` 提供日间和夜间原图。
- 新增 `CouiIcons.Outlined.Scan` 和设置齿轮的 Filled、Outlined 样式。原多色设置图标改名为 `CouiIcons.Filled.SettingDetail`，升级时请更新原有引用。
- 新增 `CouiIcons.Filled.Headset`，在原耳机轮廓上填实两个耳罩。
- 网页图标库新增 Bitmap 筛选，图标按名称从 A 到 Z 排序。首页和图标库默认以 80% 比例显示。
- 将 AndroidX DynamicAnimation 升至 `1.1.0`，解除严格锁版。修复关闭系统动画时 Slider 松手后仍停在按下高度的问题。[PR #22](https://github.com/ItosEO/COUI-Kit/pull/22)

## [0.0.1-alpha8] - 2026-11-09

`coui`、`coui-material3` 与 `coui-icons` 同版本发行。源码见 [v0.0.1-alpha8](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha8)，发行附件见 [Release](https://github.com/ItosEO/COUI-Kit/releases/tag/v0.0.1-alpha8)。以下为相对 alpha7 的主要变更。

### 新增与调整

- 新增可选 `coui-icons` 模块，核心搜索组件不再依赖公开图标包。
- 图标使用 VectorDrawable XML 保存，传统 View 与 Compose 读取同一份资源；支持日夜配色、多色和渐变图标。入口按 `CouiIcons.Filled`、`CouiIcons.Outlined` 提供，补充设置、隐私与安全、相册和云服务图标。
- 新增 `CouiColor.Day`、`CouiColor.Night` 配色预设，包括 `GRAY` 和 `DARK_GRAY`。
- 新增项目首页和图标工具网页，支持跨页主题持久化、DarkLight 日夜切换、名称搜索、Filled/Outlined 筛选、配色预览及 SVG/ImageVector 源码复制。
- FAB 支持圆角方形与自定义圆角，统一内容、材质、光效和投影的几何；`foregroundComposition` 用于相邻 FAB 的前景合成，修复拖动时背景回填遮住其他按钮的问题。
- FAB 提供 `contentColor`，默认日夜均为不透明白色。`onClick` 为必传参数，允许传入空回调 `{}`。
- 底栏提供 `selectedColor` 和 `unselectedColor`，文字与图标共用状态色；选中态不透明度为 `230 / 255`，未选中态为 `184 / 255`。`selectedIcon` 省略时复用 `icon`，拖动预选仍由底栏处理。
- `LargeTopAppBar` 支持通过 `expandedTitleSize`、`collapsedTitleSize` 分别配置展开与收起字号。
- 修复顶栏与搜索栏的主题配色，常驻搜索材质读取当前主题的 `materialTint`。
- Demo 的效果导航使用 `WandSpark`，选中与未选中分别使用 Filled 和 Outlined，并更新启动图标。
- 发布流程覆盖三个库模块；Maven Central 在上传后异步校验和发布，保留上传前的 Lint 与发行产物检查。

### 升级说明

- 使用公开图标时，添加同版本的 `implementation("io.github.itoseo:coui-icons:0.0.1-alpha8")`，并将旧图标入口调整为 `CouiIcons.Filled.*` 或 `CouiIcons.Outlined.*`。图标需在 Composable 中获取，传统 View 使用对应的 drawable XML。
- `Search`、`Headset`、`SliderCustom` 和 `LayoutSpliced` 仅提供 Outlined。多色图标使用 `Image` 或 `Icon(..., tint = Color.Unspecified)` 保留原色；`AppUpdate(color)` 只调整背景，保留白色前景。
- 所有 `Fab` 调用均需提供 `onClick`。默认内容色不再取 `onAccent` 或外层颜色；使用 `contentColor` 显式覆盖，或传入 `Color.Unspecified` 继承外层内容色。
- 预览版不保证二进制兼容，升级后重新编译使用库的模块。

## [0.0.1-alpha7] - 2026-11-06

`coui` 与 `coui-material3` 同版本发行，源码见 [v0.0.1-alpha7](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha7)。发行附件及更新日志见 [Release](https://github.com/ItosEO/COUI-Kit/releases/tag/v0.0.1-alpha7)。以下为相对 alpha6 的主要变更。

- 修复 临时禁用纵向滚动会中断子内容横向拖动的问题，恢复图表连续选点和 tooltip 跟随；保留边界回弹，以及选点期间禁止上下滚动的行为。
- 调整 Demo 启动图标，更新前景矢量与白色背景。

## [0.0.1-alpha6] - 2026-11-05

`coui` 与 `coui-material3` 同版本发行，源码见 [v0.0.1-alpha6](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha6)。发行附件及更新日志见 [Release](https://github.com/ItosEO/COUI-Kit/releases/tag/v0.0.1-alpha6)。以下为相对 alpha5 的主要变更。

- 新增 顶栏默认背景遮罩，浅色使用白色、深色使用黑色，并向下渐隐。
- 优化 顶栏背景渐隐曲线，平滑中间节点与底部过渡，减轻渐变接痕。
- 优化 渐进模糊的颜色计算精度，减轻横向色带。
- 调整 顶栏模糊强度，同步普通顶栏、大标题顶栏与旧平台预设。
- 调整 Demo 顶栏示例使用默认背景遮罩，同步背景颜色与透明遮罩的接入说明。
- 调整 Demo 本地与 CI 统一使用 `itos` 签名。
- 调整 Demo 版本名为 `0.0.1-alpha6`，与发行 Tag 对齐；版本码继续使用 Git commit count。

顶栏背景参数省略或为 `Color.Unspecified` 时使用默认遮罩。显式颜色覆盖默认值；`Color.Transparent` 关闭遮罩并保留模糊。升级时检查已有背景颜色覆盖，详见[顶栏背景参数](docs/components/top-app-bar.md#背景参数)。

## [0.0.1-alpha5] - 2026-11-05

`coui` 与 `coui-material3` 同版本发行，源码见 [v0.0.1-alpha5](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha5)。发行记录、验证结果及 Maven 坐标见 [Release](https://github.com/ItosEO/COUI-Kit/releases/tag/v0.0.1-alpha5)。以下为相对 alpha4 的主要变更。

### 组件命名迁移

公开 UI 组件统一简化名称。升级时同步修改 import、调用和函数引用；不保留旧名称转发。名称及下述参数变化属于源码及二进制不兼容变更，已发布版本的名称不变。

- `CouiText`、`CouiSwitch`、`CouiSlider`、`CouiCheckbox` → `Text`、`Switch`、`Slider`、`Checkbox`。
- `CouiMenu`、`CouiFab`、`CouiBottomBar`、`CouiScaffold` → `Menu`、`Fab`、`NavigationBar`、`Scaffold`。
- `CouiSearchBar`、`CouiCollapsingTopAppBar`、`CouiTopAppBar` → `SearchBar`、`CollapsingTopAppBar`、`TopAppBarSurface`；顶栏的 Button、ButtonGroup、ActionVisibility、Search、SearchButton、SearchAction 入口同样去掉前缀。
- `CouiChipTab`、`CouiChipTabButton`、`CouiChipTabRow` → `ChipTab`、`ChipTabButton`、`ChipTabRow`。
- `CouiAlertDialog`、`CouiInputDialog`、`CouiWheelPicker`、`CouiCalendarPicker` → `Dialog`、`InputDialog`、`WheelPicker`、`CalendarPicker`；开发快照中的 `AlertDialog` 也需改为 `Dialog`。

开发快照中已使用 `BottomBar` 的调用方，将 import 和调用改为 `com.itos.coui.navigation.NavigationBar`，参数保持不变。

`CouiTextKind`、Defaults、Style、状态等类型，以及 `CouiTheme`、`rememberCoui…` 工厂和 `coui…` 效果入口保持原名。与 M3 等同名组件混用时，通过 import 别名区分来源。

### 新增

- 新增受控 `WheelPicker`、`CalendarPicker` 和 `RadioButton`，分别提供滚轮、月历和单选交互。
- `TopAppBar` 提供完整的标题、导航与操作槽位；`LargeTopAppBar` 支持大标题折叠。仅需背景效果、继续使用宿主顶栏布局时，调用 `TopAppBarSurface`。
- `TopAppBarSearch` 支持可选右侧融合动作，内容由 `action` 提供，点击由 `onActionClick` 提交。
- `Dialog` 和 `InputDialog` 增加 `visible`，支持受控进退场与移动过程中的实时背景采样。传入时须保持调用在组合中，不用 `if (visible)` 提前移除；输入弹窗同步处理焦点、键盘关闭与位移避让。
- `Text` 增加可选 `maxLines`、`overflow`；Body 与 Description 默认按逻辑起始边对齐，显式 `textAlign` 优先。
- `SearchBar` 增加可选 `enabled`，统一控制点击、编辑、清除、取消、IME 提交和交互反馈，禁用时释放自身输入但保留宿主状态。取消文字宽度按文本、样式、密度、布局方向和测量器缓存。
- `Item` 移除无条件语义合并；整行点击沿用 clickable 的语义，纯布局保留子控件语义。`Group.headerActions` 仅随非空标题渲染。
- 移除无效的 `CouiPageDefaults.groupPadding` 与仅供 Demo 使用的 `homeRowMinHeight`；目录行高由 Demo 持有。Demo 顶栏使用既有 Defaults 并覆盖所需外观，同名图标直接使用 CouiIcons。
- `CouiPageDefaults.iconSpacing` 统一 Item 图标与文字的间距；Demo 的带图标分隔线由行内留白、图标尺寸和该间距计算缩进。
- `Group`、`Item`、`Divider` 和 `Footer` 提供可独立组合的设置式布局；`Item` 支持内容槽和可选整行点击。
- `Item` 的内容槽参数 `headline` 更名为 `title`，`supporting` 更名为 `summary`，`leading` 更名为 `icon`，图标槽尺寸 `leadingSize` 更名为 `iconSize` 并移至 `icon` 后；开发分支消费者需同步名称及受影响的位置参数，参数类型与默认值保持不变。
- 可选 `Scaffold` 提供栏位测量、正文 Padding 和系统安全区处理，核心无需 Material 3。采样、滚动、导航与主题由宿主分别组合。
- `CollapsingTopAppBar`、`SearchBar` 和相应状态提供折叠标题及常驻搜索；与既有顶栏搜索分别使用。
- `couiVerticalScroll` 提供 COUI 纵向惯性与边界回弹，支持短内容和空内容。拖动事件由可选 `interactionSource` 提供，位置与范围继续使用 `ScrollState`。
- `Text` 与 `rememberCouiTextStyle` 提供固定 COUI 排版角色；`CouiIcons` 提供系统功能矢量子集，来源与权利说明随源码和 API 文档归档分发。
- Demo 使用目录和独立 Activity 组织详情，提供目录搜索、共享外观偏好及完整页面装配示例。

### 既有接口与行为

- `LocalCouiContentColor` 的缺省值为 `Color.Unspecified`。COUI 文字和图标在消费处解析当前主题前景色；直接读取该 Local 的宿主需处理未指定值。显式文字颜色及图标的不染色语义保留。
- `CouiColorScheme` 增加独立 `background`，默认沿用 `surface`；原生页面与组件表面分别配色。M3 适配的菜单项和头部标签分别读取 `labelLarge` 与 `bodySmall`。
- `TopAppBarSurface` 支持背景透明度和全高渐隐曲线，保留显式渐变颜色的 alpha；`dark` 默认跟随 `CouiTheme`。
- 浮动表面增加双主体入口，复用既有材质、融合和光效。列表按压复用现有反馈入口。
- ChipTab 与纵向滚动共享物理内核，各实例的动画等级独立决定摩擦参数。

开发分支中临时布局名称及搜索配色参数的迁移说明见 [PR #16](https://github.com/ItosEO/COUI-Kit/pull/16)。Dialog 的显隐与宿主页键盘避让接线见 [弹窗文档](docs/components/dialog.md)。

## [0.0.1-alpha3] - 2026-11-01

`coui` 与 `coui-material3` 同版本发行。源码为 [v0.0.1-alpha3](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha3)，提交 `e40c1c413b68fa43924ac6e48620ad3e9d703a08`。发行文件见 [coui](https://repo.maven.apache.org/maven2/io/github/itoseo/coui/0.0.1-alpha3/) 与 [coui-material3](https://repo.maven.apache.org/maven2/io/github/itoseo/coui-material3/0.0.1-alpha3/)。两个模块均通过发行结构、GPG 签名和校验和检查。

升级需重新编译消费者，不承诺二进制 ABI。设备上的字体、嵌套主题和最小交互尺寸仍需验收，构建通过不表示视觉兼容已全部确认。

- 核心移除 Material 3 与 Material Icons 依赖，使用独立文本、图标和菜单定位；原生默认最小布局占位保留。
- 新增可选 CouiTheme、原生浅深色预设、普通字体和内容上下文；原组件通过默认参数简化装配，保留显式配置与底层 Effect。
- 新增可选 `coui-material3` 模块，通过 `Application.onCreate` 中的 `Coui.initialize(defaultStyle = CouiMaterial3Style())` 接入。组件在消费处读取 M3 颜色、字体和内容上下文；顶栏按钮恢复 M3 最小交互尺寸语义。
- `LocalCouiDarkTheme` 接收宿主模式；`CouiTheme` 支持局部原生或自定义外观。原生默认 tint 透明；M3 菜单与底栏默认使用宿主 surface 作已有色相层。
- Chip 与 Dialog 只接入宿主字体族，保留原生排版。Dialog 的布局测量和绘制共同响应异步字体结果。
- 菜单 Style 末尾增加可选 dark；自动默认不再通过表面亮度猜模式，旧完整 Style 调用保留。FAB 区分省略与显式材质，保留显式内容色与参数顺序。
- Demo 启动选择 M3，以局部 scope 切换原生外观并保留内容状态。PR 与快照发布同步检查、发行两个同版本 artifact，见[外观与预设](docs/guide/appearance.md)。

## 已合入 main 的近期改动（待建立发行版本映射）

以下是源码仓库的变更记录，不代表这些特性在每一个 Maven Central 预览版本中都存在。

### 2026-10-30

- [#9](https://github.com/ItosEO/COUI-Kit/pull/9)：按使用场景整理组件文档、快速开始和 README，补全接入示例。

### 2026-10-29

- [#8](https://github.com/ItosEO/COUI-Kit/pull/8)：统一 Touch/Press/Slider/Switch 等持续动画通道，调整中断与重定向。Touch/Press 运动轴收为只读 `State<Float>`，涉及二进制不兼容，原消费者需要重新编译。

### 2026-10-28

- [#7](https://github.com/ItosEO/COUI-Kit/pull/7)：新增 COUI Switch，包含受控点击、拖动吸附、加载和禁用反馈。
- [#6](https://github.com/ItosEO/COUI-Kit/pull/6)：新增普通警告弹窗与输入型弹窗预设，支持底部/居中窗口、受控操作及输入过滤。

### 2026-10-24 至 2026-10-25

- 新增 ChipTab；优化回弹中断和输入交接。
- 完善 Demo 按需签名打包、CI、MPL 2.0/NOTICE，以及 GitHub Packages 开发快照构建与发布入口。

## 已发布版本与源码对应关系

- Maven Central 的版本以 [实际包页面](https://central.sonatype.com/artifact/io.github.itoseo/coui)及远端元数据为准；[README](README.md)中的依赖示例使用双模块同版本发行。
- alpha3 的源码 Tag 与发行文件见上文；历史版本没有对应发行 Tag，不据此断言 `0.0.1-alpha1` 或 `0.0.1-alpha2` 分别对应哪个提交。
- 根目录 `VERSION_NAME=0.1.0-SNAPSHOT` 是开发/快照的基础版本，不代表 Maven Central 已公开 `0.1.0`。
- 今后每次不可变版本发布，需记录版本号、发布日期、源代码 Tag/Commit SHA、AAR 校验/发行地址、公开 API 兼容变化和迁移说明。再将上述记录归入对应版本段落。

发布渠道及执行方式见[发布指南](docs/development/publishing.md)。
