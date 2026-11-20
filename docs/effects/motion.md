# 运动与跟随回弹

需要在自定义绘制中复用 COUI 动效时，使用运动状态工厂及输入 Modifier。它们只观察触摸或位移状态并输出只读 `State<Float>`，不接管业务点击、滚动或提交。

- **平台门槛**：支持库最低系统版本（**API 31+**）。
- **公开入口**：
  - **双主体跟随回弹**：`rememberCouiMetaballFollowingTranslation`（预设来自 `CouiSwipeRevealDefaults`）。
  - **触摸形变与拉伸**：`rememberCouiTouchMotion` + `Modifier.couiTouchMotion`（底层手动类 `CouiTouchMotion`）。
  - **普通按压缩放**：`rememberCouiPressMotion` + `Modifier.couiPressMotion`（底层手动类 `CouiPressMotion`）。

## 双主体跟随

下面片段位于 Compose 内容中；`revealPx` 是宿主可观察的位移状态（单位为 **px**），读取 `translation` 时把结果用于当前几何或绘制变换：

~~~kotlin
val density = LocalDensity.current
val subjectSizePx = with(density) { CouiSwipeRevealDefaults.fusedActionSize.toPx() }
val followingDistancePx = with(density) { CouiSwipeRevealDefaults.menuFollowingDistance.roundToPx().toFloat() }
val translation by rememberCouiMetaballFollowingTranslation(
    distancePx = { revealPx },
    subjectSizePx = subjectSizePx,
    followingDistancePx = followingDistancePx,
)
~~~

跟随契约：

- **单位与参数约束**：`distancePx`、`subjectSizePx` 与 `followingDistancePx` 均使用 **px**；`subjectSizePx` 和 `followingDistancePx` 须为有限非负值且**两者之和为有限正数**，`distancePx()` 返回值须有限。
- **阶段行为**：距离递增时按比例跟随；跨过 `subjectSizePx + followingDistancePx`（或跟随达到 `-followingDistancePx`）后按 `reboundSpec`（默认 `CouiSwipeRevealDefaults.menuReboundSpec`）从当前位置回弹至 `0f`；距离归零（`<= 0f`）时取消回弹并立即重置为 `0f`。
- **与融合绘制联动**：本入口不接管手势，RTL 与目标坐标由宿主映射；配合[横向双主体融合](metaball.md)使用时，应同时更新背景与内容遮罩的坐标。预设直接引用 `CouiSwipeRevealDefaults`，无需复制固定常量。

## 触摸与按压

| 入口 | 生命周期管理 | 坐标与输出 | 适用场景 |
| --- | --- | --- | --- |
| `rememberCouiTouchMotion(expanded)` + `Modifier.couiTouchMotion(motion, expanded, targetSize)` | 随 Compose 节点自动释放与取消 | 内部映射窗口坐标触点；输出只读 `translationX`、`translationY`（**px**）与 `scaleX`、`scaleY`（倍率） | 按钮、卡片或浮层的拖拽拉伸与形变 |
| `CouiTouchMotion(scope, density, deformationEnabled)` | 调用方传入主线程 `CoroutineScope` 管理生命周期 | 手动驱动 `begin`/`move`/`release`；`deformationEnabled = false` 时仅保留位移拉伸 | 非 Modifier 触摸流或需要手动控制轴运动 |
| `rememberCouiPressMotion()` + `Modifier.couiPressMotion(press, enabled, cancelOnConsumedMove)` | 随 Compose 节点自动释放与取消 | 观察按下与取消；输出只读 `scale`（倍率） | 普通按压缩放回弹 |
| `CouiPressMotion(scope, density)` | 调用方传入主线程 `CoroutineScope` 管理生命周期 | 手动驱动 `press(size)` / `release()` | 自定义触摸会话中的按压缩放 |

下面示例在 **Compose** 内容中组合 `rememberCouiTouchMotion` 与 `rememberCouiPressMotion`，业务点击仍由宿主自己的 `clickable` 处理：

~~~kotlin
@Composable
fun TouchAndPressSurface(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = rememberCouiTouchMotion(expanded = true)
    val press = rememberCouiPressMotion()
    Box(
        modifier
            .graphicsLayer {
                translationX = motion.translationX.value
                translationY = motion.translationY.value
                scaleX = motion.scaleX.value + press.scale.value - 1f
                scaleY = motion.scaleY.value + press.scale.value - 1f
            }
            .couiTouchMotion(motion, expanded = true)
            .couiPressMotion(press)
            .clickable(onClick = onClick),
        content = content,
    )
}
~~~

接入时注意以下四点：

1. **在绘制阶段读取 `.value`**：`CouiTouchMotion` 的四轴和 `CouiPressMotion.scale` 都是只读 `State<Float>`，请在 `graphicsLayer { ... }` 或绘制阶段读取 `.value`，避免触发无谓重组。
2. **形变与按压的缩放组合公式**：同一触摸组合内的形变缩放与按压缩放必须**增量相加**（`motion.scaleX.value + press.scale.value - 1f`），**不要相乘**；独立外层的展开/收起缩放则按父子变换自然相乘，自定义动画由宿主单独持有。
3. **主线程与 `CoroutineScope` 约束**：运动对象必须在**主线程**创建、调用和释放。显式构造 `CouiTouchMotion` / `CouiPressMotion` 时须传入主线程 `CoroutineScope`；取消该 `scope` 后弹簧停止且实例不再接收输入。Compose `remember*` 工厂会自动随节点移除释放资源，无需另建作用域。触点、位移及 `IntSize` 使用 **px**，缩放为倍率。
4. **`targetSize` 与 `cancelOnConsumedMove`**：`couiTouchMotion` 的 `targetSize: ((Offset) -> IntSize?)?` 用于按局部命中范围决定是否开启形变（返回 `null` 不启动），已按住的拖拽在出界后仍持续到 `UP` 或 `CANCEL`。`couiPressMotion` 的 `cancelOnConsumedMove = true` 用于滚动子项：父容器消费移动后立即释放，并在本次触摸结束前保持取消。

### 主动控制运动

手动驱动 `CouiTouchMotion` 与 `CouiPressMotion` 时，按各方法的明确职责调用：

| 类型 | 方法 | 行为说明 |
| --- | --- | --- |
| `CouiTouchMotion` | `begin(position, size)` | 开始触摸会话（`position` 为触点 **px**，`size` 为目标尺寸 **px**） |
| `CouiTouchMotion` | `switchPointer(previous, next)` | 多指切换活动指针时衔接平移与形变原点 |
| `CouiTouchMotion` | `move(position)` | 更新当前触点位置（**px**） |
| `CouiTouchMotion` | `release(velocity)` | 以输入速度（`Offset`，**px/s** 经内部权重处理）执行弹簧回弹 |
| `CouiTouchMotion` | `cancel()` | 停在当前位置，保留速度并清除尚未处理的目标；再次开始触摸时由新的输入确定目标（**不等于**恢复未变形状态） |
| `CouiTouchMotion` | `reset()` | 立即恢复未变形状态，清除旧动画、速度与触摸会话，并保留 `coordinates` 绑定以便后续开始新触摸 |
| `CouiPressMotion` | `press(size)` / `release()` | 按目标尺寸（**px**）启动按压缩放 / 回弹至 `1f` |

选择、光效、按压与滚动各自持有独立状态 owner，不能用某一路已取消推断其他路径都已收尾。

## 公开入口

- **双主体跟随**：[CouiMetaballFollowing.kt](../../coui/src/main/java/com/itos/coui/motion/CouiMetaballFollowing.kt)、[CouiSwipeRevealDefaults.kt](../../coui/src/main/java/com/itos/coui/swipereveal/CouiSwipeRevealDefaults.kt)
- **触摸与按压输入**：[CouiTouchInput.kt](../../coui/src/main/java/com/itos/coui/motion/CouiTouchInput.kt)、[CouiTouchMotion.kt](../../coui/src/main/java/com/itos/coui/motion/CouiTouchMotion.kt)、[CouiPressMotion.kt](../../coui/src/main/java/com/itos/coui/motion/CouiPressMotion.kt)
- **侧滑跟随装配示例**：[DemoRecordSwipeRow.kt](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSwipeRow.kt)
