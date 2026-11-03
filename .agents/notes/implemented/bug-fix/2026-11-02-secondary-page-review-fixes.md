# Agent Note: 二级页输入交接与共享效果

Status: implemented

按内容长短切换输入节点的方案由[输入连续性](2026-11-03-page-input-continuity.md)取代；早期页面容器由[宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)与[可选轻量容器](../architecture/2026-11-03-optional-coui-scaffold.md)接管。本文保留输入、物理和绘制的决定及证据；已替代部分单独说明。

## Problem

二级页将滚动物理与浮动效果组合后，出现了跨组件交接问题：高速反向任务可能在新触摸后继续写位置，短页绕过父级嵌套滚动，惯性触边丢失当帧剩余位移。共享内核的静态摩擦还使一个组件的配置影响其他实例。

搜索的绘制、输入和语义范围需要使用同一几何；焦点释放、配置变化和共享文字计算也需要明确各自的所有者。修复应沿已有算法和 Foundation 分发完成。

## Decision

### 滚动任务与触边交接

长、短和空内容使用 Foundation 的触摸阈值、速度跟踪、多指与 nested scroll 分发。实际输入节点、测量节点和交互源的分工由[输入连续性](2026-11-03-page-input-continuity.md)持有。回弹位移只由外层 effect 节点管理。

`applyToFling` 的子协程和回弹帧任务都归 effect 的停止入口。DOWN、移除或新滚动先撤销旧任务的所有权，再取消任务，防止旧 `finally` 重启运动。正常结束调用一次宿主 `performFling`；边缘吸收纵向速度时传零纵向速度，让父链完成停止处理。协程取消继续传播。

`couiVerticalScroll` 在触边帧保留父链未消费的整数位移，按 COUIPhysicalAnimationUtil 映射可见位置，再把该帧浮点速度交给原 RK4 回弹。父级停止回调挂起不阻塞边缘回弹；已被边缘吸收的速度不重复交给父级。

直接越界释放与惯性触边使用不同的显示映射和积分入口。高速同向释放保留初速度，高速反向先回到边界。纵向反向阻尼按候选 COUIRecyclerView 实际读取的 `scrollX` 计算，本纵向入口该值为零；跨边界余量交回 Compose 父链属于公开平台适配。

### 实例参数与尺寸

每个物理轴根据自己的 `animationLevel` 选择摩擦：低于 3 使用 0.2，其余使用原厂初始化后的 0.24。原厂在进程内使用同一系统等级，本库允许不同组件独立配置，因此不保留首个低等级实例改变其他实例摩擦的构造副作用。该决定部分替代 [ChipTab](../feature/2026-10-24-chip-tab.md) 的旧选择；积分公式、预测、速度分段、张力和 scrollView 临时位置语义不变。

拖动阻尼距离固定于实例创建；惯性越界映射高度在真实容器尺寸变化时重读 DisplayMetrics。节点通过 `onRemeasured` 更新后者，测量本身不修改参数；尺寸变化不重建 effect，也不清除积分或速度。

触觉观察只跟踪点击判定所需的活动指针。Settings 入口禁用新增手指切换，因此首次 DOWN 和活动指针 UP 更新整数位置基准，换指不重置首次 DOWN 的计时。实际拖动与速度仍由 Foundation 管理。

### 搜索输入与绘制

搜索按实际输入节点的焦点状态释放自身输入。取消先隐藏键盘、清理自身焦点，再请求宿主关闭；IME Search 先隐藏键盘再执行业务回调，避免影响回调建立的新输入会话。宿主拒绝关闭时保留展开与查询，不在失焦后立即抢回焦点。新旧搜索只在 `requestFocus()` 成功后显示 IME。

搜索和取消的真实输入节点与浮动后端共用位移、缩放、整数宽度和层序。内容在实际变换内录制，由后端绘制一次。取消位于搜索下方；API 31–33 的普通填色分支也使用同一几何。`fallbackBackground` 只控制低平台填色，API 34+ 使用已选的模糊材质。

图标、输入与清除操作通过满幅 BasicTextField 的 `decorationBox` 排列，使胶囊留白也属于输入的触摸和语义范围。清除保留独立操作。固定输入重定位边界与既有顶栏搜索共用。

四条动画通道直接使用 CouiSpring。状态创建位置持有其生命周期；仅移除搜索节点时，保留的 state 仍可供标题与结果读取，输入和图层随节点释放。宽度与取消通道有运行中重定向依据；页面与结果的 OEM setter 语义尚未闭合，保留连续重定向适配。

### 折叠、文字与共享效果

折叠顶栏用 Box 承载空搜索槽及多个根节点，以标准 padding 消费剩余状态栏 Insets，标题缩放原点与 `placeRelative` 使用同一逻辑起始边。密度改变时取消旧像素动画，将可见、逻辑与正文位置同步到已接受目标的新比例，并保留搜索前恢复比例。

通用顶栏保留项目的 12dp 默认模糊，Settings 的 30px 由所属 Defaults 在消费处换算。标题颜色按实际 dark 解析；显式颜色 alpha 的当前行为由[布局 API 与颜色覆盖](../simplification/2026-11-03-layout-item-api.md)持有。LazyColumn 使用补偿后的 contentPadding，Insets 仍消费宿主原始 Padding。

Preference 标题和摘要默认按逻辑起始边对齐，显式对齐优先。Settings 与 Dialog 只共享内部 TextView 度量/行距和字号取整两段计算；角色及字体来源仍归各组件。Dialog 的异步字体状态同时驱动样式与按钮测量。公开文字角色的收敛见[布局 API](../simplification/2026-11-03-layout-item-api.md)。

关闭 `pressFeedback` 时移除按压观察和 mask 生命周期，hover/focus 继续生效。行的外部高度使用标准 Modifier，内部 `defaultMinSize` 提供默认下限。Group 保留按标题选择的默认顶部留白；外部 padding 只能累加，完全自定结构由宿主组合。

浮动表面的显式顺序只作用于请求该顺序的双主体；单主体和三主体保留既有 Z 排序。若三主体 elevation 为 5.05、5.0、5.09，原组内顺序为 1、0、2；不能用搜索的隐式下标顺序将其改成 0、1、2。

## Evidence

### 来源

| 材料 | Settings 17.0.0 / versionCode 17000000 的 APK SHA-256 |
| --- | --- |
| 初始实现记录的目标包 | d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5 |
| 可复核的候选导出 | 1bc5201297662091d318b0e2ea074610d311f65a7ff9be5d24b29104d7d75678 |

目标包的完整输入未取得，两个哈希不能视为同一实现。[二级页组件记录](../feature/2026-11-01-secondary-page-primitives.md)持有初始消费者与已批准特化。

AndroidX 依据为 Foundation/UI 1.12.0 与 1.13.0-alpha02 的官方 source jar。工程声明 Foundation/runtime 1.12.0，compose-bom-alpha:2026.08.01 约束相关模块为 1.13.0-alpha02；这是源码与 BOM 核对，不是 dependencyInsight 或运行时二进制验证。

### 原厂调用与反例

- `COUIPreferenceFragment.onCreateRecyclerView` 确认列表入口。`COUIRecyclerView.ViewFlinger.run` 先 `overScrollBy` 本帧 residual，再将当前速度交给 `notifyVerticalEdgeReached`；`scrollByInternal` 的纵向反向分支读取 `getScrollX`，Java 与 smali 一致。
- UP 在速度超过阈值时调用 `setCurrVelocityY`，反向另启 fling，同向直接 springBack。`COUIPhysicalAnimationUtil` 决定整数阻尼与 overfling 映射，`ReboundOverScroller` 持有当前、临时位置与速度；中断的临时积分规则见[分类栏滚动中断](2026-10-24-chip-scroll-interruption.md)。
- 惯性越界中 DOWN 后没有 MOVE 时，只有真实 UP 将 scrollType 3 改为 2；CANCEL 的 springBack 使用当前可见位置并保留 type 3 映射。进入拖动的 MOVE 才切为 type 2。
- `setEnablePointerDownAction(false)` 使新增手指不改变点击基准。A 在 100、B 在 500，抬 A 后 B 原地 UP，应按零位移排除短点按触觉；A 从 100 到 130、B 短暂按下抬起，则保留 A 的 30px 移动。
- `initProperty` 固定 mOverscrollDistance，`onSizeChanged` 更新 mScreenHeight。raw position 为 500，映射高度由 2000 变为 1000 时，可见位移由 -415 变为 -299；拖动距离仍为 2000，delta=100、位移=300 时阻尼增量保持 34。尺寸更新不应由 intrinsic/lookahead 试测触发，同实例 detach 也不额外重置高度门控。
- `COUISearchBar.changeStateWithAnimation` 在运行中修改宽度与取消目标。`HomepageImpl.animToEdit/animToNormal` 对页面和结果先调用 `setStartValue`，Java 与 smali 均确认该调用，但所用 OEM 动画类的 setter 和默认最小变化量实现缺失。
- `COUIViewOffsetHelper` 与 `COUICoordinatorScrollingViewAnimator` 分别持有逻辑目标、可见整数位置和正文位移。候选 `SettingsCollapsableAppBarLayout.scroll(type=1)` 使用独立跟随参数，但父 Behavior 传参与资源提供者缺失，不能直接映射为 Compose 非触摸滚动。
- 搜索宽度 300、取消宽度 64、间隔 12、进度 0.5 时，主体范围为 0..262。若字段语义仅覆盖 40..258，下层取消覆盖 254..302，则 x=260 可穿透到取消。满幅字段与 decorationBox 使真实输入覆盖这段留白。
- `FocusRequester` 返回失败后，键盘 controller 仍可操作当前兄弟输入会话，因此 show 必须由申请成功条件约束。

### 已替代的容器与输入装配

早期短页使用额外、不移动内容的 scrollable state，长页沿用 verticalScroll。该选择避免伪造范围和手写手势，但范围在一次拖动中跨零会更换输入节点，因此由[输入连续性](2026-11-03-page-input-continuity.md)替代。

早期 Scaffold 的稳定 PaddingValues 与正文子组合模式改编自 Miuix 固定提交 `bb7bde3109605b4c98065b53030ba429c39baea9`，当时保留版权及 Apache-2.0 来源。该派生实现已移除；当前容器见[可选轻量容器](../architecture/2026-11-03-optional-coui-scaffold.md)。稳定对象用于避免已知的 Padding 与 lambda 身份变化，不能据此推断重组或性能收益。

## Alternatives considered

- 手写短页拖动可以绕开零范围门控，但需重复速度、多指、取消和父级分发，采用 Foundation scrollable。
- 触边只传剩余速度更容易接入，但丢失当帧位移并重启自由 fling，采用位置与速度直接交接。
- 静态摩擦可保留原厂首次构造副作用，但破坏库中独立实例的配置语义，改为每轴参数。
- 不可见命中层或语义遮挡器可阻止穿透，但重复维护几何或增加点击所有者，采用真实节点变换与 BasicTextField decorationBox。
- 公共 Typography 合并全部角色可集中参数，但扩大组件耦合；继续复制则保留重复算法，只共享两段纯计算。
- 尺寸变化重建 effect 可以刷新参数，但混淆固定拖动距离与动态映射高度，也中断运动，采用节点尺寸回调更新所需字段。

## Consequences

实现复用同一 RK4 内核、Foundation 输入和现有[节点资源规则](../architecture/2026-11-02-modifier-contracts-and-picker-layout.md)。代价是要维护任务所有权、两种越界入口及少量内部 FlingBehavior/effect 连接。原厂全局构造历史、私有系统属性、异常指针索引调整和完整多轴协议不在公开承载范围内。

标准 OverscrollEffect 不提供每帧速度，独立接入其他惯性实现时只能在其 `performFling` 返回后取得剩余速度；同帧交接由 `couiVerticalScroll` 提供。

正在运行的旧任务取消与尚未执行的 release 排队是不同问题。Foundation 的 launch 和 AndroidUiDispatcher 队列不能仅靠源码证明所有批次输入的 Looper 顺序；快速 UP 后立即 DOWN 仍需设备回放。未添加会阻断父接管 CANCEL 收尾的 touched 守卫，也不以猜测状态机补齐缺失 OEM 实现。

检查结果由 PR 按提交记录。源码、数学反例和 CI 不能替代目标原包及设备上的动画中段、IME、RTL、多指、无障碍和 GPU 对照。
