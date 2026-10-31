# Agent Note: GitHub Packages 库预览发布

Status: implemented

## Problem

宿主需要接入明确提交对应的 COUI 库，现有 Demo 附件只供下载安装，不能提供传递依赖或验证库的发行结构。维护者选择先接 GitHub Packages，保留普通 push 不发布的按需规则，Maven Central 后续单独处理。

## Decision

库采用 Vanniktech 的 Android release 单变体 publication，使用 Dokka 生成真实 HTML 文档，公共坐标与基础版本由根 Gradle 属性持有。GitHub Packages 发布版本在基础版本中加入源码提交 SHA，以 SNAPSHOT 提供开发接入；不使用 Demo Git 提交数或显示版本名。

手动工作流先生成无凭据的本地 Maven 文件仓库，核对 AAR、源码、文档、许可与 Gradle 依赖元数据，再上传并读取远端版本元数据。Compose BOM 与 DynamicAnimation 严格约束由 `.module` 检查，不能以 POM 或 AAR 存在代替。PR 增加同一个本地发行目标，及早暴露打包与 Dokka 配置问题，不增加单测或远端上传。

[可选 M3 模块](../architecture/2026-10-31-optional-material3-style.md)与核心同版本，单独 artifactId 由模块配置持有。两个模块均生成本地 Maven 目标，PR 与发布共用 `.github/scripts/verify-maven-artifacts.cjs`，同时核对核心无 M3、适配器依赖同版本核心与 M3。上传与远端确认覆盖两个 artifact；渠道和凭据规则不变。

上传使用本仓库 GITHUB_TOKEN，只在上传和远端读取步骤传入；不接 GPG、Central Token 或 Demo 密钥，不缓存带凭据的配置。发布任务保留正在执行的上传。读取凭据由消费者自行配置，私有 Maven 包沿用仓库访问边界。

源码与文档归档从根声明及既有 Skia 许可生成随包声明，不维护另一份许可副本。使用说明归普通指南，维护者操作归开发指南，AGENTS 只维护任务入口与权限边界。

归档声明通过显式导入的 `org.gradle.jvm.tasks.Jar` 基类装配。AGP 源码任务继承旧包中的 Jar，而 Vanniktech 文档任务直接继承新基类；只匹配 Kotlin DSL 默认导入的旧类型会漏掉文档包。检查失败须指出具体归档与缺失声明。

## Alternatives considered

- **先使用 JitPack。** 能直接按提交消费，但引入另一套远端构建环境和私有仓库授权；当前先用 GitHub Actions 已有工具链构建并上传预览包。
- **先完成 Central 发布。** 普通用户匿名接入方便，也能发布 alpha/beta/rc；维护者当前选择先验证 GitHub Packages，Central 命名空间、GPG 和正式版本发布另行配置。
- **直接上传而不生成本地 Maven 目标。** 步骤更少，但可能在远端才暴露缺少文档、源码或传递约束；先检查发行结构，并让 PR 验证同一构建目标。

## Consequences

宿主可通过完整 Maven 坐标验证指定源码提交，代价是本地和跨仓库 CI 需要读取认证；公开 Maven 包也不支持匿名下载。SNAPSHOT 包可重复发布，提交 SHA 不保证产物不可变，回看一次发布还需 Actions run；私有包需要考虑存储和下载额度。

Vanniktech 与 Dokka 是新的构建依赖，PR 会增加库发行与文档生成时间。源码、公开 API、绘制行为及 Demo 消费方式不改变。Maven Central 与 JitPack 不属于当前接线范围。

本地只执行差异、Note、文档、YAML 与脚本静态检查，不运行 Android 构建。实际工具链兼容、包写入和远端读取以对应 Actions 结果为准，工作流尚未成功时不声称预览包可用。

首次 main 发布的发行检查发现文档归档缺少许可通知，上传因此被阻止。Gradle 本地类层次和 Vanniktech 源码确认上述新旧 Jar 类型边界；修复继续由真实发行检查验证，不跳过许可断言。

## Existing-note audit

- [独立工程](../architecture/2026-10-23-standalone-coui-demo.md)：部分重叠，本篇增加库发行入口；保留模块边界和 Demo 的 project 消费方式。
- [Demo CI](2026-10-25-demo-signing-ci.md)：部分重叠，本篇增加 PR 本地库发行构建与独立手动发布；保留既有 Demo 按需触发、签名与无单测决定。
- [MPL 与名称政策](2026-10-25-mpl-and-project-name-policy.md)：部分重叠，本篇使既有声明进入源码与文档产物，不改变代码许可或名称政策。

## References

- [GitHub Gradle 仓库](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)
- [Vanniktech Android 单变体发布](https://vanniktech.github.io/gradle-maven-publish-plugin/what/#android-library-single-variant)
- [Dokka Gradle](https://kotlinlang.org/docs/dokka-gradle.html)
