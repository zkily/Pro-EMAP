package com.example.smart_emap.ui.mes.utilization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.network.NetworkErrorHints
import com.example.smart_emap.core.network.NetworkErrors
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.InspectionUtilizationAnalysisDataDto
import com.example.smart_emap.data.repository.InspectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InspectionUtilizationUiState(
    val isLoading: Boolean = false,
    val reportBusy: Boolean = false,
    val startDate: String = InspectionUtilizationLogic.defaultDateRange().first,
    val endDate: String = InspectionUtilizationLogic.defaultDateRange().second,
    val filterInspectorId: Int? = null,
    val includeIncomplete: Boolean = false,
    val inspectorOptions: List<IuaInspectorOption> = emptyList(),
    val analysisData: InspectionUtilizationAnalysisDataDto? = null,
    val snackbarMessage: String? = null,
    val lastLoadError: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
) {
    val standardHours: Double get() = analysisData?.standardWorkdayHours ?: 7.6
    val defaultStandardHours: Double
        get() = analysisData?.defaultStandardWorkdayHours ?: analysisData?.standardWorkdayHours ?: 7.6
    val inspectorScheduleApplied: Boolean get() = analysisData?.inspectorScheduleApplied == true
    val kpiCards: List<IuaKpiCard> get() = InspectionUtilizationLogic.buildKpiCards(
        analysisData?.summary,
        analysisData?.calendarWorkdaysInRange,
    )
    val chartDailyRows: List<IuaChartDailyRow> get() = InspectionUtilizationLogic.buildChartDailyRows(
        analysisData,
        filterInspectorId,
    )
    val chartBadgeLabel: String get() = InspectionUtilizationLogic.chartBadgeLabel(filterInspectorId, inspectorOptions)
    val overtimeChartTotalLabel: String get() = InspectionUtilizationLogic.overtimeChartTotalLabel(chartDailyRows)
    val filteredDailyRows get() = InspectionUtilizationLogic.filterDailyRows(
        analysisData?.dailyByInspector.orEmpty(),
        filterInspectorId,
    )
    val filteredByInspector get() = InspectionUtilizationLogic.filterByInspector(
        analysisData?.byInspector.orEmpty(),
        filterInspectorId,
    )
    val rangeLabel: String? get() = InspectionUtilizationLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
    val dataGaps: List<String> get() = analysisData?.dataGaps.orEmpty()
    val sessionsWithoutTime get() = analysisData?.sessionsWithoutTime.orEmpty()
}

class InspectionUtilizationViewModel(
    private val inspectionRepository: InspectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionUtilizationUiState())
    val uiState: StateFlow<InspectionUtilizationUiState> = _uiState.asStateFlow()
    private var debounceJob: Job? = null
    private var loadJob: Job? = null
    private var analysisRequestSeq = 0

    init {
        loadAnalysis()
    }

    fun refreshAll() = loadAnalysis()

    /** 进入页面时：尚无数据且未在加载则重试。 */
    fun onPageEnter() {
        val state = _uiState.value
        if (state.analysisData == null && !state.isLoading) {
            loadAnalysis()
        }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end, filterInspectorId = null) }
        scheduleLoadAnalysis()
    }

    fun setFilterInspectorId(id: Int?) {
        _uiState.update { it.copy(filterInspectorId = id) }
    }

    fun setIncludeIncomplete(value: Boolean) {
        _uiState.update { it.copy(includeIncomplete = value, filterInspectorId = null) }
        scheduleLoadAnalysis()
    }

    private fun scheduleLoadAnalysis() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(350)
            loadAnalysis()
        }
    }

    fun loadAnalysis() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadAnalysisInternal()
        }
    }

    private suspend fun loadAnalysisInternal() {
        val seq = ++analysisRequestSeq
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            finishAnalysisLoading(seq)
            return
        }
        _uiState.update { it.copy(isLoading = true, lastLoadError = null) }
        try {
            val result = inspectionRepository.loadUtilizationAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                inspectorUserId = null,
                includeIncomplete = state.includeIncomplete,
            )
            if (seq != analysisRequestSeq) return
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            analysisData = data,
                            lastLoadError = null,
                            inspectorOptions = InspectionUtilizationLogic.buildInspectorOptions(data.byInspector.orEmpty()),
                        )
                    }
                },
                onFailure = { e ->
                    val msg = NetworkErrors.formatError(
                        e,
                        e.message ?: "分析データの取得に失敗しました",
                        utilizationNetworkHints(),
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

    private fun utilizationNetworkHints() = NetworkErrorHints(
        ssl = "SSL 连接失败。请将 API 地址改为后端 HTTP（如 http://局域网IP:8010/），勿用 https 或前端端口 5010/3005。",
        connection = "无法连接服务器，请检查地址、端口与后端是否已启动。",
        timeout = "连接超时，请检查网络或稍后重试。",
        server = "服务器暂时不可用，请稍后重试。",
        noConnection = "无法连接网络，请检查 Wi‑Fi 或移动数据。",
    )

    fun handleReportCommand(command: InspectionUtilizationReportCommand) {
        val state = _uiState.value
        val data = state.analysisData ?: run {
            _uiState.update { it.copy(snackbarMessage = "出力する分析データがありません") }
            return
        }
        val filters = buildReportFilters(state) ?: run {
            _uiState.update { it.copy(snackbarMessage = "集計期間を指定してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(reportBusy = true) }
            try {
                val html = when (command) {
                    InspectionUtilizationReportCommand.PRINT_DAILY_BATCH -> {
                        val items = buildDailyBatchItems(data, state.inspectorOptions)
                        if (items.isEmpty()) throw IllegalStateException("印刷できる日別データがありません")
                        InspectionUtilizationReportLogic.buildDailyBatchPrintHtml(filters, items)
                    }
                    else -> {
                        val ctx = buildReportContext(state, data, filters)
                        when (command) {
                            InspectionUtilizationReportCommand.PRINT_FULL ->
                                InspectionUtilizationReportLogic.buildFullPrintHtml(ctx)
                            InspectionUtilizationReportCommand.PRINT_DAILY -> {
                                if (state.chartDailyRows.isEmpty()) {
                                    throw IllegalStateException("印刷できる日別データがありません")
                                }
                                InspectionUtilizationReportLogic.buildDailyPrintHtml(ctx)
                            }
                            InspectionUtilizationReportCommand.PRINT_DAILY_BATCH -> error("unreachable")
                        }
                    }
                }
                val layout = when (command) {
                    InspectionUtilizationReportCommand.PRINT_DAILY,
                    InspectionUtilizationReportCommand.PRINT_DAILY_BATCH,
                    -> PrintPageLayout.A4_LANDSCAPE_SINGLE
                    else -> PrintPageLayout.A4_PORTRAIT_SINGLE
                }
                val title = InspectionUtilizationLogic.reportMenuItems()
                    .firstOrNull { it.command == command }?.label ?: "検査稼働率分析"
                _uiState.update {
                    it.copy(
                        pendingPrintHtml = html,
                        pendingPrintSubject = title,
                        pendingPrintLayout = layout,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = e.message ?: "レポート出力に失敗しました") }
            } finally {
                _uiState.update { it.copy(reportBusy = false) }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun clearPendingPrintHtml() = _uiState.update {
        it.copy(pendingPrintHtml = null, pendingPrintSubject = null)
    }

    private fun buildReportFilters(state: InspectionUtilizationUiState): IuaReportFilters? {
        val start = state.analysisData?.startDate?.take(10) ?: state.startDate.take(10)
        val end = state.analysisData?.endDate?.take(10) ?: state.endDate.take(10)
        if (start.isBlank() || end.isBlank()) return null
        val inspectorLabel = state.filterInspectorId?.let { id ->
            state.inspectorOptions.firstOrNull { it.id == id }?.name ?: "#$id"
        } ?: "（すべて）"
        return IuaReportFilters(
            startDate = start,
            endDate = end,
            inspectorLabel = inspectorLabel,
            includeIncomplete = state.includeIncomplete,
        )
    }

    private fun buildReportContext(
        state: InspectionUtilizationUiState,
        data: InspectionUtilizationAnalysisDataDto,
        filters: IuaReportFilters,
    ): IuaReportContext {
        val chartRows = state.chartDailyRows
        val charts = InspectionUtilizationReportLogic.buildReportCharts(chartRows)
        return IuaReportContext(
            filters = filters,
            kpiCards = state.kpiCards.map { card ->
                IuaReportKpiCard(
                    label = card.label,
                    value = card.value,
                    hint = card.hint,
                    tone = card.tone.name.lowercase(),
                )
            },
            charts = charts,
            inspectorRows = state.filteredByInspector,
            dailyDetailRows = state.filteredDailyRows,
        )
    }

    private fun buildDailyBatchItems(
        data: InspectionUtilizationAnalysisDataDto,
        inspectorOptions: List<IuaInspectorOption>,
    ): List<IuaDailyBatchItem> {
        val items = mutableListOf<IuaDailyBatchItem>()
        val aggDaily = InspectionUtilizationLogic.buildChartDailyRows(data, null)
        if (aggDaily.isNotEmpty()) {
            items.add(
                IuaDailyBatchItem(
                    inspectorUserId = null,
                    inspectorLabel = "検査員合算",
                    chartSvg = InspectionUtilizationReportLogic.buildDailyChartSvg(aggDaily),
                    dayCount = aggDaily.size,
                    avgUtilizationPercent = InspectionUtilizationLogic.avgUtilizationPercent(aggDaily),
                    sumOvertimeMin = InspectionUtilizationLogic.sumOvertimeMinFromDaily(aggDaily),
                ),
            )
        }
        for (insp in inspectorOptions) {
            val daily = InspectionUtilizationLogic.buildChartDailyRows(data, insp.id)
            if (daily.isEmpty()) continue
            items.add(
                IuaDailyBatchItem(
                    inspectorUserId = insp.id,
                    inspectorLabel = insp.name,
                    chartSvg = InspectionUtilizationReportLogic.buildDailyChartSvg(daily),
                    dayCount = daily.size,
                    avgUtilizationPercent = InspectionUtilizationLogic.avgUtilizationPercent(daily),
                    sumOvertimeMin = InspectionUtilizationLogic.sumOvertimeMinFromDaily(daily),
                ),
            )
        }
        return items
    }

    class Factory(
        private val inspectionRepository: InspectionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InspectionUtilizationViewModel(inspectionRepository) as T
    }
}
