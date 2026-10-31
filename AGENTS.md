# AI 开发执行规则

## 开始前按任务阅读

开发与文档维护从[贡献指南](CONTRIBUTING.md)进入，专题导航见[开发文档](docs/development/README.md)。流程、原厂分析与 1:1 复刻、代码规范及 component / effect 文档写法按下面的任务表阅读。

| 任务                        | 必须先读                                                                                                      |
|---------------------------|-----------------------------------------------------------------------------------------------------------|
| 实现、修复、重构或性能改动             | 完整阅读[开发流程](docs/development/workflow.md)和[代码规范](docs/development/coding-style.md)                         |
| 新增组件/效果，修改视觉、动画、手势或排查原厂差异 | 继续阅读[原厂分析与 1:1 复刻方法](docs/development/replication-guide.md)的相关完整章节、当前调用链与对应 Note                          |
| 新增组件或基础效果                 | 先检查[效果目录](docs/effects/README.md)、对应实现与已有消费者，按[基础效果复用规则](docs/development/workflow.md#基础效果复用)确认复用或扩展方案    |
| 修改模块职责或公开接口               | [架构](docs/development/architecture.md)、[API 设计](docs/development/api-design.md)及受影响的使用页                   |
| 新增、修改或移动普通文档              | [文档维护规则](docs/development/documentation.md)及对应组件/效果页面                                                     |
| 修改 Demo                   | [Demo 指南](docs/guide/demo.md)、[架构的 Demo 契约](docs/development/architecture.md#demo-契约)及所用公开 API            |
| 修改签名、CI 或 PR 检查           | [CI 与 PR 检查](docs/development/ci.md)、[验证与评审](docs/development/validation.md)及实际工作流；不得复制本地密钥或密码到 Git、日志或附件 |
| 修改库打包、版本或上传流程             | [预览版发布](docs/development/publishing.md)、[兼容要求](docs/guide/compatibility.md)及实际发布配置                        |

所有任务先检查工作区及目标文件的当前内容，保留用户已有修改。不能用旧快照、摘要或其他代理结论替代核心文档、原厂关键入口和即将修改的实现。

## 原厂分析与实现流程

- 新增 COUI 能力以指定版本的 COUI/Oplus 原厂实现为基准，效果至少 1:1，尽可能使用相同算法、参数语义、执行顺序和状态转换。流程细则由[开发流程](docs/development/workflow.md)持有。
- 先确认目标页面实际使用的组件、主题覆盖与运行分支，再沿参数生产、状态变化和真实绘制消费者追踪。算法进入 JNI / 系统扩展时继续追实际 native 分支；不按 setter 名称或截图猜实现。
- 新组件优先装配已有基础效果。现有实现满足原厂目标时必须复用，不因组件不同而复制 Shader、模糊核、混色、轮廓光或阴影算法；不足时先评估最小扩展公共 effect。确需新增实现时，在 Note 与 PR 说明候选实现、无法复用的原因和原厂依据，具体流程见[基础效果复用](docs/development/workflow.md#基础效果复用)。
- 对含输入与动画的改动，核对受影响的正常完成、反向、未达到拖动阈值的 UP、CANCEL、父容器接管、移除及再次进入。复用公共实现时同步检查其调用方。
- 实现整条链后做原厂 → 移植、移植 → 原厂的双向核查。主实现者亲自复核核心依据和代码，再选择能暴露差异的输入验证；参数一致、终点收敛或静态检查不等于 1:1 验收。
- 原厂行为、公开平台适配、项目主动特化分别说明。保留已批准特化；公开接口表达不了的路径写明差异，按当前授权处理，不自行换近似算法或新增静默降级。
- 使用公开 Android / Compose API，不依赖 OEM 私有 SDK、HWUI 符号或厂商 SO。研究材料独立管理，不把本机路径、厂商二进制或外部宿主装配写入依赖。

## 代码与模块边界

- coui 只持有通用 COUI 组件、效果、采样、几何、输入、动画及预设，依赖 Android、Compose 和 AndroidX；不得引用 demo、外部宿主模块、应用主题、业务状态、日志框架或第三方 Backdrop。
- coui-material3 是可选适配模块，只依赖 coui 与 M3；它在消费处读 MaterialTheme，不把 M3 类型、根色板缓存或运行时全局主题状态带入核心。初始化与局部覆盖契约见[外观指南](docs/guide/appearance.md)。
- demo 只消费公开 API，主题、页面、受控状态、错误提示和示例交互由 Demo 装配。不为示例公开内部状态，包和目录保持一致。
- 公开入口默认收窄为 internal 或 private，具体规则由[API 设计](docs/development/api-design.md)持有。Modifier 为组件第一个参数并默认 Modifier；保留受控状态、单一业务提交 owner、无障碍与取消收尾。
- 固定参数归所属 Defaults；每个独立绘制节点持有自己的可变 renderer。完整实现习惯由[代码规范](docs/development/coding-style.md)持有。
- COUI 只提供融合算法和跟随回弹，不加入记录侧滑、删除确认或业务提交。基础阴影同时保留便捷入口和低层接口。
- 输出、文档和提交说明使用简体中文；UTF-8 无 BOM。用户文案进入所属模块资源，默认英文并同步已有语言，Demo 维护简体中文。

## 普通文档必须同步

- 新增或修改 component / effect 的使用契约时，按[文档维护规则](docs/development/documentation.md)在同一个 PR 更新普通使用文档。Note 与 Demo 都不能替代使用说明。
- 新增入口：建立对应页面并加入[组件目录](docs/components/README.md)或[效果目录](docs/effects/README.md)。重命名、移动或移除入口：同步示例、目录、入站链接与迁移说明。
- 状态、回调、参数语义、平台、失败行为、采样、坐标或资源生命周期变化：同步对应页面和受影响消费者；公共要求在所属页面完整维护，其他位置链接。
- 实现参数由代码和构建配置持有。仅调参且契约不变时不复制数值表；纯内部修改在 PR 说明无需更改普通文档的原因。
- 文档示例必须调用当前公开 API，说明宿主依赖和作用域，不调用 Demo 私有函数。PR 列出文档路径及使用影响。
- 根 README 提供项目介绍与使用导航，AGENTS 提供开发任务的阅读入口；贡献流程见 CONTRIBUTING。普通文档当前使用 Markdown；库预览发布按发布指南维护，不自行接入网站、Maven Central 或 JitPack。

## Note 与交付

- 非平凡变更使用仓库内 [write-notes-like-deepseek](.agents/skills/write-notes-like-deepseek/SKILL.md)，先检索活跃决定，再更新或新增 Note。纯机械修改不新建；已有用户授权不因 Skill 通用确认流程失效。
- 只修改当前任务范围。未经用户明确要求，不执行 Git 初始化、创建分支、commit、push、rebase、reset 或 merge。贡献流程要求 PR 不代表代理已获 Git 操作授权。
- 开发通过功能分支与 PR 进入主分支，使用 [PR 模板](.github/pull_request_template.md)。维护者另行授权的直接操作按该授权执行；不自行合并或推送。
- 默认不新增测试，PR 工作流执行 Note、Lint、Demo debug/release 和库本地 Maven 发行构建，不加入单测、模拟器或远端上传任务。按风险执行必要的差异、引用及针对性静态检查，参见[验证与评审](docs/development/validation.md)，报告真实结果与未验证行为。
- 已配置的 GitHub CI 可按其触发规则执行构建与 Demo 测试签名；库上传独立手动触发，只向上传及远端确认步骤传入包凭据。本地不得自行运行 Android 构建、Gradle Sync、Preview、安装、签名、发布或设备操作；用户当前明确授权的验证按该授权执行。不自行增加外部宿主消费切换。
