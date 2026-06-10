package com.example.smart_emap.ui.mes.utilization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.InspectionUtilizationDailyInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationDailyRowDto
import com.example.smart_emap.data.model.InspectionUtilizationInspectorRowDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterLabel
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors
import com.example.smart_emap.ui.erp.production.planning.ProductionBeautifulDatePickerDialog

private val iuaGreen = Color(0xFF059669)
private val iuaGreenLight = Color(0xFF10B981)
private val iuaIndigo = Color(0xFF6366F1)
private val heroGradient = Brush.linearGradient(listOf(Color.White, Color(0xFFF0FDF4)))
private val heroAccent = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
private val panelShape = RoundedCornerShape(12.dp)

@Composable
fun IuaHeroBar(
    standardHours: Double,
    rangeLabel: String?,
    loading: Boolean,
    onRefresh: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = Color(0x1410B981))
            .clip(shape)
            .background(heroGradient)
            .border(1.dp, Color(0x2610B981), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(heroAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    "MES · 実績分析 · 稼働率",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = iuaGreen,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    "検査工程 — 稼働率分析",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                )
                Text(
                    "inspection_management · 検査員別 · 所定 ${String.format("%.1f", standardHours)}h/日 · 会社稼働カレンダー自動反映",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    lineHeight = 13.sp,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!rangeLabel.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0x1A10B981)) {
                    Text(
                        rangeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                    )
                }
            }
            IuaActionChip("更新", Icons.Default.Refresh, enabled = !loading, onClick = onRefresh)
        }
    }
}

@Composable
fun IuaCalendarBanner(
    calendarWorkdays: Int?,
    extraWorkdaysCount: Int,
    holidaysCount: Int,
    onOpenCalendar: () -> Unit,
) {
    val shape = panelShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFF0FDF4))))
            .border(1.dp, Color(0x3310B981), shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Default.CalendarMonth, null, tint = iuaGreen, modifier = Modifier.size(16.dp))
        val parts = buildList {
            add("会社稼働カレンダー反映")
            add("通常稼働 ${calendarWorkdays ?: "—"} 日")
            if (extraWorkdaysCount > 0) add("臨時出勤 $extraWorkdaysCount")
            if (holidaysCount > 0) add("休日 $holidaysCount")
        }
        Text(
            parts.joinToString(" · "),
            fontSize = 11.sp,
            color = Color(0xFF047857),
            modifier = Modifier.weight(1f),
            lineHeight = 14.sp,
        )
        Text(
            "カレンダー管理",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2563EB),
            modifier = Modifier.clickable(onClick = onOpenCalendar),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IuaOverrideCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    extraWorkdays: List<String>,
    extraHolidays: List<String>,
    onAddExtraWorkday: (String) -> Unit,
    onRemoveExtraWorkday: (String) -> Unit,
    onAddExtraHoliday: (String) -> Unit,
    onRemoveExtraHoliday: (String) -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("分析上書き（任意）", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
            Text(
                "会社カレンダーに追加指定 · 未設定時はマスタのみ",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(if (expanded) "▲" else "▼", fontSize = 10.sp, color = Color(0xFF64748B))
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IuaMultiDateField("臨時出勤", extraWorkdays, onAddExtraWorkday, onRemoveExtraWorkday)
                IuaMultiDateField("臨時休日", extraHolidays, onAddExtraHoliday, onRemoveExtraHoliday)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IuaToolbarCard(
    startDate: String,
    endDate: String,
    filterInspectorId: Int?,
    inspectorOptions: List<UserListItemDto>,
    includeIncomplete: Boolean,
    loading: Boolean,
    onDateRangeChange: (String, String) -> Unit,
    onInspectorChange: (Int?) -> Unit,
    onIncludeIncompleteChange: (Boolean) -> Unit,
    onAnalyze: () -> Unit,
) {
    val inspectorDropdownOptions = listOf("" to "（すべて）") +
        inspectorOptions.mapNotNull { u ->
            val id = u.id ?: return@mapNotNull null
            id.toString() to u.displayLabel().ifBlank { u.username.orEmpty() }
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), panelShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                ProductionFilterLabel("期間")
                ProductionCompactDateRangeField(
                    startDate = startDate,
                    endDate = endDate,
                    onStartChange = { onDateRangeChange(it, endDate) },
                    onEndChange = { onDateRangeChange(startDate, it) },
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                ProductionDropdownFilter(
                    "検査員",
                    filterInspectorId?.toString().orEmpty(),
                    inspectorDropdownOptions,
                    { v -> onInspectorChange(v.toIntOrNull()) },
                    Modifier.fillMaxWidth(),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = includeIncomplete,
                    onCheckedChange = onIncludeIncompleteChange,
                    modifier = Modifier.size(32.dp),
                    colors = CheckboxDefaults.colors(checkedColor = iuaGreen),
                )
                Text("未確定を含む", fontSize = 11.sp, color = ProductionPlanningColors.TextPrimary)
            }
            IuaPrimaryButton("分析実行", enabled = !loading, onClick = onAnalyze)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IuaMultiDateField(
    label: String,
    dates: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    if (showPicker) {
        ProductionBeautifulDatePickerDialog(
            value = dates.lastOrNull().orEmpty(),
            title = label,
            onDismiss = { showPicker = false },
            onConfirm = { onAdd(it); showPicker = false },
        )
    }
    ProductionFilterLabel(label)
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, ProductionPlanningColors.CardBorder, shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            dates.forEach { d ->
                IuaDateChip(d, onRemove)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE0F2FE),
                modifier = Modifier.clickable { showPicker = true },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                    Text("追加", fontSize = 9.sp, color = Color(0xFF0284C7))
                }
            }
        }
        if (dates.isEmpty()) {
            Text("日付未設定", fontSize = 9.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun IuaDateChip(date: String, onRemove: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFECFDF5), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0))) {
        Row(
            modifier = Modifier.padding(start = 6.dp, end = 2.dp, top = 1.dp, bottom = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(date.take(10), fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = iuaGreen)
            Icon(
                Icons.Default.Close,
                contentDescription = "削除",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(14.dp).clickable { onRemove(date) }.padding(2.dp),
            )
        }
    }
}

@Composable
fun IuaDataGapsBanner(gaps: List<String>) {
    if (gaps.isEmpty()) return
    val shape = panelShape
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), shape)
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Info, null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
            Text("データ上の留意点", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
        }
        gaps.forEach { gap ->
            Text("· $gap", fontSize = 10.sp, color = Color(0xFF475569), lineHeight = 14.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IuaKpiGrid(cards: List<IuaKpiCard>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        maxItemsInEachRow = 2,
    ) {
        cards.forEach { card ->
            val bg = when (card.tone) {
                IuaKpiTone.Green -> Color(0xFFECFDF5) to Color(0x3310B981)
                IuaKpiTone.Blue -> Color(0xFFEFF6FF) to Color(0x333B82F6)
                IuaKpiTone.Indigo -> Color(0xFFEEF2FF) to Color(0x336366F1)
                IuaKpiTone.Amber -> Color(0xFFFFF7ED) to Color(0x33F97316)
                IuaKpiTone.Violet -> Color(0xFFF5F3FF) to Color(0x338B5CF6)
            }
            val shape = RoundedCornerShape(10.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.48f)
                    .clip(shape)
                    .background(Brush.linearGradient(listOf(Color.White, bg.first)))
                    .border(1.dp, bg.second, shape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(card.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Text(card.value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF0F172A))
                Text(card.hint, fontSize = 9.sp, color = Color(0xFF94A3B8), maxLines = 2, lineHeight = 12.sp)
            }
        }
    }
}

/** 互換用 */
@Composable
fun IuaKpiStrip(cards: List<IuaKpiCard>) = IuaKpiGrid(cards)

@Composable
fun IuaDailyChartCard(daily: List<InspectionUtilizationDailyRowDto>) {
    IuaPanel(title = "日別稼働率推移", badge = "検査員合算") {
        if (daily.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("データがありません", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IuaChartLegend("稼働率", iuaGreenLight)
                Spacer(Modifier.width(10.dp))
                IuaChartLegend("正味(分)", iuaIndigo)
            }
            IuaDailyTrendChart(daily, Modifier.fillMaxWidth().height(220.dp))
            IuaChartXAxis(daily)
        }
    }
}

@Composable
private fun IuaChartXAxis(daily: List<InspectionUtilizationDailyRowDto>) {
    if (daily.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 36.dp, top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        daily.forEach { row ->
            Text(
                InspectionUtilizationLogic.chartDayLabel(row.day),
                fontSize = 8.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 28.dp),
            )
        }
    }
}

@Composable
private fun IuaChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun IuaDailyTrendChart(daily: List<InspectionUtilizationDailyRowDto>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val leftPad = 36f
        val rightPad = 36f
        val topPad = 8f
        val bottomPad = 28f
        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val count = daily.size.coerceAtLeast(1)
        val groupWidth = chartWidth / count

        val pctMax = 120f
        val minMax = (daily.maxOfOrNull { it.sumNetProductionMin ?: 0 } ?: 1).coerceAtLeast(1).toFloat()
        val minAxisMax = minMax * 1.15f

        for (tick in 0..4) {
            val y = chartBottom - chartHeight * tick / 4f
            drawLine(Color(0xFFF1F5F9), Offset(chartLeft, y), Offset(chartRight, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
        }
        drawLine(Color(0xFFE2E8F0), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)

        val barWidth = (groupWidth * 0.35f).coerceIn(4f, 20f)
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val minVal = (row.sumNetProductionMin ?: 0).toFloat()
            val barH = minVal / minAxisMax * chartHeight
            if (barH > 0f) {
                drawRect(
                    color = iuaIndigo.copy(alpha = 0.75f),
                    topLeft = Offset(centerX - barWidth / 2f, chartBottom - barH),
                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                )
            }
        }

        val pctPoints = daily.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val pct = row.utilizationPercent?.toFloat() ?: 0f
            val y = chartTop + chartHeight * (1f - pct / pctMax)
            x to y
        }
        if (pctPoints.size >= 2) {
            val path = Path()
            pctPoints.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, iuaGreenLight, style = Stroke(width = 2.5f))
            val areaPath = Path().apply {
                moveTo(pctPoints.first().first, chartBottom)
                pctPoints.forEach { (x, y) -> lineTo(x, y) }
                lineTo(pctPoints.last().first, chartBottom)
                close()
            }
            drawPath(areaPath, iuaGreenLight.copy(alpha = 0.12f))
        }
        pctPoints.forEach { (x, y) ->
            drawCircle(Color.White, 4f, Offset(x, y))
            drawCircle(iuaGreenLight, 3f, Offset(x, y))
        }
    }
}

@Composable
fun IuaInspectorSummaryCard(rows: List<InspectionUtilizationInspectorRowDto>) {
    IuaPanel(title = "検査員別サマリ", badge = "${rows.size} 名") {
        IuaInspectorSummaryTable(rows)
    }
}

@Composable
fun IuaDailyDetailCard(rows: List<InspectionUtilizationDailyInspectorRowDto>) {
    IuaPanel(title = "検査員 × 日別明細", badge = "${rows.size} 行") {
        IuaDailyDetailTable(rows)
    }
}

@Composable
private fun IuaPanel(title: String, badge: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Surface(shape = RoundedCornerShape(999.dp), color = Color(0x1A10B981)) {
                Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = iuaGreen)
            }
        }
        content()
    }
}

@Composable
private fun IuaInspectorSummaryTable(rows: List<InspectionUtilizationInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("検査員", "出勤日", "件数", "正味(h)", "所定内(h)", "残業(h)", "稼働率", "ｶﾚﾝﾀﾞ率")
    val widths = listOf(72, 52, 36, 48, 52, 48, 52, 52)
    Column(Modifier.horizontalScroll(scroll)) {
        IuaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IuaCell(row.inspectorName.orEmpty(), widths[0], align = TextAlign.Start)
                IuaCell("${row.scheduledWorkDayCount ?: 0}/${row.workDayCount ?: 0}", widths[1])
                IuaCell("${row.sessionCount ?: 0}", widths[2])
                IuaCell(InspectionUtilizationLogic.fmtHours(row.sumNetProductionSec), widths[3])
                IuaCell(InspectionUtilizationLogic.fmtHours(row.sumRegularSec), widths[4])
                val ot = row.sumOvertimeSec ?: 0
                IuaCell(
                    InspectionUtilizationLogic.fmtHours(row.sumOvertimeSec),
                    widths[5],
                    color = if (ot > 0) Color(0xFFEA580C) else ProductionPlanningColors.TextPrimary,
                    bold = ot > 0,
                )
                IuaCell(InspectionUtilizationLogic.fmtPct(row.utilizationPercent), widths[6], color = iuaGreen, bold = true)
                IuaCell(InspectionUtilizationLogic.fmtPct(row.calendarUtilizationPercent), widths[7])
            }
        }
        if (rows.isEmpty()) IuaEmptyRow(widths.sum())
    }
}

@Composable
private fun IuaDailyDetailTable(rows: List<InspectionUtilizationDailyInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("生産日", "検査員", "区分", "件", "正味", "所定内", "残業", "稼働率", "負荷率")
    val widths = listOf(72, 64, 56, 28, 40, 44, 40, 48, 48)
    Column(
        Modifier
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(scroll),
    ) {
        IuaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IuaCell(row.day?.take(10).orEmpty(), widths[0], fontMono = true)
                IuaCell(row.inspectorName.orEmpty(), widths[1], align = TextAlign.Start)
                IuaCategoryCell(row, widths[2])
                IuaCell("${row.sessionCount ?: 0}", widths[3])
                IuaCell(InspectionUtilizationLogic.fmtMin(row.sumNetProductionMin), widths[4])
                IuaCell(InspectionUtilizationLogic.fmtMin(row.regularMin), widths[5])
                val ot = row.overtimeMin ?: 0
                IuaCell(
                    InspectionUtilizationLogic.fmtMin(row.overtimeMin),
                    widths[6],
                    color = if (ot > 0) Color(0xFFEA580C) else ProductionPlanningColors.TextPrimary,
                    bold = ot > 0,
                )
                IuaCell(InspectionUtilizationLogic.fmtPct(row.utilizationPercent), widths[7])
                IuaCell(InspectionUtilizationLogic.fmtPct(row.loadPercent), widths[8])
            }
        }
        if (rows.isEmpty()) IuaEmptyRow(widths.sum())
    }
}

@Composable
private fun IuaTableHeader(headers: List<String>, widths: List<Int>) {
    Row(
        Modifier
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 5.dp),
    ) {
        headers.forEachIndexed { i, h ->
            Text(
                h,
                modifier = Modifier.width(widths[i].dp).padding(horizontal = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                textAlign = if (i <= 1) TextAlign.Start else TextAlign.End,
            )
        }
    }
}

@Composable
private fun IuaCell(
    text: String,
    widthDp: Int,
    align: TextAlign = TextAlign.End,
    color: Color = ProductionPlanningColors.TextPrimary,
    bold: Boolean = false,
    fontMono: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.width(widthDp.dp).padding(horizontal = 3.dp),
        fontSize = 11.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun IuaCategoryCell(row: InspectionUtilizationDailyInspectorRowDto, widthDp: Int) {
    val tone = InspectionUtilizationLogic.dayCategoryTone(row)
    val (bg, fg) = when (tone) {
        IuaDayCategoryTone.ExtraWorkday -> Color(0xFFFFF7ED) to Color(0xFFEA580C)
        IuaDayCategoryTone.HolidayActual -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        IuaDayCategoryTone.Weekday -> Color(0xFFECFDF5) to Color(0xFF059669)
    }
    Box(Modifier.width(widthDp.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(4.dp), color = bg) {
            Text(
                InspectionUtilizationLogic.dayCategoryLabel(row),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg,
            )
        }
    }
}

@Composable
private fun IuaEmptyRow(totalWidth: Int) {
    Text(
        "データがありません",
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        fontSize = 11.sp,
        color = ProductionPlanningColors.TextSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IuaActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .height(28.dp)
            .clip(shape)
            .background(if (enabled) Color(0x3310B981) else Color(0xFFE2E8F0))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (enabled) iuaGreen else Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) iuaGreen else Color(0xFF94A3B8))
    }
}

@Composable
private fun IuaPrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(if (enabled) heroAccent else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color(0xFF94A3B8))
    }
}

@Composable
fun IuaEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(panelShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), panelShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Speed, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text("期間を選択して「分析実行」をクリック", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}
