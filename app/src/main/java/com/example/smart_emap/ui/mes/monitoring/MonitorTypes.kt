package com.example.smart_emap.ui.mes.monitoring

enum class MonitorRowStatus {
    Running,
    Paused,
    Break,
    Completed,
    Waiting,
    Idle,
}

data class MonitorMachineCard(
    val id: Int? = null,
    val name: String,
    val status: MonitorRowStatus,
    val operatorName: String = "",
    val currentProduct: String = "",
    val elapsedSec: Int = 0,
    val pausedSec: Int = 0,
    val breakSec: Int = 0,
    val commStale: Boolean = false,
    val lastCommAt: String? = null,
    val inspectorUserId: Int? = null,
    val nextProductCd: String? = null,
    val nextProductName: String? = null,
)

/** 検査モニタ：次製品指定パネル行 */
data class NextAssignPanelRow(
    val key: String,
    val inspectorUserId: Int,
    val inspectorName: String,
    val currentProductLabel: String,
    val status: MonitorRowStatus,
    val nextProductCd: String? = null,
    val nextProductName: String? = null,
    val elapsedSec: Int = 0,
    val pausedSec: Int = 0,
    val breakSec: Int = 0,
    val commStale: Boolean = false,
    val isFirstProduct: Boolean = false,
)

data class MonitorHistoryRow(
    val id: Int,
    val title: String,
    val operatorName: String,
    val actualQty: Int,
    val defectQty: Int,
    val startedAt: String?,
    val endedAt: String?,
    val elapsedSec: Int,
)

data class MonitorDefectListRow(
    val rowKey: String,
    val inspectorName: String,
    val status: MonitorRowStatus,
    val productName: String,
    val defectItemLabel: String,
    val defectQty: Int,
    val defectOccurredAt: String?,
)

data class MonitorInspectorEfficiencyRow(
    val rowKey: String,
    val inspectorUserId: Int?,
    val inspectorName: String,
    val sessionCount: Int,
    val sumActualQty: Int,
    val efficiencyPerHour: Int,
    val rank: Int,
)

data class MonitorOverallStats(
    val totalRunning: Int = 0,
    val totalPaused: Int = 0,
    val totalBreak: Int = 0,
    val totalCompleted: Int = 0,
    val totalWaiting: Int = 0,
    val totalActual: Int = 0,
    val totalDefect: Int = 0,
    val avgEfficiency: Int? = null,
    val defectRatePercent: Double? = null,
    val runningMachines: Int = 0,
    val totalMachines: Int = 0,
    val completionRate: Int = 0,
    val totalCommStale: Int = 0,
)

data class MonitorProcessSummary(
    val inProgressPlans: Int = 0,
    val pausedPlans: Int = 0,
    val breakPlans: Int = 0,
    val completedPlans: Int = 0,
    val waitingPlans: Int = 0,
    val totalPlans: Int = 0,
    val actualQty: Int = 0,
    val defectQtyTotal: Int = 0,
    val machines: List<MonitorMachineCard> = emptyList(),
    val historyRows: List<MonitorHistoryRow> = emptyList(),
    val defectListRows: List<MonitorDefectListRow> = emptyList(),
    val inspectorEfficiencyRows: List<MonitorInspectorEfficiencyRow> = emptyList(),
)
