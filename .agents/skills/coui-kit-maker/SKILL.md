---
name: coui-kit-maker
description: COUI-Kit 组件库完整制作技能。在 COUI-Kit 仓库（参考 ColorOS 17 凝光视效的 Jetpack Compose 库）搭建宿主接入、新增组件/效果/图标、制作 Material 3 适配、维护文档与决定记录、执行验证/CI/发布时使用；含可复制的依赖配置与安装示例、20 个组件的完整目录与真实调用示例、CouiIcons 全部 111 个公开图标清单，规则逐条来自仓库内真实代码与文档并标注出处。
---

# COUI-Kit 制作

## 0. 适用范围与本 skill 文件地图

仓库根目录是 `COUI-Kit/`（含 `coui`、`coui-icons`、`coui-material3`、`demo`、`docs`、`gradle`、`.github`、`Pages`、`.agents` 等）。所有输出、文档和提交说明使用简体中文；UTF-8 无 BOM。（`COUI-Kit/AGENTS.md`）

| 需要做什么 | 读哪个分册 |
| --- | --- |
| 给宿主工程加依赖、配仓库、接 M3、接 GitHub Packages、查兼容门槛 | `references/install.md` |
| 查有哪些组件、每个组件怎么调用（真实示例）、平台门槛、源码入口 | `references/component-catalog.md` |
| 查有哪些图标、资源名、日夜规则、新增图标怎么落地 | `references/icon-catalog.md` |
| 新增/修改 coui 组件、效果、主题、公开 API | `references/coui-core.md` |
| 新增/修改 coui-icons 图标（含 Pages 预览） | `references/coui-icons.md` |
| 新增/修改 coui-material3 适配 | `references/coui-material3.md` |
| 写文档、写 Note、跑验证、CI、发布、Demo/Pages | `references/docs-release.md` |

## 1. 开始前：按任务阅读仓库文档（`AGENTS.md`「开始前按任务阅读」）

| 任务 | 必须先读 |
| --- | --- |
| 在宿主中接入依赖或升级版本 | `docs/guide/getting-started.md`、`docs/guide/compatibility.md`（本 skill 的 `install.md` 已整理可复制配置） |
| 实现、修复、重构或性能改动 | `docs/development/workflow.md` + `docs/development/coding-style.md` 全文 |
| 新增组件/效果，修改视觉、动画、手势或排查原厂差异 | `docs/development/replication-guide.md` 相关完整章节、当前调用链与对应 Note |
| 新增组件或基础效果 | `docs/effects/README.md`、对应实现与已有消费者，按 `workflow.md#基础效果复用` 确认复用或扩展方案；先查 `references/component-catalog.md` 是否已有同类 |
| 修改模块职责或公开接口 | `docs/development/architecture.md`、`docs/development/api-design.md` 及受影响使用页 |
| 新增、修改或移动普通文档 | `docs/development/documentation.md` 及对应组件/效果页面 |
| 修改 Demo | `docs/guide/demo.md`、`architecture.md#demo-契约` 及所用公开 API |
| 修改签名、CI 或 PR 检查 | `docs/development/ci.md`、`docs/development/validation.md` 及实际工作流 |
| 修改库打包、版本或上传流程 | `docs/development/publishing.md`、`docs/guide/compatibility.md` 及实际发布配置 |

所有任务先检查工作区及目标文件的当前内容，保留用户已有修改；不能用旧快照、摘要或他人结论替代核心文档、原厂关键入口和即将修改的实现。（同上）

## 2. 环境基线（全部读自构建配置）

| 项 | 值 | 出处 |
| --- | --- | --- |
| 模块 | `:coui`、`:coui-material3`、`:coui-icons`、`:demo`（`rootProject.name = "COUI"`） | `settings.gradle.kts` |
| 依赖版本目录 | `gradle/libs.versions.toml`：AGP 9.4.1、Kotlin 2.4.20（工程用 AGP 内置 Kotlin）、compose 1.13.0、material3 1.5.0、coreKtx 1.19.1、activityCompose 1.13.0、dynamicanimation 1.1.0、mavenPublish 0.37.0、dokka 2.3.0、capsule 2.1.3（仅 Demo） | 同文件 |
| Maven 坐标 | `GROUP=io.github.itoseo`、核心 `POM_ARTIFACT_ID=coui`、开发基础版本 `VERSION_NAME=0.1.0-SNAPSHOT` | `gradle.properties` |
| 库构建参数（三个库模块一致） | `minSdk = 31`，`compileSdk = release(37){ minorApiLevel = 2 }`，`aarMetadata.minCompileSdk = 37.1`，`minAgpVersion = "9.1.0"`，Java 17 | 各模块 `build.gradle.kts` |
| Gradle | `org.gradle.configuration-cache=true`、`kotlin.code.style=official`、`android.useAndroidX=true`、`-Xmx2048m` | `gradle.properties` |
| Compose 插件 | `org.jetbrains.kotlin.plugin.compose`（kotlin-compose 别名） | `libs.versions.toml` [plugins] |

## 3. 制作流程总览（先安装，再按产出类型进分册）

### 0. 安装与依赖（`references/install.md`）

最小接入（`docs/guide/getting-started.md`）：

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// 使用组件的模块 build.gradle.kts
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha15")
    // 可选，同版本：
    // implementation("io.github.itoseo:coui-material3:0.0.1-alpha15")
    // implementation("io.github.itoseo:coui-icons:0.0.1-alpha15")
}
```

分册覆盖：Maven Central / GitHub Packages / 源码模块三种接入；M3 初始化与 `LocalCouiDarkTheme`；背景源装配；`minSdk 31`、`minCompileSdk 37.1`、`minAgpVersion 9.1.0`、Java 17 等兼容门槛；`getting-started.md#常见问题` 的排查表。

### A. coui 核心组件与效果 → `references/coui-core.md`

1. 按任务表读文档（§1），检索活跃 Note：`rg --hidden --glob '!.agents/notes/archived/**' '组件名|机制名' .agents/notes/`。（`workflow.md`「决定记录」）
2. 固定目标与证据 → 基础效果复用先行（查 `docs/effects/README.md` 能力表）→ 对照行为 → 实现完整链路 → 双向核查 → 差异输入验证 → 同步文档与 Note。
3. 实现约束：包职责表、公开 API 规则、代码规范、主题体系基线、效果平台门槛，见 coui-core §2–§7。
4. 新增组件前先查 `references/component-catalog.md` 的 20 个组件与真实示例，确认是否已有同类可复用实现。

### B. coui-icons 图标 → `references/coui-icons.md` + `references/icon-catalog.md`

流程：确认目标资源（adb/aapt2）→ 处理资源与颜色（日夜目录、CouiColor）→ 接入 `drawable` + `CouiIcons.kt`（Filled/Outlined/Bitmap/Monochrome）→ 更新 `coui/ICON_SOURCES.md` → `npm ci --prefix Pages && node Pages/generate.mjs` 生成预览 → 核对（名称、日夜颜色、透明度、多色路径、状态方向）。硬规则：vector `android:name` 用 PascalCase；`coui_background` 路径仅供 `AppUpdate` 换背景；生成器对不支持的 XML 报错不静默丢弃。现有 111 个公开入口（Filled 74 / Outlined 30 / Bitmap 7）见 `icon-catalog.md`。

### C. coui-material3 适配 → `references/coui-material3.md`

唯一实现文件 `CouiMaterial3Style.kt` 实现 `CouiStyle` 三接口；初始化契约是 `Coui.initialize(defaultStyle = CouiMaterial3Style())` 一次；角色映射、染色与插槽行为以 `docs/guide/appearance.md` 为准，映射变化必须同步该页。

### D. docs 文档 + Note → `references/docs-release.md` §1–§2

- 文件归属表；component/effect 页面六段结构；示例与措辞红线；提交前 `git diff --check` 与入站链接检查。
- Note：`.agents/skills/write-notes-like-deepseek/SKILL.md` 持有格式；`{lifecycle}/{class}/yyyy-mm-dd-topic.md`；与实现同一个 PR；三条 `node --experimental-strip-types …` 校验命令。

### E. 验证 / CI / 发布 / Demo / Pages → `references/docs-release.md` §3–§6`

- 本地检查（须授权）：lint + assemble + 本地 Maven 发布 + `node .github/scripts/verify-maven-artifacts.cjs`。
- CI 触发表（`[ci]` 前缀、PR 自动检查、手动 Demo 打包）；发布：`v` Tag → Maven Central 三模块同版本同步发布；GitHub Packages 手动 SNAPSHOT；产物检查禁 Compose BOM/额外约束。

## 4. 硬性红线（违反即返工；每条有出处）

1. 新增能力以指定版本的 COUI/Oplus 原厂实现为基准，效果至少 1:1；「看起来相近」不是验收标准；不用另一种同名算法（blur/spring）替换，不用均匀模糊加渐变替代位置可变卷积，不用连续公式替换离散查表。（`workflow.md`「官方 1:1 对齐」）
2. 新组件优先装配已有基础效果；现有实现满足原厂目标时必须复用；不为复用增加只转发参数的包装层；确需新增实现时在 Note 与 PR 说明无法复用的原因和原厂依据。（`workflow.md`「基础效果复用」+ `coding-style.md`）
3. 使用公开 Android / Compose API，不依赖 OEM 私有 SDK、HWUI 符号或厂商 SO；不把厂商二进制复制进源码、assets 或构建依赖。（`AGENTS.md` + `replication-guide.md` §7）
4. coui 不引用 demo、外部宿主模块、应用主题、业务状态、日志框架或第三方 Backdrop；核心不依赖 Material 3 或图标模块；coui-material3 只依赖 coui 与 M3；demo 只消费公开 API。（`AGENTS.md`「代码与模块边界」）
5. 公开入口默认收窄 internal/private；Modifier 为组件第一个参数并默认 Modifier；受控状态与回调；单一业务提交 owner；CANCEL 不提交业务动作；每个独立绘制节点持有自己的可变 renderer；固定参数归所属 Defaults。（`AGENTS.md` + `coding-style.md` + `api-design.md`）
6. 不吞错误、不模拟成功、不新增静默降级；不新增 `!!`；协程捕获 `CancellationException` 后重抛；Shader 创建失败交给宿主决定替代 UI。（`coding-style.md`）
7. 背景、效果和业务状态分开；不建立全局历史缓存；动画、监听和图层随对应节点释放。（`coding-style.md`）
8. 严禁自采样：`CouiBackdrop` 源节点与消费者必须兄弟或外层分离；禁止跨节点共享可变 renderer（`CouiFloatingRenderer`、`CouiShadowRenderer`、`CouiContour`、`CouiEdge`、`CouiMetaballRenderer`、`CouiMenuBlur`、`CouiOverscrollEffect`）。（`docs/effects/README.md` + `backdrop.md`）
9. 依赖与版本：三个模块同版本、同步发布；显式同时声明时必须同版本；同一模块只选一种依赖来源；库 POM/`.module` 不得引入 Compose BOM 或额外 Compose 约束，否则 `verify-maven-artifacts.cjs` 失败。（`publishing.md`、`getting-started.md`、`compatibility.md`）
10. 使用文档与实现进入同一个 PR；公开使用契约变化必须同步页面、示例、目录与入站链接；Note 与 Demo 不能替代使用说明。（`workflow.md`「同步使用文档」+ `AGENTS.md`「文档维护」）
11. 交付约束：只修改当前任务范围；未经用户明确要求不执行 Git 初始化、建分支、commit、push、rebase、reset、merge；默认不新增测试；发行 Tag 创建与推送须有当前任务授权；密钥不进 Git、日志或附件。（`AGENTS.md`「交付约束」）
12. 验证记录不伪造：静态检查、替身检查或网页预览通过不能写成构建、真机或 1:1 效果通过；报告真实结果与未验证行为。（`replication-guide.md` §6 + `validation.md` + PR 模板）
13. 已批准特化不自动撤销：菜单组合描边、顶栏强度/收起速度、滑条 G2 轨道、搜索避让等是既有约束，对应 Note 持有理由和范围；新增差异单独说明和评审。（`workflow.md`「官方 1:1 对齐」+ `docs/guide/customizations.md`）

## 5. PR 交付（`.github/pull_request_template.md` 五节）

1. 问题与结果：触发场景、原行为、修改后行为。
2. 官方依据与效果：原厂版本/哈希、调用入口与实际分支；主要操作及完成/中断/取消路径对照；既有特化、新差异及授权依据；不涉及原厂行为时说明原因。
3. 影响范围：公开 API、单位、状态 owner、平台与依赖要求；Demo 和已知消费者调整；迁移方式；相关 Note 路径（纯机械修改说明无需新 Note 的原因）。
4. 使用文档：对应 component/effect 页面和目录、同步内容、改名/移除的链接与迁移处理、无需修改时说明契约为何不变。
5. 验证：实际执行的命令和结果；未执行项写「未执行」及原因；能暴露差异的输入反例；仍需验收项。

功能分支进 main（自动代理默认 `codex/` 前缀）；PR 说明最终行为、原厂目标与主要路径对照、已处理差异、公开 API 与迁移影响、文档、Note、真实检查结果与待验收项。（`workflow.md`「提交 PR」）

## 6. 执行顺序（每步可验证）

| 步骤 | 动作 | 验证方式 |
| --- | --- | --- |
| 1 | 读 §1 任务表指定文档 + 对应分册；宿主接入先读 `install.md` | 能复述目标、依赖坐标、平台门槛 |
| 2 | 查目录：组件查 `component-catalog.md`，图标查 `icon-catalog.md`，效果查 `coui-core.md` §7 | 确认是否已有可复用实现/图标 |
| 3 | 检索活跃 Note（rg 命令） | 命中项分类：无关/部分重叠/完全吸收/过时提案 |
| 4 | 实现（分册 A/B/C/D 对应流程） | 实现与原厂关键入口双向核对留有记录 |
| 5 | 同步使用文档与 Note（分册 D） | 目录可达、链接有效、`git diff --check` 通过 |
| 6 | Note 校验三脚本 + 静态/文档检查（分册 E） | 脚本退出码 0；真实输出留档 |
| 7 | Android/发布检查（仅获授权时，分册 E） | lint/assemble/publish 输出；`verify-maven-artifacts.cjs` 通过 |
| 8 | PR（§5 模板） | 五节齐全；未验证项如实标注 |

## 7. 待补充信息（仓库中不存在，不得编造）

1. **原厂参考材料清单**：目标组件对应的 ColorOS/COUI 原厂 APK 版本、构建标识、SHA-256 与提取文件位置。`replication-guide.md` §7 明确研究材料独立于源码仓库保存；本仓库不含任何原厂二进制。恢复方法按该节六问清单。
2. **签名密钥**：`itos` keystore 只存在于仓库外（`signing.properties.example` 为模板，CI 用 Actions Secrets）；仓库内无密钥文件，不得索取或伪造。
3. **Maven Central 当前已发布版本与远端状态**：文档示例为 `0.0.1-alpha15`，实际可用版本以远端解析和发行元数据为准（`publishing.md`）。
4. **GitHub Pages 部署域名**：`Pages/GENERATION.md` 只说明本地预览 `http://localhost:8080/icons/` 与 workflow 部署方式，未记录最终站点 URL。
5. **消费者实机兼容矩阵**：`publishing.md` 要求每次发行时用独立消费者确认 SDK/AGP/Compose 组合并记录；仓库无历史副本。
6. **各组件效果参数的具体数值**：任何参数以 `coui/src/main/java/com/itos/coui/` 当前源码为准，不在本 skill 内复制（`replication-guide.md` 明确不复制参数表或 Shader 全文）。
7. **`.github/workflows/*.yml` 的实际 Gradle 命令与附件保留期**：修改 CI 前必须读取对应 yml 当前内容（`ci.md` 声明由工作流持有）。

## 8. 产出核对清单（完成后逐项自查）

- [ ] 安装/升级：仓库、依赖坐标、版本一致性与 `buildFeatures.compose` 已核对；可选模块同版本；M3 适配已 `Coui.initialize` 一次；`LocalCouiDarkTheme` 按需提供。（`install.md`）
- [ ] coui：新入口落在正确组件族包；无转发参数包装层；Defaults 归位；renderer 逐节点持有；主题默认值走 CouiTheme/初始化策略链。（coui-core §2/§4/§6/§7）
- [ ] 组件：与 `component-catalog.md` 的现有入口对照，调用方式、平台门槛（31/33/34+）与父子容器约束一致；示例只用公开 API。（`component-catalog.md`）
- [ ] coui-icons：XML 命名与日夜目录合规；`CouiIcons.kt` 入口与 Monochrome 复用正确；`ICON_SOURCES.md` 已更新；Pages 预览核对完成且未把网页显示当真机验证。（`coui-icons.md` + `icon-catalog.md` §8）
- [ ] coui-material3：只改 `CouiMaterial3Style.kt` 相关行为；映射表已同步 `appearance.md`；未把 M3 类型引入核心。（coui-material3 §1/§5）
- [ ] docs：component/effect 页面按六段结构更新并入目录 README；入站链接完整；`git diff --check` 通过。（docs-release §1）
- [ ] Note：非平凡变更已有 `{lifecycle}/{class}/yyyy-mm-dd-topic.md`，三脚本通过；纯机械修改未新增 Note。（docs-release §2）
- [ ] 验证与 PR：实际命令与结果如实记录；未执行项标「未执行」；PR 五节齐全。（docs-release §3 + §5）
- [ ] 发布（如涉及）：三模块同版本；Tag 指向 main；CHANGELOG 与 README 版本号已同步；产物检查脚本通过。（docs-release §5）
- [ ] 待补充项（§7）需要时已向维护者确认，未编造。
