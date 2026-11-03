# 外观与预设

组件可省略有默认值的外观参数，也可显式传入颜色、Style、Shape、材质和阴影。未初始化时使用原生 COUI 预设；可选 `coui-material3` 模块让默认值跟随宿主的 Material 3 主题。

## 不配置主题

在 Compose 中直接使用原生默认值，`checked` 和回调由宿主持有：

~~~kotlin
import com.itos.coui.switch.CouiSwitch

CouiSwitch(checked = checked, onCheckedChange = onCheckedChange)
~~~

底层 Effect 不要求主题。原生主题使用中性表面、文字与 Theme.COUI 的蓝色强调色，不读取动态色。

## 接入 Material 3

依赖、启动初始化和深浅模式配置见 [README：接入 Material 3](../../README.md#接入-material-3)。下文说明接入后的局部覆盖、字体、染色和内容作用域。

## 局部原生或自定义外观

`CouiTheme` 建立局部原生或自定义外观，退出后恢复外层策略。它同时提供自身模式。在全局使用 M3 适配时，局部主题仍桥接 M3 内容插槽。

~~~kotlin
import androidx.compose.ui.graphics.Color
import com.itos.coui.theme.CouiPresets
import com.itos.coui.theme.CouiTheme

val colors = (if (dark) CouiPresets.Dark else CouiPresets.Light)
    .copy(accent = Color(0xFF0080FF))
CouiTheme(darkTheme = dark, colorScheme = colors) {
    content()
}
~~~

显式组件配置始终优先。存在局部 `CouiTheme` 时，模式匹配则使用它；不匹配则直接使用该模式的 native 预设与透明 tint，不再尝试全局策略。没有局部 `CouiTheme` 时，模式匹配则使用初始化策略，不匹配则回退到对应 native 预设与透明 tint。不会套用另一模式的宿主色板。完整显式 Style、颜色、Shape、材质和阴影由调用方负责匹配。

`CouiTypography` 管普通内容、标签、菜单项及可选字体族。Shape 仍由各组件 Defaults 提供，适配器不把 COUI 几何改成 M3。

`CouiColorScheme.background` 是页面底色，`surface` 是组件表面，`container` 是分组卡片。原生预设区分页面与卡片；自定义色板省略 `background` 时沿用 `surface`。`CouiPageDefaults.themeColors()` 分别读取 `background` 和 `container`，宿主将页面底色传给自己的页面容器。`CouiPageDefaults.colors(accent, dark)` 从对应的原生浅色或深色预设生成 `CouiPageColors`，再应用显式强调色；它不会读取或创建主题作用域。

## Material 颜色与字体角色

| COUI 用途 | M3 来源 |
| --- | --- |
| 强调色 / 强调色上的内容 | `primary` / `onPrimary` |
| 菜单 selected | `primary` |
| 普通与菜单 checkbox | 浅色 `primary`，深色 `inversePrimary` |
| 表面、文字、次要文字、分隔线 | `surface`、`onSurface`、`onSurfaceVariant`、`outlineVariant` |
| 页面背景 / 分组卡片 | `background` / `surfaceBright` |
| 菜单模糊补底 / 无 Backdrop 的容器 | `surface` / `surfaceBright` |
| 普通内容 / 菜单项 / 菜单头部标签 | `bodyLarge` / `labelLarge` / `bodySmall` |

Chip 从 `labelLarge` 取字体族。Dialog 从 `titleLarge`、`bodyMedium`、`labelLarge`、`bodyLarge`、`labelSmall` 分别取得标题、正文、按钮、输入与计数字体族。两者的字号、字重和排版由组件控制；未启用 M3 适配时使用系统字体策略。

`CouiText` 的 `kind` 必填；省略 `color` 时使用 `LocalCouiContentColor.current`，显式颜色优先。菜单项的字号、字重与字体族统一由 `CouiTheme.typography.menuItem` 提供。

## 菜单、底栏与 FAB 的染色

原生默认 `materialTint` 透明。M3 策略使用宿主 `surface` 为菜单与底栏染色，保留材质亮度。关闭染色后，背景采样仍会包含页面颜色。

关闭染色时，菜单使用 `CouiMenuDefaults.style().copy(themeTint = Color.Transparent)`，底栏显式传 `themeTint = Color.Transparent`。自定义局部 `CouiTheme` 也可提供自己的 `materialTint`；透明表示关闭。显式材质始终优先。

默认 FAB 材质取强调色，阴影取最终 `material.firstColor`，不叠加页面 tint。省略材质时，内容色使用 `onAccent`；显式材质使用宿主 ambient 内容色。子图标的显式 tint 始终优先。配色预设见 [FAB](../components/fab.md)。底栏右附件图标固定为白色。

## 内容插槽与最小交互尺寸

启用 M3 策略后，顶栏按钮、搜索表面、Chip 和 FAB 的内容插槽提供对应的 M3 内容 Local，普通 M3 `Text`、`Icon` 可直接继承。组件部分 TextStyle 按 M3 `ProvideTextStyle` 顺序与宿主 ambient 合并；`CouiTheme` 的完整基线替换外层样式。子 Text 的显式颜色/样式、Icon tint 和内层 Local 优先。

仅转发的顶栏 title、buttonGroup、Chip leadingAction 和 Dialog 自定义正文不会被强设新样式。BottomBar 自有文字继承宿主 ambient 字体属性，同时保留自己的字号、行高与字重。内部 `CouiText`、`CouiIcon`、`ProvideCouiTextStyle` 不作为公共组件。

顶栏按钮的 M3 路径使用 `minimumInteractiveComponentSize()`，遵守宿主尺寸 Local 与 alignment lines；原生策略使用组件的最小布局尺寸。搜索按钮使用固定容器，不受该 Local 控制。

未启用适配器而在纯 native 核心插槽中使用 M3 内容时，仍需自行桥接其内容 Local；更常见的做法是接入可选模块。

## Shape 与效果开关

[CouiShape](../../coui/src/main/java/com/itos/coui/shape/CouiShape.kt)和[CouiCapsuleShape](../../coui/src/main/java/com/itos/coui/shape/CouiCapsuleShape.kt)可被宿主 Shape 替换。材质距离场、内容 Path 和投影 Outline 有各自的几何要求，不能因圆角同名就互换。

顶栏使用 `CouiToolbarStyle`，菜单使用 `CouiMenuStyle`。菜单默认工厂使用当前主题的 `dark`；自定义 Style 应显式传入模式，省略时按 surface 亮度判断。

轮廓与内晕通过 `LocalCouiContourGlowEnabled` 控制。显式构造 Style 时，应与对应阴影工厂使用同一值：

~~~kotlin
import androidx.compose.runtime.CompositionLocalProvider
import com.itos.coui.effect.LocalCouiContourGlowEnabled

CompositionLocalProvider(LocalCouiContourGlowEnabled provides contourEnabled) {
    content()
}
~~~

普通阴影、模糊、材质和跟手光仍由各自入口控制。固定预设直接引用所属 Defaults，不在宿主复制算法参数。
