package com.example.smart_emap.ui.erp.production.planning

import com.example.smart_emap.data.model.LineGridBlockDto
import com.example.smart_emap.data.model.ScheduleGridRowDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class PlanBaselineUtilizationRow(
    val lineId: Int,
    val lineLabel: String,
    val scheduleCount: Int,
    val availableHours: Double,
    val plannedQty: Int,
    val actualQty: Int,
    val plannedHours: Double,
    val actualHours: Double,
    val diffHours: Double,
    val planUtilizationPct: Double,
    val actualUtilizationPct: Double,
    val diffUtilizationPct: Double,
)

object PlanBaselineUtilizationLogic {
    private val ymFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun defaultOperationRateMonth(): String = YearMonth.now().format(ymFormatter)

    fun monthRangeFromYm(ym: String): Pair<String, String>? = runCatching {
        val yearMonth = YearMonth.parse(ym.trim(), ymFormatter)
        yearMonth.atDay(1).format(isoFormatter) to yearMonth.atEndOfMonth().format(isoFormatter)
    }.getOrNull()

    fun utilizationMonthLabelJp(ym: String): String = runCatching {
        val yearMonth = YearMonth.parse(ym.trim(), ymFormatter)
        "${yearMonth.year}年${yearMonth.monthValue}月"
    }.getOrElse { "—" }

    fun formatUtilHours(value: Double?): String {
        if (value == null || !value.isFinite()) return "—"
        return String.format("%.1f", value)
    }

    fun formatUtilPercent(value: Double?): String {
        if (value == null || !value.isFinite()) return "—"
        return String.format("%.1f%%", value)
    }

    fun formatUtilDiffHours(value: Double?): String {
        if (value == null || !value.isFinite()) return "—"
        val prefix = if (value > 0) "+" else ""
        return prefix + String.format("%.1f", value)
    }

    fun buildUtilizationRows(
        grid: SchedulingGridResponseDto,
        lineNameById: Map<Int, String>,
    ): List<PlanBaselineUtilizationRow> {
        val monthDates = grid.dates.sorted()
        if (monthDates.isEmpty()) return emptyList()

        val flat = PlanScheduleLogic.flattenRows(grid.blocks, lineNameById, "")
        val calendarMap = grid.blocks.associate { it.lineId to it.calendar }
        val defaultHoursMap = grid.blocks.associate { it.lineId to it.defaultWorkHours }
        val rowToLine = mutableMapOf<Int, Int>()
        grid.blocks.forEach { block -> block.rows.forEach { row -> rowToLine[row.id] = block.lineId } }
        val lastActualByLine = lineLastActualDayInMonth(monthDates, flat, rowToLine)

        val map = linkedMapOf<Int, MutableUtilAgg>()
        flat.forEach { (row, label) ->
            val lineId = rowToLine[row.id] ?: return@forEach
            val plannedQty = monthDates.sumOf { d -> row.daily[d] ?: 0 }
            val actualQty = monthDates.sumOf { d -> row.actualDaily[d] ?: 0 }
            val rate = row.efficiencyRate ?: row.efficiency
            val plannedHours = if (rate > 0) plannedQty / rate else 0.0
            val actualHours = if (rate > 0) actualQty / rate else 0.0
            val endDay = lastActualByLine[lineId]
            val diffDates = if (endDay.isNullOrBlank()) emptyList() else monthDates.filter { it <= endDay }
            val diffQtyRow = diffDates.sumOf { d -> (row.actualDaily[d] ?: 0) - (row.daily[d] ?: 0) }
            val diffHoursRow = if (rate > 0) diffQtyRow / rate else 0.0

            val item = map.getOrPut(lineId) {
                MutableUtilAgg(lineId, label.ifBlank { lineNameById[lineId] ?: "ID $lineId" })
            }
            item.scheduleCount += 1
            item.plannedQty += plannedQty
            item.actualQty += actualQty
            item.plannedHours += plannedHours
            item.actualHours += actualHours
            item.diffHours += diffHoursRow
        }

        return map.values.map { agg ->
            val calMap = calendarMap[agg.lineId].orEmpty()
            val fallback = defaultHoursMap[agg.lineId] ?: 0.0
            val avail = monthDates.sumOf { d -> (calMap[d] ?: fallback).takeIf { it.isFinite() } ?: 0.0 }
            PlanBaselineUtilizationRow(
                lineId = agg.lineId,
                lineLabel = agg.lineLabel,
                scheduleCount = agg.scheduleCount,
                availableHours = avail,
                plannedQty = agg.plannedQty,
                actualQty = agg.actualQty,
                plannedHours = agg.plannedHours,
                actualHours = agg.actualHours,
                diffHours = agg.diffHours,
                planUtilizationPct = if (avail > 0) (agg.plannedHours / avail) * 100.0 else 0.0,
                actualUtilizationPct = if (avail > 0) (agg.actualHours / avail) * 100.0 else 0.0,
                diffUtilizationPct = if (avail > 0) (agg.diffHours / avail) * 100.0 else 0.0,
            )
        }.sortedBy { it.lineLabel }
    }

    private data class MutableUtilAgg(
        val lineId: Int,
        val lineLabel: String,
        var scheduleCount: Int = 0,
        var plannedQty: Int = 0,
        var actualQty: Int = 0,
        var plannedHours: Double = 0.0,
        var actualHours: Double = 0.0,
        var diffHours: Double = 0.0,
    )

    private fun lineLastActualDayInMonth(
        monthDates: List<String>,
        flat: List<Pair<ScheduleGridRowDto, String>>,
        rowToLine: Map<Int, Int>,
    ): Map<Int, String?> {
        val lineIds = rowToLine.values.toSet()
        val lastByLine = lineIds.associateWith { null as String? }.toMutableMap()
        for (date in monthDates) {
            val daySum = mutableMapOf<Int, Int>()
            flat.forEach { (row, _) ->
                val lineId = rowToLine[row.id] ?: return@forEach
                daySum[lineId] = (daySum[lineId] ?: 0) + (row.actualDaily[date] ?: 0)
            }
            daySum.forEach { (lineId, sum) ->
                if (sum > 0) lastByLine[lineId] = date
            }
        }
        return lastByLine
    }
}
