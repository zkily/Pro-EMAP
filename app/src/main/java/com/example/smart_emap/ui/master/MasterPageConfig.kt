package com.example.smart_emap.ui.master

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class MasterTableRow(
    val id: Int?,
    val cells: List<String?>,
)

data class MasterFormField(
    val key: String,
    val label: String,
    val required: Boolean = false,
)

data class MasterPageDef(
    val path: String,
    val title: String,
    val subtitle: String,
    val kind: MasterPageKind,
    val columns: List<String>,
    val columnWidths: List<Int>,
    val formFields: List<MasterFormField>,
    val statsLabels: List<String> = listOf("総件数", "表示件数"),
    val secondaryFilterLabel: String? = null,
    val secondaryFilterOptions: List<Pair<String, String>> = emptyList(),
    val supportsCreate: Boolean = true,
    val supportsBatchDelete: Boolean = false,
)

enum class MasterPageKind {
    Home,
    Product,
    Material,
    MaterialInspection,
    Part,
    Supplier,
    Process,
    ProcessRoute,
    ProductProcessRoute,
    ProcessingFee,
    Customer,
    Carrier,
    Machine,
    Roller,
    Destination,
    DestinationHoliday,
}

data class MasterHomeModule(
    val path: String,
    val title: String,
    val description: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val icon: ImageVector,
)

object MasterPageRegistry {
    val homeModules: List<MasterHomeModule> = listOf(
        MasterHomeModule("/master/product", "製品マスタ", "製品情報の登録・編集・管理", Color(0xFF667EEA), Color(0xFF764BA2), Icons.Default.Inventory2),
        MasterHomeModule("/master/material", "材料マスタ", "材料情報の登録・編集・管理", Color(0xFF11998E), Color(0xFF38EF7D), Icons.Default.Category),
        MasterHomeModule("/master/material-inspection", "材料検品マスタ", "仕入先材料検品基準の管理", Color(0xFF909399), Color(0xFFB1B3B8), Icons.Default.CheckCircle),
        MasterHomeModule("/master/part", "部品マスタ", "部品情報の登録・編集・管理", Color(0xFF6366F1), Color(0xFF8B5CF6), Icons.Default.GridView),
        MasterHomeModule("/master/supplier", "仕入先マスタ", "仕入先情報の登録・編集", Color(0xFFF093FB), Color(0xFFF5576C), Icons.Default.AccountBalance),
        MasterHomeModule("/master/process", "工程マスタ", "工程情報の登録・編集", Color(0xFF4FACFE), Color(0xFF00F2FE), Icons.Default.Settings),
        MasterHomeModule("/master/process-route", "工程ルートマスタ", "工程ルートの登録・編集", Color(0xFFFA709A), Color(0xFFFEE140), Icons.Default.Route),
        MasterHomeModule("/master/product-process-route", "製品ルートマスタ", "製品別工程ステップ・設備設定", Color(0xFF4FACFE), Color(0xFF00F2FE), Icons.Default.Timeline),
        MasterHomeModule("/master/bom/process-processing-fee", "工程加工費マスタ", "工程×加工方法の単価管理", Color(0xFF6366F1), Color(0xFF8B5CF6), Icons.Default.Build),
        MasterHomeModule("/master/customer", "顧客マスタ", "顧客情報の登録・編集", Color(0xFFA18CD1), Color(0xFFFBC2EB), Icons.Default.People),
        MasterHomeModule("/master/carrier", "運送便マスタ", "運送便情報の登録・編集", Color(0xFFD299C2), Color(0xFFFEF9D7), Icons.Default.LocalShipping),
        MasterHomeModule("/master/machine", "設備マスタ", "設備情報の登録・編集", Color(0xFF89F7FE), Color(0xFF66A6FF), Icons.Default.PrecisionManufacturing),
        MasterHomeModule("/master/roller-master", "ローラーマスタ", "ローラー情報の登録・編集", Color(0xFF43E97B), Color(0xFF38F9D7), Icons.Default.Factory),
        MasterHomeModule("/master/destination", "納入先マスタ", "納入先情報の登録・編集", Color(0xFFFDDB92), Color(0xFFD1FDFF), Icons.Default.DirectionsCar),
        MasterHomeModule("/master/destination/holiday", "納入先休日設定", "納入先休日・臨時出勤の設定", Color(0xFFE6A23C), Color(0xFFF7BA2A), Icons.Default.CalendarMonth),
    )

    private val pages: Map<String, MasterPageDef> = mapOf(
        "/master/product" to MasterPageDef(
            path = "/master/product",
            title = "製品マスタ",
            subtitle = "Product Master",
            kind = MasterPageKind.Product,
            columns = listOf("製品CD", "製品名", "種別", "カテゴリ", "状態", "納入先"),
            columnWidths = listOf(72, 100, 56, 56, 48, 56),
            formFields = listOf(
                MasterFormField("product_cd", "製品CD", true),
                MasterFormField("product_name", "製品名", true),
                MasterFormField("product_type", "種別"),
                MasterFormField("category", "カテゴリ"),
                MasterFormField("kind", "分類"),
                MasterFormField("status", "状態"),
                MasterFormField("destination_cd", "納入先CD"),
                MasterFormField("route_cd", "ルートCD"),
                MasterFormField("lot_size", "ロットサイズ"),
                MasterFormField("unit_per_box", "箱入数"),
                MasterFormField("material_cd", "材料CD"),
                MasterFormField("note", "備考"),
            ),
        ),
        "/master/material" to MasterPageDef(
            path = "/master/material",
            title = "材料マスタ",
            subtitle = "Material Master",
            kind = MasterPageKind.Material,
            columns = listOf("材料CD", "材料名", "種別", "仕入先", "単価", "状態"),
            columnWidths = listOf(72, 110, 56, 74, 56, 44),
            formFields = listOf(
                MasterFormField("material_cd", "材料CD", true),
                MasterFormField("material_name", "材料名", true),
                MasterFormField("material_type", "種別"),
                MasterFormField("standard_spec", "規格"),
                MasterFormField("unit", "単位"),
                MasterFormField("supplier_cd", "仕入先CD"),
                MasterFormField("unit_price", "単価"),
                MasterFormField("safety_stock", "安全在庫"),
                MasterFormField("status", "状態"),
                MasterFormField("note", "備考"),
            ),
        ),
        "/master/material-inspection" to MasterPageDef(
            path = "/master/material-inspection",
            title = "材料検品マスタ",
            subtitle = "Material Inspection Master",
            kind = MasterPageKind.MaterialInspection,
            columns = listOf("検品CD", "検品規格"),
            columnWidths = listOf(80, 180),
            formFields = listOf(
                MasterFormField("inspection_cd", "検品CD", true),
                MasterFormField("inspection_standard", "検品規格", true),
            ),
            supportsBatchDelete = true,
        ),
        "/master/part" to MasterPageDef(
            path = "/master/part",
            title = "部品マスタ",
            subtitle = "Part Master",
            kind = MasterPageKind.Part,
            columns = listOf("部品CD", "部品名", "区分", "支給区分", "総単価(円)", "状態"),
            columnWidths = listOf(72, 100, 44, 64, 72, 44),
            formFields = listOf(
                MasterFormField("part_cd", "部品CD", true),
                MasterFormField("part_name", "部品名", true),
                MasterFormField("category", "カテゴリ"),
                MasterFormField("kind", "区分"),
                MasterFormField("settlement_type", "支給区分"),
                MasterFormField("uom", "単位"),
                MasterFormField("unit_price", "単価"),
                MasterFormField("material_unit_price", "材料単価"),
                MasterFormField("currency", "通貨"),
                MasterFormField("exchange_rate", "為替"),
                MasterFormField("supplier_cd", "仕入先CD"),
                MasterFormField("status", "状態"),
                MasterFormField("remarks", "備考"),
            ),
            secondaryFilterLabel = "状態",
            secondaryFilterOptions = listOf("" to "全て", "1" to "有効", "0" to "無効"),
        ),
        "/master/supplier" to MasterPageDef(
            path = "/master/supplier",
            title = "仕入先マスタ",
            subtitle = "Supplier Master",
            kind = MasterPageKind.Supplier,
            columns = listOf("仕入先CD", "仕入先名", "担当者", "電話", "メール"),
            columnWidths = listOf(72, 100, 64, 80, 100),
            formFields = listOf(
                MasterFormField("supplier_cd", "仕入先CD", true),
                MasterFormField("supplier_name", "仕入先名", true),
                MasterFormField("supplier_kana", "カナ"),
                MasterFormField("contact_person", "担当者"),
                MasterFormField("phone", "電話"),
                MasterFormField("fax", "FAX"),
                MasterFormField("email", "メール"),
                MasterFormField("postal_code", "郵便番号"),
                MasterFormField("address1", "住所1"),
                MasterFormField("address2", "住所2"),
                MasterFormField("payment_terms", "支払条件"),
                MasterFormField("currency", "通貨"),
                MasterFormField("remarks", "備考"),
            ),
        ),
        "/master/process" to MasterPageDef(
            path = "/master/process",
            title = "工程マスタ",
            subtitle = "Process Master",
            kind = MasterPageKind.Process,
            columns = listOf("工程CD", "工程名", "略称", "分類", "外注", "サイクル"),
            columnWidths = listOf(64, 90, 48, 56, 44, 56),
            formFields = listOf(
                MasterFormField("process_cd", "工程CD", true),
                MasterFormField("process_name", "工程名", true),
                MasterFormField("short_name", "略称"),
                MasterFormField("category", "分類"),
                MasterFormField("is_outsource", "外注(true/false)"),
                MasterFormField("default_cycle_sec", "サイクル(秒)"),
                MasterFormField("default_yield", "良品率"),
                MasterFormField("capacity_unit", "能力単位"),
                MasterFormField("remark", "備考"),
            ),
        ),
        "/master/process-route" to MasterPageDef(
            path = "/master/process-route",
            title = "工程ルートマスタ",
            subtitle = "Process Route Master",
            kind = MasterPageKind.ProcessRoute,
            columns = listOf("ルートCD", "ルート名", "説明", "状態"),
            columnWidths = listOf(72, 100, 120, 48),
            formFields = listOf(
                MasterFormField("route_cd", "ルートCD", true),
                MasterFormField("route_name", "ルート名", true),
                MasterFormField("description", "説明"),
                MasterFormField("is_active", "有効(true/false)"),
                MasterFormField("is_default", "デフォルト(true/false)"),
            ),
        ),
        "/master/bom/process-processing-fee" to MasterPageDef(
            path = "/master/bom/process-processing-fee",
            title = "工程加工費マスタ",
            subtitle = "Process Processing Fee",
            kind = MasterPageKind.ProcessingFee,
            columns = listOf("工程CD", "工程名", "方法CD", "方法名", "単価", "状態"),
            columnWidths = listOf(64, 80, 64, 80, 56, 48),
            formFields = listOf(
                MasterFormField("process_cd", "工程CD", true),
                MasterFormField("method_cd", "方法CD", true),
                MasterFormField("method_name", "方法名"),
                MasterFormField("unit_price", "単価", true),
                MasterFormField("currency", "通貨"),
                MasterFormField("charge_uom", "課金単位"),
                MasterFormField("status", "状態"),
                MasterFormField("remarks", "備考"),
            ),
            secondaryFilterLabel = "工程",
            secondaryFilterOptions = emptyList(),
        ),
        "/master/customer" to MasterPageDef(
            path = "/master/customer",
            title = "顧客マスタ",
            subtitle = "Customer Master",
            kind = MasterPageKind.Customer,
            columns = listOf("顧客CD", "顧客名", "種別", "電話", "状態"),
            columnWidths = listOf(72, 100, 56, 80, 44),
            formFields = listOf(
                MasterFormField("customer_cd", "顧客CD", true),
                MasterFormField("customer_name", "顧客名", true),
                MasterFormField("customer_type", "種別"),
                MasterFormField("phone", "電話"),
                MasterFormField("address", "住所"),
                MasterFormField("status", "状態"),
            ),
            secondaryFilterLabel = "状態",
            secondaryFilterOptions = listOf("" to "全て", "1" to "有効", "0" to "無効"),
        ),
        "/master/carrier" to MasterPageDef(
            path = "/master/carrier",
            title = "運送便マスタ",
            subtitle = "Carrier Master",
            kind = MasterPageKind.Carrier,
            columns = listOf("運送便CD", "運送便名", "電話", "状態"),
            columnWidths = listOf(72, 110, 80, 44),
            formFields = listOf(
                MasterFormField("carrier_cd", "運送便CD", true),
                MasterFormField("carrier_name", "運送便名", true),
                MasterFormField("phone", "電話"),
                MasterFormField("address", "住所"),
                MasterFormField("status", "状態"),
            ),
            secondaryFilterLabel = "状態",
            secondaryFilterOptions = listOf("" to "全て", "1" to "有効", "0" to "無効"),
        ),
        "/master/machine" to MasterPageDef(
            path = "/master/machine",
            title = "設備マスタ",
            subtitle = "Machine Master",
            kind = MasterPageKind.Machine,
            columns = listOf("設備CD", "設備名", "種別", "状態"),
            columnWidths = listOf(72, 110, 64, 48),
            formFields = listOf(
                MasterFormField("machine_cd", "設備CD", true),
                MasterFormField("machine_name", "設備名", true),
                MasterFormField("machine_type", "種別"),
                MasterFormField("status", "状態"),
                MasterFormField("efficiency", "能率"),
                MasterFormField("remark", "備考"),
            ),
            secondaryFilterLabel = "種別",
            secondaryFilterOptions = listOf("" to "全て", "切断" to "切断", "面取" to "面取", "SW" to "SW", "成型" to "成型", "溶接" to "溶接", "メッキ" to "メッキ"),
        ),
        "/master/roller-master" to MasterPageDef(
            path = "/master/roller-master",
            title = "ローラーマスタ",
            subtitle = "Roller Master",
            kind = MasterPageKind.Roller,
            columns = listOf("ローラーCD", "ローラー名", "設備CD", "区分"),
            columnWidths = listOf(72, 100, 72, 56),
            formFields = listOf(
                MasterFormField("roller_cd", "ローラーCD", true),
                MasterFormField("roller_name", "ローラー名"),
                MasterFormField("machine_cd", "設備CD"),
                MasterFormField("category", "区分"),
                MasterFormField("exchange_freq_qty", "交換頻度(数量)"),
                MasterFormField("exchange_freq_month", "交換頻度(月)"),
                MasterFormField("cleaning_freq_month", "清掃頻度(月)"),
                MasterFormField("note", "備考"),
            ),
        ),
        "/master/destination" to MasterPageDef(
            path = "/master/destination",
            title = "納入先マスタ",
            subtitle = "Destination Master",
            kind = MasterPageKind.Destination,
            columns = listOf("納入先CD", "納入先名", "顧客CD", "運送便CD", "状態"),
            columnWidths = listOf(72, 100, 64, 64, 44),
            formFields = listOf(
                MasterFormField("destination_cd", "納入先CD", true),
                MasterFormField("destination_name", "納入先名", true),
                MasterFormField("customer_cd", "顧客CD"),
                MasterFormField("carrier_cd", "運送便CD"),
                MasterFormField("delivery_lead_time", "リードタイム"),
                MasterFormField("issue_type", "発行区分"),
                MasterFormField("phone", "電話"),
                MasterFormField("address", "住所"),
                MasterFormField("status", "状態"),
            ),
            secondaryFilterLabel = "状態",
            secondaryFilterOptions = listOf("" to "全て", "1" to "有効", "0" to "無効"),
        ),
    )

    fun pageForPath(path: String): MasterPageDef? = pages[path]

    fun kindForPath(path: String): MasterPageKind = when (path) {
        "/master" -> MasterPageKind.Home
        "/master/product-process-route" -> MasterPageKind.ProductProcessRoute
        "/master/destination/holiday" -> MasterPageKind.DestinationHoliday
        else -> pageForPath(path)?.kind ?: MasterPageKind.Home
    }
}
