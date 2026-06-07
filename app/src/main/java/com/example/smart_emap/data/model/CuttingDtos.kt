package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class CuttingListResponse(
    val success: Boolean? = null,
    val data: List<CuttingManagementRowDto>? = null,
    val message: String? = null,
)

data class CuttingManagementRowDto(
    val id: Int? = null,
    @Json(name = "production_month") val productionMonth: String? = null,
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "production_line") val productionLine: String? = null,
    @Json(name = "cutting_machine") val cuttingMachine: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "priority_order") val priorityOrder: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "planned_quantity") val plannedQuantity: Int? = null,
    @Json(name = "management_code") val managementCode: String? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_setup_time_min") val mesSetupTimeMin: Int? = null,
    @Json(name = "mes_saw_blade_exchange_min") val mesSawBladeExchangeMin: Int? = null,
    @Json(name = "mes_repair_min") val mesRepairMin: Int? = null,
    @Json(name = "mes_operator_user_id") val mesOperatorUserId: Int? = null,
    @Json(name = "mes_scanned_code") val mesScannedCode: String? = null,
    val remarks: String? = null,
)

data class PatchCuttingBody(
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "cutting_machine") val cuttingMachine: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Boolean? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_setup_time_min") val mesSetupTimeMin: Int? = null,
    @Json(name = "mes_saw_blade_exchange_min") val mesSawBladeExchangeMin: Int? = null,
    @Json(name = "mes_repair_min") val mesRepairMin: Int? = null,
    @Json(name = "mes_operator_user_id") val mesOperatorUserId: Int? = null,
    @Json(name = "mes_scanned_code") val mesScannedCode: String? = null,
    val remarks: String? = null,
)

data class ReorderCuttingBody(
    @Json(name = "cutting_machine") val cuttingMachine: String,
    @Json(name = "ordered_ids") val orderedIds: List<Int>,
)

data class SplitCuttingToNextDayBody(
    @Json(name = "today_quantity") val todayQuantity: Int,
    @Json(name = "next_day") val nextDay: String? = null,
)

data class CuttingPlanningMachineDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
)

data class UserListResponse(
    val items: List<UserListItemDto>? = null,
    val total: Int? = null,
)

data class UserListItemDto(
    val id: Int? = null,
    val username: String? = null,
    @Json(name = "full_name") val fullName: String? = null,
    val status: String? = null,
) {
    fun displayLabel(): String = fullName?.trim().orEmpty().ifEmpty { username?.trim().orEmpty() }
}
