# 设置式折叠顶栏

CouiCollapsingTopAppBar 组合大标题和常驻搜索。滚动时标题按原分支缩放、淡出，搜索停靠顶部；进入搜索时 AppBar 折叠，标题与页面内容分别消费动画。它是局部组件，不拥有页面 Scaffold、导航、正文数据或搜索结果。

## 宿主装配

以下片段位于宿主 Composable 中，使用应用自行依赖的 Material 3 Scaffold；其他容器只需提供同等的顶栏实测 padding 和 nested-scroll 连接。dark、title、query、expanded、文案、rowTextStyle、更新回调、openDetail 和 results 均由宿主提供，results 是接收查询的 Composable 内容。代码不依赖 Demo。

```kotlin
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.search.CouiSearchBar
import com.itos.coui.search.rememberCouiSearchBarState
import com.itos.coui.topappbar.CouiCollapsingAppBarDefaults
import com.itos.coui.topappbar.CouiCollapsingTopAppBar
import com.itos.coui.topappbar.rememberCouiCollapsingAppBarState

val density = LocalDensity.current
val backdrop = rememberCouiBackdrop()
val scroll = rememberScrollState()
val header = rememberCouiCollapsingAppBarState(canScroll = { scroll.maxValue > 0 })
val search = rememberCouiSearchBarState(expanded)
val colors = CouiPageDefaults.themeColors(dark)
BackHandler(enabled = expanded) { updateExpanded(false) }
Scaffold(
    modifier = Modifier.nestedScroll(header.nestedScrollConnection),
    containerColor = colors.background,
    topBar = {
        CouiCollapsingTopAppBar(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            state = header, searchState = search, title = title,
            color = colors.title, backdrop = backdrop,
            backgroundColor = colors.background, dark = dark,
        ) {
            CouiSearchBar(state = search, query = query, onQueryChange = updateQuery,
                onExpandedChange = updateExpanded, placeholder = searchLabel,
                cancelText = cancelLabel, clearDescription = clearLabel,
                dark = dark, backdrop = backdrop, shadowProgress = header.collapsedFraction)
        }
    },
) { padding ->
    val contentPadding = remember(header, padding, density) {
        header.contentPadding(padding, density)
    }
    Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(colors.background)) {
        Column(Modifier.fillMaxSize()
            .verticalScroll(scroll, enabled = !search.active)
            .padding(contentPadding).consumeWindowInsets(padding)
            .graphicsLayer { alpha = search.contentAlpha }
            .then(if (search.active) Modifier.clearAndSetSemantics {} else Modifier)) {
            Group(colors = colors) {
                Item(onClick = openDetail, enabled = !search.active, dark = dark) {
                    BasicText(rowTitle, style = rowTextStyle)
                }
            }
        }
        if (search.active) {
            Box(Modifier.matchParentSize()
                .padding(contentPadding).consumeWindowInsets(padding)
                .graphicsLayer {
                    alpha = search.resultsAlpha
                    translationY = with(density) {
                        CouiCollapsingAppBarDefaults.resultsStartOffset.toPx()
                    } * (1f - search.resultsAlpha)
                }) {
                results(query)
            }
        }
    }
}
```

输入和返回规则见[常驻搜索](search-bar.md)。示例保留主内容组合与滚动状态，搜索期间明确关闭其滚动、行点击和无障碍子树；不靠 alpha 阻止交互。宿主增加其他控件后也要处理它们的输入与焦点，不能只给外层设透明度。

可用 LazyColumn 替换 Column：以 header.contentPadding 的返回值作为 contentPadding，并用宿主原始 padding 调用 consumeWindowInsets，不叠加第二层滚动。例中使用宿主的普通惯性；需要 COUI 纵向惯性及完整同帧触边交接时，将 verticalScroll 替换为 [couiVerticalScroll](../effects/overscroll.md)，不要同时保留两者。

顶栏内部消费剩余的顶部状态栏 inset；自身横向安全区由宿主设置。宿主容器的内容 padding 仍须应用和消费，bottomBar/FAB 也保留宿主原有的 inset 与避让规则。正文源包含自身背景和结果，不包含顶栏或它自己采样的浮层。完整采样分工见[宿主页面接入](../guide/secondary-pages.md)。

searchBar 是普通内容槽，可为空或产生多个根节点；它们在同一个 Box 内测量和叠放。需要排列时由槽内 Row/Column 指定。标题位置与缩放原点跟随布局方向的逻辑起始边。

## 滚动与恢复

state.nestedScrollConnection 必须连接实际内容列表的祖先。正向优先消费标题折叠范围，反向在列表到顶后展开；停止时按原 AppBar 吸附参数收敛。标题的 expansionFraction 取标题高度与最小工具栏高度之差，不等同于整个顶栏的 collapsedFraction。

canScroll 读取实际列表是否存在滚动范围；短内容停止滚动后恢复展开。LazyColumn 可用 canScrollForward / canScrollBackward 的并集。state.contentPadding 接受普通 PaddingValues 和 Density，只补偿正文顶部，保留左右及底部留白，不依赖特定 Scaffold 类型。不要用 contentTranslationY 平移整个 fillMaxSize 视口，否则会同时移动底部背景及采样边界；该像素属性只供明确的局部布局使用。

逻辑消费位置与可见弹簧位置独立。拖动时正文立即消费目标，搜索栏独立跟随；吸附时正文和顶栏各自保持动画，不按滞后的可见位置重复消费输入。进入搜索时记录展开/折叠状态并锁住普通滚动，退出后恢复；未完全折叠的中间位置按原规则恢复展开。

配置变化只保存稳定比例及搜索前状态，不保存像素或 renderer。同一 state 保留而 density 改变时取消旧像素动画，并同步到新范围的已接受逻辑目标。宿主始终回传顶栏实际测量的占位，不手写固定补偿高度。

标题、搜索宽度、取消按钮和结果不能合并成同一动画。顶栏背景复用现有渐进模糊，backgroundAlpha 只影响背景，通用顶栏与便签式搜索原有行为不变。

## 原厂参数与平台边界

参数依据 Settings 17 的 homepage_toolbar、SettingsCollapsingToolbarLayout 和 SettingsCollapsableAppBarLayout。可见整数位置按原规则取整，目标未变时不反复重定向弹簧。Compose 承担公开的测量与 nested-scroll 分发，不提供完整 OEM RecyclerView、双栏、特权窗口或字体协议。

当前触摸与惯性滚动共用方向跟随参数；候选原厂 scroll(type=1) 另读 followHeaderResponse/followHeaderBounce，但其父 Behavior 传参和提供者仍缺同源材料，不把该分支直接等同于 Compose 非触摸来源，也不宣称已验证等价。页面导航和转场由[宿主选择](../guide/secondary-pages.md)。

首页渐进背景下沿随页面状态变化：展开时没有额外高度，折叠完成或搜索编辑时使用原资源的扩展。它不改变搜索布局或触摸范围。backgroundColor 与 dark 控制原生幂曲线背景；固定引擎像素半径保留。通用 CouiTopAppBar 的固定长度/smoothstep 和按 density 配置的强度仍使用既有默认值，相关配置复用同一后端。

## 主题与显式配色

省略标题 color 时按实际 dark 从 CouiTheme 解析，显式色值优先。`dark` 只控制折叠顶栏自身的配色，不会传入 `searchBar` 内容槽，也不会改变 `CouiTheme`；需要同一深浅模式时，给槽内 `CouiSearchBar` 显式传入相同 `dark`，或用同一个局部 `CouiTheme` 包住顶栏和槽内容。标题保留显式颜色 alpha，图层透明度为标题折叠系数乘 `searchState.contentAlpha`；背景 alpha 不覆盖标题 alpha。常驻搜索使用 CouiSearchBarDefaults.colors，分组使用自己的显式 colors 或当前主题。不存在隐式页面配色作用域。搜索材质与低平台填色的边界见[搜索配色](search-bar.md#主题与显式配色)。

[组件](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingTopAppBar.kt)、[状态与 Defaults](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingAppBarState.kt)、[首页 Demo 装配](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。文档片段与 Demo 装配不等于已执行设备验收。
