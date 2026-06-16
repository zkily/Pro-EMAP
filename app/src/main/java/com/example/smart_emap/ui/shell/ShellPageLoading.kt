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
import com.example.smart_emap.ui.erp.order.OrderMonthlyViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanBaselineViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanScheduleViewModel
import com.example.smart_emap.ui.erp.production.planning.ProcessMachinePlanViewModel
import com.example.smart_emap.ui.erp.production.planning.ProductionDataManagementViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartOrderViewModel
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
    materialReceivingHistoryViewModel: MaterialReceivingHistoryViewModel,
    materialReceivingInspectionViewModel: MaterialReceivingInspectionViewModel,
    materialForecastViewModel: MaterialForecastViewModel,
    materialOrderViewModel: MaterialOrderViewModel,
    partOrderViewModel: PartOrderViewModel,
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
    val materialReceivingHistory by materialReceivingHistoryViewModel.uiState.collectAsState()
    val materialReceivingInspection by materialReceivingInspectionViewModel.uiState.collectAsState()
    val materialForecast by materialForecastViewModel.uiState.collectAsState()
    val materialOrder by materialOrderViewModel.uiState.collectAsState()
    val partOrder by partOrderViewModel.uiState.collectAsState()
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
        materialReceivingHistory.isLoading,
        materialReceivingInspection.isLoading,
        materialForecast.isLoading,
        materialOrder.isLoading,
        partOrder.isLoading,
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
            "/erp/order/monthly" -> orderMonthly.isLoading
            "/erp/order/daily" -> orderDaily.isLoading
            "/erp/order/destination-history" -> orderDestinationHistory.isLoading
            "/erp/purchase/material/receiving-history" -> materialReceivingHistory.isLoading
            "/erp/purchase/material/receiving-inspection" -> materialReceivingInspection.isLoading
            "/erp/purchase/material/forecast" -> materialForecast.isLoading
            "/erp/purchase/material/order" -> materialOrder.isLoading
            "/erp/purchase/part/order" -> partOrder.isLoading
            "/erp/production/data-management" -> productionDataManagement.isLoading
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
