# 在宿主页面中组合 COUI

COUI 组件可以放入现有 Material、Miuix 或自有页面。纯 COUI 应用也可选择 [Scaffold](../components/scaffold.md) 处理栏位测量和安全区。

| 页面需要 | 装配方式 |
| --- | --- |
| 普通分组与控件 | 直接放入已有布局，见[分组与行布局](../components/settings.md) |
| COUI 惯性与回弹 | 在实际滚动容器使用 [couiVerticalScroll](../effects/overscroll.md) |
| 模糊顶栏、底栏或 FAB | 录制正文 Backdrop，将消费者放在源之外 |
| 材质菜单或弹窗 | 按需提供窗口背景源 |

配色来自组件参数或当前 `CouiTheme`。M3 适配是可选依赖，接入方法见[外观与预设](appearance.md)。

## 使用已有 Scaffold

下面的应用层示例使用 Material 3 Scaffold。宿主需自行依赖 Material 3 并提供 MaterialTheme；文案和导航回调由宿主传入。

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
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.text.Text
import com.itos.coui.text.CouiTextKind
import com.itos.coui.topappbar.TopAppBar

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
            TopAppBar(
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
                    Group(colors = colors) {
                        Item(onClick = onOpen) {
                            Text(text = rowTitle, kind = CouiTextKind.PreferenceTitle,
                                color = colors.title)
                        }
                    }
                }
            }
        }
    }
}
```

仅使用 Group、Item 或普通控件时，不需要示例中的 Backdrop。使用其他页面容器时，保留相同的源与消费者关系。

## Padding、Insets 与滚动

正文应用并消费宿主给出的 Padding。LazyColumn 使用 `contentPadding`；Column 使用 `verticalScroll` 或 `couiVerticalScroll` 时，将 Padding 放在滚动 Modifier 后，使内容能滚入浮动栏位下方。不要叠加两层纵向滚动。

顶栏负责顶部及自身横向安全区，底栏负责底部安全区；宿主容器返回的占位包含这些实测高度。已消费的部分不再添加第二次。Material 3 的分工见 [Android 官方说明](https://developer.android.com/develop/ui/compose/system/material-insets)，Scaffold 的分工见[页面容器](../components/scaffold.md#padding-与系统-insets)。

含输入框的页面在滚动容器外使用 `imePadding()`，使键盘打开时实际视口缩小，Foundation 才能处理焦点避让。只增加滚动内容尾部空白不会缩小视口。父级已处理 IME 时沿用其分工，详见 [Insets 消费规则](https://developer.android.com/develop/ui/compose/system/insets-ui)。

折叠顶栏还需用 `state.contentPadding` 补偿正文顶部，并用宿主原始 Padding 消费 Insets，见[折叠顶栏](../components/collapsing-app-bar.md)。该状态接收普通 PaddingValues，不依赖特定 Scaffold。

## 正文源与窗口源

正文源覆盖完整视口和不透明底色，供顶栏、底栏等浮动组件采样。将源挂在应用滚动 Padding 之前的位置，避免栏位下方出现未录制区域。消费者不能被录入自身采样的源。

菜单或材质弹窗需要包含顶栏的完整窗口背景时，可在宿主 Composable 中使用以下结构：

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.itos.coui.menu.ProvideCouiMenuBackdrop

ProvideCouiMenuBackdrop { windowSource ->
    Box(Modifier.fillMaxSize().then(windowSource)) {
        // 原有页面；正文 Backdrop 保持独立。
        // 菜单与弹窗按各自接口使用窗口源，在独立浮层中绘制。
    }
}
```

`rememberCouiWindowBackdrop()` 读取最近提供的窗口源，未提供时返回 null。只在有相应消费者时安装窗口源；每个源挂到一个实际节点，每个绘制节点持有独立可变 renderer。资源和失败处理见[背景采样](../effects/backdrop.md)，具体消费见[菜单](../components/menu.md)与[弹窗](../components/dialog.md)。

## 状态与导航

查询、结果、返回栈、页面身份和受控设置值由宿主持有。单 Activity 应用可沿用 Compose 导航；多 Activity 应用可使用标准 Intent 与 `finish()`。普通、不透明 Activity 在未指定自定义动画时使用 ROM 提供的默认转场，不保证系统设置的特权动画或双栏协议。

搜索展开或有未保存变更时，由宿主安装局部返回处理器，状态关闭后释放拦截。切页时，把持续存在的底栏、renderer 和反馈节点留在稳定组合位置，只替换页面内容。

Demo 使用普通 Activity 导航。完整装配见 [MainActivity](../../demo/src/main/java/com/itos/coui/demo/MainActivity.kt)、[DemoSecondaryActivity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt)和 [DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)。这些路由和行包装属于 Demo，库使用者直接消费公开组件。
