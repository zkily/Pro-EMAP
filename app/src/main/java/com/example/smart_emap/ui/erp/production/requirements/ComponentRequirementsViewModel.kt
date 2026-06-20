package com.example.smart_emap.ui.erp.production.requirements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ComponentRequirementsDailyMatrixRowDto
import com.example.smart_emap.data.model.ComponentRequirementsSummaryItemDto
import com.example.smart_emap.data.model.ComponentRequirementsSummaryMetaDto
import com.example.smart_emap.data.repository.ProductionRequirementsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ComponentRequirementsUiState(
    val dateStart: String = "",
    val dateEnd: String = "",
    val planColumn: String = "molding_actual_plan",
    val loading: Boolean = false,
    val hasSearched: Boolean = false,
    val items: List<ComponentRequirementsSummaryItemDto> = emptyList(),
    val summary: ComponentRequirementsSummaryMetaDto? = null,
    val summaryUse: ComponentRequirementsSummaryMetaDto? = null,
    val dailyDates: List<String> = emptyList(),
    val matrixRows: List<ComponentRequirementsDailyMatrixRowDto> = emptyList(),
    val matrixRowsUse: List<ComponentRequirementsDailyMatrixRowDto> = emptyList(),
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class ComponentRequirementsViewModel(
    private val repository: ProductionRequirementsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ComponentRequirementsUiState())
    val uiState: StateFlow<ComponentRequirementsUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        _uiState.update { it.copy(dateStart = start.toString(), dateEnd = today.toString()) }
    }

    fun setRange(start: String, end: String) {
        _uiState.update { it.copy(dateStart = start, dateEnd = end) }
    }

    fun setPlanColumn(value: String) {
        _uiState.update { it.copy(planColumn = value) }
        runSummary()
    }

    fun applyQuickMonth(offsetMonths: Long) {
        val base = LocalDate.now().plusMonths(offsetMonths)
        val start = base.withDayOfMonth(1)
        val end = base.withDayOfMonth(base.lengthOfMonth())
        _uiState.update { it.copy(dateStart = start.toString(), dateEnd = end.toString()) }
        runSummary()
    }

    fun runSummary() {
        val s = _uiState.value
        if (s.dateStart.isBlank() || s.dateEnd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "開始日・終了日を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, hasSearched = true) }
            runCatching {
                repository.loadComponentRequirements(s.dateStart, s.dateEnd, s.planColumn)
            }.onSuccess { data ->
                val demand = data.demand
                val use = data.use
                val dates = demand?.dailyMatrix?.dates?.takeIf { it.isNotEmpty() }
                    ?: use?.dailyMatrix?.dates.orEmpty()
                val demandRows = (demand?.dailyMatrix?.rows.orEmpty()).sortedBy { it.componentName ?: "" }
                val useByCd = (use?.dailyMatrix?.rows.orEmpty()).associateBy { it.componentCd ?: "" }
                val useRows = demandRows.map { d -> useByCd[d.componentCd ?: ""] ?: emptyUseRow(d) }
                _uiState.update {
                    it.copy(
                        loading = false,
                        items = (demand?.items.orEmpty()),
                        summary = demand?.summary,
                        summaryUse = use?.summary,
                        dailyDates = dates,
                        matrixRows = demandRows,
                        matrixRowsUse = useRows,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        items = emptyList(),
                        summary = null,
                        summaryUse = null,
                        dailyDates = emptyList(),
                        matrixRows = emptyList(),
                        matrixRowsUse = emptyList(),
                        snackbarMessage = e.message ?: "集計に失敗しました",
                    )
                }
            }
        }
    }

    private fun emptyUseRow(demand: ComponentRequirementsDailyMatrixRowDto) =
        ComponentRequirementsDailyMatrixRowDto(
            componentCd = demand.componentCd,
            componentName = demand.componentName,
            componentUom = demand.componentUom,
            byDate = emptyMap(),
            rowTotal = 0.0,
        )

    fun printDailyMatrix() {
        val s = _uiState.value
        if (s.dailyDates.isEmpty() || s.matrixRows.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷できるデータがありません。") }
            return
        }
        val html = ComponentRequirementsPrint.build(
            dates = s.dailyDates,
            rows = s.matrixRows,
            period = s.summary?.let { "${it.dateStart} — ${it.dateEnd}" } ?: "",
            title = "日別・部品別需要",
        )
        _uiState.update { it.copy(pendingPrintHtml = html) }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    class Factory(private val repository: ProductionRequirementsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ComponentRequirementsViewModel(repository) as T
    }
}
