package com.itos.coui.picker

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import com.itos.coui.theme.CouiIcon as Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itos.coui.theme.CouiTheme
import com.itos.coui.shape.CouiCapsuleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle as DateTextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

/**
 * 日历 App `COUICalendarPicker` 的 Compose 复刻。
 *
 * 头部、月网格、三列年月日滚轮、选中与翻页动画都来自 Calendar 17.3.70；
 * 翻月箭头只改变浏览月份，点选日期或滚动展开后的日期滚轮才提交受控值。
 * 月份浏览不读取业务数据，宿主持有选中日期与范围。
 */
@Composable
fun CouiCalendarPicker(
    modifier: Modifier = Modifier,
    value: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    dark: Boolean = CouiTheme.isDark,
    accent: Color = CouiTheme.colorSchemeFor(dark).accent,
    previousDescription: String,
    nextDescription: String,
    changeMonthDescription: String,
    yearLabel: String,
    monthLabel: String,
    dayLabel: String,
    minDate: LocalDate = LocalDate.of(1900, 1, 1),
    maxDate: LocalDate = LocalDate.of(2100, 12, 31),
    today: LocalDate = LocalDate.now(),
    colors: CouiCalendarColors = CouiCalendarDefaults.colors(dark),
) {
    require(minDate <= maxDate && value in minDate..maxDate)
    val locale = LocalConfiguration.current.locales[0]
    val medium = remember { FontFamily(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)) }
    val first = YearMonth.from(minDate)
    val count = (ChronoUnit.MONTHS.between(first, YearMonth.from(maxDate)) + 1).toInt()
    val pager = rememberPagerState(
        initialPage = ChronoUnit.MONTHS.between(first, YearMonth.from(value)).toInt(),
        pageCount = { count },
    )
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    var pageAtDown by remember { mutableIntStateOf(pager.currentPage) }
    var headerPage by remember { mutableIntStateOf(pager.currentPage) }
    val pageFling = remember(pager, density) { CouiCalendarFlingBehavior(pager, density) { pageAtDown } }
    var editingMonth by rememberSaveable { mutableStateOf(false) }
    var closingEditor by remember { mutableStateOf(false) }
    var editorYear by rememberSaveable { mutableIntStateOf(value.year) }
    var editorMonth by rememberSaveable { mutableIntStateOf(value.monthValue) }
    var editorDay by rememberSaveable { mutableIntStateOf(value.dayOfMonth) }
    var displayEditor by remember { mutableStateOf(false) }
    val editorDate = remember(editorYear, editorMonth, editorDay, minDate, maxDate) {
        LocalDate.of(
            editorYear,
            editorMonth,
            editorDay.coerceAtMost(YearMonth.of(editorYear, editorMonth).lengthOfMonth()),
        ).coerceIn(minDate, maxDate)
    }
    val current = first.plusMonths(headerPage.toLong())
    val foreground = colors.foreground
    val secondary = colors.secondary
    val weekday = colors.weekday
    val hint = colors.hint
    val highlight = colors.highlight
    val headerFormatter = remember(locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "MMMMy"), locale)
    }
    val fullDateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    }
    val arrowAlpha = remember { Animatable(1f) }

    fun setEditor(date: LocalDate) {
        val clamped = date.coerceIn(minDate, maxDate)
        editorYear = clamped.year
        editorMonth = clamped.monthValue
        editorDay = clamped.dayOfMonth
    }

    fun pageOf(date: LocalDate): Int = ChronoUnit.MONTHS.between(first, YearMonth.from(date)).toInt()

    fun submitEditor(date: LocalDate) {
        val next = date.coerceIn(minDate, maxDate)
        setEditor(next)
        onValueChange(next)
    }

    fun toggleEditor() {
        if (editingMonth) {
            closingEditor = true
            headerPage = pageOf(editorDate)
            editingMonth = false
        } else {
            closingEditor = false
            setEditor(value)
            editingMonth = true
        }
    }

    LaunchedEffect(editingMonth) {
        if (editingMonth) {
            delay(120)
            displayEditor = true
        } else {
            val wasDisplayed = displayEditor
            displayEditor = false
            if (closingEditor && !wasDisplayed) {
                val page = pageOf(editorDate)
                if (page != pager.currentPage) pager.scrollToPage(page)
            }
        }
    }
    LaunchedEffect(editingMonth) {
        arrowAlpha.animateTo(
            if (editingMonth) 0f else 1f,
            tween(
                if (editingMonth) 150 else 280,
                easing = if (editingMonth) CalendarEase else CouiCalendarPageEasing,
            ),
        )
    }
    LaunchedEffect(value, minDate, maxDate) {
        setEditor(value)
        val valuePage = pageOf(value)
        headerPage = valuePage
        if (!editingMonth && valuePage != pager.currentPage) {
            pager.animateScrollToPage(valuePage, animationSpec = tween(300, easing = CouiCalendarPageEasing))
        }
    }
    LaunchedEffect(pager.isScrollInProgress) {
        if (!pager.isScrollInProgress) headerPage = pager.currentPage
    }
    LaunchedEffect(displayEditor) {
        if (!displayEditor && closingEditor) {
            val page = pageOf(editorDate)
            if (page != pager.currentPage) pager.scrollToPage(page)
        }
    }
    LaunchedEffect(pager.currentPage, closingEditor) {
        if (closingEditor && pager.currentPage == pageOf(editorDate)) closingEditor = false
    }

    Column(modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(32.dp)) {
            CalendarArrow(
                modifier = Modifier.align(Alignment.CenterStart),
                description = previousDescription,
                next = false,
                enabled = !editingMonth && headerPage > 0,
                dark = dark,
                colors = colors,
                alpha = if (headerPage > 0) arrowAlpha.value else 0f,
            ) {
                scope.launch {
                    val target = (headerPage - 1).coerceAtLeast(0)
                    headerPage = target
                    pager.animateScrollToPage(target, animationSpec = tween(300, easing = CouiCalendarPageEasing))
                }
            }
            val headerMonth = if (editingMonth || closingEditor) YearMonth.from(editorDate) else current
            val titleInteraction = remember { MutableInteractionSource() }
            Row(
                Modifier.align(Alignment.Center)
                    .clickable(interactionSource = titleInteraction, indication = null, role = Role.Button) {
                        toggleEditor()
                    }
                    .semantics { contentDescription = changeMonthDescription },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    headerFormatter.format(headerMonth.atDay(1)),
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = medium,
                        fontWeight = FontWeight.Normal,
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        color = if (editingMonth) accent else foreground,
                    ),
                    maxLines = 2,
                )
                Box(
                    Modifier.size(16.dp).clip(CouiCapsuleShape).clickable { toggleEditor() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = CalendarExpand,
                        contentDescription = null,
                        tint = secondary,
                    )
                }
            }
            CalendarArrow(
                modifier = Modifier.align(Alignment.CenterEnd),
                description = nextDescription,
                next = true,
                enabled = !editingMonth && headerPage < count - 1,
                dark = dark,
                colors = colors,
                alpha = if (headerPage < count - 1) arrowAlpha.value else 0f,
            ) {
                scope.launch {
                    val target = (headerPage + 1).coerceAtMost(count - 1)
                    headerPage = target
                    pager.animateScrollToPage(target, animationSpec = tween(300, easing = CouiCalendarPageEasing))
                }
            }
        }
        AnimatedContent(
            targetState = displayEditor,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                fadeIn(tween(280, easing = CalendarFadeInEase)) togetherWith
                    fadeOut(tween(150, easing = CalendarEase))
            },
            label = "年月切换",
        ) { editing ->
            if (editing) {
                Box(Modifier.fillMaxWidth().height(CalendarBodyHeight), contentAlignment = Alignment.Center) {
                    CalendarDateEditor(
                        date = editorDate,
                        minDate = minDate,
                        maxDate = maxDate,
                        locale = locale,
                        yearLabel = yearLabel,
                        monthLabel = monthLabel,
                        dayLabel = dayLabel,
                        dark = dark,
                        colors = colors,
                        onChange = ::submitEditor,
                    )
                }
            } else {
                Box(Modifier.fillMaxWidth().height(CalendarBodyHeight)) {
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.fillMaxSize().pointerInput(pager) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                pageAtDown = pager.currentPage
                                do {
                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                } while (event.changes.any { it.pressed })
                            }
                        },
                        flingBehavior = pageFling,
                    ) { page ->
                        CalendarMonthPage(
                            month = first.plusMonths(page.toLong()),
                            value = value,
                            today = today,
                            minDate = minDate,
                            maxDate = maxDate,
                            locale = locale,
                            dark = dark,
                            colors = colors,
                            accent = accent,
                            medium = medium,
                            weekdayColor = weekday,
                            hintColor = hint,
                            highlightColor = highlight,
                            fullDateFormatter = fullDateFormatter,
                            onValueChange = { date -> headerPage = pageOf(date); onValueChange(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarArrow(
    modifier: Modifier = Modifier,
    description: String,
    next: Boolean,
    enabled: Boolean,
    dark: Boolean,
    colors: CouiCalendarColors,
    alpha: Float,
    click: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier.size(32.dp).clip(CouiCapsuleShape)
            .clickable(
                interactionSource = source,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = click,
            )
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(24.dp).graphicsLayer { if (next) rotationZ = 180f },
            imageVector = CalendarChevron,
            contentDescription = description,
            tint = colors.foreground,
        )
    }
}

@Composable
private fun CalendarMonthPage(
    month: YearMonth,
    value: LocalDate,
    today: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    locale: Locale,
    dark: Boolean,
    colors: CouiCalendarColors,
    accent: Color,
    medium: FontFamily,
    weekdayColor: Color,
    hintColor: Color,
    highlightColor: Color,
    fullDateFormatter: DateTimeFormatter,
    onValueChange: (LocalDate) -> Unit,
) {
    val firstDay = Calendar.SUNDAY
    val startDay = (firstDay + 5) % 7 + 1
    val start = (month.atDay(1).dayOfWeek.value - startDay + 7) % 7
    val base = month.atDay(1).minusDays(start.toLong())
    val sixWeeks = month.lengthOfMonth() + start > 35
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(40.dp)) {
            repeat(7) { col ->
                val day = base.plusDays(col.toLong())
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    BasicText(
                        day.dayOfWeek.getDisplayName(DateTextStyle.NARROW, locale),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = medium,
                            fontWeight = FontWeight.Normal,
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            textAlign = TextAlign.Center,
                            color = weekdayColor,
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
        repeat(if (sixWeeks) 6 else 5) { row ->
            Row(Modifier.fillMaxWidth().height(if (sixWeeks) 40.dp else 48.dp)) {
                repeat(7) { col ->
                    val date = base.plusDays((row * 7 + col).toLong())
                    val inMonth = date.monthValue == month.monthValue
                    val withinRange = date in minDate..maxDate
                    CalendarDayCell(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        date = date,
                        inMonth = inMonth,
                        selected = inMonth && date == value,
                        withinRange = withinRange,
                        today = inMonth && date == today,
                        dark = dark,
                        colors = colors,
                        accent = accent,
                        hintColor = hintColor,
                        highlightColor = highlightColor,
                        medium = medium,
                        locale = locale,
                        fullDateFormatter = fullDateFormatter,
                    ) { onValueChange(date) }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    withinRange: Boolean,
    today: Boolean,
    dark: Boolean,
    colors: CouiCalendarColors,
    accent: Color,
    hintColor: Color,
    highlightColor: Color,
    medium: FontFamily,
    locale: Locale,
    fullDateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val newProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (selected) tween(280, easing = CouiCalendarPageEasing) else tween(0),
        label = "日期选中",
    )
    val oldProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = if (selected) tween(0) else tween(150, easing = CalendarEase),
        label = "日期退出",
    )
    val foreground = colors.foreground
    val textColor = when {
        selected -> colors.onAccent
        !inMonth -> hintColor
        today -> accent
        withinRange -> foreground
        else -> foreground.copy(alpha = foreground.alpha * .5f)
    }
    Box(
        modifier.selectable(
            selected = selected,
            interactionSource = source,
            indication = null,
            enabled = withinRange,
            role = Role.RadioButton,
            onClick = { if (!selected) onClick() },
        ).semantics { contentDescription = date.format(fullDateFormatter) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(32.dp).drawBehind {
                val radius = size.minDimension / 2
                if (selected) {
                    val alpha = if (pressed) 176f / 255f else newProgress
                    drawCircle(accent.copy(alpha = alpha), radius * (.8f + .2f * newProgress))
                } else {
                    if (pressed) drawCircle(highlightColor, radius)
                    if (oldProgress > 0f) drawCircle(accent.copy(alpha = oldProgress), radius)
                }
                if (today && !selected) {
                    drawCircle(accent, radius, style = Stroke(1.8.dp.toPx()))
                }
            },
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                String.format(locale, "%d", date.dayOfMonth),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = medium,
                    fontWeight = FontWeight.Normal,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    textAlign = TextAlign.Center,
                    color = textColor,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CalendarDateEditor(
    date: LocalDate,
    minDate: LocalDate,
    maxDate: LocalDate,
    locale: Locale,
    yearLabel: String,
    monthLabel: String,
    dayLabel: String,
    dark: Boolean,
    colors: CouiCalendarColors,
    onChange: (LocalDate) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val pattern = remember(locale) { DateFormat.getBestDateTimePattern(locale, "yyyyMMMMdd") }
    val order = remember(pattern, layoutDirection) {
        val forward = pattern.toList().filter { it in "yMd" }.distinct()
        if (layoutDirection == LayoutDirection.Rtl) forward.reversed() else forward
    }
    val chinese = locale.language == "zh"
    val months = remember(locale) { DateFormatSymbols.getInstance(locale).shortMonths }
    val year = date.year
    val month = date.monthValue
    val minMonth = if (year == minDate.year) minDate.monthValue else 1
    val maxMonth = if (year == maxDate.year) maxDate.monthValue else 12
    val minDay = if (year == minDate.year && month == minDate.monthValue) minDate.dayOfMonth else 1
    val maxDay = if (year == maxDate.year && month == maxDate.monthValue) {
        maxDate.dayOfMonth
    } else {
        YearMonth.of(year, month).lengthOfMonth()
    }

    fun submit(next: LocalDate) {
        onChange(next.coerceIn(minDate, maxDate))
    }

    fun submitYear(nextYear: Int) {
        val y = nextYear.coerceIn(minDate.year, maxDate.year)
        val m = month.coerceIn(
            if (y == minDate.year) minDate.monthValue else 1,
            if (y == maxDate.year) maxDate.monthValue else 12,
        )
        val d = date.dayOfMonth.coerceAtMost(YearMonth.of(y, m).lengthOfMonth())
        submit(LocalDate.of(y, m, d))
    }

    fun submitMonth(nextMonth: Int) {
        val d = date.dayOfMonth.coerceAtMost(YearMonth.of(year, nextMonth).lengthOfMonth())
        submit(LocalDate.of(year, nextMonth, d))
    }

    fun submitDay(nextDay: Int) {
        submit(LocalDate.of(year, month, nextDay))
    }

    Box(
        Modifier.fillMaxWidth().height(CouiPickerDefaults.height + CouiPickerDefaults.padding * 2)
            .drawBehind {
                val inset = 16.dp.toPx().toInt().toFloat()
                val half = 24.dp.toPx().toInt().toFloat()
                val thickness = .33000004.dp.toPx().toInt().coerceAtLeast(1).toFloat()
                val color = colors.divider
                for (y in listOf(size.height / 2 - half, size.height / 2 + half)) {
                    drawRect(
                        color = color,
                        topLeft = Offset(inset, y.toInt().toFloat()),
                        size = Size((size.width - 2 * inset).coerceAtLeast(0f), thickness),
                    )
                }
            },
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.fillMaxWidth().fillMaxHeight().padding(vertical = CouiPickerDefaults.padding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (kind in order) {
                    when (kind) {
                        'y' -> CouiWheelPicker(
                            Modifier.width(CouiPickerDefaults.dateYearWidth),
                            year,
                            ::submitYear,
                            minDate.year..maxDate.year,
                            yearLabel,
                            dark,
                            colors = CouiWheelColors(colors.hint, colors.foreground),
                            repeatIntervalMillis = 100,
                            formatter = { if (chinese) "$it$yearLabel" else String.format(locale, "%d", it) },
                        )
                        'M' -> CouiWheelPicker(
                            Modifier.width(CouiPickerDefaults.dateMonthWidth),
                            month,
                            ::submitMonth,
                            minMonth..maxMonth,
                            monthLabel,
                            dark,
                            colors = CouiWheelColors(colors.hint, colors.foreground),
                            wrap = minMonth == 1 && maxMonth == 12,
                            repeatIntervalMillis = 200,
                            formatter = { months[it - 1] },
                        )
                        'd' -> CouiWheelPicker(
                            Modifier.width(CouiPickerDefaults.columnWidth),
                            date.dayOfMonth,
                            ::submitDay,
                            minDay..maxDay,
                            dayLabel,
                            dark,
                            colors = CouiWheelColors(colors.hint, colors.foreground),
                            repeatIntervalMillis = 100,
                            formatter = { if (chinese) "$it$dayLabel" else String.format(locale, "%02d", it) },
                        )
                    }
                }
            }
        }
    }
}

private val CalendarBodyHeight = 280.dp
private val CalendarEase = CubicBezierEasing(.33f, 0f, .67f, 1f)
private val CalendarFadeInEase = CubicBezierEasing(.4f, 0f, 1f, 1f)

// Calendar 实际引用的 24dp chevron；不拿返回长箭头充当月历箭头。
private val CalendarChevron = ImageVector.Builder("CalendarChevron", 24.dp, 24.dp, 24f, 24f,
    autoMirror = true).apply {
    path(fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.4f,
        strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(13.84f, 5.636f); lineTo(7.829f, 11.646f)
        curveTo(7.634f, 11.842f, 7.634f, 12.158f, 7.829f, 12.354f); lineTo(13.84f, 18.364f)
    }
}.build()

// Calendar 头部实际引用的 16dp 折叠箭头；展开只旋转视图，不替换矢量。
private val CalendarExpand = ImageVector.Builder("CalendarExpand", 16.dp, 16.dp, 16f, 16f).addPath(
    pathData = PathParser().parsePathString(
        "M4.1,5.5h7.8a0.6,0.6 0,0 1,0.6 0.6,0.65 0.65,0 0,1 -0.13,0.38L8.58,11.22a0.76 0.76,0 0,1 -1 0.12l-0.11,-0.12L3.63,6.48a0.61 0.61,0 0,1 0.09 -0.85A0.65 0.65,0 0,1,4.1 5.5Z",
    ).toNodes(),
    fill = SolidColor(Color.Black),
    pathFillType = PathFillType.EvenOdd,
).build()
