package com.example.smart_emap.ui.erp.purchase.outsourcing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun OutsourcingHomeScreen(
    viewModel: OutsourcingHomeViewModel,
    onNavigate: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    val quickRoutes = listOf(
        OutsourcingQuickRoute("/erp/purchase/outsourcing/plating-order", "メッキ注文", Icons.Default.Science, Color(0xFFF5AF19), Color(0xFFF12711)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/welding-order", "溶接注文", Icons.Default.PrecisionManufacturing, Color(0xFFFF416C), Color(0xFFFF4B2B)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/plating-receiving", "メッキ受入", Icons.Default.Download, Color(0xFF11998E), Color(0xFF38EF7D)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/welding-receiving", "溶接受入", Icons.Default.CheckCircle, Color(0xFF11998E), Color(0xFF38EF7D)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/material-issue", "材料支給", Icons.Default.Upload, Color(0xFF667EEA), Color(0xFF764BA2)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/stock", "在庫管理", Icons.Default.Inventory2, Color(0xFF4FACFE), Color(0xFF00F2FE)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/suppliers", "外注先マスタ", Icons.Default.Business, Color(0xFF5C6BC0), Color(0xFF3949AB)),
        OutsourcingQuickRoute("/erp/purchase/outsourcing/process-products", "外注工程製品", Icons.Default.GridView, Color(0xFF667EEA), Color(0xFF764BA2)),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        OutsourcingDashboardBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutsourcingDashboardGlassHeader(
                    title = "外注ダッシュボード",
                    subtitle = "外注管理の全体状況",
                    icon = Icons.Default.Dashboard,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshAll,
                )

                OutsourcingDashboardStatsGrid(
                    stats = uiState.stats,
                    dashboard = uiState.dashboard,
                )

                OutsourcingDashboardMainContent(
                    isLoading = uiState.isLoading,
                    upcomingDeliveries = uiState.upcomingDeliveries,
                    supplierSummary = uiState.supplierSummary,
                )

                OutsourcingQuickAccessSection(routes = quickRoutes, onNavigate = onNavigate)
            }
        }
    }
}
