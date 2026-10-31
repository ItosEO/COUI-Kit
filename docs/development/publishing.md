# 发布渠道与开发快照

本文区分面向使用者的 Maven Central 发行版、供开发验证的 GitHub Packages 快照，以及仅用于产物检查的本地 Maven 仓库。**当前仓库 Actions 自动化管理 GitHub Packages 的快照发布，不包含 Maven Central 的上传流程。** 不要把“没有 Central 自动上传工作流”理解为“Central 没有已发布版本”。

## 渠道与版本来源

| 渠道 | 用途 | 来源与注意事项 |
| --- | --- | --- |
| Maven Central | 公开发行的预览版/正式版 | [包页面](https://central.sonatype.com/artifact/io.github.itoseo/coui)与 [README](../../README.md)；README 当前示例版本为 `0.0.1-alpha2`。以实际远端解析和发行元数据为准 |
| GitHub Packages | 手动发布当前提交的开发快照 | 仓库 [publish-preview.yml](../../.github/workflows/publish-preview.yml) 生成带短 SHA 的 `-SNAPSHOT`，接入需认证 |
| 本地文件仓库 | CI/开发检查 AAR、POM、Module Metadata、源码包及 Dokka | 两模块各自的 `build/repository`；不上传远端，不视为真实发行 |

使用者优先选择 Maven Central 已验证的版本；需要针对具体 Commit 协作调试时才使用 GitHub Packages 快照。不同渠道不可在同一模块重复引入。

## GitHub Packages 手动快照发布

需要让宿主验证当前 COUI 提交时，在 Actions 手动运行“发布库预览版”。成功后，从运行摘要复制 Maven 坐标交给接入者；用户侧配置见[GitHub Packages 接入](../guide/github-packages.md)。

## 手动发布

1. 将发布配置合入 main，使 GitHub 注册[发布工作流](../../.github/workflows/publish-preview.yml)。
2. 打开 Actions → 发布库预览版 → Run workflow，选择已经评审的受信任分支。日常使用 main；手动选择分支等于授权执行该分支的构建脚本。
3. 等待本地 Maven 产物检查、上传和远端元数据读取全部成功。
4. 从运行摘要复制完整依赖坐标，并记录对应源码提交与 Actions run 地址。

普通 push 和 `[ci]` push 均不触发库发布。Demo 打包仍按 [CI 指南](ci.md)执行。发布工作流只允许在上游仓库运行，不使用 PR 事件，不把包写入凭据提供给 fork PR。

## 版本与产物

从适配模块首次发行起，`coui` 与 `coui-material3` 始终同版本、同步发布，正式版、预览版和开发快照均遵守此规则。即使某个模块没有代码变化，也随另一模块发布对应版本。适配模块依赖同版本核心；只有两个坐标均可下载，才视为该版本发布完成。

库 group、核心 artifactId 和共享版本由根目录 [gradle.properties](../../gradle.properties) 的 GROUP、POM_ARTIFACT_ID 和 VERSION_NAME 持有。可选模块在[自身配置](../../coui-material3/build.gradle.kts)显式指定 `coui-material3`，不继承核心的 artifactId。当前 VERSION_NAME 仅用于开发快照的基础版本，不代表已验证的 Central 发行。工作流加入当前提交的短 SHA；库版本不读取 Demo versionCode 或显示版本名。

[核心配置](../../coui/build.gradle.kts)与[可选模块配置](../../coui-material3/build.gradle.kts)均只发布 release 变体，包含 AAR、源码包、Dokka HTML 文档包、POM 和 Gradle Module Metadata。两个模块的源码与文档归档附带根许可、来源与名称声明；核心继续附带既有 Skia/AndroidX 声明与 AAR 资源。不发布 Demo、不提前混淆库，也不上传 Demo 密钥。

工作流先生成两模块的本地 Maven 文件仓库，调用与 PR 相同的[真实产物检查](../../.github/scripts/verify-maven-artifacts.cjs)。脚本检查独立坐标、非空产物、源码与 HTML、许可、两模块 Compose BOM 的元数据与 POM 导入、核心动画严格约束，以及核心无 M3、适配器依赖同版本核心与 M3。全部通过后再上传两个 artifact，并分别确认远端元数据可读。POM 不具备完整表达 Gradle 严格约束的能力，不能用 POM 存在代替 `.module` 检查。当前接线尚需真实 Actions 运行验证，不据此声称两模块已发布。

同一分支的发布串行执行，不取消正在上传的任务。重新运行相同提交仍是 SNAPSHOT 发布；SHA 只能标识源码，不把 SNAPSHOT 当作不可变发行版本。

## Maven Central 发行与变更记录

Maven Central 的现有发布记录不能由 `publish-preview.yml` 推断。当前仓库未配置 Central 的自动发布工作流；正式发布操作、签名与上传凭据不应在此处被虚构为已完成。后续将 Central 发行流程纳入仓库时，必须另行实现、审核和验证。

建立新版本时，至少完成：

1. 为确认过的发行源码建立 Git Tag，记录 Commit SHA，并让版本与构建产物一一对应。
2. 检查 Maven Central 可解析的 AAR、POM、Gradle Module Metadata、sources/javadoc 与依赖约束；不把本地构建成功当成远端可下载。
3. 使用独立消费者确认选定版本能在声明的 SDK/AGP/Compose 组合下构建和运行，并记录实际矩阵。
4. 在 [CHANGELOG](../../CHANGELOG.md) 记录用户可见能力、API 兼容性、迁移说明与发布日期；同步 README、快速开始和兼容表中的版本号。
5. 正式版、Alpha 与 SNAPSHOT 分开标识；不把快照重发视为不可变版本。

目前缺少历史发行 Tag 与 Maven 产物的明确映射，因此不反推过去每个 Alpha 版本的源码或发布时间。

## 凭据与本地检查入口

工作流使用自身的 `GITHUB_TOKEN`，仅上传与远端读取步骤接收凭据；job 授予 contents: read、packages: write。无需新增 PAT、Demo 签名或 GPG Secrets。带凭据的 Gradle 任务关闭 configuration cache，发布工作流不缓存 Gradle 状态。

获得当前任务的本地构建授权后，可在根目录仅生成文件仓库，不需要任何上传凭据：

```sh
./gradlew :coui:publishAllPublicationsToLocalRepository :coui-material3:publishAllPublicationsToLocalRepository --no-configuration-cache
node .github/scripts/verify-maven-artifacts.cjs
```

本地 Maven 目标用于检查发行结构，不会切换 Demo 或外部宿主的依赖。PR Android 检查也会生成这个目标，验证库打包和文档生成；不执行远端上传或单测。

上传任务分别为 `:coui:publishAllPublicationsToGitHubPackagesRepository` 与 `:coui-material3:publishAllPublicationsToGitHubPackagesRepository`。Gradle 按仓库名读取 GitHubPackagesUsername 和 GitHubPackagesPassword；手动本地上传须另有当前任务授权。工作流通过同名 ORG_GRADLE_PROJECT 环境属性提供它们。

## 失败处理

构建或产物检查失败时不会上传。上传失败可能已留下部分文件，先检查失败步骤、包页面和诊断附件，再决定重跑；不以工作流启动或局部上传成功作为已发布。上传后读取失败时保留失败状态，核对远端版本，避免误报成功。

构建配置及静态检查不代表远端权限和实际下载已经验证。私有包消耗仓库所有者的存储与下载额度，重复快照的保留和清理按实际使用处理。这里的工作流只上传 GitHub Packages；Maven Central 发行坐标见[快速开始](../guide/getting-started.md)，不把 SNAPSHOT 与 Central 版本等同，也不推断其提交对应关系。当前不提供 JitPack 流程。
