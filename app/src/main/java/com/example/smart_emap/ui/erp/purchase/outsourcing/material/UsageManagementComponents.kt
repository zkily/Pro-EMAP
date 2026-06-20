package com.example.smart_emap.ui.erp.purchase.outsourcing.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStatCards
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableHeaderCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableLoading
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDropdownField
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTextField
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import kotlin.math.roundToInt

private val usageGradient = Brush.linearGradient(listOf(Color(0xFFFFC107), Color(0xFFFF9800)))

@Composable
fun UsageManagementHeroBar(uiState: UsageManagementUiState) {
    OutsourcingHeroHeader(
        title = "使用数管理",
        subtitle = "外注先での材料使用数を登録・管理します",
        icon = Icons.Default.Analytics,
        gradient = usageGradient,
        badge = uiState.filteredList.size.toString(),
        kpis = listOf(
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.monthlyUsageQty), "今月使用"),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.pendingReportCount), "未報告"),
        ),
    )
}

@Composable
fun UsageManagementFilters(
    uiState: UsageManagementUiState,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onSupplierChange: (String) -> Unit,
    onOrderNoChange: (String) -> Unit,
    onMaterialCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
) {
    OutsourcingFilterCard(accent = Color(0xFFFFC107)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutsourcingTextField("開始日", uiState.startDate, Modifier.weight(1f), onValueChange = onStartDateChange)
            OutsourcingTextField("終了日", uiState.endDate, Modifier.weight(1f), onValueChange = onEndDateChange)
            OutsourcingDropdownField("外注先", uiState.supplierId, uiState.supplierOptions.map { it.value.toString() to it.label }, onSupplierChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutsourcingTextField("注文番号", uiState.orderNo, Modifier.weight(1f), onValueChange = onOrderNoChange)
            OutsourcingTextField("材料コード", uiState.materialCode, Modifier.weight(1f), onValueChange = onMaterialCodeChange)
        }
        OutsourcingActionBar(
            leftContent = {
                OutsourcingActionButton("検索", Icons.Default.Search, containerColor = Color(0xFFFFC107), contentColor = Color(0xFF1E293B), enabled = !uiState.isLoading, onClick = onSearch)
                OutsourcingActionButton("リセット", Icons.Default.Refresh, containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569), onClick = onReset)
            },
        )
    }
}

@Composable
fun UsageManagementActionBar(uiState: UsageManagementUiState, onCreate: () -> Unit, onBatch: () -> Unit, onExport: () -> Unit) {
    OutsourcingActionBar(
        leftContent = {
            OutsourcingActionButton("使用報告", Icons.Default.Add, containerColor = Color(0xFF6366F1), onClick = onCreate)
            OutsourcingActionButton("一括登録", Icons.Default.Description, containerColor = Color(0xFF16A34A), enabled = uiState.selectedIds.isNotEmpty(), badge = uiState.selectedIds.size.takeIf { it > 0 }, onClick = onBatch)
            OutsourcingActionButton("Excel出力", Icons.Default.Download, containerColor = Color(0xFFFFF7E6), contentColor = Color(0xFFD97706), onClick = onExport)
        },
        rightContent = {
            OutsourcingInfoTag("今月使用重量: ${formatOutsourcingNumber(uiState.monthlyUsageWeight)} kg", Color(0xFFFFC107))
        },
    )
}

@Composable
fun UsageManagementTable(
    uiState: UsageManagementUiState,
    onToggleSelect: (Int) -> Unit,
    onDetail: (UsageManagementItem) -> Unit,
    onEdit: (UsageManagementItem) -> Unit,
    onDelete: (UsageManagementItem) -> Unit,
) {
    OutsourcingHorizontalTable {
        Column {
            Row(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                OutsourcingTableHeaderCell("", Modifier.width(36.dp))
                OutsourcingTableHeaderCell("報告番号", Modifier.width(108.dp))
                OutsourcingTableHeaderCell("使用日", Modifier.width(84.dp))
                OutsourcingTableHeaderCell("関連注文", Modifier.width(108.dp))
                OutsourcingTableHeaderCell("外注先", Modifier.width(120.dp))
                OutsourcingTableHeaderCell("材料コード", Modifier.width(96.dp))
                OutsourcingTableHeaderCell("材料名", Modifier.width(120.dp))
                OutsourcingTableHeaderCell("使用数量", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("単位", Modifier.width(48.dp))
                OutsourcingTableHeaderCell("使用重量", Modifier.width(80.dp), alignEnd = true)
                OutsourcingTableHeaderCell("製品数量", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("歩留率", Modifier.width(64.dp), alignEnd = true)
                OutsourcingTableHeaderCell("報告者", Modifier.width(72.dp))
                OutsourcingTableHeaderCell("操作", Modifier.width(88.dp))
            }
            OutsourcingTableLoading(uiState.isLoading)
            if (!uiState.isLoading && uiState.filteredList.isEmpty()) PurchaseEmptyHint("使用報告データがありません")
            uiState.filteredList.forEach { item ->
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.width(36.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.selectedIds.contains(item.id), onCheckedChange = { onToggleSelect(item.id) })
                    }
                    OutsourcingTableCell(item.usageNo, Modifier.width(108.dp), onClick = { onDetail(item) })
                    OutsourcingTableCell(item.usageDate, Modifier.width(84.dp))
                    OutsourcingTableCell(item.orderNo, Modifier.width(108.dp))
                    OutsourcingTableCell(item.supplier, Modifier.width(120.dp))
                    OutsourcingTableCell(item.materialCode, Modifier.width(96.dp))
                    OutsourcingTableCell(item.materialName, Modifier.width(120.dp))
                    OutsourcingTableCell(formatOutsourcingNumber(item.usageQty), Modifier.width(72.dp), alignEnd = true, color = Color(0xFFFFC107), fontWeight = FontWeight.SemiBold)
                    OutsourcingTableCell(item.unit, Modifier.width(48.dp))
                    OutsourcingTableCell(formatOutsourcingNumber(item.usageWeight), Modifier.width(80.dp), alignEnd = true, color = Color(0xFFE6A23C), fontWeight = FontWeight.SemiBold)
                    OutsourcingTableCell(formatOutsourcingNumber(item.productQty), Modifier.width(72.dp), alignEnd = true)
                    OutsourcingTableCell("${item.yieldRate}%", Modifier.width(64.dp), alignEnd = true, color = yieldColor(item.yieldRate), fontWeight = FontWeight.SemiBold)
                    OutsourcingTableCell(item.reporter, Modifier.width(72.dp))
                    Row(Modifier.width(88.dp).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(onClick = { onEdit(item) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)) {
                            androidx.compose.material3.Icon(Icons.Default.Edit, null, modifier = Modifier.width(14.dp), tint = Color(0xFF2563EB))
                        }
                        TextButton(onClick = { onDelete(item) }, contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)) {
                            androidx.compose.material3.Icon(Icons.Default.Delete, null, modifier = Modifier.width(14.dp), tint = Color(0xFFDC2626))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageManagementStatCards(uiState: UsageManagementUiState) {
    OutsourcingStatCards(
        cards = listOf(
            Triple(Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2))), formatOutsourcingNumber(uiState.totalUsageQty), "総使用数量"),
            Triple(Brush.linearGradient(listOf(Color(0xFF43E97B), Color(0xFF38F9D7))), "${uiState.avgYieldRate.roundToInt()}%", "平均歩留率"),
            Triple(usageGradient, formatOutsourcingNumber(uiState.totalProductQty), "総製品数量"),
        ),
    )
}

@Composable
fun UsageManagementFormDialog(
    uiState: UsageManagementUiState,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onSupplierChange: (String) -> Unit,
    onMaterialChange: (String) -> Unit,
    onFormChange: ((UsageFormState) -> UsageFormState) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth(0.98f)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (uiState.isEdit) "使用報告編集" else "新規使用報告", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFFF9800))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingDropdownField("外注先", uiState.form.supplierId, uiState.supplierOptions.map { it.value.toString() to it.label }, onSupplierChange, Modifier.weight(1f))
                    OutsourcingTextField("使用日", uiState.form.usageDate, Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(usageDate = v) } })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingDropdownField("関連注文", uiState.form.orderNo, uiState.orderOptions.map { it.orderNo to "${it.orderNo} (${it.productCode})" }, { v -> onFormChange { it.copy(orderNo = v) } }, Modifier.weight(1f))
                    OutsourcingTextField("報告者", uiState.form.reporter, Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(reporter = v) } })
                }
                Text("材料使用情報", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingDropdownField("材料", uiState.form.materialCode, uiState.availableMaterials.map { it.code to "${it.code} (在庫:${it.stockQty})" }, onMaterialChange, Modifier.weight(1f))
                    OutsourcingTextField("材料名", uiState.form.materialName, Modifier.weight(1f), readOnly = true, onValueChange = {})
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingTextField("使用可能数", uiState.form.availableQty.toString(), Modifier.weight(1f), readOnly = true, onValueChange = {})
                    OutsourcingTextField("使用数量", uiState.form.usageQty.toString(), Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(usageQty = v.toIntOrNull() ?: 0) } })
                    OutsourcingTextField("単位", uiState.form.unit, Modifier.weight(1f), readOnly = true, onValueChange = {})
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingTextField("単重(kg)", String.format("%.3f", uiState.form.unitWeight), Modifier.weight(1f), readOnly = true, onValueChange = {})
                    val weight = (uiState.form.usageQty * uiState.form.unitWeight)
                    OutsourcingTextField("使用重量(kg)", String.format("%.2f", weight), Modifier.weight(1f), readOnly = true, onValueChange = {})
                    OutsourcingTextField("製品数量", uiState.form.productQty.toString(), Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(productQty = v.toIntOrNull() ?: 0) } })
                }
                OutlinedTextField(
                    value = uiState.form.remarks,
                    onValueChange = { v -> onFormChange { it.copy(remarks = v) } },
                    label = { Text("備考", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) { Text("キャンセル") }
                    OutsourcingActionButton(if (uiState.isEdit) "更新" else "登録", enabled = !uiState.submitLoading, onClick = onSubmit)
                }
            }
        }
    }
}

@Composable
fun UsageManagementDetailDialog(item: UsageManagementItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("使用報告詳細", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                UsageDetailRow("報告番号", item.usageNo)
                UsageDetailRow("使用日", item.usageDate)
                UsageDetailRow("外注先", item.supplier)
                UsageDetailRow("関連注文", item.orderNo)
                UsageDetailRow("材料コード", item.materialCode)
                UsageDetailRow("材料名", item.materialName)
                UsageDetailRow("使用数量", "${formatOutsourcingNumber(item.usageQty)} ${item.unit}")
                UsageDetailRow("使用重量", "${formatOutsourcingNumber(item.usageWeight)} kg")
                UsageDetailRow("製品数量", formatOutsourcingNumber(item.productQty))
                UsageDetailRow("歩留率", "${item.yieldRate}%")
                UsageDetailRow("報告者", item.reporter)
                UsageDetailRow("備考", item.remarks.ifBlank { "-" })
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("閉じる") }
            }
        }
    }
}

@Composable
private fun UsageDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(88.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private fun yieldColor(rate: Double): Color = when {
    rate >= 95 -> Color(0xFF67C23A)
    rate >= 90 -> Color(0xFFE6A23C)
    else -> Color(0xFFF56C6C)
}
