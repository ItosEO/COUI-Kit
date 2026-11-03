package com.itos.coui.demo

import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiMetaballRenderer
import com.itos.coui.motion.rememberCouiMetaballFollowingTranslation
import com.itos.coui.swipereveal.CouiSwipeRevealDefaults
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private val RecordSwipeActionSpacing = 2.dp
private val RecordDeleteActionSize = 44.dp
private val RecordDeleteCommitStretch = 54.dp

/** 侧滑手势与位移由 Demo 装配，不进入 COUI 库。 */
@RequiresApi(33)
@Composable
internal fun DemoRecordSwipeRow(
    modifier: Modifier = Modifier,
    backgroundRenderer: CouiMetaballRenderer,
    maskRenderer: CouiMetaballRenderer,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    minHeight: Dp = RecordDeleteActionSize,
    actionSpacing: Dp = RecordSwipeActionSpacing,
    onCommitDelete: () -> Unit,
    onDeleteAnimationStart: () -> Unit,
    endActionContent: @Composable BoxScope.(Boolean, Boolean, () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val maxVelocityPx = with(density) { CouiSwipeRevealDefaults.itemMaxVelocity.toPx() }
    val minHeightPx = with(density) { minHeight.toPx() }
    var contentHeightPx by remember(minHeight) { mutableFloatStateOf(minHeightPx) }
    val targetRevealPx = max(minHeightPx, contentHeightPx)
    val actionSpacingPx = with(density) { actionSpacing.toPx() }
    val deleteActionSize = CouiSwipeRevealDefaults.fusedActionSize
    val deleteActionSizePx = with(density) { deleteActionSize.toPx() }
    val openReferencePx = targetRevealPx + actionSpacingPx
    // 间距仍以原 44.dp 按钮为基准, 避免缩小按钮后把腾出的宽度再次计入间距。
    val openReferenceGapPx = (openReferencePx - with(density) { RecordDeleteActionSize.toPx() })
        .coerceAtLeast(0f)
    val openRestPx = deleteActionSizePx + max(
        openReferenceGapPx, with(density) { CouiSwipeRevealDefaults.menuMinimumGap.toPx() },
    )
    val openThresholdPx = with(density) { CouiSwipeRevealDefaults.openTriggerDistance.toPx() }
    val deleteCommitPx = openRestPx + with(density) { RecordDeleteCommitStretch.toPx() }
    val rightSwipeLimitPx = openReferencePx * 2f
    val offsetPx = remember { Animatable(0f) }
    val deleteAlpha = remember { Animatable(1f) }
    var isDragging by remember { mutableStateOf(false) }
    var deleteAnimating by remember { mutableStateOf(false) }
    var deleteConfirmationRequested by remember { mutableStateOf(false) }
    var deleteEndPx by remember { mutableFloatStateOf(0f) }
    var dragStartOffsetPx by remember { mutableFloatStateOf(0f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var motionJob by remember { mutableStateOf<Job?>(null) }
    var settlingTargetPx by remember { mutableStateOf<Float?>(null) }
    var deleteHapticPerformed by remember { mutableStateOf(false) }
    val latestIsOpen by rememberUpdatedState(isOpen)
    val latestOnOpenChange by rememberUpdatedState(onOpenChange)
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnCommitDelete by rememberUpdatedState(onCommitDelete)
    val latestOnDeleteAnimationStart by rememberUpdatedState(onDeleteAnimationStart)
    val showActionsLabel = stringResource(R.string.record_open_actions)

    fun settleTo(target: Float, velocity: Float = offsetPx.velocity, dragEndOffset: Float? = null) {
        val previousMotion = motionJob
        // 先记录目标, 防止展开状态同步重复启动动画并丢失松手速度。
        settlingTargetPx = target
        motionJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            previousMotion?.cancelAndJoin()
            if (dragEndOffset != null) offsetPx.snapTo(dragEndOffset)
            offsetPx.animateTo(target, CouiSwipeRevealDefaults.itemSettleSpec, initialVelocity = velocity)
        }
    }

    val runDeleteAnimation = {
        if (!deleteAnimating) {
            deleteConfirmationRequested = false
            latestOnOpenChange(false)
            latestOnDeleteAnimationStart()
            deleteAnimating = true
            val previousMotion = motionJob
            settlingTargetPx = null
            scope.launch {
                previousMotion?.cancelAndJoin()
                val moveJob = launch {
                    offsetPx.animateTo(
                        targetValue = -deleteEndPx,
                        animationSpec = tween(
                            durationMillis = 240,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
                val fadeJob = launch {
                    deleteAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = 220,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
                moveJob.join()
                fadeJob.join()
                latestOnCommitDelete()
            }
        }
    }

    val requestDelete = {
        if (!deleteAnimating) {
            if (deleteConfirmationRequested) {
                runDeleteAnimation()
            } else {
                deleteConfirmationRequested = true
            }
        }
    }

    LaunchedEffect(isOpen, openRestPx) {
        if (!isOpen && !deleteAnimating) {
            deleteConfirmationRequested = false
        }
        if (isDragging || deleteAnimating) return@LaunchedEffect
        val target = if (isOpen) -openRestPx else 0f
        if (settlingTargetPx != target || (motionJob?.isActive != true && offsetPx.value != target)) {
            settleTo(target)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .pointerInput(openRestPx, rightSwipeLimitPx, deleteCommitPx, layoutDirection, maxVelocityPx) {
                val velocityTracker = VelocityTracker()
                var lastPointerPosition = Offset.Zero
                var hasVelocitySample = false
                val direction = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (deleteAnimating) return@detectHorizontalDragGestures
                        isDragging = true
                        dragStartOffsetPx = offsetPx.value
                        dragOffsetPx = offsetPx.value
                        settlingTargetPx = null
                        velocityTracker.resetTracking()
                        hasVelocitySample = false
                        val previousMotion = motionJob
                        motionJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            previousMotion?.cancelAndJoin()
                            offsetPx.stop()
                        }
                        latestOnOpenChange(true)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        if (deleteAnimating || !isDragging) return@detectHorizontalDragGestures
                        change.consume()
                        if (!hasVelocitySample) {
                            velocityTracker.addPosition(change.previousUptimeMillis, change.previousPosition)
                            hasVelocitySample = true
                        }
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        lastPointerPosition = change.position
                        val next = (dragOffsetPx + dragAmount * direction).coerceAtMost(rightSwipeLimitPx)
                        dragOffsetPx = next
                        if (next < 0f && !latestIsOpen) latestOnOpenChange(true)
                        val deleteCommitReached = -next >= deleteCommitPx
                        if (deleteCommitReached && !deleteHapticPerformed) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            deleteHapticPerformed = true
                        } else if (!deleteCommitReached) {
                            deleteHapticPerformed = false
                        }
                        val previousMotion = motionJob
                        motionJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            previousMotion?.cancelAndJoin()
                            offsetPx.snapTo(next)
                        }
                    },
                    onDragEnd = {
                        if (deleteAnimating || !isDragging) return@detectHorizontalDragGestures
                        isDragging = false
                        val velocity = if (hasVelocitySample) {
                            // 检测器不提供抬手事件, 补入静止的末位置以计入停顿时间。
                            velocityTracker.addPosition(SystemClock.uptimeMillis(), lastPointerPosition)
                            velocityTracker.calculateVelocity().x.coerceIn(-maxVelocityPx, maxVelocityPx) * direction
                        } else {
                            0f
                        }
                        if (dragOffsetPx > 0f) {
                            settleTo(0f, velocity, dragOffsetPx)
                            latestOnOpenChange(false)
                            return@detectHorizontalDragGestures
                        }
                        val currentRevealPx = -dragOffsetPx
                        if (currentRevealPx >= deleteCommitPx) {
                            runDeleteAnimation()
                            return@detectHorizontalDragGestures
                        }
                        val pulledBackFromOpen = dragStartOffsetPx < -openThresholdPx &&
                            dragOffsetPx - dragStartOffsetPx >= openThresholdPx
                        val target = if (pulledBackFromOpen) {
                            0f
                        } else if (currentRevealPx >= openThresholdPx) {
                            -openRestPx
                        } else {
                            0f
                        }
                        settleTo(target, velocity, dragOffsetPx)
                        latestOnOpenChange(target != 0f)
                    },
                    onDragCancel = {
                        if (deleteAnimating || !isDragging) return@detectHorizontalDragGestures
                        isDragging = false
                        val target = if (latestIsOpen) -openRestPx else 0f
                        settleTo(target, velocity = 0f, dragEndOffset = dragOffsetPx)
                        latestOnOpenChange(target != 0f)
                    },
                )
            }
    ) {
        val fullContentWidth = maxWidth
        val fullWidthPx = with(density) { fullContentWidth.toPx() }
        LaunchedEffect(fullWidthPx, deleteActionSizePx) {
            deleteEndPx = fullWidthPx + deleteActionSizePx
        }
        val leftShrinkPx = (-offsetPx.value).coerceAtLeast(0f)
        val rightShrinkPx = offsetPx.value.coerceAtLeast(0f)
        val deleteCommitted = leftShrinkPx >= deleteCommitPx
        // SystemUI Stage1 按按钮宽度加跟随距离计算进度, 显示起点提前至 50%。
        val followingDistancePx = with(density) { CouiSwipeRevealDefaults.menuFollowingDistance.roundToPx().toFloat() }
        val progress = leftShrinkPx / (deleteActionSizePx + followingDistancePx)
        val iconRevealProgress = ((progress - CouiSwipeRevealDefaults.fusedRevealStart) /
            (1f - CouiSwipeRevealDefaults.fusedRevealStart)).coerceIn(0f, 1f)
        val iconOpacity = iconRevealProgress
        val deleteActionAlpha = if (leftShrinkPx > 0f) deleteAlpha.value else 0f
        val deleteIconAlpha = deleteActionAlpha * iconOpacity
        val deleteRevealBlur = with(density) {
            (CouiSwipeRevealDefaults.fusedRevealMaxBlurPx * (1f - iconRevealProgress)).toDp()
        }
        val visibleShrinkPx = max(leftShrinkPx, rightShrinkPx)
        val visibleShrinkDp = with(density) { visibleShrinkPx.toDp() }
        val visibleCardWidth = (fullContentWidth - visibleShrinkDp)
            .coerceAtLeast(0.dp)
        val deleteStretchPx = (leftShrinkPx - openRestPx).coerceAtLeast(0f)
        val deleteActionWidth = deleteActionSize + with(density) { deleteStretchPx.toDp() }
        val fullContentWidthPx = with(density) { fullContentWidth.roundToPx() }
        val menuTranslation by rememberCouiMetaballFollowingTranslation(
            distancePx = { (-offsetPx.value).coerceAtLeast(0f) },
            subjectSizePx = deleteActionSizePx,
            followingDistancePx = followingDistancePx,
        )
        val fusionActive = leftShrinkPx > 0f && leftShrinkPx < openRestPx && !deleteAnimating
        DemoRecordMetaballBackground(
            modifier = Modifier.matchParentSize(),
            renderer = backgroundRenderer,
            visibleCardWidth = visibleCardWidth,
            deleteActionWidth = deleteActionWidth,
            deleteActionSizePx = deleteActionSizePx,
            alignCardEnd = rightShrinkPx > 0f,
            menuTranslation = menuTranslation,
            fusionActive = fusionActive,
            deleteActionAlpha = deleteActionAlpha,
        )
        val deleteActionModifier = Modifier
            .align(Alignment.CenterEnd)
            .graphicsLayer {
                translationX = if (layoutDirection == LayoutDirection.Rtl) -menuTranslation else menuTranslation
            }
            .width(deleteActionWidth)
            .height(deleteActionSize)
            .drawWithContent {
                // 背景已移入融合层, 图标仍只在卡片让出的操作区显示。
                val hiddenWidth = (size.width - leftShrinkPx - menuTranslation).coerceIn(0f, size.width)
                clipRect(
                    left = if (layoutDirection == LayoutDirection.Ltr) hiddenWidth else 0f,
                    right = if (layoutDirection == LayoutDirection.Rtl) size.width - hiddenWidth else size.width,
                ) {
                    this@drawWithContent.drawContent()
                }
            }
            .clip(DemoShapes.record)
            .then(if (leftShrinkPx > 0f) Modifier.blur(radius = deleteRevealBlur) else Modifier)

        Box(
            modifier = deleteActionModifier,
        ) {
            if (deleteIconAlpha > 0f || deleteCommitted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(deleteIconAlpha)
                ) {
                    endActionContent(
                        deleteCommitted,
                        deleteConfirmationRequested,
                        requestDelete,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(if (rightShrinkPx > 0f) Alignment.CenterEnd else Alignment.CenterStart)
                .width(visibleCardWidth)
                .then(
                    demoRecordMetaballContentMask(
                        renderer = maskRenderer,
                        fullWidthPx = fullWidthPx,
                        minHeightPx = minHeightPx,
                        visibleCardWidth = visibleCardWidth,
                        deleteActionWidth = deleteActionWidth,
                        deleteActionSizePx = deleteActionSizePx,
                        alignCardEnd = rightShrinkPx > 0f,
                        menuTranslation = menuTranslation,
                        fusionActive = fusionActive,
                        deleteActionAlpha = deleteActionAlpha,
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !deleteAnimating,
                    onClickLabel = if (isOpen) stringResource(R.string.record_close_actions)
                        else stringResource(R.string.record_view_details),
                    onClick = {
                        if (latestIsOpen) latestOnOpenChange(false) else latestOnClick()
                    },
                )
                .semantics {
                    if (!isOpen && !deleteAnimating) customActions = listOf(
                        CustomAccessibilityAction(showActionsLabel) { latestOnOpenChange(true); true },
                    )
                }
                .onSizeChanged { size ->
                    contentHeightPx = max(minHeightPx, size.height.toFloat())
                }
        ) {
            ShiftedFixedWidthContent(
                fullWidthPx = fullContentWidthPx,
                shiftPx = if (rightShrinkPx > 0f) {
                    0
                } else {
                    -leftShrinkPx.roundToInt()
                },
                content = content,
            )
        }
    }
}

@Composable
private fun ShiftedFixedWidthContent(
    fullWidthPx: Int,
    shiftPx: Int,
    content: @Composable () -> Unit,
) {
    Layout(content = content) { measurables, constraints ->
        val contentConstraints = Constraints(
            minWidth = fullWidthPx,
            maxWidth = fullWidthPx,
            minHeight = 0,
            maxHeight = constraints.maxHeight,
        )
        val placeable = measurables.first().measure(contentConstraints)
        layout(width = constraints.maxWidth, height = placeable.height) {
            placeable.placeRelative(x = shiftPx, y = 0)
        }
    }
}
