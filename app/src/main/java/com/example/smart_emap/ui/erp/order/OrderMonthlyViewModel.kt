package com.example.smart_emap.ui.erp.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.BatchCreateProductItemDto
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.OrderDailyEditRowUi
import com.example.smart_emap.data.model.OrderMonthlyCreateDto
import com.example.smart_emap.data.model.OrderMonthlyItemDto
import com.example.smart_emap.data.model.OrderMonthlySummaryDto
import com.example.smart_emap.data.repository.OrderDailyUiMapper
import com.example.smart_emap.data.repository.OrderMonthlyFilters
import com.example.smart_emap.data.repository.OrderMonthlyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private fun japanNowDate(): LocalDate = LocalDate.now(ZoneId.of("Asia/Tokyo"))

data class BatchProductRowUi(
    val productCd: String,
    val productName: String,
    val productType: String,
    val forecastUnits: Int,
    val quantity: String,
    val exists: Boolean,
    val orderMonthlyId: Int?,
)

data class OrderMonthlyEditFormUi(
    val destinationCd: String = "",
    val destinationName: String = "",
    val year: Int = 0,
    val month: Int = 0,
    val productCd: String = "",
    val productName: String = "",
    val productAlias: String = "",
    val productType: String = "量産品",
    val forecastUnits: String = "0",
    val forecastTotalUnits: Int = 0,
)

enum class OrderMonthlyDialog {
    None,
    GenerateDaily,
    ForecastUpdate,
    UpdateFields,
    Edit,
    Delete,
    BatchRegister,
    DailyBatchEdit,
    DailyManage,
    BatchConfirm,
    UpdateFieldsConfirm,
}

data class OrderMonthlyUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val year: Int = japanNowDate().year,
    val month: Int = japanNowDate().monthValue,
    val destinationCd: String = "",
    val keyword: String = "",
    val destinationOptions: List<DestinationOptionDto> = emptyList(),
    val summary: OrderMonthlySummaryDto = OrderMonthlySummaryDto(),
    val allItems: List<OrderMonthlyItemDto> = emptyList(),
    val pageItems: List<OrderMonthlyItemDto> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val activeDialog: OrderMonthlyDialog = OrderMonthlyDialog.None,
    val editId: Int? = null,
    val editForm: OrderMonthlyEditFormUi = OrderMonthlyEditFormUi(),
    val deleteTarget: OrderMonthlyItemDto? = null,
    val generateDailyDestinationLabel: String = "全納入先",
    val updateFieldsStartDate: String = "",
    val updateFieldsSyncProduct: Boolean = true,
    val batchYear: Int = japanNowDate().year,
    val batchMonth: Int = japanNowDate().monthValue,
    val batchDestinationCd: String = "",
    val batchProducts: List<BatchProductRowUi> = emptyList(),
    val batchLoading: Boolean = false,
    val batchPendingCreateCount: Int = 0,
    val batchPendingUpdateCount: Int = 0,
    val dailyBatchOrderId: String = "",
    val dailyBatchRows: List<OrderDailyEditRowUi> = emptyList(),
    val dailyBatchChangedIds: Set<Int> = emptySet(),
    val dailyBatchLoading: Boolean = false,
    val dailyBatchSaving: Boolean = false,
    val dailyManageDate: String = "",
    val dailyManageDestinationCd: String = "",
    val dailyManageRows: List<OrderDailyEditRowUi> = emptyList(),
    val dailyManageChangedIds: Set<Int> = emptySet(),
    val dailyManageLoading: Boolean = false,
    val dailyManageSaving: Boolean = false,
    val pendingShareText: String? = null,
    val actionLoading: Boolean = false,
    val progressVisible: Boolean = false,
    val progressPercent: Int = 0,
    val snackbarMessage: String? = null,
    val snackbarIsError: Boolean = false,
)

class OrderMonthlyViewModel(
    private val repository: OrderMonthlyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderMonthlyUiState())
    val uiState: StateFlow<OrderMonthlyUiState> = _uiState.asStateFlow()

    private var keywordJob: Job? = null

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            runCatching { repository.loadDestinationOptions() }
                .onSuccess { options ->
                    _uiState.update { it.copy(destinationOptions = options) }
                }
            loadListInternal(resetPage = false)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun setYear(year: Int) {
        _uiState.update { it.copy(year = year, page = 1) }
        loadList()
    }

    fun setMonth(month: Int) {
        _uiState.update { it.copy(month = month, page = 1) }
        loadList()
    }

    fun setDestinationCd(cd: String) {
        _uiState.update { it.copy(destinationCd = cd, page = 1) }
        loadList()
    }

    fun setKeyword(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
        keywordJob?.cancel()
        keywordJob = viewModelScope.launch {
            delay(300)
            _uiState.update { it.copy(page = 1) }
            loadListInternal(resetPage = true)
        }
    }

    fun goPrevPeriod() {
        val state = _uiState.value
        if (state.month <= 1) {
            _uiState.update { it.copy(year = state.year - 1, month = 12, page = 1) }
        } else {
            _uiState.update { it.copy(month = state.month - 1, page = 1) }
        }
        loadList()
    }

    fun goNextPeriod() {
        val state = _uiState.value
        if (state.month >= 12) {
            _uiState.update { it.copy(year = state.year + 1, month = 1, page = 1) }
        } else {
            _uiState.update { it.copy(month = state.month + 1, page = 1) }
        }
        loadList()
    }

    fun goCurrentMonth() {
        val now = japanNow()
        _uiState.update { it.copy(year = now.year, month = now.monthValue, page = 1) }
        loadList()
    }

    fun setPage(page: Int) {
        _uiState.update { state ->
            val start = (page - 1) * state.pageSize
            state.copy(
                page = page,
                pageItems = state.allItems.drop(start).take(state.pageSize),
            )
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = OrderMonthlyDialog.None) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun openGenerateDailyDialog() {
        val state = _uiState.value
        val label = state.destinationCd.takeIf { it.isNotBlank() }?.let { cd ->
            state.destinationOptions.find { it.cd == cd }?.let { "${it.cd} | ${it.name}" } ?: cd
        } ?: "全納入先"
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.GenerateDaily,
                generateDailyDestinationLabel = label,
            )
        }
    }

    fun confirmGenerateDaily() {
        val state = _uiState.value
        dismissDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, progressVisible = true, progressPercent = 0) }
            runCatching {
                repository.generateDailyOrders(state.year, state.month, state.destinationCd.takeIf { it.isNotBlank() })
            }.onSuccess {
                showMessage("日受注リストを生成しました")
                loadListInternal(resetPage = false)
                _uiState.update { it.copy(progressPercent = 100) }
                delay(800)
                _uiState.update { it.copy(progressVisible = false, progressPercent = 0) }
            }.onFailure { e ->
                val msg = e.message.orEmpty()
                if (msg.contains("timeout", true) || msg.contains("Network", true)) {
                    showMessage("バックエンドで処理中です。しばらくしてから再読込してください")
                    startPolling()
                } else {
                    showError("日受注生成に失敗しました: $msg")
                    _uiState.update { it.copy(progressVisible = false) }
                }
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openForecastUpdateDialog() {
        _uiState.update { it.copy(activeDialog = OrderMonthlyDialog.ForecastUpdate) }
    }

    fun confirmForecastUpdate() {
        dismissDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true, progressVisible = true, progressPercent = 0) }
            runCatching {
                repository.runForecastUpdate(
                    destinationCd = _uiState.value.destinationCd.takeIf { it.isNotBlank() },
                    onProgress = { pct -> _uiState.update { s -> s.copy(progressPercent = pct) } },
                )
            }.onSuccess { result ->
                val msg = if (result.totalCount == 0) {
                    "更新対象がありませんでした"
                } else {
                    "内示本数更新完了（Step1:${result.step1Count} Step2:${result.step2Count} Step3:${result.step3Count} Step4:${result.step4Count}、計${result.totalReflected}件）"
                }
                showMessage(msg)
                loadListInternal(resetPage = false)
                delay(1000)
                _uiState.update { it.copy(progressVisible = false, progressPercent = 0) }
            }.onFailure { e ->
                showError(e.message ?: "内示本数更新に失敗しました")
                _uiState.update { it.copy(progressVisible = false) }
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openUpdateFieldsDialog() {
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.UpdateFields,
                updateFieldsStartDate = "",
                updateFieldsSyncProduct = true,
            )
        }
    }

    fun setUpdateFieldsStartDate(value: String) {
        _uiState.update { it.copy(updateFieldsStartDate = value) }
    }

    fun setUpdateFieldsSyncProduct(value: Boolean) {
        _uiState.update { it.copy(updateFieldsSyncProduct = value) }
    }

    fun submitUpdateFieldsRequest() {
        val startDate = _uiState.value.updateFieldsStartDate.trim()
        if (startDate.isBlank()) {
            showError("開始日を選択してください")
            return
        }
        _uiState.update { it.copy(activeDialog = OrderMonthlyDialog.UpdateFieldsConfirm) }
    }

    fun confirmUpdateFields() {
        val state = _uiState.value
        dismissDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                repository.updateOrderFields(state.updateFieldsStartDate.trim(), state.updateFieldsSyncProduct)
            }.onSuccess { res ->
                showMessage("更新完了（${res.updatedCount}件）")
                loadListInternal(resetPage = false)
            }.onFailure { e ->
                showError(e.message ?: "更新に失敗しました")
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openEditDialog(row: OrderMonthlyItemDto) {
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.Edit,
                editId = row.id,
                editForm = OrderMonthlyEditFormUi(
                    destinationCd = row.destinationCd,
                    destinationName = row.destinationName,
                    year = row.year,
                    month = row.month,
                    productCd = row.productCd,
                    productName = row.productName,
                    productAlias = row.productAlias.orEmpty(),
                    productType = row.productType,
                    forecastUnits = row.forecastUnits.toString(),
                    forecastTotalUnits = row.forecastTotalUnits,
                ),
            )
        }
    }

    fun setEditForecastUnits(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { state -> state.copy(editForm = state.editForm.copy(forecastUnits = value)) }
        }
    }

    fun saveEdit() {
        val state = _uiState.value
        val editId = state.editId ?: return
        val forecastUnits = state.editForm.forecastUnits.toIntOrNull() ?: 0
        val form = state.editForm
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching {
                val payload = OrderMonthlyCreateDto(
                    destinationCd = form.destinationCd,
                    destinationName = form.destinationName,
                    year = form.year,
                    month = form.month,
                    productCd = form.productCd,
                    productName = form.productName,
                    productAlias = form.productAlias.takeIf { it.isNotBlank() },
                    productType = form.productType,
                    forecastUnits = forecastUnits,
                    forecastTotalUnits = form.forecastTotalUnits,
                    forecastDiff = form.forecastTotalUnits - forecastUnits,
                )
                repository.updateMonthly(editId, payload)
            }.onSuccess {
                showMessage("更新しました")
                dismissDialog()
                loadListInternal(resetPage = false)
            }.onFailure { e ->
                showError(e.message ?: "保存に失敗しました")
            }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openDeleteDialog(row: OrderMonthlyItemDto) {
        _uiState.update { it.copy(activeDialog = OrderMonthlyDialog.Delete, deleteTarget = row) }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        dismissDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(actionLoading = true) }
            runCatching { repository.deleteMonthly(target.id) }
                .onSuccess {
                    showMessage("削除しました")
                    loadListInternal(resetPage = false)
                }
                .onFailure { e -> showError(e.message ?: "削除に失敗しました") }
            _uiState.update { it.copy(actionLoading = false) }
        }
    }

    fun openBatchDialog() {
        val now = japanNow()
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.BatchRegister,
                batchYear = now.year,
                batchMonth = now.monthValue,
                batchDestinationCd = "",
                batchProducts = emptyList(),
            )
        }
    }

    fun setBatchYear(year: Int) { _uiState.update { it.copy(batchYear = year, batchProducts = emptyList()) } }
    fun setBatchMonth(month: Int) { _uiState.update { it.copy(batchMonth = month, batchProducts = emptyList()) } }
    fun setBatchDestinationCd(cd: String) { _uiState.update { it.copy(batchDestinationCd = cd, batchProducts = emptyList()) } }

    fun batchGoPrevMonth() {
        val s = _uiState.value
        if (s.batchMonth <= 1) _uiState.update { it.copy(batchYear = s.batchYear - 1, batchMonth = 12, batchProducts = emptyList()) }
        else _uiState.update { it.copy(batchMonth = s.batchMonth - 1, batchProducts = emptyList()) }
    }

    fun batchGoNextMonth() {
        val s = _uiState.value
        if (s.batchMonth >= 12) _uiState.update { it.copy(batchYear = s.batchYear + 1, batchMonth = 1, batchProducts = emptyList()) }
        else _uiState.update { it.copy(batchMonth = s.batchMonth + 1, batchProducts = emptyList()) }
    }

    fun batchGoCurrentMonth() {
        val now = japanNow()
        _uiState.update { it.copy(batchYear = now.year, batchMonth = now.monthValue, batchProducts = emptyList()) }
    }

    fun setBatchQuantity(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        _uiState.update { state ->
            val rows = state.batchProducts.toMutableList()
            if (index !in rows.indices) return@update state
            rows[index] = rows[index].copy(quantity = value)
            state.copy(batchProducts = rows)
        }
    }

    fun loadBatchProducts() {
        val state = _uiState.value
        if (state.batchDestinationCd.isBlank()) {
            showError("納入先を選択してください")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(batchLoading = true) }
            runCatching {
                val destinationName = state.destinationOptions.find { it.cd == state.batchDestinationCd }?.name.orEmpty()
                val products = repository.loadOrderProducts(state.batchDestinationCd, state.batchYear, state.batchMonth)
                    .filter { it.productType != "補給品" && it.productType != "試作品" }
                val rows = products.map { p ->
                    BatchProductRowUi(
                        productCd = p.productCd,
                        productName = p.productName,
                        productType = p.productType.ifBlank { "量産品" },
                        forecastUnits = p.forecastUnits,
                        quantity = if (p.forecastUnits > 0) p.forecastUnits.toString() else "",
                        exists = false,
                        orderMonthlyId = null,
                    )
                }
                val checked = rows.map { row ->
                    val check = repository.checkCombinationExists(
                        destinationName = destinationName,
                        productName = row.productName,
                        year = state.batchYear,
                        month = state.batchMonth,
                    )
                    row.copy(
                        exists = check.exists,
                        orderMonthlyId = check.id,
                        forecastUnits = if (check.exists && check.forecastUnits != null) check.forecastUnits else row.forecastUnits,
                        quantity = if (check.exists && check.forecastUnits != null && check.forecastUnits > 0) {
                            check.forecastUnits.toString()
                        } else {
                            row.quantity
                        },
                    )
                }
                checked
            }.onSuccess { rows ->
                _uiState.update { it.copy(batchProducts = rows) }
            }.onFailure { e ->
                showError(e.message ?: "製品の読込に失敗しました")
            }
            _uiState.update { it.copy(batchLoading = false) }
        }
    }

    fun submitBatchRegister() {
        val state = _uiState.value
        val create = state.batchProducts.filter { !it.exists && it.productType != "補給品" && hasQuantity(it.quantity) }
        val update = state.batchProducts.filter {
            it.exists && it.orderMonthlyId != null && hasQuantity(it.quantity) &&
                (it.quantity.toIntOrNull() ?: 0) != it.forecastUnits
        }
        if (create.isEmpty() && update.isEmpty()) {
            showError("登録対象がありません")
            return
        }
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.BatchConfirm,
                batchPendingCreateCount = create.size,
                batchPendingUpdateCount = update.size,
            )
        }
    }

    fun confirmBatchRegister() {
        val state = _uiState.value
        dismissDialog()
        viewModelScope.launch {
            _uiState.update { it.copy(batchLoading = true, actionLoading = true) }
            val destinationName = state.destinationOptions.find { it.cd == state.batchDestinationCd }?.name.orEmpty()
            val create = state.batchProducts.filter { !it.exists && it.productType != "補給品" && hasQuantity(it.quantity) }
            val update = state.batchProducts.filter {
                it.exists && it.orderMonthlyId != null && hasQuantity(it.quantity) &&
                    (it.quantity.toIntOrNull() ?: 0) != it.forecastUnits
            }
            runCatching {
                if (create.isNotEmpty()) {
                    repository.batchCreateMonthly(
                        year = state.batchYear,
                        month = state.batchMonth,
                        destinationCd = state.batchDestinationCd,
                        destinationName = destinationName,
                        products = create.map {
                            BatchCreateProductItemDto(it.productCd, it.quantity.toIntOrNull() ?: 0)
                        },
                    )
                }
                update.forEach { row ->
                    repository.updateMonthly(
                        row.orderMonthlyId!!,
                        OrderMonthlyCreateDto(
                            destinationCd = state.batchDestinationCd,
                            destinationName = destinationName,
                            year = state.batchYear,
                            month = state.batchMonth,
                            productCd = row.productCd,
                            productName = row.productName,
                            productType = row.productType,
                            forecastUnits = row.quantity.toIntOrNull() ?: 0,
                        ),
                    )
                }
            }.onSuccess {
                showMessage("一括登録が完了しました")
                _uiState.update { it.copy(activeDialog = OrderMonthlyDialog.None) }
                loadListInternal(resetPage = false)
            }.onFailure { e ->
                showError(e.message ?: "一括登録に失敗しました")
            }
            _uiState.update { it.copy(batchLoading = false, actionLoading = false) }
        }
    }

    fun clearPendingShareText() {
        _uiState.update { it.copy(pendingShareText = null) }
    }

    fun openDailyBatchDialog(row: OrderMonthlyItemDto) {
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.DailyBatchEdit,
                dailyBatchOrderId = row.orderId,
                dailyBatchRows = emptyList(),
                dailyBatchChangedIds = emptySet(),
            )
        }
        loadDailyBatchRows(row.orderId)
    }

    fun openDailyManageDialog() {
        val today = japanNow().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        _uiState.update {
            it.copy(
                activeDialog = OrderMonthlyDialog.DailyManage,
                dailyManageDate = today,
                dailyManageDestinationCd = it.destinationCd,
                dailyManageRows = emptyList(),
                dailyManageChangedIds = emptySet(),
            )
        }
        loadDailyManageRows()
    }

    fun dailyManagePrevDay() = shiftDailyManageDate(-1)
    fun dailyManageNextDay() = shiftDailyManageDate(1)
    fun dailyManageToday() {
        val today = japanNow().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        _uiState.update { it.copy(dailyManageDate = today, dailyManageChangedIds = emptySet()) }
        loadDailyManageRows()
    }

    fun applyDailyManageShortcut(label: String) {
        val cd = _uiState.value.destinationOptions.firstOrNull { it.name.contains(label) }?.cd.orEmpty()
        _uiState.update { it.copy(dailyManageDestinationCd = cd, dailyManageChangedIds = emptySet()) }
        loadDailyManageRows()
    }

    fun setDailyManageDate(value: String) {
        _uiState.update { it.copy(dailyManageDate = value, dailyManageChangedIds = emptySet()) }
        loadDailyManageRows()
    }

    fun setDailyManageDestinationCd(value: String) {
        _uiState.update { it.copy(dailyManageDestinationCd = value, dailyManageChangedIds = emptySet()) }
        loadDailyManageRows()
    }

    fun setDailyBatchConfirmedBoxes(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        updateDailyBatchRow(index) { row ->
            OrderDailyUiMapper.applyConfirmedBoxesChange(row, value)
        }
    }

    fun setDailyBatchConfirmedUnits(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        updateDailyBatchRow(index) { row -> row.copy(confirmedUnits = value.filter { it.isDigit() }) }
    }

    fun setDailyBatchForecast(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        updateDailyBatchRow(index) { row -> row.copy(forecastUnits = value.filter { it.isDigit() }) }
    }

    fun setDailyManageConfirmedBoxes(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        updateDailyManageRow(index) { row ->
            OrderDailyUiMapper.applyConfirmedBoxesChange(row, value)
        }
    }

    fun setDailyManageConfirmedUnits(index: Int, value: String) {
        if (value.isNotEmpty() && !value.all { it.isDigit() }) return
        updateDailyManageRow(index) { row -> row.copy(confirmedUnits = value.filter { it.isDigit() }) }
    }

    fun applyDailyBatchForecastFromConfirmed() {
        var updated = 0
        _uiState.update { state ->
            val rows = state.dailyBatchRows.map { row ->
                val confirmed = OrderDailyUiMapper.parseNumeric(row.confirmedUnits)
                if (confirmed > 0) {
                    updated++
                    row.copy(forecastUnits = confirmed.toString())
                } else {
                    row
                }
            }
            val changed = state.dailyBatchChangedIds.toMutableSet()
            rows.forEach { row ->
                val confirmed = OrderDailyUiMapper.parseNumeric(row.confirmedUnits)
                if (confirmed > 0) changed.add(row.id)
            }
            state.copy(dailyBatchRows = rows, dailyBatchChangedIds = changed)
        }
        showMessage(
            if (updated > 0) "内示本数を ${updated} 件更新しました" else "更新対象がありません",
        )
    }

    fun shareDailyBatchPrint() {
        val rows = _uiState.value.dailyBatchRows
        if (rows.isEmpty()) {
            showError("印刷データがありません")
            return
        }
        val text = OrderDailyUiMapper.buildPrintText("日別受注編集", rows)
        _uiState.update { it.copy(pendingShareText = text) }
    }

    fun saveDailyBatchChanges() {
        val state = _uiState.value
        if (state.dailyBatchChangedIds.isEmpty()) {
            showError("変更されたデータがありません")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(dailyBatchSaving = true) }
            runCatching {
                val updates = state.dailyBatchRows
                    .filter { it.id in state.dailyBatchChangedIds }
                    .map { OrderDailyUiMapper.toBatchUpdateItem(it) }
                repository.batchUpdateDailyItems(updates)
            }.onSuccess {
                showMessage("一括保存しました")
                loadListInternal(resetPage = false)
                dismissDialog()
            }.onFailure { e ->
                showError(e.message ?: "保存に失敗しました")
            }
            _uiState.update { it.copy(dailyBatchSaving = false) }
        }
    }

    fun saveDailyManageChanges() {
        val state = _uiState.value
        if (state.dailyManageChangedIds.isEmpty()) {
            showError("変更されたデータがありません")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(dailyManageSaving = true) }
            runCatching {
                val updates = state.dailyManageRows
                    .filter { it.id in state.dailyManageChangedIds }
                    .map { OrderDailyUiMapper.toBatchUpdateItem(it) }
                repository.batchUpdateDailyItems(updates)
            }.onSuccess {
                showMessage("一括保存しました")
                loadDailyManageRows()
                loadListInternal(resetPage = false)
            }.onFailure { e ->
                showError(e.message ?: "保存に失敗しました")
            }
            _uiState.update { it.copy(dailyManageSaving = false) }
        }
    }

    private fun updateDailyBatchRow(index: Int, transform: (OrderDailyEditRowUi) -> OrderDailyEditRowUi) {
        _uiState.update { state ->
            val rows = state.dailyBatchRows.toMutableList()
            if (index !in rows.indices) return@update state
            val updated = transform(rows[index])
            rows[index] = updated
            state.copy(
                dailyBatchRows = rows,
                dailyBatchChangedIds = state.dailyBatchChangedIds + updated.id,
            )
        }
    }

    private fun updateDailyManageRow(index: Int, transform: (OrderDailyEditRowUi) -> OrderDailyEditRowUi) {
        _uiState.update { state ->
            val rows = state.dailyManageRows.toMutableList()
            if (index !in rows.indices) return@update state
            val updated = transform(rows[index])
            rows[index] = updated
            state.copy(
                dailyManageRows = rows,
                dailyManageChangedIds = state.dailyManageChangedIds + updated.id,
            )
        }
    }

    private fun shiftDailyManageDate(deltaDays: Long) {
        val current = runCatching {
            LocalDate.parse(_uiState.value.dailyManageDate.trim())
        }.getOrElse { japanNow() }
        val next = current.plusDays(deltaDays).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        _uiState.update { it.copy(dailyManageDate = next, dailyManageChangedIds = emptySet()) }
        loadDailyManageRows()
    }

    private fun loadDailyBatchRows(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(dailyBatchLoading = true) }
            runCatching { repository.loadDailyBatchRows(orderId) }
                .onSuccess { rows ->
                    _uiState.update { it.copy(dailyBatchRows = rows, dailyBatchChangedIds = emptySet()) }
                }
                .onFailure { e -> showError(e.message ?: "日別受注の取得に失敗しました") }
            _uiState.update { it.copy(dailyBatchLoading = false) }
        }
    }

    private fun loadDailyManageRows() {
        val state = _uiState.value
        val date = state.dailyManageDate.trim()
        if (date.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(dailyManageLoading = true) }
            runCatching {
                repository.loadDailyManageRows(date, state.dailyManageDestinationCd.takeIf { it.isNotBlank() })
            }.onSuccess { rows ->
                _uiState.update { it.copy(dailyManageRows = rows, dailyManageChangedIds = emptySet()) }
            }.onFailure { e -> showError(e.message ?: "日受注の取得に失敗しました") }
            _uiState.update { it.copy(dailyManageLoading = false) }
        }
    }

    private fun loadList() {
        viewModelScope.launch { loadListInternal(resetPage = true) }
    }

    private suspend fun loadListInternal(resetPage: Boolean) {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true) }
        val filters = OrderMonthlyFilters(
            year = state.year,
            month = state.month,
            destinationCd = state.destinationCd.takeIf { it.isNotBlank() },
            keyword = state.keyword.takeIf { it.isNotBlank() },
        )
        runCatching {
            val all = repository.loadList(filters)
            val summary = repository.loadSummary(filters)
            Pair(all, summary)
        }.onSuccess { (all, summary) ->
            val page = if (resetPage) 1 else state.page
            val start = (page - 1) * state.pageSize
            _uiState.update {
                it.copy(
                    isLoading = false,
                    allItems = all,
                    total = all.size,
                    summary = summary,
                    page = page,
                    pageItems = all.drop(start).take(state.pageSize),
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false) }
            showError(e.message ?: "読込に失敗しました")
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            repeat(POLL_MAX) {
                delay(POLL_INTERVAL_MS)
                loadListInternal(resetPage = false)
                if (!_uiState.value.isLoading) {
                    showMessage("日受注生成が完了しました")
                    _uiState.update { it.copy(progressVisible = false, progressPercent = 0) }
                    return@launch
                }
            }
        }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message, snackbarIsError = false) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(snackbarMessage = message, snackbarIsError = true) }
    }

    private fun hasQuantity(value: String): Boolean {
        if (value.isBlank()) return false
        val n = value.toIntOrNull() ?: return false
        return n > 0
    }

    class Factory(
        private val repository: OrderMonthlyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderMonthlyViewModel(repository) as T
        }
    }

    companion object {
        private val japanZone = ZoneId.of("Asia/Tokyo")
        private const val POLL_INTERVAL_MS = 5000L
        private const val POLL_MAX = 12

        val DAILY_MANAGE_SHORTCUTS = listOf("愛知", "横浜", "東海", "西浦", "吉良")

        fun japanNow(): LocalDate = japanNowDate()

        fun yearOptions(): List<Int> {
            val y = japanNow().year
            return listOf(y + 1, y, y - 1, y - 2)
        }
    }
}
