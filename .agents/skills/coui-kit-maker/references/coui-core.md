# coui 核心模块制作分册（组件 / 效果 / 主题）

本分册所有规则出自 COUI-Kit 仓库内文件，每条标注出处。路径均相对仓库根目录。

## 1. 模块定位与依赖边界

- coui 只持有通用 COUI 组件、效果、采样、几何、输入、动画及预设，依赖 Android、Compose 和 AndroidX；不得引用 demo、外部宿主模块、应用主题、业务状态、日志框架或第三方 Backdrop。（`AGENTS.md`「代码与模块边界」）
- 依赖配置（`coui/build.gradle.kts`）：`api(libs.androidx.compose.ui/animation/foundation)` + `implementation(libs.androidx.compose.runtime)` + `implementation(libs.androidx.dynamicanimation)` + `implementation(libs.androidx.core.ktx)`。版本声明集中在 `gradle/libs.versions.toml`（compose 1.13.0，dynamicanimation 1.1.0，coreKtx 1.19.1）。
- 打包：release 单变体 + SourcesJar + Dokka javadoc（`dokkaGeneratePublicationHtml`）；Jar 附带 `META-INF/coui` 下的根 `LICENSE`、`NOTICE`、`TRADEMARKS.md`、`coui/ICON_SOURCES.md`、`src/main/resources/META-INF/coui-skia-LICENSE`、`coui-androidx-LICENSE`。（`coui/build.gradle.kts`）
- 构建参数：`namespace = "com.itos.coui"`，`compileSdk = release(37){ minorApiLevel = 2 }`，`minSdk = 31`，`aarMetadata.minCompileSdk = 37.1`，`minAgpVersion = "9.1.0"`，Java 17，optIn `androidx.compose.foundation.style.ExperimentalFoundationStyleApi`。（`coui/build.gradle.kts`）

## 2. 包职责（新增入口放到实际负责的组件族）

| 包 | 职责 |
| --- | --- |
| layout | 可选页面容器、分组与行布局；Scaffold 管理局部正文源，不持有业务状态 |
| topappbar、search、navigation、floatingactionbutton、menu、slider、checkbox、radiobutton、switch、chip、dialog、picker、text | 局部组件的布局、状态装配、语义与公开 Defaults |
| backdrop | 所属窗口的内容源、绘制录制与采样坐标映射 |
| effect | 普通投影、模糊、材质、光效与融合绘制 |
| shape | 公共 Shape 与几何约定 |
| motion、input、scroll、swipereveal | 动画、触摸、滚动物理与公开运动预设 |
| diagnostic | 按需启停的开发诊断 |
| theme | 可选外观上下文、原生配色、普通字体与内容局部样式 |

出处：`docs/development/architecture.md`「库内职责」表。现有包与文件清单以 `coui/src/main/java/com/itos/coui/` 当前目录为准（如 `theme/` 下为 Coui.kt、CouiTheme.kt、CouiColor.kt、CouiContent.kt；`effect/` 下含 CouiFloatingShader.kt、CouiFloatingSurfaces.kt、CouiGradientBlur.kt、CouiShadowRenderer.kt、CouiMetaballRenderer.kt、CouiEdge.kt、CouiSpotlight.kt、CouiStateFeedback.kt、CouiEffectLocals.kt 等）。新入口放到实际负责的组件族；共享算法按职责复用，不按导入或文件名搬迁；不新增只转发参数的包装层。（同上表后注）

## 3. 新增能力的强制流程（原厂 1:1 对齐）

1. **固定目标与证据**：写清目标页面、控件、操作和状态；确认实际使用的组件、主题覆盖及分支；核对对应 APK/framework/native 的版本、构建标识或哈希；检索现有 Note，把结论分为原厂行为、公开平台适配、项目主动特化。（`docs/development/workflow.md`「官方 1:1 对齐」「开发与评审流程」§1）
2. **基础效果复用先行**：新增组件前必须检查 `docs/effects/README.md`、对应实现和已有消费者，先确定复用方案再写组件。现有实现满足原厂目标时必须复用，通过参数、所属 Defaults 和合成顺序完成装配；不足时先评估对公共 effect 做最小扩展；只有确认算法分支或执行语义无法复用才新增实现，并在 Note 与 PR 中说明检查过的候选、缺口、原厂依据及归属。（`workflow.md`「基础效果复用」）
3. **对照行为而不只对照参数**：至少区分受影响的正常完成、中途反向、未达到拖动阈值的 UP、CANCEL、父容器接管和节点移除；记录位置、速度、目标及积分临时状态由谁修改。（`workflow.md` §2）
4. **实现完整链路**：先还原数据与状态转换，再选择公开承载；所有会停止旧动画或接管手势的入口都要有完成、取消和释放路径。（`workflow.md` §3）
5. **双向核查**：原厂 → 移植找遗漏，移植 → 原厂找多做和改错；评审范围包含调用方及被复用的公共实现。（`workflow.md` §4）
6. **用能暴露差异的输入验证**：选择能区分原厂与移植行为的最小输入序列，检查中间状态、收尾及重新进入。（`workflow.md` §5）
7. **同步使用文档与 Note**：使用文档与实现进入同一个 PR。（`workflow.md`「同步使用文档」）

方法细节（定点分析 Java/资源、追 native、输入核对表、常见差异排查表、验证原理表）见 `docs/development/replication-guide.md` 全文——该文件是排查原厂差异时的必读章节。

## 4. 公开 API 设计规则

- 默认 `internal` 或 `private`；公开入口应能解释使用场景、状态 owner、平台门槛、单位、失败行为和资源生命周期。`Modifier` 为第一个参数并默认 `Modifier`。（`docs/development/api-design.md` 开头）
- 命名：公开 UI 组件用无 Coui 前缀名称（`Text`、`Switch`、`Menu`、`Scaffold`），包名区分来源；类型、Defaults、Style、状态、`CouiTheme`、`rememberCoui…` 工厂及 `coui…` 效果入口保留前缀；同文件混用其他设计体系同名组件时用 import 别名。（同上）
- 底层算法接口保留高自定义程度；普通阴影保留 `couiShadow` 与 `CouiShadowRenderer`；双主体融合只公开算法和跟随回弹，不加入侧滑容器；预设集中在所属组件族 Defaults。（同上）
- 内部装配（菜单滚动条坐标、拖动状态、Popup 定位、菜单动效帧、搜索运动实现、底栏附件控制器）不为了测试或 Demo 暴露；菜单模糊从 `rememberCouiMenuBlur` 获取，不让宿主组装固定数量的 GraphicsLayer。（同上）
- 参数归属：先确认配置修改哪个节点，再选入口——外部布局与独立装饰用标准 Modifier；内部多区域共用状态与样式用组件参数或 Defaults；多子节点排列与业务联动用组件组合或内容槽位。`enabled` 和 `onClick` 属于交互组件核心职责，不改成 Modifier；不新增 `couiWidth` 等标准 API 包装。（同上「参数归属与 Modifier 顺序」）
- Modifier 顺序是调用契约：`background().padding()` 背景包含留白，`padding().background()` 留白在背景外；`clickable().padding()` 留白可点击。效果入口须说明背景/内容范围、测量影响、坐标、裁剪和 padding 顺序；阴影通常放在主体裁剪外；背景滤镜只处理采样层。（同上）
- 同一几何应提供裁剪与效果所需参数：Dialog/Menu 从 `CouiShape` 读取半径和权重；不同几何（如 Checkbox 填充与边框）分别保留。（同上）
- 公开参数应说明单位、本地或窗口坐标、有效范围、输入错误、版本分支，以及状态和资源由谁持有；构造失败要向调用方暴露。（同上「常规入口与底层自定义」）
- 保留公开参数顺序；需要后置参数参与自动解析时，仅对相关配置使用可空默认值或原类型 Unspecified，不批量改成 null。（同上）
- 修改公开声明前检索库、Demo 和已知消费者；改变可见性、参数、单位或生命周期时同步调用与接入文档，并在 PR 写明迁移方式。（同上）

## 5. 代码规范（硬性）

出自 `docs/development/coding-style.md`：

- 文件 UTF-8 无 BOM；库 KDoc、解释性注释、开发文档及提交说明使用简体中文。
- 无状态预设使用 object，归属对应组件族 Defaults；组件可用原生 CouiTheme 默认值，宿主可显式覆盖 Shape、Style 和颜色；业务回调从宿主传入，核心不读取应用偏好或 MaterialTheme（可选 coui-material3 仅按外观契约在消费处读取 MaterialTheme）。
- 值、选择和展开目标采用受控状态与回调；宿主拥有业务状态，组件维护局部交互。
- 列表身份使用稳定且唯一的 key；绘制范围与命中范围分别确认，渐隐和裁剪不能代替输入限制。
- 整行承担交互时子控件不重复添加点击；保留禁用、键盘、焦点、TalkBack 和正确的选择语义。
- 输入观察器不能抢占点击或滚动；普通触摸提交只有一个 owner；CANCEL 不提交业务动作；停止动画、父容器接管和节点移除均有对应收尾。
- 不为复用增加只转发参数的层。
- 背景、效果和业务状态分开；每个绘制节点独立持有可变 renderer；动画、监听和图层随对应节点释放，不建立全局历史缓存。
- 不吞错误、不模拟成功、不新增静默降级；Shader 创建失败交给宿主记录并决定替代 UI；协程捕获 `CancellationException` 后重抛；不新增 `!!`。
- 用户文案进入所属模块字符串资源，默认英文并同步已有简体中文；库不依赖应用日志框架，需要诊断时用明确回调。
- 注释说明依据、约束、非直观意图和接口契约，不复述代码或记录修改历史；原厂证据与当前适配分开说明。

## 6. 主题体系（实现基线，读自 `coui/src/main/java/com/itos/coui/theme/`）

- `Coui`（Coui.kt）：进程级默认策略 object；`initialize(defaultStyle)` 只允许一次（重复调用 `check` 失败，消息「Coui 已初始化，不能重复设置默认样式」）；未初始化时 `defaultStyle = NativeCouiStyle`。
- `CouiStyle` 接口（Coui.kt）三个方法：`themeDefaults(dark)`、`ProvideContent(color, textStyle, mergeTextStyle, content)`、`minimumInteractiveComponentSize(): Modifier`。
- `NativeCouiStyle`（internal，Coui.kt）：返回 `CouiPresets.Light/Dark`；ProvideContent 解析 `LocalCouiContentColor`/`LocalCouiTextStyle`；最小交互尺寸用 `Modifier.layout` 保证 ≥48dp 并居中。
- `CouiColorScheme`（CouiTheme.kt）：字段 `accent, onAccent, surface, container, onSurface, onSurfaceVariant, outlineVariant` + 默认值 `accentOnSurface = accent`、`menuSurface = container`、`checkboxAccent = accent`、`menuCheckboxAccent = accentOnSurface`、`background = surface`、`card = container`。
- `CouiPresets`（CouiTheme.kt）：Light 用 `CouiColor.Day.BLUE` 强调、白色表面、`0xE6000000` 主文字、`background = 0xFFF0F1F2`；Dark 用 `CouiColor.Night.BLUE`、表面 `0xFF1E1E1E`、容器 `0xFF333333`、强调 `0xFF5C9DFF`、`background = Black`、`card = 0x1AFFFFFF`。
- `CouiTypography`（CouiTheme.kt）：`body`(16sp Normal)、`label`(12sp/16sp 行高/0.4sp 字距)、`menuItem`(14sp Medium/20sp/0.1sp)，均 `SansSerif` + `includeFontPadding = true`；另有 `chipLabelFontFamily` 与 dialog 五个字体族字段（仅适配字体族，不改字号字重）。
- CompositionLocals（CouiTheme.kt）：`LocalCouiDarkTheme: Boolean?`、`LocalCouiContentColor: Color（默认 Unspecified）`、`LocalCouiTextStyle`（默认 `CouiTypography().body`）。
- `CouiTheme.colorSchemeFor(dark)`（CouiTheme.kt）：局部 `CouiTheme` 存在且模式匹配则用其色板；模式不匹配回退对应模式原生预设，不复用另一模式的宿主色板；无局部主题且模式匹配 `Coui.isDark` 时走初始化策略。
- `CouiTheme(darkTheme, colorScheme, typography, materialTint, content)` composable（CouiTheme.kt）：提供 `LocalCouiTheme` + `LocalCouiDarkTheme` 并调用 `ProvideCouiContent(onSurface, typography.body, mergeTextStyle = false, content)`。
- 外观解析优先级（5 级：显式组件参数 → 局部 CouiTheme（模式匹配）→ 局部 CouiTheme（不匹配时回退原生预设）→ 全局初始化策略 → 默认原生回退）及色彩角色分工、M3 映射、染色表、插槽规则，出自 `docs/guide/appearance.md`——组件默认值实现须与该页一致。
- 公开文本入口是 `com.itos.coui.text.Text`；`com.itos.coui.theme` 包内的 `CouiText`、`CouiIcon`、`ProvideCouiTextStyle` 为内部实现，不作为公开接入入口。（`appearance.md`「内容插槽与最小交互尺寸」）

## 7. 效果能力清单与门槛（新增组件装配时必查）

出自 `docs/effects/README.md`：

| 效果 | 公开入口（便捷 / 底层） | 平台门槛 |
| --- | --- | --- |
| 背景采样 | `rememberCouiBackdrop` / `couiBackdrop` / `drawCouiBackdrop` / `ProvideCouiBackdrop` / `ProvideCouiMenuBackdrop` | API 31+（叠加 Shader 效果按对应门槛） |
| 普通阴影 | `Modifier.couiShadow` / `CouiShadowRenderer` / `CouiShadowDefaults` | API 34+（API 35+ 用公开 `Canvas.clipShader`） |
| 模糊与材质混色 | `CouiBackdropEffectScope.blur` / `drawCouiGradientBackdrop` / `rememberCouiMenuBlur` / `CouiCoverageBlurEffect` / `rememberCouiMaterialEffect` | 均匀模糊 API 31+；渐进模糊、材质混色、覆盖率归一化、菜单 Fast Kawase API 33+ |
| 浮动表面 | `couiFloatingSurface` / `couiFloatingPair` / `couiFloatingSurfaces` / `CouiFloatingRenderer` | API 34+ |
| 轮廓、跟手光与状态反馈 | `CouiContour` / `CouiEdge` / `rememberCouiSpotlight` / `couiNavigationFeedback` / `couiFabFeedback` / `couiToolbarFeedback` / `couiButtonFeedback` | 前三个 API 33+；状态反馈 Modifier API 31+ |
| 横向双主体融合 | `CouiMetaballRenderer` / `CouiMetaballBody` | API 33+ |
| 运动与跟随回弹 | `rememberCouiMetaballFollowingTranslation` / `rememberCouiTouchMotion` / `couiTouchMotion` / `rememberCouiPressMotion` / `couiPressMotion` | API 31+ |
| 边界回弹 | `couiVerticalScroll` / `rememberCouiOverscrollEffect` / `CouiOverscrollEffect` | API 31+ |

装配硬规则（同上「接入前核对」+ `docs/effects/backdrop.md`）：

- 是否需要背景源：`drawCouiBackdrop`、blur-material、floating-surfaces 需要兄弟节点或外层窗口提供的 `CouiBackdrop`；`couiShadow`、`CouiContour`、`rememberCouiSpotlight`、`CouiMetaballRenderer` 不采样背景。
- 逐节点持有可变资源：`CouiFloatingRenderer`、`CouiShadowRenderer`、`CouiContour`、`CouiEdge`、`CouiMetaballRenderer`、`CouiMenuBlur`、`CouiOverscrollEffect` 均按绘制节点独立创建与持有，禁止跨节点共享。
- 严禁自采样：源节点（`couiBackdrop`）与消费者必须是兄弟或外层分离关系。（`backdrop.md`）
- `CouiBackdropEffectScope.blur` 接收像素（px），scope 实现 `Density`；`shape` 裁剪采样背景、`onDrawSurface` 与子内容，`effects` 滤镜只作用于采样背景层。（`backdrop.md`）
- `ProvideCouiBackdrop` 的作用域消费者：`TopAppBar`、`LargeTopAppBar`、`TopAppBarSurface`、`CollapsingTopAppBar`、`NavigationBar`、`Fab`、`SearchBar`、`ChipTabRow` 读取最近作用域；未传参且无作用域抛异常。（`backdrop.md`「源、作用域与跨窗口规则」）
- 窗口源：菜单/材质弹窗/材质 Sheet 使用 `ProvideCouiMenuBackdrop` → `rememberCouiWindowBackdrop()`；COUI `Scaffold` 不自动安装窗口源；每个源只挂一个实际节点。（同上 + `docs/guide/secondary-pages.md`「正文源与窗口源」表）
- 平台失败处理：宿主在创建底层 Shader/Renderer 前检查 API 版本；需要错误 UI 时在 `remember { runCatching { ... } }` 工厂捕获构造结果再分支渲染，不把 Composable 内容放入普通 try/catch；`CancellationException` 必须继续传播。（`backdrop.md`「资源与失败处理」）

组件平台门槛速查出自 `docs/guide/compatibility.md`「效果门槛」+ `docs/components/README.md`：

- API 31+：背景源、普通控件、布局、顶栏容器、常驻 `SearchBar`（31–33 普通填色）、折叠顶栏、纵向回弹、`Scaffold`。
- API 33+：`RuntimeShader` 渐进模糊、轮廓光、双主体融合、菜单 Fast Kawase（工厂在 33 以下返回 null，菜单退回既有 Gaussian 分支）、Spotlight。
- API 34+（`@RequiresApi(34)`）：浮动 `NavigationBar`、`Fab`、`TopAppBarButton`、展开式顶栏搜索、`ChipTab`、`SegmentedControl`、普通 Mesh 阴影、`MessageInputBar`；调用前须检查系统版本并处理 renderer 创建失败。

## 8. 组件清单（docs/components/README.md 目录表）

页面容器与栏位：Scaffold、TopAppBar/TopAppBarSurface/TopAppBarButton、LargeTopAppBar/CollapsingTopAppBar、TopAppBarSearch、SearchBar、NavigationBar、Fab。列表与分组：Group、Item、Divider、Footer。选择与输入：Switch、Checkbox/RadioButton、Slider、ChipTab、SegmentedControl、MessageInputBar。浮层与选择器：Menu、Dialog（含 InputDialog/LoadingDialog）、BottomSheet、WheelPicker/CalendarPicker、CouiIcons。每一项的平台门槛见该 README 表与 `compatibility.md`。
