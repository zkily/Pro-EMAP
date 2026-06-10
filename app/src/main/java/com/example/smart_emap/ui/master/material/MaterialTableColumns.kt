package com.example.smart_emap.ui.master.material

import com.example.smart_emap.data.model.MasterMaterialDto
import java.util.Locale

data class MaterialTableColumnDef(
    val key: String,
    val label: String,
    val widthDp: Int,
    val isFixed: Boolean = false,
    val defaultVisible: Boolean = true,
)

val materialOptionalColumnDefinitions: List<MaterialTableColumnDef> = listOf(
    MaterialTableColumnDef("material_type", "種類", 72),
    MaterialTableColumnDef("standard_spec", "規格", 80),
    MaterialTableColumnDef("dimensions", "寸法", 88),
    MaterialTableColumnDef("unit", "単位", 48),
    MaterialTableColumnDef("pieces_per_bundle", "束本数", 56),
    MaterialTableColumnDef("supply_classification", "支給区分", 72),
    MaterialTableColumnDef("usegae", "用途", 64),
    MaterialTableColumnDef("supplier_name", "仕入先名", 96),
    MaterialTableColumnDef("price_info", "価格情報", 96),
    MaterialTableColumnDef("stock_info", "リード", 88),
    MaterialTableColumnDef("storage_location", "保管場所", 80),
    MaterialTableColumnDef("status", "状態", 72),
    MaterialTableColumnDef("supplier_cd", "仕入先CD", 72, defaultVisible = false),
    MaterialTableColumnDef("long_weight", "長尺単重", 72, defaultVisible = false),
    MaterialTableColumnDef("tolerance_range", "公差範囲", 72, defaultVisible = false),
    MaterialTableColumnDef("note", "備考", 80, defaultVisible = false),
)

private val materialFixedColumnDefinitions: List<MaterialTableColumnDef> = listOf(
    MaterialTableColumnDef("material_cd", "材料CD", 72, isFixed = true),
    MaterialTableColumnDef("material_name", "材料名", 110, isFixed = true),
)

val materialTableColumnDefinitions: List<MaterialTableColumnDef> =
    materialFixedColumnDefinitions + materialOptionalColumnDefinitions

fun defaultMaterialVisibleColumns(): Map<String, Boolean> =
    materialOptionalColumnDefinitions.associate { it.key to it.defaultVisible }

fun mergeMaterialVisibleColumns(saved: Map<String, Boolean>): Map<String, Boolean> {
    val defaults = defaultMaterialVisibleColumns()
    return defaults.mapValues { (key, default) -> saved[key] ?: default }
}

fun resolveVisibleMaterialColumns(visible: Map<String, Boolean>): List<MaterialTableColumnDef> =
    materialTableColumnDefinitions.filter { col ->
        col.isFixed || (visible[col.key] ?: col.defaultVisible)
    }

fun isMaterialStatusColumn(key: String): Boolean = key == "status"
fun isMaterialRichColumn(key: String): Boolean =
    key in setOf("material_type", "supply_classification", "usegae", "dimensions", "price_info", "stock_info")

fun materialCellText(row: MasterMaterialDto, key: String): String? = when (key) {
    "material_cd" -> row.materialCd
    "material_name" -> row.materialName
    "material_type" -> row.materialType
    "standard_spec" -> row.standardSpec
    "unit" -> row.unit
    "pieces_per_bundle" -> row.piecesPerBundle?.toString()
    "supply_classification" -> row.supplyClassification
    "usegae" -> row.usegae
    "supplier_cd" -> row.supplierCd
    "supplier_name" -> row.supplierName
    "storage_location" -> row.storageLocation
    "long_weight" -> row.longWeight?.let { formatDecimal(it, 3) }
    "tolerance_range" -> row.toleranceRange
    "note" -> row.note
    else -> null
}

fun formatMaterialDimensions(row: MasterMaterialDto): String = buildString {
    row.diameter?.let { append("φ${formatDecimal(it)}mm\n") }
    row.thickness?.let { append("厚${formatDecimal(it)}mm\n") }
    row.length?.let { append("L${formatDecimal(it, 0)}mm") }
}.trim()

fun formatMaterialPriceInfo(row: MasterMaterialDto): String = buildString {
    row.unitPrice?.let { append("単価: ¥${formatNumber(it)}/kg\n") }
    row.singlePrice?.let { append("本価: ¥${formatNumber(it)}") }
}.trim()

fun formatMaterialStockInfo(row: MasterMaterialDto): String = buildString {
    row.safetyStock?.let { append("安全: ${it}束\n") }
    row.leadTime?.let { append("リード: ${it}日") }
}.trim()

private fun formatDecimal(value: Double, decimals: Int = 2): String =
    String.format(Locale.JAPAN, "%.${decimals}f", value).trimEnd('0').trimEnd('.')

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else String.format(Locale.JAPAN, "%.2f", value)
