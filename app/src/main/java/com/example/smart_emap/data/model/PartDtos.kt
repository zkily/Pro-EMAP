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
    @Json(name = "stock_trend") val stockTrend: String? = null,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    @Json(name = "order_amount") val orderAmount: Double? = null,
    val remarks: String? = null,
)

data class PartStockUpdateBodyDto(
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    val remarks: String? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
)
