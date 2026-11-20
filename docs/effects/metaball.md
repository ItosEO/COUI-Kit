# 横向双主体融合

当两个同色主体横向靠近时，`CouiMetaballRenderer` 绘制横向 CONIC 融合场；传入 `Color.White` 与 `BlendMode.DstIn` 时也可作为内容遮罩，用于宿主自己的列表行、按钮或其他形变区域。

- **平台门槛**：要求 Android 13（**API 33**）及以上（使用 `RuntimeShader`）；没有内置旧平台替代容器。
- **公开入口**：底层渲染器 `CouiMetaballRenderer` + 主体描述 `CouiMetaballBody`（常配合[运动与跟随回弹](motion.md)与预设 `CouiSwipeRevealDefaults`）。
- **适用边界**：算法只支持**同色横向双主体**，**不采样背景**；需要背景采样与固定三主体材质时，见[浮动表面](floating-surfaces.md)。

## 绘制两个主体

下面示例在 **API 33+** 的 Compose 内容中绘制两个同色融合主体；`first`、`second` 与 `color` 由宿主提供，`modifier` 须为 `Canvas` 提供有效尺寸：

~~~kotlin
@RequiresApi(33)
@Composable
fun MetaballPair(
    modifier: Modifier = Modifier,
    first: CouiMetaballBody,
    second: CouiMetaballBody,
    color: Color,
    fusionEnabled: Boolean = true,
) {
    val renderer = remember { CouiMetaballRenderer() }
    Canvas(modifier) {
        renderer.draw(
            scope = this,
            first = first,
            second = second,
            color = color,
            blendRangePx = CouiSwipeRevealDefaults.metaballBlendRange.roundToPx().toFloat(),
            fusionEnabled = fusionEnabled,
        )
    }
}
~~~

### 参数、单位与坐标系

| 参数 / 字段 | 单位与坐标系 | 说明与约束 |
| --- | --- | --- |
| `CouiMetaballBody.center` | 当前 `Canvas` 本地 **px**（`Offset`） | 主体几何中心坐标，须为有限值 |
| `CouiMetaballBody.size` | 当前 `Canvas` 本地 **px**（`Size`） | 须为有限非负值；**零尺寸表示收缩后的主体** |
| `CouiMetaballBody.cornerRadius` | 当前 `Canvas` 本地 **px**（`Float`） | 须为有限非负值；内部会自动限制在主体短边的一半以内 |
| `blendRangePx` | 当前 `Canvas` 本地 **px**（`Float`） | 横向融合作用范围，须有限非负（可使用 `CouiSwipeRevealDefaults.metaballBlendRange.roundToPx().toFloat()`） |
| `fusionEnabled` | `Boolean` | 是否启用融合变形；为 `false` 时不进行双主体融合 |
| `secondAlpha` | `0f..1f` | 单独控制第二主体的透明度（默认 `1f`） |
| `blendMode` | `BlendMode` | 默认 `BlendMode.SrcOver`；绘制内容遮罩时传入 `BlendMode.DstIn` |
| `size` | 当前 `Canvas` 本地 **px**（`Size`） | 输出区域尺寸，默认 `scope.size`；`first`、`second`、`blendRangePx` 与 `size` 须处于同一坐标系 |

## 内容遮罩

需要将融合边界同时裁剪到前景内容（避免移动中的文字或笔画露出融合背景外沿）时，按以下步骤装配：

1. **背景与遮罩各自持有 `renderer`**：背景绘制节点与内容遮罩节点使用相同的几何与融合参数，但**必须各自创建并持有独立的 `CouiMetaballRenderer`**，禁止跨节点共用。
2. **离屏隔离 + `DstIn` 裁剪**：内容层先通过 `Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` 启用离屏合成，先执行 `drawContent()` 绘制前景内容，再传入 `color = Color.White` 与 `blendMode = BlendMode.DstIn` 调用同一 `renderer.draw(...)`。
3. **坐标系映射与 RTL 对齐**：宿主负责把背景坐标系转换到内容层本地坐标系；当内容容器发生宽度收缩、RTL 镜像或右侧对齐时，不能直接沿用未转换的节点坐标，需先平移绘制原点后再调用 `draw(...)`。完整实现见 [DemoRecordMetaball.kt](../../demo/src/main/java/com/itos/coui/demo/DemoRecordMetaball.kt)。

## 跟随与交互

- **职责分离**：[运动与跟随回弹](motion.md)中的 `rememberCouiMetaballFollowingTranslation` 提供随位移跟随并在越界后释放回弹的位移值；滑动距离、布局、RTL、裁剪与手势均由宿主自行装配，库不提供侧滑容器、删除确认或业务提交。
- **固定预设**：融合半径、跟随距离与回弹弹簧规格直接读取 `CouiSwipeRevealDefaults`。
- **Demo 参考**：侧滑驱动效果见 Demo 的融合页（操作说明见 [Demo 指南](../guide/demo.md#主要示例)），宿主侧滑装配见 [DemoRecordSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt) 与 [DemoRecordSwipeRow.kt](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSwipeRow.kt)。

## 公开入口

- **渲染器与主体描述**：[CouiMetaballRenderer.kt](../../coui/src/main/java/com/itos/coui/effect/CouiMetaballRenderer.kt)
- **预设参数**：[CouiSwipeRevealDefaults.kt](../../coui/src/main/java/com/itos/coui/swipereveal/CouiSwipeRevealDefaults.kt)
- **平台检查与异常捕获**：构造前检查 **API 33+**，失败处理见[资源与失败处理](backdrop.md#资源与失败处理)。
