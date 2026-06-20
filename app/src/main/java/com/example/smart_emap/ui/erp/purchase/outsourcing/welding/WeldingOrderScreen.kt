package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingConfirmDialog
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingOrderPageBackground
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun WeldingOrderScreen(viewModel: WeldingOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "外注溶接注文書",
            layout = PrintPageLayout.A4_PORTRAIT_SINGLE,
        )
        if (!opened) {
            snackbarHostState.showSnackbar("印刷を開始できませんでした")
        }
        viewModel.clearPendingPrintHtml()
    }

    if (uiState.showCreateDialog) {
        WeldingOrderCreateDialog(
            suppliers = uiState.suppliers,
            supplierCd = uiState.createSupplierCd,
            orderDate = uiState.createOrderDate,
            deliveryDate = uiState.createDeliveryDate,
            products = uiState.createProducts,
            loading = uiState.actionLoading,
            productsLoading = uiState.createProductsLoading,
            onSupplierChange = viewModel::setCreateSupplierCd,
            onOrderDateChange = viewModel::setCreateOrderDate,
            onDeliveryDateChange = viewModel::setCreateDeliveryDate,
            onLoadProducts = viewModel::loadCreateProducts,
            onQuantityChange = viewModel::setCreateProductQuantity,
            onConfirm = viewModel::confirmCreate,
            onDismiss = viewModel::dismissCreateDialog,
        )
    }

    if (uiState.showBatchCreateDialog) {
        WeldingOrderBatchCreateDialog(
            suppliers = uiState.suppliers,
            supplierCd = uiState.batchSupplierCd,
            productCd = uiState.batchProductCd,
            startDate = uiState.batchStartDate,
            endDate = uiState.batchEndDate,
            productOptions = uiState.batchProductOptions,
            rows = uiState.batchOrderRows,
            loading = uiState.actionLoading,
            productsLoading = uiState.batchProductsLoading,
            onSupplierChange = viewModel::setBatchSupplierCd,
            onProductChange = viewModel::setBatchProductCd,
            onStartDateChange = viewModel::setBatchStartDate,
            onEndDateChange = viewModel::setBatchEndDate,
            onPrevMonth = viewModel::setBatchDateRangePrevMonth,
            onThisMonth = viewModel::setBatchDateRangeThisMonth,
            onNextMonth = viewModel::setBatchDateRangeNextMonth,
            onLoad = viewModel::loadBatchOrderRows,
            onQuantityChange = viewModel::setBatchRowQuantity,
            onDeliveryDateChange = viewModel::setBatchRowDeliveryDate,
            onConfirm = viewModel::confirmBatchCreate,
            onDismiss = viewModel::dismissBatchCreateDialog,
        )
    }

    if (uiState.showEditDialog) {
        WeldingOrderEditDialog(
            form = uiState.editForm,
            loading = uiState.actionLoading,
            onFormChange = viewModel::updateEditForm,
            onConfirm = viewModel::confirmEdit,
            onDismiss = viewModel::dismissEditDialog,
        )
    }

    if (uiState.showPrintDialog) {
        WeldingOrderPrintDialog(
            form = uiState.printForm,
            orderCount = uiState.printOrders.size,
            loading = uiState.printLoading,
            onFormChange = viewModel::updatePrintForm,
            onConfirm = viewModel::confirmPrint,
            onDismiss = viewModel::dismissPrintDialog,
        )
    }

    if (uiState.showDeleteConfirm) {
        OutsourcingConfirmDialog(
            title = "注文削除",
            message = "注文 ${uiState.deleteTarget?.orderNo.orEmpty()} を削除しますか？",
            confirmText = "削除",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteConfirm,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        OutsourcingOrderPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                WeldingOrderHeroBar(uiState.orders.size)
                WeldingOrderFilterSection(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    suppliers = uiState.suppliers,
                    supplierCd = uiState.supplierCd,
                    productOptions = uiState.productOptions,
                    productName = uiState.productName,
                    statusFilter = uiState.statusFilter,
                    onStartChange = viewModel::setStartDate,
                    onEndChange = viewModel::setEndDate,
                    onShiftDate = viewModel::shiftDate,
                    onToday = viewModel::setTodayRange,
                    onThisMonth = viewModel::setThisMonthRange,
                    onSupplierChange = viewModel::setSupplierCd,
                    onProductChange = viewModel::setProductName,
                    onStatusChange = viewModel::setStatusFilter,
                    onReset = viewModel::resetFilters,
                )
                WeldingOrderActionSection(
                    totalLabel = viewModel.totalSummaryLabel(),
                    actionLoading = uiState.actionLoading || uiState.printLoading,
                    onCreate = viewModel::openCreateDialog,
                    onBatchCreate = viewModel::openBatchCreateDialog,
                    onPrint = viewModel::openPrintDialogForList,
                )
                WeldingOrderTable(
                    orders = uiState.orders,
                    isLoading = uiState.isLoading,
                    onEdit = viewModel::openEditDialog,
                    onPrint = viewModel::openPrintDialogForOrder,
                    onDelete = viewModel::requestDelete,
                )
            }
        }
    }
}
