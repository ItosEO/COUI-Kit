# Agent Note: 输入弹窗窗口策略和真实编辑范围的审查修正

Status: implemented

## Problem

PR #6 的审查指出：仅改变 DialogProperties 时 Compose Dialog.updateParameters 会重置 softInputMode，而输入内容可能跳过重组；旧内容 SideEffect 无法保证恢复键盘模式。Demo 按公共前后缀推断插入片段，把全选后粘贴中与旧文本相同的部分当作未替换内容，导致同一粘贴内容产生不同结果。此外按钮测量和实际布局有两份相同的尺寸常量。

## Decision

输入预设只通过内部 LocalCouiDialogSoftInputMode 声明策略，[CouiDialogWindow](../../../../coui/src/main/java/com/itos/coui/dialog/CouiDialogWindow.kt)持有实际 Window。子组合首次发布窗口时应用初始配置；owner 在组合阶段读取窗口引用，并在 Dialog 调用之后的 SideEffect 统一设置窗口属性，确保晚于 Compose updateParameters。输入内容不重组、窗口标题改变或 autoFocus 改变，均由同一窗口 owner 恢复所需模式；普通警告没有输入策略时保留 Compose 默认行为。没有增加公开窗口回调或另一套 Dialog 后端。

[InputDialog](../../../../coui/src/main/java/com/itos/coui/dialog/InputDialog.kt)保留原 TextFieldValue 重载，新增宿主持有 TextFieldState 与 InputTransformation 的同名重载。它们共享真实的窗口、正文、输入行、占位提示、计数、清除和焦点实现，仅 BasicTextField 的值契约不同，不新增第二个输入框组件。新方式由 Foundation 编辑事务提供实际替换范围，不以字符串差异恢复已经丢失的编辑命令。

[重命名示例](../../../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)使用新重载。转换先快照 ChangeList 的不重叠插入区间，计算未替换文本的字节配额，再按文本顺序分配插入配额，最后逆序替换，避免后续区间偏移。禁用字符令对应插入片段为空，保留实际删除的选区；长度按原 UTF-16 单元 1/2/3 计算，不切断代理对。原厂节流提示、初始保存禁用、取消草稿、计数与本地保存保留。程序性清除不经过转换，宿主通过 snapshotFlow 观察编辑结果。

[DialogButton](../../../../coui/src/main/java/com/itos/coui/dialog/CouiDialogButton.kt)直接读取 CouiDialogDefaults 的按钮高度和横向留白，删除重复 minHeight / horizontalPadding 常量。测量与绘制共享同一个参数来源，不增加通用尺寸抽象。

## 依据与路径

原厂基准沿用 Settings 17.0.0 / SHA-256 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。PhoneNameSettingsActivity.Utf8ByteLengthFilter 的实际 source/dstart/dend 语义需要保留编辑区间；全选 abc 粘贴 ab★、全选 xyz 粘贴 ab★、全选 abc 粘贴 ★bc 都是整个原选区被替换，不能保留猜出的公共部分。

核对当前 Compose DialogWrapper 参数更新的 softInputMode 重置，以及 Foundation TextFieldBuffer.replace → onTextWillChange / ChangeTracker 和 InputTransformation 的用户输入事务。公共 replace 记录替换范围；setTextIfChanged 的前后缀优化是另一条用途，不能把它复制成输入过滤算法。转换中的替换由 Foundation 调整选区/组合状态，不再手写字符串偏移映射。

多处编辑按 Foundation 合并后的非重叠事务范围与文本顺序处理，不宣称保留原生多个独立 InputFilter 调用的全部时间顺序。截断/拒绝时采用公共 TextFieldBuffer 的组合区提交规则，真实中文 IME 仍需设备验收；未调用内部 editAsUser 或 OEM 输入 API。

## Existing-note audit

- [输入预设](../feature/2026-10-27-coui-input-dialog-preset.md)：部分取代并互链，旧窗口写入位置及字符串范围推断被修正，预设复用和业务 owner 决定保留。
- [普通弹窗](../feature/2026-10-24-coui-alert-dialog.md)：部分重叠，窗口后端保持唯一；按钮尺寸直接复用现有 Defaults，不变更材质、形状或取消链。
- [公开 API 边界](../architecture/2026-10-23-public-api-boundary.md)：沿用，新增重载对应真实编辑事务需求，旧调用不迁移；没有新增公开输入布局、窗口或诊断对象。

没有同范围 proposed/rejected 待清理；上游刚合入的文档精简保持，以 PR 最新 e3e2085 为修复比较基线。

## Alternatives considered

仅把窗口 SideEffect 移入另一个内容函数仍然受子组合跳过和更新顺序影响，因此由外层窗口 owner 在 Compose 更新后执行。按旧 selection 加前后缀限制能修部分粘贴，却不能恢复所有 IME/无障碍编辑命令；新增同名状态重载承载真实事务，保留原值回调用法。直接替换旧公开 API 更省声明，但会破坏现有调用。复制两套输入行方便接不同编辑器，却使焦点、材质和间距分叉，因此只抽取实际共用的内部布局。

## Consequences

键盘策略不再依赖内容是否重组，Demo 的整段拒绝与超限过滤拥有真实范围，按钮尺寸只维护一处。代价是同一预设维护两种公开编辑状态契约，宿主需明确程序性 state.edit 不经输入转换。新使用说明见[弹窗](../../../../docs/components/dialog.md#输入型弹窗)及 [Demo](../../../../docs/guide/demo.md)；静态调用顺序与构建不能代替真实窗口、粘贴和 IME 验收。

## Verification

已回读审查对应入口、依赖实现和原厂语义，并检查窗口参数更新、首次发布与输入内容跳过重组的调用顺序。最终代码的 :demo:assembleRelease 通过，85 个任务中执行 24 个、复用 61 个，含 R8、资源优化及随构建执行的 vital lint；APK 签名校验通过，未设置 debuggable。源码时间和哈希与本次产物核对一致。

差异空白、Markdown 相对文件链接、UTF-8 无 BOM，以及 Note 目录、格式和归档检查通过。没有额外运行 JVM/设备测试、Debug、Sync、Preview 或库发行；中文 IME、properties 单独变化、真实全选粘贴和键盘调度仍待设备验收，没有将手工推导写成测试通过。APK 与 mapping 仅保存在本机审查目录，不覆盖完整重构 Demo，也未写入 GitHub。
