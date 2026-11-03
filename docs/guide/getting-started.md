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
    implementation("io.github.itoseo:coui:0.0.1-alpha3")
}
```

`0.0.1-alpha3` 需要 Android 12（API 31）及以上，编译时需要 `compileSdk` 37 和 AGP 9.1.0 或更高版本。应用仍需配置自己的 Android 与 Compose 插件。组件的具体系统要求见[兼容要求](compatibility.md)。

当前版本为早期预览版，API 可能发生不兼容的变化。

## 配置页面

纯 COUI 应用可使用 `CouiTheme + CouiScaffold + COUI 组件`，无需添加 Material 3。容器、Padding 和安全区的完整用法见[页面容器](../components/scaffold.md)。已有宿主页面也可以直接放入 COUI 组件。

组件可以直接使用 COUI 原生默认值，也可以由局部 `CouiTheme` 或显式参数配置。M3 宿主添加同版本的可选 `coui-material3` 模块，并在 `Application.onCreate` 调用 `Coui.initialize(defaultStyle = CouiMaterial3Style())`；完整初始化与模式配置见 [README：接入 Material 3](../../README.md#接入-material-3)。

平台要求按能力分：普通控件跟随库的最低系统；渐进模糊、轮廓和融合要 API 33；底栏、FAB、顶栏按钮/搜索、ChipTab 和普通阴影要 API 34。调用 API 34 的组件前先判断版本，完整表格见[兼容要求](compatibility.md#效果门槛)。

## 显示第一个组件

在 Compose 页面中加入下面的滑条，拖动时更新 `value`：

~~~kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itos.coui.slider.CouiSlider

@Composable
fun SliderExample(
    modifier: Modifier = Modifier,
) {
    var value by rememberSaveable { mutableFloatStateOf(0.4f) }
    CouiSlider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = { value = it },
    )
}
~~~

示例把值保存在页面中。要在调整结束后保存，或设置分段和禁用状态，请看[滑条](../components/slider.md)。

## 浮动组件的背景源

顶栏、底栏、FAB 和材质弹窗需要采样页面背景。下面创建一个背景源，并将顶栏放在被采样的页面内容之外：

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.topappbar.CouiTopAppBar

@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    background: Color,
    title: String,
    content: @Composable () -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            content()
        }
        CouiTopAppBar(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
            backdrop = backdrop,
        ) {
            BasicText(title)
        }
    }
}
~~~

背景源只挂一个节点，节点应覆盖不透明的页面内容。顶栏不会自动为内容留出空间，实际页面需要按顶栏高度设置顶部间距。示例文字使用 Foundation；深色页面按页面配色传入文字 Style。

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

源码模块还引用了 Maven Publish 和 Dokka 等插件别名。插件版本由项目统一声明，DynamicAnimation 的 `strictly` 约束也要保留。工具链和依赖对齐见[兼容要求](compatibility.md)。

需要 M3 适配时，同样复制 `coui-material3/`，在 settings 增加 `include(":coui-material3")`，并在调用模块依赖 `project(":coui-material3")`。

[demo/build.gradle.kts](../../demo/build.gradle.kts) 是完整示例，但不用照抄它的签名、应用标识或 Capsule 依赖。

许可范围见 [LICENSE](../../LICENSE) 和 [NOTICE](../../NOTICE)。复制模块时把许可通知和第三方声明一并带走；如果要发行修改版或分叉版，先看[名称与标识使用政策](../../TRADEMARKS.md)。

### GitHub Packages 预览版

需要验证开发快照时，按 [GitHub Packages 指南](github-packages.md)配置仓库、凭据和坐标。源码模块、Maven Central 发行版和 GitHub Packages 快照选择一种即可，同一模块不要同时引入。

## 常见问题

**依赖解析失败或 Compose 编译失败**：检查 Maven Central 和项目的 Compose 插件。使用源码模块时，还要检查模块引用的库、插件别名和 DynamicAnimation 约束。

**组件显示了，但背景材质不对**：检查系统版本，并确认背景源覆盖不透明内容，组件位于源节点之外。材质弹窗还需要传入 `material` 和宿主窗口的背景源。

也可以查看 [Demo](demo.md) 中对应的组件示例。

## 接下来

从[组件目录](../components/README.md)选择需要的控件，或查看[外观与预设](appearance.md)。
