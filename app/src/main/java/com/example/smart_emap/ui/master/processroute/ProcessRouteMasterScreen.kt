package com.example.smart_emap.ui.master.processroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ProcessRouteMasterScreen(viewModel: ProcessRouteMasterViewModel) {
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
    ) { padding ->
        MasterPageScaffold {
            when (uiState.screenMode) {
                ProcessRouteScreenMode.List -> ProcessRouteListContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding),
                )
                ProcessRouteScreenMode.StepEditor -> ProcessRouteStepEditorContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    if (uiState.showRouteForm) {
        ProcessRouteFormDialog(
            isEdit = uiState.editingRoute != null,
            values = uiState.routeFormValues,
            loading = uiState.actionLoading,
            onValueChange = viewModel::setRouteFormValue,
            onConfirm = viewModel::saveRouteForm,
            onDismiss = viewModel::closeRouteForm,
        )
    }

    if (uiState.showStepForm) {
        ProcessRouteStepFormDialog(
            isEdit = uiState.editingStep != null,
            values = uiState.stepFormValues,
            processOptions = uiState.processOptions,
            loading = uiState.actionLoading,
            onValueChange = viewModel::setStepFormValue,
            onConfirm = viewModel::saveStepForm,
            onDismiss = viewModel::closeStepForm,
        )
    }

    uiState.pendingDeleteRouteId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteRoute,
            title = { Text("削除確認") },
            text = { Text("この工程ルートを削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmDeleteRoute) { Text("はい") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDeleteRoute) { Text("キャンセル") } },
        )
    }

    uiState.pendingDeleteStepId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteStep,
            title = { Text("削除確認") },
            text = { Text("このステップを削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmDeleteStep) { Text("はい") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDeleteStep) { Text("キャンセル") } },
        )
    }
}

@Composable
private fun ProcessRouteListContent(
    uiState: ProcessRouteMasterUiState,
    viewModel: ProcessRouteMasterViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(processRoutePageBackground)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ProcessRouteHeroBar(total = uiState.total, displayed = uiState.routes.size)
        ProcessRouteFilterCard(
            keyword = uiState.keyword,
            hasActiveFilters = viewModel.hasActiveFilters(),
            actionLoading = uiState.actionLoading || uiState.isLoading,
            onKeywordChange = viewModel::setKeyword,
            onSearch = viewModel::search,
            onClear = viewModel::clearFilters,
            onAdd = viewModel::openCreateRoute,
        )
        ProcessRouteTable(
            routes = uiState.routes,
            loading = uiState.isLoading,
            total = uiState.total,
            modifier = Modifier.weight(1f),
            onSteps = viewModel::openStepEditor,
            onEdit = viewModel::openEditRoute,
            onDelete = viewModel::requestDeleteRoute,
        )
        ProcessRoutePaginationBar(
            page = uiState.page,
            pageSize = uiState.pageSize,
            total = uiState.total,
            onPageChange = viewModel::setPage,
            onPageSizeChange = viewModel::setPageSize,
        )
    }
}

@Composable
private fun ProcessRouteStepEditorContent(
    uiState: ProcessRouteMasterUiState,
    viewModel: ProcessRouteMasterViewModel,
    modifier: Modifier = Modifier,
) {
    val routeCd = uiState.editingRouteCd.orEmpty()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(processRoutePageBackground)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ProcessRouteStepHeroBar(
            routeCd = routeCd,
            routeName = uiState.editingRouteName,
            stepCount = uiState.steps.size,
            onBack = viewModel::closeStepEditor,
        )
        ProcessRouteStepActionBar(
            actionLoading = uiState.actionLoading || uiState.isLoading,
            onAddStep = viewModel::openCreateStep,
            onSaveOrder = viewModel::saveStepOrder,
            hasSteps = uiState.steps.isNotEmpty(),
        )
        ProcessRouteStepTable(
            steps = uiState.steps,
            loading = uiState.isLoading,
            modifier = Modifier.weight(1f),
            onEdit = viewModel::openEditStep,
            onDelete = viewModel::requestDeleteStep,
        )
    }
}
