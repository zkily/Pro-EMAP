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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_emap.SmartEmapAppContainer
import com.example.smart_emap.data.model.UserDto
import com.example.smart_emap.ui.dashboard.DashboardScreen
import com.example.smart_emap.ui.dashboard.DashboardViewModel
import com.example.smart_emap.ui.mes.inspection.InspectionActualScreen
import com.example.smart_emap.ui.mes.inspection.InspectionActualViewModel

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

    var isSidebarCollapsed by remember { mutableStateOf(false) }
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
) {
    when (path) {
        "/dashboard" -> DashboardScreen(viewModel = dashboardViewModel)
        "/mes/actualDataCollection/inspection" -> InspectionActualScreen(viewModel = inspectionViewModel)
        else -> PlaceholderScreen(path = path)
    }
}
