package com.example.smart_emap.ui.erp.purchase.outsourcing.plating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
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
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingActionButton
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingGlassCard
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingTextField

private val DialogAccent = Color(0xFF667EEA)

@Composable
fun PlatingOrderCreateDialog(
    suppliers: List<OutsourcingSupplierDto>,
    supplierCd: String,
    orderDate: String,
    deliveryDate: String,
    products: List<PlatingOrderProductRowUi>,
    loading: Boolean,
    productsLoading: Boolean,
    onSupplierChange: (String) -> Unit,
    onOrderDateChange: (String) -> Unit,
    onDeliveryDateChange: (String) -> Unit,
    onLoadProducts: () -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val supplierOptions = suppliers.mapNotNull { s ->
        s.supplierCd?.let { cd -> cd to (s.supplierName ?: cd) }
    }
    PlatingOrderDialogShell(
        title = "新規注文",
        loading = loading,
        confirmEnabled = products.isNotEmpty(),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatingDialogFormLabel("外注先")
            PlatingDialogDropdown(
                value = supplierCd,
                options = supplierOptions,
                placeholder = "外注先を選択",
                modifier = Modifier.weight(1f),
                onSelect = onSupplierChange,
            )
            PlatingDialogLoadButton(loading = productsLoading, onClick = onLoadProducts)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlatingDialogDateField(
                label = "注文日",
                value = orderDate,
                modifier = Modifier.weight(1f),
                onChange = onOrderDateChange,
            )
            Column(modifier = Modifier.weight(1f)) {
                PlatingDialogDateField(
                    label = "納期",
                    value = deliveryDate,
                    onChange = onDeliveryDateChange,
                )
                if (products.isNotEmpty()) {
                    Text(
                        "納期が正しいかご確認ください",
                        fontSize = 10.sp,
                        color = Color(0xFFE6A23C),
                        modifier = Modifier.padding(start = 62.dp, top = 4.dp),
                    )
                }
            }
        }
        when {
            productsLoading -> PlatingDialogPlaceholder(loading = true, message = "")
            products.isEmpty() && supplierCd.isBlank() -> PlatingDialogPlaceholder(
                loading = false,
                message = "外注先を選択し、製品一覧を読み込んでください",
            )
            products.isEmpty() -> PlatingDialogPlaceholder(loading = false, message = "製品データがありません")
            else -> PlatingCreateProductTable(products = products, onQuantityChange = onQuantityChange)
        }
    }
}

@Composable
fun PlatingOrderBatchCreateDialog(
    suppliers: List<OutsourcingSupplierDto>,
    supplierCd: String,
    productCd: String,
    startDate: String,
    endDate: String,
    productOptions: List<Pair<String, String>>,
    rows: List<PlatingBatchOrderRowUi>,
    loading: Boolean,
    productsLoading: Boolean,
    onSupplierChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onThisMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onLoad: () -> Unit,
    onQuantityChange: (String, String) -> Unit,
    onDeliveryDateChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val supplierOptions = suppliers.mapNotNull { s ->
        s.supplierCd?.let { cd -> cd to (s.supplierName ?: cd) }
    }
    PlatingOrderDialogShell(
        title = "新規一括注文",
        loading = loading,
        confirmEnabled = rows.isNotEmpty(),
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatingDialogFormLabel("外注先")
            PlatingDialogDropdown(
                value = supplierCd,
                options = supplierOptions,
                placeholder = "外注先を選択",
                modifier = Modifier.weight(1f),
                onSelect = onSupplierChange,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatingDialogFormLabel("製品")
            PlatingDialogDropdown(
                value = productCd,
                options = productOptions,
                placeholder = "製品を選択",
                enabled = supplierCd.isNotBlank(),
                modifier = Modifier.weight(1f),
                onSelect = onProductChange,
            )
            PlatingDialogLoadButton(loading = productsLoading, onClick = onLoad)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlatingDialogFormLabel("期間")
            PlatingDialogDateField(label = "", value = startDate, modifier = Modifier.weight(1f), onChange = onStartDateChange)
            Text("〜", fontSize = 12.sp, color = Color(0xFF909399))
            PlatingDialogDateField(label = "", value = endDate, modifier = Modifier.weight(1f), onChange = onEndDateChange)
            PlatingDialogMonthShortcuts(onPrevMonth = onPrevMonth, onThisMonth = onThisMonth, onNextMonth = onNextMonth)
        }
        when {
            productsLoading -> PlatingDialogPlaceholder(loading = true, message = "")
            rows.isEmpty() && (supplierCd.isBlank() || productCd.isBlank() || startDate.isBlank() || endDate.isBlank()) ->
                PlatingDialogPlaceholder(
                    loading = false,
                    message = "外注先、製品、期間を選択し、読込ボタンをクリックしてください",
                )
            rows.isEmpty() -> PlatingDialogPlaceholder(loading = false, message = "データがありません")
            else -> PlatingBatchOrderTable(rows = rows, onQuantityChange = onQuantityChange, onDeliveryDateChange = onDeliveryDateChange)
        }
    }
}

@Composable
fun PlatingOrderEditDialog(
    form: PlatingOrderEditFormUi,
    loading: Boolean,
    onFormChange: (PlatingOrderEditFormUi) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        OutsourcingGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("注文編集", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(form.orderNo, fontSize = 12.sp, color = DialogAccent)
                OutsourcingTextField("注文日", form.orderDate) { onFormChange(form.copy(orderDate = it)) }
                OutsourcingTextField("製品CD", form.productCd) { onFormChange(form.copy(productCd = it)) }
                OutsourcingTextField("製品名", form.productName) { onFormChange(form.copy(productName = it)) }
                OutsourcingTextField("数量", form.quantityText) { onFormChange(form.copy(quantityText = it)) }
                OutsourcingTextField("単価", form.unitPriceText) { onFormChange(form.copy(unitPriceText = it)) }
                OutsourcingTextField("納期", form.deliveryDate) { onFormChange(form.copy(deliveryDate = it)) }
                OutsourcingTextField("備考", form.remarks) { onFormChange(form.copy(remarks = it)) }
                Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                    OutsourcingActionButton(text = "更新", enabled = !loading, onClick = onConfirm)
                }
            }
        }
    }
}

@Composable
fun PlatingOrderPrintDialog(
    form: PlatingOrderPrintFormUi,
    orderCount: Int,
    loading: Boolean,
    onFormChange: (PlatingOrderPrintFormUi) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!loading) onDismiss() }) {
        OutsourcingGlassCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("注文書印刷確認", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    OutsourcingActionButton(
                        text = "印刷実行",
                        icon = Icons.Default.Print,
                        enabled = !loading,
                        onClick = onConfirm,
                    )
                }
                Text("対象: ${orderCount}件", fontSize = 11.sp, color = Color(0xFF64748B))
                OutsourcingTextField("受注先会社名", form.recipientCompany) {
                    onFormChange(form.copy(recipientCompany = it))
                }
                OutsourcingTextField("承認者", form.approver) { onFormChange(form.copy(approver = it)) }
                OutsourcingTextField("発行者", form.issuer) { onFormChange(form.copy(issuer = it)) }
                OutsourcingTextField("備考1", form.note1) { onFormChange(form.copy(note1 = it)) }
                OutsourcingTextField("備考2", form.note2) { onFormChange(form.copy(note2 = it)) }
                OutsourcingTextField("備考3", form.note3) { onFormChange(form.copy(note3 = it)) }
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss, enabled = !loading) { Text("キャンセル") }
                }
            }
        }
    }
}
