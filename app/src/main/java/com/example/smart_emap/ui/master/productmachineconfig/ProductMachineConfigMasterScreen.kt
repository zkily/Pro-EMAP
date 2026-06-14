package com.example.smart_emap.ui.master.productmachineconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.shell.LocalCurrentUser

@Composable
fun ProductMachineConfigMasterScreen(viewModel: ProductMachineConfigMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = LocalCurrentUser.current
    val masterModule = OperationModules.MASTER
    val canCreate = remember(user.id, user.operationPermissions) { user.canCreate(masterModule) }
    val canEdit = remember(user.id, user.operationPermissions) { user.canEdit(masterModule) }
    val canDelete = remember(user.id, user.operationPermissions) { user.canDelete(masterModule) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        PmcPageBackground {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 4.dp, vertical = 3.dp),
            ) {
                val layout = pmcLayoutMode(maxWidth)
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    ProductMachineConfigHeroBar(
                        total = uiState.rows.size,
                        filtered = uiState.filteredRows.size,
                        layout = layout,
                    )
                    PmcWorkspace(Modifier.weight(1f)) {
                        Column(Modifier.fillMaxSize()) {
                            ProductMachineConfigActionSection(
                                keyword = uiState.keyword,
                                filteredCount = uiState.filteredRows.size,
                                totalCount = uiState.rows.size,
                                loading = uiState.isLoading,
                                syncing = uiState.syncing,
                                canEdit = canEdit,
                                canCreate = canCreate,
                                layout = layout,
                                onKeywordChange = viewModel::setKeyword,
                                onClear = viewModel::clearFilters,
                                onSync = { viewModel.syncProducts(canEdit) },
                                onCreate = { viewModel.openCreate(canCreate) },
                            )
                            ProductMachineConfigTable(
                                rows = uiState.filteredRows,
                                loading = uiState.isLoading,
                                canEdit = canEdit,
                                canDelete = canDelete,
                                layout = layout,
                                modifier = Modifier.weight(1f),
                                onEdit = { viewModel.openEdit(it, canEdit) },
                                onDelete = { viewModel.requestDelete(it, canDelete) },
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.formRow?.let { row ->
        if (uiState.showForm) {
            ProductMachineConfigFormDialog(
                row = row,
                isEdit = uiState.isEdit,
                loading = uiState.actionLoading,
                productOptions = uiState.productOptions,
                machineOptions = uiState.machineOptions,
                onUpdate = viewModel::updateFormRow,
                onProductSelected = viewModel::onProductSelected,
                onConfirm = { viewModel.saveForm(canCreate, canEdit) },
                onDismiss = viewModel::closeForm,
            )
        }
    }

    uiState.pendingDeleteId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            shape = RoundedCornerShape(16.dp),
            title = { Text("削除確認", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("この機器設定を削除しますか？", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("削除", color = PmcTheme.Rose, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル", fontSize = 12.sp) }
            },
        )
    }
}
