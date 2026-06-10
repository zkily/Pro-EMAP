package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
fun ProductionDataManagementScreen(viewModel: ProductionDataManagementViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val tableData = remember(uiState.rows, uiState.activeTab, uiState.customizeVisibleColumns) {
        if (uiState.rows.isEmpty()) {
            null
        } else {
            ProductionDataManagementLogic.buildTable(
                uiState.rows,
                uiState.activeTab,
                uiState.customizeVisibleColumns,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
        viewModel.fetchData()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.processPrintRequestId) {
        if (uiState.processPrintRequestId == 0L) return@LaunchedEffect
        val payload = viewModel.takePendingPrint()
        if (payload == null || payload.html.isBlank()) {
            viewModel.notifyMessage("印刷データの生成に失敗しました")
            return@LaunchedEffect
        }
        val ok = runCatching {
            HtmlPrintHelper.printHtml(
                context,
                payload.html,
                payload.jobName,
                payload.layout,
            )
        }.getOrDefault(false)
        if (ok) {
            viewModel.notifyMessage(payload.resultMessage)
        } else {
            viewModel.notifyMessage("印刷を開始できませんでした")
        }
    }

    if (uiState.showProcessPrintDateDialog) {
        DataMgmtProcessPrintDateDialog(
            targetDate = uiState.processPrintTargetDate,
            loading = uiState.showProgressDialog,
            onTargetDateChange = viewModel::setProcessPrintTargetDate,
            onDismiss = viewModel::closeProcessPrintDateDialog,
            onConfirm = viewModel::confirmProcessPrint,
        )
    }

    if (uiState.showGenerateConfirm) {
        DataMgmtGenerateConfirmDialog(
            startDate = uiState.generateStartDate,
            endDate = uiState.generateEndDate,
            loading = false,
            onStartDateChange = viewModel::setGenerateStartDate,
            onEndDateChange = viewModel::setGenerateEndDate,
            onDismiss = viewModel::closeGenerateConfirmDialog,
            onConfirm = viewModel::confirmGenerateData,
        )
    }

    if (uiState.showAllUpdateConfirm) {
        DataMgmtAllUpdateConfirmDialog(
            steps = DataMgmtUpdateConfig.allUpdateSteps,
            loading = uiState.actionLoading,
            onDismiss = viewModel::closeAllUpdateConfirm,
            onConfirm = viewModel::confirmAllUpdate,
        )
    }

    uiState.simpleConfirmAction?.let { action ->
        DataMgmtUpdateConfig.simpleConfirmSpecs[action]?.let { spec ->
            DataMgmtSimpleConfirmDialog(
                title = spec.title,
                message = spec.message,
                confirmLabel = spec.confirmLabel,
                loading = uiState.actionLoading,
                onDismiss = viewModel::closeSimpleConfirm,
                onConfirm = viewModel::confirmSimpleUpdate,
            )
        }
    }

    if (uiState.showPlanConfirm) {
        DataMgmtPlanConfirmDialog(
            loading = uiState.actionLoading,
            onDismiss = viewModel::closePlanConfirm,
            onConfirm = viewModel::confirmPlanUpdate,
        )
    }

    if (uiState.showInventoryTrendConfirm) {
        DataMgmtInventoryTrendConfirmDialog(
            loading = uiState.actionLoading,
            onDismiss = viewModel::closeInventoryTrendConfirm,
            onConfirm = viewModel::confirmInventoryTrendUpdate,
        )
    }

    if (uiState.showProductMasterDialog) {
        DataMgmtProductMasterUpdateDialog(
            startDate = uiState.productMasterStartDate,
            endDate = uiState.productMasterEndDate,
            loading = uiState.actionLoading,
            onStartDateChange = viewModel::setProductMasterStartDate,
            onEndDateChange = viewModel::setProductMasterEndDate,
            onDismiss = viewModel::closeProductMasterDialog,
            onConfirm = viewModel::confirmProductMasterUpdate,
        )
    }

    if (uiState.showMachineDialog) {
        DataMgmtMachineUpdateDialog(
            startDate = uiState.machineStartDate,
            endDate = uiState.machineEndDate,
            loading = uiState.actionLoading,
            onStartDateChange = viewModel::setMachineStartDate,
            onEndDateChange = viewModel::setMachineEndDate,
            onDismiss = viewModel::closeMachineDialog,
            onConfirm = viewModel::confirmMachineUpdate,
        )
    }

    if (uiState.showBatchInitialDialog) {
        DataMgmtBatchInitialStockDialog(
            month = uiState.batchInitialMonth,
            processCd = uiState.batchInitialProcessCd,
            processOptions = uiState.processOptions,
            rows = uiState.batchInitialRows,
            loading = uiState.batchInitialLoading,
            saving = uiState.batchInitialSaving,
            onMonthChange = viewModel::setBatchInitialMonth,
            onProcessChange = viewModel::setBatchInitialProcessCd,
            onQuantityChange = viewModel::setBatchInitialQuantity,
            onSearch = viewModel::searchBatchInitialStock,
            onSave = viewModel::saveBatchInitialStock,
            onDismiss = viewModel::closeBatchInitialDialog,
        )
    }

    uiState.planCreateKind?.let { planKind ->
        if (uiState.showPlanCreateDialog) {
            PlanCreateMainDialog(
                kind = planKind,
                form = uiState.planCreateForm,
                results = uiState.planCreateForm.results,
                loading = uiState.planCreateLoading,
                clearLoading = uiState.planCreateClearLoading,
                inventoryTrendLoading = uiState.planCreateInventoryTrendLoading,
                onMonthChange = viewModel::setPlanCreateMonth,
                onBaseDateChange = viewModel::setPlanCreateBaseDate,
                onWorkingDaysChange = viewModel::setPlanCreateWorkingDays,
                onCoefficientChange = viewModel::setPlanCreateCoefficient,
                onClearFromDateChange = viewModel::setPlanCreateClearFromDate,
                onOpenMachineConfig = viewModel::openPlanMachineConfigDialog,
                onOpenBom = viewModel::openPlanBomDialog,
                onExecuteCreate = viewModel::executePlanCreate,
                onRequestClear = viewModel::requestPlanCreateClear,
                onRequestInventoryTrend = viewModel::requestPlanCreateInventoryTrend,
                onPrint = viewModel::printPlanCreateResult,
                onDismiss = viewModel::closePlanCreateDialog,
            )
        }
        if (uiState.showPlanCreateClearConfirm) {
            PlanCreateClearConfirmDialog(
                kind = planKind,
                startDate = uiState.planCreateForm.clearFromDate,
                loading = uiState.planCreateClearLoading,
                onDismiss = viewModel::closePlanCreateClearConfirm,
                onConfirm = viewModel::confirmPlanCreateClear,
            )
        }
        if (uiState.showPlanCreateInventoryTrendConfirm) {
            DataMgmtInventoryTrendConfirmDialog(
                loading = uiState.planCreateInventoryTrendLoading,
                onDismiss = viewModel::closePlanCreateInventoryTrendConfirm,
                onConfirm = viewModel::confirmPlanCreateInventoryTrend,
            )
        }
        if (uiState.showPlanMachineConfigDialog) {
            PlanCreateMachineConfigDialog(
                kind = planKind,
                rows = uiState.planMachineConfigRows,
                machineOptions = uiState.planMachineOptions,
                loading = uiState.planMachineConfigLoading,
                savingId = uiState.planMachineConfigSavingId,
                onMachineChange = viewModel::updatePlanMachineConfig,
                onDismiss = viewModel::closePlanMachineConfigDialog,
            )
        }
        if (uiState.showPlanBomDialog) {
            PlanCreateBomDialog(
                kind = planKind,
                rows = uiState.planBomRows,
                loading = uiState.planBomLoading,
                bulkField = uiState.planBomBulkField,
                bulkLoading = uiState.planBomBulkLoading,
                selected = uiState.planBomSelected,
                onBulkFieldChange = viewModel::setPlanBomBulkField,
                onToggleSelect = viewModel::togglePlanBomSelect,
                onSelectAll = viewModel::selectAllPlanBom,
                onSafetyChange = viewModel::setPlanBomSafety,
                onProcessLtChange = viewModel::setPlanBomProcessLt,
                onBulkDelta = viewModel::applyPlanBomBulkDelta,
                onDismiss = viewModel::closePlanBomDialog,
            )
        }
    }

    if (uiState.showBatchActualDialog) {
        DataMgmtBatchActualDialog(
            date = uiState.batchActualDate,
            rows = uiState.batchActualRows,
            productOptions = uiState.productOptions,
            saving = uiState.batchActualSaving,
            onDateChange = viewModel::setBatchActualDate,
            onProductChange = viewModel::setBatchActualProduct,
            onCuttingChange = viewModel::setBatchActualCutting,
            onChamferingChange = viewModel::setBatchActualChamfering,
            onMoldingChange = viewModel::setBatchActualMolding,
            onReset = viewModel::resetBatchActualRows,
            onSave = viewModel::submitBatchActual,
            onDismiss = viewModel::closeBatchActualDialog,
        )
    }

    if (uiState.showProgressDialog) {
        DataMgmtProgressDialog(
            title = uiState.progressDialogTitle,
            progressText = uiState.progressText,
            progressPercentage = uiState.progressPercentage,
            status = uiState.progressStatus,
        )
    }

    if (uiState.showColumnSettings) {
        DataMgmtColumnSettingsDialog(
            visibleColumns = uiState.customizeVisibleColumns,
            onToggle = viewModel::setColumnVisible,
            onSelectAll = viewModel::selectAllColumns,
            onReset = viewModel::resetColumnSettings,
            onDismiss = viewModel::closeColumnSettings,
            onSave = viewModel::closeColumnSettings,
        )
    }

    if (uiState.showInventoryStagnation) {
        InventoryStagnationDrawer(
            asOfDate = uiState.stagnationAsOfDate,
            minQuantity = uiState.stagnationMinQuantity,
            stableDays = uiState.stagnationStableDays,
            isLoading = uiState.stagnationLoading,
            rows = uiState.stagnationRows,
            meta = uiState.stagnationMeta,
            onAsOfDateChange = viewModel::setStagnationAsOfDate,
            onMinQuantityChange = viewModel::setStagnationMinQuantity,
            onStableDaysChange = viewModel::setStagnationStableDays,
            onFilterProduct = viewModel::filterProductFromStagnation,
            onPrint = {
                if (uiState.stagnationRows.isEmpty()) {
                    viewModel.notifyMessage("印刷対象データがありません")
                } else {
                    val html = InventoryStagnationLogic.buildPrintHtml(
                        asOfDate = uiState.stagnationAsOfDate,
                        minQuantity = uiState.stagnationMinQuantity,
                        stableDays = uiState.stagnationStableDays,
                        rows = uiState.stagnationRows,
                    )
                    val ok = HtmlPrintHelper.printHtml(
                        context,
                        html,
                        "在庫停滞監視",
                        PrintPageLayout.A4_PORTRAIT_SINGLE,
                    )
                    if (!ok) viewModel.notifyMessage("印刷を開始できませんでした")
                }
            },
            onDismiss = viewModel::closeInventoryStagnation,
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        ProductionPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DataMgmtPageHeader(
                    total = uiState.total,
                    actionLoading = uiState.isLoading || uiState.actionLoading,
                    onToggleUpdateMenu = viewModel::toggleUpdateMenu,
                    onToggleRecommendedPrint = viewModel::toggleRecommendedPrintMenu,
                    onToggleProductionPlan = viewModel::toggleProductionPlanMenu,
                    onProcessPrint = viewModel::openProcessPrintDialog,
                    onInventoryStagnation = viewModel::openInventoryStagnation,
                    onColumnSettings = viewModel::openColumnSettings,
                    columnSettingsEnabled = uiState.activeTab == ProductionDataTab.ColumnCustomize,
                    updateMenuExpanded = uiState.showUpdateMenu,
                    recommendedMenuExpanded = uiState.showRecommendedPrintMenu,
                    productionPlanMenuExpanded = uiState.showProductionPlanMenu,
                    onCloseUpdateMenu = viewModel::closeUpdateMenu,
                    onCloseRecommendedMenu = viewModel::closeRecommendedPrintMenu,
                    onCloseProductionPlanMenu = viewModel::closeProductionPlanMenu,
                    onUpdateAction = viewModel::runUpdateAction,
                    onPrintAction = viewModel::runPrintAction,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    DataMgmtFilterCard(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        productCd = uiState.productCd,
                        productOptions = uiState.productOptions,
                        onStartDateChange = viewModel::setStartDate,
                        onEndDateChange = viewModel::setEndDate,
                        onProductSelect = viewModel::setProductCd,
                        onProductClear = viewModel::clearProductCd,
                        onPrevDay = { viewModel.shiftDateRange(-1) },
                        onToday = viewModel::applyTodayRange,
                        onNextDay = { viewModel.shiftDateRange(1) },
                    )

                    DataMgmtTabRow(
                        tabs = ProductionDataTab.entries,
                        selected = uiState.activeTab,
                        onSelect = viewModel::setActiveTab,
                    )

                    DataMgmtTableCard(
                        isLoading = uiState.isLoading,
                        modifier = Modifier.weight(1f),
                    ) {
                        when {
                            uiState.rows.isEmpty() && !uiState.isLoading ->
                                Text("データがありません", modifier = Modifier.padding(16.dp))
                            tableData != null -> {
                                ProductionDataManagementTable(
                                    data = tableData,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
