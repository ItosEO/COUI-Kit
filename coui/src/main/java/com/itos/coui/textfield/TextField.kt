@file:Suppress("DEPRECATION") // 两个受控值重载保留 Foundation 的值与选区契约。

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.toTextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.hintText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.lerp as lerpInt
import com.itos.coui.theme.CouiTheme

/**
 * 页面内的圆角输入框：卡片底色、无边框、标签、占位提示与前后图标槽。
 * 值、校验和提交由宿主持有；组件只渲染当前值、光标与局部焦点，点击卡片任意位置都会聚焦，
 * 槽位内的控件保留自己的点击。配色取 [CouiTextFieldColors]，不创建局部主题作用域。
 *
 * 空态文案分两层：[label] 是字段标题，[placeholder] 是输入区提示。
 * 只传 label 时它先在输入位置当大字提示，有内容后上浮并缩小；同时传 placeholder 时标签停在卡片顶部，
 * 输入区由 placeholder 提示，有内容后 placeholder 消失。
 * `useLabelAsPlaceholder = true` 时标签自己当占位提示，有内容后只隐藏视觉文案。
 * 空态以显示变换后的文本为准。标签优先作为字段的无障碍提示，独立占位提示按有效行数上限显示。
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
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    val resolvedStyle = resolvedTextFieldStyle(textStyle)
    BasicTextField(value = value, onValueChange = onValueChange,
        modifier = modifier.couiTextFieldSurface(shape, colors.container, label, placeholder),
        enabled = enabled, readOnly = readOnly,
        textStyle = resolvedStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, minLines = minLines, maxLines = maxLines,
        visualTransformation = visualTransformation, onTextLayout = onTextLayout,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        decorationBox = { inner ->
            val visibleTextIsEmpty = if (visualTransformation == VisualTransformation.None) value.isEmpty()
                else remember(value, visualTransformation) {
                    visualTransformation.filter(AnnotatedString(value)).text.isEmpty()
                }
            CouiTextFieldDecoration(label = label, visibleTextIsEmpty = visibleTextIsEmpty,
                useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                enabled = enabled, textStyle = resolvedStyle, colors = colors,
                leading = leading, trailing = trailing, contentPadding = contentPadding,
                singleLine = singleLine, maxLines = maxLines, inner = inner)
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
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    val resolvedStyle = resolvedTextFieldStyle(textStyle)
    BasicTextField(value = value, onValueChange = onValueChange,
        modifier = modifier.couiTextFieldSurface(shape, colors.container, label, placeholder),
        enabled = enabled, readOnly = readOnly,
        textStyle = resolvedStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, minLines = minLines, maxLines = maxLines,
        visualTransformation = visualTransformation, onTextLayout = onTextLayout,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        decorationBox = { inner ->
            val visibleTextIsEmpty = if (visualTransformation == VisualTransformation.None) value.text.isEmpty()
                else remember(value, visualTransformation) {
                    visualTransformation.filter(value.annotatedString).text.isEmpty()
                }
            CouiTextFieldDecoration(label = label, visibleTextIsEmpty = visibleTextIsEmpty,
                useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                enabled = enabled, textStyle = resolvedStyle, colors = colors,
                leading = leading, trailing = trailing, contentPadding = contentPadding,
                singleLine = singleLine, maxLines = maxLines, inner = inner)
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
    textStyle: TextStyle = TextStyle.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    inputTransformation: InputTransformation? = null,
    outputTransformation: OutputTransformation? = null,
    onTextLayout: (Density.(getResult: () -> TextLayoutResult?) -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    interactionSource: MutableInteractionSource? = null,
    cursorBrush: Brush = SolidColor(colors.cursor),
) {
    val resolvedStyle = resolvedTextFieldStyle(textStyle)
    BasicTextField(state = state,
        modifier = modifier.couiTextFieldSurface(shape, colors.container, label, placeholder),
        enabled = enabled, readOnly = readOnly,
        inputTransformation = inputTransformation,
        textStyle = resolvedStyle.copy(color = if (enabled) colors.text else colors.disabled),
        keyboardOptions = keyboardOptions, onKeyboardAction = onKeyboardAction,
        lineLimits = if (singleLine) TextFieldLineLimits.SingleLine
            else TextFieldLineLimits.MultiLine(minLines, maxLines),
        onTextLayout = onTextLayout, scrollState = scrollState,
        interactionSource = interactionSource, cursorBrush = cursorBrush,
        outputTransformation = outputTransformation,
        decorator = { inner ->
            val visibleTextIsEmpty = if (outputTransformation == null) state.text.isEmpty() else {
                val buffer = state.toTextFieldBuffer()
                with(outputTransformation) { buffer.transformOutput() }
                buffer.length == 0
            }
            CouiTextFieldDecoration(label = label, visibleTextIsEmpty = visibleTextIsEmpty,
                useLabelAsPlaceholder = useLabelAsPlaceholder, placeholder = placeholder,
                enabled = enabled, textStyle = resolvedStyle, colors = colors,
                leading = leading, trailing = trailing, contentPadding = contentPadding,
                singleLine = singleLine, maxLines = maxLines, inner = inner)
        })
}

@Composable
private fun resolvedTextFieldStyle(textStyle: TextStyle): TextStyle {
    val defaultStyle = CouiTextFieldDefaults.textStyle()
    val style = defaultStyle.merge(textStyle)
    if (!style.fontSize.isEm) return style
    val size = with(LocalDensity.current) {
        (defaultStyle.fontSize.toPx() * style.fontSize.value).toSp()
    }
    return style.copy(fontSize = size)
}

private fun Modifier.couiTextFieldSurface(shape: Shape, color: Color, label: String?, placeholder: String?): Modifier {
    val fieldName = label?.takeIf { it.isNotEmpty() } ?: placeholder?.takeIf { it.isNotEmpty() }
    val surface = defaultMinSize(minHeight = CouiTextFieldDefaults.minHeight).clip(shape).background(color)
    return if (fieldName == null) surface else surface.semantics { hintText = fieldName }
}

/** 浮动标签沿用 COUI 菜单按压指示的弹簧。 */
private val LabelFloatSpec = spring<Float>(dampingRatio = 1f, stiffness = 1754.5962f)

@Composable
private fun CouiTextFieldDecoration(
    label: String?,
    visibleTextIsEmpty: Boolean,
    useLabelAsPlaceholder: Boolean,
    placeholder: String?,
    enabled: Boolean,
    textStyle: TextStyle,
    colors: CouiTextFieldColors,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    contentPadding: PaddingValues,
    singleLine: Boolean,
    maxLines: Int,
    inner: @Composable () -> Unit,
) {
    val fieldLabel = label?.takeIf { it.isNotEmpty() }
    val labelIsHint = useLabelAsPlaceholder && fieldLabel != null
    val floatingLabel = fieldLabel.takeUnless { labelIsHint }
    val inputHint = (if (labelIsHint) fieldLabel else placeholder)?.takeIf { it.isNotEmpty() }
    val labelColor = if (enabled) colors.label else colors.disabled
    val hintColor = when {
        labelIsHint -> labelColor
        enabled -> colors.placeholder
        else -> colors.disabled
    }
    Row(modifier = Modifier.padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(CouiTextFieldDefaults.slotSpacing),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top) {
        leading?.invoke()
        CouiTextFieldContent(Modifier.weight(1f), label = floatingLabel,
            labelCanInline = inputHint == null,
            labelAnchored = !visibleTextIsEmpty || inputHint != null,
            hint = inputHint.takeIf { visibleTextIsEmpty },
            hintHasOwnSemantics = fieldLabel != null && inputHint != fieldLabel,
            textStyle = textStyle, labelColor = labelColor, hintColor = hintColor,
            maxHintLines = if (singleLine || labelIsHint) 1 else maxLines, inner = inner)
        trailing?.invoke()
    }
}

@Composable
private fun CouiTextFieldContent(
    modifier: Modifier,
    label: String?,
    labelCanInline: Boolean,
    labelAnchored: Boolean,
    hint: String?,
    hintHasOwnSemantics: Boolean,
    textStyle: TextStyle,
    labelColor: Color,
    hintColor: Color,
    maxHintLines: Int,
    inner: @Composable () -> Unit,
) {
    val textMeasurer = if (label != null) rememberTextMeasurer(cacheSize = 3) else null
    val progress = if (label != null && labelCanInline) {
        animateFloatAsState(targetValue = if (labelAnchored) 1f else 0f,
            animationSpec = LabelFloatSpec, label = "couiTextFieldLabelFloat").value.coerceIn(0f, 1f)
    } else 1f
    val floatingSize = CouiTextFieldDefaults.floatingLabelSize
    Layout(modifier = modifier, content = {
        Box { inner() }
        if (hint != null) {
            BasicText(hint, modifier = if (hintHasOwnSemantics) Modifier else Modifier.semantics { hideFromAccessibility() },
                maxLines = maxHintLines, overflow = TextOverflow.Ellipsis, style = textStyle.copy(color = hintColor))
        }
        if (label != null) {
            BasicText(label, modifier = Modifier.clipToBounds().semantics { hideFromAccessibility() },
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = textStyle.copy(color = labelColor, fontSize = lerp(textStyle.fontSize, floatingSize, progress)))
        }
    }) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        var labelBand = 0
        var inlineOffset = 0
        if (label != null && textMeasurer != null) {
            val editorLineHeight = textMeasurer.measure("", style = textStyle, maxLines = 1,
                overflow = TextOverflow.Ellipsis, constraints = loose).size.height
            val floatingHeight = textMeasurer.measure(label, style = textStyle.copy(fontSize = floatingSize),
                maxLines = 1, overflow = TextOverflow.Ellipsis, constraints = loose).size.height
            val available = (loose.maxHeight - editorLineHeight).coerceAtLeast(0)
            labelBand = floatingHeight.coerceAtMost(available)
            if (labelCanInline) {
                // 按两个端点留出整行，使首字可以立即移入独立输入区。
                val inlineHeight = textMeasurer.measure(label, style = textStyle, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, constraints = loose).size.height
                labelBand = maxOf(labelBand, inlineHeight).coerceAtMost(available)
                inlineOffset = CouiTextFieldDefaults.labelShift.roundToPx().coerceAtMost(available - labelBand)
                labelBand += inlineOffset
            }
        }

        val bodyConstraints = loose.offset(vertical = -labelBand)
        val editor = measurables.first().measure(bodyConstraints)
        val placeholder = if (hint == null) null else measurables[1].measure(bodyConstraints)
        val title = if (label == null) null else measurables.last().measure(loose.copy(maxHeight = labelBand))
        val editorBaseline = editor.firstBaseline()
        val hintBaseline = placeholder?.firstBaseline() ?: 0
        val bodyBaseline = maxOf(editorBaseline, hintBaseline)
        val editorY = bodyBaseline - editorBaseline
        val hintY = bodyBaseline - hintBaseline
        val bodyHeight = maxOf(editorY + editor.height, hintY + (placeholder?.height ?: 0))
        val titleBaseline = title?.firstBaseline() ?: 0
        val inlineLabelTop = (inlineOffset + bodyBaseline - titleBaseline)
            .coerceIn(0, labelBand - (title?.height ?: 0))
        val inlineBodyTop = (inlineLabelTop + titleBaseline - bodyBaseline).coerceIn(0, labelBand)
        val bodyTop = if (labelAnchored) labelBand else lerpInt(inlineBodyTop, labelBand, progress)
        val width = constraints.constrainWidth(maxOf(editor.width, placeholder?.width ?: 0, title?.width ?: 0))
        val height = constraints.constrainHeight(labelBand + bodyHeight)

        layout(width, height, mapOf(FirstBaseline to bodyTop + bodyBaseline)) {
            if (title != null) {
                title.placeRelative(0, lerpInt(inlineLabelTop, 0, progress))
            }
            placeholder?.placeRelative(0, bodyTop + hintY)
            editor.placeRelative(0, bodyTop + editorY)
        }
    }
}

private fun Placeable.firstBaseline(): Int = this[FirstBaseline].let {
    if (it == AlignmentLine.Unspecified) height else it
}
