package com.example.smart_emap.ui.erp.production.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.PlanUpdateRecordDto
import com.example.smart_emap.data.repository.ApsSchedulingRepository
import com.example.smart_emap.data.repository.PlanBaselineRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PlanScheduleUiState(
    val isLoading: Boolean = false,
    val filterMonth: Int = LocalDate.now().monthValue,
    val filterEngineering: String = "",
    val filterMachineName: String = "",
    val filterProductName: String = "",
    val enrichedRows: List<PlanScheduleRow> = emptyList(),
    val displayRows: List<PlanScheduleRow> = emptyList(),
    val groupedSections: List<PlanScheduleSection> = emptyList(),
    val varianceMap: Map<String, String> = emptyMap(),
    val machineOptions: List<String> = emptyList(),
    val productOptions: List<String> = emptyList(),
    val planUpdates: List<PlanUpdateRecordDto> = emptyList(),
    val planUpdatesDisplay: List<PlanUpdateDisplayRow> = emptyList(),
    val planUpdatesTitle: String? = null,
    val planUpdatesLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class PlanScheduleViewModel(
    private val apsRepository: ApsSchedulingRepository,
    private val planBaselineRepository: PlanBaselineRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanScheduleUiState())
    val uiState: StateFlow<PlanScheduleUiState> = _uiState.asStateFlow()

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val state = _uiState.value
                val varianceMap = loadVarianceMap(state.filterMonth)
                val (wideStart, wideEnd) = PlanScheduleLogic.buildTableGridRange()
                val ganttDates = PlanScheduleLogic.monthGanttDates(LocalDate.now().year, state.filterMonth)
                val jobs = buildEngineeringJobs(state.filterEngineering)
                val rows = mutableListOf<PlanScheduleRow>()
                if (jobs.isNotEmpty()) {
                    coroutineScope {
                        jobs.map { (engineering, processCd) ->
                            async {
                                loadEngineeringRows(engineering, processCd, wideStart, wideEnd, ganttDates, varianceMap)
                            }
                        }.forEach { rows += it.await() }
                    }
                }
                rows.sortWith(
                    compareBy<PlanScheduleRow> { PlanScheduleLogic.engineeringSortKey(it.engineering) }
                        .thenBy { it.lineLabel }
                        .thenBy { it.orderNo ?: 1_000_000 + it.id }
                        .thenBy { it.id },
                )
                applyRowsState(rows, varianceMap)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "取得に失敗しました") }
            }
        }
    }

    private suspend fun loadVarianceMap(month: Int): Map<String, String> = runCatching {
        planBaselineRepository.loadPlanOperationRate(month, null)
            .associate {
                PlanScheduleLogic.operationVarianceKey(it.displayProcess.orEmpty(), it.machineName.orEmpty()) to
                    it.operationVariance.orEmpty().ifBlank { "—" }
            }
    }.getOrElse { emptyMap() }

    private fun buildEngineeringJobs(engineeringFilter: String): List<Pair<String, String>> {
        val jobs = mutableListOf<Pair<String, String>>()
        if (engineeringFilter.isBlank() || engineeringFilter == "成型") {
            jobs += "成型" to "KT04"
        }
        if (engineeringFilter.isBlank() || engineeringFilter == "溶接") {
            jobs += "溶接" to "KT07"
        }
        return jobs
    }

    private suspend fun loadEngineeringRows(
        engineering: String,
        processCd: String,
        wideStart: String,
        wideEnd: String,
        ganttDates: List<String>,
        varianceMap: Map<String, String>,
    ): List<PlanScheduleRow> {
        val lines = apsRepository.loadLines(processCd)
        val lineNameById = lines.associate { it.id to (it.lineName?.trim().orEmpty().ifBlank { it.lineCode }) }
        val grid = apsRepository.loadSchedulingGrid(wideStart, wideEnd, null, processCd)
        return PlanScheduleLogic.flattenRows(grid.blocks, lineNameById, engineering).map { (row, label) ->
            val variance = varianceMap[PlanScheduleLogic.operationVarianceKey(engineering, label)]
            PlanScheduleLogic.enrichRow(row, label, engineering, ganttDates, variance)
        }
    }

    private fun applyRowsState(allRows: List<PlanScheduleRow>, varianceMap: Map<String, String>) {
        val state = _uiState.value
        val year = LocalDate.now().year
        val monthLabel = PlanScheduleLogic.monthLabel(state.filterMonth)
        val enriched = allRows.filter { PlanScheduleLogic.rowOverlapsMonth(it, year, state.filterMonth) }
        val afterEng = enriched.filter { state.filterEngineering.isBlank() || it.engineering == state.filterEngineering }
        val machineOptions = afterEng.map { it.lineLabel }.distinct().sorted()
        val productOptions = afterEng.map { it.itemName }.distinct().sorted()
        val machineFilter = state.filterMachineName.takeIf { it.isBlank() || machineOptions.contains(it) }.orEmpty()
        val productFilter = state.filterProductName.takeIf { it.isBlank() || productOptions.contains(it) }.orEmpty()
        val display = afterEng.filter { row ->
            (machineFilter.isBlank() || row.lineLabel == machineFilter) &&
                (productFilter.isBlank() || row.itemName == productFilter)
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                enrichedRows = enriched,
                displayRows = display,
                groupedSections = PlanScheduleLogic.groupSections(display, state.filterMonth, monthLabel),
                varianceMap = varianceMap,
                machineOptions = machineOptions,
                productOptions = productOptions,
                filterMachineName = machineFilter,
                filterProductName = productFilter,
            )
        }
    }

    private fun refreshClientFilters() {
        applyRowsState(_uiState.value.enrichedRows, _uiState.value.varianceMap)
    }

    fun setFilterMonth(month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(filterMonth = month) }
            val varianceMap = loadVarianceMap(month)
            _uiState.update { it.copy(varianceMap = varianceMap) }
            refreshClientFilters()
        }
    }

    fun setFilterEngineering(value: String) {
        _uiState.update { it.copy(filterEngineering = value) }
        fetchData()
    }

    fun setFilterMachineName(value: String) {
        _uiState.update { it.copy(filterMachineName = value) }
        refreshClientFilters()
    }

    fun setFilterProductName(value: String) {
        _uiState.update { it.copy(filterProductName = value) }
        refreshClientFilters()
    }

    fun openPlanUpdates(row: PlanScheduleRow) {
        val name = row.itemName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "製品名がありません") }
            return
        }
        val year = LocalDate.now().year
        val month = _uiState.value.filterMonth
        val processName = row.engineering.takeIf { it == "成型" || it == "溶接" }
        val title = buildString {
            append("日生産スケジュール · $name · ${year}年${month}月")
            if (processName != null) append(" · $processName")
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    planUpdatesLoading = true,
                    planUpdatesTitle = title,
                    planUpdates = emptyList(),
                    planUpdatesDisplay = emptyList(),
                )
            }
            runCatching {
                val first = LocalDate.of(year, month, 1)
                val last = first.withDayOfMonth(first.lengthOfMonth())
                val records = planBaselineRepository.loadPlanData(
                    first.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    last.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    name,
                    processName,
                )
                _uiState.update {
                    it.copy(
                        planUpdatesLoading = false,
                        planUpdates = records,
                        planUpdatesDisplay = PlanScheduleLogic.buildPlanUpdateDisplayRows(records),
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        planUpdatesLoading = false,
                        snackbarMessage = e.message ?: "日生産スケジュールの取得に失敗しました",
                    )
                }
            }
        }
    }

    fun closePlanUpdates() = _uiState.update {
        it.copy(planUpdates = emptyList(), planUpdatesDisplay = emptyList(), planUpdatesTitle = null, planUpdatesLoading = false)
    }

    fun preparePrint() {
        val state = _uiState.value
        if (state.displayRows.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        _uiState.update { it.copy(pendingPrintHtml = buildPlanSchedulePrintHtml(state)) }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val apsRepository: ApsSchedulingRepository,
        private val planBaselineRepository: PlanBaselineRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlanScheduleViewModel(apsRepository, planBaselineRepository) as T
    }
}
