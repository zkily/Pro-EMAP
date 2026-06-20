package com.example.smart_emap.ui.erp.purchase.outsourcing.welding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingProcessProductDto
import com.example.smart_emap.data.model.OutsourcingSupplierDto
import com.example.smart_emap.data.repository.OutsourcingCreateOrderBody
import com.example.smart_emap.data.repository.OutsourcingOrderFilters
import com.example.smart_emap.data.repository.OutsourcingRepository
import com.example.smart_emap.ui.erp.purchase.outsourcing.calculateWeldingOrderStatus
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingCurrency
import com.example.smart_emap.ui.erp.purchase.outsourcing.formatOutsourcingNumber
import com.example.smart_emap.ui.erp.purchase.outsourcing.orderAmount
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingAddBusinessDays
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingNextMonthRange
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingPrevMonthRange
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingThisMonthRange
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingTodayJapan
import com.example.smart_emap.ui.erp.purchase.outsourcing.outsourcingWeekdayDatesBetween
import com.example.smart_emap.ui.erp.purchase.outsourcing.shiftOutsourcingDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeldingOrderProductRowUi(
    val productCd: String,
    val productName: String,
    val unitPrice: Double,
    val deliveryLeadTime: Int,
    val deliveryLocation: String?,
    val category: String?,
    val content: String?,
    val specification: String?,
    val quantityText: String = "",
)

data class WeldingOrderEditFormUi(
    val id: Int = 0,
    val orderNo: String = "",
    val orderDate: String = "",
    val supplierCd: String = "",
    val productCd: String = "",
    val productName: String = "",
    val weldingType: String = "溶接",
    val quantityText: String = "",
    val unitPriceText: String = "",
    val deliveryDate: String = "",
    val remarks: String = "",
)

data class WeldingBatchOrderRowUi(
    val orderDate: String,
    val productCd: String,
    val productName: String,
    val unitPrice: Double,
    val deliveryLocation: String?,
    val category: String?,
    val content: String?,
    val specification: String?,
    val deliveryDate: String,
    val quantityText: String = "",
)

data class WeldingOrderUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val printLoading: Boolean = false,
    val startDate: String = outsourcingTodayJapan(),
    val endDate: String = outsourcingTodayJapan(),
    val supplierCd: String? = null,
    val productName: String? = null,
    val statusFilter: String? = null,
    val suppliers: List<OutsourcingSupplierDto> = emptyList(),
    val productOptions: List<String> = emptyList(),
    val orders: List<OutsourcingWeldingOrderDto> = emptyList(),
    val snackbarMessage: String? = null,
    val pendingPrintHtml: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val showPrintDialog: Boolean = false,
    val showBatchCreateDialog: Boolean = false,
    val deleteTarget: OutsourcingWeldingOrderDto? = null,
    val printForm: WeldingOrderPrintFormUi = WeldingOrderPrintFormUi(),
    val printOrders: List<OutsourcingWeldingOrderDto> = emptyList(),
    val createSupplierCd: String = "",
    val createOrderDate: String = outsourcingTodayJapan(),
    val createDeliveryDate: String = "",
    val createRemarks: String = "",
    val createSupplierLeadTime: Int = 7,
    val createProducts: List<WeldingOrderProductRowUi> = emptyList(),
    val createProductsLoading: Boolean = false,
    val editForm: WeldingOrderEditFormUi = WeldingOrderEditFormUi(),
    val batchSupplierCd: String = "",
    val batchProductCd: String = "",
    val batchStartDate: String = "",
    val batchEndDate: String = "",
    val batchProductOptions: List<Pair<String, String>> = emptyList(),
    val batchOrderRows: List<WeldingBatchOrderRowUi> = emptyList(),
    val batchProductsLoading: Boolean = false,
)

class WeldingOrderViewModel(
    private val repository: OutsourcingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeldingOrderUiState())
    val uiState: StateFlow<WeldingOrderUiState> = _uiState.asStateFlow()

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadSuppliers("Welding")
                _uiState.update { it.copy(suppliers = suppliers) }
                loadOrdersInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { loadOrdersInternal() }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "検索失敗") } }
        }
    }

    fun setStartDate(v: String) { _uiState.update { it.copy(startDate = v) }; search() }
    fun setEndDate(v: String) { _uiState.update { it.copy(endDate = v) }; search() }
    fun setSupplierCd(v: String?) { _uiState.update { it.copy(supplierCd = v) }; search() }
    fun setProductName(v: String?) { _uiState.update { it.copy(productName = v) }; search() }
    fun setStatusFilter(v: String?) { _uiState.update { it.copy(statusFilter = v) }; search() }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun shiftDate(days: Int) {
        val s = _uiState.value
        _uiState.update {
            it.copy(
                startDate = shiftOutsourcingDate(s.startDate, days),
                endDate = shiftOutsourcingDate(s.endDate, days),
            )
        }
        search()
    }

    fun setTodayRange() {
        val today = outsourcingTodayJapan()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun setThisMonthRange() {
        val (start, end) = outsourcingThisMonthRange()
        _uiState.update { it.copy(startDate = start, endDate = end) }
        search()
    }

    fun resetFilters() {
        val today = outsourcingTodayJapan()
        _uiState.update {
            it.copy(startDate = today, endDate = today, supplierCd = null, productName = null, statusFilter = null)
        }
        search()
    }

    fun openCreateDialog() {
        val today = outsourcingTodayJapan()
        val lead = 7
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                createSupplierCd = "",
                createOrderDate = today,
                createDeliveryDate = outsourcingAddBusinessDays(today, lead),
                createSupplierLeadTime = lead,
                createRemarks = "",
                createProducts = emptyList(),
                createProductsLoading = false,
            )
        }
    }

    fun dismissCreateDialog() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun setCreateSupplierCd(v: String) {
        val lead = _uiState.value.suppliers.find { it.supplierCd == v }?.leadTimeDays?.coerceAtLeast(1) ?: 7
        val orderDate = _uiState.value.createOrderDate
        _uiState.update {
            it.copy(
                createSupplierCd = v,
                createSupplierLeadTime = lead,
                createDeliveryDate = if (orderDate.isNotBlank()) outsourcingAddBusinessDays(orderDate, lead) else "",
                createProducts = emptyList(),
            )
        }
    }

    fun setCreateOrderDate(v: String) {
        val lead = _uiState.value.createSupplierLeadTime
        _uiState.update {
            it.copy(
                createOrderDate = v,
                createDeliveryDate = if (v.isNotBlank()) outsourcingAddBusinessDays(v, lead) else "",
            )
        }
    }

    fun setCreateDeliveryDate(v: String) = _uiState.update { it.copy(createDeliveryDate = v) }
    fun setCreateRemarks(v: String) = _uiState.update { it.copy(createRemarks = v) }

    fun loadCreateProducts() {
        val state = _uiState.value
        if (state.createSupplierCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "外注先を選択してください") }
            return
        }
        if (state.createOrderDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "注文日を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(createProductsLoading = true) }
            runCatching {
                val products = repository.loadProcessProducts("Welding", state.createSupplierCd)
                if (products.isEmpty()) {
                    _uiState.update {
                        it.copy(createProductsLoading = false, createProducts = emptyList(), snackbarMessage = "対象製品が存在しません")
                    }
                    return@launch
                }
                val rows = products
                    .sortedBy { it.productName.orEmpty() }
                    .map { it.toProductRow() }
                _uiState.update {
                    it.copy(
                        createProductsLoading = false,
                        createProducts = rows,
                        createDeliveryDate = outsourcingAddBusinessDays(state.createOrderDate, state.createSupplierLeadTime),
                        snackbarMessage = "${rows.size}件の製品データを取得しました",
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(createProductsLoading = false, createProducts = emptyList(), snackbarMessage = "製品データの取得に失敗しました")
                }
            }
        }
    }

    fun setCreateProductQuantity(productCd: String, qty: String) {
        _uiState.update { state ->
            state.copy(
                createProducts = state.createProducts.map {
                    if (it.productCd == productCd) it.copy(quantityText = qty) else it
                },
            )
        }
    }

    fun confirmCreate() {
        val state = _uiState.value
        if (state.actionLoading) return
        if (state.createSupplierCd.isBlank() || state.createOrderDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "外注先と注文日を入力してください") }
            return
        }
        if (state.createProducts.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "製品一覧を読み込んでください") }
            return
        }
        val valid = state.createProducts.mapNotNull { row ->
            val qty = row.quantityText.toIntOrNull() ?: return@mapNotNull null
            if (qty <= 0) return@mapNotNull null
            row to qty
        }
        if (valid.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "数量を入力した製品がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                valid.forEach { (row, qty) ->
                    repository.createWeldingOrder(
                        OutsourcingCreateOrderBody(
                            supplierCd = state.createSupplierCd,
                            orderDate = state.createOrderDate,
                            productCd = row.productCd,
                            productName = row.productName,
                            processTypeLabel = "溶接",
                            quantity = qty,
                            unitPrice = row.unitPrice,
                            deliveryDate = state.createDeliveryDate.takeIf { it.isNotBlank() },
                            deliveryLocation = row.deliveryLocation,
                            category = row.category,
                            content = row.content,
                            specification = row.specification,
                            remarks = state.createRemarks.takeIf { it.isNotBlank() },
                        ),
                    )
                }
                loadOrdersInternal()
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showCreateDialog = false,
                        snackbarMessage = "${valid.size}件の注文を登録しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "登録失敗") }
            }
        }
    }

    fun openEditDialog(row: OutsourcingWeldingOrderDto) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editForm = WeldingOrderEditFormUi(
                    id = row.id ?: 0,
                    orderNo = row.orderNo.orEmpty(),
                    orderDate = row.orderDate.orEmpty(),
                    supplierCd = row.supplierCd.orEmpty(),
                    productCd = row.productCd.orEmpty(),
                    productName = row.productName.orEmpty(),
                    weldingType = row.weldingType ?: "溶接",
                    quantityText = (row.quantity ?: 0).toString(),
                    unitPriceText = (row.unitPrice ?: 0.0).toString(),
                    deliveryDate = row.deliveryDate.orEmpty(),
                ),
            )
        }
    }

    fun dismissEditDialog() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showEditDialog = false) }
    }

    fun updateEditForm(update: WeldingOrderEditFormUi) = _uiState.update { it.copy(editForm = update) }

    fun confirmEdit() {
        val state = _uiState.value
        val form = state.editForm
        if (state.actionLoading || form.id <= 0) return
        val qty = form.quantityText.toIntOrNull()
        val price = form.unitPriceText.toDoubleOrNull()
        if (qty == null || qty <= 0) {
            _uiState.update { it.copy(snackbarMessage = "数量を正しく入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.updateWeldingOrder(
                    form.id,
                    mapOf(
                        "order_date" to form.orderDate,
                        "product_cd" to form.productCd,
                        "product_name" to form.productName,
                        "welding_type" to form.weldingType,
                        "quantity" to qty,
                        "unit_price" to (price ?: 0.0),
                        "delivery_date" to form.deliveryDate.takeIf { it.isNotBlank() },
                        "remarks" to form.remarks.takeIf { it.isNotBlank() },
                    ),
                )
                loadOrdersInternal()
                _uiState.update { it.copy(actionLoading = false, showEditDialog = false, snackbarMessage = "更新しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "更新失敗") }
            }
        }
    }

    fun requestDelete(row: OutsourcingWeldingOrderDto) {
        _uiState.update { it.copy(showDeleteConfirm = true, deleteTarget = row) }
    }

    fun dismissDeleteConfirm() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showDeleteConfirm = false, deleteTarget = null) }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        val id = target.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val msg = repository.deleteWeldingOrder(id)
                loadOrdersInternal()
                _uiState.update {
                    it.copy(actionLoading = false, showDeleteConfirm = false, deleteTarget = null, snackbarMessage = msg)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "削除失敗") }
            }
        }
    }

    fun openPrintDialogForList() {
        val state = _uiState.value
        val orders = state.orders.filter { (it.quantity ?: 0) > 0 }
        if (orders.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "注文データがありません") }
            return
        }
        preparePrintDialog(orders, state.supplierCd)
    }

    fun openPrintDialogForOrder(row: OutsourcingWeldingOrderDto) {
        val orderNo = row.orderNo ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val orders = repository.loadWeldingOrdersByOrderNo(orderNo).filter { (it.quantity ?: 0) > 0 }
                if (orders.isEmpty()) {
                    _uiState.update { it.copy(printLoading = false, snackbarMessage = "注文データが見つかりません") }
                    return@launch
                }
                preparePrintDialog(orders, orders.firstOrNull()?.supplierCd)
                _uiState.update { it.copy(printLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "注文取得失敗") }
            }
        }
    }

    private fun preparePrintDialog(orders: List<OutsourcingWeldingOrderDto>, supplierCd: String?) {
        val state = _uiState.value
        val recipient = when {
            !supplierCd.isNullOrBlank() -> {
                val name = state.suppliers.find { it.supplierCd == supplierCd }?.supplierName ?: supplierCd
                "$name 御中"
            }
            orders.firstOrNull()?.supplierName != null -> "${orders.first().supplierName} 御中"
            else -> ""
        }
        _uiState.update {
            it.copy(
                showPrintDialog = true,
                printOrders = orders,
                printForm = it.printForm.copy(recipientCompany = recipient),
            )
        }
    }

    fun dismissPrintDialog() {
        if (_uiState.value.printLoading) return
        _uiState.update { it.copy(showPrintDialog = false, printOrders = emptyList()) }
    }

    fun updatePrintForm(update: WeldingOrderPrintFormUi) = _uiState.update { it.copy(printForm = update) }

    fun confirmPrint() {
        val state = _uiState.value
        val orders = state.printOrders.filter { (it.quantity ?: 0) > 0 }
        if (orders.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "注文データがありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val ids = orders.mapNotNull { it.id }
                if (ids.isNotEmpty()) {
                    runCatching { repository.batchOrderWelding(ids) }
                }
                loadOrdersInternal()
                val html = buildWeldingOrderPrintHtml(orders, state.printForm)
                _uiState.update {
                    it.copy(
                        printLoading = false,
                        showPrintDialog = false,
                        printOrders = emptyList(),
                        pendingPrintHtml = html,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "印刷失敗") }
            }
        }
    }

    fun openBatchCreateDialog() {
        val (start, end) = outsourcingThisMonthRange()
        _uiState.update {
            it.copy(
                showBatchCreateDialog = true,
                batchSupplierCd = "",
                batchProductCd = "",
                batchStartDate = start,
                batchEndDate = end,
                batchProductOptions = emptyList(),
                batchOrderRows = emptyList(),
            )
        }
    }

    fun dismissBatchCreateDialog() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showBatchCreateDialog = false) }
    }

    fun setBatchSupplierCd(v: String) {
        _uiState.update { it.copy(batchSupplierCd = v, batchProductCd = "", batchOrderRows = emptyList()) }
        if (v.isNotBlank()) loadBatchProductOptions(v)
    }

    fun setBatchProductCd(v: String) = _uiState.update { it.copy(batchProductCd = v, batchOrderRows = emptyList()) }
    fun setBatchStartDate(v: String) = _uiState.update { it.copy(batchStartDate = v) }
    fun setBatchEndDate(v: String) = _uiState.update { it.copy(batchEndDate = v) }

    fun setBatchDateRangePrevMonth() {
        val (start, end) = outsourcingPrevMonthRange()
        _uiState.update { it.copy(batchStartDate = start, batchEndDate = end, batchOrderRows = emptyList()) }
    }

    fun setBatchDateRangeThisMonth() {
        val (start, end) = outsourcingThisMonthRange()
        _uiState.update { it.copy(batchStartDate = start, batchEndDate = end, batchOrderRows = emptyList()) }
    }

    fun setBatchDateRangeNextMonth() {
        val (start, end) = outsourcingNextMonthRange()
        _uiState.update { it.copy(batchStartDate = start, batchEndDate = end, batchOrderRows = emptyList()) }
    }

    fun setBatchRowQuantity(orderDate: String, qty: String) {
        _uiState.update { state ->
            state.copy(
                batchOrderRows = state.batchOrderRows.map {
                    if (it.orderDate == orderDate) it.copy(quantityText = qty) else it
                },
            )
        }
    }

    fun setBatchRowDeliveryDate(orderDate: String, date: String) {
        _uiState.update { state ->
            state.copy(
                batchOrderRows = state.batchOrderRows.map {
                    if (it.orderDate == orderDate) it.copy(deliveryDate = date) else it
                },
            )
        }
    }

    fun loadBatchOrderRows() {
        val state = _uiState.value
        if (state.batchSupplierCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "外注先を選択してください") }
            return
        }
        if (state.batchProductCd.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "製品を選択してください") }
            return
        }
        if (state.batchStartDate.isBlank() || state.batchEndDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(batchProductsLoading = true) }
            runCatching {
                val products = repository.loadProcessProducts("Welding", state.batchSupplierCd)
                val selected = products.find { it.productCd == state.batchProductCd }
                    ?: throw IllegalStateException("選択した製品が見つかりません")
                val productName = selected.productName.orEmpty()
                val existing = repository.loadWeldingOrders(
                    OutsourcingOrderFilters(
                        startDate = state.batchStartDate,
                        endDate = state.batchEndDate,
                        supplierCd = state.batchSupplierCd,
                        productName = productName,
                    ),
                )
                val existingByDate = existing.groupBy { it.orderDate.orEmpty().take(10) }
                    .mapValues { (_, rows) ->
                        rows.sumOf { it.quantity ?: 0 } to rows.firstOrNull()?.deliveryDate.orEmpty()
                    }
                val lead = (selected.deliveryLeadTime ?: 7).coerceAtLeast(1)
                val rows = outsourcingWeekdayDatesBetween(state.batchStartDate, state.batchEndDate).map { orderDate ->
                    val existingEntry = existingByDate[orderDate]
                    val qty = existingEntry?.first?.takeIf { it > 0 }?.toString().orEmpty()
                    val delivery = existingEntry?.second?.takeIf { it.isNotBlank() }
                        ?: outsourcingAddBusinessDays(orderDate, lead)
                    WeldingBatchOrderRowUi(
                        orderDate = orderDate,
                        productCd = selected.productCd.orEmpty(),
                        productName = productName,
                        unitPrice = selected.unitPrice ?: 0.0,
                        deliveryLocation = selected.deliveryLocation,
                        category = selected.category,
                        content = selected.content,
                        specification = selected.specification,
                        deliveryDate = delivery,
                        quantityText = qty,
                    )
                }
                _uiState.update { it.copy(batchProductsLoading = false, batchOrderRows = rows) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(batchProductsLoading = false, snackbarMessage = e.message ?: "読込失敗", batchOrderRows = emptyList())
                }
            }
        }
    }

    fun confirmBatchCreate() {
        val state = _uiState.value
        if (state.actionLoading) return
        val valid = state.batchOrderRows.mapNotNull { row ->
            val qty = row.quantityText.toIntOrNull() ?: return@mapNotNull null
            if (qty <= 0) return@mapNotNull null
            row to qty
        }
        if (valid.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "数量を入力した行がありません") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                valid.forEach { (row, qty) ->
                    repository.createWeldingOrder(
                        OutsourcingCreateOrderBody(
                            supplierCd = state.batchSupplierCd,
                            orderDate = row.orderDate,
                            productCd = row.productCd,
                            productName = row.productName,
                            processTypeLabel = "溶接",
                            quantity = qty,
                            unitPrice = row.unitPrice,
                            deliveryDate = row.deliveryDate,
                            deliveryLocation = row.deliveryLocation,
                            category = row.category,
                            content = row.content,
                            specification = row.specification,
                        ),
                    )
                }
                loadOrdersInternal()
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showBatchCreateDialog = false,
                        snackbarMessage = "${valid.size}件の注文を登録しました",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "登録失敗") }
            }
        }
    }

    fun totalSummaryLabel(): String {
        val orders = _uiState.value.orders
        val qty = orders.sumOf { it.quantity ?: 0 }
        val amount = orders.sumOf { orderAmount(it.quantity, it.unitPrice) }
        return "合計: ${formatOutsourcingNumber(qty)} 本 / ${formatOutsourcingCurrency(amount)}"
    }

    private fun loadBatchProductOptions(supplierCd: String) {
        viewModelScope.launch {
            runCatching {
                val products = repository.loadProcessProducts("Welding", supplierCd)
                _uiState.update {
                    it.copy(
                        batchProductOptions = products
                            .sortedBy { p -> p.productName.orEmpty() }
                            .mapNotNull { p ->
                                p.productCd?.let { cd ->
                                    cd to "${cd} - ${p.productName ?: cd}"
                                }
                            },
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(snackbarMessage = "製品データの取得に失敗しました") }
            }
        }
    }

    private suspend fun loadOrdersInternal() {
        val state = _uiState.value
        val raw = repository.loadWeldingOrders(
            OutsourcingOrderFilters(
                startDate = state.startDate,
                endDate = state.endDate,
                supplierCd = state.supplierCd,
                productName = state.productName,
            ),
        )
        val filtered = state.statusFilter?.let { sf ->
            raw.filter { calculateWeldingOrderStatus(it) == sf }
        } ?: raw
        val productOptions = raw.mapNotNull { it.productName }.distinct().sorted()
        _uiState.update {
            it.copy(isLoading = false, orders = filtered, productOptions = productOptions)
        }
    }

    private fun OutsourcingProcessProductDto.toProductRow() = WeldingOrderProductRowUi(
        productCd = productCd.orEmpty(),
        productName = productName.orEmpty(),
        unitPrice = unitPrice ?: 0.0,
        deliveryLeadTime = deliveryLeadTime ?: 7,
        deliveryLocation = deliveryLocation,
        category = category,
        content = content,
        specification = specification,
    )

    class Factory(private val repository: OutsourcingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            WeldingOrderViewModel(repository) as T
    }
}
