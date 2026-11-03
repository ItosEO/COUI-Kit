# 页面容器

`Scaffold` 测量顶栏、底栏并向正文提供 Padding。它使用 Compose UI / Foundation，纯 COUI 页面只需依赖核心 `coui`。

## 显示带顶栏的列表

下面用顶栏槽显示标题，将正文 Padding 传给列表。标题和条目由页面提供，Activity 负责启用 edge-to-edge。

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Scaffold
import com.itos.coui.layout.Group
import com.itos.coui.layout.Item
import com.itos.coui.text.Text
import com.itos.coui.text.CouiTextKind
import com.itos.coui.theme.CouiTheme

@Composable
fun NativePage(
    modifier: Modifier = Modifier,
    title: String,
    entries: List<String>,
) {
    CouiTheme {
        Scaffold(
            modifier = modifier,
            topBar = {
                Box(
                    Modifier.fillMaxWidth().statusBarsPadding()
                        .height(CouiPageDefaults.toolbarHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = title, kind = CouiTextKind.Toolbar)
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
                contentPadding = padding,
            ) {
                items(entries) { entry ->
                    Group {
                        Item { Text(text = entry, kind = CouiTextKind.PreferenceTitle) }
                    }
                }
            }
        }
    }
}
```

正文先绘制，顶栏和底栏覆盖在正文上。需要模糊顶栏时，让背景源覆盖整个正文视口，使滚动内容进入栏位下方；装配见[宿主页面接入](../guide/secondary-pages.md)。

## Padding 与系统 Insets

`topBar`、`bottomBar` 均可省略。`containerColor` 默认读取 `CouiTheme.colorScheme.background`，只设置容器背景，不修改内容槽的主题。

`windowInsets` 默认包含系统栏与屏幕缺口。容器先应用并消费横向安全区，使栏位和正文使用同一安全宽度；返回的正文 Padding 不再包含这部分。父级已消费的 Insets 会被扣除。

| 区域 | 处理方式 |
| --- | --- |
| 有顶栏 | 正文顶部 Padding 等于顶栏实测高度；顶栏内容负责顶部安全区 |
| 有底栏 | 正文底部 Padding 等于底栏实测高度；底栏负责底部安全区 |
| 栏位为空或高度为零 | 对应正文 Padding 使用剩余系统 Insets |

`CollapsingTopAppBar` 已处理状态栏；通用 `TopAppBar` 的内容需要使用 `statusBarsPadding()`。`BottomBar` 自带底部可点击区域的避让和浮动间距。自定义底栏应按其布局使用 `navigationBarsPadding()` 等 Foundation Modifier。不要把同一纵向 Insets 再加到返回的正文 Padding 上。

普通正文使用 `Modifier.padding(padding).consumeWindowInsets(padding)`。`LazyColumn` 使用 `contentPadding = padding` 并消费它；`Column` 加滚动时，将 Padding 放在滚动 Modifier 后，保留栏位下方的采样区域。折叠标题需使用状态的顶部补偿，见[折叠顶栏](collapsing-app-bar.md)。

含输入框的页面在容器外侧使用 `Modifier.imePadding()`，使键盘打开时缩小实际视口；默认 `windowInsets` 不包含 IME。只增加正文尾部空白不能代替焦点避让。父级已处理 IME 时沿用父级分工。消费规则见 [Android 官方 Insets 文档](https://developer.android.com/develop/ui/compose/system/insets-ui)。

容器占满父级提供的有界宽高，不放入纵向无界滚动内容。关闭系统安全区处理可传入 `windowInsets = WindowInsets(0, 0, 0, 0)`，随后由宿主负责避让。

## 混合设计应用

已有 Material 3 `Scaffold`、Miuix 或自有布局可以继续使用。COUI 控件、主题和采样接口都可独立调用，见[宿主页面接入](../guide/secondary-pages.md)。导航、返回、业务状态、滚动、Snackbar 和 FAB 由宿主组合；设置页直接组合 `Group` 与 `Item`。

[公开实现](../../coui/src/main/java/com/itos/coui/layout/Scaffold.kt) · [首页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt) · [二级页 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt)
