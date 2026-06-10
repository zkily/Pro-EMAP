package com.example.smart_emap.ui.system.organization

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.sp
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.shell.LocalCurrentUser

@Composable
fun OrganizationListScreen(viewModel: OrganizationListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val canCreate = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canCreate(OperationModules.SYSTEM)
    }
    val canEdit = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canEdit(OperationModules.SYSTEM)
    }
    val canDelete = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canDelete(OperationModules.SYSTEM)
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OrgPageBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                OrgListHeroBar(
                    orgCount = viewModel.orgCount,
                    deptCount = viewModel.deptCount,
                )
                uiState.errorMessage?.let { msg ->
                    Text(msg, color = OrganizationTheme.Error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
                OrgTwoColumnLayout(
                    treeContent = {
                        OrgTreePanel(
                            isLoading = uiState.isTreeLoading,
                            orgTree = uiState.orgTree,
                            selectedId = uiState.selectedId,
                            expandedIds = uiState.expandedIds,
                            canCreate = canCreate,
                            onAdd = viewModel::openCreate,
                            onToggleExpand = viewModel::toggleExpand,
                            onSelect = viewModel::selectOrg,
                            onEdit = viewModel::openEditFromNode,
                        )
                    },
                    detailContent = {
                        OrgDetailPanel(
                            selectedOrg = uiState.selectedOrg,
                            parentName = viewModel.parentName(uiState.selectedOrg?.parentId),
                            orgUsers = uiState.orgUsers,
                            isLoading = uiState.isDetailLoading,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            onEdit = viewModel::openEdit,
                            onDelete = viewModel::requestDelete,
                        )
                    },
                )
            }
        }
    }

    if (uiState.showFormDialog) {
        OrganizationFormDialog(
            isEdit = uiState.isEditMode,
            form = uiState.form,
            orgTree = uiState.orgTree,
            isSubmitting = uiState.isSubmitting,
            onFormChange = viewModel::updateForm,
            onConfirm = viewModel::submitForm,
            onDismiss = viewModel::dismissForm,
        )
    }

    if (uiState.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("組織削除") },
            text = { Text("この組織を削除しますか？") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) { Text("削除", color = OrganizationTheme.Error) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) { Text("キャンセル") }
            },
        )
    }
}
