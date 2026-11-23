# CouiMaterial3Style 与 CouiTheme

已有 M3 页面想换用 COUI 开关、顶栏按钮或菜单时，可以保留页面的 `MaterialTheme`。先决定哪些组件跟随应用配色，哪些区域确实要使用 COUI 原生预设。

## CouiMaterial3Style

引入 `io.github.itoseo:coui-material3`。在已注册的 `Application.onCreate()` 中，创建 Compose UI 前调用一次 `Coui.initialize(defaultStyle = CouiMaterial3Style())`。适配器会在每个组件的使用处读取当前 `MaterialTheme`，动态色、主题动画和嵌套色板都能随之更新。切换主题时只更新宿主主题状态。

应用可独立于系统切换深浅色时，把选择 M3 色板的同一个 `dark` 提供给 COUI。下面的 `colors`、`typography` 和 `content` 都来自宿主现有的主题函数：

```kotlin
MaterialTheme(colorScheme = colors, typography = typography) {
    CompositionLocalProvider(LocalCouiDarkTheme provides dark) {
        content()
    }
}
```

让组件沿用默认的 `dark`、颜色和样式。用应用内开关把页面切到与系统相反的模式，检查菜单表面、选中项和复选框是否随页面切换。只切 M3 色板时，COUI 的深浅模式仍可能跟着系统走。

## 局部 CouiTheme

在目标子树外包 `CouiTheme(darkTheme = dark) { … }`。它会为该区域提供原生色板、排版与材质染色配置，离开作用域就恢复宿主策略。希望整页跟随 M3 时，让组件直接处于宿主主题下；在页面根部再包默认的 `CouiTheme` 会覆盖 M3 的配色。Demo 在同一页面切换两种外观就使用这两条路径。[Demo 主题](https://github.com/ItosEO/COUI-Kit/blob/main/demo/src/main/java/com/itos/coui/demo/DemoTheme.kt)

## 组件颜色覆盖

先看出问题的调用点。显式传入的颜色、`style` 或 `material` 优先于主题默认值；如果设计要求跟随 M3，去掉对应覆盖，再查包住这块内容的最近一层主题。

| 表现 | 先检查 |
| --- | --- |
| 页面已是动态色，分组卡片或强调色仍像原生 COUI | 需要当前主题时用 `CouiPageDefaults.themeColors()`。`colors(accent, dark)` 从原生预设生成配色，只替换强调色。 |
| 某个组件在深色页面突然使用另一套颜色 | 检查调用方是否显式传入了不同的 `dark`。组件模式与主题作用域不一致时，默认色板会回退到该模式的原生预设。 |
| 按钮背景正确，里面的 M3 文字或图标颜色却不对 | `TopAppBarButton` 等提供内容样式的槽位会桥接 M3 内容色与文字样式。检查子 `Text` 的显式颜色、`Icon` 的 `tint`，以及内层主题覆盖。自定义正文的排版由宿主决定。 |

角色映射和各槽位的继承规则见[外观与预设](https://github.com/ItosEO/COUI-Kit/blob/main/docs/guide/appearance.md)。
