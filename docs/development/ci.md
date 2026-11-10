# Demo CI 与 PR 检查

需要下载当前 Demo 时，在 main 的提交标题前加 `[ci]`，或在 Actions 手动运行“Demo 打包”。普通 push 跳过打包；提交 PR 后，Android 与 Note 检查自动运行。

## 触发与产物

| 操作                     | 结果                                                                               |
|------------------------|----------------------------------------------------------------------------------|
| main 普通 push           | Demo job 跳过，不占用 Android 构建 Runner                                                |
| main 最新提交标题以 `[ci]` 开头 | 构建 itos 签名的 release，上传 APK 与对应 mapping                                           |
| Actions 手动运行 Demo 打包   | 构建所选受信任分支，日常使用 main                                                              |
| 指向 main 的 PR           | 自动执行 Note、库与 Demo Lint、Demo debug/release 与库发行构建，Android 检查通过后上传 Demo Release APK   |
| Actions 手动运行发布库预览版     | 构建并检查库产物，上传 GitHub Packages；见[发布指南](publishing.md)                               |
| 推送发行 Tag               | 构建、签名并自动发布 Maven Central 库模块；版本规则与凭据见[发布指南](publishing.md#maven-central-发行与变更记录) |

前缀判断使用本次 push 最后一个提交的标题，不扫描之前的提交。GitHub 的 startsWith 比较不区分大小写。条件在 job 层执行，因此普通 push 仍可能显示 skipped 工作流记录。PR 检查不受此前缀限制，也不按文档路径整体跳过，避免必需检查一直 Pending。

Demo 下载位于对应 Actions run 的 Artifacts。本地与 CI 的 versionName 统一由 Demo 构建配置持有，每次发行更新为 Tag 去掉首字母 `v` 后的版本，不追加 CI 或 SHA 后缀。versionCode 均由 Gradle 直接读取当前 HEAD 的完整 Git 历史计数，不另传版本码参数；克隆时保留完整历史。APK 与 mapping 必须来自同一次构建。附件保留期以工作流配置为准，签名密钥不进入附件。

## 签名凭据

本地配置见 [Demo 签名](../guide/demo.md#本地签名)。CI 与维护者本地构建使用同一把 `itos` 密钥，在仓库 Actions Secrets 配置：

| Secret                  | 内容                        |
|-------------------------|---------------------------|
| KEYSTORE                | itos keystore 的 Base64 内容 |
| KEYSTORE_PASSWORD       | keystore 密码               |
| KEYSTORE_ALIAS          | `itos`                    |
| KEYSTORE_ALIAS_PASSWORD | key 密码                    |

例如由维护者上传仓库外的已有密钥，命令中的路径替换为实际路径，不将 Base64 写入终端输出或 Git：

~~~sh
base64 < /path/to/itos.jks | gh secret set KEYSTORE --repo ItosEO/COUI-Kit
~~~

其他三个字段通过 GitHub Settings → Secrets and variables → Actions 填写。PR 不注入这些凭据，使用 Runner 默认测试签名；包括 fork PR 在内，都不需要维护者密钥。构建代码通过 pull_request 执行，不使用 pull_request_target 执行贡献者代码。

Demo 工作流将密钥放在 Runner 临时目录，仅在签名步骤传入同名环境字段；缺少 Secret 会失败，不改用临时证书分发。带签名构建关闭 configuration cache，依赖缓存不存储密钥；任务结束清理临时密钥。

## 检查与失败处理

- [PR Android 工作流](../../.github/workflows/pr-checks.yml)持有实际 Gradle 命令，不执行单测、模拟器或真机测试。
- PR 同时执行 Central Tag 解析器的无凭据自检；Central 发布工作流不由 PR 触发。
- [Note 工作流](../../.github/workflows/verify-notes.yml)检查目录、格式、链接与归档封印，归档比较使用 PR base SHA。
- [Demo 打包工作流](../../.github/workflows/demo-ci.yml)构建 release、检查 APK 签名并上传产物，不创建 Release，不发布库。
- PR Android 检查对 `coui`、`coui-material3` 与 `coui-icons` 分别执行 Lint，生成同版本的本地 Maven 产物及源码、API 文档，并运行共用的[发行检查脚本](../../.github/scripts/verify-maven-artifacts.cjs)。检查覆盖独立坐标、非空归档、模块依赖边界、许可及核心动画依赖版本声明。Maven 产物仅用于本地检查；[预览发布](publishing.md)独立手动触发。

Demo 与 PR 检查的新任务取消同一分支的旧任务；库发布保留正在上传的任务并串行执行。失败时查看 Actions 中的报错与诊断附件，修复原因；不关闭 Lint、R8 或增加整包 keep 来使检查表面通过。Lint 警告按其实际影响处理，不新增历史问题 baseline。

配置主分支必需检查时，使用“Android Lint 与 Demo 构建”和“校验 Note 目录、格式与归档”这两个 job 名称，并先确认它们能在 PR 上完成。维护者审核和 bypass 单独配置。

本地检查命令与权限见[验证与评审](validation.md)。CI 编译成功不代表 GPU、触摸、无障碍或原厂 1:1 效果已经验收。
