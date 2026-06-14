package com.example.smart_emap.ui.mes.productivity

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
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

private val ipaAmber = Color(0xFFF59E0B)
private val ipaAmberDark = Color(0xFFB45309)
private val ipaAmberDeep = Color(0xFF92400E)
private val ipaAmberHeader = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
private val ipaAmberHeaderAlt = Brush.verticalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)))
private val ipaEmerald = Color(0xFF059669)
private val ipaEmeraldLight = Color(0xFF10B981)
private val ipaIndigo = Color(0xFF6366F1)
private val ipaSky = Color(0xFF38BDF8)
private val heroGlassBg = Brush.linearGradient(listOf(Color(0xEBFFFFFF), Color(0xB8FFFFFF)))
private val heroIconGradient = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857)))
private val reportBtnGradient = Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF6366F1), Color(0xFF4F46E5)))
private val refreshBtnGradient = Brush.linearGradient(listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF14B8A6)))
private val panelShape = RoundedCornerShape(14.dp)
private val fieldShape = RoundedCornerShape(10.dp)
private val heroShape = RoundedCornerShape(16.dp)

private enum class IpaPanelVariant { Default, Chart, Inspector, Product }

@Composable
fun IpaPageBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(280.dp)
                .offset((-80).dp, (-80).dp)
                .clip(CircleShape)
                .background(Color(0x596366F1)),
        )
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(40.dp, 80.dp)
                .clip(CircleShape)
                .background(Color(0x4710B981)),
        )
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.BottomCenter)
                .offset((-60).dp, (-120).dp)
                .clip(CircleShape)
                .background(Color(0x380EA5E9)),
        )
        content()
    }
}

@Composable
fun IpaHeroBar(
    rangeLabel: String?,
    loading: Boolean,
    reportBusy: Boolean,
    reportEnabled: Boolean,
    onReportCommand: (InspectionProductivityReportCommand) -> Unit,
    onRefresh: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, heroShape, spotColor = Color(0x140F172A))
            .clip(heroShape)
            .background(heroGlassBg)
            .border(1.dp, Color(0xD9FFFFFF), heroShape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        val compact = maxWidth < 520.dp
        val titleBlock: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0x4010B981)),
                    )
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(heroIconGradient)
                            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x7310B981)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.AssignmentTurnedIn, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "MES · 実績分析",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ipaIndigo,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        "検査工程 — 生産性分析",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A),
                        lineHeight = 22.sp,
                    )
                    Text(
                        "実績 · 能率 · 不良率 · 稼働",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        lineHeight = 14.sp,
                    )
                }
            }
        }
        val actionsBlock: @Composable () -> Unit = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!rangeLabel.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0x146366F1),
                        border = BorderStroke(1.dp, Color(0x266366F1)),
                    ) {
                        Text(
                            rangeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                        )
                    }
                }
                IpaReportMenuButton(
                    enabled = reportEnabled && !reportBusy,
                    busy = reportBusy,
                    onReportCommand = onReportCommand,
                )
                IpaGradientButton(
                    label = "更新",
                    icon = Icons.Default.Refresh,
                    gradient = refreshBtnGradient,
                    enabled = !loading,
                    onClick = onRefresh,
                )
            }
        }
        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth()) { titleBlock() }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { actionsBlock() }
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0x4010B981)),
                        )
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(heroIconGradient)
                                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x7310B981)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    Column {
                        Text("MES · 実績分析", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ipaIndigo, letterSpacing = 1.2.sp)
                        Text("検査工程 — 生産性分析", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0F172A), lineHeight = 22.sp)
                        Text("実績 · 能率 · 不良率 · 稼働", fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2, lineHeight = 14.sp)
                    }
                }
                actionsBlock()
            }
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
    onDateRangeChange: (String, String) -> Unit,
    onInspectorChange: (Int?) -> Unit,
    onProductChange: (String) -> Unit,
    onIncludeIncompleteChange: (Boolean) -> Unit,
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
            .shadow(6.dp, panelShape, spotColor = Color(0x120F172A))
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xEBF1F5F9))))
            .border(1.dp, Color(0xF2FFFFFF), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
            fields()
        } else {
            fields()
        }
    }
}

@Composable
fun IpaKpiGrid(cards: List<IpaKpiCard>) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 720.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                cards.forEach { card ->
                    IpaKpiCardItem(card, Modifier.weight(1f))
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
                    IpaKpiCardItem(card, Modifier.width(148.dp))
                }
            }
        }
    }
}

@Composable
private fun IpaKpiCardItem(card: IpaKpiCard, modifier: Modifier = Modifier) {
    val style = kpiToneStyle(card.tone)
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
    IpaThemedPanel(
        title = "日別推移",
        variant = IpaPanelVariant.Chart,
        titleIcon = Icons.AutoMirrored.Filled.ShowChart,
        badges = {
            IpaSoftBadge("生産数 · 能率", Color(0xFF6366F1), Color(0x1A6366F1))
        },
    ) {
        if (daily.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Text("データがありません", fontSize = 11.sp, color = ProductionPlanningColors.TextSecondary)
            }
        } else {
            val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1)
            val effMax = (daily.maxOfOrNull { (it.efficiencyPerHour ?: 0.0).toInt() } ?: 1).coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IpaChartLegend("生産数", Color(0xFF7DD3FC))
                Spacer(Modifier.width(16.dp))
                IpaChartLegend("能率", ipaEmeraldLight)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xCCF8FAFC), Color(0x66FFFFFF))))
                    .border(1.dp, Color(0xD9E2E8F0), RoundedCornerShape(12.dp)),
            ) {
                Row(Modifier.fillMaxSize()) {
                    Column(
                        Modifier
                            .width(34.dp)
                            .fillMaxHeight()
                            .padding(top = 36.dp, bottom = 24.dp),
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
                            .padding(top = 36.dp, bottom = 24.dp),
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
    inspectorCount: Int,
    inspectorSectionAvgEfficiency: Double?,
    productSectionTotalQty: Int,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {
                    IpaInspectorSection(inspectorRows, inspectorCount, inspectorSectionAvgEfficiency)
                }
                Box(Modifier.weight(1f)) {
                    IpaProductSection(productRows, productSectionTotalQty)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IpaInspectorSection(inspectorRows, inspectorCount, inspectorSectionAvgEfficiency)
                IpaProductSection(productRows, productSectionTotalQty)
            }
        }
    }
}

@Composable
fun IpaInspectorSection(
    rows: List<InspectionProductivityInspectorRowDto>,
    inspectorCount: Int,
    inspectorSectionAvgEfficiency: Double?,
) {
    val chartRows = rows
        .sortedByDescending {
            InspectionProductivityLogic.periodAvgEfficiencyFromBucket(it.sumActualQty, it.sumNetProductionSec)
                ?: it.efficiencyPerHour
                ?: -1.0
        }
    IpaThemedPanel(
        title = "検査員別",
        variant = IpaPanelVariant.Inspector,
        titleIcon = Icons.Default.Person,
        badges = {
            IpaSoftBadge("$inspectorCount 名", Color(0xFF64748B), Color(0x2494A3B8))
            inspectorSectionAvgEfficiency?.let {
                IpaSoftBadge(
                    "平均能率 ${InspectionProductivityLogic.fmtEfficiency(it)} 本/時",
                    Color(0xFF4338CA),
                    Color(0x1F6366F1),
                    border = Color(0x2E6366F1),
                )
            }
        },
    ) {
        if (chartRows.isNotEmpty()) {
            IpaInspectorBarChart(chartRows, scrollable = true)
            Spacer(Modifier.height(10.dp))
        }
        IpaInspectorTable(rows)
    }
}

@Composable
fun IpaProductSection(rows: List<InspectionProductivityProductRowDto>, productSectionTotalQty: Int) {
    val chartRows = rows.sortedByDescending { it.sumActualQty ?: 0 }
    IpaThemedPanel(
        title = "製品別",
        variant = IpaPanelVariant.Product,
        titleIcon = Icons.Default.Inventory2,
        badges = {
            IpaSoftBadge("${rows.size} 品目", Color(0xFF64748B), Color(0x2494A3B8))
            if (productSectionTotalQty > 0) {
                IpaSoftBadge(
                    "生産 ${InspectionProductivityLogic.fmtInt(productSectionTotalQty)}",
                    Color(0xFF0369A1),
                    Color(0x1F0EA5E9),
                    border = Color(0x330EA5E9),
                )
            }
        },
    ) {
        if (chartRows.isNotEmpty()) {
            IpaProductBarChart(chartRows, scrollable = true)
            Spacer(Modifier.height(10.dp))
        }
        IpaProductTable(rows)
    }
}

private fun inspectorEfficiency(row: InspectionProductivityInspectorRowDto): Double? =
    InspectionProductivityLogic.periodAvgEfficiencyFromBucket(row.sumActualQty, row.sumNetProductionSec)
        ?: row.efficiencyPerHour

@Composable
private fun IpaInspectorBarChart(
    rows: List<InspectionProductivityInspectorRowDto>,
    scrollable: Boolean = false,
    maxScrollHeight: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val colors = listOf(
        Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF0EA5E9), Color(0xFF10B981),
        Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFF97316), Color(0xFFEC4899),
    )
    val maxVal = rows.maxOfOrNull { inspectorEfficiency(it) ?: 0.0 }?.coerceAtLeast(1.0) ?: 1.0
    val chartScroll = rememberScrollState()
    val shellModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.verticalGradient(listOf(Color(0xBFF8FAFC), Color(0x80FFFFFF))))
        .border(1.dp, Color(0xCCE2E8F0), RoundedCornerShape(12.dp))

    val barRows: @Composable () -> Unit = {
        rows.forEachIndexed { index, row ->
            val value = inspectorEfficiency(row) ?: 0.0
            val color = colors[index % colors.size]
            Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.inspectorName.orEmpty(),
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
                    InspectionProductivityLogic.fmtEfficiency(value),
                    modifier = Modifier.width(48.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.End,
                )
            }
        }
    }

    if (scrollable) {
        Box(
            modifier = shellModifier.height(maxScrollHeight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(chartScroll)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                barRows()
            }
        }
    } else {
        Column(
            modifier = shellModifier
                .heightIn(min = (rows.size * 30).dp, max = maxScrollHeight)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            barRows()
        }
    }
}

@Composable
private fun IpaProductBarChart(
    rows: List<InspectionProductivityProductRowDto>,
    scrollable: Boolean = false,
    maxScrollHeight: androidx.compose.ui.unit.Dp = 220.dp,
) {
    val colors = listOf(
        Color(0xFF38BDF8), Color(0xFF0EA5E9), Color(0xFF6366F1), Color(0xFF8B5CF6),
        Color(0xFF10B981), Color(0xFF14B8A6), Color(0xFFF59E0B), Color(0xFFEC4899),
    )
    val maxVal = rows.maxOfOrNull { it.sumActualQty ?: 0 }?.coerceAtLeast(1)?.toDouble() ?: 1.0
    val chartScroll = rememberScrollState()
    val shellModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Brush.verticalGradient(listOf(Color(0xBFF8FAFC), Color(0x80FFFFFF))))
        .border(1.dp, Color(0xCCE2E8F0), RoundedCornerShape(12.dp))

    val barRows: @Composable () -> Unit = {
        rows.forEachIndexed { index, row ->
            val value = (row.sumActualQty ?: 0).toDouble()
            val label = row.productName?.trim().orEmpty().ifBlank { row.productCd.orEmpty() }
            val color = colors[index % colors.size]
            Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
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
                    InspectionProductivityLogic.fmtInt(row.sumActualQty),
                    modifier = Modifier.width(56.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.End,
                )
            }
        }
    }

    if (scrollable) {
        Box(modifier = shellModifier.height(maxScrollHeight)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(chartScroll)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                barRows()
            }
        }
    } else {
        Column(
            modifier = shellModifier
                .heightIn(min = (rows.size * 30).dp, max = maxScrollHeight)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            barRows()
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
        IpaRankOverviewBlock(
            rows = topOverview,
            activeProductCd = rankViewProductCd,
            onDetailClick = onDetailClick,
        )
    }
}

@Composable
fun IpaWeldRankSplit(
    rankOff: List<InspectorAvgRankRow>,
    rankOn: List<InspectorAvgRankRow>,
    weldingProductBomCount: Int,
    onInspectorClick: (InspectorAvgRankRow, InspectorProductScope) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 560.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    IpaWeldRankPanel(
                        title = "検査員平均能率ランキング",
                        subtitle = "溶接工程なし製品",
                        badge = "${rankOff.size} 名",
                        rows = rankOff,
                        weldOn = false,
                        emptyText = "対象データがありません",
                        onInspectorClick = onInspectorClick,
                    )
                }
                Box(Modifier.weight(1f)) {
                    IpaWeldRankPanel(
                        title = "検査員平均能率ランキング",
                        subtitle = "溶接工程あり製品",
                        badge = "${rankOn.size} 名 · BOM $weldingProductBomCount 品目",
                        rows = rankOn,
                        weldOn = true,
                        bomWarning = weldingProductBomCount == 0,
                        emptyText = "溶接工程あり製品の検査実績がありません",
                        onInspectorClick = onInspectorClick,
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                IpaWeldRankPanel(
                    title = "検査員平均能率ランキング",
                    subtitle = "溶接工程なし製品",
                    badge = "${rankOff.size} 名",
                    rows = rankOff,
                    weldOn = false,
                    emptyText = "対象データがありません",
                    onInspectorClick = onInspectorClick,
                )
                IpaWeldRankPanel(
                    title = "検査員平均能率ランキング",
                    subtitle = "溶接工程あり製品",
                    badge = "${rankOn.size} 名 · BOM $weldingProductBomCount 品目",
                    rows = rankOn,
                    weldOn = true,
                    bomWarning = weldingProductBomCount == 0,
                    emptyText = "溶接工程あり製品の検査実績がありません",
                    onInspectorClick = onInspectorClick,
                )
            }
        }
    }
}

@Composable
private fun IpaWeldRankPanel(
    title: String,
    subtitle: String,
    badge: String,
    rows: List<InspectorAvgRankRow>,
    weldOn: Boolean,
    emptyText: String,
    bomWarning: Boolean = false,
    onInspectorClick: (InspectorAvgRankRow, InspectorProductScope) -> Unit,
) {
    val accent = Color(0xFFD97706)
    val linkColor = Color(0xFFC2410C)
    val badgeFg = Color(0xFFC2410C)
    val badgeBg = Color(0x1FF97316)
    val badgeBorder = Color(0x38FB923C)
    val bg = Brush.linearGradient(listOf(Color.White, Color(0xFFFFF7ED)))
    val borderColor = Color(0x33F97316)
    val shadowColor = Color(0x12F97316)
    val shape = panelShape

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = shadowColor)
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Person, null, tint = accent, modifier = Modifier.size(15.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                    Text(
                        subtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = badgeBg,
                border = BorderStroke(1.dp, badgeBorder),
            ) {
                Text(
                    badge,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = badgeFg,
                )
            }
        }
        if (bomWarning) {
            Spacer(Modifier.height(8.dp))
            Text(
                "製品工程BOMに溶接工程の登録がありません。マスタで溶接フラグを確認してください。",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xA6FEF3C7))
                    .border(1.dp, Color(0x59FB923C), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = Color(0xFFC2410C),
            )
        }
        Spacer(Modifier.height(8.dp))
        IpaWeldRankTable(rows, weldOn, linkColor, emptyText, onInspectorClick)
    }
}

@Composable
private fun IpaWeldRankTable(
    rows: List<InspectorAvgRankRow>,
    weldOn: Boolean,
    linkColor: Color,
    emptyText: String,
    onInspectorClick: (InspectorAvgRankRow, InspectorProductScope) -> Unit,
) {
    val scope = if (weldOn) InspectorProductScope.WithWelding else InspectorProductScope.NoWelding
    val headers = listOf("順位", "検査員", "件", "生産", "不良率", "平均能率")
    val columnWeights = listOf(0.85f, 2.4f, 0.65f, 1.15f, 1f, 1.15f)
    val columnAligns = listOf(
        TextAlign.Center,
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.End,
        TextAlign.Center,
    )
    val headerBg = ipaAmberHeaderAlt
    val headerFg = Color(0xFFC2410C)
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x80FDE68A), RoundedCornerShape(10.dp))
            .heightIn(max = 300.dp)
            .verticalScroll(scroll),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(vertical = 6.dp),
        ) {
            headers.forEachIndexed { i, h ->
                Text(
                    h,
                    modifier = Modifier.weight(columnWeights[i]).padding(horizontal = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerFg,
                    textAlign = columnAligns[i],
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (rows.isEmpty()) {
            Text(
                emptyText,
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 8.dp),
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
            )
        } else {
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) Color.White else Color(0xFFFFFBEB)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.weight(columnWeights[0]),
                        contentAlignment = Alignment.Center,
                    ) {
                        IpaWeldRankBadge(row.rank)
                    }
                    Text(
                        row.inspectorName.orEmpty(),
                        modifier = Modifier
                            .weight(columnWeights[1])
                            .padding(horizontal = 4.dp)
                            .clickable { onInspectorClick(row, scope) },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = linkColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IpaFlexibleCell("${row.sessionCount}", columnWeights[2], columnAligns[2])
                    IpaFlexibleCell(
                        InspectionProductivityLogic.fmtInt(row.sumActualQty),
                        columnWeights[3],
                        columnAligns[3],
                    )
                    Box(
                        Modifier.weight(columnWeights[4]),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        IpaAmberMetricPill(InspectionProductivityLogic.fmtPct(row.defectRatePercent))
                    }
                    Box(
                        Modifier.weight(columnWeights[5]),
                        contentAlignment = Alignment.Center,
                    ) {
                        IpaAmberMetricPill(
                            InspectionProductivityLogic.fmtEfficiency(row.avgEfficiencyPerHour),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IpaAmberMetricPill(value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0x26FBBF24),
        border = BorderStroke(1.dp, Color(0x47F59E0B)),
    ) {
        Text(
            value,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ipaAmberDark,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IpaRankDetailButton(active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(999.dp)
    if (active) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("詳細", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    } else {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(Color(0xE6FFFFFF))
                .border(1.dp, Color(0x59F59E0B), shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("詳細", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ipaAmberDark)
        }
    }
}

@Composable
private fun IpaRankOverviewBlock(
    rows: List<InspectionProductivityProductRankingDto>,
    activeProductCd: String,
    onDetailClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xA6FFFFFF))
            .border(1.dp, Color(0x73FDE68A), RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.List, null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
            Text(
                "全製品 · 能率 TOP1 一覧",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78350F),
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0x33FBBF24),
            ) {
                Text(
                    "${rows.size}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ipaAmberDark,
                )
            }
        }
        IpaRankOverviewTable(rows, activeProductCd, onDetailClick)
    }
}

@Composable
private fun IpaWeldRankBadge(rank: Int?) {
    val (bg, fg, shadow) = when (rank) {
        1 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
            Color(0xFF92400E),
            4.dp,
        )
        2 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))),
            Color(0xFF475569),
            0.dp,
        )
        3 -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA))),
            Color(0xFF9A3412),
            0.dp,
        )
        else -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9))),
            Color(0xFF475569),
            0.dp,
        )
    }
    Box(
        modifier = Modifier
            .then(if (shadow > 0.dp) Modifier.shadow(shadow, CircleShape, spotColor = Color(0x4DF59E0B)) else Modifier)
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .heightIn(min = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            rank?.toString() ?: "—",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = fg,
        )
    }
}


@Composable
fun IpaInspectorProductDialog(
    state: InspectorProductDialogState,
    rangeLabel: String?,
    onDismiss: () -> Unit,
) {
    val weldOn = state.scope == InspectorProductScope.WithWelding
    val accent = if (weldOn) Color(0xFFEA580C) else Color(0xFF059669)
    val scopeLabel = if (weldOn) "溶接工程あり製品" else "溶接工程なし製品"
    val sessionCount = state.rows.sumOf { it.sessionCount }
    val totalQty = state.rows.sumOf { it.sumActualQty }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(state.target.inspectorName.orEmpty(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("生産製品一覧 · $scopeLabel", fontSize = 11.sp, color = Color(0xFF64748B))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IpaDialogStat("品目数", "${state.rows.size}")
                    IpaDialogStat("セッション", InspectionProductivityLogic.fmtInt(sessionCount))
                    IpaDialogStat("生産合計", InspectionProductivityLogic.fmtInt(totalQty))
                    IpaDialogStat(
                        "平均能率",
                        "${InspectionProductivityLogic.fmtEfficiency(state.target.avgEfficiencyPerHour)} 本/時",
                        accent,
                    )
                }
                if (!rangeLabel.isNullOrBlank()) {
                    Text("集計期間 $rangeLabel", fontSize = 10.sp, color = Color(0xFF64748B))
                }
                IpaInspectorProductTable(state.rows, accent)
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("閉じる") }
        },
    )
}

@Composable
private fun IpaDialogStat(label: String, value: String, valueColor: Color = Color(0xFF0F172A)) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun IpaInspectorProductTable(rows: List<InspectorProductDisplayRow>, accent: Color) {
    val headers = listOf("CD", "製品名", "件", "生産", "不良率", "能率")
    val widths = listOf(72, 100, 36, 56, 52, 48)
    Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
        IpaTableHeader(headers, widths)
        if (rows.isEmpty()) {
            IpaEmptyRow(widths.sum())
        } else {
            rows.forEachIndexed { index, row ->
                val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                Row(Modifier.background(bg).padding(vertical = 3.dp)) {
                    IpaCell(row.productCd, widths[0], fontMono = true, align = TextAlign.Start)
                    IpaCell(row.productName, widths[1], align = TextAlign.Start)
                    IpaCell("${row.sessionCount}", widths[2])
                    IpaCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), widths[3])
                    IpaCell(InspectionProductivityLogic.fmtPct(row.defectRatePercent), widths[4], color = Color(0xFFEA580C), bold = true)
                    IpaCell(InspectionProductivityLogic.fmtEfficiency(row.avgEfficiencyPerHour), widths[5], color = accent, bold = true)
                }
            }
        }
    }
}

@Composable
private fun IpaReportMenuButton(
    enabled: Boolean,
    busy: Boolean,
    onReportCommand: (InspectionProductivityReportCommand) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IpaGradientButton(
            label = if (busy) "出力中" else "レポート",
            icon = Icons.Default.Description,
            gradient = reportBtnGradient,
            enabled = enabled && !busy,
            showCaret = true,
            onClick = { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 300.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = Color(0xFAFFFFFF),
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
        ) {
            InspectionProductivityReportLogic.reportMenuItems().forEach { item ->
                if (item.divided) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        thickness = 1.dp,
                        color = Color(0xD9E2E8F0),
                    )
                }
                DropdownMenuItem(
                    text = { IpaReportMenuItemContent(item) },
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
private fun IpaReportMenuItemContent(item: IpaReportMenuItem) {
    val icon = ipaReportMenuIcon(item.command)
    val toneStyle = ipaReportMenuToneStyle(item.tone)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .shadow(2.dp, RoundedCornerShape(9.dp), spotColor = toneStyle.shadow)
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

private data class IpaReportMenuToneStyle(
    val background: Brush,
    val foreground: Color,
    val shadow: Color,
)

private fun ipaReportMenuToneStyle(tone: IpaReportMenuTone): IpaReportMenuToneStyle = when (tone) {
    IpaReportMenuTone.INDIGO -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFEEF2FF), Color(0xFFE0E7FF))),
        foreground = Color(0xFF4F46E5),
        shadow = Color(0x2E6366F1),
    )
    IpaReportMenuTone.SKY -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))),
        foreground = Color(0xFF0284C7),
        shadow = Color(0x2E0EA5E9),
    )
    IpaReportMenuTone.TEAL -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFCCFBF1), Color(0xFF99F6E4))),
        foreground = Color(0xFF0D9488),
        shadow = Color(0x2E14B8A6),
    )
    IpaReportMenuTone.VIOLET -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF))),
        foreground = Color(0xFF7C3AED),
        shadow = Color(0x2E8B5CF6),
    )
    IpaReportMenuTone.EMERALD -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0))),
        foreground = Color(0xFF059669),
        shadow = Color(0x2E10B981),
    )
    IpaReportMenuTone.AMBER -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))),
        foreground = Color(0xFFD97706),
        shadow = Color(0x2EF59E0B),
    )
    IpaReportMenuTone.ROSE -> IpaReportMenuToneStyle(
        background = Brush.linearGradient(listOf(Color(0xFFFFE4E6), Color(0xFFFECDD3))),
        foreground = Color(0xFFE11D48),
        shadow = Color(0x2EF43F5E),
    )
}

private fun ipaReportMenuIcon(command: InspectionProductivityReportCommand): ImageVector = when (command) {
    InspectionProductivityReportCommand.PRINT_FULL -> Icons.Default.Description
    InspectionProductivityReportCommand.PRINT_DAILY -> Icons.AutoMirrored.Filled.ShowChart
    InspectionProductivityReportCommand.PRINT_DAILY_BATCH -> Icons.Default.Analytics
    InspectionProductivityReportCommand.PRINT_INSPECTOR -> Icons.Default.Person
    InspectionProductivityReportCommand.PRINT_INSPECTOR_PRODUCT_BATCH -> Icons.Default.Inventory2
    InspectionProductivityReportCommand.PRINT_PRODUCT -> Icons.Default.Inventory2
    InspectionProductivityReportCommand.PRINT_WELD_RANK -> Icons.Default.Star
    InspectionProductivityReportCommand.PRINT_PRODUCT_RANK -> Icons.AutoMirrored.Filled.List
}

@Composable
fun IpaDefectSection(
    rows: List<InspectionProductivityDefectRowDto>,
    defectLabel: (String) -> String,
) {
    if (rows.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, panelShape)
            .clip(panelShape)
            .background(Brush.linearGradient(listOf(Color(0xF2FFFFFF), Color(0xE6F8FAFC))))
            .border(1.dp, Color(0xE6FFFFFF), panelShape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Icons.Default.Warning, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(15.dp))
            Text("不良内訳（KT09）", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rows.take(8).forEach { row ->
                Row(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Color(0x1AF97316))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5))))
                        .border(1.dp, Color(0x40FB923C), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        modifier = Modifier.widthIn(max = 140.dp),
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
fun IpaSessionDetailSection(
    rows: List<InspectionProductivitySessionRowDto>,
    exportBusy: Boolean,
    onExportCsv: () -> Unit,
) {
    IpaPanel(
        title = "セッション明細",
        badge = "${rows.size} 件",
        titleIcon = Icons.AutoMirrored.Filled.ShowChart,
        trailing = {
            IpaCsvButton(enabled = rows.isNotEmpty() && !exportBusy, onClick = onExportCsv)
        },
    ) {
        IpaSessionTable(rows)
    }
}

@Composable
fun IpaEmptyState(errorMessage: String? = null) {
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
            Icon(Icons.Default.AssignmentTurnedIn, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
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
private fun IpaThemedPanel(
    title: String,
    variant: IpaPanelVariant = IpaPanelVariant.Default,
    titleIcon: ImageVector? = null,
    badges: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val (bg, border, iconTint) = when (variant) {
        IpaPanelVariant.Inspector -> Triple(
            Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xFFF5F3FF))),
            Color(0x296366F1),
            Color(0xFF6366F1),
        )
        IpaPanelVariant.Product -> Triple(
            Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xFFF0F9FF))),
            Color(0x290EA5E9),
            Color(0xFF0EA5E9),
        )
        IpaPanelVariant.Chart -> Triple(
            Brush.linearGradient(listOf(Color(0xFAFFFFFF), Color(0xFFF8FAFC))),
            Color(0x1FE2E8F0),
            ipaIndigo,
        )
        IpaPanelVariant.Default -> Triple(
            Brush.linearGradient(listOf(Color(0xF2FFFFFF), Color(0xE6F8FAFC))),
            Color(0xE6FFFFFF),
            ipaIndigo,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, panelShape, spotColor = Color(0x0F0F172A))
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
                    Icon(titleIcon, null, tint = iconTint, modifier = Modifier.size(15.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                badges?.invoke()
                trailing?.invoke()
            }
        }
        content()
    }
}

@Composable
private fun IpaSoftBadge(
    text: String,
    textColor: Color,
    bgColor: Color,
    border: Color = Color.Transparent,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bgColor,
        border = if (border == Color.Transparent) null else BorderStroke(1.dp, border),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

@Composable
private fun IpaGradientButton(
    label: String,
    icon: ImageVector,
    gradient: Brush,
    enabled: Boolean,
    showCaret: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .height(34.dp)
            .shadow(if (enabled) 4.dp else 0.dp, shape, spotColor = Color(0x40000000))
            .clip(shape)
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = if (enabled) 1f else 0.6f), modifier = Modifier.size(14.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = if (enabled) 1f else 0.6f))
        if (showCaret) {
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White.copy(alpha = 0.88f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun IpaPanel(
    title: String,
    badge: String?,
    titleIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                trailing?.invoke()
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
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val leftPad = 4f
        val rightPad = 4f
        val topPad = 22f
        val bottomPad = 28f
        val chartLeft = leftPad
        val chartRight = size.width - rightPad
        val chartTop = topPad
        val chartBottom = size.height - bottomPad
        val chartWidth = (chartRight - chartLeft).coerceAtLeast(1f)
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val count = daily.size.coerceAtLeast(1)
        val groupWidth = chartWidth / count

        val qtyMax = (daily.maxOfOrNull { it.sumActualQty ?: 0 } ?: 1).coerceAtLeast(1).toFloat() * 1.18f
        val effMax = (daily.maxOfOrNull { it.efficiencyPerHour?.toFloat() ?: 0f } ?: 1f).coerceAtLeast(1f) * 1.22f

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

        val barWidth = (groupWidth * 0.42f).coerceIn(6f, 22f)
        val barTopRadius = 5f
        daily.forEachIndexed { index, row ->
            val centerX = chartLeft + groupWidth * index + groupWidth / 2f
            val qty = (row.sumActualQty ?: 0).toFloat()
            val barH = qty / qtyMax * chartHeight
            if (barH > 0f) {
                val barTop = chartBottom - barH
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF7DD3FC), Color(0xFF6366F1), Color(0xFF4338CA)),
                    ),
                    topLeft = Offset(centerX - barWidth / 2f, barTop),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barTopRadius, barTopRadius),
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
            drawPath(path, ipaEmeraldLight, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            val areaPath = Path().apply {
                moveTo(effPoints.first().first, chartBottom)
                effPoints.forEach { (x, y) -> lineTo(x, y) }
                lineTo(effPoints.last().first, chartBottom)
                close()
            }
            drawPath(areaPath, ipaEmeraldLight.copy(alpha = 0.12f))
        }
        effPoints.forEach { (x, y) ->
            drawCircle(Color.White, 5f, Offset(x, y))
            drawCircle(ipaEmeraldLight, 3.5f, Offset(x, y))
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val qtyTextSize = with(density) { 8.sp.toPx() }
        val effTextSize = with(density) { 7.5.sp.toPx() }
        val pillPadH = with(density) { 5.dp.toPx() }
        val pillPadV = with(density) { 2.5.dp.toPx() }
        val pillRadius = with(density) { 4.dp.toPx() }

        drawContext.canvas.nativeCanvas.apply {
            daily.forEachIndexed { index, row ->
                val centerX = chartLeft + groupWidth * index + groupWidth / 2f
                val qty = row.sumActualQty ?: 0
                if (qty > 0) {
                    val barH = qty / qtyMax * chartHeight
                    val barTop = chartBottom - barH
                    val qtyLabel = InspectionProductivityLogic.fmtInt(qty)
                    labelPaint.textSize = qtyTextSize
                    labelPaint.color = Color(0xFFEF4444).toArgb()
                    labelPaint.setShadowLayer(3f, 0f, 1f, Color(0xCCFFFFFF).toArgb())
                    val textH = labelPaint.fontMetrics.let { it.descent - it.ascent }
                    if (barH >= textH + 6f) {
                        val centerY = barTop + barH / 2f
                        val textY = centerY + (labelPaint.fontMetrics.descent - labelPaint.fontMetrics.ascent) / 2f
                        drawText(qtyLabel, centerX, textY, labelPaint)
                    }
                    labelPaint.clearShadowLayer()
                }

                val eff = row.efficiencyPerHour
                if (eff != null && eff > 0) {
                    val effY = chartTop + chartHeight * (1f - eff.toFloat() / effMax)
                    val effLabel = InspectionProductivityLogic.fmtEfficiency(eff)
                    labelPaint.textSize = effTextSize
                    labelPaint.color = Color(0xFF047857).toArgb()
                    val textW = labelPaint.measureText(effLabel)
                    val textH = labelPaint.fontMetrics.let { it.descent - it.ascent }
                    val pillW = textW + pillPadH * 2
                    val pillH = textH + pillPadV * 2
                    val pillTop = (effY - pillH - 8f).coerceAtLeast(chartTop)
                    val pillLeft = centerX - pillW / 2f
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color(0xF2FFFFFF).toArgb()
                        style = Paint.Style.FILL
                    }
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color(0x4710B981).toArgb()
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                    }
                    drawRoundRect(RectF(pillLeft, pillTop, pillLeft + pillW, pillTop + pillH), pillRadius, pillRadius, bgPaint)
                    drawRoundRect(RectF(pillLeft, pillTop, pillLeft + pillW, pillTop + pillH), pillRadius, pillRadius, borderPaint)
                    drawText(effLabel, centerX, pillTop + pillPadV - labelPaint.fontMetrics.ascent, labelPaint)
                }
            }
        }
    }
}

@Composable
private fun IpaInspectorTable(rows: List<InspectionProductivityInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("検査員", "件", "生産", "不良率", "能率")
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
        IpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
            ) {
                IpaFlexibleCell(row.inspectorName.orEmpty(), columnWeights[0], columnAligns[0])
                IpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[1], columnAligns[1])
                IpaFlexibleCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), columnWeights[2], columnAligns[2])
                IpaFlexibleCell(
                    InspectionProductivityLogic.fmtPct(row.defectRatePercent),
                    columnWeights[3],
                    columnAligns[3],
                    color = Color(0xFFEA580C),
                    bold = true,
                )
                IpaFlexibleCell(
                    InspectionProductivityLogic.fmtEfficiency(inspectorEfficiency(row)),
                    columnWeights[4],
                    columnAligns[4],
                    color = ipaEmerald,
                    bold = true,
                )
            }
        }
        if (rows.isEmpty()) {
            Text(
                "データがありません",
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                fontSize = 11.sp,
                color = ProductionPlanningColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun IpaProductTable(rows: List<InspectionProductivityProductRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "件", "生産", "不良率", "能率")
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
        IpaFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 3.dp),
            ) {
                IpaFlexibleCell(row.productCd.orEmpty(), columnWeights[0], columnAligns[0], fontMono = true)
                IpaFlexibleCell(row.productName.orEmpty(), columnWeights[1], columnAligns[1])
                IpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[2], columnAligns[2])
                IpaFlexibleCell(InspectionProductivityLogic.fmtInt(row.sumActualQty), columnWeights[3], columnAligns[3])
                IpaFlexibleCell(
                    InspectionProductivityLogic.fmtPct(row.defectRatePercent),
                    columnWeights[4],
                    columnAligns[4],
                    color = Color(0xFFEA580C),
                    bold = true,
                )
                IpaFlexibleCell(
                    InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour),
                    columnWeights[5],
                    columnAligns[5],
                    color = ipaEmerald,
                    bold = true,
                )
            }
        }
        if (rows.isEmpty()) {
            Text(
                "データがありません",
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                fontSize = 11.sp,
                color = ProductionPlanningColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun IpaRankInspectorTable(rows: List<InspectionProductivityInspectorRowDto>) {
    val scroll = rememberScrollState()
    val headers = listOf("順位", "検査員", "件", "生産", "能率", "不良率")
    val columnWeights = listOf(0.9f, 2.4f, 0.7f, 1.15f, 1.1f, 1.1f)
    val columnAligns = listOf(
        TextAlign.Center,
        TextAlign.Start,
        TextAlign.End,
        TextAlign.End,
        TextAlign.Center,
        TextAlign.Center,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0x80FDE68A), RoundedCornerShape(10.dp))
            .heightIn(max = 280.dp)
            .verticalScroll(scroll),
    ) {
        IpaAmberFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val bg = if (index % 2 == 0) Color.White else Color(0xFFFFFBEB)
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(columnWeights[0]), contentAlignment = Alignment.Center) {
                    IpaWeldRankBadge(row.rank)
                }
                IpaFlexibleCell(row.inspectorName.orEmpty(), columnWeights[1], columnAligns[1], bold = true)
                IpaFlexibleCell("${row.sessionCount ?: 0}", columnWeights[2], columnAligns[2])
                IpaFlexibleCell(
                    InspectionProductivityLogic.fmtInt(row.sumActualQty),
                    columnWeights[3],
                    columnAligns[3],
                    bold = true,
                )
                Box(Modifier.weight(columnWeights[4]), contentAlignment = Alignment.Center) {
                    IpaAmberMetricPill(InspectionProductivityLogic.fmtEfficiency(row.efficiencyPerHour))
                }
                Box(Modifier.weight(columnWeights[5]), contentAlignment = Alignment.Center) {
                    IpaAmberMetricPill(InspectionProductivityLogic.fmtPct(row.defectRatePercent))
                }
            }
        }
    }
}

@Composable
private fun IpaRankOverviewTable(
    rows: List<InspectionProductivityProductRankingDto>,
    activeProductCd: String,
    onDetailClick: (String) -> Unit,
) {
    val scroll = rememberScrollState()
    val headers = listOf("CD", "製品名", "TOP検査員", "能率", "対象人数", "")
    val columnWeights = listOf(1.1f, 2.2f, 1.8f, 1f, 1f, 0.9f)
    val columnAligns = listOf(
        TextAlign.Start,
        TextAlign.Start,
        TextAlign.Start,
        TextAlign.Center,
        TextAlign.Center,
        TextAlign.Center,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .heightIn(max = 240.dp)
            .verticalScroll(scroll),
    ) {
        IpaAmberFlexibleTableHeader(headers, columnWeights, columnAligns)
        rows.forEachIndexed { index, row ->
            val active = row.productCd == activeProductCd
            val bg = when {
                active -> Color(0x8CFEF3C7)
                index % 2 == 0 -> Color.White
                else -> Color(0xFFFFFBEB)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IpaFlexibleCell(
                    row.productCd,
                    columnWeights[0],
                    columnAligns[0],
                    color = Color(0xFF2563EB),
                    bold = true,
                    fontMono = true,
                )
                IpaFlexibleCell(row.productName.orEmpty(), columnWeights[1], columnAligns[1], bold = true)
                IpaFlexibleCell(
                    row.topInspectorName ?: "—",
                    columnWeights[2],
                    columnAligns[2],
                    bold = true,
                )
                Box(Modifier.weight(columnWeights[3]), contentAlignment = Alignment.Center) {
                    IpaAmberMetricPill(InspectionProductivityLogic.fmtEfficiency(row.topEfficiencyPerHour))
                }
                IpaFlexibleCell("${row.rankedInspectorCount ?: 0}", columnWeights[4], columnAligns[4])
                Box(Modifier.weight(columnWeights[5]), contentAlignment = Alignment.Center) {
                    IpaRankDetailButton(active = active) { onDetailClick(row.productCd) }
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
private fun IpaAmberFlexibleTableHeader(
    headers: List<String>,
    weights: List<Float>,
    aligns: List<TextAlign>,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(ipaAmberHeader)
            .padding(vertical = 6.dp),
    ) {
        headers.forEachIndexed { i, h ->
            Text(
                h,
                modifier = Modifier.weight(weights[i]).padding(horizontal = 4.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ipaAmberDeep,
                textAlign = aligns[i],
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun IpaFlexibleTableHeader(
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
private fun RowScope.IpaFlexibleCell(
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
private fun IpaCsvButton(enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEEF2FF))
            .border(1.dp, Color(0x406366F1), RoundedCornerShape(999.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            Icons.Default.FileDownload,
            null,
            tint = if (enabled) Color(0xFF4338CA) else Color(0xFF94A3B8),
            modifier = Modifier.size(12.dp),
        )
        Text(
            "CSV",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color(0xFF4338CA) else Color(0xFF94A3B8),
        )
    }
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
