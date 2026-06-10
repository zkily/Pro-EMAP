package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class InspectionProductivityAnalysisResponse(
    val success: Boolean? = null,
    val data: InspectionProductivityAnalysisDataDto? = null,
    val message: String? = null,
)

data class InspectionProductivityAnalysisDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    @Json(name = "include_incomplete") val includeIncomplete: Boolean? = null,
    val summary: InspectionProductivityBucketDto? = null,
    val daily: List<InspectionProductivityDailyRowDto>? = null,
    @Json(name = "by_inspector") val byInspector: List<InspectionProductivityInspectorRowDto>? = null,
    @Json(name = "by_product") val byProduct: List<InspectionProductivityProductRowDto>? = null,
    @Json(name = "by_product_inspector_ranking") val byProductInspectorRanking: List<InspectionProductivityProductRankingDto>? = null,
    @Json(name = "defect_by_item") val defectByItem: List<InspectionProductivityDefectRowDto>? = null,
    val sessions: List<InspectionProductivitySessionRowDto>? = null,
)

data class InspectionProductivityBucketDto(
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "completed_session_count") val completedSessionCount: Int? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "sum_defect_qty") val sumDefectQty: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "sum_paused_sec") val sumPausedSec: Int? = null,
    @Json(name = "sum_paused_min") val sumPausedMin: Int? = null,
    @Json(name = "defect_rate_percent") val defectRatePercent: Double? = null,
    @Json(name = "efficiency_per_hour") val efficiencyPerHour: Double? = null,
)

data class InspectionProductivityDailyRowDto(
    val day: String? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "completed_session_count") val completedSessionCount: Int? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "sum_defect_qty") val sumDefectQty: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "sum_paused_sec") val sumPausedSec: Int? = null,
    @Json(name = "sum_paused_min") val sumPausedMin: Int? = null,
    @Json(name = "defect_rate_percent") val defectRatePercent: Double? = null,
    @Json(name = "efficiency_per_hour") val efficiencyPerHour: Double? = null,
)

data class InspectionProductivityInspectorRowDto(
    @Json(name = "inspector_user_id") val inspectorUserId: Int? = null,
    @Json(name = "inspector_name") val inspectorName: String? = null,
    val rank: Int? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "completed_session_count") val completedSessionCount: Int? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "sum_defect_qty") val sumDefectQty: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "sum_paused_sec") val sumPausedSec: Int? = null,
    @Json(name = "sum_paused_min") val sumPausedMin: Int? = null,
    @Json(name = "defect_rate_percent") val defectRatePercent: Double? = null,
    @Json(name = "efficiency_per_hour") val efficiencyPerHour: Double? = null,
)

data class InspectionProductivityProductRowDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "completed_session_count") val completedSessionCount: Int? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "sum_defect_qty") val sumDefectQty: Int? = null,
    @Json(name = "sum_net_production_sec") val sumNetProductionSec: Int? = null,
    @Json(name = "sum_net_production_min") val sumNetProductionMin: Int? = null,
    @Json(name = "sum_paused_sec") val sumPausedSec: Int? = null,
    @Json(name = "sum_paused_min") val sumPausedMin: Int? = null,
    @Json(name = "defect_rate_percent") val defectRatePercent: Double? = null,
    @Json(name = "efficiency_per_hour") val efficiencyPerHour: Double? = null,
)

data class InspectionProductivityProductRankingDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "inspector_count") val inspectorCount: Int? = null,
    @Json(name = "ranked_inspector_count") val rankedInspectorCount: Int? = null,
    @Json(name = "top_inspector_name") val topInspectorName: String? = null,
    @Json(name = "top_efficiency_per_hour") val topEfficiencyPerHour: Double? = null,
    val inspectors: List<InspectionProductivityInspectorRowDto>? = null,
)

data class InspectionProductivityDefectRowDto(
    @Json(name = "defect_cd") val defectCd: String,
    val qty: Int? = null,
)

data class InspectionProductivitySessionRowDto(
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "inspector_display_name") val inspectorDisplayName: String? = null,
    @Json(name = "mes_inspector_name") val mesInspectorName: String? = null,
    @Json(name = "mes_inspector_user_id") val mesInspectorUserId: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,
    @Json(name = "defect_qty") val defectQty: Int? = null,
    @Json(name = "net_production_sec") val netProductionSec: Int? = null,
    @Json(name = "paused_sec") val pausedSec: Int? = null,
    @Json(name = "net_production_min") val netProductionMin: Int? = null,
    @Json(name = "paused_min") val pausedMin: Int? = null,
    @Json(name = "efficiency_per_hour") val efficiencyPerHour: Double? = null,
    @Json(name = "defect_rate_percent") val defectRatePercent: Double? = null,
    @Json(name = "is_completed") val isCompleted: Boolean? = null,
)
