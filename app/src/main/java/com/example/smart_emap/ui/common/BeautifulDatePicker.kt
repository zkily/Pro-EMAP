package com.example.smart_emap.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.ui.theme.LoginColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatterFull = DateTimeFormatter.ofPattern("yyyy/MM/dd (E)", Locale.JAPAN)
private val displayFormatterShort = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)
private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
private val japanZone = ZoneId.of("Asia/Tokyo")

fun formatBeautifulDisplayDate(value: String, withWeekday: Boolean = true): String = runCatching {
    val date = LocalDate.parse(value.trim().take(10), isoFormatter)
    if (withWeekday) date.format(displayFormatterFull) else date.format(displayFormatterShort)
}.getOrElse { value }

fun parseBeautifulDateMillis(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value.trim().take(10), isoFormatter)
            .atStartOfDay(japanZone).toInstant().toEpochMilli()
    }.getOrNull()
}

fun formatBeautifulIsoDateMillis(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(japanZone).toLocalDate().format(isoFormatter)

/** 大きめの日付入力（フォーム向け） */
@Composable
fun BeautifulDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = LoginColors.Primary,
    placeholder: String = label,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        BeautifulDatePickerDialog(
            value = value,
            title = label,
            accentColor = accentColor,
            onDismiss = { showPicker = false },
            onConfirm = { onValueChange(it) },
        )
    }

    val hasValue = value.isNotBlank()
    val shape = RoundedCornerShape(12.dp)

    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .clickable { showPicker = true },
        shape = shape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (hasValue) accentColor.copy(alpha = 0.5f) else Color(0xFFE2E8F0),
        ),
        tonalElevation = if (hasValue) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BeautifulDateIconBadge(
                accentColor = accentColor,
                active = hasValue,
                size = 32.dp,
                iconSize = 18.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasValue) accentColor else LoginColors.TextMuted,
                )
                Text(
                    text = if (hasValue) formatBeautifulDisplayDate(value) else placeholder,
                    fontSize = 14.sp,
                    fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = if (hasValue) FontFamily.Monospace else FontFamily.Default,
                    color = if (hasValue) LoginColors.TitleDark else Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** コンパクトな日付チップ（ツールバー・フィルター向け） */
@Composable
fun BeautifulDatePickerCompactChip(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LoginColors.Primary,
    height: Dp = 36.dp,
    minWidth: Dp = 112.dp,
    elevated: Boolean = false,
    showWeekday: Boolean = false,
) {
    val shape = RoundedCornerShape(10.dp)
    val hasValue = value.isNotBlank()
    Row(
        modifier = modifier
            .widthIn(min = minWidth)
            .then(if (elevated) Modifier.shadow(5.dp, shape, spotColor = accentColor.copy(alpha = 0.25f)) else Modifier)
            .height(height)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color.White, if (elevated) Color(0xFFF8FAFF) else Color(0xFFFAFBFC)),
                ),
            )
            .border(
                1.dp,
                if (hasValue) accentColor.copy(alpha = 0.45f) else Color(0xFFE2E8F0),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BeautifulDateIconBadge(accentColor = accentColor, active = hasValue, size = 24.dp, iconSize = 14.dp)
        Text(
            text = if (hasValue) formatBeautifulDisplayDate(value, withWeekday = showWeekday) else placeholder,
            fontSize = 12.sp,
            fontWeight = if (hasValue) FontWeight.SemiBold else FontWeight.Medium,
            fontFamily = if (hasValue) FontFamily.Monospace else FontFamily.Default,
            color = if (hasValue) LoginColors.TitleDark else Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

/** ラベル付きコンパクト日付（生産計画フィルター等） */
@Composable
fun BeautifulDatePickerLabeledCompactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF3B82F6),
    placeholder: String = label,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        BeautifulDatePickerDialog(
            value = value,
            title = label,
            accentColor = accentColor,
            confirmLabel = "確定",
            onDismiss = { showPicker = false },
            onConfirm = { onValueChange(it); showPicker = false },
        )
    }
    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        BeautifulDatePickerCompactChip(
            value = value,
            placeholder = placeholder,
            accentColor = accentColor,
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** フォーム内の小さな日付入力 */
@Composable
fun BeautifulDatePickerFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LoginColors.Primary,
    required: Boolean = false,
    placeholder: String = "選択",
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        BeautifulDatePickerDialog(
            value = value,
            title = label,
            accentColor = accentColor,
            confirmLabel = "確定",
            onDismiss = { showPicker = false },
            onConfirm = { onValueChange(it); showPicker = false },
        )
    }
    Column(modifier = modifier) {
        Text(
            text = if (required) "$label *" else label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF606266),
        )
        Spacer(Modifier.height(4.dp))
        BeautifulDatePickerCompactChip(
            value = value,
            placeholder = placeholder,
            accentColor = accentColor,
            onClick = { showPicker = true },
            height = 34.dp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 開始日・終了日（2チップ） */
@Composable
fun BeautifulDateRangeField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LoginColors.Primary,
    startLabel: String = "開始日",
    endLabel: String = "終了日",
    fieldHeight: Dp = 36.dp,
    fieldMinWidth: Dp = 130.dp,
    elevated: Boolean = false,
    separatorColor: Color = Color(0xFF64748B),
    chainPickEndAfterStart: Boolean = false,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        BeautifulDatePickerDialog(
            value = startDate,
            title = startLabel,
            accentColor = accentColor,
            confirmLabel = "確定",
            onDismiss = { pickStart = false },
            onConfirm = {
                onStartChange(it)
                pickStart = false
                if (chainPickEndAfterStart) pickEnd = true
            },
        )
    }
    if (pickEnd) {
        BeautifulDatePickerDialog(
            value = endDate,
            title = endLabel,
            accentColor = accentColor,
            confirmLabel = "確定",
            onDismiss = { pickEnd = false },
            onConfirm = { onEndChange(it); pickEnd = false },
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BeautifulDatePickerCompactChip(
            value = startDate,
            placeholder = startLabel,
            accentColor = accentColor,
            onClick = { pickStart = true },
            height = fieldHeight,
            minWidth = fieldMinWidth,
            elevated = elevated,
        )
        Text("～", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = separatorColor)
        BeautifulDatePickerCompactChip(
            value = endDate,
            placeholder = endLabel,
            accentColor = accentColor,
            onClick = { pickEnd = true },
            height = fieldHeight,
            minWidth = fieldMinWidth,
            elevated = elevated,
        )
    }
}

/** 1行の期間表示（タップで開始→終了） */
@Composable
fun BeautifulDateRangeCompactField(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF3B82F6),
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        BeautifulDatePickerDialog(
            value = startDate,
            title = "開始日",
            accentColor = accentColor,
            confirmLabel = "確定",
            onDismiss = { pickStart = false },
            onConfirm = {
                onStartChange(it)
                pickStart = false
                pickEnd = true
            },
        )
    }
    if (pickEnd) {
        BeautifulDatePickerDialog(
            value = endDate,
            title = "終了日",
            accentColor = accentColor,
            confirmLabel = "確定",
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
    BeautifulDatePickerCompactChip(
        value = if (startDate.isNotBlank() || endDate.isNotBlank()) rangeText else "",
        placeholder = rangeText,
        accentColor = accentColor,
        onClick = { pickStart = true },
        modifier = modifier.fillMaxWidth(),
        minWidth = 0.dp,
    )
}

@Composable
private fun BeautifulDateIconBadge(
    accentColor: Color,
    active: Boolean,
    size: Dp,
    iconSize: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape((size.value * 0.28f).dp))
            .background(
                if (active) {
                    Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.82f)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** 美しい日付選択ダイアログ（全画面共通） */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifulDatePickerDialog(
    value: String,
    title: String = "日付を選択",
    accentColor: Color = LoginColors.Primary,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    confirmLabel: String = "確定する",
    selectableDates: SelectableDates? = null,
) {
    val initialMillis = remember(value) {
        parseBeautifulDateMillis(value) ?: LocalDate.now(japanZone)
            .atStartOfDay(japanZone).toInstant().toEpochMilli()
    }

    val state = if (selectableDates != null) {
        rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayMode = DisplayMode.Picker,
            selectableDates = selectableDates,
        )
    } else {
        rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayMode = DisplayMode.Picker,
        )
    }

    val selectedDateText = state.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(japanZone).toLocalDate().format(displayFormatterFull)
    }.orEmpty()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.92f)
                .shadow(24.dp, shape, spotColor = accentColor.copy(alpha = 0.4f))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.9f), shape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.88f),
                                accentColor.copy(alpha = 0.76f),
                            ),
                        ),
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(
                                "カレンダーから日付を選択してください",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (selectedDateText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accentColor.copy(alpha = 0.04f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("選択中:", fontSize = 11.sp, color = LoginColors.TextMuted, fontWeight = FontWeight.Bold)
                        Text(
                            selectedDateText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = accentColor,
                        )
                    }
                }
            }

            DatePicker(
                state = state,
                title = null,
                headline = null,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = LoginColors.TitleDark,
                    headlineContentColor = accentColor,
                    weekdayContentColor = LoginColors.TextMuted,
                    subheadContentColor = LoginColors.TextMuted,
                    yearContentColor = LoginColors.TitleDark,
                    currentYearContentColor = accentColor,
                    selectedYearContainerColor = accentColor.copy(alpha = 0.2f),
                    selectedYearContentColor = accentColor,
                    dayContentColor = LoginColors.TitleDark,
                    selectedDayContainerColor = accentColor,
                    selectedDayContentColor = Color.White,
                    todayContentColor = accentColor,
                    todayDateBorderColor = accentColor,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = LoginColors.TextMuted),
                ) {
                    Text("キャンセル", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = {
                        state.selectedDateMillis?.let { onConfirm(formatBeautifulIsoDateMillis(it)) }
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
                ) {
                    Text(confirmLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
