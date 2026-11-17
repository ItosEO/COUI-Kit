# Agent Note: 消息输入栏的操作与状态所有权

Status: implemented

## Problem

消息输入栏需要组合文本编辑和可独立显隐的操作。宿主使用的图标、提交规则和用户设置各不相同，操作变化还需要保持编辑焦点、选区和输入法组合区连续。

## Decision

[MessageInputBar](../../../../coui/src/main/java/com/itos/coui/messageinput/MessageInputBar.kt) 持有输入布局、按钮交互和显隐转场。宿主持有 TextFieldState，并通过 [CouiMessageInputAction](../../../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputAction.kt) 配置前置、尾部和主操作的图标、本地化说明、启用状态与点击事件。提示文案也由宿主提供，省略时为空白。

文本编辑复用 Foundation 的状态式输入接口，同一编辑节点贯穿按钮显隐。图标淡化、缩放、尾部操作位移和文本留白共同完成转场；隐藏立即结束按钮交互与操作语义，移除操作配置时保留退场图标，收尾后释放所保留的配置。

材质、背景采样、轮廓与投影复用公共单主体浮动表面，配置归 [CouiMessageInputBarDefaults](../../../../coui/src/main/java/com/itos/coui/messageinput/CouiMessageInputBarDefaults.kt)。每个输入栏独立持有 renderer 和内容层，宿主负责创建失败的提示以及系统栏、IME 与背景源装配。

[MessageInputBarSurface](../../../../coui/src/main/java/com/itos/coui/messageinput/MessageInputBarSurface.kt) 持有底部全宽遮罩及导航栏补色。遮罩属于页面底部区域，不能裁剪在输入框胶囊内。其录制节点与输入栏分离，并通过现有 `CouiBackdrop.parent` 叠加正文源，使输入栏材质采样包含遮罩而不形成自采样。Surface 消费剩余导航栏占位，宿主继续持有页面的 IME 避让。

页面通过 [rememberCouiImeInsets](../../../../coui/src/main/java/com/itos/coui/layout/CouiImeInsets.kt) 在测量阶段读取当前键盘高度。失焦时仍读取 IME 并允许占位随收起动画减小至零，避免切换小窗后残留键盘空间；失焦期间不增加占位，防止其他窗口的键盘抬高宿主页。重新获焦后跟随当前 IME 高度。统一缩小页面视口后，现有底栏和底部对齐内容跟随可用底边。Demo 在底栏编辑且正文已到末端时，通过既有 ScrollState 随视口变化保持末端可见，动画时序沿用系统 IME。

输入栏与 IME 占位共用库内窗口配置，将所属窗口的调整位设为 adjustResize。库内 WeakHashMap 按窗口保存使用计数、原调整位与原 isScrollContainer 标记，记录仅在 Compose 副作用的主线程生命周期中访问，使同页输入栏和占位的移除顺序保持一致结果。使用期间将根 View 声明为可随 IME 缩小的容器。最后一个使用者退出时恢复原标记和调整位并删除记录，同时保留宿主当前的其他 softInputMode 位。窗口通过 Compose Dialog 的窗口提供者或 Activity 上下文取得，获取失败时明确报错。

ViewRootImpl 在首次 measureHierarchy 之后，根据建窗时保存的 mSoftInputMode 和可见滚动容器重新选择调整模式。初始模式为 UNSPECIFIED 时，Compose 首次测量期间设置的 adjustResize 仍可能被自动选择的 pan 覆盖。根 View 的容器标记使原生自动选择与库的运行时配置一致。入口见 [ViewRootImpl.performTraversals](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/view/ViewRootImpl.java) 和 [View.setScrollContainer](https://developer.android.com/reference/android/view/View#setScrollContainer(boolean))。

演示图标由 CouiIcons 统一提供。Demo 用 Outlined.Camera、Outlined.AddCircle 和 Outlined.Send 演示三个操作，业务回调与图标选择归 Demo。IME 提交和主操作连接到同一个宿主回调，统一处理业务校验与草稿更新。

Demo 将用户设置与即时显隐条件分别保存。三个开关复用现有持久偏好，按钮可见性由启用设置与草稿条件共同决定。这样输入、清空、重新进入页面和进程重建都延续用户的关闭选择，重新启用由开关操作触发。

## Alternatives considered

- 在输入栏中固定相机、附件和发送可缩短示例调用。通用宿主需要自行定义操作，采用统一操作配置，示例业务由 Demo 装配。
- 在页面中随草稿重建显示开关便于演示自动切换。用户的关闭选择需要跨页面和进程保留，因此启用设置进入 Demo 已有偏好存储，草稿条件只参与可见性计算。

## Consequences

宿主可以复用同一输入栏组织不同操作，并保持草稿与业务提交的单一所有者。代价是宿主需要提供图标、操作说明、提交校验、背景源和安全区装配。公开接入见[消息输入栏](../../../../docs/components/message-input-bar.md)，宿主示例见 [Demo](../../../../demo/src/main/java/com/itos/coui/demo/DemoMessageInputSample.kt)。

浮动表面沿用 API 34 要求。静态检查覆盖引用、配置与状态分支，动画中段、IME、RTL、TalkBack 和 GPU 输出仍需设备验收。
