# COUI-Kit 独立主题与组件默认参数设计（提案，未实现）

本文依据 2026-10-30 的 `main` 源码、用户提出的“COUI 核心不依赖 Material 3，但可以选择接入 Material Theme”要求制定。文中的 API 均是设计示意，尚未实现或完成消费者编译。

## 一、设计目标和边界

1. **`:coui` 是独立 Compose UI 库**：核心不直接或传递依赖 `androidx.compose.material3` 或 `androidx.compose.material` 组件，使用 Compose Runtime / UI / Foundation / Animation 和必要的 AndroidX 底层依赖。
2. **库内自有 COUI 配色与外观预设**，不使用 MaterialTheme 时组件照常工作；系统深浅色是默认模式来源，不代表 Android 12 动态取色。
3. **可选原生 `CouiTheme`** 只提供颜色、字体和 Shape 的上下文，不另建“简化组件”和“高级组件”两套 API。完全不要求调用者必须包裹 Theme。
4. **可选 Material 3 桥接由核心模块以外承载**；仅宿主决定是否启用。不能用 `compileOnly(material3)`、反射或在核心中引用 Material 3 类型伪装为无依赖。
5. 现有 `CouiSwitch`、`CouiSlider`、`CouiMenu`、`CouiBottomBar`、`CouiFab` 等名称、受控状态和高级自定义参数继续提供。支持省略有安全默认值的外观参数。
6. 原有 Backdrop、Renderer、Shader、Shadow、Motion 等 Effect/Engine 可以直接独立使用，不必经过主题或组件。资源、窗口和失败前置条件不隐藏。
7. **在迁移源码前维持现有绘制/交互效果**。不能把“使用 BasicText 替代 Material3 Text”与“视觉/行为已等价”混为一谈。

## 二、建议逻辑结构（不是大规模模块拆分）

~~~text
宿主（可使用 Material 3，也可以不用）
   ├── 可选的 COUI 主题适配
   │      └── coui-material3（薄桥接模块，可后续独立发行）
   └── coui（唯一必需库）
         ├── CouiTheme / CouiColorScheme / CouiPresets（库内）
         ├── 原有全部 Coui 组件（同名、同一绘制实现）
         └── 原有 Effect / Backdrop / Renderer / Motion（仍独立）
~~~

`coui-material3` 只是一个小的按需依赖，不表示现在就需要拆出所有组件或 Effect 模块。首轮可把 M3 配色映射原型留在 `demo` 中验证；规则稳定后再创建可发布的桥接模块。

### 原生 COUI 色彩令牌

建议使用最小的 `CouiColorScheme`：`accent`、`onAccent`、`surface`、`container`、`onSurface`、`onSurfaceVariant`、`outlineVariant` 等真实消费者需要的颜色；不照抄 M3 的全部角色。`CouiPresets.Light` 与 `CouiPresets.Dark` 来自核验过的 COUI 预设，区分“库默认配色”和“原厂原值”，不随意冒用原厂名称。

文字与 Shape 可用各组件现有 Defaults 作为初始默认。暂不强制所有窗口、Dialog 文本改用全局 Typography；当前 `CouiDialogText` 本身就有独立的原厂排版策略。

### 无 Theme、COUI Theme、Material 3 三种模式

~~~kotlin
// 仅示意：直接使用库内的 COUI 预设
CouiSwitch(checked = checked, onCheckedChange = onCheckedChange)

// 仅示意：可选的 COUI 原生主题
CouiTheme(darkTheme = dark, colorScheme = customCouiColors) {
    CouiSlider(value = progress, onValueChange = onProgressChange)
}

// 仅示意：可选 Material 3 适配模块；COUI 核心不读取 MaterialTheme
MaterialTheme(colorScheme = appColors) {
    CouiMaterial3Theme(darkTheme = dark) {
        CouiSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
~~~

Material 3 适配模块只读取宿主当前的 `MaterialTheme.colorScheme`，转换为 COUI 的颜色令牌；若宿主自己启用动态色，COUI 才随之跟随。适配层不创建第二套组件，不强制用户更换应用主题，也不在核心内调用 `dynamicLightColorScheme` / `dynamicDarkColorScheme`。

## 三、默认值和颜色决策优先级

**显式组件参数 ＞ 当前有效的 COUI 主题令牌 ＞ COUI 自身 Light/Dark 预设**。

- `dark` 未传：有 COUI Theme 时使用提供者的 `darkTheme`；否则使用 `isSystemInDarkTheme()` 的结果，避免读取 Material 3。
- `colors` 未传：根据最终生效的深浅色和主题令牌生成对应组件的预设。一个组件显式指定 `dark = true`，但外层是浅色主题时，不能继续套用浅色主题的表面/前景；优先找同模式配色，缺失则回退到 COUI 对应模式的预设。
- 显式传入 `colors`：完整尊重调用者，不因主题切换悄悄覆写。显式覆盖 `shape` / `style` 同理。
- `Backdrop` / `Renderer` 属于资源所有权和平台前置条件，不能与颜色参数采用相同的无条件默认策略。
- 宿主强制深浅色与系统深浅色不同，应以宿主显式的 `darkTheme` 或组件 `dark` 为准，不通过 `Color.luminance()` 猜测整个主题模式。

### Kotlin 签名限制

`CouiSwitch` 的 `dark` 在 `colors` 前面，可评估：

~~~kotlin
// 仅示意，需先完成 CouiTheme.isDark 和 Defaults 的实现
dark: Boolean = CouiTheme.isDark,
colors: CouiSwitchColors = CouiSwitchDefaults.colors(
    accent = CouiTheme.colorSchemeFor(dark).accent,
    dark = dark
)
~~~

**`CouiSlider` 当前却是 `colors` 在 `dark` 前面**，默认表达式不能引用后面的 `dark`。不应为此直接调换原公开参数顺序。可评估让 `colors: CouiSliderColors? = null` 明确代表“自动根据最终 dark 生成”，并在函数内部解析 `colors ?: CouiSliderDefaults.colors(...)`。这种 nullable 仅针对需要跨参数解析的配置，不能盲目把所有参数改成 null；要核对 Kotlin 元数据、Compose 默认参数桥接、已发布版本的二进制兼容及调用点。

## 四、迁出 Material 3 的真实代码范围

当前 `coui/build.gradle.kts` 存在 `implementation(libs.androidx.compose.material3)` 和 `implementation(libs.androidx.compose.material.icons.extended)`。下面五个源码文件直接引用了 Material 3：

| 代码 | 目前 M3 依赖内容 | 拟议改法 | 重点验收 |
| --- | --- | --- | --- |
| `menu/CouiMenu.kt` | `Text`、`Icon`、`MenuDefaults.rememberDropdownMenuPopupPositionProvider`、`MenuAnchorPosition` | 文本改为 Foundation `BasicText`，图标改为 Foundation `Image`/Painter；菜单定位使用自己的 `PopupPositionProvider` | 锚点、RTL、屏幕边缘、IME、全窗口 Popup、子菜单 |
| `navigation/CouiBottomBar.kt` | `Text`、`Icon` | `BasicText` 和矢量 `Image`，显式颜色与语义 | 图标 tint、字体度量、焦点和布局 |
| `chip/CouiChipTab.kt` | `Text`、`LocalContentColor` | `BasicText`、独立的 `LocalCouiContentColor` | 自定义 icon slot 颜色传递、RTL |
| `topappbar/CouiTopAppBarButton.kt` | `LocalContentColor`、`ProvideTextStyle`、`minimumInteractiveComponentSize` | 库内独立内容颜色/文字 Style 上下文；通过 Foundation/UI 实现等价触摸目标 | 40dp 外观、48dp 命中/布局、disabled 内容、外部 slot |
| `topappbar/CouiTopAppBarSearch.kt` | `Text`、`Icon`、`LocalContentColor`、`ProvideTextStyle`，搜索图标 `Icons.Outlined.Search` | `BasicText`、矢量 `Image`、库内内容上下文；搜索图标改为可再分发的内置矢量 | 交互中段、placeholder、焦点和图标位置 |

`CouiDialogText.kt` 已使用 Foundation 的 `BasicText`，可作为排版处理参考，但不能直接复用其专用字体逻辑。

**不能简单换掉 Material3 LocalContentColor / ProvideTextStyle 然后认为无行为变化**：外部在 Slot 中放 `Material3 Icon/Text` 的用户，过去依赖 M3 `CompositionLocal` 继承颜色和文字样式。核心独立以后应暴露 `LocalCouiContentColor` 或明确的样式令牌；使用者在 Slot 中主动读取或显式传 tint，M3 适配层再提供可选桥接。必须检查 Demo 和已有宿主的内容着色回归。

`MenuDefaults` 的定位器不是纯装饰：当前 `CouiMenuMotionPositionProvider` 接受通用 `PopupPositionProvider`，可以替换委托实现，但需要真实设备回归，禁止仅凭构建成功断言等价。M3 的 `minimumInteractiveComponentSize` 也不能直接等同 `sizeIn(48.dp)`，须核查实际布局尺寸和可触摸区域。

源码替换完成、通过测试后，才删除核心 `build.gradle.kts` 中的 M3 与 Extended Icons 依赖。Demo 仍可保留 Material 3，作为桥接示例和独立依赖边界验证者。

## 五、实施和验收顺序

1. **P0：清理核心 M3 使用点**：先实现保真的文本/图标、弹出菜单位置、内容局部样式和触摸目标，验证后移除依赖。核实当前与新版本的菜单、文字、图标、手势没有行为回退。
2. **P1：COUI 原生预设和最小颜色令牌**：库外没有 M3 时，Switch/Slider/Checkbox 可以只传业务状态；建立并验证深浅色默认值，不复制 M3 完整主题系统。
3. **P1：可选 COUI 主题和 Component Defaults**：为真实有需求的组件补默认参数；不添加新同名高级/便捷重载；保留全参数自定义与独立 Effect。
4. **P1：Material 3 薄桥接**：先在 Demo 验证从 `MaterialTheme` 到 COUI Tokens 的映射，确认混用文本及动态色后，按需发行 `coui-material3`。
5. **P2：测试与发布**：在无 Material 3 的独立消费者工程中验证 `:coui` 的 Release RuntimeClasspath 不含 M3/Material；在有 M3 的消费者验证可选桥接。还要覆盖显式 `dark` 覆盖、重组、配置切换、组合槽、R8、菜单 RTL/IME、Shader 资源失败和已有高自定义 Effect。

不在本设计任务实施 Gradle 模块拆分、组件重写、KMP 或 GPU 性能优化。实现仍须独立 PR、设备与消费者验证。

参考：[Android 自定义 Compose 设计系统](https://developer.android.com/develop/ui/compose/designsystems/custom)、[Compose 主题结构](https://developer.android.com/develop/ui/compose/designsystems/anatomy)、[Compose Text](https://developer.android.com/develop/ui/compose/text)。
