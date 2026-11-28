# 组件目录

需要直接在布局中使用控件时，可从下表按场景选择。每个组件均可单独接入，也可与 Material 3、Miuix 等现有设计体系混搭，无需替换宿主的 `Scaffold`、主题或导航框架。

- 首次接入环境与依赖配置见[快速开始](../guide/getting-started.md)。
- 第三方页面容器集成方法见[宿主页面接入](../guide/secondary-pages.md)。
- 浮动材质所需的公共背景采样见[背景采样](../effects/backdrop.md)；单独调用底层绘制算法见[效果目录](../effects/README.md)。

## 页面容器与栏位

| 组件 | 定位与核心能力 | 最低版本 / 前置依赖 |
| --- | --- | --- |
| [页面容器](scaffold.md) | 可选的 `Scaffold`，统一顶栏、底栏与正文 `PaddingValues`、安全区及自动背景源 | API 31+；无需 Material 3 |
| [导航转场](predictive-back.md) | Nav3 的 AOSP 跟手缩放与 ChatGPT 水平转场预设 | API 31+；跟手预览需要系统提供手势进度 |
| [顶栏与按钮](top-app-bar.md) | `TopAppBar` 标题与操作槽位、`TopAppBarSurface` 自定义渐进模糊背景容器、`TopAppBarButton` 浮动按钮 | 容器 API 31+；按钮需 API 34+ 与 `backdrop` |
| [折叠顶栏](collapsing-app-bar.md) | `LargeTopAppBar` 大标题弧线收起、`CollapsingTopAppBar` 联动常驻搜索折叠 | 容器 API 31+；需滚动连接与 `backdrop` |
| [顶栏搜索](search.md) | `TopAppBarSearch` 顶栏搜索按钮到胶囊输入框的融合展开转场 | API 34+；需位于顶栏作用域内 |
| [常驻搜索](search-bar.md) | `SearchBar` 常驻搜索胶囊与分裂取消按钮，支持联动页面与结果层 | API 31+（API 34+ 启用浮动模糊材质）；需 `backdrop` |
| [底栏](navigation-bar.md) | `NavigationBar` 底部胶囊导航项与左右可折叠融合附件 | API 34+；需 `backdrop`（可选独立三主体 `renderer`） |
| [FAB](fab.md) | `Fab` 独立浮动操作按钮，支持主题强调色、内置配色预设与自定义形状 | API 34+；需 `backdrop`（可选独立 `standalone` `renderer`） |
| [悬浮操作栏](floating-toolbar.md) | `FloatingToolbar` 胶囊浮动操作栏，同级操作并列、按列数收放并支持反馈蒙版 | API 34+；需 `backdrop`（可选独立 `standalone` `renderer`） |

## 列表与分组

| 组件 | 定位与核心能力 |
| --- | --- |
| [Group](group.md) | 分组卡片容器、组标题操作与说明布局 |
| [Item](item.md) | 列表行容器、文本角色与点击/选择装配 |
| [Divider](divider.md) | 分组卡片内部条目分隔线 |
| [Footer](footer.md) | 分组卡片底部辅助说明 |

## 选择与输入

| 组件 | 定位与核心能力 |
| --- | --- |
| [开关](switch.md) | 二值切换、拖动吸附与加载状态 |
| [单/复选框](checkbox.md) | 单选、复选状态与切换动画 |
| [滑动选择条](slider.md) | 连续、分段与隐藏点位的步进选择 |
| [锥光进度条](conic-glow-progress-bar.md) | 流体云歌曲进度样式，支持只读展示与播放位置跳转 |
| [ChipTab](chip-tab.md) | 单选分类标签栏、横向滚动与独立图标动作 |
| [分段选择器](segmented-control.md) | 胶囊内单选滑块、不等宽标签与拖动回弹 |
| [消息输入栏](message-input-bar.md) | `MessageInputBar` 消息编辑、操作与底部渐变遮罩 |

## 浮层、选择器与图标

| 组件 | 定位与核心能力 |
| --- | --- |
| [菜单](menu.md) | 弹出菜单、勾选、分组与一级子菜单 |
| [弹窗](dialog.md) | 底部/居中窗口、受控操作、单选、输入与加载预设 |
| [Sheet 面板](bottom-sheet.md) | 受控入退场、拖动关闭和通用内容槽 |
| [选择器](pickers.md) | 受控整数滚轮与月历（时间、日期与弹窗由宿主组合） |
| [图标](icons.md) | `coui-icons` 矢量与位图图标集，支持日夜资源与自定义染色 |
