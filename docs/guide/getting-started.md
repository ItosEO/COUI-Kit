# 快速开始

在已有 Compose 页面中显示一个可调节的 COUI 滑条。可以按下文接入源码模块，或按 [GitHub Packages 指南](github-packages.md)使用已经成功发布的预览版；先确认[环境与平台要求](compatibility.md)。

## 添加模块

独立宿主工程可复制本仓库的 `coui/` 到自己的根目录，并合并 [版本目录](../../gradle/libs.versions.toml) 中该模块引用的库和插件别名。保留库资源、既有许可及构建配置，不要只复制 Kotlin 文件。插件版本由宿主统一声明，避免同一插件出现不同版本。先按当前开发基线对齐，不能省略 Compose 插件或随意放宽 DynamicAnimation 约束。

源码的许可范围见 [LICENSE](../../LICENSE) 与 [NOTICE](../../NOTICE)。复制模块时请一并保留许可通知和已有第三方声明；以修改版或分叉版对外发行时，另遵循[名称与标识使用政策](../../TRADEMARKS.md)。

宿主 `settings.gradle.kts`：

```kotlin
include(":coui")
```

消费者模块 `build.gradle.kts`：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":coui"))
}
```

以上是增量配置，消费者仍需自己的 Android/Compose 插件、SDK 和主题配置。此工程的 [demo/build.gradle.kts](../../demo/build.gradle.kts) 是完整示例，不要求普通宿主复制 Demo 签名、应用标识或 Capsule 依赖。独立消费的验证范围见[兼容要求](compatibility.md#验证范围)。

## 显示第一个组件

下面的函数接收宿主的深浅色与强调色，并在页面内保存当前示例值：

~~~kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.shape.CouiCapsuleShape
import com.itos.coui.slider.CouiSlider
import com.itos.coui.slider.CouiSliderDefaults

@Composable
fun SliderExample(
    modifier: Modifier = Modifier,
    dark: Boolean,
    accent: Color,
) {
    var value by rememberSaveable { mutableFloatStateOf(0.4f) }
    CouiSlider(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = { value = it },
        colors = CouiSliderDefaults.colors(accent, dark),
        dark = dark,
        shape = CouiCapsuleShape,
    )
}
~~~

在 App 中，业务数值及保存操作由宿主状态层持有。步进和提交示例见[滑条](../components/slider.md)。

## 继续接入

- 按[外观与预设](appearance.md)提供配色、字体和 Shape。
- 从[组件目录](../components/README.md)选择菜单、顶栏、底栏等控件。
- 浮动组件先装配[背景采样源](../effects/backdrop.md)，再按组件页放置消费者。
- 查看完整效果与交互，运行 [Demo](demo.md)。
