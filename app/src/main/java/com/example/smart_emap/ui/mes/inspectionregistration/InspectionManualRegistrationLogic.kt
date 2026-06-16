package com.example.smart_emap.ui.mes.inspectionregistration

import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.ui.mes.inspection.HistoryRowFormat
import com.example.smart_emap.ui.mes.inspection.InspectionManagementRowExt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InspectionManualRegistrationLogic {
    const val SHIAGE_SECTION_NAME = "仕上課"
    private val JST = ZoneId.of("Asia/Tokyo")
    private val dayPattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

    data class DefectGroup(
        val processCd: String,
        val processName: String,
        val items: List<ProcessDefectItemDto>,
    )

    data class ProductionWindow(
        val startedMs: Long?,
        val endedMs: Long?,
        val endsNextDay: Boolean,
    )

    data class TimeSummary(
        val shiftMin: Int? = null,
        val workMin: Int? = null,
        val breakMin: Int = 0,
        val stopMin: Int = 0,
        val endsNextDay: Boolean = false,
    )

    data class QtyMismatchInfo(
        val piece: Int,
        val upb: Int,
    )

    fun parseQtyInput(raw: String): Int? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toIntOrNull()?.coerceAtLeast(0)
    }

    fun formatQtyInput(value: Int?): String = value?.toString().orEmpty()

    fun sanitizeTimeDraft(raw: String): String {
        val s = raw.filter { it.isDigit() || it == ':' }
        if (':' in s) {
            val parts = s.split(':', limit = 2)
            return "${parts[0].take(2)}:${parts.getOrElse(1) { "" }.take(2)}"
        }
        return s.take(4)
    }

    fun parseTimeInput(raw: String?): String? {
        val s = raw?.trim().orEmpty()
        if (s.isEmpty()) return null
        val colon = Regex("""^(\d{1,2}):(\d{1,2})$""").matchEntire(s)
        if (colon != null) {
            val h = colon.groupValues[1].toIntOrNull() ?: return null
            val m = colon.groupValues[2].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return "%02d:%02d:00".format(h, m)
        }
        val digits = s.filter { it.isDigit() }
        return when (digits.length) {
            1, 2 -> {
                val h = digits.toIntOrNull() ?: return null
                if (h !in 0..23) return null
                "%02d:00:00".format(h)
            }
            3 -> {
                val h = digits.take(1).toIntOrNull() ?: return null
                val m = digits.drop(1).toIntOrNull() ?: return null
                if (h !in 0..23 || m !in 0..59) return null
                "%02d:%02d:00".format(h, m)
            }
            else -> {
                val h = digits.take(2).toIntOrNull() ?: return null
                val m = digits.drop(2).take(2).toIntOrNull() ?: return null
                if (h !in 0..23 || m !in 0..59) return null
                "%02d:%02d:00".format(h, m)
            }
        }
    }

    fun formatTimeDisplay(time: String?): String {
        if (time.isNullOrBlank()) return ""
        val m = Regex("""^(\d{1,2}):(\d{2})""").find(time.trim()) ?: return ""
        return "%02d:%02d".format(m.groupValues[1].toInt(), m.groupValues[2].toInt())
    }

    fun timeOnlyFromIso(iso: String?): String? {
        val ms = MesDateTime.parseToMillis(iso) ?: return null
        return Instant.ofEpochMilli(ms).atZone(JST).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    fun combineProductionDayAndTime(day: String, time: String?): Long? {
        val dayStr = day.trim().take(10)
        if (!dayPattern.matches(dayStr) || time.isNullOrBlank()) return null
        val normalized = parseTimeInput(time) ?: return null
        val match = Regex("""^(\d{2}):(\d{2}):(\d{2})$""").matchEntire(normalized) ?: return null
        val date = LocalDate.parse(dayStr)
        val dt = LocalDateTime.of(
            date.year,
            date.monthValue,
            date.dayOfMonth,
            match.groupValues[1].toInt(),
            match.groupValues[2].toInt(),
            match.groupValues[3].toInt(),
        )
        return dt.atZone(JST).toInstant().toEpochMilli()
    }

    fun resolveProductionWindow(day: String, startTime: String?, endTime: String?): ProductionWindow {
        val started = combineProductionDayAndTime(day, startTime)
        val endedSameDay = combineProductionDayAndTime(day, endTime)
        if (started == null || endedSameDay == null) {
            return ProductionWindow(started, endedSameDay, false)
        }
        if (endedSameDay <= started) {
            val nextDayEnd = endedSameDay + 24L * 60 * 60 * 1000
            return ProductionWindow(started, nextDayEnd, true)
        }
        return ProductionWindow(started, endedSameDay, false)
    }

    fun pieceQtyFromBoxes(boxes: Int, unitPerBox: Int): Int = boxes * unitPerBox

    fun boxQtyFromPieces(pieces: Int, unitPerBox: Int): Int {
        if (unitPerBox <= 0) return 0
        return (pieces.toDouble() / unitPerBox).toInt()
    }

    fun hasPieceBoxQtyMismatch(pieceQty: Int, unitPerBox: Int): Boolean {
        if (unitPerBox <= 0) return false
        return pieceQty % unitPerBox != 0
    }

    fun mergeDefects(defects: Map<String, Int>): Map<String, Int> =
        defects.mapNotNull { (k, v) ->
            val qty = v.coerceAtLeast(0)
            if (qty > 0) k to qty else null
        }.toMap()

    /** 帰属工程の表示順（成型 → メッキ → 検査、Web loadProcessDefectItems と同順） */
    private val ATTRIBUTABLE_PROCESS_DISPLAY_ORDER = listOf(
        "KT01",
        "KT02",
        "KT04",
        "KT07",
        "KT05",
        "KT09",
    )

    private fun attributableProcessSortIndex(processCd: String): Int {
        val cd = processCd.trim().uppercase()
        val idx = ATTRIBUTABLE_PROCESS_DISPLAY_ORDER.indexOf(cd)
        return if (idx == -1) ATTRIBUTABLE_PROCESS_DISPLAY_ORDER.size else idx
    }

    fun groupDefectItems(items: List<ProcessDefectItemDto>): List<DefectGroup> =
        items.groupBy { (it.attributableProcessCd ?: "").trim().ifEmpty { "—" } }
            .map { (cd, list) ->
                DefectGroup(
                    processCd = cd,
                    processName = list.firstOrNull()?.attributableProcessName?.trim().orEmpty().ifEmpty { cd },
                    items = list,
                )
            }
            .sortedWith(
                compareBy<DefectGroup> { attributableProcessSortIndex(it.processCd) }
                    .thenBy { it.processCd },
            )

    fun isRowMesInProgress(row: InspectionManagementRowDto): Boolean =
        !row.mesProductionStartedAt.isNullOrBlank() && row.mesProductionEndedAt.isNullOrBlank()

    fun resolveDataSource(row: InspectionManagementRowDto): String {
        val ds = row.dataSource?.trim()?.lowercase()
        if (ds == "mes" || ds == "excel" || ds == "csv") return ds
        val remarks = row.remarks?.trim().orEmpty()
        if (remarks.startsWith("EXCEL_SYNC:")) return "excel"
        if (remarks.startsWith("CSV_IMPORT:")) return "csv"
        return "mes"
    }

    fun dataSourceLabel(source: String): String = when (source) {
        "excel" -> "Excel"
        "csv" -> "CSV"
        else -> "MES"
    }

    fun resolveEfficiencyRate(row: InspectionManagementRowDto): Int? {
        val text = InspectionManagementRowExt.formatEfficiencyRate(row)
        if (text == "—") return null
        return text.toIntOrNull()
    }

    fun isEfficiencyOutOfRange(rate: Int?): Boolean {
        if (rate == null) return false
        return rate < 200 || rate > 800
    }

    fun formatBreakMin(row: InspectionManagementRowDto): String {
        val br = (row.mesBreakSec ?: 0) / 60.0
        val min = kotlin.math.round(br).toInt()
        return if (min > 0) "${min}分" else "—"
    }

    fun formatStopMin(row: InspectionManagementRowDto): String {
        val st = (row.mesStopSec ?: 0) / 60.0
        val min = kotlin.math.round(st).toInt()
        return if (min > 0) "${min}分" else "—"
    }

    fun formatDateTimeShort(iso: String?): String = HistoryRowFormat.formatRecordTime(iso)

    fun formatMinutesLabel(totalMin: Int): String {
        val m = totalMin.coerceAtLeast(0)
        val h = m / 60
        val min = m % 60
        return if (h <= 0) "${min}分" else "${h}時間${min}分"
    }

    fun buildTimeSummary(
        productionDay: String,
        startedAtText: String,
        endedAtText: String,
        breakMin: Int,
        stopMin: Int,
    ): TimeSummary {
        val started = parseTimeInput(startedAtText)
        val ended = parseTimeInput(endedAtText)
        val window = resolveProductionWindow(productionDay, started, ended)
        val ws = window.startedMs
        val we = window.endedMs
        val br = breakMin.coerceAtLeast(0)
        val st = stopMin.coerceAtLeast(0)
        val pauseMin = br + st
        if (ws == null || we == null) {
            return TimeSummary(breakMin = br, stopMin = st)
        }
        val shiftMin = ((we - ws) / 60000.0).toInt()
        val workMin = (shiftMin - pauseMin).coerceAtLeast(0)
        return TimeSummary(
            shiftMin = shiftMin,
            workMin = workMin,
            breakMin = br,
            stopMin = st,
            endsNextDay = window.endsNextDay,
        )
    }

    fun buildQtyMismatch(pieceQtyText: String, unitPerBox: Int): QtyMismatchInfo? {
        if (unitPerBox <= 0) return null
        val piece = parseQtyInput(pieceQtyText) ?: return null
        if (!hasPieceBoxQtyMismatch(piece, unitPerBox)) return null
        return QtyMismatchInfo(piece = piece, upb = unitPerBox)
    }
}
