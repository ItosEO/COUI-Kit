# Agent Note: Demo 固定签名与按需 CI

Status: proposed

## Problem

临时 Runner 会生成不同的默认测试密钥，下载的新 Demo 可能无法覆盖安装旧包。仓库也只有 Note 结构校验，PR 没有验证 Android 编译、Lint 和 release 优化链；每次普通 push 打包又会产生不需要的构建消耗。

## Proposal

Demo 使用独立固定测试密钥，本地通过 signing.properties 提供，CI 通过同名环境字段提供；环境配置作为完整一组优先于文件，缺项明确失败，完全未配置时保留 AGP 默认测试签名。实际材料不进入 Git、缓存或构建附件。

Demo 打包仅由 main 的 [ci] 提交标题或手动入口触发，保留 release 的 R8 与 mapping；PR 独立运行 Note、Lint 和 debug/release 构建，使用临时默认测试签名，不读取固定密钥，不增加或执行单测、模拟器测试。工作流只读仓库，新任务取消同一分支的旧任务。

本轮不接 JitPack、Maven Central、库发行或外部宿主消费切换。使用者签名步骤归 Demo 指南，CI 与协作步骤归开发验证文档，AGENTS 保留执行授权边界。

## Alternatives considered

- **继续使用 Runner 默认测试密钥分发。** 无需管理凭据，但连续下载的 APK 可能无法覆盖安装；PR 可以使用它，长期下载的 Demo 使用固定密钥。
- **每次 push 自动打包。** 使用方便且结果及时，但不符合按需构建要求；PR 自动检查与 Demo 主动打包分别触发。
- **为首次 CI 建立算法单测与模拟器矩阵。** 能覆盖更多运行行为，但用户明确移除单测，先验证静态检查与真实编译，不把构建结果视为视觉验收。

## Acceptance criteria

- 本地完整签名配置用于 debug/release；未配置时可构建，缺项或错误路径明确失败。
- 普通 push 跳过 Demo 构建，[ci] 与手动入口可生成签名 APK 和 mapping，签名与本地相同。
- PR 无签名凭据完成 Note、Lint 和两种构建，失败保持可见，远端必需检查不替代维护者审核。
- 文档、工作流和 Note 校验通过，实测结果与未覆盖设备行为如实记录。

## Risks

切换证书后，旧测试包需要卸载一次。GitHub 的提交标题判断发生在 job 条件，普通 push 可能保留 skipped 记录。GPU 效果与原厂一致性仍需设备验收。固定测试密钥仅用于 Demo，不复用其他 App 或 Maven GPG 密钥。

## Existing-note audit

- [独立工程](../../implemented/architecture/2026-10-23-standalone-coui-demo.md)：部分重叠，本篇扩展测试签名策略，保留独立模块与默认无配置入口。
- [开发流程](../../implemented/process/2026-10-23-developer-guide-and-pr-notes.md)：部分重叠，本篇扩展远端 Android 检查，保留 Note 与原厂评审流程。
- [R8 优化](../../implemented/process/2026-10-24-demo-release-optimization.md)：部分重叠，保留优化配置，仅验证其实际构建与附件链。
