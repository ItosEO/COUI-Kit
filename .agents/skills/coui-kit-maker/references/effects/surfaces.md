# 模糊、阴影与浮动表面

## 背景模糊与材质

对已有菜单、弹窗和顶栏，推荐使用对应组件的材质预设。自绘模糊面板时，把记录节点和显示结果的节点并列放置，文字和其他前景绘制在材质之上。以下在同一个 `Box` 里复用页面的形状和不透明底色，`radiusPx` 是像素半径。[Demo 背景模糊](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoBlurTutorial.kt#L35)

```kotlin
val backdrop = rememberCouiBackdrop()
Box(Modifier.fillMaxSize()) {
    Box(Modifier.matchParentSize().couiBackdrop(backdrop).background(pageColor)) {
        backgroundContent()
    }
    Box(Modifier.drawCouiBackdrop(backdrop, shape = { panelShape }, effects = { blur(radiusPx) })) {
        panelContent()
    }
}
```

需要顶栏的渐进模糊时直接使用 [TopAppBar](../components/top-app-bar.md)；均匀模糊会让整个面板采用同一半径。自定义两层材质混色使用 `rememberCouiMaterialEffect`，在 API 33+ 按先模糊、后混色的顺序组合效果。[Demo 材质混色](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt#L62)

## 普通阴影

API 34+ 的静态胶囊只需要投影时，用 `Modifier.couiShadow` 和与主体一致的轮廓。半径由胶囊高度确定，父布局留出投影空间。[Demo 阴影](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt#L68)

```kotlin
val style = CouiShadowDefaults.menu(CouiTheme.isDark)
Box(
    Modifier
        .size(width = 160.dp, height = 56.dp)
        .couiShadow(style, CouiShadowOutline.SmoothCapsule, radius = 28.dp)
        .background(surfaceColor, CouiCapsuleShape),
)
```

## 浮动表面与轮廓光

推荐先用 [Fab](../components/fab.md)、[NavigationBar](../components/navigation-bar.md) 或 [SearchBar](../components/search.md)，保留组件已有的材质、投影和轮廓光。显式调色时同步更新材质与阴影，并让阴影读取 `LocalCouiContourGlowEnabled.current`。页面提供轮廓光偏好时默认开启。自绘浮动表面需要 API 34+、不透明正文源以及每个节点独立的绘制器，按单、双、三主体选择对应入口。[Demo 浮动按钮](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoChromeDetail.kt#L123) · [浮动表面 API](https://github.com/ItosEO/COUI-Kit/blob/main/docs/effects/floating-surfaces.md)
