package com.example.smart_emap.ui.master.materialinspection

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
fun MaterialInspectionMasterScreen(viewModel: MaterialInspectionMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

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
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MaterialInspectionHeroBar(total = uiState.total, displayed = uiState.items.size)
                MaterialInspectionToolbar(
                    total = uiState.total,
                    actionLoading = uiState.actionLoading,
                    onAdd = viewModel::openCreate,
                )
                MaterialInspectionTable(
                    items = uiState.items,
                    loading = uiState.isLoading,
                    onRowClick = viewModel::openDetail,
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::requestDelete,
                )
                MaterialInspectionPaginationBar(
                    page = uiState.page,
                    pageSize = uiState.pageSize,
                    total = uiState.total,
                    onPageChange = viewModel::setPage,
                    onPageSizeChange = viewModel::setPageSize,
                )
            }
        }
    }

    if (uiState.showForm) {
        MaterialInspectionMasterFormDialog(
            isEdit = uiState.editingItem != null,
            inspectionCd = uiState.inspectionCd,
            inspectionStandard = uiState.inspectionStandard,
            loading = uiState.actionLoading,
            onCdChange = viewModel::setInspectionCd,
            onStandardChange = viewModel::setInspectionStandard,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
        )
    }

    if (uiState.showDetail && uiState.detailItem != null) {
        MaterialInspectionDetailDialog(item = uiState.detailItem!!, onDismiss = viewModel::closeDetail)
    }

    uiState.pendingDeleteIds?.let { ids ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("削除確認") },
            text = { Text("この検品CDを削除しますか？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("はい") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル") }
            },
        )
    }
}
