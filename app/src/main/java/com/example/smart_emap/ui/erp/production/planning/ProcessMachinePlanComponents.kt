package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.data.model.ProcessMachineMetricsDto
import com.example.smart_emap.data.model.ProcessMachinePlanRowDto
import com.example.smart_emap.data.model.ProcessMachineProductsDataDto

private object PmpColors {
    val Blue = Color(0xFF3B82F6)
    val Green = Color(0xFF22C55E)
    val Purple = Color(0xFF8B5CF6)
    val Amber = Color(0xFFF59E0B)
    val Red = Color(0xFFEF4444)
    val Ink = Color(0xFF334155)
    val Sub = Color(0xFF64748B)
    val Line = Color(0xFFE2E8F0)
    val PageBg = Brush.linearGradient(listOf(Color(0xFFEEF2F8), Color(0xFFE8EDF4)))
    val ToolbarBg = Brush.verticalGradient(listOf(Color(0xFFFCFDFE), Color(0xFFF1F5F9)))
    val CardBorder = Color(0xFFE2E8F0)
    val SubtotalBg = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEEF2F7)))
    val GrandBg = Color(0xFFEFF6FF)
    val TableHeaderBg = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE8EEF5)))
    val StripBg = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEEF2F7)))
    val ViewStripBg = Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF)))
}

@Composable
fun ProcessMachinePlanPageBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(PmpColors.PageBg)) { content() }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProcessMachinePlanToolbar(
    startDate: String,
    endDate: String,
    selectedProcesses: Set<String>,
    selectedMachines: Set<String>,
    machineGroups: List<Pair<String, List<String>>>,
    viewMode: ProcessMachineViewMode,
    loading: Boolean,
    printEnabled: Boolean,
    exportEnabled: Boolean,
    onDateRangeChange: (String, String) -> Unit,
    onThisMonth: () -> Unit,
    onLastMonth: () -> Unit,
    onToggleProcess: (String) -> Unit,
    onToggleMachine: (String) -> Unit,
    onClearMachines: () -> Unit,
    onViewModeChange: (ProcessMachineViewMode) -> Unit,
    onRefresh: () -> Unit,
    onPrint: () -> Unit,
    onExport: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, spotColor = Color(0x1A3B82F6))
            .clip(shape)
            .background(PmpColors.ToolbarBg)
            .border(1.dp, PmpColors.CardBorder, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.widthIn(max = 180.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(Brush.verticalGradient(listOf(PmpColors.Blue, PmpColors.Purple)), RoundedCornerShape(2.dp)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("工程別設備別計画", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PmpColors.Ink)
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFEEF2FF), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8E2EC))) {
                    Text(
                        "$startDate ～ $endDate",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = PmpColors.Sub,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PmpToolbarStrip(
                background = PmpColors.StripBg,
                borderColor = Color(0xFFD5DDE8),
            ) {
                Text("条件", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = PmpColors.Sub)
                ProductionCompactDateRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    onStartChange = { onDateRangeChange(it, endDate) },
                    onEndChange = { onDateRangeChange(startDate, it) },
                    modifier = Modifier.weight(1f, fill = false).widthIn(min = 160.dp, max = 220.dp),
                )
                PmpMiniButton("今月", onThisMonth)
                PmpMiniButton("先月", onLastMonth)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    ProcessMachinePlanLogic.allProcessOptions.forEach { (key, label) ->
                        PmpChip(label, selectedProcesses.contains(key)) { onToggleProcess(key) }
                    }
                }
                var machineDialog by remember { mutableStateOf(false) }
                Surface(
                    onClick = { machineDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PmpColors.CardBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp), tint = PmpColors.Sub)
                        Text(
                            if (selectedMachines.isEmpty()) "設備（全て）" else "設備 ${selectedMachines.size}件",
                            fontSize = 11.sp,
                            color = PmpColors.Ink,
                        )
                    }
                }
                if (selectedMachines.isNotEmpty()) {
                    TextButton(onClick = onClearMachines, contentPadding = PaddingValues(horizontal = 4.dp)) {
                        Text("クリア", fontSize = 10.sp)
                    }
                }
                if (machineDialog) {
                    ProcessMachineMachineFilterDialog(
                        groups = machineGroups,
                        selected = selectedMachines,
                        onToggle = onToggleMachine,
                        onDismiss = { machineDialog = false },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PmpToolbarStrip(
                    background = PmpColors.ViewStripBg,
                    borderColor = Color(0xFFC7D2FE),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    ProductionTabStrip(
                        ProcessMachineViewMode.entries.map { it.name to it.label },
                        viewMode.name,
                        { name -> ProcessMachineViewMode.entries.find { it.name == name }?.let(onViewModeChange) },
                    )
                }
                PmpToolbarStrip(
                    background = PmpColors.StripBg,
                    borderColor = Color(0xFFD5DDE8),
                ) {
                    PmpActionButton("更新", Icons.Default.Refresh, PmpColors.Blue, loading, onRefresh)
                    PmpActionButton("印刷", Icons.Default.Print, PmpColors.Amber, loading || !printEnabled, onPrint, isPrint = true)
                    PmpActionButton("Excel", Icons.Default.Download, PmpColors.Green, loading || !exportEnabled, onExport, isExcel = true)
                }
            }
        }
    }
}

@Composable
private fun PmpToolbarStrip(
    background: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}

@Composable
private fun PmpMiniButton(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(6.dp), color = Color(0xFFF1F5F9), border = androidx.compose.foundation.BorderStroke(1.dp, PmpColors.CardBorder)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PmpChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PmpColors.Blue else Color(0xFFF1F5F9),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) PmpColors.Blue else PmpColors.CardBorder),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else ProductionPlanningColors.TextPrimary,
        )
    }
}

@Composable
private fun PmpActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    disabled: Boolean,
    onClick: () -> Unit,
    isPrint: Boolean = false,
    isExcel: Boolean = false,
) {
    val containerColor = when {
        isPrint -> Color(0xFFFFFBEB)
        isExcel -> Color(0xFFF0FDF4)
        else -> color
    }
    val contentColor = when {
        isPrint -> Color(0xFFB45309)
        isExcel -> Color(0xFF15803D)
        else -> Color.White
    }
    val borderColor = when {
        isPrint -> Color(0xFFFCD34D)
        isExcel -> Color(0xFF86EFAC)
        else -> color.copy(alpha = 0.8f)
    }
    Button(
        onClick = onClick,
        enabled = !disabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        modifier = Modifier.height(30.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProcessMachineMachineFilterDialog(
    groups: List<Pair<String, List<String>>>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(modifier = Modifier.padding(14.dp).widthIn(max = 400.dp)) {
                Text("設備を選択", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    groups.forEach { (group, machines) ->
                        Text(group, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PmpColors.Purple)
                        machines.forEach { machine ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onToggle(machine) },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = selected.contains(machine), onCheckedChange = { onToggle(machine) })
                                Text(machine, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("閉じる") }
                }
            }
        }
    }
}

@Composable
fun ProcessMachinePlanKpiCards(grand: ProcessMachineMetricsDto?) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        PmpKpiCard("計画合計", formatProductionNumber(grand?.plan), PmpColors.Blue, Icons.Default.BarChart)
        PmpKpiCard("実績合計", formatProductionNumber(grand?.actual), PmpColors.Green, Icons.Default.CheckCircle)
        val diff = grand?.diff ?: 0
        PmpKpiCard(
            "差異（実績-計画）",
            formatProductionSigned(grand?.diff),
            if (diff >= 0) PmpColors.Green else PmpColors.Red,
            if (diff >= 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
        )
        PmpKpiCard(
            "達成率",
            formatProductionPercent(grand?.achievementRate),
            achievementRateColor(grand?.achievementRate),
            Icons.Default.TrendingUp,
        )
        PmpKpiCard("不良率", formatProductionPercent(grand?.defectRate), PmpColors.Red, Icons.Default.Warning)
    }
}

@Composable
private fun PmpKpiCard(label: String, value: String, accent: Color, icon: ImageVector) {
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier = Modifier
            .widthIn(min = 120.dp)
            .shadow(1.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
            .border(1.dp, PmpColors.CardBorder, shape)
            .padding(start = 11.dp, end = 10.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Brush.linearGradient(listOf(Color.White, accent.copy(alpha = 0.12f))))
                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp), tint = accent)
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, fontSize = 9.sp, color = PmpColors.Sub, maxLines = 1)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        }
    }
}

@Composable
fun ProcessMachinePlanPanel(
    title: String,
    hint: String? = null,
    extraHeader: @Composable (() -> Unit)? = null,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, PmpColors.CardBorder, shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color.White)))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PmpColors.Ink)
                if (!hint.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0DAE6)),
                    ) {
                        Text(
                            hint,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = PmpColors.Sub,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                extraHeader?.invoke()
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            }
        }
        HorizontalDivider(color = PmpColors.CardBorder)
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFD4DCE8), RoundedCornerShape(10.dp))
                .background(Color.White),
        ) {
            content()
        }
    }
}

@Composable
fun ProcessMachineSummaryTable(
    rows: List<ProcessMachinePlanLogic.TableRow>,
    onRowClick: (ProcessMachinePlanRowDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val headers = listOf("工程", "設備", "計画", "実績", "差異", "達成率", "実計", "不良", "廃棄", "不良率")
    val widths = listOf(72, 100, 56, 56, 56, 56, 56, 48, 48, 56)
    PmpScrollableTable(
        headers = headers,
        widths = widths,
        rowCount = rows.size,
        modifier = modifier,
        groupStartColumns = setOf(2, 6, 8),
        rowBackground = { index ->
            when (rows[index].type) {
                ProcessMachinePlanLogic.TableRowType.Grand -> PmpColors.GrandBg
                ProcessMachinePlanLogic.TableRowType.Subtotal -> Color(0xFFF8FAFC)
                else -> if (index % 2 == 0) Color.White else Color(0xFFFAFBFD)
            }
        },
        rowTopPadding = { index ->
            when {
                rows[index].type == ProcessMachinePlanLogic.TableRowType.Grand -> 16.dp
                rows[index].processStart && index > 0 -> 12.dp
                rows[index].type == ProcessMachinePlanLogic.TableRowType.Subtotal -> 14.dp
                else -> 0.dp
            }
        },
        hiddenCell = { rowIndex, colIndex ->
            rows[rowIndex].type == ProcessMachinePlanLogic.TableRowType.Grand && colIndex == 1
        },
        columnWidth = { rowIndex, colIndex ->
            if (rows[rowIndex].type == ProcessMachinePlanLogic.TableRowType.Grand && colIndex == 0) widths[0] + widths[1]
            else widths.getOrElse(colIndex) { 56 }
        },
        onRowClick = { index ->
            rows[index].sourceRow?.let(onRowClick)
        },
        clickableRow = { index -> rows[index].type == ProcessMachinePlanLogic.TableRowType.Machine },
    ) { index, col ->
        val row = rows[index]
        if (row.type == ProcessMachinePlanLogic.TableRowType.Grand && col == 1) return@PmpScrollableTable
        when (col) {
            0 -> {
                if (row.type == ProcessMachinePlanLogic.TableRowType.Grand) {
                    Box(Modifier.width((widths[0] + widths[1]).dp), contentAlignment = Alignment.Center) {
                        PmpTableCell(row.processLabel, PmpColors.Ink, FontWeight.ExtraBold, TextAlign.Center)
                    }
                } else if (row.type == ProcessMachinePlanLogic.TableRowType.Machine) {
                    PmpProcessChip(row.processLabel, isDaily = false)
                } else {
                    PmpTableCell(row.processLabel, PmpColors.Ink, FontWeight.Bold, TextAlign.Center, widths[0])
                }
            }
            1 -> {
                val machineText = row.machine
                val weight = if (row.type != ProcessMachinePlanLogic.TableRowType.Machine) FontWeight.Bold else FontWeight.Normal
                val color = if (row.type != ProcessMachinePlanLogic.TableRowType.Machine) Color(0xFF475467) else PmpColors.Ink
                PmpTableCell(machineText, color, weight, TextAlign.Center, widths[1])
            }
            2 -> PmpTableCell(formatProductionNumber(row.plan), PmpColors.Ink, rowWeight(row), TextAlign.End, widths[2])
            3 -> PmpTableCell(formatProductionNumber(row.actual), PmpColors.Ink, rowWeight(row), TextAlign.End, widths[3])
            4 -> PmpTableCell(formatProductionSigned(row.diff), diffValueColor(row.diff), rowWeight(row), TextAlign.End, widths[4])
            5 -> PmpTableCell(formatProductionPercent(row.achievementRate), achievementRateColor(row.achievementRate), FontWeight.Bold, TextAlign.End, widths[5])
            6 -> PmpTableCell(formatProductionNumber(row.actualPlan), Color(0xFF94A3B8), rowWeight(row), TextAlign.End, widths[6])
            7 -> PmpTableCell(formatProductionNumber(row.defect), Color(0xFF94A3B8), rowWeight(row), TextAlign.End, widths[7])
            8 -> PmpTableCell(formatProductionNumber(row.scrap), Color(0xFF94A3B8), rowWeight(row), TextAlign.End, widths[8])
            9 -> PmpTableCell(
                formatProductionPercent(row.defectRate),
                if ((row.defectRate ?: 0.0) >= 3) PmpColors.Red else PmpColors.Ink,
                FontWeight.Bold,
                TextAlign.End,
                widths[9],
            )
        }
    }
}

private fun rowWeight(row: ProcessMachinePlanLogic.TableRow): FontWeight =
    if (row.type != ProcessMachinePlanLogic.TableRowType.Machine) FontWeight.Bold else FontWeight.Normal

@Composable
private fun PmpProcessChip(label: String, isDaily: Boolean) {
    Surface(
        shape = RoundedCornerShape(if (isDaily) 5.dp else 6.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, PmpColors.Line),
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = if (isDaily) 8.dp else 10.dp, vertical = if (isDaily) 2.dp else 4.dp),
            fontSize = if (isDaily) 11.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            color = PmpColors.Ink,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ProcessMachineDailyTable(
    rows: List<ProcessMachinePlanLogic.TableRow>,
    dates: List<String>,
    metric: String,
    onRowClick: (ProcessMachinePlanRowDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayColWidth = 44
    val widths = listOf(64, 96) + List(dates.size) { dayColWidth } + listOf(56)
    val headerRow1 = listOf("工程", "設備") + dates.map { ProcessMachinePlanLogic.formatDateHeaderDate(it) } + listOf("合計")
    val headerRow2 = listOf("", "") + dates.map { ProcessMachinePlanLogic.formatDateHeaderWeek(it) } + listOf("")
    val hScroll = rememberScrollState()
    Column(modifier = modifier.fillMaxSize()) {
        Row(Modifier.horizontalScroll(hScroll)) {
            Column {
                Row(Modifier.background(PmpColors.TableHeaderBg).padding(vertical = 4.dp)) {
                    headerRow1.forEachIndexed { i, h ->
                        val groupStart = i == 2
                        PmpHeaderCell(h, widths.getOrElse(i) { 48 }.dp, groupStart = groupStart)
                    }
                }
                Row(Modifier.background(Color(0xFFF8FAFC)).padding(vertical = 3.dp)) {
                    headerRow2.forEachIndexed { i, h ->
                        val weekend = if (i >= 2 && i < 2 + dates.size) ProcessMachinePlanLogic.isWeekend(dates[i - 2]) else false
                        PmpHeaderCell(
                            h,
                            widths.getOrElse(i) { 48 }.dp,
                            if (weekend) PmpColors.Red else PmpColors.Sub,
                            groupStart = i == 2,
                        )
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(rows, key = { idx, row -> "${row.processLabel}_${row.machine}_$idx" }) { index, row ->
                val bg = when (row.type) {
                    ProcessMachinePlanLogic.TableRowType.Subtotal -> Color(0xFFF8FAFC)
                    else -> if (index % 2 == 0) Color.White else Color(0xFFFAFBFD)
                }
                val topPad = when {
                    row.processStart && index > 0 -> 10.dp
                    row.type == ProcessMachinePlanLogic.TableRowType.Subtotal -> 12.dp
                    else -> 0.dp
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(hScroll)
                        .background(bg)
                        .padding(top = topPad)
                        .then(
                            if (row.type == ProcessMachinePlanLogic.TableRowType.Machine && row.sourceRow != null) {
                                Modifier.clickable { row.sourceRow?.let(onRowClick) }
                            } else Modifier,
                        )
                        .padding(vertical = 4.dp),
                ) {
                    Box(Modifier.width(64.dp), contentAlignment = Alignment.Center) {
                        if (row.type == ProcessMachinePlanLogic.TableRowType.Machine) {
                            PmpProcessChip(row.processLabel, isDaily = true)
                        } else {
                            PmpTableCell(row.processLabel, PmpColors.Ink, FontWeight.Bold, TextAlign.Center, 64)
                        }
                    }
                    PmpTableCell(
                        row.machine,
                        if (row.type != ProcessMachinePlanLogic.TableRowType.Machine) Color(0xFF475467) else PmpColors.Ink,
                        if (row.type == ProcessMachinePlanLogic.TableRowType.Subtotal) FontWeight.Bold else FontWeight.Normal,
                        TextAlign.Center,
                        96,
                    )
                    dates.forEach { d ->
                        val v = row.dailyValues[d] ?: 0
                        PmpTableCell(
                            ProcessMachinePlanLogic.formatDailyCell(v, metric),
                            when {
                                v == 0 -> Color(0xFFCBD5E1)
                                metric == "diff" -> diffValueColor(v)
                                else -> PmpColors.Ink
                            },
                            FontWeight.Normal,
                            TextAlign.End,
                            dayColWidth,
                        )
                    }
                    PmpTableCell(
                        ProcessMachinePlanLogic.formatDailyCell(row.rowTotal, metric),
                        when {
                            row.rowTotal == 0 -> Color(0xFFCBD5E1)
                            metric == "diff" -> diffValueColor(row.rowTotal)
                            else -> PmpColors.Ink
                        },
                        FontWeight.Bold,
                        TextAlign.End,
                        56,
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessMachineTrendSection(
    trendGroup: String,
    stats: ProcessMachinePlanLogic.TrendStats?,
    dailyRows: List<ProcessMachinePlanLogic.TrendDailyRow>,
    processDayRows: List<ProcessMachinePlanLogic.TrendProcessDayRow>,
    processColumns: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        stats?.let { s ->
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PmpTrendKpi("期間計画", formatProductionNumber(s.totalPlan), PmpColors.Blue)
                PmpTrendKpi("期間実績", formatProductionNumber(s.totalActual), PmpColors.Green)
                PmpTrendKpi("平均達成率", formatProductionPercent(s.avgRate), achievementRateColor(s.avgRate), PmpColors.Purple)
                PmpTrendKpi(
                    "最高日",
                    s.bestDate?.let { "${ProcessMachinePlanLogic.formatDateHeaderDate(it)} ${formatProductionPercent(s.bestRate)}" } ?: "—",
                    PmpColors.Green,
                    Color(0xFF16A34A),
                )
                PmpTrendKpi(
                    "最低日",
                    s.worstDate?.let { "${ProcessMachinePlanLogic.formatDateHeaderDate(it)} ${formatProductionPercent(s.worstRate)}" } ?: "—",
                    PmpColors.Red,
                    PmpColors.Red,
                )
            }
        }
        ProcessMachineTrendChartCard(
            trendGroup = trendGroup,
            dailyRows = dailyRows,
            processDayRows = processDayRows,
            processColumns = processColumns,
        )
        if (dailyRows.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, PmpColors.Line, RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))),
            ) {
                Text(
                    "日別数値一覧",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color.White)))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PmpColors.Ink,
                )
                ProcessMachineTrendDailyTable(
                    trendGroup = trendGroup,
                    dailyRows = dailyRows,
                    processDayRows = processDayRows,
                    processColumns = processColumns,
                    modifier = Modifier.heightIn(max = 220.dp),
                )
            }
        }
    }
}

@Composable
private fun ProcessMachineTrendChartCard(
    trendGroup: String,
    dailyRows: List<ProcessMachinePlanLogic.TrendDailyRow>,
    processDayRows: List<ProcessMachinePlanLogic.TrendProcessDayRow>,
    processColumns: List<Pair<String, String>>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, PmpColors.Line, RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFFAFBFC)))),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color.White)))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (trendGroup == "all") "日別 計画・実績・達成率" else "工程別 日別達成率",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PmpColors.Ink,
            )
            Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFFFFFBEB), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))) {
                Text(
                    "100% 基準線あり",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB45309),
                )
            }
        }
        ProcessMachineTrendChart(
            trendGroup = trendGroup,
            dailyRows = dailyRows,
            processDayRows = processDayRows,
            processColumns = processColumns,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun PmpChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 10.sp, color = PmpColors.Sub)
    }
}

@Composable
private fun ProcessMachineTrendChart(
    trendGroup: String,
    dailyRows: List<ProcessMachinePlanLogic.TrendDailyRow>,
    processDayRows: List<ProcessMachinePlanLogic.TrendProcessDayRow>,
    processColumns: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    if (dailyRows.isEmpty()) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("データがありません", color = PmpColors.Sub, fontSize = 12.sp)
        }
        return
    }
    Column(modifier = modifier) {
        if (trendGroup == "all") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PmpChartLegend("計画", Color(0xFF60A5FA))
                Spacer(Modifier.width(12.dp))
                PmpChartLegend("実績", Color(0xFF22C55E))
                Spacer(Modifier.width(12.dp))
                PmpChartLegend("達成率", PmpColors.Purple)
            }
        }
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
        val leftPad = 42f
        val rightPad = if (trendGroup == "all") 36f else 12f
        val topPad = 28f
        val bottomPad = 36f
        val dates = dailyRows.map { it.date }
        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val groupWidth = chartWidth / dates.size.coerceAtLeast(1)

        if (trendGroup == "all") {
            val qtyMax = dailyRows.maxOf { maxOf(it.plan, it.actual) }.coerceAtLeast(1)
            val qtyAxisMax = (((qtyMax * 1.15).toInt() / 100) + 1) * 100
            val rates = dailyRows.map { it.rate }
            val rateAxisMax = ProcessMachinePlanLogic.trendRateAxisMax(rates).toFloat()
            val barWidth = minOf(28f, groupWidth * 0.28f).coerceAtLeast(6f)
            val barGap = barWidth * 0.12f

            for (tick in 0..4) {
                val y = chartBottom - chartHeight * tick / 4f
                drawLine(Color(0xFFF1F5F9), Offset(chartLeft, y), Offset(chartRight, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            }
            drawLine(Color(0xFFE2E8F0), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)

            val rate100Y = chartTop + chartHeight * (1f - 100f / rateAxisMax)
            drawLine(
                Color(0xFFF59E0B),
                Offset(chartLeft, rate100Y),
                Offset(chartRight, rate100Y),
                1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
            )

            dailyRows.forEachIndexed { index, row ->
                val centerX = chartLeft + groupWidth * index + groupWidth / 2f
                val planLeft = centerX - barGap / 2f - barWidth
                val actualLeft = centerX + barGap / 2f
                val planH = row.plan.toFloat() / qtyAxisMax * chartHeight
                val actualH = row.actual.toFloat() / qtyAxisMax * chartHeight
                if (row.plan > 0) {
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF93C5FD), Color(0xFF60A5FA))),
                        topLeft = Offset(planLeft, chartBottom - planH),
                        size = androidx.compose.ui.geometry.Size(barWidth, planH),
                    )
                }
                if (row.actual > 0) {
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFF86EFAC), Color(0xFF22C55E))),
                        topLeft = Offset(actualLeft, chartBottom - actualH),
                        size = androidx.compose.ui.geometry.Size(barWidth, actualH),
                    )
                }
            }

            val ratePoints = dailyRows.mapIndexed { index, row ->
                val x = chartLeft + groupWidth * index + groupWidth / 2f
                val rate = row.rate?.toFloat()
                val y = if (rate == null) null else chartTop + chartHeight * (1f - rate / rateAxisMax)
                x to y
            }
            ratePoints.zipWithNext { a, b ->
                if (a.second != null && b.second != null) {
                    drawLine(PmpColors.Purple, Offset(a.first, a.second!!), Offset(b.first, b.second!!), 3f)
                }
            }
            ratePoints.forEach { (x, y) ->
                if (y != null) {
                    drawCircle(Color.White, 5f, Offset(x, y))
                    drawCircle(PmpColors.Purple, 3.5f, Offset(x, y))
                }
            }
        } else {
            val allRates = processDayRows.flatMap { row -> row.rates.values.toList() }
            val rateAxisMax = ProcessMachinePlanLogic.trendRateAxisMax(allRates).toFloat()
            for (tick in 0..4) {
                val y = chartBottom - chartHeight * tick / 4f
                drawLine(Color(0xFFF1F5F9), Offset(chartLeft, y), Offset(chartRight, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            }
            drawLine(Color(0xFFE2E8F0), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)
            val rate100Y = chartTop + chartHeight * (1f - 100f / rateAxisMax)
            drawLine(
                Color(0xFFF59E0B),
                Offset(chartLeft, rate100Y),
                Offset(chartRight, rate100Y),
                1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
            )
            processColumns.forEachIndexed { procIndex, (key, _) ->
                val color = Color(ProcessMachinePlanLogic.trendProcessColors[procIndex % ProcessMachinePlanLogic.trendProcessColors.size])
                val points = processDayRows.mapIndexed { index, row ->
                    val x = chartLeft + groupWidth * index + groupWidth / 2f
                    val rate = row.rates[key]?.toFloat()
                    val y = if (rate == null) null else chartTop + chartHeight * (1f - rate / rateAxisMax)
                    x to y
                }
                points.zipWithNext { a, b ->
                    if (a.second != null && b.second != null) {
                        drawLine(color, Offset(a.first, a.second!!), Offset(b.first, b.second!!), 2.5f)
                    }
                }
                points.forEach { (x, y) ->
                    if (y != null) drawCircle(color, 3f, Offset(x, y))
                }
            }
        }
        }
    }
}

@Composable
private fun PmpTrendKpi(label: String, value: String, valueColor: Color, borderColor: Color = valueColor) {
    Row(
        modifier = Modifier
            .widthIn(min = 100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC))))
            .border(1.dp, PmpColors.Line, RoundedCornerShape(8.dp))
            .padding(start = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(44.dp)
                .background(borderColor),
        )
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PmpColors.Sub)
            Text(value, fontSize = if (value.length > 12) 12.sp else 15.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, maxLines = 1)
        }
    }
}

@Composable
private fun ProcessMachineTrendDailyTable(
    trendGroup: String,
    dailyRows: List<ProcessMachinePlanLogic.TrendDailyRow>,
    processDayRows: List<ProcessMachinePlanLogic.TrendProcessDayRow>,
    processColumns: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    if (trendGroup == "all") {
        val headers = listOf("日付", "曜", "計画", "実績", "差異", "達成率")
        val widths = listOf(72, 40, 64, 64, 64, 64)
        PmpScrollableTable(headers, widths, dailyRows.size, modifier = modifier) { index, col ->
            val row = dailyRows[index]
            val text = when (col) {
                0 -> ProcessMachinePlanLogic.formatDateHeaderDate(row.date)
                1 -> ProcessMachinePlanLogic.formatDateHeaderWeek(row.date)
                2 -> formatProductionNumber(row.plan)
                3 -> formatProductionNumber(row.actual)
                4 -> formatProductionSigned(row.diff)
                5 -> formatProductionPercent(row.rate)
                else -> ""
            }
            val color = when (col) {
                4 -> diffValueColor(row.diff)
                5 -> achievementRateColor(row.rate)
                1 -> if (ProcessMachinePlanLogic.isWeekend(row.date)) PmpColors.Red else ProductionPlanningColors.TextSecondary
                else -> ProductionPlanningColors.TextPrimary
            }
            PmpTableCell(text, color, FontWeight.Normal, if (col <= 1) TextAlign.Center else TextAlign.End)
        }
    } else {
        val headers = listOf("日付", "曜") + processColumns.map { it.second }
        val widths = listOf(72, 40) + List(processColumns.size) { 56 }
        PmpScrollableTable(headers, widths, processDayRows.size, modifier = modifier) { index, col ->
            val row = processDayRows[index]
            val text = when (col) {
                0 -> ProcessMachinePlanLogic.formatDateHeaderDate(row.date)
                1 -> ProcessMachinePlanLogic.formatDateHeaderWeek(row.date)
                else -> {
                    val key = processColumns.getOrNull(col - 2)?.first
                    formatProductionPercent(key?.let { row.rates[it] })
                }
            }
            val color = when (col) {
                1 -> if (ProcessMachinePlanLogic.isWeekend(row.date)) PmpColors.Red else ProductionPlanningColors.TextSecondary
                else -> if (col >= 2) achievementRateColor(processColumns.getOrNull(col - 2)?.first?.let { row.rates[it] }) else ProductionPlanningColors.TextPrimary
            }
            PmpTableCell(text, color, FontWeight.Normal, TextAlign.Center)
        }
    }
}

@Composable
fun ProcessMachineDrillDownDialog(
    title: String,
    data: ProcessMachineProductsDataDto?,
    loading: Boolean,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(0.95f).shadow(8.dp, RoundedCornerShape(12.dp)),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEEF2F7))))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PmpColors.Ink)
                }
                HorizontalDivider(color = PmpColors.Line)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    data?.total?.let { t ->
                        Row(Modifier.padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PmpDrillSummaryPill("計画", formatProductionNumber(t.plan))
                            PmpDrillSummaryPill("実績", formatProductionNumber(t.actual))
                            PmpDrillSummaryPill("達成率", formatProductionPercent(t.achievementRate))
                        }
                    }
                    if (loading) {
                        Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PmpColors.Blue)
                        }
                    } else {
                        val products = data?.products.orEmpty()
                        val headers = listOf("製品CD", "製品名", "計画", "実績", "差異", "達成率", "実計", "不良", "廃棄")
                        val widths = listOf(72, 100, 52, 52, 52, 52, 52, 44, 44)
                        PmpScrollableTable(
                            headers = headers,
                            widths = widths,
                            rowCount = products.size,
                            modifier = Modifier.heightIn(min = 120.dp, max = 320.dp),
                        ) { index, col ->
                            val p = products[index]
                            val text = when (col) {
                                0 -> p.productCd
                                1 -> p.productName.orEmpty()
                                2 -> formatProductionNumber(p.plan)
                                3 -> formatProductionNumber(p.actual)
                                4 -> formatProductionSigned(p.diff)
                                5 -> formatProductionPercent(p.achievementRate)
                                6 -> formatProductionNumber(p.actualPlan)
                                7 -> formatProductionNumber(p.defect)
                                8 -> formatProductionNumber(p.scrap)
                                else -> ""
                            }
                            val color = when (col) {
                                4 -> diffValueColor(p.diff)
                                5 -> achievementRateColor(p.achievementRate)
                                else -> PmpColors.Ink
                            }
                            PmpTableCell(text, color, FontWeight.Normal, if (col <= 1) TextAlign.Start else TextAlign.End, widths[col])
                        }
                    }
                }
                HorizontalDivider(color = PmpColors.Line)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (!loading && !data?.products.isNullOrEmpty()) {
                        TextButton(onClick = onExport) { Text("Excel", color = PmpColors.Green) }
                    }
                    TextButton(onClick = onDismiss) { Text("閉じる") }
                }
            }
        }
    }
}

@Composable
private fun PmpDrillSummaryPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, PmpColors.Line),
    ) {
        Text(
            "$label $value",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = PmpColors.Sub,
        )
    }
}

@Composable
private fun PmpScrollableTable(
    headers: List<String>,
    widths: List<Int>,
    rowCount: Int,
    modifier: Modifier = Modifier,
    groupStartColumns: Set<Int> = emptySet(),
    rowBackground: (Int) -> Color = { if (it % 2 == 0) Color.White else Color(0xFFFAFBFD) },
    rowTopPadding: (Int) -> androidx.compose.ui.unit.Dp = { 0.dp },
    onRowClick: ((Int) -> Unit)? = null,
    clickableRow: (Int) -> Boolean = { false },
    columnWidth: (rowIndex: Int, colIndex: Int) -> Int = { _, col -> widths.getOrElse(col) { 56 } },
    hiddenCell: (rowIndex: Int, colIndex: Int) -> Boolean = { _, _ -> false },
    cellContent: @Composable (rowIndex: Int, colIndex: Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val totalWidth = widths.sumOf { it }.dp + 16.dp
    Column(modifier = modifier.fillMaxSize()) {
        Row(Modifier.horizontalScroll(hScroll)) {
            Row(Modifier.width(totalWidth).background(PmpColors.TableHeaderBg).padding(vertical = 6.dp)) {
                headers.forEachIndexed { i, h -> PmpHeaderCell(h, widths.getOrElse(i) { 56 }.dp, groupStart = groupStartColumns.contains(i)) }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            items(rowCount) { index ->
                Row(
                    modifier = Modifier
                        .horizontalScroll(hScroll)
                        .width(totalWidth)
                        .background(rowBackground(index))
                        .padding(top = rowTopPadding(index))
                        .then(
                            if (clickableRow(index) && onRowClick != null) Modifier.clickable { onRowClick(index) }
                            else Modifier,
                        )
                        .padding(vertical = 4.dp),
                ) {
                    headers.forEachIndexed { colIndex, _ ->
                        if (hiddenCell(index, colIndex)) return@forEachIndexed
                        val w = columnWidth(index, colIndex)
                        if (w <= 0) return@forEachIndexed
                        Box(Modifier.width(w.dp)) { cellContent(index, colIndex) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PmpHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = PmpColors.Ink,
    groupStart: Boolean = false,
) {
    Row(modifier = Modifier.width(width)) {
        if (groupStart) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(Color(0xFF94A3B8)),
            )
        }
        Text(
            text,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun PmpTableCell(
    text: String,
    color: Color,
    weight: FontWeight,
    align: TextAlign,
    widthDp: Int? = null,
) {
    Text(
        text,
        modifier = Modifier.then(if (widthDp != null) Modifier.width(widthDp.dp).padding(horizontal = 4.dp) else Modifier.padding(horizontal = 4.dp)).fillMaxWidth(),
        fontSize = 10.sp,
        fontWeight = weight,
        color = color,
        textAlign = align,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
