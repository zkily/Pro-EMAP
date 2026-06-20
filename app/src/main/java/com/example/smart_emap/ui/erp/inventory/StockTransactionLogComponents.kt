package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.smart_emap.data.model.InventoryStockTransactionLogRowDto
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDateFilterRow

@Composable
fun StockTransactionLogHeroBar(total: Int) {
    InventoryGlassHeader(
        title = "在庫取引記録",
        subtitle = "在庫受払履歴の照会・編集",
        icon = Icons.Default.Description,
        dark = false,
        trailing = {
            Text("${total}件", fontSize = 11.sp, color = Color(0xFF64748B))
        },
    )
}

@Composable
fun StockTransactionLogStatsRow(
    totalQuantity: Double,
    inboundQuantity: Double,
    outboundQuantity: Double,
    totalRecords: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StockTransactionStatCard("総数量", formatInventoryNum(totalQuantity), Modifier.weight(1f))
        StockTransactionStatCard("入庫系", formatInventoryNum(inboundQuantity), Modifier.weight(1f))
        StockTransactionStatCard("出庫系", formatInventoryNum(outboundQuantity), Modifier.weight(1f))
        StockTransactionStatCard("総件数", formatInventoryNum(totalRecords), Modifier.weight(1f))
    }
}

@Composable
private fun StockTransactionStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 9.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun StockTransactionLogFilterPanel(
    uiState: StockTransactionLogUiState,
    onStockTypeChange: (String) -> Unit,
    onTargetCdChange: (String) -> Unit,
    onKeywordChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTransactionTypeChange: (String) -> Unit,
    onProcessChange: (String) -> Unit,
    onDateStartChange: (String) -> Unit,
    onDateEndChange: (String) -> Unit,
    onShiftDate: (Int) -> Unit,
    onToday: () -> Unit,
    onReset: () -> Unit,
) {
    InventoryFilterPanel(onReset = onReset) {
        Text("在庫種別", fontSize = 10.sp, color = Color(0xFF64748B))
        InventoryChipRow(
            options = STOCK_TYPES,
            selected = uiState.filterStockType,
            onSelect = onStockTypeChange,
            dark = false,
        )
        InventoryDropdownField(
            label = "対象CD（製品）",
            value = uiState.filterTargetCd,
            options = uiState.productOptions.map { it.productCd to "${it.productCd} | ${it.productName.orEmpty()}" },
            onSelect = onTargetCdChange,
        )
        InventoryTextField(
            label = "キーワード",
            value = uiState.filterKeyword,
            onValueChange = onKeywordChange,
        )
        InventoryDropdownField(
            label = "保管場所",
            value = uiState.filterLocationCd,
            options = uiState.locationOptions,
            onSelect = onLocationChange,
        )
        InventoryDropdownField(
            label = "操作種別",
            value = uiState.filterTransactionType,
            options = TRANSACTION_TYPES.map { it to it },
            onSelect = onTransactionTypeChange,
        )
        InventoryDropdownField(
            label = "工程",
            value = uiState.filterProcessCd,
            options = uiState.processOptions,
            onSelect = onProcessChange,
        )
        Text("操作日", fontSize = 10.sp, color = Color(0xFF64748B))
        OutsourcingDateFilterRow(
            startDate = uiState.filterDateStart,
            endDate = uiState.filterDateEnd,
            onStartChange = onDateStartChange,
            onEndChange = onDateEndChange,
            onShiftDate = onShiftDate,
            onToday = onToday,
            onThisMonth = {
                val today = java.time.LocalDate.now()
                onDateStartChange(today.withDayOfMonth(1).toString())
                onDateEndChange(today.toString())
            },
        )
    }
}

@Composable
fun StockTransactionLogTable(
    rows: List<InventoryStockTransactionLogRowDto>,
    onEdit: (InventoryStockTransactionLogRowDto) -> Unit,
    onDelete: (InventoryStockTransactionLogRowDto) -> Unit,
) {
    val scroll = rememberScrollState()
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            InventoryTableHeaderCell("操作日", 88)
            InventoryTableHeaderCell("在庫種別", 56)
            InventoryTableHeaderCell("操作種別", 56)
            InventoryTableHeaderCell("対象CD", 72)
            InventoryTableHeaderCell("品名", 100)
            InventoryTableHeaderCell("工程", 72)
            InventoryTableHeaderCell("保管場所", 72)
            InventoryTableHeaderCell("数量", 56)
            InventoryTableHeaderCell("単位", 40)
            InventoryTableHeaderCell("備考", 80)
            InventoryTableHeaderCell("操作", 72)
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                InventoryTableCell(formatTransactionTime(row.transactionTime), 88)
                InventoryTableCell(row.stockType.orEmpty(), 56)
                InventoryTableCell(row.transactionType.orEmpty(), 56)
                InventoryTableCell(row.targetCd.orEmpty(), 72)
                InventoryTableCell(row.productName.orEmpty(), 100)
                InventoryTableCell(row.processName ?: row.processCd.orEmpty(), 72)
                InventoryTableCell(row.locationCd.orEmpty(), 72)
                InventoryTableCell(formatInventoryNum(row.quantity), 56, bold = true)
                InventoryTableCell(row.unit.orEmpty(), 40)
                InventoryTableCell(row.remarks.orEmpty(), 80)
                Row(modifier = Modifier.width(72.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = { onEdit(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("編集", fontSize = 9.sp)
                    }
                    TextButton(onClick = { onDelete(row) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                        Text("削除", fontSize = 9.sp, color = Color(0xFFEF4444))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}

private fun formatTransactionTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return value.replace('T', ' ').take(16)
}

@Composable
fun StockTransactionLogEditDialog(
    visible: Boolean,
    form: StockTransactionLogFormUi,
    loading: Boolean,
    processOptions: List<Pair<String, String>>,
    locationOptions: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onFormChange: ((StockTransactionLogFormUi) -> StockTransactionLogFormUi) -> Unit,
) {
    if (!visible) return
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("履歴データ編集", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                InventoryDropdownField(
                    label = "在庫種別",
                    value = form.stockType,
                    options = STOCK_TYPES.map { it to it },
                    onSelect = { onFormChange { f -> f.copy(stockType = it) } },
                    allowEmpty = false,
                )
                InventoryDropdownField(
                    label = "操作種別",
                    value = form.transactionType,
                    options = TRANSACTION_TYPES.map { it to it },
                    onSelect = { onFormChange { f -> f.copy(transactionType = it) } },
                    allowEmpty = false,
                )
                InventoryTextField(
                    label = "対象CD",
                    value = form.targetCd,
                    onValueChange = { onFormChange { f -> f.copy(targetCd = it) } },
                )
                InventoryDropdownField(
                    label = "保管場所",
                    value = form.locationCd,
                    options = locationOptions,
                    onSelect = { onFormChange { f -> f.copy(locationCd = it) } },
                    allowEmpty = false,
                )
                InventoryDropdownField(
                    label = "工程",
                    value = form.processCd,
                    options = processOptions,
                    onSelect = { onFormChange { f -> f.copy(processCd = it) } },
                )
                InventoryTextField(
                    label = "数量",
                    value = form.quantity,
                    onValueChange = { onFormChange { f -> f.copy(quantity = it) } },
                )
                InventoryTextField(
                    label = "単位",
                    value = form.unit,
                    onValueChange = { onFormChange { f -> f.copy(unit = it) } },
                )
                InventoryTextField(
                    label = "操作日時",
                    value = form.transactionTime,
                    onValueChange = { onFormChange { f -> f.copy(transactionTime = it) } },
                )
                InventoryTextField(
                    label = "備考",
                    value = form.remarks,
                    onValueChange = { onFormChange { f -> f.copy(remarks = it) } },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(androidx.compose.ui.Alignment.End)) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    Button(
                        onClick = onSubmit,
                        enabled = !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA)),
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}
