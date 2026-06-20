package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun StockTransactionLogScreen(viewModel: StockTransactionLogViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        InventoryLightPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StockTransactionLogHeroBar(total = uiState.total)
                StockTransactionLogFilterPanel(
                    uiState = uiState,
                    onStockTypeChange = viewModel::setFilterStockType,
                    onTargetCdChange = viewModel::setFilterTargetCd,
                    onKeywordChange = viewModel::setFilterKeyword,
                    onLocationChange = viewModel::setFilterLocationCd,
                    onTransactionTypeChange = viewModel::setFilterTransactionType,
                    onProcessChange = viewModel::setFilterProcessCd,
                    onDateStartChange = viewModel::setFilterDateStart,
                    onDateEndChange = viewModel::setFilterDateEnd,
                    onShiftDate = viewModel::shiftDateRange,
                    onToday = viewModel::setTodayRange,
                    onReset = viewModel::resetFilters,
                )
                StockTransactionLogStatsRow(
                    totalQuantity = uiState.totalQuantity,
                    inboundQuantity = uiState.inboundQuantity,
                    outboundQuantity = uiState.outboundQuantity,
                    totalRecords = uiState.total,
                )
                InventoryTablePanel(
                    title = "履歴データ一覧",
                    countLabel = "${uiState.total}件",
                    loading = uiState.isLoading,
                    dark = false,
                ) {
                    if (uiState.rows.isEmpty()) {
                        androidx.compose.material3.Text(
                            "データがありません",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 11.sp,
                        )
                    } else {
                        StockTransactionLogTable(
                            rows = uiState.rows,
                            onEdit = viewModel::openEditDialog,
                            onDelete = viewModel::requestDelete,
                        )
                    }
                }
                InventoryPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    total = uiState.total,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                    dark = false,
                )
            }
        }
    }

    StockTransactionLogEditDialog(
        visible = uiState.showEditDialog,
        form = uiState.editForm,
        loading = uiState.actionLoading,
        processOptions = uiState.processOptions,
        locationOptions = uiState.locationOptions,
        onDismiss = viewModel::dismissEditDialog,
        onSubmit = viewModel::submitEdit,
        onFormChange = viewModel::updateEditForm,
    )

    if (uiState.showDeleteConfirm) {
        InventoryConfirmDialog(
            title = "削除確認",
            message = "この在庫取引記録を削除しますか？削除後は元に戻せません。",
            confirmText = "削除",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteConfirm,
        )
    }
}
