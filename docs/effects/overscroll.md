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

Demo 的完整二级页装配使用该能力；宿主在实际内容滚动容器上使用 couiVerticalScroll，传 null 可以关闭。不要同时安装另一个 overscroll 或给同一容器叠加 verticalScroll。

## 状态和输入

每个容器分别 remember，一个 effect 不能用于两个布局节点。拖动在正常滚动及祖先消费后处理剩余位移；短内容由同一入口在越过纵向触摸阈值后接管，不抢占按钮点按、横向滑条或子选择器。零滚动范围时保留 Foundation 的测量与 ScrollState，由短页输入处理越界，避免零范围分发门控。回弹绘制节点始终挂在同一外层位置，长页通过 withoutVisualEffect 包装只分发事件；首次测量、长短内容互换均不转移绘制节点归属。容器须占满可交互视口，留白区域同样可以起手。反向拖动先恢复边界，再交还正常内容。

松手和取消恢复边界；再次 DOWN 停止旧运动，未达到拖动阈值的 UP 也恢复回弹。禁用会移除效果节点并复位，节点移除停止帧任务。位置只属于局部运动，不保存为业务滚动值。

所有长度与速度在内核中使用像素。阻尼上限来自显示高度，名义时间步长来自公开 Display 刷新率，回弹按帧时钟推进。RK4 与分类栏共享内核，纵向使用实际 RecyclerView 的张力配置，不能套用分类栏横向系数。

越界拖动的 UP/CANCEL 排除原厂短点按后调用当前 View 的 COUI 边缘触觉码；惯性首次进入越界时反馈一次，不在回弹帧中重复调用。可用 `rememberCouiOverscrollEffect(hapticFeedbackEnabled = false)` 关闭。系统触觉设置继续生效；不支持该码的 ROM 不强行播放另一种波形。禁用和节点移除只清理运动，不额外发触觉。

## 承载范围

运行门槛随库 minSdk。couiVerticalScroll 的正常惯性也复用同一 SpringOverScroller 内核，不再使用 Foundation 的默认 decay；松手触边时把未消费速度交给 COUI 越界阶段。单独把 effect 接到其他宿主时，其正常 fling 仍归宿主。

当前内核使用原厂低动画等级分支；不通过反射读取 persist.sys.oplus.anim_level。公开 Compose 仍承担测量与嵌套分发，不包含原厂 RecyclerView 的连续 fling 增速、多轴和全部窗口调度。Compose OverscrollEffect 仅作为分发/绘制接口，视觉、阻尼和积分均属于 COUI 实现；高速反向、多指、滚动条和边缘振感需要设备对照，不声明完整原厂滚动器已移植。

[公开 effect 与滚动入口](../../coui/src/main/java/com/itos/coui/scroll/CouiOverscrollEffect.kt)、[二级页装配](../guide/secondary-pages.md)。
