package com.example.smart_emap.ui.erp.purchase.outsourcing.material

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
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun SuppliedMaterialStockScreen(viewModel: SuppliedMaterialStockViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    if (uiState.showHistoryDialog) {
        SuppliedMaterialHistoryDialog(
            title = uiState.historyTitle,
            items = uiState.historyItems,
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
                SuppliedMaterialStockHeroBar(uiState)
                SuppliedMaterialStockFilters(
                    uiState = uiState,
                    onSupplierChange = viewModel::setSupplierId,
                    onMaterialCodeChange = viewModel::setMaterialCode,
                    onStockStatusChange = viewModel::setStockStatus,
                    onSearch = viewModel::search,
                    onReset = viewModel::resetFilters,
                )
                SuppliedMaterialStockActionBar(
                    uiState = uiState,
                    onExport = viewModel::exportData,
                    onRefresh = viewModel::refreshStock,
                )
                SuppliedMaterialSupplierCards(
                    suppliers = uiState.filteredSuppliers,
                    isLoading = uiState.isLoading,
                    onHistory = viewModel::viewHistory,
                )
            }
        }
    }
}
