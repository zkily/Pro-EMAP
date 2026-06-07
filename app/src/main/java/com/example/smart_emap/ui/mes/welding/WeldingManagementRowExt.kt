package com.example.smart_emap.ui.mes.welding

import com.example.smart_emap.data.model.WeldingManagementRowDto
import java.util.Locale

/** welding_management 行の表示・集計（Web useWeldingMesCollection と同等） */
object WeldingManagementRowExt {

    fun historyDefectQty(row: WeldingManagementRowDto): Int {
        val stored = row.defectQty ?: 0
        if (stored > 0) return stored
        return row.mesDefectByItem?.values?.sumOf { it.coerceAtLeast(0) } ?: 0
    }

    fun formatHistoryProductionDay(row: WeldingManagementRowDto): String {
        val stored = row.productionDay?.trim()?.take(10).orEmpty()
        if (stored.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return stored
        val fromStart = WeldingHistoryRowFormat.parseIsoToMillis(row.mesProductionStartedAt)
        if (fromStart != null) {
            return WeldingHistoryRowFormat.productionDayFromMillis(fromStart)
        }
        return "—"
    }

    fun formatDefectRate(row: WeldingManagementRowDto): String {
        val prod = row.actualProductionQuantity ?: 0
        val defects = historyDefectQty(row)
        if (prod <= 0) return "—"
        return String.format(Locale.JAPAN, "%.1f%%", defects * 100.0 / prod)
    }

    fun formatEfficiencyRate(row: WeldingManagementRowDto): String {
        val prod = row.actualProductionQuantity ?: 0
        if (prod <= 0) return "—"
        val netSec = (WeldingHistoryRowFormat.rowWallElapsedSec(row) - WeldingHistoryRowFormat.rowPausedAccumSec(row))
            .coerceAtLeast(0)
        if (netSec <= 0) {
            val mesNet = row.mesNetProductionSec ?: 0
            if (mesNet <= 0) return "—"
            return (prod / (mesNet / 3600.0)).toInt().toString()
        }
        return (prod / (netSec / 3600.0)).toInt().toString()
    }

    fun compareForHistory(a: WeldingManagementRowDto, b: WeldingManagementRowDto): Int {
        val seqA = a.productionSequence ?: 0
        val seqB = b.productionSequence ?: 0
        if (seqA != seqB) return seqA.compareTo(seqB)
        return (a.id ?: 0).compareTo(b.id ?: 0)
    }

    fun operatorLabel(row: WeldingManagementRowDto, currentUserId: Int, currentUserLabel: String): String {
        val id = row.mesOperatorUserId ?: return "—"
        return if (id == currentUserId) {
            currentUserLabel.ifBlank { id.toString() }
        } else {
            id.toString()
        }
    }
}
