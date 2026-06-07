package com.example.smart_emap.ui.erp.purchase.material

import com.example.smart_emap.data.model.MaterialForecastDetailDto
import com.example.smart_emap.data.model.MaterialForecastStatsDto
import com.example.smart_emap.data.model.MaterialForecastSummaryDto
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun buildMaterialForecastPrintHtml(
    tab: MaterialForecastTab,
    year: Int,
    month: Int,
    details: List<MaterialForecastDetailDto>,
    summary: List<MaterialForecastSummaryDto>,
    stats: MaterialForecastStatsDto,
): String {
    val jpNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)
    val issued = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/M/d H:mm"))
    val title = if (tab == MaterialForecastTab.Detail) "製品別一覧" else "材料別集計"
    val tableHtml = if (tab == MaterialForecastTab.Detail) {
        val header = """
            <tr>
              <th>年</th><th>月</th><th>仕入先</th><th>材料名</th><th>製品名</th>
              <th>内示数量</th><th>ロットサイズ</th><th>材料必要数</th>
            </tr>
        """.trimIndent()
        val rows = details.joinToString("\n") { row ->
            """
            <tr>
              <td>${row.year ?: year}</td>
              <td>${row.month ?: month}</td>
              <td>${escape(row.supplierName)}</td>
              <td>${escape(row.materialName)}</td>
              <td>${escape(row.productName)}</td>
              <td class="num">${jpNumber.format(row.forecastUnits ?: 0)}</td>
              <td class="num">${row.lotSize ?: "-"}</td>
              <td class="num">${formatRequired(row.materialRequired)}</td>
            </tr>
            """.trimIndent()
        }
        "<table><thead>$header</thead><tbody>$rows</tbody></table>"
    } else {
        val header = """
            <tr>
              <th>仕入先</th><th>材料名</th><th>製品数</th>
              <th>内示数量合計</th><th>平均ロットサイズ</th><th>材料必要数合計</th>
            </tr>
        """.trimIndent()
        val rows = summary.joinToString("\n") { row ->
            """
            <tr>
              <td>${escape(row.supplierName)}</td>
              <td>${escape(row.materialName)}</td>
              <td class="num">${row.productCount ?: 0}</td>
              <td class="num">${jpNumber.format(row.totalForecastUnits ?: 0)}</td>
              <td class="num">${row.avgLotSize?.toInt() ?: "-"}</td>
              <td class="num">${formatRequired(row.totalMaterialRequired)}</td>
            </tr>
            """.trimIndent()
        }
        "<table><thead>$header</thead><tbody>$rows</tbody></table>"
    }

    return """
        <!DOCTYPE html>
        <html lang="ja"><head><meta charset="UTF-8"/><title>材料内示管理</title>
        <style>
          body { font-family: "Yu Gothic", sans-serif; font-size: 11px; padding: 12px; }
          h1 { font-size: 16px; margin-bottom: 4px; }
          .meta { color: #64748b; margin-bottom: 12px; }
          .stats { margin-bottom: 12px; line-height: 1.8; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #cbd5e1; padding: 4px 6px; }
          th { background: #f1f5f9; }
          .num { text-align: right; }
        </style></head><body>
          <h1>材料内示管理 - $title</h1>
          <div class="meta">対象: ${year}年${month}月 / 発行: $issued</div>
          <div class="stats">
            製品種類: ${stats.totalProducts ?: 0} /
            材料種類: ${stats.totalMaterials ?: 0} /
            仕入先: ${stats.totalSuppliers ?: 0} /
            内示合計: ${jpNumber.format(stats.totalForecastUnits ?: 0)}本 /
            材料必要数: ${formatRequired(stats.totalMaterialRequired?.toDouble())}
          </div>
          $tableHtml
        </body></html>
    """.trimIndent()
}

private fun formatRequired(value: Double?): String {
    if (value == null) return "-"
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.JAPAN, "%.1f", value)
}

private fun escape(value: String?): String =
    (value ?: "-")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
