package com.example.smart_emap.ui.erp.production.actual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smart_emap.data.model.ProductionActualLogDto
import com.example.smart_emap.data.repository.ProcessOption
import com.example.smart_emap.data.repository.ProductionActualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private val ALLOWED_PROCESS_ORDER = listOf(
    "切断", "面取", "成型", "メッキ", "外注メッキ",
    "溶接", "外注溶接", "検査", "溶接前検査", "倉庫",
)

data class ProcessSummaryRow(
    val processCd: String,
    val processName: String,
    val total: Double,
    val activeDays: Int,
    val avgPerDay: Double,
    val ratio: Double,
    val colorIndex: Int,
)

data class ProcessMatrixRow(
    val processCd: String,
    val processName: String,
    val byDate: Map<String, Double>,
    val total: Double,
    val colorIndex: Int,
)

data class ProcessActualUiState(
    val mode: String = "day", // day / month
    val selectedDay: String = "",
    val selectedMonth: String = "",
    val selectedProcessCd: String = "",
    val loading: Boolean = false,
    val hasSearched: Boolean = false,
    val processOptions: List<ProcessOption> = emptyList(),
    val dateColumns: List<String> = emptyList(),
    val summary: List<ProcessSummaryRow> = emptyList(),
    val matrixRows: List<ProcessMatrixRow> = emptyList(),
    val totalQuantity: Double = 0.0,
    val processCount: Int = 0,
    val activeDays: Int = 0,
    val topProcessName: String = "-",
    val topProcessTotal: Double = 0.0,
    val selectedDailyLabels: List<String> = emptyList(),
    val selectedDailyValues: List<Double> = emptyList(),
    val selectedProcessTotal: Double = 0.0,
    val snackbarMessage: String? = null,
) {
    val periodLabel: String
        get() {
            val (from, to) = if (mode == "day") selectedDay to selectedDay else monthRange(selectedMonth)
            return if (from == to) from else "$from 〜 $to"
        }
    val selectedProcessName: String
        get() = processOptions.find { it.processCd == selectedProcessCd }?.processName
            ?: if (selectedProcessCd.isBlank()) "全工程" else selectedProcessCd
}

private fun monthRange(month: String): Pair<String, String> {
    return runCatching {
        val parts = month.split("-")
        val y = parts[0].toInt()
        val m = parts[1].toInt()
        val first = LocalDate.of(y, m, 1)
        val last = first.withDayOfMonth(first.lengthOfMonth())
        first.toString() to last.toString()
    }.getOrElse { month to month }
}

class ProcessActualViewModel(
    private val repository: ProductionActualRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProcessActualUiState())
    val uiState: StateFlow<ProcessActualUiState> = _uiState.asStateFlow()

    private var records: List<ProductionActualLogDto> = emptyList()
    private var processMaster: List<ProcessOption> = emptyList()
    private var processFilterInitialized = false

    init {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(
                selectedDay = today.toString(),
                selectedMonth = "%04d-%02d".format(today.year, today.monthValue),
            )
        }
        loadProcessMaster()
    }

    private fun loadProcessMaster() {
        viewModelScope.launch {
            runCatching { repository.loadProcessMaster() }
                .onSuccess { processMaster = it; recompute() }
        }
    }

    fun setMode(mode: String) {
        if (mode == _uiState.value.mode) return
        _uiState.update { it.copy(mode = mode) }
        loadData()
    }

    fun setDay(v: String) {
        _uiState.update { it.copy(selectedDay = v) }
        loadData()
    }

    fun setMonth(v: String) {
        _uiState.update { it.copy(selectedMonth = v) }
        loadData()
    }

    fun setProcess(cd: String) {
        _uiState.update { it.copy(selectedProcessCd = cd) }
        recompute()
    }

    fun shift(delta: Long) {
        val s = _uiState.value
        if (s.mode == "day") {
            val d = runCatching { LocalDate.parse(s.selectedDay).plusDays(delta) }.getOrNull() ?: return
            _uiState.update { it.copy(selectedDay = d.toString()) }
        } else {
            val parts = s.selectedMonth.split("-")
            val base = runCatching { LocalDate.of(parts[0].toInt(), parts[1].toInt(), 1).plusMonths(delta) }.getOrNull() ?: return
            _uiState.update { it.copy(selectedMonth = "%04d-%02d".format(base.year, base.monthValue)) }
        }
        loadData()
    }

    fun setCurrent() {
        val now = LocalDate.now()
        val s = _uiState.value
        if (s.mode == "day") _uiState.update { it.copy(selectedDay = now.toString()) }
        else _uiState.update { it.copy(selectedMonth = "%04d-%02d".format(now.year, now.monthValue)) }
        loadData()
    }

    fun loadData() {
        val s = _uiState.value
        val (from, to) = if (s.mode == "day") s.selectedDay to s.selectedDay else monthRange(s.selectedMonth)
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, hasSearched = true) }
            runCatching {
                repository.loadProductionActualLogs(from, to, null)
            }.onSuccess { list ->
                records = list
                syncProcessSelection()
                _uiState.update { it.copy(loading = false) }
                recompute()
            }.onFailure { e ->
                records = emptyList()
                _uiState.update { it.copy(loading = false, snackbarMessage = e.message ?: "実績データの取得に失敗しました") }
                recompute()
            }
        }
    }

    private fun extractDate(time: String?): String {
        val s = time.orEmpty()
        return if (s.length >= 10) s.substring(0, 10) else ""
    }

    private fun buildProcessOptions(): List<ProcessOption> {
        val map = LinkedHashMap<String, ProcessOption>()
        processMaster.forEach { if (it.processCd.isNotBlank()) map[it.processCd] = it }
        records.forEach { r ->
            val cd = r.processCd?.trim().orEmpty()
            if (cd.isNotEmpty() && !map.containsKey(cd)) {
                map[cd] = ProcessOption(cd, r.processName ?: cd)
            }
        }
        return map.values.sortedWith(processComparator { it.processName })
    }

    private fun syncProcessSelection() {
        val opts = buildProcessOptions()
        val current = _uiState.value.selectedProcessCd
        if (current.isNotBlank()) {
            if (opts.none { it.processCd == current } && opts.isNotEmpty()) {
                _uiState.update { it.copy(selectedProcessCd = opts.first().processCd) }
            }
            return
        }
        if (!processFilterInitialized && opts.isNotEmpty()) {
            _uiState.update { it.copy(selectedProcessCd = opts.first().processCd) }
            processFilterInitialized = true
        }
    }

    private fun <T> processComparator(nameOf: (T) -> String): Comparator<T> = Comparator { a, b ->
        val ia = ALLOWED_PROCESS_ORDER.indexOf(nameOf(a))
        val ib = ALLOWED_PROCESS_ORDER.indexOf(nameOf(b))
        when {
            ia != -1 && ib != -1 -> ia - ib
            ia != -1 -> -1
            ib != -1 -> 1
            else -> nameOf(a).compareTo(nameOf(b))
        }
    }

    private fun recompute() {
        val s = _uiState.value
        val (from, to) = if (s.mode == "day") s.selectedDay to s.selectedDay else monthRange(s.selectedMonth)
        val dateColumns = buildDateColumns(from, to)
        val options = buildProcessOptions()

        // フィルタ（選択工程）
        val filtered = if (s.selectedProcessCd.isBlank()) records
        else records.filter { (it.processCd ?: "") == s.selectedProcessCd }

        // 工程ごと集計
        data class Agg(var total: Double, val daily: HashMap<String, Double>, val name: String)
        val aggMap = LinkedHashMap<String, Agg>()
        filtered.forEach { r ->
            val cd = r.processCd ?: "__none__"
            val name = r.processName ?: r.processCd ?: "未分類"
            val qty = r.quantity ?: 0.0
            val date = extractDate(r.transactionTime)
            val agg = aggMap.getOrPut(cd) { Agg(0.0, HashMap(), name) }
            agg.total += qty
            if (date.isNotEmpty()) agg.daily[date] = (agg.daily[date] ?: 0.0) + qty
        }
        val sortedAgg = aggMap.entries.sortedWith(
            Comparator { a, b ->
                val ia = ALLOWED_PROCESS_ORDER.indexOf(a.value.name)
                val ib = ALLOWED_PROCESS_ORDER.indexOf(b.value.name)
                when {
                    ia != -1 && ib != -1 -> ia - ib
                    ia != -1 -> -1
                    ib != -1 -> 1
                    else -> b.value.total.compareTo(a.value.total)
                }
            },
        )

        val totalQuantity = sortedAgg.sumOf { it.value.total }
        val safeTotal = if (totalQuantity <= 0) 1.0 else totalQuantity

        val summary = sortedAgg.mapIndexed { i, e ->
            val activeDays = e.value.daily.size
            ProcessSummaryRow(
                processCd = e.key,
                processName = e.value.name,
                total = e.value.total,
                activeDays = activeDays,
                avgPerDay = if (activeDays > 0) e.value.total / activeDays else 0.0,
                ratio = e.value.total / safeTotal * 100.0,
                colorIndex = i,
            )
        }
        val matrixRows = sortedAgg.mapIndexed { i, e ->
            ProcessMatrixRow(
                processCd = e.key,
                processName = e.value.name,
                byDate = e.value.daily.toMap(),
                total = e.value.total,
                colorIndex = i,
            )
        }

        val activeDaysTotal = filtered.mapNotNull { extractDate(it.transactionTime).takeIf { d -> d.isNotEmpty() } }.toSet().size
        val top = summary.maxByOrNull { it.total }

        // 選択工程の日次
        val selectedAgg = sortedAgg.firstOrNull { it.key == s.selectedProcessCd }
        val selLabels = dateColumns.map { shortDate(it) }
        val selValues = if (s.selectedProcessCd.isBlank() || selectedAgg == null) emptyList()
        else dateColumns.map { selectedAgg.value.daily[it] ?: 0.0 }

        _uiState.update {
            it.copy(
                processOptions = options,
                dateColumns = dateColumns,
                summary = summary,
                matrixRows = matrixRows,
                totalQuantity = totalQuantity,
                processCount = sortedAgg.size,
                activeDays = activeDaysTotal,
                topProcessName = top?.processName ?: "-",
                topProcessTotal = top?.total ?: 0.0,
                selectedDailyLabels = selLabels,
                selectedDailyValues = selValues,
                selectedProcessTotal = selValues.sum(),
            )
        }
    }

    private fun buildDateColumns(from: String, to: String): List<String> {
        val start = runCatching { LocalDate.parse(from) }.getOrNull() ?: return emptyList()
        val end = runCatching { LocalDate.parse(to) }.getOrNull() ?: return emptyList()
        val cols = mutableListOf<String>()
        var cur = start
        var guard = 0
        while (!cur.isAfter(end) && guard < 366) {
            cols.add(cur.toString())
            cur = cur.plusDays(1)
            guard++
        }
        return cols
    }

    fun clearSnackbar() = _uiState.update { it.copy(snackbarMessage = null) }

    class Factory(private val repository: ProductionActualRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProcessActualViewModel(repository) as T
    }
}
