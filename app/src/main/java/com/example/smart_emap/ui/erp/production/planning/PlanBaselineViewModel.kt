package com.example.smart_emap.ui.erp.production.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.PlanBaselineFullComparisonItemDto
import com.example.smart_emap.data.repository.ApsSchedulingRepository
import com.example.smart_emap.data.repository.MasterRepository
import com.example.smart_emap.data.repository.PlanBaselineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class PlanBaselineUiState(
    val isLoading: Boolean = false,
    val generating: Boolean = false,
    val deleting: Boolean = false,
    val baselineMonth: String = PlanBaselineLogic.defaultBaselineMonth(),
    val processName: String = "",
    val comparisonItems: List<PlanBaselineFullComparisonItemDto> = emptyList(),
    val summary: com.example.smart_emap.data.model.PlanBaselineComparisonDto? = null,
    val resultMonthLabel: String? = null,
    val activeProcessTab: String = "",
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val pendingExportHtml: String? = null,
    val exportPdfLoading: Boolean = false,
    val pendingOperationPrintHtml: String? = null,
    val showGenerateConfirm: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showFixedBaselineDialog: Boolean = false,
    val fixedBaselineProcessName: String = "",
    val fixedWeekdayBaseline: String = "",
    val fixedSaturdayBaseline: String = "",
    val fixedSundayBaseline: String = "",
    val showAdjustmentDialog: Boolean = false,
    val adjustmentLoading: Boolean = false,
    val adjustmentMonth: String = PlanBaselineLogic.defaultBaselineMonth(),
    val adjustmentProcessName: String = "",
    val adjustmentItems: List<PlanBaselineAdjustmentItem> = emptyList(),
    val pendingDeleteAdjustmentIndex: Int? = null,
    val operationRateMonth: String = PlanBaselineUtilizationLogic.defaultOperationRateMonth(),
    val operationRateProcessCd: String = "",
    val operationRateProcessOptions: List<Pair<String, String>> = emptyList(),
    val operationRateLoading: Boolean = false,
    val operationRateRows: List<PlanBaselineUtilizationRow> = emptyList(),
    val operationRateMonthOptions: List<Pair<String, String>> = emptyList(),
) {
    val processTabs: List<PlanBaselineProcessTab>
        get() = PlanBaselineLogic.buildProcessTabs(comparisonItems)

    val activeTabItems: List<PlanBaselineFullComparisonItemDto>
        get() = processTabs.find { it.name == activeProcessTab }?.items
            ?: processTabs.firstOrNull()?.items
            ?: emptyList()

    val totalItemsCount: Int get() = comparisonItems.size

    val canExportBaselinePdf: Boolean
        get() = comparisonItems.isNotEmpty() && !resultMonthLabel.isNullOrBlank()
}

class PlanBaselineViewModel(
    private val repository: PlanBaselineRepository,
    private val apsRepository: ApsSchedulingRepository,
    private val masterRepository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlanBaselineUiState())
    val uiState: StateFlow<PlanBaselineUiState> = _uiState.asStateFlow()

    private suspend fun loadScheduledWorkdaysForBaselineMonth(iso: String): Int? = withContext(Dispatchers.IO) {
        val ym = iso.trim().take(7)
        if (ym.length < 7) return@withContext null
        masterRepository.loadScheduledWorkdaysForMonth(ym)
    }

    init {
        _uiState.update { it.copy(operationRateMonthOptions = buildMonthOptions()) }
        loadProcessOptions()
        loadComparison()
    }

    private fun buildMonthOptions(): List<Pair<String, String>> {
        val now = YearMonth.now()
        return (0 until 24).map { offset ->
            val ym = now.minusMonths(offset.toLong())
            val key = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            key to "${ym.year}年${ym.monthValue}月"
        }
    }

    private fun loadProcessOptions() {
        viewModelScope.launch {
            val processes = apsRepository.loadProcesses()
            val options = processes
                .filter { !it.processCd.isNullOrBlank() }
                .map { (it.processCd.orEmpty()) to "${it.processCd.orEmpty()} — ${it.processName.orEmpty()}" }
            val defaultCd = processes.firstOrNull {
                val name = it.processName.orEmpty()
                it.processCd == "KT04" || name == "成型"
            }?.processCd.orEmpty().ifBlank { options.firstOrNull()?.first.orEmpty() }
            _uiState.update {
                it.copy(
                    operationRateProcessOptions = options,
                    operationRateProcessCd = it.operationRateProcessCd.ifBlank { defaultCd },
                )
            }
            loadOperationRate()
        }
    }

    fun loadComparison() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val result = repository.loadComparison(state.baselineMonth, state.processName.ifBlank { null })
            val tabs = PlanBaselineLogic.buildProcessTabs(result?.items.orEmpty())
            _uiState.update {
                it.copy(
                    isLoading = false,
                    comparisonItems = result?.items.orEmpty(),
                    summary = result?.summary,
                    resultMonthLabel = PlanBaselineLogic.formatBaselineMonthLabel(result?.baselineMonth ?: state.baselineMonth),
                    activeProcessTab = tabs.firstOrNull()?.name.orEmpty(),
                    snackbarMessage = if (result == null) "比較データの取得に失敗しました" else null,
                )
            }
        }
    }

    fun setBaselineMonth(value: String) = _uiState.update { it.copy(baselineMonth = value) }

    fun setProcessName(value: String) = _uiState.update { it.copy(processName = value) }

    fun setActiveProcessTab(value: String) = _uiState.update { it.copy(activeProcessTab = value) }

    fun resetFilters() {
        _uiState.update { it.copy(baselineMonth = PlanBaselineLogic.defaultBaselineMonth(), processName = "") }
        loadComparison()
    }

    fun requestGenerate() = _uiState.update { it.copy(showGenerateConfirm = true) }

    fun cancelGenerate() = _uiState.update { it.copy(showGenerateConfirm = false) }

    fun confirmGenerate() {
        _uiState.update { it.copy(showGenerateConfirm = false) }
        val process = _uiState.value.processName
        if (PlanBaselineLogic.fixedBaselineProcesses.contains(process)) {
            _uiState.update {
                it.copy(
                    showFixedBaselineDialog = true,
                    fixedBaselineProcessName = process,
                    fixedWeekdayBaseline = "",
                    fixedSaturdayBaseline = "",
                    fixedSundayBaseline = "",
                )
            }
            return
        }
        runGenerate()
    }

    private fun runGenerate(
        weekday: Double? = null,
        saturday: Double? = null,
        sunday: Double? = null,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(generating = true) }
            runCatching {
                repository.generate(
                    _uiState.value.baselineMonth,
                    _uiState.value.processName.ifBlank { null },
                    weekdayBaseline = weekday,
                    saturdayBaseline = saturday,
                    sundayBaseline = sunday,
                )
                loadComparison()
                _uiState.update { it.copy(snackbarMessage = "ベースラインを生成しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "生成に失敗しました") }
            }
            _uiState.update { it.copy(generating = false, showFixedBaselineDialog = false) }
        }
    }

    fun setFixedWeekday(v: String) = _uiState.update { it.copy(fixedWeekdayBaseline = v) }
    fun setFixedSaturday(v: String) = _uiState.update { it.copy(fixedSaturdayBaseline = v) }
    fun setFixedSunday(v: String) = _uiState.update { it.copy(fixedSundayBaseline = v) }
    fun dismissFixedDialog() = _uiState.update { it.copy(showFixedBaselineDialog = false) }

    fun submitFixedGenerate() {
        val weekday = _uiState.value.fixedWeekdayBaseline.toDoubleOrNull()
        if (weekday == null || weekday <= 0) {
            _uiState.update { it.copy(snackbarMessage = "平日の基準計画数を入力してください（1以上）") }
            return
        }
        val sat = _uiState.value.fixedSaturdayBaseline.toDoubleOrNull()
        val sun = _uiState.value.fixedSundayBaseline.toDoubleOrNull()
        runGenerate(weekday, sat, sun)
    }

    fun requestDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun confirmDelete() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
        viewModelScope.launch {
            _uiState.update { it.copy(deleting = true) }
            runCatching {
                repository.delete(_uiState.value.baselineMonth, _uiState.value.processName.ifBlank { null })
                loadComparison()
                _uiState.update { it.copy(snackbarMessage = "ベースラインを削除しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除に失敗しました") }
            }
            _uiState.update { it.copy(deleting = false) }
        }
    }

    fun openAdjustmentDialog() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                showAdjustmentDialog = true,
                adjustmentMonth = state.baselineMonth,
                adjustmentProcessName = state.processName,
                adjustmentItems = emptyList(),
            )
        }
        loadAdjustmentRecords()
    }

    fun closeAdjustmentDialog() = _uiState.update { it.copy(showAdjustmentDialog = false) }

    fun setAdjustmentMonth(v: String) = _uiState.update { it.copy(adjustmentMonth = v) }
    fun setAdjustmentProcessName(v: String) = _uiState.update { it.copy(adjustmentProcessName = v) }

    fun resetAdjustmentForm() {
        _uiState.update {
            it.copy(
                adjustmentMonth = _uiState.value.baselineMonth,
                adjustmentProcessName = "",
                adjustmentItems = emptyList(),
            )
        }
    }

    fun loadAdjustmentRecords() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.adjustmentMonth.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "基準月を選択してください") }
                return@launch
            }
            _uiState.update { it.copy(adjustmentLoading = true) }
            runCatching {
                val records = repository.loadRecords(state.adjustmentMonth, state.adjustmentProcessName.ifBlank { null })
                val items = records.map {
                    PlanBaselineAdjustmentItem(
                        planDate = it.planDate.take(10),
                        processName = it.processName,
                        planQuantity = it.planQuantity,
                        tempPlanQuantity = it.planQuantity.toLong().toString(),
                    )
                }
                _uiState.update {
                    it.copy(
                        adjustmentLoading = false,
                        adjustmentItems = items,
                        snackbarMessage = if (items.isEmpty()) "該当データがありません" else null,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(adjustmentLoading = false, snackbarMessage = e.message ?: "取得に失敗しました") }
            }
        }
    }

    fun setAdjustmentQuantity(index: Int, value: String) {
        val items = _uiState.value.adjustmentItems.toMutableList()
        if (index !in items.indices) return
        items[index] = items[index].copy(tempPlanQuantity = value)
        _uiState.update { it.copy(adjustmentItems = items) }
    }

    fun saveAdjustmentRow(index: Int) {
        val state = _uiState.value
        val item = state.adjustmentItems.getOrNull(index) ?: return
        val qty = item.tempPlanQuantity.toDoubleOrNull()
        if (qty == null) {
            _uiState.update { it.copy(snackbarMessage = "数値を入力してください") }
            return
        }
        viewModelScope.launch {
            updateAdjustmentItem(index) { it.copy(saving = true) }
            runCatching {
                repository.updatePlanQuantity(state.adjustmentMonth, item.planDate, item.processName, qty)
                updateAdjustmentItem(index) { it.copy(saving = false, planQuantity = qty) }
                _uiState.update { it.copy(snackbarMessage = "修正しました") }
                loadComparison()
            }.onFailure { e ->
                updateAdjustmentItem(index) { it.copy(saving = false) }
                _uiState.update { it.copy(snackbarMessage = e.message ?: "修正に失敗しました") }
            }
        }
    }

    fun requestDeleteAdjustmentRow(index: Int) = _uiState.update { it.copy(pendingDeleteAdjustmentIndex = index) }
    fun cancelDeleteAdjustmentRow() = _uiState.update { it.copy(pendingDeleteAdjustmentIndex = null) }

    fun confirmDeleteAdjustmentRow() {
        val index = _uiState.value.pendingDeleteAdjustmentIndex ?: return
        val state = _uiState.value
        val item = state.adjustmentItems.getOrNull(index) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingDeleteAdjustmentIndex = null) }
            updateAdjustmentItem(index) { it.copy(deleting = true) }
            runCatching {
                repository.deleteRecord(state.adjustmentMonth, item.planDate, item.processName)
                _uiState.update {
                    it.copy(
                        adjustmentItems = it.adjustmentItems.filterIndexed { i, _ -> i != index },
                        snackbarMessage = "削除しました",
                    )
                }
                loadComparison()
            }.onFailure { e ->
                updateAdjustmentItem(index) { it.copy(deleting = false) }
                _uiState.update { it.copy(snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun batchSaveAdjustments() {
        val state = _uiState.value
        val modified = state.adjustmentItems.withIndex().filter { (_, item) ->
            item.tempPlanQuantity.toDoubleOrNull() != item.planQuantity
        }
        if (modified.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "変更された項目がありません") }
            return
        }
        viewModelScope.launch {
            modified.forEach { (index, item) ->
                val qty = item.tempPlanQuantity.toDoubleOrNull() ?: return@forEach
                runCatching {
                    repository.updatePlanQuantity(state.adjustmentMonth, item.planDate, item.processName, qty)
                    updateAdjustmentItem(index) { it.copy(planQuantity = qty) }
                }
            }
            loadComparison()
            _uiState.update { it.copy(snackbarMessage = "一括で保存しました") }
        }
    }

    private fun updateAdjustmentItem(index: Int, transform: (PlanBaselineAdjustmentItem) -> PlanBaselineAdjustmentItem) {
        val items = _uiState.value.adjustmentItems.toMutableList()
        if (index !in items.indices) return
        items[index] = transform(items[index])
        _uiState.update { it.copy(adjustmentItems = items) }
    }

    fun setOperationRateMonth(v: String) {
        _uiState.update { it.copy(operationRateMonth = v) }
        loadOperationRate()
    }

    fun setOperationRateProcessCd(v: String) {
        _uiState.update { it.copy(operationRateProcessCd = v) }
        loadOperationRate()
    }

    private fun loadOperationRate() {
        viewModelScope.launch {
            val state = _uiState.value
            val processCd = state.operationRateProcessCd
            if (processCd.isBlank()) {
                _uiState.update { it.copy(operationRateRows = emptyList()) }
                return@launch
            }
            val range = PlanBaselineUtilizationLogic.monthRangeFromYm(state.operationRateMonth) ?: return@launch
            _uiState.update { it.copy(operationRateLoading = true) }
            runCatching {
                val grid = apsRepository.loadSchedulingGrid(range.first, range.second, null, processCd)
                val lines = apsRepository.loadLines(processCd)
                val lineNameById = lines.associate { it.id to (it.lineName?.trim().orEmpty().ifBlank { it.lineCode }) }
                val rows = PlanBaselineUtilizationLogic.buildUtilizationRows(grid, lineNameById)
                _uiState.update { it.copy(operationRateLoading = false, operationRateRows = rows) }
            }.onFailure {
                _uiState.update { it.copy(operationRateLoading = false, operationRateRows = emptyList()) }
            }
        }
    }

    fun prepareExportReports() {
        val state = _uiState.value
        val tabs = PlanBaselineLogic.buildPdfExportTabs(state.processTabs)
        if (tabs.isEmpty()) {
            _uiState.update {
                it.copy(snackbarMessage = "工程別報告書を発行する比較データがありません。先に検索を実行してください。")
            }
            return
        }
        viewModelScope.launch {
            val scheduled = loadScheduledWorkdaysForBaselineMonth(state.baselineMonth)
            val monthLabel = state.resultMonthLabel ?: PlanBaselineLogic.formatBaselineMonthLabel(state.baselineMonth)
            val processLabel = state.operationRateProcessOptions.find { it.first == state.operationRateProcessCd }?.second
                ?: state.operationRateProcessCd
            val html = buildBaselineExportHtml(
                monthLabel = monthLabel,
                tabs = tabs,
                operationRateMonthLabel = PlanBaselineUtilizationLogic.utilizationMonthLabelJp(state.operationRateMonth),
                operationRateProcessLabel = processLabel,
                operationRateRows = state.operationRateRows,
                scheduledWorkdays = scheduled,
            )
            _uiState.update { it.copy(pendingExportHtml = html) }
        }
    }

    fun clearPendingExportHtml() = _uiState.update { it.copy(pendingExportHtml = null) }

    fun prepareComparisonPrint() {
        viewModelScope.launch {
            val state = _uiState.value
            val scheduled = loadScheduledWorkdaysForBaselineMonth(state.baselineMonth)
            val tabName = state.activeProcessTab.ifBlank { state.processTabs.firstOrNull()?.name.orEmpty() }
            val html = buildBaselineComparisonPrintHtml(
                processName = tabName,
                monthLabel = state.resultMonthLabel ?: PlanBaselineLogic.formatBaselineMonthLabel(state.baselineMonth),
                items = state.activeTabItems,
                scheduledWorkdays = scheduled,
            )
            _uiState.update { it.copy(pendingPrintHtml = html) }
        }
    }

    fun prepareOperationPrint() {
        val state = _uiState.value
        val processLabel = state.operationRateProcessOptions.find { it.first == state.operationRateProcessCd }?.second
            ?: state.operationRateProcessCd
        val html = buildBaselineOperationRatePrintHtml(
            PlanBaselineUtilizationLogic.utilizationMonthLabelJp(state.operationRateMonth),
            processLabel,
            state.operationRateRows,
        )
        _uiState.update { it.copy(pendingOperationPrintHtml = html) }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }
    fun clearPendingOperationPrintHtml() = _uiState.update { it.copy(pendingOperationPrintHtml = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: PlanBaselineRepository,
        private val apsRepository: ApsSchedulingRepository,
        private val masterRepository: MasterRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlanBaselineViewModel(repository, apsRepository, masterRepository) as T
    }
}
