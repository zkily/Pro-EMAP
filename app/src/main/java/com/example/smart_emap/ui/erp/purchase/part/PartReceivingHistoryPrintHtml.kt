package com.example.smart_emap.ui.erp.purchase.part

import com.example.smart_emap.data.model.PartLogItemDto
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun buildPartReceivingHistoryPrintHtml(rows: List<PartLogItemDto>): String {
    val printDate = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
        .format(DateTimeFormatter.ofPattern("yyyy/M/d H:mm"))
    val tableRows = rows.joinToString("\n") { row ->
        """
        <tr>
          <td>${escapeReceivingHtml(row.logDate.orEmpty().ifBlank { "-" })}</td>
          <td>${escapeReceivingHtml(row.logTime.orEmpty().ifBlank { "-" })}</td>
          <td class="text-left">${escapeReceivingHtml(row.item.orEmpty().ifBlank { "-" })}</td>
          <td class="text-left">${escapeReceivingHtml(row.partName.orEmpty().ifBlank { "-" })}</td>
          <td>${escapeReceivingHtml(row.manufactureNo.orEmpty().ifBlank { "-" })}</td>
          <td>${row.quantity ?: "-"}</td>
          <td>${row.outerDiameter1 ?: "-"}</td>
          <td>${row.outerDiameter2 ?: "-"}</td>
          <td class="text-left">${escapeReceivingHtml(row.supplier.orEmpty().ifBlank { "-" })}</td>
          <td>${formatMagnetic(row.magnetic)}</td>
          <td>${formatAppearance(row.appearance)}</td>
          <td class="text-left">${escapeReceivingHtml(row.remarks.orEmpty().ifBlank { "-" })}</td>
        </tr>
        """.trimIndent()
    }
    val body = """
    <div class="print-header">
      <div class="print-title">部品受入履歴</div>
      <div class="print-date">印刷日時: $printDate　表示件数: ${rows.size}件</div>
    </div>
    <table class="print-table">
      <thead>
        <tr>
          <th>日付</th><th>時間</th><th>項目</th><th>部品名</th><th>製造番号</th>
          <th>数量</th><th>外径1</th><th>外径2</th><th>仕入先</th><th>磁気</th><th>外観</th><th>作業員</th>
        </tr>
      </thead>
      <tbody>$tableRows</tbody>
    </table>
    """.trimIndent()
    return """
    <!DOCTYPE html>
    <html lang="ja"><head><meta charset="UTF-8"/><title>部品受入履歴</title>
    <style>
      @page { size: A4 landscape; margin: 10mm; }
      body { font-family: 'Meiryo','Yu Gothic',sans-serif; margin:0; padding:0; font-size:9pt; color:#000; }
      .print-header { text-align:center; margin-bottom:6mm; border-bottom:1.5px solid #333; padding-bottom:2mm; }
      .print-title { font-size:16pt; font-weight:bold; margin-bottom:1.5mm; }
      .print-date { font-size:9pt; color:#666; }
      .print-table { width:100%; border-collapse:collapse; margin-top:5mm; font-size:8pt; }
      .print-table th, .print-table td { border:1px solid #333; padding:2mm 1mm; text-align:center; }
      .print-table th { background:#f5f5f5; font-weight:bold; }
      .text-left { text-align:left; }
    </style></head><body>$body</body></html>
    """.trimIndent()
}

private fun formatMagnetic(value: String?): String = when {
    value.isNullOrBlank() -> "無"
    value == "1" || value.equals("true", true) || value == "有" -> "有"
    else -> value
}

private fun formatAppearance(value: String?): String = when {
    value.isNullOrBlank() -> "不良"
    value == "1" || value.equals("true", true) || value == "良" -> "良"
    else -> value
}

private fun escapeReceivingHtml(value: String): String =
    value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
