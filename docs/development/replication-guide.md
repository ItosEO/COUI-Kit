# COUI 实现分析与复刻指南

复刻新组件或排查效果差异时，先确认原厂页面实际使用的组件，再追踪参数、算法、状态和最终绘制。本指南整理菜单、底栏、FAB、顶栏、搜索、滑条和 ChipTab 的分析方法与排错经验，代码入口和专题导航均指向本仓库。开发与评审流程见 [Developer 指南](workflow.md)。

## 1. 先找对入口

按问题选择资料，不要先扫描所有 APK 或重写效果。

| 要做的事              | 起点                                                                                                       |
|-------------------|----------------------------------------------------------------------------------------------------------|
| 在宿主中使用现有组件        | [接入指南](../guide/getting-started.md)、[架构](architecture.md)及 Demo 的同族示例                                    |
| 确认模块职责、开发要求和已批准差异 | [Developer 指南](README.md)、[组件装配](../components/README.md)及对应 Note                                        |
| 研究菜单背景、材质或厂商 SDK  | 本文第 2、3 节及[菜单输入与合成记录](../../.agents/notes/implemented/bug-fix/2026-10-15-coui-submenu-input-dispatch.md) |
| 查某次选型、修复原因和验证范围   | 本文末尾的专题导航，继续进入对应决策记录                                                                                     |
| 恢复原厂分析            | 对应记录中的 APK、资源、源码导出和二进制证据                                                                                 |

当前代码和构建配置说明本库实际实现；选定版本的原厂材料说明复刻目标；已批准的适配由对应 Note 记录。三者有差异时先定位原因，不能用当前实现证明自身符合原厂，也不能用旧记录覆盖已批准适配。历史数值、地址和旧文件名仅作定位线索。本文不复制参数表或 Shader 全文；参数调整在实现处维护，原始证据在专题中保留。

## 2. 从系统页面到实际消费者

### 2.1 固定目标和来源

先描述目标页面、控件及操作。例如“便签顶栏搜索的展开与收起”，比“COUI 搜索效果”更准确。

对新材料记录以下信息：

- 设备型号、系统构建、APK 包名、版本和设备路径。
- 实际参与目标链路的 APK、framework 或系统库的版本与 SHA-256；更新后重新匹配，不沿用旧地址，不为无关材料建立清单。
- 页面所属 Activity/Fragment、布局、组件类型、资源及主题覆盖。
- 浅深色、光效开关、系统动画等级等会改变分支的条件。
- 材料缺口。缺少构建指纹或某段源码时明确记录，不补猜测值。

**先确认应用实际装配，再分析库默认值。** 同一 APK 内可以同时包含独立 FAB、附件 FAB、旧版控件和未使用实现。某个类或资源存在，不表示目标页面使用它。不同 APK 带有同名类，也不表示版本及默认分支相同。

有设备授权时，可用 `adb shell pm path <包名>` 确认输入，再只读提取。不要猜系统 APK 路径；本项目的设备操作权限仍按 [AGENTS.md](../../AGENTS.md) 执行。

### 2.2 定点分析 Java、资源与状态

优先用 Droid ASC 查类、引用及方法；需要完整导出或交叉核对时再用 JADX。不要把反编译文本当作可直接编译的官方源码。

追踪顺序：

```text
目标页面 / 布局
  → 实际组件和 delegate / controller
  → 主题、资源、调用方覆盖与运行门控
  → 参数生产者、动画及事件通知
  → Drawable / RenderNode / JNI / 系统扩展入口
```

每项外观和交互都要找到消费者。例如资源声明 press 色，还需要确认谁更新 pressed/touchEntered，谁启动动画，谁绘制蒙层。不能只凭 setter 名称、默认资源或截图确认行为。

对于动画，除弹簧或曲线外，还要检查起终点、初速度、结束阈值、可见性、输入启用时机，以及中断、反向和 CANCEL。只搬曲线无法复刻整条交互链。

对含交互的组件，沿原厂基类、父容器事件分发与回调补齐行为。每条受影响的路径应能回答：由哪个事件、在什么前置状态下进入，改了哪些状态，如何退出，下一次操作从哪里继续。将原厂入口与移植入口对照记录在所属 Note，不另建重复报告。输入被父容器接管后，按压、点触光、点击和滚动各自如何结束，必须分别确认。

### 2.3 算法进入系统扩展时，再追 native

`OplusBlurParam` 一类对象只是参数载体。算法或绘制委托给系统扩展时，继续查谁解析字段、选择算法、构造 Shader 和提交绘制。纯 Java 已闭合的输入路径，不因项目使用 Shader 就重新研究 native。

```text
参数生产者
  → JNI / 系统参数传递
  → native 分支选择及预计算
  → Shader 片段拼装、uniform 和 child shader
  → 网格 / 纹理 / coverage / 混合
  → 实际输出目标
```

复查 native 时注意：

- 区分文件偏移、虚拟地址和符号地址；按 ELF 段映射转换。
- Shader 可能由多个空终止字符串拼接。单段没有完整公式，不表示原厂缺少该计算；要核对构造函数的拼接顺序。
- 同时读 Shader 与 CPU 预计算。距离场常量、圆角转换、influence 顺序和 alpha 可能在 Shader 之前处理。
- AOSP/Skia 源码可帮助理解结构，但 OEM 库中的实际分支才是目标证据。
- 反编译的类型、布尔表达式或浮点常量可出错。关键分支要用另一导出、字节码或汇编交叉核对。

### 2.4 先判断能否直接调用，再确定移植边界

本项目要求跨 OEM。厂商 Java 反射成功，不表示其他 ROM 有相同消费者；Shell/Root 能加载类，也不证明普通 App UID 能运行同一路径。

ONextBox 可用于定位反射调用链，但它不是官方算法证据。复制参数类或打包系统 so，也不会自动取得内部 Skia、HWUI、RenderEngine 对象与私有 ABI。

当前路线是：保留已确认的原厂算法、资源和状态语义，用公开 Android/Compose API 承载；不要按截图另造近似公式，也不要为保持厂商类结构而搬入未使用的框架。**改变承载方式仍有代价，必须登记，不能称为整条 native 管线等价。** 实现边界见 [Developer 指南](README.md)与[模块分层记录](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)。

## 3. 典型研究链与当前移植入口

本节链接包含库内部实现，供分析和维护时阅读；宿主接入使用[组件与效果入口](../../README.md)。Demo 是公开接口的消费者，不直接装配内部状态。

### 3.1 菜单：背景与前景不是同一阶段

录音菜单的已定位原厂链为：

```text
COUIPopupListWindow / COUIPopupWindow
  → RoundFrameLayout
  → COUIBackgroundBlurBuilder
  → ViewRootManager / BackgroundBlurDrawable 扩展
  → 系统合成端的模糊与材质消费者
```

Java 入口、参数与 native 模糊片段分别有证据；这不表示所有中间传递步骤都已逐段执行验证。原厂窗口后景与本库的应用内采样来源也不同。

当前移植入口：

- [CouiMenuBlur](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)：Fast Kawase 的采样核、各轮网格及边界。
- [CouiMenuMaterial](../../coui/src/main/java/com/itos/coui/menu/CouiMenuMaterial.kt)：采样、材质与前景装配。
- [CouiEdge](../../coui/src/main/java/com/itos/coui/effect/CouiEdge.kt)：当前菜单组合使用的通知描边与菜单内晕。
- [CouiSpotlight](../../coui/src/main/java/com/itos/coui/effect/CouiSpotlight.kt)：点触光、跟手及预设。
- [CouiMenuSubMenu](../../coui/src/main/java/com/itos/coui/menu/CouiMenuSubMenu.kt)：主子面板及连续输入。

菜单现在是用户批准的组合，不是原 COUI 菜单整套预设。不要以“恢复官方”为由撤销通知描边、主题染色等已登记特化。当前组合、采样和输入边界见[菜单记录](../../.agents/notes/implemented/bug-fix/2026-10-15-coui-submenu-input-dispatch.md)。

### 3.2 底栏与 FAB：先分主体，再分效果

日历、电话和便签用于确认真实容器、附件收放、独立 FAB、材质及阴影配置。不能仅凭便签只有一个可见按钮，就断定它始终使用独立容器。

底栏融合的主要链路为：

```text
组件配置、主体几何和两两预计算
  → blendPair 更新融合距离 minDistance
  → ShapeResult.sdf
  → 内晕与 blendEdge
  → 使用同一融合覆盖率及最终 alpha 合成
```

当前 [CouiFloatingShader](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingShader.kt) 已让轮廓使用融合距离场。再次要求“轮廓融合”时，应先验证此链路是否被破坏，而不是另叠一层独立描边。

[CouiFloatingSurfaces](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt) 承接采样、普通投影、caustic、材质和内容的分层。独立 FAB 走单主体入口，不填充假附件，也不共享底栏的收放状态。当前保留附件式底栏和独立 FAB，没有实现普通底栏绑定外部 FAB 的协议，当前装配见[独立 FAB](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)与[底栏](../../coui/src/main/java/com/itos/coui/navigation/CouiBottomBar.kt)。

**普通投影与有色外光晕分开研究：**

- 普通投影：组件配置 → 光源与 Outline → Skia fast/generic 网格 → 离散 LUT 或对应衰减 → ambient/spot 各自裁剪和合成。
- caustic 外光晕：独立 Shader、颜色和背景参考，不等于上述 spot 投影。
- 轮廓与内晕：材质距离场内的另一条计算，不用普通投影模拟。

[CouiShadowRenderer](../../coui/src/main/java/com/itos/coui/effect/CouiShadowRenderer.kt) 保留平台承载分支：支持公开 Shader 裁剪的平台让正常 fast 胶囊继续查原 A8 纹理；旧平台保留每路滤镜节点。真正三角化失败的 blur 分支另行保留。不能把“减少滤镜层”写成“合成两路后裁剪一次”，详见[阴影移植](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)中的公开裁剪与缓存决定。

### 3.3 顶栏与搜索：复刻目标包含合成位置

顶栏渐进模糊沿 `AppBarBlurHelper` → `OplusRenderEffect.createGradientBlurEffect` → native 渐进 Gaussian 消费者追踪。目标分支先横后纵执行位置可变卷积，不是菜单 Fast Kawase，也不是均匀模糊后与原图混合。当前 [CouiGradientBlur](../../coui/src/main/java/com/itos/coui/effect/CouiGradientBlur.kt) 保留核与两遍顺序，强度曲线、噪声和颜色装配有项目适配，见[渐进模糊决策](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-topbar-gradient-blur.md)。

顶栏从 `ToolbarMaterialEffectDelegate` 继续追到状态 Drawable、点触光及形变消费者；不能直接复用 FAB 整套预设。

便签搜索从 `COUISearchBar.showInToolBar/hideInToolBar` 追到宽度裁剪、位移、内容透明度和中段模糊。当前 [CouiTopAppBarSearch](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarSearch.kt) 只模糊透明内容层，材质、轮廓、光晕和投影保持清晰；不是将整个按钮等比放大并模糊。

当前搜索支持常驻与退场操作，Demo 展示两种装配。常驻操作移动和收起避让包含已批准的适配，不能将全部收起参数视为原厂默认；宿主自行决定动作内容。研究与修复依据见[搜索转场边界](../../.agents/notes/implemented/bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)。

### 3.4 滑条与通知融合：复用已确认的分支

字体设置页的研究从 `FontSettingsFragment` 分别进入 `COUISeekBar` 和 `COUISectionSeekBar`，再查核心交互、Section 动画、纹理绘制、跟手和反馈。隐藏点位不等于没有内部步进；预览节流也不是滑块步长。

当前 [CouiSlider](../../coui/src/main/java/com/itos/coui/slider/CouiSlider.kt) 分离逻辑值与视觉位置，业务范围及保存仍由宿主负责。宿主选择的 G2 轨道是外观适配，不是提取版本的原厂默认；专用马达波形及完整物理引擎没有跨平台等价保证。见[滑条决策](../../.agents/notes/implemented/feature/2026-10-21-coui-slider.md)。

通知侧滑的同色横向双主体 CONIC 场已抽到 [CouiMetaballRenderer](../../coui/src/main/java/com/itos/coui/effect/CouiMetaballRenderer.kt)。[Demo 融合示例](../../demo/src/main/java/com/itos/coui/demo/DemoRecordMetaball.kt)中的背景与内容遮罩使用相同算法、分别持有 renderer。它不提供背景采样或输入，也不替代底栏的多主体分层材质。名称都叫“融球”，不是整段替换的依据。

### 3.5 ChipTab：核对整条手势，而不只核对物理公式

分类栏的惯性、回弹和选中露出并非同一条动画。物理类能收敛，不代表调用它的手势总能完成收尾；复用通用按压入口，也不代表已经保留原厂的取消时机。

原厂对照覆盖便签与录音的 `COUIChip`、`COUIHorizontalScrollView` 和 `SpringOverScroller`。材料版本及哈希见[输入中断记录](../../.agents/notes/implemented/bug-fix/2026-10-24-chip-scroll-interruption.md)，组件装配见[ChipTab 记录](../../.agents/notes/implemented/feature/2026-10-24-chip-tab.md)。本次核查使用以下方法发现差异：

| 反例与检查方法                            | 发现的差异                         | 原厂依据与当前入口                                                                                                                                                                                                |
|------------------------------------|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 越界回弹中按下，不达到拖动阈值就抬手；从 DOWN 追到每个提前返回 | DOWN 已停止旧回弹，但阈值等待外的收尾无法恢复它    | 原厂分发及触摸路径在 UP/CANCEL 调用 `springBack`，未进入拖动的 UP 也有处理；当前 [CouiChipTabRow](../../coui/src/main/java/com/itos/coui/chip/CouiChipTabRow.kt) 将阈值等待纳入同一收尾范围                                                     |
| 按住分类项后横向拖动，让父容器接管；分别检查按压与点触光       | 点触光取消不能代替按压释放；按压还须防止本次手势中重新启动 | `COUIChip.handleActionCancel` 调用 `executeFeedbackAnimator(false)`；当前分类项显式开启公共输入的 `cancelOnConsumedMove`，其他消费者保留原行为                                                                                       |
| 分别追直接松手、惯性触边和中断，不只比较可见位置与速度        | 将临时积分位置统一重置到边界，会改变不同入口的初始状态   | 原厂 `springBack` 经 `setCurrentValue(..., false)` 保留临时位置；`setAtRest` 同步当前位置；`notifyEdgeReached` 写入边界。当前 [CouiChipScrollPhysics](../../coui/src/main/java/com/itos/coui/chip/CouiChipScrollPhysics.kt) 分别处理 |

此外还要检查 Compose 承载：滚动后的不可见子项能否命中相邻图标按钮，点击露出与物理回弹能否在同帧竞争。Canvas 渐隐只处理绘制，不能替代输入范围限制。这些是移植装配问题，不能都归为原厂物理算法的差异。

审查应双向进行：从原厂找移植遗漏，再从移植代码查无依据的重置、裁剪和回调。不要用同一份移植逻辑同时生成实现和“正确答案”。已有终止、方向和终点检查保留，但不能据此声称已经覆盖中断、再次进入或 Android 实际事件分发。

## 4. 移植前要写清的边界

### 4.1 区分三种结论

| 类型     | 例子                                      | 记录方式                    |
|--------|-----------------------------------------|-------------------------|
| 原厂依据   | Shader 片段、预计算、资源覆盖、事件启用顺序               | 记录目标材料、调用者与消费者、实际分支     |
| 公开平台适配 | GraphicsLayer 采样、Mesh、RenderEffect、公开裁剪 | 说明与系统上下文的差异、支持范围和验证缺口   |
| 项目主动特化 | 主题染色、菜单光效组合、滑条 G2、搜索避让                  | 记录用户目标及作用范围，不传播成其他组件默认值 |

不要将用户接受的视觉差异写成已证明等价，也不要将明确特化误判成遗漏。选择 public API 不等于获准用另一公式替代原厂算法。

### 4.2 复用按职责，不按文件名

当前归属为：

```text
宿主 App / demo：页面、主题、Shape、导航、查询、选择与业务回调
  → 宿主组件适配层（按需）：样式选择、错误报告及外观装配
  → coui：COUI 控件、固定预设、效果、采样、几何、动效和输入
```

公共 Defaults 由各组件族持有；主题取色、HCT、应用偏好和日志留在宿主。不要因为文件导入 COUI 就将它迁入库，也不要新增只转发参数的包装层。当前代码入口和完整边界见[架构](architecture.md)及[分层决策](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)。

### 4.3 为一条绘制链核对这些输入

- **采样来源**：原始页面、已模糊渐进背景，还是含普通投影的前缀？是否误采样自身？
- **坐标**：本地、窗口、屏幕、采样留白和纹理坐标分别由谁换算？单位是 px 还是 dp？
- **几何**：材质 SDF、内容 Path、普通投影 Outline、系统 Window blur 是否本来就不同？
- **颜色**：预乘还是非预乘，SDR 还是其他分支，alpha 在何处加权或截断？
- **合成**：输出包含 destination，还是只有前景？最终是 Src、SrcOver 还是其他模式？
- **状态**：点击、形变、点触光、绘制和融合参与是否分别受控？取消及移除如何收尾？
- **资源**：uniform 更新何时形成快照，缓存键包含什么，资源何时释放或重录？

这些输入未对齐时，不应先用颜色和强度补偿。

## 5. 已遇到的坑及排查方法

### 5.1 材质、光效与投影

| 症状 / 错误前提                      | 实际检查点与经验                                                                               | 依据                                                                                                     |
|--------------------------------|----------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| 同名 Overlay 就是同一算法              | 背景材质模式、Paint 混合、通知 EdgeType 属于不同消费者，不能按名字互换                                            | [菜单合成记录](../../.agents/notes/implemented/bug-fix/2026-10-15-coui-submenu-input-dispatch.md)            |
| 顶栏点触光过强，先减 intensity           | 原参数相同也可能因混合目标不同而变强。透明前景内 Overlay 后再 SrcOver，不等于直接混合已完成的按钮表面；先核对层序                      | [点触光合成](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButton.kt)                       |
| 按下整面发灰，资源里有 press 色所以正确        | `setNativeStateEnabled` 的提取实现写入 disabled flag；顶栏也未走相应 touchEntered 通知。必须核对消费者，不能按方法名猜  | [顶栏状态反馈](../../coui/src/main/java/com/itos/coui/effect/CouiStateFeedback.kt)                           |
| 相邻按钮出现矩形截断，扩大 Canvas           | 完整 destination patch 用 Src 回填时会擦掉先绘制的兄弟投影。扩大 patch 反而扩大擦除。顶栏改用透明前景表达，但真正重叠的光区仍有采样顺序差异  | [兄弟投影合成](../../.agents/notes/implemented/bug-fix/2026-10-20-topappbar-search-transition-boundaries.md) |
| 浅色有色 FAB 底下不明显，全部当成 caustic 调整 | 先分普通 ambient/spot、caustic、轮廓和内晕。阴影颜色 alpha 不是最终像素强度；还受光源、节点/Outline alpha、全局乘数、截断和裁剪影响 | [阴影移植](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)                       |
| 关闭轮廓后仍有淡色影，所以保留弱 caustic       | 提取分支中的残留色影来自普通投影。关闭光效应停止对应 patch 和采样前缀贡献，不能只将 caustic 调淡                               | [外光晕门控](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt)                         |
| 全部叫 G2，所以强制一条 Path             | 材质 SDF、投影 conic、Capsule Path、Window blur 的几何及覆盖率不自动等价。遮色窄带也无法补回缺失模糊；先保留各链路真实边界         | [阴影移植](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)                       |

### 5.2 采样、坐标与资源

| 症状 / 错误前提                 | 实际检查点与经验                                                                                                       | 依据                                                                                                                                                          |
|---------------------------|----------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 模糊出现马赛克，直接提高半径            | 先检查源栅格化、降采样网格、滤波与逻辑边界 Clamp。不要在低分辨率目标中重新绘制文字和矢量内容                                                              | [采样与 Fast Kawase](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)                                                                              |
| 菜单用 Kawase，所以顶栏也用多级降采样    | 顶栏实际渐进分支使用位置可变 Gaussian；旧多级 atlas 路线没有对应依据。也不能未查采样器就将马赛克归因为 nearest filtering                                  | [渐进模糊研究](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-topbar-gradient-blur.md)                                                                   |
| 拖动时后景跟按钮走                 | 检查实际窗口坐标、采样原点与变换订阅。浮动 bounds 为未缩放坐标，缩放单独传入；投影网格已含缩放时不能外层再缩放                                                    | [浮动几何](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingGeometry.kt)、[绘制装配](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt) |
| 背景停在旧帧或主题切换无效             | 背景源不能永久缓存；已有 RenderEffect 保存的是 uniform 快照。几何、主题、开关或目标坐标变化后需正确失效；跨窗口还要核对源 View                                  | [背景契约](../effects/backdrop.md)                                                                                                                              |
| 后台返回投影变淡，点一下恢复            | 不能用同色判断录制仍有效。系统可丢弃内部 RenderNode 的 DisplayList；应在失效时重录，不增加持续刷新掩盖缺陷。历史缓存自检证明修复行为，不证明每次设备症状都由此触发                  | [阴影缓存与验证](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)                                                                         |
| 录制投影触发 StackOverflowError | 当前 Compose 的作用域 record 可将调用方 Density 写回自身 DrawContext。纯投影使用显式独立 Density 的成员入口；含 drawContent 的前景仍需原绘制上下文，不能批量替换 | [当前分层录制](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt)                                                                             |
| Shader 构造不报错，因此空白就是透明度问题  | 构造成功不证明目标 GPU 能执行所用 SkSL。先区分未执行与正常透明输出，不能给未知后端写虚假成功路径                                                          | [兼容要求](../guide/compatibility.md)                                                                                                                           |

### 5.3 布局、动画与输入

| 症状 / 错误前提                 | 实际检查点与经验                                                                        | 依据                                                                                                     |
|---------------------------|---------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| 静止正常，淡入淡出时阴影被裁            | `clip=false` 不能解除祖先 alpha 离屏纹理边界。逐层核对动画 outsets，不用布局 padding 改测量、命中和菜单锚点        | [动画绘制边界](../../coui/src/main/java/com/itos/coui/topappbar/CouiTopAppBarButtonGroup.kt)                 |
| 将每个按钮单独提升到 overlay 就能保留动画 | 可能丢失内部祖先淡化。当前提升完整槽位、保留原位输入与锚点；局部 overlay 仍受页面/Pager 外层变换约束                      | [槽位与背景](../../.agents/notes/implemented/bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)  |
| 聚焦搜索时突然翻页，必然是 overlay 跨页  | 先追焦点 → BringIntoView → Pager。关闭用户滑动不等于禁止焦点滚动；固定顶栏应约束向祖先传递的可见范围，不延迟焦点或强制跳回页面掩盖原因 | [搜索转场边界](../../.agents/notes/implemented/bug-fix/2026-10-20-topappbar-search-transition-boundaries.md) |
| 搜索起点跳一下、收起偶发横移            | 点击时捕获实际中心，真实放置时换算；转场端点保持稳定，宽度与位移使用同帧进度。不能等待全局坐标后修正首帧，也不能由操作区恢复重定向收起路径           | 同上                                                                                                     |
| 隐藏搜索时操作区向右下缩，搜索先收再消失      | UI 隐藏不等于结束业务搜索。纯淡化须保留实际测量及预布局尺寸；查询、展收目标、可见性和焦点分别处理                              | 同上                                                                                                     |
| 收起尾部偶发方形亮块                | 排查残留透明点击区继承的 Material indication，而不是只检查 COUI Shader 裁剪                          | 同上                                                                                                     |
| FAB 展开时无法点击，等弹簧结束才恢复      | 官方把按钮启用与收放期间的形变限制分开。不能用一个 running 开关关闭点击、语义、跟手光和形变                              | [附件控制器](../../coui/src/main/java/com/itos/coui/navigation/CouiFabExpansion.kt)                         |
| 手指出界就统一 CANCEL            | 原生 DOWN 与主子面板转发的会话规则不同；销毁也不等于真实 UP。不能因此误提交点击或导航                                 | [子菜单输入](../../.agents/notes/implemented/bug-fix/2026-10-15-coui-submenu-input-dispatch.md)             |

## 6. 验证原理，不伪造验收

先用已有最小入口验证单条链。核对算法与参数后，再由用户完成设备视觉、输入和性能测试。构建、设备操作等执行范围遵循 [AGENTS.md](../../AGENTS.md)，不因本文新增默认动作。

| 方法                      | 能说明什么                    | 不能说明什么                 |
|-------------------------|--------------------------|------------------------|
| 源码、资源、汇编与调用引用核对         | 目标分支、公式来源、参数生产和消费关系      | 系统运行时必然选择该分支           |
| 原库执行与移植代码数值对照           | 所覆盖输入下的分支、网格、索引及强度计算差异   | 未执行入口、最终 GPU 像素和全设备兼容性 |
| 移植代码的终止、方向与范围断言         | 所覆盖输入下的基本不变量             | 原厂等价、完整事件分发及未覆盖的中断路径   |
| 针对性数学断言 / 图形 API 替身     | 缓存失效、坐标与透明度约束、版本路由等可观察行为 | Android 实际绘制或动画轨迹      |
| PSI、离线类型检查或桌面 Shader 编译 | 各自工具覆盖的语法、签名或编译能力        | Android 模块构建、目标 GPU 实绘 |
| 设备静态图、慢放及操作记录           | 指定设备和条件下的可见结果            | 所有主题、变换、设备及 GPU 成本     |
| 同条件 trace 和单因素旁路        | 具体操作中某条链的成本线索            | 小样本固定收益或全应用平均性能        |

验证记录要说明实际执行的入口、输入和工具。例如只调用原库网格生成函数，就不能写成已执行整个原厂工厂链；替身通过不能写成真机通过。

优先选择能暴露差异的最小输入序列。对中断问题，检查“动画中 → DOWN → 未达到阈值的 UP/CANCEL → 再次操作”，并区分父容器接管与真实释放。比较对应路径的状态更新及下一帧起点，不只比较最终是否回到边界。只有本库输出时，记录为自身行为检查，不写成原库对照；增加同类数值样本不能补上缺失的入口。

视觉差异先按“采样 → 坐标 → 材质 → 合成 → 几何 → 状态”缩小范围。性能差异先查缓存失效、重复录制、离屏面积、采样次数、网格重建及片段运算。**没有新增 effect 节点，不等于没有新增 GPU 成本。**

普通投影的性能研究采用过同包旁路对照，再针对 fast 裁剪承载减少中间图像。操作速度和样本量不同的结果只作线索，不转成固定收益；细节保留在[对应记录](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)。不要为复现一次实验长期保留额外设置、计数器或诊断入口。

## 7. 交接时保留什么

研究材料独立于源码仓库保存。交接时提供材料位置和恢复方法，不把某位开发者的本机目录写入库、构建配置或公共接入步骤。

材料可按 `apks/`、`sources/`、`notes/`、`notes/evidence/` 和 `notes/indexes/` 组织，并按设备或版本区分。先从专题找到原始输入，再匹配哈希；旧资料索引中的实现路径需要对照本仓库当前代码。

一份可恢复的研究记录至少应让后续开发者回答：

1. **研究什么？** 目标页面、操作、状态和期望差异。
2. **根据什么？** 原始输入及来源、哈希、目标类/资源/符号、必要提取片段。
3. **链路在哪里？** 从装配到实际消费者；哪些步骤已确认，哪些仍缺证据。
4. **怎么移植？** 当前生产入口、输入输出、平台承载与项目主动特化。
5. **为什么这样选？** 真实比较过的方案、放弃原因及剩余代价。
6. **怎么验证？** 实际执行的检查、反例、用户观察与未验收部分。

不要只交接临时目录、聊天摘要或一张效果图。完整 APK/库、来源记录和必要证据应能恢复；整棵反编译副本和一次性实验脚本不必全部长期保留。不得将厂商二进制复制进源码、assets 或构建依赖来代替移植。许可证随实际移植代码维护，Skia 许可已位于 [coui-skia-LICENSE](../../coui/src/main/resources/META-INF/coui-skia-LICENSE)。

## 8. 专题导航

| 专题            | 当前入口 / 理由与证据                                                                                                                                                                                                                                            |
|---------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 模块归属与预设       | [架构](architecture.md)、[分层决策](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)                                                                                                                                           |
| 菜单采样、材质与输入    | [菜单记录](../../.agents/notes/implemented/bug-fix/2026-10-15-coui-submenu-input-dispatch.md)、[模糊实现](../../coui/src/main/java/com/itos/coui/menu/CouiMenuBlur.kt)、[主子面板](../../coui/src/main/java/com/itos/coui/menu/CouiMenuSubMenu.kt)                    |
| 底栏、独立 FAB 与配色 | [底栏](../../coui/src/main/java/com/itos/coui/navigation/CouiBottomBar.kt)、[独立 FAB](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFab.kt)、[配色预设](../../coui/src/main/java/com/itos/coui/floatingactionbutton/CouiFabDefaults.kt)        |
| 轮廓门控与材质分层     | [浮动表面](../effects/floating-surfaces.md)、[光效开关](../../coui/src/main/java/com/itos/coui/effect/CouiEffectLocals.kt)、[浮动表面](../../coui/src/main/java/com/itos/coui/effect/CouiFloatingSurfaces.kt)                                                         |
| 普通投影          | [网格、覆盖率与缓存决定](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-native-shadow.md)、[菜单投影装配](../../coui/src/main/java/com/itos/coui/menu/CouiMenuShadow.kt)                                                                                         |
| 顶栏与搜索         | [渐进模糊](../../.agents/notes/implemented/bug-fix/2026-10-18-coui-topbar-gradient-blur.md)、[overlay 与搜索边界](../../.agents/notes/implemented/bug-fix/2026-10-20-topappbar-search-transition-boundaries.md)                                                   |
| 滑条            | [接入与物理边界](../../.agents/notes/implemented/feature/2026-10-21-coui-slider.md)、[公开组件](../../coui/src/main/java/com/itos/coui/slider/CouiSlider.kt)                                                                                                        |
| ChipTab 与横向输入 | [组件装配](../../.agents/notes/implemented/feature/2026-10-24-chip-tab.md)、[选中项露出](../../.agents/notes/implemented/bug-fix/2026-10-24-chip-selection-visibility.md)、[命中与中断](../../.agents/notes/implemented/bug-fix/2026-10-24-chip-scroll-interruption.md) |
| 横向双主体融合       | [算法接入](../effects/metaball.md)、[生产算法](../../coui/src/main/java/com/itos/coui/effect/CouiMetaballRenderer.kt)、[Demo](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt)                                                                   |
| 开发与验证流程       | [Developer 指南](README.md)、[消费者兼容要求](../guide/compatibility.md)、[执行约束](../../AGENTS.md)                                                                                                                                                                  |

后续增加新组件时，先检查已有消费者、算法和决策，再恢复缺少的定点证据。不要因一个局部差异，自动重开已停止的整套研究或重写可用链路。
