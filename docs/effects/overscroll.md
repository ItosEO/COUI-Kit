# 边界回弹

COUI 回弹移动滚动内容，使用系统 COUIRecyclerView 的越界阻尼和 SpringOverScroller 计算，不安装 Android glow 或 Compose stretch。

## 页面装配

以下片段放在宿主 Composable 中，content 是页面内容。容器应占满可交互视口，短内容和空内容才能在留白区域响应拖动。

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import com.itos.coui.scroll.couiVerticalScroll
import com.itos.coui.scroll.rememberCouiOverscrollEffect

val scroll = rememberScrollState()
val edge = rememberCouiOverscrollEffect()
Column(Modifier.fillMaxSize().couiVerticalScroll(scroll, overscrollEffect = edge)) {
    content()
}
```

宿主在实际内容滚动容器上使用 couiVerticalScroll，传 null 可以关闭。不要同时安装另一个 overscroll 或给同一容器叠加 verticalScroll。

## 状态和输入

每个容器分别 remember，一个 effect 不能用于两个布局节点。拖动在正常滚动及祖先消费后处理剩余位移；长、短和空内容共用同一个 Foundation scrollable 输入节点，触摸阈值、速度跟踪、多指切换及完整 nested scroll 分发均由 Foundation 处理。输入节点将滚动操作委托给宿主的真实 ScrollState，共用同一滚动互斥；内容范围跨过零不会替换输入节点，也不修改真实滚动范围。内侧 verticalScroll 负责测量、位置、范围语义和焦点滚入可见区，外侧不重复启动焦点及视口缩小动画。

ScrollState.value、maxValue、isScrollInProgress、scrollTo、animateScrollTo 和 stopScroll 继续操作同一状态。需要观察手指拖动时，在宿主 remember 一个 MutableInteractionSource，传给 couiVerticalScroll 的 interactionSource 参数，再通过该对象的 collectIsDraggedAsState 观察；拖动通知不发送到 ScrollState.interactionSource。交互源可以省略，不影响手势处理。

回弹绘制节点始终挂在同一外层位置，scrollable 只负责事件分发；首次测量和长短内容互换均不转移绘制节点归属。容器须占满可交互视口，留白区域同样可以起手。反向拖动先按 RecyclerView 的反向阻尼恢复边界，再交还跨边界的剩余输入。

松手和取消恢复边界；再次 DOWN 同时取消高速反向运动及回弹帧任务，未达到拖动阈值的 UP 也恢复回弹。高速同向释放保留原厂初速度，高速反向释放先回到边界，再交还内容。边缘完全吸收纵向速度时仍向滚动父链分发一次零纵向速度的 fling，父级可以完成自身的停止处理。禁用会移除效果节点并复位，节点移除停止帧任务。位置只属于局部运动，不保存为业务滚动值。

惯性越界中按下但尚未拖动时，UP 按原厂切回直接回弹；CANCEL 保留原厂惯性越界的显示映射。两者重新开始 springBack 时都取当前可见位置，不能把取消误当作一次新的惯性触边、恢复旧物理起点。

所有长度与速度在内核中使用像素。拖动阻尼距离取实例创建时的显示高度；惯性越界映射高度按原厂 onSizeChanged，在容器实际尺寸改变后重新读取当前显示高度。尺寸更新不重建 effect，也不中断已有运动。名义时间步长来自公开 Display 刷新率，回弹按帧时钟推进。RK4 与分类栏共享内核，纵向使用实际 RecyclerView 的张力配置，不能套用分类栏横向系数。

越界拖动的 UP/CANCEL 排除原厂短点按后调用当前 View 的 COUI 边缘触觉码；按 Settings 的实际入口，新增手指不重置点击基准，活动指针抬起后才切换到剩余手指，计时保留首次 DOWN；惯性首次进入越界时反馈一次，不在回弹帧中重复调用。可用 `rememberCouiOverscrollEffect(hapticFeedbackEnabled = false)` 关闭。系统触觉设置继续生效；不支持该码的 ROM 不强行播放另一种波形。禁用和节点移除只清理运动，不额外发触觉。

## 承载范围

运行门槛随库 minSdk。couiVerticalScroll 的正常惯性也复用同一 SpringOverScroller 内核，不再使用 Foundation 的默认 decay；触边帧先保留未消费位移并绘制原厂越界映射，同时把该帧速度交给同一边界轴，直接进入回弹，不重新预测一次自由 fling。边缘接管后向父级 postFling 返回零剩余纵向速度，避免重复消费；父级停止回调挂起时，边缘仍正常逐帧回弹。

单独把 effect 接到其他滚动宿主时，正常 fling 仍归宿主；标准 OverscrollEffect 回调不提供逐帧速度，边缘只能在宿主 performFling 返回后取得剩余速度，无法保证与上述入口相同的触边交接时序。要求完整 COUI 纵向惯性与同帧触边交接时使用 couiVerticalScroll。

纵向入口固定使用原厂 animationLevel=1 对应分支；不通过反射读取 persist.sys.oplus.anim_level。公开 Compose 仍承担测量与嵌套分发，不包含原厂 RecyclerView 的连续 fling 增速、多轴和全部窗口调度。Compose OverscrollEffect 仅作为分发/绘制接口，视觉、阻尼和积分均属于 COUI 实现；高速反向、多指、滚动条和边缘振感需要设备对照，不声明完整原厂滚动器已移植。

[公开 effect 与滚动入口](../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt)、[二级页装配](../guide/secondary-pages.md)。
