package com.example.smart_emap.ui.mes.planinstruction

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.PlanInstructionRecordDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
private val TableMaxHeight = 245.dp
private val ControlHeight = 34.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanInstructionPageContent(
    config: PlanInstructionConfig,
    uiState: PlanInstructionUiState,
    viewModel: PlanInstructionViewModel,
) {
    PlanInstructionHeader(
        config = config,
        stats = uiState.planStats,
        actionLoading = uiState.actionLoading,
        onRefresh = viewModel::refreshPlanData,
    )
    PlanInstructionPlanCard(
        config = config,
        uiState = uiState,
        viewModel = viewModel,
    )
    PlanInstructionChartCard(
        config = config,
        uiState = uiState,
        onChartMonth = viewModel::setChartMonth,
        onChartStartChange = { start ->
            viewModel.setChartDateRange(start, uiState.chartEndDate)
        },
        onChartEndChange = { end ->
            viewModel.setChartDateRange(uiState.chartStartDate, end)
        },
    )
}

@Composable
private fun PlanInstructionHeader(
    config: PlanInstructionConfig,
    stats: PlanInstructionStats,
    actionLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(10.dp), ambientColor = PlanInstructionTheme.CardShadow),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.CardBorder),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(PlanInstructionTheme.HeaderTopGradient),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = config.headerAccent,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            config.pageTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PlanInstructionTheme.TitleDark,
                        )
                    }
                    Text(
                        config.pageSubtitle,
                        fontSize = 11.sp,
                        color = PlanInstructionTheme.Subtitle,
                        modifier = Modifier.padding(top = 2.dp, start = 28.dp),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeaderStatChip(
                        icon = Icons.Default.TrendingUp,
                        iconBrush = PlanInstructionTheme.StatTotalGradient,
                        value = PlanInstructionLogic.formatNumber(stats.totalQuantity),
                        label = "計画生産数",
                    )
                    HeaderStatChip(
                        icon = Icons.Default.Monitor,
                        iconBrush = PlanInstructionTheme.StatMachineGradient,
                        value = stats.machineCount.toString(),
                        label = "稼働設備",
                    )
                    GradientToolbarButton(
                        text = "データ更新",
                        icon = Icons.Default.Refresh,
                        loading = actionLoading,
                        brush = PlanInstructionTheme.BtnRefreshGradient,
                        contentColor = Color.White,
                        onClick = onRefresh,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBrush: Brush,
    value: String,
    label: String,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.CardBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            Column {
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PlanInstructionTheme.TitleDark)
                Text(label, fontSize = 9.sp, color = PlanInstructionTheme.Subtitle)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanInstructionPlanCard(
    config: PlanInstructionConfig,
    uiState: PlanInstructionUiState,
    viewModel: PlanInstructionViewModel,
) {
    var machineExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = PlanInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.CardBorder),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, spotColor = PlanInstructionTheme.CardShadow)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                PlanInstructionTheme.SearchBarBgStart,
                                PlanInstructionTheme.SearchBarBgEnd,
                                Color(0xFFF8FAFC),
                            ),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                PlanInstructionTheme.SearchBarBorderStart,
                                PlanInstructionTheme.SearchBarBorderEnd,
                                PlanInstructionTheme.FilterBorder,
                            ),
                        ),
                        shape = RectangleShape,
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                val toolbarScroll = rememberScrollState()
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ControlHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(ControlHeight)
                            .horizontalScroll(toolbarScroll),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ToolbarDateLabel()
                        CompactDateChip(
                            label = PlanInstructionLogic.formatShortDate(uiState.selectedDate),
                            onClick = { showDatePicker = true },
                        )
                        GradientNavButton("前日", PlanInstructionTheme.DatePrevGradient) { viewModel.shiftDate(-1) }
                        GradientNavButton("今日", PlanInstructionTheme.DateTodayGradient) {
                            viewModel.setDate(PlanInstructionLogic.todayIso())
                        }
                        GradientNavButton("翌日", PlanInstructionTheme.DateNextGradient) { viewModel.shiftDate(1) }

                        ExposedDropdownMenuBox(
                            expanded = machineExpanded,
                            onExpandedChange = { machineExpanded = it },
                            modifier = Modifier.toolbarControlHeight(),
                        ) {
                            FilterChip(
                                label = uiState.machineFilter.ifBlank { "設備選択" },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxHeight()
                                    .heightIn(max = ControlHeight),
                                onClick = { machineExpanded = true },
                            )
                            ExposedDropdownMenu(
                                expanded = machineExpanded,
                                onDismissRequest = { machineExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("すべて") },
                                    onClick = {
                                        viewModel.setMachine(null)
                                        machineExpanded = false
                                    },
                                )
                                uiState.machines.forEach { machine ->
                                    val name = machine.machineName.orEmpty()
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            viewModel.setMachine(name)
                                            machineExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        ToolbarInlineField(
                            value = uiState.keyword,
                            onValueChange = viewModel::setKeyword,
                            placeholder = "製品名・設備名検索",
                            leadingIcon = Icons.Default.Search,
                            modifier = Modifier.widthIn(min = 150.dp, max = 200.dp),
                        )

                        GradientToolbarButton(
                            text = "指示書発行",
                            icon = Icons.Default.Download,
                            loading = uiState.actionLoading,
                            brush = PlanInstructionTheme.BtnIssueGradient,
                            contentColor = Color.White,
                            onClick = viewModel::printInstructions,
                        )

                        if (config.showSetupSchedulePreview) {
                            GradientToolbarButton(
                                text = "段取表プレビュー",
                                loading = uiState.actionLoading,
                                brush = PlanInstructionTheme.BtnPrimaryGradient,
                                contentColor = Color.White,
                                onClick = viewModel::openSetupPreview,
                            )
                        }

                        GradientToolbarButton(
                            text = if (config.showSetupSchedulePreview) "段取表印刷" else "段取予定発行",
                            icon = Icons.Default.Print,
                            loading = uiState.actionLoading,
                            brush = PlanInstructionTheme.BtnPrimaryGradient,
                            contentColor = Color.White,
                            onClick = viewModel::printSetupSchedule,
                        )

                        if (config.showSpecifiedWorkingDays) {
                            ToolbarWorkingDaysStepper(
                                value = uiState.specifiedWorkingDays,
                                onValueChange = viewModel::setSpecifiedWorkingDays,
                                onStep = viewModel::stepSpecifiedWorkingDays,
                                modifier = Modifier.width(120.dp),
                            )
                        }

                        if (config.showEfficiencyUpdate) {
                            GradientToolbarButton(
                                text = "能率・段取時間更新",
                                brush = Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
                                contentColor = Color(0xFFB45309),
                                onClick = viewModel::openEfficiencyUpdate,
                            )
                        }
                    }
                    NotesToolbarButton(
                        count = uiState.notesCount,
                        onClick = viewModel::openNotes,
                    )
                }
                }
            }

            HorizontalDivider(color = PlanInstructionTheme.TableBorder.copy(alpha = 0.35f))

            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                if (uiState.planLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(TableMaxHeight),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                } else {
                    PlanInstructionPlanTable(
                        rows = uiState.planRows,
                        remarksForRow = viewModel::remarksForRow,
                        onRemarksChange = viewModel::onRemarksChange,
                        onRemarksCommit = { row -> viewModel.saveRemarks(row, showSuccessMessage = true) },
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        OrderDailyDatePickerDialog(
            value = uiState.selectedDate,
            accent = config.headerAccent,
            onDismiss = { showDatePicker = false },
            onConfirm = viewModel::setDate,
        )
    }
}

@Composable
private fun PlanInstructionPlanTable(
    rows: List<PlanInstructionRecordDto>,
    remarksForRow: (PlanInstructionRecordDto) -> String,
    onRemarksChange: (PlanInstructionRecordDto, String) -> Unit,
    onRemarksCommit: (PlanInstructionRecordDto) -> Unit,
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()
    val tableWidth = 980.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(TableMaxHeight)
            .border(1.dp, PlanInstructionTheme.TableBorder, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Row(modifier = Modifier.horizontalScroll(hScroll)) {
            PlanTableHeaderRow(modifier = Modifier.width(tableWidth))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(vScroll)
                .horizontalScroll(hScroll),
        ) {
            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .width(tableWidth)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "該当する計画データがありません",
                        color = PlanInstructionTheme.Subtitle,
                        fontSize = 13.sp,
                    )
                }
            } else {
                rows.take(200).forEachIndexed { index, row ->
                    val highlightProduct = PlanInstructionLogic.isHighlightProduct(row.productName)
                    val bg = when {
                        highlightProduct -> PlanInstructionTheme.HighlightRowBg
                        index % 2 == 1 -> PlanInstructionTheme.TableStripeBg
                        else -> Color.White
                    }
                    PlanTableDataRow(
                        row = row,
                        bg = bg,
                        remarks = remarksForRow(row),
                        remarksPlaceholder = PlanInstructionLogic.defaultRemarksPlaceholder(row.productName),
                        onRemarksChange = { onRemarksChange(row, it) },
                        onRemarksCommit = { onRemarksCommit(row) },
                        modifier = Modifier.width(tableWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanTableHeaderRow(modifier: Modifier = Modifier) {
    val headers = listOf(
        "生産日" to 96.dp, "工程名" to 72.dp, "設備名" to 108.dp, "製品CD" to 88.dp,
        "製品名" to 140.dp, "生産順位" to 96.dp, "計画生産数" to 96.dp,
        "能率" to 72.dp, "段取時間" to 88.dp, "備考" to 200.dp,
    )
    Row(
        modifier = modifier
            .background(PlanInstructionTheme.TableHeaderBg)
            .padding(vertical = 8.dp),
    ) {
        headers.forEach { (label, width) ->
            Text(
                label,
                modifier = Modifier.width(width).padding(horizontal = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PlanInstructionTheme.TitleDark,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlanTableDataRow(
    row: PlanInstructionRecordDto,
    bg: Color,
    remarks: String,
    remarksPlaceholder: String,
    onRemarksChange: (String) -> Unit,
    onRemarksCommit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = PlanInstructionTheme.TitleDark
    val fontWeight = FontWeight.Normal
    val cells = listOf(
        row.planDate.orEmpty() to 96.dp,
        row.processName.orEmpty() to 72.dp,
        row.machineName.orEmpty() to 108.dp,
        row.productCd.orEmpty() to 88.dp,
        row.productName.orEmpty() to 140.dp,
        row.operator.orEmpty() to 96.dp,
        (row.quantity?.toString() ?: "") to 96.dp,
        PlanInstructionLogic.formatEfficiency(row.efficiencyRate) to 72.dp,
        (row.setupTime?.let { "${it}分" } ?: "-") to 88.dp,
    )
    Row(
        modifier = modifier
            .background(bg)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEach { (value, width) ->
            Text(
                value,
                modifier = Modifier.width(width).padding(horizontal = 4.dp),
                fontSize = 11.sp,
                fontWeight = fontWeight,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        BasicTextField(
            value = remarks,
            onValueChange = onRemarksChange,
            modifier = Modifier
                .width(200.dp)
                .padding(horizontal = 4.dp)
                .border(1.dp, PlanInstructionTheme.FilterBorder, RoundedCornerShape(6.dp))
                .background(Color.White, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = PlanInstructionTheme.TitleDark),
            singleLine = true,
            decorationBox = { inner ->
                if (remarks.isEmpty()) {
                    Text(
                        remarksPlaceholder,
                        fontSize = 11.sp,
                        color = PlanInstructionTheme.Subtitle.copy(alpha = 0.65f),
                    )
                }
                inner()
            },
        )
    }
}

@Composable
private fun ChartCardHeaderBar(
    chartStartDate: String,
    chartEndDate: String,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onChartMonth: (Int) -> Unit,
) {
    val chartToolbarScroll = rememberScrollState()
    val iconShape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, spotColor = PlanInstructionTheme.CardShadow)
            .background(
                Brush.linearGradient(
                    listOf(
                        PlanInstructionTheme.SearchBarBgStart,
                        PlanInstructionTheme.SearchBarBgEnd,
                        Color(0xFFF8FAFC),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        PlanInstructionTheme.SearchBarBorderStart,
                        PlanInstructionTheme.SearchBarBorderEnd,
                        PlanInstructionTheme.FilterBorder,
                    ),
                ),
                shape = RectangleShape,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ControlHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(ControlHeight)
                            .shadow(
                                elevation = 2.dp,
                                shape = iconShape,
                                spotColor = Color(0xFF3B82F6).copy(alpha = 0.25f),
                            )
                            .clip(iconShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
                                ),
                            )
                            .border(1.dp, Color(0xFF93C5FD).copy(alpha = 0.5f), iconShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        "日別計画・実績生産数",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        color = PlanInstructionTheme.TitleDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "計画・実績を日付ごとに合計表示",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = PlanInstructionTheme.Subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
                Row(
                    modifier = Modifier
                        .height(ControlHeight)
                        .horizontalScroll(chartToolbarScroll),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompactDateChip(
                        label = PlanInstructionLogic.formatShortDate(chartStartDate),
                        onClick = onStartClick,
                    )
                    ChartRangeSeparator()
                    CompactDateChip(
                        label = PlanInstructionLogic.formatShortDate(chartEndDate),
                        onClick = onEndClick,
                    )
                    GradientNavButton("前月", PlanInstructionTheme.DatePrevGradient) { onChartMonth(-1) }
                    GradientNavButton("今月", PlanInstructionTheme.DateTodayGradient) { onChartMonth(0) }
                    GradientNavButton("翌月", PlanInstructionTheme.DateNextGradient) { onChartMonth(1) }
                }
            }
        }
    }
}

@Composable
private fun ChartRangeSeparator() {
    Box(
        modifier = Modifier
            .toolbarControlHeight()
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "〜",
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            color = PlanInstructionTheme.Subtitle,
        )
    }
}

@Composable
private fun PlanInstructionChartCard(
    config: PlanInstructionConfig,
    uiState: PlanInstructionUiState,
    onChartMonth: (Int) -> Unit,
    onChartStartChange: (String) -> Unit,
    onChartEndChange: (String) -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = PlanInstructionTheme.CardShadow),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.98f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PlanInstructionTheme.CardBorder),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            ChartCardHeaderBar(
                chartStartDate = uiState.chartStartDate,
                chartEndDate = uiState.chartEndDate,
                onStartClick = { showStartPicker = true },
                onEndClick = { showEndPicker = true },
                onChartMonth = onChartMonth,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = PlanInstructionTheme.TableBorder.copy(alpha = 0.5f),
            )
            if (uiState.chartLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(320.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            } else if (!PlanInstructionLogic.chartHasData(uiState.chartPoints)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "該当期間に計画・実績の数量がありません",
                        color = PlanInstructionTheme.Subtitle,
                        fontSize = 13.sp,
                    )
                }
            } else {
                PlanQtyBarChart(
                    points = uiState.chartPoints,
                    todayIso = PlanInstructionLogic.todayIso(),
                    modifier = Modifier.fillMaxWidth().height(340.dp),
                )
            }
        }
    }

    if (showStartPicker) {
        OrderDailyDatePickerDialog(
            value = uiState.chartStartDate,
            accent = config.headerAccent,
            onDismiss = { showStartPicker = false },
            onConfirm = onChartStartChange,
        )
    }
    if (showEndPicker) {
        OrderDailyDatePickerDialog(
            value = uiState.chartEndDate,
            accent = config.headerAccent,
            onDismiss = { showEndPicker = false },
            onConfirm = onChartEndChange,
        )
    }
}

@Composable
fun PlanQtyBarChart(
    points: List<PlanQtyChartPoint>,
    todayIso: String = PlanInstructionLogic.todayIso(),
    modifier: Modifier = Modifier,
) {
    val visible = points.filter { it.planQty > 0 || it.actualQty > 0 }
    if (visible.isEmpty()) return

    val dataMax = visible.maxOf { maxOf(it.planQty, it.actualQty) }.coerceAtLeast(1)
    val axisMax = computeChartAxisMax(dataMax)
    val yTicks = buildChartYTicks(axisMax)
    val today = todayIso.take(10)
    val todayIndex = visible.indexOfFirst { it.date.take(10) == today }
    val rotateXLabels = visible.size > 14

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ChartLegend("計画生産数", Color(0xFF0284C7))
            Box(modifier = Modifier.width(20.dp))
            ChartLegend("実績生産数", Color(0xFF22C55E))
        }
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .width(44.dp)
                    .fillMaxHeight()
                    .padding(end = 4.dp, top = 2.dp),
            ) {
                Text(
                    "数量",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        yTicks.asReversed().forEach { tick ->
                            Text(
                                PlanInstructionLogic.formatNumber(tick),
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val labelTop = 14.dp.toPx()
                    val chartTop = labelTop + 6.dp.toPx()
                    val chartBottom = size.height - 2.dp.toPx()
                    val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
                    val groupWidth = size.width / visible.size.coerceAtLeast(1)
                    val barWidth = minOf(22.dp.toPx(), groupWidth * 0.34f).coerceAtLeast(6f)
                    val barGap = barWidth * 0.12f
                    val corner = 6.dp.toPx()

                    yTicks.forEach { tick ->
                        val ratio = tick.toFloat() / axisMax.coerceAtLeast(1)
                        val y = chartBottom - ratio * chartHeight
                        drawLine(
                            color = Color(0xFFE8ECF1),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                        )
                    }

                    drawLine(
                        color = Color(0xFFE2E8F0),
                        start = Offset(0f, chartBottom),
                        end = Offset(size.width, chartBottom),
                        strokeWidth = 1f,
                    )

                    if (todayIndex >= 0) {
                        val cx = groupWidth * todayIndex + groupWidth / 2f
                        drawLine(
                            color = Color(0xEBF59E0B),
                            start = Offset(cx, chartTop),
                            end = Offset(cx, chartBottom),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f)),
                        )
                        drawTodayBadge(cx = cx, top = 2.dp.toPx())
                    }

                    visible.forEachIndexed { index, point ->
                        val date = point.date.take(10)
                        val centerX = groupWidth * index + groupWidth / 2f
                        val planLeft = centerX - barGap / 2f - barWidth
                        val actualLeft = centerX + barGap / 2f
                        val planH = barHeight(point.planQty, axisMax, chartHeight)
                        val actualH = barHeight(point.actualQty, axisMax, chartHeight)

                        if (point.planQty > 0) {
                            drawTopRoundedBar(
                                brush = chartPlanBrush(date, today),
                                left = planLeft,
                                top = chartBottom - planH,
                                width = barWidth,
                                height = planH,
                                radius = corner,
                            )
                            drawChartValueLabel(
                                text = PlanInstructionLogic.formatNumber(point.planQty),
                                x = planLeft + barWidth / 2f,
                                y = chartBottom - planH - 3.dp.toPx(),
                                color = Color(0xFF475569),
                            )
                        }
                        if (point.actualQty > 0) {
                            drawTopRoundedBar(
                                brush = chartActualBrush(date, today),
                                left = actualLeft,
                                top = chartBottom - actualH,
                                width = barWidth,
                                height = actualH,
                                radius = corner,
                            )
                            drawChartValueLabel(
                                text = PlanInstructionLogic.formatNumber(point.actualQty),
                                x = actualLeft + barWidth / 2f,
                                y = chartBottom - actualH - 3.dp.toPx(),
                                color = Color(0xFF92400E),
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                ) {
                    visible.forEach { point ->
                        val dateLabel = PlanInstructionLogic.formatShortDate(point.date)
                        Text(
                            dateLabel,
                            modifier = Modifier
                                .weight(1f)
                                .graphicsLayer {
                                    rotationZ = if (rotateXLabels) -40f else 0f
                                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                                }
                                .padding(top = if (rotateXLabels) 8.dp else 0.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private fun computeChartAxisMax(dataMax: Int): Int {
    if (dataMax <= 0) return 10
    val exp = floor(log10(dataMax.toDouble())).toInt()
    val pow = 10.0.pow(exp).toInt().coerceAtLeast(1)
    val fraction = dataMax.toDouble() / pow
    val niceFraction = when {
        fraction <= 1 -> 1
        fraction <= 2 -> 2
        fraction <= 5 -> 5
        else -> 10
    }
    return niceFraction * pow
}

private fun buildChartYTicks(axisMax: Int): List<Int> {
    if (axisMax <= 0) return listOf(0)
    val divisions = 5
    val step = (axisMax / divisions).coerceAtLeast(1)
    val ticks = mutableListOf(0)
    var value = step
    while (value < axisMax) {
        ticks += value
        value += step
    }
    if (ticks.lastOrNull() != axisMax) {
        ticks += axisMax
    }
    return ticks
}

private fun barHeight(qty: Int, axisMax: Int, chartHeight: Float): Float {
    if (qty <= 0) return 0f
    return (qty.toFloat() / axisMax.coerceAtLeast(1) * chartHeight).coerceAtLeast(2f)
}

private fun chartPlanBrush(date: String, today: String): Brush = when {
    date < today -> Brush.verticalGradient(listOf(Color(0xFFBAE6FD), Color(0xFF0284C7)))
    date == today -> Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color(0xFF6EE7B7),
            0.55f to Color(0xFF34D399),
            1f to Color(0xFF047857),
        ),
    )
    else -> Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8)))
}

private fun chartActualBrush(date: String, today: String): Brush = when {
    date < today -> Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFD97706)))
    date == today -> Brush.verticalGradient(
        colorStops = arrayOf(
            0f to Color(0xFFFCD34D),
            0.55f to Color(0xFFF59E0B),
            1f to Color(0xFFB45309),
        ),
    )
    else -> Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFD6D3D1)))
}

private fun DrawScope.drawTopRoundedBar(
    brush: Brush,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    radius: Float,
) {
    if (height <= 0f) return
    val r = radius.coerceAtMost(minOf(width / 2f, height))
    val path = Path().apply {
        moveTo(left, top + height)
        lineTo(left, top + r)
        arcTo(
            rect = Rect(left, top, left + 2f * r, top + 2f * r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(left + width - r, top)
        arcTo(
            rect = Rect(left + width - 2f * r, top, left + width, top + 2f * r),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false,
        )
        lineTo(left + width, top + height)
        close()
    }
    drawPath(path, brush)
}

private fun DrawScope.drawChartValueLabel(
    text: String,
    x: Float,
    y: Float,
    color: Color,
) {
    if (text.isEmpty()) return
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        textSize = 8.sp.toPx()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.drawTodayBadge(cx: Float, top: Float) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xFFC2410C).toArgb()
        textSize = 10.sp.toPx()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val text = "今日"
    val textWidth = paint.measureText(text)
    val padH = 8.dp.toPx()
    val padV = 2.dp.toPx()
    val rect = RectF(
        cx - textWidth / 2f - padH,
        top,
        cx + textWidth / 2f + padH,
        top + paint.textSize + padV * 2f,
    )
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color(0xF2FEF3C7).toArgb()
    }
    drawContext.canvas.nativeCanvas.drawRoundRect(rect, 6.dp.toPx(), 6.dp.toPx(), bgPaint)
    drawContext.canvas.nativeCanvas.drawText(text, cx, rect.bottom - padV - 1.dp.toPx(), paint)
}

@Composable
private fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val ToolbarFieldShape = RoundedCornerShape(10.dp)
private val ToolbarButtonShape = RoundedCornerShape(9.dp)

private fun Modifier.toolbarControlHeight(): Modifier = this
    .requiredHeight(ControlHeight)
    .defaultMinSize(minWidth = 0.dp, minHeight = ControlHeight)
    .heightIn(max = ControlHeight)

private fun Modifier.toolbarElevatedField(
    shape: RoundedCornerShape = ToolbarFieldShape,
    accent: Color = PlanInstructionTheme.FilterBorder,
): Modifier = this
    .toolbarControlHeight()
    .shadow(
        elevation = 2.dp,
        shape = shape,
        spotColor = PlanInstructionTheme.ToolbarFieldShadow,
        ambientColor = PlanInstructionTheme.CardShadow,
    )
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(PlanInstructionTheme.ToolbarFieldTop, PlanInstructionTheme.ToolbarFieldBottom),
        ),
        shape,
    )
    .border(1.dp, accent.copy(alpha = 0.55f), shape)

@Composable
private fun ToolbarDateLabel() {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                spotColor = PlanInstructionTheme.ToolbarLabelAccent.copy(alpha = 0.28f),
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        PlanInstructionTheme.ToolbarLabelBgStart,
                        PlanInstructionTheme.ToolbarLabelBgEnd,
                    ),
                ),
            )
            .border(1.dp, PlanInstructionTheme.ToolbarLabelAccent.copy(alpha = 0.35f), shape)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "生産日",
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PlanInstructionTheme.ToolbarLabelText,
            maxLines = 1,
        )
    }
}

@Composable
private fun ToolbarInlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    Row(
        modifier = modifier
            .toolbarElevatedField()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (leadingIcon != null) {
            Icon(
                leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = PlanInstructionTheme.Subtitle,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = PlanInstructionTheme.TitleDark,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = ControlHeight),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            placeholder,
                            fontSize = 12.sp,
                            color = PlanInstructionTheme.Subtitle.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun ToolbarWorkingDaysStepper(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .toolbarControlHeight()
            .toolbarElevatedField()
            .padding(horizontal = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WorkingDaysStepButton(
            icon = Icons.Default.ChevronLeft,
            contentDescription = "稼働日数を減らす",
            enabled = (value ?: 1) > 1,
            onClick = { onStep(-1) },
        )
        BasicTextField(
            value = value?.toString().orEmpty(),
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(2)
                onValueChange(
                    when {
                        digits.isEmpty() -> null
                        else -> digits.toIntOrNull()?.coerceIn(1, 31)
                    },
                )
            },
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 12.sp,
                lineHeight = 14.sp,
                color = PlanInstructionTheme.TitleDark,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(max = ControlHeight),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (value == null) {
                        Text(
                            "稼働日数",
                            fontSize = 11.sp,
                            color = PlanInstructionTheme.Subtitle.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    inner()
                }
            },
        )
        WorkingDaysStepButton(
            icon = Icons.Default.ChevronRight,
            contentDescription = "稼働日数を増やす",
            enabled = (value ?: 0) < 31,
            onClick = { onStep(1) },
        )
    }
}

@Composable
private fun WorkingDaysStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
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
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = if (enabled) {
                PlanInstructionTheme.TitleDark.copy(alpha = 0.75f)
            } else {
                PlanInstructionTheme.Subtitle.copy(alpha = 0.35f)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactDateChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ToolbarFieldShape,
        color = Color.Transparent,
        modifier = Modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = ToolbarFieldShape,
                spotColor = PlanInstructionTheme.ToolbarFieldShadow,
                ambientColor = PlanInstructionTheme.CardShadow,
            )
            .background(
                Brush.verticalGradient(
                    listOf(PlanInstructionTheme.ToolbarFieldTop, PlanInstructionTheme.ToolbarFieldBottom),
                ),
                ToolbarFieldShape,
            )
            .border(1.dp, Color(0xFF93C5FD).copy(alpha = 0.55f), ToolbarFieldShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF2563EB),
            )
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PlanInstructionTheme.TitleDark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradientNavButton(label: String, brush: Brush, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ToolbarButtonShape,
        color = Color.Transparent,
        modifier = Modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = ToolbarButtonShape,
                spotColor = Color(0x55000000),
                ambientColor = Color(0x33000000),
            )
            .background(brush, ToolbarButtonShape)
            .border(1.dp, Color.White.copy(alpha = 0.22f), ToolbarButtonShape),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
        ) {
            Text(
                label,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = ToolbarFieldShape,
                spotColor = PlanInstructionTheme.ToolbarFieldShadow,
            )
            .background(
                Brush.verticalGradient(
                    listOf(PlanInstructionTheme.ToolbarFieldTop, PlanInstructionTheme.ToolbarFieldBottom),
                ),
                ToolbarFieldShape,
            )
            .border(1.dp, PlanInstructionTheme.FilterBorder.copy(alpha = 0.65f), ToolbarFieldShape),
        shape = ToolbarFieldShape,
        color = Color.Transparent,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
        ) {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PlanInstructionTheme.TitleDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradientToolbarButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    loading: Boolean = false,
    brush: Brush,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = !loading,
        shape = ToolbarButtonShape,
        color = Color.Transparent,
        modifier = Modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = ToolbarButtonShape,
                spotColor = Color(0x50000000),
                ambientColor = Color(0x30000000),
            )
            .background(brush, ToolbarButtonShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), ToolbarButtonShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = contentColor)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = contentColor)
            }
            Text(
                text,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesToolbarButton(count: Int, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier
            .toolbarControlHeight()
            .shadow(
                elevation = 2.dp,
                shape = shape,
                spotColor = PlanInstructionTheme.NotesBtnText.copy(alpha = 0.25f),
            )
            .background(PlanInstructionTheme.NotesBtnGradient, shape)
            .border(1.dp, PlanInstructionTheme.NotesBtnBorder, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.NoteAdd,
                contentDescription = "メモ",
                tint = PlanInstructionTheme.NotesBtnText,
                modifier = Modifier.size(14.dp),
            )
            if (count > 0) {
                Text(
                    count.toString(),
                    color = PlanInstructionTheme.NotesBadge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-9).dp),
                )
            }
        }
    }
}
