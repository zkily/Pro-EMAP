package com.example.smart_emap.ui.erp.production.planning

import androidx.compose.ui.graphics.Color
import com.example.smart_emap.data.model.InventoryStagnationRowDto
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object InventoryStagnationLogic {
    private val iso = DateTimeFormatter.ISO_LOCAL_DATE
    private val japanZone = ZoneId.of("Asia/Tokyo")

    private val inventoryColumnLabels = mapOf(
        "cutting_inventory" to "切断",
        "chamfering_inventory" to "面取",
        "molding_inventory" to "成型",
        "plating_inventory" to "メッキ",
        "welding_inventory" to "溶接",
        "inspection_inventory" to "検査",
        "warehouse_inventory" to "倉庫",
        "outsourced_warehouse_inventory" to "外注倉庫",
        "outsourced_plating_inventory" to "外注メッキ",
        "outsourced_welding_inventory" to "外注溶接",
        "pre_welding_inspection_inventory" to "溶接前検査",
        "pre_inspection_inventory" to "外注支給前",
        "pre_outsourcing_inventory" to "外注検査前",
    )

    private val inventoryChipColors = mapOf(
        "cutting_inventory" to (Color(0xFFE0F2FE) to Color(0xFF075985)),
        "chamfering_inventory" to (Color(0xFFDCFCE7) to Color(0xFF166534)),
        "molding_inventory" to (Color(0xFFFEF3C7) to Color(0xFF92400E)),
        "plating_inventory" to (Color(0xFFEDE9FE) to Color(0xFF5B21B6)),
        "welding_inventory" to (Color(0xFFFEE2E2) to Color(0xFF991B1B)),
        "inspection_inventory" to (Color(0xFFCFFAFE) to Color(0xFF155E75)),
        "warehouse_inventory" to (Color(0xFFE5E7EB) to Color(0xFF374151)),
        "outsourced_warehouse_inventory" to (Color(0xFFFCE7F3) to Color(0xFF9D174D)),
        "outsourced_plating_inventory" to (Color(0xFFFAE8FF) to Color(0xFF86198F)),
        "outsourced_welding_inventory" to (Color(0xFFFFE4E6) to Color(0xFF9F1239)),
        "pre_welding_inspection_inventory" to (Color(0xFFECFCCB) to Color(0xFF365314)),
        "pre_inspection_inventory" to (Color(0xFFE0E7FF) to Color(0xFF3730A3)),
        "pre_outsourcing_inventory" to (Color(0xFFF1F5F9) to Color(0xFF334155)),
    )

    fun todayJst(): String = LocalDate.now(japanZone).format(iso)

    fun inventoryLabel(column: String): String = inventoryColumnLabels[column] ?: column

    fun inventoryChipColors(column: String): Pair<Color, Color> =
        inventoryChipColors[column] ?: (Color(0xFFEEF2F7) to Color(0xFF374151))

    fun sortRows(rows: List<InventoryStagnationRowDto>): List<InventoryStagnationRowDto> =
        rows.sortedWith(
            compareBy(
                { inventoryLabel(it.inventoryColumn) },
                { it.productName.orEmpty() },
            ),
        )

    fun buildPrintHtml(
        asOfDate: String,
        minQuantity: Int,
        stableDays: Int,
        rows: List<InventoryStagnationRowDto>,
    ): String {
        val grouped = rows.groupBy { inventoryLabel(it.inventoryColumn) }
        val sections = grouped.entries.joinToString("") { (groupName, groupRows) ->
            val body = groupRows.joinToString("") { row ->
                val qty = formatProductionNumber(row.stableQuantity)
                "<tr><td>${escapeHtml(row.productName.orEmpty())}</td>" +
                    "<td class=\"num\">$qty</td>" +
                    "<td>${row.periodStart} ～ ${row.periodEnd}</td></tr>"
            }
            """<section class="group"><h2>$groupName</h2><table><thead><tr><th>製品名</th><th>在庫数</th><th>期間</th></tr></thead><tbody>$body</tbody></table></section>"""
        }
        return """<!doctype html>
<html>
<head>
  <meta charset="utf-8" />
  <title>在庫停滞監視 印刷</title>
  <style>
    @page { size: A4 portrait; margin: 10mm; }
    body { font-family: "Segoe UI", "Yu Gothic UI", sans-serif; color:#1f2937; font-size:12px; }
    .meta { margin-bottom: 8px; font-size: 11px; color:#4b5563; }
    .group { margin-bottom: 10px; break-inside: avoid; }
    h2 { margin: 0 0 6px; font-size: 13px; }
    table { width:100%; border-collapse: collapse; table-layout: fixed; }
    th, td { border: 1px solid #d1d5db; padding: 4px 6px; word-break: break-all; }
    th { background:#f3f4f6; text-align:left; }
    td.num { text-align:right; width: 90px; }
  </style>
</head>
<body>
  <div class="meta">基準日: $asOfDate / 閾値(>): $minQuantity / 連続暦日: $stableDays / 件数: ${rows.size}</div>
  $sections
</body>
</html>"""
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
