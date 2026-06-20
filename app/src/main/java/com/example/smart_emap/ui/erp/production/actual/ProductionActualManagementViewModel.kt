package com.example.smart_emap.ui.erp.production.actual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ProductionActualLogDto
import com.example.smart_emap.data.model.ProductionActualStatsDto
import com.example.smart_emap.data.model.ProductionActualTypeSummaryDto
import com.example.smart_emap.data.model.ProductionActualUpdateBody
import com.example.smart_emap.data.repository.ProcessOption
import com.example.smart_emap.data.repository.ProductionActualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private val PAM_PROCESS_ORDER = listOf(
    "切断", "面取", "成型", "メッキ", "外注メッキ",
    "溶接", "外注溶接", "検査", "溶接前検査", "倉庫",
)

private const val PAM_CHART_LIMIT = 500

data class PamNamedTotal(val name: String, val total: Double)

data class PamEditState(
    val id: Int = 0,
    val transactionTime: String = "",
    val transactionType: String = "",
    val stockType: String = "",
    val targetCd: String = "",
    val targetName: String = "",
    val locationCd: String = "",
    val machineCd: String = "",
    val machineName: String = "",
    val orderNo: String = "",
    val quantity: String = "0",
)

data class ProductionActualManagementUiState(
    val dateFrom: String = "",
    val dateTo: String = "",
    val activeProcessCd: String = "ALL",
    val targetName: String = "",
    val machineName: String = "",
    val transactionType: String = "実績",
    val sortBy: String = "",
    val sortOrder: String = "",
    val page: Int = 1,
    val limit: Int = 50,
    val total: Int = 0,
    val loading: Boolean = false,
    val records: List<ProductionActualLogDto> = emptyList(),
    val stats: ProductionActualStatsDto = ProductionActualStatsDto(),
    val typeSummary: List<ProductionActualTypeSummaryDto> = emptyList(),
    val processOptions: List<ProcessOption> = emptyList(),
    val productOptions: List<String> = emptyList(),
    val machineOptions: List<String> = emptyList(),
    // チャート
    val dailyLabels: List<String> = emptyList(),
    val dailyValues: List<Double> = emptyList(),
    val processAgg: List<PamNamedTotal> = emptyList(),
    val typeDist: List<PamNamedTotal> = emptyList(),
    val productTop: List<PamNamedTotal> = emptyList(),
    // 編集ダイアログ
    val editVisible: Boolean = false,
    val edit: PamEditState = PamEditState(),
    val saving: Boolean = false,
    val pendingPrintHtml: String? = null,
    val snackbarMessage: String? = null,
) {
    val pageCount: Int get() = if (total <= 0) 1 else (total + limit - 1) / limit
}

class ProductionActualManagementViewModel(
    private val repository: ProductionActualRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductionActualManagementUiState())
    val uiState: StateFlow<ProductionActualManagementUiState> = _uiState.asStateFlow()

    private var chartData: List<ProductionActualLogDto> = emptyList()

    init {
        val today = LocalDate.now()
        _uiState.update { it.copy(dateFrom = today.minusDays(29).toString(), dateTo = today.toString()) }
        loadProcesses()
    }

    private fun loadProcesses() {
        viewModelScope.launch {
            runCatching { repository.loadProcessMaster() }
                .onSuccess { list -> _uiState.update { it.copy(processOptions = orderedProcesses(list)) } }
        }
    }

    private fun orderedProcesses(list: List<ProcessOption>): List<ProcessOption> {
        val byName = LinkedHashMap<String, ProcessOption>()
        list.forEach { if (it.processCd.isNotBlank()) byName[it.processName] = it }
        return PAM_PROCESS_ORDER.mapNotNull { byName[it] }
    }

    fun setProcessTab(cd: String) {
        _uiState.update { it.copy(activeProcessCd = cd, page = 1) }
        loadData()
    }

    fun setRange(from: String, to: String) {
        _uiState.update { it.copy(dateFrom = from, dateTo = to, page = 1) }
        loadData()
    }

    fun shiftDay(delta: Long) {
        val s = _uiState.value
        val base = runCatching { LocalDate.parse(s.dateTo) }.getOrNull() ?: LocalDate.now()
        val d = base.plusDays(delta)
        _uiState.update { it.copy(dateFrom = d.toString(), dateTo = d.toString(), page = 1) }
        loadData()
    }

    fun setToday() {
        val d = LocalDate.now().toString()
        _uiState.update { it.copy(dateFrom = d, dateTo = d, page = 1) }
        loadData()
    }

    fun setTargetName(name: String) {
        _uiState.update { it.copy(targetName = name, page = 1) }
        loadData()
    }

    fun setMachineName(name: String) {
        _uiState.update { it.copy(machineName = name, page = 1) }
        loadData()
    }

    fun setSort(prop: String) {
        val s = _uiState.value
        val (newProp, newOrder) = when {
            s.sortBy != prop -> prop to "ascending"
            s.sortOrder == "ascending" -> prop to "descending"
            else -> "" to ""
        }
        _uiState.update { it.copy(sortBy = newProp, sortOrder = newOrder, page = 1) }
        loadData()
    }

    fun goToPage(page: Int) {
        val s = _uiState.value
        val target = page.coerceIn(1, s.pageCount)
        if (target == s.page) return
        _uiState.update { it.copy(page = target) }
        loadData()
    }

    fun loadData() {
        val s = _uiState.value
        val processCd = s.activeProcessCd.takeIf { it != "ALL" }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            runCatching {
                repository.loadStockActualLogs(
                    page = s.page,
                    limit = s.limit,
                    processCd = processCd,
                    transactionType = s.transactionType,
                    targetName = s.targetName,
                    machineName = s.machineName,
                    dateFrom = s.dateFrom,
                    dateTo = s.dateTo,
                    sortBy = s.sortBy.takeIf { it.isNotBlank() },
                    sortOrder = if (s.sortBy.isNotBlank()) (if (s.sortOrder == "ascending") "ASC" else "DESC") else null,
                )
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        records = data.list.orEmpty(),
                        stats = data.stats ?: ProductionActualStatsDto(),
                        typeSummary = data.typeSummary.orEmpty(),
                        total = data.pagination?.total ?: 0,
                    )
                }
                loadChartData(processCd, s)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        records = emptyList(),
                        stats = ProductionActualStatsDto(),
                        typeSummary = emptyList(),
                        total = 0,
                        snackbarMessage = e.message ?: "データの取得に失敗しました",
                    )
                }
            }
        }
    }

    private suspend fun loadChartData(processCd: String?, s: ProductionActualManagementUiState) {
        runCatching {
            repository.loadStockActualLogs(
                page = 1,
                limit = PAM_CHART_LIMIT,
                processCd = processCd,
                transactionType = s.transactionType,
                targetName = s.targetName,
                machineName = s.machineName,
                dateFrom = s.dateFrom,
                dateTo = s.dateTo,
                sortBy = null,
                sortOrder = null,
            )
        }.onSuccess { data ->
            chartData = data.list.orEmpty()
            recomputeCharts()
            recomputeOptions()
        }
    }

    private fun recomputeOptions() {
        val combined = (chartData + _uiState.value.records)
        val products = combined.mapNotNull { it.targetName?.trim()?.takeIf { n -> n.isNotEmpty() } }
            .distinct().sorted()
        val machines = combined.mapNotNull { it.machineName?.trim()?.takeIf { n -> n.isNotEmpty() } }
            .distinct().sorted()
        _uiState.update { it.copy(productOptions = products, machineOptions = machines) }
    }

    private fun dateKey(time: String?): String {
        val s = time.orEmpty()
        val m = Regex("^(\\d{4}-\\d{2}-\\d{2})").find(s)
        return m?.groupValues?.get(1) ?: ""
    }

    private fun recomputeCharts() {
        val s = _uiState.value
        // 日別推移
        val daily = HashMap<String, Double>()
        chartData.forEach { r ->
            val k = dateKey(r.transactionTime)
            if (k.isNotEmpty()) daily[k] = (daily[k] ?: 0.0) + (r.quantity ?: 0.0)
        }
        val dates: List<String> = if (s.dateFrom.isNotBlank() && s.dateTo.isNotBlank()) {
            buildDateRange(s.dateFrom, s.dateTo)
        } else {
            daily.keys.sorted()
        }
        val dailyValues = dates.map { daily[it] ?: 0.0 }

        // 工程別
        val proc = LinkedHashMap<String, Double>()
        chartData.forEach { r ->
            val name = r.processName?.takeIf { it.isNotBlank() } ?: r.processCd ?: "未分類"
            proc[name] = (proc[name] ?: 0.0) + (r.quantity ?: 0.0)
        }
        val processAgg = proc.entries
            .sortedWith(compareBy({ idxOrLast(it.key) }, { -it.value }))
            .map { PamNamedTotal(it.key, it.value) }

        // 取引タイプ分布（typeSummary の数量）
        val typeDist = s.typeSummary
            .filter { (it.totalQuantity ?: 0.0) != 0.0 }
            .map { PamNamedTotal(it.transactionType ?: "-", it.totalQuantity ?: 0.0) }

        // 製品 TOP10
        val prod = HashMap<String, Double>()
        chartData.forEach { r ->
            val name = r.targetName?.takeIf { it.isNotBlank() } ?: r.targetCd ?: "-"
            prod[name] = (prod[name] ?: 0.0) + (r.quantity ?: 0.0)
        }
        val productTop = prod.entries.sortedByDescending { it.value }.take(10)
            .map { PamNamedTotal(it.key, it.value) }

        _uiState.update {
            it.copy(
                dailyLabels = dates.map { d -> if (d.length >= 10) d.substring(5) else d },
                dailyValues = dailyValues,
                processAgg = processAgg,
                typeDist = typeDist,
                productTop = productTop,
            )
        }
    }

    private fun idxOrLast(name: String): Int {
        val i = PAM_PROCESS_ORDER.indexOf(name)
        return if (i == -1) PAM_PROCESS_ORDER.size else i
    }

    private fun buildDateRange(from: String, to: String): List<String> {
        val start = runCatching { LocalDate.parse(from) }.getOrNull() ?: return emptyList()
        val end = runCatching { LocalDate.parse(to) }.getOrNull() ?: return emptyList()
        val out = mutableListOf<String>()
        var cur = start
        var guard = 0
        while (!cur.isAfter(end) && guard < 366) {
            out.add(cur.toString())
            cur = cur.plusDays(1)
            guard++
        }
        return out
    }

    fun openEdit(record: ProductionActualLogDto) {
        _uiState.update {
            it.copy(
                editVisible = true,
                edit = PamEditState(
                    id = record.id ?: 0,
                    transactionTime = record.transactionTime ?: "",
                    transactionType = record.transactionType ?: "",
                    stockType = record.stockType ?: "",
                    targetCd = record.targetCd ?: "",
                    targetName = record.targetName ?: "",
                    locationCd = record.locationCd ?: "",
                    machineCd = record.machineCd ?: "",
                    machineName = record.machineName ?: "",
                    orderNo = record.relatedDocNo ?: "",
                    quantity = formatQtyInput(record.quantity ?: 0.0),
                ),
            )
        }
    }

    fun closeEdit() = _uiState.update { it.copy(editVisible = false) }

    fun setEditQuantity(v: String) {
        _uiState.update { it.copy(edit = it.edit.copy(quantity = v.filter { c -> c.isDigit() })) }
    }

    fun saveEdit() {
        val e = _uiState.value.edit
        val qty = e.quantity.toDoubleOrNull()
        if (qty == null || qty < 0) {
            _uiState.update { it.copy(snackbarMessage = "数量は0以上である必要があります") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true) }
            runCatching {
                repository.updateStockActualLog(
                    e.id,
                    ProductionActualUpdateBody(
                        transactionTime = e.transactionTime.takeIf { it.isNotBlank() },
                        transactionType = e.transactionType.takeIf { it.isNotBlank() },
                        stockType = e.stockType.takeIf { it.isNotBlank() },
                        targetCd = e.targetCd.takeIf { it.isNotBlank() },
                        quantity = qty,
                        locationCd = e.locationCd.takeIf { it.isNotBlank() },
                        machineCd = e.machineCd.takeIf { it.isNotBlank() },
                        orderNo = e.orderNo.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(saving = false, editVisible = false, snackbarMessage = "更新に成功しました") }
                loadData()
            }.onFailure { ex ->
                _uiState.update { it.copy(saving = false, snackbarMessage = ex.message ?: "更新に失敗しました") }
            }
        }
    }

    fun deleteRecord(id: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteStockActualLog(id) }
                .onSuccess {
                    _uiState.update { it.copy(snackbarMessage = "削除に成功しました") }
                    loadData()
                }
                .onFailure { ex ->
                    _uiState.update { it.copy(snackbarMessage = ex.message ?: "削除に失敗しました") }
                }
        }
    }

    fun printTable() {
        val s = _uiState.value
        if (s.records.isEmpty()) {
            _uiState.update { it.copy(snackbarMessage = "印刷するデータがありません") }
            return
        }
        val html = buildProductionActualPrintHtml(
            records = s.records,
            dateFrom = s.dateFrom,
            dateTo = s.dateTo,
            processLabel = if (s.activeProcessCd == "ALL") "全工程" else
                (s.processOptions.firstOrNull { it.processCd == s.activeProcessCd }?.processName ?: s.activeProcessCd),
            total = s.total,
        )
        _uiState.update { it.copy(pendingPrintHtml = html) }
    }

    fun clearPendingPrintHtml() = _uiState.update { it.copy(pendingPrintHtml = null) }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ProductionActualRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProductionActualManagementViewModel(repository) as T
    }
}

private fun formatQtyInput(q: Double): String =
    if (q % 1.0 == 0.0) q.toLong().toString() else q.toString()
