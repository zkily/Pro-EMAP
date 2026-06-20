package com.example.smart_emap.ui.erp.purchase.outsourcing.material

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingInfoTag
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingKpiItem
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingMaterialHistoryTypeChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableHeaderCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableLoading
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDropdownField
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTextField
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.resolveOutsourcingStockStatus

private val greenGradient = Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7)))

@Composable
fun SuppliedMaterialStockHeroBar(uiState: SuppliedMaterialStockUiState) {
    OutsourcingHeroHeader(
        title = "支給材料在庫管理",
        subtitle = "外注先に支給した材料の在庫状況を管理します",
        icon = Icons.Default.Inventory2,
        gradient = greenGradient,
        kpis = listOf(
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.supplierCount), "外注先"),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.materialCount), "材料種"),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.lowStockCount), "在庫僅少", Color(0xFFFF9800)),
        ),
    )
}

@Composable
fun SuppliedMaterialStockFilters(
    uiState: SuppliedMaterialStockUiState,
    onSupplierChange: (String) -> Unit,
    onMaterialCodeChange: (String) -> Unit,
    onStockStatusChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
) {
    OutsourcingFilterCard(accent = Color(0xFF43E97B)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutsourcingDropdownField("外注先", uiState.supplierId, uiState.supplierOptions.map { it.value.toString() to it.label }, onSupplierChange, Modifier.weight(1f))
            OutsourcingTextField("材料コード", uiState.materialCode, Modifier.weight(1f), onValueChange = onMaterialCodeChange)
            OutsourcingDropdownField("在庫状況", uiState.stockStatus, listOf("normal" to "正常", "low" to "僅少", "empty" to "なし"), onStockStatusChange, Modifier.weight(1f))
        }
        OutsourcingActionBar(
            leftContent = {
                OutsourcingActionButton("検索", Icons.Default.Search, containerColor = Color(0xFF43E97B), enabled = !uiState.isLoading, onClick = onSearch)
                OutsourcingActionButton("リセット", Icons.Default.Refresh, containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569), onClick = onReset)
            },
        )
    }
}

@Composable
fun SuppliedMaterialStockActionBar(
    uiState: SuppliedMaterialStockUiState,
    onExport: () -> Unit,
    onRefresh: () -> Unit,
) {
    OutsourcingActionBar(
        leftContent = {
            OutsourcingActionButton("Excel出力", Icons.Default.Download, containerColor = Color(0xFFFFF7E6), contentColor = Color(0xFFD97706), onClick = onExport)
            OutsourcingActionButton("在庫更新", Icons.Default.Refresh, containerColor = Color(0xFFE0F2FE), contentColor = Color(0xFF0284C7), onClick = onRefresh)
        },
        rightContent = {
            if (uiState.lowStockCount > 0) {
                OutsourcingInfoTag("${uiState.lowStockCount}件の在庫が僅少です", Color(0xFFE6A23C))
            }
        },
    )
}

@Composable
fun SuppliedMaterialSupplierCards(
    suppliers: List<SuppliedMaterialSupplierStock>,
    isLoading: Boolean,
    onHistory: (SuppliedMaterialSupplierStock, SuppliedMaterialStockItem) -> Unit,
) {
    OutsourcingTableLoading(isLoading)
    if (!isLoading && suppliers.isEmpty()) PurchaseEmptyHint("該当する外注先データがありません")
    suppliers.forEach { supplier ->
        val hasWarning = supplier.lowStockItems > 0
        OutsourcingGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasWarning) Modifier.border(2.dp, Color(0xFFE6A23C), androidx.compose.foundation.shape.RoundedCornerShape(10.dp)) else Modifier),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF43E97B))
                        Text(supplier.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), color = Color(0xFFEEF2FF)) {
                            Text("${supplier.totalItems}種", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, color = Color(0xFF6366F1))
                        }
                        if (hasWarning) {
                            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), color = Color(0xFFFEF3C7)) {
                                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(Icons.Default.Warning, null, tint = Color(0xFFD97706), modifier = Modifier.padding(end = 2.dp).width(12.dp))
                                    Text("${supplier.lowStockItems}", fontSize = 10.sp, color = Color(0xFFD97706))
                                }
                            }
                        }
                    }
                }
                OutsourcingHorizontalTable {
                    Column {
                        Row(modifier = Modifier.background(Color(0xFFF8F9FA))) {
                            OutsourcingTableHeaderCell("材料コード", Modifier.width(96.dp))
                            OutsourcingTableHeaderCell("材料名", Modifier.width(120.dp))
                            OutsourcingTableHeaderCell("規格", Modifier.width(96.dp))
                            OutsourcingTableHeaderCell("支給累計", Modifier.width(72.dp), alignEnd = true)
                            OutsourcingTableHeaderCell("使用累計", Modifier.width(72.dp), alignEnd = true)
                            OutsourcingTableHeaderCell("現在庫", Modifier.width(72.dp), alignEnd = true)
                            OutsourcingTableHeaderCell("単位", Modifier.width(48.dp))
                            OutsourcingTableHeaderCell("状況", Modifier.width(56.dp))
                            OutsourcingTableHeaderCell("操作", Modifier.width(56.dp))
                        }
                        supplier.materials.forEach { material ->
                            val status = resolveOutsourcingStockStatus(material.stockQty, material.minStock)
                            val rowBg = when (status) {
                                OutsourcingStockStatus.Empty -> Color(0xFFFDECEA)
                                OutsourcingStockStatus.Low -> Color(0xFFFEF9E7)
                                else -> Color.Transparent
                            }
                            val stockColor = when (status) {
                                OutsourcingStockStatus.Empty -> Color(0xFFF56C6C)
                                OutsourcingStockStatus.Low -> Color(0xFFE6A23C)
                                else -> Color(0xFF67C23A)
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Row(modifier = Modifier.background(rowBg)) {
                                OutsourcingTableCell(material.materialCode, Modifier.width(96.dp))
                                OutsourcingTableCell(material.materialName, Modifier.width(120.dp))
                                OutsourcingTableCell(material.spec, Modifier.width(96.dp))
                                OutsourcingTableCell(formatOutsourcingNumber(material.issuedQty), Modifier.width(72.dp), alignEnd = true)
                                OutsourcingTableCell(formatOutsourcingNumber(material.usedQty), Modifier.width(72.dp), alignEnd = true)
                                OutsourcingTableCell(formatOutsourcingNumber(material.stockQty), Modifier.width(72.dp), alignEnd = true, color = stockColor, fontWeight = FontWeight.SemiBold)
                                OutsourcingTableCell(material.unit, Modifier.width(48.dp))
                                Row(Modifier.width(56.dp).padding(6.dp)) { OutsourcingStatusChip(status) }
                                Row(Modifier.width(56.dp).padding(4.dp)) {
                                    TextButton(onClick = { onHistory(supplier, material) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)) {
                                        Text("履歴", fontSize = 10.sp, color = Color(0xFF2563EB))
                                    }
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("総支給重量: ${formatOutsourcingNumber(supplier.totalIssuedWeight)} kg", fontSize = 11.sp, color = Color(0xFF606266))
                    Text("現在庫重量: ${formatOutsourcingNumber(supplier.currentStockWeight)} kg", fontSize = 11.sp, color = Color(0xFF606266))
                }
            }
        }
    }
}

@Composable
fun SuppliedMaterialHistoryDialog(title: String, items: List<SuppliedMaterialHistoryItem>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth(0.98f)) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                        OutsourcingTableHeaderCell("日付", Modifier.width(88.dp))
                        OutsourcingTableHeaderCell("種別", Modifier.width(64.dp))
                        OutsourcingTableHeaderCell("関連注文", Modifier.width(110.dp))
                        OutsourcingTableHeaderCell("数量", Modifier.width(72.dp), alignEnd = true)
                        OutsourcingTableHeaderCell("在庫残", Modifier.width(72.dp), alignEnd = true)
                        OutsourcingTableHeaderCell("担当者", Modifier.width(72.dp))
                        OutsourcingTableHeaderCell("備考", Modifier.width(120.dp))
                    }
                    items.forEach { row ->
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Row {
                            OutsourcingTableCell(row.date, Modifier.width(88.dp))
                            Row(Modifier.width(64.dp).padding(6.dp)) { OutsourcingMaterialHistoryTypeChip(row.type) }
                            OutsourcingTableCell(row.orderNo, Modifier.width(110.dp))
                            val color = if (row.type == "issue") Color(0xFF67C23A) else Color(0xFFE6A23C)
                            val prefix = if (row.type == "issue") "+" else "-"
                            OutsourcingTableCell("$prefix${formatOutsourcingNumber(row.quantity)}", Modifier.width(72.dp), alignEnd = true, color = color, fontWeight = FontWeight.SemiBold)
                            OutsourcingTableCell(formatOutsourcingNumber(row.stockAfter), Modifier.width(72.dp), alignEnd = true)
                            OutsourcingTableCell(row.operator, Modifier.width(72.dp))
                            OutsourcingTableCell(row.remarks, Modifier.width(120.dp))
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("閉じる") }
            }
        }
    }
}
