package com.example.smart_emap.ui.erp.purchase.part

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MasterPartDto
import com.example.smart_emap.data.model.PartStockCreateBodyDto
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.PartStockUpdateBodyDto
import com.example.smart_emap.data.repository.PartRepository
import com.example.smart_emap.data.repository.PartStockFilters
import com.example.smart_emap.data.repository.PartStockStatsUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class PartOrderTab(val label: String) {
    Initial("初期在庫管理"),
    Daily("部品日別在庫"),
    Usage("部品使用管理"),
    Order("部品注文"),
    OrderHistory("部品注文履歴"),
}

private val PART_ORDER_JST: ZoneId = ZoneId.of("Asia/Tokyo")
private val PART_ORDER_DATE_FMT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun partOrderTodayJapan(): String =
    LocalDate.now(PART_ORDER_JST).format(PART_ORDER_DATE_FMT)

data class PartOrderUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val tab: PartOrderTab = PartOrderTab.Daily,
    val keyword: String = "",
    val startDate: String = partOrderTodayJapan(),
    val endDate: String = partOrderTodayJapan(),
    val supplierOptions: List<String> = emptyList(),
    val selectedSuppliers: List<String> = emptyList(),
    val stockItems: List<PartStockItemDto> = emptyList(),
    val stats: PartStockStatsUi = PartStockStatsUi(),
    val snackbarMessage: String? = null,
    val showSyncMasterConfirm: Boolean = false,
    val showDataGenerationDialog: Boolean = false,
    val dataGenStartDate: String = "",
    val dataGenEndDate: String = "",
    val showManualOrderDialog: Boolean = false,
    val showPrintConfirmDialog: Boolean = false,
    val manualOrderLoading: Boolean = false,
    val printLoading: Boolean = false,
    val partOptions: List<MasterPartDto> = emptyList(),
    val manualOrderForm: PartManualOrderFormUi = PartManualOrderFormUi(),
    val selectedMasterPart: MasterPartDto? = null,
    val printForm: PartOrderPrintFormUi = PartOrderPrintFormUi(),
    val printOrderCount: Int = 0,
    val pendingPrintHtml: String? = null,
)

class PartOrderViewModel(
    private val repository: PartRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PartOrderUiState())
    val uiState: StateFlow<PartOrderUiState> = _uiState.asStateFlow()
    private var keywordSearchJob: Job? = null

    init { refreshAll() }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val suppliers = repository.loadStockSuppliers()
                _uiState.update { it.copy(supplierOptions = suppliers) }
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    private fun loadDataOnly() {
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

    fun setKeyword(v: String) {
        _uiState.update { it.copy(keyword = v) }
        keywordSearchJob?.cancel()
        keywordSearchJob = viewModelScope.launch {
            delay(350)
            loadDataOnly()
        }
    }

    fun setStartDate(v: String) = _uiState.update { it.copy(startDate = v) }
    fun setEndDate(v: String) = _uiState.update { it.copy(endDate = v) }
    fun setSelectedSuppliers(v: List<String>) = _uiState.update { it.copy(selectedSuppliers = v) }
    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }
    fun search() = loadDataOnly()

    fun shiftDateByDays(days: Int) {
        val state = _uiState.value
        val base = runCatching { LocalDate.parse(state.startDate) }.getOrElse {
            LocalDate.now(PART_ORDER_JST)
        }
        val target = base.plusDays(days.toLong()).format(PART_ORDER_DATE_FMT)
        _uiState.update { it.copy(startDate = target, endDate = target) }
        search()
    }

    fun setTodayRange() {
        val today = partOrderTodayJapan()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        search()
    }

    fun syncMaster() {
        val state = _uiState.value
        if (state.startDate.isBlank() || state.endDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "まず日付（期間）を選択してください") }
            return
        }
        _uiState.update { it.copy(showSyncMasterConfirm = true) }
    }

    fun dismissSyncMasterConfirm() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showSyncMasterConfirm = false) }
    }

    fun confirmSyncMaster() {
        val state = _uiState.value
        if (state.actionLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val updated = repository.syncPartMaster(state.startDate, state.endDate)
                loadInternal()
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showSyncMasterConfirm = false,
                        snackbarMessage = "部品マスタ更新が完了しました。更新件数: ${updated}件",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "部品マスタ更新に失敗しました",
                    )
                }
            }
        }
    }

    fun calculateStock() = runAction("在庫計算が完了しました") { repository.calculateStock() }

    fun openDataGenerationDialog() {
        _uiState.update {
            it.copy(showDataGenerationDialog = true, dataGenStartDate = "", dataGenEndDate = "")
        }
    }

    fun dismissDataGenerationDialog() {
        if (_uiState.value.actionLoading) return
        _uiState.update { it.copy(showDataGenerationDialog = false) }
    }

    fun setDataGenStartDate(v: String) = _uiState.update { it.copy(dataGenStartDate = v) }
    fun setDataGenEndDate(v: String) = _uiState.update { it.copy(dataGenEndDate = v) }

    fun confirmDataGeneration() {
        val state = _uiState.value
        if (state.actionLoading) return
        when {
            state.dataGenStartDate.isBlank() -> {
                _uiState.update { it.copy(snackbarMessage = "開始日を選択してください") }
                return
            }
            state.dataGenEndDate.isBlank() -> {
                _uiState.update { it.copy(snackbarMessage = "終了日を選択してください") }
                return
            }
            runCatching {
                LocalDate.parse(state.dataGenStartDate) > LocalDate.parse(state.dataGenEndDate)
            }.getOrDefault(false) -> {
                _uiState.update { it.copy(snackbarMessage = "開始日は終了日より前である必要があります") }
                return
            }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val result = repository.generateStockData(state.dataGenStartDate, state.dataGenEndDate)
                loadInternal()
                val generated = result.generated_count ?: 0
                val updated = result.updated_count ?: 0
                val duplicate = result.duplicate_count ?: 0
                val message = buildString {
                    append("データ生成が完了しました")
                    append("（新規: ${generated}件")
                    if (updated > 0) append("、更新: ${updated}件")
                    if (duplicate > 0) append("、重複スキップ: ${duplicate}件")
                    append("）")
                }
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showDataGenerationDialog = false,
                        snackbarMessage = message,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "データ生成に失敗しました",
                    )
                }
            }
        }
    }

    fun updateOrderQuantity(item: PartStockItemDto, value: Int) {
        val id = item.id ?: return
        val derived = deriveOrderQuantityFields(item, value.coerceAtLeast(0))
        viewModelScope.launch {
            runCatching {
                repository.updateStock(
                    id,
                    PartStockUpdateBodyDto(
                        orderQuantity = derived.orderQuantity,
                        orderBundleQuantity = derived.orderBundleQuantity,
                        orderAmount = derived.orderAmount,
                    ),
                )
                patchStockItem(id) { derived }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "注文数量更新失敗") }
            }
        }
    }

    /** Web `handleOrderQuantityChange` と同様 */
    private fun deriveOrderQuantityFields(item: PartStockItemDto, orderQuantity: Int): PartStockItemDto {
        val ppb = item.piecesPerBundle ?: 1
        val unitPrice = item.unitPrice ?: 0.0
        return if (orderQuantity > 0) {
            val bundleQty = orderQuantity * ppb
            item.copy(
                orderQuantity = orderQuantity,
                orderBundleQuantity = bundleQty,
                orderAmount = bundleQty * unitPrice,
            )
        } else {
            item.copy(
                orderQuantity = 0,
                orderBundleQuantity = 0,
                orderAmount = 0.0,
            )
        }
    }

    fun updateInitialStock(item: PartStockItemDto, value: Int) {
        val id = item.id ?: return
        val safe = value.coerceAtLeast(0)
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, PartStockUpdateBodyDto(initialStock = safe))
                patchStockItem(id) { it.copy(initialStock = safe) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "初期在庫更新失敗") }
            }
        }
    }

    fun updateAdjustmentQuantity(item: PartStockItemDto, value: Int) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, PartStockUpdateBodyDto(adjustmentQuantity = value))
                patchStockItem(id) { it.copy(adjustmentQuantity = value) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "調整数更新失敗") }
            }
        }
    }

    fun updateStockRemarks(item: PartStockItemDto, remarks: String) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, PartStockUpdateBodyDto(remarks = remarks))
                patchStockItem(id) { it.copy(remarks = remarks) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "備考更新失敗") }
            }
        }
    }

    private fun patchStockItem(id: Int, transform: (PartStockItemDto) -> PartStockItemDto) {
        _uiState.update { state ->
            val rows = state.stockItems.map { if (it.id == id) transform(it) else it }
            state.copy(stockItems = rows, stats = repository.summarizeStock(rows))
        }
    }

    fun openManualOrderDialog() {
        val today = partOrderTodayJapan()
        _uiState.update {
            it.copy(
                showManualOrderDialog = true,
                manualOrderForm = PartManualOrderFormUi(date = today),
                selectedMasterPart = null,
            )
        }
        viewModelScope.launch {
            runCatching {
                val parts = repository.loadMasterParts()
                _uiState.update { it.copy(partOptions = parts) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "部品マスタ読込失敗") }
            }
        }
    }

    fun dismissManualOrderDialog() {
        _uiState.update {
            it.copy(showManualOrderDialog = false, manualOrderLoading = false, selectedMasterPart = null)
        }
    }

    fun setManualOrderDate(value: String) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(date = value)) }

    fun setManualOrderPart(partCd: String) {
        val part = _uiState.value.partOptions.find { it.partCd == partCd }
        _uiState.update {
            it.copy(
                selectedMasterPart = part,
                manualOrderForm = it.manualOrderForm.copy(
                    partCd = partCd,
                    partName = part?.partName.orEmpty(),
                    unitPrice = part?.unitPrice ?: 0.0,
                    unit = part?.uom.orEmpty(),
                    supplierCd = part?.supplierCd.orEmpty(),
                    supplierName = "",
                    standardSpec = part?.category.orEmpty(),
                    piecesPerBundle = 1,
                ),
            )
        }
    }

    fun setManualOrderQuantity(value: Int) {
        val qty = value.coerceAtLeast(0)
        val form = _uiState.value.manualOrderForm
        val ppb = form.piecesPerBundle.coerceAtLeast(1)
        _uiState.update {
            it.copy(
                manualOrderForm = form.copy(
                    orderQuantity = qty,
                ),
            )
        }
    }

    fun setManualOrderRemarks(value: String) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(remarks = value)) }

    fun confirmManualOrder() {
        val state = _uiState.value
        val form = state.manualOrderForm
        val part = state.selectedMasterPart
        if (form.date.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "日付を選択してください") }
            return
        }
        if (form.partCd.isBlank() || part == null) {
            _uiState.update { it.copy(snackbarMessage = "部品を選択してください") }
            return
        }
        val ppb = form.piecesPerBundle.coerceAtLeast(1)
        val oq = form.orderQuantity
        val bundleQty = if (oq > 0) oq * ppb else 0
        val amount = if (bundleQty > 0) bundleQty * form.unitPrice else 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(manualOrderLoading = true) }
            runCatching {
                repository.createStock(
                    PartStockCreateBodyDto(
                        date = form.date,
                        partCd = form.partCd,
                        partName = form.partName.ifBlank { part.partName.orEmpty() },
                        unit = form.unit.ifBlank { part.uom },
                        unitPrice = form.unitPrice,
                        supplierCd = form.supplierCd.ifBlank { part.supplierCd },
                        supplierName = form.supplierName.ifBlank { part.supplierName },
                        leadTime = form.leadTime,
                        orderQuantity = oq,
                        orderBundleQuantity = bundleQty,
                        orderAmount = amount,
                        standardSpec = form.standardSpec.ifBlank { part.category },
                        piecesPerBundle = ppb,
                        remarks = form.remarks.ifBlank { null },
                    ),
                )
                loadInternal()
                _uiState.update {
                    it.copy(
                        manualOrderLoading = false,
                        showManualOrderDialog = false,
                        snackbarMessage = "部品注文が正常に登録されました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(manualOrderLoading = false, snackbarMessage = e.message ?: "部品注文の登録に失敗しました")
                }
            }
        }
    }

    fun openPrintOrderDialog() {
        val state = _uiState.value
        if (state.startDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "日付範囲を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val rows = repository.loadPrintOrderRows(state.startDate, state.stockItems)
                if (rows.isEmpty()) {
                    _uiState.update {
                        it.copy(printLoading = false, snackbarMessage = "没有找到符合条件的注文数据")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            printLoading = false,
                            showPrintConfirmDialog = true,
                            printOrderCount = rows.size,
                        )
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "注文データ取得失敗") }
            }
        }
    }

    fun dismissPrintOrderDialog() {
        _uiState.update { it.copy(showPrintConfirmDialog = false, printLoading = false) }
    }

    fun setPrintRecipientCompany(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(recipientCompany = value)) }

    fun setPrintRecipientPersons(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(recipientPersons = value)) }

    fun setPrintApprover(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(approver = value)) }

    fun setPrintIssuer(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(issuer = value)) }

    fun setPrintNote1(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(note1 = value)) }

    fun setPrintNote2(value: String) =
        _uiState.update { it.copy(printForm = it.printForm.copy(note2 = value)) }

    fun confirmPrintOrder() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                val rows = repository.loadPrintOrderRows(state.startDate, state.stockItems)
                if (rows.isEmpty()) {
                    _uiState.update {
                        it.copy(printLoading = false, snackbarMessage = "没有找到符合条件的注文数据")
                    }
                    return@runCatching
                }
                val html = buildPartOrderPrintHtml(rows, state.startDate, state.printForm)
                _uiState.update {
                    it.copy(
                        printLoading = false,
                        showPrintConfirmDialog = false,
                        pendingPrintHtml = html,
                        snackbarMessage = "印刷プレビューを生成中…",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "印刷処理失敗") }
            }
        }
    }

    fun clearPendingPrintHtml() {
        _uiState.update { it.copy(pendingPrintHtml = null) }
    }

    private fun runAction(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                block()
                loadInternal()
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = successMessage) }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "操作失敗") }
            }
        }
    }

    private suspend fun loadInternal() {
        val state = _uiState.value
        val suppliers = state.selectedSuppliers
        val isInitialTab = state.tab == PartOrderTab.Initial
        val rows = repository.loadStockList(
            PartStockFilters(
                keyword = state.keyword,
                startDate = if (isInitialTab) null else state.startDate,
                endDate = if (isInitialTab) null else state.endDate,
                targetDate = if (isInitialTab) periodFirstDay(state.startDate) else null,
                suppliers = suppliers,
                orderOnly = state.tab == PartOrderTab.OrderHistory,
            ),
        ).let { list ->
            when (state.tab) {
                PartOrderTab.OrderHistory -> list.filter { (it.orderQuantity ?: 0) > 0 }
                PartOrderTab.Initial -> list.map { item ->
                    item.copy(
                        initialStock = item.initialStock ?: item.currentStock ?: 0,
                        adjustmentQuantity = item.adjustmentQuantity ?: 0,
                    )
                }
                else -> list
            }
        }
        _uiState.update {
            it.copy(isLoading = false, stockItems = rows, stats = repository.summarizeStock(rows))
        }
    }

    private fun periodFirstDay(startDate: String): String =
        runCatching {
            LocalDate.parse(startDate).withDayOfMonth(1).format(PART_ORDER_DATE_FMT)
        }.getOrElse { startDate }

    class Factory(private val repository: PartRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PartOrderViewModel(repository) as T
    }
}
