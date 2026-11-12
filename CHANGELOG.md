# 更新记录（CHANGELOG）

COUI-Kit 处于早期预览阶段。本文记录公开 API、行为、接入和兼容要求的变化。日期以提交合并时间为准；版本归档以真实 Git Tag 和发行产物为准。

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
