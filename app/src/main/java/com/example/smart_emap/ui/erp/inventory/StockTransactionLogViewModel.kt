package com.example.smart_emap.ui.erp.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InventoryStockTransactionLogBodyDto
import com.example.smart_emap.data.model.InventoryStockTransactionLogRowDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.repository.InventoryRepository
import com.example.smart_emap.data.repository.StockTransactionLogFilters
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StockTransactionLogFormUi(
    val stockType: String = "",
    val transactionType: String = "",
    val targetCd: String = "",
    val locationCd: String = "",
    val processCd: String = "",
    val quantity: String = "",
    val unit: String = "本",
    val transactionTime: String = "",
    val remarks: String = "",
)

data class StockTransactionLogUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val rows: List<InventoryStockTransactionLogRowDto> = emptyList(),
    val total: Int = 0,
    val totalQuantity: Double = 0.0,
    val inboundQuantity: Double = 0.0,
    val outboundQuantity: Double = 0.0,
    val filterStockType: String = "",
    val filterTargetCd: String = "",
    val filterKeyword: String = "",
    val filterLocationCd: String = "",
    val filterTransactionType: String = "",
    val filterProcessCd: String = "",
    val filterDateStart: String = LocalDate.now().minusMonths(1).toString(),
    val filterDateEnd: String = LocalDate.now().toString(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    val processOptions: List<Pair<String, String>> = emptyList(),
    val locationOptions: List<Pair<String, String>> = emptyList(),
    val showEditDialog: Boolean = false,
    val editingId: Int? = null,
    val editForm: StockTransactionLogFormUi = StockTransactionLogFormUi(),
    val showDeleteConfirm: Boolean = false,
    val pendingDelete: InventoryStockTransactionLogRowDto? = null,
    val snackbarMessage: String? = null,
)

class StockTransactionLogViewModel(
    private val repository: InventoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StockTransactionLogUiState())
    val uiState: StateFlow<StockTransactionLogUiState> = _uiState.asStateFlow()
    private var filterJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val products = repository.loadProductOptions()
                val processes = repository.loadProcessOptions()
                val locations = repository.loadLocationOptions()
                _uiState.update {
                    it.copy(
                        productOptions = products,
                        processOptions = processes,
                        locationOptions = locations,
                    )
                }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "データの取得に失敗しました") }
            }
        }
    }

    fun setFilterStockType(value: String) {
        _uiState.update { it.copy(filterStockType = value, page = 1) }
        debounceLoad()
    }

    fun setFilterTargetCd(value: String) {
        _uiState.update { it.copy(filterTargetCd = value, page = 1) }
        debounceLoad()
    }

    fun setFilterKeyword(value: String) {
        _uiState.update { it.copy(filterKeyword = value, page = 1) }
        debounceLoad()
    }

    fun setFilterLocationCd(value: String) {
        _uiState.update { it.copy(filterLocationCd = value, page = 1) }
        debounceLoad()
    }

    fun setFilterTransactionType(value: String) {
        _uiState.update { it.copy(filterTransactionType = value, page = 1) }
        debounceLoad()
    }

    fun setFilterProcessCd(value: String) {
        _uiState.update { it.copy(filterProcessCd = value, page = 1) }
        debounceLoad()
    }

    fun setFilterDateStart(value: String) {
        _uiState.update { it.copy(filterDateStart = value, page = 1) }
        debounceLoad()
    }

    fun setFilterDateEnd(value: String) {
        _uiState.update { it.copy(filterDateEnd = value, page = 1) }
        debounceLoad()
    }

    fun shiftDateRange(days: Int) {
        val start = LocalDate.parse(_uiState.value.filterDateStart).plusDays(days.toLong())
        val end = LocalDate.parse(_uiState.value.filterDateEnd).plusDays(days.toLong())
        _uiState.update { it.copy(filterDateStart = start.toString(), filterDateEnd = end.toString(), page = 1) }
        debounceLoad()
    }

    fun setTodayRange() {
        val today = repository.todayStr()
        _uiState.update { it.copy(filterDateStart = today, filterDateEnd = today, page = 1) }
        debounceLoad()
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

    fun resetFilters() {
        val today = repository.todayStr()
        _uiState.update {
            it.copy(
                filterStockType = "",
                filterTargetCd = "",
                filterKeyword = "",
                filterLocationCd = "",
                filterTransactionType = "",
                filterProcessCd = "",
                filterDateStart = LocalDate.parse(today).minusMonths(1).toString(),
                filterDateEnd = today,
                page = 1,
            )
        }
        debounceLoad()
    }

    fun openEditDialog(row: InventoryStockTransactionLogRowDto) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingId = row.id,
                editForm = StockTransactionLogFormUi(
                    stockType = row.stockType.orEmpty(),
                    transactionType = row.transactionType.orEmpty(),
                    targetCd = row.targetCd.orEmpty(),
                    locationCd = row.locationCd.orEmpty(),
                    processCd = row.processCd.orEmpty(),
                    quantity = row.quantity?.toString().orEmpty(),
                    unit = row.unit.orEmpty().ifBlank { "本" },
                    transactionTime = row.transactionTime.orEmpty(),
                    remarks = row.remarks.orEmpty(),
                ),
            )
        }
    }

    fun dismissEditDialog() = _uiState.update { it.copy(showEditDialog = false, editingId = null) }

    fun updateEditForm(transform: (StockTransactionLogFormUi) -> StockTransactionLogFormUi) {
        _uiState.update { it.copy(editForm = transform(it.editForm)) }
    }

    fun submitEdit() {
        val state = _uiState.value
        val id = state.editingId ?: return
        val form = state.editForm
        val qty = form.quantity.toDoubleOrNull()
        if (form.stockType.isBlank() || form.targetCd.isBlank() || form.locationCd.isBlank() || qty == null) {
            _uiState.update { it.copy(snackbarMessage = "必須項目を入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            val body = InventoryStockTransactionLogBodyDto(
                stockType = form.stockType,
                transactionType = form.transactionType.ifBlank { "調整" },
                targetCd = form.targetCd.trim(),
                locationCd = form.locationCd,
                quantity = qty,
                unit = form.unit.ifBlank { "本" },
                processCd = form.processCd.takeIf { it.isNotBlank() },
                transactionTime = form.transactionTime,
                remarks = form.remarks.trim().ifBlank { null },
            )
            runCatching {
                repository.updateStockTransactionLog(id, body)
                _uiState.update {
                    it.copy(actionLoading = false, showEditDialog = false, editingId = null, snackbarMessage = "更新しました")
                }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "更新に失敗しました") }
            }
        }
    }

    fun requestDelete(row: InventoryStockTransactionLogRowDto) {
        _uiState.update { it.copy(showDeleteConfirm = true, pendingDelete = row) }
    }

    fun dismissDeleteConfirm() = _uiState.update { it.copy(showDeleteConfirm = false, pendingDelete = null) }

    fun confirmDelete() {
        val id = _uiState.value.pendingDelete?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, showDeleteConfirm = false) }
            runCatching {
                repository.deleteStockTransactionLog(id)
                _uiState.update { it.copy(actionLoading = false, pendingDelete = null, snackbarMessage = "削除しました") }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除に失敗しました") }
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private fun debounceLoad() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val result = repository.loadStockTransactionLogs(
            StockTransactionLogFilters(
                stockType = state.filterStockType.takeIf { it.isNotBlank() },
                targetCd = state.filterTargetCd.takeIf { it.isNotBlank() },
                keyword = state.filterKeyword.takeIf { it.isNotBlank() },
                locationCd = state.filterLocationCd.takeIf { it.isNotBlank() },
                transactionType = state.filterTransactionType.takeIf { it.isNotBlank() },
                processCd = state.filterProcessCd.takeIf { it.isNotBlank() },
                dateStart = state.filterDateStart,
                dateEnd = state.filterDateEnd,
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
                inboundQuantity = result.inboundQuantity,
                outboundQuantity = result.outboundQuantity,
            )
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            StockTransactionLogViewModel(repository) as T
    }
}
