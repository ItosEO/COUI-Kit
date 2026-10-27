package com.itos.coui.dialog

import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.itos.coui.backdrop.CouiBackdrop
import kotlinx.coroutines.flow.first

/**
 * 单输入弹窗预设，复用 [CouiAlertDialog] 的窗口、操作区与材质。
 *
 * 对应 Settings 17 的 COUIAlertDialog.SingleInput / COUIInputView。value 包含宿主的草稿、
 * 选区和输入法组合区；输入过滤、counter 文案、counterIsError 与确认启用条件均由宿主持有。
 * 清除只回调空值，不提交确认。accent 用于焦点线和光标，clearContentDescription 由宿主本地化。
 * 默认等待窗口获焦后请求输入焦点和键盘；autoFocus=false 时由用户点击输入区进入编辑。
 * properties 必须保留 decorFitsSystemWindows=true；预设显式请求窗口 resize，勿再叠加 imePadding。
 */
@Composable
fun CouiInputDialog(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    confirmAction: CouiDialogAction,
    dark: Boolean,
    accent: Color,
    clearContentDescription: String,
    title: String? = null,
    message: String? = null,
    placeholder: String? = null,
    counter: String? = null,
    counterIsError: Boolean = false,
    dismissAction: CouiDialogAction? = null,
    placement: CouiDialogPlacement = CouiDialogPlacement.Bottom,
    colors: CouiDialogColors = CouiDialogDefaults.colors(dark),
    material: CouiDialogMaterial? = null,
    backdrop: CouiBackdrop? = null,
    autoFocus: Boolean = true,
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = true),
) {
    require(properties.decorFitsSystemWindows) { "输入型弹窗需保留 decorFitsSystemWindows=true，由窗口 resize 避让键盘" }
    CouiAlertDialog(modifier = modifier, onDismissRequest = onDismissRequest, confirmAction = confirmAction,
        dark = dark, title = title, dismissAction = dismissAction, placement = placement,
        colors = colors, material = material, backdrop = backdrop, properties = properties) {
        val window = checkNotNull((LocalView.current.parent as? DialogWindowProvider)?.window) { "未找到输入弹窗所属 Window" }
        SideEffect {
            // 原厂请求 ALWAYS_VISIBLE；公开 Compose 窗口另需明确 ADJUST_RESIZE，避免旧的 unspecified 策略。
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                if (autoFocus) WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                else WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        }
        // SingleInput 属于 BottomAssignment：正文到 customPanel 是 4dp，不沿用普通警告的 16dp。
        if (!message.isNullOrEmpty()) DialogText(Modifier.fillMaxWidth()
            .padding(horizontal = CouiDialogDefaults.contentPadding)
            .padding(bottom = DialogInputDefaults.messageBottom), message, DialogTextKind.Message, colors.message)
        DialogInput(Modifier.fillMaxWidth().padding(horizontal = CouiDialogDefaults.contentPadding)
            .padding(bottom = DialogInputDefaults.bottomMargin), value, onValueChange, dark, accent,
            colors, placeholder, counter, counterIsError, clearContentDescription, autoFocus)
    }
}

/** 来自 SingleInput 布局、COUIInputView、Widget.COUI.InputPreference 与 COUIEditText。 */
private object DialogInputDefaults {
    val messageBottom = 4.dp
    val bottomMargin = 18.dp
    val verticalPadding = 15.dp
    val textMinHeight = 22.dp
    val buttonSpacing = 8.dp
    val clearSize = 22.dp
    val strokeWidth = 0.33000004.dp
    val focusedStrokeWidth = 1.dp
    const val focusDuration = 250
    val focusEasing = CubicBezierEasing(0f, 0f, .1f, 1f)
}

@Composable
private fun DialogInput(
    modifier: Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    dark: Boolean,
    accent: Color,
    colors: CouiDialogColors,
    placeholder: String?,
    counter: String?,
    counterIsError: Boolean,
    clearContentDescription: String,
    autoFocus: Boolean,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val windowInfo = LocalWindowInfo.current
    var focused by remember { mutableStateOf(false) }
    val focusLine = remember { Animatable(0f) }
    val hintColor = if (dark) Color(0x4DFFFFFF) else Color(0x42000000)
    val dividerColor = if (dark) Color(0x33FFFFFF) else Color(0x1F000000)
    val style = rememberDialogTextStyle(DialogTextKind.Input).copy(color = colors.title, textAlign = TextAlign.Start)
    LaunchedEffect(autoFocus, windowInfo) {
        if (autoFocus) {
            snapshotFlow { windowInfo.isWindowFocused }.first { it }
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
    LaunchedEffect(focused) {
        focusLine.snapTo(0f)
        if (focused) focusLine.animateTo(1f, tween(DialogInputDefaults.focusDuration, easing = DialogInputDefaults.focusEasing))
    }
    Row(modifier.drawBehind {
        // Android getDimensionPixelSize 为正的小尺寸至少取 1px；焦点线从物理左侧展开，包含计数/清除区域。
        val normal = DialogInputDefaults.strokeWidth.roundToPx().coerceAtLeast(1).toFloat()
        val active = DialogInputDefaults.focusedStrokeWidth.roundToPx().coerceAtLeast(1).toFloat()
        drawRect(dividerColor, Offset(0f, size.height - normal), Size(size.width, normal))
        if (focused) drawRect(accent, Offset(0f, size.height - active), Size(size.width * focusLine.value, active))
    }.padding(vertical = DialogInputDefaults.verticalPadding), verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(value, onValueChange,
            modifier = Modifier.weight(1f).heightIn(min = DialogInputDefaults.textMinHeight)
                .focusRequester(focusRequester).onFocusChanged { focused = it.isFocused },
            singleLine = true, textStyle = style, cursorBrush = SolidColor(accent),
            decorationBox = { input ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.text.isEmpty() && !placeholder.isNullOrEmpty())
                        DialogText(text = placeholder, kind = DialogTextKind.Input, color = hintColor, textAlign = TextAlign.Start)
                    input()
                }
            })
        if (counter != null || (focused && value.text.isNotEmpty())) {
            Row(Modifier.padding(start = DialogInputDefaults.buttonSpacing),
                horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
                if (counter != null) DialogText(Modifier.padding(start = DialogInputDefaults.buttonSpacing),
                    counter, DialogTextKind.Counter, if (counterIsError) colors.destructive else hintColor)
                if (focused && value.text.isNotEmpty()) {
                    Box(Modifier.padding(start = DialogInputDefaults.buttonSpacing)) {
                        DialogInputClear(Modifier.size(DialogInputDefaults.clearSize)
                            .semantics { contentDescription = clearContentDescription }
                            .clickable(interactionSource = null, indication = null, role = Role.Button) {
                                onValueChange(TextFieldValue("", TextRange.Zero))
                                focusRequester.requestFocus()
                            }, colors.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogInputClear(modifier: Modifier, color: Color) {
    // 以原厂 22dp 图标的圆与镂空交叉几何绘制，不携带 APK 位图或资源依赖。
    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        val unit = size.width / 22f
        drawCircle(color, 9f * unit)
        drawLine(Color.Transparent, Offset(8.03f, 8.03f) * unit, Offset(13.97f, 13.97f) * unit,
            1.6f * unit, StrokeCap.Round, blendMode = BlendMode.Clear)
        drawLine(Color.Transparent, Offset(13.97f, 8.03f) * unit, Offset(8.03f, 13.97f) * unit,
            1.6f * unit, StrokeCap.Round, blendMode = BlendMode.Clear)
    }
}
