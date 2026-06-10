package com.example.smart_emap.ui.system.role

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
fun RolePermissionScreen(viewModel: RolePermissionViewModel) {
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
        containerColor = LayoutColors.ShellBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            RolePageBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(10.dp),
            ) {
                RoleHeroBar(
                    roleCount = uiState.roleList.size,
                    totalUsers = viewModel.totalUserCount,
                )
                RoleTwoColumnLayout(
                    roleListContent = {
                        RoleListPanel(
                            isLoading = uiState.isRolesLoading,
                            roles = uiState.roleList,
                            selectedId = uiState.selectedRoleId,
                            canCreate = canCreate,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            onAdd = viewModel::openAddRole,
                            onSelect = viewModel::selectRole,
                            onEdit = viewModel::openEditRole,
                            onDelete = viewModel::requestDeleteRole,
                        )
                    },
                    permissionContent = {
                        RolePermissionPanel(
                            selectedRole = uiState.selectedRole,
                            isDetailLoading = uiState.isDetailLoading,
                            isSaving = uiState.isSaving,
                            activeTab = uiState.activeTab,
                            permissionMenuTree = uiState.permissionMenuTree,
                            isMenuLoading = uiState.isMenuLoading,
                            expandedMenuCodes = uiState.expandedMenuCodes,
                            checkedMenuCodes = uiState.checkedMenuCodes,
                            operationPermissions = uiState.operationPermissions,
                            dataScope = uiState.dataScope,
                            customDepartments = uiState.customDepartments,
                            departmentOptions = uiState.departmentOptions,
                            canEdit = canEdit,
                            onTabChange = viewModel::setActiveTab,
                            onSave = viewModel::savePermissions,
                            onToggleMenuExpand = viewModel::toggleMenuExpand,
                            onToggleMenuCheck = viewModel::toggleMenuCheck,
                            onOperationChange = viewModel::updateOperationPermission,
                            onDataScopeChange = viewModel::setDataScope,
                            onToggleCustomDept = viewModel::toggleCustomDepartment,
                        )
                    },
                )
            }
        }
    }

    if (uiState.showFormDialog) {
        RoleFormDialog(
            isEdit = uiState.isEditMode,
            form = uiState.form,
            isSubmitting = uiState.isSubmitting,
            onFormChange = viewModel::updateForm,
            onConfirm = viewModel::submitRole,
            onDismiss = viewModel::dismissForm,
        )
    }

    uiState.showDeleteConfirm?.let { role ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text("削除確認", fontSize = 15.sp) },
            text = { Text("ロール「${role.name}」を削除しますか？", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteRole) {
                    Text("削除", color = RolePermissionTheme.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text("キャンセル")
                }
            },
        )
    }
}
