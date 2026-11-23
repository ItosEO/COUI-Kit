# CouiBackdrop 背景采样

## 正文背景源

顶栏、底栏或浮动按钮需要透出滚动正文时，在 COUI `Scaffold` 的相应槽位直接使用组件。宿主使用自己的容器时，通过 `rememberCouiBackdrop()` 创建源，将 `.couiBackdrop(backdrop)` 挂在铺满视口的正文节点上，在它内侧绘制不透明底色，把同一个源传给栏位。保持消费者在录制区域之外，避免采到自身。

`ProvideCouiBackdrop` 只向子树传递已有源。顶栏看不到滚动内容时，先检查实际录制节点及其范围。[TopAppBar 接入示例](../components/top-app-bar.md#topappbar)

## 窗口背景源

材质菜单、弹窗和底部面板采样所属窗口。优先复用已有窗口源。首次接入时，用 `ProvideCouiMenuBackdrop { windowSource -> … }` 包住页面，将 `.then(windowSource)` 挂在实际绘制页面背景内容的根节点一次。浮层自身及其遮罩位于录制区域之外。COUI `Scaffold` 的栏位使用单独的正文源。菜单、弹窗和底部面板推荐保留窗口源，以便启用各自的材质。[背景采样](https://github.com/ItosEO/COUI-Kit/blob/main/docs/effects/backdrop.md)

```kotlin
ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        pageContent()
    }
}
```

将现有页面放进 `pageContent`，浮层在独立窗口中绘制。[Demo 窗口根节点](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/MainActivity.kt#L43)
