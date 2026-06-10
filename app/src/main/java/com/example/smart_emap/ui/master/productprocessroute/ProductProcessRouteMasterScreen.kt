package com.example.smart_emap.ui.master.productprocessroute

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.master.MasterPageScaffold
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ProductProcessRouteMasterScreen(viewModel: ProductProcessRouteMasterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val rightScroll = rememberScrollState()

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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(productRoutePageBackground)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            ) {
                val wide = maxWidth >= 720.dp
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ProductRoutePageHeader(uiState.selectedProductCd.takeIf { it.isNotBlank() })
                    if (wide) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            ProductRouteProductListPanel(
                                keyword = uiState.productKeyword,
                                products = uiState.products,
                                selectedProductCd = uiState.selectedProductCd,
                                loading = uiState.isLoading,
                                page = uiState.productPage,
                                total = uiState.productTotal,
                                pageSize = uiState.productPageSize,
                                onKeywordChange = viewModel::setProductKeyword,
                                onSelect = viewModel::selectProduct,
                                onPageChange = viewModel::setProductPage,
                                modifier = Modifier.width(220.dp),
                            )
                            ProductRouteRightPanel(
                                uiState = uiState,
                                viewModel = viewModel,
                                scroll = rightScroll,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    } else {
                        ProductRouteProductListPanel(
                            keyword = uiState.productKeyword,
                            products = uiState.products,
                            selectedProductCd = uiState.selectedProductCd,
                            loading = uiState.isLoading,
                            page = uiState.productPage,
                            total = uiState.productTotal,
                            pageSize = uiState.productPageSize,
                            onKeywordChange = viewModel::setProductKeyword,
                            onSelect = viewModel::selectProduct,
                            onPageChange = viewModel::setProductPage,
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                        )
                        ProductRouteRightPanel(
                            uiState = uiState,
                            viewModel = viewModel,
                            scroll = rightScroll,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    if (uiState.showProcessDialog) {
        ProductProcessRouteProcessSelectDialog(
            processes = uiState.processOptions,
            loading = uiState.actionLoading,
            onSelect = viewModel::addProcess,
            onDismiss = viewModel::closeProcessDialog,
        )
    }

    uiState.pendingDeleteStepIndex?.let { index ->
        val step = uiState.steps.getOrNull(index)
        AlertDialog(
            onDismissRequest = viewModel::cancelRemoveStep,
            title = { Text("削除確認") },
            text = { Text("工程ステップ「${step?.processName.orEmpty()}」を削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmRemoveStep) { Text("確定") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRemoveStep) { Text("キャンセル") } },
        )
    }

    uiState.pendingDeleteMachine?.let { (stepId, mIndex) ->
        val step = uiState.steps.find { it.localId == stepId }
        val machine = step?.machines?.getOrNull(mIndex)
        AlertDialog(
            onDismissRequest = viewModel::cancelRemoveMachine,
            title = { Text("削除確認") },
            text = { Text("設備「${machine?.machineName?.ifBlank { machine.machineCd }.orEmpty()}」を削除しますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmRemoveMachine) { Text("確定") } },
            dismissButton = { TextButton(onClick = viewModel::cancelRemoveMachine) { Text("キャンセル") } },
        )
    }

    if (uiState.pendingReset) {
        AlertDialog(
            onDismissRequest = viewModel::cancelReset,
            title = { Text("リセット確認") },
            text = { Text("リセットしますか？") },
            confirmButton = { TextButton(onClick = viewModel::confirmReset) { Text("確定") } },
            dismissButton = { TextButton(onClick = viewModel::cancelReset) { Text("キャンセル") } },
        )
    }
}

@Composable
private fun ProductRouteRightPanel(
    uiState: com.example.smart_emap.ui.master.productprocessroute.ProductProcessRouteMasterUiState,
    viewModel: ProductProcessRouteMasterViewModel,
    scroll: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (uiState.selectedProductCd.isBlank()) {
            ProductRouteEmptyState()
        } else {
            uiState.routeInfo?.let { ProductRouteProductInfoPanel(it) }
            ProductRouteStepsPanel(
                steps = uiState.steps,
                loading = uiState.isLoading,
                dataLoaded = uiState.dataLoaded,
                actionLoading = uiState.actionLoading,
                machinesForProcess = viewModel::machinesForProcess,
                onAddProcess = viewModel::openProcessDialog,
                onReset = viewModel::requestReset,
                onSaveSteps = viewModel::saveSteps,
                onMoveUp = viewModel::moveStepUp,
                onMoveDown = viewModel::moveStepDown,
                onRemoveStep = viewModel::requestRemoveStep,
                onAddMachine = viewModel::addMachine,
                onMachineCdChange = viewModel::updateMachineCd,
                onProcessTimeChange = viewModel::updateMachineProcessTime,
                onSetupTimeChange = viewModel::updateMachineSetupTime,
                onSaveMachine = viewModel::saveMachine,
                onRemoveMachine = viewModel::requestRemoveMachine,
            )
        }
    }
}
