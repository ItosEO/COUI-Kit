# Dialog / InputDialog / LoadingDialog

**三种弹窗都显式传 `visible`。默认启用弹窗材质。** 在[窗口根部安装背景源](../effects/backdrop.md#窗口背景源)，再从其作用域读取 `rememberCouiWindowBackdrop()`。Android 13（API 33）起且背景源可用时，传 `CouiDialogDefaults.material(dark)` 和同一个 `backdrop`。仅添加背景源不会开启弹窗材质，`Dialog` 的 `material` 默认是 `null`。[Demo 的材质选择](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt#L48)

## 确认弹窗

在页面的 Composable 中保留下面的调用。打开按钮将 `visible` 设为 `true`，关闭或确认时设为 `false`，让弹窗完成退场动画。页面提供文案及 `onConfirm`。

```kotlin
var visible by rememberSaveable { mutableStateOf(false) }
val backdrop = rememberCouiWindowBackdrop()
val material = if (CouiDialogDefaults.isMaterialSupported() && backdrop != null) {
    CouiDialogDefaults.material(CouiTheme.isDark)
} else {
    null
}

Dialog(
    visible = visible,
    onDismissRequest = { visible = false },
    title = title,
    message = message,
    confirmAction = CouiDialogAction(confirmText, {
        onConfirm()
        visible = false
    }),
    dismissAction = CouiDialogAction(cancelText, { visible = false }),
    material = material,
    backdrop = backdrop,
)
```

外层用 `if (visible)` 包住调用会在关闭时立即移除窗口。低于 API 33 时仍传 `visible`，使用不透明表面。

## 输入与加载

从列表打开改名弹窗时，每次点行都用该记录的名称重置草稿，再显示弹窗。这样改过 A 后取消，打开 B 会看到 B 的原名。下面的 `openEditor` 接列表行的点击事件，`onRename` 接页面的保存操作。两种弹窗沿用上面的 `material` 和 `backdrop`。[Demo 编辑会话](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt#L68)

```kotlin
var editing by rememberSaveable { mutableStateOf(false) }
var recordId by rememberSaveable { mutableStateOf("") }
val draft = rememberTextFieldState()
val openEditor: (String, String) -> Unit = { id, name ->
    recordId = id
    draft.edit {
        replace(0, length, name)
        selection = TextRange(name.length)
    }
    editing = true
}

InputDialog(
    state = draft,
    visible = editing,
    title = title,
    clearContentDescription = clearText,
    onDismissRequest = { editing = false },
    confirmAction = CouiDialogAction(saveText, {
        onRename(recordId, draft.text.toString())
        editing = false
    }, enabled = draft.text.isNotBlank()),
    dismissAction = CouiDialogAction(cancelText, { editing = false }),
    material = material,
    backdrop = backdrop,
)

LoadingDialog(
    visible = loading,
    message = loadingText,
    material = material,
    backdrop = backdrop,
)
```

加载弹窗的 `loading` 读取真实保存任务的状态。保存失败时结束加载并显示页面已有的错误提示。若提供“重新编辑”，保留该记录的 ID 和未保存草稿，直接恢复 `editing = true`，重新走 `openEditor` 会覆盖这次输入。加载期间默认按返回键会继续等待；产品支持取消任务时，将 `onDismissRequest` 接到任务的取消操作。

输入弹窗自行处理键盘。若宿主页也用了 `imePadding()`，只在 `LocalWindowInfo.current.isWindowFocused` 为真时给宿主页加这层留白，否则弹窗输入时背后的页面也会被抬起。[弹窗与宿主页的键盘避让](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/docs/components/dialog.md#宿主页的键盘避让)
