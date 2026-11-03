# Agent Note: 整行开关保留拖动与单次提交

Status: implemented

## Problem

Demo 将开关放入整行 toggleable 后，为避免重复点击，给 CouiSwitch 传入 onCheckedChange=null。onDragCheckedChange 的默认值也因此变成 null，普通开关示例失去拖动。只恢复子控件点击会产生第二个点击与语义 owner；只让滑块交互又不满足整行可点击的要求。

## Decision

DemoSwitchPreference 继续由整行 toggleable 处理点击、键盘和 Switch 语义。子 CouiSwitch 的 onCheckedChange 保持 null，onDragCheckedChange 显式接到同一个业务回调；两者共用 MutableInteractionSource。所有可交互的整行开关使用这一装配，不增加拖动开关参数或另一类行组件。

未达到拖动阈值的 UP 由整行点击提交。达到阈值后，现有 Switch 输入在 Initial 阶段消费移动，整行的点击检测取消；滑块沿已有吸附过程提交候选值。取消、父容器接管、禁用、加载及组件移除沿已有输入和运动生命周期收尾，宿主拒绝候选时恢复实际 checked。业务值仍由宿主持有。

整行点击与滑块拖动分别在自身提交处调用现有公开触觉入口。CouiSwitch 也使用该入口，不再重复维护触觉码和附着检查。加载期间两条输入均禁用；加载示例在空闲时允许点击或拖动发起同一个模拟请求。

## Existing-note audit

- [通用 Item 收敛](../simplification/2026-11-03-layout-item-api.md)：部分取代“尾部不提交拖动”的 Demo 决定；保留 Item 只负责局部布局和可选点击的边界，互链。
- [开关组件](../feature/2026-10-27-coui-switch.md)：复用其现有独立拖动回调、运动和受控取消，只将私有触觉封装改为共享现有入口，互链。
- [目录式 Demo](../feature/2026-11-01-demo-catalog-and-system-icons.md)：继续持有页面、示例和业务状态，链接本篇说明行输入。
- 其余活跃笔记不改变本次输入归属，没有需要删除或归档的同范围提案。

## Alternatives considered

- 让 Item 只布局、滑块独立点击和拖动，可以直接演示基础组件，但会移除用户要求的整行点击。
- 给私有行增加拖动布尔参数，可以区分展示模式，但当前正常行均应支持同一套输入，现有 onDragCheckedChange 已表达职责。
- 保留无拖动装配并修改文案，改动最少，但会丢失原开关示例已有的公开能力。

## Consequences

公开 Switch 签名、吸附算法和 Item API 不变。整行点击与滑块拖动可以并存，业务回调只有一个状态 owner。宿主装配须把两条输入接到同一个受控值，并在加载或禁用时同时关闭；只禁用行而保留滑块输入会破坏这个边界。接入示例见[开关文档](../../../../docs/components/switch.md#整行点击)。

源码核对覆盖阈值前 UP、拖动消费、吸附完成、取消、加载及宿主拒绝。它不能代替设备上的嵌套滚动、RTL、TalkBack、触觉和快速重复操作；实际检查结果由 PR 记录。
