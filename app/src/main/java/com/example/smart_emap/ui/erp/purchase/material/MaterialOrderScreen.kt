package com.example.smart_emap.ui.erp.purchase.material

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import android.content.Intent
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseEmptyHint
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun MaterialOrderScreen(viewModel: MaterialOrderViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val showDateFilter = uiState.tab != MaterialOrderTab.Sub && uiState.tab != MaterialOrderTab.UnusedReceiving
    val tableScroll = rememberScrollState()
    var deleteSubTarget by remember { mutableStateOf<MaterialStockSubItemDto?>(null) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, "材料注文書")
            putExtra(Intent.EXTRA_TEXT, html)
        }
        context.startActivity(Intent.createChooser(intent, "印刷 / 共有"))
        viewModel.clearPendingPrintHtml()
    }

    if (uiState.showManualOrderDialog) {
        MaterialManualOrderDialog(
            form = uiState.manualOrderForm,
            materialOptions = uiState.materialOptions,
            selectedMaterial = uiState.selectedMasterMaterial,
            loading = uiState.manualOrderLoading,
            onDateChange = viewModel::setManualOrderDate,
            onMaterialChange = viewModel::setManualOrderMaterial,
            onOrderQuantityChange = viewModel::setManualOrderQuantity,
            onOrderBundleQuantityChange = viewModel::setManualOrderBundleQuantity,
            onRemarksChange = viewModel::setManualOrderRemarks,
            onConfirm = viewModel::confirmManualOrder,
            onDismiss = viewModel::dismissManualOrderDialog,
        )
    }

    if (uiState.showPrintConfirmDialog) {
        MaterialPrintOrderConfirmDialog(
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
        MaterialDataGenerationDialog(
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
        MaterialSyncMasterConfirmDialog(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            loading = uiState.actionLoading,
            onConfirm = viewModel::confirmSyncMaster,
            onDismiss = viewModel::dismissSyncMasterConfirm,
        )
    }

    deleteSubTarget?.let { row ->
        AlertDialog(
            onDismissRequest = { deleteSubTarget = null },
            title = { Text("削除確認") },
            text = { Text("材料「${row.materialName.orEmpty()}」のデータを削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubItem(row)
                        deleteSubTarget = null
                    },
                ) {
                    Text("削除", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteSubTarget = null }) {
                    Text("キャンセル")
                }
            },
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
                    .imePadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MaterialOrderHeroBar(
                    actionLoading = uiState.actionLoading,
                    onSyncMaster = viewModel::syncMaster,
                    onGenerateData = viewModel::openDataGenerationDialog,
                    onCalculateStock = viewModel::calculateStock,
                )
                MaterialOrderKpiStrip(stats = uiState.stats)
                MaterialOrderFilterBar(
                    showDateFilter = showDateFilter,
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
                MaterialOrderTablePanel(
                    selectedTab = uiState.tab,
                    onTabSelect = viewModel::setTab,
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(tableScroll),
                    headerActions = if (uiState.tab == MaterialOrderTab.Order) {
                        {
                            MaterialOrderTabActionButtons(
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
                        MaterialOrderTab.UnusedReceiving -> {
                            PurchaseEmptyHint("材料未使用番号は Web 版と同じ API 連携を準備中です")
                        }
                        MaterialOrderTab.Sub -> {
                            if (uiState.subItems.isEmpty()) {
                                Text(
                                    "半端材料データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderSubTable(
                                    items = uiState.subItems,
                                    onUsageChange = viewModel::updateSubUsage,
                                    onRemarksChange = viewModel::updateSubRemarks,
                                    onLabelColorChange = viewModel::updateSubLabelColor,
                                    onDelete = { deleteSubTarget = it },
                                )
                            }
                        }
                        MaterialOrderTab.Initial -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderInitialStockTable(
                                    items = uiState.stockItems,
                                    onInitialStockChange = viewModel::updateInitialStock,
                                    onAdjustmentChange = viewModel::updateAdjustmentQuantity,
                                )
                            }
                        }
                        MaterialOrderTab.Usage -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderUsageTable(
                                    items = uiState.stockItems,
                                    onUsageChange = viewModel::updateUsageQuantity,
                                )
                            }
                        }
                        MaterialOrderTab.Order -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderPurchaseTable(
                                    items = uiState.stockItems,
                                    onOrderChange = viewModel::updateOrderQuantity,
                                    onRemarksChange = viewModel::updateStockRemarks,
                                )
                            }
                        }
                        MaterialOrderTab.OrderHistory -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderHistoryTable(items = uiState.stockItems)
                            }
                        }
                        MaterialOrderTab.Daily -> {
                            if (uiState.stockItems.isEmpty()) {
                                Text(
                                    "在庫データがありません",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color(0xFF94A3B8),
                                )
                            } else {
                                MaterialOrderStockTable(
                                    items = uiState.stockItems,
                                    showTransfer = true,
                                    onUsageChange = viewModel::updateUsageQuantity,
                                    onOrderChange = viewModel::updateOrderQuantity,
                                    onTransfer = viewModel::transferToSub,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

