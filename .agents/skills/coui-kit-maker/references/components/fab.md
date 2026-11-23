# Fab 浮动按钮

在 API 34+ 将 `Fab` 放入 COUI `Scaffold.floatingActionButton`。默认底色取主题强调色，内容在深浅模式下都默认白色。使用 M3 适配时，让按钮内的 M3 `Icon` 继承内容色；若图标仍显示成另一种颜色，先检查图标自身的 `tint`。

多个按钮直接使用各自的默认绘制器。需要显式创建 `renderer` 时，每个按钮持有一个独立实例。[Fab 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/fab.md)

默认强调色直接沿用组件预设。需要特定有色按钮时，将同一种预设色同时用于材质和阴影；下面是 API 34+ 槽位中的“新建”按钮。[Demo 预设色按钮](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoChromeDetail.kt#L124)

```kotlin
val dark = CouiTheme.isDark
val color = CouiFabDefaults.containerColor(CouiFabColor.Notes, dark)
Fab(
    onClick = onAdd,
    material = CouiFabDefaults.material(color, dark),
    shadow = CouiFabDefaults.shadow(color, LocalDensity.current.density, LocalCouiContourGlowEnabled.current),
) {
    Icon(CouiIcons.Filled.Add, contentDescription = addDescription)
}
```
