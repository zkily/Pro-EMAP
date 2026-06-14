package com.example.smart_emap.ui.master.productprocessbom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.shell.LocalCurrentUser

@Composable
fun ProductProcessBomMasterScreen(viewModel: ProductProcessBomMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = LocalCurrentUser.current
    val masterModule = OperationModules.MASTER
    val canEdit = remember(user.id, user.operationPermissions) { user.canEdit(masterModule) }
    val canDelete = remember(user.id, user.operationPermissions) { user.canDelete(masterModule) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        PpbPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProductProcessBomHeroBar(stats = uiState.stats)
                PpbWorkspace {
                    ProductProcessBomToolbar(
                        keyword = uiState.keyword,
                        loading = uiState.isLoading,
                        syncing = uiState.syncing,
                        canEdit = canEdit,
                        onKeywordChange = viewModel::setKeyword,
                        onSearch = viewModel::search,
                        onClear = viewModel::clearFilters,
                        onSync = { viewModel.syncProducts(canEdit) },
                    )
                    ProductProcessBomTable(
                        rows = uiState.rows,
                        loading = uiState.isLoading,
                        savingProductCd = uiState.savingProductCd,
                        canEdit = canEdit,
                        canDelete = canDelete,
                        onRowChange = viewModel::updateRow,
                        onAutoSave = { viewModel.scheduleAutoSave(it, canEdit) },
                        onEdit = { viewModel.openEdit(it, canEdit) },
                        onDelete = { viewModel.requestDelete(it, canDelete) },
                    )
                    ProductProcessBomPaginationBar(
                        page = uiState.page,
                        pageSize = uiState.pageSize,
                        total = uiState.stats.total,
                        onPageChange = viewModel::setPage,
                        onPageSizeChange = viewModel::setPageSize,
                    )
                }
            }
        }
    }

    uiState.formRow?.let { row ->
        if (uiState.showForm) {
            ProductProcessBomFormDialog(
                row = row,
                loading = uiState.actionLoading,
                onUpdate = viewModel::updateFormRow,
                onConfirm = { viewModel.saveForm(canEdit) },
                onDismiss = viewModel::closeForm,
            )
        }
    }

    uiState.pendingDeleteProductCd?.let { cd ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            shape = RoundedCornerShape(16.dp),
            title = { Text("削除確認", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("製品CD: $cd の製品工程BOMを削除しますか？", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("削除", color = PpbTheme.Rose, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル", fontSize = 12.sp) }
            },
        )
    }
}
