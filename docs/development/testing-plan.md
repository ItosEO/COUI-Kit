# COUI-Kit 自动化测试建设评估（提案）

本文依据 2026-10-30 的 `main` 代码与 CI 设计。当前仓库没有 `coui/src/test` 或 `coui/src/androidTest` 测试源码；[PR Android 工作流](../../.github/workflows/pr-checks.yml)执行 Lint、Demo 构建与本地 Maven 发行构建，不运行自动化行为测试。本文件不是已经执行的测试报告。

## 判断

**应当增加测试，但不建议对所有视觉算法立即补齐截图金图或按像素模仿原厂输出。** COUI 近期变更集中在弹簧重定向、拖动取消、窗口输入、Shader 生命周期。首批最有价值的测试是独立于原厂逆向实现的**状态、回调、生命周期和无障碍不变量**。

## 测试层次

| 类型 | 使用方式 | 适用对象 | 不应宣称的结论 |
| --- | --- | --- | --- |
| JVM 单测 | `coui/src/test`，纯函数/状态模型测试 | 数值边界、插值、几何、滚动状态、回调去重等可隔离逻辑 | 不代表真实触摸、Choreographer 或 GPU |
| Compose 设备测试 | `coui/src/androidTest`，`ui-test-junit4`、`createComposeRule` | Switch、Slider、Menu、Dialog、Checkbox、导航语义 | 不代表跨设备 GPU 结果相同 |
| 设备与视觉回归 | 真实 API 31/34/35+ 设备，手动录像/选择性截图断言 | Shader 编译、背景采样、中断动画、窗体及性能 | 单台通过不代表所有 ROM 都支持 |

优先使用 Compose 的语义树查找控件、执行点击/拖动与断言；必要时检查未合并的 semantics tree。对没有自身语义的绘制型 Checkbox，应从正确的宿主 `toggleable` 容器测试，不应为了测试将错误的交互逻辑加到库内。

参考：[官方 Compose Testing](https://developer.android.com/develop/ui/compose/testing)、[Testing APIs](https://developer.android.com/develop/ui/compose/testing/apis)。

## 首批用例建议

| 组件 | 首批必须覆盖的行为 | 优先级 |
| --- | --- | --- |
| CouiSwitch | 点击后回调一次；拖动到另一锚点仅提交一次；CANCEL/父级接管不提交；loading/disabled 拒绝输入；宿主拒绝后回到受控值；RTL | P0 |
| CouiSlider | 0/1 和非零范围边界；step 吸附与可见 Tick 分离；快速反向；禁用与完成回调；键盘调值和语义 | P0 |
| CouiAlertDialog / InputDialog | 返回/外点关闭；焦点与 IME 策略；中文 IME 组合区、选区替换；输入过滤；窗口创建/移除；材质缺失时明确失败 | P0 |
| CouiMenu | 选中/禁用项，关闭回调；长菜单滚动；子菜单交接；快速取消、再次展开；键盘导航 | P1 |
| CouiCheckbox | 外部整行 toggleable 的单一点击 owner、正确 Checkbox 语义；动画中切换与禁用 | P1 |
| CouiBottomBar / ChipTab | 选中项合法性、RTL、长项目列表、惯性/回弹中中断、隐藏附件；资源随节点移除而释放 | P1 |
| Backdrop / Effects | 不采样自身、释放后不再使用；不同 API 下显式失败/降级策略；独立 effect 入口 | P1 |

现有实现中 `CouiChipTabRow` 读取 display 刷新率、`CouiDialogWindow` 要求正确的 Window 和背景源、`CouiFloatingRenderer` 创建时校验 Shader。这些都是应在设备集成测试中处理的真实前置条件，**不能通过 Mock 消除错误条件后宣布兼容**。

## 推荐的最小验证集

第一轮完成 **10～15 个稳定、可重复执行的测试**即可，建议至少覆盖：

1. Switch 的点击、拖动取消、加载禁用与回调去重。
2. Slider 的范围边界、离散步进、键盘/语义。
3. Dialog 的正常关闭、输入替换、IME 与错误前置条件。
4. Menu 的禁用/关闭、子菜单中断。
5. Checkbox 整行语义与点击 owner。
6. Renderer API 34 的创建/失败处理以及单独使用 Effect。

“每个组件都存在一个测试文件”不是目标；测试必须能捕获具体回归。首批不要为生产代码写镜像算法当作测试 oracle。

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
- 不在此次评估中增加 CI 必需检查或承诺已完成设备验收。

测试实施顺序与 [高层 API 设计](high-level-api-design.md)配套：为现有组件增加默认参数时，验证“省略配置参数”与“显式传入相同预设”的行为一致，也验证原有自定义参数和 Effect 独立使用不受影响。
