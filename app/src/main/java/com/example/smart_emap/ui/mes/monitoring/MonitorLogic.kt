package com.example.smart_emap.ui.mes.monitoring

import com.example.smart_emap.core.mes.MesDateTime
import com.example.smart_emap.data.model.InspectionManagementRowDto
import com.example.smart_emap.data.model.MachineDto
import com.example.smart_emap.data.model.ProcessDefectItemDto
import com.example.smart_emap.data.model.UserListItemDto
import com.example.smart_emap.data.model.WeldingManagementRowDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object MonitorLogic {
    private val JST = ZoneId.of("Asia/Tokyo")
    private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(JST)
    private val monitorDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withZone(JST)
    private val historyTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(JST)

    fun formatClock(nowMs: Long = System.currentTimeMillis()): String =
        clockFormatter.format(Instant.ofEpochMilli(nowMs))

    /** 検査モニタヘッダー用（Web と同形式の日時表示） */
    fun formatMonitorDateTime(nowMs: Long = System.currentTimeMillis()): String =
        monitorDateTimeFormatter.format(Instant.ofEpochMilli(nowMs))

    fun formatMonitorDateTime(iso: String?): String {
        val ms = MesDateTime.parseToMillis(iso) ?: return "—"
        return monitorDateTimeFormatter.format(Instant.ofEpochMilli(ms))
    }

    /** 検査実績収集端 checkpoint（5s）+ モニタ刷新（15s）を考慮 */
    const val INSPECTION_MONITOR_COMM_STALE_MS = 60_000L

    fun isInspectionClientCommStale(
        row: InspectionManagementRowDto,
        status: MonitorRowStatus,
        tickNow: Long,
    ): Boolean {
        if (status != MonitorRowStatus.Running && status != MonitorRowStatus.Paused && status != MonitorRowStatus.Break) {
            return false
        }
        if (row.mesClientInstanceId.isNullOrBlank()) return false
        if (row.mesProductionStartedAt.isNullOrBlank()) return false
        val updatedMs = MesDateTime.parseToMillis(row.updatedAt) ?: return false
        return tickNow - updatedMs > INSPECTION_MONITOR_COMM_STALE_MS
    }

    fun formatDuration(sec: Int): String {
        if (sec <= 0) return "--:--"
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return if (h > 0) {
            "${h}h ${m.toString().padStart(2, '0')}m"
        } else {
            "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        }
    }

    fun formatHistoryTime(iso: String?): String {
        val ms = MesDateTime.parseToMillis(iso) ?: return "—"
        return historyTimeFormatter.format(Instant.ofEpochMilli(ms))
    }

    fun formatHistoryTimeOnly(iso: String?): String = formatHistoryTime(iso)

    fun fmtMonitorEfficiency(value: Int?): String = value?.toString() ?: "—"

    fun fmtMonitorDefectRate(rate: Double?): String =
        if (rate == null) "—" else "${rate}%"

    fun isMonitorEfficiencyOutOfRange(value: Int?): Boolean {
        if (value == null) return false
        return value < 200 || value > 800
    }

    fun statusLabel(status: MonitorRowStatus): String = when (status) {
        MonitorRowStatus.Running -> "稼働中"
        MonitorRowStatus.Paused -> "一時停止"
        MonitorRowStatus.Break -> "休憩中"
        MonitorRowStatus.Waiting, MonitorRowStatus.Idle -> "待機中"
        MonitorRowStatus.Completed -> "完了"
    }

    private fun isRowMesActive(
        completed: Int?,
        startedAt: String?,
        endedAt: String?,
    ): Boolean {
        if ((completed ?: 0) == 1) return false
        if (startedAt.isNullOrBlank()) return false
        return endedAt.isNullOrBlank()
    }

    fun classifyWeldingRow(row: WeldingManagementRowDto): MonitorRowStatus {
        if ((row.productionCompletedCheck ?: 0) == 1) return MonitorRowStatus.Completed
        if (isRowMesActive(row.productionCompletedCheck, row.mesProductionStartedAt, row.mesProductionEndedAt)) {
            return if ((row.mesProductionIsPaused ?: 0) == 1) MonitorRowStatus.Paused else MonitorRowStatus.Running
        }
        if (!row.mesProductionEndedAt.isNullOrBlank()) return MonitorRowStatus.Completed
        return MonitorRowStatus.Waiting
    }

    fun classifyInspectionRow(row: InspectionManagementRowDto): MonitorRowStatus {
        if ((row.productionCompletedCheck ?: 0) == 1) return MonitorRowStatus.Completed
        if (isRowMesActive(row.productionCompletedCheck, row.mesProductionStartedAt, row.mesProductionEndedAt)) {
            return when (row.mesProductionIsPaused ?: 0) {
                2 -> MonitorRowStatus.Break
                1 -> MonitorRowStatus.Paused
                else -> MonitorRowStatus.Running
            }
        }
        if (!row.mesProductionEndedAt.isNullOrBlank()) return MonitorRowStatus.Completed
        return MonitorRowStatus.Waiting
    }

    private fun elapsedSeconds(
        netSec: Int?,
        startedAt: String?,
        endedAt: String?,
        tickNow: Long,
    ): Int {
        val startedMs = MesDateTime.parseToMillis(startedAt)
        if (startedMs == null) {
            val net = netSec ?: 0
            return net.coerceAtLeast(0)
        }
        val endedMs = MesDateTime.parseToMillis(endedAt)
        if (endedMs == null) {
            return ((tickNow - startedMs) / 1000).toInt().coerceAtLeast(0)
        }
        val net = netSec
        if (net != null && net >= 0) return net
        return ((endedMs - startedMs) / 1000).toInt().coerceAtLeast(0)
    }

    private fun rowNetSecForEfficiency(row: InspectionManagementRowDto, tickNow: Long): Int {
        val net = row.mesNetProductionSec
        if (net != null && net >= 0 && (row.productionCompletedCheck ?: 0) == 1) return net
        if (net != null && net > 0) return net
        return elapsedSeconds(net, row.mesProductionStartedAt, row.mesProductionEndedAt, tickNow)
    }

    fun operatorNameForInspection(
        row: InspectionManagementRowDto,
        users: Map<Int, UserListItemDto>,
    ): String {
        val fromRow = (row.mesInspectorName ?: row.mesInspectorUsername ?: "").trim()
        if (fromRow.isNotEmpty()) return fromRow
        val id = row.mesInspectorUserId ?: return ""
        return users[id]?.displayLabel().orEmpty()
    }

    private fun operatorNameForWelding(
        row: WeldingManagementRowDto,
        users: Map<Int, UserListItemDto>,
    ): String {
        val id = row.mesOperatorUserId ?: return ""
        return users[id]?.displayLabel().orEmpty()
    }

    fun inspectionCardName(row: InspectionManagementRowDto): String {
        val cd = row.productCd?.trim().orEmpty()
        val name = row.productName?.trim().orEmpty()
        return when {
            cd.isNotEmpty() && name.isNotEmpty() -> "$cd · $name"
            cd.isNotEmpty() -> cd
            name.isNotEmpty() -> name
            else -> "検査ライン"
        }
    }

    fun buildInspectionSummary(
        rows: List<InspectionManagementRowDto>,
        users: Map<Int, UserListItemDto>,
        defectItems: List<ProcessDefectItemDto>,
        tickNow: Long,
        nextAssignments: List<com.example.smart_emap.data.model.InspectionNextAssignmentDto> = emptyList(),
    ): MonitorProcessSummary {
        val assignmentByInspector = nextAssignments
            .mapNotNull { a ->
                val id = a.inspectorUserId ?: return@mapNotNull null
                id to a
            }
            .toMap()
        val machines = mutableListOf<MonitorMachineCard>()
        for (row in rows) {
            val status = classifyInspectionRow(row)
            if (status != MonitorRowStatus.Running && status != MonitorRowStatus.Paused && status != MonitorRowStatus.Break) {
                continue
            }
            val inspectorId = row.mesInspectorUserId
            val assignment = inspectorId?.let { assignmentByInspector[it] }
            val breakSec = if (status == MonitorRowStatus.Break) {
                (row.mesBreakSec ?: 0).coerceAtLeast(0)
            } else {
                0
            }
            val pausedSec = if (status == MonitorRowStatus.Paused) {
                (row.mesPausedAccumSec ?: row.mesStopSec ?: 0).coerceAtLeast(0)
            } else {
                0
            }
            val commStale = isInspectionClientCommStale(row, status, tickNow)
            machines += MonitorMachineCard(
                id = row.id,
                name = inspectionCardName(row),
                status = status,
                operatorName = operatorNameForInspection(row, users),
                currentProduct = row.productName.orEmpty(),
                elapsedSec = elapsedSeconds(
                    row.mesNetProductionSec,
                    row.mesProductionStartedAt,
                    row.mesProductionEndedAt,
                    tickNow,
                ),
                pausedSec = pausedSec,
                breakSec = breakSec,
                commStale = commStale,
                lastCommAt = if (commStale) row.updatedAt else null,
                inspectorUserId = inspectorId,
                nextProductCd = assignment?.nextProductCd,
                nextProductName = assignment?.nextProductName,
            )
        }
        machines.sortBy { it.name }

        val defectLabelMap = defectItems.associate { it.defectCd to it.defectName }
        val defectRows = buildInspectionDefectListRows(rows, users, defectLabelMap)
        val efficiencyRows = buildInspectionInspectorEfficiencyRows(rows, users, tickNow)

        val history = rows
            .filter { (it.productionCompletedCheck ?: 0) == 1 }
            .sortedWith(
                compareByDescending<InspectionManagementRowDto> {
                    MesDateTime.parseToMillis(it.mesProductionEndedAt) ?: 0L
                }.thenByDescending { it.id ?: 0 },
            )
            .map { row ->
                MonitorHistoryRow(
                    id = row.id ?: 0,
                    title = row.productName?.trim().orEmpty().ifEmpty { "—" },
                    operatorName = operatorNameForInspection(row, users).ifEmpty { "—" },
                    actualQty = row.actualProductionQuantity ?: 0,
                    defectQty = row.defectQty ?: 0,
                    startedAt = row.mesProductionStartedAt,
                    endedAt = row.mesProductionEndedAt,
                    elapsedSec = elapsedSeconds(
                        row.mesNetProductionSec,
                        row.mesProductionStartedAt,
                        row.mesProductionEndedAt,
                        tickNow,
                    ),
                )
            }

        val inProgress = rows.count { classifyInspectionRow(it) == MonitorRowStatus.Running }
        val paused = rows.count { classifyInspectionRow(it) == MonitorRowStatus.Paused }
        val onBreak = rows.count { classifyInspectionRow(it) == MonitorRowStatus.Break }
        val completed = rows.count { classifyInspectionRow(it) == MonitorRowStatus.Completed }
        val waiting = rows.count { classifyInspectionRow(it) == MonitorRowStatus.Waiting }
        val actualQty = rows.sumOf { it.actualProductionQuantity ?: 0 }
        val defectQtyTotal = rows.sumOf { it.defectQty ?: 0 }

        return MonitorProcessSummary(
            inProgressPlans = inProgress,
            pausedPlans = paused,
            breakPlans = onBreak,
            completedPlans = completed,
            waitingPlans = waiting,
            totalPlans = rows.size,
            actualQty = actualQty,
            defectQtyTotal = defectQtyTotal,
            machines = machines,
            historyRows = history,
            defectListRows = defectRows,
            inspectorEfficiencyRows = efficiencyRows,
        )
    }

    fun computeCompletedInspectorAvgEfficiency(
        rows: List<InspectionManagementRowDto>,
        users: Map<Int, UserListItemDto>,
    ): Int? {
        val completed = rows.filter { (it.productionCompletedCheck ?: 0) == 1 }
        val effRows = buildInspectionInspectorEfficiencyRows(completed, users, System.currentTimeMillis())
        val sumActual = effRows.sumOf { it.sumActualQty }
        val sumNetSec = completed.sumOf { rowNetSecForEfficiency(it, System.currentTimeMillis()) }
        return efficiencyPerHourFromTotals(sumActual, sumNetSec)
    }

    private fun efficiencyPerHourFromTotals(sumActualQty: Int, sumNetSec: Int): Int? {
        if (sumActualQty <= 0 || sumNetSec <= 0) return null
        return (sumActualQty / (sumNetSec / 3600.0)).roundToInt()
    }

    fun buildInspectionInspectorEfficiencyRows(
        rows: List<InspectionManagementRowDto>,
        users: Map<Int, UserListItemDto>,
        tickNow: Long,
    ): List<MonitorInspectorEfficiencyRow> {
        data class Bucket(
            val inspectorUserId: Int?,
            val inspectorName: String,
            var sessionCount: Int = 0,
            var sumActualQty: Int = 0,
            var sumNetSec: Int = 0,
        )
        val map = linkedMapOf<String, Bucket>()
        for (row in rows) {
            val actual = row.actualProductionQuantity ?: 0
            val netSec = rowNetSecForEfficiency(row, tickNow)
            if (actual <= 0 && netSec <= 0) continue
            val inspId = row.mesInspectorUserId
            val inspectorName = operatorNameForInspection(row, users).ifEmpty { "—" }
            val key = inspId?.toString() ?: "name:$inspectorName"
            val bucket = map.getOrPut(key) {
                Bucket(inspectorUserId = inspId, inspectorName = inspectorName)
            }
            bucket.sessionCount += 1
            bucket.sumActualQty += actual
            bucket.sumNetSec += netSec
        }
        val list = map.map { (key, bucket) ->
            MonitorInspectorEfficiencyRow(
                rowKey = key,
                inspectorUserId = bucket.inspectorUserId,
                inspectorName = bucket.inspectorName,
                sessionCount = bucket.sessionCount,
                sumActualQty = bucket.sumActualQty,
                efficiencyPerHour = efficiencyPerHourFromTotals(bucket.sumActualQty, bucket.sumNetSec) ?: 0,
                rank = 0,
            )
        }.filter { it.efficiencyPerHour > 0 }
            .sortedByDescending { it.efficiencyPerHour }
        return list.mapIndexed { index, row -> row.copy(rank = index + 1) }
    }

    private fun buildInspectionDefectListRows(
        rows: List<InspectionManagementRowDto>,
        users: Map<Int, UserListItemDto>,
        defectLabelMap: Map<String, String>,
    ): List<MonitorDefectListRow> {
        val list = mutableListOf<MonitorDefectListRow>()
        for (row in rows) {
            val status = classifyInspectionRow(row)
            val planId = row.id ?: continue
            val inspector = operatorNameForInspection(row, users).ifEmpty { "—" }
            val productName = row.productName?.trim().orEmpty().ifEmpty { "—" }
            val fallbackAt = if (status == MonitorRowStatus.Completed) {
                row.mesProductionEndedAt ?: row.updatedAt
            } else {
                row.updatedAt
            }
            val defects = row.mesDefectByItem.orEmpty()
            val seen = mutableSetOf<String>()
            for (item in defectLabelMap.keys) {
                val qty = defects[item] ?: 0
                if (qty <= 0) continue
                seen += item
                list += MonitorDefectListRow(
                    rowKey = "$planId-$item",
                    inspectorName = inspector,
                    status = status,
                    productName = productName,
                    defectItemLabel = defectLabelMap[item] ?: item,
                    defectQty = qty,
                    defectOccurredAt = fallbackAt,
                )
            }
            for ((cd, qty) in defects) {
                if (cd in seen || qty <= 0) continue
                list += MonitorDefectListRow(
                    rowKey = "$planId-$cd",
                    inspectorName = inspector,
                    status = status,
                    productName = productName,
                    defectItemLabel = defectLabelMap[cd] ?: cd,
                    defectQty = qty,
                    defectOccurredAt = fallbackAt,
                )
            }
        }
        return list.sortedWith(
            compareByDescending<MonitorDefectListRow> {
                MesDateTime.parseToMillis(it.defectOccurredAt) ?: 0L
            }.thenBy { it.productName }
                .thenBy { it.defectItemLabel },
        )
    }

    fun buildWeldingSummary(
        rows: List<WeldingManagementRowDto>,
        machines: List<MachineDto>,
        users: Map<Int, UserListItemDto>,
        tickNow: Long,
    ): MonitorProcessSummary {
        val machineNames = machines.mapNotNull { dto ->
            dto.machineName?.trim()?.takeIf { it.isNotEmpty() }
                ?: dto.machineCd?.trim()?.takeIf { it.isNotEmpty() }
        }.toMutableSet()
        rows.mapNotNull { it.weldingMachine?.trim()?.takeIf { it.isNotEmpty() } }.forEach { machineNames += it }

        val cards = mutableListOf<MonitorMachineCard>()
        for (name in machineNames.sorted()) {
            val mRows = rows.filter { (it.weldingMachine ?: "").trim() == name }
            val running = mRows.firstOrNull { classifyWeldingRow(it) == MonitorRowStatus.Running }
            val paused = mRows.firstOrNull { classifyWeldingRow(it) == MonitorRowStatus.Paused }
            val active = running ?: paused
            val status = when {
                running != null -> MonitorRowStatus.Running
                paused != null -> MonitorRowStatus.Paused
                else -> MonitorRowStatus.Idle
            }
            cards += MonitorMachineCard(
                name = name,
                status = status,
                operatorName = active?.let { operatorNameForWelding(it, users) }.orEmpty(),
                currentProduct = active?.productName.orEmpty(),
                elapsedSec = active?.let {
                    elapsedSeconds(it.mesNetProductionSec, it.mesProductionStartedAt, it.mesProductionEndedAt, tickNow)
                } ?: 0,
                pausedSec = if (status == MonitorRowStatus.Paused) {
                    (active?.mesPausedAccumSec ?: 0).coerceAtLeast(0)
                } else {
                    0
                },
            )
        }

        val unassigned = rows.filter { it.weldingMachine.isNullOrBlank() }
        if (unassigned.isNotEmpty()) {
            val running = unassigned.firstOrNull { classifyWeldingRow(it) == MonitorRowStatus.Running }
            val paused = unassigned.firstOrNull { classifyWeldingRow(it) == MonitorRowStatus.Paused }
            val active = running ?: paused
            val status = when {
                running != null -> MonitorRowStatus.Running
                paused != null -> MonitorRowStatus.Paused
                else -> MonitorRowStatus.Idle
            }
            cards += MonitorMachineCard(
                name = "未割当",
                status = status,
                operatorName = active?.let { operatorNameForWelding(it, users) }.orEmpty(),
                currentProduct = active?.productName.orEmpty(),
                elapsedSec = active?.let {
                    elapsedSeconds(it.mesNetProductionSec, it.mesProductionStartedAt, it.mesProductionEndedAt, tickNow)
                } ?: 0,
                pausedSec = if (status == MonitorRowStatus.Paused) {
                    (active?.mesPausedAccumSec ?: 0).coerceAtLeast(0)
                } else {
                    0
                },
            )
        }

        val history = rows
            .filter { (it.productionCompletedCheck ?: 0) == 1 }
            .sortedWith(
                compareByDescending<WeldingManagementRowDto> {
                    MesDateTime.parseToMillis(it.mesProductionEndedAt) ?: 0L
                }.thenByDescending { it.id ?: 0 },
            )
            .map { row ->
                val machine = row.weldingMachine?.trim().orEmpty()
                val product = row.productName?.trim().orEmpty().ifEmpty { "—" }
                val title = if (machine.isNotEmpty()) "$machine · $product" else product
                MonitorHistoryRow(
                    id = row.id ?: 0,
                    title = title,
                    operatorName = operatorNameForWelding(row, users).ifEmpty { "—" },
                    actualQty = row.actualProductionQuantity ?: 0,
                    defectQty = row.defectQty ?: 0,
                    startedAt = row.mesProductionStartedAt,
                    endedAt = row.mesProductionEndedAt,
                    elapsedSec = elapsedSeconds(
                        row.mesNetProductionSec,
                        row.mesProductionStartedAt,
                        row.mesProductionEndedAt,
                        tickNow,
                    ),
                )
            }

        val inProgress = rows.count { classifyWeldingRow(it) == MonitorRowStatus.Running }
        val paused = rows.count { classifyWeldingRow(it) == MonitorRowStatus.Paused }
        val completed = rows.count { classifyWeldingRow(it) == MonitorRowStatus.Completed }

        return MonitorProcessSummary(
            inProgressPlans = inProgress,
            pausedPlans = paused,
            breakPlans = 0,
            completedPlans = completed,
            waitingPlans = rows.size - completed - inProgress - paused,
            totalPlans = rows.size,
            machines = cards,
            historyRows = history,
            defectListRows = emptyList(),
        )
    }

    fun buildOverallStats(
        processKey: MonitorProcessKey,
        summary: MonitorProcessSummary,
        cachedAvgEfficiency: Int? = null,
    ): MonitorOverallStats {
        val runningMachines = summary.machines.count { it.status == MonitorRowStatus.Running }
        if (processKey == MonitorProcessKey.INSPECTION) {
            val defectRate = if (summary.actualQty > 0) {
                (summary.defectQtyTotal * 1000.0 / summary.actualQty).roundToInt() / 10.0
            } else {
                null
            }
            return MonitorOverallStats(
                totalRunning = summary.inProgressPlans,
                totalPaused = summary.pausedPlans,
                totalBreak = summary.breakPlans,
                totalCompleted = summary.completedPlans,
                totalWaiting = summary.waitingPlans,
                totalActual = summary.actualQty,
                totalDefect = summary.defectQtyTotal,
                avgEfficiency = cachedAvgEfficiency,
                defectRatePercent = defectRate,
                runningMachines = runningMachines,
                totalMachines = summary.machines.size,
                totalCommStale = summary.machines.count { it.commStale },
            )
        }
        val waiting = summary.totalPlans - summary.completedPlans - summary.inProgressPlans - summary.pausedPlans
        val completionRate = if (summary.totalPlans > 0) {
            ((summary.completedPlans * 100.0) / summary.totalPlans).toInt()
        } else {
            0
        }
        val totalDefect = summary.historyRows.sumOf { it.defectQty }
        return MonitorOverallStats(
            totalRunning = summary.inProgressPlans,
            totalPaused = summary.pausedPlans,
            totalBreak = summary.breakPlans,
            totalCompleted = summary.completedPlans,
            totalWaiting = waiting.coerceAtLeast(0),
            totalDefect = totalDefect,
            runningMachines = runningMachines,
            totalMachines = summary.machines.size,
            completionRate = completionRate,
        )
    }

    fun historyProductionQtyTotal(rows: List<MonitorHistoryRow>): Int =
        rows.sumOf { it.actualQty }

    fun buildNextAssignPanelRows(
        machines: List<MonitorMachineCard>,
        nextAssignments: List<com.example.smart_emap.data.model.InspectionNextAssignmentDto>,
        inspectorNameById: Map<Int, String>,
    ): List<NextAssignPanelRow> {
        val activeInspectorIds = mutableSetOf<Int>()
        val rows = mutableListOf<NextAssignPanelRow>()
        for (machine in machines) {
            if (
                machine.status != MonitorRowStatus.Running &&
                machine.status != MonitorRowStatus.Paused &&
                machine.status != MonitorRowStatus.Break
            ) {
                continue
            }
            val inspectorUserId = machine.inspectorUserId ?: continue
            activeInspectorIds += inspectorUserId
            rows += NextAssignPanelRow(
                key = "active-$inspectorUserId",
                inspectorUserId = inspectorUserId,
                inspectorName = machine.operatorName.ifBlank { inspectorNameById[inspectorUserId] ?: inspectorUserId.toString() },
                currentProductLabel = machine.name,
                status = machine.status,
                nextProductCd = machine.nextProductCd,
                nextProductName = machine.nextProductName,
                elapsedSec = machine.elapsedSec,
                pausedSec = machine.pausedSec,
                breakSec = machine.breakSec,
                commStale = machine.commStale,
                isFirstProduct = false,
            )
        }
        for (assignment in nextAssignments) {
            val uid = assignment.inspectorUserId ?: continue
            if (activeInspectorIds.contains(uid)) continue
            val inspectorName = assignment.inspectorName?.trim().orEmpty()
                .ifBlank { inspectorNameById[uid] ?: assignment.inspectorUsername?.trim().orEmpty() }
                .ifBlank { uid.toString() }
            rows += NextAssignPanelRow(
                key = "idle-$uid",
                inspectorUserId = uid,
                inspectorName = inspectorName,
                currentProductLabel = "—",
                status = MonitorRowStatus.Idle,
                nextProductCd = assignment.nextProductCd,
                nextProductName = assignment.nextProductName,
                isFirstProduct = true,
            )
        }
        return rows.sortedWith(
            compareBy<NextAssignPanelRow> { it.isFirstProduct }.thenBy { it.inspectorName },
        )
    }
}
