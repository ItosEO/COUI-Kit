# 架构与职责

## 模块边界

~~~text
宿主 App / demo
  ├─ coui-material3（可选）→ coui + Material 3
  └─ coui → Android / Compose / AndroidX
~~~

coui 持有通用 COUI 组件、原生主题与固定预设、效果、背景采样、几何、输入与动效。宿主提供受控状态、导航、错误展示和业务回调，并可覆盖配色、字体、Shape 与 Style。应用偏好与动态色创建仍归宿主；核心不读取应用设置或 MaterialTheme。

可选 coui-material3 只承担宿主 M3 默认值解析、内容 Local 桥接与 M3 最小交互尺寸。Application 通过 Coui.initialize 固定策略；适配器在组件消费处读取 MaterialTheme，不缓存根色板、不持有 Context、不接管业务状态。核心的 CouiStyle 只使用核心和基础 Compose 类型。局部 CouiTheme 提供 native/custom 外观，同时保留已选适配器的插槽桥接。模式与覆盖顺序见[外观指南](../guide/appearance.md)。

demo 只消费公开 API，负责页面、主题和示例交互；示例不读取或删除真实记录。不能为演示公开内部状态，也不能让库引用 demo、外部业务模块、宿主日志框架或第三方 Backdrop。

## 页面与独立组件

宿主选择页面容器。可选 CouiScaffold 负责栏位测量、正文 Padding、安全区和默认背景，其他组件不依赖它。导航、返回栈、业务状态、滚动与采样由宿主组合；核心不增加专用设置页或页面管理层。

Group、Item、Divider、Footer 处理局部分组和行布局。Item.onClick 为空时只排列内容；选择、开关或独立尾部操作由宿主提供。普通布局和文字不创建背景源。默认外观读取现有 CouiTheme，显式配色只作用于声明的组件区域，不隐式改写任意内容槽。

局部几何与交互可共享必要作用域，例如顶栏按钮的渐进背景和搜索转场；这种关系不扩大为全页依赖。当前取舍见[可选轻量页面容器](../../.agents/notes/implemented/architecture/2026-11-03-optional-coui-scaffold.md)和[宿主页面所有权](../../.agents/notes/implemented/architecture/2026-11-03-host-owned-page-layout.md)。

## 库内职责

| 包 | 职责 |
| --- | --- |
| layout | 可选页面容器、分组与行布局，不持有业务状态或背景源 |
| topappbar、search、navigation、floatingactionbutton、menu、slider、checkbox、switch、chip、dialog、picker、text | 局部组件的布局、状态装配、语义与公开 Defaults |
| backdrop | 所属窗口的内容源、绘制录制与采样坐标映射 |
| effect | 普通投影、模糊、材质、光效与融合绘制 |
| shape | 公共 Shape 与几何约定 |
| motion、input、scroll、swipereveal | 动画、触摸、滚动物理与公开运动预设 |
| diagnostic | 按需启停的开发诊断 |
| theme | 可选外观上下文、原生配色、普通字体与内容局部样式 |

这些是现有 coui 模块内的职责划分。新入口放到实际负责的组件族；共享算法按职责复用，不按导入或文件名搬迁。不要新增只转发参数的包装层。

## 状态与资源

宿主持有查询、选择、展开目标与业务提交。组件持有局部交互和动画；输入观察器、点击和父级滚动分别处理自己的会话。

背景源由所属窗口持有，独立绘制节点各持有自己的可变 renderer。录制层、动画和监听随节点生命周期管理，不建立全局历史缓存。源与消费者的关系见[背景采样](../effects/backdrop.md)。

双主体融合只提供算法与跟随回弹。侧滑容器、删除确认和业务提交属于宿主，Demo 的记录侧滑不是库接口。基础阴影同时保留便捷 Modifier 与底层 Canvas 入口。

## Demo 契约

展示范围见 [Demo 指南](../guide/demo.md)。Demo 独立维护 M3 语义、页面与卡片层级和 G2 形状；设置开关、单选与复选分别使用相应语义，整行交互时子控件不重复点击。数值滑条使用 CouiSlider。

首页使用底部导航，API 34 以下使用普通底栏。目录搜索负责筛选可导航条目，详情由独立 Activity 展示；切页和搜索期间保留相应页面状态。

## 依据

参数以[库源码](../../coui/src/main/java/com/itos/coui/)与[构建配置](../../coui/build.gradle.kts)为准。原厂行为、公开平台适配与项目主动特化分别记录；完整使用要求由对应[组件](../components/README.md)和[效果](../effects/README.md)页面持有。

历史取舍见[模块边界](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)和[独立工程](../../.agents/notes/implemented/architecture/2026-10-23-standalone-coui-demo.md)。主题与染色的当前使用契约见[外观与预设](../guide/appearance.md)。
