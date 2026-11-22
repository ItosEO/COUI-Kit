# 安装与接入分册（依赖 / 工具链 / 可选模块 / 认证）

本分册规则出自 COUI-Kit 仓库内文件，每条标注出处。路径均相对仓库根目录。

## 1. 发布渠道与坐标

| 渠道 | 坐标 / 用途 | 出处 |
| --- | --- | --- |
| Maven Central（正式发行/预览版） | `io.github.itoseo:coui:<版本>`；可选 `io.github.itoseo:coui-material3:<版本>`、`io.github.itoseo:coui-icons:<版本>` | `README.md`、`docs/guide/getting-started.md` |
| GitHub Packages（开发快照） | 同名坐标 + 带短 SHA 的 `-SNAPSHOT`，需认证 | `docs/guide/github-packages.md`、`docs/development/publishing.md` |
| 源码模块 | `project(":coui")` / `project(":coui-material3")` / `project(":coui-icons")` | `getting-started.md#源码模块` |

- 当前文档示例版本为 `0.0.1-alpha15`（`README.md`、`getting-started.md`、`demo/build.gradle.kts` 的 `versionName`、`CHANGELOG.md` 最新条目）。开发基础版本 `VERSION_NAME=0.1.0-SNAPSHOT`（`gradle.properties`）。实际接入版本以 Maven Central 远端解析与发行元数据为准（`publishing.md`）。
- 同一模块只选择一种依赖来源，避免重复定义类；不同渠道不可在同一模块重复引入（`getting-started.md`、`publishing.md`）。
- 三个模块同版本、同步发布；`coui-material3` 传递引入同版本 `coui`，显式声明 `coui` 时必须同版本（`README.md`、`compatibility.md`）。

## 2. Maven Central 接入（`docs/guide/getting-started.md`）

### 2.1 仓库配置

`settings.gradle.kts` 确认启用 Google 与 Maven Central：

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

### 2.2 模块依赖

使用组件的模块启用 Compose 并添加依赖：

```kotlin
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha15")
    // 可选：自动跟随宿主 MaterialTheme
    // implementation("io.github.itoseo:coui-material3:0.0.1-alpha15")
    // 可选：CouiIcons
    // implementation("io.github.itoseo:coui-icons:0.0.1-alpha15")
}
```

## 3. 源码模块接入（`getting-started.md#源码模块`）

1. 将仓库 `coui/` 复制到应用工程根目录；需要 M3 适配同时复制 `coui-material3/`，需要 `CouiIcons` 同时复制 `coui-icons/`（`coui-icons` 依赖 `coui`，仅用核心组件不需要）。
2. 合并 `gradle/libs.versions.toml` 中该模块引用的依赖库与插件别名（含 `Maven Publish`、`Dokka` 等插件别名）；工具链对齐见 `compatibility.md`。
3. 保留源码模块的资源、`LICENSE`、`NOTICE` 与第三方声明；发行修改版或分叉版遵守 `TRADEMARKS.md`。
4. `settings.gradle.kts` 引入模块：

```kotlin
include(":coui")
// include(":coui-material3")
// include(":coui-icons")
```

5. 模块依赖：

```kotlin
dependencies {
    implementation(project(":coui"))
    // implementation(project(":coui-material3"))
    // implementation(project(":coui-icons"))
}
```

## 4. 页面装配三选一（`getting-started.md#配置页面`）

| 场景 | 装配方式 | 参考 |
| --- | --- | --- |
| 纯 COUI 应用 | `CouiTheme` + `Scaffold` + COUI 组件，无需 Material 3 | `docs/components/scaffold.md` |
| 已有 Material 3 App | 引入 `coui-material3` 并初始化 `CouiMaterial3Style()`，现有布局直接使用组件 | `README.md#接入-material-3` |
| Miuix / 自有容器混搭 | 在已有布局放入 COUI 组件，按需显式挂载 `Backdrop` | `docs/guide/secondary-pages.md` |

## 5. Material 3 适配初始化（`README.md` + `appearance.md`）

仅添加依赖不会启用适配，必须在 `Application.onCreate()` 调用一次（创建 Compose UI 前；重复调用会失败）：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Coui.initialize(defaultStyle = CouiMaterial3Style())
    }
}
```

宿主有独立深浅模式开关时，在对应主题作用域边界提供：

```kotlin
CompositionLocalProvider(LocalCouiDarkTheme provides darkTheme) {
    content()
}
```

## 6. 第一个组件（`getting-started.md#显示第一个组件`）

```kotlin
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
```

## 7. 背景源接入（`getting-started.md#浮动组件的背景源`）

COUI `Scaffold` 自动录制正文视口作为 `topBar`、`bottomBar`、`floatingActionButton` 槽位的默认源：

```kotlin
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
```

第三方容器（Material 3 Scaffold / Miuix / 自定义）需手动 `rememberCouiBackdrop()` + `.couiBackdrop(backdrop)` + 显式传给浮动组件，完整示例见 `docs/guide/secondary-pages.md#使用已有-scaffold` 与 `docs/effects/backdrop.md#顶栏接入`。

## 8. GitHub Packages 快照接入（`docs/guide/github-packages.md`）

1. 从 Actions「发布库预览版」成功运行摘要复制完整 `-SNAPSHOT` 坐标。
2. 凭据：有 `read:packages` 且有目标仓库读取权限的 **PAT (classic)**（fine-grained PAT 不适用），写入用户目录 `~/.gradle/gradle.properties`（不写入宿主项目 Git）：

```properties
GitHubPackagesUsername=你的GitHub用户名
GitHubPackagesPassword=你的PAT_classic
```

3. 仓库追加：

```kotlin
maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/itoseo/COUI-Kit")
    credentials {
        username = providers.gradleProperty("GitHubPackagesUsername").orNull
        password = providers.gradleProperty("GitHubPackagesPassword").orNull
    }
    content { includeGroup("io.github.itoseo") }
}
```

4. 宿主 CI 通过 Secrets → `ORG_GRADLE_PROJECT_GitHubPackagesUsername` / `ORG_GRADLE_PROJECT_GitHubPackagesPassword` 传入；带凭据运行 Gradle 建议 `--no-configuration-cache`。跨仓库工作流自带的 `GITHUB_TOKEN` 不自动获得本仓库包读取权限。
5. 错误排查：`401 Unauthorized` 查 PAT classic/过期/名称；`403 Forbidden` 查 `read:packages` 与仓库权限；解析不到版本查 Actions 是否成功并复制了完整 `-SNAPSHOT` 版本号。

## 9. 消费者兼容要求（`docs/guide/compatibility.md`）

| 项 | 要求 |
| --- | --- |
| 运行系统 | `minSdk` 以库配置为准（当前 API 31） |
| 编译 SDK | 不低于库 `aarMetadata.minCompileSdk`（当前 37.1） |
| 消费者 AGP | 不低于 `aarMetadata.minAgpVersion`（当前 9.1.0） |
| Java 字节码 | 库编译目标 Java 17；Gradle 运行 JVM 按 AGP/Gradle 要求选择 |
| Kotlin / Compose 编译器 | 源码接入先对齐 `gradle/libs.versions.toml` 开发基线并配套 Compose 编译插件；更低 Kotlin 版本兼容未确认 |
| DynamicAnimation | 核心请求 `1.1.0`，按宿主依赖图解析 |
| 预览期 | 不保证二进制兼容，升级后重新编译使用库的模块 |

`minSdk` 通过 manifest 合并检查，不要用 `overrideLibrary` 绕过；发行版编译要求以对应 AAR 元数据为准。效果门槛见 `references/coui-core.md` §7。

## 10. 常见问题（`getting-started.md#常见问题`）

| 现象 | 排查 |
| --- | --- |
| 依赖解析失败或 Compose 编译失败 | `settings.gradle.kts` 是否配置 `mavenCentral()`；模块是否启用 `buildFeatures.compose`；源码接入检查版本目录依赖、插件别名与 `DynamicAnimation` 解析版本 |
| 组件显示但背景材质不对或无模糊 | 核对设备系统版本（API 33+/34+）；背景源覆盖不透明内容且组件位于源节点之外；材质弹窗还须传入 `material` 与宿主窗口背景源 |
| 低版本设备调用高版本组件崩溃 | `Fab`、`NavigationBar`、`TopAppBarButton`、`ChipTab`、`SegmentedControl` 等 `@RequiresApi(34)` 入口前判断 `Build.VERSION.SDK_INT >= 34` |

## 11. 版本与升级说明来源

- 版本变化、迁移与编译要求：`CHANGELOG.md`（例：0.0.1-alpha15 要求宿主最低 `compileSdk` 37.1，运行最低仍 API 31；`BottomSheet` 内容 lambda 改为 `content = { … }`）。
- `coui`、`coui-material3`、`coui-icons` 使用同一版本，升级后重新编译宿主模块（`CHANGELOG.md` 0.0.1-alpha14「升级说明」）。
