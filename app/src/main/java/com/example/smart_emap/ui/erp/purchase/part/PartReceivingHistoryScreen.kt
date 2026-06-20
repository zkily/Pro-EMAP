package com.example.smart_emap.ui.erp.purchase.part

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PartReceivingHistoryScreen(viewModel: PartReceivingHistoryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scroll = rememberScrollState()
    val actionLoading = uiState.isLoading || uiState.importLoading || uiState.printLoading

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
            jobName = "部品受入履歴",
            layout = PrintPageLayout.A4_LANDSCAPE_SINGLE,
        )
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    uiState.detailItem?.let { item ->
        PartReceivingDetailDialog(item = item, onDismiss = viewModel::hideDetail)
    }

    if (uiState.showColumnSettings) {
        PartReceivingColumnSettingsDialog(
            draft = uiState.columnSettingsDraft,
            onToggle = viewModel::toggleColumnDraft,
            onReset = viewModel::resetColumnSettingsDraft,
            onSave = viewModel::saveColumnSettings,
            onDismiss = viewModel::closeColumnSettings,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        PurchasePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PartReceivingHeroBar(
                    totalCount = uiState.totalCount,
                    displayCount = uiState.items.size,
                )
                PartReceivingSearchFilterBar(
                    keyword = uiState.keyword,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    supplierOptions = uiState.supplierOptions,
                    selectedSuppliers = uiState.selectedSuppliers,
                    sortField = uiState.sortField,
                    sortOrder = uiState.sortOrder,
                    actionLoading = actionLoading,
                    printLoading = uiState.printLoading,
                    importLoading = uiState.importLoading,
                    canPrint = uiState.totalCount > 0,
                    onKeywordChange = viewModel::setKeyword,
                    onStartChange = viewModel::setStartDate,
                    onEndChange = viewModel::setEndDate,
                    onSupplierChange = viewModel::setSelectedSuppliers,
                    onSortFieldChange = viewModel::setSortField,
                    onSortOrderChange = viewModel::setSortOrder,
                    onSearch = viewModel::search,
                    onClear = viewModel::clearFilters,
                    onColumnSettings = viewModel::openColumnSettings,
                    onPrint = viewModel::printAll,
                    onImport = viewModel::importCsv,
                )
                PartReceivingHistoryTable(
                    items = uiState.items,
                    visibleColumns = uiState.visibleColumns,
                    isLoading = uiState.isLoading,
                    totalCount = uiState.totalCount,
                    onRowClick = viewModel::showDetail,
                )
                PartReceivingPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    totalCount = uiState.totalCount,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                )
            }
        }
    }
}
