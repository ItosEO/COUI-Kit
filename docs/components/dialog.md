# 弹窗

CouiAlertDialog 显示带标题、正文与受控操作的 COUI 弹窗，支持底部或居中位置以及可选项目材质。

## 在页面中装配

下面是可放入宿主 Compose 页面的完整容器。页面持有显示状态，宿主提供背景色、深浅色、主题色、文案与确认回调；实际 App 将这些文案放入自己的字符串资源。backgroundColor 必须不透明，pageContent 是弹窗下方的真实页面内容。

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.dialog.CouiAlertDialog
import com.itos.coui.dialog.CouiDialogAction
import com.itos.coui.dialog.CouiDialogActionStyle
import com.itos.coui.dialog.CouiDialogDefaults

@Composable
fun DialogPage(
    modifier: Modifier = Modifier,
    dark: Boolean,
    accent: Color,
    backgroundColor: Color,
    openLabel: String,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    pageContent: @Composable ColumnScope.() -> Unit,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().couiBackdrop(backdrop).background(backgroundColor)) {
            Button(onClick = { visible = true }) { Text(openLabel) }
            pageContent()
        }
        if (visible) {
            val close = { visible = false }
            CouiAlertDialog(
                onDismissRequest = close,
                dark = dark,
                title = title,
                message = message,
                confirmAction = CouiDialogAction(confirmLabel, { onConfirm(); close() },
                    style = CouiDialogActionStyle.Primary,
                    colors = CouiDialogDefaults.primaryActionColors(accent)),
                dismissAction = CouiDialogAction(cancelLabel, close),
                properties = DialogProperties(usePlatformDefaultWidth = false, windowTitle = title),
                material = if (CouiDialogDefaults.isMaterialSupported()) CouiDialogDefaults.material(dark) else null,
                backdrop = backdrop,
            )
        }
    }
}
~~~

这里的源只挂到页面 Column，弹窗在源子树外，通过屏幕坐标采样该源。已有不透明页面源时直接传入其 CouiBackdrop；已有 ProvideCouiMenuBackdrop 的宿主也可省略 backdrop，沿用当前 Provider 的源。不要把弹窗或应用自画遮罩录入源。不需要材质时省略 material 和背景源即可使用。

库不持有业务可见性。确认、取消和第三个操作分别执行各自的 onClick；是否关闭由宿主决定。返回和外点触发 onDismissRequest，不提交确认；禁用操作不提交点击。触摸探索启用时禁止外点关闭，保留允许的返回路径。

操作默认 Secondary，Primary 需要显式提供配色。CouiDialogAction.colors 可以分别覆盖启用、禁用的填色与文字色；neutralAction 默认使用破坏性文字色，操作显式配色优先。库不读取宿主 MaterialTheme。

## 布局与内容

placement 使用 CouiDialogPlacement.Bottom 或 Center。两项短操作横排，单项、三项或文案放不下时竖排；正文滚动与操作区独立。操作区只有实际超高时才创建滚动视口，正常高度允许按钮运动越过正文边界，卡片外侧仍使用项目平滑形状裁剪。

content 是正文区的 ColumnScope slot，供宿主加入非滚动内容；草稿、校验和确认保存归宿主。不要在 slot 中嵌套另一个全屏纵向滚动，也不要把操作按钮放入正文视口。面板和操作使用项目平滑圆角，修改 shape 时 cornerRadius 必须与它匹配。宽度上限应用于包含左右留白的外框，宿主无需重复加留白；底部位置结合导航 inset 计算外边距。

标题、正文、操作按目标字体、字重、缩放和 TextView 字体度量在 Compose 中排版；按钮、文字与窗口承载均为内部实现，不另建公共按钮或 Typography 组件。字体来自运行设备，没有打包 OEM 字体。

## 背景材质

默认不透明分支可直接使用。API 33 起，可传入 material = CouiDialogDefaults.material(dark)。需要背景采样时，显式提供所属窗口的 CouiBackdrop，或在已有 ProvideCouiMenuBackdrop 范围内调用弹窗。内容源按[背景采样](../effects/backdrop.md)装配，使用 Provider 提供的 Modifier 录制页面；弹窗、应用自画遮罩及采样效果自身不进入源。缺少平台支持或窗口源时明确报错。

处理顺序为项目 Fast Kawase、材质混色、共享 Dialog 轮廓、前景内容。模糊等级是原厂引擎等级，不是 Gaussian 像素半径。Defaults 在浅色选择 ColorDodge、深色选择 Overlay；颜色或模式改变后更新 RenderEffect 快照。自定义材质的 blendMode 默认 Overlay，其他既有材质消费者仍保留原行为。窗口 DIM 由系统 Window 单独处理，不录入 Compose 源。

内部次按钮在材质分支使用半透明白色并关闭 pressed 蒙层；不透明分支恢复中性底色与 pressed 蒙层，避免浅色按钮融入白色面板。两者继续使用相同的形变、轮廓和跟手光。旧平台不冒充具有相同 GPU 材质；Shader 创建错误向上传播。

## 窗口属性与边界

默认 properties 关闭平台默认宽度以使用 COUI 外框策略。返回、外点、安全策略、平台宽度、decorFitsSystemWindows、windowTitle、窗口类型与 token 转交 Compose。自定义 DialogProperties 时如要保留 COUI 宽度，应显式设置 usePlatformDefaultWidth = false。位置、动画、DIM、透明窗口背景和系统模糊由弹窗统一管理；外观通过 shape、colors、material 指定，不通过平台窗口 Shape、blur 或 scrim 属性另设一套外观。

Tiny、推荐/AI Payment 按钮、列表 Controller 与原厂极端高度的完整动态拆分/合并协议未移植；content slot 不代表这些 Controller 已实现。公开 inset 的导航类型识别与厂商私有设置存在承载差异。跨 ROM 字体、极矮窗口、键盘、RTL、无障碍及 GPU 动画中段仍需设备验收。

## Demo 中的样式与示例

进入 Demo 的“组件 → 弹窗”分组，可体验以下装配示例。它们均使用 CouiAlertDialog，通过位置、内容与操作参数组合，不是六个独立组件。

| 示例 | 展示内容 |
| --- | --- |
| 底部确认弹窗 | 底部显示标题、短正文与确定/取消操作，演示常规确认场景。 |
| 居中确认弹窗 | 将相同内容放到屏幕中央，体验居中窗口的进入和退出。 |
| 长内容滚动 | 正文超过可用高度后滚动，操作区保持独立可达。 |
| 三个操作 | 增加“重置选择”，演示三项操作的竖排及独立回调。 |
| 禁用确认操作 | 确认按钮显示禁用状态，取消仍可使用。 |
| 主题色确认弹窗 | 将确定操作改为宿主主题色，取消保持次操作外观。 |

支持平台可通过分组中的“弹窗背景材质”开关，在项目模糊材质和不透明表面间比较；轮廓总开关及深浅色沿用 Demo 现有设置。确定更新示例结果，取消、外点和返回只关闭窗口，“重置选择”清除示例结果。

装配源码见 [DemoDialogSamples.kt](../../demo/src/main/java/com/itos/coui/demo/DemoDialogSamples.kt)。公开参数见 [CouiAlertDialog / CouiDialogAction](../../coui/src/main/java/com/itos/coui/dialog/CouiAlertDialog.kt) 与 [CouiDialogDefaults](../../coui/src/main/java/com/itos/coui/dialog/CouiDialogDefaults.kt)。
