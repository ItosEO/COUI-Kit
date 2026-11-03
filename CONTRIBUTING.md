# 参与 COUI 开发

修复组件、增加效果或改进文档时，先确认对应的公开入口和现有行为，再在同一个 PR 中交付实现、用法和必要的依据。

## 开始工作

1. 阅读[开发流程](docs/development/workflow.md)与[代码规范](docs/development/coding-style.md)。
2. 新增或修改原厂视觉、动画、输入行为时，阅读[原厂分析与 1:1 复刻方法](docs/development/replication-guide.md)，确认目标版本、实际消费者和退出路径。
3. 涉及模块或公开接口时，阅读[架构](docs/development/architecture.md)和[API 设计](docs/development/api-design.md)。
4. 按[文档维护规则](docs/development/documentation.md)确定要同步的 component、effect、指南和示例。

开发入口与专题导航见[开发文档](docs/development/README.md)。AI 代理另须遵守 [AGENTS.md](AGENTS.md) 的操作权限与任务阅读要求。

## 使用 AI 辅助开发

- 本项目要求使用前沿模型，例如 Astra、6.1 Sol、Opus 5.5，不使用 DeepSeek 等模型承担项目开发。
- 给 AI 指定仓库中最接近的组件、调用方和文档作为参照，让它先阅读，再沿用现有写法、命名、调用逻辑和状态归属。文档参照同类页面组织，代码优先复用已有入口和基础效果；具体参数与接口以当前源码为准。参照与目标存在差异时，先核实差异，不机械复制。

可以这样提出任务：

> 先阅读 AGENTS.md。参照仓库中的【现有组件及其调用方】实现【目标行为】，沿用现有调用逻辑和状态归属；使用文档参照【同类页面】的写法。先确认可复用的实现，只修改本次任务需要的内容。

## 提交变更

使用功能分支，通过 PR 进入主分支。PR 按[统一模板](.github/pull_request_template.md)说明结果、官方依据、公开 API 与文档影响、检查结果和待验收项。维护者另行授权的直接操作按该授权执行。

行为、架构、跨文件契约和流程等非平凡变更，按[开发流程](docs/development/workflow.md#决定记录)更新或新增 Note。纯机械修改不额外建立记录。

组件及效果的新增、重命名、移除、参数语义、平台要求或装配方式变化，必须同步普通使用文档。PR 需要列出所改文档；没有使用影响时说明原因。只写 Note 或修改 Demo 不能替代使用文档。

验证方式见[验证与评审](docs/development/validation.md)。构建与设备操作按当前任务授权执行，填写实际结果。

指向 main 的 PR 自动执行 Note、Lint、Demo debug/release 和库本地 Maven 发行构建，不运行单测，不需要固定签名或包写入凭据。失败时查看 Actions 诊断，修复后再请求合并。Demo 的按需打包与附件下载见 [CI 指南](docs/development/ci.md)，Central 的 Tag 自动发布与 Packages 手动发布见[发布指南](docs/development/publishing.md)。自动检查成功不替代维护者审核或原厂效果验收。

## 许可与第三方内容

提交自有源码时遵循本项目 [MPL 2.0](LICENSE)。引入第三方代码或资源时，保留其许可、版权与来源声明，并同步 [NOTICE](NOTICE)，不得将第三方内容直接改标为本项目许可。分叉发行的名称和 Logo 使用见[名称与标识政策](TRADEMARKS.md)。
