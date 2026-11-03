# Agent Note: 二级页输入交接与共享效果复核

Status: implemented

## Problem

本篇保留五轮修复的依据与结论。其中关于库内 Scaffold 的稳定 padding 和正文子组合优化已由[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)取代，相关实现及其专用作用域已删除；这些段落仅保留当时的取舍与来源，不再是当前接入要求。其余输入、绘制、资源与物理修复继续有效。

按长短页切换输入节点的装配随后由[页面范围变化时保留输入所有权](2026-11-03-page-input-continuity.md)取代。本文的短页专用输入 state 仅记录旧决定；当前所有范围使用同一 Foundation 输入节点，并通过显式 interactionSource 提供拖动通知。

二级页把既有滚动物理和浮动效果接入新容器后，部分调用没有保留完整的状态交接：高速反向帧任务可在新触摸后继续写位置，短页绕开父级嵌套滚动，惯性触边丢失当帧剩余位移。共享物理内核的静态摩擦还让一个组件的动画等级影响其他组件。独立复核同时发现搜索输入与可见/语义范围脱节、配置变化和公共参数职责不一致。修复需要沿现有算法和 Compose 分发边界完成，不能增加第二套滚动器或业务状态。

## Decision

长、短页共用 Foundation 的触摸阈值、速度跟踪、多指和 nested scroll 协议。短页保留 verticalScroll 的原始测量与 ScrollState，使用不移动内容的 scrollable state 接收输入；不伪造滚动范围，不手写另一条拖动链。effect 的可见位移仍由唯一外层节点持有。

applyToFling 的结构化协程和回弹帧任务都归 effect 的现有停止入口。DOWN、移除或新滚动先撤销旧任务的 owner，再取消任务；旧 finally 不能重启已被接管的运动。正常结束路径始终调用一次宿主 performFling；边缘吸收纵向速度时传零纵向速度，让父链完成停止处理，协程取消继续传播。

couiVerticalScroll 的惯性触边在同一帧保留父链未消费的整数位移，按 COUIPhysicalAnimationUtil 映射可见位置，再携带该帧浮点速度直接进入原 RK4 回弹。边缘已接管的速度不再交给父级重复消费；父级停止回调挂起不阻塞子回弹。不会从零重启自由 fling 或重复预测。普通越界释放仅高速同向保留初速度，高速反向先回到边界；直接释放与惯性触边保留不同的可见映射和积分入口。反向拖动使用候选 COUIRecyclerView 纵向分支实际读取的 scrollX；纵向入口该值为零，保留原整数运算。跨边界余量交还 Compose 父链是既有公开平台适配。

摩擦由每个物理轴的 animationLevel 独立确定，不再读写跨实例变量。原厂运行在单一系统动画等级，本库允许多个组件各自传入等级；将全局构造历史带入库会改变无关纵向列表。animationLevel 小于 3 时取 0.2，其余取原厂初始化后的 0.24。这项局部配置适配替代此前保留首个低等级实例构造副作用的决定，不声称复现原厂进程中首个 X 轴的历史轨迹。积分公式、预测、速度分段、横向 scrollView 临时位置和张力保持。

常驻搜索的焦点所有权来自实际输入节点，只在仍拥有焦点时释放自身输入。取消先隐藏键盘、清理该焦点，再请求宿主关闭；IME Search 先隐藏键盘再执行宿主回调，避免覆盖回调内的新输入。宿主拒绝关闭时保留展开和查询，不因失焦立即抢回输入。

搜索的真实输入节点与浮动后端使用同一位移、缩放、整数宽度和层序，内容录制位于实际变换内部，由浮动后端绘制一次。普通填色分支同样消费几何。背景填色参数明确为 fallbackBackground，仅作用于 API 31–33；API 34 起继续使用已批准的系统模糊材质。旧顶栏搜索直接复用已有固定输入重定位边界。

四条搜索通道复用 CouiSpring，不增加另一套弹簧包装或通道管理器。各自的响应参数及标题、结果消费者保持分离。动画资源归创建 rememberCouiSearchBarState 的组合；仅隐藏搜索节点而保留 state 时仍可供标题和结果消费，输入、图层及局部效果随各自节点释放。

折叠顶栏的搜索槽用现有 Box 承载空内容和多个根节点；状态栏使用标准消费感知 padding，标题的缩放原点与 placeRelative 使用同一逻辑起始边。范围因密度变化时，取消旧像素动画并将可见、逻辑和内容位置同步到已接受目标的新比例，保留搜索前恢复比例；正常拖动与吸附仍有不同的内容跟随职责，不额外建立重测状态机。

通用顶栏继续使用项目已批准的 12dp 默认模糊，Settings 的 30px 放在所属 Defaults 并在消费处换算。Preference 标题和摘要默认逻辑起始对齐，显式对齐优先。列表 pressFeedback 关闭时退出 mask 的组合生命周期并移除按压观察器，hover/focus 不随之关闭。

Settings 与 Dialog 只共享两段内部字体计算：TextView 度量/行距，以及原厂字号适配取整。角色、字体来源和参数继续归各组件，Dialog 的异步字体读取仍参与样式和按钮测量。删除本 PR 无消费者的五个重复 Dialog 文字角色；只有 Message 保存自动对齐所需行数，公共 onLineCount 回调保留。

行的外部高度交给标准 Modifier，内部用 defaultMinSize 补充默认最小高度。分组移除重复的 topSpacing 参数，但保留按标题选择的系统默认留白；外部 padding 只能叠加，不能抵消默认值。完全自定组间距时组合已有布局与 Defaults，不为覆盖默认新增开关或包装 API。

Scaffold 持有一个可观察的 PaddingValues 外壳，在测量顶栏、底栏后一次更新；正文子组合 lambda 保持身份，真实内容、背景或主题变化仍更新。双主体的显式绘制顺序只在有明确参数时应用，单/三主体继续保留已有 Z 排序，不能用搜索的子项顺序覆盖通用绘制行为。

最终交叉复核把搜索装饰归回同一个满幅 BasicTextField 的 decorationBox：原图标、文字和清除布局保持，真实输入拥有整个胶囊的语义与点按，清除仍是独立操作。不能只依靠普通 pointer 节点阻止无障碍语义查询穿透。新旧搜索均仅在申请焦点成功后显示 IME，旧顶栏保留原确认回调时序；未增加历史焦点状态。

折叠标题缺省颜色在消费处按实际 dark 解析，显式 RGB 优先。标题透明度继续归原厂折叠和搜索变换，不改变已观察到的 Paint alpha 覆盖。LazyColumn 装配使用补偿后的 contentPadding，但 inset 消费继续使用 Scaffold 原始 padding。

边缘触觉观察器只跟踪点击判定所需的活动指针。Settings 禁用了新增手指切换入口，因此只有首次 DOWN 与活动指针 UP 更新整数位置基准，换指不重置整次手势计时；拖动和速度仍由 Foundation 持有。拖动距离与惯性映射高度按原厂分开：前者固定于实例创建，后者在真实容器尺寸变化时重读 DisplayMetrics。现有节点通过 onRemeasured 更新映射，measure 保持纯测量；尺寸变化不重建 effect、不清除积分和速度。

## Evidence

初始二级页 Note 指定 Settings 17.0.0 / 17000000，APK SHA-256 为 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。本次可复查的同版本导出来自 SHA-256 为 1bc5201297662091d318b0e2ea074610d311f65a7ff9be5d24b29104d7d75678 的候选；未取得前者完整输入，不能将两个哈希等同。

COUIPreferenceFragment.onCreateRecyclerView 确认列表入口。候选 COUIRecyclerView.ViewFlinger.run 先 overScrollBy 本帧 residual，再把当前速度交给 notifyVerticalEdgeReached；COUIRecyclerView.scrollByInternal 的纵向反向分支读取 getScrollX，已用 smali 交叉核对。UP 对超过阈值的纵向速度先调用 setCurrVelocityY，仅反向另启 fling，同向直接 springBack。COUIPhysicalAnimationUtil 决定整数阻尼与 overfling 映射，SpringOverScroller.ReboundOverScroller 决定触边回弹的当前位置、临时位置和速度。Foundation OverscrollEffect 的 performScroll/performFling 契约由其公开接口持有。

惯性越界中 DOWN 后没有 MOVE 的取消另行保留原分支：dispatchTouchEvent 只有真实 UP 将 scrollType 3 切到 2，CANCEL 的 performSpringBack 使用当前可见位置但继续按 type 3 映射。实际 MOVE 进入拖动后改为 type 2。位置来源和显示映射分别传给同一个回弹入口，不能把这些退出都合并成普通松手。

COUISearchBar.changeStateWithAnimation 的宽度和取消通道在运行中改变目标；HomepageImpl.animToEdit/animToNormal 对页面和结果先调用 setStartValue，Java 与 smali 均有该调用。当前材料缺少所用 OEM 动画类的 setter 与默认最小变化量实现。页面和结果保留共享弹簧的连续重定向适配，不将猜测的 snapTo 当作官方语义，也不宣称四通道的快速反向都已证明等价。

折叠范围重测采用已经接受的逻辑位置，依据是 COUIViewOffsetHelper 与 COUICoordinatorScrollingViewAnimator 分别持有逻辑目标、可见整数位置和内容位移。候选 SettingsCollapsableAppBarLayout 的 scroll(type=1) 确有独立跟随参数，但父 Behavior 的传参及对应资源提供者未闭合，不能把这个类型直接映射成 Compose 的非触摸滚动；当前保持方向跟随路径，不增加无依据参数。

稳定 padding 与正文子组合复用模式改编自固定版本 Miuix Scaffold bb7bde3109605b4c98065b53030ba429c39baea9，并按当前容器职责调整，版权和 Apache-2.0 来源已保留在源码与 NOTICE；AndroidX Scaffold 也采用稳定 padding 外壳。此结构仅消除已知的对象/子组合身份变化，不代替重组计数或性能测量。三主体以 elevation 为 5.05、5.0、5.09 的合法输入时，既有组内顺序是 1、0、2，搜索新增的隐式下标排序会误改为 0、1、2；现在无显式 drawOrder 时直接保留原组内顺序。

候选 COUIPreferenceFragment.onCreateRecyclerView 明确 setEnablePointerDownAction(false)，Java 与 smali 一致；COUIRecyclerView.dispatchTouchEvent 的 POINTER_DOWN 早退，只有 active pointer 的 UP 继续更新点击基准。通用 RecyclerView 默认 POINTER_DOWN 的切换分支不适用于这个 Settings 入口。A 在 100、B 在 500、抬 A 后 B 原地 UP 应按零位移排除短点按触觉；A 从 100 移至 130、B 短暂按下抬起时，必须保留 A 的 30px 移动。

COUIRecyclerView.initProperty 固定 mOverscrollDistance，onSizeChanged 更新 mScreenHeight，type3 的 onOverScrolled 使用后者。raw position 为 500、映射高度由 2000 改为 1000 时，可见位移由 -415 改为 -299；创建时拖动距离仍为 2000，delta 为 100、位移为 300 时阻尼增量保持 34。用真实测量后的 LayoutAwareModifierNode 避免 intrinsic/lookahead 试测修改参数；同实例 detach 不额外重置高度门控，保持候选 attach/detach 路径。

AndroidX 证据来自 Foundation/UI 1.12.0 及 1.13.0-alpha02 的官方 source jar。工程直接声明 Foundation/runtime 1.12.0，compose-bom-alpha:2026.08.01 约束 Foundation/runtime/UI 为 1.13.0-alpha02。关键语义、测量和输入入口已交叉核对，未将其表述为 dependencyInsight 或运行时二进制验证。FocusRequester 返回失败时，键盘 controller 仍可操作当前兄弟会话，因此用既有返回值约束 show。展开搜索旧结构在宽度 300、取消 64、间隔 12、进度 0.5 时，正文范围 0..262、字段语义 40..258、取消范围 254..302，x=260 的空白语义可穿透；满幅字段及标准装饰坐标转换关闭这一缺口。

| 输入与退出 | 当前处理 | 检查边界 |
| --- | --- | --- |
| 高速反向中 DOWN，再次拖动 | 取消 effect 自有 fling 子协程，旧帧不再写位置 | Kotlin 协程所有权和帧调用链 |
| 越界中 DOWN，未达到阈值即 UP/CANCEL | 输入观察节点停止旧运动并恢复必要回弹 | Foundation 分发与 effect 节点收尾 |
| 短页、第二指接替、父容器消费 | 原生 scrollable 负责完整手势与嵌套链 | 不把源码核查当作设备手势验收 |
| 正常惯性首次触边 | 同帧 residual 位置与剩余速度进入边界轴 | Java、smali 与有区分力的公式输入 |
| 不同等级的 Chip 与纵向页面交替创建 | 物理轴参数只取自己的配置 | 不再依赖构造顺序 |

## Existing-note audit

- [二级页装配](../feature/2026-11-01-secondary-page-primitives.md)：部分重叠，继续持有公共组件职责、原始来源和已批准特化；本篇记录输入交接修复与候选证据边界。
- [ChipTab](../feature/2026-10-24-chip-tab.md)：部分重叠，仅替代共享物理内核的全局摩擦构造副作用；分类交互、材质和正常积分规则保持。
- [分类栏滚动中断](2026-10-24-chip-scroll-interruption.md)：继续保留完整取消收尾和不同积分入口的临时位置语义。
- [Modifier 与节点资源](../architecture/2026-11-02-modifier-contracts-and-picker-layout.md)：继续使用独立节点资源，不新建兼容层。
- 活跃 proposed/rejected 中没有本次修复对应的待清理提案。

## Alternatives considered

- 继续手写短页拖动可直接绕开零范围门控，但必须重复处理速度、多指、取消与父级分发；采用现有 Foundation scrollable。
- 触边只传剩余速度无需保留位移，但会丢掉触边帧并重新启动一次自由 fling，与原入口不符；直接接续位置和速度。
- 保留静态摩擦可还原原厂进程首次构造副作用，但库允许不同实例独立配置，跨组件污染更改了公开配置语义；只保留每等级的局部参数。
- 搜索另设不可见命中层或点击遮挡器会重复维护几何与层序；使用真实交互节点的标准 graphicsLayer，与既有浮动绘制消费同一几何。
- 全部排版角色共用一个公共 Typography 或继续复制 Dialog 实现，都会扩大维护面；仅提取已有两处重复的纯计算，保持各组件的主题与角色职责。
- 为搜索 padding 另加无动作语义遮挡或 clickable 会增加第二个交互 owner；使用 BasicTextField 已有 decorationBox，清除操作继续独立。
- 配置变化直接重建 effect 会把原厂固定拖动距离和当前映射高度混为一谈，也中断在途运动；按两个真实消费者保留最小字段，并沿现有节点尺寸回调更新。

## Consequences

实现继续复用同一 RK4 内核和 Foundation 输入；新增的任务引用只表达已有异步任务的生命周期，不公开状态机。短页的额外 scrollable state 只承载输入，不保存业务位置。原厂全局构造历史、私有系统属性和完整 RecyclerView 多轴协议不在公开承载范围内。

标准 OverscrollEffect 不能从 performScroll 获得当帧速度；独立接入其他正常惯性实现时，只能在其 performFling 返回后使用剩余速度。同帧位置与速度交接由库内 FlingBehavior 与 effect 的最小内部连接提供，未扩大公共 API。

已运行的旧任务取消与尚未执行的 release 排队要分开：Foundation 的普通 launch 和 AndroidUiDispatcher 队列不能仅靠源码证明所有批次输入的 Looper 顺序。未添加会阻断父接管 CANCEL 收尾的 touched 守卫；快速 UP 后立刻 DOWN 的排队边界仍须设备回放。原厂异常指针索引调整和缺失的 OEM 动画/绘制类也没有用新映射框架或猜测状态机补齐。

每轮通过既有 Note、Lint、Demo debug/release 与两个库的本地 Maven 产物 CI 后继续复核。具体提交和实际检查结果由 PR 持有；源码、数学断言和构建不能代替目标原包与设备的动画中段、IME、RTL、多指、无障碍和 GPU 对照。
