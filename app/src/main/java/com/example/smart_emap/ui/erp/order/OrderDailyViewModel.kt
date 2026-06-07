package com.example.smart_emap.ui.erp.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.AddMonthlyOrderBodyDto
import com.example.smart_emap.data.model.DestinationOptionDto
import com.example.smart_emap.data.model.MasterProductItemDto
import com.example.smart_emap.data.model.OrderDailyCreateBodyDto
import com.example.smart_emap.data.model.OrderDailyItemDto
import com.example.smart_emap.data.model.OrderDailySummaryUi
import com.example.smart_emap.data.repository.OrderDailyListFilters
import com.example.smart_emap.data.repository.OrderDailyRepository
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrderDailyPageDialog {
    None,
    Form,
    DeleteConfirm,
}

data class OrderDailyFormUi(
    val editId: Int? = null,
    val monthlyOrderId: String = "",
    val destinationCd: String = "",
    val destinationName: String = "",
    val date: String = "",
    val weekday: String = "",
    val productCd: String = "",
    val productName: String = "",
    val productAlias: String = "",
    val productType: String = "量産品",
    val unitPerBox: String = "0",
    val confirmedBoxes: String = "0",
    val confirmedUnits: Int = 0,
    val forecastUnits: Int = 0,
    val status: String = "未出荷",
    val remarks: String = "",
    val deliveryDate: String = "",
)

data class OrderDailyUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val fullList: List<OrderDailyItemDto> = emptyList(),
    val pageItems: List<OrderDailyItemDto> = emptyList(),
    val summary: OrderDailySummaryUi = OrderDailySummaryUi(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val pageRangeText: String = "表示 0件",
    val startDate: String = "",
    val endDate: String = "",
    val destinationCd: String = "",
    val keyword: String = "",
    val destinationOptions: List<DestinationOptionDto> = emptyList(),
    val allProductOptions: List<MasterProductItemDto> = emptyList(),
    val filteredProductOptions: List<MasterProductItemDto> = emptyList(),
    val lastFetchedText: String = "",
    val snackbarMessage: String? = null,
    val pendingCsvShare: String? = null,
    val activeDialog: OrderDailyPageDialog = OrderDailyPageDialog.None,
    val form: OrderDailyFormUi = OrderDailyFormUi(),
    val formSaving: Boolean = false,
    val deleteTarget: OrderDailyItemDto? = null,
)

class OrderDailyViewModel(
    private val repository: OrderDailyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrderDailyUiState())
    val uiState: StateFlow<OrderDailyUiState> = _uiState.asStateFlow()

    private val japanZone = ZoneId.of("Asia/Tokyo")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val fetchedFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    init {
        val today = todayString()
        _uiState.update { it.copy(startDate = today, endDate = today) }
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            runCatching { repository.loadDestinationOptions() }
                .onSuccess { options ->
                    _uiState.update { state -> state.copy(destinationOptions = options) }
                }
            runCatching { repository.loadProductOptions() }
                .onSuccess { products ->
                    _uiState.update { state ->
                        state.copy(
                            allProductOptions = products,
                            filteredProductOptions = if (state.form.destinationCd.isBlank()) {
                                emptyList()
                            } else {
                                filterProducts(
                                    products,
                                    state.form.destinationCd,
                                    state.form.productCd,
                                )
                            },
                        )
                    }
                }
                .onFailure { showMessage("製品一覧の取得に失敗しました") }
            loadListInternal(showLoading = false)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun loadList() {
        viewModelScope.launch { loadListInternal(showLoading = true) }
    }

    private suspend fun loadListInternal(showLoading: Boolean) {
        val state = _uiState.value
        if (showLoading) _uiState.update { it.copy(isLoading = true) }
        runCatching {
            repository.loadList(
                OrderDailyListFilters(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    destinationCd = state.destinationCd,
                    keyword = state.keyword,
                ),
            )
        }.onSuccess { rows ->
            val summary = repository.summarize(rows)
            _uiState.update {
                it.copy(
                    fullList = rows,
                    summary = summary,
                    lastFetchedText = java.time.ZonedDateTime.now(japanZone).format(fetchedFormatter),
                    page = 1,
                )
            }
            applyPagination()
        }.onFailure { e ->
            _uiState.update {
                it.copy(
                    fullList = emptyList(),
                    pageItems = emptyList(),
                    summary = OrderDailySummaryUi(),
                    total = 0,
                    pageRangeText = "表示 0件",
                    snackbarMessage = e.message ?: "一覧の取得に失敗しました",
                )
            }
        }
        if (showLoading) _uiState.update { it.copy(isLoading = false) }
    }

    fun applyQuickRange(kind: QuickRange) {
        val range = when (kind) {
            QuickRange.Today -> todayString() to todayString()
            QuickRange.Week -> weekRange()
            QuickRange.Month -> monthRange(0)
            QuickRange.LastMonth -> monthRange(-1)
        }
        _uiState.update { it.copy(startDate = range.first, endDate = range.second, page = 1) }
        loadList()
    }

    fun setStartDate(value: String) {
        _uiState.update { it.copy(startDate = value, page = 1) }
        loadList()
    }

    fun setEndDate(value: String) {
        _uiState.update { it.copy(endDate = value, page = 1) }
        loadList()
    }

    fun setDestinationCd(value: String) {
        _uiState.update { it.copy(destinationCd = value, page = 1) }
        loadList()
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value, page = 1) }
        loadList()
    }

    fun setPage(page: Int) {
        _uiState.update { it.copy(page = page.coerceAtLeast(1)) }
        applyPagination()
    }

    fun setPageSize(size: Int) {
        _uiState.update { it.copy(pageSize = size, page = 1) }
        applyPagination()
    }

    fun exportCsv() {
        val rows = _uiState.value.fullList
        if (rows.isEmpty()) {
            showMessage("出力するデータがありません")
            return
        }
        _uiState.update { it.copy(pendingCsvShare = repository.buildCsv(rows)) }
    }

    fun clearPendingCsvShare() {
        _uiState.update { it.copy(pendingCsvShare = null) }
    }

    fun openCreateDialog() {
        val today = todayString()
        _uiState.update {
            it.copy(
                activeDialog = OrderDailyPageDialog.Form,
                form = OrderDailyFormUi(
                    date = today,
                    deliveryDate = today,
                    weekday = weekdayOf(today),
                ),
                filteredProductOptions = emptyList(),
            )
        }
    }

    fun openEditDialog(row: OrderDailyItemDto) {
        _uiState.update { state ->
            state.copy(
                activeDialog = OrderDailyPageDialog.Form,
                form = OrderDailyFormUi(
                    editId = row.id,
                    monthlyOrderId = row.monthlyOrderId.orEmpty(),
                    destinationCd = row.destinationCd,
                    destinationName = row.destinationName.orEmpty(),
                    date = row.date.orEmpty(),
                    weekday = row.weekday.orEmpty(),
                    productCd = row.productCd,
                    productName = row.productName.orEmpty(),
                    productType = row.productType.orEmpty(),
                    unitPerBox = (row.unitPerBox ?: 0).toString(),
                    confirmedBoxes = (row.confirmedBoxes ?: 0).toString(),
                    confirmedUnits = row.confirmedUnits ?: 0,
                    forecastUnits = row.forecastUnits,
                    status = row.status.orEmpty().ifBlank { "未出荷" },
                    remarks = row.remarks.orEmpty(),
                    deliveryDate = row.deliveryDate.orEmpty(),
                ),
                filteredProductOptions = emptyList(),
            )
        }
        loadFormProductOptions(row.destinationCd, row.productCd)
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = OrderDailyPageDialog.None, deleteTarget = null) }
    }

    fun setFormDate(value: String) {
        _uiState.update { state ->
            val delivery = state.form.deliveryDate.ifBlank { value }
            state.copy(
                form = state.form.copy(
                    date = value,
                    weekday = weekdayOf(value),
                    deliveryDate = delivery,
                ),
            )
        }
    }

    fun setFormDeliveryDate(value: String) {
        _uiState.update { state -> state.copy(form = state.form.copy(deliveryDate = value)) }
    }

    fun setFormDestinationCd(value: String) {
        val name = _uiState.value.destinationOptions.find { it.cd == value }?.name.orEmpty()
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    destinationCd = value,
                    destinationName = name,
                    productCd = "",
                    productName = "",
                    productAlias = "",
                    productType = "量産品",
                    unitPerBox = "0",
                    confirmedUnits = 0,
                ),
                filteredProductOptions = emptyList(),
            )
        }
        if (value.isNotBlank()) {
            loadFormProductOptions(value)
        }
    }

    private fun loadFormProductOptions(destinationCd: String, keepProductCd: String = "") {
        viewModelScope.launch {
            runCatching { repository.loadProductOptions(destinationCd) }
                .onSuccess { products ->
                    _uiState.update { state ->
                        state.copy(
                            filteredProductOptions = ensureProductInList(
                                products,
                                keepProductCd.ifBlank { state.form.productCd },
                                state.allProductOptions,
                            ),
                        )
                    }
                }
                .onFailure { showMessage("製品一覧の取得に失敗しました") }
        }
    }

    private fun ensureProductInList(
        products: List<MasterProductItemDto>,
        keepProductCd: String,
        allProducts: List<MasterProductItemDto>,
    ): List<MasterProductItemDto> {
        if (keepProductCd.isBlank()) return products
        if (products.any { it.productCd == keepProductCd }) return products
        val extra = allProducts.find { it.productCd == keepProductCd } ?: return products
        return products + extra
    }

    fun setFormProductCd(value: String) {
        val product = _uiState.value.filteredProductOptions.find { it.productCd == value }
        _uiState.update { state ->
            val unitPerBox = product?.unitPerBox ?: 0
            val boxes = state.form.confirmedBoxes.toIntOrNull() ?: 0
            state.copy(
                form = state.form.copy(
                    productCd = value,
                    productName = product?.productName.orEmpty(),
                    productAlias = product?.productAlias.orEmpty(),
                    productType = product?.productType ?: "量産品",
                    unitPerBox = unitPerBox.toString(),
                    confirmedUnits = calcUnits(unitPerBox, boxes),
                ),
            )
        }
    }

    fun setFormProductType(value: String) {
        _uiState.update { state -> state.copy(form = state.form.copy(productType = value)) }
    }

    fun setFormUnitPerBox(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { state ->
            val boxes = state.form.confirmedBoxes.toIntOrNull() ?: 0
            val unit = filtered.toIntOrNull() ?: 0
            state.copy(
                form = state.form.copy(
                    unitPerBox = filtered,
                    confirmedUnits = calcUnits(unit, boxes),
                ),
            )
        }
    }

    fun setFormConfirmedBoxes(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { state ->
            val unit = state.form.unitPerBox.toIntOrNull() ?: 0
            val boxes = filtered.toIntOrNull() ?: 0
            state.copy(
                form = state.form.copy(
                    confirmedBoxes = filtered,
                    confirmedUnits = calcUnits(unit, boxes),
                ),
            )
        }
    }

    fun submitForm() {
        val state = _uiState.value
        val form = state.form
        if (form.date.isBlank()) return showMessage("出荷日を入力してください")
        if (form.deliveryDate.isBlank()) return showMessage("納入日を入力してください")
        if (form.destinationCd.isBlank()) return showMessage("納入先を選択してください")
        if (form.productCd.isBlank()) return showMessage("製品を選択してください")
        val unitPerBox = form.unitPerBox.toIntOrNull() ?: 0
        val confirmedBoxes = form.confirmedBoxes.toIntOrNull() ?: 0
        if (unitPerBox <= 0 || confirmedBoxes <= 0) return showMessage("確定箱数と入数を入力してください")

        viewModelScope.launch {
            _uiState.update { it.copy(formSaving = true) }
            val confirmedUnits = calcUnits(unitPerBox, confirmedBoxes)
            runCatching {
                if (form.editId != null) {
                    repository.updateDaily(
                        form.editId,
                        buildSaveBody(form, confirmedUnits, unitPerBox),
                    )
                } else {
                    createWithMonthly(form, confirmedUnits, unitPerBox)
                }
            }.onSuccess {
                showMessage(if (form.editId != null) "更新しました" else "追加成功しました")
                dismissDialog()
                loadListInternal(showLoading = false)
            }.onFailure { e ->
                showMessage(e.message ?: "保存に失敗しました")
            }
            _uiState.update { it.copy(formSaving = false) }
        }
    }

    fun openDeleteDialog(row: OrderDailyItemDto) {
        _uiState.update { it.copy(activeDialog = OrderDailyPageDialog.DeleteConfirm, deleteTarget = row) }
    }

    fun confirmDelete() {
        val target = _uiState.value.deleteTarget ?: return
        viewModelScope.launch {
            runCatching { repository.deleteDaily(target.id) }
                .onSuccess {
                    showMessage("削除しました")
                    dismissDialog()
                    loadListInternal(showLoading = false)
                }
                .onFailure { e -> showMessage(e.message ?: "削除に失敗しました") }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private suspend fun createWithMonthly(form: OrderDailyFormUi, confirmedUnits: Int, unitPerBox: Int) {
        val parts = form.date.split("-")
        val year = parts.getOrNull(0).orEmpty()
        val month = parts.getOrNull(1).orEmpty().padStart(2, '0')
        val typeSuffix = typeSuffix(form.productType)
        val monthlyOrderId = "$year$month${form.destinationCd}${form.productCd}$typeSuffix"
        val check = repository.checkMonthlyOrderExists(monthlyOrderId)
        if (!check.exists) {
            val response = repository.addMonthlyOrder(
                AddMonthlyOrderBodyDto(
                    orderId = monthlyOrderId,
                    destinationCd = form.destinationCd,
                    destinationName = form.destinationName,
                    year = year.toIntOrNull() ?: 0,
                    month = month.toIntOrNull() ?: 0,
                    productCd = form.productCd,
                    productName = form.productName,
                    productAlias = form.productAlias.ifBlank { null },
                    productType = form.productType,
                    forecastUnits = confirmedUnits,
                    forecastTotalUnits = confirmedUnits,
                ),
            )
            if (!response.ok) error("月次注文の作成に失敗しました")
        }
        repository.createDaily(
            buildSaveBody(
                form.copy(monthlyOrderId = monthlyOrderId, forecastUnits = confirmedUnits),
                confirmedUnits,
                unitPerBox,
            ).copy(status = "未出荷", forecastUnits = confirmedUnits),
        )
    }

    private fun buildSaveBody(form: OrderDailyFormUi, confirmedUnits: Int, unitPerBox: Int) =
        OrderDailyCreateBodyDto(
            monthlyOrderId = form.monthlyOrderId.ifBlank { null },
            destinationCd = form.destinationCd,
            destinationName = form.destinationName.ifBlank { null },
            date = form.date,
            weekday = form.weekday.ifBlank { weekdayOf(form.date) },
            productCd = form.productCd,
            productName = form.productName.ifBlank { null },
            productAlias = form.productAlias.ifBlank { null },
            productType = form.productType,
            forecastUnits = form.forecastUnits.takeIf { it > 0 } ?: confirmedUnits,
            confirmedBoxes = form.confirmedBoxes.toIntOrNull(),
            confirmedUnits = confirmedUnits,
            unitPerBox = unitPerBox,
            status = form.status.ifBlank { null },
            remarks = form.remarks.ifBlank { null },
            deliveryDate = form.deliveryDate.ifBlank { null },
        )

    private fun applyPagination() {
        _uiState.update { state ->
            val total = state.fullList.size
            val maxPage = maxOf(1, (total + state.pageSize - 1) / state.pageSize)
            val page = state.page.coerceIn(1, maxPage)
            val start = (page - 1) * state.pageSize
            val end = minOf(start + state.pageSize, total)
            val nf = NumberFormat.getIntegerInstance(Locale.JAPAN)
            val rangeText = if (total == 0) {
                "表示 0件"
            } else {
                "表示 ${start + 1}〜$end 件 / 全${nf.format(total)}件"
            }
            state.copy(
                page = page,
                total = total,
                pageItems = state.fullList.drop(start).take(state.pageSize),
                pageRangeText = rangeText,
            )
        }
    }

    private fun filterProducts(
        all: List<MasterProductItemDto>,
        destinationCd: String,
        keepProductCd: String = "",
    ): List<MasterProductItemDto> {
        if (destinationCd.isBlank()) return emptyList()
        val filtered = all.filter { it.destinationCd == destinationCd }
        return ensureProductInList(filtered, keepProductCd, all)
    }

    private fun calcUnits(unitPerBox: Int, boxes: Int): Int =
        if (unitPerBox > 0) unitPerBox * boxes else boxes

    private fun todayString(): String = LocalDate.now(japanZone).format(dateFormatter)

    private fun weekRange(): Pair<String, String> {
        val today = LocalDate.now(japanZone)
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val end = start.plusDays(6)
        return start.format(dateFormatter) to end.format(dateFormatter)
    }

    private fun monthRange(offset: Int): Pair<String, String> {
        val base = LocalDate.now(japanZone).plusMonths(offset.toLong())
        val start = base.withDayOfMonth(1)
        val end = base.withDayOfMonth(base.lengthOfMonth())
        return start.format(dateFormatter) to end.format(dateFormatter)
    }

    private fun weekdayOf(date: String): String {
        if (date.isBlank()) return ""
        return runCatching {
            WEEKDAYS[LocalDate.parse(date, dateFormatter).dayOfWeek.value % 7]
        }.getOrDefault("")
    }

    private fun typeSuffix(productType: String): String = TYPE_SUFFIX[productType] ?: "0"

    private fun showMessage(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    enum class QuickRange { Today, Week, Month, LastMonth }

    class Factory(
        private val repository: OrderDailyRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OrderDailyViewModel(repository) as T
    }

    companion object {
        private val WEEKDAYS = listOf("日", "月", "火", "水", "木", "金", "土")
        private val TYPE_SUFFIX = mapOf(
            "量産品" to "0",
            "試作品" to "1",
            "別注品" to "2",
            "補給品" to "3",
            "サンプル品" to "4",
            "代替品" to "5",
            "返却品" to "6",
            "その他" to "7",
        )

        val PRODUCT_TYPES = listOf(
            "量産品", "試作品", "別注品", "補給品", "サンプル品", "代替品", "返却品", "その他",
        )

        val PAGE_SIZES = listOf(20, 50, 100, 200)
    }
}
