# 运动与跟随回弹

需要在宿主自己的绘制中复用 COUI 动效时，使用运动工厂及输入 Modifier。它们观察触摸和更新运动值，业务点击与滚动仍由宿主处理。

## 双主体跟随

下面片段位于 Compose 内容中。revealPx 是宿主可观察的位移状态，使用像素；读取 translation 时再把结果用于当前几何或绘制变换。

~~~kotlin
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import com.itos.coui.motion.rememberCouiMetaballFollowingTranslation
import com.itos.coui.swipereveal.CouiSwipeRevealDefaults

val density = LocalDensity.current
val subjectSizePx = with(density) { CouiSwipeRevealDefaults.fusedActionSize.toPx() }
val followingDistancePx = with(density) { CouiSwipeRevealDefaults.menuFollowingDistance.roundToPx().toFloat() }
val translation by rememberCouiMetaballFollowingTranslation(
    distancePx = { revealPx },
    subjectSizePx = subjectSizePx,
    followingDistancePx = followingDistancePx,
)
~~~

距离递增时跟随，跨过主体尺寸与跟随距离之和后从当前位置回弹；距离归零时重置。两个尺寸须有限非负，合计为正；距离也须有限。reboundSpec 默认使用所属 Defaults，可显式提供动画规格。

这一入口不接管手势。RTL 与目标坐标由宿主映射，完整[融合绘制](metaball.md)应同时更新背景和遮罩。

## 触摸与按压

| 入口 | 用法 |
| --- | --- |
| rememberCouiTouchMotion / couiTouchMotion | 工厂随节点释放，Modifier 映射触点，宿主读取位移与缩放 |
| CouiTouchMotion | 需要显式 CoroutineScope 生命周期时使用；保持释放与取消路径 |
| rememberCouiPressMotion / couiPressMotion | 工厂随节点释放，Modifier 观察普通按压，宿主读取缩放 |
| CouiPressMotion | 显式作用域构造；与形变缩放的增量相加，不替换形变状态 |

运动对象在主线程创建、调用和释放。显式构造时使用主线程 CoroutineScope；取消该 scope 后弹簧停止，实例不再接收输入。Compose 工厂自动处理节点移除，不需要另建作用域。触点、位移及 IntSize 使用像素，缩放为倍率。

Touch 的四轴和 Press.scale 都是只读 State<Float>。在绘制阶段读取 .value；同一触摸组合中的形变与按压使用 `touchScale + pressScale - 1f`，不要相乘。独立外层的展开/收起缩放按父子变换相乘；自定义动画由宿主单独持有。

下面片段位于 Compose 内容中；Box 内放宿主的按钮内容。点击仍由宿主自己的 clickable 处理：

~~~kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.itos.coui.motion.couiPressMotion
import com.itos.coui.motion.couiTouchMotion
import com.itos.coui.motion.rememberCouiPressMotion
import com.itos.coui.motion.rememberCouiTouchMotion

val motion = rememberCouiTouchMotion(expanded = true)
val press = rememberCouiPressMotion()
Box(
    Modifier.graphicsLayer {
        translationX = motion.translationX.value
        translationY = motion.translationY.value
        scaleX = motion.scaleX.value + press.scale.value - 1f
        scaleY = motion.scaleY.value + press.scale.value - 1f
    }.couiTouchMotion(motion, expanded = true).couiPressMotion(press),
) {
    // 宿主按钮内容。
}
~~~

### 主动控制运动

手动驱动 CouiTouchMotion 时，使用 begin 开始触摸、move 更新位置、release 以输入速度回弹。cancel 只停在当前位置，保留底层速度与待处理目标；需要立即恢复未变形状态时调用 reset，它会同时清除旧动画与触摸会话。reset 不会移除坐标绑定，后续可开始新的触摸。CouiPressMotion 使用 press 和 release 控制按下与释放。

按压输入的 cancelOnConsumedMove 用于父容器消费移动后立即释放，并在本次触摸结束前保持取消。选择、光效、按压与滚动各自有状态 owner，不能用某一路已取消推断其他路径都已收尾。

预设集中在 CouiSwipeRevealDefaults；菜单公开展开弹簧仍可引用。使用参数含义和单位，不在宿主复制固定数值。

## 入口与示例

[跟随](../../coui/src/main/java/com/itos/coui/motion/CouiMetaballFollowing.kt)、[输入工厂和 Modifier](../../coui/src/main/java/com/itos/coui/motion/CouiTouchInput.kt)、[TouchMotion](../../coui/src/main/java/com/itos/coui/motion/CouiTouchMotion.kt)、[PressMotion](../../coui/src/main/java/com/itos/coui/motion/CouiPressMotion.kt)、[预设](../../coui/src/main/java/com/itos/coui/swipereveal/CouiSwipeRevealDefaults.kt)。跟随装配见 [DemoRecordSwipeRow](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSwipeRow.kt)。
