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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PrecisionManufacturing
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.ui.erp.order.OrderDailyDatePickerDialog
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingOrderStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingProgressBar
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTag
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculateWeldingOrderStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingCurrency
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.orderAmount
import com.example.smart_emap.ui.erp.purchase.outsourcing.weldingOrderReceivingQty

private val WeldingHeroGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
private val FilterAccent = Color(0xFF667EEA)
private val OrderStatusOptions = listOf("未発注", "発注済", "一部受入", "受入完")

@Composable
fun WeldingOrderHeroBar(orderCount: Int) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape, spotColor = Color(0x40667EEA))
            .clip(shape)
            .background(WeldingHeroGradient)
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
            Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("外注溶接注文", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = 0.25f)) {
                    Text(
                        "$orderCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                }
            }
            Text("外注溶接加工の注文作成・管理を行います", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WeldingOrderFilterSection(
    startDate: String,
    endDate: String,
    suppliers: List<OutsourcingSupplierDto>,
    supplierCd: String?,
    productOptions: List<String>,
    productName: String?,
    statusFilter: String?,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onToday: () -> Unit,
    onThisMonth: () -> Unit,
    onSupplierChange: (String?) -> Unit,
    onProductChange: (String?) -> Unit,
    onStatusChange: (String?) -> Unit,
    onReset: () -> Unit,
) {
    val shape = RoundedCornerShape(10.dp)
    val scroll = rememberScrollState()
    val supplierOptions = listOf("" to "全て") + suppliers.mapNotNull { s ->
        s.supplierCd?.let { cd -> cd to (s.supplierName ?: cd) }
    }

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
            WeldingFilterGroup(label = "期間", icon = Icons.Default.CalendarMonth) {
                WeldingCompactDateRange(
                    startDate = startDate,
                    endDate = endDate,
                    onStartChange = onStartChange,
                    onEndChange = onEndChange,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    WeldingMiniBtn("", Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onShiftDate(-1) }
                    WeldingMiniBtn("今日", null, filled = true, onClick = onToday)
                    WeldingMiniBtn("", Icons.AutoMirrored.Filled.KeyboardArrowRight) { onShiftDate(1) }
                    WeldingMiniBtn("今月", null, onClick = onThisMonth)
                }
            }
            WeldingFilterDivider()
            WeldingFilterGroup(label = "外注先", icon = Icons.Default.Search) {
                WeldingCompactDropdown(
                    value = supplierCd.orEmpty(),
                    options = supplierOptions,
                    width = 160.dp,
                    onSelect = { onSupplierChange(it.takeIf { v -> v.isNotBlank() }) },
                )
            }
            WeldingFilterGroup(label = "製品") {
                WeldingCompactDropdown(
                    value = productName.orEmpty(),
                    options = listOf("" to "全て") + productOptions.map { it to it },
                    width = 140.dp,
                    onSelect = { onProductChange(it.takeIf { v -> v.isNotBlank() }) },
                )
            }
            WeldingFilterGroup(label = "状態") {
                WeldingCompactDropdown(
                    value = statusFilter.orEmpty(),
                    options = listOf("" to "全て") + OrderStatusOptions.map { it to it },
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
private fun WeldingFilterGroup(
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
private fun WeldingFilterDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color(0xFFE2E8F0)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeldingCompactDropdown(
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
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    display,
                    modifier = Modifier.weight(1f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF334155),
                )
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
private fun WeldingCompactDateRange(
    startDate: String,
    endDate: String,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        WeldingDateChip(startDate) { pickStart = true }
        Text("〜", fontSize = 10.sp, color = Color(0xFF94A3B8))
        WeldingDateChip(endDate) { pickEnd = true }
    }
    if (pickStart) {
        OrderDailyDatePickerDialog(
            value = startDate,
            accent = FilterAccent,
            onDismiss = { pickStart = false },
            onConfirm = { onStartChange(it); pickStart = false },
        )
    }
    if (pickEnd) {
        OrderDailyDatePickerDialog(
            value = endDate,
            accent = FilterAccent,
            onDismiss = { pickEnd = false },
            onConfirm = { onEndChange(it); pickEnd = false },
        )
    }
}

@Composable
private fun WeldingDateChip(date: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(32.dp),
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
private fun WeldingMiniBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    val bg = if (filled) WeldingHeroGradient else Brush.linearGradient(listOf(Color.White, Color.White))
    val borderColor = if (filled) Color.Transparent else Color(0x4D667EEA)
    Surface(
        modifier = Modifier
            .height(28.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .background(bg)
                .padding(horizontal = if (text.isBlank()) 4.dp else 8.dp),
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
fun WeldingOrderActionSection(
    totalLabel: String,
    actionLoading: Boolean,
    onCreate: () -> Unit,
    onBatchCreate: () -> Unit,
    onPrint: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stacked = maxWidth < 520.dp
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, Color(0x0F000000)),
        ) {
            if (stacked) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WeldingActionBtn("新規注文", Icons.Default.Add, Color(0xFF409EFF), Color(0xFF337ECC), !actionLoading, onCreate)
                        WeldingActionBtn("新規一括注文", Icons.Default.Add, Color(0xFF67C23A), Color(0xFF529B2E), !actionLoading, onBatchCreate)
                        WeldingActionBtn("注文書発行", Icons.Default.Print, Color(0xFFE6A23C), Color(0xFFCF9236), !actionLoading, onPrint)
                    }
                    WeldingTotalTag(totalLabel)
                }
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        WeldingActionBtn("新規注文", Icons.Default.Add, Color(0xFF409EFF), Color(0xFF337ECC), !actionLoading, onCreate)
                        WeldingActionBtn("新規一括注文", Icons.Default.Add, Color(0xFF67C23A), Color(0xFF529B2E), !actionLoading, onBatchCreate)
                        WeldingActionBtn("注文書発行", Icons.Default.Print, Color(0xFFE6A23C), Color(0xFFCF9236), !actionLoading, onPrint)
                    }
                    WeldingTotalTag(totalLabel)
                }
            }
        }
    }
}

@Composable
private fun WeldingActionBtn(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    start: Color,
    end: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.background(
            if (enabled) Brush.linearGradient(listOf(start, end)) else Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))),
            RoundedCornerShape(8.dp),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (enabled) Color.White else Color(0xFF94A3B8))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) Color.White else Color(0xFF94A3B8), modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun WeldingTotalTag(totalLabel: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFF0F9FF),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
    ) {
        Text(
            totalLabel,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0369A1),
        )
    }
}

@Composable
fun WeldingOrderTable(
    orders: List<OutsourcingWeldingOrderDto>,
    isLoading: Boolean,
    onEdit: (OutsourcingWeldingOrderDto) -> Unit,
    onPrint: (OutsourcingWeldingOrderDto) -> Unit,
    onDelete: (OutsourcingWeldingOrderDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minTableWidth = 980.dp
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
                            "注文番号" to 120.dp,
                            "納入日" to 84.dp,
                            "外注先" to 120.dp,
                            "製品名" to 140.dp,
                            "数量" to 64.dp,
                            "単価" to 72.dp,
                            "金額" to 80.dp,
                            "納期" to 84.dp,
                            "入庫数" to 64.dp,
                            "状態" to 80.dp,
                            "進捗" to 88.dp,
                            "操作" to 96.dp,
                        ).forEach { (label, w) ->
                            WeldingHeaderCell(label, w, label in listOf("数量", "単価", "金額", "入庫数"))
                        }
                    }
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = FilterAccent)
                        }
                    } else if (orders.isEmpty()) {
                        Text("データがありません", modifier = Modifier.padding(16.dp), fontSize = 12.sp, color = Color(0xFF94A3B8))
                    } else {
                        orders.forEachIndexed { index, row ->
                            WeldingOrderRow(
                                row = row,
                                striped = index % 2 == 1,
                                onEdit = { onEdit(row) },
                                onPrint = { onPrint(row) },
                                onDelete = { onDelete(row) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeldingHeaderCell(label: String, width: androidx.compose.ui.unit.Dp, alignEnd: Boolean = false) {
    Box(
        modifier = Modifier.width(width).padding(horizontal = 2.dp),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.Center,
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF606266), textAlign = if (alignEnd) TextAlign.End else TextAlign.Center)
    }
}

@Composable
private fun WeldingOrderRow(
    row: OutsourcingWeldingOrderDto,
    striped: Boolean,
    onEdit: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit,
) {
    val received = weldingOrderReceivingQty(row)
    val status = calculateWeldingOrderStatus(row)
    val supplierLabel = row.supplierName ?: row.supplierCd.orEmpty()
    HorizontalDivider(color = Color(0xFFEBEEF5), thickness = 0.5.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(if (striped) Color(0xFFFAFAFA) else Color.White)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeldingBodyCell(120.dp) {
            Text(
                row.orderNo.orEmpty(),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF409EFF),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onEdit),
            )
        }
        WeldingBodyCell(84.dp) { Text(row.orderDate.orEmpty(), fontSize = 10.sp, maxLines = 1) }
        WeldingBodyCell(120.dp) {
            OutsourcingTag(text = supplierLabel.ifBlank { "—" }, containerColor = Color(0xFFECF5FF), contentColor = Color(0xFF409EFF))
        }
        WeldingBodyCell(140.dp, alignStart = true) {
            Text(row.productName.orEmpty(), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        WeldingBodyCell(64.dp, alignEnd = true) { Text(formatOutsourcingNumber(row.quantity), fontSize = 10.sp) }
        WeldingBodyCell(72.dp, alignEnd = true) { Text(formatOutsourcingCurrency(row.unitPrice), fontSize = 10.sp) }
        WeldingBodyCell(80.dp, alignEnd = true) {
            Text(formatOutsourcingCurrency(orderAmount(row.quantity, row.unitPrice)), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF303133))
        }
        WeldingBodyCell(84.dp) { Text(row.deliveryDate.orEmpty(), fontSize = 10.sp, maxLines = 1) }
        WeldingBodyCell(64.dp, alignEnd = true) { Text(formatOutsourcingNumber(received), fontSize = 10.sp) }
        WeldingBodyCell(80.dp) { OutsourcingOrderStatusChip(status) }
        WeldingBodyCell(88.dp) { OutsourcingProgressBar(received, row.quantity ?: 0, Modifier.padding(horizontal = 2.dp)) }
        WeldingBodyCell(96.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF409EFF), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onPrint, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = Color(0xFF909399), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFF56C6C), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun WeldingBodyCell(
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
