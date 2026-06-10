package com.example.smart_emap.ui.erp.production.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ProcessMachinePlanDataDto
import com.example.smart_emap.data.model.ProcessMachinePlanRowDto
import com.example.smart_emap.data.model.ProcessMachineProductsDataDto
import com.example.smart_emap.data.repository.ProductionSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProcessMachineViewMode(val label: String) {
    Summary("対比集計"),
    Daily("日別明細"),
    Trend("達成率"),
}

data class ProcessMachinePlanUiState(
    val isLoading: Boolean = false,
    val startDate: String = ProcessMachinePlanLogic.currentMonthRange().first,
    val endDate: String = ProcessMachinePlanLogic.currentMonthRange().second,
    val selectedProcesses: Set<String> = emptySet(),
    val selectedMachines: Set<String> = emptySet(),
    val viewMode: ProcessMachineViewMode = ProcessMachineViewMode.Summary,
    val dailyMetric: String = "actual",
    val trendGroup: String = "all",
    val data: ProcessMachinePlanDataDto? = null,
    val drillDown: ProcessMachineProductsDataDto? = null,
    val drillTitle: String? = null,
    val drillLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingCsvContent: String? = null,
) {
    val filteredSummary: List<ProcessMachinePlanRowDto>
        get() = ProcessMachinePlanLogic.filterSummary(
            data?.summary.orEmpty(),
            selectedProcesses,
            selectedMachines,
        )

    val machineGroups: List<Pair<String, List<String>>>
        get() = ProcessMachinePlanLogic.machineOptionGroups(data?.summary.orEmpty())

    val summaryTableRows: List<ProcessMachinePlanLogic.TableRow>
        get() = ProcessMachinePlanLogic.buildSummaryTableData(
            data?.processes.orEmpty(),
            filteredSummary,
        )

    val dailyTableRows: List<ProcessMachinePlanLogic.TableRow>
        get() = ProcessMachinePlanLogic.buildDailyTableData(
            data?.processes.orEmpty(),
            filteredSummary,
            data?.dates.orEmpty(),
            dailyMetric,
        )

    val trendDailyRows: List<ProcessMachinePlanLogic.TrendDailyRow>
        get() = ProcessMachinePlanLogic.buildTrendDailyRows(filteredSummary, data?.dates.orEmpty())

    val trendProcessDayRows: List<ProcessMachinePlanLogic.TrendProcessDayRow>
        get() = ProcessMachinePlanLogic.buildTrendProcessDayRows(
            data?.processes.orEmpty(),
            filteredSummary,
            data?.dates.orEmpty(),
        )

    val trendStats: ProcessMachinePlanLogic.TrendStats?
        get() = ProcessMachinePlanLogic.computeTrendStats(trendDailyRows)
}

class ProcessMachinePlanViewModel(
    private val repository: ProductionSummaryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProcessMachinePlanUiState())
    val uiState: StateFlow<ProcessMachinePlanUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val processes = state.selectedProcesses.joinToString(",").ifBlank { null }
            val data = repository.loadProcessMachinePlan(state.startDate, state.endDate, processes)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    data = data,
                    snackbarMessage = if (data == null) "データの取得に失敗しました" else null,
                )
            }
        }
    }

    fun setDateRange(start: String, end: String) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        loadData()
    }

    fun applyThisMonth() {
        val (start, end) = ProcessMachinePlanLogic.currentMonthRange()
        setDateRange(start, end)
    }

    fun applyLastMonth() {
        val (start, end) = ProcessMachinePlanLogic.previousMonthRange()
        setDateRange(start, end)
    }

    fun toggleProcess(key: String) {
        _uiState.update { state ->
            val next = state.selectedProcesses.toMutableSet()
            if (!next.add(key)) next.remove(key)
            state.copy(selectedProcesses = next)
        }
        loadData()
    }

    fun toggleMachine(name: String) {
        _uiState.update { state ->
            val next = state.selectedMachines.toMutableSet()
            if (!next.add(name)) next.remove(name)
            state.copy(selectedMachines = next)
        }
    }

    fun clearMachines() = _uiState.update { it.copy(selectedMachines = emptySet()) }

    fun setViewMode(mode: ProcessMachineViewMode) = _uiState.update { it.copy(viewMode = mode) }

    fun setDailyMetric(metric: String) = _uiState.update { it.copy(dailyMetric = metric) }

    fun setTrendGroup(group: String) = _uiState.update { it.copy(trendGroup = group) }

    fun openDrillDown(row: ProcessMachinePlanRowDto) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    drillLoading = true,
                    drillDown = null,
                    drillTitle = "製品別明細 — ${row.processLabel} / ${row.machine}",
                )
            }
            val state = _uiState.value
            val data = repository.loadProcessMachineProducts(
                state.startDate,
                state.endDate,
                row.processKey,
                row.machine,
            )
            _uiState.update { it.copy(drillLoading = false, drillDown = data) }
        }
    }

    fun closeDrillDown() = _uiState.update { it.copy(drillDown = null, drillLoading = false, drillTitle = null) }

    fun exportExcel() {
        val state = _uiState.value
        if (state.filteredSummary.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "出力するデータがありません") }
            return
        }
        val csv = ProcessMachinePlanLogic.buildExcelCsv(
            viewMode = state.viewMode,
            processes = state.data?.processes.orEmpty(),
            filteredRows = state.filteredSummary,
            dates = state.data?.dates.orEmpty(),
            dailyMetric = state.dailyMetric,
            startDate = state.startDate,
            endDate = state.endDate,
        )
        _uiState.update { it.copy(pendingCsvContent = csv, snackbarMessage = "Excel（CSV）を生成しました") }
    }

    fun exportDrillExcel() {
        val drill = _uiState.value.drillDown ?: return
        val sb = StringBuilder()
        sb.appendLine("製品CD,製品名,計画,実績,差異,達成率,実計,不良,廃棄,不良率")
        drill.products.forEach { p ->
            sb.appendLine(
                listOf(
                    p.productCd,
                    p.productName.orEmpty(),
                    p.plan,
                    p.actual,
                    p.diff,
                    p.achievementRate ?: "",
                    p.actualPlan,
                    p.defect,
                    p.scrap,
                    p.defectRate ?: "",
                ).joinToString(",") { "\"$it\"" },
            )
        }
        _uiState.update { it.copy(pendingCsvContent = sb.toString(), snackbarMessage = "製品別明細を出力しました") }
    }

    fun preparePrint() {
        val state = _uiState.value
        val html = buildProcessMachinePlanPrintHtml(state)
        _uiState.update { it.copy(pendingPrintHtml = html) }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun clearPendingCsv() = _uiState.update { it.copy(pendingCsvContent = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: ProductionSummaryRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProcessMachinePlanViewModel(repository) as T
    }
}
