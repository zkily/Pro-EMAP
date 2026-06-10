package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val psvCardShape = RoundedCornerShape(10.dp)
private val psvTableShape = RoundedCornerShape(8.dp)
private val psvPrimary = Color(0xFF409EFF)
private val psvPrimaryDark = Color(0xFF2563EB)
private val psvAccentGrad = Brush.linearGradient(listOf(Color(0xFF409EFF), Color(0xFF6366F1)))
private val psvWarnGrad = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFE6A23C)))

@Composable
fun PlanSchedulePageBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC), Color(0xFFFFFFFF))),
                ),
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = (-30).dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x33409EFF), Color.Transparent)),
                    CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 60.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x226366F1), Color.Transparent)),
                    CircleShape,
                ),
        )
        content()
    }
}

@Composable
private fun PlanScheduleElevatedSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = psvCardShape,
    elevation: androidx.compose.ui.unit.Dp = 4.dp,
    accentTop: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.shadow(elevation, shape, spotColor = Color(0x330F172A), ambientColor = Color(0x180F172A)),
        shape = shape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8EDF5)),
    ) {
        Column {
            if (accentTop) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF409EFF), Color(0xFF818CF8), Color(0xFF409EFF)))),
                )
            }
            content()
        }
    }
}

@Composable
fun PlanScheduleHeader(
    totalFetched: Int,
    shown: Int,
    loading: Boolean,
    printEnabled: Boolean,
    onPrint: () -> Unit,
    onQuery: () -> Unit,
) {
    PlanScheduleFadeInUp(0) {
        PlanScheduleElevatedSurface(modifier = Modifier.fillMaxWidth(), elevation = 6.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF), shadowElevation = 2.dp) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = psvPrimary,
                            modifier = Modifier.padding(6.dp).size(18.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "生産スケジュール",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.2.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PlanScheduleStatChip("取得", totalFetched, Color(0xFF409EFF), Color(0xFFEFF6FF))
                            PlanScheduleStatChip("表示", shown, Color(0xFF16A34A), Color(0xFFECFDF5))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    PlanScheduleActionButton(
                        label = "印刷",
                        icon = Icons.Default.Print,
                        brush = psvWarnGrad,
                        enabled = printEnabled && !loading,
                        onClick = onPrint,
                    )
                    PlanScheduleActionButton(
                        label = "取得",
                        icon = Icons.Default.Refresh,
                        brush = psvAccentGrad,
                        enabled = !loading,
                        loading = loading,
                        onClick = onQuery,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanScheduleStatChip(label: String, value: Int, valueColor: Color, bg: Color) {
    Surface(shape = RoundedCornerShape(999.dp), color = bg, border = androidx.compose.foundation.BorderStroke(1.dp, valueColor.copy(alpha = 0.18f))) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
            AnimatedContent(
                targetState = value,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                label = "stat-$label",
            ) { v ->
                Text(v.toString(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = valueColor, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun PlanScheduleActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    brush: Brush,
    enabled: Boolean,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .height(32.dp)
            .shadow(if (enabled) 3.dp else 0.dp, shape, spotColor = Color(0x40000000))
            .clip(shape)
            .then(
                if (enabled) {
                    Modifier.background(brush, shape).clickable(onClick = onClick)
                } else {
                    Modifier.background(Color(0xFFE2E8F0), shape)
                },
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(icon, contentDescription = null, tint = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            }
            Text(label, color = if (enabled) Color.White else Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlanScheduleFilterBar(
    filterMonth: Int,
    filterEngineering: String,
    filterMachineName: String,
    filterProductName: String,
    machineOptions: List<String>,
    productOptions: List<String>,
    onMonthChange: (Int) -> Unit,
    onEngineeringChange: (String) -> Unit,
    onMachineChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
) {
    PlanScheduleFadeInUp(1) {
        PlanScheduleElevatedSurface(modifier = Modifier.fillMaxWidth(), elevation = 4.dp, accentTop = false) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFFAFBFC), Color.White)),
                    )
                    .padding(horizontal = 8.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top,
            ) {
                PlanScheduleFilterDropdown(
                    label = "月",
                    icon = Icons.Default.CalendarMonth,
                    value = filterMonth.toString(),
                    options = (1..12).map { it.toString() to "${it}月" },
                    onSelect = { onMonthChange(it.toIntOrNull() ?: filterMonth) },
                    theme = PlanScheduleFilterTheme(
                        accent = Color(0xFF2563EB),
                        labelBg = Color(0xFFDBEAFE),
                        fieldBg = Brush.linearGradient(listOf(Color(0xFFEFF6FF), Color.White)),
                        border = Color(0xFF93C5FD),
                        shadow = Color(0x402563EB),
                    ),
                    modifier = Modifier.weight(0.68f),
                )
                PlanScheduleFilterDropdown(
                    label = "工程",
                    icon = Icons.Default.Category,
                    value = filterEngineering,
                    options = listOf("" to "すべて", "成型" to "成型", "溶接" to "溶接"),
                    onSelect = onEngineeringChange,
                    theme = PlanScheduleFilterTheme(
                        accent = Color(0xFF7C3AED),
                        labelBg = Color(0xFFEDE9FE),
                        fieldBg = Brush.linearGradient(listOf(Color(0xFFF5F3FF), Color.White)),
                        border = Color(0xFFC4B5FD),
                        shadow = Color(0x407C3AED),
                    ),
                    modifier = Modifier.weight(0.82f),
                )
                PlanScheduleFilterDropdown(
                    label = "ライン",
                    icon = Icons.Default.Layers,
                    value = filterMachineName,
                    options = listOf("" to "すべて") + machineOptions.map { it to it },
                    onSelect = onMachineChange,
                    theme = PlanScheduleFilterTheme(
                        accent = Color(0xFF0D9488),
                        labelBg = Color(0xFFCCFBF1),
                        fieldBg = Brush.linearGradient(listOf(Color(0xFFECFDF5), Color.White)),
                        border = Color(0xFF5EEAD4),
                        shadow = Color(0x400D9488),
                    ),
                    modifier = Modifier.weight(1.15f),
                )
                PlanScheduleFilterDropdown(
                    label = "品名",
                    icon = Icons.Default.Inventory2,
                    value = filterProductName,
                    options = listOf("" to "すべて") + productOptions.map { it to it },
                    onSelect = onProductChange,
                    theme = PlanScheduleFilterTheme(
                        accent = Color(0xFFEA580C),
                        labelBg = Color(0xFFFFEDD5),
                        fieldBg = Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color.White)),
                        border = Color(0xFFFDBA74),
                        shadow = Color(0x40EA580C),
                    ),
                    modifier = Modifier.weight(1.15f),
                )
            }
        }
    }
}

private data class PlanScheduleFilterTheme(
    val accent: Color,
    val labelBg: Color,
    val fieldBg: Brush,
    val border: Color,
    val shadow: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanScheduleFilterDropdown(
    label: String,
    icon: ImageVector,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    theme: PlanScheduleFilterTheme,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value.ifBlank { "すべて" }
    val fieldShape = RoundedCornerShape(8.dp)
    Column(modifier = modifier) {
        Surface(shape = RoundedCornerShape(4.dp), color = theme.labelBg) {
            Text(
                label,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accent,
            )
        }
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Surface(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(32.dp)
                    .shadow(2.dp, fieldShape, spotColor = theme.shadow, ambientColor = theme.shadow),
                shape = fieldShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.border),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.fieldBg)
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .background(Brush.verticalGradient(listOf(theme.accent, theme.accent.copy(alpha = 0.55f)))),
                    )
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = theme.accent,
                        modifier = Modifier.padding(start = 6.dp, end = 4.dp).size(14.dp),
                    )
                    Text(
                        display,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = theme.accent.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (key, text) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text,
                                fontSize = 12.sp,
                                fontWeight = if (key == value) FontWeight.Bold else FontWeight.Normal,
                                color = if (key == value) theme.accent else Color(0xFF334155),
                            )
                        },
                        onClick = {
                            onSelect(key)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PlanScheduleSectionHead(
    section: PlanScheduleSection,
    varianceMap: Map<String, String>,
) {
    val crit = PlanScheduleLogic.sectionCriticalProgressLines(varianceMap, section)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Color(0x20409EFF)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33409EFF)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFEFF6FF), Color(0xFFF8FAFC), Color.White),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("月：${section.monthLabel}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E3A8A))
            Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFFCBD5E1)))
            Text("工程：${section.engineering}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
            if (crit.overPlan.isNotEmpty() || crit.severeBehind.isNotEmpty()) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (crit.overPlan.isNotEmpty()) {
                        PlanScheduleAlertLine("生産進捗", "計画超過", crit.overPlan.joinToString("、"))
                    }
                    if (crit.severeBehind.isNotEmpty()) {
                        PlanScheduleAlertLine("生産進捗", "大幅な遅れ", crit.severeBehind.joinToString("、"))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanScheduleAlertLine(label: String, tag: String, lines: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFFEE2E2), shadowElevation = 1.dp) {
            Text(tag, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
        }
        Text(lines, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun PlanScheduleMachineHead(
    machineName: String,
    ov: MachineOvHeadParts,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, bottom = 2.dp)
            .shadow(1.dp, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFFF8FAFC), Color.White)))
                .padding(start = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(Brush.verticalGradient(listOf(psvPrimary, psvPrimaryDark))),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("ライン：$machineName", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("操業度差異：", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                    Text(
                        ov.display,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ov.negative) Color(0xFFEF4444) else Color(0xFF1E293B),
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("生産進捗：", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                    PlanScheduleProgressTag(ov.kind, ov.progressLabel)
                }
            }
        }
    }
}

@Composable
fun PlanScheduleProgressTag(kind: OperationVarianceProgressKind, label: String) {
    val (bg, fg) = when (kind) {
        OperationVarianceProgressKind.Normal -> Color(0xFFDCFCE7) to Color(0xFF166534)
        OperationVarianceProgressKind.Ahead -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        OperationVarianceProgressKind.Behind -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        OperationVarianceProgressKind.OverPlan, OperationVarianceProgressKind.SevereBehind -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        OperationVarianceProgressKind.None -> Color(0xFFF1F5F9) to Color(0xFF64748B)
    }
    Surface(shape = RoundedCornerShape(4.dp), color = bg, shadowElevation = 1.dp) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}

@Composable
fun PlanScheduleDataTable(
    rows: List<PlanScheduleRow>,
    onProductClick: (PlanScheduleRow) -> Unit,
) {
    val hScroll = rememberScrollState()
    val headers = listOf("生産順", "品名", "開始日", "終了日", "計画数", "実績", "生産残数", "進捗度", "生産状況")
    val widths = listOf(48, 108, 78, 78, 64, 64, 72, 56, 88)
    val tableWidth = (widths.sum() + 12).dp
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, bottom = 2.dp)
            .shadow(3.dp, psvTableShape, spotColor = Color(0x250F172A)),
        shape = psvTableShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDCE3EE)),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .horizontalScroll(hScroll)
                    .width(tableWidth)
                    .background(Brush.verticalGradient(listOf(Color(0xFFEEF2FF), Color(0xFFF8FAFC))))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                headers.forEachIndexed { i, h ->
                    Text(
                        h,
                        modifier = Modifier.width(widths[i].dp).padding(horizontal = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        textAlign = if (i <= 1 || i == 8) TextAlign.Center else TextAlign.End,
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Column(modifier = Modifier.horizontalScroll(hScroll)) {
                rows.forEachIndexed { index, row ->
                    PlanScheduleTableRow(row, index, widths, tableWidth, onProductClick)
                }
            }
        }
    }
}

@Composable
private fun PlanScheduleTableRow(
    row: PlanScheduleRow,
    index: Int,
    widths: List<Int>,
    tableWidth: androidx.compose.ui.unit.Dp,
    onProductClick: (PlanScheduleRow) -> Unit,
) {
    val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
    Row(
        modifier = Modifier
            .width(tableWidth)
            .height(IntrinsicSize.Min)
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            (row.orderNo ?: row.id).toString(),
            modifier = Modifier.width(widths[0].dp),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF64748B),
            fontFamily = FontFamily.Monospace,
        )
        Text(
            row.itemName,
            modifier = Modifier
                .width(widths[1].dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onProductClick(row) },
                )
                .padding(horizontal = 3.dp, vertical = 1.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = psvPrimaryDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(PlanScheduleLogic.displayDate(row.startDate), modifier = Modifier.width(widths[2].dp), fontSize = 10.sp, textAlign = TextAlign.Center, color = Color(0xFF475569))
        Text(PlanScheduleLogic.displayDate(row.endDate), modifier = Modifier.width(widths[3].dp), fontSize = 10.sp, textAlign = TextAlign.Center, color = Color(0xFF475569))
        Text(formatProductionNumber(row.plannedQty), modifier = Modifier.width(widths[4].dp), fontSize = 10.sp, textAlign = TextAlign.End, color = Color(0xFF334155), fontFamily = FontFamily.Monospace)
        Text(formatProductionNumber(row.actualQty), modifier = Modifier.width(widths[5].dp), fontSize = 10.sp, textAlign = TextAlign.End, color = Color(0xFF0D9488), fontFamily = FontFamily.Monospace)
        Text(formatProductionNumber(row.remainingQty), modifier = Modifier.width(widths[6].dp), fontSize = 10.sp, textAlign = TextAlign.End, color = Color(0xFF334155), fontFamily = FontFamily.Monospace)
        Text(row.progressPct, modifier = Modifier.width(widths[7].dp), fontSize = 10.sp, textAlign = TextAlign.End, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
        PlanScheduleStatusCell(row.status, Modifier.width(widths[8].dp))
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
}

@Composable
private fun PlanScheduleStatusCell(status: PlanScheduleStatus, modifier: Modifier = Modifier) {
    val dotColor = when (status) {
        PlanScheduleStatus.Done -> Color(0xFFCBD5E1)
        PlanScheduleStatus.Ongoing -> Color(0xFF22C55E)
        PlanScheduleStatus.Pending -> Color(0xFFF59E0B)
    }
    val pulse = if (status == PlanScheduleStatus.Ongoing) {
        val transition = rememberInfiniteTransition(label = "ongoing-pulse")
        transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulse-scale",
        ).value
    } else {
        1f
    }
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.Center) {
            if (status == PlanScheduleStatus.Ongoing) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(pulse)
                        .clip(CircleShape)
                        .background(Color(0x3322C55E)),
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .scale(if (status == PlanScheduleStatus.Ongoing) pulse else 1f)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(1.dp, Color(0x26000000), CircleShape),
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(status.label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PlanScheduleGroupedContent(
    sections: List<PlanScheduleSection>,
    varianceMap: Map<String, String>,
    loading: Boolean,
    onProductClick: (PlanScheduleRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    PlanScheduleElevatedSurface(modifier = modifier.fillMaxSize(), elevation = 5.dp) {
        when {
            loading && sections.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = psvPrimary, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
                }
            }
            sections.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
                        Text("表示するデータがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    var animIndex = 2
                    sections.forEach { section ->
                        PlanScheduleFadeInUp(animIndex++) {
                            PlanScheduleSectionHead(section, varianceMap)
                        }
                        section.machines.forEach { machine ->
                            val ov = PlanScheduleLogic.machineOvHeadParts(varianceMap, section.engineering, machine.machineName)
                            PlanScheduleFadeInUp(animIndex++) {
                                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                                    PlanScheduleMachineHead(machine.machineName, ov)
                                    PlanScheduleDataTable(machine.rows, onProductClick)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanScheduleFadeInUp(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(320, delayMillis = index * 35, easing = FastOutSlowInEasing)) +
            slideInVertically(tween(320, delayMillis = index * 35, easing = FastOutSlowInEasing)) { it / 5 },
    ) {
        content()
    }
}

@Composable
fun PlanSchedulePlanUpdatesDialog(
    title: String,
    rows: List<PlanUpdateDisplayRow>,
    loading: Boolean,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 8 },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .shadow(16.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(title, modifier = Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "閉じる", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(10.dp)
                            .heightIn(min = 100.dp, max = 420.dp),
                    ) {
                        when {
                            loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = psvPrimary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                            rows.isEmpty() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("日生産スケジュールデータがありません", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                            else -> {
                                val headers = listOf("工程", "ライン", "操作員", "計画日", "計画数", "能率", "所要時間")
                                val widths = listOf(48, 80, 68, 78, 58, 50, 58)
                                val tableRows = rows.map { item ->
                                    val r = item.record
                                    listOf(
                                        if (item.showProcess) r.processName.orEmpty().ifBlank { "—" } else "",
                                        if (item.showMachine) r.machineName.orEmpty().ifBlank { "—" } else "",
                                        if (item.showOperator) r.operator.orEmpty().ifBlank { "—" } else "",
                                        PlanScheduleLogic.displayDate(r.planDate),
                                        formatProductionNumber(r.quantity),
                                        r.efficiencyRate?.let { formatProductionNumber(it.toInt()) }.orEmpty().ifBlank { "—" },
                                        PlanScheduleLogic.formatRequiredTime(r.quantity, r.efficiencyRate),
                                    )
                                }
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    ProductionDataTable(headers, tableRows, widths)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
