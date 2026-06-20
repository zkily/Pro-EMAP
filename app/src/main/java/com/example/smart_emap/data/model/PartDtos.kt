package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class PartStockItemDto(
    val id: Int? = null,
    @Json(name = "part_cd") val partCd: String? = null,
    @Json(name = "part_name") val partName: String? = null,
    val date: String? = null,
    @Json(name = "initial_stock") val initialStock: Int? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
    @Json(name = "planned_usage") val plannedUsage: Int? = null,
    @Json(name = "usage_plan_qty") val usagePlanQty: Int? = null,
    @Json(name = "stock_trend") val stockTrend: Int? = null,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    @Json(name = "order_amount") val orderAmount: Double? = null,
    val remarks: String? = null,
    @Json(name = "part_master_status") val partMasterStatus: Int? = null,
)

data class PartStockUpdateBodyDto(
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    @Json(name = "order_amount") val orderAmount: Double? = null,
    val remarks: String? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
    @Json(name = "initial_stock") val initialStock: Int? = null,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int? = null,
    @Json(name = "planned_usage") val plannedUsage: Int? = null,
    @Json(name = "usage_plan_qty") val usagePlanQty: Int? = null,
    @Json(name = "stock_trend") val stockTrend: Int? = null,
)

data class PartStockCreateBodyDto(
    val date: String,
    @Json(name = "part_cd") val partCd: String,
    @Json(name = "part_name") val partName: String,
    @Json(name = "initial_stock") val initialStock: Int = 0,
    @Json(name = "current_stock") val currentStock: Int = 0,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int = 0,
    val unit: String? = null,
    @Json(name = "unit_price") val unitPrice: Double = 0.0,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "lead_time") val leadTime: Int = 0,
    @Json(name = "planned_usage") val plannedUsage: Int = 0,
    @Json(name = "usage_plan_qty") val usagePlanQty: Int = 0,
    @Json(name = "stock_trend") val stockTrend: Int = 0,
    @Json(name = "order_quantity") val orderQuantity: Int = 0,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int = 0,
    @Json(name = "order_amount") val orderAmount: Double = 0.0,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int = 0,
    val remarks: String? = null,
)

data class PartOrderPrintRow(
    val partName: String?,
    val standardSpec: String?,
    val orderQuantity: Int,
    val remarks: String?,
)

fun PartStockItemDto.toPrintRow(): PartOrderPrintRow = PartOrderPrintRow(
    partName = partName,
    standardSpec = standardSpec,
    orderQuantity = orderQuantity ?: 0,
    remarks = remarks,
)

data class PartLogItemDto(
    val id: Int? = null,
    val item: String? = null,
    @Json(name = "part_cd") val partCd: String? = null,
    @Json(name = "part_name") val partName: String? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "log_date") val logDate: String? = null,
    @Json(name = "log_time") val logTime: String? = null,
    val quantity: Int? = null,
    @Json(name = "bundle_quantity") val bundleQuantity: Int? = null,
    @Json(name = "manufacture_no") val manufactureNo: String? = null,
    @Json(name = "manufacture_date") val manufactureDate: String? = null,
    val length: Double? = null,
    @Json(name = "outer_diameter1") val outerDiameter1: Double? = null,
    @Json(name = "outer_diameter2") val outerDiameter2: Double? = null,
    val supplier: String? = null,
    @Json(name = "part_quality") val partQuality: String? = null,
    @Json(name = "hd_no") val hdNo: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    val magnetic: String? = null,
    val appearance: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    val remarks: String? = null,
    val note: String? = null,
)
