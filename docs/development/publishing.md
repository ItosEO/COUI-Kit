# 发布渠道与开发快照

本文区分面向使用者的 Maven Central 发行版、供开发验证的 GitHub Packages 快照，以及仅用于产物检查的本地 Maven 仓库。**当前仓库 Actions 自动化管理 GitHub Packages 的快照发布，不包含 Maven Central 的上传流程。** 不要把“没有 Central 自动上传工作流”理解为“Central 没有已发布版本”。

## 渠道与版本来源

| 渠道 | 用途 | 来源与注意事项 |
| --- | --- | --- |
| Maven Central | 公开发行的预览版/正式版 | [包页面](https://central.sonatype.com/artifact/io.github.itoseo/coui)与 [README](../../README.md)；README 当前示例版本为 `0.0.1-alpha2`。以实际远端解析和发行元数据为准 |
| GitHub Packages | 手动发布当前提交的开发快照 | 仓库 [publish-preview.yml](../../.github/workflows/publish-preview.yml) 生成带短 SHA 的 `-SNAPSHOT`，接入需认证 |
| 本地文件仓库 | CI/开发检查 AAR、POM、Module Metadata、源码包及 Dokka | `coui/build/repository`；不上传远端，不视为真实发行 |

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

库坐标和开发基础版本由根目录 [gradle.properties](../../gradle.properties) 的 GROUP、POM_ARTIFACT_ID 和 VERSION_NAME 持有。当前 `VERSION_NAME=0.1.0-SNAPSHOT`，仅用于开发快照的基础版本；它**不是** Maven Central 上一个已验证的 `0.1.0` 发布。工作流在基础版本中加入当前提交的短 SHA；进入下一轮开发时更新基础版本。库版本不读取 Demo versionCode 或显示版本名。

[库构建配置](../../coui/build.gradle.kts)只发布 release 变体，包含 AAR、源码包、Dokka HTML 文档包、POM 和 Gradle Module Metadata。源码与文档归档附带根许可、来源与名称声明以及既有 Skia 许可；AAR 保留库已有的 Skia 资源。不发布 Demo、不提前混淆库，也不上传 Demo 密钥。

工作流先发布到构建目录中的 Maven 文件仓库，检查坐标、非空产物、源码与 HTML、许可声明，以及 `.module` 中的 Compose BOM 和 DynamicAnimation 严格版本约束，再上传 GitHub Packages。POM 不具备完整表达 Gradle 严格约束的能力，不能用 POM 存在代替 `.module` 检查。

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
./gradlew :coui:publishAllPublicationsToLocalRepository --no-configuration-cache
```

本地 Maven 目标用于检查发行结构，不会切换 Demo 或外部宿主的依赖。PR Android 检查也会生成这个目标，验证库打包和文档生成；不执行远端上传或单测。

上传任务为 `:coui:publishAllPublicationsToGitHubPackagesRepository`。Gradle 按仓库名读取 GitHubPackagesUsername 和 GitHubPackagesPassword；手动本地上传须另有当前任务授权。工作流通过同名 ORG_GRADLE_PROJECT 环境属性提供它们。

## 失败处理

构建或产物检查失败时不会上传。上传失败可能已留下部分文件，先检查失败步骤、包页面和诊断附件，再决定重跑；不以工作流启动或局部上传成功作为已发布。上传后读取失败时保留失败状态，核对远端版本，避免误报成功。

首次工作流运行前，构建配置及静态检查不代表发布插件兼容、远端权限和实际下载已经验证。私有包消耗仓库所有者的存储与下载额度，重复快照的保留和清理按实际使用处理。当前未配置 Maven Central 或 JitPack 的自动上传工作流；公开发行的实际版本以远端仓库为准。
