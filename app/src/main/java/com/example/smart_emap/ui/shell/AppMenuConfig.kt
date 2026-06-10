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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 与 Smart-EMAPs frontend `src/router/menuConfig.ts` 的层级对齐。
 *
 * 说明：
 * - Android 侧暂时只用「路由 path + 显示名称」来驱动左侧菜单和 Tab。
 * - 图标做了简化映射（不影响路由一致性）。
 */
object AppMenuConfig {

    val rootMenus: List<AppMenuNode> = listOf(
        // Dashboard
        AppMenuNode.Leaf(
            code = "DASHBOARD",
            label = "ダッシュボード",
            icon = Icons.Default.Home,
            path = "/dashboard",
            isHome = true,
        ),

        // ===== ERP =====
        AppMenuNode.Group(
            code = "ERP",
            label = "ERP管理メニュー",
            icon = Icons.Default.Business,
            children = listOf(
                AppMenuNode.Group(
                    code = "ERP_SALES",
                    label = "販売管理(営業)",
                    icon = Icons.Default.Business,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_SALES_HOME", "販売ホーム", Icons.Default.Home, "/erp/sales", isHome = true),
                        AppMenuNode.Leaf("ERP_SALES_QUOTATION", "見積管理", Icons.Default.Description, "/erp/sales/quotation"),
                        AppMenuNode.Leaf("ERP_SALES_ORDERS", "受注一覧", Icons.Default.List, "/erp/sales/orders"),
                        AppMenuNode.Leaf("ERP_SALES_FORECAST", "内示・フォーキャスト", Icons.Default.TrendingUp, "/erp/sales/forecast"),
                        AppMenuNode.Leaf("ERP_SALES_CREDIT", "与信管理", Icons.Default.Settings, "/erp/sales/credit"),
                        AppMenuNode.Leaf("ERP_SALES_CONTRACT", "契約単価管理", Icons.Default.Description, "/erp/sales/contract-pricing"),
                        AppMenuNode.Leaf("ERP_SALES_SHIPPING", "出荷指示", Icons.Default.LocalShipping, "/erp/sales/shipping"),
                        AppMenuNode.Leaf("ERP_SALES_RECORDING", "売上計上", Icons.Default.Description, "/erp/sales/recording"),
                        AppMenuNode.Leaf("ERP_SALES_INVOICE", "請求書発行", Icons.Default.Description, "/erp/sales/invoice"),
                        AppMenuNode.Leaf("ERP_SALES_CORRECTION", "赤黒訂正処理", Icons.Default.Settings, "/erp/sales/return-correction"),
                        AppMenuNode.Leaf("ERP_SALES_RETURNS", "返品管理(RMA)", Icons.Default.GridView, "/erp/sales/returns"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_ORDER",
                    label = "受注管理",
                    icon = Icons.Default.Storage,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_ORDER_HOME", "受注ホーム", Icons.Default.Home, "/erp/order", isHome = true),
                        AppMenuNode.Leaf("ERP_ORDER_MONTHLY", "月受注管理", Icons.Default.List, "/erp/order/monthly"),
                        AppMenuNode.Leaf("ERP_ORDER_DAILY", "日受注管理", Icons.Default.List, "/erp/order/daily"),
                        AppMenuNode.Leaf("ERP_ORDER_DEST_HIST", "納入先別受注履歴", Icons.Default.Description, "/erp/order/destination-history"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_PURCHASE",
                    label = "購買・外注管理",
                    icon = Icons.Default.ShoppingCart,
                    children = listOf(
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_MATERIAL",
                            label = "材料管理",
                            icon = Icons.Default.Inventory2,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_HOME", "材料管理ホーム", Icons.Default.Home, "/erp/purchase/material", isHome = true),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_ORDER", "材料在庫管理", Icons.Default.ShoppingCart, "/erp/purchase/material/order"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_RECEIVING", "受入履歴", Icons.Default.List, "/erp/purchase/material/receiving-history"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_INSPECTION", "受入検品", Icons.Default.CheckCircle, "/erp/purchase/material/receiving-inspection"),
                                AppMenuNode.Leaf("ERP_PURCHASE_MATERIAL_FORECAST", "内示管理", Icons.Default.TrendingUp, "/erp/purchase/material/forecast"),
                            ),
                        ),
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_PART",
                            label = "部品管理",
                            icon = Icons.Default.GridView,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PURCHASE_PART_HOME", "部品管理ホーム", Icons.Default.Home, "/erp/purchase/part", isHome = true),
                                AppMenuNode.Leaf("ERP_PURCHASE_PART_ORDER", "部品在庫管理", Icons.Default.ShoppingCart, "/erp/purchase/part/order"),
                            ),
                        ),
                        AppMenuNode.Group(
                            code = "ERP_PURCHASE_OUTSOURCING",
                            label = "外注管理",
                            icon = Icons.Default.ShoppingCart,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_OUTSOURCING_HOME", "外注ホーム", Icons.Default.Home, "/erp/purchase/outsourcing", isHome = true),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PLATING_ORDER", "外注メッキ注文", Icons.Default.Description, "/erp/purchase/outsourcing/plating-order"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PLATING_RECEIVING", "外注メッキ受入", Icons.Default.Description, "/erp/purchase/outsourcing/plating-receiving"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_WELDING_ORDER", "外注溶接注文", Icons.Default.Description, "/erp/purchase/outsourcing/welding-order"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_WELDING_RECEIVING", "外注溶接受入", Icons.Default.Description, "/erp/purchase/outsourcing/welding-receiving"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_SUPPLIERS", "外注先マスタ", Icons.Default.Settings, "/erp/purchase/outsourcing/suppliers"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_PROCESS_PRODUCTS", "外注工程製品", Icons.Default.GridView, "/erp/purchase/outsourcing/process-products"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_STOCK", "外注在庫", Icons.Default.Inventory2, "/erp/purchase/outsourcing/stock"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_SUPPLIED_STOCK", "支給材料在庫", Icons.Default.Inventory2, "/erp/purchase/outsourcing/supplied-material-stock"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_USAGE", "使用数管理", Icons.Default.TrendingUp, "/erp/purchase/outsourcing/usage"),
                                AppMenuNode.Leaf("ERP_OUTSOURCING_MATERIAL_ISSUE", "支給材料出庫", Icons.Default.LocalShipping, "/erp/purchase/outsourcing/material-issue"),
                            ),
                        ),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_INVENTORY",
                    label = "在庫管理",
                    icon = Icons.Default.Inventory2,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_INVENTORY_HOME", "在庫ホーム", Icons.Default.Home, "/erp/inventory", isHome = true),
                        AppMenuNode.Leaf("ERP_INVENTORY_LIST", "製品在庫照会", Icons.Default.List, "/erp/inventory/list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_MATERIAL_LIST", "材料在庫照会", Icons.Default.List, "/erp/inventory/material-list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_PART_LIST", "部品在庫照会", Icons.Default.List, "/erp/inventory/part-list"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCK_ENTRY", "在庫登録管理", Icons.Default.Description, "/erp/inventory/stock-entry"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCK_TX_LOG", "在庫取引記録", Icons.Default.Description, "/erp/inventory/stock-transaction-logs"),
                        AppMenuNode.Leaf("ERP_INVENTORY_STOCKTAKE", "棚卸管理", Icons.Default.Settings, "/erp/inventory/stocktake"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_PRODUCTION",
                    label = "生産管理",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_PRODUCTION_HOME", "生産ホーム", Icons.Default.Home, "/erp/production", isHome = true),
                        AppMenuNode.Group(
                            code = "ERP_PRODUCTION_PLANNING",
                            label = "生産計画",
                            icon = Icons.Default.CalendarMonth,
                            children = listOf(
                                AppMenuNode.Leaf("ERP_PRODUCTION_DATA", "生産データ管理", Icons.Default.List, "/erp/production/data-management"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_BASELINE", "計画ベースライン", Icons.Default.TrendingUp, "/erp/production/plan-baseline"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_PLAN_SCHEDULES", "生産スケジュール", Icons.Default.List, "/erp/production/plan-schedules"),
                                AppMenuNode.Leaf("ERP_PRODUCTION_PROCESS_MACHINE_PLAN", "工程別設備別計画", Icons.Default.TrendingUp, "/erp/production/process-machine-plan"),
                            ),
                        ),
                        AppMenuNode.Leaf("ERP_PRODUCTION_REQUIREMENTS", "生産需要量", Icons.Default.TrendingUp, "/erp/production-requirements/material"),
                        AppMenuNode.Leaf("ERP_PRODUCTION_METRICS", "生産指標", Icons.Default.TrendingUp, "/erp/production/metrics/scrap-rate"),
                        AppMenuNode.Leaf("ERP_PRODUCTION_RESULT", "生産実績", Icons.Default.Description, "/erp/production/actual-management"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "ERP_SHIPPING",
                    label = "出荷管理",
                    icon = Icons.Default.LocalShipping,
                    children = listOf(
                        AppMenuNode.Leaf("ERP_SHIPPING_HOME", "出荷ホーム", Icons.Default.Home, "/erp/shipping", isHome = true),
                        AppMenuNode.Leaf("ERP_SHIPPING_LIST", "出荷構成表管理", Icons.Default.List, "/erp/shipping/list"),
                        AppMenuNode.Leaf("ERP_SHIPPING_REPORT", "出荷報告書管理", Icons.Default.Description, "/erp/shipping/report"),
                        AppMenuNode.Leaf("ERP_SHIPPING_OVERVIEW", "出荷予定表発行", Icons.Default.CalendarMonth, "/erp/shipping/overview"),
                        AppMenuNode.Leaf("ERP_SHIPPING_CONFIRM", "出荷確認リスト", Icons.Default.List, "/erp/shipping/confirm"),
                        AppMenuNode.Leaf("ERP_SHIPPING_WELDING", "溶接出荷管理", Icons.Default.Description, "/erp/shipping/welding"),
                        AppMenuNode.Leaf("ERP_SHIPPING_PICKING", "ピッキング管理", Icons.Default.GridView, "/erp/shipping/picking"),
                        AppMenuNode.Leaf("ERP_SHIPPING_INVENTORY_SHORTAGE", "倉庫在庫管理", Icons.Default.Inventory2, "/erp/shipping/inventory-shortage"),
                        AppMenuNode.Leaf("ERP_SHIPPING_ABC", "ABC分析", Icons.Default.Analytics, "/erp/shipping/abc-analysis"),
                        AppMenuNode.Leaf("ERP_SHIPPING_INVENTORY_KPI", "在庫KPI・アラート", Icons.Default.Analytics, "/erp/shipping/inventory-kpi"),
                        AppMenuNode.Leaf("ERP_SHIPPING_WAREHOUSE_DAILY", "倉庫日次在庫", Icons.Default.List, "/erp/shipping/warehouse-daily"),
                    ),
                ),
            ),
        ),

        // ===== APS =====
        AppMenuNode.Group(
            code = "APS",
            label = "APS管理メニュー",
            icon = Icons.Default.Analytics,
            children = listOf(
                AppMenuNode.Group(
                    code = "APS_PRODUCTION_PLAN_CREATE",
                    label = "生産計画作成",
                    icon = Icons.Default.CalendarMonth,
                    children = listOf(
                        AppMenuNode.Leaf("APS_CUTTING_PLANNING", "切断計画作成", Icons.Default.Settings, "/aps/cutting-planning"),
                        AppMenuNode.Leaf("APS_PLANNING", "成型計画作成", Icons.Default.Description, "/aps/planning"),
                        AppMenuNode.Leaf("APS_WELDING_PLANNING", "溶接計画作成", Icons.Default.Description, "/aps/welding-planning"),
                        AppMenuNode.Leaf("APS_PLATING_PLANNING", "メッキ計画作成", Icons.Default.Description, "/aps/plating-planning"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "APS_PRODUCTION_PLAN_VIEW",
                    label = "生産計画一覧",
                    icon = Icons.Default.List,
                    children = listOf(
                        AppMenuNode.Leaf("APS_FORMING_PLAN_LIST", "成型計画一覧", Icons.Default.List, "/aps/planning-list"),
                        AppMenuNode.Leaf("APS_WELDING_PLAN_LIST", "溶接計画一覧", Icons.Default.List, "/aps/welding-planning-list"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "APS_EQUIPMENT_UTILIZATION_MANAGEMENT",
                    label = "設備稼働管理",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("APS_CAPACITY", "設備稼働設定", Icons.Default.Settings, "/aps/capacity"),
                        AppMenuNode.Leaf("APS_CAPACITY_MATRIX", "設備稼働時間表", Icons.Default.GridView, "/aps/capacity-matrix"),
                        AppMenuNode.Leaf("APS_DAILY_REPORT", "日別設備計画表", Icons.Default.List, "/aps/daily-report"),
                    ),
                ),
                AppMenuNode.Leaf(
                    code = "APS_SCHEDULING",
                    label = "スケジューリング",
                    icon = Icons.Default.TrendingUp,
                    path = "/aps/scheduling",
                ),
            ),
        ),

        // ===== MES =====
        AppMenuNode.Group(
            code = "MES",
            label = "MES管理メニュー",
            icon = Icons.Default.Monitor,
            children = listOf(
                AppMenuNode.Group(
                    code = "MES_PRODUCTION_INSTRUCTION",
                    label = "生産指示",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_CUTTING", "切断・面取指示", Icons.Default.Settings, "/mes/productionInstruction/cutting"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_FORMING", "成型指示", Icons.Default.Inventory2, "/mes/productionInstruction/forming"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_WELDING", "溶接指示", Icons.Default.Monitor, "/mes/productionInstruction/welding"),
                        AppMenuNode.Leaf("MES_PRODUCTION_INSTR_PLATING", "メッキ指示", Icons.Default.Description, "/mes/productionInstruction/plating"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MES_ACTUAL_DATA_COLLECTION",
                    label = "実績収集",
                    icon = Icons.Default.Monitor,
                    children = listOf(
                        AppMenuNode.Leaf("MES_PRODUCTION_MONITOR", "生産モニター", Icons.Default.Monitor, "/mes/actualDataCollection/monitor"),
                        AppMenuNode.Leaf("MES_ACTUAL_CUTTING", "切断実績収集", Icons.Default.Settings, "/mes/actualDataCollection/cutting"),
                        AppMenuNode.Leaf("MES_ACTUAL_CHAMFERING", "面取実績収集", Icons.Default.Settings, "/mes/actualDataCollection/chamfering"),
                        AppMenuNode.Leaf("MES_ACTUAL_FORMING", "成型実績収集", Icons.Default.Inventory2, "/mes/actualDataCollection/forming"),
                        AppMenuNode.Leaf("MES_ACTUAL_PLATING", "メッキ実績収集", Icons.Default.Description, "/mes/actualDataCollection/plating"),
                        AppMenuNode.Leaf("MES_ACTUAL_WELDING", "溶接実績収集", Icons.Default.Monitor, "/mes/actualDataCollection/welding"),
                        AppMenuNode.Leaf("MES_ACTUAL_INSPECTION", "検査実績収集", Icons.Default.GridView, "/mes/actualDataCollection/inspection"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MES_ACTUAL_ANALYSIS",
                    label = "実績分析項目",
                    icon = Icons.Default.Analytics,
                    children = listOf(
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_PRODUCTIVITY",
                            label = "生産性分析",
                            icon = Icons.Default.TrendingUp,
                            analysisCategory = "productivity",
                            categoryLabel = "生産性",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_UTILIZATION",
                            label = "稼働率分析",
                            icon = Icons.Default.Speed,
                            analysisCategory = "utilization",
                            categoryLabel = "稼働率",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_PROGRESS",
                            label = "進捗分析",
                            icon = Icons.Default.Timeline,
                            analysisCategory = "progress",
                            categoryLabel = "進捗",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_QUALITY",
                            label = "品質分析",
                            icon = Icons.Default.CheckCircle,
                            analysisCategory = "quality",
                            categoryLabel = "品質",
                        ),
                        mesActualAnalysisCategoryGroup(
                            code = "MES_ACTUAL_ANALYSIS_COST",
                            label = "コスト分析",
                            icon = Icons.Default.BarChart,
                            analysisCategory = "cost",
                            categoryLabel = "コスト",
                        ),
                    ),
                ),
            ),
        ),

        // ===== MASTER =====
        AppMenuNode.Group(
            code = "MASTER",
            label = "マスタ管理",
            icon = Icons.Default.Storage,
            children = listOf(
                AppMenuNode.Group(
                    code = "MASTER_LIST",
                    label = "マスタ",
                    icon = Icons.Default.List,
                    children = listOf(
                        AppMenuNode.Leaf("MASTER_HOME", "マスタホーム", Icons.Default.Home, "/master", isHome = true),
                        AppMenuNode.Leaf("MASTER_PRODUCT", "製品マスタ", Icons.Default.Description, "/master/product"),
                        AppMenuNode.Leaf("MASTER_MATERIAL", "材料マスタ", Icons.Default.Description, "/master/material"),
                        AppMenuNode.Leaf("MASTER_MATERIAL_INSPECTION", "材料検品マスタ", Icons.Default.Description, "/master/material-inspection"),
                        AppMenuNode.Leaf("MASTER_PART", "部品マスタ", Icons.Default.Description, "/master/part"),
                        AppMenuNode.Leaf("MASTER_SUPPLIER", "仕入先マスタ", Icons.Default.Description, "/master/supplier"),
                        AppMenuNode.Leaf("MASTER_PROCESS", "工程マスタ", Icons.Default.Description, "/master/process"),
                        AppMenuNode.Leaf("MASTER_PROCESS_ROUTE", "工程ルートマスタ", Icons.Default.Description, "/master/process-route"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_PROCESS_ROUTE", "製品ルートマスタ", Icons.Default.Description, "/master/product-process-route"),
                        AppMenuNode.Leaf("MASTER_CUSTOMER", "顧客マスタ", Icons.Default.Description, "/master/customer"),
                        AppMenuNode.Leaf("MASTER_CARRIER", "運送便マスタ", Icons.Default.Description, "/master/carrier"),
                        AppMenuNode.Leaf("MASTER_MACHINE", "設備マスタ", Icons.Default.Description, "/master/machine"),
                        AppMenuNode.Leaf("MASTER_ROLLER_MASTER", "ローラーマスタ", Icons.Default.Description, "/master/roller-master"),
                        AppMenuNode.Leaf("MASTER_DESTINATION", "納入先マスタ", Icons.Default.Description, "/master/destination"),
                        AppMenuNode.Leaf("MASTER_DESTINATION_HOLIDAY", "納入先休日設定", Icons.Default.Description, "/master/destination/holiday"),
                        AppMenuNode.Leaf("MASTER_COMPANY_WORK_CALENDAR", "会社稼働カレンダー", Icons.Default.CalendarMonth, "/master/company-work-calendar"),
                        AppMenuNode.Leaf("MASTER_PROCESS_PROCESSING_FEE", "工程加工費マスタ", Icons.Default.TrendingUp, "/master/bom/process-processing-fee"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "MASTER_BOM",
                    label = "BOM",
                    icon = Icons.Default.GridView,
                    children = listOf(
                        AppMenuNode.Leaf("MASTER_BOM_HOME", "BOMホーム", Icons.Default.Home, "/master/bom", isHome = true),
                        AppMenuNode.Leaf("MASTER_PRODUCT_PROCESS_BOM", "製品工程BOM", Icons.Default.GridView, "/master/bom/product-process"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_MACHINE_CONFIG", "製品機器設定", Icons.Default.GridView, "/master/bom/product-machine-config"),
                        AppMenuNode.Leaf("MASTER_EQUIPMENT_EFFICIENCY", "設備能率管理", Icons.Default.Analytics, "/master/bom/equipment-efficiency"),
                        AppMenuNode.Leaf("MASTER_ROLLER_BOM", "ローラーBOM管理", Icons.Default.GridView, "/master/bom/roller-bom"),
                        AppMenuNode.Leaf("MASTER_PRODUCT_BOM_DETAIL", "製品BOM表管理", Icons.Default.GridView, "/master/bom/product-bom"),
                        AppMenuNode.Leaf("MASTER_PROCESS_DEFECT_ITEMS", "工程別不良項目マスタ", Icons.Default.Description, "/master/bom/process-defect-items"),
                        AppMenuNode.Leaf("MASTER_UNIT_PRICE", "工程別標準原価", Icons.Default.Description, "/master/bom/product-unit-price"),
                    ),
                ),
            ),
        ),

        // ===== SYSTEM =====
        AppMenuNode.Group(
            code = "SYSTEM",
            label = "システム管理",
            icon = Icons.Default.Settings,
            children = listOf(
                AppMenuNode.Group(
                    code = "SYSTEM_USER",
                    label = "ユーザー・組織",
                    icon = Icons.Default.Business,
                    children = listOf(
                        AppMenuNode.Leaf("SYSTEM_HOME", "システムホーム", Icons.Default.Home, "/system", isHome = true),
                        AppMenuNode.Leaf("SYSTEM_USERS", "ユーザー管理", Icons.Default.Person, "/system/users"),
                        AppMenuNode.Leaf("SYSTEM_ORG", "組織・部門管理", Icons.Default.Business, "/system/organization"),
                        AppMenuNode.Leaf("SYSTEM_ROLE", "権限・ロール管理", Icons.Default.Settings, "/system/roles"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "SYSTEM_SETTINGS",
                    label = "システム設定",
                    icon = Icons.Default.Settings,
                    children = listOf(
                        AppMenuNode.Leaf("SYSTEM_NUMBERING", "採番ルール管理", Icons.Default.Description, "/system/numbering"),
                        AppMenuNode.Leaf("SYSTEM_WORKFLOW", "ワークフロー設定", Icons.Default.Description, "/system/workflow"),
                        AppMenuNode.Leaf("SYSTEM_NOTIFICATION", "通知センター", Icons.Default.Description, "/system/notification"),
                        AppMenuNode.Leaf("SYSTEM_LOGS", "システムログ", Icons.Default.Description, "/system/logs"),
                        AppMenuNode.Leaf("SYSTEM_DATA", "データ管理", Icons.Default.Description, "/system/data"),
                        AppMenuNode.Leaf("SYSTEM_MENUS", "メニュー管理", Icons.Default.Description, "/system/menus"),
                        AppMenuNode.Leaf("SYSTEM_FILE_WATCHER", "ファイル監視設定", Icons.Default.Description, "/system/file-watcher"),
                    ),
                ),
                AppMenuNode.Group(
                    code = "SYSTEM_DATABASE",
                    label = "データベース",
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

    /** 同一路由可能对应多个菜单 code（例如各模块ホーム） */
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

    /** 按登录用户的 menu_codes（角色菜单权限）过滤侧栏 */
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
    MesActualAnalysisProcess("CUTTING", "切断", "cutting", Icons.Default.Settings),
    MesActualAnalysisProcess("CHAMFERING", "面取", "chamfering", Icons.Default.Build),
    MesActualAnalysisProcess("FORMING", "成型", "forming", Icons.Default.Inventory2),
    MesActualAnalysisProcess("PLATING", "メッキ", "plating", Icons.Default.Description),
    MesActualAnalysisProcess("WELDING", "溶接", "welding", Icons.Default.Monitor),
    MesActualAnalysisProcess("INSPECTION", "検査", "inspection", Icons.Default.CheckCircle),
)

/** Web `menuConfig.ts` と同じ命名（例: 切断生産性 / 切断稼働率）で leaf を生成する */
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
