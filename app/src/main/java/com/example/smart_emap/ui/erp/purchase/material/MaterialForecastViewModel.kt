package com.example.smart_emap.ui.erp.purchase.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialForecastDetailDto
import com.example.smart_emap.data.model.MaterialForecastStatsDto
import com.example.smart_emap.data.model.MaterialForecastSummaryDto
import com.example.smart_emap.data.repository.MaterialForecastFilters
import com.example.smart_emap.data.repository.MaterialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class MaterialForecastUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val year: Int = LocalDate.now(ZoneId.of("Asia/Tokyo")).year,
    val month: Int = LocalDate.now(ZoneId.of("Asia/Tokyo")).monthValue,
    val keyword: String = "",
    val supplierCd: String = "",
    val supplierOptions: List<Pair<String, String>> = emptyList(),
    val stats: MaterialForecastStatsDto = MaterialForecastStatsDto(),
    val tab: MaterialForecastTab = MaterialForecastTab.Detail,
    val details: List<MaterialForecastDetailDto> = emptyList(),
    val summary: List<MaterialForecastSummaryDto> = emptyList(),
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
)

class MaterialForecastViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialForecastUiState())
    val uiState: StateFlow<MaterialForecastUiState> = _uiState.asStateFlow()
    private var keywordSearchJob: Job? = null

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                loadInternal()
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "データを更新しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "更新失敗") }
            }
        }
    }

    fun setYear(value: Int) {
        _uiState.update { it.copy(year = value) }
        refreshAll()
    }

    fun setMonth(value: Int) {
        _uiState.update { it.copy(month = value) }
        refreshAll()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
        keywordSearchJob?.cancel()
        keywordSearchJob = viewModelScope.launch {
            delay(350)
            refreshAll()
        }
    }

    fun setSupplierCd(value: String) {
        _uiState.update { it.copy(supplierCd = value) }
        refreshAll()
    }

    fun setTab(tab: MaterialForecastTab) = _uiState.update { it.copy(tab = tab) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    fun setCurrentMonth() {
        val today = LocalDate.now(ZoneId.of("Asia/Tokyo"))
        _uiState.update { it.copy(year = today.year, month = today.monthValue) }
        refreshAll()
    }

    fun shiftMonth(delta: Int) {
        val state = _uiState.value
        var y = state.year
        var m = state.month + delta
        while (m < 1) { m += 12; y -= 1 }
        while (m > 12) { m -= 12; y += 1 }
        _uiState.update { it.copy(year = y, month = m) }
        refreshAll()
    }

    fun resetFilters() {
        val today = LocalDate.now(ZoneId.of("Asia/Tokyo"))
        _uiState.update {
            it.copy(
                year = today.year,
                month = today.monthValue,
                keyword = "",
                supplierCd = "",
            )
        }
        refreshAll()
    }

    fun preparePrint() {
        val state = _uiState.value
        val rows = if (state.tab == MaterialForecastTab.Detail) state.details else emptyList()
        val summary = if (state.tab == MaterialForecastTab.Summary) state.summary else emptyList()
        if (state.tab == MaterialForecastTab.Detail && rows.isEmpty() || state.tab == MaterialForecastTab.Summary && summary.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        val html = buildMaterialForecastPrintHtml(
            tab = state.tab,
            year = state.year,
            month = state.month,
            details = rows,
            summary = summary,
            stats = state.stats,
        )
        _uiState.update {
            it.copy(pendingPrintHtml = html, snackbarMessage = "印刷プレビューを共有します")
        }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val filters = MaterialForecastFilters(
            year = state.year,
            month = state.month,
            supplierCd = state.supplierCd,
            keyword = state.keyword,
        )
        val suppliers = repository.loadForecastSuppliers(state.year, state.month)
        val stats = repository.loadForecastStats(filters)
        val details = repository.loadForecastDetails(filters)
        val summary = repository.loadForecastSummary(filters)
        _uiState.update {
            it.copy(
                isLoading = false,
                actionLoading = false,
                supplierOptions = suppliers,
                stats = stats,
                details = details,
                summary = summary,
            )
        }
    }

    class Factory(private val repository: MaterialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialForecastViewModel(repository) as T
    }
}
