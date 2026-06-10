package com.example.smart_emap.ui.aps.scheduling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto
import com.example.smart_emap.data.repository.ApsSchedulingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class SchedulingUiState(
    val isLoading: Boolean = false,
    val processCd: String = "KT04",
    val startDate: String = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val endDate: String = LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val lineId: Int? = null,
    val itemName: String? = null,
    val lines: List<ApsProductionLineDto> = emptyList(),
    val grid: SchedulingGridResponseDto? = null,
    val matrixPlanExtendMode: Boolean = false,
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class SchedulingViewModel(
    private val repository: ApsSchedulingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SchedulingUiState())
    val uiState: StateFlow<SchedulingUiState> = _uiState.asStateFlow()

    private var autoLoadJob: Job? = null

    init {
        viewModelScope.launch {
            loadLines()
            loadGrid()
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            loadLines()
            loadGrid()
        }
    }

    fun setProcessCd(value: String) {
        val raw = value.trim().ifEmpty { "KT04" }
        val cd = if (raw == "KT07") "KT07" else "KT04"
        if (cd == _uiState.value.processCd) return
        _uiState.update { it.copy(processCd = cd, lineId = null, itemName = null) }
        viewModelScope.launch {
            loadLines()
            loadGrid()
        }
    }

    fun setLineId(value: Int?) {
        _uiState.update { it.copy(lineId = value) }
        scheduleAutoLoad()
    }

    fun setStartDate(value: String) {
        _uiState.update { it.copy(startDate = value) }
        scheduleAutoLoad()
    }

    fun setEndDate(value: String) {
        _uiState.update { it.copy(endDate = value) }
        scheduleAutoLoad()
    }

    fun setItemName(value: String?) {
        _uiState.update { it.copy(itemName = value?.trim()?.ifEmpty { null }) }
    }

    fun setMatrixPlanExtendMode(value: Boolean) {
        _uiState.update { it.copy(matrixPlanExtendMode = value) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun clearPendingPrintHtml() {
        _uiState.update { it.copy(pendingPrintHtml = null) }
    }

    fun preparePrint() {
        val state = _uiState.value
        val dates = state.grid?.dates.orEmpty()
        if (dates.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        val lineNameById = state.lines.associate { line ->
            line.id to (line.lineName?.trim().orEmpty().ifEmpty { line.lineCode })
        }
        val visibleBlocks = SchedulingMatrixLogic.filterVisibleBlocks(state.grid?.blocks.orEmpty(), lineNameById)
        val displayBlocks = SchedulingMatrixLogic.filterBlocksByProduct(
            visibleBlocks,
            dates,
            state.itemName,
            state.matrixPlanExtendMode,
        )
        val matrixRows = SchedulingMatrixLogic.buildMatrixRows(
            displayBlocks,
            dates,
            lineNameById,
            state.matrixPlanExtendMode,
        )
        val sections = SchedulingMatrixLogic.buildMatrixSections(matrixRows)
        val featureLabel = SchedulingTheme.processLabel(state.processCd)
        val overallTotal = SchedulingMatrixLogic.overallPlannedOutputTotal(displayBlocks, dates)
        val dailyTotals = SchedulingMatrixLogic.overallDailyTotals(displayBlocks, dates, state.matrixPlanExtendMode)
        _uiState.update {
            it.copy(
                pendingPrintHtml = buildSchedulingPrintHtml(
                    featureLabel = featureLabel,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    dates = dates,
                    sections = sections,
                    overallTotal = overallTotal,
                    dailyTotals = dailyTotals,
                    planExtendMode = state.matrixPlanExtendMode,
                ),
            )
        }
    }

    private fun scheduleAutoLoad() {
        autoLoadJob?.cancel()
        autoLoadJob = viewModelScope.launch {
            delay(240)
            loadGrid()
        }
    }

    private suspend fun loadLines() {
        val state = _uiState.value
        val lines = repository.loadLines(state.processCd)
            .filter { line ->
                val name = line.lineName?.trim().orEmpty().ifEmpty { line.lineCode }
                !SchedulingMatrixLogic.isIgnoredLine(name)
            }
        val lineId = state.lineId?.takeIf { id -> lines.any { it.id == id } }
        _uiState.update { it.copy(lines = lines, lineId = lineId) }
    }

    private suspend fun loadGrid() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) return
        _uiState.update { it.copy(isLoading = true) }
        try {
            val grid = repository.loadSchedulingGrid(
                startDate = state.startDate,
                endDate = state.endDate,
                lineId = state.lineId,
                processCd = state.processCd,
            )
            val productOptions = SchedulingMatrixLogic.productOptions(
                SchedulingMatrixLogic.filterVisibleBlocks(
                    grid.blocks,
                    state.lines.associate { line ->
                        line.id to line.lineName?.trim().orEmpty().ifEmpty { line.lineCode }
                    },
                ),
            )
            val itemName = state.itemName?.takeIf { productOptions.contains(it) }
            _uiState.update {
                it.copy(
                    grid = grid,
                    itemName = itemName,
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = e.message ?: "データの取得に失敗しました") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    class Factory(
        private val repository: ApsSchedulingRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SchedulingViewModel::class.java)) {
                return SchedulingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
