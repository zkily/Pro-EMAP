package com.example.smart_emap.ui.erp.shipping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun ShippingHomeScreen(
    viewModel: ShippingHomeViewModel,
    onNavigate: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scroll = rememberScrollState()
    val accentColors = listOf(
        Color(0xFF667EEA), Color(0xFFE6A23C), Color(0xFF67C23A), Color(0xFF409EFF), Color(0xFFF56C6C),
    )

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearSnackbar() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LayoutColors.ShellBg,
        contentWindowInsets = PurchaseShellWindowInsets,
    ) { padding ->
        ShippingDashboardBackground {
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(scroll).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ShippingFadeIn(0) {
                    ShippingGlassHeader(
                        title = "出荷管理",
                        subtitle = "${uiState.todayStr} · Shipping Management",
                        icon = Icons.Default.LocalShipping,
                        loading = uiState.isLoading,
                        onRefresh = viewModel::refreshAll,
                        dark = true,
                    )
                }
                ShippingFadeIn(1) {
                    ShippingStatCardsRow(
                        cards = SHIPPING_HOME_STAT_CARDS.map { it.label to (uiState.statValues[it.key] ?: "0") },
                        accentColors = accentColors,
                        onCardClick = { index -> onNavigate(SHIPPING_HOME_STAT_CARDS[index].path) },
                        dark = true,
                    )
                }
                ShippingFadeIn(2) {
                    ShippingSectionTitle("機能メニュー", "${SHIPPING_HOME_MODULES.size} 機能")
                }
                ShippingModuleGrid(SHIPPING_HOME_MODULES, onNavigate, startIndex = 3)
            }
        }
    }
}
