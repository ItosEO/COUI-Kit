# 设置式折叠顶栏

CouiCollapsingTopAppBar 组合设置首页大标题和常驻搜索。滚动时标题按原分支缩放、淡出，搜索停靠顶部；进入搜索时 AppBar 折叠，标题与页面内容另做进入动画。

## 宿主装配

以下片段位于宿主 Composable 中。dark、accent、title、query、expanded、文案、rowTextStyle 及回调由宿主提供。页面内容示意使用普通列表，导航和搜索结果自行替换；不依赖 Demo。

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.settings.CouiPreferenceLayout
import com.itos.coui.settings.CouiSettingsItem
import com.itos.coui.settings.CouiSettingsDefaults
import com.itos.coui.settings.CouiSettingsGroup
import com.itos.coui.settings.CouiSettingsScaffold
import com.itos.coui.topappbar.CouiCollapsingTopAppBar
import com.itos.coui.topappbar.rememberCouiCollapsingAppBarState

val density = LocalDensity.current
val scroll = rememberScrollState()
val header = rememberCouiCollapsingAppBarState(canScroll = { scroll.maxValue > 0 })
val search = rememberCouiSearchBarState(expanded)
val colors = CouiSettingsDefaults.colors(accent, dark)
CouiSettingsScaffold(
    modifier = Modifier.nestedScroll(header.nestedScrollConnection),
    dark = dark,
    colors = colors,
    topBar = { backdrop ->
        CouiCollapsingTopAppBar(state = header, searchState = search,
            title = title, color = colors.title, backdrop = backdrop) {
            CouiSearchBar(state = search, query = query, onQueryChange = updateQuery,
                onExpandedChange = updateExpanded, placeholder = searchLabel,
                cancelText = cancelLabel, clearDescription = clearLabel,
                dark = dark, backdrop = backdrop, shadowProgress = header.collapsedFraction)
        }
    },
) { padding ->
    val contentPadding = remember(header, padding, density) { header.contentPadding(padding, density) }
    Column(Modifier.fillMaxSize()
        .verticalScroll(scroll, enabled = !search.active)
        .padding(contentPadding).consumeWindowInsets(padding)) {
        // 实际应用在搜索期间用结果层覆盖，并连接 contentAlpha / resultsAlpha。
        CouiSettingsGroup {
            CouiSettingsItem(onClick = openDetail) {
                CouiPreferenceLayout { BasicText(rowTitle, style = rowTextStyle) }
            }
        }
    }
}
```

搜索的受控状态、IME、取消与返回契约见[常驻搜索](search-bar.md)。可用 LazyColumn 替换 Column：将前文通过 header.contentPadding 计算的 contentPadding 传给列表，并用原始 padding 调用 consumeWindowInsets，不再套另一层滚动容器。顶栏使用消费感知的状态栏 padding；祖先已消费的部分不会重复添加，内容仍须应用并消费 Scaffold 的实测 padding。底栏/FAB 可继续装入 CouiSettingsScaffold 的对应 slot，业务不进入 coui。

searchBar 是普通内容槽，可为空或产生多个根节点；这些节点在同一个 Box 内测量和叠放。需要横向或纵向排列时，由槽内的 Row/Column 指定。标题的位置与缩放原点都跟随布局方向的逻辑起始边。

## 滚动与恢复

顶栏 state 的 nestedScrollConnection 必须连接实际内容列表。正向优先消耗标题折叠范围，反向在列表到顶后展开；停止时按原 AppBar 吸附参数收敛。标题的 expansionFraction 使用标题高度与最小工具栏高度之差，不等同于整个顶栏的 collapsedFraction。

canScroll 由宿主读取实际列表是否存在滚动范围；内容完全装得下时，停止滚动会恢复展开。LazyColumn 可使用 canScrollForward / canScrollBackward 的并集。内容使用 state.contentPadding(scaffoldPadding, density)，它只在顶部补偿顶栏实际位置与列表逻辑位置的差值，保留底部留白。不能用 contentTranslationY 平移整个 fillMaxSize 视口，否则底部背景和采样边界也会移动；该像素属性保留供局部内容布局使用。拖动时列表立即消费目标，搜索栏独立弹簧跟随；吸附时内容与顶栏各自保持动画。不要把两者重新绑在同一个可见高度。逻辑消费位置与实际弹簧位置独立：向上、向下跟随以及吸附分别使用原响应参数，不按滞后的可见位置重复消费输入。

进入搜索时记录展开/折叠状态并锁住普通滚动，退出恢复该状态；与原厂一致，未完全折叠的中间位置按展开状态恢复。配置变化保存稳定折叠比例及搜索前状态，不持久化像素坐标或旧 renderer。同一 state 保留而 density 改变时，取消旧像素动画，并把顶栏和内容同步到缩放后的已接受逻辑目标。状态栏 inset 由顶栏消费尚未处理的部分，内容留白由 Scaffold 测量后传入；不要自行使用一个固定高度补偿。

标题、搜索宽度、取消按钮和结果各自的动画不能替换成同一插值。顶栏背景复用现有渐进模糊，backgroundAlpha 只影响背景，不淡化标题或操作内容。默认顶栏和便签搜索的已有行为不变。

顶栏跟随保留独立逻辑目标与可见整数位置，按原厂坐标取整；目标不变时不反复重定向弹簧。配合 couiVerticalScroll 使用 COUI 正常惯性，列表与顶栏的跟随差不再混入默认 Compose decay。若改用其他滚动宿主，其惯性参数由宿主负责。

参数来源为 Settings 17 的实际 homepage_toolbar / SettingsCollapsingToolbarLayout / SettingsCollapsableAppBarLayout；公开 Compose nested scroll 不包括完整 OEM RecyclerView 边缘、双栏、privileged Window 和私有字体协议。当前触摸与惯性滚动共用方向跟随参数；候选原厂的 scroll(type=1) 分支另外读取 followHeaderResponse/followHeaderBounce，但父 Behavior 的参数传递与对应提供者尚缺完整同源材料，不能将该参数直接等同于 Compose 的非触摸来源，也未声明该分支已完成等价核验。默认 Activity 二级页继续使用[平台转场](../guide/secondary-pages.md)，本组件不接管导航。

[组件](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingTopAppBar.kt)、[状态与 Defaults](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingAppBarState.kt)、完整宿主（完整 Demo 在后续重构批次提供）。

设置首页的渐进背景下沿跟随实际页面状态：展开时没有额外高度，折叠完成或搜索编辑时采用首页资源的扩展。该处理只改变背景，不改变搜索布局或触摸范围；普通顶栏的默认扩展不受影响。

设置首页可显式传入 backgroundColor 与 dark，复用同一顶栏后端：全覆盖层高度、原生 speed=3 幂曲线、固定引擎像素半径及独立主题背景渐变。通用 CouiTopAppBar 的固定长度/smoothstep 和按 density 装配强度的适配默认保留；需要原厂半径场时传 gradientSpeed，需要背景渐变时传 gradientColor 与 dark。它们是同一个组件的配置，不是另一套组件或模糊算法。

## 主题与显式配色

默认值在消费处读取 `CouiTheme`，跟随原生或可选 M3 策略；显式颜色优先。省略标题 color 时按实际 dark 解析对应主题，显式色值继续优先；标题透明度仍由原厂折叠与搜索动画控制。设置容器使用 `CouiSettingsDefaults.themeColors`，常驻搜索使用 `CouiSearchBarDefaults.colors`。搜索的文本、图标与低平台背景覆盖范围见[常驻搜索配色](search-bar.md#主题与显式配色)。
