package com.example.smart_emap.ui.erp.order

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val printTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)
private val japanZone = ZoneId.of("Asia/Tokyo")

private fun escHtml(s: String): String = s
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun formatPrintTime(): String =
    LocalDateTime.now(japanZone).format(printTimeFormatter)

private fun build2DTableHtml(
    items: List<DestinationHistoryItemUi>,
    numberFormat: NumberFormat,
): String {
    val products = items.map { it.productName }.distinct().sortedWith(java.text.Collator.getInstance(Locale.JAPAN))
    val dates = items.map { it.date }.distinct().sorted()

    val qtyMap = mutableMapOf<String, Int>()
    val dateTotals = mutableMapOf<String, Int>()
    for (item in items) {
        val key = "${item.productName}__${item.date}"
        qtyMap[key] = (qtyMap[key] ?: 0) + item.quantity
        dateTotals[item.date] = (dateTotals[item.date] ?: 0) + item.quantity
    }

    val sb = StringBuilder()
    sb.append("""<table class="table-2d"><thead><tr><th>製品名</th>""")
    for (date in dates) {
        sb.append("""<th class="date-header">${escHtml(date)}</th>""")
    }
    sb.append("<th>合計</th></tr></thead><tbody>")

    var grandTotal = 0
    for (product in products) {
        var productTotal = 0
        sb.append("""<tr><td class="product-name">${escHtml(product)}</td>""")
        for (date in dates) {
            val key = "${product}__${date}"
            val qty = qtyMap[key] ?: 0
            productTotal += qty
            val cell = if (qty > 0) numberFormat.format(qty) else ""
            sb.append("""<td class="number">$cell</td>""")
        }
        grandTotal += productTotal
        sb.append("""<td class="number total">${numberFormat.format(productTotal)}</td></tr>""")
    }

    sb.append("""<tr class="total-row"><td class="total-label">合計</td>""")
    var dateGrandSum = 0
    for (date in dates) {
        val dateTotal = dateTotals[date] ?: 0
        dateGrandSum += dateTotal
        val cell = if (dateTotal > 0) numberFormat.format(dateTotal) else ""
        sb.append("""<td class="number total">$cell</td>""")
    }
    val footerTotal = if (grandTotal > 0) grandTotal else dateGrandSum
    sb.append("""<td class="number grand-total">${numberFormat.format(footerTotal)}</td></tr>""")
    sb.append("</tbody></table>")
    return sb.toString()
}

fun buildDestinationHistoryPrintHtml(
    items: List<DestinationHistoryItemUi>,
    summary: List<DestinationHistorySummaryUi>,
    destinationLabel: String,
    startDate: String,
    endDate: String,
    numberFormat: NumberFormat,
): String {
    val filterParts = mutableListOf<String>()
    if (startDate.isNotBlank() && endDate.isNotBlank()) {
        filterParts.add("期間: $startDate ~ $endDate")
    }
    if (destinationLabel.isNotBlank()) {
        filterParts.add("納入先: ${escHtml(destinationLabel)}")
    }
    val filterText = if (filterParts.isNotEmpty()) {
        """<div class="filter-info">検索条件: ${filterParts.joinToString(" / ")}</div>"""
    } else {
        ""
    }

    val summaryRows = summary.joinToString("") { row ->
        """<tr>
            <td class="center">${escHtml(row.ym)}</td>
            <td class="number">${numberFormat.format(row.totalQuantity)}</td>
        </tr>"""
    }
    val summaryHtml = """
        <table class="summary-table">
          <thead><tr><th>年月</th><th>受注数量合計</th></tr></thead>
          <tbody>$summaryRows</tbody>
        </table>
    """.trimIndent()

    val listRows = items.joinToString("") { row ->
        """<tr>
            <td class="center">${escHtml(row.date)}</td>
            <td>${escHtml(row.destinationName)}</td>
            <td>${escHtml(row.productName)}</td>
            <td class="number">${numberFormat.format(row.quantity)}</td>
            <td>${escHtml(row.status.ifBlank { "-" })}</td>
            <td class="center">${escHtml(row.deliveryDate.ifBlank { "-" })}</td>
        </tr>"""
    }
    val listHtml = """
        <table class="list-table">
          <thead>
            <tr>
              <th>出荷日</th><th>納入先名</th><th>製品名</th>
              <th>数量</th><th>状態</th><th>納入日</th>
            </tr>
          </thead>
          <tbody>$listRows</tbody>
        </table>
    """.trimIndent()

    val table2dHtml = build2DTableHtml(items, numberFormat)

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <title>納入先別受注履歴</title>
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            @page { size: A4 landscape; margin: 8mm 10mm; }
            body {
              font-family: "Yu Gothic", "Hiragino Kaku Gothic Pro", "Meiryo", sans-serif;
              padding: 10px 12px;
              color: #000;
              font-size: 11px;
              line-height: 1.2;
            }
            .print-info {
              text-align: right;
              font-size: 9.5px;
              margin-bottom: 8px;
            }
            h2 {
              text-align: center;
              font-size: 16px;
              margin-bottom: 10px;
              padding-bottom: 6px;
              border-bottom: 1px solid #000;
            }
            h3 {
              font-size: 12px;
              margin: 10px 0 6px;
            }
            .filter-info {
              margin: 4px 0 10px;
              padding: 4px 6px;
              background: #f8f9fa;
              border-radius: 2px;
              font-size: 9.5px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              margin-bottom: 10px;
            }
            th, td {
              border: 1px solid #000;
              padding: 3px 4px;
              text-align: center;
              vertical-align: middle;
            }
            th {
              background: #f0f0f0;
              font-weight: bold;
              font-size: 9px;
            }
            .table-2d th { font-size: 8px; background: #e6e6e6; }
            .table-2d td { font-size: 8.5px; }
            .date-header {
              writing-mode: vertical-rl;
              text-orientation: mixed;
              width: 24px;
            }
            .product-name {
              text-align: left;
              font-weight: bold;
              max-width: 160px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            .number { font-family: 'Courier New', monospace; text-align: right; }
            .total { background: #f8f8f8; font-weight: bold; }
            .total-row td { background: #e8e8e8; font-weight: bold; }
            .summary-table th, .summary-table td { font-size: 9px; }
            .list-table th, .list-table td { font-size: 9.5px; }
            .center { text-align: center; }
            @media print {
              body { padding: 6px; }
              tr { page-break-inside: avoid; }
            }
          </style>
        </head>
        <body>
          <div class="print-info">印刷日時: ${formatPrintTime()}</div>
          <h2>納入先別受注履歴</h2>
          $filterText
          <h3>月別集計</h3>
          $summaryHtml
          <h3>出荷明細（二次元表）</h3>
          $table2dHtml
          <h3>出荷明細（一覧表）</h3>
          $listHtml
        </body>
        </html>
    """.trimIndent()
}
