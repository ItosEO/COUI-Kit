# 边界回弹

COUI 回弹在滚动边界移动内容，使用 COUIRecyclerView 的越界阻尼和 SpringOverScroller 计算。`couiVerticalScroll` 同时提供纵向惯性与触边交接。

## 基本用法

以下片段位于宿主 Composable 中，`content` 是页面内容。滚动容器占满交互视口，使短内容和空内容的留白区域也能响应拖动。

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import com.itos.coui.scroll.couiVerticalScroll

val scroll = rememberScrollState()
Column(Modifier.fillMaxSize().couiVerticalScroll(scroll)) {
    content()
}
```

每个容器使用独立 effect，默认参数会创建并记住该实例。需要关闭边界效果时传入 `overscrollEffect = null`；正常滚动仍使用 COUI 惯性。不要在同一容器再叠加 `verticalScroll` 或另一套 overscroll。

## 状态与拖动观察

宿主的 `ScrollState` 持有真实位置、范围和滚动互斥。`value`、`maxValue`、`isScrollInProgress`、`scrollTo`、`animateScrollTo` 和 `stopScroll` 均操作同一个状态。

此入口的手指拖动事件通过可选 `interactionSource` 发出，不发送到只读的 `ScrollState.interactionSource`。下面片段同样位于 Composable 中；`dragged` 可供宿主 UI 读取。

```kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

val interactions = remember { MutableInteractionSource() }
val dragged by interactions.collectIsDraggedAsState()
val scrolling = Modifier.couiVerticalScroll(scroll, interactionSource = interactions)
```

将 `scrolling` 用于上述 Column，替换原来的滚动 Modifier。未传交互源时，手势仍正常工作。

长、短和空内容共用同一个输入节点，内容范围变化不会切换手势处理器。Foundation 负责触摸阈值、多指、速度跟踪和父级 pre/post 回调；标准 `verticalScroll` 的内部布局与焦点节点负责测量、位置、范围语义和焦点避让。该装配的理由与替代方案见[输入连续性记录](../../.agents/notes/implemented/bug-fix/2026-11-03-page-input-continuity.md)。

## 生命周期与触觉

一个 effect 只能挂到一个布局节点。边界位移属于该实例，不写入业务滚动位置，也不保存为页面状态。再次按下会停止旧运动；松手和取消按各自分支恢复边界。禁用或移除效果节点时停止任务并清理位移。

触觉使用当前 View 的 COUI 边缘反馈码，并遵守系统触觉设置；不支持该码的 ROM 不替换为其他波形。关闭方式如下：

```kotlin
import com.itos.coui.scroll.rememberCouiOverscrollEffect

val edge = rememberCouiOverscrollEffect(hapticFeedbackEnabled = false)
val scrolling = Modifier.couiVerticalScroll(scroll, overscrollEffect = edge)
```

尺寸变化更新当前越界映射所需的显示高度，保留实例与在途运动。内部长度和速度使用像素；宿主无需换算。

## 接入其他滚动容器

`CouiOverscrollEffect` 实现 Foundation 的 `OverscrollEffect`，可传给支持它的容器。此时正常惯性由该容器负责。标准接口不提供每帧速度，effect 只能在宿主 `performFling` 返回后取得剩余速度，触边时序与 `couiVerticalScroll` 不同。

需要 COUI 纵向惯性及同帧触边交接时，使用 `couiVerticalScroll`。它把触边帧的剩余位移和速度交给边界物理轴；已吸收的纵向速度不会再次交给父级消费。与折叠顶栏、Insets 和 IME 的组合见[宿主页面接入](../guide/secondary-pages.md)。

## 实现范围

运行门槛随库 minSdk。纵向入口使用原厂 `animationLevel=1` 分支，与 ChipTab 共享积分内核，但保留各自张力和显示映射。它不读取私有系统属性，也不包含原厂 RecyclerView 的连续 fling 增速、多轴和完整窗口协议。

原始与候选 Settings 材料的哈希不同；同源差异、UP/CANCEL 映射、反向速度及排队输入限制由[输入与共享效果记录](../../.agents/notes/implemented/bug-fix/2026-11-02-secondary-page-review-fixes.md)说明。高速反向、多指、边缘触觉和设备调度仍需动态对照。

[公开 effect 与滚动入口](../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt) · [惯性实现](../../coui/src/main/java/com/itos/coui/scroll/CouiScrollFlingBehavior.kt)
