package com.example.smart_emap.ui.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storage

/** 与 Smart-EMAPs frontend SidebarMenu / menuConfig 顶级结构对齐 */
object AppMenuConfig {
    val rootMenus: List<AppMenuNode> = listOf(
        AppMenuNode.Leaf(
            code = "DASHBOARD",
            label = "ダッシュボード",
            icon = Icons.Default.Home,
            path = "/dashboard",
            isHome = true,
        ),
        AppMenuNode.Group(
            code = "ERP",
            label = "ERP管理メニュー",
            icon = Icons.Default.Business,
            children = listOf(
                AppMenuNode.Leaf("ERP_SALES", "販売管理(営業)", Icons.Default.Business, "/erp/sales", isHome = true),
                AppMenuNode.Leaf("ERP_ORDER", "受注管理", Icons.Default.Storage, "/erp/order", isHome = true),
                AppMenuNode.Leaf("ERP_PURCHASE", "購買・外注管理", Icons.Default.ShoppingCart, "/erp/purchase", isHome = true),
                AppMenuNode.Leaf("ERP_INVENTORY", "在庫管理", Icons.Default.Inventory2, "/erp/inventory", isHome = true),
                AppMenuNode.Leaf("ERP_PRODUCTION", "生産管理", Icons.Default.Settings, "/erp/production", isHome = true),
                AppMenuNode.Leaf("ERP_SHIPPING", "出荷管理", Icons.Default.Storage, "/erp/shipping", isHome = true),
            ),
        ),
        AppMenuNode.Group(
            code = "APS",
            label = "APS管理メニュー",
            icon = Icons.Default.Analytics,
            children = listOf(
                AppMenuNode.Leaf("APS_HOME", "APSホーム", Icons.Default.Home, "/aps", isHome = true),
                AppMenuNode.Leaf("APS_PLANNING", "成型計画一覧", Icons.Default.Analytics, "/aps/planning-list"),
                AppMenuNode.Leaf("APS_WELDING", "溶接計画一覧", Icons.Default.Analytics, "/aps/welding-planning-list"),
            ),
        ),
        AppMenuNode.Group(
            code = "MES",
            label = "MES管理メニュー",
            icon = Icons.Default.Monitor,
            children = listOf(
                AppMenuNode.Leaf("MES_HOME", "MESホーム", Icons.Default.Home, "/mes", isHome = true),
                AppMenuNode.Leaf("MES_CUTTING", "切断・面取指示", Icons.Default.Settings, "/mes/productionInstruction/cutting"),
                AppMenuNode.Leaf("MES_FORMING", "成型指示", Icons.Default.Inventory2, "/mes/productionInstruction/forming"),
                AppMenuNode.Leaf("MES_WELDING", "溶接指示", Icons.Default.Monitor, "/mes/productionInstruction/welding"),
            ),
        ),
        AppMenuNode.Group(
            code = "MASTER",
            label = "マスタ管理",
            icon = Icons.Default.Storage,
            children = listOf(
                AppMenuNode.Leaf("MASTER_HOME", "マスタホーム", Icons.Default.Home, "/master", isHome = true),
            ),
        ),
        AppMenuNode.Group(
            code = "SYSTEM",
            label = "システム管理",
            icon = Icons.Default.Settings,
            children = listOf(
                AppMenuNode.Leaf("SYSTEM_HOME", "システムホーム", Icons.Default.Home, "/system", isHome = true),
            ),
        ),
    )

    fun titleForPath(path: String): String {
        fun walk(nodes: List<AppMenuNode>): String? {
            for (node in nodes) {
                when (node) {
                    is AppMenuNode.Leaf -> if (node.path == path) return node.label
                    is AppMenuNode.Group -> walk(node.children)?.let { return it }
                }
            }
            return null
        }
        return walk(rootMenus) ?: path
    }
}
