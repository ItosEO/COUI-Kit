# 边界回弹

`couiVerticalScroll` 提供纵向惯性滚动，内容到达边界后可继续拖动，松手时回弹。

## 基本用法

以下片段位于宿主 Composable 中，`content` 是页面内容。滚动容器占满交互视口，使短内容和空内容的留白区域也能响应拖动。

```kotlin
val scroll = rememberScrollState()
Column(Modifier.fillMaxSize().couiVerticalScroll(scroll)) {
    content()
}
```

每个容器使用独立 effect，默认参数会创建并记住该实例。需要关闭边界效果时传入 `overscrollEffect = null`；正常滚动仍使用 COUI 惯性。不要在同一容器再叠加 `verticalScroll` 或另一套 overscroll。

## 状态与拖动观察

宿主的 `ScrollState` 持有真实位置、范围和滚动互斥。`value`、`maxValue`、`isScrollInProgress`、`scrollTo`、`animateScrollTo` 和 `stopScroll` 均操作同一个状态。

子内容需要横向拖动时，可暂时设置 `enabled = false`，暂停用户纵向滚动。回弹节点保持挂载，子内容的当前手势可继续；恢复为 `true` 后可再次上下滚动。需要按住滑条时同时停止惯性滚动，可用[滑条与回弹的组合示例](../components/slider.md#在回弹页面中使用滑条)。

此入口的手指拖动事件通过可选 `interactionSource` 发出，不发送到只读的 `ScrollState.interactionSource`。下面片段同样位于 Composable 中；`dragged` 可供宿主 UI 读取。

```kotlin
val interactions = remember { MutableInteractionSource() }
val dragged by interactions.collectIsDraggedAsState()
val scrolling = Modifier.couiVerticalScroll(scroll, interactionSource = interactions)
```

将 `scrolling` 用于上述 Column，替换原来的滚动 Modifier。未传交互源时，手势仍正常工作。

## 生命周期与触觉

一个 effect 只能挂到一个布局节点。边界位移属于该实例，不写入业务滚动位置，也不保存为页面状态。再次按下会停止旧运动；松手和取消按各自分支恢复边界。移除效果节点时停止任务并清理位移。

触觉使用当前 View 的 COUI 边缘反馈码，并遵守系统触觉设置；不支持该码的 ROM 不替换为其他波形。关闭方式如下：

```kotlin
val edge = rememberCouiOverscrollEffect(hapticFeedbackEnabled = false)
val scrolling = Modifier.couiVerticalScroll(scroll, overscrollEffect = edge)
```

## 接入其他滚动容器

`CouiOverscrollEffect` 实现 Foundation 的 `OverscrollEffect`，可传给支持它的容器。此时正常惯性由该容器负责。标准接口不提供每帧速度，effect 只能在宿主 `performFling` 返回后取得剩余速度，触边时序与 `couiVerticalScroll` 不同。

需要 COUI 纵向惯性及同帧触边交接时，使用 `couiVerticalScroll`。它把触边帧的剩余位移和速度交给边界物理轴；已吸收的纵向速度不会再次交给父级消费。与折叠顶栏、Insets 和 IME 的组合见[宿主页面接入](../guide/secondary-pages.md)。

[公开 effect 与滚动入口](../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt) · [惯性实现](../../coui/src/main/java/com/itos/coui/scroll/CouiScrollFlingBehavior.kt)
