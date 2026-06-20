package com.example.smart_emap.ui.erp.purchase.outsourcing.plating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OutsourcingPlatingOrderDto
import com.example.smart_emap.data.model.OutsourcingPlatingReceivingDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.data.repository.OutsourcingReceivingFilters
import com.example.smart_emap.data.repository.OutsourcingRepository
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculatePlatingReceivingStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingThisMonthRange
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingTodayJapan
import com.example.smart_emap.ui.erp.purchase.outsourcing.shiftOutsourcingDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlatingPendingOrderOption(
    val id: Int,
    val orderNo: String,
    val supplierName: String,
    val productCd: String,
    val productName: String,
    val platingType: String,
    val orderQty: Int,
    val receivedQty: Int,
) {
    val remainQty: Int get() = (orderQty - receivedQty).coerceAtLeast(0)
}

data class PlatingReceivingFormUi(
    val id: Int? = null,
    val orderId: Int = 0,
    val orderNo: String = "",
    val receivingDate: String = outsourcingTodayJapan(),
    val supplierName: String = "",
    val productCd: String = "",
    val productName: String = "",
    val platingType: String = "",
    val orderQty: Int = 0,
    val deliveredQty: Int = 0,
    val remainQty: Int = 0,
    val receivingQtyText: String = "",
    val goodQtyText: String = "",
    val defectQtyText: String = "0",
    val defectReason: String = "",
    val inspector: String = "",
    val remarks: String = "",
)

data class PlatingReceivingUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val startDate: String = outsourcingTodayJapan(),
    val endDate: String = outsourcingTodayJapan(),
    val supplierId: Int? = null,
    val productName: String? = null,
    val keyword: String = "",
    val statusFilter: String? = null,
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Int = 0,
    val suppliers: List<OutsourcingSupplierDto> = emptyList(),
    val productOptions: List<String> = emptyList(),
    val receivings: List<OutsourcingPlatingReceivingDto> = emptyList(),
    val pendingOrders: List<PlatingPendingOrderOption> = emptyList(),
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val showReceivingDialog: Boolean = false,
    val isEditMode: Boolean = false,
    val form: PlatingReceivingFormUi = PlatingReceivingFormUi(),
    val detailReceiving: OutsourcingPlatingReceivingDto? = null,
    val detailOrder: OutsourcingPlatingOrderDto? = null,
)

class PlatingReceivingViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlatingReceivingUiState())
    val uiState: StateFlow<PlatingReceivingUiState> = _uiState.asStateFlow()
    private var keywordSearchJob: Job? = null

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadSuppliers("plating")
                val products = repository.loadPlatingReceivingProducts()
                _uiState.update { it.copy(suppliers = suppliers, productOptions = products) }
                loadReceivingsInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadReceivingsInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    fun setStartDate(v: String) {
        _uiState.update { it.copy(startDate = v) }
        search()
    }

    fun setEndDate(v: String) {
        _uiState.update { it.copy(endDate = v) }
        search()
    }

    fun setSupplierId(v: Int?) {
        _uiState.update { it.copy(supplierId = v, page = 1) }
        search()
    }

    fun setProductName(v: String?) {
        _uiState.update { it.copy(productName = v, page = 1) }
        search()
    }

    fun setKeyword(v: String) {
        _uiState.update { it.copy(keyword = v, page = 1) }
        keywordSearchJob?.cancel()
        keywordSearchJob = viewModelScope.launch {
            delay(300)
            search()
        }
    }

    fun setStatusFilter(v: String?) {
        _uiState.update { it.copy(statusFilter = v, page = 1) }
        search()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page.coerceAtLeast(1)) }
        search()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        search()
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun shiftDate(days: Int) {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                startDate = shiftOutsourcingDate(s.startDate, days),
                endDate = shiftOutsourcingDate(s.endDate, days),
                page = 1,
            )
        }
        search()
    }

    fun setTodayRange() {
        val today = outsourcingTodayJapan()
        _uiState.update { it.copy(startDate = today, endDate = today, page = 1) }
        search()
    }

    fun setThisMonthRange() {
        val (start, end) = outsourcingThisMonthRange()
        _uiState.update { it.copy(startDate = start, endDate = end, page = 1) }
        search()
    }

    fun resetFilters() {
        val today = outsourcingTodayJapan()
        _uiState.update {
            it.copy(
                startDate = today,
                endDate = today,
                supplierId = null,
                productName = null,
                keyword = "",
                statusFilter = null,
                page = 1,
            )
        }
        search()
    }

    fun pendingCount(): Int = _uiState.value.receivings.count { calculatePlatingReceivingStatus(it) == "未検収" }

    fun todayQuantityLabel(): String {
        val today = outsourcingTodayJapan()
        val qty = _uiState.value.receivings
            .filter { it.receivingDate == today }
            .sumOf { it.receivingQty ?: 0 }
        return "本日入庫: ${formatOutsourcingNumber(qty)} 個"
    }

    fun printList() {
        val items = _uiState.value.receivings
        if (items.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        _uiState.update { it.copy(pendingPrintHtml = buildPlatingReceivingPrintHtml(items)) }
    }

    fun openCreateDialog() {
        viewModelScope.launch {
            runCatching {
                val pending = loadPendingOptions()
                _uiState.update {
                    it.copy(
                        showReceivingDialog = true,
                        isEditMode = false,
                        pendingOrders = pending,
                        form = PlatingReceivingFormUi(receivingDate = outsourcingTodayJapan()),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "未完了注文の取得に失敗しました") }
            }
        }
    }

    fun openEditDialog(row: OutsourcingPlatingReceivingDto) {
        viewModelScope.launch {
            runCatching {
                val pending = loadPendingOptions()
                val matched = pending.find { it.orderNo == row.orderNo }
                val qty = row.orderQty ?: matched?.orderQty ?: 0
                val deliveredQty = matched?.receivedQty ?: (qty - (matched?.remainQty ?: qty)).coerceAtLeast(0)
                _uiState.update {
                    it.copy(
                        showReceivingDialog = true,
                        isEditMode = true,
                        detailReceiving = null,
                        pendingOrders = pending,
                        form = PlatingReceivingFormUi(
                            id = row.id,
                            orderId = row.orderId ?: matched?.id ?: 0,
                            orderNo = row.orderNo.orEmpty(),
                            receivingDate = row.receivingDate.orEmpty(),
                            supplierName = row.supplierName ?: row.supplierCd.orEmpty(),
                            productCd = row.productCd.orEmpty(),
                            productName = row.productName.orEmpty(),
                            platingType = row.platingType.orEmpty().ifBlank { matched?.platingType.orEmpty() },
                            orderQty = qty,
                            deliveredQty = deliveredQty,
                            remainQty = matched?.remainQty ?: (qty - deliveredQty).coerceAtLeast(0),
                            receivingQtyText = qty.toString(),
                            goodQtyText = qty.toString(),
                            defectQtyText = "0",
                            defectReason = row.defectReason.orEmpty(),
                            inspector = row.inspector.orEmpty(),
                            remarks = row.remarks.orEmpty(),
                        ),
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun openDetailDialog(row: OutsourcingPlatingReceivingDto) {
        _uiState.update { it.copy(detailReceiving = row) }
    }

    fun dismissDetailDialog() = _uiState.update { it.copy(detailReceiving = null) }

    fun editFromDetail() {
        val row = _uiState.value.detailReceiving ?: return
        dismissDetailDialog()
        openEditDialog(row)
    }

    fun openOrderDetail(orderNo: String) {
        viewModelScope.launch {
            runCatching {
                val orders = repository.loadPlatingOrdersByOrderNo(orderNo)
                val order = orders.firstOrNull()
                if (order == null) {
                    _uiState.update { it.copy(snackbarMessage = "注文が見つかりません") }
                } else {
                    _uiState.update { it.copy(detailOrder = order) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "注文詳細の取得に失敗しました") }
            }
        }
    }

    fun dismissOrderDetail() = _uiState.update { it.copy(detailOrder = null) }

    fun dismissReceivingDialog() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showReceivingDialog = false) }
    }

    fun updateForm(form: PlatingReceivingFormUi) = _uiState.update { it.copy(form = form) }

    fun onOrderSelected(orderNo: String) {
        val order = _uiState.value.pendingOrders.find { it.orderNo == orderNo } ?: return
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    orderId = order.id,
                    orderNo = order.orderNo,
                    supplierName = order.supplierName,
                    productCd = order.productCd,
                    productName = order.productName,
                    platingType = order.platingType,
                    orderQty = order.orderQty,
                    deliveredQty = order.receivedQty,
                    remainQty = order.remainQty,
                    receivingQtyText = order.orderQty.toString(),
                    goodQtyText = order.orderQty.toString(),
                    defectQtyText = "0",
                    defectReason = "",
                ),
            )
        }
    }

    fun onReceivingQtyChange(qty: String) {
        _uiState.update { state ->
            val form = state.form
            if (qty.isEmpty()) {
                return@update state.copy(form = form.copy(receivingQtyText = qty))
            }
            val receiving = qty.toIntOrNull()
                ?: return@update state.copy(form = form.copy(receivingQtyText = qty))
            val good = form.goodQtyText.toIntOrNull() ?: receiving
            val adjustedGood = good.coerceAtMost(receiving)
            state.copy(
                form = form.copy(
                    receivingQtyText = qty,
                    goodQtyText = adjustedGood.toString(),
                    defectQtyText = (receiving - adjustedGood).coerceAtLeast(0).toString(),
                ),
            )
        }
    }

    fun onGoodQtyChange(qty: String) {
        _uiState.update { state ->
            val form = state.form
            if (qty.isEmpty()) {
                return@update state.copy(form = form.copy(goodQtyText = qty))
            }
            val receiving = form.receivingQtyText.toIntOrNull() ?: 0
            val good = qty.toIntOrNull()
                ?: return@update state.copy(form = form.copy(goodQtyText = qty))
            val maxGood = minOf(form.orderQty, receiving).coerceAtLeast(0)
            val adjustedGood = good.coerceIn(0, maxGood)
            state.copy(
                form = form.copy(
                    goodQtyText = if (good == adjustedGood) qty else adjustedGood.toString(),
                    defectQtyText = (receiving - adjustedGood).coerceAtLeast(0).toString(),
                ),
            )
        }
    }

    fun confirmReceiving() {
        val state = _uiState.value
        val form = state.form
        if (state.actionLoading) return
        if (form.orderId <= 0) {
            _uiState.update { it.copy(snackbarMessage = "注文を選択してください") }
            return
        }
        val receivingQty = form.receivingQtyText.toIntOrNull() ?: 0
        val goodQty = form.goodQtyText.toIntOrNull() ?: 0
        val defectQty = form.defectQtyText.toIntOrNull() ?: 0
        if (receivingQty <= 0) {
            _uiState.update { it.copy(snackbarMessage = "受入数を入力してください") }
            return
        }
        if (form.inspector.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "検収者を選択してください") }
            return
        }
        if (goodQty > receivingQty) {
            _uiState.update { it.copy(snackbarMessage = "良品数は受入数以下にしてください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val body = mapOf(
                    "order_id" to form.orderId,
                    "receiving_date" to form.receivingDate,
                    "receiving_qty" to receivingQty,
                    "good_qty" to goodQty,
                    "defect_qty" to defectQty,
                    "defect_reason" to form.defectReason.takeIf { it.isNotBlank() && defectQty > 0 },
                    "inspector" to form.inspector,
                    "remarks" to form.remarks.takeIf { it.isNotBlank() },
                )
                if (state.isEditMode && form.id != null) {
                    repository.updatePlatingReceiving(form.id, body)
                } else {
                    repository.createPlatingReceiving(body)
                }
                loadReceivingsInternal()
                _uiState.update {
                    it.copy(actionLoading = false, showReceivingDialog = false, snackbarMessage = if (state.isEditMode) "更新しました" else "登録しました")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "登録失敗") }
            }
        }
    }

    private suspend fun loadPendingOptions(): List<PlatingPendingOrderOption> =
        repository.loadPendingPlatingOrders()
            .mapNotNull { it.toPendingOption() }
            .filter { it.remainQty > 0 }

    private suspend fun loadReceivingsInternal() {
        val state = _uiState.value
        val page = repository.loadPlatingReceivingsPage(
            OutsourcingReceivingFilters(
                startDate = state.startDate,
                endDate = state.endDate,
                supplierId = state.supplierId,
                productName = state.productName,
                keyword = state.keyword,
                page = state.page,
                pageSize = state.pageSize,
            ),
        )
        val filtered = state.statusFilter?.let { sf ->
            page.items.filter { calculatePlatingReceivingStatus(it) == sf }
        } ?: page.items
        val total = if (state.statusFilter != null) filtered.size else page.total
        _uiState.update {
            it.copy(
                isLoading = false,
                receivings = filtered,
                totalCount = total,
                page = page.page,
                pageSize = page.pageSize,
            )
        }
    }

    private fun OutsourcingPlatingOrderDto.toPendingOption(): PlatingPendingOrderOption? {
        val id = id ?: return null
        val orderQty = quantity ?: 0
        val receivedQty = receivedQty ?: totalReceivingQty ?: 0
        return PlatingPendingOrderOption(
            id = id,
            orderNo = orderNo.orEmpty(),
            supplierName = supplierName ?: supplierCd.orEmpty(),
            productCd = productCd.orEmpty(),
            productName = productName.orEmpty(),
            platingType = platingType.orEmpty().ifBlank { "メッキ" },
            orderQty = orderQty,
            receivedQty = receivedQty,
        )
    }

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PlatingReceivingViewModel(repository) as T
    }
}
