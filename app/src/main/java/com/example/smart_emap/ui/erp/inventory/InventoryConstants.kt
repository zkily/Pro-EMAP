package com.example.smart_emap.ui.erp.inventory

import com.example.smart_emap.data.model.MaterialStockItemDto
import com.example.smart_emap.data.model.PartStockItemDto
import com.example.smart_emap.data.model.ProductionSummaryFullRowDto
import java.text.NumberFormat
import java.util.Locale

data class InventoryStatField(
    val key: String,
    val label: String,
)

data class InventoryQuickRoute(
    val path: String,
    val label: String,
    val startColor: Long,
    val endColor: Long,
)

val PRODUCT_INVENTORY_FIELDS: List<InventoryStatField> = listOf(
    InventoryStatField("cutting", "切断"),
    InventoryStatField("chamfering", "面取"),
    InventoryStatField("molding", "成型"),
    InventoryStatField("plating", "メッキ"),
    InventoryStatField("welding", "溶接"),
    InventoryStatField("inspection", "検査"),
    InventoryStatField("warehouse", "倉庫"),
    InventoryStatField("outsourced_warehouse", "外注倉庫"),
    InventoryStatField("outsourced_plating", "外注メッキ"),
    InventoryStatField("outsourced_welding", "外注溶接"),
    InventoryStatField("pre_welding_inspection", "溶接前検査"),
    InventoryStatField("pre_inspection", "支給前"),
    InventoryStatField("pre_outsourcing", "検査前"),
)

val MATERIAL_STAT_FIELDS: List<InventoryStatField> = listOf(
    InventoryStatField("current_stock", "現在在庫"),
    InventoryStatField("safety_stock", "安全在庫"),
    InventoryStatField("planned_usage", "使用数"),
    InventoryStatField("order_quantity", "注文数"),
)

val PART_STAT_FIELDS: List<InventoryStatField> = listOf(
    InventoryStatField("current_stock", "現在在庫"),
    InventoryStatField("planned_usage", "使用数"),
    InventoryStatField("usage_plan_qty", "計画使用"),
    InventoryStatField("order_quantity", "注文数"),
)

val INVENTORY_HOME_QUICK_ROUTES: List<InventoryQuickRoute> = listOf(
    InventoryQuickRoute("/erp/inventory/list", "製品在庫照会", 0xFF409EFF, 0xFF67C23A),
    InventoryQuickRoute("/erp/inventory/material-list", "材料在庫照会", 0xFF14B8A6, 0xFF3B82F6),
    InventoryQuickRoute("/erp/inventory/part-list", "部品在庫照会", 0xFFF97316, 0xFFEA580C),
    InventoryQuickRoute("/erp/inventory/stock-entry", "在庫登録管理", 0xFF67C23A, 0xFF85CE61),
    InventoryQuickRoute("/erp/inventory/stocktake", "棚卸管理", 0xFF667EEA, 0xFF764BA2),
)

val STOCKTAKE_MODULES: List<InventoryQuickRoute> = listOf(
    InventoryQuickRoute("/erp/inventory/stocktake/list", "棚卸リスト一覧", 0xFF409EFF, 0xFF67C23A),
    InventoryQuickRoute("/erp/inventory/stocktake/entry", "棚卸登録", 0xFF667EEA, 0xFF764BA2),
    InventoryQuickRoute("/erp/inventory/stocktake/statistics", "棚卸分析", 0xFF14B8A6, 0xFF3B82F6),
    InventoryQuickRoute("/erp/inventory/stocktake/value", "棚卸金額管理", 0xFFF97316, 0xFFEA580C),
    InventoryQuickRoute("/erp/inventory/stocktake/carryover", "棚卸繰越管理", 0xFF8B5CF6, 0xFF6366F1),
)

val STOCK_TYPES = listOf("製品", "仕掛品", "部品", "材料")

val TRANSACTION_TYPES = listOf("入庫", "出庫", "実績", "不良", "廃棄", "調整", "初期", "移動")

val STOCKTAKE_ITEMS = listOf("材料棚卸", "部品棚卸", "製品棚卸")

fun formatInventoryNum(value: Number?): String {
    if (value == null) return "0"
    return NumberFormat.getNumberInstance(Locale.JAPAN).format(value)
}

fun productInventoryValue(row: ProductionSummaryFullRowDto, key: String): Int = when (key) {
    "cutting" -> row.cuttingInventory ?: 0
    "chamfering" -> row.chamferingInventory ?: 0
    "molding" -> row.moldingInventory ?: 0
    "plating" -> row.platingInventory ?: 0
    "welding" -> row.weldingInventory ?: 0
    "inspection" -> row.inspectionInventory ?: 0
    "warehouse" -> row.warehouseInventory ?: 0
    "outsourced_warehouse" -> row.outsourcedWarehouseInventory ?: 0
    "outsourced_plating" -> row.outsourcedPlatingInventory ?: 0
    "outsourced_welding" -> row.outsourcedWeldingInventory ?: 0
    "pre_welding_inspection" -> row.preWeldingInspectionInventory ?: 0
    "pre_inspection" -> row.preInspectionInventory ?: 0
    "pre_outsourcing" -> row.preOutsourcingInventory ?: 0
    else -> 0
}

fun sumProductInventory(rows: List<ProductionSummaryFullRowDto>, key: String): Int =
    rows.sumOf { productInventoryValue(it, key) }

fun materialStatValue(row: MaterialStockItemDto, key: String): Int = when (key) {
    "current_stock" -> row.currentStock ?: 0
    "safety_stock" -> row.safetyStock ?: 0
    "planned_usage" -> row.plannedUsage ?: 0
    "order_quantity" -> row.orderQuantity ?: 0
    else -> 0
}

fun partStatValue(row: PartStockItemDto, key: String): Int = when (key) {
    "current_stock" -> row.currentStock ?: 0
    "planned_usage" -> row.plannedUsage ?: 0
    "usage_plan_qty" -> row.usagePlanQty ?: 0
    "order_quantity" -> row.orderQuantity ?: 0
    else -> 0
}

fun sumMaterialStat(rows: List<MaterialStockItemDto>, key: String): Int =
    rows.sumOf { materialStatValue(it, key) }

fun sumPartStat(rows: List<PartStockItemDto>, key: String): Int =
    rows.sumOf { partStatValue(it, key) }
