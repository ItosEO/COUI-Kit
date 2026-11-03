# Agent Note: 页面范围变化时保留输入所有权

Status: implemented

## Problem

折叠顶栏会减少滚动内容的顶部 padding。正文只比视口稍高时，同一次拖动就能使 ScrollState.maxValue 从正数变为零。按范围插入短页 scrollable 并禁用原 verticalScroll 会重置 Foundation 的 DragGestureNode，新节点没有原 DOWN，当前拖动因此中断。独立宿主在手势中增减内容也有同一问题。

真实 Demo 另有三个消费者遗漏：搜索只隐藏正文和关闭滚动，目录及预设仍可经键盘操作；新 Slider/TextField 包装器没有把标题关联到实际语义节点；进度到达终点后仍保持运行状态。Divider 直接使用小数 dp 高度，在低密度下取整为零。

## Decision

长、短和空内容使用同一个 Foundation scrollable 输入节点。其 ScrollableState 将滚动会话、消费、进度和方向记录委托给宿主 ScrollState，只保持边界输入的可分发能力。verticalScroll 继续负责标准测量、真实范围、内容放置及可访问范围，关闭其用户输入。不得另起 MutatorMutex、改写 maxValue 或手写触摸/速度协议。

仅由标准 verticalScroll 的 ContentInViewNode 处理焦点和视口变化；外侧输入节点使用零位移 BringIntoViewSpec，避免两个节点争用同一个滚动会话。公开入口末尾提供 Foundation 风格的可选 MutableInteractionSource，拖动交互从此参数观察；ScrollState.interactionSource 只有只读公开接口，不强转为内部可写实现。普通位置和 isScrollInProgress 仍从真实 ScrollState 观察。

Demo 将 search.active 对应的 enabled 传到原正文实际交互控件，覆盖退出动画；清理正文语义只负责可访问树，不代替输入禁用。Slider 和输入框在自身节点关联标题，保留原进度/编辑动作。进度自然完成时退出 running。

Divider 在消费处将默认厚度解析为至少一个物理像素，再用于现有布局和填色，保留外部约束优先。Wallpaper 直接复用 Personalise。图标来源表随既有源码/API 文档归档分发，并沿用现有产物检查。

## Existing-note audit

- [二级页输入修复](2026-11-02-secondary-page-review-fixes.md)：部分取代。仅替代按长短页切换输入节点的装配，其余物理、焦点和绘制修复继续保留。
- [二级页组件](../feature/2026-11-01-secondary-page-primitives.md)：部分重叠，沿用独立组件和已有物理算法，不新增页面容器。
- [设置式 Demo 与系统图标](../feature/2026-11-01-demo-catalog-and-system-icons.md)：部分重叠，补齐实际消费者的输入/标签与来源归档，不改变目录和 Activity 方案。
- [宿主页面所有权](../architecture/2026-11-03-host-owned-page-layout.md)：沿用，页面输入门控仍由 Demo/宿主负责。
- 活跃 proposed/rejected 中没有同范围提案。

## Alternatives considered

- 保留按 maxValue 切换输入节点，代码最短，但真实测量会在一次拖动内翻转分支，不能保留当前手势。
- 在零范围的 nested post 中启动第一次回弹，可保留单一 verticalScroll；但会先于更外层 post 父容器消费首个 delta。提前手动询问父级又会使标准链重复分发 post，因此不采用。
- 让 OverscrollEffect.isInProgress 在零范围始终为 true，可打开内部门控，但会影响立即拖动和触摸阈值，属性也不再表达真实运动。
- 用 rememberScrollableState 加 dispatchRawDelta 驱动 ScrollState，可减少委托代码，但新互斥不能取消宿主对真实 ScrollState 发起的程序滚动。直接委托同一 scroll 会话。
- 强转 ScrollState.interactionSource 可保留原流，但依赖公开接口没有保证的内部实现。提供可选交互源并记录未发布入口的接入差异。

## Verification

核对固定 AndroidX Foundation/UI 1.13.0-alpha02 source jar 的 ScrollableState、ScrollingLogic、ContentInViewNode 和语义合并调用链：canScroll=true 允许分发而不保证消费；所有滚动方法仍直接委托真实状态。范围不再参与输入节点身份或 enabled；没有自定义 nested dispatcher，父 pre/post scroll/fling 继续由 Foundation 分发。外侧零距离 BringIntoViewSpec 将内层算出的目标矩形继续交给祖先，不截断请求。

逐个核对 Demo 目录、预设和搜索结果调用点，确认禁用值到达实际交互 Modifier，搜索结果保留启用；Slider/TextField 的标题语义附着于实际控件，没有清除范围和编辑动作。进度循环正常完成后将 running 置为 false，现有按钮下一次点击先归零再启动。

默认 0.33dp 在 density 0.75、1、1.5 时原来取整为零，现在均为一个物理像素；density 2、3、6 分别保持 1、1、2 像素。候选 Settings 的 COUIRecyclerView 绘制同样使用 Math.max(1, mDividerStrokeWidth)，只用来佐证最小线宽，不能证明目标哈希的全部布局。没有运行本地 Android 构建或设备测试；静态格式、Note 结构检查以及两轮独立 Review、既有 PR CI 的实际结果记录在本轮 PR 评论。

## Consequences

长短页切换不再改变输入节点，标准 ScrollState 继续持有唯一业务位置与滚动互斥。代价是拖动观察改为显式 interactionSource，不能继续从 ScrollState.interactionSource 收到此入口的拖动事件；文档说明这一未发布 API 的边界。外层保留一个输入节点，内层保留 Foundation 测量和焦点节点，未以节点数量推断性能收益。若 Foundation 后续提供零范围 overscroll 的公开开关，应重新评估是否可回到单一 verticalScroll。

来源清单随既有源码和 API 文档归档分发，不代表已经补齐第三方使用许可。

目标 Settings 包与附件候选哈希不同；图标缺少指定原包中的 24 组 XML/选择器/调用材料，不能根据候选补造 night 几何或授权结论。快速 release 排队、IME、硬件键盘、TalkBack、配置重建和 GPU 行为需要设备验证。静态所有权及数学反例不等于完整原厂动态验收。
