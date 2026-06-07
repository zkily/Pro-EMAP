package com.example.smart_emap.ui.erp.purchase.part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.PartStockUpdateBodyDto
import com.example.smart_emap.data.repository.PartRepository
import com.example.smart_emap.data.repository.PartStockFilters
import com.example.smart_emap.data.repository.PartStockStatsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class PartOrderTab { Daily, Usage, Order, OrderHistory }

data class PartOrderUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val tab: PartOrderTab = PartOrderTab.Daily,
    val keyword: String = "",
    val startDate: String = LocalDate.now().minusDays(7).toString(),
    val endDate: String = LocalDate.now().plusDays(14).toString(),
    val items: List<PartStockItemDto> = emptyList(),
    val stats: PartStockStatsUi = PartStockStatsUi(),
    val snackbarMessage: String? = null,
    val editingItem: PartStockItemDto? = null,
    val editOrderQty: String = "",
    val editRemarks: String = "",
)

class PartOrderViewModel(
    private val repository: PartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartOrderUiState())
    val uiState: StateFlow<PartOrderUiState> = _uiState.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadInternal() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
                }
        }
    }

    fun setTab(tab: PartOrderTab) {
        _uiState.update { it.copy(tab = tab) }
        refreshAll()
    }

    fun setKeyword(v: String) = _uiState.update { it.copy(keyword = v) }
    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun search() = refreshAll()

    fun syncMaster() = runAction("部品マスタを同期しました") { repository.syncPartMaster() }
    fun calculateStock() = runAction("在庫計算が完了しました") {
        val s = _uiState.value
        repository.calculateStock(s.startDate, s.endDate)
    }
    fun generateData() = runAction("データ生成が完了しました") {
        val s = _uiState.value
        repository.generateStockData(s.startDate, s.endDate)
    }

    fun openEdit(item: PartStockItemDto) {
        _uiState.update {
            it.copy(
                editingItem = item,
                editOrderQty = item.orderQuantity?.toString().orEmpty(),
                editRemarks = item.remarks.orEmpty(),
            )
        }
    }

    fun setEditOrderQty(v: String) = _uiState.update { it.copy(editOrderQty = v) }
    fun setEditRemarks(v: String) = _uiState.update { it.copy(editRemarks = v) }
    fun closeEdit() = _uiState.update { it.copy(editingItem = null) }

    fun saveEdit() {
        val state = _uiState.value
        val item = state.editingItem ?: return
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(
                    id,
                    PartStockUpdateBodyDto(
                        orderQuantity = state.editOrderQty.toIntOrNull(),
                        remarks = state.editRemarks.takeIf { it.isNotBlank() },
                    ),
                )
                closeEdit()
                refreshAll()
                _uiState.update { it.copy(snackbarMessage = "更新しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "更新失敗") }
            }
        }
    }

    private fun runAction(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                block()
                refreshAll()
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = successMessage) }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "操作失敗") }
            }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val rows = when (state.tab) {
            PartOrderTab.OrderHistory -> repository.loadStockList(
                PartStockFilters(
                    keyword = state.keyword,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    orderOnly = true,
                ),
            )
            PartOrderTab.Order -> repository.loadStockList(
                PartStockFilters(
                    keyword = state.keyword,
                    startDate = state.startDate,
                    endDate = state.endDate,
                ),
            ).filter { (it.orderQuantity ?: 0) > 0 }
            PartOrderTab.Usage -> repository.loadStockList(
                PartStockFilters(
                    keyword = state.keyword,
                    startDate = state.startDate,
                    endDate = state.endDate,
                ),
            ).filter { (it.plannedUsage ?: 0) > 0 || (it.usagePlanQty ?: 0) > 0 }
            PartOrderTab.Daily -> repository.loadStockList(
                PartStockFilters(
                    keyword = state.keyword,
                    startDate = state.startDate,
                    endDate = state.endDate,
                ),
            )
        }
        _uiState.update {
            it.copy(isLoading = false, items = rows, stats = repository.summarizeStock(rows))
        }
    }

    class Factory(private val repository: PartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PartOrderViewModel(repository) as T
    }
}
