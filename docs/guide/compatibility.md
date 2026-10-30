# 消费者兼容要求

接入 COUI 时，先检查编译环境，再确认所用组件和效果的 Android 版本要求。

## Android 与工具链

| 项目                   | 当前要求与依据                                                                                                                                                                  |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 运行系统                 | `minSdk` 以 [库配置](../../coui/build.gradle.kts) 为准；具体效果还受下表的平台门槛约束。                                                                                                        |
| 编译 SDK               | 不低于库 `aarMetadata.minCompileSdk`。该值与当前 Compose AAR 的要求一致。                                                                                                                |
| 消费者 AGP              | 不低于 `aarMetadata.minAgpVersion`，这是当前依赖元数据的必要下限，不是完整接入验证结果。                                                                                                               |
| Java 字节码             | 库配置使用 Java 17。Gradle 运行 JVM 按所用 AGP/Gradle 要求选择，不能把字节码目标当作运行 JVM 要求。                                                                                                     |
| Kotlin 与 Compose 编译器 | 源码接入先对齐 [版本目录](../../gradle/libs.versions.toml) 的开发基线，并配套 Compose 编译插件。更低 Kotlin 版本的元数据消费与 Compose 编译兼容尚未确认，不声明最低版本。                                                     |
| 本工程工具链               | [Wrapper](../../gradle/wrapper/gradle-wrapper.properties)、[Gradle JVM](../../gradle/gradle-daemon-jvm.properties) 和版本目录是开发基线。使用 AGP 内置 Kotlin；消费者不必仅因本工程配置而照搬全部 Demo 插件。 |

Maven Central 的 `0.0.1-alpha2` AAR 要求 `compileSdk` 不低于 37、AGP 不低于 9.1.0。其他版本以对应 AAR 元数据为准。库的 `minSdk` 由 manifest 合并表达，不要使用 `overrideLibrary` 绕过。

## 依赖对齐

库通过 `api(platform(...))` 提供 Compose alpha BOM，并公开 UI、Animation、Foundation 依赖。版本目录还显式请求 Foundation/Runtime；BOM 约束可能在依赖解析中选出更高版本，不能只看某一条 version.ref 就断言最终解析版本。宿主需要检查其实际依赖图，不要用 force、exclude 或另一组不匹配的 Compose 版本绕过冲突。

DynamicAnimation 保持版本目录中的 **strictly** 约束。持续重定向、取消和 pending 目标等行为是当前运动实现的一部分。宿主若要求不兼容的严格版本，应暴露冲突并评审整条运动链；不能直接放宽锁定，也不能以“已解析”为由声称动画等价。

当前源码的核心不依赖 Material3 或 Material Icons；Demo 与宿主的 M3 适配独立声明这些依赖。宿主自己调用 Material3、Activity 或其他 API 时，应直接声明相应依赖。`capsule` 只属于 Demo 的 G2 装配，不是 COUI 的公共或运行依赖。库不依赖外部业务工程、Hilt、宿主日志框架、第三方 Backdrop 或厂商 SDK。

### 原生主题与默认参数迁移

`CouiTheme` 与组件默认值属于当前源码，尚未发布到 `0.0.1-alpha2`。该发行版仍需要原有的显式装配，不能直接套用当前源码的省略参数示例；开发阶段使用源码模块或明确包含本改动的快照。

组件名称与原参数顺序保留。`CouiSlider.colors` 的 null 表示按最终 dark、enabled 自动生成；BottomBar 的 `rightContainerColor` 使用非空 `Color.Unspecified` 表示自动取色，避免 nullable Color 装箱。原有显式颜色、Style、Shape 与资源入口保留。默认参数、Kotlin 元数据与 Compose 消费兼容仍需重新编译调用模块验证，不声明二进制 ABI 稳定。

核心独立后，M3 `Icon/Text` 插槽不再自动继承 M3 的内容 Local；按[插槽迁移](appearance.md#m3-内容插槽迁移)显式传入 COUI 内容颜色与文字样式。顶栏按钮保留固定最小布局占位，不再读取 M3 的最小尺寸 Local。菜单普通锚点仍按原 AndroidX Below 候选顺序与安全区定位；触点窗口避让继续由原有 COUI motion provider 处理。

### 运动接口迁移

Touch/Press 的动画轴改为只读 State<Float>，原 `.value` 读取和构造入口保留，直接轴控制改走组件方法。这是二进制不兼容变更，所有调用模块需要重新编译；具体替换方式与生命周期要求见[运动与跟随回弹](../effects/motion.md#从公开-animatable-迁移)。

## 效果门槛

| 能力                                  | 运行边界                                                                 |
|-------------------------------------|----------------------------------------------------------------------|
| 背景源、普通 UI 与顶栏容器                     | 按库最低系统运行；顶栏内部保留明确的版本分支。                                              |
| RuntimeShader 的渐进模糊、轮廓、双主体融合        | 需要 API 33；融合没有内置旧平台替代容器。                                             |
| 菜单 Fast Kawase                      | 工厂在 API 33 以下返回 null，菜单内部使用既有 Gaussian 分支；该分支不等价于 Fast Kawase。       |
| 跟手光                                 | API 33 以下没有等价 Shader 光效；不会因此伪造绘制成功。                                  |
| 浮动底栏、FAB、顶栏按钮/搜索、ChipTab、普通 Mesh 阴影 | 需要 API 34；调用前检查版本，并处理 renderer 创建失败。                                 |
| fast 胶囊 Shader 裁剪                   | API 35 起使用公开 Canvas.clipShader；API 34 保留 RenderNode/RenderEffect 承载。 |

具体门槛仍以公开声明的 `RequiresApi` 和内部版本分支为准。所有能力都依赖 Android 实际图形管线；API 级别满足不保证设备 Shader 编译、精度和性能。宿主应在创建 renderer 时决定错误报告和替代 UI，不在库内吞掉错误。

## 验证范围

平台要求已核对配置、公开声明及缓存中的依赖元数据。库发行构建与 GitHub Packages 产物检查见[发布流程](../development/publishing.md)，实际结果以对应 Actions 为准。最低 Kotlin 消费版本、其他 AGP/Gradle 组合、独立接入、消费者 R8 和代表性设备效果仍待后续验证；不通过增加未经验证的兼容层扩大支持范围。
