package com.example.smart_emap.ui.erp.production.planning

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ProcessMachinePlanScreen(viewModel: ProcessMachinePlanViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val grand = uiState.data?.grandTotal

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val ok = HtmlPrintHelper.printHtml(context, html, "工程別設備別計画", PrintPageLayout.A4_LANDSCAPE_SINGLE)
        if (!ok) snackbarHostState.showSnackbar("印刷を開始できませんでした")
        viewModel.clearPendingPrintHtml()
    }

    LaunchedEffect(uiState.pendingCsvContent) {
        val csv = uiState.pendingCsvContent ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "process_machine_plan.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        context.startActivity(Intent.createChooser(intent, "Excel共有"))
        viewModel.clearPendingCsv()
    }

    if (uiState.drillTitle != null) {
        ProcessMachineDrillDownDialog(
            title = uiState.drillTitle.orEmpty(),
            data = uiState.drillDown,
            loading = uiState.drillLoading,
            onDismiss = viewModel::closeDrillDown,
            onExport = viewModel::exportDrillExcel,
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        ProcessMachinePlanPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProcessMachinePlanToolbar(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    selectedProcesses = uiState.selectedProcesses,
                    selectedMachines = uiState.selectedMachines,
                    machineGroups = uiState.machineGroups,
                    viewMode = uiState.viewMode,
                    loading = uiState.isLoading,
                    printEnabled = uiState.filteredSummary.isNotEmpty(),
                    exportEnabled = uiState.filteredSummary.isNotEmpty(),
                    onDateRangeChange = viewModel::setDateRange,
                    onThisMonth = viewModel::applyThisMonth,
                    onLastMonth = viewModel::applyLastMonth,
                    onToggleProcess = viewModel::toggleProcess,
                    onToggleMachine = viewModel::toggleMachine,
                    onClearMachines = viewModel::clearMachines,
                    onViewModeChange = viewModel::setViewMode,
                    onRefresh = viewModel::loadData,
                    onPrint = viewModel::preparePrint,
                    onExport = viewModel::exportExcel,
                )
                ProcessMachinePlanKpiCards(grand)
                ProcessMachinePlanPanel(
                    title = when (uiState.viewMode) {
                        ProcessMachineViewMode.Summary -> "対比集計"
                        ProcessMachineViewMode.Daily -> "日別明細"
                        ProcessMachineViewMode.Trend -> "達成率トレンド"
                    },
                    hint = when (uiState.viewMode) {
                    ProcessMachineViewMode.Summary -> "設備行タップ → 製品別明細"
                    ProcessMachineViewMode.Daily -> "0は「—」・行タップで明細"
                    ProcessMachineViewMode.Trend -> "棒＝計画/実績　折線＝達成率%"
                    },
                    extraHeader = {
                        when (uiState.viewMode) {
                            ProcessMachineViewMode.Daily -> ProductionTabStrip(
                                listOf("plan" to "計画", "actual" to "実績", "diff" to "差異"),
                                uiState.dailyMetric,
                                viewModel::setDailyMetric,
                            )
                            ProcessMachineViewMode.Trend -> ProductionTabStrip(
                                listOf("all" to "全工程合計", "process" to "工程別"),
                                uiState.trendGroup,
                                viewModel::setTrendGroup,
                            )
                            else -> Unit
                        }
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f),
                ) {
                    when {
                        uiState.filteredSummary.isEmpty() && !uiState.isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("データがありません", color = ProductionPlanningColors.TextSecondary)
                            }
                        }
                        uiState.viewMode == ProcessMachineViewMode.Summary -> {
                            ProcessMachineSummaryTable(
                                rows = uiState.summaryTableRows,
                                onRowClick = viewModel::openDrillDown,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        uiState.viewMode == ProcessMachineViewMode.Daily -> {
                            ProcessMachineDailyTable(
                                rows = uiState.dailyTableRows,
                                dates = uiState.data?.dates.orEmpty(),
                                metric = uiState.dailyMetric,
                                onRowClick = viewModel::openDrillDown,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        else -> {
                            ProcessMachineTrendSection(
                                trendGroup = uiState.trendGroup,
                                stats = uiState.trendStats,
                                dailyRows = uiState.trendDailyRows,
                                processDayRows = uiState.trendProcessDayRows,
                                processColumns = uiState.data?.processes.orEmpty().map { it.key to it.label },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}
