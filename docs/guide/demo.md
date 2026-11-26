# 运行与操作 Demo

## 运行

1. 在 Android Studio 中打开仓库根目录并完成 Gradle Sync，然后选择 `demo` 运行（SDK、Java 与依赖要求见[版本目录](../../gradle/libs.versions.toml)、[库配置](../../coui/build.gradle.kts)、[Demo 配置](../../demo/build.gradle.kts)与 [Gradle JVM 配置](../../gradle/gradle-daemon-jvm.properties)，工程使用 AGP 内置 Kotlin）。
2. 如需直接下载 CI 构建产物，见 [Demo CI 与 PR 检查](../development/ci.md)。维护者本地构建与 CI 分发的 Demo 统一使用 `itos` 签名；同 `applicationId` 覆盖安装需证书一致。
3. Demo 的 `release` 构建默认启用 R8 代码压缩、优化、混淆与资源裁剪（COUI 库由最终 App 统一优化，不单独预混淆），排查 release 崩溃时请保留对应构建的 `mapping` 文件。

## 本地签名

| 角色 / 场景 | 签名配置方式 | 注意事项 |
| --- | --- | --- |
| 维护者本地构建 | 将根目录 [signing.properties.example](../../signing.properties.example) 复制为 `signing.properties`，填全 `KEYSTORE_FILE`、`KEYSTORE_PASSWORD`、`KEYSTORE_ALIAS`、`KEYSTORE_ALIAS_PASSWORD` 四个字段（别名为 `itos`，密钥保存在仓库外并使用实际绝对路径）。 | `signing.properties` 与 keystore 已被 Git 忽略，切勿提交；`debug` 与 `release` 共用同一配置。旧 Demo 若使用其他证书需先卸载再安装。 |
| 贡献者无密钥 | 无需创建 `signing.properties`，AGP 自动使用本机默认 debug keystore。 | 仅用于本地开发验证，不作为官方 Demo 分发。 |
| CI 签名覆盖 | 通过环境变量 `KEYSTORE_FILE`、`KEYSTORE_PASSWORD`、`KEYSTORE_ALIAS`、`KEYSTORE_ALIAS_PASSWORD` 传入。 | 环境变量必须完整成组；只提供部分字段、空值或不存在的路径时会明确失败，不会混用本地配置或静默回退。库编译与 PR 不要求签名配置。 |

## 展示与操作

Demo 首页包含三个底栏页签（API 34+ 使用浮动 `NavigationBar`，左附件切换深浅色，右附件打开 `Fab` 示例；API 34 以下使用普通底部导航），主题和轮廓开关在 Activity 之间共享：

| 页面 | 内容 |
| --- | --- |
| 组件 | 开关、滑条、单/复选框、Sheet 面板、菜单、弹窗、顶栏、搜索、底栏、FAB、选择器、分段选择器、消息输入栏与 `ChipTab` |
| 效果 | 界面切换动画效果、平滑胶囊阴影、记录侧滑融合、背景模糊和材质 |
| 设置 | 主题设置、底栏设置、顶栏模糊，以及“关于此示例”和“还原示例”选项 |

### 搜索与返回

- 首页搜索唤起键盘时，底栏在屏幕底部原地收起，结果列表随键盘调整可滚动区域。键盘开始收起时，底栏即在原位回弹。
- **筛选与入口**：组件页搜索直接筛选首页组件分组，空查询显示完整目录（不显示效果或隐藏的独立类型条目）；搜索“时间”“倒计时”或“日期”时通过滚轮选择器条目进入对应类型。筛选列表使用独立滚动位置，清空查询或取消搜索后恢复原目录位置。
- **清除、取消与返回**：清空按钮仅清除查询文本；点击取消或在搜索展开时按系统返回键会关闭搜索并清除查询。点击条目仅打开目标页，返回后保留来源页的查询与展开状态（状态和路由由 Demo 持有，组件用法见[常驻搜索](../components/search-bar.md)）。

### 主要示例

- **底栏**：在「组件 → 底栏」独立切换左侧有色按钮和右侧透明按钮的显隐，观察融合收放。首页用左透明、右有色的组合。
- **界面切换动画效果**：在「效果」页的菜单中选择 AOSP 或 ChatGPT，打开任一效果详情后即可侧滑返回。

所有二级页统一使用 `LargeTopAppBar` 大标题顶栏：展开标题位于返回按钮下方，上滑时沿弧线收起至按钮旁，返回按钮与操作区位于标题上层（见[二级页大标题与弧线收起](customizations.md#二级页大标题与弧线收起)）。

- **Sheet 面板**：在**组件 → Sheet 面板**可分别试用「添加闹钟」与「半屏面板」；闹钟示例展示全高编辑、草稿取消/保存与内嵌 `ChipTabRow`，半屏示例展示拖动条与右上角「更多」菜单，页面顶部的模糊开关可对照下方三色色块预览采样效果（用法见 [Sheet 面板](../components/bottom-sheet.md)）。
- **分段选择器**：依次展示多色背景上的透明模糊、默认 `Group` 内的普通填色和页面底色上的透明模糊；各组显示已提交选择，支持切换紧凑尺寸、禁用态和抬手提交（`commitOnRelease`），横向滑选期间页面保持静止。
- **开关**：展示点击、拖动吸附、加载中（`loading`）、独立开关与禁用状态。
- **单/复选框**：展示互斥单选、禁用单选，以及独立、整行和禁用复选框。
- **菜单**：分别打开按钮定位、触点定位、勾选与分隔线、二级菜单、带标题菜单、可滚动长菜单和自定义外观示例。页首的「菜单背景模糊」可切换页面示例的采样材质和纯色背景。设置行使用上下双箭头，见[菜单选择行的图标规范](../components/menu.md#菜单选择行的图标)。
- **弹窗**在 Android 13 及以上默认模糊窗口背景，轮廓光跟随设置中的「轮廓光」。在弹窗示例页关闭「弹窗背景材质」可对照纯色效果。示例包含底部、居中、长文、三操作、上方通栏加下方两个按钮、禁用确认、主题色确认及打开时切换主题。单选弹窗模拟「USB 用于」，点选后关闭，再次打开时显示已选项。输入型示例演示草稿、过滤、计数、清除与取消。加载示例在模拟任务结束后自动关闭。
- **选择器**：滚轮选择器通过类型菜单切换数字、时间、倒计时和日期，共用示例区域与弹窗入口并各自保留值和设置；月历独立展示。弹窗确认后才提交草稿，搜索具体类型可直接直达对应示例。
- **设置**：主题组提供 Material 配色开关，底栏组提供轮廓光与左右附件开关。Android 13 及以上，Demo 各页面的顶栏默认使用 Compose 渐进模糊。在「顶栏模糊」关闭开关可切换到 COUI。点击“还原示例”并确认后恢复上述设置的默认值。“关于此示例”展示当前 Demo 的 COUI 版本和 ColorOS 样式参考版本。Hero 卡片也显示样式参考版本。
- **搜索**：常驻 `SearchBar` 与顶栏搜索上下排列并共用模拟列表；使用其中一种时禁用另一种，顶栏搜索通过右侧融合分离的“取消”按钮关闭。
- **消息输入栏**：输入栏固定在底部，自带渐变遮罩、预设留白和底部导航栏占位，由宿主页处理键盘避让。示例展示文本提交以及相机、加号和发送按钮的显隐转场。三个开关独立保存按钮启用设置，关闭后持续隐藏，重新开启后按草稿状态显示。“最近操作”卡片显示相机和附件按钮的回调结果，以及已提交的消息。
- **模糊**：展示原图对照与参数调节；顶栏、底栏、搜索与 `ChipTab` 在各自展示区域中交互。
- **FAB**：可切换圆形、圆角方形与配色预设；按钮固定在右下角，滚动正文时保持原位。
- 在**悬浮操作栏**中打开「选择显示的图标」，勾选需要的操作后点「确定」，可以看到多个图标同时进出。切换「显示工具栏」可单独查看整栏的缩放显隐。
- **融合**：使用本地记录样例，支持侧滑展开删除区、二次点击确认或继续长拉松手删除（图标淡出后收缩整行）；点击记录打开预览并更新最近查看标记，全部删除后可恢复。

更多效果入口见[效果目录](../effects/README.md)。

## 主题与页面装配

- **主题切换**：`DemoApplication` 初始化 `CouiMaterial3Style`。开启「设置 → 使用 Material 主题配色」后，控件（开关、滑条、单/复选框、弹窗主操作与首页右下角按钮）跟随宿主 Material 主题；关闭后通过局部 `CouiTheme` 切换为原生外观。`FAB` 示例页中的固定预设优先于主题，详见[外观与预设](appearance.md)。
- **页面容器与背景源**：首页和详情页使用公开的 [Scaffold](../components/scaffold.md) 测量栏位并处理横向安全区，正文应用测量所得 `Padding`。首页由正文单独避让键盘，需要抬升底部输入栏的详情页由外层容器避让。容器自动向顶栏、底栏和 `FAB` 槽位提供正文背景源，正文内的 `ChipTab` 与模糊演示保留独立源；Activity 根部安装 `ProvideCouiMenuBackdrop` 窗口源供菜单、弹窗与 Sheet 面板使用（见[宿主页面接入](secondary-pages.md)）。
- **导航边界**：效果目录通过 [Nav3 页面转场](../components/predictive-back.md) 打开效果详情。其余目录入口通过标准 `Intent` 启动、`finish()` 退出。

## 源码导航

| 职责 | 文件 |
| --- | --- |
| 首页、搜索与底栏 | [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt) |
| 目录与查询 | [DemoCatalog](../../demo/src/main/java/com/itos/coui/demo/DemoCatalog.kt) |
| Activity 导航 | [DemoSecondaryActivity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt) |
| 详情分发与页面容器 | [DemoSecondaryPage](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)、[DemoSettingsPage](../../demo/src/main/java/com/itos/coui/demo/DemoSettingsPage.kt) |
| 组件与设置样例 | [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt) |
| 阴影、模糊与材质 | [DemoShadowSamples](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)、[DemoBlurTutorial](../../demo/src/main/java/com/itos/coui/demo/DemoBlurTutorial.kt)、[DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt) |
| 菜单、弹窗、Sheet 与选择器 | [DemoPopupSamples](../../demo/src/main/java/com/itos/coui/demo/DemoPopupSamples.kt)、[DemoDialogSamples](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)、[DemoSheetSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)、[DemoPickerSamples](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt) |
| 记录侧滑 | [DemoRecordSamples](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt) |

Demo 仅展示宿主装配方式，其中的行包装与路由属于示例实现，并非库公开 API。实际接入请从[组件目录](../components/README.md)或[效果目录](../effects/README.md)开始。
