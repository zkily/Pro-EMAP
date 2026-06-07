package com.example.smart_emap.ui.erp.purchase.part

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smart_emap.ui.erp.purchase.PurchaseHeroHeader
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleCard
import com.example.smart_emap.ui.erp.purchase.PurchaseModuleItem
import com.example.smart_emap.ui.erp.purchase.PurchasePageBackground
import com.example.smart_emap.ui.erp.purchase.PurchaseSectionTitle

@Composable
fun PartHomeScreen(onNavigate: (String) -> Unit) {
    PurchasePageBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PurchaseHeroHeader(
                title = "部品管理",
                subtitle = "Parts Management",
                icon = Icons.Default.Inventory2,
            )
            PurchaseSectionTitle("部品マスタ管理")
            PurchaseModuleCard(
                PurchaseModuleItem(
                    "/master/part",
                    "部品マスタ",
                    "部品マスタの登録・編集（単価・仕入先等）",
                    Icons.Default.Description,
                    Color(0xFF909399),
                    Color(0xFFB1B3B8),
                ),
            ) { onNavigate("/master/part") }

            PurchaseSectionTitle("発注管理")
            PurchaseModuleCard(
                PurchaseModuleItem(
                    "/erp/purchase/part/order",
                    "部品在庫管理",
                    "部品の在庫状況と将来使用量を参考に発注数量を決定",
                    Icons.Default.ShoppingCart,
                    Color(0xFF667EEA),
                    Color(0xFF764BA2),
                ),
            ) { onNavigate("/erp/purchase/part/order") }
        }
    }
}
