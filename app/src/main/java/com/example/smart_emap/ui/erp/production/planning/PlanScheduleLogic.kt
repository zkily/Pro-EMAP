package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.PlanUpdateRecordDto
import com.example.smart_emap.data.model.ScheduleGridRowDto
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class PlanScheduleRow(
    val id: Int,
    val orderNo: Int?,
    val itemName: String,
    val lineLabel: String,
    val engineering: String,
    val startDate: String?,
    val endDate: String?,
    val plannedQty: Int,
    val actualQty: Int,
    val remainingQty: Int,
    val progressPct: String,
    val status: PlanScheduleStatus,
    val operationVariance: String?,
    val source: ScheduleGridRowDto,
)

enum class PlanScheduleStatus(val label: String) {
    Done("生産済"),
    Ongoing("生産中"),
    Pending("準備中"),
}

data class PlanScheduleMachineGroup(
    val machineName: String,
    val operationVariance: String?,
    val rows: List<PlanScheduleRow>,
)

data class PlanScheduleSection(
    val monthLabel: String,
    val engineering: String,
    val machines: List<PlanScheduleMachineGroup>,
)

enum class OperationVarianceProgressKind {
    None,
    Normal,
    Ahead,
    Behind,
    OverPlan,
    SevereBehind,
}

data class MachineOvHeadParts(
    val display: String,
    val num: Double?,
    val kind: OperationVarianceProgressKind,
    val negative: Boolean,
    val progressLabel: String,
)

data class SectionCriticalProgress(
    val overPlan: List<String>,
    val severeBehind: List<String>,
)

data class PlanUpdateDisplayRow(
    val record: PlanUpdateRecordDto,
    val showProcess: Boolean,
    val showMachine: Boolean,
    val showOperator: Boolean,
)

object PlanScheduleLogic {
    private const val PROCESS_CD_FORMING = "KT04"
    private const val PROCESS_CD_WELDING = "KT07"

    fun buildTableGridRange(): Pair<String, String> {
        val today = LocalDate.now()
        val start = today.minusYears(10)
        val end = today.plusYears(5)
        return start.format(DateTimeFormatter.ISO_LOCAL_DATE) to end.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun monthGanttDates(year: Int, month: Int): List<String> {
        val first = LocalDate.of(year, month, 1)
        val last = first.withDayOfMonth(first.lengthOfMonth())
        return generateSequence(first) { d -> if (d < last) d.plusDays(1) else null }.map {
            it.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.toList()
    }

    fun flattenRows(
        blocks: List<com.example.smart_emap.data.model.LineGridBlockDto>,
        lineNameById: Map<Int, String>,
        engineering: String,
    ): List<Pair<ScheduleGridRowDto, String>> {
        val result = mutableListOf<Pair<ScheduleGridRowDto, String>>()
        blocks.forEach { block ->
            val label = lineNameById[block.lineId]
                ?: block.lineCode.ifBlank { "ID ${block.lineId}" }
            block.rows.forEach { row ->
                result += row to label
            }
        }
        return result
    }

    fun enrichRow(
        row: ScheduleGridRowDto,
        lineLabel: String,
        engineering: String,
        ganttDates: List<String>,
        operationVariance: String?,
    ): PlanScheduleRow {
        val span = effectiveScheduleDateSpan(row)
        val actual = periodActualForRow(row, ganttDates)
        val planned = row.plannedProcessQty
        val remaining = tableRemainingLikeForming(row, ganttDates, actual, planned)
        val progress = formatProgress(actual, planned)
        val status = productionStatusKind(span.start ?: row.startDate, span.end ?: row.endDate)
        return PlanScheduleRow(
            id = row.id,
            orderNo = row.orderNo,
            itemName = row.itemName,
            lineLabel = lineLabel,
            engineering = engineering,
            startDate = span.start ?: row.startDate,
            endDate = span.end ?: row.endDate,
            plannedQty = planned,
            actualQty = actual,
            remainingQty = remaining,
            progressPct = progress,
            status = status,
            operationVariance = operationVariance,
            source = row,
        )
    }

    fun rowOverlapsMonth(row: PlanScheduleRow, year: Int, month: Int): Boolean {
        val first = LocalDate.of(year, month, 1)
        val last = first.withDayOfMonth(first.lengthOfMonth())
        val ms = first.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val me = last.format(DateTimeFormatter.ISO_LOCAL_DATE)
        var rs = row.startDate
        var re = row.endDate
        if (rs.isNullOrBlank() && re.isNullOrBlank()) return true
        if (rs.isNullOrBlank()) rs = re
        if (re.isNullOrBlank()) re = rs
        if (rs.isNullOrBlank() || re.isNullOrBlank()) return true
        return !(re < ms || rs > me)
    }

    fun groupSections(rows: List<PlanScheduleRow>, month: Int, monthLabel: String): List<PlanScheduleSection> {
        val year = LocalDate.now().year
        return rows
            .filter { rowOverlapsMonth(it, year, month) }
            .groupBy { it.engineering }
            .entries
            .sortedBy { engineeringSortKey(it.key) }
            .map { (engineering, engRows) ->
                val machines = engRows
                    .groupBy { it.lineLabel }
                    .entries
                    .sortedBy { it.key }
                    .map { (machine, machineRows) ->
                        PlanScheduleMachineGroup(
                            machineName = machine,
                            operationVariance = machineRows.firstOrNull()?.operationVariance,
                            rows = machineRows.sortedWith(compareBy({ it.orderNo ?: 1_000_000 + it.id }, { it.id })),
                        )
                    }
                PlanScheduleSection(monthLabel = monthLabel, engineering = engineering, machines = machines)
            }
    }

    fun monthLabel(month: Int): String = "${LocalDate.now().year}年${month}月"

    fun displayDate(value: String?): String {
        if (value.isNullOrBlank()) return "—"
        return if (value.length >= 10) value.take(10) else value
    }

    fun parseOperationVarianceToNumber(raw: String?): Double? {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty() || s == "—" || s == "-") return null
        return s.replace(",", "").replace("%", "").trim().toDoubleOrNull()
    }

    fun operationVarianceForMachine(varianceMap: Map<String, String>, engineering: String, machineName: String): String {
        val v = varianceMap[operationVarianceKey(engineering, machineName)]
        return if (!v.isNullOrBlank() && v != "—") v else "—"
    }

    fun operationVarianceProgressKind(n: Double?): OperationVarianceProgressKind {
        if (n == null) return OperationVarianceProgressKind.None
        return when {
            n > 10 -> OperationVarianceProgressKind.OverPlan
            n < -10 -> OperationVarianceProgressKind.SevereBehind
            n > 2 -> OperationVarianceProgressKind.Ahead
            n < -2 -> OperationVarianceProgressKind.Behind
            else -> OperationVarianceProgressKind.Normal
        }
    }

    fun operationVarianceProgressLabel(kind: OperationVarianceProgressKind): String = when (kind) {
        OperationVarianceProgressKind.Normal -> "正常"
        OperationVarianceProgressKind.Ahead -> "先行"
        OperationVarianceProgressKind.Behind -> "遅れ"
        OperationVarianceProgressKind.OverPlan -> "計画超過"
        OperationVarianceProgressKind.SevereBehind -> "大幅な遅れ"
        OperationVarianceProgressKind.None -> "—"
    }

    fun machineOvHeadParts(
        varianceMap: Map<String, String>,
        engineering: String,
        machineName: String,
    ): MachineOvHeadParts {
        val display = operationVarianceForMachine(varianceMap, engineering, machineName)
        val num = parseOperationVarianceToNumber(
            varianceMap[operationVarianceKey(engineering, machineName)],
        )
        val kind = operationVarianceProgressKind(num)
        return MachineOvHeadParts(
            display = display,
            num = num,
            kind = kind,
            negative = num != null && num < 0,
            progressLabel = operationVarianceProgressLabel(kind),
        )
    }

    fun sectionCriticalProgressLines(
        varianceMap: Map<String, String>,
        section: PlanScheduleSection,
    ): SectionCriticalProgress {
        val overPlan = mutableListOf<String>()
        val severeBehind = mutableListOf<String>()
        section.machines.forEach { mc ->
            when (machineOvHeadParts(varianceMap, section.engineering, mc.machineName).kind) {
                OperationVarianceProgressKind.OverPlan -> overPlan += mc.machineName
                OperationVarianceProgressKind.SevereBehind -> severeBehind += mc.machineName
                else -> Unit
            }
        }
        return SectionCriticalProgress(overPlan = overPlan, severeBehind = severeBehind)
    }

    fun sortPlanUpdatesForGroup(rows: List<PlanUpdateRecordDto>): List<PlanUpdateRecordDto> =
        rows.sortedWith(
            compareBy<PlanUpdateRecordDto> { engineeringSortKey(it.processName.orEmpty()) }
                .thenBy { it.processName.orEmpty() }
                .thenBy { it.machineName.orEmpty() }
                .thenBy { it.operator.orEmpty() }
                .thenBy { it.planDate.orEmpty() },
        )

    fun buildPlanUpdateDisplayRows(rows: List<PlanUpdateRecordDto>): List<PlanUpdateDisplayRow> {
        val sorted = sortPlanUpdatesForGroup(rows)
        return sorted.mapIndexed { index, record ->
            val prev = sorted.getOrNull(index - 1)
            PlanUpdateDisplayRow(
                record = record,
                showProcess = prev?.processName != record.processName,
                showMachine = (prev?.processName != record.processName) ||
                    (prev?.machineName != record.machineName),
                showOperator = (prev?.processName != record.processName) ||
                    (prev?.machineName != record.machineName) ||
                    (prev?.operator != record.operator),
            )
        }
    }

    fun formatRequiredTime(qty: Int?, efficiencyRate: Double?): String {
        val q = qty ?: return "—"
        val e = efficiencyRate ?: return "—"
        if (e == 0.0) return "—"
        val v = q / e
        if (!v.isFinite()) return "—"
        return String.format(Locale.JAPAN, "%.1f", v)
    }

    fun operationVarianceKey(engineering: String, machineName: String): String = "$engineering|$machineName"

    fun processCdForEngineering(engineering: String?): String? = when (engineering) {
        "成型" -> PROCESS_CD_FORMING
        "溶接" -> PROCESS_CD_WELDING
        else -> null
    }

    private fun effectiveScheduleDateSpan(row: ScheduleGridRowDto): DateSpan {
        val dates = isoDatesWithGridActivity(row)
        if (dates.isEmpty()) return DateSpan(row.startDate, row.endDate)
        return DateSpan(dates.first(), dates.last())
    }

    private fun isoDatesWithGridActivity(row: ScheduleGridRowDto): List<String> {
        val keys = linkedSetOf<String>()
        listOf(row.daily, row.actualDaily, row.remainingDaily, row.defectDaily).forEach { map ->
            map.forEach { (d, v) -> if (v != 0) keys += d }
        }
        return keys.sorted()
    }

    private fun periodActualForRow(row: ScheduleGridRowDto, dates: List<String>): Int =
        dates.sumOf { d -> row.actualDaily[d] ?: 0 }

    private fun tableRemainingLikeForming(row: ScheduleGridRowDto, dates: List<String>, actual: Int, planned: Int): Int {
        val remainByDaily = dates.sumOf { d -> row.remainingDaily[d] ?: 0 }
        if (remainByDaily > 0) return remainByDaily
        val remain = planned - actual
        return if (remain > 0) remain else 0
    }

    private fun formatProgress(actual: Int, planned: Int): String {
        if (planned <= 0) return "0%"
        val pct = (actual.toDouble() / planned * 100.0).coerceIn(0.0, 999.0)
        return "${pct.roundToInt()}%"
    }

    private fun productionStatusKind(start: String?, end: String?): PlanScheduleStatus {
        val today = LocalDate.now()
        val startDate = parseLocalDay(start)
        val endDate = parseLocalDay(end)
        if (endDate != null && endDate.isBefore(today)) return PlanScheduleStatus.Done
        if (startDate != null && endDate != null &&
            !startDate.isAfter(today) && !endDate.isBefore(today)
        ) {
            return PlanScheduleStatus.Ongoing
        }
        return PlanScheduleStatus.Pending
    }

    private fun parseLocalDay(value: String?): LocalDate? = runCatching {
        value?.take(10)?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
    }.getOrNull()

    fun engineeringSortKey(eng: String): Int = when (eng) {
        "成型" -> 0
        "溶接" -> 1
        else -> 2
    }

    private data class DateSpan(val start: String?, val end: String?)
}
