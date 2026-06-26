package com.example.smart_emap.ui.mes.productivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.network.NetworkErrorHints
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.CuttingProductivityAnalysisDataDto
import com.example.smart_emap.data.model.CuttingProductivityDailyRowDto
import com.example.smart_emap.data.model.CuttingProductivityProductRankingDto
import com.example.smart_emap.data.model.CuttingProductivitySessionRowDto
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.repository.CuttingRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CuttingProductivityUiState(
    val isLoading: Boolean = false,
    val reportBusy: Boolean = false,
    val startDate: String = CuttingProductivityLogic.defaultDateRange().first,
    val endDate: String = CuttingProductivityLogic.defaultDateRange().second,
    val filterLineName: String = "",
    val filterProductCd: String = "",
    val includeIncomplete: Boolean = false,
    val lineOptions: List<String> = emptyList(),
    val productOptions: List<ErpProductDto> = emptyList(),
    val loadingProducts: Boolean = false,
    val analysisData: CuttingProductivityAnalysisDataDto? = null,
    val rankViewProductCd: String = "",
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    val pendingPrintContentBaseUrl: String? = null,
    val snackbarMessage: String? = null,
    val lastLoadError: String? = null,
) {
    val kpiCards: List<IpaKpiCard> get() = CuttingProductivityLogic.buildKpiCards(analysisData?.summary)
    val productRankList: List<CuttingProductivityProductRankingDto> get() =
        CuttingProductivityLogic.resolveProductRankList(analysisData)
    val selectedProductRanking: CuttingProductivityProductRankingDto? get() {
        val list = productRankList
        if (list.isEmpty()) return null
        return list.find { it.productCd == rankViewProductCd } ?: list.first()
    }
    val podiumOperators get() = CuttingProductivityLogic.podiumOperators(selectedProductRanking)
    val productRankTopOverview get() = productRankList.filter { it.topEfficiencyPerHour != null }
    val rangeLabel: String? get() = CuttingProductivityLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
    val operatorSectionAvgEfficiency: Double? get() =
        CuttingProductivityLogic.operatorSectionAvgEfficiency(analysisData?.byOperator.orEmpty())
    val productSectionTotalQty: Int get() =
        analysisData?.byProduct.orEmpty().sumOf { it.sumActualQty ?: 0 }
    val operatorDisplayRows get() =
        CuttingProductivityLogic.operatorDisplayRows(analysisData?.byOperator.orEmpty())
    val productDisplayRows get() =
        CuttingProductivityLogic.productDisplayRows(analysisData?.byProduct.orEmpty())
}

class CuttingProductivityViewModel(
    private val cuttingRepository: CuttingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CuttingProductivityUiState())
    val uiState: StateFlow<CuttingProductivityUiState> = _uiState.asStateFlow()
    private var debounceJob: Job? = null
    private var analysisJob: Job? = null
    private var analysisRequestSeq = 0

    init {
        viewModelScope.launch {
            loadLines()
            loadProductOptions()
        }
        viewModelScope.launch { loadAnalysisInternal(silent = true) }
    }

    fun refreshAll() = loadAnalysis(silent = false)

    fun onPageEnter() {
        val state = _uiState.value
        if (state.analysisData == null && !state.isLoading) {
            loadAnalysis(silent = true)
        }
    }

    private fun loadLines() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) return
        viewModelScope.launch {
            runCatching {
                cuttingRepository.loadProductivityLines(state.startDate, state.endDate)
            }.onSuccess { list ->
                val current = _uiState.value.filterLineName
                val next = if (current.isNotBlank() && current !in list) "" else current
                _uiState.update { it.copy(lineOptions = list, filterLineName = next) }
            }.onFailure {
                _uiState.update { it.copy(lineOptions = emptyList()) }
            }
        }
    }

    private fun loadProductOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProducts = true) }
            runCatching { cuttingRepository.loadProductivityProducts() }
                .onSuccess { list ->
                    _uiState.update { it.copy(loadingProducts = false, productOptions = list) }
                }
                .onFailure {
                    _uiState.update { it.copy(loadingProducts = false, productOptions = emptyList()) }
                }
        }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        loadLines()
        scheduleLoadAnalysis()
    }

    fun setFilterLineName(line: String) {
        _uiState.update { it.copy(filterLineName = line) }
        scheduleLoadAnalysis()
    }

    fun setFilterProductCd(cd: String) {
        _uiState.update { it.copy(filterProductCd = cd) }
        scheduleLoadAnalysis()
    }

    fun setIncludeIncomplete(value: Boolean) {
        _uiState.update { it.copy(includeIncomplete = value) }
        scheduleLoadAnalysis()
    }

    fun setRankViewProductCd(cd: String) {
        _uiState.update { it.copy(rankViewProductCd = cd) }
    }

    private fun scheduleLoadAnalysis() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(350)
            loadAnalysis(silent = true)
        }
    }

    fun loadAnalysis(silent: Boolean = false) {
        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            loadAnalysisInternal(silent)
        }
    }

    private suspend fun loadAnalysisInternal(silent: Boolean) {
        val seq = ++analysisRequestSeq
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            if (!silent) {
                _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            }
            finishAnalysisLoading(seq)
            return
        }
        _uiState.update { it.copy(isLoading = true, lastLoadError = null) }
        try {
            val result = cuttingRepository.loadProductivityAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                productionLine = state.filterLineName.ifBlank { null },
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = state.includeIncomplete,
            )
            if (seq != analysisRequestSeq) return
            result.fold(
                onSuccess = { data ->
                    syncRankProductSelection(data)
                    _uiState.update {
                        it.copy(
                            analysisData = data,
                            lastLoadError = null,
                        )
                    }
                },
                onFailure = { e ->
                    val msg = NetworkErrors.formatError(
                        e,
                        e.message ?: "分析データの取得に失敗しました",
                        NetworkErrorHints(
                            ssl = "SSL 连接失败。请将 API 地址改为后端 HTTP（如 http://局域网IP:8010/），勿用 https 或前端端口 5010/3005。",
                            connection = "无法连接服务器，请检查地址、端口与后端是否已启动。",
                            timeout = "连接超时，请检查网络或稍后重试。",
                            server = "服务器暂时不可用，请稍后重试。",
                            noConnection = "无法连接网络，请检查 Wi‑Fi 或移动数据。",
                        ),
                    )
                    _uiState.update {
                        it.copy(
                            analysisData = null,
                            lastLoadError = msg,
                            snackbarMessage = msg,
                        )
                    }
                },
            )
        } finally {
            finishAnalysisLoading(seq)
        }
    }

    private suspend fun finishAnalysisLoading(seq: Int) {
        if (seq != analysisRequestSeq) return
        withContext(NonCancellable) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun handleReportCommand(commandKey: String, printCacheDir: File) {
        val command = CuttingProductivityReportCommand.entries.firstOrNull { it.name == commandKey } ?: return
        val state = _uiState.value
        val data = state.analysisData ?: run {
            _uiState.update { it.copy(snackbarMessage = "出力する分析データがありません") }
            return
        }
        val filters = buildReportFilters() ?: run {
            _uiState.update { it.copy(snackbarMessage = "集計期間を指定してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(reportBusy = true) }
            try {
                printCacheDir.mkdirs()
                clearPrintChartCache(printCacheDir)
                val contentBaseUrl = printCacheDir.toFileBaseUrl()
                val html = when (command) {
                    CuttingProductivityReportCommand.PRINT_DAILY_BATCH -> {
                        val items = buildDailyBatchPrintItems(filters, printCacheDir)
                        if (items.isEmpty()) throw IllegalStateException("印刷できる日別データがありません")
                        CuttingProductivityReportLogic.buildDailyBatchPrintHtml(filters, items)
                    }
                    CuttingProductivityReportCommand.PRINT_OPERATOR_PRODUCT_BATCH -> {
                        val batchData = loadAnalysisForBatch(filters)
                        val items = loadOperatorProductBatchItems(batchData.sessions.orEmpty())
                        if (items.isEmpty()) throw IllegalStateException("印刷できるライン別製品データがありません")
                        CuttingProductivityReportLogic.buildOperatorProductBatchPrintHtml(filters, items)
                    }
                    else -> {
                        val dailyChartFileName = renderDailyChartFile(printCacheDir, data.daily.orEmpty())
                        val ctx = buildPrintContext(filters, data, dailyChartFileName)
                        CuttingProductivityReportLogic.buildPrintHtml(command, data, ctx)
                    }
                }
                val layout = when (command) {
                    CuttingProductivityReportCommand.PRINT_DAILY,
                    CuttingProductivityReportCommand.PRINT_DAILY_BATCH,
                    -> PrintPageLayout.A4_LANDSCAPE_SINGLE
                    else -> PrintPageLayout.A4_PORTRAIT_SINGLE
                }
                val title = CuttingProductivityLogic.reportMenuItems()
                    .firstOrNull { it.key == command.name }?.label ?: "切断生産性分析"
                _uiState.update {
                    it.copy(
                        pendingPrintHtml = html,
                        pendingPrintSubject = title,
                        pendingPrintLayout = layout,
                        pendingPrintContentBaseUrl = contentBaseUrl,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = e.message ?: "レポート出力に失敗しました") }
            } finally {
                _uiState.update { it.copy(reportBusy = false) }
            }
        }
    }

    private suspend fun loadAnalysisForBatch(filters: CuttingProductivityReportFilters): CuttingProductivityAnalysisDataDto =
        cuttingRepository.loadProductivityAnalysis(
            startDate = filters.startDate,
            endDate = filters.endDate,
            productionLine = null,
            productCd = _uiState.value.filterProductCd.ifBlank { null },
            includeIncomplete = filters.includeIncomplete,
        ).getOrElse { throw it }

    private suspend fun buildDailyBatchPrintItems(
        filters: CuttingProductivityReportFilters,
        printCacheDir: File,
    ): List<CuttingDailyBatchPrintItem> {
        val state = _uiState.value
        val items = mutableListOf<CuttingDailyBatchPrintItem>()
        var chartIndex = 0
        for (line in state.lineOptions) {
            if (line.isBlank()) continue
            val data = cuttingRepository.loadProductivityAnalysis(
                startDate = filters.startDate,
                endDate = filters.endDate,
                productionLine = line,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = filters.includeIncomplete,
            ).getOrNull() ?: continue
            val daily = data.daily.orEmpty()
            if (daily.isEmpty()) continue
            val chartFileName = renderDailyChartFile(printCacheDir, daily, "cpa_daily_batch_${chartIndex++}.png")
                ?: continue
            items.add(CuttingDailyBatchPrintItem(lineLabel = line, daily = daily, chartFileName = chartFileName))
        }
        return items
    }

    private suspend fun renderDailyChartFile(
        cacheDir: File,
        daily: List<CuttingProductivityDailyRowDto>,
        fileName: String = "cpa_daily_chart.png",
    ): String? = withContext(Dispatchers.Default) {
        val rows = CuttingProductivityLogic.toInspectionDailyRows(daily)
        IpaDailyTrendChartExport.savePngFile(cacheDir, fileName, rows, fontSizeOffset = 1)
    }

    private fun clearPrintChartCache(cacheDir: File) {
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.startsWith("cpa_daily")) {
                file.delete()
            }
        }
    }

    private fun File.toFileBaseUrl(): String {
        val path = absolutePath.replace('\\', '/')
        return if (path.endsWith("/")) "file://$path" else "file://$path/"
    }

    private fun loadOperatorProductBatchItems(
        sessions: List<CuttingProductivitySessionRowDto>,
    ): List<Pair<String, List<CuttingOperatorProductDisplayRow>>> {
        val state = _uiState.value
        return state.lineOptions.mapNotNull { line ->
            if (line.isBlank()) return@mapNotNull null
            val rows = CuttingProductivityLogic.buildOperatorProductRows(sessions, line)
            if (rows.isEmpty()) return@mapNotNull null
            line to rows
        }.sortedByDescending { (_, rows) ->
            rows.sumOf { it.sumNetProductionSec }.let { sec ->
                val qty = rows.sumOf { it.sumActualQty }
                if (qty > 0 && sec > 0) qty / (sec / 3600.0) else 0.0
            }
        }
    }

    private fun buildPrintContext(
        filters: CuttingProductivityReportFilters,
        data: CuttingProductivityAnalysisDataDto,
        dailyChartFileName: String? = null,
    ): CuttingProductivityPrintContext {
        val state = _uiState.value
        return CuttingProductivityPrintContext(
            filters = filters,
            kpiCards = CuttingProductivityLogic.buildKpiCards(data.summary),
            operatorRows = state.operatorDisplayRows,
            productRows = state.productDisplayRows,
            operatorSectionAvgEfficiency = state.operatorSectionAvgEfficiency,
            productSectionTotalQty = state.productSectionTotalQty,
            productRankList = state.productRankList,
            selectedProductRanking = state.selectedProductRanking,
            productRankTopOverview = state.productRankTopOverview,
            dailyChartFileName = dailyChartFileName,
        )
    }

    fun buildReportFilters(): CuttingProductivityReportFilters? {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) return null
        val lineLabel = state.filterLineName.ifBlank { "（すべて）" }
        val productLabel = state.filterProductCd.ifBlank { "（すべて）" }.let { cd ->
            if (cd == "（すべて）") cd
            else {
                val found = state.productOptions.find { it.productCode == cd }
                found?.let { "${it.productCode} · ${it.productName.ifBlank { it.productCode }}" } ?: cd
            }
        }
        return CuttingProductivityReportFilters(
            startDate = state.startDate,
            endDate = state.endDate,
            lineLabel = lineLabel,
            productLabel = productLabel,
            includeIncomplete = state.includeIncomplete,
        )
    }

    private fun syncRankProductSelection(data: CuttingProductivityAnalysisDataDto) {
        val list = CuttingProductivityLogic.resolveProductRankList(data)
        val current = _uiState.value.rankViewProductCd
        val next = when {
            list.isEmpty() -> ""
            list.any { it.productCd == current } -> current
            else -> list.first().productCd
        }
        _uiState.update { it.copy(rankViewProductCd = next) }
    }

    fun varianceLabel(defectCd: String): String = defectCd.trim().ifBlank { "—" }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun clearPendingPrintHtml() = _uiState.update {
        it.copy(
            pendingPrintHtml = null,
            pendingPrintSubject = null,
            pendingPrintContentBaseUrl = null,
        )
    }

    class Factory(
        private val cuttingRepository: CuttingRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CuttingProductivityViewModel(cuttingRepository) as T
    }
}
