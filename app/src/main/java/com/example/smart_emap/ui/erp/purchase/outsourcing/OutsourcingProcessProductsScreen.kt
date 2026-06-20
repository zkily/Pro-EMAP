package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
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
fun OutsourcingProcessProductsScreen(viewModel: OutsourcingProcessProductsViewModel) {
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
        OutsourcingProcessPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutsourcingProcessHeroHeader(
                    title = "外注工程製品管理",
                    subtitle = "外注工程ごとの製品情報を管理します",
                    icon = Icons.Default.GridView,
                    stats = listOf(
                        uiState.statsTotal.toString() to "総登録数",
                        uiState.statsActive.toString() to "有効",
                        uiState.statsSuppliers.toString() to "外注先数",
                    ),
                )
                OutsourcingProcessTabsRow(
                    selected = uiState.tab,
                    counts = viewModel::processCount,
                    onSelect = viewModel::setTab,
                )
                OutsourcingProcessProductsFilterSection(
                    supplierCd = uiState.supplierCd,
                    keyword = uiState.keyword,
                    isActiveFilter = uiState.isActiveFilter,
                    supplierOptions = uiState.supplierOptions,
                    onSupplierChange = viewModel::setSupplierCd,
                    onKeywordChange = viewModel::setKeyword,
                    onActiveChange = viewModel::setIsActiveFilter,
                )
                OutsourcingProcessProductsActionSection(
                    actionLoading = uiState.actionLoading,
                    onCreate = viewModel::openCreateDialog,
                )
                OutsourcingProcessProductsTablePanel(
                    isLoading = uiState.isLoading,
                    items = uiState.items,
                    onEdit = viewModel::openEditDialog,
                    onToggle = viewModel::requestToggle,
                    onDelete = viewModel::requestDelete,
                )
                OutsourcingPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    totalCount = uiState.total,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                )
            }
        }
    }

    OutsourcingProcessProductsFormDialog(
        visible = uiState.showFormDialog,
        isEdit = uiState.isEdit,
        form = uiState.form,
        supplierOptions = uiState.supplierOptions,
        productOptions = uiState.productOptions,
        loading = uiState.actionLoading,
        onDismiss = viewModel::dismissFormDialog,
        onSubmit = viewModel::submitForm,
        onFormChange = viewModel::updateForm,
        onSupplierSelected = viewModel::onSupplierSelected,
        onProductSelected = viewModel::onProductSelected,
    )

    if (uiState.showDeleteConfirm) {
        val row = uiState.pendingActionRow
        OutsourcingConfirmDialog(
            title = "削除確認",
            message = "「${row?.productName ?: row?.productCd.orEmpty()}」を削除しますか？",
            confirmText = "削除",
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteConfirm,
        )
    }

    if (uiState.showToggleConfirm) {
        val row = uiState.pendingActionRow
        val action = if (row?.isActive == true) "無効化" else "有効化"
        OutsourcingConfirmDialog(
            title = "確認",
            message = "「${row?.productName ?: row?.productCd.orEmpty()}」を${action}しますか？",
            confirmText = action,
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmToggle,
            onDismiss = viewModel::dismissToggleConfirm,
        )
    }
}
