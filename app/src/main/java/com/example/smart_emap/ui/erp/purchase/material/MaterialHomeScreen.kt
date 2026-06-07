package com.example.smart_emap.ui.erp.purchase.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseHeroHeader
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleCard
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleItem
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseSectionTitle
import androidx.compose.foundation.layout.Column

@Composable
fun MaterialHomeScreen(onNavigate: (String) -> Unit) {
    PurchasePageBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PurchaseHeroHeader(
                title = "材料管理",
                subtitle = "Material Management",
                icon = Icons.Default.Inventory2,
            )
            PurchaseSectionTitle("受入管理")
            listOf(
                PurchaseModuleItem(
                    "/erp/purchase/material/receiving-history",
                    "受入履歴",
                    "受け入れ履歴の確認・検索",
                    Icons.Default.Description,
                    Color(0xFFE6A23C),
                    Color(0xFFF7BA2A),
                ),
                PurchaseModuleItem(
                    "/erp/purchase/material/receiving-inspection",
                    "受入検品",
                    "受け入れ検品の実施・管理",
                    Icons.Default.CheckCircle,
                    Color(0xFF67C23A),
                    Color(0xFF85CE61),
                ),
            ).forEach { PurchaseModuleCard(it) { onNavigate(it.path) } }

            PurchaseSectionTitle("発注管理")
            listOf(
                PurchaseModuleItem(
                    "/erp/purchase/material/forecast",
                    "内示管理",
                    "月別受注から材料必要数量を算出・管理",
                    Icons.Default.TrendingUp,
                    Color(0xFF43E97B),
                    Color(0xFF38F9D7),
                ),
                PurchaseModuleItem(
                    "/erp/purchase/material/order",
                    "材料在庫管理",
                    "材料の在庫状況と将来使用量を参考に受注数量を決定",
                    Icons.Default.ShoppingCart,
                    Color(0xFFA18CD1),
                    Color(0xFFFBC2EB),
                ),
            ).forEach { PurchaseModuleCard(it) { onNavigate(it.path) } }
        }
    }
}
