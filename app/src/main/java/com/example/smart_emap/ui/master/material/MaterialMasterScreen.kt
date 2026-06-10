package com.example.smart_emap.ui.master.material

import android.content.Intent
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
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canCreate
import com.example.smart_emap.core.auth.canDelete
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.core.auth.canExport
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors
import com.example.smart_emap.ui.shell.LocalCurrentUser

@Composable
fun MaterialMasterScreen(viewModel: MaterialMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val user = LocalCurrentUser.current
    val inventoryModule = OperationModules.INVENTORY
    val canCreate = remember(user.id, user.operationPermissions) { user.canCreate(inventoryModule) }
    val canEdit = remember(user.id, user.operationPermissions) { user.canEdit(inventoryModule) }
    val canDelete = remember(user.id, user.operationPermissions) { user.canDelete(inventoryModule) }
    val canExport = remember(user.id, user.operationPermissions) { user.canExport(inventoryModule) }
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
        val subject = uiState.pendingPrintSubject ?: "材料マスタ印刷"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    LaunchedEffect(uiState.pendingCsvContent) {
        val csv = uiState.pendingCsvContent ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "materials.csv")
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
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MaterialMasterHeroBar(stats = uiState.stats)
                MaterialMasterActionSection(
                    filteredCount = uiState.filteredMaterials.size,
                    totalCount = uiState.allMaterials.size,
                    hasActiveFilters = viewModel.hasActiveFilters(),
                    actionLoading = uiState.actionLoading || uiState.isLoading,
                    canCreate = canCreate,
                    canEdit = canEdit,
                    canExport = canExport,
                    onClear = viewModel::resetFilters,
                    onColumnSettings = viewModel::openColumnSettings,
                    onPrint = viewModel::openPrintSettings,
                    onQrPrint = viewModel::printQrCodes,
                    onCsv = viewModel::exportCsv,
                    onCalcPrice = viewModel::calcSinglePrice,
                    onAdd = viewModel::openCreate,
                )
                MaterialMasterFilterGrid(
                    keyword = uiState.keyword,
                    statusFilter = uiState.statusFilter,
                    materialType = uiState.materialType,
                    supplyClassification = uiState.supplyClassification,
                    usage = uiState.usage,
                    storageLocation = uiState.storageLocation,
                    onKeywordChange = viewModel::setKeyword,
                    onStatusChange = viewModel::setStatusFilter,
                    onMaterialTypeChange = viewModel::setMaterialType,
                    onSupplyClassificationChange = viewModel::setSupplyClassification,
                    onUsageChange = viewModel::setUsage,
                    onStorageLocationChange = viewModel::setStorageLocation,
                )
                MaterialMasterTable(
                    materials = uiState.filteredMaterials,
                    loading = uiState.isLoading,
                    visibleColumns = uiState.visibleColumns,
                    totalCount = uiState.allMaterials.size,
                    statusUpdatingIds = uiState.statusUpdatingIds,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onEdit = viewModel::openEdit,
                    onDelete = viewModel::deleteMaterial,
                    onToggleStatus = viewModel::toggleStatus,
                )
            }
        }
    }

    if (uiState.showColumnSettings) {
        MaterialColumnSettingsDialog(
            draft = uiState.columnSettingsDraft,
            onToggle = viewModel::toggleColumnSettingsDraft,
            onSelectAll = { viewModel.selectAllColumnSettingsDraft(true) },
            onDeselectAll = { viewModel.selectAllColumnSettingsDraft(false) },
            onSave = viewModel::saveColumnSettings,
            onDismiss = viewModel::closeColumnSettings,
        )
    }

    if (uiState.showPrintSettings) {
        MaterialPrintSettingsDialog(
            settings = uiState.printSettingsDraft,
            onChange = viewModel::updatePrintSettingsDraft,
            onConfirm = viewModel::confirmPrint,
            onDismiss = viewModel::closePrintSettings,
        )
    }

    if (uiState.showForm) {
        MaterialMasterFormDialog(
            isEdit = uiState.editingMaterial != null,
            values = uiState.formValues,
            loading = uiState.actionLoading,
            supplierOptions = uiState.supplierOptions,
            onValueChange = viewModel::setFormValue,
            onConfirm = viewModel::saveForm,
            onDismiss = viewModel::closeForm,
        )
    }
}
