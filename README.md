<div align="center">

# COUI-Kit

面向 Android Jetpack Compose 的 COUI 组件与视觉效果库

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.0.1--alpha2-blue)](https://central.sonatype.com/artifact/io.github.itoseo/coui)
[![Android API 31+](https://img.shields.io/badge/minSdk-31-3DDC84?logo=android)](docs/guide/compatibility.md)
[![MPL 2.0](https://img.shields.io/badge/License-MPL--2.0-blue)](LICENSE)

</div>

COUI-Kit 参考 ColorOS 17 的「凝光视效」，提供背景模糊、材质混色、轮廓光和跟手动效。

## 特点

- 组件和效果可以分别使用，也可以组合使用。
- 支持接入 App 的配色、文字样式和形状，并提供常用外观预设。
- 当前源码提供可选原生主题与组件默认值，核心独立于 Material 3；[版本与迁移](docs/guide/compatibility.md#原生主题与默认参数迁移)。
- 基于 Android、Compose 和 AndroidX 的公开 API，不依赖厂商 SDK 或私有 SO。

## Demo

可以运行仓库中的 `demo` 模块，体验组件和效果，并切换深浅色与外观预设。运行方法和 CI 构建下载方式见 [Demo 指南](docs/guide/demo.md)。

## 开始使用

### 引入依赖

COUI-Kit 已发布到 Maven Central。以下示例使用 Kotlin DSL。

1. 在项目的 `settings.gradle.kts` 中确认已配置 Maven Central：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

2. 在使用 COUI 的模块中启用 Compose，并添加依赖：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha2")
}
```

`0.0.1-alpha2` 的最低运行版本为 Android 12（API 31），编译时需要 `compileSdk` 37 和 AGP 9.1.0 或更高版本。部分组件和效果需要更高的 Android 版本，使用前请查看[兼容要求](docs/guide/compatibility.md#效果门槛)。

当前版本为早期预览版，后续版本可能调整 API。

### 在页面中使用

添加依赖后，请参考[快速开始](docs/guide/getting-started.md)配置页面并运行示例。

## 文档

- [组件目录](docs/components/README.md)
- [效果目录](docs/effects/README.md)
- [兼容要求](docs/guide/compatibility.md)
- [外观与预设](docs/guide/appearance.md)
- [更新记录](CHANGELOG.md)
- [GitHub Packages 开发快照](docs/guide/github-packages.md)
- [更新记录与版本映射](CHANGELOG.md)

## 反馈与贡献

遇到问题，可以先查看[常见问题](docs/guide/getting-started.md#常见问题)。反馈时请附上库版本、Android 版本、复现步骤和相关日志；视觉问题请同时提供截图或录屏。

欢迎提交修复、组件、效果或文档改进，参与开发请阅读[贡献指南](CONTRIBUTING.md)。

## 许可与名称使用

本项目自有源码采用 [MPL 2.0](LICENSE)，第三方内容保留各自许可与版权声明，具体范围见 [NOTICE](NOTICE)。

ColorOS 及相关名称归 OPPO 所有；本项目与 OPPO 没有隶属、授权或背书关系。

修改版和分叉版的发行名称、Logo 及来源说明遵循[名称与标识使用政策](TRADEMARKS.md)。
