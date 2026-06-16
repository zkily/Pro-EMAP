package com.example.smart_emap.ui.mes.utilization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.InspectionUtilizationDailyInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationInspectorRowDto
import com.example.smart_emap.data.model.InspectionUtilizationSessionGapDto
import com.example.smart_emap.ui.erp.production.planning.ProductionCompactDateRangeField
import com.example.smart_emap.ui.erp.production.planning.ProductionDropdownFilter
import com.example.smart_emap.ui.erp.production.planning.ProductionFilterLabel
import com.example.smart_emap.ui.erp.production.planning.ProductionPlanningColors
import com.example.smart_emap.ui.erp.production.planning.ProductionBeautifulDatePickerDialog

private val iuaGreen = Color(0xFF059669)
private val iuaGreenLight = Color(0xFF10B981)
private val iuaIndigo = Color(0xFF6366F1)
private val iuaAmber = Color(0xFFF59E0B)
private val iuaAmberDark = Color(0xFFB45309)
private val heroGradient = Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xE6F0FDF4)))
private val heroAccent = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))
private val heroGlassBg = Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xE6F0FDF4)))
private val heroShape = RoundedCornerShape(16.dp)
private val panelShape = RoundedCornerShape(12.dp)

@Composable
fun IuaPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF0FDF4)))),
    ) {
        Box(
            Modifier
                .size(280.dp)
                .offset((-60).dp, (-60).dp)
                .clip(CircleShape)
                .background(Color(0x5910B981)),
        )
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(30.dp, 60.dp)
                .clip(CircleShape)
                .background(Color(0x383B82F6)),
        )
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.BottomStart)
                .offset((-40).dp, (-100).dp)
                .clip(CircleShape)
                .background(Color(0x2E6366F1)),
        )
        content()
    }
}

@Composable
fun IuaHeroBar(
    defaultStandardHours: Double,
    inspectorScheduleApplied: Boolean,
    rangeLabel: String?,
    loading: Boolean,
    reportBusy: Boolean,
    reportEnabled: Boolean,
    dataGaps: List<String>,
    sessionsWithoutTime: List<InspectionUtilizationSessionGapDto>,
    onReportCommand: (InspectionUtilizationReportCommand) -> Unit,
    onRefresh: () -> Unit,
) {
    var gapsDialogOpen by remember { mutableStateOf(false) }
    if (gapsDialogOpen && dataGaps.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { gapsDialogOpen = false },
            title = { Text("データ上の留意点", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    dataGaps.forEach { gap ->
                        Text("· $gap", fontSize = 11.sp, color = Color(0xFF475569), lineHeight = 15.sp)
                        if (gap.contains("正味稼働時間が算出できない") && sessionsWithoutTime.isNotEmpty()) {
                            sessionsWithoutTime.forEach { s ->
                                Text(
                                    "ID ${s.id ?: "—"} · ${s.productionDay.orEmpty()} · ${s.inspectorName ?: "検査員未割当"} · ${InspectionUtilizationLogic.sessionProductLabel(s)} · ${InspectionUtilizationLogic.sessionTimeGapReason(s)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 14.sp,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Text("閉じる", modifier = Modifier.clickable { gapsDialogOpen = false }, fontSize = 12.sp, color = iuaGreen)
            },
        )
    }
    val shape = heroShape
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, spotColor = Color(0x1410B981))
            .clip(shape)
            .background(heroGlassBg)
            .border(1.dp, Color(0x2610B981), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        val compact = maxWidth < 520.dp
        val titleBlock: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x4010B981)),
                    )
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(heroAccent)
                            .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x7310B981)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "MES · 実績分析 · 稼働率",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = iuaGreen,
                        letterSpacing = 0.8.sp,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            "検査工程 — 稼働率分析",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A),
                            lineHeight = 20.sp,
                        )
                        if (dataGaps.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .clickable { gapsDialogOpen = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Text(
                        buildString {
                            append("inspection_management · 検査員別 · 所定 ")
                            if (inspectorScheduleApplied) append("検査員マスタ優先") else append("デフォルト ${String.format("%.1f", defaultStandardHours)}h/日")
                            append(" · 会社稼働カレンダー自動反映")
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        lineHeight = 13.sp,
                    )
                }
            }
        }
        val actionsBlock: @Composable () -> Unit = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!rangeLabel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0x1A10B981),
                        border = BorderStroke(1.dp, Color(0x2610B981)),
                    ) {
                        Text(
                            rangeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                        )
                    }
                }
                IuaReportMenuButton(
                    enabled = reportEnabled && !reportBusy,
                    busy = reportBusy,
                    onReportCommand = onReportCommand,
                )
                IuaRefreshButton(loading = loading, onClick = onRefresh)
            }
        }
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                titleBlock()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { actionsBlock() }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) { titleBlock() }
                actionsBlock()
            }
        }
    }
}

@Composable
fun IuaCalendarBanner(
    calendarWorkdays: Int?,
    extraWorkdaysCount: Int,
    holidaysCount: Int,
    onOpenCalendar: () -> Unit,
    onOpenSchedule: () -> Unit,
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
            add("通常稼働日 ${calendarWorkdays ?: "—"} 日")
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
        Text(
            "所定工時管理",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2563EB),
            modifier = Modifier.clickable(onClick = onOpenSchedule),
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
    inspectorOptions: List<IuaInspectorOption>,
    includeIncomplete: Boolean,
    onDateRangeChange: (String, String) -> Unit,
    onInspectorChange: (Int?) -> Unit,
    onIncludeIncompleteChange: (Boolean) -> Unit,
) {
    val inspectorDropdownOptions = listOf("" to "（すべて）") +
        inspectorOptions.map { it.id.toString() to it.name }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, panelShape, spotColor = Color(0x120F172A))
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xEBF8FAFC))))
            .border(1.dp, Color(0xE2E8F0), panelShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IuaPillDateField(startDate = startDate, endDate = endDate, onDateRangeChange = onDateRangeChange)
            IuaPillInspectorField(
                value = filterInspectorId?.toString().orEmpty(),
                options = inspectorDropdownOptions,
                onSelect = { onInspectorChange(it.toIntOrNull()) },
            )
            IuaPillCheckField(checked = includeIncomplete, onCheckedChange = onIncludeIncompleteChange)
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
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 720.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                cards.forEach { card ->
                    IuaKpiCardItem(card, Modifier.weight(1f))
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                cards.forEach { card ->
                    IuaKpiCardItem(card, Modifier.width(148.dp))
                }
            }
        }
    }
}

/** 互換用 */
@Composable
fun IuaKpiStrip(cards: List<IuaKpiCard>) = IuaKpiGrid(cards)

@Composable
private fun IuaKpiCardItem(card: IuaKpiCard, modifier: Modifier = Modifier) {
    val style = iuaKpiToneStyle(card.tone)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(4.dp, shape, spotColor = style.shadow)
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
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(style.iconBrush),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iuaKpiIcon(card.icon), null, tint = Color.White, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(card.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = style.labelColor)
                Text(
                    card.value,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (card.tone == IuaKpiTone.Green) 22.sp else 20.sp,
                    color = style.valueColor,
                    lineHeight = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    card.hint,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = style.hintColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private data class IuaKpiToneStyle(
    val bg: Color,
    val border: Color,
    val accent: Brush,
    val iconBrush: Brush,
    val labelColor: Color,
    val valueColor: Color,
    val hintColor: Color,
    val shadow: Color,
)

private fun iuaKpiToneStyle(tone: IuaKpiTone): IuaKpiToneStyle = when (tone) {
    IuaKpiTone.Green -> IuaKpiToneStyle(
        Color(0xFFD1FAE5), Color(0x4510B981),
        Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399))),
        Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))),
        Color(0xFF059669), Color(0xFF047857), Color(0xFF34D399), Color(0x2810B981),
    )
    IuaKpiTone.Blue -> IuaKpiToneStyle(
        Color(0xFFDBEAFE), Color(0x333B82F6),
        Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))),
        Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF2563EB), Color(0xFF1D4ED8))),
        Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFF60A5FA), Color(0x1A3B82F6),
    )
    IuaKpiTone.Indigo -> IuaKpiToneStyle(
        Color(0xFFE0E7FF), Color(0x336366F1),
        Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))),
        Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1), Color(0xFF4F46E5))),
        Color(0xFF6366F1), Color(0xFF4338CA), Color(0xFF818CF8), Color(0x1A6366F1),
    )
    IuaKpiTone.Amber -> IuaKpiToneStyle(
        Color(0xFFFFEDD5), Color(0x33F97316),
        Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFFB923C))),
        Brush.linearGradient(listOf(Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFEA580C))),
        Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFB923C), Color(0x1AF97316),
    )
    IuaKpiTone.Violet -> IuaKpiToneStyle(
        Color(0xFFEDE9FE), Color(0x338B5CF6),
        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))),
        Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF8B5CF6), Color(0xFF7C3AED))),
        Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFFA78BFA), Color(0x1A8B5CF6),
    )
}

private fun iuaKpiIcon(icon: IuaKpiIcon): ImageVector = when (icon) {
    IuaKpiIcon.Utilization -> Icons.Default.Timeline
    IuaKpiIcon.Calendar -> Icons.Default.CalendarMonth
    IuaKpiIcon.NetTime -> Icons.Default.Timer
    IuaKpiIcon.Overtime -> Icons.Default.WbSunny
    IuaKpiIcon.Inspectors -> Icons.Default.Person
}

@Composable
private fun IuaPillDateField(
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
        startDate.isNotBlank() && endDate.isNotBlank() -> "${startDate.take(10)} ～ ${endDate.take(10)}"
        startDate.isNotBlank() -> startDate.take(10)
        endDate.isNotBlank() -> endDate.take(10)
        else -> "期間を選択"
    }
    IuaPillFieldShell(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IuaPillInspectorField(
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: "（すべて）"
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        IuaPillFieldShell(
            pillLabel = "検査員",
            pillIcon = Icons.Default.Person,
            pillColors = Brush.linearGradient(listOf(Color(0xFFFAF5FF), Color(0xFFF3E8FF))),
            pillTextColor = Color(0xFF6B21A8),
            minWidth = 148.dp,
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
private fun IuaPillCheckField(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    IuaPillFieldShell(
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
                colors = CheckboxDefaults.colors(checkedColor = iuaGreen),
            )
            Text("未確定を含む", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
        }
    }
}

@Composable
private fun IuaPillFieldShell(
    pillLabel: String,
    pillIcon: ImageVector?,
    pillColors: Brush,
    pillTextColor: Color,
    minWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .widthIn(min = minWidth)
            .height(36.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(pillColors)
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (pillIcon != null) {
                Icon(pillIcon, null, tint = pillTextColor, modifier = Modifier.size(12.dp))
            }
            Text(pillLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = pillTextColor)
        }
        Box(Modifier.weight(1f, fill = false)) { content() }
    }
}

@Composable
fun IuaDailyChartCard(daily: List<IuaChartDailyRow>, badge: String) {
    IuaPanel(
        title = "日別稼働率推移",
        badge = badge,
        titleIcon = Icons.Default.Timeline,
        theme = IuaPanelTheme.Chart,
    ) {
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
                IuaChartLegend("正味(H)", iuaIndigo)
            }
            IuaDailyTrendChart(daily, Modifier.fillMaxWidth().height(320.dp))
            IuaChartXAxis(daily)
        }
    }
}

@Composable
fun IuaOvertimeChartCard(daily: List<IuaChartDailyRow>, totalLabel: String) {
    IuaPanel(
        title = "日別残業推移",
        badge = "合計 $totalLabel",
        titleIcon = Icons.Default.WbSunny,
        theme = IuaPanelTheme.Overtime,
        badgeTone = IuaBadgeTone.Overtime,
    ) {
        if (daily.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("データがありません", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                IuaChartLegend("残業(H)", iuaAmber)
            }
            IuaOvertimeTrendChart(daily, Modifier.fillMaxWidth().height(320.dp))
            IuaChartXAxis(daily)
        }
    }
}

@Composable
private fun IuaChartXAxis(daily: List<IuaChartDailyRow>) {
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
private fun IuaDailyTrendChart(daily: List<IuaChartDailyRow>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFF0FDF4), Color.White))),
    ) {
        val leftPad = 36f
        val rightPad = 36f
        val topPad = 20f
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
        val hoursMax = (daily.maxOfOrNull { InspectionUtilizationLogic.netHoursFromRow(it) } ?: 1f).coerceAtLeast(0.1f) * 1.15f

        for (tick in 0..4) {
            val y = chartBottom - chartHeight * tick / 4f
            drawLine(Color(0xFFF1F5F9), Offset(chartLeft, y), Offset(chartRight, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
        }
        drawLine(Color(0xFFE2E8F0), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)

        val barWidth = (groupWidth * 0.35f).coerceIn(4f, 20f)
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val hours = InspectionUtilizationLogic.netHoursFromRow(row)
            val barH = hours / hoursMax * chartHeight
            if (barH > 0f) {
                drawRect(
                    color = iuaIndigo.copy(alpha = 0.75f),
                    topLeft = Offset(centerX - barWidth / 2f, chartBottom - barH),
                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                )
                drawChartLabel(
                    text = String.format("%.0f", hours),
                    x = centerX,
                    y = chartBottom - barH - 6f,
                    color = Color(0xFF4338CA),
                    sizeSp = 18f,
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
        pctPoints.forEachIndexed { index, (x, y) ->
            drawCircle(Color.White, 4f, Offset(x, y))
            drawCircle(iuaGreenLight, 3f, Offset(x, y))
            val pct = daily.getOrNull(index)?.utilizationPercent
            if (pct != null && pct > 0) {
                drawChartLabel(
                    text = String.format("%.0f%%", pct),
                    x = x,
                    y = y - 10f,
                    color = iuaGreen,
                    sizeSp = 18f,
                )
            }
        }
    }
}

@Composable
private fun IuaOvertimeTrendChart(daily: List<IuaChartDailyRow>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color.White)))
            .border(1.dp, Color(0xA6FDE68A), RoundedCornerShape(12.dp)),
    ) {
        val leftPad = 36f
        val rightPad = 20f
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
        val hoursMax = (daily.maxOfOrNull { InspectionUtilizationLogic.overtimeHoursFromRow(it) } ?: 1f).coerceAtLeast(0.1f) * 1.15f

        for (tick in 0..4) {
            val y = chartBottom - chartHeight * tick / 4f
            drawLine(Color(0xFFFEF3C7), Offset(chartLeft, y), Offset(chartRight, y), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
        }
        drawLine(Color(0xFFFDE68A), Offset(chartLeft, chartBottom), Offset(chartRight, chartBottom), 1f)

        val barWidth = (groupWidth * 0.4f).coerceIn(4f, 22f)
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val hours = InspectionUtilizationLogic.overtimeHoursFromRow(row)
            val barH = hours / hoursMax * chartHeight
            if (barH > 0f) {
                drawRect(
                    color = iuaAmber,
                    topLeft = Offset(centerX - barWidth / 2f, chartBottom - barH),
                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                )
                drawChartLabel(
                    text = String.format("%.1f", hours),
                    x = centerX,
                    y = chartBottom - barH - 6f,
                    color = iuaAmberDark,
                    sizeSp = 17f,
                )
            }
        }
    }
}

private fun DrawScope.drawChartLabel(text: String, x: Float, y: Float, color: Color, sizeSp: Float) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            textSize = sizeSp
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        },
    )
}

@Composable
fun IuaInspectorDailySplit(
    inspectorRows: List<InspectionUtilizationInspectorRowDto>,
    dailyRows: List<InspectionUtilizationDailyInspectorRowDto>,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 900.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IuaInspectorSummaryCard(
                    rows = inspectorRows,
                    modifier = Modifier.weight(1f),
                )
                IuaDailyDetailCard(
                    rows = dailyRows,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IuaInspectorSummaryCard(rows = inspectorRows, modifier = Modifier.fillMaxWidth())
                IuaDailyDetailCard(rows = dailyRows, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun IuaInspectorSummaryCard(
    rows: List<InspectionUtilizationInspectorRowDto>,
    modifier: Modifier = Modifier,
) {
    IuaPanel(
        title = "検査員別サマリ",
        badge = "${rows.size} 名",
        titleIcon = Icons.Default.Person,
        theme = IuaPanelTheme.Inspector,
        badgeTone = IuaBadgeTone.Soft,
        modifier = modifier,
    ) {
        IuaInspectorSummaryTable(rows)
    }
}

@Composable
fun IuaDailyDetailCard(
    rows: List<InspectionUtilizationDailyInspectorRowDto>,
    modifier: Modifier = Modifier,
) {
    IuaPanel(
        title = "検査員 × 日別明細",
        badge = "${rows.size} 行",
        titleIcon = Icons.AutoMirrored.Filled.List,
        theme = IuaPanelTheme.Daily,
        badgeTone = IuaBadgeTone.Daily,
        modifier = modifier,
    ) {
        IuaDailyDetailTable(rows)
    }
}

private data class IuaTableCol(
    val label: String,
    val weight: Float,
    val alignStart: Boolean = false,
)

private val inspectorSummaryCols = listOf(
    IuaTableCol("#", 0.45f),
    IuaTableCol("検査員", 2.1f, alignStart = true),
    IuaTableCol("出勤日", 1.1f),
    IuaTableCol("件数", 0.65f),
    IuaTableCol("正味(h)", 0.95f),
    IuaTableCol("所定内(h)", 1.05f),
    IuaTableCol("残業(h)", 0.95f),
    IuaTableCol("稼働率", 1.05f),
    IuaTableCol("ｶﾚﾝﾀﾞ率", 1.05f),
)

private val dailyDetailCols = listOf(
    IuaTableCol("生産日", 1.2f, alignStart = true),
    IuaTableCol("検査員", 1.55f, alignStart = true),
    IuaTableCol("区分", 1.05f),
    IuaTableCol("件", 0.5f),
    IuaTableCol("所定(h)", 0.9f),
    IuaTableCol("正味", 0.8f),
    IuaTableCol("所定内", 0.85f),
    IuaTableCol("残業", 0.8f),
    IuaTableCol("稼働率", 0.95f),
    IuaTableCol("負荷率", 0.95f),
)

private enum class IuaPanelTheme { Default, Chart, Overtime, Inspector, Daily }
private enum class IuaBadgeTone { Default, Soft, Overtime, Daily }

@Composable
private fun IuaPanel(
    title: String,
    badge: String,
    titleIcon: ImageVector? = null,
    theme: IuaPanelTheme = IuaPanelTheme.Default,
    badgeTone: IuaBadgeTone = IuaBadgeTone.Default,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val (bg, border) = when (theme) {
        IuaPanelTheme.Chart -> Brush.linearGradient(listOf(Color.White, Color(0xFFF0FDF4))) to Color(0x2910B981)
        IuaPanelTheme.Overtime -> Brush.linearGradient(listOf(Color.White, Color(0xFFFFFBEB))) to Color(0x29F59E0B)
        IuaPanelTheme.Inspector -> Brush.linearGradient(listOf(Color.White, Color(0xFFF5F3FF))) to Color(0x246366F1)
        IuaPanelTheme.Daily -> Brush.linearGradient(listOf(Color.White, Color(0xFFF8FAFC))) to Color(0x3394A3B8)
        IuaPanelTheme.Default -> Brush.linearGradient(listOf(Color.White, Color.White)) to Color(0xFFE2E8F0)
    }
    val (badgeBg, badgeFg, badgeBorder) = when (badgeTone) {
        IuaBadgeTone.Overtime -> Triple(Color(0x24F59E0B), iuaAmberDark, Color(0x33F59E0B))
        IuaBadgeTone.Daily -> Triple(Color(0x1A10B981), iuaGreen, Color(0x2910B981))
        IuaBadgeTone.Soft -> Triple(Color(0x1A6366F1), iuaIndigo, Color(0x296366F1))
        IuaBadgeTone.Default -> Triple(Color(0x1A10B981), iuaGreen, Color(0x2910B981))
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(bg)
            .border(1.dp, border, panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (titleIcon != null) {
                    Icon(titleIcon, null, tint = if (theme == IuaPanelTheme.Overtime) iuaAmber else iuaGreen, modifier = Modifier.size(15.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            }
            Surface(shape = RoundedCornerShape(999.dp), color = badgeBg, border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)) {
                Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = badgeFg)
            }
        }
        content()
    }
}

@Composable
private fun IuaInspectorSummaryTable(rows: List<InspectionUtilizationInspectorRowDto>) {
    Column(Modifier.fillMaxWidth()) {
        IuaTableHeaderRow(inspectorSummaryCols)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 4.dp),
            ) {
                IuaRankCell(index, inspectorSummaryCols[0].weight)
                IuaTableCell(row.inspectorName.orEmpty(), inspectorSummaryCols[1], alignStart = true)
                IuaTableCell("${row.scheduledWorkDayCount ?: 0}/${row.workDayCount ?: 0}", inspectorSummaryCols[2])
                IuaTableCell("${row.sessionCount ?: 0}", inspectorSummaryCols[3])
                IuaTableCell(InspectionUtilizationLogic.fmtHours(row.sumNetProductionSec), inspectorSummaryCols[4])
                IuaTableCell(InspectionUtilizationLogic.fmtHours(row.sumRegularSec), inspectorSummaryCols[5])
                val ot = row.sumOvertimeSec ?: 0
                IuaTableCell(
                    InspectionUtilizationLogic.fmtHours(row.sumOvertimeSec),
                    inspectorSummaryCols[6],
                    color = if (ot > 0) Color(0xFFEA580C) else ProductionPlanningColors.TextPrimary,
                    bold = ot > 0,
                )
                IuaUtilPillCell(
                    InspectionUtilizationLogic.fmtPct(row.utilizationPercent),
                    row.utilizationPercent,
                    inspectorSummaryCols[7].weight,
                )
                IuaTableCell(InspectionUtilizationLogic.fmtPct(row.calendarUtilizationPercent), inspectorSummaryCols[8])
            }
        }
        if (rows.isEmpty()) IuaEmptyTableRow()
    }
}

@Composable
private fun IuaDailyDetailTable(rows: List<InspectionUtilizationDailyInspectorRowDto>) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        IuaTableHeaderRow(dailyDetailCols)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 4.dp),
            ) {
                IuaTableCell(row.day?.take(10).orEmpty(), dailyDetailCols[0], alignStart = true, fontMono = true)
                IuaTableCell(row.inspectorName.orEmpty(), dailyDetailCols[1], alignStart = true)
                IuaCategoryCell(row, dailyDetailCols[2].weight)
                IuaTableCell("${row.sessionCount ?: 0}", dailyDetailCols[3])
                IuaTableCell(InspectionUtilizationLogic.fmtScheduledHours(row.scheduledHours), dailyDetailCols[4])
                IuaTableCell(InspectionUtilizationLogic.fmtMin(row.sumNetProductionMin), dailyDetailCols[5])
                IuaTableCell(InspectionUtilizationLogic.fmtMin(row.regularMin), dailyDetailCols[6])
                val ot = row.overtimeMin ?: 0
                IuaTableCell(
                    InspectionUtilizationLogic.fmtMin(row.overtimeMin),
                    dailyDetailCols[7],
                    color = if (ot > 0) Color(0xFFEA580C) else ProductionPlanningColors.TextPrimary,
                    bold = ot > 0,
                )
                IuaUtilPillCell(
                    InspectionUtilizationLogic.fmtPct(row.utilizationPercent),
                    row.utilizationPercent,
                    dailyDetailCols[8].weight,
                )
                IuaTableCell(InspectionUtilizationLogic.fmtPct(row.loadPercent), dailyDetailCols[9])
            }
        }
        if (rows.isEmpty()) IuaEmptyTableRow()
    }
}

@Composable
private fun IuaTableHeaderRow(cols: List<IuaTableCol>) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 6.dp),
    ) {
        cols.forEach { col ->
            Text(
                col.label,
                modifier = Modifier
                    .weight(col.weight)
                    .padding(horizontal = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                textAlign = if (col.alignStart) TextAlign.Start else TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RowScope.IuaTableCell(
    text: String,
    col: IuaTableCol,
    alignStart: Boolean = col.alignStart,
    color: Color = ProductionPlanningColors.TextPrimary,
    bold: Boolean = false,
    fontMono: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier
            .weight(col.weight)
            .padding(horizontal = 4.dp),
        fontSize = 11.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        fontFamily = if (fontMono) FontFamily.Monospace else FontFamily.Default,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = if (alignStart) TextAlign.Start else TextAlign.End,
    )
}

@Composable
private fun RowScope.IuaRankCell(index: Int, weight: Float) {
    val tone = InspectionUtilizationLogic.inspectorRankTone(index)
    val (bg, fg) = when (tone) {
        IuaRankTone.Gold -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        IuaRankTone.Silver -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        IuaRankTone.Bronze -> Color(0xFFFFEDD5) to Color(0xFF9A3412)
        IuaRankTone.None -> Color.Transparent to Color(0xFF94A3B8)
    }
    Box(Modifier.weight(weight), contentAlignment = Alignment.Center) {
        if (tone == IuaRankTone.None) {
            Text("${index + 1}", fontSize = 10.sp, color = fg, textAlign = TextAlign.Center)
        } else {
            Surface(shape = RoundedCornerShape(4.dp), color = bg) {
                Text(
                    "${index + 1}",
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = fg,
                )
            }
        }
    }
}

@Composable
private fun RowScope.IuaUtilPillCell(text: String, pct: Double?, weight: Float) {
    val tone = InspectionUtilizationLogic.utilPillTone(pct)
    val (bg, fg, border) = when (tone) {
        IuaUtilPillTone.High -> Triple(Color(0xFFECFDF5), Color(0xFF047857), Color(0x3310B981))
        IuaUtilPillTone.Mid -> Triple(Color(0xFFEFF6FF), Color(0xFF1D4ED8), Color(0x333B82F6))
        IuaUtilPillTone.Low -> Triple(Color(0xFFFFF7ED), Color(0xFFC2410C), Color(0x33F97316))
        IuaUtilPillTone.None -> Triple(Color(0xFFF8FAFC), Color(0xFF94A3B8), Color(0x33CBD5E1))
    }
    Box(
        Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Surface(shape = RoundedCornerShape(5.dp), color = bg, border = androidx.compose.foundation.BorderStroke(1.dp, border)) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
        }
    }
}

@Composable
private fun RowScope.IuaCategoryCell(row: InspectionUtilizationDailyInspectorRowDto, weight: Float) {
    val tone = InspectionUtilizationLogic.dayCategoryTone(row)
    val (bg, fg) = when (tone) {
        IuaDayCategoryTone.ExtraWorkday -> Color(0xFFFFF7ED) to Color(0xFFEA580C)
        IuaDayCategoryTone.HolidayActual -> Color(0xFFF1F5F9) to Color(0xFF64748B)
        IuaDayCategoryTone.Weekday -> Color(0xFFECFDF5) to Color(0xFF059669)
    }
    Box(
        Modifier
            .weight(weight)
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
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
private fun IuaEmptyTableRow() {
    Text(
        "データがありません",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        fontSize = 11.sp,
        color = ProductionPlanningColors.TextSecondary,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IuaRefreshButton(loading: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(if (!loading) heroAccent else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
            .then(if (!loading) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Default.Refresh, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Text(
            "更新",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (!loading) Color.White else Color(0xFF94A3B8),
        )
    }
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
fun IuaEmptyState(errorMessage: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(panelShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), panelShape),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Icon(Icons.Default.Speed, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                if (errorMessage.isNullOrBlank()) {
                    "分析データがありません。期間を確認して「更新」を押してください"
                } else {
                    errorMessage
                },
                fontSize = 11.sp,
                color = if (errorMessage.isNullOrBlank()) Color(0xFF94A3B8) else Color(0xFFDC2626),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun IuaReportMenuButton(
    enabled: Boolean,
    busy: Boolean,
    onReportCommand: (InspectionUtilizationReportCommand) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                .then(if (enabled && !busy) Modifier.clickable { expanded = true } else Modifier)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = iuaGreen, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Description, null, tint = if (enabled) Color(0xFF475569) else Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            }
            Text(
                if (busy) "出力中" else "レポート",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color(0xFF475569) else Color(0xFF94A3B8),
            )
            Icon(Icons.Default.KeyboardArrowDown, null, tint = if (enabled) Color(0xFF94A3B8) else Color(0xFFCBD5E1), modifier = Modifier.size(14.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .widthIn(min = 280.dp)
                .border(1.dp, Color(0x3310B981), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            containerColor = Color(0xFAFFFFFF),
            tonalElevation = 4.dp,
            shadowElevation = 12.dp,
        ) {
            InspectionUtilizationLogic.reportMenuItems().forEach { item ->
                if (item.divided) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        thickness = 1.dp,
                        color = Color(0xE6E2E8F0),
                    )
                }
                DropdownMenuItem(
                    text = { IuaReportMenuItemContent(item) },
                    onClick = {
                        expanded = false
                        onReportCommand(item.command)
                    },
                    contentPadding = PaddingValues(0.dp),
                )
            }
        }
    }
}

@Composable
private fun IuaReportMenuItemContent(item: IuaReportMenuItem) {
    val icon = iuaReportMenuIcon(item.command)
    val toneStyle = iuaReportMenuToneStyle(item.tone)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(toneStyle.background)
                .padding(6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = toneStyle.foreground, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                lineHeight = 16.sp,
            )
            Text(
                item.hint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                lineHeight = 13.sp,
            )
        }
    }
}

private data class IuaReportMenuToneStyle(
    val background: Brush,
    val foreground: Color,
)

private fun iuaReportMenuToneStyle(tone: IuaReportMenuTone): IuaReportMenuToneStyle = when (tone) {
    IuaReportMenuTone.GREEN -> IuaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))),
        foreground = Color(0xFF047857),
    )
    IuaReportMenuTone.SKY -> IuaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
        foreground = Color(0xFF0369A1),
    )
    IuaReportMenuTone.TEAL -> IuaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4))),
        foreground = Color(0xFF0F766E),
    )
}

private fun iuaReportMenuIcon(command: InspectionUtilizationReportCommand): ImageVector = when (command) {
    InspectionUtilizationReportCommand.PRINT_FULL -> Icons.Default.Description
    InspectionUtilizationReportCommand.PRINT_DAILY -> Icons.AutoMirrored.Filled.ShowChart
    InspectionUtilizationReportCommand.PRINT_DAILY_BATCH -> Icons.Default.Analytics
}
