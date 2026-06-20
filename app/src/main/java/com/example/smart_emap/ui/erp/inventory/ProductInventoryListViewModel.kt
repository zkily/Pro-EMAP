package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
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

data class ProductInventoryListUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val productCd: String = "",
    val keyword: String = "",
    val rows: List<ProductionSummaryFullRowDto> = emptyList(),
    val totalCount: Int = 0,
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    val statValues: Map<String, Int> = emptyMap(),
    val showUpdateConfirm: Boolean = false,
    val snackbarMessage: String? = null,
)

class ProductInventoryListViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val _uiState = MutableStateFlow(ProductInventoryListUiState())
    val uiState: StateFlow<ProductInventoryListUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        val today = repository.todayStr()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val options = repository.loadProductOptions()
                _uiState.update { it.copy(productOptions = options) }
                loadInternal()
            }.onFailure { e ->
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

    fun setProductCd(value: String) {
        _uiState.update { it.copy(productCd = value) }
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
        _uiState.update { it.copy(startDate = today, endDate = today, productCd = "", keyword = "") }
        debounceLoad()
    }

    fun requestUpdateInventory() = _uiState.update { it.copy(showUpdateConfirm = true) }

    fun dismissUpdateConfirm() = _uiState.update { it.copy(showUpdateConfirm = false) }

    fun confirmUpdateInventory() {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showUpdateConfirm = false) }
            runCatching {
                val msg = repository.updateProductInventory(_uiState.value.startDate.takeIf { it.isNotBlank() })
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = msg) }
                loadInternal()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(actionLoading = false, snackbarMessage = e.message ?: "在庫更新に失敗しました")
                }
            }
        }
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
        val (list, total) = repository.loadProductSummaryRange(start, end, state.productCd)
        val filtered = applyKeywordFilter(list, state.keyword)
        val statValues = PRODUCT_INVENTORY_FIELDS.associate { field ->
            field.key to sumProductInventory(filtered, field.key)
        }
        _uiState.update {
            it.copy(
                isLoading = false,
                rows = filtered,
                totalCount = if (state.keyword.isBlank()) total else filtered.size,
                statValues = statValues,
            )
        }
    }

    private fun applyKeywordFilter(
        rows: List<ProductionSummaryFullRowDto>,
        keyword: String,
    ): List<ProductionSummaryFullRowDto> {
        val q = keyword.trim()
        if (q.isBlank()) return rows
        return rows.filter { row ->
            row.productCd.orEmpty().contains(q, ignoreCase = true) ||
                row.productName.orEmpty().contains(q, ignoreCase = true)
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductInventoryListViewModel(repository) as T
    }
}
