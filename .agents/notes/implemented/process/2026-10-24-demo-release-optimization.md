# Agent Note: Demo release 使用标准 R8 优化

Status: implemented

## Problem

Demo release 未启用代码与资源优化，未使用内容会进入最终 App。用户要求正常启用 R8，不能通过整包保留或关闭优化使 release 仅在名称上成立。

## Decision

在 Demo 的 release build type 使用当前 AGP 的 `optimization.enable`，统一开启代码压缩、优化、混淆及资源裁剪。默认优化规则由 AGP 提供，外部依赖的 consumer rules 继续参与合并；不同时维护旧 minify/shrink DSL 或重复添加默认文件。debug 和默认测试签名不变。

删除没有有效规则的 keepRules 模板。当前源码检索未发现自有的字符串反射、按名称加载或 JNI 入口，因此不添加包级 keep、dontwarn 或全局关闭开关。Shader uniform 名称不依赖 Kotlin 类名。以后出现真实动态入口时，围绕该入口添加必要规则，不保留整个 COUI、Compose 或 Kotlin 包。

公共 COUI 模块保持未预混淆，由最终 App 的 R8 连同实际使用链一起优化；不提前冻结可裁剪实现，也不改变对外 API、依赖版本或发行配置。构建行为由 [Demo 配置](../../../../demo/build.gradle.kts) 持有。

## Alternatives considered

- **使用旧版 minify/shrink 开关并显式添加默认 ProGuard 文件。** 适用于旧 AGP，但当前版本已有统一入口和默认规则；使用现有 API，避免两套配置叠加。
- **为 COUI 与 AndroidX 整包添加 keep。** 可以减少被裁剪的代码，却会阻止本次需要的优化；当前没有要求此范围保留的动态调用。

## Consequences

[Demo 固定签名与 CI](2026-10-25-demo-signing-ci.md) 扩展签名配置并通过真实构建验证优化链，保留本篇的 R8 配置与未预混淆库边界。

release 可以裁剪未使用的组件、图标和资源，并优化已用调用链。调试 release 堆栈需要保留该次构建的 mapping，实际体积和运行效果以用户构建结果为准。

静态验证核对当前 AGP 的 Optimization/ApplicationBuildType API、内部代码与资源开关合并、默认规则装配、无禁用优化属性和无宽泛保留规则。未运行 Gradle 分析任务、Android 构建或设备测试，未产生 R8 分析报告或体积数据。

## Existing-note audit

- [独立工程与 Demo](../architecture/2026-10-23-standalone-coui-demo.md)：部分重叠，保留模块归属、构建基线与测试签名，互链；本篇增加 release 优化策略。
- [Demo 精简](../feature/2026-10-24-demo-focused-showcase.md)：展示选择不变，本篇只影响最终 App 构建优化。
- 活跃笔记未发现其他 R8 决定或待处理提案，不替代或归档无关笔记。
