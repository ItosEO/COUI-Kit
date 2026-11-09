# COUI 开发文档

新增组件或修复行为时，从[开发流程](workflow.md)开始。先确认原厂目标和当前链路，再实现、评审并同步使用文档。

| 要确认的内容                              | 文档                                     |
|-------------------------------------|----------------------------------------|
| 工作顺序、原厂对齐要求、决定记录与 PR                | [开发流程](workflow.md)                    |
| 如何定位原厂消费者、还原算法和排查差异                 | [原厂分析与 1:1 复刻方法](replication-guide.md) |
| 从目标应用筛选、转换和接入图标                     | [新增图标](icon-workflow.md)               |
| 已确认的视觉与交互定制                         | [与原厂的定制差异](../guide/customizations.md) |
| Kotlin、Compose、资源、输入及错误处理           | [代码规范](coding-style.md)                |
| 模块、包职责与状态归属                         | [架构](architecture.md)                  |
| 公开入口、单位、资源与 API 变更                  | [API 设计](api-design.md)                |
| 新增或修改 component / effect 文档         | [文档维护](documentation.md)               |
| 对照、检查、设备验收与诊断                       | [验证与评审](validation.md)                 |
| Demo 打包、固定签名、PR 检查与失败诊断             | [CI 与 PR 检查](ci.md)                    |
| Maven Central、GitHub Packages 与版本映射 | [发布渠道](publishing.md)                  |
| 自动化测试建设顺序与场景                        | [自动化测试建议](testing-plan.md)             |

AI 代理的任务阅读顺序与执行权限由 [AGENTS.md](../../AGENTS.md)规定。
