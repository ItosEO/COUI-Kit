# 🔍顶栏搜索

CouiTopAppBarSearch 把顶栏里的搜索按钮展开成输入框，按钮和输入框共用同一份转场状态。查询、展开状态和返回处理放在页面里。

## 显示一个可展开的搜索

使用以下代码显示一个搜索顶栏：

~~~kotlin
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.topappbar.CouiToolbarStyle
import com.itos.coui.topappbar.CouiTopAppBar
import com.itos.coui.topappbar.CouiTopAppBarButtonGroup
import com.itos.coui.topappbar.CouiTopAppBarSearch
import com.itos.coui.topappbar.CouiTopAppBarSearchButton
import com.itos.coui.topappbar.rememberCouiTopAppBarSearchState

@RequiresApi(34)
@Composable
fun SearchToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop,
    style: CouiToolbarStyle,
    searchStyle: CouiToolbarStyle,
    title: String,
    placeholder: String,
    searchDescription: String,
    closeDescription: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var focusPending by remember { mutableStateOf(false) }
    val state = rememberCouiTopAppBarSearchState(expanded)
    val keyboard = LocalSoftwareKeyboardController.current
    val close = {
        expanded = false
        focusPending = false
        keyboard?.hide()
        Unit
    }
    BackHandler(enabled = expanded, onBack = close)
    CouiTopAppBar(modifier = modifier, backdrop = backdrop) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
                .heightIn(min = 64.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CouiTopAppBarSearch(
                modifier = Modifier.weight(1f),
                state = state,
                style = searchStyle,
                query = query,
                onQueryChange = onQueryChange,
                onSearch = onSearch,
                placeholder = placeholder,
                focusPending = focusPending,
                onFocused = { focusPending = false },
                trailingIcon = {
                    IconButton(onClick = close) {
                        Icon(Icons.Default.Close, contentDescription = closeDescription)
                    }
                },
            ) {
                Text(title)
            }
            CouiTopAppBarButtonGroup {
                actions()
                CouiTopAppBarSearchButton(
                    state = state,
                    style = style,
                    onClick = {
                        expanded = true
                        focusPending = true
                    },
                    contentDescription = searchDescription,
                )
            }
        }
    }
}
~~~

请注意，搜索按钮和输入框必须放在同一个 CouiTopAppBar 里，并共用同一个 `state`。把 `state` 提到页面层，不要在两处各建一个。

`expanded` 是搜索唯一的展开目标。关闭时同时清掉 `focusPending`、收起键盘；返回键只在展开时拦截。`onFocused` 在焦点请求发出后调用，记得把 `focusPending` 设回 `false`，否则每次重组都会重复请求。

组件不保存查询值。关闭时要不要清空查询，由页面决定。

## 添加常驻动作

右侧动作可以这样放：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarSearchAction

CouiTopAppBarButtonGroup {
    CouiTopAppBarSearchAction(state = state) {
        CouiTopAppBarButton(onClick = onReset, style = style) {
            Icon(Icons.Default.Refresh, contentDescription = resetLabel)
        }
    }
    CouiTopAppBarSearchAction(state = state, visibleWhileSearching = true) {
        CouiTopAppBarButton(onClick = onSettings, style = style) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel)
        }
    }
    CouiTopAppBarSearchButton(
        state = state,
        style = style,
        onClick = {
            expanded = true
            focusPending = true
        },
        contentDescription = searchDescription,
    )
}
~~~

默认动作在搜索展开时原位淡出；`visibleWhileSearching = true` 的动作会移到新位置并保持可用。`CouiTopAppBarSearchAction` 和 `CouiTopAppBarButton` 在同一个包里。

## 搜索框样式

搜索框用 `searchStyle`，通常和顶栏按钮共用一套构造。在 `rememberToolbarStyle` 里把材质和阴影换成：

~~~kotlin
material = { capsule -> CouiTopAppBarButtonDefaults.material(dark, capsule, search = true) },
shadow = { capsule -> CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour, search = true) },
~~~

`dark`、`density`、`contour` 就是 `rememberToolbarStyle` 里的同名变量，完整构造见[顶栏与按钮](top-app-bar.md#配置顶栏样式)。`CouiTopAppBarSearch` 的 `visible` 控制整组显隐，不会改变展开目标；设为 `false` 时搜索框也不接受输入。只想隐藏顶栏时别顺手把 `expanded` 设成 `false`。

## 版本和键盘

搜索入口要求 API 34。`BackHandler` 来自 Activity Compose，`IconButton` 来自 Material3，页面需要自己声明这两个依赖。

部分 ROM 上请求焦点后键盘仍可能不弹，用户点一下输入框就能输入。

搜索框会占满可用宽度，放在 Row 里用 `Modifier.weight(1f)`，再按页面需要调 `endPadding`。源码见[搜索与动作](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearch.kt)、[状态工厂](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearchState.kt)，页面示例见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
