package com.example.smart_emap.ui.mes.productivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import com.example.smart_emap.data.model.InspectionProductivityDefectRowDto
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.WeldingProductivityOperatorRowDto
import com.example.smart_emap.data.model.WeldingProductivityProductRankingDto
import com.example.smart_emap.data.model.WeldingProductivityProductRowDto
import com.example.smart_emap.data.model.WeldingProductivitySessionRowDto
import com.example.smart_emap.data.model.WeldingProductivityDefectRowDto
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors

private val wpaEmerald = Color(0xFF059669)
private val wpaEmeraldLight = Color(0xFF10B981)
private val wpaIndigo = Color(0xFF6366F1)
private val wpaSky = Color(0xFF38BDF8)
private val panelShape = RoundedCornerShape(14.dp)

@Composable
fun IpaWeldingDefectSection(
    rows: List<WeldingProductivityDefectRowDto>,
    defectLabel: (String) -> String,
    sectionTitle: String = "不良内訳（KT07）",
) {
    IpaDefectSection(
        rows = rows.map { InspectionProductivityDefectRowDto(defectCd = it.defectCd, qty = it.qty) },
        defectLabel = defectLabel,
        sectionTitle = sectionTitle,
    )
}

@Composable
fun IpaWeldingOperatorProductSplit(
    operatorRows: List<WeldingProductivityOperatorRowDto>,
    productRows: List<WeldingProductivityProductRowDto>,
    operatorCount: Int,
    operatorSectionAvgEfficiency: Double?,
    productSectionTotalQty: Int,
    operatorSectionTitle: String = "溶接作業者別",
    operatorCountSuffix: String = "名",
    operatorAvgEfficiencyUnit: String = "個/時",
    operatorColumnLabel: String = "作業者",
    defectRateColumnLabel: String = "不良率",
    productDefectRateColumnLabel: String = "不良率",
    efficiencyUnitLabel: String = "個/時",
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    IpaWeldingOperatorSection(
                        operatorRows,
                        operatorCount,
                        operatorSectionAvgEfficiency,
                        operatorSectionTitle,
                        operatorCountSuffix,
                        operatorAvgEfficiencyUnit,
                        operatorColumnLabel,
                        defectRateColumnLabel,
                        efficiencyUnitLabel,
                    )
                }
                Box(Modifier.weight(1f)) {
                    IpaWeldingProductSection(
                        productRows,
                        productSectionTotalQty,
                        defectRateColumnLabel = productDefectRateColumnLabel,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IpaWeldingOperatorSection(
                    operatorRows,
                    operatorCount,
                    operatorSectionAvgEfficiency,
                    operatorSectionTitle,
                    operatorCountSuffix,
                    operatorAvgEfficiencyUnit,
                    operatorColumnLabel,
                    defectRateColumnLabel,
                    efficiencyUnitLabel,
                )
                IpaWeldingProductSection(
                    productRows,
                    productSectionTotalQty,
                    defectRateColumnLabel = productDefectRateColumnLabel,
                )
            }
        }
    }
}

@Composable
fun IpaWeldingOperatorSection(
    rows: List<WeldingProductivityOperatorRowDto>,
    operatorCount: Int,
    operatorSectionAvgEfficiency: Double?,
    operatorSectionTitle: String = "溶接作業者別",
    operatorCountSuffix: String = "名",
    operatorAvgEfficiencyUnit: String = "個/時",
    operatorColumnLabel: String = "作業者",
    defectRateColumnLabel: String = "不良率",
    efficiencyUnitLabel: String = "個/時",
) {
    val chartRows = rows.sortedByDescending { it.efficiencyPerHour ?: -1.0 }
    WpaThemedPanel(
        title = operatorSectionTitle,
        titleIcon = Icons.Default.Person,
        badges = {
            WpaSoftBadge("$operatorCount $operatorCountSuffix", Color(0xFF64748B), Color(0x2494A3B8))
            operatorSectionAvgEfficiency?.let {
                WpaSoftBadge(
                    "平均能率 ${WeldingProductivityLogic.fmtEfficiency(it)} $operatorAvgEfficiencyUnit",
                    Color(0xFF4338CA),
                    Color(0x1F6366F1),
                    border = Color(0x2E6366F1),
                )
            }
        },
    ) {
        if (chartRows.isNotEmpty()) {
            WpaOperatorBarChart(chartRows, efficiencyUnitLabel)
            Spacer(Modifier.height(10.dp))
        }
        WpaOperatorTable(rows, operatorColumnLabel, defectRateColumnLabel)
    }
}

@Composable
fun IpaWeldingProductSection(
    rows: List<WeldingProductivityProductRowDto>,
    productSectionTotalQty: Int,
    defectRateColumnLabel: String = "不良率",
) {
    val chartRows = rows.sortedByDescending { it.sumActualQty ?: 0 }
    WpaThemedPanel(
        title = "製品別",
        titleIcon = Icons.Default.Inventory2,
        badges = {
            WpaSoftBadge("${rows.size} 品目", Color(0xFF64748B), Color(0x2494A3B8))
            if (productSectionTotalQty > 0) {
                WpaSoftBadge(
                    "生産 ${WeldingProductivityLogic.fmtInt(productSectionTotalQty)}",
                    Color(0xFF0369A1),
                    Color(0x1F0EA5E9),
                    border = Color(0x330EA5E9),
                )
            }
        },
    ) {
        if (chartRows.isNotEmpty()) {
            WpaProductBarChart(chartRows)
            Spacer(Modifier.height(10.dp))
        }
        WpaProductTable(rows, defectRateColumnLabel)
    }
}

@Composable
private fun WpaOperatorBarChart(
    rows: List<WeldingProductivityOperatorRowDto>,
    efficiencyUnitLabel: String = "個/時",
) {
    WpaEfficiencyBarChartPanel(
        rows = rows,
        showRank = false,
        barColor = { index, _ ->
            listOf(
                Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
                Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEC4899),
            )[index % 8]
        },
        label = { it.operatorName.orEmpty().ifBlank { "—" } },
        value = { it.efficiencyPerHour ?: 0.0 },
        efficiencyUnitLabel = efficiencyUnitLabel,
    )
}

@Composable
private fun WpaOperatorEfficiencyRankChart(
    operators: List<WeldingProductivityOperatorRowDto>,
    efficiencyUnitLabel: String = "個/時",
) {
    WpaEfficiencyBarChartPanel(
        rows = operators.sortedBy { it.rank ?: Int.MAX_VALUE },
        showRank = true,
        barColor = { index, row ->
            when (row.rank) {
                1 -> Color(0xFFF59E0B)
                2 -> Color(0xFF94A3B8)
                3 -> Color(0xFFB45309)
                else -> listOf(
                    Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
                    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFFF97316),
                )[index % 7]
            }
        },
        label = { it.operatorName.orEmpty().ifBlank { "—" } },
        value = { it.efficiencyPerHour ?: 0.0 },
        efficiencyUnitLabel = efficiencyUnitLabel,
    )
}

@Composable
private fun WpaEfficiencyBarChartPanel(
    rows: List<WeldingProductivityOperatorRowDto>,
    showRank: Boolean,
    barColor: (index: Int, row: WeldingProductivityOperatorRowDto) -> Color,
    label: (WeldingProductivityOperatorRowDto) -> String,
    value: (WeldingProductivityOperatorRowDto) -> Double,
    efficiencyUnitLabel: String = "個/時",
) {
    if (rows.isEmpty()) return
    val maxVal = rows.maxOf { value(it) }.coerceAtLeast(1.0)
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp, max = 280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xE6FFFFFF), Color(0xFFF1F5F9)),
                ),
            )
            .border(1.dp, Color(0xD9E2E8F0), RoundedCornerShape(12.dp))
            .verticalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEachIndexed { index, row ->
            val metric = value(row)
            val color = barColor(index, row)
            val fraction = (metric / maxVal).toFloat().coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showRank) {
                    WpaChartRankChip(row.rank ?: (index + 1))
                }
                Text(
                    label(row),
                    modifier = Modifier
                        .weight(0.36f, fill = false)
                        .widthIn(min = 72.dp, max = 132.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFE2E8F0).copy(alpha = 0.65f)),
                ) {
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(color.copy(alpha = 0.72f), color),
                                    ),
                                ),
                        )
                    }
                }
                Column(
                    modifier = Modifier.widthIn(min = 44.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        WeldingProductivityLogic.fmtEfficiency(metric),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = wpaEmerald,
                        textAlign = TextAlign.End,
                    )
                    Text(
                        efficiencyUnitLabel,
                        fontSize = 9.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun WpaChartRankChip(rank: Int) {
    val (bg, fg) = when (rank) {
        1 -> Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))) to Color(0xFF92400E)
        2 -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))) to Color(0xFF475569)
        3 -> Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))) to Color(0xFF9A3412)
        else -> Brush.linearGradient(listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))) to Color(0xFF64748B)
    }
    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            rank.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
        )
    }
}

@Composable
private fun WpaProductBarChart(rows: List<WeldingProductivityProductRowDto>) {
    val colors = listOf(
        Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF6366F1), Color(0xFF8B5CF6),
        Color(0xFF10B981), Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFEC4899),
    )
    val maxVal = rows.maxOfOrNull { it.sumActualQty ?: 0 }?.coerceAtLeast(1)?.toDouble() ?: 1.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(Color(0xBFF8FAFC), Color(0x80FFFFFF))))
            .border(1.dp, Color(0xCCE2E8F0), RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        rows.forEachIndexed { index, row ->
            val value = (row.sumActualQty ?: 0).toDouble()
            val color = colors[index % colors.size]
            Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.productName.orEmpty().ifBlank { row.productCd.orEmpty() },
                    modifier = Modifier.width(76.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .background(Color(0xFFF1F5F9)),
                ) {
                    val fraction = (value / maxVal).toFloat().coerceIn(0f, 1f)
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .background(Brush.horizontalGradient(listOf(color.copy(0.85f), color))),
                        )
                    }
                }
                Text(
                    WeldingProductivityLogic.fmtInt(row.sumActualQty),
                    modifier = Modifier.width(48.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun WpaThemedPanel(
    title: String,
    titleIcon: androidx.compose.ui.graphics.vector.ImageVector,
    badges: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, panelShape, spotColor = Color(0x120F172A))
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xEBF1F5F9))))
            .border(1.dp, Color(0xF2FFFFFF), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(titleIcon, null, tint = wpaIndigo, modifier = Modifier.size(15.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { badges() }
        }
        content()
    }
}

@Composable
private fun WpaSoftBadge(
    text: String,
    fg: Color,
    bg: Color,
    border: Color = Color.Transparent,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = if (border == Color.Transparent) null else androidx.compose.foundation.BorderStroke(1.dp, border),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
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
    sectionTitle: String = "製品別 · 溶接作業者能率ランキング",
    operatorLabel: String = "作業者",
    operatorCountSuffix: String = "名",
    defectRateColumnLabel: String = "不良率",
    topOperatorColumnLabel: String = "TOP作業者",
    emptyOperatorMessage: String = "能率を算出できる作業者データがありません（正味稼働時間が必要です）",
    chartBlockTitle: String = "作業者別能率",
    efficiencyUnitLabel: String = "個/時",
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
                sectionTitle,
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
                    "生産 ${WeldingProductivityLogic.fmtInt(ranking.sumActualQty)} · $operatorLabel ${ranking.rankedOperatorCount ?: 0} $operatorCountSuffix",
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
                            Text(efficiencyUnitLabel, fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            val operators = ranking.operators.orEmpty()
            if (operators.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                WpaOperatorEfficiencyRankChart(operators, efficiencyUnitLabel)
                Spacer(Modifier.height(8.dp))
                WpaRankOperatorTable(operators, operatorLabel, defectRateColumnLabel)
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
                        emptyOperatorMessage,
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
        WpaRankOverviewTable(topOverview, onDetailClick, topOperatorColumnLabel)
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
fun WpaSessionDetailSection(
    rows: List<WeldingProductivitySessionRowDto>,
    operatorColumnLabel: String = "作業者",
    defectQtyColumnLabel: String = "不良",
    defectRateColumnLabel: String = "不良率",
    showMachineColumn: Boolean = true,
) {
    WpaThemedPanel(
        title = "セッション明細",
        titleIcon = Icons.AutoMirrored.Filled.List,
        badges = {
            WpaSoftBadge("${rows.size} 件", Color(0xFF64748B), Color(0x2494A3B8))
        },
    ) {
        WpaSessionTable(rows, operatorColumnLabel, defectQtyColumnLabel, defectRateColumnLabel, showMachineColumn)
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
private fun WpaOperatorTable(
    rows: List<WeldingProductivityOperatorRowDto>,
    operatorColumnLabel: String = "作業者",
    defectRateColumnLabel: String = "不良率",
) {
    val scroll = rememberScrollState()
    val headers = listOf(operatorColumnLabel, "件", "生産", defectRateColumnLabel, "能率")
    val columnWeights = listOf(3.2f, 0.75f, 1.1f, 1.1f, 1f)
    val columnAligns = listOf(
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .verticalScroll(scroll),
    ) {
        WpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
            ) {
                WpaFlexibleCell(row.operatorName.orEmpty(), columnWeights[0], columnAligns[0])
                WpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[1], columnAligns[1])
                WpaFlexibleCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), columnWeights[2], columnAligns[2])
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtPct(row.defectRatePercent),
                    columnWeights[3],
                    columnAligns[3],
                    color = Color(0xFFEA580C),
                    bold = true,
                )
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour),
                    columnWeights[4],
                    columnAligns[4],
                    color = wpaEmerald,
                    bold = true,
                )
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun WpaProductTable(
    rows: List<WeldingProductivityProductRowDto>,
    defectRateColumnLabel: String = "不良率",
) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "件", "生産", defectRateColumnLabel, "能率")
    val columnWeights = listOf(1.2f, 2.8f, 0.7f, 1.1f, 1.1f, 1f)
    val columnAligns = listOf(
        TextAlign.Start,
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .verticalScroll(scroll),
    ) {
        WpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
            ) {
                WpaFlexibleCell(row.productCd.orEmpty(), columnWeights[0], columnAligns[0], fontMono = true)
                WpaFlexibleCell(row.productName.orEmpty(), columnWeights[1], columnAligns[1])
                WpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[2], columnAligns[2])
                WpaFlexibleCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), columnWeights[3], columnAligns[3])
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtPct(row.defectRatePercent),
                    columnWeights[4],
                    columnAligns[4],
                    color = Color(0xFFEA580C),
                    bold = true,
                )
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtEfficiency(
                        WeldingProductivityLogic.periodAvgEfficiencyFromBucket(row) ?: row.efficiencyPerHour,
                    ),
                    columnWeights[5],
                    columnAligns[5],
                    color = wpaEmerald,
                    bold = true,
                )
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun WpaRankOperatorTable(
    rows: List<WeldingProductivityOperatorRowDto>,
    operatorColumnLabel: String = "作業者",
    defectRateColumnLabel: String = "不良率",
) {
    val scroll = rememberScrollState()
    val headers = listOf("順位", operatorColumnLabel, "件", "生産", "能率", defectRateColumnLabel, "稼働")
    val columnWeights = listOf(0.85f, 2.2f, 0.65f, 1.1f, 1.05f, 1f, 0.85f)
    val columnAligns = listOf(
        TextAlign.Center,
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
            .verticalScroll(scroll),
    ) {
        WpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WpaRankBadge(row.rank, columnWeights[0])
                WpaFlexibleCell(row.operatorName.orEmpty(), columnWeights[1], columnAligns[1], bold = true)
                WpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[2], columnAligns[2])
                WpaFlexibleCell(WeldingProductivityLogic.fmtInt(row.sumActualQty), columnWeights[3], columnAligns[3])
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour),
                    columnWeights[4],
                    columnAligns[4],
                    color = wpaEmerald,
                    bold = true,
                )
                WpaFlexibleCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), columnWeights[5], columnAligns[5])
                WpaFlexibleCell(WeldingProductivityLogic.fmtDurationMin(row.sumNetProductionMin), columnWeights[6], columnAligns[6])
            }
        }
    }
}

@Composable
private fun WpaRankOverviewTable(
    rows: List<WeldingProductivityProductRankingDto>,
    onDetailClick: (String) -> Unit,
    topOperatorColumnLabel: String = "TOP作業者",
) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", topOperatorColumnLabel, "能率", "対象人数", "")
    val columnWeights = listOf(1.1f, 2.2f, 1.8f, 1f, 1f, 0.85f)
    val columnAligns = listOf(
        TextAlign.Start,
        TextAlign.Start,
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.Center,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .verticalScroll(scroll),
    ) {
        WpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WpaFlexibleCell(row.productCd, columnWeights[0], columnAligns[0], fontMono = true)
                WpaFlexibleCell(row.productName.orEmpty(), columnWeights[1], columnAligns[1], bold = true)
                WpaFlexibleCell(row.topOperatorName ?: "—", columnWeights[2], columnAligns[2])
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour),
                    columnWeights[3],
                    columnAligns[3],
                    color = wpaEmerald,
                    bold = true,
                )
                WpaFlexibleCell("${row.rankedOperatorCount ?: 0}", columnWeights[4], columnAligns[4])
                Box(Modifier.weight(columnWeights[5]), contentAlignment = Alignment.Center) {
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
private fun WpaSessionTable(
    rows: List<WeldingProductivitySessionRowDto>,
    operatorColumnLabel: String = "作業者",
    defectQtyColumnLabel: String = "不良",
    defectRateColumnLabel: String = "不良率",
    showMachineColumn: Boolean = true,
) {
    val scroll = rememberScrollState()
    val headers = buildList {
        add("生産日")
        add(operatorColumnLabel)
        if (showMachineColumn) add("設備")
        addAll(listOf("CD", "製品名", "生産", defectQtyColumnLabel, defectRateColumnLabel, "能率", "稼働", "停止", "状態"))
    }
    val columnWeights = if (showMachineColumn) {
        listOf(1f, 1.45f, 1.15f, 0.95f, 2f, 0.65f, 0.6f, 0.8f, 0.8f, 0.65f, 0.65f, 0.85f)
    } else {
        listOf(1f, 1.45f, 0.95f, 2f, 0.65f, 0.6f, 0.8f, 0.8f, 0.65f, 0.65f, 0.85f)
    }
    val columnAligns = headers.mapIndexed { index, _ ->
        when {
            index == 0 || (showMachineColumn && index in 1..4) || (!showMachineColumn && index in 1..3) -> TextAlign.Start
            index == headers.lastIndex -> TextAlign.Center
            else -> TextAlign.End
        }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(scroll),
    ) {
        WpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var col = 0
                WpaFlexibleCell(row.productionDay?.take(10).orEmpty(), columnWeights[col], columnAligns[col], fontMono = true)
                col++
                WpaFlexibleCell(row.operatorDisplayName.orEmpty(), columnWeights[col], columnAligns[col])
                col++
                if (showMachineColumn) {
                    WpaFlexibleCell(row.weldingMachine.orEmpty().ifBlank { "—" }, columnWeights[col], columnAligns[col])
                    col++
                }
                WpaFlexibleCell(row.productCd.orEmpty(), columnWeights[col], columnAligns[col], fontMono = true)
                col++
                WpaFlexibleCell(row.productName.orEmpty(), columnWeights[col], columnAligns[col])
                col++
                WpaFlexibleCell(WeldingProductivityLogic.fmtInt(row.actualProductionQuantity), columnWeights[col], columnAligns[col])
                col++
                WpaFlexibleCell(WeldingProductivityLogic.fmtInt(row.defectQty), columnWeights[col], columnAligns[col])
                col++
                WpaFlexibleCell(WeldingProductivityLogic.fmtPct(row.defectRatePercent), columnWeights[col], columnAligns[col])
                col++
                WpaFlexibleCell(
                    WeldingProductivityLogic.fmtEfficiency(row.efficiencyPerHour),
                    columnWeights[col],
                    columnAligns[col],
                    color = wpaEmerald,
                    bold = true,
                )
                col++
                WpaFlexibleCell(row.netProductionMin?.toString() ?: "—", columnWeights[col], columnAligns[col])
                col++
                WpaFlexibleCell(row.pausedMin?.toString() ?: "—", columnWeights[col], columnAligns[col])
                col++
                WpaStatusCell(row.isCompleted == true, columnWeights[col])
            }
        }
        if (rows.isEmpty()) WpaEmptyRow()
    }
}

@Composable
private fun RowScope.WpaRankBadge(rank: Int?, weight: Float) {
    val (bg, fg) = when (rank) {
        1 -> Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))) to Color(0xFF92400E)
        2 -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))) to Color(0xFF475569)
        3 -> Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))) to Color(0xFF9A3412)
        else -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9))) to Color(0xFF475569)
    }
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
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
private fun RowScope.WpaStatusCell(completed: Boolean, weight: Float) {
    val (bg, fg, label) = if (completed) {
        Triple(Color(0x2610B981), Color(0xFF047857), "確定")
    } else {
        Triple(Color(0x3394A3B8), Color(0xFF64748B), "未確定")
    }
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(999.dp), color = bg) {
            Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}

@Composable
private fun WpaFlexibleTableHeader(
    headers: List<String>,
    weights: List<Float>,
    aligns: List<TextAlign>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))))
            .padding(vertical = 5.dp),
    ) {
        headers.forEachIndexed { i, h ->
            Text(
                h,
                modifier = Modifier.weight(weights[i]).padding(horizontal = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                textAlign = aligns[i],
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RowScope.WpaFlexibleCell(
    text: String,
    weight: Float,
    align: TextAlign,
    color: Color = ProductionPlanningColors.TextPrimary,
    bold: Boolean = false,
    fontMono: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.weight(weight).padding(horizontal = 4.dp),
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
