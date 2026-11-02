# Agent Note: 受控滚轮与月历选择器

Status: implemented

## Problem

主线缺少数值滚轮和月历。时间、倒计时、日期与弹窗需要组合相同的受控内核，避免将闹钟业务和整套 Demo 重构带入组件。

## Decision

公开 CouiWheelPicker 与 CouiCalendarPicker，组合预设和草稿留在 Demo。滚轮使用两路 Scroller、整数位移和原厂像素取整；跨项提交当前候选值。CANCEL 先终止旧帧任务，再吸附已有位移，不追加点击；普通长按 UP 保留已开始的单步，DOWN 先结算剩余位移，再检查新方向。禁用、移除停止动画；宿主拒绝候选时稳定后回到受控值。步长来自真实测量高度，区间基数以 Long 计算，避免每帧遍历 IntRange。

月历用 Calendar 的头部、星期条、月网格和三列日期编辑器。翻月只浏览；点日期与编辑滚轮立即提交。日期范围、月长与外部更新统一夹紧，只有完整自然月/日范围才循环。编辑器不持有长期日期镜像；外部 value 是唯一已接受日期，同帧的列回调先合成最新临时值，再由宿主决定是否接受。标题右侧箭头按 Calendar 17.3.70 的 `coui_calendar_arrow.xml` 状态选择器切换：日历态向下、次级色；滚轮态向上、强调色。该页面 `COUIRotateView` 未设置 `supportRotateType`，默认值为 0，`startExpandAnimation/startCollapseAnimation` 实际切换两种矢量状态，而非 180° 旋转动画。编辑区保留淡入淡出及展开延迟。滚轮返回月历时显式重播选中圆的 280ms 半径/透明度变化，单一动画 owner 只允许一个旧圆退出；普通外部设值不伪造旧圆。整体淡入使用原厂线性 280ms。此前将箭头始终固定向下是与原厂不符的特化，本次恢复 `setCurrentView` 的两态图标切换，不额外添加原厂当前布局中不存在的旋转。

选中日的绘制状态通过一次 Compose 快照写入当前日期、旧日期与两路动画进度，不允许上一轮的旧日期和新一轮的退出透明度混用。旧圆只在相同月份内绘制，150ms 退出完成立即清除；每次新点击使上一动画代次失效，被取消的回调不能回写后续动画。网格点击来源保留至宿主接受或被其他值取代；`PressInteraction.Release` 延迟时不能使新圆的透明度从 176/255 突跳到 0。按压高亮与旧选中圆互斥，对应原厂 `drawDays` 分支。

Calendar 17.3.70 的 `COUIDateMonthView.drawDays` 仅对圆半径/透明度做插值，`mDayPaint` 以固定字号 `drawText`；没有文本缩放。用户反馈的目标表现是新选中数字与圆同步放大，本库仅对入场的新选中数字采用同一 0.8→1 比例与 280ms 进度的缩放。文字不额外做透明度动画，避免在背景圆渐显时产生重复淡化。这是用户要求的视觉特化，**不能宣称其文字绘制严格等同这份原厂源码**；需要同帧设备录像才能进一步确认不同版本/页面是否另有文字变换。普通未选中数字不增加图层，避免整月动画重组。

月历公开接口补充 `enabled: Boolean = true`，放在参数末尾以保留既有位置参数的排列。禁用时禁止上/下月点击、日期标题切换、横向滑页、日期格点击与三列年月日滚轮，仍允许宿主更新受控 `value`；标题和网格半透明显示，滚轮沿用自身禁用绘制，避免父层重复降低 alpha。外部程序值变更与交互禁止是不同的状态来源，不混为一谈。

专用月历的 `previousDescription`、`nextDescription`、`changeMonthDescription`、`yearLabel`、`monthLabel`、`dayLabel` 已由必填改为带库内本地化默认值的可选覆盖。默认英文、`values-zh` 中文跟随 Android 系统语言；保留旧参数以兼容显式文案及其他语言定制。`yearLabel/dayLabel` 在中文格式中仍承担可见后缀，不能简单删除后写死字符串。通用 `CouiWheelPicker` 的 `label` 继续必填，因为它由宿主决定单位语义，不能由底层通用整数滚轮猜测。

基于上游 2aabbd1 的可选主题策略解析默认颜色；显式配色优先。月历网格、箭头、分隔线、选中内容和展开滚轮共用最终色板，核心不读取 MaterialTheme。时间/日期列、确认草稿与标准 Dialog 由宿主组合，不引入普通按钮、Sheet、厂商二进制或反射对照桥。

## Evidence

- Clock 17.6.94 的 COUINumberPicker、COUITimeLimitPicker、OplusTimePickerCustomClock、COUIDatePicker：双 Scroller、惯性/吸附、文本分区裁剪与像素取整。
- Calendar 17.3.70 / 17003070，APK SHA-256 ba72436c495e581e058878a82a9c5858b061e2bf08105871bc33e9a853354752。COUICalendarPickerDelegate.setCurrentView、COUIDateMonthView、COUICalendarViewPagerScroller 与日期编辑布局确定当前布局、选中动画和分页参数。
- Calendar `COUIDateMonthView.configAnimator`：进入使用 280ms COUIMoveEaseInterpolator，退出 150ms COUIEaseInterpolator。内部 Animator 更新圆半径/alpha；`drawDays` 固定 `TextPaint` 绘制日期字符，按压高亮与旧圆的分支互斥。适配记录区分源码事实和用户视觉要求。
- 源材料不进入依赖。公开 HorizontalPager、Compose selectable 与系统 Scroller 承载移植，不宣称 OEM ViewPager 触摸分发、虚拟无障碍节点与字体缩放协议全部等价。

## Existing-note audit

- [原生主题](../architecture/2026-10-30-native-theme-and-component-defaults.md)和[可选 M3 策略](../architecture/2026-10-31-optional-material3-style.md)：部分重叠，沿用消费处解析与显式覆盖，未修改初始化契约。
- [弹窗](2026-10-24-coui-alert-dialog.md)：部分重叠，直接复用已有内容槽、窗口与材质，未新增弹窗后端。
- 活跃 proposed/rejected 无相同选择器决定；本地历史研究保留在完整开发树，不作为本 PR 的共享依赖。

## Alternatives considered

- AndroidView 加载 OEM Picker 能复用原版，但引入厂商运行库、资源和私有协议，不符合公开平台边界。
- LazyList fling/snap 更容易组合，但与已有证据中的双 Scroller、整数取整和惯性距离不同，保留原算法。
- 将时间、日期和标准弹窗均公开为新组件方便调用，但只增加业务组合入口，留作 Demo 示例。

## Consequences

宿主可独立使用内核并覆盖颜色；月历最小调用现在只要求受控日期与更新回调，六个固定文案可省略，仍保留可选覆盖。禁用时不接受用户交互，外部更新正常反映。所有业务状态与确认提交仍由宿主承担。新增源码需要重新编译，不承诺二进制 ABI。修复审查覆盖了受控拒绝、同帧列回调、两路 Scroller 中断、范围截断、翻页目标以及日期圆动画的输入序列。静态源码与来源核对不代表像素、动画中段、RTL、IME、键盘、TalkBack 或性能设备验收；最终 Android Lint/Debug/Release 的状态以本 PR 这次提交后生成的 Checks 为准。
