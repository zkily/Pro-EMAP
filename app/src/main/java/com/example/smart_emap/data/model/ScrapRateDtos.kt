package com.example.smart_emap.data.model

import com.squareup.moshi.Json

// ── 廃棄率分析（quality-rate） ────────────────────────────────────────────────

/** 工程別集計の1行 */
data class QualityProcessRowDto(
    val key: String? = null,
    val label: String? = null,
    @Json(name = "sum_actual") val sumActual: Double? = null,
    @Json(name = "sum_defect") val sumDefect: Double? = null,
    @Json(name = "sum_scrap") val sumScrap: Double? = null,
    @Json(name = "sum_defect_and_scrap") val sumDefectAndScrap: Double? = null,
    @Json(name = "sum_defect_amount") val sumDefectAmount: Double? = null,
    @Json(name = "sum_scrap_amount") val sumScrapAmount: Double? = null,
    @Json(name = "sum_defect_and_scrap_amount") val sumDefectAndScrapAmount: Double? = null,
    val rate: Double? = null,
    @Json(name = "rate_percent") val ratePercent: Double? = null,
)

data class QualitySummaryDto(
    val basis: String? = null,
    @Json(name = "reference_process_key") val referenceProcessKey: String? = null,
    @Json(name = "reference_process_label") val referenceProcessLabel: String? = null,
    @Json(name = "sum_actual") val sumActual: Double? = null,
    @Json(name = "sum_defect") val sumDefect: Double? = null,
    @Json(name = "sum_scrap") val sumScrap: Double? = null,
    @Json(name = "sum_defect_and_scrap") val sumDefectAndScrap: Double? = null,
    val rate: Double? = null,
    @Json(name = "rate_percent") val ratePercent: Double? = null,
    @Json(name = "rolled_yield_rate") val rolledYieldRate: Double? = null,
    @Json(name = "rolled_yield_percent") val rolledYieldPercent: Double? = null,
)

data class QualityRateByProcessDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    @Json(name = "process_filter") val processFilter: String? = null,
    val processes: List<QualityProcessRowDto>? = null,
    val summary: QualitySummaryDto? = null,
    @Json(name = "all_processes_defect_scrap_total") val allProcessesDefectScrapTotal: Double? = null,
)

data class QualityRateByProcessResponse(
    val data: QualityRateByProcessDataDto? = null,
)

/** 製品別マトリクスの工程セル */
data class QualityProductProcessDto(
    val key: String? = null,
    val label: String? = null,
    @Json(name = "sum_actual") val sumActual: Double? = null,
    @Json(name = "sum_defect") val sumDefect: Double? = null,
    @Json(name = "sum_scrap") val sumScrap: Double? = null,
    @Json(name = "sum_defect_and_scrap") val sumDefectAndScrap: Double? = null,
    val rate: Double? = null,
    @Json(name = "rate_percent") val ratePercent: Double? = null,
)

data class QualityProductRowDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "all_processes_defect_scrap") val allProcessesDefectScrap: Double? = null,
    val processes: List<QualityProductProcessDto>? = null,
)

data class MainLineLabelDto(
    val key: String? = null,
    val label: String? = null,
)

data class QualityPaginationDto(
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null,
)

data class QualityRateByProductDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    val scope: String? = null,
    @Json(name = "main_line_labels") val mainLineLabels: List<MainLineLabelDto>? = null,
    val pagination: QualityPaginationDto? = null,
    val products: List<QualityProductRowDto>? = null,
)

data class QualityRateByProductResponse(
    val data: QualityRateByProductDataDto? = null,
)
