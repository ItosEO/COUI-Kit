# Agent Note: 开关拖动按压会话与键盘焦点描边

Status: implemented

## Problem

PR #7 审查指出：开关在 Initial 消费拖动 MOVE 后，Foundation toggleable 会取消点击按压 Interaction；旧反馈只观察该 Interaction，导致手指未松开时蒙层已退出。键盘焦点也误用了其他控件的整块 focus 蒙层，遗漏原厂独立外描边。

## Decision

[CouiSwitch](../../../../coui/src/main/java/com/itos/coui/switch/CouiSwitch.kt)的 pointerInput 在 DOWN 开启局部 touchPressed，会话 finally 在 UP、CANCEL、父级接管、输入失效或节点移除时清理。它与 InteractionSource 的点击/键盘 pressed 取并集，只决定按压反馈，不创建第二个 clickable 或业务提交。原有 MOVE 消费、拖动/吸附、受控 CANCEL、VelocityTracker 回收保持；不取消消费来维持蒙层。

[CouiStateFeedback](../../../../coui/src/main/java/com/itos/coui/effect/CouiStateFeedback.kt)的开关分支只合成 hover/press；焦点复用同一蒙层弹簧和 ARGB 插值，按 COUIStrokeDrawable.TYPE_OUTER 将双倍笔宽的 stroke 绘在形状边界，再裁掉轨道内部。焦点绘制位于轨道 clip 之前，轨道和滑块继续使用用户批准的平滑几何。没有复制 Shader、动画后端或改变导航/FAB/工具栏的反馈。

私有状态工厂返回稳定的 State，所有消费者在绘制阶段读取相同进度；焦点路径缓存不捕获每帧 Float，避免每次动画重组都重建平滑路径。既有消费者的颜色、弹簧、裁剪与输入规则不变，没有引入第二套路径缓存或动画控制器。

CouiSwitchColors 增加末尾默认字段 focusOutline；原六项 Kotlin 构造保持可用，宿主可显式覆盖。Defaults.colors 使用目标 Settings 的默认蓝色主题焦点配色；直接构造时默认取宿主 checkedTrack 的透明色。这是单独的焦点配色契约，不从 hover/focus 通用蒙层推断原厂颜色。

## 目标 Settings 证据

基准为 Settings 17.0.0 / SHA-256 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5，亲自读取 COUISwitch、COUIStrokeDrawable、StateEffectAnimator 及对应资源，并非只沿用审查者的录音 App 替代材料。

COUISwitch.handleActionDown 在可拖动判断之前启动 mPressAnimator；handleActionMove 不清除它，handleActionUp / handleActionCancel 才退出。drawBar 仅将 hover 与 press 混到轨道。initStateEffectBackground 另建 COUIStrokeDrawable，以 mSwitchRect / bar_radius 设置边界，放入原背景层，并关闭平台默认焦点高亮。

COUIStrokeDrawable 构造使用 default_focus_stroke_radius 的两倍笔宽，默认 TYPE_OUTER；draw 对路径/圆角轮廓执行 DIFFERENCE 裁剪再画 stroke，因此可见半笔宽位于外侧。资源为 3dp，按 getDimensionPixelOffset 取整后加倍，不能将整条 stroke 裁在开关内部。焦点颜色沿 Theme.COUI.Main / Main.Dark → coui_color_focus_outline_blue / 夜间值，弹簧 response=.3、bounce=0。项目只将其几何映射到已有平滑形状，属于已批准形状适配。

## 输入与退出路径

| 路径 | 本次 owner 与收尾 |
| --- | --- |
| 普通点击或键盘按压 | InteractionSource 继续负责，释放后退出蒙层 |
| 超阈值拖动且继续按住 | 点击 Cancel 不清理 touchPressed，蒙层保持 |
| 拖动后 UP | touchPressed 清理，原吸附和单一提交继续执行 |
| 父级抢占、合成 CANCEL、指针丢失 | finally 清理按压并调用原受控 cancel |
| 禁用、加载、回调移除或节点移除 | 指针节点取消/显式清理，关闭输入，不提交新值 |
| Tab 聚焦与失焦 | 只有外描边动画；禁用绘制门控立即关闭反馈 |

## Existing-note audit

[普通弹簧持久通道](../architecture/2026-10-29-persistent-motion-channels.md)进一步替换共享状态进度承载，保留本篇的触摸会话、焦点外描边和颜色。

[开关组件](../feature/2026-10-27-coui-switch.md)部分重叠并互链，原型、物理、PNG 和宿主职责保持。本篇补 Compose 消费导致的蒙层会话差异与遗漏的原厂焦点消费者。[公开边界](../architecture/2026-10-23-public-api-boundary.md)沿用；只扩展已有配色对象，内部绘制入口不公开。[ChipTab](../feature/2026-10-24-chip-tab.md)保持各自手势，未修改公共点击分发。没有同范围待清理提案。

## Alternatives considered

停止消费拖动事件可保住 toggleable 的 pressed，但会恢复误点击并破坏父子滚动协议。另造独立交互 source 会分散宿主语义与业务 owner；局部触摸标志只补视觉生命周期。给轨道增加普通边框最短，但会留下内侧描边并被轨道 clip 裁剪；采用原厂外侧裁剪语义，并复用项目形状与状态后端。

## Consequences

拖动按住、真正取消和键盘焦点由各自 owner 管理，其他反馈消费者不受影响。代价是一个局部触摸状态与额外配色字段，源码调用保持兼容但没有承诺旧二进制 ABI。使用文档见[开关](../../../../docs/components/switch.md)。默认焦点色对齐提取目标；自定义颜色、平滑形状、跨 ROM 焦点/触觉仍有宿主承载边界。

## Verification

已对照目标 Java、资源和当前 Compose 输入分发入口，逐条回读 DOWN、消费 MOVE、UP、CANCEL、父级接管、禁用、加载和移除路径，以及焦点描边在轨道 clip 外的绘制顺序。最终代码的 :demo:assembleRelease 通过，85 个任务中执行 21 个、复用 64 个，含 R8、资源优化及随构建执行的 vital lint；APK 签名校验通过，未设置 debuggable。源码时间和哈希与本次产物核对一致。

差异空白、Markdown 相对文件链接、UTF-8 无 BOM，以及 Note 目录、格式和归档检查通过。没有额外运行 JVM/设备测试、Debug、Sync、Preview 或库发行；拖动中段、父级抢占和键盘焦点仍待设备验收，构建不等于视觉验收。APK 与 mapping 仅保存在本机审查目录，不覆盖完整重构 Demo，也未写入 GitHub。
