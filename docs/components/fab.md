# FAB

`Fab` 提供带浮动材质、轮廓光与物理跟手反馈的悬浮操作按钮，可单独使用或与底栏、容器搭配。

- **最低系统版本**：要求 **API 34+**。
- **前置依赖**：需要不包含按钮自身的 `backdrop`（放入 COUI `Scaffold` 的 `floatingActionButton` 槽位时可省略）；每个 `Fab` 默认各自创建独立的 `CouiFloatingRenderer.standalone()`，如需在外部捕获 Shader 初始化失败也可显式传入 `renderer`。

## 最小调用

省略 `material` 与 `shadow` 时，按钮自动采用 COUI 主题强调色作为底色，并基于最终 `material.firstColor` 生成阴影；内容色在日间和夜间默认均为不透明白色（也可通过 `contentColor` 覆盖）：

~~~kotlin
@RequiresApi(34)
@Composable
fun BasicFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        content = content,
    )
}
~~~

接入说明：

- **必传 `onClick`**：`onClick` 为必传参数，同时注册点击与 `Role.Button` 语义；仅展示交互效果时可传入 `onClick = {}`。
- **拖拽与滚动隔离**：拖拽超过系统触摸阈值后，`Fab` 会接管指针移动事件，避免带动父级滚动容器；轻触仍正常触发 `onClick`。
- **强调色与材质**：`Fab` 不叠加页面 `themeTint`。强调色生成策略（如 HCT）由宿主持有，主题规则见[外观与预设](../guide/appearance.md)。

## 使用配色预设与自定义底色

### 使用内置配色预设

`CouiFabDefaults` 提供了 `Notes`、`Phone`、`InfraredRemote` 与 `Recorder` 四种内置配色，结合 `CouiFabDefaults.material` 与 `CouiFabDefaults.shadow` 装配：

~~~kotlin
@RequiresApi(34)
@Composable
fun PresetFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    preset: CouiFabColor,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val color = CouiFabDefaults.containerColor(preset, dark)
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current

    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        dark = dark,
        shape = CouiFabDefaults.circleShape,
        material = CouiFabDefaults.material(color, dark),
        shadow = CouiFabDefaults.shadow(color, density, contour),
        content = content,
    )
}
~~~

### 使用自定义底色

使用任意自定义颜色时，将同一个 `color` 分别传给 `CouiFabDefaults.material` 和 `CouiFabDefaults.shadow`，保持材质底色与有色投影/焦散光一致：

~~~kotlin
@RequiresApi(34)
@Composable
fun CustomColorFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    dark: Boolean,
    containerColor: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current.density
    val contour = LocalCouiContourGlowEnabled.current

    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        dark = dark,
        shape = CouiFabDefaults.circleShape,
        material = CouiFabDefaults.material(containerColor, dark),
        shadow = CouiFabDefaults.shadow(containerColor, density, contour),
        content = content,
    )
}
~~~

- **图标着色**：内容色在日间和夜间默认均为不透明白色（显式 `material` 下同样适用）。启用 M3 适配后，`Fab` 会向 M3 `Icon` 提供该内容色，图标可省略 `tint`；传入 `contentColor = Color.Unspecified` 时继承外部内容色，子图标显式 `tint` 始终优先（`Image` 不会自动读取内容色）。
- **轮廓光切换**：`LocalCouiContourGlowEnabled` 控制是否开启轮廓光；关闭后 `CouiFabDefaults.shadow` 自动切换为普通有色投影。

## 圆角方形与自定义形状

`Fab` 默认使用圆形 `CouiFabDefaults.circleShape`。需要圆角方形或自定义圆角尺寸时，传入 `CouiFabDefaults.roundedSquareShape` 或 `CouiShape(20.dp)`：

~~~kotlin
@RequiresApi(34)
@Composable
fun RoundedSquareFabExample(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Fab(
        modifier = modifier,
        backdrop = backdrop,
        onClick = onClick,
        shape = CouiFabDefaults.roundedSquareShape,
        content = content,
    )
}
~~~

- **`CouiShape` 与矩形扩展**：使用 `CouiShape` 时，其圆角半径（按实际短边的一半上限裁剪）与角权重会同步应用于内容裁剪、浮动材质与轮廓光；配合 `Modifier.size(width, height)` 可改为圆角矩形。普通投影会按同半径的圆角矩形遮罩生成（该路径按不透明主体绘制，若使用自定义半透明材质会透出内部阴影）。
- **其他 `Shape` 类型**：非 `CouiShape` 的普通 `Shape` 仅用于内容裁剪，浮动材质与外围轮廓仍采用圆形或胶囊几何。

## 参数与行为速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `backdrop` | `LocalCouiBackdrop.current` | COUI `Scaffold` 的 `floatingActionButton` 自动提供；第三方容器需显式传入不含 `Fab` 的正文源 |
| `renderer` | `remember { CouiFloatingRenderer.standalone() }` | 默认由组件各自创建单主体绘制器；需处理 Shader 初始化失败时可自行创建后传入，不可跨节点复用 |
| `onClick` | 必填 | 点击回调；仅展示效果可传入 `{}` |
| `dark` | `CouiTheme.isDark` | 控制默认强调色、按压反馈与高光分支 |
| `shape` | `CouiFabDefaults.circleShape` | 按钮形状；推荐使用 `CouiFabDefaults.circleShape`、`roundedSquareShape` 或自定义 `CouiShape` |
| `material` / `shadow` | `null` / `null` | `null` 时分别回退至主题强调色材质与基于最终 `material.firstColor` 的投影 |
| `foregroundComposition` | `false` | 相邻组合多个 FAB 时设为 `true`，使材质与外光晕仅以透明前景叠加 |
| `contentColor` | `CouiFabDefaults.contentColor` | 默认日夜均为不透明白色；传入 `Color.Unspecified` 继承外部内容色 |

## 装配注意事项

- **每个 FAB 独占一个 `renderer`**：默认情况下每个 `Fab` 各建一个独立单主体绘制器；显式创建 `CouiFloatingRenderer.standalone()` 传入时，也切勿在多个 `Fab` 或其他节点之间共享同一个实例。
- **多个相邻 FAB 启用 `foregroundComposition`**：在同一个容器内并排或竖向叠放多个 `Fab` 时，请将 `foregroundComposition = true` 传入每个按钮，防止拖拽形变时的矩形背景回填擦除相邻按钮。该模式要求 `backdrop` 覆盖不透明 SDR 页面且不包含任何 `Fab`，重叠光效边界详见[浮动表面](../effects/floating-surfaces.md#几何与绘制)。
- **Scaffold 与第三方容器定位**：放入 COUI `Scaffold` 的 `floatingActionButton` 槽位时，容器已应用正文安全区留白但未加外边距，使用 `Modifier.align(Alignment.BottomEnd).padding(24.dp)` 定位（详见[页面容器](scaffold.md#放置-fab)）。在第三方容器中请将 `Fab` 放在正文 `.couiBackdrop(backdrop)` 节点之外，并为外围投影与安全区预留充足空间。

## 源码与完整示例

- **公开入口与预设**：[Fab.kt](../../coui/src/main/java/com/itos/coui/floatingactionbutton/Fab.kt)、[CouiFabColor.kt](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabColor.kt)、[CouiFabDefaults.kt](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)
- **Demo 固定位置装配**：[DemoSettingsPage.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)、[DemoSecondaryPage.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)
