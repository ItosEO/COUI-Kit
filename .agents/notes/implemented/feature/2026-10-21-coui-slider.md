# Agent Note: 字体设置式滑条与物理边界

Status: implemented

## Problem

字体设置式滑条需要整条按压、纹理、跟手、跨档、吸附和反馈。隐藏密集点位不应删除逻辑步进，字体预览节流阈值也不是滑块步长。

## Decision

以 PJZ110 Settings 的 FontSettingsFragment 为基准：字号使用 COUISectionSeekBar，字重使用 COUISeekBar。CouiSlider 移植 COUISeekBarCore/Advanced、SectionAnimationHandler 和 TexturedThumbRenderer 的实际可见分支，逻辑进度与视觉位置分开，不复制无视觉消费者的状态或整个 View 继承树。

steps 保留等分语义，showTickMarks 控制连续/Section 点位及交互，不偷偷改变范围或回调。背景、进度和光效裁剪使用同一宿主 Shape；原厂默认轨道与宿主 G2 分开说明，拇指保持圆形。纹理首次创建后缩放复用，不逐帧软件模糊。

横向拖动与纵向滚动仲裁、多指、RTL、键盘与无障碍属于公共组件。主题、样式切换、暂存、保存和确认属于宿主。跟手光复用公共预设与输入，只在可见时强制所需离屏。

## Alternatives considered

- **只给 M3 Slider 换色。** 成熟输入可复用，但不能表达独立的整条按压、纹理与 Section 位置链。
- **搬入完整厂商 View 与物理库。** 原调用结构完整，却引入无用的主题、日志和引擎依赖。
- **AndroidX FlingAnimation 代替惯性。** 代码少，但指数衰减不同于原厂 World 的离散阻尼。

## Consequences

普通位置弹簧的完整重置与后端复用见[持久运动通道](../architecture/2026-10-29-persistent-motion-channels.md)，不改变本篇惯性和边界的取舍。

连续惯性保留可见范围内的无约束单轴计算，到边界结束；未搬运不可见的越界约束体。需要精确越界效果或边界结束时机时必须重访该决定，不能把现有结束回调当成完整原厂物理等价。反馈优先尝试厂商码，再按已有路径使用系统标准反馈；跨设备马达波形不是保证。

本篇由原宿主的同名 Note 和滑条契约整理迁入，首次决定日期保留。当前入口为 [CouiSlider](../../../../coui/src/main/java/com/itos/coui/slider/CouiSlider.kt)，不依赖原宿主的样式开关、主题和 Capsule。宿主可选择自己的 Shape，但不能将其称为原厂默认。

原工程执行过数学断言、PSI 与接线检查，没有完成本仓库的 Android 构建或真机验收。本次迁入不改变输入、物理或绘制算法。
