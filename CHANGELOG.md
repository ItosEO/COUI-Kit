# 更新记录（CHANGELOG）

COUI-Kit 处于早期预览阶段。本文记录公开 API、行为、接入和兼容要求的变化。日期以提交合并时间为准；版本归档以真实 Git Tag 和发行产物为准。

## [Unreleased]

- 核心移除 Material 3 与 Material Icons 依赖，使用独立文本、图标、菜单定位和顶栏最小布局占位。
- 新增可选 CouiTheme、原生浅深色预设、普通字体和内容上下文；原组件通过默认参数简化装配，保留显式配置与底层 Effect。
- 原生默认不启用额外页面染色；Demo 展示可选 Material 配色映射和染色开关。
- M3 插槽内容需显式接入 COUI 内容色与文字样式，见[迁移说明](docs/guide/compatibility.md#原生主题与默认参数迁移)。

## 已合入 main 的近期改动（待建立发行版本映射）

以下是源码仓库的变更记录，不代表这些特性在每一个 Maven Central 预览版本中都存在。

### 2026-10-30

- [#9](https://github.com/ItosEO/COUI-Kit/pull/9)：按使用场景整理组件文档、快速开始和 README，补全接入示例。

### 2026-10-29

- [#8](https://github.com/ItosEO/COUI-Kit/pull/8)：统一 Touch/Press/Slider/Switch 等持续动画通道，调整中断与重定向。Touch/Press 运动轴收为只读 `State<Float>`，涉及二进制不兼容，原消费者需要重新编译。

### 2026-10-28

- [#7](https://github.com/ItosEO/COUI-Kit/pull/7)：新增 COUI Switch，包含受控点击、拖动吸附、加载和禁用反馈。
- [#6](https://github.com/ItosEO/COUI-Kit/pull/6)：新增普通警告弹窗与输入型弹窗预设，支持底部/居中窗口、受控操作及输入过滤。

### 2026-10-24 至 2026-10-25

- 新增 ChipTab；优化回弹中断和输入交接。
- 完善 Demo 按需签名打包、CI、MPL 2.0/NOTICE，以及 GitHub Packages 开发快照构建与发布入口。

## 已发布版本与源码对应关系

- Maven Central 的版本以 [实际包页面](https://central.sonatype.com/artifact/io.github.itoseo/coui)及远端元数据为准；[README](README.md)中的依赖示例目前使用 `0.0.1-alpha2`。
- 当前可读取 Git refs 没有建立对应发行 Tag，无法据此断言 `0.0.1-alpha1` 或 `0.0.1-alpha2` 分别对应哪个提交。
- 根目录 `VERSION_NAME=0.1.0-SNAPSHOT` 是开发/快照的基础版本，不代表 Maven Central 已公开 `0.1.0`。
- 今后每次不可变版本发布，需记录版本号、发布日期、源代码 Tag/Commit SHA、AAR 校验/发行地址、公开 API 兼容变化和迁移说明。再将上述记录归入对应版本段落。

发布渠道及执行方式见[发布指南](docs/development/publishing.md)。
