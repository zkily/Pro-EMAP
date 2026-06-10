package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)
private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val japanZone = ZoneId.of("Asia/Tokyo")

@Composable
fun ProductionSingleDatePickerField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = label,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        ProductionBeautifulDatePickerDialog(
            value = value,
            title = label,
            onDismiss = { showPicker = false },
            onConfirm = { onChange(it); showPicker = false },
        )
    }
    Column(modifier = modifier) {
        ProductionFilterLabel(label)
        ProductionDateChip(
            value = value,
            placeholder = placeholder,
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 单行期间展示（Web 筛选样式：2026-06-09 ~ 2026-06-09） */
@Composable
fun ProductionCompactDateRangeField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        ProductionBeautifulDatePickerDialog(
            value = startDate,
            title = "開始日",
            onDismiss = { pickStart = false },
            onConfirm = {
                onStartChange(it)
                pickStart = false
                pickEnd = true
            },
        )
    }
    if (pickEnd) {
        ProductionBeautifulDatePickerDialog(
            value = endDate,
            title = "終了日",
            onDismiss = { pickEnd = false },
            onConfirm = { onEndChange(it); pickEnd = false },
        )
    }
    val rangeText = when {
        startDate.isNotBlank() && endDate.isNotBlank() -> "${startDate.take(10)} ~ ${endDate.take(10)}"
        startDate.isNotBlank() -> startDate.take(10)
        endDate.isNotBlank() -> endDate.take(10)
        else -> "期間を選択"
    }
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .clickable { pickStart = true }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = "期間",
            tint = ProductionPlanningColors.TextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            rangeText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (startDate.isNotBlank() || endDate.isNotBlank()) FontFamily.Monospace else FontFamily.Default,
            color = if (startDate.isNotBlank() || endDate.isNotBlank()) {
                ProductionPlanningColors.TextPrimary
            } else {
                ProductionPlanningColors.TextSecondary
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ProductionDateRangePickerField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    startLabel: String = "開始日",
    endLabel: String = "終了日",
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        ProductionBeautifulDatePickerDialog(
            value = startDate,
            title = startLabel,
            onDismiss = { pickStart = false },
            onConfirm = { onStartChange(it); pickStart = false },
        )
    }
    if (pickEnd) {
        ProductionBeautifulDatePickerDialog(
            value = endDate,
            title = endLabel,
            onDismiss = { pickEnd = false },
            onConfirm = { onEndChange(it); pickEnd = false },
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProductionDateChip(
            value = startDate,
            placeholder = startLabel,
            onClick = { pickStart = true },
            modifier = Modifier.weight(1f),
        )
        Text("～", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ProductionPlanningColors.TextSecondary)
        ProductionDateChip(
            value = endDate,
            placeholder = endLabel,
            onClick = { pickEnd = true },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun ProductionDateChip(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val hasValue = value.isNotBlank()
    Row(
        modifier = modifier
            .height(36.dp)
            .shadow(if (hasValue) 3.dp else 1.dp, shape, spotColor = Color(0x402563EB))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White, Color(0xFFF8FAFF)),
                ),
            )
            .border(
                1.dp,
                if (hasValue) Color(0xFF93C5FD) else ProductionPlanningColors.CardBorder,
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF3B82F6), Color(0xFF6366F1)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = placeholder,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = if (hasValue) formatDisplayDate(value) else placeholder,
            fontSize = 12.sp,
            fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = if (hasValue) FontFamily.Monospace else FontFamily.Default,
            color = if (hasValue) ProductionPlanningColors.AccentBlue else ProductionPlanningColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionBeautifulDatePickerDialog(
    value: String,
    title: String = "日付を選択",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val accent = ProductionPlanningColors.AccentBlue
    val accentSecondary = ProductionPlanningColors.AccentPurple
    val initialMillis = remember(value) {
        parseProductionDateMillis(value) ?: LocalDate.now(japanZone)
            .atStartOfDay(japanZone).toInstant().toEpochMilli()
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        initialDisplayMode = DisplayMode.Picker,
    )
    val previewDate = state.selectedDateMillis?.let { formatDisplayDateMillis(it) }.orEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(18.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.92f)
                .shadow(16.dp, shape, spotColor = Color(0x406366F1))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.9f), shape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF8B5CF6)),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("カレンダーから日付を選択", color = Color.White.copy(alpha = 0.88f), fontSize = 11.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.16f))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (previewDate.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SurfaceLikeChip(previewDate)
                }
            }

            DatePicker(
                state = state,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = ProductionPlanningColors.TextPrimary,
                    headlineContentColor = accent,
                    weekdayContentColor = ProductionPlanningColors.TextSecondary,
                    subheadContentColor = ProductionPlanningColors.TextSecondary,
                    yearContentColor = ProductionPlanningColors.TextPrimary,
                    currentYearContentColor = accent,
                    selectedYearContainerColor = accentSecondary,
                    selectedYearContentColor = Color.White,
                    dayContentColor = ProductionPlanningColors.TextPrimary,
                    selectedDayContainerColor = accent,
                    selectedDayContentColor = Color.White,
                    todayContentColor = accent,
                    todayDateBorderColor = accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider(color = ProductionPlanningColors.CardBorder)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル", color = ProductionPlanningColors.TextSecondary, fontSize = 13.sp)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { onConfirm(formatIsoDateMillis(it)) }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                ) {
                    Text("確定", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SurfaceLikeChip(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("選択日", fontSize = 10.sp, color = ProductionPlanningColors.TextSecondary)
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = ProductionPlanningColors.AccentBlue)
    }
}

private fun parseProductionDateMillis(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value.trim().take(10), isoFormatter)
            .atStartOfDay(japanZone).toInstant().toEpochMilli()
    }.getOrNull()
}

private fun formatIsoDateMillis(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(japanZone).toLocalDate().format(isoFormatter)

private fun formatDisplayDateMillis(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(japanZone).toLocalDate().format(displayFormatter)

private fun formatDisplayDate(value: String): String = runCatching {
    LocalDate.parse(value.take(10), isoFormatter).format(displayFormatter)
}.getOrElse { value }
