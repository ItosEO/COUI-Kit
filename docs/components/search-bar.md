# 常驻搜索与分裂取消胶囊

CouiSearchBar 对应 SettingsSearchBar 的 instant-search 分支：普通状态是一条常驻搜索胶囊，进入后收窄输入背景，分裂出独立取消胶囊。它与[便签式顶栏搜索](search.md)是不同组件；麦克风属于宿主功能，本组件不装配或保留其占位。

## 基本用法

以下片段位于宿主 Composable 内。expanded、query 和对应更新函数由宿主持有；backdrop 是页面内容源，不能包含当前搜索。label、cancel、clearLabel 来自宿主资源，dark 为宿主主题。

```kotlin
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState

val search = rememberCouiSearchBarState(expanded)
CouiSearchBar(
    state = search,
    query = query,
    onQueryChange = updateQuery,
    onExpandedChange = updateExpanded,
    placeholder = label,
    cancelText = cancel,
    clearDescription = clearLabel,
    dark = dark,
    backdrop = backdrop,
    onSearch = submitSearch,
)
```

state 保持稳定；不要用 expanded 作为 remember 的重建 key。背景宽度、取消胶囊、页面和结果分别持有弹簧通道，运行中反向保留当前速度。宽度与取消的原厂目标重定向路径已确认；页面与结果保留当前共享弹簧适配。候选 HomepageImpl 会先调用 setStartValue，但所用 OEM 动画类的运行中 setter 语义及默认阈值尚缺实现材料，页面与结果的快速反向不能据此声称原厂等价。

查询、结果筛选、展开目标和导航始终归宿主。输入变化与 IME 提交不能直接调用取消或导航；结果点击才执行导航。Demo 同时匹配本地标题、组件 ID 与介绍。清空只提交空查询；取消释放搜索自身的焦点及 IME，并请求 onExpandedChange(false)，是否同时清空查询由宿主决定。初始收起及搜索已经失焦时，组件不清理其他输入框的焦点或键盘。宿主拒绝关闭回调时保留展开和查询，搜索不会立即抢回焦点；用户仍可点击输入框继续编辑。IME 搜索先隐藏键盘，再调用 onSearch。

在 CouiTopAppBar 内，搜索采样其作用域提供的渐进背景，包含随滚动显现的背景处理，但排除标题和搜索自身；独立使用时采样显式 backdrop。搜索材质仍使用所属预设，不能用整块原始页面回填覆盖顶栏背景。设置式整页结果可按 CouiCollapsingAppBarDefaults.resultsStartOffset 及 resultsAlpha 装配原厂进入位移，不能将结果退场的响应时间与标题淡入合并。

普通状态的整条搜索承担入口语义，输入在展开时才可编辑。取消和清空分别是独立操作节点。展开后使用 Activity Compose 的 BackHandler 或宿主返回分发器关闭搜索；没有展开时释放拦截，保留 Activity/系统返回。输入由 query / onQueryChange 直接受控；输入变化只发布查询，清除图标的出现和重绘不改变展开目标。弹簧在创建 rememberCouiSearchBarState 的组合退出时释放；仅隐藏搜索节点而保留 state 时，各通道继续供标题和结果消费。输入、图层和局部触摸效果随各自搜索节点释放，不会对宿主整个焦点树调用清理。查询与展开可用 rememberSaveable 恢复，动画中间值不持久化。

## 折叠标题与页面装配

设置首页使用[折叠顶栏](collapsing-app-bar.md)组合它。将同一个 search state 传给 CouiCollapsingTopAppBar 和 CouiSearchBar。宿主主内容可使用 search.contentAlpha，结果使用 search.resultsAlpha；结果内容、空态和业务路径由宿主提供。进入搜索时主内容应停止输入，并由结果层覆盖，不能只设置 alpha 留下不可见点击区。

形状使用项目平滑胶囊，材质、轮廓、投影及双主体融合复用[浮动表面](../effects/floating-surfaces.md)。按本项目的模糊要求选择原厂 TYPE_FRAMEWORK_TOP_BAR_BLUR，保留顶栏混色和 CAPSULE_1 描边；该预设有真实背景模糊，与原厂默认 TYPE_FRAMEWORK_TOP_BAR 的零半径分支有区别。不能把后者的材质混色称为模糊。shadowProgress 可传入折叠顶栏的 collapsedFraction；不传时普通搜索没有滚动投影，进入搜索按原曲线显现投影。每个搜索实例持有独立 renderer 和内容层。搜索与取消均保留原厂 extension，保证融合区域进入材质绘制范围。固定输入边界复用旧搜索的 BringIntoView 处理，限制祖先重定位，输入内部光标滚动仍保留。

材质索引保持搜索 1、取消 2；同一高度的绘制顺序则是取消在下、搜索在上，与原厂两次 bringChildToFront 顺序一致。收合时，搜索的整层材质遮盖取消背景和内容。取消文字保留随进度变化的透明度及原厂内容模糊，不把整个搜索文字一起模糊。双主体通过公共浮动后端只输出前景，以 SrcOver 叠到已有顶栏；材质外沿和外光晕不再回填整块 destination，避免擦掉渐进背景和投影。

API 34 起启用项目材质与双主体融合；较低平台保留明确的普通填色、输入、宽度/取消动画与反馈路径，不声称等同于 GPU 融球。Shader 创建错误向上传播，不吞掉构造失败。原厂字体与主题覆盖取决于宿主设备，公开 Compose 的焦点和 IME 承载仍需设备对照。

[公开组件](../../coui/src/main/java/com/itos/coui/search/CouiSearchBar.kt)、[状态](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarState.kt)、[Defaults](../../coui/src/main/java/com/itos/coui/search/CouiSearchBarDefaults.kt)、完整 Demo 装配（完整 Demo 在后续重构批次提供）。

搜索与取消都接入项目 OpaqueMedium2 跟手光，并在实际材质后、内容前绘制。光源随各自局部坐标和形变映射；输入观察不消费编辑或取消操作。API 33 普通填色分支也绘制支持的跟手光；低版本仍按原平台边界处理。

搜索主体与取消胶囊各自持有随节点释放的 Touch/Press 通道。同一触摸的形变与按压按缩放增量相加，取消胶囊独立的展开缩放再与其相乘；材质、光效、内容、触摸命中与无障碍边界消费同一组几何。两条平台绘制路径都保留位移和缩放；输入节点同样将搜索放在取消上方，展开中途不会让下层取消截获搜索范围内的点击。

## 主题与显式配色

`CouiSearchBarDefaults.colors` 在消费处读取 `CouiTheme`，跟随原生或可选 M3 策略。`foreground`、`secondary`、`clear` 控制文字、图标和清除操作，显式值优先。`fallbackBackground` 仅用于 API 31–33 的普通填色分支；API 34 起保留所属的原厂模糊材质、混色、轮廓与融合，不使用该填色覆盖材质。可通过 `copy` 覆盖需要的颜色角色。
