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

## 资源与失败处理

每个独立绘制节点持有自己的可变 renderer。单主体使用 CouiFloatingRenderer.standalone()，底栏使用 CouiFloatingRenderer()；不要跨节点共享。融合背景和遮罩也分别持有 renderer。

宿主在创建前检查平台门槛。需要错误 UI 时，在 remember 的工厂中捕获构造结果，再按结果决定是否进入组件；不要把 Compose content 放进普通 try/catch。创建异常保留原因，协程取消须继续传播。

主题、几何和位置变化后，保持当前状态流入绘制，不永久缓存旧背景。已有 RenderEffect 持有 uniform 快照；低层装配更新参数时需重建相应快照。

## 公开入口

[CouiBackdrop / rememberCouiBackdrop / couiBackdrop](../../coui/src/main/java/com/itos/coui/backdrop/CouiBackdrop.kt)、[drawCouiBackdrop / CouiBackdropEffectScope](../../coui/src/main/java/com/itos/coui/backdrop/DrawCouiBackdrop.kt)。完整采样舞台见 [DemoVisualEffects](../../demo/src/main/java/com/itos/coui/demo/DemoVisualEffects.kt)。
