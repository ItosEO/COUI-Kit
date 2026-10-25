# 接入 GitHub Packages 预览版

需要在自己的 App 中验证尚未正式发行的 COUI 时，从“发布库预览版”的成功运行摘要复制依赖坐标。先确认[兼容要求](compatibility.md)，再配置下载凭据。

## 准备下载凭据

GitHub 的 Maven/Gradle 仓库即使公开也需要认证。本地使用具有 `read:packages` 权限的 PAT classic，账号还必须有目标仓库的读取权限；fine-grained PAT 不适用于此认证。私有仓库的包继承仓库访问权限。

在用户目录的 `~/.gradle/gradle.properties` 中配置，不写入宿主项目的 Git：

```properties
GitHubPackagesUsername=你的GitHub用户名
GitHubPackagesPassword=你的PAT_classic
```

宿主 `settings.gradle.kts` 的 `dependencyResolutionManagement.repositories` 中保留 Google、Maven Central，并添加：

```kotlin
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
```

## 添加依赖

将成功发布摘要中的完整坐标放入 App 模块的 `dependencies`。以下版本字段需要替换：

```kotlin
implementation("io.github.itoseo:coui:<从发布摘要复制的版本>")
```

保留宿主的 Android 与 Compose 插件，并启用 `buildFeatures.compose`。同一模块不要同时依赖源码版 `project(":coui")` 和这个包，以免重复定义类。显示第一个组件的代码见[快速开始](getting-started.md#显示第一个组件)。

预览版本由库的开发版本与提交 SHA 组成，带 `-SNAPSHOT` 后缀。SHA 用于定位源码；相同提交重新发布仍可能生成新的 SNAPSHOT 产物。需要精确回看一次发布时，同时保留 Actions run 地址。

## 在宿主 CI 中下载

将读取凭据配置为宿主仓库的 Secrets，在需要解析此依赖的 Gradle 步骤中传入：

```yaml
env:
  ORG_GRADLE_PROJECT_GitHubPackagesUsername: ${{ secrets.COUI_PACKAGES_USERNAME }}
  ORG_GRADLE_PROJECT_GitHubPackagesPassword: ${{ secrets.COUI_PACKAGES_TOKEN }}
```

跨仓库的 `GITHUB_TOKEN` 不自动获得本仓库包的读取权限；首次接入按实际授权核对。凭据不写入日志、构建附件或 configuration cache，带凭据运行 Gradle 时使用 `--no-configuration-cache`。

出现 `401` 时检查令牌类型、有效期和凭据；`403` 时检查账号及仓库权限；解析不到版本时，先确认对应发布运行成功并复制了完整坐标。

发布者操作见[预览版发布](../development/publishing.md)，源码接入仍见[快速开始](getting-started.md#添加模块)。认证要求以 [GitHub 官方文档](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)为准。
