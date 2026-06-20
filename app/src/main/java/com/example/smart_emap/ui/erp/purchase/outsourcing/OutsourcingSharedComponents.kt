package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.PurchaseTabChipRow
import java.text.NumberFormat
import java.util.Locale

val OutsourcingCardShape = RoundedCornerShape(10.dp)
val OutsourcingGlassTop = Color(0xFFFFFFFF)
val OutsourcingGlassMid = Color(0xFFEEF2FF)
val OutsourcingGlassBottom = Color(0xFFF5F3FF)
val OutsourcingGlassGradient = Brush.linearGradient(
    listOf(
        OutsourcingGlassTop.copy(alpha = 0.95f),
        OutsourcingGlassMid.copy(alpha = 0.82f),
        OutsourcingGlassBottom.copy(alpha = 0.88f),
    ),
)

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

fun formatOutsourcingNumber(value: Int): String = jpNumber.format(value)

data class OutsourcingKpiItem(
    val value: String,
    val label: String,
    val accent: Color = Color(0xFF6366F1),
)

@Composable
fun OutsourcingGlassCard(
    modifier: Modifier = Modifier,
    accent: Brush? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, OutsourcingCardShape, spotColor = Color(0x206366F1)),
        shape = OutsourcingCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, Color(0xFFE8ECF4)),
    ) {
        Box {
            if (accent != null) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(52.dp)
                        .background(accent)
                        .align(Alignment.CenterStart),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OutsourcingGlassGradient)
                    .padding(start = if (accent != null) 12.dp else 10.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun OutsourcingHeroHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    kpis: List<OutsourcingKpiItem> = emptyList(),
    badge: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                            if (!badge.isNullOrBlank()) {
                                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                                    Text(badge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Text(subtitle, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                if (kpis.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        kpis.forEach { kpi ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.18f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(kpi.value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    Text(kpi.label, fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }
                } else if (trailing != null) {
                    trailing()
                }
            }
        }
    }
}

@Composable
fun OutsourcingFilterCard(
    title: String = "検索条件",
    accent: Color = Color(0xFF6366F1),
    titleIcon: ImageVector = Icons.Default.Search,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    OutsourcingGlassCard(accent = Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f)))) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(titleIcon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1E293B))
                }
                trailing?.invoke()
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            content()
        }
    }
}

@Composable
fun OutsourcingActionBar(
    leftContent: @Composable () -> Unit,
    rightContent: (@Composable () -> Unit)? = null,
) {
    OutsourcingGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                leftContent()
            }
            rightContent?.invoke()
        }
    }
}

@Composable
fun OutsourcingActionButton(
    text: String,
    icon: ImageVector? = null,
    containerColor: Color = Color(0xFF6366F1),
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    badge: Int? = null,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        if (badge != null && badge > 0) {
            Spacer(Modifier.width(4.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.25f)) {
                Text("$badge", modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun OutsourcingSummaryStrip(cards: List<Triple<ImageVector, String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        cards.forEach { (icon, value, label) ->
            OutsourcingGlassCard(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF1E3C72), Color(0xFF4FACFE)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1F2937), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(label, fontSize = 9.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingStatCards(cards: List<Triple<Brush, String, String>>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        cards.forEach { (gradient, value, label) ->
            OutsourcingGlassCard(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(gradient),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("●", color = Color.White, fontSize = 18.sp)
                    }
                    Column {
                        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF303133))
                        Text(label, fontSize = 10.sp, color = Color(0xFF909399))
                    }
                }
            }
        }
    }
}

enum class OutsourcingStockStatus { Normal, Low, Empty }

fun resolveOutsourcingStockStatus(stockQty: Int, minStock: Int): OutsourcingStockStatus = when {
    stockQty <= 0 -> OutsourcingStockStatus.Empty
    stockQty < minStock -> OutsourcingStockStatus.Low
    else -> OutsourcingStockStatus.Normal
}

@Composable
fun OutsourcingStatusChip(status: OutsourcingStockStatus) {
    val (bg, fg, label) = when (status) {
        OutsourcingStockStatus.Normal -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "正常")
        OutsourcingStockStatus.Low -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "僅少")
        OutsourcingStockStatus.Empty -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "なし")
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OutsourcingIssueStatusChip(status: String) {
    val (bg, fg, label) = when (status) {
        "preparing" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "準備中")
        "issued" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "出庫済")
        "returned" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "返却済")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), status)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OutsourcingMaterialHistoryTypeChip(type: String) {
    val isIssue = type == "issue" || type == "receive"
    val (bg, fg, label) = if (isIssue) {
        Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), if (type == "receive") "入庫" else "支給")
    } else {
        Triple(Color(0xFFFEF3C7), Color(0xFFD97706), if (type == "usage") "使用" else "出庫")
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = fg, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutsourcingDropdownField(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = options.find { it.first == value }?.second ?: if (value.isBlank()) "" else value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6366F1)),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("—", fontSize = 12.sp) },
                onClick = { onSelect(""); expanded = false },
            )
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = { onSelect(id); expanded = false },
                )
            }
        }
    }
}

@Composable
fun OutsourcingTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF6366F1)),
    )
}

@Composable
fun OutsourcingTabStrip(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    PurchaseTabChipRow(tabs = tabs, selectedIndex = selectedIndex, onSelect = onSelect)
}

@Composable
fun OutsourcingPaginationBar(
    page: Int,
    pageSize: Int,
    totalCount: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
) {
    val totalPages = maxOf(1, (totalCount + pageSize - 1) / pageSize)
    OutsourcingGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("全 $totalCount 件", fontSize = 11.sp, color = Color(0xFF64748B))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { onPageChange(page - 1) }, enabled = page > 1, contentPadding = PaddingValues(2.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text("$page / $totalPages", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                TextButton(onClick = { onPageChange(page + 1) }, enabled = page < totalPages, contentPadding = PaddingValues(2.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                OutsourcingPageSizeMenu(pageSize = pageSize, onSelect = onPageSizeChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutsourcingPageSizeMenu(pageSize: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        ) {
            Text("${pageSize}件", fontSize = 10.sp)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(20, 50, 100, 200).forEach { size ->
                DropdownMenuItem(
                    text = { Text("$size 件/页", fontSize = 11.sp) },
                    onClick = { onSelect(size); expanded = false },
                )
            }
        }
    }
}

@Composable
fun OutsourcingTableLoading(isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6366F1), modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun OutsourcingTableHeaderCell(text: String, modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 6.dp, vertical = 8.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF475569),
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
    )
}

@Composable
fun OutsourcingTableCell(
    text: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    color: Color = Color(0xFF334155),
    fontWeight: FontWeight = FontWeight.Normal,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier.padding(horizontal = 6.dp, vertical = 6.dp)
    if (onClick != null) {
        Text(
            text = text,
            modifier = base.clickable(onClick = onClick),
            fontSize = 11.sp,
            color = Color(0xFF2563EB),
            fontWeight = FontWeight.Medium,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Text(
            text = text,
            modifier = base,
            fontSize = 11.sp,
            color = color,
            fontWeight = fontWeight,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun OutsourcingInfoTag(text: String, accent: Color = Color(0xFF6366F1)) {
    Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = 0.12f)) {
        Text(text, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, color = accent, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun OutsourcingHorizontalTable(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scroll = rememberScrollState()
    OutsourcingGlassCard(modifier = modifier) {
        Column {
            Row(modifier = Modifier.horizontalScroll(scroll)) {
                content()
            }
        }
    }
}

@Composable
fun OutsourcingOrderStatusChip(status: String) {
    val color = orderStatusColor(status)
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Text(status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun OutsourcingReceivingStatusChip(status: String) {
    val color = receivingStatusColor(status)
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Text(status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun OutsourcingProgressBar(received: Int, quantity: Int, modifier: Modifier = Modifier) {
    val pct = orderProgressPercent(received, quantity)
    Column(modifier = modifier.width(72.dp)) {
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = if (pct >= 100) Color(0xFF10B981) else Color(0xFF6366F1),
            trackColor = Color(0xFFE2E8F0),
        )
        Text("$pct%", fontSize = 9.sp, color = Color(0xFF64748B), modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun OutsourcingDateFilterRow(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onToday: () -> Unit,
    onThisMonth: () -> Unit,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(onClick = { showStartPicker = true }, shape = RoundedCornerShape(8.dp), color = Color(0xFFEEF2FF), border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                Text(startDate, fontSize = 11.sp)
            }
        }
        Text("〜", fontSize = 11.sp, color = Color(0xFF64748B))
        Surface(onClick = { showEndPicker = true }, shape = RoundedCornerShape(8.dp), color = Color(0xFFEEF2FF), border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                Text(endDate, fontSize = 11.sp)
            }
        }
        TextButton(onClick = { onShiftDate(-1) }, contentPadding = PaddingValues(2.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6366F1))
        }
        Button(onClick = onToday, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp), shape = RoundedCornerShape(6.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))) {
            Text("今日", fontSize = 10.sp)
        }
        TextButton(onClick = { onShiftDate(1) }, contentPadding = PaddingValues(2.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6366F1))
        }
        TextButton(onClick = onThisMonth) { Text("今月", fontSize = 10.sp, color = Color(0xFF6366F1)) }
    }
    if (showStartPicker) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = Color(0xFF6366F1),
            onDismiss = { showStartPicker = false },
            onConfirm = { date ->
                onStartChange(date)
                showStartPicker = false
            },
        )
    }
    if (showEndPicker) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = Color(0xFF6366F1),
            onDismiss = { showEndPicker = false },
            onConfirm = { date ->
                onEndChange(date)
                showEndPicker = false
            },
        )
    }
}

@Composable
fun OutsourcingConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "確認",
    loading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        OutsourcingGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(message, fontSize = 13.sp, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    Button(onClick = onConfirm, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        else Text(confirmText)
                    }
                }
            }
        }
    }
}
