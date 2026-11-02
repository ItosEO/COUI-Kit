# 更新记录（CHANGELOG）

COUI-Kit 处于早期预览阶段。本文记录公开 API、行为、接入和兼容要求的变化。日期以提交合并时间为准；版本归档以真实 Git Tag 和发行产物为准。

## 未发布（PR #15）

以下迁移只针对 PR 开发分支中的临时接口，不表示已合入 main 或进入现有发行版本。

- CouiSearchBarColors 的 background 更名为 fallbackBackground，仅负责 API 31–33 的普通填色；API 34 起继续使用所属模糊材质，文字、图标与清除颜色仍可显式覆盖。
- CouiSettingsGroup 移除 topSpacing 参数，保留按标题选择的默认顶部留白。删除显式传入的默认值；额外外部留白使用 Modifier.padding。该 Modifier 不抵消内部默认留白；需要完全自定组间距时，由宿主组合标题、卡片与内容。CouiPreferenceLayout 移除 minHeight 参数，改用 Modifier.heightIn 或 height，内部默认行高服从外部约束。内部 contentPadding 与 leadingSize 参数保留。
- CouiTextKind 移除没有消费者的 Title、Message、Button、Input、Counter 角色；弹窗使用已有 Dialog 组件内部排版，自定义文字使用 Compose TextStyle。设置页、搜索与页面标题角色及 onLineCount 回调保留。接入说明见[设置页布局](docs/components/settings.md)。

## [0.0.1-alpha3] - 2026-11-01

`coui` 与 `coui-material3` 同版本发行。源码为 [v0.0.1-alpha3](https://github.com/ItosEO/COUI-Kit/tree/v0.0.1-alpha3)，提交 `e40c1c413b68fa43924ac6e48620ad3e9d703a08`。发行文件见 [coui](https://repo.maven.apache.org/maven2/io/github/itoseo/coui/0.0.1-alpha3/) 与 [coui-material3](https://repo.maven.apache.org/maven2/io/github/itoseo/coui-material3/0.0.1-alpha3/)。两个模块均通过发行结构、GPG 签名和校验和检查。

升级需重新编译消费者，不承诺二进制 ABI。设备上的字体、嵌套主题和最小交互尺寸仍需验收，构建通过不表示视觉兼容已全部确认。

- 核心移除 Material 3 与 Material Icons 依赖，使用独立文本、图标和菜单定位；原生策略保留原最小布局占位。
- 新增可选 CouiTheme、原生浅深色预设、普通字体和内容上下文；原组件通过默认参数简化装配，保留显式配置与底层 Effect。
- 新增可选 `coui-material3` 模块，通过 `Application.onCreate` 中的 `Coui.initialize(defaultStyle = CouiMaterial3Style())` 接入。组件在消费处读取 M3 颜色、字体和内容上下文；顶栏按钮恢复 M3 最小交互尺寸语义。
- `LocalCouiDarkTheme` 接收宿主模式；`CouiTheme` 支持局部原生或自定义外观。原生默认 tint 透明；M3 菜单与底栏默认使用宿主 surface 作已有色相层。
- Chip 与 Dialog 只接入宿主字体族，保留原生排版。Dialog 的布局测量和绘制共同响应异步字体结果。
- 菜单 Style 末尾增加可选 dark；自动默认不再通过表面亮度猜模式，旧完整 Style 调用保留。FAB 区分省略与显式材质，保留显式内容色与参数顺序。
- Demo 启动选择 M3，以局部 scope 切换原生外观并保留内容状态。PR 与快照发布同步检查、发行两个同版本 artifact，见[外观与预设](docs/guide/appearance.md)。

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

- Maven Central 的版本以 [实际包页面](https://central.sonatype.com/artifact/io.github.itoseo/coui)及远端元数据为准；[README](README.md)中的依赖示例使用双模块同版本发行。
- alpha3 的源码 Tag 与发行文件见上文；历史版本没有对应发行 Tag，不据此断言 `0.0.1-alpha1` 或 `0.0.1-alpha2` 分别对应哪个提交。
- 根目录 `VERSION_NAME=0.1.0-SNAPSHOT` 是开发/快照的基础版本，不代表 Maven Central 已公开 `0.1.0`。
- 今后每次不可变版本发布，需记录版本号、发布日期、源代码 Tag/Commit SHA、AAR 校验/发行地址、公开 API 兼容变化和迁移说明。再将上述记录归入对应版本段落。

发布渠道及执行方式见[发布指南](docs/development/publishing.md)。
