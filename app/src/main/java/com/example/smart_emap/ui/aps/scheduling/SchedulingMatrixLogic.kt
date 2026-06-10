package com.example.smart_emap.ui.aps.scheduling

import androidx.compose.ui.graphics.Color
import com.example.smart_emap.data.model.DailyUpstreamTintSegDto
import com.example.smart_emap.data.model.LineGridBlockDto
import com.example.smart_emap.data.model.ScheduleGridRowDto
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

sealed class SchedulingMatrixRow {
    abstract val key: String
    abstract val lineId: Int
    abstract val lineCode: String
    abstract val lineName: String

    data class Group(
        override val key: String,
        override val lineId: Int,
        override val lineCode: String,
        override val lineName: String,
        val sumPlannedOutputQty: Int,
        val dailyTotals: Map<String, Int>,
        val avgEfficiency: Double?,
    ) : SchedulingMatrixRow()

    data class Item(
        override val key: String,
        override val lineId: Int,
        override val lineCode: String,
        override val lineName: String,
        val orderNo: Int?,
        val itemName: String,
        val materialShortage: Boolean,
        val efficiencyRate: Double,
        val plannedOutputQty: Int,
        val completionRate: Double?,
        val dueDate: String?,
        val daily: Map<String, Int>,
        val actualDaily: Map<String, Int>,
        val hasApsBatchPlans: Boolean,
        val dailyUpstreamTint: Map<String, DailyUpstreamTintSegDto>,
    ) : SchedulingMatrixRow()
}

data class SchedulingMatrixSection(
    val key: String,
    val rows: List<SchedulingMatrixRow>,
)

data class SchedulingMatrixCellStyle(
    val backgroundColor: Color? = null,
    val gradient: Pair<Color, Color>? = null,
    val gradientSplit: Float? = null,
    val tone: SchedulingCellTone = SchedulingCellTone.None,
    val dueHighlight: Boolean = false,
)

enum class SchedulingCellTone {
    None,
    Actual,
    Active,
    High,
    Mid,
    Low,
    Shortage,
}

data class SchedulingMatrixCell(
    val displayValue: Int,
    val displayText: String,
    val title: String,
    val style: SchedulingMatrixCellStyle,
)

object SchedulingMatrixLogic {
    private val isoDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val matrixDate = DateTimeFormatter.ofPattern("MM/dd")
    private val weekdays = listOf("日", "月", "火", "水", "木", "金", "土")
    private val jpNumber = java.text.NumberFormat.getIntegerInstance(Locale.JAPAN)

    private val ignoredLines = setOf("成型他", "FM-026")

    fun isIgnoredLine(lineName: String): Boolean = lineName.trim() in ignoredLines

    fun formatQty(value: Number?): String {
        val n = value?.toDouble() ?: 0.0
        if (!n.isFinite()) return "-"
        return jpNumber.format(n.roundToInt())
    }

    fun formatEfficiency(value: Double?): String {
        if (value == null || !value.isFinite()) return "-"
        return String.format(Locale.JAPAN, "%.1f", value)
    }

    fun formatHours(value: Double?): String {
        if (value == null || !value.isFinite()) return "-"
        return String.format(Locale.JAPAN, "%.1fH", value)
    }

    fun formatMatrixDate(iso: String): String = runCatching {
        LocalDate.parse(iso, isoDate).format(matrixDate)
    }.getOrDefault(iso)

    fun weekdayLabel(iso: String): String = runCatching {
        weekdays[LocalDate.parse(iso, isoDate).dayOfWeek.value % 7]
    }.getOrDefault("")

    fun isWeekend(iso: String): Boolean = runCatching {
        val d = LocalDate.parse(iso, isoDate).dayOfWeek
        d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY
    }.getOrDefault(false)

    fun isToday(iso: String): Boolean = iso == LocalDate.now().format(isoDate)

    fun matrixCellDisplayMode(
        dateIso: String,
        actualQty: Int,
        planExtendMode: Boolean,
        todayStr: String = LocalDate.now().format(isoDate),
    ): String {
        val boundary = if (planExtendMode) {
            LocalDate.parse(todayStr, isoDate).minusDays(5).format(isoDate)
        } else {
            todayStr
        }
        return when {
            planExtendMode -> if (dateIso <= boundary) "actual" else "plan"
            dateIso < todayStr -> "actual"
            dateIso > todayStr -> "plan"
            actualQty > 0 -> "actual"
            else -> "plan"
        }
    }

    fun matrixCellDisplayQty(
        plannedDaily: Map<String, Int>,
        actualDaily: Map<String, Int>,
        dateIso: String,
        planExtendMode: Boolean,
    ): Int {
        val planned = plannedDaily[dateIso] ?: 0
        val actual = actualDaily[dateIso] ?: 0
        return if (matrixCellDisplayMode(dateIso, actual, planExtendMode) == "actual") actual else planned
    }

    fun filterVisibleBlocks(
        blocks: List<LineGridBlockDto>,
        lineNameById: Map<Int, String>,
    ): List<LineGridBlockDto> = blocks.filter { block ->
        val name = resolveLineName(block.lineId, block.lineCode, lineNameById)
        !isIgnoredLine(name)
    }

    fun productOptions(blocks: List<LineGridBlockDto>): List<String> {
        val names = linkedSetOf<String>()
        blocks.forEach { block ->
            block.rows.forEach { row ->
                row.itemName.trim().takeIf { it.isNotEmpty() }?.let(names::add)
            }
        }
        return names.sortedWith(compareBy { it })
    }

    fun filterBlocksByProduct(
        blocks: List<LineGridBlockDto>,
        dates: List<String>,
        itemName: String?,
        planExtendMode: Boolean,
    ): List<LineGridBlockDto> {
        val needle = itemName?.trim().orEmpty()
        if (needle.isEmpty()) return blocks
        return blocks.mapNotNull { block ->
            val rows = block.rows.filter { it.itemName.trim() == needle }
            if (rows.isEmpty()) return@mapNotNull null
            val dailyTotals = dates.associateWith { date ->
                rows.sumOf { row ->
                    matrixCellDisplayQty(row.daily, row.actualDaily, date, planExtendMode)
                }
            }
            val sumPlannedOutput = rows.sumOf { it.plannedOutputQty }
            val sumPlannedProcess = rows.sumOf { it.plannedProcessQty }
            block.copy(
                rows = rows,
                dailyTotals = dailyTotals,
                sumPlannedOutputQty = sumPlannedOutput,
                sumPlannedProcessQty = sumPlannedProcess,
            )
        }
    }

    fun buildMatrixRows(
        blocks: List<LineGridBlockDto>,
        dates: List<String>,
        lineNameById: Map<Int, String>,
        planExtendMode: Boolean,
    ): List<SchedulingMatrixRow> {
        val rows = mutableListOf<SchedulingMatrixRow>()
        blocks.forEachIndexed { blockIndex, block ->
            val periodRows = block.rows.filter { rowHasPeriodData(it, dates) }
            if (periodRows.isEmpty()) return@forEachIndexed

            val lineName = resolveLineName(block.lineId, block.lineCode, lineNameById)
            val groupDisplayTotals = dates.associateWith { date ->
                val fromBlock = block.dailyTotals[date] ?: 0
                if (fromBlock > 0) {
                    fromBlock
                } else {
                    periodRows.sumOf { row ->
                        matrixCellDisplayQty(row.daily, row.actualDaily, date, planExtendMode)
                    }
                }
            }

            var effWeightedSum = 0.0
            var effWeightedDenom = 0.0
            periodRows.forEach { row ->
                val efficiencyRate = row.efficiencyRate ?: row.efficiency
                val plannedProcess = row.plannedProcessQty
                effWeightedSum += efficiencyRate * plannedProcess
                effWeightedDenom += plannedProcess
            }

            val groupKey = "group-${block.lineId}-$blockIndex"
            rows += SchedulingMatrixRow.Group(
                key = groupKey,
                lineId = block.lineId,
                lineCode = block.lineCode,
                lineName = lineName,
                sumPlannedOutputQty = block.sumPlannedOutputQty,
                dailyTotals = groupDisplayTotals,
                avgEfficiency = if (effWeightedDenom > 0) effWeightedSum / effWeightedDenom else null,
            )

            periodRows.forEach { row ->
                rows += SchedulingMatrixRow.Item(
                    key = "item-${block.lineId}-${row.id}",
                    lineId = block.lineId,
                    lineCode = block.lineCode,
                    lineName = lineName,
                    orderNo = row.orderNo,
                    itemName = row.itemName,
                    materialShortage = row.materialShortage,
                    efficiencyRate = row.efficiencyRate ?: row.efficiency,
                    plannedOutputQty = row.plannedOutputQty,
                    completionRate = row.completionRate,
                    dueDate = row.dueDate,
                    daily = row.daily,
                    actualDaily = row.actualDaily,
                    hasApsBatchPlans = row.hasApsBatchPlans,
                    dailyUpstreamTint = row.dailyUpstreamTint,
                )
            }
        }
        return rows
    }

    fun buildMatrixSections(rows: List<SchedulingMatrixRow>): List<SchedulingMatrixSection> {
        val sections = mutableListOf<SchedulingMatrixSection>()
        var currentRows = mutableListOf<SchedulingMatrixRow>()
        var currentKey = ""
        rows.forEach { row ->
            if (row is SchedulingMatrixRow.Group) {
                if (currentRows.isNotEmpty()) {
                    sections += SchedulingMatrixSection(currentKey, currentRows)
                }
                currentKey = row.key
                currentRows = mutableListOf(row)
            } else {
                if (currentRows.isEmpty()) currentKey = row.key
                currentRows += row
            }
        }
        if (currentRows.isNotEmpty()) sections += SchedulingMatrixSection(currentKey, currentRows)
        return sections
    }

    fun overallPlannedOutputTotal(
        blocks: List<LineGridBlockDto>,
        dates: List<String>,
    ): Int = blocks.sumOf { block ->
        block.rows.sumOf { row ->
            dates.sumOf { date -> row.daily[date] ?: 0 }
        }
    }

    fun avgEfficiencyRate(blocks: List<LineGridBlockDto>): Double? {
        var weightedSum = 0.0
        var weightedDenom = 0.0
        blocks.forEach { block ->
            block.rows.forEach { row ->
                val rate = row.efficiencyRate ?: row.efficiency
                val qty = row.plannedProcessQty.toDouble()
                if (!rate.isFinite() || !qty.isFinite() || qty <= 0) return@forEach
                weightedSum += rate * qty
                weightedDenom += qty
            }
        }
        return if (weightedDenom > 0) weightedSum / weightedDenom else null
    }

    fun requiredProductionHours(totalQty: Int, avgRate: Double?): Double? {
        val rate = avgRate ?: return null
        if (!rate.isFinite() || rate <= 0) return null
        return totalQty / rate
    }

    fun overallDailyTotals(
        blocks: List<LineGridBlockDto>,
        dates: List<String>,
        planExtendMode: Boolean,
    ): Map<String, Int> = dates.associateWith { date ->
        blocks.sumOf { block ->
            block.rows.sumOf { row ->
                matrixCellDisplayQty(row.daily, row.actualDaily, date, planExtendMode)
            }
        }
    }

    fun resolveLineName(lineId: Int, fallbackCode: String, lineNameById: Map<Int, String>): String =
        lineNameById[lineId]?.trim().orEmpty().ifEmpty { fallbackCode }

    fun processFeatureLabel(processCd: String, processOptions: List<Pair<String, String>>): String {
        val cd = processCd.trim()
        if (cd.isEmpty()) return "全工程"
        val name = processOptions.firstOrNull { it.first == cd }?.second?.trim().orEmpty()
        return name.ifEmpty { cd }
    }

    fun buildCell(
        row: SchedulingMatrixRow,
        date: String,
        planExtendMode: Boolean,
    ): SchedulingMatrixCell {
        val displayValue = cellDisplayValue(row, date, planExtendMode)
        val style = cellStyle(row, date, displayValue, planExtendMode)
        return SchedulingMatrixCell(
            displayValue = displayValue,
            displayText = if (displayValue > 0) formatQty(displayValue) else "",
            title = cellTitle(row, date, displayValue, planExtendMode),
            style = style,
        )
    }

    private fun rowHasPeriodData(row: ScheduleGridRowDto, dates: List<String>): Boolean =
        dates.any { date ->
            (row.daily[date] ?: 0) > 0 || (row.actualDaily[date] ?: 0) > 0
        }

    private fun cellDisplayValue(
        row: SchedulingMatrixRow,
        date: String,
        planExtendMode: Boolean,
    ): Int = when (row) {
        is SchedulingMatrixRow.Group -> row.dailyTotals[date] ?: 0
        is SchedulingMatrixRow.Item -> matrixCellDisplayQty(row.daily, row.actualDaily, date, planExtendMode)
    }

    private fun cellStyle(
        row: SchedulingMatrixRow,
        date: String,
        displayValue: Int,
        planExtendMode: Boolean,
    ): SchedulingMatrixCellStyle {
        if (row !is SchedulingMatrixRow.Item) return SchedulingMatrixCellStyle()
        val dueMatch = row.dueDate == date
        val upstream = upstreamStyle(row, date, planExtendMode)
        if (upstream != null) {
            return SchedulingMatrixCellStyle(
                backgroundColor = upstream.first,
                gradient = upstream.second,
                gradientSplit = upstream.third,
                dueHighlight = dueMatch,
            )
        }
        val tone = when {
            row.materialShortage && displayValue > 0 -> SchedulingCellTone.Shortage
            row.materialShortage -> SchedulingCellTone.None
            cellShowsActualDisplay(row, date, displayValue, planExtendMode) -> SchedulingCellTone.Actual
            displayValue <= 0 -> SchedulingCellTone.None
            else -> toneFromCompletion(row.completionRate)
        }
        return SchedulingMatrixCellStyle(tone = tone, dueHighlight = dueMatch)
    }

    private fun toneFromCompletion(rate: Double?): SchedulingCellTone {
        if (rate == null || !rate.isFinite()) return SchedulingCellTone.Active
        return when {
            rate >= 80 -> SchedulingCellTone.High
            rate >= 50 -> SchedulingCellTone.Mid
            else -> SchedulingCellTone.Low
        }
    }

    private fun cellShowsActualDisplay(
        row: SchedulingMatrixRow.Item,
        date: String,
        displayValue: Int,
        planExtendMode: Boolean,
    ): Boolean {
        if (displayValue <= 0) return false
        val actual = row.actualDaily[date] ?: 0
        return matrixCellDisplayMode(date, actual, planExtendMode) == "actual"
    }

    private fun plannedUpstreamTintActive(row: SchedulingMatrixRow.Item, date: String, planExtendMode: Boolean): Boolean {
        val actual = row.actualDaily[date] ?: 0
        return matrixCellDisplayMode(date, actual, planExtendMode) == "plan"
    }

    private fun upstreamStyle(
        row: SchedulingMatrixRow.Item,
        date: String,
        planExtendMode: Boolean,
    ): Triple<Color?, Pair<Color, Color>?, Float?>? {
        if (row.materialShortage || !row.hasApsBatchPlans || !plannedUpstreamTintActive(row, date, planExtendMode)) return null
        val planned = row.daily[date] ?: 0
        if (planned <= 0) return null
        val seg = row.dailyUpstreamTint[date] ?: return null
        val a = seg.inCutting.toDouble()
        val b = seg.inInstruction.toDouble()
        val c = seg.onlyPlanned.toDouble()
        val t = a + b + c
        if (t <= 0) return null
        val red = SchedulingTheme.UpstreamCutting
        val green = SchedulingTheme.UpstreamPlanned
        return when {
            a == t -> Triple(red, null, null)
            a == 0.0 -> Triple(green, null, null)
            else -> Triple(null, red to green, (a / t).toFloat())
        }
    }

    private fun cellTitle(
        row: SchedulingMatrixRow,
        date: String,
        displayValue: Int,
        planExtendMode: Boolean,
    ): String = when (row) {
        is SchedulingMatrixRow.Group -> {
            if (displayValue <= 0) "$date: ライン合計なし"
            else "$date: ライン合計 $displayValue"
        }
        is SchedulingMatrixRow.Item -> {
            val planned = row.daily[date] ?: 0
            val actual = row.actualDaily[date] ?: 0
            val modeLabel = if (matrixCellDisplayMode(date, actual, planExtendMode) == "actual") "実績" else "計画"
            val seg = row.dailyUpstreamTint[date]
            val tintSum = seg?.let { it.inCutting + it.inInstruction + it.onlyPlanned } ?: 0
            val cutHint = if (
                tintSum > 0 &&
                planned > 0 &&
                plannedUpstreamTintActive(row, date, planExtendMode) &&
                seg != null
            ) {
                " / CM:${seg.inCutting} 指示:${seg.inInstruction} 計画:${seg.onlyPlanned}"
            } else {
                ""
            }
            if (displayValue <= 0) {
                "$date: ${row.itemName} / —（$modeLabel）計$planned 実$actual$cutHint"
            } else {
                "$date: ${row.itemName} / $displayValue（$modeLabel・計$planned/実$actual）$cutHint"
            }
        }
    }
}
