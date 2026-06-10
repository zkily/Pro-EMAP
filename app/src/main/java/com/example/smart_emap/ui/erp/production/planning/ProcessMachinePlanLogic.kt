package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.ProcessMachinePlanRowDto
import com.example.smart_emap.data.model.ProcessMachineProcessOptionDto
import com.example.smart_emap.data.model.ProcessMachineProcessTotalDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object ProcessMachinePlanLogic {
    val allProcessOptions = listOf(
        "cutting" to "切断",
        "chamfering" to "面取",
        "sw" to "SW",
        "molding" to "成型",
        "welding" to "溶接",
    )

    enum class TableRowType { Machine, Subtotal, Grand }

    data class TableRow(
        val type: TableRowType,
        val processStart: Boolean = false,
        val processKey: String = "",
        val processLabel: String = "",
        val machine: String = "",
        val plan: Int = 0,
        val actual: Int = 0,
        val actualPlan: Int = 0,
        val defect: Int = 0,
        val scrap: Int = 0,
        val diff: Int = 0,
        val achievementRate: Double? = null,
        val defectRate: Double? = null,
        val dailyValues: Map<String, Int> = emptyMap(),
        val rowTotal: Int = 0,
        val sourceRow: ProcessMachinePlanRowDto? = null,
    )

    data class TrendDailyRow(
        val date: String,
        val plan: Int,
        val actual: Int,
        val diff: Int,
        val rate: Double?,
    )

    data class TrendProcessDayRow(
        val date: String,
        val rates: Map<String, Double?>,
    )

    data class TrendStats(
        val totalPlan: Int,
        val totalActual: Int,
        val avgRate: Double?,
        val bestDate: String?,
        val bestRate: Double?,
        val worstDate: String?,
        val worstRate: Double?,
    )

    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val headerDate = DateTimeFormatter.ofPattern("M/d", Locale.JAPAN)
    private val weekdays = listOf("日", "月", "火", "水", "木", "金", "土")

    fun monthStartEnd(yearMonth: String): Pair<String, String> {
        val parts = yearMonth.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: LocalDate.now().year
        val month = parts.getOrNull(1)?.toIntOrNull() ?: LocalDate.now().monthValue
        val first = LocalDate.of(year, month, 1)
        val last = first.withDayOfMonth(first.lengthOfMonth())
        return first.format(isoDate) to last.format(isoDate)
    }

    fun currentMonthRange(): Pair<String, String> {
        val now = LocalDate.now()
        return monthStartEnd("${now.year}-${now.monthValue.toString().padStart(2, '0')}")
    }

    fun previousMonthRange(): Pair<String, String> {
        val prev = LocalDate.now().minusMonths(1)
        return monthStartEnd("${prev.year}-${prev.monthValue.toString().padStart(2, '0')}")
    }

    fun filterSummary(
        rows: List<ProcessMachinePlanRowDto>,
        selectedProcesses: Set<String>,
        selectedMachines: Set<String>,
    ): List<ProcessMachinePlanRowDto> = rows.filter { row ->
        (selectedProcesses.isEmpty() || selectedProcesses.contains(row.processKey)) &&
            (selectedMachines.isEmpty() || selectedMachines.contains(row.machine))
    }

    fun machineOptionGroups(rows: List<ProcessMachinePlanRowDto>): List<Pair<String, List<String>>> =
        rows.groupBy { it.processLabel }
            .entries
            .sortedBy { it.key }
            .map { (label, group) -> label to group.map { it.machine }.distinct().sorted() }

    fun aggregate(rows: List<ProcessMachinePlanRowDto>): TableRow {
        var plan = 0
        var actual = 0
        var actualPlan = 0
        var defect = 0
        var scrap = 0
        rows.forEach { r ->
            plan += r.plan
            actual += r.actual
            actualPlan += r.actualPlan
            defect += r.defect
            scrap += r.scrap
        }
        val diff = actual - plan
        val achievementRate = if (plan > 0) round1(actual * 100.0 / plan) else null
        val defectBase = actual + defect + scrap
        val defectRate = if (defectBase > 0) round1((defect + scrap) * 100.0 / defectBase) else null
        return TableRow(
            type = TableRowType.Subtotal,
            plan = plan,
            actual = actual,
            actualPlan = actualPlan,
            defect = defect,
            scrap = scrap,
            diff = diff,
            achievementRate = achievementRate,
            defectRate = defectRate,
        )
    }

    fun buildSummaryTableData(
        processes: List<ProcessMachineProcessOptionDto>,
        filteredRows: List<ProcessMachinePlanRowDto>,
    ): List<TableRow> {
        if (filteredRows.isEmpty()) return emptyList()
        val result = mutableListOf<TableRow>()
        for (proc in processes) {
            val machineRows = filteredRows.filter { it.processKey == proc.key }
            if (machineRows.isEmpty()) continue
            var firstInProcess = true
            machineRows.forEach { row ->
                result += TableRow(
                    type = TableRowType.Machine,
                    processStart = firstInProcess,
                    processKey = row.processKey,
                    processLabel = row.processLabel,
                    machine = row.machine,
                    plan = row.plan,
                    actual = row.actual,
                    actualPlan = row.actualPlan,
                    defect = row.defect,
                    scrap = row.scrap,
                    diff = row.diff,
                    achievementRate = row.achievementRate,
                    defectRate = row.defectRate,
                    sourceRow = row,
                )
                firstInProcess = false
            }
            val sub = aggregate(machineRows)
            result += sub.copy(
                type = TableRowType.Subtotal,
                processKey = proc.key,
                processLabel = proc.label,
                machine = "小計",
            )
        }
        val grand = aggregate(filteredRows)
        result += grand.copy(type = TableRowType.Grand, processLabel = "合計", machine = "")
        return result
    }

    fun dailyValue(row: ProcessMachinePlanRowDto, date: String, metric: String): Int {
        val cell = row.daily[date] ?: return 0
        return when (metric) {
            "actual" -> cell.actual
            "diff" -> cell.diff
            else -> cell.plan
        }
    }

    fun buildDailyTableData(
        processes: List<ProcessMachineProcessOptionDto>,
        filteredRows: List<ProcessMachinePlanRowDto>,
        dates: List<String>,
        metric: String,
    ): List<TableRow> {
        if (filteredRows.isEmpty() || dates.isEmpty()) return emptyList()
        val result = mutableListOf<TableRow>()
        for (proc in processes) {
            val machineRows = filteredRows.filter { it.processKey == proc.key }
            if (machineRows.isEmpty()) continue
            val subDaily = mutableMapOf<String, Int>()
            var subTotal = 0
            var firstInProcess = true
            machineRows.forEach { row ->
                val daily = mutableMapOf<String, Int>()
                var rowTotal = 0
                dates.forEach { d ->
                    val v = dailyValue(row, d, metric)
                    daily[d] = v
                    rowTotal += v
                    subDaily[d] = (subDaily[d] ?: 0) + v
                }
                subTotal += rowTotal
                result += TableRow(
                    type = TableRowType.Machine,
                    processStart = firstInProcess,
                    processKey = row.processKey,
                    processLabel = row.processLabel,
                    machine = row.machine,
                    dailyValues = daily,
                    rowTotal = rowTotal,
                    sourceRow = row,
                )
                firstInProcess = false
            }
            result += TableRow(
                type = TableRowType.Subtotal,
                processKey = proc.key,
                processLabel = proc.label,
                machine = "小計",
                dailyValues = subDaily,
                rowTotal = subTotal,
            )
        }
        return result
    }

    fun formatDailyCell(value: Int, metric: String): String {
        if (value == 0) return "—"
        return if (metric == "diff") formatProductionSigned(value) else formatProductionNumber(value)
    }

    fun formatDateHeaderDate(date: String): String = runCatching {
        LocalDate.parse(date.take(10), isoDate).format(headerDate)
    }.getOrElse { date.take(10) }

    fun formatDateHeaderWeek(date: String): String = runCatching {
        val dow = LocalDate.parse(date.take(10), isoDate).dayOfWeek
        weekdays[dow.value % 7]
    }.getOrElse { "" }

    fun isWeekend(date: String): Boolean = runCatching {
        val dow = LocalDate.parse(date.take(10), isoDate).dayOfWeek
        dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
    }.getOrElse { false }

    fun buildTrendDailyRows(
        filteredRows: List<ProcessMachinePlanRowDto>,
        dates: List<String>,
    ): List<TrendDailyRow> = dates.map { d ->
        var plan = 0
        var actual = 0
        filteredRows.forEach { row ->
            val cell = row.daily[d]
            plan += cell?.plan ?: 0
            actual += cell?.actual ?: 0
        }
        val diff = actual - plan
        val rate = if (plan > 0) round1(actual * 100.0 / plan) else null
        TrendDailyRow(d, plan, actual, diff, rate)
    }

    fun buildTrendProcessDayRows(
        processes: List<ProcessMachineProcessOptionDto>,
        filteredRows: List<ProcessMachinePlanRowDto>,
        dates: List<String>,
    ): List<TrendProcessDayRow> = dates.map { d ->
        val rates = processes.associate { proc ->
            val rows = filteredRows.filter { it.processKey == proc.key }
            var plan = 0
            var actual = 0
            rows.forEach { row ->
                val cell = row.daily[d]
                plan += cell?.plan ?: 0
                actual += cell?.actual ?: 0
            }
            proc.key to if (plan > 0) round1(actual * 100.0 / plan) else null
        }
        TrendProcessDayRow(d, rates)
    }

    val trendProcessColors = listOf(
        0xFF3B82F6L,
        0xFF22C55EL,
        0xFF14B8A6L,
        0xFF8B5CF6L,
        0xFFF59E0BL,
        0xFFEF4444L,
        0xFF06B6D4L,
        0xFFEC4899L,
        0xFF64748BL,
    )

    fun trendRateAxisMax(rates: List<Double?>): Int {
        val nums = rates.filterNotNull().filter { it.isFinite() }
        val peak = if (nums.isEmpty()) 100.0 else maxOf(nums.maxOrNull() ?: 0.0, 100.0)
        return minOf(160, (kotlin.math.ceil(peak / 10) * 10 + 10).toInt())
    }

    fun computeTrendStats(rows: List<TrendDailyRow>): TrendStats? {
        if (rows.isEmpty()) return null
        val totalPlan = rows.sumOf { it.plan }
        val totalActual = rows.sumOf { it.actual }
        val rates = rows.mapNotNull { r -> r.rate?.let { r.date to it } }
        val avgRate = if (rates.isNotEmpty()) round1(rates.sumOf { it.second } / rates.size) else null
        val best = rates.maxByOrNull { it.second }
        val worst = rates.minByOrNull { it.second }
        return TrendStats(
            totalPlan = totalPlan,
            totalActual = totalActual,
            avgRate = avgRate,
            bestDate = best?.first,
            bestRate = best?.second,
            worstDate = worst?.first,
            worstRate = worst?.second,
        )
    }

    fun buildSummaryTableRows(
        rows: List<ProcessMachinePlanRowDto>,
        processTotals: Map<String, ProcessMachineProcessTotalDto>,
    ): List<List<String>> {
        val processes = rows.map { it.processKey to it.processLabel }.distinctBy { it.first }
        val data = buildSummaryTableData(
            processes.map { ProcessMachineProcessOptionDto(it.first, it.second) },
            rows,
        )
        return data.map { rowToCells(it) }
    }

    fun buildDailyRows(
        rows: List<ProcessMachinePlanRowDto>,
        dates: List<String>,
        metric: String,
    ): List<List<String>> {
        val processes = rows.map { ProcessMachineProcessOptionDto(it.processKey, it.processLabel) }.distinctBy { it.key }
        return buildDailyTableData(processes, rows, dates, metric).map { tableRow ->
            listOf(tableRow.processLabel, tableRow.machine) +
                dates.map { d -> formatDailyCell(tableRow.dailyValues[d] ?: 0, metric) } +
                listOf(formatDailyCell(tableRow.rowTotal, metric))
        }
    }

    fun buildDailyHeaders(dates: List<String>): List<String> = listOf("工程", "設備") + dates + listOf("合計")

    fun rowToCells(row: TableRow): List<String> = listOf(
        row.processLabel,
        row.machine,
        formatProductionNumber(row.plan),
        formatProductionNumber(row.actual),
        formatProductionSigned(row.diff),
        formatProductionPercent(row.achievementRate),
        formatProductionNumber(row.actualPlan),
        formatProductionNumber(row.defect),
        formatProductionNumber(row.scrap),
        formatProductionPercent(row.defectRate),
    )

    fun buildExcelCsv(
        viewMode: ProcessMachineViewMode,
        processes: List<ProcessMachineProcessOptionDto>,
        filteredRows: List<ProcessMachinePlanRowDto>,
        dates: List<String>,
        dailyMetric: String,
        startDate: String,
        endDate: String,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("工程別設備別計画,$startDate～$endDate")
        when (viewMode) {
            ProcessMachineViewMode.Summary -> {
                val headers = listOf("工程", "設備", "計画", "実績", "差異", "達成率", "実計", "不良", "廃棄", "不良率")
                sb.appendLine(headers.joinToString(","))
                buildSummaryTableData(processes, filteredRows).forEach { row ->
                    sb.appendLine(rowToCells(row).joinToString(",") { "\"$it\"" })
                }
            }
            ProcessMachineViewMode.Daily -> {
                val headers = buildDailyHeaders(dates)
                sb.appendLine(headers.joinToString(","))
                buildDailyTableData(processes, filteredRows, dates, dailyMetric).forEach { row ->
                    val cells = listOf(row.processLabel, row.machine) +
                        dates.map { d -> formatDailyCell(row.dailyValues[d] ?: 0, dailyMetric) } +
                        listOf(formatDailyCell(row.rowTotal, dailyMetric))
                    sb.appendLine(cells.joinToString(",") { "\"$it\"" })
                }
            }
            ProcessMachineViewMode.Trend -> {
                sb.appendLine("日付,曜,計画,実績,差異,達成率")
                buildTrendDailyRows(filteredRows, dates).forEach { r ->
                    sb.appendLine(
                        listOf(
                            r.date,
                            formatDateHeaderWeek(r.date),
                            r.plan,
                            r.actual,
                            r.diff,
                            r.rate?.let { "${it.toInt()}%" } ?: "—",
                        ).joinToString(","),
                    )
                }
            }
        }
        return sb.toString()
    }

    private fun round1(v: Double): Double = (v * 10).roundToInt() / 10.0
}
