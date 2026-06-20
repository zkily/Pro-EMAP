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
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingOrderPageBackground
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingTodayJapan
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun WeldingReceivingScreen(viewModel: WeldingReceivingViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val today = outsourcingTodayJapan()
    val todayReceiveCount = uiState.receivings.count { it.receivingDate == today }

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
            jobName = "外注溶接受入一覧",
            layout = PrintPageLayout.A4_PORTRAIT_SINGLE,
        )
        if (!opened) {
            snackbarHostState.showSnackbar("印刷を開始できませんでした")
        }
        viewModel.clearPendingPrintHtml()
    }

    if (uiState.showReceivingDialog) {
        WeldingReceivingDialog(
            isEdit = uiState.isEditMode,
            form = uiState.form,
            pendingOrders = uiState.pendingOrders,
            loading = uiState.actionLoading,
            onOrderSelect = viewModel::onOrderSelected,
            onFormChange = viewModel::updateForm,
            onReceivingQtyChange = viewModel::onReceivingQtyChange,
            onGoodQtyChange = viewModel::onGoodQtyChange,
            onConfirm = viewModel::confirmReceiving,
            onDismiss = viewModel::dismissReceivingDialog,
        )
    }

    uiState.detailReceiving?.let { row ->
        WeldingReceivingDetailDialog(
            row = row,
            onDismiss = viewModel::dismissDetailDialog,
            onEdit = viewModel::editFromDetail,
        )
    }

    uiState.detailOrder?.let { order ->
        WeldingReceivingOrderDetailDialog(order = order, onDismiss = viewModel::dismissOrderDetail)
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
                WeldingReceivingHeroBar(
                    count = uiState.totalCount,
                    pendingCount = viewModel.pendingCount(),
                    todayCount = todayReceiveCount,
                )
                WeldingReceivingFilterSection(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    keyword = uiState.keyword,
                    suppliers = uiState.suppliers,
                    supplierId = uiState.supplierId,
                    productOptions = uiState.productOptions,
                    productName = uiState.productName,
                    statusFilter = uiState.statusFilter,
                    onStartChange = viewModel::setStartDate,
                    onEndChange = viewModel::setEndDate,
                    onShiftDate = viewModel::shiftDate,
                    onToday = viewModel::setTodayRange,
                    onThisMonth = viewModel::setThisMonthRange,
                    onKeywordChange = viewModel::setKeyword,
                    onSupplierChange = viewModel::setSupplierId,
                    onProductChange = viewModel::setProductName,
                    onStatusChange = viewModel::setStatusFilter,
                    onReset = viewModel::resetFilters,
                )
                WeldingReceivingActionSection(
                    todayLabel = viewModel.todayQuantityLabel(),
                    actionLoading = uiState.actionLoading,
                    onCreate = viewModel::openCreateDialog,
                    onPrint = viewModel::printList,
                )
                WeldingReceivingTableSection(
                    items = uiState.receivings,
                    isLoading = uiState.isLoading,
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    totalCount = uiState.totalCount,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                    onViewDetail = viewModel::openDetailDialog,
                    onViewOrder = { viewModel.openOrderDetail(it.orderNo.orEmpty()) },
                    onEdit = viewModel::openEditDialog,
                )
            }
        }
    }
}
