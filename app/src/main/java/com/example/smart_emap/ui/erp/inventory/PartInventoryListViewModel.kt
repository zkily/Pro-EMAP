package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.repository.InventoryRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PartInventoryListUiState(
    val isLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val keyword: String = "",
    val rows: List<PartStockItemDto> = emptyList(),
    val statValues: Map<String, Int> = emptyMap(),
    val snackbarMessage: String? = null,
)

class PartInventoryListViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val _uiState = MutableStateFlow(PartInventoryListUiState())
    val uiState: StateFlow<PartInventoryListUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました")
                    }
                }
        }
    }

    fun setStartDate(value: String) {
        _uiState.update { it.copy(startDate = value) }
        debounceLoad()
    }

    fun setEndDate(value: String) {
        _uiState.update { it.copy(endDate = value) }
        debounceLoad()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
        debounceLoad()
    }

    fun setQuickDatePrev() = shiftSingleDay(-1)

    fun setQuickDateToday() {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        debounceLoad()
    }

    fun setQuickDateNext() = shiftSingleDay(1)

    fun resetFilters() {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today, keyword = "") }
        debounceLoad()
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private fun shiftSingleDay(offset: Long) {
        val state = _uiState.value
        val base = runCatching { LocalDate.parse(state.startDate, dateFmt) }.getOrElse { LocalDate.now() }
        val shifted = base.plusDays(offset).format(dateFmt)
        _uiState.update { it.copy(startDate = shifted, endDate = shifted) }
        debounceLoad()
    }

    private fun debounceLoad() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました")
                    }
                }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val start = state.startDate.ifBlank { repository.todayStr() }
        val end = state.endDate.ifBlank { start }
        val keyword = state.keyword.trim().takeIf { it.isNotBlank() }
        val rows = if (start == end) {
            repository.loadPartStockForDate(start).let { list ->
                if (keyword == null) list else list.filter { matchesPartKeyword(it, keyword) }
            }
        } else {
            repository.loadPartStockRange(start, end, keyword)
        }
        val statValues = PART_STAT_FIELDS.associate { field ->
            field.key to sumPartStat(rows, field.key)
        }
        _uiState.update {
            it.copy(isLoading = false, rows = rows, statValues = statValues)
        }
    }

    private fun matchesPartKeyword(row: PartStockItemDto, keyword: String): Boolean =
        row.partCd.orEmpty().contains(keyword, ignoreCase = true) ||
            row.partName.orEmpty().contains(keyword, ignoreCase = true) ||
            row.supplierName.orEmpty().contains(keyword, ignoreCase = true)

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PartInventoryListViewModel(repository) as T
    }
}
