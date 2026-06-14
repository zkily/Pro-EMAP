package com.example.smart_emap.ui.mes.utilization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun InspectionUtilizationScreen(
    viewModel: InspectionUtilizationViewModel,
    onNavigate: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onPageEnter()
    }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    LaunchedEffect(uiState.pendingPrintHtml) {
        val html = uiState.pendingPrintHtml ?: return@LaunchedEffect
        val subject = uiState.pendingPrintSubject ?: "検査稼働率分析"
        val opened = HtmlPrintHelper.printHtml(context, html, subject, uiState.pendingPrintLayout)
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        IuaPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IuaHeroBar(
                    defaultStandardHours = uiState.defaultStandardHours,
                    inspectorScheduleApplied = uiState.inspectorScheduleApplied,
                    rangeLabel = uiState.rangeLabel,
                    loading = uiState.isLoading,
                    reportBusy = uiState.reportBusy,
                    reportEnabled = uiState.analysisData != null,
                    dataGaps = uiState.dataGaps,
                    sessionsWithoutTime = uiState.sessionsWithoutTime,
                    onReportCommand = viewModel::handleReportCommand,
                    onRefresh = viewModel::loadAnalysis,
                )

                IuaToolbarCard(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    filterInspectorId = uiState.filterInspectorId,
                    inspectorOptions = uiState.inspectorOptions,
                    includeIncomplete = uiState.includeIncomplete,
                    onDateRangeChange = viewModel::setDateRange,
                    onInspectorChange = viewModel::setFilterInspectorId,
                    onIncludeIncompleteChange = viewModel::setIncludeIncomplete,
                )

                uiState.analysisData?.let { data ->
                    IuaCalendarBanner(
                        calendarWorkdays = data.calendarWorkdaysInRange,
                        extraWorkdaysCount = data.companyCalendarExtraWorkdays?.size ?: 0,
                        holidaysCount = data.companyCalendarHolidays?.size ?: 0,
                        onOpenCalendar = { onNavigate("/master/company-work-calendar") },
                        onOpenSchedule = { onNavigate("/master/inspection-inspector-work-schedule") },
                    )
                }

                Box(Modifier.fillMaxWidth()) {
                    when {
                        uiState.isLoading && uiState.analysisData == null -> {
                            Box(
                                Modifier.fillMaxWidth().padding(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = Color(0xFF059669), strokeWidth = 2.dp)
                            }
                        }
                        uiState.analysisData != null -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                IuaKpiGrid(uiState.kpiCards)
                                IuaDailyChartCard(uiState.chartDailyRows, uiState.chartBadgeLabel)
                                IuaOvertimeChartCard(uiState.chartDailyRows, uiState.overtimeChartTotalLabel)
                                IuaInspectorDailySplit(
                                    inspectorRows = uiState.filteredByInspector,
                                    dailyRows = uiState.filteredDailyRows,
                                )
                            }
                        }
                        !uiState.isLoading -> IuaEmptyState(errorMessage = uiState.lastLoadError)
                    }
                    if (uiState.isLoading && uiState.analysisData != null) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .padding(top = 4.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                color = Color(0xFF059669),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}
