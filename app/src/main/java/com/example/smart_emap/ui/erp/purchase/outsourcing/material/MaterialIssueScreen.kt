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
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingPaginationBar
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialIssueScreen(viewModel: MaterialIssueViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    if (uiState.showFormDialog) {
        MaterialIssueFormDialog(
            uiState = uiState,
            onDismiss = viewModel::hideFormDialog,
            onSubmit = viewModel::submitForm,
            onMaterialChange = viewModel::onMaterialChange,
            onFormChange = viewModel::updateForm,
        )
    }

    uiState.detailItem?.let { item ->
        MaterialIssueDetailDialog(item = item, onDismiss = viewModel::hideDetail)
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
                MaterialIssueHeroBar(uiState)
                MaterialIssueFilters(
                    uiState = uiState,
                    onStartDateChange = viewModel::setStartDate,
                    onEndDateChange = viewModel::setEndDate,
                    onSupplierChange = viewModel::setSupplierId,
                    onOrderNoChange = viewModel::setOrderNo,
                    onMaterialCodeChange = viewModel::setMaterialCode,
                    onStatusChange = viewModel::setStatus,
                    onSearch = viewModel::search,
                    onReset = viewModel::resetFilters,
                )
                MaterialIssueActionBar(
                    uiState = uiState,
                    onCreate = viewModel::openCreateDialog,
                    onBatchIssue = viewModel::batchIssue,
                    onExport = viewModel::exportData,
                    onPrint = viewModel::printDeliveryNote,
                )
                MaterialIssueTable(
                    uiState = uiState,
                    onToggleSelect = viewModel::toggleSelection,
                    onDetail = viewModel::showDetail,
                    onIssue = viewModel::issueItem,
                    onEdit = viewModel::openEditDialog,
                    onDelete = viewModel::deleteIssue,
                )
                OutsourcingPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    totalCount = uiState.totalCount,
                    onPageChange = {},
                    onPageSizeChange = {},
                )
            }
        }
    }
}
