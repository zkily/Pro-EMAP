package com.example.smart_emap.ui.master.process

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ProcessMasterScreen(viewModel: ProcessMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val subject = uiState.pendingPrintSubject ?: "工程QRコード印刷"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
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
                    .background(processPageBackground)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProcessMasterHeroBar(total = uiState.total)
                ProcessMasterActionCard(
                    keyword = uiState.keyword,
                    displayedCount = uiState.processes.size,
                    hasActiveFilters = viewModel.hasActiveFilters(),
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    onKeywordChange = viewModel::setKeyword,
                    onSearch = viewModel::search,
                    onClear = viewModel::clearFilters,
                    onQrPrint = viewModel::printQrCodes,
                    onAdd = viewModel::openCreate,
                )
                ProcessMasterTable(
                    processes = uiState.processes,
                    loading = uiState.isLoading,
                    total = uiState.total,
                    modifier = Modifier.weight(1f),
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::requestDelete,
                )
            }
        }
    }

    if (uiState.showForm) {
        ProcessMasterFormDialog(
            isEdit = uiState.editingProcess != null,
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
            text = { Text("この工程を削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("はい") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル") } },
        )
    }
}
