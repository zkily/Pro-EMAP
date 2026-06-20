package com.example.smart_emap.ui.erp.purchase.part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.PartLogItemDto
import com.example.smart_emap.data.repository.PartReceivingFilters
import com.example.smart_emap.data.repository.PartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PartReceivingSortField(val label: String) {
    PART_NAME("部品名"),
    LOG_DATE("日付"),
    MANUFACTURE_DATE("製造日"),
    SUPPLIER("仕入先"),
}

enum class PartReceivingSortOrder { ASC, DESC }

data class PartReceivingColumnVisibility(
    val logDate: Boolean = true,
    val logTime: Boolean = true,
    val item: Boolean = true,
    val partCd: Boolean = false,
    val partName: Boolean = true,
    val processCd: Boolean = false,
    val manufactureNo: Boolean = true,
    val manufactureDate: Boolean = false,
    val piecesPerBundle: Boolean = false,
    val length: Boolean = false,
    val quantity: Boolean = true,
    val bundleQuantity: Boolean = false,
    val outerDiameter1: Boolean = true,
    val outerDiameter2: Boolean = true,
    val supplier: Boolean = true,
    val partQuality: Boolean = false,
    val magnetic: Boolean = false,
    val appearance: Boolean = false,
    val hdNo: Boolean = false,
    val remarks: Boolean = false,
    val note: Boolean = false,
    val createdAt: Boolean = false,
    val updatedAt: Boolean = false,
)

data class PartReceivingHistoryUiState(
    val isLoading: Boolean = false,
    val importLoading: Boolean = false,
    val printLoading: Boolean = false,
    val items: List<PartLogItemDto> = emptyList(),
    val totalCount: Int = 0,
    val keyword: String = "",
    val startDate: String = LocalDate.now().minusMonths(1).toString(),
    val endDate: String = LocalDate.now().toString(),
    val supplierOptions: List<String> = emptyList(),
    val selectedSuppliers: List<String> = emptyList(),
    val sortField: PartReceivingSortField? = null,
    val sortOrder: PartReceivingSortOrder = PartReceivingSortOrder.DESC,
    val page: Int = 1,
    val pageSize: Int = 20,
    val visibleColumns: PartReceivingColumnVisibility = PartReceivingColumnVisibility(),
    val showColumnSettings: Boolean = false,
    val columnSettingsDraft: PartReceivingColumnVisibility = PartReceivingColumnVisibility(),
    val snackbarMessage: String? = null,
    val detailItem: PartLogItemDto? = null,
    val pendingPrintHtml: String? = null,
)

class PartReceivingHistoryViewModel(
    private val repository: PartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartReceivingHistoryUiState())
    val uiState: StateFlow<PartReceivingHistoryUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadReceivingSuppliers()
                _uiState.update { it.copy(supplierOptions = suppliers) }
                searchInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }
    fun setStartDate(value: String) = _uiState.update { it.copy(startDate = value) }
    fun setEndDate(value: String) = _uiState.update { it.copy(endDate = value) }
    fun setSelectedSuppliers(value: List<String>) = _uiState.update { it.copy(selectedSuppliers = value) }
    fun setSortField(value: PartReceivingSortField?) {
        _uiState.update { it.copy(sortField = value, page = 1) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { searchInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    fun setSortOrder(value: PartReceivingSortOrder) {
        _uiState.update { it.copy(sortOrder = value, page = 1) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { searchInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }
    fun showDetail(item: PartLogItemDto) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, page = 1) }
            runCatching { searchInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") }
                }
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                keyword = "",
                startDate = LocalDate.now().minusMonths(1).toString(),
                endDate = LocalDate.now().toString(),
                selectedSuppliers = emptyList(),
                sortField = null,
                sortOrder = PartReceivingSortOrder.DESC,
                page = 1,
            )
        }
        search()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page.coerceAtLeast(1)) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { searchInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        search()
    }

    fun openColumnSettings() {
        _uiState.update { it.copy(showColumnSettings = true, columnSettingsDraft = it.visibleColumns) }
    }

    fun closeColumnSettings() = _uiState.update { it.copy(showColumnSettings = false) }

    fun toggleColumnDraft(key: String, enabled: Boolean) {
        _uiState.update { state ->
            val draft = when (key) {
                "logDate" -> state.columnSettingsDraft.copy(logDate = enabled)
                "logTime" -> state.columnSettingsDraft.copy(logTime = enabled)
                "item" -> state.columnSettingsDraft.copy(item = enabled)
                "partCd" -> state.columnSettingsDraft.copy(partCd = enabled)
                "partName" -> state.columnSettingsDraft.copy(partName = enabled)
                "processCd" -> state.columnSettingsDraft.copy(processCd = enabled)
                "manufactureNo" -> state.columnSettingsDraft.copy(manufactureNo = enabled)
                "manufactureDate" -> state.columnSettingsDraft.copy(manufactureDate = enabled)
                "piecesPerBundle" -> state.columnSettingsDraft.copy(piecesPerBundle = enabled)
                "length" -> state.columnSettingsDraft.copy(length = enabled)
                "quantity" -> state.columnSettingsDraft.copy(quantity = enabled)
                "bundleQuantity" -> state.columnSettingsDraft.copy(bundleQuantity = enabled)
                "outerDiameter1" -> state.columnSettingsDraft.copy(outerDiameter1 = enabled)
                "outerDiameter2" -> state.columnSettingsDraft.copy(outerDiameter2 = enabled)
                "supplier" -> state.columnSettingsDraft.copy(supplier = enabled)
                "partQuality" -> state.columnSettingsDraft.copy(partQuality = enabled)
                "magnetic" -> state.columnSettingsDraft.copy(magnetic = enabled)
                "appearance" -> state.columnSettingsDraft.copy(appearance = enabled)
                "hdNo" -> state.columnSettingsDraft.copy(hdNo = enabled)
                "remarks" -> state.columnSettingsDraft.copy(remarks = enabled)
                "note" -> state.columnSettingsDraft.copy(note = enabled)
                "createdAt" -> state.columnSettingsDraft.copy(createdAt = enabled)
                "updatedAt" -> state.columnSettingsDraft.copy(updatedAt = enabled)
                else -> state.columnSettingsDraft
            }
            state.copy(columnSettingsDraft = draft)
        }
    }

    fun resetColumnSettingsDraft() {
        _uiState.update { it.copy(columnSettingsDraft = PartReceivingColumnVisibility()) }
    }

    fun saveColumnSettings() {
        _uiState.update {
            it.copy(
                visibleColumns = it.columnSettingsDraft,
                showColumnSettings = false,
                snackbarMessage = "列表示設定を保存しました",
            )
        }
    }

    fun importCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(importLoading = true) }
            runCatching {
                val message = repository.importReceivingCsv()
                searchInternal()
                _uiState.update { it.copy(importLoading = false, snackbarMessage = message) }
            }.onFailure { e ->
                _uiState.update { it.copy(importLoading = false, snackbarMessage = e.message ?: "データ読取失敗") }
            }
        }
    }

    fun printAll() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.totalCount <= 0) {
                _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
                return@launch
            }
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val rows = repository.loadReceivingListAll(currentFilters(state), state.totalCount)
                if (rows.isEmpty()) {
                    _uiState.update { it.copy(printLoading = false, snackbarMessage = "印刷するデータがありません") }
                    return@launch
                }
                val html = buildPartReceivingHistoryPrintHtml(sortItems(rows, state.sortField, state.sortOrder))
                _uiState.update { it.copy(printLoading = false, pendingPrintHtml = html) }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "印刷失敗") }
            }
        }
    }

    private suspend fun searchInternal() {
        val state = _uiState.value
        val (items, total) = repository.loadReceivingList(currentFilters(state))
        _uiState.update {
            it.copy(
                isLoading = false,
                items = sortItems(items, state.sortField, state.sortOrder),
                totalCount = total,
            )
        }
    }

    private fun currentFilters(state: PartReceivingHistoryUiState) = PartReceivingFilters(
        keyword = state.keyword,
        startDate = state.startDate,
        endDate = state.endDate,
        suppliers = state.selectedSuppliers,
        page = state.page,
        pageSize = state.pageSize,
    )

    private fun sortItems(
        items: List<PartLogItemDto>,
        field: PartReceivingSortField?,
        order: PartReceivingSortOrder,
    ): List<PartLogItemDto> {
        if (field == null) return items
        val comparator = when (field) {
            PartReceivingSortField.LOG_DATE -> compareBy<PartLogItemDto> { it.logDate.orEmpty() }
                .thenBy { it.logTime.orEmpty() }
            PartReceivingSortField.PART_NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.partName.orEmpty() }
            PartReceivingSortField.MANUFACTURE_DATE -> compareBy { it.manufactureDate.orEmpty() }
            PartReceivingSortField.SUPPLIER -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.supplier.orEmpty() }
        }
        return if (order == PartReceivingSortOrder.DESC) items.sortedWith(comparator.reversed()) else items.sortedWith(comparator)
    }

    class Factory(private val repository: PartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PartReceivingHistoryViewModel(repository) as T
    }
}
