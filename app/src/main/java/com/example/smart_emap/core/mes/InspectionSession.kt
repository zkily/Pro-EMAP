package com.example.smart_emap.core.mes

/** 検査 MES ローカル計測セッション（Web inspectionActualPersist と同等の最小実装） */
data class PlanSession(
    var wallStart: Long? = null,
    var wallEnd: Long? = null,
    var activeAccumMs: Long = 0,
    var pausedAccumMs: Long = 0,
    var breakAccumMs: Long = 0,
    var runningSliceStart: Long? = null,
    var pauseSliceStart: Long? = null,
    var breakSliceStart: Long? = null,
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

    fun isTimerOnBreak(sess: PlanSession): Boolean =
        sess.wallStart != null && sess.wallEnd == null && sess.breakSliceStart != null

    fun isProductionInProgress(sess: PlanSession): Boolean =
        sess.wallStart != null && sess.wallEnd == null

    fun timerPhase(sess: PlanSession): TimerPhase {
        if (sess.wallEnd != null) return TimerPhase.Ended
        if (sess.wallStart == null) return TimerPhase.Idle
        if (sess.pauseSliceStart != null) return TimerPhase.Paused
        if (sess.breakSliceStart != null) return TimerPhase.Break
        if (sess.runningSliceStart != null) return TimerPhase.Running
        return TimerPhase.Idle
    }

    fun readNetProductionMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        if (sess.wallStart == null) return 0
        var total = sess.activeAccumMs
        val runningStart = sess.runningSliceStart
        if (runningStart != null && sess.wallEnd == null) {
            total += (at - runningStart).coerceAtLeast(0)
        }
        return total.coerceAtLeast(0)
    }

    /**
     * 再開時：稼働時間を「生産開始〜現在の壁時計 − 一時停止 − 休憩」で補正し、running スライスを整合。
     * Web inspectionActualPersist.correctNetProductionFromWallClock と同等。
     */
    fun correctNetProductionFromWallClock(
        sess: PlanSession,
        wallStartMs: Long,
        now: Long = System.currentTimeMillis(),
    ) {
        val wallMs = (now - wallStartMs).coerceAtLeast(0)
        val pauseMs = readExplicitPausedAccumMs(sess, now)
        val breakMs = readExplicitBreakAccumMs(sess, now)
        sess.activeAccumMs = (wallMs - pauseMs - breakMs).coerceAtLeast(0)
        sess.runningSliceStart = now
        sess.pauseSliceStart = null
        sess.breakSliceStart = null
    }

    /** 明示的一時停止のみ（ボタン操作分） */
    fun readExplicitPausedAccumMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        if (sess.wallStart == null) return 0
        var total = sess.pausedAccumMs
        val pauseStart = sess.pauseSliceStart
        if (pauseStart != null) total += (at - pauseStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    /** 明示的休憩のみ（ボタン操作分） */
    fun readExplicitBreakAccumMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        if (sess.wallStart == null) return 0
        var total = sess.breakAccumMs
        val breakStart = sess.breakSliceStart
        if (breakStart != null) total += (at - breakStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    /**
     * 在産中タイマーの running / pause / break スライスを整合。
     * Web inspectionActualPersist.reconcileInProgressTimer と同等。
     */
    fun reconcileInProgressTimer(sess: PlanSession, now: Long = System.currentTimeMillis()) {
        if (sess.wallEnd != null || sess.wallStart == null) return
        val wallStart = sess.wallStart!!

        if (sess.pauseSliceStart != null) {
            if (sess.pauseSliceStart!! < wallStart) sess.pauseSliceStart = wallStart
            return
        }
        if (sess.breakSliceStart != null) {
            if (sess.breakSliceStart!! < wallStart) sess.breakSliceStart = wallStart
            return
        }
        if (sess.runningSliceStart != null) {
            if (sess.runningSliceStart!! < wallStart) sess.runningSliceStart = wallStart
            return
        }
        sess.runningSliceStart = now
        val maxNet = (now - wallStart).coerceAtLeast(0)
        if (sess.activeAccumMs > maxNet) sess.activeAccumMs = maxNet
    }

    fun readPausedAccumMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.pausedAccumMs
        val pauseStart = sess.pauseSliceStart
        if (pauseStart != null) total += (at - pauseStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    fun readBreakAccumMs(sess: PlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.breakAccumMs
        val breakStart = sess.breakSliceStart
        if (breakStart != null) total += (at - breakStart).coerceAtLeast(0)
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

    fun flushBreakSlice(sess: PlanSession, now: Long) {
        val start = sess.breakSliceStart ?: return
        sess.breakAccumMs += (now - start).coerceAtLeast(0)
        sess.breakSliceStart = null
    }

    fun formatDurationMs(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun hydrateFromRow(sess: PlanSession, row: InspectionRowSnapshot) {
        val started = MesDateTime.parseToMillis(row.mesProductionStartedAt)
        val ended = MesDateTime.parseToMillis(row.mesProductionEndedAt)
        val now = System.currentTimeMillis()

        if (started == null && ended == null) {
            sess.wallStart = null
            sess.wallEnd = null
            sess.activeAccumMs = 0
            sess.runningSliceStart = null
            sess.pausedAccumMs = 0
            sess.pauseSliceStart = null
            sess.breakAccumMs = 0
            sess.breakSliceStart = null
        } else if (ended != null) {
            sess.wallStart = started
            sess.wallEnd = ended
            sess.runningSliceStart = null
            sess.pauseSliceStart = null
            sess.breakSliceStart = null
            val netSec = row.mesNetProductionSec
            sess.activeAccumMs = when {
                netSec != null -> (netSec * 1000L).coerceAtLeast(0)
                started != null -> (ended - started).coerceAtLeast(0)
                else -> 0
            }
            sess.breakAccumMs = ((row.mesBreakSec ?: 0) * 1000L).coerceAtLeast(0)
            val stopSec = row.mesStopSec
            sess.pausedAccumMs = when {
                stopSec != null -> (stopSec * 1000L).coerceAtLeast(0)
                row.mesPausedAccumSec != null -> (row.mesPausedAccumSec * 1000L).coerceAtLeast(0)
                started != null -> {
                    val wallSpan = (ended - started).coerceAtLeast(0)
                    (wallSpan - sess.activeAccumMs - sess.breakAccumMs).coerceAtLeast(0)
                }
                else -> 0
            }
        } else if (started != null) {
            sess.wallStart = started
            sess.wallEnd = null
            val netSec = row.mesNetProductionSec
            val nsec = netSec?.coerceAtLeast(0)
            val stopFromRow = row.mesStopSec?.coerceAtLeast(0)
            val pausedFromRow = row.mesPausedAccumSec?.coerceAtLeast(0) ?: 0
            sess.activeAccumMs = ((nsec ?: 0) * 1000L).coerceAtLeast(0)
            sess.breakAccumMs = ((row.mesBreakSec ?: 0) * 1000L).coerceAtLeast(0)
            sess.breakSliceStart = null
            sess.pausedAccumMs = ((stopFromRow ?: pausedFromRow) * 1000L).coerceAtLeast(0)
            sess.pauseSliceStart = null
            when (row.mesProductionIsPaused) {
                1 -> {
                    sess.runningSliceStart = null
                    sess.pauseSliceStart = now
                    if (nsec == null) {
                        sess.pausedAccumMs = (now - started).coerceAtLeast(0)
                    }
                }
                2 -> {
                    sess.runningSliceStart = null
                    sess.breakSliceStart = now
                }
                else -> {
                    sess.runningSliceStart = now
                    if (nsec == null) {
                        sess.activeAccumMs = 0
                        sess.runningSliceStart = started
                    }
                }
            }
        }

        val parsed = row.mesDefectByItem.orEmpty()
        sess.defects.keys.forEach { k -> sess.defects[k] = parsed[k] ?: 0 }
        for ((k, v) in parsed) {
            if (k !in sess.defects) sess.defects[k] = v
        }
    }
}

enum class TimerPhase {
    Idle, Running, Paused, Break, Ended
}

data class InspectionRowSnapshot(
    val mesProductionStartedAt: String?,
    val mesProductionEndedAt: String?,
    val mesNetProductionSec: Int?,
    val mesPausedAccumSec: Int?,
    val mesBreakSec: Int? = null,
    val mesStopSec: Int? = null,
    val mesProductionIsPaused: Int?,
    val mesDefectByItem: Map<String, Int>?,
)
