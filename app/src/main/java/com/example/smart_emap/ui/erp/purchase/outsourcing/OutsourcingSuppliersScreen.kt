package com.example.smart_emap.ui.erp.purchase.outsourcing

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
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun OutsourcingSuppliersScreen(viewModel: OutsourcingSuppliersViewModel) {
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
        OutsourcingMasterPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutsourcingSuppliersHeroBar(
                    count = uiState.suppliers.size,
                    actionLoading = uiState.actionLoading,
                    onCreate = viewModel::openCreateDialog,
                )
                OutsourcingSuppliersFilterBar(
                    filterType = uiState.filterType,
                    filterIsActive = uiState.filterIsActive,
                    keyword = uiState.keyword,
                    onTypeChange = viewModel::setFilterType,
                    onActiveChange = viewModel::setFilterIsActive,
                    onKeywordChange = viewModel::setKeyword,
                    onReset = viewModel::resetFilters,
                )
                OutsourcingSuppliersTablePanel(
                    isLoading = uiState.isLoading,
                    suppliers = uiState.suppliers,
                    togglingId = uiState.togglingId,
                    onAdd = viewModel::openCreateDialog,
                    onEdit = viewModel::openEditDialog,
                    onDelete = viewModel::requestDelete,
                    onToggle = viewModel::toggleStatus,
                )
            }
        }
    }

    OutsourcingSuppliersFormDialog(
        visible = uiState.showFormDialog,
        isEdit = uiState.isEdit,
        form = uiState.form,
        duplicateCodeError = uiState.duplicateCodeError,
        loading = uiState.actionLoading,
        onDismiss = viewModel::dismissFormDialog,
        onSubmit = viewModel::submitForm,
        onFormChange = viewModel::updateForm,
        onCodeBlur = viewModel::checkSupplierCode,
    )

    if (uiState.showDeleteConfirm) {
        OutsourcingConfirmDialog(
            title = "削除確認",
            message = "「${uiState.pendingDelete?.supplierName.orEmpty()}」を削除しますか？削除後は元に戻せません。",
            confirmText = "削除",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteConfirm,
        )
    }
}
