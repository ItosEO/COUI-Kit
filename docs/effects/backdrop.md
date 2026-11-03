# 背景采样

让面板模糊其下方的页面内容时，先录制真实背景，再把面板作为兄弟节点绘制。

## 一个源与一个面板

下面函数把 background 内容作为不透明采样源，foreground 作为面板上的清晰内容。两个参数都是宿主的 Compose 内容，示例的尺寸和模糊半径用于展示装配。

~~~kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.itos.coui.backdrop.couiBackdrop
import com.itos.coui.backdrop.drawCouiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.shape.CouiShape

@Composable
fun BlurStage(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    background: @Composable BoxScope.() -> Unit,
    foreground: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberCouiBackdrop()
    Box(modifier.fillMaxWidth().height(200.dp)) {
        Box(
            Modifier.matchParentSize().couiBackdrop(backdrop).background(backgroundColor),
            content = background,
        )
        Box(
            Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth().height(112.dp)
                .drawCouiBackdrop(
                    backdrop = backdrop,
                    shape = { CouiShape(24.dp) },
                    effects = { blur(16.dp.toPx()) },
                ),
            content = foreground,
        )
    }
}
~~~

backgroundColor 需不透明，background 绘制实际图案或页面内容。这里的 blur 接收像素，使用所在 Density 将 dp 转换；只对采样层应用效果，前景内容保持独立。

## 源与窗口

一个 CouiBackdrop 只挂到一个内容节点，源子树不采样自身。顶栏、底栏、FAB 和效果面板放在源子树外。

源按自身尺寸录制真实内容。自定义 drawSource 时必须绘制内容，不能只录制补底；parent 用于所属窗口的源叠加。消费者负责本地坐标映射，跨窗口通过真实屏幕坐标转换。透明窗口不是屏幕截图源。

材质弹窗采样宿主窗口里不包含弹窗自身的背景源，不能只在 Dialog 内新建一个源来代替宿主背景，装配见[弹窗](../components/dialog.md#使用背景材质)。弹窗窗口里的内容若还要供菜单或其他面板采样，再为该窗口挂源。[菜单](../components/menu.md)使用 ProvideCouiMenuBackdrop 返回的 Modifier 挂到窗口页面内容，Popup 自身不进入该采样树。

## 菜单窗口的离屏策略

普通 `rememberCouiBackdrop` 和 `ProvideCouiMenuBackdrop(content)` 保持内容离屏隔离。只有明确使用 `ProvideCouiMenuBackdrop(isolateContent = false, content = ...)` 的窗口源，在没有材质消费者时使用 `CompositingStrategy.Auto`。接入片段见[菜单](../components/menu.md#空闲时按需离屏)。

菜单和材质弹窗在消费期间保留该源及其全部 parent 源，最后一个消费者结束后，各源恢复自己的空闲策略。菜单退场仍属于消费期间。源节点、布局坐标、裁剪、绘制指令录制和跨窗口更新继续存在；这不是暂停采样或缓存旧背景的开关。

选择 `false` 前，宿主需要确认以下条件：

- 内容中的 `Clear`、`DstIn`、`DstOut` 等目标混合在其所属组件内完成隔离。不能依赖这个窗口源为任意子内容提供隔离。
- 优先用于不透明、静态的页面根。取消强制离屏后，文字与矢量可能在最终变换下直接栅格化；先栅格化再变换纹理与直接绘制，在分数位移或缩放时可能产生不同的抗锯齿结果。
- 透明窗口、移动的面板和存在额外变换的容器，需要单独核对颜色、覆盖率及动画中段。未完成核对时使用默认隔离。

`Auto` 由 Android / Compose 决定实际合成方式，其他效果仍可能要求离屏缓冲。此选项不改变模糊算法、菜单内容的独立隔离或最低平台要求；性能收益需要在实际设备和操作序列上测量。

## 资源与失败处理

`drawCouiBackdrop` 的内部采样层、效果 scope 和目标坐标逐节点创建；返回的 Modifier 可用于多个面板。`shape` 裁剪背景、surface 与子内容，滤镜仅处理背景。padding 在它外侧时缩小采样和裁剪区域，在内侧时留白也包含在面板内。

通用背景和渐进背景的内部图层随节点挂载和移除成对创建、释放。自定义回调捕获的外部 renderer 仍由调用方管理，不能借复用 Modifier 共享可变绘制资源。

每个独立绘制节点持有自己的可变 renderer。单主体使用 CouiFloatingRenderer.standalone()，底栏使用 CouiFloatingRenderer()；不要跨节点共享。融合背景和遮罩也分别持有 renderer。

宿主在创建前检查平台门槛。需要错误 UI 时，在 remember 的工厂中捕获构造结果，再按结果决定是否进入组件；不要把 Compose content 放进普通 try/catch。创建异常保留原因，协程取消须继续传播。

主题、几何和位置变化后，保持当前状态流入绘制，不永久缓存旧背景。已有 RenderEffect 持有 uniform 快照；低层装配更新参数时需重建相应快照。

## 公开入口

[CouiBackdrop / rememberCouiBackdrop / couiBackdrop](../../coui/src/main/java/com/itos/coui/backdrop/CouiBackdrop.kt)、[drawCouiBackdrop / CouiBackdropEffectScope](../../coui/src/main/java/com/itos/coui/backdrop/DrawCouiBackdrop.kt)。完整采样舞台见 [DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)。
