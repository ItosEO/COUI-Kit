---
name: coui-kit
description: 在 Android Jetpack Compose 应用接入或使用 COUI-Kit（组件、背景模糊材质、浮层、主题适配）时使用，防止 API 门槛崩溃、采样失效和样式偏差。
---

# COUI-Kit 接入指南

按需打开对应 Reference。每节按「推荐用法 → 最小代码 → 避坑」编写。

## 全局红线

1. **API 门槛**：库 `minSdk = 31`。调用前判断 `Build.VERSION.SDK_INT`：
   - **API 34+**：`NavigationBar`、`Fab`、`TopAppBarButton`、`TopAppBarSearch`、`ChipTab`、`ChipTabRow`、`SegmentedControl`、`MessageInputBar`、`couiShadow`、`CouiFloatingRenderer`、`CouiShadowRenderer`（低版本保留宿主等价入口）。
   - **API 33+**：`RuntimeShader` 渐进模糊、轮廓光、`CouiMetaballRenderer`、弹窗与面板材质。
2. **背景源分流**：顶栏、底栏、`Fab`、搜索、输入栏用**正文源**（COUI `Scaffold` 自动提供；第三方容器挂 `.couiBackdrop`）。`Menu`、`Dialog`、`BottomSheet` 用**窗口源**（`Scaffold` 不提供，根部挂 `ProvideCouiMenuBackdrop`）。**禁止自采样。**
3. **M3 适配两步**：引 `coui-material3` 后，在 `Application.onCreate()` 调一次 `Coui.initialize(defaultStyle = CouiMaterial3Style())`。支持独立切深浅色时，同步提供 `LocalCouiDarkTheme`。
4. **默认材质**。API 33+ 且有窗口源时，`SingleChoiceDialog` 与 `BottomSheet` 自动启用材质。`Dialog`、`InputDialog` 和 `LoadingDialog` 可显式传入 `CouiDialogDefaults.material` 与 `backdrop`。卡片内 `SegmentedControl` 用填色。
5. **独立 Renderer**：`CouiFloatingRenderer`、`CouiMetaballRenderer`、`CouiShadowRenderer` 禁止跨节点共享；`standalone()` 与无参构造不可混用；创建失败由宿主降级。
6. **浮层常驻**：`Dialog` 系列传 `visible`，`BottomSheet` 传 `MutableState<Boolean>`。禁止外包 `if (visible)`，否则丢退场动画。
7. **版本对齐**：`coui`、`coui-material3`、`coui-icons` 同版本，勿混用源码与 Maven 坐标。接入基线（`minSdk` 31、`compileSdk` 37.1、AGP `9.1.0`）见 [官方指南](https://github.com/ItosEO/COUI-Kit/tree/main/docs/guide)。

## 按需查阅

| 场景 | 文档 |
| --- | --- |
| M3 适配、局部主题、配色排查、`Text`、HCT、`CouiIcons` | [theme-and-icons.md](references/theme-and-icons.md) |
| 正文/窗口源、模糊、阴影、浮动表面、反馈、回弹、融球 | [backdrop-and-effects.md](references/backdrop-and-effects.md) |
| `Scaffold`、顶栏/折叠/自定义、`NavigationBar`、`Fab`、搜索、输入栏 | [bars-and-scaffold.md](references/bars-and-scaffold.md) |
| `Menu`（行/按钮锚点）、`Dialog` 系列、`BottomSheet` | [overlays.md](references/overlays.md) |
| `Group`/`Item`/`Divider`、`TextField`、`Switch`、`Checkbox`/`RadioButton`、`Slider`、`ChipTabRow`、`SegmentedControl`、选择器 | [list-and-controls.md](references/list-and-controls.md) |
