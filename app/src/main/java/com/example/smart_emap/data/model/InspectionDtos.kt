package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class InspectionListResponse(
    val success: Boolean? = null,
    val data: List<InspectionManagementRowDto>? = null,
    val message: String? = null,
)

data class InspectionMonitorSummaryResponse(
    val success: Boolean? = null,
    val data: List<InspectionManagementRowDto>? = null,
    @Json(name = "fetched_at") val fetchedAt: String? = null,
    val message: String? = null,
)

data class InspectionNextAssignmentDto(
    val id: Int? = null,
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "inspector_user_id") val inspectorUserId: Int? = null,
    @Json(name = "next_product_cd") val nextProductCd: String? = null,
    @Json(name = "next_product_name") val nextProductName: String? = null,
    @Json(name = "assigned_by_user_id") val assignedByUserId: Int? = null,
    @Json(name = "assigned_at") val assignedAt: String? = null,
    val note: String? = null,
    @Json(name = "inspector_name") val inspectorName: String? = null,
    @Json(name = "inspector_username") val inspectorUsername: String? = null,
    @Json(name = "assigned_by_name") val assignedByName: String? = null,
)

data class InspectionNextAssignmentsResponse(
    val success: Boolean? = null,
    val data: List<InspectionNextAssignmentDto>? = null,
    val message: String? = null,
)

data class InspectionNextAssignmentResponse(
    val success: Boolean? = null,
    val data: InspectionNextAssignmentDto? = null,
    val message: String? = null,
)

data class UpsertInspectionNextAssignmentBody(
    @Json(name = "production_day") val productionDay: String,
    @Json(name = "inspector_user_id") val inspectorUserId: Int,
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    val note: String? = null,
)

data class DeleteInspectionNextAssignmentBody(
    @Json(name = "production_day") val productionDay: String,
    @Json(name = "inspector_user_id") val inspectorUserId: Int,
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
    @MesDefectByItem @Json(name = "mes_defect_by_item") val mesDefectByItem: Map<String, Int>? = null,
    @Json(name = "production_completed_check") val productionCompletedCheck: Int? = null,
    @Json(name = "mes_production_started_at") val mesProductionStartedAt: String? = null,
    @Json(name = "mes_production_ended_at") val mesProductionEndedAt: String? = null,
    @Json(name = "mes_net_production_sec") val mesNetProductionSec: Int? = null,
    @Json(name = "mes_paused_accum_sec") val mesPausedAccumSec: Int? = null,
    @Json(name = "mes_break_sec") val mesBreakSec: Int? = null,
    @Json(name = "mes_stop_sec") val mesStopSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    @Json(name = "mes_inspector_name") val mesInspectorName: String? = null,
    @Json(name = "mes_inspector_username") val mesInspectorUsername: String? = null,
    @Json(name = "mes_client_instance_id") val mesClientInstanceId: String? = null,
    @Json(name = "data_source") val dataSource: String? = null,
    @Json(name = "external_sync_key") val externalSyncKey: String? = null,
    @Json(name = "manual_registration_note") val manualRegistrationNote: String? = null,
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
    @Json(name = "manual_registration_note") val manualRegistrationNote: String? = null,
    @Json(name = "manual_registration") val manualRegistration: Boolean? = null,
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
    @Json(name = "mes_break_sec") val mesBreakSec: Int? = null,
    @Json(name = "mes_stop_sec") val mesStopSec: Int? = null,
    @Json(name = "mes_production_is_paused") val mesProductionIsPaused: Int? = null,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    @MesDefectByItem @Json(name = "mes_defect_by_item") val mesDefectByItem: Map<String, Int>? = null,
    @Json(name = "mes_client_instance_id") val mesClientInstanceId: String? = null,
    @Json(name = "mes_claim_client_lock") val mesClaimClientLock: Boolean? = null,
    @Json(name = "mes_force_release") val mesForceRelease: Boolean? = null,
    val remarks: String? = null,
    @Json(name = "manual_registration_note") val manualRegistrationNote: String? = null,
    @Json(name = "manual_registration") val manualRegistration: Boolean? = null,
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
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
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
