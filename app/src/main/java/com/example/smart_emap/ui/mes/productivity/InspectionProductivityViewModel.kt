package com.example.smart_emap.ui.mes.productivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.model.ErpProductDto
import com.example.smart_emap.data.model.InspectionProductivityAnalysisDataDto
import com.example.smart_emap.data.model.InspectionProductivityProductRankingDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.repository.InspectionRepository
import com.example.smart_emap.data.repository.SystemUserRepository
import com.example.smart_emap.core.network.NetworkErrorHints
import com.example.smart_emap.core.network.NetworkErrors
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InspectorProductDialogState(
    val target: InspectorAvgRankRow,
    val scope: InspectorProductScope,
    val rows: List<InspectorProductDisplayRow>,
)

data class InspectionProductivityUiState(
    val isLoading: Boolean = false,
    val reportBusy: Boolean = false,
    val sessionExportBusy: Boolean = false,
    val startDate: String = InspectionProductivityLogic.defaultDateRange().first,
    val endDate: String = InspectionProductivityLogic.defaultDateRange().second,
    val filterInspectorId: Int? = null,
    val filterProductCd: String = "",
    val includeIncomplete: Boolean = false,
    val inspectorOptions: List<UserListItemDto> = emptyList(),
    val productOptions: List<ErpProductDto> = emptyList(),
    val loadingProducts: Boolean = false,
    val defectLabelMap: Map<String, String> = emptyMap(),
    val weldingProductCdSet: Set<String> = emptySet(),
    val weldRankOff: List<InspectorAvgRankRow> = emptyList(),
    val weldRankOn: List<InspectorAvgRankRow> = emptyList(),
    val analysisData: InspectionProductivityAnalysisDataDto? = null,
    val rankViewProductCd: String = "",
    val inspectorProductDialog: InspectorProductDialogState? = null,
    val pendingCsvContent: String? = null,
    val pendingCsvSubject: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingPrintSubject: String? = null,
    val pendingPrintLayout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    val snackbarMessage: String? = null,
    val lastLoadError: String? = null,
) {
    val kpiCards: List<IpaKpiCard> get() = InspectionProductivityLogic.buildKpiCards(analysisData?.summary)
    val productRankList: List<InspectionProductivityProductRankingDto> get() =
        InspectionProductivityLogic.resolveProductRankList(analysisData)
    val selectedProductRanking: InspectionProductivityProductRankingDto? get() {
        val list = productRankList
        if (list.isEmpty()) return null
        return list.find { it.productCd == rankViewProductCd } ?: list.first()
    }
    val podiumInspectors get() = InspectionProductivityLogic.podiumInspectors(selectedProductRanking)
    val productRankTopOverview get() = productRankList.filter { it.topEfficiencyPerHour != null }
    val rangeLabel: String? get() = InspectionProductivityLogic.rangeLabel(
        analysisData?.startDate ?: startDate,
        analysisData?.endDate ?: endDate,
    )
    val inspectorSectionAvgEfficiency: Double? get() =
        InspectionProductivityLogic.inspectorSectionAvgEfficiency(analysisData?.byInspector.orEmpty())
    val productSectionTotalQty: Int get() =
        analysisData?.byProduct.orEmpty().sumOf { it.sumActualQty ?: 0 }
}

class InspectionProductivityViewModel(
    private val inspectionRepository: InspectionRepository,
    private val userRepository: SystemUserRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(InspectionProductivityUiState())
    val uiState: StateFlow<InspectionProductivityUiState> = _uiState.asStateFlow()
    private var debounceJob: Job? = null
    private var analysisJob: Job? = null
    /** Web `analysisRequestSeq`：丢弃过期的分析响应，避免并发/防抖导致旧数据覆盖新数据。 */
    private var analysisRequestSeq = 0

    init {
        viewModelScope.launch {
            loadInspectors()
            loadProductOptions()
            loadDefectLabels()
        }
        // BOM 与主分析并行；BOM 失败不阻塞 KPI / 图表
        viewModelScope.launch { syncWeldingProductCdSet() }
        viewModelScope.launch { loadAnalysisInternal(silent = true) }
    }

    fun refreshAll() {
        loadAnalysis(silent = false)
    }

    /** 切回页面时：若尚无数据且未在加载，则重试。 */
    fun onPageEnter() {
        val state = _uiState.value
        if (state.analysisData == null && !state.isLoading) {
            loadAnalysis(silent = true)
        }
    }

    /**
     * Web `loadWeldingProductFlags`：拉取溶接品目 BOM。
     * 失败时保留已有集合；首次失败时提示用户。
     */
    private suspend fun syncWeldingProductCdSet(): Set<String> {
        val previous = _uiState.value.weldingProductCdSet
        return try {
            val set = inspectionRepository.loadWeldingProductCdSet()
            _uiState.update { recomputeWeldRanks(it.copy(weldingProductCdSet = set)) }
            set
        } catch (_: Exception) {
            if (previous.isNotEmpty()) {
                previous
            } else {
                _uiState.update {
                    it.copy(snackbarMessage = "溶接ランキング用の BOM 取得に失敗しました。他の分析結果は表示されます。")
                }
                emptySet()
            }
        }
    }

    private fun loadInspectors() {
        viewModelScope.launch {
            userRepository.getUsers(status = "active", page = 1, pageSize = 500)
                .onSuccess { res ->
                    _uiState.update { it.copy(inspectorOptions = res.items.orEmpty()) }
                }
        }
    }

    private fun loadProductOptions() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProducts = true) }
            runCatching { inspectionRepository.loadProducts() }
                .onSuccess { list ->
                    _uiState.update { it.copy(loadingProducts = false, productOptions = list) }
                }
                .onFailure {
                    _uiState.update { it.copy(loadingProducts = false, productOptions = emptyList()) }
                }
        }
    }

    private fun loadDefectLabels() {
        viewModelScope.launch {
            runCatching { inspectionRepository.loadDefectItems() }
                .onSuccess { items ->
                    val map = items.associate { item ->
                        val key = item.defectCd.trim().ifBlank { item.id?.toString().orEmpty() }
                        key to item.defectName
                    }
                    _uiState.update { it.copy(defectLabelMap = map) }
                }
        }
    }

    /** Web `buildInspectorAvgRankBySessions` + 溶接 BOM 过滤；分析数据或 BOM 变更时同步重算。 */
    private fun recomputeWeldRanks(state: InspectionProductivityUiState): InspectionProductivityUiState {
        val sessions = state.analysisData?.sessions.orEmpty()
        val welding = state.weldingProductCdSet
        return state.copy(
            weldRankOff = InspectionProductivityLogic.buildInspectorAvgRankBySessions(sessions) { cd ->
                !InspectionProductivityLogic.sessionProductHasWelding(cd, welding)
            },
            weldRankOn = InspectionProductivityLogic.buildInspectorAvgRankBySessions(sessions) { cd ->
                InspectionProductivityLogic.sessionProductHasWelding(cd, welding)
            },
        )
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        scheduleLoadAnalysis()
    }

    fun setFilterInspectorId(id: Int?) {
        _uiState.update { it.copy(filterInspectorId = id) }
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

    fun openInspectorProductDialog(row: InspectorAvgRankRow, scope: InspectorProductScope) {
        val sessions = _uiState.value.analysisData?.sessions.orEmpty()
        val welding = _uiState.value.weldingProductCdSet
        val includeProduct: (String) -> Boolean = when (scope) {
            InspectorProductScope.NoWelding -> { cd -> !InspectionProductivityLogic.sessionProductHasWelding(cd, welding) }
            InspectorProductScope.WithWelding -> { cd -> InspectionProductivityLogic.sessionProductHasWelding(cd, welding) }
        }
        val rows = InspectionProductivityLogic.buildInspectorProductRows(
            sessions,
            InspectionProductivityLogic.inspectorRankRowKey(row),
            includeProduct,
        )
        _uiState.update {
            it.copy(inspectorProductDialog = InspectorProductDialogState(row, scope, rows))
        }
    }

    fun closeInspectorProductDialog() {
        _uiState.update { it.copy(inspectorProductDialog = null) }
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
            val result = inspectionRepository.loadProductivityAnalysis(
                startDate = state.startDate,
                endDate = state.endDate,
                inspectorUserId = state.filterInspectorId,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = state.includeIncomplete,
            )
            if (seq != analysisRequestSeq) return
            result.fold(
                onSuccess = { data ->
                    syncRankProductSelection(data)
                    _uiState.update {
                        recomputeWeldRanks(
                            it.copy(
                                analysisData = data,
                                lastLoadError = null,
                            ),
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
                        recomputeWeldRanks(
                            it.copy(
                                analysisData = null,
                                lastLoadError = msg,
                                snackbarMessage = msg,
                            ),
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

    fun exportSessionsCsv() {
        val state = _uiState.value
        val data = state.analysisData ?: run {
            _uiState.update { it.copy(snackbarMessage = "出力する分析データがありません") }
            return
        }
        val filters = buildReportFilters() ?: run {
            _uiState.update { it.copy(snackbarMessage = "集計期間を指定してください") }
            return
        }
        _uiState.update {
            it.copy(
                pendingCsvContent = "\uFEFF${InspectionProductivityLogic.buildSessionsCsv(data, filters)}",
                pendingCsvSubject = InspectionProductivityLogic.sessionsCsvFilename(filters),
            )
        }
    }

    fun handleReportCommand(command: InspectionProductivityReportCommand) {
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
                val ctx = buildPrintContext(filters, data)
                val html = when (command) {
                    InspectionProductivityReportCommand.PRINT_DAILY_BATCH -> {
                        val items = loadDailyBatchPrintItems(filters)
                        if (items.isEmpty()) throw IllegalStateException("印刷できる日別データがありません")
                        InspectionProductivityReportLogic.buildDailyBatchPrintHtml(filters, items)
                    }
                    InspectionProductivityReportCommand.PRINT_INSPECTOR_PRODUCT_BATCH -> {
                        val batchData = loadAnalysisForBatch(filters)
                        val items = loadInspectorProductBatchItems(batchData.sessions.orEmpty())
                        if (items.isEmpty()) throw IllegalStateException("印刷できる検査員別製品データがありません")
                        InspectionProductivityReportLogic.buildInspectorProductBatchPrintHtml(filters, items)
                    }
                    InspectionProductivityReportCommand.PRINT_INSPECTOR_METRICS -> {
                        val prepared = InspectionProductivityLogic.prepareInspectorMetricsForDisplay(
                            data.byInspectorMetrics,
                            state.inspectorOptions,
                        ) ?: throw IllegalStateException("印刷できる検査員別指標データがありません")
                        if (prepared.rows.isEmpty()) {
                            throw IllegalStateException("印刷できる検査員別指標データがありません")
                        }
                        InspectionProductivityReportLogic.buildInspectorMetricsPrintHtml(
                            filters,
                            prepared,
                            ctx.kpiCards,
                        )
                    }
                    else -> InspectionProductivityReportLogic.buildPrintHtml(command, data, ctx)
                }
                val layout = when (command) {
                    InspectionProductivityReportCommand.PRINT_DAILY,
                    InspectionProductivityReportCommand.PRINT_DAILY_BATCH,
                    InspectionProductivityReportCommand.PRINT_INSPECTOR_METRICS,
                    -> PrintPageLayout.A4_LANDSCAPE_SINGLE
                    else -> PrintPageLayout.A4_PORTRAIT_SINGLE
                }
                val title = InspectionProductivityReportLogic.reportMenuItems()
                    .firstOrNull { it.command == command }?.label ?: "検査生産性分析"
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

    private suspend fun loadAnalysisForBatch(filters: InspectionProductivityReportFilters): InspectionProductivityAnalysisDataDto {
        return inspectionRepository.loadProductivityAnalysis(
            startDate = filters.startDate,
            endDate = filters.endDate,
            inspectorUserId = null,
            productCd = _uiState.value.filterProductCd.ifBlank { null },
            includeIncomplete = filters.includeIncomplete,
        ).getOrElse { throw it }
    }

    private suspend fun loadDailyBatchPrintItems(
        filters: InspectionProductivityReportFilters,
    ): List<Pair<String, List<com.example.smart_emap.data.model.InspectionProductivityDailyRowDto>>> {
        val state = _uiState.value
        val items = mutableListOf<Pair<String, List<com.example.smart_emap.data.model.InspectionProductivityDailyRowDto>>>()
        for (insp in state.inspectorOptions) {
            val id = insp.id ?: continue
            val data = inspectionRepository.loadProductivityAnalysis(
                startDate = filters.startDate,
                endDate = filters.endDate,
                inspectorUserId = id,
                productCd = state.filterProductCd.ifBlank { null },
                includeIncomplete = filters.includeIncomplete,
            ).getOrNull() ?: continue
            val daily = data.daily.orEmpty()
            if (daily.isEmpty()) continue
            val label = insp.displayLabel().ifBlank { insp.username.orEmpty() }
            items.add(label to daily)
        }
        return items
    }

    private fun loadInspectorProductBatchItems(
        sessions: List<com.example.smart_emap.data.model.InspectionProductivitySessionRowDto>,
    ): List<Pair<String, List<InspectorProductDisplayRow>>> {
        val state = _uiState.value
        return state.inspectorOptions.mapNotNull { insp ->
            val id = insp.id ?: return@mapNotNull null
            val rows = InspectionProductivityLogic.buildInspectorProductRows(sessions, id.toString()) { true }
            if (rows.isEmpty()) return@mapNotNull null
            val label = insp.displayLabel().ifBlank { insp.username.orEmpty() }
            label to rows
        }.sortedByDescending { (_, rows) ->
            rows.sumOf { it.sumNetProductionSec }.let { sec ->
                val qty = rows.sumOf { it.sumActualQty }
                if (qty > 0 && sec > 0) qty / (sec / 3600.0) else 0.0
            }
        }
    }

    private fun buildPrintContext(
        filters: InspectionProductivityReportFilters,
        data: InspectionProductivityAnalysisDataDto,
    ): InspectionProductivityPrintContext {
        val state = _uiState.value
        return InspectionProductivityPrintContext(
            filters = filters,
            kpiCards = InspectionProductivityLogic.buildKpiCards(data.summary),
            inspectorRows = data.byInspector.orEmpty(),
            productRows = data.byProduct.orEmpty(),
            inspectorSectionAvgEfficiency = state.inspectorSectionAvgEfficiency,
            productSectionTotalQty = state.productSectionTotalQty,
            weldRankOff = state.weldRankOff,
            weldRankOn = state.weldRankOn,
            productRankList = state.productRankList,
            selectedProductRanking = state.selectedProductRanking,
            productRankTopOverview = state.productRankTopOverview,
        )
    }

    fun buildReportFilters(): InspectionProductivityReportFilters? {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) return null
        val inspectorLabel = state.filterInspectorId?.let { id ->
            state.inspectorOptions.find { it.id == id }?.displayLabel()?.ifBlank { null }
                ?: "#$id"
        } ?: "（すべて）"
        val productLabel = state.filterProductCd.ifBlank { "（すべて）" }.let { cd ->
            if (cd == "（すべて）") cd
            else {
                val found = state.productOptions.find { it.productCode == cd }
                found?.let { "${it.productCode} · ${it.productName.ifBlank { it.productCode }}" } ?: cd
            }
        }
        return InspectionProductivityReportFilters(
            startDate = state.startDate,
            endDate = state.endDate,
            inspectorLabel = inspectorLabel,
            productLabel = productLabel,
            includeIncomplete = state.includeIncomplete,
        )
    }

    private fun syncRankProductSelection(data: InspectionProductivityAnalysisDataDto) {
        val list = InspectionProductivityLogic.resolveProductRankList(data)
        val current = _uiState.value.rankViewProductCd
        val next = when {
            list.isEmpty() -> ""
            list.any { it.productCd == current } -> current
            else -> list.first().productCd
        }
        _uiState.update { it.copy(rankViewProductCd = next) }
    }

    fun defectLabel(defectCd: String): String {
        val cd = defectCd.trim()
        return _uiState.value.defectLabelMap[cd] ?: cd
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun clearPendingCsv() = _uiState.update { it.copy(pendingCsvContent = null, pendingCsvSubject = null) }

    fun clearPendingPrintHtml() = _uiState.update {
        it.copy(pendingPrintHtml = null, pendingPrintSubject = null)
    }

    class Factory(
        private val inspectionRepository: InspectionRepository,
        private val userRepository: SystemUserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InspectionProductivityViewModel(inspectionRepository, userRepository) as T
    }
}
