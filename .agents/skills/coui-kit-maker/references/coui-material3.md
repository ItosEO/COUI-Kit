# coui-material3 适配模块制作分册

本分册规则出自 COUI-Kit 仓库内文件，每条标注出处。路径均相对仓库根目录。

## 1. 模块定位与边界

- coui-material3 是可选适配模块，只依赖 coui 与 M3；它在消费处读 MaterialTheme，不把 M3 类型、根色板缓存或运行时全局主题状态带入核心。（`AGENTS.md`「代码与模块边界」）
- 适配器只承担宿主 M3 默认值解析、内容 Local 桥接与 M3 最小交互尺寸；Application 通过 `Coui.initialize` 固定策略；适配器在组件消费处读取 MaterialTheme，不缓存根色板、不持有 Context、不接管业务状态；核心的 `CouiStyle` 只使用核心和基础 Compose 类型。（`docs/development/architecture.md`「模块边界」）
- 依赖配置（`coui-material3/build.gradle.kts`）：`api(project(":coui"))` + `api(libs.androidx.compose.material3)`（material3 1.5.0，见 `gradle/libs.versions.toml`）；`namespace = "com.itos.coui.material3"`；显式 `coordinates(artifactId = "coui-material3")`（构建文件注释：根属性仍由核心使用，适配模块必须有独立 artifactId）；Jar 附带 `META-INF/coui` 的根 `LICENSE`、`NOTICE`、`TRADEMARKS.md`。
- 全模块只有一个 Kotlin 源文件：`coui-material3/src/main/java/com/itos/coui/material3/CouiMaterial3Style.kt`（59 行）。

## 2. 初始化契约（使用侧，新增适配行为时不得破坏）

出自 `README.md`「接入 Material 3」与 `docs/guide/appearance.md`：

- 仅添加 `coui-material3` 依赖不会启用适配；必须在 `Application.onCreate()` 调用一次 `Coui.initialize(defaultStyle = CouiMaterial3Style())`（创建 Compose UI 前调用，并在 AndroidManifest 注册该 Application；策略初始化后不可重复调用——对应 `Coui.initialize` 的 check）。
- 宿主有独立深浅模式开关或嵌套 `MaterialTheme` 独立切换模式时，需在对应 `@Composable` 主题作用域边界提供 `LocalCouiDarkTheme`；库不从自定义色板猜测深浅模式。
- `coui`、`coui-material3` 与 `coui-icons` 同版本、同步发布；`coui-material3` 传递引入同版本核心；显式声明 `coui` 须保持同一版本。（`README.md` + `docs/guide/compatibility.md`「依赖对齐」）

## 3. CouiMaterial3Style 实现（唯一实现文件，逐条读自源码）

```kotlin
class CouiMaterial3Style : CouiStyle {
    override fun themeDefaults(dark: Boolean): CouiThemeDefaults { … }
    override fun ProvideContent(color, textStyle, mergeTextStyle, content) { … }
    override fun minimumInteractiveComponentSize(): Modifier = Modifier.minimumInteractiveComponentSize()
}
```

- `themeDefaults(dark)` 在 `@Composable` 中读取 `MaterialTheme.colorScheme` 与 `MaterialTheme.typography`（注释：支持动态色、动画和嵌套主题），角色映射：
  - `CouiColorScheme`：accent=`primary`，onAccent=`onPrimary`，surface=`surface`，container=`surfaceBright`，onSurface=`onSurface`，onSurfaceVariant=`onSurfaceVariant`，outlineVariant=`outlineVariant`，accentOnSurface=`primary`，menuSurface=`surface`，checkboxAccent/menuCheckboxAccent=深色 `inversePrimary`、浅色 `primary`，background=`colors.background`。
  - `CouiTypography`：body=`bodyLarge`，label=`bodySmall`，menuItem=`labelLarge`；`chipLabelFontFamily = labelLarge.fontFamily ?: FontFamily.Default`；dialog 五个字体族分别取 `titleLarge`、`bodyMedium`、`labelLarge`、`bodyLarge`、`labelSmall` 的 fontFamily（缺省 `FontFamily.Default`）。
  - `materialTint = colors.surface`（页面表面色染色，见外观页染色表）。
- `ProvideContent`：`color.isSpecified` 时直接用 color，否则取 M3 `LocalContentColor.current`；`mergeTextStyle` 为真时按 `LocalTextStyle.current.merge(textStyle)`（注释：与 M3 ProvideTextStyle 使用相同合并顺序；主题基线直接替换，组件局部样式才合并），否则用 textStyle 或 `TextStyle.Default`；同时提供 M3 与 COUI 两组 Local（`LocalContentColor`/`LocalCouiContentColor`、`LocalTextStyle`/`LocalCouiTextStyle`）。
- `minimumInteractiveComponentSize` 直接委托 M3 的 `Modifier.minimumInteractiveComponentSize()`（遵守宿主尺寸 Local 与 alignment lines）；原生策略（`NativeCouiStyle`）使用组件自身最小布局尺寸 48dp。（源码 + `appearance.md`「最小交互尺寸」）

## 4. 文字角色映射与插槽行为（外观页持有，适配实现须对齐）

出自 `docs/guide/appearance.md`「Material 颜色与字体角色」「内容插槽与最小交互尺寸」：

- M3 来源映射：强调色/其上内容=`primary`/`onPrimary`；菜单选中态=`primary`；普通与菜单 Checkbox=浅色 `primary`、深色 `inversePrimary`；表面、文字、次要文字、分隔线=`surface`/`onSurface`/`onSurfaceVariant`/`outlineVariant`；页面背景/分组卡片=`background`/`surfaceBright`；菜单模糊补底/无 Backdrop 容器=`surface`/`surfaceBright`；普通内容/菜单项/菜单头部标签=`bodyLarge`/`labelLarge`/`bodySmall`。
- 字体族：`ChipTab` 取 `labelLarge`；`Dialog` 取 `titleLarge`（标题）、`bodyMedium`（正文）、`labelLarge`（按钮）、`bodyLarge`（输入）、`labelSmall`（计数）；字号字重排版仍由组件自身控制。
- M3 插槽 Local 桥接：启用适配后 `TopAppBarButton`、搜索表面、`ChipTab` 与 `Fab` 的内容插槽自动提供 M3 内容 Local；子 `Text` 显式颜色/样式、子 `Icon` 显式 `tint` 及内层 Local 始终优先；未启用适配却在纯原生插槽中使用 M3 `Text`/`Icon` 时由宿主自行桥接。
- 仅转发样式的槽位：顶栏 `title`、`TopAppBarButtonGroup`、`ChipTab` 的 `leadingAction` 与 `Dialog` 自定义正文不强设新样式。
- 背景材质染色（M3 适配默认用宿主 `surface` 染色、保留材质亮度）：`Menu`、`NavigationBar`、`SearchBar`(API 34+)、`BottomSheet`(API 33+ 模糊材质) 均可用 `themeTint = Color.Transparent` 关闭；`Fab` 取 M3 `primary`、不叠加页面 `themeTint`，日夜默认内容色均为不透明白色，可用 `contentColor` 覆盖。

## 5. 制作新适配行为时的规则

- 修改 `CouiStyle` 接口或本模块公开行为属于「修改模块职责或公开接口」任务：先读 `docs/development/architecture.md`、`docs/development/api-design.md` 及受影响使用页（`AGENTS.md` 任务表）。
- 色彩/字体角色映射变化必须同步 `docs/guide/appearance.md` 的映射表（`docs/development/documentation.md`「参数、回调、受控状态或生命周期语义变化」行）。
- 历史取舍见 `.agents/notes/implemented/architecture/2026-10-31-optional-material3-style.md`（`docs/guide/customizations.md`「菜单文字排版」引用）。
