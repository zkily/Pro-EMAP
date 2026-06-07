package com.example.smart_emap.ui.erp.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.unit.Dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderDailySummaryUi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh

private val dailyNumberFormat = java.text.NumberFormat.getIntegerInstance(java.util.Locale.JAPAN)
private val dailyJapanZone = ZoneId.of("Asia/Tokyo")
private val dailyDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/** Web `.page-hero`：标题 + 操作 + 期间 + 筛选一体 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDailyPageHero(
    lastFetchedText: String,
    startDate: String,
    endDate: String,
    destinationCd: String,
    keyword: String,
    destinationOptions: List<DestinationOptionDto>,
    productOptions: List<MasterProductItemDto>,
    actionLoading: Boolean,
    exportEnabled: Boolean,
    onRefresh: () -> Unit,
    onExportCsv: () -> Unit,
    onCreate: () -> Unit,
    onQuickRange: (OrderDailyViewModel.QuickRange) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
) {
    var destExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    val activeRange = remember(startDate, endDate) { detectQuickRange(startDate, endDate) }
    val heroShape = RoundedCornerShape(16.dp)
    val heroGradient = Brush.linearGradient(
        listOf(Color(0xFF4F46E5), Color(0xFF6366F1), Color(0xFF7C3AED), Color(0xFF6D28D9)),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, heroShape, spotColor = Color(0x474F46E5))
            .clip(heroShape)
            .background(heroGradient)
            .border(1.dp, Color.White.copy(alpha = 0.22f), heroShape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.18f)),
        )
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "日受注管理",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp,
                    )
                    if (lastFetchedText.isNotBlank()) {
                        Text(
                            "更新: $lastFetchedText",
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DailyTbBtn("更新", Icons.Default.Refresh, DailyTbVariant.Refresh, !actionLoading, onRefresh)
                    DailyTbBtn("CSV", Icons.Default.FileDownload, DailyTbVariant.Export, exportEnabled && !actionLoading, onExportCsv)
                    DailyTbBtn("新規登録", Icons.AutoMirrored.Filled.NoteAdd, DailyTbVariant.Create, !actionLoading, onCreate)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.18f), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("期間", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium)
                listOf(
                    "今日" to OrderDailyViewModel.QuickRange.Today,
                    "今週" to OrderDailyViewModel.QuickRange.Week,
                    "今月" to OrderDailyViewModel.QuickRange.Month,
                    "先月" to OrderDailyViewModel.QuickRange.LastMonth,
                ).forEach { (label, range) ->
                    HeroQuickBtn(label, filled = activeRange == range) { onQuickRange(range) }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .border(1.dp, Color.White.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(15.dp))
                    OrderDailyCalendarRangeField(
                        startDate = startDate,
                        endDate = endDate,
                        accent = Color(0xFF6366F1),
                        onStartChange = onStartDateChange,
                        onEndChange = onEndDateChange,
                        fieldHeight = 32.dp,
                        fieldMinWidth = 118.dp,
                    )
                    Box(modifier = Modifier.width(1.dp).height(22.dp).background(Color(0x14000000)))
                    HeroFilterDropdown(
                        label = destinationOptions.find { it.cd == destinationCd }?.let { "${it.cd} | ${it.name}" }
                            ?: "納入先",
                        expanded = destExpanded,
                        onExpandedChange = { destExpanded = it },
                        minWidth = 150.dp,
                        showClear = destinationCd.isNotBlank(),
                        onClear = { onDestinationChange("") },
                    ) {
                        DropdownMenuItem(text = { Text("すべて") }, onClick = { onDestinationChange(""); destExpanded = false })
                        destinationOptions.forEach { d ->
                            DropdownMenuItem(
                                text = { Text("${d.cd} | ${d.name}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { onDestinationChange(d.cd); destExpanded = false },
                            )
                        }
                    }
                    HeroFilterDropdown(
                        label = productOptions.find { it.productCd == keyword }?.let { "${it.productCd} | ${it.productName}" }
                            ?: "製品",
                        expanded = productExpanded,
                        onExpandedChange = { productExpanded = it },
                        minWidth = 180.dp,
                        showClear = keyword.isNotBlank(),
                        onClear = { onKeywordChange("") },
                    ) {
                        DropdownMenuItem(text = { Text("すべて") }, onClick = { onKeywordChange(""); productExpanded = false })
                        productOptions.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.productCd} | ${p.productName}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { onKeywordChange(p.productCd.orEmpty()); productExpanded = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class DailyTbVariant { Refresh, Export, Create }

@Composable
private fun DailyTbBtn(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    variant: DailyTbVariant,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val (gradient, shadow) = when (variant) {
        DailyTbVariant.Refresh -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))) to Color(0xFF0EA5E9)
        DailyTbVariant.Export -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))) to Color(0xFFF59E0B)
        DailyTbVariant.Create -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))) to Color(0xFF10B981)
    }
    Box(
        modifier = Modifier
            .height(32.dp)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(10.dp), spotColor = shadow.copy(alpha = 0.45f))
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray.copy(0.4f), Color.Gray.copy(0.5f))))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.35f else 0.15f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.size(13.dp))
            Text(label, color = Color.White.copy(alpha = if (enabled) 1f else 0.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

@Composable
private fun HeroQuickBtn(text: String, filled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (filled) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f),
            )
            .border(1.dp, Color.White.copy(alpha = if (filled) 0.48f else 0.32f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroFilterDropdown(
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    minWidth: Dp,
    showClear: Boolean,
    onClear: () -> Unit,
    menuContent: @Composable () -> Unit,
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        Row(
            modifier = Modifier
                .widthIn(min = minWidth)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (showClear) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = OrderMonthlyColors.TextMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClear,
                        ),
                )
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            menuContent()
        }
    }
}

private fun detectQuickRange(start: String, end: String): OrderDailyViewModel.QuickRange? {
    if (start.isBlank() || end.isBlank()) return null
    val today = LocalDate.now(dailyJapanZone)
    val todayStr = today.format(dailyDateFormatter)
    if (start == end && start == todayStr) return OrderDailyViewModel.QuickRange.Today
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(dailyDateFormatter)
    val weekEnd = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusDays(6).format(dailyDateFormatter)
    if (start == weekStart && end == weekEnd) return OrderDailyViewModel.QuickRange.Week
    val monthStart = today.withDayOfMonth(1).format(dailyDateFormatter)
    val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).format(dailyDateFormatter)
    if (start == monthStart && end == monthEnd) return OrderDailyViewModel.QuickRange.Month
    val lastMonth = today.minusMonths(1)
    val lastStart = lastMonth.withDayOfMonth(1).format(dailyDateFormatter)
    val lastEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()).format(dailyDateFormatter)
    if (start == lastStart && end == lastEnd) return OrderDailyViewModel.QuickRange.LastMonth
    return null
}

@Composable
fun OrderDailySummaryStrip(summary: OrderDailySummaryUi) {
    data class Kpi(val title: String, val value: Int, val accent: Color)
    val cards = listOf(
        Kpi("件数", summary.count, Color(0xFF6366F1)),
        Kpi("確定本数", summary.confirmedUnits, Color(0xFF10B981)),
        Kpi("確定箱数", summary.confirmedBoxes, Color(0xFF3B82F6)),
        Kpi("内示本数", summary.forecastUnits, Color(0xFF8B5CF6)),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cards.forEach { card ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Color(0x0F000000))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.88f))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(52.dp)
                        .background(card.accent),
                )
                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 6.dp, bottom = 8.dp)) {
                    Text(card.title, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(
                        dailyNumberFormat.format(card.value),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.2).sp,
                    )
                }
            }
        }
    }
}

@Composable
fun OrderDailyTableSection(
    isLoading: Boolean,
    isEmpty: Boolean,
    rows: List<OrderDailyItemDto>,
    pageRangeText: String,
    page: Int,
    pageSize: Int,
    total: Int,
    onEdit: (OrderDailyItemDto) -> Unit,
    onDelete: (OrderDailyItemDto) -> Unit,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val sectionShape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, sectionShape, spotColor = Color(0x0A000000))
            .clip(sectionShape)
            .background(Color.White.copy(alpha = 0.82f))
            .border(1.dp, Color(0xFFE2E8F0), sectionShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("受注一覧", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Text("クリックで列ソート", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
        }
        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(bottom = 4.dp))
        when {
            isLoading && isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color(0xFF6366F1),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            isEmpty -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("該当データがありません", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
            else -> {
                OrderDailyDataTable(rows = rows, onEdit = onEdit, onDelete = onDelete)
                OrderDailyPaginationBar(
                    pageRangeText = pageRangeText,
                    page = page,
                    pageSize = pageSize,
                    total = total,
                    onPageChange = onPageChange,
                    onPageSizeChange = onPageSizeChange,
                )
            }
        }
    }
}

@Composable
fun OrderDailyDataTable(
    rows: List<OrderDailyItemDto>,
    onEdit: (OrderDailyItemDto) -> Unit,
    onDelete: (OrderDailyItemDto) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val scroll = rememberScrollState()
        val tableWidth = 1280.dp
        val headerGradient = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9)))
        Column(modifier = Modifier.horizontalScroll(scroll).width(tableWidth)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(headerGradient),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DailyTableHeader("日付", 96.dp)
                DailyTableHeader("曜日", 44.dp, TextAlign.Center)
                DailyTableHeader("月受注ID", 140.dp)
                DailyTableHeader("納入先CD", 84.dp)
                DailyTableHeader("納入先名", 120.dp)
                DailyTableHeader("製品CD", 84.dp)
                DailyTableHeader("製品名", 120.dp)
                DailyTableHeader("種別", 72.dp, TextAlign.Center)
                DailyTableHeader("内示本数", 72.dp, TextAlign.End)
                DailyTableHeader("確定箱数", 64.dp, TextAlign.End)
                DailyTableHeader("確定本数", 64.dp, TextAlign.End)
                DailyTableHeader("ステータス", 80.dp, TextAlign.Center)
                DailyTableHeader("納入日", 96.dp)
                DailyTableHeader("操作", 96.dp, TextAlign.Center)
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(if (index % 2 == 1) Color(0xFFFAFBFC) else Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DailyTableCell(row.date.orEmpty(), 96.dp)
                    DailyTableCell(row.weekday.orEmpty(), 44.dp, TextAlign.Center)
                    DailyTableCell(row.monthlyOrderId.orEmpty(), 140.dp, monospace = true)
                    DailyTableCell(row.destinationCd, 84.dp, monospace = true)
                    DailyTableCell(row.destinationName.orEmpty(), 120.dp)
                    DailyTableCell(row.productCd, 84.dp, monospace = true)
                    DailyTableCell(row.productName.orEmpty(), 120.dp)
                    DailyTableCell(row.productType.orEmpty(), 72.dp, TextAlign.Center)
                    DailyTableCell(row.forecastUnits.toString(), 72.dp, TextAlign.End, monospace = true)
                    DailyTableCell((row.confirmedBoxes ?: 0).toString(), 64.dp, TextAlign.End, monospace = true)
                    DailyTableCell((row.confirmedUnits ?: 0).toString(), 64.dp, TextAlign.End, monospace = true)
                    Box(Modifier.width(80.dp).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                        DailyStatusTag(row.status.orEmpty())
                    }
                    DailyTableCell(row.deliveryDate.orEmpty(), 96.dp)
                    Row(
                        modifier = Modifier.width(96.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("編集", fontSize = 12.sp, color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onEdit(row) }.padding(4.dp))
                        Text("削除", fontSize = 12.sp, color = Color(0xFFE11D48), fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onDelete(row) }.padding(4.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDailyPaginationBar(
    pageRangeText: String,
    page: Int,
    pageSize: Int,
    total: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val maxPage = maxOf(1, (total + pageSize - 1) / pageSize)
    var sizeExpanded by remember { mutableStateOf(false) }
    val visiblePages = remember(page, maxPage) {
        val start = maxOf(1, page - 2)
        val end = minOf(maxPage, start + 4)
        val adjustedStart = maxOf(1, end - 4)
        (adjustedStart..end).toList()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 2.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(pageRangeText, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ExposedDropdownMenuBox(expanded = sizeExpanded, onExpandedChange = { sizeExpanded = it }) {
                Row(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${pageSize}件/ページ", fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                }
                ExposedDropdownMenu(expanded = sizeExpanded, onDismissRequest = { sizeExpanded = false }) {
                    OrderDailyViewModel.PAGE_SIZES.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("${size}件/ページ") },
                            onClick = { onPageSizeChange(size); sizeExpanded = false },
                        )
                    }
                }
            }
            PaginationNavBtn(enabled = page > 1, forward = false) { onPageChange(page - 1) }
            visiblePages.forEach { p ->
                PaginationPageBtn(pageNum = p, active = p == page) { onPageChange(p) }
            }
            PaginationNavBtn(enabled = page < maxPage, forward = true) { onPageChange(page + 1) }
        }
    }
}

@Composable
private fun PaginationNavBtn(enabled: Boolean, forward: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (forward) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = if (enabled) Color(0xFF334155) else Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun PaginationPageBtn(pageNum: Int, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (active) Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                else Brush.linearGradient(listOf(Color.White, Color.White)),
            )
            .border(1.dp, if (active) Color(0xFF6366F1) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            pageNum.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else Color(0xFF334155),
        )
    }
}

@Composable
private fun DailyTableHeader(text: String, width: androidx.compose.ui.unit.Dp, align: TextAlign = TextAlign.Start) {
    Box(modifier = Modifier.width(width).padding(horizontal = 6.dp), contentAlignment = when (align) {
        TextAlign.Center -> Alignment.Center
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155), textAlign = align)
    }
}

@Composable
private fun DailyTableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    align: TextAlign = TextAlign.Start,
    monospace: Boolean = false,
) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = when (align) {
            TextAlign.Center -> Alignment.Center
            TextAlign.End -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        },
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = Color(0xFF1E293B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = align,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
        )
    }
}

@Composable
private fun DailyStatusTag(status: String) {
    val (bg, fg) = when {
        status.contains("取消") || status.contains("キャンセル") -> Color(0x1AEF4444) to Color(0xFFDC2626)
        status.contains("未出荷") || status.contains("保留") -> Color(0x1AF59E0B) to Color(0xFFD97706)
        status.contains("出荷済") || status.contains("完了") -> Color(0x1A10B981) to Color(0xFF059669)
        status.contains("出荷") -> Color(0x1A10B981) to Color(0xFF059669)
        else -> Color(0x1A6366F1) to Color(0xFF4F46E5)
    }
    Text(
        status.ifBlank { "—" },
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bg)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        maxLines = 1,
    )
}
