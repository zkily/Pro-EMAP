package com.example.smart_emap.ui.master.product

import com.example.smart_emap.data.model.MasterProductDto

const val PRODUCT_MASTER_PAGE_SIZE = 50

data class ProductTableColumnDef(
    val key: String,
    val label: String,
    val widthDp: Int,
    val isFixed: Boolean = false,
    val defaultVisible: Boolean = true,
)

val productOptionalColumnDefinitions: List<ProductTableColumnDef> = listOf(
    ProductTableColumnDef("part_number", "品番", 80),
    ProductTableColumnDef("product_type", "製品種別", 64),
    ProductTableColumnDef("category", "カテゴリ", 56),
    ProductTableColumnDef("kind", "分類", 44),
    ProductTableColumnDef("box_type", "箱種", 48),
    ProductTableColumnDef("unit_per_box", "入数", 40),
    ProductTableColumnDef("process_count", "工程数", 44),
    ProductTableColumnDef("location_cd", "保管場所", 64),
    ProductTableColumnDef("start_use_date", "使用開始日", 72),
    ProductTableColumnDef("status", "状態", 52),
    ProductTableColumnDef("vehicle_model", "対応車種", 72, defaultVisible = false),
    ProductTableColumnDef("dimensions", "サイズ", 64, defaultVisible = false),
    ProductTableColumnDef("weight", "重量(kg)", 52, defaultVisible = false),
    ProductTableColumnDef("lead_time", "リードタイム", 56, defaultVisible = false),
    ProductTableColumnDef("lot_size", "ロットサイズ", 52, defaultVisible = false),
    ProductTableColumnDef("priority", "優先度", 44, defaultVisible = false),
    ProductTableColumnDef("cut_length", "切断長(mm)", 56, defaultVisible = false),
    ProductTableColumnDef("chamfer_length", "面取長(mm)", 56, defaultVisible = false),
    ProductTableColumnDef("developed_length", "展開長(mm)", 56, defaultVisible = false),
    ProductTableColumnDef("take_count", "取り数", 44, defaultVisible = false),
    ProductTableColumnDef("scrap_length", "端材長(mm)", 56, defaultVisible = false),
    ProductTableColumnDef("safety_days", "安全在庫日数", 56, defaultVisible = false),
    ProductTableColumnDef("unit_price", "販売単価", 56, defaultVisible = false),
    ProductTableColumnDef("product_alias", "別名", 72, defaultVisible = false),
    ProductTableColumnDef("is_multistage", "多段階工程", 56, defaultVisible = false),
    ProductTableColumnDef("note", "備考", 80, defaultVisible = false),
)

private val productFixedColumnDefinitions: List<ProductTableColumnDef> = listOf(
    ProductTableColumnDef("product_cd", "製品CD", 72, isFixed = true),
    ProductTableColumnDef("product_name", "製品名称", 110, isFixed = true),
    ProductTableColumnDef("destination_cd", "納入先CD", 72, isFixed = true),
)

val productTableColumnDefinitions: List<ProductTableColumnDef> =
    productFixedColumnDefinitions + productOptionalColumnDefinitions

fun defaultProductVisibleColumns(): Map<String, Boolean> =
    productOptionalColumnDefinitions.associate { it.key to it.defaultVisible }

fun mergeProductVisibleColumns(saved: Map<String, Boolean>): Map<String, Boolean> {
    val defaults = defaultProductVisibleColumns()
    return defaults.mapValues { (key, default) -> saved[key] ?: default }
}

fun resolveVisibleProductColumns(visible: Map<String, Boolean>): List<ProductTableColumnDef> =
    productTableColumnDefinitions.filter { col ->
        col.isFixed || (visible[col.key] ?: col.defaultVisible)
    }

fun isProductStatusColumn(key: String): Boolean = key == "status"

fun productCellText(row: MasterProductDto, key: String): String? = when (key) {
    "product_cd" -> row.productCd
    "product_name" -> row.productName
    "part_number" -> row.partNumber
    "destination_cd" -> row.destinationCd
    "product_type" -> row.productType
    "category" -> row.category
    "kind" -> row.kind
    "box_type" -> row.boxType
    "unit_per_box" -> row.unitPerBox?.toString()
    "process_count" -> row.processCount?.toString()
    "location_cd" -> row.locationCd
    "start_use_date" -> row.startUseDate?.take(10)
    "vehicle_model" -> row.vehicleModel
    "dimensions" -> row.dimensions
    "weight" -> row.weight?.let { formatDecimal(it) }
    "lead_time" -> row.leadTime?.toString()
    "lot_size" -> row.lotSize?.toString()
    "priority" -> when (row.priority) {
        1 -> "高"
        2 -> "中"
        3 -> "低"
        else -> row.priority?.toString()
    }
    "cut_length" -> row.cutLength?.let { formatDecimal(it) }
    "chamfer_length" -> row.chamferLength?.let { formatDecimal(it) }
    "developed_length" -> row.developedLength?.let { formatDecimal(it) }
    "take_count" -> row.takeCount?.toString()
    "scrap_length" -> row.scrapLength?.let { formatDecimal(it) }
    "safety_days" -> row.safetyDays?.toString()
    "unit_price" -> row.unitPrice?.let { formatDecimal(it) }
    "product_alias" -> row.productAlias
    "is_multistage" -> when (row.isMultistage) {
        true -> "はい"
        false -> "いいえ"
        null -> null
    }
    "note" -> row.note
    else -> null
}

private fun formatDecimal(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else String.format("%.2f", value)
