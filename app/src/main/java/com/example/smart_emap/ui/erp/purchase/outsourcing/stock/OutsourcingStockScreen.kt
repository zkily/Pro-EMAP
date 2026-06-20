package com.example.smart_emap.ui.erp.purchase.outsourcing.stock

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
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingPaginationBar
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun OutsourcingStockScreen(viewModel: OutsourcingStockViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    uiState.detailItem?.let { item ->
        OutsourcingStockDetailDialog(
            item = item,
            isPlating = uiState.activeTab == OutsourcingStockTab.Plating,
            onDismiss = viewModel::hideDetail,
        )
    }

    if (uiState.showHistoryDialog) {
        OutsourcingStockHistoryDialog(
            title = uiState.historyTitle,
            items = uiState.historyItems,
            isLoading = uiState.historyLoading,
            onDismiss = viewModel::hideHistory,
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
                OutsourcingStockHeroBar(uiState)
                OutsourcingStockToolbar(
                    uiState = uiState,
                    onTabSelect = { index ->
                        viewModel.setActiveTab(if (index == 0) OutsourcingStockTab.Plating else OutsourcingStockTab.Welding)
                    },
                    onSupplierChange = viewModel::setSupplierId,
                    onProductCodeChange = viewModel::setProductCode,
                    onStockStatusChange = viewModel::setStockStatus,
                    onSearch = viewModel::search,
                    onRefresh = viewModel::refreshStock,
                    onExport = viewModel::exportData,
                )
                OutsourcingStockSummaryStrip(uiState)
                OutsourcingStockTable(
                    uiState = uiState,
                    onDetail = viewModel::showDetail,
                    onHistory = viewModel::viewHistory,
                )
                OutsourcingPaginationBar(
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
