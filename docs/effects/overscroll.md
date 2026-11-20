# 边界回弹

`Modifier.couiVerticalScroll` 提供 COUI 纵向惯性滚动、到达边界后的阻尼拉动与松手回弹（不绘制平台 glow 或 Compose stretch）。即使在短内容或空内容页面的留白区域，也能接收拖动并回弹。

- **平台门槛**：支持库最低系统版本（**API 31+**）。
- **公开入口**：
  - **便捷滚动入口**：`Modifier.couiVerticalScroll(state, enabled, overscrollEffect, interactionSource)`。
  - **边界效果工厂**：`rememberCouiOverscrollEffect(hapticFeedbackEnabled)`（返回实现 `OverscrollEffect` 的 `CouiOverscrollEffect`）。

## 基本用法

下面示例在宿主 Composable 中为一个纵向容器装配 COUI 惯性与边界回弹；容器占满交互视口，使短内容与空内容的留白区域也能响应拖动：

~~~kotlin
@Composable
fun OverscrollPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier
            .fillMaxSize()
            .couiVerticalScroll(scroll),
        content = content,
    )
}
~~~

接入时注意以下三点：

1. **单容器独立实例**：默认参数会为当前容器创建并记住一个独立的 `CouiOverscrollEffect`，不可多个节点共用同一个实例。
2. **禁止叠加其他滚动**：不要在同一容器上同时叠加 Compose 原生 `verticalScroll` 或另一套 `overscroll`。
3. **关闭边界效果**：需要关闭越界回弹时传入 `overscrollEffect = null`，正常滚动仍保留 COUI 纵向惯性。

## 状态与拖动观察

- **真实滚动状态归属**：宿主的 `ScrollState` 持有真实滚动位置、范围和滚动互斥；`value`、`maxValue`、`isScrollInProgress`、`scrollTo`、`animateScrollTo` 与 `stopScroll` 均操作同一个 `ScrollState`。
- **临时暂停纵向滚动（`enabled = false`）**：当子内容需要横向拖动时，可暂时传入 `enabled = false` 暂停用户纵向滚动；底层的回弹节点保持挂载，**不会打断子内容的当前手势**，恢复为 `true` 后可再次上下滚动。需要按住滑条时同时停止惯性滚动，见[在回弹页面中使用滑条](../components/slider.md#在回弹页面中使用滑条)。
- **观察手指拖动（`interactionSource`）**：`couiVerticalScroll` 的手指拖动事件通过可选参数 `interactionSource` 发出，**不会发送到只读的 `ScrollState.interactionSource`**：

~~~kotlin
@Composable
fun ObservedOverscrollColumn(
    modifier: Modifier = Modifier,
    onDraggedChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    val interactions = remember { MutableInteractionSource() }
    val dragged by interactions.collectIsDraggedAsState()
    LaunchedEffect(dragged) { onDraggedChange(dragged) }
    Column(
        modifier
            .fillMaxSize()
            .couiVerticalScroll(scroll, interactionSource = interactions),
        content = content,
    )
}
~~~

## 生命周期与触觉

- **节点独占与位移清理**：一个 `CouiOverscrollEffect` 实例只能挂到一个布局节点。越界位移归该实例持有，不写入业务 `ScrollState.value`，也不持久化为页面状态。再次按下会停止旧回弹运动，松手与取消按各自路径恢复边界；移除效果节点时停止协程任务并清理位移。
- **边缘触觉开关**：触边与越界释放（排除短点按）使用当前 `View` 的 COUI 边缘反馈码，并遵守系统触觉设置；不支持该反馈码的 ROM 不会替换为其他波形。关闭边缘触觉的方式如下（在 Composable 内使用）：

~~~kotlin
val edge = rememberCouiOverscrollEffect(hapticFeedbackEnabled = false)
val scrolling = Modifier.couiVerticalScroll(scroll, overscrollEffect = edge)
~~~

## 接入其他滚动容器

| 接入方式 | 正常惯性与触边交接行为 | 适用场景 |
| --- | --- | --- |
| `Modifier.couiVerticalScroll(...)` | 使用 COUI 纵向惯性，并在**触边同一帧**把剩余位移与速度交给边界物理轴；已吸收的纵向速度返回 `0f`，不会再次交给父级重复消费 | 普通纵向页面、短内容与空内容页 |
| 将 `CouiOverscrollEffect` 传给其他容器（如 `LazyColumn`） | 正常惯性仍由宿主容器的 `FlingBehavior` 负责；由于标准 `OverscrollEffect` 接口不提供每帧速度，effect 只能在宿主 `performFling` 返回后接收剩余速度，**触边时序与 `couiVerticalScroll` 不同** | 需要沿用外部列表容器自身的 `FlingBehavior` 时 |

与折叠顶栏、系统栏 `Insets` 和 `IME` 的页面级组合方式见[宿主页面接入](../guide/secondary-pages.md)。

## 公开入口

- **边界效果与滚动入口**：[CouiOverscrollEffect.kt](../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt)
- **纵向惯性实现**：[CouiScrollFlingBehavior.kt](../../coui/src/main/java/com/itos/coui/scroll/CouiScrollFlingBehavior.kt)
