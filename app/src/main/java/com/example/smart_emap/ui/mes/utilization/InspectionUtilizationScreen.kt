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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.production.planning.ProductionPageBackground
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun InspectionUtilizationScreen(
    viewModel: InspectionUtilizationViewModel,
    onNavigate: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    var overrideExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        ProductionPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IuaHeroBar(
                    standardHours = uiState.standardHours,
                    rangeLabel = uiState.rangeLabel,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::loadAnalysis,
                )

                IuaToolbarCard(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    filterInspectorId = uiState.filterInspectorId,
                    inspectorOptions = uiState.inspectorOptions,
                    includeIncomplete = uiState.includeIncomplete,
                    loading = uiState.isLoading,
                    onDateRangeChange = viewModel::setDateRange,
                    onInspectorChange = viewModel::setFilterInspectorId,
                    onIncludeIncompleteChange = viewModel::setIncludeIncomplete,
                    onAnalyze = viewModel::loadAnalysis,
                )

                uiState.analysisData?.let { data ->
                    IuaCalendarBanner(
                        calendarWorkdays = data.calendarWorkdaysInRange,
                        extraWorkdaysCount = data.companyCalendarExtraWorkdays?.size ?: 0,
                        holidaysCount = data.companyCalendarHolidays?.size ?: 0,
                        onOpenCalendar = { onNavigate("/master/company-work-calendar") },
                    )
                }

                IuaOverrideCard(
                    expanded = overrideExpanded,
                    onToggle = { overrideExpanded = !overrideExpanded },
                    extraWorkdays = uiState.extraWorkdays,
                    extraHolidays = uiState.extraHolidays,
                    onAddExtraWorkday = viewModel::addExtraWorkday,
                    onRemoveExtraWorkday = viewModel::removeExtraWorkday,
                    onAddExtraHoliday = viewModel::addExtraHoliday,
                    onRemoveExtraHoliday = viewModel::removeExtraHoliday,
                )

                uiState.analysisData?.dataGaps?.let { IuaDataGapsBanner(it) }

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
                                IuaDailyChartCard(uiState.analysisData!!.daily.orEmpty())
                                IuaInspectorSummaryCard(uiState.analysisData!!.byInspector.orEmpty())
                                IuaDailyDetailCard(uiState.filteredDailyRows)
                            }
                        }
                        !uiState.isLoading -> IuaEmptyState()
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
