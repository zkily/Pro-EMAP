package com.example.smart_emap.core.mes

/** 検査 MES ローカル計測セッション（Web inspectionActualPersist と同等の最小実装） */
data class PlanSession(
    var wallStart: Long? = null,
    var wallEnd: Long? = null,
    var activeAccumMs: Long = 0,
    var pausedAccumMs: Long = 0,
    var runningSliceStart: Long? = null,
    var pauseSliceStart: Long? = null,
    val defects: MutableMap<String, Int> = mutableMapOf(),
)

object InspectionSessionLogic {
    fun emptySession(defectKeys: Collection<String>): PlanSession {
        val defects = defectKeys.associateWith { 0 }.toMutableMap()
        return PlanSession(defects = defects)
    }

    fun isTimerRunning(sess: PlanSession): Boolean = sess.runningSliceStart != null

    fun isTimerPaused(sess: PlanSession): Boolean =
        sess.wallStart != null && sess.wallEnd == null && sess.pauseSliceStart != null

    fun isProductionInProgress(sess: PlanSession): Boolean =
        sess.wallStart != null && sess.wallEnd == null

    fun timerPhase(sess: PlanSession): TimerPhase {
        if (sess.wallEnd != null) return TimerPhase.Ended
        if (sess.wallStart == null) return TimerPhase.Idle
        if (sess.pauseSliceStart != null) return TimerPhase.Paused
        if (sess.runningSliceStart != null) return TimerPhase.Running
        return TimerPhase.Idle
    }

    fun readNetProductionMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.activeAccumMs
        val runningStart = sess.runningSliceStart
        if (runningStart != null) total += (at - runningStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    fun readPausedAccumMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.pausedAccumMs
        val pauseStart = sess.pauseSliceStart
        if (pauseStart != null) total += (at - pauseStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    fun flushRunningSlice(sess: PlanSession, now: Long) {
        val start = sess.runningSliceStart ?: return
        sess.activeAccumMs += (now - start).coerceAtLeast(0)
        sess.runningSliceStart = null
    }

    fun flushPauseSlice(sess: PlanSession, now: Long) {
        val start = sess.pauseSliceStart ?: return
        sess.pausedAccumMs += (now - start).coerceAtLeast(0)
        sess.pauseSliceStart = null
    }

    fun formatDurationMs(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun hydrateFromRow(sess: PlanSession, row: InspectionRowSnapshot) {
        val started = row.mesProductionStartedAt?.let { parseIsoToMillis(it) }
        val ended = row.mesProductionEndedAt?.let { parseIsoToMillis(it) }
        sess.wallStart = started
        sess.wallEnd = ended
        sess.activeAccumMs = ((row.mesNetProductionSec ?: 0) * 1000L).coerceAtLeast(0)
        sess.pausedAccumMs = ((row.mesPausedAccumSec ?: 0) * 1000L).coerceAtLeast(0)
        sess.runningSliceStart = when {
            ended != null || started == null -> null
            row.mesProductionIsPaused == 1 -> null
            else -> System.currentTimeMillis()
        }
        sess.pauseSliceStart = if (row.mesProductionIsPaused == 1 && ended == null && started != null) {
            System.currentTimeMillis()
        } else {
            null
        }
        val parsed = row.mesDefectByItem.orEmpty()
        sess.defects.keys.forEach { k -> sess.defects[k] = parsed[k] ?: 0 }
        for ((k, v) in parsed) {
            if (k !in sess.defects) sess.defects[k] = v
        }
    }

    private fun parseIsoToMillis(iso: String): Long? {
        return runCatching {
            java.time.Instant.parse(iso.trim()).toEpochMilli()
        }.getOrNull()
    }
}

enum class TimerPhase {
    Idle, Running, Paused, Ended
}

data class InspectionRowSnapshot(
    val mesProductionStartedAt: String?,
    val mesProductionEndedAt: String?,
    val mesNetProductionSec: Int?,
    val mesPausedAccumSec: Int?,
    val mesProductionIsPaused: Int?,
    val mesDefectByItem: Map<String, Int>?,
)
