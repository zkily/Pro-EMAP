package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class WeldingProductivityAnalysisResponse(
    val success: Boolean? = null,
    val data: WeldingProductivityAnalysisDataDto? = null,
    val message: String? = null,
)

data class WeldingProductivityAnalysisDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    @Json(name = "include_incomplete") val includeIncomplete: Boolean? = null,
    val summary: WeldingProductivityBucketDto? = null,
    val daily: List<WeldingProductivityDailyRowDto>? = null,
    @Json(name = "by_operator") val byOperator: List<WeldingProductivityOperatorRowDto>? = null,
    @Json(name = "by_product") val byProduct: List<WeldingProductivityProductRowDto>? = null,
    @Json(name = "by_product_operator_ranking") val byProductOperatorRanking: List<WeldingProductivityProductRankingDto>? = null,
    @Json(name = "defect_by_item") val defectByItem: List<WeldingProductivityDefectRowDto>? = null,
    val sessions: List<WeldingProductivitySessionRowDto>? = null,
)

data class WeldingProductivityBucketDto(
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

data class WeldingProductivityDailyRowDto(
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

data class WeldingProductivityOperatorRowDto(
    @Json(name = "operator_user_id") val operatorUserId: Int? = null,
    @Json(name = "operator_name") val operatorName: String? = null,
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

data class WeldingProductivityProductRowDto(
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

data class WeldingProductivityProductRankingDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "sum_actual_qty") val sumActualQty: Int? = null,
    @Json(name = "session_count") val sessionCount: Int? = null,
    @Json(name = "operator_count") val operatorCount: Int? = null,
    @Json(name = "ranked_operator_count") val rankedOperatorCount: Int? = null,
    @Json(name = "top_operator_name") val topOperatorName: String? = null,
    @Json(name = "top_efficiency_per_hour") val topEfficiencyPerHour: Double? = null,
    val operators: List<WeldingProductivityOperatorRowDto>? = null,
)

data class WeldingProductivityDefectRowDto(
    @Json(name = "defect_cd") val defectCd: String,
    val qty: Int? = null,
)

data class WeldingProductivitySessionRowDto(
    @Json(name = "production_day") val productionDay: String? = null,
    @Json(name = "operator_display_name") val operatorDisplayName: String? = null,
    @Json(name = "mes_operator_name") val mesOperatorName: String? = null,
    @Json(name = "mes_operator_user_id") val mesOperatorUserId: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "welding_machine") val weldingMachine: String? = null,
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
