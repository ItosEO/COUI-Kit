<div align="center">

# COUI-Kit

面向 Android Jetpack Compose 的 COUI 组件与视觉效果库

[![Maven Central](https://img.shields.io/maven-central/v/io.github.itoseo/coui?label=Maven%20Central&color=blue)](https://central.sonatype.com/artifact/io.github.itoseo/coui)
[![Android API 31+](https://img.shields.io/badge/minSdk-31-3DDC84?logo=android)](docs/guide/compatibility.md)
[![MPL 2.0](https://img.shields.io/badge/License-MPL--2.0-blue)](LICENSE)

</div>

COUI-Kit 参考 ColorOS 17 的「凝光视效」，提供背景模糊、材质混色、轮廓光和跟手动效。

## 特点

- 组件和效果可以分别使用，也可以组合使用。
- 支持接入 App 的配色、文字样式和形状，并提供常用外观预设。
- 当前源码提供原生主题、组件默认值与可选 `coui-material3` 适配模块。核心独立于 Material 3；M3 宿主只需启动初始化，见[接入与外观](docs/guide/appearance.md)和[版本与迁移](docs/guide/compatibility.md#原生主题与默认参数迁移)。
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

### 接入 Material 3

已有 Material 3 App 可以让 COUI 的默认配色和字体跟随当前 `MaterialTheme`，插槽中的 M3 `Text`、`Icon` 直接继承组件样式，无需逐处接线或增加根主题包装。

`coui` 与 `coui-material3` 采用相同版本并同步发布。通过 Maven Central 接入 M3 时，只需添加适配模块：

```kotlin
val couiVersion = "<已发布版本>"

dependencies {
    implementation("io.github.itoseo:coui-material3:$couiVersion")
}
```

`<已发布版本>` 是占位符，需替换为同时包含两个模块的实际版本。本分支新增的适配模块尚未发行，不能填写上面的 `0.0.1-alpha2`。适配模块会传递引入同版本核心与 M3；如果同时显式声明 `coui`，也使用该版本。

包含适配模块的开发快照发布后，也可通过 [GitHub Packages](docs/guide/github-packages.md) 接入。需要本地修改库时，按[源码模块接入](docs/guide/getting-started.md#源码模块)配置工程；`project(":coui-material3")` 仅用于这种情况。

在已有 `Application.onCreate()` 中、创建 Compose UI 前初始化一次：

```kotlin
import android.app.Application
import com.itos.coui.material3.CouiMaterial3Style
import com.itos.coui.theme.Coui

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Coui.initialize(defaultStyle = CouiMaterial3Style())
    }
}
```

已有 Application 只需加入初始化；新建 Application 时需在 manifest 中注册。初始化选择固定策略，重复调用会报错。组件在各自的 Compose 作用域读取 M3 配色与字体，随动态色、主题动画和嵌套主题更新。

默认深浅模式跟随系统。App 有独立主题开关时，在现有主题 Provider 中提供实际的 `darkTheme`：

```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import com.itos.coui.theme.LocalCouiDarkTheme

CompositionLocalProvider(LocalCouiDarkTheme provides darkTheme) {
    content()
}
```

`darkTheme` 和 `content` 来自宿主主题。嵌套 `MaterialTheme` 若独立改变深浅模式，也在该边界提供此 Local；库不会从自定义色板猜测模式。显式组件参数继续优先，局部原生主题、字体与染色规则见[外观与预设](docs/guide/appearance.md)。

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
