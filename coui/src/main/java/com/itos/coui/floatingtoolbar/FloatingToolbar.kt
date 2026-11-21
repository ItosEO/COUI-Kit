package com.itos.coui.floatingtoolbar

import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.LocalCouiBackdrop
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.effect.CouiFloatingSurface
import com.itos.coui.effect.SpotlightPreset
import com.itos.coui.effect.couiFloatingSurface
import com.itos.coui.effect.couiToolbarFeedback
import com.itos.coui.effect.rememberCouiSpotlight
import com.itos.coui.motion.CouiSpring
import com.itos.coui.motion.couiPressMotion
import com.itos.coui.motion.couiTouchMotion
import com.itos.coui.motion.rememberCouiPressMotion
import com.itos.coui.motion.rememberCouiTouchMotion
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.ProvideCouiContent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** 禁用态内容淡化；与顶栏按钮的禁用内容色一致。 */
private const val DisabledContentAlpha = 0.38f

/** 原厂 ItemViewAnimatorImpl 的项弹簧，整条栏共用一条。 */
private const val ToolbarTransitionResponse = 0.5f
private const val ToolbarTransitionBounce = 0.4f

/**
 * 原厂 mProgress 以 10000 为单位，COUISpringAnimation 用默认最小可见变化 1；
 * 这里归一化到 0..1，阈值同为 1/10000。设成 0.5 会让弹簧约 90ms 就判定收敛。
 */
private const val ToolbarSpringMinimumChange = 0.0001f

/** 原厂 ItemViewAnimatorImpl：0–200ms 线性到 0.75，200–400ms 线性回 0。 */
private const val ToolbarGapPeak = 0.75f
private const val ToolbarGapMillis = 200L

/** 原厂 GAP_ANIMATION_MULTIPLIER：退出项在弹簧超调期间的负间距推进系数。 */
private const val ToolbarExitOvershootMultiplier = 5f

private const val PhaseIdle = 0
private const val PhasePending = 1
private const val PhaseRunning = 2

private const val RoleEnter = 1
private const val RoleExit = -1

/**
 * 进出场状态由整条栏持有。原厂收到新项列表后立刻按最终可见项完成 measure/layout，动画期间不再改
 * 节点宽度，只逐帧重排每个图标的 x/scale；可见胶囊边界由首尾图标反算，没有独立的宽度弹簧。
 * 这里让每个槽位和胶囊宽度取自同一组 gap/progress/角色，等价于「首尾图标反算边界」：
 * 槽位之和加两端内边距就是这一帧的胶囊宽度，不存在第二条动画轨。
 */
internal class FloatingToolbarMeasureState {
    /** 间距扩张量 0..0.75。插入时有效间距 = base·(1−gap)，删除时 = base·(1+gap)。 */
    val gap = mutableFloatStateOf(0f)

    /** 200ms 后启动的全栏项弹簧，0→1，带 0.4 回弹。 */
    val progress = CouiSpring(0f, minimumChange = ToolbarSpringMinimumChange)

    var phase by mutableIntStateOf(PhaseIdle)

    /** 净减少操作项时为 true：间距向外扩，否则向内压缩。 */
    var enlarging = false

    var baseSpacingPx = 0f

    /** 两端内边距，1↔2 项切换时随项进度插值，避免瞬间增减 2·INSET。 */
    val edgePx = mutableFloatStateOf(0f)
    var edgeTargetPx = 0f

    var changeToken by mutableIntStateOf(0)
    var kindVersion by mutableIntStateOf(0)

    /** 本批变化里每个项的角色：1 进入、-1 退出、0 保留。 */
    val roles = HashMap<Int, Int>()

    /** 按 Row 顺序登记的显隐；measure 阶段据此判断首尾项与可见项数。 */
    val visibility = linkedMapOf<Int, Boolean>()

    var nextItemId = 0
    var netInsertions = 0

    /**
     * 当前有效项间距，单位 px。
     *
     * 弹簧带回弹时 progress 会越过 1。原厂 `handleItemState` 把退出项未钳制的 `1−progress`
     * （此时为负）返回给 `updateAllAnimatingViews`，后者把它左右两侧的半间距各改成
     * `5·(1−progress)·eff`，即给退出项一份负的水平推进量，胶囊因此先压过头再弹回。
     * 项的视觉 scale 仍钳在 0，所以这里只把这份负推进量摊到整排间距上；进入项本身可以
     * 越过 1，扩展方向的弹性不需要这一项。
     */
    fun spacingPx(): Float {
        val signed = if (enlarging) gap.floatValue else -gap.floatValue
        val base = baseSpacingPx * (1f + signed)
        if (phase != PhaseRunning) return base
        val exits = roles.values.count { it == RoleExit }
        val overshoot = 1f - progress.value
        if (exits == 0 || overshoot >= 0f) return base
        val spacingCount = (visibility.size - 1).coerceAtLeast(1)
        return base + 2f * ToolbarExitOvershootMultiplier * overshoot * base * exits / spacingCount
    }

    /** 显隐变化在 composition 阶段同步登记，measure 的同一帧就能读到正确的项角色。 */
    fun markChange(id: Int, visible: Boolean) {
        roles[id] = if (visible) RoleEnter else RoleExit
        netInsertions += if (visible) 1 else -1
        progress.cancel()
        progress.snapTo(0f)
        gap.floatValue = 0f
        phase = PhasePending
        kindVersion++
        changeToken++
    }
}

internal val LocalFloatingToolbarMeasureState = compositionLocalOf { FloatingToolbarMeasureState() }

/** 线性推进一段 200ms 的间距动画；值是 MutableFloatState，测量与绘制读取后各自失效。 */
private suspend fun tweenGap(state: MutableFloatState, from: Float, to: Float) {
    val start = withFrameNanos { it }
    val duration = ToolbarGapMillis * 1_000_000L
    while (true) {
        val now = withFrameNanos { it }
        val fraction = ((now - start).toFloat() / duration).coerceIn(0f, 1f)
        state.floatValue = from + (to - from) * fraction
        if (fraction >= 1f) return
    }
}

/** 单项这一帧的缩放：保留项恒为静止值，进入项 0→1，退出项 1→0，回弹允许越过 1。 */
private fun itemScale(state: FloatingToolbarMeasureState, id: Int, visible: Boolean): Float {
    // roles 是普通 Map，读一次 kindVersion 才能让本批角色变化触发已测槽位重新测量。
    state.kindVersion
    val idle = if (visible) 1f else 0f
    return when (state.phase) {
        PhasePending -> when (state.roles[id] ?: 0) {
            RoleEnter -> 0f
            RoleExit -> 1f
            else -> idle
        }
        PhaseRunning -> when (state.roles[id] ?: 0) {
            RoleEnter -> state.progress.value.coerceAtLeast(0f)
            RoleExit -> (1f - state.progress.value).coerceAtLeast(0f)
            else -> idle
        }
        else -> idle
    }
}

/**
 * 胶囊形悬浮操作栏，功能对齐 M3/Miuix 的 FloatingToolbar：一排同级操作共用一个玻璃表面。
 * [content] 是完整的 Row 插槽，可放任意数量、任意宽度的同级内容；[FloatingToolbarItem] 只是常用的
 * 可点击图标项，不参与材质绘制。
 *
 * 跟手光、位移回弹和形变由整条胶囊持有：按下时高光出现在触点处并随手指移动，胶囊随拖动一起位移、
 * 拉伸，因此同一栏内各操作共享一个指示区域，不各自叠加高光；装配方式与底栏中间胶囊一致。
 * 操作项增减按原厂两段式：先 200ms 线性调整项间距，再用一条弹簧推进图标缩放；胶囊宽度逐帧等于
 * 各槽位之和加两端内边距，与图标同源，不会另起一条宽度动画。
 * 省略 [renderer] 时创建独立的单主体绘制器，宿主可先创建并处理失败，再显式传入。
 */
@RequiresApi(34)
@Composable
fun FloatingToolbar(
    modifier: Modifier = Modifier,
    backdrop: CouiBackdrop = checkNotNull(LocalCouiBackdrop.current) {
        "悬浮操作栏需要显式 backdrop 或 COUI Scaffold 栏位提供的背景源"
    },
    dark: Boolean = CouiTheme.isDark,
    style: CouiFloatingToolbarStyle = CouiFloatingToolbarDefaults.style(dark),
    renderer: CouiFloatingRenderer = remember { CouiFloatingRenderer.standalone() },
    content: @Composable RowScope.() -> Unit,
) {
    var boundsSize by remember { mutableStateOf(IntSize.Zero) }
    val contentLayer = rememberGraphicsLayer()
    val motion = rememberCouiTouchMotion(true)
    val press = rememberCouiPressMotion()
    val (lightInput, drawLight) = rememberCouiSpotlight(
        interactive = true,
        preset = SpotlightPreset.OpaqueSmall2,
        dark = style.dark,
        color = style.lightColor,
    )
    val measureState = remember { FloatingToolbarMeasureState() }
    val density = LocalDensity.current
    measureState.baseSpacingPx = with(density) { CouiFloatingToolbarDefaults.itemSpacing.toPx() }
    val edgeInsetPx = with(density) { CouiFloatingToolbarDefaults.edgeInset.toPx() }
    // 项只在相邻侧带半个间距，两端补的就是原厂那 6dp（单项时贴边取 0）。
    SideEffect {
        measureState.edgeTargetPx =
            if (measureState.visibility.count { it.value } > 1) edgeInsetPx else 0f
        if (measureState.phase == PhaseIdle) {
            measureState.edgePx.floatValue = measureState.edgeTargetPx
        }
    }

    LaunchedEffect(measureState.changeToken) {
        if (measureState.changeToken == 0) return@LaunchedEffect
        measureState.progress.cancel()
        measureState.progress.snapTo(0f)
        measureState.gap.floatValue = 0f
        measureState.enlarging = measureState.netInsertions < 0
        measureState.netInsertions = 0
        val edgeFrom = measureState.edgePx.floatValue
        val edgeTo = measureState.edgeTargetPx
        measureState.phase = PhasePending
        measureState.kindVersion++

        // 0–200ms：插入把间距压到 0.25 倍，删除扩到 1.75 倍；图标保持原状。
        tweenGap(measureState.gap, 0f, ToolbarGapPeak)
        // 200ms：项弹簧启动，与间距回程并行；必须等两条都收敛才回到静止态。
        measureState.phase = PhaseRunning
        var springDone = false
        var gapDone = false
        fun finish() {
            if (!springDone || !gapDone) return
            measureState.phase = PhaseIdle
            measureState.roles.clear()
            measureState.gap.floatValue = 0f
            measureState.edgePx.floatValue = measureState.edgeTargetPx
            measureState.kindVersion++
        }
        launch {
            tweenGap(measureState.gap, ToolbarGapPeak, 0f)
            gapDone = true
            finish()
        }
        measureState.progress.animateTo(
            1f,
            ToolbarTransitionResponse,
            ToolbarTransitionBounce,
            onFrame = { p ->
                measureState.edgePx.floatValue =
                    edgeFrom + (edgeTo - edgeFrom) * p.coerceIn(0f, 1f)
            },
            onEnd = {
                springDone = true
                finish()
            },
        )
    }
    DisposableEffect(measureState.progress) { onDispose { measureState.progress.cancelComplete() } }

    Box(
        modifier
            .height(CouiFloatingToolbarDefaults.height)
            .onSizeChanged { boundsSize = it }
            .couiFloatingSurface(
                backdrop = backdrop,
                renderer = renderer,
                material = style.material,
                shadow = style.shadow,
                shape = style.shape,
                contentLayer = contentLayer,
                contourGlowEnabled = style.contourGlowEnabled,
            ) {
                CouiFloatingSurface(
                    bounds = Rect(Offset.Zero, Size(boundsSize.width.toFloat(), boundsSize.height.toFloat()))
                        .translate(Offset(motion.translationX.value, motion.translationY.value)),
                    scaleX = motion.scaleX.value + press.scale.value - 1f,
                    scaleY = motion.scaleY.value + press.scale.value - 1f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalCouiFloatingToolbarStyle provides style,
            LocalFloatingToolbarMeasureState provides measureState,
        ) {
            Row(
                Modifier.fillMaxHeight()
                    .layout { measurable, constraints ->
                        // 胶囊宽度 = 这一帧各槽位之和 + 两端内边距；gap 与角色失效时同步重测。
                        measureState.kindVersion
                        measureState.gap.floatValue
                        val placeable = measurable.measure(constraints)
                        val edge = measureState.edgePx.floatValue.roundToInt()
                        layout(placeable.width + edge * 2, placeable.height) { placeable.place(edge, 0) }
                    }
                    // 内容与材质读同一组位移与按压弹簧，避免缩放时图标和玻璃错位。
                    .graphicsLayer {
                        translationX = motion.translationX.value
                        translationY = motion.translationY.value
                        scaleX = motion.scaleX.value + press.scale.value - 1f
                        scaleY = motion.scaleY.value + press.scale.value - 1f
                    }
                    .then(
                        Modifier
                            // 拖动由操作栏接管，纵向滚动不会带着胶囊一起走。
                            .couiTouchMotion(motion, true, cancelOnConsumedMove = false, consumeDrag = true)
                            .couiPressMotion(press, true)
                            .then(lightInput),
                    )
                    .drawWithContent { contentLayer.record { this@drawWithContent.drawContent() } }
                    .clip(style.shape)
                    // 高光在操作内容之下、整条胶囊之内，图标按自身配色保持可见。
                    .drawBehind { drawLight?.invoke(this) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/**
 * 操作栏中的单个可点击项。点击、悬停、焦点语义与键盘焦点由本节点持有。
 * 最小宽度为 [CouiFloatingToolbarDefaults.itemSize]，宿主可用 `Modifier.width` 加宽。
 * 本节点不绘制表面：跟手光由 [FloatingToolbar] 整条提供；按压只在原厂蒙版矩形范围内加一层反馈，
 * 与整条栏的收缩、位移和形变并行，不改变胶囊轮廓。
 * 内容色来自操作栏样式（默认 `onSurface` 的 80%），图标自带 `tint` 时以图标为准。
 *
 * [visible] 为 false 时按原厂两段式退出：先 200ms 调整项间距，再由全栏共用的弹簧把槽位、透明度和
 * 缩放一起收到 0；重新置 true 反向淡入。宿主保留操作项、只切换 [visible] 即可，不需要自己包
 * `AnimatedVisibility` 或维护占位。
 */
@Composable
fun FloatingToolbarItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    val style = LocalCouiFloatingToolbarStyle.current
    val measureState = LocalFloatingToolbarMeasureState.current
    val interactionSource = remember { MutableInteractionSource() }
    val id = remember { measureState.nextItemId++ }
    val tracked = remember { booleanArrayOf(visible, false) }
    // composition 阶段登记显隐：父节点的 apply/measure 在同帧就能拿到正确的首尾项与可见项数。
    measureState.visibility[id] = visible
    if (!tracked[1]) {
        tracked[1] = true
    } else if (tracked[0] != visible) {
        tracked[0] = visible
        measureState.markChange(id, visible)
    }
    DisposableEffect(Unit) {
        onDispose {
            measureState.visibility.remove(id)
            measureState.roles.remove(id)
            measureState.kindVersion++
        }
    }
    Box(
        modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val scale = itemScale(measureState, id, visible)
                // 原厂 updateAllAnimatingViews：每项占 scale·width，两侧各 scale·有效间距/2；
                // 首尾项不留外侧半间距，背景边界因此随图标连续收放。
                val half = scale * measureState.spacingPx() * 0.5f
                val first = id == measureState.visibility.keys.firstOrNull()
                val last = id == measureState.visibility.keys.lastOrNull()
                val leftHalf = if (first) 0f else half
                val rightHalf = if (last) 0f else half
                val slot = (leftHalf + scale * placeable.width + rightHalf).roundToInt().coerceAtLeast(0)
                layout(slot, placeable.height) {
                    // 内容仍按原尺寸放置，缩放由内层 graphicsLayer 围绕中心完成；
                    // 抵消 (1−scale)·width/2 后，绘制出的可见左边缘正好落在 leftHalf。
                    placeable.place((leftHalf - (1f - scale) * placeable.width * 0.5f).roundToInt(), 0)
                }
            }
            // 原厂操作项就是 44×40，不做 48dp 最小点击区扩张，否则胶囊会比参考宽 4dp。
            .height(CouiFloatingToolbarDefaults.itemHeight)
            .widthIn(min = CouiFloatingToolbarDefaults.itemSize)
            .graphicsLayer { alpha = itemScale(measureState, id, visible).coerceIn(0f, 1f) }
            .couiToolbarFeedback(
                style.dark, interactionSource, enabled && visible,
                pressFeedback = true,
                // `applyBackgroundMaskRect`：多项时左右各外扩一个项间距，单项时内缩 2dp。
                maskInset = if (measureState.edgeTargetPx > 0f) {
                    -CouiFloatingToolbarDefaults.itemSpacing
                } else {
                    CouiFloatingToolbarDefaults.singleItemFeedbackInset
                },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && visible,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // 禁用状态只淡化内容，不改变宿主或图标自带的颜色。
        Box(
            Modifier
                .then(if (enabled) Modifier else Modifier.alpha(DisabledContentAlpha))
                .graphicsLayer {
                    val scale = itemScale(measureState, id, visible)
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.Center,
        ) {
            if (style.contentColor.isSpecified) {
                ProvideCouiContent(color = style.contentColor) { content() }
            } else {
                content()
            }
        }
    }
}
