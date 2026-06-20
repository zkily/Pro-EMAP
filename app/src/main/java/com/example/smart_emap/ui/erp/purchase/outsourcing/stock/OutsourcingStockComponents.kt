package com.example.smart_emap.ui.erp.purchase.outsourcing.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.smart_emap.ui.erp.purchase.PurchaseEmptyHint
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingActionBar
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingActionButton
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingFilterCard
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingGlassCard
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingHeroHeader
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingHorizontalTable
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingKpiItem
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingMaterialHistoryTypeChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingPaginationBar
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingSummaryStrip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTabStrip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableHeaderCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableLoading
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDropdownField
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTextField
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.resolveOutsourcingStockStatus

private val stockHeroGradient = Brush.linearGradient(listOf(Color(0xFF1E3C72), Color(0xFF2A5298), Color(0xFF4FACFE)))

@Composable
fun OutsourcingStockHeroBar(uiState: OutsourcingStockUiState) {
    OutsourcingHeroHeader(
        title = "外注在庫管理",
        subtitle = "外注メッキ・溶接品の在庫状況を一元管理",
        icon = Icons.Default.Inventory2,
        gradient = stockHeroGradient,
        kpis = listOf(
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.platingStockCount), "メッキ品種", Color(0xFF4ECDC4)),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.weldingStockCount), "溶接品種", Color(0xFFA777E3)),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.totalStockQty), "総在庫数"),
        ) + if (uiState.lowStockCount > 0) {
            listOf(OutsourcingKpiItem(formatOutsourcingNumber(uiState.lowStockCount), "僅少警告", Color(0xFFFFC107)))
        } else {
            emptyList()
        },
    )
}

@Composable
fun OutsourcingStockToolbar(
    uiState: OutsourcingStockUiState,
    onTabSelect: (Int) -> Unit,
    onSupplierChange: (String) -> Unit,
    onProductCodeChange: (String) -> Unit,
    onStockStatusChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
) {
    OutsourcingGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutsourcingTabStrip(
                tabs = listOf(
                    "メッキ品在庫 (${uiState.platingStockCount})",
                    "溶接品在庫 (${uiState.weldingStockCount})",
                ),
                selectedIndex = if (uiState.activeTab == OutsourcingStockTab.Plating) 0 else 1,
                onSelect = onTabSelect,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutsourcingDropdownField(
                    label = "外注先",
                    value = uiState.supplierId,
                    options = uiState.supplierOptions.map { it.id.toString() to it.label },
                    onSelect = onSupplierChange,
                    modifier = Modifier.weight(1.2f),
                )
                OutsourcingTextField(
                    label = "品番",
                    value = uiState.productCode,
                    onValueChange = onProductCodeChange,
                    modifier = Modifier.weight(1f),
                )
                OutsourcingDropdownField(
                    label = "在庫状況",
                    value = uiState.stockStatus,
                    options = listOf("normal" to "正常", "low" to "僅少", "empty" to "なし"),
                    onSelect = onStockStatusChange,
                    modifier = Modifier.weight(1f),
                )
            }
            OutsourcingActionBar(
                leftContent = {
                    OutsourcingActionButton("検索", Icons.Default.Search, enabled = !uiState.isLoading, onClick = onSearch)
                    OutsourcingActionButton("更新", Icons.Default.Refresh, containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569), enabled = !uiState.isLoading, onClick = onRefresh)
                    OutsourcingActionButton("Excel", Icons.Default.Download, containerColor = Color(0xFFFFF7E6), contentColor = Color(0xFFD97706), onClick = onExport)
                },
            )
        }
    }
}

@Composable
fun OutsourcingStockSummaryStrip(uiState: OutsourcingStockUiState) {
    OutsourcingSummaryStrip(
        cards = listOf(
            Triple(Icons.Default.Inventory2, formatOutsourcingNumber(uiState.totalStockQty), "総在庫数量"),
            Triple(Icons.Default.Download, formatOutsourcingNumber(uiState.totalReceivedQty), "今月入庫数"),
            Triple(Icons.Default.Upload, formatOutsourcingNumber(uiState.totalUsedQty), "今月出庫数"),
            Triple(Icons.Default.CalendarMonth, formatOutsourcingNumber(uiState.totalPendingQty), "入庫予定数"),
        ),
    )
}

@Composable
fun OutsourcingStockTable(
    uiState: OutsourcingStockUiState,
    onDetail: (OutsourcingStockUiItem) -> Unit,
    onHistory: (OutsourcingStockUiItem) -> Unit,
) {
    OutsourcingHorizontalTable {
        Column {
            Row(modifier = Modifier.background(Color(0xFFF0F2F5))) {
                OutsourcingTableHeaderCell("製品CD", Modifier.width(72.dp))
                OutsourcingTableHeaderCell("品名", Modifier.width(120.dp))
                OutsourcingTableHeaderCell("外注先", Modifier.width(130.dp))
                if (uiState.activeTab == OutsourcingStockTab.Plating) {
                    OutsourcingTableHeaderCell("メッキ種類", Modifier.width(88.dp))
                } else {
                    OutsourcingTableHeaderCell("溶接種類", Modifier.width(88.dp))
                }
                OutsourcingTableHeaderCell("発注累計", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("入庫累計", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("出庫累計", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("現在庫", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("入庫予定", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("状況", Modifier.width(56.dp))
                OutsourcingTableHeaderCell("最終入庫日", Modifier.width(84.dp))
                OutsourcingTableHeaderCell("操作", Modifier.width(64.dp))
            }
            OutsourcingTableLoading(uiState.isLoading)
            if (!uiState.isLoading && uiState.currentItems.isEmpty()) {
                PurchaseEmptyHint("在庫データがありません")
            }
            uiState.currentItems.forEach { item ->
                val status = resolveOutsourcingStockStatus(item.stockQty, item.minStock)
                val rowBg = when (status) {
                    com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus.Empty -> Color(0xFFFEF2F2)
                    com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus.Low -> Color(0xFFFFFBEB)
                    else -> Color.Transparent
                }
                val stockColor = when (status) {
                    com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus.Empty -> Color(0xFFDC2626)
                    com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus.Low -> Color(0xFFD97706)
                    else -> Color(0xFF16A34A)
                }
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(modifier = Modifier.background(rowBg)) {
                    OutsourcingTableCell(item.productCode, Modifier.width(72.dp), onClick = { onDetail(item) })
                    OutsourcingTableCell(item.productName, Modifier.width(120.dp))
                    OutsourcingTableCell(item.supplier, Modifier.width(130.dp))
                    OutsourcingTableCell(
                        if (uiState.activeTab == OutsourcingStockTab.Plating) item.platingType.orEmpty() else item.weldingType.orEmpty(),
                        Modifier.width(88.dp),
                    )
                    OutsourcingTableCell(formatOutsourcingNumber(item.orderedQty), Modifier.width(72.dp), alignEnd = true)
                    OutsourcingTableCell(formatOutsourcingNumber(item.receivedQty), Modifier.width(72.dp), alignEnd = true)
                    OutsourcingTableCell(formatOutsourcingNumber(item.usedQty), Modifier.width(72.dp), alignEnd = true)
                    OutsourcingTableCell(formatOutsourcingNumber(item.stockQty), Modifier.width(72.dp), alignEnd = true, color = stockColor, fontWeight = FontWeight.SemiBold)
                    OutsourcingTableCell(formatOutsourcingNumber(item.pendingQty), Modifier.width(72.dp), alignEnd = true, color = Color(0xFF2563EB))
                    Row(Modifier.width(56.dp).padding(horizontal = 6.dp, vertical = 6.dp)) {
                        OutsourcingStatusChip(status)
                    }
                    OutsourcingTableCell(item.lastReceiveDate, Modifier.width(84.dp))
                    Row(Modifier.width(64.dp).padding(4.dp)) {
                        TextButton(onClick = { onHistory(item) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)) {
                            Text("履歴", fontSize = 10.sp, color = Color(0xFF2563EB))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OutsourcingStockDetailDialog(item: OutsourcingStockUiItem, isPlating: Boolean, onDismiss: () -> Unit) {
    val status = resolveOutsourcingStockStatus(item.stockQty, item.minStock)
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth(0.95f)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("在庫詳細", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                HorizontalDivider()
                DetailRow("品番", item.productCode)
                DetailRow("品名", item.productName)
                DetailRow("外注先", item.supplier)
                DetailRow(if (isPlating) "メッキ種類" else "溶接種類", if (isPlating) item.platingType.orEmpty() else item.weldingType.orEmpty())
                DetailRow("発注累計", formatOutsourcingNumber(item.orderedQty))
                DetailRow("入庫累計", formatOutsourcingNumber(item.receivedQty))
                DetailRow("出庫累計", formatOutsourcingNumber(item.usedQty))
                DetailRow("現在庫", formatOutsourcingNumber(item.stockQty))
                DetailRow("入庫予定", formatOutsourcingNumber(item.pendingQty))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("状況", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(88.dp))
                    OutsourcingStatusChip(status)
                }
                DetailRow("最終入庫日", item.lastReceiveDate)
                DetailRow("最終出庫日", item.lastIssueDate ?: "-")
                TextButton(onClick = onDismiss, modifier = Modifier.align(androidx.compose.ui.Alignment.End)) {
                    Text("閉じる")
                }
            }
        }
    }
}

@Composable
fun OutsourcingStockHistoryDialog(
    title: String,
    items: List<OutsourcingStockHistoryUiItem>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth(0.98f)) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                OutsourcingTableLoading(isLoading)
                Column(modifier = Modifier.height(320.dp).verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                        OutsourcingTableHeaderCell("日付", Modifier.width(88.dp))
                        OutsourcingTableHeaderCell("種別", Modifier.width(64.dp))
                        OutsourcingTableHeaderCell("関連番号", Modifier.width(110.dp))
                        OutsourcingTableHeaderCell("数量", Modifier.width(72.dp), alignEnd = true)
                        OutsourcingTableHeaderCell("在庫残", Modifier.width(72.dp), alignEnd = true)
                        OutsourcingTableHeaderCell("担当者", Modifier.width(72.dp))
                        OutsourcingTableHeaderCell("備考", Modifier.width(120.dp))
                    }
                    if (!isLoading && items.isEmpty()) PurchaseEmptyHint("履歴データがありません")
                    items.forEach { row ->
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Row {
                            OutsourcingTableCell(row.date, Modifier.width(88.dp))
                            Row(Modifier.width(64.dp).padding(6.dp)) {
                                OutsourcingMaterialHistoryTypeChip(row.type)
                            }
                            OutsourcingTableCell(row.orderNo, Modifier.width(110.dp))
                            val qtyColor = if (row.type == "receive") Color(0xFF16A34A) else Color(0xFFD97706)
                            val prefix = if (row.type == "receive") "+" else "-"
                            OutsourcingTableCell("$prefix${formatOutsourcingNumber(row.quantity)}", Modifier.width(72.dp), alignEnd = true, color = qtyColor, fontWeight = FontWeight.SemiBold)
                            OutsourcingTableCell(formatOutsourcingNumber(row.stockAfter), Modifier.width(72.dp), alignEnd = true)
                            OutsourcingTableCell(row.operator, Modifier.width(72.dp))
                            OutsourcingTableCell(row.remarks, Modifier.width(120.dp))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(androidx.compose.ui.Alignment.End)) {
                    Text("閉じる")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(88.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
