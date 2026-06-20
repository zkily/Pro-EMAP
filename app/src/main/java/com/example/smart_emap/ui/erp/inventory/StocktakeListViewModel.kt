package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InventoryLogRowDto
import com.example.smart_emap.data.repository.InventoryLogFilters
import com.example.smart_emap.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StocktakeListUiState(
    val isLoading: Boolean = false,
    val importLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val rows: List<InventoryLogRowDto> = emptyList(),
    val total: Int = 0,
    val totalQuantity: Int = 0,
    val keyword: String = "",
    val dateStart: String = LocalDate.now().minusMonths(1).toString(),
    val dateEnd: String = LocalDate.now().toString(),
    val monthPicker: String = "",
    val stageType: String = "",
    val item: String = "",
    val processOptions: List<Pair<String, String>> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val showDeleteConfirm: Boolean = false,
    val pendingDelete: InventoryLogRowDto? = null,
    val snackbarMessage: String? = null,
)

class StocktakeListViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StocktakeListUiState())
    val uiState: StateFlow<StocktakeListUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val processes = repository.loadProcessOptions()
                _uiState.update { it.copy(processOptions = processes) }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました") }
            }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }
    fun setDateStart(value: String) = _uiState.update { it.copy(dateStart = value) }
    fun setDateEnd(value: String) = _uiState.update { it.copy(dateEnd = value) }
    fun setMonthPicker(value: String) = _uiState.update { it.copy(monthPicker = value) }
    fun setStageType(value: String) {
        _uiState.update { it.copy(stageType = value, page = 1) }
        search()
    }
    fun setItem(value: String) {
        _uiState.update { it.copy(item = value, page = 1) }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, page = 1) }
            runCatching { loadInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                keyword = "",
                dateStart = LocalDate.now().minusMonths(1).toString(),
                dateEnd = LocalDate.now().toString(),
                monthPicker = "",
                stageType = "",
                item = "",
                page = 1,
            )
        }
        search()
    }

    fun applyMonthPicker(month: String) {
        if (month.isBlank()) {
            _uiState.update { it.copy(monthPicker = "") }
            return
        }
        val parsed = runCatching { LocalDate.parse("$month-01") }.getOrNull() ?: return
        _uiState.update {
            it.copy(
                monthPicker = month,
                dateStart = parsed.withDayOfMonth(1).toString(),
                dateEnd = parsed.withDayOfMonth(parsed.lengthOfMonth()).toString(),
                page = 1,
            )
        }
        search()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page.coerceAtLeast(1)) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") } }
        }
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") } }
        }
    }

    fun importLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(importLoading = true) }
            runCatching {
                val message = repository.importInventoryLogs()
                _uiState.update { it.copy(importLoading = false, snackbarMessage = message) }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(importLoading = false, snackbarMessage = e.message ?: "取込に失敗しました") }
            }
        }
    }

    fun requestDelete(row: InventoryLogRowDto) {
        _uiState.update { it.copy(showDeleteConfirm = true, pendingDelete = row) }
    }

    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false, pendingDelete = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDelete?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showDeleteConfirm = false) }
            runCatching {
                repository.deleteInventoryLog(id)
                _uiState.update { it.copy(actionLoading = false, pendingDelete = null, snackbarMessage = "削除しました") }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val result = repository.loadInventoryLogs(
            InventoryLogFilters(
                item = state.item.takeIf { it.isNotBlank() },
                keyword = state.keyword,
                dateStart = state.dateStart,
                dateEnd = state.dateEnd,
                monthPicker = state.monthPicker.takeIf { it.isNotBlank() },
                stageType = state.stageType.takeIf { it.isNotBlank() },
                page = state.page,
                pageSize = state.pageSize,
            ),
        )
        _uiState.update {
            it.copy(
                isLoading = false,
                rows = result.list,
                total = result.total,
                totalQuantity = result.totalQuantity,
            )
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StocktakeListViewModel(repository) as T
    }
}
