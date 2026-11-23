# MessageInputBar 消息输入栏

在 API 34+ 将输入栏放入 COUI `Scaffold.bottomBar`。输入栏补足导航栏占位，宿主负责 IME 避让。键盘弹出后若输入栏和键盘之间多出空白，优先核对父容器与底栏是否重复添加底部留白。

使用其他页面容器时，显式传入不含输入栏自身的[正文背景源](../effects/backdrop.md#正文背景源)。[MessageInputBar 用法](https://github.com/ItosEO/COUI-Kit/blob/main/docs/components/message-input-bar.md)

让输入法动作和发送按钮调用同一个 `submit`。编辑状态、发送图标与描述由页面提供，按钮使用组件自带的材质和反馈。[Demo 消息输入栏](https://github.com/ItosEO/COUI-Kit/blob/v0.0.1-alpha16/demo/src/main/java/com/itos/coui/demo/DemoMessageInputSample.kt#L132)

```kotlin
MessageInputBar(
    state = draft,
    onSubmit = submit,
    primaryAction = if (draft.text.isNotBlank()) {
        CouiMessageInputAction(sendIcon, sendDescription, onClick = submit)
    } else null,
)
```
