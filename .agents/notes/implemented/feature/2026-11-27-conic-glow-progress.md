# Agent Note: 流体云歌曲进度的锥光和跳转

Status: implemented

## Problem

SleepDown 将流体云的锥光应用在朗读进度上，宿主版还叠加了分割线转场、浅色外层染色和旧平台近似光斑。独立进度条需要保留歌曲进度真正使用的绘制次序和状态。只按 SleepDown 的轨道几何移植会把原厂没有的两端外扩带进组件。

## Decision

[锥光进度条](../../../../docs/components/conic-glow-progress-bar.md)对齐 ColorOS 17 PMX110 17.0.0.115 的歌曲卡片。SystemUIPlugin 包 `com.oplus.systemui.plugins` 版本为 17.000.002，APK SHA-256 为 `43fc34f7d4aa9ce1882e454e449d84d610a9454cf36fe8c7c5fc97954b678372`。`media_card_section` 实际装配 `DirectionAwareSeekBar`，`s1.M/N` 分别写入媒体取色和播放位置，播放流将状态设为 PLAYING 或 PAUSED。背景设为明确的轨道色，绕过该 View 的背景模糊路径。

播放时亮边长度为整条的五分之一，暂停拖动时为十分之一。可用长度不足时使用实际已播长度。SystemUIPlugin 的 `Sc` 字节码保留了 JADX 输出遗漏的跳转，Settings 同族 `IntentConicProgressRenderer` 也是同一判定。先绘制固色进度和白光并按轨道圆角裁剪，再在轨道外用 SCREEN 合成锥光。进度几何按端点直接结束，轨道两端无需 SleepDown 的半径外扩。RTL 低进度调制沿用原厂的物理位置，光尾方向随布局镜像。

光形、两束光的透明度、白色亮边各自计时。[CouiSpring](../../../../coui/src/main/java/com/itos/coui/motion/CouiSpring.kt)承载临界阻尼弹簧，切换模式从当前绘制参数重新起算。歌曲卡片的横竖方向仲裁映射到 Compose 输入。横拖第一段先让当前位置追上手指，接着直接跟随。取消横拖或移除组件时释放预览和动画。

点击抬手后，`core.a.onTouchEvent → A6 → Q4` 用响应 0.3 秒、回弹量 0 的像素位置弹簧移动进度头，并保持按压轨道高度。`R4` 在正常结束时把量化位置交给媒体回调，随后退出按压。组件按轨道宽度换算最小可见变化，连续点击从当前动画位置改向，由最新目标提交。横拖接管、键盘或无障碍调节、关闭交互和移除组件均取消待提交的点击动画。

拖动触觉沿 `core.a.G → Lp` 在整数进度变化时分发。原厂线性马达区分步进与端点，View 分支分别使用 305 和 306。组件复用 [SeekBar 触觉适配](../../../../coui/src/main/java/com/itos/coui/input/CouiHaptics.kt)，按设备制造商选择 OPlus 系列反馈码或 Android 公开的轻触觉与端点反馈。[AOSP 的 ViewRootImpl](https://android.googlesource.com/platform/frameworks/base/+/01d329349864f78f7f1b4c774341514e8719eb9c/core/java/android/view/ViewRootImpl.java) 异步请求的返回值只能说明分发成功，设备识别须在提交反馈码之前完成。触觉按千分位输入去重。

进度条的横向拉动沿原厂 `dispatchTouchEvent → Xn → M4 → G4`。`G4` 的进度条分支通过 `View.setTranslationX` 移动整条轨道及光效，轨道的按压高度则由 SeekBar 独立驱动。`COUIIntentSeekBar$1` 把同一位移交由歌曲卡片同步两侧时间文字。组件复用 [CouiTouchMotion](../../../../coui/src/main/java/com/itos/coui/motion/CouiTouchMotion.kt) 的阻尼和平移回弹通道，轨道、进度、白边及锥光作为整体横移。触点换算扣除当前偏移，避免回弹中再次按下造成进度跳变。宿主负责时间文字的布局。

组件容器统一测量命中区与语义区，画布匹配容器的实际尺寸。Compose `Canvas` 底层的 `Spacer` 在单轴仅收到最小尺寸时会以零尺寸绘制，即使父布局已为它预留空间。扩大触摸区域时，居中的轨道仍使用自己的视觉高度。

## Alternatives considered

- 直接搬用 SleepDown 渲染器。该路径已有朗读场景，但浅色 SRC_OVER、轨道端点外扩与径向替代均属于宿主选择。
- 复用字体设置式 [Slider](2026-10-21-coui-slider.md) 的整条绘制。它已经处理滚动仲裁、键盘和触觉，但依赖字体滑条的几何、拇指和跟手运动。锥光需要不同的光场与合成位置。`CouiSpotlight` 的光心来自触点，轮廓光与融合 Shader 也无法表达沿播放位置移动的双束衰减。

## Consequences

进度和播放时钟由调用方持有，可以用只读模式复用 SleepDown 的展示用途。控件要求 Android 13，以公开 Canvas 和 RuntimeShader 绘制，调用方可提供颜色和卡片裁剪。绝对时间和播放器进度更新节流留在宿主。实际触觉强度由 ROM、设备及系统设置决定，专用马达波形和中间帧仍需 ColorOS 真机对照。
