package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
fun InventoryHomeScreen(
    viewModel: InventoryHomeViewModel,
    onNavigate: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        InventoryDashboardBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scroll)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InventoryGlassHeader(
                    title = "在庫管理",
                    subtitle = "${uiState.todayStr} · 製品 / 材料 / 部品 当日サマリ",
                    icon = Icons.Default.Inventory2,
                    loading = uiState.isLoading,
                    onRefresh = viewModel::refreshAll,
                    dark = true,
                )

                InventoryQuickNavBar(
                    routes = INVENTORY_HOME_QUICK_ROUTES,
                    onNavigate = onNavigate,
                    dark = true,
                )

                InventoryCategoryHeader(
                    title = "製品在庫",
                    subtitle = "工程別 · 当日集計",
                    accentColor = Color(0xFF409EFF),
                    dark = true,
                )
                InventoryStatCardsGrid(
                    fields = PRODUCT_INVENTORY_FIELDS,
                    values = uiState.productStats,
                    dark = true,
                )

                InventoryCategoryHeader(
                    title = "材料在庫",
                    subtitle = "当日分集計",
                    accentColor = Color(0xFF14B8A6),
                    dark = true,
                )
                InventoryStatCardsGrid(
                    fields = MATERIAL_STAT_FIELDS,
                    values = uiState.materialStats,
                    dark = true,
                )

                InventoryCategoryHeader(
                    title = "部品在庫",
                    subtitle = "当日分集計",
                    accentColor = Color(0xFFF97316),
                    dark = true,
                )
                InventoryStatCardsGrid(
                    fields = PART_STAT_FIELDS,
                    values = uiState.partStats,
                    dark = true,
                )

                InventoryTablePanel(
                    title = "在庫アラート",
                    countLabel = "${uiState.alertsTotal} 件",
                    loading = uiState.isLoading,
                    dark = true,
                    header = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 6.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            InventoryTableHeaderCell("品番", 100, dark = true)
                            InventoryTableHeaderCell("品名", 140, dark = true)
                            InventoryTableHeaderCell("倉庫", 100, dark = true)
                            InventoryTableHeaderCell("アラート種別", 100, dark = true)
                            InventoryTableHeaderCell("現在数量", 80, dark = true)
                            InventoryTableHeaderCell("しきい値", 80, dark = true)
                        }
                    },
                ) {
                    if (uiState.alerts.isEmpty()) {
                        Text(
                            "アラートはありません",
                            modifier = Modifier.padding(12.dp),
                            color = Color.White.copy(alpha = 0.6f),
                        )
                    } else {
                        val hScroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(hScroll),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            uiState.alerts.forEach { alert ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    InventoryTableCell(alert.productCode.orEmpty(), 100, dark = true)
                                    InventoryTableCell(alert.productName.orEmpty(), 140, dark = true)
                                    InventoryTableCell(alert.warehouseName.orEmpty(), 100, dark = true)
                                    InventoryTableCell(alert.alertTypeName.orEmpty(), 100, dark = true)
                                    InventoryTableCell(formatInventoryNum(alert.currentQuantity), 80, dark = true)
                                    InventoryTableCell(formatInventoryNum(alert.thresholdQuantity), 80, dark = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
