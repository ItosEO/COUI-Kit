# SegmentedControl 分段选择器

`SegmentedControl` 在胶囊容器内提供少量互斥选项（如“日 / 周 / 月”）切换，支持不等宽标签、独立回弹选中指示器以及从指示器发起的横向拖动切换。

- **系统版本要求**：要求 Android 14（**API 34+**），组件标注 `@RequiresApi(34)`。
- **前置依赖**：支持两种外观模式——放入 [Group](group.md) 等普通容器时可省略 `backdrop`（普通填色模式）；放在页面背景或图案上方时传入覆盖背景兄弟节点的 `backdrop`（透明模糊模式），且选择器自身不能位于采样源内部。

## 选择外观：普通填色 vs 透明模糊

| 放置位置 | 推荐用法 | 渲染与依赖 |
| --- | --- | --- |
| `Group` 内或纯色卡片内 | 省略 `backdrop`（默认 `null`） | 使用普通填色（俗称“不透明模式”，颜色仍按预设保留透明度），不采样背景，也不创建模糊材质与 `renderer` |
| 直接放在页面背景或图案之上 | 传入 `backdrop`（及可选 `renderer`） | 使用透明模糊材质，要求 `backdrop` 覆盖完整采样范围的不透明 SDR 背景且不含选择器自身 |

## 基本用法

### `Group` 内使用（普通填色）

省略 `backdrop` 直接放入 `Group` 内即可，`labels` 来自宿主字符串资源，并在 `onSelected` 中更新选中下标：

~~~kotlin
@RequiresApi(34)
@Composable
fun RangeInGroupExample(
    modifier: Modifier = Modifier,
    labels: List<String>,
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    if (labels.isEmpty()) return

    val safeSelected = selected.coerceIn(0, labels.lastIndex)
    LaunchedEffect(labels.size) {
        if (selected != safeSelected) selected = safeSelected
    }
    Group(modifier = modifier) {
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            labels = labels,
            selectedIndex = safeSelected,
            onSelected = { selected = it },
        )
    }
}
~~~

### 页面背景与透明模糊

在 `Box` 中用底层兄弟节点挂载 `.couiBackdrop(backdrop)` 作为背景源（须覆盖不透明 SDR 背景，不能留空），再同层放置 `SegmentedControl`；若需在 Shader / `renderer` 创建失败时展示降级界面，可在 `remember` 中用 `runCatching { CouiFloatingRenderer.standalone() }` 创建后传入：

~~~kotlin
@RequiresApi(34)
@Composable
fun ScheduleRangeExample(
    modifier: Modifier = Modifier,
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    dark: Boolean,
    backgroundColor: Color,
    errorContent: @Composable (Throwable) -> Unit,
) {
    val rendering = remember { runCatching { CouiFloatingRenderer.standalone() } }
    val renderer = rendering.getOrNull()
    if (renderer == null) {
        errorContent(checkNotNull(rendering.exceptionOrNull()))
        return
    }
    val backdrop = rememberCouiBackdrop()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .couiBackdrop(backdrop)
                .background(backgroundColor),
        )
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.Center),
            labels = labels,
            selectedIndex = selectedIndex,
            onSelected = onSelected,
            backdrop = backdrop,
            dark = dark,
            renderer = renderer,
        )
    }
}
~~~

- **标签校验**：`labels` 不能为空且 `selectedIndex` 必须处于 `labels.indices` 范围内，否则直接抛出 `IllegalArgumentException`。上方示例在列表为空时不渲染，在列表缩短后先以有效下标绘制，再同步修正保存的状态。实际页面动态增删、重排选项时，建议以稳定选项 ID 保存选择，再映射为下标，避免仅保留下标导致选错项。
- **尺寸与空间分配**：各分段按自然文字宽度分配（单段最小 `52.dp`、左右各 `12.dp` 内边距），整体在父级约束内自适应；传入 `Modifier.fillMaxWidth()` 时占满可用宽度，空间不足时单行省略。
- **高度预设**：默认高度由 `CouiSegmentedControlDefaults.height`（`40.dp`）提供，`small = true` 切换为紧凑高度 `CouiSegmentedControlDefaults.smallHeight`（`32.dp`）。外部尺寸 Modifier 可覆盖高度，指示器、采样与命中区域均使用实际约束尺寸。

## 提交模式：即时提交 vs 抬手提交（`commitOnRelease`）

按下当前选中指示器后可横向拖动切换分段，两种提交模式的行为差异如下：

| 模式 | 拖动过程 | 抬手与取消行为 |
| --- | --- | --- |
| `commitOnRelease = false`（默认） | 指示器滑入其他分段时**立即**调用 `onSelected`；普通点击在抬手时调用。 | 纵向滑动、父容器接管、失去主指、禁用或取消不会回滚先前已提交给宿主的索引。 |
| `commitOnRelease = true` | 仅更新指示器预览位置，不触发 `onSelected`。 | 在有效分段内抬手且目标改变时提交一次 `onSelected`；取消或被父容器接管时，预览恢复为宿主当前 `selectedIndex`。 |

无论使用哪种模式，选中索引始终由宿主持有，`onSelected` 中必须更新宿主状态。

## 参数速查

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符，可覆盖宽度与默认高度。 |
| `labels` | 必填 | 非空分段标题列表。 |
| `selectedIndex` | 必填 | 受控当前选中下标，须在 `labels.indices` 内。 |
| `onSelected` | 必填 | 分段选择提交回调。 |
| `backdrop` | `null` | 为 `null` 时使用普通填色模式；非空时启用透明模糊材质，须覆盖不透明 SDR 背景且严禁包含选择器自身。 |
| `dark` | `CouiTheme.isDark` | 明暗外观与指示器/字体对比度模式。 |
| `enabled` | `true` | 是否允许触摸、键盘与无障碍交互。 |
| `small` | `false` | 是否启用紧凑尺寸（`32.dp` 高 / `2.dp` 内缩，普通为 `40.dp` / `4.dp`）。 |
| `commitOnRelease` | `false` | 为 `true` 时拖动仅预览，仅在有效分段内抬手提交。 |
| `textStyle` | `CouiSegmentedControlDefaults.textStyle()` | 分段文字排版样式（默认使用 `14.sp` 中等字重预设）。 |
| `containerColor` | `CouiSegmentedControlDefaults.containerColor(dark)` | 普通填色（`backdrop == null`）时的胶囊容器底色。 |
| `indicatorColor` | `CouiSegmentedControlDefaults.indicatorColor(dark)` | 两种模式共用的选中指示器底色。 |
| `renderer` | 透明模式自动创建 / 普通模式为 `null` | `backdrop != null` 时须为非空单主体 `CouiFloatingRenderer.standalone()`，不可跨节点共享；可省略由组件自动创建，或自行 `runCatching` 创建后传入以处理失败界面。 |

## 交互职责与装配注意事项

- **禁用与无障碍**：`enabled = false` 时关闭触摸、键盘与无障碍点击（每个标签具有 `Role.Tab` 与 `selected` 语义），若在触摸过程中被禁用会自动收尾释放；键盘焦点支持 `Enter`、`Spacebar` 与方向键中心键激活。
- **RTL 支持**：RTL 布局自动镜像视觉排列与触摸命中方向，回调与 `selectedIndex` 仍保持传入 `labels` 的原始索引顺序。
- **材质与渲染器**：透明模糊模式需要独立的单主体 `CouiFloatingRenderer.standalone()`（节点移除时自动释放动画）；若在组件内部创建且运行环境初始化失败，异常会向上传播。背景源搭建与离屏细节见[背景采样](../effects/backdrop.md)。

## 源码与完整示例

- **公开入口与预设**：[SegmentedControl.kt](../../coui/src/main/java/com/itos/coui/segmentbutton/SegmentedControl.kt)、[CouiSegmentedControlDefaults.kt](../../coui/src/main/java/com/itos/coui/segmentbutton/CouiSegmentedControlDefaults.kt)
- **Demo 完整示例**：[DemoSegmentedControl.kt](../../demo/src/main/java/com/itos/coui/demo/DemoSegmentedControl.kt)
