# 文档维护、Note、验证、CI 与发布分册（docs 目录 + demo/Pages 辅助产出）

本分册规则出自 COUI-Kit 仓库内文件，每条标注出处。路径均相对仓库根目录。

## 1. 文档体系与文件归属（`docs/development/documentation.md`）

| 内容 | 唯一维护位置 |
| --- | --- |
| 项目能力和使用导航 | 根 README |
| AI 开发任务阅读入口与执行权限 | AGENTS.md |
| 源码接入、兼容要求、外观和 Demo 操作 | docs/guide |
| 可直接放入布局的控件及受控状态 | docs/components |
| 可独立调用的绘制、采样、光效和算法 | docs/effects |
| 开发流程、架构、代码与 API 规范、复刻和评审方法 | docs/development |
| 具体决定的依据、备选和历史证据 | 对应 Note |
| 完整参数声明、默认值和固定算法常量 | 源码、KDoc 和构建配置 |

- 顶栏是 component，顶栏使用的渐进模糊是 effect；组件页讲装配与回调，效果页讲采样、坐标和绘制契约；共用要求在所属页完整维护，其他页面链接过去。

### 哪些改动必须更新文档（同上）

| 改动 | 同步内容 |
| --- | --- |
| 新增 component 或 effect | 新建对应页面，加入本目录 README，提供公开 API 示例与源码入口；按任务范围补 Demo |
| 重命名或移除公开入口 | 更新页面、目录、示例和入站链接；迁移方式写入 PR，发行时记入 CHANGELOG |
| 参数、回调、受控状态或生命周期语义变化 | 更新对应页面的用法、责任划分和相关示例 |
| 平台门槛、依赖要求或失败行为变化 | 更新兼容要求与受影响页面，保留明确的平台分支 |
| 采样、单位、坐标或合成要求变化 | 更新对应 effect 和使用它的 component 装配说明 |
| 纯内部实现调整，使用契约不变 | 按需更新开发文档、KDoc 和 Note；PR 说明普通使用文档无需修改的原因 |
| 仅调整实现参数，使用契约不变 | 在代码维护参数；不复制一套数值到文档 |

### Component 页面结构（同上）

文件名用英文小写和连字符；标题用组件名；开头一句话说明它帮助用户做什么。按实际需要组织，不建空章节：① 用途与可见结果（有真实图片/视频才引用，不用占位图）② 基本用法（完整操作 + 示例运行位置 + 宿主依赖）③ 状态与事件（谁持有值、如何更新、回调何时提交、禁用行为）④ 外观与预设（引用所属 Defaults，不抄常量）⑤ 平台与装配（版本门槛、父容器、采样源、inset、语义、失败处理）⑥ 完整示例与 API（链接公开声明和 Demo 具体文件）。参考：`docs/components/slider.md`、`checkbox.md`、`menu.md`。

### Effect 页面结构（同上）

① 最小用法与公开入口（区分便捷入口与底层自定义）② 输入、单位与坐标系 ③ 采样与合成（是否需要背景源、源与消费者分层、蒙版/alpha 含义）④ 资源与生命周期（谁持有可变 renderer，变化时谁更新）⑤ 平台门槛、错误与已有替代路径（不把不同算法写成等价）⑥ 完整装配和源码链接。参考：`docs/effects/shadow.md`、`metaball.md`。像素算法没有业务事件时，不强加组件式的点击或状态章节。

### 示例与措辞（同上）

- Kotlin 示例用公开 API、不展示导入语句；完整函数的 Modifier 放首位并提供默认值；片段须说明所在 Compose 或绘制作用域。
- 宿主参数必须注明来源；不调用 Demo 私有函数或要求依赖 Demo；应用文案作为函数参数传入，实际 App 放入字符串资源。
- 示例尺寸、颜色和输入用于演示技术，不称为官方默认；固定预设直接引用 Defaults。
- 必须知道的限制写在对应操作附近；高级坐标与合成细节放效果页。
- 使用文档面向首次接入者，直接说明当前用法；删除「已改为」「仍保留旧调用」等变更叙述；版本变化与迁移步骤归 CHANGELOG，决定依据归 Note，单轮检查与交付过程归 PR。
- 提交前：核对示例 API 来源、公开可见性、必填参数和回调；检查目录可达、相对链接与章节锚点；执行 `git diff --check`；移动页面时更新所有入站链接。

## 2. 决定记录 Note（`docs/development/workflow.md`「决定记录」+ `.agents/skills/write-notes-like-deepseek/SKILL.md`）

- 使用仓库内 write-notes-like-deepseek Skill 维护格式和生命周期；行为、架构、跨文件契约、开发流程、验证策略或数据格式改变时必须写或更新 Note；纯格式、无歧义重命名和不改行为的局部机械修改不新建 Note。
- 动手前搜索活跃决定：`rg --hidden --glob '!.agents/notes/archived/**' '组件名|机制名' .agents/notes/`。
- Note 路径即分类：`{lifecycle}/{class}/yyyy-mm-dd-topic.md`（lifecycle ∈ proposed/implemented/rejected/archived）；同一决定的路径、符号等事实就地更新；理由翻转则新建并互链，不覆盖旧理由；新建时将命中项分类为无关/部分重叠/完全吸收/过时提案并记录处理结果；只建实际使用的目录，不建 INDEX。
- Note 与实现同一个 PR；正文说明问题、当前决定、真实备选、代价和验证限制；引用当前源码位置，避免复制可变参数；落地后移入 implemented 并同步状态。
- 归档检查整个分支时，用目标分支共同基点作为 `AGENT_NOTE_ARCHIVE_BASE_REF`；PR CI 使用 PR 的 base SHA。
- 校验命令（`docs/development/validation.md`，Node.js 24，无需 npm 依赖）：

```sh
node --experimental-strip-types .agents/skills/write-notes-like-deepseek/scripts/verify-agent-note-tree.ts
node --experimental-strip-types .agents/skills/write-notes-like-deepseek/scripts/verify-agent-note-format.ts
node --experimental-strip-types .agents/skills/write-notes-like-deepseek/scripts/verify-archived-agent-notes.ts
git diff --check
```

- 文档维护总体触发条件以 `AGENTS.md`「文档维护」节为准：默认不因代码修改更新规范或新增 Note；只有设计约束、执行规则或 AI 易错边界变化时才原地修订；参数、版本、字段、算法步骤、调用链和文件清单以当前代码、资源及构建配置为准。

## 3. 验证与评审（`docs/development/validation.md`）

- 文档检查：相对路径、章节锚点、目录可达、公开 API 示例、失效入口及编码；移动页面后检查普通文档、PR 模板、AGENTS 和活跃 Note 的入站链接。
- 本地 Android 检查（须先获得当前任务授权，`AGENTS.md` 默认禁止本地自行运行 Android 构建）：

```sh
./gradlew :coui:lintDebug :coui-material3:lintDebug :coui-icons:lintDebug :demo:lintRelease :demo:assembleDebug :demo:assembleRelease \
  :coui:publishAllPublicationsToLocalRepository :coui-material3:publishAllPublicationsToLocalRepository :coui-icons:publishAllPublicationsToLocalRepository
node .github/scripts/verify-maven-artifacts.cjs
```

需要避免签名参数进入本地 configuration cache 时加 `--no-configuration-cache`；失败须查看实际错误，不建立吞掉失败的路径。
- 开发诊断：`CouiRenderDiagnostics` 与 `renderDiagnosticProbe` 只在主线程按需启停，不成为业务状态，不默认启用截图或逐帧日志；提交前清理临时检查器、日志和实验入口。
- 设备验收（原厂对照方法与证据范围表见 `replication-guide.md` §6「验证原理，不伪造验收」）：对齐目标版本、尺寸、density、深浅色、字体和操作序列，检查动画中段；覆盖反向、未拖动的 UP、CANCEL、父容器接管、隐藏恢复、RTL、键盘和无障碍；性能结论附可复现的设备条件与数据；未执行构建或设备检查时在 PR 中记录未执行项、原因和需要用户验收的行为。

## 4. CI 与 PR（`docs/development/ci.md`）

| 操作 | 结果 |
| --- | --- |
| main 普通 push | Demo job 跳过 |
| main 最新提交标题以 `[ci]` 开头 | 构建 itos 签名 release，上传 APK 与对应 mapping |
| Actions 手动运行 Demo 打包 | 构建所选受信任分支（日常 main） |
| 指向 main 的 PR | 自动执行 Note、库与 Demo Lint、Demo debug/release 与库发行构建；通过后上传 Demo Release APK |
| Actions 手动运行发布库预览版 | 构建并检查库产物，上传 GitHub Packages |
| 推送发行 Tag | 构建、签名并自动发布 Maven Central 库模块 |

- PR Android 工作流（`.github/workflows/pr-checks.yml`）持有实际 Gradle 命令，不执行单测、模拟器或真机测试；PR 同时执行 Central Tag 解析器无凭据自检；Note 工作流（`verify-notes.yml`）检查目录、格式、链接与归档封印。
- PR 检查对三个库模块分别 Lint，生成同版本本地 Maven 产物及源码、API 文档，运行 `verify-maven-artifacts.cjs`（检查独立坐标、非空归档、模块依赖边界、许可及核心动画依赖版本声明）。
- Demo 签名 Secrets：`KEYSTORE`（Base64）、`KEYSTORE_PASSWORD`、`KEYSTORE_ALIAS`（`itos`）、`KEYSTORE_ALIAS_PASSWORD`；PR 不注入凭据，用 Runner 默认测试签名；构建代码通过 `pull_request` 执行，不用 `pull_request_target`。
- Demo 构建配置（`demo/build.gradle.kts`）：`applicationId = "com.itos.coui.demo"`，`minSdk = 31`，`versionCode` = HEAD Git 提交计数（`git rev-list --count HEAD`），`versionName` 与发行版本一致（当前 `0.0.1-alpha15`）；签名经环境变量（`KEYSTORE_FILE` 等 4 字段成组）或根目录 `signing.properties` 提供，缺字段 `require` 失败；本地配置模板 `signing.properties.example`，密钥在仓库外。
- 失败处理：不关闭 Lint、R8 或增加整包 keep；Lint 警告按实际影响处理，不新增 baseline。必需检查 job 名称：「Android Lint 与 Demo 构建」「校验 Note 目录、格式与归档」。

## 5. 发布（`docs/development/publishing.md`）

- 渠道：Maven Central（`publish-central.yml` 响应 `v` 开头发行 Tag）；GitHub Packages（`publish-preview.yml` 手动发布当前提交快照，版本带短 SHA 的 `-SNAPSHOT`）；本地文件仓库（各模块 `build/repository`，仅检查用）。
- 版本规则：三个模块同版本、同步发布，即使某模块无代码变化；`GROUP=io.github.itoseo`、核心 `POM_ARTIFACT_ID=coui`、开发基础版本 `VERSION_NAME` 由根 `gradle.properties` 持有（当前 `0.1.0-SNAPSHOT`）；可选模块在各自 build 文件显式指定 artifactId；Central 从 Tag 去掉首字母 `v` 并经 `-PVERSION_NAME` 同时覆盖三个模块。
- Tag 格式：`v主版本.次版本.补丁版本` 与 `-alphaN` 后缀（如 `v0.1.0`、`v0.1.0-alpha1`）；不接受 Beta、RC、SNAPSHOT、构建元数据或 Commit 标题；校验由 `.github/scripts/central-release-version.cjs` 持有。发行只使用 Alpha 和正式 Release；Release 内容手写；先建 GitHub 预发布草稿，Tag 推送独立触发 Maven 发布。
- Release 正文按 `demo` 和实际有改动的库模块分组；每条以「新增」「优化」「重构」「移除」「调整」等动词开头；`CHANGELOG.md` 版本条目只记录库模块变更及升级说明。
- 发行步骤：先把发布工作流与待发行改动合入 main 并确认 CI 通过 → 建 Tag（指向 main 历史提交）→ `git tag -a vX.Y.Z -m "…"` → `git push origin vX.Y.Z` → 在 Actions 查看「发布 Maven Central」。
- Central 凭据 Secrets：`MAVEN_CENTRAL_USERNAME`/`MAVEN_CENTRAL_PASSWORD`（Portal User Token）、`GPG_SIGNING_KEY`（ASCII-armored 私钥文本，非 Base64）、`GPG_PASSPHRASE`（可为空）；仅在签名发布步骤注入，关闭 configuration cache 与 Gradle 缓存。
- 产物：三个模块只发布 release 变体（AAR、源码包、Dokka HTML、POM、Gradle Module Metadata）；POM 与 `.module` 必须声明相同直接 Compose 依赖及版本——Compose BOM 和额外 Compose 约束会使 `verify-maven-artifacts.cjs` 失败；不发布 Demo、不提前混淆库。
- GitHub Packages 手动发布：Actions → 发布库预览版 → Run workflow，选已评审受信任分支；等待本地产物检查、上传和远端元数据读取全部成功后从运行摘要复制完整坐标；凭据为工作流自身 `GITHUB_TOKEN`（contents: read、packages: write）。
- 建立新版本至少完成：① Tag 对应确认过的源码并记录 Commit SHA ② 用独立消费者确认选定版本在声明 SDK/AGP/Compose 组合下可构建运行并记录矩阵 ③ 在 CHANGELOG 记录用户可见能力、API 兼容性、迁移说明与发布日期，同步 README、快速开始和兼容表版本号 ④ 正式版、Alpha 与 SNAPSHOT 分开标识。
- 失败处理：Central 已发布版本不可覆盖；超时先用 deployment ID 查 Central Deployments，仍在发布则等待，已发布则不重传同版本、不移动旧 Tag，改源码用新版本 Tag。

## 6. Demo 与 Pages 辅助产出

- Demo 只消费公开 API；主题、页面、受控状态、错误提示和示例交互由 Demo 装配；不为示例公开内部状态，包和目录保持一致。（`AGENTS.md`「代码与模块边界」）
- Demo 首页三个底栏页签（组件/效果/设置；API 34+ 用浮动 `NavigationBar`，API 34 以下普通底栏）；二级页统一 `LargeTopAppBar`；目录搜索筛选可导航条目，详情由独立 Activity 展示。（`docs/development/architecture.md`「Demo 契约」+ `docs/guide/demo.md`）
- Demo 主题切换：`DemoApplication` 初始化可选 `CouiMaterial3Style`；开启 Material 配色时组件读取宿主主题，关闭时局部 `CouiTheme` 切换原生外观；Activity 根部安装 `ProvideCouiMenuBackdrop` 窗口源。（`demo.md`「主题与页面装配」）
- Demo 源码导航表见 `demo.md`「源码导航」（DemoApp、DemoCatalog、DemoSecondaryActivity、DemoSecondaryPage、DemoSamples 等）。修改 Demo 前必读 `docs/guide/demo.md` 与架构页 Demo 契约（`AGENTS.md` 任务表）。
- Pages 网页（图标预览站）制作见 `references/coui-icons.md` §3 与 `Pages/GENERATION.md`。
