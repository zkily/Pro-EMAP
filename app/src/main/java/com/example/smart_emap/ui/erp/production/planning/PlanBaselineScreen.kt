package com.example.smart_emap.ui.erp.production.planning

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PlanBaselineScreen(viewModel: PlanBaselineViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingExportHtml) {
        val html = uiState.pendingExportHtml ?: return@LaunchedEffect
        HtmlPrintHelper.printHtml(context, html, "工程別報告書", PrintPageLayout.A4_PORTRAIT_SINGLE)
        viewModel.clearPendingExportHtml()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        HtmlPrintHelper.printHtml(context, html, "ベースライン比較", PrintPageLayout.A4_PORTRAIT_SINGLE)
        viewModel.clearPendingPrintHtml()
    }

    LaunchedEffect(uiState.pendingOperationPrintHtml) {
        val html = uiState.pendingOperationPrintHtml ?: return@LaunchedEffect
        HtmlPrintHelper.printHtml(context, html, "操業度", PrintPageLayout.A4_PORTRAIT_SINGLE)
        viewModel.clearPendingOperationPrintHtml()
    }

    if (uiState.showGenerateConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::cancelGenerate,
            title = { Text("確認") },
            text = { Text("対象月のベースラインを再生成します。既存データは上書きされますがよろしいですか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmGenerate) { Text("生成") } },
            dismissButton = { TextButton(onClick = viewModel::cancelGenerate) { Text("キャンセル") } },
        )
    }

    if (uiState.showDeleteConfirm) {
        val monthLabel = PlanBaselineLogic.formatBaselineMonthLabel(uiState.baselineMonth)
        val message = if (uiState.processName.isNotBlank()) {
            "基準月「$monthLabel」の「${uiState.processName}」ベースラインを削除します。よろしいですか？"
        } else {
            "基準月「$monthLabel」の全工程ベースラインを削除します。よろしいですか？"
        }
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("確認") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::confirmDelete) { Text("削除") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDelete) { Text("キャンセル") } },
        )
    }

    uiState.pendingDeleteAdjustmentIndex?.let { index ->
        val item = uiState.adjustmentItems.getOrNull(index)
        AlertDialog(
            onDismissRequest = viewModel::cancelDeleteAdjustmentRow,
            title = { Text("削除の確認") },
            text = {
                Text(
                    "「${PlanBaselineLogic.formatBaselineDate(item?.planDate)}」・工程「${item?.processName.orEmpty().ifBlank { "未指定" }}」のベースライン行を削除します。よろしいですか？",
                )
            },
            confirmButton = { TextButton(onClick = viewModel::confirmDeleteAdjustmentRow) { Text("削除") } },
            dismissButton = { TextButton(onClick = viewModel::cancelDeleteAdjustmentRow) { Text("キャンセル") } },
        )
    }

    if (uiState.showFixedBaselineDialog) {
        PlanBaselineFixedGenerateDialog(
            processName = uiState.fixedBaselineProcessName,
            weekdayBaseline = uiState.fixedWeekdayBaseline,
            saturdayBaseline = uiState.fixedSaturdayBaseline,
            sundayBaseline = uiState.fixedSundayBaseline,
            loading = uiState.generating,
            onWeekdayChange = viewModel::setFixedWeekday,
            onSaturdayChange = viewModel::setFixedSaturday,
            onSundayChange = viewModel::setFixedSunday,
            onConfirm = viewModel::submitFixedGenerate,
            onDismiss = viewModel::dismissFixedDialog,
        )
    }

    if (uiState.showAdjustmentDialog) {
        PlanBaselineAdjustmentDialog(
            baselineMonth = uiState.adjustmentMonth,
            processName = uiState.adjustmentProcessName,
            monthLabel = PlanBaselineLogic.formatBaselineMonthLabel(uiState.adjustmentMonth),
            items = uiState.adjustmentItems,
            loading = uiState.adjustmentLoading,
            onMonthChange = viewModel::setAdjustmentMonth,
            onProcessChange = viewModel::setAdjustmentProcessName,
            onLoad = viewModel::loadAdjustmentRecords,
            onReset = viewModel::resetAdjustmentForm,
            onQuantityChange = viewModel::setAdjustmentQuantity,
            onSaveRow = viewModel::saveAdjustmentRow,
            onDeleteRow = viewModel::requestDeleteAdjustmentRow,
            onBatchSave = viewModel::batchSaveAdjustments,
            onDismiss = viewModel::closeAdjustmentDialog,
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        PlanBaselinePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PlanBaselineHeroBar(actionLoading = uiState.isLoading, onRefresh = viewModel::loadComparison)
                PlanBaselineActionCard(
                    baselineMonth = uiState.baselineMonth,
                    processName = uiState.processName,
                    generating = uiState.generating,
                    deleting = uiState.deleting,
                    loading = uiState.isLoading,
                    onMonthChange = viewModel::setBaselineMonth,
                    onProcessChange = viewModel::setProcessName,
                    onGenerate = viewModel::requestGenerate,
                    onDelete = viewModel::requestDelete,
                    onEdit = viewModel::openAdjustmentDialog,
                    onSearch = viewModel::loadComparison,
                    onClear = viewModel::resetFilters,
                )
                PlanBaselineSummaryStrip(PlanBaselineLogic.buildSummaryCards(uiState.summary, uiState.comparisonItems))
                PlanBaselineComparisonCard(
                    monthLabel = uiState.resultMonthLabel,
                    totalCount = uiState.totalItemsCount,
                    isLoading = uiState.isLoading,
                    tabs = uiState.processTabs,
                    activeTab = uiState.activeProcessTab,
                    activeItems = uiState.activeTabItems,
                    canExport = uiState.canExportBaselinePdf,
                    exportLoading = uiState.exportPdfLoading,
                    onTabSelect = viewModel::setActiveProcessTab,
                    onExport = viewModel::prepareExportReports,
                    onPrint = viewModel::prepareComparisonPrint,
                )
                PlanBaselineOperationRateCard(
                    month = uiState.operationRateMonth,
                    monthOptions = uiState.operationRateMonthOptions,
                    processCd = uiState.operationRateProcessCd,
                    processOptions = uiState.operationRateProcessOptions,
                    rows = uiState.operationRateRows,
                    loading = uiState.operationRateLoading,
                    onMonthChange = viewModel::setOperationRateMonth,
                    onProcessChange = viewModel::setOperationRateProcessCd,
                    onPrint = viewModel::prepareOperationPrint,
                )
            }
        }
    }
}
