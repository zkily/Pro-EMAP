package com.example.smart_emap.data.model

import com.squareup.moshi.Json

// ── 材料消費実績（material usage records） ─────────────────────────────────────

data class MaterialUsageRecordDto(
    val id: Int? = null,
    @Json(name = "usage_date") val usageDate: String? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "usage_count") val usageCount: Double? = null,
    val source: String? = null,
    @Json(name = "management_code") val managementCode: String? = null,
    @Json(name = "management_codes") val managementCodes: String? = null,
    val reflected: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class MaterialUsageRecordsDataDto(
    val list: List<MaterialUsageRecordDto>? = null,
    val total: Int? = null,
)

data class MaterialUsageRecordsResponse(
    val success: Boolean? = null,
    val data: MaterialUsageRecordsDataDto? = null,
    val message: String? = null,
)

data class MaterialUsageByDateDto(
    @Json(name = "usage_date") val usageDate: String? = null,
    val total: Double? = null,
)

data class MaterialUsageByMaterialDto(
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    val total: Double? = null,
)

data class MaterialUsageChartDataDto(
    @Json(name = "by_date") val byDate: List<MaterialUsageByDateDto>? = null,
    @Json(name = "by_material") val byMaterial: List<MaterialUsageByMaterialDto>? = null,
)

data class MaterialUsageChartResponse(
    val success: Boolean? = null,
    val data: MaterialUsageChartDataDto? = null,
    val message: String? = null,
)

// ── 工程別実績（production-actual-logs） ──────────────────────────────────────

data class ProductionActualLogDto(
    val id: Int? = null,
    @Json(name = "transaction_time") val transactionTime: String? = null,
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "stock_type") val stockType: String? = null,
    @Json(name = "target_cd") val targetCd: String? = null,
    @Json(name = "target_name") val targetName: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    @Json(name = "location_cd") val locationCd: String? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "related_doc_no") val relatedDocNo: String? = null,
)

data class ProductionActualPaginationDto(
    val total: Int? = null,
)

data class ProductionActualStatsDto(
    @Json(name = "total_records") val totalRecords: Int? = null,
    @Json(name = "total_quantity") val totalQuantity: Double? = null,
    @Json(name = "avg_quantity") val avgQuantity: Double? = null,
    @Json(name = "product_count") val productCount: Int? = null,
    @Json(name = "active_days") val activeDays: Int? = null,
)

data class ProductionActualTypeSummaryDto(
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "record_count") val recordCount: Int? = null,
    @Json(name = "total_quantity") val totalQuantity: Double? = null,
)

data class ProductionActualDataDto(
    val list: List<ProductionActualLogDto>? = null,
    val stats: ProductionActualStatsDto? = null,
    val typeSummary: List<ProductionActualTypeSummaryDto>? = null,
    val pagination: ProductionActualPaginationDto? = null,
)

data class ProductionActualLogsResponse(
    val success: Boolean? = null,
    val data: ProductionActualDataDto? = null,
    val message: String? = null,
)

/** 生産実績ログ 1 件更新（在庫取引ログ PUT）。後端 allowed キーのみ送信 */
data class ProductionActualUpdateBody(
    @Json(name = "transaction_time") val transactionTime: String? = null,
    @Json(name = "transaction_type") val transactionType: String? = null,
    @Json(name = "stock_type") val stockType: String? = null,
    @Json(name = "target_cd") val targetCd: String? = null,
    val quantity: Double? = null,
    @Json(name = "location_cd") val locationCd: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "order_no") val orderNo: String? = null,
)
