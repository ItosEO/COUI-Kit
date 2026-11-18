package com.itos.coui.textfield

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.lerp
import com.itos.coui.theme.CouiTheme

/**
 * 页面内的圆角输入框：卡片底色、无边框、标签、占位提示与前后图标槽。
 * 值、校验和提交由宿主持有；组件只渲染当前值、光标与局部焦点，点击卡片任意位置都会聚焦，
 * 槽位内的控件保留自己的点击。配色取 [CouiTextFieldColors]，不创建局部主题作用域。
 *
 * 空态文案分两层：[label] 是字段标题，[placeholder] 是输入区提示。
 * 只传 label 时它先在输入位置当大字提示，有内容后上浮并缩小；同时传 placeholder 时标签停在卡片顶部，
 * 输入区由 placeholder 提示，有内容后 placeholder 消失。
 * `useLabelAsPlaceholder = true` 时标签自己当占位提示，有内容后整体隐藏，placeholder 不再参与。
 *
 * 聚焦不画描边、不改底色，也不改标签颜色：编辑态的线索只有光标，其余外观保持不变。
 *
 * 单行时 `minLines` 与 `maxLines` 不参与布局；多行时按两者在最小高度内增长并在 `maxLines` 处滚动。
 * `enabled = false` 停止输入并改用禁用文字色，`readOnly = true` 仍可聚焦、选择与复制。
 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    label: String? = null,
    useLabelAsPlaceholder: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    dark: Boolean = CouiTheme.isDark,
    colors: CouiTextFieldColors = CouiTextFieldDefaults.colors(dark),
    shape: Shape = CouiTextFieldDefaults.shape,
    contentPadding: PaddingValues = CouiTextFieldDefaults.contentPadding,
    textStyle: TextStyle = CouiTextFieldDefaults.textStyle(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    requireTextFieldLines(singleLine, minLines, maxLines)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)
    BasicTextField(value = value, onValueChange = currentOnValueChange,
        modifier = modifier.couiTextFieldSurface(shape, colors.container),
        enabled = enabled, readOnly = readOnly,
        textStyle = textStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines,
        visualTransformation = visualTransformation, onTextLayout = currentOnTextLayout,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        decorationBox = { inner ->
            CouiTextFieldDecoration(label = label, valueIsEmpty = value.isEmpty(),
                useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                enabled = enabled, textStyle = textStyle, colors = colors,
                leading = leading, trailing = trailing, contentPadding = contentPadding,
                singleLine = singleLine, inner = inner)
        })
}

/**
 * 同一外观的选区重载：宿主持有文本、选区和光标位置，用于聚焦时全选、指定初始光标等场景。
 * 需要编辑事务或撤销栈时改用 [TextField] 的 `TextFieldState` 重载。
 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String? = null,
    label: String? = null,
    useLabelAsPlaceholder: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    dark: Boolean = CouiTheme.isDark,
    colors: CouiTextFieldColors = CouiTextFieldDefaults.colors(dark),
    shape: Shape = CouiTextFieldDefaults.shape,
    contentPadding: PaddingValues = CouiTextFieldDefaults.contentPadding,
    textStyle: TextStyle = CouiTextFieldDefaults.textStyle(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    requireTextFieldLines(singleLine, minLines, maxLines)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)
    BasicTextField(value = value, onValueChange = currentOnValueChange,
        modifier = modifier.couiTextFieldSurface(shape, colors.container),
        enabled = enabled, readOnly = readOnly,
        textStyle = textStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, minLines = if (singleLine) 1 else minLines,
        maxLines = if (singleLine) 1 else maxLines,
        visualTransformation = visualTransformation, onTextLayout = currentOnTextLayout,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        decorationBox = { inner ->
            CouiTextFieldDecoration(label = label, valueIsEmpty = value.text.isEmpty(),
                useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                enabled = enabled, textStyle = textStyle, colors = colors,
                leading = leading, trailing = trailing, contentPadding = contentPadding,
                singleLine = singleLine, inner = inner)
        })
}

/**
 * 同一外观的宿主状态重载：编辑事务、撤销与光标由 [state] 持有，组件不复制文本。
 * `inputTransformation` 与 `outputTransformation` 直接交给 Foundation，用于长度、格式和掩码限制。
 * 键盘动作沿用 Foundation 的状态分支：只有默认动作回调，拿不到当前 ImeAction。
 * 多行滚动位置由 [scrollState] 持有，需要与宿主滚动联动时自行创建并传入。
 */
@Composable
fun TextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    placeholder: String? = null,
    label: String? = null,
    useLabelAsPlaceholder: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    dark: Boolean = CouiTheme.isDark,
    colors: CouiTextFieldColors = CouiTextFieldDefaults.colors(dark),
    shape: Shape = CouiTextFieldDefaults.shape,
    contentPadding: PaddingValues = CouiTextFieldDefaults.contentPadding,
    textStyle: TextStyle = CouiTextFieldDefaults.textStyle(),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    requireTextFieldLines(singleLine, minLines, maxLines)
    val currentOnTextLayout by rememberUpdatedState(onTextLayout)
    BasicTextField(state = state,
        modifier = modifier.couiTextFieldSurface(shape, colors.container),
        enabled = enabled, readOnly = readOnly,
        inputTransformation = inputTransformation,
        textStyle = textStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, onKeyboardAction = onKeyboardAction,
        lineLimits = if (singleLine) TextFieldLineLimits.SingleLine
            else TextFieldLineLimits.MultiLine(minLines, maxLines),
        onTextLayout = currentOnTextLayout, scrollState = scrollState,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        outputTransformation = outputTransformation,
        decorator = object : TextFieldDecorator {
            @Composable
            override fun Decoration(innerTextField: @Composable () -> Unit) {
                CouiTextFieldDecoration(label = label, valueIsEmpty = state.text.isEmpty(),
                    useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                    enabled = enabled, textStyle = textStyle, colors = colors,
                    leading = leading, trailing = trailing, contentPadding = contentPadding,
                    singleLine = singleLine, inner = innerTextField)
            }
        })
}

/** 卡片轮廓属于输入本体，命中区包含留白。 */
private fun Modifier.couiTextFieldSurface(shape: Shape, color: Color): Modifier =
    defaultMinSize(minHeight = CouiTextFieldDefaults.minHeight).clip(shape).background(color)

/**
 * 标签的落位。`Inline` 是空值且输入区没有自己的提示时，标签占据输入位置当大字提示；
 * `Anchored` 是有内容、或输入区已经由 [placeholder] 提供提示时，标签停在卡片顶部的小字位置。
 */
private enum class CouiLabelState { Hidden, Inline, Anchored, Dismissed }

/** 标签浮动与输入下移共用的无回弹弹簧，参数与菜单按下指示一致。 */
private val LabelFloatSpec = spring<Float>(dampingRatio = 1f, stiffness = 1754.5962f)

/**
 * 装饰也属于输入本体：图标槽与留白不拦截输入焦点，槽位内的可点击控件先行消费事件。
 * 标签与输入共用 [textStyle]；上浮只改字号和纵向偏移，不额外撑高卡片。
 * 占位提示与输入文字共用同一排版、同一条偏移；多行时图标与文字对齐首行。
 */
@Composable
private fun CouiTextFieldDecoration(
    label: String?,
    valueIsEmpty: Boolean,
    useLabelAsPlaceholder: Boolean,
    placeholder: String?,
    enabled: Boolean,
    textStyle: TextStyle,
    colors: CouiTextFieldColors,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    contentPadding: PaddingValues,
    singleLine: Boolean,
    inner: @Composable () -> Unit,
) {
    val labelState = remember(label, valueIsEmpty, useLabelAsPlaceholder, placeholder) {
        when {
            label.isNullOrEmpty() -> CouiLabelState.Hidden
            // 标签自己当提示：空值占输入位，有内容后整段隐藏，不与 placeholder 叠加。
            useLabelAsPlaceholder -> if (valueIsEmpty) CouiLabelState.Inline else CouiLabelState.Dismissed
            !valueIsEmpty -> CouiLabelState.Anchored
            // 空值但输入区已有自己的提示，标签让位停在顶部，两层文案同时可见。
            placeholder != null -> CouiLabelState.Anchored
            else -> CouiLabelState.Inline
        }
    }
    val float by animateFloatAsState(
        targetValue = if (labelState == CouiLabelState.Anchored) 1f else 0f,
        animationSpec = LabelFloatSpec,
        label = "couiTextFieldLabelFloat",
    )
    val labelColor = if (enabled) colors.label else colors.disabled
    val labelText = when (labelState) {
        CouiLabelState.Inline, CouiLabelState.Anchored -> label
        else -> null
    }
    // 标签占据输入位时由它承担空态提示；其余情况 placeholder 只在空值出现，有内容即消失。
    val showsPlaceholder = placeholder != null && valueIsEmpty && labelState != CouiLabelState.Inline
    val shift = CouiTextFieldDefaults.labelShift * float
    Row(modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(CouiTextFieldDefaults.slotSpacing),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top) {
        leading?.invoke()
        Box(Modifier.weight(1f)) {
            if (labelText != null) {
                BasicText(labelText, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = textStyle.copy(color = labelColor, fontSize = lerp(labelFontSize(textStyle),
                        CouiTextFieldDefaults.floatingLabelSize, float)),
                    modifier = Modifier.offset(y = -shift))
            }
            // 提示与输入共用同一条偏移，标签停在顶部时二者一起下移，不会错行。
            Box(Modifier.offset(y = shift)) {
                if (showsPlaceholder) {
                    BasicText(checkNotNull(placeholder), maxLines = if (singleLine) 1 else Int.MAX_VALUE,
                        overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
                        style = textStyle.copy(color = if (enabled) colors.placeholder else colors.disabled))
                }
                inner()
            }
        }
        trailing?.invoke()
    }
}

/** 标签常态字号跟随输入排版；宿主传入未指定字号的 [TextStyle] 时回退到 Defaults。 */
private fun labelFontSize(textStyle: TextStyle) =
    if (textStyle.fontSize.isUnspecified) CouiTextFieldDefaults.labelSize else textStyle.fontSize

/** 行数只在多行时生效，单行由 [singleLine] 决定高度。 */
private fun requireTextFieldLines(singleLine: Boolean, minLines: Int, maxLines: Int) {
    if (singleLine) return
    require(minLines >= 1) { "minLines 必须大于零" }
    require(maxLines >= minLines) { "maxLines 不能小于 minLines" }
}
