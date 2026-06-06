package com.example.smart_emap.ui.mes.inspection

import com.example.smart_emap.data.model.InspectionManagementRowDto
import java.util.Locale

/** inspection_management 行の表示・集計（Web useInspectionMesCollection と同等） */
object InspectionManagementRowExt {

    fun historyDefectQty(row: InspectionManagementRowDto): Int {
        val stored = row.defectQty ?: 0
        if (stored > 0) return stored
        return row.mesDefectByItem?.values?.sumOf { it.coerceAtLeast(0) } ?: 0
    }

    fun formatHistoryProductionDay(row: InspectionManagementRowDto): String {
        val stored = row.productionDay?.trim()?.take(10).orEmpty()
        if (stored.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return stored
        val fromStart = HistoryRowFormat.parseIsoToMillis(row.mesProductionStartedAt)
        if (fromStart != null) {
            return HistoryRowFormat.productionDayFromMillis(fromStart)
        }
        return "—"
    }

    fun formatDefectRate(row: InspectionManagementRowDto): String {
        val prod = row.actualProductionQuantity ?: 0
        val defects = historyDefectQty(row)
        if (prod <= 0) return "—"
        return String.format(Locale.JAPAN, "%.1f%%", defects * 100.0 / prod)
    }

    fun formatEfficiencyRate(row: InspectionManagementRowDto): String {
        val prod = row.actualProductionQuantity ?: 0
        if (prod <= 0) return "—"
        val netSec = (HistoryRowFormat.rowWallElapsedSec(row) - HistoryRowFormat.rowPausedAccumSec(row))
            .coerceAtLeast(0)
        if (netSec <= 0) {
            val mesNet = row.mesNetProductionSec ?: 0
            if (mesNet <= 0) return "—"
            return (prod / (mesNet / 3600.0)).toInt().toString()
        }
        return (prod / (netSec / 3600.0)).toInt().toString()
    }

    fun compareForHistory(a: InspectionManagementRowDto, b: InspectionManagementRowDto): Int {
        val seqA = a.productionSequence ?: 0
        val seqB = b.productionSequence ?: 0
        if (seqA != seqB) return seqA.compareTo(seqB)
        return (a.id ?: 0).compareTo(b.id ?: 0)
    }

    fun inspectorLabel(row: InspectionManagementRowDto, currentUserId: Int, currentUserLabel: String): String {
        val id = row.mesInspectorUserId ?: return "—"
        return if (id == currentUserId) {
            currentUserLabel.ifBlank { id.toString() }
        } else {
            id.toString()
        }
    }
}
