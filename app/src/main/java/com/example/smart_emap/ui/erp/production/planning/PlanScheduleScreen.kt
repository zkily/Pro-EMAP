package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun PlanScheduleScreen(viewModel: PlanScheduleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val ok = HtmlPrintHelper.printHtml(context, html, "生産スケジュール", PrintPageLayout.A3_LANDSCAPE_SINGLE)
        if (!ok) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
        viewModel.clearPendingPrintHtml()
    }

    uiState.planUpdatesTitle?.let { title ->
        PlanSchedulePlanUpdatesDialog(
            title = title,
            rows = uiState.planUpdatesDisplay,
            loading = uiState.planUpdatesLoading,
            onDismiss = viewModel::closePlanUpdates,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
    ) { padding ->
        PlanSchedulePageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                PlanScheduleHeader(
                    totalFetched = uiState.enrichedRows.size,
                    shown = uiState.displayRows.size,
                    loading = uiState.isLoading,
                    printEnabled = uiState.displayRows.isNotEmpty(),
                    onPrint = viewModel::preparePrint,
                    onQuery = viewModel::fetchData,
                )
                PlanScheduleFilterBar(
                    filterMonth = uiState.filterMonth,
                    filterEngineering = uiState.filterEngineering,
                    filterMachineName = uiState.filterMachineName,
                    filterProductName = uiState.filterProductName,
                    machineOptions = uiState.machineOptions,
                    productOptions = uiState.productOptions,
                    onMonthChange = viewModel::setFilterMonth,
                    onEngineeringChange = viewModel::setFilterEngineering,
                    onMachineChange = viewModel::setFilterMachineName,
                    onProductChange = viewModel::setFilterProductName,
                )
                PlanScheduleGroupedContent(
                    sections = uiState.groupedSections,
                    varianceMap = uiState.varianceMap,
                    loading = uiState.isLoading,
                    onProductClick = viewModel::openPlanUpdates,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
