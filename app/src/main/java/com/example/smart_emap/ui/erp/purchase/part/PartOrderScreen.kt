package com.example.smart_emap.ui.erp.purchase.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PartOrderScreen(viewModel: PartOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val tableScroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val deliveryYmd = uiState.startDate.replace("-", "")
        val jobName = if (deliveryYmd.isNotBlank()) "${deliveryYmd}注文書_丸一鋼管" else "注文書"
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = jobName,
            layout = PrintPageLayout.A4_PORTRAIT_SINGLE,
        )
        if (!opened) {
            snackbarHostState.showSnackbar("印刷を開始できませんでした")
        }
        viewModel.clearPendingPrintHtml()
    }

    if (uiState.showManualOrderDialog) {
        PartManualOrderDialog(
            form = uiState.manualOrderForm,
            partOptions = uiState.partOptions,
            selectedPart = uiState.selectedMasterPart,
            loading = uiState.manualOrderLoading,
            onDateChange = viewModel::setManualOrderDate,
            onPartChange = viewModel::setManualOrderPart,
            onOrderQuantityChange = viewModel::setManualOrderQuantity,
            onRemarksChange = viewModel::setManualOrderRemarks,
            onConfirm = viewModel::confirmManualOrder,
            onDismiss = viewModel::dismissManualOrderDialog,
        )
    }

    if (uiState.showPrintConfirmDialog) {
        PartPrintOrderConfirmDialog(
            form = uiState.printForm,
            orderCount = uiState.printOrderCount,
            loading = uiState.printLoading,
            onRecipientCompanyChange = viewModel::setPrintRecipientCompany,
            onRecipientPersonsChange = viewModel::setPrintRecipientPersons,
            onApproverChange = viewModel::setPrintApprover,
            onIssuerChange = viewModel::setPrintIssuer,
            onNote1Change = viewModel::setPrintNote1,
            onNote2Change = viewModel::setPrintNote2,
            onConfirm = viewModel::confirmPrintOrder,
            onDismiss = viewModel::dismissPrintOrderDialog,
        )
    }

    if (uiState.showDataGenerationDialog) {
        PartDataGenerationDialog(
            startDate = uiState.dataGenStartDate,
            endDate = uiState.dataGenEndDate,
            loading = uiState.actionLoading,
            onStartDateChange = viewModel::setDataGenStartDate,
            onEndDateChange = viewModel::setDataGenEndDate,
            onConfirm = viewModel::confirmDataGeneration,
            onDismiss = viewModel::dismissDataGenerationDialog,
        )
    }

    if (uiState.showSyncMasterConfirm) {
        PartSyncMasterConfirmDialog(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmSyncMaster,
            onDismiss = viewModel::dismissSyncMasterConfirm,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        PurchasePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PartOrderHeroBar(
                    actionLoading = uiState.actionLoading,
                    onSyncMaster = viewModel::syncMaster,
                    onGenerateData = viewModel::openDataGenerationDialog,
                    onCalculateStock = viewModel::calculateStock,
                )
                PartOrderKpiStrip(stats = uiState.stats)
                PartOrderFilterBar(
                    showDateFilter = true,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    keyword = uiState.keyword,
                    supplierOptions = uiState.supplierOptions,
                    selectedSuppliers = uiState.selectedSuppliers,
                    onStartChange = viewModel::setStartDate,
                    onEndChange = viewModel::setEndDate,
                    onShiftDate = viewModel::shiftDateByDays,
                    onToday = viewModel::setTodayRange,
                    onKeywordChange = viewModel::setKeyword,
                    onSupplierChange = viewModel::setSelectedSuppliers,
                    onSearch = viewModel::search,
                )
                PartOrderTablePanel(
                    selectedTab = uiState.tab,
                    onTabSelect = viewModel::setTab,
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(tableScroll),
                    headerActions = if (uiState.tab == PartOrderTab.Order) {
                        {
                            PartOrderTabActionButtons(
                                actionLoading = uiState.actionLoading || uiState.printLoading || uiState.manualOrderLoading,
                                onAddManualOrder = viewModel::openManualOrderDialog,
                                onPrintOrder = viewModel::openPrintOrderDialog,
                            )
                        }
                    } else {
                        null
                    },
                ) {
                    when (uiState.tab) {
                        PartOrderTab.Initial -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                PartOrderInitialStockTable(
                                    items = uiState.stockItems,
                                    onInitialStockChange = viewModel::updateInitialStock,
                                    onAdjustmentChange = viewModel::updateAdjustmentQuantity,
                                )
                            }
                        }
                        PartOrderTab.Usage -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                PartOrderUsageTable(items = uiState.stockItems)
                            }
                        }
                        PartOrderTab.Order -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                PartOrderPurchaseTable(
                                    items = uiState.stockItems,
                                    onOrderChange = viewModel::updateOrderQuantity,
                                    onRemarksChange = viewModel::updateStockRemarks,
                                )
                            }
                        }
                        PartOrderTab.OrderHistory -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                PartOrderHistoryTable(items = uiState.stockItems)
                            }
                        }
                        PartOrderTab.Daily -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                PartOrderStockTable(
                                    items = uiState.stockItems,
                                    onOrderChange = viewModel::updateOrderQuantity,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
