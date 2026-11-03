# 运行与操作 Demo

## 运行

在 Android Studio 中打开仓库根目录，完成 Gradle Sync，再选择 `demo` 运行。SDK、Java 和依赖要求以 [版本目录](../../gradle/libs.versions.toml)、[库配置](../../coui/build.gradle.kts)、[Demo 配置](../../demo/build.gradle.kts) 和 [Gradle JVM 配置](../../gradle/gradle-daemon-jvm.properties) 为准。工程使用 AGP 内置 Kotlin。

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

启动保留组件、效果、预设三页及 dock、左右附件。首页独立 FAB 和点击次数统计已移除，FAB 本身在组件详情继续展示。dock 的组件与效果项使用 ColorOS 图标的普通/实心状态对，预设以主题色表达选择，切页不重建其反馈节点。组件页采用设置式图标与文字分组目录，点击进入具体组件的完整 Activity；效果页同样逐项进入，不将所有样例铺在首页。

首页标题采用 Settings 实际大标题和滚动缩放/淡出，常驻搜索随 AppBar 折叠后停靠顶部。点击搜索后输入背景收窄并分裂出取消胶囊；查询只筛选可导航目录。取消/返回关闭搜索，清空仅删除查询，不关闭输入；打开搜索结果保留来源行和查询，返回时恢复搜索；没有麦克风及其占位，也没有原顶栏重置、主题和搜索图标按钮。主题仍可通过原 dock 附件和预设页切换。

搜索活动和退场动画期间，原目录与预设保留布局和页面状态，但停止点击、拖动及键盘焦点，并退出无障碍遍历；搜索结果保持独立操作。

首页和二级页的 Scaffold 在滚动容器外消费 IME，使键盘打开时实际视口缩小；系统栏和顶栏 padding 继续留在滚动内容侧，保留浮动材质所需的正文采样。

- 独立组件页面覆盖开关、滑条、复选、菜单、弹窗、顶栏、搜索、底栏和 FAB。每页使用正常二级标题、浮动返回按钮及对应完整交互；不再进入另一份三页 gallery。
- 弹窗保留底部、居中、长文、三操作、禁用确认、主题色确认和打开时切换主题的例子，并增加“输入型弹窗：重命名设备”，可对照真实选区粘贴过滤、计数、清除和取消草稿。项目材质及平滑形状修复保留。
- 设置页包含主题、显示预览、依赖开关、单选子页、隐私与还原确认。主题及轮廓选项在 Activity 之间共享，滚动与局部输入各自恢复。
- 预设页保留原 dock 附件、轮廓与 FAB 配色，不保留已经移除的首页顶栏按钮开关。底栏布局保留，配色作用于右侧附件；首页不再额外悬浮加号。

Demo 在 DemoApplication 调用 `Coui.initialize(CouiMaterial3Style())`。默认值在组件消费处解析。关闭“使用 Material 主题配色”时，局部 `CouiTheme` 使用原生外观，Demo 的 M3 内容与背景也使用 COUI 中性表面和蓝色强调色；菜单与底栏不额外叠加页面染色。显式 FAB 配色预设继续生效。

首页和详情页使用 Demo 自己的 Material 3 Scaffold，正文采样源供顶栏和底栏消费，Activity 根部提供菜单与弹窗的窗口采样源。分组和行布局直接使用 `com.itos.coui.layout` 的公开组件，安全区和返回处理由 Demo 管理。

首页只使用彩色图标目录和分组卡片，不增加分类小标题；详情都有用途介绍，按控件类型使用对应系统式行列与说明。菜单页集中展示上下箭头 Popup 行；开关页包含模拟等待的加载状态；滑条轨道在独立槽位居中。时间、倒计时、日期与月历分别提供页面组件和标准弹窗。模糊页采用设置式图示、说明、调节的顺序。

视觉与行为核对 Settings 首页和普通二级页的实际消费者；Miuix 仅保留宿主职责参考。原生标题、搜索与列表的参数和状态分别追踪，不用固定小标题或便签式搜索替代。本地未进行设备像素、动态输入或性能验收。

本轮按最新主线整理，保留主题、构建、发行和 CI 配置。Sheet 与独立普通按钮不在本次展示范围。构建成功不等于 GPU、像素、触摸或性能验收。

## 源码导航

[DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)持有顶级导航与 dock，[目录](../../demo/src/main/java/com/itos/coui/demo/DemoCatalog.kt)登记实际导航项，[具体样例](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)装配对应控件。[二级 Activity](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryActivity.kt)负责平台导航，[二级页面](../../demo/src/main/java/com/itos/coui/demo/DemoSecondaryPage.kt)消费公开组件。[弹窗示例](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)包含主题切换复查入口。库使用者可直接参照[二级页接入](secondary-pages.md)。

宿主接入从[组件](../components/README.md)与[效果](../effects/README.md)开始。

分类栏使用独立演示，不嵌入首页。菜单页集中展示同一个菜单的右上角入口与列表项两种预设；原菜单示例采用设置行排布。效果页按系统说明/设置行排布。顶栏、底栏、搜索和 FAB 的展示区域占满平滑圆角卡片。页面及短内容的边界反馈使用 COUI 回弹。

效果目录与上游保持四类：平滑胶囊阴影、记录侧滑融合、背景模糊与材质。轮廓、跟手光、形状、运动及反馈不再单独占 Demo 入口，库能力和组件内反馈保留。分类栏示意区提供投影留白后才裁圆角，不把阴影裁在单行高度内。

融合页参照 Metric 帧率记录列表的默认外观与交互：图标独立于滑动卡片，卡片展示时间、FPS、功耗、时长与备注；删除包含桶盖动画、图标淡出和整行收缩。点击记录可查看本地样例并更新最近查看标记，全部删除后可恢复。

选择器四页只展示纯 Compose 控件及标准弹窗。月历保留一张页面组件，原有选值、日期范围、三列日期编辑和翻页继续使用同一实现。
