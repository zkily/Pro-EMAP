package com.example.smart_emap.data.model

import com.squareup.moshi.Json

// ── production_summarys 一覧 ────────────────────────────────────────────────

data class ProductionSummaryPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 50,
)

data class ProductionSummaryListPageData(
    val list: List<ProductionSummaryFullRowDto>? = null,
    val pagination: ProductionSummaryPaginationDto? = null,
)

data class ProductionSummaryListPageResponse(
    val data: ProductionSummaryListPageData? = null,
    val message: String? = null,
)

data class ProductionSummaryProductOptionDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
)

data class ProductionSummaryProductsResponse(
    val data: List<ProductionSummaryProductOptionDto>? = null,
)

data class ProductionSummaryFullRowDto(
    val id: Int? = null,
    val date: String? = null,
    @Json(name = "day_of_week") val dayOfWeek: String? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "order_quantity") val orderQuantity: Int? = null,
    @Json(name = "forecast_quantity") val forecastQuantity: Int? = null,
    @Json(name = "safety_stock") val safetyStock: Int? = null,
    @Json(name = "cutting_carry_over") val cuttingCarryOver: Int? = null,
    @Json(name = "cutting_actual") val cuttingActual: Int? = null,
    @Json(name = "cutting_defect") val cuttingDefect: Int? = null,
    @Json(name = "cutting_scrap") val cuttingScrap: Int? = null,
    @Json(name = "cutting_on_hold") val cuttingOnHold: Int? = null,
    @Json(name = "cutting_inventory") val cuttingInventory: Int? = null,
    @Json(name = "cutting_trend") val cuttingTrend: Int? = null,
    @Json(name = "cutting_production_date") val cuttingProductionDate: String? = null,
    @Json(name = "cutting_machine") val cuttingMachine: String? = null,
    @Json(name = "cutting_plan") val cuttingPlan: Int? = null,
    @Json(name = "cutting_actual_plan") val cuttingActualPlan: Int? = null,
    @Json(name = "cutting_actual_plan_trend") val cuttingActualPlanTrend: Int? = null,
    @Json(name = "chamfering_carry_over") val chamferingCarryOver: Int? = null,
    @Json(name = "chamfering_actual") val chamferingActual: Int? = null,
    @Json(name = "chamfering_defect") val chamferingDefect: Int? = null,
    @Json(name = "chamfering_scrap") val chamferingScrap: Int? = null,
    @Json(name = "chamfering_on_hold") val chamferingOnHold: Int? = null,
    @Json(name = "chamfering_inventory") val chamferingInventory: Int? = null,
    @Json(name = "chamfering_trend") val chamferingTrend: Int? = null,
    @Json(name = "chamfering_production_date") val chamferingProductionDate: String? = null,
    @Json(name = "chamfering_machine") val chamferingMachine: String? = null,
    @Json(name = "sw_machine") val swMachine: String? = null,
    @Json(name = "sw_plan") val swPlan: Int? = null,
    @Json(name = "chamfering_plan") val chamferingPlan: Int? = null,
    @Json(name = "chamfering_actual_plan") val chamferingActualPlan: Int? = null,
    @Json(name = "chamfering_actual_plan_trend") val chamferingActualPlanTrend: Int? = null,
    @Json(name = "molding_carry_over") val moldingCarryOver: Int? = null,
    @Json(name = "molding_actual") val moldingActual: Int? = null,
    @Json(name = "molding_defect") val moldingDefect: Int? = null,
    @Json(name = "molding_scrap") val moldingScrap: Int? = null,
    @Json(name = "molding_on_hold") val moldingOnHold: Int? = null,
    @Json(name = "molding_inventory") val moldingInventory: Int? = null,
    @Json(name = "molding_trend") val moldingTrend: Int? = null,
    @Json(name = "molding_production_date") val moldingProductionDate: String? = null,
    @Json(name = "molding_machine") val moldingMachine: String? = null,
    @Json(name = "molding_plan") val moldingPlan: Int? = null,
    @Json(name = "molding_actual_plan") val moldingActualPlan: Int? = null,
    @Json(name = "molding_actual_plan_trend") val moldingActualPlanTrend: Int? = null,
    @Json(name = "pre_molding_inventory") val preMoldingInventory: Int? = null,
    @Json(name = "pre_molding_prev_process") val preMoldingPrevProcess: String? = null,
    @Json(name = "plating_carry_over") val platingCarryOver: Int? = null,
    @Json(name = "plating_actual") val platingActual: Int? = null,
    @Json(name = "plating_defect") val platingDefect: Int? = null,
    @Json(name = "plating_scrap") val platingScrap: Int? = null,
    @Json(name = "plating_on_hold") val platingOnHold: Int? = null,
    @Json(name = "plating_inventory") val platingInventory: Int? = null,
    @Json(name = "plating_trend") val platingTrend: Int? = null,
    @Json(name = "plating_production_date") val platingProductionDate: String? = null,
    @Json(name = "plating_machine") val platingMachine: String? = null,
    @Json(name = "plating_plan") val platingPlan: Int? = null,
    @Json(name = "plating_actual_plan") val platingActualPlan: Int? = null,
    @Json(name = "plating_actual_plan_trend") val platingActualPlanTrend: Int? = null,
    @Json(name = "pre_plating_inventory") val prePlatingInventory: Int? = null,
    @Json(name = "pre_plating_prev_process") val prePlatingPrevProcess: String? = null,
    @Json(name = "welding_carry_over") val weldingCarryOver: Int? = null,
    @Json(name = "welding_actual") val weldingActual: Int? = null,
    @Json(name = "welding_defect") val weldingDefect: Int? = null,
    @Json(name = "welding_scrap") val weldingScrap: Int? = null,
    @Json(name = "welding_on_hold") val weldingOnHold: Int? = null,
    @Json(name = "welding_inventory") val weldingInventory: Int? = null,
    @Json(name = "welding_trend") val weldingTrend: Int? = null,
    @Json(name = "welding_production_date") val weldingProductionDate: String? = null,
    @Json(name = "welding_machine") val weldingMachine: String? = null,
    @Json(name = "welding_plan") val weldingPlan: Int? = null,
    @Json(name = "welding_actual_plan") val weldingActualPlan: Int? = null,
    @Json(name = "welding_actual_plan_trend") val weldingActualPlanTrend: Int? = null,
    @Json(name = "pre_welding_inventory") val preWeldingInventory: Int? = null,
    @Json(name = "pre_welding_prev_process") val preWeldingPrevProcess: String? = null,
    @Json(name = "inspection_carry_over") val inspectionCarryOver: Int? = null,
    @Json(name = "inspection_actual") val inspectionActual: Int? = null,
    @Json(name = "inspection_defect") val inspectionDefect: Int? = null,
    @Json(name = "inspection_scrap") val inspectionScrap: Int? = null,
    @Json(name = "inspection_on_hold") val inspectionOnHold: Int? = null,
    @Json(name = "inspection_inventory") val inspectionInventory: Int? = null,
    @Json(name = "inspection_trend") val inspectionTrend: Int? = null,
    @Json(name = "inspection_production_date") val inspectionProductionDate: String? = null,
    @Json(name = "inspector_machine") val inspectorMachine: String? = null,
    @Json(name = "inspection_plan") val inspectionPlan: Int? = null,
    @Json(name = "inspection_actual_plan") val inspectionActualPlan: Int? = null,
    @Json(name = "inspection_actual_plan_trend") val inspectionActualPlanTrend: Int? = null,
    @Json(name = "warehouse_carry_over") val warehouseCarryOver: Int? = null,
    @Json(name = "warehouse_actual") val warehouseActual: Int? = null,
    @Json(name = "warehouse_defect") val warehouseDefect: Int? = null,
    @Json(name = "warehouse_scrap") val warehouseScrap: Int? = null,
    @Json(name = "warehouse_on_hold") val warehouseOnHold: Int? = null,
    @Json(name = "warehouse_inventory") val warehouseInventory: Int? = null,
    @Json(name = "warehouse_trend") val warehouseTrend: Int? = null,
    @Json(name = "outsourced_warehouse_carry_over") val outsourcedWarehouseCarryOver: Int? = null,
    @Json(name = "outsourced_warehouse_actual") val outsourcedWarehouseActual: Int? = null,
    @Json(name = "outsourced_warehouse_defect") val outsourcedWarehouseDefect: Int? = null,
    @Json(name = "outsourced_warehouse_scrap") val outsourcedWarehouseScrap: Int? = null,
    @Json(name = "outsourced_warehouse_on_hold") val outsourcedWarehouseOnHold: Int? = null,
    @Json(name = "outsourced_warehouse_inventory") val outsourcedWarehouseInventory: Int? = null,
    @Json(name = "outsourced_warehouse_trend") val outsourcedWarehouseTrend: Int? = null,
    @Json(name = "outsourced_warehouse_plan") val outsourcedWarehousePlan: Int? = null,
    @Json(name = "outsourced_plating_carry_over") val outsourcedPlatingCarryOver: Int? = null,
    @Json(name = "outsourced_plating_actual") val outsourcedPlatingActual: Int? = null,
    @Json(name = "outsourced_plating_defect") val outsourcedPlatingDefect: Int? = null,
    @Json(name = "outsourced_plating_scrap") val outsourcedPlatingScrap: Int? = null,
    @Json(name = "outsourced_plating_on_hold") val outsourcedPlatingOnHold: Int? = null,
    @Json(name = "outsourced_plating_inventory") val outsourcedPlatingInventory: Int? = null,
    @Json(name = "outsourced_plating_trend") val outsourcedPlatingTrend: Int? = null,
    @Json(name = "outsourced_plating_plan") val outsourcedPlatingPlan: Int? = null,
    @Json(name = "outsourced_plating_actual_plan_trend") val outsourcedPlatingActualPlanTrend: Int? = null,
    @Json(name = "outsourced_welding_carry_over") val outsourcedWeldingCarryOver: Int? = null,
    @Json(name = "outsourced_welding_actual") val outsourcedWeldingActual: Int? = null,
    @Json(name = "outsourced_welding_defect") val outsourcedWeldingDefect: Int? = null,
    @Json(name = "outsourced_welding_scrap") val outsourcedWeldingScrap: Int? = null,
    @Json(name = "outsourced_welding_on_hold") val outsourcedWeldingOnHold: Int? = null,
    @Json(name = "outsourced_welding_inventory") val outsourcedWeldingInventory: Int? = null,
    @Json(name = "outsourced_welding_trend") val outsourcedWeldingTrend: Int? = null,
    @Json(name = "outsourced_welding_plan") val outsourcedWeldingPlan: Int? = null,
    @Json(name = "outsourced_welding_actual_plan_trend") val outsourcedWeldingActualPlanTrend: Int? = null,
    @Json(name = "pre_welding_inspection_carry_over") val preWeldingInspectionCarryOver: Int? = null,
    @Json(name = "pre_welding_inspection_actual") val preWeldingInspectionActual: Int? = null,
    @Json(name = "pre_welding_inspection_defect") val preWeldingInspectionDefect: Int? = null,
    @Json(name = "pre_welding_inspection_scrap") val preWeldingInspectionScrap: Int? = null,
    @Json(name = "pre_welding_inspection_on_hold") val preWeldingInspectionOnHold: Int? = null,
    @Json(name = "pre_welding_inspection_inventory") val preWeldingInspectionInventory: Int? = null,
    @Json(name = "pre_welding_inspection_trend") val preWeldingInspectionTrend: Int? = null,
    @Json(name = "pre_inspection_carry_over") val preInspectionCarryOver: Int? = null,
    @Json(name = "pre_inspection_actual") val preInspectionActual: Int? = null,
    @Json(name = "pre_inspection_scrap") val preInspectionScrap: Int? = null,
    @Json(name = "pre_inspection_inventory") val preInspectionInventory: Int? = null,
    @Json(name = "pre_inspection_trend") val preInspectionTrend: Int? = null,
    @Json(name = "pre_outsourcing_carry_over") val preOutsourcingCarryOver: Int? = null,
    @Json(name = "pre_outsourcing_actual") val preOutsourcingActual: Int? = null,
    @Json(name = "pre_outsourcing_scrap") val preOutsourcingScrap: Int? = null,
    @Json(name = "pre_outsourcing_inventory") val preOutsourcingInventory: Int? = null,
    @Json(name = "pre_outsourcing_trend") val preOutsourcingTrend: Int? = null,
)

data class InventoryStagnationRowDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "route_cd") val routeCd: String? = null,
    @Json(name = "inventory_column") val inventoryColumn: String,
    @Json(name = "stable_quantity") val stableQuantity: Int,
    @Json(name = "period_start") val periodStart: String,
    @Json(name = "period_end") val periodEnd: String,
    val days: Int = 0,
)

data class InventoryStagnationDataDto(
    val list: List<InventoryStagnationRowDto>? = null,
    val total: Int = 0,
    @Json(name = "as_of") val asOf: String? = null,
    @Json(name = "period_start") val periodStart: String? = null,
    @Json(name = "period_end") val periodEnd: String? = null,
    @Json(name = "min_quantity") val minQuantity: Int? = null,
    @Json(name = "stable_calendar_days") val stableCalendarDays: Int? = null,
)

data class InventoryStagnationResponse(
    val data: InventoryStagnationDataDto? = null,
)

data class SimpleMessageResponse(
    val message: String? = null,
)

data class BatchUpdateLockBody(
    @Json(name = "lockValue") val lockValue: String,
    @Json(name = "ttlSeconds") val ttlSeconds: Int? = null,
)

data class BatchUpdateLockResponse(
    val data: BatchUpdateLockData? = null,
    val message: String? = null,
)

data class BatchUpdateLockData(
    val acquired: Boolean? = null,
    val released: Boolean? = null,
)

data class GenerateProductionSummaryBody(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
)

data class UpdateFromOrderDailyBody(
    @Json(name = "updateMode") val updateMode: String? = null,
    val days: Int? = null,
    @Json(name = "clearBeforeUpdate") val clearBeforeUpdate: Boolean? = null,
)

data class StartDateBody(
    @Json(name = "startDate") val startDate: String,
)

data class InventoryTrendCalcStartDateData(
    @Json(name = "startDate") val startDate: String? = null,
    val source: String? = null,
)

data class InventoryTrendCalcStartDateResponse(
    val data: InventoryTrendCalcStartDateData? = null,
    val message: String? = null,
)

data class StockTransactionLogRowDto(
    val id: Int = 0,
    @Json(name = "target_cd") val targetCd: String? = null,
    val quantity: Int? = null,
    @Json(name = "transaction_time") val transactionTime: String? = null,
)

data class StockTransactionLogListData(
    val list: List<StockTransactionLogRowDto>? = null,
)

data class StockTransactionLogListResponse(
    val data: StockTransactionLogListData? = null,
    val list: List<StockTransactionLogRowDto>? = null,
)

data class StockTransactionLogBody(
    @Json(name = "transaction_time") val transactionTime: String,
    @Json(name = "transaction_type") val transactionType: String,
    @Json(name = "target_cd") val targetCd: String,
    val quantity: Int,
    @Json(name = "stock_type") val stockType: String,
    @Json(name = "location_cd") val locationCd: String,
    val unit: String = "本",
    @Json(name = "process_cd") val processCd: String? = null,
    @Json(name = "source_file") val sourceFile: String = "生産データ管理",
)

data class BatchActualTransaction(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "process_cd") val processCd: String,
    val quantity: Int,
    @Json(name = "transaction_time") val transactionTime: String,
)

data class BatchActualBody(
    val transactions: List<BatchActualTransaction>,
)

data class ProductProcessBomRowDto(
    @Json(name = "product_cd") val productCd: Int? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "safety_stock_days") val safetyStockDays: Int? = null,
    @Json(name = "forming_process_lt") val formingProcessLt: Int? = null,
    @Json(name = "welding_process_lt") val weldingProcessLt: Int? = null,
)

data class ProductProcessBomListData(
    val list: List<ProductProcessBomRowDto>? = null,
    val total: Int? = null,
)

data class ProductProcessBomListResponse(
    val data: ProductProcessBomListData? = null,
    val list: List<ProductProcessBomRowDto>? = null,
) {
    fun items(): List<ProductProcessBomRowDto> = data?.list.orEmpty().ifEmpty { list.orEmpty() }
}

data class UpdateProductProcessBomBody(
    @Json(name = "safety_stock_days") val safetyStockDays: Int? = null,
    @Json(name = "forming_process_lt") val formingProcessLt: Int? = null,
    @Json(name = "welding_process_lt") val weldingProcessLt: Int? = null,
)

data class ProductMachineConfigUpdateBody(
    @Json(name = "molding_machine") val moldingMachine: String? = null,
    @Json(name = "welding_machine") val weldingMachine: String? = null,
)

data class ClearPlanFieldsData(
    val cleared: Int? = null,
)

data class ClearPlanFieldsResponse(
    val message: String? = null,
    val data: ClearPlanFieldsData? = null,
)

// ── 工程別設備別計画 ────────────────────────────────────────────────────────

data class ProcessMachineDailyCellDto(
    val plan: Int = 0,
    val actual: Int = 0,
    val diff: Int = 0,
)

data class ProcessMachineMetricsDto(
    val plan: Int = 0,
    val actual: Int = 0,
    @Json(name = "actual_plan") val actualPlan: Int = 0,
    val defect: Int = 0,
    val scrap: Int = 0,
    val diff: Int = 0,
    @Json(name = "achievement_rate") val achievementRate: Double? = null,
    @Json(name = "defect_rate") val defectRate: Double? = null,
    val days: Int = 0,
)

data class ProcessMachinePlanRowDto(
    @Json(name = "process_key") val processKey: String,
    @Json(name = "process_label") val processLabel: String,
    val machine: String,
    val plan: Int = 0,
    val actual: Int = 0,
    @Json(name = "actual_plan") val actualPlan: Int = 0,
    val defect: Int = 0,
    val scrap: Int = 0,
    val diff: Int = 0,
    @Json(name = "achievement_rate") val achievementRate: Double? = null,
    @Json(name = "defect_rate") val defectRate: Double? = null,
    val days: Int = 0,
    val daily: Map<String, ProcessMachineDailyCellDto> = emptyMap(),
)

data class ProcessMachineProcessTotalDto(
    @Json(name = "process_key") val processKey: String,
    @Json(name = "process_label") val processLabel: String,
    val plan: Int = 0,
    val actual: Int = 0,
    @Json(name = "actual_plan") val actualPlan: Int = 0,
    val defect: Int = 0,
    val scrap: Int = 0,
    val diff: Int = 0,
    @Json(name = "achievement_rate") val achievementRate: Double? = null,
    @Json(name = "defect_rate") val defectRate: Double? = null,
    val days: Int = 0,
)

data class ProcessMachinePlanDataDto(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    val dates: List<String> = emptyList(),
    val processes: List<ProcessMachineProcessOptionDto> = emptyList(),
    val summary: List<ProcessMachinePlanRowDto> = emptyList(),
    @Json(name = "processTotals") val processTotals: Map<String, ProcessMachineProcessTotalDto> = emptyMap(),
    @Json(name = "grandTotal") val grandTotal: ProcessMachineMetricsDto? = null,
)

data class ProcessMachineProcessOptionDto(
    val key: String,
    val label: String,
)

data class ProcessMachinePlanResponse(
    val data: ProcessMachinePlanDataDto? = null,
)

data class ProcessMachineProductRowDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    val plan: Int = 0,
    val actual: Int = 0,
    @Json(name = "actual_plan") val actualPlan: Int = 0,
    val defect: Int = 0,
    val scrap: Int = 0,
    val diff: Int = 0,
    @Json(name = "achievement_rate") val achievementRate: Double? = null,
    @Json(name = "defect_rate") val defectRate: Double? = null,
    val days: Int = 0,
)

data class ProcessMachineProductsDataDto(
    @Json(name = "startDate") val startDate: String,
    @Json(name = "endDate") val endDate: String,
    @Json(name = "process_key") val processKey: String,
    @Json(name = "process_label") val processLabel: String,
    val machine: String,
    val products: List<ProcessMachineProductRowDto> = emptyList(),
    val total: ProcessMachineMetricsDto? = null,
)

data class ProcessMachineProductsResponse(
    val data: ProcessMachineProductsDataDto? = null,
)

data class PrevCarryWipTotalDataDto(
    val total: Int = 0,
    @Json(name = "pre_welding_total") val preWeldingTotal: Int = 0,
    @Json(name = "pre_inspection_total") val preInspectionTotal: Int = 0,
    @Json(name = "as_of_date") val asOfDate: String? = null,
)

data class PrevCarryWipTotalResponse(
    val data: PrevCarryWipTotalDataDto? = null,
)

data class PrevCarryBreakdownItemDto(
    @Json(name = "product_cd") val productCd: String,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "route_cd") val routeCd: String,
    val quantity: Int = 0,
)

data class PrevCarryBreakdownDataDto(
    val month: String,
    val column: String,
    val items: List<PrevCarryBreakdownItemDto> = emptyList(),
    val total: Int = 0,
)

data class PrevCarryBreakdownResponse(
    val data: PrevCarryBreakdownDataDto? = null,
)

// ── 計画ベースライン ────────────────────────────────────────────────────────

data class PlanBaselineFullComparisonItemDto(
    @Json(name = "plan_date") val planDate: String? = null,
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "baseline_plan") val baselinePlan: Double? = null,
    @Json(name = "current_plan") val currentPlan: Double? = null,
    @Json(name = "plan_diff") val planDiff: Double? = null,
    @Json(name = "current_actual") val currentActual: Double? = null,
    @Json(name = "actual_diff") val actualDiff: Double? = null,
)

data class PlanBaselineFullComparisonResultDto(
    val success: Boolean? = null,
    @Json(name = "baselineMonth") val baselineMonth: String? = null,
    val summary: PlanBaselineComparisonDto? = null,
    val items: List<PlanBaselineFullComparisonItemDto>? = null,
)

data class PlanBaselineRecordDto(
    @Json(name = "plan_date") val planDate: String,
    @Json(name = "process_name") val processName: String,
    @Json(name = "plan_quantity") val planQuantity: Double,
    @Json(name = "actual_quantity") val actualQuantity: Double? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "product_name") val productName: String? = null,
)

data class PlanBaselineGenerateBody(
    @Json(name = "baselineMonth") val baselineMonth: String,
    @Json(name = "processName") val processName: String? = null,
    @Json(name = "weekdayBaseline") val weekdayBaseline: Double? = null,
    @Json(name = "saturdayBaseline") val saturdayBaseline: Double? = null,
    @Json(name = "sundayBaseline") val sundayBaseline: Double? = null,
)

data class PlanBaselinePlanQuantityBody(
    @Json(name = "baselineMonth") val baselineMonth: String,
    @Json(name = "planDate") val planDate: String,
    @Json(name = "processName") val processName: String? = null,
    @Json(name = "planQuantity") val planQuantity: Double,
)

data class PlanOperationRateRowDto(
    val id: Int? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    @Json(name = "display_process") val displayProcess: String? = null,
    @Json(name = "operation_variance") val operationVariance: String? = null,
    @Json(name = "display_month") val displayMonth: String? = null,
)

data class PlanOperationRateResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val items: List<PlanOperationRateRowDto>? = null,
)

// ── 生産スケジュール plan-data ──────────────────────────────────────────────

data class PlanUpdateRecordDto(
    @Json(name = "process_name") val processName: String? = null,
    @Json(name = "machine_name") val machineName: String? = null,
    val operator: String? = null,
    @Json(name = "plan_date") val planDate: String? = null,
    val quantity: Int? = null,
    @Json(name = "efficiency_rate") val efficiencyRate: Double? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "product_cd") val productCd: String? = null,
    @Json(name = "setup_time") val setupTime: Int? = null,
)

data class PlanDataRecordsPack(
    val records: List<PlanUpdateRecordDto>? = null,
    val total: Int? = null,
)

data class PlanDataListResponse(
    val data: PlanDataRecordsPack? = null,
    val list: List<PlanUpdateRecordDto>? = null,
) {
    fun records(): List<PlanUpdateRecordDto> = data?.records.orEmpty().ifEmpty { list.orEmpty() }
}
