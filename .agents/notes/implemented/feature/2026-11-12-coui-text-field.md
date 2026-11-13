# Agent Note: 正文输入框复用页面色板与分组轮廓

Status: implemented

## Problem

参考页面把三张卡片输入直接放在正文里：`地点` 带尾部图标、`备注` 为多行输入、`即时消息` 带头部图标；同页的 `日历分类` 是选择行而不是输入框。库内只有两处输入：常驻搜索胶囊（玻璃材质、双主体、取消胶囊、需要背景采样）和弹窗输入行（15sp，位于弹窗外壳内）。宿主想把输入放进正文，只能在 `Item` 或裸布局里塞 `BasicTextField`，自己处理卡片底色、圆角、内留白、占位提示、光标颜色和图标槽。

## Decision

新增 `com.itos.coui.textfield.TextField`，只承担一层卡片输入：

- 表面由 `defaultMinSize(minHeight) + clip(shape) + background(card)` 构成，不接入材质、模糊、跟手光或投影。参考页里的输入卡片与同页分组卡片同形同色，页面输入不承担玻璃表面语义，也没有可用背景源。
- 配色由组件族自己的 `CouiTextFieldColors`（`container` / `text` / `placeholder` / `cursor` / `disabled`）持有，`CouiTextFieldDefaults.colors(dark)` 从同一份 `CouiColorScheme` 解析：`card` 是底色、`onSurface` 是文字、`onSurfaceVariant` 是占位提示、`accent` 是光标。取值与页面色板同源，但公开参数不依赖 `layout` 的 `CouiPageColors`——和 SearchBar、Dialog 等族一样，输入组件族自持有角色。
- 形状直接引用 `CouiPageDefaults.shape`，与 [Group](../../../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt) 卡片同轮廓，不复制半径与权重。
- 排版不外扩公开的 `CouiTextKind`，而在 `CouiTextFieldDefaults.textStyle()` 内按族私有方式计算（16sp、`sans-serif`、400，行距与列表主文字一致），沿用弹窗 `DialogTextKind` 的既有做法。占位提示与输入共用同一排版、只换颜色；不复用 500 字重的 `ItemTitle`，也不复用 15sp 的 `Search`（那是搜索胶囊与弹窗输入的字号）。
- 槽位为 `placeholder`、`leading`、`trailing`。图标尺寸与颜色由调用方决定，组件不改写槽位内容，与 Group/Item 的内容槽约定一致。
- 装饰层用 `Box(Modifier.weight(1f))` 撑开输入区，使图标槽之间的留白也属于输入本体的点击范围；槽位内的可点击控件先消费事件，因此清空按钮不会顺带聚焦输入框。
- 两个重载：`value: String` + `onValueChange`，以及宿主持有的 `TextFieldState`。状态重载把 `inputTransformation` / `outputTransformation` 原样交给 Foundation；不为 String 重载再包一层状态同步。
- 键盘动作沿用 Foundation 自身的不对称：String 重载收 `KeyboardActions`，状态重载收 `onKeyboardAction: KeyboardActionHandler?`。后者只拿到默认动作、没有当前 `ImeAction`，在组件内部无法还原成 `KeyboardActions`，因此不强行统一成一个参数。
- `singleLine` 决定单行语义；`minLines` / `maxLines` 只在多行生效，并在组合期要求 `maxLines >= minLines`，不静默夹取。
- 组件不加页面留白。外部间距与宽度由调用方 Modifier 提供，与 Group 同页时按 `CouiPageDefaults.horizontalMargin` 对齐。

## Alternatives considered

- 复用 `SearchBar`：它的形状是玻璃胶囊，带双主体/取消胶囊与展开状态机，还要求背景源；即使关闭材质也与参考图的扁平卡片不符，且为一个静态卡片引入采样依赖。
- 在 `Item` 内增加输入变体：`Item` 的整行 `clickable` 是唯一点击 owner，输入会与它争夺焦点与点击语义，行内禁用态也无法表达输入本身的只读。
- 直接复用 `CouiPageColors` 当公开参数：省掉一次角色映射，但把输入组件族的公开 API 绑到 `layout` 的页面类型上，页面色板增删会牵动输入框签名。
- 扩公开的 `CouiTextKind` 加输入角色：宿主对枚举的穷举 `when` 会因此编译失败，而弹窗已经证明族内私有排版够用。
- 让 String 重载内部用 `rememberTextFieldState` + `LaunchedEffect` 回写外部值：外部值回流会与输入法组合区竞争，直接使用 Foundation 的 String 重载更稳。
- 抽 `label` / 浮动标签槽：参考页面没有标签，标签由 Group 标题或 Footer 承担；增加未使用的槽位只会多一条测量分支。宿主随后提出了自带标题的输入场景，这条备选已在[输入框标签按内容浮动并补齐重载](2026-11-13-coui-text-field-label-and-overloads.md)翻转。

## Consequences

- 新实现只有布局与配色装配，不含 Shader、采样源与平台门槛，`minSdk` 26 即可。
- 字段与页面卡片各持一份颜色角色，取值都来自 `CouiColorScheme`，不会出现色差；代价是宿主改强调色时要对两个对象分别处理。
- 52dp 最小高度、16dp/14dp 内留白、12dp 槽间距与 24dp 图标建议取自参考图按 3x 密度估算，没有取得 OEM 字段级证据；真机字重、光标颜色、多行滚动与 RTL 仍需设备验收。
- 本轮未运行 Android 构建与设备操作（按 AGENTS 授权范围），只做静态检查。

## Existing-note audit

- [设置式组件、常驻搜索与折叠顶栏](2026-11-01-secondary-page-primitives.md)：部分重叠，共用 Group 轮廓与同源配色；本篇只新增正文输入卡片，且颜色取值同源、类型各自持有，不改其决定。
- [输入型弹窗](2026-10-27-coui-input-dialog-preset.md)：部分重叠，同样把编辑事务交给 Foundation，也沿用其族内私有排版的做法；弹窗输入保持 15sp 与弹窗外壳，与页面输入的 16sp 属于上下文差异，不互相合并。
- 悬浮操作栏（同批拆分出的另一篇实现记录）：无关，材质、投影与采样路径不同。
