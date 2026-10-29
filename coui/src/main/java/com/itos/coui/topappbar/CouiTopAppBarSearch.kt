@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.itos.coui.topappbar

import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.LayerOutsets
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.lookaheadScopeCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.relocation.BringIntoViewModifierNode
import androidx.compose.ui.relocation.bringIntoView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.CouiFloatingSurface
import com.itos.coui.effect.SpotlightPreset
import com.itos.coui.effect.couiFloatingSurface
import com.itos.coui.effect.rememberCouiSpotlight
import com.itos.coui.motion.couiPressMotion
import com.itos.coui.motion.couiTouchMotion
import com.itos.coui.motion.rememberCouiPressMotion
import com.itos.coui.motion.rememberCouiTouchMotion
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** 标题槽位入口。visible 只控制整组淡化, 不改变搜索展收目标或页面查询。 */
@RequiresApi(34)
@Composable
fun CouiTopAppBarSearch(
    modifier: Modifier = Modifier,
    state: CouiTopAppBarSearchState,
    style: CouiToolbarStyle,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String,
    focusPending: Boolean,
    onFocused: () -> Unit,
    enabled: Boolean = true,
    visible: Boolean = true,
    endPadding: Dp = 6.dp,
    trailingIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCouiToolbarStyle provides style) {
        val requester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current
        BoxWithConstraints(
            modifier.fillMaxWidth().height(64.dp)
                .then(SearchRelocationBoundary),
            contentAlignment = Alignment.CenterStart,
        ) {
            val density = LocalDensity.current
            val targetWidth = (constraints.maxWidth - with(density) { endPadding.roundToPx() }).coerceAtLeast(0).toFloat()
            SideEffect {
                if ((visible && state.motion.expanded) || state.expandedWidth == 0f) state.expandedWidth = targetWidth
            }
            CouiTopAppBarActionVisibility(visible = visible, animateSize = false) {

                Box(
                    Modifier.then(if (state.motion.expanded) Modifier.clearAndSetSemantics {} else Modifier)
                        .graphicsLayer { alpha = state.motion.toolbarAlpha },
                ) { title() }

            }

            val renderer = checkNotNull(state.renderer)
            if (state.motion.present && state.expandedWidth > 0f) {
                val overlay = checkNotNull(LocalCouiToolbarOverlay.current)
                val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                val fullWidth = state.expandedWidth
                val height = with(density) { 40.dp.roundToPx().toFloat() }
                val progress = state.motion.progress
                val visibleWidth = max(height, height + (fullWidth - height) * progress)
                val blurRadius = max(0f, 1f - abs((1f - progress) - 0.5f) * 2f) * 30f
                var expandedCenter by remember { mutableStateOf<Offset?>(null) }
                val blur = remember(blurRadius) {
                    if (blurRadius > 0f) RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.DECAL)
                        .asComposeRenderEffect() else null
                }
                val outsets = remember(density) { LayerOutsets(all = (120f / density.density).dp) }
                overlay.apply {
                    CouiTopAppBarActionVisibility(
                        modifier = Modifier.padding(end = endPadding)
                            .renderInSharedTransitionScopeOverlay(renderInOverlay = { true }, zIndexInOverlay = 1f),
                        visible = visible,
                        animateSize = false,
                    ) {
                        Layout(
                            content = {
                                SearchSurface(
                                    modifier = Modifier,
                                    renderer = renderer,
                                    capsule = state.motion.expanded,
                                    interactive = enabled && visible && state.motion.expanded,
                                    deform = enabled && visible && state.motion.expanded && !state.motion.running,
                                    contentRenderEffect = blur,
                                ) {
                                    // 内容按最终宽度排版, 只裁剪可见宽度, 不对文字和图标做缩放。
                                    Layout(content = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(Modifier.padding(start = 2.dp).size(36.dp), contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Outlined.Search, null, Modifier.size(24.dp))
                                                }
                                                Box(Modifier.weight(1f).graphicsLayer { alpha = progress.coerceIn(0f, 1f) }) {
                                                    BasicTextField(
                                                        value = query,
                                                        onValueChange = onQueryChange,
                                                        modifier = Modifier.fillMaxWidth().focusRequester(requester)
                                                            .semantics { contentDescription = placeholder },
                                                        enabled = enabled && visible && state.motion.expanded,
                                                        singleLine = true,
                                                        textStyle = LocalCouiToolbarStyle.current.textStyle.copy(
                                                            color = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f), fontSize = 15.sp,
                                                        ),
                                                        cursorBrush = SolidColor(LocalCouiToolbarStyle.current.cursorColor),
                                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                                        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                                                        decorationBox = { input ->
                                                            Box(contentAlignment = Alignment.CenterStart) {
                                                                if (query.isEmpty()) Text(
                                                                    placeholder,
                                                                    color = LocalContentColor.current.copy(alpha = 0.6f),
                                                                    fontSize = 15.sp,
                                                                    maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
                                                                )
                                                                input()
                                                            }
                                                        },
                                                    )
                                                }
                                                Box(Modifier.graphicsLayer { alpha = progress.coerceIn(0f, 1f) }) { trailingIcon() }
                                            }
                                    }) { measurables, constraints ->
                                        val child = measurables.single().measure(Constraints.fixed(fullWidth.roundToInt(), constraints.maxHeight))
                                        layout(constraints.maxWidth, constraints.maxHeight) {
                                            child.place(if (rtl) constraints.maxWidth - child.width else 0, 0)
                                        }
                                    }
                                }
                            },
                        ) { measurables, constraints ->
                            val child = measurables.single().measure(Constraints.fixed(visibleWidth.roundToInt(), height.roundToInt()))
                            layout(constraints.maxWidth, constraints.maxHeight) {
                                val iconCenter = if (rtl) constraints.maxWidth - height * 0.5f else height * 0.5f
                                val top = (constraints.maxHeight - child.height) / 2
                                val center = Offset(iconCenter, top + height * 0.5f)
                                var offset = Offset.Zero
                                if (!isLookingAhead) coordinates?.let { target ->
                                    val root = lookaheadScopeCoordinates
                                    if (expandedCenter == null || (visible && state.motion.expanded && !state.motion.running)) {
                                        expandedCenter = root.localPositionOf(target, center)
                                    }
                                    // 收起保留顶栏内的展开端点, 不跟随恢复中的标题槽位重算路径。
                                    val end = checkNotNull(expandedCenter)
                                    offset = target.localPositionOf(root, lerp(state.sourceCenter ?: end, end, progress)) - center
                                }
                                child.placeWithLayer(if (rtl) constraints.maxWidth - child.width else 0, top) {
                                    translationX = offset.x
                                    translationY = offset.y
                                    this.outsets = outsets
                                }
                            }
                        }
                    }
                }
                LaunchedEffect(focusPending, enabled, visible, state.motion.expanded) {
                    if (focusPending && enabled && visible && state.motion.expanded) {
                        requester.requestFocus()
                        keyboard?.show()
                        onFocused()
                    }
                }
            }

        }
    }
}

/** 与展开搜索框共用材质。收起尾段仍允许重新按下入口。 */
@RequiresApi(34)
@Composable
fun CouiTopAppBarSearchButton(
    modifier: Modifier = Modifier,
    state: CouiTopAppBarSearchState,
    style: CouiToolbarStyle,
    onClick: () -> Unit,
    contentDescription: String,
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalCouiToolbarStyle provides style) {
        val overlay = LocalCouiToolbarOverlay.current
        CouiTopAppBarSearchAction(
            modifier = modifier.onPlaced {
                if ((!state.motion.present || (!state.motion.expanded && state.sourceCenter == null))) {
                    state.sourceCenter = checkNotNull(overlay).lookaheadScopeCoordinates(it)
                        .localPositionOf(it, Offset(it.size.width * 0.5f, it.size.height * 0.5f))
                }
            },
            state = state,
        ) {
            Box(
                Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                val open = { state.motion.finishCollapse(); onClick() }

                if (state.motion.present) {
                    Box(Modifier.size(40.dp).semantics { this.contentDescription = contentDescription }
                            .clickable(
                            interactionSource = null, indication = null,
                            enabled = enabled && !state.motion.expanded, role = Role.Button, onClick = open,
                    ))
                } else {
                    SearchSurface(
                        modifier = Modifier.size(40.dp), renderer = checkNotNull(state.renderer),
                        capsule = false, interactive = enabled, deform = enabled,
                        onClick = { center -> state.sourceCenter = center; open() },
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription, Modifier.size(24.dp),
                            tint = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f))
                    }
                }

            }
        }

    }
}

/** 退场动作保留原位淡化; 常驻动作连续移动到新布局位置。 */
@RequiresApi(34)
@Composable
fun CouiTopAppBarSearchAction(
    modifier: Modifier = Modifier,
    state: CouiTopAppBarSearchState,
    visibleWhileSearching: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (visibleWhileSearching) {
        val placement = modifier.animateBounds(
            lookaheadScope = checkNotNull(LocalCouiToolbarOverlay.current),
            boundsTransform = state.motion.actionBoundsTransform,
        )
        Box(placement) { content() }
        return
    }

    val density = LocalDensity.current.density
    val outsets = remember(density) { LayerOutsets(all = (120f / density).dp) }
    val overlay = checkNotNull(LocalCouiToolbarOverlay.current)
    var restingOrigin by remember { mutableStateOf<Offset?>(null) }
    overlay.apply {
        Layout(
            modifier = modifier.then(if (state.motion.expanded) Modifier.clearAndSetSemantics {} else Modifier),
            content = {
                Box(propagateMinConstraints = true) { content() }
            },
        ) { measurables, constraints ->
            val child = measurables.single().measure(constraints.copy(minWidth = 0, minHeight = 0))
            layout(if (state.motion.expanded) 0 else child.width, child.height) {
                var offset = Offset.Zero
                if (!isLookingAhead) coordinates?.let { target ->
                    val root = lookaheadScopeCoordinates
                    if (!state.motion.present || (restingOrigin == null && !state.motion.expanded)) {
                        restingOrigin = root.localPositionOf(target, Offset.Zero)
                    }
                    restingOrigin?.let { offset = target.localPositionOf(root, it) }
                }
                // 在同一次实际放置中抵消操作区移位, 不等全局回调修正首帧。
                if (state.motion.toolbarAlpha > 0f) child.placeWithLayer(0, 0) {
                    translationX = offset.x
                    translationY = offset.y
                    alpha = state.motion.toolbarAlpha
                    scaleX = state.motion.toolbarScale
                    scaleY = state.motion.toolbarScale
                    this.outsets = outsets
                }
            }
        }
    }
}

@RequiresApi(34)
@Composable
private fun SearchSurface(
    modifier: Modifier,
    renderer: CouiFloatingRenderer,
    capsule: Boolean,
    interactive: Boolean,
    deform: Boolean,
    onClick: ((Offset) -> Unit)? = null,
    contentRenderEffect: androidx.compose.ui.graphics.RenderEffect? = null,
    content: @Composable () -> Unit,
) {
    val backdrop = checkNotNull(LocalCouiToolbarBackdrop.current)
    val overlay = checkNotNull(LocalCouiToolbarOverlay.current)
    val style = LocalCouiToolbarStyle.current
    val dark = style.dark
    val contour = style.contourGlowEnabled
    var boundsSize by remember { mutableStateOf(IntSize.Zero) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val shape = style.capsuleShape
    val material = remember(style, capsule) { style.material(capsule) }
    val shadow = remember(style) { style.shadow(true) }
    val contentLayer = rememberGraphicsLayer()
    SideEffect { contentLayer.renderEffect = contentRenderEffect }
    val motion = rememberCouiTouchMotion(deform)
    val press = rememberCouiPressMotion()
    val (lightInput, drawLight) = rememberCouiSpotlight(interactive, SpotlightPreset.OpaqueMedium2, dark)
    val interactions = remember { MutableInteractionSource() }
    CompositionLocalProvider(LocalContentColor provides style.contentColor) {
        Box(
            modifier.onGloballyPositioned { coordinates = it }.onSizeChanged { boundsSize = it }
                .drawWithCache {
                val path = Path().apply { addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache)) }
                onDrawWithContent {
                    drawContent()
                    if (drawLight != null) withTransform({
                            translate(motion.translationX.value, motion.translationY.value)
                            scale(motion.scaleX.value + press.scale.value - 1f, motion.scaleY.value + press.scale.value - 1f, center)
                    }) { clipPath(path) { drawLight.invoke(this) } }
                }
            }
                .couiFloatingSurface(backdrop, renderer, material, shadow, shape, contentLayer, contour,
                foregroundComposition = true) {
                CouiFloatingSurface(
                    bounds = Rect(Offset(motion.translationX.value, motion.translationY.value), Size(boundsSize.width.toFloat(), boundsSize.height.toFloat())),
                    scaleX = motion.scaleX.value + press.scale.value - 1f, scaleY = motion.scaleY.value + press.scale.value - 1f,
                    cornerWeight = 1f,
                )
            },
        ) {
            Box(
                Modifier.matchParentSize().graphicsLayer {
                    translationX = motion.translationX.value; translationY = motion.translationY.value
                    scaleX = motion.scaleX.value + press.scale.value - 1f; scaleY = motion.scaleY.value + press.scale.value - 1f
                }.couiTouchMotion(motion, deform).couiPressMotion(press, deform).then(lightInput)
                    .drawWithContent { contentLayer.record { this@drawWithContent.drawContent() } }
                    .clip(shape)
                    .then(if (onClick != null) Modifier.clickable(
                        interactionSource = interactions, indication = null, enabled = interactive,
                        role = Role.Button, onClick = {
                            val target = checkNotNull(coordinates)
                            onClick(overlay.lookaheadScopeCoordinates(target).localPositionOf(target, Offset(
                                        target.size.width * 0.5f + motion.translationX.value,
                                        target.size.height * 0.5f + motion.translationY.value,
                            )))
                        },
                    ) else Modifier),
                contentAlignment = Alignment.Center,
            ) { ProvideTextStyle(LocalCouiToolbarStyle.current.textStyle) { content() } }
        }
    }
}

private data object SearchRelocationBoundary : ModifierNodeElement<SearchRelocationNode>() {
    override fun create() = SearchRelocationNode()
    override fun update(node: SearchRelocationNode) = Unit
}

private class SearchRelocationNode : Modifier.Node(), BringIntoViewModifierNode {
    override suspend fun bringIntoView(childCoordinates: LayoutCoordinates, boundsProvider: () -> Rect?) {
        if (!childCoordinates.isAttached || boundsProvider() == null) return
        // 输入保留内部滚动; 祖先只需显示固定顶栏, 不能追逐展开时越界的输入坐标而翻页。
        bringIntoView {
            if (isAttached) Rect(Offset.Zero, requireLayoutCoordinates().size.toSize()) else null
        }
    }
}
