<div align="center">

# COUI-Kit

面向 Android Jetpack Compose 的 COUI 组件与视觉效果库

[![Maven Central](https://img.shields.io/maven-central/v/io.github.itoseo/coui?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/io.github.itoseo/coui)
[![Android API 31+](https://img.shields.io/badge/minSdk-31-3DDC84?logo=android)](docs/guide/compatibility.md)
[![MPL 2.0](https://img.shields.io/badge/License-MPL--2.0-blue)](LICENSE)
[![Telegram 讨论群组](https://img.shields.io/badge/Telegram-%E8%AE%A8%E8%AE%BA%E7%BE%A4%E7%BB%84-26A5E4?logo=telegram&logoColor=white)](https://t.me/metric_official/2949)

</div>

COUI-Kit 参考 ColorOS 17 的「凝光视效」，提供背景模糊、材质混色、轮廓光、跟手动效以及常用组件。

## 特点

- **按需组合**：组件和效果既可独立取用，也可组合装配；可与 Material 3、Miuix 或自有设计体系混搭，无需替换宿主的 `Scaffold`、主题与导航。
- **纯 COUI 开箱即用**：提供原生 `CouiTheme`、组件默认值与可选 `Scaffold`（统一栏位测量与安全区），核心模块不依赖 Material 3。
- **可选 Material 3 适配**：通过可选 `coui-material3` 模块在启动时初始化适配，让 COUI 默认配色与字体随宿主 `MaterialTheme` 自动更新，见[外观与预设](docs/guide/appearance.md)。
- **灵活定制**：支持接入 App 自定义配色、文字样式与 `Shape`，同时提供原生浅色/深色预设。
- **全系统可用**：仅基于 Android、Compose 和 AndroidX 公开 API 实现，不依赖厂商 SDK 或私有 SO；支持 Android 12 及以上设备，部分效果要求更高版本。

## Demo

运行仓库中的 `demo` 模块，可按目录浏览组件与效果详情，并切换深浅色、Material 配色与外观预设。运行方式、操作说明及 CI 构建下载见 [Demo 指南](docs/guide/demo.md)。

## 开始使用

### 引入依赖

在已启用 Compose 的模块中添加核心依赖（最低支持 Android 12 / API 31）：

~~~kotlin
dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha11")
}
~~~

- 仓库配置、工具链要求及完整上手示例见[快速开始](docs/guide/getting-started.md)。
- 部分高阶材质、模糊与浮动组件需要更高 Android 版本，详见[兼容要求](docs/guide/compatibility.md#效果门槛)。
- 使用 `CouiIcons` 时，添加可选的图标依赖，详见[图标用法](docs/components/icons.md)。

### 接入 Material 3

已有 Material 3 App 可引入可选的 `coui-material3` 适配模块，让 COUI 组件的默认配色、字体族和内容插槽自动跟随当前 `MaterialTheme`（插槽中的 M3 `Text`、`Icon` 可直接继承样式，无需逐处接线或包一层根主题）。

1. **添加适配依赖**（`coui`、`coui-material3` 与 `coui-icons` 采用相同版本并同步发布；`coui-material3` 会传递引入同版本核心库，若显式声明 `coui` 须保持同一版本）：

   ~~~kotlin
   val couiVersion = "0.0.1-alpha11"

   dependencies {
       implementation("io.github.itoseo:coui-material3:$couiVersion")
   }
   ~~~

   > [!TIP]
   > 协作调试开发快照可使用 [GitHub Packages](docs/guide/github-packages.md)；需要在本地修改库源码时按[源码模块](docs/guide/getting-started.md#源码模块)配置（`project(":coui-material3")` 仅用于源码接入）。

2. **在 `Application.onCreate()` 初始化一次**（创建 Compose UI 前调用，并在 `AndroidManifest.xml` 注册该 `Application`；策略初始化后不可重复调用）：

   ~~~kotlin
   class App : Application() {
       override fun onCreate() {
           super.onCreate()
           Coui.initialize(defaultStyle = CouiMaterial3Style())
       }
   }
   ~~~

   组件会在各自的 Compose 作用域中读取当前 `MaterialTheme`，随动态色、主题动画和嵌套主题自动更新。

3. **提供独立的深浅模式开关（可选）**：默认深浅模式跟随系统。如果 App 拥有独立主题开关，或嵌套 `MaterialTheme` 独立切换了深浅模式，需在对应 `@Composable` 主题作用域边界提供 `LocalCouiDarkTheme`（库不会从自定义色板猜测深浅模式）：

   ~~~kotlin
   // 在宿主主题 Provider 的 @Composable 作用域中提供实际模式
   CompositionLocalProvider(LocalCouiDarkTheme provides darkTheme) {
       content()
   }
   ~~~

显式组件参数始终优先；局部原生主题、字体映射与染色规则见[外观与预设](docs/guide/appearance.md)。

## 文档

| 文档 | 说明 |
| --- | --- |
| [快速开始](docs/guide/getting-started.md) | 依赖配置、第一个组件、背景源与常见问题 |
| [组件目录](docs/components/README.md) | 布局容器、顶栏/底栏、搜索、开关、滑条、弹窗、分段选择器与图标 |
| [图标用法](docs/components/icons.md) | `coui-icons` 矢量与位图图标集、日夜资源与指定颜色 |
| [效果目录](docs/effects/README.md) | 背景采样、模糊材质、轮廓光、跟手光、投影、融球与回弹 |
| [外观与预设](docs/guide/appearance.md) | 原生主题、M3 角色映射、主题优先级、插槽与染色规则 |
| [宿主页面接入](docs/guide/secondary-pages.md) | 在 Material / 自有布局中组合 COUI、Padding/Insets 与采样源分层 |
| [兼容要求](docs/guide/compatibility.md) | 工具链基线、依赖对齐与各能力 Android 版本门槛 |
| [Demo 指南](docs/guide/demo.md) | 运行、签名配置与页面示例说明 |
| [与原厂的定制差异](docs/guide/customizations.md) | 相对参考版本的视觉与交互微调及作用范围 |
| [GitHub Packages 开发快照](docs/guide/github-packages.md) | 配置快照仓库、凭据与 CI 下载 |
| [更新记录与版本映射](CHANGELOG.md) | 版本变更与旧版命名迁移 |

## 反馈与贡献

- 遇到接入或效果问题可先排查[常见问题](docs/guide/getting-started.md#常见问题)。
- 反馈问题时请附上库版本、Android 版本、复现步骤和相关日志；视觉问题请同时提供截图或录屏。
- 欢迎提交修复、组件、效果或文档改进，参与开发请先阅读[贡献指南](CONTRIBUTING.md)。

## 许可与名称使用

- 本项目自有源码采用 [MPL 2.0](LICENSE)，第三方内容保留各自许可与版权声明，具体范围见 [NOTICE](NOTICE)。
- ColorOS 及相关名称归 OPPO 所有；本项目与 OPPO 没有隶属、授权或背书关系。
- 修改版和分叉版的发行名称、Logo 及来源说明遵循[名称与标识使用政策](TRADEMARKS.md)。

---

[![Star History Chart](https://api.star-history.com/svg?repos=ItosEO/COUI-Kit&type=Date)](https://star-history.com/#ItosEO/COUI-Kit&Date)
