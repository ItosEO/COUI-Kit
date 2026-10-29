# 🏠快速开始

COUI 目前提供源码模块和 GitHub Packages 预览版两种接入方式，请二选一，不要同时依赖。

## 引入 COUI

### 源码模块

1) 把仓库里的 `coui/` 复制到应用工程根目录。库资源、许可和构建配置要一起保留，只复制 Kotlin 文件会缺资源。

2) 在 `settings.gradle.kts` 中加入模块：

```kotlin
include(":coui")
```

3) 在消费者模块的 `build.gradle.kts` 中打开 Compose，并依赖该模块：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":coui"))
}
```

上面只是增量配置，应用自己的 Android/Compose 插件、SDK 和主题仍然要有。版本目录里的 Compose 编译插件和 DynamicAnimation 约束也要一起合并，否则会出现编译或运行时找不到类的问题。

[demo/build.gradle.kts](../../demo/build.gradle.kts) 是完整示例，但不用照抄它的签名、应用标识或 Capsule 依赖。

许可范围见 [LICENSE](../../LICENSE) 和 [NOTICE](../../NOTICE)。复制模块时把许可通知和第三方声明一并带走；如果要发行修改版或分叉版，先看[名称与标识使用政策](../../TRADEMARKS.md)。

### GitHub Packages 预览版

需要验证还没正式发行的版本时，按 [GitHub Packages 指南](github-packages.md)配置仓库、凭据和坐标。预览版坐标带 `-SNAPSHOT` 和提交短 SHA，适合临时验证，不要和源码模块同时出现在一个模块里。

## 配置页面

COUI 不读 MaterialTheme。使用前需要由页面提供深浅色、强调色、内容色、字体和 Shape，固定参数从各组件的 Defaults 取。Demo 的配色只是演示，不要当成库的默认值。

平台要求按能力分：普通控件跟随库的最低系统；渐进模糊、轮廓和融合要 API 33；底栏、FAB、顶栏按钮/搜索、ChipTab 和普通阴影要 API 34。调用 API 34 的组件前先判断版本，完整表格见[兼容要求](compatibility.md#效果门槛)。

## 显示第一个组件

滑条不采样背景，适合先跑通接入。使用以下代码显示一个可调节的滑条：

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
        colors = CouiSliderDefaults.colors(primary = accent, dark = dark),
        dark = dark,
        shape = CouiCapsuleShape,
    )
}
~~~

值的保存、上报和确认由页面自己做，滑条只回调当前值。分段、隐藏步进和禁用见[滑条](../components/slider.md)。

## 浮动组件的背景源

顶栏、底栏、FAB、菜单和材质弹窗都要采样页面背景。先在窗口里建源，再把 `Modifier.couiBackdrop(source)` 挂到一个不透明的内容节点上：

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop

@Composable
fun PageContent(
    modifier: Modifier = Modifier,
    background: Color,
    content: @Composable () -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(background)) {
            content()
        }
        // 顶栏、底栏、FAB 等消费者放在这里，把同一个 backdrop 传进去。
    }
}
~~~

请注意，源只能挂一个节点，组件要放在源子树外。菜单用 `ProvideCouiMenuBackdrop` 返回的 Modifier 挂到窗口内容上；弹窗用自己窗口的源。每个绘制节点单独持有 renderer，单主体用 `CouiFloatingRenderer.standalone()`，底栏用 `CouiFloatingRenderer()`，不要跨节点共用。

API 34 以下或 renderer 创建失败时，由页面显示错误或降级 UI。坐标和失败处理的细节见[背景采样](../effects/backdrop.md)。

## 常见问题

如果你遇到问题，可以按下面顺序排查：

1. 依赖解析不到或 Compose 编译失败，先检查版本目录里的 Compose 编译插件和 DynamicAnimation 约束是否一起合并了。
2. 组件能显示但没有模糊或材质，检查运行版本，确认背景源挂在真正的不透明页面上，组件又在源子树外。
3. 材质弹窗没有效果，确认同时传了 `material` 和所属窗口的 `backdrop`，不要只改窗口背景。

还是没解决的话，先在 [Demo](demo.md)里找同族示例，对比自己的接入方式。

## 接下来

外观配置见[外观与预设](appearance.md)，控件从[组件目录](../components/README.md)挑。浮动组件建议先读[背景采样](../effects/backdrop.md)再动手。
