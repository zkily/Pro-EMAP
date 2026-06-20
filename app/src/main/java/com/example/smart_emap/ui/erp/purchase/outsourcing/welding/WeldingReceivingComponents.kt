package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingPaginationBar
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingReceivingStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTag
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculateWeldingReceivingStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingSupplierColor

private val ReceivingHeroGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val FilterAccent = Color(0xFF667EEA)
private val ReceivingStatusOptions = listOf("未検収", "一部検収", "検収済")

@Composable
fun WeldingReceivingHeroBar(count: Int, pendingCount: Int, todayCount: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(ReceivingHeroGradient)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("外注溶接受入", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                    Text(
                        "$count",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
            Text("外注溶接品の受入検収処理を行います", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReceivingKpiBadge(pendingCount.toString(), "未検収")
            ReceivingKpiBadge(todayCount.toString(), "本日入庫")
        }
    }
}

@Composable
private fun ReceivingKpiBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeldingReceivingFilterSection(
    startDate: String,
    endDate: String,
    keyword: String,
    suppliers: List<OutsourcingSupplierDto>,
    supplierId: Int?,
    productOptions: List<String>,
    productName: String?,
    statusFilter: String?,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onToday: () -> Unit,
    onThisMonth: () -> Unit,
    onKeywordChange: (String) -> Unit,
    onSupplierChange: (Int?) -> Unit,
    onProductChange: (String?) -> Unit,
    onStatusChange: (String?) -> Unit,
    onReset: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val scroll = rememberScrollState()
    val supplierOptions = listOf(null to "全て") + suppliers.mapNotNull { s ->
        s.id?.let { id ->
            val label = listOfNotNull(
                s.supplierCd?.takeIf { it.isNotBlank() },
                s.supplierName?.takeIf { it.isNotBlank() },
            ).joinToString(" - ").ifBlank { s.supplierCd.orEmpty() }
            id to label
        }
    }
    val supplierKey = supplierId?.toString().orEmpty()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x1A667EEA)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReceivingFilterGroup(label = "期間", icon = Icons.Default.CalendarMonth) {
                ReceivingCompactDateRange(startDate, endDate, onStartChange, onEndChange)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    ReceivingMiniBtn("", Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onShiftDate(-1) }
                    ReceivingMiniBtn("今日", null, filled = true, onClick = onToday)
                    ReceivingMiniBtn("", Icons.AutoMirrored.Filled.KeyboardArrowRight) { onShiftDate(1) }
                    ReceivingMiniBtn("今月", null, onClick = onThisMonth)
                }
            }
            ReceivingFilterDivider()
            ReceivingFilterGroup(label = "外注先", icon = Icons.Default.Search) {
                ReceivingCompactDropdown(
                    value = supplierKey,
                    options = supplierOptions.map { (id, label) -> (id?.toString() ?: "") to label },
                    width = 160.dp,
                    onSelect = { v -> onSupplierChange(v.toIntOrNull()) },
                )
            }
            ReceivingFilterGroup(label = "製品") {
                ReceivingCompactDropdown(
                    value = productName.orEmpty(),
                    options = listOf("" to "全て") + productOptions.map { it to it },
                    width = 140.dp,
                    onSelect = { onProductChange(it.takeIf { v -> v.isNotBlank() }) },
                )
            }
            ReceivingFilterGroup(label = "キーワード", icon = Icons.Default.Search) {
                ReceivingKeywordField(keyword, onKeywordChange)
            }
            ReceivingFilterGroup(label = "状態") {
                ReceivingCompactDropdown(
                    value = statusFilter.orEmpty(),
                    options = listOf("" to "全て") + ReceivingStatusOptions.map { it to it },
                    width = 96.dp,
                    onSelect = { onStatusChange(it.takeIf { v -> v.isNotBlank() }) },
                )
            }
            IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "リセット", tint = FilterAccent, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ReceivingKeywordField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .width(140.dp)
            .height(32.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0x33667EEA)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color(0xFF334155)),
                cursorBrush = SolidColor(FilterAccent),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text("製品名...", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                    inner()
                },
            )
        }
    }
}

@Composable
private fun ReceivingFilterGroup(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = FilterAccent, modifier = Modifier.size(14.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = FilterAccent)
        }
        content()
    }
}

@Composable
private fun ReceivingFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFFE2E8F0)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReceivingCompactDropdown(
    value: String,
    options: List<Pair<String, String>>,
    width: androidx.compose.ui.unit.Dp,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: if (value.isBlank()) "全て" else value
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Surface(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .width(width)
                .height(32.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(6.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0x33667EEA)),
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(display, modifier = Modifier.weight(1f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color(0xFF334155))
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, text) ->
                DropdownMenuItem(text = { Text(text, fontSize = 12.sp) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}

@Composable
private fun ReceivingCompactDateRange(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        ReceivingDateChip(startDate) { pickStart = true }
        Text("〜", fontSize = 10.sp, color = Color(0xFF94A3B8))
        ReceivingDateChip(endDate) { pickEnd = true }
    }
    if (pickStart) {
        OrderDailyDatePickerDialog(value = startDate, accent = FilterAccent, onDismiss = { pickStart = false }, onConfirm = { onStartChange(it); pickStart = false })
    }
    if (pickEnd) {
        OrderDailyDatePickerDialog(value = endDate, accent = FilterAccent, onDismiss = { pickEnd = false }, onConfirm = { onEndChange(it); pickEnd = false })
    }
}

@Composable
private fun ReceivingDateChip(date: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick).height(32.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0x33667EEA)),
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(date, fontSize = 11.sp, color = Color(0xFF334155))
        }
    }
}

@Composable
private fun ReceivingMiniBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = if (filled) ReceivingHeroGradient else Brush.linearGradient(listOf(Color.White, Color.White))
    val borderColor = if (filled) Color.Transparent else Color(0x4D667EEA)
    Surface(
        modifier = Modifier.height(28.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.background(bg).padding(horizontal = if (text.isBlank()) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = if (filled) Color.White else FilterAccent, modifier = Modifier.size(16.dp))
            }
            if (text.isNotBlank()) {
                Text(text, fontSize = 10.sp, fontWeight = if (filled) FontWeight.SemiBold else FontWeight.Medium, color = if (filled) Color.White else FilterAccent)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeldingReceivingActionSection(
    todayLabel: String,
    actionLoading: Boolean,
    onCreate: () -> Unit,
    onPrint: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stacked = maxWidth < 480.dp
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0x0F000000)),
        ) {
            if (stacked) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReceivingActionBtn("受入登録", Icons.Default.Add, Color(0xFF409EFF), Color(0xFF337ECC), !actionLoading, onCreate)
                        ReceivingActionBtn("印刷", Icons.Default.Print, Color(0xFFE6A23C), Color(0xFFCF9236), !actionLoading, onPrint)
                    }
                    ReceivingTodayTag(todayLabel)
                }
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReceivingActionBtn("受入登録", Icons.Default.Add, Color(0xFF409EFF), Color(0xFF337ECC), !actionLoading, onCreate)
                        ReceivingActionBtn("印刷", Icons.Default.Print, Color(0xFFE6A23C), Color(0xFFCF9236), !actionLoading, onPrint)
                    }
                    ReceivingTodayTag(todayLabel)
                }
            }
        }
    }
}

@Composable
private fun ReceivingActionBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    start: Color,
    end: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        color = Color.Transparent,
        modifier = Modifier.background(
            brush = if (enabled) Brush.linearGradient(listOf(start, end)) else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))),
            shape = shape,
        ),
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (enabled) Color.White else Color(0xFF94A3B8))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun ReceivingTodayTag(label: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF0F9EB), border = BorderStroke(1.dp, Color(0xFFC2E7B0))) {
        Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF67C23A))
    }
}

@Composable
fun WeldingReceivingTableSection(
    items: List<OutsourcingWeldingReceivingDto>,
    isLoading: Boolean,
    page: Int,
    pageSize: Int,
    totalCount: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onViewDetail: (OutsourcingWeldingReceivingDto) -> Unit,
    onViewOrder: (OutsourcingWeldingReceivingDto) -> Unit,
    onEdit: (OutsourcingWeldingReceivingDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        WeldingReceivingTable(items, isLoading, onViewDetail, onViewOrder, onEdit)
        if (totalCount > 0) {
            OutsourcingPaginationBar(page, pageSize, totalCount, onPageChange, onPageSizeChange)
        }
    }
}

@Composable
fun WeldingReceivingTable(
    items: List<OutsourcingWeldingReceivingDto>,
    isLoading: Boolean,
    onViewDetail: (OutsourcingWeldingReceivingDto) -> Unit,
    onViewOrder: (OutsourcingWeldingReceivingDto) -> Unit,
    onEdit: (OutsourcingWeldingReceivingDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minTableWidth = 1080.dp
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0x126366F1)),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val scrollH = rememberScrollState()
                val tableModifier = if (minTableWidth > maxWidth) {
                    Modifier.horizontalScroll(scrollH).widthIn(min = minTableWidth)
                } else {
                    Modifier.fillMaxWidth()
                }
                Column(tableModifier) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F7FA))
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            "受入番号" to 120.dp,
                            "受入予定日" to 84.dp,
                            "注文番号" to 120.dp,
                            "外注先" to 120.dp,
                            "製品CD" to 72.dp,
                            "製品名" to 130.dp,
                            "注文数" to 64.dp,
                            "受入数" to 64.dp,
                            "良品数" to 64.dp,
                            "不良数" to 64.dp,
                            "検収状態" to 80.dp,
                            "検収者" to 72.dp,
                            "操作" to 48.dp,
                        ).forEach { (label, w) ->
                            ReceivingHeaderCell(label, w, label.endsWith("数"))
                        }
                    }
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = FilterAccent)
                        }
                    } else if (items.isEmpty()) {
                        Text("データがありません", modifier = Modifier.padding(16.dp), fontSize = 12.sp, color = Color(0xFF94A3B8))
                    } else {
                        items.forEachIndexed { index, row ->
                            WeldingReceivingRow(
                                row = row,
                                striped = index % 2 == 1,
                                onViewDetail = { onViewDetail(row) },
                                onViewOrder = { onViewOrder(row) },
                                onEdit = { onEdit(row) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivingHeaderCell(label: String, width: androidx.compose.ui.unit.Dp, alignEnd: Boolean = false) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 2.dp),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.Center,
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF606266), textAlign = if (alignEnd) TextAlign.End else TextAlign.Center)
    }
}

@Composable
private fun WeldingReceivingRow(
    row: OutsourcingWeldingReceivingDto,
    striped: Boolean,
    onViewDetail: () -> Unit,
    onViewOrder: () -> Unit,
    onEdit: () -> Unit,
) {
    val status = calculateWeldingReceivingStatus(row)
    val supplierLabel = row.supplierName ?: row.supplierCd.orEmpty()
    val supplierColors = outsourcingSupplierColor(supplierLabel)
    val defectQty = row.defectQty ?: 0
    val rowBg = when {
        status == "未検収" -> Color(0xFFFEF9E7)
        defectQty > 0 -> Color(0xFFFDECDA)
        striped -> Color(0xFFFAFAFA)
        else -> Color.White
    }
    HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
    Row(
        modifier = Modifier.fillMaxWidth().height(34.dp).background(rowBg).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ReceivingBodyCell(120.dp) {
            Text(
                row.receivingNo.orEmpty(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF667EEA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onViewDetail),
            )
        }
        ReceivingBodyCell(84.dp) { Text(row.receivingDate.orEmpty(), fontSize = 10.sp, maxLines = 1) }
        ReceivingBodyCell(120.dp) {
            Text(
                row.orderNo.orEmpty(),
                fontSize = 10.sp,
                color = Color(0xFF909399),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onViewOrder),
            )
        }
        ReceivingBodyCell(120.dp) {
            OutsourcingTag(text = supplierLabel.ifBlank { "—" }, containerColor = supplierColors.container, contentColor = supplierColors.content)
        }
        ReceivingBodyCell(72.dp) { Text(row.productCd.orEmpty(), fontSize = 10.sp, maxLines = 1) }
        ReceivingBodyCell(130.dp, alignStart = true) {
            Text(row.productName.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ReceivingBodyCell(64.dp, alignEnd = true) { Text(formatOutsourcingNumber(row.orderQty), fontSize = 10.sp) }
        ReceivingBodyCell(64.dp, alignEnd = true) {
            Text(formatOutsourcingNumber(row.receivingQty), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF667EEA))
        }
        ReceivingBodyCell(64.dp, alignEnd = true) {
            Text(formatOutsourcingNumber(row.goodQty), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF67C23A))
        }
        ReceivingBodyCell(64.dp, alignEnd = true) {
            Text(
                formatOutsourcingNumber(row.defectQty),
                fontSize = 10.sp,
                fontWeight = if (defectQty > 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (defectQty > 0) Color(0xFFF56C6C) else Color(0xFF606266),
            )
        }
        ReceivingBodyCell(80.dp) { OutsourcingReceivingStatusChip(status) }
        ReceivingBodyCell(72.dp) { Text(row.inspector.orEmpty().ifBlank { "—" }, fontSize = 10.sp, maxLines = 1) }
        ReceivingBodyCell(48.dp) {
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF409EFF), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ReceivingBodyCell(
    width: androidx.compose.ui.unit.Dp,
    alignEnd: Boolean = false,
    alignStart: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 2.dp),
        contentAlignment = when {
            alignEnd -> Alignment.CenterEnd
            alignStart -> Alignment.CenterStart
            else -> Alignment.Center
        },
    ) {
        content()
    }
}
