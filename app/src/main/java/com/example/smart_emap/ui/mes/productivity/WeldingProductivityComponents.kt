package com.example.smart_emap.ui.mes.productivity

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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.WeldingProductivityDefectRowDto
import com.example.smart_emap.data.model.WeldingProductivityOperatorRowDto
import com.example.smart_emap.data.model.WeldingProductivityProductRankingDto
import com.example.smart_emap.data.model.WeldingProductivityProductRowDto
import com.example.smart_emap.data.model.WeldingProductivitySessionRowDto
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterLabel
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors

private val wpaEmerald = Color(0xFF059669)
private val wpaEmeraldLight = Color(0xFF10B981)
private val wpaIndigo = Color(0xFF6366F1)
private val wpaSky = Color(0xFF38BDF8)
private val heroGradient = Brush.linearGradient(listOf(Color.White, Color(0xFFF0FDF4)))
private val heroAccent = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
private val panelShape = RoundedCornerShape(12.dp)

@Composable
fun WpaHeroBar(
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
                Icon(Icons.Default.AssignmentTurnedIn, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    "MES · 実績分析",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = wpaIndigo,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    "溶接工程 — 生産性分析",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                )
                Text(
                    "welding_management 実績 · 能率 · 不良率 · 稼働",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    lineHeight = 13.sp,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!rangeLabel.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0x1A6366F1)) {
                    Text(
                        rangeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                    )
                }
            }
            WpaActionChip("更新", Icons.Default.Refresh, enabled = !loading, onClick = onRefresh)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WpaToolbarCard(
    startDate: String,
    endDate: String,
    filterOperatorId: Int?,
    filterProductCd: String,
    operatorOptions: List<UserListItemDto>,
    productOptions: List<ErpProductDto>,
    includeIncomplete: Boolean,
    loading: Boolean,
    onDateRangeChange: (String, String) -> Unit,
    onOperatorChange: (Int?) -> Unit,
    onProductChange: (String) -> Unit,
    onIncludeIncompleteChange: (Boolean) -> Unit,
    onAnalyze: () -> Unit,
) {
    val operatorDropdownOptions = listOf("" to "（すべて）") +
        operatorOptions.mapNotNull { u ->
            val id = u.id ?: return@mapNotNull null
            id.toString() to u.displayLabel().ifBlank { u.username.orEmpty() }
        }
    val productDropdownOptions = listOf("" to "（すべて）") +
        productOptions.map { p ->
            val cd = p.productCode
            cd to (p.productName.ifBlank { cd })
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
                    "溶接作業者",
                    filterOperatorId?.toString().orEmpty(),
                    operatorDropdownOptions,
                    { v -> onOperatorChange(v.toIntOrNull()) },
                    Modifier.fillMaxWidth(),
                )
            }
        }
        ProductionDropdownFilter(
            "製品名",
            filterProductCd,
            productDropdownOptions,
            onProductChange,
            Modifier.fillMaxWidth(),
        )
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
                    colors = CheckboxDefaults.colors(checkedColor = wpaEmerald),
                )
                Text("未確定を含む", fontSize = 11.sp, color = ProductionPlanningColors.TextPrimary)
            }
            WpaPrimaryButton("分析実行", enabled = !loading, onClick = onAnalyze)
        }
    }
}

@Composable
fun WpaOperatorSection(rows: List<WeldingProductivityOperatorRowDto>) {
    val topOperators = rows.take(8)
    WpaPanel(title = "溶接作業者別", badge = null) {
        if (topOperators.isNotEmpty()) {
            WpaHorizontalBarChart(
                labels = topOperators.map { it.operatorName.orEmpty() },
                values = topOperators.map { it.efficiencyPerHour?.toFloat() ?: 0f },
                barColors = listOf(
                    Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
                    Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEC4899),
                ),
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            Spacer(Modifier.height(8.dp))
        }
        WpaOperatorTable(rows)
    }
}

@Composable
fun WpaProductSection(rows: List<WeldingProductivityProductRowDto>) {
    WpaPanel(title = "製品別", badge = "${rows.size} 品目") {
        WpaProductTable(rows)
    }
}

@Composable
fun WpaProductRankSection(
    productRankList: List<WeldingProductivityProductRankingDto>,
    selectedRanking: WeldingProductivityProductRankingDto?,
    podiumOperators: List<WeldingProductivityOperatorRowDto>,
    rankViewProductCd: String,
    topOverview: List<WeldingProductivityProductRankingDto>,
    onProductSelect: (String) -> Unit,
    onDetailClick: (String) -> Unit,
) {
    if (productRankList.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color.White, Color(0x26FEF3C7))))
            .border(1.dp, Color(0x2EF59E0B), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "製品別 · 作業者能率ランキング",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
            )
            ProductionDropdownFilter(
                "製品",
                rankViewProductCd,
                productRankList.map { p ->
                    p.productCd to WeldingProductivityLogic.productRankOptionLabel(p)
                },
                onProductSelect,
                Modifier.widthIn(max = 200.dp),
            )
        }

        selectedRanking?.let { ranking ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Color(0x146366F1), Color(0x0F0EA5E9))))
                    .border(1.dp, Color(0x1F6366F1), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.9f)) {
                    Text(
                        ranking.productCd,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF4338CA),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    ranking.productName.orEmpty(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "生産 ${WeldingProductivityLogic.fmtInt(ranking.sumActualQty)} · 作業者 ${ranking.rankedOperatorCount ?: 0} 名",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                )
            }

            if (podiumOperators.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    podiumOperators.forEach { item ->
                        val rank = item.rank ?: 0
                        val weight = if (rank == 1) 1.15f else 1f
                        val minH = when (rank) {
                            1 -> 108.dp
                            2 -> 92.dp
                            else -> 84.dp
                        }
                        val bg = if (rank == 1) {
                            Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7), Color.White))
                        } else {
                            Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC)))
                        }
                        Column(
                            modifier = Modifier
                                .weight(weight)
                                .heightIn(min = minH)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .border(
                                    1.dp,
                                    if (rank == 1) Color(0x59F59E0B) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(WeldingProductivityLogic.rankMedal(rank), fontSize = 22.sp)
                            Text(
                                item.operatorName.orEmpty(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                WeldingProductivityLogic.fmtEfficiency(item.efficiencyPerHour),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = wpaEmerald,
                            )
                            Text("個/時", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            val operators = ranking.operators.orEmpty()
            if (operators.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                WpaHorizontalBarChart(
                    labels = operators.map { "#${it.rank} ${it.operatorName.orEmpty()}" },
                    values = operators.map { it.efficiencyPerHour?.toFloat() ?: 0f },
                    barColors = operators.mapIndexed { i, _ ->
                        if (i == 0) Color(0xFFF59E0B) else listOf(
                            Color(0xFF94A3B8), Color(0xFFB45309), Color(0xFF6366F1),
                            Color(0xFF0EA5E9), Color(0xFF10B981), Color(0xFF8B5CF6), Color(0xFFEC4899),
                        )[i % 7]
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
                Spacer(Modifier.height(8.dp))
                WpaRankOperatorTable(operators)
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x6694A3B8), RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "能率を算出できる作業者データがありません（正味稼働時間が必要です）",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("全製品 · 能率 TOP1 一覧", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        Spacer(Modifier.height(6.dp))
        WpaRankOverviewTable(topOverview, onDetailClick)
    }
}

@Composable
fun WpaDefectSection(
    rows: List<WeldingProductivityDefectRowDto>,
    defectLabel: (String) -> String,
) {
    if (rows.isEmpty()) return
    WpaPanel(title = "不良内訳（KT07）", badge = null) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            rows.take(8).forEach { row ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))))
                        .border(1.dp, Color(0x40FB923C), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        defectLabel(row.defectCd),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9A3412),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 120.dp),
                    )
                    Text(
                        WeldingProductivityLogic.fmtInt(row.qty),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC2410C),
                    )
                }
            }
        }
    }
}

@Composable
fun WpaSessionDetailSection(rows: List<WeldingProductivitySessionRowDto>) {
    WpaPanel(title = "セッション明細", badge = "${rows.size} 件") {
        WpaSessionTable(rows)
    }
}

@Composable
private fun WpaPanel(title: String, badge: String?, content: @Composable () -> Unit) {
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
            if (!badge.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0x1A6366F1)) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = wpaIndigo,
                    )
                }
            }
        }
        content()
    }
}

@Composable
private fun WpaHorizontalBarChart(
    labels: List<String>,
    values: List<Float>,
    barColors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (labels.isEmpty()) return@Canvas
        val leftPad = 8f
        val rightPad = 40f
        val topPad = 4f
        val bottomPad = 4f
        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val rowHeight = chartHeight / labels.size.coerceAtLeast(1)
        val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f) * 1.1f

        labels.forEachIndexed { index, _ ->
            val value = values.getOrElse(index) { 0f }
            val yCenter = chartTop + rowHeight * index + rowHeight / 2f
            val barW = (value / maxVal) * (chartRight - chartLeft)
            val color = barColors.getOrElse(index) { wpaIndigo }
            if (barW > 0f) {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f))),
                    topLeft = Offset(chartLeft, yCenter - rowHeight * 0.28f),
                    size = Size(barW, rowHeight * 0.56f),
                )
            }
        }
    }
}

@Composable
private fun WpaOperatorTable(rows: List<WeldingProductivityOperatorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("作業者", "件", "生産", "不良率", "能率")
    val widths = listOf(88, 40, 56, 52, 44)
    Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        WpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                WpaCell(row.operatorName.orEmpty(), widths[0], align = TextAlign.Start)
                WpaCell("${row.sessionCount ?: 0}", widths[1])
                WpaCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), widths[2])
                WpaCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), widths[3], color = Color(0xFFEA580C), bold = true)
                WpaCell(WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[4], color = wpaEmerald, bold = true)
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun WpaProductTable(rows: List<WeldingProductivityProductRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "件", "生産", "不良率", "能率")
    val widths = listOf(72, 100, 36, 56, 52, 44)
    Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        WpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                WpaCell(row.productCd.orEmpty(), widths[0], fontMono = true, align = TextAlign.Start)
                WpaCell(row.productName.orEmpty(), widths[1], align = TextAlign.Start)
                WpaCell("${row.sessionCount ?: 0}", widths[2])
                WpaCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), widths[3])
                WpaCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), widths[4], color = Color(0xFFEA580C), bold = true)
                WpaCell(WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[5], color = wpaEmerald, bold = true)
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun WpaRankOperatorTable(rows: List<WeldingProductivityOperatorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("順位", "作業者", "件", "生産", "能率", "不良率", "稼働")
    val widths = listOf(44, 88, 36, 56, 52, 52, 44)
    Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        WpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                WpaRankBadge(row.rank, widths[0])
                WpaCell(row.operatorName.orEmpty(), widths[1], align = TextAlign.Start)
                WpaCell("${row.sessionCount ?: 0}", widths[2])
                WpaCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), widths[3])
                WpaCell(WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[4], color = wpaEmerald, bold = true)
                WpaCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), widths[5])
                WpaCell(WeldingProductivityLogic.fmtDurationMin(row.sumNetProductionMin), widths[6])
            }
        }
    }
}

@Composable
private fun WpaRankOverviewTable(
    rows: List<WeldingProductivityProductRankingDto>,
    onDetailClick: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "TOP作業者", "能率", "対象人数", "")
    val widths = listOf(72, 100, 88, 52, 56, 48)
    Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        WpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                WpaCell(row.productCd, widths[0], fontMono = true, align = TextAlign.Start)
                WpaCell(row.productName.orEmpty(), widths[1], align = TextAlign.Start)
                WpaCell(row.topOperatorName ?: "—", widths[2], align = TextAlign.Start)
                WpaCell(WeldingProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour), widths[3], color = wpaEmerald, bold = true)
                WpaCell("${row.rankedOperatorCount ?: 0}", widths[4])
                Box(Modifier.width(widths[5].dp), contentAlignment = Alignment.Center) {
                    Text(
                        "詳細",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.clickable { onDetailClick(row.productCd) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WpaSessionTable(rows: List<WeldingProductivitySessionRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("生産日", "作業者", "設備", "CD", "製品名", "生産", "不良", "不良率", "能率", "稼働", "停止", "状態")
    val widths = listOf(72, 72, 56, 68, 88, 44, 40, 48, 44, 40, 40, 52)
    Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        WpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                WpaCell(row.productionDay?.take(10).orEmpty(), widths[0], fontMono = true)
                WpaCell(row.operatorDisplayName.orEmpty(), widths[1], align = TextAlign.Start)
                WpaCell(row.weldingMachine.orEmpty().ifBlank { "—" }, widths[2], align = TextAlign.Start)
                WpaCell(row.productCd.orEmpty(), widths[3], fontMono = true)
                WpaCell(row.productName.orEmpty(), widths[4], align = TextAlign.Start)
                WpaCell(WeldingProductivityLogic.fmtInt(row.actualProductionQuantity), widths[5])
                WpaCell(WeldingProductivityLogic.fmtInt(row.defectQty), widths[6])
                WpaCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), widths[7])
                WpaCell(WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[8], color = wpaEmerald, bold = true)
                WpaCell(row.netProductionMin?.toString() ?: "—", widths[9])
                WpaCell(row.pausedMin?.toString() ?: "—", widths[10])
                WpaStatusCell(row.isCompleted == true, widths[11])
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun WpaRankBadge(rank: Int?, widthDp: Int) {
    val (bg, fg) = when (rank) {
        1 -> Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))) to Color(0xFF92400E)
        2 -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))) to Color(0xFF475569)
        3 -> Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))) to Color(0xFF9A3412)
        else -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9))) to Color(0xFF475569)
    }
    Box(Modifier.width(widthDp.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(bg)
                .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text(rank?.toString() ?: "—", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = fg)
        }
    }
}

@Composable
private fun WpaStatusCell(completed: Boolean, widthDp: Int) {
    val (bg, fg, label) = if (completed) {
        Triple(Color(0x2610B981), Color(0xFF047857), "確定")
    } else {
        Triple(Color(0x3394A3B8), Color(0xFF64748B), "未確定")
    }
    Box(Modifier.width(widthDp.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(999.dp), color = bg) {
            Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}

@Composable
private fun WpaTableHeader(headers: List<String>, widths: List<Int>) {
    Row(
        Modifier
            .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))))
            .padding(vertical = 5.dp),
    ) {
        headers.forEachIndexed { i, h ->
            Text(
                h,
                modifier = Modifier.width(widths[i].dp).padding(horizontal = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                textAlign = if (i <= 2) TextAlign.Start else TextAlign.End,
            )
        }
    }
}

@Composable
private fun WpaCell(
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
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontFamily = if (fontMono) FontFamily.Monospace else FontFamily.Default,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
    )
}

@Composable
private fun WpaEmptyRow() {
    Text(
        "データがありません",
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        fontSize = 11.sp,
        color = ProductionPlanningColors.TextSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun WpaActionChip(
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
        Icon(icon, null, tint = if (enabled) wpaEmerald else Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) wpaEmerald else Color(0xFF94A3B8))
    }
}

@Composable
private fun WpaPrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val primaryBrush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(if (enabled) primaryBrush else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color(0xFF94A3B8))
    }
}
