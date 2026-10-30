# COUI-Kit 组件默认参数优化（设计提案，未实现）

本文依据 2026-10-30 的仓库接口制定。**不另建高层组件 API，也不设计 L1/L2 双层组件、便捷重载、强制 CouiTheme 或 CouiBackdropHost。** 目标是在**现有同名组件和原有参数列表**中，为有明确默认语义的配置项增加 Kotlin 默认值，让调用者可以省略这些参数；需要完全自定义时，仍按现有接口逐项传入。

## 目标与不变量

1. 保留 `CouiSwitch`、`CouiSlider`、`CouiCheckbox`、`CouiMenu`、`CouiBottomBar`、`CouiFab` 等现有组件入口；不创建 `SimpleCouiXxx` 或另一套高级组件接口。
2. **宿主业务输入和事件回调保留必填**，例如 `checked`、`value`、`items`、`onValueChange`、`onDismissRequest`。只把具有明确通用预设或安全空态的选项变为可省略。
3. `colors`、`shape`、`dark`、`textStyle`、`style` 等外观参数优先采用所属 `Defaults` 的默认配置；宿主显式传参始终优先。
4. 保留现有底层 **Effect / Engine** 接口、Renderer、Backdrop、Shader、Shadow 与 Motion，允许宿主完全绕开组件直接使用，不依赖主题或包装器。
5. 不添加全局隐式状态，不强制读取 `MaterialTheme`，也不将没有足够信息的背景采样/Shader 资源参数假装为安全默认值。
6. 修改默认值不是改变现有组件绘制后端；不让简单调用与定制调用走两套算法。

现有接口参考：[组件目录](../components/README.md)、[效果目录](../effects/README.md)、[API 约定](api-design.md)和[架构](architecture.md)。

## 对照 MIUIX 与 DialogX 后的取舍

- [Compose MIUIX](https://github.com/compose-miuix-ui/miuix) 的 `Switch(checked, onCheckedChange, modifier = ..., colors = ..., enabled = ...)` 体现了在单个组件函数中放置明确默认值的思路。COUI-Kit 可以借鉴此点，**不必引入 MiuixTheme 式完整主题系统**。
- [DialogX](https://github.com/kongzue/DialogX) 将常用配置设为可省略，使用者可继续深度定制；借鉴简化配置的原则，而不是复制其 View/全局窗口 API。
- COUI 的优势是独立 Effect 与复杂动效，不能为了参数数量好看就隐去渲染前置条件。

## 期望的调用方式

以下代码是**改造后的目标写法，当前 main 不保证可以编译**。

~~~kotlin
// 最常用的调用：业务状态与回调仍由宿主持有
CouiSwitch(
    checked = checked,
    onCheckedChange = { checked = it },
)
CouiSlider(
    value = value,
    onValueChange = { value = it },
)

// 同一个组件函数，按需覆盖原有参数
CouiSlider(
    value = value,
    onValueChange = onValueChange,
    dark = dark,
    colors = customColors,
    shape = customShape,
    steps = 4,
    showTickMarks = true,
)
~~~

不要求调用者首先包裹 `CouiTheme { ... }`。基础场景可用库内真实定义的 COUI 预设；宿主要使用动态主题色时仍可直接传自定义 Colors。

## 各组件参数建议

| 组件（现有函数） | 继续必填 | 首批评估增加默认值 | 不可直接省略的原因或注意事项 |
| --- | --- | --- | --- |
| `CouiSwitch` | `checked`、`onCheckedChange`（允许原有 `null` 表示外层接管） | `dark`、`colors` | 默认 `colors` 依赖深浅色和默认强调色，须确认配色来源；已有 `enabled`、`loading` 等默认值保留 |
| `CouiSlider` | `value`、`onValueChange` | `dark`、`colors`、`shape` | 当前 `colors` **位于** `dark` 前面；需避免显式覆盖 `dark` 后颜色仍按系统模式生成 |
| `CouiCheckbox` | `checked` | `shape`、`borderShape`、`colors` | 继续只是纯绘制组件；不擅自附加点击/无障碍 owner，宿主整行 `toggleable` 保持不变 |
| `CouiAlertDialog` / `CouiInputDialog` | 关闭/确认回调、受控输入值等真实业务参数 | `dark`，其余继续使用现有默认值 | `confirmAction` 不应自动伪造；材质背景源和窗口策略必须符合原契约 |
| `CouiMenu` | `expanded`、`items`、关闭与点击回调 | `style`（需要明确无主题预设来源） | 不能用空 Style 规避必需颜色、Shape；Popup 错误与输入行为不变 |
| `CouiTopAppBarButton` | `onClick`、内容 | `style`（从真实 Defaults 构建） | 内部 Renderer 的现有默认实现与错误行为保持 |
| `CouiFab` / `CouiBottomBar` / `CouiChipTabRow` | 状态/回调及必要渲染资源 | 稳定的颜色、Shape、可选外观配置；部分附件选项需单独评估 | `Backdrop` 是明确的采样来源，`Renderer` 有平台及资源生命周期约束，不为省参数就隐藏或传入虚构资源 |

表格不是逐项修改许可。实施前必须检查每个参数的真实消费者、默认值是否能在不访问宿主私有状态的情况下正确确定，以及旧调用如何继续工作。

## Kotlin 默认参数的实际规则

- **优先**使用 `shape: Shape = CouiCapsuleShape`、`enabled: Boolean = true` 一类保持原参数类型不变的默认值。省略参数与显式传入同一个默认值应有相同结果。
- **不等于全改 `? = null`**：`null` 应当有明确独立语义（如“由组件按当前 dark 计算配置”），而不是泛用占位符。优先使用非空默认值，避免额外判空、配置歧义和兼容风险。
- **依赖顺序必须验证**：如 `CouiSlider(colors, dark, shape, ...)`，Kotlin 的默认表达式不能引用后面才声明的 `dark`。不能为了默认值随意调换公开参数顺序，或让颜色默认值与显式 `dark` 不一致。必要时可在内部延迟求默认颜色；涉及 `colors` 的 nullable 改造须单独审核兼容性。
- **预设来源要明确**：库目前没有统一的全局强调色/主题。需要决定一个有来源的库内默认强调色及深浅色策略。默认跟随系统深浅色不等于自动跟随宿主强制主题。不要在设计中引用不存在的 `CouiDefaults.defaultAccent` 或宣称已经支持 M3 动态配色。
- 为 Compose 函数新增默认参数，仍需检查旧二进制调用、默认参数桥接、Kotlin 命名参数、`@Composable` 编译器签名以及跨版本消费。只看到普通 JVM 参数类型不变不足以证明所有调用完全兼容。
- **不要删除高级配置能力**：原参数的类型与行为尽量保留，现有 `Defaults` 工厂、Effect 调用和自定义 Shader/Renderer 路径必须继续可用。

## Backdrop / Renderer 特别边界

`CouiBackdrop` 需要显式采样源，消费者必须位于源之外。调用方可能有多层背景、跨 Window Dialog、不同 Renderer 生命周期或 Shader 失败处理。

因此**本轮不新增 `CouiBackdropHost`，也不把必需的 `Backdrop`/`Renderer` 一律设为 `null`**。先简化没有资源归属歧义的外观参数；以后若能证明组件可以安全持有独立 Renderer，再独立评审该参数的默认创建与释放。公开底层 Effect 的权限不因组件参数默认化而减少。

## 实施优先级与验收

1. **A：Switch / Slider / Checkbox 的外观参数默认化**。先确定默认色与暗色策略，核对原有参数顺序、调用点与新的省略调用；尽量只改签名及内部默认参数解析，不写重复绘制逻辑。
2. **B：Dialog、Menu、TopAppBarButton 的默认配置**。保留业务回调和 Window/Popup 输入语义；不让默认配置隐藏材料/键盘/跨窗口的错误。
3. **C：FAB、BottomBar、ChipTab 的更多外观选项**。关注多主体和可选附件，不急于省略 Renderer/Backdrop。
4. 每阶段同时验证：原有显式全参数调用、只写必需参数调用、部分参数覆盖、系统深浅色和宿主强制深浅色、语义/交互、设备与 R8 消费者构建，以及底层 Effect 单独使用。

**暂不实施**：新组件层级、便捷包装重载、`CouiTheme`/`CouiAppearance`、`CouiBackdropHost`、模块拆分、KMP、全面性能重构。本提案只涉及设计，不表示以上接口变更已经完成。
