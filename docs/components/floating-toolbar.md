# 🧰悬浮操作栏

`FloatingToolbar` 把多个同级操作放进同一个胶囊玻璃表面，语义对齐 M3、Miuix 的 FloatingToolbar：容器承担材质、投影与轮廓，操作项只承担点击与反馈。

## 最小调用

操作栏需要 API 34 和页面背景源。`content` 是完整的 `Row` 插槽，操作项通过 `FloatingToolbarItem` 传入；下面在第三方容器中显式接收背景源：

~~~kotlin
@RequiresApi(34)
@Composable
fun PageFloatingToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    onEdit: () -> Unit,
    onAdd: () -> Unit,
) {
    FloatingToolbar(modifier = modifier, backdrop = backdrop) {
        FloatingToolbarItem(onClick = onEdit) {
            // 多色图标自带配色，显式 tint 优先于操作栏内容色。
            Icon(CouiIcons.Filled.AI, "AI 编辑", Modifier.size(CouiFloatingToolbarDefaults.iconSize),
                tint = Color.Unspecified)
        }
        FloatingToolbarItem(onClick = onAdd) {
            // 单色图标不传 tint，跟随操作栏内容色。
            Icon(CouiIcons.Filled.Add, "添加", Modifier.size(CouiFloatingToolbarDefaults.iconSize))
        }
    }
}
~~~

需要更多操作时按同样方式继续添加，或用 `forEach` 从一个列表生成，数量不受限制。

栏高 48dp，每个操作项的最小宽度也是 48dp；栏宽由内容决定，四项为 192dp，两项为 96dp，左右没有额外内边距。默认图标 24dp，因此操作项内左右各留 12dp，与系统胶囊的间距一致。位置、外边距与安全区由页面决定；父容器需要为投影留出空间。示例图标来自可选的 `coui-icons`，库本身不依赖该模块。

## 数量、宽度与顺序

操作数量不固定：`FloatingToolbar` 不为项目数预留槽位，`FloatingToolbarItem` 也不区分主次，传入多少个就排多少个。操作项按 `Row` 顺序排列，RTL 下顺序镜像。

操作项宽度默认等于最小值 48dp；需要更宽的操作项（例如图标加文字）时传入 `Modifier.width(...)`，悬停蒙层按单项短边取圆角，整条栏仍是胶囊。内容超出可用宽度时由宿主限制数量或自行装配，组件不内置横向滚动。

`content` 插槽不只接受 `FloatingToolbarItem`：需要分隔、标签或自定义控件时直接放入该 `Row`。只有 `FloatingToolbarItem` 会提供点击与反馈语义。

## 操作项

`FloatingToolbarItem` 是可点击的图标按钮：它持有点击、悬停、焦点、键盘焦点与无障碍 Button 语义，以及单项范围内的悬停、焦点蒙层。按压指示不落在单项上，见下节。

操作项**不各自绘制玻璃表面**。玻璃只由 `FloatingToolbar` 绘制一次，混色、模糊、轮廓光与投影都来自已有的[浮动表面](../effects/floating-surfaces.md)后端，新增代码只负责布局与装配。因此同一栏内不要再用 `Fab` 或 `TopAppBarButton` 组合操作项。

`enabled = false` 时移除点击与反馈，内容按 38% 不透明度淡化（整条栏的形变仍会发生，它属于表面级反馈）。操作项内容按中心对齐，尺寸由调用方决定，默认图标尺寸见 `CouiFloatingToolbarDefaults.iconSize`。

## 按压高光与形变

跟手光由整条胶囊持有：按下时高光出现在触点处、随手指在同一栏内移动，并绘制在图标之下，因此相邻操作不会各自亮起，颜色也不会被高光覆盖。装配方式与底栏的中间胶囊一致。

同一条胶囊还持有跟随手势的位移与形变：按下时轻微收缩，拖动时整体位移并沿拖动方向拉伸，抬手后回弹。拖动由操作栏接管，不会带着页面一起滚动；形成拖动时本次轻触不再触发点击，静止点按才会提交。

这两类反馈由 `FloatingToolbar` 统一提供，宿主不需要也不应该在操作项上再叠跟手光或形变。

## 图标配色

不传 `style` 时，内容色兜底为当前模式的 `onSurface` 压到 80%：没有自带颜色的图标不会因为操作栏浮在任意背景上而不可见，也不会被染成强调色，同时避免纯白、纯黑压在透明材质上过重。图标自带配色（多色图标，或自带深浅色资源的图标）时，按图标库的通用约定传 `tint = Color.Unspecified`，显式 tint 始终优先于内容色。

需要整栏统一着色时，传入带 `contentColor` 的样式；把 `contentColor` 设为 `Color.Unspecified` 则不提供内容色，退回环境色。子内容的显式颜色始终优先：

~~~kotlin
val style = CouiFloatingToolbarDefaults.style(dark).copy(contentColor = CouiColor.Day.BLUE)
FloatingToolbar(backdrop = backdrop, style = style) { /* 操作项 */ }
~~~

颜色不表达主次：多色图标与单色图标使用完全相同的按钮反馈，不会因为颜色获得独立表面或强调样式。

## 形状和材质

`dark` 默认跟随主题，`style` 默认由该值生成；需要样式与主题模式不一致时同时传入两者。`CouiFloatingToolbarDefaults.style(dark)` 直接复用顶栏胶囊的系统材质（`CAPSULE_1`）与胶囊投影，不叠加项目对顶栏按钮的加深特化；`shape` 为 `CouiCapsuleShape`。

`themeTint` 默认读取匹配模式的主题策略：原生为透明，M3 适配为宿主 surface，仅进入已有页面色相层。显式透明关闭染色。`lightColor` 用于跟手光高光，轮廓光由 `LocalCouiContourGlowEnabled` 控制。

默认样式在系统胶囊材质上补了 20px 背景模糊：`CAPSULE_1` 在顶栏里不带模糊，因为顶栏背景本身已经渐进模糊；操作栏单独浮在正文上，零半径会让正文清晰透出、读不出玻璃层次。模糊半径使用设备像素，由已有的浮动表面材质链执行，组件没有另建模糊实现。

需要不同的强度时直接改材质的 `blurRadius`；设为 `0f` 可关闭背景模糊：

~~~kotlin
val base = CouiFloatingToolbarDefaults.style(dark)
val style = base.copy(material = base.material.copy(blurRadius = 30f))
~~~

## 版本和采样

低于 API 34 时，可以显示项目自己的操作栏。

每个操作栏默认创建独立的单主体绘制器。宿主需要处理 Shader 初始化失败时，可以先用 `CouiFloatingRenderer.standalone()` 创建绘制器，成功后通过 `renderer` 传入。每个操作栏各用一个实例；同一页面出现两个操作栏时分别创建。

放入 COUI Scaffold 的顶栏、底栏或 FAB 槽位时可省略 `backdrop`；正文中的操作栏需要显式传入覆盖页面内容的背景源，创建方式见[背景采样](../effects/backdrop.md)。

源码见 [FloatingToolbar](../../coui/src/main/java/com/itos/coui/floatingtoolbar/FloatingToolbar.kt) 与 [Defaults](../../coui/src/main/java/com/itos/coui/floatingtoolbar/CouiFloatingToolbarDefaults.kt)。Demo 装配见 [DemoFloatingToolbar](../../demo/src/main/java/com/itos/coui/demo/DemoFloatingToolbar.kt)。
