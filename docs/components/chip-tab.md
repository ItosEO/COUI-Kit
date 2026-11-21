# ChipTab 分类栏

`ChipTabRow` 用于横向排列单选分类胶囊，支持超宽惯性滚动、边缘渐隐与前置独立动作按钮（`ChipTabButton`）；也提供可单独使用的 `ChipTab`。

- **系统版本要求**：要求 Android 14（**API 34+**），公开组件均标注 `@RequiresApi(34)`。
- **前置依赖**：必须传入覆盖不透明页面内容的 `CouiBackdrop` 背景源，且分类栏自身必须位于采样源节点之外。

## 基本用法

构造稳定的 `CouiChipTabItem` 列表，由宿主保存 `selectedKey` 并在回调中更新：

~~~kotlin
@RequiresApi(34)
@Composable
fun CategoryChipTabExample(
    modifier: Modifier = Modifier,
    items: List<CouiChipTabItem>,
    selectedKey: Any?,
    onSelectedKeyChange: (Any) -> Unit,
    backdrop: CouiBackdrop,
) {
    ChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = onSelectedKeyChange,
        backdrop = backdrop,
    )
}
~~~

创建 `items` 时传入唯一且稳定的 `key` 与展示文案（如 `CouiChipTabItem("all", allLabel)`）：

1. **`key` 与选中合法性**：`items` 中每个 `key` 必须唯一；非空列表的 `selectedKey` 必须属于 `items`，空列表必须传 `null`，否则会抛出异常。
2. **受控单选行为**：重复点击已选项不会取消选中，也不重复触发 `onSelectedKeyChange`；点击、获得焦点或外部修改 `selectedKey` 时会自动平滑滚动使对应项目露出。
3. **有限宽度与背景源**：`ChipTabRow` 必须获得有限宽度约束；`backdrop` 应采样分类栏背后的兄弟节点或底层容器内容（配置方法见[浮动组件的背景源](../guide/getting-started.md#浮动组件的背景源)）。

## 添加前置独立动作（`leadingAction`）

通过 `leadingAction` 配合 `ChipTabButton` 可在分类列表前放置独立图标按钮（例如打开分类管理菜单）：

~~~kotlin
@RequiresApi(34)
@Composable
fun CategoryChipTabWithActionExample(
    modifier: Modifier = Modifier,
    items: List<CouiChipTabItem>,
    selectedKey: Any?,
    onSelectedKeyChange: (Any) -> Unit,
    backdrop: CouiBackdrop,
    categoriesVisible: Boolean,
    categoriesLabel: String,
    onOpenCategories: () -> Unit,
) {
    ChipTabRow(
        modifier = modifier.fillMaxWidth(),
        items = items,
        selectedKey = selectedKey,
        onSelectedKeyChange = onSelectedKeyChange,
        backdrop = backdrop,
        leadingAction = {
            ChipTabButton(
                onClick = onOpenCategories,
                contentDescription = categoriesLabel,
                backdrop = backdrop,
            ) {
                Icon(Icons.Default.Menu, contentDescription = null)
            }
        },
        leadingActionVisible = categoriesVisible,
    )
}
~~~

- **退场动画保持内容**：`leadingAction` 位于单选组之外。需要显隐动画时，请**保留 `leadingAction` lambda 内容，仅切换 `leadingActionVisible`**。
- **内容色继承**：`ChipTabButton` 会向下提供适配当前明暗模式与启用态的内容色，示例中的 Material 3 `Icon` 可直接继承（使用示例中的图标需宿主声明 Material 3 图标依赖；若传入图标自身显式设置了 `tint`，以显式颜色优先）。

## 参数速查

### `ChipTabRow` 与 `CouiChipTabItem`

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `modifier` | `Modifier` | 外层修饰符（要求父级提供有限宽度）。 |
| `items` | 必填 | `CouiChipTabItem(key, label, enabled, icon)` 列表；`key` 须唯一且稳定。 |
| `selectedKey` | 必填 | 当前选中的 `key`；非空列表须有效，空列表须为 `null`。 |
| `onSelectedKeyChange` | 必填 | 点击未选中分类项时的选择回调。 |
| `backdrop` | 必填 | 覆盖不透明背景内容的采样源，严禁包含分类栏自身。 |
| `dark` | `CouiTheme.isDark` | 明暗外观模式。 |
| `enabled` | `true` | 整栏启用状态（单项可由 `CouiChipTabItem.enabled` 单独禁用）。 |
| `leadingAction` | `null` | 前置独立动作槽位，通常放置 `ChipTabButton`。 |
| `leadingActionVisible` | `true` | 控制 `leadingAction` 的动画显隐并联动列表起始留白。 |
| `itemSpacing` | `8.dp` | 分类胶囊间距，必须为有限非负 `Dp`。 |
| `animationLevel` | `1` | 横向滚动物理动画等级，范围 `1..4`（`1` 最明显，`4` 最弱）。 |
| `contentColor` / `disabledContentColor` | `CouiChipTabDefaults.contentColor(...)` | 正常态和禁用态前景色。传入 `Color.Unspecified` 时继承当前内容色。禁用项统一取 `disabledContentColor`。 |
| `textStyle` | `CouiChipTabDefaults.themeTextStyle()` | 文字排版样式（可选 M3 适配仅接入 `labelLarge` 字体族，保留 `14.sp` 字号与 `Medium` 字重；显式传入则完整覆盖）。 |
| `contourGlowEnabled` | `LocalCouiContourGlowEnabled.current` | 是否启用边缘轮廓光效。 |
| `renderers` | 自动按 `items.key` 创建 | 各个 `ChipTab` 对应的 `CouiFloatingRenderer` 列表；显式传入时数量须与 `items` 一致且实例不可重复。 |
| `selectedColor` | `null` | 选中背景色。省略时使用中性玻璃材质。 |
| `selectedContentColor` | `contentColor` | 随选中进度过渡的前景色。`Color.Unspecified` 继承当前内容色，禁用项取 `disabledContentColor`，图标和文字的显式着色优先。 |

`ChipTab` 和 `ChipTabRow` 均可分别设置选中背景色与前景色。需要强调色时可搭配 `CouiTheme.colorScheme.accent` 和 `onAccent`，Material 3 宿主可使用 `primary` 和 `onPrimary`。

## 装配注意事项

- **文字超宽与边缘渐隐**：单个 `ChipTab` 胶囊高度固定为 `32.dp`、宽度限制在 `52.dp..300.dp`（文本最大 `200.dp`），超宽自动显示省略号。列表边缘渐隐在停靠边界外 `12.dp` 完成，停靠后的完整胶囊不会被淡化。
- **渲染器生命周期与异常兜底**：每个分类项与 `ChipTabButton` 各自持有单主体 `CouiFloatingRenderer`，不可跨节点共享。若需自定义渲染器创建失败时的降级界面，可在调用前用 `runCatching { CouiFloatingRenderer.standalone() }` 创建后传入 `renderers` / `renderer`（参见 Demo 示例）。
- **通用主题规则**：省略 `dark` 或显式颜色时，跟随当前 `CouiTheme` 或系统深浅色（详见[外观与预设](../guide/appearance.md)）。

## 源码与完整示例

- **公开入口与预设**：[ChipTabRow.kt](../../coui/src/main/java/com/itos/coui/chip/ChipTabRow.kt)、[ChipTab.kt](../../coui/src/main/java/com/itos/coui/chip/ChipTab.kt)、[CouiChipTabDefaults.kt](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabDefaults.kt)
- **Demo 完整示例（含分类菜单与错误兜底）**：[DemoChipSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt)
