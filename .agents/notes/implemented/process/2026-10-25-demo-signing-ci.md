# Agent Note: Demo 固定签名与按需 CI

Status: implemented

## Problem

临时 Runner 会生成不同的默认测试密钥，下载的新 Demo 可能无法覆盖安装旧包。仓库也只有 Note 结构校验，PR 没有验证 Android 编译、Lint 和 release 优化链；每次普通 push 打包又会产生不需要的构建消耗。

## Decision

Demo 使用独立固定测试密钥，本地通过 signing.properties 提供，CI 通过同名环境字段提供；环境配置作为完整一组优先于文件，缺项明确失败，完全未配置时保留 AGP 默认测试签名。实际材料不进入 Git、缓存或构建附件。

Demo 打包仅由 main 的 [ci] 提交标题或手动入口触发，保留 release 的 R8 与 mapping；PR 独立运行 Note、Lint 和 debug/release 构建，使用临时默认测试签名，不读取固定密钥，不增加或执行单测、模拟器测试。工作流只读仓库，新任务取消同一分支的旧任务。

Demo 的 versionCode 在 Gradle 中直接读取当前 HEAD 的 Git 提交总数，本地与 CI 共用，不保留单独覆盖参数或固定回退值。显示版本名与系统版本码分开，CI 后缀只含 ci 与短 SHA，不重复提交计数。构建需要完整 Git 历史，Git 读取失败保持可见。

本轮不接 JitPack、Maven Central、库发行或外部宿主消费切换。使用者签名步骤归 Demo 指南，CI 与协作步骤归开发验证文档，AGENTS 保留执行授权边界。

## Alternatives considered

- **继续使用 Runner 默认测试密钥分发。** 无需管理凭据，但连续下载的 APK 可能无法覆盖安装；PR 可以使用它，长期下载的 Demo 使用固定密钥。
- **每次 push 自动打包。** 使用方便且结果及时，但不符合按需构建要求；PR 自动检查与 Demo 主动打包分别触发。
- **为首次 CI 建立算法单测与模拟器矩阵。** 能覆盖更多运行行为，但用户明确移除单测，先验证静态检查与真实编译，不把构建结果视为视觉验收。

## Consequences

切换证书后，旧测试包需要卸载一次。GitHub 的提交标题判断发生在 job 条件，普通 push 可能保留 skipped 记录。GPU 效果与原厂一致性仍需设备验收。固定测试密钥仅用于 Demo，不复用其他 App 或 Maven GPG 密钥。

本地与 CI 使用同一密钥时可以连续覆盖安装；代价是需要备份并维护固定测试密钥与 Secrets。PR 使用临时证书，不作为固定签名的下载渠道。工作流尚未合入 main 时，不提前添加全仓必需状态，避免其他 PR 因缺少工作流而被阻塞；合入后再接入状态规则，已有审核与 bypass 保留。

本地执行库 Lint、Demo release Lint 和 debug/release 构建通过，两种 APK 的签名证书一致。首次 Lint 暴露菜单绘制中两处 API 33 调用缺少可见版本判断；在实际调用处补齐检查，保留工厂的旧平台 null 返回、Gaussian 分支及算法。未添加 baseline、关闭 Lint 或改变依赖。

签名错误路径通过实际 Gradle 配置验证：完整文件用于固定签名；部分环境字段不能混用完整文件；密钥路径不存在时失败；完全无配置时 debug/release 均选择 AGP 默认测试签名。Note、项目文档链接、YAML 与差异检查通过。

远端验证使用 PR 与独立临时分支。临时分支仅扩展 push 的分支过滤，核对普通 push 跳过与 [ci] push 打包；手动入口使用 PR 分支，正式配置保留 main 的 push 限制。远端结果由 PR Checks 和对应 Actions 记录持有，不把任务启动视为构建成功；main 的实际 push 需要合入后验收。

## Existing-note audit

- [独立工程](../architecture/2026-10-23-standalone-coui-demo.md)：部分重叠，本篇扩展测试签名策略，保留独立模块与默认无配置入口。
- [开发流程](2026-10-23-developer-guide-and-pr-notes.md)：部分重叠，本篇扩展远端 Android 检查，保留 Note 与原厂评审流程。
- [R8 优化](2026-10-24-demo-release-optimization.md)：部分重叠，保留优化配置，仅验证其实际构建与附件链。
