# 开关

CouiSwitch 提供受控的二值切换、弹簧拖动与吸附，以及可由宿主装配的加载状态。

## 基本用法

以下代码位于宿主 Compose 函数中。checked 和 change 由业务层提供，accent 和 dark 来自应用主题，label 来自应用字符串资源。

```kotlin
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.itos.coui.switch.CouiSwitch
import com.itos.coui.switch.CouiSwitchDefaults

CouiSwitch(
    modifier = Modifier.semantics { contentDescription = label },
    checked = checked,
    onCheckedChange = change,
    dark = dark,
    colors = CouiSwitchDefaults.colors(accent, dark),
)
```

独立开关拥有 Switch 点击、键盘和无障碍语义，并提供触摸留白；视觉尺寸由 Defaults 持有。禁用时传 enabled = false，同一组件可以显示禁用已选中和禁用未选中，两者都是状态示例。

## 点击、拖动与整行装配

点击请求新 checked，宿主决定是否接受。超过拖动阈值后，开关拥有本次拖动，抬手按最近锚点、速度与位移门控吸附；结束后只请求一次业务变化。RTL 反转几何位置，checked 的逻辑含义不变。动画中反向保留已有位置和速度。

CANCEL、父容器接管、禁用和移除终止手势，不提交新业务值，并回到宿主状态。这是受控组件的明确适配：提取版本的原厂 CompoundButton 在 CANCEL 后仍可能吸附并改变值。宿主拒绝拖动候选时，组件回到实际 checked。

需要整行交互时，外层使用同一个 MutableInteractionSource 持有唯一 toggleable 和语义，子开关传 onCheckedChange = null。若仍需在滑块区域拖动，再把同一业务回调传给 onDragCheckedChange；子开关不建立第二个点击 owner。外层应自行提供文案、布局及点击反馈，不把 Demo 的私有行布局当作库 API。

开关自己的点击和成功改变候选值的拖动，通过当前 View 调用原厂开关反馈码，遵守系统触觉设置与附着状态；hapticFeedbackEnabled = false 可关闭。程序赋值、初始化、取消和禁用不额外振动，不支持该码的 ROM 不合成替代波形。整行点击若由宿主持有，其触觉也由宿主处理。原厂声音与专用马达波形不属于本实现。

## 加载状态

加载是同一个 CouiSwitch 的能力。宿主在请求开始时设置 loading = true，保留原 checked；业务成功时提交新 checked 并清除 loading，失败或取消时只清除 loading。组件不启动网络任务，也不自行在动画结束后切换值。

```kotlin
CouiSwitch(
    modifier = Modifier.semantics { contentDescription = label },
    checked = checked,
    onCheckedChange = requestChange,
    dark = dark,
    colors = CouiSwitchDefaults.colors(accent, dark),
    onDragCheckedChange = null,
    loading = loading,
    loadingDescription = loadingDescription,
)
```

requestChange 和 loading 由宿主业务管理；loadingDescription 为宿主本地化文案。示例在等待开始前也关闭拖动，因此显式传 onDragCheckedChange = null。等待期间禁止重复输入，并以状态说明和 live region 表达加载。

滑块缩小、加载图像放大显现并旋转；停止时恢复滑块并立即关闭加载图绘制。快速重开保留原厂两组 AnimatorSet 的时序，节点移除释放动画。进入 loading 或禁用输入不截断已经开始的普通切换，手势则按上述受控规则取消。

加载图像直接使用随库打包的原厂 PNG，每个节点独立持有 Drawable，配置变化时重新加载。图像保留原始像素、透明尾迹与 density；按整数边界映射，绕浮点滑块中心缩放、旋转。来源和第三方权利见[图像来源](../../coui/RESOURCE_SOURCES.md)。不需要系统设置包、OEM SDK、私有 SO 或运行时反射。

## 外观与平台

轨道与拉伸后的滑块使用项目 CouiCapsuleShape；这是保留的平滑几何适配，原厂滑块使用 drawRoundRect。原厂这一分支明确关闭 spotlight，普通开关不叠按钮材质和高光描边。宿主通过 colors 提供配色，加载图保持原图白色，不随 thumb 配色染色。

沿库最低平台运行，无 RuntimeShader 门槛。不包含 OEM 换肤分支。参数与源码核对、原图一致或构建通过不等于所有设备和帧率均已完成 1:1 验收。

## Demo 示例

进入原 Demo 的“组件”页，找到“开关”分组，也可以按分组标题搜索。分组内集中展示：

- 普通点击/拖动，以及带标题说明的同一组件。
- 加载状态：直接给同一个 CouiSwitch 传 loading，模拟等待完成后再提交选择。
- 禁用未选中、禁用已选中：两个状态示例，不是独立控件。

[DemoSwitchSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSwitchSamples.kt)持有模拟业务与状态示例，[DemoSamples](../../demo/src/main/java/com/itos/coui/demo/DemoSamples.kt)注册分组，[DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)登记搜索标题。库入口与配色见 [CouiSwitch / CouiSwitchDefaults](../../coui/src/main/java/com/itos/coui/switch/CouiSwitch.kt)，实现依据见[开关决定](../../.agents/notes/implemented/feature/2026-10-27-coui-switch.md)。
