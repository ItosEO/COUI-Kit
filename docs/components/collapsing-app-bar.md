# 折叠顶栏

`CouiCollapsingTopAppBar` 组合大标题与搜索槽。正文上滚时标题缩放、淡出，搜索停靠顶部；搜索展开时，顶栏折叠并协调标题与正文动画。

## 基本装配

以下片段位于宿主 Composable 中，使用应用自己的 Material 3 Scaffold。宿主持有 `dark`、`title`、`query`、`expanded`、文案、`rowTitle`、`rowTextStyle` 和更新回调；`openDetail` 执行导航，`results` 是接收查询和启用状态的 Composable 内容，并将启用状态传给其滚动与交互控件。Material 3 只属于这个宿主示例。

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
                .then(if (search.expanded) Modifier else Modifier.clearAndSetSemantics {})
                .graphicsLayer {
                    alpha = search.resultsAlpha
                    translationY = with(density) {
                        CouiCollapsingAppBarDefaults.resultsStartOffset.toPx()
                    } * (1f - search.resultsAlpha)
                }) {
                results(query, search.expanded)
            }
        }
    }
}
```

这里使用宿主的普通惯性。需要 COUI 惯性与同帧触边交接时，用 [couiVerticalScroll](../effects/overscroll.md) 替换 `verticalScroll`。使用 [CouiScaffold](scaffold.md) 时，横向安全区由容器处理，可省略顶栏的横向 `windowInsetsPadding`。

## 状态与正文布局

`state.nestedScrollConnection` 连接到实际列表的祖先：向上滚动先折叠标题，反向滚动在列表到顶后展开；停止时执行吸附。`canScroll` 读取实际列表是否存在滚动范围，短内容在滚动结束后恢复展开。LazyColumn 可读取 `canScrollForward || canScrollBackward`。

顶栏回传实际测量占位。`state.contentPadding(padding, density)` 只修正正文顶部，保留两侧和底部 Padding；将返回对象记住，供布局阶段读取。不要用 `contentTranslationY` 平移整个视口，否则背景和底部边界也会移动；该像素值只适用于宿主明确控制的局部布局。

LazyColumn 使用补偿后的 `contentPadding`，但 `consumeWindowInsets` 仍接收宿主原始 Padding。顶栏内部消费剩余状态栏 Insets，宿主处理横向安全区和底栏，具体分工见[宿主页面接入](../guide/secondary-pages.md)。

状态保存逻辑折叠比例和搜索前状态，不保存像素或 renderer。密度变化时，旧像素动画停止，位置换算到已接受目标的新范围。

## 搜索与输入

顶栏和 `CouiSearchBar` 使用同一个搜索 state。搜索展开前记录恢复位置：完全折叠的顶栏恢复折叠，中间位置恢复展开。搜索活动期间锁住普通顶栏滚动。

宿主用 `contentAlpha` 和 `resultsAlpha` 绘制正文与结果，并在 `search.active` 期间禁用正文交互、移除其无障碍子树。示例同时处理滚动和行点击；增加其他控件时，也要禁用其输入与焦点。结果内容以 `search.expanded` 控制交互，关闭时禁用输入并保留退场绘制。透明度不能代替这些处理。输入与返回规则见[常驻搜索](search-bar.md)。

`searchBar` 是普通内容槽。空内容或多个根节点由同一 Box 测量；需要排列时，在槽内使用 Row 或 Column。

## 外观与采样

`color` 默认按实际 `dark` 解析，显式颜色优先，其 alpha 与标题动画相乘。`dark` 只控制顶栏自身；需要搜索采用同一模式时，显式传入相同 `dark`，或将二者放入同一个局部 `CouiTheme`。

`backgroundColor` 提供背景渐变色。顶栏复用现有渐进模糊，使用 Settings 固定引擎像素半径；展开时不增加下沿高度，折叠完成或搜索活动时增加背景延伸。延伸不改变布局或命中高度。

Backdrop 覆盖正文与结果，不包含顶栏或其浮动消费者。通用顶栏的独立参数见[顶栏与按钮](top-app-bar.md#背景参数)。

## 实现依据

布局与状态依据 Settings 17 的 `homepage_toolbar`、`SettingsCollapsingToolbarLayout` 和 `SettingsCollapsableAppBarLayout`。逻辑消费、可见标题与正文吸附分别保存位置，以免用滞后动画位置重复消费输入。

候选原厂的 `scroll(type=1)` 另有跟随参数，但父 Behavior 的传参与资源来源尚未闭合；当前使用方向跟随路径，不能据此认定非触摸滚动完全等价。原包与候选包的区别及其他限制见[输入与共享效果记录](../../.agents/notes/implemented/bug-fix/2026-11-02-secondary-page-review-fixes.md)。

[组件](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingTopAppBar.kt) · [状态与 Defaults](../../coui/src/main/java/com/itos/coui/topappbar/CouiCollapsingAppBarState.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
