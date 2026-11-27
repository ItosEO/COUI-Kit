# 输入框

`TextField` 是页面内的圆角输入卡片：无边框、卡片底色、标签、占位提示与前后图标槽，位于 `com.itos.coui.textfield`。它只负责把当前值、光标和焦点画出来，值、校验和提交仍由宿主持有。

| 用途 | 使用 |
| --- | --- |
| 单行或多行文本输入 | `TextField` |
| 纯展示的选择行（右侧当前值 + 箭头） | [Item](item.md) |
| 输入型弹窗 | [弹窗](dialog.md) |

## 基本用法

```kotlin
@Composable
fun LocationField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    colors: CouiTextFieldColors,
) {
    TextField(modifier = modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
        value = value, onValueChange = onValueChange,
        placeholder = "地点",
        trailing = { Icon(CouiIcons.Filled.Location, null, Modifier.size(CouiPageDefaults.iconSize), tint = colors.text) })
}
```
`padding` 与 `colors` 由页面提供，`Icon` 来自宿主使用的图标库。输入框本身不加页面留白，与 [Group](group.md) 同页时按 `CouiPageDefaults.horizontalMargin` 对齐。

## 三种入口

三种重载共用外观和装配参数。根据状态归属选择入口。

| 入口 | 值的持有 | 适用 |
| --- | --- | --- |
| `value: String` + `onValueChange` | 宿主，只有文本 | 大多数表单 |
| `value: TextFieldValue` + `onValueChange` | 宿主，文本加选区与光标位置 | 聚焦时全选、指定初始光标 |
| `state: TextFieldState` | `TextFieldState`，含编辑事务与撤销栈 | 富编辑、需要 `inputTransformation` / `outputTransformation` |

状态重载提供 `inputTransformation` 与 `outputTransformation`。前者限制或改写编辑事务，后者改写显示文本。输出变换可能为绘制和空态判断分别调用，应只修改传入的缓冲区。业务校验由宿主持有。

两个键盘动作入口与 Foundation 一致：`String` / `TextFieldValue` 重载使用 `KeyboardActions`，`TextFieldState` 重载使用 `onKeyboardAction`，后者只收到默认动作的调用，拿不到当前 `ImeAction`。需要在状态重载里区分搜索、发送等动作时，由宿主改用前两个重载或自行处理按键。

## 标签与占位提示

`label` 提供字段标题，`placeholder` 提供输入提示。两者的显示由格式化后的文字决定，与焦点无关。

| `label` | `placeholder` | 空值 | 有内容 |
| --- | --- | --- | --- |
| 有 | 无 | 标签占据输入位置当提示 | 标签缩小并上浮，首字进入预留的独立输入区 |
| 有 | 有 | 标签停在卡片顶部（12sp），输入区显示占位提示 | 标签位置不变，占位提示消失 |
| 有 + `useLabelAsPlaceholder` | 忽略 | 标签占据输入位置当提示 | 标签整体隐藏，不上浮 |
| 无 | 有 | 输入区显示占位提示 | 占位提示消失 |
| 无 | 无 | 卡片留空，只有图标槽 | — |

标签按"让位"规则落位：输入区已经有自己的提示时，标签停在顶部当小标题；没有提示时，标签落到输入位置承担这段文案，不必再传 `placeholder`。`useLabelAsPlaceholder` 让标签自己当提示，因此它和 `placeholder` 不会同时生效。

浮动标签按字号和字体度量预留空间。占位提示与正文对齐首行基线。编辑节点优先用 `label` 作为无障碍提示，名称在视觉标签隐藏后仍可读取。只有 `placeholder` 时，它提供无障碍提示。

聚焦不画描边、不改底色，也不改标签颜色：编辑态的唯一线索是光标，其余外观保持不变。

## 状态与事件

`value` 与 `onValueChange` 是受控入口。宿主持有文本，组件在每次编辑后回调完整新值。使用 `TextFieldState` 时，宿主创建并持有状态，编辑事务和撤销交给 Foundation。`VisualTransformation` 或 `OutputTransformation` 生成的非空文字也会隐藏占位提示。

点击卡片任意位置都会聚焦并把光标落在点击处，包括文字两侧的留白；`leading` 和 `trailing` 里的可点击控件先消费事件，不会同时聚焦输入框。

`enabled = false` 停止输入与聚焦，文字、标签和占位提示改用禁用色；`readOnly = true` 仍可聚焦、选择与复制，只是不能改写内容。

`onTextLayout` 在文本排版变化时回调：`String` / `TextFieldValue` 重载收到 `TextLayoutResult`，`TextFieldState` 重载沿用 Foundation 的 `Density.(getResult: () -> TextLayoutResult?) -> Unit`。需要按实际行数调整外部布局时使用它，不要在组合里自己估算行数。

## 单行与多行

`singleLine = true` 是默认值，字段按字号、标签和内边距测量，最低高度由 `CouiTextFieldDefaults.minHeight` 提供。多行时设 `singleLine = false`，字段从 `minLines` 开始增长，超过 `maxLines` 后内部滚动。多行字段的图标槽靠上排列。单行时这两个行数参数不参与布局。

`maxLines` 同时限制占位提示的行数，超出时显示省略号。多行时 `maxLines` 必须大于等于 `minLines`，且两者都必须大于零。违反约束时 Foundation 抛出 `IllegalArgumentException`。

滚动位置默认由组件内部的 `rememberScrollState()` 持有。状态重载可以传入自己的 `scrollState`，用于恢复草稿位置或和宿主的滚动联动；`String` / `TextFieldValue` 重载沿用 Foundation，不暴露该参数。

## 外观与预设

| 入口 | 作用 |
| --- | --- |
| `colors: CouiTextFieldColors` | 卡片底色、正文、占位提示、标签、禁用文字与光标颜色 |
| `shape` | 卡片轮廓，默认 `CouiTextFieldDefaults.shape` |
| `contentPadding` | 卡片内留白 |
| `textStyle` | 覆盖输入、标签与占位提示共用的排版，未指定的属性沿用 `CouiTextFieldDefaults.textStyle()` |
| `cursorBrush` | 光标画笔，默认取 `colors.cursor` |

`CouiTextFieldDefaults.colors(dark)` 从当前主题解析六个角色：`container` 是卡片底色（与同页分组卡片同源的 `card`）、`text` 是输入文字、`placeholder` 是占位提示、`label` 是标签、`cursor` 是光标（跟随强调色）、`disabled` 是禁用态文字。强调色与主题不一致时，在该结果上 `copy(cursor = accent)`，不要另建色板。

组件没有聚焦态配色：不画描边、边框或底色动画，标签颜色也不随焦点变化，输入过程中卡片外观保持稳定。

默认排版使用系统无衬线常规字重，行距与列表主文字一致。`textStyle.fontSize` 使用 `em` 时，相对于组件的默认字号。需要对齐前缀或计数器时，可使用 `CouiTextFieldDefaults.textStyle()`。

组件不创建局部主题作用域，也不改写槽位内容的颜色：`leading` 与 `trailing` 里的图标需要显式 `tint`，尺寸由调用方决定，常用 24dp。

## 常用组合

### 密码输入

掩码与键盘类型由 Foundation 提供，尾部按钮切换 `VisualTransformation`。示例中的密码与明文开关只随当前界面的组合存活。`ImeAction.Done` 通过 `KeyboardActions` 收起键盘。

```kotlin
var password by remember { mutableStateOf("") }
var visible by remember { mutableStateOf(false) }
val focus = LocalFocusManager.current

TextField(
    modifier = Modifier.fillMaxWidth(),
    value = password,
    onValueChange = { password = it },
    label = "密码",
    useLabelAsPlaceholder = true,
    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
    trailing = {
        // 尾部槽位里的可点击控件先消费事件，不会顺带聚焦输入框。
        Box(Modifier.size(CouiPageDefaults.iconSize)
            .clickable(role = Role.Button) { visible = !visible }, contentAlignment = Alignment.Center) {
            Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (visible) "隐藏密码" else "显示密码")
        }
    },
)
```

### 校验错误

组件没有内置错误态。宿主在校验失败时覆盖 `label` 与 `cursor`，错误文字由页面自己排版：

```kotlin
val invalid = email.isNotEmpty() && !emailPattern.matches(email)

TextField(
    modifier = Modifier.fillMaxWidth(),
    value = email,
    onValueChange = { email = it },
    label = "邮箱",
    colors = if (invalid) colors.copy(label = errorColor, cursor = errorColor) else colors,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
)
if (invalid) {
    Text(text = "请输入有效的邮箱地址", kind = CouiTextKind.GroupFooter, color = errorColor,
        modifier = Modifier.padding(start = CouiPageDefaults.rowPadding + 4.dp, top = 4.dp))
}
```

错误色由宿主主题提供，不要复用 `focus` 的强调色。

### 多行备注

```kotlin
val note = rememberTextFieldState()

TextField(
    modifier = Modifier.fillMaxWidth(),
    state = note,
    label = "备注",
    singleLine = false,
    maxLines = 5,
)
```

多行时标签仍然按内容浮动；超过 `maxLines` 后输入区内部滚动，卡片高度不再增长。

## 平台与装配

输入框只依赖 Foundation 与 COUI 核心，不额外提高系统版本要求（与库一致，API 31+），也不需要背景采样源。它不安装滚动、不消费系统栏 Insets，页面负责键盘避让；页面装配见[宿主页面接入](../guide/secondary-pages.md)。

需要横向并排多个输入框时，用 `Modifier.width` 或 `weight` 限制各自宽度；字段默认占满可用宽度。输入框是独立卡片，不要放进 `Group` 的卡片内容中。

## 完整示例与 API

源码见 [TextField](../../coui/src/main/java/com/itos/coui/textfield/TextField.kt) 与 [Defaults](../../coui/src/main/java/com/itos/coui/textfield/CouiTextFieldDefaults.kt)。Demo 装配见 [DemoTextField](../../demo/src/main/java/com/itos/coui/demo/DemoTextField.kt)。
