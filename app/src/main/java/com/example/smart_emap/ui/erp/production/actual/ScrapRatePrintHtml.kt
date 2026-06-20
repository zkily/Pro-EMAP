package com.example.smart_emap.ui.erp.production.actual

import com.example.smart_emap.data.model.MainLineLabelDto
import com.example.smart_emap.data.model.QualityProductRowDto
import java.text.NumberFormat
import java.util.Locale

private val printNumber = NumberFormat.getIntegerInstance(Locale.JAPAN)

private fun fmtIntPrint(value: Double?): String {
    if (value == null || value.isNaN()) return "—"
    return printNumber.format(value.toLong())
}

private fun escScrapHtml(text: String?): String = (text ?: "")
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")

private fun procCell(row: QualityProductRowDto, key: String): String {
    val p = row.processes?.firstOrNull { it.key == key }
    val rp = p?.ratePercent ?: return "—"
    return "%.2f %%".format(rp)
}

private fun rtyLossCell(row: QualityProductRowDto): String {
    val x = computeProductMainLineRty(row) ?: return "—"
    return "%.2f %%".format(x.second)
}

private fun rtyCell(row: QualityProductRowDto): String {
    val x = computeProductMainLineRty(row) ?: return "—"
    return "%.2f %%".format(x.first)
}

/** 製品別集計の印刷用 HTML（A4 横）。Web の buildProductPrintHtml に対応 */
fun buildScrapProductPrintHtml(
    rows: List<QualityProductRowDto>,
    total: Int,
    dateFrom: String,
    dateTo: String,
    mainLineLabels: List<MainLineLabelDto>,
    filterProductCd: String,
    sortLabel: String,
): String {
    val thProc = mainLineLabels.joinToString("") { "<th>${escScrapHtml(it.label)}（％）</th>" }
    val bodyRows = rows.joinToString("") { row ->
        val tds = buildString {
            append("<td class=\"num\">${escScrapHtml(row.productCd)}</td>")
            append("<td>${escScrapHtml(row.productName)}</td>")
            append("<td class=\"num\">${escScrapHtml(fmtIntPrint(row.allProcessesDefectScrap))}</td>")
            mainLineLabels.forEach { append("<td class=\"num\">${escScrapHtml(procCell(row, it.key ?: ""))}</td>") }
            append("<td class=\"num\">${escScrapHtml(rtyLossCell(row))}</td>")
            append("<td class=\"num\">${escScrapHtml(rtyCell(row))}</td>")
        }
        "<tr>$tds</tr>"
    }
    val filterLine = if (filterProductCd.isNotBlank()) {
        "製品絞込: ${escScrapHtml(filterProductCd)}"
    } else {
        "製品絞込: （すべて）"
    }
    val truncated = total > rows.size
    val note = if (truncated) {
        "<p class=\"note\">※ 全 ${fmtIntPrint(total.toDouble())} 件中 ${fmtIntPrint(rows.size.toDouble())} 件を印刷</p>"
    } else {
        "<p class=\"meta\">件数: ${fmtIntPrint(rows.size.toDouble())} 件</p>"
    }

    return """<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="utf-8" />
  <title>製品別集計</title>
  <style>
    @page { size: A4 landscape; margin: 8mm 10mm; }
    html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    body {
      font-family: 'Hiragino Sans', 'Meiryo', sans-serif;
      font-size: 11px; color: #0f172a; margin: 14px 18px; box-sizing: border-box;
    }
    h1 { font-size: 15px; margin: 0 0 8px; }
    .meta { margin: 3px 0; color: #475569; font-size: 10.5px; }
    .note { margin: 6px 0 0; color: #b45308; font-size: 10px; }
    table { border-collapse: collapse; width: 100%; margin-top: 8px; table-layout: fixed; }
    th, td { border: 1px solid #94a3b8; padding: 3px 5px; vertical-align: middle; word-wrap: break-word; }
    th { background: #e2e8f0; font-weight: 600; text-align: center; font-size: 10px; }
    td.num { text-align: right; font-variant-numeric: tabular-nums; }
  </style>
</head>
<body>
  <h1>廃棄率分析 · 製品別集計</h1>
  <p class="meta">集計期間: ${escScrapHtml(dateFrom)} ～ ${escScrapHtml(dateTo)}</p>
  <p class="meta">$filterLine</p>
  <p class="meta">並び順: ${escScrapHtml(sortLabel)}</p>
  $note
  <table>
    <thead>
      <tr>
        <th>製品CD</th>
        <th>製品名</th>
        <th>不良＋廃棄</th>
        $thProc
        <th>廃棄率</th>
        <th>合格率</th>
      </tr>
    </thead>
    <tbody>$bodyRows</tbody>
  </table>
</body>
</html>"""
}
