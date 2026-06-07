package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class ChamferingListResponse(
    val success: Boolean? = null,
    val data: List<ChamferingManagementRowDto>? = null,
    val message: String? = null,
)

data class ChamferingManagementRowDto(
    val id: Int? = null,
    @Json(name = "production_month") val productionMonth: String? = null,
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "production_line") val productionLine: String? = null,
    @Json(name = "chamfering_machine") val chamferingMachine: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
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
    @Json(name = "mes_operator_user_id") val mesOperatorUserId: Int? = null,
    @Json(name = "mes_scanned_code") val mesScannedCode: String? = null,
    val remarks: String? = null,
)

data class PatchChamferingBody(
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "chamfering_machine") val chamferingMachine: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Boolean? = null,
    @Json(name = "no_count") val noCount: Boolean? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_setup_time_min") val mesSetupTimeMin: Int? = null,
    @Json(name = "mes_operator_user_id") val mesOperatorUserId: Int? = null,
    @Json(name = "mes_scanned_code") val mesScannedCode: String? = null,
    val remarks: String? = null,
)

data class ReorderChamferingBody(
    @Json(name = "chamfering_machine") val chamferingMachine: String,
    @Json(name = "production_day") val productionDay: String,
    @Json(name = "ordered_ids") val orderedIds: List<Int>,
)

data class SplitChamferingToNextDayBody(
    @Json(name = "today_quantity") val todayQuantity: Int,
    @Json(name = "next_day") val nextDay: String? = null,
)

data class ChamferingMachineDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
)
