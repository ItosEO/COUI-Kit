package com.itos.coui.sheet

import android.graphics.drawable.ColorDrawable
import android.graphics.BlendMode
import android.os.Build
import android.view.WindowManager
import android.view.WindowInsetsController
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.itos.coui.backdrop.CouiBackdrop
import com.itos.coui.backdrop.rememberCouiBackdrop
import com.itos.coui.dialog.CouiDialogColors
import com.itos.coui.dialog.CouiDialogDefaults
import com.itos.coui.dialog.CouiDialogMaterial
import com.itos.coui.effect.CouiMaterialBlendMode
import com.itos.coui.effect.CouiFloatingMaterial
import com.itos.coui.menu.LocalCouiMenuBackdrop
import com.itos.coui.menu.LocalCouiMenuVisibleBounds
import com.itos.coui.menu.CouiMenuVisibleBounds
import com.itos.coui.menu.ProvideCouiMenuBackdrop
import com.itos.coui.input.isCouiTouchCancelled
import com.itos.coui.motion.CouiVelocityTracker
import com.itos.coui.theme.CouiTheme
import com.itos.coui.theme.LocalCouiDarkTheme
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.abs

private val SheetEase = CubicBezierEasing(.33f, 0f, .67f, 1f)

object CouiSheetDefaults {
    val cornerRadius = 32.dp
    val maxWidth = 540.dp
    val topSpacing = 40.dp
    val handleHeight = 16.dp
    val bottomPadding = 60.dp
    internal val maxHeight = 2000.dp
    internal const val edgeWeight = 3f
    internal fun innerGlowAlpha(dark: Boolean) = if (dark) .1f else .4f
    internal fun lineAlpha(dark: Boolean) = if (dark) .15f else .8f
    internal fun lineWidth(dark: Boolean) = if (dark) 2.dp else 4.dp
    val toolbarTopSpacing = 11.dp
    val toolbarHeight = 50.dp
    val toolbarHorizontalPadding = 16.dp
    internal val toolbarTitleGap = 4.dp
    internal val toolbarTitleVerticalPadding = 12.dp
    internal val toolbarTitleMinHeight = 26.dp
    fun colors(dark: Boolean): CouiDialogColors = CouiDialogDefaults.colors(dark).copy(
        surface = Color(if (dark) 0xFF383838 else 0xFFFFFFFF))

    /** Clock 17 的 bottomdialog 资源，与警告窗口共用模糊/混色后端。 */
    fun material(dark: Boolean) = CouiDialogMaterial(
        mix = Color(if (dark) 0xA4515151 else 0xDCC8C8C8),
        blend = Color(if (dark) 0xB30F0F0F else 0x376E6E6E),
        blurRadius = 400,
        blendMode = if (dark) CouiMaterialBlendMode.Overlay else CouiMaterialBlendMode.ColorDodge,
    )

    /** Panel 内工具栏走 Drawable 材质，使用 CAPSULE_8 / CIRCLE_4 的窗口内轮廓。 */
    fun toolbarMaterial(dark: Boolean, capsule: Boolean) = CouiFloatingMaterial(
        firstColor = Color(if (dark) 0xFF333333 else 0xFFF0F0F0),
        firstBlendMode = BlendMode.SRC_OVER,
        secondColor = Color.Transparent,
        secondBlendMode = BlendMode.SRC_OVER,
        blurRadius = 0f, strokeType = if (capsule) 1 else 2,
        edgeAlpha = if (dark) .15f else .8f, edgeWidthDp = 1.8f,
        innerGlowAlpha = if (dark) .01f else .1f,
        innerGlowScale = 1f,
    )
}

/**
 * COUI 底部面板，提供一个展开位置及隐藏位置。visible 由宿主持有，必须持续调用本组件以完成退场。
 * sheetHeight 为包含 handle、内容及底部安全区的可见目标高度；null 按内容测量。
 * 普通手机窗口顶部使用状态栏高度，无状态栏时保留 40dp；屏外绘制余量不占内容高度。
 * 内容自行选择一个滚动容器；nested scroll 在内容到顶后接管向下拖动。顶部 handle 可直接拖动。
 * title / dismissDescription 由宿主资源提供；确认、草稿、页面导航和二级面板属于宿主。
 * 可选材质复用弹窗后端，要求 API 33 及不含面板/遮罩的窗口源。
 */
@Composable
fun ModalBottomSheet(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    dismissDescription: String,
    dark: Boolean = CouiTheme.isDark,
    sheetHeight: Dp? = null,
    draggable: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    colors: CouiDialogColors = CouiDialogDefaults.themeColors(dark),
    material: CouiDialogMaterial? = null,
    backdrop: CouiBackdrop? = null,
    showDragHandle: Boolean = true,
    isolateContent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    require(sheetHeight == null || (sheetHeight.value.isFinite() && sheetHeight > 0.dp)) { "面板高度必须为有限正值" }
    val windowBackdrop = backdrop ?: LocalCouiMenuBackdrop.current
    if (material != null) {
        check(Build.VERSION.SDK_INT >= 33) { "面板材质要求 Android 13 或更高版本" }
        checkNotNull(windowBackdrop) { "面板材质需要不包含面板与遮罩的宿主窗口源" }
    }
    var mounted by remember { mutableStateOf(visible) }
    if (visible && !mounted) SideEffect { mounted = true }
    if (!visible && !mounted) return
    val requestDismiss by rememberUpdatedState(onDismissRequest)
    val currentVisible by rememberUpdatedState(visible)
    val motion = remember { CouiSheetMotion() }
    SideEffect { motion.onHidden = { if (!currentVisible) mounted = false } }
    DisposableEffect(motion) { onDispose { motion.dispose() } }
    Dialog(onDismissRequest = { if (visible) requestDismiss() }, properties = DialogProperties(
        dismissOnClickOutside = false, usePlatformDefaultWidth = false, decorFitsSystemWindows = false, windowTitle = title,
    )) {
        val view = LocalView.current
        SideEffect {
            val window = checkNotNull((view.parent as? DialogWindowProvider)?.window)
            window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND or WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.setWindowAnimations(0)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            window.isNavigationBarContrastEnforced = false
            val barAppearance = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            window.insetsController?.setSystemBarsAppearance(if (dark) 0 else barAppearance, barAppearance)
        }
        val density = LocalDensity.current
        val surfaceBackdrop = rememberCouiBackdrop(parent = windowBackdrop)
        var panelHeight by remember { mutableStateOf<Float?>(null) }
        var panelCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null, neverEqualPolicy()) }
        var viewportCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null, neverEqualPolicy()) }
        var geometryReady by remember { mutableStateOf(false) }
        val menuBounds = remember(view, density, motion) {
            CouiMenuVisibleBounds(view) {
                val panel = panelCoordinates?.takeIf { it.isAttached }
                val viewport = viewportCoordinates?.takeIf { it.isAttached }?.boundsInWindow()
                if (panel == null || viewport == null) null else if (motion.height == 0f) {
                    // 首次隐藏帧与零高度时，表面移到窗口之外，菜单也必须保持空区域。
                    val left = viewport.left.roundToInt()
                    val bottom = viewport.bottom.roundToInt()
                    IntRect(left, bottom, left, bottom)
                } else {
                    // 完整 frame 先随面板位移，再裁到真实父视口；不能先裁静态 H 再平移。
                    val width = panel.size.width.toFloat()
                    val top = motion.offset
                    val bottom = top + panel.size.height + with(density) { CouiSheetDefaults.bottomPadding.roundToPx() }
                    val topLeft = panel.localToWindow(Offset(0f, top))
                    val topRight = panel.localToWindow(Offset(width, top))
                    val bottomLeft = panel.localToWindow(Offset(0f, bottom))
                    val bottomRight = panel.localToWindow(Offset(width, bottom))
                    val left = minOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
                        .coerceIn(viewport.left, viewport.right)
                    val right = maxOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x)
                        .coerceIn(left, viewport.right)
                    val visibleTop = minOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)
                        .coerceIn(viewport.top, viewport.bottom)
                    val visibleBottom = maxOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y)
                        .coerceIn(visibleTop, viewport.bottom)
                    val inset = with(density) { 16.dp.toPx() }
                        .coerceAtMost((right - left - 1f).coerceAtLeast(0f) / 2f)
                    IntRect((left + inset).roundToInt(), visibleTop.roundToInt(),
                        (right - inset).roundToInt(), visibleBottom.roundToInt())
                }
            }
        }
        val statusInset = WindowInsets.statusBars.getTop(density)
        val topSpacing = if (LocalConfiguration.current.smallestScreenWidthDp < 600 && statusInset > 0) {
            with(density) { statusInset.toDp() }
        } else CouiSheetDefaults.topSpacing
        val gestureNavigation = WindowInsets.tappableElement.getBottom(density) == 0
        val peek = with(density) { (if (gestureNavigation) 82.dp else 110.dp).toPx() }
        val imeVisible = WindowInsets.ime.getBottom(density) > 0
        val parentInsets = if (gestureNavigation) WindowInsets.ime.only(WindowInsetsSides.Bottom)
            else WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
        val canDrag = draggable && visible && motion.entered
        val canDragNow by rememberUpdatedState(canDrag)
        var touchActive by remember { mutableStateOf(false) }
        LaunchedEffect(canDrag) { if (!canDrag) motion.cancelDrag() }
        val nested = remember(motion) { object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (canDragNow && touchActive && source == NestedScrollSource.UserInput && available.y < 0f &&
                    (motion.offset > 0f || motion.hasNestedDrag))
                    Offset(0f, motion.drag(available.y, nested = true)) else Offset.Zero
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (canDragNow && touchActive && source == NestedScrollSource.UserInput && available.y > 0f)
                    Offset(0f, motion.drag(available.y, nested = true)) else Offset.Zero
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!canDragNow || !motion.hasNestedDrag || motion.offset <= 0f) return Velocity.Zero
                if (motion.releaseNested(available.y)) requestDismiss()
                return Velocity(0f, available.y)
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (canDragNow && motion.releaseNested(available.y)) requestDismiss()
                return Velocity.Zero
            }
        } }
        // 自身表面仍采样外层窗口；新窗口源只供内容里的后续 Popup/Dialog 使用。
        CompositionLocalProvider(LocalCouiMenuBackdrop provides windowBackdrop) {
            ProvideCouiMenuBackdrop(isolateContent = isolateContent) { source ->
                BoxWithConstraints(Modifier.fillMaxSize().then(source)) {
                    val windowHeight = constraints.maxHeight.toFloat()
                    val outside = remember { MutableInteractionSource() }
                    val dismissOnScrim = if (visible && motion.entered && dismissOnClickOutside) {
                        Modifier.clickable(outside, null, onClickLabel = dismissDescription) { requestDismiss() }
                            .semantics { contentDescription = dismissDescription }
                    } else Modifier
                    Box(Modifier.fillMaxSize().graphicsLayer { alpha = motion.dim }
                        .background(Color.Black.copy(alpha = colors.dimAmount)).then(dismissOnScrim))
                    // 三键导航在父容器避让，手势导航在内容留白；IME 与导航取并集并由 Insets 统一消费。
                    BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(parentInsets)
                        .padding(top = topSpacing).consumeWindowInsets(PaddingValues(top = topSpacing)).clipToBounds()
                        .onGloballyPositioned { viewportCoordinates = it }) {
                        val parentHeight = constraints.maxHeight.toFloat()
                        val visibleFrame = remember { android.graphics.Rect() }
                        view.getWindowVisibleDisplayFrame(visibleFrame)
                        // 原厂只在可见窗口超过 maxPanelHeight 时约束完整 frame（包含屏外 60dp）。
                        val maximumFrameHeight = with(density) { CouiSheetDefaults.maxHeight.toPx().toInt() }
                        val limit = if (visibleFrame.height() > maximumFrameHeight) {
                            minOf(maxHeight, with(density) {
                                (maximumFrameHeight - CouiSheetDefaults.bottomPadding.roundToPx()).toDp()
                            })
                        } else maxHeight
                        val imeHeight = if (imeVisible) {
                            (windowHeight - with(density) { topSpacing.toPx() } - parentHeight).coerceAtLeast(0f)
                        } else 0f
                        // 在组合阶段订阅测量结果，首轮 onSizeChanged 后必须重新发布运动目标。
                        val measuredHeight = panelHeight
                        LaunchedEffect(visible, measuredHeight, parentHeight, windowHeight, imeHeight, peek) {
                            if (measuredHeight == null) return@LaunchedEffect
                            // 首次窗口测量后先提交隐藏帧，窗口创建不占用入场弹簧的时间。
                            if (!geometryReady && measuredHeight > 0f) { withFrameNanos { }; geometryReady = true }
                            motion.update(visible, measuredHeight, parentHeight, windowHeight, imeHeight, peek)
                        }
                        val heightModifier = if (sheetHeight != null) Modifier.height(sheetHeight.coerceAtMost(limit)) else Modifier.heightIn(max = limit)
                        // motion 与菜单使用可见高度；Surface 单独保留原厂屏外绘制余量。
                        Box(modifier.align(Alignment.BottomCenter).widthIn(max = CouiSheetDefaults.maxWidth).fillMaxWidth()
                            .then(heightModifier).onSizeChanged {
                                panelHeight = it.height.toFloat()
                            }
                            .onGloballyPositioned { panelCoordinates = it }) {
                            Box(Modifier.fillMaxWidth().graphicsLayer {
                                    translationY = if (motion.height == 0f) windowHeight else motion.offset
                                }
                                .nestedScroll(nested).sheetDrag(motion, canDrag && !showDragHandle) { requestDismiss() }.pointerInput(motion) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                        // UserInput 也包含键盘、滚轮和无障碍滚动；这些输入没有触摸释放阶段。
                                        touchActive = true
                                        var released = false
                                        try {
                                            while (true) {
                                                // 系统取消先于子滚动容器的释放处理，避免将 CANCEL 当作向下抬手。
                                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                                if (event.isCouiTouchCancelled()) break
                                                if (event.changes.none { it.pressed }) { released = true; break }
                                            }
                                        } finally {
                                            touchActive = false
                                            if (!released) motion.cancelDrag()
                                        }
                                    }
                                }.semantics {
                                    paneTitle = title
                                    if (visible) dismiss { requestDismiss(); true }
                                }) {
                                CouiSheetSurface(Modifier.fillMaxWidth(), material, windowBackdrop, colors, dark,
                                    surfaceBackdrop = surfaceBackdrop,
                                    sampleCoordinates = { panelCoordinates }, sampleTranslation = { Offset(0f, motion.offset) }) {
                                    CompositionLocalProvider(LocalCouiDarkTheme provides dark,
                                        LocalCouiMenuVisibleBounds provides menuBounds,
                                        LocalCouiSheetSurfaceBackdrop provides surfaceBackdrop) {
                                        Column(Modifier.fillMaxWidth().then(if (sheetHeight != null) Modifier.fillMaxHeight() else Modifier)
                                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))) {
                                            if (showDragHandle) {
                                                var handlePressed by remember { mutableStateOf(false) }
                                                val handlePressAlpha by animateFloatAsState(
                                                    targetValue = if (handlePressed) 1f else 0f,
                                                    animationSpec = tween(200, easing = SheetEase),
                                                    label = "拖手柄按压",
                                                )
                                                Canvas(Modifier.fillMaxWidth().height(CouiSheetDefaults.handleHeight)
                                                    .pointerInput(canDrag) {
                                                        if (!canDrag) return@pointerInput
                                                        awaitEachGesture {
                                                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                                            handlePressed = true
                                                            try {
                                                                while (true) {
                                                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                                                    if (event.isCouiTouchCancelled() ||
                                                                        event.changes.none { it.pressed }) break
                                                                }
                                                            } finally {
                                                                handlePressed = false
                                                            }
                                                        }
                                                    }
                                                    .sheetDrag(motion, canDrag) { requestDismiss() }) {
                                                    val press = handlePressAlpha
                                                    if (press > 0f) {
                                                        val shadowWidth = 70.dp.toPx()
                                                        val shadowHeight = 20.dp.toPx()
                                                        val color = Color(if (dark) 0x33FFFFFF else 0x1F000000)
                                                        drawRoundRect(
                                                            color = color.copy(alpha = color.alpha * press),
                                                            topLeft = Offset((size.width - shadowWidth) / 2f,
                                                                (size.height - shadowHeight) / 2f),
                                                            size = Size(shadowWidth, shadowHeight),
                                                            cornerRadius = CornerRadius(16.dp.toPx()),
                                                        )
                                                    }
                                                    val width = 36.dp.toPx(); val height = 4.dp.toPx()
                                                    drawRoundRect(Color(if (dark) 0x40FFFFFF else 0x29000000),
                                                        Offset((size.width - width) / 2f, 12.dp.toPx()), Size(width, height), CornerRadius(height / 2f))
                                                }
                                            }
                                            content()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.sheetDrag(motion: CouiSheetMotion, enabled: Boolean, onDismiss: () -> Unit): Modifier {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val configuration = android.view.ViewConfiguration.get(LocalView.current.context)
    return onGloballyPositioned { coordinates = it }.pointerInput(motion, enabled, configuration) {
        if (!enabled) return@pointerInput
        var cancelled = false
        var dragVelocity = 0f
        var releaseVelocity = Velocity.Zero
        fun track(tracker: CouiVelocityTracker, change: PointerInputChange, start: Boolean = false) {
            coordinates?.takeIf { it.isAttached }?.let { tracker.add(change, it, start) }
        }
        try {
            coroutineScope {
                // Initial 先于拖动的 Main 阶段，UP 也在释放判定前完成采样；观察器不消费事件。
                launch(start = CoroutineStart.UNDISPATCHED) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var tracker = CouiVelocityTracker(down.uptimeMillis)
                        cancelled = false
                        dragVelocity = 0f
                        releaseVelocity = Velocity.Zero
                        track(tracker, down)
                        var pointerId = down.id
                        try {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.isCouiTouchCancelled()) { cancelled = true; break }
                                val change = event.changes.firstOrNull { it.id == pointerId }
                                if (change == null) { cancelled = true; break }
                                track(tracker, change)
                                if (change.pressed && configuration.scaledMaximumFlingVelocity > 10000) {
                                    dragVelocity = tracker.calculateVelocity(
                                        maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()).y
                                }
                                if (!change.pressed) {
                                    val next = event.changes.firstOrNull { it.pressed }
                                    if (next == null) {
                                        val velocity = tracker.calculateVelocity(
                                            maximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat())
                                        val minimum = configuration.scaledMinimumFlingVelocity.toFloat()
                                        releaseVelocity = Velocity(
                                            if (abs(velocity.x) < minimum) 0f else velocity.x,
                                            if (abs(velocity.y) < minimum) 0f else velocity.y)
                                        break
                                    }
                                    // 主指切换重新估速；坐标由当前节点换到窗口，不叠加尚未提交的动画状态。
                                    pointerId = next.id
                                    dragVelocity = 0f
                                    tracker.recycle()
                                    tracker = CouiVelocityTracker(next.uptimeMillis)
                                    track(tracker, next, start = true)
                                }
                            }
                        } finally {
                            tracker.recycle()
                        }
                    }
                }
                detectVerticalDragGestures(
                    onVerticalDrag = { change, amount -> change.consume(); motion.drag(amount, velocity = dragVelocity) },
                    onDragEnd = {
                        if (cancelled) motion.cancelDrag()
                        else if (motion.release(releaseVelocity)) onDismiss()
                    },
                    onDragCancel = motion::cancelDrag,
                )
            }
        } finally {
            motion.cancelDrag()
        }
    }
}
