package com.example.smart_emap.data.model

import com.squareup.moshi.Json

// ===== 材料需要量 =====

data class MaterialRequirementsSummaryItemDto(
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "material_manufacturer") val materialManufacturer: String? = null,
    @Json(name = "standard_specification") val standardSpecification: String? = null,
    @Json(name = "piece_count") val pieceCount: Double? = null,
)

data class MaterialRequirementsSummaryMetaDto(
    @Json(name = "date_start") val dateStart: String? = null,
    @Json(name = "date_end") val dateEnd: String? = null,
    @Json(name = "production_month_filter") val productionMonthFilter: String? = null,
    @Json(name = "total_material_kinds") val totalMaterialKinds: Int? = null,
    @Json(name = "total_piece_count") val totalPieceCount: Double? = null,
    @Json(name = "effective_date_note") val effectiveDateNote: String? = null,
    @Json(name = "daily_matrix_omitted") val dailyMatrixOmitted: Boolean? = null,
    @Json(name = "daily_matrix_max_days") val dailyMatrixMaxDays: Int? = null,
)

data class MaterialRequirementsDailyMatrixRowDto(
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "material_manufacturer") val materialManufacturer: String? = null,
    @Json(name = "standard_specification") val standardSpecification: String? = null,
    @Json(name = "by_date") val byDate: Map<String, Double>? = null,
    @Json(name = "row_total") val rowTotal: Double? = null,
)

data class MaterialRequirementsDailyMatrixDto(
    val dates: List<String>? = null,
    val rows: List<MaterialRequirementsDailyMatrixRowDto>? = null,
)

data class MaterialRequirementsDataDto(
    val items: List<MaterialRequirementsSummaryItemDto>? = null,
    val summary: MaterialRequirementsSummaryMetaDto? = null,
    @Json(name = "daily_matrix") val dailyMatrix: MaterialRequirementsDailyMatrixDto? = null,
)

data class MaterialRequirementsSummaryResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: MaterialRequirementsDataDto? = null,
)

// ===== 部品需要量（demand + use bundle） =====

data class ComponentRequirementsSummaryItemDto(
    @Json(name = "component_cd") val componentCd: String? = null,
    @Json(name = "component_name") val componentName: String? = null,
    @Json(name = "component_uom") val componentUom: String? = null,
    @Json(name = "source_lot_count") val sourceLotCount: Double? = null,
    @Json(name = "required_qty") val requiredQty: Double? = null,
)

data class ComponentRequirementsSummaryMetaDto(
    @Json(name = "date_start") val dateStart: String? = null,
    @Json(name = "date_end") val dateEnd: String? = null,
    @Json(name = "production_month_filter") val productionMonthFilter: String? = null,
    @Json(name = "total_component_kinds") val totalComponentKinds: Int? = null,
    @Json(name = "total_required_qty") val totalRequiredQty: Double? = null,
    @Json(name = "effective_date_note") val effectiveDateNote: String? = null,
    @Json(name = "daily_matrix_omitted") val dailyMatrixOmitted: Boolean? = null,
    @Json(name = "daily_matrix_max_days") val dailyMatrixMaxDays: Int? = null,
    @Json(name = "plan_column") val planColumn: String? = null,
)

data class ComponentRequirementsDailyMatrixRowDto(
    @Json(name = "component_cd") val componentCd: String? = null,
    @Json(name = "component_name") val componentName: String? = null,
    @Json(name = "component_uom") val componentUom: String? = null,
    @Json(name = "by_date") val byDate: Map<String, Double>? = null,
    @Json(name = "row_total") val rowTotal: Double? = null,
)

data class ComponentRequirementsDailyMatrixDto(
    val dates: List<String>? = null,
    val rows: List<ComponentRequirementsDailyMatrixRowDto>? = null,
)

data class ComponentRequirementsSectionDto(
    val items: List<ComponentRequirementsSummaryItemDto>? = null,
    val summary: ComponentRequirementsSummaryMetaDto? = null,
    @Json(name = "daily_matrix") val dailyMatrix: ComponentRequirementsDailyMatrixDto? = null,
)

data class ComponentRequirementsBundleDataDto(
    @Json(name = "date_start") val dateStart: String? = null,
    @Json(name = "date_end") val dateEnd: String? = null,
    @Json(name = "production_month_filter") val productionMonthFilter: String? = null,
    val demand: ComponentRequirementsSectionDto? = null,
    val use: ComponentRequirementsSectionDto? = null,
)

data class ComponentRequirementsBundleResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val data: ComponentRequirementsBundleDataDto? = null,
)
