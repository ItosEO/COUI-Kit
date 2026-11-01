# Agent Note: 受控滚轮与月历选择器

Status: implemented

## Problem

主线缺少数值滚轮和月历。时间、倒计时、日期与弹窗需要组合相同的受控内核，避免将闹钟业务和整套 Demo 重构带入组件。

## Decision

公开 CouiWheelPicker 与 CouiCalendarPicker，组合预设和草稿留在 Demo。滚轮使用两路 Scroller、整数位移和原厂像素取整；跨项提交当前候选值。CANCEL 只吸附，不追加点击；禁用、移除停止动画；宿主拒绝候选时稳定后回到受控值。

月历用 Calendar 的头部、星期条、月网格和三列日期编辑器。翻月只浏览；点日期与编辑滚轮立即提交。日期范围、月长与外部更新统一夹紧。按本次用户要求，倒三角始终朝下，不旋转；编辑区保留淡入淡出及展开延迟。这是明确特化：原厂通用 delegate 的 setCurrentView 调用 startExpandAnimation/startCollapseAnimation，不能将无旋转称为该通用分支的默认行为。

基于上游 2aabbd1 的可选主题策略解析默认颜色；显式配色优先。月历网格、箭头、分隔线、选中内容和展开滚轮共用最终色板，核心不读取 MaterialTheme。时间/日期列、确认草稿与标准 Dialog 由宿主组合，不引入普通按钮、Sheet、厂商二进制或反射对照桥。

## Evidence

- Clock 17.6.94 的 COUINumberPicker、COUITimeLimitPicker、OplusTimePickerCustomClock、COUIDatePicker：双 Scroller、惯性/吸附、文本分区裁剪与像素取整。
- Calendar 17.3.70 / 17003070，APK SHA-256 ba72436c495e581e058878a82a9c5858b061e2bf08105871bc33e9a853354752。COUICalendarPickerDelegate.setCurrentView、COUIDateMonthView、COUICalendarViewPagerScroller 与日期编辑布局确定当前布局、选中动画和分页参数。
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

宿主可独立使用内核并覆盖颜色；所有业务状态与确认提交仍由宿主承担。新增源码需要重新编译，不承诺二进制 ABI。取消收尾、反向、日期范围和主题路径已做源码核对；构建与 Note 检查不代表像素、动画中段、RTL、IME、键盘、TalkBack 或性能验收。
