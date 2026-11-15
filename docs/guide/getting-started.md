# 🏠快速开始

在已有的 Compose 项目中添加 COUI 依赖，就可以使用滑条、开关、弹窗等组件。以下示例使用 Kotlin DSL。

## 引入 COUI

### Maven Central

1. 确认项目的 `settings.gradle.kts` 包含 Maven Central：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

2. 在使用组件的模块中添加依赖，并启用 Compose：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha11")
}
```

`0.0.1-alpha11` 需要 Android 12（API 31）及以上，编译时需要 `compileSdk` 37 和 AGP 9.1.0 或更高版本。应用仍需配置自己的 Android 与 Compose 插件。组件的具体系统要求见[兼容要求](compatibility.md)。

当前版本为早期预览版，API 可能发生不兼容的变化。

## 配置页面

纯 COUI 应用可使用 `CouiTheme + Scaffold + COUI 组件`，无需添加 Material 3。容器、Padding 和安全区的完整用法见[页面容器](../components/scaffold.md)。已有宿主页面也可以直接放入 COUI 组件。

组件可以直接使用 COUI 原生默认值，也可以由局部 `CouiTheme` 或显式参数配置。M3 宿主添加同版本的可选 `coui-material3` 模块，并在 `Application.onCreate` 调用 `Coui.initialize(defaultStyle = CouiMaterial3Style())`；完整初始化与模式配置见 [README：接入 Material 3](../../README.md#接入-material-3)。

平台要求按能力分：普通控件跟随库的最低系统；渐进模糊、轮廓和融合要 API 33；底栏、FAB、顶栏按钮/展开式搜索、ChipTab 和普通阴影要 API 34。常驻 `SearchBar` 在 API 31–33 使用普通填色，API 34 起提供浮动材质与融合。调用 API 34 的组件前先判断版本，完整表格见[兼容要求](compatibility.md#效果门槛)。

## 显示第一个组件

在 Compose 页面中加入下面的滑条，拖动时更新 `value`。示例使用 `com.itos.coui.slider.Slider`：

~~~kotlin
@Composable
fun SliderExample(
    modifier: Modifier = Modifier,
) {
    var value by rememberSaveable { mutableFloatStateOf(0.4f) }
    Slider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = { value = it },
    )
}
~~~

示例把值保存在页面中。要在调整结束后保存，或设置分段和禁用状态，请看[滑条](../components/slider.md)。

## 浮动组件的背景源

COUI `Scaffold` 自动录制正文背景，供顶栏、底栏和 FAB 槽位使用，无需创建、挂载或传递 Backdrop。下面使用 COUI Scaffold 与顶栏；采样本身不需要全局初始化：

~~~kotlin
@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    background: Color,
    title: String,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { BasicText(title, style = LocalCouiTextStyle.current) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding).consumeWindowInsets(padding)) {
            content()
        }
    }
}
~~~

容器录制完整正文视口与 `containerColor`，不包含顶栏、底栏和 FAB。正文需应用并消费 Padding。正文内部的模糊效果需使用独立源，不能采样自身。Material 等第三方容器需手动创建源、挂载源并显式传参，见[宿主页面接入](secondary-pages.md)。

`TopAppBar` 提供标题字体和主题内容色，并处理顶部及横向安全区。示例文字使用 Foundation，通过 `LocalCouiTextStyle` 读取槽位样式。

菜单的背景源使用 `ProvideCouiMenuBackdrop`；材质弹窗的用法见[弹窗](../components/dialog.md#使用背景材质)。背景源、renderer 和失败处理的细节见[背景采样](../effects/backdrop.md)。

## 其他接入方式

### 源码模块

需要直接修改库时，可以使用源码模块。把仓库里的 `coui/` 复制到应用工程根目录，并合并[版本目录](../../gradle/libs.versions.toml)中该模块引用的库和插件别名。请同时保留资源、许可和构建配置。

在 `settings.gradle.kts` 中加入模块：

```kotlin
include(":coui")
```

在使用组件的模块中启用 Compose，并依赖源码模块：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":coui"))
}
```

源码模块还引用了 Maven Publish 和 Dokka 等插件别名。插件与依赖版本由项目统一声明。工具链和依赖对齐见[兼容要求](compatibility.md)。

需要 M3 适配时，同样复制 `coui-material3/`，在 settings 增加 `include(":coui-material3")`，并在调用模块依赖 `project(":coui-material3")`。

需要 `CouiIcons` 时，复制 `coui-icons/`，在 settings 增加 `include(":coui-icons")`，并在调用模块依赖 `project(":coui-icons")`。该模块依赖 `coui` 读取主题；只使用核心组件时不需要它。Maven 坐标及使用方式见[图标用法](../components/icons.md)。

[demo/build.gradle.kts](../../demo/build.gradle.kts) 是完整示例，但不用照抄它的签名、应用标识或 Capsule 依赖。

许可范围见 [LICENSE](../../LICENSE) 和 [NOTICE](../../NOTICE)。复制模块时把许可通知和第三方声明一并带走；如果要发行修改版或分叉版，先看[名称与标识使用政策](../../TRADEMARKS.md)。

### GitHub Packages 预览版

需要验证开发快照时，按 [GitHub Packages 指南](github-packages.md)配置仓库、凭据和坐标。源码模块、Maven Central 发行版和 GitHub Packages 快照选择一种即可，同一模块不要同时引入。

## 常见问题

**依赖解析失败或 Compose 编译失败**：检查 Maven Central 和项目的 Compose 插件。使用源码模块时，还要检查模块引用的库、插件别名和 DynamicAnimation 解析版本。

**组件显示了，但背景材质不对**：检查系统版本，并确认背景源覆盖不透明内容，组件位于源节点之外。材质弹窗还需要传入 `material` 和宿主窗口的背景源。

也可以查看 [Demo](demo.md) 中对应的组件示例。

## 接下来

从[组件目录](../components/README.md)选择需要的控件，或查看[外观与预设](appearance.md)。
