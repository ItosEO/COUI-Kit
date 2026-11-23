# Switch 开关

点按后立即生效的设置，直接给 `com.itos.coui.switch.Switch` 传 `checked` 和 `onCheckedChange`。[Demo 独立开关](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSettingsSamples.kt#L34)

## 异步切换

点按后要等设备或服务确认时，直接用同一个 COUI `Switch` 的 `loading`。等待时滑块内会显示加载动画，组件会暂停点击和拖动。另加 `CircularProgressIndicator` 会重复显示进度；只传 `enabled = false` 会把等待显示成普通禁用。

```kotlin
Switch(
    checked = checked,
    onCheckedChange = onRequestChange,
    enabled = canChangeSetting,
    loading = saving,
    loadingDescription = savingDescription,
)
```

`canChangeSetting` 表示这项设置本身是否可用。`onRequestChange(target)` 发起请求时，宿主将该项的 `saving` 设为 `true`，保留上次确认的 `checked`。成功后更新为目标值并结束加载；失败或取消时结束加载，保留原值。`savingDescription` 使用本地化的等待说明，供无障碍服务播报。[Demo 加载开关](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoSettingsSamples.kt#L26)

## Item 中的整行开关

需要整行响应时，让行处理点击和无障碍开关语义，尾部滑块保留拖动。把同一项的请求状态传给滑块的 `loading`，并用 `enabled && !loading` 控制行的点击和按压反馈。这样点文字也不会重复提交，标题仍按设置本身的可用状态着色。页面提供回调、加载说明和[设置行标题](../containers/group.md#item)。

```kotlin
val view = LocalView.current
val source = remember { MutableInteractionSource() }
val interactive = enabled && !loading
Item(
    modifier = Modifier
        .couiButtonFeedback(CouiTheme.isDark, source, interactive, list = true)
        .toggleable(
            value = checked,
            interactionSource = source,
            indication = null,
            enabled = interactive,
            role = Role.Switch,
            onValueChange = { target ->
                view.performCouiHapticFeedback(CouiHapticFeedback.Switch)
                onCheckedChange(target)
            },
        ),
    title = title,
    trailing = {
        Switch(
            checked = checked,
            onCheckedChange = null,
            onDragCheckedChange = onCheckedChange,
            interactionSource = source,
            enabled = enabled,
            loading = loading,
            loadingDescription = loadingDescription,
        )
    },
)
```

行和开关共用交互源，点文字或滑块时能显示一致的反馈。分别点行、点滑块、拖过切换点，每次有效操作应只提交一次。请求处理中若点行仍能再次提交，检查行的 `toggleable` 是否也使用了 `enabled && !loading`。只希望点按时切换，可以省略 `onDragCheckedChange`，并保持 `onCheckedChange = null`。[整行开关示例](https://github.com/ItosEO/COUI-Kit/blob/main/demo/src/main/java/com/itos/coui/demo/DemoPreferenceRows.kt)
