# FloatingToolbar 悬浮操作栏

`FloatingToolbar` 把多个同级操作放进同一个胶囊玻璃表面，语义对齐 M3、Miuix 的 FloatingToolbar：容器承担材质、投影与轮廓，操作项只承担点击与反馈。组件位于 `com.itos.coui.floatingtoolbar`，操作数量与业务提交由宿主决定。

- **系统版本要求**：需要 **API 34+**，并需要所属页面的不透明 SDR 背景源（`backdrop`）。
- **核心特性**：胶囊材质、投影与轮廓由容器统一承担、操作项数量不限并按列数收放、按压共用同一反馈蒙版；不内置横向滚动。

## 基本用法

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

栏高 44dp，操作项 44×40dp，项间 4dp，两项以上时胶囊两侧各留 6dp；四项为 200dp，两项为 104dp，单项为 44dp。默认图标 24dp，因此图标到胶囊边缘共 16dp，与原厂便签一致。位置、外边距与安全区由页面决定；父容器需要为投影留出空间。示例图标来自可选的 `coui-icons`，库本身不依赖该模块。

## 数量、宽度与顺序

操作数量不固定：`FloatingToolbar` 不为项目数预留槽位，`FloatingToolbarItem` 也不区分主次，传入多少个就排多少个。操作项按 `Row` 顺序排列，RTL 下顺序镜像。

操作项宽度默认等于最小值 44dp；需要更宽的操作项（例如图标加文字）时传入 `Modifier.width(...)`。悬停、焦点与按压共用同一个蒙版矩形：多项时左右各外扩 4dp（一个项间距），单项时各内缩 2dp，圆角均按项高一半；蒙层仍被胶囊轮廓裁剪，整条栏始终是胶囊。内容超出可用宽度时由宿主限制数量或自行装配，组件不内置横向滚动。

`content` 插槽不只接受 `FloatingToolbarItem`：需要分隔、标签或自定义控件时直接放入该 `Row`。只有 `FloatingToolbarItem` 会提供点击与反馈语义。

## 列数变化

进出场按原厂 `ItemViewAnimatorImpl` 的两段节奏，整条栏共用一条进度，不另设宽度动画：

1. 0–200ms 线性调整项间距：删除时把 4dp 撑到 1.75 倍，插入时压到 0.25 倍。图标保持原状，胶囊宽度只随间距变化。
2. 200ms 起，项弹簧（response 0.5s、回弹 0.4，与原厂菜单项弹簧一致）推进图标缩放与透明度，间距同时在另一个 200ms 内线性回到 4dp。进入项 0→1、退出项 1→0、保留项恒为 1；弹簧允许越过 1，收尾带一次回弹。弹簧与间距回程都结束后才回到静止态。

弹簧越过 1 时两个方向都会过冲：进入项的缩放直接让槽位涨过头；退出项的视觉缩放钳在 0，原厂 `handleItemState` 把未钳制的负进度交给 `updateAllAnimatingViews`，按 `GAP_ANIMATION_MULTIPLIER = 5` 转成负的间距推进量，胶囊因此先压过头再弹回。缺这一步时收缩会「收到终点就停住」，只有扩展方向有弹性。

每一帧的槽位是 `缩放·项宽` 加两侧各 `缩放·有效间距/2`，首尾项不留外侧半间距；胶囊宽度就是这一帧全部槽位之和加两端内边距。这等价于原厂从首尾图标的 x/scale 反算 `mBackgroundBounds`：原厂收到新项列表后立刻按最终可见项完成 measure/layout，动画期间只重排各图标的位置与缩放，可见胶囊边界由首尾图标反算，因此不存在「宽度单独补间」或收尾跳变。宿主不需要自行计算宽度或再包一层尺寸动画。

单个操作项的进出场内置在 `FloatingToolbarItem`：把 `visible` 置 false 后，槽位、透明度和缩放按同一条弹簧一起收到 0，图标随胶囊向中间收且不会与相邻项重叠；重新置 true 反向淡入。宿主保留操作项、只切换 `visible` 即可，不要用 `if` 直接移除项，否则没有退出动画。同一帧多个操作项一起切换时按一批处理，共用这一条进度。

隐藏项的缩放为 0，槽位连同它两侧的半间距一起归零：中间项隐藏后，两侧只剩一个 4dp 间距，不会留下两道空档。

## 操作项

`FloatingToolbarItem` 是可点击的图标按钮：它持有点击、悬停、焦点、键盘焦点与无障碍 Button 语义，以及单项范围内的悬停、焦点与按压蒙层。整条栏的跟手光与形变另由胶囊提供，见下文。

操作项**不各自绘制玻璃表面**。玻璃只由 `FloatingToolbar` 绘制一次，混色、模糊、轮廓光与投影都来自已有的[浮动表面](../effects/floating-surfaces.md)后端，新增代码只负责布局与装配。因此同一栏内不要再用 `Fab` 或 `TopAppBarButton` 组合操作项。

`enabled = false` 时移除点击与反馈，内容按 38% 不透明度淡化（整条栏的形变仍会发生，它属于表面级反馈）。操作项内容按中心对齐，尺寸由调用方决定，默认图标尺寸见 `CouiFloatingToolbarDefaults.iconSize`。

## 按压高光与形变

跟手光由整条胶囊持有：按下时高光出现在触点处、随手指在同一栏内移动，并绘制在图标之下，因此相邻操作不会各自亮起，颜色也不会被高光覆盖。装配方式与底栏的中间胶囊一致。

同一条胶囊还持有跟随手势的位移与形变：按下时轻微收缩，拖动时整体位移并沿拖动方向拉伸，抬手后回弹。拖动由操作栏接管，不会带着页面一起滚动；形成拖动时本次轻触不再触发点击，静止点按才会提交。

操作项自己保留按压蒙层（原厂对应 `COUIMaskRippleDrawable`）：它只在这个单项的蒙版范围内出现，不改变胶囊形状。整条栏的跟手光、收缩与形变由 `FloatingToolbar` 统一提供，宿主不需要也不应该在操作项上再叠。

## 与原厂实现的对应

参数取自系统便签 17.0.30 的 `com.coui.appcompat.bottomfloatingtoolbar` 与 `animation.touchanimation`，本组件的触摸层与灯效层按同一组数值实现，差别只在 Compose 侧的装配方式：

| 环节 | 原厂数值 | 本组件 |
| --- | --- | --- |
| 拖动判定 | 死区 5px，两轴都小于才判为未拖动 | 同 |
| 跟手位移 | 橡皮筋：`D·(1−1/(1+c·|dx|/D))`，`D=28dp`、`c=0.05`，跟随弹簧 response 0.15 / bounce 0.15 | 同 |
| 拖动形变 | `150dp`、曲线 0.55，最大缩放 1.2，按面积与宽高比衰减到 1.03 | 同 |
| 抬手回弹 | response 0.42（小面积）→0.45（大面积），bounce `max(0.65, |v|/1000)`，初速 `v/28dp` 限 ±1000 | 同 |
| 按压缩放 | response 0.25 / bounce 0；释放 response 0.5 / bounce 0.6；缩放 0.85–0.97，按按压面积与宽高比取 | 同 |
| 跟手光 | 白色、半径 100→500px，预设 `TYPE_OPAQUE_MEDIUM_2`（亮 1.0 / 暗 0.15），跟手 0.35 / 0.15，点亮 0.15 / 0，熄灭 0.4 | 同 |
| 项进出场 | 项弹簧 response 0.5 / bounce 0.4；先 200ms 线性调整间距（删除 1.75 倍、插入 0.25 倍），再与弹簧并行回程 | 同 |
| 胶囊边界 | measure/layout 按最终可见项定宽；`mBackgroundBounds` 由首尾项的 x/scale 反算，无独立宽度动画 | 同一帧各槽位求和，读同一组 gap / 进度 / 角色 |
| 单项反馈蒙版 | `COUIMaskRippleDrawable`：多项时左右外扩 4dp、单项时内缩 2dp，圆角按项高一半 | 同一矩形，hover / focus / press 共用 |
| 栏出现/收起 | 缩放 0↔1，出现 response 0.5 / bounce 0.4，收起 0.25 / 0 | 由宿主的出现与移除决定，组件不自带 |
| 布局 | 高 44dp、项 44×40dp、项间 4dp、图标 24dp、两项以上两端 6dp | 同 |

原厂操作项就是 44×40dp，低于 48dp 的最小点击区指引；本组件按参考复刻，没有再叠 `minimumInteractiveComponentSize`，否则每项会被撑到 48dp、胶囊比参考宽 4dp。需要更大点击区时由宿主给操作项加 `Modifier.height/width`。栏本身的出现与收起由宿主控制，组件不内置缩放入场。

材质与投影不在动画范围内：默认样式复用顶栏胶囊材质并补 20px 背景模糊，原厂兼容路径是 30px 模糊加两层混色，系统材质路径是 25px。

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

## 源码与完整示例

源码见 [FloatingToolbar](../../coui/src/main/java/com/itos/coui/floatingtoolbar/FloatingToolbar.kt) 与 [Defaults](../../coui/src/main/java/com/itos/coui/floatingtoolbar/CouiFloatingToolbarDefaults.kt)。Demo 装配见 [DemoFloatingToolbar](../../demo/src/main/java/com/itos/coui/demo/DemoFloatingToolbar.kt)。
