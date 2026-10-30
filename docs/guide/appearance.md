# 外观与预设

直接调用 COUI 组件时，可以省略有预设的外观参数。组件默认跟随系统深浅色，使用库内的中性表面、文字与蓝色强调色。也可以用可选的 `CouiTheme` 统一配置外观，或继续逐个传入颜色、Style 和 Shape。

本文介绍当前源码中的主题与默认参数；发行版本的区别见[兼容要求](compatibility.md#原生主题与默认参数迁移)。

## 不配置主题

在 Compose 内容中，`checked` 与 `onCheckedChange` 由宿主持有，开关不需要额外的主题装配：

~~~kotlin
import com.itos.coui.switch.CouiSwitch

CouiSwitch(checked = checked, onCheckedChange = onCheckedChange)
~~~

底层 Effect 同样不要求 Theme。Backdrop、Renderer、所属窗口和平台条件仍按各自的接口提供。

## 统一 COUI 配色

在宿主的 Compose 内容中，使用原生预设或复制其中的色彩角色：

~~~kotlin
import androidx.compose.ui.graphics.Color
import com.itos.coui.theme.CouiPresets
import com.itos.coui.theme.CouiTheme

val colors = (if (dark) CouiPresets.Dark else CouiPresets.Light)
    .copy(accent = Color(0xFF0080FF), accentOnSurface = Color(0xFF0080FF))
CouiTheme(darkTheme = dark, colorScheme = colors) {
    content()
}
~~~

`dark` 和 `content` 由宿主提供。`CouiColorScheme` 区分控件强调色、表面内容和弹出表面上的强调色。原生深色预设中，这两种强调色对应不同原厂角色。`CouiTypography` 提供普通内容、标签与菜单项样式；菜单项采用 `DefaultAdapter.setTitle` 使用的 `couiTextBodyL` 角色，不能与旧名 `couiTextAppearanceBodyL` 混为同一字号。Dialog 保留自己的系统字体、行距与字体缩放策略。

颜色解析顺序为：显式组件参数 → 与最终深浅色匹配的 COUI Theme → 对应模式的原生预设。组件省略 `dark` 时先跟随 COUI Theme，没有 Theme 时跟随系统。如果组件显式指定了另一模式，则不复用外层的异模式色板或页面染色。显式传入的颜色与完整 Style 由调用方负责匹配，库不会再次覆盖。

Shape 继续由各组件 Defaults 提供，也可通过现有参数或 Style 覆盖；主题不强制统一 Dialog、菜单和浮动表面的几何。

## 接入 Material 3

Material 3 适配由宿主完成，核心不依赖 Material 3 或 Material Icons。下面的函数可以放入已使用 M3 的宿主模块；它不创建动态色，宿主原先启用动态色时，映射结果才随之变化：

~~~kotlin
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.itos.coui.theme.CouiColorScheme
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.CouiTypography

@Composable
fun CouiMaterial3Theme(dark: Boolean, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    CouiTheme(
        darkTheme = dark,
        colorScheme = CouiColorScheme(
            accent = colors.primary, onAccent = colors.onPrimary,
            surface = colors.surface, container = colors.surfaceBright,
            onSurface = colors.onSurface, onSurfaceVariant = colors.onSurfaceVariant,
            outlineVariant = colors.outlineVariant,
        ),
        typography = CouiTypography(
            body = MaterialTheme.typography.bodyLarge,
            label = MaterialTheme.typography.labelLarge,
            menuItem = MaterialTheme.typography.bodyLarge,
        ),
        materialTint = colors.surface,
        content = content,
    )
}
~~~

在宿主的 `MaterialTheme` 内调用此函数，并显式传入宿主的深浅色状态。当前提供 Demo 与宿主示例，尚无单独发行的 `coui-material3` 模块。

## 菜单、底栏与 FAB 的染色

原生 COUI 使用中性色材质，默认 `materialTint` 为透明。仅在外层放置 `MaterialTheme` 不会自动启用染色。上述适配示例显式提供页面表面色后，默认菜单 Style 和底栏会启用已有的额外页面色相染色；它保留材质亮度，不参与文字、图标或模糊采样。此效果是项目特化，不是原厂 COUI 默认材质。关闭额外染色后，背景采样仍会自然包含页面本身的色彩。

FAB 与右侧附件另有语义底色。省略颜色时使用当前 COUI 强调色；原生为原厂蓝色，Material 适配后取宿主强调色。显式选择 `Notes`、`Phone` 等有色预设，或传入自定义材质时，保留调用方的选择。FAB 不自动叠加菜单的页面色相层。宿主已有的 HCT 取色仍由宿主执行，并将同一最终底色交给材质和投影。默认 FAB 投影从 `material.firstColor` 取色，避免两条链分别取色。

只需关闭页面色相染色时，在适配函数中省略 `materialTint` 或传入 `Color.Transparent`。这不会关闭强调色，也不会取消原厂有色 FAB 预设。显式的 `CouiMenuStyle.themeTint`、`CouiBottomBar.themeTint` 和底层材质参数仍优先。

## M3 内容插槽迁移

独立核心不再提供 M3 的 `LocalContentColor` 与 `LocalTextStyle`。在 COUI 按钮或 Chip 的插槽中使用 M3 `Icon/Text` 时，显式读取 COUI 内容上下文：

~~~kotlin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle

// 在 COUI 内容插槽中，icon、label 与 description 由宿主提供。
Icon(icon, description, tint = LocalCouiContentColor.current)
Text(label, color = LocalCouiContentColor.current, style = LocalCouiTextStyle.current)
~~~

这会保留组件提供的禁用颜色与文字样式。宿主显式指定图标色或文字样式时，可以继续使用自己的值。根部 Material 主题映射不能代替每个插槽的内容上下文接线。

## 选择预设

| 场景      | 公开入口                                         |
|---------|----------------------------------------------|
| 滑条      | CouiSliderDefaults.colors                    |
| 复选框     | CouiCheckboxDefaults.colors                  |
| 顶栏按钮与搜索 | CouiTopAppBarButtonDefaults、CouiToolbarStyle |
| FAB     | CouiFabColor、CouiFabDefaults                 |
| ChipTab | CouiChipTabDefaults                          |
| 普通阴影    | CouiShadowDefaults                           |
| 融合跟随    | CouiSwipeRevealDefaults                      |

固定参数直接引用所属 Defaults。宿主覆盖的主题色与 Shape 是外观装配，不称为原厂默认。顶栏强度、搜索收起行为和菜单组合光效含已有项目适配，具体用法见各组件页。

FAB 的最终底色同时用于 material 与 shadow；分别再次取色会造成材质和阴影不一致，示例见 [FAB](../components/fab.md)。

## Shape 与样式对象

[CouiShape](../../coui/src/main/java/com/itos/coui/shape/CouiShape.kt)接受半径与角权重，[CouiCapsuleShape](../../coui/src/main/java/com/itos/coui/shape/CouiCapsuleShape.kt)提供胶囊。宿主可传入自己的 Shape；材质距离场、内容 Path 和投影 Outline 各有几何要求，不能认为同名圆角在所有绘制链中相同。

顶栏使用 [CouiToolbarStyle](../../coui/src/main/java/com/itos/coui/topappbar/CouiToolbarStyle.kt)描述文字、光效、形状、材质和阴影。菜单使用 [CouiMenuStyle](../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)描述表面、项目、复选框及文字样式。它们只描述外观，不读取 App 偏好或持有业务状态。

## 控制轮廓光

在 Compose 内容中，用同一个宿主状态控制对应组件的轮廓和内晕：

~~~kotlin
import androidx.compose.runtime.CompositionLocalProvider
import com.itos.coui.effect.LocalCouiContourGlowEnabled

CompositionLocalProvider(LocalCouiContourGlowEnabled provides contourEnabled) {
    content()
}
~~~

contourEnabled 是宿主 Boolean，content 是要展示的 Compose 内容。显式构造 CouiToolbarStyle 时，它的 contourGlowEnabled 也使用同一值。普通阴影、模糊、材质和跟手光分别由各自入口控制。
