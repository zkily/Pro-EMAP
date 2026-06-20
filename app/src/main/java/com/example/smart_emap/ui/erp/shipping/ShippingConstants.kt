package com.example.smart_emap.ui.erp.shipping

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warehouse
import java.text.NumberFormat
import java.util.Locale

data class ShippingStatCard(
    val key: String,
    val label: String,
    val path: String,
    val startColor: Long,
    val endColor: Long,
)

data class ShippingModuleCard(
    val path: String,
    val title: String,
    val description: String,
    val startColor: Long,
    val endColor: Long,
    val icon: ImageVector,
)

fun formatShippingNum(value: Number?): String {
    if (value == null) return "0"
    val n = value.toDouble()
    if (n >= 1_000_000_000) return String.format(Locale.JAPAN, "%.1fB", n / 1_000_000_000)
    if (n >= 1_000_000) return String.format(Locale.JAPAN, "%.1fM", n / 1_000_000)
    if (n >= 1_000) return String.format(Locale.JAPAN, "%.1fK", n / 1_000)
    return NumberFormat.getNumberInstance(Locale.JAPAN).format(n)
}

val SHIPPING_HOME_STAT_CARDS: List<ShippingStatCard> = listOf(
    ShippingStatCard("pallets", "本日パレット", "/erp/shipping/picking", 0xFF667EEA, 0xFF764BA2),
    ShippingStatCard("pending", "未ピッキング", "/erp/shipping/picking", 0xFFE6A23C, 0xFFF7BA2A),
    ShippingStatCard("completed", "本日完了", "/erp/shipping/picking", 0xFF67C23A, 0xFF85CE61),
    ShippingStatCard("inventory", "現在在庫数", "/erp/shipping/inventory-shortage", 0xFF409EFF, 0xFF66B1FF),
    ShippingStatCard("shortage", "倉庫不足", "/erp/shipping/inventory-shortage", 0xFFF56C6C, 0xFFF78989),
)

val SHIPPING_HOME_MODULES: List<ShippingModuleCard> = listOf(
    ShippingModuleCard("/erp/shipping/list", "出荷構成表管理", "作成・編集・一覧", 0xFF409EFF, 0xFF66B1FF, Icons.AutoMirrored.Filled.List),
    ShippingModuleCard("/erp/shipping/report", "出荷報告書管理", "印刷・履歴", 0xFF67C23A, 0xFF85CE61, Icons.Default.Description),
    ShippingModuleCard("/erp/shipping/overview", "出荷予定表発行", "予定表印刷", 0xFFE6A23C, 0xFFF7BA2A, Icons.Default.CalendarMonth),
    ShippingModuleCard("/erp/shipping/confirm", "出荷確認リスト", "確認・印刷", 0xFF909399, 0xFFB1B3B8, Icons.Default.CheckCircle),
    ShippingModuleCard("/erp/shipping/welding", "溶接出荷管理", "溶接出荷指示", 0xFFF56C6C, 0xFFF78989, Icons.Default.PrecisionManufacturing),
    ShippingModuleCard("/erp/shipping/picking", "ピッキング管理", "リスト・進捗・履歴", 0xFF667EEA, 0xFF764BA2, Icons.Default.Inventory2),
    ShippingModuleCard("/erp/shipping/inventory-shortage", "倉庫在庫管理", "在庫不足管理", 0xFF9254DE, 0xFFB37FEB, Icons.Default.Warehouse),
    ShippingModuleCard("/erp/shipping/inventory-kpi", "在庫KPI・アラート", "回転率・欠品・発注点", 0xFF2563EB, 0xFF3B82F6, Icons.Default.Analytics),
    ShippingModuleCard("/erp/shipping/warehouse-daily", "倉庫日次在庫", "日次受注・在庫推移", 0xFF7C3AED, 0xFFA855F7, Icons.Default.Assessment),
    ShippingModuleCard("/erp/shipping/abc-analysis", "ABC分析", "品目・納入先分析", 0xFF13C2C2, 0xFF36CFC9, Icons.Default.LocalShipping),
)

enum class ShippingDocumentMode(val title: String, val subtitle: String) {
    REPORT("出荷報告書管理", "出荷報告書の作成・印刷"),
    OVERVIEW("出荷予定表発行", "出荷予定表の発行・印刷"),
    CONFIRM("出荷確認リスト", "出荷確認リストの照会・印刷"),
}

enum class ShippingKpiTab(val label: String) {
    TURNOVER("在庫回転率"),
    AVG_DAYS("平均在庫日数"),
    SHORTAGE("欠品アラート"),
    OVERSTOCK("過剰アラート"),
    REORDER("発注点"),
}

enum class ShippingPickingTab(val label: String) {
    PROGRESS("進捗"),
    GENERATOR("リスト生成"),
    HISTORY("履歴"),
}
