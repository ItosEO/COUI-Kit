# 快速开始

在已有的 Jetpack Compose Android 项目中添加 COUI 依赖，即可使用滑条、开关、弹窗、顶栏等组件。本页使用 Gradle Kotlin DSL 说明接入步骤。

- **最低运行系统**：Android 12（API 31），部分高级效果与浮动组件需要 API 33 或 API 34+。
- **编译环境基线**：`compileSdk` 37、AGP `9.1.0` 及以上，并启用 Jetpack Compose。完整环境要求见[兼容要求](compatibility.md)。
- **预览阶段说明**：当前版本处于早期预览期，API 可能发生不兼容调整。

## 引入 COUI

同一模块请仅选择以下**一种**依赖来源，避免重复定义类。

### Maven Central

1. 在 `settings.gradle.kts` 确认已启用 Google 与 Maven Central 仓库：

   ~~~kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
       }
   }
   ~~~

2. 在使用组件的模块 `build.gradle.kts` 中启用 Compose 并添加依赖：

   ~~~kotlin
   android {
       buildFeatures { compose = true }
   }

   dependencies {
       implementation("io.github.itoseo:coui:0.0.1-alpha12")
   }
   ~~~

   需要自动跟随宿主 `MaterialTheme` 时，可改为引入同版本的 `io.github.itoseo:coui-material3:0.0.1-alpha12`，详见[接入 Material 3](../../README.md#接入-material-3)；需要 `CouiIcons` 时可额外添加同版本的 `io.github.itoseo:coui-icons:0.0.1-alpha12`，见[图标用法](../components/icons.md)。

### GitHub Packages 预览版

需要验证开发快照时，按 [GitHub Packages 指南](github-packages.md)配置凭据、仓库与快照坐标。

### 源码模块

需要在本地直接修改库源码时，按以下步骤引入源码模块：

1. 将仓库中的 `coui/` 目录复制到应用工程根目录。如需 Material 3 适配，同时复制 `coui-material3/`；如需 `CouiIcons`，同时复制 `coui-icons/`（`coui-icons` 依赖 `coui` 读取主题，仅使用核心组件时不需要它）。
2. 合并 [版本目录](../../gradle/libs.versions.toml) 中该模块引用的依赖库与插件别名（包括 `Maven Publish`、`Dokka` 等插件别名；插件与依赖版本由项目统一声明）。工具链与依赖对齐见[兼容要求](compatibility.md)。
3. 保留源码模块的资源、`LICENSE`、`NOTICE` 与第三方声明（发行修改版或分叉版需遵守[名称与标识使用政策](../../TRADEMARKS.md)）。
4. 在 `settings.gradle.kts` 中引入模块：

   ~~~kotlin
   include(":coui")
   // 按需追加可选模块：
   // include(":coui-material3")
   // include(":coui-icons")
   ~~~

5. 在使用组件的模块中启用 Compose 并添加项目依赖（完整参考见 [demo/build.gradle.kts](../../demo/build.gradle.kts)，无需照抄其签名、应用标识或 `Capsule` 依赖）：

   ~~~kotlin
   android {
       buildFeatures { compose = true }
   }

   dependencies {
       implementation(project(":coui"))
       // 按需追加可选模块：
       // implementation(project(":coui-material3"))
       // implementation(project(":coui-icons"))
   }
   ~~~

## 配置页面

根据宿主应用的现有架构选择页面装配方式：

| 场景 | 推荐装配方式 | 参考文档 |
| --- | --- | --- |
| 纯 COUI 应用 | `CouiTheme` + `Scaffold` + COUI 组件，无需 Material 3 | [页面容器](../components/scaffold.md) |
| 已有 Material 3 App | 引入 `coui-material3` 并初始化 `CouiMaterial3Style()`，在现有布局中直接使用组件 | [接入 Material 3](../../README.md#接入-material-3) |
| Miuix / 自有容器混搭 | 在已有布局中放入 COUI 组件，按需显式挂载 `Backdrop` | [宿主页面接入](secondary-pages.md) |

调用高版本组件前需做好系统版本判断（完整能力对照见[兼容要求](compatibility.md#效果门槛)）：

- **API 31+**：普通控件、页面容器与顶栏容器；常驻 `SearchBar` 在 API 31–33 使用普通填色。
- **API 33+**：渐进模糊、轮廓光、融球及菜单 Fast Kawase 模糊。
- **API 34+**：浮动 `NavigationBar`、`Fab`、`TopAppBarButton`、展开式顶栏搜索、`ChipTab`、`SegmentedControl`、常驻 `SearchBar` 浮动材质与普通阴影。

## 显示第一个组件

在 Compose 页面中添加下面的滑条示例，滑条值由页面通过 `rememberSaveable` 持有并在 `onValueChange` 中更新：

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

分段刻度、松手再提交及禁用态配置见[滑条](../components/slider.md)。

## 浮动组件的背景源

使用 COUI `Scaffold` 时，容器会自动录制正文视口作为背景源，供 `topBar`、`bottomBar` 和 `floatingActionButton` 槽位内的浮动组件采样，无需手动创建或传递 `Backdrop`：

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            content()
        }
    }
}
~~~

背景源装配规则如下：

- **自动作用域与正文要求**：`Scaffold` 只录制完整正文视口与 `containerColor`（不包含顶栏、底栏和 `FAB`），采样本身不需要全局初始化。正文必须应用并 `consumeWindowInsets(padding)`；`TopAppBar` 会读取槽位提供的 `LocalCouiTextStyle` 与主题内容色，并处理顶部及横向安全区。
- **正文内部的模糊组件**：正文 `content` 内部的模糊组件（如 `ChipTab`）不能采样自身，需另建独立的兄弟 `Backdrop` 源。
- **第三方容器**：Material 3 `Scaffold`、Miuix 或自定义容器需手动创建源、挂载 `.couiBackdrop(backdrop)` 并显式传给浮动组件，见[宿主页面接入](secondary-pages.md)。
- **独立窗口浮层**：菜单的窗口源使用 `ProvideCouiMenuBackdrop`；材质弹窗的用法见[弹窗](../components/dialog.md#使用背景材质)。资源生命周期与失败处理详见[背景采样](../effects/backdrop.md)。

## 常见问题

| 问题现象 | 排查要点 |
| --- | --- |
| 依赖解析失败或 Compose 编译失败 | 检查 `settings.gradle.kts` 是否配置 `mavenCentral()` 及模块是否启用 `buildFeatures.compose`；源码接入时检查版本目录依赖、插件别名与 `DynamicAnimation` 解析版本。 |
| 组件显示了，但背景材质不对或无模糊 | 核对设备系统版本（API 33+/34+）；确认背景源覆盖不透明内容且组件位于源节点**之外**；材质弹窗还须传入 `material` 与宿主窗口背景源。 |
| 低版本设备调用高版本组件崩溃 | 确认调用 `Fab`、`NavigationBar`、`TopAppBarButton`、`ChipTab`、`SegmentedControl` 等 `@RequiresApi(34)` 入口前已判断 `Build.VERSION.SDK_INT >= 34`。 |

也可在 [Demo](demo.md) 中查看对应组件的完整运行示例。

## 接下来

- 挑选并装配控件：[组件目录](../components/README.md)
- 定制主题、字体与染色：[外观与预设](appearance.md)
