package com.itos.coui.messageinput

import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.LocalCouiBackdrop
import com.itos.coui.effect.CouiFloatingMaterial
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.CouiFloatingShadow
import com.itos.coui.effect.CouiFloatingSurface
import com.itos.coui.effect.LocalCouiContourGlowEnabled
import com.itos.coui.effect.couiFloatingSurface
import com.itos.coui.layout.ConfigureCouiImeWindow
import com.itos.coui.theme.CouiIcon
import com.itos.coui.theme.CouiTheme

/**
 * 消息输入栏。放在所属页面的底栏或浮层，backdrop 只录制下方正文。
 *
 * state 由宿主持有，onSubmit 接收 IME 提交事件，业务校验与草稿修改由宿主处理。
 * placeholder 默认空字符串，提示文案由宿主按需提供。
 * leadingAction、trailingAction 和 primaryAction 分别配置前置、尾部和主操作。
 * 对应 show 参数控制显隐，操作为空时释放该位置，显隐规则和设置持久化由宿主处理。
 * 退场时立即释放点击和无障碍入口，图标及文本留白连续过渡。
 * 系统栏与 IME 占位由宿主容器处理，shape 应与 surfaceCornerRadius 对应。
 * 库自动配置所属窗口的 IME 调整模式，同窗最后一个输入栏或 IME 占位使用者退出时恢复。
 */
@RequiresApi(34)
@Composable
fun MessageInputBar(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    onSubmit: () -> Unit,
    enabled: Boolean = true,
    dark: Boolean = CouiTheme.isDark,
    backdrop: CouiBackdrop = checkNotNull(LocalCouiBackdrop.current) { "消息输入栏需要所属页面的正文背景源" },
    renderer: CouiFloatingRenderer = remember { CouiFloatingRenderer.standalone() },
    colors: CouiMessageInputBarColors = CouiMessageInputBarDefaults.colors(dark),
    material: CouiFloatingMaterial = CouiMessageInputBarDefaults.material(dark),
    contourGlowEnabled: Boolean = LocalCouiContourGlowEnabled.current,
    shadow: CouiFloatingShadow = CouiMessageInputBarDefaults.shadow(dark, LocalDensity.current.density, contourGlowEnabled),
    shape: Shape = CouiMessageInputBarDefaults.shape,
    surfaceCornerRadius: Dp = CouiMessageInputBarDefaults.cornerRadius,
    textStyle: TextStyle = CouiMessageInputBarDefaults.textStyle(),
    placeholder: String = "",
    inputTransformation: InputTransformation? = null,
    leadingAction: CouiMessageInputAction? = null,
    trailingAction: CouiMessageInputAction? = null,
    primaryAction: CouiMessageInputAction? = null,
    showLeadingAction: Boolean = true,
    showTrailingAction: Boolean = true,
    showPrimaryAction: Boolean = true,
) {
    ConfigureCouiImeWindow()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    var ownsFocus by remember { mutableStateOf(false) }
    var boundsSize by remember { mutableStateOf(IntSize.Zero) }
    val contentLayer = rememberGraphicsLayer()
    val leadingVisible = showLeadingAction && leadingAction != null
    val trailingVisible = showTrailingAction && trailingAction != null
    val primaryVisible = showPrimaryAction && primaryAction != null
    val leadingPresence by animateFloatAsState(if (leadingVisible) 1f else 0f,
        tween(CouiMessageInputBarDefaults.actionDurationMillis, easing = FastOutSlowInEasing), label = "MessageLeadingAction")
    val trailingPresence by animateFloatAsState(if (trailingVisible) 1f else 0f,
        tween(CouiMessageInputBarDefaults.actionDurationMillis, easing = FastOutSlowInEasing), label = "MessageTrailingAction")
    val primaryPresence by animateFloatAsState(if (primaryVisible) 1f else 0f,
        tween(CouiMessageInputBarDefaults.actionDurationMillis, easing = FastOutSlowInEasing), label = "MessagePrimaryAction")
    val textStart = CouiMessageInputBarDefaults.textStart +
        (CouiMessageInputBarDefaults.textStartWithLeadingAction - CouiMessageInputBarDefaults.textStart) * leadingPresence
    val actionCount = (if (trailingVisible) 1 else 0) + (if (primaryVisible) 1 else 0)
    val textEnd by animateDpAsState(
        if (actionCount == 0) CouiMessageInputBarDefaults.textEnd
        else CouiMessageInputBarDefaults.actionsEnd + CouiMessageInputBarDefaults.actionSize * actionCount +
            CouiMessageInputBarDefaults.actionSpacing * (actionCount - 1) + CouiMessageInputBarDefaults.textActionSpacing,
        tween(CouiMessageInputBarDefaults.actionDurationMillis, easing = FastOutSlowInEasing), label = "MessageTextEnd")
    LaunchedEffect(enabled) {
        if (!enabled && ownsFocus) {
            keyboard?.hide()
            focusManager.clearFocus()
        }
    }
    val contentColor = if (enabled) colors.content else colors.content.copy(alpha = colors.content.alpha * .38f)
    Box(modifier.fillMaxWidth().heightIn(min = CouiMessageInputBarDefaults.minHeight)
        .onSizeChanged { boundsSize = it }
        .couiFloatingSurface(backdrop, renderer, material, shadow, shape, contentLayer, contourGlowEnabled,
            foregroundComposition = true) {
            CouiFloatingSurface(Rect(Offset.Zero, boundsSize.toSize()), cornerWeight = 2f,
                cornerRadius = with(density) { surfaceCornerRadius.toPx() })
        }) {
        Box(Modifier.fillMaxWidth()
            .drawWithContent { contentLayer.record { this@drawWithContent.drawContent() } }
            .clip(shape)) {
            Box(Modifier.fillMaxWidth().padding(start = textStart, end = textEnd)
                .heightIn(min = CouiMessageInputBarDefaults.minHeight),
                contentAlignment = Alignment.CenterStart) {
                BasicTextField(state = state,
                    modifier = Modifier.fillMaxWidth().padding(vertical = CouiMessageInputBarDefaults.textPadding)
                        .onFocusChanged { ownsFocus = it.hasFocus },
                    enabled = enabled, textStyle = textStyle.copy(color = contentColor),
                    cursorBrush = SolidColor(colors.cursor), inputTransformation = inputTransformation,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 3),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    onKeyboardAction = { if (enabled) onSubmit() },
                    decorator = { field ->
                        Box {
                            if (state.text.isEmpty()) BasicText(placeholder,
                                style = textStyle.copy(color = colors.hint), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            field()
                        }
                    })
            }
            // 裁剪边界与输入留白使用同一帧尺寸，退场图标随占位收拢，避免覆盖文字。
            // 子项保留按钮原尺寸和起止锚点，区域变窄时由外围裁剪。
            Box(Modifier.align(Alignment.BottomStart).width(textStart)
                .height(CouiMessageInputBarDefaults.minHeight).clipToBounds()) {
                MessageAction(Modifier.align(Alignment.BottomStart)
                    .wrapContentSize(Alignment.BottomStart, unbounded = true)
                    .padding(start = CouiMessageInputBarDefaults.leadingActionStart, bottom = CouiMessageInputBarDefaults.leadingActionBottom)
                    .size(CouiMessageInputBarDefaults.leadingActionSize), leadingAction, colors.content, enabled,
                    visible = leadingVisible, presence = leadingPresence)
            }
            Box(Modifier.align(Alignment.BottomEnd).width(textEnd)
                .height(CouiMessageInputBarDefaults.minHeight).clipToBounds()) {
                MessageAction(Modifier.align(Alignment.BottomEnd)
                    .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                    .padding(end = CouiMessageInputBarDefaults.actionsEnd, bottom = CouiMessageInputBarDefaults.actionsBottom)
                    .offset(x = -(CouiMessageInputBarDefaults.actionSize + CouiMessageInputBarDefaults.actionSpacing) * primaryPresence)
                    .size(CouiMessageInputBarDefaults.actionSize), trailingAction, colors.content, enabled,
                    visible = trailingVisible, presence = trailingPresence)
                MessageAction(Modifier.align(Alignment.BottomEnd)
                    .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                    .padding(end = CouiMessageInputBarDefaults.actionsEnd, bottom = CouiMessageInputBarDefaults.actionsBottom)
                    .size(CouiMessageInputBarDefaults.actionSize), primaryAction, colors.content, enabled,
                    visible = primaryVisible, presence = primaryPresence, pressedColor = colors.pressedIcon)
            }
        }
    }
}

@Composable
private fun MessageAction(modifier: Modifier, action: CouiMessageInputAction?, color: Color,
    enabled: Boolean, visible: Boolean, presence: Float, pressedColor: Color = color) {
    var retainedAction by remember { mutableStateOf(action) }
    val displayedAction = action ?: retainedAction
    // 配置移除后保留退场图标，动画结束即释放图标及宿主回调。
    SideEffect { retainedAction = if (visible || presence > 0f) displayedAction else null }
    if (displayedAction == null || (!visible && presence == 0f)) return
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val actionEnabled = enabled && displayedAction.enabled
    val interactionModifier = if (visible) Modifier
        .clickable(interaction, indication = null, enabled = actionEnabled, role = Role.Button, onClick = displayedAction.onClick)
        .semantics { contentDescription = displayedAction.contentDescription }
    else Modifier.clearAndSetSemantics {}
    val tint = if (!actionEnabled) color.copy(alpha = color.alpha * .38f) else if (pressed) pressedColor else color
    Box(modifier.then(interactionModifier), contentAlignment = Alignment.Center) {
        CouiIcon(Modifier.size(CouiMessageInputBarDefaults.iconSize).graphicsLayer {
            alpha = presence
            scaleX = CouiMessageInputBarDefaults.hiddenIconScale + (1f - CouiMessageInputBarDefaults.hiddenIconScale) * presence
            scaleY = scaleX
        }, displayedAction.icon, null, tint = tint)
    }
}
