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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
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
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingIssueStatusChip
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingKpiItem
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableHeaderCell
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTableLoading
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingDropdownField
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTextField
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber

private val issueGradient = Brush.linearGradient(listOf(Color(0xFFF093FB), Color(0xFFF5576C)))

@Composable
fun MaterialIssueHeroBar(uiState: MaterialIssueUiState) {
    OutsourcingHeroHeader(
        title = "支給材料出庫",
        subtitle = "外注業者への材料支給・出庫管理を行います",
        icon = Icons.Default.Upload,
        gradient = issueGradient,
        badge = uiState.filteredList.size.toString(),
        kpis = listOf(
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.todayIssueCount), "本日出庫"),
            OutsourcingKpiItem(formatOutsourcingNumber(uiState.pendingCount), "準備中"),
        ),
    )
}

@Composable
fun MaterialIssueFilters(
    uiState: MaterialIssueUiState,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onSupplierChange: (String) -> Unit,
    onOrderNoChange: (String) -> Unit,
    onMaterialCodeChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit,
) {
    OutsourcingFilterCard(accent = Color(0xFFF093FB)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutsourcingTextField("開始日", uiState.startDate, Modifier.weight(1f), onValueChange = onStartDateChange)
            OutsourcingTextField("終了日", uiState.endDate, Modifier.weight(1f), onValueChange = onEndDateChange)
            OutsourcingDropdownField("外注先", uiState.supplierId, uiState.supplierOptions.map { it.value.toString() to it.label }, onSupplierChange, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            OutsourcingTextField("注文番号", uiState.orderNo, Modifier.weight(1f), onValueChange = onOrderNoChange)
            OutsourcingTextField("材料コード", uiState.materialCode, Modifier.weight(1f), onValueChange = onMaterialCodeChange)
            OutsourcingDropdownField("状態", uiState.status, listOf("preparing" to "準備中", "issued" to "出庫済", "returned" to "返却済"), onStatusChange, Modifier.weight(1f))
        }
        OutsourcingActionBar(
            leftContent = {
                OutsourcingActionButton("検索", Icons.Default.Search, containerColor = Color(0xFFF093FB), enabled = !uiState.isLoading, onClick = onSearch)
                OutsourcingActionButton("リセット", Icons.Default.Refresh, containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF475569), onClick = onReset)
            },
        )
    }
}

@Composable
fun MaterialIssueActionBar(
    uiState: MaterialIssueUiState,
    onCreate: () -> Unit,
    onBatchIssue: () -> Unit,
    onExport: () -> Unit,
    onPrint: () -> Unit,
) {
    OutsourcingActionBar(
        leftContent = {
            OutsourcingActionButton("新規支給", Icons.Default.Add, containerColor = Color(0xFF6366F1), onClick = onCreate)
            OutsourcingActionButton("一括出庫", Icons.Default.Upload, containerColor = Color(0xFF16A34A), enabled = uiState.selectedIds.isNotEmpty(), badge = uiState.selectedIds.size.takeIf { it > 0 }, onClick = onBatchIssue)
            OutsourcingActionButton("Excel出力", Icons.Default.Download, containerColor = Color(0xFFFFF7E6), contentColor = Color(0xFFD97706), onClick = onExport)
            OutsourcingActionButton("支給伝票印刷", Icons.Default.Print, containerColor = Color(0xFFE0F2FE), contentColor = Color(0xFF0284C7), enabled = uiState.selectedIds.isNotEmpty(), onClick = onPrint)
        },
        rightContent = {
            OutsourcingInfoTag("本日支給: ${formatOutsourcingNumber(uiState.todayQuantity)} kg", Color(0xFFF093FB))
        },
    )
}

@Composable
fun MaterialIssueTable(
    uiState: MaterialIssueUiState,
    onToggleSelect: (Int) -> Unit,
    onDetail: (MaterialIssueItem) -> Unit,
    onIssue: (MaterialIssueItem) -> Unit,
    onEdit: (MaterialIssueItem) -> Unit,
    onDelete: (MaterialIssueItem) -> Unit,
) {
    OutsourcingHorizontalTable {
        Column {
            Row(modifier = Modifier.background(Color(0xFFF5F7FA))) {
                OutsourcingTableHeaderCell("", Modifier.width(36.dp))
                OutsourcingTableHeaderCell("支給番号", Modifier.width(108.dp))
                OutsourcingTableHeaderCell("出庫日", Modifier.width(84.dp))
                OutsourcingTableHeaderCell("関連注文", Modifier.width(108.dp))
                OutsourcingTableHeaderCell("外注先", Modifier.width(120.dp))
                OutsourcingTableHeaderCell("材料コード", Modifier.width(96.dp))
                OutsourcingTableHeaderCell("材料名", Modifier.width(120.dp))
                OutsourcingTableHeaderCell("規格", Modifier.width(96.dp))
                OutsourcingTableHeaderCell("支給数量", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("単位", Modifier.width(48.dp))
                OutsourcingTableHeaderCell("単重", Modifier.width(64.dp), alignEnd = true)
                OutsourcingTableHeaderCell("総重量", Modifier.width(72.dp), alignEnd = true)
                OutsourcingTableHeaderCell("状態", Modifier.width(64.dp))
                OutsourcingTableHeaderCell("担当者", Modifier.width(72.dp))
                OutsourcingTableHeaderCell("操作", Modifier.width(120.dp))
            }
            OutsourcingTableLoading(uiState.isLoading)
            if (!uiState.isLoading && uiState.filteredList.isEmpty()) PurchaseEmptyHint("支給出庫データがありません")
            uiState.filteredList.forEach { item ->
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.width(36.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = uiState.selectedIds.contains(item.id), onCheckedChange = { onToggleSelect(item.id) })
                    }
                    OutsourcingTableCell(item.issueNo, Modifier.width(108.dp), onClick = { onDetail(item) })
                    OutsourcingTableCell(item.issueDate, Modifier.width(84.dp))
                    OutsourcingTableCell(item.orderNo.ifBlank { "-" }, Modifier.width(108.dp))
                    OutsourcingTableCell(item.supplier, Modifier.width(120.dp))
                    OutsourcingTableCell(item.materialCode, Modifier.width(96.dp))
                    OutsourcingTableCell(item.materialName, Modifier.width(120.dp))
                    OutsourcingTableCell(item.spec, Modifier.width(96.dp))
                    OutsourcingTableCell(formatOutsourcingNumber(item.quantity), Modifier.width(72.dp), alignEnd = true, color = Color(0xFFF093FB), fontWeight = FontWeight.SemiBold)
                    OutsourcingTableCell(item.unit, Modifier.width(48.dp))
                    OutsourcingTableCell(String.format("%.3f", item.unitWeight), Modifier.width(64.dp), alignEnd = true)
                    OutsourcingTableCell(formatOutsourcingNumber(item.totalWeight), Modifier.width(72.dp), alignEnd = true, color = Color(0xFFE6A23C), fontWeight = FontWeight.SemiBold)
                    Row(Modifier.width(64.dp).padding(6.dp)) { OutsourcingIssueStatusChip(item.status) }
                    OutsourcingTableCell(item.operator, Modifier.width(72.dp))
                    Row(Modifier.width(120.dp).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(onClick = { onIssue(item) }, enabled = item.status == "preparing", contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)) {
                            Text("出庫", fontSize = 9.sp, color = if (item.status == "preparing") Color(0xFF16A34A) else Color(0xFF94A3B8))
                        }
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
fun MaterialIssueFormDialog(
    uiState: MaterialIssueUiState,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    onMaterialChange: (String) -> Unit,
    onFormChange: ((MaterialIssueFormState) -> MaterialIssueFormState) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth(0.98f)) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (uiState.isEdit) "支給編集" else "新規支給", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFF5576C))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingDropdownField("外注先", uiState.form.supplierId, uiState.supplierOptions.map { it.value.toString() to it.label }, { v -> onFormChange { it.copy(supplierId = v) } }, Modifier.weight(1f))
                    OutsourcingDropdownField("関連注文", uiState.form.orderNo, uiState.orderOptions.map { it.orderNo to "${it.orderNo} (${it.productCode})" }, { v -> onFormChange { it.copy(orderNo = v) } }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingTextField("出庫日", uiState.form.issueDate, Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(issueDate = v) } })
                    OutsourcingTextField("担当者", uiState.form.operator, Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(operator = v) } })
                }
                Text("材料情報", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF64748B))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingDropdownField("材料", uiState.form.materialCode, uiState.materialOptions.map { it.code to "${it.code} ${it.name}" }, onMaterialChange, Modifier.weight(1f))
                    OutsourcingTextField("材料名", uiState.form.materialName, Modifier.weight(1f), readOnly = true, onValueChange = {})
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingTextField("規格", uiState.form.spec, Modifier.weight(1f), readOnly = true, onValueChange = {})
                    OutsourcingTextField("在庫数", uiState.form.stockQty.toString(), Modifier.weight(1f), readOnly = true, onValueChange = {})
                    OutsourcingTextField("単重(kg)", String.format("%.3f", uiState.form.unitWeight), Modifier.weight(1f), readOnly = true, onValueChange = {})
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutsourcingTextField("支給数量", uiState.form.quantity.toString(), Modifier.weight(1f), onValueChange = { v -> onFormChange { it.copy(quantity = v.toIntOrNull() ?: 0) } })
                    OutsourcingTextField("単位", uiState.form.unit, Modifier.weight(1f), readOnly = true, onValueChange = {})
                    val total = uiState.form.quantity * uiState.form.unitWeight
                    OutsourcingTextField("総重量(kg)", String.format("%.2f", total), Modifier.weight(1f), readOnly = true, onValueChange = {})
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
fun MaterialIssueDetailDialog(item: MaterialIssueItem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        OutsourcingGlassCard(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("支給詳細", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                IssueDetailRow("支給番号", item.issueNo)
                IssueDetailRow("出庫日", item.issueDate)
                IssueDetailRow("外注先", item.supplier)
                IssueDetailRow("関連注文", item.orderNo.ifBlank { "-" })
                IssueDetailRow("材料コード", item.materialCode)
                IssueDetailRow("材料名", item.materialName)
                IssueDetailRow("規格", item.spec)
                IssueDetailRow("単位", item.unit)
                IssueDetailRow("支給数量", formatOutsourcingNumber(item.quantity))
                IssueDetailRow("単重(kg)", String.format("%.3f", item.unitWeight))
                IssueDetailRow("総重量(kg)", formatOutsourcingNumber(item.totalWeight))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("状態", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(88.dp))
                    OutsourcingIssueStatusChip(item.status)
                }
                IssueDetailRow("担当者", item.operator)
                IssueDetailRow("備考", item.remarks.ifBlank { "-" })
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("閉じる") }
            }
        }
    }
}

@Composable
private fun IssueDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.width(88.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}
