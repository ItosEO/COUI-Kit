# 浮动表面

浮动表面接口用于在单一渲染链中组合**背景采样材质、普通 Mesh 投影、caustic 外光晕、轮廓内晕与前景内容**，并支持多主体按 Z 分组与互相融合。需要现成控件时，优先使用[顶栏按钮](../components/top-app-bar.md)、[底栏](../components/navigation-bar.md)、[FAB](../components/fab.md)与[搜索栏](../components/search-bar.md)。

- **平台门槛**：全部接口要求 Android 14（**API 34**）及以上，并需要覆盖**不透明 SDR 页面**的 `CouiBackdrop` 背景源。
- **公开入口**：
  - **渲染器**：`CouiFloatingRenderer`（`standalone()`、`pair()` 或默认三主体构造）。
  - **绘制 Modifier**：`Modifier.couiFloatingSurface`（1 主体）、`Modifier.couiFloatingPair`（2 主体）、`Modifier.couiFloatingSurfaces`（3 主体）。
  - **配置与几何数据**：`CouiFloatingMaterial`、`CouiFloatingShadow`、`CouiFloatingSurface`。

## 选择主体数量

| 主体数量 | `CouiFloatingRenderer` 工厂 | Modifier 入口 | 典型场景 | `material.index` 约束 |
| --- | --- | --- | --- | --- |
| 1（单主体） | `CouiFloatingRenderer.standalone()` | `Modifier.couiFloatingSurface` | 独立按钮或 `Fab` | 无额外限制（默认 `2`） |
| 2（双主体） | `CouiFloatingRenderer.pair()` | `Modifier.couiFloatingPair` | 两个相互融合的表面（如搜索框与取消按钮） | 列表长度必须为 2，且 `index` 依次为 `listOf(1, 2)` |
| 3（三主体） | `CouiFloatingRenderer()` | `Modifier.couiFloatingSurfaces` | 固定三个主体（如底栏菜单与两侧附件） | 列表长度必须为 3，排序后 `index` 必须为 `listOf(1, 2, 2)`（菜单 `1`，两侧附件 `2`） |

多主体装配规则：

- **禁止虚构空主体凑数**：单主体或双主体请使用 `standalone()` / `pair()`，不要凑三主体；各配置列表（`materials`、`shadows`、`shapes`、`contentLayers`）与 `surfaces()` 返回值的长度和顺序必须完全一致。
- **区分 `drawOrder` 与 `material.index`**：`couiFloatingPair` 的 `drawOrder`（默认 `listOf(0, 1)`）使用**列表下标 `0`、`1`**，仅调整同一 Z 分组内的绘制顺序（例如 `listOf(1, 0)` 先画第二项、后画第一项），不要与 `material.index`（`1`、`2`）混淆。

## 单主体装配

下面示例在 **API 34+** 的 Compose 内容中装配一个单主体浮动表面；`backdrop` 来自不包含当前节点的不透明 SDR 页面，`material`、`shadow`、`shape` 为宿主外观，`content` 为前景内容：

~~~kotlin
@RequiresApi(34)
@Composable
fun FloatingSurfaceSample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    material: CouiFloatingMaterial,
    shadow: CouiFloatingShadow,
    shape: Shape,
    contourGlowEnabled: Boolean,
    errorContent: @Composable (Throwable) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val renderer = rendering.getOrNull()
    if (renderer == null) {
        errorContent(checkNotNull(rendering.exceptionOrNull()))
        return
    }
    val contentLayer = rememberGraphicsLayer()
    var boundsSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier
            .onSizeChanged { boundsSize = it }
            .couiFloatingSurface(
                backdrop = backdrop,
                renderer = renderer,
                material = material,
                shadow = shadow,
                shape = shape,
                contentLayer = contentLayer,
                contourGlowEnabled = contourGlowEnabled,
            ) {
                CouiFloatingSurface(
                    bounds = Rect(
                        Offset.Zero,
                        Size(boundsSize.width.toFloat(), boundsSize.height.toFloat()),
                    ),
                )
            },
    ) {
        Box(
            Modifier
                .matchParentSize()
                .drawWithContent {
                    contentLayer.record { this@drawWithContent.drawContent() }
                },
            content = content,
        )
    }
}
~~~

装配时注意以下三点：

1. **宿主录制前景内容层**：内层通过 `drawWithContent { contentLayer.record { this@drawWithContent.drawContent() } }` 录制清晰前景并交给 `couiFloatingSurface` 按层序输出，不要把空的 `GraphicsLayer` 当作已录制内容。
2. **禁止跨节点复用**：传入的 `renderer`、`contentLayer` 与返回的 `Modifier` 共同绑定当前绘制节点，第二个节点须单独装配；不可变配置（如 `CouiFloatingMaterial`、`CouiFloatingShadow`）可以复用。
3. **禁止外层裁剪投影**：`couiFloatingSurface` 不改变布局测量，内容路径、投影溢出与材质 patch 按各自层序绘制，切勿在外层添加 `clip` 把投影与外光晕一并裁掉。

## 几何与绘制

### 输入参数、单位与坐标系

| 类型 / 字段 | 单位与坐标系 | 说明与约束 |
| --- | --- | --- |
| `CouiFloatingSurface.bounds` | 当前绘制节点的**未缩放 px** | 位移已计入 `bounds`（左上角），`scaleX`/`scaleY` 围绕 `bounds` 中心缩放 |
| `CouiFloatingSurface.scaleX` / `scaleY` | 倍率（默认 `1f`） | 轴向缩放倍率；宿主须让 `contentLayer` 录制尺寸与 `bounds` 对应同一帧 |
| `CouiFloatingSurface.cornerRadius` | 未缩放 **px**（默认 `null`） | 省略时取短边的一半；显式值须有限非负，绘制时限制到短边一半。当显式圆角小于短边一半时，投影自动切换为 `RoundRect` caster（沿用不透明主体、不做 inverse clip） |
| `CouiFloatingSurface.cornerWeight` | 无量纲权重（默认 `1f`） | 圆角矩形建议使用 `>= 2f`（例如 `2.5f`），并让传入的 `shape` 使用相同的半径与权重 |
| `CouiFloatingSurface.alpha` / `visible` | `0f..1f` / `Boolean` | 控制该主体的显示透明度与参与绘制状态 |
| `CouiFloatingShadow` | 长度均使用 **px** | `lightZ == 0f` 时保留基于设备显示尺寸计算的默认光源高度；`smoothCorner = true` 对应底栏 `SmoothCapsule` 投影轮廓 |
| `CouiFloatingMaterial` | 各字段以公开声明为准（`edgeWidthDp` 为 **dp**，`blurRadius`/`extension` 等为 **px**） | 固定预设应从所属组件 `Defaults` 获取（如 `CouiFabDefaults`、`CouiSearchBarDefaults`、`CouiNavigationBarDefaults`） |

### 合成模式与扩展绘制

- **`foregroundComposition` 模式**：单主体 `couiFloatingSurface` 与双主体 `couiFloatingPair` 支持 `foregroundComposition = true`。启用后，材质与光晕仅输出透明前景并以 `SrcOver` 叠到已绘制页面，避免矩形背景回填覆盖相邻兄弟节点；前提是 `backdrop` 必须覆盖**不透明 SDR 页面**（且不含当前所有浮动主体）。三主体 `couiFloatingSurfaces` 保留完整 `destination` 合成。
- **`beforeContent` 回调**：在对应主体的材质之后、前景内容之前直接混合到目标（双主体回调另接收列表下标 `Int`）。回调处于当前绘制节点本地坐标系，传入的缓存 `Path` 描述未缩放主体的局部轮廓；调用方负责位移、缩放和裁剪，**不得修改该 `Path`**。完整双主体装配见 [SearchBar.kt](../../coui/src/main/java/com/itos/coui/search/SearchBar.kt)，完整单主体装配见 [Fab.kt](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)。

## 资源与错误

- **状态更新与释放**：主题、轮廓、几何或位置变化后，更新当前节点传入的配置与 `surfaces()`；内部创建的 `shadowLayers`、`causticLayers`、`materialLayers` 与录制层随节点生命周期自动释放。
- **平台门槛与构造失败**：`CouiFloatingRenderer` 在构造时即编译原生 `RuntimeShader`，不会在绘制时吞没错误。调用前须检查 **API 34+**。上方示例在 `remember` 中通过 `runCatching` 捕获构造异常，创建失败时交由宿主提供的 `errorContent` 显示替代 UI；不要在构造失败后继续创建并使用浮动表面。详见[资源与失败处理](backdrop.md#资源与失败处理)。

## 公开入口

- **渲染器、配置与 Modifier**：[CouiFloatingSurfaces.kt](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt)
- **几何与分组计算**：[CouiFloatingGeometry.kt](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingGeometry.kt)
- **组件完整装配参考**：[Fab.kt](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)、[SearchBar.kt](../../coui/src/main/java/com/itos/coui/search/SearchBar.kt)
- **无背景采样的横向融合**：[横向双主体融合](metaball.md)
