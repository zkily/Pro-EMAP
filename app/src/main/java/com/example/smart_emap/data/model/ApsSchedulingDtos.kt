package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class ApsProductionLineDto(
    val id: Int,
    @Json(name = "line_code") val lineCode: String,
    @Json(name = "line_name") val lineName: String? = null,
    @Json(name = "default_work_hours") val defaultWorkHours: Double = 0.0,
    @Json(name = "is_active") val isActive: Boolean = true,
)

data class DailyUpstreamTintSegDto(
    @Json(name = "in_cutting") val inCutting: Int = 0,
    @Json(name = "in_instruction") val inInstruction: Int = 0,
    @Json(name = "only_planned") val onlyPlanned: Int = 0,
)

data class ScheduleGridRowDto(
    val id: Int,
    @Json(name = "order_no") val orderNo: Int? = null,
    @Json(name = "item_name") val itemName: String,
    @Json(name = "material_shortage") val materialShortage: Boolean = false,
    @Json(name = "lot_qty") val lotQty: Int = 0,
    @Json(name = "planned_batch_count") val plannedBatchCount: Int = 0,
    @Json(name = "lot_size_snapshot") val lotSizeSnapshot: Int = 0,
    @Json(name = "planned_process_qty") val plannedProcessQty: Int = 0,
    @Json(name = "prev_month_carryover") val prevMonthCarryover: Int = 0,
    @Json(name = "due_date") val dueDate: String? = null,
    @Json(name = "material_date") val materialDate: String? = null,
    @Json(name = "setup_time") val setupTime: Int = 0,
    val efficiency: Double = 0.0,
    @Json(name = "efficiency_rate") val efficiencyRate: Double? = null,
    @Json(name = "daily_capacity") val dailyCapacity: Int = 0,
    @Json(name = "planned_output_qty") val plannedOutputQty: Int = 0,
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    @Json(name = "completion_rate") val completionRate: Double? = null,
    val status: String = "",
    val daily: Map<String, Int> = emptyMap(),
    @Json(name = "actual_daily") val actualDaily: Map<String, Int> = emptyMap(),
    @Json(name = "remaining_daily") val remainingDaily: Map<String, Int> = emptyMap(),
    @Json(name = "defect_daily") val defectDaily: Map<String, Int> = emptyMap(),
    @Json(name = "has_aps_batch_plans") val hasApsBatchPlans: Boolean = false,
    @Json(name = "daily_upstream_tint") val dailyUpstreamTint: Map<String, DailyUpstreamTintSegDto> = emptyMap(),
)

data class LineGridBlockDto(
    @Json(name = "line_id") val lineId: Int,
    @Json(name = "line_code") val lineCode: String,
    @Json(name = "default_work_hours") val defaultWorkHours: Double = 0.0,
    val calendar: Map<String, Double> = emptyMap(),
    val rows: List<ScheduleGridRowDto> = emptyList(),
    @Json(name = "daily_totals") val dailyTotals: Map<String, Int> = emptyMap(),
    @Json(name = "sum_planned_process_qty") val sumPlannedProcessQty: Int = 0,
    @Json(name = "sum_planned_output_qty") val sumPlannedOutputQty: Int = 0,
    @Json(name = "completion_rate") val completionRate: Double? = null,
)

data class SchedulingGridResponseDto(
    val dates: List<String> = emptyList(),
    val blocks: List<LineGridBlockDto> = emptyList(),
)
