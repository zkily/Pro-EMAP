package com.example.smart_emap.ui.erp.purchase.outsourcing.plating

import androidx.compose.runtime.Composable

@Composable
fun PlatingReceivingDialog(
    isEdit: Boolean,
    form: PlatingReceivingFormUi,
    pendingOrders: List<PlatingPendingOrderOption>,
    loading: Boolean,
    onOrderSelect: (String) -> Unit,
    onFormChange: (PlatingReceivingFormUi) -> Unit,
    onReceivingQtyChange: (String) -> Unit,
    onGoodQtyChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    PlatingReceivingDialogShell(
        title = if (isEdit) "受入編集" else "新規受入",
        loading = loading,
        confirmEnabled = form.orderId > 0 && form.inspector.isNotBlank(),
        confirmText = if (isEdit) "更新" else "登録",
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    ) {
        PlatingReceivingFormFields(
            form = form,
            isEdit = isEdit,
            pendingOrders = pendingOrders,
            onOrderSelect = onOrderSelect,
            onFormChange = onFormChange,
            onReceivingQtyChange = onReceivingQtyChange,
            onGoodQtyChange = onGoodQtyChange,
        )
    }
}
