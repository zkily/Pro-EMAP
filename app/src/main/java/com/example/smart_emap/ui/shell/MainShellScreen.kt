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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_emap.SmartEmapAppContainer
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.dashboard.DashboardScreen
import com.example.smart_emap.ui.dashboard.DashboardViewModel
import com.example.smart_emap.ui.erp.order.OrderDailyScreen
import com.example.smart_emap.ui.erp.order.OrderDailyViewModel
import com.example.smart_emap.ui.erp.order.OrderDestinationHistoryScreen
import com.example.smart_emap.ui.erp.order.OrderDestinationHistoryViewModel
import com.example.smart_emap.ui.erp.order.OrderMonthlyScreen
import com.example.smart_emap.ui.erp.order.OrderMonthlyViewModel
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
import com.example.smart_emap.ui.mes.welding.WeldingActualScreen
import com.example.smart_emap.ui.mes.welding.WeldingActualViewModel

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

    val cuttingInstructionViewModel: CuttingInstructionViewModel = viewModel(
        factory = CuttingInstructionViewModel.Factory(
            repository = appContainer.cuttingInstructionRepository,
        ),
    )

    var isSidebarCollapsed by remember { mutableStateOf(true) }
    var activePath by remember { mutableStateOf("/dashboard") }
    var tabs by remember {
        mutableStateOf(
            listOf(ShellTab(path = "/dashboard", title = "ダッシュボード", closable = false)),
        )
    }

    fun navigateTo(path: String) {
        val normalizedPath = path.trim().ifEmpty { "/dashboard" }
        val title = AppMenuConfig.titleForPath(normalizedPath)
        if (tabs.none { it.path == normalizedPath }) {
            tabs = tabs + ShellTab(path = normalizedPath, title = title)
        }
        activePath = normalizedPath
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LayoutColors.ShellBg),
    ) {
        val isMobile = maxWidth < 768.dp
        val sidebarVisible = !isMobile || !isSidebarCollapsed

        LaunchedEffect(isMobile) {
            if (isMobile) isSidebarCollapsed = true
        }

        if (isMobile && sidebarVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isSidebarCollapsed = true },
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            if (sidebarVisible) {
                SidebarMenu(
                    isCollapsed = !isMobile && isSidebarCollapsed,
                    activePath = activePath,
                    showCollapseControl = !isMobile,
                    onNavigate = { path ->
                        navigateTo(path)
                        if (isMobile) isSidebarCollapsed = true
                    },
                    onToggleCollapse = { isSidebarCollapsed = !isSidebarCollapsed },
                    modifier = Modifier
                        .then(
                            if (isMobile) {
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
                    isMobile = isMobile,
                    sidebarOpen = sidebarVisible,
                    onToggleSidebar = { isSidebarCollapsed = !isSidebarCollapsed },
                    onLogout = onLogout,
                )
                TabsNav(
                    tabs = tabs,
                    activePath = activePath,
                    onTabSelected = { activePath = it },
                    onTabClosed = { path ->
                        val closingIndex = tabs.indexOfFirst { it.path == path }
                        if (closingIndex < 0) return@TabsNav
                        val newTabs = tabs.filterNot { it.path == path }
                        tabs = newTabs
                        if (activePath == path) {
                            val fallback = newTabs.getOrNull(closingIndex - 1) ?: newTabs.first()
                            activePath = fallback.path
                        }
                    },
                    onRefresh = {
                        when (activePath) {
                            "/dashboard" -> dashboardViewModel.loadDashboard()
                            "/mes/actualDataCollection/inspection" -> inspectionViewModel.refreshAll()
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
                            else -> if (activePath.startsWith("/master")) {
                                when (activePath) {
                                    "/master/product" -> productMasterViewModel.refreshAll()
                                    else -> masterViewModel.refreshAll()
                                }
                            }
                        }
                    },
                    onCloseOthers = {
                        tabs = tabs.filter { !it.closable || it.path == activePath }
                    },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    key(activePath) {
                        ShellRouteContent(
                            path = activePath,
                            dashboardViewModel = dashboardViewModel,
                            inspectionViewModel = inspectionViewModel,
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
                            cuttingInstructionViewModel = cuttingInstructionViewModel,
                            onNavigate = ::navigateTo,
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
    dashboardViewModel: DashboardViewModel,
    inspectionViewModel: InspectionActualViewModel,
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
    cuttingInstructionViewModel: CuttingInstructionViewModel,
    onNavigate: (String) -> Unit,
) {
    when (path) {
        "/dashboard" -> DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigate = onNavigate,
        )
        "/master" -> MasterHomeScreen(onNavigate = onNavigate)
        "/master/product" -> ProductMasterScreen(viewModel = productMasterViewModel)
        "/master/material",
        "/master/material-inspection",
        "/master/part",
        "/master/supplier",
        "/master/process",
        "/master/process-route",
        "/master/product-process-route",
        "/master/bom/process-processing-fee",
        "/master/customer",
        "/master/carrier",
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
        "/mes/actualDataCollection/welding" -> WeldingActualScreen(viewModel = weldingViewModel)
        "/mes/actualDataCollection/cutting" -> CuttingActualScreen(viewModel = cuttingViewModel)
        "/mes/actualDataCollection/chamfering" -> ChamferingActualScreen(viewModel = chamferingViewModel)
        "/mes/productionInstruction/cutting" -> CuttingInstructionScreen(viewModel = cuttingInstructionViewModel)
        else -> PlaceholderScreen(path = path)
    }
}
