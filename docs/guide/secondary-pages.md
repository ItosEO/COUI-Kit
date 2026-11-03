# 在宿主页面中组合 COUI

COUI-Kit 是可按需取用的组件与视觉效果库，不提供独立 Scaffold，也不要求替换宿主主题、导航或页面结构。可以只放入一个 COUI 控件，也可以与 Material、Miuix 或自己的组件混用。

## 先决定需要哪一层

| 需要的能力 | 使用方式 |
| --- | --- |
| 一行内容或一个控件 | 直接放入现有布局，不安装页面容器或背景 Provider |
| 分组、说明、图示 | 组合 CouiGroup、CouiFooter、CouiIllustration |
| COUI 点击反馈 | 使用 CouiItem 或已有反馈 Modifier，保持唯一点击 owner |
| COUI 惯性与回弹 | 在实际滚动容器上显式使用 couiVerticalScroll |
| 模糊顶栏、底栏或 FAB | 由宿主录制正文 Backdrop，浮动组件位于该源之外 |
| 材质菜单或弹窗 | 按需安装既有窗口采样 Provider，不改变页面测量 |

独立行布局与配色见[分组与行布局](../components/settings.md)。默认值来自现有 CouiTheme，显式参数优先；混搭不要求把全局主题换成 COUI。可选 M3 适配与宿主字体规则见[外观与预设](appearance.md)。

## 使用已有 Scaffold 和模糊顶栏

下面是应用层 Material 3 Scaffold 的装配示例。应用需自行声明 Material 3 依赖并使用自己的 MaterialTheme；核心 coui 不依赖 Material 3。文案来自宿主资源，onOpen 执行业务导航。

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.layout.CouiGroup
import com.itos.coui.layout.CouiItem
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.CouiPreferenceLayout
import com.itos.coui.text.CouiText
import com.itos.coui.text.CouiTextKind
import com.itos.coui.topappbar.CouiTopAppBar

@Composable
fun MixedPage(
    modifier: Modifier = Modifier,
    title: String,
    rowTitle: String,
    onOpen: () -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    val background = MaterialTheme.colorScheme.background
    val titleColor = MaterialTheme.colorScheme.onBackground
    val colors = CouiPageDefaults.themeColors()
    Scaffold(
        modifier = modifier,
        containerColor = background,
        topBar = {
            CouiTopAppBar(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
                backdrop = backdrop,
            ) {
                Box(Modifier.fillMaxWidth().statusBarsPadding()
                    .height(CouiPageDefaults.toolbarHeight),
                    contentAlignment = Alignment.Center) {
                    BasicText(title, style = TextStyle(color = titleColor))
                }
            }
        },
    ) { padding ->
        // 录制整个正文视口及其底色，但不录制顶栏和其他浮动消费者。
        Box(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                item {
                    CouiGroup(colors = colors) {
                        CouiItem(onClick = onOpen) {
                            CouiPreferenceLayout {
                                CouiText(text = rowTitle, kind = CouiTextKind.PreferenceTitle,
                                    color = colors.title)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

这里只有需要材质的顶栏要求 Backdrop；仅使用 Group、Item 或普通文字时省略采样源即可。使用其他 Scaffold 或自有布局时保留其原有测量规则，按相同源与消费者关系装配，不复制此例的 Material 依赖到核心库。

内容应用并消费宿主传入的 padding。使用 LazyColumn 时将其传为 contentPadding；使用 Column 加 verticalScroll/couiVerticalScroll 时，padding 放在滚动内容侧，不能缩小整个采样视口。顶栏处理顶部及自身横向安全区；带底栏时由该底栏处理其底部安全区。不要对已消费部分重复添加系统 inset。Material 3 的规则见 [Android 官方说明](https://developer.android.com/develop/ui/compose/system/material-insets)。

折叠顶栏仍通过 state.contentPadding 修正正文顶部，保留底部视口，具体见[折叠顶栏](../components/collapsing-app-bar.md)。自定义宿主必须回传顶栏实测占位，不以固定高度代替；这是局部组件的布局契约，不要求库内 Scaffold。

## 按需提供窗口背景

正文源只包含正文。菜单或材质弹窗需要包含顶栏等已绘制内容的完整窗口源时，使用现有 Provider。以下是宿主 Composable 内的结构片段，注释处放入已有页面和浮层调用：

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.itos.coui.menu.ProvideCouiMenuBackdrop

ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        // 原有 Scaffold 或页面布局；其正文 Backdrop 保持独立。
        // 菜单/弹窗使用本 Provider 的窗口源，并按各自契约位于独立浮层中。
    }
}
```

默认入口保留其已有隔离规则。不要为了省去一次显式装配就引入新的页面 Provider，也不要默认给每页同时安装两份源。`rememberCouiWindowBackdrop()` 只读取最近已提供的窗口源；未提供时为 null，不会自行创建源。每个源只挂一个实际节点，每个绘制节点单独持有可变 renderer。详细规则见[背景采样](../effects/backdrop.md)、[菜单](../components/menu.md)和[弹窗](../components/dialog.md)。

## 状态、导航与返回

保留宿主已有的 Navigation、单 Activity 或多 Activity 方案。查询、搜索结果、返回栈、页面身份与受控设置值均由宿主持有，COUI 不注册业务路由或直接启动 Activity。

只有宿主选择使用当前 ROM 默认 Activity 转场时，才用普通、不透明的 ComponentActivity，并通过标准 Intent 启动、finish 退出；不传自定义 ActivityOptions，不覆盖窗口动画。这个方案得到第三方应用可用的系统默认转场，不保证系统设置的特权动画或双栏协议。单 Activity 应用继续使用自己的 Compose 导航与转场，不必为使用 COUI 组件重构页面。

搜索展开、编辑或未保存变更需要局部拦截返回时，由宿主安装处理器；状态关闭后释放拦截。弹窗自己的返回仍由弹窗处理。

切页时，持续显示的 dock、renderer 与反馈节点留在宿主的稳定作用域中，只对真正替换的页面内容设置 key。不要通过延迟业务回调掩盖错误的资源销毁时机。

Demo 使用普通 Activity 导航，页面由应用层 Material 3 Scaffold 装配。完整示例见 [MainActivity](../../demo/src/main/java/com/itos/coui/demo/MainActivity.kt)、[二级 Activity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt)和 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)；这些装配属于 Demo，不是库 API。

## 回弹与更多菜单

需要 COUI 正常惯性及同帧触边交接时使用 [couiVerticalScroll](../effects/overscroll.md)。短页/空页占满交互视口；标题和 dock 不随正文越界位移移动。每个容器独立 remember effect，传 null 可关闭边缘效果。不需要 COUI 物理的页面保留原滚动容器，不叠加两层滚动或两套回弹。

三点菜单用现有 CouiTopAppBarButton、按钮锚点和 CouiMenu 组合；列表菜单使用行布局与触点锚点。库不为具体业务再增加三点菜单或 Popup 行包装。示例和公开入口见[菜单](../components/menu.md)。

## 从 PR 临时容器迁移

移除 CouiScaffold 后，使用宿主已有容器，按上文显式接入所需采样能力。原来的 colors/dark 不再经隐藏页面作用域传给整页；使用既有 CouiTheme 局部覆盖，或分别传 Group.colors、Item.dark、Divider.color、Footer.color。Group 的内容槽保留其自己的主题。

COUI 的组件形状、排版、材质和交互具有独立职责。其他库可以作为研究参考，但其页面、导航和主题框架不是 COUI-Kit 必须复制的架构。
