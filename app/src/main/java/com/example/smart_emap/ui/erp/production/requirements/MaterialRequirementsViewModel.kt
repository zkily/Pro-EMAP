package com.example.smart_emap.ui.erp.production.requirements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialRequirementsDailyMatrixRowDto
import com.example.smart_emap.data.model.MaterialRequirementsSummaryItemDto
import com.example.smart_emap.data.model.MaterialRequirementsSummaryMetaDto
import com.example.smart_emap.data.repository.ProductionRequirementsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MaterialRequirementsUiState(
    val dateStart: String = "",
    val dateEnd: String = "",
    val loading: Boolean = false,
    val hasSearched: Boolean = false,
    val items: List<MaterialRequirementsSummaryItemDto> = emptyList(),
    val summary: MaterialRequirementsSummaryMetaDto? = null,
    val dailyDates: List<String> = emptyList(),
    val matrixRows: List<MaterialRequirementsDailyMatrixRowDto> = emptyList(),
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class MaterialRequirementsViewModel(
    private val repository: ProductionRequirementsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialRequirementsUiState())
    val uiState: StateFlow<MaterialRequirementsUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val start = today.withDayOfMonth(1)
        _uiState.update { it.copy(dateStart = start.toString(), dateEnd = today.toString()) }
    }

    fun setRange(start: String, end: String) {
        _uiState.update { it.copy(dateStart = start, dateEnd = end) }
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
                repository.loadMaterialRequirements(s.dateStart, s.dateEnd)
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        items = data.items.orEmpty(),
                        summary = data.summary,
                        dailyDates = data.dailyMatrix?.dates.orEmpty(),
                        matrixRows = data.dailyMatrix?.rows.orEmpty(),
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        items = emptyList(),
                        summary = null,
                        dailyDates = emptyList(),
                        matrixRows = emptyList(),
                        snackbarMessage = e.message ?: "集計に失敗しました",
                    )
                }
            }
        }
    }

    fun printDailyMatrix() {
        val s = _uiState.value
        if (s.dailyDates.isEmpty() || s.matrixRows.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷できるデータがありません。") }
            return
        }
        val html = MaterialRequirementsPrint.build(
            dates = s.dailyDates,
            rows = s.matrixRows,
            period = s.summary?.let { "${it.dateStart} — ${it.dateEnd}" } ?: "",
        )
        _uiState.update { it.copy(pendingPrintHtml = html) }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    class Factory(private val repository: ProductionRequirementsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialRequirementsViewModel(repository) as T
    }
}
