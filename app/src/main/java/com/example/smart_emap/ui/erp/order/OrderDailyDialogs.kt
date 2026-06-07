package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.OrderDailyEditRowUi
import com.example.smart_emap.data.repository.OrderDailyUiMapper

private const val DailyDialogWidthFraction = 0.784f // 0.98 × 80%

private enum class DailyNumericField(val label: String) {
    ConfirmedBoxes("確定箱"),
    ConfirmedUnits("確定本"),
    Forecast("内示"),
}

private data class DailyNumericEditTarget(
    val rowIndex: Int,
    val field: DailyNumericField,
)

private val DailyRowHeight = 32.dp
private val DailyHeaderHeight = 30.dp
private val DailyCellPadH = 6.dp

private enum class DailyColAlign {
    Start,
    Center,
    End,
    ;

    val box: Alignment
        get() = when (this) {
            Start -> Alignment.CenterStart
            Center -> Alignment.Center
            End -> Alignment.CenterEnd
        }

    val text: TextAlign
        get() = when (this) {
            Start -> TextAlign.Start
            Center -> TextAlign.Center
            End -> TextAlign.End
        }
}

private data class DailyTableWidths(
    val destination: Dp,
    val productName: Dp,
    val productType: Dp,
    val unitPerBox: Dp,
    val shipDate: Dp,
    val weekday: Dp,
    val confirmedBoxes: Dp,
    val confirmedUnits: Dp,
    val forecast: Dp,
    val delivery: Dp,
) {
    fun total(includeForecast: Boolean): Dp =
        destination + productName + productType + unitPerBox + shipDate + weekday +
            confirmedBoxes + confirmedUnits + (if (includeForecast) forecast else 0.dp) + delivery
}

private fun resolveDailyTableWidths(available: Dp, includeForecast: Boolean): DailyTableWidths {
    val fixed = 58.dp + 36.dp + 72.dp + 28.dp + 52.dp + 52.dp +
        (if (includeForecast) 52.dp else 0.dp) + 40.dp
    val destBase = 82.dp
    val nameBase = 88.dp
    val minTotal = fixed + destBase + nameBase
    val extra = (available - minTotal).coerceAtLeast(0.dp)
    return DailyTableWidths(
        destination = destBase + extra * 0.38f,
        productName = nameBase + extra * 0.62f,
        productType = 58.dp,
        unitPerBox = 36.dp,
        shipDate = 72.dp,
        weekday = 28.dp,
        confirmedBoxes = 52.dp,
        confirmedUnits = 52.dp,
        forecast = 52.dp,
        delivery = 40.dp,
    )
}

@Composable
fun DailyBatchEditDialog(
    state: OrderMonthlyUiState,
    viewModel: OrderMonthlyViewModel,
) {
    DailyDialogShell(
        saving = state.dailyBatchSaving,
        onDismiss = viewModel::dismissDialog,
    ) {
        DailyBatchHeader(
            changedCount = state.dailyBatchChangedIds.size,
            totalCount = state.dailyBatchRows.size,
            saving = state.dailyBatchSaving,
            onPrint = viewModel::shareDailyBatchPrint,
            onUpdateForecast = viewModel::applyDailyBatchForecastFromConfirmed,
            onSave = viewModel::saveDailyBatchChanges,
            onClose = viewModel::dismissDialog,
            saveEnabled = state.dailyBatchChangedIds.isNotEmpty(),
            actionEnabled = state.dailyBatchRows.isNotEmpty() && !state.dailyBatchSaving,
        )
        when {
            state.dailyBatchLoading -> DailyDialogLoading()
            state.dailyBatchRows.isEmpty() -> DailyDialogEmpty(
                if (state.dailyBatchOrderId.isBlank()) "月次受注IDがありません" else "日別受注データがありません",
            )
            else -> DailyBatchTable(
                rows = state.dailyBatchRows,
                changedIds = state.dailyBatchChangedIds,
                includeForecast = true,
                onConfirmedBoxesChange = viewModel::setDailyBatchConfirmedBoxes,
                onConfirmedUnitsChange = viewModel::setDailyBatchConfirmedUnits,
                onForecastChange = viewModel::setDailyBatchForecast,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyManageDialog(
    state: OrderMonthlyUiState,
    viewModel: OrderMonthlyViewModel,
) {
    DailyDialogShell(
        saving = state.dailyManageSaving,
        onDismiss = viewModel::dismissDialog,
    ) {
        DailyManageHeader(
            totalCount = state.dailyManageRows.size,
            changedCount = state.dailyManageChangedIds.size,
            saving = state.dailyManageSaving,
            onSave = viewModel::saveDailyManageChanges,
            onClose = viewModel::dismissDialog,
            saveEnabled = state.dailyManageChangedIds.isNotEmpty(),
        )
        DailyManageFilterBar(
            date = state.dailyManageDate,
            destinationCd = state.dailyManageDestinationCd,
            destinationOptions = state.destinationOptions,
            onDateChange = viewModel::setDailyManageDate,
            onPrevDay = viewModel::dailyManagePrevDay,
            onToday = viewModel::dailyManageToday,
            onNextDay = viewModel::dailyManageNextDay,
            onDestinationChange = viewModel::setDailyManageDestinationCd,
            onShortcut = viewModel::applyDailyManageShortcut,
        )
        when {
            state.dailyManageLoading -> DailyDialogLoading(height = 100.dp)
            state.dailyManageRows.isEmpty() -> DailyDialogEmpty("該当データがありません")
            else -> DailyBatchTable(
                rows = state.dailyManageRows,
                changedIds = state.dailyManageChangedIds,
                includeForecast = false,
                onConfirmedBoxesChange = viewModel::setDailyManageConfirmedBoxes,
                onConfirmedUnitsChange = viewModel::setDailyManageConfirmedUnits,
                onForecastChange = { _, _ -> },
            )
        }
    }
}

@Composable
private fun DailyDialogShell(
    saving: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(DailyDialogWidthFraction)
                .heightIn(max = 660.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp), spotColor = Color(0x506366F1))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF8FAFC), Color(0xFFEEF2FF), Color(0xFFF5F3FF)),
                    ),
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
            if (saving) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = OrderMonthlyColors.BtnIndigo,
                        strokeWidth = 3.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyDialogLoading(height: Dp = 120.dp) {
    Box(
        modifier = Modifier.fillMaxWidth().height(height),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = OrderMonthlyColors.BtnBlue, strokeWidth = 2.5.dp)
    }
}

@Composable
private fun DailyDialogEmpty(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, color = OrderMonthlyColors.TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun DailyBatchHeader(
    changedCount: Int,
    totalCount: Int,
    saving: Boolean,
    onPrint: () -> Unit,
    onUpdateForecast: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    saveEnabled: Boolean,
    actionEnabled: Boolean,
) {
    DailyDialogHeaderBar(
        icon = Icons.Default.Edit,
        title = "日別受注編集",
        totalCount = totalCount,
        changedCount = changedCount,
        onClose = onClose,
    ) {
        DailyHeaderBtn("印刷", Icons.Default.Print, DailyHeaderBtnStyle.Glass, actionEnabled, onPrint)
        DailyHeaderBtn("内示更新", Icons.Default.Refresh, DailyHeaderBtnStyle.Teal, actionEnabled, onUpdateForecast)
        DailyHeaderBtn("一括保存", Icons.Default.Check, DailyHeaderBtnStyle.Save, saveEnabled && !saving, onSave)
    }
}

@Composable
private fun DailyManageHeader(
    totalCount: Int,
    changedCount: Int,
    saving: Boolean,
    onSave: () -> Unit,
    onClose: () -> Unit,
    saveEnabled: Boolean,
) {
    DailyDialogHeaderBar(
        icon = Icons.Default.CalendarMonth,
        title = "日受注管理",
        totalCount = totalCount,
        changedCount = changedCount,
        onClose = onClose,
        headerGradient = listOf(Color(0xF03B82F6), Color(0xF06366F1), Color(0xF0818CF8)),
    ) {
        DailyHeaderBtn("保存", Icons.Default.Check, DailyHeaderBtnStyle.Indigo, saveEnabled && !saving, onSave)
    }
}

private val DailyFilterControlHeight = 32.dp

private object DailyFilterAccents {
    val Date = Color(0xFF3B82F6)
    val DateDark = Color(0xFF2563EB)
    val Dest = Color(0xFF6366F1)
    val DestDark = Color(0xFF4F46E5)
    val Shortcut = Color(0xFF8B5CF6)
    val ShortcutDark = Color(0xFF7C3AED)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyManageFilterBar(
    date: String,
    destinationCd: String,
    destinationOptions: List<DestinationOptionDto>,
    onDateChange: (String) -> Unit,
    onPrevDay: () -> Unit,
    onToday: () -> Unit,
    onNextDay: () -> Unit,
    onDestinationChange: (String) -> Unit,
    onShortcut: (String) -> Unit,
) {
    var destExpanded by remember { mutableStateOf(false) }
    val japanZone = remember { ZoneId.of("Asia/Tokyo") }
    val todayStr = remember { LocalDate.now(japanZone).format(DateTimeFormatter.ISO_LOCAL_DATE) }
    val isToday = date == todayStr

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(12.dp), spotColor = Color(0x206366F1))
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.88f), Color(0xFFF8FAFF).copy(alpha = 0.82f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .padding(vertical = 7.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .wrapContentWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DailyFilterSection(
                accent = DailyFilterAccents.Date,
                icon = Icons.Default.CalendarMonth,
                label = "日付",
            ) {
                DailyManageDateField(date = date, onDateSelected = onDateChange)
                DailyManageNavChip("前日", onPrevDay, accent = DailyFilterAccents.Date)
                DailyManageNavChip("今日", onToday, accent = DailyFilterAccents.Date, filled = isToday)
                DailyManageNavChip("翌日", onNextDay, accent = DailyFilterAccents.Date)
            }

            DailyFilterSection(
                accent = DailyFilterAccents.Dest,
                icon = Icons.Default.LocalShipping,
                label = "納入先",
            ) {
                DailyManageDestDropdown(
                    destinationCd = destinationCd,
                    destinationOptions = destinationOptions,
                    expanded = destExpanded,
                    onExpandedChange = { destExpanded = it },
                    onDestinationChange = onDestinationChange,
                )
            }

            DailyFilterSection(
                accent = DailyFilterAccents.Shortcut,
                label = "拠点",
            ) {
                OrderMonthlyViewModel.DAILY_MANAGE_SHORTCUTS.forEach { label ->
                    val active = destinationOptions.any {
                        it.cd == destinationCd && it.name.contains(label)
                    }
                    DailyManageShortcutChip(
                        text = label,
                        filled = active,
                        onClick = { onShortcut(label) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyFilterSection(
    accent: Color,
    label: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .height(DailyFilterControlHeight + 10.dp)
            .shadow(3.dp, shape, spotColor = accent.copy(alpha = 0.28f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.14f), accent.copy(alpha = 0.05f)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.24f), shape)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .shadow(2.dp, RoundedCornerShape(5.dp), spotColor = accent.copy(0.35f))
                        .clip(RoundedCornerShape(5.dp))
                        .background(Brush.linearGradient(listOf(accent.copy(0.25f), accent.copy(0.12f))))
                        .border(1.dp, accent.copy(0.3f), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(11.dp))
                }
            }
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent.copy(alpha = 0.85f),
                letterSpacing = 0.2.sp,
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyManageDateField(
    date: String,
    onDateSelected: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val accent = DailyFilterAccents.Date
    val shape = RoundedCornerShape(8.dp)
    val japanZone = remember { ZoneId.of("Asia/Tokyo") }
    val initialMillis = remember(date) {
        parseDailyDateMillis(date) ?: LocalDate.now(japanZone)
            .atStartOfDay(japanZone)
            .toInstant()
            .toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(formatDailyDateMillis(millis, japanZone))
                        }
                        showPicker = false
                    },
                ) {
                    Text("確定", color = accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("キャンセル", color = OrderMonthlyColors.TextMuted)
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = accent,
                    todayDateBorderColor = accent,
                    selectedYearContainerColor = accent,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .widthIn(min = 112.dp)
            .height(DailyFilterControlHeight)
            .shadow(3.dp, shape, spotColor = accent.copy(alpha = 0.35f))
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White, Color(0xFFEFF6FF)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.35f), shape)
            .clickable { showPicker = true },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                    ),
                ),
        )
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
            Text(
                text = date.ifBlank { "選択" },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (date.isBlank()) OrderMonthlyColors.TextMuted else DailyFilterAccents.DateDark,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyManageDestDropdown(
    destinationCd: String,
    destinationOptions: List<DestinationOptionDto>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDestinationChange: (String) -> Unit,
) {
    val accent = DailyFilterAccents.Dest
    val shape = RoundedCornerShape(8.dp)
    val label = destinationOptions.find { it.cd == destinationCd }
        ?.let { if (it.name.isBlank()) it.cd else it.name }
        ?: "すべて"

    Box(modifier = Modifier.wrapContentWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.wrapContentWidth(),
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 136.dp, max = 188.dp)
                    .height(DailyFilterControlHeight)
                    .shadow(3.dp, shape, spotColor = accent.copy(alpha = 0.32f))
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFF5F3FF)),
                        ),
                    )
                    .border(1.dp, accent.copy(alpha = 0.32f), shape)
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                            ),
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = if (destinationCd.isNotBlank()) 2.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (destinationCd.isBlank()) OrderMonthlyColors.TextMuted else DailyFilterAccents.DestDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (destinationCd.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .shadow(1.dp, RoundedCornerShape(99.dp))
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onDestinationChange("")
                                        onExpandedChange(false)
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "クリア",
                                tint = OrderMonthlyColors.TextMuted,
                                modifier = Modifier.size(12.dp),
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                DropdownMenuItem(
                    text = { Text("すべて", fontSize = 12.sp) },
                    onClick = { onDestinationChange(""); onExpandedChange(false) },
                )
                destinationOptions.forEach { d ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${d.cd} | ${d.name}".trimEnd(' ', '|'),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        onClick = { onDestinationChange(d.cd); onExpandedChange(false) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyManageNavChip(
    text: String,
    onClick: () -> Unit,
    accent: Color,
    filled: Boolean = false,
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .height(DailyFilterControlHeight)
            .then(
                if (filled) {
                    Modifier
                        .shadow(4.dp, shape, spotColor = accent.copy(alpha = 0.45f))
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(accent.copy(alpha = 0.95f), DailyFilterAccents.DateDark),
                            ),
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                } else {
                    Modifier
                        .shadow(2.dp, shape, spotColor = Color(0x15000000))
                        .clip(shape)
                        .background(
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFC))),
                        )
                        .border(1.dp, accent.copy(alpha = 0.22f), shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = if (filled) FontWeight.Bold else FontWeight.Medium,
            color = if (filled) Color.White else accent.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun DailyManageShortcutChip(text: String, onClick: () -> Unit, filled: Boolean = false) {
    val accent = DailyFilterAccents.Shortcut
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = Modifier
            .height(DailyFilterControlHeight)
            .then(
                if (filled) {
                    Modifier
                        .shadow(4.dp, shape, spotColor = accent.copy(alpha = 0.5f))
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFA78BFA), DailyFilterAccents.ShortcutDark),
                            ),
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.38f), shape)
                } else {
                    Modifier
                        .shadow(2.dp, shape, spotColor = Color(0x12000000))
                        .clip(shape)
                        .background(
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFFAF5FF))),
                        )
                        .border(1.dp, accent.copy(alpha = 0.2f), shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = if (filled) FontWeight.Bold else FontWeight.Medium,
            color = if (filled) Color.White else accent.copy(alpha = 0.85f),
        )
    }
}

@Composable
private fun DailyFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(Color(0x18000000)),
    )
}

private fun parseDailyDateMillis(value: String): Long? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            .atStartOfDay(ZoneId.of("Asia/Tokyo"))
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun formatDailyDateMillis(millis: Long, zone: ZoneId): String =
    Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)

@Composable
private fun DailyDialogHeaderBar(
    icon: ImageVector,
    title: String,
    totalCount: Int,
    changedCount: Int,
    onClose: () -> Unit,
    headerGradient: List<Color> = listOf(Color(0xF04F46E5), Color(0xF06366F1), Color(0xF08B5CF6)),
    actions: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Brush.linearGradient(headerGradient)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        radius = 360f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.28f)),
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .shadow(3.dp, RoundedCornerShape(7.dp), spotColor = Color(0x30000000))
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.1f)),
                            ),
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 108.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                DailyStatChip("$totalCount", "件")
                if (changedCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    DailyStatChip("$changedCount", "変更", highlight = true)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                actions()
                DailyHeaderCloseBtn(onClick = onClose)
            }
        }
    }
}

@Composable
private fun DailyStatChip(value: String, label: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .height(22.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                if (highlight) Color(0x40FEF08A) else Color.White.copy(alpha = 0.14f),
            )
            .border(
                1.dp,
                if (highlight) Color(0x80FEF08A) else Color.White.copy(alpha = 0.24f),
                RoundedCornerShape(5.dp),
            )
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.82f), fontSize = 9.sp)
    }
}

private enum class DailyHeaderBtnStyle {
    Glass,
    Teal,
    Save,
    Indigo,
}

@Composable
private fun DailyHeaderBtn(
    label: String,
    icon: ImageVector,
    style: DailyHeaderBtnStyle,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = when (style) {
        DailyHeaderBtnStyle.Glass -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.1f)),
        )
        DailyHeaderBtnStyle.Teal -> Brush.linearGradient(
            listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)),
        )
        DailyHeaderBtnStyle.Save -> Brush.linearGradient(
            listOf(Color(0xFF34D399), Color(0xFF059669)),
        )
        DailyHeaderBtnStyle.Indigo -> Brush.linearGradient(
            listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5)),
        )
    }
    Box(
        modifier = Modifier
            .height(26.dp)
            .shadow(
                if (enabled) 5.dp else 0.dp,
                RoundedCornerShape(7.dp),
                spotColor = when (style) {
                    DailyHeaderBtnStyle.Glass -> Color.White.copy(alpha = 0.25f)
                    DailyHeaderBtnStyle.Teal -> Color(0xFF0D9488).copy(alpha = 0.35f)
                    DailyHeaderBtnStyle.Save -> Color(0xFF059669).copy(alpha = 0.4f)
                    DailyHeaderBtnStyle.Indigo -> Color(0xFF4F46E5).copy(alpha = 0.4f)
                },
            )
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.3f else 0.12f), RoundedCornerShape(7.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = if (enabled) 0.18f else 0.06f), Color.Transparent),
                    ),
                ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.45f),
                modifier = Modifier.size(12.dp),
            )
            Text(
                label,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.45f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DailyHeaderCloseBtn(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .shadow(4.dp, RoundedCornerShape(7.dp), spotColor = Color(0x35EF4444))
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.08f)),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.26f), RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun DailyFilterChip(text: String, onClick: () -> Unit, filled: Boolean = false) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (filled) {
                    Modifier.background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))))
                } else {
                    Modifier
                        .background(Color.White)
                        .border(1.dp, OrderMonthlyColors.BorderLight, RoundedCornerShape(6.dp))
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (filled) Color.White else OrderMonthlyColors.TextPrimary,
        )
    }
}

@Composable
private fun DailyBatchTable(
    rows: List<OrderDailyEditRowUi>,
    changedIds: Set<Int>,
    includeForecast: Boolean,
    onConfirmedBoxesChange: (Int, String) -> Unit,
    onConfirmedUnitsChange: (Int, String) -> Unit,
    onForecastChange: (Int, String) -> Unit,
) {
    val summary = OrderDailyUiMapper.summarize(rows, includeForecast)
    val scroll = rememberScrollState()
    val listState = rememberLazyListState()
    var numericTarget by remember { mutableStateOf<DailyNumericEditTarget?>(null) }

    numericTarget?.let { target ->
        if (target.rowIndex in rows.indices) {
            key(target.rowIndex, target.field) {
                val row = rows[target.rowIndex]
                val initial = when (target.field) {
                    DailyNumericField.ConfirmedBoxes -> row.confirmedBoxes
                    DailyNumericField.ConfirmedUnits -> row.confirmedUnits
                    DailyNumericField.Forecast -> row.forecastUnits
                }
                DailyNumericEntryDialog(
                    target = target,
                    row = row,
                    rowCount = rows.size,
                    initialValue = initial,
                    onDismiss = { numericTarget = null },
                    onConfirm = { value ->
                        when (target.field) {
                            DailyNumericField.ConfirmedBoxes -> onConfirmedBoxesChange(target.rowIndex, value)
                            DailyNumericField.ConfirmedUnits -> onConfirmedUnitsChange(target.rowIndex, value)
                            DailyNumericField.Forecast -> onForecastChange(target.rowIndex, value)
                        }
                        val nextIndex = target.rowIndex + 1
                        numericTarget = if (nextIndex < rows.size) {
                            target.copy(rowIndex = nextIndex)
                        } else {
                            null
                        }
                    },
                )
            }
        } else {
            numericTarget = null
        }
    }

    LaunchedEffect(numericTarget) {
        numericTarget?.let { listState.animateScrollToItem(it.rowIndex) }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 4.dp)
            .orderMonthlyGlassSurface(RoundedCornerShape(10.dp), elevation = 4.dp)
            .padding(4.dp),
    ) {
        val widths = resolveDailyTableWidths(maxWidth, includeForecast)
        val tableWidth = widths.total(includeForecast).coerceAtLeast(maxWidth)

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scroll)) {
            Column(modifier = Modifier.width(tableWidth)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DailyHeaderHeight)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(OrderMonthlyColors.tableHeaderBackground),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DailyHeaderCell("納入先", widths.destination, DailyColAlign.Start)
                    DailyHeaderCell("製品名", widths.productName, DailyColAlign.Start)
                    DailyHeaderCell("種別", widths.productType, DailyColAlign.Center)
                    DailyHeaderCell("入数", widths.unitPerBox, DailyColAlign.Center)
                    DailyHeaderCell("出荷日", widths.shipDate, DailyColAlign.Center)
                    DailyHeaderCell("曜", widths.weekday, DailyColAlign.Center)
                    DailyHeaderCell("確定箱", widths.confirmedBoxes, DailyColAlign.End, DailyFieldColors.boxes.accent)
                    DailyHeaderCell("確定本", widths.confirmedUnits, DailyColAlign.End, DailyFieldColors.units.accent)
                    if (includeForecast) {
                        DailyHeaderCell("内示", widths.forecast, DailyColAlign.End, DailyFieldColors.forecast.accent)
                    }
                    DailyHeaderCell("納入日", widths.delivery, DailyColAlign.Center)
                }
                HorizontalDivider(color = Color(0x336366F1), thickness = 1.dp)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                ) {
                    itemsIndexed(rows, key = { _, row -> row.id }) { index, row ->
                        val changed = row.id in changedIds
                        val activeTarget = numericTarget?.takeIf { it.rowIndex == index }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .background(
                                    when {
                                        changed -> Color(0xFFFFF7ED)
                                        index % 2 == 0 -> Color.White.copy(alpha = 0.7f)
                                        else -> Color(0xFFFAFBFC)
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (changed) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFFF97316)),
                                )
                            }
                            DailyTextCell(row.destinationName, widths.destination, DailyColAlign.Start)
                            DailyTextCell(row.productName, widths.productName, DailyColAlign.Start)
                            DailyTextCell(row.productType, widths.productType, DailyColAlign.Center)
                            DailyTextCell(row.unitPerBox.toString(), widths.unitPerBox, DailyColAlign.Center, monospace = true)
                            DailyTextCell(
                                OrderDailyUiMapper.formatShipDate(row.shipDate),
                                widths.shipDate,
                                DailyColAlign.Center,
                            )
                            DailyTextCell(
                                row.weekday.orEmpty(),
                                widths.weekday,
                                DailyColAlign.Center,
                                color = weekdayColor(row.weekday),
                            )
                            DailyTapNumericCell(
                                value = row.confirmedBoxes,
                                width = widths.confirmedBoxes,
                                field = DailyNumericField.ConfirmedBoxes,
                                changed = changed,
                                active = activeTarget?.field == DailyNumericField.ConfirmedBoxes,
                                onClick = { numericTarget = DailyNumericEditTarget(index, DailyNumericField.ConfirmedBoxes) },
                            )
                            DailyTapNumericCell(
                                value = row.confirmedUnits,
                                width = widths.confirmedUnits,
                                field = DailyNumericField.ConfirmedUnits,
                                changed = changed,
                                active = activeTarget?.field == DailyNumericField.ConfirmedUnits,
                                onClick = { numericTarget = DailyNumericEditTarget(index, DailyNumericField.ConfirmedUnits) },
                            )
                            if (includeForecast) {
                                DailyTapNumericCell(
                                    value = row.forecastUnits,
                                    width = widths.forecast,
                                    field = DailyNumericField.Forecast,
                                    changed = changed,
                                    active = activeTarget?.field == DailyNumericField.Forecast,
                                    onClick = { numericTarget = DailyNumericEditTarget(index, DailyNumericField.Forecast) },
                                )
                            }
                            DailyTextCell(
                                OrderDailyUiMapper.formatDeliveryDate(row.deliveryDate),
                                widths.delivery,
                                DailyColAlign.Center,
                            )
                        }
                        HorizontalDivider(color = OrderMonthlyColors.TableRowDivider, thickness = 0.5.dp)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DailyRowHeight)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x263B82F6), Color(0x1A10B981), Color(0x268B5CF6)),
                            ),
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DailyTextCell("合計", widths.destination, DailyColAlign.Start, bold = true)
                    DailyTextCell("", widths.productName, DailyColAlign.Start)
                    DailyTextCell("", widths.productType, DailyColAlign.Center)
                    DailyTextCell("", widths.unitPerBox, DailyColAlign.Center)
                    DailyTextCell("", widths.shipDate, DailyColAlign.Center)
                    DailyTextCell("", widths.weekday, DailyColAlign.Center)
                    DailyTextCell(
                        OrderDailyUiMapper.formatCount(summary.confirmedBoxes),
                        widths.confirmedBoxes,
                        DailyColAlign.End,
                        bold = true,
                        monospace = true,
                        color = DailyFieldColors.boxes.accent,
                    )
                    DailyTextCell(
                        OrderDailyUiMapper.formatCount(summary.confirmedUnits),
                        widths.confirmedUnits,
                        DailyColAlign.End,
                        bold = true,
                        monospace = true,
                        color = DailyFieldColors.units.accent,
                    )
                    if (includeForecast) {
                        DailyTextCell(
                            OrderDailyUiMapper.formatCount(summary.forecastUnits),
                            widths.forecast,
                            DailyColAlign.End,
                            bold = true,
                            monospace = true,
                            color = DailyFieldColors.forecast.accent,
                        )
                    }
                    DailyTextCell("", widths.delivery, DailyColAlign.Center)
                }
            }
        }
    }
}

private data class DailyFieldColorSet(
    val bg: Color,
    val border: Color,
    val accent: Color,
)

private object DailyFieldColors {
    val boxes = DailyFieldColorSet(Color(0xFFEFF6FF), Color(0xFF93C5FD), Color(0xFF2563EB))
    val units = DailyFieldColorSet(Color(0xFFECFDF5), Color(0xFF6EE7B7), Color(0xFF059669))
    val forecast = DailyFieldColorSet(Color(0xFFF5F3FF), Color(0xFFC4B5FD), Color(0xFF7C3AED))
}

@Composable
private fun DailyNumericEntryDialog(
    target: DailyNumericEditTarget,
    row: OrderDailyEditRowUi,
    rowCount: Int,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val fieldColors = when (target.field) {
        DailyNumericField.ConfirmedBoxes -> DailyFieldColors.boxes
        DailyNumericField.ConfirmedUnits -> DailyFieldColors.units
        DailyNumericField.Forecast -> DailyFieldColors.forecast
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun submit() {
        onConfirm(text.filter { it.isDigit() })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 292.dp)
                .fillMaxWidth(0.58f)
                .shadow(18.dp, RoundedCornerShape(14.dp), spotColor = fieldColors.accent.copy(alpha = 0.45f))
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, fieldColors.border.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    fieldColors.accent.copy(alpha = 0.95f),
                                    fieldColors.accent,
                                    fieldColors.accent.copy(alpha = 0.88f),
                                ),
                            ),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                                    radius = 280f,
                                ),
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.3f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                when (target.field) {
                                    DailyNumericField.ConfirmedBoxes -> "箱"
                                    DailyNumericField.ConfirmedUnits -> "本"
                                    DailyNumericField.Forecast -> "示"
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${target.field.label}数 入力",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                            )
                            Text(
                                "${row.productName} · ${OrderDailyUiMapper.formatShipDate(row.shipDate)} · ${target.rowIndex + 1}/$rowCount",
                                color = Color.White.copy(alpha = 0.86f),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        DailyHeaderCloseBtn(onClick = onDismiss)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .height(38.dp)
                            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = fieldColors.accent.copy(alpha = 0.15f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(fieldColors.bg, Color.White),
                                ),
                            )
                            .border(1.5.dp, fieldColors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            fontFamily = FontFamily.Monospace,
                            color = fieldColors.accent,
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                if (text.isEmpty()) {
                                    Text(
                                        "0",
                                        color = fieldColors.border.copy(alpha = 0.55f),
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                }
                                inner()
                            }
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "数字のみ · Enter→次行",
                            fontSize = 9.sp,
                            color = OrderMonthlyColors.TextMuted,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        NumericDialogBtn(
                            label = "取消",
                            accent = false,
                            fieldColors = fieldColors,
                            onClick = onDismiss,
                        )
                        NumericDialogBtn(
                            label = "確定",
                            accent = true,
                            fieldColors = fieldColors,
                            onClick = ::submit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericDialogBtn(
    label: String,
    accent: Boolean,
    fieldColors: DailyFieldColorSet,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .widthIn(min = 56.dp)
            .then(
                if (accent) {
                    Modifier.shadow(4.dp, RoundedCornerShape(7.dp), spotColor = fieldColors.accent.copy(alpha = 0.35f))
                } else {
                    Modifier
                },
            )
            .clip(RoundedCornerShape(7.dp))
            .then(
                if (accent) {
                    Modifier.background(
                        Brush.linearGradient(
                            listOf(fieldColors.accent.copy(alpha = 0.9f), fieldColors.accent),
                        ),
                    )
                } else {
                    Modifier
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, OrderMonthlyColors.BorderLight, RoundedCornerShape(7.dp))
                },
            )
            .then(
                if (accent) {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.32f), RoundedCornerShape(7.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (accent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                        ),
                    ),
            )
        }
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (accent) FontWeight.Bold else FontWeight.Medium,
            color = if (accent) Color.White else OrderMonthlyColors.TextMuted,
        )
    }
}

@Composable
private fun DailyHeaderCell(
    text: String,
    width: Dp,
    align: DailyColAlign,
    accent: Color? = null,
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = DailyCellPadH),
        contentAlignment = align.box,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = accent ?: OrderMonthlyColors.TextPrimary,
            textAlign = align.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DailyTextCell(
    text: String,
    width: Dp,
    align: DailyColAlign,
    bold: Boolean = false,
    color: Color = OrderMonthlyColors.TextPrimary,
    monospace: Boolean = false,
) {
    Box(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = DailyRowHeight)
            .padding(horizontal = DailyCellPadH, vertical = 2.dp),
        contentAlignment = align.box,
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 10.5.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
            textAlign = align.text,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DailyTapNumericCell(
    value: String,
    width: Dp,
    field: DailyNumericField,
    changed: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    val colors = when (field) {
        DailyNumericField.ConfirmedBoxes -> DailyFieldColors.boxes
        DailyNumericField.ConfirmedUnits -> DailyFieldColors.units
        DailyNumericField.Forecast -> DailyFieldColors.forecast
    }
    val display = value.ifBlank { "—" }
    Box(
        modifier = Modifier
            .width(width)
            .defaultMinSize(minHeight = DailyRowHeight)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .shadow(
                    if (active) 4.dp else 1.dp,
                    RoundedCornerShape(6.dp),
                    spotColor = colors.accent.copy(alpha = 0.25f),
                )
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (active) {
                        Brush.linearGradient(listOf(colors.bg, colors.border.copy(alpha = 0.35f)))
                    } else {
                        Brush.linearGradient(listOf(colors.bg, colors.bg))
                    },
                )
                .border(
                    width = if (active) 1.5.dp else 1.dp,
                    color = when {
                        active -> colors.accent
                        changed -> Color(0xFFF97316)
                        else -> colors.border
                    },
                    shape = RoundedCornerShape(6.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = display,
                fontSize = 10.5.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                color = if (value.isBlank()) colors.border else colors.accent,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun weekdayColor(weekday: String?): Color = when (weekday) {
    "土" -> Color(0xFF2563EB)
    "日" -> Color(0xFFDC2626)
    else -> OrderMonthlyColors.TextPrimary
}
