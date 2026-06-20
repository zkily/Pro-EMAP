package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class ShippingItemDto(
    val id: Int? = null,
    @Json(name = "shipping_no") val shippingNo: String? = null,
    @Json(name = "shipping_no_p") val shippingNoP: String? = null,
    @Json(name = "shipping_date") val shippingDate: String? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "destination_name") val destinationName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "box_type") val boxType: String? = null,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "confirmed_units") val confirmedUnits: Int? = null,
    val unit: String? = null,
    val status: String? = null,
    @Json(name = "picking_log_matched") val pickingLogMatched: Int? = null,
    val remarks: String? = null,
)

data class ShippingOverviewRowDto(
    @Json(name = "shipping_date") val shippingDate: String? = null,
    @Json(name = "destination_name") val destinationName: String? = null,
    @Json(name = "shipping_no") val shippingNo: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "box_type") val boxType: String? = null,
    val quantity: Int? = null,
    val units: Int? = null,
    @Json(name = "delivery_date") val deliveryDate: String? = null,
)

data class PickingTodayOverviewDto(
    @Json(name = "total_today") val totalToday: Int? = null,
    @Json(name = "pending_today") val pendingToday: Int? = null,
    @Json(name = "completed_today") val completedToday: Int? = null,
    @Json(name = "today_completion_rate") val todayCompletionRate: Double? = null,
)

data class PickingProgressStatDto(
    @Json(name = "shipping_date") val shippingDate: String? = null,
    @Json(name = "total_count") val totalCount: Int? = null,
    @Json(name = "pending_count") val pendingCount: Int? = null,
    @Json(name = "completed_count") val completedCount: Int? = null,
    @Json(name = "completion_rate") val completionRate: Double? = null,
)

data class PickingNewProgressDto(
    @Json(name = "todayOverview") val todayOverview: PickingTodayOverviewDto? = null,
    @Json(name = "palletList") val palletList: List<ShippingItemDto>? = null,
    @Json(name = "progressStats") val progressStats: List<PickingProgressStatDto>? = null,
)

data class PickingHistoryRowDto(
    @Json(name = "shipping_date") val shippingDate: String? = null,
    @Json(name = "destination_name") val destinationName: String? = null,
    @Json(name = "shipping_no_p") val shippingNoP: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "confirmed_boxes") val confirmedBoxes: Int? = null,
    @Json(name = "picking_log_matched") val pickingLogMatched: Int? = null,
    val status: String? = null,
)

data class DestinationGroupDto(
    val id: Int? = null,
    @Json(name = "group_name") val groupName: String? = null,
    @Json(name = "destination_cds") val destinationCds: List<String>? = null,
    @Json(name = "destination_names") val destinationNames: List<String>? = null,
)

data class WeldingShippingProductDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
)

data class WeldingShippingCellDto(
    @Json(name = "shipping_date") val shippingDate: String? = null,
    val quantity: Int? = null,
)

data class WeldingShippingRowDto(
    @Json(name = "destination_name") val destinationName: String? = null,
    val cells: List<WeldingShippingCellDto>? = null,
    val total: Int? = null,
)

data class WeldingShippingDataDto(
    @Json(name = "date_columns") val dateColumns: List<String>? = null,
    val rows: List<WeldingShippingRowDto>? = null,
    val html: String? = null,
)

data class WarehouseDailyRowDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "work_date") val workDate: String? = null,
    @Json(name = "order_qty") val orderQty: Double? = null,
    @Json(name = "forecast_qty") val forecastQty: Double? = null,
    @Json(name = "warehouse_stock") val warehouseStock: Double? = null,
    @Json(name = "warehouse_actual") val warehouseActual: Double? = null,
    @Json(name = "warehouse_carryover") val warehouseCarryover: Double? = null,
    @Json(name = "warehouse_defect") val warehouseDefect: Double? = null,
    @Json(name = "warehouse_disposal") val warehouseDisposal: Double? = null,
    @Json(name = "warehouse_hold") val warehouseHold: Double? = null,
)

data class WarehouseDailyRowsResponse(
    val rows: List<WarehouseDailyRowDto>? = null,
    val total: Int? = null,
)

data class InventoryKpiRowDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "turnover") val turnoverRate: Double? = null,
    @Json(name = "turnover_days") val turnoverDays: Double? = null,
    @Json(name = "avg_inventory_days") val avgInventoryDays: Double? = null,
    @Json(name = "warehouse_inventory") val warehouseInventory: Int? = null,
    @Json(name = "closing_inventory") val closingInventory: Int? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "shortage_qty") val shortageQty: Int? = null,
    @Json(name = "overstock_qty") val overstockQty: Int? = null,
    @Json(name = "reorder_point") val reorderPoint: Int? = null,
    val rank: String? = null,
    @Json(name = "abc_class") val abcClass: String? = null,
)

data class AbcAnalysisRowDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "destination_name") val destinationName: String? = null,
    @Json(name = "shipping_qty") val shippingQty: Int? = null,
    val rank: String? = null,
    @Json(name = "cumulative_pct") val cumulativePct: Double? = null,
)
