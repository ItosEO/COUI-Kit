# 轮廓、跟手光与状态反馈

已有 COUI 组件均已内置光效与状态反馈。仅在自绘自定义表面时，才需要直接调用这里的独立轮廓、背景感知描边、触摸跟手光与交互蒙层接口。

- **平台门槛**：
  - `CouiContour`、`CouiEdge`、`rememberCouiSpotlight`：要求 Android 13（**API 33**）及以上。其中 `rememberCouiSpotlight` 在低于 API 33 时返回 `Modifier to null`（无等价旧平台 Shader 回调）；`CouiContour` 与 `CouiEdge` 须由宿主先检查版本。
  - 状态反馈 Modifier（`couiNavigationFeedback`、`couiFabFeedback`、`couiToolbarFeedback`、`couiButtonFeedback`）：支持库最低系统版本（**API 31+**）。

## 不透明表面的跟手光

下面示例在 **API 33+** 的 Compose 内容中为一个不透明按钮添加跟手光；绘制顺序为：**底色 -> 裁剪 -> 光效 -> 前景内容**，点击回调与文案由宿主传入：

~~~kotlin
@RequiresApi(33)
@Composable
fun LitSurface(
    modifier: Modifier = Modifier,
    color: Color,
    shape: Shape,
    dark: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val (lightInput, drawLight) = rememberCouiSpotlight(
        interactive = true,
        preset = SpotlightPreset.OpaqueMedium1,
        dark = dark,
    )
    Box(
        modifier
            .background(color, shape)
            .clip(shape)
            .then(lightInput)
            .drawBehind { drawLight?.invoke(this) }
            .clickable(
                interactionSource = interactions,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        content()
    }
}
~~~

> [!IMPORTANT]
> 这是针对**不透明表面**的组合方式。半透明浮动组件必须沿用[浮动表面](floating-surfaces.md)自身的合成位置；若先把 `Overlay` 光效混入空的透明图层再整体 `SrcOver`，会改变最终混合结果。

## 光效与状态反馈入口

| 入口 | 平台门槛 | 输入与行为 | 关键约束 |
| --- | --- | --- | --- |
| `CouiContour(floating, style)` | **API 33+** | 输入表面色、尺寸、`density`、像素半径、角权重与各透明度；绘制独立白光轮廓与内晕 | **不采样背景**；输出已包含 `surfaceColor`，不可再叠一次底色 |
| `CouiEdge(...)` | **API 33+** | 输入描边样式（`CouiEdgeStyle.Light`/`Dark`）、像素圆角、角权重、两次材质混色与开关；返回与尺寸匹配的 `RenderEffect` | 接收原始模糊图执行背景感知描边；`cornerWeight` 须 `>= 2f` 且与裁剪 `Shape` 一致 |
| `rememberCouiSpotlight(...)` | **API 33+**（旧平台返回 `Modifier to null`） | 返回 `(触摸 Modifier, 可空绘制回调)`，不接管点击或滚动 | 熄灭后回调为 `null`；绘制回调要求硬件加速 `Canvas` |
| `SpotlightPreset` | **API 33+** | 原厂跟手光预设（`TranslucentLarge1`、`OpaqueSmall1`、`OpaqueMedium1`、`OpaqueSmall2`、`OpaqueMedium2`、`ChipTab`） | 半径与光源高度使用**设备像素（px）**，不随 `density` 再次换算 |
| `couiButtonFeedback` | **API 31+** | 普通按钮或列表行（`list = true`）的 `hover`/`focus`/`press` 蒙层 | 宿主将同一 `MutableInteractionSource` 交给唯一 `clickable`/`toggleable` owner 并自行裁剪 |
| `couiNavigationFeedback` / `couiFabFeedback` / `couiToolbarFeedback` | **API 31+** | 导航项、FAB 与顶栏按钮的状态蒙层（按压形变由 `motion` 单独处理） | 配合同一 `MutableInteractionSource` 使用 |

### 跟手光参数与触摸会话规则

- **预设覆盖**：`rememberCouiSpotlight` 的可选参数（`intensity`、`ambient`、`diffuse`、`lightHeight`、`noise`、`dither`、`blendMode`、`startRadius`、`endRadius`、`color`）为 `null` 时继承 `preset`（按 `dark` 选择深浅色分支），非 `null` 时仅覆盖该项。
- **跨节点触摸映射（`inputTarget`）**：接收 `(本地触点 Offset, 是否已有光效会话 Boolean)`，返回目标 `LayoutCoordinates?`；返回 `null` 时立即取消当前会话。
- **父容器滚动取消（`cancelOnConsumedMove`）**：用于可滚动列表子项；父容器接管并消费移动后取消本次光效，且在所有手指抬起前**不会在同次触摸中重新点亮**。
- **开关与状态分离**：全局轮廓光开关使用 `LocalCouiContourGlowEnabled`（仅控制描边与内晕，不控制模糊、材质、跟手光或外投影），高级样式同步见[外观与预设](../guide/appearance.md)。`CANCEL`、点击、按压与绘制各自独立处理，不能把所有状态合并成一个 `running` 开关。

## 独立 Drawable 轮廓

### `CouiContour` 预设与参数组合

| 配置 | 行为与角权重约束 | 适用场景 |
| --- | --- | --- |
| `floating = false`, `style = CouiContourStyle.Menu`（默认） | 菜单轮廓与内晕，`cornerWeight >= 2f` | 菜单或圆角卡片 |
| `floating = false`, `style = CouiContourStyle.Dialog` / `Button` | 警告窗口 / 控件的轮廓预设，`cornerWeight >= 1f` | 自定义弹窗或按钮（支持胶囊权重 `1f`）；[弹窗](../components/dialog.md)已内置，无需单独配置 |
| `floating = false`, `style = CouiContourStyle.Panel` | 底部面板的独立内晕分布（使用同一 `Shader`），`cornerWeight >= 1f` | 底部面板轮廓 |
| `floating = true`（`style` 须保持默认 `Menu`） | 使用 FAB 加亮算法并保持表面 `alpha`，禁止与其他 `style` 组合 | 浮动操作按钮类独立表面 |

### 单位、叠色与资源生命周期

- **输入单位与取值范围**：`shader(size, density, radiusPx, cornerWeight, surfaceColor, innerGlowAlpha, lineAlpha, effectAlpha, lineWidthPx)` 中，`size`、`radiusPx`、`lineWidthPx`（默认 `2.5f * density`）均使用**节点本地 px**，`density` 须为正数；`innerGlowAlpha`、`lineAlpha` 与 `effectAlpha` 须在 `0f..1f` 范围内。
- **透明覆盖 vs 带底色输出**：仅需独立覆盖光效时，传入 `surfaceColor = Color.Transparent`，再覆盖到已有表面上；传入真实 `surfaceColor` 时，输出已包含该底色，**不得再重复叠加底色**。独立轮廓使用白光 `SrcOver`，需要后景颜色参与混合时应使用 `CouiEdge`。
- **节点资源归属**：每个独立绘制节点持有自己的 `CouiContour` 或 `CouiEdge`；尺寸、几何、`density` 或颜色改变时应调用 `shader(...)` / `renderEffect(size, density)` 更新参数，不复用旧尺寸或旧参数的 uniform 快照。外部裁剪 `Shape` 须与传入距离场的圆角和 `cornerWeight` 匹配。

## 公开入口

- **轮廓与描边**：[CouiContour.kt](../../coui/src/main/java/com/itos/coui/effect/CouiContour.kt)、[CouiEdge.kt](../../coui/src/main/java/com/itos/coui/effect/CouiEdge.kt)、[CouiEffectLocals.kt](../../coui/src/main/java/com/itos/coui/effect/CouiEffectLocals.kt)
- **跟手光与状态反馈**：[CouiSpotlight.kt](../../coui/src/main/java/com/itos/coui/effect/CouiSpotlight.kt)、[CouiStateFeedback.kt](../../coui/src/main/java/com/itos/coui/effect/CouiStateFeedback.kt)
- **组合绘制实例**：[TopAppBarButton.kt](../../coui/src/main/java/com/itos/coui/topappbar/TopAppBarButton.kt)
- **平台边界**：[兼容要求](../guide/compatibility.md)
