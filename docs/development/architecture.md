# 架构与职责

## 模块边界

~~~text
宿主 App / demo
  └─ coui
      └─ Android / Compose / AndroidX
~~~

coui 持有通用 COUI 组件、固定预设、效果、背景采样、几何、输入与动效。宿主提供主题、Shape、受控状态、导航、错误展示和业务回调。是否使用 COUI 的应用偏好，以及宿主自己的样式适配组件，属于宿主。

demo 只消费公开 API，负责页面、主题和示例交互；示例不读取或删除真实记录。不能为演示公开内部状态，也不能让库引用 demo、外部业务模块、宿主日志框架或第三方 Backdrop。

## 库内职责

| 包                                                                   | 职责                        |
|---------------------------------------------------------------------|---------------------------|
| topappbar、navigation、floatingactionbutton、menu、slider、checkbox、switch、chip、dialog | 组件的布局、状态装配、语义与公开 Defaults |
| backdrop                                                            | 所属窗口的内容源、绘制录制与采样坐标映射      |
| effect                                                              | 普通投影、模糊、材质、光效与融合绘制        |
| shape                                                               | 公共 Shape 与几何约定            |
| motion、input、swipereveal                                            | 动画、触摸观察与公开运动预设            |
| diagnostic                                                          | 按需启停的开发诊断                 |

这些是现有 coui 模块内的职责划分。新入口放到实际负责的组件族；共享算法按职责复用，不按导入或文件名搬迁。不要新增只转发参数的包装层。

## 状态与资源

宿主持有查询、选择、展开目标与业务提交。组件持有局部交互和动画；输入观察器、点击和父级滚动分别处理自己的会话。

背景源由所属窗口持有，独立绘制节点各持有自己的可变 renderer。录制层、动画和监听随节点生命周期管理，不建立全局历史缓存。源与消费者的关系见[背景采样](../effects/backdrop.md)。

双主体融合只提供算法与跟随回弹。侧滑容器、删除确认和业务提交属于宿主，Demo 的记录侧滑不是库接口。基础阴影同时保留便捷 Modifier 与底层 Canvas 入口。

## Demo 契约

展示范围见 [Demo 指南](../guide/demo.md)。Demo 独立维护 M3 语义、页面与卡片层级和 G2 形状；设置开关、单选与复选分别使用相应语义，整行交互时子控件不重复点击。数值滑条使用 CouiSlider。

Material 导航在横屏使用侧栏，COUI 保持底栏。切页和搜索筛选保留示例状态。没有当前任务要求时，不恢复已经取消的独立效果分组。

## 依据

参数以代码与构建配置为准，模块只读入口见[库目录](../../coui/README.md)。原厂行为、公开平台适配与项目主动特化分别记录；完整使用要求由对应[组件](../components/README.md)和[效果](../effects/README.md)页面持有。

历史取舍见[模块边界](../../.agents/notes/implemented/architecture/2026-10-21-coui-module-boundary.md)和[独立工程](../../.agents/notes/implemented/architecture/2026-10-23-standalone-coui-demo.md)。
