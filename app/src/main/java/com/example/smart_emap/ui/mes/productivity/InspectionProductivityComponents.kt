package com.example.smart_emap.ui.mes.productivity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.smart_emap.data.model.InspectionProductivityDailyRowDto
import com.example.smart_emap.data.model.InspectionProductivityDefectRowDto
import com.example.smart_emap.data.model.InspectionProductivityInspectorRowDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.InspectionProductivityProductRowDto
import com.example.smart_emap.data.model.InspectionProductivitySessionRowDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.ui.erp.production.planning.ProductionBeautifulDatePickerDialog
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors

private val ipaEmerald = Color(0xFF059669)
private val ipaEmeraldLight = Color(0xFF10B981)
private val ipaIndigo = Color(0xFF6366F1)
private val ipaSky = Color(0xFF38BDF8)
private val heroGradient = Brush.linearGradient(listOf(Color.White, Color(0xFFF0FDF4)))
private val heroAccent = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
private val panelShape = RoundedCornerShape(14.dp)
private val fieldShape = RoundedCornerShape(10.dp)

@Composable
fun IpaPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))),
    ) {
        Box(
            Modifier
                .size(220.dp)
                .offset((-60).dp, (-50).dp)
                .clip(CircleShape)
                .background(Color(0x206366F1)),
        )
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(30.dp, 80.dp)
                .clip(CircleShape)
                .background(Color(0x1810B981)),
        )
        content()
    }
}

@Composable
fun IpaHeroBar(
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
                    color = ipaIndigo,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    "検査工程 — 生産性分析",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A),
                )
                Text(
                    "inspection_management 実績 · 能率 · 不良率 · 稼働",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    lineHeight = 13.sp,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!rangeLabel.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0x146366F1), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x266366F1))) {
                    Text(
                        rangeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                    )
                }
            }
            IpaGhostButton("更新", Icons.Default.Refresh, enabled = !loading, onClick = onRefresh)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IpaToolbarCard(
    startDate: String,
    endDate: String,
    filterInspectorId: Int?,
    filterProductCd: String,
    inspectorOptions: List<UserListItemDto>,
    productOptions: List<ErpProductDto>,
    includeIncomplete: Boolean,
    loading: Boolean,
    onDateRangeChange: (String, String) -> Unit,
    onInspectorChange: (Int?) -> Unit,
    onProductChange: (String) -> Unit,
    onIncludeIncompleteChange: (Boolean) -> Unit,
    onAnalyze: () -> Unit,
) {
    val inspectorDropdownOptions = listOf("" to "（すべて）") +
        inspectorOptions.mapNotNull { u ->
            val id = u.id ?: return@mapNotNull null
            id.toString() to u.displayLabel().ifBlank { u.username.orEmpty() }
        }
    val productDropdownOptions = listOf("" to "（すべて）") +
        productOptions.map { p ->
            val cd = p.productCode
            cd to (p.productName.ifBlank { cd })
        }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, panelShape)
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFCFCFF), Color(0xFFF1F5F9))))
            .border(1.dp, Color.White, panelShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        val fields: @Composable () -> Unit = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IpaPillDateField(startDate = startDate, endDate = endDate, onDateRangeChange = onDateRangeChange)
                IpaPillDropdownField(
                    pillLabel = "検査員",
                    pillIcon = Icons.Default.Person,
                    pillColors = Brush.linearGradient(listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF))),
                    pillTextColor = Color(0xFF6B21A8),
                    value = filterInspectorId?.toString().orEmpty(),
                    options = inspectorDropdownOptions,
                    onSelect = { onInspectorChange(it.toIntOrNull()) },
                    minWidth = 148.dp,
                )
                IpaPillDropdownField(
                    pillLabel = "製品名",
                    pillIcon = Icons.Default.Inventory2,
                    pillColors = Brush.linearGradient(listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5))),
                    pillTextColor = Color(0xFF047857),
                    value = filterProductCd,
                    options = productDropdownOptions,
                    onSelect = onProductChange,
                    minWidth = 160.dp,
                )
                IpaPillCheckField(checked = includeIncomplete, onCheckedChange = onIncludeIncompleteChange)
            }
        }
        if (maxWidth < 520.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fields()
                IpaPrimaryButton("分析実行", enabled = !loading, onClick = onAnalyze, modifier = Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { fields() }
                Spacer(Modifier.width(8.dp))
                IpaPrimaryButton("分析実行", enabled = !loading, onClick = onAnalyze)
            }
        }
    }
}

@Composable
fun IpaKpiGrid(cards: List<IpaKpiCard>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cards.forEach { card ->
            IpaKpiCardItem(card, Modifier.width(152.dp))
        }
    }
}

@Composable
private fun IpaKpiCardItem(card: IpaKpiCard, modifier: Modifier = Modifier) {
    val style = kpiToneStyle(card.tone)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(2.dp, shape, spotColor = style.shadow)
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color.White, style.bg)))
            .border(1.dp, style.border, shape),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(style.accent),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(style.iconBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(kpiIcon(card.icon), null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(card.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = style.labelColor)
                Text(
                    card.value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (card.tone == IpaKpiTone.Emerald) 22.sp else 20.sp,
                    color = style.valueColor,
                    lineHeight = 24.sp,
                )
                Text(card.hint, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = style.hintColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private data class IpaKpiToneStyle(
    val bg: Color,
    val border: Color,
    val accent: Brush,
    val iconBrush: Brush,
    val labelColor: Color,
    val valueColor: Color,
    val hintColor: Color,
    val shadow: Color,
)

private fun kpiToneStyle(tone: IpaKpiTone): IpaKpiToneStyle = when (tone) {
    IpaKpiTone.Indigo -> IpaKpiToneStyle(
        Color(0xFFEEF2FF), Color(0x336366F1),
        Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))),
        Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5))),
        Color(0xFF6366F1), Color(0xFF4338CA), Color(0xFF818CF8), Color(0x1A6366F1),
    )
    IpaKpiTone.Sky -> IpaKpiToneStyle(
        Color(0xFFE0F2FE), Color(0x330EA5E9),
        Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF38BDF8))),
        Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF0284C7))),
        Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF38BDF8), Color(0x1A0EA5E9),
    )
    IpaKpiTone.Amber -> IpaKpiToneStyle(
        Color(0xFFFFEDD5), Color(0x33F97316),
        Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFFB923C))),
        Brush.linearGradient(listOf(Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFEA580C))),
        Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFB923C), Color(0x1AF97316),
    )
    IpaKpiTone.Emerald -> IpaKpiToneStyle(
        Color(0xFFD1FAE5), Color(0x4510B981),
        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399))),
        Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))),
        Color(0xFF059669), Color(0xFF047857), Color(0xFF34D399), Color(0x2810B981),
    )
    IpaKpiTone.Violet -> IpaKpiToneStyle(
        Color(0xFFEDE9FE), Color(0x338B5CF6),
        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))),
        Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF7C3AED))),
        Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFFA78BFA), Color(0x1A8B5CF6),
    )
}

private fun kpiIcon(icon: IpaKpiIcon): ImageVector = when (icon) {
    IpaKpiIcon.Sessions -> Icons.Default.CheckCircle
    IpaKpiIcon.Production -> Icons.Default.Inventory2
    IpaKpiIcon.Defect -> Icons.Default.Warning
    IpaKpiIcon.Efficiency -> Icons.AutoMirrored.Filled.ShowChart
    IpaKpiIcon.Runtime -> Icons.Default.Timer
}

@Composable
private fun IpaPillDateField(
    startDate: String,
    endDate: String,
    onDateRangeChange: (String, String) -> Unit,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    if (pickStart) {
        ProductionBeautifulDatePickerDialog(
            value = startDate,
            title = "開始日",
            onDismiss = { pickStart = false },
            onConfirm = {
                onDateRangeChange(it, endDate)
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
            onConfirm = { onDateRangeChange(startDate, it); pickEnd = false },
        )
    }
    val rangeText = when {
        startDate.isNotBlank() && endDate.isNotBlank() -> "${startDate.take(10)} ~ ${endDate.take(10)}"
        startDate.isNotBlank() -> startDate.take(10)
        endDate.isNotBlank() -> endDate.take(10)
        else -> "期間を選択"
    }
    IpaPillFieldShell(
        pillLabel = "期間",
        pillIcon = Icons.Default.CalendarMonth,
        pillColors = Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))),
        pillTextColor = Color(0xFF3730A3),
        minWidth = 196.dp,
        onClick = { pickStart = true },
    ) {
        Text(
            rangeText,
            fontSize = 12.sp,
            color = Color(0xFF334155),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IpaPillDropdownField(
    pillLabel: String,
    pillIcon: ImageVector,
    pillColors: Brush,
    pillTextColor: Color,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    minWidth: androidx.compose.ui.unit.Dp,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: "（すべて）"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        IpaPillFieldShell(
            pillLabel = pillLabel,
            pillIcon = pillIcon,
            pillColors = pillColors,
            pillTextColor = pillTextColor,
            minWidth = minWidth,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        ) {
            Text(display, fontSize = 12.sp, color = Color(0xFF334155), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = { onSelect(key); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun IpaPillCheckField(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    IpaPillFieldShell(
        pillLabel = "オプション",
        pillIcon = null,
        pillColors = Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))),
        pillTextColor = Color(0xFF92400E),
        minWidth = 148.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(28.dp),
                colors = CheckboxDefaults.colors(checkedColor = ipaEmerald),
            )
            Text("未確定を含む", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun IpaPillFieldShell(
    pillLabel: String,
    pillIcon: ImageVector?,
    pillColors: Brush,
    pillTextColor: Color,
    minWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .heightIn(min = 32.dp)
            .widthIn(min = minWidth)
            .clip(fieldShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), fieldShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .background(pillColors)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (pillIcon != null) {
                Icon(pillIcon, null, tint = pillTextColor, modifier = Modifier.size(13.dp))
            }
            Text(pillLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pillTextColor)
        }
        Box(Modifier.padding(horizontal = 8.dp)) {
            content()
        }
    }
}

@Composable
fun IpaDailyChartCard(daily: List<InspectionProductivityDailyRowDto>) {
    IpaPanel(title = "日別推移", badge = "生産数 · 能率", titleIcon = Icons.AutoMirrored.Filled.ShowChart) {
        if (daily.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("データがありません", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
            }
        } else {
            val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1)
            val effMax = (daily.maxOfOrNull { (it.efficiencyPerHour ?: 0.0).toInt() } ?: 1).coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IpaChartLegend("生産数", ipaSky)
                Spacer(Modifier.width(12.dp))
                IpaChartLegend("能率", ipaEmeraldLight)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0x99F8FAFC), Color(0x4DFFFFFF))))
                    .border(1.dp, Color(0xCCE2E8F0), RoundedCornerShape(10.dp)),
            ) {
                Row(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .width(34.dp)
                            .fillMaxHeight()
                            .padding(top = 28.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        (4 downTo 0).forEach { tick ->
                            Text(
                                "${(qtyMax * tick / 4)}",
                                fontSize = 8.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        IpaDailyProductivityChart(daily, Modifier.fillMaxSize())
                    }
                    Column(
                        Modifier
                            .width(34.dp)
                            .fillMaxHeight()
                            .padding(top = 28.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        (4 downTo 0).forEach { tick ->
                            Text(
                                "${(effMax * tick / 4)}",
                                fontSize = 8.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            IpaChartXAxis(daily.map { it.day })
        }
    }
}

@Composable
fun IpaInspectorProductSplit(
    inspectorRows: List<InspectionProductivityInspectorRowDto>,
    productRows: List<InspectionProductivityProductRowDto>,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { IpaInspectorSection(inspectorRows) }
                Box(Modifier.weight(1f)) { IpaProductSection(productRows) }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IpaInspectorSection(inspectorRows)
                IpaProductSection(productRows)
            }
        }
    }
}

@Composable
fun IpaInspectorSection(
    rows: List<InspectionProductivityInspectorRowDto>,
) {
    val topInspectors = rows.take(8)
    IpaPanel(title = "検査員別", badge = null, titleIcon = Icons.Default.Person) {
        if (topInspectors.isNotEmpty()) {
            IpaInspectorBarChart(topInspectors)
            Spacer(Modifier.height(8.dp))
        }
        IpaInspectorTable(rows)
    }
}

@Composable
fun IpaProductSection(rows: List<InspectionProductivityProductRowDto>) {
    IpaPanel(title = "製品別", badge = "${rows.size} 品目", titleIcon = Icons.Default.Inventory2) {
        IpaProductTable(rows)
    }
}

@Composable
private fun IpaInspectorBarChart(rows: List<InspectionProductivityInspectorRowDto>) {
    val colors = listOf(
        Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEC4899),
    )
    val maxVal = rows.maxOfOrNull { it.efficiencyPerHour ?: 0.0 }?.coerceAtLeast(1.0) ?: 1.0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (rows.size * 28).dp, max = 180.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0x99F8FAFC), Color(0x4DFFFFFF))))
            .border(1.dp, Color(0xCCE2E8F0), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        rows.forEachIndexed { index, row ->
            val value = row.efficiencyPerHour ?: 0.0
            val color = colors[index % colors.size]
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.inspectorName.orEmpty(),
                    modifier = Modifier.width(72.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                        .background(Color(0xFFF1F5F9)),
                ) {
                    val fraction = (value / maxVal).toFloat().coerceIn(0f, 1f)
                    if (fraction > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.65f)))),
                        )
                    }
                }
                Text(
                    InspectionProductivityLogic.fmtEfficiency(value),
                    modifier = Modifier.width(44.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
fun IpaProductRankSection(
    productRankList: List<InspectionProductivityProductRankingDto>,
    selectedRanking: InspectionProductivityProductRankingDto?,
    podiumInspectors: List<InspectionProductivityInspectorRowDto>,
    rankViewProductCd: String,
    topOverview: List<InspectionProductivityProductRankingDto>,
    onProductSelect: (String) -> Unit,
    onDetailClick: (String) -> Unit,
) {
    if (productRankList.isEmpty()) return
    val rankShape = panelShape
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, rankShape)
            .clip(rankShape)
            .background(Brush.linearGradient(listOf(Color.White, Color(0x26FEF3C7))))
            .border(1.dp, Color(0x2EF59E0B), rankShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "製品別 · 検査員能率ランキング",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
            )
            ProductionDropdownFilter(
                "製品",
                rankViewProductCd,
                productRankList.map { p ->
                    p.productCd to InspectionProductivityLogic.productRankOptionLabel(p)
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
                    "生産 ${InspectionProductivityLogic.fmtInt(ranking.sumActualQty)} · 検査員 ${ranking.rankedInspectorCount ?: 0} 名",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                )
            }

            if (podiumInspectors.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    podiumInspectors.forEach { item ->
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
                                    if (rank == 1) Color(0x59F59E0B) else Color(0xE2E8F0),
                                    RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(InspectionProductivityLogic.rankMedal(rank), fontSize = 22.sp)
                            Text(
                                item.inspectorName.orEmpty(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                InspectionProductivityLogic.fmtEfficiency(item.efficiencyPerHour),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ipaEmerald,
                            )
                            Text("個/時", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            val inspectors = ranking.inspectors.orEmpty()
            if (inspectors.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                IpaInspectorBarChart(
                    inspectors.map { row ->
                        row.copy(inspectorName = "#${row.rank} ${row.inspectorName.orEmpty()}")
                    },
                )
                Spacer(Modifier.height(8.dp))
                IpaRankInspectorTable(inspectors)
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
                        "能率を算出できる検査員データがありません（正味稼働時間が必要です）",
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
        IpaRankOverviewTable(topOverview, onDetailClick)
    }
}

@Composable
fun IpaDefectSection(
    rows: List<InspectionProductivityDefectRowDto>,
    defectLabel: (String) -> String,
) {
    if (rows.isEmpty()) return
    IpaPanel(title = "不良内訳（KT09）", badge = null) {
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
                        InspectionProductivityLogic.fmtInt(row.qty),
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
fun IpaSessionDetailSection(rows: List<InspectionProductivitySessionRowDto>) {
    IpaPanel(title = "セッション明細", badge = "${rows.size} 件") {
        IpaSessionTable(rows)
    }
}

@Composable
fun IpaEmptyState() {
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
            Icon(Icons.Default.AssignmentTurnedIn, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text("期間を選択して「分析実行」をクリック", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun IpaPanel(
    title: String,
    badge: String?,
    titleIcon: ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xFFF2FFFFFF), Color(0xE6F8FAFC))))
            .border(1.dp, Color(0xE6FFFFFF), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (titleIcon != null) {
                    Icon(titleIcon, null, tint = ipaIndigo, modifier = Modifier.size(15.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            }
            if (!badge.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0x1A6366F1)) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ipaIndigo,
                    )
                }
            }
        }
        content()
    }
}

@Composable
private fun IpaChartXAxis(days: List<String?>) {
    if (days.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = 36.dp, top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        days.forEach { day ->
            Text(
                InspectionProductivityLogic.chartDayLabel(day),
                fontSize = 8.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 28.dp),
            )
        }
    }
}

@Composable
private fun IpaChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
    }
}

@Composable
private fun IpaDailyProductivityChart(
    daily: List<InspectionProductivityDailyRowDto>,
    modifier: Modifier = Modifier,
) {
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

        val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1).toFloat() * 1.15f
        val effMax = (daily.maxOfOrNull { it.efficiencyPerHour?.toFloat() ?: 0f } ?: 1f).coerceAtLeast(1f) * 1.15f

        for (tick in 0..4) {
            val y = chartBottom - chartHeight * tick / 4f
            drawLine(
                Color(0xFFF1F5F9),
                Offset(chartLeft, y),
                Offset(chartRight, y),
                1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
            )
        }
        drawLine(Color(0xFFE2E8F0), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)

        val barWidth = (groupWidth * 0.45f).coerceIn(4f, 24f)
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val qty = (row.sumActualQty ?: 0).toFloat()
            val barH = qty / qtyMax * chartHeight
            if (barH > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(listOf(ipaSky, ipaIndigo)),
                    topLeft = Offset(centerX - barWidth / 2f, chartBottom - barH),
                    size = Size(barWidth, barH),
                )
            }
        }

        val effPoints = daily.mapIndexed { index, row ->
            val x = chartLeft + groupWidth * index + groupWidth / 2f
            val eff = row.efficiencyPerHour?.toFloat() ?: 0f
            val y = chartTop + chartHeight * (1f - eff / effMax)
            x to y
        }
        if (effPoints.size >= 2) {
            val path = Path()
            effPoints.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, ipaEmeraldLight, style = Stroke(width = 2.5f))
            val areaPath = Path().apply {
                moveTo(effPoints.first().first, chartBottom)
                effPoints.forEach { (x, y) -> lineTo(x, y) }
                lineTo(effPoints.last().first, chartBottom)
                close()
            }
            drawPath(areaPath, ipaEmeraldLight.copy(alpha = 0.12f))
        }
        effPoints.forEach { (x, y) ->
            drawCircle(Color.White, 4f, Offset(x, y))
            drawCircle(ipaEmeraldLight, 3f, Offset(x, y))
        }
    }
}

@Composable
private fun IpaInspectorTable(rows: List<InspectionProductivityInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("検査員", "件", "生産", "不良率", "能率")
    val widths = listOf(88, 40, 56, 52, 44)
    Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        IpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IpaCell(row.inspectorName.orEmpty(), widths[0], align = TextAlign.Start)
                IpaCell("${row.sessionCount ?: 0}", widths[1])
                IpaCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), widths[2])
                IpaCell(InspectionProductivityLogic.fmtPct(row.defectRatePercent), widths[3], color = Color(0xFFEA580C), bold = true)
                IpaCell(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[4], color = ipaEmerald, bold = true)
            }
        }
        if (rows.isEmpty()) IpaEmptyRow(widths.sum())
    }
}

@Composable
private fun IpaProductTable(rows: List<InspectionProductivityProductRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "件", "生産", "不良率", "能率")
    val widths = listOf(72, 100, 36, 56, 52, 44)
    Column(Modifier.heightIn(max = 340.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        IpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IpaCell(row.productCd.orEmpty(), widths[0], fontMono = true, align = TextAlign.Start)
                IpaCell(row.productName.orEmpty(), widths[1], align = TextAlign.Start)
                IpaCell("${row.sessionCount ?: 0}", widths[2])
                IpaCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), widths[3])
                IpaCell(InspectionProductivityLogic.fmtPct(row.defectRatePercent), widths[4], color = Color(0xFFEA580C), bold = true)
                IpaCell(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[5], color = ipaEmerald, bold = true)
            }
        }
        if (rows.isEmpty()) IpaEmptyRow(widths.sum())
    }
}

@Composable
private fun IpaRankInspectorTable(rows: List<InspectionProductivityInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("順位", "検査員", "件", "生産", "能率", "不良率", "稼働")
    val widths = listOf(44, 88, 36, 56, 52, 52, 44)
    Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        IpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IpaRankBadge(row.rank, widths[0])
                IpaCell(row.inspectorName.orEmpty(), widths[1], align = TextAlign.Start)
                IpaCell("${row.sessionCount ?: 0}", widths[2])
                IpaCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), widths[3])
                IpaCell(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[4], color = ipaEmerald, bold = true)
                IpaCell(InspectionProductivityLogic.fmtPct(row.defectRatePercent), widths[5])
                IpaCell(InspectionProductivityLogic.fmtDurationMin(row.sumNetProductionMin), widths[6])
            }
        }
    }
}

@Composable
private fun IpaRankOverviewTable(
    rows: List<InspectionProductivityProductRankingDto>,
    onDetailClick: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "TOP検査員", "能率", "対象人数", "")
    val widths = listOf(72, 100, 88, 52, 56, 48)
    Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        IpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                IpaCell(row.productCd, widths[0], fontMono = true, align = TextAlign.Start)
                IpaCell(row.productName.orEmpty(), widths[1], align = TextAlign.Start)
                IpaCell(row.topInspectorName ?: "—", widths[2], align = TextAlign.Start)
                IpaCell(InspectionProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour), widths[3], color = ipaEmerald, bold = true)
                IpaCell("${row.rankedInspectorCount ?: 0}", widths[4])
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
private fun IpaSessionTable(rows: List<InspectionProductivitySessionRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("生産日", "検査員", "CD", "製品名", "生産", "不良", "不良率", "能率", "稼働", "停止", "状態")
    val widths = listOf(72, 72, 68, 88, 44, 40, 48, 44, 40, 40, 52)
    Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()).horizontalScroll(scroll)) {
        IpaTableHeader(headers, widths)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                IpaCell(row.productionDay?.take(10).orEmpty(), widths[0], fontMono = true)
                IpaCell(row.inspectorDisplayName.orEmpty(), widths[1], align = TextAlign.Start)
                IpaCell(row.productCd.orEmpty(), widths[2], fontMono = true)
                IpaCell(row.productName.orEmpty(), widths[3], align = TextAlign.Start)
                IpaCell(InspectionProductivityLogic.fmtInt(row.actualProductionQuantity), widths[4])
                IpaCell(InspectionProductivityLogic.fmtInt(row.defectQty), widths[5])
                IpaCell(InspectionProductivityLogic.fmtPct(row.defectRatePercent), widths[6])
                IpaCell(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour), widths[7], color = ipaEmerald, bold = true)
                IpaCell(row.netProductionMin?.toString() ?: "—", widths[8])
                IpaCell(row.pausedMin?.toString() ?: "—", widths[9])
                IpaStatusCell(row.isCompleted == true, widths[10])
            }
        }
        if (rows.isEmpty()) IpaEmptyRow(widths.sum())
    }
}

@Composable
private fun IpaRankBadge(rank: Int?, widthDp: Int) {
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
            Text(
                rank?.toString() ?: "—",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = fg,
            )
        }
    }
}

@Composable
private fun IpaStatusCell(completed: Boolean, widthDp: Int) {
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
private fun IpaTableHeader(headers: List<String>, widths: List<Int>) {
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
private fun IpaCell(
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
private fun IpaEmptyRow(totalWidth: Int) {
    Text(
        "データがありません",
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        fontSize = 11.sp,
        color = ProductionPlanningColors.TextSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IpaGhostButton(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(Color(0xB3FFFFFF))
            .border(1.dp, Color(0x5994A3B8), shape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, null, tint = if (enabled) Color(0xFF475569) else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) Color(0xFF475569) else Color(0xFF94A3B8))
    }
}

@Composable
private fun IpaPrimaryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val primaryBrush = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
    Row(
        modifier = modifier
            .height(32.dp)
            .shadow(if (enabled) 4.dp else 0.dp, shape, spotColor = Color(0x666366F1))
            .clip(shape)
            .background(if (enabled) primaryBrush else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (enabled) Color.White else Color(0xFF94A3B8))
    }
}
