package com.example.smart_emap.ui.master.supplier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun SupplierMasterScreen(viewModel: SupplierMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val emailCount = uiState.suppliers.count { !it.email.isNullOrBlank() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        MasterPageScaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(supplierPageBackground)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SupplierMasterHeroBar(
                    total = uiState.total,
                    emailRegistered = emailCount,
                )
                SupplierMasterFilterCard(
                    keyword = uiState.keyword,
                    displayedCount = uiState.suppliers.size,
                    total = uiState.total,
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    onKeywordChange = viewModel::setKeyword,
                    onSearch = viewModel::search,
                    onClear = viewModel::clearFilters,
                    onAdd = viewModel::openCreate,
                )
                SupplierMasterTable(
                    suppliers = uiState.suppliers,
                    loading = uiState.isLoading,
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    total = uiState.total,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::requestDelete,
                )
            }
        }
    }

    if (uiState.showForm) {
        SupplierMasterFormDialog(
            isEdit = uiState.editingSupplier != null,
            values = uiState.formValues,
            loading = uiState.actionLoading,
            onValueChange = viewModel::setFormValue,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
        )
    }

    uiState.pendingDeleteId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("削除確認") },
            text = { Text("この仕入先を削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("はい") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル") } },
        )
    }
}
