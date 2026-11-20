# 普通阴影

`Modifier.couiShadow` 与 `CouiShadowRenderer` 为卡片、胶囊或椭圆形主体绘制普通 Mesh 投影。它不依赖 `CouiBackdrop` 背景源，也不包含半透明材质、背景模糊或 caustic 外光晕。

- **平台门槛**：要求 Android 14（**API 34**）及以上；API 35+ 使用公开 `Canvas.clipShader` 执行快速胶囊裁剪，API 34 保留内部 `RenderNode` / `RenderEffect` 承载分支。
- **公开入口**：
  - **便捷 Modifier**：`Modifier.couiShadow`（配合 `CouiShadowDefaults.menu(dark)`）。
  - **底层渲染器**：`CouiShadowRenderer`（配合 `CouiShadowRenderer.draw(...)`）。
  - **轮廓枚举**：`CouiShadowOutline`（`Oval`、`SmoothCapsule`、`RoundRect`）。

## 最小普通阴影

下面示例在 **API 34+** 的 Compose 内容中为一个圆角矩形绘制普通投影；宿主须先检查系统版本，并在外层通过 `padding` 为阴影保留绘制空间：

~~~kotlin
@RequiresApi(34)
@Composable
fun ShadowSample(
    modifier: Modifier = Modifier,
    dark: Boolean,
) {
    val radius = 24.dp
    Box(
        modifier
            .padding(32.dp)
            .size(160.dp, 72.dp)
            .couiShadow(
                style = CouiShadowDefaults.menu(dark),
                radius = radius,
            )
            .background(
                color = if (dark) Color.DarkGray else Color.White,
                shape = CouiShape(radius, cornerWeight = 2f),
            ),
    )
}
~~~

装配时注意以下两点：

1. **留足投影绘制空间**：父级与外层 `padding` 必须给阴影留出足够溢出区域，切勿在 `couiShadow` 外侧沿主体边界 `clip`。
2. **层级顺序**：阴影画在内容后方，主体自身的 `clip` 与 `background` 写在 `couiShadow` 内侧；`couiShadow` 本身不改变布局尺寸，父级变换作用于主体与投影整体。

## 入口与参数对比

| 对比项 | `Modifier.couiShadow`（便捷入口） | `CouiShadowRenderer.draw`（底层接口） |
| --- | --- | --- |
| **坐标系** | 节点本地坐标（`windowX`/`windowY` 固定为 `0f`，`sx`/`sy` 为 `1f`） | 支持传入实时窗口坐标（`windowX`/`windowY`）、缩放倍率（`sx`/`sy`）与透明度（`alpha`） |
| **长度单位** | `CouiShadowStyle` 与 `radius` 使用 **`Dp`**（`elevation`/`lightY`/`lightZ`/`lightRadius` 取整转 px，`radius` 保留小数；`radius = null` 时取短边的一半） | 所有长度与坐标（`width`、`height`、`windowX`、`windowY`、`elevation`、`lightY`、`lightZ`、`lightRadius`、`radius`）均使用 **px** |
| **多主体槽位** | 每节点单槽位（`slot = 0`） | 可通过 `CouiShadowRenderer(surfaceCount)` 分配多路槽位并用 `slot` 索引绘制 |
| **典型场景** | 本地卡片、胶囊或菜单常规投影 | 需要窗口坐标透视、独立节点 `alpha`、轴向缩放或多主体槽位的自定义绘制 |

### 轮廓选项与参数约束

- **`CouiShadowOutline` 分支**：
  - `Oval`：椭圆或正圆胶囊轮廓，支持 `clipping = true`（inverse clip）。
  - `SmoothCapsule`：平滑胶囊轮廓，支持 `clipping = true`；使用 `SmoothCapsule` 时，内容形状与投影几何须按同一主体装配，参见 [DemoShadowSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)。
  - `RoundRect`：普通圆角矩形轮廓；**不支持 inverse clip**，`clipping` 必须为 `false`（`couiShadow` 默认在 `outline == RoundRect` 时将 `clipping` 设为 `false`，显式传 `true` 会抛异常）。
- **`CouiShadowStyle` 合法范围**：`elevation >= 0.dp`、`lightRadius >= 0.dp`、`lightY` 为有限值，且 `lightZ` 必须为有限值并严格大于 `elevation`；`radius` 为 `null` 或有限非负 `Dp`。

## 自定义绘制与生命周期

- **节点资源归属**：同一个 `couiShadow` Modifier 可复用到多个节点，内部为每个节点分别创建并持有 `CouiShadowRenderer` 与两路缓存槽（ambient / spot）。节点尺寸变化时仅更新本节点网格，不重建 `Shader`。
- **直接持有 `CouiShadowRenderer`**：在自定义 `DrawScope` 中绘制时，每个独立节点应通过 `remember { runCatching { CouiShadowRenderer() } }` 持有自己的 `CouiShadowRenderer`。注意 `CouiShadowRenderer.draw` 生成的网格已经包含 `sx`/`sy` 缩放，**不要在 `Canvas` 上对已缩放的网格再次整体缩放**，也不要由外层重复相乘 `alpha`。
- **平台门槛与异常捕获**：调用前检查 `API 34+`；构造 `CouiShadowRenderer` 时即会编译 Shader，建议在 `remember { runCatching { ... } }` 工厂中捕获异常并由宿主提供降级或错误 UI，详见[资源与失败处理](backdrop.md#资源与失败处理)。父级裁剪与离屏透明度边界仍会影响最终可见阴影。

## 公开入口

- **便捷入口与预设**：[CouiShadow.kt](../../coui/src/main/java/com/itos/coui/effect/CouiShadow.kt)
- **底层渲染器与轮廓定义**：[CouiShadowRenderer.kt](../../coui/src/main/java/com/itos/coui/effect/CouiShadowRenderer.kt)
- **完整示例与运行分支**：[DemoShadowSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)、[兼容要求](../guide/compatibility.md)
- **原厂网格与移植方法**：[复刻指南](../development/replication-guide.md#32-底栏与-fab先分主体再分效果)
