package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.PlanBaselineFullComparisonItemDto
import com.example.smart_emap.ui.mes.planinstruction.formatPlanComparisonValue

private val heroGradient = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
private val pageBackground = Brush.linearGradient(
    listOf(Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9)),
)

@Composable
fun PlanBaselinePageBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(pageBackground)) {
        content()
    }
}

@Composable
fun PlanBaselineHeroBar(actionLoading: Boolean, onRefresh: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x406366F1))
            .clip(shape)
            .background(heroGradient)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.2f)) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Column {
                Text("生産計画ベースライン管理", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 22.sp)
                Text(
                    "月次計画を固定化し、現行計画・実績と比較して推移を把握します",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlanBaselineGradientButton(
                "再取得",
                Icons.Default.Refresh,
                Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1))),
                enabled = !actionLoading,
                onClick = onRefresh,
            )
            if (actionLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
fun PlanBaselineActionCard(
    baselineMonth: String,
    processName: String,
    generating: Boolean,
    deleting: Boolean,
    loading: Boolean,
    onMonthChange: (String) -> Unit,
    onProcessChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, shape),
        shape = shape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PlanBaselineActionSection(
                    title = "ベースライン生成",
                    subtitle = "計画を固定化して比較基準を作成（成型・溶接は molding_plan／welding_plan を反映）",
                    iconBg = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                    iconTint = Color.White,
                    icon = Icons.Default.Add,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ProductionSingleDatePickerField(baselineMonth, onMonthChange, "基準月", modifier = Modifier.weight(1f))
                        ProductionDropdownFilter("工程", processName, PlanBaselineLogic.processOptions, onProcessChange, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        PlanBaselineGradientButton("生成", Icons.Default.Add, Brush.linearGradient(listOf(Color(0xFF22C55E), Color(0xFF16A34A))), enabled = !generating && !loading, onClick = onGenerate)
                        PlanBaselineOutlinedButton("削除", Icons.Default.Delete, Color(0xFFEF4444), enabled = !deleting && !loading, onClick = onDelete)
                        PlanBaselineOutlinedButton("計画を修正", Icons.Default.Edit, Color(0xFFF59E0B), enabled = !loading, onClick = onEdit)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFE2E8F0), Color.Transparent))),
            )
            Box(modifier = Modifier.weight(1f)) {
                PlanBaselineActionSection(
                    title = "比較条件",
                    subtitle = "対象月と工程を指定して比較",
                    iconBg = Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
                    iconTint = Color.White,
                    icon = Icons.Default.Search,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        ProductionSingleDatePickerField(baselineMonth, onMonthChange, "対象月", modifier = Modifier.weight(1f))
                        ProductionDropdownFilter("工程", processName, PlanBaselineLogic.processOptions, onProcessChange, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PlanBaselineGradientButton("検索", Icons.Default.Search, Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))), enabled = !loading, onClick = onSearch)
                        PlanBaselineOutlinedButton("クリア", Icons.Default.Refresh, Color(0xFF64748B), enabled = !loading, onClick = onClear)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanBaselineActionSection(
    title: String,
    subtitle: String,
    iconBg: Brush,
    iconTint: Color,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(shape = RoundedCornerShape(6.dp), color = Color.Transparent) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B), lineHeight = 18.sp)
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B), lineHeight = 14.sp)
            }
        }
        content()
    }
}

@Composable
fun PlanBaselineSummaryStrip(cards: List<ProductionKpiCard>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        cards.forEach { card ->
            val shape = RoundedCornerShape(8.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 118.dp)
                    .clip(shape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), shape),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                )
                Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
                    Text(card.label, fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        card.value,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 20.sp,
                        color = when {
                            card.isNegative -> Color(0xFFDC2626)
                            card.value != "-" -> Color(0xFF059669)
                            else -> Color(0xFF0F172A)
                        },
                    )
                    if (!card.description.isNullOrBlank()) {
                        Text(card.description.orEmpty(), fontSize = 9.sp, color = Color(0xFF94A3B8), lineHeight = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun PlanBaselineComparisonCard(
    monthLabel: String?,
    totalCount: Int,
    isLoading: Boolean,
    tabs: List<PlanBaselineProcessTab>,
    activeTab: String,
    activeItems: List<PlanBaselineFullComparisonItemDto>,
    canExport: Boolean,
    exportLoading: Boolean,
    onTabSelect: (String) -> Unit,
    onExport: () -> Unit,
    onPrint: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEEF2FF)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.padding(6.dp).size(16.dp))
                }
                Column {
                    Text("ベースライン 比較一覧", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!monthLabel.isNullOrBlank()) PlanBaselineTag(monthLabel, accent = Color(0xFF6366F1))
                        if (totalCount > 0) PlanBaselineTag("全 $totalCount 行", accent = Color(0xFF2563EB))
                    }
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PlanBaselineGradientButton(
                    "工程別報告書発行",
                    Icons.Default.Download,
                    Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB))),
                    enabled = canExport && !exportLoading && !isLoading,
                    onClick = onExport,
                )
                PlanBaselineOutlinedButton("印刷", Icons.Default.Print, Color(0xFF64748B), enabled = activeItems.isNotEmpty() && !isLoading, onClick = onPrint)
            }
        }
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else if (tabs.isEmpty()) {
                Text("データがありません", modifier = Modifier.padding(16.dp), color = Color(0xFF64748B), fontSize = 12.sp)
            } else {
                PlanBaselineProcessTabStrip(tabs, activeTab.ifBlank { tabs.first().name }, onTabSelect)
                PlanBaselineComparisonTable(activeItems)
                val totals = PlanBaselineLogic.buildTabTotals(activeItems)
                PlanBaselineTabTotalsBar(totals, activeItems)
            }
        }
    }
}

@Composable
private fun PlanBaselineProcessTabStrip(
    tabs: List<PlanBaselineProcessTab>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEach { tab ->
            val active = tab.name == selected
            val shape = RoundedCornerShape(6.dp)
            Surface(
                modifier = Modifier.clickable { onSelect(tab.name) },
                shape = shape,
                color = if (active) Color(0xFF6366F1) else Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (active) Color(0xFF6366F1) else Color(0xFFE2E8F0),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) Color.White else Color(0xFF475569),
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (active) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0),
                    ) {
                        Text(
                            tab.count.toString(),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) Color.White else Color(0xFF64748B),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanBaselineComparisonTable(items: List<PlanBaselineFullComparisonItemDto>) {
    val scrollH = rememberScrollState()
    val scrollV = rememberScrollState()
    val headers = listOf("日付", "基準計画", "現行計画", "計画差異", "現行実績合計", "計画対実績差")
    val widths = listOf(92, 78, 78, 78, 88, 92)
    val borderColor = Color(0xFFE2E8F0)
    val shape = RoundedCornerShape(6.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .verticalScroll(scrollV)
                .horizontalScroll(scrollH),
        ) {
            Row(
                modifier = Modifier
                    .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
                    .padding(vertical = 7.dp),
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        modifier = Modifier.width(widths[i].dp).padding(horizontal = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = if (i == 0) TextAlign.Center else TextAlign.End,
                    )
                }
            }
            items.forEachIndexed { index, item ->
                val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                Row(
                    modifier = Modifier
                        .background(bg)
                        .border(0.5.dp, borderColor)
                        .padding(vertical = 5.dp),
                ) {
                    PlanBaselineDateCell(PlanBaselineLogic.formatBaselineDate(item.planDate), widths[0])
                    PlanBaselineNumCell(formatPlanComparisonValue(item.baselinePlan), widths[1])
                    PlanBaselineNumCell(formatPlanComparisonValue(item.currentPlan), widths[2])
                    PlanBaselineDiffBadgeCell(item.planDiff, widths[3])
                    if (item.currentActual != null) PlanBaselineNumCell(formatPlanComparisonValue(item.currentActual), widths[4])
                    else PlanBaselineNumCell("-", widths[4], muted = true)
                    if (item.actualDiff != null) PlanBaselineDiffBadgeCell(item.actualDiff, widths[5])
                    else PlanBaselineNumCell("-", widths[5], muted = true)
                }
            }
        }
    }
}

@Composable
private fun PlanBaselineTabTotalsBar(totals: PlanBaselineTabTotals, items: List<PlanBaselineFullComparisonItemDto>) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("合計", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1E293B))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PlanBaselineTotalChip("現行計画", formatPlanComparisonValue(totals.currentPlan), Color(0xFFFB8C00), modifier = Modifier.weight(1f))
            PlanBaselineTotalChip("計画差異", formatPlanComparisonValue(totals.planDiff), diffColor(totals.planDiff), totals.planDiff, Modifier.weight(1f))
            if (items.any { it.currentActual != null }) {
                PlanBaselineTotalChip("現行実績", formatPlanComparisonValue(totals.currentActual), Color(0xFF388E3C), modifier = Modifier.weight(1f))
            }
            if (items.any { it.actualDiff != null }) {
                PlanBaselineTotalChip("計画対実績差", formatPlanComparisonValue(totals.actualDiff), diffColor(totals.actualDiff), totals.actualDiff, Modifier.weight(1f))
            }
        }
    }
}

private val utilizationRowHeight = 27.dp
private const val utilizationVisibleRows = 13
private val utilizationActualColor = Color(0xFF0D9488)
private val utilizationNegativeColor = Color(0xFFC62828)

@Composable
fun PlanBaselineOperationRateCard(
    month: String,
    monthOptions: List<Pair<String, String>>,
    processCd: String,
    processOptions: List<Pair<String, String>>,
    rows: List<PlanBaselineUtilizationRow>,
    loading: Boolean,
    onMonthChange: (String) -> Unit,
    onProcessChange: (String) -> Unit,
    onPrint: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val monthLabel = PlanBaselineUtilizationLogic.utilizationMonthLabelJp(month)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFFFAFBFC), Color.White)))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEEF2FF)) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.padding(6.dp).size(16.dp))
                }
                Text("操業度", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text("APS 設備操業度（月次）", fontSize = 12.sp, color = Color(0xFF94A3B8))
                if (rows.isNotEmpty()) PlanBaselineTag("${rows.size} 設備", accent = Color(0xFF2563EB))
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9))))
                    .border(1.dp, Color(0xFF94A3B8).copy(alpha = 0.42f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlanBaselineInlineFilterLabel("集計月")
                PlanBaselineCompactDropdown(month, monthOptions, onMonthChange, minWidth = 118.dp)
                PlanBaselineInlineFilterLabel("工程")
                PlanBaselineCompactDropdown(processCd, processOptions, onProcessChange, minWidth = 148.dp)
                PlanBaselineOutlinedButton("印刷", Icons.Default.Print, Color(0xFF2563EB), enabled = rows.isNotEmpty() && !loading, onClick = onPrint)
            }
        }
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                PlanBaselineUtilNoteChip("対象：$monthLabel", formula = false)
                PlanBaselineUtilNoteChip("操業度＝各時間÷理論稼働", formula = true)
                PlanBaselineUtilNoteChip("差異工時＝上記期間の Σ((実績−計画)/能率)", formula = true)
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                PlanBaselineUtilizationTable(rows, loading)
                if (loading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.White.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanBaselineInlineFilterLabel(text: String) {
    Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.3.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanBaselineCompactDropdown(
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    minWidth: androidx.compose.ui.unit.Dp,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "選択" }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .widthIn(min = minWidth)
                .height(30.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x5994A3B8)),
        ) {
            Box(Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                Text(display, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF1E293B))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PlanBaselineUtilNoteChip(text: String, formula: Boolean) {
    val bg = if (formula) Color(0xFFEEF2FF) else Color(0xFFF1F5F9)
    val fg = if (formula) Color(0xFF4338CA) else Color(0xFF475569)
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 11.sp, color = fg)
    }
}

@Composable
private fun PlanBaselineUtilizationTable(rows: List<PlanBaselineUtilizationRow>, loading: Boolean = false) {
    val scrollH = rememberScrollState()
    val scrollV = rememberScrollState()
    val headers = listOf(
        "設備", "指示数", "理論稼働(H)", "計画数", "実績数",
        "計画時間(H)", "実績時間(H)", "計画操業度", "実績操業度", "操業度差異(H)", "差異操業度(%)",
    )
    val widths = listOf(68, 52, 78, 68, 68, 82, 82, 78, 78, 86, 92)
    val borderColor = Color(0xFFE2E8F0)
    val shape = RoundedCornerShape(8.dp)
    val bodyHeight = utilizationRowHeight * utilizationVisibleRows
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, borderColor, shape),
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollH)
                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
                .padding(vertical = 7.dp),
        ) {
            headers.forEachIndexed { i, h ->
                Text(
                    h,
                    modifier = Modifier.width(widths[i].dp).padding(horizontal = 3.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = if (i <= 1) TextAlign.Center else TextAlign.End,
                    lineHeight = 12.sp,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(bodyHeight)
                .verticalScroll(scrollV)
                .horizontalScroll(scrollH),
        ) {
            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bodyHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (loading) "" else "該当データがありません。集計月・工程を変更すると自動で再読込されます。",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                rows.forEachIndexed { index, row ->
                    val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                    Row(
                        modifier = Modifier
                            .height(utilizationRowHeight)
                            .background(bg)
                            .border(0.5.dp, borderColor)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            row.lineLabel,
                            modifier = Modifier.width(widths[0].dp).padding(horizontal = 3.dp),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF1E293B),
                        )
                        Text(
                            row.scheduleCount.toString(),
                            modifier = Modifier.width(widths[1].dp),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                        )
                        PlanBaselineUtilNumCell(PlanBaselineUtilizationLogic.formatUtilHours(row.availableHours), widths[2], actual = false)
                        PlanBaselineUtilNumCell(formatProductionNumber(row.plannedQty), widths[3], actual = false)
                        PlanBaselineUtilNumCell(formatProductionNumber(row.actualQty), widths[4], actual = true)
                        PlanBaselineUtilNumCell(PlanBaselineUtilizationLogic.formatUtilHours(row.plannedHours), widths[5], actual = false)
                        PlanBaselineUtilNumCell(PlanBaselineUtilizationLogic.formatUtilHours(row.actualHours), widths[6], actual = true)
                        PlanBaselineUtilNumCell(PlanBaselineUtilizationLogic.formatUtilPercent(row.planUtilizationPct), widths[7], actual = false)
                        PlanBaselineUtilNumCell(PlanBaselineUtilizationLogic.formatUtilPercent(row.actualUtilizationPct), widths[8], actual = true)
                        PlanBaselineUtilNumCell(
                            PlanBaselineUtilizationLogic.formatUtilDiffHours(row.diffHours),
                            widths[9],
                            actual = false,
                            negative = row.diffHours < 0,
                        )
                        PlanBaselineUtilNumCell(
                            PlanBaselineUtilizationLogic.formatUtilPercent(row.diffUtilizationPct),
                            widths[10],
                            actual = false,
                            negative = row.diffUtilizationPct < 0,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanBaselineUtilNumCell(text: String, widthDp: Int, actual: Boolean, negative: Boolean = false) {
    val color = when {
        negative -> utilizationNegativeColor
        actual -> utilizationActualColor
        else -> Color(0xFF1E293B)
    }
    Text(
        text,
        modifier = Modifier.width(widthDp.dp).padding(horizontal = 3.dp),
        fontSize = 10.sp,
        textAlign = TextAlign.End,
        fontFamily = FontFamily.Monospace,
        color = color,
        fontWeight = if (negative) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun PlanBaselineDateCell(text: String, widthDp: Int) {
    Row(modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(2.dp))
        Text(text, fontSize = 10.sp, color = ProductionPlanningColors.TextPrimary)
    }
}

@Composable
private fun PlanBaselineNumCell(text: String, widthDp: Int, muted: Boolean = false) {
    Text(text, modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), fontSize = 10.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace, color = if (muted) Color(0xFF94A3B8) else ProductionPlanningColors.TextPrimary)
}

@Composable
private fun PlanBaselineDiffBadgeCell(value: Double?, widthDp: Int) {
    val text = formatPlanComparisonValue(value)
    val tone = PlanBaselineLogic.diffTone(value)
    val (bg, fg) = when (tone) {
        PlanBaselineDiffTone.Positive -> Color(0xFFD1FAE5) to Color(0xFF059669)
        PlanBaselineDiffTone.Negative -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }
    Box(modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.CenterEnd) {
        Surface(shape = RoundedCornerShape(4.dp), color = bg) {
            Row(
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                when (tone) {
                    PlanBaselineDiffTone.Positive -> Icon(Icons.Default.TrendingUp, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
                    PlanBaselineDiffTone.Negative -> Text("-", color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    else -> {}
                }
                Text(text, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = fg, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlanBaselineDiffCell(value: Double?, widthDp: Int) {
    val text = formatPlanComparisonValue(value)
    val tone = PlanBaselineLogic.diffTone(value)
    val color = when (tone) {
        PlanBaselineDiffTone.Positive -> ProductionPlanningColors.Positive
        PlanBaselineDiffTone.Negative -> ProductionPlanningColors.Negative
        else -> ProductionPlanningColors.TextPrimary
    }
    Row(modifier = Modifier.width(widthDp.dp).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        when (tone) {
            PlanBaselineDiffTone.Positive -> Icon(Icons.Default.TrendingUp, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            PlanBaselineDiffTone.Negative -> Icon(Icons.Default.TrendingDown, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            else -> {}
        }
        Text(text, fontSize = 10.sp, textAlign = TextAlign.End, fontFamily = FontFamily.Monospace, color = color, fontWeight = if (tone != PlanBaselineDiffTone.Zero && tone != PlanBaselineDiffTone.Neutral) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun PlanBaselineTotalChip(label: String, value: String, color: Color, trend: Double? = null, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp).fillMaxWidth()) {
            Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (trend != null && trend > 0) Icon(Icons.Default.TrendingUp, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                if (trend != null && trend < 0) Icon(Icons.Default.TrendingDown, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
                Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun PlanBaselineTag(text: String, accent: Color = Color(0xFF64748B)) {
    Surface(shape = RoundedCornerShape(4.dp), color = accent.copy(alpha = 0.1f)) {
        Text(text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = accent)
    }
}

@Composable
private fun PlanBaselineGradientButton(label: String, icon: ImageVector, brush: Brush, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .then(if (enabled) Modifier.background(brush, shape).clickable(onClick = onClick) else Modifier.background(Color(0xFFE2E8F0), shape))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PlanBaselineOutlinedButton(label: String, icon: ImageVector, color: Color, enabled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(color.copy(alpha = if (enabled) 0.08f else 0.04f), shape)
            .border(1.dp, color.copy(alpha = if (enabled) 0.35f else 0.15f), shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (enabled) color else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, color = if (enabled) color else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun diffColor(value: Double): Color = when {
    value > 0 -> ProductionPlanningColors.Positive
    value < 0 -> ProductionPlanningColors.Negative
    else -> ProductionPlanningColors.TextPrimary
}
