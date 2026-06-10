package com.example.smart_emap.ui.mes.productivity

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
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun InspectionProductivityScreen(
    viewModel: InspectionProductivityViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        IpaPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IpaHeroBar(
                    rangeLabel = uiState.rangeLabel,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::loadAnalysis,
                )

                IpaToolbarCard(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    filterInspectorId = uiState.filterInspectorId,
                    filterProductCd = uiState.filterProductCd,
                    inspectorOptions = uiState.inspectorOptions,
                    productOptions = uiState.productOptions,
                    includeIncomplete = uiState.includeIncomplete,
                    loading = uiState.isLoading,
                    onDateRangeChange = viewModel::setDateRange,
                    onInspectorChange = viewModel::setFilterInspectorId,
                    onProductChange = viewModel::setFilterProductCd,
                    onIncludeIncompleteChange = viewModel::setIncludeIncomplete,
                    onAnalyze = viewModel::loadAnalysis,
                )

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
                            val data = uiState.analysisData!!
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                IpaKpiGrid(uiState.kpiCards)
                                IpaDailyChartCard(data.daily.orEmpty())
                                IpaInspectorProductSplit(
                                    inspectorRows = data.byInspector.orEmpty(),
                                    productRows = data.byProduct.orEmpty(),
                                )
                                IpaProductRankSection(
                                    productRankList = uiState.productRankList,
                                    selectedRanking = uiState.selectedProductRanking,
                                    podiumInspectors = uiState.podiumInspectors,
                                    rankViewProductCd = uiState.rankViewProductCd,
                                    topOverview = uiState.productRankTopOverview,
                                    onProductSelect = viewModel::setRankViewProductCd,
                                    onDetailClick = viewModel::setRankViewProductCd,
                                )
                                IpaDefectSection(
                                    rows = data.defectByItem.orEmpty(),
                                    defectLabel = viewModel::defectLabel,
                                )
                                IpaSessionDetailSection(data.sessions.orEmpty())
                            }
                        }
                        !uiState.isLoading -> IpaEmptyState()
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
