package com.example.smart_emap.ui.erp.production.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.InventoryStagnationDataDto
import com.example.smart_emap.data.model.InventoryStagnationRowDto
import com.example.smart_emap.data.model.ProductMachineConfigRowDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.core.system.PrintPageLayout
import com.example.smart_emap.data.repository.AllBatchUpdateProgress
import com.example.smart_emap.data.repository.ProductionSummaryFilters
import com.example.smart_emap.data.repository.ProductionSummaryRepository
import com.example.smart_emap.data.repository.MasterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ProductionDataManagementUiState(
    val isLoading: Boolean = false,
    val actionLoading: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val productCd: String = "",
    val activeTab: ProductionDataTab = ProductionDataTab.Order,
    val rows: List<ProductionSummaryFullRowDto> = emptyList(),
    val total: Int = 0,
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    val processOptions: List<Pair<String, String>> = emptyList(),
    val customizeVisibleColumns: Map<String, Boolean> = ProductionDataManagementLogic.defaultCustomizeVisibleColumns,
    val showUpdateMenu: Boolean = false,
    val showRecommendedPrintMenu: Boolean = false,
    val showProductionPlanMenu: Boolean = false,
    val showColumnSettings: Boolean = false,
    val showInventoryStagnation: Boolean = false,
    val stagnationAsOfDate: String = "",
    val stagnationMinQuantity: Int = 50,
    val stagnationStableDays: Int = 7,
    val stagnationLoading: Boolean = false,
    val stagnationRows: List<InventoryStagnationRowDto> = emptyList(),
    val stagnationMeta: InventoryStagnationDataDto? = null,
    val showGenerateConfirm: Boolean = false,
    val generateStartDate: String = "",
    val generateEndDate: String = "",
    val showProcessPrintDateDialog: Boolean = false,
    val processPrintTargetDate: String = "",
    val processPrintRequestId: Long = 0L,
    val showProgressDialog: Boolean = false,
    val progressDialogTitle: String = "",
    val progressPercentage: Float = 0f,
    val progressText: String = "",
    val progressStatus: DataMgmtProgressStatus = DataMgmtProgressStatus.Running,
    val showAllUpdateConfirm: Boolean = false,
    val showPlanConfirm: Boolean = false,
    val showInventoryTrendConfirm: Boolean = false,
    val showProductMasterDialog: Boolean = false,
    val productMasterStartDate: String = "",
    val productMasterEndDate: String = "",
    val showMachineDialog: Boolean = false,
    val machineStartDate: String = "",
    val machineEndDate: String = "",
    val simpleConfirmAction: String? = null,
    val showBatchInitialDialog: Boolean = false,
    val batchInitialMonth: String = "",
    val batchInitialProcessCd: String = "",
    val batchInitialRows: List<BatchInitialStockRow> = emptyList(),
    val batchInitialLoading: Boolean = false,
    val batchInitialSaving: Boolean = false,
    val showBatchActualDialog: Boolean = false,
    val batchActualDate: String = "",
    val batchActualRows: List<BatchActualRow> = emptyList(),
    val batchActualSaving: Boolean = false,
    val showPlanCreateDialog: Boolean = false,
    val planCreateKind: PlanCreateKind? = null,
    val planCreateForm: PlanCreateFormState = PlanCreateFormState(),
    val planCreateLoading: Boolean = false,
    val planCreateClearLoading: Boolean = false,
    val planCreateInventoryTrendLoading: Boolean = false,
    val showPlanCreateClearConfirm: Boolean = false,
    val showPlanCreateInventoryTrendConfirm: Boolean = false,
    val showPlanMachineConfigDialog: Boolean = false,
    val planMachineConfigLoading: Boolean = false,
    val planMachineConfigRows: List<ProductMachineConfigRowDto> = emptyList(),
    val planMachineOptions: List<Pair<String, String>> = emptyList(),
    val planMachineConfigSavingId: Int? = null,
    val showPlanBomDialog: Boolean = false,
    val planBomLoading: Boolean = false,
    val planBomRows: List<PlanBomUiRow> = emptyList(),
    val planBomBulkField: PlanBomBulkField = PlanBomBulkField.Safety,
    val planBomBulkLoading: Boolean = false,
    val planBomSelected: Set<Int> = emptySet(),
    val snackbarMessage: String? = null,
)

enum class DataMgmtProgressStatus {
    Running,
    Success,
    Error,
}

data class PendingPrintPayload(
    val html: String,
    val jobName: String,
    val resultMessage: String,
    val layout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
)

class ProductionDataManagementViewModel(
    private val repository: ProductionSummaryRepository,
    private val masterRepository: MasterRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(createInitialUiState())
    val uiState: StateFlow<ProductionDataManagementUiState> = _uiState.asStateFlow()
    private var pendingPrint: PendingPrintPayload? = null

    fun loadProducts() {
        viewModelScope.launch {
            val products = repository.loadProducts()
            _uiState.update { it.copy(productOptions = products) }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.update { state ->
                ensureDateRange(state).copy(isLoading = true)
            }
            val state = ensureDateRange(_uiState.value)
            runCatching {
                repository.loadList(
                    ProductionSummaryFilters(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        productCd = state.productCd,
                        limit = 150,
                        sortBy = "product_name",
                        sortOrder = "ASC",
                    ),
                )
            }.onSuccess { (list, total) ->
                _uiState.update { it.copy(isLoading = false, rows = list, total = total) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rows = emptyList(),
                        total = 0,
                        snackbarMessage = e.message ?: "データの読み込みに失敗しました",
                    )
                }
            }
        }
    }

    fun setStartDate(v: String) {
        _uiState.update { it.copy(startDate = v) }
        fetchData()
    }

    fun setEndDate(v: String) {
        _uiState.update { it.copy(endDate = v) }
        fetchData()
    }

    fun setProductCd(v: String) {
        _uiState.update { it.copy(productCd = v) }
        fetchData()
    }

    fun clearProductCd() {
        _uiState.update { it.copy(productCd = "") }
        fetchData()
    }

    fun setActiveTab(tab: ProductionDataTab) = _uiState.update { it.copy(activeTab = tab) }

    fun applyTodayRange() {
        val (start, end) = ProductionDataManagementLogic.todayRange()
        _uiState.update { it.copy(startDate = start, endDate = end) }
        fetchData()
    }

    fun shiftDateRange(dayOffset: Int) {
        val state = ensureDateRange(_uiState.value)
        val (start, end) = ProductionDataManagementLogic.shiftDateRange(
            state.startDate,
            state.endDate,
            dayOffset,
        )
        _uiState.update { it.copy(startDate = start, endDate = end) }
        fetchData()
    }

    fun toggleUpdateMenu() = _uiState.update { it.copy(showUpdateMenu = !it.showUpdateMenu) }
    fun closeUpdateMenu() = _uiState.update { it.copy(showUpdateMenu = false) }
    fun toggleRecommendedPrintMenu() = _uiState.update { it.copy(showRecommendedPrintMenu = !it.showRecommendedPrintMenu) }
    fun closeRecommendedPrintMenu() = _uiState.update { it.copy(showRecommendedPrintMenu = !it.showRecommendedPrintMenu) }
    fun toggleProductionPlanMenu() = _uiState.update { it.copy(showProductionPlanMenu = !it.showProductionPlanMenu) }
    fun closeProductionPlanMenu() = _uiState.update { it.copy(showProductionPlanMenu = false) }
    fun openColumnSettings() {
        if (_uiState.value.activeTab != ProductionDataTab.ColumnCustomize) {
            _uiState.update { it.copy(snackbarMessage = "列設定はカスタムタブでのみ利用できます") }
            return
        }
        _uiState.update { it.copy(showColumnSettings = true) }
    }
    fun closeColumnSettings() = _uiState.update { it.copy(showColumnSettings = false) }
    fun openInventoryStagnation() {
        val today = InventoryStagnationLogic.todayJst()
        _uiState.update {
            it.copy(
                showInventoryStagnation = true,
                stagnationAsOfDate = it.stagnationAsOfDate.ifBlank { today },
            )
        }
        fetchInventoryStagnation()
    }

    fun closeInventoryStagnation() = _uiState.update { it.copy(showInventoryStagnation = false) }

    fun setStagnationAsOfDate(value: String) {
        _uiState.update { it.copy(stagnationAsOfDate = value) }
        fetchInventoryStagnation()
    }

    fun setStagnationMinQuantity(value: Int) {
        _uiState.update { it.copy(stagnationMinQuantity = value.coerceAtLeast(0)) }
        fetchInventoryStagnation()
    }

    fun setStagnationStableDays(value: Int) {
        _uiState.update { it.copy(stagnationStableDays = value.coerceIn(2, 60)) }
        fetchInventoryStagnation()
    }

    fun filterProductFromStagnation(productCd: String) {
        if (productCd.isBlank()) return
        _uiState.update {
            it.copy(
                showInventoryStagnation = false,
                productCd = productCd,
                activeTab = ProductionDataTab.Inventory,
            )
        }
        fetchData()
    }

    fun notifyMessage(message: String) = _uiState.update { it.copy(snackbarMessage = message) }

    private var stagnationFetchJob: Job? = null

    private fun fetchInventoryStagnation() {
        stagnationFetchJob?.cancel()
        stagnationFetchJob = viewModelScope.launch {
            _uiState.update { it.copy(stagnationLoading = true) }
            val state = _uiState.value
            val asOf = state.stagnationAsOfDate.ifBlank { InventoryStagnationLogic.todayJst() }
            runCatching {
                repository.loadInventoryStagnation(
                    asOf = asOf,
                    minQuantity = state.stagnationMinQuantity,
                    stableCalendarDays = state.stagnationStableDays,
                )
            }.onSuccess { (list, meta) ->
                _uiState.update {
                    it.copy(
                        stagnationLoading = false,
                        stagnationRows = InventoryStagnationLogic.sortRows(list),
                        stagnationMeta = meta,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        stagnationLoading = false,
                        stagnationRows = emptyList(),
                        stagnationMeta = null,
                        snackbarMessage = e.message ?: "在庫停滞の取得に失敗しました",
                    )
                }
            }
        }
    }

    fun setColumnVisible(key: String, visible: Boolean) {
        _uiState.update { state ->
            state.copy(customizeVisibleColumns = state.customizeVisibleColumns + (key to visible))
        }
    }

    fun resetColumnSettings() {
        _uiState.update { it.copy(customizeVisibleColumns = ProductionDataManagementLogic.defaultCustomizeVisibleColumns) }
    }

    fun selectAllColumns() {
        _uiState.update { state ->
            state.copy(customizeVisibleColumns = state.customizeVisibleColumns.mapValues { true })
        }
    }

    fun runUpdateAction(action: String) {
        _uiState.update { it.copy(showUpdateMenu = false) }
        when (action) {
            "generate" -> openGenerateConfirmDialog()
            "all-update" -> _uiState.update { it.copy(showAllUpdateConfirm = true) }
            "batch-initial" -> openBatchInitialDialog()
            "batch-actual" -> openBatchActualDialog()
            "plan" -> _uiState.update { it.copy(showPlanConfirm = true) }
            "inventory-trend" -> _uiState.update { it.copy(showInventoryTrendConfirm = true) }
            "product-master" -> openProductMasterDialog()
            "machine" -> openMachineDialog()
            "update-order" -> executeDirectUpdate("update-order")
            "carry-over", "actual", "defect", "scrap", "on-hold", "production-dates" ->
                _uiState.update { it.copy(simpleConfirmAction = action) }
            else -> notifyMessage("未対応の操作です")
        }
    }

    fun closeSimpleConfirm() = _uiState.update { it.copy(simpleConfirmAction = null) }
    fun closeAllUpdateConfirm() = _uiState.update { it.copy(showAllUpdateConfirm = false) }
    fun closePlanConfirm() = _uiState.update { it.copy(showPlanConfirm = false) }
    fun closeInventoryTrendConfirm() = _uiState.update { it.copy(showInventoryTrendConfirm = false) }
    fun closeProductMasterDialog() = _uiState.update { it.copy(showProductMasterDialog = false) }
    fun closeMachineDialog() = _uiState.update { it.copy(showMachineDialog = false) }
    fun closeBatchInitialDialog() = _uiState.update { it.copy(showBatchInitialDialog = false) }
    fun closeBatchActualDialog() = _uiState.update { it.copy(showBatchActualDialog = false) }

    fun confirmSimpleUpdate() {
        val action = _uiState.value.simpleConfirmAction ?: return
        _uiState.update { it.copy(simpleConfirmAction = null) }
        executeConfirmedUpdate(action)
    }

    fun confirmAllUpdate() {
        _uiState.update { it.copy(showAllUpdateConfirm = false) }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    actionLoading = true,
                    showProgressDialog = true,
                    progressDialogTitle = "一括更新中",
                    progressPercentage = 0f,
                    progressText = "一括更新を開始しています...",
                    progressStatus = DataMgmtProgressStatus.Running,
                )
            }
            runCatching {
                repository.runAllBatchUpdate { progress ->
                    val pct = ((progress.stepIndex.toFloat() / progress.stepTotal) * 90f).coerceAtMost(90f)
                    _uiState.update { state ->
                        state.copy(
                            progressPercentage = pct,
                            progressText = "${progress.stepLabel}を実行中... (${progress.stepIndex}/${progress.stepTotal})",
                        )
                    }
                }
            }.onSuccess { msg ->
                finishProgressSuccess(msg)
                fetchData()
            }.onFailure { e ->
                finishProgressError(e.message ?: "一括更新に失敗しました")
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun confirmPlanUpdate() {
        _uiState.update { it.copy(showPlanConfirm = false) }
        executeConfirmedUpdate("plan")
    }

    fun confirmInventoryTrendUpdate() {
        _uiState.update { it.copy(showInventoryTrendConfirm = false) }
        viewModelScope.launch {
            startProgress("在庫・推移更新", "在庫・推移データを取得中...")
            val progressJob = launchProgressTicker()
            runCatching {
                repository.updateInventoryTrendSequence { stepText ->
                    _uiState.update { it.copy(progressText = stepText) }
                }
            }.onSuccess { msg ->
                progressJob.cancel()
                finishProgressSuccess(msg)
                fetchData()
            }.onFailure { e ->
                progressJob.cancel()
                finishProgressError(e.message ?: "在庫・推移更新に失敗しました")
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openProductMasterDialog() {
        val (start, end) = ProductionDataManagementLogic.updatePeriodDefaultRange()
        _uiState.update {
            it.copy(
                showProductMasterDialog = true,
                productMasterStartDate = start,
                productMasterEndDate = end,
            )
        }
    }

    fun setProductMasterStartDate(value: String) = _uiState.update { it.copy(productMasterStartDate = value) }
    fun setProductMasterEndDate(value: String) = _uiState.update { it.copy(productMasterEndDate = value) }

    fun confirmProductMasterUpdate() {
        val start = _uiState.value.productMasterStartDate.trim()
        val end = _uiState.value.productMasterEndDate.trim()
        if (start.isBlank() || end.isBlank()) {
            notifyMessage("更新期間を選択してください")
            return
        }
        _uiState.update { it.copy(showProductMasterDialog = false) }
        viewModelScope.launch {
            startProgress("製品マスタ更新", "製品マスタデータを更新中...")
            val progressJob = launchProgressTicker()
            runCatching { repository.updateProductMaster(start, end) }
                .onSuccess { msg ->
                    progressJob.cancel()
                    finishProgressSuccess(msg)
                    fetchData()
                }
                .onFailure { e ->
                    progressJob.cancel()
                    finishProgressError(e.message ?: "製品マスタ更新に失敗しました")
                }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openMachineDialog() {
        val (start, end) = ProductionDataManagementLogic.updatePeriodDefaultRange()
        _uiState.update {
            it.copy(
                showMachineDialog = true,
                machineStartDate = start,
                machineEndDate = end,
            )
        }
    }

    fun setMachineStartDate(value: String) = _uiState.update { it.copy(machineStartDate = value) }
    fun setMachineEndDate(value: String) = _uiState.update { it.copy(machineEndDate = value) }

    fun confirmMachineUpdate() {
        val start = _uiState.value.machineStartDate.trim()
        val end = _uiState.value.machineEndDate.trim()
        if (start.isBlank() || end.isBlank()) {
            notifyMessage("更新期間を選択してください")
            return
        }
        _uiState.update { it.copy(showMachineDialog = false) }
        viewModelScope.launch {
            startProgress("設備フィールド更新", "機器フィールドデータを更新中...")
            val progressJob = launchProgressTicker()
            runCatching { repository.updateMachine(start, end) }
                .onSuccess { msg ->
                    progressJob.cancel()
                    finishProgressSuccess(msg)
                    fetchData()
                }
                .onFailure { e ->
                    progressJob.cancel()
                    finishProgressError(e.message ?: "機器フィールド更新に失敗しました")
                }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openBatchInitialDialog() {
        viewModelScope.launch {
            val options = _uiState.value.processOptions.ifEmpty { repository.loadProcessOptions() }
            _uiState.update {
                it.copy(
                    showBatchInitialDialog = true,
                    batchInitialMonth = DataMgmtBatchLogic.currentMonthYm(),
                    batchInitialProcessCd = "",
                    batchInitialRows = emptyList(),
                    processOptions = options,
                )
            }
        }
    }

    fun setBatchInitialMonth(value: String) = _uiState.update { it.copy(batchInitialMonth = value.take(7)) }
    fun setBatchInitialProcessCd(value: String) = _uiState.update { it.copy(batchInitialProcessCd = value) }

    fun setBatchInitialQuantity(index: Int, quantity: Int?) {
        _uiState.update { state ->
            val rows = state.batchInitialRows.toMutableList()
            if (index !in rows.indices) return@update state
            rows[index] = rows[index].copy(editQuantity = quantity)
            state.copy(batchInitialRows = rows)
        }
    }

    fun searchBatchInitialStock() {
        val month = _uiState.value.batchInitialMonth.trim()
        val processCd = _uiState.value.batchInitialProcessCd.trim()
        if (month.isBlank() || processCd.isBlank()) {
            notifyMessage("月と工程を選択してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(batchInitialLoading = true) }
            runCatching { repository.searchBatchInitialStock(month, processCd) }
                .onSuccess { rows ->
                    _uiState.update { it.copy(batchInitialLoading = false, batchInitialRows = rows) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(batchInitialLoading = false, snackbarMessage = e.message ?: "検索に失敗しました")
                    }
                }
        }
    }

    fun saveBatchInitialStock() {
        val month = _uiState.value.batchInitialMonth.trim()
        val processCd = _uiState.value.batchInitialProcessCd.trim()
        if (month.isBlank() || processCd.isBlank()) {
            notifyMessage("月と工程を選択してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(batchInitialSaving = true) }
            runCatching { repository.saveBatchInitialStock(month, processCd, _uiState.value.batchInitialRows) }
                .onSuccess { msg ->
                    _uiState.update { it.copy(batchInitialSaving = false, snackbarMessage = msg) }
                    searchBatchInitialStock()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(batchInitialSaving = false, snackbarMessage = e.message ?: "一括保存に失敗しました")
                    }
                }
        }
    }

    fun openBatchActualDialog() {
        val date = ensureDateRange(_uiState.value).startDate
        _uiState.update {
            it.copy(
                showBatchActualDialog = true,
                batchActualDate = date,
                batchActualRows = DataMgmtBatchLogic.emptyBatchActualRows(date),
            )
        }
    }

    fun setBatchActualDate(value: String) {
        _uiState.update { state ->
            state.copy(
                batchActualDate = value,
                batchActualRows = state.batchActualRows.map { it.copy(date = value) },
            )
        }
    }

    fun setBatchActualProduct(index: Int, productCd: String) {
        val productName = _uiState.value.productOptions.find { it.productCd == productCd }?.productName.orEmpty()
        _uiState.update { state ->
            val rows = state.batchActualRows.toMutableList()
            if (index !in rows.indices) return@update state
            rows[index] = rows[index].copy(productCd = productCd, productName = productName)
            state.copy(batchActualRows = rows)
        }
    }

    fun setBatchActualCutting(index: Int, value: Int?) = updateBatchActualRow(index) { it.copy(cuttingActual = value) }
    fun setBatchActualChamfering(index: Int, value: Int?) = updateBatchActualRow(index) { it.copy(chamferingActual = value) }
    fun setBatchActualMolding(index: Int, value: Int?) = updateBatchActualRow(index) { it.copy(moldingActual = value) }

    private fun updateBatchActualRow(index: Int, transform: (BatchActualRow) -> BatchActualRow) {
        _uiState.update { state ->
            val rows = state.batchActualRows.toMutableList()
            if (index !in rows.indices) return@update state
            rows[index] = transform(rows[index])
            state.copy(batchActualRows = rows)
        }
    }

    fun resetBatchActualRows() {
        val date = _uiState.value.batchActualDate
        _uiState.update { it.copy(batchActualRows = DataMgmtBatchLogic.emptyBatchActualRows(date)) }
    }

    fun submitBatchActual() {
        val date = _uiState.value.batchActualDate.trim()
        if (date.isBlank()) {
            notifyMessage("日付を選択してください")
            return
        }
        val rows = _uiState.value.batchActualRows
        val validRows = rows.filter { it.productCd.isNotBlank() }
        if (validRows.isEmpty()) {
            notifyMessage("少なくとも1つの製品を選択してください")
            return
        }
        val hasAnyActual = validRows.any {
            (it.cuttingActual != null && it.cuttingActual != 0) ||
                (it.chamferingActual != null && it.chamferingActual != 0) ||
                (it.moldingActual != null && it.moldingActual != 0)
        }
        if (!hasAnyActual) {
            notifyMessage("少なくとも1つの実績を入力してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(batchActualSaving = true) }
            runCatching { repository.submitBatchActual(date, rows) }
                .onSuccess { msg ->
                    _uiState.update {
                        it.copy(
                            batchActualSaving = false,
                            showBatchActualDialog = false,
                            snackbarMessage = msg,
                        )
                    }
                    resetBatchActualRows()
                    fetchData()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(batchActualSaving = false, snackbarMessage = e.message ?: "実績一括登録に失敗しました")
                    }
                }
        }
    }

    private fun executeDirectUpdate(action: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching { runRepositoryAction(action) }
                .onSuccess { msg ->
                    fetchData()
                    notifyMessage(msg)
                }
                .onFailure { e ->
                    notifyMessage(e.message ?: "更新に失敗しました")
                }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    private fun executeConfirmedUpdate(action: String) {
        viewModelScope.launch {
            val title = progressTitleFor(action)
            val initialText = progressInitialTextFor(action)
            startProgress(title, initialText)
            val progressJob = launchProgressTicker()
            runCatching { runRepositoryAction(action) }
                .onSuccess { msg ->
                    progressJob.cancel()
                    finishProgressSuccess(msg)
                    fetchData()
                }
                .onFailure { e ->
                    progressJob.cancel()
                    finishProgressError(progressErrorFor(action, e))
                }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    private suspend fun runRepositoryAction(action: String): String = when (action) {
        "update-order" -> repository.updateFromOrderDaily(allMode = false)
        "carry-over" -> repository.updateCarryOver()
        "actual" -> repository.updateActual()
        "defect" -> repository.updateDefect()
        "scrap" -> repository.updateScrap()
        "on-hold" -> repository.updateOnHold()
        "production-dates" -> repository.updateProductionDates()
        "plan" -> repository.updatePlan()
        else -> throw IllegalArgumentException("未対応の操作です")
    }

    private fun progressTitleFor(action: String): String = when (action) {
        "carry-over" -> "繰越データ更新"
        "actual" -> "実績データ更新"
        "defect" -> "不良データ更新"
        "scrap" -> "廃棄データ更新"
        "on-hold" -> "保留データ更新"
        "production-dates" -> "生産計画日更新"
        "plan" -> "計画データ更新"
        else -> "更新中"
    }

    private fun progressInitialTextFor(action: String): String = when (action) {
        "carry-over" -> "繰越フィールドをクリア中..."
        "actual" -> "実績データを取得中..."
        "defect" -> "不良データを取得中..."
        "scrap" -> "廃棄データを取得中..."
        "on-hold" -> "保留データを取得中..."
        "production-dates" -> "生産計画日データを取得中..."
        "plan" -> "計画データを取得中..."
        else -> "処理中..."
    }

    private fun progressErrorFor(action: String, error: Throwable): String = when (action) {
        "carry-over" -> "繰越データ更新に失敗しました"
        "actual" -> "実績データ更新に失敗しました"
        "defect" -> "不良データ更新に失敗しました"
        "scrap" -> "廃棄データ更新に失敗しました"
        "on-hold" -> "保留データ更新に失敗しました"
        "production-dates" -> "生産計画日更新に失敗しました"
        "plan" -> "計画データ更新に失敗しました"
        else -> error.message ?: "更新に失敗しました"
    }

    private fun startProgress(title: String, text: String) {
        _uiState.update {
            it.copy(
                actionLoading = true,
                showProgressDialog = true,
                progressDialogTitle = title,
                progressPercentage = 0f,
                progressText = text,
                progressStatus = DataMgmtProgressStatus.Running,
            )
        }
    }

    private suspend fun finishProgressSuccess(message: String) {
        val successMsg = message.ifBlank { "更新が完了しました" }
        _uiState.update {
            it.copy(
                progressPercentage = 100f,
                progressStatus = DataMgmtProgressStatus.Success,
                progressText = successMsg,
            )
        }
        delay(1500)
        _uiState.update {
            it.copy(
                showProgressDialog = false,
                snackbarMessage = successMsg,
            )
        }
    }

    private suspend fun finishProgressError(message: String) {
        _uiState.update {
            it.copy(
                progressPercentage = 100f,
                progressStatus = DataMgmtProgressStatus.Error,
                progressText = message,
            )
        }
        delay(2000)
        _uiState.update {
            it.copy(
                showProgressDialog = false,
                snackbarMessage = message,
            )
        }
    }

    fun openProcessPrintDialog() {
        _uiState.update {
            it.copy(
                showProcessPrintDateDialog = true,
                processPrintTargetDate = ProcessPlanPrintLogic.defaultTargetDate(),
            )
        }
    }

    fun closeProcessPrintDateDialog() = _uiState.update { it.copy(showProcessPrintDateDialog = false) }

    fun setProcessPrintTargetDate(value: String) = _uiState.update { it.copy(processPrintTargetDate = value) }

    fun confirmProcessPrint() {
        val targetDate = ProcessPlanPrintLogic.normalizeTargetDate(_uiState.value.processPrintTargetDate)
        if (targetDate.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "日付を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showProcessPrintDateDialog = false,
                    showProgressDialog = true,
                    progressDialogTitle = "工程別計画確認印刷",
                    progressPercentage = 0f,
                    progressText = "印刷データを取得中...",
                    progressStatus = DataMgmtProgressStatus.Running,
                )
            }
            val progressJob = launchProgressTicker()
            runCatching {
                withContext(Dispatchers.IO) {
                    val accumulator = repository.prepareProcessPrint(targetDate)
                    if (accumulator.totalRowsIngested == 0) {
                        throw IllegalStateException("印刷できるデータがありません。")
                    }
                    accumulator.buildHtml()
                }
            }.onSuccess { html ->
                progressJob.cancel()
                queuePrint(
                    html = html,
                    jobName = "工程別生産計画確認サマリー",
                    resultMessage = "印刷プレビューを表示しました",
                )
            }.onFailure { e ->
                progressJob.cancel()
                _uiState.update {
                    it.copy(
                        showProgressDialog = false,
                        snackbarMessage = resolveProcessPrintError(e),
                    )
                }
            }
        }
    }

    fun takePendingPrint(): PendingPrintPayload? = pendingPrint.also { pendingPrint = null }

    private fun queuePrint(
        html: String,
        jobName: String,
        resultMessage: String,
        layout: PrintPageLayout = PrintPageLayout.A4_PORTRAIT_SINGLE,
    ) {
        pendingPrint = PendingPrintPayload(html, jobName, resultMessage, layout)
        _uiState.update {
            it.copy(
                showProgressDialog = false,
                processPrintRequestId = it.processPrintRequestId + 1,
            )
        }
    }

    private fun resolveProcessPrintError(error: Throwable): String {
        val raw = error.message?.trim().orEmpty()
        return when {
            raw.contains("timeout", ignoreCase = true) ||
                raw.contains("timed out", ignoreCase = true) ->
                "印刷データの取得がタイムアウトしました。しばらくしてから再試行してください。"
            raw.isNotBlank() -> raw
            else -> "印刷データの取得に失敗しました"
        }
    }

    fun runPrintAction(action: String) {
        val recommendedConfig = RecommendedProductionPrintLogic.PrintConfig.fromAction(action)
        if (recommendedConfig != null) {
            _uiState.update { it.copy(showRecommendedPrintMenu = false, showProductionPlanMenu = false) }
            runRecommendedProductionPrint(recommendedConfig)
            return
        }
        when (action) {
            "molding-plan-create" -> openPlanCreateDialog(PlanCreateKind.Molding)
            "welding-plan-create" -> openPlanCreateDialog(PlanCreateKind.Welding)
            else -> _uiState.update {
                it.copy(
                    showRecommendedPrintMenu = false,
                    showProductionPlanMenu = false,
                    snackbarMessage = "未対応の印刷操作です",
                )
            }
        }
    }

    fun openPlanCreateDialog(kind: PlanCreateKind) {
        val form = ProductionPlanCreateLogic.defaultFormState()
        _uiState.update {
            it.copy(
                showRecommendedPrintMenu = false,
                showProductionPlanMenu = false,
                showPlanCreateDialog = true,
                planCreateKind = kind,
                planCreateForm = form,
                planCreateLoading = false,
                planCreateClearLoading = false,
                planCreateInventoryTrendLoading = false,
            )
        }
        refreshPlanCreateWorkingDays(form.month)
    }

    fun closePlanCreateDialog() = _uiState.update {
        it.copy(
            showPlanCreateDialog = false,
            planCreateKind = null,
            showPlanCreateClearConfirm = false,
            showPlanCreateInventoryTrendConfirm = false,
        )
    }

    fun setPlanCreateMonth(month: String) {
        val next = ProductionPlanCreateLogic.applyMonthChange(month)
        _uiState.update { state ->
            state.copy(planCreateForm = state.planCreateForm.copy(
                month = next.month,
                baseDate = next.baseDate,
                workingDays = next.workingDays,
                clearFromDate = next.clearFromDate,
            ))
        }
        refreshPlanCreateWorkingDays(month)
    }

    private fun refreshPlanCreateWorkingDays(month: String) {
        if (month.isBlank()) return
        viewModelScope.launch {
            val days = withContext(Dispatchers.IO) {
                masterRepository.loadScheduledWorkdaysForMonth(month)
            }
            _uiState.update { state ->
                if (state.planCreateForm.month != month) return@update state
                state.copy(planCreateForm = state.planCreateForm.copy(workingDays = days))
            }
        }
    }

    fun setPlanCreateBaseDate(value: String) = _uiState.update {
        it.copy(planCreateForm = it.planCreateForm.copy(baseDate = value))
    }

    fun setPlanCreateWorkingDays(value: Int) = _uiState.update {
        it.copy(planCreateForm = it.planCreateForm.copy(workingDays = value.coerceIn(1, 31)))
    }

    fun setPlanCreateCoefficient(value: Double) = _uiState.update {
        it.copy(planCreateForm = it.planCreateForm.copy(coefficient = value.coerceIn(1.0, 2.0)))
    }

    fun setPlanCreateClearFromDate(value: String) = _uiState.update {
        it.copy(planCreateForm = it.planCreateForm.copy(clearFromDate = value))
    }

    fun executePlanCreate() {
        val kind = _uiState.value.planCreateKind ?: return
        val form = _uiState.value.planCreateForm
        if (form.baseDate.isBlank()) {
            notifyMessage("基準日を選択してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(planCreateLoading = true) }
            runCatching {
                withContext(Dispatchers.IO) { repository.executePlanCreate(kind, form) }
            }.onSuccess { rows ->
                _uiState.update { state ->
                    state.copy(
                        planCreateForm = state.planCreateForm.copy(results = rows),
                        planCreateLoading = false,
                    )
                }
                when {
                    rows.isEmpty() -> notifyMessage(
                        "対象データがありません（対応日の production_summarys 行なし、または実計推移が 0 以上）",
                    )
                    else -> notifyMessage("計算が完了しました（${rows.size} 件）")
                }
            }.onFailure { e ->
                _uiState.update { it.copy(planCreateLoading = false) }
                notifyMessage(e.message ?: "計算に失敗しました")
            }
        }
    }

    fun printPlanCreateResult() {
        val kind = _uiState.value.planCreateKind ?: return
        val form = _uiState.value.planCreateForm
        val rows = form.results
        if (rows.isEmpty()) {
            notifyMessage("印刷するデータがありません")
            return
        }
        val html = ProductionPlanCreateLogic.buildPrintHtml(kind, form, rows)
        queuePrint(html = html, jobName = kind.title, resultMessage = "印刷プレビューを表示しました")
    }

    fun requestPlanCreateClear() {
        if (_uiState.value.planCreateForm.clearFromDate.isBlank()) {
            notifyMessage("計画クリア開始日を選択してください")
            return
        }
        _uiState.update { it.copy(showPlanCreateClearConfirm = true) }
    }

    fun closePlanCreateClearConfirm() = _uiState.update { it.copy(showPlanCreateClearConfirm = false) }

    fun confirmPlanCreateClear() {
        val kind = _uiState.value.planCreateKind ?: return
        val startDate = _uiState.value.planCreateForm.clearFromDate.trim()
        if (startDate.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(planCreateClearLoading = true, showPlanCreateClearConfirm = false) }
            runCatching {
                withContext(Dispatchers.IO) {
                    when (kind) {
                        PlanCreateKind.Molding -> repository.clearMoldingPlan(startDate)
                        PlanCreateKind.Welding -> repository.clearWeldingPlan(startDate)
                    }
                }
            }.onSuccess { msg ->
                _uiState.update { it.copy(planCreateClearLoading = false) }
                notifyMessage(msg)
                fetchData()
            }.onFailure { e ->
                _uiState.update { it.copy(planCreateClearLoading = false) }
                notifyMessage(e.message ?: "クリアに失敗しました")
            }
        }
    }

    fun requestPlanCreateInventoryTrend() {
        _uiState.update { it.copy(showPlanCreateInventoryTrendConfirm = true) }
    }

    fun closePlanCreateInventoryTrendConfirm() =
        _uiState.update { it.copy(showPlanCreateInventoryTrendConfirm = false) }

    fun confirmPlanCreateInventoryTrend() {
        _uiState.update { it.copy(showPlanCreateInventoryTrendConfirm = false) }
        viewModelScope.launch {
            _uiState.update { it.copy(planCreateInventoryTrendLoading = true) }
            startProgress("在庫・推移更新", "在庫・推移データを取得中...")
            val progressJob = launchProgressTicker()
            runCatching {
                repository.updateInventoryTrendSequence { stepText ->
                    _uiState.update { it.copy(progressText = stepText) }
                }
            }.onSuccess { msg ->
                progressJob.cancel()
                finishProgressSuccess(msg)
                fetchData()
            }.onFailure { e ->
                progressJob.cancel()
                finishProgressError(e.message ?: "在庫・推移更新に失敗しました")
            }
            _uiState.update { it.copy(planCreateInventoryTrendLoading = false) }
        }
    }

    fun openPlanMachineConfigDialog() {
        _uiState.update { it.copy(showPlanMachineConfigDialog = true, planMachineConfigLoading = true) }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val rows = repository.loadProductMachineConfig()
                    val machines = repository.loadMachineOptions()
                    val options = machines.mapNotNull { m ->
                        val cd = m.machineCd.orEmpty().trim()
                        val name = m.machineName.orEmpty().trim()
                        if (cd.isBlank()) null else "$name ($cd)" to cd
                    }
                    rows to options
                }
            }.onSuccess { (rows, options) ->
                _uiState.update {
                    it.copy(planMachineConfigRows = rows, planMachineOptions = options, planMachineConfigLoading = false)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(showPlanMachineConfigDialog = false, planMachineConfigLoading = false) }
                notifyMessage(e.message ?: "データの取得に失敗しました")
            }
        }
    }

    fun closePlanMachineConfigDialog() = _uiState.update { it.copy(showPlanMachineConfigDialog = false) }

    fun updatePlanMachineConfig(row: ProductMachineConfigRowDto, machine: String) {
        val id = row.id ?: return
        val kind = _uiState.value.planCreateKind ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(planMachineConfigSavingId = id) }
            runCatching {
                withContext(Dispatchers.IO) {
                    when (kind) {
                        PlanCreateKind.Molding -> repository.updateProductMachineConfigMolding(id, machine)
                        PlanCreateKind.Welding -> repository.updateProductMachineConfigWelding(id, machine)
                    }
                }
            }.onSuccess {
                _uiState.update { state ->
                    val updated = state.planMachineConfigRows.map { r ->
                        if (r.id != id) r else when (kind) {
                            PlanCreateKind.Molding -> r.copy(moldingMachine = machine.ifBlank { null })
                            PlanCreateKind.Welding -> r.copy(weldingMachine = machine.ifBlank { null })
                        }
                    }
                    state.copy(planMachineConfigRows = updated, planMachineConfigSavingId = null)
                }
                notifyMessage(if (kind == PlanCreateKind.Molding) "成型機を保存しました" else "溶接機を保存しました")
            }.onFailure { e ->
                _uiState.update { it.copy(planMachineConfigSavingId = null) }
                notifyMessage(e.message ?: "保存に失敗しました")
            }
        }
    }

    fun openPlanBomDialog() {
        val kind = _uiState.value.planCreateKind ?: return
        _uiState.update {
            it.copy(
                showPlanBomDialog = true,
                planBomLoading = true,
                planBomSelected = emptySet(),
                planBomBulkField = PlanBomBulkField.Safety,
            )
        }
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val bom = repository.fetchAllProductProcessBom()
                    val products = repository.fetchAllMasterProducts()
                    ProductionPlanCreateLogic.filterBomRows(
                        ProductionPlanCreateLogic.toBomRows(bom, kind),
                        products,
                    )
                }
            }.onSuccess { rows ->
                _uiState.update { it.copy(planBomRows = rows, planBomLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(showPlanBomDialog = false, planBomLoading = false) }
                notifyMessage(e.message ?: "読み込みに失敗しました")
            }
        }
    }

    fun closePlanBomDialog() = _uiState.update { it.copy(showPlanBomDialog = false) }

    fun setPlanBomBulkField(field: PlanBomBulkField) = _uiState.update { it.copy(planBomBulkField = field) }

    fun togglePlanBomSelect(productCd: Int) = _uiState.update { state ->
        val next = state.planBomSelected.toMutableSet()
        if (productCd in next) next.remove(productCd) else next.add(productCd)
        state.copy(planBomSelected = next)
    }

    fun selectAllPlanBom() = _uiState.update { state ->
        val all = state.planBomRows.map { it.productCd }.toSet()
        val next = if (state.planBomSelected.size == all.size) emptySet() else all
        state.copy(planBomSelected = next)
    }

    fun setPlanBomSafety(productCd: Int, value: Int) {
        val clamped = ProductionPlanCreateLogic.clampBomNum(value)
        _uiState.update { state ->
            state.copy(planBomRows = state.planBomRows.map {
                if (it.productCd != productCd) it else it.copy(safetyStockDays = clamped)
            })
        }
        savePlanBomRow(productCd)
    }

    fun setPlanBomProcessLt(productCd: Int, value: Int) {
        val clamped = ProductionPlanCreateLogic.clampBomNum(value)
        _uiState.update { state ->
            state.copy(planBomRows = state.planBomRows.map {
                if (it.productCd != productCd) it else it.copy(processLt = clamped)
            })
        }
        savePlanBomRow(productCd)
    }

    private var planBomSaveJob: Job? = null

    private fun savePlanBomRow(productCd: Int) {
        val kind = _uiState.value.planCreateKind ?: return
        val row = _uiState.value.planBomRows.find { it.productCd == productCd } ?: return
        planBomSaveJob?.cancel()
        planBomSaveJob = viewModelScope.launch {
            delay(600)
            runCatching {
                withContext(Dispatchers.IO) {
                    when (kind) {
                        PlanCreateKind.Molding -> repository.updateProductProcessBomMolding(
                            productCd, row.safetyStockDays, row.processLt,
                        )
                        PlanCreateKind.Welding -> repository.updateProductProcessBomWelding(
                            productCd, row.safetyStockDays, row.processLt,
                        )
                    }
                }
            }.onFailure { e -> notifyMessage(e.message ?: "保存に失敗しました") }
        }
    }

    fun applyPlanBomBulkDelta(delta: Int) {
        val kind = _uiState.value.planCreateKind ?: return
        val selected = _uiState.value.planBomSelected
        if (selected.isEmpty()) {
            notifyMessage("行を選択してください")
            return
        }
        val field = _uiState.value.planBomBulkField
        viewModelScope.launch {
            _uiState.update { it.copy(planBomBulkLoading = true) }
            val updatedRows = _uiState.value.planBomRows.map { row ->
                if (row.productCd !in selected) row else {
                    val safety = when (field) {
                        PlanBomBulkField.Safety, PlanBomBulkField.Both ->
                            ProductionPlanCreateLogic.clampBomNum(row.safetyStockDays + delta)
                        else -> row.safetyStockDays
                    }
                    val lt = when (field) {
                        PlanBomBulkField.ProcessLt, PlanBomBulkField.Both ->
                            ProductionPlanCreateLogic.clampBomNum(row.processLt + delta)
                        else -> row.processLt
                    }
                    row.copy(safetyStockDays = safety, processLt = lt)
                }
            }
            _uiState.update { it.copy(planBomRows = updatedRows) }
            runCatching {
                withContext(Dispatchers.IO) {
                    updatedRows.filter { it.productCd in selected }.forEach { row ->
                        when (kind) {
                            PlanCreateKind.Molding -> repository.updateProductProcessBomMolding(
                                row.productCd, row.safetyStockDays, row.processLt,
                            )
                            PlanCreateKind.Welding -> repository.updateProductProcessBomWelding(
                                row.productCd, row.safetyStockDays, row.processLt,
                            )
                        }
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(planBomBulkLoading = false) }
                notifyMessage("${selected.size}件を更新しました")
            }.onFailure { e ->
                _uiState.update { it.copy(planBomBulkLoading = false) }
                notifyMessage(e.message ?: "一括更新に失敗しました")
            }
        }
    }

    private fun runRecommendedProductionPrint(config: RecommendedProductionPrintLogic.PrintConfig) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showProgressDialog = true,
                    progressDialogTitle = "推奨生産日",
                    progressPercentage = 0f,
                    progressText = "リスト用データを取得しています...",
                    progressStatus = DataMgmtProgressStatus.Running,
                )
            }
            val progressJob = launchProgressTicker()
            runCatching {
                withContext(Dispatchers.IO) {
                    val (monthStart, rangeEnd) = ProductionDataManagementLogic.generateDateRange()
                    val collector = RecommendedProductionPrintLogic.Collector(monthStart, config)
                    repository.forEachSummaryInRange(monthStart, rangeEnd) { page ->
                        collector.ingest(page)
                    }
                    val rows = collector.sortedRows()
                    val html = RecommendedProductionPrintLogic.buildHtml(rows, config, monthStart, rangeEnd)
                    Triple(html, rows.size, config.jobName)
                }
            }.onSuccess { (html, count, jobName) ->
                progressJob.cancel()
                val message = if (count > 0) {
                    "プレビューを表示しました（$count 件）"
                } else {
                    "該当行はありません。プレビュー後に印刷ダイアログが開きます"
                }
                queuePrint(
                    html = html,
                    jobName = jobName,
                    resultMessage = message,
                    layout = PrintPageLayout.A4_PORTRAIT_DUPLEX_COLOR,
                )
            }.onFailure { e ->
                progressJob.cancel()
                _uiState.update {
                    it.copy(
                        showProgressDialog = false,
                        snackbarMessage = resolveProcessPrintError(e),
                    )
                }
            }
        }
    }

    fun openGenerateConfirmDialog() {
        val (start, end) = ProductionDataManagementLogic.generateDateRange()
        _uiState.update {
            it.copy(
                showUpdateMenu = false,
                showGenerateConfirm = true,
                generateStartDate = start,
                generateEndDate = end,
            )
        }
    }

    fun closeGenerateConfirmDialog() = _uiState.update { it.copy(showGenerateConfirm = false) }

    fun setGenerateStartDate(value: String) = _uiState.update { it.copy(generateStartDate = value) }

    fun setGenerateEndDate(value: String) = _uiState.update { it.copy(generateEndDate = value) }

    fun confirmGenerateData() {
        val start = _uiState.value.generateStartDate.trim()
        val end = _uiState.value.generateEndDate.trim()
        if (start.isBlank() || end.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showGenerateConfirm = false,
                    showProgressDialog = true,
                    progressDialogTitle = "データ生成中",
                    progressPercentage = 0f,
                    progressText = "データ生成中...",
                    progressStatus = DataMgmtProgressStatus.Running,
                    actionLoading = true,
                )
            }
            val progressJob = launchProgressTicker()
            runCatching {
                repository.generate(start, end)
            }.onSuccess { msg ->
                progressJob.cancel()
                finishProgressSuccess(msg.ifBlank { "データ生成が完了しました！" })
                fetchData()
            }.onFailure { e ->
                progressJob.cancel()
                finishProgressError(e.message ?: "データ生成に失敗しました")
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    private fun launchProgressTicker(): Job = viewModelScope.launch {
        while (isActive) {
            delay(300)
            _uiState.update { state ->
                if (!state.showProgressDialog || state.progressStatus != DataMgmtProgressStatus.Running) {
                    return@update state
                }
                val increment = 4f + Random.nextFloat() * 8f
                val next = (state.progressPercentage + increment).coerceAtMost(95f)
                state.copy(progressPercentage = next)
            }
        }
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(
        private val repository: ProductionSummaryRepository,
        private val masterRepository: MasterRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductionDataManagementViewModel(repository, masterRepository) as T
    }

    private companion object {
        fun createInitialUiState(): ProductionDataManagementUiState {
            val (start, end) = runCatching { ProductionDataManagementLogic.defaultDateRange() }
                .getOrElse { "" to "" }
            return ProductionDataManagementUiState(startDate = start, endDate = end)
        }

        fun ensureDateRange(state: ProductionDataManagementUiState): ProductionDataManagementUiState {
            if (state.startDate.isNotBlank() && state.endDate.isNotBlank()) return state
            val (start, end) = ProductionDataManagementLogic.defaultDateRange()
            return state.copy(startDate = start, endDate = end)
        }
    }
}
