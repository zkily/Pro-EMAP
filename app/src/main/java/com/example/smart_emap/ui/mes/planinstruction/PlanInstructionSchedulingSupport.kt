package com.example.smart_emap.ui.mes.planinstruction

import com.example.smart_emap.data.model.ApsProductionLineDto
import com.example.smart_emap.data.model.MasterMachineFullDto
import com.example.smart_emap.data.model.ScheduleGridRowDto
import com.example.smart_emap.data.model.SchedulingGridResponseDto
import com.example.smart_emap.data.repository.PlanInstructionRepository
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

data class OperationVarianceRow(
    val machineName: String,
    val operationVariance: Double,
)

private data class FlatGridRow(
    val lineId: Int,
    val lineLabel: String,
    val efficiencyRate: Double,
    val daily: Map<String, Int>,
    val actualDaily: Map<String, Int>,
)

suspend fun loadUtilizationDiffHoursByLineName(
    repository: PlanInstructionRepository,
    config: PlanInstructionConfig,
    productionDateIso: String,
): Map<String, Double> {
    val monthRange = monthRangeFromDate(productionDateIso) ?: return emptyMap()
    val grid = repository.loadSchedulingGrid(config, monthRange.first, monthRange.second)
    val lines = repository.loadApsLines(config.apsProcessCd)
    return computeUtilizationDiffHours(grid, lines)
}

suspend fun loadOperationVarianceRows(
    repository: PlanInstructionRepository,
    config: PlanInstructionConfig,
    productionDateIso: String,
): List<OperationVarianceRow> =
    loadUtilizationDiffHoursByLineName(repository, config, productionDateIso)
        .map { (name, hours) -> OperationVarianceRow(machineName = name, operationVariance = hours) }
        .sortedBy { it.machineName }

suspend fun loadPlannedWorkHoursByMachine(
    repository: PlanInstructionRepository,
    machines: List<MasterMachineFullDto>,
    productionDateIso: String,
): Map<String, Double> {
    val date = productionDateIso.take(10)
    val result = mutableMapOf<String, Double>()
    machines.forEach { machine ->
        val name = machine.machineName?.trim().orEmpty()
        val lineId = machine.id ?: return@forEach
        if (name.isBlank()) return@forEach
        val capacities = repository.loadLineCapacities(lineId, date)
        val hours = capacities
            .firstOrNull { it.workDate?.take(10) == date }
            ?.availableHours
            ?.takeIf { it.isFinite() && it != 0.0 }
        if (hours != null) {
            result[name] = hours
        }
    }
    return result
}

fun computeUtilizationDiffHours(
    grid: SchedulingGridResponseDto,
    lines: List<ApsProductionLineDto>,
): Map<String, Double> {
    val monthDates = grid.dates.sorted()
    if (monthDates.isEmpty()) return emptyMap()

    val lineNameById = lines.associate { line ->
        val name = line.lineName?.trim().orEmpty()
        val code = line.lineCode.trim()
        line.id to (name.ifBlank { code.ifBlank { "ID ${line.id}" } })
    }

    val flatRows = mutableListOf<FlatGridRow>()
    grid.blocks.forEach { block ->
        val label = lineNameById[block.lineId]
            ?: block.lineCode.ifBlank { "ID ${block.lineId}" }
        block.rows.forEach { row ->
            flatRows += row.toFlatGridRow(block.lineId, label)
        }
    }

    val lastActualByLine = mutableMapOf<Int, String?>()
    flatRows.map { it.lineId }.distinct().forEach { lastActualByLine[it] = null }
    monthDates.forEach { day ->
        val daySum = mutableMapOf<Int, Int>()
        flatRows.forEach { row ->
            daySum[row.lineId] = (daySum[row.lineId] ?: 0) + (row.actualDaily[day] ?: 0)
        }
        daySum.forEach { (lineId, total) ->
            if (total > 0) lastActualByLine[lineId] = day
        }
    }

    val hoursByLineId = mutableMapOf<Int, Double>()
    flatRows.forEach { row ->
        val endDay = lastActualByLine[row.lineId]
        val diffDates = if (endDay.isNullOrBlank()) emptyList() else monthDates.filter { it <= endDay }
        val diffQty = diffDates.sumOf { day ->
            val plan = row.daily[day] ?: 0
            val actual = row.actualDaily[day] ?: 0
            actual - plan
        }
        val rate = row.efficiencyRate.takeIf { it > 0 } ?: 0.0
        val diffHours = if (rate > 0) diffQty / rate else 0.0
        hoursByLineId[row.lineId] = (hoursByLineId[row.lineId] ?: 0.0) + diffHours
    }

    val labelByLineId = flatRows.associate { it.lineId to it.lineLabel }
    return hoursByLineId.mapNotNull { (lineId, hours) ->
        val label = labelByLineId[lineId]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        label to hours
    }.toMap()
}

private fun ScheduleGridRowDto.toFlatGridRow(lineId: Int, lineLabel: String): FlatGridRow =
    FlatGridRow(
        lineId = lineId,
        lineLabel = lineLabel,
        efficiencyRate = efficiencyRate ?: efficiency,
        daily = daily,
        actualDaily = actualDaily,
    )

private fun monthRangeFromDate(iso: String): Pair<String, String>? {
    val date = runCatching {
        LocalDate.parse(iso.take(10), DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull() ?: return null
    val ym = YearMonth.of(date.year, date.month)
    val start = ym.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
    val end = ym.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)
    return start to end
}

fun operationVarianceJudgment(value: Double?): String {
    val n = value ?: return ""
    if (!n.isFinite()) return ""
    return when {
        n > 10 -> "大幅な先行"
        n > 2 -> "先行"
        n < -10 -> "大幅な遅れ"
        n < -2 -> "遅れ"
        else -> "正常"
    }
}

fun operationVarianceJudgmentClass(value: Double?): String {
    val n = value ?: return ""
    if (!n.isFinite()) return ""
    return when {
        n > 10 -> "variance-judge-very-ahead"
        n > 2 -> "variance-judge-ahead"
        n < -10 -> "variance-judge-very-delay"
        n < -2 -> "variance-judge-delay"
        else -> "variance-judge-normal"
    }
}

fun formatOperationVarianceHours(value: Double?): String {
    val n = value ?: return ""
    if (!n.isFinite()) return ""
    return n.toLong().toString()
}

fun formatOperationVarianceForRow(value: Double?): String {
    val n = value ?: return ""
    if (!n.isFinite()) return ""
    return n.roundToInt().toString()
}
