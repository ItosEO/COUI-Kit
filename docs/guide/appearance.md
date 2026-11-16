# 外观与预设

COUI 组件的所有外观参数均提供默认值，也可显式传入颜色、`Style`、`Shape`、`material` 和 `shadow`。未全局初始化时默认使用原生 COUI 预设；引入可选 `coui-material3` 模块后，默认值会自动跟随宿主的 `MaterialTheme`。

## 不配置主题

直接在 `@Composable` 作用域中调用组件即可使用原生预设，状态与回调仍由宿主持有：

~~~kotlin
@Composable
fun NativeSwitchExample(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
~~~

- 原生主题采用中性表面、文字与 `Theme.COUI` 蓝色强调色，不读取壁纸动态色。
- 底层 Effect（阴影、模糊、轮廓光等）不依赖主题作用域。

## 接入 Material 3

依赖配置、启动初始化（`Coui.initialize(defaultStyle = CouiMaterial3Style())`）与独立深浅模式开关（`LocalCouiDarkTheme`）见 [README：接入 Material 3](../../README.md#接入-material-3)。下文说明局部主题覆盖、M3 角色映射、染色规则与插槽行为。

## 局部原生或自定义外观

使用 `CouiTheme` 可以为局部子树建立原生或自定义外观，退出作用域后恢复外层策略，同时提供自身的深浅模式；即使全局启用了 Material 3 适配，局部 `CouiTheme` 内仍会保持对 M3 内容插槽的桥接：

~~~kotlin
// 在宿主 @Composable 作用域中包裹需要自定义外观的子树
val colors = (if (dark) CouiPresets.Dark else CouiPresets.Light)
    .copy(accent = Color(0xFF0080FF))
CouiTheme(darkTheme = dark, colorScheme = colors) {
    content()
}
~~~

### 外观解析优先级

组件在读取外观配置时按以下顺序解析：

| 优先级 | 条件 | 实际生效外观 |
| --- | --- | --- |
| 1. 显式组件参数 | 显式传入颜色、`Style`、`Shape`、`material` 或 `shadow` | 直接生效，由调用方负责与组件 `dark` 匹配。 |
| 2. 局部 `CouiTheme` | 存在局部 `CouiTheme` 且组件 `dark` 与之匹配 | 使用局部 `CouiTheme` 的 `colorScheme`、`typography` 与 `materialTint`。 |
| 3. 局部 `CouiTheme`（模式不匹配） | 存在局部 `CouiTheme`，但组件 `dark` 与之不一致 | 直接回退到对应模式的原生预设（`CouiPresets.Light` / `CouiPresets.Dark`）与透明 `materialTint`，**不再尝试全局初始化策略**。 |
| 4. 全局初始化策略 | 无局部 `CouiTheme`，且组件 `dark` 与 `CouiTheme.isDark` 一致 | 使用 `Coui.initialize(...)` 设定的策略（默认 `NativeCouiStyle`，或 `CouiMaterial3Style`）。 |
| 5. 默认原生回退 | 无局部 `CouiTheme`，且组件 `dark` 与 `CouiTheme.isDark` 不一致 | 回退到对应模式的原生预设与透明 `materialTint`，不会套用另一模式的宿主色板。 |

### 色彩与排版角色分工

- **`CouiColorScheme` 表面色分工**：

  | 颜色字段 | 用途说明 | 缺省回退与预设行为 |
  | --- | --- | --- |
  | `background` | 页面底色 | 自定义色板省略时沿用 `surface` |
  | `surface` | 组件与弹窗表面色 | 必填 |
  | `container` | 通用容器底色 | 必填；菜单等顶层容器另使用 `menuSurface`（默认 `container`） |
  | `card` | 列表分组卡片底色 | 自定义色板省略时沿用 `container`；`CouiPresets.Dark` 使用半透明白色 `0x1AFFFFFF` 叠加页面背景 |

- **页面分组配色工厂**：
  - `CouiPageDefaults.themeColors(dark)`：分别读取当前主题的 `background` 与 `card` 等角色，适合随 `CouiTheme` 或 Material 3 主题联动；宿主应将页面底色传给自己的页面容器。
  - `CouiPageDefaults.colors(accent, dark)`：从对应的原生浅色/深色预设生成 `CouiPageColors` 并覆盖强调色，**不会读取或创建主题作用域**。
- **排版与形状**：`CouiTypography` 管理普通内容、标签、菜单项及可选字体族；`Shape` 仍由各组件 `Defaults` 提供，M3 适配不会把 COUI 几何替换为 Material 形状。

## Material 颜色与字体角色

启用 `CouiMaterial3Style` 后，COUI 语义角色与 Material 3 角色的映射如下：

| COUI 用途 | Material 3 来源 |
| --- | --- |
| 强调色 / 强调色上的内容 | `primary` / `onPrimary` |
| 菜单选中态（`selected`） | `primary` |
| 普通与菜单 `Checkbox` | 浅色模式 `primary`，深色模式 `inversePrimary` |
| 表面、文字、次要文字、分隔线 | `surface`、`onSurface`、`onSurfaceVariant`、`outlineVariant` |
| 页面背景 / 分组卡片 | `background` / `surfaceBright` |
| 菜单模糊补底 / 无 `Backdrop` 的容器 | `surface` / `surfaceBright` |
| 普通内容 / 菜单项 / 菜单头部标签 | `bodyLarge` / `labelLarge` / `bodySmall` |

字体与内容色解析补充规则：

- **局部字体族定制**：`ChipTab` 取 `labelLarge` 的字体族；`Dialog` 分别取 `titleLarge`（标题）、`bodyMedium`（正文）、`labelLarge`（按钮）、`bodyLarge`（输入）与 `labelSmall`（计数）的字体族。两者的字号、字重与排版仍由组件自身控制；未启用 M3 适配时使用系统字体策略。
- **COUI 文本组件（`com.itos.coui.text.Text`）**：提供固定 COUI 排版角色（`kind` 必填），文字颜色依次取「显式 `color` → 已提供的 `LocalCouiContentColor` → 当前 `CouiTheme.colorScheme.onSurface`」，在独立布局中省略颜色即可跟随主题（详见 [Item](../components/item.md#配色与文字)）。菜单项则读取 `CouiTheme.typography.menuItem` 的字号、字重与字体族。
- **`LocalCouiContentColor` 约定**：缺省值为 `Color.Unspecified`（表示未提供内容色）。直接读取该 Local 的宿主应在未指定时自行取当前主题色；COUI 的文字和图标已在消费处完成解析。显式颜色（包含 `Color.Transparent`）始终优先；图标显式传入 `tint = Color.Unspecified` 时保留不染色语义。

## 菜单、底栏、搜索栏与 FAB 的染色

| 组件 | 原生默认 | M3 适配默认 | 关闭染色 / 自定义方式 |
| --- | --- | --- | --- |
| `Menu` | 透明（不染色） | 使用宿主 `surface` 染色（保留材质亮度） | 传入 `CouiMenuDefaults.style().copy(themeTint = Color.Transparent)` |
| `NavigationBar` | 透明（不染色） | 使用宿主 `surface` 染色（保留材质亮度） | 显式传入 `themeTint = Color.Transparent`；右附件图标固定为白色 |
| `SearchBar`（API 34+） | 透明（不染色） | 使用宿主 `surface` 染色（保留材质亮度） | 通过局部 `CouiTheme` 的 `materialTint` 调整或关闭染色 |
| `Fab` | 取主题强调色，不叠加页面 `themeTint` | 取 M3 `primary`，不叠加页面 `themeTint` | 日夜默认内容色均为不透明白色（显式 `material` 同样适用）；可通过 `contentColor` 覆盖（传 `Color.Unspecified` 继承外部内容色），子图标显式 `tint` 优先 |

- 局部 `CouiTheme(materialTint = ...)` 同样可提供自定义染色，`Color.Transparent` 表示关闭。
- 关闭染色后，背景采样仍会自然包含后方页面颜色；显式传入的 `material` 参数始终优先。
- FAB 配色预设详见 [FAB](../components/fab.md)。

## 内容插槽与最小交互尺寸

- **M3 插槽 Local 桥接**：启用 `CouiMaterial3Style` 后，`TopAppBarButton`、搜索表面、`ChipTab` 与 `Fab` 的内容插槽会自动提供 M3 内容 Local，直接放置 M3 `Text`、`Icon` 即可继承颜色与文字样式。
  - 组件局部 `TextStyle` 按 M3 `ProvideTextStyle` 顺序与宿主 ambient 样式合并；`CouiTheme` 的完整基线则会替换外层样式。
  - 子 `Text` 的显式颜色/样式、子 `Icon` 的 `tint` 以及内层 Local 始终优先。
  - 未启用 `coui-material3` 却在纯原生插槽中使用 M3 `Text`/`Icon` 时，需由宿主自行桥接 M3 内容 Local。
- **仅转发样式的槽位**：顶栏 `title`、`TopAppBarButtonGroup`、`ChipTab` 的 `leadingAction` 与 `Dialog` 自定义正文不会强设新样式。`NavigationBar` 自有文字继承宿主 ambient 字体属性，同时保留自身的字号、行高与字重。
- **公开文本入口**：普通文字组件请使用公开的 `com.itos.coui.text.Text`；`com.itos.coui.theme` 包内的 `CouiText`、`CouiIcon` 和 `ProvideCouiTextStyle` 为内部实现，不作为公开接入入口。
- **最小交互尺寸**：M3 策略下 `TopAppBarButton` 使用 `Modifier.minimumInteractiveComponentSize()`，遵守宿主尺寸 Local 与 alignment lines；原生策略使用组件自身最小布局尺寸（`48.dp`）。`TopAppBarSearchButton` 使用固定容器，不受该 Local 控制。
- **独立复选框容器**：在行外独立使用 `Checkbox` 时，外层点击容器应隐藏涟漪并保留点击与无障碍语义，见[只放一个复选框](../components/checkbox.md#只放一个复选框)。

## Shape 与效果开关

- **自定义 Shape**：[CouiShape](../../coui/src/main/java/com/itos/coui/shape/CouiShape.kt) 与 [CouiCapsuleShape](../../coui/src/main/java/com/itos/coui/shape/CouiCapsuleShape.kt) 可被宿主 `Shape` 替换；但材质距离场、内容 Path 与投影 Outline 有各自的几何要求，切勿仅因圆角名称相同就随意互换。
- **Style 构造与深浅模式**：顶栏使用 `CouiToolbarStyle`，菜单使用 `CouiMenuStyle`。`CouiMenuDefaults.style()` 会自动携带当前主题的 `dark`；手动构造自定义 `CouiMenuStyle` 时建议显式传入 `dark`，省略时将按 `colors.surface` 亮度自动判断。
- **轮廓与内晕总开关**：通过 `LocalCouiContourGlowEnabled`（默认 `true`）控制。显式构造 `Style` 时，必须与对应阴影工厂传入同一值：

  ~~~kotlin
  // 在宿主 @Composable 作用域中控制描边与内光晕开关
  CompositionLocalProvider(LocalCouiContourGlowEnabled provides contourEnabled) {
      content()
  }
  ~~~

  普通阴影、模糊、材质与跟手光仍由各自效果入口控制；固定预设直接引用所属 `Defaults`，不要在宿主复制算法参数。
