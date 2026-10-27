# Agent Note: 输入型弹窗预设与重命名设备示例

Status: implemented

## Problem

普通弹窗虽提供 content slot，使用者仍需自行处理标准输入区的排版、焦点、计数和清除。用户明确要求在首个弹窗 PR 基础上提供专门的输入预设，以设置的设备名称弹窗为依据；只在 Demo 拼一份示例不能满足这个接入需求。

## Decision

在 dialog 包提供 CouiInputDialog 预设，复用 CouiAlertDialog 的窗口、标题、操作按钮、位置、项目材质及取消路径。预设只补输入区，不新增输入框组件、窗口后端或 Shader。TextFieldValue 与 onValueChange 由宿主持有草稿、选区和输入法组合区；计数文案、计数错误色、确认启用及保存回调均受控。清除回调空值并保留编辑焦点，不提交确认。

### 原厂依据与承载

材料沿用 [普通弹窗记录](2026-10-24-coui-alert-dialog.md)中的 Settings 17.0.0 / versionCode 17000000 / SHA-256 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5，缺少设备构建指纹。

实际入口为 PhoneNamePreferenceController.handlePreferenceTreeClick → showAlertDialog，主题 COUIAlertDialog.SingleInput → BottomAssignment → coui_bottom_assignment_alert_dialog_layout，并装配 coui_single_inputview_bottom_alert_dialog_layout 内的 COUIInputView。不是蓝牙页的 BluetoothNameDialogFragment，也不是仅含普通 EditText 的另一个布局。标题和按钮继续使用普通弹窗同一组样式；initContentSpacing / initMessagePadding 对自定义内容使用的正文底间距由预设内部装配。预设通过 content slot 复用内部 DialogText 绘制正文，避免修改普通警告的默认间距。

输入区沿 COUIInputView.instanceCOUIEditText → COUIInputViewStyle → Widget.COUI.InputPreference / Input 追到 couiTextBodyL、line 背景与单行分支；updatePadding 的无标题上下留白、输入最小内容高度、button_layout 及 count/delete 的间距均在 DialogInputDefaults 与内部字体角色维护。输入与计数使用目标资源的字体和度量，不继承宿主 M3 Typography。清除图标按原厂圆与镂空交叉几何重新绘制，不复制 APK 资源文件。

焦点线沿 COUIEditText.updateLineModeBackground → animateToShowBackground → draw：正常线与焦点线分别量化为像素，焦点宽度从物理左侧展开，使用 COUIInEaseInterpolator。失焦的 draw 分支不画焦点线，预设停止并清理对应协程；再次获焦从起点启动。计数达到上限只改变计数颜色，不冒充网络校验后的整个输入框错误动画。清除按钮依 COUIInputView.updateDeleteButton 的获焦、非空分支显示。

原厂通过 setSoftInputMode(5) 与 SettingsUtils.showSoftInput 请求焦点和键盘。预设用 LocalWindowInfo 等待实际窗口获焦，再通过 FocusRequester / SoftwareKeyboardController 请求输入；不使用固定延时。已读当前依赖中 DialogWrapper.updateParameters 的实际字节码：decorFitsSystemWindows=true 设置 softInputMode=0，false 在 API 31+ 设置 ADJUST_NOTHING。因此预设要求 decorFitsSystemWindows=true，并在所属窗口显式设置 ADJUST_RESIZE；autoFocus 开启时附加 ALWAYS_VISIBLE，关闭时保留 STATE_UNCHANGED。不是把默认策略误称为已确认的 resize；内容不再重复叠加 imePadding。窗口退出由原有 Dialog 生命周期释放，未添加全局键盘隐藏或全局焦点清除。

### Demo 的业务策略

DemoDialogInputSample 只消费公开预设，示例名称由 DialogSample 保存。新打开时从已保存名称建立独立草稿，初始保存禁用；编辑后按 trim 非空且不等于字面值 null 启用，与 Controller 的 TextWatcher 分支对应。保存去除首尾空白后更新 Demo；取消、返回或外点丢弃草稿，再进入读取上次已保存值。

名称策略对照 PhoneNameSettingsActivity.Utf8ByteLengthFilter 的 Java 与 smali：计数按 UTF-16 单元累加 1/2/3，不将其写成一般 Unicode UTF-8 的四字节算法。长度和特殊字符范围只属于 Demo；正则以 smali 的真实转义常量核对，避免 JADX 对补充平面范围的错误显示。替换选区时先扣除未被替换部分的配额，超限粘贴只截断插入片段，并映射光标与组合区；不截断代理对。被禁字符使本次插入片段为空，保持 InputFilter 替换语义，提示按原厂节流。该策略不读写系统设备名，不接网络、敏感词审核、运营定制或 OEM 服务；这些业务没有迁入库。

### 输入路径对照

| 操作 | 原厂入口 | 当前承载 |
| --- | --- | --- |
| 打开与再次打开 | showAlertDialog / initialize / setText | Demo 建立草稿，预设等待窗口焦点并请求输入 |
| 编辑、选区、粘贴与中文组合 | Utf8ByteLengthFilter / TextWatcher | 宿主过滤 TextFieldValue 并保留有效选区/组合区 |
| 计数与达到上限 | OnEditTextChangeListener | 宿主计算 counter / counterIsError，预设绘制 |
| 清除与失焦 | initDeleteButton / updateDeleteButton / updateLineModeBackground | 受控空值回调、获焦显示、焦点线协程收尾 |
| 保存 | TextWatcher / verifyDeviceName 的本地提交分支 | Demo 校验后更新示例名称并关闭，不接系统业务 |
| 取消、返回、外点及移除 | 原 AlertDialog / onDialogDismissed | 复用 CouiAlertDialog 的关闭请求，宿主销毁草稿 |
| 按钮 UP / CANCEL / 父接管 | 普通弹窗的公共触摸链 | 保留 PR 中 3b346e7 的滚动取消修复，不复制或覆盖 |

## Alternatives considered

- **只在 Demo 中装配输入区。** 可保持库 API 最少，也符合此前组合示例的边界；这次用户明确指定专门的预设组件，因此提供可直接调用的 CouiInputDialog，业务继续留在 Demo。
- **另外实现完整输入框和另一种输入弹窗窗口。** 更便于独立扩展，却超出一个输入预设的需求，还会使普通弹窗的材质、按钮与取消链分叉；只复用现有后端并补所属预设内容。
- **把 32 上限和设备改名规则固定到预设。** 宿主调用更短，但普通文本输入会被设备名称业务限制；由调用方控制输入值、计数和确认，Demo 展示具体策略。
- **沿用默认窗口模式，或额外加 imePadding。** 改动少，但当前依赖没有明确请求 resize，重复 inset 也可能多留键盘高度；预设持有单一窗口避让策略，并公开说明覆盖属性的要求。

## Consequences

库新增的是普通弹窗的专门预设，不改变现有 CouiAlertDialog 签名、普通间距或共享材质默认行为。宿主能直接调用标准输入弹窗，但仍需正确保存 TextFieldValue、计算校验并处理确认和取消。公开 API 不预设字符上限、语言、设备名或真实系统写入。

输入、计数和下划线需要随 Compose 编辑承载维护；OEM 光标/选区菜单、私有窗口调度、网络错误抖动动画与跨 ROM 字体不因此等价。键盘弹出/收起、横屏和极矮窗口、中文 IME、TalkBack、RTL、焦点反向及 GPU 动画中段仍需设备验收，源码和 Release 构建不能替代验收。

## Verification

最终代码执行 `:demo:assembleRelease --offline --console=plain --no-daemon` 成功：85 项任务，11 项执行、74 项复用，包含 R8、资源优化与 Release vital lint。22 篇 Note 的树/格式/归档检查、差异格式、双语资源及新增内容的 UTF-8 无 BOM 检查通过；73 篇 Markdown 的 481 条本地文件链接可达，排除 Skill 中两个非实际文件的日期占位例子。25 组弹窗资源的键和格式参数一致，特殊字符正则与 smali 常量完全一致。

定向源码复核覆盖初始禁用、编辑后启用、空白/null、选区替换、超限粘贴、组合区和代理对边界、取消再进入、清除、窗口焦点与键盘避让。已确认 3b346e7 的三个触摸修复文件未被修改。没有运行这些交互的原库或设备对照，也没有新增永久测试、Debug 构建或库发行；不把源码复核写成真实 IME/触摸运行结果。

## Existing-note audit

- [普通弹窗](2026-10-24-coui-alert-dialog.md)：部分重叠；窗口、材质、操作区与取消沿用该决定，本篇持有用户新增的专门输入预设与宿主输入边界，双方互链。
- [公开 API 收口](../architecture/2026-10-23-public-api-boundary.md)、[模块边界](../architecture/2026-10-21-coui-module-boundary.md)：继续沿用；没有为 Demo 暴露内部窗口、文字或普通输入框。
- [菜单输入](../bug-fix/2026-10-15-coui-submenu-input-dispatch.md)、[顶栏搜索](../bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)、ChipTab 与其他 Demo 记录：不被本预设取代；本次不改变其输入和合成。

没有同范围 proposed / rejected 需要删除或归档。
