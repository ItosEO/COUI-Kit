# 自动化测试建设建议

为组件补充自动化测试时，优先覆盖状态、回调、生命周期和无障碍行为。[PR Android 工作流](../../.github/workflows/pr-checks.yml)执行 Lint、Demo 构建与本地 Maven 发行构建；下面的行为测试和 CI 阶段是建设建议。

## 测试层次

| 类型 | 使用方式 | 适用对象 | 不应宣称的结论 |
| --- | --- | --- | --- |
| JVM 单测 | `coui/src/test`，纯函数/状态模型测试 | 数值边界、插值、几何、滚动状态、回调去重等可隔离逻辑 | 不代表真实触摸、Choreographer 或 GPU |
| Compose 设备测试 | `coui/src/androidTest`，`ui-test-junit4`、`createComposeRule` | Switch、Slider、Menu、Dialog、Checkbox、导航语义 | 不代表跨设备 GPU 结果相同 |
| 设备与视觉回归 | 真实 API 31/34/35+ 设备，手动录像/选择性截图断言 | Shader 编译、背景采样、中断动画、窗体及性能 | 单台通过不代表所有 ROM 都支持 |

优先使用 Compose 的语义树查找控件、执行点击/拖动与断言；必要时检查未合并的 semantics tree。对没有自身语义的绘制型 Checkbox，应从正确的宿主 `toggleable` 容器测试，不应为了测试将错误的交互逻辑加到库内。

参考：[官方 Compose Testing](https://developer.android.com/develop/ui/compose/testing)、[Testing APIs](https://developer.android.com/develop/ui/compose/testing/apis)。

## 首批用例建议

| 组件 | 建议覆盖的行为 | 优先级 |
| --- | --- | --- |
| Switch | 点击后回调一次；拖动到另一锚点仅提交一次；CANCEL/父级接管不提交；loading/disabled 拒绝输入；宿主拒绝后回到受控值；RTL | P0 |
| Slider | 0/1 和非零范围边界；step 吸附与可见 Tick 分离；快速反向；禁用与完成回调；键盘调值和语义 | P0 |
| AlertDialog / InputDialog | 返回/外点关闭；焦点与 IME 策略；中文 IME 组合区、选区替换；输入过滤；窗口创建/移除；材质缺失时明确失败 | P0 |
| Menu | 选中/禁用项，关闭回调；长菜单滚动；子菜单交接；快速取消、再次展开；键盘导航 | P1 |
| Checkbox | 外部整行 toggleable 的单一点击 owner、正确 Checkbox 语义；动画中切换与禁用 | P1 |
| BottomBar / ChipTab | 选中项合法性、RTL、长项目列表、惯性/回弹中中断、隐藏附件；资源随节点移除而释放 | P1 |
| Backdrop / Effects | 不采样自身、释放后不再使用；不同 API 下显式失败/降级策略；独立 effect 入口 | P1 |

设备集成测试需要覆盖实际前置条件：`ChipTabRow` 的 display 刷新率、`CouiDialogWindow` 的 Window 和背景源，以及 `CouiFloatingRenderer` 的 Shader 创建和失败处理。

增加默认参数时，检查省略参数与显式传入同一预设的行为一致，并覆盖自定义参数和独立 Effect 用法。测试预期应来自接口契约或独立证据，不复制生产算法作为预期值。

## CI 接入建议

- **阶段 1**：新增依赖、可运行的 JVM 测试，执行 `:coui:testDebugUnitTest`；在 PR Android 检查中启用，失败直接阻断合并。
- **阶段 2**：新增 Android Compose UI 测试；在受控 API 34 模拟器工作流运行，先手动触发或定期运行，收集失败与耗时。
- **阶段 3**：引入 API 31/34/35 设备矩阵、可选择的截图回归和真机 GPU 验收；确认稳定后再让部分 UI 测试成为 PR 必需检查。

给 UI 测试使用 `performTouchInput`、`performKeyInput`、`waitForIdle`、`mainClock` 和明确的 State 断言；避免硬编码 `Thread.sleep` 作为完成条件。对于弹簧、系统窗口动画和 HWUI 帧，由真实设备的时序决定断言范围。

## 测试设计红线

- 不暴露菜单滚动条坐标、动画通道或 renderer 私有状态，仅为测试而扩大公开 API。
- 可以在相同模块 `test` 源集测试必要的 internal 纯逻辑；但不能为测试复制一套生产物理实现。
- 每个失败应留有复现设备、系统/API、Commit SHA、操作序列和必要日志。
- 视觉算法若依据原厂移植，应同时保留独立事实依据、边界示例和实际设备对照；“测试通过”不自动等于 1:1 复刻。
