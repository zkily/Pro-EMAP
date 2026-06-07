package com.example.smart_emap.ui.mes.welding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private val JST = ZoneId.of("Asia/Tokyo")

enum class ConfirmedEditDateTimeTarget {
    Start, End,
}

private fun millisToLocalDateTime(ms: Long): LocalDateTime =
    Instant.ofEpochMilli(ms).atZone(JST).toLocalDateTime()

private fun localDateTimeToMillis(date: LocalDate, hour: Int, minute: Int): Long =
    date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        .atZone(JST)
        .toInstant()
        .toEpochMilli()

private fun fallbackMillis(productionDay: String): Long {
    val date = runCatching { LocalDate.parse(productionDay.trim().take(10)) }.getOrNull()
        ?: LocalDate.now(JST)
    return localDateTimeToMillis(date, 8, 0)
}

private fun weekDayLabels(locale: WeldLocale): List<String> = when (locale) {
    WeldLocale.Ja -> listOf("日", "月", "火", "水", "木", "金", "土")
    WeldLocale.Zh -> listOf("日", "一", "二", "三", "四", "五", "六")
    WeldLocale.Vi -> listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    WeldLocale.En -> listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
}

private fun formatMonthTitle(yearMonth: YearMonth, locale: WeldLocale): String = when (locale) {
    WeldLocale.Ja -> "${yearMonth.year} 年 ${yearMonth.monthValue}月"
    WeldLocale.Zh -> "${yearMonth.year}年${yearMonth.monthValue}月"
    WeldLocale.Vi -> "${yearMonth.monthValue}/${yearMonth.year}"
    WeldLocale.En -> "${yearMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${yearMonth.year}"
}

@Composable
fun ConfirmedEditDateTimeField(
    title: String,
    summary: String,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    fieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
    ) {
        OutlinedTextField(
            value = summary,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(title, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (expanded) Color(0xFF0F766E) else Color(0xFF64748B),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { onExpandedChange(!expanded) },
        )
    }
}

private fun timeColumnLabels(locale: WeldLocale): Pair<String, String> = when (locale) {
    WeldLocale.Ja -> "時" to "分"
    WeldLocale.Zh -> "时" to "分"
    WeldLocale.Vi -> "Giờ" to "Phút"
    WeldLocale.En -> "Hr" to "Min"
}

@Composable
fun DateTimeCalendarTimePanel(
    epochMillis: Long?,
    productionDay: String,
    locale: WeldLocale,
    enabled: Boolean,
    onEpochMillisChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resolvedMs = epochMillis ?: remember(productionDay) { fallbackMillis(productionDay) }
    val initial = remember(resolvedMs) { millisToLocalDateTime(resolvedMs) }
    val (hourLabel, minuteLabel) = remember(locale) { timeColumnLabels(locale) }

    var selectedDate by remember(resolvedMs) { mutableStateOf(initial.toLocalDate()) }
    var hour by remember(resolvedMs) { mutableIntStateOf(initial.hour) }
    var minute by remember(resolvedMs) { mutableIntStateOf(initial.minute) }

    fun emitChange(date: LocalDate = selectedDate, h: Int = hour, m: Int = minute) {
        if (enabled) onEpochMillisChange(localDateTimeToMillis(date, h, m))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFDCDFE6), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MesMonthCalendar(
            selectedDate = selectedDate,
            locale = locale,
            enabled = enabled,
            onDateSelected = { date ->
                selectedDate = date
                emitChange(date = date)
            },
            modifier = Modifier.weight(1.55f),
        )
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .height(248.dp)
                .background(Color(0xFFEBEEF5)),
        )
        MesCompactTimePicker(
            hour = hour,
            minute = minute,
            hourLabel = hourLabel,
            minuteLabel = minuteLabel,
            enabled = enabled,
            onHourChange = { h ->
                hour = h
                emitChange(h = h)
            },
            onMinuteChange = { m ->
                minute = m
                emitChange(m = m)
            },
            modifier = Modifier.weight(0.95f),
        )
    }
}

@Composable
private fun MesMonthCalendar(
    selectedDate: LocalDate,
    locale: WeldLocale,
    enabled: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var displayMonth by remember(selectedDate) { mutableStateOf(YearMonth.from(selectedDate)) }
    val weekDays = remember(locale) { weekDayLabels(locale) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatMonthTitle(displayMonth, locale),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF606266),
                modifier = Modifier.weight(1f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                CalendarNavIcon(Icons.Default.KeyboardDoubleArrowLeft, enabled) {
                    displayMonth = displayMonth.minusYears(1)
                }
                CalendarNavIcon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, enabled) {
                    displayMonth = displayMonth.minusMonths(1)
                }
                CalendarNavIcon(Icons.AutoMirrored.Filled.KeyboardArrowRight, enabled) {
                    displayMonth = displayMonth.plusMonths(1)
                }
                CalendarNavIcon(Icons.Default.KeyboardDoubleArrowRight, enabled) {
                    displayMonth = displayMonth.plusYears(1)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 1.dp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color(0xFF606266),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val firstCell = displayMonth.atDay(1)
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(6) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    repeat(7) { dayIndex ->
                        val cellDate = firstCell.plusDays((week * 7 + dayIndex).toLong())
                        val inMonth = cellDate.month == displayMonth.month
                        val selected = cellDate == selectedDate
                        CalendarDayCell(
                            day = cellDate.dayOfMonth,
                            inMonth = inMonth,
                            selected = selected,
                            enabled = enabled,
                            onClick = {
                                if (!inMonth) displayMonth = YearMonth.from(cellDate)
                                onDateSelected(cellDate)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(28.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF606266),
        )
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    inMonth: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(CircleShape)
            .background(
                when {
                    selected -> Color(0xFF0D9488)
                    else -> Color.Transparent
                },
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                selected -> Color.White
                inMonth -> Color(0xFF606266)
                else -> Color(0xFFC0C4CC)
            },
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MesCompactTimePicker(
    hour: Int,
    minute: Int,
    hourLabel: String,
    minuteLabel: String,
    enabled: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFDCDFE6), RoundedCornerShape(8.dp))
                .background(Color(0xFFFAFAFA))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF909399),
            )
            Text(
                text = "%02d:%02d".format(hour, minute),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF606266),
                fontFamily = FontFamily.Monospace,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TimeWheelColumn(
                label = hourLabel,
                value = hour,
                range = 0..23,
                enabled = enabled,
                onValueChange = onHourChange,
                modifier = Modifier.weight(1f),
            )
            TimeWheelColumn(
                label = minuteLabel,
                value = minute,
                range = 0..59,
                enabled = enabled,
                onValueChange = onMinuteChange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TimeWheelColumn(
    label: String,
    value: Int,
    range: IntRange,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF909399),
        )
        IntWheelColumn(
            value = value,
            range = range,
            formatter = { "%02d".format(it) },
            enabled = enabled,
            onValueChange = onValueChange,
        )
    }
}

@Composable
private fun IntWheelColumn(
    value: Int,
    range: IntRange,
    formatter: (Int) -> String,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
) {
    val items = remember(range) { range.toList() }
    if (items.isEmpty()) return

    val itemHeight = 28.dp
    val visibleCount = 3
    val padCount = visibleCount / 2
    val selectedIndex = (value - range.first).coerceIn(items.indices)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(value, range) {
        if (listState.isScrollInProgress) return@LaunchedEffect
        val idx = (value - range.first).coerceIn(items.indices)
        if (listState.firstVisibleItemIndex != idx || listState.firstVisibleItemScrollOffset != 0) {
            listState.animateScrollToItem(idx)
        }
    }

    LaunchedEffect(listState, enabled, range) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it }
            .collect {
                val idx = listState.firstVisibleItemIndex.coerceIn(items.indices)
                val picked = items[idx]
                if (picked != value) onValueChange(picked)
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleCount)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .border(0.5.dp, Color(0xFFDCDFE6), RoundedCornerShape(8.dp)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color(0xFFECFDF5).copy(alpha = 0.7f)),
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * padCount),
            userScrollEnabled = enabled,
        ) {
            items(items.size) { index ->
                val item = items[index]
                val selected = item == value
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { onValueChange(item) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = formatter(item),
                        fontSize = if (selected) 15.sp else 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) Color(0xFF0F766E) else Color(0xFF909399),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** @deprecated 互換用：旧呼び出しを新パネルへ委譲 */
@Composable
fun DateTimeWheelPickerPanel(
    epochMillis: Long?,
    productionDay: String,
    enabled: Boolean,
    monthLabel: String,
    dayLabel: String,
    hourLabel: String,
    minuteLabel: String,
    onEpochMillisChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    DateTimeCalendarTimePanel(
        epochMillis = epochMillis,
        productionDay = productionDay,
        locale = WeldLocale.Ja,
        enabled = enabled,
        onEpochMillisChange = onEpochMillisChange,
        modifier = modifier,
    )
}

@Composable
fun ConfirmedEditDateTimeWheelSection(
    title: String,
    epochMillis: Long?,
    productionDay: String,
    locale: WeldLocale,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onEpochMillisChange: (Long) -> Unit,
    fieldColors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    modifier: Modifier = Modifier,
) {
    val resolvedMs = epochMillis ?: remember(productionDay) { fallbackMillis(productionDay) }
    val summary = remember(resolvedMs) { WeldingHistoryRowFormat.formatWallInput(resolvedMs) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ConfirmedEditDateTimeField(
            title = title,
            summary = summary,
            enabled = enabled,
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            fieldColors = fieldColors,
        )
        AnimatedVisibility(
            visible = expanded && enabled,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            DateTimeCalendarTimePanel(
                epochMillis = epochMillis,
                productionDay = productionDay,
                locale = locale,
                enabled = enabled,
                onEpochMillisChange = onEpochMillisChange,
            )
        }
    }
}
