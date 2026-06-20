package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import androidx.compose.runtime.Composable

@Composable
fun WeldingReceivingDialog(
    isEdit: Boolean,
    form: WeldingReceivingFormUi,
    pendingOrders: List<WeldingPendingOrderOption>,
    loading: Boolean,
    onOrderSelect: (String) -> Unit,
    onFormChange: (WeldingReceivingFormUi) -> Unit,
    onReceivingQtyChange: (String) -> Unit,
    onGoodQtyChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    WeldingReceivingDialogShell(
        title = if (isEdit) "受入編集" else "新規受入",
        loading = loading,
        confirmEnabled = form.orderId > 0 && form.inspector.isNotBlank(),
        confirmText = if (isEdit) "更新" else "登録",
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    ) {
        WeldingReceivingFormFields(
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
