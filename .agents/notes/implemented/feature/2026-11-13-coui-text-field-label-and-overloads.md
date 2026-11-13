# Agent Note: 输入框标签按内容浮动并补齐重载

Status: implemented

## Problem

[卡片输入框](2026-11-12-coui-text-field.md)首版只有占位提示，字段标题交给 `Group` 标题或 `Footer`。一旦输入要落在一张独立卡片里（没有分组标题、也没有页面说明），宿主就只能自己拼一行标签，再处理字号、间距和禁用态。同一轮还暴露了三个值域缺口：宿主想控制选区与初始光标、想拿到实际排版结果、想恢复多行滚动位置，都只能绕开组件直接写 `BasicTextField`。

## Decision

`TextField` 在卡片内部自带标签槽，位置只由内容决定，与焦点无关：

- 空态文案分两层：`label` 是标题，`placeholder` 是输入区提示，各自只看内容。只传标签时，标签在空态落到输入位置，有内容后在 8dp 内上浮并缩到 12sp；同时传 `placeholder` 时，标签直接停在顶部当小标题，输入区留给提示。标签按"输入区是否已有自己的提示"决定落位，上浮只改字号和纵向偏移，卡片高度不变。
- `useLabelAsPlaceholder = true` 时标签自己当占位提示：有内容直接隐藏，不上浮，`placeholder` 不再参与。它表达的是"这段提示只服务空态"，不是另一种浮动方式。
- 焦点既不参与标签位置判定，也不影响配色。组件不画描边、边框、底色变化或底色动画，标签色也不随焦点切换：编辑态的唯一视觉增量是光标，因此 `CouiTextFieldColors` 只有 `label`，不设 `focus` 角色。
- 三种重载共用外观：`String`、`TextFieldValue`（宿主持有选区与光标）与 `TextFieldState`（编辑事务与撤销栈）。宿主回调统一经 `rememberUpdatedState` 转交，避免闭包每帧变化把旧 lambda 带进 Foundation。
- `onTextLayout` 按各自分支的原生类型透传：`String` / `TextFieldValue` 收 `TextLayoutResult`，状态分支沿用 `Density.(getResult: () -> TextLayoutResult?) -> Unit`。
- `scrollState` 只在状态重载暴露。Foundation 的 `String` / `TextFieldValue` 分支没有该参数，为它们再造一个可注入滚动状态只会多一条不可用分支。

标签度量放在 `CouiTextFieldDefaults`：`labelSize`（`textStyle` 未指定字号时的回退，16sp）、`floatingLabelSize`（12sp）、`labelShift`（8dp）。颜色角色只新增 `label`（取 `onSurfaceVariant`），与占位提示同源但语义独立：标签会一直存在，占位提示只在空态出现。

## Alternatives considered

- 标签按焦点浮动（Material 行为）：聚焦即上浮会让空态在点入时改变轮廓，而且焦点进入视觉层就与"编辑态只靠光标"直接冲突。内容驱动只承认一件事：这段字段是否已经有值。
- 焦点用 2dp 描边或底色变化（miuix 的 `borderColor` 做法）：卡片与同页 `Group` 同形同色，描边会在分组卡片之间多出一圈不属于页面语汇的线；底色动画还会让输入时整卡变色。
- 标签随焦点切换强调色：曾用 `focus` 角色实现"聚焦时标签变色"，作为描边之外最轻的聚焦增量。评审结论是连这一层也不要——"只靠光标"意味着标签不参与焦点表达，保留一个没有消费点的颜色角色只会让 `CouiTextFieldColors` 多一个空参数。
- 标签固定在输入上方、不浮动：卡片要么永远留两行高，要么在首个字符落下时突然变高，两种都把成本转移到宿主布局上。
- 用 `placeholder` 一个参数兼任标题与提示：无法区分"常驻标题"和"只在空态出现的提示"，`useLabelAsPlaceholder` 也就无从表达。
- `label` 与 `placeholder` 互斥，同时传入时忽略 `placeholder`：实现更简单，但宿主无法表达"顶部标题 + 输入区提示"这一常见表单结构，只能把标题挪到卡片外面，卡片与标题的间距也由宿主重新对齐。
- `String` 重载也暴露 `scrollState`：Foundation 的对应分支没有这个参数，组件只能自己 `rememberScrollState()` 或伪造一个不可注入的入口。

## Consequences

- 标签与输入共用同一个 `Box`，靠 `offset` 重叠布局，上浮距离由 `contentPadding` 的纵向留白吸收，因此不增加测量分支，也不额外撑高卡片。
- 组件不表达聚焦态，宿主若要描边或聚焦强调色，只能在外面包一层自己绘制；组件不提供入口，避免两条并行实现。
- 12sp 上浮字号与 8dp 位移来自参考图估算，没有取得 OEM 字段级证据。`:demo:assembleDebug` 已在本机通过并安装到设备，标签上浮、标签与占位提示同时可见、placeholder 有内容即消失三态已在真机确认；字号、字重、多行首行对齐与 RTL 仍需继续逐项验收。
- `TextFieldValue` 与 `String` 重载有同名参数、只有 `value` 类型不同，宿主显式传入 `TextFieldValue` 时不会与 `String` 混淆；但省略 `value` 的命名调用无法解析，属于预期行为。

## Existing-note audit

- [卡片输入框](2026-11-12-coui-text-field.md)：部分重叠。表面、配色来源与"不接入材质"仍然成立，本篇只翻转其中「不抽 label 槽」这一条备选，并在该篇保留指向本篇的链接。
- [输入型弹窗](2026-10-27-coui-input-dialog-preset.md)：无关，弹窗输入行仍走自己的 15sp 排版，不进卡片。
