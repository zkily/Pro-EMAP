package com.example.smart_emap.ui.erp.production.actual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.MainLineLabelDto
import com.example.smart_emap.data.model.ProductionSummaryProductOptionDto
import com.example.smart_emap.data.model.QualityProcessRowDto
import com.example.smart_emap.data.model.QualityProductRowDto
import com.example.smart_emap.data.model.QualitySummaryDto
import com.example.smart_emap.data.repository.ProductionActualRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 主ライン工程キー順（後端 RTY と一致） */
internal val MAIN_LINE_KEYS_ORDER = listOf(
    "cutting", "chamfering", "molding", "plating", "welding", "inspection",
)

internal val DEFAULT_MAIN_LINE_LABELS = listOf(
    MainLineLabelDto("cutting", "切断"),
    MainLineLabelDto("chamfering", "面取"),
    MainLineLabelDto("molding", "成型"),
    MainLineLabelDto("plating", "メッキ"),
    MainLineLabelDto("welding", "溶接"),
    MainLineLabelDto("inspection", "検査"),
)

/** 工程フィルタ選択肢（Web と同一） */
internal val SCRAP_PROCESS_OPTIONS = listOf(
    "cutting" to "切断",
    "chamfering" to "面取",
    "molding" to "成型",
    "plating" to "メッキ",
    "welding" to "溶接",
    "inspection" to "検査",
    "warehouse" to "倉庫",
    "outsourced_warehouse" to "外注倉庫",
    "outsourced_plating" to "外注メッキ",
    "outsourced_welding" to "外注溶接",
    "pre_welding_inspection" to "溶接前検査",
    "pre_inspection" to "外注支給前",
    "pre_outsourcing" to "外注検査前",
)

private const val PRODUCT_LIMIT = 50
private const val CHART_PRODUCT_DISPLAY_CAP = 80
private const val PRINT_FETCH_LIMIT = 500
private const val PRINT_MAX_PAGES = 40

data class ScrapRateUiState(
    val dateFrom: String = "",
    val dateTo: String = "",
    val process: String = "",
    val filterProductCd: String = "",
    val productOptions: List<ProductionSummaryProductOptionDto> = emptyList(),
    // 工程別
    val loadingProcessAgg: Boolean = false,
    val processAmountsSyncing: Boolean = false,
    val processRows: List<QualityProcessRowDto> = emptyList(),
    val summary: QualitySummaryDto? = null,
    val allProcessesDefectScrapTotal: Double = 0.0,
    // 製品別
    val mainLineLabels: List<MainLineLabelDto> = DEFAULT_MAIN_LINE_LABELS,
    val loadingProductMatrix: Boolean = false,
    val productRows: List<QualityProductRowDto> = emptyList(),
    val productTotal: Int = 0,
    val productPage: Int = 1,
    val productSortProp: String = "product_name",
    val productSortOrder: String = "asc",
    // 製品チャート
    val productChartLoading: Boolean = false,
    val productChartRows: List<QualityProductRowDto> = emptyList(),
    val productChartTotal: Int = 0,
    // 印刷
    val printLoading: Boolean = false,
    val pendingPrintHtml: String? = null,
    val snackbarMessage: String? = null,
) {
    val productPageCount: Int
        get() = if (productTotal <= 0) 1 else (productTotal + PRODUCT_LIMIT - 1) / PRODUCT_LIMIT
}

/** 製品行の主ライン連乘 RTY（合格率 %）と廃棄率（1−RTY %）。実績>0 工程のみ */
internal fun computeProductMainLineRty(row: QualityProductRowDto): Pair<Double, Double>? {
    var prod = 1.0
    var used = false
    for (key in MAIN_LINE_KEYS_ORDER) {
        val p = row.processes?.firstOrNull { it.key == key } ?: continue
        val sa = p.sumActual ?: 0.0
        if (sa <= 0.0) continue
        val bad = p.sumDefectAndScrap ?: 0.0
        var r = bad / sa
        if (r > 1.0) r = 1.0
        if (r < 0.0) r = 0.0
        prod *= (1.0 - r)
        used = true
    }
    if (!used) return null
    return (prod * 100.0) to ((1.0 - prod) * 100.0)
}

class ScrapRateViewModel(
    private val repository: ProductionActualRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScrapRateUiState())
    val uiState: StateFlow<ScrapRateUiState> = _uiState.asStateFlow()

    init {
        val today = LocalDate.now()
        val first = today.withDayOfMonth(1)
        _uiState.update { it.copy(dateFrom = first.toString(), dateTo = today.toString()) }
        loadProductOptions()
    }

    private fun loadProductOptions() {
        viewModelScope.launch {
            runCatching { repository.loadProductionSummaryProducts() }
                .onSuccess { list -> _uiState.update { it.copy(productOptions = list) } }
        }
    }

    fun setRange(from: String, to: String) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to, productPage = 1) }
        fetchAll()
    }

    fun setProcess(cd: String) {
        _uiState.update { it.copy(process = cd) }
        fetchProcessSummary()
    }

    fun setProductFilter(cd: String) {
        _uiState.update { it.copy(filterProductCd = cd, productPage = 1) }
        fetchProductMatrix()
        refreshProductChart()
    }

    fun reset() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                dateFrom = today.withDayOfMonth(1).toString(),
                dateTo = today.toString(),
                process = "",
                filterProductCd = "",
                productPage = 1,
                productSortProp = "product_name",
                productSortOrder = "asc",
            )
        }
        fetchAll()
    }

    fun fetchAll() {
        val s = _uiState.value
        if (s.dateFrom.isBlank() || s.dateTo.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        fetchProcessSummary()
        fetchProductMatrix()
        refreshProductChart()
    }

    private fun fetchProcessSummary() {
        val s = _uiState.value
        if (s.dateFrom.isBlank() || s.dateTo.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProcessAgg = true, processAmountsSyncing = false) }
            val fast = runCatching {
                repository.loadQualityRateByProcess(s.dateFrom, s.dateTo, s.process, includeAmounts = false)
            }
            fast.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        loadingProcessAgg = false,
                        processRows = data.processes.orEmpty(),
                        summary = data.summary,
                        allProcessesDefectScrapTotal = data.allProcessesDefectScrapTotal ?: 0.0,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loadingProcessAgg = false,
                        processRows = emptyList(),
                        summary = null,
                        allProcessesDefectScrapTotal = 0.0,
                        snackbarMessage = e.message ?: "工程別データの取得に失敗しました",
                    )
                }
                return@launch
            }

            // 第2段：金額列
            _uiState.update { it.copy(processAmountsSyncing = true) }
            runCatching {
                repository.loadQualityRateByProcess(s.dateFrom, s.dateTo, s.process, includeAmounts = true)
            }.onSuccess { data ->
                val byKey = data.processes.orEmpty().associateBy { it.key }
                _uiState.update { st ->
                    st.copy(
                        processAmountsSyncing = false,
                        processRows = st.processRows.map { r ->
                            val src = byKey[r.key] ?: return@map r
                            r.copy(
                                sumDefectAmount = src.sumDefectAmount ?: 0.0,
                                sumScrapAmount = src.sumScrapAmount ?: 0.0,
                                sumDefectAndScrapAmount = src.sumDefectAndScrapAmount ?: 0.0,
                            )
                        },
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(processAmountsSyncing = false, snackbarMessage = "工程別金額の取得に失敗しました（数量は表示済み）")
                }
            }
        }
    }

    private fun fetchProductMatrix() {
        val s = _uiState.value
        if (s.dateFrom.isBlank() || s.dateTo.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loadingProductMatrix = true) }
            runCatching {
                repository.loadQualityRateByProduct(
                    startDate = s.dateFrom,
                    endDate = s.dateTo,
                    page = s.productPage,
                    limit = PRODUCT_LIMIT,
                    productCd = s.filterProductCd,
                    sortBy = s.productSortProp,
                    sortOrder = s.productSortOrder,
                )
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        loadingProductMatrix = false,
                        productRows = data.products.orEmpty(),
                        productTotal = data.pagination?.total ?: 0,
                        mainLineLabels = data.mainLineLabels?.takeIf { l -> l.isNotEmpty() } ?: it.mainLineLabels,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loadingProductMatrix = false,
                        productRows = emptyList(),
                        productTotal = 0,
                        snackbarMessage = e.message ?: "製品別データの取得に失敗しました",
                    )
                }
            }
        }
    }

    private fun refreshProductChart() {
        val s = _uiState.value
        if (s.dateFrom.isBlank() || s.dateTo.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(productChartLoading = true) }
            runCatching { fetchAllProductRows(s.dateFrom, s.dateTo, s.filterProductCd, "rty_loss", "desc") }
                .onSuccess { (rows, total) ->
                    _uiState.update {
                        it.copy(
                            productChartLoading = false,
                            productChartRows = rows.take(CHART_PRODUCT_DISPLAY_CAP),
                            productChartTotal = total,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(productChartLoading = false, productChartRows = emptyList(), productChartTotal = 0)
                    }
                }
        }
    }

    private suspend fun fetchAllProductRows(
        from: String,
        to: String,
        productCd: String,
        sortBy: String,
        sortOrder: String,
    ): Pair<List<QualityProductRowDto>, Int> = coroutineScope {
        val out = mutableListOf<QualityProductRowDto>()
        var total = 0
        var page = 1
        while (page <= PRINT_MAX_PAGES) {
            val data = repository.loadQualityRateByProduct(
                startDate = from,
                endDate = to,
                page = page,
                limit = PRINT_FETCH_LIMIT,
                productCd = productCd,
                sortBy = sortBy,
                sortOrder = sortOrder,
            )
            total = data.pagination?.total ?: 0
            val batch = data.products.orEmpty()
            out.addAll(batch)
            if (batch.size < PRINT_FETCH_LIMIT || out.size >= total) break
            page++
        }
        out to total
    }

    fun goToProductPage(page: Int) {
        val s = _uiState.value
        val target = page.coerceIn(1, s.productPageCount)
        if (target == s.productPage) return
        _uiState.update { it.copy(productPage = target) }
        fetchProductMatrix()
    }

    fun setProductSort(prop: String) {
        val s = _uiState.value
        val (newProp, newOrder) = if (s.productSortProp == prop) {
            prop to if (s.productSortOrder == "asc") "desc" else "asc"
        } else {
            prop to "asc"
        }
        _uiState.update { it.copy(productSortProp = newProp, productSortOrder = newOrder, productPage = 1) }
        fetchProductMatrix()
    }

    fun printProductTable() {
        val s = _uiState.value
        if (s.dateFrom.isBlank() || s.dateTo.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "期間を選択してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(printLoading = true) }
            runCatching {
                fetchAllProductRows(s.dateFrom, s.dateTo, s.filterProductCd, s.productSortProp, s.productSortOrder)
            }.onSuccess { (rows, total) ->
                if (rows.isEmpty()) {
                    _uiState.update { it.copy(printLoading = false, snackbarMessage = "印刷するデータがありません") }
                    return@onSuccess
                }
                val html = buildScrapProductPrintHtml(
                    rows = rows,
                    total = total,
                    dateFrom = s.dateFrom,
                    dateTo = s.dateTo,
                    mainLineLabels = s.mainLineLabels,
                    filterProductCd = s.filterProductCd,
                    sortLabel = describeSort(s.productSortProp, s.productSortOrder, s.mainLineLabels),
                )
                _uiState.update { it.copy(printLoading = false, pendingPrintHtml = html) }
            }.onFailure { e ->
                _uiState.update { it.copy(printLoading = false, snackbarMessage = e.message ?: "印刷用データの取得に失敗しました") }
            }
        }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ProductionActualRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ScrapRateViewModel(repository) as T
    }
}

private fun describeSort(prop: String, order: String, labels: List<MainLineLabelDto>): String {
    val ord = if (order == "desc") "降順" else "昇順"
    labels.firstOrNull { it.key == prop }?.let { return "${it.label}（％）·$ord" }
    val fixed = mapOf(
        "product_cd" to "製品CD",
        "product_name" to "製品名",
        "all_processes_defect_scrap" to "不良＋廃棄",
        "rty" to "合格率",
        "rty_loss" to "廃棄率",
    )
    return "${fixed[prop] ?: prop}·$ord"
}
