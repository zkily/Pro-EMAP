package com.example.smart_emap.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.smart_emap.ui.aps.scheduling.SchedulingViewModel
import com.example.smart_emap.ui.dashboard.DashboardViewModel
import com.example.smart_emap.ui.erp.order.OrderDailyViewModel
import com.example.smart_emap.ui.erp.order.OrderDestinationHistoryViewModel
import com.example.smart_emap.ui.erp.order.OrderHomeViewModel
import com.example.smart_emap.ui.erp.order.OrderMonthlyViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanBaselineViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanScheduleViewModel
import com.example.smart_emap.ui.erp.production.planning.ProcessMachinePlanViewModel
import com.example.smart_emap.ui.erp.production.planning.ProductionDataManagementViewModel
import com.example.smart_emap.ui.erp.production.requirements.MaterialRequirementsViewModel
import com.example.smart_emap.ui.erp.production.requirements.ComponentRequirementsViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartOrderViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingHomeViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingProcessProductsViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingSuppliersViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.MaterialIssueViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.SuppliedMaterialStockViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.UsageManagementViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingOrderViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingReceivingViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.stock.OutsourcingStockViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingOrderViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingReceivingViewModel
import com.example.smart_emap.ui.erp.inventory.InventoryHomeViewModel
import com.example.smart_emap.ui.erp.inventory.MaterialInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.PartInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.ProductInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.StockEntryViewModel
import com.example.smart_emap.ui.erp.inventory.StockTransactionLogViewModel
import com.example.smart_emap.ui.erp.inventory.StocktakeEntryViewModel
import com.example.smart_emap.ui.erp.inventory.StocktakeListViewModel
import com.example.smart_emap.ui.erp.shipping.AbcAnalysisViewModel
import com.example.smart_emap.ui.erp.shipping.InventoryKpiViewModel
import com.example.smart_emap.ui.erp.shipping.InventoryShortageViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingDocumentViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingHomeViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingListViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingPickingViewModel
import com.example.smart_emap.ui.erp.shipping.WarehouseDailyViewModel
import com.example.smart_emap.ui.erp.shipping.WeldingShippingViewModel
import com.example.smart_emap.ui.master.MasterViewModel
import com.example.smart_emap.ui.master.carrier.CarrierMasterViewModel
import com.example.smart_emap.ui.master.companycalendar.CompanyWorkCalendarViewModel
import com.example.smart_emap.ui.master.customer.CustomerMasterViewModel
import com.example.smart_emap.ui.master.equipmentefficiency.EquipmentEfficiencyMasterViewModel
import com.example.smart_emap.ui.master.material.MaterialMasterViewModel
import com.example.smart_emap.ui.master.materialinspection.MaterialInspectionMasterViewModel
import com.example.smart_emap.ui.master.part.PartMasterViewModel
import com.example.smart_emap.ui.master.process.ProcessMasterViewModel
import com.example.smart_emap.ui.master.processroute.ProcessRouteMasterViewModel
import com.example.smart_emap.ui.master.product.ProductMasterViewModel
import com.example.smart_emap.ui.master.productmachineconfig.ProductMachineConfigMasterViewModel
import com.example.smart_emap.ui.master.productprocessbom.ProductProcessBomMasterViewModel
import com.example.smart_emap.ui.master.productprocessroute.ProductProcessRouteMasterViewModel
import com.example.smart_emap.ui.master.supplier.SupplierMasterViewModel
import com.example.smart_emap.ui.mes.chamfering.ChamferingActualViewModel
import com.example.smart_emap.ui.mes.cutting.CuttingActualViewModel
import com.example.smart_emap.ui.mes.cuttinginstruction.CuttingInstructionViewModel
import com.example.smart_emap.ui.mes.inspection.InspectionActualViewModel
import com.example.smart_emap.ui.mes.inspection.InspectionUiState
import com.example.smart_emap.ui.mes.inspectionregistration.InspectionManualRegistrationViewModel
import com.example.smart_emap.ui.mes.inspectionregistration.InspectionManualRegistrationUiState
import com.example.smart_emap.ui.mes.planinstruction.PlanInstructionViewModel
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityViewModel
import com.example.smart_emap.ui.mes.productivity.WeldingProductivityViewModel
import com.example.smart_emap.ui.mes.monitoring.ProcessMonitorViewModel
import com.example.smart_emap.ui.mes.utilization.InspectionUtilizationViewModel
import com.example.smart_emap.ui.mes.welding.WeldingActualViewModel
import com.example.smart_emap.ui.mes.welding.WeldingUiState
import com.example.smart_emap.ui.mes.cutting.CuttingUiState
import com.example.smart_emap.ui.mes.chamfering.ChamferingUiState
import com.example.smart_emap.ui.system.organization.OrganizationListViewModel
import com.example.smart_emap.ui.system.role.RolePermissionViewModel
import com.example.smart_emap.ui.system.user.UserListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

private val masterGenericPaths = setOf(
    "/master/bom/process-processing-fee",
    "/master/machine",
    "/master/roller-master",
    "/master/destination",
    "/master/destination/holiday",
)

private fun mesInspectionRegistrationLoading(state: InspectionManualRegistrationUiState): Boolean =
    state.isLoadingRows || state.isLoadingProducts || state.isLoadingInspectors || state.isLoadingDefects

private fun mesInspectionLikeLoading(state: InspectionUiState): Boolean =
    state.isLoadingPlans || state.isLoadingProducts || state.isLoadingDefects

private fun mesWeldingLikeLoading(state: WeldingUiState): Boolean =
    state.isLoadingPlans || state.isLoadingProducts || state.isLoadingDefects

private fun mesCuttingLikeLoading(state: CuttingUiState): Boolean =
    state.isLoadingMachines || state.isLoadingPlans || state.isLoadingUsers

private fun mesChamferingLikeLoading(state: ChamferingUiState): Boolean =
    state.isLoadingMachines || state.isLoadingPlans || state.isLoadingUsers

@Composable
fun rememberShellPageLoading(
    activePath: String,
    dashboardViewModel: DashboardViewModel,
    inspectionViewModel: InspectionActualViewModel,
    inspectionManualRegistrationViewModel: InspectionManualRegistrationViewModel,
    inspectionUtilizationViewModel: InspectionUtilizationViewModel,
    inspectionProductivityViewModel: InspectionProductivityViewModel,
    weldingProductivityViewModel: WeldingProductivityViewModel,
    inspectionMonitorViewModel: ProcessMonitorViewModel,
    weldingMonitorViewModel: ProcessMonitorViewModel,
    weldingViewModel: WeldingActualViewModel,
    cuttingViewModel: CuttingActualViewModel,
    chamferingViewModel: ChamferingActualViewModel,
    orderMonthlyViewModel: OrderMonthlyViewModel,
    orderDailyViewModel: OrderDailyViewModel,
    orderDestinationHistoryViewModel: OrderDestinationHistoryViewModel,
    orderHomeViewModel: OrderHomeViewModel,
    materialReceivingHistoryViewModel: MaterialReceivingHistoryViewModel,
    materialReceivingInspectionViewModel: MaterialReceivingInspectionViewModel,
    materialForecastViewModel: MaterialForecastViewModel,
    materialOrderViewModel: MaterialOrderViewModel,
    partOrderViewModel: PartOrderViewModel,
    partReceivingHistoryViewModel: PartReceivingHistoryViewModel,
    outsourcingHomeViewModel: OutsourcingHomeViewModel,
    platingOrderViewModel: PlatingOrderViewModel,
    platingReceivingViewModel: PlatingReceivingViewModel,
    weldingOrderViewModel: WeldingOrderViewModel,
    weldingReceivingViewModel: WeldingReceivingViewModel,
    outsourcingSuppliersViewModel: OutsourcingSuppliersViewModel,
    outsourcingProcessProductsViewModel: OutsourcingProcessProductsViewModel,
    outsourcingStockViewModel: OutsourcingStockViewModel,
    suppliedMaterialStockViewModel: SuppliedMaterialStockViewModel,
    usageManagementViewModel: UsageManagementViewModel,
    materialIssueViewModel: MaterialIssueViewModel,
    inventoryHomeViewModel: InventoryHomeViewModel,
    productInventoryListViewModel: ProductInventoryListViewModel,
    materialInventoryListViewModel: MaterialInventoryListViewModel,
    partInventoryListViewModel: PartInventoryListViewModel,
    stockEntryViewModel: StockEntryViewModel,
    stockTransactionLogViewModel: StockTransactionLogViewModel,
    stocktakeListViewModel: StocktakeListViewModel,
    stocktakeEntryViewModel: StocktakeEntryViewModel,
    shippingHomeViewModel: ShippingHomeViewModel,
    shippingListViewModel: ShippingListViewModel,
    shippingReportViewModel: ShippingDocumentViewModel,
    shippingOverviewViewModel: ShippingDocumentViewModel,
    shippingConfirmViewModel: ShippingDocumentViewModel,
    shippingPickingViewModel: ShippingPickingViewModel,
    weldingShippingViewModel: WeldingShippingViewModel,
    inventoryShortageViewModel: InventoryShortageViewModel,
    inventoryKpiViewModel: InventoryKpiViewModel,
    warehouseDailyViewModel: WarehouseDailyViewModel,
    abcAnalysisViewModel: AbcAnalysisViewModel,
    masterViewModel: MasterViewModel,
    productMasterViewModel: ProductMasterViewModel,
    materialMasterViewModel: MaterialMasterViewModel,
    materialInspectionMasterViewModel: MaterialInspectionMasterViewModel,
    partMasterViewModel: PartMasterViewModel,
    supplierMasterViewModel: SupplierMasterViewModel,
    customerMasterViewModel: CustomerMasterViewModel,
    carrierMasterViewModel: CarrierMasterViewModel,
    processMasterViewModel: ProcessMasterViewModel,
    processRouteMasterViewModel: ProcessRouteMasterViewModel,
    productProcessRouteMasterViewModel: ProductProcessRouteMasterViewModel,
    productProcessBomMasterViewModel: ProductProcessBomMasterViewModel,
    productMachineConfigMasterViewModel: ProductMachineConfigMasterViewModel,
    equipmentEfficiencyMasterViewModel: EquipmentEfficiencyMasterViewModel,
    companyWorkCalendarViewModel: CompanyWorkCalendarViewModel,
    cuttingInstructionViewModel: CuttingInstructionViewModel,
    formingInstructionViewModel: PlanInstructionViewModel,
    weldingInstructionViewModel: PlanInstructionViewModel,
    schedulingViewModel: SchedulingViewModel,
    planBaselineViewModel: PlanBaselineViewModel,
    planScheduleViewModel: PlanScheduleViewModel,
    processMachinePlanViewModel: ProcessMachinePlanViewModel,
    productionDataManagementViewModel: ProductionDataManagementViewModel,
    materialRequirementsViewModel: MaterialRequirementsViewModel,
    componentRequirementsViewModel: ComponentRequirementsViewModel,
    userListViewModel: UserListViewModel,
    organizationListViewModel: OrganizationListViewModel,
    rolePermissionViewModel: RolePermissionViewModel,
): Boolean {
    val dashboard by dashboardViewModel.uiState.collectAsState()
    val inspection by inspectionViewModel.uiState.collectAsState()
    val inspectionManualRegistration by inspectionManualRegistrationViewModel.uiState.collectAsState()
    val inspectionUtilization by inspectionUtilizationViewModel.uiState.collectAsState()
    val inspectionProductivity by inspectionProductivityViewModel.uiState.collectAsState()
    val weldingProductivity by weldingProductivityViewModel.uiState.collectAsState()
    val inspectionMonitor by inspectionMonitorViewModel.uiState.collectAsState()
    val weldingMonitor by weldingMonitorViewModel.uiState.collectAsState()
    val welding by weldingViewModel.uiState.collectAsState()
    val cutting by cuttingViewModel.uiState.collectAsState()
    val chamfering by chamferingViewModel.uiState.collectAsState()
    val orderMonthly by orderMonthlyViewModel.uiState.collectAsState()
    val orderDaily by orderDailyViewModel.uiState.collectAsState()
    val orderDestinationHistory by orderDestinationHistoryViewModel.uiState.collectAsState()
    val orderHome by orderHomeViewModel.uiState.collectAsState()
    val materialReceivingHistory by materialReceivingHistoryViewModel.uiState.collectAsState()
    val materialReceivingInspection by materialReceivingInspectionViewModel.uiState.collectAsState()
    val materialForecast by materialForecastViewModel.uiState.collectAsState()
    val materialOrder by materialOrderViewModel.uiState.collectAsState()
    val partOrder by partOrderViewModel.uiState.collectAsState()
    val partReceivingHistory by partReceivingHistoryViewModel.uiState.collectAsState()
    val outsourcingHome by outsourcingHomeViewModel.uiState.collectAsState()
    val platingOrder by platingOrderViewModel.uiState.collectAsState()
    val platingReceiving by platingReceivingViewModel.uiState.collectAsState()
    val weldingOrder by weldingOrderViewModel.uiState.collectAsState()
    val weldingReceiving by weldingReceivingViewModel.uiState.collectAsState()
    val outsourcingSuppliers by outsourcingSuppliersViewModel.uiState.collectAsState()
    val outsourcingProcessProducts by outsourcingProcessProductsViewModel.uiState.collectAsState()
    val outsourcingStock by outsourcingStockViewModel.uiState.collectAsState()
    val suppliedMaterialStock by suppliedMaterialStockViewModel.uiState.collectAsState()
    val usageManagement by usageManagementViewModel.uiState.collectAsState()
    val materialIssue by materialIssueViewModel.uiState.collectAsState()
    val inventoryHome by inventoryHomeViewModel.uiState.collectAsState()
    val productInventoryList by productInventoryListViewModel.uiState.collectAsState()
    val materialInventoryList by materialInventoryListViewModel.uiState.collectAsState()
    val partInventoryList by partInventoryListViewModel.uiState.collectAsState()
    val stockEntry by stockEntryViewModel.uiState.collectAsState()
    val stockTransactionLog by stockTransactionLogViewModel.uiState.collectAsState()
    val stocktakeList by stocktakeListViewModel.uiState.collectAsState()
    val stocktakeEntry by stocktakeEntryViewModel.uiState.collectAsState()
    val shippingHome by shippingHomeViewModel.uiState.collectAsState()
    val shippingList by shippingListViewModel.uiState.collectAsState()
    val shippingReport by shippingReportViewModel.uiState.collectAsState()
    val shippingOverview by shippingOverviewViewModel.uiState.collectAsState()
    val shippingConfirm by shippingConfirmViewModel.uiState.collectAsState()
    val shippingPicking by shippingPickingViewModel.uiState.collectAsState()
    val weldingShipping by weldingShippingViewModel.uiState.collectAsState()
    val inventoryShortage by inventoryShortageViewModel.uiState.collectAsState()
    val inventoryKpi by inventoryKpiViewModel.uiState.collectAsState()
    val warehouseDaily by warehouseDailyViewModel.uiState.collectAsState()
    val abcAnalysis by abcAnalysisViewModel.uiState.collectAsState()
    val materialRequirements by materialRequirementsViewModel.uiState.collectAsState()
    val componentRequirements by componentRequirementsViewModel.uiState.collectAsState()
    val master by masterViewModel.uiState.collectAsState()
    val productMaster by productMasterViewModel.uiState.collectAsState()
    val materialMaster by materialMasterViewModel.uiState.collectAsState()
    val materialInspectionMaster by materialInspectionMasterViewModel.uiState.collectAsState()
    val partMaster by partMasterViewModel.uiState.collectAsState()
    val supplierMaster by supplierMasterViewModel.uiState.collectAsState()
    val customerMaster by customerMasterViewModel.uiState.collectAsState()
    val carrierMaster by carrierMasterViewModel.uiState.collectAsState()
    val processMaster by processMasterViewModel.uiState.collectAsState()
    val processRouteMaster by processRouteMasterViewModel.uiState.collectAsState()
    val productProcessRouteMaster by productProcessRouteMasterViewModel.uiState.collectAsState()
    val productProcessBomMaster by productProcessBomMasterViewModel.uiState.collectAsState()
    val productMachineConfigMaster by productMachineConfigMasterViewModel.uiState.collectAsState()
    val equipmentEfficiencyMaster by equipmentEfficiencyMasterViewModel.uiState.collectAsState()
    val companyWorkCalendar by companyWorkCalendarViewModel.uiState.collectAsState()
    val cuttingInstruction by cuttingInstructionViewModel.uiState.collectAsState()
    val formingInstruction by formingInstructionViewModel.uiState.collectAsState()
    val weldingInstruction by weldingInstructionViewModel.uiState.collectAsState()
    val scheduling by schedulingViewModel.uiState.collectAsState()
    val planBaseline by planBaselineViewModel.uiState.collectAsState()
    val planSchedule by planScheduleViewModel.uiState.collectAsState()
    val processMachinePlan by processMachinePlanViewModel.uiState.collectAsState()
    val productionDataManagement by productionDataManagementViewModel.uiState.collectAsState()
    val userList by userListViewModel.uiState.collectAsState()
    val organizationList by organizationListViewModel.uiState.collectAsState()
    val rolePermission by rolePermissionViewModel.uiState.collectAsState()

    return remember(
        activePath,
        dashboard.isLoading,
        inspection.isLoadingPlans,
        inspection.isLoadingProducts,
        inspection.isLoadingDefects,
        inspectionManualRegistration.isLoadingRows,
        inspectionManualRegistration.isLoadingProducts,
        inspectionManualRegistration.isLoadingInspectors,
        inspectionManualRegistration.isLoadingDefects,
        inspectionUtilization.isLoading,
        inspectionProductivity.isLoading,
        inspectionProductivity.loadingProducts,
        weldingProductivity.isLoading,
        weldingProductivity.loadingProducts,
        inspectionMonitor.isLoading,
        weldingMonitor.isLoading,
        welding.isLoadingPlans,
        welding.isLoadingProducts,
        welding.isLoadingDefects,
        cutting.isLoadingMachines,
        cutting.isLoadingPlans,
        cutting.isLoadingUsers,
        chamfering.isLoadingMachines,
        chamfering.isLoadingPlans,
        chamfering.isLoadingUsers,
        orderMonthly.isLoading,
        orderDaily.isLoading,
        orderDestinationHistory.isLoading,
        orderHome.isLoadingSummary,
        orderHome.isLoadingAnalytics,
        materialReceivingHistory.isLoading,
        materialReceivingInspection.isLoading,
        materialForecast.isLoading,
        materialOrder.isLoading,
        partOrder.isLoading,
        partReceivingHistory.isLoading,
        outsourcingHome.isLoading,
        platingOrder.isLoading,
        platingReceiving.isLoading,
        weldingOrder.isLoading,
        weldingReceiving.isLoading,
        outsourcingSuppliers.isLoading,
        outsourcingProcessProducts.isLoading,
        outsourcingStock.isLoading,
        suppliedMaterialStock.isLoading,
        usageManagement.isLoading,
        materialIssue.isLoading,
        inventoryHome.isLoading,
        productInventoryList.isLoading,
        materialInventoryList.isLoading,
        partInventoryList.isLoading,
        stockEntry.isLoading,
        stockTransactionLog.isLoading,
        stocktakeList.isLoading,
        stocktakeEntry.isLoading,
        shippingHome.isLoading,
        shippingList.isLoading,
        shippingReport.isLoading,
        shippingOverview.isLoading,
        shippingConfirm.isLoading,
        shippingPicking.isLoading,
        weldingShipping.isLoading,
        inventoryShortage.isLoading,
        inventoryKpi.isLoading,
        warehouseDaily.isLoading,
        abcAnalysis.isLoading,
        master.path,
        master.isLoading,
        productMaster.isLoading,
        materialMaster.isLoading,
        materialInspectionMaster.isLoading,
        partMaster.isLoading,
        supplierMaster.isLoading,
        customerMaster.isLoading,
        carrierMaster.isLoading,
        processMaster.isLoading,
        processRouteMaster.isLoading,
        productProcessRouteMaster.isLoading,
        productProcessBomMaster.isLoading,
        productMachineConfigMaster.isLoading,
        equipmentEfficiencyMaster.isLoading,
        companyWorkCalendar.isLoading,
        cuttingInstruction.isLoading,
        formingInstruction.isLoading,
        weldingInstruction.isLoading,
        scheduling.isLoading,
        planBaseline.isLoading,
        planSchedule.isLoading,
        processMachinePlan.isLoading,
        productionDataManagement.isLoading,
        materialRequirements.loading,
        componentRequirements.loading,
        userList.isLoading,
        organizationList.isTreeLoading,
        organizationList.isDetailLoading,
        rolePermission.isRolesLoading,
        rolePermission.isDetailLoading,
        rolePermission.isMenuLoading,
    ) {
        when (activePath) {
            "/dashboard" -> dashboard.isLoading
            "/mes/actualDataCollection/inspection" -> mesInspectionLikeLoading(inspection)
            "/mes/actualCollectionRegistration/inspection" -> mesInspectionRegistrationLoading(inspectionManualRegistration)
            "/mes/actualAnalysis/utilization/inspection" -> inspectionUtilization.isLoading
            "/mes/actualAnalysis/productivity/inspection" ->
                inspectionProductivity.isLoading || inspectionProductivity.loadingProducts
            "/mes/actualAnalysis/productivity/welding" ->
                weldingProductivity.isLoading || weldingProductivity.loadingProducts
            "/mes/monitoring/inspection" -> inspectionMonitor.isLoading && !inspectionMonitor.hasInitialData
            "/mes/monitoring/welding" -> weldingMonitor.isLoading && !weldingMonitor.hasInitialData
            "/mes/actualDataCollection/welding" -> mesWeldingLikeLoading(welding)
            "/mes/actualDataCollection/cutting" -> mesCuttingLikeLoading(cutting)
            "/mes/actualDataCollection/chamfering" -> mesChamferingLikeLoading(chamfering)
            "/mes/productionInstruction/cutting" -> cuttingInstruction.isLoading
            "/mes/productionInstruction/forming" -> formingInstruction.isLoading
            "/mes/productionInstruction/welding" -> weldingInstruction.isLoading
            "/aps/scheduling" -> scheduling.isLoading
            "/erp/order" -> orderHome.isLoadingSummary || orderHome.isLoadingAnalytics
            "/erp/order/monthly" -> orderMonthly.isLoading
            "/erp/order/daily" -> orderDaily.isLoading
            "/erp/order/destination-history" -> orderDestinationHistory.isLoading
            "/erp/purchase/material" -> materialOrder.isLoading
            "/erp/purchase/material/receiving-history" -> materialReceivingHistory.isLoading
            "/erp/purchase/material/receiving-inspection" -> materialReceivingInspection.isLoading
            "/erp/purchase/material/forecast" -> materialForecast.isLoading
            "/erp/purchase/material/order" -> materialOrder.isLoading
            "/erp/purchase/part/order" -> partOrder.isLoading
            "/erp/purchase/part/receiving-history" -> partReceivingHistory.isLoading
            "/erp/purchase/outsourcing" -> outsourcingHome.isLoading
            "/erp/purchase/outsourcing/plating-order" -> platingOrder.isLoading
            "/erp/purchase/outsourcing/plating-receiving" -> platingReceiving.isLoading
            "/erp/purchase/outsourcing/welding-order" -> weldingOrder.isLoading
            "/erp/purchase/outsourcing/welding-receiving" -> weldingReceiving.isLoading
            "/erp/purchase/outsourcing/suppliers" -> outsourcingSuppliers.isLoading
            "/erp/purchase/outsourcing/process-products" -> outsourcingProcessProducts.isLoading
            "/erp/purchase/outsourcing/stock" -> outsourcingStock.isLoading
            "/erp/purchase/outsourcing/supplied-material-stock" -> suppliedMaterialStock.isLoading
            "/erp/purchase/outsourcing/usage" -> usageManagement.isLoading
            "/erp/purchase/outsourcing/material-issue" -> materialIssue.isLoading
            "/erp/inventory" -> inventoryHome.isLoading
            "/erp/inventory/list" -> productInventoryList.isLoading
            "/erp/inventory/material-list" -> materialInventoryList.isLoading
            "/erp/inventory/part-list" -> partInventoryList.isLoading
            "/erp/inventory/stock-entry" -> stockEntry.isLoading
            "/erp/inventory/stock-transaction-logs" -> stockTransactionLog.isLoading
            "/erp/inventory/stocktake/list" -> stocktakeList.isLoading
            "/erp/inventory/stocktake/entry" -> stocktakeEntry.isLoading
            "/erp/shipping" -> shippingHome.isLoading
            "/erp/shipping/list" -> shippingList.isLoading
            "/erp/shipping/report" -> shippingReport.isLoading
            "/erp/shipping/overview" -> shippingOverview.isLoading
            "/erp/shipping/confirm" -> shippingConfirm.isLoading
            "/erp/shipping/welding" -> weldingShipping.isLoading
            "/erp/shipping/picking" -> shippingPicking.isLoading
            "/erp/shipping/inventory-shortage" -> inventoryShortage.isLoading
            "/erp/shipping/inventory-kpi" -> inventoryKpi.isLoading
            "/erp/shipping/warehouse-daily" -> warehouseDaily.isLoading
            "/erp/shipping/abc-analysis" -> abcAnalysis.isLoading
            "/erp/production/data-management" -> productionDataManagement.isLoading
            "/erp/production-requirements/material" -> materialRequirements.loading
            "/erp/production-requirements/component" -> componentRequirements.loading
            "/erp/production/plan-baseline" -> planBaseline.isLoading
            "/erp/production/plan-schedules" -> planSchedule.isLoading
            "/erp/production/process-machine-plan" -> processMachinePlan.isLoading
            "/master/product" -> productMaster.isLoading
            "/master/material" -> materialMaster.isLoading
            "/master/material-inspection" -> materialInspectionMaster.isLoading
            "/master/part" -> partMaster.isLoading
            "/master/supplier" -> supplierMaster.isLoading
            "/master/customer" -> customerMaster.isLoading
            "/master/carrier" -> carrierMaster.isLoading
            "/master/process" -> processMaster.isLoading
            "/master/process-route" -> processRouteMaster.isLoading
            "/master/product-process-route" -> productProcessRouteMaster.isLoading
            "/master/bom/product-process" -> productProcessBomMaster.isLoading
            "/master/bom/product-machine-config" -> productMachineConfigMaster.isLoading
            "/master/bom/equipment-efficiency" -> equipmentEfficiencyMaster.isLoading
            "/master/company-work-calendar" -> companyWorkCalendar.isLoading
            "/system/users" -> userList.isLoading
            "/system/organization" ->
                organizationList.isTreeLoading || organizationList.isDetailLoading
            "/system/roles" ->
                rolePermission.isRolesLoading ||
                    rolePermission.isDetailLoading ||
                    rolePermission.isMenuLoading
            in masterGenericPaths -> master.path == activePath && master.isLoading
            else -> false
        }
    }
}

/** 路由切换 + 页面数据加载中的内容区遮罩 */
@Composable
fun ShellRouteLoadingHost(
    activePath: String,
    isRouteLoading: Boolean,
    pageLoading: Boolean,
    onRouteReady: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val showOverlay = isRouteLoading || pageLoading

    Box(modifier = modifier) {
        content()
        if (showOverlay) {
            ShellRouteLoadingOverlay()
        }
    }

    LaunchedEffect(activePath, pageLoading, isRouteLoading) {
        if (!isRouteLoading) return@LaunchedEffect
        if (pageLoading) return@LaunchedEffect
        yield()
        delay(120)
        onRouteReady()
    }
}
