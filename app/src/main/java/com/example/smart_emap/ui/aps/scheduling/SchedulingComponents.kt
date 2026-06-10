package com.example.smart_emap.ui.aps.scheduling

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Print
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog

private val leftColWidths = listOf(50.dp, 50.dp, 120.dp, 66.dp, 90.dp)
private val dateColWidth = 42.dp
private val matrixHeaderHeight = 36.dp
private val matrixRowHeight = 28.dp
private val matrixRowHeightTall = 40.dp
private val matrixFooterHeight = 28.dp

private fun matrixRowHeightFor(row: SchedulingMatrixRow): Dp =
    if (row is SchedulingMatrixRow.Item && row.materialShortage) matrixRowHeightTall else matrixRowHeight

@Composable
fun SchedulingPageHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF7C3AED)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
        Column {
            Text(
                "生産スケジューリングボード",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SchedulingTheme.TitleDark,
            )
            Text(
                "工程・ライン・期間で絞り込み、日別の計画と実績をマトリクスで確認。",
                fontSize = 12.sp,
                color = SchedulingTheme.Subtitle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulingFilterCard(
    processCd: String,
    lineId: Int?,
    lines: List<ApsProductionLineDto>,
    startDate: String,
    endDate: String,
    itemName: String?,
    productOptions: List<String>,
    onProcessChange: (String) -> Unit,
    onLineChange: (Int?) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onItemChange: (String?) -> Unit,
) {
    var processExpanded by remember { mutableStateOf(false) }
    var lineExpanded by remember { mutableStateOf(false) }
    var itemExpanded by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showPeriodMenu by remember { mutableStateOf(false) }

    val processLabel = SchedulingTheme.processLabel(processCd)
    val lineLabel = lines.firstOrNull { it.id == lineId }?.let { line ->
        line.lineName?.trim().orEmpty().ifEmpty { line.lineCode }
    } ?: "全ライン"
    val periodText = "$startDate ~ $endDate"

    val filterShape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, filterShape, spotColor = Color(0x402563EB), ambientColor = Color(0x202563EB)),
        shape = filterShape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFF0F7FF),
                            Color(0xFFF5F0FF),
                            Color(0xFFFAFCFF),
                        ),
                    ),
                )
                .border(1.dp, Color(0xFFD6E4FF), filterShape)
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SchedulingFilterDropdown(
                    label = "工程",
                    value = processLabel,
                    icon = Icons.Default.PrecisionManufacturing,
                    accent = Color(0xFF2563EB),
                    expanded = processExpanded,
                    onExpandedChange = { processExpanded = it },
                    width = SchedulingTheme.FilterProcessWidth,
                ) {
                    SchedulingTheme.processOptions.forEach { (cd, name) ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                onProcessChange(cd)
                                processExpanded = false
                            },
                        )
                    }
                }
                SchedulingFilterDropdown(
                    label = "ライン",
                    value = lineLabel,
                    icon = Icons.Default.Factory,
                    accent = Color(0xFF7C3AED),
                    expanded = lineExpanded,
                    onExpandedChange = { lineExpanded = it },
                    width = SchedulingTheme.FilterLineWidth,
                ) {
                    DropdownMenuItem(
                        text = { Text("全ライン", fontSize = 12.sp) },
                        onClick = { onLineChange(null); lineExpanded = false },
                    )
                    lines.forEach { line ->
                        val label = line.lineName?.trim().orEmpty().ifEmpty { line.lineCode }
                        DropdownMenuItem(
                            text = { Text(label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { onLineChange(line.id); lineExpanded = false },
                        )
                    }
                }
                SchedulingPeriodField(
                    label = "期間",
                    value = periodText,
                    onClick = { showPeriodMenu = true },
                )
                SchedulingFilterDropdown(
                    label = "製品",
                    value = itemName ?: "全製品",
                    icon = Icons.Default.Inventory2,
                    accent = Color(0xFF059669),
                    expanded = itemExpanded,
                    onExpandedChange = { itemExpanded = it },
                    width = SchedulingTheme.FilterProductWidth,
                ) {
                    DropdownMenuItem(
                        text = { Text("全製品", fontSize = 12.sp) },
                        onClick = { onItemChange(null); itemExpanded = false },
                    )
                    productOptions.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { onItemChange(name); itemExpanded = false },
                        )
                    }
                }
            }
        }
    }

    if (showPeriodMenu) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPeriodMenu = false },
            title = { Text("期間", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PeriodPickRow("開始日", startDate) { showStartPicker = true }
                    PeriodPickRow("終了日", endDate) { showEndPicker = true }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPeriodMenu = false }) {
                    Text("閉じる", color = SchedulingTheme.FeatureBlue, fontWeight = FontWeight.SemiBold)
                }
            },
        )
    }

    if (showStartPicker) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = SchedulingTheme.FeatureBlue,
            onDismiss = { showStartPicker = false },
            onConfirm = { onStartDateChange(it); showStartPicker = false },
        )
    }
    if (showEndPicker) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = SchedulingTheme.FeatureBlue,
            onDismiss = { showEndPicker = false },
            onConfirm = { onEndDateChange(it); showEndPicker = false },
        )
    }
}

@Composable
private fun PeriodPickRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(Color(0xFFF8FAFC))
            .border(1.dp, SchedulingTheme.CardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, color = SchedulingTheme.FilterLabel, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SchedulingTheme.TitleDark)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulingFilterDropdown(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    width: Dp,
    menuContent: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.width(width),
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .height(SchedulingTheme.FilterFieldHeight)
                .shadow(1.dp, shape, spotColor = accent.copy(alpha = 0.12f))
                .clip(shape)
                .background(Color.White)
                .border(1.dp, accent.copy(alpha = 0.28f), shape)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    lineHeight = 11.sp,
                )
                Text(
                    value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SchedulingTheme.TitleDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = accent.copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp),
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            menuContent()
        }
    }
}

@Composable
private fun SchedulingPeriodField(
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val accent = Color(0xFFD97706)
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .widthIn(min = SchedulingTheme.FilterPeriodMinWidth)
            .height(SchedulingTheme.FilterFieldHeight)
            .shadow(1.dp, shape, spotColor = accent.copy(alpha = 0.12f))
            .clip(shape)
            .clickable(onClick = onClick)
            .background(Color.White)
            .border(1.dp, accent.copy(alpha = 0.28f), shape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent, lineHeight = 11.sp)
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SchedulingTheme.TitleDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
fun SchedulingStatGrid(
    lineCount: Int,
    plannedTotal: String,
    avgEfficiency: String,
    requiredHours: String,
) {
    val cards = listOf(
        StatCardSpec("ライン数", lineCount.toString(), Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF1E3A8A)),
        StatCardSpec("生産計画合計", plannedTotal, Color(0xFF059669), Color(0xFF34D399), Color(0xFF065F46)),
        StatCardSpec("能率(本/H)平均", avgEfficiency, Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFF92400E)),
        StatCardSpec("所要生産時間", requiredHours, Color(0xFF7C3AED), Color(0xFFA78BFA), Color(0xFF5B21B6)),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                cards.forEach { spec ->
                    SchedulingStatCard(spec, modifier = Modifier.weight(1f))
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 1.dp),
            ) {
                items(cards) { spec -> SchedulingStatCard(spec) }
            }
        }
    }
}

private data class StatCardSpec(
    val label: String,
    val value: String,
    val accent: Color,
    val glow: Color,
    val valueColor: Color,
)

@Composable
private fun SchedulingStatCard(
    spec: StatCardSpec,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .then(if (modifier == Modifier) Modifier.width(128.dp) else Modifier)
            .shadow(10.dp, shape, spotColor = spec.accent.copy(alpha = 0.35f), ambientColor = spec.accent.copy(alpha = 0.15f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.88f),
                        Color.White.copy(alpha = 0.55f),
                        spec.accent.copy(alpha = 0.06f),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.75f), shape)
            .drawBehind {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(spec.glow, spec.accent)),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, 2.5.dp.toPx()),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(spec.glow.copy(alpha = 0.45f), Color.Transparent),
                    ),
                    radius = 36.dp.toPx(),
                    center = Offset(size.width - 8.dp.toPx(), 8.dp.toPx()),
                )
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                spec.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = spec.valueColor.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                spec.value,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = spec.valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SchedulingMatrixCard(
    featureLabel: String,
    dateRangeText: String,
    planExtendMode: Boolean,
    isLoading: Boolean,
    dates: List<String>,
    sections: List<SchedulingMatrixSection>,
    overallTotal: String,
    dailyTotals: Map<String, Int>,
    planExtendModeValue: Boolean,
    onPlanExtendModeChange: (Boolean) -> Unit,
    onPrint: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SchedulingTheme.CardBorder),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(featureLabel, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SchedulingTheme.FeatureBlue)
                        Text("・", color = Color(0xFF94A3B8))
                        Text("スケジューリングマトリクス", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SchedulingTheme.TitleDark)
                    }
                    SchedulingLegend(
                        planExtendMode = planExtendMode,
                        onPlanExtendModeChange = onPlanExtendModeChange,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xFFF1F5F9),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SchedulingTheme.CardBorder),
                        ) {
                            Text(
                                "期間：$dateRangeText",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                        Surface(
                            onClick = onPrint,
                            shape = RoundedCornerShape(999.dp),
                            color = Color(0xCCE0F2FE),
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.62f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF1E3A8A))
                                Text("印刷", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                            }
                        }
                    }
            }

            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                dates.isEmpty() -> Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("期間を指定して検索してください", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
                else -> SchedulingMatrixTable(
                    dates = dates,
                    sections = sections,
                    overallTotal = overallTotal,
                    dailyTotals = dailyTotals,
                    planExtendMode = planExtendModeValue,
                )
            }
        }
    }
}

@Composable
private fun SchedulingLegend(
    planExtendMode: Boolean,
    onPlanExtendModeChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 2.dp),
    ) {
        LegendSwatch(Color(0xE0FEF9C3), "実績")
        Text("｜", color = Color(0xFFCBD5E1), fontSize = 10.sp)
        LegendSwatch(SchedulingTheme.UpstreamCutting, "切断指示済")
        Text("｜", color = Color(0xFFCBD5E1), fontSize = 10.sp)
        LegendSwatch(SchedulingTheme.UpstreamPlanned, "計画")
        Switch(
            checked = planExtendMode,
            onCheckedChange = onPlanExtendModeChange,
            modifier = Modifier.graphicsLayer {
                scaleX = 0.5f
                scaleY = 0.5f
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = Color(0xFF16A34A),
                uncheckedTrackColor = Color(0xFF94A3B8),
            ),
        )
        Text(if (planExtendMode) "拡張" else "標準", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
    }
}

@Composable
private fun LegendSwatch(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            Modifier
                .size(width = 14.dp, height = 10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .border(1.dp, Color(0x1F0F172A), RoundedCornerShape(3.dp)),
        )
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
    }
}

@Composable
private fun SchedulingMatrixTable(
    dates: List<String>,
    sections: List<SchedulingMatrixSection>,
    overallTotal: String,
    dailyTotals: Map<String, Int>,
    planExtendMode: Boolean,
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(520.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, SchedulingTheme.CardBorder),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(matrixHeaderHeight),
            ) {
                SchedulingLeftHeader()
                Row(Modifier.horizontalScroll(horizontalScroll)) {
                    dates.forEach { date ->
                        SchedulingDateHeader(date)
                    }
                }
            }
            HorizontalDivider(color = SchedulingTheme.TableBorder)
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(verticalScroll),
            ) {
                Column {
                    sections.forEach { section ->
                        section.rows.forEach { row ->
                            SchedulingLeftRow(row, matrixRowHeightFor(row))
                        }
                    }
                    SchedulingLeftFooter(overallTotal)
                }
                Column(Modifier.horizontalScroll(horizontalScroll)) {
                    sections.forEach { section ->
                        section.rows.forEach { row ->
                            val rowHeight = matrixRowHeightFor(row)
                            SchedulingDateRow(
                                row = row,
                                dates = dates,
                                planExtendMode = planExtendMode,
                                rowHeight = rowHeight,
                            )
                        }
                    }
                    SchedulingDateFooter(dates, dailyTotals)
                }
            }
        }
    }
}

@Composable
private fun SchedulingLeftHeader() {
    val labels = listOf("ライン", "順位", "製品", "能率(本/H)", "生産計画")
    Row(
        Modifier
            .fillMaxHeight()
            .background(SchedulingTheme.TableHeaderBg),
    ) {
        labels.forEachIndexed { index, label ->
            MatrixHeaderCell(
                text = label,
                width = leftColWidths[index],
                height = matrixHeaderHeight,
                alignStart = index == 2,
            )
        }
    }
}

@Composable
private fun SchedulingDateHeader(date: String) {
    val weekend = SchedulingMatrixLogic.isWeekend(date)
    val today = SchedulingMatrixLogic.isToday(date)
    val bg = if (today) SchedulingTheme.TodayHeader else SchedulingTheme.TableHeaderBg
    Column(
        modifier = Modifier
            .width(dateColWidth)
            .height(matrixHeaderHeight)
            .background(bg)
            .border(0.5.dp, SchedulingTheme.TableBorder),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            SchedulingMatrixLogic.formatMatrixDate(date),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (weekend) SchedulingTheme.WeekendRed else Color(0xFF42526A),
        )
        Text(
            SchedulingMatrixLogic.weekdayLabel(date),
            fontSize = 9.sp,
            color = if (weekend) SchedulingTheme.WeekendRed else Color(0xFF6B7A90),
        )
    }
}

@Composable
private fun SchedulingLeftRow(row: SchedulingMatrixRow, rowHeight: Dp) {
    val isGroup = row is SchedulingMatrixRow.Group
    val bg = if (isGroup) Color(0xFFF8FBFF) else Color.White
    Row(Modifier.height(rowHeight).background(bg)) {
        MatrixBodyCell(
            width = leftColWidths[0],
            height = rowHeight,
            text = if (isGroup) (row as SchedulingMatrixRow.Group).lineName else "",
            fontWeight = if (isGroup) FontWeight.ExtraBold else FontWeight.Normal,
            textColor = if (isGroup) SchedulingTheme.GroupLineBlue else SchedulingTheme.TitleDark,
            alignStart = false,
        )
        MatrixBodyCell(
            width = leftColWidths[1],
            height = rowHeight,
            text = if (row is SchedulingMatrixRow.Item) (row.orderNo?.toString() ?: "-") else "",
        )
        MatrixItemCell(
            width = leftColWidths[2],
            height = rowHeight,
            row = row,
        )
        MatrixBodyCell(
            width = leftColWidths[3],
            height = rowHeight,
            text = when (row) {
                is SchedulingMatrixRow.Group -> SchedulingMatrixLogic.formatEfficiency(row.avgEfficiency)
                is SchedulingMatrixRow.Item -> SchedulingMatrixLogic.formatEfficiency(row.efficiencyRate)
            },
        )
        MatrixBodyCell(
            width = leftColWidths[4],
            height = rowHeight,
            text = when (row) {
                is SchedulingMatrixRow.Group -> SchedulingMatrixLogic.formatQty(row.sumPlannedOutputQty)
                is SchedulingMatrixRow.Item -> SchedulingMatrixLogic.formatQty(row.plannedOutputQty)
            },
        )
    }
}

@Composable
private fun MatrixItemCell(
    width: Dp,
    height: Dp,
    row: SchedulingMatrixRow,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(0.5.dp, SchedulingTheme.TableBorder)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (row is SchedulingMatrixRow.Item) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(row.itemName, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (row.materialShortage) {
                    Text("資材不足", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), lineHeight = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SchedulingDateRow(
    row: SchedulingMatrixRow,
    dates: List<String>,
    planExtendMode: Boolean,
    rowHeight: Dp,
) {
    Row(Modifier.height(rowHeight)) {
        dates.forEach { date ->
            val cell = SchedulingMatrixLogic.buildCell(row, date, planExtendMode)
            MatrixDateCell(
                cell = cell,
                height = rowHeight,
                showValue = row is SchedulingMatrixRow.Item && cell.displayValue > 0,
                isGroupRow = row is SchedulingMatrixRow.Group,
            )
        }
    }
}

@Composable
private fun MatrixDateCell(
    cell: SchedulingMatrixCell,
    height: Dp,
    showValue: Boolean,
    isGroupRow: Boolean = false,
) {
    val style = cell.style
    val bgBrush = when {
        style.gradient != null && style.gradientSplit != null -> {
            val (left, right) = style.gradient
            val split = style.gradientSplit.coerceIn(0f, 1f)
            Brush.horizontalGradient(
                0f to left,
                split to left,
                split to right,
                1f to right,
            )
        }
        style.backgroundColor != null -> Brush.linearGradient(listOf(style.backgroundColor, style.backgroundColor))
        else -> SchedulingTheme.toneColor(style.tone)?.let { Brush.linearGradient(listOf(it, it)) }
    }
    Box(
        modifier = Modifier
            .width(dateColWidth)
            .height(height)
            .then(
                when {
                    bgBrush != null -> Modifier.background(bgBrush)
                    isGroupRow -> Modifier.background(Color(0xFFF8FBFF))
                    else -> Modifier
                },
            )
            .border(0.5.dp, SchedulingTheme.TableBorder)
            .then(
                if (style.dueHighlight) {
                    Modifier.border(1.dp, SchedulingTheme.CellDue)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showValue && cell.displayText.isNotEmpty()) {
            Text(cell.displayText, fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SchedulingLeftFooter(overallTotal: String) {
    Row(Modifier.height(matrixFooterHeight).background(SchedulingTheme.FooterBg)) {
        MatrixBodyCell(leftColWidths[0], matrixFooterHeight, "合計", FontWeight.Bold)
        MatrixBodyCell(leftColWidths[1], matrixFooterHeight, "")
        MatrixBodyCell(leftColWidths[2], matrixFooterHeight, "", alignStart = true)
        MatrixBodyCell(leftColWidths[3], matrixFooterHeight, "")
        MatrixBodyCell(leftColWidths[4], matrixFooterHeight, overallTotal, FontWeight.Bold)
    }
}

@Composable
private fun SchedulingDateFooter(dates: List<String>, dailyTotals: Map<String, Int>) {
    Row(Modifier.height(matrixFooterHeight).background(SchedulingTheme.FooterBg)) {
        dates.forEach { date ->
            MatrixBodyCell(
                width = dateColWidth,
                height = matrixFooterHeight,
                text = SchedulingMatrixLogic.formatQty(dailyTotals[date] ?: 0),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MatrixHeaderCell(
    text: String,
    width: Dp,
    height: Dp,
    alignStart: Boolean = false,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(0.5.dp, SchedulingTheme.TableBorder)
            .background(SchedulingTheme.TableHeaderBg)
            .padding(horizontal = 4.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF42526A), maxLines = 2, lineHeight = 12.sp)
    }
}

@Composable
private fun MatrixBodyCell(
    width: Dp,
    height: Dp,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    textColor: Color = SchedulingTheme.TitleDark,
    alignStart: Boolean = false,
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .border(0.5.dp, SchedulingTheme.TableBorder)
            .padding(horizontal = 4.dp),
        contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(text, fontSize = 11.sp, fontWeight = fontWeight, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
