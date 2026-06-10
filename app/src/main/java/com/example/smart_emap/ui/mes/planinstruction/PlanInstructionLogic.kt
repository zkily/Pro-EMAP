package com.example.smart_emap.ui.mes.planinstruction

import com.example.smart_emap.data.model.PlanInstructionRecordDto
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PlanInstructionStats(
    val totalQuantity: Int = 0,
    val machineCount: Int = 0,
)

data class PlanQtyChartPoint(
    val date: String,
    val planQty: Int,
    val actualQty: Int,
)

object PlanInstructionLogic {
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayDate = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)
    private val jpNumber = NumberFormat.getNumberInstance(Locale.JAPAN)

    private val highlightProducts = setOf(
        "900B FR", "900B RR", "900B 対米", "410D CTR", "410D FR1", "410D FR2", "410D RR",
    )

    fun todayIso(): String = LocalDate.now().format(isoDate)

    fun shiftDate(iso: String, days: Int): String =
        runCatching { LocalDate.parse(iso, isoDate).plusDays(days.toLong()).format(isoDate) }
            .getOrDefault(iso)

    fun formatDisplayDate(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            LocalDate.parse(iso.take(10), isoDate).format(displayDate)
        }.getOrDefault(iso)
    }

    /** Web compact-date-picker 表示（MM/DD） */
    fun formatShortDate(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            val d = LocalDate.parse(iso.take(10), isoDate)
            String.format(Locale.US, "%02d/%02d", d.monthValue, d.dayOfMonth)
        }.getOrDefault(iso)
    }

    fun formatNumber(value: Int): String = jpNumber.format(value)

    fun formatEfficiency(value: Double?): String {
        if (value == null || !value.isFinite() || value <= 0) return "-"
        val rounded = (value * 10).toInt() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else String.format(Locale.US, "%.1f", rounded)
    }

    fun isHighlightProduct(name: String?): Boolean =
        name?.trim()?.let { highlightProducts.contains(it) } == true

    fun defaultRemarksPlaceholder(name: String?): String =
        if (isHighlightProduct(name)) "新聞紙をかける" else "備考を入力"

    fun filterPlanRows(
        records: List<PlanInstructionRecordDto>,
        machineName: String?,
        keyword: String?,
    ): List<PlanInstructionRecordDto> {
        val machine = machineName?.trim().orEmpty()
        val kw = keyword?.trim().orEmpty()
        return records
            .filter { row ->
                val qty = row.quantity ?: 0
                val name = row.productName?.trim().orEmpty()
                qty > 0 && name.isNotEmpty()
            }
            .filter { row ->
                machine.isEmpty() || row.machineName == machine
            }
            .filter { row ->
                if (kw.isEmpty()) return@filter true
                val hay = listOf(row.productName, row.machineName, row.productCd)
                    .joinToString(" ") { it.orEmpty() }
                hay.contains(kw, ignoreCase = true)
            }
            .sortedWith(compareBy<PlanInstructionRecordDto> { it.planDate.orEmpty() }
                .thenBy {
                    it.operator?.toIntOrNull() ?: Int.MAX_VALUE
                })
    }

    fun calculateStats(rows: List<PlanInstructionRecordDto>): PlanInstructionStats {
        val total = rows.sumOf { it.quantity ?: 0 }
        val machines = rows
            .filter { (it.quantity ?: 0) > 0 }
            .mapNotNull { it.machineName?.trim()?.ifEmpty { null } }
            .toSet()
        return PlanInstructionStats(totalQuantity = total, machineCount = machines.size)
    }

    fun chartDateRangeDefault(): Pair<String, String> {
        val start = LocalDate.now().minusDays(2)
        val end = LocalDate.now().plusDays(30)
        return start.format(isoDate) to end.format(isoDate)
    }

    fun chartMonthRange(monthOffset: Int): Pair<String, String> {
        val ym = YearMonth.now().plusMonths(monthOffset.toLong())
        return ym.atDay(1).format(isoDate) to ym.atEndOfMonth().format(isoDate)
    }

    fun enumerateChartDates(startIso: String, endIso: String): List<String> = runCatching {
        val start = LocalDate.parse(startIso, isoDate)
        val end = LocalDate.parse(endIso, isoDate)
        val dates = mutableListOf<String>()
        var cursor = start
        while (!cursor.isAfter(end)) {
            dates += cursor.format(isoDate)
            cursor = cursor.plusDays(1)
        }
        dates
    }.getOrElse { emptyList() }

    fun aggregateChartPoints(
        records: List<PlanInstructionRecordDto>,
        dates: List<String>,
    ): List<PlanQtyChartPoint> {
        val planByDate = mutableMapOf<String, Int>()
        val actualByDate = mutableMapOf<String, Int>()
        records.forEach { row ->
            val date = row.planDate?.take(10).orEmpty()
            if (date.isBlank()) return@forEach
            val name = row.productName?.trim().orEmpty()
            if (name.isEmpty()) return@forEach
            planByDate[date] = (planByDate[date] ?: 0) + (row.quantity ?: 0)
            val actual = row.actualProduction ?: row.actualQty ?: 0
            actualByDate[date] = (actualByDate[date] ?: 0) + actual
        }
        return dates.map { date ->
            PlanQtyChartPoint(
                date = date,
                planQty = planByDate[date] ?: 0,
                actualQty = actualByDate[date] ?: 0,
            )
        }
    }

    fun chartHasData(points: List<PlanQtyChartPoint>): Boolean =
        points.any { it.planQty > 0 || it.actualQty > 0 }

    suspend fun calculateSmartDateRange(
        baseDateIso: String,
        processName: String,
        machineName: String?,
        hasProductionOnDate: suspend (String) -> Boolean,
    ): Pair<String, String> {
        val base = runCatching { LocalDate.parse(baseDateIso, isoDate) }.getOrNull()
            ?: return baseDateIso to baseDateIso
        val dates = mutableListOf<LocalDate>()
        val prev = when (base.dayOfWeek) {
            DayOfWeek.MONDAY -> base.minusDays(3)
            else -> base.minusDays(1)
        }
        dates += prev
        dates += base
        when (base.dayOfWeek) {
            DayOfWeek.FRIDAY -> {
                dates += base.plusDays(3)
                dates += base.plusDays(4)
            }
            DayOfWeek.SATURDAY -> {
                dates += base.plusDays(2)
                dates += base.plusDays(3)
                dates += base.plusDays(4)
            }
            else -> dates += base.plusDays(1)
        }
        val valid = mutableListOf<String>()
        for (date in dates.distinct()) {
            val iso = date.format(isoDate)
            val weekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            if (!weekend) {
                valid += iso
            } else if (hasProductionOnDate(iso)) {
                valid += iso
            }
        }
        valid.sort()
        if (valid.isEmpty()) return baseDateIso to baseDateIso
        return valid.first() to valid.last()
    }

    fun machinesForPrint(
        selectedMachine: String?,
        allMachines: List<String>,
        excluded: Set<String>,
    ): List<String> {
        val selected = selectedMachine?.trim().orEmpty()
        if (selected.isNotEmpty()) {
            return if (excluded.contains(selected)) emptyList() else listOf(selected)
        }
        return allMachines.filter { it.isNotBlank() && !excluded.contains(it) }.sorted()
    }

    fun groupByMachine(rows: List<PlanInstructionRecordDto>): Map<String, List<PlanInstructionRecordDto>> =
        rows.groupBy { it.machineName.orEmpty() }

    fun rowKey(row: PlanInstructionRecordDto): String =
        row.id?.let { "id_$it" }
            ?: "${row.planDate}_${row.machineName}_${row.productCd}_${row.processName}"

    fun baselineMonthFromDate(iso: String?): String? {
        if (iso.isNullOrBlank()) return null
        return runCatching {
            val d = LocalDate.parse(iso.take(10), isoDate)
            String.format(Locale.US, "%04d-%02d", d.year, d.monthValue)
        }.getOrNull()
    }
}
