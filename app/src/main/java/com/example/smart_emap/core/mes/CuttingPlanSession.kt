package com.example.smart_emap.core.mes

import com.example.smart_emap.data.model.CuttingManagementRowDto

data class CuttingPlanSession(
    var activeAccumMs: Long = 0,
    var runningSliceStart: Long? = null,
    var pausedAccumMs: Long = 0,
    var pauseSliceStart: Long? = null,
    var wallStart: Long? = null,
    var wallEnd: Long? = null,
    var operatorUserId: Int? = null,
    var setupTimeMin: Int? = null,
    var sawBladeExchangeMin: Int? = null,
    var repairMin: Int? = null,
)

object CuttingSessionLogic {
    fun emptySession(): CuttingPlanSession = CuttingPlanSession()

    fun isTimerRunning(sess: CuttingPlanSession): Boolean = sess.runningSliceStart != null

    fun isProductionInProgress(sess: CuttingPlanSession): Boolean =
        sess.wallStart != null && sess.wallEnd == null

    fun isTimerPaused(sess: CuttingPlanSession): Boolean =
        isProductionInProgress(sess) && sess.runningSliceStart == null

    fun timerPhase(sess: CuttingPlanSession): TimerPhase {
        if (sess.wallEnd != null) return TimerPhase.Ended
        if (sess.wallStart == null) return TimerPhase.Idle
        if (isTimerPaused(sess)) return TimerPhase.Paused
        if (isTimerRunning(sess)) return TimerPhase.Running
        return TimerPhase.Idle
    }

    fun readNetProductionMs(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.activeAccumMs
        val runningStart = sess.runningSliceStart
        if (runningStart != null) total += (at - runningStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    fun readPausedAccumMs(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Long {
        var total = sess.pausedAccumMs
        val pauseStart = sess.pauseSliceStart
        if (pauseStart != null) total += (at - pauseStart).coerceAtLeast(0)
        return total.coerceAtLeast(0)
    }

    fun flushRunningSlice(sess: CuttingPlanSession, now: Long) {
        val start = sess.runningSliceStart ?: return
        sess.activeAccumMs += (now - start).coerceAtLeast(0)
        sess.runningSliceStart = null
    }

    fun flushPauseSlice(sess: CuttingPlanSession, now: Long) {
        val start = sess.pauseSliceStart ?: return
        sess.pausedAccumMs += (now - start).coerceAtLeast(0)
        sess.pauseSliceStart = null
    }

    fun freezePausedAccumMs(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Long {
        if (sess.wallStart == null) {
            sess.pausedAccumMs = 0
            sess.pauseSliceStart = null
            return 0
        }
        var ms = sess.pausedAccumMs
        if (sess.pauseSliceStart != null) {
            ms += (at - sess.pauseSliceStart!!).coerceAtLeast(0)
            sess.pauseSliceStart = null
        }
        val wallEnd = sess.wallEnd ?: at
        val wallSpan = (wallEnd - sess.wallStart!!).coerceAtLeast(0)
        var netMs = sess.activeAccumMs
        if (sess.runningSliceStart != null && sess.wallEnd == null) {
            netMs += (at - sess.runningSliceStart!!).coerceAtLeast(0)
        }
        val derived = (wallSpan - netMs.coerceAtMost(wallSpan)).coerceAtLeast(0)
        val total = maxOf(ms, derived)
        sess.pausedAccumMs = total
        return total
    }

    fun reconcileInProgressTimer(sess: CuttingPlanSession, now: Long = System.currentTimeMillis()) {
        if (sess.wallEnd != null || sess.wallStart == null) return
        if (sess.runningSliceStart != null) {
            if (sess.runningSliceStart!! < sess.wallStart!!) {
                sess.runningSliceStart = sess.wallStart
            }
            return
        }
        val maxNet = (now - sess.wallStart!!).coerceAtLeast(0)
        if (sess.activeAccumMs > maxNet) sess.activeAccumMs = maxNet
    }

    fun formatDurationMs(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun netProductionSeconds(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Int =
        (readNetProductionMs(sess, at) / 1000).toInt().coerceAtLeast(0)

    fun pausedAccumSeconds(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Int =
        (readPausedAccumMs(sess, at) / 1000).toInt().coerceAtLeast(0)

    fun mesMinuteFromRow(value: Int?): Int? {
        if (value == null) return null
        return value.coerceAtLeast(0)
    }

    fun hydrateFromRow(sess: CuttingPlanSession, row: CuttingManagementRowDto) {
        val ws = MesDateTime.parseToMillis(row.mesProductionStartedAt)
        val we = MesDateTime.parseToMillis(row.mesProductionEndedAt)
        val netSec = row.mesNetProductionSec
        val pausedSec = row.mesPausedAccumSec
        val pausedFlag = row.mesProductionIsPaused

        if (ws != null) sess.wallStart = ws

        if (we != null) {
            sess.wallEnd = we
            sess.runningSliceStart = null
            sess.pauseSliceStart = null
            sess.activeAccumMs = if (netSec != null) {
                (netSec * 1000L).coerceAtLeast(0)
            } else if (ws != null) {
                (we - ws).coerceAtLeast(0)
            } else {
                0
            }
            sess.pausedAccumMs = if (pausedSec != null) {
                (pausedSec * 1000L).coerceAtLeast(0)
            } else if (ws != null) {
                ((we - ws) - sess.activeAccumMs).coerceAtLeast(0)
            } else {
                0
            }
            return
        }

        if (ws == null) return
        val now = System.currentTimeMillis()
        val nsec = netSec?.coerceAtLeast(0)
        val psec = pausedSec?.coerceAtLeast(0) ?: 0
        val isPaused = when (pausedFlag) {
            1 -> true
            0 -> false
            else -> nsec != null
        }

        sess.activeAccumMs = (nsec ?: 0) * 1000L
        sess.pausedAccumMs = psec * 1000L
        sess.pauseSliceStart = null

        if (isPaused) {
            sess.runningSliceStart = null
            sess.pauseSliceStart = now
            if (nsec == null) {
                sess.pausedAccumMs = (now - ws).coerceAtLeast(0)
            }
            return
        }

        sess.runningSliceStart = now
        if (nsec == null) {
            sess.activeAccumMs = 0
            sess.runningSliceStart = ws
        }
    }

    fun operationDisplayMs(sess: CuttingPlanSession, at: Long = System.currentTimeMillis()): Long {
        if (sess.wallStart == null) return 0
        if (sess.wallEnd != null) {
            if (sess.activeAccumMs > 0) return sess.activeAccumMs
            return (sess.wallEnd!! - sess.wallStart!!).coerceAtLeast(0)
        }
        return readNetProductionMs(sess, at)
    }
}

data class PersistedCuttingPlanSession(
    val activeAccumMs: Long = 0,
    val runningSliceStart: Long? = null,
    val pausedAccumMs: Long = 0,
    val pauseSliceStart: Long? = null,
    val wallStart: Long? = null,
    val wallEnd: Long? = null,
    val operatorUserId: Int? = null,
    val setupTimeMin: Int? = null,
    val sawBladeExchangeMin: Int? = null,
    val repairMin: Int? = null,
)

fun CuttingPlanSession.toPersisted(): PersistedCuttingPlanSession = PersistedCuttingPlanSession(
    activeAccumMs = activeAccumMs,
    runningSliceStart = runningSliceStart,
    pausedAccumMs = pausedAccumMs,
    pauseSliceStart = pauseSliceStart,
    wallStart = wallStart,
    wallEnd = wallEnd,
    operatorUserId = operatorUserId,
    setupTimeMin = setupTimeMin,
    sawBladeExchangeMin = sawBladeExchangeMin,
    repairMin = repairMin,
)

fun PersistedCuttingPlanSession.applyTo(sess: CuttingPlanSession) {
    sess.activeAccumMs = activeAccumMs
    sess.runningSliceStart = runningSliceStart
    sess.pausedAccumMs = pausedAccumMs
    sess.pauseSliceStart = pauseSliceStart
    sess.wallStart = wallStart
    sess.wallEnd = wallEnd
    sess.operatorUserId = operatorUserId
    sess.setupTimeMin = setupTimeMin
    sess.sawBladeExchangeMin = sawBladeExchangeMin
    sess.repairMin = repairMin
}
