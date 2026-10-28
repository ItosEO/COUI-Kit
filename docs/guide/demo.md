# 运行与操作 Demo

## 运行

在 Android Studio 中打开仓库根目录，完成 Gradle Sync，再选择 `demo` 运行。SDK、Java 和依赖要求以 [版本目录](../../gradle/libs.versions.toml)、[库配置](../../coui/build.gradle.kts)、[Demo 配置](../../demo/build.gradle.kts) 和 [Gradle JVM 配置](../../gradle/gradle-daemon-jvm.properties) 为准。工程使用 AGP 内置 Kotlin，不再应用 `org.jetbrains.kotlin.android`。

需要下载 CI 构建时，见 [Demo CI 与 PR 检查](../development/ci.md)。本地与 CI 可以共用独立的 Demo 测试密钥；同一 applicationId 的覆盖安装要求证书一致。

Demo 的 release 启用 R8 代码压缩、优化、混淆及资源裁剪，使用 AGP 默认优化规则和依赖自带的 consumer rules；debug 保留默认调试配置。COUI 库由最终 App 统一优化，不单独预混淆。定位 release 崩溃时保留对应构建的 mapping 文件。

## 本地签名

首次配置时，将根目录 [signing.properties.example](../../signing.properties.example) 复制为 signing.properties，填写已有 Demo 密钥。KEYSTORE_FILE 相对于仓库根目录，也可填写绝对路径。四个字段必须完整，debug 与 release 使用同一配置；未配置文件和环境字段时，AGP 自动使用或创建本机默认 debug keystore。

没有现成密钥时，可以使用 JDK 的 keytool 创建独立测试密钥，再将实际密码填入配置：

~~~sh
keytool -genkeypair -keystore demo-test.jks -storetype PKCS12 \
  -alias coui-demo -keyalg RSA -keysize 3072 -validity 10000
~~~

请备份固定测试密钥。signing.properties 与 keystore 被 Git 忽略，不加入提交；模板中的测试密码只用于示例，不用于生产 App。首次从默认测试证书切换到固定证书时，旧 Demo 需要先卸载一次。

CI 可通过 KEYSTORE_FILE、KEYSTORE_PASSWORD、KEYSTORE_ALIAS、KEYSTORE_ALIAS_PASSWORD 环境字段覆盖本地文件。环境字段按完整一组优先；只提供部分字段、空值或不存在的密钥路径时明确失败，不混用本地配置或静默退回其他证书。库编译与 PR 不要求提供固定签名。

## 展示与操作

Demo 包含三个页面：

| 页面 | 示例                                                                                                  |
|----|-----------------------------------------------------------------------------------------------------|
| 组件 | 连续滑条、可见分段、隐式步进与禁用态；复选框各状态；带图标、选中项、独立勾选、分隔线、禁用项及子菜单的菜单；长菜单与滚动条；ChipTab 的单选、分类菜单、可选图标按钮与纯色背景、禁用态及横向滚动 |
| 效果 | 胶囊阴影及便捷 / 底层入口；双主体融合、内容遮罩和跟随回弹；背景采样与模糊；材质混色                                                         |
| 预设 | 全部 FAB 固定配色、轮廓光、左右附件独立收放及顶栏按钮禁用态                                                                    |

组件页还展示底部和居中弹窗、操作按钮、输入预设及背景材质，详见[弹窗示例](../components/dialog.md#demo-中的样式与示例)。

顶栏演示渐进模糊、图标按钮、文字按钮和搜索，搜索时同时展示退场动作与常驻动作的布局。搜索仅匹配当前页的分组标题。底栏切换示例页；独立 FAB 与右侧附件打开外观预设页；顶栏“重置”恢复默认 FAB 配色。深浅色通过顶栏或左侧附件切换。全部数值滑条使用 CouiSlider，切页和筛选保留受控值。融合示例使用三条独立的示例记录，展示侧滑、跟随回弹、内容遮罩、删除确认和离场动画；删除只改变 Demo 内存状态，清空后可恢复。侧滑装配只在 Demo，库继续提供算法。滑条占满分组的可用宽度；长菜单限制高度并保留四组三项。

Demo 使用系统动态色。M3 导航在竖屏使用底栏、横屏使用侧栏；COUI 使用底栏。

不支持相应效果的平台会显示版本要求；浮动渲染器创建失败会显示失败信息。

## 源码导航

- [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)：页面、采样源、顶栏、搜索、底栏和 FAB。
- [DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)：滑条、复选框与菜单。
- [DemoDialogSamples](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)：弹窗、材质开关与宿主受控回调。
- [DemoDialogInputSample](../../demo/src/main/java/com/itos/coui/demo/DemoDialogInputSample.kt)：输入型预设、重命名草稿与宿主校验策略。
- [DemoChipSamples](../../demo/src/main/java/com/itos/coui/demo/DemoChipSamples.kt)：ChipTab 与分类菜单。
- [DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)：采样模糊与材质混色。
- [DemoShadowSamples](../../demo/src/main/java/com/itos/coui/demo/DemoShadowSamples.kt)：便捷和底层阴影。
- [DemoRecordSamples](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt)：侧滑驱动融合、确认和示例删除。

重命名示例用 TextFieldState 和 InputTransformation 保存真实编辑范围：整段粘贴包含禁用字符时，将本次插入部分置空；长度超限只保留配额内的插入内容。过滤和计数仍归 Demo，清除与取消不提交名称。
