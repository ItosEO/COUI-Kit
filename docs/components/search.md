# 🔍顶栏搜索

`CouiTopAppBarSearch` 可以将顶栏中的搜索按钮展开为输入框。页面负责保存查询内容和控制搜索的展开、关闭。

## 显示一个可展开的搜索

下面的搜索框支持输入、提交、关闭和返回键。搜索按钮与输入框需要放在同一个顶栏中，并使用同一份状态。此组件要求 API 34。

~~~kotlin
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
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
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.theme.LocalCouiTextStyle
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
                Text(title, style = LocalCouiTextStyle.current,
                    color = LocalCouiContentColor.current)
            }
            CouiTopAppBarButtonGroup {
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

`query` 和 `onQueryChange` 由页面提供。点击搜索按钮时设置 `expanded = true`，关闭时将其设为 `false`；是否清空查询内容也由页面决定。

示例在展开时设置 `focusPending`，并在 `onFocused` 中清除请求。关闭时同时清除该状态并收起键盘。返回键只在搜索展开时拦截。

## 添加常驻动作

将前面示例中的 `CouiTopAppBarButtonGroup` 替换为下面的内容，可以控制右侧动作在搜索展开时的显示方式。动作回调和描述文案由页面提供：

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import com.itos.coui.theme.LocalCouiContentColor
import com.itos.coui.topappbar.CouiTopAppBarButton
import com.itos.coui.topappbar.CouiTopAppBarSearchAction

CouiTopAppBarButtonGroup {
    CouiTopAppBarSearchAction(state = state) {
        CouiTopAppBarButton(onClick = onReset, style = style) {
            Icon(Icons.Default.Refresh, contentDescription = resetLabel,
                tint = LocalCouiContentColor.current)
        }
    }
    CouiTopAppBarSearchAction(state = state, visibleWhileSearching = true) {
        CouiTopAppBarButton(onClick = onSettings, style = style) {
            Icon(Icons.Default.Settings, contentDescription = settingsLabel,
                tint = LocalCouiContentColor.current)
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

默认动作会在搜索展开时淡出。设置 `visibleWhileSearching = true` 后，动作会移到新位置并保持可用。

## 搜索框样式

搜索框使用 `searchStyle`。可以在[顶栏样式](top-app-bar.md#配置顶栏样式)的基础上，将材质和阴影的 `search` 设为 `true`：

~~~kotlin
material = { capsule -> CouiTopAppBarButtonDefaults.material(dark, capsule, search = true) },
shadow = { capsule -> CouiTopAppBarButtonDefaults.shadow(dark, capsule, density, contour, search = true) },
~~~

`dark`、`density` 和 `contour` 来自顶栏样式示例。`CouiTopAppBarSearch` 的 `visible` 控制搜索区域显隐，不会改变 `expanded`；隐藏时搜索框不接受输入。

## 版本和键盘

`BackHandler` 来自 Activity Compose，`IconButton` 来自 Material3，页面需要声明相应依赖。

部分设备在请求焦点后可能不会自动弹出键盘，可以手动点击输入框。

搜索框会占满可用宽度，放在 Row 里用 `Modifier.weight(1f)`，再按页面需要调 `endPadding`。源码见[搜索与动作](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearch.kt)、[状态工厂](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearchState.kt)，页面示例见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。
