# 二级页面接入

COUI-Kit 提供页面容器与设置项，宿主负责页面身份、返回栈、主题和业务状态。所有页面通过 CouiSettingsScaffold 自行装配顶栏和滚动内容，不为某种页面组合增加第二个公共入口。

组件目录和组件详情分别作为页面：目录用带图标的 行布局与唯一点击 owner 提交导航，具体组件放在详情页内。设置首页的大标题与分裂搜索使用[对应容器](../components/collapsing-app-bar.md)，普通详情仍使用紧凑标题与返回按钮。不要在库中注册 Demo 路由，也不要让通用组件直接启动 Activity。

## 使用当前 ROM 的默认转场

每个二级页面使用普通、不透明的 ComponentActivity，从当前 Activity 用标准 Intent 启动，返回调用 finish。打开和关闭均不提供自定义 ActivityOptions，不调用 overridePendingTransition / overrideActivityTransition，也不设置自定义 windowAnimationStyle；动画交由当前系统 WindowManager 选择。

在 AndroidManifest.xml 注册自己的页面。以下应用主题继承平台默认窗口动画，页面不启用透明/浮动窗口，不使用 NEW_TASK 或 singleTask。

```xml
<application
    android:theme="@style/Theme.MyApp"
    android:enableOnBackInvokedCallback="true">
    <activity android:name=".DetailActivity" android:exported="false" />
</application>
```

values/themes.xml：

```xml
<resources>
    <style name="Theme.MyApp" parent="android:style/Theme.Material.Light.NoActionBar" />
</resources>
```

在已有 Activity 中打开：

```kotlin
startActivity(Intent(this, DetailActivity::class.java))
```

Activity 的 setContent 中组合 CouiSettingsScaffold、CouiTopAppBar、CouiSettingsGroup 和实际控件。完整工作示例见 DemoSettingsPage（完整 Demo 在后续重构批次提供）；它是 Demo 的 internal 装配，不属于库 API。宿主可以参考这份装配，自行选择标题、返回、actions、滚动容器和反馈。

Activity 根级不需要 BackHandler：系统返回与预测性返回继续由 Activity 处理。只有页面内正在编辑、搜索、存在未保存变更等业务状态需要拦截时，再局部安装处理器；关闭该状态后释放拦截，让 Activity 返回恢复。弹窗自己的返回由弹窗处理。

ROM 可以按应用、窗口和设备形态改变默认转场。这个方案调用第三方应用能获得的当前系统默认动画；系统设置的特权白名单、OOS 特殊动画和嵌入式双栏协议没有公开等价保证。不要通过资源反射或复制某个 ROM 的固定动画 XML 来声称自动适配所有系统。

## 滚动、安全区和恢复

Scaffold 负责横向和底部安全区，顶栏负责顶部 inset；内容必须应用并消费回传的 padding。折叠顶栏通过 state.contentPadding 修正内容顶部留白，底部视口不随顶栏跟随动画平移，具体见[折叠顶栏](../components/collapsing-app-bar.md)。滚动状态、返回处理与业务值由宿主持有；不要重复添加系统 inset，也不要对同一内容套两层滚动。短页使用 fillMaxSize 和 couiVerticalScroll，保留空白区域回弹。

需要 LazyColumn 时，使用低层容器。以下片段在宿主 Composable 中，dark、colors、title、rows、rowTextStyle 和 openRow 都是宿主提供的参数；rows 的 id 稳定且唯一。

```kotlin
CouiSettingsScaffold(dark = dark, colors = colors, topBar = { backdrop ->
    CouiTopAppBar(backdrop = backdrop) {
        Box(Modifier.fillMaxWidth().statusBarsPadding().height(52.dp),
            contentAlignment = Alignment.Center) {
            BasicText(title, style = TextStyle(color = colors.title))
        }
    }
}) { innerPadding ->
    LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding),
        contentPadding = innerPadding,
    ) {
        items(rows, key = { it.id }) { row ->
            CouiSettingsGroup {
                CouiSettingsItem(onClick = { openRow(row.id) }) {
                    CouiPreferenceLayout { BasicText(row.title, style = rowTextStyle) }
                }
            }
        }
    }
}
```

此片段需要 foundation.layout 的 Box / height / fillMaxSize / fillMaxWidth / statusBarsPadding / consumeWindowInsets，foundation.lazy 的 LazyColumn / items，BasicText、TextStyle、Alignment、Modifier、dp，以及 coui.settings 和 coui.topappbar 对应公开入口的导入。低层容器先测量顶栏，再向内容传入 PaddingValues；首帧即可获得顶栏尺寸。顶栏负责自己的顶部 inset，容器处理横向安全区和祖先已消费的 inset，底部留白通过 PaddingValues 交给内容。自定义 contentWindowInsets 只影响容器负责的内容安全区，不代替顶栏自身的顶部处理。

需要浮动导航的页面可传 bottomBar 与 floatingActionButton slot，二者都收到内容 CouiBackdrop。bottomBar 拥有自己的底部系统 inset；容器把底栏完整测量高度写入内容 padding。没有底栏时改用底部安全区。FAB 放在底栏上方的逻辑末端，由 slot 自己加末端/底部间距；内容如需避让 FAB，另外追加滚动尾部留白。每个绘制节点仍独立持有 renderer。原三页 Demo 的 DemoApp（完整 Demo 在后续重构批次提供）展示了这些 slot 与 CouiBottomBar / CouiFab 的组合。

采样仅覆盖页面内容，顶栏、底栏、FAB 与 Popup / Dialog 不进入同一源。弹窗位于容器内容作用域时可直接使用 rememberCouiWindowBackdrop；不要另录含压暗遮罩的根树。参见[设置组件](../components/settings.md)、[弹窗](../components/dialog.md)与[采样契约](../effects/backdrop.md)。

## Miuix 参考与本项目分工

本实现参考 Miuix 的 [Scaffold](https://github.com/compose-miuix-ui/miuix/blob/bb7bde3109605b4c98065b53030ba429c39baea9/miuix-ui/src/commonMain/kotlin/top/yukonga/miuix/kmp/basic/Scaffold.kt)和[设置页调用](https://github.com/compose-miuix-ui/miuix/blob/bb7bde3109605b4c98065b53030ba429c39baea9/example/shared/src/commonMain/kotlin/SettingsPage.kt)的职责：容器测量顶栏，内容消费 padding，宿主维护状态和导航。COUI 的尺寸、排版、色彩和形状来自目标 Settings 的实际调用链，没有引入 Miuix 依赖。

行级职责继续参考同提交的 [BasicComponent](https://github.com/compose-miuix-ui/miuix/blob/bb7bde3109605b4c98065b53030ba429c39baea9/miuix-ui/src/commonMain/kotlin/top/yukonga/miuix/kmp/basic/Component.kt)：起始内容、文字内容、末端动作与内边距可分别替换。COUI 对应结构如下，使用者可以只取需要的一层。

| 层次 | COUI 入口 | 宿主可调整的部分 |
| --- | --- | --- |
| 窗口/页面容器 | CouiSettingsScaffold | topBar、bottomBar、FAB、content、窗口 inset；浮层与内容采样分离 |
| 常规二级页面装配 | 宿主组合 Scaffold / TopAppBar / 滚动内容 | 标题、返回、actions、ScrollState 与页面内容 |
| 自定义首页/顶栏 | CouiTopAppBar / CouiCollapsingTopAppBar | 自己装配标题/搜索，连接实际滚动与内容位移 |
| 分组/图示 | CouiSettingsGroup / CouiSettingsIllustration | 标题、headerActions、说明、间隔与任意内容 |
| 原厂行布局 | CouiPreferenceLayout | leading/headline/supporting/trailing、内边距、最小高度、图标尺寸 |
| 标准行与控件 | 宿主组合 PreferenceLayout 与唯一控件 | 受控值、文案、图标、状态与唯一业务回调 |

需要整行点击和原厂列表反馈的自定义内容，用 CouiSettingsItem 拥有交互，再在其 RowScope 中放 CouiPreferenceLayout 或自定义内容；不用再嵌套另一个 clickable。纯信息行直接使用 PreferenceLayout。行级装配示例与完整 API 见[列表族](../components/settings.md)。

顶级切页时，dock、renderer 与反馈节点必须放在稳定的 Scaffold slot 中，只把页面内容放进带页面 key 的状态作用域。把整个 Scaffold 放在该 key 内会在切页时销毁反馈；延迟业务切页不是生命周期修复。Demo 当前结构可直接参考这一点。

Miuix 示例中的 [CrossActivityTransition](https://github.com/compose-miuix-ui/miuix/blob/bb7bde3109605b4c98065b53030ba429c39baea9/example/shared/src/commonMain/kotlin/navigation/CrossActivityTransition.kt)是 Compose 内部转场。本项目要求当前 ROM 的默认 Activity 动画，所以 Demo 用真实 Activity 启动/结束。若宿主选择单 Activity 导航，仍可复用 COUI 页面容器，但需要自己决定 Compose 转场，不能称为调用 ROM 的窗口转场。

实际可运行的宿主装配见 MainActivity（完整 Demo 在后续重构批次提供）、二级 Activity（完整 Demo 在后续重构批次提供）与页面内容（完整 Demo 在后续重构批次提供）。

## 回弹和更多菜单

内容使用 [couiVerticalScroll](../effects/overscroll.md)，空页/短页仍占满内容视口，标题与 dock 不随越界位移移动。每个容器独立 remember 其 effect；null 表示关闭该能力。

三点菜单由宿主在顶栏动作槽组合 CouiTopAppBarButton、完整按钮锚点及唯一的 CouiMenu；列表菜单组合行布局与触点锚点。完整示例分别在 DemoMenuPresets（完整 Demo 在后续重构批次提供） 和 DemoPreferenceControls（完整 Demo 在后续重构批次提供）。库没有三点菜单或 Popup 行的独立包装入口。
