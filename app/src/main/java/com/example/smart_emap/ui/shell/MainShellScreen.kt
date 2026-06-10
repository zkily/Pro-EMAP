package com.example.smart_emap.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_emap.SmartEmapAppContainer
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.aps.scheduling.SchedulingScreen
import com.example.smart_emap.ui.aps.scheduling.SchedulingViewModel
import com.example.smart_emap.ui.mes.planinstruction.PlanInstructionConfig
import com.example.smart_emap.ui.mes.planinstruction.PlanInstructionScreen
import com.example.smart_emap.ui.mes.planinstruction.PlanInstructionViewModel
import com.example.smart_emap.ui.dashboard.DashboardScreen
import com.example.smart_emap.ui.dashboard.DashboardViewModel
import com.example.smart_emap.ui.erp.order.OrderDailyScreen
import com.example.smart_emap.ui.erp.order.OrderDailyViewModel
import com.example.smart_emap.ui.erp.order.OrderDestinationHistoryScreen
import com.example.smart_emap.ui.erp.order.OrderDestinationHistoryViewModel
import com.example.smart_emap.ui.erp.order.OrderMonthlyScreen
import com.example.smart_emap.ui.erp.order.OrderMonthlyViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanBaselineScreen
import com.example.smart_emap.ui.erp.production.planning.PlanBaselineViewModel
import com.example.smart_emap.ui.erp.production.planning.PlanScheduleScreen
import com.example.smart_emap.ui.erp.production.planning.PlanScheduleViewModel
import com.example.smart_emap.ui.erp.production.planning.ProcessMachinePlanScreen
import com.example.smart_emap.ui.erp.production.planning.ProcessMachinePlanViewModel
import com.example.smart_emap.ui.erp.production.planning.ProductionDataManagementScreen
import com.example.smart_emap.ui.erp.production.planning.ProductionDataManagementViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialHomeScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartHomeScreen
import com.example.smart_emap.ui.erp.purchase.part.PartOrderScreen
import com.example.smart_emap.ui.erp.purchase.part.PartOrderViewModel
import com.example.smart_emap.ui.master.MasterHomeScreen
import com.example.smart_emap.ui.master.MasterScreen
import com.example.smart_emap.ui.master.MasterViewModel
import com.example.smart_emap.ui.master.companycalendar.CompanyWorkCalendarScreen
import com.example.smart_emap.ui.master.companycalendar.CompanyWorkCalendarViewModel
import com.example.smart_emap.ui.master.material.MaterialColumnSettingsStore
import com.example.smart_emap.ui.master.material.MaterialMasterScreen
import com.example.smart_emap.ui.master.material.MaterialMasterViewModel
import com.example.smart_emap.ui.master.materialinspection.MaterialInspectionMasterScreen
import com.example.smart_emap.ui.master.materialinspection.MaterialInspectionMasterViewModel
import com.example.smart_emap.ui.master.part.PartMasterScreen
import com.example.smart_emap.ui.master.part.PartMasterViewModel
import com.example.smart_emap.ui.master.process.ProcessMasterScreen
import com.example.smart_emap.ui.master.process.ProcessMasterViewModel
import com.example.smart_emap.ui.master.processroute.ProcessRouteMasterScreen
import com.example.smart_emap.ui.master.processroute.ProcessRouteMasterViewModel
import com.example.smart_emap.ui.master.productprocessroute.ProductProcessRouteMasterScreen
import com.example.smart_emap.ui.master.productprocessroute.ProductProcessRouteMasterViewModel
import com.example.smart_emap.ui.master.carrier.CarrierMasterScreen
import com.example.smart_emap.ui.master.carrier.CarrierMasterViewModel
import com.example.smart_emap.ui.master.customer.CustomerMasterScreen
import com.example.smart_emap.ui.master.customer.CustomerMasterViewModel
import com.example.smart_emap.ui.master.supplier.SupplierMasterScreen
import com.example.smart_emap.ui.master.supplier.SupplierMasterViewModel
import com.example.smart_emap.ui.master.product.ProductColumnSettingsStore
import com.example.smart_emap.ui.master.product.ProductMasterScreen
import com.example.smart_emap.ui.master.product.ProductMasterViewModel
import com.example.smart_emap.ui.mes.chamfering.ChamferingActualScreen
import com.example.smart_emap.ui.mes.chamfering.ChamferingActualViewModel
import com.example.smart_emap.ui.mes.chamfering.ChamferingActualViewModelFactory
import com.example.smart_emap.ui.mes.cuttinginstruction.CuttingInstructionScreen
import com.example.smart_emap.ui.mes.cuttinginstruction.CuttingInstructionViewModel
import com.example.smart_emap.ui.mes.cutting.CuttingActualScreen
import com.example.smart_emap.ui.mes.cutting.CuttingActualViewModel
import com.example.smart_emap.ui.mes.cutting.CuttingActualViewModelFactory
import com.example.smart_emap.ui.mes.inspection.InspectionActualScreen
import com.example.smart_emap.ui.mes.inspection.InspectionActualViewModel
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityScreen
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityViewModel
import com.example.smart_emap.ui.mes.productivity.WeldingProductivityScreen
import com.example.smart_emap.ui.mes.productivity.WeldingProductivityViewModel
import com.example.smart_emap.ui.mes.utilization.InspectionUtilizationScreen
import com.example.smart_emap.ui.mes.utilization.InspectionUtilizationViewModel
import com.example.smart_emap.ui.mes.welding.WeldingActualScreen
import com.example.smart_emap.ui.mes.welding.WeldingActualViewModel
import com.example.smart_emap.ui.system.organization.OrganizationListScreen
import com.example.smart_emap.ui.system.organization.OrganizationListViewModel
import com.example.smart_emap.ui.system.role.RolePermissionScreen
import com.example.smart_emap.ui.system.role.RolePermissionViewModel
import com.example.smart_emap.ui.system.user.UserListScreen
import com.example.smart_emap.ui.system.user.UserListViewModel

/** Web MainLayout.vue と同構造：サイドバー + ヘッダー + タブ + コンテンツ */
@Composable
fun MainShellScreen(
    appContainer: SmartEmapAppContainer,
    user: UserDto,
    onLogout: () -> Unit,
) {
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            dashboardRepository = appContainer.dashboardRepository,
            username = user.fullName ?: user.username,
        ),
    )
    val inspectionViewModel: InspectionActualViewModel = viewModel(
        factory = InspectionActualViewModel.Factory(
            repository = appContainer.inspectionRepository,
            offlineStore = appContainer.inspectionOfflineStore,
            networkMonitor = appContainer.networkMonitor,
            userId = user.id,
            inspectorLabel = user.fullName?.trim().orEmpty().ifEmpty { user.username },
        ),
    )
    val inspectionUtilizationViewModel: InspectionUtilizationViewModel = viewModel(
        factory = InspectionUtilizationViewModel.Factory(
            inspectionRepository = appContainer.inspectionRepository,
            userRepository = appContainer.systemUserRepository,
        ),
    )
    val inspectionProductivityViewModel: InspectionProductivityViewModel = viewModel(
        factory = InspectionProductivityViewModel.Factory(
            inspectionRepository = appContainer.inspectionRepository,
            userRepository = appContainer.systemUserRepository,
        ),
    )
    val weldingProductivityViewModel: WeldingProductivityViewModel = viewModel(
        factory = WeldingProductivityViewModel.Factory(
            weldingRepository = appContainer.weldingRepository,
            userRepository = appContainer.systemUserRepository,
        ),
    )
    val weldingViewModel: WeldingActualViewModel = viewModel(
        factory = WeldingActualViewModel.Factory(
            repository = appContainer.weldingRepository,
            offlineStore = appContainer.weldingOfflineStore,
            networkMonitor = appContainer.networkMonitor,
            userId = user.id,
            operatorLabel = user.fullName?.trim().orEmpty().ifEmpty { user.username },
        ),
    )

    val cuttingViewModel: CuttingActualViewModel = viewModel(
        factory = CuttingActualViewModelFactory(
            repository = appContainer.cuttingRepository,
            offlineStore = appContainer.cuttingOfflineStore,
            networkMonitor = appContainer.networkMonitor,
        ),
    )

    val chamferingViewModel: ChamferingActualViewModel = viewModel(
        factory = ChamferingActualViewModelFactory(
            repository = appContainer.chamferingRepository,
            offlineStore = appContainer.chamferingOfflineStore,
            networkMonitor = appContainer.networkMonitor,
        ),
    )

    val orderMonthlyViewModel: OrderMonthlyViewModel = viewModel(
        factory = OrderMonthlyViewModel.Factory(
            repository = appContainer.orderMonthlyRepository,
        ),
    )

    val orderDailyViewModel: OrderDailyViewModel = viewModel(
        factory = OrderDailyViewModel.Factory(
            repository = appContainer.orderDailyRepository,
        ),
    )

    val orderDestinationHistoryViewModel: OrderDestinationHistoryViewModel = viewModel(
        factory = OrderDestinationHistoryViewModel.Factory(
            repository = appContainer.orderDailyRepository,
        ),
    )

    val materialReceivingHistoryViewModel: MaterialReceivingHistoryViewModel = viewModel(
        factory = MaterialReceivingHistoryViewModel.Factory(
            repository = appContainer.materialRepository,
        ),
    )

    val materialReceivingInspectionViewModel: MaterialReceivingInspectionViewModel = viewModel(
        factory = MaterialReceivingInspectionViewModel.Factory(
            repository = appContainer.materialRepository,
        ),
    )

    val materialForecastViewModel: MaterialForecastViewModel = viewModel(
        factory = MaterialForecastViewModel.Factory(
            repository = appContainer.materialRepository,
        ),
    )

    val materialOrderViewModel: MaterialOrderViewModel = viewModel(
        factory = MaterialOrderViewModel.Factory(
            repository = appContainer.materialRepository,
        ),
    )

    val partOrderViewModel: PartOrderViewModel = viewModel(
        factory = PartOrderViewModel.Factory(
            repository = appContainer.partRepository,
        ),
    )

    val masterViewModel: MasterViewModel = viewModel(
        factory = MasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val productMasterViewModel: ProductMasterViewModel = viewModel(
        factory = ProductMasterViewModel.Factory(
            repository = appContainer.masterRepository,
            columnSettingsStore = ProductColumnSettingsStore(LocalContext.current.applicationContext),
        ),
    )

    val materialMasterViewModel: MaterialMasterViewModel = viewModel(
        factory = MaterialMasterViewModel.Factory(
            repository = appContainer.masterRepository,
            columnSettingsStore = MaterialColumnSettingsStore(LocalContext.current.applicationContext),
        ),
    )

    val materialInspectionMasterViewModel: MaterialInspectionMasterViewModel = viewModel(
        factory = MaterialInspectionMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val partMasterViewModel: PartMasterViewModel = viewModel(
        factory = PartMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val supplierMasterViewModel: SupplierMasterViewModel = viewModel(
        factory = SupplierMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val customerMasterViewModel: CustomerMasterViewModel = viewModel(
        factory = CustomerMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val carrierMasterViewModel: CarrierMasterViewModel = viewModel(
        factory = CarrierMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val processMasterViewModel: ProcessMasterViewModel = viewModel(
        factory = ProcessMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val processRouteMasterViewModel: ProcessRouteMasterViewModel = viewModel(
        factory = ProcessRouteMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val productProcessRouteMasterViewModel: ProductProcessRouteMasterViewModel = viewModel(
        factory = ProductProcessRouteMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val companyWorkCalendarViewModel: CompanyWorkCalendarViewModel = viewModel(
        factory = CompanyWorkCalendarViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val cuttingInstructionViewModel: CuttingInstructionViewModel = viewModel(
        factory = CuttingInstructionViewModel.Factory(
            repository = appContainer.cuttingInstructionRepository,
        ),
    )

    val schedulingViewModel: SchedulingViewModel = viewModel(
        factory = SchedulingViewModel.Factory(
            repository = appContainer.apsSchedulingRepository,
        ),
    )

    val planBaselineViewModel: PlanBaselineViewModel = viewModel(
        factory = PlanBaselineViewModel.Factory(
            repository = appContainer.planBaselineRepository,
            apsRepository = appContainer.apsSchedulingRepository,
            masterRepository = appContainer.masterRepository,
        ),
    )

    val planScheduleViewModel: PlanScheduleViewModel = viewModel(
        factory = PlanScheduleViewModel.Factory(
            apsRepository = appContainer.apsSchedulingRepository,
            planBaselineRepository = appContainer.planBaselineRepository,
        ),
    )

    val processMachinePlanViewModel: ProcessMachinePlanViewModel = viewModel(
        factory = ProcessMachinePlanViewModel.Factory(
            repository = appContainer.productionSummaryRepository,
        ),
    )

    val userListViewModel: UserListViewModel = viewModel(
        factory = UserListViewModel.Factory(
            repository = appContainer.systemUserRepository,
        ),
    )

    val organizationListViewModel: OrganizationListViewModel = viewModel(
        factory = OrganizationListViewModel.Factory(
            repository = appContainer.systemOrganizationRepository,
        ),
    )

    val rolePermissionViewModel: RolePermissionViewModel = viewModel(
        factory = RolePermissionViewModel.Factory(
            repository = appContainer.systemRoleRepository,
        ),
    )

    val activity = LocalContext.current as ComponentActivity

    val formingInstructionViewModel: PlanInstructionViewModel = viewModel(
        key = PlanInstructionViewModel.VIEW_MODEL_KEY_FORMING,
        factory = PlanInstructionViewModel.Factory(
            repository = appContainer.planInstructionRepository,
            masterRepository = appContainer.masterRepository,
            config = PlanInstructionConfig.Forming,
        ),
    )

    val weldingInstructionViewModel: PlanInstructionViewModel = viewModel(
        key = PlanInstructionViewModel.VIEW_MODEL_KEY_WELDING,
        factory = PlanInstructionViewModel.Factory(
            repository = appContainer.planInstructionRepository,
            masterRepository = appContainer.masterRepository,
            config = PlanInstructionConfig.Welding,
        ),
    )

    val shellViewModel: MainShellViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = MainShellViewModel.Factory(),
    )
    val shellState by shellViewModel.uiState.collectAsState()
    val activePath = shellState.activePath
    val tabs = shellState.tabs

    LaunchedEffect(user.id, user.role, user.permissions, user.menuCodes) {
        shellViewModel.enforceUserAccess(user)
    }

    CompositionLocalProvider(LocalCurrentUser provides user) {
        MainShellContent(
            user = user,
            appContainer = appContainer,
            shellViewModel = shellViewModel,
            shellState = shellState,
            activePath = activePath,
            tabs = tabs,
            dashboardViewModel = dashboardViewModel,
            inspectionViewModel = inspectionViewModel,
            inspectionUtilizationViewModel = inspectionUtilizationViewModel,
            inspectionProductivityViewModel = inspectionProductivityViewModel,
            weldingProductivityViewModel = weldingProductivityViewModel,
            weldingViewModel = weldingViewModel,
            cuttingViewModel = cuttingViewModel,
            chamferingViewModel = chamferingViewModel,
            orderMonthlyViewModel = orderMonthlyViewModel,
            orderDailyViewModel = orderDailyViewModel,
            orderDestinationHistoryViewModel = orderDestinationHistoryViewModel,
            materialReceivingHistoryViewModel = materialReceivingHistoryViewModel,
            materialReceivingInspectionViewModel = materialReceivingInspectionViewModel,
            materialForecastViewModel = materialForecastViewModel,
            materialOrderViewModel = materialOrderViewModel,
            partOrderViewModel = partOrderViewModel,
            masterViewModel = masterViewModel,
            productMasterViewModel = productMasterViewModel,
            materialMasterViewModel = materialMasterViewModel,
            materialInspectionMasterViewModel = materialInspectionMasterViewModel,
            partMasterViewModel = partMasterViewModel,
            supplierMasterViewModel = supplierMasterViewModel,
            customerMasterViewModel = customerMasterViewModel,
            carrierMasterViewModel = carrierMasterViewModel,
            processMasterViewModel = processMasterViewModel,
            processRouteMasterViewModel = processRouteMasterViewModel,
            productProcessRouteMasterViewModel = productProcessRouteMasterViewModel,
            companyWorkCalendarViewModel = companyWorkCalendarViewModel,
            cuttingInstructionViewModel = cuttingInstructionViewModel,
            formingInstructionViewModel = formingInstructionViewModel,
            weldingInstructionViewModel = weldingInstructionViewModel,
            schedulingViewModel = schedulingViewModel,
            planBaselineViewModel = planBaselineViewModel,
            planScheduleViewModel = planScheduleViewModel,
            processMachinePlanViewModel = processMachinePlanViewModel,
            userListViewModel = userListViewModel,
            organizationListViewModel = organizationListViewModel,
            rolePermissionViewModel = rolePermissionViewModel,
            onLogout = onLogout,
        )
    }
}

@Composable
private fun MainShellContent(
    user: UserDto,
    appContainer: SmartEmapAppContainer,
    shellViewModel: MainShellViewModel,
    shellState: MainShellUiState,
    activePath: String,
    tabs: List<ShellTab>,
    dashboardViewModel: DashboardViewModel,
    inspectionViewModel: InspectionActualViewModel,
    inspectionUtilizationViewModel: InspectionUtilizationViewModel,
    inspectionProductivityViewModel: InspectionProductivityViewModel,
    weldingProductivityViewModel: WeldingProductivityViewModel,
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
    companyWorkCalendarViewModel: CompanyWorkCalendarViewModel,
    cuttingInstructionViewModel: CuttingInstructionViewModel,
    formingInstructionViewModel: PlanInstructionViewModel,
    weldingInstructionViewModel: PlanInstructionViewModel,
    schedulingViewModel: SchedulingViewModel,
    planBaselineViewModel: PlanBaselineViewModel,
    planScheduleViewModel: PlanScheduleViewModel,
    processMachinePlanViewModel: ProcessMachinePlanViewModel,
    userListViewModel: UserListViewModel,
    organizationListViewModel: OrganizationListViewModel,
    rolePermissionViewModel: RolePermissionViewModel,
    onLogout: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LayoutColors.ShellBg),
    ) {
        val configuration = LocalConfiguration.current
        val layoutMode = resolveShellLayoutMode(maxWidth, configuration.orientation)
        val autoCollapse = shouldAutoCollapseSidebar(layoutMode)
        val sidebarCollapsed = when {
            layoutMode.useCompactSidebar -> true
            autoCollapse -> true
            else -> shellState.isSidebarCollapsed
        }
        val sidebarVisible = when {
            layoutMode.useCompactSidebar -> true
            layoutMode.useMobileOverlay -> !sidebarCollapsed
            else -> true
        }

        LaunchedEffect(layoutMode) {
            if (autoCollapse) {
                shellViewModel.setSidebarCollapsed(true)
            }
        }

        if (layoutMode.useMobileOverlay && sidebarVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { shellViewModel.setSidebarCollapsed(true) },
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (sidebarVisible) {
                SidebarMenu(
                    user = user,
                    isCollapsed = sidebarCollapsed,
                    activePath = activePath,
                    showCollapseControl = !layoutMode.useMobileOverlay && !layoutMode.useCompactSidebar,
                    onNavigate = { path ->
                        shellViewModel.navigateTo(path, user)
                        if (layoutMode.useMobileOverlay) {
                            shellViewModel.setSidebarCollapsed(true)
                        }
                    },
                    onToggleCollapse = {
                        shellViewModel.setSidebarCollapsed(!shellState.isSidebarCollapsed)
                    },
                    modifier = Modifier
                        .then(
                            if (layoutMode.useMobileOverlay) {
                                Modifier
                                    .fillMaxHeight()
                                    .width(220.dp)
                                    .zIndex(2f)
                            } else {
                                Modifier.fillMaxHeight()
                            },
                        ),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                HeaderBar(
                    user = user,
                    isMobile = layoutMode.useMobileOverlay,
                    sidebarOpen = sidebarVisible,
                    onToggleSidebar = {
                        shellViewModel.setSidebarCollapsed(!shellState.isSidebarCollapsed)
                    },
                    onLogout = onLogout,
                )
                TabsNav(
                    tabs = tabs,
                    activePath = activePath,
                    onTabSelected = shellViewModel::selectTab,
                    onTabClosed = shellViewModel::closeTab,
                    onRefresh = {
                        when (activePath) {
                            "/dashboard" -> dashboardViewModel.loadDashboard()
                            "/mes/actualDataCollection/inspection" -> inspectionViewModel.refreshAll()
                            "/mes/actualAnalysis/utilization/inspection" -> inspectionUtilizationViewModel.refreshAll()
                            "/mes/actualAnalysis/productivity/inspection" -> inspectionProductivityViewModel.refreshAll()
                            "/mes/actualAnalysis/productivity/welding" -> weldingProductivityViewModel.refreshAll()
                            "/mes/actualDataCollection/welding" -> weldingViewModel.refreshAll()
                            "/mes/actualDataCollection/cutting" -> cuttingViewModel.refreshAll()
                            "/mes/actualDataCollection/chamfering" -> chamferingViewModel.refreshAll()
                            "/erp/order/monthly" -> orderMonthlyViewModel.refreshAll()
                            "/erp/order/daily" -> orderDailyViewModel.refreshAll()
                            "/erp/order/destination-history" -> orderDestinationHistoryViewModel.loadDestinationOptions()
                            "/erp/purchase/material/receiving-history" -> materialReceivingHistoryViewModel.refreshAll()
                            "/erp/purchase/material/receiving-inspection" -> materialReceivingInspectionViewModel.refreshAll()
                            "/erp/purchase/material/forecast" -> materialForecastViewModel.refreshAll()
                            "/erp/purchase/material/order" -> materialOrderViewModel.refreshAll()
                            "/erp/purchase/part/order" -> partOrderViewModel.refreshAll()
                            "/aps/scheduling" -> schedulingViewModel.refreshAll()
                            "/erp/production/plan-baseline" -> planBaselineViewModel.loadComparison()
                            "/erp/production/plan-schedules" -> planScheduleViewModel.fetchData()
                            "/erp/production/process-machine-plan" -> processMachinePlanViewModel.loadData()
                            "/mes/productionInstruction/forming" -> formingInstructionViewModel.refreshAll()
                            "/mes/productionInstruction/welding" -> weldingInstructionViewModel.refreshAll()
                            "/system/users" -> userListViewModel.refreshUsers()
                            "/system/organization" -> organizationListViewModel.refreshTree()
                            "/system/roles" -> rolePermissionViewModel.refreshAll()
                            else -> if (activePath.startsWith("/master")) {
                                when (activePath) {
                                    "/master/product" -> productMasterViewModel.refreshAll()
                                    "/master/material" -> materialMasterViewModel.refreshAll()
                                    "/master/material-inspection" -> materialInspectionMasterViewModel.refreshAll()
                                    "/master/part" -> partMasterViewModel.refreshAll()
                                    "/master/supplier" -> supplierMasterViewModel.refreshAll()
                                    "/master/customer" -> customerMasterViewModel.refreshAll()
                                    "/master/carrier" -> carrierMasterViewModel.refreshAll()
                                    "/master/process" -> processMasterViewModel.refreshAll()
                                    "/master/process-route" -> processRouteMasterViewModel.refreshAll()
                                    "/master/product-process-route" -> productProcessRouteMasterViewModel.refreshAll()
                                    "/master/company-work-calendar" -> companyWorkCalendarViewModel.refreshAll()
                                    else -> masterViewModel.refreshAll()
                                }
                            }
                        }
                    },
                    onCloseOthers = shellViewModel::closeOtherTabs,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    key(activePath) {
                        ShellRouteContent(
                            path = activePath,
                            appContainer = appContainer,
                            dashboardViewModel = dashboardViewModel,
                            inspectionViewModel = inspectionViewModel,
                            inspectionUtilizationViewModel = inspectionUtilizationViewModel,
                            inspectionProductivityViewModel = inspectionProductivityViewModel,
                            weldingProductivityViewModel = weldingProductivityViewModel,
                            weldingViewModel = weldingViewModel,
                            cuttingViewModel = cuttingViewModel,
                            chamferingViewModel = chamferingViewModel,
                            orderMonthlyViewModel = orderMonthlyViewModel,
                            orderDailyViewModel = orderDailyViewModel,
                            orderDestinationHistoryViewModel = orderDestinationHistoryViewModel,
                            materialReceivingHistoryViewModel = materialReceivingHistoryViewModel,
                            materialReceivingInspectionViewModel = materialReceivingInspectionViewModel,
                            materialForecastViewModel = materialForecastViewModel,
                            materialOrderViewModel = materialOrderViewModel,
                            partOrderViewModel = partOrderViewModel,
                            masterViewModel = masterViewModel,
                            productMasterViewModel = productMasterViewModel,
                            materialMasterViewModel = materialMasterViewModel,
                            materialInspectionMasterViewModel = materialInspectionMasterViewModel,
                            partMasterViewModel = partMasterViewModel,
                            supplierMasterViewModel = supplierMasterViewModel,
                            customerMasterViewModel = customerMasterViewModel,
                            carrierMasterViewModel = carrierMasterViewModel,
                            processMasterViewModel = processMasterViewModel,
                            processRouteMasterViewModel = processRouteMasterViewModel,
                            productProcessRouteMasterViewModel = productProcessRouteMasterViewModel,
                            companyWorkCalendarViewModel = companyWorkCalendarViewModel,
                            cuttingInstructionViewModel = cuttingInstructionViewModel,
                            formingInstructionViewModel = formingInstructionViewModel,
                            weldingInstructionViewModel = weldingInstructionViewModel,
                            schedulingViewModel = schedulingViewModel,
                            planBaselineViewModel = planBaselineViewModel,
                            planScheduleViewModel = planScheduleViewModel,
                            processMachinePlanViewModel = processMachinePlanViewModel,
                            userListViewModel = userListViewModel,
                            organizationListViewModel = organizationListViewModel,
                            rolePermissionViewModel = rolePermissionViewModel,
                            onNavigate = { path -> shellViewModel.navigateTo(path, user) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShellRouteContent(
    path: String,
    appContainer: SmartEmapAppContainer,
    dashboardViewModel: DashboardViewModel,
    inspectionViewModel: InspectionActualViewModel,
    inspectionUtilizationViewModel: InspectionUtilizationViewModel,
    inspectionProductivityViewModel: InspectionProductivityViewModel,
    weldingProductivityViewModel: WeldingProductivityViewModel,
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
    companyWorkCalendarViewModel: CompanyWorkCalendarViewModel,
    cuttingInstructionViewModel: CuttingInstructionViewModel,
    formingInstructionViewModel: PlanInstructionViewModel,
    weldingInstructionViewModel: PlanInstructionViewModel,
    schedulingViewModel: SchedulingViewModel,
    planBaselineViewModel: PlanBaselineViewModel,
    planScheduleViewModel: PlanScheduleViewModel,
    processMachinePlanViewModel: ProcessMachinePlanViewModel,
    userListViewModel: UserListViewModel,
    organizationListViewModel: OrganizationListViewModel,
    rolePermissionViewModel: RolePermissionViewModel,
    onNavigate: (String) -> Unit,
) {
    when (path) {
        "/access-denied" -> AccessDeniedScreen(onGoHome = { onNavigate("/dashboard") })
        "/dashboard" -> DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigate = onNavigate,
        )
        "/master" -> MasterHomeScreen(onNavigate = onNavigate)
        "/master/product" -> ProductMasterScreen(viewModel = productMasterViewModel)
        "/master/material" -> MaterialMasterScreen(viewModel = materialMasterViewModel)
        "/master/material-inspection" -> MaterialInspectionMasterScreen(viewModel = materialInspectionMasterViewModel)
        "/master/part" -> PartMasterScreen(viewModel = partMasterViewModel)
        "/master/supplier" -> SupplierMasterScreen(viewModel = supplierMasterViewModel)
        "/master/customer" -> CustomerMasterScreen(viewModel = customerMasterViewModel)
        "/master/carrier" -> CarrierMasterScreen(viewModel = carrierMasterViewModel)
        "/master/process" -> ProcessMasterScreen(viewModel = processMasterViewModel)
        "/master/process-route" -> ProcessRouteMasterScreen(viewModel = processRouteMasterViewModel)
        "/master/product-process-route" -> ProductProcessRouteMasterScreen(viewModel = productProcessRouteMasterViewModel)
        "/master/company-work-calendar" -> CompanyWorkCalendarScreen(viewModel = companyWorkCalendarViewModel)
        "/master/bom/process-processing-fee",
        "/master/machine",
        "/master/roller-master",
        "/master/destination",
        "/master/destination/holiday",
        -> MasterScreen(path = path, viewModel = masterViewModel)
        "/erp/order/monthly" -> OrderMonthlyScreen(viewModel = orderMonthlyViewModel)
        "/erp/order/daily" -> OrderDailyScreen(viewModel = orderDailyViewModel)
        "/erp/order/destination-history" -> OrderDestinationHistoryScreen(viewModel = orderDestinationHistoryViewModel)
        "/erp/purchase/material" -> MaterialHomeScreen(onNavigate = onNavigate)
        "/erp/purchase/material/receiving-history" -> MaterialReceivingHistoryScreen(viewModel = materialReceivingHistoryViewModel)
        "/erp/purchase/material/receiving-inspection" -> MaterialReceivingInspectionScreen(viewModel = materialReceivingInspectionViewModel)
        "/erp/purchase/material/forecast" -> MaterialForecastScreen(viewModel = materialForecastViewModel)
        "/erp/purchase/material/order" -> MaterialOrderScreen(viewModel = materialOrderViewModel)
        "/erp/purchase/part" -> PartHomeScreen(onNavigate = onNavigate)
        "/erp/purchase/part/order" -> PartOrderScreen(viewModel = partOrderViewModel)
        "/mes/actualDataCollection/inspection" -> InspectionActualScreen(viewModel = inspectionViewModel)
        "/mes/actualAnalysis/utilization/inspection" -> InspectionUtilizationScreen(
            viewModel = inspectionUtilizationViewModel,
            onNavigate = onNavigate,
        )
        "/mes/actualAnalysis/productivity/inspection" -> InspectionProductivityScreen(
            viewModel = inspectionProductivityViewModel,
        )
        "/mes/actualAnalysis/productivity/welding" -> WeldingProductivityScreen(
            viewModel = weldingProductivityViewModel,
        )
        "/mes/actualDataCollection/welding" -> WeldingActualScreen(viewModel = weldingViewModel)
        "/mes/actualDataCollection/cutting" -> CuttingActualScreen(viewModel = cuttingViewModel)
        "/mes/actualDataCollection/chamfering" -> ChamferingActualScreen(viewModel = chamferingViewModel)
        "/mes/productionInstruction/cutting" -> CuttingInstructionScreen(viewModel = cuttingInstructionViewModel)
        "/mes/productionInstruction/forming" -> PlanInstructionScreen(viewModel = formingInstructionViewModel)
        "/mes/productionInstruction/welding" -> PlanInstructionScreen(viewModel = weldingInstructionViewModel)
        "/aps/scheduling" -> SchedulingScreen(viewModel = schedulingViewModel)
        "/erp/production/data-management" -> {
            val vm: ProductionDataManagementViewModel = viewModel(
                factory = ProductionDataManagementViewModel.Factory(
                    repository = appContainer.productionSummaryRepository,
                    masterRepository = appContainer.masterRepository,
                ),
            )
            ProductionDataManagementScreen(viewModel = vm)
        }
        "/erp/production/plan-baseline" -> PlanBaselineScreen(viewModel = planBaselineViewModel)
        "/erp/production/plan-schedules" -> PlanScheduleScreen(viewModel = planScheduleViewModel)
        "/erp/production/process-machine-plan" -> ProcessMachinePlanScreen(viewModel = processMachinePlanViewModel)
        "/system/users" -> UserListScreen(viewModel = userListViewModel)
        "/system/organization" -> OrganizationListScreen(viewModel = organizationListViewModel)
        "/system/roles" -> RolePermissionScreen(viewModel = rolePermissionViewModel)
        else -> PlaceholderScreen(path = path)
    }
}
