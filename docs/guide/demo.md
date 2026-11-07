# 运行与操作 Demo

## 运行

在 Android Studio 中打开仓库根目录，完成 Gradle Sync，再选择 `demo` 运行。SDK、Java 和依赖要求以 [版本目录](../../gradle/libs.versions.toml)、[库配置](../../coui/build.gradle.kts)、[Demo 配置](../../demo/build.gradle.kts) 和 [Gradle JVM 配置](../../gradle/gradle-daemon-jvm.properties) 为准。工程使用 AGP 内置 Kotlin。

需要下载 CI 构建时，见 [Demo CI 与 PR 检查](../development/ci.md)。维护者本地构建和 CI 分发的 Demo 统一使用 `itos` 签名；同一 applicationId 的覆盖安装要求证书一致。

Demo 的 release 启用 R8 代码压缩、优化、混淆及资源裁剪，使用 AGP 默认优化规则和依赖自带的 consumer rules；debug 保留默认调试配置。COUI 库由最终 App 统一优化，不单独预混淆。定位 release 崩溃时保留对应构建的 mapping 文件。

## 本地签名

维护者将根目录 [signing.properties.example](../../signing.properties.example) 复制为 signing.properties，填写已有 `itos` 密钥的路径及密码，别名为 `itos`。密钥保存在仓库外，KEYSTORE_FILE 使用实际绝对路径。四个字段必须完整，debug 与 release 使用同一配置。

signing.properties 与 keystore 被 Git 忽略，不加入提交。旧 Demo 若使用其他证书，需要先卸载再安装；同为 `itos` 签名的版本可按 Android 的版本要求覆盖安装。

没有维护者密钥的贡献者无需配置此文件，AGP 使用本机默认 debug keystore；这类构建用于开发验证，不作为官方 Demo 分发。

CI 可通过 KEYSTORE_FILE、KEYSTORE_PASSWORD、KEYSTORE_ALIAS、KEYSTORE_ALIAS_PASSWORD 环境字段覆盖本地文件。环境字段按完整一组优先；只提供部分字段、空值或不存在的密钥路径时明确失败，不混用本地配置或静默退回其他证书。库编译与 PR 不要求提供固定签名。

## 展示与操作

首页有组件、效果、设置三页。组件与效果页显示分组目录，点击条目进入详情。组件页可搜索；效果和设置页上滑时，大标题缩成固定的小标题。

| 页面 | 内容 |
| --- | --- |
| 组件 | 开关、滑条、单/复选框、菜单、弹窗、顶栏、搜索、底栏、FAB、选择器和 ChipTab |
| 效果 | 平滑胶囊阴影、记录侧滑融合、背景模糊和材质 |
| 设置 | 主题设置、底栏设置，以及同一无标题组中的关于此示例和还原示例选项 |

底栏切换页面。左附件切换深浅色，右附件打开 FAB 示例；API 34 以下使用普通底部导航。主题和轮廓选项在 Activity 之间共享。

### 搜索与返回

组件页搜索直接筛选首页原有的组件分组，空查询显示完整组件目录，不显示效果或隐藏的独立类型条目。搜索时间、倒计时或日期时，通过滚轮选择器条目进入对应类型。筛选使用独立滚动位置，清空查询或取消搜索后恢复原目录的位置。

清空按钮只清除查询；取消或在搜索展开时按返回键会关闭搜索并清除查询。点击条目只打开目标页面，返回后保留来源页的查询与展开状态。搜索状态和路由由 Demo 持有，组件用法见[常驻搜索](../components/search-bar.md)。

### 主要示例

二级页统一使用 Large 大标题顶栏：展开标题位于返回按钮下方，上滑时沿弧线缩至按钮旁，返回按钮和操作区位于标题上层。此行为属于[项目定制](customizations.md#二级页大标题与弧线收起)。

- Sheet 页提供新建闹钟与倒计时两个草稿示例，持续装配窗口以完成退场；保存只更新本 Demo。

- 开关页展示点击、拖动、加载、独立开关和禁用状态。
- 单/复选框页展示互斥单选、禁用单选，以及独立、整行和禁用复选框。
- 菜单页展示列表项入口、触点锚定、上下箭头行、勾选、子菜单和限高滚动；勾选项与普通操作用分隔线分组。
- 弹窗页展示底部、居中、长文、三操作、禁用确认、主题色确认及打开时切换主题。输入型示例用于观察草稿、过滤、计数、清除和取消。
- 滚轮选择器页通过类型菜单切换数字、时间、倒计时和日期，共用示例区域与弹窗入口，各类型保留自己的值和设置；月历独立展示。弹窗确认后才提交草稿，搜索具体类型可直接进入对应示例。
- 设置页的主题组提供 Material 配色开关；底栏组提供轮廓光与左右附件开关。还原前需要确认，确认后恢复这四项默认值。关于页显示应用名称与参考系统版本。
- 搜索页将常驻搜索与顶栏搜索上下排列，共用模拟列表。使用一种搜索时禁用另一种；顶栏搜索通过右侧融合分离的“取消”按钮关闭。
- 模糊页展示原图对照和参数调节。顶栏、底栏、搜索与 ChipTab 在各自展示区域中操作。
- FAB 页可切换圆形、圆角方形及配色预设；按钮固定在页面右下角，滚动正文时保持原位。
- 融合页使用本地记录样例：可侧滑展开删除区、二次点击确认，或继续长拉后松手删除。图标淡出后收缩整行；点击记录打开预览并更新最近查看标记，全部删除后可恢复。

更多效果入口见[效果目录](../effects/README.md)。

## 主题与页面装配

`DemoApplication` 初始化可选 `CouiMaterial3Style`。开启 Material 配色时，组件读取宿主主题；关闭时，局部 `CouiTheme` 使用原生外观，Demo 的 M3 内容也映射到 COUI 表面与强调色。显式 FAB 配色预设优先于主题。主题接入见[外观与预设](appearance.md)。

首页和详情页使用公开的 [Scaffold](../components/scaffold.md)。容器测量栏位并处理横向安全区，Demo 在容器外处理 IME；正文应用测量所得 Padding。容器自动向顶栏、底栏和 FAB 槽位提供正文背景源，正文内的 ChipTab 和模糊演示保留独立源。Activity 根部的窗口源供菜单与弹窗使用，不由 Scaffold 安装。源和消费者的分层见[宿主页面接入](secondary-pages.md)。

页面通过标准 Intent 启动、`finish()` 退出，使用当前 ROM 向普通应用提供的 Activity 转场。Demo 不实现系统设置的特权窗口或双栏协议。

## 源码导航

| 职责 | 文件 |
| --- | --- |
| 首页、搜索与底栏 | [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt) |
| 目录与查询 | [DemoCatalog](../../demo/src/main/java/com/itos/coui/demo/DemoCatalog.kt) |
| Activity 导航 | [DemoSecondaryActivity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt) |
| 详情分发与页面容器 | [DemoSecondaryPage](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)、[DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt) |
| 组件与设置样例 | [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt) |
| 阴影、模糊与材质 | [DemoShadowSamples](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)、[DemoBlurTutorial](../../demo/src/main/java/com/itos/coui/demo/DemoBlurTutorial.kt)、[DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt) |
| 弹窗与选择器 | [DemoDialogSamples](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)、[DemoPickerSamples](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt) |
| 记录侧滑 | [DemoRecordSamples](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt) |

Demo 展示宿主装配，内部行包装和路由不是库 API。接入从[组件目录](../components/README.md)或[效果目录](../effects/README.md)开始。
