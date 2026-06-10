package com.example.smart_emap.ui.master.product

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
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ProductMasterScreen(viewModel: ProductMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val subject = uiState.pendingPrintSubject ?: "製品マスタ印刷"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        if (!opened) {
            snackbarHostState.showSnackbar("印刷画面を開けませんでした")
        }
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
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProductMasterHeroBar(stats = uiState.stats)
                ProductMasterActionSection(
                    totalCount = uiState.totalCount,
                    hasActiveFilters = uiState.keyword.isNotBlank() || uiState.category.isNotBlank() ||
                        uiState.kind.isNotBlank() || uiState.materialCd.isNotBlank(),
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    listNotEmpty = uiState.totalCount > 0,
                    onColumnSettings = viewModel::openColumnSettings,
                    onCsv = viewModel::exportCsv,
                    onQrPrint = viewModel::openQrPrint,
                    onCuttingPrint = viewModel::printCuttingLength,
                    onScrapCalc = viewModel::recalculateScrapLength,
                    onAdd = viewModel::openCreate,
                )
                ProductMasterFilterGrid(
                    keyword = uiState.keyword,
                    category = uiState.category,
                    kind = uiState.kind,
                    materialCd = uiState.materialCd,
                    materialOptions = uiState.materialOptions,
                    onKeywordChange = viewModel::setKeyword,
                    onCategoryChange = viewModel::setCategory,
                    onKindChange = viewModel::setKind,
                    onMaterialChange = viewModel::setMaterialCd,
                    onClear = viewModel::resetFilters,
                )
                ProductMasterTable(
                    products = uiState.products,
                    loading = uiState.isLoading,
                    visibleColumns = uiState.visibleColumns,
                    page = uiState.page,
                    pageSize = ProductMasterViewModel.PAGE_SIZE,
                    total = uiState.totalCount,
                    onPageChange = viewModel::setPage,
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::deleteProduct,
                )
            }
        }
    }

    if (uiState.showProductTypeSelector) {
        ProductTypeSelectorDialog(
            selectedTypes = uiState.selectedProductTypes,
            onToggleType = viewModel::toggleProductType,
            onConfirm = viewModel::confirmQrPrint,
            onDismiss = viewModel::dismissProductTypeSelector,
        )
    }

    if (uiState.showScrapConfirm) {
        ProductScrapLengthConfirmDialog(
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmScrapCalc,
            onDismiss = viewModel::dismissScrapCalcConfirm,
        )
    }

    if (uiState.showColumnSettings) {
        ProductColumnSettingsDialog(
            draft = uiState.columnSettingsDraft,
            onToggle = viewModel::toggleColumnSettingsDraft,
            onSelectAll = { viewModel.selectAllColumnSettingsDraft(true) },
            onDeselectAll = { viewModel.selectAllColumnSettingsDraft(false) },
            onSave = viewModel::saveColumnSettings,
            onDismiss = viewModel::closeColumnSettings,
        )
    }

    if (uiState.showForm) {
        ProductMasterFormDialog(
            isEdit = uiState.editingProduct != null,
            values = uiState.formValues,
            loading = uiState.actionLoading,
            materialOptions = uiState.materialOptions,
            destinationOptions = uiState.formDestinationOptions,
            routeOptions = uiState.formRouteOptions,
            onValueChange = viewModel::setFormValue,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
        )
    }
}
