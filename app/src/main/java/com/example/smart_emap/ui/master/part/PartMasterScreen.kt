package com.example.smart_emap.ui.master.part

import android.content.Intent
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
fun PartMasterScreen(viewModel: PartMasterViewModel) {
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
        val subject = uiState.pendingPrintSubject ?: "部品マスタ印刷"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    LaunchedEffect(uiState.pendingCsvContent) {
        val csv = uiState.pendingCsvContent ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "parts.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "CSV共有"))
        viewModel.clearPendingCsv()
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
                    .background(partPageBackground)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PartMasterHeroBar(total = uiState.total, displayed = uiState.parts.size)
                PartMasterFilterCard(
                    keyword = uiState.keyword,
                    statusFilter = uiState.statusFilter,
                    total = uiState.total,
                    displayedCount = uiState.parts.size,
                    hasActiveFilters = viewModel.hasActiveFilters(),
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    onKeywordChange = viewModel::setKeyword,
                    onStatusChange = viewModel::setStatusFilter,
                    onSearch = viewModel::search,
                    onClear = viewModel::clearFilters,
                    onExportCsv = viewModel::exportCsv,
                    onQrPrint = viewModel::printQrCodes,
                    onAdd = viewModel::openCreate,
                )
                PartMasterTable(
                    parts = uiState.parts,
                    loading = uiState.isLoading,
                    total = uiState.total,
                    modifier = Modifier.weight(1f),
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::requestDelete,
                )
                PartMasterPaginationBar(
                    page = uiState.page,
                    pageSize = PartMasterViewModel.PAGE_SIZE,
                    total = uiState.total,
                    onPageChange = viewModel::setPage,
                )
            }
        }
    }

    if (uiState.showForm) {
        PartMasterFormDialog(
            isEdit = uiState.editingPart != null,
            values = uiState.formValues,
            loading = uiState.actionLoading,
            supplierOptions = uiState.supplierOptions,
            onValueChange = viewModel::setFormValue,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
        )
    }

    uiState.pendingDeleteId?.let {
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("削除確認") },
            text = { Text("この部品を削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("はい") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル") } },
        )
    }
}
