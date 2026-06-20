package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.ui.graphics.Color
import com.example.smart_emap.data.model.OutsourcingPlatingOrderDto
import com.example.smart_emap.data.model.OutsourcingPlatingReceivingDto
import com.example.smart_emap.data.model.OutsourcingWeldingOrderDto
import com.example.smart_emap.data.model.OutsourcingWeldingReceivingDto
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val OutsourcingAccent = Color(0xFF6366F1)
val OutsourcingAccentSoft = Color(0xFFEEF2FF)
val OutsourcingJst: ZoneId = ZoneId.of("Asia/Tokyo")
val OutsourcingDateFmt: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)
private val jpCurrency = NumberFormat.getCurrencyInstance(Locale.JAPAN)

fun outsourcingTodayJapan(): String = LocalDate.now(OutsourcingJst).format(OutsourcingDateFmt)

fun formatOutsourcingNumber(value: Int?): String = jpNumber.format(value ?: 0)

fun formatOutsourcingCurrency(value: Double?): String = jpCurrency.format(value ?: 0.0)

fun orderAmount(quantity: Int?, unitPrice: Double?): Double =
    (quantity ?: 0) * (unitPrice ?: 0.0)

fun platingOrderReceivingQty(row: OutsourcingPlatingOrderDto): Int =
    row.totalReceivingQty ?: row.receivedQty ?: 0

fun weldingOrderReceivingQty(row: OutsourcingWeldingOrderDto): Int =
    row.totalReceivingQty ?: row.receivedQty ?: 0

fun calculatePlatingOrderStatus(row: OutsourcingPlatingOrderDto): String {
    val backendStatus = row.status.orEmpty().lowercase()
    val quantity = row.quantity ?: 0
    val receivingQty = platingOrderReceivingQty(row)
    return when {
        backendStatus == "pending" -> "未発注"
        backendStatus == "ordered" || backendStatus == "partial" -> when {
            receivingQty == 0 -> "発注済"
            receivingQty >= quantity -> "受入完"
            receivingQty in 1 until quantity -> "一部受入"
            else -> "発注済"
        }
        backendStatus == "completed" -> "受入完"
        backendStatus == "cancelled" -> "取消"
        receivingQty == 0 -> "未発注"
        receivingQty >= quantity -> "受入完"
        receivingQty in 1 until quantity -> "一部受入"
        else -> "発注済"
    }
}

fun calculateWeldingOrderStatus(row: OutsourcingWeldingOrderDto): String {
    val backendStatus = row.status.orEmpty().lowercase()
    val quantity = row.quantity ?: 0
    val receivingQty = weldingOrderReceivingQty(row)
    return when {
        backendStatus == "pending" -> "未発注"
        backendStatus == "ordered" || backendStatus == "partial" -> when {
            receivingQty == 0 -> "発注済"
            receivingQty >= quantity -> "受入完"
            receivingQty in 1 until quantity -> "一部受入"
            else -> "発注済"
        }
        backendStatus == "completed" -> "受入完"
        backendStatus == "cancelled" -> "取消"
        receivingQty == 0 -> "未発注"
        receivingQty >= quantity -> "受入完"
        receivingQty in 1 until quantity -> "一部受入"
        else -> "発注済"
    }
}

fun orderProgressPercent(received: Int, quantity: Int): Int =
    if (quantity <= 0) 0 else ((received.toDouble() / quantity) * 100).toInt().coerceIn(0, 100)

fun calculateReceivingStatus(receivingQty: Int, orderQty: Int): String = when {
    receivingQty == 0 -> "未検収"
    receivingQty >= orderQty -> "検収済"
    receivingQty in 1 until orderQty -> "一部検収"
    else -> "未検収"
}

fun calculatePlatingReceivingStatus(row: OutsourcingPlatingReceivingDto): String =
    calculateReceivingStatus(row.receivingQty ?: 0, row.orderQty ?: 0)

fun calculateWeldingReceivingStatus(row: OutsourcingWeldingReceivingDto): String =
    calculateReceivingStatus(row.receivingQty ?: 0, row.orderQty ?: 0)

fun orderStatusColor(status: String): Color = when (status) {
    "未発注" -> Color(0xFF64748B)
    "発注済" -> Color(0xFFF59E0B)
    "一部受入" -> OutsourcingAccent
    "受入完" -> Color(0xFF10B981)
    "取消" -> Color(0xFFEF4444)
    else -> Color(0xFF64748B)
}

data class OutsourcingSupplierColor(val content: Color, val container: Color)

private val supplierColorPalette = listOf(
    OutsourcingSupplierColor(Color(0xFF667EEA), Color(0xFFE8ECFF)),
    OutsourcingSupplierColor(Color(0xFF67C23A), Color(0xFFE8F5E9)),
    OutsourcingSupplierColor(Color(0xFFE6A23C), Color(0xFFFEF5E7)),
    OutsourcingSupplierColor(Color(0xFFF56C6C), Color(0xFFFDECDA)),
    OutsourcingSupplierColor(Color(0xFF909399), Color(0xFFF4F4F5)),
    OutsourcingSupplierColor(Color(0xFF409EFF), Color(0xFFE1F3FF)),
    OutsourcingSupplierColor(Color(0xFF9C27B0), Color(0xFFF3E5F5)),
    OutsourcingSupplierColor(Color(0xFF00BCD4), Color(0xFFE0F7FA)),
)

fun outsourcingSupplierColor(supplier: String?): OutsourcingSupplierColor {
    if (supplier.isNullOrBlank()) return supplierColorPalette[0]
    var hash = 0
    supplier.forEach { ch ->
        hash = ch.code + ((hash shl 5) - hash)
    }
    return supplierColorPalette[kotlin.math.abs(hash) % supplierColorPalette.size]
}

fun receivingStatusColor(status: String): Color = when (status) {
    "未検収" -> Color(0xFFF59E0B)
    "一部検収" -> OutsourcingAccent
    "検収済" -> Color(0xFF10B981)
    else -> Color(0xFF64748B)
}

fun shiftOutsourcingDate(date: String, days: Int): String =
    runCatching {
        LocalDate.parse(date).plusDays(days.toLong()).format(OutsourcingDateFmt)
    }.getOrElse { date }

fun outsourcingMonthRange(year: Int, month: Int): Pair<String, String> {
    val first = LocalDate.of(year, month, 1)
    val last = first.withDayOfMonth(first.lengthOfMonth())
    return first.format(OutsourcingDateFmt) to last.format(OutsourcingDateFmt)
}

fun outsourcingThisMonthRange(): Pair<String, String> {
    val today = LocalDate.now(OutsourcingJst)
    return outsourcingMonthRange(today.year, today.monthValue)
}

fun outsourcingPrevMonthRange(): Pair<String, String> {
    val prev = LocalDate.now(OutsourcingJst).minusMonths(1)
    return outsourcingMonthRange(prev.year, prev.monthValue)
}

fun outsourcingNextMonthRange(): Pair<String, String> {
    val next = LocalDate.now(OutsourcingJst).plusMonths(1)
    return outsourcingMonthRange(next.year, next.monthValue)
}

fun outsourcingAddBusinessDays(date: String, days: Int): String {
    var current = runCatching { LocalDate.parse(date) }.getOrNull() ?: return date
    var remaining = days.coerceAtLeast(0)
    while (remaining > 0) {
        current = current.plusDays(1)
        val dow = current.dayOfWeek.value
        if (dow in 1..5) remaining--
    }
    return current.format(OutsourcingDateFmt)
}

fun outsourcingWeekdayDatesBetween(start: String, end: String): List<String> {
    val startDate = runCatching { LocalDate.parse(start) }.getOrNull() ?: return emptyList()
    val endDate = runCatching { LocalDate.parse(end) }.getOrNull() ?: return emptyList()
    if (endDate.isBefore(startDate)) return emptyList()
    val dates = mutableListOf<String>()
    var current = startDate
    while (!current.isAfter(endDate)) {
        if (current.dayOfWeek.value in 1..5) {
            dates.add(current.format(OutsourcingDateFmt))
        }
        current = current.plusDays(1)
    }
    return dates
}

fun formatOutsourcingOrderDateJa(date: String?): String {
    val parsed = runCatching { LocalDate.parse(date?.take(10)) }.getOrNull() ?: return date.orEmpty()
    return "${parsed.year}年${parsed.monthValue}月${parsed.dayOfMonth}日"
}
