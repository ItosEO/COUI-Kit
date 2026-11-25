# 发布渠道与开发快照

推送一次发行 Tag，由“发布 COUI”工作流按版本选择渠道。正式版同号发布到 JitPack 和 Maven Central，Alpha 默认只发布到 JitPack；需要为指定 Alpha Tag 同步发 Maven Central 时，在 Actions 手动触发“发布 COUI”并填入已推送 Tag。开发快照通过独立的 GitHub Packages 工作流手动发布。

## 渠道与版本来源

| 渠道 | 用途 | 来源与注意事项 |
| --- | --- | --- |
| Maven Central | 正式版，及手动触发补发的 Alpha | [发布 COUI](../../.github/workflows/publish-central.yml) 构建、签名并提交发布 |
| JitPack | Alpha 和正式版 | 同一工作流按发行版本请求构建并检查三个模块的远端产物 |
| GitHub Packages | 手动发布当前提交的开发快照 | 仓库 [publish-preview.yml](../../.github/workflows/publish-preview.yml) 生成带短 SHA 的 `-SNAPSHOT`，接入需认证 |
| 本地文件仓库 | CI/开发检查 AAR、POM、Module Metadata、源码包及 Dokka | 各模块的 `build/repository`；不上传远端，不视为真实发行 |

使用者可以从 Maven Central 选择正式版，从 JitPack 选择 Alpha 或正式版。需要针对具体提交协作调试时，可使用 GitHub Packages 快照。三个 COUI 模块使用同一渠道和版本。

## GitHub Packages 手动快照发布

需要让宿主验证当前 COUI 提交时，在 Actions 手动运行“发布库预览版”。成功后，从运行摘要复制 Maven 坐标交给接入者；用户侧配置见[GitHub Packages 接入](../guide/github-packages.md)。

### 手动发布步骤

1. 打开 Actions → 发布库预览版 → Run workflow，选择已经评审的受信任分支。日常使用 main；手动选择分支等于授权执行该分支的构建脚本。
2. 等待本地 Maven 产物检查、上传和远端元数据读取全部成功。
3. 从运行摘要复制完整依赖坐标，并记录对应源码提交与 Actions run 地址。

普通分支 push 和 `[ci]` push 均不触发库发布。Demo 打包按 [CI 指南](ci.md)执行。库发布工作流只允许在上游仓库运行，发布凭据只进入实际使用的步骤。

## 版本与产物

`coui`、`coui-material3` 与 `coui-icons` 同版本、同步发布，正式版、预览版和开发快照均遵守此规则。即使某个模块没有代码变化，也随其他模块发布对应版本。适配模块和图标模块依赖同版本核心；只有三个坐标均可下载，才视为该版本发布完成。

库 group、核心 artifactId 和开发基础版本由根目录 [gradle.properties](../../gradle.properties) 持有。可选模块分别在[适配模块](../../coui-material3/build.gradle.kts)和[图标模块](../../coui-icons/build.gradle.kts)显式指定 artifactId。JitPack 和 Central 共用去掉首字母 `v` 的发行版本。JitPack 从 `0.0.1-alpha18` 起使用 `com.github.itoseo.coui-kit`，Central 使用 `io.github.itoseo`。GitHub Packages 在开发版本中加入提交短 SHA。库版本独立于 Demo versionCode 和显示版本名。

[核心配置](../../coui/build.gradle.kts)与两个可选模块配置均只发布 release 变体，包含 AAR、源码包、Dokka HTML 文档包、POM 和 Gradle Module Metadata。源码与文档归档附带根许可、来源与名称声明；核心与图标模块归档包含 ICON_SOURCES.md，核心还包含既有 Skia/AndroidX 声明。不发布 Demo、不提前混淆库，也不上传 Demo 密钥。

Central 和 GitHub Packages 发布前先生成三个模块的本地 Maven 文件仓库，调用与 PR 相同的[产物检查](../../.github/scripts/verify-maven-artifacts.cjs)。脚本检查独立坐标、非空产物、源码与 HTML、许可、核心动画依赖的版本声明，以及各模块依赖边界。POM 和 `.module` 必须声明相同的直接 Compose 依赖及版本，Compose BOM 和额外 Compose 约束均会使检查失败。Packages 上传后检查远端版本元数据。Gradle 的版本约束以 `.module` 为准。

Packages 按分支、公开发行按 Tag 串行执行，不取消正在上传的任务。重新运行相同提交的 Packages 任务仍是 SNAPSHOT 发布；SHA 只能标识源码，不把 SNAPSHOT 当作不可变发行版本。

## 发行与变更记录

先将发布工作流与待发行改动合入 main，确认该提交的 CI 和所需验收通过，再创建尚未发布的版本 Tag。Tag 必须指向 main 历史中的提交；不要在未合入的开发分支发布。

发行使用 Alpha 和正式版。按版本类型创建 GitHub Release 草稿，手写正文。正式版使用不带预览后缀的版本，并取消 GitHub 的 Pre-release 标记。GitHub Release 可在正文就绪后公开，依赖可用状态按远端产物报告。

Release 正文按 `demo` 和实际有改动的库模块分组。每条以“新增”“优化”“重构”“移除”“调整”或其他符合变更的动词开头。`CHANGELOG.md` 的版本条目只记录库模块的变更及升级说明。

推送一个 Tag 即可启动发布。

| Tag 示例 | 渠道 |
| --- | --- |
| `v0.0.1` | JitPack 和 Maven Central 并行发布 `0.0.1` |
| `v0.0.1-alpha18` | 推送 Tag 时只发布到 JitPack |

Alpha Tag 默认只触发 JitPack；如需同时发布到 Maven Central，在 Actions 手动运行“发布 COUI”，填入已推送的 Tag 并选择 `maven-central`（补发）或 `both`（双渠道）。

例如发布 Alpha：

```sh
git switch main
git pull --ff-only
git tag -a v0.0.1-alpha18 -m "发布 0.0.1-alpha18"
git push origin v0.0.1-alpha18
```

Tag 格式为 `v主版本.次版本.补丁版本`，可带 `-alphaN` 后缀。版本数字采用无多余前导零的十进制表示，规则由 [Tag 校验脚本](../../.github/scripts/central-release-version.cjs)持有。

在 Actions 查看“发布 COUI”。JitPack 步骤请求该版本的 POM 以触发构建，确认构建对应发行提交，并检查三个模块的 AAR、POM 和 `.module` 可下载。Central 步骤先执行 Release Lint 和本地产物检查，再通过 Vanniktech 插件签名、上传并提交自动发布。Central 摘要表示已提交，最终可用时间以三个模块在 Central 的实际下载状态为准。

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
2. 使用独立消费者确认选定版本能在声明的 SDK/AGP/Compose 组合下构建和运行，并记录实际矩阵。
3. 在 [CHANGELOG](../../CHANGELOG.md) 记录用户可见能力、API 兼容性、迁移说明与发布日期；同步 README、快速开始和兼容表中的版本号。
4. 正式版、Alpha 与 SNAPSHOT 分开标识；不把快照重发视为不可变版本。

## 凭据与本地检查入口

GitHub Packages 工作流使用自身的 `GITHUB_TOKEN`，仅上传与远端读取步骤接收凭据；job 授予 contents: read、packages: write。该渠道不使用 Central、Demo 签名或 GPG Secrets。Central 工作流仅需 contents: read，发布认证使用上文的 User Token。带凭据的 Gradle 任务关闭 configuration cache，发布工作流不缓存 Gradle 状态。

获得当前任务的本地构建授权后，可在根目录仅生成文件仓库，不需要任何上传凭据：

```sh
./gradlew :coui:publishAllPublicationsToLocalRepository :coui-material3:publishAllPublicationsToLocalRepository :coui-icons:publishAllPublicationsToLocalRepository --no-configuration-cache
node .github/scripts/verify-maven-artifacts.cjs
```

本地 Maven 目标用于检查发行结构，不会切换 Demo 或外部宿主的依赖。PR Android 检查也会生成这个目标，验证库打包和文档生成；不执行远端上传或单测。

三个模块分别提供 `publishAllPublicationsToGitHubPackagesRepository` 上传任务。Gradle 按仓库名读取 GitHubPackagesUsername 和 GitHubPackagesPassword；手动本地上传须另有当前任务授权。工作流通过同名 ORG_GRADLE_PROJECT 环境属性提供它们。

## 失败处理

构建或产物检查失败时不会上传。上传失败可能已留下部署或部分文件，先检查失败步骤和远端状态，再决定重跑；不以工作流启动或局部上传成功作为已发布。Packages 上传后读取失败时保留失败状态，核对远端版本，避免误报成功。

Central 已发布版本不可覆盖。发布步骤失败或超时时，先用日志中的 deployment ID 检查 [Central Deployments](https://central.sonatype.com/publishing/deployments)。若仍在发布，等待原部署完成；若已发布，不重新上传同一版本，也不移动旧 Tag。需要改变源码时使用新版本 Tag。工作流不自动撤销部署或重发，以免把超时误判为上传失败。

JitPack 构建失败时，从工作流日志进入对应版本的构建日志。双发时按渠道检查结果，使用 Actions 的“重新运行失败的作业”重试失败步骤。

私有包消耗仓库所有者的存储与下载额度，重复快照的保留和清理按实际使用处理。各渠道的接入坐标见[快速开始](../guide/getting-started.md)。
