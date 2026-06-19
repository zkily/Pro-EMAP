package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.layout.Arrangement
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
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialReceivingInspectionScreen(viewModel: MaterialReceivingInspectionViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = "材料受入検品履歴",
            layout = PrintPageLayout.A4_PORTRAIT_SINGLE,
        )
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    uiState.detailItem?.let { item ->
        MaterialInspectionDetailDialog(item = item, onDismiss = viewModel::hideDetail)
    }

    uiState.editingMaster?.let { item ->
        MaterialInspectionQualityEditDialog(
            item = item,
            form = uiState.editForm,
            saving = uiState.savingMaster,
            onFormChange = viewModel::setEditForm,
            onSave = viewModel::saveMasterEdit,
            onDismiss = viewModel::closeMasterEdit,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        PurchasePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MaterialInspectionPageHeader()
                MaterialInspectionTabRow(
                    selectedIndex = uiState.tabIndex,
                    onSelect = viewModel::setTab,
                )
                if (uiState.tabIndex == 0) {
                    MaterialInspectionHistoryCard(
                        supplierOptions = uiState.supplierOptions,
                        selectedSuppliers = uiState.selectedSuppliers,
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        printLoading = uiState.printLoading,
                        isLoading = uiState.isLoading,
                        items = uiState.historyItems,
                        totalCount = uiState.totalCount,
                        page = uiState.page,
                        pageSize = uiState.pageSize,
                        onSupplierChange = viewModel::setSelectedSuppliers,
                        onStartChange = viewModel::setStartDate,
                        onEndChange = viewModel::setEndDate,
                        onPrint = viewModel::printHistory,
                        onDetail = viewModel::showDetail,
                        onPageChange = viewModel::setPage,
                        onPageSizeChange = viewModel::setPageSize,
                    )
                } else {
                    MaterialInspectionStandardsCard(
                        materialNameOptions = uiState.materialNameOptions,
                        selectedMaterialName = uiState.selectedMaterialName,
                        isLoading = uiState.isLoading,
                        items = uiState.filteredMasterItems,
                        onMaterialNameChange = viewModel::setSelectedMaterialName,
                        onEdit = viewModel::openMasterEdit,
                        onStatusChange = viewModel::toggleMasterStatus,
                    )
                }
            }
        }
    }
}
