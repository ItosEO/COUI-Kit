# 📋菜单

`CouiMenu` 可以显示普通菜单项、勾选项和一级子菜单。菜单的展开状态和选择结果由页面保存。

## 最小调用

页面提供展开状态、菜单项和回调：

~~~kotlin
import com.itos.coui.menu.CouiMenu

CouiMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    items = items,
    onItemClick = onItemClick,
)
~~~

需要采样页面背景时，把菜单调用放在[背景材质 Provider](#使用背景材质)的 `ProvideCouiMenuBackdrop` 内容中；默认样式不会创建背景源。样式默认使用 `CouiMenuDefaults.style()`，显式传入的样式仍优先。原生默认 `themeTint` 透明；M3 适配取 host surface，显式透明可关闭。通用主题规则见[外观与预设](../guide/appearance.md)。

## 显示一个菜单

下面的按钮会打开菜单。选择项目后调用 `onSelect`，然后关闭菜单：

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
import com.itos.coui.menu.CouiMenuDefaults
import com.itos.coui.menu.CouiMenuItem
import com.itos.coui.menu.CouiMenuStyle
import com.itos.coui.menu.couiMenuAnchor
import com.itos.coui.menu.rememberCouiMenuAnchor

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    style: CouiMenuStyle = CouiMenuDefaults.style(),
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

`anchor` 记录按钮上的触点，菜单根据触点和窗口可见范围定位。通过键盘或无障碍服务打开时，没有触点，菜单使用默认锚点。示例中的 Material3 `Button` 和 `Text` 需要由应用声明 Material3 依赖；最小调用不需要该依赖。自定义 `style` 的创建方式见[菜单样式](#菜单样式)。

`expanded` 控制菜单显隐。点击菜单项时调用 `onItemClick`；点击外部或按返回键时调用 `onDismissRequest`。需要在选中后关闭菜单时，在 `onItemClick` 中将 `expanded` 设为 `false`，如上例所示。

## 添加勾选和子菜单

每个 `CouiMenuItem` 对应一个选项：

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

示例中的 `selected` 和 `pinned` 是页面状态。点击勾选项后，需要在 `onItemClick` 中更新 `pinned`。有 `children` 的父项只负责展开，子项才会提交值；子菜单只支持一级。

## 使用背景材质

不提供背景源时，菜单使用 `CouiMenuStyle.colors.container` 作为不透明底色。需要采样页面背景时，用 `ProvideCouiMenuBackdrop` 包住页面，并将返回的 Modifier 加到背景内容上：

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

这里的 `background` 和 `pageContent` 由页面提供，`MenuButton` 沿用前面的示例。菜单放在背景源之外；弹窗和页面遮罩也不要录入背景源。`style.themeTint` 可以传页面表面色。

### 空闲时按需离屏

上面的单参数入口保留内容隔离，现有调用无需迁移。对于已经核对的不透明、静态页面根，可以显式使用 `isolateContent = false`，让没有菜单或材质弹窗消费背景时的源使用 `CompositingStrategy.Auto`。下面沿用前面的 import、参数和 `MenuButton`：

~~~kotlin
ProvideCouiMenuBackdrop(isolateContent = false) { source ->
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().then(source).background(background)) {
            pageContent()
        }
        MenuButton(style = style, label = label, items = items, onSelect = onSelect)
    }
}
~~~

库在菜单进入、显示和退出期间保持源及其父窗口源的离屏隔离。材质弹窗按实际存活期持有背景；最后一个消费者释放后，源恢复所选的空闲策略。普通菜单、子菜单和嵌套窗口共用这套管理，宿主不要按 `expanded` 手动切换背景策略。

此模式保留源节点、裁剪、坐标和绘制指令录制，只取消空闲时强制建立的离屏隔离。`Auto` 不保证底层完全没有离屏缓冲，也不保证固定比例的 GPU 收益。内容的目标混合必须自行隔离；透明窗口、移动的 BottomSheet、缩放或分数位移容器应先保留原入口。完整的合成要求见[背景源的离屏策略](../effects/backdrop.md#菜单窗口的离屏策略)，公开声明见 [ProvideCouiMenuBackdrop](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBackdrop.kt)。

## 菜单样式

`CouiMenuStyle` 设置菜单的颜色、形状和文字。下面根据页面颜色创建一份样式：

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

`radius` 要和 `shape` 的半径一致。菜单没有默认高度上限；长菜单可以在 `CouiMenu` 的 `modifier` 中传入 `Modifier.heightIn(max = 320.dp)`，让它在限定高度内滚动。子菜单会按窗口高度限制。

新样式建议在末尾传 `dark = dark`。默认工厂已填实际模式，主菜单与子菜单的文字、材质、阴影和反馈共同使用它。旧完整 Style 未填模式时保留原 surface 亮度判定；自动 M3 默认值不会猜色板模式。M3 模糊补底取 surface，无 Backdrop 的容器取 surfaceBright；selected 取 primary，checkbox 深色取 inversePrimary。

## 兼容性和滚动

API 33 以下的菜单模糊效果与较新系统不同，见[兼容要求](../guide/compatibility.md)。

`headerText` 用于显示提示文案，不参与选择。`themeTint` 透明时关闭采样材质上的页面色相叠加；显式样式可以提供非透明色。

标题字体默认来自 `CouiTheme.typography.label`。修改主题的 `label` 或显式传入 `CouiMenuStyle.headerTextStyle` 可以调整字体、字号、字重和行距；绘制时不再覆盖这些设置。标题颜色、对齐和最多两行的规则仍由菜单控制。

源码见 [CouiMenu / CouiMenuItem](../../coui/src/main/java/com/itos/coui/menu/CouiMenu.kt)、[样式](../../coui/src/main/java/com/itos/coui/menu/CouiMenuStyle.kt)、[触点锚点](../../coui/src/main/java/com/itos/coui/menu/CouiMenuAnchor.kt)，选中、勾选、子菜单和限高示例见 [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)。

顶栏三点入口可用 rememberCouiMenuAnchor(useTouchPosition = false)，保持完整按钮布局锚点；设置行默认捕获触点。两者只观察，不接管点击 owner。
