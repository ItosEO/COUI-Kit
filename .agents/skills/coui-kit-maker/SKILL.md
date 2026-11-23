---
name: coui-kit-maker
description: 在 Android Jetpack Compose 应用中使用 COUI-Kit 组件、背景效果和 Material 3 主题适配时使用。
---

# COUI-Kit

按要使用的组件、容器或效果打开对应的 Reference。默认采用 Demo 在同一位置的外观：弹窗有窗口背景源时按系统版本启用材质，卡片内的分段选择沿用填色。应用需要兼容较低版本时，保留用户原本能执行的操作。

## 组件

- Menu 菜单　[components/menu.md](references/components/menu.md)
- Dialog / InputDialog / LoadingDialog 弹窗　[components/dialog.md](references/components/dialog.md)
- Switch 开关　[异步切换与内置加载态](references/components/switch.md#异步切换) · [整行点击与拖动](references/components/switch.md#item-中的整行开关)
- Checkbox / RadioButton 复选与单选　[components/checkbox.md](references/components/checkbox.md)
- Slider 滑条　[components/slider.md](references/components/slider.md)
- ChipTab 分类栏　[components/chip-tab.md](references/components/chip-tab.md)
- SegmentedControl 分段选择　[components/segmented-control.md](references/components/segmented-control.md)
- WheelPicker / CalendarPicker 选择器　[components/pickers.md](references/components/pickers.md)
- TopAppBar / TopAppBarButton / TopAppBarSurface 顶栏　[components/top-app-bar.md](references/components/top-app-bar.md)
- LargeTopAppBar / CollapsingTopAppBar 折叠顶栏　[components/top-app-bar.md · 折叠顶栏](references/components/top-app-bar.md#折叠顶栏)
- NavigationBar 底栏　[components/navigation-bar.md](references/components/navigation-bar.md)
- Fab 浮动按钮　[components/fab.md](references/components/fab.md)
- TopAppBarSearch 顶栏搜索 / SearchBar 常驻搜索　[components/search.md](references/components/search.md)
- MessageInputBar 消息输入栏　[components/message-input-bar.md](references/components/message-input-bar.md)

## 容器

- Scaffold 页面容器　[containers/scaffold.md](references/containers/scaffold.md)
- Group / Item / Divider / Footer 分组与列表行　[containers/group.md](references/containers/group.md)
- BottomSheet 底部面板　[containers/bottom-sheet.md](references/containers/bottom-sheet.md)

## 效果

- CouiBackdrop 背景采样　[effects/backdrop.md](references/effects/backdrop.md)
- 背景模糊、材质混色、阴影、浮动表面与轮廓光　[effects/surfaces.md](references/effects/surfaces.md)
- 点击反馈、纵向回弹、横向融合与跟随运动　[effects/interaction.md](references/effects/interaction.md)

## 主题与图标

- CouiMaterial3Style / CouiTheme 主题　[theme/material3.md](references/theme/material3.md)
- CouiIcons 图标　[theme/icons.md](references/theme/icons.md)

首次接入时按[快速开始](https://github.com/ItosEO/COUI-Kit/blob/main/docs/guide/getting-started.md)选择模块，并核对[兼容要求](https://github.com/ItosEO/COUI-Kit/blob/main/docs/guide/compatibility.md)。
