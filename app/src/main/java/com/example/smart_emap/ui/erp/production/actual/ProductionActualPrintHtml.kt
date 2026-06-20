package com.example.smart_emap.ui.erp.production.actual

import com.example.smart_emap.data.model.ProductionActualLogDto
import java.text.NumberFormat
import java.util.Locale

private val pamNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

private fun escPam(text: String?): String = (text ?: "")
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun pamDateTime(iso: String?): String {
    val s = iso.orEmpty()
    if (s.length < 16) return s
    return s.substring(0, 16).replace('T', ' ')
}

/** 取引ログ一覧の印刷用 HTML（A4 横）。Web の handlePrintTable に対応 */
fun buildProductionActualPrintHtml(
    records: List<ProductionActualLogDto>,
    dateFrom: String,
    dateTo: String,
    processLabel: String,
    total: Int,
): String {
    val body = records.joinToString("") { r ->
        val tds = buildString {
            append("<td>${escPam(pamDateTime(r.transactionTime))}</td>")
            append("<td>${escPam(r.processName ?: "-")}</td>")
            append("<td>${escPam(r.transactionType ?: "-")}</td>")
            append("<td>${escPam(r.stockType ?: "-")}</td>")
            append("<td>${escPam(r.targetCd ?: "-")}</td>")
            append("<td>${escPam(r.targetName ?: "-")}</td>")
            append("<td class=\"num\">${pamNumber.format((r.quantity ?: 0.0).toLong())}</td>")
            append("<td>${escPam(r.locationCd ?: "-")}</td>")
            append("<td>${escPam(r.machineName ?: "-")}</td>")
        }
        "<tr>$tds</tr>"
    }
    return """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8" />
  <title>生産実績 取引ログ</title>
  <style>
    @page { size: A4 landscape; margin: 8mm 10mm; }
    html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    body { font-family: 'Hiragino Sans', 'Meiryo', sans-serif; font-size: 11px; color: #0f172a; margin: 14px 18px; }
    h1 { font-size: 15px; margin: 0 0 8px; }
    .meta { margin: 3px 0; color: #475569; font-size: 10.5px; }
    table { border-collapse: collapse; width: 100%; margin-top: 8px; }
    th, td { border: 1px solid #94a3b8; padding: 3px 5px; word-wrap: break-word; }
    th { background: #e2e8f0; font-weight: 600; text-align: center; font-size: 10px; }
    td.num { text-align: right; font-variant-numeric: tabular-nums; }
  </style>
</head>
<body>
  <h1>生産実績管理 · 取引ログ一覧</h1>
  <p class="meta">集計期間: ${escPam(dateFrom)} ～ ${escPam(dateTo)}　工程: ${escPam(processLabel)}</p>
  <p class="meta">件数: ${pamNumber.format(total.toLong())} 件（本ページ ${records.size} 件）</p>
  <table>
    <thead>
      <tr>
        <th>取引日時</th><th>工程</th><th>タイプ</th><th>区分</th>
        <th>製品CD</th><th>製品名</th><th>数量</th><th>保管場所</th><th>設備名</th>
      </tr>
    </thead>
    <tbody>$body</tbody>
  </table>
</body>
</html>"""
}
