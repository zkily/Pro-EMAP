package com.example.smart_emap.ui.erp.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseShellWindowInsets
import com.example.smart_emap.ui.shell.LayoutColors

@Composable
fun StocktakeHomeScreen(onNavigate: (String) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }

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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                InventoryGlassHeader(
                    title = "棚卸管理",
                    subtitle = "棚卸リスト・登録・分析・金額・繰越",
                    icon = Icons.Default.Settings,
                    dark = true,
                )
                InventoryModuleGrid(
                    modules = STOCKTAKE_MODULES,
                    onNavigate = onNavigate,
                    dark = true,
                )
            }
        }
    }
}
