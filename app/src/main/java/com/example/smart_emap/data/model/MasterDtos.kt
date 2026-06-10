package com.example.smart_emap.data.model

import com.squareup.moshi.Json

data class MasterListEnvelope<T>(
    val success: Boolean? = null,
    val data: PagedListDto<T>? = null,
    val list: List<T>? = null,
    val total: Int? = null,
) {
    fun items(): List<T> = data?.list ?: list.orEmpty()
    fun totalCount(): Int = data?.total ?: total ?: items().size
}

data class MasterProductDto(
    val id: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_type") val productType: String? = null,
    val category: String? = null,
    val kind: String? = null,
    val status: String? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "lot_size") val lotSize: Int? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    @Json(name = "cut_length") val cutLength: Double? = null,
    @Json(name = "part_number") val partNumber: String? = null,
    @Json(name = "box_type") val boxType: String? = null,
    @Json(name = "process_count") val processCount: Int? = null,
    @Json(name = "location_cd") val locationCd: String? = null,
    @Json(name = "start_use_date") val startUseDate: String? = null,
    @Json(name = "vehicle_model") val vehicleModel: String? = null,
    @Json(name = "product_alias") val productAlias: String? = null,
    @Json(name = "is_multistage") val isMultistage: Boolean? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    val priority: Int? = null,
    @Json(name = "safety_days") val safetyDays: Int? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    val dimensions: String? = null,
    val weight: Double? = null,
    @Json(name = "chamfer_length") val chamferLength: Double? = null,
    @Json(name = "developed_length") val developedLength: Double? = null,
    @Json(name = "scrap_length") val scrapLength: Double? = null,
    @Json(name = "take_count") val takeCount: Int? = null,
    val note: String? = null,
)

data class ProductMasterStatsDto(
    val total: Int = 0,
    val massProduction: Int = 0,
    val prototype: Int = 0,
    val supply: Int = 0,
    val other: Int = 0,
)

data class MasterMaterialDto(
    val id: Int? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
    @Json(name = "material_type") val materialType: String? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    val diameter: Double? = null,
    val thickness: Double? = null,
    val length: Double? = null,
    @Json(name = "supply_classification") val supplyClassification: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    val usegae: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "long_weight") val longWeight: Double? = null,
    @Json(name = "single_price") val singlePrice: Double? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "storage_location") val storageLocation: String? = null,
    val status: Int? = null,
    @Json(name = "tolerance_range") val toleranceRange: String? = null,
    @Json(name = "tolerance_1") val tolerance1: Double? = null,
    @Json(name = "tolerance_2") val tolerance2: Double? = null,
    @Json(name = "range_value") val rangeValue: String? = null,
    @Json(name = "min_value") val minValue: Double? = null,
    @Json(name = "max_value") val maxValue: Double? = null,
    @Json(name = "actual_value_1") val actualValue1: Double? = null,
    @Json(name = "actual_value_2") val actualValue2: Double? = null,
    @Json(name = "actual_value_3") val actualValue3: Double? = null,
    @Json(name = "representative_model") val representativeModel: String? = null,
    val note: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class MaterialMasterStatsDto(
    val total: Int = 0,
    val active: Int = 0,
    val inactive: Int = 0,
)

data class MasterMaterialBodyDto(
    @Json(name = "material_cd") val materialCd: String,
    @Json(name = "material_name") val materialName: String,
    @Json(name = "material_type") val materialType: String? = null,
    @Json(name = "standard_spec") val standardSpec: String? = null,
    val unit: String? = null,
    val diameter: Double? = null,
    val thickness: Double? = null,
    val length: Double? = null,
    @Json(name = "supply_classification") val supplyClassification: String? = null,
    @Json(name = "pieces_per_bundle") val piecesPerBundle: Int? = null,
    val usegae: String? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "long_weight") val longWeight: Double? = null,
    @Json(name = "single_price") val singlePrice: Double? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "storage_location") val storageLocation: String? = null,
    val status: Int? = null,
    @Json(name = "tolerance_range") val toleranceRange: String? = null,
    @Json(name = "tolerance_1") val tolerance1: Double? = null,
    @Json(name = "tolerance_2") val tolerance2: Double? = null,
    @Json(name = "range_value") val rangeValue: String? = null,
    @Json(name = "min_value") val minValue: Double? = null,
    @Json(name = "max_value") val maxValue: Double? = null,
    @Json(name = "actual_value_1") val actualValue1: Double? = null,
    @Json(name = "actual_value_2") val actualValue2: Double? = null,
    @Json(name = "actual_value_3") val actualValue3: Double? = null,
    @Json(name = "representative_model") val representativeModel: String? = null,
    val note: String? = null,
)

data class MaterialCsvExportItemDto(
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "material_name") val materialName: String? = null,
)

data class MasterInspectionDto(
    val id: Int? = null,
    @Json(name = "inspection_cd") val inspectionCd: String? = null,
    @Json(name = "inspection_standard") val inspectionStandard: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class MasterInspectionBodyDto(
    @Json(name = "inspection_cd") val inspectionCd: String,
    @Json(name = "inspection_standard") val inspectionStandard: String,
)

data class MasterPartDto(
    val id: Int? = null,
    @Json(name = "part_cd") val partCd: String? = null,
    @Json(name = "part_name") val partName: String? = null,
    val category: String? = null,
    val kind: String? = null,
    @Json(name = "settlement_type") val settlementType: String? = null,
    val uom: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "material_unit_price") val materialUnitPrice: Double? = null,
    @Json(name = "total_unit_price") val totalUnitPrice: Double? = null,
    val currency: String? = null,
    @Json(name = "exchange_rate") val exchangeRate: Double? = null,
    @Json(name = "standard_price_jpy") val standardPriceJpy: Double? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    val status: Int? = null,
    val remarks: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
)

data class PartCsvExportItemDto(
    @Json(name = "part_cd") val partCd: String? = null,
    @Json(name = "part_name") val partName: String? = null,
)

data class MasterPartBodyDto(
    @Json(name = "part_cd") val partCd: String,
    @Json(name = "part_name") val partName: String,
    val category: String? = null,
    val kind: String? = null,
    @Json(name = "settlement_type") val settlementType: String? = null,
    val uom: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "material_unit_price") val materialUnitPrice: Double? = null,
    val currency: String? = null,
    @Json(name = "exchange_rate") val exchangeRate: Double? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    val status: Int? = null,
    val remarks: String? = null,
)

data class MasterSupplierDto(
    val id: Int? = null,
    @Json(name = "supplier_cd") val supplierCd: String? = null,
    @Json(name = "supplier_name") val supplierName: String? = null,
    @Json(name = "supplier_kana") val supplierKana: String? = null,
    @Json(name = "contact_person") val contactPerson: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val email: String? = null,
    @Json(name = "postal_code") val postalCode: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    @Json(name = "payment_terms") val paymentTerms: String? = null,
    val currency: String? = null,
    val remarks: String? = null,
)

data class MasterSupplierBodyDto(
    @Json(name = "supplier_cd") val supplierCd: String,
    @Json(name = "supplier_name") val supplierName: String,
    @Json(name = "supplier_kana") val supplierKana: String? = null,
    @Json(name = "contact_person") val contactPerson: String? = null,
    val phone: String? = null,
    val fax: String? = null,
    val email: String? = null,
    @Json(name = "postal_code") val postalCode: String? = null,
    val address1: String? = null,
    val address2: String? = null,
    @Json(name = "payment_terms") val paymentTerms: String? = null,
    val currency: String? = null,
    val remarks: String? = null,
)

data class MasterProcessDto(
    val id: Int? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "short_name") val shortName: String? = null,
    val category: String? = null,
    @Json(name = "is_outsource") val isOutsource: Boolean? = null,
    @Json(name = "default_cycle_sec") val defaultCycleSec: Double? = null,
    @Json(name = "default_yield") val defaultYield: Double? = null,
    @Json(name = "capacity_unit") val capacityUnit: String? = null,
    val remark: String? = null,
)

data class MasterProcessBodyDto(
    @Json(name = "process_cd") val processCd: String,
    @Json(name = "process_name") val processName: String,
    @Json(name = "short_name") val shortName: String? = null,
    val category: String? = null,
    @Json(name = "is_outsource") val isOutsource: Boolean? = null,
    @Json(name = "default_cycle_sec") val defaultCycleSec: Double? = null,
    @Json(name = "default_yield") val defaultYield: Double? = null,
    @Json(name = "capacity_unit") val capacityUnit: String? = null,
    val remark: String? = null,
)

data class MasterProcessRouteDto(
    val id: Int? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "route_name") val routeName: String? = null,
    val description: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "is_default") val isDefault: Boolean? = null,
)

data class MasterProcessRouteBodyDto(
    @Json(name = "route_cd") val routeCd: String,
    @Json(name = "route_name") val routeName: String,
    val description: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = true,
    @Json(name = "is_default") val isDefault: Boolean? = false,
)

data class MasterRouteStepDto(
    val id: Int? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "step_no") val stepNo: Int? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "yield_percent") val yieldPercent: Double? = null,
    @Json(name = "cycle_sec") val cycleSec: Double? = null,
    val remarks: String? = null,
)

data class MasterRouteStepBodyDto(
    @Json(name = "step_no") val stepNo: Int,
    @Json(name = "process_cd") val processCd: String,
    @Json(name = "yield_percent") val yieldPercent: Double? = 100.0,
    @Json(name = "cycle_sec") val cycleSec: Double? = 0.0,
    val remarks: String? = null,
)

data class MasterRouteStepUpdateDto(
    @Json(name = "step_no") val stepNo: Int? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "yield_percent") val yieldPercent: Double? = null,
    @Json(name = "cycle_sec") val cycleSec: Double? = null,
    val remarks: String? = null,
)

data class MasterRouteStepOrderItemDto(
    val id: Int,
    @Json(name = "step_no") val stepNo: Int,
)

data class MasterCustomerDto(
    val id: Int? = null,
    @Json(name = "customer_cd") val customerCd: String? = null,
    @Json(name = "customer_name") val customerName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    @Json(name = "customer_type") val customerType: String? = null,
    val status: Int? = null,
)

data class MasterCustomerBodyDto(
    @Json(name = "customer_cd") val customerCd: String,
    @Json(name = "customer_name") val customerName: String,
    val phone: String? = null,
    val address: String? = null,
    @Json(name = "customer_type") val customerType: String? = null,
    val status: Int? = null,
)

data class MasterCarrierDto(
    val id: Int? = null,
    @Json(name = "carrier_cd") val carrierCd: String? = null,
    @Json(name = "carrier_name") val carrierName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val status: Int? = null,
)

data class MasterCarrierBodyDto(
    @Json(name = "carrier_cd") val carrierCd: String,
    @Json(name = "carrier_name") val carrierName: String,
    val phone: String? = null,
    val address: String? = null,
    val status: Int? = null,
)

data class MasterMachineFullDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "machine_type") val machineType: String? = null,
    val status: String? = null,
    val efficiency: Double? = null,
    val remark: String? = null,
)

data class MasterMachineBodyDto(
    @Json(name = "machine_cd") val machineCd: String,
    @Json(name = "machine_name") val machineName: String,
    @Json(name = "machine_type") val machineType: String? = null,
    val status: String? = null,
    val efficiency: Double? = null,
    val remark: String? = null,
)

data class MasterRollerDto(
    val id: Int? = null,
    @Json(name = "roller_cd") val rollerCd: String? = null,
    @Json(name = "roller_name") val rollerName: String? = null,
    @Json(name = "exchange_freq_qty") val exchangeFreqQty: Int? = null,
    @Json(name = "exchange_freq_month") val exchangeFreqMonth: Int? = null,
    @Json(name = "cleaning_freq_month") val cleaningFreqMonth: Int? = null,
    val category: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    val note: String? = null,
)

data class MasterRollerBodyDto(
    @Json(name = "roller_cd") val rollerCd: String,
    @Json(name = "roller_name") val rollerName: String? = null,
    @Json(name = "exchange_freq_qty") val exchangeFreqQty: Int? = null,
    @Json(name = "exchange_freq_month") val exchangeFreqMonth: Int? = null,
    @Json(name = "cleaning_freq_month") val cleaningFreqMonth: Int? = null,
    val category: String? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    val note: String? = null,
)

data class MasterDestinationDto(
    val id: Int? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "destination_name") val destinationName: String? = null,
    @Json(name = "customer_cd") val customerCd: String? = null,
    @Json(name = "carrier_cd") val carrierCd: String? = null,
    @Json(name = "delivery_lead_time") val deliveryLeadTime: Int? = null,
    @Json(name = "issue_type") val issueType: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val status: Int? = null,
)

data class MasterDestinationBodyDto(
    @Json(name = "destination_cd") val destinationCd: String,
    @Json(name = "destination_name") val destinationName: String,
    @Json(name = "customer_cd") val customerCd: String? = null,
    @Json(name = "carrier_cd") val carrierCd: String? = null,
    @Json(name = "delivery_lead_time") val deliveryLeadTime: Int? = null,
    @Json(name = "issue_type") val issueType: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val status: Int? = null,
)

data class MasterProcessingFeeDto(
    val id: Int? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "method_cd") val methodCd: String? = null,
    @Json(name = "method_name") val methodName: String? = null,
    @Json(name = "unit_price") val unitPrice: Double? = null,
    val currency: String? = null,
    @Json(name = "charge_uom") val chargeUom: String? = null,
    val status: String? = null,
    val remarks: String? = null,
)

data class MasterProcessingFeeBodyDto(
    @Json(name = "process_cd") val processCd: String,
    @Json(name = "method_cd") val methodCd: String,
    @Json(name = "method_name") val methodName: String? = null,
    @Json(name = "unit_price") val unitPrice: Double,
    val currency: String? = null,
    @Json(name = "charge_uom") val chargeUom: String? = null,
    val status: String? = null,
    val remarks: String? = null,
)

data class MasterProductRouteInfoDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "route_name") val routeName: String? = null,
    @Json(name = "delivery_destination_name") val deliveryDestinationName: String? = null,
)

data class MasterProductRouteStepDto(
    val id: Int? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "step_no") val stepNo: Int? = null,
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    val machines: List<MasterProductRouteMachineDto>? = null,
)

data class MasterProductRouteMachineDto(
    val id: Int? = null,
    @Json(name = "machine_cd") val machineCd: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "process_time_sec") val processTimeSec: Double? = null,
    @Json(name = "setup_time") val setupTime: Int? = null,
)

data class MasterDestinationHolidayDto(
    val id: Int? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "holiday_date") val holidayDate: String? = null,
)

data class MasterDestinationWorkdayDto(
    val id: Int? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "work_date") val workDate: String? = null,
    val reason: String? = null,
)

data class CompanyWorkCalendarListResponse(
    val success: Boolean? = null,
    val data: CompanyWorkCalendarListDataDto? = null,
)

data class CompanyWorkCalendarListDataDto(
    @Json(name = "start_date") val startDate: String? = null,
    @Json(name = "end_date") val endDate: String? = null,
    val items: List<CompanyWorkCalendarItemDto>? = null,
    @Json(name = "scheduled_workday_count") val scheduledWorkdayCount: Int? = null,
    @Json(name = "total_days") val totalDays: Int? = null,
)

data class CompanyWorkCalendarItemDto(
    val id: Int? = null,
    @Json(name = "calendar_date") val calendarDate: String? = null,
    @Json(name = "day_type") val dayType: String? = null,
    @Json(name = "day_type_label") val dayTypeLabel: String? = null,
    @Json(name = "is_scheduled") val isScheduled: Boolean? = null,
    val name: String? = null,
    val note: String? = null,
)

data class CompanyWorkCalendarDayTypeDto(
    val value: String? = null,
    val label: String? = null,
)

data class CompanyWorkCalendarBatchBodyDto(
    val dates: List<String>,
    @Json(name = "day_type") val dayType: String,
    val name: String? = null,
)

data class CompanyWorkCalendarBatchResponse(
    val success: Boolean? = null,
    val created: Int? = null,
    val skipped: Int? = null,
)

data class MasterOptionDto(
    val cd: String? = null,
    val name: String? = null,
)

data class MasterProductBodyDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String,
    @Json(name = "product_type") val productType: String? = null,
    @Json(name = "part_number") val partNumber: String? = null,
    @Json(name = "product_alias") val productAlias: String? = null,
    val category: String? = null,
    val kind: String? = null,
    val priority: Int? = 2,
    val status: String? = "active",
    @Json(name = "unit_price") val unitPrice: Double? = null,
    @Json(name = "process_count") val processCount: Int? = 1,
    @Json(name = "is_multistage") val isMultistage: Boolean? = true,
    @Json(name = "lead_time") val leadTime: Int? = null,
    @Json(name = "safety_days") val safetyDays: Int? = null,
    @Json(name = "lot_size") val lotSize: Int? = 1,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "box_type") val boxType: String? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
    val dimensions: String? = null,
    val weight: Double? = null,
    @Json(name = "destination_cd") val destinationCd: String? = null,
    @Json(name = "vehicle_model") val vehicleModel: String? = null,
    @Json(name = "location_cd") val locationCd: String? = null,
    @Json(name = "start_use_date") val startUseDate: String? = null,
    @Json(name = "material_cd") val materialCd: String? = null,
    @Json(name = "cut_length") val cutLength: Double? = null,
    @Json(name = "chamfer_length") val chamferLength: Double? = null,
    @Json(name = "developed_length") val developedLength: Double? = null,
    @Json(name = "scrap_length") val scrapLength: Double? = null,
    @Json(name = "take_count") val takeCount: Int? = null,
    val note: String? = null,
)

data class MasterBatchDeleteBodyDto(
    val ids: List<Int>,
)

data class ProductCsvExportItemDto(
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "unit_per_box") val unitPerBox: Int? = null,
)

data class ProductCsvExportResultDto(
    val success: Boolean? = null,
    val message: String? = null,
    @Json(name = "fileName") val fileName: String? = null,
    @Json(name = "csvFilePath") val csvFilePath: String? = null,
    @Json(name = "rowCount") val rowCount: Int? = null,
)
