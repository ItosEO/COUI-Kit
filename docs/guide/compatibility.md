# 消费者兼容要求

接入 COUI 前，请先核对编译工具链基线，再确认所用组件与效果的 Android 系统版本门槛。

## Android 与工具链

| 项目 | 当前要求与依据 |
| --- | --- |
| 运行系统 | `minSdk` 以[库配置](../../coui/build.gradle.kts)为准（当前为 API 31）；具体效果还受下方平台门槛约束。 |
| 编译 SDK | 不低于库 `aarMetadata.minCompileSdk`（当前为 37），与当前 Compose AAR 要求一致。 |
| 消费者 AGP | 不低于库 `aarMetadata.minAgpVersion`（当前为 `9.1.0`）。 |
| Java 字节码 | 库编译目标使用 Java 17。Gradle 运行 JVM 按所用 AGP / Gradle 要求选择，不能把字节码目标当作运行 JVM 要求。 |
| Kotlin 与 Compose 编译器 | 源码接入先对齐[版本目录](../../gradle/libs.versions.toml)的开发基线，并配套 Compose 编译插件。更低 Kotlin 版本的元数据消费与 Compose 编译兼容尚未确认，不声明最低版本。 |
| 本工程工具链 | [Wrapper](../../gradle/wrapper/gradle-wrapper.properties)、[Gradle JVM](../../gradle/gradle-daemon-jvm.properties) 和版本目录为开发基线，使用 AGP 内置 Kotlin；消费者无需照搬全部 Demo 插件。 |

> [!IMPORTANT]
> 发行版的编译要求以对应 AAR 元数据为准。库的 `minSdk` 通过 manifest 合并检查，不要使用 `overrideLibrary` 绕过。

## 依赖对齐

- **Compose 传递依赖**：核心库通过 `api(platform(...))` 提供 Compose alpha BOM，并公开 UI、Animation、Foundation 依赖；版本目录另显式请求 Foundation/Runtime，最终版本由 Gradle 解析结果决定。发生版本冲突时请检查宿主依赖图并对齐版本，不要使用 `force` 或 `exclude` 绕过约束。
- **`DynamicAnimation` 依赖对齐**：核心库请求 AndroidX `DynamicAnimation` `1.1.0`（Gradle 按宿主依赖图解析版本，例如与同样请求 `1.1.0` 的 Material Components for Android 共用）；取消后重启行为见[主动控制运动](../effects/motion.md#主动控制运动)。
- **可选 Material 3 与图标模块**：`coui-material3` 会传递引入同版本的 `coui` 与 Material 3；`CouiIcons` 位于可选的 `coui-icons` 模块（依赖 `coui` 读取主题，核心不依赖图标模块）。同时声明多个模块时必须保持相同版本；核心库本身不依赖 Material 3 或 Material Icons。
- **宿主自备依赖**：宿主直接调用 Material 3、Activity 等 API 时须自行声明相应依赖；Demo 的 `Capsule` 依赖无需随库引入。
- **预览版兼容策略**：当前处于早期预览期，不保证二进制兼容，升级后请重新编译使用库的模块；版本变化见 [CHANGELOG](../../CHANGELOG.md)，主题配置见[外观与预设](appearance.md)。

## 效果门槛

| 能力 / 组件 | 运行边界 |
| --- | --- |
| 背景源、普通控件、布局与顶栏容器 | 按库最低系统（API 31+）运行；顶栏内部保留明确的版本分支。 |
| `RuntimeShader` 渐进模糊、轮廓光、双主体融合 | 需要 **API 33+**；双主体融合没有内置旧平台替代容器。 |
| 常驻 `SearchBar`、折叠顶栏、纵向回弹 | 按库最低系统（API 31+）运行；常驻 `SearchBar` 在 API 31–33 使用普通填色，API 34+ 自动启用浮动材质与融合。 |
| 菜单 Fast Kawase 模糊 | 工厂在 API 33 以下返回 `null`，菜单内部使用既有 Gaussian 分支（该分支不等价于 Fast Kawase）。 |
| 跟手光（`Spotlight`） | API 33 以下不提供 Shader 光效回调。 |
| 浮动 `NavigationBar`、`Fab`、`TopAppBarButton`、顶栏展开式搜索、`ChipTab`、`SegmentedControl`、普通 Mesh 阴影 | 需要 **API 34+**（标记 `@RequiresApi(34)`）；调用前须检查系统版本，并处理 `renderer` 创建失败。 |
| fast 胶囊 Shader 裁剪 | API 35+ 使用公开 `Canvas.clipShader`；API 34 保留 `RenderNode` / `RenderEffect` 承载。 |

具体门槛以公开声明的 `@RequiresApi` 与内部版本分支为准。Shader 渲染还受设备图形后端影响；创建 `renderer` 失败时，由宿主报告错误并提供替代 UI。
