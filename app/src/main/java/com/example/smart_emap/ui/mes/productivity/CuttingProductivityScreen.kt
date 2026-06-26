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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smart_emap.core.system.HtmlPrintHelper
import com.example.smart_emap.ui.shell.LayoutColors
import java.io.File

@Composable
fun CuttingProductivityScreen(
    viewModel: CuttingProductivityViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val printCacheDir = remember(context) { File(context.cacheDir, "cutting_productivity_print") }

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
        val subject = uiState.pendingPrintSubject ?: "切断生産性分析"
        val opened = HtmlPrintHelper.printHtml(
            context = context,
            html = html,
            jobName = subject,
            layout = uiState.pendingPrintLayout,
            contentBaseUrl = uiState.pendingPrintContentBaseUrl ?: "https://smart-emap.local/",
        )
        viewModel.clearPendingPrintHtml()
        if (!opened) snackbarHostState.showSnackbar("印刷画面を開けませんでした")
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = LayoutColors.ShellBg) { padding ->
        IpaPageBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .verticalScroll(scroll),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IpaHeroBarWithReports(
                    rangeLabel = uiState.rangeLabel,
                    loading = uiState.isLoading,
                    reportBusy = uiState.reportBusy,
                    reportEnabled = uiState.analysisData != null,
                    reportEntries = CuttingProductivityLogic.reportMenuItems(),
                    onReportCommand = { key -> viewModel.handleReportCommand(key, printCacheDir) },
                    onRefresh = viewModel::loadAnalysis,
                    pageTitle = "切断工程 — 生産性分析",
                    pageSubtitle = "実績 · 能率 · 不良率 · 稼働",
                )

                CpaLineToolbarCard(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    filterLineName = uiState.filterLineName,
                    filterProductCd = uiState.filterProductCd,
                    lineOptions = uiState.lineOptions,
                    productOptions = uiState.productOptions,
                    onDateRangeChange = viewModel::setDateRange,
                    onLineChange = viewModel::setFilterLineName,
                    onProductChange = viewModel::setFilterProductCd,
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
                                IpaDailyChartCard(
                                    CuttingProductivityLogic.toInspectionDailyRows(data.daily.orEmpty()),
                                    chartFontSizeOffset = 1,
                                )
                                IpaWeldingOperatorProductSplit(
                                    operatorRows = uiState.operatorDisplayRows,
                                    productRows = uiState.productDisplayRows,
                                    operatorCount = data.byOperator.orEmpty().size,
                                    operatorSectionAvgEfficiency = uiState.operatorSectionAvgEfficiency,
                                    productSectionTotalQty = uiState.productSectionTotalQty,
                                    operatorSectionTitle = "ライン別",
                                    operatorCountSuffix = "ライン",
                                    operatorAvgEfficiencyUnit = "本/時",
                                    operatorColumnLabel = "ライン",
                                    defectRateColumnLabel = "不良率",
                                    productDefectRateColumnLabel = "不良率",
                                    efficiencyUnitLabel = "本/時",
                                )
                                WpaProductRankSection(
                                    productRankList = uiState.productRankList,
                                    selectedRanking = uiState.selectedProductRanking,
                                    podiumOperators = uiState.podiumOperators,
                                    rankViewProductCd = uiState.rankViewProductCd,
                                    topOverview = uiState.productRankTopOverview,
                                    onProductSelect = viewModel::setRankViewProductCd,
                                    onDetailClick = viewModel::setRankViewProductCd,
                                    sectionTitle = "製品別 · ライン能率ランキング",
                                    operatorLabel = "ライン",
                                    operatorCountSuffix = "ライン",
                                    defectRateColumnLabel = "不良率",
                                    topOperatorColumnLabel = "TOPライン",
                                    emptyOperatorMessage = "能率を算出できるラインデータがありません",
                                    chartBlockTitle = "ライン別能率",
                                    efficiencyUnitLabel = "本/時",
                                )
                                IpaWeldingDefectSection(
                                    rows = data.defectByItem.orEmpty(),
                                    defectLabel = viewModel::varianceLabel,
                                    sectionTitle = "差異内訳",
                                )
                                WpaSessionDetailSection(
                                    rows = data.sessions.orEmpty(),
                                    operatorColumnLabel = "ライン",
                                    defectQtyColumnLabel = "差異",
                                    defectRateColumnLabel = "不良率",
                                    showMachineColumn = false,
                                )
                            }
                        }
                        !uiState.isLoading -> IpaEmptyState(errorMessage = uiState.lastLoadError)
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
