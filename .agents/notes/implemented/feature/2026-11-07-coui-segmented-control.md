# Agent Note: 胶囊分段指示器与受控提交

Status: implemented

## Problem

宿主需要胶囊内共享指示器的分段选择。ChipTab 的每项独立材质和横向滚动不能表达这一布局。已有宿主尝试用偏大的文字角色和逐帧布局实现指示器，容器材质依赖业务效果层，无法直接成为通用库组件。

## Decision

[SegmentedControl](../../../../coui/src/main/java/com/itos/coui/segmentbutton/SegmentedControl.kt) 持有布局、指示器和一个输入 owner。标签、选中索引和提交归宿主；默认即时提交，抬手提交是明确的项目选项。延后模式中的取消不提交草稿，即时模式中的已提交选择不回滚。业务未接受选择时，收尾后回到宿主值。

容器复用 CouiFloatingRenderer 的单主体材质、模糊与轮廓；位置、宽度、高度、橡皮筋复用 CouiSpring，按压和形变复用公共 Motion。普通弹簧只补充可选的 AndroidX 积分边界，默认仍无界；指示器的中心范围、增长宽度上限及高度下限进入积分器，不能只把超界结果裁在绘制端。没有复制另一套弹簧或材质 Shader。

指示器以浮点矩形在材质之后、标签之前绘制，运动帧不重新测量标签。指示器自己的径向投影属于该组件的专属原厂绘制，不使用外部浮动投影代替。CAPSULE_10 轮廓复用 CouiContour；跟手光在同一 beforeContent 阶段直接混合到已经绘制的材质和指示器，不写入透明标签层。

输入统一驱动公共 Motion，横拖消费不会误触发本控件自己的“父容器接管”判断。纵向手势、已消费的移动、系统 CANCEL、多指切换、禁用和节点移除释放指示器、标签、容器及橡皮筋；取消阻止新的业务提交。点按非选中项在 UP 请求选择，从当前指示器或其运动中的可见范围起拖才能滑选。标签保留焦点、激活和选中语义；RTL 同时映射几何和命中。

默认字体是 ButtonM；尺寸由预设与父约束共同决定。字体不借用警告按钮角色，实际受约束高度同时进入绘制和命中。宿主若需要特殊字体可覆盖 textStyle，组件不依赖课程表业务包、外部 Backdrop 或本机研究路径。

## 原厂依据与适配边界

目标为 Settings 17.0.0 中的 COUISegmentButtonLayout、SegmentButton / Tiny、COUIMaterialBlurEffect 与 COUIMaterialStrokeEffect。Settings APK SHA-256 为 d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5。位置、宽度、高度通道、动态宽度参数和移动指示器命中入口均交叉核对 smali；素材不进入库的运行依赖。

原厂 calculateChildrenWidths、animateIndicatorToPosition、updateIndicatorRect、handleRubberBandEffect 和 dispatchTouchEvent 共同决定几何及手势，不能仅按圆角截图调参。普通与紧凑模式保留各自位置 bounce 与宽度余量分支，均按实际尺寸绘制。材料选择 CONTENT_SEGMENT_BUTTON 和 CAPSULE_6；指示器使用 CAPSULE_10 并关闭其内晕投影，保留独立渐变投影。

公开 API 34 的浮动后端承担背景采样与 GPU 合成；没有移植 OEM 响应式栅格、多页 ViewPager 回调、特权材质开关或专用振动波形。跨平台触感使用公开 HapticFeedback。动画和材质的真机中间帧、RTL、父滚动接管、TalkBack 及性能仍需按同一原厂版本验收，构建与静态对照不构成 1:1 结论。

## Existing-note audit

- [ChipTab 状态与装配](2026-10-24-chip-tab.md)：相邻但不替代；继续持有独立分类项和滚动决定，本组件只复用其公共后端和直接混光方式。
- [持久运动通道](../architecture/2026-10-29-persistent-motion-channels.md)：部分重叠；复用现有重定向、硬重置和释放语义，本篇只补指示器的有界消费者，不翻转该决定。
- 活跃 Note 中没有同一胶囊分段组件的提案或实现需要归档。

## Alternatives considered

逐帧改变一个子布局的宽高容易复用背景 Modifier，但会持续测量指示器，并把浮点物理量提前取整；采用绘制矩形及稳定标签布局。直接搬入宿主的材质包装可以快速显示，却带入外部 Backdrop 和宿主策略；本库装配已有单主体公共后端。

只在画面上裁剪弹簧超界值能少改公共通道，却让内部位置、速度和反向时间继续沿越界路径积分，因此给公共通道增加默认无界的可选 min/max，而不是新增组件专用物理引擎。

## Consequences

分段选择能在本库和 Demo 中独立使用，宿主持有提交且无需携带业务材质层。代价是每个组件持有独立的四路指示器弹簧及浮动资源，并要求 API 34 和不含自身的背景源。性能、视觉及输入中断仍需真实设备验证；当前不接管外部课程表项目的依赖和代码。
