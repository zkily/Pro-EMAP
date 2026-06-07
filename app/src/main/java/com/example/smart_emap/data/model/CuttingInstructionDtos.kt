package com.example.smart_emap.data.model



import com.squareup.moshi.Json



data class PlanListResponse<T>(

    val success: Boolean? = null,

    val data: List<T>? = null,

    val message: String? = null,

)



data class InstructionPlanRowDto(

    val id: Int? = null,

    @Json(name = "production_month") val productionMonth: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "priority_order") val priorityOrder: Int? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "planned_quantity") val plannedQuantity: Int? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "scrap_length") val scrapLength: Double? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "material_manufacturer") val materialManufacturer: String? = null,

    @Json(name = "standard_specification") val standardSpecification: String? = null,

    @Json(name = "use_material_stock_sub") val useMaterialStockSub: Int? = null,

    @Json(name = "usage_count") val usageCount: Double? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Int? = null,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "is_cutting_instructed") val isCuttingInstructed: Int? = null,

    @Json(name = "is_chamfering_instructed") val isChamferingInstructed: Int? = null,

    @Json(name = "is_sw_instructed") val isSwInstructed: Int? = null,

)



data class InstructionCuttingRowDto(

    val id: Int? = null,

    val cd: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "cutting_machine") val cuttingMachine: String? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "defect_qty") val defectQty: Int? = null,

    @Json(name = "production_completed_check") val productionCompletedCheck: Int? = null,

    @Json(name = "production_sequence") val productionSequence: Int? = null,

    @Json(name = "production_time") val productionTime: String? = null,

    val remarks: String? = null,

    @Json(name = "use_material_stock_sub") val useMaterialStockSub: Int? = null,

    @Json(name = "usage_count") val usageCount: Double? = null,

    @Json(name = "material_usage_reflected") val materialUsageReflected: String? = null,

)



data class InstructionChamferingRowDto(

    val id: Int? = null,

    val cd: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "chamfering_machine") val chamferingMachine: String? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "defect_qty") val defectQty: Int? = null,

    @Json(name = "production_completed_check") val productionCompletedCheck: Int? = null,

    @Json(name = "production_sequence") val productionSequence: Int? = null,

    @Json(name = "production_time") val productionTime: String? = null,

    @Json(name = "no_count") val noCount: Int? = null,

    val remarks: String? = null,

)



data class InstructionChamferingPlanRowDto(

    val id: Int? = null,

    @Json(name = "cutting_management_id") val cuttingManagementId: Int? = null,

    @Json(name = "production_month") val productionMonth: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "production_order") val productionOrder: Int? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = null,

)



data class KanbanIssuanceRowDto(

    val id: Int? = null,

    @Json(name = "process_type") val processType: String? = null,

    @Json(name = "source_id") val sourceId: Int? = null,

    @Json(name = "kanban_no") val kanbanNo: String? = null,

    @Json(name = "issue_date") val issueDate: String? = null,

    val status: String? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "cutting_machine") val cuttingMachine: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "standard_specification") val standardSpecification: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

    @Json(name = "planned_quantity") val plannedQuantity: Int? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Boolean? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

)



data class ProductBatchDetailDto(

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "lot_size") val lotSize: Int? = null,

    @Json(name = "material_cd") val materialCd: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cut_length") val cutLength: Double? = null,

    @Json(name = "chamfer_length") val chamferLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "scrap_length") val scrapLength: Double? = null,

)



data class ProductBatchDetailResponse(

    val success: Boolean? = null,

    val data: ProductBatchDetailDto? = null,

    val message: String? = null,

)



data class EquipmentEfficiencyRowDto(

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "machine_cd") val machineCd: String? = null,

    @Json(name = "machines_name") val machinesName: String? = null,

    @Json(name = "efficiency_rate") val efficiencyRate: Double? = null,

    val status: Int? = null,

)



data class EquipmentEfficiencyPagedData(

    val list: List<EquipmentEfficiencyRowDto>? = null,

)



data class EquipmentEfficiencyListResponse(

    val success: Boolean? = null,

    val data: EquipmentEfficiencyPagedData? = null,

    val list: List<EquipmentEfficiencyRowDto>? = null,

) {

    fun items(): List<EquipmentEfficiencyRowDto> =

        list.orEmpty().ifEmpty { data?.list.orEmpty() }

}



data class ProductionSummaryRowDto(

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "pre_molding_inventory") val preMoldingInventory: Int? = null,

    @Json(name = "pre_molding_prev_process") val preMoldingPrevProcess: String? = null,

)



data class ProductionSummaryListData(

    val list: List<ProductionSummaryRowDto>? = null,

)



data class ProductionSummaryListResponse(

    val data: ProductionSummaryListData? = null,

)



data class ProductByProcessDto(

    @Json(name = "product_cd") val productCd: String,

    @Json(name = "product_name") val productName: String? = null,

)



data class ProductMachineConfigRowDto(

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "molding_machine") val moldingMachine: String? = null,

)



data class CuttingInstructionNoteDto(

    val id: Int? = null,

    val content: String? = null,

    @Json(name = "is_done") val isDone: Boolean? = null,

    @Json(name = "created_at") val createdAt: String? = null,

)



data class CuttingInstructionNotesResponse(

    val success: Boolean? = null,

    val data: List<CuttingInstructionNoteDto>? = null,

)



data class ConfirmActualResponse(

    val success: Boolean? = null,

    val message: String? = null,

    val detail: String? = null,

    val inserted: Int? = null,

    @Json(name = "total_quantity") val totalQuantity: Int? = null,

)



data class MaterialUsageReflectedResponse(

    val success: Boolean? = null,

    val date: String? = null,

    val reflected: Boolean? = null,

)



data class MaterialUsageReflectedCodesResponse(

    val success: Boolean? = null,

    val data: List<String>? = null,

    val codes: List<String>? = null,

)



data class KanbanProductNamesResponse(

    val success: Boolean? = null,

    val data: List<String>? = null,

)



data class MoveFromBatchBody(

    @Json(name = "plan_id") val planId: Int,

    @Json(name = "production_month") val productionMonth: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "production_day") val productionDay: String,

    @Json(name = "priority_order") val priorityOrder: Int? = null,

    @Json(name = "cutting_machine") val cuttingMachine: String? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Int? = null,

)



data class MoveCuttingToBatchBody(

    @Json(name = "cutting_id") val cuttingId: Int,

    @Json(name = "production_month") val productionMonth: String,

    @Json(name = "production_line") val productionLine: String,

    @Json(name = "product_cd") val productCd: String,

    @Json(name = "product_name") val productName: String,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "production_order") val productionOrder: Int? = null,

)



data class CreateInstructionPlanBody(

    @Json(name = "production_month") val productionMonth: String,

    @Json(name = "production_line") val productionLine: String = "",

    @Json(name = "priority_order") val priorityOrder: Int? = 0,

    @Json(name = "product_cd") val productCd: String,

    @Json(name = "product_name") val productName: String,

    @Json(name = "planned_quantity") val plannedQuantity: Int? = 0,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = 0,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "scrap_length") val scrapLength: Double? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "material_manufacturer") val materialManufacturer: String? = null,

    @Json(name = "standard_specification") val standardSpecification: String? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Int? = 0,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = 0,

    @Json(name = "use_material_stock_sub") val useMaterialStockSub: Int? = 0,

    @Json(name = "usage_count") val usageCount: Double? = 1.0,

)



data class PatchInstructionPlanBody(

    @Json(name = "production_month") val productionMonth: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "priority_order") val priorityOrder: Int? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "planned_quantity") val plannedQuantity: Int? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "material_manufacturer") val materialManufacturer: String? = null,

    @Json(name = "standard_specification") val standardSpecification: String? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "scrap_length") val scrapLength: Double? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Int? = null,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = null,

    @Json(name = "is_cutting_instructed") val isCuttingInstructed: Int? = null,

    @Json(name = "is_chamfering_instructed") val isChamferingInstructed: Int? = null,

    @Json(name = "is_sw_instructed") val isSwInstructed: Int? = null,

    @Json(name = "use_material_stock_sub") val useMaterialStockSub: Int? = null,

    @Json(name = "usage_count") val usageCount: Double? = null,

)



data class PatchInstructionCuttingBody(

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "cutting_machine") val cuttingMachine: String? = null,

    @Json(name = "production_sequence") val productionSequence: Int? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "defect_qty") val defectQty: Int? = null,

    @Json(name = "production_completed_check") val productionCompletedCheck: Boolean? = null,

    @Json(name = "remarks") val remarks: String? = null,

    @Json(name = "use_material_stock_sub") val useMaterialStockSub: Int? = null,

    @Json(name = "usage_count") val usageCount: Double? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

)



data class CreateChamferingPlanBody(

    @Json(name = "production_month") val productionMonth: String,

    @Json(name = "production_day") val productionDay: String,

    @Json(name = "production_line") val productionLine: String,

    @Json(name = "production_order") val productionOrder: Int? = null,

    @Json(name = "product_cd") val productCd: String,

    @Json(name = "product_name") val productName: String,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = 0,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = 0,

)



data class MoveChamferingPlanBody(

    @Json(name = "chamfering_plan_id") val chamferingPlanId: Int,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "production_line_2") val productionLine2: String? = null,

)



data class PatchChamferingPlanBody(

    @Json(name = "has_sw_process") val hasSwProcess: Int? = null,

)



data class UpdateChamferingPlanContentBody(

    @Json(name = "production_month") val productionMonth: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "production_order") val productionOrder: Int? = null,

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "has_sw_process") val hasSwProcess: Int? = null,

)



data class CreateChamferingManagementBody(

    @Json(name = "production_day") val productionDay: String,

    @Json(name = "production_line") val productionLine: String = "",

    @Json(name = "chamfering_machine") val chamferingMachine: String,

    @Json(name = "product_cd") val productCd: String,

    @Json(name = "product_name") val productName: String,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = 0,

    @Json(name = "production_sequence") val productionSequence: Int? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    val remarks: String? = null,

)



data class PatchKanbanBody(

    @Json(name = "product_cd") val productCd: String? = null,

    @Json(name = "product_name") val productName: String? = null,

    @Json(name = "production_line") val productionLine: String? = null,

    @Json(name = "cutting_machine") val cuttingMachine: String? = null,

    @Json(name = "material_name") val materialName: String? = null,

    @Json(name = "standard_specification") val standardSpecification: String? = null,

    @Json(name = "management_code") val managementCode: String? = null,

    @Json(name = "start_date") val startDate: String? = null,

    @Json(name = "end_date") val endDate: String? = null,

    @Json(name = "planned_quantity") val plannedQuantity: Int? = null,

    @Json(name = "production_lot_size") val productionLotSize: Int? = null,

    @Json(name = "actual_production_quantity") val actualProductionQuantity: Int? = null,

    @Json(name = "take_count") val takeCount: Int? = null,

    @Json(name = "cutting_length") val cuttingLength: Double? = null,

    @Json(name = "chamfering_length") val chamferingLength: Double? = null,

    @Json(name = "developed_length") val developedLength: Double? = null,

    @Json(name = "has_chamfering_process") val hasChamferingProcess: Boolean? = null,

    @Json(name = "lot_number") val lotNumber: String? = null,

    @Json(name = "production_day") val productionDay: String? = null,

)



data class KanbanBatchIssueBody(

    @Json(name = "kanban_ids") val kanbanIds: List<Int>,

)



data class MaterialUsageCommitBody(

    @Json(name = "today_date") val todayDate: String,

    @Json(name = "tomorrow_date") val tomorrowDate: String,

    @Json(name = "source") val source: String = "cutting_management",

)



data class CreateNoteBody(

    val content: String,

)



data class PatchNoteBody(

    @Json(name = "is_done") val isDone: Boolean? = null,

    val content: String? = null,

)


