# 消费者兼容要求

接入 COUI 时，先检查编译环境，再确认所用组件和效果的 Android 版本要求。

## Android 与工具链

| 项目                   | 当前要求与依据                                                                                                                                                                  |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 运行系统                 | `minSdk` 以 [库配置](../../coui/build.gradle.kts) 为准；具体效果还受下表的平台门槛约束。                                                                                                        |
| 编译 SDK               | 不低于库 `aarMetadata.minCompileSdk`。该值与当前 Compose AAR 的要求一致。                                                                                                                |
| 消费者 AGP              | 不低于库 `aarMetadata.minAgpVersion`。 |
| Java 字节码             | 库配置使用 Java 17。Gradle 运行 JVM 按所用 AGP/Gradle 要求选择，不能把字节码目标当作运行 JVM 要求。                                                                                                     |
| Kotlin 与 Compose 编译器 | 源码接入先对齐 [版本目录](../../gradle/libs.versions.toml) 的开发基线，并配套 Compose 编译插件。更低 Kotlin 版本的元数据消费与 Compose 编译兼容尚未确认，不声明最低版本。                                                     |
| 本工程工具链               | [Wrapper](../../gradle/wrapper/gradle-wrapper.properties)、[Gradle JVM](../../gradle/gradle-daemon-jvm.properties) 和版本目录是开发基线。使用 AGP 内置 Kotlin；消费者不必仅因本工程配置而照搬全部 Demo 插件。 |

发行版的编译要求以对应 AAR 元数据为准。库的 `minSdk` 通过 manifest 合并检查，不要使用 `overrideLibrary` 绕过。

## 依赖对齐

库通过 `api(platform(...))` 提供 Compose alpha BOM，并公开 UI、Animation、Foundation 依赖。版本目录还显式请求 Foundation/Runtime；最终版本由 Gradle 解析结果决定。发生冲突时，检查宿主依赖图并对齐版本，不要用 force 或 exclude 绕过约束。

DynamicAnimation 使用版本目录中的 **strictly** 约束，运动实现依赖该版本的重定向和取消行为。宿主应使用兼容的版本约束。

`coui-material3` 传递引入同版本的 `coui` 与 M3；同时声明两个模块时，使用相同版本。核心库不依赖 Material3 或 Material Icons。宿主直接调用 Material3、Activity 等 API 时，应声明相应依赖；Demo 的 Capsule 依赖无需随库引入。

预览版不保证二进制兼容。升级后重新编译使用库的模块，版本变化见 [CHANGELOG](../../CHANGELOG.md)。主题配置见[外观与预设](appearance.md)。

## 效果门槛

| 能力                                  | 运行边界                                                                 |
|-------------------------------------|----------------------------------------------------------------------|
| 背景源、普通控件、布局与顶栏容器              | 按库最低系统运行；顶栏内部保留明确的版本分支。                                              |
| RuntimeShader 的渐进模糊、轮廓、双主体融合        | 需要 API 33；融合没有内置旧平台替代容器。                                             |
| 常驻 SearchBar、折叠顶栏、纵向回弹 | 按库最低系统运行；常驻搜索在 API 31–33 使用普通填色，API 34+ 使用浮动材质与融合。 |
| 菜单 Fast Kawase                      | 工厂在 API 33 以下返回 null，菜单内部使用既有 Gaussian 分支；该分支不等价于 Fast Kawase。       |
| 跟手光                                 | API 33 以下不提供 Shader 光效回调。 |
| SegmentedControl 分段选择器 | 需要 API 34 和不含自身的背景源，复用公共浮动材质与轮廓。 |
| 浮动底栏、FAB、顶栏按钮/展开式搜索、ChipTab、普通 Mesh 阴影 | 需要 API 34；调用前检查版本，并处理 renderer 创建失败。                                 |
| fast 胶囊 Shader 裁剪                   | API 35 起使用公开 Canvas.clipShader；API 34 保留 RenderNode/RenderEffect 承载。 |

具体门槛以公开声明的 `RequiresApi` 和内部版本分支为准。Shader 还受设备图形后端影响；创建 renderer 失败时，由宿主报告错误并提供替代 UI。
