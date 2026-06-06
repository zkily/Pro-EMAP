package com.example.smart_emap.ui.mes.inspection

import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.data.model.InspectionManagementRowDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val JST = ZoneId.of("Asia/Tokyo")

object HistoryRowFormat {

    private val recordTimeFmt = DateTimeFormatter.ofPattern("MM/dd HH:mm", Locale.JAPAN)
    private val wallInputFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
    private val wallInputAltFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN)

    fun parseIsoToMillis(iso: String?): Long? = MesDateTime.parseToMillis(iso)

    /** 確定実績表：inspection_management.mes_production_started_at */
    fun formatProductionStart(row: InspectionManagementRowDto): String =
        formatRecordTime(row.mesProductionStartedAt)

    /** 確定実績表：inspection_management.mes_production_ended_at */
    fun formatProductionEnd(row: InspectionManagementRowDto): String =
        formatRecordTime(row.mesProductionEndedAt)

    fun formatRecordTime(iso: String?): String {
        val ms = parseIsoToMillis(iso) ?: return "—"
        return Instant.ofEpochMilli(ms).atZone(JST).format(recordTimeFmt)
    }

    fun formatWallInput(ms: Long?): String {
        if (ms == null) return ""
        return Instant.ofEpochMilli(ms).atZone(JST).format(wallInputFmt)
    }

    fun parseWallInput(text: String): Long? {
        val raw = text.trim()
        if (raw.isEmpty()) return null
        parseIsoToMillis(raw)?.let { return it }
        return runCatching {
            java.time.LocalDateTime.parse(raw, wallInputFmt).atZone(JST).toInstant().toEpochMilli()
        }.getOrNull() ?: runCatching {
            java.time.LocalDateTime.parse(raw, wallInputAltFmt).atZone(JST).toInstant().toEpochMilli()
        }.getOrNull()
    }

    /** 確定実績表：開始〜終了の壁時計秒（DB 優先は net ではなく壁時計） */
    fun rowWallElapsedSec(row: InspectionManagementRowDto): Int {
        val ws = parseIsoToMillis(row.mesProductionStartedAt)
        if (ws == null) return (row.mesNetProductionSec ?: 0).coerceAtLeast(0)
        val we = parseIsoToMillis(row.mesProductionEndedAt) ?: System.currentTimeMillis()
        return ((we - ws) / 1000).coerceAtLeast(0).toInt()
    }

    /** 確定実績表：一時停止累計（DB 優先） */
    fun rowPausedAccumSec(row: InspectionManagementRowDto): Int {
        val stored = row.mesPausedAccumSec
        if (stored != null) return stored.coerceAtLeast(0)
        val ws = parseIsoToMillis(row.mesProductionStartedAt) ?: return 0
        val we = parseIsoToMillis(row.mesProductionEndedAt) ?: System.currentTimeMillis()
        val wallSec = ((we - ws) / 1000).coerceAtLeast(0)
        val netSec = (row.mesNetProductionSec ?: 0).coerceAtLeast(0)
        return (wallSec - netSec).coerceAtLeast(0).toInt()
    }

    fun formatSecondsAsMinutes(sec: Int): String {
        return (sec.coerceAtLeast(0) / 60.0).let { kotlin.math.round(it).toInt() }.toString()
    }

    fun productionDayFromMillis(ms: Long): String {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return Instant.ofEpochMilli(ms).atZone(JST).toLocalDate().format(fmt)
    }
}
