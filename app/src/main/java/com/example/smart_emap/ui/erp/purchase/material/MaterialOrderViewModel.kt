package com.example.smart_emap.ui.erp.purchase.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.MaterialStockSubItemDto
import com.example.smart_emap.data.model.MaterialMasterItemDto
import com.example.smart_emap.data.model.MaterialStockSubCreateBodyDto
import com.example.smart_emap.data.model.MaterialStockSubUpdateBodyDto
import com.example.smart_emap.data.model.MaterialStockUpdateBodyDto
import com.example.smart_emap.data.repository.MaterialRepository
import com.example.smart_emap.data.repository.MaterialStockFilters
import com.example.smart_emap.data.repository.MaterialStockStatsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class MaterialOrderTab(val label: String) {
    Initial("初期在庫管理"),
    Daily("材料日別在庫"),
    Sub("半端材料管理"),
    Usage("材料使用管理"),
    Order("材料注文"),
    OrderHistory("材料注文履歴"),
    UnusedReceiving("材料未使用番号"),
}

data class MaterialOrderUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val tab: MaterialOrderTab = MaterialOrderTab.Daily,
    val keyword: String = "",
    val startDate: String = LocalDate.now(ZoneId.of("Asia/Tokyo")).withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val endDate: String = LocalDate.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val supplierOptions: List<String> = emptyList(),
    val selectedSuppliers: List<String> = emptyList(),
    val stockItems: List<MaterialStockItemDto> = emptyList(),
    val subItems: List<MaterialStockSubItemDto> = emptyList(),
    val stats: MaterialStockStatsUi = MaterialStockStatsUi(),
    val snackbarMessage: String? = null,
    val showSyncMasterConfirm: Boolean = false,
    val showDataGenerationDialog: Boolean = false,
    val dataGenStartDate: String = "",
    val dataGenEndDate: String = "",
    val showManualOrderDialog: Boolean = false,
    val showPrintConfirmDialog: Boolean = false,
    val manualOrderLoading: Boolean = false,
    val printLoading: Boolean = false,
    val materialOptions: List<MaterialMasterItemDto> = emptyList(),
    val manualOrderForm: MaterialManualOrderFormUi = MaterialManualOrderFormUi(),
    val selectedMasterMaterial: MaterialMasterItemDto? = null,
    val printForm: MaterialOrderPrintFormUi = MaterialOrderPrintFormUi(),
    val printOrderCount: Int = 0,
    val pendingPrintHtml: String? = null,
)

class MaterialOrderViewModel(
    private val repository: MaterialRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaterialOrderUiState())
    val uiState: StateFlow<MaterialOrderUiState> = _uiState.asStateFlow()
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
            runCatching {
                loadInternal()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, snackbarMessage = e.message ?: "読込失敗") }
            }
        }
    }

    fun setTab(tab: MaterialOrderTab) {
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
            LocalDate.now(ZoneId.of("Asia/Tokyo"))
        }
        val target = base.plusDays(days.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
        _uiState.update { it.copy(startDate = target, endDate = target) }
        search()
    }

    fun setTodayRange() {
        val today = LocalDate.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ISO_LOCAL_DATE)
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
                val updated = repository.syncMaterialMaster(state.startDate, state.endDate)
                loadInternal()
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        showSyncMasterConfirm = false,
                        snackbarMessage = "材料マスタ更新が完了しました。更新件数: ${updated}件",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        actionLoading = false,
                        snackbarMessage = e.message ?: "材料マスタ更新に失敗しました",
                    )
                }
            }
        }
    }

    fun calculateStock() = runAction("在庫計算が完了しました") { repository.calculateStock() }

    fun openDataGenerationDialog() {
        _uiState.update {
            it.copy(
                showDataGenerationDialog = true,
                dataGenStartDate = "",
                dataGenEndDate = "",
            )
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

    fun updateUsageQuantity(item: MaterialStockItemDto, value: Int) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, MaterialStockUpdateBodyDto(plannedUsage = value.coerceAtLeast(0)))
                patchStockItem(id) { it.copy(plannedUsage = value.coerceAtLeast(0)) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "使用数更新失敗") }
            }
        }
    }

    fun updateOrderQuantity(item: MaterialStockItemDto, value: Int) {
        val id = item.id ?: return
        val derived = deriveOrderQuantityFields(item, value.coerceAtLeast(0))
        viewModelScope.launch {
            runCatching {
                repository.updateStock(
                    id,
                    MaterialStockUpdateBodyDto(
                        orderQuantity = derived.orderQuantity,
                        orderBundleQuantity = derived.orderBundleQuantity,
                        bundleWeight = derived.bundleWeight,
                        orderAmount = derived.orderAmount,
                    ),
                )
                patchStockItem(id) { derived }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "注文束数更新失敗") }
            }
        }
    }

    /** Web `handleOrderQuantityChange` と同様：束数 → 本数・重量・注文金額を自動計算 */
    private fun deriveOrderQuantityFields(item: MaterialStockItemDto, orderQuantity: Int): MaterialStockItemDto {
        val pieces = item.piecesPerBundle ?: 0
        val longWeight = item.longWeight ?: 0.0
        val unitPrice = item.unitPrice ?: 0.0
        return if (orderQuantity > 0 && pieces > 0 && longWeight > 0.0) {
            val bundleQty = orderQuantity * pieces
            val weight = bundleQty * longWeight
            item.copy(
                orderQuantity = orderQuantity,
                orderBundleQuantity = bundleQty,
                bundleWeight = weight,
                orderAmount = weight * unitPrice,
            )
        } else {
            item.copy(
                orderQuantity = orderQuantity,
                orderBundleQuantity = 0,
                bundleWeight = 0.0,
                orderAmount = 0.0,
            )
        }
    }

    fun updateInitialStock(item: MaterialStockItemDto, value: Int) {
        val id = item.id ?: return
        val safe = value.coerceAtLeast(0)
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, MaterialStockUpdateBodyDto(initialStock = safe))
                patchStockItem(id) { it.copy(initialStock = safe) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "初期在庫更新失敗") }
            }
        }
    }

    fun updateAdjustmentQuantity(item: MaterialStockItemDto, value: Int) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, MaterialStockUpdateBodyDto(adjustmentQuantity = value))
                patchStockItem(id) { it.copy(adjustmentQuantity = value) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "調整数更新失敗") }
            }
        }
    }

    fun transferToSub(item: MaterialStockItemDto) {
        val id = item.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.transferToSub(id)
                refreshAll()
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = "半端へ転送しました") }
            }.onFailure { e ->
                _uiState.update { it.copy(actionLoading = false, snackbarMessage = e.message ?: "転送失敗") }
            }
        }
    }

    private fun patchStockItem(id: Int, transform: (MaterialStockItemDto) -> MaterialStockItemDto) {
        _uiState.update { state ->
            val rows = state.stockItems.map { if (it.id == id) transform(it) else it }
            state.copy(stockItems = rows, stats = repository.summarizeStock(rows))
        }
    }

    fun updateSubUsage(item: MaterialStockSubItemDto, value: Int) {
        val id = item.id ?: return
        val safe = value.coerceAtLeast(0)
        viewModelScope.launch {
            runCatching {
                repository.updateSubStock(id, MaterialStockSubUpdateBodyDto(plannedUsage = safe))
                patchSubItem(id) { it.copy(plannedUsage = safe.toDouble()) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "使用数更新失敗") }
            }
        }
    }

    fun updateSubRemarks(item: MaterialStockSubItemDto, remarks: String) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateSubStock(id, MaterialStockSubUpdateBodyDto(remarks = remarks))
                patchSubItem(id) { it.copy(remarks = remarks) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "備考更新失敗") }
            }
        }
    }

    fun updateSubLabelColor(item: MaterialStockSubItemDto, color: String?) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateSubStock(id, MaterialStockSubUpdateBodyDto(labelColor = color))
                patchSubItem(id) { it.copy(labelColor = color) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "ラベル色更新失敗") }
            }
        }
    }

    fun deleteSubItem(item: MaterialStockSubItemDto) {
        val id = item.id ?: return
        runAction("データを削除しました") {
            repository.deleteSubStock(id)
        }
    }

    fun updateStockRemarks(item: MaterialStockItemDto, remarks: String) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching {
                repository.updateStock(id, MaterialStockUpdateBodyDto(remarks = remarks))
                patchStockItem(id) { it.copy(remarks = remarks) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "備考更新失敗") }
            }
        }
    }

    fun openManualOrderDialog() {
        val today = LocalDate.now(ZoneId.of("Asia/Tokyo")).format(DateTimeFormatter.ISO_LOCAL_DATE)
        _uiState.update {
            it.copy(
                showManualOrderDialog = true,
                manualOrderForm = MaterialManualOrderFormUi(date = today),
                selectedMasterMaterial = null,
            )
        }
        viewModelScope.launch {
            runCatching {
                val materials = repository.loadMasterMaterials()
                _uiState.update { it.copy(materialOptions = materials) }
            }.onFailure { e ->
                _uiState.update { it.copy(snackbarMessage = e.message ?: "材料マスタ読込失敗") }
            }
        }
    }

    fun dismissManualOrderDialog() {
        _uiState.update {
            it.copy(showManualOrderDialog = false, manualOrderLoading = false, selectedMasterMaterial = null)
        }
    }

    fun setManualOrderDate(value: String) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(date = value)) }

    fun setManualOrderMaterial(materialCd: String) {
        val material = _uiState.value.materialOptions.find { it.materialCd == materialCd }
        _uiState.update {
            it.copy(
                selectedMasterMaterial = material,
                manualOrderForm = it.manualOrderForm.copy(
                    materialCd = materialCd,
                    materialName = material?.materialName.orEmpty(),
                ),
            )
        }
    }

    fun setManualOrderQuantity(value: Int) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(orderQuantity = value.coerceAtLeast(0))) }

    fun setManualOrderBundleQuantity(value: Int) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(orderBundleQuantity = value.coerceAtLeast(0))) }

    fun setManualOrderRemarks(value: String) =
        _uiState.update { it.copy(manualOrderForm = it.manualOrderForm.copy(remarks = value)) }

    fun confirmManualOrder() {
        val state = _uiState.value
        val form = state.manualOrderForm
        val material = state.selectedMasterMaterial
        if (form.date.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "日付を選択してください") }
            return
        }
        if (form.materialCd.isBlank() || material == null) {
            _uiState.update { it.copy(snackbarMessage = "材料を選択してください") }
            return
        }
        val weight = form.orderBundleQuantity * (material.longWeight ?: 0.0)
        val amount = weight * (material.unitPrice ?: 0.0)
        viewModelScope.launch {
            _uiState.update { it.copy(manualOrderLoading = true) }
            runCatching {
                repository.createSubStock(
                    MaterialStockSubCreateBodyDto(
                        date = form.date,
                        materialCd = form.materialCd,
                        materialName = form.materialName.ifBlank { material.materialName.orEmpty() },
                        safetyStock = material.safetyStock ?: 0,
                        unit = material.unit,
                        unitPrice = material.unitPrice ?: 0.0,
                        supplierCd = material.supplierCd,
                        supplierName = material.supplierName,
                        leadTime = material.leadTime ?: 0,
                        orderQuantity = form.orderQuantity,
                        orderBundleQuantity = form.orderBundleQuantity,
                        bundleWeight = weight,
                        orderAmount = amount,
                        standardSpec = material.standardSpec,
                        piecesPerBundle = material.piecesPerBundle ?: 0,
                        longWeight = material.longWeight,
                        remarks = form.remarks.ifBlank { null },
                    ),
                )
                loadInternal()
                _uiState.update {
                    it.copy(
                        manualOrderLoading = false,
                        showManualOrderDialog = false,
                        snackbarMessage = "材料注文が正常に登録されました",
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(manualOrderLoading = false, snackbarMessage = e.message ?: "材料注文の登録に失敗しました")
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
                val rows = repository.loadMergedPrintOrderRows(state.startDate, state.stockItems)
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
                val rows = repository.loadMergedPrintOrderRows(state.startDate, state.stockItems)
                if (rows.isEmpty()) {
                    _uiState.update {
                        it.copy(printLoading = false, snackbarMessage = "没有找到符合条件的注文数据")
                    }
                    return@runCatching
                }
                val html = buildMaterialOrderPrintHtml(rows, state.startDate, state.printForm)
                _uiState.update {
                    it.copy(
                        printLoading = false,
                        showPrintConfirmDialog = false,
                        pendingPrintHtml = html,
                        snackbarMessage = "印刷プレビューを共有します",
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

    private fun patchSubItem(id: Int, transform: (MaterialStockSubItemDto) -> MaterialStockSubItemDto) {
        _uiState.update { state ->
            val rows = state.subItems.map { if (it.id == id) transform(it) else it }
            state.copy(subItems = rows)
        }
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
        when (state.tab) {
            MaterialOrderTab.Sub -> {
                val sub = repository.loadSubStockList(state.keyword, suppliers)
                _uiState.update { it.copy(isLoading = false, subItems = sub) }
            }
            MaterialOrderTab.UnusedReceiving -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stockItems = emptyList(),
                        stats = MaterialStockStatsUi(),
                    )
                }
            }
            else -> {
                val isInitialTab = state.tab == MaterialOrderTab.Initial
                val rows = repository.loadStockList(
                    MaterialStockFilters(
                        keyword = state.keyword,
                        startDate = if (isInitialTab || state.tab == MaterialOrderTab.Sub) null else state.startDate,
                        endDate = if (isInitialTab || state.tab == MaterialOrderTab.Sub) null else state.endDate,
                        targetDate = if (isInitialTab) periodFirstDay(state.startDate) else null,
                        suppliers = suppliers,
                        orderOnly = state.tab == MaterialOrderTab.OrderHistory,
                    ),
                ).let { list ->
                    when (state.tab) {
                        MaterialOrderTab.OrderHistory -> list.filter { (it.orderQuantity ?: 0) > 0 }
                        MaterialOrderTab.Initial -> list.map { item ->
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
        }
    }

    /** 筛选期间所在月份的 1 号（初期在庫管理用） */
    private fun periodFirstDay(startDate: String): String =
        runCatching {
            LocalDate.parse(startDate).withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrElse { startDate }

    class Factory(private val repository: MaterialRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MaterialOrderViewModel(repository) as T
    }
}
