# 状态反馈、运动与回弹

## 点击与跟手光

使用 [Menu](../components/menu.md)、[Fab](../components/fab.md) 等已有控件时，让组件处理支持平台上的光效和反馈。给自定义设置行添加点击时，让反馈与点击共用一个交互源；静止、按压、禁用状态据此保持一致。菜单展开期间需要持续高亮触发行时，再传 `hoverLocked = expanded`。[Demo 菜单触发行](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoPreferenceControls.kt#L54)

```kotlin
val source = remember { MutableInteractionSource() }
val rowModifier = Modifier
    .couiButtonFeedback(CouiTheme.isDark, source, enabled, list = true)
    .clickable(source, indication = null, enabled = enabled, role = Role.Button, onClick = onOpen)
```

## 纵向滚动与边界回弹

普通纵向页面使用 `couiVerticalScroll`，将正文留白放在滚动修饰符内侧。需要控制回弹时，给这个滚动容器持有独立的 `rememberCouiOverscrollEffect()`。已有 `LazyColumn` 时，保留其滚动机制，将效果传给列表的 `overscrollEffect` 参数。[Demo 设置页滚动](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt#L106) · [边界回弹](https://github.com/ItosEO/COUI-Kit/blob/main/docs/effects/overscroll.md)

```kotlin
Column(
    Modifier.fillMaxSize()
        .couiVerticalScroll(rememberScrollState())
        .padding(padding)
        .consumeWindowInsets(padding),
) { pageContent() }
```

## 横向融合与跟随回弹

侧滑卡片与操作按钮需要同色融合时，在同一绘制坐标系中准备两个 `CouiMetaballBody`，用 `CouiMetaballRenderer` 画连接区域（API 33+）。跟随回弹用实际拖动距离驱动，主体尺寸、跟随距离和返回位移使用像素。宿主在手势完成时决定是否执行业务操作。[Demo 两主体绘制](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoRecordMetaball.kt#L104) · [Demo 跟随位移](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoRecordSwipeRow.kt#L308)

```kotlin
val translationPx by rememberCouiMetaballFollowingTranslation(
    distancePx = { (-offsetPx.value).coerceAtLeast(0f) },
    subjectSizePx = actionSizePx,
    followingDistancePx = followingDistancePx,
)
```
