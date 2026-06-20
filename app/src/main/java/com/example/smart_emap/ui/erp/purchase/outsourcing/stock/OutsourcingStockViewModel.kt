package com.example.smart_emap.ui.erp.purchase.outsourcing.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OutsourcingStockHistoryItemDto
import com.example.smart_emap.data.model.OutsourcingStockItemDto
import com.example.smart_emap.data.repository.OutsourcingRepository
import com.example.smart_emap.data.repository.OutsourcingStockFilters
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingStockStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.resolveOutsourcingStockStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OutsourcingStockTab { Plating, Welding }

data class OutsourcingStockUiItem(
    val id: Int,
    val productCode: String,
    val productName: String,
    val supplier: String,
    val supplierCd: String,
    val supplierId: Int?,
    val platingType: String?,
    val weldingType: String?,
    val orderedQty: Int,
    val receivedQty: Int,
    val usedQty: Int,
    val stockQty: Int,
    val pendingQty: Int,
    val minStock: Int,
    val lastReceiveDate: String,
    val lastIssueDate: String?,
)

data class OutsourcingStockHistoryUiItem(
    val date: String,
    val type: String,
    val orderNo: String,
    val quantity: Int,
    val stockAfter: Int,
    val operator: String,
    val remarks: String,
)

data class OutsourcingSupplierOption(
    val id: Int,
    val label: String,
    val supplierCd: String,
)

data class OutsourcingStockUiState(
    val isLoading: Boolean = false,
    val historyLoading: Boolean = false,
    val activeTab: OutsourcingStockTab = OutsourcingStockTab.Plating,
    val supplierId: String = "",
    val productCode: String = "",
    val stockStatus: String = "",
    val supplierOptions: List<OutsourcingSupplierOption> = emptyList(),
    val platingItems: List<OutsourcingStockUiItem> = emptyList(),
    val weldingItems: List<OutsourcingStockUiItem> = emptyList(),
    val platingTotal: Int = 0,
    val weldingTotal: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Int = 0,
    val detailItem: OutsourcingStockUiItem? = null,
    val historyTitle: String = "",
    val historyItems: List<OutsourcingStockHistoryUiItem> = emptyList(),
    val showHistoryDialog: Boolean = false,
    val snackbarMessage: String? = null,
) {
    val currentItems: List<OutsourcingStockUiItem>
        get() = if (activeTab == OutsourcingStockTab.Plating) platingItems else weldingItems

    val platingStockCount: Int get() = platingTotal
    val weldingStockCount: Int get() = weldingTotal

    val totalStockQty: Int
        get() = currentItems.sumOf { it.stockQty }

    val totalReceivedQty: Int
        get() = currentItems.sumOf { it.receivedQty }

    val totalUsedQty: Int
        get() = currentItems.sumOf { it.usedQty }

    val totalPendingQty: Int
        get() = currentItems.sumOf { it.pendingQty }

    val lowStockCount: Int
        get() = currentItems.count {
            val status = resolveOutsourcingStockStatus(it.stockQty, it.minStock)
            status == OutsourcingStockStatus.Low || status == OutsourcingStockStatus.Empty
        }
}

class OutsourcingStockViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OutsourcingStockUiState())
    val uiState: StateFlow<OutsourcingStockUiState> = _uiState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadSuppliers(isActive = true).mapNotNull { supplier ->
                    val id = supplier.id ?: return@mapNotNull null
                    val name = supplier.supplierName.orEmpty()
                    val cd = supplier.supplierCd.orEmpty()
                    OutsourcingSupplierOption(
                        id = id,
                        label = if (cd.isNotBlank()) "$cd - $name" else name,
                        supplierCd = cd,
                    )
                }
                _uiState.update { it.copy(supplierOptions = suppliers) }
                searchInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setActiveTab(tab: OutsourcingStockTab) {
        _uiState.update { it.copy(activeTab = tab, page = 1) }
        search()
    }

    fun setSupplierId(value: String) = _uiState.update { it.copy(supplierId = value) }
    fun setProductCode(value: String) = _uiState.update { it.copy(productCode = value) }
    fun setStockStatus(value: String) = _uiState.update { it.copy(stockStatus = value) }
    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page) }
        search()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        search()
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { searchInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    fun refreshStock() {
        _uiState.update { it.copy(snackbarMessage = "在庫情報を更新しています...") }
        search()
    }

    fun exportData() {
        _uiState.update { it.copy(snackbarMessage = "Excel出力機能は準備中です") }
    }

    fun showDetail(item: OutsourcingStockUiItem) = _uiState.update { it.copy(detailItem = item) }
    fun hideDetail() = _uiState.update { it.copy(detailItem = null) }

    fun viewHistory(item: OutsourcingStockUiItem) {
        val tab = _uiState.value.activeTab
        val title = "${item.productCode} - ${item.productName} 入出庫履歴"
        _uiState.update {
            it.copy(
                historyTitle = title,
                showHistoryDialog = true,
                historyItems = emptyList(),
                historyLoading = true,
            )
        }
        viewModelScope.launch {
            runCatching {
                val processType = if (tab == OutsourcingStockTab.Plating) "plating" else "welding"
                val history = repository.getOutsourcingStockHistory(
                    processType = processType,
                    productCd = item.productCode,
                    supplierCd = item.supplierCd,
                    weldingType = item.weldingType,
                )
                _uiState.update {
                    it.copy(
                        historyItems = history.map(::mapHistory),
                        historyLoading = false,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        historyLoading = false,
                        snackbarMessage = e.message ?: "履歴データの取得に失敗しました",
                    )
                }
            }
        }
    }

    fun hideHistory() = _uiState.update { it.copy(showHistoryDialog = false, historyTitle = "", historyItems = emptyList()) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    private suspend fun searchInternal() {
        val state = _uiState.value
        val filters = OutsourcingStockFilters(
            supplierId = state.supplierId.toIntOrNull(),
            productCode = state.productCode.trim().takeIf { it.isNotBlank() },
            stockStatus = state.stockStatus.takeIf { it.isNotBlank() },
            page = state.page,
            pageSize = state.pageSize,
        )
        if (state.activeTab == OutsourcingStockTab.Plating) {
            val (items, total) = repository.getPlatingStock(filters)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    platingItems = items.map(::mapStock),
                    platingTotal = total,
                    totalCount = total,
                    snackbarMessage = if (it.snackbarMessage == "在庫情報を更新しています...") "在庫情報を更新しました" else it.snackbarMessage,
                )
            }
        } else {
            val (items, total) = repository.getWeldingStock(filters)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    weldingItems = items.map(::mapStock),
                    weldingTotal = total,
                    totalCount = total,
                    snackbarMessage = if (it.snackbarMessage == "在庫情報を更新しています...") "在庫情報を更新しました" else it.snackbarMessage,
                )
            }
        }
    }

    private fun mapStock(dto: OutsourcingStockItemDto): OutsourcingStockUiItem {
        val stockQty = dto.stockQty ?: dto.currentStock ?: 0
        val pendingQty = dto.pendingQty ?: dto.pendingReceiving ?: 0
        return OutsourcingStockUiItem(
            id = dto.id ?: 0,
            productCode = dto.productCd.orEmpty(),
            productName = dto.productName.orEmpty(),
            supplier = dto.supplierName.orEmpty(),
            supplierCd = dto.supplierCd.orEmpty(),
            supplierId = dto.supplierId,
            platingType = dto.platingType,
            weldingType = dto.weldingType,
            orderedQty = dto.orderedQty ?: 0,
            receivedQty = dto.receivedQty ?: 0,
            usedQty = dto.usedQty ?: 0,
            stockQty = stockQty,
            pendingQty = pendingQty,
            minStock = dto.minStock ?: 0,
            lastReceiveDate = dto.lastReceiveDate ?: dto.lastReceivingDate.orEmpty(),
            lastIssueDate = dto.lastIssueDate,
        )
    }

    private fun mapHistory(dto: OutsourcingStockHistoryItemDto): OutsourcingStockHistoryUiItem {
        val type = dto.transactionType ?: dto.type.orEmpty()
        return OutsourcingStockHistoryUiItem(
            date = dto.transactionDate ?: dto.logDate.orEmpty(),
            type = if (type == "receive") "receive" else "issue",
            orderNo = dto.relatedNo.orEmpty(),
            quantity = dto.quantity ?: 0,
            stockAfter = dto.stockAfter ?: 0,
            operator = dto.operator.orEmpty(),
            remarks = dto.remarks.orEmpty(),
        )
    }

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OutsourcingStockViewModel(repository) as T
    }
}
