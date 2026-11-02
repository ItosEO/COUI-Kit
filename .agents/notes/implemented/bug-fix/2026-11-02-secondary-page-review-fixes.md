# Agent Note: 二级页输入交接与共享效果复核

Status: implemented

## Problem

二级页把既有滚动物理和浮动效果接入新容器后，部分调用没有保留完整的状态交接：高速反向帧任务可在新触摸后继续写位置，短页绕开父级嵌套滚动，惯性触边丢失当帧剩余位移。共享物理内核的静态摩擦还让一个组件的动画等级影响其他组件。修复需要沿现有算法和 Compose 分发边界完成，不能增加第二套滚动器或业务状态。

## Decision

长、短页共用 Foundation 的触摸阈值、速度跟踪、多指和 nested scroll 协议。短页保留 verticalScroll 的原始测量与 ScrollState，使用不移动内容的 scrollable state 接收输入；不伪造滚动范围，不手写另一条拖动链。effect 的可见位移仍由唯一外层节点持有。

applyToFling 的结构化协程和回弹帧任务都归 effect 的现有停止入口。DOWN、移除或新滚动先撤销旧任务的 owner，再取消任务；旧 finally 不能重启已被接管的运动。正常结束路径始终调用一次宿主 performFling；边缘吸收纵向速度时传零纵向速度，让父链完成停止处理，协程取消继续传播。

couiVerticalScroll 的惯性触边在同一帧保留父链未消费的整数位移，按 COUIPhysicalAnimationUtil 映射可见位置，再携带该帧浮点速度直接进入原 RK4 回弹。边缘已接管的速度不再交给父级重复消费；父级停止回调挂起不阻塞子回弹。不会从零重启自由 fling 或重复预测。普通越界释放仅高速同向保留初速度，高速反向先回到边界；直接释放与惯性触边保留不同的可见映射和积分入口。反向拖动使用候选 COUIRecyclerView 纵向分支实际读取的 scrollX；纵向入口该值为零，保留原整数运算。跨边界余量交还 Compose 父链是既有公开平台适配。

摩擦由每个物理轴的 animationLevel 独立确定，不再读写跨实例变量。原厂运行在单一系统动画等级，本库允许多个组件各自传入等级；将全局构造历史带入库会改变无关纵向列表。高等级使用初始摩擦，低等级使用原厂初始化后的摩擦。这项局部配置适配替代此前保留首个低等级实例构造副作用的决定，不声称复现原厂进程中首个 X 轴的历史轨迹。积分公式、预测、速度分段、横向 scrollView 临时位置和张力保持。

## Evidence

初始二级页 Note 指定 Settings 17.0.0 / 17000000，APK SHA-256 为 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。本次可复查的同版本导出来自 SHA-256 为 1bc5201297662091d318b0e2ea074610d311f65a7ff9be5d24b29104d7d75678 的候选；未取得前者完整输入，不能将两个哈希等同。

COUIPreferenceFragment.onCreateRecyclerView 确认列表入口。候选 COUIRecyclerView.ViewFlinger.run 先 overScrollBy 本帧 residual，再把当前速度交给 notifyVerticalEdgeReached；COUIRecyclerView.scrollByInternal 的纵向反向分支读取 getScrollX，已用 smali 交叉核对。UP 对超过阈值的纵向速度先调用 setCurrVelocityY，仅反向另启 fling，同向直接 springBack。COUIPhysicalAnimationUtil 决定整数阻尼与 overfling 映射，SpringOverScroller.ReboundOverScroller 决定触边回弹的当前位置、临时位置和速度。Foundation OverscrollEffect 的 performScroll/performFling 契约由其公开接口持有。

惯性越界中 DOWN 后没有 MOVE 的取消另行保留原分支：dispatchTouchEvent 只有真实 UP 将 scrollType 3 切到 2，CANCEL 的 performSpringBack 使用当前可见位置但继续按 type 3 映射。实际 MOVE 进入拖动后改为 type 2。位置来源和显示映射分别传给同一个回弹入口，不能把这些退出都合并成普通松手。

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

## Consequences

实现继续复用同一 RK4 内核和 Foundation 输入；新增的任务引用只表达已有异步任务的生命周期，不公开状态机。短页的额外 scrollable state 只承载输入，不保存业务位置。原厂全局构造历史、私有系统属性和完整 RecyclerView 多轴协议不在公开承载范围内。

标准 OverscrollEffect 不能从 performScroll 获得当帧速度；独立接入其他正常惯性实现时，只能在其 performFling 返回后使用剩余速度。同帧位置与速度交接由库内 FlingBehavior 与 effect 的最小内部连接提供，未扩大公共 API。

每轮通过既有 Note、Lint、Demo debug/release 与两个库的本地 Maven 产物 CI 后继续复核。具体提交和实际检查结果由 PR 持有；源码、数学断言和构建不能代替目标原包与设备的动画中段、IME、RTL、多指、无障碍和 GPU 对照。
