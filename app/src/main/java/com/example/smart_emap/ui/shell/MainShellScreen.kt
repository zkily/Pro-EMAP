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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
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
import com.example.smart_emap.data.model.ShortcutItemDto
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
import com.example.smart_emap.ui.erp.order.OrderHomeScreen
import com.example.smart_emap.ui.erp.order.OrderHomeViewModel
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
import com.example.smart_emap.ui.erp.production.metrics.UtilizationRateScreen
import com.example.smart_emap.ui.erp.production.metrics.DefectRateScreen
import com.example.smart_emap.ui.erp.production.requirements.MaterialRequirementsScreen
import com.example.smart_emap.ui.erp.production.requirements.MaterialRequirementsViewModel
import com.example.smart_emap.ui.erp.production.requirements.ComponentRequirementsScreen
import com.example.smart_emap.ui.erp.production.requirements.ComponentRequirementsViewModel
import com.example.smart_emap.ui.erp.production.actual.MaterialConsumptionScreen
import com.example.smart_emap.ui.erp.production.actual.MaterialConsumptionViewModel
import com.example.smart_emap.ui.erp.production.actual.ProcessActualScreen
import com.example.smart_emap.ui.erp.production.actual.ProcessActualViewModel
import com.example.smart_emap.ui.erp.production.actual.ProductionActualManagementScreen
import com.example.smart_emap.ui.erp.production.actual.ProductionActualManagementViewModel
import com.example.smart_emap.ui.erp.production.actual.ScrapRateScreen
import com.example.smart_emap.ui.erp.production.actual.ScrapRateViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialForecastViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialOrderViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionScreen
import com.example.smart_emap.ui.erp.purchase.material.MaterialReceivingInspectionViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartOrderScreen
import com.example.smart_emap.ui.erp.purchase.part.PartOrderViewModel
import com.example.smart_emap.ui.erp.purchase.part.PartReceivingHistoryScreen
import com.example.smart_emap.ui.erp.purchase.part.PartReceivingHistoryViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingHomeScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingHomeViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingProcessProductsScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingProcessProductsViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingSuppliersScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.OutsourcingSuppliersViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.MaterialIssueScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.MaterialIssueViewModel
import com.example.smart_emap.ui.erp.inventory.InventoryHomeScreen
import com.example.smart_emap.ui.erp.inventory.InventoryHomeViewModel
import com.example.smart_emap.ui.erp.inventory.MaterialInventoryListScreen
import com.example.smart_emap.ui.erp.inventory.MaterialInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.PartInventoryListScreen
import com.example.smart_emap.ui.erp.inventory.PartInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.ProductInventoryListScreen
import com.example.smart_emap.ui.erp.inventory.ProductInventoryListViewModel
import com.example.smart_emap.ui.erp.inventory.StockEntryScreen
import com.example.smart_emap.ui.erp.inventory.StockEntryViewModel
import com.example.smart_emap.ui.erp.inventory.StockTransactionLogScreen
import com.example.smart_emap.ui.erp.inventory.StockTransactionLogViewModel
import com.example.smart_emap.ui.erp.inventory.StocktakeEntryScreen
import com.example.smart_emap.ui.erp.inventory.StocktakeEntryViewModel
import com.example.smart_emap.ui.erp.inventory.StocktakeHomeScreen
import com.example.smart_emap.ui.erp.inventory.StocktakeListScreen
import com.example.smart_emap.ui.erp.inventory.StocktakeListViewModel
import com.example.smart_emap.ui.erp.inventory.StocktakePlaceholderScreen
import com.example.smart_emap.ui.erp.shipping.AbcAnalysisScreen
import com.example.smart_emap.ui.erp.shipping.AbcAnalysisViewModel
import com.example.smart_emap.ui.erp.shipping.InventoryKpiScreen
import com.example.smart_emap.ui.erp.shipping.InventoryKpiViewModel
import com.example.smart_emap.ui.erp.shipping.InventoryShortageScreen
import com.example.smart_emap.ui.erp.shipping.InventoryShortageViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingDocumentMode
import com.example.smart_emap.ui.erp.shipping.ShippingDocumentScreen
import com.example.smart_emap.ui.erp.shipping.ShippingDocumentViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingHomeScreen
import com.example.smart_emap.ui.erp.shipping.ShippingHomeViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingListScreen
import com.example.smart_emap.ui.erp.shipping.ShippingListViewModel
import com.example.smart_emap.ui.erp.shipping.ShippingPickingScreen
import com.example.smart_emap.ui.erp.shipping.ShippingPickingViewModel
import com.example.smart_emap.ui.erp.shipping.WarehouseDailyScreen
import com.example.smart_emap.ui.erp.shipping.WarehouseDailyViewModel
import com.example.smart_emap.ui.erp.shipping.WeldingShippingScreen
import com.example.smart_emap.ui.erp.shipping.WeldingShippingViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.SuppliedMaterialStockScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.SuppliedMaterialStockViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.UsageManagementScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.material.UsageManagementViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingOrderScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingOrderViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingReceivingScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.plating.PlatingReceivingViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.stock.OutsourcingStockScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.stock.OutsourcingStockViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingOrderScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingOrderViewModel
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingReceivingScreen
import com.example.smart_emap.ui.erp.purchase.outsourcing.welding.WeldingReceivingViewModel
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
import com.example.smart_emap.ui.master.equipmentefficiency.EquipmentEfficiencyMasterScreen
import com.example.smart_emap.ui.master.equipmentefficiency.EquipmentEfficiencyMasterViewModel
import com.example.smart_emap.ui.master.productmachineconfig.ProductMachineConfigMasterScreen
import com.example.smart_emap.ui.master.productmachineconfig.ProductMachineConfigMasterViewModel
import com.example.smart_emap.ui.master.productprocessbom.ProductProcessBomMasterScreen
import com.example.smart_emap.ui.master.productprocessbom.ProductProcessBomMasterViewModel
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
import com.example.smart_emap.core.auth.OperationModules
import com.example.smart_emap.core.auth.canEdit
import com.example.smart_emap.core.network.ApiDefaults
import com.example.smart_emap.ui.mes.inspectionregistration.InspectionManualRegistrationScreen
import com.example.smart_emap.ui.mes.inspectionregistration.InspectionManualRegistrationViewModel
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityScreen
import com.example.smart_emap.ui.mes.productivity.InspectionProductivityViewModel
import com.example.smart_emap.ui.mes.productivity.WeldingProductivityScreen
import com.example.smart_emap.ui.mes.productivity.WeldingProductivityViewModel
import com.example.smart_emap.ui.mes.monitoring.MonitorProcessKey
import com.example.smart_emap.ui.mes.monitoring.InspectionMonitorScreen
import com.example.smart_emap.ui.mes.monitoring.ProcessMonitorScreen
import com.example.smart_emap.ui.mes.monitoring.ProcessMonitorViewModel
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
            sessionStore = appContainer.sessionStore,
            defaultApiBaseUrl = ApiDefaults.displayBaseUrl,
            canMesEdit = user.canEdit(OperationModules.MES),
            userId = user.id,
            inspectorLabel = user.fullName?.trim().orEmpty().ifEmpty { user.username },
        ),
    )
    val inspectionManualRegistrationViewModel: InspectionManualRegistrationViewModel = viewModel(
        factory = InspectionManualRegistrationViewModel.Factory(
            repository = appContainer.inspectionRepository,
            loggedInUserId = user.id,
        ),
    )
    val inspectionUtilizationViewModel: InspectionUtilizationViewModel = viewModel(
        factory = InspectionUtilizationViewModel.Factory(
            inspectionRepository = appContainer.inspectionRepository,
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
    val inspectionMonitorViewModel: ProcessMonitorViewModel = viewModel(
        key = "monitor_inspection",
        factory = ProcessMonitorViewModel.Factory(
            processKey = MonitorProcessKey.INSPECTION,
            inspectionRepository = appContainer.inspectionRepository,
            weldingRepository = appContainer.weldingRepository,
            systemUserRepository = appContainer.systemUserRepository,
        ),
    )
    val weldingMonitorViewModel: ProcessMonitorViewModel = viewModel(
        key = "monitor_welding",
        factory = ProcessMonitorViewModel.Factory(
            processKey = MonitorProcessKey.WELDING,
            inspectionRepository = appContainer.inspectionRepository,
            weldingRepository = appContainer.weldingRepository,
            systemUserRepository = appContainer.systemUserRepository,
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

    val orderHomeViewModel: OrderHomeViewModel = viewModel(
        factory = OrderHomeViewModel.Factory(
            repository = appContainer.orderMonthlyRepository,
            dailyRepository = appContainer.orderDailyRepository,
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

    val partReceivingHistoryViewModel: PartReceivingHistoryViewModel = viewModel(
        factory = PartReceivingHistoryViewModel.Factory(
            repository = appContainer.partRepository,
        ),
    )

    val outsourcingHomeViewModel: OutsourcingHomeViewModel = viewModel(
        factory = OutsourcingHomeViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val platingOrderViewModel: PlatingOrderViewModel = viewModel(
        factory = PlatingOrderViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val platingReceivingViewModel: PlatingReceivingViewModel = viewModel(
        factory = PlatingReceivingViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val weldingOrderViewModel: WeldingOrderViewModel = viewModel(
        factory = WeldingOrderViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val weldingReceivingViewModel: WeldingReceivingViewModel = viewModel(
        factory = WeldingReceivingViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val outsourcingSuppliersViewModel: OutsourcingSuppliersViewModel = viewModel(
        factory = OutsourcingSuppliersViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val outsourcingProcessProductsViewModel: OutsourcingProcessProductsViewModel = viewModel(
        factory = OutsourcingProcessProductsViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val outsourcingStockViewModel: OutsourcingStockViewModel = viewModel(
        factory = OutsourcingStockViewModel.Factory(
            repository = appContainer.outsourcingRepository,
        ),
    )
    val suppliedMaterialStockViewModel: SuppliedMaterialStockViewModel = viewModel(
        factory = SuppliedMaterialStockViewModel.Factory(),
    )
    val usageManagementViewModel: UsageManagementViewModel = viewModel(
        factory = UsageManagementViewModel.Factory(),
    )
    val materialIssueViewModel: MaterialIssueViewModel = viewModel(
        factory = MaterialIssueViewModel.Factory(),
    )

    val inventoryHomeViewModel: InventoryHomeViewModel = viewModel(
        factory = InventoryHomeViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val productInventoryListViewModel: ProductInventoryListViewModel = viewModel(
        factory = ProductInventoryListViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val materialInventoryListViewModel: MaterialInventoryListViewModel = viewModel(
        factory = MaterialInventoryListViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val partInventoryListViewModel: PartInventoryListViewModel = viewModel(
        factory = PartInventoryListViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val stockEntryViewModel: StockEntryViewModel = viewModel(
        factory = StockEntryViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val stockTransactionLogViewModel: StockTransactionLogViewModel = viewModel(
        factory = StockTransactionLogViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val stocktakeListViewModel: StocktakeListViewModel = viewModel(
        factory = StocktakeListViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )
    val stocktakeEntryViewModel: StocktakeEntryViewModel = viewModel(
        factory = StocktakeEntryViewModel.Factory(
            repository = appContainer.inventoryRepository,
        ),
    )

    val shippingHomeViewModel: ShippingHomeViewModel = viewModel(
        factory = ShippingHomeViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val shippingListViewModel: ShippingListViewModel = viewModel(
        factory = ShippingListViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val shippingReportViewModel: ShippingDocumentViewModel = viewModel(
        factory = ShippingDocumentViewModel.Factory(appContainer.shippingRepository, ShippingDocumentMode.REPORT),
    )
    val shippingOverviewViewModel: ShippingDocumentViewModel = viewModel(
        factory = ShippingDocumentViewModel.Factory(appContainer.shippingRepository, ShippingDocumentMode.OVERVIEW),
    )
    val shippingConfirmViewModel: ShippingDocumentViewModel = viewModel(
        factory = ShippingDocumentViewModel.Factory(appContainer.shippingRepository, ShippingDocumentMode.CONFIRM),
    )
    val shippingPickingViewModel: ShippingPickingViewModel = viewModel(
        factory = ShippingPickingViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val weldingShippingViewModel: WeldingShippingViewModel = viewModel(
        factory = WeldingShippingViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val inventoryShortageViewModel: InventoryShortageViewModel = viewModel(
        factory = InventoryShortageViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val inventoryKpiViewModel: InventoryKpiViewModel = viewModel(
        factory = InventoryKpiViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val warehouseDailyViewModel: WarehouseDailyViewModel = viewModel(
        factory = WarehouseDailyViewModel.Factory(repository = appContainer.shippingRepository),
    )
    val abcAnalysisViewModel: AbcAnalysisViewModel = viewModel(
        factory = AbcAnalysisViewModel.Factory(repository = appContainer.shippingRepository),
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

    val productProcessBomMasterViewModel: ProductProcessBomMasterViewModel = viewModel(
        factory = ProductProcessBomMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val productMachineConfigMasterViewModel: ProductMachineConfigMasterViewModel = viewModel(
        factory = ProductMachineConfigMasterViewModel.Factory(
            repository = appContainer.masterRepository,
        ),
    )

    val equipmentEfficiencyMasterViewModel: EquipmentEfficiencyMasterViewModel = viewModel(
        factory = EquipmentEfficiencyMasterViewModel.Factory(
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

    val materialRequirementsViewModel: MaterialRequirementsViewModel = viewModel(
        factory = MaterialRequirementsViewModel.Factory(
            repository = appContainer.productionRequirementsRepository,
        ),
    )

    val componentRequirementsViewModel: ComponentRequirementsViewModel = viewModel(
        factory = ComponentRequirementsViewModel.Factory(
            repository = appContainer.productionRequirementsRepository,
        ),
    )

    val materialConsumptionViewModel: MaterialConsumptionViewModel = viewModel(
        factory = MaterialConsumptionViewModel.Factory(
            repository = appContainer.productionActualRepository,
        ),
    )

    val processActualViewModel: ProcessActualViewModel = viewModel(
        factory = ProcessActualViewModel.Factory(
            repository = appContainer.productionActualRepository,
        ),
    )

    val scrapRateViewModel: ScrapRateViewModel = viewModel(
        factory = ScrapRateViewModel.Factory(
            repository = appContainer.productionActualRepository,
        ),
    )

    val productionActualManagementViewModel: ProductionActualManagementViewModel = viewModel(
        factory = ProductionActualManagementViewModel.Factory(
            repository = appContainer.productionActualRepository,
        ),
    )

    val productionDataManagementViewModel: ProductionDataManagementViewModel = viewModel(
        factory = ProductionDataManagementViewModel.Factory(
            repository = appContainer.productionSummaryRepository,
            masterRepository = appContainer.masterRepository,
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
    val shortcutsViewModel: SidebarShortcutsViewModel = viewModel(
        factory = SidebarShortcutsViewModel.Factory(appContainer.sidebarShortcutsRepository),
    )
    val shellState by shellViewModel.uiState.collectAsState()
    val shortcutsState by shortcutsViewModel.uiState.collectAsState()
    val activePath = shellState.activePath
    val tabs = shellState.tabs

    LaunchedEffect(user.id) {
        shortcutsViewModel.load()
    }

    LaunchedEffect(activePath) {
        shortcutsViewModel.recordVisit(activePath)
    }

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
            shortcutsPinned = shortcutsState.pinned,
            shortcutsFrequent = shortcutsState.frequent,
            dashboardViewModel = dashboardViewModel,
            inspectionViewModel = inspectionViewModel,
            inspectionManualRegistrationViewModel = inspectionManualRegistrationViewModel,
            inspectionUtilizationViewModel = inspectionUtilizationViewModel,
            inspectionProductivityViewModel = inspectionProductivityViewModel,
            weldingProductivityViewModel = weldingProductivityViewModel,
            inspectionMonitorViewModel = inspectionMonitorViewModel,
            weldingMonitorViewModel = weldingMonitorViewModel,
            weldingViewModel = weldingViewModel,
            cuttingViewModel = cuttingViewModel,
            chamferingViewModel = chamferingViewModel,
            orderMonthlyViewModel = orderMonthlyViewModel,
            orderDailyViewModel = orderDailyViewModel,
            orderDestinationHistoryViewModel = orderDestinationHistoryViewModel,
            orderHomeViewModel = orderHomeViewModel,
            materialReceivingHistoryViewModel = materialReceivingHistoryViewModel,
            materialReceivingInspectionViewModel = materialReceivingInspectionViewModel,
            materialForecastViewModel = materialForecastViewModel,
            materialOrderViewModel = materialOrderViewModel,
            partOrderViewModel = partOrderViewModel,
            partReceivingHistoryViewModel = partReceivingHistoryViewModel,
            outsourcingHomeViewModel = outsourcingHomeViewModel,
            platingOrderViewModel = platingOrderViewModel,
            platingReceivingViewModel = platingReceivingViewModel,
            weldingOrderViewModel = weldingOrderViewModel,
            weldingReceivingViewModel = weldingReceivingViewModel,
            outsourcingSuppliersViewModel = outsourcingSuppliersViewModel,
            outsourcingProcessProductsViewModel = outsourcingProcessProductsViewModel,
            outsourcingStockViewModel = outsourcingStockViewModel,
            suppliedMaterialStockViewModel = suppliedMaterialStockViewModel,
            usageManagementViewModel = usageManagementViewModel,
            materialIssueViewModel = materialIssueViewModel,
            inventoryHomeViewModel = inventoryHomeViewModel,
            productInventoryListViewModel = productInventoryListViewModel,
            materialInventoryListViewModel = materialInventoryListViewModel,
            partInventoryListViewModel = partInventoryListViewModel,
            stockEntryViewModel = stockEntryViewModel,
            stockTransactionLogViewModel = stockTransactionLogViewModel,
            stocktakeListViewModel = stocktakeListViewModel,
            stocktakeEntryViewModel = stocktakeEntryViewModel,
            shippingHomeViewModel = shippingHomeViewModel,
            shippingListViewModel = shippingListViewModel,
            shippingReportViewModel = shippingReportViewModel,
            shippingOverviewViewModel = shippingOverviewViewModel,
            shippingConfirmViewModel = shippingConfirmViewModel,
            shippingPickingViewModel = shippingPickingViewModel,
            weldingShippingViewModel = weldingShippingViewModel,
            inventoryShortageViewModel = inventoryShortageViewModel,
            inventoryKpiViewModel = inventoryKpiViewModel,
            warehouseDailyViewModel = warehouseDailyViewModel,
            abcAnalysisViewModel = abcAnalysisViewModel,
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
            productProcessBomMasterViewModel = productProcessBomMasterViewModel,
            productMachineConfigMasterViewModel = productMachineConfigMasterViewModel,
            equipmentEfficiencyMasterViewModel = equipmentEfficiencyMasterViewModel,
            companyWorkCalendarViewModel = companyWorkCalendarViewModel,
            cuttingInstructionViewModel = cuttingInstructionViewModel,
            formingInstructionViewModel = formingInstructionViewModel,
            weldingInstructionViewModel = weldingInstructionViewModel,
            schedulingViewModel = schedulingViewModel,
            planBaselineViewModel = planBaselineViewModel,
            planScheduleViewModel = planScheduleViewModel,
            processMachinePlanViewModel = processMachinePlanViewModel,
            productionDataManagementViewModel = productionDataManagementViewModel,
            materialRequirementsViewModel = materialRequirementsViewModel,
            componentRequirementsViewModel = componentRequirementsViewModel,
            materialConsumptionViewModel = materialConsumptionViewModel,
            processActualViewModel = processActualViewModel,
            scrapRateViewModel = scrapRateViewModel,
            productionActualManagementViewModel = productionActualManagementViewModel,
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
    shortcutsPinned: List<ShortcutItemDto> = emptyList(),
    shortcutsFrequent: List<ShortcutItemDto> = emptyList(),
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
    materialConsumptionViewModel: MaterialConsumptionViewModel,
    processActualViewModel: ProcessActualViewModel,
    scrapRateViewModel: ScrapRateViewModel,
    productionActualManagementViewModel: ProductionActualManagementViewModel,
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
                    shortcutsPinned = shortcutsPinned,
                    shortcutsFrequent = shortcutsFrequent,
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
                            "/mes/actualCollectionRegistration/inspection" -> inspectionManualRegistrationViewModel.refreshAll()
                            "/mes/actualAnalysis/utilization/inspection" -> inspectionUtilizationViewModel.refreshAll()
                            "/mes/actualAnalysis/productivity/inspection" -> inspectionProductivityViewModel.refreshAll()
                            "/mes/actualAnalysis/productivity/welding" -> weldingProductivityViewModel.refreshAll()
                            "/mes/monitoring/inspection" -> inspectionMonitorViewModel.refreshAll()
                            "/mes/monitoring/welding" -> weldingMonitorViewModel.refreshAll()
                            "/mes/actualDataCollection/welding" -> weldingViewModel.refreshAll()
                            "/mes/actualDataCollection/cutting" -> cuttingViewModel.refreshAll()
                            "/mes/actualDataCollection/chamfering" -> chamferingViewModel.refreshAll()
                            "/erp/order" -> orderHomeViewModel.refreshAll()
                            "/erp/order/monthly" -> orderMonthlyViewModel.refreshAll()
                            "/erp/order/daily" -> orderDailyViewModel.refreshAll()
                            "/erp/order/destination-history" -> orderDestinationHistoryViewModel.loadDestinationOptions()
                            "/erp/purchase/material" -> materialOrderViewModel.refreshAll()
                            "/erp/purchase/material/receiving-history" -> materialReceivingHistoryViewModel.refreshAll()
                            "/erp/purchase/material/receiving-inspection" -> materialReceivingInspectionViewModel.refreshAll()
                            "/erp/purchase/material/forecast" -> materialForecastViewModel.refreshAll()
                            "/erp/purchase/material/order" -> materialOrderViewModel.refreshAll()
                            "/erp/purchase/part/order" -> partOrderViewModel.refreshAll()
                            "/erp/purchase/part/receiving-history" -> partReceivingHistoryViewModel.refreshAll()
                            "/erp/purchase/outsourcing" -> outsourcingHomeViewModel.refreshAll()
                            "/erp/purchase/outsourcing/plating-order" -> platingOrderViewModel.refreshAll()
                            "/erp/purchase/outsourcing/plating-receiving" -> platingReceivingViewModel.refreshAll()
                            "/erp/purchase/outsourcing/welding-order" -> weldingOrderViewModel.refreshAll()
                            "/erp/purchase/outsourcing/welding-receiving" -> weldingReceivingViewModel.refreshAll()
                            "/erp/purchase/outsourcing/suppliers" -> outsourcingSuppliersViewModel.refreshAll()
                            "/erp/purchase/outsourcing/process-products" -> outsourcingProcessProductsViewModel.refreshAll()
                            "/erp/purchase/outsourcing/stock" -> outsourcingStockViewModel.refreshAll()
                            "/erp/purchase/outsourcing/supplied-material-stock" -> suppliedMaterialStockViewModel.refreshAll()
                            "/erp/purchase/outsourcing/usage" -> usageManagementViewModel.refreshAll()
                            "/erp/purchase/outsourcing/material-issue" -> materialIssueViewModel.refreshAll()
                            "/erp/inventory" -> inventoryHomeViewModel.refreshAll()
                            "/erp/inventory/list" -> productInventoryListViewModel.refreshAll()
                            "/erp/inventory/material-list" -> materialInventoryListViewModel.refreshAll()
                            "/erp/inventory/part-list" -> partInventoryListViewModel.refreshAll()
                            "/erp/inventory/stock-entry" -> stockEntryViewModel.refreshOptions()
                            "/erp/inventory/stock-transaction-logs" -> stockTransactionLogViewModel.refreshAll()
                            "/erp/inventory/stocktake/list" -> stocktakeListViewModel.refreshAll()
                            "/erp/inventory/stocktake/entry" -> stocktakeEntryViewModel.refreshOptions()
                            "/erp/shipping" -> shippingHomeViewModel.refreshAll()
                            "/erp/shipping/list" -> shippingListViewModel.search()
                            "/erp/shipping/report" -> shippingReportViewModel.search()
                            "/erp/shipping/overview" -> shippingOverviewViewModel.search()
                            "/erp/shipping/confirm" -> shippingConfirmViewModel.search()
                            "/erp/shipping/picking" -> shippingPickingViewModel.refresh()
                            "/erp/shipping/welding" -> weldingShippingViewModel.search()
                            "/erp/shipping/inventory-shortage" -> inventoryShortageViewModel.search()
                            "/erp/shipping/inventory-kpi" -> inventoryKpiViewModel.search()
                            "/erp/shipping/warehouse-daily" -> warehouseDailyViewModel.search()
                            "/erp/shipping/abc-analysis" -> abcAnalysisViewModel.search()
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
                                    "/master/bom/product-process" -> productProcessBomMasterViewModel.refreshAll()
                                    "/master/bom/product-machine-config" -> productMachineConfigMasterViewModel.refreshAll()
                                    "/master/bom/equipment-efficiency" -> equipmentEfficiencyMasterViewModel.refreshAll()
                                    "/master/company-work-calendar" -> companyWorkCalendarViewModel.refreshAll()
                                    else -> masterViewModel.refreshAll()
                                }
                            }
                        }
                    },
                    onCloseOthers = shellViewModel::closeOtherTabs,
                )
                val pageLoading = rememberShellPageLoading(
                    activePath = activePath,
                    dashboardViewModel = dashboardViewModel,
                    inspectionViewModel = inspectionViewModel,
                    inspectionManualRegistrationViewModel = inspectionManualRegistrationViewModel,
                    inspectionUtilizationViewModel = inspectionUtilizationViewModel,
                    inspectionProductivityViewModel = inspectionProductivityViewModel,
                    weldingProductivityViewModel = weldingProductivityViewModel,
                    inspectionMonitorViewModel = inspectionMonitorViewModel,
                    weldingMonitorViewModel = weldingMonitorViewModel,
                    weldingViewModel = weldingViewModel,
                    cuttingViewModel = cuttingViewModel,
                    chamferingViewModel = chamferingViewModel,
                    orderMonthlyViewModel = orderMonthlyViewModel,
                    orderDailyViewModel = orderDailyViewModel,
                    orderDestinationHistoryViewModel = orderDestinationHistoryViewModel,
                    orderHomeViewModel = orderHomeViewModel,
                    materialReceivingHistoryViewModel = materialReceivingHistoryViewModel,
                    materialReceivingInspectionViewModel = materialReceivingInspectionViewModel,
                    materialForecastViewModel = materialForecastViewModel,
                    materialOrderViewModel = materialOrderViewModel,
                    partOrderViewModel = partOrderViewModel,
                    partReceivingHistoryViewModel = partReceivingHistoryViewModel,
                    outsourcingHomeViewModel = outsourcingHomeViewModel,
                    platingOrderViewModel = platingOrderViewModel,
                    platingReceivingViewModel = platingReceivingViewModel,
                    weldingOrderViewModel = weldingOrderViewModel,
                    weldingReceivingViewModel = weldingReceivingViewModel,
                    outsourcingSuppliersViewModel = outsourcingSuppliersViewModel,
                    outsourcingProcessProductsViewModel = outsourcingProcessProductsViewModel,
                    outsourcingStockViewModel = outsourcingStockViewModel,
                    suppliedMaterialStockViewModel = suppliedMaterialStockViewModel,
                    usageManagementViewModel = usageManagementViewModel,
                    materialIssueViewModel = materialIssueViewModel,
                    inventoryHomeViewModel = inventoryHomeViewModel,
                    productInventoryListViewModel = productInventoryListViewModel,
                    materialInventoryListViewModel = materialInventoryListViewModel,
                    partInventoryListViewModel = partInventoryListViewModel,
                    stockEntryViewModel = stockEntryViewModel,
                    stockTransactionLogViewModel = stockTransactionLogViewModel,
                    stocktakeListViewModel = stocktakeListViewModel,
                    stocktakeEntryViewModel = stocktakeEntryViewModel,
                    shippingHomeViewModel = shippingHomeViewModel,
                    shippingListViewModel = shippingListViewModel,
                    shippingReportViewModel = shippingReportViewModel,
                    shippingOverviewViewModel = shippingOverviewViewModel,
                    shippingConfirmViewModel = shippingConfirmViewModel,
                    shippingPickingViewModel = shippingPickingViewModel,
                    weldingShippingViewModel = weldingShippingViewModel,
                    inventoryShortageViewModel = inventoryShortageViewModel,
                    inventoryKpiViewModel = inventoryKpiViewModel,
                    warehouseDailyViewModel = warehouseDailyViewModel,
                    abcAnalysisViewModel = abcAnalysisViewModel,
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
                    productProcessBomMasterViewModel = productProcessBomMasterViewModel,
                    productMachineConfigMasterViewModel = productMachineConfigMasterViewModel,
                    equipmentEfficiencyMasterViewModel = equipmentEfficiencyMasterViewModel,
                    companyWorkCalendarViewModel = companyWorkCalendarViewModel,
                    cuttingInstructionViewModel = cuttingInstructionViewModel,
                    formingInstructionViewModel = formingInstructionViewModel,
                    weldingInstructionViewModel = weldingInstructionViewModel,
                    schedulingViewModel = schedulingViewModel,
                    planBaselineViewModel = planBaselineViewModel,
                    planScheduleViewModel = planScheduleViewModel,
                    processMachinePlanViewModel = processMachinePlanViewModel,
                    productionDataManagementViewModel = productionDataManagementViewModel,
                    materialRequirementsViewModel = materialRequirementsViewModel,
                    componentRequirementsViewModel = componentRequirementsViewModel,
                    userListViewModel = userListViewModel,
                    organizationListViewModel = organizationListViewModel,
                    rolePermissionViewModel = rolePermissionViewModel,
                )
                ShellRouteLoadingHost(
                    activePath = activePath,
                    isRouteLoading = shellState.isRouteLoading,
                    pageLoading = pageLoading,
                    onRouteReady = shellViewModel::finishRouteTransition,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    key(activePath) {
                        ShellRouteContent(
                            path = activePath,
                            user = user,
                            dashboardViewModel = dashboardViewModel,
                            inspectionViewModel = inspectionViewModel,
                            inspectionManualRegistrationViewModel = inspectionManualRegistrationViewModel,
                            inspectionUtilizationViewModel = inspectionUtilizationViewModel,
                            inspectionProductivityViewModel = inspectionProductivityViewModel,
                            weldingProductivityViewModel = weldingProductivityViewModel,
                            inspectionMonitorViewModel = inspectionMonitorViewModel,
                            weldingMonitorViewModel = weldingMonitorViewModel,
                            weldingViewModel = weldingViewModel,
                            cuttingViewModel = cuttingViewModel,
                            chamferingViewModel = chamferingViewModel,
                            orderMonthlyViewModel = orderMonthlyViewModel,
                            orderDailyViewModel = orderDailyViewModel,
                            orderDestinationHistoryViewModel = orderDestinationHistoryViewModel,
                            orderHomeViewModel = orderHomeViewModel,
                            materialReceivingHistoryViewModel = materialReceivingHistoryViewModel,
                            materialReceivingInspectionViewModel = materialReceivingInspectionViewModel,
                            materialForecastViewModel = materialForecastViewModel,
                            materialOrderViewModel = materialOrderViewModel,
                            partOrderViewModel = partOrderViewModel,
                            partReceivingHistoryViewModel = partReceivingHistoryViewModel,
                            outsourcingHomeViewModel = outsourcingHomeViewModel,
                            platingOrderViewModel = platingOrderViewModel,
                            platingReceivingViewModel = platingReceivingViewModel,
                            weldingOrderViewModel = weldingOrderViewModel,
                            weldingReceivingViewModel = weldingReceivingViewModel,
                            outsourcingSuppliersViewModel = outsourcingSuppliersViewModel,
                            outsourcingProcessProductsViewModel = outsourcingProcessProductsViewModel,
                            outsourcingStockViewModel = outsourcingStockViewModel,
                            suppliedMaterialStockViewModel = suppliedMaterialStockViewModel,
                            usageManagementViewModel = usageManagementViewModel,
                            materialIssueViewModel = materialIssueViewModel,
                            inventoryHomeViewModel = inventoryHomeViewModel,
                            productInventoryListViewModel = productInventoryListViewModel,
                            materialInventoryListViewModel = materialInventoryListViewModel,
                            partInventoryListViewModel = partInventoryListViewModel,
                            stockEntryViewModel = stockEntryViewModel,
                            stockTransactionLogViewModel = stockTransactionLogViewModel,
                            stocktakeListViewModel = stocktakeListViewModel,
                            stocktakeEntryViewModel = stocktakeEntryViewModel,
                            shippingHomeViewModel = shippingHomeViewModel,
                            shippingListViewModel = shippingListViewModel,
                            shippingReportViewModel = shippingReportViewModel,
                            shippingOverviewViewModel = shippingOverviewViewModel,
                            shippingConfirmViewModel = shippingConfirmViewModel,
                            shippingPickingViewModel = shippingPickingViewModel,
                            weldingShippingViewModel = weldingShippingViewModel,
                            inventoryShortageViewModel = inventoryShortageViewModel,
                            inventoryKpiViewModel = inventoryKpiViewModel,
                            warehouseDailyViewModel = warehouseDailyViewModel,
                            abcAnalysisViewModel = abcAnalysisViewModel,
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
                            productProcessBomMasterViewModel = productProcessBomMasterViewModel,
                            productMachineConfigMasterViewModel = productMachineConfigMasterViewModel,
                            equipmentEfficiencyMasterViewModel = equipmentEfficiencyMasterViewModel,
                            companyWorkCalendarViewModel = companyWorkCalendarViewModel,
                            cuttingInstructionViewModel = cuttingInstructionViewModel,
                            formingInstructionViewModel = formingInstructionViewModel,
                            weldingInstructionViewModel = weldingInstructionViewModel,
                            schedulingViewModel = schedulingViewModel,
                            planBaselineViewModel = planBaselineViewModel,
                            planScheduleViewModel = planScheduleViewModel,
                            processMachinePlanViewModel = processMachinePlanViewModel,
                            productionDataManagementViewModel = productionDataManagementViewModel,
                            materialRequirementsViewModel = materialRequirementsViewModel,
                            componentRequirementsViewModel = componentRequirementsViewModel,
                            materialConsumptionViewModel = materialConsumptionViewModel,
                            processActualViewModel = processActualViewModel,
                            scrapRateViewModel = scrapRateViewModel,
                            productionActualManagementViewModel = productionActualManagementViewModel,
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
    user: UserDto,
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
    materialConsumptionViewModel: MaterialConsumptionViewModel,
    processActualViewModel: ProcessActualViewModel,
    scrapRateViewModel: ScrapRateViewModel,
    productionActualManagementViewModel: ProductionActualManagementViewModel,
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
        "/master/bom/product-process" -> ProductProcessBomMasterScreen(viewModel = productProcessBomMasterViewModel)
        "/master/bom/product-machine-config" -> ProductMachineConfigMasterScreen(viewModel = productMachineConfigMasterViewModel)
        "/master/bom/equipment-efficiency" -> EquipmentEfficiencyMasterScreen(viewModel = equipmentEfficiencyMasterViewModel)
        "/master/company-work-calendar" -> CompanyWorkCalendarScreen(viewModel = companyWorkCalendarViewModel)
        "/master/bom/process-processing-fee",
        "/master/machine",
        "/master/roller-master",
        "/master/destination",
        "/master/destination/holiday",
        -> MasterScreen(path = path, viewModel = masterViewModel)
        "/erp/order" -> OrderHomeScreen(viewModel = orderHomeViewModel)
        "/erp/order/monthly" -> OrderMonthlyScreen(viewModel = orderMonthlyViewModel)
        "/erp/order/daily" -> OrderDailyScreen(viewModel = orderDailyViewModel)
        "/erp/order/destination-history" -> OrderDestinationHistoryScreen(viewModel = orderDestinationHistoryViewModel)
        "/erp/purchase/material" -> MaterialOrderScreen(viewModel = materialOrderViewModel)
        "/erp/purchase/material/receiving-history" -> MaterialReceivingHistoryScreen(viewModel = materialReceivingHistoryViewModel)
        "/erp/purchase/material/receiving-inspection" -> MaterialReceivingInspectionScreen(viewModel = materialReceivingInspectionViewModel)
        "/erp/purchase/material/forecast" -> MaterialForecastScreen(viewModel = materialForecastViewModel)
        "/erp/purchase/material/order" -> MaterialOrderScreen(viewModel = materialOrderViewModel)
        "/erp/purchase/part/order" -> PartOrderScreen(viewModel = partOrderViewModel)
        "/erp/purchase/part/receiving-history" -> PartReceivingHistoryScreen(viewModel = partReceivingHistoryViewModel)
        "/erp/purchase/outsourcing" -> OutsourcingHomeScreen(
            viewModel = outsourcingHomeViewModel,
            onNavigate = onNavigate,
        )
        "/erp/purchase/outsourcing/plating-order" -> PlatingOrderScreen(viewModel = platingOrderViewModel)
        "/erp/purchase/outsourcing/plating-receiving" -> PlatingReceivingScreen(viewModel = platingReceivingViewModel)
        "/erp/purchase/outsourcing/welding-order" -> WeldingOrderScreen(viewModel = weldingOrderViewModel)
        "/erp/purchase/outsourcing/welding-receiving" -> WeldingReceivingScreen(viewModel = weldingReceivingViewModel)
        "/erp/purchase/outsourcing/suppliers" -> OutsourcingSuppliersScreen(viewModel = outsourcingSuppliersViewModel)
        "/erp/purchase/outsourcing/process-products" -> OutsourcingProcessProductsScreen(viewModel = outsourcingProcessProductsViewModel)
        "/erp/purchase/outsourcing/stock" -> OutsourcingStockScreen(viewModel = outsourcingStockViewModel)
        "/erp/purchase/outsourcing/supplied-material-stock" -> SuppliedMaterialStockScreen(viewModel = suppliedMaterialStockViewModel)
        "/erp/purchase/outsourcing/usage" -> UsageManagementScreen(viewModel = usageManagementViewModel)
        "/erp/purchase/outsourcing/material-issue" -> MaterialIssueScreen(viewModel = materialIssueViewModel)
        "/erp/inventory" -> InventoryHomeScreen(
            viewModel = inventoryHomeViewModel,
            onNavigate = onNavigate,
        )
        "/erp/inventory/list" -> ProductInventoryListScreen(viewModel = productInventoryListViewModel)
        "/erp/inventory/material-list" -> MaterialInventoryListScreen(viewModel = materialInventoryListViewModel)
        "/erp/inventory/part-list" -> PartInventoryListScreen(viewModel = partInventoryListViewModel)
        "/erp/inventory/stock-entry" -> StockEntryScreen(viewModel = stockEntryViewModel)
        "/erp/inventory/stock-transaction-logs" -> StockTransactionLogScreen(viewModel = stockTransactionLogViewModel)
        "/erp/inventory/stocktake" -> StocktakeHomeScreen(onNavigate = onNavigate)
        "/erp/inventory/stocktake/list" -> StocktakeListScreen(viewModel = stocktakeListViewModel)
        "/erp/inventory/stocktake/entry" -> StocktakeEntryScreen(viewModel = stocktakeEntryViewModel)
        "/erp/inventory/stocktake/statistics" -> StocktakePlaceholderScreen(title = "棚卸分析")
        "/erp/inventory/stocktake/value" -> StocktakePlaceholderScreen(title = "棚卸金額管理")
        "/erp/inventory/stocktake/carryover" -> StocktakePlaceholderScreen(title = "棚卸繰越管理")
        "/erp/shipping" -> ShippingHomeScreen(viewModel = shippingHomeViewModel, onNavigate = onNavigate)
        "/erp/shipping/list" -> ShippingListScreen(viewModel = shippingListViewModel)
        "/erp/shipping/report" -> ShippingDocumentScreen(viewModel = shippingReportViewModel)
        "/erp/shipping/overview" -> ShippingDocumentScreen(viewModel = shippingOverviewViewModel)
        "/erp/shipping/confirm" -> ShippingDocumentScreen(viewModel = shippingConfirmViewModel)
        "/erp/shipping/welding" -> WeldingShippingScreen(viewModel = weldingShippingViewModel)
        "/erp/shipping/picking" -> ShippingPickingScreen(viewModel = shippingPickingViewModel)
        "/erp/shipping/inventory-shortage" -> InventoryShortageScreen(viewModel = inventoryShortageViewModel)
        "/erp/shipping/inventory-kpi" -> InventoryKpiScreen(viewModel = inventoryKpiViewModel)
        "/erp/shipping/warehouse-daily" -> WarehouseDailyScreen(viewModel = warehouseDailyViewModel)
        "/erp/shipping/abc-analysis" -> AbcAnalysisScreen(viewModel = abcAnalysisViewModel)
        "/mes/actualDataCollection/inspection" -> InspectionActualScreen(viewModel = inspectionViewModel)
        "/mes/actualCollectionRegistration/inspection" -> InspectionManualRegistrationScreen(
            viewModel = inspectionManualRegistrationViewModel,
            user = user,
        )
        "/mes/actualCollectionRegistration/cutting",
        "/mes/actualCollectionRegistration/chamfering",
        "/mes/actualCollectionRegistration/forming",
        "/mes/actualCollectionRegistration/plating",
        "/mes/actualCollectionRegistration/welding",
        -> PlaceholderScreen(path = path)
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
        "/mes/monitoring/inspection" -> InspectionMonitorScreen(
            viewModel = inspectionMonitorViewModel,
            onNavigate = onNavigate,
        )
        "/mes/monitoring/welding" -> ProcessMonitorScreen(viewModel = weldingMonitorViewModel)
        "/mes/actualDataCollection/welding" -> WeldingActualScreen(viewModel = weldingViewModel)
        "/mes/actualDataCollection/cutting" -> CuttingActualScreen(viewModel = cuttingViewModel)
        "/mes/actualDataCollection/chamfering" -> ChamferingActualScreen(viewModel = chamferingViewModel)
        "/mes/productionInstruction/cutting" -> CuttingInstructionScreen(viewModel = cuttingInstructionViewModel)
        "/mes/productionInstruction/forming" -> PlanInstructionScreen(viewModel = formingInstructionViewModel)
        "/mes/productionInstruction/welding" -> PlanInstructionScreen(viewModel = weldingInstructionViewModel)
        "/aps/scheduling" -> SchedulingScreen(viewModel = schedulingViewModel)
        "/erp/production/data-management" -> ProductionDataManagementScreen(
            viewModel = productionDataManagementViewModel,
        )
        "/erp/production/plan-baseline" -> PlanBaselineScreen(viewModel = planBaselineViewModel)
        "/erp/production/plan-schedules" -> PlanScheduleScreen(viewModel = planScheduleViewModel)
        "/erp/production/process-machine-plan" -> ProcessMachinePlanScreen(viewModel = processMachinePlanViewModel)
        "/erp/production/metrics/utilization-rate" -> UtilizationRateScreen()
        "/erp/production/metrics/defect-rate" -> DefectRateScreen()
        "/erp/production/metrics/scrap-rate" -> ScrapRateScreen(viewModel = scrapRateViewModel)
        "/erp/production-requirements/material" -> MaterialRequirementsScreen(viewModel = materialRequirementsViewModel)
        "/erp/production-requirements/component" -> ComponentRequirementsScreen(viewModel = componentRequirementsViewModel)
        "/erp/production/consumption" -> MaterialConsumptionScreen(viewModel = materialConsumptionViewModel)
        "/erp/production/process-actual" -> ProcessActualScreen(viewModel = processActualViewModel)
        "/erp/production/actual-management" -> ProductionActualManagementScreen(viewModel = productionActualManagementViewModel)
        "/system/users" -> UserListScreen(viewModel = userListViewModel)
        "/system/organization" -> OrganizationListScreen(viewModel = organizationListViewModel)
        "/system/roles" -> RolePermissionScreen(viewModel = rolePermissionViewModel)
        else -> PlaceholderScreen(path = path)
    }
}
