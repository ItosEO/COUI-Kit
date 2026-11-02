# 发布渠道与开发快照

推送 `v` 开头的发行 Tag 后，GitHub Actions 自动构建、签名并发布 Maven Central 的两个模块。开发快照继续通过独立的 GitHub Packages 工作流手动发布。

## 渠道与版本来源

| 渠道 | 用途 | 来源与注意事项 |
| --- | --- | --- |
| Maven Central | 公开发行的预览版/正式版 | [publish-central.yml](../../.github/workflows/publish-central.yml) 响应发行 Tag；接入版本见 [README](../../README.md)，以实际远端解析和发行元数据为准 |
| GitHub Packages | 手动发布当前提交的开发快照 | 仓库 [publish-preview.yml](../../.github/workflows/publish-preview.yml) 生成带短 SHA 的 `-SNAPSHOT`，接入需认证 |
| 本地文件仓库 | CI/开发检查 AAR、POM、Module Metadata、源码包及 Dokka | 两模块各自的 `build/repository`；不上传远端，不视为真实发行 |

使用者优先选择 Maven Central 已验证的版本；需要针对具体 Commit 协作调试时才使用 GitHub Packages 快照。不同渠道不可在同一模块重复引入。

## GitHub Packages 手动快照发布

需要让宿主验证当前 COUI 提交时，在 Actions 手动运行“发布库预览版”。成功后，从运行摘要复制 Maven 坐标交给接入者；用户侧配置见[GitHub Packages 接入](../guide/github-packages.md)。

### 手动发布步骤

1. 打开 Actions → 发布库预览版 → Run workflow，选择已经评审的受信任分支。日常使用 main；手动选择分支等于授权执行该分支的构建脚本。
2. 等待本地 Maven 产物检查、上传和远端元数据读取全部成功。
3. 从运行摘要复制完整依赖坐标，并记录对应源码提交与 Actions run 地址。

普通分支 push 和 `[ci]` push 均不触发库发布；发行 Tag 触发 Central 工作流。Demo 打包仍按 [CI 指南](ci.md)执行。两个发布工作流只允许在上游仓库运行，不使用 PR 事件，不把发布凭据提供给 fork PR。

## 版本与产物

`coui` 与 `coui-material3` 同版本、同步发布，正式版、预览版和开发快照均遵守此规则。即使某个模块没有代码变化，也随另一模块发布对应版本。适配模块依赖同版本核心；只有两个坐标均可下载，才视为该版本发布完成。

库 group、核心 artifactId 和开发基础版本由根目录 [gradle.properties](../../gradle.properties) 持有。可选模块在[自身配置](../../coui-material3/build.gradle.kts)显式指定 `coui-material3`，不继承核心的 artifactId。GitHub Packages 在开发版本中加入提交短 SHA；Central 从 Tag 去掉首字母 `v`，通过 `-PVERSION_NAME` 同时覆盖两个模块的版本，不修改开发基础版本。库版本不读取 Demo versionCode 或显示版本名。

[核心配置](../../coui/build.gradle.kts)与[可选模块配置](../../coui-material3/build.gradle.kts)均只发布 release 变体，包含 AAR、源码包、Dokka HTML 文档包、POM 和 Gradle Module Metadata。两个模块的源码与文档归档附带根许可、来源与名称声明；核心继续附带既有 Skia/AndroidX 声明与 AAR 资源。不发布 Demo、不提前混淆库，也不上传 Demo 密钥。

两个发布工作流先生成两模块的本地 Maven 文件仓库，调用与 PR 相同的[真实产物检查](../../.github/scripts/verify-maven-artifacts.cjs)。脚本检查独立坐标、非空产物、源码与 HTML、许可、两模块 Compose BOM 的元数据与 POM 导入、核心动画严格约束，以及核心无 M3、适配器依赖同版本核心与 M3。全部通过后再上传两个 artifact。Packages 检查远端版本元数据；Central 检查发行文件及签名可匿名下载。POM 不具备完整表达 Gradle 严格约束的能力，不能用 POM 存在代替 `.module` 检查。每次发行仍须确认对应运行结果，不能以历史成功代替当前版本验收。

Packages 按分支、Central 按 Tag 串行执行，不取消正在上传的任务。重新运行相同提交的 Packages 任务仍是 SNAPSHOT 发布；SHA 只能标识源码，不把 SNAPSHOT 当作不可变发行版本。

## Maven Central 发行与变更记录

先将发布工作流与待发行改动合入 main，确认该提交的 CI 和所需验收通过，再创建尚未发布的版本 Tag。Tag 必须指向 main 历史中的提交；不要在未合入的开发分支发布。

例如准备发布 `0.0.1-alpha4`：

```sh
git switch main
git pull --ff-only
git tag -a v0.0.1-alpha4 -m "发布 0.0.1-alpha4"
git push origin v0.0.1-alpha4
```

支持 `v主版本.次版本.补丁版本`，以及 `-alphaN`、`-betaN`、`-rcN` 后缀，例如 `v0.1.0`、`v0.1.0-rc1`。版本数字不带多余前导零。不接受 SNAPSHOT、构建元数据或 Commit 标题作为 Central 版本；具体校验由 [Tag 脚本](../../.github/scripts/central-release-version.cjs)持有。

在 Actions 查看“发布 Maven Central”。工作流先执行两个库的 Release Lint 和本地发行检查，再通过已有 Vanniktech 插件签名、上传并自动发布。Central 配置只在发布命令中启用，普通 PR、Demo 与 Packages 构建不需要 Central 凭据或 GPG 私钥。

工作流等待 `PUBLISHED` 后，匿名下载两个模块的 AAR、POM、`.module`、源码包、文档包及对应签名。所有下载检查通过后才输出成功摘要和坐标。无需手动打 ZIP 或在 Portal 点击 Publish。Central 校验和索引需要时间，`VALIDATED` 或 `PUBLISHING` 都不代表已完成。

### Central 凭据

在仓库 Actions Secrets 配置下列字段；现有字段可直接复用：

| Secret | 内容 |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | Central Portal User Token 的 username，不是网页登录名 |
| `MAVEN_CENTRAL_PASSWORD` | 同一 User Token 的 password |
| `GPG_SIGNING_KEY` | ASCII-armored 完整签名私钥文本，不是 Base64；公钥须已分发 |
| `GPG_PASSPHRASE` | 私钥口令；无口令的密钥可为空 |

工作流仅在签名发布步骤注入这些值，并关闭 Gradle configuration cache 与 Gradle 缓存。缺少必需凭据直接失败；不写入仓库、构建附件或日志。插件参数见 [Central 官方接入说明](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)。

建立新版本时，至少完成：

1. 为确认过的发行源码建立 Git Tag，记录 Commit SHA，并让版本与构建产物一一对应。
2. 检查 Maven Central 可解析的 AAR、POM、Gradle Module Metadata、sources/javadoc 与依赖约束；不把本地构建成功当成远端可下载。
3. 使用独立消费者确认选定版本能在声明的 SDK/AGP/Compose 组合下构建和运行，并记录实际矩阵。
4. 在 [CHANGELOG](../../CHANGELOG.md) 记录用户可见能力、API 兼容性、迁移说明与发布日期；同步 README、快速开始和兼容表中的版本号。
5. 正式版、Alpha 与 SNAPSHOT 分开标识；不把快照重发视为不可变版本。

## 凭据与本地检查入口

GitHub Packages 工作流使用自身的 `GITHUB_TOKEN`，仅上传与远端读取步骤接收凭据；job 授予 contents: read、packages: write。该渠道不使用 Central、Demo 签名或 GPG Secrets。Central 工作流仅需 contents: read，发布认证使用上文的 User Token。带凭据的 Gradle 任务关闭 configuration cache，发布工作流不缓存 Gradle 状态。

获得当前任务的本地构建授权后，可在根目录仅生成文件仓库，不需要任何上传凭据：

```sh
./gradlew :coui:publishAllPublicationsToLocalRepository :coui-material3:publishAllPublicationsToLocalRepository --no-configuration-cache
node .github/scripts/verify-maven-artifacts.cjs
```

本地 Maven 目标用于检查发行结构，不会切换 Demo 或外部宿主的依赖。PR Android 检查也会生成这个目标，验证库打包和文档生成；不执行远端上传或单测。

上传任务分别为 `:coui:publishAllPublicationsToGitHubPackagesRepository` 与 `:coui-material3:publishAllPublicationsToGitHubPackagesRepository`。Gradle 按仓库名读取 GitHubPackagesUsername 和 GitHubPackagesPassword；手动本地上传须另有当前任务授权。工作流通过同名 ORG_GRADLE_PROJECT 环境属性提供它们。

## 失败处理

构建或产物检查失败时不会上传。上传失败可能已留下部署或部分文件，先检查失败步骤和远端状态，再决定重跑；不以工作流启动或局部上传成功作为已发布。上传后读取失败时保留失败状态，核对远端版本，避免误报成功。

Central 已发布版本不可覆盖。发布步骤失败或超时时，先用日志中的 deployment ID 检查 [Central Deployments](https://central.sonatype.com/publishing/deployments)。若仍在发布，等待原部署完成；若已发布而只有匿名下载检查失败，可重跑下载命令确认，不重新上传同一版本，也不移动旧 Tag。需要改变源码时使用新版本 Tag。工作流不自动撤销部署或重发，以免把超时误判为上传失败。

私有包消耗仓库所有者的存储与下载额度，重复快照的保留和清理按实际使用处理。Maven Central 接入坐标见[快速开始](../guide/getting-started.md)。
