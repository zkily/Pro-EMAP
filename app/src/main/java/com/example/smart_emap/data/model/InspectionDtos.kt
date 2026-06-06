package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class InspectionListResponse(
    val success: Boolean? = null,
    val data: List<InspectionManagementRowDto>? = null,
    val message: String? = null,
)

data class InspectionManagementRowDto(
    val id: Int? = null,
    @Json(name = "production_month") val productionMonth: String? = null,
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "mes_defect_by_item") val mesDefectByItem: Map<String, Int>? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    @Json(name = "mes_client_instance_id") val mesClientInstanceId: String? = null,
    val remarks: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class CreateInspectionBody(
    @Json(name = "production_day") val productionDay: String,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    val remarks: String? = null,
)

data class CreateInspectionResponse(
    val success: Boolean? = null,
    val data: CreateInspectionData? = null,
    val message: String? = null,
)

data class CreateInspectionData(
    val id: Int? = null,
)

data class PatchInspectionBody(
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "production_sequence") val productionSequence: Int? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Boolean? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    @Json(name = "mes_defect_by_item") val mesDefectByItem: Map<String, Int>? = null,
    @Json(name = "mes_client_instance_id") val mesClientInstanceId: String? = null,
    @Json(name = "mes_claim_client_lock") val mesClaimClientLock: Boolean? = null,
    @Json(name = "mes_force_release") val mesForceRelease: Boolean? = null,
    val remarks: String? = null,
)

data class ApiMessageResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val detail: String? = null,
)

data class ErpProductDto(
    val id: Int? = null,
    @Json(name = "product_code") val productCode: String = "",
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String = "",
    @Json(name = "is_active") val isActive: Boolean? = true,
) {
    fun normalizedCode(): String = productCode.trim().ifEmpty { productCd?.trim().orEmpty() }

    fun normalizedName(): String = productName.trim()
}

data class ErpProductsEnvelope(
    val success: Boolean? = null,
    val data: List<ErpProductDto>? = null,
)

data class ProcessDefectItemDto(
    val id: Int? = null,
    @Json(name = "detection_process_cd") val detectionProcessCd: String? = null,
    @Json(name = "attributable_process_cd") val attributableProcessCd: String? = null,
    @Json(name = "defect_cd") val defectCd: String,
    @Json(name = "defect_name") val defectName: String,
    @Json(name = "attributable_process_name") val attributableProcessName: String? = null,
)

data class ProcessDefectOptionsResponse(
    val success: Boolean? = null,
    val data: List<ProcessDefectItemDto>? = null,
)
