# 📋菜单

CouiMenu 是带选中、勾选、分隔和一级子菜单的弹出菜单。展开状态和选择结果放在页面状态层，菜单自己负责弹出、点击和关闭动画。

## 显示一个菜单

使用以下代码显示一个由按钮打开的菜单：

~~~kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itos.coui.menu.CouiMenu
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    style: CouiMenuStyle,
    label: String,
    items: List<CouiMenuItem<Int>>,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val anchor = rememberCouiMenuAnchor()
    Box(modifier) {
        Button(
            modifier = Modifier.couiMenuAnchor(anchor),
            onClick = { anchor.open { expanded = true } },
        ) {
            Text(label)
        }
        CouiMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = items,
            onItemClick = { item ->
                onSelect(item.value)
                expanded = false
            },
            style = style,
            anchor = anchor,
        )
    }
}
~~~

菜单默认贴着按钮下方弹出。`anchor` 记录最后一次触点，菜单会按这个位置和当前窗口可见范围重新定位；键盘或无障碍触发没有触点，就沿用默认锚点。

`expanded` 是唯一开关。点击菜单项、点击外部或返回键触发 `onDismissRequest`，不会自动替你提交选择。

## 添加勾选和子菜单

`CouiMenuItem` 的常用字段：

| 字段 | 作用 |
| --- | --- |
| `value` | 提交给业务层的值 |
| `text` | 显示文本 |
| `icon` / `iconTint` | 图标和自定义颜色 |
| `selected` | 当前选中样式，和勾选状态分开 |
| `checked` | 独立复选框状态；`null` 表示普通菜单项 |
| `enabled` | 是否接受点击 |
| `dividerBefore` | 在该项前加分组线 |
| `children` | 一级子菜单；父项只展开，不提交值 |

~~~kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit

val items = listOf(
    CouiMenuItem(0, "重命名", icon = Icons.Default.Edit, selected = selected == 0),
    CouiMenuItem(1, "固定到桌面", icon = Icons.Default.Check, checked = pinned),
    CouiMenuItem(2, "更多", dividerBefore = true, children = listOf(
        CouiMenuItem(3, "分享"),
        CouiMenuItem(4, "删除", enabled = false),
    )),
)
~~~

父项有 `children` 时只负责展开，值由子项提交。子菜单只能有一级，再嵌套会被拒绝。勾选项是否切换由页面决定，菜单只把点击回调出去。

## 使用背景材质

不提供背景源时，菜单用 `CouiMenuStyle.colors.container` 画不透明表面。要让菜单采样页面背景，用 `ProvideCouiMenuBackdrop` 包住页面内容。这里假设当前页面函数已有 `background: Color` 和 `pageContent`，`style`、`label`、`items`、`onSelect` 沿用上一节：

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.itos.coui.menu.ProvideCouiMenuBackdrop

ProvideCouiMenuBackdrop { source ->
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().then(source).background(background)) {
            pageContent()
        }
        MenuButton(style = style, label = label, items = items, onSelect = onSelect)
    }
}
~~~

菜单本身在采样子树外。弹窗、页面自画的遮罩和菜单自身都不要录进源里。`style.themeTint` 控制材质叠加的页面色相，通常传当前页面表面的颜色。

## 菜单样式

`CouiMenuStyle` 描述表面、菜单项、复选框和文字。库不读 MaterialTheme，需要页面自己组一份：

~~~kotlin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.checkbox.CouiCheckboxDefaults
import com.itos.coui.menu.CouiMenuColors
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.shape.CouiShape

@Composable
fun rememberMenuStyle(
    dark: Boolean,
    accent: Color,
    surface: Color,
    contentColor: Color,
): CouiMenuStyle = remember(dark, accent, surface, contentColor) {
    CouiMenuStyle(
        colors = CouiMenuColors(
            surface = surface,
            container = surface,
            primary = accent,
            onSurface = contentColor,
            onSurfaceVariant = contentColor.copy(alpha = 0.6f),
            outlineVariant = contentColor.copy(alpha = 0.12f),
        ),
        shape = CouiShape(16.dp, cornerWeight = 2f),
        itemShape = CouiShape(10.dp, cornerWeight = 2f),
        checkboxShape = CouiShape(4.dp),
        checkboxBorderShape = CouiShape(3.2.dp),
        checkboxColors = CouiCheckboxDefaults.colors(checked = accent, dark = dark),
        radius = 16.dp,
        itemTextStyle = TextStyle(fontSize = 15.sp, color = contentColor),
        headerTextStyle = TextStyle(fontSize = 12.sp, color = contentColor.copy(alpha = 0.6f)),
        themeTint = surface,
    )
}
~~~

`radius` 要和 `shape` 的半径一致。菜单没有默认高度上限；长菜单要给 `CouiMenu` 传 `Modifier.heightIn(max = …)`，它才会在这个高度内滚动。子菜单会按窗口高度收住。

## 兼容性和滚动

API 33 以下菜单的模糊和 33 以上不完全一致，具体限制见[兼容要求](../guide/compatibility.md)。

光效是 COUI 加的；`themeTint` 设成透明就不叠加页面色。`headerText` 是提示文案，不参与选择。长菜单给了 `heightIn` 后会在内部滚动，外面不要再套滚动容器。

源码见 [CouiMenu / CouiMenuItem](../../coui/src/main/java/com/itos/coui/menu/CouiMenu.kt)、[样式](../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)、[触点锚点](../../coui/src/main/java/com/itos/coui/menu/CouiMenuAnchor.kt)，选中、勾选、子菜单和限高示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。
