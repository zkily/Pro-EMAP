package com.example.smart_emap.data.repository

import com.example.smart_emap.data.model.BatchUpdateDailyItemDto
import com.example.smart_emap.data.model.OrderDailyEditRowUi
import com.example.smart_emap.data.model.OrderDailyItemDto
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

object OrderDailyUiMapper {
    private val numberFormat = NumberFormat.getIntegerInstance(Locale.JAPAN)

    fun numericField(value: Int?): String = if (value == null || value == 0) "" else value.toString()

    fun parseNumeric(value: String): Int = value.trim().toIntOrNull() ?: 0

    fun fromDto(dto: OrderDailyItemDto): OrderDailyEditRowUi = OrderDailyEditRowUi(
        id = dto.id,
        destinationName = dto.destinationName.orEmpty(),
        productName = dto.productName.orEmpty(),
        productType = dto.productType.orEmpty(),
        unitPerBox = dto.unitPerBox ?: 0,
        shipDate = dto.date,
        weekday = dto.weekday,
        confirmedBoxes = numericField(dto.confirmedBoxes),
        confirmedUnits = numericField(dto.confirmedUnits),
        forecastUnits = numericField(dto.forecastUnits),
        deliveryDate = dto.deliveryDate,
        status = dto.status,
        remarks = dto.remarks,
    )

    fun processBatchRows(raw: List<OrderDailyItemDto>): List<OrderDailyEditRowUi> {
        val list = raw.map { fromDto(it) }.toMutableList()
        var lastPositiveBoxIndex = -1
        for (i in list.indices.reversed()) {
            if (parseNumeric(list[i].confirmedBoxes) > 0) {
                lastPositiveBoxIndex = i
                break
            }
        }
        if (lastPositiveBoxIndex >= 0) {
            for (i in lastPositiveBoxIndex downTo 0) {
                if (parseNumeric(list[i].confirmedBoxes) <= 0) {
                    list[i] = list[i].copy(confirmedUnits = "")
                }
            }
        }
        return list.sortedBy { sortKey(it.shipDate) }
    }

    fun processManageRows(raw: List<OrderDailyItemDto>): List<OrderDailyEditRowUi> =
        raw.map { fromDto(it) }.sortedBy { it.productName }

    fun toBatchUpdateItem(row: OrderDailyEditRowUi): BatchUpdateDailyItemDto =
        BatchUpdateDailyItemDto(
            id = row.id,
            forecastUnits = parseNumeric(row.forecastUnits),
            confirmedBoxes = parseNumeric(row.confirmedBoxes),
            confirmedUnits = parseNumeric(row.confirmedUnits),
            status = row.status ?: "未出荷",
            remarks = row.remarks.orEmpty(),
        )

    fun applyConfirmedBoxesChange(row: OrderDailyEditRowUi, boxesText: String): OrderDailyEditRowUi {
        val sanitized = boxesText.filter { it.isDigit() }
        val boxes = parseNumeric(sanitized)
        val units = if (row.unitPerBox > 0) boxes * row.unitPerBox else boxes
        return row.copy(
            confirmedBoxes = sanitized,
            confirmedUnits = if (units == 0) "" else units.toString(),
        )
    }

    fun formatShipDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return runCatching {
            val d = LocalDate.parse(value.take(10))
            "${d.year}/${d.monthValue.toString().padStart(2, '0')}/${d.dayOfMonth.toString().padStart(2, '0')}"
        }.getOrDefault(value)
    }

    fun formatDeliveryDate(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return runCatching {
            val d = LocalDate.parse(value.take(10))
            "${d.monthValue}/${d.dayOfMonth}"
        }.getOrDefault(value)
    }

    data class DailySummary(
        val confirmedBoxes: Int = 0,
        val confirmedUnits: Int = 0,
        val forecastUnits: Int = 0,
    )

    fun summarize(rows: List<OrderDailyEditRowUi>, includeForecast: Boolean): DailySummary =
        DailySummary(
            confirmedBoxes = rows.sumOf { parseNumeric(it.confirmedBoxes) },
            confirmedUnits = rows.sumOf { parseNumeric(it.confirmedUnits) },
            forecastUnits = if (includeForecast) rows.sumOf { parseNumeric(it.forecastUnits) } else 0,
        )

    fun buildPrintText(title: String, rows: List<OrderDailyEditRowUi>): String {
        val sb = StringBuilder()
        sb.appendLine(title)
        sb.appendLine("出力日時: ${java.time.LocalDateTime.now()}")
        sb.appendLine()
        rows.groupBy { it.productName.ifBlank { "未設定" } }
            .toSortedMap(compareBy { it })
            .forEach { (productName, group) ->
                sb.appendLine("【$productName】")
                sb.appendLine("納入先\t種別\t入数\t出荷日\t曜\t確定箱\t確定本\t内示\t差異\t納入日")
                var sumBoxes = 0
                var sumUnits = 0
                var sumForecast = 0
                group.sortedBy { sortKey(it.shipDate) }.forEach { row ->
                    val boxes = parseNumeric(row.confirmedBoxes)
                    val units = parseNumeric(row.confirmedUnits)
                    val forecast = parseNumeric(row.forecastUnits)
                    sumBoxes += boxes
                    sumUnits += units
                    sumForecast += forecast
                    sb.appendLine(
                        listOf(
                            row.destinationName,
                            row.productType,
                            row.unitPerBox,
                            formatShipDate(row.shipDate),
                            row.weekday.orEmpty(),
                            boxes,
                            units,
                            forecast,
                            units - forecast,
                            formatDeliveryDate(row.deliveryDate),
                        ).joinToString("\t"),
                    )
                }
                sb.appendLine("合計\t\t\t\t\t$sumBoxes\t$sumUnits\t$sumForecast\t${sumUnits - sumForecast}\t")
                sb.appendLine()
            }
        return sb.toString()
    }

    fun formatCount(value: Int): String = if (value > 0) numberFormat.format(value) else ""

    private fun sortKey(value: String?): String = value?.take(10).orEmpty()
}
