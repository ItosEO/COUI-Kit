# 主题、文本、取色与图标

## Material 3 适配

- 引同版本 `coui-material3`，在 `Application.onCreate()` 创建 Compose UI 前调一次 `Coui.initialize(defaultStyle = CouiMaterial3Style())`（重复调用抛异常）。
- `CouiMaterial3Style()` 默认从 `primary` 生成 HCT 强调色；传 `useHctColors = false` 则直接沿用 M3 原始强调色（深色 `Checkbox` 取 `inversePrimary`）。
- 跟随 M3 时不要在页面根部包 `CouiTheme`（它会屏蔽全局 M3 策略）；仅对需要原生或自定义色板的局部子树包 `CouiTheme(darkTheme = dark)`。
- 支持应用内独立切深浅色时，向主界面、独立悬浮窗及改模式的嵌套 `MaterialTheme` 边界提供 `LocalCouiDarkTheme`：

```kotlin
MaterialTheme(colorScheme = colors, typography = typography) {
    CompositionLocalProvider(LocalCouiDarkTheme provides dark) { content() }
}
```

### 角色映射与默认染色

- 默认映射（启用 HCT）：`primary` 经 HCT 增强彩度并保留原色调 → `accent`（菜单选中态、光标等）；按浅/深模式固定色调 → `controlAccent`（弹窗主操作、默认 `Fab` / 底栏有色附件、开关、滑条、单/复选框填色）；`background` / `surfaceBright` → 页面底色 / 分组卡片底色。
- M3 下 `Menu`、`NavigationBar`（胶囊与默认透明附件）、`SearchBar`（API 34+）与材质 `BottomSheet` 默认用宿主 `surface` 做保亮度染色；传 `themeTint = Color.Transparent`（菜单用 `CouiMenuDefaults.style().copy(themeTint = Color.Transparent)`）关闭。`NavigationBar` 顶层 `themeTint` 仅控制中间胶囊，透明附件染色由 `style.material.themeTint`（`transparentActionStyle(material = ...)`）控制；`Fab` 与有色附件 `coloredActionStyle()` 不叠加页面 `themeTint`。

## 配色排查

显式颜色、`style`、`material` 优先级高于主题；要跟随主题请删除显式覆盖。

| 现象 | 原因与修正 |
| --- | --- |
| 动态色页卡片/强调色仍为原生色 | `CouiPageDefaults.colors(accent, dark)` 只读原生预设。改用 `CouiPageDefaults.themeColors()`。 |
| 某组件突变为原生配色 | 显式传入的 `dark` 与外层 `CouiTheme.isDark` 不一致，回退原生预设并清空 `materialTint`。去掉或对齐 `dark`。 |
| 按钮背景对，子 M3 文字/图标色错 | `TopAppBarButton`/`Fab`/`ChipTab`/搜索槽已桥接 M3 内容 Local。去掉子 `Text.color`、子 `Icon.tint` 及内层主题。 |
| 流光轮廓开关无效 | 根部提供 `LocalCouiContourGlowEnabled provides enabled`；自建 `Style`/阴影时传入同一值。 |

## 文本、HCT 取色与图标

- **文本**：独立布局用 `com.itos.coui.text.Text(text = ..., kind = CouiTextKind.xxx)`（省略 `color` 自动跟主题）。**禁止调用** `com.itos.coui.theme` 内部 `CouiText`、`CouiIcon`、`ProvideCouiTextStyle`。
- **HCT**：显式增强单色（如自定义有色 `Fab`）调 `enhanceHctColor(seed, tone)`（要求 `seed.isSpecified`、`tone in 0.0..100.0`）；固定预设 `CouiFabColor` 直接使用，勿再经 HCT 处理。
- **图标（`coui-icons`）**：底栏配对同名 `Outlined`/`Filled`（勿预乘透明度）；设置行菜单提示用 `CouiIcons.Outlined.Popup`（不旋转、不单绑点击）；多色图标用 `Image` 或 `Icon(..., tint = Color.Unspecified)`；单色省略 `tint`；搜索栏已内置放大镜，勿重复添加。
