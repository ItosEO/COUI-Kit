# 消费者兼容要求

接入源码模块或 GitHub Packages 预览版时，先对齐本工程开发基线，再检查所用效果的 Android 门槛。独立消费者与发行产物的验证范围见本页末尾。

## Android 与工具链

| 项目                   | 当前要求与依据                                                                                                                                                                  |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 运行系统                 | `minSdk` 以 [库配置](../../coui/build.gradle.kts) 为准；具体效果还受下表的平台门槛约束。                                                                                                        |
| 编译 SDK               | 不低于库 `aarMetadata.minCompileSdk`。该值与当前 Compose AAR 的要求一致。                                                                                                                |
| 消费者 AGP              | 不低于 `aarMetadata.minAgpVersion`，这是当前依赖元数据的必要下限，不是完整接入验证结果。                                                                                                               |
| Java 字节码             | 库配置使用 Java 17。Gradle 运行 JVM 按所用 AGP/Gradle 要求选择，不能把字节码目标当作运行 JVM 要求。                                                                                                     |
| Kotlin 与 Compose 编译器 | 源码接入先对齐 [版本目录](../../gradle/libs.versions.toml) 的开发基线，并配套 Compose 编译插件。更低 Kotlin 版本的元数据消费与 Compose 编译兼容尚未确认，不声明最低版本。                                                     |
| 本工程工具链               | [Wrapper](../../gradle/wrapper/gradle-wrapper.properties)、[Gradle JVM](../../gradle/gradle-daemon-jvm.properties) 和版本目录是开发基线。使用 AGP 内置 Kotlin；消费者不必仅因本工程配置而照搬全部 Demo 插件。 |

`minCompileSdk` 与 AGP 下限已核对本地缓存中对应版本的 Compose UI、Foundation 和 Material3 AAR 的 `META-INF/com/android/build/gradle/aar-metadata.properties`。生成 AAR 时会带出库自身要求；实际发行 AAR 尚未完成验证。库的 `minSdk` 仍由 manifest 合并表达，不使用 overrideLibrary 强行绕过。

## 依赖对齐

库通过 `api(platform(...))` 提供 Compose alpha BOM，并公开 UI、Animation、Foundation 依赖。版本目录还显式请求 Foundation/Runtime；BOM 约束可能在依赖解析中选出更高版本，不能只看某一条 version.ref 就断言最终解析版本。宿主需要检查其实际依赖图，不要用 force、exclude 或另一组不匹配的 Compose 版本绕过冲突。

DynamicAnimation 保持版本目录中的 **strictly** 约束。持续重定向、取消和 pending 目标等行为是当前运动实现的一部分。宿主若要求不兼容的严格版本，应暴露冲突并评审整条运动链；不能直接放宽锁定，也不能以“已解析”为由声称动画等价。

Material3 与图标依赖仍供库内部装配使用。宿主自己调用 Material3、Activity 或其他 API 时，应直接声明相应依赖。`capsule` 只属于 Demo 的 G2 装配，不是 COUI 的公共或运行依赖。库不依赖外部业务工程、Hilt、宿主日志框架、第三方 Backdrop 或厂商 SDK。

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
