# Agent Note: 单选框资源与整行选择所有权

Status: implemented

## Problem

Demo 的互斥选择行使用勾图标，只在选中时显示，不能展示单选框的未选中和禁用外观。需要独立的 COUI 单选绘制组件，并保留整行唯一的点击与语义所有者。

## Decision

RadioButton 按 coui_btn_radio 的圆形几何、配色及双向 animated-selector 绘制。它与 Checkbox 一样只消费受控状态，不建立点击和选择业务。宿主在组容器提供 selectableGroup，在行提供 selectable 与 Role.RadioButton，并向指示器同步 enabled。多个选项共享一个选中值，点击当前项不取消选择。

选中外观是强调色外圆与浅色内圆，不替换成 Material 的细环加中心点。描边、填充和内圆分别使用原资源的时间段；切换中反向按另一条不可逆时间线重播，禁用状态直接显示静态资源。初始选中内圆为白色，动画资源内圆为浅灰色，保持两种资源已有差异。

Demo 的公共单选行消费该组件，因此主题、颜色和配色预设等单选项共用同一实现。菜单勾选、复选框和删除确认仍表达各自的语义，不按图标名称批量替换。

## Evidence

来源为 COUI-17 录音原包 soundrecorder.apk 的 Widget.COUI.CompoundButton.RadioButton → android:listChoiceIndicatorSingle → coui_btn_radio。APK SHA-256 为 968c3c17c6cdcc09f9a64e6fd939bfa4382104d7afda6dffb7c2f14ccb587588。静态几何、日夜颜色及 drawable 映射已从原 APK 用 aapt2 复核，动效来自两个方向的原始 animated-vector 与 objectAnimator 资源。

本地未取得设置“显示大小”的目标包，因此这些证据限定为 COUI 同族单选资源，不构成该设置页的逐项对齐证明。原始厂商资源不作为运行依赖。

## Alternatives considered

- 沿用勾图标无需新组件，但不能展示未选中状态，也与复选状态难以区分。
- 直接使用 Material RadioButton 可以复用交互，却改变 COUI 几何和动画，并给核心增加设计体系依赖。
- 在指示器内部处理点击可以独立操作，但与已有整行 selectable 产生第二个点击所有者；保持视觉组件和宿主语义组合。

## Consequences

普通单选项有一致的可用、禁用和切换外观。宿主负责互斥状态、点击范围与无障碍描述。资源端点和时间线可做静态检查，实际动画、快速反向、字体与 TalkBack 仍需设备验收。使用说明见[单/复选框](../../../../docs/components/checkbox.md)。
