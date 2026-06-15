package com.example.smart_emap.ui.shell

import com.example.smart_emap.core.auth.canAccessMenuCode
import com.example.smart_emap.data.model.UserDto
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ? Smart-EMAPs frontend `src/router/menuConfig.ts` ?????
 *
 * ???
 * - Android ???????? path + ????????????? Tab?
 * - ???????????????????
 */
object AppMenuConfig {

    val rootMenus: List<AppMenuNode> = listOf(
        // Dashboard
        AppMenuNode.Leaf(
            code = "DASHBOARD",
            label = "???????",
            icon = Icons.Default.Home,
            path = "/dashboard",
            isHome = true,
        ),

        // ===== ERP =====
        AppMenuNode.Group(
            code = "ERP",
            label = "ERP??????",
            icon = Icons.Default.Business,
            children = listOf(
                AppMenuNode.Group(
                    code = "ERP_SALES",
                    label = "????(??)",
                    icon = Icons.Default.Business,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_SALES_HOME", "?????", Icons.Default.Home, "/erp/sales", isHome = true),
                        AppMenuNode.Leaf("ERP_SALES_QUOTATION", "????", Icons.Default.Description, "/erp/sales/quotation"),
                        AppMenuNode.Leaf("ERP_SALES_ORDERS", "????", Icons.AutoMirrored.Filled.List, "/erp/sales/orders"),
                        AppMenuNode.Leaf("ERP_SALES_FORECAST", "??????????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/sales/forecast"),
                        AppMenuNode.Leaf("ERP_SALES_CREDIT", "????", Icons.Default.Settings, "/erp/sales/credit"),
                        AppMenuNode.Leaf("ERP_SALES_CONTRACT", "??????", Icons.Default.Description, "/erp/sales/contract-pricing"),
                        AppMenuNode.Leaf("ERP_SALES_SHIPPING", "????", Icons.Default.LocalShipping, "/erp/sales/shipping"),
                        AppMenuNode.Leaf("ERP_SALES_RECORDING", "????", Icons.Default.Description, "/erp/sales/recording"),
                        AppMenuNode.Leaf("ERP_SALES_INVOICE", "?????", Icons.Default.Description, "/erp/sales/invoice"),
                        AppMenuNode.Leaf("ERP_SALES_CORRECTION", "??????", Icons.Default.Settings, "/erp/sales/return-correction"),
                        AppMenuNode.Leaf("ERP_SALES_RETURNS", "????(RMA)", Icons.Default.GridView, "/erp/sales/returns"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_ORDER",
                    label = "????",
                    icon = Icons.Default.Storage,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_ORDER_HOME", "?????", Icons.Default.Home, "/erp/order", isHome = true),
                        AppMenuNode.Leaf("ERP_ORDER_MONTHLY", "?????", Icons.AutoMirrored.Filled.List, "/erp/order/monthly"),
                        AppMenuNode.Leaf("ERP_ORDER_DAILY", "?????", Icons.AutoMirrored.Filled.List, "/erp/order/daily"),
                        AppMenuNode.Leaf("ERP_ORDER_DEST_HIST", "????????", Icons.Default.Description, "/erp/order/destination-history"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_PURCHASE",
                    label = "???????",
                    icon = Icons.Default.ShoppingCart,
                    children = listOf(
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_MATERIAL",
                            label = "????",
                            icon = Icons.Default.Inventory2,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_HOME", "???????", Icons.Default.Home, "/erp/purchase/material", isHome = true),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_ORDER", "??????", Icons.Default.ShoppingCart, "/erp/purchase/material/order"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_RECEIVING", "????", Icons.AutoMirrored.Filled.List, "/erp/purchase/material/receiving-history"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_INSPECTION", "????", Icons.Default.CheckCircle, "/erp/purchase/material/receiving-inspection"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_FORECAST", "????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/purchase/material/forecast"),
                            ),
                        ),
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_PART",
                            label = "????",
                            icon = Icons.Default.GridView,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PURCHASE_PART_HOME", "???????", Icons.Default.Home, "/erp/purchase/part", isHome = true),
                                AppMenuNode.Leaf("ERP_PURCHASE_PART_ORDER", "??????", Icons.Default.ShoppingCart, "/erp/purchase/part/order"),
                            ),
                        ),
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_OUTSOURCING",
                            label = "????",
                            icon = Icons.Default.ShoppingCart,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_OUTSOURCING_HOME", "?????", Icons.Default.Home, "/erp/purchase/outsourcing", isHome = true),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PLATING_ORDER", "???????", Icons.Default.Description, "/erp/purchase/outsourcing/plating-order"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PLATING_RECEIVING", "???????", Icons.Default.Description, "/erp/purchase/outsourcing/plating-receiving"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_WELDING_ORDER", "??????", Icons.Default.Description, "/erp/purchase/outsourcing/welding-order"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_WELDING_RECEIVING", "??????", Icons.Default.Description, "/erp/purchase/outsourcing/welding-receiving"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_SUPPLIERS", "??????", Icons.Default.Settings, "/erp/purchase/outsourcing/suppliers"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PROCESS_PRODUCTS", "??????", Icons.Default.GridView, "/erp/purchase/outsourcing/process-products"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_STOCK", "????", Icons.Default.Inventory2, "/erp/purchase/outsourcing/stock"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_SUPPLIED_STOCK", "??????", Icons.Default.Inventory2, "/erp/purchase/outsourcing/supplied-material-stock"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_USAGE", "?????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/purchase/outsourcing/usage"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_MATERIAL_ISSUE", "??????", Icons.Default.LocalShipping, "/erp/purchase/outsourcing/material-issue"),
                            ),
                        ),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_INVENTORY",
                    label = "????",
                    icon = Icons.Default.Inventory2,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_INVENTORY_HOME", "?????", Icons.Default.Home, "/erp/inventory", isHome = true),
                        AppMenuNode.Leaf("ERP_INVENTORY_LIST", "??????", Icons.AutoMirrored.Filled.List, "/erp/inventory/list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_MATERIAL_LIST", "??????", Icons.AutoMirrored.Filled.List, "/erp/inventory/material-list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_PART_LIST", "??????", Icons.AutoMirrored.Filled.List, "/erp/inventory/part-list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCK_ENTRY", "??????", Icons.Default.Description, "/erp/inventory/stock-entry"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCK_TX_LOG", "??????", Icons.Default.Description, "/erp/inventory/stock-transaction-logs"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCKTAKE", "????", Icons.Default.Settings, "/erp/inventory/stocktake"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_PRODUCTION",
                    label = "????",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_PRODUCTION_HOME", "?????", Icons.Default.Home, "/erp/production", isHome = true),
                        AppMenuNode.Group(
                            code = "ERP_PRODUCTION_PLANNING",
                            label = "????",
                            icon = Icons.Default.CalendarMonth,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PRODUCTION_DATA", "???????", Icons.AutoMirrored.Filled.List, "/erp/production/data-management"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_BASELINE", "????????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/production/plan-baseline"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_PLAN_SCHEDULES", "????????", Icons.AutoMirrored.Filled.List, "/erp/production/plan-schedules"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_PROCESS_MACHINE_PLAN", "????????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/production/process-machine-plan"),
                            ),
                        ),
                        AppMenuNode.Leaf("ERP_PRODUCTION_REQUIREMENTS", "?????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/production-requirements/material"),
                        AppMenuNode.Leaf("ERP_PRODUCTION_METRICS", "????", Icons.AutoMirrored.Filled.TrendingUp, "/erp/production/metrics/scrap-rate"),
                        AppMenuNode.Leaf("ERP_PRODUCTION_RESULT", "????", Icons.Default.Description, "/erp/production/actual-management"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_SHIPPING",
                    label = "????",
                    icon = Icons.Default.LocalShipping,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_SHIPPING_HOME", "?????", Icons.Default.Home, "/erp/shipping", isHome = true),
                        AppMenuNode.Leaf("ERP_SHIPPING_LIST", "???????", Icons.AutoMirrored.Filled.List, "/erp/shipping/list"),
                        AppMenuNode.Leaf("ERP_SHIPPING_REPORT", "???????", Icons.Default.Description, "/erp/shipping/report"),
                        AppMenuNode.Leaf("ERP_SHIPPING_OVERVIEW", "???????", Icons.Default.CalendarMonth, "/erp/shipping/overview"),
                        AppMenuNode.Leaf("ERP_SHIPPING_CONFIRM", "???????", Icons.AutoMirrored.Filled.List, "/erp/shipping/confirm"),
                        AppMenuNode.Leaf("ERP_SHIPPING_WELDING", "??????", Icons.Default.Description, "/erp/shipping/welding"),
                        AppMenuNode.Leaf("ERP_SHIPPING_PICKING", "???????", Icons.Default.GridView, "/erp/shipping/picking"),
                        AppMenuNode.Leaf("ERP_SHIPPING_INVENTORY_SHORTAGE", "??????", Icons.Default.Inventory2, "/erp/shipping/inventory-shortage"),
                        AppMenuNode.Leaf("ERP_SHIPPING_ABC", "ABC??", Icons.Default.Analytics, "/erp/shipping/abc-analysis"),
                        AppMenuNode.Leaf("ERP_SHIPPING_INVENTORY_KPI", "??KPI?????", Icons.Default.Analytics, "/erp/shipping/inventory-kpi"),
                        AppMenuNode.Leaf("ERP_SHIPPING_WAREHOUSE_DAILY", "??????", Icons.AutoMirrored.Filled.List, "/erp/shipping/warehouse-daily"),
                    ),
                ),
            ),
        ),

        // ===== APS =====
        AppMenuNode.Group(
            code = "APS",
            label = "APS??????",
            icon = Icons.Default.Analytics,
            children = listOf(
                AppMenuNode.Group(
                    code = "APS_PRODUCTION_PLAN_CREATE",
                    label = "??????",
                    icon = Icons.Default.CalendarMonth,
                    children = listOf(
                        AppMenuNode.Leaf("APS_CUTTING_PLANNING", "??????", Icons.Default.Settings, "/aps/cutting-planning"),
                        AppMenuNode.Leaf("APS_PLANNING", "??????", Icons.Default.Description, "/aps/planning"),
                        AppMenuNode.Leaf("APS_WELDING_PLANNING", "??????", Icons.Default.Description, "/aps/welding-planning"),
                        AppMenuNode.Leaf("APS_PLATING_PLANNING", "???????", Icons.Default.Description, "/aps/plating-planning"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "APS_PRODUCTION_PLAN_VIEW",
                    label = "??????",
                    icon = Icons.AutoMirrored.Filled.List,
                    children = listOf(
                        AppMenuNode.Leaf("APS_FORMING_PLAN_LIST", "??????", Icons.AutoMirrored.Filled.List, "/aps/planning-list"),
                        AppMenuNode.Leaf("APS_WELDING_PLAN_LIST", "??????", Icons.AutoMirrored.Filled.List, "/aps/welding-planning-list"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "APS_EQUIPMENT_UTILIZATION_MANAGEMENT",
                    label = "??????",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("APS_CAPACITY", "??????", Icons.Default.Settings, "/aps/capacity"),
                        AppMenuNode.Leaf("APS_CAPACITY_MATRIX", "???????", Icons.Default.GridView, "/aps/capacity-matrix"),
                        AppMenuNode.Leaf("APS_DAILY_REPORT", "???????", Icons.AutoMirrored.Filled.List, "/aps/daily-report"),
                    ),
                ),
                AppMenuNode.Leaf(
                    code = "APS_SCHEDULING",
                    label = "????????",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    path = "/aps/scheduling",
                ),
            ),
        ),

        // ===== MES =====
        AppMenuNode.Group(
            code = "MES",
            label = "MES??????",
            icon = Icons.Default.Monitor,
            children = listOf(
                AppMenuNode.Group(
                    code = "MES_PRODUCTION_INSTRUCTION",
                    label = "????",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_CUTTING", "???????", Icons.Default.Settings, "/mes/productionInstruction/cutting"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_FORMING", "????", Icons.Default.Inventory2, "/mes/productionInstruction/forming"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_WELDING", "????", Icons.Default.Monitor, "/mes/productionInstruction/welding"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_PLATING", "?????", Icons.Default.Description, "/mes/productionInstruction/plating"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MES_ACTUAL_DATA_COLLECTION",
                    label = "????",
                    icon = Icons.Default.Monitor,
                    children = listOf(
                        AppMenuNode.Leaf("MES_PRODUCTION_MONITOR", "??????", Icons.Default.Monitor, "/mes/actualDataCollection/monitor"),
                        AppMenuNode.Leaf("MES_ACTUAL_CUTTING", "??????", Icons.Default.Settings, "/mes/actualDataCollection/cutting"),
                        AppMenuNode.Leaf("MES_ACTUAL_CHAMFERING", "??????", Icons.Default.Settings, "/mes/actualDataCollection/chamfering"),
                        AppMenuNode.Leaf("MES_ACTUAL_FORMING", "??????", Icons.Default.Inventory2, "/mes/actualDataCollection/forming"),
                        AppMenuNode.Leaf("MES_ACTUAL_PLATING", "???????", Icons.Default.Description, "/mes/actualDataCollection/plating"),
                        AppMenuNode.Leaf("MES_ACTUAL_WELDING", "??????", Icons.Default.Monitor, "/mes/actualDataCollection/welding"),
                        AppMenuNode.Leaf("MES_ACTUAL_INSPECTION", "??????", Icons.Default.GridView, "/mes/actualDataCollection/inspection"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MES_ACTUAL_COLLECTION_REGISTRATION",
                    label = "????",
                    icon = Icons.Default.Edit,
                    children = listOf(
                        AppMenuNode.Leaf("MES_ACTUAL_REG_CUTTING", "??????", Icons.Default.Settings, "/mes/actualCollectionRegistration/cutting"),
                        AppMenuNode.Leaf("MES_ACTUAL_REG_CHAMFERING", "??????", Icons.Default.Build, "/mes/actualCollectionRegistration/chamfering"),
                        AppMenuNode.Leaf("MES_ACTUAL_REG_FORMING", "??????", Icons.Default.Inventory2, "/mes/actualCollectionRegistration/forming"),
                        AppMenuNode.Leaf("MES_ACTUAL_REG_PLATING", "???????", Icons.Default.Description, "/mes/actualCollectionRegistration/plating"),
                        AppMenuNode.Leaf("MES_ACTUAL_REG_WELDING", "??????", Icons.Default.Monitor, "/mes/actualCollectionRegistration/welding"),
                        AppMenuNode.Leaf("MES_ACTUAL_REG_INSPECTION", "??????", Icons.Default.CheckCircle, "/mes/actualCollectionRegistration/inspection"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MES_ACTUAL_ANALYSIS",
                    label = "??????",
                    icon = Icons.Default.Analytics,
                    children = listOf(
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_PRODUCTIVITY",
                            label = "?????",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            analysisCategory = "productivity",
                            categoryLabel = "???",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_UTILIZATION",
                            label = "?????",
                            icon = Icons.Default.Speed,
                            analysisCategory = "utilization",
                            categoryLabel = "???",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_PROGRESS",
                            label = "????",
                            icon = Icons.Default.Timeline,
                            analysisCategory = "progress",
                            categoryLabel = "??",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_QUALITY",
                            label = "????",
                            icon = Icons.Default.CheckCircle,
                            analysisCategory = "quality",
                            categoryLabel = "??",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_COST",
                            label = "?????",
                            icon = Icons.Default.BarChart,
                            analysisCategory = "cost",
                            categoryLabel = "???",
                        ),
                    ),
                ),
            ),
        ),

        // ===== MASTER =====
        AppMenuNode.Group(
            code = "MASTER",
            label = "?????",
            icon = Icons.Default.Storage,
            children = listOf(
                AppMenuNode.Group(
                    code = "MASTER_LIST",
                    label = "???",
                    icon = Icons.AutoMirrored.Filled.List,
                    children = listOf(
                        AppMenuNode.Leaf("MASTER_HOME", "??????", Icons.Default.Home, "/master", isHome = true),
                        AppMenuNode.Leaf("MASTER_PRODUCT", "?????", Icons.Default.Description, "/master/product"),
                        AppMenuNode.Leaf("MASTER_MATERIAL", "?????", Icons.Default.Description, "/master/material"),
                        AppMenuNode.Leaf("MASTER_MATERIAL_INSPECTION", "???????", Icons.Default.Description, "/master/material-inspection"),
                        AppMenuNode.Leaf("MASTER_PART", "?????", Icons.Default.Description, "/master/part"),
                        AppMenuNode.Leaf("MASTER_SUPPLIER", "??????", Icons.Default.Description, "/master/supplier"),
                        AppMenuNode.Leaf("MASTER_PROCESS", "?????", Icons.Default.Description, "/master/process"),
                        AppMenuNode.Leaf("MASTER_PROCESS_ROUTE", "????????", Icons.Default.Description, "/master/process-route"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_PROCESS_ROUTE", "????????", Icons.Default.Description, "/master/product-process-route"),
                        AppMenuNode.Leaf("MASTER_CUSTOMER", "?????", Icons.Default.Description, "/master/customer"),
                        AppMenuNode.Leaf("MASTER_CARRIER", "??????", Icons.Default.Description, "/master/carrier"),
                        AppMenuNode.Leaf("MASTER_MACHINE", "?????", Icons.Default.Description, "/master/machine"),
                        AppMenuNode.Leaf("MASTER_ROLLER_MASTER", "???????", Icons.Default.Description, "/master/roller-master"),
                        AppMenuNode.Leaf("MASTER_DESTINATION", "??????", Icons.Default.Description, "/master/destination"),
                        AppMenuNode.Leaf("MASTER_DESTINATION_HOLIDAY", "???????", Icons.Default.Description, "/master/destination/holiday"),
                        AppMenuNode.Leaf("MASTER_COMPANY_WORK_CALENDAR", "?????????", Icons.Default.CalendarMonth, "/master/company-work-calendar"),
                        AppMenuNode.Leaf("MASTER_PROCESS_PROCESSING_FEE", "????????", Icons.AutoMirrored.Filled.TrendingUp, "/master/bom/process-processing-fee"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MASTER_BOM",
                    label = "BOM",
                    icon = Icons.Default.GridView,
                    children = listOf(
                        AppMenuNode.Leaf("MASTER_BOM_HOME", "BOM???", Icons.Default.Home, "/master/bom", isHome = true),
                        AppMenuNode.Leaf("MASTER_PRODUCT_PROCESS_BOM", "????BOM", Icons.Default.GridView, "/master/bom/product-process"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_MACHINE_CONFIG", "??????", Icons.Default.GridView, "/master/bom/product-machine-config"),
                        AppMenuNode.Leaf("MASTER_EQUIPMENT_EFFICIENCY", "??????", Icons.Default.Analytics, "/master/bom/equipment-efficiency"),
                        AppMenuNode.Leaf("MASTER_ROLLER_BOM", "????BOM??", Icons.Default.GridView, "/master/bom/roller-bom"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_BOM_DETAIL", "??BOM???", Icons.Default.GridView, "/master/bom/product-bom"),
                        AppMenuNode.Leaf("MASTER_PROCESS_DEFECT_ITEMS", "??????????", Icons.Default.Description, "/master/bom/process-defect-items"),
                        AppMenuNode.Leaf("MASTER_UNIT_PRICE", "???????", Icons.Default.Description, "/master/bom/product-unit-price"),
                    ),
                ),
            ),
        ),

        // ===== SYSTEM =====
        AppMenuNode.Group(
            code = "SYSTEM",
            label = "??????",
            icon = Icons.Default.Settings,
            children = listOf(
                AppMenuNode.Group(
                    code = "SYSTEM_USER",
                    label = "???????",
                    icon = Icons.Default.Business,
                    children = listOf(
                        AppMenuNode.Leaf("SYSTEM_HOME", "???????", Icons.Default.Home, "/system", isHome = true),
                        AppMenuNode.Leaf("SYSTEM_USERS", "??????", Icons.Default.Person, "/system/users"),
                        AppMenuNode.Leaf("SYSTEM_ORG", "???????", Icons.Default.Business, "/system/organization"),
                        AppMenuNode.Leaf("SYSTEM_ROLE", "????????", Icons.Default.Settings, "/system/roles"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "SYSTEM_SETTINGS",
                    label = "??????",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("SYSTEM_NUMBERING", "???????", Icons.Default.Description, "/system/numbering"),
                        AppMenuNode.Leaf("SYSTEM_WORKFLOW", "????????", Icons.Default.Description, "/system/workflow"),
                        AppMenuNode.Leaf("SYSTEM_NOTIFICATION", "??????", Icons.Default.Description, "/system/notification"),
                        AppMenuNode.Leaf("SYSTEM_LOGS", "??????", Icons.Default.Description, "/system/logs"),
                        AppMenuNode.Leaf("SYSTEM_DATA", "?????", Icons.Default.Description, "/system/data"),
                        AppMenuNode.Leaf("SYSTEM_MENUS", "??????", Icons.Default.Description, "/system/menus"),
                        AppMenuNode.Leaf("SYSTEM_FILE_WATCHER", "????????", Icons.Default.Description, "/system/file-watcher"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "SYSTEM_DATABASE",
                    label = "??????",
                    icon = Icons.Default.Storage,
                    children = listOf(
                        AppMenuNode.Leaf("SYSTEM_DB_ORDER_DAILY", "order_daily", Icons.Default.Description, "/system/database/order/daily"),
                    ),
                ),
            ),
        ),
    )

    fun titleForPath(path: String): String {
        return findLeaf(path)?.label ?: path
    }

    fun findLeaf(path: String): AppMenuNode.Leaf? {
        fun walk(nodes: List<AppMenuNode>): AppMenuNode.Leaf? {
            for (node in nodes) {
                when (node) {
                    is AppMenuNode.Leaf -> if (node.path == path) return node
                    is AppMenuNode.Group -> walk(node.children)?.let { return it }
                }
            }
            return null
        }
        return walk(rootMenus)
    }

    fun isKnownPath(path: String): Boolean = findLeaf(path) != null

    /** ?? path ???? menu code??? leaf ??? path ?????? */
    fun codesForPath(path: String): List<String> {
        val codes = mutableListOf<String>()
        fun walk(nodes: List<AppMenuNode>) {
            for (node in nodes) {
                when (node) {
                    is AppMenuNode.Leaf -> if (node.path == path) codes.add(node.code)
                    is AppMenuNode.Group -> walk(node.children)
                }
            }
        }
        walk(rootMenus)
        return codes
    }

    /** ????? menu_codes ???????????????? */
    fun menusForUser(user: UserDto): List<AppMenuNode> =
        rootMenus.mapNotNull { filterNodeForUser(it, user) }

    private fun filterNodeForUser(node: AppMenuNode, user: UserDto): AppMenuNode? {
        return when (node) {
            is AppMenuNode.Group -> {
                val children = node.children.mapNotNull { filterNodeForUser(it, user) }
                if (children.isEmpty()) return null
                node.copy(children = children)
            }

            is AppMenuNode.Leaf -> if (user.canAccessMenuCode(node.code)) node else null
        }
    }
}

private data class MesActualAnalysisProcess(
    val codeSuffix: String,
    val processLabel: String,
    val pathSegment: String,
    val icon: ImageVector,
)

private val mesActualAnalysisProcesses = listOf(
    MesActualAnalysisProcess("CUTTING", "??", "cutting", Icons.Default.Settings),
    MesActualAnalysisProcess("CHAMFERING", "??", "chamfering", Icons.Default.Build),
    MesActualAnalysisProcess("FORMING", "??", "forming", Icons.Default.Inventory2),
    MesActualAnalysisProcess("PLATING", "???", "plating", Icons.Default.Description),
    MesActualAnalysisProcess("WELDING", "??", "welding", Icons.Default.Monitor),
    MesActualAnalysisProcess("INSPECTION", "??", "inspection", Icons.Default.CheckCircle),
)

/** Web `menuConfig.ts` ???????? + ????? leaf ?????? */
private fun mesActualAnalysisProcessLeaves(
    codePrefix: String,
    analysisCategory: String,
    categoryLabel: String,
): List<AppMenuNode.Leaf> = mesActualAnalysisProcesses.map { process ->
    AppMenuNode.Leaf(
        code = "${codePrefix}_${process.codeSuffix}",
        label = "${process.processLabel}$categoryLabel",
        icon = process.icon,
        path = "/mes/actualAnalysis/$analysisCategory/${process.pathSegment}",
    )
}

private fun mesActualAnalysisCategoryGroup(
    code: String,
    label: String,
    icon: ImageVector,
    analysisCategory: String,
    categoryLabel: String,
): AppMenuNode.Group = AppMenuNode.Group(
    code = code,
    label = label,
    icon = icon,
    children = mesActualAnalysisProcessLeaves(code, analysisCategory, categoryLabel),
)
