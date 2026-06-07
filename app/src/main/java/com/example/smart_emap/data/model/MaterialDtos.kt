package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class MaterialLogItemDto(
    val id: Int? = null,
    val item: String? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
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
    @Json(name = "material_quality") val materialQuality: String? = null,
    val remarks: String? = null,
    val note: String? = null,
)

data class MaterialStockItemDto(
    val id: Int? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    val date: String? = null,
    @Json(name = "initial_stock") val initialStock: Int? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "planned_usage") val plannedUsage: Int? = null,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int? = null,
    @Json(name = "max_stock") val maxStock: Int? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    @Json(name = "long_weight") val longWeight: Double? = null,
    @Json(name = "bundle_quantity") val bundleQuantity: Int? = null,
    @Json(name = "bundle_weight") val bundleWeight: Double? = null,
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    @Json(name = "order_amount") val orderAmount: Double? = null,
    val remarks: String? = null,
)

data class MaterialStockSubItemDto(
    val id: Int? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    val date: String? = null,
    @Json(name = "current_stock") val currentStock: Double? = null,
    @Json(name = "planned_usage") val plannedUsage: Double? = null,
    @Json(name = "order_quantity") val orderQuantity: Double? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Double? = null,
    @Json(name = "bundle_weight") val bundleWeight: Double? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "label_color") val labelColor: String? = null,
    val remarks: String? = null,
)

data class MaterialStockSubUpdateBodyDto(
    @Json(name = "planned_usage") val plannedUsage: Int? = null,
    val remarks: String? = null,
    @Json(name = "label_color") val labelColor: String? = null,
)

data class MaterialForecastDetailDto(
    val year: Int? = null,
    val month: Int? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "forecast_units") val forecastUnits: Int? = null,
    @Json(name = "lot_size") val lotSize: Int? = null,
    @Json(name = "material_required") val materialRequired: Double? = null,
)

data class MaterialForecastSummaryDto(
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "product_count") val productCount: Int? = null,
    @Json(name = "total_forecast_units") val totalForecastUnits: Int? = null,
    @Json(name = "avg_lot_size") val avgLotSize: Double? = null,
    @Json(name = "total_material_required") val totalMaterialRequired: Double? = null,
)

data class MaterialForecastStatsDto(
    @Json(name = "total_products") val totalProducts: Int? = null,
    @Json(name = "total_materials") val totalMaterials: Int? = null,
    @Json(name = "total_suppliers") val totalSuppliers: Int? = null,
    @Json(name = "total_forecast_units") val totalForecastUnits: Int? = null,
    @Json(name = "total_material_required") val totalMaterialRequired: Double? = null,
)

data class MaterialMasterItemDto(
    val id: Int? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "long_weight") val longWeight: Double? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "tolerance_1") val tolerance1: Double? = null,
    @Json(name = "tolerance_2") val tolerance2: Double? = null,
    val status: Int? = null,
)

data class MaterialStockSubCreateBodyDto(
    @Json(name = "material_cd") val materialCd: String,
    @Json(name = "material_name") val materialName: String,
    val date: String,
    @Json(name = "current_stock") val currentStock: Int = 0,
    @Json(name = "safety_stock") val safetyStock: Int = 0,
    @Json(name = "max_stock") val maxStock: Int = 0,
    val unit: String? = null,
    @Json(name = "unit_price") val unitPrice: Double = 0.0,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "lead_time") val leadTime: Int = 0,
    @Json(name = "planned_usage") val plannedUsage: Int = 0,
    @Json(name = "order_quantity") val orderQuantity: Int = 0,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int = 0,
    @Json(name = "bundle_weight") val bundleWeight: Double = 0.0,
    @Json(name = "order_amount") val orderAmount: Double = 0.0,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int = 0,
    @Json(name = "long_weight") val longWeight: Double? = null,
    val remarks: String? = null,
)

data class MaterialMasterUpdateBodyDto(
    @Json(name = "tolerance_1") val tolerance1: Double? = null,
    @Json(name = "tolerance_2") val tolerance2: Double? = null,
    val status: Int? = null,
)

data class MaterialStockUpdateBodyDto(
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "order_bundle_quantity") val orderBundleQuantity: Int? = null,
    @Json(name = "planned_usage") val plannedUsage: Int? = null,
    val remarks: String? = null,
    @Json(name = "current_stock") val currentStock: Int? = null,
    @Json(name = "initial_stock") val initialStock: Int? = null,
    @Json(name = "adjustment_quantity") val adjustmentQuantity: Int? = null,
    @Json(name = "bundle_weight") val bundleWeight: Double? = null,
    @Json(name = "order_amount") val orderAmount: Double? = null,
)

data class MaterialTransferToSubBodyDto(
    @Json(name = "stock_id") val stockId: Int,
    val quantity: Int = 1,
)

data class MaterialOrderPrintRow(
    val materialName: String?,
    val standardSpec: String?,
    val orderQuantity: Int,
    val orderBundleQuantity: Int,
    val bundleWeight: Double,
    val remarks: String?,
)

fun MaterialStockItemDto.toPrintRow(): MaterialOrderPrintRow = MaterialOrderPrintRow(
    materialName = materialName,
    standardSpec = standardSpec,
    orderQuantity = orderQuantity ?: 0,
    orderBundleQuantity = orderBundleQuantity ?: 0,
    bundleWeight = bundleWeight ?: 0.0,
    remarks = remarks,
)

fun MaterialStockSubItemDto.toPrintRow(): MaterialOrderPrintRow = MaterialOrderPrintRow(
    materialName = materialName,
    standardSpec = standardSpec,
    orderQuantity = orderQuantity?.toInt() ?: 0,
    orderBundleQuantity = orderBundleQuantity?.toInt() ?: 0,
    bundleWeight = bundleWeight ?: 0.0,
    remarks = remarks,
)
