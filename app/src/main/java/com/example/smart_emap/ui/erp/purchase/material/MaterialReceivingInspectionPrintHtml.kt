package com.example.smart_emap.ui.erp.purchase.material

import com.example.smart_emap.data.model.MaterialLogItemDto

internal fun buildInspectionHistoryPrintHtml(
    rows: List<MaterialLogItemDto>,
    supplierLabel: String,
): String {
    if (rows.isEmpty()) return ""
    val grouped = rows.groupBy { it.logDate.orEmpty() }.toSortedMap()
    val pages = grouped.entries.mapIndexed { index, (date, dayRows) ->
        buildInspectionPrintPage(dayRows, date, index + 1, supplierLabel)
    }
    return """
        <html>
        <head>
          <meta charset="UTF-8"/>
          <title>材料受入チェックシート</title>
          <style>
            body { font-family: 'MS Gothic', monospace; margin: 0; padding: 0; font-size: 14px; line-height: 1.3; }
            @page { margin: 60px 30px 20px 30px; size: A4; }
            .page-break { page-break-before: always; }
            .page-container { padding: 15px 0 0 0; page-break-after: always; }
            .page-container:last-child { page-break-after: avoid; }
            .header { text-align: center; margin-bottom: 15px; position: relative; }
            .main-title { font-size: 22px; font-weight: 900; margin-bottom: 10px; }
            .manager-info { position: absolute; top: 0; right: 0; font-size: 10px; }
            .quality-section { margin-bottom: 15px; text-align: left; }
            .quality-title { font-weight: 900; font-size: 16px; margin-bottom: 4px; }
            .quality-item { margin-bottom: 2px; padding-left: 10px; font-size: 13px; }
            .arrival-info { margin-bottom: 15px; border-top: 1px solid #000; padding-top: 12px; font-size: 19px; font-weight: 600; }
            table { width: 100%; border-collapse: collapse; margin-top: 5px; font-size: 13px; }
            th, td { border: 1px solid #000; padding: 3px 4px; text-align: center; }
            th { background-color: #f0f0f0; font-weight: 900; font-size: 14px; }
            .text-left { text-align: left; }
            .text-right { text-align: right; }
            .footer { margin-top: 8px; text-align: right; font-size: 12px; font-weight: 600; }
            .circle { font-size: 16px; font-weight: 900; }
          </style>
        </head>
        <body>${pages.joinToString("")}</body>
        </html>
    """.trimIndent()
}

private fun buildInspectionPrintPage(
    dayRows: List<MaterialLogItemDto>,
    date: String,
    pageNumber: Int,
    supplierLabel: String,
): String {
    val sorted = dayRows.sortedBy { it.materialName.orEmpty() }
    val rowsHtml = sorted.joinToString("") { item ->
        val od1 = item.outerDiameter1?.let { "%.3f".format(it) } ?: "-"
        val od2 = item.outerDiameter2?.let { "%.3f".format(it) } ?: "-"
        val inspector = item.inspectorName?.takeIf { it.isNotBlank() }
            ?: item.remarks?.takeIf { it.isNotBlank() }
            ?: item.item?.takeIf { it.isNotBlank() }
            ?: "検査員"
        """
        <tr>
          <td class="text-left">${escapeHtml(item.logDate.orEmpty().ifBlank { "-" })}</td>
          <td class="text-left">${escapeHtml(item.materialName.orEmpty().ifBlank { "-" })}</td>
          <td class="text-left">${escapeHtml(item.materialQuality.orEmpty().ifBlank { "-" })}</td>
          <td class="text-right">${item.quantity?.toString() ?: "-"}</td>
          <td class="text-left">${escapeHtml(item.manufactureNo.orEmpty().ifBlank { "-" })}</td>
          <td class="text-right">$od1</td>
          <td class="text-right">$od2</td>
          <td><span class="circle">○</span></td>
          <td><span class="circle">○</span></td>
          <td>${escapeHtml(inspector)}</td>
        </tr>
        """.trimIndent()
    }
    val breakClass = if (pageNumber > 1) " page-break" else ""
    return """
        <div class="page-container$breakClass">
          <div class="header">
            <div class="main-title">材料受入チェックシート</div>
            <div class="manager-info">管理者名 小森 印</div>
          </div>
          <div class="quality-section">
            <div class="quality-title">品質特性:</div>
            <div class="quality-item">① 検品日: 外径・肉厚・長さ・材質を確認し、相違なければ、検品日を記入する</div>
            <div class="quality-item">② 数量: 単位本数</div>
            <div class="quality-item">③ 製造番号: 製造番号を記入する</div>
            <div class="quality-item">④ 外径: 別紙参照 (測定値をn=2記入する事)</div>
            <div class="quality-item">⑤ 磁気: クリップが二つ以上付かないこと</div>
            <div class="quality-item">⑥ 外観: 錆、打痕キズ、擦りキズの無いこと</div>
          </div>
          <div class="arrival-info">入荷日 ${escapeHtml(date)} 仕入先 ${escapeHtml(supplierLabel)}</div>
          <table>
            <thead>
              <tr>
                <th>入荷日</th><th>材料名</th><th>材質</th><th>数量</th><th>製造番号</th>
                <th>外径</th><th>外径</th><th>外観</th><th>磁気</th><th>検査員</th>
              </tr>
            </thead>
            <tbody>$rowsHtml</tbody>
          </table>
          <div class="footer">件数 ${sorted.size}</div>
        </div>
    """.trimIndent()
}

private fun escapeHtml(text: String): String =
    text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

internal fun MaterialLogItemDto.inspectorDisplayName(): String =
    inspectorName?.takeIf { it.isNotBlank() }
        ?: remarks?.takeIf { it.isNotBlank() }
        ?: item?.takeIf { it.isNotBlank() }
        ?: "—"
