# Sheet 面板

ModalBottomSheet 提供独立窗口的底部编辑面板，包含可选顶部拖动条、入退场、遮罩、下拖关闭和键盘避让。内容、确认按钮及草稿由宿主装配。showDragHandle=false 隐藏拖动条；如使用面板工具栏，按实际页面分支选择该值。

## 基本用法

在宿主 Composable 中持续调用组件，用 visible 控制目标；不能写成 `if (visible) ModalBottomSheet(...)`，否则关闭时会直接移除窗口而跳过退场。下面的 label、cancel、save、dark、opened、draft 和回调来自宿主资源与状态；正文可换成输入、滚轮或列表。

```kotlin
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import com.itos.coui.sheet.ModalBottomSheet

ModalBottomSheet(
    visible = opened,
    onDismissRequest = closeDraft,
    title = label,
    dismissDescription = cancel,
    dark = dark,
    sheetHeight = 520.dp,
) {
    Text(label)
    Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
        Text(draft)
    }
    TextButton(onClick = saveDraft) { Text(save) }
}
```

weight 由本组件的 ColumnScope 提供，不需要额外导入。sheetHeight 包含拖动条和底部留白；null 按内容测量。高度始终受窗口顶部总留白 40dp 的可用范围限制，横屏及键盘出现时也受约束。长内容由宿主放入一个滚动容器，避免重复滚动。固定高度下可将标题和操作放在滚动容器外。

## 状态与退出

visible 是唯一展开目标，组件只持有运动状态。返回、遮罩点按、向下释放和无障碍 dismiss 请求 onDismissRequest；宿主决定关闭或保留草稿。确认按钮由宿主拥有，确认后再将 visible 置 false。组件不会自行保存、创建闹钟或写入日历。

顶部拖动条可直接拖动。直接下拖在纵向快速甩动或面板越过中线时请求退出。内容到顶后，未消费的下拉交给面板。面板尚未回到展开位置且最后消费的是下拉时，释放会请求退出。上拖优先把面板送回展开位置，再交给内容滚动。系统取消回到展开位置，节点移除释放动画。

拖动条按下有 200ms COUI 缓动的 70×20dp 按压阴影。`draggable = false` 关闭拖动。`dismissOnClickOutside = false` 时，返回与无障碍 dismiss 仍可请求退出。

半屏内容中的 Menu 会把面板可见区域作为弹出边界，菜单自身按可用高度滚动；宿主仍需保证菜单数据与业务状态。普通底部面板的入退场使用位移与遮罩，不执行居中/锚点分支的缩放。遮罩与位移使用相同 response；固定高度与内容测量共用同一套运动。

入退场运行中改变目标保留速度。拖动接管后清除先前动画的待处理目标和速度，从当前手动位置释放；取消回展开位置，节点移除停止两路动画和完成回调。

窗口可用高度、面板可见高度或导航 peek 变化时重新发布运动目标；内容安全留白独立处理，不重复从可见高度扣除。

当前提供一个展开位置和隐藏位置，对应原厂 skipCollapsed 的装配。顶部安全区、底部留白和 IME 在面板窗口内处理，宿主不要再次给面板追加 navigationBarsPadding / imePadding。Dialog 使用公开平台窗口，未覆盖 OEM 小折叠屏、锚点跟手弹出、嵌入多窗、上拉业务退出及多级 Fragment 动态合并协议。

## 材质与平台

默认使用不透明表面。需要半透明材质时传 `CouiSheetDefaults.material(dark)` 及宿主的 CouiBackdrop，要求 API 33；缺少源或平台不支持时明确抛出错误。源不得包含本面板及遮罩。通过宿主窗口的 ProvideCouiMenuBackdrop 和 rememberCouiWindowBackdrop 取源，具体要求见[背景采样](../effects/backdrop.md)。

面板复用公共 Fast Kawase、混色和轮廓实现，并选择 Panel 专用内晕预设；轮廓光绘制在清晰内容之后。Sheet Defaults 单独持有 bottomdialog 资源参数。每个面板独立持有 renderer，不与宿主弹窗共享可变实例。形状使用项目平滑轮廓，顶部 32dp 圆角、底部直边；公开 GPU 承载不代表厂商窗口渲染逐像素相同。菜单在面板内的边界与高度规则见[菜单](menu.md)。

`isolateContent` 默认保留窗口内容的离屏隔离。设为 `false` 时，没有后续材质菜单或弹窗消费当前窗口源期间采用 Auto；有消费者时恢复离屏隔离，退场结束后释放。选择此值前，内容中的 Clear、DstIn、DstOut 等目标混合必须自行隔离，具体约束见[背景采样](../effects/backdrop.md#菜单窗口的离屏策略)。该选项不暂停实时背景采样，也不改变面板本身的模糊。

面板窗口为内容里的后续菜单或弹窗提供自己的源，包含当前面板与 Compose 遮罩，并叠加外层页面；本面板表面始终采样进入窗口前取得的宿主源。内容里的 rememberCouiWindowBackdrop 返回当前面板窗口源，无需再包一层 ProvideCouiMenuBackdrop，也不能把它反传给本面板自身。

SheetToolbar 提供居中标题和两侧操作 slot，要求 API 34。slot 收到 CouiToolbarStyle，可传给已有 TopAppBarButton；按钮采用窗口内的纯色底与轮廓预设，采样单独录制的面板背景，不采样含自身的窗口内容源。该工具栏必须位于 ModalBottomSheet 内容中。其他同窗口浮动控件可调用 rememberCouiSheetSurfaceBackdrop 取纯背景源；后续独立 Popup/Dialog 仍取窗口完整内容源。

标题使用 Panel 的 18sp、550 字重和字体留白，最多两行；操作文字使用工具栏菜单的固定 14dp、500 字重。标题按两侧实际较宽的操作区对称避让，长标题可增加工具栏高度。slot 中的文字读取收到的 `style.textStyle` 或 `LocalCouiTextStyle`，不要另套警告弹窗按钮或 M3 的字号。

Demo 的闹钟草稿按普通手机 Clock 分支使用可用最大高度，隐藏拖动条，装配面板工具栏、提示、等宽时间列、类型标签、名称及设置卡片；颜色覆盖属于闹钟消费者，通用 Defaults 保持原值。设置行切换演示选项，未实现原厂类型业务及铃声、振动和稍后提醒子页。

Demo 提供闹钟、倒计时两个编辑示例，共用同一个 Sheet。倒计时使用编辑页的三列滚轮及名称/铃声/常用项草稿；闹钟分类标签组合已有 ChipTabRow，并由 Demo 传入主题色作为 selectedColor。Sheet 的内容 slot 保持通用。

内容可以自行拥有顶栏，无需同时装配 SheetToolbar。嵌入上游 Scaffold 时，它只录制自己的正文源；Sheet 持有所属窗口的完整源，宿主不再重复提供同一窗口源。系统顶部和底部 inset 已由 Sheet 处理，内容不再重复追加。

[公开组件与 Defaults](../../coui/src/main/java/com/itos/coui/sheet/ModalBottomSheet.kt)、[运动实现](../../coui/src/main/java/com/itos/coui/sheet/CouiSheetMotion.kt)、[Demo 两种草稿编辑](../../demo/src/main/java/com/itos/coui/demo/DemoSheetSamples.kt)。Demo 的时间/倒计时组合在 Demo，不成为新的库预设。
