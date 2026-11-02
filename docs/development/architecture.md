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

## 可独立取用与混搭

COUI-Kit 是组件与风格能力库，不是要求整套替换宿主页面的框架。调用方可以只使用一个 COUI 控件，或把 COUI 与 Material、Miuix、自有设计放在同一页面。参考其他库时吸收具体经验，不以复制其组件目录、页面框架和主题层为目标。

- 页面 Scaffold、导航、返回栈、安全区分工和业务状态由宿主选择；库不另设 Scaffold 或只改名字的页面包装器。
- 组件通过现有 CouiTheme 与显式参数解析外观，不以新页面 Provider 作为使用前提。除宿主主动选择的主题/适配契约外，不隐式改变内容槽中其他设计组件的配色或状态。
- 普通分组、行布局、文字和图示不创建背景采样层。模糊材质使用现有 Backdrop 显式接入，窗口源按需提供，不与页面测量绑定。
- 有明确几何或交互协作的局部组件可以共享所需作用域，例如顶栏按钮的背景与搜索转场；这种局部契约不扩大为全页或全应用依赖。

这一边界不要求删掉有实际职责的局部布局，也不允许用近似效果替换已经批准的原厂算法。当前决定与取舍见[宿主页面所有权](../../.agents/notes/implemented/architecture/2026-11-03-host-owned-page-layout.md)。

## 库内职责

| 包 | 职责 |
| --- | --- |
| layout、topappbar、search、navigation、floatingactionbutton、menu、slider、checkbox、switch、chip、dialog、picker、text | 局部组件的布局、状态装配、语义与公开 Defaults |
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

Material 导航在横屏使用侧栏，COUI 使用底栏。切页和搜索筛选保留示例状态。

## 依据

参数以[库源码](../../coui/src/main/java/com/itos/coui/)与[构建配置](../../coui/build.gradle.kts)为准。原厂行为、公开平台适配与项目主动特化分别记录；完整使用要求由对应[组件](../components/README.md)和[效果](../effects/README.md)页面持有。

历史取舍见[模块边界](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)和[独立工程](../../.agents/notes/implemented/architecture/2026-10-23-standalone-coui-demo.md)。主题与染色的当前使用契约见[外观与预设](../guide/appearance.md)。
