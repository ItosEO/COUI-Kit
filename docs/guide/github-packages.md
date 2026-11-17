# 接入 GitHub Packages 预览版

需要在自己的 App 中验证 COUI 开发快照时，可从 GitHub Actions“发布库预览版”的成功运行摘要复制依赖坐标。接入前请先确认[兼容要求](compatibility.md)；正式发行版请从 Maven Central 引入，见[快速开始](getting-started.md)。

## 准备下载凭据

GitHub 的 Maven / Gradle 仓库即使公开也需要认证，须使用具有 `read:packages` 权限且有目标仓库读取权限的 **PAT (classic)**（fine-grained PAT 不适用于此认证；私有仓库的包继承仓库访问权限）。

1. 在用户目录的 `~/.gradle/gradle.properties` 中配置凭据（不要写入宿主项目的 Git）：

   ~~~properties
   GitHubPackagesUsername=你的GitHub用户名
   GitHubPackagesPassword=你的PAT_classic
   ~~~

2. 在宿主 `settings.gradle.kts` 的 `dependencyResolutionManagement.repositories` 中保留 Google、Maven Central，并追加 `GitHubPackages` 仓库：

   ~~~kotlin
   maven {
       name = "GitHubPackages"
       url = uri("https://maven.pkg.github.com/itoseo/COUI-Kit")
       credentials {
           username = providers.gradleProperty("GitHubPackagesUsername").orNull
           password = providers.gradleProperty("GitHubPackagesPassword").orNull
       }
       content {
           includeGroup("io.github.itoseo")
       }
   }
   ~~~

## 添加依赖

1. 确保模块保留 Android 与 Compose 插件并启用 `buildFeatures.compose = true`。
2. 将发布摘要中的完整坐标写入模块的 `dependencies`（如同时使用 Material 3 适配，改用或同版本引入 `coui-material3`；需要 `CouiIcons` 时额外添加发布摘要中的 `coui-icons` 坐标并与核心保持相同版本；同一 COUI 构件仅选择一种发布渠道，以免重复定义类）：

   ~~~kotlin
   dependencies {
       implementation("io.github.itoseo:coui:<从发布摘要复制的版本>")
   }
   ~~~

- 预览版本坐标由开发版本号与提交 SHA 组成并带 `-SNAPSHOT` 后缀。SHA 用于定位源码；同一提交重新发布仍可能生成新的 SNAPSHOT 产物，需要精确溯源时请同时记录 Actions run 地址。
- 显示第一个组件的代码见[快速开始](getting-started.md#显示第一个组件)。

## 在宿主 CI 中下载

在宿主仓库配置 Secrets，并在需要解析该依赖的 Gradle 步骤中以环境变量传入（带凭据运行 Gradle 时建议附带 `--no-configuration-cache`，避免凭据进入配置缓存、日志或构建附件）：

~~~yaml
env:
  ORG_GRADLE_PROJECT_GitHubPackagesUsername: ${{ secrets.COUI_PACKAGES_USERNAME }}
  ORG_GRADLE_PROJECT_GitHubPackagesPassword: ${{ secrets.COUI_PACKAGES_TOKEN }}
~~~

> [!NOTE]
> 跨仓库工作流自带的 `GITHUB_TOKEN` 不自动获得本仓库包的读取权限，首次接入请按实际授权核对。

### 常见错误排查

| 错误现象 | 排查要点 |
| --- | --- |
| `401 Unauthorized` | 检查使用的是否为 PAT classic、令牌是否过期以及凭据名称是否拼写正确。 |
| `403 Forbidden` | 检查对应 GitHub 账号是否有 `read:packages` 权限及目标仓库读取权限。 |
| 解析不到版本 | 确认对应的“发布库预览版”Actions 已运行成功，且复制了摘要中的完整 `-SNAPSHOT` 版本号。 |

发布者操作见[预览版发布](../development/publishing.md)，源码接入见[快速开始](getting-started.md#源码模块)，认证细则以 [GitHub 官方文档](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)为准。
