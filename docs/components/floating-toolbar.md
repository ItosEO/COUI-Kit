# FloatingToolbar 悬浮操作栏

编辑页面可以把常用操作放在底部胶囊里。调用方提供动作列表与点击回调，工具栏负责增删时的回弹，并在宽度不足时把剩余动作收进“更多”。组件位于 `com.itos.coui.floatingtoolbar`，需要 Android 14（API 34）及以上，且依赖不包含操作栏自身的 `CouiBackdrop` 背景源。

## 最小可用用法

放入 COUI `Scaffold` 的 `bottomBar` 槽位时可直接省略 `backdrop`、`renderer`、`style` 与 `themeTint`，自动跟随容器背景源与当前主题：

~~~kotlin
@RequiresApi(34)
@Composable
fun EditorActions(
    modifier: Modifier = Modifier,
    items: List<CouiFloatingToolbarItem<String>>,
    visible: Boolean,
    onAction: (String) -> Unit,
) {
    FloatingToolbar(
        modifier = modifier,
        items = items,
        onItemClick = onAction,
        visible = visible,
    )
}
~~~

每个动作提供唯一且稳定的 `value`、无障碍与菜单共用的 `label`、`ImageVector` 图标。显示顺序跟随列表。使用业务 ID 作为 `value`，删除前面的动作后，后续动作仍能沿用自己的身份。普通按钮和“更多”菜单都通过 `onItemClick` 提交该值。`enabled = false` 时两个入口都禁用。

放在第三方布局时，通过 `backdrop` 指定同页的不透明 SDR 背景源。“更多”菜单优先采样当前窗口，宿主没有提供窗口采样源时使用工具栏的 `backdrop`，此时采样范围应覆盖菜单弹出的区域。默认每个工具栏创建自己的绘制器，需要处理初始化失败时可显式传入 `CouiFloatingRenderer.standalone()`。[背景采样](../effects/backdrop.md)

## 收起整栏与增删动作

- 切换整栏的 `visible` 时让组件留在组合中。收起时整栏缩小淡出，前景图标随之模糊；显示时放大回弹，图标逐渐清晰。前景模糊跟随系统材质设置。按钮从收起开始停止响应，工具区保留布局高度占位。
- 修改动作列表时，移除项保留到这批退出动画完成。先用 200ms 改变间距，然后间距在 200ms 内恢复，图标同步按 response 0.5s、bounce 0.4 的弹簧缩放。连续修改按提交顺序播放。胶囊边缘跟随当帧首尾图标。
- 一次提交新的 `items` 列表，可以在任意位置同时增减多个图标。清空列表会让所有图标一起退场；再次添加时按同一套规则入场。切换整栏的 `visible` 会保留已选图标。

## 布局、安全区与配色

列表入口占满父容器可用宽度。胶囊高 `44.dp`（`CouiFloatingToolbarDefaults.height`），基础工具区含上下各 `16.dp` 悬浮留白共 `76.dp`（`CouiFloatingToolbarDefaults.containerHeight`）。组件参照 `NavigationBar` 内置处理横向与底部 `WindowInsets.tappableElement` 安全区及悬浮留白（手势导航额外增加 `8.dp` 底部边距）；放入 `Scaffold.bottomBar` 时不要再外叠 `navigationBarsPadding()`，页面键盘避让仍由宿主容器统一处理。足够宽时七项能直接显示，八项会留出“更多”。

默认使用当前主题的底栏玻璃材质，可通过顶层 `dark`、`style` 或 `themeTint` 覆盖外观。`themeTint` 同步作用于胶囊玻璃材质（纯色模式下忽略）与“更多”菜单，传 `Color.Transparent` 关闭染色；宿主选择纯色模式时，使用 `CouiFloatingToolbarDefaults.style(containerColor = color)` 替换玻璃底色。用 `iconTint` 覆盖单个动作，多色矢量图标传入 `Color.Unspecified` 可显示自带颜色。整栏按压、跟手光与投影各由一个胶囊承担，单项有自己的按压波纹、悬停与焦点反馈。

在可滚动页面中，轻点图标会执行操作。从胶囊上开始拖动时，操作栏跟手形变，页面保持原位；从胶囊外起手可正常滚动页面。

## 约束与避坑

- **标识唯一性**：`items` 中每个 `CouiFloatingToolbarItem.value` 必须唯一，重复会触发 `require` 异常。
- **勿外叠底部安全区**：组件内部已处理 `WindowInsets.tappableElement`，放入 `Scaffold.bottomBar` 时勿再套 `navigationBarsPadding()`；局部卡片预览且未由父级消费系统栏 Insets 时，请通过外层容器消费或自行处理。
- **保持组合与安全占位**：需要播放整栏退场动画时必须让组件留在组合中并切换 `visible`，且收起后仍保留工具区高度占位。

## 源码与完整示例

- **公开实现**：[FloatingToolbarActions.kt](../../coui/src/main/java/com/itos/coui/floatingtoolbar/FloatingToolbarActions.kt)、[FloatingToolbar.kt](../../coui/src/main/java/com/itos/coui/floatingtoolbar/FloatingToolbar.kt)、[CouiFloatingToolbarDefaults.kt](../../coui/src/main/java/com/itos/coui/floatingtoolbar/CouiFloatingToolbarDefaults.kt)
- **完整示例**：[DemoFloatingToolbar.kt](../../demo/src/main/java/com/itos/coui/demo/DemoFloatingToolbar.kt)
