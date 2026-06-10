package com.example.smart_emap.ui.system.user

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.core.auth.canExport
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.shell.LocalCurrentUser

@Composable
fun UserListScreen(viewModel: UserListViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = LocalCurrentUser.current
    val canCreate = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canCreate(OperationModules.SYSTEM)
    }
    val canEdit = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canEdit(OperationModules.SYSTEM)
    }
    val canExport = remember(currentUser.id, currentUser.operationPermissions) {
        currentUser.canExport(OperationModules.SYSTEM)
    }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(context, html, "ユーザー一覧", PrintPageLayout.A4_LANDSCAPE_SINGLE)
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
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
            UserPageBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                UserListPageCard {
                    UserListHeroBar(
                        total = uiState.total,
                        activeCount = viewModel.activeCount,
                        lockedCount = viewModel.lockedCount,
                    )
                    UserListFilterSection(
                        keyword = uiState.keyword,
                        departmentId = uiState.departmentId,
                        statusFilter = uiState.statusFilter,
                        departments = uiState.departments,
                        canCreate = canCreate,
                        canExport = canExport,
                        onKeywordChange = viewModel::setKeyword,
                        onDepartmentChange = viewModel::setDepartmentFilter,
                        onStatusChange = viewModel::setStatusFilter,
                        onAdd = viewModel::openCreate,
                        onPrint = viewModel::printUsers,
                    )
                    uiState.errorMessage?.let { msg ->
                        UserErrorBanner(message = msg)
                    }
                    UserListTable(
                        users = uiState.users,
                        isLoading = uiState.isLoading,
                        canEdit = canEdit,
                        currentUserId = currentUser.id,
                        onEdit = viewModel::openEdit,
                        onToggleLock = { user -> viewModel.toggleLock(user, currentUser.id) },
                        onResetPassword = viewModel::openResetPassword,
                    )
                    UserListPagination(
                        page = uiState.page,
                        pageSize = uiState.pageSize,
                        total = uiState.total,
                        shown = uiState.users.size,
                        pages = uiState.pages,
                        onPageChange = viewModel::setPage,
                        onPageSizeChange = viewModel::setPageSize,
                    )
                }
            }
        }
    }

    if (uiState.showFormDialog) {
        UserFormDialog(
            isEdit = uiState.isEditMode,
            form = uiState.form,
            roles = uiState.roles,
            departments = uiState.departments,
            isSubmitting = uiState.isSubmitting,
            onFormChange = viewModel::updateForm,
            onConfirm = viewModel::submitForm,
            onDismiss = viewModel::dismissForm,
        )
    }

    if (uiState.showResetPasswordDialog) {
        ResetPasswordDialog(
            form = uiState.resetPasswordForm,
            isSubmitting = uiState.isSubmitting,
            onFormChange = viewModel::updateResetPasswordForm,
            onConfirm = viewModel::submitResetPassword,
            onDismiss = viewModel::dismissResetPassword,
        )
    }
}
