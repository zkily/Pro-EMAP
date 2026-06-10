package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class PlanInstructionRecordDto(
    val id: Int? = null,
    @Json(name = "plan_date") val planDate: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    val operator: String? = null,
    val quantity: Int? = null,
    @Json(name = "efficiency_rate") val efficiencyRate: Double? = null,
    @Json(name = "setup_time") val setupTime: Int? = null,
    @Json(name = "actual_production") val actualProduction: Int? = null,
    @Json(name = "actual_qty") val actualQty: Int? = null,
    val remarks: String? = null,
    @Json(name = "schedule_id") val scheduleId: String? = null,
    @Json(name = "planned_output_qty") val plannedOutputQty: Int? = null,
)

data class PlanInstructionDataEnvelope(
    val records: List<PlanInstructionRecordDto>? = null,
    val total: Int? = null,
)

data class PlanInstructionListResponse(
    val success: Boolean? = null,
    val data: PlanInstructionDataEnvelope? = null,
    val message: String? = null,
)

data class PlanInstructionNoteDto(
    val id: Int? = null,
    val content: String? = null,
    @Json(name = "is_done") val isDone: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
)

data class PlanInstructionNotesListData(
    val list: List<PlanInstructionNoteDto>? = null,
)

data class PlanInstructionNotesResponse(
    val success: Boolean? = null,
    val data: PlanInstructionNotesListData? = null,
    val message: String? = null,
)

data class PlanInstructionNoteBody(
    val content: String,
)

data class PlanInstructionNotePatchBody(
    @Json(name = "is_done") val isDone: Boolean,
)

data class PlanInstructionRemarksBody(
    val remarks: String,
    val id: Int? = null,
    @Json(name = "plan_date") val planDate: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
)

data class PlanInstructionEfficiencyUpdateBody(
    @Json(name = "startDate") val startDate: String,
)

data class LineCapacityDto(
    @Json(name = "work_date") val workDate: String? = null,
    @Json(name = "available_hours") val availableHours: Double? = null,
    @Json(name = "line_id") val lineId: Int? = null,
)

data class PlanBaselineComparisonDto(
    @Json(name = "baselinePlanTotal") val baselinePlanTotal: Double? = null,
    @Json(name = "currentPlanTotal") val currentPlanTotal: Double? = null,
    @Json(name = "planDifference") val planDifference: Double? = null,
    @Json(name = "currentActualTotal") val currentActualTotal: Double? = null,
    @Json(name = "actualDifference") val actualDifference: Double? = null,
    @Json(name = "planAchievementRatio") val planAchievementRatio: Double? = null,
    @Json(name = "baselineDailyAverage") val baselineDailyAverage: Double? = null,
    @Json(name = "productionStatus") val productionStatus: String? = null,
)

data class PlanBaselineComparisonItemDto(
    @Json(name = "plan_date") val planDate: String? = null,
    @Json(name = "current_actual") val currentActual: Double? = null,
)

data class PlanBaselineComparisonResultDto(
    val success: Boolean? = null,
    val summary: PlanBaselineComparisonDto? = null,
    val items: List<PlanBaselineComparisonItemDto>? = null,
    val data: PlanBaselineComparisonDto? = null,
)

data class MachineWorkTimeConfigDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "time_slot") val timeSlot: String? = null,
    @Json(name = "is_active") val isActive: Int? = null,
)

data class MachineWorkTimeConfigBody(
    @Json(name = "machine_cd") val machineCd: String,
    @Json(name = "machine_name") val machineName: String,
    @Json(name = "time_slot") val timeSlot: String,
    @Json(name = "is_active") val isActive: Boolean = true,
)
